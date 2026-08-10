<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Harden TCM/J2TC Boundary — Active State

Updated: 2026-08-10T19:00:53Z

## Active milestone

- Milestone 2: stable wire format and safe publication.
- Active plan: `.agent/exec-plan-harden-tcm-j2tc-boundary.md`.
- Active subplan: `.agent/subplan-tcm-wire-and-publication.md`.

## Repository state

- Branch: `feature/422-create-ir-for-jniaot`.
- Baseline revision: `441c5785dd88a6aaf8c028c2a390c27d113ad0d6`.
- Last plan commit: `1dd173ed7` (Milestone 0 baseline).
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
- `.agent/subplan-tcm-wire-and-publication.md`
- `TotalCrossSDK/src/main/java/tc/tools/converter/metadata/TcmWriter.java`
- `TotalCrossSDK/src/main/java/tc/tools/converter/metadata/TcmReader.java`
- `TotalCrossSDK/src/main/java/tc/tools/converter/metadata/TcmFormat.java`
- `TotalCrossSDK/src/test/java/tc/tools/converter/metadata/`

## Next concrete action

Commit the validated Milestone 1 changes, then read
`.agent/subplan-tcm-wire-and-publication.md` in full and inspect only its named
wire, artifact, publication, and failure-test paths.

## Tests written but not executed

- None for Milestone 2.

## Validation completed

- Milestone 0 baseline capture passed for `none` and `aot` with three warmups
  and ten measured samples per mode. Compact outputs and full logs are recorded
  in the evidence index.
- Milestone 1 focused tests, fixed workloads, TCZ comparison, copyright,
  whitespace, and new-file size checks passed. See evidence index.

## Deferred validation

- Milestones 2–4 validation remains deferred until each implementation is
  complete.

## Evidence and logs

- Evidence index: `.agent/evidence/harden-tcm-j2tc-boundary-01.jsonl`.
- Milestones 0 and 1 evidence are recorded in the evidence index.

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
