<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Pre-image master work 01: fix targeted JPEG scaling correctness

This is plan 1 of 2. Execute it before
`.agent/plans/pre-image-optimization-master-02-native-swap.md`.

This ExecPlan follows `AGENTS.md`, `.agent/PLANS.md`, and
`.agents/skills/logical-commits/SKILL.md`. It is self-contained and must be
executed without relying on conversation history.

## Purpose / Big Picture

Create `fix/pre-image-optimization-master` from the current `origin/master` and
fix the adaptive/targeted JPEG metadata bug that can make deferred smooth-scaled
JPEGs render at `target / denominator` instead of the requested extent.

At completion:

- targeted JPEG decode at denominator 2/4/8 keeps correct physical/logical
  metadata;
- fresh decode and cached backing reuse are equivalent;
- complete-raster regressions cover the affected smooth-scale family using an
  independent full-decode reference;
- nearest scale and rotate-scale still force full decode;
- `getHwScaledInstance()` remains a smooth-scaling compatibility alias;
- explicit JPEG factories remain eager;
- the branch is ready for plan 2, which benchmarks `USE_NATIVE_SWAP`.

Do not rebase any image-optimization branch in this plan.

## Branch Contract

Use exactly:

    fix/pre-image-optimization-master

At first execution:

    git fetch origin master
    BASE_SHA=$(git rev-parse origin/master)

Create the branch from that exact SHA and record `BASE_SHA` in state. If the
branch already exists, resume only when its state and history match this plan.
Do not force-push, amend, rebase published commits, update `master`, create tags,
or merge a PR.

Commits are required. Invoke `.agents/skills/logical-commits/SKILL.md` before
every commit. Stage only task paths and preserve unrelated local changes.

## Working Set and Resume Protocol

Commit this plan at:

    .agent/plans/pre-image-optimization-master-01-correctness.md

Plan 2 must also be present at:

    .agent/plans/pre-image-optimization-master-02-native-swap.md

Use:

    .agent/state/pre-image-optimization-master-01-correctness.md
    .agent/evidence/pre-image-optimization-master-01-correctness.jsonl
    .agent/reports/pre-image-optimization-master-01-correctness-editorial.md

After interruption, read the state file first. It must record the current
milestone/slice, `BASE_SHA`, last logical commit, active paths, next exact action,
focused validation completed, deferred validation and reason, and blockers.
Read evidence selectively; never reread all prior logs or repository history to
resume.

Primary source paths:

    TotalCrossSDK/src/main/java/totalcross/ui/image/Image.java
    TotalCrossSDK/src/main/java/totalcross/ui/image/ImageDecodeRequirement.java
    TotalCrossSDK/src/main/java/totalcross/ui/image/EncodedImageSource.java
    TotalCrossSDK/src/test/java/totalcross/ui/image/
    TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageJpegPinchSmokeApp.java
    TotalCrossSDK/build.gradle
    TotalCrossVM/src/nm/ui/skia/skia_image_geometry.cpp
    TotalCrossVM/src/nm/ui/skia/skia_image_geometry_materialize.cpp
    TotalCrossVM/src/nm/ui/image_Image.c
    TotalCrossVM/third_party/jpeg/JpegLoader.c

Inspect only narrow regions around symbols named below.

## Execution, Artifact, Build, and Size Rules

Operate in token-efficient mode. Use `rg`, scoped status/diffs, and narrow file
ranges. Save verbose Gradle/CMake/Ninja/smoke output to local logs and report only
compact summaries and relevant failures.

Every new file must stay below 20 KiB and approximately 600 lines. Do not
refactor existing files merely to reduce their size.

Commit every artifact created specifically for this plan: both ExecPlan files,
state, evidence, editorial report, and any new focused fixture/test source.
Do not commit normal build products, deployed smoke output, caches, binaries, or
verbose logs.

Local build operations are permitted only for the SDK and macOS native runtime,
and only at the end of a related milestone. Native smokes may run only at the
end of a related milestone and at final completion. Do not build Android, iOS,
Windows, or Linux TotalCross targets.

