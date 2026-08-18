<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Complete logical UI scaling with core SkFont metrics

This living ExecPlan continues `feat/logical-ui-scaling`. Preserve branch history,
state, evidence, and validated implementation.

## Execution efficiency

- Keep state short and operational; read only its active checkpoint when resuming.
- Do not reread review/history/completed milestones or old evidence routinely.
- Read exact symbols and narrow ranges; track audited and pending files.
- Redirect verbose commands to artifact logs and inspect only tails or errors.
- Batch coherent changes before SDK distribution and native deployment.
- Verify deployed artifacts with hashes and machine-readable assertions.
- Update state and evidence once per validated slice; review token consumption only
  at milestone boundaries.

## Purpose

Make public layout, drawing, font metrics, and natural image dimensions logical,
while framebuffers, backing stores, codecs, and raw pixels remain physical.

Text scaling uses only the pinned core Skia APIs. TotalCross continues to own
line breaking and multiline layout.

## Final Text Scope

Required:

- `SkFont::getMetrics`;
- `SkFont::measureText`;
- `SkTextBlob::MakeFromText` or the existing equivalent core path;
- destination `fontScale`;
- canvas `contentScale`;
- TotalCross wrapping, multiline layout, preferred sizes, cursors, selection,
  alignment, and ellipsis.

Out of scope:

- SkShaper;
- HarfBuzz;
- ICU;
- SkParagraph;
- a new text engine;
- guaranteed bidi, complex-script shaping, ligatures, font fallback, or cluster
  semantics beyond existing behavior.

Do not modify depot-tools or the Skia package for text-engine support.

## Resume Protocol

Read first:

    .agent/state/logical-ui-scaling.md

Then read only the active checkpoint and named support files.

Use:

    .agent/reviews/logical-ui-scaling-current-review.md
    .agent/design/logical-ui-scaling-api.md
    .agent/design/logical-ui-scaling-images.md
    .agent/design/logical-ui-scaling-text.md
    .agent/guides/logical-ui-scaling-validation.md
    .agent/guides/macos-native-runtime-validation.md
    .agent/guides/logical-ui-scaling-danfe.md
    .agent/guides/private-screenshot-capture.md
    .agent/evidence/logical-ui-scaling.md
    .agent/reports/logical-ui-scaling-editorial.md

Store verbose logs under `artifacts/logical-ui-scaling/`.

## Execution Policy

During implementation:

- JavaSE/AWT is Java proof;
- native code is compiled and executed through a deployed macOS application
  using the matching freshly built dylib;
- do not run Android or iOS.

At final validation:

- Android is required;
- iOS is optional;
- embedded target validation is optional when available.

A command, test, build, commit, slice, or milestone is not a stopping point.
Continue until the plan is complete or a genuine external blocker exists.

Do not push, open a pull request, update the issue, publish, or use credentials.

## Token and File Budget

Use focused searches, scoped diffs, concise tails, and milestone-specific tests.

Do not use `clean` without evidence of stale output.

Every new source, test, helper, guide, or support file remains below 20 KiB and
approximately 600 lines.

## Progress

- [x] Establish clean recorded base and identify global-density coupling.
- [x] Add logical API and image scaffolding.
- [x] Restore and validate guarded `USE_WRITE_PIXELS`.
- [x] Finish layout behavior: correct nonzero PIXEL client origins and run native
      root-PIXEL migration proof.
- [x] Establish native macOS high-DPI backing and core Skia base transforms.
- [x] Complete SkFont-only logical text and TotalCross multiline behavior.
- [x] Complete image codecs, transforms, cache ownership, and bidirectional sync.
- [x] Complete Java renderer semantics.
- [x] Audit supported non-Skia native semantics; macOS `USE_SKIA=OFF` is recorded
      as unsupported rather than falsely equivalent.
- [x] Complete DANFE Java and native macOS lanes and process-specific screenshot.
- [ ] Run final Android semantic validation and complete handoff (stopped: the
      installed fixture aborts during Android UI-resource lookup).

## Current Branch Review

Reviewed head before this revision:

    c8a3152c482d6f3ac511e8295cc400b555aeecae

The branch is 82 commits ahead of the recorded base.

