<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# TotalCross independent IR design

## Status and intent

This document proposes a backend-neutral intermediate representation between TotalCross bytecode and execution engines. It is not implemented yet. The on-disk TCZ/class/bytecode format remains unchanged, and the interpreter remains the semantic reference.

The Java package currently named `tc.tools.converter.ir` is not this IR. Its instruction classes mirror TotalCross opcodes and bit layouts, and register allocation mutates those target-shaped instructions before serialization. Some control-flow and liveness algorithms may be reusable after decoupling, but the model itself must not become the JIT/AOT contract.

## Design goals

- Represent TotalCross runtime semantics without depending on a particular native ISA or code generator.
- Make control flow, types, values, side effects, exceptional edges, and GC points explicit and verifiable.
- Preserve enough source identity to report a TotalCross PC and Java source line.
- Support an IR interpreter, an SLJIT baseline backend, a generated-C AOT backend, and later optimizing backends.
- Produce stable, deterministic text dumps for tests and diagnostics.
- Reject malformed or unsupported methods before executing compiled code.
- Allow mixed interpreted/compiled calls without changing TCZ artifacts.

## Non-goals for the first implementation

- Replacing the Java-to-TotalCross converter.
- Changing the 4-byte TotalCross bytecode encoding.
- Whole-program optimization, speculative inlining, deoptimization, or OSR.
- Recovering Java `float` precision metadata that the serialized bytecode has normalized into the 64-bit floating register bank.
- Supporting every opcode in the first backend proof of concept.

## Core model

An `IRModule` owns versioned symbol identities and one or more `IRFunction` objects. A function contains a signature, typed virtual register homes, basic blocks, metadata, and an ordered instruction list per block.

The initial representation uses a pragmatic hybrid:

- operation results are immutable typed virtual values;
- basic-block arguments represent values merged from predecessors;
- the three mutable TotalCross register banks are modeled as explicit typed slots at the bytecode boundary; and
- a construction pass promotes safe slot traffic to values and inserts block arguments.

This is simplified SSA without requiring a full optimizing SSA framework. It makes merges explicit while retaining a direct fallback representation for legacy indirect control flow and exception handlers. A verifier runs both before and after promotion.

### Alternatives considered

| Model | Fit with current input | Benefits | Problems | Decision |
|---|---|---|---|---|
| stack IR | poor: TotalCross bytecode has already eliminated the Java runtime stack | compact expression reconstruction | would invent stack state, obscure typed homes, and duplicate converter work | rejected |
| mutable register IR | direct mechanical decoding | simple frontend and interpreter | target-shaped copies, implicit merges, weaker optimization/verification boundary | retain only as transient construction form |
| full SSA immediately | strong analysis and optimization | explicit data flow and mature algorithms | exception/legacy control and mutable runtime homes complicate initial construction | defer full canonical SSA |
| non-SSA basic blocks with virtual values | easy incremental construction | explicit CFG without phi construction | merges and definition identity remain less precise | used as pre-promotion form |
| simplified SSA with block arguments plus explicit homes | matches register bytecode and GC frame requirements | deterministic merges, typed immutable results, backend neutrality | requires promotion and home-spill rules | selected canonical form |
| separate high-level and low-level IRs | can isolate runtime semantics from target details | future optimizer/backend flexibility | doubles versioning, verification, dumps, and lowering work before need is proven | one semantic TCIR initially; backend-local machine IR stays private |

The selected model does not prohibit a later high-level-to-low-level split. Such a split is justified only when at least two backends need the same lowering that cannot be expressed cleanly as a TCIR operation or pass.

## Types

The required first-version types are:

| Type | Meaning |
|---|---|
| `void` | no value |
| `i1` | internal condition; never a serialized TotalCross register |
| `i8`, `i16`, `i32` | integer widths with Java conversion semantics where annotated |
| `i64` | Java long |
| `f64` | TotalCross floating value; includes Java float values normalized by the converter |
| `ref` | nullable managed object reference |
| `ref!` | verifier-proven non-null managed reference inside a dominated region |
| `token` | ordered runtime effect, used only if needed to prevent illegal motion |

Array element and class identities are metadata on `ref`, not native pointers. An internal address type may exist only within a no-safepoint region and may never be stored, returned, merged across a safepoint, or exposed in text as a stable ABI.

## Control flow

Every block has a stable numeric label and zero or more typed block arguments. Every block ends in exactly one terminator:

- `br block(args...)`;
- `br_if condition, true(args...), false(args...)`;
- `switch key, default(args...), [case: block(args...)]`;
- `ret`, `ret value`;
- `throw value`; or
- `unreachable`.

Handler blocks are explicitly marked and receive the thrown `ref` as an argument. Instructions that may throw carry the originating TC PC and an exceptional successor or a `propagate` marker. The initial lowering may route all failures through a shared runtime check, but the IR must retain the logical exceptional edge.

`JUMP_regI` is accepted only when the frontend can enumerate its possible targets from the legacy `jsr/ret` pattern. Otherwise, the method is not compilable and remains interpreted.

## Operation families

The initial operation set is semantic rather than encoding-shaped:

- constants, `copy`, and typed conversions;
- integer and floating arithmetic, shifts, and bitwise operations;
- comparisons returning `i1`;
- `load.slot` and `store.slot` for TC register homes;
- `null.check`, `bounds.check`, `array.length`, array load/store;
- symbolic static/instance field load/store;
- `new.object`, `new.array`, and `new.multiarray`;
- `instanceof` and `checkcast`;
- direct, virtual, native, and dispatcher calls;
- monitor enter/exit;
- explicit `safepoint`/`runtime.call` annotations; and
- the terminators above.

