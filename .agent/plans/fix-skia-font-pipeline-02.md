<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Plan 2 — Harden the Skia font registry and complete the Font contract

This ExecPlan follows `AGENTS.md`, `.agent/PLANS.md`, and
`.agents/skills/logical-commits/SKILL.md`.

Execute only after
`.agent/plans/fix-skia-font-pipeline-01.md`
has completed successfully.

## Purpose / Big Picture

Complete the Skia font correction after Plan 1 has separated TTF loading from
legacy PalmFont ownership.

At completion:

- the Skia typeface registry has no fixed 31/32-font ceiling;
- invalid TTF data is never cached as a valid registry entry;
- `Font.style == BOLD` affects Skia drawing and measurement consistently;
- bold state cannot leak into later plain text;
- SDK documentation describes the renderer-specific font source correctly;
- legacy font assets, generated TCZ fonts, and non-Skia behavior remain
  supported;
- final validation respects the user-imposed platform build restrictions.

## Working Set and Resume Protocol

Plan path:

    .agent/plans/fix-skia-font-pipeline-02.md

State path:

    .agent/state/fix-skia-font-pipeline-02.md

Editorial report:

    .agent/reports/fix-skia-font-pipeline-editorial.md

Keep every new file below 20 KB or approximately 600 lines.

At resume:

1. Read the Plan 2 state file first.
2. Read only the active milestone below.
3. Inspect only named active paths.
4. Do not reread Plan 1 unless a current regression contradicts its handoff
   contract.

Before editing, verify the Plan 1 handoff with focused inspection:

- Skia `fontInit()` does not initialize PalmFont state.
- Skia `tufF_fontCreate()` does not call `loadFontFile()`.
- Skia `hv_UserFont` remains null.
- custom TTF success preserves `Font.name`.
- missing custom TTF selects `TCFont`.

If any handoff item is false, stop and repair/finish Plan 1 rather than mixing
its incomplete work into Plan 2.

Read once:

- `TotalCrossVM/src/nm/ui/skia/skia.h`;
- `TotalCrossVM/src/nm/ui/skia/skia.cpp`;
- `TotalCrossVM/src/nm/ui/skia/skia_primitives.cpp`;
- `TotalCrossVM/src/nm/ui/font_FontMetrics.c`;
- `TotalCrossVM/src/nm/ui/PalmFont_c.h`;
- `TotalCrossVM/src/nm/ui/GraphicsPrimitivesText_c.h`;
- `TotalCrossSDK/src/main/java/totalcross/ui/font/Font.java`;
- existing native font/Skia tests.

## Progress

- [x] (2026-09-03) Verify Plan 1 handoff.
- [x] (2026-09-03) Milestone 1: remove registry capacity and invalid-cache
  defects; committed as `62d4afbba`.
- [x] (2026-09-03) Milestone 2: propagate bold style through Skia draw/measure
  operations; committed as `bc6085176`, with fixture follow-up `d712b2df4`.
- [x] (2026-09-03) Milestone 3: update SDK docs and run final validation;
  documentation committed as `374d55370`.
- [x] (2026-09-03) Write the factual editorial report.

## Current Architecture and Scope

Plan 1 established one renderer-specific ownership model:

- Skia uses TTF resources and `Font.skiaIndex`.
- legacy renderers use `FontFile`, `UserFont`, and generated font TCZs.
- Skia does not depend on `TCFont.tcz`.
- TTF resources may still be stored inside loaded TCZ containers.

This plan must preserve that separation.

Current Skia registry defects:

- `typefaces` is a fixed `TYPEFACE_LEN` array;
- `skia_makeTypeface()` uses `typefaceIdx < TYPEFACE_LEN - 1`, so the usable
  capacity is smaller than the declared capacity;
- `SkTypeface::MakeFromData()` can return null but the current code still
  records an index/map entry.

Current Skia style defect:

- Java `Font.style` is passed nowhere in Skia rendering/measurement;
- default `TCFont` resolves to `Roboto Regular.ttf` even for a bold `Font`;
- drawing, widths, and metrics use typeface index and size only.

