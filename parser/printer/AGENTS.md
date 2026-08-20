# AGENTS.md — parser/printer

## Purpose

Renders a `*proto.GoFile` back to Go source text. The Go-side inverse of `../lst/builder.go`.

## Key Files

- `printer.go` — the whole package. `Printer` wraps a `strings.Builder`; `Print(file)` resets and walks the tree via ~36 unexported `print*` methods.

## Conventions

- Every `print*` method emits `p.printSpace(node.Prefix)` **first**, then the node's own text. All formatting lives in the prefix — never hardcode a leading space or newline in a node's body.
- Fixed syntax is written with literal `p.write("...")` (e.g. `"package "`, `"import "`, `"func "`). Keep the trailing space convention consistent with the surrounding methods.
- One `print<NodeKind>` per proto message, dispatched by the `Decl`/`Stmt`/`Expr` oneof.

## This Printer Is Not the One Recipes Use

Two independent printers exist in this repo and they can drift:

| | Input | Used by |
|---|---|---|
| `parser/printer/printer.go` (this one) | `proto.GoFile` | Go-side tooling only |
| `src/main/java/.../tree/GoPrinter.java` | Java LST | `GoFile.printer()` — what OpenRewrite actually calls |

A formatting fix here does **not** fix recipe output. Changes to node rendering generally need to be made in both.

## Pitfalls

- **Prefixes own all spacing — the printer must not add its own.** Keywords are written bare (`"package"`, `"func"`, `"return"`), and the following node's prefix supplies the space. Writing `"package "` double-spaces the output. The two exceptions are spots where the AST records no position and gofmt's spacing is invariant: `" ("` after `import`/`var`, and the single space before an assignment or binary operator.
- **Every literal the printer writes, the builder must consume.** The two files are one contract; see `../lst/AGENTS.md`. `TestRoundTrip` covers 15 constructs byte-exactly and will catch a violation immediately.
- **Struct and interface bodies do not round trip** — the proto has no Space fields for their braces. `TestRoundTripKnownGaps` records this, skipped.
- **`printGoType` is for type attribution only.** Fields typed `Expr` in the proto (`ValueSpec.type`, `TypeSpec.type`, `Field.type`) must go through `printExpr`. Passing them to `printGoType` was a build break for several commits after `Field.type` changed from `GoType` to `Expr` — nothing caught it because this package had no tests.
- **`Printer` is stateful and not safe for concurrent use.** `Print` calls `p.output.Reset()`, so a single `Printer` cannot serve two goroutines. Call `New()` per invocation.
- Grouped imports print as `import (...)` only when `ImportDecl.Grouped` is set — and `../lst/builder.go` never sets it. Correct-looking code here still produces one `import` line per spec.
- **`go build ./...` is not run by the JVM build.** Nothing in Gradle or Maven compiles this package, so a break here is invisible until someone runs the Go toolchain. Run `cd parser && go build ./... && go test ./...` before committing.

## Related

- `../proto/` — the input schema
- `../lst/` — produces what this consumes; the two must agree on which fields carry formatting
- `../../src/main/java/org/openrewrite/go/tree/GoPrinter.java` — the parallel Java printer
