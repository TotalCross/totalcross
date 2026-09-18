<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Editorial handoff — image-scroll diagnostics macOS instrumentation

Plan 1 completed successfully. This factual handoff records the delivered
diagnostics, validation, limitations, and next action without claiming an
optimization result.

## Purpose / Big Picture

Instrument the existing 663-image macOS image-scroll benchmark for writePixels
fallback classification and native JPEG timing. The work is diagnostic only:
no render, cache, JPEG-selection, or optimization policy changed.

## Working Set and Resume Protocol

- Plan 1: `.agent/plans/image-scroll-diagnostics-macos-01-instrumentation.md`
- State: `.agent/state/image-scroll-diagnostics-macos-01-instrumentation.md`
- Evidence: `.agent/evidence/image-scroll-diagnostics-macos-01-instrumentation.md`
- Next plan: `.agent/plans/image-scroll-diagnostics-macos-02-benchmark.md`

Read state first on resume; evidence is append-only. Plan 2 is the only next
action after this closeout.

## Progress

- Milestone 1 delivered gated writePixels structural/downstream counters and
  native JPEG denominator timing.
- Milestone 2 delivered prefetch/scroll separation, per-frame deltas, compact
  summaries, schema checks, and explicit attempt-based statuses.
- The final correction restored the original disabled-accounting short circuit
  while retaining independent rejection counters when accounting is enabled.

## Current Architecture and Scope

The existing `RASTER_OPAQUE_WRITE_PIXELS` path remains unchanged. Accounting is
gated by `backingAccountingForTest`; mask 4 is authoritative for that feature.
Detailed rejection/candidate counters cover regular `tryWritePixels*` calls.

JPEG timing records actual denominator buckets, requested modes, failures, and
prefetch versus scroll phase/frame deltas. The distributed schema validates
attempt, hit, fallback, candidate, JPEG, and frame-sum invariants.

## Plan of Work

Plan 1 added only instrumentation, schema validation, and the private bridge
names needed to expose it. The exact task commits include `e1c3bab55`
(`fix(benchmark): gate write pixel diagnostics`) and `58699d768`
(`docs(benchmark): add macos diagnostics benchmark plan`).

## Decision Log

- Disabled accounting preserves the original short-circuit evaluation.
- Enabled accounting evaluates independent diagnostic rejection conditions.
- Global attempts/hits/fallbacks may include `tryDirectImageCopy()` and
  `tryDirectPhysicalCopy()` under composite masks; detailed counters do not.
  Mask 4 remains the authoritative analysis mask.
- The native resolver limitation was fixed by `f803d4a4d`; public wrappers and
  output schema were unchanged.

## Validation and Acceptance

Passed: focused SDK image tests; SDK packaging; macOS ARM64 CMake configure;
`tcvm`/`Launcher` build; macOS-only bundle packaging; bundle self-test; four
prescribed smokes (`0/off`, `0/on`, `32799/off`, `32799/on`); and mask-4
`off`/`on` runner-validated processes.

Fresh mask-4 results reported attempts/hits/fallbacks of `220/0/220` off and
`3414/0/3414` on; candidates were `198` and `3414`; both were
`ATTEMPTED_NO_HIT`. JPEG/frame invariants passed, with prefetch JPEG count 660
and scroll JPEG count 0 for mask 4/on.

No other platform or full decode matrix was built; those are intentionally
deferred to Plan 2.

## Risks and Open Questions

Composite-mask global counters are broader than the detailed regular-path
counters. Do not interpret them as mask-4-only measurements. Plan 2 must retain
this limitation in its report and use mask 4 for the authoritative feature
analysis.

## Idempotence and Recovery

Task-specific build/package directories and raw smoke output were used. No
unrelated repository files were removed; existing local changes remain
preserved. Full logs and raw results remain outside Git under `/tmp`.

## Outcomes & Retrospective

Plan 1 completed successfully. The resolver fix and complete rerun passed, the
short-circuit fix passed its focused post-fix gate, and no rendering or
optimization policy changed. The only material discovery was the VM's
32-character native resolver limit, corrected before closeout.

## Next Action

Execute Plan 2's full macOS diagnostics benchmark. It will build SDK/macOS
only, preserve raw results outside Git, and record absolute raw-results and ZIP
paths in its compact evidence and report.
