<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Complete logical UI scaling from the reviewed branch

This ExecPlan is a living document. Keep `Progress`, `Surprises & Discoveries`,
`Decision Log`, `Outcomes & Retrospective`, state, and evidence synchronized.

This revision continues branch `feat/logical-ui-scaling`. It does not restart from
master and does not discard useful committed work. Read
`.agent/reviews/logical-ui-scaling-branch-review.md` before the first resumed
slice.

## Purpose / Big Picture

Finish a coherent logical-coordinate API in which layout, drawing, fonts, and
images are independent of display density, while physical buffers and raw pixel
operations remain explicit.

The final user-visible proof is a complete synthetic DANFE rendered:

- through JavaSE/AWT for the Java renderer;
- through a deployed application using the freshly built native macOS VM;
- at default image scale one and explicit logical image scale two;
- finally on Android after the implementation is stable.

A Java process running on macOS is not native macOS validation.

## Working Set and Resume Protocol

Read first:

    .agent/state/logical-ui-scaling.md

Then read only the active section of this plan and its named guide. Use:

    .agent/reviews/logical-ui-scaling-branch-review.md
    .agent/design/logical-ui-scaling-api.md
    .agent/design/logical-ui-scaling-images.md
    .agent/design/logical-ui-scaling-text.md
    .agent/guides/logical-ui-scaling-validation.md
    .agent/guides/macos-native-runtime-validation.md
    .agent/guides/logical-ui-scaling-danfe.md
    .agent/guides/private-screenshot-capture.md
    .agent/evidence/logical-ui-scaling.md
    .agent/archive/logical-ui-scaling-history.md
    .agent/reports/logical-ui-scaling-editorial.md

Do not reread the full bundle on each resume. Search evidence selectively and
store verbose output under `artifacts/logical-ui-scaling/`.

## Execution and Platform Policy

Use focused Java tests and native macOS tests while implementing.

During corrective and implementation milestones:

- JavaSE/AWT tests may run on macOS but count only as Java renderer proof;
- native code must be compiled and executed through a deployed native macOS app;
- do not start iOS or Android builds, deploys, or runtime tests;
- do not interpret `totalcross.Launcher` execution as native proof.

At final cross-platform validation only:

- Android validation is required because issue #433 reports Android;
- iOS validation is optional unless separately requested or needed to resolve a
  platform-specific concern;
- embedded Linux validation may be performed for the `USE_WRITE_PIXELS`
  configuration when target access is available, but its code path must already
  have been compiled and focused-tested on macOS.

## Token and File Budget

Use exact symbol searches, relevant file ranges, scoped status, diff stats, and
focused tests. Redirect verbose commands to logs and inspect concise tails.

Do not run `clean` unless stale output is demonstrated. Do not front-load final
platform validation.

Every new source, test, helper, guide, or support file must remain below 20 KiB
and approximately 600 lines. Extract cohesive logic from oversized existing
files instead of adding large implementations inline.

## Progress

- [x] Established the branch directly from the recorded master and captured the
      original global-density coupling.
- [x] Added useful API scaffolding for layout units, graphics scales, logical
      images, and compatibility deprecations.
- [x] Restored scale-aware `USE_WRITE_PIXELS` eligibility, compiled both macro
      configurations on macOS, and exercised the native Skia surface fixture.
- [x] Connect `LayoutUnit` to real child placement and prove the root PIXEL
      migration path in focused Java layout tests.
- [ ] Validate the root PIXEL migration fixture in the deployed native macOS
      application during the runtime milestone.
- [x] Complete Skia logical drawing and native macOS content-scale initialization.
- [ ] Complete logical text shaping, fontScale, metrics, preferred sizes, and
      cache behavior.
- [ ] Complete image codecs, transformations, cache ownership, and bidirectional
      Java/native synchronization.
- [ ] Complete Java renderer semantic equivalence.
- [ ] Complete non-Skia native semantic equivalence on macOS.
- [ ] Deploy and run the complete DANFE through both Java and native macOS lanes,
      including deterministic screenshots.
