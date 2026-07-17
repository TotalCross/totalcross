<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Bytecode, IR, JIT, and AOT compatibility matrix

## How to read this matrix

This is the baseline inventory for the planned work, not a claim that JIT/AOT exists. Status values are:

- **Current**: implemented in the existing converter/interpreter path found in source.
- **POC**: required in the first verified TCIR + backend proof of concept.
- **Planned**: must be implemented in a later milestone before broad enablement.
- **Fallback**: a valid method remains wholly interpreted.
- **Investigate**: semantics or test evidence need deeper source/application inspection.

The exact per-opcode semantics are in `totalcross-bytecode-reference.md`. Every numeric TotalCross opcode 0–159 appears below; `OPCODE_LENGTH` is excluded because it is a count.

## TotalCross opcode coverage

| Range/family | TotalCross opcodes | Interpreter | TCIR frontend/interpreter | SLJIT | C AOT | Current test evidence / required focus |
|---|---|---|---|---|---|---|
| Debug/move integer | `0 BREAK`; `1 MOV_regI_regI`; `6 MOV_regI_sym`; `7 MOV_regI_s18` | Current | POC | POC | POC | Native test declarations exist for many opcode handlers; inventory and differential cases required. |
| Integer field/static | `2 MOV_regI_field`; `3 MOV_regI_static`; `24 MOV_field_regI`; `27 MOV_static_regI` | Current | Planned | Fallback | Planned | null, lazy resolution, class initialization, missing field. |
| Integer array | `4 MOV_regI_aru`; `5 MOV_regI_arc`; `8 MOV_regI_arlen`; `30 MOV_arc_regI`; `33 MOV_aru_regI`; `36 MOV_arc_regIb`; `37 MOV_arc_reg16`; `38 MOV_aru_regIb`; `39 MOV_aru_reg16`; `40 MOV_regIb_arc`; `41 MOV_reg16_arc`; `42 MOV_regIb_aru`; `43 MOV_reg16_aru` | Current | Planned | Fallback | Planned | checked/unchecked proof, signed byte vs char/short, null/bounds. |
| Reference move/field/static | `9 MOV_regO_regO`; `10 MOV_regO_field`; `11 MOV_regO_static`; `14 MOV_regO_sym`; `25 MOV_field_regO`; `28 MOV_static_regO`; `44 MOV_regO_null` | Current | Planned | Fallback | Planned | GC homes, static roots, lazy resolution. |
| Reference array | `12 MOV_regO_aru`; `13 MOV_regO_arc`; `31 MOV_arc_regO`; `34 MOV_aru_regO` | Current | Planned | Fallback | Planned | null/bounds, forced GC, and explicit coverage of the current absence of a dynamic array-store type check. |
| 64-bit move/field/static/array | `15 MOV_reg64_reg64`; `16 MOV_reg64_field`; `17 MOV_reg64_static`; `18 MOV_reg64_aru`; `19 MOV_reg64_arc`; `20 MOV_regD_sym`; `21 MOV_regL_sym`; `22 MOV_regD_s18`; `23 MOV_regL_s18`; `26 MOV_field_reg64`; `29 MOV_static_reg64`; `32 MOV_arc_reg64`; `35 MOV_aru_reg64` | Current | Planned | Fallback | Planned | distinguish `i64`/`f64` from signature; float normalization. |
| Increment/integer add/sub/mul | `45 INC_regI`; `46 ADD_regI_regI_regI`; `47 ADD_regI_s12_regI`; `50 ADD_regI_regI_sym`; `54 SUB_regI_s12_regI`; `55 SUB_regI_regI_regI`; `58 MUL_regI_regI_s12`; `59 MUL_regI_regI_regI` | Current | POC | POC | POC | overflow, immediate sign extension, loop coverage. |
| Fused array arithmetic | `48 ADD_regI_arc_s6`; `49 ADD_regI_aru_s6`; `53 ADD_aru_regI_s6`; `80 AND_regI_aru_s6` | Current | Planned | Fallback | Planned | decomposition equivalence and check dominance. |
| Double arithmetic | `51 ADD_regD_regD_regD`; `56 SUB_regD_regD_regD`; `60 MUL_regD_regD_regD`; `64 DIV_regD_regD_regD`; `68 MOD_regD_regD_regD` | Current | Planned | Fallback | Planned | NaN, infinities, signed zero, Java remainder. |
| Long arithmetic | `52 ADD_regL_regL_regL`; `57 SUB_regL_regL_regL`; `61 MUL_regL_regL_regL`; `65 DIV_regL_regL_regL`; `69 MOD_regL_regL_regL` | Current | Planned | Fallback | Planned | zero division and `MIN_VALUE / -1`. |
| Integer division/remainder | `62 DIV_regI_regI_s12`; `63 DIV_regI_regI_regI`; `66 MOD_regI_regI_s12`; `67 MOD_regI_regI_regI` | Current | Planned | Fallback | Planned | exact ArithmeticException PC and overflow edge. |
| Shifts | `70 SHR_regI_regI_s12`; `71 SHR_regI_regI_regI`; `72 SHR_regL_regL_regL`; `73 SHL_regI_regI_s12`; `74 SHL_regI_regI_regI`; `75 SHL_regL_regL_regL`; `76 USHR_regI_regI_s12`; `77 USHR_regI_regI_regI`; `78 USHR_regL_regL_regL` | Current | Planned | Fallback | Planned | Java shift-count masking and sign. |
| Bitwise | `79 AND_regI_regI_s12`; `81 AND_regI_regI_regI`; `82 AND_regL_regL_regL`; `83 OR_regI_regI_s12`; `84 OR_regI_regI_regI`; `85 OR_regL_regL_regL`; `86 XOR_regI_regI_s12`; `87 XOR_regI_regI_regI`; `88 XOR_regL_regL_regL` | Current | Planned | Fallback | Planned | immediate encoding and long width. |
| Equality branches | `89 JEQ_regO_regO`; `90 JEQ_regO_null`; `91 JEQ_regI_regI`; `92 JEQ_regL_regL`; `93 JEQ_regD_regD`; `94 JEQ_regI_s6`; `95 JEQ_regI_sym`; `96 JNE_regO_regO`; `97 JNE_regO_null`; `98 JNE_regI_regI`; `99 JNE_regL_regL`; `100 JNE_regD_regD`; `101 JNE_regI_s6`; `102 JNE_regI_sym` | Current | Integer subset POC; rest planned | Integer subset POC | Integer subset POC | taken/not-taken, NaN unordered behavior, reference identity. |
| Ordered branches | `103 JLT_regI_regI`; `104 JLT_regL_regL`; `105 JLT_regD_regD`; `106 JLT_regI_s6`; `107 JLE_regI_regI`; `108 JLE_regL_regL`; `109 JLE_regD_regD`; `110 JLE_regI_s6`; `111 JGT_regI_regI`; `112 JGT_regL_regL`; `113 JGT_regD_regD`; `114 JGT_regI_s6`; `115 JGE_regI_regI`; `116 JGE_regL_regL`; `117 JGE_regD_regD`; `118 JGE_regI_s6`; `119 JGE_regI_arlen` | Current | Integer subset POC; rest planned | Integer subset POC | Integer subset POC | boundary values, NaN, array length/null ordering. |
| Loop/control and null check | `120 DECJGTZ_regI`; `121 DECJGEZ_regI`; `122 TEST_regO`; `123 JUMP_s24` | Current | Integer/JUMP POC; null check planned | Integer/JUMP POC | Integer/JUMP POC | backward edges, zero/negative, `TEST_regO` exception behavior, GC poll policy. |
| Conversions | `124 CONV_regI_regL`; `125 CONV_regI_regD`; `126 CONV_regIb_regI`; `127 CONV_regIc_regI`; `128 CONV_regIs_regI`; `129 CONV_regL_regI`; `130 CONV_regL_regD`; `131 CONV_regD_regI`; `132 CONV_regD_regL` | Current | Planned | Fallback | Planned | NaN/out-of-range Java conversion, signed/unsigned narrow types. |
| Returns | `133 RETURN_regI`; `134 RETURN_regO`; `135 RETURN_reg64`; `136 RETURN_void`; `137 RETURN_s24I`; `138 RETURN_null`; `139 RETURN_s24D`; `140 RETURN_s24L`; `141 RETURN_symI`; `142 RETURN_symO`; `143 RETURN_symD`; `144 RETURN_symL` | Current | Integer/void POC; rest planned | Integer/void POC | Integer/void POC | result transfer, reference root lifetime, constants. |
| Switch | `145 SWITCH` | Current | Planned | Fallback | Planned | sparse/dense, default, malformed continuation, negative key. |
| Allocation | `146 NEWARRAY_len`; `147 NEWARRAY_regI`; `148 NEWARRAY_multi`; `149 NEWOBJ` | Current | Planned | Fallback | Planned | OOM, negative length, GC at every allocation, class init. |
| Exceptions/types | `150 THROW`; `151 INSTANCEOF`; `152 CHECKCAST` | Current | Planned | Fallback | Planned | handler ranges, class resolution, cast errors, and investigation of the current non-null throwable assumption. |
| Calls | `153 CALL_normal`; `154 CALL_virtual` | Current | Direct call after POC; virtual planned | Fallback initially | Direct dispatcher thunk planned | interpreted/compiled/native matrix, cache races, exception/GC. |
| Legacy indirect control | `155 JUMP_regI` | Current | Investigate/normalize | Fallback | Fallback until proven | shipped `jsr/ret` patterns and target enumeration. |
| Monitors | `156 MONITOR_Enter`; `157 MONITOR_Exit`; `158 MONITOR_Enter2`; `159 MONITOR_Exit2` | Current | Planned | Fallback | Planned | ownership, null/error, exception cleanup, name-table discrepancy. |

