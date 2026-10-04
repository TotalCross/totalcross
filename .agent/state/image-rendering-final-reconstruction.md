<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Image/rendering final reconstruction state

- Active milestone: 6, production-candidate benchmark after completing SDK/native integration validation.
- Base / branch: `5a44f503bf6fa1bec350f1218f4d501a70fc4812` / `fix/image-rendering-final-reconstruction`.
- Last commit: `2f81accfb` — use deploy-compatible weak references in the persistent owner registry.
- Active paths: benchmark support and candidate result directories in sibling checkout `/Users/flsobral/repos/totalcross-performance-lab`, branch `perf/image-rendering-benchmarks`; preserve commits `5b4902108c73842db54287f2b8d0dcec76a6573c` and `264b9fba8581f08f3c0e4f31529831b7270d2c95`.
- Next concrete action: inspect the candidate scripts and recorded baseline, then measure only static warm paint, deterministic repeated scroll, and controlled transient/one-shot behavior.
- Validation completed: M1–M5 complete. M5 passed the 317-test SDK regression suite (20 skipped), SDK distribution, Skia surface assertions, deployed Skia image smokes, and the legacy macOS native scroll-reuse smoke with `nativePrimitive=true`. The weak owner registry was adjusted to pass TotalCross deployment conversion and committed as `2f81accfb`. Focused copyright validation and `git diff --check` passed. Exact commands and logs are indexed in `.agent/evidence/image-rendering-final-reconstruction.md`.
- Deferred validation: production-candidate benchmark, PowerShell-only Windows package, and final history audit follow related milestones. Windows execution is not authorized; do not build Windows, Android, Linux, iOS, or WinCE. Completed P12 probes remain accepted evidence and must not be repeated.
- Active decisions: generic final-raster admission remains second-observation; only an attached persistent UI owner may request immediate admission; native `TARGET_COLOR`/`PHYSICAL` admission remains unchanged; the existing single pipeline slot clears after the last owner leaves.
- Blockers: none identified yet.
- Deliberate untouched local state: dirty source checkout `/Users/flsobral/repos/totalcross-image-scroll-raster-fast-path` and other dirty sibling checkouts.
- Resume command: `cd /Users/flsobral/repos/totalcross-image-rendering-final-reconstruction && sed -n '1,160p' .agent/state/image-rendering-final-reconstruction.md && git -C /Users/flsobral/repos/totalcross-performance-lab status --short --branch`.
