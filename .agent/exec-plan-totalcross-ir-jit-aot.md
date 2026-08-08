<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Build a verified TotalCross IR with baseline JIT and portable AOT backends

Status: complete as an architectural validation plan on 2026-08-08.

This ExecPlan follows `.agent/PLANS.md` and `AGENTS.md`. Its objective was to
determine whether TotalCross bytecode could feed a verified, backend-neutral
intermediate representation with JIT and AOT execution without replacing the
legacy interpreter or TCZ format. That objective is complete. Exhaustive opcode
coverage and production readiness are deliberately outside final acceptance.

## Purpose / Big Picture

TotalCross converts Java classes to compact register bytecode and normally
executes that bytecode in the C `executeMethod` interpreter. This work inserted
a typed, backend-neutral TotalCross intermediate representation (TCIR) after
TotalCross bytecode. TCIR can execute through a reference interpreter, an
optional SLJIT baseline backend, and deterministic portable C generated ahead
of time. The legacy interpreter remains the semantic authority and whole-method
fallback.

The principal objective was architectural validation, not exhaustive
production coverage of every TotalCross opcode. Representative semantic
families were sufficient to prove that bounded bytecode can be translated,
verified, interpreted, compiled, and integrated without changing TCZ files or
default VM behavior. Unsupported methods are rejected before a compiled path
produces effects and remain wholly interpreted.

The observable completed result is a default-off four-path architecture for the
implemented subset: legacy `executeMethod`, TCIR interpretation, SLJIT machine
code, and generated C agree under differential and contract testing. Broader
semantic coverage, platform qualification, packaging, publication, and default
selection are product-expansion work in
`.agent/exec-plan-expand-tcir-semantic-coverage-and-production-readiness.md`.

## Working Set and Completed-State Protocol

`.agent/state/totalcross-ir-jit-aot.md` is the completed-state record. It names
the final revision, delivered boundary, validation summary, limitations, and
the separate continuation plan. There is no active slice or resume command for
this completed plan.

`.agent/evidence/totalcross-ir-jit-aot.md` is the append-only index of compact
validation checkpoints. `.agent/archive/exec-plan-totalcross-ir-jit-aot-history.md`
preserves milestone detail and points to the unabridged pre-consolidation plan
at revision `ba6d2f0c3`. The definitive factual handoff is
`.agent/reports/totalcross-ir-jit-aot-editorial.md`.

Future semantic or production work starts with the new continuation ExecPlan;
it must not reopen this plan or reinterpret unsupported families as missing
acceptance criteria here.

## Progress

- [x] Documented the existing Java-to-TotalCross conversion, TCZ/class format,
  all 160 bytecode dispositions, `executeMethod`, frames, calls, exceptions,
  monitors, memory management, and GC-root model.
- [x] Designed and implemented the owned, typed, backend-neutral TCIR contract,
  deterministic text form, opcode registry, verifier, bounded decoder, and
  bytecode frontend.
- [x] Implemented a TCIR reference interpreter and a fresh-state differential
  oracle against the real legacy `executeMethod` path.
- [x] Implemented the optional SLJIT baseline backend with whole-function
  eligibility, artifact lifecycle management, and centralized W^X handling.
- [x] Implemented deterministic portable-C AOT generation, exact identity
  registry/manifest handling, clean regeneration, and compiled execution.
- [x] Integrated default-off mixed interpreted/compiled execution through a
  runtime-owned method side table without changing serialized `TMethod` or TCZ
  layouts.
- [x] Proved representative i32, i64, normalized-f64, reference transport,
  switch, pre-bound static-call, checked-failure, and object-allocation
  semantics while retaining whole-method fallback for unsupported operations.
- [x] Established versioned runtime ABI, GC-visible root-home, TC-PC,
  exception-status, call, allocation, preflight, and fallback contracts.
- [x] Reconciled evidence, history, architecture documentation, completed state,
  final retrospective, and the editorial report.
