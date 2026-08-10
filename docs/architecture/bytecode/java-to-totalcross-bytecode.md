<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Java bytecode to TotalCross bytecode conversion

## Verified pipeline

The current pipeline is:

```text
Java .class
  -> JavaClass/JavaCode
  -> ByteCode[] and Java constant pool
  -> Bytecode2TCCode + virtual operand stack
  -> TotalCross Instruction[]
  -> CFG + register allocation
  -> 4-byte TCode slots
  -> TCClass + global constant pool
  -> compressed TCZ entries
```

`J2TC` coordinates conversion. For each method, `Bytecode2TCCode` walks Java bytecodes, simulates the operand stack, and produces register-oriented instructions. `CFG` builds blocks over those instructions; `RegAllocation` computes liveness and replaces virtual registers with typed physical registers; `generateCode` serializes the slots.

The Java package named `converter.ir` is an implementation-level intermediate
form, but it remains coupled to TotalCross opcodes, layouts, and compact-encoding
restrictions. It is not the backend-neutral TCIR now implemented after
TotalCross-bytecode lowering for interpretation, JIT, and AOT.

## Java input

`JavaClass` reads the constant pool, interfaces, fields, methods, and attributes. `JavaCode` reads `max_stack`, `max_locals`, code bytes, handlers, and `LineNumberTable`. The analyzed source permits class-file major version 70 (Java 26); preview classes with minor 65535 are rejected.

The reader recognizes modern metadata including bootstrap methods, nests, modules, records, and permitted subclasses. Recognizing an attribute does not imply that every associated runtime semantic is convertible.

## Register model

TotalCross bytecode has three physical banks:

- `regI`: 32-bit integer, boolean, byte, char, and short values;
- `regO`: managed references; and
- `reg64`: long and floating-point values.

The converter retains auxiliary types while translating, but the serialized format normalizes `float` into the double register bank. An IR built from TotalCross bytecode cannot claim to recover exact `float` versus `double` identity without additional metadata.

`OperandReg.init` reserves typed parameter and local registers. Java loads push operands that refer to those registers. Stores pop a simulated value and emit `MOV` only when a copy is needed. `POP`, `DUP`, and `SWAP` usually change only the converter's operand stack and generate no VM operation.

## Java-to-TotalCross mapping

The converter chooses compact forms according to operand shape, so several Java opcodes map to a family rather than one fixed TotalCross opcode.

