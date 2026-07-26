<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Eliminate Plausible Thread and GC Memory-Access Failures

This ExecPlan is a living document. The sections `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective` must be kept up to date as work proceeds.

This document follows `.agent/PLANS.md`. Keep it self-contained when revising it: a future implementer must be able to resume from this file and the current working tree alone.

## Purpose / Big Picture

The goal is to remove plausible use-after-free, invalid cross-context access, stale-root, and thread-lifecycle failure paths in the TotalCross VM. After this work, object allocation and garbage collection must coordinate safely with all running Java threads, VM contexts must remain valid for their complete published lifetime, and shutdown must not free thread-owned state while native threads can still execute.

A developer can observe the result through focused stress tests that repeatedly allocate objects, expand interpreter register arrays, start and stop Java threads, trigger garbage collection, and shut the VM down under load. Those tests must complete without crashes, invalid memory reports, deadlocks, corrupted object lists, or references disappearing while still reachable. AddressSanitizer and UndefinedBehaviorSanitizer runs must remain clean for the covered scenarios, and ThreadSanitizer should be used where the platform and build permit it.

This plan addresses the complete set of plausible failures found in the thread, context, allocation, and garbage-collection paths, not only the Android shutdown symptom summarized in issue #432.

## Working Set and Resume Protocol

The primary implementation paths are:

- `TotalCrossSDK/src/main/java/totalcross/lang/Thread4D.java`, which exposes the Java thread lifecycle state and native start entry point.
- `TotalCrossVM/src/nm/lang/Thread.c`, which bridges `Thread4D.start()` into the native runtime.
- `TotalCrossVM/src/tcvm/tcthread.c` and `TotalCrossVM/src/tcvm/tcthread.h`, which create Java threads, allocate thread arguments, create and destroy VM contexts, and track native thread handles.
- `TotalCrossVM/src/tcvm/posix/tcthread_c.h` and `TotalCrossVM/src/tcvm/win/tcthread_c.h`, which implement platform thread creation, joining, identity, and termination.
- `TotalCrossVM/src/tcvm/context.c` and `TotalCrossVM/src/tcvm/context.h`, which allocate, publish, grow, own, and destroy VM execution contexts and their interpreter register arrays.
- `TotalCrossVM/src/tcvm/objectmemorymanager.c` and `TotalCrossVM/src/tcvm/objectmemorymanager.h`, which allocate objects, maintain free/used/locked lists, mark roots, run finalizers, and reclaim objects.
- `TotalCrossVM/src/tcvm/tcvm.c`, which executes methods, manages interpreter frames, and writes Java references into registers, arrays, fields, and static fields.
- `TotalCrossVM/src/init/startup.c`, which coordinates VM shutdown.
- `TotalCrossVM/src/util/mem.c` and `TotalCrossVM/src/util/mem.h`, which implement heap allocation and the current long-jump failure path.

Supporting files should be created only when implementation begins and only when they reduce resume cost:

- `.agent/state/thread-gc-memory-safety.md` is the first normal read when resuming. Rewrite it after each logical commit with the active milestone, current slice, last commit, touched paths, focused validation, deferred validation, next concrete action, blockers, and a resume command.
- `.agent/evidence/thread-gc-memory-safety.md` is append-only and records compact sanitizer, stress-test, and platform validation evidence with command, revision, result, and log path.
- `.agent/archive/thread-gc-memory-safety-history.md` stores completed milestone detail and rejected alternatives whose rationale remains useful.
- `.agent/reports/thread-gc-memory-safety-editorial.md` is created at the first major milestone closure and updated at final completion.

When resuming, read the state file first if it exists. Then read only the active sections of this plan and the source paths named by the next action. Do not reread every source file or historical record unless the state file identifies a changed assumption.

## Progress

