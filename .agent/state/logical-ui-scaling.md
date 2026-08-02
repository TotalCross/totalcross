<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Logical UI scaling execution state

Rewrite this file instead of appending. Read it first when resuming.

## Active checkpoint

- Base: `d480df074e7fb6f5a32dfcc2f1f30c3949095e73`
- Branch: `feat/logical-ui-scaling`; preserve history and user changes.
- Active milestone: execution stopped at user request.
- Active slice: Android high-density fixture builds and installs, but cannot
  reach its assertions because Android UI resources in `TCUI.tcz` are unresolved.

## Execution rules

- Read only the active design/gate, audited and pending symbols, and narrow
  ranges.
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
- Audited: frame copies and transforms allocate fresh physical backing with the
  established fixed-pixel scale-1 result; legacy shared texture/cache lifetime
  has no logical-dimension branch.
- Validated ownership boundaries: Java backing writes are covered by focused
  Java tests; native image-canvas writes/readback, alpha, source rectangles,
  frames, codecs, and texture reuse are covered by the deployed macOS fixture.
- Corrected: `Image4D` only reuses a same-sized transform when its backing is
  scale-one; logical scale-two transforms now produce the fixed-pixel result.

## Next concrete action

Resolve Android runtime access to `totalcross/res/android/*.png` from `TCUI.tcz`,
then redeploy and run the high-density semantic fixture.

## M8R stopped state

- Android SDK `/Users/flsobral/Library/Android/sdk` and emulator `emulator-5554`
  are available. Native dependencies and the standard release build pass; the
  generated fixture AAB installs successfully.
- Runtime failure: `Resources.multiedit` is null although the packaged `TCUI.tcz`
  contains `totalcross/res/android/multiedit.png`. Constructing `MultiEdit`
  then throws from `NinePatch$ScalableImage.hashCode`, before any logical-scale
  assertion. The experimental Android bootstrap change was reverted.
- iOS workspace validation remains optional and was not run.

## M7R audit

- Validated: Java Launcher and hash-matched native macOS Skia fixture both pass
  the deterministic semantic assertions.
- Captured: process-owned native window only, with fixed public fixture data;
  no desktop-wide screenshot was retained.

## M6R audit

- Audited: the repository-supported `-DUSE_SKIA=OFF` macOS build and deployed
  fixture with a hash-matched dylib.
- Unsupported: image-backed primitive coordinates are not mapped from logical
  to physical backing; a scale-two source paints only its first physical pixel.
  Do not represent this configuration as semantically equivalent to Skia.

## M5R audit

- Validated: Java image-backed point, horizontal/vertical line, rectangle, and
  outline paths convert logical edges once to physical backing edges. Fractional
  scale `1.5` has focused physical-pixel coverage.
- Validated: Java image blits and source rectangles resolve clip/source/dest in
  logical coordinates, then rasterize the source at destination backing scale.
- Corrected: Java text rasterization selects a local effective `Font` at
  `Font.size * fontScale`; justification and glyph fallback use that same font.
  The raw-image unit harness has no initialized Java font and cannot itself
  prove raster coverage.
- Corrected: scaled Java image text is rasterized in a logical temporary image
  and composed through the validated scaled blit path, preserving logical clip
  and translation.
- Validated: the initialized Java Launcher DANFE fixture passes with scale-one
  backing; logical PIXEL expectations derive from destination contentScale.
- Audited: Java dirty state is a control-surface repaint boolean, not a
  coordinate-bearing dirty rectangle; it requires no additional scale mapping.

## Stable foundations

- Logical layout, Retina macOS backing, SkFont-only effective-size text, and
  primary text-control geometry have validated Java and native macOS evidence.
- Android is deferred until final validation; iOS workspace validation is not
  part of ordinary milestone work.
