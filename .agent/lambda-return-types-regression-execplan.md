<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Exercise Every Lambda Return Kind and Run the Full SDK Suite

This ExecPlan follows `AGENTS.md` and `.agent/PLANS.md`.

## Purpose / Big Picture

Extend the lambda smoke coverage so value-to-`void` method-reference adaptation
is exercised for every Java Virtual Machine return kind, including the
category-two `long` and `double` cases that require `POP2`.

The work must also run the complete `TotalCrossSDK` automated test suite to
detect regressions introduced by the recent receiver-adaptation, return-discard,
and synthetic-adapter packaging fixes.

A developer must be able to observe:

- successful conversion and macOS execution for every return kind;
- `POP` for category-one discarded values;
- `POP2` for discarded `long` and `double` values;
- successful execution of the existing inherited `this::getGraphics` smoke;
- successful execution of the aggregate modern-Java smoke;
- the complete SDK test-suite result and any regression classification;
- a final factual editorial report with return-type and full-suite matrices.

This plan does not broaden support to non-lambda `invokedynamic` bootstraps,
redesign the adapter architecture, publish artifacts, or fix unrelated failures
found by the full suite.

## Working Set and Resume Protocol

Read these files before implementation:

- `AGENTS.md`, for repository safety, validation escalation, and log policy;
- `.agent/PLANS.md`, for ExecPlan structure and completion requirements;
- `.agent/reports/lambda-lowering-fixes-editorial.md`, for the delivered
  receiver, return-discard, and packaging behavior;
- `.agent/evidence/lambda-lowering-fixes.md`, only when exact prior commands or
  artifact paths are needed;
- `TotalCrossSDK/src/main/java/tc/tools/converter/Java8LambdaLowering.java`;
- `TotalCrossSDK/src/test/java/tc/tools/converter/modernjava/Java8LambdaLoweringTest.java`;
- `TotalCrossSDK/src/test/resources/modernjava/lambda-repro/GraphicsReferenceRepro.java`;
- `TotalCrossSDK/src/test/resources/modernjava/lambda-repro/GraphicsAction.java`;
- `TotalCrossSDK/src/test/resources/modernjava/smoke/FeatureSmokeApp.java`;
- `TotalCrossSDK/src/test/resources/modernjava/smoke/Java8FeatureSmokeTest.java`;
- `TotalCrossSDK/build.gradle`.

Use these supporting files:

- `.agent/state/lambda-return-types-regression.md` as the first read when
  resuming;
- `.agent/evidence/lambda-return-types-regression.md` as the append-only compact
  command and result index;
- `.agent/reports/lambda-return-types-regression-editorial.md` as the final
  factual report;
- `TotalCrossSDK/build/lambda-return-types-regression/` for full logs,
  `javap` output, TCZ inventories, and test-result summaries.

Do not duplicate complete logs in the plan, state, evidence, and report.

## Progress

- [x] (2026-07-31 18:04 -03:00) Recorded revision `794ef05effa1c13adaf91639c609e51f4848f5ea`, local-change scope, macOS arm64/OpenJDK 17/Gradle 9.6.1 environment, and the existing smoke task names in `TotalCrossSDK/build/lambda-return-types-regression/environment.log`.
- [x] (2026-07-31 18:09 -03:00) Added a table-driven Java 8 fixture and focused lowering assertions for `void`, all eight category-one primitive/reference forms, and category-two `long`/`double`, including exact `POP`/`POP2`, `RETURN`, no `invokedynamic`, uniqueness, and J2TC conversion checks.
- [x] (2026-07-31 18:12 -03:00) Added `ReturnDiscardAction`, `LambdaReturnDiscardRepro`, and Gradle compile/deploy/run tasks for the eleven-case deployed smoke.
- [x] (2026-07-31 18:16 -03:00) Focused lambda tests and the complete modern-Java converter group passed.
- [x] (2026-07-31 18:17 -03:00) SDK distribution and matching checkout-built macOS VM passed; VM hash was recorded in the evidence and state files.
- [x] (2026-07-31 18:18 -03:00) All-return-kinds, inherited-reference, and aggregate modern-Java smokes passed; TCZ inventories were checked for expected adapters and duplicates.
- [x] (2026-07-31 18:18 -03:00) Complete `TotalCrossSDK` test suite passed with 43 tests, 0 failures, 0 errors, and 0 skipped tests.
- [x] (2026-07-31 18:20 -03:00) Wrote the compact evidence index, state snapshot, and final editorial report; final `git diff --check` is recorded below.