- [ ] Run final Android validation, optional iOS validation, audits,
      documentation, and editorial handoff.

## Current Architecture and Scope

The branch contains 37 commits above its recorded base. Preserve history and
correct behavior with new commits.

API scaffolding already present includes:

- `LayoutUnit`;
- root DP configuration;
- `DP = 0`;
- identity `UnitsConverter.toPixels`;
- deprecated `Settings.screenDensity`;
- `Graphics.contentScale` and `fontScale`;
- logical and physical `Image` dimensions.

Do not treat the presence of a field, getter, build pass, or Java fixture launch
as proof that its behavioral milestone is complete.

Do not modify the deployer for DP compatibility. Deployment is used only to
produce the native macOS test application from the matching SDK.

## Plan of Work

### Corrective checkpoint R0: reconcile the existing branch

Read the branch review in full.

Restore the `USE_WRITE_PIXELS` fast path removed by `fd7e5d358`, while preventing
it from bypassing a non-identity destination transform. Preserve its original
opaque/full-source/same-size/full-alpha requirements. Add a focused eligibility
helper if this keeps the code testable and below the file-size limit.

Locate the repository definition that enables `USE_WRITE_PIXELS`. Compile both
enabled and disabled configurations on macOS. Do not invent a new build option if
the repository already has one. Add a focused test or native fixture proving:

- identity-scale eligible copies may use direct writes;
- scaled or transformed destinations use `drawBitmapRect`;
- raw `setPixel`, `getPixel`, and RGB APIs remain physical;
- output is equal for the eligible direct and fallback cases.

Review the public visibility of `Graphics.setScales`. Make scale mutation internal
unless a public lifecycle API is deliberately approved.

Update state and evidence to distinguish Java, native compile, and native runtime
results.

Acceptance: the embedded specialization is preserved, both build configurations
compile, focused output matches, and no unrelated cleanup is included.

### Milestone 1R: complete actual logical layout behavior

Read the API design.

Connect the effective parent `LayoutUnit` to child placement. DP values are
logical. PIXEL values represent physical pixels and must be converted into the
parent's logical coordinate space at the layout boundary.

Prove:

- MainWindow DP default;
- Container INHERIT behavior;
- the parent controls placement of the child;
- a child container's explicit unit controls only its descendants;
- one root `setLayoutUnit(LayoutUnit.PIXEL)` preserves a legacy layout;
- semantic constants and offsets use the correct unit;
- shared rectangle edges do not create rounding gaps at 1.5, 2, and 3;
- event and hit-test coordinates are converted exactly once;
- native screen logical dimensions do not remain accidental physical dimensions.

Do not mark this milestone complete based only on resolver tests.

Acceptance: behavioral placement tests and a small migration application pass in
Java and in a deployed native macOS run.

### Milestone 2R: complete Skia and native macOS surface scaling

Complete the coherent Skia base transform for coordinates, clips, paths, strokes,
image destinations, text positions, and dirty bounds. Keep image source
rectangles and raw pixel APIs physical.

Implement native macOS backing-scale acquisition from the actual native window or
view and initialize the native screen `Graphics`. Do not use the AWT
`GraphicsConfiguration` as evidence for this path.

Validate through the procedure in
`.agent/guides/macos-native-runtime-validation.md`. Run scales 1, 1.5 where
supported, 2, and 3 through offscreen fixtures. Test a real Retina scale through
the native application.

Acceptance: the deployed native macOS app reports the expected logical and
physical dimensions and exercises the freshly built Skia code.

### Milestone 3R: complete logical text and FontMetrics

Read the text design.

Implement and test:

- `Font.size` and `fontScale` in logical units;
- actual double metrics rather than integer delegation;
- shared shaping or equivalent measurement/drawing results;
- ascent, descent, leading, line height, and shaped advances;
- preferred sizes and baselines;
- fallback, accents, kerning, ligatures, and multiline wrapping;
- separation of logical layout caches from physical raster caches;
- raster invalidation on content-scale change;
- layout invalidation on font-scale change.

