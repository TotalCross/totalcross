<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Eliminate `Image4D` and unify `Image`

This ExecPlan follows `AGENTS.md` and `.agent/PLANS.md`.

This plan is for the Luna coding agent. Execute it imperatively. Do not depend on chat history or prior agent memory. Preserve unrelated local changes. Commit at the explicit checkpoints and keep the state file current so another Luna session can resume without reconstructing the investigation.

## Purpose / Big Picture

Eliminate `TotalCrossSDK/src/main/java/totalcross/ui/image/Image4D.java` and make `totalcross.ui.image.Image` the single Java source definition used by JavaSE and deployed TotalCross applications.

Completion means: `Image4D.java` is gone; generic `*4D` replacement remains for other classes; deployment converts `Image.java` directly; JavaSE behavior remains equivalent to the current `Image.java`; deployed behavior remains equivalent to the current `Image4D.java`; every native `Image_*` field macro keeps the same VM index; Java/native dual implementations use `@ReplacedByNativeOnDeploy` where appropriate; and a deployed macOS smoke application runs against a freshly built `libtcvm.dylib` and passes native image ABI assertions.

Do not implement lazy loading, deferred image operations, `ImageResource`, new density policy, sampling changes, or image-quality fixes. Keep this refactor behavior-preserving and independently reviewable.

## Working Set and Resume Protocol

Create and maintain these files:

- `.agent/state/eliminate-image4d.md`: rewrite after every logical commit. Record active milestone, last commit, active paths, last focused validation, deferred validation, blockers, exact next action, and one resume command.
- `.agent/evidence/eliminate-image4d.md`: append compact validation records only: timestamp, commit, command, status, concise result, and log/artifact paths.
- `.agent/archive/eliminate-image4d-history.md`: use only when completed milestone detail would make active state noisy. Do not read it during normal resumption.
- `.agent/reports/eliminate-image4d-editorial.md`: update only at major milestone completion and final completion.

Initial execution: read `AGENTS.md` and `.agent/PLANS.md` in full; read `.agent/guides/macos-native-runtime-validation.md` before creating or running macOS native smoke; record `git rev-parse HEAD`; then create state/evidence files.

Normal resume: read the state file first, run its resume command, read only the active milestone below, inspect only active paths, and expand investigation only when a validation failure or decision rule requires it. Do not repeat the original `Image` versus `Image4D` investigation after context compaction unless relevant files changed after the commit recorded in state.

## Known Baseline and Hard ABI Contract

Planning reference: `df4903620873ee40ba5227dbe045160bcaa71386`. Do not assume execution starts there.

`TotalCrossSDK/src/main/java/tc/tools/converter/J2TC.java` implements generic `4D` replacement: if `Xxx4D.class` exists, ordinary `Xxx.class` is skipped, the suffix is removed, and the replacement becomes deployed class `Xxx`. Preserve this generic mechanism.

`TotalCrossSDK/proguard.txt` already treats `*4D` classes as implementation classes. Do not retain a deploy-visible `Image4D` compatibility stub; it would reactivate replacement behavior.

Treat the current `Image` macros in `TotalCrossVM/src/nm/instancefields.h` as a hard ABI contract.

I32 indices:

    0  surfaceType
    1  width
    2  height
    3  frameCount
    4  currentFrame
    5  widthOfAllFrames
    6  transparentColor
    7  useAlpha
    8  alphaMask
    9  lastAccess
    10 textureId
    11 logicalWidth
    12 logicalHeight

Object indices:

    0 pixels
    1 pixelsOfAllFrames
    2 comment
    3 gfx
    4 changed
    5 instanceCount

Value64/double indices:

    0 hwScaleW
    1 hwScaleH
    2 contentScale

The converter groups fields by VM storage category. Preserve relative declaration order inside each category. Keep ABI-sensitive fields visibly grouped and documented in unified `Image.java`.

