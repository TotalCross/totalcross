<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Fix Lambda Receiver Adaptation and Adapter Packaging

This ExecPlan follows `AGENTS.md` and `.agent/PLANS.md`.

## Purpose / Big Picture

Fix the two lambda failures reproduced by the follow-up smoke work:

1. deployment rejects a bound reference to an inherited instance method, as in
   `map.put("1", this::getGraphics)`, because the captured receiver is a subclass
   of the method-handle owner rather than the exact same descriptor;
2. deployment rewrites `Scanner` lambda sites to generated
   `Scanner$$TC$$Lambda$N` classes, but those adapter classes are not included in
   the deployed application and runtime initialization fails with
   `ClassNotFoundException`.

The exact converter fixture and the newly added runtime smoke cases must become
passing regression coverage. Completion requires a final report showing that the
focused converter tests and the macOS smoke executions succeeded.

This plan does not redesign general `invokedynamic` support, add arbitrary
bootstrap support, or address unrelated Java API/runtime failures.

## Working Set and Resume Protocol

Read these files before implementation:

- `AGENTS.md`
- `.agent/PLANS.md`
- `.agent/lambda-smoke-follow-up-execplan.md`
- `.agent/reports/lambda-smoke-follow-up-report.md`
- `TotalCrossSDK/src/main/java/tc/tools/converter/Java8LambdaLowering.java`
- `TotalCrossSDK/src/main/java/tc/tools/converter/J2TC.java`
- `TotalCrossSDK/src/main/java/tc/tools/converter/java/JavaClass.java`
- `TotalCrossSDK/src/test/java/tc/tools/converter/modernjava/Java8LambdaLoweringTest.java`
- the existing lambda-related converter integration tests
- `TotalCrossSDK/src/test/resources/modernjava/lambda-repro/`
- `TotalCrossSDK/src/test/resources/modernjava/smoke/Java8FeatureSmokeTest.java`
- `TotalCrossSDK/build.gradle`

Use:

- `.agent/state/lambda-lowering-fixes.md` as the first read when resuming;
- `.agent/evidence/lambda-lowering-fixes.md` for compact command, result, and log
  references;
- `.agent/reports/lambda-lowering-fixes-editorial.md` as the final factual report;
- `TotalCrossSDK/build/lambda-lowering-fixes/` for full logs and generated
  inspection artifacts.

Do not copy full logs into the active plan or report.

## Progress

- [x] (2026-07-31 17:46) Convert both reproduced failures into focused regression tests.
- [x] (2026-07-31 17:46) Accept assignable receivers for inherited instance method references.
- [x] (2026-07-31 17:46) Support discarding a non-void implementation result for a `void` SAM.
- [x] (2026-07-31 17:46) Generate and enqueue synthetic lambda adapters for every converted class.
- [x] (2026-07-31 17:55) Run focused tests, build the SDK, and execute the new smokes on macOS.
- [x] (2026-07-31 17:55) Produce the final report confirming all newly created smoke cases pass.

## Current Architecture and Scope

`Java8LambdaLowering` generates ordinary adapter classes and replaces supported
`LambdaMetafactory` call sites with calls to adapter factory methods.

The inherited-reference fixture captures the concrete subclass:

    Llambda/repro/GraphicsReferenceRepro;

while its implementation handle is:

    REF_invokeVirtual
    totalcross/ui/Control.getGraphics:()Ltotalcross/ui/gfx/Graphics;

The current receiver selection accepts only exact descriptor equality. It must
accept a captured or SAM receiver whose class is assignable to the implementation
owner.

The same fixture also adapts a value-returning implementation to a `void`
functional method:

    Control.getGraphics() -> Graphics
    GraphicsAction.execute() -> void

Java permits the result to be discarded. The generated adapter must remove the
unused result from the operand stack before returning.

`J2TC.addSyntheticLambdaAdapters` is currently tied to class expansion.
Expansion is intentionally skipped for some classes, including TotalCross
classes during ordinary application deployment, and JAR/ZIP input also queues
original entries without using the same expansion path. Adapter generation must
therefore be separated from recursive dependency expansion and applied to every
class that reaches conversion.

Production changes are limited to converter/deployer code needed for these
behaviors. Smoke resources, focused tests, build wiring, plan support files, and
the final report may also change.

## Plan of Work

### Milestone 1: Establish focused regression gates

Extend `Java8LambdaLoweringTest` or add a narrowly scoped companion test with
compiled fixtures that isolate:

