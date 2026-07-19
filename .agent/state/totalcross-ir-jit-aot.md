<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Active State: TotalCross IR, JIT, and AOT

Read this file first when resuming `.agent/exec-plan-totalcross-ir-jit-aot.md`.
It is a compact, rewritten state file, not a chronological log.

## Current position

- Active milestone: 8, semantic coverage expansion.
- Last functional checkpoint: object allocation, commits `e7ea5cb14` and
  `051800dcd`; the plan checkpoint is `ba6d2f0c3`.
- Current next family: fields and class initialization. Do not begin it until
  the field access, class-resolution, initialization, GC, and exception effects
  are traced and a bounded slice is selected.
- Default production behavior remains unchanged: `TC_ENABLE_COMPILED_DISPATCH`,
  `TC_ENABLE_SLJIT_JIT`, and `TC_ENABLE_C_AOT` are opt-in.

## Read next

1. Read the active plan's `Working Set and Resume Protocol`, `Milestone 8`,
   `Validation and Acceptance`, and `Risks and Open Questions` sections.
2. Inspect only the paths for the selected slice. For fields, start with the
   plan's named VM field/class-resolution paths and relevant TCIR interfaces.
3. Search the evidence index only if a prior command, result, artifact, or
   limitation is needed. Do not read the archive or editorial report by default.

Recommended first command:

    rg -n '^#{1,3} |Milestone 8|Validation and Acceptance|Open Questions' \
      .agent/exec-plan-totalcross-ir-jit-aot.md

## Implemented Milestone 8 coverage

- Pure i32, i64, normalized f64, conversions with stable semantics, reference
  transport/identity, switch control flow, pre-bound static calls, and `NEWOBJ`
  have TCIR, portable-C AOT, and 64-bit SLJIT coverage where eligible.
- Checked division/remainder and `TEST_regO` are TCIR-only effectful operations;
  compiled backends reject them before execution.
- `NEWOBJ` uses runtime ABI version 5. A helper receives class identity, live
  reference homes, destination home, and TC PC. A successful object must be
  published in the destination `regO` home before it is unlocked.
- The standalone allocation harness proves contract, status, root transport, and
  publication. It does not initialize the real TCZ/class-loader/object-memory
  manager path or prove moving/forced GC, arena growth, or class initialization.

## Active paths and contracts

- TCIR contracts and frontend: `TotalCrossVM/src/tcvm/ir/`.
- Backends: `TotalCrossVM/src/tcvm/jit/tcir_jit.c` and
  `TotalCrossVM/src/tcvm/aot/tcir_aot.c`.
- Conditional runtime adapter: `TotalCrossVM/src/tcvm/ir/tcir_runtime.c`.
- Converter fixtures and native tests:
  `TotalCrossVM/src/tests/ir/` and
  `TotalCrossSDK/src/test/java/tc/tools/converter/modernjava/`.
- Before a field slice, inspect field/class-resolution behavior and all
  transitive effects before declaring any lowering eligible.

## Validation status

- The allocation family completed Level 3 validation: focused host Release and
  ASan suites, focused UBSan runtime execution, default-off isolation, Android
  arm64-v8a/API 23 compilation, converter fixture verification, 6,398 retained
  legacy comparisons, and 16 allocation-contract comparisons.
- Do not repeat that matrix for a documentation-only change or an unrelated
  semantic slice. Select Level 1 or 2 during implementation; escalate to Level
  3 only for a family boundary, ABI change, or directly affected platform.
- The historical arithmetic benchmark does not exercise fields or allocation.
  Do not run it for a field slice unless the dispatcher, invocation ABI, frame,
  scratch allocation, or backend-emission hot path changes. Record any deferred
  broader validation in this file.

## Decisions still active

- Preserve whole-method fallback; never execute partially compiled effects.
- Preflight bindings and class symbols before backend entry when later fallback
  would repeat observable effects.
- Materialize live reference homes at every `may_gc` helper boundary and reload
  arena bases after helpers that can grow them.
- Keep unresolved lazy resolution, virtual/interface calls, fields, arrays,
  handlers, monitors, and special/legacy cases explicit fallback until their
  effects and tests exist.

## Deliberate local scope

- Do not modify or stage `.agent/sljit-depot-tools-execplan.md`; it is unrelated
  local work.
- Build directories, generated dependency checkouts, logs, and benchmark
  artifacts remain uncommitted unless the user explicitly requests them.
- The active process documentation is in scope for this checkpoint; preserve
  unrelated changes while committing only scoped files.

## Supporting records

- Active plan: `.agent/exec-plan-totalcross-ir-jit-aot.md`.
- Evidence index: `.agent/evidence/totalcross-ir-jit-aot.md`.
- Historical detail: `.agent/archive/exec-plan-totalcross-ir-jit-aot-history.md`.
- Editorial handoff: `.agent/reports/totalcross-ir-jit-aot-editorial.md`.