## Current Architecture and Required Invariant

`ImageDecodeRequirement.choose()` deliberately allows reduced JPEG decode for an
eligible `SMOOTH_SCALE` pipeline. Preserve these existing decisions:

- `SMOOTH_SCALE` may select denominator 2, 4, or 8;
- `SCALE` forces denominator 1;
- `ROTATE_SCALE` forces denominator 1;
- unsafe operation orderings continue to fall back to denominator 1.

The bug is at the targeted-decode/cache metadata boundary. A JPEG decoded at
uniform denominator `d` has physical dimensions approximately
`ceil(intrinsic/d)` but keeps the original logical dimensions. The Image must
therefore carry:

    d=1 -> contentScale=1.0
    d=2 -> contentScale=0.5
    d=4 -> contentScale=0.25
    d=8 -> contentScale=0.125

Keep decoder-produced physical width/height exactly, including libjpeg ceiling
rounding for odd dimensions. Keep original encoded-source logical width/height.
Do not derive different X/Y scales from rounded dimensions; JPEG denominator is
uniform.

Repair this invariant where targeted decoded state becomes an Image and where a
cached targeted backing is reconstructed. Do not compensate in
`geometryDrawCompiled()` and do not disable adaptive decode.

`getHwScaledInstance()` currently delegates to smooth scaling. Keep that runtime
behavior. `hwScaleW/hwScaleH` remain presentation-only metadata and are not the
fix.

If Javadocs for `getHwScaledInstance`, `hwScaledBy`, or
`hwScaledFixedAspectRatio` still claim that OpenGL creates an Image sharing the
same texture while only changing `hwScaleW/hwScaleH`, update those comments to
describe the current alias behavior. Do not restore the old backend branch.

Keep `getJpegBestFit()` and `getJpegScaled()` eager. Do not change their decode
policy or exception timing.

## Explicitly Out of Scope

Do not implement or import:

- Phase-2 runtime opacity or `writePixels` policy;
- physical-transform folding or reusable physical materialization;
- GPU-only backing;
- RGB565/GRAY8/ARGB4444 storage;
- runtime color-type specialization;
- cache-budget/mmap/memory-pressure work;
- any rebase of `perf/image-opt-*` branches.

## Progress

- [x] (2026-09-06T23:40:09Z) Milestone 0: created `fix/pre-image-optimization-master` from `origin/master` at `1898014784b2fba5716cc033e49520740b05f0dd`; added resumable state, evidence, and editorial files.
- [x] (2026-09-07T00:25:18Z) Milestone 1: repaired targeted JPEG metadata transactionally at fresh/cached materialization boundaries, added initialization-failure coverage, and completed the Java/macOS regression matrix; implementation commit is `8156f62f9`.
- [x] (2026-09-07T00:25:18Z) Milestone 2: completed the final SDK/macOS gates and documented an ancestor-plus-allowlist handoff for plan 2; the current tip may contain only handoff artifacts after the implementation commit.

## Plan of Work

### Milestone 0 — Bootstrap

Create the branch from freshly fetched `origin/master`. Copy both plan files to
their repository paths. Create concise state/evidence/editorial skeletons for
this plan. Record `BASE_SHA` and the next action.

Run no build. Validate only changed/new files and staged diff:

    python3 scripts/validate-copyright-headers.sh --files <changed files>
    git diff --check --cached

Commit the bootstrap as one logical documentation checkpoint, for example:

    docs(image): add pre-optimization correctness plan

Acceptance: branch base is recorded and a fresh agent can resume from state.

### Milestone 1 — Fix targeted decode metadata and regressions

Inspect only these contracts before editing:

1. `ImageDecodeRequirement.choose()` denominator selection;
2. encoded-root materialization in `Image.java`;
3. `materializeCachedEncodedSource()`;
4. `EncodedImageSource` decoded width/height/denominator/generation fields;
5. targeted decode entry points in `image_Image.c` and `JpegLoader.c`;
6. root `contentScale` use in Skia geometry draw/materialization;
7. `ImageJpegPinchSmokeApp` and its Gradle smoke registration.

