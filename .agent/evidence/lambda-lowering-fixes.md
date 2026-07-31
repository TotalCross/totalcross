<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Lambda Lowering Fixes Evidence

All commands ran on macOS arm64 with OpenJDK 17.0.12 and Gradle 9.6.1. Full
logs are kept under `TotalCrossSDK/build/lambda-lowering-fixes/` or the
Gradle-agent paths recorded by each summary log.

Focused validation:

- `./gradlew-agent test --tests tc.tools.converter.modernjava.Java8LambdaLoweringTest --no-daemon --console=plain` — exit 0; focused tests passed, including inherited receiver, `POP`, and duplicate enqueue assertions. Log: `focused-tests.log`.
- `./gradlew-agent test --tests 'tc.tools.converter.modernjava.*' --no-daemon --console=plain` — exit 0. Log: `modernjava-tests.log`.
- `git diff --check` — exit 0.

Build validation:

- `./gradlew-agent dist -x test --no-daemon --console=plain` — exit 0. Log: `dist.log`.
- `cmake -S TotalCrossVM -B build -DCMAKE_BUILD_TYPE=Release -G Ninja` — exit 0. Log: `cmake.log`.
- `ninja -C build tcvm` — exit 0; produced `build/libtcvm.dylib`. Log: `tcvm.log`.

Smoke validation:

- `./gradlew-agent runLambdaLoweringFixesRepro -PtcvmDylib=/Users/flsobral/repos/totalcross-github/build/libtcvm.dylib --no-daemon --console=plain` — exit 0. Log: `run-isolated-task.log`; output contains the expected `[PASS] Lambda repro - inherited getGraphics method reference`.
- `./gradlew-agent runModernJavaFeatureSmoke -PtcvmDylib=/Users/flsobral/repos/totalcross-github/build/libtcvm.dylib --no-daemon --console=plain` — exit 0. Log: `run-aggregate-task.log`; output contains all reported Java 8 `[PASS]` lines, Scanner version `1.0`, and the callback line.

TCZ inspection:

- `GraphicsReferenceRepro.tcz` contains exactly `lambda.repro.GraphicsReferenceRepro$$TC$$Lambda$0`.
- `FeatureSmokeApp.tcz` contains the converted `smoke.Java8FeatureSmokeTest` adapters with ordinals `0..32, 34, 35` and no duplicate names.
- `tc.base.misc.tcz` contains exactly `totalcross.io.device.scanner.Scanner$$TC$$Lambda$0`, `$1`, and `$2`.
- Inspection output: `tcz-contents.log` and `all-tcz-contents.log`.
