<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Reproduce Reported Lambda Failures

This ExecPlan follows `AGENTS.md` and `.agent/PLANS.md`.

## Purpose / Big Picture

Extend the existing modern-Java lambda smoke coverage with the exact source shapes from issue #434 and the three runtime reports, then compile, deploy, and execute the smoke app on macOS. The only deliverable is reproducible evidence: command results, stack traces, class-file details, and a concise report for later analysis.

This plan does not diagnose or fix the converter, deployer, SDK, or VM. Reproduction is not required for completion. If the specified run does not reproduce a reported failure, record that result and stop without trying alternate source forms, compiler targets, deploy options, isolated applications, or implementation changes.

## Working Set and Resume Protocol

Read these files first:

- `AGENTS.md` for repository safety and validation policy.
- `.agent/PLANS.md` for ExecPlan requirements.
- `TotalCrossSDK/src/test/resources/modernjava/smoke/Java8FeatureSmokeTest.java` for current lambda smoke coverage.
- `TotalCrossSDK/src/test/resources/modernjava/smoke/FeatureSmokeTest.java` for smoke output conventions.
- `TotalCrossSDK/src/test/resources/modernjava/smoke/FeatureSmokeApp.java` for aggregate execution.
- `TotalCrossSDK/build.gradle` for `compileModernJavaFeatureSmoke` and `deployModernJavaFeatureSmoke`.
- `TotalCrossSDK/src/main/java/totalcross/io/device/scanner/Scanner.java` only to preserve the reported SDK initialization shape.

Create the final report at:

- `.agent/reports/lambda-smoke-reproduction-report.md`

Store full command output under:

- `TotalCrossSDK/build/lambda-smoke-reproduction/`

No state, history, or separate evidence file is needed for this bounded investigation. Resume from this ExecPlan, the modified smoke sources, and the report draft.

## Progress

- [x] (2026-07-31) Add the four reported source shapes to the existing Java 8 smoke suite.
- [x] (2026-07-31) Build and deploy the aggregate smoke app once with the macOS target (SDK build retry exit 0; deploy exit 0).
- [x] (2026-07-31) Execute the generated macOS application once and capture its complete output (runtime exit 0; the existing Java 8 smoke aborted before the reported cases).
- [x] (2026-07-31) Record class-file metadata and produce the final reproduction report.

## Current Architecture and Scope

`Java8FeatureSmokeTest` already covers ordinary lambdas and static, bound, and unbound method references. `compileModernJavaFeatureSmoke` compiles all Java files in `src/test/resources/modernjava/smoke`, while `deployModernJavaFeatureSmoke` deploys `smoke/FeatureSmokeApp.class` using the host platform and selects `-macos` on macOS.

Keep all production code out of scope. The allowed source changes are limited to the smoke resources, this ExecPlan, generated logs, and the final report.

## Plan of Work

### Milestone 1: Add the reported smoke cases

Add a public functional interface in its own source file:

- `TotalCrossSDK/src/test/resources/modernjava/smoke/ReportedLambdaAction.java`

The interface must be in package `smoke`, be annotated with `@FunctionalInterface`, and expose one no-argument `void execute()` method.

Extend `Java8FeatureSmokeTest.java` with exactly these cases and invoke them from `initUI()` before `finish()`:

1. **Issue #434 method references in a map.** Create `Map<String, ReportedLambdaAction>` and execute separate cases for:
   - `map.put("public", this::reportedPublicMethod)`;
   - `map.put("private", this::reportedPrivateMethod)`.

   The two target methods must have identical no-argument `void` signatures and differ only in visibility. Execute the stored actions so both construction and invocation are covered.

2. **Lambda expression passed directly to `HashMap.put`.** Preserve the reported shape:

       controlHandlers.put(numericPad, () -> numericPadClick());

   Use a local `HashMap`, a stable local key named `numericPad`, and a private no-argument `numericPadClick()` method. Retrieve and execute the stored `Runnable`.

3. **Bound private method reference passed to `runOnMainThread`.** Add a non-static inner `Background` class with an `exec()` method containing:

       MainWindow.getMainWindow().runOnMainThread(this::postExecute);

   Keep `postExecute()` private. Invoke `exec()` during the smoke test. Log whether scheduling returned; also log from `postExecute()` if the callback runs before application exit, but do not make callback timing an acceptance requirement.

4. **SDK `Scanner` static lambda initialization.** Access a non-constant static member of `totalcross.io.device.scanner.Scanner` so its static initializer runs. Do not activate hardware, open scanner UI, or call a native scanner operation.

Each case must print a unique `[CASE]` line before execution and a `[PASS]` or `[FAIL]` line afterward. Catch `Throwable` separately inside each reported case, print its class, message, and full stack trace, then continue to the next case. Do not introduce a generic lambda-based test wrapper, because that would add unrelated lambda sites to the class under investigation.