Out of scope:

- changing Java `Font` field layout;
- cache-key redesign;
- alternate bold TTF filename discovery;
- operating-system family lookup;
- shaping/HarfBuzz/variable-font work;
- removing legacy font resources;
- repository-wide unrelated renderer-macro cleanup.

## Plan of Work

### Milestone 1 — Harden the typeface registry

Goal: make the registry dynamically sized and cache only valid SkTypefaces.

In `skia.cpp`, replace the fixed storage:

    sk_sp<SkTypeface> typefaces[TYPEFACE_LEN];
    int typefaceIdx;

with:

    std::vector<sk_sp<SkTypeface>> typefaces;

Keep `typefaceIndexMap` for resource-name-to-index lookup.

Integer index contract:

- index equals the vector position at insertion time;
- indices remain stable for process lifetime;
- existing Java `Font.skiaIndex` remains unchanged in type/meaning;
- do not erase/reorder entries while the VM is running.

Implement `skia_makeTypeface()` as:

1. check the map and return an existing index;
2. copy the TTF bytes into `SkData`;
3. call `SkTypeface::MakeFromData()`;
4. if the result is null, return `-1`;
5. do not insert a map entry for failure;
6. append the valid typeface;
7. compute its index from the vector position;
8. insert the name/index map entry;
9. return the index.

Update `skia_getTypeface()`:

- valid only when index is non-negative and `< typefaces.size()`;
- invalid index retains the existing Skia system-default fallback;
- introduce no new numeric limit.

Do not clear/rebuild the registry during ordinary screen recreation; preserve
the existing process-lifetime index model.

Add focused tests:

- invalid TTF bytes return `-1`;
- invalid data does not create a retrievable cache entry;
- repeated valid name returns the same index;
- more than 32 distinct valid names can receive stable indices.

For the >32 test, reuse one known-good TTF byte buffer under unique test-only
registry names. Do not commit a duplicated font binary. If the current test
fixture cannot expose valid TTF bytes cleanly, verify the capacity case in the
milestone-end native smoke instead and keep structural unit coverage.

Before committing:

- inspect scoped status/diff;
- run focused copyright-header validation;
- stage only this milestone;
- run `git diff --check --cached`;
- review staged diff;
- follow logical-commits message rules.

Checkpoint commit:

    fix(skia): harden typeface registry

Milestone-end validation only:

- permitted macOS native Skia build;
- relevant native font/Skia smoke/tests;
- no Windows, Linux, Android, iOS, WinCE, or cross build.

Acceptance:

- no fixed font-count ceiling remains;
- invalid `SkTypeface` creation never consumes an index;
- cached indices remain stable.

### Milestone 2 — Honor Font bold style in Skia

Goal: make `Font.style` affect rendering and measurement without inventing a
new resource naming convention.

Architecture decision: use synthetic emboldening with:

    SkFont::setEmbolden(bool)

Do not search for or guess filenames such as `Roboto Bold.ttf`.
Do not encode style into the typeface registry key.
The same loaded typeface may be used plain or synthetically bold.

Extend these Skia APIs to receive bold state explicitly:

- `skia_stringWidth`;
- `skia_stringWidthD`;
- `skia_fontMetrics`;
- `skia_drawText`.

Update declarations and every Skia call site in:

- `skia.h`;
- `skia.cpp`;
- `skia_primitives.cpp`;
- `PalmFont_c.h`;
- `font_FontMetrics.c`;
- `GraphicsPrimitivesText_c.h`;
- any existing internal Skia header required by the implementation.

At each `Font` call site, derive the style from the object:

    (Font_style(font) & 1) != 0

Use an existing `BOLD` constant if visible in that compilation unit; do not add
a global enum solely for this plan.

For the shared/global `skFont`:

- keep the current typeface and size reuse optimization;
- also compare/set `getEmbolden()`/`setEmbolden()` or set the requested boolean
  unconditionally if the API does not expose a cheap state query;
- a bold operation followed by a plain operation must restore plain state.

For local metric fonts:

- construct/select the typeface and size;
- call `setEmbolden(bold)` before `getMetrics()`.

