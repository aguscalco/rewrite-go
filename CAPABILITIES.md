# rewrite-go Capabilities

This document outlines the current capabilities of rewrite-go and compares them to rewrite-java's migration features.

## Overview

Just as rewrite-java enables migrations from Java 17 → 25, rewrite-go enables migrations across Go versions and frameworks. Our goal is to provide the same level of automated refactoring support for the Go ecosystem.

## Current Capabilities

### ✅ Implemented Features

#### 1. Go Language Support
- **Parser**: Go 1.21+ parsing via `go/parser` and `go/types`
- **LST**: Go AST representation covering declarations, statements, expressions, and type expressions
- **Printer**: Java-side `GoPrinter` and Go-side `printer` package
- **Visitor Pattern**: `GoVisitor` traversal for transformations

**Known gaps.** Type attribution is single-file — the parser runs `go/types` with a nil importer, so imported symbols are not resolved. Comments are not carried through the LST. The Go-side parse→print round trip is not yet byte-exact (see `parser/printer/roundtrip_test.go`). There is no `Parser` implementation on the Java side yet, so recipes cannot be run against `.go` files end to end; they are exercised against hand-built LSTs.

#### 2. Core Recipes

##### OrganizeImports
- **Purpose**: Sort and group Go imports
- **Behavior**: 
  - Separates stdlib from third-party imports
  - Sorts alphabetically within groups
  - Maintains proper formatting
- **Example**:
  ```go
  // Before
  import (
      "github.com/gin-gonic/gin"
      "fmt"
      "os"
  )
  
  // After
  import (
      "fmt"
      "os"
  )
  import (
      "github.com/gin-gonic/gin"
  )
  ```

##### WrapErrorWithContext
- **Purpose**: Wrap errors with context using `fmt.Errorf`
- **Behavior**:
  - Transforms `return err` → `return fmt.Errorf("context: %w", err)`
  - Configurable context message
  - Only transforms single error returns
- **Example**:
  ```go
  // Before
  func doSomething() error {
      err := someOperation()
      if err != nil {
          return err
      }
      return nil
  }
  
  // After
  func doSomething() error {
      err := someOperation()
      if err != nil {
          return fmt.Errorf("failed to do something: %w", err)
      }
      return nil
  }
  ```

##### MigrateIoutilToIO
- **Purpose**: Migrate deprecated `io/ioutil` to `io` and `os` (Go 1.16+)
- **Behavior**:
  - `ioutil.ReadAll` → `io.ReadAll`
  - `ioutil.NopCloser` → `io.NopCloser`
  - `ioutil.Discard` → `io.Discard`
  - `ioutil.ReadFile` → `os.ReadFile`
  - `ioutil.WriteFile` → `os.WriteFile`
  - `ioutil.ReadDir` → `os.ReadDir`
  - `ioutil.TempDir` → `os.MkdirTemp`
  - `ioutil.TempFile` → `os.CreateTemp`
  - The `io/ioutil` import is replaced by whichever of `io` / `os` the rewritten call sites actually use.
- **Example**:
  ```go
  // Before
  import "io/ioutil"
  
  data, err := ioutil.ReadFile("file.txt")
  
  // After
  import "os"
  
  data, err := os.ReadFile("file.txt")
  ```

##### InterfaceToAny
- **Purpose**: Replace the empty `interface{}` with `any` (Go 1.18+)
- **Behavior**: Rewrites `interface{}` with no methods to `any`; interfaces with methods are left alone.

##### UseErrorsIs
- **Purpose**: Replace direct error comparison with `errors.Is`
- **Behavior**: `err == target` → `errors.Is(err, target)`; `err != target` → the negated form. Adds the `errors` import.
- **Limitation**: Matches on identifiers named `err`/`e` compared against a selector or an identifier starting with `Err`/`EOF`. It does not use type attribution, so it both misses and over-matches.

