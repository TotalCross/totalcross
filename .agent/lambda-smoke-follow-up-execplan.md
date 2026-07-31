<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Reproduce Lambda Converter and Runtime Failures

This ExecPlan follows `AGENTS.md` and `.agent/PLANS.md`.

## Purpose / Big Picture

Update the existing lambda smoke coverage so the reported cases are actually reached, reproduce the exact `Tcsort` converter case separately, and collect evidence from converter, deploy, and macOS runtime execution.

This work is diagnostic only. Do not change production code, fix the converter or VM, add speculative variants, or continue searching when a prescribed case does not reproduce. A non-reproduction is a valid result and must be reported.

## Working Set and Resume Protocol

Read:

- `AGENTS.md`
- `.agent/PLANS.md`
- `.agent/reports/lambda-smoke-reproduction-report.md`
- `TotalCrossSDK/src/test/resources/modernjava/smoke/Java8FeatureSmokeTest.java`
- `TotalCrossSDK/src/test/resources/modernjava/smoke/ReportedLambdaAction.java`
- `TotalCrossSDK/src/test/resources/modernjava/smoke/FeatureSmokeApp.java`
- `TotalCrossSDK/build.gradle`

Create:

- converter fixture sources under `TotalCrossSDK/src/test/resources/modernjava/lambda-repro/`
- final report at `.agent/reports/lambda-smoke-follow-up-report.md`
- full logs under `TotalCrossSDK/build/lambda-smoke-follow-up/`

No separate state, evidence, or history file is required.

## Progress

- [x] (2026-07-31) Move the reported runtime cases before the existing `Predicate` smoke.
- [x] (2026-07-31) Add the isolated inherited-method-reference converter fixture.
- [x] (2026-07-31) Run the converter fixture and aggregate runtime smoke on macOS.
- [ ] Capture bytecode and stack-trace evidence.
- [ ] Produce the final factual report.

## Current Architecture and Scope

`Java8FeatureSmokeTest` contains the reported runtime cases, but the previous run reached `testPredicateDefaults()` first and aborted with `NoSuchMethodError`. The exact compilation failure from `Tcsort.jar` has a different shape and must remain isolated because adding it to the aggregate smoke may stop deployment before runtime cases execute:

    map.put("1", this::getGraphics);

The receiver is the smoke subclass, while the implementation handle owner is inherited `totalcross.ui.Control`. The implementation returns `Graphics`, while the public functional interface returns `void`.

Allowed changes are limited to smoke resources, minimal smoke build wiring if required, this ExecPlan, logs, and the final report.

## Plan of Work

### Milestone 1: Make the runtime cases reachable

In `Java8FeatureSmokeTest.initUI()`, invoke all reported runtime cases before `testDefaultAndStaticInterfaceMethods()` and `testPredicateDefaults()`.

Keep the existing reported cases and source shapes:

1. Public and private no-argument method references stored in a map through `ReportedLambdaAction`.
2. A lambda passed directly to `HashMap.put`:

       controlHandlers.put(numericPad, () -> numericPadClick());

3. A private bound method reference passed directly to:

       MainWindow.getMainWindow().runOnMainThread(this::postExecute);

4. Static initialization of `totalcross.io.device.scanner.Scanner`.

Each case must:

- print a unique `[CASE]` line before the operation;
- catch `Throwable` locally;
- print exception class, message, and full stack trace;
- print `[PASS]` or `[FAIL]`;
- allow the next reported case to run.

Do not change or remove the existing `Predicate` smoke. Only move the reported cases before it.

### Milestone 2: Add the exact isolated converter fixture

Under `TotalCrossSDK/src/test/resources/modernjava/lambda-repro/`, add:

- a public `@FunctionalInterface` in its own file with one no-argument `void` method;
- a `MainWindow` subclass containing a `Map<String, ...>`;
- in `initUI()`, the exact operation:

       map.put("1", this::getGraphics);

After storing it, retrieve and invoke the action so the same fixture can provide runtime evidence if deployment succeeds. Print `[CASE]`, `[PASS]`, and local `[FAIL]` output.

Keep this directory outside the aggregate `modernjava/smoke` source set so it cannot block the runtime smoke deployment.

Do not add public/private alternatives, casts, explicit lambdas, anonymous classes, or other receiver types.

