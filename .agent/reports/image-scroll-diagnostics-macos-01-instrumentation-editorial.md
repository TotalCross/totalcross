<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Editorial Summary

Plan 1 completed successfully. It added policy-neutral diagnostics to the
existing 663-image macOS image-scroll benchmark and passed the focused SDK,
macOS ARM64, packaging, self-test, and smoke gates. No rendering, cache,
JPEG-selection, or optimization policy changed. Plan 2 at
`.agent/plans/image-scroll-diagnostics-macos-02-benchmark.md` is the only next
action.

## Original Plan versus Actual Outcome

The original plan was to explain why `RASTER_OPAQUE_WRITE_PIXELS` had attempts
but no hits, and to measure native JPEG work and actual decode tiers during
scroll, without changing runtime policy. The actual outcome delivered gated
writePixels rejection and candidate accounting, actual JPEG denominator timing,
prefetch-versus-scroll separation, per-frame deltas, compact summaries, and
schema invariants. The full macOS benchmark matrix remains Plan 2 work.

## What Changed

- Native writePixels accounting now exposes structural and downstream reasons,
  observational physical-1:1 candidates, and existing attempt/hit/fallback
  counters without changing eligibility or fallback control flow.
- The final disabled-accounting short-circuit correction was implemented and
  revalidated. With accounting disabled, the original short-circuit expression
  is preserved; with accounting enabled, rejection conditions remain
  independently observable.
- JPEG accounting records actual denominator buckets, requested modes,
  failures, nanoseconds, and prefetch/scroll phase and frame deltas.
- The benchmark schema validates writePixels, JPEG, phase, and frame-sum
  invariants.
- The VM native resolver issue was fixed in `f803d4a4d`; public wrappers and
  output schema were unchanged.

## Decisions and Trade-offs

Diagnostics remain gated by `backingAccountingForTest`, so normal applications
do not pay for timing or diagnostic counter updates. Structural rejection
counters are intentionally non-exclusive, and candidate counters are
observational only; neither changes writePixels policy.

The JPEG timer includes allocation, libjpeg decompression, pixel/storage
conversion, and native backing creation in the successful loader path. It is
not a claim about pure IDCT time. Mask 4 remains the authoritative mask for
`OPAQUE_WRITE_PIXELS` analysis.

## Unexpected Problems and Discoveries

The first packaged smoke found the VM's 32-character native resolver limit:
long diagnostic getter names produced `NoSuchMethodError`, and two candidate
names would have collided after truncation. The private bridge names were
shortened consistently, and the macOS gate was rerun successfully.

The first package attempt ran out of disk space. After task-output cleanup and
space being released, packaging and smokes completed successfully. No
unrelated repository files were removed.

The preserved commits `e1c3bab55`, `3f0ba4067`, and `5e78c6168` contain body
lines over the 80-character checker limit. They were not amended because the
plan forbids history rewriting. The later Plan 2 commit passed the checker;
this exception is recorded factually rather than hidden or rewritten.

## Validation and Measurable Results

Focused SDK image tests, SDK packaging, macOS ARM64 CMake configure, and only
the `tcvm` and `Launcher` targets passed. The macOS-only bundle package,
self-test, prescribed smokes (`0/off`, `0/on`, `32799/off`, `32799/on`), and
mask-4 `off`/`on` processes also passed.

Final mask-4 evidence was:

- off: `220/0/220` attempts/hits/fallbacks and `198` physical-1:1 candidates;
- on: `3414/0/3414` attempts/hits/fallbacks and `3414` candidates;
- both runs: `ATTEMPTED_NO_HIT`.

JPEG phase/frame invariants passed. For mask 4/on, prefetch recorded 660 JPEG
decodes and scroll recorded 0. No Android, iOS, Windows, or Linux build ran,
and the full decode matrix was not run.

## Useful Evidence and Examples

The final validation logs are under `/tmp`:

- `/tmp/image-scroll-diagnostics-plan1-fix-sdk-tests.log`
- `/tmp/image-scroll-diagnostics-plan1-fix-sdk-package.log`
- `/tmp/image-scroll-diagnostics-plan1-fix-cmake-configure.log`
- `/tmp/image-scroll-diagnostics-plan1-fix-cmake-build.log`
- `/tmp/image-scroll-diagnostics-plan1-fix-self-test.log`
- `/tmp/image-scroll-diagnostics-plan1-fix-smokes.log`
- `/tmp/image-scroll-diagnostics-plan1-fix-mask4.log`

The final macOS-only bundle output was
`/tmp/image-scroll-diagnostics-plan1-fix/bundle/`. The SDK ZIP SHA-256 was
`5fb07ada2df36d2267c43eace387b44e8f4de8a6570bbac4ec2beeeabf91338a`.

## Limitations, Remaining Work, and Open Questions

Global `writePixelsAttempts`, `writePixelsHits`, and `writePixelsFallbacks`
may include `tryDirectImageCopy()` and `tryDirectPhysicalCopy()` under
composite masks. The detailed rejection and candidate counters cover regular
`tryWritePixels*`; mask 4 is authoritative for `OPAQUE_WRITE_PIXELS`.

The smoke results do not establish a policy or performance improvement. The
JPEG timer's broader loader-path scope must be retained in later analysis.
Plan 2 must run the full macOS diagnostics benchmark, keep raw results outside
Git, and report absolute raw-results and final ZIP paths. It is the only next
action.

## Possible Article Angles

- How policy-neutral instrumentation explains a fallback-heavy raster path.
- Why attempts, candidates, and successful writePixels hits are distinct.
- How prefetch and scroll phases separate JPEG work without changing caching.
- How a native resolver-name limit was discovered by a packaged smoke.

## Suggested Narrative

Start with the two unanswered diagnostic questions. Explain the gated counters,
actual JPEG denominator timing, and phase/frame snapshots. Show how the first
packaged smoke exposed the resolver limit, then describe the focused fix and
successful rerun. Present the final mask-4 counts and the composite-mask
limitation together, and close by handing off the full macOS measurement to
Plan 2 without implying an optimization result.

## Claims Requiring Human Review

- Confirm that the wording “original short-circuit preserved” is appropriate
  for the supported runtime configurations beyond the validated macOS gate.
- Confirm that the composite-mask counter limitation is clear enough for any
  article or benchmark interpretation.
- Do not turn the 660 prefetch versus 0 scroll observation into a performance
  or cache-policy claim; it is diagnostic evidence for mask 4/on only.
- Confirm that the resolver fix is described as private bridge compatibility,
  not as a public API change.
