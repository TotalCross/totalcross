<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Implement logical UI units and surface-aware rendering scales

This ExecPlan is a living document. Keep `Progress`, `Surprises & Discoveries`,
`Decision Log`, `Outcomes & Retrospective`, and the resume state current. Follow
`AGENTS.md`, `.agent/PLANS.md`, and the token-efficient, resumable-plan principles
described by `totalcross-depot-tools/.agent/PLANS.md`.

## Purpose / Big Picture

After this change, TotalCross UI geometry and font sizes are logical rather than
device-pixel values. A `Graphics` object owns the scale that maps its logical
coordinate space to its physical buffer. Text additionally uses a font scale.
A normal `Image` has scale `1`, so rendering a fixed-size document into an image
does not inherit Android density or macOS Retina scale.

The user-visible proof is issue #433: a DANFE drawn into a default
`360 x 540` image exports exactly `360 x 540` pixels with correctly sized,
undistorted text on both standard- and high-density displays. An explicitly
scaled logical image can export a larger physical buffer while keeping the same
logical layout. Skia is implemented first, then the Java renderer, then the
non-Skia native renderer, and the three paths must be logically equivalent.

## Working Set and Resume Protocol

Use these files:

    .agent/logical-ui-scaling-execplan.md
    .agent/design/logical-ui-scaling-api.md
    .agent/design/logical-ui-scaling-images.md
    .agent/design/logical-ui-scaling-text.md
    .agent/guides/logical-ui-scaling-validation.md
    .agent/guides/logical-ui-scaling-danfe.md
    .agent/guides/private-screenshot-capture.md
    .agent/state/logical-ui-scaling.md
    .agent/evidence/logical-ui-scaling.md
    .agent/archive/logical-ui-scaling-history.md
    .agent/reports/logical-ui-scaling-editorial.md

On resume, read the state file first. Then read only this plan's active milestone
and the supporting file explicitly named by that milestone. Search evidence only
for the current milestone, command, or artifact. Do not routinely reread all
design files, old logs, the archive, or completed milestone detail.

Read the API design before Milestones 1 and 2. Read the text design before
Milestone 3. Read the image design before Milestone 4. Read the validation guide
before closing any milestone. Read the DANFE and screenshot guides only when
building or capturing the end-to-end proof.

Store generated logs and artifacts outside Git tracking under:

    artifacts/logical-ui-scaling/

## Token and File-Size Budget

Operate economically in tokens:

- prefer `rg`, `rg --files`, file ranges, and exact symbols;
- inspect `git diff --stat` before reading a diff;
- use scoped `git status --short -- <paths>`;
- redirect verbose builds and inspect only failures or a short tail;
- run focused tests before module or full builds;
- do not repeat expensive validation after comments or formatting;
- finish and commit one coherent slice before opening a broad investigation.

No new source, test, helper, documentation, plan-support, or script file may
exceed 20 KiB or approximately 600 lines. Existing oversized files may receive
only small bridge edits. Extract substantive new logic into cohesive files below
both limits. Audit changed files at each milestone closure.

## Progress

- [x] (2026-08-01 21:00Z) Created `feat/logical-ui-scaling` directly from
      `origin/master` at `d480df074e7fb6f5a32dfcc2f1f30c3949095e73` and recorded
      static baseline evidence of the density-coupled image text path.
- [x] (2026-08-01 21:35Z) Added layout-unit configuration and inheritance,
      deprecated the encoded DP and global converter path, and established
      per-Graphics scale accessors with focused SDK tests.
- [x] (2026-08-01 22:05Z) Applied a per-Graphics Skia base transform, removed
      a scale-bypassing image fast path, preserved raw pixel coordinates, and
      compiled the macOS native target.
- [x] (2026-08-01 22:30Z) Added peer-aware AWT backing-scale refresh and
      removed Skia text and metric dependence on global screen density.
- [x] (2026-08-01 22:30Z) Added double-precision FontMetrics accessors while
      preserving integer logical compatibility fields.
- [x] (2026-08-01 22:50Z) Added immutable logical-image backing scales,
      physical dimension accessors, and Skia upload refresh behavior.
- [x] (2026-08-01 23:00Z) Made Java natural image drawing reduce scaled backing
      buffers to logical size and covered it with the Graphics scale fixture.
