<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Phase 2 raster extension 02: cache target-aware physical variants

This ExecPlan follows `AGENTS.md`, `.agent/PLANS.md`, and
`.agents/skills/logical-commits/SKILL.md`.

Execute only after
`.agent/plans/exec-plan-image-opt-phase2-raster-extension-01.md`.
After completion execute
`.agent/plans/exec-plan-image-opt-phase2-raster-extension-03.md`.

## Purpose / Big Picture

Finish the missing Phase 2 raster representation work by implementing:

- `RASTER_TARGET_COLORTYPE_CONVERSION`;
- `RASTER_PHYSICAL_VARIANT_CACHE`.

Reuse the physical-plan/identity work from extension 01. Keep one bounded
derived raster per source backing, materialize only after observed reuse, and
never pull Phase 3 compact-source storage or later global cache policy into this
plan.

## Working Set and Resume Protocol

Work only on:

    perf/image-opt-phase2-raster

Read first after interruption:

    .agent/state/image-opt-phase2-raster-extension.md

Supporting files:

    .agent/evidence/image-opt-phase2-raster-extension.jsonl
    .agent/archive/image-opt-phase2-raster-extension-history.md
    .agent/reports/image-opt-phase2-raster-extension-editorial.md
    .agent/benchmarks/image-opt-phase2-raster-extension/

The state must record extension 01 final HEAD and the exact pre-implementation
S1 production SHA for IDs 13 and 14.

Read `.agent/design/image-optimization-benchmark-protocol.md` only when
preparing/validating a benchmark. Do not reread the full original Phase 2 plan.

## Progress

- [x] Verify extension 01 handoff and capture target/variant S1.
- [x] Implement target-color conversion.
- [x] Implement physical variant caching.
- [ ] Record isolated S2/S3 evidence for both features.
- [ ] Hand off exact runtime state to extension 03.

## Current Architecture and Scope

Extension 01 must have delivered:

- `RASTER_PHYSICAL_IDENTITY_FOLDING` (ID 15);
- an ephemeral `RasterPhysicalPlan` or equivalent decision object;
- exact physical-identity proof before materialization.

Activate only these remaining reserved controls:

    13 RASTER_TARGET_COLORTYPE_CONVERSION
    14 RASTER_PHYSICAL_VARIANT_CACHE

Keep IDs 0-15 stable and default-disabled.

The source backing remains authoritative. A derived variant must not change:

- logical dimensions/content scale;
- adaptive JPEG semantics;
- public ARGB readback;
- explicit eager JPEG factories;
- Image/Pipeline identity;
- source ownership/mutation semantics.

RGB565 in this plan is only a derived raster-target format. Source-storage
RGB565/GRAY8/ARGB4444 remains Phase 3.

Do not add global cache budgets, LRU, memory pressure, mmap, GPU-only backing,
compressed textures, or public APIs.

Primary paths:

    TotalCrossVM/src/nm/ui/skia/skia_image_backing_internal.h
    TotalCrossVM/src/nm/ui/skia/skia_image_backing.cpp
    TotalCrossVM/src/nm/ui/skia/skia_image_geometry.cpp
    TotalCrossVM/src/nm/ui/skia/skia_image_geometry_materialize.cpp
    TotalCrossVM/src/nm/ui/skia/skia.h
    TotalCrossVM/src/nm/ui/ImageTestAccounting_c.h
    TotalCrossSDK/src/main/java/totalcross/ui/image/Image.java
    TotalCrossSDK/src/main/java/totalcross/ui/image/NativeImageBacking.java
    TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/

## Derived Variant Contract

Add one derived-raster slot per `NativeImageBackingRecord` (or one equivalent
owned object). Do not add another cache elsewhere.

The slot contains:

- one immutable CPU-accessible Skia raster/snapshot;
- source `generation`;
- `sourceDecodeGeneration`;
- normalized frame/source rectangle;
- final physical width/height;
- target `SkColorType`;
- an explicit signature of eligible geometry;
- a flag/type distinguishing color-only from transformed variant.

Keep one pending observation key/count beside the slot.

Build the key/signature from explicit stable scalar plan fields and operation,
parameter, and dimension values. Do not hash raw structs, pointer addresses, or
padding. Encode relevant floating values by exact bit representation for key
identity; do not use fuzzy cache equality.

`markMutated()` clears the materialized variant and pending observation.
A changed `sourceDecodeGeneration` also prevents reuse.

Different eligible keys replace the pending observation. Replacing an existing
materialized key evicts the old one before installing the new candidate.
Skia smart pointers own variant memory.

Failure to create/install a new candidate must leave the source and any
previously valid state consistent and fall back to normal draw.

## Admission Policy

Use exactly this deterministic policy:

- first eligible occurrence: remember key, normal draw;
- second consecutive occurrence of same key: materialize/install;
- subsequent identical occurrences: cache hit;
- mutation, decode-generation change, or key change resets observation.

