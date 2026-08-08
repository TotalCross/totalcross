<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# TotalCross independent IR design

## Status and intent

Version 1 of the backend-neutral TCIR contract is implemented under
`TotalCrossVM/src/tcvm/ir`. It provides owned construction APIs, opaque public
structures, structural and type verification, a canonical text printer, a
complete TotalCross-opcode disposition registry, bounded TotalCross-bytecode
decoding, a reference interpreter, SLJIT and deterministic-C backends, and a
separately default-off runtime adapter. Representative i32, i64, normalized-f64,
reference, switch, pre-bound static-call, and object-allocation families validate
the architecture without changing the on-disk TCZ/class/bytecode format,
`TMethod` layout, or semantic authority of the bytecode interpreter. Exact
supported and fallback operations are recorded in `compatibility-matrix.md` and
`TotalCrossVM/src/tcvm/ir/tcir_opcode_registry.def`.

The Java package currently named `tc.tools.converter.ir` is not this IR. Its instruction classes mirror TotalCross opcodes and bit layouts, and register allocation mutates those target-shaped instructions before serialization. Some control-flow and liveness algorithms may be reusable after decoupling, but the model itself must not become the JIT/AOT contract.

TCIR is intentionally positioned after Java-to-TotalCross lowering. It is a
valid backend/runtime IR for TotalCross bytecode and native execution, but it
does not retain every Java-level fact needed by a possible future whole-program
optimizer. Any Java-aware high-level IR is a separate future investigation, not
an unimplemented portion of TCIR version 1.

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

A `TCIRModule` owns versioned `TCIRSymbol` identities and one or more `TCIRFunction` objects. A function contains a signature, typed virtual register homes, basic blocks, metadata, and an ordered instruction list per block. Public declarations are in `TotalCrossVM/src/tcvm/ir/tcir.h`; concrete layouts remain private in `tcir_internal.h`. A module also owns every function, block, value, operation, edge copy, and symbol created through it. `tcirModuleDestroy` releases the graph, while text returned by `tcirFunctionDump` is explicitly released with `tcirFreeText`.

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
| `internal_address` | temporary interior/native address restricted to one no-safepoint region |

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

The semantic operation roadmap is encoding-independent:

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

The public enum began with the operations needed to establish the contract and
its hard invariants. It now also includes the implemented i32/i64/f64 arithmetic
and comparisons, stable conversions, reference identity, object allocation, and
method-call operations used by the representative coverage. Array and field
operations remain in the model as verified contract shapes but are not general
frontend/backend coverage. Every new operation must still extend the enum,
verifier, interpreter, backend eligibility, and tests together.

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

The canonical text format is versioned, deterministic, and intended for tests, not as a new shipping artifact. Functions and symbols use quoted stable identities, values use construction-stable numeric identifiers, blocks are printed in numeric order, effects have a fixed order, and memory addresses never appear. The committed `sumTo` fixture is:

```text
tcir 1
func @"Example.sumTo:(I)I"(%v0: i32) -> i32
  homes i32 4, ref 0, v64 0
bb0() ; tcpc=0 line=30
  %v8 = const.i32 0 ; tcpc=0
  br bb1(%v8, %v8, %v0) ; tcpc=1
bb1(%v1: i32, %v2: i32, %v3: i32) ; tcpc=2 line=31
  %v9 = cmp.ge.s.i32 %v1, %v3 -> i1 ; tcpc=2
  br_if %v9, bb3(%v2), bb2(%v1, %v2, %v3) ; tcpc=4
bb2(%v4: i32, %v5: i32, %v6: i32) ; tcpc=5 line=32
  %v10 = add.i32 %v5, %v4 -> i32 ; tcpc=5
  %v11 = const.i32 1 ; tcpc=6
  %v12 = add.i32 %v4, %v11 -> i32 ; tcpc=7
  br bb1(%v12, %v10, %v6) ; tcpc=8
bb3(%v7: i32) ; tcpc=9 line=33
  ret %v7 ; tcpc=9
end
```

Names derived from memory addresses, hash-table iteration order, or thread timing are forbidden. Dumps include the IR version, function signature, homes, block order, TC PC, optional source line, effects, GC-home declarations, exceptional destinations, and explicit successors. The printer is deliberately one-way in this milestone; no text parser or shipping/cache format is defined.

## Frontend algorithm

The bytecode-to-IR frontend performs these phases for its bounded supported
subset:

