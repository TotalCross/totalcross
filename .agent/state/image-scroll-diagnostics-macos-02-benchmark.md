<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# State — full macOS image diagnostics benchmark

## Active slice

- Plan 2 completed successfully on benchmark revision `a43535e48`.
- Closeout artifact commit: pending in this slice.
- Required branch: `perf/image-decode-distributed-benchmark`.
- Corpus: `/Users/flsobral/Downloads/win32/win32`.
- Final raw results and combined ZIP are recorded in the evidence, summary,
  and editorial report.

## Working set

- Plan: `.agent/plans/image-scroll-diagnostics-macos-02-benchmark.md`
- State: this file; rewrite on resume.
- Evidence: `.agent/evidence/image-scroll-diagnostics-macos-02-benchmark.md`
- Editorial report:
  `.agent/reports/image-scroll-diagnostics-macos-02-benchmark-editorial.md`
- Compact summary: `.agent/benchmarks/image-scroll-diagnostics-macos/summary.md`

## Preconditions and corpus

- Plan 1 recorded passing SDK, macOS ARM64, package, self-test, smoke, mask-4,
  and JPEG diagnostic gates.
- The resolved corpus has `imag`, `lossless`, `decode-baseline`, `decode-fast`,
  `aggressive-480`, and `aggressive-540`, each with 663 JPEG files.
- The packager stages the two `aggressive-*` folders under its historical
  `aggresive-*` variant names; this is existing tooling behavior.

## Progress

- [x] Bootstrap state/evidence and verify Plan 1/corpus preconditions.
- [x] Build/package the exact macOS revision and pass the final smoke gate.
- [x] Run the complete 126-process scroll and 90-process decode suite.
- [x] Validate, summarize, and prepare the compact closeout artifacts.

## Validation and results

- SDK package passed; log `/tmp/image-scroll-diagnostics-plan2-sdk-package.log`.
- macOS ARM64 configure/build passed; logs
  `/tmp/image-scroll-diagnostics-plan2-cmake-configure.log` and
  `/tmp/image-scroll-diagnostics-plan2-cmake-build.log`.
- Fresh SDK ZIP and macOS-only bundle passed; logs
  `/tmp/image-scroll-diagnostics-plan2-sdk-zip.log` and
  `/tmp/image-scroll-diagnostics-plan2-package-bundle.log`.
- Self-test, four smokes, mask-4 off/on, and decode self-test passed; logs
  `/tmp/image-scroll-diagnostics-plan2-self-test.log`,
  `/tmp/image-scroll-diagnostics-plan2-smokes.log`,
  `/tmp/image-scroll-diagnostics-plan2-mask4.log`, and
  `/tmp/image-scroll-diagnostics-plan2-decode-self-test.log`.
- Full suite passed in 2,242 seconds; log
  `/tmp/image-scroll-diagnostics-plan2-full.log`.
- Scroll: 126/126 PASS rows and unique keys. Decode: 90/90 processes,
  59,670 detailed image rows, zero failed image rows.
- Independent invariant audit passed for every scroll run.
- Benchmark revision hashes: SDK ZIP
  `d9055df2dfad516d1fce114dcd370405c68b2d6706f2ac97da73d4d4e4f01fc7`, SDK
  JAR `0eb93da0a557010e992b00e611533f1f1ac19a580c128eb7fc1af0736a98c63a`,
  dylib `caec5b46d1b58ce65cbe7e43eeb8e25e13fe6a493df7f81b49f1fb2d81b649b2`,
  Launcher `ef6f924f3beda71e6615badf94dd93d5dd167a332250aa22e6dc3855cbcf384a`.
- Combined ZIP SHA-256:
  `2eb68a53125b25cec27cd46fdcb1b961ec59c80a99139228a2f4df57dbd8ae45`.
- Raw results:
  `/private/tmp/image-scroll-diagnostics-plan2-a43535e/bundle/image-scroll-benchmark-macos-arm64/results`.
- Combined ZIP:
  `/private/tmp/image-scroll-diagnostics-plan2-a43535e/bundle/image-scroll-benchmark-macos-arm64/results/totalcross-image-benchmark-results-1789775641349415000.zip`.
- Initial self-test invocation omitted `--bundle`; the corrected invocation
  passed. The full benchmark required no retry.
- No other platform or platform matrix build ran. Raw output remains outside
  Git; compact artifacts are the only committed benchmark results.

## Deliberate local files

Preserve pre-existing unrelated files, including the mask-diagnosis plans,
`TotalCrossSDK/IOSDateFixture.tcz`, the SDK benchmark log and launcher output,
and `scripts/__pycache__/`.

## Resume command

Read this state first, then continue the active milestone in the Plan 2 file.
Use a fresh task-specific `/tmp` output directory for any retry; never delete
prior benchmark output or unrelated local files.
