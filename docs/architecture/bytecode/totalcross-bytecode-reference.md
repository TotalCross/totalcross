<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# TotalCross bytecode reference

## Conventions

This reference inventories opcodes 0 through 159 as declared in `opcodes.h` and dispatched in `tcvm.c`. `OPCODE_LENGTH` is 160 and is a count, not an opcode.

The VM is register-based: no instruction consumes or produces a runtime operand stack. In the tables, `d` means destination, `s` source, `o` receiver/object, `a` array, `i` index, `k` constant/symbol, `f` field, `m` method, and `Δ` a displacement in slots. `I`, `O`, `L`, and `D` identify integer, reference, long, and double registers. `64` selects `L` or `D` according to the method type metadata.

`arc` performs null and bounds checks and falls through to the `aru` implementation; `aru` assumes that the access was already checked or proven safe. Field operations may throw null/class/field errors; static operations may resolve classes/fields; integer division/remainder may throw on zero; allocation, calls, type operations, monitors, and `THROW` use runtime helpers. A compiler must treat helper calls as potential GC points according to the runtime ABI.

Every entry occupies one 4-byte slot, except that calls, switch, and multidimensional arrays have additional data slots. The IR column names the proposed canonical operation; it is not an existing implementation.

## 0–44: moves, fields, and arrays

| No. | Opcode | Operands and effect | Check/exception | Proposed IR |
|---:|---|---|---|---|
| 0 | `BREAK` | no operands; production NOP, controlled test termination | none | `nop`/debug trap |
| 1 | `MOV_regI_regI` | `dI = sI` | none | `copy i32` |
| 2 | `MOV_regI_field` | `dI = o.f` | null/resolution | `load.field i32` |
| 3 | `MOV_regI_static` | `dI = static.f` | resolution | `load.static i32` |
| 4 | `MOV_regI_aru` | `dI = a[i]`, unchecked | precondition | `load.array.unchecked i32` |
| 5 | `MOV_regI_arc` | `dI = a[i]`, checked | null/bounds | `load.array i32` |
| 6 | `MOV_regI_sym` | `dI = cp.i32[k]` | frontend validates index | `const i32` |
| 7 | `MOV_regI_s18` | `dI = imm18` | none | `const i32` |
| 8 | `MOV_regI_arlen` | `dI = length(a)` | null | `array.length` |
| 9 | `MOV_regO_regO` | `dO = sO` | none | `copy ref` |
| 10 | `MOV_regO_field` | `dO = o.f` | null/resolution | `load.field ref` |
| 11 | `MOV_regO_static` | `dO = static.f` | resolution | `load.static ref` |
| 12 | `MOV_regO_aru` | `dO = a[i]`, unchecked | precondition | `load.array.unchecked ref` |
| 13 | `MOV_regO_arc` | `dO = a[i]`, checked | null/bounds | `load.array ref` |
| 14 | `MOV_regO_sym` | `dO = cp.string[k]` | symbol | `const.ref` |
| 15 | `MOV_reg64_reg64` | `d64 = s64` | none | `copy i64/f64` |
| 16 | `MOV_reg64_field` | `d64 = o.f` | null/resolution | `load.field i64/f64` |
| 17 | `MOV_reg64_static` | `d64 = static.f` | resolution | `load.static i64/f64` |
| 18 | `MOV_reg64_aru` | `d64 = a[i]`, unchecked | precondition | `load.array.unchecked i64/f64` |
| 19 | `MOV_reg64_arc` | `d64 = a[i]`, checked | null/bounds | `load.array i64/f64` |
| 20 | `MOV_regD_sym` | `dD = cp.double[k]` | symbol | `const f64` |
| 21 | `MOV_regL_sym` | `dL = cp.i64[k]` | symbol | `const i64` |
| 22 | `MOV_regD_s18` | `dD = converted imm18` | none | `const f64` |
| 23 | `MOV_regL_s18` | `dL = sign-extended imm18` | none | `const i64` |
| 24 | `MOV_field_regI` | `o.f = sI` | null/resolution | `store.field i32` |
| 25 | `MOV_field_regO` | `o.f = sO` | null/resolution | `store.field ref` |
| 26 | `MOV_field_reg64` | `o.f = s64` | null/resolution | `store.field i64/f64` |
| 27 | `MOV_static_regI` | `static.f = sI` | resolution | `store.static i32` |
| 28 | `MOV_static_regO` | `static.f = sO` | resolution | `store.static ref` |
| 29 | `MOV_static_reg64` | `static.f = s64` | resolution | `store.static i64/f64` |
| 30 | `MOV_arc_regI` | `a[i] = sI`, checked | null/bounds | `store.array i32` |
| 31 | `MOV_arc_regO` | `a[i] = sO`, checked | null/bounds; no dynamic array-store type check in the current handler | `store.array ref` |
| 32 | `MOV_arc_reg64` | `a[i] = s64`, checked | null/bounds | `store.array i64/f64` |
| 33 | `MOV_aru_regI` | `a[i] = sI`, unchecked | precondition | `store.array.unchecked i32` |
| 34 | `MOV_aru_regO` | `a[i] = sO`, unchecked | precondition; no dynamic array-store type check in the current handler | `store.array.unchecked ref` |
| 35 | `MOV_aru_reg64` | `a[i] = s64`, unchecked | precondition | `store.array.unchecked i64/f64` |
| 36 | `MOV_arc_regIb` | `a[i] = low8(sI)`, checked | null/bounds | `store.array i8` |
| 37 | `MOV_arc_reg16` | `a[i] = low16(sI)`, checked | null/bounds | `store.array i16` |
| 38 | `MOV_aru_regIb` | unchecked 8-bit store | precondition | `store.array.unchecked i8` |
| 39 | `MOV_aru_reg16` | unchecked 16-bit store | precondition | `store.array.unchecked i16` |
| 40 | `MOV_regIb_arc` | checked 8-bit load and extension to `dI` | null/bounds | `load.array i8` |
| 41 | `MOV_reg16_arc` | checked 16-bit load and extension to `dI` | null/bounds | `load.array i16` |
| 42 | `MOV_regIb_aru` | unchecked 8-bit load | precondition | `load.array.unchecked i8` |
| 43 | `MOV_reg16_aru` | unchecked 16-bit load | precondition | `load.array.unchecked i16` |
| 44 | `MOV_regO_null` | `dO = null` | none | `const null` |

