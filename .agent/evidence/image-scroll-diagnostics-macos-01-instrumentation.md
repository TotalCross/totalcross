<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Evidence — image-scroll diagnostics macOS instrumentation

Evidence is append-only. Each record keeps the command/result summary and
defers verbose output to an external log when one exists.

## 2026-09-18 — bootstrap

- Revision before work: `5917a4aa3e20123a1ff02c1a5b0cedd9640c0c6b`.
- Branch check: passed for `perf/image-decode-distributed-benchmark`.
- Scoped pre-existing change: the untracked task plan only.
- Bootstrap commit: `73519be8b` (`docs(benchmark): plan macos image diagnostics`).
- Header, size, and staged whitespace checks: passed.
- Commit-message checker: reported one overlong line because the body used
  literal `\\n` escapes; no amend/rewrite was performed per plan.
- Build/smoke: intentionally not run at bootstrap.

## 2026-09-18 — Milestone 1 Slice 1A

- Commit: `12490a1ab` (`perf(vm): diagnose opaque write pixel fallbacks`).
- Delivered: gated structural/downstream writePixels rejection counters,
  device one-to-one candidate counters, and Java/native accounting getters.
- Static checks: focused header validation and `git diff --check` passed.
- SDK/native build and smoke: deferred to the Milestone 1 gate.
- Commit-message checker: the body exceeded 80 columns; no amend/rewrite was
  performed per plan.

## 2026-09-18 — Milestone 1 Slice 1B

- Commit: `0563e08b8` (`perf(vm): measure jpeg decode tiers`).
- Delivered: gated nanosecond timing, actual denominator count/ns buckets,
  requested-mode success counts, and JPEG failure accounting.
- Static checks: focused header validation and `git diff --check` passed.
- Milestone 1 gate: next action; SDK/native builds remain pending.

## 2026-09-18 — Milestone 1 gate

- Focused SDK tests: passed (`totalcross.ui.image.*`). Wrapper log:
  `/tmp/image-scroll-diagnostics-sdk-image-tests.log`.
- SDK distribution: passed with `dist -x test`. Wrapper log:
  `/tmp/image-scroll-diagnostics-sdk-dist.log`.
- Native configure: passed for macOS ARM64. Log:
  `/tmp/image-scroll-diagnostics-cmake-configure.log`.
- Native build: passed for only `tcvm` and `Launcher` (121/121). Log:
  `/tmp/image-scroll-diagnostics-cmake-build.log`.
- Warnings: existing compiler warnings and duplicate static-library linker
  warnings; no task failure.
- Platform scope: no Android, iOS, Windows, or Linux build was run.

## 2026-09-18 — Milestone 2 Slice 2A

- Commit: `4d781d5a2` (`perf(benchmark): record scroll decode diagnostics`).
- Delivered: prefetch/scroll JPEG phase snapshots, primitive per-frame JPEG
  deltas, detailed writePixels counters, corrected attempt-feature statuses,
  and runtime diagnostic invariants.
- Focused header validation and `git diff --check`: passed.
- No SDK/native build ran between Slice 2A and Slice 2B, as required.

## 2026-09-18 — Milestone 2 Slice 2B

- Commit: `72fe224f2` (`test(benchmark): validate diagnostic result schema`).
- Delivered: expanded `frames.csv` parsing, compact diagnostic fields in
  distributed `summary.csv`, counters-schema validation, and README updates.
- `bash -n scripts/package-image-scroll-benchmark.sh`: passed.
- `PYTHONDONTWRITEBYTECODE=1 python3 -m py_compile
  scripts/run-image-scroll-distributed-benchmark.py`: passed.
- Deterministic parser fixture: passed; it verified JPEG denominator and
  writePixels invariants plus phase separation.
- Focused header validation and `git diff --check`: passed.
- Milestone 2 gate: next action; macOS package and benchmark smokes remain
  pending.
