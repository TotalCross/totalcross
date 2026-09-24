<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Prefetch Thread Diagnostic Outcome

## Delivered

`ImagePreparation` retains legacy per-entry threads as its default. Diagnostic
accounting records lifecycle, decode, UI dispatch, adoption, finish, and worker
poll timings only when accounting is enabled. The optional serialized worker
uses `Vm.sleep` polling, claims one active entry at a time, and keeps
already-decoded adoption on the existing UI queue. Package-private shutdown and
reset hooks keep Java SE tests from leaving the worker running.

The benchmark app accepts internal thread mode and worker sleep arguments. Its
new `prefetch-thread-diagnostics` profile uses prefetch/accounting on, one pass,
masks 6 and 38, and six configurations: legacy/0 ms, worker/1 ms, and
worker/2 ms for each mask. It records thread metrics in each run summary and
`prefetchPhases`, gives each configuration a distinct result path, and reports
per-mask elapsed deltas as descriptive data without a performance threshold.

The runner now self-tests a clean results directory before a non-decode phase,
reuses valid self-test state, and preserves partial/invalid state. Result ZIPs
contain regular files under `results/` plus `DebugConsole.txt` when present.
The package manifest adds the new six-process profile while retaining the
31-process full suite and five-process `prefetch-diagnostics` profile.

## Validation

| Check | Result |
| --- | --- |
| `python3 scripts/test-image-scroll-distributed-benchmark.py` | Passed; six-run matrix, aggregation, preflight, ZIP, and existing suite coverage. |
| Python `py_compile` for runner and test | Passed. |
| `bash -n scripts/package-image-scroll-benchmark.sh` | Passed. |
| Focused copyright validation and `git diff --check` | Passed. |
| `./gradlew-agent test --tests totalcross.ui.image.ImagePreparationTest` | Passed after the shutdown hook was changed to avoid unsupported thread APIs. |
| `./gradlew-agent dist -x test` | Passed; deployment conversion now accepts the worker implementation. |
| Benchmark app/support `javac` compilation against `dist/totalcross-sdk.jar` | Passed. |
| macOS CMake configure and `ninja -C build-prefetch-thread-diagnostics tcvm` | Passed using the qrcodegen and SQLite release tags from the pinned depot manifest. |

The first SDK distribution attempts exposed that TotalCross deployment rejects
`Thread.interrupt()` and `Thread.setDaemon(boolean)`. The test shutdown hook now
waits for the worker polling loop to observe its shutdown flag, and the worker
does not depend on daemon status. The final focused test and distribution build
both pass.

The first native configure used stale default dependency tags and received a
404 for qrcodegen; a second attempt exposed a SQLite tag mismatch in discovery.
Using the pinned depot manifest tags for qrcodegen and SQLite allowed configure
and the macOS `tcvm` build to complete. No dependency checkout files were
committed.

No valid `TC_IMAGE_CORPUS` with 663 JPEGs was available. The real six-process
macOS benchmark, packaging run, generated result CSV/JSON, and final result ZIP
are therefore deferred; no performance measurement is claimed.

The local commit-message checker reported a body line longer than 80 characters
for commits `75fda6ef2` and `5caad29d3`. The ExecPlan prohibits rewriting
committed history, so those commits were preserved. The final documentation
checkpoint uses wrapped body lines.

## Commits

- `e6e15cf94` — define the plan on the pinned base.
- `1953ad05a` — account for the legacy preparation lifecycle.
- `2d3d41ae2` — add the serialized prefetch worker.
- `75fda6ef2` — add the diagnostic profile, runner integration, and ZIP contract.
- `5caad29d3` — keep worker shutdown compatible with SDK deployment.