1. a bound reference from a subclass to an inherited `void` method, proving
   receiver assignability independently;
2. the exact `this::getGraphics` shape, proving an inherited receiver and
   non-void-to-void return adaptation together.

Before the production fix, record that the exact fixture fails with the current
`receiverSource` diagnostic. After the fix, assert that:

- adapter generation succeeds;
- owner conversion succeeds;
- the generated adapter invokes the inherited implementation method;
- the generated SAM method returns normally with no value left on the stack;
- no `invokedynamic` remains in the adapter.

Add a focused packaging regression test. Compile a fixture containing static
lambda sites, feed it through the same deploy input processing used for classes
and JARs, and assert that the output contains the owner and every expected
`$$TC$$Lambda$N` class exactly once.

Keep the existing exact converter smoke under
`src/test/resources/modernjava/lambda-repro/` unchanged in source meaning:

    map.put("1", this::getGraphics);

Do not replace it with an explicit lambda, cast, anonymous class, or method
declared directly in the subclass.

Acceptance for this milestone is a deterministic test that fails on each
reproduced defect before the corresponding production change and passes after
it.

### Milestone 2: Fix receiver selection for inherited references

Change receiver selection so it receives enough class context to validate type
assignability rather than relying only on descriptor equality.

Implement a focused class-hierarchy resolver that:

- keeps exact descriptor equality as the fast path;
- accepts object receivers whose candidate type is the implementation owner or
  extends/implements it;
- walks superclass and interface names from class-file metadata;
- loads missing hierarchy headers through the converter's existing classpath
  lookup instead of host-JVM reflection;
- caches parsed hierarchy headers for the current conversion run;
- uses a visited set to prevent cycles;
- treats unresolved hierarchy data conservatively and preserves a precise
  unsupported diagnostic containing both candidate and required receiver types.

Update all receiver-dependent paths consistently, including validation and
adapter bytecode generation. Do not weaken validation by accepting every
reference type.

The exact `GraphicsReferenceRepro extends MainWindow` hierarchy must resolve
through `Window`, `Container`, and `Control`, selecting the captured fixture
instance as the receiver of `Control.getGraphics()`.

Acceptance is that both the isolated receiver unit test and the exact converter
fixture pass conversion without changing their source shape.

### Milestone 3: Support non-void implementation results for `void` SAMs

Update return compatibility so a non-void implementation result is accepted when
the instantiated and SAM return types are `void`. A `void` implementation must
remain invalid for a value-returning SAM.

Update adapter generation to discard the implementation result before emitting
the SAM return:

- use `POP` for category-one values, including references;
- use `POP2` for category-two values;
- emit the normal `RETURN` for the `void` SAM;
- preserve existing boxing, unboxing, widening, reference-cast, constructor, and
  value-return behavior.

Keep the logic in the return-adaptation path rather than treating `void` as a
normal value type.

The exact `Control.getGraphics() -> GraphicsAction.execute(): void` fixture must
exercise the category-one path. Add a focused bytecode assertion that the
generated adapter has a balanced operand stack and discards the returned
`Graphics`.

Acceptance is successful adapter conversion and execution of the stored action
in the isolated macOS smoke.

### Milestone 4: Enqueue adapters independently of dependency expansion

Refactor `J2TC` so synthetic lambda adapter discovery is performed for every
class entry immediately before that class is converted.

The preferred structure is:

- call `addSyntheticLambdaAdapters(vin, javaClass)` from the common
  `processFiles` class path before constructing `J2TC` for the owner;
- remove adapter generation from `expandClass`, leaving that method responsible
  only for recursive dependency discovery;
- retain `htAddedClasses` and `htExcludedClasses` as the duplicate guard;
- append adapters to the existing `vin` queue so the current remove-first loop
  processes them in the same deploy operation;
- preserve the original owner and main-class ordering;
- do not recursively generate adapters from generated adapters when they contain
  no lambda sites;
- preserve constant-pool split and retry behavior.

This common point must cover:

- a directly supplied `.class`;
- a class discovered from the application classpath;
- a class whose recursive expansion is prohibited;
- classes supplied through JAR/ZIP input.

Add assertions that repeated discovery cannot produce duplicate adapter entries.

Acceptance is that the aggregate smoke deploy includes all generated `Scanner`
adapters and runtime no longer reports a missing
`Scanner$$TC$$Lambda$N` class.

### Milestone 5: Make the regression smoke repeatable

Keep the newly created cases ahead of unrelated Java 8 checks in
`Java8FeatureSmokeTest`.