Useful work must be preserved. The shaping blocker recorded at the reviewed head
is resolved by the final text scope above.

## Plan of Work

### Reconciliation checkpoint R1

Apply and commit this plan revision.

Correct `Control.setRect` PIXEL client bounds. Preserve original logical left,
top, right, and bottom before converting each edge exactly once. Add nonzero
insets/client-origin tests at scales 1.5, 2, and 3.

Synchronize living records:

- actual reviewed head;
- no external text-engine blocker;
- append-only evidence for later claims only after checking logs;
- accurate Java/native compile/native runtime labels;
- no invented evidence.

Review the public visibility of `Graphics.setScales`. Record a decision to expose
a documented lifecycle API or replace public mutation with an internal runtime
and test bridge before final API acceptance.

Rename unsupported typography claims such as `kerningAdvance` to neutral
representative-string terminology.

Acceptance:

- nonzero PIXEL client-origin regression passes;
- root PIXEL native fixture passes;
- plan, state, outcomes, and evidence agree;
- no text-engine blocker remains;
- no production text-engine dependency is added.

Progress 2026-08-01: corrected client-rectangle conversion to preserve logical
edges before applying PIXEL conversion. Focused Java
`LogicalLayoutUnitTest` passed at 1.5, 2, and 3 with nonzero insets; log:
`artifacts/logical-ui-scaling/logs/pixel-client-origin-test.log`.
The freshly deployed native macOS fixture also passed the root PIXEL assertion,
reporting `pixelChild=10,5,50,20`; log:
`artifacts/logical-ui-scaling/logs/root-pixel-fixture-native-final.log`.

Progress 2026-08-01: added internal destination-aware FontMetrics measurement
and line-height methods. On Skia they configure metrics and `measureText` at
`Font.size * Graphics.fontScale`; controls now consume that path rather than
post-measure multiplication. Java focused test, native compile, and deployed
native macOS fixture passed. `AV` is recorded only as a representative string,
not a kerning assertion.

Progress 2026-08-01: `Convert.insertLineBreak` gained an effective-size
measurement overload and Label uses it for autoSplit and explicit split. Focused
Java wrapping assertions pass; log:
`artifacts/logical-ui-scaling/logs/effective-wrap-test.log`.
The deployed native macOS fixture passed as well; logs:
`effective-wrap-dist.log`, `effective-wrap-deploy.log`, and
`effective-wrap-native.log`.

Decision 2026-08-01: retain `Graphics.setScales` as the public,
destination-owned surface lifecycle API. It is used by the runtime, Image, and
test fixtures; documentation freezes its invalidation behavior and explicitly
rejects global-density semantics.

Progress 2026-08-01: added effective-size SkFont descent for Edit cursor
geometry, removing the final post-measure scale multiplication found in the
text-control audit. Native compile and focused Java test passed; logs:
`effective-descent-build.log` and `effective-descent-java-test.log`.
The deployed native macOS fixture passed; logs: `effective-descent-dist.log`,
`effective-descent-deploy.log`, and `effective-descent-native.log`.

Progress 2026-08-01: routed MultiEdit's TotalCross break-position calls through
the effective-size overload. Focused Java validation passed; log:
`artifacts/logical-ui-scaling/logs/effective-multiedit-wrap-test.log`.
The deployed native macOS fixture passed; logs:
`effective-multiedit-wrap-dist.log`, `effective-multiedit-wrap-deploy.log`,
and `effective-multiedit-wrap-native.log`.

Progress 2026-08-01: added a focused Label cache regression proving logical
line widths ignore contentScale and refresh for fontScale; log:
`artifacts/logical-ui-scaling/logs/text-layout-cache-test.log`.

Progress 2026-08-01: completed the remaining Button and MultiEdit direct
text-geometry audit. Button ellipsis, material preferred bounds, image/text
placement, relative-label alignment, and underline geometry now use the
destination effective-size measurement. MultiEdit selection height and dotted
baseline use effective line-height and ascent. The native `FontMetrics`
adapter now exposes effective ascent from `SkFont::getMetrics`. Focused Java,
native compile, and deployed native macOS fixture validation passed; logs:
`effective-text-controls-test.log`, `effective-text-controls-native-build.log`,
`effective-text-controls-dist.log`, `effective-text-controls-deploy.log`, and
`effective-text-controls-native.log`.