Known split: `Image4D.java` owns deployed-sensitive state such as `lastAccess`, `textureId`, `changed`, `instanceCount`, and `master`; `Image.java` contains JavaSE/AWT/ImageIO/simulator code; `@ReplacedByNativeOnDeploy` makes the deployed method native; native image behavior is primarily in `TotalCrossVM/src/nm/ui/image_Image.c` and `TotalCrossVM/src/nm/ui/ImagePrimitives_c.h`; native entry points already target deployed class `totalcross.ui.image.Image`.

## Critical Invariants

1. Do not change existing `Image_*` indices in `instancefields.h`.
2. Keep `surfaceType` at the index expected by generic image/control surface detection.
3. Keep `contentScale` as backing resolution and `hwScaleW/H` as visual transforms.
4. Keep `new Image(width,height)` fixed-pixel scale-1 behavior and `Image.createLogical(...)` semantics.
5. Keep current JavaSE format support and current deployed PNG/JPEG native decode behavior.
6. Keep current multi-frame behavior and deployed texture/change-sharing behavior.
7. Do not remove generic `*4D` conversion.
8. Do not add public API only to ease unification.
9. Do not call a JavaSE/AWT run on macOS native validation.
10. Native macOS acceptance must use a deployed app and the dylib built from the same tested revision.

If preserving field indices appears impossible, stop before editing `instancefields.h`, record the blocker, and require maintainer review.

## Progress

- [x] Record starting revision and baseline focused validation.
- [x] Add converter-level `Image` field ABI guard; the guard also exposed and corrected the pre-existing `Image4D.hashCode` ordering shift.
- [x] Make `Image.java` directly deployable while `Image4D` still exists.
- [x] Align `Image.java` state/behavior with deployed `Image4D` semantics.
- [x] Remove `Image4D.java` and prove direct conversion; isolate the JavaSE-only nested reader through `ImageLoader4D`.
- [x] Run deployed native macOS field-ABI smoke against a fresh Release `libtcvm.dylib`.
- [ ] Clean stale references and complete final validation.

## Initial Reconnaissance

Perform this once and summarize only findings needed later in `.agent/state/eliminate-image4d.md`.

    git rev-parse HEAD
    git status --short -- \
      TotalCrossSDK/src/main/java/totalcross/ui/image/Image.java \
      TotalCrossSDK/src/main/java/totalcross/ui/image/Image4D.java \
      TotalCrossSDK/src/main/java/tc/tools/converter \
      TotalCrossVM/src/nm/instancefields.h \
      TotalCrossVM/src/nm/ui/image_Image.c \
      TotalCrossVM/src/nm/ui/ImagePrimitives_c.h

    rg -n '\bImage4D\b|image/Image4D' \
      TotalCrossSDK TotalCrossVM scripts .agent \
      --glob '!build/**' --glob '!**/.gradle/**'

    rg -n 'java\.awt|javax\.imageio|ImageIO|BufferedImage|ImageReader|Launcher\.' \
      TotalCrossSDK/src/main/java/totalcross/ui/image/Image.java

    rg -n '@ReplacedByNativeOnDeploy|\bnative\b' \
      TotalCrossSDK/src/main/java/totalcross/ui/image/Image.java \
      TotalCrossSDK/src/main/java/totalcross/ui/image/Image4D.java

    rg -n 'TC_API void tuiI_|Image_[A-Za-z0-9_]+\(' \
      TotalCrossVM/src/nm/ui/image_Image.c \
      TotalCrossVM/src/nm/ui/ImagePrimitives_c.h \
      TotalCrossVM/src/nm/instancefields.h

Record only: fields unique to each class; signature differences; methods Java in `Image` but native in `Image4D`; JavaSE-only types reachable from candidate deployed bytecode; and native entry points without a signature-compatible candidate method. Do not paste full diffs.

If active source paths have unrelated local edits, preserve them. If the task conflicts with those same lines and cannot be adapted safely, stop and report the conflict. Never reset or checkout user changes.

