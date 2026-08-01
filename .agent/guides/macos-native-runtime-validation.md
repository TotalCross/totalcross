<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Native macOS runtime validation

Use this guide for every milestone that changes C, C++, Skia, native fonts,
native image synchronization, native window scale, or methods annotated with
`@ReplacedByNativeOnDeploy`.

## Two distinct lanes

### JavaSE/AWT lane

This lane runs:

    java ... totalcross.Launcher ...

It validates Java implementations and AWT integration. Running it on a Mac does
not make it native macOS validation.

### Native macOS lane

This lane runs a fixture compiled against the current SDK, deployed for macOS,
and executed through a native TotalCross application using the freshly built
`libtcvm.dylib`.

Only this lane proves changed native VM and Skia behavior.

Record the lane explicitly in every evidence entry.

## Build identity

From the repository worktree:

    cd TotalCrossSDK
    ./gradlew-agent dist -x test --no-daemon --console=plain \
      > ../artifacts/logical-ui-scaling/logs/sdk-dist.log 2>&1

Then:

    cd ..
    cmake -S TotalCrossVM -B build-logical-ui \
      -DCMAKE_BUILD_TYPE=Release -G Ninja \
      > artifacts/logical-ui-scaling/logs/macos-cmake.log 2>&1
    ninja -C build-logical-ui tcvm \
      > artifacts/logical-ui-scaling/logs/macos-tcvm.log 2>&1

Use the current repository's supported target names when they differ. Do not use a
packaged dylib from another checkout or revision.

Record:

    git rev-parse HEAD
    shasum -a 256 build-logical-ui/libtcvm.dylib

## Fixture tasks

Add focused Gradle tasks, following the existing modern-Java native smoke pattern,
with responsibilities equivalent to:

    compileLogicalUiScalingSmoke
    deployLogicalUiScalingSmokeMacOS
    runLogicalUiScalingSmokeMacOS

The compile task uses the generated SDK. The deploy task creates the macOS
application. The run task accepts the exact dylib path, copies or selects it using
the repository's existing smoke mechanism, launches the generated executable, and
captures stdout, stderr, exit status, PNGs, and assertion JSON.

Do not invoke `totalcross.Launcher` in the native run task.

## Direct executable launch

Launch the generated executable directly rather than through `open`, so `$!`
belongs to the process that owns the application window:

    APP_EXECUTABLE=".../install/macos/<FixtureName>"
    APP_LOG="artifacts/logical-ui-scaling/logs/native-macos-app.log"

    "$APP_EXECUTABLE" >"$APP_LOG" 2>&1 &
    APP_PID=$!

If the deploy output is an `.app` bundle, execute:

    <Fixture>.app/Contents/MacOS/<Fixture>

Avoid `open -a` because its returned PID may not be the window owner.

## Runtime identity

Before accepting the run:

- verify the native executable exists;
- verify the freshly built dylib exists;
- copy it through the same mechanism used by repository smoke tasks;
- hash the source and deployed dylib and require equality;
- record the executable path relative to the worktree;
- record the tested commit;
- require exit status zero;
- reject loader errors or fallback to another installed TotalCross runtime.

Use `otool -L` or the repository's runtime-copy evidence to show where the
executable resolves `libtcvm.dylib`. Do not rely on a successful launch alone.

## Native assertions

The native application must emit a concise machine-readable result containing:

    renderer
    logical window width and height
    physical backing width and height
    contentScale
    fontScale
    default image logical and physical dimensions
    scaled image logical and physical dimensions
    text metric assertions
    barcode run count
    upload and readback assertions
    PNG paths
    final pass or failure

A native build pass without execution is not enough.

## Skia and non-Skia builds

Use the repository's real CMake switches. Do not invent flags.

For Skia, build and run the fixture with the matching Skia-enabled dylib.

For the non-Skia native path, create a separate build directory and run the same
fixture if that macOS configuration is supported. If it is not supported, record
the exact configuration failure for maintainer review; do not substitute Java
Launcher output.

## Platform boundary

During implementation, do not run iOS or Android native builds. Keep the feedback
loop on native macOS.

Android is run only in the final cross-platform milestone. iOS is optional unless
explicitly requested or promoted to required scope.

## Evidence

For each native run, record:

- commit;
- SDK build log;
- CMake and Ninja logs;
- dylib source and deployed hashes;
- deploy log;
- executable path;
- renderer configuration;
- process exit status;
- assertion summary;
- generated PNG hashes;
- screenshot path and privacy review.

Do not label a JavaSE/AWT run as native macOS evidence.
