<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# State — full macOS image diagnostics benchmark

## Active slice

- Plan 2 bootstrap complete; final build/package milestone is next.
- Required branch: `perf/image-decode-distributed-benchmark`.
- Starting HEAD: `d0c8aabf97900db2dc75e1d3c3c9c58bfb78c018`.
- Plan 1 preconditions passed; no implementation paths changed afterward.
- Corpus: `/Users/flsobral/Downloads/win32/win32`.
- Next action: build SDK and macOS ARM64 `tcvm`/`Launcher`, package, and run
  the final self-test/smoke gate before the full benchmark.

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
- [ ] Build/package the exact macOS revision and pass the final smoke gate.
- [ ] Run the complete 126-process scroll and 90-process decode suite.
- [ ] Validate, summarize, and commit the compact closeout artifacts.

## Validation and deferrals

- No Plan 2 SDK/native build or smoke has run yet.
- No other platform or platform matrix build is allowed.
- Raw benchmark output, logs, and ZIPs remain outside Git.

## Deliberate local files

Preserve pre-existing unrelated files, including the mask-diagnosis plans,
`TotalCrossSDK/IOSDateFixture.tcz`, the SDK benchmark log and launcher output,
and `scripts/__pycache__/`.

## Resume command

Read this state first, then continue the active milestone in the Plan 2 file.
Use a fresh task-specific `/tmp` output directory for any retry; never delete
prior benchmark output or unrelated local files.
