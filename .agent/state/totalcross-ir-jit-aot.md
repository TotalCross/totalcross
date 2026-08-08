<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Completed State: TotalCross IR, JIT, and AOT

## Status

- Plan status: complete as an architectural validation plan on 2026-08-08.
- Last implementation checkpoint: object allocation at rebased branch commits
  `8e5b31bfe` and `f593d24d2`; `ab28d1d59` recorded its evidence.
- Process checkpoint before closure: `201537cd2`.
- Default production behavior is unchanged: `TC_ENABLE_COMPILED_DISPATCH`,
  `TC_ENABLE_SLJIT_JIT`, and `TC_ENABLE_C_AOT` remain opt-in.
- There is no active milestone, next slice, or resume command for this plan.

The request's original starting revision `739dda5f2` is the pre-rebase form of
current-branch commit `3496c035f`; the two commits have the same stable patch ID.
The final review used the rebased branch history through `201537cd2` plus the
documentation-only closure worktree.

## Completed architectural outcome

- The existing converter, TCZ/bytecode, `executeMethod`, frame, exception, call,
  monitor, memory, and GC-root architecture is documented under
  `docs/architecture/bytecode/`.
- TCIR version 1 provides an owned backend-neutral module/function/value/CFG
  model, simplified SSA with explicit typed homes, canonical verification,
  deterministic dumps, and a complete opcode disposition registry.
- A bounded frontend translates supported TotalCross bytecode to verified TCIR.
- A reference interpreter, optional SLJIT backend, and deterministic portable-C
  AOT backend consume the same verified representation.
- Default-off runtime registration and dispatch prove mixed interpreted,
  compiled, and native execution without changing TCZ or `TMethod` layout.
- Runtime ABI version 5 establishes typed frame, dispatch, pre-bound call,
  allocation, TC-PC, exception-status, live-root-home, and
  publication-before-unlock contracts.
- Representative i32/i64/normalized-f64, reference identity/transport, null
  check, switch, static-call, and object-allocation families prove the
  architecture. Unsupported families remain whole-method legacy fallback.

## Final evidence boundary

- 15 converter-backed fixtures retained 6,398 fresh-state legacy
  `executeMethod`/TCIR/SLJIT/AOT comparisons for eligible paths.
- 16 TCIR/SLJIT/AOT allocation-contract comparisons proved status, root
  transport, and destination publication in the isolated harness.
- The final implementation checkpoint passed macOS arm64 Release and ASan 8/8,
  focused UBSan runtime execution, the SDK converter fixture, dispatch-disabled
  7/7 isolation, and Android arm64-v8a/API 23 compilation of the TCIR stack and
  conditional VM hook.
- Historical benchmark evidence caught and verified removal of a disabled
  dispatch mutex cost. Its arithmetic workloads do not measure later semantic
  families.

Detailed checkpoints are indexed in
`.agent/evidence/totalcross-ir-jit-aot.md`; historical milestone detail and raw
artifact references remain in
`.agent/archive/exec-plan-totalcross-ir-jit-aot-history.md` and the preserved
`ba6d2f0c3` plan snapshot.

## Explicit limitations

This completed plan does not claim general fields or class initialization,
arrays, full throw/handler behavior, virtual/interface or lazy calls, monitors,
legacy/special/reflection-sensitive cases, full opcode coverage, real
class-loader/object-memory-manager forced or moving GC, automatic publication,
Linux/Windows execution, full iOS linkage, device validation, packaging,
security/distribution approval, production performance, or backend enablement.

Those limitations do not reopen the architectural result. Methods containing
unsupported semantics remain wholly interpreted, and all compiler/runtime flags
remain default-off.

## Continuation

Future work belongs to the separate plan:

`.agent/exec-plan-expand-tcir-semantic-coverage-and-production-readiness.md`

That plan begins with a priority/architecture decision gate because upcoming
Java-level whole-program optimization and possible high-level IR investigation
may change which TCIR semantic or production work should be done first. No field
or other semantic-family implementation was started during closure.

## Deliberate local scope

- `.agent/sljit-depot-tools-execplan.md` is unrelated untracked local work and
  must not be modified or staged as part of this closure.
- Build directories, generated dependency checkouts, logs, and benchmark
  artifacts remain local unless a user explicitly requests otherwise.

## Supporting records

- Completed plan: `.agent/exec-plan-totalcross-ir-jit-aot.md`.
- Evidence index: `.agent/evidence/totalcross-ir-jit-aot.md`.
- Historical detail: `.agent/archive/exec-plan-totalcross-ir-jit-aot-history.md`.
- Final editorial report:
  `.agent/reports/totalcross-ir-jit-aot-editorial.md`.
- Future continuation:
  `.agent/exec-plan-expand-tcir-semantic-coverage-and-production-readiness.md`.