## Current Architecture and Scope

The recent lambda fix explicitly discards a non-`void` implementation result
when the instantiated functional method returns `void`.

The JVM operand stack divides the values relevant to this behavior into:

- no value: `void`;
- category one: `boolean`, `byte`, `char`, `short`, `int`, `float`, object
  references, and array references;
- category two: `long` and `double`.

Category-one values occupy one operand-stack slot and must be discarded with
`POP`. Category-two values occupy two slots and must be discarded with `POP2`.

The previous smoke proved a reference-returning implementation:

    this::getGraphics

adapted to a functional method returning `void`. It did not execute primitive
return variants, and therefore did not exercise `POP2` in a deployed runtime.

The full-suite scope for this plan is the complete Gradle-managed
`TotalCrossSDK` automated test suite on macOS. The native VM is rebuilt and used
for smoke execution, but a cross-platform native build matrix is outside this
plan.

Production code should not change unless the new focused coverage or complete
suite exposes a regression caused by the recent lambda fixes. Unrelated or
pre-existing failures are documented, not repaired.

## Plan of Work

### Milestone 1: Record a reproducible starting point

From the repository root, record:

- `git rev-parse HEAD`;
- `git diff --stat`;
- scoped status for the converter, tests, smoke resources, build file, and
  `.agent` paths;
- macOS version and architecture;
- JDK and Gradle versions;
- the exact path and hash of the checkout-built `libtcvm.dylib` used later.

Save the output under:

    TotalCrossSDK/build/lambda-return-types-regression/environment.log

Inspect the existing Gradle lambda smoke tasks and record their exact names and
dependencies. Reuse them instead of creating duplicate tasks where practical.

Do not run `clean` at this point.

Acceptance:

- the starting revision and local-change scope are known;
- the commands used later can be reproduced;
- unrelated local files remain untouched.

### Milestone 2: Add focused converter coverage for every return kind

Extend `Java8LambdaLoweringTest` with a compiled fixture whose functional
interface has one method:

    void execute();

Provide bound instance methods with these implementation return descriptors:

- `()V` for the baseline `void` case;
- `()Z` for `boolean`;
- `()B` for `byte`;
- `()C` for `char`;
- `()S` for `short`;
- `()I` for `int`;
- `()J` for `long`;
- `()F` for `float`;
- `()D` for `double`;
- `()Ljava/lang/String;` for an object reference;
- `()[I` for an array reference.

Create one method reference per implementation:

    ReturnDiscardAction action = this::returnLong;

The generated adapter for each case must be inspected independently. Assert:

- adapter generation succeeds;
- owner conversion succeeds;
- the adapter contains no `invokedynamic`;
- the functional method calls the expected implementation method;
- `void` emits no discard instruction;
- `boolean`, `byte`, `char`, `short`, `int`, `float`, object, and array returns
  emit `POP`;
- `long` and `double` emit `POP2`;
- every functional method completes with `RETURN`;
- no value-return opcode is emitted by the functional method;
- each generated adapter can be converted by `J2TC`;
- adapter names are unique and generated once.

Prefer one table-driven fixture and shared assertions over eleven nearly
identical test bodies, provided failures still identify the exact descriptor.

Keep the existing inherited-receiver and packaging tests unchanged.

Acceptance:

- the focused test fails if `POP2` is replaced by `POP` or omitted;
- all JVM return kinds listed above pass;
- existing lambda lowering tests continue to pass.

### Milestone 3: Add the deployed return-discard smoke

Add a dedicated smoke fixture under:

    TotalCrossSDK/src/test/resources/modernjava/lambda-repro/

Use clear names such as:

- `ReturnDiscardAction.java`;
- `LambdaReturnDiscardRepro.java`.

