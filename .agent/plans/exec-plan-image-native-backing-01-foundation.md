<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda
SPDX-License-Identifier: LGPL-2.1-only
-->

# Introduce Image backing and native ownership foundations

This ExecPlan follows `AGENTS.md`, `.agent/PLANS.md`, the repository
`logical-commits` skill, and
`.agent/image-native-backing-roadmap.md`. Execute it as plan 1 of 5.

## Purpose / Big Picture

Introduce explicit Image backing contracts and a native Skia backing owner while
preserving current behavior. At the end of this plan, the code can represent
raster-backed and native-backed images without using `pixels == null` as the
state model, but decoded/generated images may still use the legacy raster path
until plan 2.

This plan is deliberately additive. Do not migrate image algorithms yet.

## Working Set and Resume Protocol

Maintain:

- `.agent/state/image-native-backing-01.md` — first read on resume; rewrite it.
- `.agent/evidence/image-native-backing-01.jsonl` — compact append-only results.
- `.agent/archive/image-native-backing-01-history.md` — create only if completed
  detail would otherwise bloat the active plan/state.
- `.agent/reports/image-native-backing-01-editorial.md` — update at milestone
  completion and final completion only.

All new files must stay below 20 KB and about 600 lines.

Start/resume with scoped commands, not a broad worktree dump:

    sed -n '1,180p' .agent/state/image-native-backing-01.md 2>/dev/null || true
    git status --short -- TotalCrossSDK/src/main/java/totalcross/ui/image \
      TotalCrossVM/src/nm/ui TotalCrossVM/src/nm/instancefields.h

If state does not exist, create it with current `HEAD`, active milestone 1, the
paths above, and the next action. Record the starting SHA with
`git rev-parse HEAD`.

## Progress

- [x] Milestone 1: freeze storage/ABI invariants and add focused contract tests.
- [x] Milestone 2: add Java backing/source abstractions without changing native
  authority.
- [x] Milestone 3: add native Skia backing ownership and bridge primitives.
- [x] Close plan 1 and prepare plan 2 state.

Checkpoint 2026-09-02: milestones 1 and 2 are implemented in commits
`e82957614` and `635a30eb3`. The focused SDK image/backing tests and
`dist -x test` gate passed. The first two commit messages contain an
overlong body line reported by the local checker; history was not rewritten.

Checkpoint 2026-09-02: milestone 3 is implemented in commits `481698aff` and
`1f81dc624`. The native bridge, opaque Skia handle lifecycle, ARGB readback,
macOS arm64 native build, SDK backing tests, SDK distribution build, and native
smoke all passed. The first native commit also contains an overlong body line
reported by the local checker; history was not rewritten.

## Current Architecture and Scope

The starting implementation stores `Image.pixels`, `pixelsOfAllFrames`, and
`textureId` directly in `Image`. `ImagePipeline` roots are `EncodedImageSource`
or `RasterImageSource`; raster snapshots clone arrays. Skia image surfaces
currently contain `SkBitmap` plus `SkCanvas`, and `GraphicsPrimitivesSkia_c.h`
can create a Skia surface from `Image_pixels` when `textureId < 0`.

`instancefields.h` indexes Image fields by storage category. Preserve all
existing indexes during this plan. Add new Image object fields after the existing
object sequence only.

The native backing introduced here must not become authoritative for normal Image
content yet. This keeps plan 1 reviewable and lets plan 2 switch authority with a
clear boundary.

Out of scope here:

- decoder migration;
- generated-image allocation migration;
- pipeline execution changes;
- replacing color or geometry algorithms;
- removing `pixels` fields;
- changing PNG/JPEG encoding.

## Plan of Work

### Milestone 1: Freeze contracts and create focused tests

Inspect, without broad rereads, these exact paths and record only facts needed by
later work:

- `Image.java`: field order, constructors, `getGraphics`, `lockChanges`,
  `freeTexture`, materialization, copy/snapshot helpers.
- `ImagePipeline.java`, `ImageSource.java`, `RasterImageSource.java`,
  `EncodedImageSource.java`.
- `Graphics.java` and `ImageDrawingBridge.java` draw-resolution boundary.
- `instancefields.h` Image and EncodedImageSource macros.
- `GraphicsPrimitivesSkia_c.h`, `skia_internal.h`, `skia_surface.cpp`.
- direct native `Image_pixels` users found with one focused `rg`.

