<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Modern Java 17 Class File Support

This ExecPlan is a living document. The sections `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective` must be kept up to date as work proceeds.

This document follows `.agent/PLANS.md`. Keep it self-contained when revising it: a future implementer should be able to start from this file and the current working tree alone.

## Purpose / Big Picture

The goal is to let TotalCross deploy and run applications compiled by Java toolchains through Java 17, class-file major 61, with the largest practical compatibility gain for the smallest implementation effort. The work started from a Java 7 class-file baseline where Retrolambda lowered Java 8 language features before TotalCross saw them. The branch now moves the deployer toward direct Java 8, Java 11, and Java 17 class-file support.

The plan focuses on common javac output and practical application code. `invokedynamic` support is implemented where it unlocks mainstream features such as lambdas, method references, string concatenation, and records. Full dynamic call-site machinery remains useful only when a supported Java 17 feature cannot be lowered safely.

## Version Roadmap

This plan stops at Java 17. Preview class files, identified by minor version 65535, are intentionally rejected with a clear diagnostic.

| Priority | Java release / feature area | Class file major | Goal |
| ---: | --- | ---: | --- |
| Baseline | Java 7 and earlier | 51 and earlier | Preserve existing SDK/runtime behavior while documenting older language feature coverage. |
| 1 | Java 8 | 52 | Preserve direct Java 8 class-file deploy, lambda lowering, method references, default/static interface methods, and Retrolambda-free SDK build behavior. |
| 2 | Java 9 and Java 10 | 53-54 | Support common class-file and source output such as string concat, private interface methods, diamond anonymous classes, effectively-final try-with-resources, and local variable type inference. |
| 3 | Java 11 LTS | 55 | Complete class-file acceptance for module metadata, nestmates, dynamic constants diagnostics, lambda `var` parameters, and common Java 11 javac output. |
| 4 | Java 12 through Java 17 | 56-61 | Complete non-preview class-file acceptance and support high-value Java 14-17 features: switch expressions, text blocks, records, instanceof pattern matching, and sealed classes. |

## Java 17 Feature Status

This table tracks language and class-file features through Java 17. "Supported" means the deployer can convert representative javac output. "Partial" means the common case works but documented gaps remain.

