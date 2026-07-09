<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Migrate embedded MiniZip to prebuilt minizip-ng

This ExecPlan is a living document. Maintain the `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective` sections according to `.agent/PLANS.md`.

## Purpose / Big Picture

The VM now links the static minizip-ng 4.2.2 compatibility library supplied by totalcross-depot-tools instead of compiling the legacy MiniZip source tree. Existing `ZipStream` native methods keep adapting `totalcross.io.Stream` through the classic `unz*` and `zip*` API.

## Progress

- [x] (2026-07-09 America/Sao_Paulo) Updated the depot-tools ref and fetched minizip-ng artifacts for every published target archive.
- [x] (2026-07-09 America/Sao_Paulo) Added CMake, Android, and CI dependency wiring.
- [x] (2026-07-09 America/Sao_Paulo) Extracted the TotalCross stream callback bridge to `third_party/minizip`.
- [x] (2026-07-09 America/Sao_Paulo) Removed `TotalCrossVM/src/minizip`.
- [x] (2026-07-09 America/Sao_Paulo) Built macOS `tcvm` and the Android arm64-v8a native target; compiled and deployed the ZIP smoke application.
- [ ] Run the deployed ZIP smoke application on a macOS host with an available SDL/OpenGL display, or another supported runtime host.

## Surprises & Discoveries

- Observation: the macOS minizip-ng archive uses Apple Security random APIs.
  Evidence: linking initially failed on `SecRandomCopyBytes`; linking CoreFoundation and Security frameworks resolves the static-library dependency.

- Observation: the local headless macOS environment cannot start the UI smoke application.
  Evidence: `SDL_VIDEODRIVER=offscreen ./FeatureSmokeApp` exits 107 because SDL cannot initialize OpenGL/GLES.

## Decision Log

- Decision: retain the classic MiniZip compatibility API instead of migrating `ZipFile.c` to the lower-level `mz_*` API.
  Rationale: minizip-ng 4.2.2 exports the required compatible entry points and this limits the change to the TotalCross I/O adapter.
  Date/Author: 2026-07-09 / Codex

- Decision: keep WinCE source compatibility in `MinizipIO.c`, but require an externally supplied `MINIZIP_DIR` for CMake WinCE builds.
  Rationale: the requested depot release contains no WinCE archive; `vc2008` and `src/jni/Android.mk` are discontinued and intentionally unchanged.
  Date/Author: 2026-07-09 / Codex

## Outcomes & Retrospective

The CMake configure found `MINIZIP::MINIZIP`, `cmake --build build/minizip --target tcvm Launcher --parallel` succeeded on macOS, and `:tcvm:externalNativeBuildRelease` succeeded for Android arm64-v8a. The Java smoke compiles and deploys; executing it remains dependent on a graphics-capable runtime host.

## Context and Orientation

`TotalCrossVM/src/nm/util/zip/ZipFile.c` owns native methods for `ZipStream` and `ZipFile`. `TotalCrossVM/third_party/minizip/MinizipIO.c` provides its callbacks over a TotalCross stream. `TotalCrossVM/CMakeLists.txt` discovers the imported `MINIZIP::MINIZIP` target and folds its archive into iOS `libtcvm.a`.

## Plan of Work

The implementation changes `TotalCrossVM/deps/totalcross-depot-tools.ref` to `minizip-ng-4.2.2`, fetches all published `.tar.gz` platform archives in CI, and adds the Android fetch task inputs and outputs. CMake uses the depot-tools minizip-ng modules, links the imported archive, and links CoreFoundation/Security on Apple targets.

The old `TOTALCROSS` block from MiniZip `ioapi.c/.h` is moved into `MinizipIO.c/.h`. It retains the VM callback contract: reads and writes use reusable byte-array buffers, seek maps directly to `RandomAccessStream` origins, callback close leaves the caller stream open, and callback errors surface the pending TotalCross exception.

## Concrete Steps

From the repository root, fetch the pinned checkout and target artifact with:

    TotalCrossVM/deps/fetch-depot-tools.sh
    bash TotalCrossVM/deps/totalcross-depot-tools/minizip-ng/fetch.sh --platform macos --arch arm64

Then configure and build with:

    cmake -S TotalCrossVM -B build/minizip -DCMAKE_BUILD_TYPE=Release -G Ninja
    cmake --build build/minizip --target tcvm Launcher --parallel

## Validation and Acceptance

`cmake` must print `Found MINIZIP`, the VM must link without `unz*` or `zip*` unresolved symbols, and Android must complete:

    cd TotalCrossVM/android
    ./gradlew :tcvm:fetchNativeDependencies :tcvm:externalNativeBuildRelease

The deployed `ZipStreamSmokeTest` writes DEFLATE and STORED entries, reopens them, verifies content and EOF, and prints a `[PASS]` marker on a graphics-capable VM host.

## Idempotence and Recovery

The fetch scripts replace only their requested platform directory. Re-running CMake, Gradle, or the smoke deployment is safe. Do not commit depot-tools `local/` artifacts, Gradle output, or CMake build directories.

## Artifacts and Notes

The legacy `src/minizip` directory is intentionally removed. References in `TotalCrossVM/vc2008/TCVM.vcproj` and `TotalCrossVM/src/jni/Android.mk` are intentionally left untouched because those build paths are discontinued.

## Interfaces and Dependencies

The only new internal interface is `tcMinizipInitialize(Context, TCMinizipNativeP, TCObject)` in `MinizipIO.h`. It initializes `zlib_filefunc_def` for minizip-ng's compatibility layer; the Java and exported native method interfaces remain unchanged.