Add focused SDK tests for backing state transitions as package-private contracts,
but do not run a Gradle task until this milestone's end. Tests must establish:

- a deferred encoded image has no materialized backing;
- a raster-created Java SE image reports raster backing;
- a backing snapshot is detached for result-producing transforms;
- metadata is independent of backing type;
- no test assumes `synchronized` provides device mutual exclusion.

Do not introduce test-only architecture that must later be removed; use small
package-private inspection hooks only when unavoidable.

Commit checkpoint A after tests/contracts are written and static/header checks
pass. Use a `test(sdk): ...` or similarly focused message. Record SDK execution
as deferred to the milestone gate.

Milestone gate — this is the first allowed build in this milestone:

    cd TotalCrossSDK
    ./gradlew-agent <focused image test task>
    ./gradlew-agent dist -x test

Capture full output in logs. If tests fail because they intentionally describe
future behavior rather than current behavior, the test split was wrong: keep
only tests that describe the additive contract introduced in this milestone.
The gate must be green before milestone 2.

Acceptance:

- required current invariants are captured in state/evidence;
- focused additive tests pass;
- no native behavior changed;
- SDK build passes.

### Milestone 2: Add Java backing abstractions

Create small package-private classes:

    ImageBacking
    RasterImageBacking
    NativeImageBacking
    BackingImageSource

Do not exceed the new-file size limit. Keep `RasterImageSource` temporarily if
needed for incremental compatibility; do not remove it until all call sites use
the backing-aware source.

Required contracts:

`ImageBacking`:

- identifies raster versus native representation without `instanceof` leaking
  throughout `Image`;
- provides dimensions/validity needed by Image invariants;
- provides snapshot semantics through package-private operations;
- contains no public API.

`RasterImageBacking`:

- owns the existing visible-frame and all-frame arrays;
- keeps Java SE behavior equivalent;
- can clone itself for detached pipeline roots;
- does not depend on Skia.

`NativeImageBacking`:

- owns one opaque `long nativeHandle`;
- exposes package-private native bridge methods for lifecycle/snapshot/readback;
- contains no Java raster array;
- releases its handle idempotently on finalization;
- does not use `synchronized` for correctness.

`BackingImageSource`:

- stores an immutable backing snapshot plus the metadata currently carried by
  `RasterImageSource`;
- materializes a raster source on Java SE and a native source on deployed Skia;
- does not allow later source writes to affect a derived image.

Add a transitional `ImageBacking backing` field after the existing Image object
fields. Do not move/remove `pixels` yet. Populate a `RasterImageBacking` for
Java SE-created rasters while preserving legacy fields until plan 5. During the
transition, a single helper owns synchronization between the duplicate legacy
fields and `RasterImageBacking`; do not scatter dual writes.

Use an explicitly named transitional helper such as
`adoptRasterBackingCompatibility(...)`. Mark it for removal in plan 5 state.

Change `snapshotRasterSource()` to create a backing-aware snapshot, but keep
observable pipeline behavior unchanged.

Commit checkpoint B for backing class introduction. Commit checkpoint C for
Image/pipeline adoption if the diff is independently reviewable. Do not combine
unrelated formatting or old Image cleanup.

Milestone gate:

    cd TotalCrossSDK
    ./gradlew-agent <focused image backing/pipeline tests>
    ./gradlew-agent dist -x test

No native build is required unless a native declaration or field macro changed.

Acceptance:

- Java code represents backing explicitly;
- Java SE remains raster-authoritative;
- current deferred pipeline tests pass;
- existing object-field indexes remain unchanged;
- no production behavior relies on a native backing yet.

### Milestone 3: Add native Skia backing core

Create one or more cohesive files under `TotalCrossVM/src/nm/ui/skia/`, each
below 20 KB/~600 lines. Do not split existing large files merely for size.

Implement an opaque native backing object that can own either:

- `sk_sp<SkImage>` immutable content; or
- `sk_sp<SkSurface>` mutable content.

Provide C-facing bridge functions sufficient for later plans:

- create empty raster surface with checked dimensions;
- create immutable backing from native pixel ownership;
- convert immutable image to mutable surface by drawing once;
- make immutable snapshot of a mutable surface;
- query width/height and validity;
- return target canvas for a mutable backing;
- read a row/region in TotalCross ARGB order;
- release backing idempotently.

Use a heap object behind the opaque handle. Do not expose its struct layout to
Java. Reject zero/stale handles safely.