| Java | Feature | Status | Working cases | Gaps / next work |
| --- | --- | --- | --- | --- |
| 1.0-1.3 | Classes, interfaces, inheritance, overloading, primitives, arrays, packages, exceptions, `synchronized`, and ordinary control flow | Supported | Existing SDK code and all smoke tests rely on these constructs. | No new work planned in this ExecPlan. |
| 1.1 | Inner, local, and anonymous classes | Supported | Anonymous classes are covered by Java 7 diamond anonymous-class smoke coverage; nested access is further covered by Java 11 nestmate tests. | No dedicated runtime semantic gap known. |
| 1.4 | `assert` | Supported | `Java4FeatureSmokeTest` compiles and deploys an assert statement. | Runtime behavior depends on normal Java assertion enablement; no special deployer work planned. |
| 5 | Enums | Supported | `Java5FeatureSmokeTest` covers enum constants and enum switch. `Enum4D` exists in the compatibility runtime. | Broader `EnumSet`/`EnumMap` behavior is runtime API compatibility and should be added only when real apps require it. |
| 5 | Generics | Supported | `Java5FeatureSmokeTest` covers generic classes; Java 8 lambda tests cover erased generic SAM adaptation. | Generic signatures are metadata; reflection over generic signatures is not a deployer milestone. |
| 5 | Enhanced for, autoboxing/unboxing, varargs, covariant returns | Supported | `Java5FeatureSmokeTest` covers each source feature through deployable bytecode. | No known deployer gap. |
| 5 | Annotations | Partial | Annotation metadata compiles and deploys in `Java5FeatureSmokeTest`. | Runtime reflection over annotations is not validated by the smoke app. |
| 6 | `@Override` on interface methods | Supported | `Java6FeatureSmokeTest` covers compilation and deploy of the source feature. | No bytecode-specific work expected. |
| 7 | Diamond operator, string switch, try-with-resources, multi-catch, binary literals, numeric separators | Supported | `Java7FeatureSmokeTest` covers each source feature. Try-with-resources deploys with current `Throwable.addSuppressed` support. | Broader Java 7 library APIs remain demand-driven runtime compatibility work. |
| 8 | Lambdas and method references | Supported for common javac output | `Java8LambdaLoweringTest`, `RetrolambdaRemovalTest`, and `Java8FeatureSmokeTest` cover stateless/captured lambdas, static/bound/unbound method references, constructor references, marker and bridge `altMetafactory`, reference casts, reference-covariant returns, and common primitive boxing/unboxing/widening. | Rare descriptor adaptations, arbitrary bootstrap methods, and serializable lambda deserialization remain unsupported. |
| 8 | Default and static interface methods | Supported | `Java8FeatureSmokeTest` covers default and static interface methods. | Java 8 helper APIs such as `Predicate.and` are runtime API gaps, not parser gaps. |
| 8 | Type annotations and repeatable annotations | Partial | `Java8FeatureSmokeTest` includes type-use and repeatable annotation metadata. | Runtime reflection over those annotations is not validated. |
| 9 | `StringConcatFactory` string concatenation | Supported for common recipes | `JavaStringConcatLowering` lowers `makeConcat` and common `makeConcatWithConstants` recipes to `StringBuffer`; Java 9, Java 11, and Java 17 smokes exercise string concat. | Exotic concat constants should keep precise diagnostics if found. |
| 9 | Private interface methods and private static interface methods | Supported | `Java9FeatureSmokeTest` covers both. | No known deployer gap. |
| 9 | Diamond anonymous classes, effectively-final try-with-resources, private `@SafeVarargs` | Supported | `Java9FeatureSmokeTest` covers each source feature. | No known deployer gap. |
| 9 | Module metadata | Partial | `Java11ClassFileTest` parses generated `module-info.class`, `Module`, `ModulePackages`, and `ModuleMainClass`. | TotalCross does not implement the Java module runtime; module descriptors are metadata for deployer acceptance. |
| 10 | Local variable type inference | Supported | `Java10FeatureSmokeTest` covers `var` local variables. | No bytecode-specific work expected. |
| 11 | Nestmates | Supported for deployer acceptance | `Java11ClassFileTest` verifies `NestHost`/`NestMembers`; `Java11FeatureSmokeTest` covers nested private access. | No runtime nestmate enforcement beyond deployable javac output is planned. |
| 11 | Lambda `var` parameters | Supported | `Java11FeatureSmokeTest` covers lambda `var` parameters. | No bytecode-specific work expected. |
| 11 | `CONSTANT_Dynamic` | Partial | Constant-pool tag 17 is parsed; `ldc` of dynamic constants fails with a precise unsupported-feature diagnostic. | Add deterministic lowering only if a common Java 17-compatible source fixture requires it. |
| 12-13 | Switch expressions as preview | Unsupported by policy | Preview class files are rejected by minor version 65535. | Keep preview rejection clear. |
| 14 | Switch expressions | Supported | `Java14FeatureSmokeTest` covers final switch expressions. | No known deployer gap. |
| 15 | Text blocks | Supported | `Java15FeatureSmokeTest` covers text blocks as ordinary string constants. | No known deployer gap. |
| 16 | Records | Partial | `Java16FeatureSmokeTest`, `Java17ClassFileTest`, and `Java17FeatureTest` cover record metadata, construction, accessors, and custom methods. `Record4D` supplies the `java/lang/Record` superclass mapping, and `JavaObjectMethodsLowering` handles javac-generated `ObjectMethods` sites enough for deploy. | Exact generated record `equals`, `hashCode`, and `toString` semantics currently fall back to `Object` semantics and must be completed. |
| 16 | Instanceof pattern matching | Supported | `Java16FeatureSmokeTest` covers pattern variables from `instanceof`, which javac lowers to supported bytecode. | No known deployer gap. |
| 17 | Sealed classes and interfaces | Partial | `JavaClass` reads `PermittedSubclasses`; `Java17FeatureSmokeTest` deploys a sealed interface plus permitted implementation. | Runtime semantic enforcement is not implemented; decide whether metadata-only support is enough for TotalCross. |
| Through 17 | Preview features | Unsupported by policy | Class-file minor version 65535 is rejected. | Keep diagnostics naming class, major, minor, and preview status. |

