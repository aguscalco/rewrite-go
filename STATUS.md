# OpenRewrite Go Recipes - Status & Plan

**Project:** https://github.com/aguscalco/rewrite-go  
**Last Updated:** 2026-09-13  
**Overall Progress:** 15/50+ recipes (30%)

---

## 📊 Current Status

### ✅ Completed (15 recipes, all tested)

| # | Recipe | Category | Go Version | Commit |
|---|--------|----------|------------|--------|
| 1 | OrganizeImports | Code Quality | - | Pre-existing |
| 2 | WrapErrorWithContext | Error Handling | - | Pre-existing |
| 3 | MigrateIoutilToIO | Version Migration | 1.16+ | Pre-existing |
| 4 | InterfaceToAny | Version Migration | 1.18+ | `e6650db` |
| 5 | UseSlicesPackage | Version Migration | 1.21+ | `c75e070` |
| 6 | UseMapsPackage | Version Migration | 1.21+ | `a8f85ee` |
| 7 | MigrateToSlog | Version Migration | 1.21+ | `d6012b0` |
| 8 | UseErrorsIs | Error Handling | - | `914559a` |
| 9 | UseErrorsAs | Error Handling | - | `d03e234` |
| 10 | RangeOverIntegers | Version Migration | 1.22+ | `c75e070` |
| 11 | UseMathRandV2 | Version Migration | 1.22+ | `e6650db` |
| 12 | AddContextParameter | Context Propagation | - | `9fc3fd7` |
| 13 | PropagateContext | Context Propagation | - | `9fc3fd7` |
| 14 | ReplaceContextTODO | Context Propagation | - | `b860877` |
| 15 | AddContextTimeout | Context Propagation | - | `a2f9153` |

| 16 | ContextCancellation | Context Propagation | - | To be committed |
| 17 | ContextWithValue | Context Propagation | - | To be committed |
| 18 | ParameterizedQueries | Security | - | `15` |
| 19 | SecureRandom | Security | - | To be committed |
| 20 | TLSConfig | Security | - | To be committed |
| 21 | InputValidation | Security | - | To be committed |
| 22 | PathTraversal | Security | - | To be committed |
| 23 | SQLInjection | Security | - | To be committed |
| 24 | AddTestHelper | Testing | - | To be committed |
| 25 | UseTSetenv | Testing | 1.17+ | To be committed |

**Tests:** 129 passing, 0 failing  
**Build Status:** ✅ All tests pass (Gradle + Maven)

### 🚧 In Progress (0 recipes)

| Recipe | Category | Status | Notes |
|--------|----------|--------|-------|

### 📋 Pending Recipes

#### Phase 3: Context Propagation (0 remaining)

#### Phase 6: Security (0 recipes)

#### Phase 4: Testing (5 recipes)
- [ ] TableDrivenTests - Convert sequential tests to table-driven
- [ ] TestSubtests - Convert loops to `t.Run` subtests
- [ ] BenchmarkConversion - Convert tests to benchmarks
- [ ] ExampleTestGeneration - Generate example tests from functions
- [ ] MockGeneration - Generate mocks from interfaces

#### Phase 5: Performance (7 recipes)
- [ ] PreallocateSlices - `var s []T` → `s := make([]T, 0, capacity)`
- [ ] UseStringBuilder - String concatenation → `strings.Builder`
- [ ] PreallocateMaps - `make(map[K]V)` → `make(map[K]V, capacity)`
- [ ] UseSyncPool - Frequent allocations → `sync.Pool`
- [ ] OptimizeStringConversion - `[]byte(string)` optimizations
- [ ] AvoidSliceAppend - Preallocate instead of append in loops
- [ ] UseBytesBuffer - `[]byte` operations → `bytes.Buffer`

