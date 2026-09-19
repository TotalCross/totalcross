<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Evidence — device-space writePixels policy

## 2026-09-18 — bootstrap

- Revision: `8ab1c6b28a6238c2e2aea53afcd6103bfc46d45b`.
- Branch: `perf/image-decode-distributed-benchmark`.
- Plan 1 closure and the prepared Plan 2 specification were verified; Plan 2
  specification commit: `9594720fa`.
- The fixed device-space mapping, explicit clip/source subset, regular counter,
  native test, and 24-process benchmark requirements are preserved in the
  active plan.
- No implementation, SDK/native build, smoke, package, or focused benchmark
  execution has run for Plan 2.
- Next milestone: implement the shared device-space planner and regular-path
  accounting in `skia_image_backing.cpp`.

## 2026-09-18 — Milestone 1 implementation

- Revision: `5e9540d70` (`perf(skia): enable device space write pixels`).
- Implemented one device-space positive scale+translate planner for regular
  `writePixels()` paths, with explicit device clip and source-subset mapping.
- Added regular attempts, hits, fallbacks, copied-byte, and clipped-hit
  counters through the native bridge and benchmark JSON/CSV validation.
- Checks passed: focused copyright-header validation, `git diff --check`,
  staged whitespace validation, Python AST parsing, and commit-message
  validation.
- Deferred by plan: native macOS build/tests, SDK build, package/self-test,
  focused smoke matrix, and the 24-process benchmark.
- Next milestone: native correctness tests and macOS ARM64 validation.

## 2026-09-18 — Milestone 2 correctness and smoke gate

- Revisions: `5ca1c0686` (`test(skia): cover clipped device write pixels`) and
  `5265f38a6` (`fix(benchmark,macos): stabilize packaged benchmark launch`).
- Native gate passed on macOS ARM64: CMake configure, `tcvm`, `Launcher`,
  `skia_surface_test`, and the native executable test.
- Native regular device-space fixture passed identity, scale, saved partial
  clip, translated partial device clip, skew/rotation fallback, fractional
  physical-boundary fallback, alpha fallback, and non-opaque fallback cases.
- SDK distribution, package creation, package self-test, standard smokes, and
  focused smokes for `4/6/32795/32799 × off/on` passed.
- The package runner required bundle-relative `-p .` and `--app-root=.` on
  macOS; absolute values caused a pre-diagnostic `SIGTRAP`. Diagnostic native
  method names were shortened to fit the VM resolver limit.
- Logs: `/tmp/image-write-pixels-policy-02-cmake-configure.log`,
  `/tmp/image-write-pixels-policy-02-native-build.log`,
  `/tmp/image-write-pixels-policy-02-native-test.log`,
  `/tmp/image-write-pixels-policy-02-package-sdk-final.log`,
  `/tmp/image-write-pixels-policy-02-package-final.log`,
  `/tmp/image-write-pixels-policy-02-smokes-final.log`.

## 2026-09-18 — Milestone 3 focused matrix

- Command: packaged `run-benchmark.py --phase matrix --profile
  write-pixels-policy`.
- Result: exactly 24/24 PASS rows, two rounds, six masks, two prefetch modes;
  aggregation and controlled pairwise comparison passed.
- Raw results: `build/image-write-pixels-policy-02-package/image-scroll-benchmark-macos-arm64/results/`.
- Result ZIP SHA-256:
  `860a0bfdd9d093bd331f9519a26ac264065d1b4d6d1232f6534cb81154353570`.
- Bundle ZIP SHA-256:
  `e6ee75d24942968308a828d231ad603c2ec3e57aec4a199d2e40cf5586831e2e`.
- SDK SHA-256:
  `5fe1876a875232de6e39aa59e26f0067d1c9aac9a1a975c9d709877378962d9f`.
- Native `libtcvm.dylib` SHA-256:
  `76d10a90851b450c0c8759deadfb6f01547562c3912c4e3953b819acc3f50163`.
- Corpus: 663 JPEGs; self-test hash `588a7e0f4019424a`.
- `2→6` with prefetch off is `INCONCLUSIVE_VARIANCE`; no third round was run.
- Real-scroll clipped hits were zero; native partial-clip coverage recorded two.
- Final summary and editorial handoff were created under `.agent/benchmarks/`
  and `.agent/reports/`; Plan 2 is complete.