Use the same tests in Java and deployed native macOS lanes. Pixel-identical output
is not required across engines, but semantic metrics, wrapping, containment, and
preferred bounds must satisfy the documented tolerances.

Acceptance: DANFE text metrics and ordinary text controls remain logically stable
across content scales without over-shrinking.

### Milestone 4R: complete Image behavior and synchronization

Read the image design.

Audit every logical/physical boundary: constructors, loaders, codecs, PNG/JPEG,
frames, row pitch, transforms, hardware scale, texture upload, native readback,
caches, and natural-size drawing.

Preserve established `applyChanges` direction and make ownership explicit.
Validate Java-to-native upload, native-to-Java readback, alternating ownership,
alpha 128, odd row widths, failures, and multiframe images in a deployed native
macOS fixture.

Acceptance: default and scaled images follow their public dimension contract in
Java and native macOS, and no synchronization result is inferred from a Java-only
test.

### Milestone 5R: complete the Java renderer

Apply the logical model to all Java primitives, clips, translations, text, image
destinations, source rectangles, and dirty bounds. Do not limit scaling to
`fillRect` and natural-size image drawing.

Run the common semantic fixture matrix. Record renderer-specific antialiasing
differences separately from logical failures.

Acceptance: the Java renderer satisfies the complete logical fixture and DANFE
semantic assertions.

### Milestone 6R: complete the non-Skia native renderer

Build the repository-supported non-Skia configuration on macOS. Adapt native font,
primitive, image, clip, event, and cache behavior to the same surface-owned scale
contract.

Do not attempt other native platforms during this milestone.

Acceptance: a deployed native macOS app using the non-Skia configuration satisfies
the common semantic matrix, or the plan records a concrete unsupported
configuration with maintainer review rather than claiming equivalence.

### Milestone 7R: native macOS DANFE and screenshots

Read the native runtime, DANFE, and screenshot guides.

The same deterministic fixture must run in two distinct lanes:

- JavaSE/AWT through `totalcross.Launcher`;
- deployed native macOS executable through the exact freshly built
  `libtcvm.dylib`.

Both lanes produce PNGs and machine-readable metrics. The native lane is the
required proof for changed C/C++ and Skia code.

Resolve each process-owned CoreGraphics window ID and call:

    /usr/sbin/screencapture -x -l "$WINDOW_ID" "$OUTPUT_PNG"

Do not use a Computer Use integration as the primary capture path and do not fall
back to a desktop screenshot.

Acceptance: all DANFE assertions pass, the exact native runtime is verified,
screenshots are sanitized, and Java results are not mislabeled as native.

### Milestone 8R: final cross-platform validation and handoff

Only now run Android validation. Use at least one high-density Android device or
emulator and execute the complete DANFE and image synchronization fixture.

Run iOS validation only if available, requested, or necessary to resolve a
specific platform concern. Do not make an unavailable iOS environment block the
Android issue fix unless iOS is explicitly promoted to a required target.

When target access exists, validate the embedded `USE_WRITE_PIXELS` build without
changing its semantics merely to make another backend pass.

Complete Javadocs, compatibility notes, source audits, file-size audits, evidence,
and the editorial report.

Acceptance: required Java, native macOS, and Android proof passes; optional
platform results are labeled accurately; no core validation remains hidden under
documentation status.

## Validation and Acceptance

Follow `.agent/guides/logical-ui-scaling-validation.md`.

The task is complete only when:

1. actual layout semantics, not only API metadata, pass;
2. `USE_WRITE_PIXELS` remains supported;
3. Java and native macOS validation are recorded separately;
4. native macOS backing scale is proven in a deployed app;
5. fontScale and real double metrics work;
6. image synchronization passes in both directions natively;
7. Java, Skia, and supported non-Skia behavior meet semantic equivalence;
8. the complete text-bearing DANFE passes;
9. deterministic window screenshots use `screencapture -l`;
10. Android final validation passes;
11. file-size, privacy, compatibility, and source audits pass.

## Risks and Open Questions