- [ ] Make the non-Skia native renderer logically equivalent.
- [ ] Run the full DANFE, macOS, Android, synchronization, and privacy validation.
- [ ] Complete deprecations, Javadocs, audits, evidence, and editorial handoff.

## Current Architecture and Scope

At plan authoring time, `master` was observed at
`d480df074e7fb6f5a32dfcc2f1f30c3949095e73`. Execution must fetch
`origin/master` and record the actual current commit instead of assuming this SHA.

Start in a new worktree created from that fetched commit. Do not reuse, merge,
cherry-pick, or copy source changes, branches, patches, generated binaries, or
worktrees created by earlier plans in this session. Earlier screenshots and PNGs
may be used only as optional human references after provenance and privacy review;
they are not implementation inputs or fresh validation evidence.

The relevant areas include:

- `TotalCrossSDK/src/main/java/totalcross/ui/Control.java`;
- `TotalCrossSDK/src/main/java/totalcross/ui/Container.java`;
- `TotalCrossSDK/src/main/java/totalcross/ui/MainWindow.java`;
- `TotalCrossSDK/src/main/java/totalcross/ui/gfx/Graphics.java`;
- `TotalCrossSDK/src/main/java/totalcross/ui/font/Font.java`;
- `TotalCrossSDK/src/main/java/totalcross/ui/font/FontMetrics.java`;
- `TotalCrossSDK/src/main/java/totalcross/ui/image/Image.java`;
- `TotalCrossSDK/src/main/java/totalcross/util/UnitsConverter.java`;
- `TotalCrossSDK/src/main/java/totalcross/sys/Settings.java`;
- `TotalCrossSDK/src/main/java/totalcross/Launcher.java`;
- Skia graphics and text code under `TotalCrossVM/src/nm/ui`;
- legacy native graphics and font code under `TotalCrossVM/src`.

Inspect the current tree before finalizing exact edit paths. Do not change the
deployer. Compilation, SDK, deployer, and runtime are expected to come from the
same release, and applications or libraries using inlined constants must be
recompiled.

## Plan of Work

### Milestone 0: Establish a clean base and fresh failure evidence

Fetch `origin/master`, record its SHA, and create a separate branch and worktree.
Abort instead of overwriting an existing branch or directory. A safe pattern is:

    git fetch origin master
    base=$(git rev-parse origin/master)
    git worktree add -b feat/logical-ui-scaling ../totalcross-logical-ui "$base"

Record the SHA and scoped clean status in state and evidence. Inventory current
uses of `Control.DP`, `UnitsConverter.toPixels`, `Settings.screenDensity`,
`FontMetrics`, `fmH`, image dimensions, pixel arrays, Skia surface creation, and
Java/native image synchronization.

Build a deterministic issue reproducer from current master. Prove that text drawn
into an image incorrectly depends on screen density, or record the closest
reproducible failure if the host path differs. Save only sanitized baseline
metrics and images.

Read: validation guide, baseline sections only.

Acceptance: the implementation worktree is based directly on the recorded current
master, contains no source from previous plans, and has reproducible baseline
evidence for issue #433.

### Milestone 1: Introduce the logical API and layout contract

Read `.agent/design/logical-ui-scaling-api.md` in full. Add `LayoutUnit`, root DP
default, container inheritance, explicit pixel mode, scale ownership on
`Graphics`, `DP = 0`, identity `UnitsConverter.toPixels`, and deprecation of
`Settings.screenDensity`.

Keep public integer APIs where compatibility requires them, but use `double` for
fractional calculations. Convert rectangle edges and derive physical widths and
heights from converted edges. Add focused tests for inheritance, semantic layout
constants, `PREFERRED`, fractional scales, `DP + n`, identity conversion, and the
single-line root pixel opt-out.

Acceptance: API and layout tests pass, and adding only
`setLayoutUnit(LayoutUnit.PIXEL)` to a root window preserves the old pixel layout
for the migration sample.

### Milestone 2: Implement logical drawing in Skia

Use the surface's `contentScale` as the Skia canvas base transform. Do not fix only
text size. Coordinates, clips, translations, paths, strokes, image destinations,
glyph positions, and dirty bounds must use one coherent logical space. Physical
framebuffer dimensions, raw pixel operations, and image source rectangles remain
explicitly physical.

