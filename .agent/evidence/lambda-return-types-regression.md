<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Lambda Return-Types Regression Evidence

Environment: macOS 26.5.2 arm64, OpenJDK 17.0.12, Gradle 9.6.1.
Revision: `794ef05effa1c13adaf91639c609e51f4848f5ea`.
Matching VM: `/Users/flsobral/repos/totalcross-github/build/libtcvm.dylib`.
VM SHA-256: `b9fba1e4dce56cb10259525674f49359c82a6585e49c78051f2285905aa031bf`.

Validation results:

- `./gradlew-agent test --tests tc.tools.converter.modernjava.Java8LambdaLoweringTest --no-daemon --console=plain` — exit 0, 23 tests. Log: `TotalCrossSDK/build/lambda-return-types-regression/focused-lambda-test.log`.
- `./gradlew-agent test --tests 'tc.tools.converter.modernjava.*' --no-daemon --console=plain` — exit 0. Log: `modernjava-tests.log`.
- `./gradlew-agent dist -x test --no-daemon --console=plain` — exit 0. Log: `dist.log`.
- `cmake -S TotalCrossVM -B build -DCMAKE_BUILD_TYPE=Release -G Ninja` — exit 0. Log: `cmake.log`.
- `ninja -C build tcvm` — exit 0. Log: `tcvm.log`.
- `./gradlew-agent runLambdaReturnTypesSmoke -PtcvmDylib=/Users/flsobral/repos/totalcross-github/build/libtcvm.dylib --no-daemon --console=plain` — exit 0. Log: `run-return-types-smoke.log`.
- `./gradlew-agent runLambdaLoweringFixesRepro -PtcvmDylib=/Users/flsobral/repos/totalcross-github/build/libtcvm.dylib --no-daemon --console=plain` — exit 0. Log: `run-inherited-smoke.log`.
- `./gradlew-agent runModernJavaFeatureSmoke -PtcvmDylib=/Users/flsobral/repos/totalcross-github/build/libtcvm.dylib --no-daemon --console=plain` — exit 0. Log: `run-aggregate-smoke.log`.
- `./gradlew-agent test --no-daemon --console=plain` — exit 0, 43 tests, 43 passed, 0 failures, 0 errors, 0 skipped. Log: `full-sdk-tests.log`; XML results: `TotalCrossSDK/build/test-results/test/`.
- `git diff --check` — exit 0.

The new TCZ inventory contains exactly these eleven unique adapters:

    lambda.repro.LambdaReturnDiscardRepro$$TC$$Lambda$0
    lambda.repro.LambdaReturnDiscardRepro$$TC$$Lambda$1
    lambda.repro.LambdaReturnDiscardRepro$$TC$$Lambda$2
    lambda.repro.LambdaReturnDiscardRepro$$TC$$Lambda$3
    lambda.repro.LambdaReturnDiscardRepro$$TC$$Lambda$4
    lambda.repro.LambdaReturnDiscardRepro$$TC$$Lambda$5
    lambda.repro.LambdaReturnDiscardRepro$$TC$$Lambda$6
    lambda.repro.LambdaReturnDiscardRepro$$TC$$Lambda$7
    lambda.repro.LambdaReturnDiscardRepro$$TC$$Lambda$8
    lambda.repro.LambdaReturnDiscardRepro$$TC$$Lambda$9
    lambda.repro.LambdaReturnDiscardRepro$$TC$$Lambda$10

The inherited TCZ contains `lambda.repro.GraphicsReferenceRepro$$TC$$Lambda$0`.
The aggregate TCZ contains the Java 8 adapters with ordinals `0..32, 34, 35`.
`tc.base.misc.tcz` contains Scanner adapters `$0`, `$1`, and `$2`. No duplicate
names were found in the relevant per-TCZ inventories.

The new runtime transcript contains eleven `[PASS] Lambda return discard - ...`
lines and `[SUMMARY] Lambda return discard - total=11, passed=11, failed=0`.
No verifier error, stack error, converter exception, or
`ClassNotFoundException` appears in the new smoke log.

## Predicate Aggregate Smoke Follow-up

Command:

    ./gradlew-agent runModernJavaFeatureSmoke \
      -PtcvmDylib=/Users/flsobral/repos/totalcross-github/build/libtcvm.dylib \
      --no-daemon \
      --console=plain

Exit code: `0`. The complete Gradle/runtime log is
`TotalCrossSDK/agent-logs/20260731-182844-runModernJavaFeatureSmoke-full.log`;
the wrapper summary is
`TotalCrossSDK/build/lambda-return-types-regression/run-aggregate-predicate-validation.log`.

Direct runtime evidence from the complete log:

    [CASE] Java 8 - Predicate default methods
    [PASS] Java 8 - predicate default and
    [PASS] Java 8 - predicate default or
    [PASS] Java 8 - predicate default negate
    [PASS] Java 8 - Predicate default methods

The case directly exercised `Predicate.and`, `Predicate.or`, and
`Predicate.negate`; the dedicated case pass was printed only after all
assertions completed. The full log contains no `[FAIL] Java 8 - Predicate
default methods`, no `NoSuchMethodError` involving
`java.util.function.Predicate`, and no `Aborting program`.
