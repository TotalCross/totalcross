<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Lambda Smoke Follow-up Report

## Environment

The prescribed run used repository revision `ebc57e1b8f61a23b284675c0623779d2d1173a98`.
The environment capture reported macOS 26.5.2, build 25F84, on `arm64`, with
OpenJDK 17.0.12 (Azul Zulu) and Gradle 9.6.1.

## Changed source paths

- `TotalCrossSDK/src/test/resources/modernjava/smoke/Java8FeatureSmokeTest.java`
- `TotalCrossSDK/src/test/resources/modernjava/lambda-repro/GraphicsAction.java`
- `TotalCrossSDK/src/test/resources/modernjava/lambda-repro/GraphicsReferenceRepro.java`
- `.agent/lambda-smoke-follow-up-execplan.md`
- `.agent/reports/lambda-smoke-follow-up-report.md`

No production source was changed.

## Phase results

| Phase | Exit code | Result |
| --- | ---: | --- |
| Environment capture | 0 | Completed |
| SDK `./gradlew-agent dist -x test` | 0 | Completed |
| Isolated fixture compilation | 0 | Completed |
| Isolated fixture deploy with `tc.Deploy` | 1 | Converter failure; runtime not reached |
| Isolated fixture runtime | — | Not attempted because deploy failed |
| Aggregate `deployModernJavaFeatureSmoke` | 0 | Completed |
| Aggregate macOS runtime | 0 | Completed with reported failures in output |
| All five `javap -v -p` captures | 0 | Completed |

Full logs and exit-code files are under
`TotalCrossSDK/build/lambda-smoke-follow-up/`. The converter failure is in
`deploy-converter-repro.log`; the aggregate runtime output is in
`runtime-aggregate.log`.

The compilation and isolated deploy commands were each invoked once without
log redirection and then once again with the identical command solely to
preserve the required full logs. No source, flag, target, library, or launch
method changed between those invocations.

## Isolated converter fixture

The fixture compiled and produced the exact call site from
`map.put("1", this::getGraphics)`. Deployment failed before an executable was
created. The converter reported:

    tc.tools.converter.ConverterException: Unsupported invokedynamic in totalcross/ui/Control.getGraphics: instance method reference does not expose receiver Ltotalcross/ui/Control;
        at tc.tools.converter.Java8LambdaLowering.receiverSource(Java8LambdaLowering.java:676)
        at tc.tools.converter.Java8LambdaLowering.expectedImplementationArguments(Java8LambdaLowering.java:342)
        at tc.tools.converter.Java8LambdaLowering.validateImplementationDescriptor(Java8LambdaLowering.java:310)
        at tc.tools.converter.Java8LambdaLowering.validateSupportedLambdaMetafactory(Java8LambdaLowering.java:82)
        at tc.tools.converter.Java8LambdaLowering.generateAdapterClasses(Java8LambdaLowering.java:64)
        at tc.tools.converter.J2TC.addSyntheticLambdaAdapters(J2TC.java:1093)
        at tc.tools.converter.J2TC.expandClass(J2TC.java:1062)
        at tc.tools.converter.J2TC.addAndExpand(J2TC.java:1050)
        at tc.tools.converter.J2TC.process(J2TC.java:1270)
        at tc.Deploy.<init>(Deploy.java:81)
        at tc.Deploy.main(Deploy.java:43)

The same exception was printed in the fatal-error block and as the uncaught
main-thread exception. The isolated fixture runtime was not reached.

## Aggregate runtime results

| Case | Result | Reached before `Predicate` failure? |
| --- | --- | --- |
| Public/private method references in a map | `[PASS]` | Yes |
| Lambda passed directly to `HashMap.put` | `[PASS]` | Yes |
| Private bound method reference passed to `runOnMainThread` | `[PASS]` for scheduling | Yes |
| `Scanner` static lambda initialization | `[FAIL]` with `ClassNotFoundException` | Yes |

The `runOnMainThread` case printed `[INFO] Java 8 - runOnMainThread scheduling returned`.
No `[CALLBACK]` line was observed, so `postExecute()` was scheduled
successfully but its execution was not observed before the later failure.

The Scanner case printed:

    java.lang.ClassNotFoundException: totalcross.io.device.scanner.Scanner$$TC$$Lambda$1

    Warning! java.lang.ClassNotFoundException: totalcross.io.device.scanner.Scanner$$TC$$Lambda$1
    totalcross.io.device.scanner.Scanner.<S>
    smoke.Java8FeatureSmokeTest.testReportedScannerInitialization 218
    smoke.Java8FeatureSmokeTest.initUI 49
    totalcross.ui.Control.setRect 1385
    totalcross.ui.Container.add 229
    smoke.FeatureSmokeApp.initUI 17
    totalcross.ui.MainWindow.startProgram 533
    totalcross.ui.MainWindow._onTimerTick 573

Scanner initialization therefore did not complete in the deployed runtime.

