# AGENTS.md — src/test/java/org/openrewrite/go

## Purpose

All Java tests for the module. 65 tests across 11 files, mirroring the main source layout: recipe tests here, `tree/GoVisitorTest` and `tree/GoPrinterTest`, `internal/GoDeserializerTest` and `internal/GoImportsTest`.

## The Convention That Will Surprise You

**No test uses `RewriteTest` or `rewriteRun`.** There is no `implements RewriteTest`, no `RecipeSpec`, no source-string-in / source-string-out. Do not reach for the OpenRewrite testing DSL here — it will not work, because no `Parser` implementation exists to turn `.go` text into a `GoFile` (see the root `AGENTS.md`).

Instead, every test hand-constructs an LST with positional constructors and drives the recipe directly:

```java
class InterfaceToAnyTest {
    @Test
    void replaceEmptyInterface() {
        // Create: func process(data interface{}) {}
        InterfaceTypeExpr emptyInterface = new InterfaceTypeExpr(
            UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.emptyList());
        // ... build up Field -> FuncType -> FuncDecl -> GoFile

        InterfaceToAny recipe = new InterfaceToAny();
        ExecutionContext ctx = new InMemoryExecutionContext();
        Tree result = recipe.getVisitor().visit(file, ctx);

        assertNotNull(result);
        assertTrue(result instanceof GoFile);
        // ... cast down through the tree and assert on leaf values
    }
}
```

## Conventions

- Package-private `class XxxTest` — not `public`.
- JUnit 5 + `static org.junit.jupiter.api.Assertions.*`. No AssertJ, no Hamcrest.
- `UUID.randomUUID()` in tests (production code uses `Tree.randomId()`); `Markers.EMPTY`; `Space.build("\n\t")` where formatting is under test.
- Drive the recipe with `recipe.getVisitor().visit(file, ctx)` and an `InMemoryExecutionContext` — **not** `recipe.run(...)`. This bypasses the recipe scheduler, so cycles, `Recipe` validation, and `@Option` binding are not exercised.
- Assert by casting down the tree (`(FuncDecl) resultFile.getDeclarations().get(0)`) and checking leaf values. A leading `// Create: <the Go source this LST represents>` comment above the construction block is the standard — keep it; it is the only readable form of the input.
- One test file per recipe, named `<Recipe>Test.java`, co-located in the same package as the recipe.

## Pitfalls

- **Positional constructors make tests brittle by design.** Adding a field mid-class in `tree/` breaks every construction site here, and where the types happen to line up it compiles and silently tests the wrong thing. Append fields at the end of tree classes.
- **`getVisitor().visit(...)` bypasses the recipe scheduler**, so cycles, `Recipe` validation, and `@Option` binding are not exercised. Options must be passed to the constructor explicitly, as `WrapErrorWithContextTest` does.
- **Build the node where it really occurs.** A recipe test that hand-builds its target directly under a node type that traverses can pass while the recipe fails on realistic input. Wrap the target in an `AssignStmt` or `ReturnStmt` if that is where it appears in real Go.
- **The Go round trip is not covered from here.** Both printers are separate; `tree/GoPrinterTest` covers the Java one only. The Go-side round trip lives in `../../../../../../parser/printer/roundtrip_test.go` and is still skipped.

## Running

```bash
./gradlew test        # or: mvn test
```

JUnit Platform, `useJUnitPlatform()`. The Go tests are separate: `cd parser && go test ./...`.

## Coverage Map

| Area | File |
|---|---|
| `GoVisitor` traversal + `isAcceptable` | `tree/GoVisitorTest.java` |
| `GoPrinter`, incl. slice/pointer/type-param and grouped imports | `tree/GoPrinterTest.java` |
| `GoDeserializer`, incl. unsupported-kind errors | `internal/GoDeserializerTest.java` |
| `GoImports` add/remove/grouping | `internal/GoImportsTest.java` |
| One file per recipe | `<Recipe>Test.java` |

**Still untested:** the `tree/` node classes themselves, `GoType` null paths from partial deserialization, and marker preservation.

## Related

- `../../../../../main/java/org/openrewrite/go/` — the recipes under test
- `../../../../../main/java/org/openrewrite/go/tree/` — the model these tests construct by hand
- `../../../../../../parser/parser_test.go` — the Go-side suite, entirely separate