| Java opcode(s) | Transformation | TotalCross opcode(s) | Runtime/helper | Restrictions/notes |
|---|---|---|---|---|
| `aconst_null`, `iconst_*`, `lconst_*`, `fconst_*`, `dconst_*`, `bipush`, `sipush`, `ldc*` | Create typed constant operands; materialize only when a consumer needs a register. | `MOV_regO_null`, `MOV_*_s18`, `MOV_*_sym`, or immediate/symbol consumer/return forms | global constant pool | `float` is normalized to the double bank; compact immediates are range-limited. |
| `iload`/`lload`/`fload`/`dload`/`aload` and short forms | Push an operand that names a typed local register. | Usually none | none | This is conversion-time stack behavior, not a runtime load. |
| typed `*store` and short forms | Pop a typed operand and assign the local's canonical register. | `MOV_regI_regI`, `MOV_regO_regO`, `MOV_reg64_reg64` when a copy is necessary | none | Register allocation may later coalesce the move. |
| `pop`, `pop2`, `dup*`, `swap` | Rearrange/remove conversion-time operands. | Usually none | none | Correct category-1/category-2 stack shape is a frontend invariant. |
| `iinc` | Update an integer local by a signed constant. | `INC_regI` | none | Serialized immediate is 16 bits. |
| `iadd/ladd/fadd/dadd` through arithmetic, remainder, shifts, and bitwise operations | Select typed register/immediate/symbol form; negation and some shapes lower to ordinary arithmetic; array peepholes may fuse access. | `ADD` through `XOR`, including fused `*_aru/arc_*` forms | division-by-zero exception path for integer/long div/rem; array checks for `arc` | Java overflow, shift masks, NaN, signed zero, and remainder need differential tests. |
| `i2*`, `l2*`, `f2*`, `d2*` and `i2b/i2c/i2s` | Convert between typed banks or narrow/extend an integer. | `CONV_*` | conversion helpers/macros in interpreter as applicable | Java NaN/out-of-range conversion and lost `f32` identity are observable concerns. |
| `lcmp`, `fcmpl/g`, `dcmpl/g` | Create a deferred comparison operand consumed by the following conditional branch where recognized. | Typed `JEQ/JNE/JLT/JLE/JGT/JGE` | none | NaN direction must match the specific Java compare opcode. |
| `ifeq`…`ifle`, `if_icmp*`, `if_acmp*`, `ifnull`, `ifnonnull`, `goto` | Materialize operands if necessary, assign symbolic target, then rewrite to slot displacement. | `JEQ`…`JGE`, `JUMP_s24`; selected loop peepholes | exception path only for forms that explicitly check objects/arrays | Target identity is converted after instruction expansion. |
| `jsr`, `jsr_w`, `ret` | Preserve a return TC PC in an integer register. | `JUMP_regI` plus associated moves/branches | none | Legacy control flow; compiling it requires enumerated targets. |
| `tableswitch`, `lookupswitch` | Normalize cases and destination identities into the TotalCross ordered representation. | `SWITCH` plus continuation slots | interpreter table search | PCs are TotalCross slots; payload must be structurally validated. |
| typed `*aload`, `*astore`, `arraylength` | Create array-access operands and choose checked/unchecked and element-width variants; selected sequences fuse. | `MOV_*_arc`, `MOV_*_aru`, `MOV_regI_arlen`, fused add/and forms | `ARRAYCHECK` for `arc`; direct array memory access | Current reference stores do not dynamically check array element class compatibility. |
| `newarray`, `anewarray`, `multianewarray` | Resolve element/class symbol, length registers, and dimensions. | `NEWARRAY_len`, `NEWARRAY_regI`, `NEWARRAY_multi` | `createArrayObject*`, OOM/GC path | Negative size and partial multiarray behavior require focused tests. |
| `new` | Resolve a class and create an uninitialized object for the Java constructor sequence. | `NEWOBJ` | `createObjectWithoutCallingDefaultConstructor`, OOM/GC | Constructor call is separate; `Object.<init>` may be eliminated. |
| `getfield`, `putfield`, `getstatic`, `putstatic` | Select instance/static and value-bank forms. | `MOV_*_field`, `MOV_field_*`, `MOV_*_static`, `MOV_static_*` | lazy class/field resolution, null/error paths | Compact instance-field symbols are 12-bit; static symbols use a wider layout. |
| `invokestatic`, `invokespecial`, `invokeinterface` | Build method symbol and compact argument continuation payload. | `CALL_normal` | normal/interface/native resolution and dispatcher | Despite Java interface semantics, the TotalCross opcode is the normal call form. |
| `invokevirtual` | Build call payload and retain virtual dispatch. | `CALL_virtual` | receiver-class lookup and small dispatch cache | Null receiver and signature/class resolution are runtime effects. |
| recognized `invokedynamic` patterns | Lower string concat, record object methods, or Java 8 lambdas into ordinary generated classes/calls/operations. | Resulting ordinary `CALL_*` and other opcodes | lowering-specific runtime code | Arbitrary bootstrap methods are rejected. |
| typed `*return` and `return` | Select bank and compact constant form if possible. | `RETURN_reg*`, `RETURN_s24*`, `RETURN_sym*`, `RETURN_null`, `RETURN_void` | frame/result transfer | Return destination is encoded by the caller's call header. |
| `athrow` | Pop or recover a reference register and emit explicit throw. | `THROW` | `context->thrownException`, stack trace, handler search | Current runtime handler assumes a non-null throwable; null behavior needs targeted investigation. |
| `checkcast`, `instanceof` | Resolve class symbol and retain object/destination registers. | `CHECKCAST`, `INSTANCEOF` | `areClassesCompatible`, class/cast exception path | Null is allowed by checkcast and false for instanceof. |
| `monitorenter`, `monitorexit` | Emit explicit monitor operation; converter-generated forms may use a constant-pool string object. | `MONITOR_Enter/Exit`, `MONITOR_Enter2/Exit2` | mutex or `totalcross.util.concurrent.Lock` helpers | Method-level `synchronized` is not thereby implemented. |
| `wide` | Parsed as the widened form of the following local/index instruction. | Resulting normal register operation | none | Final TotalCross register encodings still impose their own limits. |
| `breakpoint` and invalid/reserved opcodes | `breakpoint` is ignored in the analyzed converter path; invalid input reaches a hard error path. | none | conversion diagnostic/process path | Future validation should return structured errors rather than terminate the process. |

### Calls

