<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Review of feat/logical-ui-scaling

This review was prepared against branch head
`ef3b3dec8f15e409597b552ee13db09fd98f3fca`, based on
`d480df074e7fb6f5a32dfcc2f1f30c3949095e73`.

## Executive assessment

The branch contains useful architectural scaffolding and should be continued,
not discarded. The clean base, API symbols, image dimension split, Skia surface
scale hook, and focused SDK tests are valuable.

The recorded progress is nevertheless substantially ahead of the proven
behavior. The branch is not at final documentation and handoff. Several original
milestone acceptance criteria are either only represented by API scaffolding or
were tested through the Java Launcher while the changed native code was only
compiled.

Resume at the corrective checkpoint defined by the updated state file.

## Correct work worth preserving

The branch correctly:

- starts directly from the recorded master commit;
- adds `LayoutUnit`, makes `MainWindow` configure DP, and provides container
  inheritance metadata;
- deprecates `Control.DP` with value zero;
- makes `UnitsConverter.toPixels(int)` an identity;
- deprecates `Settings.screenDensity` as a compatibility mirror;
- adds `Graphics.contentScale` and `Graphics.fontScale` storage;
- adds logical and physical dimensions to `Image`;
- preserves native field ordering through explicit instance-field mappings;
- compiles the current native macOS target after the Skia changes;
- adds focused tests for API validation, image dimensions, basic scaled image
  drawing, PNG dimensions, pixel alpha, and barcode-run counting.

These commits form a reasonable implementation base.

## Finding 1: USE_WRITE_PIXELS was removed incorrectly

Commit `fd7e5d358cd3e19ef4c57e1a4a7ba5622ee0ea33` removed the complete
`USE_WRITE_PIXELS` fast path from `skia_drawSurface`.

With the macro disabled, the resulting path is effectively unchanged. With the
macro enabled, behavior and performance change for the embedded configuration
that deliberately selected direct pixel writes. This is unrelated cleanup and
removes a supported specialization.

The likely motivation was that `writePixels` bypasses the new canvas transform.
That is a real concern only when the destination transform is not identity. The
correct response is not unconditional deletion.

Required correction:

1. Restore the guarded direct-write path.
2. Preserve the existing opaque, full-source, same-size, full-alpha eligibility.
3. Add an additional requirement that direct writing is semantically equivalent
   for the current destination, normally an identity effective transform and
   compatible physical coordinates.
4. Fall back to `drawBitmapRect` whenever scaling, filtering, alpha, clipping, or
   destination transformation requires canvas drawing.
5. Compile the code with the repository's actual `USE_WRITE_PIXELS` configuration
   enabled and disabled.
6. Keep the raw-pixel matrix reset added to `skia_setPixel`, subject to its
   focused tests; do not revert the whole commit.

Do not attempt to run Linux embedded during ordinary implementation. Compile and
exercise the guarded code on macOS where possible, then reserve target-platform
validation for the final platform checkpoint.

## Finding 2: LayoutUnit is not connected to layout behavior

`Container` stores a `LayoutUnit` and resolves inheritance, but the effective unit
is not consumed by `Control.setRect`, child placement, semantic offsets, screen
dimensions, or another layout conversion boundary.

The existing `LogicalLayoutUnitTest` tests only the resolver and constant
identity. It does not prove that:

- DP and PIXEL produce different physical placement at scale greater than one;
- a parent controls the unit used to place its child;
- an explicit child unit controls only its descendants;
- the one-line `MainWindow` PIXEL opt-out preserves a legacy layout;
- shared edges round without gaps.

Therefore the original logical-layout milestone is incomplete. Complete the
actual behavior and tests before treating later renderer work as final.

## Finding 3: Graphics scale is scaffolding, not a complete Java renderer

The Java `Graphics` implementation stores both scales, but current scale-aware
drawing coverage is narrow. Basic image natural-size drawing and one `fillRect`
path are tested. The branch does not yet prove scale handling for the full
primitive, clip, translation, polygon, line, text, image-source, dirty-bound, and
event matrix.

The public `setScales` method also exposes mutable surface-owned state to
application code. Reconcile this with the approved ownership contract. Prefer an
internal, package-private, or native initialization path unless a public mutable
scale is an intentional API decision with lifecycle guarantees.

## Finding 4: text and FontMetrics are incomplete