Do not materialize during decode, Image construction, or first use.

## Target Color Conversion Contract

ID 13 creates a color-only same-size derived variant for repeated
physical-identity opaque draws.

Require all:

- supported software-raster target;
- physical identity already proven;
- effective alpha 255;
- source proven `OPAQUE`;
- no color/filter/fade/touch-up/rotation stage;
- existing matrix/clip/save/bounds/writePixels safety conditions;
- target `SkColorType` is RGBA8888, BGRA8888, or RGB565.

Behavior:

- RGBA8888 is the no-conversion/control case;
- BGRA8888 may create a same-size BGRA variant;
- RGB565 is allowed only for proven opaque content;
- translucent/unknown or unsupported targets fall back.

Use Skia conversion/draw primitives. Do not create a duplicate manual pixel
conversion loop when Skia can preserve exact semantics.

The variant keeps `OPAQUE` proof, never replaces the primary source backing,
and never changes `getPixels()` output.

On reuse, feed the variant to the same conservative writePixels helper. Do not
duplicate writePixels eligibility.

Add test-only counters equivalent to:

    targetColorAttempts
    targetColorMaterializations
    targetColorHits
    targetColorFallbacks
    targetColorConvertedBytes

## Physical Variant Cache Contract

ID 14 extends the same slot to repeated non-identity pure geometry.

Support only:

- frame selection/layout;
- crop;
- SCALE;
- SMOOTH_SCALE.

Do not cache:

- ROTATE_SCALE;
- color/alpha/fade/touch-up/transparency-key stages;
- `hwScaleW/H` or `rootHwScaleW/H` values other than 1;
- unsafe matrix/clip/save/bounds state;
- GPU targets.

Use existing materializer dimensions and frame-layout contract:

    ceil(outputWidth * outputContentScale)
    ceil(outputHeight * outputContentScale)

When ID 13 is disabled, materialize the physical variant in the normal RGBA8888
representation. When IDs 13 and 14 are both enabled and target conversion is
eligible, materialize directly in the final target color type. Never create a
full RGBA transformed raster followed by a second full target-colored raster.

Canonical transformed case:

    source 400x400 physical
    smooth -> 100x100 logical
    outputContentScale 2
    => final 200x200 physical

Expected:

    draw 1: normal
    draw 2: one 200x200 materialization
    draw 3+: variant hit, then writePixels if independently eligible

Canonical identity case:

    source 200x200
    smooth -> 100x100 logical
    outputContentScale 2

Expected:

    identity fold wins
    physicalVariantMaterializations == 0

Add counters equivalent to:

    physicalVariantLookups
    physicalVariantHits
    physicalVariantMisses
    physicalVariantMaterializations
    physicalVariantEvictions
    physicalVariantBytes

Destination memory is not scratch memory.

## GPU Safety Contract

Raster specialization must remain target-canvas-specific. Source CPU
accessibility alone is never sufficient.

Use `TC_GRAPHICS_SOFTWARE` as an outer gate where it matches the current build
split, and keep the shared eligibility logic target-aware.

This plan does not build/test Android. Extension 03 validates the GPU negative
invariants.

## Benchmark Contract

Extension 01 already committed target-color and physical-variant workloads
before implementation.

For each feature use:

- S1: exact pre-implementation production commit;
- S2: post runtime, target disabled, every other optimization disabled;
- S3: same runtime, only target enabled.

Use 3 warmups, 60 samples, >=30 ms timed batches, and shared 60-to-200/RSS
rules. Keep hashes/parity checks outside timing.

Target-color evidence covers:

- RGBA control;
- BGRA conversion;
- RGB565 opaque conversion;
- translucent fallback;
- mutation invalidation.

Physical-variant evidence covers:

- first use;
- second-use materialization;
- repeated hits;
- key/size replacement;
- mutation/decode-generation invalidation;
- 400 -> 100 @ scale2 materialization;
- 200 -> 100 @ scale2 zero materialization.

Commit compact raw samples/reports. Do not commit build logs/binaries.

## Plan of Work

### Milestone 0 — Verify handoff and capture both S1 baselines

Verify:

    git switch perf/image-opt-phase2-raster
    git status --short
    sed -n '1,180p' .agent/state/image-opt-phase2-raster-extension.md

Confirm extension 01 is complete and its final HEAD matches state.

Use its final production SHA as both target-color and physical-variant S1. The
benchmark sources must already exist.

Do not build only for bootstrap. Capture S1 during the next related
milestone-end SDK/macOS build before changing runtime code. Record exact
production/harness SHAs.

Commit only a needed state/evidence handoff update:

    docs(image): resume target variant work

### Milestone 1 — Implement target-color variants

Implement the one-slot scaffold and ID 13 color-only behavior. Do not implement
transformed variants yet.

Tests/smokes must prove:

- second repeated BGRA draw materializes exactly once;
- later BGRA draws hit;
- RGB565 requires proven opacity;
- translucent/unknown falls back;
- RGBA control avoids unnecessary conversion;
- mutation invalidates;
- changed target color type replaces observation/variant;
- `getPixels()` remains canonical ARGB;
- disabled ID 13 preserves prior output/path.

Commit runtime/tests:

    perf(image): cache target color variants

At milestone end run only allowed builds:

    cd TotalCrossSDK
    ./gradlew-agent test --tests 'totalcross.ui.image.*' \
      --no-daemon --console=plain
    ./gradlew-agent dist -x test --no-daemon --console=plain

    cmake -S TotalCrossVM -B build/image-opt-phase2-extension-macos -G Ninja \
      -DCMAKE_BUILD_TYPE=Release \
      -DTC_GRAPHICS_SOFTWARE=ON \
      -DTC_RENDERER_SKIA=ON \
      -DTC_WINDOWING_SDL=ON
    ninja -C build/image-opt-phase2-extension-macos tcvm Launcher

Use the exact dylib and run related native image smokes.

Capture S2/S3, apply escalation rules, and commit:

    test(image): record target conversion results

### Milestone 2 — Implement transformed physical variants

Extend the same slot for ID 14. Follow the exact eligibility/admission/key rules
above.

Tests/smokes must prove:

- 400 -> 100 @ scale2: one second-use materialization then hits;
- 200 -> 100 @ scale2: identity fold, zero materializations;
- physical-size/key change replaces the slot;
- source mutation clears slot/observation;
- `sourceDecodeGeneration` change prevents stale reuse;
- frame/crop keys cannot alias;
- rotation/color/alpha/hwScale never cache;
- IDs 13+14 together create one final target-colored materialization;
- disabled ID 14 preserves pre-implementation behavior.

Commit:

    perf(image): cache physical raster variants

At milestone end repeat the allowed SDK/macOS build and related native smokes.

Capture physical-variant S2/S3 and commit:

    test(image): record physical variant results

### Milestone 3 — Hand off to integrated closeout

Update continuation state/evidence/archive/editorial with factual results only.
Record:

- final HEAD;
- exact S1/S2/S3 production SHAs;
- authoritative sample counts;
- target-color and variant counters/results;
- any matched RSS diagnostics;
- next plan path.

Commit:

    docs(image): hand off raster closeout

No additional build if runtime did not change after Milestone 2.

## Validation and Acceptance

Build only SDK and macOS native targets, and only at related milestone ends.
Do not build Android, Windows, Linux, or iOS.

Native smokes run only at related milestone ends or final execution completion.

Before every commit invoke the logical-commits skill, validate focused headers,
run `git diff --check --cached`, inspect staged paths/diff, and commit only the
logical slice. Do not amend/rewrite/push.

New files must stay <=20 KiB or approximately 600 lines. Do not refactor
existing large files merely to meet this limit.

Acceptance:

- IDs 13/14 remain independently switchable/default-disabled;
- exact output parity holds;
- disabled S2 has no confirmed >5% regression after escalation;
- target conversion never changes primary backing/readback semantics;
- one-entry variant memory is bounded and mutation-safe;
- identity folding always wins before variant admission;
- no global cache/lifecycle/compact-storage behavior is introduced.

## Surprises & Discoveries

Record only findings that alter remaining work.

Do not change the two-observation threshold because a first materialization is
slow. Measure and report the trade-off.

If a target color type cannot preserve exact semantics with current Skia,
fallback for that target and record it; do not force a lossy/manual path.

## Decision Log

- Decision: one derived slot per source backing.
  Rationale: bounded reuse without Phase 4 cache policy.
- Decision: materialize on the second identical eligible occurrence.
  Rationale: avoid one-shot penalties.
- Decision: target RGB565 is derived target representation only.
  Rationale: source compact storage belongs to Phase 3.
- Decision: cache only frame/crop/scale/smooth-scale.
  Rationale: exclude rotation/color/dynamic presentation risk.
- Decision: identity folding precedes cache admission.
  Rationale: eliminate work before allocating work.

## Risks and Open Questions

No architecture choices remain for the executor.

Stop/report if:

- invalidation cannot be made transactional;
- target conversion changes pixel semantics;
- physical materialization needs two full intermediates;
- disabled behavior regresses beyond protocol;
- implementation requires public API or compact source storage.

## Idempotence and Recovery

Never overwrite historical or extension-01 samples. Put corrective reruns in a
new subdirectory with the supersession reason.

Failed materialization must publish nothing partial and fall back safely.

After interruption read extension state, verify branch/HEAD, inspect only active
paths, and rerun only incomplete/required escalations. Preserve unrelated local
changes and untracked files.

## Outcomes & Retrospective

At completion record measured behavior for IDs 13/14 and the exact handoff to
extension 03.

## Revision Note

Second sequential Phase 2 continuation plan. It implements the bounded derived
raster representation; integrated/platform closeout is intentionally isolated in
extension 03.
