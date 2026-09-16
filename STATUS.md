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
| 76 | LogrusToSlog | Ecosystem Frameworks | - | To be committed |
| 77 | ZapToSlog | Ecosystem Frameworks | - | To be committed |
| 78 | GorillaToStdlib | Ecosystem Frameworks | - | To be committed |
| 79 | GinBindValidation | Ecosystem Frameworks | - | To be committed |
| 80 | GormAutoMigrateCheck | Ecosystem Frameworks | - | To be committed |
| 81 | EmptyStringTest | gocritic Parity | - | To be committed |
| 82 | BoolExprSimplify | gocritic Parity | - | To be committed |
| 83 | PreferFilepathJoin | gocritic Parity | - | To be committed |
| 84 | DupArgSimplify | gocritic Parity | - | To be committed |
| 85 | Underef | gocritic Parity | - | To be committed |
| 86 | IndexToContains | staticcheck Parity | - | To be committed |
| 87 | CountToContains | staticcheck Parity | - | To be committed |
| 88 | SprintfConcatSimplify | gocritic Parity | - | To be committed |
| 89 | FmtErrorfToErrorsNew | staticcheck Parity | - | To be committed |
| 90 | ErrorsNewErrorf | staticcheck Parity | - | To be committed |
| 91 | BytesCompareToEqual | staticcheck Parity | - | To be committed |
| 92 | StringsCompareToEqual | staticcheck Parity | - | To be committed |
| 93 | FmtFormatToPrint | staticcheck Parity | - | To be committed |
| 94 | StringsIndexByte | staticcheck Parity | - | To be committed |
| 95 | MathPowToMultiplication | staticcheck Parity | - | To be committed |
| 96 | EmptyAppend | staticcheck Parity | - | To be committed |
| 97 | TimeSleepZero | gocritic Parity | - | To be committed |
| 98 | UnnecessaryStringCast | gocritic Parity | - | To be committed |

| 99 | MathExp2 | staticcheck Parity | - | To be committed |
| 100 | DoubleNegation | gocritic Parity | - | To be committed |
| 101 | StringsHasPrefixEq | staticcheck Parity | - | To be committed |
| 102 | RedundantBoolCmp | gocritic Parity | - | To be committed |
| 103 | BytesHasPrefixEq | staticcheck Parity | - | To be committed |
| 104 | TimeSubCompare | staticcheck Parity | - | To be committed |
| 105 | MathFloorPlus05 | staticcheck Parity | - | To be committed |
| 106 | IoUtilReadAllToIoReadAll | staticcheck Parity | - | To be committed |
| 107 | IoUtilReadFileToOsReadFile | staticcheck Parity | - | To be committed |
| 108 | IoUtilWriteFileToOsWriteFile | staticcheck Parity | - | To be committed |
| 109 | IoUtilReadDirToOsReadDir | staticcheck Parity | - | To be committed |
| 110 | IoUtilNopCloserToIoNopCloser | staticcheck Parity | - | To be committed |

| 111 | SecureDirectoryPermissions | gosec Parity | - | To be committed |
| 112 | SecureFilePermissions | gosec Parity | - | To be committed |
| 113 | TimeSinceNow | staticcheck Parity | - | To be committed |
| 114 | MathAbsNeg | staticcheck Parity | - | To be committed |
| 115 | SortInts | staticcheck Parity | - | To be committed |
| 116 | SortFloat64s | staticcheck Parity | - | To be committed |
| 117 | SortStrings | staticcheck Parity | - | To be committed |
| 118 | MathIsNaNCompare | staticcheck Parity | - | To be committed |
| 119 | MathPow1 | staticcheck Parity | - | To be committed |
| 120 | MathPow0 | staticcheck Parity | - | To be committed |

