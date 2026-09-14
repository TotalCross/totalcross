<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# System nanoTime execution state

Updated: 2026-09-14
Branch: `feat/system-nanotime`
Active milestone: complete
Last commit: `test(system): add nanoTime macOS smoke` (final milestone)

## Active paths

- `TotalCrossSDK/src/main/java/jdkcompat/lang/System4D.java`
- `TotalCrossSDK/src/main/java/tc/tools/converter/`
- `TotalCrossSDK/src/test/java/tc/tools/converter/`
- `TotalCrossVM/src/nm/NativeMethods.txt`
- `TotalCrossVM/src/nm/NativeMethodsPrototypes.txt`
- `TotalCrossVM/src/nm/NativeMethods.h`
- `TotalCrossVM/src/init/nativeProcAddressesTC.c`
- `TotalCrossVM/src/nm/lang/System.c`
- `TotalCrossVM/src/util/utils.h`
- `TotalCrossVM/src/util/utils.c`
- `TotalCrossVM/src/util/posix/utils_c.h`
- `TotalCrossVM/src/util/win/utils_c.h`
- `TotalCrossVM/CMakeLists.txt`
- `TotalCrossVM/vc2008/TCVM.vcproj`

## Next concrete action

Finish static/native structural coverage for the platform helper implementations,
run header and diff checks, then perform the allowed macOS native build at the
Milestone 2 boundary. Do not run other platform builds.

## Focused validation completed

- Read the pasted requirements file.
- Read `AGENTS.md` and `.agent/PLANS.md` in full.
- Confirmed `perf/image-scroll-prefetch` was up to date before branch creation.
- Confirmed the repository initially had only unrelated untracked artifacts;
  they are deliberately out of scope.
- Milestone 1 focused test passed: `./gradlew-agent test
  --tests tc.tools.converter.SystemNanoTimeConverterTest --console=plain`;
  full and compact logs are under `TotalCrossSDK/agent-logs/`.
- Milestone 2 macOS native build passed with
  `cmake -S TotalCrossVM -B build/system-nanotime-macos -G Ninja
  -DCMAKE_BUILD_TYPE=Release` followed by `cmake --build
  build/system-nanotime-macos`; log: `/tmp/system-nanotime-macos-build.log`;
  output: `build/system-nanotime-macos/libtcvm.dylib`.
- Milestone 3 SDK distribution build passed; log: `/tmp/system-nanotime-sdk-dist.log`.
- Milestone 3 focused SDK test passed; log: `/tmp/system-nanotime-sdk-test.log`.
- Milestone 3 final macOS native rebuild passed; log:
  `/tmp/system-nanotime-macos-final-build.log`.
- Milestone 3 macOS smoke passed with `deltaNanos=21110084`; log:
  `TotalCrossSDK/agent-logs/20260914-190516-runSystemNanoTimeSmokeMacOS-full.log`.

## Deferred validation

The SDK focused test ran at the end of Milestone 1. The macOS native build ran
at the end of Milestone 2 and passed. The final SDK distribution build, focused
test, native rebuild, and smoke all passed. The first smoke deployment attempt
failed because the task passed a standalone class to `tc.Deploy`; the task was
corrected to create a JAR and the rerun passed. No other platform builds were
run.

## Active decisions and blockers

Use `jlS_nanoTime` for `java/lang/System.nanoTime()` and keep `Vm` unchanged.
The `logical-commits` skill named by the request is unavailable; milestone
commits will follow the repository's commit-title rules manually.

## Deliberate out-of-scope local files

Leave all unrelated benchmark logs, generated launcher files, `IOSDateFixture.tcz`,
and existing `.agent/plans/image-optimization-mask-*` files untouched.

## Resume command

`git status --short -- .agent/plans/system-nanotime.md .agent/state/system-nanotime.md .agent/evidence/system-nanotime.md .agent/archive/system-nanotime-history.md .agent/reports/system-nanotime-editorial.md TotalCrossSDK/build.gradle TotalCrossSDK/src/smokeTest/java/totalcross/sys/SystemNanoTimeSmokeApp.java TotalCrossSDK/src/test/java/tc/tools/converter TotalCrossVM/src/nm TotalCrossVM/src/util TotalCrossVM/CMakeLists.txt TotalCrossVM/vc2008/TCVM.vcproj`
