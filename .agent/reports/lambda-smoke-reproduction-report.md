<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Lambda Smoke Reproduction Report

## Run environment

The prescribed run used repository revision `53f890101d4914206d149b4ad71e49968c8c6e5c`.
The environment capture reported macOS 26.5.2, build 25F84, on `arm64`, with
OpenJDK 17.0.12 (Azul Zulu) and Gradle 9.6.1.

The requested state file `.agent/state/lambda-smoke-reproduction-execplan.md`
was not present. The ExecPlan states that no separate state file is required.

## Changed source paths

- `TotalCrossSDK/src/test/resources/modernjava/smoke/Java8FeatureSmokeTest.java`
- `TotalCrossSDK/src/test/resources/modernjava/smoke/ReportedLambdaAction.java`
- `.agent/lambda-smoke-reproduction-execplan.md`
- `.agent/reports/lambda-smoke-reproduction-report.md`

No production source was changed.

## Procedure results

| Phase | Exit code | Result |
| --- | ---: | --- |
| Environment capture | 0 | Completed |
| SDK `./gradlew-agent dist -x test` | 0 | Completed after the environment correction |
| `deployModernJavaFeatureSmoke` (compile and deploy) | 0 | Completed; macOS executable generated |
| Generated macOS runtime | 0 | Process exited, but the aggregate smoke aborted before the reported cases |
| `javap` for `Java8FeatureSmokeTest` | 0 | Captured |
| `javap` for `Scanner` | 0 | Captured |
| `javap` for `Java8FeatureSmokeTest$Background` | 0 | Captured as supplemental evidence |

Full command output is preserved under
`TotalCrossSDK/build/lambda-smoke-reproduction/`. The Gradle agent logs are
under `TotalCrossSDK/agent-logs/`.

## Reported case results

| Case | Status | Evidence |
| --- | --- | --- |
| Map method references to public and private methods | Not reached | The runtime stopped in the existing `testPredicateDefaults` case before the new `[CASE]` lines. |
| Lambda passed directly to `HashMap.put` | Not reached | Same runtime interruption. |
| Bound private method reference passed to `runOnMainThread` | Not reached | No scheduling or callback line was emitted. |
| `Scanner` static lambda initialization | Not reached | The static member access was not executed by the aggregate runtime. |

The runtime did not emit any `[CASE]` line for the four reported cases. It also
did not emit the scheduling or callback messages, so `postExecute()` was
neither observed scheduled nor observed executing.

## Runtime exception

Before the reported cases, the runtime emitted failures for the existing
default and static interface method checks, then an unhandled
`java.lang.NoSuchMethodError`. The relevant complete stack trace was:

    java.lang.NoSuchMethodError:
     java.util.function.Predicate and(java.util.function.Predicate,). The current VM may not be compatible with this program OR there may be a bug in the Java compiler; try to upgrade or downgrade your JDK.

    Stack trace:
    smoke.Java8FeatureSmokeTest.testPredicateDefaults 134
    smoke.Java8FeatureSmokeTest.initUI 47
    totalcross.ui.Control.setRect 1385
    totalcross.ui.Container.add 229
    smoke.FeatureSmokeApp.initUI 17
    totalcross.ui.MainWindow.startProgram 533
    totalcross.ui.MainWindow._onTimerTick 573

    Aborting program.

The same unhandled exception and stack trace appeared twice in
`runtime.log`. Despite the abort text, the executable returned exit code 0.

## Class-file evidence

The complete raw dumps are in:

- `TotalCrossSDK/build/lambda-smoke-reproduction/javap-java8-smoke.log`
- `TotalCrossSDK/build/lambda-smoke-reproduction/javap-scanner.log`
- `TotalCrossSDK/build/lambda-smoke-reproduction/javap-background.log`

For the two map method references, `Java8FeatureSmokeTest` contains
`invokedynamic` call sites with descriptor
`execute:(Lsmoke/Java8FeatureSmokeTest;)Lsmoke/ReportedLambdaAction;`.
Both use `java/lang/invoke/LambdaMetafactory.metafactory` with a `()V`
functional method type. The public case has implementation handle
`REF_invokeVirtual smoke/Java8FeatureSmokeTest.reportedPublicMethod:()V`;
the private case has
`REF_invokeVirtual smoke/Java8FeatureSmokeTest.reportedPrivateMethod:()V`.

For the direct `HashMap.put` lambda, the call site has descriptor
`run:(Lsmoke/Java8FeatureSmokeTest;)Ljava/lang/Runnable;` and uses
`LambdaMetafactory.metafactory` with implementation handle
`REF_invokeVirtual smoke/Java8FeatureSmokeTest.lambda$testReportedHashMapPutLambda$19:()V`.
That synthetic implementation method invokes the private
`numericPadClick:()V` method.

For `Background.exec()`, the supplemental dump contains an `invokedynamic`
call site with descriptor
`run:(Lsmoke/Java8FeatureSmokeTest$Background;)Ljava/lang/Runnable;`.
It uses `LambdaMetafactory.metafactory` with implementation handle
`REF_invokeVirtual smoke/Java8FeatureSmokeTest$Background.postExecute:()V`
and functional and instantiated method type `()V`. The resulting Runnable is
passed to `MainWindow.runOnMainThread:(Ljava/lang/Runnable;)V`.

For `Scanner`, the static initializer contains two relevant `invokedynamic`
sites assigning `scannerLoader` and `doLoad`. Both use
`LambdaMetafactory.metafactory` with `()V` method types. Their implementation
handles are respectively:

- `REF_invokeStatic totalcross/io/device/scanner/Scanner.lambda$static$0:()V`
- `REF_invokeStatic totalcross/io/device/scanner/Scanner.lambda$static$2:()V`

The nested `doLoad` implementation contains a third site whose implementation
handle is
`REF_invokeStatic totalcross/io/device/scanner/Scanner.lambda$static$1:()V`.

No `$$TC$$Lambda$N` class name appeared in the runtime output, Gradle logs, or
the three `javap` outputs. The dumps show synthetic `lambda$...` methods and
`invokedynamic` sites, but no observed generated class with that naming form.

## Limitations and conclusion

The first SDK build attempt, before the environment correction, failed in
`:jar` because of the duplicate `TCFont.tcz` entry. The prescribed build was
then rerun after the correction and succeeded; deploy and runtime were each
run once afterward. No alternate source form, compiler target, deploy option,
runtime library, launch method, or isolated application was used.

Under the exact prescribed procedure, none of the four reported failures was
reproduced. This result is limited to the fact that the aggregate runtime
stopped in an existing Java 8 smoke case before any of the four reported cases
could execute.
