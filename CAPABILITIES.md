# rewrite-go Capabilities

This document outlines the current capabilities of rewrite-go and compares them to rewrite-java's migration features.

## Overview

Just as rewrite-java enables migrations from Java 17 → 25, rewrite-go enables migrations across Go versions and frameworks. Our goal is to provide the same level of automated refactoring support for the Go ecosystem.

## Current Capabilities

### ✅ Implemented Features

#### 1. Go Language Support
- **Parser**: Full Go 1.21+ parsing with type information
- **LST (Lossless Semantic Tree)**: Complete Go AST representation
- **Printer**: Format-preserving code generation
- **Visitor Pattern**: Tree traversal for transformations

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
  - `ioutil.ReadFile` → `io.ReadFile`
  - `ioutil.WriteFile` → `os.WriteFile`
  - `ioutil.ReadDir` → `os.ReadDir`
  - `ioutil.NopCloser` → `io.NopCloser`
  - `ioutil.Discard` → `io.Discard`
- **Example**:
  ```go
  // Before
  import "io/ioutil"
  
  data, err := ioutil.ReadFile("file.txt")
  
  // After
  import "io"
  
  data, err := io.ReadFile("file.txt")
  ```

### 🚧 Planned Features

#### Go Version Migrations

##### Go 1.16 → 1.17+
- [ ] `interface{}` → `any` (Go 1.18+)
- [ ] Embed package usage
- [ ] `go:embed` directive support

##### Go 1.17 → 1.18+
- [ ] Generics migration (add type parameters)
- [ ] Fuzzing test conversion
- [ ] `any` type alias usage

##### Go 1.18 → 1.21+
- [ ] `slices` package migration (from `golang.org/x/exp/slices`)
- [ ] `maps` package migration (from `golang.org/x/exp/maps`)
- [ ] `log/slog` migration (from third-party structured logging)
- [ ] `errors.Join` usage patterns

##### Go 1.21 → 1.22+
- [ ] Range over integers (`for i := range 10`)
- [ ] `math/rand/v2` migration
- [ ] `net/http` routing enhancements

#### Error Handling Patterns
- [ ] Use `errors.Is` instead of `==` comparison
- [ ] Use `errors.As` instead of type assertions
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
| **Type System** | ✅ Full type attribution | ✅ Full type attribution | Complete |
| **Format Preservation** | ✅ Lossless | ✅ Lossless | Complete |
| **Build Tools** | ✅ Maven, Gradle | ✅ Go modules | Complete |

## What We Can Do Today

### ✅ Supported Migrations

1. **Go 1.15 → 1.16+**: `io/ioutil` deprecation
2. **Error Handling**: Basic error wrapping with context
3. **Code Organization**: Import sorting and grouping
4. **Custom Recipes**: Build your own Go transformations

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

### Phase 1: Foundation (Complete ✅)
- [x] Go parser with type information
- [x] LST representation
- [x] Basic recipes (imports, errors, ioutil)
- [x] Test infrastructure
- [x] Maven and Gradle support

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
