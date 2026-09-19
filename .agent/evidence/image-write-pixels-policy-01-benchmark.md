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
