<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization phase 1 state

Updated: 2026-09-05T04:33:16-03:00
Branch: `perf/image-opt-phase1-controls`
Base SHA: `1898014784b2fba5716cc033e49520740b05f0dd`
Plan: `.agent/plans/exec-plan-image-opt-phase1-controls.md`

## Active slice

Milestone 0 bootstrap is complete in commit `ea124e140083bc577500412063e3164e6e900b22`.
Milestone 1 harness and S1 baseline are complete through commit
`d00d7c1dfec4bddc14bdfbb8293b30dfe8b3a3c6`. Milestone 2 implementation is
complete in `4d8c5ab469a18c8a92060885fc790c72ba6b7c00`, with the S3 benchmark
mode fix in `613762c45ed887733b1dab9a12b20b32280e0fca`. Corrected S2/S3 runs
recorded 60 samples each: S2 median 669 ms / peak RSS 107280 KB; S3 median
669 ms / peak RSS 109008 KB. S3 counters prove intentional accounting.

## Next concrete action

Commit the S2/S3 raw samples, summaries, and report. Then finalize the reusable
protocol, run final focused Image tests, SDK dist, and only the relevant native
image smokes if the previous milestone build remains at HEAD. Record the phase-2
handoff SHA in state and editorial output.

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
process runs all passed. Final S2/S3 artifacts and report are staged for
commitment.

## Deferred validation

Final focused validation is pending milestone 3. Verbose logs remain under the
ignored `artifacts/image-opt-phase1-controls/` path.

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
