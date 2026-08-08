<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Bytecode, IR, JIT, and AOT compatibility matrix

## How to read this matrix

This is the live baseline inventory and roadmap. A POC cell identifies an exact
implemented architectural-proof subset; only rows and targets with named
evidence below claim implementation. Status values are:

- **Current**: implemented in the existing converter/interpreter path found in source.
- **POC**: implemented in the verified TCIR/backend architectural proof for the
  exact evidenced subset.
- **Planned**: candidate continuation work; priority is not implied.
- **Fallback**: a valid method remains wholly interpreted.
- **Investigate**: semantics or test evidence need deeper source/application inspection.

The exact per-opcode semantics are in `totalcross-bytecode-reference.md`. Every
numeric TotalCross opcode 0–159 appears below; `OPCODE_LENGTH` is excluded
because it is a count. The table remains both the final architectural-proof
inventory and a continuation roadmap. Fifteen converter-backed fixtures cover
representative i32, i64, normalized-f64, reference, switch, pre-bound static
call, and object-allocation paths. Checked division/remainder and `TEST_regO`
execute through TCIR while compiled backends reject the whole function before
entry. Other cells labeled POC remain exact implemented subsets rather than
broad family-level claims. General TCZ/class-loader registration, fields,
arrays, handlers, virtual/lazy calls, monitors, and production publication
remain fallback or future work. Their absence does not make the completed
architectural proof incomplete.

The machine-readable authority added in Milestone 2 is `TotalCrossVM/src/tcvm/ir/tcir_opcode_registry.def`. Every opcode has three independent fields: decoder shape, planned lowering class, and current POC status. The lowering classes are `direct`, `lowered`, `runtime-helper`, `unsupported-in-poc`, `future`, `obsolete`, `platform-specific`, and `needs-investigation`; the POC status is `supported`, `fallback`, or `investigate`. Keeping these axes separate prevents a future direct lowering from being mistaken for already implemented frontend or backend support.

## TotalCross opcode coverage