## Milestone 1 — Lock the current field ABI

### Goal

Add a focused converter test proving the deployed `Image` field layout before removing the replacement class.

### Work

Add `TotalCrossSDK/src/test/java/tc/tools/converter/ImageFieldAbiTest.java` or an equivalently focused test. Reuse existing converter test helpers. Inspect the resulting `TCClass`, not only Java reflection, because VM indices are created by converter field grouping.

Assert exactly the I32, object, and value64 sequences in this plan. Failure messages must name storage category, expected field/index, and actual field/index. At this milestone the test may obtain deployed `Image` through existing `Image4D` replacement. Structure the helper so the same assertion can later be applied to direct `Image.class` conversion.

If inherited fields are exposed separately, assert final runtime category indices. Inspect `GfxSurface` once if required and record the inherited contribution in state.

### Validation

    cd TotalCrossSDK
    ./gradlew-agent test \
      --tests 'tc.tools.converter.ImageFieldAbiTest' \
      --no-daemon --console=plain
    cd ..
    git diff --check

Expected: ABI test passes against the current replacement; no production behavior changed.

### Commit checkpoint 1

    test(compiler): lock image native field ABI

Update state with commit hash and next action.

## Milestone 2 — Make `Image.java` independently deployable

### Goal

Remove JavaSE-only obstacles from bytecode that must be converted once `Image4D` is absent, while keeping `Image4D` as the production fallback.

### Work and decision rules

Build a method-signature parity matrix once. For every difference:

1. If both sides are platform-neutral Java with equivalent behavior, keep one implementation in `Image`.
2. If `Image` has JavaSE behavior and TCVM already has a signature-compatible native implementation, keep the Java body and add `@ReplacedByNativeOnDeploy`.
3. If a Java body references AWT, ImageIO, `Launcher`, or another simulator-only type, move those dependencies behind a small JavaSE helper or a body replaced on deploy.
4. Do not annotate a method unless a matching native deployed signature is registered.
5. If private JavaSE/deployed signatures differ, trace constructors and callers before selecting one. Prefer changing private/internal Java signatures to changing native ABI or public API.
6. Preserve public constructors and methods. Do not add a public overload just to silence conversion errors.

Pay special attention to `Image(String)`, `Image(Stream)`, `Image(byte[])`, and `Image(byte[],int)`: current JavaSE and deployed parsing paths differ. Make all constructors work on JavaSE and convert without pulling unsupported desktop classes into deployed executable bytecode.

When useful, move AWT/ImageIO code to a package-private helper such as `totalcross.ui.image.ImageJavaSESupport`. Keep AWT/ImageIO imports out of `Image.java` when practical.

Add or extend a converter test that converts `Image.class` directly without allowing `Image4D.class` replacement. Prove: direct conversion succeeds; expected annotated methods become native; deployed executable bodies do not require JavaSE-only classes; converted name is `totalcross/ui/image/Image`; and the ABI helper can inspect the direct candidate.

Do not delete `Image4D.java` until direct conversion passes.

If an unsupported JavaSE class remains, identify the exact method. If it has a native counterpart, isolate/annotate it. If it is required on deployed platforms without a native counterpart, port the current `Image4D` behavior; do not stub it.

### Validation

    cd TotalCrossSDK
    ./gradlew-agent test \
      --tests 'tc.tools.converter.ImageFieldAbiTest' \
      --tests 'tc.tools.converter.*Image*' \
      --tests 'totalcross.ui.image.*' \
      --no-daemon --console=plain
    ./gradlew-agent dist -x test --no-daemon --console=plain
    cd ..
    git diff --check

### Commit checkpoint 2

    refactor(sdk): make Image directly deployable

Keep `Image4D.java` present. Update state.

## Milestone 3 — Align unified `Image` state with native runtime state

### Goal

Make `Image.java` carry every field and state-management behavior required by native code without changing existing indices.

### Work