Add `NativeImageBacking_handle` field access to `instancefields.h` for the new
Java class. Do not change Image slot meanings in this plan.

Wire `NativeImageBacking` native methods through the repository's native-method
registration conventions. Keep native function names/signatures small and
purpose-specific; do not add a generic command dispatcher.

Add native unit/probe coverage where existing test infrastructure permits it,
without creating a new platform harness. Test handle lifecycle, surface->snapshot
copy-on-write behavior, row color conversion, and invalid-handle safety.

Commit checkpoint D for the native backing type/lifecycle. Commit checkpoint E
for Java/native bridge registration/tests if separate review improves clarity.

Milestone gate — allowed native/SDK builds only now:

    cmake -S TotalCrossVM -B build-image-backing-01 \
      -DCMAKE_BUILD_TYPE=Release \
      -DCMAKE_OSX_ARCHITECTURES=arm64 \
      -G Ninja
    cmake --build build-image-backing-01 --target tcvm Launcher --parallel

    cd TotalCrossSDK
    ./gradlew-agent <focused image backing tests>
    ./gradlew-agent dist -x test

A native smoke may run only now, after the builds. Keep it minimal: create an
empty native backing through a test bridge, draw/read deterministic pixels, and
release it. Do not migrate application Image behavior in this smoke.

Acceptance:

- native backing owns Skia content independently of `Image.pixels`;
- mutable surface and immutable snapshot behave correctly;
- explicit ARGB readback is correct;
- invalid/released handles fail safely;
- macOS and SDK gates pass.

## Surprises & Discoveries

The existing `skia_surface_test.cpp` is not a configured CMake test target, so
the permitted native smoke used a temporary probe linked against the built
`libtcvm.dylib`. No later architecture is affected.

## Decision Log

- Decision: keep plan 1 additive and preserve legacy Image field indexes.
  Rationale: separate ownership scaffolding from authority migration.
- Decision: one Java wrapper owns one opaque native handle; shared Images share
  the wrapper object instead of maintaining a second Java refcount.
- Decision: do not rely on `synchronized` for native/device correctness.
- Decision: `freeTexture()` is not allowed to destroy canonical backing.

## Validation and Acceptance

Only SDK and macOS builds are permitted, and only at milestone gates above.
Native smoke is permitted only at the end of milestone 3. Between gates use
header validation, scoped diffs, native-method table consistency checks, and
`git diff --check`.

Before every commit follow `.agents/skills/logical-commits/SKILL.md`, including
`validate-headers`, staged diff review, `git diff --check --cached`, and commit
message validation. Record deferred builds in state/commit body.

## Risks and Open Questions

There are no architecture questions left for the agent. Treat these as failure
conditions, not invitations to redesign:

- If the TCVM cannot safely store an opaque pointer in the existing `long`
  native field mechanism, stop and report the concrete runtime limitation.
- If native finalization cannot invoke the release bridge for this wrapper, stop
  and report it; do not replace ownership with a global leak-prone registry.
- If pinned Skia cannot make an image snapshot from the selected surface type,
  use a raster `SkSurface` snapshot path, not a Java pixel array.

## Idempotence and Recovery

Native release must tolerate repeated calls. Creation failures leave handle zero.
Do not delete build caches or unrelated generated files. Do not reset or checkout
unrelated local changes. A failed milestone build may be retried in the same
milestone build directory unless stale configuration is the demonstrated cause.

## Outcomes & Retrospective

Plan 1 is complete at `1f81dc624` (with native implementation at
`481698aff`). Java backing contracts are in
`TotalCrossSDK/src/main/java/totalcross/ui/image/`, including the transitional
`adoptRasterBackingCompatibility` helper and `BackingImageSource`. Native
ownership and bridge code is in `TotalCrossVM/src/nm/ui/image_NativeImageBacking.c`
and `TotalCrossVM/src/nm/ui/skia/skia_image_backing.{h,cpp}`, with native method
registration and field access updates in the existing VM tables.

The focused SDK tests, SDK `dist -x test`, macOS arm64 `tcvm`/`Launcher` build,
and deterministic native backing smoke passed. Native backing is still
additive: `Image.pixels` remains authoritative and native-backed materialization
is intentionally deferred to plan 2. The compatibility helper and legacy raster
fields therefore remain by design.

## Revision Note

This plan is fixed as the first phase of the native Image backing migration.
Change it only if repository policy or a proven runtime impossibility requires a
material revision; record the reason once.