- [x] (2026-07-26) Documented the current failure analysis and created issue #432.
- [x] (2026-07-26) Created this initial ExecPlan on branch `agent/432-fix-thread-gc-memory-access-failures`.
- [ ] Add focused deterministic and stress-test coverage that reproduces or detects the identified lifecycle, root-publication, register-growth, and shutdown failures before changing production behavior.
- [ ] Introduce a safe thread lifecycle and stable native thread-start record that cannot be replaced or reclaimed by a second `Thread.start()` call.
- [ ] Make VM-context construction, publication, register growth, root scanning, and destruction follow a coherent ownership protocol.
- [ ] Introduce stop-the-world garbage-collection coordination and stable root snapshots for all running contexts.
- [ ] Add a native temporary-root mechanism for references held outside published VM roots, including top-level native method return paths.
- [ ] Replace unsafe platform-specific thread identity, creation, join, shutdown, and forced-termination behavior.
- [ ] Make finalization, object-lock transitions, thread counters, and Java-visible thread state safe and deterministic.
- [ ] Run focused sanitizer and stress validation, then close with the smallest sufficient platform build and runtime matrix.

## Current Architecture and Scope

TotalCross objects are allocated from global object-memory-manager lists rather than from per-thread heaps. A newly allocated object is initially placed in a locked state so the garbage collector cannot reclaim it before the caller publishes it into a root or reachable object graph. The collector marks roots from static fields, locked objects, each published context's Java object registers, the context thread object, native method return slots, and the current thrown exception.

A VM context is the native execution state for one Java thread or one externally driven execution path. It owns interpreter register arrays, the Java call stack, exception state, native method parameters, usage ownership fields, and a native thread handle. Context pointers are published in the global `contexts` array so garbage collection can enumerate roots.

The current collector can run while native Java threads continue mutating registers, arrays, object fields, static fields, context register arrays, and object-list membership. Normal reference writes do not pass through a write barrier, and collector root scanning does not acquire a stable snapshot of every context. This means the collector can observe a reference graph that never existed atomically, miss a newly published reference, or read a context register array while it is being replaced.

Thread startup currently stores native thread arguments and the platform thread handle inside the payload of the Java `Thread4D.taskID` byte array. Calling `start()` again can replace that array while the first native thread still depends on it. Thread state is represented by a plain Java boolean and does not enforce a single legal transition from new to running to terminated.

The plan covers memory safety and lifecycle correctness in these paths. It does not redesign the Java threading API, add Java Memory Model guarantees beyond what is necessary for safe VM operation, replace the entire object allocator, or introduce a moving/compacting collector. Performance optimization is secondary until correctness is demonstrated.

## Plan of Work

### Milestone 1: Build focused failure-detection coverage

Create small tests and runtime fixtures that exercise each unsafe boundary before broad production changes. Prefer deterministic hooks available only in test builds over timing-dependent sleeps. The tests should make it possible to pause a mutator immediately before and after publishing a reference, pause context register growth after allocating the replacement array, force collection at those boundaries, and hold native threads during shutdown.

Add native tests near the existing VM test infrastructure rather than creating a separate standalone framework. If the repository lacks a suitable native test target, add a narrowly scoped test executable under the existing `TotalCrossVM` CMake structure. Add a Java fixture for double `Thread.start()`, thread termination state, allocation pressure from multiple Java threads, and shutdown while worker threads remain active.

The initial test set must cover:

- a reference written by one mutator while another thread triggers collection;
- root scanning while `contextIncrease` replaces the object-register array;
- context allocation failure after a slot would previously have been published;
- two calls to `Thread.start()` on the same object;
- Android-style shutdown with a native Java thread still executing;
- an object reference held temporarily only in native code across an allocation or collection point;
- object resurrection from a finalizer, either as a supported behavior test or as a test for an explicit prohibition;
- invalid repeated object lock/unlock transitions;
- repeated thread creation failure and successful retry without leaked locks or counters.

Acceptance for this milestone is a test suite that reliably detects the old unsafe states through assertions, sanitizer findings, or controlled failure injection. Tests that cannot be made deterministic must run enough iterations to expose the old path consistently and must record their limitations in the evidence file.

### Milestone 2: Establish an explicit Java-thread lifecycle and stable start record

Replace the mutable Java-byte-array-backed `TThreadArgs` storage with a native allocation whose lifetime is independent from Java object movement, field replacement, or garbage collection. The native start record must contain the Java thread object reference, platform thread handle or identifier, startup status, completion status, and synchronization needed by the creator and child. Keep the Java thread object rooted through the native root mechanism introduced later or through an existing locked-object contract that is used exactly once and released exactly once.

