<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda
SPDX-License-Identifier: LGPL-2.1-only
-->

# Execute Image color and pixel mutations on native Skia backing

This ExecPlan follows `AGENTS.md`, `.agent/PLANS.md`, the repository
`logical-commits` skill, and
`.agent/image-native-backing-roadmap.md`. Execute it only after plan 3 passes.

## Purpose / Big Picture

Remove the remaining dependency on `Image.pixels` for color, alpha, fade,
brightness/contrast, exact color replacement, and transparent-color operations on
deployed Skia. Keep these operations as semantic pipeline state. Fuse exact Skia
filters into draw when possible; otherwise perform an exact native backing
transform without a Java full-image raster.

## Working Set and Resume Protocol

Use `.agent/state/image-native-backing-04.md` first and compact evidence
`.agent/evidence/image-native-backing-04.jsonl`. Inspect only Image color methods,
`ImagePipeline`, native geometry/materialization executor, Skia backing/readback,
and focused tests.

Any new filter/exact-transform native file must remain below 20 KB/~600 lines.
Do not grow a single switch file beyond this limit; split filter compilation from
exact native passes by responsibility.

## Progress

- [x] Milestone 1: migrate fade/alpha/touch-up filter candidates.
- [ ] Milestone 2: migrate applyColor and applyColor2.
- [ ] Milestone 3: migrate exact color-key mutations and composition/barriers.
- [ ] Close plan 4 and prepare plan 5 state.

## Current Architecture and Scope

Plans 1-3 provide native backing and direct Skia geometry execution. Color nodes
still resolve through eager routines or direct loops. The operations in scope are:

    FADE
    ALPHA
    APPLY_COLOR
    APPLY_COLOR2
    APPLY_FADE
    TOUCH_UP
    CHANGE_COLORS
    SET_TRANSPARENT_COLOR

Preserve all existing result-producing versus in-place semantics, multi-frame
quirks, call-time frame selection, alpha behavior, and integer formulas.

## Fixed execution policy

For every candidate filter, differential-test the exact TotalCross output for
boundary values. Use this predetermined fallback order if a built-in Skia filter
is not byte-equivalent where output is observable:

1. exact pinned-Skia runtime/filter implementation;
2. exact native transform over Skia/native pixel storage producing a new native
   backing.

Never use a Java full-image array fallback on deployed Skia.

Exact-match operations (`CHANGE_COLORS`, `SET_TRANSPARENT_COLOR`) use the exact
native transform path by design; do not approximate them through premultiplied
color filters.

## Plan of Work

### Milestone 1: Fade, alpha, and touch-up

Implement `APPLY_FADE` as an exact RGB channel mapping with alpha preserved.
Prefer 8-bit lookup tables when the pinned Skia color-filter API applies them
without changing hidden RGB/alpha semantics. The operation affects only the frame
selected at call time for multi-frame images; represent this frame scope in the
compiled pipeline and apply it only when that frame is rendered/materialized.

Implement result-producing `FADE(backColor)` using the historical interpolation
formula and preserved alpha. Differential-test channel rounding; if a color
matrix differs by rounding, use exact LUT/runtime/native implementation.

Implement `ALPHA(delta)` with the historical rule:

- alpha 0 stays 0 and the pixel remains otherwise unchanged;
- nonzero alpha becomes clamp(alpha + delta);
- RGB remains unchanged.

An alpha lookup table is preferred if exact.

Implement `TOUCH_UP(brightness, contrast)` by precomputing the same per-channel
mapping as the current code. Use one 256-entry mapping per required channel and
identity alpha when the Skia table filter is exact. Preserve the current
brightness/contrast formulas rather than substituting Skia's generic contrast.

For direct draw, compose compatible filters with the geometry draw plan. For a
materialization barrier, apply the same filter semantics while rendering the one
final native surface.

Add exhaustive small channel-domain tests where cheap (0, 1, boundaries, 127,
128, 254, 255 plus representative combinations) and multi-frame APPLY_FADE tests.
Defer Gradle/native execution to the gate.

Commit checkpoint A for fade/alpha. Checkpoint B for touch-up.

Milestone gate:

    cmake -S TotalCrossVM -B build-image-backing-04-m1 \
      -DCMAKE_BUILD_TYPE=Release \
      -DCMAKE_OSX_ARCHITECTURES=arm64 -G Ninja
    cmake --build build-image-backing-04-m1 --target tcvm Launcher --parallel

    cd TotalCrossSDK
    ./gradlew-agent <focused fade/alpha/touch-up tests>
    ./gradlew-agent dist -x test

Run a native color smoke only after the gate. Compare selected output pixels from
direct draw and save/readback for the same pipeline.

Acceptance:

- operations no longer access `Image.pixels` on native Skia;
- direct draw and materialized output agree;
- frame-scoped APPLY_FADE is preserved;
- SDK/macOS gate and smoke pass.

### Milestone 2: ApplyColor and ApplyColor2

`APPLY_COLOR` must preserve the current multiplier formulas, clamp behavior,
alpha preservation, and the rule that fully transparent pixels remain
byte-identical. Compute channel mappings from the same formula. Use a Skia filter
only if it can preserve the alpha-zero bypass exactly; otherwise use the fixed
runtime/native exact fallback.

`APPLY_COLOR2` requires source analysis. Implement it as a two-phase native
operation at execution time:

1. evaluate all pipeline nodes before APPLY_COLOR2 to a native source suitable
   for analysis, materializing one native intermediate only when unavoidable;
2. scan the correct full source/frame strip natively to find the brightest opaque
   pixel using the existing `Color.getBrightness` weighting and tie behavior;
3. derive `hiR`, `hiG`, `hiB`, and `hi` exactly as current code, including zero
   replacement with 255;
4. apply the current RGB/optional-alpha formula through an exact filter/runtime
   effect or native transform;
5. continue subsequent pipeline nodes.

Do not move APPLY_COLOR2 ahead of prior nodes and do not cache its analysis across
a prior semantic mutation unless the cache key is the exact immutable source
backing plus preceding pipeline identity.

For multi-frame content, analyze the same full all-frames storage the legacy code
uses. Preserve current frame state after in-place mutation.

Add differential tests with opaque, transparent, grayscale, zero-channel, tied
brightness, multi-frame, and `0xAA` alpha-control colors.

Commit checkpoint C for APPLY_COLOR. Checkpoint D for APPLY_COLOR2 analysis and
execution.

Milestone gate:

    cmake -S TotalCrossVM -B build-image-backing-04-m2 \
      -DCMAKE_BUILD_TYPE=Release \
      -DCMAKE_OSX_ARCHITECTURES=arm64 -G Ninja
    cmake --build build-image-backing-04-m2 --target tcvm Launcher --parallel

    cd TotalCrossSDK
    ./gradlew-agent <focused applyColor/applyColor2 tests>
    ./gradlew-agent dist -x test

Run native smoke after the gate with decode -> applyColor/applyColor2 -> save and
verify deterministic output pixels.

Acceptance:

- both operations are native-backing operations on deployed Skia;
- alpha-zero and brightness-analysis semantics are preserved;
- no Java full-image raster is created;
- direct draw/save outputs agree;
- gates pass.

### Milestone 3: Exact color replacement, transparent color, and composition

Implement `CHANGE_COLORS(from,to)` as an exact native ARGB comparison/replace
pass. Do not use approximate Skia color matching. Operate on unpremultiplied
TotalCross ARGB values; convert explicitly at the backing boundary.

Implement `SET_TRANSPARENT_COLOR(color)` with exact current color-key semantics,
including alpha and frame behavior. Preserve the public `transparentColor`
metadata contract.

These nodes remain deferred until execution. When encountered during a direct
screen draw, the exact-pass node may materialize one native intermediate backing;
subsequent draw-fusible geometry/filter nodes continue without Java rasterization.
Do not materialize earlier than necessary.

Add a pipeline executor rule that groups consecutive draw-fusible geometry/filter
nodes, inserts native intermediate barriers only for exact/source-analysis nodes,
and preserves node order. A long chain must never create more native
intermediates than the number of unavoidable exact/source-analysis barriers plus
one final barrier when an output API requests it.

Test mixed sequences such as:

    crop -> smooth scale -> applyFade -> changeColors -> rotate -> alpha

and:

    applyColor -> applyColor2 -> setTransparentColor -> save PNG

Verify direct draw versus saved/readback selected pixels.

Commit checkpoint E for exact passes. Checkpoint F for executor composition and
mixed regression tests.

Milestone gate:

    cmake -S TotalCrossVM -B build-image-backing-04-m3 \
      -DCMAKE_BUILD_TYPE=Release \
      -DCMAKE_OSX_ARCHITECTURES=arm64 -G Ninja
    cmake --build build-image-backing-04-m3 --target tcvm Launcher --parallel

    cd TotalCrossSDK
    ./gradlew-agent <focused mixed image pipeline tests>
    ./gradlew-agent dist -x test

Run mixed-pipeline native smoke only after the gate.

Acceptance:

- all Image color/mutation nodes execute without `Image.pixels` on native Skia;
- exact nodes preserve byte semantics;
- mixed pipeline ordering is unchanged;
- native intermediates occur only at required exact/analysis boundaries;
- SDK/macOS gate and smoke pass.

## Surprises & Discoveries

Record only pinned-Skia API limitations or proven differential mismatches that
affect the fixed fallback choice. Do not treat a mismatch as permission to relax
legacy semantics.

## Decision Log

- Decision: built-in filters are conditional on exact differential behavior.
- Decision: APPLY_COLOR2 performs native source analysis in pipeline order.
- Decision: exact color replacement/color-key operations use native exact passes.
- Decision: no color operation may fall back to a Java full-image raster on
  deployed Skia.

## Validation and Acceptance

Only SDK and macOS builds at milestone gates. Native smoke only after gates. Run
logical-commit/header/diff checks before every commit; record build deferral until
the gate.

## Risks and Open Questions

No architecture is left open. If a pinned Skia filter changes rounding or
premultiplication semantics, use the specified exact fallback. If runtime effects
are not compiled into the pinned Skia build, skip directly to native exact pass;
do not change Skia dependency configuration in this plan.

## Idempotence and Recovery

Exact-pass allocation failure leaves the previous immutable backing/pipeline
unchanged and retryable. Source-analysis caches must be invalidated when preceding
pipeline identity changes. Temporary native buffers are released on every error
path.

## Outcomes & Retrospective

Record operations migrated, which ones use built-in filters versus exact native
passes, commit SHAs, and gate/smoke outcomes. Do not paste LUTs or raw image dumps.

## Revision Note

This plan completes operation migration. Pixel field/readback/encoding retirement
is intentionally plan 5.
