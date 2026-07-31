<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Lambda Return-Types Regression State

Active milestone: complete.

Revision: `794ef05effa1c13adaf91639c609e51f4848f5ea`

Environment: macOS 26.5.2 arm64, OpenJDK 17.0.12, Gradle 9.6.1.

Matching VM: `/Users/flsobral/repos/totalcross-github/build/libtcvm.dylib`

Matching VM SHA-256: `b9fba1e4dce56cb10259525674f49359c82a6585e49c78051f2285905aa031bf`

Modified paths for this execution:

- `TotalCrossSDK/build.gradle`
- `TotalCrossSDK/src/test/java/tc/tools/converter/modernjava/Java8LambdaLoweringTest.java`
- `TotalCrossSDK/src/test/java/tc/tools/converter/modernjava/ModernJavaClassFileFixtures.java`
- `TotalCrossSDK/src/test/resources/modernjava/lambda-repro/LambdaReturnDiscardRepro.java`
- `TotalCrossSDK/src/test/resources/modernjava/lambda-repro/ReturnDiscardAction.java`
- `.agent/lambda-return-types-regression-execplan.md`
- `.agent/evidence/lambda-return-types-regression.md`
- `.agent/reports/lambda-return-types-regression-editorial.md`
- `.agent/state/lambda-return-types-regression.md`

Successful validations:

- Focused `Java8LambdaLoweringTest`: exit 0, 23 tests.
- Modern-Java converter group: exit 0.
- `dist -x test`: exit 0.
- CMake configure and `ninja -C build tcvm`: exit 0.
- `runLambdaReturnTypesSmoke`: exit 0, 11 pass lines, summary `total=11, passed=11, failed=0`.
- `runLambdaLoweringFixesRepro`: exit 0, inherited `getGraphics` pass.
- `runModernJavaFeatureSmoke`: exit 0, Java 8 suite reports 28 tests and Scanner/callback checks pass.
- Predicate aggregate follow-up: exit 0; dedicated `[CASE]` and `[PASS] Java 8 - Predicate default methods` lines present; `and`, `or`, and `negate` pass; no Predicate-related `NoSuchMethodError`, `[FAIL]`, or `Aborting program`. Complete log: `TotalCrossSDK/agent-logs/20260731-182844-runModernJavaFeatureSmoke-full.log`.
- Complete SDK `test`: exit 0, 43 tests, 43 passed, 0 failed, 0 skipped.
- `git diff --check`: exit 0.

Key evidence:

- Environment: `TotalCrossSDK/build/lambda-return-types-regression/environment.log`
- Focused test: `TotalCrossSDK/build/lambda-return-types-regression/focused-lambda-test.log`
- Modern-Java tests: `TotalCrossSDK/build/lambda-return-types-regression/modernjava-tests.log`
- SDK distribution: `TotalCrossSDK/build/lambda-return-types-regression/dist.log`
- Native build: `TotalCrossSDK/build/lambda-return-types-regression/cmake.log`, `tcvm.log`
- New smoke: `TotalCrossSDK/build/lambda-return-types-regression/run-return-types-smoke.log`
- Adapter inventory: `TotalCrossSDK/build/lambda-return-types-regression/all-tcz-inventory.log`
- Existing smokes: `run-inherited-smoke.log`, `run-aggregate-smoke.log`
- Full suite: `TotalCrossSDK/build/lambda-return-types-regression/full-sdk-tests.log`
- XML results: `TotalCrossSDK/build/test-results/test/`

Logical commits:

- `da7a45c77` — `test(sdk): harden modern Java smoke validation`
- `ee7520fda` — `fix(sdk): lower lambda method references safely`

Next command: none; the execution plan is complete.