In `Thread4D.java`, replace the implicit `alive = true` construction state with explicit lifecycle semantics. The required state machine is `NEW -> STARTING -> RUNNING -> TERMINATED`. A second `start()` from any state other than `NEW` must fail deterministically with the most compatible existing exception type available in the TotalCross class library. The native side must perform the authoritative atomic transition so concurrent callers cannot both start the same object.

In `Thread.c` and `tcthread.c`, ensure every failure path reverses only the resources it acquired: release the Java thread root or lock, destroy the native start record, leave thread counts unchanged when no native thread exists, and publish a terminated or failed state consistently. `threadCount` must become atomic or be protected by the thread registry lock.

Do not store a reusable platform handle in Java byte-array contents. The child must receive the stable native record directly from the platform thread-create function. The creator and child must use a defined handoff so the creator does not free the record before the child has copied or adopted it.

Acceptance is that concurrent and sequential double-start tests fail cleanly, failed creation leaves no locked Java object or incremented count, successful threads move through the documented states, and repeated create/join cycles remain sanitizer-clean.

### Milestone 3: Make context construction and publication transactional

Change `newContext` so a context is fully initialized before it becomes visible in the global `contexts` array. Allocate the context, interpreter registers, call-stack storage, locks, and required bookkeeping into private local variables. Only after every fallible allocation and initialization succeeds may the code acquire the global context-registry lock and publish the pointer.

The current heap long-jump behavior must not bypass cleanup for partially initialized state. Prefer converting context construction to explicit fallible allocation checks. If changing the general heap API is too broad, contain the jump inside a helper whose error branch frees all private allocations and cannot observe or leave a published context pointer. Document why the chosen boundary is safe.

Make context removal symmetric: first prevent new users, then remove the pointer from the global registry under the same registry lock, then wait for or prove the absence of root scanners and context users, and only then free registers and context storage. No collector may read a context after registry removal unless it already holds a stable reference protected by the stop-the-world protocol.

Acceptance is that injected failure at every allocation point in `newContext` leaves the registry unchanged, context counts correct, and no leaked or dangling allocation. A collector triggered after each injected failure must complete without reading freed memory.

### Milestone 4: Add stop-the-world collection and coherent root publication

Introduce a stop-the-world protocol. A stop-the-world collection temporarily prevents every Java mutator thread from changing Java references while the collector takes and traces the root set. A mutator is any thread executing Java bytecode or native VM code that can allocate an object or modify a Java reference.

Add a global GC coordination state and per-context safepoint state. A safepoint is a location where a mutator can pause with its Java references published in known VM roots and without holding a lock needed by the collector. Check for a pending collection at interpreter back edges, method-call boundaries, return boundaries, allocation slow paths, and native-to-Java transition points. Native methods that can run for a long time must either cooperate through an explicit poll or be classified as not touching managed references while collection proceeds.

The collector must request a pause, wait until all registered mutator contexts are parked or classified as safely outside managed execution, freeze context creation/destruction, mark from a stable root set, sweep, and then resume all mutators. The protocol must avoid waiting for the collector thread itself and must define lock ordering among the GC coordinator, context registry, object-memory manager, and per-context usage locks.

Normal Java reference writes do not require a write barrier once all mutators are stopped during marking. Do not add a partial barrier scheme unless measurement later proves stop-the-world pauses unacceptable. Correctness and a small auditable protocol take precedence.

Acceptance is that stress tests can force collection at every deterministic reference-publication hook without losing a live object, reading a freed context, or deadlocking. The evidence file must include at least one multi-thread allocation run under AddressSanitizer and one race-oriented run under ThreadSanitizer where supported.

### Milestone 5: Make register growth and root bounds coherent

Fix interpreter register-array growth so no pointer is advanced beyond its valid allocation before capacity is confirmed. Compute the required size first, grow if needed, update all pointers only after allocation succeeds, and then advance the active register boundary.

During stop-the-world marking, the collector may read each parked context's `regOStart` and active `regO` boundary directly because mutators cannot change them. Outside stop-the-world operation, any diagnostics or non-GC readers must use the context usage protocol or registry lock defined in the previous milestone.