### Milestone 3: Run both paths on macOS

From `TotalCrossSDK`, create the evidence directory and capture the environment:

    mkdir -p build/lambda-smoke-follow-up
    {
      git rev-parse HEAD
      sw_vers
      uname -m
      java -version
      ./gradlew-agent --version
    } > build/lambda-smoke-follow-up/environment.log 2>&1

Build the SDK once:

    ./gradlew-agent dist -x test       > build/lambda-smoke-follow-up/dist.log 2>&1

If this fails, record the failure and stop execution without attempting a workaround.

Compile the isolated converter fixture against `dist/totalcross-sdk.jar`, preserving the full command and output in `compile-converter-repro.log`. Deploy its main class with `tc.Deploy` and the macOS target, preserving output in `deploy-converter-repro.log`.

A converter failure is an expected observable result. Record it and continue to the independent aggregate runtime phase.

If the isolated fixture deploys successfully, run its generated macOS executable once and save output in `runtime-converter-repro.log`. Do not rerun with alternate flags or libraries.

Then run the existing aggregate smoke flow:

    ./gradlew-agent deployModernJavaFeatureSmoke       > build/lambda-smoke-follow-up/deploy-aggregate.log 2>&1

If deployment succeeds, execute once:

    build/feature-smoke/classes/install/macos/FeatureSmokeApp       > build/lambda-smoke-follow-up/runtime-aggregate.log 2>&1

Do not use `clean`, change the JDK target, replace `libtcvm.dylib`, alter deploy options, or create another application.

### Milestone 4: Capture bytecode evidence and report

Capture `javap -v -p` output for:

- the isolated converter fixture;
- its public functional interface;
- `smoke.Java8FeatureSmokeTest`;
- `smoke.Java8FeatureSmokeTest$Background`;
- `totalcross.io.device.scanner.Scanner`.

The final report must contain:

- repository revision and macOS/JDK/Gradle environment;
- changed source paths;
- commands and exit codes for build, compilation, deploy, and both runtime paths;
- a result for the isolated `this::getGraphics` case;
- a result for each reported runtime case;
- whether each case was reached before the `Predicate` failure;
- complete relevant exception type, message, and stack trace;
- every observed `$$TC$$Lambda$N` name and where it appeared;
- for each relevant call site, captured receiver descriptor, SAM descriptor, implementation handle kind, owner, method, parameter descriptor, and return descriptor;
- whether `postExecute()` was scheduled and whether it executed;
- whether `Scanner` initialization completed;
- any later `Predicate` failure, clearly marked as pre-existing and non-blocking for the reported cases;
- limitations and a factual conclusion.

Do not propose a fix or new test matrix in the report.

## Surprises & Discoveries

Record only observations that materially change the interpretation of the prescribed cases. Keep raw output in the log directory.

- Observation: The isolated fixture compiled, but `tc.Deploy` failed in the converter while processing inherited `Control.getGraphics`.
  Evidence: `TotalCrossSDK/build/lambda-smoke-follow-up/deploy-converter-repro.log` records `tc.tools.converter.ConverterException: Unsupported invokedynamic in totalcross/ui/Control.getGraphics: instance method reference does not expose receiver Ltotalcross/ui/Control;` with exit code `1`.

- Observation: Reordering made the map, `HashMap.put`, and `runOnMainThread` cases reachable and successful in the aggregate runtime.
  Evidence: `runtime-aggregate.log` contains `[PASS]` for each; it records scheduling returned for `runOnMainThread`, but no callback line before the later failure.

- Observation: Accessing `Scanner.scanManagerVersion` reached a runtime lambda-loading failure before the case could pass.
  Evidence: `runtime-aggregate.log` records `java.lang.ClassNotFoundException: totalcross.io.device.scanner.Scanner$$TC$$Lambda$1` and the local case stack trace.

- Observation: The pre-existing default/static interface failures and `Predicate.and` failure occurred after the reported cases, so they did not prevent those cases from being reached in this run.
  Evidence: In `runtime-aggregate.log`, the reported case lines precede the later `[FAIL] Java 8 smoke failed` lines and the unhandled `NoSuchMethodError`.

## Decision Log

- Decision: Separate the exact `this::getGraphics` fixture from the aggregate smoke.
  Rationale: Its converter failure must not prevent the runtime cases from being deployed and executed.
  Date: 2026-07-31

