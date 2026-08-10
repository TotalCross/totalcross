<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Harden TCM/J2TC Boundary — Active State

Updated: 2026-08-10T19:09:20Z

## Active milestone

- Milestone 3: canonical converter semantics.
- Active plan: `.agent/exec-plan-harden-tcm-j2tc-boundary.md`.
- Active subplan: `.agent/subplan-converter-semantic-resolution.md`.

## Repository state

- Branch: `feature/422-create-ir-for-jniaot`.
- Baseline revision: `441c5785dd88a6aaf8c028c2a390c27d113ad0d6`.
- Last plan commit: `7ec5707c5` (Milestone 1 capture/origin hardening).
- Tracked worktree changes at start: none.
- Coordinator plan and subplans were committed with Milestone 0.
- Deliberately out of scope untracked file:
  `.agent/sljit-depot-tools-execplan.md`.

## Active paths

- `.agent/exec-plan-harden-tcm-j2tc-boundary.md`
- `.agent/state/harden-tcm-j2tc-boundary.md`
- `.agent/evidence/harden-tcm-j2tc-boundary-01.jsonl`
- `.agent/archive/harden-tcm-j2tc-boundary-history.md`
- `.agent/reports/harden-tcm-j2tc-boundary-editorial.md`
- `.agent/subplan-converter-semantic-resolution.md`
- `TotalCrossSDK/src/main/java/tc/tools/converter/GlobalConstantPool.java`
- `TotalCrossSDK/src/main/java/tc/tools/converter/tclass/TCMethod.java`
- `TotalCrossSDK/src/main/java/tc/tools/converter/metadata/CompilationMetadataCollector.java`
- Relevant metadata and inherited-owner tests named by the subplan.

## Next concrete action

Commit the validated Milestone 2 changes, then read
`.agent/subplan-converter-semantic-resolution.md` in full and inspect only the
canonical type-mapping, method-validation, program/device hierarchy, and
metadata call-site paths it names.

## Tests written but not executed

- None for Milestone 3.

## Validation completed

- Milestone 0 baseline capture passed for `none` and `aot` with three warmups
  and ten measured samples per mode. Compact outputs and full logs are recorded
  in the evidence index.
- Milestone 1 focused tests, fixed workloads, TCZ comparison, copyright,
  whitespace, and new-file size checks passed. See evidence index.
- Milestone 2 focused tests, frozen v1 validation, one-sample deploy identity,
  static audits, copyright, whitespace, and new-file size checks passed.

## Deferred validation

- Milestones 3–4 validation remains deferred until each implementation is
  complete.

## Evidence and logs

- Evidence index: `.agent/evidence/harden-tcm-j2tc-boundary-01.jsonl`.
- Milestones 0–2 evidence are recorded in the evidence index.

## Active decisions and blockers

- Preserve TCM v1 values and TCZ format.
- `NONE` must preserve ordinary converter/deploy semantics while avoiding all
  metadata-only work.
- Host-JDK reflection must not become declaration truth.
- No blockers.

## Resume command

```bash
sed -n '1,180p' .agent/state/harden-tcm-j2tc-boundary.md
```