#### Phase 7: Code Quality (7 recipes)
- [ ] ReceiverNaming - Consistent receiver names (single letter)
- [ ] ErrorVariableNaming - `errXxx` → `ErrXxx` for exported errors
- [ ] PackageComment - Add package-level documentation
- [ ] ExportedComment - Add comments to exported types/functions
- [ ] RemoveUnusedImports - Remove unused imports
- [ ] SimplifyReturn - Simplify return statements
- [ ] UseNamedReturns - Anonymous → named returns (or vice versa)
- [ ] ConstantNaming - `const xxx` → `const Xxx` for exported

#### Phase 1: Advanced Go Features (2 remaining)
- [ ] GenericsRefactor - Add type parameters to functions/types (Go 1.18+)
- [ ] FuzzTestConversion - Convert unit tests to fuzz tests (Go 1.18+)

#### Phase 8: Framework Migrations (separate modules)
- [ ] **rewrite-gin:** GinV1ToV2, GinMiddlewareUpdate, GinContextMethods, GinBindingValidation
- [ ] **rewrite-echo:** EchoV3ToV4, EchoMiddlewareUpdate
- [ ] **Standard HTTP:** HTTPRouting, HTTPMiddleware, HTTPContext

---

## 🗺️ Implementation Plan

### Execution Strategy

1. **Atomic Commits:** Each recipe = 1 commit with implementation + 100% test coverage
2. **Test-First:** Write comprehensive tests before implementation
3. **Document as We Go:** Update CAPABILITIES.md and this file after each recipe
4. **Push Frequently:** Commit and push after each recipe completion

### Phase Order

**Phase 3 (Context Propagation)** - HIGH PRIORITY  
Complete the context propagation suite for modern Go best practices.

**Phase 6 (Security)** - HIGH PRIORITY  
Enterprise adoption requires security recipes.

**Phase 4 (Testing)** - MEDIUM PRIORITY  
Improves code quality across projects.

**Phase 5 (Performance)** - MEDIUM PRIORITY  
Runtime improvements for production code.

**Phase 7 (Code Quality)** - LOWER PRIORITY  
Polish and consistency improvements.

**Phase 1 (Advanced Features)** - LOWER PRIORITY  
Complex transformations requiring more infrastructure.

**Phase 8 (Frameworks)** - SEPARATE MODULES  
Framework-specific modules (rewrite-gin, rewrite-echo).

---

## 🎯 Immediate Next Steps

### 1. Finish ContextCancellation (Recipe 16)
- [ ] Run tests: `mvn test -Dtest=ContextCancellationTest`
- [ ] Verify all 8 tests pass
- [ ] Update CAPABILITIES.md
- [ ] Commit with message: "Add ContextCancellation recipe"
- [ ] Push to GitHub

### 2. ContextWithValue (Recipe 17)
- [ ] Implement recipe
- [ ] Write tests
- [ ] Verify and commit

### 3. Security Recipes (Recipes 18-23)
- [ ] ParameterizedQueries
- [ ] InputValidation
- [ ] SecureRandom
- [ ] TLSConfig
- [ ] PathTraversal
- [ ] SQLInjection

### 4. Continue Through Phases
- Complete Phase 3 → Phase 6 → Phase 4 → Phase 5 → Phase 7 → Phase 1 → Phase 8

---

## 📈 Metrics

- **Total Recipes Target:** 50+
- **Completed:** 15 (30%)
- **In Progress:** 1
- **Remaining:** ~35
- **Test Coverage:** 100% (all completed recipes)
- **Build Health:** ✅ Green (Gradle + Maven)

---

## 🔗 Resources

- **Repository:** https://github.com/aguscalco/rewrite-go
- **Capabilities Doc:** [CAPABILITIES.md](CAPABILITIES.md)
- **Contributing Guide:** [CONTRIBUTING.md](CONTRIBUTING.md)
- **OpenRewrite Docs:** https://docs.openrewrite.org

---

## 📝 Notes

- All recipes follow the pattern: Recipe class + comprehensive tests (4-8 test cases)
- Each recipe is committed atomically with full test coverage
- Build status must be green before moving to next recipe
- Update CAPABILITIES.md after each recipe completion
- This file should be updated after each recipe to track progress