Provide repeatable Gradle tasks, or consolidate the existing commands into
documented tasks, for:

1. compiling and deploying the isolated `GraphicsReferenceRepro`;
2. running its generated macOS executable;
3. compiling and deploying `FeatureSmokeApp`;
4. running the generated aggregate macOS executable.

Use a native macOS runtime built from the same repository revision as the SDK.
Do not validate with a stale packaged `libtcvm.dylib`.

From the repository root, the expected validation sequence is equivalent to:

    mkdir -p TotalCrossSDK/build/lambda-lowering-fixes

    cd TotalCrossSDK
    ./gradlew-agent test \
      --tests tc.tools.converter.modernjava.Java8LambdaLoweringTest \
      > build/lambda-lowering-fixes/focused-tests.log 2>&1

    ./gradlew-agent dist -x test \
      > build/lambda-lowering-fixes/dist.log 2>&1

    cd ..
    cmake -S TotalCrossVM -B build -DCMAKE_BUILD_TYPE=Release -G Ninja \
      > TotalCrossSDK/build/lambda-lowering-fixes/cmake.log 2>&1
    ninja -C build tcvm \
      > TotalCrossSDK/build/lambda-lowering-fixes/tcvm.log 2>&1

Run the dedicated converter smoke and aggregate smoke with the generated
`libtcvm.dylib` from that build. Save each compile, deploy, runtime, and exit code
separately.

The exact task names and generated install paths may follow the existing Gradle
conventions, but the final report must record them precisely.

Do not use `clean` unless stale generated output is demonstrated. Do not change
the JDK release, fixture source form, deployment target, or runtime after a
failure merely to obtain a pass.

### Milestone 6: Final validation and report

Run, in order:

1. focused lambda lowering and packaging tests;
2. the relevant modern-Java converter test group;
3. SDK distribution build;
4. isolated `GraphicsReferenceRepro` compile, deploy, and macOS execution;
5. aggregate modern-Java smoke deploy and macOS execution.

The isolated smoke succeeds only when:

- deployment returns zero;
- an executable is produced;
- the action stored by `map.put("1", this::getGraphics)` is invoked;
- the process returns zero;
- no converter exception or `[FAIL]` line is present.

The newly created aggregate lambda cases succeed only when:

- public and private method references in the map pass;
- the lambda passed directly to `HashMap.put` passes;
- `runOnMainThread(this::postExecute)` is created and accepted by the scheduler;
- `Scanner` static initialization completes;
- no `ClassNotFoundException` names a `$$TC$$Lambda$N` class;
- every case emits its expected `[PASS]` line before any unrelated suite output.

Inspect the deployed TCZ contents or deploy evidence and record every generated
adapter for the isolated fixture and `Scanner`.

Create `.agent/reports/lambda-lowering-fixes-editorial.md` with:

- `Editorial Summary`;
- `Original Plan versus Actual Outcome`;
- `What Changed`;
- `Decisions and Trade-offs`;
- `Unexpected Problems and Discoveries`;
- `Validation and Measurable Results`;
- `Smoke Test Success Matrix`;
- `Useful Evidence and Examples`;
- `Limitations, Remaining Work, and Open Questions`;
- `Possible Article Angles`;
- `Suggested Narrative`;
- `Claims Requiring Human Review`.

The success matrix must include the command, platform, exit code, expected
adapter names, and observed result for each newly created smoke case.

The report may state that the work is complete only when all required smoke rows
are successful. If any required row fails, document the blocker factually and
leave this ExecPlan incomplete; do not write a false success conclusion.

## Surprises & Discoveries

- Observation: The reproduced converter call site combines two legal
  adaptations: a subclass receiver for an inherited method and a discarded
  non-void return.
  Evidence: `.agent/reports/lambda-smoke-follow-up-report.md`.

- Observation: Adapter generation coupled to recursive class expansion misses
  classes that are converted but intentionally not expanded.
  Evidence: `Scanner` reaches runtime while
  `Scanner$$TC$$Lambda$1` does not.

- Observation: javac emits `void` as the instantiated return descriptor for the
  inherited method-reference fixture even though the implementation handle
  returns `String`.
  Evidence: The focused test initially reported expected implementation `()V`
  and found implementation `()Ljava/lang/String;`; validation now recognizes
  that the SAM return is the discard boundary and the generated adapter emits
  `POP`.

Add only discoveries that change implementation or validation.

## Decision Log

