# AGENTS.md — tree

## Purpose

The Java LST model for Go: 39 immutable node classes, the visitor that walks them, and the printer that renders them. Everything a recipe touches lives here.

## Key Files

- `Go.java` — root marker interface, extends `org.openrewrite.Tree`. Declares `getPrefix()` / `withPrefix()`.
- `GoFile.java` — the `SourceFile`. Entry point of every tree; returns `new GoPrinter<>()` from `printer(Cursor)`.
- `GoVisitor.java` — the traversal base class. **Read the pitfalls below before subclassing.**
- `GoPrinter.java` — LST → Go source (499 lines). Note it wraps an *anonymous* `GoVisitor` inside `visit(Tree, PrintOutputCapture)` rather than extending `GoVisitor` directly.
- `GoType.java` — type attribution. A `GoType` node holds a nested `Type` implementation: `Basic`, `Named`, `Pointer`, `Array`, `Slice`, `Map`, `Chan`, `Func`, `Interface`, `Struct`, `TypeParameter`.
- `Space.java` — whitespace only (a `String`). Interned `Space.EMPTY`; build via `Space.build(s)`.
- `Expr` / `Stmt` / `Decl` / `Spec` — empty marker interfaces used for list typing.

Everything else is a leaf node class following the identical shape described below.

## The Node Class Shape

Every node is written by hand to exactly this template. Copy an existing one (`Ident.java` is the smallest, `CallExpr.java` the most representative):

```java
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Data
public final class CallExpr implements Go, Expr {
    @With @EqualsAndHashCode.Include @Getter UUID id;
    @With @Getter Space prefix;
    @With @Getter Markers markers;
    // ... node-specific fields, each @With @Getter
    @With @Getter GoType type;          // expressions carry this; statements do not

    @Override
    public <R extends Tree, P> R accept(TreeVisitor<R, P> v, P p) {
        return (R) v.adapt(GoVisitor.class).visitCallExpr(this, p);
    }
}
```

Rules that are load-bearing:

- **`final class`, all fields final** (via `@FieldDefaults`). Never add a setter; use the Lombok `with*` methods.
- **Identity is `id` alone** — `onlyExplicitlyIncluded = true` plus `@EqualsAndHashCode.Include` on `id` only. Two structurally identical nodes with different ids are unequal, and that is intentional: OpenRewrite uses id equality to detect "did this recipe change anything."
- **Field order is the constructor order.** There is no builder — `@Data` on a class with all-final fields gives you one positional constructor. `GoDeserializer` and every test construct nodes positionally, so **inserting a field in the middle silently breaks every call site that still compiles**. Append new fields at the end.
- **The first three fields are always `id`, `prefix`, `markers`,** matching the proto message layout.
- New nodes get a matching `visitXxx` in `GoVisitor` and a `visitXxx` in `GoPrinter`, or they will not traverse and will not print.

## Pitfalls

**1. `GoVisitor` recurses into every syntactic child** as of the traversal fix — including `AssignStmt`, `ReturnStmt`, `BinaryExpr`, `UnaryExpr`, `GenDecl`, `PackageClause`, `Method`, and all the composite type expressions. `ValueSpec.names`, `TypeSpec.name`/`typeParams`, and `FuncDecl.typeParams` are visited too. If you add a node class, add its children to `GoVisitor` in the same change, or a recipe targeting them will silently never fire. `GoVisitorTest` covers this; extend it when you add a node.

**2. Type attribution is deliberately not traversed.** `visitIdent`, `visitBasicLit`, `visitGoType`, and `visitTypeParamDecl` do not descend into their `GoType` fields — matching how `rewrite-java` treats `JavaType`. `GoType` is attribution, not a syntactic child. Do not "fix" this.

**3. `GoType` from the wire is partly null.** `GoDeserializer.deserializeGoType` handles only `Basic`, `Named`, `Pointer`, `Slice`, `Array`, `Map`. For `Chan`, `Func`, `Interface`, `Struct`, and `TypeParameter` it leaves `GoType.type` **null** even though the classes exist here. Null-check `getType().getType()` before using it.

**4. `Space` cannot hold comments.** The proto `Space` has a `repeated Comment` field; this Java `Space` has only `whitespace`. Comments do not survive a round trip.

**5. Some classes redundantly declare `implements Go, Expr`** (e.g. `Ident`, `CallExpr`) though `Expr extends Go`. Harmless; match the surrounding style rather than "fixing" it in isolation.

**6. `GoPrinter` emits keyword spacing itself** — `"package "`, `"import "`, `"func "` are written with a trailing space, and the following node's prefix usually carries that space too. This is why the Go-side round trip still double-spaces; see `parser/printer/roundtrip_test.go`. Be careful adding new keyword output.

## Test Coverage

- `../../../../../../test/java/org/openrewrite/go/tree/GoVisitorTest.java` — traversal coverage: descent into assignments, returns, binary/unary operands, composite type expressions, `GenDecl` specs, and the package clause, plus `isAcceptable`. **Add a case here whenever you add a node class.**
- `../../../../../../test/java/org/openrewrite/go/tree/GoPrinterTest.java` — printer output for hand-built trees, including `SliceTypeExpr`, `PointerTypeExpr`, `TypeParamDecl`, and grouped imports.

Still uncovered: the node classes themselves, and the `GoType` null paths from pitfall 3.

## Related

- `../internal/` — `GoDeserializer` builds these classes from proto; a constructor-signature change breaks it
- `../` — the recipes that consume this model
- `../../../../../../../parser/proto/go.proto` — the schema these classes mirror
