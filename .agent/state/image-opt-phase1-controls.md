<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization phase 1 state

Updated: 2026-09-05T04:32:00-03:00
Branch: `perf/image-opt-phase1-controls`
Base SHA: `1898014784b2fba5716cc033e49520740b05f0dd`
Plan: `.agent/plans/exec-plan-image-opt-phase1-controls.md`

## Active slice

Milestone 0 bootstrap is complete in commit `ea124e140083bc577500412063e3164e6e900b22`.
Milestone 1 harness and S1 baseline are complete through commit
`d00d7c1dfec4bddc14bdfbb8293b30dfe8b3a3c6`. S1 recorded 60 samples at median
668 ms, p95 671 ms, CV 0.22%, and peak RSS 107456 KB with clean process exit.

## Next concrete action

Implement package-private `ImageOptimizationSettings` and focused Java tests,
then integrate only `DIAGNOSTIC_ACCOUNTING` with existing Image/native-backing
accounting. Keep every other reserved feature behaviorally inert and defer
the next build/smoke gate until the milestone is source-complete.

## Active paths

- `.agent/plans/exec-plan-image-opt-phase1-controls.md`
- `.agent/state/image-opt-phase1-controls.md`
- `.agent/evidence/image-opt-phase1-controls.jsonl`
- `.agent/archive/image-opt-phase1-controls-history.md`
- `.agent/reports/image-opt-phase1-controls-editorial.md`
- `.agent/design/image-optimization-benchmark-protocol.md`

## Validation

Bootstrap and harness source validation passed: focused copyright headers,
Python syntax, and staged whitespace. The SDK distribution, macOS software-
Skia Release build, benchmark deployment, runtime-copy comparison, and S1
process run all passed. S1 artifacts are committed below the control-plumbing
benchmark directory.

## Deferred validation

The SDK and macOS Release builds for settings validation are deferred until
the milestone 2 gate. Verbose logs remain under the ignored
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
