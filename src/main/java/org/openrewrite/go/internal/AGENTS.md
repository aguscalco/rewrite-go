# AGENTS.md — internal

## Purpose

Bridges the protobuf wire format to the Java LST. One class, one direction: `GoProto.GoFile` → `org.openrewrite.go.tree.GoFile`.

## Key Files

- `GoDeserializer.java` — `public GoFile deserialize(GoProto.GoFile)` plus ~45 private `deserializeXxx` helpers.
- `GoImports.java` — add/remove imports on a `GoFile`. Recipes that synthesize a qualified call (`errors.Is`, `slices.Sort`) **must** use this or they emit non-compiling Go. Call it from an overridden `visitGoFile` after `super.visitGoFile(...)` has walked the tree. It also keeps `ImportDecl.grouped` consistent with the spec count, which matters because `GoPrinter` prints only the first spec of an ungrouped declaration.

## How It Works

Recursive descent over the proto tree. Polymorphic nodes are resolved by `oneof` presence checks:

```java
private Stmt deserializeStmt(GoProto.Stmt proto) {
    if (proto.hasBlockStmt())      { ... }
    else if (proto.hasExprStmt())  { ... }
    // ...
}
```

Three shared converters at the bottom of the file: `toUUID`, `toSpace`, `toMarkers`.

Nodes are built with **positional constructors** — `new GoFile(id, prefix, markers, sourcePath, charset, bom, pkg, imports, decls, eof, null, null)`. This file is therefore tightly coupled to field *order* in `../tree/`. Adding a field anywhere but the end of a tree class breaks this file, sometimes without a compile error if types happen to line up.

## Conventions

- One `deserializeXxx` per node kind, plus a plural `deserializeXxxs(List<...>)` wrapper wherever a repeated field exists. Follow the pair convention.
- `hasX()` guards for every optional proto field; pass `null` when absent (`proto.hasAlias() ? deserializeIdent(...) : null`).
- Package is `internal` for a reason — nothing outside the module should import it.

## Pitfalls

- **There is no serializer and no parser.** Nothing in the Java module produces proto bytes, and nothing reads them off a stream or a subprocess. `deserialize` takes an already-constructed `GoProto.GoFile` object, which only exists if a caller built one by hand. This class currently has no production caller — the Java↔Go bridge is unbuilt on both ends (see the root `AGENTS.md`).
- **Unsupported node kinds now throw.** The `hasX()` chains used to fall through to `return null`, and the list helpers dropped the null — so a proto `ForStmt` (which `parser/lst/builder.go` actively builds) vanished from the tree without a warning. They now throw `IllegalArgumentException` naming the proto case. If you hit one, the fix is to add the node class in `../tree/` and wire it through, never to restore the silent drop.
- **`toMarkers` discards every marker:** `new Markers(toUUID(proto.getId()), Collections.emptyList())`. Marker payloads on the wire are dropped unconditionally. Any recipe relying on markers surviving a round trip will not work.
- **`deserializeGoType` covers 6 of 11 type kinds.** It handles `Basic`, `Named`, `Pointer`, `Slice`, `Array`, `Map`. For `Chan`, `Func`, `Interface`, `Struct`, `TypeParameter` no branch exists, so `type` stays **null** and the returned `GoType` is a shell with an id and a prefix. Not an error, not logged — just null. This one still fails silently.
- **`Checksum` and `FileAttributes` are hardcoded to `null`** in `deserialize` (the last two constructor args).
- **The `if (x != null)` guards in the list helpers are now unreachable** — the dispatchers either return a node or throw. Harmless, but do not read them as evidence that null is a supported outcome.

## Test Coverage

`../../../../../../test/java/org/openrewrite/go/internal/GoDeserializerTest.java` — builds proto objects in code and asserts on the resulting Java LST. Plain JUnit 5, no `RewriteTest`. Covers `SliceTypeExpr` / `PointerTypeExpr` and the unsupported-kind exception.

`../../../../../../test/java/org/openrewrite/go/internal/GoImportsTest.java` covers `GoImports` (see below).

Gaps: nothing covers the null-`GoType` paths or marker loss.

## Related

- `../tree/` — the target model; constructor order here must match it exactly
- `../` — recipes consume `GoImports` from this package
- `../../../../../../../parser/proto/go.proto` — the source schema
- `../../../../../../../parser/lst/builder.go` — the Go-side writer this class mirrors