`invokestatic`, `invokespecial`, and `invokeinterface` become `CALL_normal`; `invokevirtual` becomes `CALL_virtual`. A call has a header slot and continuation slots containing registers or compact constants. Encodings 0–63 identify registers; values starting at 64 identify a limited constant range. The receiver and return register use compact fields in the header.

Calls to `java.lang.Object.<init>()` can be eliminated. The VM resolves and stores lazy bindings in the loaded constant pool. Virtual calls maintain a small receiver-class cache.

### Control flow and merges

Before conversion, the pipeline gives Java targets symbolic identities. At a branch, `stackOfBranch` reconciles the operand stack and materializes values into canonical registers for the destination. After generation, `updateBranchs` converts identities to relative TotalCross slot displacements, may insert moves for compact forms, and updates switch tables and handler PCs.

This phase contains important merge semantics that must move into explicit blocks and values in the independent IR. It must not remain hidden in a side operand stack in the new frontend.

## Modern Java and `invokedynamic`

The pipeline has specific lowerings for:

- string concatenation;
- record methods synthesized through `ObjectMethods`; and
- Java 8 lambdas.

An `invokedynamic` outside known patterns produces a deterministic conversion error. There is no generic arbitrary-bootstrap implementation. Lambda deserialization methods and selected replaced artifacts can be removed during conversion.

## Compatibility boundary for optimized class files

J2TC accepts JVM-valid sparse line tables, including a first entry after bytecode
PC zero. Instructions before that entry retain line zero as the unknown-line
sentinel. It also resolves inherited Java declarations while preserving the
symbolic call owner and the selected TotalCross virtual or normal call opcode.

Exception handlers enter with the thrown object on the JVM operand stack. The
converter initializes that value even when the handler begins with an operation
such as `dup`; the older `astore` fast path remains an equivalent special case.

Optimization does not make every transformed Java ABI a supported TotalCross
replacement ABI. In particular, optimizer-created constructor descriptors such
as `Throwable(String, Throwable, byte)` and renamed members such as
`BiPredicate.test$...` or `Reader.read$...` are valid class-file shapes but are
not canonical TotalCross 4D contracts. The converter rejects them instead of
normalizing generated descriptors or allowlisting generated names. This keeps
replacement validation strict while distinguishing those intentional rejections
from converter bugs.

## Semantics requiring targeted tests

- `float` uses the same runtime bank as `double`; rounding and NaN need differential cases.
- `LCMP`, `FCMP*`, and `DCMP*` can become deferred comparisons; `fcmpl/fcmpg` and `dcmpl/dcmpg` NaN directions must be proven.
- Handler, line, and branch PCs are TotalCross slot PCs, not Java byte offsets.
- Methods carry a `synchronized` flag, but the converter reports method synchronization as unsupported; only explicit monitor opcodes are observable.
- The reader models Java opcodes through its recognized range, but one invalid-bytecode path still terminates the process instead of returning a structured error.
- Compact symbol/register ranges introduce materialization and limits that are not Java semantics.

## Requirements for the bytecode-to-IR frontend

The implemented TCIR frontend consumes a bounded view of `TMethod` metadata and
TotalCross slots rather than repeating `.class` conversion. `TMethod` itself
does not retain the serialized code-slot count, so `TCIRMethodView` makes that
bound and the required pool/debug metadata explicit. The frontend must:

1. decode instructions and every continuation slot;
2. reject a target in the middle of an instruction;
3. create blocks for entry, targets, fallthrough, and handlers;
4. represent typed registers as explicit values/slots;
5. turn lazy bindings into stable symbolic references in IR;
6. preserve TC PC and Java source line as metadata;
7. make null check, bounds check, division-by-zero, resolution, allocation, and call order explicit where observable;
8. model `context->thrownException` and exceptional edges; and
9. fail before compilation when an opcode or combination is not supported.

The independent frontend starts after Java compatibility has already been reduced to TotalCross semantics. Differential tests should therefore compare the TotalCross bytecode interpreter with the IR interpreter, while converter tests continue covering `.class -> TCZ` behavior.

`TCIRConverterFixtureTest` began with `add`, `abs`, and `sumTo` and now generates
the 15 representative fixtures through the production `J2TC` path, comparing
exact emitted TCode words and source lines with the native fixture header. The
native differential harness separately supplies legacy-interpreter, TCIR,
SLJIT, and generated-C equivalence evidence for eligible paths.

## Preservation rule

In the first evolution, current bytecode remains the canonical on-disk format and the interpreted path remains the reference. JIT/AOT may select a method only after complete decoding, verification, and compilation. A backend must never execute part of a native method and restart it in the interpreter because observable effects would be duplicated.