The current branch may contain changes that compile but are not reached by its
tests. Prefer behavioral tests at public and native runtime boundaries.

A global scale mirror may remain for compatibility, but no renderer or layout
decision may read it.

A native macOS test requires a matching SDK, deployed application, and dylib.
Treat any stale packaged runtime as invalid evidence.

`USE_WRITE_PIXELS` may have backend-specific restrictions. Preserve the feature
and make eligibility explicit rather than deleting it.

## Idempotence and Recovery

Continue in the existing worktree and branch. Do not reset, rebase, rewrite, or
drop the existing commits as part of this review correction.

A failed test is not a reason to switch platforms. Fix or record the smallest
blocking cause on macOS.

Do not push, open a pull request, update the issue, publish artifacts, or use
credentials unless explicitly requested.

## Surprises & Discoveries

- Observation: the prior execution labeled an AWT Launcher run as macOS platform
  proof.
  Evidence: branch evidence records `totalcross.Launcher` with `/scale`.

- Observation: `USE_WRITE_PIXELS` was removed even though the new code differs
  only in builds where that specialization is enabled.
  Evidence: commit `fd7e5d358`.

- Observation: `LayoutUnit` is stored and tested as metadata but is not consumed
  by the layout engine.
  Evidence: no effective-unit use in child placement or `Control.setRect`.

- Observation: the pinned Skia package exposes `SkFont` but not `SkShaper` or
  `SkParagraph` headers.
  Evidence: scoped file audit of
  `TotalCrossVM/deps/totalcross-depot-tools/skia/local/include` on 2026-08-01.
  The current measurement/drawing path can be kept equivalent, but this does
  not by itself prove full script shaping, fallback, or ligature behavior.

Add only discoveries that change remaining work.

## Decision Log

- Decision: preserve the existing branch and correct it with new commits.
  Rationale: useful scaffolding is present, while history remains auditable.
  Date: 2026-08-01.

- Decision: native implementation validation uses macOS only until final
  cross-platform validation.
  Rationale: it gives a fast, matching native VM loop without premature iOS or
  Android build work.
  Date: 2026-08-01.

- Decision: JavaSE/AWT and native macOS are separate proof lanes.
  Rationale: Java methods do not execute native replacements or Skia C++ code.
  Date: 2026-08-01.

- Decision: restore `USE_WRITE_PIXELS` with scale-aware eligibility.
  Rationale: preserving an embedded specialization is safer than unrelated
  deletion.
  Date: 2026-08-01.

- Decision: use CoreGraphics process ownership and `screencapture -l`.
  Rationale: it is deterministic, avoids title enumeration, and captures only the
  target window.
  Date: 2026-08-01.

## Outcomes & Retrospective

The branch review found useful foundations but no final accepted outcome yet.
Update this section after each corrected behavioral milestone.

- 2026-08-01: Milestone 3R's first layout slice connected destination
  `fontScale` to `Control` and `Label` preferred measurements, while preserving
  logical dimensions across content-scale changes. `Label` refreshes cached
  line widths when its destination font scale changes. Evidence: focused Java
  `LogicalTextScaleTest` passed; the freshly deployed native macOS fixture
  reported `labelWidths=84,84,125` at content scales 2 and 4, then font scale
  1.5, respectively. Full logs: `artifacts/logical-ui-scaling/logs/`
  `logical-text-scale-test.log`, `sdk-dist-logical-text-scale.log`,
  `deploy-logical-text-scale-after-dist.log`, and
  `native-logical-text-scale-after-dist.log`.
- 2026-08-01: Extended the same preferred-size contract to `Button`, including
  its cached multiline widths. Evidence: focused Java
  `LogicalTextScaleTest` passed; full log:
  `artifacts/logical-ui-scaling/logs/logical-text-scale-button-test.log`.
- 2026-08-01: Extended non-material `Edit` preferred measurements to the
  destination font scale. Evidence: focused Java `LogicalTextScaleTest` passed;
  full log: `artifacts/logical-ui-scaling/logs/logical-text-scale-edit-test.log`.
