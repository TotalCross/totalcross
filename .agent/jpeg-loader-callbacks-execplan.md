<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Refactor JpegLoader Callback Managers

This ExecPlan is a living document. The sections `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective` must be kept up to date as work proceeds.

This document follows `.agent/PLANS.md`. Keep it self-contained when revising it: a future implementer should be able to start from this file and the current working tree alone.

## Purpose / Big Picture

TotalCross loads JPEG images from TCZ archives, TotalCross streams, and memory-mapped files, and writes JPEG images to TotalCross streams. The current implementation embeds libjpeg source and destination managers inside `TotalCrossVM/third_party/jpeg/JpegLoader.c`. After this change, those managers live in dedicated files derived from the original libjpeg streaming implementations, while `JpegLoader.c` keeps only TotalCross-specific stream adaptation and image conversion logic.

The observable behavior should not change: JPEG loading and writing should keep working through the existing native methods, but the code is easier to compare with upstream libjpeg/libjpeg-turbo sources and less tangled with TotalCross object handling.

## Progress

- [x] (2026-07-09 America/Sao_Paulo) Inspected the current `JpegLoader.c`, `image_Image.c`, old TotalCross JPEG files, and `jpeg-original` sources.
- [x] (2026-07-09 America/Sao_Paulo) Confirmed `jdatasrc-tj.c` and `jdatadst-tj.c` are memory-only helpers in this tree and are not suitable as the streaming base.
- [x] (2026-07-09 14:36 America/Sao_Paulo) Created `JpegLoader.h` and moved shared JPEG declarations there.
- [x] (2026-07-09 14:36 America/Sao_Paulo) Added callback-based source and destination managers derived from `jpeg-original/jdatasrc.c` and `jpeg-original/jdatadst.c`.
- [x] (2026-07-09 14:36 America/Sao_Paulo) Refactored `JpegLoader.c` and `image_Image.c` to use the new header and managers.
- [x] (2026-07-09 14:36 America/Sao_Paulo) Updated CMake wiring and validated the focused `tcvm` build.
- [x] (2026-07-09 14:36 America/Sao_Paulo) Inlined the old `jpegRead` and `jpegWrite` helpers into `jpegReadCallback` and `jpegWriteCallback`, leaving only the callback entry points.
- [x] (2026-07-09 America/Sao_Paulo) Added `jerror-tc.h` and `jerror-tc.c` based on the original libjpeg `jerror.c`, then moved TotalCross JPEG error handling out of `JpegLoader.c`.
- [x] (2026-07-09 America/Sao_Paulo) Reviewed `jdatasrc-tc.c` and `jdatadst-tc.c` for warning/trace behavior. The source manager still emits `JWRN_JPEG_EOF` through `WARNMS`, and `jerror-tc.c` now routes warning and trace output through `debug()`.

## Surprises & Discoveries

- Observation: `TotalCrossVM/third_party/jpeg/jpeg-original/jdatasrc-tj.c` exposes only `jpeg_mem_src_tj`, and `jdatadst-tj.c` exposes only `jpeg_mem_dest_tj`.
  Evidence: Searching those files shows no callback fields or `FILE *` source/destination state; they only receive memory buffers.

- Observation: The active Android Gradle module uses CMake through `TotalCrossVM/CMakeLists.txt`, not the stale `TotalCrossVM/src/jni/Android.mk` JPEG source list.
  Evidence: `TotalCrossVM/android/tcvm/build.gradle` points `externalNativeBuild.cmake.path` at `../../CMakeLists.txt`.

- Observation: There is no registered CTest or Ninja test target for the native image test in the current build tree, and the JPEG fixture expected by `tuiI_imageLoad_s` is absent.
  Evidence: `ctest --test-dir build -N` reports `Total Tests: 0`, `ninja -C build -t targets | rg -n "test|tc_tests|image|tuiI"` finds no targets, and `rg --files | rg '(^|/)barbara\.jpg$|(^|/)Test\.jar$'` finds no fixture.

- Observation: The original streaming `jdatasrc.c` has one recoverable warning path, while the original streaming `jdatadst.c` has only write-error paths.
  Evidence: `rg -n "WARNMS|TRACEMS|ERREXIT" jpeg-original/jdatasrc.c jpeg-original/jdatadst.c` shows `WARNMS(cinfo, JWRN_JPEG_EOF)` in `jdatasrc.c` and only `ERREXIT` calls in the destination stream path.

## Decision Log

- Decision: Derive the new TotalCross source and destination managers from `jpeg-original/jdatasrc.c` and `jpeg-original/jdatadst.c`.
  Rationale: Those files contain the original streaming structure with 4096-byte buffers, EOF handling, skip behavior, and flush behavior. Replacing `fread` and `fwrite` with TotalCross callbacks is smaller and closer to the existing requirement than starting from the memory-only `*-tj.c` files.
  Date/Author: 2026-07-09 / Codex

- Decision: Store the active `TCJpegIOContext` in `jpeg_common_fields.client_data`.
  Rationale: `client_data` exists in both compression and decompression structs and is intended for application state. This keeps manager structs focused on libjpeg buffer state instead of owning TotalCross stream objects.
  Date/Author: 2026-07-09 / Codex

