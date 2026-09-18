<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Editorial Summary

Plan 2 completed successfully on benchmark revision `a43535e48`. The final
macOS ARM64 SDK/runtime bundle ran the complete 126-process scroll matrix and
90-process decode matrix. All required results passed, and the compact summary
records absolute raw-results and ZIP paths. No optimization policy changed.

## Original Plan versus Actual Outcome

The plan was to consume Plan 1 diagnostics, build only SDK/macOS, run the exact
full distributed suite, validate the diagnostic contract, and preserve compact
evidence. The actual run produced 126/126 PASS scroll rows, 90/90 decode
processes, 59,670 successful detailed image rows, zero failed image rows, and a
combined results ZIP. The only next work is analysis or a later policy plan;
this plan does not implement one.

## What Changed

Plan 2 changed only tracked execution artifacts: state, append-only evidence,
the compact benchmark summary, this editorial report, and the living Plan 2
progress/outcomes. It did not change runtime code, benchmark code, counters,
optimization policy, or benchmark semantics.

The exact SDK/native revision was packaged for macOS ARM64. The full runner
executed the existing four smokes, 126 scroll processes, scroll aggregation,
the existing 90-process decode matrix, decode aggregation, and combined ZIP.

## Decisions and Trade-offs

- The existing suite composition was preserved; no quarter/eighth scenarios
  were added to the standalone decode matrix.
- Raw CSV/JSON/log output remains outside Git because it exceeds the compact
  artifact policy. The committed summary records paths, hashes, counts, and
  selected metrics instead.
- Per-run values and medians are reported for selected mask 0/mask 4 cells;
  no mean-only conclusion is used.
- Mask 4 remains authoritative for `OPAQUE_WRITE_PIXELS`. Composite masks can
  add direct image/physical copies to global attempts/hits/fallbacks, while
  detailed rejection/candidate counters cover regular `tryWritePixels*`.

## Unexpected Problems and Discoveries

The first self-test invocation omitted `--bundle`, so the runner looked for
`manifest.json` in the repository and failed before executing the bundle. The
prescribed invocation with `--bundle` passed; this was an invocation error, not
a benchmark or runtime failure.

The full run completed without retry in 2,242 seconds. Mask-4 runs remained
`ATTEMPTED_NO_HIT`; matrix and save-count rejection counters were present in
every selected run, and size-mismatch counters were also present throughout
the prefetch-on mask-4 cells.

## Validation and Measurable Results

The SDK package, macOS ARM64 CMake configure, `tcvm`/`Launcher` build, fresh
SDK ZIP, macOS-only bundle, self-test, four smokes, mask-4 off/on gate, and
decode self-test passed. The full runner then reported 126/126 scroll PASS
rows and produced 90 decode processes with 663 images each and zero failures.

Independent validation confirmed 126 unique scroll keys, 59,670 detailed
decode rows, attempts equal hits plus fallbacks, candidate counts no greater
than attempts, JPEG bucket count/ns sums, and per-frame JPEG sums.

For mask 4, off runs had attempts/hits/fallbacks `223/0/223`, `240/0/240`,
and `240/0/240`, with candidates `201`, `216`, and `216`. On runs had
`3411/0/3411`, `3405/0/3405`, and `3408/0/3408`, with equal candidate counts.
All six selected runs reported `ATTEMPTED_NO_HIT`.

Half-resolution JPEG decoding dominated the selected cells: mask 0/mask 4
scroll-off runs were nearly all half, while prefetch-on runs recorded 655
half and 5 full decodes. No quarter, eighth, or other bucket occurred.

## Useful Evidence and Examples

The compact committed summary is
`.agent/benchmarks/image-scroll-diagnostics-macos/summary.md`.

Absolute raw results:
`/private/tmp/image-scroll-diagnostics-plan2-a43535e/bundle/image-scroll-benchmark-macos-arm64/results`

Absolute combined ZIP:
`/private/tmp/image-scroll-diagnostics-plan2-a43535e/bundle/image-scroll-benchmark-macos-arm64/results/totalcross-image-benchmark-results-1789775641349415000.zip`

The combined ZIP SHA-256 is
`2eb68a53125b25cec27cd46fdcb1b961ec59c80a99139228a2f4df57dbd8ae45`.
Build and run logs remain in `/tmp`, including
`image-scroll-diagnostics-plan2-full.log`.

## Limitations, Remaining Work, and Open Questions

The full run measures the selected macOS workload; it is not cross-platform
evidence and does not prove that a future optimization is safe or beneficial.
The JPEG timer retains Plan 1's loader-path meaning, including allocation,
storage conversion, and native backing work rather than pure IDCT time.

Global writePixels counters remain broader than detailed regular-path counters
under composite masks. The summary deliberately reports this limitation and
does not turn the measured rejection distribution into a policy recommendation.

## Possible Article Angles

- Running a full distributed benchmark without changing its suite semantics.
- Turning fallback counts into actionable, but policy-neutral, evidence.
- Separating prefetch JPEG work from measured scroll work.
- Preserving raw reproducibility while committing only compact evidence.

## Suggested Narrative

Begin with the two questions from Plan 1, then describe the exact macOS-only
build and unchanged suite composition. Show the 126/126 and 90/90 completion
proof, followed by mask-4 rejection/candidate distributions and the half/full
JPEG tier split. Close with the composite-mask caveat and state clearly that
the measurements inform a later policy decision rather than making one.

## Claims Requiring Human Review

- Confirm that the corpus manifest hash method is suitable for external
  reproducibility claims.
- Confirm that half-resolution dominance is stated only for the selected
  measured cells and not generalized to every possible workload.
- Review any interpretation of JPEG nanoseconds because the timer covers the
  successful loader path, not only libjpeg computation.
- Review the composite-mask counter caveat before using writePixels totals in a
  policy or performance article.