Progress 2026-08-01: routed the remaining live `Convert.insertLineBreak` and
`getBreakPos` UI consumers through destination effective font sizes, including
dialogs, popup/menu titles, lists, tooltips, Check, Radio, and Grid tooltips.
The compatibility-only static ToolTip helper remains scale-one by signature.
Focused Java and deployed native macOS fixture validation passed; logs:
`effective-multiline-consumers-test.log`,
`effective-multiline-consumers-dist.log`,
`effective-multiline-consumers-deploy.log`, and
`effective-multiline-consumers-native.log`.

### Milestone 2R residual: native surface lifecycle

The established base transform and Retina initialization remain valid.

Audit remaining scale-change lifecycle:

- framebuffer and drawable resize;
- monitor/backing-scale changes where supported;
- raster and image cache invalidation;
- dirty bounds after a scale transition;
- logical layout remains unchanged when only contentScale changes.

Validate through the deployed native macOS lane.

Acceptance: initial and changed surface scales preserve logical geometry and
physical backing correctness without stale caches.

### Milestone 3R: SkFont-only logical text

Read the updated text design.

#### Effective-size measurement

Keep public FontMetrics as scale-one logical compatibility metrics.

Add an internal destination-aware path that configures the renderer font at:

    Font.size * Graphics.fontScale

before measuring.

For Skia:

- `SkFont::getMetrics` receives the effective logical size;
- `SkFont::measureText` receives the effective logical size;
- drawing uses the same typeface, effective size, and UTF-16 input;
- canvas contentScale is the only physical scale;
- integer rounding occurs after effective-size measurement.

Replace helpers that calculate:

    fm.metric * fontScale

with the destination-aware measurement result.

#### TotalCross wrapping and multiline

TotalCross keeps all line-breaking and multiline responsibility.

Adapt `Convert.insertLineBreak` or introduce an internal overload/measurement
adapter so Label autoSplit and every current multiline consumer measure candidate
lines through the destination-aware path.

Preserve explicit newline behavior and existing public APIs.

Do not call or add SkParagraph.

#### Controls and caches

Audit Label, Button, Edit, MultiEdit, and other direct uses of:

    fmH
    fm.height
    fm.ascent
    fm.descent
    fm.stringWidth
    fm.charWidth

Classify each use as scale-one compatibility, destination layout, drawing,
cursor, selection, wrapping, or raw allocation.

Ensure fontScale changes invalidate preferred bounds, wrapping, line widths,
cursor and selection geometry, and relevant text caches. ContentScale changes
must not change logical text layout.

#### Renderer behavior

Java and supported non-Skia paths follow the same order: apply fontScale before
measurement and drawing. Quantization is allowed only at the documented backend
boundary.

Do not claim advanced shaping or fallback.

#### Tests

Cover:

- content scales 1, 1.5, 2, and 3;
- font scales 1 and 1.5;
- Label, Button, Edit, and MultiEdit;
- explicit newline and automatic TotalCross wrapping;
- Portuguese accents and DANFE strings;
- empty and whitespace strings;
- baseline, alignment, cursor, and selection;
- same Font on simultaneous destinations;
- cache invalidation;
- measurement/drawing effective-size equality;
- integer compatibility rounding.

Acceptance:

- logical metrics and wrapping are invariant under contentScale;
- fontScale changes effective metrics and wrapping;
- measured accepted lines fit when drawn;
- native deployed macOS fixture passes;
- no SkShaper, HarfBuzz, ICU, or SkParagraph dependency exists.

### Milestone 4R: Image behavior and synchronization

Audit constructors, loaders, PNG/JPEG, frames, row pitch, transforms, natural
drawing, source rectangles, textures, caches, and readback.

Preserve explicit Java/native ownership. Validate upload, native readback,
alternating ownership, alpha 128, odd widths, failure state, and multiframe
images in deployed native macOS.

Acceptance: default and scaled images honor logical/public and physical/internal
dimensions in Java and native macOS.

### Milestone 5R: Java renderer

Apply logical units to Java primitives, clips, translations, text, images, source
rectangles, and dirty bounds.

