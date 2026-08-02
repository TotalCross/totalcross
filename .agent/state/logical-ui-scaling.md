<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Logical UI scaling execution state

Rewrite this file instead of appending. Read it first when resuming.

## Active checkpoint

- Base: `d480df074e7fb6f5a32dfcc2f1f30c3949095e73`
- Branch: `feat/logical-ui-scaling`; preserve history and user changes.
- Active milestone: 4R — image behavior and synchronization.
- Active slice: finish the narrow ownership/cache audit and decide whether M4R
  is complete without widening into renderer work.

## Execution rules

- Read only M4R design/gate, audited and pending symbols, and narrow ranges.
- Redirect verbose output to artifact logs; inspect concise tails/errors only.
- Batch coherent M4R changes before SDK distribution and macOS native deploy.
- Verify deployed output with hashes and machine-readable assertions.
- Update state/evidence once per validated slice; review token use only at
  milestone boundaries.

## M4R audit

- Audited: `Image.java`, `Image4D.java`, `image_Image.c`, `instancefields.h`,
  `DanfeScalingTest`, and `GraphicsScaleTest`.
- Validated: `Image4D.createLogical(3, 2, 2)` is deployed to macOS and reports
  logical `3x2`, physical `6x4`. ABI macros retain legacy `lastAccess` and
  `textureId` offsets before the new logical dimensions.
- Validated: native Skia reads image rows in one physical readback; a scale-2
  `2x2` source copies its four colors at natural logical size and a two-frame
  image exposes a physical and logical visible width of `3`.
- Corrected: native row readback uses one RGBA_8888 bitmap/readPixels operation
  and `getColor`; temporary per-pixel readback and diagnostic field lookups are
  removed. Direct Image ABI offsets are proven again by the deployed fixture.
- Validated: logical images export physical PNG dimensions. Existing transforms
  deliberately produce fixed-pixel scale-1 images using physical dimensions.
- Validated: ordinary PNG decoding preserves encoded physical dimensions and
  creates the existing scale-1 image contract in Java and native macOS.
- Validated: an existing native Skia texture is discarded and recreated when
  Java-side image writes mark its backing changed; deployed macOS redraw sees
  the refreshed pixels.
- Pending: narrow cache/copy ownership audit only.

## Next concrete action

Inspect copy and texture-release ownership for a scaling-specific defect; if
none is found, close M4R with the audited ownership matrix.

## Stable foundations

- Logical layout, Retina macOS backing, SkFont-only effective-size text, and
  primary text-control geometry have validated Java and native macOS evidence.
- Android is deferred until final validation; iOS workspace validation is not
  part of ordinary milestone work.
