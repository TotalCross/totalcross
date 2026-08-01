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
- Active slice: audit codec, frame, transform, cache, and bidirectional
  Java/native ownership paths before batching the next implementation.

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
- Pending: codecs/loaders, frame offsets, transforms, texture/cache ownership,
  native readback, and alternating Java/native dirty ownership.

## Next concrete action

Map `applyChanges`, native drawing/readback, and the narrow image codec/frame
callers to their physical-versus-logical dimension ownership before editing.

## Stable foundations

- Logical layout, Retina macOS backing, SkFont-only effective-size text, and
  primary text-control geometry have validated Java and native macOS evidence.
- Android is deferred until final validation; iOS workspace validation is not
  part of ordinary milestone work.
