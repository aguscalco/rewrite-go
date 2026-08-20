# AGENTS.md — org.openrewrite.go (recipes)

## Purpose

The recipe layer — the public surface of the module. Each class here is one `Recipe` that transforms a Go LST.

## Key Files

| Recipe | Does | Notes |
|---|---|---|
| `OrganizeImports.java` | splits imports into stdlib / third-party, sorts each group | rebuilds the `ImportDecl` list wholesale |
| `MigrateIoutilToIO.java` | `ioutil.*` → `io.*` / `os.*` | covers `ReadAll`/`NopCloser`/`Discard` → `io`, `ReadFile`/`WriteFile`/`ReadDir` → `os`, `TempDir` → `os.MkdirTemp`, `TempFile` → `os.CreateTemp` |
| `InterfaceToAny.java` | empty `interface{}` → `any` | simplest recipe in the repo; good template |
| `WrapErrorWithContext.java` | `return err` → `return fmt.Errorf("<msg>: %w", err)` | the only recipe with an `@Option` — see pitfalls |
| `UseErrorsIs.java` | `err == target` → `errors.Is(err, target)` | |
| `UseErrorsAs.java` | `t, ok := err.(*T)` → `errors.As(err, &t)` | |
| `UseSlicesPackage.java` | `sort.Slice` → `slices.Sort` | drops the `less` func without analyzing it |

Declarative composites live one directory over, in `../../../../resources/META-INF/rewrite/go.yml`. Today that file declares a single recipe, `org.openrewrite.go.BestPractices`, listing only `OrganizeImports` and `MigrateIoutilToIO` — the five recipes added since have **not** been added to it. Update `go.yml` when you add a recipe that belongs in the best-practices set.

## Conventions

Match the existing shape rather than importing habits from other OpenRewrite modules:

```java
public class InterfaceToAny extends Recipe {
    @Override public String getDisplayName() { return "Replace interface{} with any"; }
    @Override public String getDescription() { return "Replace empty interface{} with any type alias (Go 1.18+)."; }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            public Tree visitInterfaceTypeExpr(InterfaceTypeExpr e, ExecutionContext ctx) { ... }
        };
    }
}
```

- Plain `public class ... extends Recipe`. **No `@Value` / `@EqualsAndHashCode(callSuper = false)`** on recipes here, unlike upstream OpenRewrite modules.
- `getDescription()` ends with a period (OpenRewrite validates this).
- The visitor is always an **anonymous `GoVisitor<ExecutionContext>`** returned from `getVisitor()`.
- **Put `@Override` on every `visitXxx` method** in the anonymous visitor. Older recipes were written without it, and a signature typo there compiles into a method that is simply never called. New and edited visitors annotate.
- If your recipe synthesizes a qualified call, override `visitGoFile`, call `super.visitGoFile(...)` first, then adjust imports with `GoImports` based on flags the visitor set during traversal. See `UseErrorsIs` for the pattern.
- Build replacement nodes positionally with `Tree.randomId()`, `Space.EMPTY`, and the source node's `getMarkers()`; carry the original node's `getPrefix()` onto the *outermost* replacement so formatting is preserved.
- `import org.openrewrite.go.tree.*;` — the wildcard is the convention here.

## Pitfalls

- **Matching is name-based and heuristic, not type-based.** `UseErrorsIs` and `UseErrorsAs` trigger on identifiers literally named `err` or `e`, and treat a right-hand identifier as a sentinel only if it starts with `Err` or equals `EOF`. Type attribution exists (`Expr.getType()`) but is unused. False positives and false negatives are expected; do not assume a recipe is semantically safe.
- **Negation is faked.** `UseErrorsIs` models `!errors.Is(...)` as a `CallExpr` whose `fun` is an `Ident` named `"!"`, because there is no unary-expression construction path. Prefer `UnaryExpr` if you extend this.
- **`UseSlicesPackage` drops the comparator.** `sort.Slice(x, less)` becomes `slices.Sort(x)` without analyzing `less`, so a non-natural ordering is silently changed. It is only correct for comparators that sort ascending by natural order.
- **Recipes cannot be run against real `.go` files.** No `Parser` implementation exists. See the root `AGENTS.md`.
- **`GoPrinter` prints only the first spec of an ungrouped `ImportDecl`.** If you build import declarations by hand rather than through `GoImports`, set `grouped` whenever there is more than one spec or the rest are dropped on print.

## Adding a Recipe

1. New class here, following the shape above.
2. Check whether `GoVisitor` recurses to your node — if not, fix `GoVisitor` first and add a case to `GoVisitorTest`.
3. Confirm `GoPrinter` can print every node kind you construct.
4. If you synthesize a qualified call, add the import with `GoImports` and remove any import your rewrite orphaned.
5. Test in `../../../../../test/java/org/openrewrite/go/<Name>Test.java` — hand-built LST, plain JUnit. See the test `AGENTS.md`.
6. Add to `go.yml` if it belongs to `BestPractices`.
7. Update `CAPABILITIES.md` at the repo root in the same change.

## Related

- `tree/` — the LST model and visitor you extend
- `internal/` — deserialization; not used by recipes directly
- `search/` — **empty**; `README.md` claims search recipes live there
