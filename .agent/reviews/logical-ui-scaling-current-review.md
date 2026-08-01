<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Review of feat/logical-ui-scaling at c8a3152c4

## Executive assessment

The branch is materially closer to the intended logical-scaling architecture and
should be continued. It is 82 commits ahead of the recorded base and preserves
the corrected embedded path, actual native macOS validation, fractional Skia
metrics, and destination-owned scale fields.

The current blocker is not valid under the maintainer decision recorded in this
bundle. SkShaper, HarfBuzz, ICU, and SkParagraph are outside this task.

Progress is broadly in the right direction, but Milestone 3R is not complete.
Two production corrections and one evidence reconciliation are required before
continuing to later milestones.

## Work aligned with the plan

### USE_WRITE_PIXELS

The guarded direct-write specialization was restored instead of deleted. It now
requires an opaque full-source same-size alpha-255 copy, an identity canvas
matrix, no saved clipping state, and integral in-bounds physical coordinates.
All other cases use `drawBitmapRect`.

This is the expected correction for embedded Linux compatibility.

### Native macOS scaling

The SDL path requests high-DPI backing, records physical drawable dimensions,
derives a surface content scale, exposes logical Settings dimensions, and
initializes screen Graphics with that scale.

The deployed fixture recorded a real Retina result with matching source and
deployed dylib hashes. This is native macOS runtime evidence rather than a Java
Launcher substitute.

### SkFont text model

The native drawing path already uses:

    effectiveLogicalSize = Font.size * Graphics.fontScale

for drawing and dirty bounds, while the canvas applies `contentScale`.

`SkFont::getMetrics`, `SkFont::measureText`, and
`SkTextBlob::MakeFromText` are present and sufficient for this task's simple
text contract. No additional text engine is needed.

### Controls and focused validation

Label, Button, Edit, and MultiEdit contain useful destination-font-scale work.
Tests prove content-scale invariance and font-scale growth for selected preferred
sizes and cursor geometry. Native assertions cover fractional metrics, overload
compatibility, accents, and common DANFE text.

Keep this work.

## Finding 1: destination measurement occurs at the wrong stage

The intended process is:

    effective logical SkFont size = Font.size * fontScale
    measureText at that effective size
    draw with the same effective size
    canvas applies contentScale

Current control helpers instead do:

    measureText at Font.size
    multiply returned advance or metrics by fontScale

The two forms are approximately linear but they are not the same contract. They
can diverge because of metric quantization, font implementation details, and
renderer behavior. They also prevent proving that measurement and drawing used
the same SkFont configuration.

Required correction:

1. Keep public `FontMetrics` as scale-one logical metrics for compatibility.
2. Add an internal destination-aware text measurement path.
3. On Skia, call `SkFont::measureText` and `SkFont::getMetrics` with
   `Font.size * Graphics.fontScale`.
4. Draw with that same effective size, typeface, and UTF-16 interpretation.
5. Make control layout call the destination-aware path rather than multiplying a
   previously measured result.
6. Freeze integer compatibility rounding only after the effective-size
   measurement.

Do not add a shaper.

## Finding 2: TotalCross wrapping is not destination-font-scale aware

`Label.split` and automatic splitting still call
`Convert.insertLineBreak(maxWidth, fm, text)`. That algorithm receives only the
scale-one FontMetrics object and therefore does not necessarily use the same
effective-size measurement as drawing.

TotalCross must retain responsibility for wrapping. Update its line-breaking
algorithm or add an overload/internal measurement adapter so every candidate line
is measured through the destination-aware text path.

Required tests:

- wrapping is unchanged when only contentScale changes;
- increasing fontScale changes wrap points when expected;
- explicit newline handling remains TotalCross behavior;
- multiline line height uses effective SkFont metrics;
- the measured line accepted by the wrapper fits when drawn;
- no SkParagraph dependency or delegated paragraph layout is introduced.

## Finding 3: PIXEL layout conversion mutates the origin too early

In `Control.setRect`, the code converts `cli.x` and `cli.y` to physical units,
then calculates width and height from expressions containing the already
converted origin:

    cli.x = toLayoutPixels(cli.x);
    cli.width = toLayoutPixels(cli.x + cli.width) - cli.x;

This mixes a physical origin with a logical width. Existing tests use a zero
client origin, so they do not expose the defect.

Preserve the original logical left, top, right, and bottom, then convert each
edge exactly once. Add a test with nonzero insets or client origin at scales
1.5, 2, and 3.

Do not mark Milestone 1R complete until this test and the deployed native PIXEL
migration fixture pass.

## Finding 4: plan, state, and evidence disagree

The actual branch head is `c8a3152c482d6f3ac511e8295cc400b555aeecae`,
but the state file records `f820d4540`.

The state and ExecPlan outcomes describe many validations after the native
font-scale drawing slice, while the append-only evidence file stops at that
slice. Before further implementation:

- update the reviewed head;
- remove the external shaping blocker;
- append concise evidence for later verified commits after checking their logs;
- do not invent evidence for results whose logs are unavailable;
- keep Java, native compile, and deployed native runtime labels distinct.

## Finding 5: public scale mutation remains unresolved

`Graphics.setScales` is public and tests and fixtures call it directly. The
architectural contract says the destination owns the scales.

Before final API acceptance, either:

- document and approve a public lifecycle API with clear invalidation behavior;
  or
- move mutation behind an internal runtime/test bridge while retaining public
  getters.

This does not block the next text slice, but it must not be forgotten.

## Finding 6: remove unsupported typography claims

The current fixture names the `"AV"` measurement a kerning advance and the old
plan requires ligatures, fallback, bidi, and complex shaping.

Under the approved scope, tests may measure `"AV"` or accented Portuguese as
ordinary representative strings, but must not claim they prove:

- kerning feature application;
- ligature formation;
- bidi;
- script shaping;
- font fallback;
- cluster semantics.

Rename assertions and reports accordingly.

## Finding 7: Java fractional metrics remain limited

The native Skia path exposes actual fractional metrics. The Java fallback
`stringWidthD` and `charWidthD` currently return integer measurements as double.

This is acceptable as a temporary Java-renderer limitation, but the plan must not
claim cross-renderer fractional equality. Milestone 5R must either obtain
fractional Java metrics from the Java rendering context or document and test an
approved tolerance/rounding boundary.

## Remaining work after reconciliation

1. Correct edge conversion for nonzero PIXEL client origins.
2. Measure at the effective destination SkFont size.
3. Route TotalCross wrapping and multiline calculations through that measurement.
4. Complete text-control audits and tests.
5. Complete images and bidirectional synchronization.
6. Complete Java and supported non-Skia renderer behavior.
7. Run complete Java and native macOS DANFE lanes and screenshots.
8. Run final Android validation and handoff.

## Verdict

The branch is suitable for continuation. R0 and the core native macOS scaling
work align with the plan. M3R contains strong foundations but must be corrected
to measure before scaling rather than scale a previous measurement, and TotalCross
wrapping must consume that same destination-aware metric path.

No SkShaper, HarfBuzz, ICU, SkParagraph, or alternate text engine work belongs in
this branch.