## Progress

- [x] (2026-07-01) Created the initial modern class-file ExecPlan and inspected the current `invokedynamic` parser/converter commits.
- [x] (2026-07-01) Added modern class-file parser tests and version gates, then accepted normal class files through the implemented target while rejecting preview files clearly.
- [x] (2026-07-02) Implemented Java 8 `LambdaMetafactory` lowering for common javac lambdas, method references, constructor references, `altMetafactory` marker/bridge cases, and descriptor adaptation.
- [x] (2026-07-02) Removed Retrolambda from the SDK build after proving direct Java 8 class files and focused modern-Java tests pass.
- [x] (2026-07-02) Added Java 8 smoke coverage and fixed deployer class expansion for modern constant-pool entries.
- [x] (2026-07-02) Implemented Java 9+ `StringConcatFactory` lowering for common javac string concat output.
- [x] (2026-07-02) Compiled the SDK as Java 9 class files and added a Java 9 smoke app.
- [x] (2026-07-03) Completed Java 11 class-file support for module metadata, nestmates, `CONSTANT_Dynamic` parsing, and precise unsupported-feature diagnostics.
- [x] (2026-07-04) Compiled the SDK as Java 11 class files and added a Java 11 smoke app.
- [x] (2026-07-04) Completed Java 17 class-file metadata acceptance for record components, permitted subclasses, and ordinary non-preview Java 17 output.
- [x] (2026-07-04) Compiled the SDK as Java 17 class files and added Java 17 smoke coverage for deployable Java 17-era compiler output.
- [x] (2026-07-04) Added initial record deploy support through `Record4D` and `ObjectMethods` lowering.
- [x] (2026-07-04) Reorganized smoke tests into per-version `Container` classes plus a single `FeatureSmokeApp` `MainWindow`, with stdout pass/fail logging and UI labels.
- [x] (2026-07-04) Added smoke coverage for earlier language features that were missing from the smoke suite: Java 1.4 assertions, Java 5 enums/generics/varargs/autoboxing/enhanced-for/annotations/covariant returns, Java 6 interface `@Override`, Java 7 source features, Java 10 `var`, Java 14 switch expressions, Java 15 text blocks, and Java 16 records/instanceof patterns.
- [ ] Complete exact generated record `equals`, `hashCode`, and `toString` semantics using record component metadata instead of `Object` semantics.
- [ ] Decide and document whether sealed classes remain metadata-only or need runtime enforcement in TotalCross.
- [ ] Add a repeatable Gradle or script target for compiling and deploying `FeatureSmokeApp` from the generated SDK.
- [ ] Review Java 8 runtime API gaps found by smoke validation, especially `Predicate.and` and serializable lambda deserialization, and either implement or document precise unsupported diagnostics.
- [ ] Review Java 9-11 runtime/API gaps found by fixtures, especially module runtime behavior and deterministic `CONSTANT_Dynamic` lowering.
- [ ] Add focused tests for annotation metadata retention/reflection only if TotalCross apps need annotation reflection through Java 17.
- [ ] Run broad SDK validation and the aggregate smoke deploy after each remaining feature fix.

## Surprises & Discoveries

- Observation: `CONSTANT_InvokeDynamic_info` does not contain a class index. Its first field is a `bootstrap_method_attr_index`, and its second field is a `name_and_type_index`.
  Evidence: the original parser treated the bootstrap index like a class name, which made `MethodCall` unsuitable for opcode 186.