- [x] Transferred semantic expansion and production readiness to a separate
  continuation ExecPlan whose sequencing is gated by future Java-level
  whole-program optimization/HIR decisions.

The former “Milestone 8 complete semantic coverage” and “Milestone 9 release
readiness” were prospective expansion phases. They are not presented as
implemented. Their unimplemented portions have been reclassified as follow-up
scope because they are not necessary to answer this plan's architectural
question.

## Current Architecture and Delivered Scope

The native TCIR implementation is under `TotalCrossVM/src/tcvm/ir/`. A
`TCIRModule` owns stable symbols and typed functions; functions contain explicit
basic blocks, immutable values, block arguments, TotalCross register homes,
source TC PCs, and declared effects. The canonical model is simplified SSA:
mutable bytecode-register traffic is a construction boundary, while promoted
values and block arguments make control-flow merges explicit without requiring
a full optimizing SSA framework.

`tcirVerifyFunction` is the mandatory boundary before interpretation or backend
entry. It checks graph structure, value/type ownership, block arguments,
terminators, logical bytecode targets, result types, effect declarations,
non-null and unchecked-access proofs, exceptional destinations, and GC-visible
homes at `may_gc` operations. Unsupported or malformed input produces stable
diagnostics and pre-execution rejection.

The reference interpreter in `tcir_interp.c` is the semantic isolation layer
and backend oracle. `TotalCrossVM/src/tcvm/jit/tcir_jit.c` consumes only verified
eligible functions and produces optional SLJIT artifacts. The AOT implementation
in `TotalCrossVM/src/tcvm/aot/tcir_aot.c` produces deterministic C and manifests
in build directories; generated C is not a new shipping bytecode or runtime C
compiler.

`TotalCrossVM/src/tcvm/ir/tcir_runtime.c` owns experimental registration,
policy, diagnostics, lifecycle, and dispatch. `TC_ENABLE_COMPILED_DISPATCH`,
`TC_ENABLE_SLJIT_JIT`, and `TC_ENABLE_C_AOT` remain independent default-off
controls. `executeMethod` remains the facade, and runtime state remains outside
`TMethod`.

Runtime ABI version 5 carries typed frame storage plus direct dispatch,
pre-bound call, and allocation thunks. Helper-bearing operations publish the
originating TC PC and materialize live managed references in `regO` homes. The
implemented allocation contract publishes a newly allocated object in its
destination home before unlock. Whole-method backend preflight prevents a late
fallback from repeating an observable call or allocation effect.

Implemented representative coverage includes pure and checked i32/i64
arithmetic where the recorded semantics are stable, pure normalized-f64
arithmetic and stable integer-to-f64 conversions, managed-reference
transport/identity, null checking, keyed switch control flow, pre-bound static
`CALL_normal`, and `NEWOBJ`. Checked division/remainder and `TEST_regO` are
effectful TCIR paths for which compiled backends deliberately reject the whole
function. Exact per-opcode status remains in
`docs/architecture/bytecode/compatibility-matrix.md` and
`TotalCrossVM/src/tcvm/ir/tcir_opcode_registry.def`.

Not implemented as general TCIR/backend coverage are fields and class
initialization, arrays, complete throw/handler execution, virtual/interface or
lazy calls, monitors, legacy indirect control flow, reflection-sensitive and
other special cases, full opcode coverage, automatic class-loader publication,
and production platform/release policy. Those methods remain safe because the
legacy interpreter handles them through method-atomic fallback.

## Plan of Work — Completed Architectural Outcome

The work proceeded through architecture inventory, TCIR contract, bounded
frontend, reference execution, SLJIT, portable-C AOT, conditional mixed-mode
dispatch, and representative semantic families. The archive contains the
milestone-by-milestone implementation and validation history.

Final acceptance is architectural:

1. TotalCross bytecode has a verified backend-neutral IR boundary after its
   existing Java-to-bytecode lowering.
2. The same verified TCIR can drive an interpreter, a baseline machine-code
   backend, and deterministic generated C.
