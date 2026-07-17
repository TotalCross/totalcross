<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Memory management and the GC contract

## Current model

The manager in `objectmemorymanager.c` uses free/used lists organized by size class. The analyzed code implements a non-moving collector: live objects keep their addresses. Adjacent free blocks may be merged, but live objects are not compacted.

Each object is preceded by `TObjectProperties`, containing class, list links, size, lock, and mark bits. The exposed object pointer addresses the data after this prefix. Arrays store length in the first data slot and elements after it.

A newly allocated object starts locked so it cannot be collected before the caller publishes it in a root; callers normally unlock it afterward. This transient lock must not become a general substitute for correct rooting.

## Collection

At a high level, the collector:

1. moves used objects into free-candidate lists;
2. walks roots and marks/revives reachable objects into used lists;
3. processes finalization and weak references;
4. frees unreachable objects; and
5. coalesces adjacent free blocks.

The meaning of the mark bit alternates between cycles to avoid a full clearing pass. Global OMM locks/flags coordinate collection and finalization.

Finalizers run through a dedicated GC context and call `executeMethod`. Allocation by the GC context itself is rejected. A compiled backend must not introduce hidden allocation in that path.

## Observed roots

The current code marks:

- reference static fields of loaded classes;
- explicitly locked objects, including constant-pool strings;
- for each `Context`, `threadObj`, `nmp.retO`, every slot in `regOStart..regO`, and `thrownException`;
- transitive references in object arrays and object fields; and
- other roots explicitly registered by current subsystems.

`WeakReference` is special: the referent is not retained solely by the weak reference and may be cleared.

The GC does not conservatively scan the C stack or CPU registers. This is the central JIT/AOT constraint.

## Layout and references

The loader groups instance fields by storage: 32-bit, reference, then 64-bit. The scanner uses class metadata and those offsets/counts to locate references. A backend may calculate addresses against this ABI only while the class/layout remains valid; IR should preserve a field symbol and let runtime resolution/cache determine offsets.

No read or write barrier was found in the analyzed path. That is consistent with a non-generational, non-moving heap, but it is a current property rather than a permanent ABI promise. Reference stores must remain identifiable in IR so a future barrier can be added without redesigning every backend.

## Initial contract for compiled code

The POC reuses `Context` arenas as the canonical frame:

- every `ref` value has a home slot in `regO`;
- before a call, allocation, resolution, or other potentially collecting helper, every live reference is written to its home;
- dead slots are cleared when a frame is created/reused so stale values neither retain objects nor resemble pointers;
- after a helper that may grow arenas, base pointers are reloaded from `Context`; and
- precise TC PC is published before the helper.

Integer and 64-bit values may stay in native registers if exception/return ABI is preserved. Interior object addresses cannot cross a safepoint; generated code retains the base reference and recomputes the address.

This strategy limits register-allocation freedom but establishes correctness before machine stack maps.

## Safepoints

The interpreter has no explicit poll. Mandatory baseline safepoints are:

- object or array allocation;
- interpreted, compiled, or native calls;
- lazy class, field, or method resolution;
- monitor, cast/instanceof, and exception-creation helpers if they may allocate; and
- any new helper declared `may_gc`.

Pure numeric loops do not gain a POC poll. If the runtime requires cooperative collection or thread suspension while compiled code runs, a later milestone must insert entry/backedge polls with a defined protocol.

## Shadow stack and stack maps

| Option | Benefit | Cost/risk | Decision |
|---|---|---|---|
| homes in `Context.regO` | Reuses current scanner and frames; easiest to compare | extra stores/reloads | selected for POC |
| explicit shadow stack | separate compact native root frames | changes `Context`, scanner, and unwind protocol | candidate after equivalence |
| native-PC stack maps | best register allocation | backend-specific GC/unwind/signal integration | mature backend only |

A future shadow stack must register a frame before any allocation, remove it on every return/exception path, and be visible under correct synchronization. Stack maps must map native PC to roots and TC PC, including calls and any future deoptimization.

## Exceptions, return, and root lifetime

`context->thrownException` is already a root. When propagating an exception, compiled code leaves the object there and removes its frame only after it no longer depends on frame roots. A reference return remains rooted in an agreed slot until copied into the caller frame or `NMParams.retO`.

Fallback is not an exception mechanism. Once compiled code starts, it must return or propagate; reinterpreting would duplicate stores, locks, I/O, and calls.

## Required memory tests

- Allocate at the limit while live references occupy every `regO` position.
- Run compiled → interpreted → compiled calls with GC at each boundary.
- Return and throw an object that would otherwise be unreachable.
- Cover reference arrays, static/instance fields, and `WeakReference`.
- Grow/reallocate every context arena during a call.
- Run a finalizer that calls interpreted code while backends are enabled.
- Combine monitor, exception, and GC paths.
- Stress concurrent compilation publication and collection.

Each test runs with interpreter-only and backend-enabled modes, compares result/exception, and, where practical, forces GC at every `may_gc` helper.

## Questions requiring deeper inspection

- Which helpers outside `objectmemorymanager.c` can directly or transitively trigger GC on each platform?
- Can current thread suspension safely observe arbitrary native generated code, or does it require cooperation?
- Do external consumers depend on the exact `Context` layout and constrain adding a shadow stack?
- Which memory-order/lock guarantees protect compiled-entry publication and shutdown disposal?

These questions do not block the arena-home POC, but they must be answered before JIT is enabled by default.