1. decode slots into logical instructions and mark continuation slots;
2. validate structural widths, POC operands, symbols, register banks, branch/switch targets, handler ranges, and parameter homes;
3. reject or return method-atomic fallback before creating TCIR;
4. discover block leaders from entry, supported branch targets, and fallthrough;
5. precreate deterministic blocks and typed-home block arguments;
6. lower register state into immutable operations and explicit successor edges while preserving TC PC/source line;
7. run the canonical verifier; and
8. optionally emit stable text for diagnostics and golden comparison.

A failure returns a structured reason and leaves the method eligible for the existing interpreter. It must not terminate the process.

The implementation accepts a non-owning `TCIRMethodView`, not a bare runtime `TMethod`, because the current loader does not retain the serialized code-slot count in `TMethod`. The view makes code bounds, home counts, parameter-home mapping, i32 constants, source lines, handlers, and call-shape resolution explicit. This is a frontend safety boundary rather than a new shipping format or runtime ABI. Milestone 7 runtime registration deliberately requires the same explicit bounded view; a general class-loader/TCZ adapter must retain or reconstruct authoritative bounds before automatic registration can exist.

The original static-integer frontend gave every non-entry block the declared
i32 homes as deterministic block arguments. Later slices extended the same
construction discipline to i64, normalized-f64, and managed-reference values.
Entry parameters seed explicit homes and converter instructions define local
homes before use. This intentionally favors simple verifiable construction over
liveness-minimal argument lists. Complete handler-bearing methods remain
fallback.

## Verifier invariants

`tcirVerifyFunction` implements the structural contract before any execution backend exists. It returns a stable `TCIRDiagnosticCode` and records the function identity and originating TC PC on every failure. The builder copies caller-owned operand, edge, and GC-home arrays, but intentionally permits several malformed graphs so the canonical verifier remains the single rejection boundary tested by negative fixtures.

- Every block is reachable, has one terminator, and agrees with predecessor argument types.
- Every value is defined before use and belongs to the same function.
- Register index and symbol identity match the opcode/IR type.
- All branch, switch, and handler targets are logical instruction boundaries.
- A `ref!` is dominated by a valid non-null proof.
- Unchecked array memory access is dominated by compatible null/bounds checks or preserves a validated bytecode precondition.
- Every helper's declared effects match its runtime ABI entry.
- Live references have GC-visible homes at `may_gc` operations.
- A throwing operation has a valid handler edge or propagates to the dispatcher.
- No internal address crosses a safepoint.
- Return type matches the declared `TCIRFunction` signature; the Milestone 3 frontend must additionally prove parameter mapping against `TMethod`.

The current negative suite exercises foreign/undefined values, mismatched block arguments, an invalid conditional terminator, a wrong return type, a missing GC-visible home, a source PC into a continuation slot, mismatched helper effects, an incompatible unchecked-array proof, an internal address live across `may_gc`, and a `ref!` result without a non-null proof. Handler signatures and exceptional destinations are checked by the same verifier even though the three valid integer fixtures do not need handlers yet.

Verification is mandatory in debug and release builds for untrusted artifacts. A build may cache a successful result, but the cache key includes bytecode identity, IR version, runtime ABI version, target, and relevant feature flags.

## Opcode registry

`TotalCrossVM/src/tcvm/ir/tcir_opcode_registry.def` is the macro-driven registry for all numeric TotalCross opcodes 0–159. Each row records its decoder shape (`single`, `call`, `switch`, or `multiarray`), planned lowering class, and current POC status. `tcir_opcode_map.c` compiles each row against the matching constant in `opcodes.h`; `tcirOpcodeRegistryValidate` rejects gaps, duplicates, invalid enum values, and count drift.

`scripts/validate-tcir-opcodes.py` supplies the repository-wide cross-check. It compares the registry with `opcodes.h`, the numeric Java constants in `TCConstants.java`, every `OPCODE(...)` dispatch in `tcvm.c`, the bytecode reference, and the compatibility matrix. It reports the known `bcTClassNames` omission of opcodes 158 and 159 as an explicit discrepancy rather than treating that incomplete text array as the numeric authority.

## IR interpreter

`tcir_interp.c` executes verified functions directly over immutable values, block arguments, explicit edges, and the three typed home banks. `TCIRInterpreterFrame` mirrors the `Context` integer, reference, and 64-bit arenas without making the standalone TCIR library depend on VM-private structures. Milestone 7's optional `tcir_runtime.c` adapter points those banks at an already reserved real `Context` frame. Function arguments are explicit because frontend parameter values need not be reloaded from homes.