| Range/family | TotalCross opcodes | Interpreter | TCIR frontend/interpreter | SLJIT | C AOT | Current test evidence / required focus |
|---|---|---|---|---|---|---|
| Debug/move integer | `0 BREAK`; `1 MOV_regI_regI`; `6 MOV_regI_sym`; `7 MOV_regI_s18` | Current | POC | POC | POC | Native test declarations exist for many opcode handlers; inventory and differential cases required. |
| Integer field/static | `2 MOV_regI_field`; `3 MOV_regI_static`; `24 MOV_field_regI`; `27 MOV_static_regI` | Current | Planned | Fallback | Planned | null, lazy resolution, class initialization, missing field. |
| Integer array | `4 MOV_regI_aru`; `5 MOV_regI_arc`; `8 MOV_regI_arlen`; `30 MOV_arc_regI`; `33 MOV_aru_regI`; `36 MOV_arc_regIb`; `37 MOV_arc_reg16`; `38 MOV_aru_regIb`; `39 MOV_aru_reg16`; `40 MOV_regIb_arc`; `41 MOV_reg16_arc`; `42 MOV_regIb_aru`; `43 MOV_reg16_aru` | Current | Planned | Fallback | Planned | checked/unchecked proof, signed byte vs char/short, null/bounds. |
| Reference move/field/static | `9 MOV_regO_regO`; `10 MOV_regO_field`; `11 MOV_regO_static`; `14 MOV_regO_sym`; `25 MOV_field_regO`; `28 MOV_static_regO`; `44 MOV_regO_null` | Current | Pure move/null POC; symbols/fields fallback | Pure move/null POC | Pure move/null POC | Null/opaque identity transport passed; symbol constants, heap fields, static roots, and lazy resolution remain fallback. |
| Reference array | `12 MOV_regO_aru`; `13 MOV_regO_arc`; `31 MOV_arc_regO`; `34 MOV_aru_regO` | Current | Planned | Fallback | Planned | null/bounds, forced GC, and explicit coverage of the current absence of a dynamic array-store type check. |
| 64-bit move/field/static/array | `15 MOV_reg64_reg64`; `16 MOV_reg64_field`; `17 MOV_reg64_static`; `18 MOV_reg64_aru`; `19 MOV_reg64_arc`; `20 MOV_regD_sym`; `21 MOV_regL_sym`; `22 MOV_regD_s18`; `23 MOV_regL_s18`; `26 MOV_field_reg64`; `29 MOV_static_reg64`; `32 MOV_arc_reg64`; `35 MOV_aru_reg64` | Current | Pure moves/constants POC; fields/arrays fallback | Pure moves/constants on 64-bit POC | Pure moves/constants POC | i64/f64 types come from signatures; normalized float and constant bits passed; heap access remains fallback. |
| Increment/integer add/sub/mul | `45 INC_regI`; `46 ADD_regI_regI_regI`; `47 ADD_regI_s12_regI`; `50 ADD_regI_regI_sym`; `54 SUB_regI_s12_regI`; `55 SUB_regI_regI_regI`; `58 MUL_regI_regI_s12`; `59 MUL_regI_regI_regI` | Current | POC | POC | POC | overflow, immediate sign extension, loop coverage. |
| Fused array arithmetic | `48 ADD_regI_arc_s6`; `49 ADD_regI_aru_s6`; `53 ADD_aru_regI_s6`; `80 AND_regI_aru_s6` | Current | Planned | Fallback | Planned | decomposition equivalence and check dominance. |
| Double arithmetic | `51 ADD_regD_regD_regD`; `56 SUB_regD_regD_regD`; `60 MUL_regD_regD_regD`; `64 DIV_regD_regD_regD`; `68 MOD_regD_regD_regD` | Current | Add/sub/mul POC; checked div TCIR; mod fallback | Pure add/sub/mul POC; div/mod fallback | Pure add/sub/mul POC; div/mod fallback | Finite, infinity, signed-zero, subnormal, and NaN-bit cases passed; legacy `dmod` remains target-dependent fallback. |
| Long arithmetic | `52 ADD_regL_regL_regL`; `57 SUB_regL_regL_regL`; `61 MUL_regL_regL_regL`; `65 DIV_regL_regL_regL`; `69 MOD_regL_regL_regL` | Current | Add/sub/mul POC; checked div/rem TCIR | Pure add/sub/mul on 64-bit POC; div/rem fallback | Pure add/sub/mul POC; div/rem fallback | Zero and `MIN_VALUE / -1` checked paths are TCIR-only. |
| Integer division/remainder | `62 DIV_regI_regI_s12`; `63 DIV_regI_regI_regI`; `66 MOD_regI_regI_s12`; `67 MOD_regI_regI_regI` | Current | POC | Fallback | Fallback | TCIR covers zero, exact ArithmeticException PC/hook, and `MIN/-1`; compiled lowering remains ineligible. |
| Shifts | `70 SHR_regI_regI_s12`; `71 SHR_regI_regI_regI`; `72 SHR_regL_regL_regL`; `73 SHL_regI_regI_s12`; `74 SHL_regI_regI_regI`; `75 SHL_regL_regL_regL`; `76 USHR_regI_regI_s12`; `77 USHR_regI_regI_regI`; `78 USHR_regL_regL_regL` | Current | i32/i64 POC | i32 and 64-bit i64 POC | i32/i64 POC | Java count masking/sign passed differential execution; 32-bit SLJIT rejects i64 methods before emission. |
| Bitwise | `79 AND_regI_regI_s12`; `81 AND_regI_regI_regI`; `82 AND_regL_regL_regL`; `83 OR_regI_regI_s12`; `84 OR_regI_regI_regI`; `85 OR_regL_regL_regL`; `86 XOR_regI_regI_s12`; `87 XOR_regI_regI_regI`; `88 XOR_regL_regL_regL` | Current | i32/i64 POC | i32 and 64-bit i64 POC | i32/i64 POC | Immediate/register i32 and i64 forms passed representative differential execution. |
| Equality branches | `89 JEQ_regO_regO`; `90 JEQ_regO_null`; `91 JEQ_regI_regI`; `92 JEQ_regL_regL`; `93 JEQ_regD_regD`; `94 JEQ_regI_s6`; `95 JEQ_regI_sym`; `96 JNE_regO_regO`; `97 JNE_regO_null`; `98 JNE_regI_regI`; `99 JNE_regL_regL`; `100 JNE_regD_regD`; `101 JNE_regI_s6`; `102 JNE_regI_sym` | Current | i32/i64/f64/ref POC | i32/ref and 64-bit i64/f64 POC | i32/i64/f64/ref POC | Taken/not-taken, NaN unordered behavior, null, and reference identity passed representative fixtures. |
| Ordered branches | `103 JLT_regI_regI`; `104 JLT_regL_regL`; `105 JLT_regD_regD`; `106 JLT_regI_s6`; `107 JLE_regI_regI`; `108 JLE_regL_regL`; `109 JLE_regD_regD`; `110 JLE_regI_s6`; `111 JGT_regI_regI`; `112 JGT_regL_regL`; `113 JGT_regD_regD`; `114 JGT_regI_s6`; `115 JGE_regI_regI`; `116 JGE_regL_regL`; `117 JGE_regD_regD`; `118 JGE_regI_s6`; `119 JGE_regI_arlen` | Current | Numeric POC; array-length form fallback | i32 and 64-bit i64/f64 POC; array fallback | i32/i64/f64 POC; array fallback | Numeric boundaries and NaN behavior passed; array length/null ordering remains fallback. |
| Loop/control and null check | `120 DECJGTZ_regI`; `121 DECJGEZ_regI`; `122 TEST_regO`; `123 JUMP_s24` | Current | Loop/JUMP POC; null check TCIR | Loop/JUMP POC; null-check functions fallback | Loop/JUMP POC; null-check functions fallback | `TEST_regO` publishes live homes and exact exception PC through TCIR; compiled helper lowering remains absent. |
| Conversions | `124 CONV_regI_regL`; `125 CONV_regI_regD`; `126 CONV_regIb_regI`; `127 CONV_regIc_regI`; `128 CONV_regIs_regI`; `129 CONV_regL_regI`; `130 CONV_regL_regD`; `131 CONV_regD_regI`; `132 CONV_regD_regL` | Current | Stable integer and integer-to-f64 POC; f64-to-integer fallback | Stable forms on 64-bit POC | Stable forms POC | Byte/short/char, i64/i32, and i32/i64-to-f64 passed; legacy out-of-range f64 casts remain target-dependent fallback. |
| Returns | `133 RETURN_regI`; `134 RETURN_regO`; `135 RETURN_reg64`; `136 RETURN_void`; `137 RETURN_s24I`; `138 RETURN_null`; `139 RETURN_s24D`; `140 RETURN_s24L`; `141 RETURN_symI`; `142 RETURN_symO`; `143 RETURN_symD`; `144 RETURN_symL` | Current | Typed/constant POC except symbol-ref fallback | Typed/constant POC on supported widths | Typed/constant POC except symbol-ref fallback | Result transfer covers i32/i64/f64/ref/null; unresolved reference symbols remain fallback. |
| Switch | `145 SWITCH` | Current | POC | POC | POC | Sparse/default, malformed continuation, negative, zero, and large keys covered. |
| Allocation | `146 NEWARRAY_len`; `147 NEWARRAY_regI`; `148 NEWARRAY_multi`; `149 NEWOBJ` | Current | `NEWOBJ` POC; arrays fallback | `NEWOBJ` contract POC; arrays fallback | `NEWOBJ` contract POC; arrays fallback | Allocation thunk proves roots/status/publication; no real class-loader/OMM forced-GC proof. |
| Exceptions/types | `150 THROW`; `151 INSTANCEOF`; `152 CHECKCAST` | Current | Planned | Fallback | Planned | handler ranges, class resolution, cast errors, and investigation of the current non-null throwable assumption. |
| Calls | `153 CALL_normal`; `154 CALL_virtual` | Current | Pre-bound static `CALL_normal` POC; other normal/virtual fallback | Pre-bound static POC | Pre-bound static POC | Typed arguments/results, full conservative effects, mixed interpreted/compiled/native bridge, and preflight passed; lazy/interface/virtual calls remain fallback. |
| Legacy indirect control | `155 JUMP_regI` | Current | Investigate/normalize | Fallback | Fallback until proven | shipped `jsr/ret` patterns and target enumeration. |
| Monitors | `156 MONITOR_Enter`; `157 MONITOR_Exit`; `158 MONITOR_Enter2`; `159 MONITOR_Exit2` | Current | Planned | Fallback | Planned | ownership, null/error, exception cleanup, name-table discrepancy. |

