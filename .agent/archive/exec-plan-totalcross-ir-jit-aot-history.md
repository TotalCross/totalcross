<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Archived History: TotalCross IR, JIT, and AOT

This file contains retired execution detail for
`.agent/exec-plan-totalcross-ir-jit-aot.md`. Do not read it during an ordinary
continuation. Search it when a past decision, validation result, artifact hash,
or milestone rationale is needed.

## Preservation reference

The complete active-plan snapshot before the 2026-07-18 process consolidation is
preserved without loss at Git revision `ba6d2f0c3`, path
`.agent/exec-plan-totalcross-ir-jit-aot.md`. It contains the former full
Progress, Outcomes, benchmark tables/hashes, editorial report, and slice-level
revision notes. Inspect it narrowly, for example:

    git show ba6d2f0c3:.agent/exec-plan-totalcross-ir-jit-aot.md | \
      rg -n 'm8-allocation-benchmark|Revision note|Milestone 8'

## Completed milestones

### Milestone 1 — architecture inventory

Created bytecode, execution, memory, TCIR, JIT/AOT, and compatibility documents.
The source-oriented inventory remains in `docs/architecture/bytecode/` and is
stable reference material rather than normal continuation input.

### Milestone 2 — TCIR contract

Commits `96c17be4b` and `a3a5e33fa` added the owned TCIR API, verifier, printer,
opcode registry, canonical fixtures, and focused validation. The 158/159 name
table discrepancy remains an explicit compatibility-matrix notice.

### Milestone 3 — frontend

Commits `0fa51be08` and `f0e241b11` added the bounded bytecode method view,
decoder/frontend, converter-backed fixtures, golden TCIR, deterministic
diagnostics, and method-atomic fallback.

### Milestone 4 — reference execution

Commits `d5ebceb43` and `801ae507b` added typed-home TCIR interpretation and a
real `executeMethod` differential harness. The original 1,179-input result is
recorded in the evidence index and baseline snapshot.

### Milestone 5 — SLJIT

Commits `d7d4ad64a` and `8a5ae42d6` added default-off SLJIT integration, W^X
code management, whole-method eligibility, and Android arm64-v8a/API 23
cross-compilation. Benchmark tooling followed in `e49acf6a5` and `77d179edc`.

### Milestone 6 — portable C AOT

Commits `8d738ff66` and `1aa428b74` added the shared compiled ABI, deterministic
C generation, manifest, registry, native host tool, and four-way execution.
Generated sources remain build-directory artifacts.

### Milestone 7 — conditional dispatch

Commits `35b14388b` and `3cdfd6974` added default-off runtime dispatch,
side-table registration, policy/diagnostics, mixed-mode tests, and a disabled
policy fast path. This was intentionally a static-i32 slice.

### Milestone 8 — partial semantic coverage

Commits `da38b7278`, `a7af114e8`, `e305e0d89`, `fedb32c8a`, `eade23ce0`,
`cc78ad5aa`, `64a8bec8c`, `a57eace1a`, `0feb15e29`, `891eb84bc`,
`4115f2f95`, `155c74ebd`, `e7ea5cb14`, and `051800dcd` added the current
numeric, reference, switch, static-call, and allocation coverage. The active
plan states only what remains relevant to fields, class initialization, arrays,
handlers, dispatch, monitors, and special cases.

## Retired process rules

The historical plan required 60/200/1,000 benchmark profiles after many partial
slices, and repeated the same evidence in Progress, Outcomes, Editorial Report,
revision notes, and artifacts. That requirement is retired. `AGENTS.md` and
`.agent/PLANS.md` now define proportional validation, state-first resumption,
evidence indexing, and 60/200-sample full checkpoints unless variance or the
user requires more.
