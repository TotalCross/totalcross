<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Plan 1 — Separate Skia TTF loading from legacy PalmFont

This ExecPlan follows `AGENTS.md`, `.agent/PLANS.md`, and
`.agents/skills/logical-commits/SKILL.md`.

Execute this plan before
`.agent/plans/fix-skia-font-pipeline-02.md`.

## Purpose / Big Picture

Fix native `Font` creation so a Skia build uses one coherent TTF pipeline
instead of loading a Skia typeface and then continuing through the legacy
PalmFont/FontFile pipeline.

At completion:

- `TC_RENDERER_SKIA` creates fonts only through Skia typefaces.
- Skia may still read a `.ttf` resource from an already loaded TCZ container
  through `tczGetFile()`.
- Skia does not call `loadFontFile()`, populate `Font.hv_UserFont`, or require
  `TCFont.tcz`.
- a valid custom TTF keeps the caller's `Font.name`;
- a missing custom TTF explicitly falls back to `TCFont`;
- TTF suffix/name handling is memory-safe and case-insensitive;
- non-Skia PalmFont behavior remains unchanged.

Expected starting branch:
`fix/windowing-clickthrough-fontname`.

If the implementation no longer matches the architecture below, stop and
record the conflicting path before editing. Do not silently redesign the
solution.

## Working Set and Resume Protocol

Plan path:

    .agent/plans/fix-skia-font-pipeline-01.md

State path:

    .agent/state/fix-skia-font-pipeline-01.md

Keep the state file below 100-150 lines and rewrite it after each logical
commit. Record only:

- active milestone/slice;
- last logical commit;
- modified paths;
- next concrete action;
- focused checks completed;
- milestone-end build/smoke still deferred;
- blocker, if any;
- exact resume command.

At resume, read the state file first, then only the active milestone and paths.
Do not repeat the original repository-wide investigation.

Before editing, read once:

- `AGENTS.md`;
- `.agents/skills/logical-commits/SKILL.md`;
- `TotalCrossVM/src/nm/ui/font_Font.c`;
- `TotalCrossVM/src/nm/ui/PalmFont_c.h`;
- `TotalCrossVM/src/nm/ui/font_FontMetrics.c`;
- `TotalCrossVM/src/nm/ui/GraphicsPrimitivesText_c.h`;
- `TotalCrossVM/src/nm/ui/skia/skia.h`;
- `TotalCrossVM/src/nm/ui/skia/skia.cpp`;
- `TotalCrossVM/src/nm/instancefields.h`;
- `TotalCrossSDK/src/main/java/totalcross/ui/font/Font.java`;
- `TotalCrossVM/src/nm/ui/font_Font_test.h`.

Do not change the native `Font` instance-field layout.

## Progress

- [x] (2026-09-03) Record baseline and dirty-worktree state.
- [x] (2026-09-03) Milestone 1: make Skia and legacy font ownership mutually
  exclusive; commit `bfac1883c` — `fix(font): separate skia and legacy loading`.
- [x] (2026-09-03) Milestone 2: make Skia TTF resolution safe and
  deterministic; commit `7f4f11d2a` —
  `fix(font): make skia ttf resolution deterministic`.
- [x] (2026-09-03) Close Plan 1 and prepare the exact handoff contract for
  Plan 2.

## Current Architecture and Scope

`Font` exposes native state through `instancefields.h`:

- object fields: `name`, `hv_UserFont`, `fm`;
- integer fields: `style`, `size`, `skiaIndex`.

Legacy PalmFont behavior:

1. `fontInit()` initializes `fontsHeap`, `htUF`, and `htBaseFonts`.
2. It opens the default generated font through `loadFontFile("TCFont")`.
3. `tufF_fontCreate()` resolves a `FontFile *`.
4. The pointer is stored in the byte array referenced by `hv_UserFont`.
5. `loadUserFontFromFontObj()` loads glyph data from generated font TCZs.
6. legacy drawing/measurement consumes `UserFont`.

Skia behavior:

1. `tufF_fontCreate()` maps `TCFont` to `Roboto Regular.ttf`, otherwise uses
   the requested name and appends `.ttf` when needed.
2. `tczGetFile()` locates the TTF resource in already loaded TCZ containers.
3. `skia_makeTypeface()` creates/caches a SkTypeface.
4. the index is stored in `Font.skiaIndex`.
5. Skia drawing and metrics consume `Font.skiaIndex` and `Font.size`.