Ensure reset-transform returns to the logical base transform. Avoid double
scaling, especially where existing code already multiplies by screen density.
Add scale tests for `1`, `1.5`, `2`, and `3`.

Read: API design; relevant validation sections.

Acceptance: Skia renders the same logical geometry at all test scales, with the
expected physical buffer size and no screen-global density dependency.

### Milestone 3: Add macOS Retina detection and logical text

First identify the current macOS desktop window path. For JavaSE/AWT, use the
visible window's `GraphicsConfiguration` default transform after peer creation.
If an AppKit desktop path exists, use its backing scale. Do not mistake the iOS
Darwin implementation for macOS desktop support.

Update scale when a window moves between displays. A scale change recreates or
invalidates physical buffers, glyph atlases, raster caches, screenshots, and
paint, but must not change logical component bounds or line wrapping.

Read `.agent/design/logical-ui-scaling-text.md` in full. Implement logical
`Font.size`, shaped logical advances, logical vertical metrics, integer
compatibility metrics, and `double` accessors. Preferred sizes and `fmH` use
logical values. Drawing uses the same shaping result under the Skia base
transform.

Acceptance: a Retina window reports and uses its actual scale; logical bounds and
text wrapping remain stable across scale changes; text-based controls fit their
content at all tested scales.

### Milestone 4: Implement scaled-image semantics

Read `.agent/design/logical-ui-scaling-images.md` in full. Add immutable image
scale and separate logical and physical dimensions. The normal constructor and
ordinary file loading use scale `1`. An explicit factory creates a logical image
with a larger backing buffer.

Audit codecs, row pitch, frames, transformations, texture upload, readback,
`applyChanges`, dirty ownership, and pixel arrays. Preserve established upload
versus readback direction. Do not clear dirty state after a failed native copy.

Acceptance: a default `360 x 540` image owns exactly `360 x 540` pixels; a logical
scale-2 image owns `720 x 1080` pixels while retaining `360 x 540` logical
dimensions; export and natural-size drawing follow the documented contract.

### Milestone 5: Make the Java renderer equivalent

Implement the same logical coordinate, clipping, image, font metric, preferred
size, and scale contracts in the Java renderer. Share semantic fixtures with
Skia. Exact antialiasing pixels may differ, but integer compatibility results,
logical bounds, line breaks, baselines, containment, and image dimensions must
match. Double metrics may use a documented small tolerance.

Acceptance: the same focused test matrix passes against both Skia and Java, and
the DANFE semantic assertions are equivalent.

### Milestone 6: Make the non-Skia native renderer equivalent

Adapt the legacy native graphics and bitmap font paths last. Pass logical and
physical sizes explicitly. Logical metric caches must be independent of display
density; raster caches must include effective physical size and other values that
change raster output.

Reuse public tests and add backend-specific tests only for unavoidable integer
behavior. Do not weaken the API contract to accommodate an implementation detail.

Acceptance: the non-Skia path matches Skia and Java in logical metrics, preferred
bounds, wrapping, containment, and physical image dimensions.

### Milestone 7: Run end-to-end DANFE and platform validation

Read the DANFE, screenshot, and full validation guides. Recreate the DANFE
fixture from this plan and current master, not from earlier implementation
patches. Run automated assertions, macOS Retina validation, Android device or
emulator validation, Java/Skia equivalence, and image synchronization tests.

Capture the launched application window by process identity or an explicitly
selected known window. Do not enumerate or persist unrelated window titles.
Inspect, crop, and sanitize every artifact before acceptance.

Acceptance: the default-image DANFE is independent of display scale, has the
required dimensions and barcode structure, keeps text within approved bounds,
passes on macOS and Android, and produces private-data-safe evidence.

### Milestone 8: Finalize documentation and handoff

Update Javadocs for logical units, scale ownership, image dimensions, pixel APIs,
metric rounding, root pixel migration, recompilation requirements, and
deprecations. Search for remaining internal dependencies on
`Settings.screenDensity`, old DP-marker detection, and implicit image
logical-equals-physical assumptions.

Run focused tests, then module builds and final distribution validation as
required by the validation guide. Audit file sizes and extract logic from any
oversized new file. Complete the editorial report and distinguish delivered proof
from tolerated renderer differences or remaining limitations.

Acceptance: all required validation passes, evidence is reproducible and
sanitized, documentation matches behavior, and issue #433 has Android as well as
macOS proof before it is considered ready to close.