The architectural subset is intentionally method-atomic: if any operation in a
method is outside the selected backend subset, that method is rejected before
execution. Fifteen production-converter fixtures cover the exact representative
combinations named above and reached 6,398 fresh-state comparisons across
legacy `executeMethod`, TCIR, SLJIT, and AOT for eligible paths on macOS arm64.
`NEWOBJ` adds 16 separate TCIR/SLJIT/AOT allocation-contract comparisons.
Checked arithmetic and null failures remain TCIR-only where compiled helper
lowering is absent. This does not promote every operation in a surrounding
family row: registry entries marked supported still require exact fixture and
backend evidence before a broader claim.

## Java class-file and converter compatibility

| Area | Current conversion status | Planned action |
|---|---|---|
| Class-file versions | Reader constant permits major 70; preview minor 65535 is rejected | Keep independent from TCIR; add fixtures at supported boundaries. |
| Basic constants, locals, arithmetic, fields, arrays, calls | Converted to register bytecode | Differential tests through TCIR by resulting opcode family. |
| Stack manipulation (`pop`, `dup`, `swap`) | Usually resolved in converter operand-stack simulation | Verify generated register behavior; no runtime stack operation required. |
| `tableswitch`, `lookupswitch` | Normalized to TC `SWITCH` | Validate sorting, offsets and duplicate/edge cases. |
| Exceptions | Java handler PCs remapped to TotalCross slots | Preserve precise TC PC and source-line metadata. |
| `invokedynamic` string concat | Known lowering exists | Keep converter tests; TCIR sees ordinary resulting calls/ops. |
| `invokedynamic` record object methods | Known lowering exists | Keep converter tests. |
| `invokedynamic` lambdas | Java 8 lambda lowering exists | Keep converter tests and serialization fixtures. |
| Arbitrary `invokedynamic`/bootstrap | Unsupported paths return converter error | Do not claim generic support; improve diagnostic tests only. |
| Method-level `synchronized` | Flag copied, but converter/runtime state says unsupported | Preserve current behavior; explicit monitor opcodes are separate. |
| Legacy `jsr`/`ret` | Converted through `JUMP_regI` | Inspect real artifacts before compiling; interpreter fallback remains. |
| Float | Normalized into 64-bit floating bank | Test Java float rounding; do not invent `f32` from TC bytecode. |
| Invalid bytecode handling | Some reader path can terminate process | POC frontend returns structured failure for its bounded view; converter hardening is separate. |

