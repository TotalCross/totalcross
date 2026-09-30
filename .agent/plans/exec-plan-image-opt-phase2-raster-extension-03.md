<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Phase 2 raster extension 03: integrated validation and final closeout

This ExecPlan follows `AGENTS.md`, `.agent/PLANS.md`, and
`.agents/skills/logical-commits/SKILL.md`.

Execute only after extensions 01 and 02 are complete.

## Purpose / Big Picture

Close Phase 2 after all eight lossless/raster controls exist. Prove their
interactions on authoritative macOS software Skia, verify the GPU-negative
invariants on the available Android device when a branch-matching runtime can be
used without an Android build, and produce the final Phase 3 handoff.

No new optimization architecture is introduced here.

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

Read `.agent/design/image-optimization-benchmark-protocol.md` before final
benchmark classification. Do not reread full historical plans.

The state must identify the final extension-02 runtime HEAD.

## Progress

- [x] Run all-eight macOS S1/S2/S3 closeout.
- [x] Run required 200-sample/RSS diagnostics.
- [x] Validate cross-feature correctness/invariants.
- [x] Validate Android GPU invariants or record explicit build-constrained deferral.
- [x] Finalize Phase 2 state/evidence/editorial and Phase 3 base handoff.

## Current Architecture and Acceptance Contract

Phase 2 controls:

    0  DECODE_ZERO_COPY
    1  RASTER_OPACITY_METADATA
    2  RASTER_OPAQUE_WRITE_PIXELS
    3  RASTER_ROW_READBACK
    4  RASTER_DIRECT_COLOR_MATERIALIZATION
    13 RASTER_TARGET_COLORTYPE_CONVERSION
    14 RASTER_PHYSICAL_VARIANT_CACHE
    15 RASTER_PHYSICAL_IDENTITY_FOLDING

Features 5-12 remain outside this combined S3 except diagnostic accounting when
explicitly needed for measurement.

Final architecture order must be:

    compile existing geometry
      -> derive physical plan
      -> exact identity fold when possible
      -> otherwise observe repeated eligible pure geometry
      -> materialize at most one final physical variant
      -> choose target color during that materialization when eligible
      -> reuse through existing conservative writePixels
      -> otherwise normal Skia draw

GPU targets must never materialize/read back CPU pixels just to reach these
raster paths.

Source mutation/decode-generation change invalidates opacity/derived variants as
defined by prior plans.

Phase 3 remains source compact formats (RGB565/GRAY8/ARGB4444). Later phases
retain global cache budget/pressure, GPU-only backing, and mmap.

## Final macOS Benchmark Contract

Use macOS Release software Skia as primary/authoritative evidence.

Run combined:

- S1: frozen Phase 1 base
  `a8a9480bd61aa510de423569af494d8dde69e8f2`, using the committed true-base
  adapter without modifying its production tree;
- S2: final Phase 2 runtime, all optimization controls explicitly disabled;
- S3: same final runtime, only 0-4 and 13-15 enabled.

The combined workload must exercise:

- JPEG/PNG decode;
- opacity proof;
- one physical-identity draw;
- repeated transformed draw that materializes/hits the variant;
- target-color draw;
- writePixels reuse;
- getPixels/readback;
- mutation invalidation.

Record full fixture/output hashes and all relevant counters.

Use:

- three warmup batches;
- 60 measured samples initially;
- >=30 ms timed work per sample;
- hashes outside timing.

Escalate to 200 when CV >5%, result is near acceptance, or post-disabled
elapsed/RSS is >5%.

If >5% RSS persists after 200, capture matched:

    vmmap -summary
    RSS
    physical footprint

before classification.

Because S1 and S2 are different production revisions, if S2/S1 elapsed or RSS
exceeds 5%, run a final-runtime pre-equivalent versus post-disabled control
before attributing overhead to disabled features. Follow the existing Phase 2
closeout precedent; do not label allocator/residency revision drift as
disabled-feature overhead without this control.

All compact samples/reports/evidence are committed. Build logs are not.

## Cross-feature Correctness Matrix

Prove at final runtime:

- adaptive JPEG denominator 2/4/8 pixels equal independent full decode;
- fresh/cached targeted decode equivalence;
- targeted JPEG already at required physical size is not resampled again;
- `200 -> 100 logical @ scale2` folds identity and never materializes;
- `400 -> 100 logical @ scale2` materializes once after reuse threshold;
- target color is selected during final materialization, not through a second
  full raster;
- mutation invalidates opacity, pending observation, and materialized variant;
- `sourceDecodeGeneration` change prevents stale reuse;
- RGBA->BGRA target conversion is exact;
- opaque RGBA->RGB565 target conversion is exact within RGB565 target semantics,
  without changing source/readback representation;
- translucent/unknown alpha does not use opaque target-color fast path;
- alpha/clip/matrix/bounds/color/rotation/hwScale guards fall back;
- materialization barriers (`getPixels`, encoding, `getGraphics`) remain correct;
- `getJpegBestFit()` and `getJpegScaled()` remain eager;
- ordinary direct draw does not trigger readback;
- APPLY_COLOR2 alpha parity and zero-copy failure/retry contracts still pass.

## Android GPU Validation Contract

A physical Android device is available via `adb`. Android production rendering
is GPU/OpenGL ES. Do not create Android software raster.

This execution is explicitly forbidden from building Android.

Run:

    adb devices

Proceed only if a branch-matching Android runtime containing the final Phase 2
runtime is already available through an installed/deployed artifact without
invoking an Android build. Record package/artifact/version/SHA provenance.

If no matching runtime exists, record:

    DEFERRED — matching Android runtime requires a prohibited Android build

Do not build Android to remove this deferral.