Implement one deterministic internal conversion from decoded denominator to
content scale if a helper avoids duplication. It must not become public API.
Use the actual selected `decodedDenominator()`, not the requested target size.

For fresh targeted decode:

- preserve native physical width/height;
- preserve source logical width/height;
- set content scale to `1.0 / decodedDenominator()`;
- preserve generation/cache bookkeeping.

For cached targeted backing reuse, reconstruct exactly the same metadata from
`EncodedImageSource`'s decoded fields and denominator. Full decode stays at
content scale 1.

Add regressions that compare rendered/read-back pixels, not only dimensions.
The broken implementation can allocate a correctly sized output while drawing
content into only a fraction of it.

The reference must be independent: construct a separate JPEG Image, force a
canonical full-decode barrier such as `getPixels()`, then apply the same smooth
transformation. Do not use a reduced `decodeJpegAtDenominatorForTest(d)` result
as the visual reference for another reduced path.

Cover at least:

- denominator 2, 4, and 8;
- plain `getSmoothScaledInstance()`;
- `getHwScaledInstance()`;
- `smooth + alpha`;
- valid `crop + smooth`;
- `smooth + smooth`;
- cached targeted backing reuse;
- direct draw-plan rendering;
- a materialization/readback path;
- odd intrinsic JPEG dimensions;
- destination scale near denominator transition boundaries;
- proof that nearest `SCALE` and `ROTATE_SCALE` still use denominator 1.

Prefer extending existing Image tests and `ImageJpegPinchSmokeApp`. Add a new
small fixture/test only if existing structure would become unclear. For visual
parity use full-raster equality/hash plus focused pixel assertions.

Update stale hardware-scale Javadocs without changing behavior.

Use one logical correctness commit containing implementation plus its focused
regressions. A separate docs commit is allowed only if the Javadoc change is
clearly independent. Suggested correctness subject:

    fix(image): preserve targeted jpeg scale metadata

At milestone end only, run:

    cd TotalCrossSDK
    ./gradlew-agent test --tests 'totalcross.ui.image.*' --no-daemon --console=plain
    ./gradlew-agent dist -x test --no-daemon --console=plain

Then build one Release macOS software-Skia runtime in a dedicated local build
directory:

    cmake -S TotalCrossVM -B build-preopt-macos \
      -DCMAKE_BUILD_TYPE=Release \
      -DTC_GRAPHICS_SOFTWARE=ON \
      -DTC_RENDERER_SKIA=ON \
      -DTC_WINDOWING_SDL=ON -G Ninja
    ninja -C build-preopt-macos

Locate the single freshly built `libtcvm.dylib`, convert its path to absolute,
and run the existing smoke task against that exact file:

    cd TotalCrossSDK
    ./gradlew-agent runImageJpegPinchSmokeMacOS \
      -PtcvmDylib=<absolute-path-to-fresh-libtcvm.dylib> \
      --no-daemon --console=plain

Run an additional Image native smoke only if it directly exercises a changed
path. Do not create another deploy mechanism.

Record concise evidence; never commit the build/smoke logs.

Acceptance:

- denominators 2/4/8 render identically to independent full-decode references;
- targeted `contentScale` matches the denominator invariant;
- odd dimensions are correct;
- cached and fresh targeted backings expose equivalent metadata/output;
- direct draw and materialization paths both pass;
- SCALE/ROTATE_SCALE still force full decode;
- SDK tests/dist and related macOS native smoke pass.

### Milestone 2 — Finalize plan 1 and hand off to plan 2

Inspect `git diff --stat "$BASE_SHA"..HEAD` before reading detailed diffs. Verify
that no Phase-2/3/4 optimization was imported and explicit JPEG factory behavior
was not changed.

Run scoped header validation and `git diff --check`. Repeat an expensive build or
smoke only if production code changed after the Milestone-1 checkpoint.

