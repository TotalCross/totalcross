<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Scroll raster reuse POC state

- Active milestone: `STOP / REVIEW` — distributed package correction complete.
- Branch: `perf/scroll-raster-reuse-poc`.
- Base branch: `perf/writepixels-tail-diagnosis`.
- Base SHA: `c6cc3bcbf9e28ead3edabb69c12e5c31926a55d0`.
- Current source HEAD: this state revision; verify with `git rev-parse HEAD`.
- Functional implementation checkpoint: `9deef878a`.
- Next action: none; do not rerun macOS performance or structural-smoke phases.
- Active paths: `.agent/plans/scroll-raster-reuse-poc-plan.md`,
  `.agent/state/scroll-raster-reuse-poc.md`,
  `.agent/evidence/scroll-raster-reuse-poc.md`.
- Focused validation completed: Python and shell syntax checks, focused
  copyright-header validation, smoke-test Java compilation, profile-plan
  reconciliation, default package self-test, default decode-negative check,
  and decode-enabled package self-test. Prior POC evidence remains retained.
- The prior M2 correctness result remains retained; only the prior M3
  performance set is invalidated for this timer-gating correction.
- Prior correction issues were fixed: repaint state, endpoint filtering,
  native byte accounting, diagnostics-off local hit measurement, and overlay
  fallback. They are not reopened by this timer-only change.
- The functional correction commit is signed and conventional; its local
  message check reports only the known overlong body-line warning caused by a
  literal `\\n` in the shell-created body. No history rewrite is performed.
- Corrected M2: OFF/ON waypoint hashes matched; ON recorded 71/71 local hits,
  zero fallbacks and recoveries, and four-byte BGRA8888 moved-byte accounting.
- Prior corrected M3 checkpoint: three OFF and three ON processes passed with
  75 trace rows and 71 measured movement frames per pass; it is superseded by
  the timer-gating rerun.
- Final M3: three OFF and three ON processes passed with 150 trace rows and
  142 measured movement frames per pass; accounting and rendering diagnostics
  were disabled, all diagnostic timer deltas were zero, and local outcome
  metrics remained valid.
- Prior classification: `PRESENTATION-BOUND` (invalidated pending rerun);
  final artifacts are under `.agent/benchmarks/scroll-raster-reuse-poc/final/`.
- Previous final evidence commit: `ac0abff74` — invalidated M3 results.
- Final evidence commit: `8d086003f` — record timer-gated M3 results.
- Final classification: `PRESENTATION-BOUND`; fresh artifacts are under
  `.agent/benchmarks/scroll-raster-reuse-poc/final/`.
- Final review status: `STOP / REVIEW`; no follow-up optimization is in scope.
- Deferred validation: the full 44-process suite and macOS performance reruns
  were intentionally skipped because this correction changes workload
  selection and gates but does not provide new performance evidence.
- Decisions still active: vertical-only software-raster reuse; unchanged SDL
  full-frame upload/presentation; release default mask 32799 through Java,
  native draw/decode, and observed-mask probes.
- Deliberate out-of-scope local files: `.agent/benchmarks/image-scroll-prefetch/**`,
  `.agent/plans/image-optimization-mask-*.md`, `TotalCrossSDK/IOSDateFixture.tcz`,
  `TotalCrossSDK/ImageScrollRealWorkloadBenchmarkApp.log`,
  `TotalCrossSDK/etc/launchers/**`, and `scripts/__pycache__/**`.
- Blockers: the available SDK fixture lacks the required
  `dist/etc/launchers/win32/Launcher.exe`; no Windows ZIP or Windows
  benchmark result is claimed. Runtime SHA validation and the explicit Windows
  SDK source attestation remain required.

## Distributed package correction

- The default non-decode suite remains 44 benchmark processes: 30 reduced
  ImageOptimizations, 2 correctness, 6 performance, and 6 release/default-
  scroll processes, plus one self-test process.
- Correctness and performance retain the 120-image `scroll-raster-reuse-poc`
  workload. Release/default-scroll uses all 663 `corpus/imag` client images,
  no explicit ImageOptimizations mask, prefetch on, accounting off, reuse
  off/on for three rounds, and cold plus warm passes.
- `manifest.json` records `sdkSourceAttestation`; Windows requires the
  supplied `--sdk-source-commit` value to equal `--source-commit`. This is an
  explicit operator attestation, not cryptographic proof of the SDK ZIP's
  build revision.
- `--include-decode` remains optional. The default package rejects decode
  phases with a rebuild instruction, and the decode-enabled package self-test
  remains valid.
