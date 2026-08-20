# AGENTS.md — parser

## Purpose

Go-native parser that turns `.go` source into a protobuf LST using the standard `go/parser`, `go/token`, and `go/types` packages. Separate Go module (`github.com/openrewrite/rewrite-go/parser`) — independent of the Gradle/Maven build.

## Key Files

- `parser.go` — the public API: `New()`, `ParseFile(filename)`, `ParseSource(filename, src)`, `ParsePackage(dir)`. All return `*proto.GoFile`.
- `parser_test.go` — 16 `TestParseXxx` functions, plain stdlib `testing`, no assertion library. Each parses a Go source literal and inspects the proto tree field by field.
- `go.mod` — Go 1.21. Only two deps: `google/uuid`, `google.golang.org/protobuf`.

## Library Plus One Binary

`package parser` is a library. The executable lives in `cmd/parser` and is the Java side's entry point:

```bash
cd parser && go build -o rewrite-go-parser ./cmd/parser
```

It reads a batch of sources from stdin and writes one serialized `proto.GoFile` per source to stdout, in order. Both directions are length-prefixed; responses carry a status byte so a single unparseable file fails alone rather than taking down the batch. The framing is documented at the top of `cmd/parser/main.go` — **change it there and in `GoParser.java` together, or the two silently disagree.**

## Pitfalls

- **Type resolution is effectively single-file.** Both `ParseSource` and `ParsePackage` build `types.Config{Importer: nil}`. With a nil importer, any import cannot be resolved, so type info for cross-package expressions is absent. Do not write a recipe that depends on `GoType.Named.packagePath` being populated for imported symbols.
- **Type errors are swallowed twice over.** `Error: func(err error) {}` discards every type error, and the return value of `conf.Check(...)` is ignored. A file with type errors parses "successfully" with degraded type info and no signal.
- **`ParsePackage` silently skips files** it cannot read (`continue` on `os.ReadFile` error), so the returned slice may be shorter than the package.
- **`ParsePackage` re-reads from disk** but `parser.ParseDir` is called with `nil` src — fine, but it means `ParsePackage` cannot parse in-memory sources. Use `ParseSource` for that.
- **`newUUID()` at `parser.go:99` is dead code.** An identical unexported `newUUID()` lives in `lst/builder.go:1005`, and that is the one actually used. Changing the one in `parser.go` changes nothing.
- **A shared `token.FileSet` accumulates across calls.** `Parser.fset` is created once in `New()` and reused by every `ParseFile`/`ParseSource`. Offsets stay correct, but a long-lived `Parser` grows unboundedly. Prefer a fresh `New()` per batch.

## Building

`go.pb.go` is gitignored, so a fresh clone will not compile until you generate it. `protoc-gen-go` is a separate install from `protoc`:

```bash
go install google.golang.org/protobuf/cmd/protoc-gen-go@v1.33.0   # matches the pinned protobuf version
cd parser && protoc --go_out=. --go_opt=paths=source_relative proto/go.proto
go build ./... && go test ./...
```

A stale `go.pb.go` produces confusing type errors in `lst/` and `printer/` that look like source bugs — regenerate before investigating. Neither Gradle nor Maven compiles this module, so **run the Go toolchain yourself before committing**; a break here is otherwise invisible.

## Test Coverage

`parser_test.go` covers: empty file, functions (with/without params), imports (incl. aliased), var/type decls, method calls, return, binary expr, assignment, if, for, range, syntax error, multiple functions.

`printer/roundtrip_test.go` is the only place printed whitespace is asserted. The full round trip is still skipped — see that file and `printer/AGENTS.md`.

## Related

- `proto/` — the schema everything here produces
- `lst/` — where `ParseSource` delegates all real work (`lst.NewBuilder(...).BuildFile(file)`)
- `printer/` — the inverse direction, proto → Go source
