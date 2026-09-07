<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization phase 1 state

Updated: 2026-09-07T12:16:00-03:00
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
in `5ecb1b393`. The current-master rebaseline is active.

## Next concrete action

Build the current-master harness overlay and Phase 1 harness, capture the new
S1/S2/S3 rebaseline, run the required macOS validation, and record the final
handoff. The original reports remain untouched.

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
accounting smoke, and related native Image smokes passed. The new smoke
asserts zero Java/readback/native create/release counters after a real native
create/readback/release while disabled, and increments in the enabled state.
S1/S2/S3 each recorded 60 samples; all S2 diagnostic fields are zero and S3
has nonzero Java/readback/native backing counters. No benchmark rerun was
required because the follow-up only changes counter clearing, not the timed
workload or hot path.

The addendum focused run passed all 137 `totalcross.ui.image.*` tests after
the test-isolation follow-up, and SDK `dist -x test` passed. Focused copyright
and whitespace checks passed. The first focused run found only a shared-JVM
test precondition issue; no production assertion failed after the test reset
was added.

The active plan compaction is 8,507 bytes and 195 lines. The explicit bit-15
mask assertion is committed, but the post-stabilization benchmark and its
required native build/smokes remain pending.

## Deferred validation

The post-stabilization macOS software-Skia build/smokes, S1/S2/S3 rebaseline,
and any required 200-sample diagnostics remain deferred. Android/iOS/Windows/
Linux/GPU validation and later optimizations remain outside the plan. Verbose
logs remain under ignored `artifacts/image-opt-phase1-controls/`.

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
