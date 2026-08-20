# AGENTS.md — parser/lst

## Purpose

Converts a Go `*ast.File` (plus its `*types.Info` and the raw source bytes) into the protobuf LST defined in `../proto/go.proto`. This is where losslessness is either preserved or lost.

## Key Files

- `builder.go` — the whole package. `Builder` struct + ~38 `build*` methods, one per node kind, dispatched by type switch in `buildDecl`, `buildStmt`, `buildExpr`, `buildSpec`.

## How It Works

`NewBuilder(fset, info, src)` -> `BuildFile(file)`. The builder keeps a **cursor**: the byte offset just past the last token accounted for. Nodes are built in source order, so everything between the cursor and the next node is exactly that node's prefix.

```go
Id:      newUUID(),
Prefix:  b.prefix(node.Pos()),   // claims src[cursor:pos], advances cursor
Markers: &proto.Markers{Id: newUUID()},
```

Four helpers manage it:

| Helper | Use |
|---|---|
| `prefix(pos)` | claim the gap before a node as its prefix |
| `consume(pos, text)` | advance past a token at a known position |
| `consumeText(text)` | advance past a token whose position the AST does not record |
| `rest()` | claim the tail of the file for `GoFile.Eof` |

**The contract:** anything the printer writes literally — keywords, braces, parens, dots, commas, operators — the builder must consume. Miss one and it leaks into the next node's prefix, and the printer emits it a second time. This is exactly how printing a file used to produce `package package package main`.

Type attribution comes from `b.info` via `buildGoType` / `convertType`.

## Conventions

- One `build<NodeKind>` method per proto message; name it after the **proto** message, not the Go AST type (`buildFuncTypeExpr` vs `buildFuncType` are different things — the former is the expression form, the latter the signature form).
- Dispatch happens in the four type-switch functions. A new node kind needs a case added there *and* a `build*` method, or it is silently dropped.
- **Build children in source order.** A method's receiver precedes its name, so `buildFuncDecl` assigns `Recv` before `Name`. Composite literals evaluate fields top to bottom, so a field whose value calls a `build*` method must appear in source order too — this is why several methods assign after the literal rather than inside it.
- `newUUID()` is package-local and packs `uuid.New()` into the proto's two int64 halves. Use it — do not construct `proto.UUID` by hand.

## Pitfalls

- **Comments are dropped entirely.** `parser.ParseComments` is passed upstream, and `proto.Space` has a `repeated Comment comments` field — but `builder.go` contains no reference to `Comment` or `.Doc`. Every doc comment and inline comment is lost. This is the largest remaining losslessness gap.
- **Struct and interface bodies do not round trip.** The proto models neither the space before the opening brace nor the one before the closing brace, so `struct{}` and `struct {\n}` are indistinguishable once parsed. See `TestRoundTripKnownGaps`.
- **`spaceAfter` is gone.** It scanned forward to end of line and captured trailing content, not only whitespace. Nothing needs it now that closing tokens take a `prefix()` before being consumed.
- **Builds nodes the Java side cannot receive.** `buildForStmt`, `buildRangeStmt`, `buildCompositeLit` produce valid proto, but `src/main/java/.../tree/` has no matching class. `GoDeserializer` now throws rather than dropping them, so work here surfaces as a loud failure on the Java side instead of a silent one.

## Test Coverage

No test file in this package. `lst` is exercised through `../parser_test.go` (structure — names, counts, kinds) and, more importantly for this file, through `../printer/roundtrip_test.go`: `TestRoundTrip` covers 15 constructs byte-exactly, so a missed `consume` shows up immediately as duplicated output. **Add a case there whenever you teach the builder a new construct.**

## Related

- `../proto/` — the target schema; check it before adding a field
- `../printer/` — consumes what this produces; a builder change usually needs a matching printer change
- `../../src/main/java/org/openrewrite/go/internal/` — the Java-side counterpart of this file
