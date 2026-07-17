<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# The `executeMethod` interpreter

## Current role

`executeMethod(Context, Method, ...)` in `TotalCrossVM/src/tcvm/tcvm.c` is the reference executor for TotalCross bytecode. It combines dispatch, frame setup, parameter passing, lazy resolution, native calls, virtual dispatch, exceptions, and returns. The first IR/JIT/AOT version must preserve this path and use it as the differential oracle.

## Execution state

`Context` owns contiguous arenas for three register banks:

- `regIStart..regI..regIEnd` for 32-bit integers;
- `regOStart..regO..regOEnd` for references; and
- `reg64Start..reg64..reg64End` for long/double values.

It also contains the call stack, pending exception, `NMParams`, thread object, and a usage lock. An interpreted frame reserves `method->iCount`, `oCount`, and `v64Count` elements by advancing the current pointers. The new reference region is cleared before becoming visible to the GC. The call stack uses two positions per call for the method and caller return/code state.

There is no JVM operand stack at runtime and no operational distinction between a local and a temporary; the converter has allocated both to typed registers.

## Entry and parameters

The public API is variadic. For an instance method, the first object is `this`, and null produces `NullPointerException`. `method->paramRegs` identifies the bank for each argument. `context->parametersInArray` provides an internal `TValue` array path.

Native methods use `NMParams`, with separate integer, object, long, and double arrays and typed return slots. Native functions are resolved by name/signature from the runtime or a loaded library.

`usageLock` prevents two threads from executing the same `Context` concurrently. A compiled entry must preserve that rule and must not reacquire the lock when called internally by an already-owned dispatcher.

## Dispatch

Where supported, `DIRECT_JUMP` builds a label table and uses computed goto. The analyzed configuration enables it for selected GCC builds while excluding modern Android; `TRACK_USED_OPCODES` forces switch dispatch. Other builds use the switch/macro path.

The PC is a `TCode` pointer. Most handlers finish with `NEXT_OP`; calls, branches, returns, and exceptions change flow explicitly. In production `BREAK` acts as a NOP; in the native test suite it ends the method. In switch dispatch an invalid opcode can fall through to method termination, so the new frontend must verify all code before native execution.

```text
reserve/clear typed frame
          |
          v
fetch TCode at code pointer
          |
          v
computed-goto or switch dispatch
          |
          +--> pure register operation ---------+
          +--> branch/switch changes code ------+
          +--> call changes method/frame -------+--> next fetch
          +--> helper sets thrownException -----+
          `--> return/unhandled exception -> unwind/finish
