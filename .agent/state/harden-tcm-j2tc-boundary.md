<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Harden TCM/J2TC Boundary — Active State

Updated: 2026-08-10T19:59:18Z

## Active milestone

- Complete through Milestone 4.
- Active plan: `.agent/exec-plan-harden-tcm-j2tc-boundary.md`.
- Active subplan: none; Milestone 3 subplan is complete.

## Repository state

- Branch: `feature/422-create-ir-for-jniaot`.
- Baseline revision: `441c5785dd88a6aaf8c028c2a390c27d113ad0d6`.
- Last implementation commits: `d25e8e3bf` (canonical semantics) and
  `e161158ed` (mapped hierarchy compatibility).
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

Commit the final documentation/evidence checkpoint. No implementation remains.

## Tests written but not executed

- None for Milestone 4.

## Validation completed

- Milestone 0 baseline capture passed for `none` and `aot` with three warmups
  and ten measured samples per mode. Compact outputs and full logs are recorded
  in the evidence index.
- Milestone 1 focused tests, fixed workloads, TCZ comparison, copyright,
  whitespace, and new-file size checks passed. See evidence index.
- Milestone 2 focused tests, frozen v1 validation, one-sample deploy identity,
  static audits, copyright, whitespace, and new-file size checks passed.
- Milestone 3 focused type/owner/metadata/inherited-owner tests passed on JDK
  17. Header, whitespace, duplicate-lowering, guarded-reflection, and new-file
  size audits passed.
- Milestone 4 complete focused suite, incremental distribution, aggregate
  deploy, isolated `NONE`/`AOT` byte comparison, production TCM inspection,
  and native macOS smoke passed. The native smoke reported 97 passes and no
  failures.

## Deferred validation

- Installed JDK 11 cannot run the Java-17 Gradle/project bytecode, so the
  optional second-runtime comparison is unavailable without changing the build.
- Android, iOS, and Windows matrices were not run because no platform-specific
  path changed.

## Evidence and logs

- Evidence index: `.agent/evidence/harden-tcm-j2tc-boundary-01.jsonl`.
- Milestones 0–4 evidence are recorded in the evidence index.

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
