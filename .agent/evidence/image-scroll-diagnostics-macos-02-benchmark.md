<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Evidence — full macOS image diagnostics benchmark

## 2026-09-18 — Plan 2 bootstrap

- Revision: `d0c8aabf97900db2dc75e1d3c3c9c58bfb78c018`.
- Branch and Plan 1 preconditions: passed.
- Corpus: `/Users/flsobral/Downloads/win32/win32`; six variants with 663 JPEG
  files each; source names `aggressive-480` and `aggressive-540` are staged by
  existing tooling as `aggresive-480` and `aggresive-540`.
- Scope: no build, smoke, other platform, or full benchmark run yet.
- Next: exact SDK/macOS ARM64 build, package, smoke gate, then full suite.

## 2026-09-18 — final macOS build and smoke gate

- Revision: `a43535e48`; corpus count 3,978 JPEG files, packager hash
  `af39fea695191a27`, corpus manifest SHA-256
  `77dd1e0560f33677f116bcd9aff75dbbcaccb143c4e304b207dd27d45c566f07`.
- SDK package, macOS ARM64 configure/build of only `tcvm` and `Launcher`,
  fresh SDK ZIP, and macOS-only bundle passed. Logs:
  `/tmp/image-scroll-diagnostics-plan2-sdk-package.log`,
  `/tmp/image-scroll-diagnostics-plan2-cmake-configure.log`,
  `/tmp/image-scroll-diagnostics-plan2-cmake-build.log`,
  `/tmp/image-scroll-diagnostics-plan2-sdk-zip.log`, and
  `/tmp/image-scroll-diagnostics-plan2-package-bundle.log`.
- Corrected self-test, four scroll smokes, mask-4 off/on, and decode self-test
  passed. Logs:
  `/tmp/image-scroll-diagnostics-plan2-self-test.log`,
  `/tmp/image-scroll-diagnostics-plan2-smokes.log`,
  `/tmp/image-scroll-diagnostics-plan2-mask4.log`, and
  `/tmp/image-scroll-diagnostics-plan2-decode-self-test.log`.
- The first self-test invocation omitted `--bundle` and failed before bundle
  execution; the corrected invocation passed. No benchmark retry was needed.

## 2026-09-18 — full macOS benchmark

- Full command completed with status 0 in 2,242 seconds; log:
  `/tmp/image-scroll-diagnostics-plan2-full.log`.
- Scroll aggregation: 126/126 PASS rows and 126 unique matrix keys.
- Decode aggregation: 90/90 processes, 59,670 detailed rows (90 x 663), and
  zero failed image rows.
- Independent audit passed attempts/hits/fallback, candidate, JPEG bucket,
  and per-frame delta invariants for every scroll run.
- Raw results:
  `/private/tmp/image-scroll-diagnostics-plan2-a43535e/bundle/image-scroll-benchmark-macos-arm64/results`.
- Combined ZIP:
  `/private/tmp/image-scroll-diagnostics-plan2-a43535e/bundle/image-scroll-benchmark-macos-arm64/results/totalcross-image-benchmark-results-1789775641349415000.zip`.
- SDK ZIP SHA-256:
  `d9055df2dfad516d1fce114dcd370405c68b2d6706f2ac97da73d4d4e4f01fc7`.
- Combined ZIP SHA-256:
  `2eb68a53125b25cec27cd46fdcb1b961ec59c80a99139228a2f4df57dbd8ae45`.
- Selected mask-4 writePixels medians: off `240/0/240` attempts/hits/fallbacks
  with 216 candidates; on `3408/0/3408` with 3408 candidates; all runs
  `ATTEMPTED_NO_HIT`. Matrix and save-count reasons occurred on every attempt;
  source-rect and size-mismatch counters remained overlapping diagnostics.
- Selected JPEG result: mask 0/mask 4 scroll-off was almost entirely half;
  prefetch-on recorded 655 half and 5 full decodes, with zero scroll decodes
  after reset. No quarter, eighth, or other bucket occurred.
- No optimization policy or other platform changed.