Use `Image4D.java` as source of truth for ABI-sensitive deployed state. Reorder/add `Image` fields until the direct-conversion candidate has exactly the required category sequences. Add this warning above the ABI-sensitive field block:

    // ABI-sensitive: keep storage-category order synchronized with
    // TotalCrossVM/src/nm/instancefields.h.

Preserve at least `lastAccess`, `textureId`, `changed`, `instanceCount`, `master`, pixel/frame fields, alpha state, logical size, `contentScale`, and hardware scale fields. Port deployed Java-side behavior from `Image4D` when native code does not supply it, especially copy/share/finalization state and texture ownership/change tracking. Do not duplicate behavior already equivalent in `Image`.

If importing deployed sharing behavior would alter JavaSE semantics, retain the fields but isolate platform-specific behavior using the smallest existing platform distinction. Do not remove ABI fields.

Run `ImageFieldAbiTest` immediately after every field reorder. If any existing macro field shifts, fix `Image.java`; never change `instancefields.h` to follow an accidental new order.

### Validation

    cd TotalCrossSDK
    ./gradlew-agent test \
      --tests 'tc.tools.converter.ImageFieldAbiTest' \
      --tests 'totalcross.ui.image.*' \
      --no-daemon --console=plain
    ./gradlew-agent dist -x test --no-daemon --console=plain
    cd ..
    git diff --check
    python3 scripts/validate-copyright-headers.sh --files \
      TotalCrossSDK/src/main/java/totalcross/ui/image/Image.java

Include any new helper in copyright validation.

### Commit checkpoint 3

    refactor(sdk): align Image with native runtime state

Keep `Image4D.java` through this checkpoint. Update state/evidence.

## Milestone 4 — Remove `Image4D` and switch deployment to `Image`

### Goal

Delete the replacement source and prove ordinary deployment converts `Image.java` directly with the same ABI.

### Work

Delete `TotalCrossSDK/src/main/java/totalcross/ui/image/Image4D.java`. Do not modify generic `4D` selection in `J2TC.java`; absence of `Image4D.class` should be sufficient.

Search remaining references:

    rg -n '\bImage4D\b|image/Image4D' \
      TotalCrossSDK TotalCrossVM scripts \
      --glob '!build/**' --glob '!**/.gradle/**'

Update stale source/type references and native comments. Keep generic `4D` logic. Ignore generated output. Keep historical docs only if still unambiguous. Do not rename native C entry points solely because `Image4D` disappeared.

If a baseline SDK jar is available, run compatibility checking:

    cd TotalCrossSDK
    ./gradlew-agent aggregateCompatibilityCheck \
      -PaggregateCompatibilityBaseline=<baseline-sdk.jar> \
      --no-daemon --console=plain

Continue if `Image4D` is outside supported published API or existing artifact boundaries classify `*4D` as internal. If tooling reports removal as a supported public binary break, do not add a deploy-visible stub; record the report and stop for maintainer policy review.

Deploy a minimal fixture and verify deployment no longer reports replacing `Image` by its `4D` class. Do not enable verbose per-class deploy output unless diagnosis requires it.

### Validation

    cd TotalCrossSDK
    ./gradlew-agent test \
      --tests 'tc.tools.converter.ImageFieldAbiTest' \
      --tests 'tc.tools.converter.*Image*' \
      --tests 'totalcross.ui.image.*' \
      --no-daemon --console=plain
    ./gradlew-agent dist -x test --no-daemon --console=plain
    cd ..
    test ! -f TotalCrossSDK/src/main/java/totalcross/ui/image/Image4D.java
    git diff --check

Do not commit until the same exact ABI assertions pass from direct `Image` conversion.

### Commit checkpoint 4

    refactor(sdk): remove Image4D replacement class

Treat this as an ABI checkpoint. Update plan Progress and state.

## Milestone 5 — Native macOS field-ABI smoke

### Goal