The first POC is intentionally method-atomic: if any operation in a method is outside the POC subset, that method is interpreted from its first instruction.

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
| Invalid bytecode handling | Some reader path can terminate process | Future frontend returns structured failure; converter hardening is separate. |

## Platform validation matrix

The requested target matrix and current workflow reality differ. The plan must track both rather than silently claiming coverage.

| Target | Existing build evidence in workflow/source | IR interpreter | SLJIT POC | C AOT | Notes |
|---|---|---|---|---|---|
| Linux x86-64 | current CI job | required first | required first | required first | primary development/reference target |
| Linux aarch64 | current CI job | required | required | required | native runner/emulation policy to record |
| Linux armv7 | current CI job; arm32 cross job also marked disabled elsewhere | required | planned | required | distinguish active native/cross jobs |
| Windows x86 | current CI job | required | planned | required | current repository target |
| Windows x86-64 | requested architecture target, not established by inspected workflow | required before release | planned | planned | add only after toolchain/product support is confirmed |
| macOS arm64 | current CI job | required | opt-in only | required | Hardened Runtime/JIT entitlement gate |
| Android arm64-v8a | root `TotalCrossVM/CMakeLists.txt` with Android toolchain | required | off initially | required | device tests and executable-memory policy; legacy `Android.mk` is out of scope |
| iOS arm64 | root `TotalCrossVM/CMakeLists.txt` with iOS generator/toolchain | required where feasible | off by policy | required/primary | generated C statically linked; legacy `TCVM.xcodeproj` is out of scope |

## Test status accounting

The repository contains converter tests under `TotalCrossSDK/src/test/java/tc/tools/converter/modernjava` and native test declarations/fixtures under `TotalCrossVM/src/tests` and `tcvm_test.h`. This document does not mark an opcode “tested” merely because a handler or test function name exists. Milestone 0 must generate a machine-readable cross-check with these states:

```text
no_test_found
source_test_declared
focused_test_run_passed
differential_ir_passed
jit_target_passed
aot_target_passed
```

Every matrix change must name the test and the platform result. A forced-backend test mode must fail if a method expected to compile silently falls back.

## Exit condition for broad backend enablement

No JIT/AOT backend may be enabled by default until all opcode families it advertises have interpreter-vs-IR and IR-vs-backend differential coverage; GC-at-helper and exception-PC stress pass; mixed-mode calls pass; malformed input is rejected before code emission; platform security gates are documented; and fallback telemetry shows no unexpected unsupported operation in the representative application corpus.