For measurement:

- configure the same bold state before `measureText()`.

For drawing:

- configure the same bold state before creating/drawing the text blob.

Do not change the legacy `faceType`/PalmFont bold selection.

Tests must prove state propagation, not a fragile font-specific width delta:

- plain path passes false;
- bold path passes true;
- bold then plain does not retain bold state;
- drawing and measurement use the same style flag.

Do not assert that every string has a different advance when emboldened.

Checkpoint commit:

    fix(font): honor bold style with skia

Keep focused tests in the same commit.

Milestone-end validation only:

- focused header/diff checks;
- permitted macOS native Skia build;
- relevant native smoke/tests;
- no other platform build.

Stop condition:

If the pinned Skia API does not provide `SkFont::setEmbolden(bool)`, stop this
milestone and report the mismatch. Do not silently substitute filename
heuristics, `SkPaint` fake-bold behavior, or another architecture.

Acceptance:

- Skia bold Font requests alter the configured SkFont;
- measurement and drawing consume identical style state;
- no bold state leaks to a later plain operation.

### Milestone 3 — Reconcile SDK documentation and final behavior

Goal: describe the actual font contract and close validation.

Update only relevant Javadocs/comments in:

    TotalCrossSDK/src/main/java/totalcross/ui/font/Font.java

Clarify:

- legacy renderers use generated TotalCross/PalmFont font archives;
- Skia renderers use TTF resources;
- a TTF may be stored inside a TCZ already loaded by the VM;
- this generic TCZ resource storage is different from the legacy generated
  font-TCZ format;
- missing fonts fall back to `Font.DEFAULT`;
- `boldStyle` is renderer behavior and does not imply a particular bold TTF
  filename.

Do not expose `skiaIndex` as a new supported public API.
Do not redesign Java font caching.

Reinspect the final native call graph and verify:

- Skia `tufF_fontCreate()` has no legacy fallthrough;
- Skia `fontInit()` has no `TCFont.tcz` requirement;
- Skia draw/measure paths never require `hv_UserFont`;
- `loadUserFontFromFontObj()` remains legacy-only in actual rendering;
- valid custom TTF names survive unchanged;
- missing custom TTF selects `TCFont`;
- registry failures use index `-1` system fallback rather than PalmFont;
- bold is passed consistently to draw and metric functions.

Run the SDK build only at the end of this SDK-related milestone:

    cd TotalCrossSDK
    ./gradlew-agent clean dist

Then run the final permitted macOS native build and native font smoke tests.

Do not run local builds for:

- Windows;
- Linux;
- Android;
- iOS;
- WinCE;
- any cross target.

Run final static validation:

    python3 scripts/validate-copyright-headers.sh --files <all changed source files>
    git diff --check <plan-series-base>..HEAD
    git diff --stat <plan-series-base>..HEAD
    git status --short

Confirm every file newly created by the two-plan execution is at most 20 KB or
approximately 600 lines. Existing large files do not need size refactors.

Small documentation changes may remain with the final functional commit only if
they describe exactly that contract. If they are substantial and logically
independent, create:

    docs(font): clarify renderer font sources

Follow logical-commits for staging and message validation.

At final completion create/update:

    .agent/reports/fix-skia-font-pipeline-editorial.md

Keep it below the new-file size limit and include:

- Editorial Summary;
- Original Plan versus Actual Outcome;
- What Changed;
- Decisions and Trade-offs;
- Unexpected Problems and Discoveries;
- Validation and Measurable Results;
- Useful Evidence and Examples;
- Limitations, Remaining Work, and Open Questions;
- Possible Article Angles;
- Suggested Narrative;
- Claims Requiring Human Review.

Do not copy raw build logs into the report.

## Surprises & Discoveries

Known baseline; do not rediscover unless contradicted:

- Plan 1 removed PalmFont ownership from Skia.
- `Font.skiaIndex` is the Skia typeface handle.
- `typefaceIndexMap` already provides name caching.
- fixed registry storage is unnecessary because `<vector>` is already used in
  the Skia implementation.
- `Font.style` currently does not reach Skia draw/measure calls.

