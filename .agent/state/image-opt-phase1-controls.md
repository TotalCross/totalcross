<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization phase 1 state

Updated: 2026-09-05T06:35:00-03:00
Branch: `perf/image-opt-phase1-controls`
Base SHA: `1898014784b2fba5716cc033e49520740b05f0dd`
Plan: `.agent/plans/exec-plan-image-opt-phase1-controls.md`

## Active slice

The original Phase 1 control-plumbing work and report remain historical. The
corrective slice is complete through focused tests `42a183473`, workload and
runner `f33760435`, gate fix `4721397d6`, registration fix `8399b8b0a`, and
benchmark evidence `884ffcb61`. S1 used `f33760435`; S2/S3 used `8399b8b0a`.

## Next concrete action

No implementation or validation work remains. Phase 2 may branch from the
final phase-1 branch HEAD after this documentation checkpoint. The original
control report was not overwritten.

## Active paths

- `.agent/plans/exec-plan-image-opt-phase1-controls.md`
- `.agent/state/image-opt-phase1-controls.md`
- `.agent/evidence/image-opt-phase1-controls.jsonl`
- `.agent/archive/image-opt-phase1-controls-history.md`
- `.agent/reports/image-opt-phase1-controls-editorial.md`
- `.agent/design/image-optimization-benchmark-protocol.md`
- `.agent/benchmarks/image-opt-phase1-controls/complete-diagnostic-gating/`

## Validation

Focused copyright, whitespace, runner syntax, SDK Image tests, SDK `dist`,
macOS Release CMake/Ninja, exact-dylib deployment, relevant native Image
smokes, and S1/S2/S3 process runs passed. S1/S2/S3 each recorded 60 samples;
all S2 diagnostic fields are zero and S3 has nonzero Java/readback/native
backing counters. No 200-sample rerun was required.

## Deferred validation

Android/iOS/Windows/Linux/GPU validation and later optimizations remain outside
the plan. Verbose logs remain under ignored `artifacts/image-opt-phase1-controls/`.

## Decisions still active

- Settings are package-private, process-global, tri-state, and opt-in.
- All future optimization defaults resolve to disabled.
- Native hot paths receive effective feature bits at call boundaries.
- Diagnostic native counter pointers are cached at the Java/native boundary;
  counted native operations do not perform Java class or field lookups.
- The corrective artifact is separate from the historical control report;
  S2 zeroes every emitted diagnostic field and S3 proves re-enablement.
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