`contextIncrease` must never expose the new pointer before copied contents and bounds are complete, and it must never free the old array while another reader can still hold it. With all mutators parked and registry operations frozen during collection, no extra read-copy-update structure should be necessary.

Acceptance is a forced-growth stress test that repeatedly expands registers while another thread requests collection. The test must preserve all live object references and remain free of out-of-bounds and use-after-free reports.

### Milestone 6: Protect temporary native references

Add a native temporary-root facility for managed object references stored only in C locals across any operation that can allocate, invoke Java, block at a safepoint, or trigger collection. A temporary root is a native slot registered with the current context so the collector treats the referenced object as live until the slot is removed.

Define a small API in `objectmemorymanager.h` or a dedicated root header with operations equivalent to push, update, and pop. Prefer stack-disciplined roots associated with the current context so cleanup can be audited. In debug builds, assert balanced push/pop operations at method and context exit.

Audit `executeMethod`, native method parameter/return handling, exception construction, object allocation helpers, and thread startup for C locals that survive a possible collection point. In particular, protect top-level object return values until the caller has copied them into another published root. Do not assume the native C stack is scanned.

Acceptance is a test in which the only reference to an object is a registered native temporary root while forced allocation and collection occur. The object must remain valid until the root is popped and become collectible afterward.

### Milestone 7: Correct platform thread creation, identity, join, and shutdown

On POSIX platforms, check the integer return value of `pthread_create`; a zero return means success. Configure `pthread_attr_t`, including stack size, before creation and pass the attribute object into `pthread_create`. Destroy the attribute object on every path. Do not join the current thread. Separate self cleanup from external join and define who owns the final native start record.

On Windows, do not use the `GetCurrentThread()` pseudo-handle as persistent thread identity. Store a stable thread ID from `GetCurrentThreadId()` or use thread-local storage for context ownership checks. Keep real handles only where joining or waiting requires them, and close each real handle exactly once.

Remove `TerminateThread` from normal VM shutdown. Forced termination can leave allocator, object-manager, and context locks permanently held and can interrupt list mutation. Replace it with cooperative cancellation: mark shutdown requested, wake blocked VM-managed waits where possible, let threads leave managed execution, join them, and only then destroy their contexts and global VM state. If a platform cannot safely interrupt an arbitrary foreign blocking call, document the limitation and use a bounded diagnostic path rather than freeing memory underneath the thread.

For Android, implement the same cooperative stop-and-join ownership as other platforms. `destroyAll` must not call `destroyContexts` until all VM-created Java threads have acknowledged shutdown and their native execution functions have exited. This directly resolves item 6 from the analysis and issue #432.

Acceptance is that shutdown under active Java-thread load completes without freeing a live context, that POSIX create failures are reported correctly, that self completion does not self-join, and that Windows ownership checks distinguish threads reliably without forced termination.

### Milestone 8: Harden finalization, object locks, counters, and visible state

Choose and document finalizer resurrection policy. If TotalCross intends to support resurrection, run a second mark after finalizers before clearing class metadata or reclaiming objects, and ensure finalizers run at most according to the existing contract. If resurrection is intentionally unsupported, prevent resurrected references from becoming silently dangling and expose deterministic documented behavior. The preferred compatibility path is a second mark because Java code can assign `this` to a static field from `finalize()`.

Change `setObjectLock` invalid transitions from alert-and-continue behavior to fail-fast debug assertions plus safe release behavior that does not unlink or relink an object when its current list membership does not match the requested transition. Centralize list membership updates under the object-memory-manager lock.

Make `threadCount`, shutdown flags, and Java-visible thread state atomic or lock-protected. `isAlive()` must report false before start, true only after successful start and while execution remains active, and false after termination. Preserve compatibility where possible, but prefer correct lifecycle state over the current construction-time `true` value.

Acceptance is that resurrection behavior has an explicit passing test, invalid lock transitions do not corrupt lists, counters return to baseline after stress runs, and Java-visible lifecycle tests pass.

### Milestone 9: Integrate validation and close the plan