3. Runtime integration can mix interpreted, compiled, and native calls behind
   default-off policy while preserving `executeMethod` as the facade.
4. Effects, GC roots, exceptions, calls, allocation, diagnostics, and fallback
   have explicit contracts sufficient for the implemented evidence.
5. Unsupported methods retain legacy behavior through whole-method fallback.

The result does not assert exhaustive opcode semantics, production performance,
or release readiness. Those are separate outcomes requiring separate evidence.

## Decision Log

- Decision: Close this plan on architectural proof rather than exhaustive
  opcode and platform coverage.
  Rationale: the original architecture question is answered by representative
  end-to-end families; treating product expansion as a prerequisite would hide
  the completed result and conflate proof with rollout.
  Date: 2026-08-08.

- Decision: Keep every backend and mixed-mode dispatch default-off.
  Rationale: experimental compilation must not alter ordinary VM behavior.

- Decision: Preserve `executeMethod` and the legacy interpreter as the semantic
  authority and permanent compatibility fallback.
  Rationale: unsupported or rejected methods keep established behavior without
  changing TCZ or serialized method layouts.

- Decision: Make backend eligibility and fallback whole-method and preflighted.
  Rationale: no call, allocation, throw, lock, or mutation may execute twice
  because a backend rejects later.

- Decision: Use explicit typed homes and conservative helper effects at GC and
  exception boundaries.
  Rationale: correctness can be proven without exposing private heap layouts or
  relying on native-stack root discovery.

- Decision: Position TCIR after TotalCross bytecode.
  Rationale: TCIR captures TotalCross runtime semantics and native execution
  contracts; it does not retain all Java information needed for a possible
  future Java-aware whole-program optimization layer.

- Decision: Transfer the former remaining Milestone 8/9 scope to a new plan.
  Rationale: semantic/product expansion must start from the proven foundation
  and may be reprioritized after Java-level optimization/HIR investigation.

## Validation and Acceptance

The implementation checkpoints recorded in the evidence index include:

- 15 converter-backed fixtures and 6,398 fresh-state comparisons across legacy
  `executeMethod`, TCIR, SLJIT, and generated C for eligible paths;
- 16 TCIR/SLJIT/AOT allocation-contract comparisons, explicitly separate from
  a real TCZ/class-loader/object-memory-manager proof;
- host macOS arm64 Release and ASan 8/8 focused CTest results, focused UBSan
  runtime execution, converter fixture validation, deterministic AOT checks,
  W^X/lifecycle checks, and a dispatch-disabled 7/7 isolation build;
- Android arm64-v8a/API 23 compilation of TCIR, JIT, AOT, runtime integration,
  and the conditional VM hook; and
- historical benchmark checkpoints that caught and verified removal of a
  disabled-policy mutex cost, without claiming performance for later semantic
  families the workloads did not execute.

These results satisfy this plan because they demonstrate the architecture and
fallback contracts for representative families. Linux and Windows execution,
full iOS application linkage, device execution, forced/moving-GC stress, real
class initialization, handler/stack-trace equivalence, application benchmarks,
packaging, signing, and distribution policy were not observed and are not part
of final acceptance.

The closure itself changes documentation/process artifacts only. Its validation
is therefore limited to plan structure, local references, opcode/document
consistency, copyright headers, whitespace, contradictory continuation wording,
and a scoped diff confirming that no runtime source changed. Full native,
sanitizer, benchmark, platform, and packaging matrices are intentionally not
repeated because the closure does not change runtime behavior.

## Risks and Open Questions — Transferred, Not Blocking Closure

Future work must still resolve transitive helper effects, class initialization,
field/array layout and checks, forced or moving GC, arena growth, arbitrary
native suspension, exception stack traces and handler selection, monitor
semantics, class/method unloading, legacy `JUMP_regI`, and production artifact
identity/security.