- Decision: Move all reported runtime cases before the known `Predicate` failure.
  Rationale: The previous run was inconclusive because none of the reported cases was reached.
  Date: 2026-07-31

- Decision: Continue from converter-fixture failure to the independent runtime phase.
  Rationale: Converter and runtime reports concern different execution stages and both need evidence from one bounded run.
  Date: 2026-07-31

- Decision: Reorder only the existing reported runtime calls in `initUI()`.
  Rationale: The cases already preserve the required source shapes and local failure handling; changing anything else would exceed Milestone 1 and could alter the behavior under investigation.
  Date: 2026-07-31

- Decision: Use `lambda.repro.GraphicsReferenceRepro` and its public `GraphicsAction` interface for the isolated fixture.
  Rationale: The fixture remains outside the aggregate `modernjava/smoke` source set while preserving the exact inherited `map.put("1", this::getGraphics)` operation and a direct invocation path.
  Date: 2026-07-31

- Decision: Treat the isolated converter failure as the prescribed result and continue to the aggregate runtime path.
  Rationale: The plan explicitly separates converter evidence from runtime evidence and requires the independent aggregate phase even when the fixture fails during deployment.
  Date: 2026-07-31

## Validation and Acceptance

The plan is complete when:

- the reported runtime cases execute before `testPredicateDefaults()`;
- the isolated fixture preserves `map.put("1", this::getGraphics)` with a public SAM in its own file;
- the prescribed macOS commands are attempted once in order;
- converter and runtime results are preserved independently;
- `.agent/reports/lambda-smoke-follow-up-report.md` contains the required evidence;
- no production fix, alternate reproducer, unrelated test, or exploratory retry is added.

A reproduced exception and a clean result are both acceptable when accurately reported.

## Risks and Open Questions

- The isolated fixture may fail during conversion before an executable exists. Record it and continue to the aggregate runtime phase.
- `runOnMainThread` is asynchronous. Scheduling failure is primary evidence; callback execution is supplemental.
- `Scanner` may fail during static initialization before its lambda body runs. Preserve the first exception and continue through the local case handler.
- The executable may print an abort message but return exit code `0`. Report both independently.

## Idempotence and Recovery

Do not reset unrelated changes or delete caches. Reuse successful build outputs when resuming, but do not rerun a completed phase merely to seek a different result. A partial report may be completed from preserved logs.

## Outcomes & Retrospective

Milestone 1 is complete. The four existing reported runtime cases now execute before `testDefaultAndStaticInterfaceMethods()` and `testPredicateDefaults()`, while the existing tests remain unchanged. The isolated converter fixture, macOS execution, bytecode capture, and final report remain for later milestones.

Milestone 2 is complete. `lambda.repro.GraphicsAction` is a public functional interface in its own file, and `lambda.repro.GraphicsReferenceRepro` contains the exact inherited `map.put("1", this::getGraphics)` operation, retrieves and invokes the stored action, and emits local case result output. No compilation, deployment, or runtime validation was performed.

Milestone 3 is complete. The SDK build and fixture compilation passed. The isolated fixture deploy failed with the reported converter exception in `totalcross/ui/Control.getGraphics`; its runtime was therefore not reached. The aggregate deploy passed, and the aggregate runtime reached the map, `HashMap.put`, and `runOnMainThread` cases successfully. Scanner initialization failed with `ClassNotFoundException` for `totalcross.io.device.scanner.Scanner$$TC$$Lambda$1`, then the existing default/static interface failures and `Predicate.and` `NoSuchMethodError` occurred. The bytecode evidence and final report remain for Milestone 4.

## Revision Note

Initial follow-up plan separating the exact inherited method-reference converter case from the reordered runtime smoke cases.

Milestone 1 update: reordered only the existing reported runtime calls so they can execute before the known `Predicate` failure; no fixture or runtime validation was performed.

Milestone 2 update: added only the isolated converter fixture under `TotalCrossSDK/src/test/resources/modernjava/lambda-repro/`; no aggregate source-set wiring or production code was changed.

Milestone 3 update: captured the prescribed macOS build, converter deployment, aggregate deployment, and aggregate runtime results; stopped before bytecode capture and report generation.
