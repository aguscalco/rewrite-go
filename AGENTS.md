# AGENTS.md — rewrite-go

## Purpose

OpenRewrite language module for Go: parse Go source into a Lossless Semantic Tree (LST), transform it with recipes, print it back. Modeled on `rewrite-java`, but the parser is Go-native rather than JVM-hosted.

## The One Thing to Understand First

This is a **two-runtime project split across a protobuf schema**, and the two halves are **not yet connected**:

```
Go source ──> parser/ (Go)  ──> proto bytes ──X──> src/main/java (Java) ──> recipes ──> GoPrinter
             go/parser+go/types     go.proto        GoDeserializer
```

- `parser/` is a Go **library** (`package parser`) — there is no `main`, no CLI, nothing that writes protobuf bytes to a stream.
- The Java side has **no `Parser` implementation** — nothing in `src/main/java` invokes the Go binary or reads proto bytes off a wire. `GoDeserializer` takes an already-built `GoProto.GoFile` object.

**Consequence:** recipes cannot be run against real `.go` files today. `rewriteRun` / `RewriteTest` does not work here, and no test uses it. Every Java test hand-constructs an LST in code. Do not write a test that assumes source-string-in / source-string-out until that bridge exists.

## Layer Widths (they are not equal — this is the recurring bug source)

Each layer covers a **narrower** set of Go constructs than the one before it:

| Layer | Coverage |
|---|---|
| `parser/proto/go.proto` | 75 messages — the widest; models comments, switch, defer, go, select, slices, generics |
| `parser/lst/builder.go` | builds a subset — incl. `ForStmt`, `RangeStmt`, `CompositeLit` |
| `src/main/java/.../tree/` | 39 node classes — **no** `ForStmt`, `RangeStmt`, `SwitchStmt`, `CompositeLit`, `FuncLit`, `Comment` |
| `GoDeserializer` | matches `tree/`; **throws** on a proto node it has no class for |
| `GoPrinter` | matches `tree/` |

Adding a Go construct end-to-end means touching **all five**, in that order — proto, Go builder, Java node class, `GoVisitor` traversal, `GoDeserializer`, `GoPrinter`. Skipping the Java side no longer fails silently: `GoDeserializer` throws an `IllegalArgumentException` naming the proto case, because dropping a node (a whole `for` loop, say) would corrupt the file.

## Layout

| Path | What lives there |
|---|---|
| `parser/` | Go-native parser library (own `go.mod`) |
| `parser/proto/go.proto` | The contract between the two runtimes — source of truth for both sides |
| `parser/lst/` | Go AST → proto LST |
| `parser/printer/` | proto LST → Go source (Go-side printer) |
| `src/main/java/org/openrewrite/go/` | Recipe implementations |
| `src/main/java/org/openrewrite/go/tree/` | Java LST model, visitor, printer |
| `src/main/java/org/openrewrite/go/internal/` | proto → Java LST deserialization |
| `src/main/resources/META-INF/rewrite/go.yml` | Declarative composite recipes |
| `src/test/java/org/openrewrite/go/` | All Java tests |
| `src/main/java/org/openrewrite/go/search/` | **Empty.** README describes search recipes here; none exist |

Each of these directories has its own `AGENTS.md`. Read the local one before working there.

## Build

Two build systems are maintained in parallel and must stay in sync: `build.gradle.kts` and `pom.xml`. Both generate the Java proto classes from `parser/proto/go.proto` (`protoSourceRoot` / `sourceSets.main.proto`). A change to `go.proto` affects both.

```bash
# Go side — go.pb.go is gitignored, so generate it first. protoc-gen-go is a separate install:
go install google.golang.org/protobuf/cmd/protoc-gen-go@v1.33.0
cd parser && protoc --go_out=. --go_opt=paths=source_relative proto/go.proto
cd parser && go build ./... && go test ./...

# JVM side (does not compile or test the Go module)
./gradlew build      # or: mvn clean test
```

**Run both.** Neither build touches the other's code, so a break on the Go side stays invisible to `./gradlew build` — which is how `parser/printer` stayed uncompilable for several commits.

- Java toolchain 21, but `options.release = 17` — **write Java 17-compatible source**, not 21.
- Lombok is `compileOnly` + `annotationProcessor`. IDEs without the Lombok plugin will show phantom errors on every `tree/` class.
- `parser/proto/*.pb.go` is gitignored. Never commit it.

## Conventions

- Java package root is `org.openrewrite.go`; generated proto lands in `org.openrewrite.go.proto` as `GoProto` (outer class).
- Go module path is `github.com/openrewrite/rewrite-go/parser` — note it does **not** match the GitHub remote (`aguscalco/rewrite-go`). Import paths use the module path.
- Apache 2.0. Source files currently carry no license header; don't add one to a single file in isolation.

## Doc Accuracy Warning

`README.md`, `PROJECT_SUMMARY.md`, and `CAPABILITIES.md` describe intent and roadmap, and have drifted from the code. Verified discrepancies:

- `CAPABILITIES.md` marks parser/LST/printer "Complete ✅" and "Format Preservation: Lossless". Neither holds — the Go round trip is still broken (`parser/printer/roundtrip_test.go` is skipped) and comments are dropped entirely.
- `README.md` documents `search/` as holding search recipes. It is empty.
- `README.md` and `CONTRIBUTING.md` show `git clone` from `openrewrite/rewrite-go`; the actual remote is `aguscalco/rewrite-go`.

**Treat the code as the source of truth.** If you change behavior, update `CAPABILITIES.md` in the same change.

## Related

- Upstream: https://github.com/openrewrite/rewrite
- `CONTRIBUTING.md` — recipe-authoring walkthrough (same drift caveat applies)