If a matching runtime exists, use the existing deploy/smoke path plus `adb`.
Run a draw-only GPU workload with raster features enabled and assert:

    writePixelsHits == 0
    targetColorAttempts == 0
    targetColorMaterializations == 0
    physicalVariantLookups == 0
    physicalVariantMaterializations == 0
    physicalIdentityHits == 0

Also assert row/full readback counters do not increase during draw-only
execution.

CPU-side decode/opacity/readback features may be exercised separately when the
workload explicitly requests them.

A correctness failure on a matching runtime is a blocker. Missing matching
runtime under the build restriction is a documented deferral.

Commit concise evidence only; do not commit verbose logcat.

## Platform Policy for This Execution

The shared protocol permits Windows/Linux hosted comparative evidence, but the
explicit task allows builds only for SDK and macOS. Therefore:

- do not add/run Windows builds;
- do not add/run Linux builds;
- do not run QEMU ARM benchmarks;
- record Windows/Linux Phase 2 evidence as intentionally deferred by this task;
- do not weaken macOS acceptance because hosted evidence is absent.

## Plan of Work

### Milestone 0 — Verify final runtime

Run:

    git switch perf/image-opt-phase2-raster
    git status --short
    sed -n '1,180p' .agent/state/image-opt-phase2-raster-extension.md

Confirm extensions 01/02 are complete and record exact final runtime HEAD.

No build for bootstrap.

### Milestone 1 — Final macOS build, correctness, and combined benchmark

At this related milestone end run the allowed builds:

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

Use the exact dylib for the related native image smoke family.

Run the cross-feature correctness matrix and combined S1/S2/S3 benchmark.

Apply 60-to-200 and memory rules exactly. Create matched final-runtime controls
when required.

Commit samples/report/evidence:

    test(image): validate combined raster optimizations

Do not create a runtime fix solely for an unconfirmed cross-runtime RSS shift.

### Milestone 2 — Android GPU invariant check

Run `adb devices`.

If a matching runtime is available without building Android, execute the GPU
draw-only and focused CPU-side checks above. Commit:

    test(image,android): validate gpu raster invariants

If not available, commit only the concise deferral evidence:

    docs(image): record android validation deferral

Do not build Android.

No SDK/macOS build is needed for an evidence-only milestone.

### Milestone 3 — Final Phase 2 handoff

Update continuation state/evidence/archive/editorial with factual final results.

Also add a concise pointer/result to existing Phase 2 state/editorial if useful;
do not rewrite historical benchmarks.

Editorial must distinguish:

- delivered behavior;
- authoritative macOS measurements;
- Android executed versus deferred;
- Windows/Linux deferred by explicit build restriction;
- Phase 3 compact-source work;
- later cache/lifecycle/mmap work.

Record that Phase 3 must resolve the frozen tip of
`perf/image-opt-phase2-raster` when it starts and record that exact SHA in Phase
3 provenance. Do not create another self-referential "final SHA" loop inside the
commit that changes the tip.

Commit:

    docs(image): complete phase two raster extension

No extra build if only docs/evidence changed after Milestone 1.

## Validation and Acceptance

Build operations are allowed only for SDK and macOS native, at related milestone
ends. Do not build Android, Windows, Linux, or iOS.

Native smokes run only at related milestone ends or final execution completion.

Before every commit:

1. invoke logical-commits skill;
2. stage only intended paths;
3. validate focused copyright headers;
4. run `git diff --check --cached`;
5. inspect staged stat/diff;
6. create a non-amended logical commit;
7. validate commit-message format.

Do not push.

Commit every directly generated plan artifact, benchmark sample/report,
state/evidence/archive/editorial update, and test source. Do not commit ordinary
build logs, binaries, deploy outputs, build directories, or verbose logcat.

New files <=20 KiB or approximately 600 lines. Do not refactor existing files
only for size.

Final acceptance:

- all eight controls stable/default-disabled;
- IDs 13-15 have proven intended behavior;
- exact lossless parity/correctness passes;
- no unexplained confirmed >5% disabled regression;
- identity folding precedes materialization;
- transformed variant memory is one-entry bounded;
- target color produces no second full intermediate;
- raster-only paths remain absent on GPU when Android evidence is executable,
  otherwise the build-constrained deferral is explicit;
- Phase 2 is frozen for sequential Phase 3 rebase.

## Surprises & Discoveries

Record only findings that affect final classification/handoff.

A hosted-platform absence is not a surprise in this task; it is an explicit
build-policy deferral.

## Decision Log

- Decision: macOS software Skia is authoritative performance evidence.
  Rationale: shared protocol and explicit build policy.
- Decision: Android validation is GPU-only and conditional on an already
  matching runtime.
  Rationale: no Android build and no unsupported raster target.
- Decision: final-runtime controls are required before blaming >5% cross-runtime
  RSS on disabled features.
  Rationale: preserve the corrected Phase 2 measurement standard.
- Decision: freeze Phase 2 after this closeout.
  Rationale: compact source formats belong to sequential Phase 3.

## Risks and Open Questions

No architecture choices remain.

Stop/report if final correctness fails, a matching Android GPU runtime shows a
raster fast-path hit/readback, or a confirmed disabled regression cannot be
explained/fixed without changing scope.

Do not expand into Phase 3 or later lifecycle work.

## Idempotence and Recovery

Never overwrite historical samples. Corrective reruns get new directories and a
supersession reason.

After interruption read extension state, verify HEAD, inspect only named active
paths, and reuse completed evidence whose SHA/workload/regime still matches.

Preserve unrelated local changes.

## Outcomes & Retrospective

At completion summarize delivered IDs 13-15, combined measured behavior,
platform evidence/deferrals, and the exact next action for Phase 3.

## Revision Note

Third and final sequential continuation plan. It closes Phase 2 without adding
new optimization architecture.