For Java text, derive the font at `Font.size * fontScale` before measuring and
drawing. Use the same Java rendering context where possible. Document any
integer-only double metric limitation.

Acceptance: Java common fixtures and DANFE semantic assertions pass.

### Milestone 6R: supported non-Skia native renderer

Build the repository-supported non-Skia macOS configuration. Apply fontScale
before selecting/resizing fonts and preserve logical coordinates for primitives,
images, clips, events, and controls.

No other platform is attempted during this milestone.

Acceptance: the common semantic matrix passes, or a concrete unsupported macOS
configuration is recorded for maintainer review without a false equivalence
claim.

### Milestone 7R: complete DANFE and screenshots

Run the same deterministic fixture in separate lanes:

- JavaSE/AWT;
- deployed native macOS with the exact matching dylib.

The DANFE validates SkFont-only metrics and TotalCross wrapping. It does not claim
advanced shaping.

Produce PNGs, machine-readable assertions, logs, and process-specific screenshots.

Resolve a CoreGraphics window ID owned by the launched PID and execute:

    /usr/sbin/screencapture -x -l "$WINDOW_ID" "$OUTPUT_PNG"

Acceptance: text containment, size ranges, TotalCross wraps, geometry, barcode,
runtime identity, synchronization, and privacy checks pass in both lanes.

### Milestone 8R: final Android and handoff

Only after Java and native macOS pass:

- run a high-density Android fixture;
- validate logical text, TotalCross wrapping, images, synchronization, and
  barcode;
- run optional iOS or embedded validation when available;
- finish Javadocs, compatibility notes, audits, evidence, and editorial report.

Acceptance: required Java, native macOS, and Android rows pass and every optional
or unavailable platform is labeled accurately.

## Final Acceptance

The task is complete only when:

1. `USE_WRITE_PIXELS` remains supported;
2. DP and PIXEL layout pass, including nonzero client origins;
3. native high-DPI backing is proven;
4. text is measured and drawn at `Font.size * fontScale`;
5. contentScale is applied only by destination rendering;
6. TotalCross line breaking and multiline layout use destination-aware metrics;
7. no new shaping/paragraph dependency is introduced;
8. images synchronize in both directions;
9. Java, native Skia, and supported non-Skia semantic fixtures pass;
10. complete DANFE Java and native macOS lanes pass;
11. deterministic screenshots pass;
12. final Android validation passes;
13. records, privacy, compatibility, and file-size audits pass.

## Risks

The current text layout helpers may appear correct because font advances scale
approximately linearly. Tests must prove effective-size measurement directly.

Legacy TotalCross line breaking accepts FontMetrics rather than a destination.
Prefer a small internal adapter or overload over a new generalized text engine.

The current branch may contain claims not represented in append-only evidence.
Revalidate or downgrade those claims rather than reconstructing results from
memory.

## Decision Log

- Decision: continue existing branch and preserve history.
  Date: 2026-08-01.

- Decision: preserve `USE_WRITE_PIXELS` with scale-aware eligibility.
  Date: 2026-08-01.

- Decision: keep Java and native macOS proof separate.
  Date: 2026-08-01.

- Decision: native implementation validation uses macOS until final Android.
  Date: 2026-08-01.

- Decision: use CoreGraphics owner PID and `screencapture -l`.
  Date: 2026-08-01.

- Decision: logical text scaling uses core SkFont only.
  Rationale: logical units require effective-size measurement and a canvas
  transform, not a new shaping or paragraph engine.
  Date: 2026-08-01.

- Decision: TotalCross retains wrapping and multiline layout.
  Rationale: this task changes unit and scale semantics, not text-layout
  ownership.
  Date: 2026-08-01.

## Outcomes

The branch has completed valuable foundations, including guarded embedded
drawing, native Retina initialization, fractional Skia metrics, native runtime
identity, and selected control scaling.

The plan is stopped by user request with Android as the only required acceptance
lane not proven. The Android build, packaging, install, and launch are proven;
the fixture aborts before assertions because an image resource contained in
`TCUI.tcz` is not resolved by the Android runtime. The editorial and token-use
reports at `.agent/reports/` record validated results and the efficient restart
point.
