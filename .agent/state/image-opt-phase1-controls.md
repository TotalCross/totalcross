<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization phase 1 state

Updated: 2026-09-07T14:19:44-03:00
Branch: `perf/image-opt-phase1-controls`
Base SHA: `1898014784b2fba5716cc033e49520740b05f0dd`
Current master baseline SHA: `7add0f29e9366a19d894237119a415416e6bb557`
Plan: `.agent/plans/exec-plan-image-opt-phase1-controls.md`

## Active slice

The original Phase 1 control-plumbing work and report remain historical. The
corrective slice is complete through focused tests `42a183473`, workload and
runner `f33760435`, gate fix `4721397d6`, registration fix `8399b8b0a`, and
benchmark evidence `884ffcb61`. The follow-up clear-state tests/smoke landed
in `89458ecc7`; the native clear-only fix is `62a4c9278`. S1 used
`f33760435`; S2/S3 used `8399b8b0a`. The reservation addendum code landed in
`23b361051`, the protocol/plan/editorial update in `e5e35e305`, the
test-isolation follow-up in `edcefbe06`, and the explicit bit-15 mask coverage
in `5ecb1b393`, and compact plan/docs in `1d8deacb1`. Rebaseline evidence is
in `a860deb3f`.

## Next concrete action

No further Phase 1 action remains. Before Phase 2 optimization work, rebase
the implementation from current master; historical reports remain untouched.

## Active paths

- `.agent/plans/exec-plan-image-opt-phase1-controls.md`
- `.agent/state/image-opt-phase1-controls.md`
- `.agent/evidence/image-opt-phase1-controls.jsonl`
- `.agent/archive/image-opt-phase1-controls-history.md`
- `.agent/reports/image-opt-phase1-controls-editorial.md`
- `.agent/design/image-optimization-benchmark-protocol.md`
- `.agent/benchmarks/image-opt-phase1-controls/complete-diagnostic-gating/`
- `.agent/benchmarks/image-opt-phase1-controls/post-stabilization-rebaseline/`

## Validation

Focused copyright/whitespace checks, SDK Image tests, SDK `dist`, macOS Release
software-Skia CMake/Ninja, exact-dylib deployment, the disabled/enabled native
accounting smoke, and related native Image smokes passed. The addendum-focused
run passed all 137 `totalcross.ui.image.*` tests, and SDK `dist -x test` passed.

The post-stabilization report records S1 at current master `7add0f29`, S2/S3
at `1d8deacb1`, and 200/200 samples for each scenario. The initial 60-sample
RSS gap required the 200-sample rerun; the final S2/S1 and S3/S1 RSS deltas
were +1.942% and +0.211%, so matched `vmmap` diagnostics were not required.
All scenarios exited successfully; S2 diagnostic fields were zero and S3
showed expected accounting activity. The plan is 8,748 bytes and 199 lines.

## Deferred validation

Android/iOS/Windows/Linux/GPU validation and later optimizations remain outside
the plan. Matched memory diagnostics were not needed because the 200-sample
RSS differences fell below 5%. Verbose logs remain under ignored
`artifacts/image-opt-phase1-controls/` and temporary logs.

## Decisions still active

- Settings are package-private, process-global, tri-state, and opt-in.
- All future optimization defaults resolve to disabled.
- Native hot paths receive effective feature bits at call boundaries.
- Diagnostic native counter pointers are cached at the Java/native boundary;
  counted native operations do not perform Java class or field lookups.
- The corrective artifact is separate from the historical control report;
  S2 zeroes every emitted diagnostic field and S3 proves re-enablement.
- Clear-only accounting preserves the configured Java/native gate; legacy reset
  helpers continue to reset and enable accounting.
- Benchmark evidence is committed; generated binaries and verbose logs are not.
- IDs 13-15 are reservation-only raster controls; no raster optimization,
  excluded control, or GPU `writePixels` invariant was added.
- A persistent peak-RSS difference above 5% after 200 samples requires matched
  memory/residency diagnostics before regression classification.
- The authored SHA remains historical; current master `7add0f29e9366a19d894237119a415416e6bb557`
  is the S1 source for the new post-stabilization comparison.
- The shared benchmark protocol’s cross-platform policy is prospective for
  Phase 2+; macOS software Skia remains authoritative, hosted CI is secondary,
  Android uses production GPU/OpenGL ES, and historical Phase 1 artifacts need
  no rerun.

## Blockers and deliberate out-of-scope files

There are no Phase 1 blockers. Phase 2 must rebase before implementing later
optimizations. The pre-existing untracked
`scripts/run-image-modifier-memory-smoke.py` is unrelated local work and must
remain untouched and unstaged.

## Resume command

```sh
sed -n '1,220p' .agent/state/image-opt-phase1-controls.md
sed -n '1,220p' .agent/plans/exec-plan-image-opt-phase1-controls.md
git log -1 --oneline
```