The current defect is that the Skia block ends and execution continues into
the legacy `loadFontFile()` path. This can produce an object such as:

    Font.name        = "TCFont"
    Font.skiaIndex   = index of "MyFont.ttf"
    Font.hv_UserFont = legacy TCFont FontFile pointer

The renderer uses the custom TTF while the public name says `TCFont`.

Do not confuse these mechanisms:

KEEP:

    tczGetFile("SomeFont.ttf", false)

This reads a generic resource from a TCZ already loaded by the VM.

REMOVE FROM SKIA FONT OWNERSHIP:

    loadFontFile()
    FontFile
    UserFont
    Font.hv_UserFont
    TCFont.tcz as a PalmFont source

Current Skia consumers already bypass `hv_UserFont`:

- `GraphicsPrimitivesText_c.h` uses `Font_skiaIndex`;
- `font_FontMetrics.c` uses `Font_skiaIndex`;
- the Skia width path in `PalmFont_c.h` uses `Font_skiaIndex`.

Known defects in this plan:

1. Skia font creation falls through into legacy creation.
2. valid custom TTF names can be overwritten by legacy fallback.
3. legacy font TCZ presence can make a name appear valid even though Skia does
   not render that font.
4. Skia startup unnecessarily depends on `TCFont.tcz`.
5. `.ttf` detection accesses `len - 4` without proving `len >= 4`.
6. `.ttf` detection is case-sensitive.
7. fixed 128-byte buffers can overflow for long names.
8. font-path renderer selection uses `SKIA_H` where `TC_RENDERER_SKIA` is the
   real architectural switch.

Out of scope for Plan 1:

- Skia registry capacity/validation;
- bold/style behavior;
- Java cache-key redesign;
- deletion of legacy font assets or generators;
- filesystem/system font discovery;
- shaping/fallback-family redesign;
- repository-wide cleanup of unrelated `SKIA_H` uses.

## Plan of Work

### Milestone 1 — Separate renderer ownership

Goal: make initialization, creation, and destruction compile-time-exclusive.

Start with `TotalCrossVM/src/nm/ui/font_Font.c`.

Replace the current pattern:

    #ifdef SKIA_H
       ... Skia ...
    #endif
       ... legacy ...

with:

    #if TC_RENDERER_SKIA
       ... Skia only ...
    #else
       ... existing legacy only ...
    #endif

In the Skia branch:

- never call `loadFontFile()`;
- never allocate `Font_hvUserFont`;
- never write a `FontFile *`;
- set `Font_skiaIndex`;
- modify `Font_name` only when Skia itself selects fallback;
- do not use a legacy lookup to validate a Skia font.

Keep the legacy branch behavior unchanged.

Then update `fontInit()` in `PalmFont_c.h`.

Always initialize the shared Java/static metadata:

- `MAX_FONT_SIZE`;
- `MIN_FONT_SIZE`;
- `NORMAL_SIZE`;
- `TAB_SIZE`;
- `Font.DEFAULT` / `defaultFontName`.

When `TC_RENDERER_SKIA` is true, return success after shared metadata is ready.
Do not initialize:

- `fontsHeap`;
- `htUF`;
- `htBaseFonts`;
- `defaultFont`;
- `openFonts`.

Do not call `loadFontFile(defaultFontName)` and do not fail because
`TCFont.tcz` is absent.

For `!TC_RENDERER_SKIA`, retain the existing legacy initialization.

Make `fontDestroy()` symmetrical:

- Skia must not traverse/free legacy structures it never initialized.
- non-Skia keeps existing cleanup.

In touched font paths, replace `SKIA_H` as a behavior selector with
`TC_RENDERER_SKIA`/`!TC_RENDERER_SKIA`. Do not perform a repository-wide macro
migration.

Update `font_Font_test.h`:

- legacy tests retain `defaultFont` and `hv_UserFont` expectations;
- Skia tests must not require `defaultFont`;
- Skia font creation must leave `hv_UserFont == null`;
- preserve field-order tests.

Before the commit, run only focused non-build checks:

    git status --short -- <task paths>
    git diff --stat -- <task paths>
    git diff -- <task paths>
    python3 scripts/validate-copyright-headers.sh --files <changed files>
    git diff --check --cached

Stage only Milestone 1 paths and inspect the staged diff.

Checkpoint commit:

    fix(font): separate skia and legacy loading

Use the full logical-commits procedure, including an English explanatory body
and post-commit message validation.

