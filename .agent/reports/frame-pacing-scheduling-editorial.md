<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Frame pacing and scheduling editorial handoff

## Editorial Summary

The investigation completed five macOS ARM64 measurement stages and a static
Windows packaging pass. Across the macOS stages, 48 fresh measured processes
passed, with one accounting-enabled correctness preflight before each stage.
The corpus preflights confirmed 663 READY images, zero failed or
non-prefetchable images, and no scroll-time JPEG decodes or image/native
geometry materializations.

The work adds diagnostic comparisons for the synthetic pacer, Flick drivers,
animation clocks, native timer deadlines, SDL event waiting, and thread yield
policy. Production defaults remain unchanged. The Windows runner and package
were built and checked without executing the Windows benchmark binary.

## Original Plan versus Actual Outcome

All five stages completed with their planned process counts: 6, 9, 12, 12, and
9. The Windows PowerShell runner encodes the complete 48-process matrix and is
included in Windows benchmark packages with its companion and manifest fields.
The action-built SDK was attested to the source SHA used by the successful
`package.yml` run. Canonical hashes and counts are in
`.agent/evidence/frame-pacing-windows-package.json`.

No pacing policy was promoted to a production default. Three processes per
configuration provide descriptive samples, not statistical proof.

## What Changed

- Added benchmark-only selection for synthetic pacing profiles, Flick driver
  and clock, absolute timer deadlines, SDL wait mode, and native yield mode.
- Added a pure PowerShell Windows runner for all five stages, package manifest
  fields, and a single documented Windows command.
- Added deterministic coverage for Flick and native scheduling helpers, plus
  static contract tests for the Windows matrix and package.
- Preserved the existing defaults: TimerEvent at 40 fps, millisecond animation
  time, relative timer deadlines, SDL polling, and legacy `Sleep(1)`.

## Decisions and Trade-offs

Each measured sample used a fresh process and the same 663-image workload.
The benchmark kept image optimization mask 6, prefetch enabled, the
`worker-semaphore` preparation strategy, and accounting disabled during
measurement. Correctness preflights enabled accounting before each stage.

The absolute native timer mode, SDL wait mode, and native yield mode remain
opt-in diagnostics. The wait implementation uses SDL's event wait plus a
custom wake event; no Windows-specific waitable timer or busy-spin path was
introduced.

## Unexpected Problems and Discoveries

Stage 4 initially stopped one pixel short. Frame output exposed a rounded
zero-motion plateau; a subsequent run showed that `ScrollContainer` treats a
zero-delta `scrollContent` call as an end condition. The benchmark now skips
that call and remains active through the plateau.

An early Stage 3 evidence JSON exceeded the 20 KiB cap after all processes had
passed. The writer now omits null-only row fields and enforces the size cap
before replacing canonical evidence.

The first operator run of the Windows package failed during corpus-hash
initialization: PowerShell 5.1 parsed an FNV seed as a signed `Int32` before
the cast to `UInt32`. The failure occurred before preflight and started no
benchmark processes. The seeds now use `UInt32.Parse` with `HexNumber`; static
contracts and a replacement package pass. The replacement still needs a
Windows run.

## Validation and Measurable Results

For each row below, the values are the minimum and maximum of the three
per-process frame-interval P95 measurements, in milliseconds. They describe
this macOS ARM64 run and do not establish significance.

| Stage | Processes | Per-configuration frame-interval P95 range (ms) |
| --- | ---: | --- |
| 1: synthetic pacer | 6 | current 16 ms: 16.768–16.930; 60 Hz: 17.412–17.450 |
| 2: Flick drivers | 9 | Timer 40: 25.928–26.675; Timer 60: 22.219–23.682; UpdateListener: 23.153–24.245 |
| 3: animation clock | 12 | Timer millis: 23.268–25.023; Timer nano: 23.521–26.248; Update millis: 25.656–26.221; Update nano: 25.139–26.340 |
| 4: timer deadline | 12 | Timer relative: 21.582–24.335; Timer absolute: 20.160–20.663; Update relative: 23.266–25.378; Update absolute: 32.070–32.252 |
| 5: event wait and yield | 9 | Poll/legacy: 31.596–32.125; wait/legacy: 32.069–32.144; wait/native: 31.962–32.520 |

Focused validation passed: 13 frame-pacing contract checks, the distributed
benchmark tests, shell syntax, copyright headers, and diff checks. The
PowerShell entry point is 18,623 bytes / 315 lines and its helper is 14,129
bytes / 272 lines. Focused macOS native tests passed for timer scheduling,
event-loop helpers, and yield parsing. The successful workflow
[`package.yml`](https://github.com/TotalCross/totalcross/actions/runs/36103442356)
built the SDK and platform artifacts and passed its packaged-SDK deploy checks
on macOS ARM, Windows x86/64, and Linux x86/64.

The static Windows package check verified the action-built `tcvm.dll`, EXE,
application TCZ, PowerShell runner pair, manifest, and all 663 corpus files
(660 JPEG, 3 PNG). It recomputed the corpus hash and matched the manifest and
the previously measured corpus. Windows benchmark execution is recorded as
`false` in the package provenance.

## Useful Evidence and Examples

- Stage CSV/JSON: `.agent/evidence/frame-pacing-stage-1-macos.csv` through
  `.agent/evidence/frame-pacing-stage-5-macos.json`.
- Append-only execution notes: `.agent/evidence/frame-pacing-scheduling.md`.
- Windows package and SDK provenance: `.agent/evidence/frame-pacing-windows-package.json`.
- Windows operator entry point: `scripts/run-frame-pacing-benchmark-windows.ps1`.
- Shared benchmark contract and static tests: `scripts/frame_pacing_contract.py`
  and `scripts/test-frame-pacing-benchmark.py`.

## Limitations, Remaining Work, and Open Questions

Each macOS configuration has three process samples from one host. The results
do not support a broad platform comparison or production policy change. The
measurements report callback and frame timing; they do not establish CPU
energy savings from the SDL wait path.

The corrected Windows PowerShell runner has not yet been exercised on
Windows. An operator should run its 48-process matrix from the replacement
bundle and retain the resulting ZIP before drawing Windows-specific
conclusions.

## Possible Article Angles

- Separate the benchmark's own sleep loop from application callbacks and
  rendered frame intervals.
- Compare timer deadline policies per driver instead of presenting one global
  winner.
- Treat event waiting and thread yielding as independent diagnostic variables.

## Suggested Narrative

Start with the original pacing question, then show how the five-stage design
isolated synthetic waits, Flick drivers, clocks, deadlines, and native waiting.
Use the per-process P95 ranges as observations, call out the rounded-scroll
plateau that had to be corrected, and close with the unchanged production
defaults and the Windows runtime-validation limitation.

## Claims Requiring Human Review

- Do not describe three processes per configuration as statistical evidence.
- Do not claim the nano clock, absolute deadlines, SDL wait, or native yield
  improves all workloads or platforms.
- Do not claim CPU or energy savings; this run did not measure them.
- Do not describe Windows behavior as validated until the packaged runner has
  been executed on Windows.