- Decision: Resolve receiver compatibility from class-file hierarchy metadata,
  not host-JVM reflection.
  Rationale: Deployment must use the application's actual classpath and remain
  deterministic across host environments.
  Date: 2026-07-31

- Decision: Handle value-to-void adaptation explicitly in the return path.
  Rationale: `void` is not a value type, and stack cleanup must be visible in
  generated adapter bytecode.
  Date: 2026-07-31

- Decision: Generate adapters from the common conversion queue rather than
  recursive expansion.
  Rationale: Every converted owner must have its generated adapter classes
  packaged regardless of how the owner was discovered.
  Date: 2026-07-31

- Decision: Require a matching macOS VM for final smoke execution.
  Rationale: A stale runtime previously produced unrelated interface-method
  failures and cannot prove the converter/runtime change.
  Date: 2026-07-31

- Decision: Resolve hierarchy headers through `tc.tools.deployer.Utils` and
  `DeploySettings`, with a per-conversion-run cache and conservative failure.
  Rationale: This reuses the deployer's actual classpath lookup and avoids
  host-JVM reflection or accepting unverifiable reference types.
  Date: 2026-07-31

- Decision: Keep adapter discovery at the common `processFiles` class-entry
  boundary and expose the enqueue helper for focused duplicate-guard testing.
  Rationale: That boundary is shared by direct classes, expanded classes, and
  JAR/ZIP entries, while the small test hook verifies exactly-once queueing.
  Date: 2026-07-31

## Validation and Acceptance

Use focused validation first and stop broadening only after each level passes:

1. focused lowering and packaging tests;
2. modern-Java converter test group;
3. SDK distribution build;
4. isolated macOS smoke;
5. aggregate macOS smoke.

Completion requires:

- inherited receiver conversion passes;
- non-void-to-void adapter execution passes;
- generated adapters are present exactly once;
- `Scanner` initialization passes without a missing lambda class;
- all newly created smoke cases emit `[PASS]`;
- final macOS smoke processes exit successfully;
- the final report contains reproducible command and evidence paths.

No release, tag, push, or publication is part of this plan.

## Risks and Open Questions

- Class hierarchy lookup may encounter unavailable external classes. Preserve a
  clear diagnostic rather than accepting an unverified receiver.
- Interface-owned implementation handles require interface traversal as well as
  superclass traversal.
- Appending adapters during `processFiles` must remain safe when the global
  constant pool causes TCZ splitting and retry.
- Adapter order must not displace the main class from the first TCZ.
- The asynchronous callback body in the `runOnMainThread` case may not execute
  before application shutdown. The required regression criterion is successful
  creation and scheduling, matching the original failure point; callback
  execution may be reported as supplemental evidence.
- Unrelated failures outside the newly created lambda cases must be identified
  separately and must not be misreported as a successful full-suite run.

## Idempotence and Recovery

Preserve unrelated local changes and generated dependency caches.

Tests and builds may be rerun with the same inputs. Before rerunning an expensive
phase, inspect its exit-code file and concise log summary. Do not repeat a
successful phase after documentation-only changes.

If conversion stops during TCZ splitting, retain the existing queue and
constant-pool recovery behavior. Do not delete all build outputs as a first
response.

Update `.agent/state/lambda-lowering-fixes.md` after each logical checkpoint with
the last successful command, active paths, next action, and deferred validation.

## Outcomes & Retrospective

Receiver adaptation, value-to-void return discard, and adapter packaging were
delivered. The focused lowering test and the full relevant modern-Java
converter group passed. The SDK distribution and checkout-built macOS VM passed
their validation. The isolated fixture and aggregate smoke both passed with
exit code 0; Scanner initialization completed and no generated lambda class was
missing. Evidence is recorded in `.agent/evidence/lambda-lowering-fixes.md`,
the factual report is `.agent/reports/lambda-lowering-fixes-editorial.md`, and
the resumable state is `.agent/state/lambda-lowering-fixes.md`.

## Revision Note

Initial correction plan created from the exact converter and runtime failures
recorded by the lambda smoke follow-up report.

Implementation update: added class-file hierarchy resolution with deployer
classpath lookup, explicit `POP`/`POP2` return discard for void SAMs, adapter
discovery at `processFiles`, focused inherited-reference and duplicate-enqueue
tests, and Gradle tasks for isolated and aggregate macOS smoke execution.

Completion update: focused tests, modern-Java converter tests, `dist -x test`,
CMake/Ninja macOS VM build, isolated smoke, aggregate smoke, TCZ inspection,
state, evidence, and editorial report all completed successfully on 2026-07-31.