- Observation: `BootstrapMethods` is a class-level attribute, not a constant-pool entry.
  Evidence: lambda, string concat, and record `ObjectMethods` support all depend on parsing class-level bootstrap metadata before converting methods.

- Observation: TCVM already has only two method-call opcodes, `CALL_normal` and `CALL_virtual`.
  Evidence: existing TCVM dispatch resolves calls through the TotalCross global constant pool, so the high-value path is deployer lowering into ordinary calls.

- Observation: Java 8 helper APIs and serializable lambda deserialization are runtime compatibility gaps rather than parser/lowering blockers.
  Evidence: ordinary lambdas and method references deploy after lowering, while `Predicate.and` and `SerializedLambda` require additional runtime API support.

- Observation: Java 9+ string concatenation is emitted for ordinary string `+` source code.
  Evidence: Java 9, Java 11, and Java 17 fixtures contain `StringConcatFactory` sites and now deploy after lowering to `StringBuffer`.

- Observation: Java 11 metadata support must preserve diagnostics instead of masking errors.
  Evidence: `CONSTANT_Dynamic` loaded by `ldc` now fails with a precise unsupported-feature message instead of a later null dereference.

- Observation: javac emits `ObjectMethods` invokedynamic sites in record classes even when the app only uses record construction, accessors, and custom methods.
  Evidence: `javap` on the Java 16/17 record smoke class reports `toString`, `hashCode`, and `equals` dynamic sites backed by `java/lang/runtime/ObjectMethods.bootstrap`.

- Observation: The smoke app shape matters.
  Evidence: per-version smokes are now `Container` classes so `FeatureSmokeApp` can add every suite from one `MainWindow` and exercise the full feature surface in a single deployable application.

## Decision Log

- Decision: Limit this ExecPlan to Java 17 class files and features.
  Rationale: The current milestone should finish the LTS target already in progress before expanding the roadmap.
  Date/Author: 2026-07-04 / Codex

- Decision: Keep preview class files unsupported.
  Rationale: Preview features are explicitly unstable and marked by class-file minor version 65535; clear rejection is safer than accidental support.
  Date/Author: 2026-07-01 / Codex

- Decision: Implement `invokedynamic` in layers: parse metadata first, lower common bootstraps second, and add runtime dynamic behavior only if a supported Java 17 feature cannot be lowered.
  Rationale: Lowering unlocks normal javac output while keeping TCVM changes small.
  Date/Author: 2026-07-01 / Codex

- Decision: Treat `LambdaMetafactory`, `StringConcatFactory`, and record `ObjectMethods` as the high-value bootstraps through Java 17.
  Rationale: These are emitted by javac for mainstream source features.
  Date/Author: 2026-07-04 / Codex

- Decision: For the first records slice, lower javac `ObjectMethods` sites to existing `java/lang/Object` virtual calls.
  Rationale: This unblocks deploy of record construction, accessors, and custom methods. Exact record object-method semantics remain visible application behavior and are tracked as follow-up work.
  Date/Author: 2026-07-04 / Codex

- Decision: Use per-version smoke `Container` classes plus a single aggregate `FeatureSmokeApp`.
  Rationale: The source layout documents when each feature was introduced, while the aggregate app gives one deploy target that schedules every smoke suite.
  Date/Author: 2026-07-04 / Codex

## Outcomes & Retrospective

2026-07-02 / Codex: The planned Java 8 `LambdaMetafactory` lowering milestone is complete for the common javac shapes targeted by this ExecPlan. Generated adapters support stateless and captured lambdas, static/bound/unbound method references, constructor references, `altMetafactory` markers and direct bridges, reference casts, reference-covariant returns, and common primitive boxing/unboxing/widening through wrapper methods.

2026-07-02 / Codex: The full SDK distribution build passes without the Retrolambda Gradle plugin. `TotalCrossSDK/build.gradle` compiles source and target compatibility according to the current milestone, and the focused `tc.tools.converter.modernjava.*` suite validates direct modern class-file deploy behavior.

