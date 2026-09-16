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
| 26 | TestSubtests | Testing | - | To be committed |
| 27 | TableDrivenTests | Testing | - | To be committed |
| 28 | BenchmarkConversion | Testing | - | To be committed |
| 29 | ExampleTestGeneration | Testing | - | To be committed |
| 30 | MockGeneration | Testing | - | To be committed |
| 31 | PreallocateSlices | Performance | - | To be committed |
| 32 | PreallocateMaps | Performance | - | To be committed |
| 33 | AvoidSliceAppend | Performance | - | To be committed |
| 34 | UseStringBuilder | Performance | - | To be committed |
| 35 | UseSyncPool | Performance | - | To be committed |
| 36 | OptimizeStringConversion | Performance | - | To be committed |
| 37 | UseBytesBuffer | Performance | - | To be committed |
| 38 | ReceiverNaming | Code Quality | - | To be committed |
| 39 | PackageComment | Code Quality | - | To be committed |
| 40 | ExportedComment | Code Quality | - | To be committed |
| 41 | SimplifyReturn | Code Quality | - | To be committed |
| 42 | UseNamedReturns | Code Quality | - | To be committed |
| 43 | ErrorVariableNaming | Code Quality | - | To be committed |
| 44 | ConstantNaming | Code Quality | - | To be committed |

| 45 | GenericsRefactor | Advanced Go Features | - | To be committed |
| 46 | FuzzTestConversion | Advanced Go Features | - | To be committed |

| 47 | GinV1ToV2 | Framework Migrations | - | To be committed |
| 48 | EchoV4ToV5 | Framework Migrations | - | To be committed |
| 49 | HttpServeMuxRouting | Framework Migrations | - | To be committed |
| 50 | GormV1ToV2 | Framework Migrations | - | To be committed |
| 51 | LoopVarCaptureFix | Modern Go Adoptions | - | To be committed |
| 52 | UseClearBuiltin | Modern Go Adoptions | - | To be committed |
| 53 | UseMinMaxBuiltins | Modern Go Adoptions | - | To be committed |
| 54 | ErrorsJoinMigration | Modern Go Adoptions | - | To be committed |
| 55 | IterSeqMigration | Modern Go Adoptions | - | To be committed |
| 56 | DetectContextLeak | Concurrency & Safety | - | To be committed |
| 57 | MutexByValue | Concurrency & Safety | - | To be committed |
| 58 | TimerLeakPrevention | Concurrency & Safety | - | To be committed |
| 59 | AtomicPointerMigration | Concurrency & Safety | - | To be committed |
| 60 | DeferInLoop | Concurrency & Safety | - | To be committed |
| 61 | YodaCondition | Code Quality & Linters | - | To be committed |
| 62 | RemoveRedundantType | Code Quality & Linters | - | To be committed |
| 63 | SimplifyRange | Code Quality & Linters | - | To be committed |
| 64 | TimeSinceFix | Code Quality & Linters | - | To be committed |
| 65 | TimeUntilFix | Code Quality & Linters | - | To be committed |
| 66 | TempDirMigration | Testing & CI | - | To be committed |
| 67 | DeepEqualMigration | Testing & CI | - | To be committed |
| 68 | ParallelizeTests | Testing & CI | - | To be committed |
| 69 | TestifyNoErrorToStdlib | Testing & CI | - | To be committed |
| 70 | MockgenToUberMock | Testing & CI | - | To be committed |
| 71 | InsecureTLSCheck | Security & Crypto | - | To be committed |
| 72 | HardcodedSecretRemoval | Security & Crypto | - | To be committed |
| 73 | WeakHashMigration | Security & Crypto | - | To be committed |
| 74 | SSRFPrevention | Security & Crypto | - | To be committed |
| 75 | WeakCryptoKeyCheck | Security & Crypto | - | To be committed |

**Tests:** 180 passing, 0 failing  
**Build Status:** ✅ All tests pass (Gradle + Maven)

### 🚧 In Progress (0 recipes)

### ⏳ Pending (0 recipes)

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