## 45–88: arithmetic and bit operations

| No. | Opcode | Operands and effect | Check/exception | Proposed IR |
|---:|---|---|---|---|
| 45 | `INC_regI` | `rI += imm16` | Java modular overflow | `add i32` |
| 46 | `ADD_regI_regI_regI` | `dI = s1I + s2I` | none | `add i32` |
| 47 | `ADD_regI_s12_regI` | `dI = imm12 + sI` | none | `add i32` |
| 48 | `ADD_regI_arc_s6` | `dI = a[i] + imm6`, checked | null/bounds | `load.array; add` |
| 49 | `ADD_regI_aru_s6` | same, unchecked | precondition | `load.array.unchecked; add` |
| 50 | `ADD_regI_regI_sym` | `dI = sI + cp.i32[k]` | symbol | `add i32` |
| 51 | `ADD_regD_regD_regD` | `dD = s1D + s2D` | IEEE-754 | `fadd f64` |
| 52 | `ADD_regL_regL_regL` | `dL = s1L + s2L` | none | `add i64` |
| 53 | `ADD_aru_regI_s6` | `a[i] += imm6`, unchecked | precondition | `load/add/store.array.unchecked` |
| 54 | `SUB_regI_s12_regI` | `dI = imm12 - sI` | none | `sub i32` |
| 55 | `SUB_regI_regI_regI` | `dI = s1I - s2I` | none | `sub i32` |
| 56 | `SUB_regD_regD_regD` | `dD = s1D - s2D` | IEEE-754 | `fsub f64` |
| 57 | `SUB_regL_regL_regL` | `dL = s1L - s2L` | none | `sub i64` |
| 58 | `MUL_regI_regI_s12` | `dI = sI * imm12` | none | `mul i32` |
| 59 | `MUL_regI_regI_regI` | `dI = s1I * s2I` | none | `mul i32` |
| 60 | `MUL_regD_regD_regD` | `dD = s1D * s2D` | IEEE-754 | `fmul f64` |
| 61 | `MUL_regL_regL_regL` | `dL = s1L * s2L` | none | `mul i64` |
| 62 | `DIV_regI_regI_s12` | `dI = sI / imm12` | zero; Java `MIN/-1` | `div.s i32` |
| 63 | `DIV_regI_regI_regI` | `dI = s1I / s2I` | zero; Java `MIN/-1` | `div.s i32` |
| 64 | `DIV_regD_regD_regD` | `dD = s1D / s2D` | IEEE-754 | `fdiv f64` |
| 65 | `DIV_regL_regL_regL` | `dL = s1L / s2L` | zero; Java `MIN/-1` | `div.s i64` |
| 66 | `MOD_regI_regI_s12` | `dI = sI % imm12` | zero | `rem.s i32` |
| 67 | `MOD_regI_regI_regI` | `dI = s1I % s2I` | zero | `rem.s i32` |
| 68 | `MOD_regD_regD_regD` | Java floating remainder | IEEE-754 | `frem f64` |
| 69 | `MOD_regL_regL_regL` | `dL = s1L % s2L` | zero | `rem.s i64` |
| 70 | `SHR_regI_regI_s12` | arithmetic right shift by immediate | Java count mask | `ashr i32` |
| 71 | `SHR_regI_regI_regI` | arithmetic right shift by register | Java count mask | `ashr i32` |
| 72 | `SHR_regL_regL_regL` | arithmetic right shift of long | Java count mask | `ashr i64` |
| 73 | `SHL_regI_regI_s12` | left shift by immediate | Java count mask | `shl i32` |
| 74 | `SHL_regI_regI_regI` | left shift by register | Java count mask | `shl i32` |
| 75 | `SHL_regL_regL_regL` | left shift of long | Java count mask | `shl i64` |
| 76 | `USHR_regI_regI_s12` | logical right shift by immediate | Java count mask | `lshr i32` |
| 77 | `USHR_regI_regI_regI` | logical right shift by register | Java count mask | `lshr i32` |
| 78 | `USHR_regL_regL_regL` | logical right shift of long | Java count mask | `lshr i64` |
| 79 | `AND_regI_regI_s12` | `dI = sI & imm12` | none | `and i32` |
| 80 | `AND_regI_aru_s6` | `dI = a[i] & imm6`, unchecked | precondition | `load.array.unchecked; and` |
| 81 | `AND_regI_regI_regI` | `dI = s1I & s2I` | none | `and i32` |
| 82 | `AND_regL_regL_regL` | `dL = s1L & s2L` | none | `and i64` |
| 83 | `OR_regI_regI_s12` | `dI = sI \| imm12` | none | `or i32` |
| 84 | `OR_regI_regI_regI` | `dI = s1I \| s2I` | none | `or i32` |
| 85 | `OR_regL_regL_regL` | `dL = s1L \| s2L` | none | `or i64` |
| 86 | `XOR_regI_regI_s12` | `dI = sI ^ imm12` | none | `xor i32` |
| 87 | `XOR_regI_regI_regI` | `dI = s1I ^ s2I` | none | `xor i32` |
| 88 | `XOR_regL_regL_regL` | `dL = s1L ^ s2L` | none | `xor i64` |