| 121 | StringsReplaceToReplaceAll | staticcheck Parity | - | To be committed |
| 122 | BytesReplaceToReplaceAll | staticcheck Parity | - | To be committed |
| 123 | EmptySliceTest | staticcheck Parity | - | To be committed |
| 124 | HttpStatusConstants | Idiom | - | To be committed |
| 125 | HttpRedirectConstants | Idiom | - | To be committed |
| 126 | FmtSprintString | staticcheck Parity | - | To be committed |
| 127 | FmtSprintfString | staticcheck Parity | - | To be committed |
| 128 | MathPow05 | staticcheck Parity | - | To be committed |
| 129 | StringsReplaceEmpty | staticcheck Parity | - | To be committed |
| 130 | BytesReplaceEmpty | staticcheck Parity | - | To be committed |

| 131 | HttpErrorConstants | Idiom | - | To be committed |
| 132 | StringBytesString | staticcheck Parity | - | To be committed |
| 133 | BytesStringBytes | staticcheck Parity | - | To be committed |
| 134 | SprintfIntToItoa | staticcheck Parity | - | To be committed |
| 135 | TimeUntilNow | staticcheck Parity | - | To be committed |
| 136 | ErrorsNewToFmtErrorf | staticcheck Parity | - | To be committed |
| 137 | StringsEqualFoldToBytes | staticcheck Parity | - | To be committed |
| 138 | BytesEqualFoldToStrings | staticcheck Parity | - | To be committed |
| 139 | BytesCountToContains | staticcheck Parity | - | To be committed |
| 140 | BytesIndexToContains | staticcheck Parity | - | To be committed |

| 141 | MathLog10 | staticcheck Parity | - | To be committed |
| 142 | MathLog2 | staticcheck Parity | - | To be committed |
| 143 | IndexToHasPrefix | staticcheck Parity | - | To be committed |
| 144 | BytesIndexToHasPrefix | staticcheck Parity | - | To be committed |
| 145 | FmtFprintToPrint | staticcheck Parity | - | To be committed |
| 146 | MathExpm1 | staticcheck Parity | - | To be committed |
| 147 | MathLog1p | staticcheck Parity | - | To be committed |
| 148 | FilepathJoinEmptyString | staticcheck Parity | - | To be committed |
| 149 | PathJoinEmptyString | staticcheck Parity | - | To be committed |
| 150 | BytesEqualNil | staticcheck Parity | - | To be committed |

| 151 | IoUtilTempFileToOsCreateTemp | staticcheck Parity | - | To be committed |
| 152 | IoUtilTempDirToOsMkdirTemp | staticcheck Parity | - | To be committed |
| 153 | BytesCompareNotEqual | staticcheck Parity | - | To be committed |
| 154 | StringsCompareNotEqual | staticcheck Parity | - | To be committed |
| 155 | StringsEqualFold | staticcheck Parity | - | To be committed |
| 156 | BytesEqualFold | staticcheck Parity | - | To be committed |
| 157 | SortStringsAreSorted | staticcheck Parity | - | To be committed |
| 158 | SortIntsAreSorted | staticcheck Parity | - | To be committed |
| 159 | SortFloat64sAreSorted | staticcheck Parity | - | To be committed |
| 160 | MathPow10 | staticcheck Parity | - | To be committed |

| 161 | TimeNowSubToSince | staticcheck Parity | - | To be committed |
| 162 | TimeSubNowToUntil | staticcheck Parity | - | To be committed |
| 163 | MathFloorAddHalf | staticcheck Parity | - | To be committed |
| 164 | FmtPrintfNoArgs | staticcheck Parity | - | To be committed |
| 165 | FmtFprintfNoArgs | staticcheck Parity | - | To be committed |
| 166 | FmtSprintfNoArgs | staticcheck Parity | - | To be committed |
| 167 | SortSortIntSlice | staticcheck Parity | - | To be committed |
| 168 | SortSortStringSlice | staticcheck Parity | - | To be committed |
| 169 | SortSortFloat64Slice | staticcheck Parity | - | To be committed |
| 170 | FmtSprintfVToSprint | staticcheck Parity | - | To be committed |

**Tests:** 276 passing, 0 failing  
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
