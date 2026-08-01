<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Logical UI scaling editorial report

Update this report at major milestone closure and final completion. Keep claims
factual and point to evidence rather than copying logs.

## Editorial Summary

Logical layout units are now explicit, image backing scale is independent from
logical image dimensions, and Graphics owns content and font scale. The worktree
also removes control-level dependence on the deprecated global density setting.

## Original Plan versus Actual Outcome

The implementation started directly from the recorded master revision. The
macOS proof uses a deterministic synthetic DANFE fixture at simulated scales 1
and 2, following the user's macOS-first validation direction. Android and iOS
workspace validation remain deferred; no issue or external artifact was updated.

## What Changed

- Added `LayoutUnit` with explicit DP, PIXEL, and inheritance semantics.
- Deprecated the density marker and converter as logical identity APIs.
- Added surface-owned `Graphics` scales and logical/physical Image dimensions.
- Added `Double4D.isFinite` for deployable scale validation.
- Added headless DANFE image dimensions, barcode, and Java synchronization tests.

## Decisions and Trade-offs

Applications that inlined the old `Control.DP` marker require recompilation.
Default images retain scale 1 and raw pixels remain accessible only through the
explicit physical-dimension APIs. The deprecated compatibility density value is
still mirrored by launcher/platform initialization but no UI control reads it.

## Unexpected Problems and Discoveries

`Double.isFinite` initially failed at deployment because the device substitute
`Double4D` lacked the API; the substitute now provides it and deployment passes.
The initial DANFE fixture exposed Java `fillRect` using logical dimensions as
physical bounds for scaled images; it now converts rectangle edges at the image
backing boundary.

## Validation and Measurable Results

Focused SDK tests pass for layout units, scale validation, DANFE dimensions,
barcode runs, and Java-side synchronization. SDK distribution and the macOS
native Ninja tree build pass. The macOS Java fixture started at scale 1 and 2.
Evidence records exact commands and limits in `.agent/evidence/logical-ui-scaling.md`.

## Useful Evidence and Examples

Use `.agent/evidence/logical-ui-scaling.md` and
`artifacts/logical-ui-scaling/` for sanitized, repository-relative evidence.

## Limitations, Remaining Work, and Open Questions

Native-to-Java readback, renderer-equivalence measurements, deployed text
containment, and a window-only macOS screenshot are not independently proven.
The installed capture integration cannot target the launched Java process, and
no desktop-wide capture was accepted. Android and iOS workspace validation are
deferred per the current platform-validation direction. These limitations mean
issue #433 must not be described as ready to close.

## Possible Article Angles

- Moving density from a global setting to a destination-surface contract.
- Separating image logical size from backing-pixel ownership.

## Suggested Narrative

The original density-dependent image path mixed global display information with
image drawing. The delivered surface-owned scale model establishes logical units
at the API boundary and preserves explicit access to physical pixels.

## Claims Requiring Human Review

- Native renderer equivalence and bidirectional synchronization require a
  runtime fixture.
- A privacy-sanitized window-only macOS capture requires a compatible capture
  path or manual maintainer capture.
