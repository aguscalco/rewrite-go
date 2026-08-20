# AGENTS.md — parser/lst

## Purpose

Converts a Go `*ast.File` (plus its `*types.Info` and the raw source bytes) into the protobuf LST defined in `../proto/go.proto`. This is where losslessness is either preserved or lost.

## Key Files

- `builder.go` — the whole package. `Builder` struct + ~38 `build*` methods, one per node kind, dispatched by type switch in `buildDecl`, `buildStmt`, `buildExpr`, `buildSpec`.

## How It Works

`NewBuilder(fset, info, src)` → `BuildFile(file)`. Every node gets:

```go
Id:      newUUID(),
Prefix:  b.spaceBefore(node.Pos()),
Markers: &proto.Markers{Id: newUUID()},
```

Formatting is carried entirely in `Prefix` (leading whitespace), recovered from `src` by byte offset. Type attribution comes from `b.info` via `buildGoType` / `convertType`.

## Conventions

- One `build<NodeKind>` method per proto message; name it after the **proto** message, not the Go AST type (`buildFuncTypeExpr` vs `buildFuncType` are different things — the former is the expression form, the latter the signature form).
- Dispatch happens in the four type-switch functions. A new node kind needs a case added there *and* a `build*` method, or it is silently dropped.
- `newUUID()` is package-local (`builder.go:1005`) and packs `uuid.New()` into the proto's two int64 halves. Use it — do not construct `proto.UUID` by hand.

## Pitfalls

These are the known losslessness gaps. They matter because "Lossless" is the L in LST:

- **Comments are dropped entirely.** `parser.ParseComments` is passed upstream, and `proto.Space` has a `repeated Comment comments` field — but `builder.go` contains no reference to `Comment` or `.Doc`. Every doc comment and inline comment is lost.
- **`spaceBefore` captures only the whitespace run immediately before the node** (via `isSpace`). It used to scan back to the start of the line, which swallowed real source tokens into a `Space` — the printer then emitted them a second time, producing output like `package package main`. `parser/printer/roundtrip_test.go` guards against a regression.
- **Nested nodes at the same offset each capture the same whitespace.** A statement, its call expression, its selector, and its identifier all start at the same byte, so each gets the same prefix and the indentation prints once per nesting level. Only the outermost node at an offset should own the prefix; this is unresolved.
- **Import grouping is destroyed.** `BuildFile` iterates the flattened `file.Imports` and wraps **each spec in its own `ImportDecl`**. It never reads `file.Decls` for the `import (...)` block, and never sets `Grouped`. A grouped import block becomes N separate single-import decls, and `printer` will emit N `import` lines.
- **`spaceAfter` scans forward to end of line** — it captures trailing content, not only whitespace, if the node does not end the line. It has the bug `spaceBefore` used to have.
- **Builds nodes the Java side cannot receive.** `buildForStmt`, `buildRangeStmt`, `buildCompositeLit` produce valid proto, but `src/main/java/.../tree/` has no `ForStmt`, `RangeStmt`, or `CompositeLit` class and `GoDeserializer` has no branch for them. Work here is not usable end-to-end until the Java layer catches up.

## Test Coverage

No test file in this package. `lst` is exercised indirectly through `../parser_test.go` (structure only — names, counts, kinds) and through `../printer/roundtrip_test.go`, which does assert on printed whitespace: `TestPrefixesContainOnlyWhitespace` passes, and the full `TestRoundTrip` is skipped with the remaining work listed in its comment.

## Related

- `../proto/` — the target schema; check it before adding a field
- `../printer/` — consumes what this produces; a builder change usually needs a matching printer change
- `../../src/main/java/org/openrewrite/go/internal/` — the Java-side counterpart of this file