Checked TotalCross array opcodes lower to explicit checks followed by an unchecked memory operation. Unchecked opcodes require a dominating proof or retain an `assume.checked` precondition that the verifier can trace back to the original bytecode contract. A backend must never silently drop a required check.

## Symbols and runtime ABI

IR symbols are stable tuples, not loaded pointers. A tuple contains kind, owning class identity, name/type/signature identity, and the originating constant-pool index when available. Resolution lowering uses runtime cells or helpers and may cache a pointer only with the VM's publication and lifetime rules.

Runtime operations declare effects:

```text
pure
reads_heap
writes_heap
may_throw
may_gc
may_lock
resolves_symbol
calls_unknown
```

Effect declarations are part of verification and backend conformance. In particular, every `may_gc` operation requires all live `ref` values to have visible homes in the current `Context.regO` frame in the baseline design.

## Text form

The canonical text format is versioned, deterministic, and intended for tests, not as a new shipping artifact. Example:

```text
tcir 1
func @Example.sum(%this: ref, %n: i32) -> i32
  homes i32 4, ref 1, v64 0
bb0:                                      ; tcpc=0 line=12
  %zero = const.i32 0
  store.slot.i32 1, %zero
  br bb1(%zero, %zero)
bb1(%i: i32, %acc: i32):                 ; tcpc=2
  %done = cmp.ge.s.i32 %i, %n
  br_if %done, bb3(%acc), bb2(%i, %acc)
bb2(%old_i: i32, %old_acc: i32):         ; tcpc=5
  %next_acc = add.i32 %old_acc, %old_i
  %one = const.i32 1
  %next_i = add.i32 %old_i, %one
  br bb1(%next_i, %next_acc)
bb3(%result: i32):                        ; tcpc=9
  ret %result
end
```

Names derived from memory addresses, hash-table iteration order, or thread timing are forbidden. Constant/symbol tables are sorted by stable identity. Dumps include the IR version, function signature, homes, block order, TC PC, optional source line, effects, and explicit successors.

## Frontend algorithm

The bytecode-to-IR frontend performs these phases:

1. decode slots into logical instructions and mark continuation slots;
2. validate opcode, operand ranges, symbols, register banks, call payloads, switch tables, handler ranges, and line data;
3. discover block leaders from entry, branch/switch targets, fallthrough, handlers, and legacy indirect targets;
4. emit slot-form IR with explicit checks/effects and TC PC metadata;
5. compute predecessor/successor and exceptional edges;
6. propagate types and definedness through every edge;
7. promote slot values where safe and create block arguments;
8. run the canonical verifier; and
9. optionally emit stable text and an execution eligibility report.

A failure returns a structured reason and leaves the method eligible for the existing interpreter. It must not terminate the process.

## Verifier invariants

- Every block is reachable or explicitly retained for diagnostics, has one terminator, and agrees with predecessor argument types.
- Every value is defined before use and belongs to the same function.
- Register index and symbol identity match the opcode/IR type.
- All branch, switch, and handler targets are logical instruction boundaries.
- A `ref!` is dominated by a valid non-null proof.
- Unchecked array memory access is dominated by compatible null/bounds checks or preserves a validated bytecode precondition.
- Every helper's declared effects match its runtime ABI entry.
- Live references have GC-visible homes at `may_gc` operations.
- A throwing operation has a valid handler edge or propagates to the dispatcher.
- No internal address crosses a safepoint.
- Return type and parameter mapping match `TMethod`.

Verification is mandatory in debug and release builds for untrusted artifacts. A build may cache a successful result, but the cache key includes bytecode identity, IR version, runtime ABI version, target, and relevant feature flags.

## IR interpreter

Before native code generation, a small IR interpreter executes the same `IRFunction` using the current `Context` arenas and runtime helpers. Its purpose is semantic isolation and differential testing, not production speed. The comparison tiers are:

```text
TotalCross bytecode interpreter
          vs
IR interpreter
          vs
SLJIT baseline / generated-C AOT
```

Adding a new operation requires verifier rules, text round-trip/golden coverage, IR-interpreter semantics, backend eligibility behavior, and compatibility-matrix updates.

## Optimization policy

Version 1 permits only obviously semantics-preserving local passes after differential coverage exists: constant folding with exact Java/IEEE rules, copy propagation, dead pure operations, branch simplification, and bounds-check reuse with dominance proof. Passes are individually toggleable and dump before/after IR. No pass may reorder `may_throw`, `may_gc`, locks, volatile-like runtime operations, or symbol resolution without a documented memory/effect model.

## Versioning and extensibility

`TC_IR_VERSION` and `TC_RUNTIME_ABI_VERSION` are independent. Backends consume an interface that enumerates operations, types, effects, blocks, symbols, and metadata rather than concrete struct layouts. An LLVM or Cranelift backend becomes viable only after:

- the semantic test matrix covers all opcodes selected for that backend;
- runtime helper ABI and GC-root protocol are stable;
- exception and debug-PC mapping are specified;
- generated code ownership/invalidation is defined; and
- the backend can be optional without changing the TCZ format or interpreter behavior.

## Decisions still requiring evidence

- Whether legacy `JUMP_regI` patterns can always be normalized from shipped applications.
- Whether volatile/atomic field semantics exist outside the analyzed opcode names and helpers.
- Which runtime helpers are transitively `may_gc` on every platform.
- Whether a future IR should preserve a distinct `f32` by extending converter metadata.
- Whether the text form should eventually become a cache format; it is explicitly not one in version 1.