2026-07-03 / Codex: Java 11 class-file support accepts and exposes the main metadata forms introduced by Java 9-11. `JavaClass` reads module and nestmate metadata, `CONSTANT_Dynamic` loads fail with precise diagnostics, and Gradle 9 test execution includes the required JUnit Platform launcher runtime dependency.

2026-07-04 / Codex: Java 17 class-file acceptance is in place. `JavaClass` reads record component metadata and permitted subclasses. `Java17ClassFileTest` proves javac `--release 17` output is major 61, verifies record and sealed metadata, and converts ordinary Java 17 output through `J2TC`.

2026-07-04 / Codex: Initial Java 17 record deploy support is in place. `Record4D` gives record classes a deployable `java/lang/Record` compatibility superclass, and `JavaObjectMethodsLowering` recognizes javac `java/lang/runtime/ObjectMethods.bootstrap` sites enough for record construction, accessors, and custom methods to convert. Exact record `equals`, `hashCode`, and `toString` semantics remain pending.

2026-07-04 / Codex: The smoke suite is reorganized around `FeatureSmokeApp`. Each version-specific smoke class extends `Container`, logs pass/fail results to stdout, and displays a label in the UI. The aggregate app extends `MainWindow` and adds every suite from `initUI` so one deploy target exercises Java 1.4 through Java 17 feature coverage.

## Context and Orientation

The deployer parses Java class files under `TotalCrossSDK/src/main/java/tc/tools/converter/java`. `JavaClass` owns class metadata, `JavaConstantPool` owns raw constants, `JavaMethod` owns method declarations, and `JavaCode` turns method bytecode bytes into `ByteCode` objects. `Bytecode2TCCode` converts those bytecode objects into TotalCross IR instructions. `GlobalConstantPool` serializes method, field, class, and literal references into the TCZ constant pool. TCVM reads that pool in `TotalCrossVM/src/tcvm/tcclass.c` and executes method calls in `TotalCrossVM/src/tcvm/tcvm.c`.

`invokedynamic` is Java bytecode opcode 186. For TotalCross through Java 17, the practical implementation is deployer lowering: recognize common bootstrap methods, generate ordinary bytecode/classes or ordinary TotalCross IR calls, and let TCVM execute existing call instructions.

Modern class files through Java 17 add constant-pool tags and attributes that older parsers must understand enough to skip or translate safely. Java 9 introduced module metadata and `CONSTANT_Module`/`CONSTANT_Package` tags. Java 11 introduced `CONSTANT_Dynamic` and nestmate metadata. Java 16/17-era class files include records and sealed-class metadata.

## Plan of Work

Keep the class-file parser strict and explicit. It accepts normal class files through Java 17 major 61, rejects preview minor 65535 clearly, and rejects unsupported known features before they mutate global conversion state. Unknown attributes should be skipped using their declared lengths.

Finish Java 17 feature support by first completing record object-method semantics. Use parsed record component metadata and the bootstrap arguments from `ObjectMethods` to generate the same visible behavior javac expects for `equals`, `hashCode`, and `toString`. Keep the current construction/accessor/custom-method support as the baseline that must not regress.

Review sealed classes after records. If metadata-only behavior is acceptable for TotalCross, document that in tests and diagnostics. If runtime enforcement is required, add the smallest deploy/runtime check that preserves normal permitted subclass behavior.

Turn `FeatureSmokeApp` into the standard smoke validation target. The generated SDK should compile the smoke sources with `javac --release 17`, then deploy `smoke/FeatureSmokeApp.class` with `tc.Deploy`. The deploy should include all per-version containers and prove no class expansion, constant-pool parsing, or lowering path regresses.

After the aggregate smoke target is stable, address the remaining runtime/API gaps through Java 17 by priority: Java 8 functional helper defaults used by real apps, serializable lambda deserialization if needed, deterministic `CONSTANT_Dynamic` lowering when a common source fixture requires it, annotation reflection if apps need it, and sealed runtime enforcement if metadata-only behavior is insufficient.