Record only observations that change remaining work.

## Decision Log

- Decision: vector position remains the stable Java-visible Skia index.
  Rationale: removes capacity without changing object layout.

- Decision: invalid `SkTypeface` objects are never cached.
  Rationale: registry presence must mean successful typeface creation.

- Decision: registry entries live for process lifetime.
  Rationale: existing Font objects keep stable integer indices.

- Decision: Skia bold uses `SkFont::setEmbolden(bool)`.
  Rationale: style is independent of resource naming.

- Decision: bold state is passed explicitly to drawing and measurement.
  Rationale: both must render/measure the same effective font.

- Decision: do not guess a bold TTF filename.
  Rationale: the API supplies style separately from the family/resource name.

- Decision: keep all legacy font assets and packaging.
  Rationale: non-Skia compatibility remains supported.

## Validation and Acceptance

Allowed builds:

- macOS native build only at the end of related milestones;
- SDK build only at the end of Milestone 3.

Native smoke tests:

- allowed at the end of related milestones;
- allowed again at final completion.

Forbidden local builds:

- Windows;
- Linux;
- Android;
- iOS;
- WinCE;
- cross targets.

Before every logical commit:

    git status --short -- <task paths>
    git diff --stat -- <task paths>
    git diff -- <task paths>
    python3 scripts/validate-copyright-headers.sh --files <changed files>
    git diff --check --cached

Review staged content and validate the commit message with
`.agents/skills/logical-commits/SKILL.md`.

Final acceptance:

1. Plan 1 handoff remains true.
2. valid TTF registry entries are dynamically sized and stable.
3. invalid TTF data does not enter the registry.
4. more than 32 typefaces are representable.
5. bold state reaches both draw and measure.
6. bold does not leak into plain operations.
7. SDK documentation matches actual renderer behavior.
8. legacy font assets and non-Skia behavior remain present.
9. permitted macOS builds/smokes pass.
10. final SDK build passes.
11. copyright and diff checks are clean.
12. every new plan-support/report file respects the 20 KB/~600-line limit.

## Risks and Open Questions

No architecture is intentionally left open.

Stop and report instead of inventing a new design when:

- `SkFont::setEmbolden(bool)` is unavailable;
- stable typeface indices require registry deletion/reordering;
- a valid registry test would require committing a duplicate font binary;
- Plan 1 separation is found incomplete;
- unrelated dirty-worktree changes cannot be isolated safely.

## Idempotence and Recovery

Preserve unrelated local changes.

Never use:

    git reset --hard
    git clean -fd
    blanket checkout/restore of the worktree

Stage only intended paths.

After each logical commit, rewrite the state file with the next concrete action.
Do not amend/rewrite commits unless explicitly requested.
Do not push unless explicitly requested.

If a milestone-end build fails, retain the logical commit when focused evidence
shows the source behavior is correct; diagnose the failure and create a
follow-up commit only when it proves a defect in the milestone.

## Outcomes & Retrospective

Milestone 1 replaced the fixed typeface array with stable vector positions and
rejected invalid `SkTypeface` creation before cache insertion. The direct
macOS fixture passed invalid-data, repeated-name, 40-entry capacity, and
index-stability assertions.

Milestone 2 passed bold state explicitly to Skia metrics, measurement, and
drawing, and the fixture confirmed bold rendering plus plain-state restoration.
The pinned Skia API provided `SkFont::setEmbolden(bool)` as planned.

Milestone 3 updated `Font.java`, passed the macOS native build and startup
smoke, and passed `TotalCrossSDK/gradlew-agent clean dist`. No non-macOS or
cross-target build was run. The historical full native VM suite was unavailable
as a CMake target; the direct Skia fixture was the relevant native test.

The detailed factual handoff is in
`.agent/reports/fix-skia-font-pipeline-editorial.md`.

At final completion, reconcile these outcomes with the editorial report and
distinguish actual results from planned expectations.

## Revision Note

Initial revision. This plan is intentionally sequential after Plan 1 and owns
registry robustness, Skia bold/style propagation, SDK documentation, and final
series validation.