Every public invocation runs the canonical verifier and a complete interpreter-eligibility scan before changing the frame. Malformed graphs, undersized frames, and operations outside the stable direct subset are rejected without partial execution. Debug builds additionally assert the successful verifier result, while release builds retain the mandatory checked path. Execution has a configurable step bound and structured `returned`, `thrown`, `rejected`, `step-limit`, and allocation-failure outcomes with the last TC PC. Integer add, subtract, and multiply use explicit 32-bit modular arithmetic instead of relying on signed C overflow.

The current interpreter subset covers the implemented i32/i64/f64 and reference
value operations, typed-home traffic, branches, switches, returns, checked
division/null failures, pre-bound static calls, and object allocation through
explicit callbacks. General field/array access, class initialization,
`THROW`/handlers, virtual or unresolved calls, monitors, and remaining
helper-bearing operations remain ineligible. The interpreter's purpose is
semantic isolation and differential testing, not production speed. The
comparison tiers are:

```text
TotalCross bytecode interpreter
          vs
IR interpreter
          vs
SLJIT baseline / generated-C AOT
```

Adding a new operation requires verifier rules, canonical golden-dump coverage, IR-interpreter semantics, backend eligibility behavior, and compatibility-matrix updates.

The original Milestone 4 host harness built the production-converter `add`,
`abs`, and `sumTo` slots into both a minimal real `TMethod` for `executeMethod`
and a verified `TCIRFunction`, producing 1,179 comparisons. The final
architectural corpus contains 15 converter-backed fixtures and 6,398
fresh-state legacy/TCIR/SLJIT/AOT comparisons for eligible paths, plus 16
separate TCIR/SLJIT/AOT allocation-contract comparisons. Each invocation uses
independent frame storage and compares outcome, return type/value, stable
exception details when available, and frame restoration. The allocation harness
uses opaque tokens and does not claim real class-loader/object-memory-manager or
forced-GC equivalence; full handlers, heap families, and other unsupported
effects remain future cases rather than inferred coverage.

## Runtime registration and dispatch

`tcir_runtime.h` is the experimental VM-facing boundary. It provides explicit whole-method registration, backend policy (`off`, `ir`, `jit`, `aot`, `auto`), forced-method selection for tests, structured diagnostics/fallback reasons, statistics, explicit IR dumps, and lifecycle control. Its owned side table is keyed by `Method`; it owns each module/function and optional lazy JIT artifact while leaving `TMethod` untouched. Registration rejection never publishes a partial entry.

With root CMake option `TC_ENABLE_COMPILED_DISPATCH=ON`, `executeMethod` consults this table after top-level frame setup and after nested normal-call frame preparation. The initial backend state is `off`; a platform atomic makes that state a no-lock/no-statistics fast path. Enabling a backend allows compatible registered methods to run through the existing typed homes. A version-2 compiled frame carries an opaque runtime dispatch thunk, letting a generated entry re-enter `executeMethod` for interpreted, compiled, or native targets. The test-only canonical registration remains explicit and is not a production class-loader publication mechanism.

The runtime test proves default-off bypass, IR/JIT/AOT policy, forced fallback,
exact AOT selection, lazy-JIT single publication under eight callers,
interpreter-to-compiled nested calls, compiled thunk calls to
interpreted/compiled/native targets, primitive results, pending-exception
status/TC-PC handoff, frame/usage restoration, shutdown during an active AOT
call, and explicit IR output. Later representative fixtures add reference
returns, pre-bound static `CALL_normal`, and `NEWOBJ` through runtime ABI version
5. They do not promote handlers, arbitrary lazy/virtual calls, forced/moving GC,
arena growth, or stack traces to supported coverage.

## Optimization policy

Version 1 permits only obviously semantics-preserving local passes after differential coverage exists: constant folding with exact Java/IEEE rules, copy propagation, dead pure operations, branch simplification, and bounds-check reuse with dominance proof. Passes are individually toggleable and dump before/after IR. No pass may reorder `may_throw`, `may_gc`, locks, volatile-like runtime operations, or symbol resolution without a documented memory/effect model.

## Versioning and extensibility

`TC_IR_VERSION` is currently `1U` in `tcir.h`. `TC_RUNTIME_ABI_VERSION` is a
separate contract and is currently `5U` in `tcir_compiled.h`; versions 2–5 added
opaque dispatch, mixed typed-value storage, pre-bound call, and allocation
thunks. Backends consume read-only accessors that enumerate operations, types,
effects, blocks, symbols, terminators, and metadata rather than concrete struct
layouts. An LLVM or Cranelift backend becomes viable only after:

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