The branch removes direct global-density multiplication from some font paths and
adds double-named methods. The new double accessors currently delegate to integer
metrics, so they are not fractional metrics.

The implementation does not yet prove:

- application of `fontScale`;
- shared shaping between measurement and drawing;
- logical shaped advances;
- leading and vertical metrics from actual fonts;
- fallback, kerning, ligature, and multiline behavior;
- cache separation between logical shaping and physical glyph rasterization;
- preferred-size and baseline equivalence across renderers.

This work remains an active implementation milestone.

## Finding 5: Java macOS launch was recorded as macOS platform proof

The evidence launches the fixture through `totalcross.Launcher` with simulated
scale values. That exercises JavaSE/AWT code on a Mac. It does not execute native
methods marked `@ReplacedByNativeOnDeploy`, the native VM, Skia C++ integration,
or the legacy native font path.

The CMake/Ninja build proves only that `libtcvm.dylib` compiles and links.

Native macOS proof requires all of the following from the same repository
revision:

1. build the SDK;
2. build the macOS `libtcvm.dylib`;
3. compile the fixture against that SDK;
4. deploy the fixture for macOS;
5. place or select the exact freshly built dylib used by the generated app;
6. execute the generated native macOS application;
7. verify exit status, machine-readable assertions, PNGs, and screenshots.

JavaSE/AWT and native macOS are separate validation lanes. Neither substitutes
for the other.

## Finding 6: native macOS content scale is not proven

The added backing-scale refresh is in the AWT `LauncherFrame`. No reviewed change
initializes native macOS screen `Graphics.contentScale` from the native window or
view backing scale.

Skia now consumes the `Graphics` field, but the native screen path may still leave
it at one. Add native macOS scale acquisition and a deployed native assertion
before claiming Retina support.

## Finding 7: scaled Image work is useful but incomplete

Logical and physical dimensions and scale-one defaults are implemented in the
right direction. Remaining proof includes:

- codecs and every row/frame stride;
- transformations and frame extraction;
- caches and texture ownership;
- native-to-Java readback;
- alternating Java/native ownership;
- failure-state preservation;
- natural-size drawing in each renderer;
- physical source rectangles and logical destinations.

The current Java synchronization test does not execute native synchronization.

## Finding 8: the current DANFE test does not validate text

`DanfeScalingTest` draws rectangles and barcode bars. It proves dimensions,
selected Java pixel behavior, and 31 dark runs. It does not draw or measure DANFE
text.

The separate visual fixture draws text, but it was only launched through the Java
Launcher and no screenshot was accepted. There are no automated text-containment,
anti-over-shrinking, baseline, advance, or renderer-equivalence assertions.

Keep the current test as a low-level fixture and add the complete DANFE contract
instead of renaming its limited proof as final acceptance.

## Finding 9: non-Skia native equivalence is not complete

Removing one global-density multiplier and compiling the native target is not
equivalence proof. The branch still needs per-surface scale behavior, cache-key
review, logical metrics, primitive and image behavior, and a native macOS runtime
fixture for the non-Skia configuration when that configuration is supported.

## Finding 10: state and Progress overstate completion

The state file places execution at final handoff while explicitly deferring:

- deployed text containment;
- Java/Skia and non-Skia equivalence;
- native-to-Java pixel readback;
- native application screenshot;
- Android proof.

Those are core acceptance criteria rather than editorial leftovers. The updated
state returns execution to a corrective implementation checkpoint.

## Secondary observation: Double4D

Adding `Double4D.isFinite` is justified because deployed TotalCross code uses the
API. Correcting the existing NaN comparison is technically valid but adjacent to
this feature. Keep it isolated and documented; do not use it as evidence that a
logical rendering milestone is complete.

## Required next sequence

Continue on the existing branch without rewriting history:

1. restore the `USE_WRITE_PIXELS` specialization safely;
2. connect `LayoutUnit` to actual layout and prove root PIXEL compatibility;
3. complete Skia surface semantics and native macOS backing scale;
4. complete logical text, metrics, and fontScale;
5. complete image ownership and native readback;
6. complete the Java renderer;
7. complete the non-Skia native path;
8. deploy and run the full DANFE through the native macOS VM;
9. capture Java and native windows through process-owned CoreGraphics window IDs
   and `/usr/sbin/screencapture`;
10. only after implementation is stable, run final Android validation and any
    optional iOS validation.

No iOS or Android build is required during the implementation checkpoints.