## 89–123: comparison and control flow

Conditional branches set `PC = PC + Δ` when true and advance when false. Double comparisons must preserve NaN behavior already selected by Java conversion.

| No. | Opcode | Condition/effect | Proposed IR |
|---:|---|---|---|
| 89 | `JEQ_regO_regO` | `s1O == s2O` | `cmp.eq ref; br` |
| 90 | `JEQ_regO_null` | `sO == null` | `is.null; br` |
| 91 | `JEQ_regI_regI` | `s1I == s2I` | `cmp.eq i32; br` |
| 92 | `JEQ_regL_regL` | `s1L == s2L` | `cmp.eq i64; br` |
| 93 | `JEQ_regD_regD` | `s1D == s2D` | `fcmp.oeq f64; br` |
| 94 | `JEQ_regI_s6` | `sI == imm6` | `cmp.eq i32; br` |
| 95 | `JEQ_regI_sym` | `sI == cp.i32[k]` | `cmp.eq i32; br` |
| 96 | `JNE_regO_regO` | `s1O != s2O` | `cmp.ne ref; br` |
| 97 | `JNE_regO_null` | `sO != null` | `is.nonnull; br` |
| 98 | `JNE_regI_regI` | `s1I != s2I` | `cmp.ne i32; br` |
| 99 | `JNE_regL_regL` | `s1L != s2L` | `cmp.ne i64; br` |
| 100 | `JNE_regD_regD` | `s1D != s2D` | `fcmp.une f64; br` |
| 101 | `JNE_regI_s6` | `sI != imm6` | `cmp.ne i32; br` |
| 102 | `JNE_regI_sym` | `sI != cp.i32[k]` | `cmp.ne i32; br` |
| 103 | `JLT_regI_regI` | `s1I < s2I` | `cmp.lt.s i32; br` |
| 104 | `JLT_regL_regL` | `s1L < s2L` | `cmp.lt.s i64; br` |
| 105 | `JLT_regD_regD` | `s1D < s2D` | `fcmp.olt f64; br` |
| 106 | `JLT_regI_s6` | `sI < imm6` | `cmp.lt.s i32; br` |
| 107 | `JLE_regI_regI` | `s1I <= s2I` | `cmp.le.s i32; br` |
| 108 | `JLE_regL_regL` | `s1L <= s2L` | `cmp.le.s i64; br` |
| 109 | `JLE_regD_regD` | `s1D <= s2D` | `fcmp.ole f64; br` |
| 110 | `JLE_regI_s6` | `sI <= imm6` | `cmp.le.s i32; br` |
| 111 | `JGT_regI_regI` | `s1I > s2I` | `cmp.gt.s i32; br` |
| 112 | `JGT_regL_regL` | `s1L > s2L` | `cmp.gt.s i64; br` |
| 113 | `JGT_regD_regD` | `s1D > s2D` | `fcmp.ogt f64; br` |
| 114 | `JGT_regI_s6` | `sI > imm6` | `cmp.gt.s i32; br` |
| 115 | `JGE_regI_regI` | `s1I >= s2I` | `cmp.ge.s i32; br` |
| 116 | `JGE_regL_regL` | `s1L >= s2L` | `cmp.ge.s i64; br` |
| 117 | `JGE_regD_regD` | `s1D >= s2D` | `fcmp.oge f64; br` |
| 118 | `JGE_regI_s6` | `sI >= imm6` | `cmp.ge.s i32; br` |
| 119 | `JGE_regI_arlen` | `sI >= length(a)`; current handler assumes non-null `a` | `array.length.unchecked; cmp.ge; br` |
| 120 | `DECJGTZ_regI` | decrement `rI`; branch if `> 0` | `sub; cmp.gt; br` |
| 121 | `DECJGEZ_regI` | decrement `rI`; branch if `>= 0` | `sub; cmp.ge; br` |
| 122 | `TEST_regO` | continue when non-null; throw `NullPointerException` when null | `null.check` |
| 123 | `JUMP_s24` | unconditional relative branch | `br` |