The smoke must not replace or weaken `GraphicsReferenceRepro`.

`LambdaReturnDiscardRepro` must:

1. define one instance method for each return descriptor listed in Milestone 2;
2. bind each method to `ReturnDiscardAction.execute(): void`;
3. invoke every action;
4. record a unique side effect inside every implementation method;
5. verify that all implementations executed exactly once;
6. print a unique `[CASE]` line and `[PASS]` or `[FAIL]` for every return kind;
7. catch `Throwable` locally per case so one failure does not hide later cases;
8. print a final summary with total, passed, and failed counts;
9. return a non-successful process or smoke status when any required case fails.

Use a bit mask, counters, or another deterministic mechanism for side effects.
Do not attempt to inspect the discarded value at the functional interface,
because the contract intentionally returns `void`.

Use stable, non-default return values, for example:

- `true`;
- non-zero byte, char, short, and int values;
- a non-zero `long`;
- finite non-zero `float` and `double` values;
- a non-null `String`;
- a non-null array.

The return values demonstrate that a real value is produced before being
discarded; the side effects demonstrate that the implementation body executed.

Add or extend Gradle tasks to compile, deploy, and run this fixture using the
same conventions as:

- `runLambdaLoweringFixesRepro`;
- `runModernJavaFeatureSmoke`.

The run task must accept `-PtcvmDylib=<path>` and use the VM built from the same
checkout.

Acceptance:

- deployment produces the expected `$$TC$$Lambda$N` adapters;
- TCZ inspection finds every expected adapter exactly once;
- macOS runtime prints eleven per-case `[PASS]` lines;
- the smoke summary reports zero failures;
- no verifier error, stack error, converter exception, or
  `ClassNotFoundException` is present.

### Milestone 4: Preserve and rerun the existing regression smokes

Build the SDK and native VM from the same revision:

    cd TotalCrossSDK
    ./gradlew-agent dist -x test --no-daemon --console=plain \
      > build/lambda-return-types-regression/dist.log 2>&1

    cd ..
    cmake -S TotalCrossVM -B build -DCMAKE_BUILD_TYPE=Release -G Ninja \
      > TotalCrossSDK/build/lambda-return-types-regression/cmake.log 2>&1

    ninja -C build tcvm \
      > TotalCrossSDK/build/lambda-return-types-regression/tcvm.log 2>&1

Then run, with the freshly built `build/libtcvm.dylib`:

1. the new all-return-kinds smoke;
2. `runLambdaLoweringFixesRepro`;
3. `runModernJavaFeatureSmoke`.

Save one concise summary, one full log reference, and one exit-code record per
task.

Inspect the TCZ contents and record:

- the adapter names for the new return-discard fixture;
- `GraphicsReferenceRepro$$TC$$Lambda$0`;
- all three `Scanner$$TC$$Lambda$N` adapters;
- whether any adapter name appears more than once.

Acceptance:

- all three smoke applications exit successfully;
- all newly required return-kind lines pass;
- the inherited `this::getGraphics` case still passes;
- the aggregate map, direct `HashMap.put`, scheduler, callback, and Scanner
  cases still pass;
- no generated adapter is missing or duplicated.

### Milestone 5: Run the complete SDK automated test suite

Run the complete `TotalCrossSDK` test task once after focused coverage passes:

    cd TotalCrossSDK
    ./gradlew-agent test --no-daemon --console=plain \
      > build/lambda-return-types-regression/full-sdk-tests.log 2>&1

Record:

- command and exit code;
- total, passed, failed, skipped, and disabled test counts when available;
- failing test class and method names;
- the smallest relevant error excerpt;
- Gradle XML/HTML result paths;
- duration;
- revision and JDK.

Do not repeat the complete suite merely because a report or comment changes.

If the suite fails:

1. rerun each failing test or smallest failing class individually;
2. determine whether it exercises the changed lambda lowering, adapter
   packaging, class hierarchy lookup, or smoke build wiring;
3. inspect test reports before modifying code;
4. fix the failure within this plan only when evidence shows that the recent
   lambda changes caused a regression;
