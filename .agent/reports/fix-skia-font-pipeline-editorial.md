<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Editorial Report — Skia Font Pipeline

## Editorial Summary

The two-plan font correction is complete. Skia now owns TTF-backed typefaces,
keeps valid registry indices stable without a 32-entry ceiling, applies
synthetic bolding consistently, and documents the renderer-specific contract.
Legacy PalmFont loading and assets remain isolated and supported.

## Original Plan versus Actual Outcome

Plan 1 separated Skia and PalmFont ownership and made TTF lookup deterministic.
Plan 2 hardened the registry, propagated bold style, reconciled SDK
documentation, and required final native and SDK validation. All implementation
requirements were delivered. The runtime tests specifically exercising Plan 1's
`tufF_fontCreate()` could not be executed because the available CMake
configuration does not expose the corresponding native test-suite target. A
standalone Skia fixture supplied the focused registry and rendering coverage.

## What Changed

- `skia.cpp` and `skia_internal.h` now use a vector of valid `SkTypeface`
  objects. Failed creation returns `-1` without changing either cache.
- `skia.h`, `skia.cpp`, `skia_primitives.cpp`, `PalmFont_c.h`,
  `font_FontMetrics.c`, and `GraphicsPrimitivesText_c.h` carry an explicit
  bold flag through metrics, measurement, dirty-region sizing, and drawing.
- The existing Skia fixture covers invalid data, repeated names, 40 unique
  typefaces, bold rendering, and restoration to plain rendering.
- `Font.java` documents generated legacy font archives, generic TCZ TTF
  lookup, default fallback, and renderer-specific bold behavior.
- The final cleanup commit removes the obsolete default-font bold warning while
  preserving the existing `getFont` API, cache, and legacy flow.

## Decisions and Trade-offs

Stable vector positions preserve the meaning of Java-visible `skiaIndex` for
the lifetime of the VM. Registry entries are never erased or reordered.

`SkFont::setEmbolden(bool)` was used instead of guessing a bold resource name.
This keeps style independent of resource naming, at the cost of synthetic
rather than family-specific bold outlines.

The fixture reads the checked-in Roboto TTF at runtime, so it tests real font
data without adding a duplicate binary to the repository.

## Unexpected Problems and Discoveries

The first version of the standalone fixture compared backing pixel arrays and
missed the rendered bold difference. Reading through `skia_getPixel()` matched
the authoritative Skia surface and made the test pass.

The existing macOS CMake configuration builds the native startup test but not
the legacy `ENABLE_TEST_SUITE` VM target. The focused fixture was therefore
compiled directly against the rebuilt `libtcvm.dylib`.

## Validation and Measurable Results

- `python3 scripts/validate-copyright-headers.sh --files ...`: passed for all
  changed source, test, documentation, state, and report files.
- `git diff --check`: passed for milestone and final series diffs.
- `ninja -C build-window-startup-macos`: passed with Skia enabled on macOS.
- `./build-window-startup-macos/window_startup_native_test`: passed.
- Direct Skia fixture: passed invalid-cache, repeated-cache, 40-entry
  capacity, bold-rendering, plain-reset, and existing surface assertions.
- `TotalCrossSDK/gradlew-agent clean dist`: passed.
- Plan 1 `tufF_fontCreate()` runtime tests: not executed; the corresponding
  native CMake test-suite target is unavailable in this checkout.

The finalization commit is `d35933a57`. The Plan 2 checkpoint commits are
`62d4afbba`, `bc6085176`, `d712b2df4`, and `374d55370`; Plan 1 supplied
`bfac1883c` and `7f4f11d2a`.

## Useful Evidence and Examples

The direct fixture accepts the repository’s `Roboto Regular.ttf` path as its
argument. It inserts 40 uniquely named valid typefaces and re-queries every
index after insertion, proving both capacity and stability. It also renders a
plain glyph, a bold glyph, and a later plain glyph, asserting that the first
and third surfaces match while the bold surface differs.

The commands wrote output to local temporary logs referenced by
`.agent/state/fix-skia-font-pipeline-02.md`; raw logs are intentionally not
duplicated here.

## Limitations, Remaining Work, and Open Questions

No Windows, Linux, Android, iOS, WinCE, or cross-target builds were run, as
required by the active plans. The CMake checkout does not expose the native
test-suite target needed for the Plan 1 `tufF_fontCreate()` runtime tests. The
deployed package’s final TTF inventory and behavior on every supported platform
remain platform-release validation.

## Possible Article Angles

- Why a fixed native font cache can silently break custom-font loading.
- Keeping legacy binary font ownership separate from a modern TTF renderer.
- Passing visual style explicitly to prevent shared-renderer state leaks.

## Suggested Narrative

Start with the confusing symptom—custom fonts falling through to legacy
loading—then show the ownership split and deterministic resource resolution.
Follow with the registry failure/capacity fix, use synthetic emboldening as the
style boundary, and close with the focused native fixture and SDK build.

## Claims Requiring Human Review

- Confirm that all release packaging paths load the intended TTF resources in
  every supported Skia platform.
- Confirm that synthetic bold is visually acceptable compared with any
  platform-specific font-family expectations.
- Decide whether the two earlier no-amend commit-message exceptions should be
  handled by a maintainer during branch preparation.
