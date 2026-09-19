<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Device-space writePixels policy handoff

## Purpose / Big Picture

Plan 2 delivers a regular Skia `writePixels()` fast path for safe positive
device-space scale/translate mappings. It applies the device clip and copies
the corresponding source subset, while preserving fallback behavior for
unsupported transforms, alpha, and geometry.

## Working Set and Resume Protocol

- Plan: `.agent/plans/image-write-pixels-policy-02-device-copy.md`.
- State: `.agent/state/image-write-pixels-policy-02-device-copy.md`; final
  milestone status and resume context.
- Evidence: `.agent/evidence/image-write-pixels-policy-02-device-copy.md`;
  append-only commands, revisions, artifacts, and acceptance results.
- Benchmark summary:
  `.agent/benchmarks/image-write-pixels-device-policy/summary.md`;
  consolidated 24-process metrics and hashes.
- Raw results:
  `build/image-write-pixels-policy-02-package/image-scroll-benchmark-macos-arm64/results/`.

Read the state first when resuming; use the evidence and summary for exact
validation records instead of reopening raw logs.

## Progress

- [x] Implemented the device-space planner and regular-path counters.
- [x] Added native clipping/transform correctness coverage.
- [x] Built, packaged, self-tested, and smoke-tested the macOS ARM64 bundle.
- [x] Completed the exact 24-process focused matrix and result summary.

## Current Architecture and Scope

The planner is shared by regular image and pixel-source `writePixels()` paths.
It accepts only finite positive scale/translate mappings with integral physical
boundaries and a compatible source/target size. It intersects the device clip
and target bounds, derives a source subset, and falls back otherwise.

The change adds regular attempts, hits, fallbacks, copied bytes, and clipped-hit
counters to the native bridge and benchmark artifacts. Physical-variant cache,
JPEG/storage/resampling, and prefetch policies remain unchanged.

## Plan of Work

Milestone 1 implemented the policy, counters, bridge, and benchmark schema.
Milestone 2 added native identity, scale, clip, translation, and fallback cases
and passed the macOS ARM64 native gate. Milestone 3 ran the focused profile with
masks `0, 2, 4, 6, 32795, 32799`, both prefetch modes, and two rounds.

## Decision Log

- Decision: keep only positive device-space scale/translate mappings on the
  direct-copy path. Rationale: preserve exact pixels and make matrix/clip
  behavior explicit; unsupported transforms retain the generic path.
- Decision: treat zero real-scroll clipped hits as an observation, not a
  failure. Rationale: the native partial-clip fixture records two clipped hits
  and proves the source-subset behavior.
- Decision: retain the implementation despite mixed performance. Rationale:
  performance is measured evidence for this branch, while correctness and
  accounting gates passed; the `2→6` cold pair is explicitly inconclusive.

## Validation and Acceptance

- `cmake -S TotalCrossVM -B build/image-write-pixels-policy-02-native -G Ninja
  -DCMAKE_BUILD_TYPE=Release -DCMAKE_OSX_ARCHITECTURES=arm64
  -DTC_RENDERER_SKIA=ON -DTC_WINDOWING_SDL=ON
  -DTC_BUILD_NATIVE_STARTUP_TEST=ON`: passed.
- `ninja -C build/image-write-pixels-policy-02-native tcvm Launcher
  skia_surface_test`: passed.
- `build/image-write-pixels-policy-02-native/skia_surface_test`: passed,
  including regular device-space and clipped-copy assertions.
- `./gradlew-agent dist -x test --warning-mode=none --console=plain`: passed.
- `scripts/package-sdk.sh` and the macOS ARM64 package script: passed.
- Package self-test, standard smokes, and focused smokes for
  `4/6/32795/32799 × off/on`: passed.
- Focused matrix: exactly 24/24 PASS rows; aggregation and pairwise comparison
  passed. Full logs are in `/tmp/image-write-pixels-policy-02-*.log`.
- Expensive validation skipped: full platform matrix, standalone decode matrix,
  and release packaging outside macOS ARM64; the plan explicitly scopes this
  milestone to the focused macOS profile.

## Risks and Open Questions

The matrix shows higher P95 work time for several enabled paths and an
`INCONCLUSIVE_VARIANCE` `2→6` cold comparison. This is recorded in the summary;
no third matrix was run. Real scroll clipped-hit counters are zero, but native
partial-clip assertions pass.

The package manifest stores an `imag`-relative corpus hash while the runner's
self-test marker stores the bundle-corpus-root-relative hash. Both are
deterministic, and bundle validation plus all 24 processes passed.

## Idempotence and Recovery

The benchmark package and raw result ZIP are reproducible under the task-specific
`build/image-write-pixels-policy-02-package/` directory. Existing generated
builds, caches, logs, and unrelated local files were preserved. Re-run the
package, self-test, focused smokes, and matrix commands from the plan using the
same corpus and SDK hash; no source cleanup is required.

## Outcomes & Retrospective

The native proof covers clipping and transform fallback, and the packaged
runtime reports regular attempts equal to hits plus fallbacks with zero save
count rejects. The exact matrix completed successfully, with pairwise work and
paint deltas recorded in the summary. Candidate-to-hit conversion was 100% for
the enabled regular candidates; real-scroll clipped hits were zero, while the
native fixture recorded two.

The result ZIP, bundle ZIP, SDK hash, native hash, corpus count/hash, and raw
result directory are recorded in the summary. The implementation deliberately
does not alter physical-variant caching or JPEG/storage/resampling policies.

## Revision Note

Plan 2 is closed after the macOS ARM64 focused matrix and documentation
checkpoint. The follow-up bridge/runner fix is recorded at `5265f38a6`.