5. add a focused regression test before changing production code;
6. rerun the focused test, affected test class, complete SDK suite, and required
   smokes after an attributable fix.

When attribution remains uncertain, use a separate temporary Git worktree at the
known pre-fix revision and run only the failing test with the same JDK and
environment. Do not reset or rewrite the active worktree.

If no unambiguous pre-fix revision is available, report the failure as
unattributed rather than claiming it is new or pre-existing.

Unrelated failures are documented with evidence and remain out of scope.

Acceptance:

- the complete SDK suite exits zero; or
- every non-zero result is reproducible, individually classified, and proven
  unrelated or pre-existing with comparative evidence.

The preferred completion state is a zero-exit full suite.

### Milestone 6: Final validation and editorial report

Run final static validation:

    git diff --check

Run no expensive command again after documentation-only changes.

Create:

    .agent/reports/lambda-return-types-regression-editorial.md

The report must contain the standard completion sections required by
`.agent/PLANS.md`:

- `Editorial Summary`;
- `Original Plan versus Actual Outcome`;
- `What Changed`;
- `Decisions and Trade-offs`;
- `Unexpected Problems and Discoveries`;
- `Validation and Measurable Results`;
- `Useful Evidence and Examples`;
- `Limitations, Remaining Work, and Open Questions`;
- `Possible Article Angles`;
- `Suggested Narrative`;
- `Claims Requiring Human Review`.

Also include two explicit matrices.

#### Return-Type Smoke Matrix

For every return kind, record:

- Java type;
- JVM descriptor;
- stack category;
- expected discard instruction;
- generated adapter;
- deploy exit code;
- runtime result;
- observed side effect;
- relevant log or TCZ evidence path.

The matrix must contain:

    void, boolean, byte, char, short, int, long, float, double, object, array

#### Full-Suite Regression Matrix

Record:

- suite or failing test;
- command;
- exit code;
- passed/failed/skipped counts;
- first observed failure;
- isolated reproduction result;
- attribution: caused by this work, pre-existing, unrelated, or unattributed;
- corrective action or explicit out-of-scope decision;
- final status.

The report may claim success only when:

- every return-kind smoke row passes;
- focused lambda tests pass;
- existing isolated and aggregate smokes pass;
- adapter inventories are complete and duplicate-free;
- the complete SDK suite passes, or every remaining failure has defensible
  comparative evidence showing that it is not a regression from this work.

Do not hide failures behind an overall Gradle exit code or omit failing tests
from the matrix.

## Surprises & Discoveries

- Observation: The previous runtime smoke exercised category-one reference
  discard but did not execute category-two return discard.
  Evidence: `.agent/reports/lambda-lowering-fixes-editorial.md`.

- Observation: The previous focused tests asserted the inherited receiver and
  `POP`, while `POP2` was implemented by type size without an exact deployed
  smoke.
  Evidence: `.agent/reports/lambda-lowering-fixes-editorial.md`.

- Observation: TCZ files are TotalCross archives rather than ordinary ZIP files,
  so standard `unzip` inventory failed and the existing `totalcross.util.zip.TCZ`
  reader was used for final adapter inspection.
  Evidence: `TotalCrossSDK/build/lambda-return-types-regression/all-tcz-inventory.log`.

Add only discoveries that alter the implementation, test shape, or regression
assessment.

## Decision Log

- Decision: Cover JVM return descriptors rather than selecting representative
  Java source types.
  Rationale: The converter operates on descriptors and operand-stack category;
  explicit descriptor coverage makes omissions visible.
  Date: 2026-07-31

- Decision: Treat object and array returns as separate smoke rows.
  Rationale: Both are category-one references, but they have distinct JVM
  descriptors and class-file forms.
  Date: 2026-07-31

- Decision: Keep the new return-discard smoke separate from the inherited
  `GraphicsReferenceRepro`.
  Rationale: The original reproducer remains stable while the expanded matrix
  can fail and report each return kind independently.
  Date: 2026-07-31

- Decision: Define the requested complete suite as the complete
  `TotalCrossSDK` Gradle test suite on macOS.
  Rationale: The changes are in the SDK converter/deployer; cross-platform
  native and packaging matrices require environments beyond this bounded task.
  Date: 2026-07-31

