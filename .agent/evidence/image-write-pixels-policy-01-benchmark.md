<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Evidence — focused writePixels policy benchmark preparation

## 2026-09-18 — bootstrap

- Revision: `02c7e1f528b4d198e6eecb57885313129ee7c294`.
- Branch: `perf/image-decode-distributed-benchmark`.
- Scoped pre-existing untracked logs, plans, caches, and generated fixture
  files were observed and will remain untouched.
- No implementation, build, native smoke, or focused-profile execution has
  run yet.
- Next milestone: same-frame active-work timing with paced frame compatibility
  preserved.

## 2026-09-18 — milestone 1

- Revision: `11042035e` (`perf(benchmark): measure active scroll work`).
- Added same-frame `scroll_work_ns`, `paint_work_ns`, and `work_time_ns`;
  retained paced `frame_time_ns` and added work/paint percentiles.
- Focused static/header checks passed before commit.
- `./gradlew-agent compileSmokeTestJava --no-daemon --console=plain` passed;
  log: `/tmp/image-write-pixels-policy-01-compile-smoke.log`.
- `./gradlew-agent dist -x test --no-daemon --console=plain` passed;
  log: `/tmp/image-write-pixels-policy-01-sdk-dist.log`.
- Native macOS build/smoke and the focused 24-process benchmark were deferred
  by plan scope.

## 2026-09-18 — milestone 2 and closeout

- Revisions: `28c942ef7` timer alignment and `b97e68583` focused profile.
- Static checks passed: package-script `bash -n`, runner `py_compile`, focused
  copyright validation, and `git diff --check`.
- Deterministic fixture passed: full plan 126, focused plan 24, two runs per
  focused key, controlled pairs complete, 24 aggregate rows, 12 pairwise rows,
  new work fields parsed, and work invariants accepted.
- SDK smoke-source compilation and `dist -x test` passed again after the Java
  timer-alignment correction; logs:
  `/tmp/image-write-pixels-policy-01-milestone2-compile-smoke.log` and
  `/tmp/image-write-pixels-policy-01-milestone2-sdk-dist.log`.
- The focused profile writes `results/write-pixels-policy-comparison.csv` and
  marks contradictory two-round work-P50 directions as
  `INCONCLUSIVE_VARIANCE`.
- The focused 24-process benchmark and native macOS validation are deferred
  to Plan 2. No rendering/native optimization policy changed.
- Commit-message checker deviation: `b97e68583` contains one overlong body
  line because literal `\\n` was passed through the shell; history was not
  rewritten.
