<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Lazy explicit JPEG factories state

- Branch: `perf/image-jpeg-factories-lazy`
- Frozen Phase-3 base: `224682b15a244201718701ad24be8f9d54b3fa71`
- Phase-3 runtime candidate retained for handoff: `d2f8195b9faf828f4ee04154c93a93e1136c5bd4`
- Active milestone: lazy explicit JPEG factory closeout
- Last runtime commit: `3dd8ccd78d5ed47b6b7a1e5cf1e86b3d8d4fca7c`
- Current runtime/test candidate: `b54380800131f036a50879b921b8d30d3af1f154`
- Final docs closeout tip: this docs-only closeout commit at the final branch tip
- Active paths: `TotalCrossSDK/src/main/java/totalcross/ui/image/Image.java`,
  `ImagePipeline.java`, new `ImageDecodePolicy.java`, focused JPEG tests,
  native image registration/bridge files, existing deployed image smokes.
- Deliberately out of scope: Phase-4 lifecycle/cache-budget work, generated
  native dependency directories, unrelated dirty-worktree files.
- Completed: exact-base branch bootstrap; immutable policy/pipeline resolver;
  native private decode bridges and retired public eager registrations; native
  TCZ/filesystem source capture without a Java full-file copy; converter ABI
  coverage; real decoder policy matrix; deployed macOS smokes and benchmark;
  compact JPEG factory smoke; Android build/package check; hosted matrix;
  headers and diff validation.
- Focused validation: `totalcross.ui.image.*` tests passed; SDK
  `dist -x test` passed; Release macOS software-Skia CMake/Ninja passed;
  legacy lazy smoke and focused JPEG factory smoke passed; 60-sample factory
  benchmark passed; Android `:tcvm:externalNativeBuildCleanRelease`,
  `:app:assembleStandardRelease`, and `:app:bundleStandardRelease` passed.
  The exact rebuilt AAB was packaged with the updated native template, installed
  with normal `adb install -r`, and ran successfully on the physical device.
- Benchmark result: eager reference was about 8–9 ms, factory-only 0–1 ms
  with zero decode/materialization, first draw about 5 ms with one decode and
  one materialization, and repeated draws about 5–6 ms with one reusable
  decode/materialization per sample. This is accounting/sanity evidence, not
  a precise old-versus-new performance claim.
- Actual decode-policy matrix: filesystem odd JPEG `1601x901` passed BEST_FIT
  at 1/8, 1/4, 1/2, and 1/1 plus EXPLICIT_RATIO at 1/8, 1/2, 3/4, and 1/1,
  with real `getPixels()` barriers and independent full-decode raster checks.
  Packaged/TCZ deployed coverage also passed. Converter ABI coverage confirms
  the public factories remain Java and the two private bridges are native.
- Deployed source capture: `nativeSourceCapture=true`; TCZ/filesystem path
  factories expose native-owned encoded bags with no Java byte-array backing.
- Compact/GPU result: the macOS and physical Android production-OpenGL compact
  smokes passed RGB565 and GRAY8 backing, one decode each, zero temporary RGBA
  bytes, zero promotions, reuse, screen draw, and zero raster-only counters.
- Hosted matrix: run `34209665445`, head
  `b54380800131f036a50879b921b8d30d3af1f154`, passed SDK, macOS, Windows,
  Windows-native-legacy, Linux amd64/arm64/arm32v7, Android, and iOS; only
  the intentional Linux arm32 cross job was skipped.
- Android physical result: device `192.168.1.154:43289` accepted the exact
  rebuilt APK and the focused smoke reported `rgb565=true`, `gray8=true`,
  `screenDraw=true`, `rasterCountersZero=true`, and `overallPass=true`.
- Final decision: `GO` for Phase 4 after this closeout. Phase 4 is not started
  by this task.
- Next action: stop after the docs-only closeout and hand off the exact final
  branch tip, without beginning Phase 4.
- Resume command: `sed -n '1,220p' .agent/state/image-jpeg-factories-lazy.md`