Run a deployed macOS app against a freshly built Release `libtcvm.dylib` and prove unified `Image` interoperates with existing native field macros. This milestone is mandatory; do not substitute `totalcross.Launcher`.

### Fixture and assertions

Add a focused fixture, preferably `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageAbiSmokeApp.java`, plus only tiny deterministic resources under `TotalCrossSDK/src/smokeTest/resources/image-abi/`.

Emit one concise machine-readable result containing at least: `fixture`, `commit`, `renderer`, `imageClass`, `constructorPass`, `decodePass`, `logicalDimensionsPass`, `physicalDimensionsPass`, `framePass`, `colorMutationPass`, `resizePass`, `textureUploadPass`, `textureRecreatePass`, `pngRoundTripPass`, and `overallPass`. Fail deterministically on any assertion failure.

Exercise these existing behaviors:

1. `new Image(7,5)` must report logical/pixel `7x5` and scale `1`.
2. `Image.createLogical(7,5,2)` must report logical `7x5`, physical `14x10`, scale `2`.
3. Load a tiny PNG by path and validate native decode dimensions.
4. Load equivalent encoded data through the supported stream/byte constructor path and validate dimensions, proving constructor/native-parse bridging.
5. Execute `changeColors` or another native pixel mutation and verify deterministic observable output.
6. Execute `applyColor` or another mutation that updates changed state.
7. Call `getSmoothScaledInstance` and validate dimensions.
8. Exercise frame count/current frame and `getFrameInstance()` or equivalent copy path.
9. Call `applyChanges()` before drawing to exercise pixels, frame state, `textureId`, and `changed`.
10. Draw the image in the native app.
11. Call `freeTexture()`, draw again, and require success to exercise texture recreation.
12. Export PNG, reconstruct an `Image`, and validate current dimension/equality semantics.
13. Allow normal derived/shared-image cleanup during shutdown; do not add timing-sensitive GC assertions.

Do not add public field-access APIs for the smoke. `ImageFieldAbiTest` is exact index proof; native macOS smoke is runtime macro/interoperability proof. Require both.

### Smoke Gradle tasks

Add focused tasks to `TotalCrossSDK/build.gradle` using repository-native macOS smoke conventions:

    compileImageAbiSmoke
    deployImageAbiSmokeMacOS
    runImageAbiSmokeMacOS

Locate reusable mechanics first:

    rg -n 'libtcvm\.dylib|tcvmDylib|deploy.*MacOS|run.*MacOS|native.*smoke' \
      TotalCrossSDK/build.gradle TotalCrossSDK/src/smokeTest scripts .agent/guides

`compileImageAbiSmoke` must compile against the current SDK artifact. `deployImageAbiSmokeMacOS` must create a native macOS TotalCross application. `runImageAbiSmokeMacOS` must require `-PtcvmDylib=<absolute path>`, use exactly that dylib via the repository-supported copy/selection mechanism, launch the generated executable directly, capture stdout/stderr/exit status, verify runtime identity, and fail unless `overallPass=true`.

Do not use `open`. Do not invoke `totalcross.Launcher` in the native run task.

### Native macOS commands

Run from repository root:

    mkdir -p artifacts/eliminate-image4d/logs

    (
      cd TotalCrossSDK
      ./gradlew-agent dist -x test --no-daemon --console=plain \
        > ../artifacts/eliminate-image4d/logs/sdk-dist.log 2>&1
    )

    cmake -S TotalCrossVM -B build-image-abi \
      -DCMAKE_BUILD_TYPE=Release -G Ninja \
      > artifacts/eliminate-image4d/logs/macos-cmake.log 2>&1

    ninja -C build-image-abi tcvm \
      > artifacts/eliminate-image4d/logs/macos-tcvm.log 2>&1

    TCVM_DYLIB="$(find "$PWD/build-image-abi" -type f \
      -name 'libtcvm.dylib' -print -quit)"
    test -n "$TCVM_DYLIB"
    test -f "$TCVM_DYLIB"
    git rev-parse HEAD
    shasum -a 256 "$TCVM_DYLIB"

    ./TotalCrossSDK/gradlew-agent -p TotalCrossSDK \
      compileImageAbiSmoke deployImageAbiSmokeMacOS \
      --no-daemon --console=plain \
      > artifacts/eliminate-image4d/logs/macos-smoke-deploy.log 2>&1

    ./TotalCrossSDK/gradlew-agent -p TotalCrossSDK \
      runImageAbiSmokeMacOS \
      -PtcvmDylib="$TCVM_DYLIB" \
      --no-daemon --console=plain \
      > artifacts/eliminate-image4d/logs/macos-smoke-run.log 2>&1