Run validation in increasing cost order and stop at the first sufficient level for each slice. During implementation, use focused native or Java tests and sanitizer targets. At milestone boundaries, build the native VM for the host platform. Before completion, run at least one Android build or device/emulator stress execution because the original shutdown defect is Android-specific, plus one POSIX desktop run and one Windows run when infrastructure is available.

Update `.agent/state/thread-gc-memory-safety.md` after every logical commit. Append concise evidence rather than copying logs into this plan. At final completion, reconcile every progress item, write the editorial report, and summarize any unsupported native blocking-call shutdown cases as explicit limitations rather than silently accepting unsafe cleanup.

## Surprises & Discoveries

- Observation: Thread start arguments and the platform handle are stored inside the payload of a Java `byte[]` referenced by `Thread4D.taskID`.
  Evidence: `ThreadArgsFromObject` derives a native pointer from `ARRAYOBJ_START(Thread_taskID(o))`, so replacing `taskID` can invalidate native state still used by the first thread.

- Observation: Context creation can publish a context pointer before all fallible allocations are complete.
  Evidence: `newContext` inserts the context into `contexts[]`, then allocates register arrays; a heap long jump can return without removing the published pointer.

- Observation: Garbage collection enumerates context registers while mutators can modify or replace those arrays.
  Evidence: ordinary reference writes are direct, `contextIncrease` frees the old register allocation, and root marking reads context bounds without a stop-the-world handshake.

- Observation: Android thread destruction does not provide the join barrier required by global shutdown ordering.
  Evidence: shutdown destroys contexts after thread-destroy handling even though Android native threads may still execute.

- Observation: Windows context ownership relies on a pseudo-handle rather than stable thread identity.
  Evidence: `GetCurrentThread()` is meaningful relative to the calling thread and is unsuitable as a stored cross-thread identity token.

- Observation: finalizers run after the initial mark and before all free-list objects have class metadata cleared, without a second reachability pass.
  Evidence: a finalizer can publish an object into a root after marking, leaving a live Java reference to memory the collector still treats as free.

## Decision Log

- Decision: Use a stop-the-world collector protocol rather than adding an incremental write barrier to the current collector.
  Rationale: current mutations are widespread and direct. Parking all mutators before root scanning provides a smaller, auditable correctness boundary and avoids an incomplete barrier retrofit.
  Date/Author: 2026-07-26 / OpenAI

- Decision: Move native thread startup state out of the Java `taskID` byte array.
  Rationale: native thread ownership must not depend on a replaceable managed array or on object-list locking side effects.
  Date/Author: 2026-07-26 / OpenAI

- Decision: Fully initialize contexts before publishing them globally.
  Rationale: transactional publication eliminates dangling registry entries after allocation failure and simplifies collector reasoning.
  Date/Author: 2026-07-26 / OpenAI

- Decision: Replace forced thread termination with cooperative shutdown and joins.
  Rationale: freeing VM memory while threads can still run or terminating threads while they hold allocator locks cannot be made memory-safe.
  Date/Author: 2026-07-26 / OpenAI

- Decision: Prefer supporting finalizer resurrection through a second mark unless implementation evidence shows an incompatible existing contract.
  Rationale: this matches plausible Java behavior and prevents silently dangling resurrected references.
  Date/Author: 2026-07-26 / OpenAI

## Validation and Acceptance

Use the validation escalation policy in `AGENTS.md`. Plan-only edits require:

    git diff --check

Implementation slices should first run the focused test target added in Milestone 1. Record the exact target name in the state file once it exists. Sanitizer builds should use compiler-supported AddressSanitizer and UndefinedBehaviorSanitizer flags for host-native tests. Use ThreadSanitizer in a separate build because it is generally incompatible with AddressSanitizer in the same executable.

At native milestone closure, run from the repository root:

    cmake -S TotalCrossVM -B build -DCMAKE_BUILD_TYPE=Debug -G Ninja
    ninja -C build

Use a dedicated sanitizer build directory rather than overwriting a normal build, for example:

    cmake -S TotalCrossVM -B build-asan -DCMAKE_BUILD_TYPE=Debug -G Ninja \
      -DCMAKE_C_FLAGS="-fsanitize=address,undefined -fno-omit-frame-pointer" \
      -DCMAKE_CXX_FLAGS="-fsanitize=address,undefined -fno-omit-frame-pointer"
    ninja -C build-asan

