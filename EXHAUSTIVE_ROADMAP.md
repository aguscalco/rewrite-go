# OpenRewrite for Go: Exhaustive Recipe Roadmap

## Vision
To make OpenRewrite the definitive automated refactoring engine for the Go ecosystem, comparable in depth and capability to `rewrite-java`. This roadmap outlines the path from the foundational 50 recipes to a comprehensive suite of 200+ recipes covering modern Go features, advanced concurrency, security, and popular community frameworks.

---

## 1. Modern Go Adoptions (Go 1.20 - 1.23+)
The Go language has evolved rapidly. These recipes ensure codebases automatically adopt the latest built-ins and standard library additions, reducing tech debt and third-party dependencies.

| Recipe Idea | Description | Target Go Version |
|-------------|-------------|-------------------|
| `LoopVarCaptureFix` | Remove redundant `v := v` captures inside `for` loops, which are no longer needed. | Go 1.22+ |
| `UseClearBuiltin` | Convert `for k := range m { delete(m, k) }` and `s = s[:0]` to `clear(m)` / `clear(s)`. | Go 1.21+ |
| `UseMinMaxBuiltins` | Convert `math.Min(a, b)` and manual `if a < b` boilerplate to `min(a, b)` / `max(a, b)`. | Go 1.21+ |
| `ErrorsJoinMigration` | Convert custom multierror implementations (e.g. HashiCorp's `multierror`) to standard `errors.Join`. | Go 1.20+ |
| `UseMapsPackage` | Expand to full migration for `maps.Clone`, `maps.DeleteFunc`, `maps.Equal`. | Go 1.21+ |
| `UseSlicesPackage` | Expand to full migration for `slices.BinarySearch`, `slices.Compact`, `slices.Replace`. | Go 1.21+ |
| `IterSeqMigration` | Convert custom iterator patterns or channel-based generators to standard `iter.Seq`. | Go 1.23+ |
| `HttpRoutingPatterns` | Fully migrate `gorilla/mux` or `chi` routers to standard library `http.ServeMux` using new verb patterns. | Go 1.22+ |

---

## 2. Concurrency & Goroutine Safety
Concurrency bugs are the most difficult to debug in Go. OpenRewrite can proactively enforce safety patterns.

| Recipe Idea | Description | Impact |
|-------------|-------------|--------|
| `DetectContextLeak` | Ensure `context.WithCancel` / `WithTimeout` return a `cancel` function that is explicitly deferred. | High |
| `WaitGroupSafety` | Ensure `wg.Add()` is called *outside* the goroutine, and `wg.Done()` is deferred *inside*. | Critical |
| `MutexByValue` | Detect and rewrite function signatures passing `sync.Mutex` by value (changes to `*sync.Mutex`). | Critical |
| `TimerLeakPrevention` | Convert `time.After` in long-running `select` loops to `time.NewTimer` + `timer.Stop()`. | High |
| `ChannelDirectionality` | Add `<-chan` or `chan<-` to function parameters where the channel is only read/written. | Medium |
| `AtomicPointerMigration` | Migrate `atomic.Value` usage to the type-safe `atomic.Pointer[T]`. | Medium |

---

## 3. Code Quality & Linter Auto-Fixes (`golangci-lint` parity)
Go developers rely heavily on linters. OpenRewrite can take this a step further by *automatically fixing* what linters complain about.

| Recipe Idea | Description | Linter Equivalent |
|-------------|-------------|-------------------|
| `RemoveRedundantType` | Remove redundant type declarations in composite literals (e.g. nested structs/slices). | `simplify` |
| `PreferEmptySliceInit` | Standardize slice initialization: `var x []int` vs `x := []int{}` based on usage. | `nilslice` |
| `NilInterfaceCheck` | Fix dangerous interface nil checks where a typed nil is boxed in an interface. | `nilerr` |
| `DeferInLoop` | Detect `defer` statements inside loops and wrap them in a closure or extract to a function. | `gocritic` |
| `YodaCondition` | Standardize condition order (e.g., `if 42 == x` -> `if x == 42`). | `stylecheck` |
| `IneffAssign` | Remove assignments to variables that are never used (requires basic dataflow analysis). | `ineffassign` |

---

## 4. Testing & CI Mastery
Automated testing is a core pillar of Go culture. We can completely modernize a test suite in one PR.

| Recipe Idea | Description | Category |
|-------------|-------------|----------|
| `TestifyToStdlib` | Migrate `stretchr/testify/assert` to pure standard library checks (or vice-versa, depending on team preference). | Framework |
| `MockgenToUberMock` | Migrate imports and directives from the deprecated `golang/mock` to `go.uber.org/mock`. | Framework |
| `ParallelizeTests` | Automatically inject `t.Parallel()` into safe table-driven test sub-tests. | Performance |
| `TempDirMigration` | Migrate manual `os.MkdirTemp` cleanup to `t.TempDir()`. | Quality |
| `DeepEqualMigration` | Migrate `reflect.DeepEqual` to `cmp.Equal` (from `google/go-cmp`). | Quality |

---

## 5. Security & Cryptography Posture
A comprehensive suite to harden Go applications against OWASP vulnerabilities.

| Recipe Idea | Description | Severity |
|-------------|-------------|----------|
| `InsecureTLSCheck` | Flag and remove `InsecureSkipVerify: true` in production `tls.Config`. | Critical |
| `HardcodedSecretRemoval`| Detect hardcoded tokens/keys in `BasicLit` and extract them to `os.Getenv()`. | Critical |
| `WeakHashMigration` | Migrate `crypto/md5` and `crypto/sha1` usage to `crypto/sha256`. | High |
| `SqlInjectionPrevention`| Expand `ParameterizedQueries` to support `sqlx` and `pgx` specific string building. | Critical |
| `SSRFPrevention` | Enforce validation/sanitization of URLs passed to `http.Get` or `http.Client.Do`. | High |

---

## 6. Ecosystem Frameworks (Deep Support)
Beyond the standard library, enterprise Go apps use a common stack of frameworks.

### Logging
*   **`LogrusToSlog`**: Migrate `sirupsen/logrus` to standard library `log/slog`.
*   **`ZapToSlog`**: Migrate `go.uber.org/zap` to `log/slog` (or vice-versa for high performance needs).

### Web Frameworks & RPC
*   **`GinToStdlib`**: Downgrade/migrate `gin` routes to Go 1.22 `http.ServeMux` for zero-dependency microservices.
*   **`GrpcContextPropagation`**: Ensure `context.Context` is correctly passed to all `grpc` service calls.
*   **`GinBindValidation`**: Migrate `c.Bind()` to `c.ShouldBind()` to prevent automatic 400 responses where custom error handling is desired.

### Database & ORM
*   **`GormToEnt` / `GormToSqlc`**: (Moonshot) Structural migrations between popular database access patterns.
*   **`SqlxNamedQueries`**: Convert positional `?` / `$1` queries to `sqlx` Named Queries using struct tags.

---

## Requirements for Exhaustive Implementation

To implement the advanced recipes above, the `rewrite-go` underlying parser and AST will need the following capabilities:

1. **Global Type Attribution (`GoType`)**: Recipes like `IneffAssign`, `MutexByValue`, or advanced `GenericsRefactor` require the Java Visitor to know the exact semantic type of an `Ident` across package boundaries.
2. **Data-Flow Analysis**: Recipes like `TimerLeakPrevention` or `DetectContextLeak` require knowing if a variable escapes a function or is passed to a specific method.
3. **AST Expansion**: Completing the parser mapping for missing nodes like `SelectStmt`, `TypeAssertExpr`, `ChanType`, `GoStmt`, and `SendStmt`.