## Platform validation matrix

The requested target matrix and current workflow reality differ. The plan must track both rather than silently claiming coverage.

| Target | Existing build evidence in workflow/source | IR interpreter | SLJIT POC | C AOT | Notes |
|---|---|---|---|---|---|
| Linux x86-64 | current CI job | required first | required first | implemented, unvalidated | GCC/Clang host build remains required |
| Linux aarch64 | current CI job | required | required | implemented, unvalidated | native runner/emulation policy to record |
| Linux armv7 | current CI job; arm32 cross job also marked disabled elsewhere | required | planned | implemented, unvalidated | distinguish active native/cross jobs |
| Windows x86 | current CI job | required | planned | implemented, unvalidated | MSVC Release must use static `/MT` runtime |
| Windows x86-64 | requested architecture target, not established by inspected workflow | required before release | planned | implemented, unvalidated | confirm product/toolchain support; validate MSVC `/MT` |
| macOS arm64 | current CI job | Representative 15-fixture architecture corpus passed | Eligible i32/i64/f64/ref/switch/call/allocation POC passed | Eligible 15-fixture generation/link POC passed | 6,398 eligible four-way comparisons plus 16 allocation-contract comparisons, mixed-mode ABI, W^X, lifecycle, sanitizers, and benchmark checkpoints passed; broader heap/handler/GC semantics remain |
| Android arm64-v8a | root `TotalCrossVM/CMakeLists.txt` with Android toolchain | compile path passed | compile path passed | generated object passed | NDK `28.2.13676358`, API 23, arm64-v8a compiled the expanded `tcir`, `tcir_jit`, `tcir_aot`, and `tcir_runtime`; no device execution or publication |
| iOS arm64 | root `TotalCrossVM/CMakeLists.txt` with iOS generator/toolchain | required where feasible | off by policy | generated object passed; primary | full root-CMake/app linkage, dead stripping, signing, and device execution remain; legacy `TCVM.xcodeproj` is out of scope |

## Registry and test status accounting