## Validation and Acceptance

The detailed matrix, commands, tolerances, evidence format, and escalation policy
are in `.agent/guides/logical-ui-scaling-validation.md`. Each milestone closes at
the smallest sufficient validation level. The full task closes only when:

1. root DP, inherited container layout, and explicit root pixel mode work;
2. `Graphics.contentScale` and `fontScale` govern their own surface;
3. logical fonts, metrics, preferred sizes, events, clips, and drawing agree;
4. default and explicitly scaled images follow their logical/physical contract;
5. `DP`, `UnitsConverter.toPixels`, and `Settings.screenDensity` are deprecated
   without hidden internal rendering dependencies;
6. Skia, Java, and non-Skia native paths meet the equivalence rules;
7. macOS Retina and scale changes are proven;
8. DANFE passes on macOS and Android;
9. synchronization, screenshot selection, and privacy checks pass;
10. every new file remains below 20 KiB and approximately 600 lines.

## Risks and Open Questions

The largest compatibility risk is code that treats component or image dimensions
as raw pixel counts. Keep physical access explicit and audit every internal pixel
boundary.

Changing `DP` to zero requires recompilation. Do not add deployer rewriting or a
runtime marker compatibility layer.

Font engines may differ in antialiasing and slightly in fractional metrics.
Require equal logical behavior and semantic containment rather than cross-engine
PNG identity. Within one renderer and font set, a default-image export must be
identical across display scales.

Image upload/readback ownership can corrupt pixels if direction is ambiguous.
Treat synchronization semantics as a blocking decision before changing
`applyChanges`.

Fractional and multi-monitor scale changes may expose rounding gaps. Use `double`,
convert rectangle edges, and centralize rounding.

## Idempotence and Recovery

The isolated worktree is the primary protection against previous-session changes.
Never use destructive reset or checkout commands on an existing worktree. If a
milestone fails, preserve its log, update state with the exact blocker, correct
the smallest cause, and rerun only the focused command.

Generated artifacts may be recreated. Evidence and history are append-only.
State and editorial files may be rewritten. Do not delete unrelated caches or
local files. Before each commit, inspect only active paths and ensure that no
private screenshot, verbose log, or generated binary is staged.

Commits follow `CONTRIBUTING.md`. Use English Conventional Commit titles with a
required scope. Use `!` and a `BREAKING CHANGE:` footer when the default logical
layout or public dimension semantics require it. Do not push, open a pull request,
close the issue, or publish artifacts without explicit user instruction.

## Surprises & Discoveries

No implementation discoveries are recorded yet. Add only observations that alter
remaining architecture, compatibility, validation, or recovery work.

## Decision Log

- Decision: Start from freshly fetched current `origin/master` in an isolated
  worktree and ignore source changes from earlier plans.
  Rationale: The implementation and evidence must stand independently.
  Date/Author: 2026-08-01 / plan author.

- Decision: Use logical units throughout public UI drawing and keep physical
  dimensions explicit at surface and pixel-buffer boundaries.
  Rationale: A text-only scale fix would preserve the mixed model behind issue
  #433.
  Date/Author: 2026-08-01 / plan author.

- Decision: Use `double`, never new `float` API, for fractional calculations.
  Rationale: This matches TotalCross constraints and reduces rounding drift.
  Date/Author: 2026-08-01 / plan author.

- Decision: Deprecate `DP` with value zero and make `toPixels` an identity.
  Rationale: Recompiled source migrates without marker detection or deployer work.
  Date/Author: 2026-08-01 / plan author.

- Decision: Make root layout DP by default and require one explicit root setting
  to preserve pixel layout.
  Rationale: Migration is encouraged while the opt-out remains minimal.
  Date/Author: 2026-08-01 / plan author.

- Decision: Implement Skia first, Java second, and non-Skia native last.
  Rationale: Skia establishes the reference transform and shaping contract.
  Date/Author: 2026-08-01 / plan author.

## Outcomes & Retrospective

No implementation outcome exists yet. At each milestone, record delivered
behavior, exact proof, deviations, and remaining risk without duplicating logs.

## Revision Note

2026-08-01: Replaced the original near-limit single-file plan with a compact
living ExecPlan plus scoped design and validation files. The split preserves
detail, resume efficiency, and room for implementation updates while keeping each
file below the requested size limit.
