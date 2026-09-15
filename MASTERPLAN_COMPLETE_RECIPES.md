# MASTERPLAN_COMPLETE_RECIPES

## Objective
Implement, test, and document all remaining OpenRewrite Go recipes across all functional categories to reach 50+ production-grade recipes with 100% test coverage and full Gradle/Maven verification.

## Version
v1.0 — created 2026-09-14

## Progress Dashboard

**Active plan version:** v1.0  
**Last updated:** 2026-09-14  
**Active phase:** Phase 2 (Testing Improvements Suite)  

| Phase | Name | Status | Notes |
|-------|------|--------|-------|
| 0 | Context Propagation Suite | ✅ Done | ContextCancellation and ContextWithValue complete |
| 1 | Security Recipes Suite | ✅ Done | ParameterizedQueries, InputValidation, SecureRandom, TLSConfig, PathTraversal, SQLInjection complete |
| 2 | Testing Improvements Suite | ✅ Done | Testing recipes complete |
| 3 | Performance Optimizations Suite | 🔄 Active | Starting PreallocateSlices |
| 4 | Code Quality & Idiomatic Go Suite | ⏳ Pending | Blocked on Phase 3 |
| 5 | Advanced Features & Framework Migrations | ⏳ Pending | Blocked on Phase 4 |

**Current blockers:** None  
**Next agent action:** Transition to Phase 2 (TableDrivenTests)

---

## Phase 0: Context Propagation Suite

**Entry criteria:** OpenRewrite Go build is operational; in-progress `DeferStmt` & `ContextCancellation` draft exists.  
**Exit criteria:** `ContextCancellation` and `ContextWithValue` pass all tests, documented in `STATUS.md` and `CAPABILITIES.md`.

### Tasks

| # | Task | Owner | Status | Depends On | Acceptance Criteria |
|---|------|-------|--------|------------|---------------------|
| 0.1 | Complete & Verify `ContextCancellation` | agent | done | — | `ContextCancellationTest` passes all test cases (8+ cases), DeferStmt properly integrated into visitor and printer |
| 0.2 | Implement `ContextWithValue` Recipe & Tests | agent | done | 0.1 | `ContextWithValue` recipe implemented with full test suite passing, docs updated |

---

## Phase 1: Security Recipes Suite

**Entry criteria:** Phase 0 complete.  
**Exit criteria:** All 6 security recipes implemented with comprehensive tests passing.

### Tasks

| # | Task | Owner | Status | Depends On | Acceptance Criteria |
|---|------|-------|--------|------------|---------------------|
| 1.1 | `ParameterizedQueries` | agent | done | Phase 0 | Detect & convert string concatenation / `fmt.Sprintf` in SQL queries to parameterized queries |
| 1.2 | `InputValidation` | agent | done | 1.1 | Add standard boundary/nil checks on exported entry points |
| 1.3 | `SecureRandom` | agent | done | 1.2 | Migrate `math/rand` → `crypto/rand` for security contexts |
| 1.4 | `TLSConfig` | agent | done | 1.3 | Enforce modern TLS minimum version and cipher suites |
| 1.5 | `PathTraversal` | agent | done | 1.4 | Detect & sanitize unvalidated path traversal constructs |
| 1.6 | `SQLInjection` | agent | done | 1.5 | Deep detection and remediation for raw SQL string formats |

---

## Phase 2: Testing Improvements Suite

**Entry criteria:** Phase 1 complete.  
**Exit criteria:** All testing recipes implemented with tests passing.

### Tasks

| # | Task | Owner | Status | Depends On | Acceptance Criteria |
|---|------|-------|--------|------------|---------------------|
| 2.1 | `TableDrivenTests` | agent | done | Phase 1 | Convert sequential test calls to table-driven tests |
| 2.2 | `AddTestHelper` | agent | done | 2.1 | Add `t.Helper()` to test helper functions |
| 2.3 | `UseTSetenv` | agent | done | 2.2 | Migrate `os.Setenv` to `t.Setenv` (Go 1.17+) |
| 2.4 | `TestSubtests` | agent | done | 2.3 | Convert test loops to `t.Run` |
| 2.5 | `BenchmarkConversion` & `ExampleTestGeneration` | agent | done | 2.4 | Generate benchmarks and example tests |

---

## Phase 3: Performance Optimizations Suite

**Entry criteria:** Phase 2 complete.  
**Exit criteria:** All performance recipes implemented and tested.

### Tasks

| # | Task | Owner | Status | Depends On | Acceptance Criteria |
|---|------|-------|--------|------------|---------------------|
| 3.1 | `PreallocateSlices` & `AvoidSliceAppend` | agent | pending | Phase 2 | Convert dynamic append loops to preallocated slices |
| 3.2 | `UseStringBuilder` | agent | pending | 3.1 | Convert string concatenation chains to `strings.Builder` |
| 3.3 | `PreallocateMaps` | agent | pending | 3.2 | Convert `make(map[K]V)` to `make(map[K]V, hint)` |
| 3.4 | `UseSyncPool` | agent | pending | 3.3 | Introduce `sync.Pool` for frequent allocations |
| 3.5 | `OptimizeStringConversion` & `UseBytesBuffer` | agent | pending | 3.4 | Optimize byte-string conversion and buffer reuse |

---

## Phase 4: Code Quality & Idiomatic Go Suite

**Entry criteria:** Phase 3 complete.  
**Exit criteria:** All code quality recipes implemented and tested.

### Tasks

| # | Task | Owner | Status | Depends On | Acceptance Criteria |
|---|------|-------|--------|------------|---------------------|
| 4.1 | `ReceiverNaming` | agent | pending | Phase 3 | Enforce consistent receiver naming |
| 4.2 | `ErrorVariableNaming` | agent | pending | 4.1 | Enforce `ErrXxx` convention for exported errors |
| 4.3 | `PackageComment` & `ExportedComment` | agent | pending | 4.2 | Add proper doc comments to package and exported items |
| 4.4 | `RemoveUnusedImports` & `SimplifyReturn` | agent | pending | 4.3 | Clean unused imports and streamline returns |
| 4.5 | `ConstantNaming` & `UseNamedReturns` | agent | pending | 4.4 | Standardize constants and return naming |

---

## Phase 5: Advanced Features & Framework Migrations

**Entry criteria:** Phase 4 complete.  
**Exit criteria:** Generics and framework migration support delivered.

### Tasks

| # | Task | Owner | Status | Depends On | Acceptance Criteria |
|---|------|-------|--------|------------|---------------------|
| 5.1 | `GenericsRefactor` & `FuzzTestConversion` | agent | pending | Phase 4 | Type parameter refactoring and fuzz testing |
| 5.2 | Framework Modules (Gin, Echo, stdlib HTTP) | agent | pending | 5.1 | Framework migration recipes delivered |