Update the editorial report with:

- `BASE_SHA` and final plan-1 HEAD;
- exact metadata invariant repaired;
- regression cases and validation actually run;
- deliberate exclusions/remaining optimization work;
- any platform limitations.

Update state/evidence and commit the factual handoff, for example:

    docs(image): complete targeted jpeg correction

Record the final implementation/test commit as
`PLAN1_IMPLEMENTATION_HEAD`. Plan 2 must verify that commit is an ancestor of
the current branch tip, inspect every later commit against the explicit
handoff-only path allowlist, and then record the actual current tip as
`PLAN2_BASE`. Handoff documentation may advance the branch tip; plan 2 must
not require an exact tip or recreate/rebase the branch.

Implementation completed at `8156f62f9`: targeted physical dimensions remain
decoder output, logical dimensions remain encoded-source metadata, and
`contentScale` is `1.0 / decodedDenominator()` for fresh and cached targeted
roots. The follow-up handoff artifacts are limited to the plan/state/evidence/
report files named by plan 2.

## Validation and Acceptance

Every commit must use the logical-commits skill, focused header validation, and:

    git diff --check --cached

Final correctness contract:

- targeted physical dimensions are decoder output;
- logical dimensions are encoded-source logical dimensions;
- `contentScale == 1.0 / decodedDenominator()`;
- cached reuse matches fresh targeted decode;
- smooth scale retains adaptive denominator selection;
- nearest scale and rotate-scale retain denominator 1;
- output matches an independent full-decode smooth reference;
- explicit JPEG factories remain eager;
- hardware-scale aliases keep current runtime behavior.

## Surprises & Discoveries

- Moving full-decode initialization after metadata verification delayed GIF
  frame-count parsing; the final implementation initializes full decodes before
  verification and applies targeted metadata before targeted initialization.
- The existing reduced-JPEG visual tolerance is sensitive to high-frequency
  odd-dimension fixtures, so the independent full-decode parity family uses the
  stable even-dimension fixture while odd dimensions are asserted separately at
  the decoder/cache metadata boundary.

## Decision Log

- Repair metadata at the targeted-decode/cache boundary, not geometry.
- Preserve adaptive smooth decode and denominator-1 nearest/rotate behavior.
- Preserve eager explicit JPEG factories.
- Preserve current `getHwScaledInstance()` alias behavior.
- Keep image-optimization work out of this master-preparation branch.

## Risks and Open Questions

There are no architecture choices left to the executor. Stop and record evidence
instead of redesigning if:

- the selected denominator cannot be obtained reliably at the metadata boundary;
- the fix exposes a separate geometry defect;
- a public API change would be required;
- explicit JPEG factory error timing would change.

## Idempotence and Recovery

Re-running tests/builds may replace local logs but must not duplicate state or
evidence conclusions. Stage explicit paths only. Never clean unrelated worktree
files. Do not push in plan 1 unless needed for repository synchronization; plan 2
contains the authorized CI-benchmark push procedure.

## Outcomes & Retrospective

Plan 1 completed on `fix/pre-image-optimization-master`; the final production
implementation/test commit is `8156f62f9`. The current tip after this handoff
documentation is the starting tip that plan 2 must record as `PLAN2_BASE`
after its ancestor and path-allowlist checks.
Adaptive JPEG smooth selection remains intact; targeted roots now carry the
decoder's physical raster, encoded logical dimensions, and denominator-derived
content scale across fresh and cached paths. The focused SDK suite, SDK
distribution, Release macOS software-Skia build, and exact-dylib JPEG pinch
smoke passed after the transactional correction. The implementation commit's
message check reported one 84-character body line; the no-amend rule preserved
that commit, and all later handoff commit messages were checked separately.
Evidence is recorded in
`.agent/evidence/pre-image-optimization-master-01-correctness.jsonl`.

## Revision Note

Initial revision. This plan isolates correctness work that should land on master
before the image-optimization branch chain is rebased.