## 124–159: conversions, returns, and runtime operations

| No. | Opcode | Operands and effect | Check/exception | Proposed IR |
|---:|---|---|---|---|
| 124 | `CONV_regI_regL` | `dI = (i32)sL` | none | `trunc i64` |
| 125 | `CONV_regI_regD` | Java double-to-int conversion | NaN/range rules | `fptosi.java i32` |
| 126 | `CONV_regIb_regI` | truncate/sign-extend byte | none | `sext(trunc i8)` |
| 127 | `CONV_regIc_regI` | truncate/zero-extend char | none | `zext(trunc i16)` |
| 128 | `CONV_regIs_regI` | truncate/sign-extend short | none | `sext(trunc i16)` |
| 129 | `CONV_regL_regI` | sign-extend int to long | none | `sext i32 to i64` |
| 130 | `CONV_regL_regD` | Java double-to-long conversion | NaN/range rules | `fptosi.java i64` |
| 131 | `CONV_regD_regI` | signed int to double | IEEE rounding | `sitofp i32` |
| 132 | `CONV_regD_regL` | signed long to double | IEEE rounding | `sitofp i64` |
| 133 | `RETURN_regI` | return `sI` | frame unwind | `ret i32` |
| 134 | `RETURN_regO` | return `sO` | root transfer | `ret ref` |
| 135 | `RETURN_reg64` | return `s64` | none | `ret i64/f64` |
| 136 | `RETURN_void` | return without value | none | `ret void` |
| 137 | `RETURN_s24I` | return immediate integer | none | `ret const i32` |
| 138 | `RETURN_null` | return `null` | none | `ret null` |
| 139 | `RETURN_s24D` | return immediate converted to double | none | `ret const f64` |
| 140 | `RETURN_s24L` | return immediate extended to long | none | `ret const i64` |
| 141 | `RETURN_symI` | return `cp.i32[k]` | symbol | `ret const i32` |
| 142 | `RETURN_symO` | return pool reference | symbol/root | `ret const.ref` |
| 143 | `RETURN_symD` | return `cp.double[k]` | symbol | `ret const f64` |
| 144 | `RETURN_symL` | return `cp.i64[k]` | symbol | `ret const i64` |
| 145 | `SWITCH` | table lookup and relative branch; extra slots | table validation | `switch` |
| 146 | `NEWARRAY_len` | allocate typed array with immediate length | length/OOM/GC | `new.array` |
| 147 | `NEWARRAY_regI` | allocate typed array with register length | length/OOM/GC | `new.array` |
| 148 | `NEWARRAY_multi` | allocate multidimensional array; extra slots | length/OOM/GC | `new.multiarray` |
| 149 | `NEWOBJ` | allocate object of class symbol | resolution/OOM/GC | `new.object` |
| 150 | `THROW` | place `sO` in `context->thrownException` and enter handler lookup | explicit exception; current handler assumes a non-null throwable | `throw` |
| 151 | `INSTANCEOF` | test object against class symbol | resolution | `instanceof` |
| 152 | `CHECKCAST` | validate/copy reference against class | resolution/cast | `checkcast` |
| 153 | `CALL_normal` | resolve and call; extra argument slots | call/GC/exception | `call.direct` |
| 154 | `CALL_virtual` | receiver-class dispatch and cache | null/resolution/GC/exception | `call.virtual` |
| 155 | `JUMP_regI` | absolute method PC from integer register | validated PC | legacy `br.indirect` |
| 156 | `MONITOR_Enter` | enter monitor for `sO` | null/lock | `monitor.enter` |
| 157 | `MONITOR_Exit` | leave monitor for `sO` | null/monitor state | `monitor.exit` |
| 158 | `MONITOR_Enter2` | enter monitor for the constant-pool string object selected by the operand | null/lock | `monitor.enter.const-string` |
| 159 | `MONITOR_Exit2` | leave monitor for the constant-pool string object selected by the operand | null/monitor state | `monitor.exit.const-string` |