- Decision: Inline the TotalCross read and write helper bodies into the callback functions.
  Rationale: After moving source and destination managers out of `JpegLoader.c`, `jpegRead` and `jpegWrite` were only called by the callbacks. Keeping separate wrappers added indirection without reuse.
  Date/Author: 2026-07-09 / Codex

- Decision: Add a TotalCross-specific error manager derived from libjpeg `jerror.c`, named `jerror-tc.c`, with declarations in `jerror-tc.h`.
  Rationale: The original `jerror.c` exits the process after printing to stderr. TotalCross needs libjpeg fatal errors to flow through `HEAP_ERROR`, and it needs warning/trace output to use the VM `debug()` facility.
  Date/Author: 2026-07-09 / Codex

## Outcomes & Retrospective

The refactor is implemented. `git diff --check`, `cmake -S TotalCrossVM -B build -DCMAKE_BUILD_TYPE=Release -G Ninja`, and `ninja -C build tcvm` completed successfully for the first callback-manager pass. Later incremental `git diff --check` and `ninja -C build tcvm` runs passed after inlining `jpegRead` and `jpegWrite` into the callbacks, and again after extracting `jerror-tc.c`. The focused native image test was not run because this build has no CTest/Ninja test target and the expected `barbara.jpg` fixture is not present.

## Context and Orientation

`TotalCrossVM/third_party/jpeg/JpegLoader.c` adapts TotalCross image objects to libjpeg. It reads bytes through `jpegRead`, writes bytes through `jpegWrite`, decodes into TotalCross pixel arrays in `jpegLoad`, and encodes from image pixels in `image2jpeg` and `rgb565_2jpeg`.

`TotalCrossVM/src/nm/ui/image_Image.c` owns native methods for `totalcross.ui.image.Image` and currently declares `jpegLoad` and `image2jpeg` manually. This change replaces those manual prototypes with a new shared header.

`TotalCrossVM/third_party/CMakeLists.txt` is the active CMake wiring for the third-party wrappers. It currently adds only `JpegLoader.c` for JPEG.

## Plan of Work

Create `TotalCrossVM/third_party/jpeg/JpegLoader.h` with the `JPEGFILE` structure, callback types, `TCJpegIOContext`, loader entry points, and `jpeg_tc_src`/`jpeg_tc_dest` declarations.

Create `TotalCrossVM/third_party/jpeg/jdatasrc-tc.c` by adapting the streaming half of `jpeg-original/jdatasrc.c`: keep the source manager state and behavior, replace `FILE *` and `fread` with the read callback found through `cinfo->client_data`, and omit the memory-source helper.

Create `TotalCrossVM/third_party/jpeg/jdatadst-tc.c` by adapting the streaming half of `jpeg-original/jdatadst.c`: keep the destination manager state and behavior, replace `FILE *` and `fwrite` with the write callback found through `cinfo->client_data`, and omit the memory-destination helper.

Create `TotalCrossVM/third_party/jpeg/jerror-tc.h` and `TotalCrossVM/third_party/jpeg/jerror-tc.c` by adapting `TotalCrossVM/third_party/jpeg/jerror.c`: keep message formatting, reset, warning counting, and trace gating from the original error manager, replace stderr output with `debug("%s", buffer)`, and replace process exit with `HEAP_ERROR`.

Refactor `JpegLoader.c` to include the new header, remove embedded source/destination managers, and set up `TCJpegIOContext` before calling `jpeg_tc_src` and `jpeg_tc_dest`.

Update `image_Image.c` to include `JpegLoader.h` and remove manual prototypes. Update `TotalCrossVM/third_party/CMakeLists.txt` to compile the two new manager files and expose the JPEG wrapper include directory to `tcvm`.

## Concrete Steps

From the repository root, edit the files named above. Then run:

    git diff --check
    cmake -S TotalCrossVM -B build -DCMAKE_BUILD_TYPE=Release -G Ninja
    ninja -C build tcvm

If the CMake configure step needs to fetch missing native dependencies and fails because of sandboxed network access, rerun it with approval according to the agent permissions rules.

## Validation and Acceptance

`git diff --check` should print no whitespace errors.

The focused CMake build should compile `tcvm`, including `JpegLoader.c`, `jdatasrc-tc.c`, `jdatadst-tc.c`, and `jerror-tc.c`, without duplicate libjpeg symbols or missing header errors.

If native image tests are available, run the existing `tuiI_imageLoad_s` test and expect JPEG loading to still produce a non-null pixel array with the expected dimensions. If a required fixture such as `barbara.jpg` is absent, record the skipped validation rather than adding unrelated binary fixtures.

## Idempotence and Recovery

The source edits are additive except for removing duplicated manager code from `JpegLoader.c`. Re-running CMake and Ninja is safe. If validation fails, inspect the compiler error first; likely recovery points are include paths, callback signatures, or a missing CMake source entry.

## Artifacts and Notes

Keep validation command summaries in the final response. Do not commit generated build products, dependency caches, or local logs.

## Interfaces and Dependencies

At completion, `JpegLoader.h` provides the TotalCross JPEG wrapper interface used by both `JpegLoader.c` and `image_Image.c`. The source and destination manager files depend on libjpeg headers from the configured `JPEG_INCLUDE_DIRS` and on `JpegLoader.h` for `TCJpegIOContext`.