If this wrapper layout does not support `-p`, run the same tasks from `TotalCrossSDK/` and keep the dylib path absolute.

The run task must verify: native executable exists; tested dylib exists; deployed copy hash matches source when copied; `otool -L <fixture executable>` or equivalent repository evidence confirms runtime resolution; no loader fallback/error occurred; exit status is zero; and `overallPass=true`.

Keep full logs only under `artifacts/eliminate-image4d/logs/`; record concise summaries in evidence.

If native build/deploy fails before fixture launch because of an unrelated missing prerequisite, use only repository-prescribed macOS bootstrap. If smoke infrastructure itself is broken, reproduce at the starting revision when practical, record the blocker, and stop for maintainer review. Do not substitute JavaSE output.

This is Level 3 ABI validation under `AGENTS.md`.

### Commit checkpoint 5

    test(sdk,macos): cover unified Image native ABI

Commit smoke fixture/tasks only after they pass against unified `Image`. Update state, evidence, Progress, and editorial report.

## Milestone 6 — Cleanup and final validation

Search again:

    rg -n '\bImage4D\b|image/Image4D' \
      TotalCrossSDK TotalCrossVM scripts \
      --glob '!build/**' --glob '!**/.gradle/**'

Remove stale production comments/tests implying `Image4D` still implements `Image`; keep generic `4D` converter behavior.

Because substantial first-party code may move from `Image4D` into `Image`, follow copyright/provenance instructions in `AGENTS.md`. Run the provenance audit when repository policy requires it. Do not auto-approve an audit.

Final focused commands:

    cd TotalCrossSDK
    ./gradlew-agent test \
      --tests 'tc.tools.converter.ImageFieldAbiTest' \
      --tests 'tc.tools.converter.*Image*' \
      --tests 'totalcross.ui.image.*' \
      --no-daemon --console=plain
    ./gradlew-agent dist -x test --no-daemon --console=plain
    cd ..
    git diff --check

Run changed-file copyright validation from the scoped diff. Repeat Milestone 5 native smoke at the final production commit if production code changed after the last successful smoke. Do not repeat it for comment/plan/evidence-only changes; record the deferral reason.

Final acceptance requires all of the following: `Image4D.java` absent; direct `Image.java` conversion passes; exact original field indices pass; no `Image_*` macro index changed; focused JavaSE image tests pass; SDK `dist` succeeds; macOS Release VM builds; deployed macOS ABI fixture runs against fresh verified dylib; all smoke assertions pass; generic `4D` conversion remains for other classes; no lazy/density/quality redesign is mixed in; `git diff --check` passes; changed-file copyright validation passes.

### Commit checkpoint 6

If cleanup after checkpoint 5 contains logical source/test changes, commit:

    refactor(sdk): finish Image implementation unification

Do not create an empty cleanup commit.

## Decision Log