##### UseErrorsAs
- **Purpose**: Replace type assertions on errors with `errors.As`
- **Behavior**: `target, ok := err.(*MyError)` → `errors.As(err, &target)`. Adds the `errors` import.
- **Limitation**: Same name-based matching as `UseErrorsIs`.

##### UseSlicesPackage
- **Purpose**: Replace manual slice operations with the `slices` package (Go 1.21+)
- **Behavior**: `sort.Slice` → `slices.Sort`, `sort.SliceStable` → `slices.SortStable`. Adds `slices` and removes `sort` when no other `sort.*` call remains.
- **Limitation**: The comparator argument is dropped without analysis, so this is only correct for comparators that sort ascending by natural order.

### 🚧 Planned Features

#### Go Version Migrations

##### Go 1.16 → 1.17+
- [x] `interface{}` → `any` (Go 1.18+)
- [ ] Embed package usage
- [ ] `go:embed` directive support

##### Go 1.17 → 1.18+
- [ ] Generics migration (add type parameters)
- [ ] Fuzzing test conversion
- [ ] `any` type alias usage

##### Go 1.18 → 1.21+
- [x] `slices` package migration (`sort.Slice` → `slices.Sort`)
- [ ] `maps` package migration (from `golang.org/x/exp/maps`)
- [ ] `log/slog` migration (from third-party structured logging)
- [ ] `errors.Join` usage patterns

##### Go 1.21 → 1.22+
- [ ] Range over integers (`for i := range 10`)
- [ ] `math/rand/v2` migration
- [ ] `net/http` routing enhancements

#### Error Handling Patterns
- [x] Use `errors.Is` instead of `==` comparison
- [x] Use `errors.As` instead of type assertions
- [ ] Wrap errors with context (multi-level)
- [ ] Remove `errors.Wrap` (pkg/errors) → `fmt.Errorf`

#### Context Propagation
- [ ] Add `context.Context` as first parameter
- [ ] Propagate context through call chains
- [ ] Replace `context.TODO()` with proper context
- [ ] Timeout and cancellation patterns

#### Testing Improvements
- [ ] Table-driven test generation
- [ ] `t.Helper()` usage
- [ ] `t.Setenv` migration (Go 1.17+)
- [ ] Fuzz test scaffolding

#### Performance Optimizations
- [ ] `sync.Pool` usage patterns
- [ ] Preallocate slices with `make`
- [ ] String builder vs concatenation
- [ ] Map preallocation

#### Security Best Practices
- [ ] SQL injection prevention (parameterized queries)
- [ ] Input validation patterns
- [ ] Secure random number generation
- [ ] TLS configuration updates

### 🎯 Framework Migrations

#### Gin Framework (rewrite-gin)
- [ ] Gin v1 → v2 migration
- [ ] Middleware pattern updates
- [ ] Context method changes
- [ ] Routing API updates
- [ ] Binding validation updates

#### Echo Framework (rewrite-echo)
- [ ] Echo v3 → v4 migration
- [ ] Middleware updates
- [ ] Context changes
- [ ] Handler signatures

#### Fiber Framework (rewrite-fiber)
- [ ] Fiber v2 → v3 migration
- [ ] Middleware updates
- [ ] Context changes

#### Standard Library HTTP
- [ ] `net/http` → `net/http` with routing (Go 1.22+)
- [ ] Handler middleware patterns
- [ ] Request context usage

## Comparison with rewrite-java

| Feature | rewrite-java | rewrite-go | Status |
|---------|--------------|------------|--------|
| **Language Version Migrations** | ✅ Java 8→25 | 🚧 Go 1.16→1.22 | Planned |
| **API Migrations** | ✅ Comprehensive | ✅ Basic (ioutil) | Expanding |
| **Framework Migrations** | ✅ Spring, Quarkus, Micronaut | 🚧 Gin, Echo, Fiber | Planned |
| **Testing Migrations** | ✅ JUnit 4→5, TestNG→JUnit | 🚧 Testing patterns | Planned |
| **Security Fixes** | ✅ OWASP, CVE patches | 🚧 Security patterns | Planned |
| **Performance** | ✅ Optimization recipes | 🚧 Performance patterns | Planned |
| **Code Style** | ✅ Checkstyle, Spotless | 🚧 Go fmt, golangci-lint | Planned |
| **Type System** | ✅ Full type attribution | 🚧 Single-file only | Partial |
| **Format Preservation** | ✅ Lossless | 🚧 Round trip incomplete | In progress |
| **Build Tools** | ✅ Maven, Gradle | ✅ Go modules | Complete |