```

The fast paths are direct typed-register arithmetic, moves, already-bound fields/methods, and unchecked array forms. Slow paths resolve symbols/classes/methods, grow context arenas, allocate, construct exceptions, dispatch virtual/interface calls, enter runtime/native code, and operate monitors. Macros such as `ARRAYCHECK`, `GET_STATIC_FIELD`, `GET_INSTANCE_FIELD`, `NEXT_OP`, `FIRST_OP`, `OPCODE`, and the computed-goto selection macros hide control transfers and must be included in semantic review; an opcode case alone is not the whole behavior.

## Interpreted calls

The `CALL` header references a method and encodes receiver/return information; following slots contain compact arguments. The interpreter:

1. resolves the normal or virtual target and updates lazy caches;
2. validates or grows the context arenas;
3. saves method and return PC on the call stack;
4. changes `method`, `class_`, `cp`, `code`, and register bases; and
5. continues in the same C loop without C recursion for an interpreted callee.

Virtual calls use the concrete receiver class and a small method/class cache. The result is written to the caller register encoded by the call header. A native function returns through `NMParams` and rejoins the same protocol.

A backend cannot assume a lazy binding is stable across processes, AOT files, or future unloading. IR must keep symbolic identity and use a resolution cell/helper with defined concurrency and lifetime.

## Branches and switch

Branches use displacements relative to the current slot. `JUMP_regI`, used for legacy `jsr/ret`, treats an integer register as an absolute method code index. `SWITCH` searches an ordered table and applies relative displacements. Targets must land on logical instruction starts; the current runtime trusts the converter, but the new verifier must not trust external bytes.

## Exceptions

There is no C++ or machine unwinding. Helpers place an object in `context->thrownException` and transfer control to `handleException`. The interpreter:

1. searches the current method for a handler whose range includes the failing TC PC and whose class accepts the exception;
2. moves the exception to the handler's designated `regO`, clears pending state, and resumes at the handler PC; or
3. removes the frame like a void return and repeats the search in the caller.

Null, bounds, division-by-zero, missing class/field/method, cast, OOM, and explicit throw converge on this protocol. Precise TC PC is observable through handler selection, stack traces, and source lines.

The recommended compiled protocol is explicit: after a helper, code checks `context->thrownException`; an exception status returns control to the dispatcher, which performs the same handler search. The POC should not use longjmp or backend-specific unwinding.

## Monitors

`MONITOR_Enter` and `MONITOR_Exit` operate on a reference; `Enter2`/`Exit2` use an object resolved from a symbol. The runtime calls mutex/lock helpers. The method `synchronized` flag exists, but automatic locking code is commented out, matching the converter warning that synchronized methods are unsupported.

A backend must preserve pairing, exception order, and visibility. It must not add synchronized-method behavior as a side effect of this work.

## GC and safe points

The loop has no safepoint opcode or periodic poll. Allocation and other runtime helpers may reach collection. Interpreted references remain in the `Context.regO` arenas scanned by the GC.

For the compiled POC, native frames should reserve and use the same `regO` region. Before every helper that may collect, every live reference must be in that region; a value only in a CPU register or C local is not a root. This avoids machine stack maps initially. Keeping references elsewhere later requires an integrated shadow stack or stack maps.

## Recommended integration point

The dispatcher keeps `executeMethod` as its facade. After frame validation/reservation and before the first opcode, it consults a side table keyed by `Method`:

```text
unseen -> verifying -> compilable -> compiling -> ready
                         |              |
                         v              v
                    interpreter       failed
```

Only `ready` invokes compiled code. `failed` records a bounded reason and stays interpreted. The POC compiles by explicit option or controlled first use rather than introducing hotness policy immediately.

An entry returns normal, pending-exception, or pre-execution-fallback status. Verification guarantees that fallback is never requested after the first effect. A call to an uncompiled method can use a dispatcher thunk.

## Equivalence invariants

- The same frames and objects are visible to the GC at every operation capable of collecting.
- Returns reach the bank/register encoded by the caller.
- A pending exception prevents execution of the next normal operation.
- TC PC is published before every effect that can throw or call.
- Lazy binding and virtual dispatch preserve class, signature, and lock behavior.
- Null check, bounds check, resolution, and store order are semantic.
- A backend never retains arena base pointers across a helper that may call `contextIncreaseReg*`.

## Responsibility boundaries

| Responsibility | Current owner | Examples |
|---|---|---|
| bytecode format | converter serializers and `tcclass.h` layouts | opcode/operand bits, continuation slots, register counts, handler and line PCs |
| interpreter | `executeMethod` and dispatch macros | fetch/decode, register effects, frame switching, branch/switch, result transfer |
| linker/class runtime | `tcclass.c`, `tcfield.c`, `tcmethod.c`, constant-pool caches | class loading, static initialization, lazy field/method resolution, virtual/interface target selection |
| object system | class layouts and object/array helpers | field offsets, object/array allocation, type compatibility, locks |
| GC | `objectmemorymanager.c` plus root-registration users | allocation pressure, root scan, mark/revive/free, finalization, weak references |
| native bridge | native lookup and `NMParams` paths | argument-bank conversion, native symbol lookup, typed return, pending exceptions |

Compiled code may implement an interpreter responsibility directly only when TCIR specifies it and conformance tests prove equivalence. Linker, object-system, GC, and native-bridge responsibilities initially remain runtime helpers. This boundary prevents a backend from copying private layout or cache behavior that is not yet an ABI.

## Minimum future diagnostics

When enabled by a build flag, the dispatcher should record structured events only: method, backend, fallback reason, verification/compilation time, and code size. It must not print tokens, private URLs, sensitive arguments, or per-opcode logs by default.