Do not add JUnit tests, alternate fixtures, extra syntax variants, or production instrumentation.

### Milestone 2: Run the fixed smoke procedure on macOS

From `TotalCrossSDK`, create the log directory and capture the environment:

    mkdir -p build/lambda-smoke-reproduction
    {
      git rev-parse HEAD
      sw_vers
      uname -m
      java -version
      ./gradlew-agent --version
    } > build/lambda-smoke-reproduction/environment.log 2>&1

Build the SDK without tests:

    ./gradlew-agent dist -x test \
      > build/lambda-smoke-reproduction/dist.log 2>&1

Record the exit code. If this command fails, do not change build settings or investigate unrelated failures; continue directly to the report with deploy and runtime marked as not reached.

If the SDK build succeeds, compile and deploy the aggregate smoke app:

    ./gradlew-agent deployModernJavaFeatureSmoke \
      > build/lambda-smoke-reproduction/deploy.log 2>&1

Record the exit code. If compilation or deploy fails, preserve the full error and stack trace, do not alter the smoke source to bypass it, and continue directly to the report with runtime marked as not reached.

If deploy succeeds, run the generated macOS executable once:

    build/feature-smoke/classes/install/macos/FeatureSmokeApp \
      > build/lambda-smoke-reproduction/runtime.log 2>&1

Record the exit code. Do not rerun with alternate runtime libraries, copied dylibs, flags, or launch methods.

### Milestone 3: Capture class-file evidence and report

If smoke compilation produced class files, capture the relevant bytecode metadata without changing the test:

    javap -classpath build/feature-smoke/classes -v -p smoke.Java8FeatureSmokeTest \
      > build/lambda-smoke-reproduction/javap-java8-smoke.log 2>&1

    javap -classpath dist/totalcross-sdk.jar -v -p totalcross.io.device.scanner.Scanner \
      > build/lambda-smoke-reproduction/javap-scanner.log 2>&1

The report should summarize, without dumping the entire files:

- repository revision, macOS version, architecture, JDK, and Gradle version;
- exact source paths changed;
- exit code and status for SDK build, smoke compilation/deploy, and macOS runtime;
- one result row for each reported case: reached, passed, failed, or not reached;
- complete relevant exception type, message, and stack trace;
- every observed `$$TC$$Lambda$N` class name and where it appeared;
- the corresponding `invokedynamic` bootstrap, implementation handle kind, owner, method name, and descriptor from `javap` when available;
- whether `postExecute()` was merely scheduled or was also observed executing;
- limitations, including any phase not reached;
- a factual conclusion stating either which failures were reproduced or that they were not reproduced under this exact procedure.

Do not include a proposed fix, speculative root cause, new test plan, or recommendation to retry with different inputs.

## Surprises & Discoveries

Record only observations that change the factual interpretation of the run. Keep raw output in the log files and place only concise evidence in the report.

- Observation: The requested `.agent/state/lambda-smoke-reproduction-execplan.md` file is absent from the checkout. The ExecPlan itself states that this investigation has no separate state file, so Milestone 1 was treated as active from the unchecked progress list.
  Evidence: `find .agent -maxdepth 3 -type f` listed no matching state file; all four progress items were unchecked before this milestone.

- Observation: The prescribed SDK build failed before smoke compilation or deployment.
  Evidence: `TotalCrossSDK/build/lambda-smoke-reproduction/dist.exitcode` contains `1`; the Gradle agent summary reports `:jar` failed because `TCFont.tcz` is a duplicate with no duplicate handling strategy. Full output is preserved in `TotalCrossSDK/agent-logs/20260731-161339-dist-full.log` and the compact summary in `TotalCrossSDK/agent-logs/20260731-161339-dist-agent.log`.

- Observation: After the environment duplicate was removed, the prescribed SDK build and macOS deploy succeeded.
  Evidence: `TotalCrossSDK/build/lambda-smoke-reproduction/dist.exitcode` and `deploy.exitcode` contain `0`; the generated executable is `TotalCrossSDK/build/feature-smoke/classes/install/macos/FeatureSmokeApp`.

- Observation: The generated macOS application exited with code 0 but aborted before the four reported cases ran because the existing Java 8 smoke failed in `testPredicateDefaults` with `java.lang.NoSuchMethodError: java.util.function.Predicate and(java.util.function.Predicate,)`.
  Evidence: `TotalCrossSDK/build/lambda-smoke-reproduction/runtime.exitcode` contains `0`; `runtime.log` also records failures for the existing default and static interface method checks and the unhandled `NoSuchMethodError` at `smoke.Java8FeatureSmokeTest.testPredicateDefaults 134`.

- Observation: Class-file inspection found the requested method-reference and lambda bootstrap metadata, but no generated class name matching `$$TC$$Lambda$N`.
  Evidence: The outer smoke class, `Java8FeatureSmokeTest$Background`, and `Scanner` dumps all completed with exit code `0`; searches across those dumps and the captured runtime/build logs found no `$$TC$$Lambda$` occurrence.

