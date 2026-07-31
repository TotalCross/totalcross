<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Lambda Lowering Fixes State

Last checkpoint: 2026-07-31 17:55 America/Sao_Paulo.

The implementation is complete. `Java8LambdaLowering` now resolves inherited
receivers from class-file hierarchy metadata using the deployer classpath,
discards non-void implementation results for void SAM methods with `POP` or
`POP2`, and `J2TC` discovers synthetic adapters at the common class-processing
boundary. Focused lowering tests, the modern Java converter test group, SDK
distribution, native macOS VM build, isolated smoke, aggregate smoke, and TCZ
inspection all passed.

Useful paths:

- Full validation logs: `TotalCrossSDK/build/lambda-lowering-fixes/`.
- Native runtime: `build/libtcvm.dylib`.
- Isolated executable: `TotalCrossSDK/build/lambda-lowering-fixes/classes/install/macos/GraphicsReferenceRepro`.
- Aggregate executable: `TotalCrossSDK/build/feature-smoke/classes/install/macos/FeatureSmokeApp`.
- Final report: `.agent/reports/lambda-lowering-fixes-editorial.md`.

Last successful commands:

    ./gradlew-agent test --tests tc.tools.converter.modernjava.Java8LambdaLoweringTest --no-daemon --console=plain
    ./gradlew-agent test --tests 'tc.tools.converter.modernjava.*' --no-daemon --console=plain
    ./gradlew-agent dist -x test --no-daemon --console=plain
    cmake -S TotalCrossVM -B build -DCMAKE_BUILD_TYPE=Release -G Ninja
    ninja -C build tcvm
    ./gradlew-agent runLambdaLoweringFixesRepro -PtcvmDylib=/Users/flsobral/repos/totalcross-github/build/libtcvm.dylib --no-daemon --console=plain
    ./gradlew-agent runModernJavaFeatureSmoke -PtcvmDylib=/Users/flsobral/repos/totalcross-github/build/libtcvm.dylib --no-daemon --console=plain

Next action: preserve the report and evidence, update the ExecPlan outcome,
and review the final scoped diff. No deferred validation remains.
