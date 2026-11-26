<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Building the TotalCross SDK and VM

This guide builds the Java SDK first and then the native TotalCross VM and its
launcher. The SDK must be built first because it produces the runtime TCZ files
used by the VM and by Android packaging.

Run all commands from the repository root unless a command explicitly changes
directory.

## 1. Prerequisites

The SDK build requires:

- JDK 17;
- Bash; and
- internet access for Gradle and Maven dependencies.

Verify that Java 17 is active before building:

```bash
java -version
```

Using JDK 21 currently causes `LinkedList4D` to conflict with the sequenced
collection methods added to `java.util.List` and `java.util.Deque`.

The Linux VM build additionally requires:

- a C and C++ compiler;
- CMake;
- Ninja;
- Git; and
- SDL2 development files, version 2.0.12 or newer.

On Debian or Ubuntu, install the native build tools with:

```bash
sudo apt install build-essential cmake ninja-build git libsdl2-dev
```

The CMake configuration fetches the pinned native dependencies automatically.
This includes Skia, SQLite, mbedTLS, zlib, minizip, libpng, libjpeg-turbo,
axTLS, and QR-code support.

## 2. Build the SDK

From the repository root:

```bash
cd TotalCrossSDK
./gradlew-agent dist
cd ..
```

`gradlew-agent` keeps terminal output compact and writes timestamped logs to
`TotalCrossSDK/agent-logs/`. Each invocation creates a full log and a shorter
agent-oriented summary. For more Gradle detail, use:

```bash
cd TotalCrossSDK
./gradlew-agent dist --info --stacktrace
cd ..
```

The SDK build produces:

```text
TotalCrossSDK/dist/totalcross-sdk.jar
TotalCrossSDK/dist/libs/
TotalCrossSDK/dist/vm/tc.base.lang.tcz
TotalCrossSDK/dist/vm/tc.base.util.tcz
TotalCrossSDK/dist/vm/tc.base.misc.tcz
TotalCrossSDK/dist/vm/TCUI.tcz
TotalCrossSDK/dist/vm/TCFont.tcz
```

The `deployTcbaselang`, `deployTcbaseutil`, `deployTcbasemisc`, and
`deployTcui` Gradle tasks run `tc.Deploy`. The J2TC converter translates Java
bytecode into TotalCross bytecode, and the TCZ writer stores it in the generated
archives under `TotalCrossSDK/build/libs/`. The `dist` task copies those
archives into `TotalCrossSDK/dist/vm/`.

Run the SDK tests separately when required:

```bash
cd TotalCrossSDK
./gradlew-agent test
cd ..
```

## 3. Build the Linux VM and launcher

Configure a Release build:

```bash
cmake -S TotalCrossVM -B build/vm \
  -G Ninja \
  -DCMAKE_BUILD_TYPE=Release
```

Build the VM library and launcher:

```bash
cmake --build build/vm \
  --target tcvm Launcher \
  --parallel
```

The principal outputs are:

```text
build/vm/libtcvm.so
build/vm/Launcher
```

On macOS, install the prerequisites with `brew install cmake ninja sdl2` and
use the same CMake commands. The VM library output is `libtcvm.dylib`.

## 4. Assemble and run an application

The launcher needs the VM library, the SDK runtime TCZ files, and an application
TCZ in its working directory. The following example assumes the application is
named `MyApp.tcz`:

```bash
mkdir -p build/run
cp build/vm/Launcher build/run/
cp build/vm/libtcvm.so build/run/
cp TotalCrossSDK/dist/vm/*.tcz build/run/
cp /path/to/MyApp.tcz build/run/
```

Run the application by passing its name without the `.tcz` suffix:

```bash
cd build/run
./Launcher MyApp
```

The Linux launcher looks for `libtcvm.so` in its current directory first and
translates the `MyApp` argument into `MyApp.tcz`.

## 5. Build the Android VM

The Android build requires JDK 17, Android SDK 35, and Android NDK
`28.2.13676358`. Configure `ANDROID_HOME` or `ANDROID_SDK_ROOT` before running
the build. Build the SDK first as described above because the Android app embeds
the SDK runtime TCZ files.

Fetch the Android native dependencies and build a debug APK:

```bash
cd TotalCrossVM/android
./gradlew :tcvm:fetchNativeDependencies
./gradlew :app:assembleStandardDebug
cd ../..
```

For a release APK, use:

```bash
cd TotalCrossVM/android
./gradlew :app:assembleStandardRelease
cd ../..
```

Android artifacts are written under
`TotalCrossVM/android/app/build/outputs/apk/`. Native module outputs are under
`TotalCrossVM/android/tcvm/build/`.

## 6. Build the iOS VM

The iOS build requires macOS, Xcode, CMake, CocoaPods, and an appropriate Apple
signing configuration. After building the SDK, run:

```bash
cd TotalCrossVM/xcode
cmake ../ -GXcode
pod install
ruby ../../scripts/fix-ios-xcode-dependencies.rb \
  TCVM.xcodeproj/project.pbxproj
xcodebuild \
  -workspace TotalCross.xcworkspace \
  -scheme TotalCross \
  archive
cd ../..
```

Open `TotalCrossVM/xcode/TotalCross.xcworkspace` in Xcode when an interactive
build, device selection, or signing configuration is needed.

## Troubleshooting

### `LinkedList4D` reports incompatible `reversed()` methods

The SDK is being compiled with JDK 21. Activate JDK 17 and rerun
`./gradlew-agent dist`.

### CMake cannot find SDL2

Install the SDL2 development package, delete only the failed VM build directory
if CMake cached the missing dependency, and configure again:

```bash
cmake -S TotalCrossVM -B build/vm \
  -G Ninja \
  -DCMAKE_BUILD_TYPE=Release
```

### The launcher cannot load `libtcvm`

Run the launcher from a directory containing `libtcvm.so` on Linux or
`libtcvm.dylib` on macOS. The application and SDK runtime TCZ files must also be
available from the launcher's working directory.