Platform support remains evidence-specific. macOS arm64 host execution and
Android arm64 compilation do not establish Linux, Windows, iOS, device,
entitlement, signing, or distribution readiness. SLJIT CPU support does not by
itself establish executable-memory policy. Generated C compilation does not by
itself establish archive publication or dead-strip-safe registration.

These are limitations of product coverage, not unresolved questions about
whether TCIR is a viable TotalCross bytecode/runtime IR. They are recorded in
the continuation plan and editorial report.

## Idempotence and Recovery

This plan has no next implementation action. Its feature flags remain
default-off and its evidence/history are preserved. Revalidating or editing the
completed record must not delete build directories, generated dependency
checkouts, user logs, or unrelated local work such as
`.agent/sljit-depot-tools-execplan.md`.

Any continuation begins from
`.agent/exec-plan-expand-tcir-semantic-coverage-and-production-readiness.md`.
That plan must create its own active state/evidence records when execution is
authorized; it must not rewrite this completed state into a resume document.

## Outcomes & Retrospective

The original intent was to learn whether a verified intermediate form could sit
between TotalCross bytecode and native execution, support both JIT and AOT, and
coexist safely with the current interpreter and TCZ format. The branch delivered
that architecture. TCIR now occupies the boundary immediately after
TotalCross-bytecode decoding, where TotalCross register, control-flow,
exception-PC, runtime-helper, and GC-home semantics are still explicit.

The implemented model uses typed immutable values, explicit CFG blocks and
edges, simplified-SSA block arguments, and mutable typed homes where the legacy
frame and collector require them. The canonical verifier makes malformed
graphs, unsupported operations, mismatched effects, invalid edges, hidden roots,
and other unsafe states rejectable before execution. Stable diagnostics and
deterministic dumps make this a testable compiler contract rather than a
backend-specific instruction list.

The TCIR interpreter supplied an independent semantic oracle between the legacy
VM and native backends. Differential testing then confirmed that representative
numeric, reference, switch, static-call, and allocation paths could preserve
the existing behavior. SLJIT demonstrated a fast-compiling baseline backend
with controlled executable-memory lifecycle. The portable-C backend
demonstrated deterministic AOT generation, exact registry identity, reproducible
output, and execution through the same frame/runtime ABI.

Mixed-mode integration proved that interpreted, compiled, and native paths can
coexist behind opt-in registration and policy without changing `TMethod` or TCZ.
The established contracts require precise TC-PC publication, visible reference
homes at `may_gc`, immediate pending-exception status transfer, typed call
arguments/results, allocation publication before unlock, and pre-execution
backend rejection. The legacy interpreter remains the safe answer for every
unsupported family.

The evidence is intentionally bounded. Opaque-reference and allocation harnesses
prove identity, root transport, status, and publication contracts; they do not
prove arbitrary real-heap mutation, class initialization, moving GC, exception
handlers, or stack traces. Host and cross-compilation checkpoints do not equal a
production platform matrix. Historical arithmetic measurements do not support
performance claims for calls, allocation, or unimplemented families.

The central hypotheses were confirmed: TC bytecode can be translated into a
verified semantic IR; one IR can support reference interpretation, baseline JIT,
and portable AOT; mixed execution can preserve the existing VM facade; and
whole-method fallback permits incremental coverage without weakening
compatibility. The plan's architectural goal is therefore complete.

The most important lesson for later compiler work is boundary discipline. TCIR
is a valid backend/runtime IR for TotalCross bytecode and native execution, but
it begins after Java-level information has already been lowered or normalized.
It should not be stretched into a substitute for a future Java-aware
whole-program optimization layer. Future work should decide that higher-level
strategy first, then add TCIR operations, helpers, and optimizations only when a
measured product need and an executable semantic contract justify them.

## Revision Note

2026-08-08: formally concluded the architectural plan. Representative semantic
coverage and runtime/backend evidence satisfy its purpose; exhaustive opcode
coverage and production readiness were moved, without being claimed complete,
to a new continuation ExecPlan gated by future Java-level optimization/HIR
priorities.
