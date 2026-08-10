<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Harden TCM/J2TC Boundary — Active State

Updated: 2026-08-10T18:45:53Z

## Active milestone

- Milestone 1: disabled metadata path and linear origins.
- Active plan: `.agent/exec-plan-harden-tcm-j2tc-boundary.md`.
- Active subplan: `.agent/subplan-tcm-disabled-path-and-origins.md`.

## Repository state

- Branch: `feature/422-create-ir-for-jniaot`.
- Baseline revision: `441c5785dd88a6aaf8c028c2a390c27d113ad0d6`.
- Last plan commit: pending Milestone 0 baseline commit.
- Tracked worktree changes at start: none.
- Plan inputs are untracked: coordinator plan and its three subplans.
- Deliberately out of scope untracked file:
  `.agent/sljit-depot-tools-execplan.md`.

## Active paths

- `.agent/exec-plan-harden-tcm-j2tc-boundary.md`
- `.agent/state/harden-tcm-j2tc-boundary.md`
- `.agent/evidence/harden-tcm-j2tc-boundary-01.jsonl`
- `.agent/archive/harden-tcm-j2tc-boundary-history.md`
- `.agent/reports/harden-tcm-j2tc-boundary-editorial.md`
- `.agent/subplan-tcm-disabled-path-and-origins.md`
- `TotalCrossSDK/src/main/java/tc/tools/converter/J2TC.java`
- `TotalCrossSDK/src/main/java/tc/tools/converter/java/JavaCode.java`
- `TotalCrossSDK/src/main/java/tc/tools/converter/metadata/CompilationMetadataCollector.java`
- `TotalCrossSDK/src/test/java/tc/tools/converter/metadata/`

## Next concrete action

Read `.agent/subplan-tcm-disabled-path-and-origins.md` in full, then inspect only
the named J2TC, Java parsing, origin-tag, and metadata tests needed for the first
Milestone 1 implementation slice.

## Tests written but not executed

- None.

## Validation completed

- Milestone 0 baseline capture passed for `none` and `aot` with three warmups
  and ten measured samples per mode. Compact outputs and full logs are recorded
  in the evidence index.

## Deferred validation

- Milestones 1–4 validation remains deferred until each implementation is
  complete.

## Evidence and logs

- Evidence index: `.agent/evidence/harden-tcm-j2tc-boundary-01.jsonl`.
- Milestone 0 evidence is recorded at baseline revision `441c5785`.

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