## Concrete Steps

Work from the repository root unless a command specifies another directory.

At the end of every implementation step, stage only the files changed for that step and generate a descriptive commit message that follows the repository commit-message rules. Do this after validation, and leave unrelated dirty or untracked files untouched.

1. Run focused converter tests:

       cd TotalCrossSDK
       ./gradlew test --tests tc.tools.converter.modernjava.*

2. Build the SDK distribution:

       cd TotalCrossSDK
       ./gradlew clean dist -x test

3. Compile and deploy the aggregate feature smoke app:

       cd TotalCrossSDK
       mkdir -p /tmp/totalcross-feature-smoke/classes
       javac --release 17 -cp dist/totalcross-sdk.jar -d /tmp/totalcross-feature-smoke/classes src/test/resources/modernjava/smoke/*.java
       javap -verbose /tmp/totalcross-feature-smoke/classes/smoke/FeatureSmokeApp.class
       cd /tmp/totalcross-feature-smoke/classes
       java -Djava.awt.headless=true -cp "/Users/flsobral/repos/totalcross-github/TotalCrossSDK/dist/totalcross-sdk.jar:/Users/flsobral/repos/totalcross-github/TotalCrossSDK/dist/libs/*" tc.Deploy smoke/FeatureSmokeApp.class /v

   Expect the aggregate app to compile as Java 17 class files and deploy into `FeatureSmokeApp.tcz`. The deploy should include every per-version smoke container. Running the app should print `[PASS]` lines to stdout and display one label per suite.

4. Complete record object-method semantics:

       cd TotalCrossSDK
       ./gradlew test --tests tc.tools.converter.modernjava.Java17FeatureTest

   Extend the test so generated record `equals`, `hashCode`, and `toString` use record component semantics, not `Object` fallback behavior.

5. Decide sealed-class runtime behavior:

       cd TotalCrossSDK
       ./gradlew test --tests tc.tools.converter.modernjava.Java17ClassFileTest

   Either add runtime/deploy enforcement tests or explicitly document metadata-only sealed support as the accepted TotalCross behavior.

6. Close remaining through-Java-17 runtime/API gaps:

       cd TotalCrossSDK
       ./gradlew test --tests tc.tools.converter.modernjava.*

   Add focused fixtures only when a real language feature or smoke app exposes the gap.

## Validation and Acceptance

Class-file acceptance is proven by tests that compile or generate class files through Java 17 and then parse or convert them with the TotalCross deployer. A successful milestone must either convert the class or reject it with a message that identifies a specific unsupported feature.

Each completed step is ready to hand off only when its relevant files are staged and a descriptive commit message has been produced. The staged set must be limited to that step's changes and must not include generated artifacts, local logs, downloaded dependencies, or unrelated worktree files.

The aggregate smoke suite is accepted when `FeatureSmokeApp` compiles with `javac --release 17`, deploys through `tc.Deploy`, includes every per-version smoke container, prints pass/fail results to stdout, and displays UI labels for successful suites.

Java 8 acceptance is that high-value language/class-file features deploy without Retrolambda. The generated TCZ must contain ordinary synthetic adapter classes, and converted methods must use ordinary TotalCross calls. Java 8 runtime helper APIs and serializable lambda deserialization are separate compatibility follow-ups unless SDK code requires them.

Java 11 acceptance includes successful parsing of class-file major 55, module metadata, nestmate metadata, and lowering of javac string concatenation via `StringConcatFactory`. Dynamic constants may remain unsupported only when diagnostics are precise and actionable.

Java 17 acceptance includes successful parsing of class-file major 61, metadata for records and sealed classes, and deployable non-preview Java 17 compiler output. Records are complete only when construction, accessors, custom methods, and generated `equals`, `hashCode`, and `toString` semantics all behave correctly.

## Idempotence and Recovery

All generated fixtures must write to JUnit temporary directories or `/tmp` smoke directories. Do not commit generated `.class`, `.jar`, `.tcz`, Gradle `build/`, native build, or dependency-cache artifacts.

Parser changes must be safe to rerun. Unknown attributes should be skipped using their declared lengths, and unsupported known features should throw converter errors before mutating global conversion state. If conversion of a generated class fails, the error should name the generated class and original call site when applicable.

If class-file parsing becomes blocked by a rare feature through Java 17, keep the milestone moving by rejecting that feature clearly and documenting it in `Outcomes & Retrospective`. The priority is broad safe acceptance, not pretending that every Java runtime API exists on device.

## Artifacts and Notes

Important current code locations:

       TotalCrossSDK/src/main/java/tc/tools/converter/java/JavaConstantPool.java
       TotalCrossSDK/src/main/java/tc/tools/converter/java/JavaClass.java
       TotalCrossSDK/src/main/java/tc/tools/converter/java/JavaMethod.java
       TotalCrossSDK/src/main/java/tc/tools/converter/java/JavaCode.java
       TotalCrossSDK/src/main/java/tc/tools/converter/bytecode/BC186_invokedynamic.java
       TotalCrossSDK/src/main/java/tc/tools/converter/Bytecode2TCCode.java
       TotalCrossSDK/src/main/java/tc/tools/converter/Java8LambdaLowering.java
       TotalCrossSDK/src/main/java/tc/tools/converter/JavaStringConcatLowering.java
       TotalCrossSDK/src/main/java/tc/tools/converter/JavaObjectMethodsLowering.java
       TotalCrossSDK/src/main/java/tc/tools/converter/GlobalConstantPool.java
       TotalCrossSDK/src/main/java/tc/tools/converter/J2TC.java
       TotalCrossSDK/src/main/java/jdkcompat/lang/Record4D.java
       TotalCrossSDK/src/test/resources/modernjava/smoke/FeatureSmokeApp.java
       TotalCrossSDK/src/test/resources/modernjava/smoke/FeatureSmokeTest.java
       TotalCrossSDK/build.gradle
       TotalCrossVM/src/tcvm/tcclass.c
       TotalCrossVM/src/tcvm/tcvm.c

## Interfaces and Dependencies

Compatibility classes should follow the existing `jdkcompat` convention and use `4D` suffixes. Expected additions for this ExecPlan include `jdkcompat.lang.Record4D` and any Java runtime classes through Java 17 required by fixtures or real applications.

`BC186_invokedynamic` should expose the call-site name, descriptor, bootstrap method index, static bootstrap arguments, parsed return type, and parsed argument descriptors. It must not be treated as a normal method reference.

Lowering helpers should stay small and bootstrap-specific:

       Java8LambdaLowering
       JavaStringConcatLowering
       JavaObjectMethodsLowering

Each helper should either lower a recognized common bootstrap into ordinary TotalCross-compatible calls/classes or throw a precise `ConverterException`.

## Revision Notes

2026-07-01 / Codex: Initial ExecPlan written after inspecting the branch, issue 324, Java class-file requirements, and TotalCross deployer/VM call paths.

2026-07-01 / Codex: Revised the plan to make modern Java class-file support the primary objective and to treat `invokedynamic` as layered support for high-value bootstraps rather than an all-or-nothing dynamic runtime.

2026-07-02 / Codex: Added Java 8 lambda lowering milestones, Retrolambda removal validation, and the first smoke app workflow.

2026-07-03 / Codex: Completed the Java 11 class-file metadata milestone and documented dynamic constants, module metadata, and nestmates.

2026-07-04 / Codex: Completed the Java 17 class-file metadata milestone and added initial record deploy support.

2026-07-04 / Codex: Renamed this plan to `modern-java-17-classfile-support-execplan`, limited scope to Java 17, expanded feature status through Java 17, and reorganized smoke coverage around `FeatureSmokeApp`.

2026-07-06 / Codex: Added the handoff rule that each implementation step must finish with the relevant files staged and a descriptive commit message generated.