Adjust flags through a repository CMake option if direct flag injection does not reach every required target. Save full output outside the plan and append only result summaries and log paths to the evidence file.

Final acceptance requires all of the following observable behavior:

- live objects remain reachable under forced concurrent allocation and collection stress;
- context register growth cannot produce out-of-bounds or stale-pointer reads;
- context allocation failure leaves no published dangling context;
- a Java thread cannot be started twice, including concurrent starts;
- native temporary references survive collection only while registered;
- Android shutdown waits for VM-created Java threads before freeing contexts;
- POSIX and Windows thread ownership and joining complete without self-join, leaked handle, or forced termination;
- finalizer resurrection follows the documented tested policy;
- object-list and thread counters return to their baseline after repeated stress cycles;
- no focused AddressSanitizer or UndefinedBehaviorSanitizer report remains;
- ThreadSanitizer findings in the changed lifecycle and GC protocol are resolved or individually documented as verified false positives.

Expensive full SDK distribution builds are deferred until a milestone changes public Java classes, native ABI, packaging, or runtime integration broadly enough to require them. The final checkpoint should run the smallest applicable Android, POSIX, Windows, and SDK validations available in the execution environment and record unavailable platforms explicitly.

## Risks and Open Questions

The largest design risk is native code that blocks indefinitely while holding managed references or VM locks. The stop-the-world and cooperative-shutdown protocols must classify these sites and either add polling/wakeup support or prove that the thread is outside managed reference mutation. Do not resume collection or free global memory based only on a timeout.

The existing meaning and external use of `Thread4D.taskID` must be checked before removing or changing it. Preserve it only if public or native compatibility requires it; otherwise replace it with an opaque lifecycle representation.

The repository's native test coverage and sanitizer compatibility may vary by platform. If a platform cannot run a sanitizer, retain deterministic assertions and run the same fixture on a supported host. Lack of sanitizer support does not waive behavioral testing.

Finalizer behavior may expose pre-existing application assumptions. Record the current observable behavior before changing it and prefer the closest Java-compatible safe behavior.

Stop-the-world pauses may affect latency under allocation-heavy workloads. Do not optimize prematurely. After correctness passes, measure a small representative allocation workload and record pause and throughput deltas. Only then consider reducing safepoint frequency or introducing more advanced collection techniques.

## Idempotence and Recovery

All implementation steps must preserve unrelated local changes. Stage and commit only the paths named by the active milestone. Generated build directories, sanitizer logs, and downloaded dependencies must remain uncommitted.

Test hooks must be disabled or compiled out of production builds. Re-running stress tests must not depend on leftover files, fixed ports, or global state from a prior process.

Context and thread cleanup must be safe to call after partial initialization. Use explicit ownership flags or null-safe destroy helpers so a failed startup can retry without double-freeing resources. Registry insertion and removal must be idempotent at the caller level: an object is either unpublished or published exactly once, and removal occurs exactly once.

If a milestone introduces a deadlock, use the last logical commit as the recovery boundary. Capture thread stacks and lock ownership before reverting the active slice. Do not use destructive Git commands against unrelated work.

Shutdown changes must retain a diagnostic escape path for development builds, but that path must not free contexts or global object memory while native threads remain active. A process-level abort is safer than continuing after an unjoinable managed thread when memory ownership cannot be proven.

## Outcomes & Retrospective

The initial plan has been created and tied to issue #432. No production implementation has been completed yet. The intended outcome is a coherent memory-safety boundary spanning Java thread state, native thread ownership, context publication, garbage-collection safepoints, temporary native roots, platform shutdown, finalization, and object-list integrity.

At each milestone closure, summarize what became observably safe, what sanitizer or stress evidence proves it, and what remains. At final completion, compare measured behavior against the original failure paths rather than reporting only that code compiled.

## Revision Note

2026-07-26: Created the initial self-contained ExecPlan from the thread, context, allocation, and garbage-collection failure analysis. The plan deliberately sequences reproducible tests, lifecycle ownership, transactional context publication, stop-the-world collection, temporary native roots, platform thread cleanup, and finalization hardening so each safety boundary can be validated independently.