After the four reported cases, the existing default and static interface
checks printed their pre-existing failures. The later unhandled exception was:

    java.lang.NoSuchMethodError:
     java.util.function.Predicate and(java.util.function.Predicate,). The current VM may not be compatible with this program OR there may be a bug in the Java compiler; try to upgrade or downgrade your JDK.

    Stack trace:
    smoke.Java8FeatureSmokeTest.testPredicateDefaults 134
    smoke.Java8FeatureSmokeTest.initUI 51
    totalcross.ui.Control.setRect 1385
    totalcross.ui.Container.add 229
    smoke.FeatureSmokeApp.initUI 17
    totalcross.ui.MainWindow.startProgram 533
    totalcross.ui.MainWindow._onTimerTick 573

The runtime printed the abort sequence twice but returned exit code 0. This
later failure did not block the reported cases in this follow-up run.

## Bytecode evidence

The captures are:

- `javap-converter-fixture.log`
- `javap-converter-interface.log`
- `javap-java8-smoke.log`
- `javap-background.log`
- `javap-scanner.log`

The public `lambda.repro.GraphicsAction` interface is annotated with
`@FunctionalInterface` and declares `execute:()V`.

The isolated fixture has an `invokedynamic` call site with receiver/captured
argument descriptor
`execute:(Llambda/repro/GraphicsReferenceRepro;)Llambda/repro/GraphicsAction;`.
Its SAM descriptor is `()V`. The `LambdaMetafactory.metafactory` implementation
handle is `REF_invokeVirtual`, owner `totalcross/ui/Control`, method
`getGraphics`, parameter descriptor `()`, and return descriptor
`Ltotalcross/ui/gfx/Graphics;`. The call-site receiver type is the fixture
subclass, while the implementation handle owner is the inherited `Control`.

The public and private map references have receiver descriptor
`execute:(Lsmoke/Java8FeatureSmokeTest;)Lsmoke/ReportedLambdaAction;` and SAM
descriptor `()V`. Their `LambdaMetafactory.metafactory` handles are both
`REF_invokeVirtual` with owner `smoke/Java8FeatureSmokeTest`, methods
`reportedPublicMethod` and `reportedPrivateMethod`, parameter descriptor `()`,
and return descriptor `V`.

The direct `HashMap.put` lambda has receiver descriptor
`run:(Lsmoke/Java8FeatureSmokeTest;)Ljava/lang/Runnable;` and SAM descriptor
`()V`. Its implementation handle is `REF_invokeVirtual`, owner
`smoke/Java8FeatureSmokeTest`, method
`lambda$testReportedHashMapPutLambda$19`, parameter descriptor `()`, and return
descriptor `V`. That synthetic method invokes the private `numericPadClick:()V`
method.

The `Background` call site has receiver descriptor
`run:(Lsmoke/Java8FeatureSmokeTest$Background;)Ljava/lang/Runnable;` and SAM
descriptor `()V`. Its `LambdaMetafactory.metafactory` handle is
`REF_invokeVirtual`, owner `smoke/Java8FeatureSmokeTest$Background`, method
`postExecute`, parameter descriptor `()`, and return descriptor `V`. The
resulting Runnable is passed to
`MainWindow.runOnMainThread:(Ljava/lang/Runnable;)V`.

The Scanner class initializer reads the non-constant
`scanManagerVersion:Ljava/lang/String;` field and contains two
`invokedynamic` sites producing `Runnable` instances. Their SAM descriptor is
`()V`; their static implementation handles are:

- `REF_invokeStatic`, owner `totalcross/io/device/scanner/Scanner`, method
  `lambda$static$0`, parameter descriptor `()`, return descriptor `V`;
- `REF_invokeStatic`, owner `totalcross/io/device/scanner/Scanner`, method
  `lambda$static$2`, parameter descriptor `()`, return descriptor `V`.

The nested static-lambda body contains a third `Runnable` site with
`REF_invokeStatic`, owner `totalcross/io/device/scanner/Scanner`, method
`lambda$static$1`, parameter descriptor `()`, and return descriptor `V`.

The only observed generated lambda class name matching `$$TC$$Lambda$N` was
`totalcross.io.device.scanner.Scanner$$TC$$Lambda$1`, in the aggregate runtime
failure. No other such generated class name appeared in the captured logs or
`javap` output.

## Limitations and conclusion

The isolated converter executable could not be produced, so its runtime case
was not observed. The aggregate runtime returned code 0 while printing both
the Scanner failure and the later pre-existing `Predicate.and` failure. The
`runOnMainThread` callback timing was not observed beyond the scheduling-returned
line.

Under the exact prescribed follow-up procedure, the inherited
`this::getGraphics` converter failure was reproduced. The public/private map
references, direct `HashMap.put` lambda, and `runOnMainThread` scheduling cases
passed in the aggregate runtime. Scanner static lambda initialization failed
with the observed missing generated lambda class. The later Predicate failure
remained present but no longer prevented the reported runtime cases from being
reached.