## Global effects relevant to compilers

- Reference loads/stores have no current write barrier because the GC is non-generational and non-moving. Compiled code must still expose every live reference at a collecting helper.
- Lazy binding mutates constant-pool and dispatch caches. IR models it as a runtime effect and cannot embed an unvalidated process pointer.
- The interpreter uses `context->thrownException` and handler lookup by TC PC. A backend publishes precise PC before every failing effect.
- An `aru` operation can be emitted only when the checked precondition is validated or dominated by explicit null/bounds checks.
- `JUMP_regI` supports legacy `jsr/ret`; it can be normalized only after all possible targets are enumerated and verified.
- Reference-array store handlers write the pointer directly and do not perform a dynamic array-store compatibility check. TCIR must preserve documented current behavior; any Java-semantics correction requires a separate compatibility change and tests.

## Uniform per-opcode metadata

Each numbered table row is read together with these uniform fields. The numeric value and name come from `opcodes.h`. Unless the row explicitly says it has extra slots, instruction size is one 4-byte slot. Runtime stack input/output is empty for every opcode because the VM is register-based; the operands column identifies typed register reads and writes that subsume Java locals and temporaries. The current implementation is the same-named `OPCODE(<name>)` handler in `executeMethod` in `tcvm.c`. Control-flow behavior is explicit in the branch/call/return rows. Exceptions and runtime interactions are in the check/runtime column and the global-effects section. GC interaction is “none directly” for pure register arithmetic and “spill live references before a `may_gc` runtime helper” for resolution, allocation, calls, exception creation, type helpers, and monitors.