## What We Can Do Today

### ✅ Supported Migrations

1. **Go 1.15 → 1.16+**: `io/ioutil` deprecation
2. **Go 1.18+**: `interface{}` → `any`
3. **Go 1.21+**: `sort.Slice` → `slices.Sort`
4. **Error Handling**: wrapping with context, `errors.Is`, `errors.As`
5. **Code Organization**: Import sorting and grouping
6. **Custom Recipes**: Build your own Go transformations

All of the above operate on an LST you construct or deserialize yourself — see the "Known gaps" note above.

### 🎯 Use Cases

#### Legacy Code Modernization
```bash
# Migrate deprecated APIs
./gradlew rewriteRun -Drewrite.activeRecipe=org.openrewrite.go.MigrateIoutilToIO

# Improve error handling
./gradlew rewriteRun -Drewrite.activeRecipe=org.openrewrite.go.WrapErrorWithContext

# Clean up imports
./gradlew rewriteRun -Drewrite.activeRecipe=org.openrewrite.go.OrganizeImports
```

#### Code Quality Improvement
- Standardize import ordering across teams
- Add error context for better debugging
- Remove deprecated API usage

#### Migration Preparation
- Identify deprecated patterns before Go version upgrades
- Automate repetitive migration tasks
- Ensure consistency across large codebases

## Roadmap

### Phase 1: Foundation (In progress 🚧)
- [x] Go parser with single-file type information
- [x] LST representation
- [x] Basic recipes (imports, errors, ioutil, slices, any)
- [x] Test infrastructure
- [x] Maven and Gradle support
- [ ] Java↔Go bridge (a `Parser` implementation and a parser binary)
- [ ] Byte-exact parse→print round trip
- [ ] Comment preservation

### Phase 2: Go Version Migrations (Next)
- [ ] `interface{}` → `any` (Go 1.18+)
- [ ] Generics support
- [ ] `slices` and `maps` package migrations
- [ ] `log/slog` migration
- [ ] Range over integers (Go 1.22+)

### Phase 3: Error Handling & Context (Q2 2026)
- [ ] `errors.Is` and `errors.As` patterns
- [ ] Context propagation
- [ ] Timeout and cancellation
- [ ] pkg/errors migration

### Phase 4: Framework Support (Q3 2026)
- [ ] rewrite-gin module
- [ ] rewrite-echo module
- [ ] rewrite-fiber module
- [ ] Standard library HTTP routing

### Phase 5: Testing & Quality (Q4 2026)
- [ ] Table-driven test generation
- [ ] Fuzz testing support
- [ ] Performance optimization recipes
- [ ] Security best practices

### Phase 6: Advanced Features (2027)
- [ ] Generics refactoring
- [ ] Interface extraction
- [ ] Dependency injection patterns
- [ ] Microservices patterns

## Contributing

We welcome contributions! Priority areas:

1. **Go version migration recipes** (1.18+ features)
2. **Framework-specific recipes** (Gin, Echo, Fiber)
3. **Error handling patterns**
4. **Testing improvements**
5. **Performance optimizations**

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## Getting Help

- **Documentation**: [README.md](README.md)
- **Examples**: See test files in `src/test/java/org/openrewrite/go/`
- **Issues**: [GitHub Issues](https://github.com/aguscalco/rewrite-go/issues)
- **Discussions**: [GitHub Discussions](https://github.com/aguscalco/rewrite-go/discussions)

## License

Apache License 2.0 - see [LICENSE](LICENSE) for details.