- 2026-08-01: Corrected `Label` and `Button` vertical line placement to use
  destination-scaled logical line height. Evidence: focused Java
  `LogicalTextScaleTest` passed; full log:
  `artifacts/logical-ui-scaling/logs/logical-text-vertical-scale-test.log`.
- 2026-08-01: Corrected `Edit` text, selection, and cursor vertical geometry
  to use destination-scaled line metrics. Evidence: focused Java
  `LogicalTextScaleTest` passed; full log:
  `artifacts/logical-ui-scaling/logs/logical-text-edit-vertical-test.log`.
- 2026-08-01: Corrected `Edit` horizontal layout and caret calculations to use
  destination-scaled advances. Evidence: focused Java `LogicalTextScaleTest`
  passed after materializing the edit through its normal layout cycle; full log:
  `artifacts/logical-ui-scaling/logs/logical-text-edit-cursor-test.log`.
- 2026-08-01: Added deployed native macOS `Edit` preferred-width evidence. The
  fixture reports `editWidths=48,48,67` at content scales 2 and 4 then font
  scale 1.5. Full logs: `sdk-dist-edit-scale.log`, `deploy-edit-scale.log`, and
  `native-edit-scale.log` under `artifacts/logical-ui-scaling/logs/`.
- 2026-08-01: Changed native integer Skia text widths from truncation to upward
  rounding of their fractional advance, preserving the compatibility rule that
  preferred extents must not clip. Native compile and deployed macOS fixture
  passed; logs: `native-font-width-rounding-build.log`,
  `deploy-font-width-rounding.log`, and `native-font-width-rounding.log`.
- 2026-08-01: Added machine-checked deployed text metrics for DANFE, accented
  Portuguese text, and `AV`, plus vertical metric and control-scale invariants.
  Native macOS runtime passed with fractional values. Logs:
  `sdk-dist-text-assertions.log`, `deploy-text-assertions.log`, and
  `native-text-assertions.log`.
- 2026-08-01: Made destination font-scale transitions reposition control
  surfaces and invalidate painting. The focused Java test asserts actual
  preferred control bounds grow after a font-scale change; full log:
  `artifacts/logical-ui-scaling/logs/logical-text-font-invalidation-test.log`.
  The freshly deployed native macOS fixture also passed; logs:
  `sdk-dist-font-invalidation.log`, `deploy-font-invalidation.log`, and
  `native-font-invalidation.log`.
- 2026-08-01: Unified native Skia `FontMetrics` integer overloads (character,
  String, char array, and StringBuffer) with the same Skia measurement path as
  drawing and fractional String advances. Native compile and deployed macOS
  runtime passed; logs: `native-font-overload-consistency-build.log`,
  `deploy-font-overload-consistency.log`, and
  `native-font-overload-consistency.log`.
- 2026-08-01: Added deployed assertion that the compatibility integer String
  width equals the upward-rounded fractional advance. Native macOS passed with
  `81.6484375 -> 82`; logs: `sdk-dist-font-compatibility.log`,
  `deploy-font-compatibility.log`, and `native-font-compatibility.log`.
- 2026-08-01: Extended deployed compatibility assertions to char-array and
  StringBuffer widths; all overloads returned `82`. Logs:
  `sdk-dist-font-overloads.log`, `deploy-font-overloads.log`, and
  `native-font-overloads.log`.
- 2026-08-01: Updated `MultiEdit` cached line height and masked preferred width
  for destination font scale. Focused Java `LogicalTextScaleTest` passed; log:
  `artifacts/logical-ui-scaling/logs/logical-text-multiedit-test.log`.
  Native deployed fixture passed with `multiEditHeights=42,42,58`; logs:
  `sdk-dist-multiedit-scale.log`, `deploy-multiedit-scale.log`, and
  `native-multiedit-scale.log`.

## Revision Note

2026-08-01: Rebased the living plan's reported progress on reviewed behavior,
restored embedded-platform scope, separated Java from native macOS validation,
deferred other native platforms until final validation, and specified a
deterministic screenshot path.