- Decision: preserve all existing `instancefields.h` indices. Rationale: native code addresses `Image` by category index; shifts are ABI breaks. Date: 2026-08-27.
- Decision: remove `Image4D`, not generic `*4D` conversion. Rationale: other replacement classes still use it. Date: 2026-08-27.
- Decision: use `@ReplacedByNativeOnDeploy` only where a matching native method exists. Rationale: one source class without changing native entry points. Date: 2026-08-27.
- Decision: require both converter exact-index proof and deployed native macOS smoke. Rationale: static layout and runtime macro interoperability catch different failures. Date: 2026-08-27.
- Decision: do not keep a deploy-visible `Image4D` stub. Rationale: it would reactivate replacement. Date: 2026-08-27.
- Decision: defer lazy image work. Rationale: unification must be behavior-preserving groundwork. Date: 2026-08-27.
- Decision: keep the legacy `hashCode` cache after the ABI-sensitive I32 prefix. Rationale: its old declaration shifted `logicalWidth` and `logicalHeight`; relocating it preserves the native macro indices without changing the cache contract. Date: 2026-08-27.
- Decision: retain the JavaSE reader as `Image$ImageLoader` and provide `Image$ImageLoader4D` for deployment. Rationale: deployment expands referenced classes, so AWT/ImageIO must be removed from the converted class graph while JavaSE format support remains unchanged. Date: 2026-08-27.
- Decision: validate the macOS launcher through copied-dylib hash equality plus `otool -L` and loader-symbol evidence. Rationale: this launcher loads `libtcvm.dylib` by name instead of declaring it as a Mach-O load command. Date: 2026-08-27.

## Validation Policy

Use the smallest validation sufficient for each slice. Milestones 1-2 normally use Level 2. Milestone 3 reruns exact ABI test after every field reorder and uses Level 3 at the boundary. Milestones 4-5 are Level 3 ABI/runtime checkpoints with mandatory native macOS execution. Repeat expensive native smoke after checkpoint 5 only when production code changes.

Do not run `clean` by default. Redirect verbose Gradle/CMake/Ninja output to named logs and report concise failures only.

## Risks and Explicit Decision Criteria

JavaSE-only dependency: if direct conversion reaches AWT/ImageIO/simulator classes, identify the exact method and isolate it behind a JavaSE helper or matching native-replaced method. Never package unsupported desktop code into deployed `Image`.

Signature drift: trace constructor/caller behavior before changing private parse/load signatures. Prefer adapting private Java code to changing native ABI. Do not change public signatures without explicit maintainer approval.

Field ordering: if any existing native macro field changes VM index, fix Java declaration order; never edit `instancefields.h` to follow an accidental new order.

Texture/change sharing: if JavaSE lacks deployed state, add the state before deleting the fallback. Preserve copy/finalization behavior and verify texture free/recreate in native smoke.

`Image4D` visibility: if supported API compatibility tooling treats removal as a real published binary break, stop for maintainer policy review. Do not create a named stub that defeats the goal.

Native smoke identity: reject an otherwise successful run if runtime identity is ambiguous. Require explicit dylib path/hash and executable resolution evidence.

## Idempotence and Recovery

Never use destructive recovery commands such as `git reset --hard` or `git checkout --`.

Before commits inspect only active paths:

    git status --short -- <active paths>
    git diff --stat -- <active paths>
    git diff -- <active paths>

If validation is interrupted, rerun that command. Do not rerun earlier expensive validation unless production code changed afterward. If `build-image-abi` is stale, rerun CMake configuration first; remove only that generated build directory if stale state is proven. Never delete source, `TotalCrossVM/deps/totalcross-depot-tools`, or dependency caches just to clean the worktree.

After every commit, state must record commit hash, checkpoint, changed paths, focused validation result, native smoke status when applicable, exact next action, and one runnable resume command.

## Outcomes & Retrospective

At completion record only: whether `Image4D.java` was fully removed; whether direct conversion required converter changes; whether a JavaSE helper was introduced; whether every original native field index was preserved; final native macOS smoke result/tested commit; intentional compatibility limitation; and deferred lazy image work. Point to evidence for commands/results rather than duplicating logs.

## Revision Note

2026-08-27: Initial plan. It isolates `Image4D` elimination from later lazy-image work and makes native macOS field-ABI validation a mandatory acceptance gate.