- Decision: Fix only regressions attributable to the recent lambda work.
  Rationale: A complete suite may expose unrelated historical failures, which
  must not expand this plan into general repository repair.
  Date: 2026-07-31

- Decision: Keep the new smoke in the existing `lambda-repro` source directory
  but give it a separate Gradle output directory and task family.
  Rationale: This reuses the established fixture conventions while keeping the
  new TCZ, executable, and logs isolated from the inherited-reference smoke.
  Date: 2026-07-31

## Validation and Acceptance

Use the repository validation escalation order:

1. static and fixture compilation checks;
2. focused `Java8LambdaLoweringTest`;
3. modern-Java converter test group;
4. new return-discard smoke;
5. existing lambda and aggregate smokes;
6. complete `TotalCrossSDK` test suite;
7. distribution and matching native runtime build when smoke artifacts require
   them.

Required concise outcomes:

- focused lambda tests: exit `0`;
- all-return-kinds smoke: eleven passed, zero failed;
- inherited-reference smoke: exit `0` and expected `[PASS]`;
- aggregate modern-Java smoke: exit `0` and all expected lambda `[PASS]` lines;
- complete SDK suite: preferably exit `0`, otherwise fully classified as
  described in Milestone 5;
- `git diff --check`: exit `0`.

Save verbose output to the plan log directory and record compact evidence only.

## Risks and Open Questions

- A Java source compiler may share adapter implementations or number generated
  call sites differently than expected. Assert semantic presence and uniqueness,
  not fragile ordinal assumptions unless the ordinal itself is the contract.
- `boolean`, `byte`, `char`, and `short` may use integer stack operations despite
  distinct descriptors. Keep separate source and descriptor coverage while
  expecting category-one discard.
- The full suite may rely on external services, platform tools, credentials, or
  environment-specific assumptions. Distinguish an environmental failure from a
  code regression and preserve exact evidence.
- A failing full-suite test may predate the lambda fixes. Comparative execution
  must use a separate worktree and the same environment.
- New smoke actions must remain reachable before unrelated failures and must
  catch failures locally.
- Adapter inventory checks must tolerate stable numbering gaps caused by other
  call sites while rejecting missing or duplicate expected classes.

## Idempotence and Recovery

Do not use destructive Git commands or reset unrelated local changes.

The state file must record:

- active milestone and slice;
- last successful command;
- current revision;
- modified paths;
- focused validations completed;
- full-suite status;
- failing tests awaiting classification;
- matching VM path and hash;
- next concrete command.

Successful expensive phases may be reused when source inputs and the matching VM
hash are unchanged. Do not rerun the complete suite after report-only edits.

A temporary baseline worktree may be removed after evidence is saved. Never use
it to mutate the active branch.

Generated logs and TCZ inventories remain under the build evidence directory and
are not committed unless explicitly requested.

## Outcomes & Retrospective

All eleven requested return descriptors are covered. Focused conversion tests
prove `POP` for category-one values and `POP2` for `long` and `double`, while
the deployed macOS smoke reports eleven passes and zero failures with one
recorded invocation per implementation. The inherited `getGraphics` smoke and
aggregate modern-Java smoke pass, and their expected adapter inventories are
present without duplicates. The complete SDK suite passes 43/43 with no
regressions to classify. No production converter change was needed for this
regression plan; the work is test, smoke, and build-task coverage. Evidence is
indexed in `.agent/evidence/lambda-return-types-regression.md`, state is in
`.agent/state/lambda-return-types-regression.md`, and the factual editorial
report is `.agent/reports/lambda-return-types-regression-editorial.md`.

## Revision Note

Initial plan created to close the remaining category-two return-discard coverage
gap and perform the complete SDK regression run explicitly deferred by the
previous lambda-lowering correction plan.

Execution note (2026-07-31): completed the planned fixture, deployed smoke,
matching macOS VM validation, full SDK suite, evidence index, and editorial
report. The plan was updated with actual commands, outcomes, and the TCZ
inspection discovery.
