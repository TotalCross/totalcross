<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization phase 1 state

Updated: 2026-09-05
Branch: `perf/image-opt-phase1-controls`
Base SHA: `1898014784b2fba5716cc033e49520740b05f0dd`
Plan: `.agent/plans/exec-plan-image-opt-phase1-controls.md`

## Active slice

Milestone 0 bootstrap is in progress. The branch is created from the authored
master SHA. The plan and resumable artifact skeletons are being committed
before source implementation.

## Next concrete action

Validate the scoped bootstrap files, stage only the plan and phase-1 control
artifacts, create the milestone-0 documentation commit, validate its message,
then begin milestone 1 benchmark harness work.

## Active paths

- `.agent/plans/exec-plan-image-opt-phase1-controls.md`
- `.agent/state/image-opt-phase1-controls.md`
- `.agent/evidence/image-opt-phase1-controls.jsonl`
- `.agent/archive/image-opt-phase1-controls-history.md`
- `.agent/reports/image-opt-phase1-controls-editorial.md`
- `.agent/design/image-optimization-benchmark-protocol.md`

## Validation

No build or native smoke is permitted for milestone 0. Pending focused header
validation, Markdown size checks, and `git diff --check`.

## Deferred validation

SDK and macOS Release builds are deferred to the end of milestone 1, as
required by the active plan. Verbose logs will remain under the ignored
`artifacts/image-opt-phase1-controls/` path.

## Decisions still active

- Settings are package-private, process-global, tri-state, and opt-in.
- All future optimization defaults resolve to disabled.
- Native hot paths receive effective feature bits at call boundaries.
- Benchmark evidence is committed; generated binaries and verbose logs are not.

## Blockers and deliberate out-of-scope files

There are no blockers. The pre-existing untracked
`scripts/run-image-modifier-memory-smoke.py` is unrelated local work and must
remain untouched and unstaged.

## Resume command

```sh
sed -n '1,220p' .agent/state/image-opt-phase1-controls.md
sed -n '300,430p' .agent/plans/exec-plan-image-opt-phase1-controls.md
git log -1 --oneline
```