The repository contains converter tests under `TotalCrossSDK/src/test/java/tc/tools/converter/modernjava` and native test declarations/fixtures under `TotalCrossVM/src/tests` and `tcvm_test.h`. This document does not mark an opcode “tested” merely because a handler or test function name exists. The long-term execution states remain:

```text
no_test_found
source_test_declared
focused_test_run_passed
differential_ir_passed
jit_target_passed
aot_target_passed
```

Every matrix change must name the test and the platform result. A forced-backend test mode must fail if a method expected to compile silently falls back.

Eight focused CTest entries are available when `TC_BUILD_IR_TESTS=ON`,
`TC_ENABLE_SLJIT_JIT=ON`, `TC_ENABLE_C_AOT=ON`, and
`TC_ENABLE_COMPILED_DISPATCH=ON`. `tcir-core` verifies exact TCode words/source
lines generated by the production converter, repeated frontend goldens, stable
verifier/frontend diagnostics, all 160 registry dispositions, reference
execution, bounded loops, typed homes, helper contracts, and pre-execution
rejection. `tcir-opcode-sources` runs `scripts/validate-tcir-opcodes.py` across
the C, Java, runtime-dispatch, matrix, and bytecode-reference sources.
`tcir-aot` covers deterministic order, semantic invalidation, exact lookup
mismatch, and unsupported valid input. `tcir-aot-determinism` performs two clean
file generations, while `tcir-aot-manifest` independently validates schema and
generated artifacts. `tcir-differential` links the built runtime and generated-C
artifacts and runs the 15-fixture/6,398-comparison corpus plus 16 allocation
contract comparisons. `tcir-jit` retains W^X/lifecycle coverage.
`tcir-runtime` covers backend policy, method-atomic fallback, frame/usage
restoration, mixed-mode call/allocation thunks, pending-exception status, lazy
compilation concurrency, active-dispatch shutdown, and observability.
`TCIRConverterFixtureTest` independently regenerates slots through javac and
production `J2TC`.

On macOS arm64, Debug and Release passed all 8/8 integration-enabled entries and the named fixtures therefore reach both `jit_target_passed` and `aot_target_passed`. An IR-only integration build passed 4/4. With JIT/AOT libraries built but compiled dispatch disabled, 7/7 passed and neither the runtime target nor runtime symbols existed in `libtcvm`. ASan passed 8/8 with unsupported Apple leak detection disabled; the runtime test passed focused UBSan; focused Clang analysis found no bug. Android arm64-v8a/API 23 compiled the runtime library and conditional VM hook, while the pre-existing standalone full-VM build stopped later at undeclared `fadeScreen` in `gfx_Graphics.c`. Neither Android nor the earlier iPhoneOS generated-object result includes device execution or production publication.

The optional Release benchmark matrix was repeated twice during Milestone 7 under the recorded Apple M1 Pro/AC/Low Power Mode conditions. The initial revision `35b14388b690` revealed that backend `off` still acquired the runtime mutex, raising short `executeMethod` calls to 159–169 ns. Commit `3cdfd6974027` added a lock-free backend-off path; the accepted repeated 60/200/1,000 profiles measured `add` at 53.989/54.567/54.729 ns, `abs` at 59.193/59.011/59.639 ns, and SLJIT `sumTo(65537)` speedups of 1.188x–1.219x. Both matrices used 5/10/20 warmups, rotated every backend order, removed no outlier, validated every checksum, and cross-checked 720/2,400/12,000 CSV rows. All twelve raw artifacts remain revision-keyed and are recorded in the ExecPlan. This historical regime contains no AOT performance result and does not promote additional opcode/platform cells. Device execution, exception handlers, object state, GC, helper calls, automatic production registration, application benchmarks, Linux/Windows execution, and Apple distribution entitlements remain unvalidated.

The cross-check also emits the current known discrepancy: `TCConstants.bcTClassNames` contains 158 entries and omits `MONITOR_Enter2` and `MONITOR_Exit2`, while every numeric/dispatch source and the TCIR registry contain all 160. Correcting that Java presentation array remains a separately scoped compatibility change.

## Exit condition for broad backend enablement

No JIT/AOT backend may be enabled by default until all opcode families it advertises have interpreter-vs-IR and IR-vs-backend differential coverage; GC-at-helper and exception-PC stress pass; mixed-mode calls pass; malformed input is rejected before code emission; platform security gates are documented; and fallback telemetry shows no unexpected unsupported operation in the representative application corpus.