## Decision Log

- Decision: Reuse the aggregate Java 8 feature smoke instead of creating a separate application.
  Rationale: The existing Gradle tasks already compile, deploy, and run the relevant lambda path, and this keeps the investigation bounded.
  Date: 2026-07-31

- Decision: Treat a non-reproduction as a valid result.
  Rationale: The objective is evidence collection, not exploratory debugging or correction.
  Date: 2026-07-31

- Decision: Make no production-code changes.
  Rationale: Any fix or instrumentation would alter the behavior being observed and exceed the requested scope.
  Date: 2026-07-31

- Decision: Stop after the smoke-source milestone and do not run compilation, deployment, runtime, or bytecode inspection.
  Rationale: Those commands belong to later milestones, and the requested execution boundary is the active milestone only.
  Date: 2026-07-31

- Decision: Stop the active macOS procedure after the SDK build failure without changing build settings or retrying.
  Rationale: The ExecPlan explicitly requires deploy to be skipped when `dist -x test` fails, and the user requested stopping at the active milestone or a real blocker.
  Date: 2026-07-31

- Decision: Resume the failed SDK build after the environment correction and continue through the prescribed deploy and single runtime execution.
  Rationale: The build failure was not reproduced after the duplicate generated `TCFont.tcz` was removed, and the ExecPlan requires the next phase when the preceding command succeeds.
  Date: 2026-07-31

- Decision: Stop after Milestone 2 without running class-file inspection or generating the report.
  Rationale: Those activities belong to Milestone 3, while the requested execution boundary is the active milestone only.
  Date: 2026-07-31

- Decision: Inspect `Java8FeatureSmokeTest$Background` in an additional `javap` capture.
  Rationale: The prescribed outer-class dump does not contain the inner class method body, while the report requires the bootstrap and implementation handle for `this::postExecute`.
  Date: 2026-07-31

- Decision: Mark the four reported cases as not reached rather than inferred failures or passes.
  Rationale: The aggregate runtime aborted in the pre-existing `testPredicateDefaults` case before emitting any new `[CASE]` line.
  Date: 2026-07-31

## Validation and Acceptance

Acceptance requires all of the following:

- the smoke sources contain exactly the four reported case groups described above;
- the public functional interface is declared in its own file;
- the prescribed build, deploy, and macOS runtime commands are attempted in order when the preceding phase succeeds;
- full logs and exit codes are preserved;
- `.agent/reports/lambda-smoke-reproduction-report.md` clearly distinguishes reproduced failures, successful cases, and phases not reached;
- no JUnit tests, alternative reproductions, production fixes, or unrelated changes are added.

A reproduced error is evidence, not a validation failure. A clean run is also acceptable when accurately reported.

## Risks and Open Questions

- The original issue may fail during deploy before runtime cases can be reached. Record this and stop; do not split the smoke into alternate applications.
- The `runOnMainThread` callback may not execute before `FeatureSmokeApp` exits. The required observation is whether construction and scheduling throw; callback execution is supplemental evidence only.
- `Scanner` may already be initialized before its case runs. Record the observed behavior without changing test order or forcing class loading through another mechanism.

## Idempotence and Recovery

The source edits and report are safe to resume. Do not run `clean`, delete caches, replace runtime libraries, or reset unrelated local changes. A partially written report may be updated from the preserved logs. Do not repeat successful commands solely to seek a different outcome.

## Outcomes & Retrospective

Milestone 1 is complete. `Java8FeatureSmokeTest.java` now contains the four reported source shapes: public/private method references stored in a map, a lambda passed directly to `HashMap.put`, a bound private callback passed to `runOnMainThread`, and access to `Scanner.scanManagerVersion` for static initialization. The public `ReportedLambdaAction` interface is in its own source file. No build, deploy, runtime, or bytecode evidence was collected because those are later milestones and were intentionally not executed.

The remaining milestones must be resumed separately to produce the prescribed macOS evidence and final report. Do not add diagnosis or remediation work.

Milestone 2 is complete. The environment capture was reused, the corrected SDK build passed with exit code 0, the macOS deploy passed with exit code 0, and the generated executable was run once with exit code 0. The runtime output shows that the existing Java 8 smoke aborted at `testPredicateDefaults` before the four reported cases, so those cases are not reached in this aggregate application.

Milestone 3 is complete. The prescribed `javap` captures and a supplemental inner-class capture passed, and `.agent/reports/lambda-smoke-reproduction-report.md` summarizes the environment, phase results, not-reached cases, exception stack trace, lambda bootstrap metadata, and the absence of observed `$$TC$$Lambda$N` names. No reported failure was reproduced; the cases were not reached under the exact procedure. No diagnosis or remediation was added.

## Revision Note

Initial bounded plan for reproducing and documenting the reported lambda compilation and macOS runtime failures without attempting a fix.
