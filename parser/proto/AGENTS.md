# AGENTS.md — parser/proto

## Purpose

The protobuf schema for the Go LST. This is the **contract between the two runtimes** — the Go parser writes it, the Java module reads it. It is the single source of truth for the tree shape.

## Key Files

- `go.proto` — 75 messages, proto3. Hand-written; edit this.
- `go.pb.go` — generated Go bindings. **Gitignored and untracked** (`.gitignore`: `parser/proto/*.pb.go`). It exists only after you run `protoc`. Never commit it, never hand-edit it.

## Generation

Both the Go and Java bindings come from this one file, via two different toolchains:

```bash
# Go bindings — must be run before `go build ./...` in a fresh clone.
# protoc-gen-go is a separate install from protoc:
go install google.golang.org/protobuf/cmd/protoc-gen-go@v1.33.0
cd parser && protoc --go_out=. --go_opt=paths=source_relative proto/go.proto
```

**Regenerate after every `go.proto` edit.** Because `go.pb.go` is untracked, it goes stale silently: a schema change that is committed leaves everyone else's generated file behind, and the resulting errors in `../lst/` and `../printer/` read like source bugs rather than a stale artifact.

```
# Java bindings — generated automatically by the JVM build:
#   build.gradle.kts : sourceSets.main.proto.srcDir("parser/proto")   + com.google.protobuf plugin 0.9.4
#   pom.xml          : protobuf-maven-plugin, protoSourceRoot = ${project.basedir}/parser/proto
```

Both pin protoc/protobuf **3.25.3**. Keep the Gradle and Maven versions in step.

## Options That Callers Depend On

```proto
option java_package     = "org.openrewrite.go.proto";
option java_outer_classname = "GoProto";        // Java sees GoProto.GoFile, GoProto.Ident, ...
option go_package       = "github.com/openrewrite/rewrite-go/parser/proto";
```

Changing `java_outer_classname` breaks every import in `GoDeserializer.java`.

## Conventions

- Every node message carries `UUID id`, `Space prefix`, `Markers markers` as its first fields, mirroring `org.openrewrite.Tree`.
- `UUID` is split into `most_sig_bits` / `least_sig_bits` int64s to map onto `java.util.UUID`.
- `Space` carries `whitespace` **and** `repeated Comment comments` — the comments field is currently written by nobody and read by nobody.
- Polymorphism uses `oneof` wrapper messages (`Decl`, `Stmt`, `Expr`, `Spec`), so adding a node kind means adding both the message *and* a `oneof` arm.

## Pitfalls

- **This schema is far ahead of both implementations.** 75 messages here; ~39 Java classes; a narrower `GoDeserializer` still. Messages with no consumer on either side include `SwitchStmt`, `TypeSwitchStmt`, `SelectStmt`, `CommClause`, `CaseClause`, `DeferStmt`, `GoStmt`, `BranchStmt`, `LabeledStmt`, `IncDecStmt`, `SendStmt`, `EmptyStmt`, `DeclStmt`, `FuncLit`, `IndexExpr`, `SliceExpr`, `KeyValueExpr`, `ParenExpr`, `Ellipsis`, `Comment`. **A message existing here is not evidence the feature works.**
- Adding a field is cheap; adding a *node kind* obligates work in four other places — `../lst/builder.go`, `../printer/printer.go`, `src/main/java/.../tree/`, `src/main/java/.../internal/GoDeserializer.java` — plus `GoVisitor` and `GoPrinter`.
- Field numbers are the wire contract. Renumbering or reusing a number silently corrupts trees. Append new fields; never renumber.
- After editing, regenerate **both** sides. Forgetting the Go side leaves a stale `go.pb.go` that still compiles.

## Related

- `../lst/` — the writer
- `../printer/` — the Go-side reader
- `../../src/main/java/org/openrewrite/go/internal/` — the Java-side reader