Probable Java origins are grouped below. The converter may choose a compact or fused variant according to operand shape, so this is a semantic map rather than a one-to-one promise:

| TotalCross opcode set | Probable Java bytecode origin |
|---|---|
| `0 BREAK` | padding/debug/test behavior; no ordinary semantic Java operation |
| `1`–`8`, `20`–`23`, `44` | constants, `ldc`, local load/store materialization, `arraylength` |
| `9`–`19` | reference/64-bit constants, local moves, field/static/array loads |
| `24`–`43` | `putfield`, `putstatic`, and typed `*astore`/`*aload` |
| `45`–`88` | `iinc`, integer/long/float/double arithmetic, shifts, and bitwise bytecodes; fused variants are converter optimizations |
| `89`–`123` | `if*`, `if_icmp*`, `if_acmp*`, deferred `lcmp`/`fcmp*`/`dcmp*`, `goto`, loop peepholes, and explicit null checks |
| `124`–`132` | Java primitive conversion bytecodes |
| `133`–`144` | typed `*return` and `return`, with compact constant variants selected by the converter |
| `145 SWITCH` | `tableswitch` and `lookupswitch` |
| `146`–`149` | `newarray`, `anewarray`, `multianewarray`, and `new` |
| `150 THROW` | `athrow` |
| `151 INSTANCEOF`, `152 CHECKCAST` | `instanceof` and `checkcast` |
| `153 CALL_normal` | `invokestatic`, `invokespecial`, and `invokeinterface`; also lowered modern-Java constructs |
| `154 CALL_virtual` | `invokevirtual` |
| `155 JUMP_regI` | legacy `jsr`/`ret` conversion |
| `156`–`159` | `monitorenter`/`monitorexit` and converter-generated symbol/string monitor forms |

## Inventory discrepancy

The converter text-name array `TCConstants.bcTClassNames` ends before `MONITOR_Enter2` and `MONITOR_Exit2`, while the Java numeric constants, `opcodes.h`, and runtime dispatch include 158 and 159. Text names are therefore not sufficient evidence of coverage. The verification milestone must generate an opcode registry from one source of truth and fail on any count, name, or number mismatch.