Milestone-end validation only:

- do not build before the milestone source/tests are complete;
- run the permitted macOS native Skia build;
- run the relevant native font smoke/tests after that build;
- run no Windows, Linux, Android, iOS, WinCE, or cross build.

Acceptance:

- no Skia path from `tufF_fontCreate()` reaches `loadFontFile()`;
- Skia `fontInit()` does not open `TCFont.tcz`;
- Skia-created Font objects do not store `FontFile` state;
- legacy source behavior remains intact;
- macOS Skia build and relevant smoke pass.

After the commit, rewrite the state file before starting Milestone 2.

### Milestone 2 — Make TTF resolution safe and deterministic

Goal: resolve one TTF identity, preserve `Font.name` on success, and explicitly
select the Skia default on failure.

Keep the implementation in `font_Font.c`; do not create a new production file.

Replace fixed native font-name buffers with dynamically sized strings.

Use the existing allocation contract:

    String2CharP(...)
    xfree(...)

Every allocated name/resource buffer must be released on every normal exit.

Canonical resource-name policy:

- public `TCFont` maps to resource `Roboto Regular.ttf`;
- another requested name ending in `.ttf`, case-insensitively, is used as-is;
- otherwise append `.ttf`;
- allocate exact required space plus terminator;
- never index before the beginning of short strings.

Create small static helpers with single responsibilities, for example:

- case-insensitive `.ttf` suffix check;
- canonical TTF resource-name creation;
- load-or-get-cached Skia typeface from one canonical resource.

Do not expose these helpers outside `font_Font.c` unless an existing test
pattern requires a declaration.

Typeface resolution order:

1. Canonicalize the requested Skia resource.
2. Call `skia_getTypefaceIndex(resource)`.
3. If absent, call `tczGetFile(resource, false)`.
4. If found, allocate a buffer of `uncompressedSize`, read it, call
   `skia_makeTypeface()`, free the buffer, and close the `TCZFile`.
5. Treat any negative result as failure.
6. For a failed custom font, resolve `Roboto Regular.ttf` using the same helper.
7. When custom fallback is selected, change `Font.name` to the Java default
   name `TCFont`.
8. If the default TTF also fails, keep `Font.skiaIndex == -1`, keep/set
   `Font.name == "TCFont"`, and allow `skia_getTypeface(-1)` to use the existing
   Skia system-default fallback.
9. Never fall back to `loadFontFile()` or PalmFont.

Identity rules:

- `Font.getFont("MyFont", ...)` keeps public name `MyFont` when
  `MyFont.ttf` resolves.
- `Font.getFont("Fonts/MyFont.ttf", ...)` keeps that exact public name on
  success.
- uppercase/mixed-case `.TTF` must not receive another `.ttf`.
- a missing custom font changes its public name to `TCFont`.
- legacy font archives must not influence any of these decisions.

Handle allocation failures consistently with existing VM conventions. Do not
continue with a partially built resource name.

Add focused tests for:

- names shorter than four characters;
- `.ttf`, `.TTF`, and mixed-case suffixes;
- successful custom resolution preserving the exact public name;
- missing custom resource selecting `TCFont`;
- Skia `hv_UserFont` remaining null.

Do not add a duplicated TTF binary fixture. Reuse a TTF already available to
the native test environment. If no known-good TTF is accessible without a new
binary fixture, keep pure-name/fallback tests in the unit suite and verify the
real custom TTF case in milestone-end native smoke.

Before committing, run the same focused header/staged-diff/diff-check procedure
required by logical-commits.

Checkpoint commit:

    fix(font): make skia ttf resolution deterministic

Keep its focused tests in the same commit.

Milestone-end validation only:

- permitted macOS native Skia build;
- native font smoke/tests after the build;
- explicitly exercise:
  - default font;
  - custom TTF;
  - missing custom font fallback;
  - short font name;
  - uppercase `.TTF`;
- no other platform build.

Acceptance:

- a successful custom TTF cannot be renamed by legacy state;
- a legacy `.tcz` font cannot make a missing Skia TTF appear installed;
- short and long font names avoid fixed-buffer hazards;
- `.ttf` suffix handling is case-insensitive;
- Skia still works without `TCFont.tcz`.

## Surprises & Discoveries

Do not rediscover these baseline facts unless current code contradicts them:

- Skia rendering does not use `hv_UserFont`.
- Skia metrics do not use `hv_UserFont`.
- `tczGetFile()` is generic resource lookup in already loaded TCZ containers.
- `loadFontFile()` is the separate PalmFont archive mechanism.
- current `fontInit()` treats missing `TCFont.tcz` as fatal.
- current `tufF_fontCreate()` can create contradictory Skia/legacy state.

Record only observations that change the remaining implementation.

If a Skia caller unexpectedly reads `defaultFont`, `fontsHeap`, `htUF`,
`htBaseFonts`, `openFonts`, or `Font_hvUserFont`, identify its exact path and
stop the affected milestone. Do not restore the dual pipeline as a shortcut.

## Decision Log

- Decision: Skia and PalmFont are compile-time-exclusive font pipelines.
  Rationale: their state and consumers are independent.

- Decision: generic `.ttf` lookup inside loaded TCZ containers remains.
  Rationale: TCZ resource storage is not the legacy PalmFont format.

- Decision: Skia never uses `FontFile`, `UserFont`, or `TCFont.tcz`.
  Rationale: current Skia rendering/measurement has no consumer for them.

- Decision: `TCFont` maps to `Roboto Regular.ttf`.
  Rationale: preserve current Skia default mapping.

- Decision: custom TTF failure explicitly resolves the default TTF.
  Rationale: public `Font.name` must match the selected font policy.

- Decision: if even the default TTF cannot load, use existing Skia index `-1`
  system fallback, not PalmFont.
  Rationale: keep the renderer pipeline single and robust.

- Decision: preserve the exact caller-visible name on custom TTF success.
  Rationale: maintain the documented installed-font check.

- Decision: use dynamically sized native font names.
  Rationale: remove fixed-buffer and short-suffix memory hazards.

- Decision: use `TC_RENDERER_SKIA` in touched font behavior guards.
  Rationale: an include guard is not an architectural selector.

## Validation and Acceptance

Hard build restrictions:

Allowed:

- macOS native build only at the end of a related milestone;
- native smoke tests only at the end of a related milestone and final plan
  completion.

Not needed in Plan 1:

- SDK build, because Plan 1 must not change SDK Java code.

Forbidden locally:

- Windows builds;
- Linux builds;
- Android builds;
- iOS builds;
- WinCE builds;
- cross builds for those targets.

Before every logical commit:

- scoped status and diff;
- focused copyright-header validator;
- staged diff review;
- `git diff --check --cached`;
- logical-commits message validation after commit.

Final Plan 1 checks:

    git diff --check <plan-1-base>..HEAD
    git status --short

Confirm any new plan/state/report file remains under 20 KB or approximately
600 lines. Do not refactor an existing oversized source file to satisfy this
limit.

## Risks and Open Questions

No architecture is intentionally left open.

Required stop conditions:

- an unexpected Skia consumer requires PalmFont state;
- removing legacy initialization changes a non-Skia compile-time branch;
- a required TTF test would need adding a duplicated binary font fixture;
- unrelated dirty-worktree changes overlap a target hunk and cannot be safely
  staged separately.

Preserve unrelated local changes. Never use destructive cleanup.

## Idempotence and Recovery

Never run `git reset --hard` or blanket checkout/clean operations.

Before each commit:

    git status --short -- <task paths>
    git diff --stat -- <task paths>
    git diff -- <task paths>

Stage only intended paths.

If interrupted before a commit, state must list modified files and next check.
If interrupted after a commit, resume from the state file and do not reconstruct
completed investigation.

Do not amend/rewrite history unless explicitly requested.
Do not push unless explicitly requested.

## Outcomes & Retrospective

At each milestone boundary record:

- behavior delivered;
- logical commit subject;
- focused checks;
- macOS build/smoke result;
- deferred validation and reason.

Plan 1 is complete only when both milestones pass and the handoff contract
below is true.

Handoff contract for Plan 2:

- Skia `fontInit()` has no PalmFont dependency.
- Skia `tufF_fontCreate()` has no legacy fallthrough.
- `hv_UserFont` remains unused/unpopulated on Skia.
- TTF resource names are safely canonicalized.
- successful custom TTF preserves public `Font.name`.
- missing custom TTF selects `TCFont`.
- legacy behavior remains compile-time isolated.

## Revision Note

Initial revision. This plan intentionally ends before Skia registry capacity,
invalid-typeface caching, bold/style propagation, SDK documentation, and final
cross-contract validation; those belong to sequential Plan 2.
