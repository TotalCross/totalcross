<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Expand Modern Java Class File Support

This ExecPlan is a living document. The sections `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective` must be kept up to date as work proceeds.

This document follows `.agent/PLANS.md`. Keep it self-contained when revising it: a future implementer should be able to start from this file and the current working tree alone.

## Purpose / Big Picture

The goal is to let TotalCross deploy and run applications compiled by current Java toolchains with the largest practical compatibility gain for the smallest implementation effort. Today the working compatibility baseline is effectively Java 7 class files, major version 51, because Retrolambda is used to lower Java 8 language features before TotalCross sees them. The branch already starts parsing Java 8 `invokedynamic`, but the broader goal is not just one opcode: it is staged support for modern class-file versions, with `invokedynamic` support advanced where it unlocks common Java features.

After this plan is implemented through the early milestones, users should be able to compile ordinary Java 8 applications without Retrolambda, deploy lambdas and method references, and then progressively deploy Java 11, Java 17, Java 21, and newer class files when they do not rely on unsupported runtime APIs or rare dynamic-linking patterns. Support for class-file versions and common compiler output has priority over complete support for every exotic `invokedynamic` bootstrap.

## Version Roadmap

The source of truth for class-file major versions is the Java Version Almanac class-file table. Class files are backward compatible, but newer class files normally cannot run on older VMs unless the VM or deployer understands their class-file structure. This plan targets the following stages:

| Stage | Java release | Class file major | Goal |
| ---: | ---: | ---: | --- |
| Today | Java 7 | 51 | Preserve existing Retrolambda-era behavior. |
| 1 | Java 8 | 52 | Parse Java 8 class files, lower lambda `invokedynamic`, and make Retrolambda removable for lambda use cases. |
| 2 | Java 11 LTS | 55 | Accept Java 9-11 class-file structures and lower common Java 9+ string concatenation `invokedynamic`. |
| 3 | Java 17 LTS | 61 | Accept records, sealed-class metadata, nestmate metadata, and normal Java 17 compiler output when APIs exist. |
| 4 | Java 21 LTS | 65 | Accept Java 21 class files and common non-preview compiler output, while explicitly rejecting unsupported preview or dynamic features. |
| 5 | Java 22 | 66 | Maintain parser acceptance and clear diagnostics for newer attributes and constant-pool entries. |
| 6 | Java 23 | 67 | Maintain parser acceptance and keep common compiler output deployable. |
| 7 | Java 24 | 68 | Maintain parser acceptance and keep common compiler output deployable. |
| 8 | Java 25 LTS | 69 | Treat as the main modern LTS target after Java 21. |
| 9 | Java 26 | 70 | Accept current class-file major 70 where no unsupported runtime feature is required. |

Preview class files, identified by minor version 65535, are out of scope for the initial pass. The deployer should reject preview class files with a clear message naming the class, major version, minor version, and the fact that preview bytecode is intentionally unsupported.

## Progress

- [x] (2026-07-01 23:01Z) Read `.agent/PLANS.md` and created the initial ExecPlan focused on Java 8 lambda lowering.
- [x] (2026-07-01 23:01Z) Reviewed the last four commits on `support-lambda-expressions`: they add constant-pool tags 15, 16, and 18, add `BC186_invokedynamic`, and route opcode 186 through the existing call conversion path.
- [x] (2026-07-01 23:01Z) Inspected `JavaConstantPool`, `JavaClass`, `BC186_invokedynamic`, `MethodCall`, `Bytecode2TCCode`, `GlobalConstantPool`, `J2TC`, and TCVM call dispatch.
- [x] (2026-07-01 23:01Z) Confirmed the class-file major-version roadmap through the Java Version Almanac: Java 8 is 52, Java 11 is 55, Java 17 is 61, Java 21 is 65, Java 25 is 69, and Java 26 is 70.
- [x] (2026-07-01 23:01Z) Revised this ExecPlan so the objective is broader modern class-file support, with `invokedynamic` implemented incrementally according to common compiler output and value delivered.
- [x] (2026-07-01 23:35Z) Built the initial class-file compatibility test harness under `TotalCrossSDK/src/test/java/tc/tools/converter/modernjava`. It generates minimal class files for Java 8, 11, 17, 21, 25, and 26 majors, and compiles source fixtures with the local `javac` when that compiler can target the requested release.
- [x] (2026-07-01 23:38Z) Added temporary Gradle source excludes for unrelated local preview, launcher runtime, SSL, and incompatible test sources so `./gradlew test --tests tc.tools.converter.modernjava.*` can validate the new harness in the current worktree.
- [x] (2026-07-01 23:47Z) Implemented initial parser infrastructure and version gates: normal class files through Java 26 major 70 are accepted, preview minor 65535 and majors above 70 are rejected, class-level attributes are skipped by length, and constant-pool tags 17, 19, and 20 are parsed.
- [x] Implement Java 8 lambda and method-reference lowering from `LambdaMetafactory`.
  - [x] (2026-07-02 00:05Z) Parse `BootstrapMethods`, expose method-handle metadata, and model `BC186_invokedynamic` without pretending it is a normal method call.
  - [x] (2026-07-02 00:25Z) Generate synthetic adapter classes for stateless `metafactory` lambdas backed by `REF_invokeStatic`, enqueue those adapters for deploy, and lower the original site to a normal static factory call.
  - [x] (2026-07-02 00:40Z) Support simply captured `metafactory` lambdas by storing captured arguments in generated adapter fields and passing captured values through the generated factory.
  - [x] (2026-07-02 01:05Z) Support common method references emitted through `LambdaMetafactory`: static references, unbound virtual references whose receiver is the first SAM argument, and bound virtual references whose receiver is captured by the factory.
  - [x] (2026-07-02 01:25Z) Support constructor references emitted through `LambdaMetafactory` when the constructed type exactly matches the SAM return type and no descriptor adaptation is required.
  - [x] (2026-07-02 02:05Z) Support marker-interface cases from `altMetafactory`, including extra marker interfaces declared by javac and the serializable marker flag.
  - [x] (2026-07-02 02:30Z) Support direct bridge methods from `altMetafactory` when bridge arguments exactly match the SAM arguments and the bridge return is exact or reference-covariant.
  - [x] Support descriptor adaptation beyond exact argument and return descriptors.
    - [x] (2026-07-02 02:50Z) Support reference-covariant return adaptation when SAM and instantiated arguments match exactly, such as `Object get()` backed by an implementation returning `String`.
    - [x] (2026-07-02 03:10Z) Support reference argument casts for SAM/instantiated argument descriptors that differ safely, including erased generic SAM arguments lowered through `CHECKCAST`.
    - [x] (2026-07-02 03:35Z) Support common boxing, unboxing, primitive widening, and primitive return adaptation for lambda adapters by lowering through wrapper `valueOf` and primitive value methods.
- [x] (2026-07-02 03:55Z) Add a specific Retrolambda removal milestone and prove the deployer converts direct Java 8 lambda class files without Retrolambda for representative lambda use cases.
- [ ] Implement Java 9+ string-concat lowering from `StringConcatFactory`.
- [ ] Accept Java 11 class-file structures, including module and nestmate metadata, with clear unsupported-feature diagnostics.
- [ ] Accept Java 17 class-file structures, including records and sealed-class metadata, with minimal compatibility classes where needed.
- [ ] Accept Java 21, Java 25, and Java 26 class-file major versions when bytecode and APIs are otherwise supported.
- [ ] Add limited runtime `invokedynamic` or method-handle execution only after the high-value lowering paths and modern class-file parsing are working.

## Surprises & Discoveries

- Observation: `CONSTANT_InvokeDynamic_info` does not contain a class index. Its first field is a `bootstrap_method_attr_index`, and its second field is a `name_and_type_index`.
  Evidence: the current `JavaConstantPool.getClassName(JavaConstantInfo)` returns `String.valueOf(jci.index1)` for type 18, which lets `MethodCall` pretend the bootstrap index is a class name.

- Observation: `BootstrapMethods` is a class-level attribute, not a constant-pool entry. The current manual `JavaClass` parser reads fields and methods but does not parse class attributes after the methods table.
  Evidence: `JavaClass` contains only a commented-out block marked `skip - attributes`, while `JavaCode` already parses method-level `LineNumberTable` attributes.

- Observation: TCVM already has only two method-call opcodes, `CALL_normal` and `CALL_virtual`, and runtime method linking expects a TotalCross global constant-pool method reference.
  Evidence: `TotalCrossVM/src/tcvm/tcvm.c` dispatches `CALL_normal` through `cp->boundNormal[code->mtd.sym]` and `CALL_virtual` through `cp->boundVirtualMethod[code->mtd.sym]`; there is no dynamic call-site opcode.

- Observation: Current SDK build configuration still applies Retrolambda and configures `javaVersion JavaVersion.VERSION_1_7`, while the Java parser currently rejects only class files with major version greater than 52.
  Evidence: `TotalCrossSDK/build.gradle` applies `me.tatarka.retrolambda`, sets `sourceCompatibility = 1.8`, and configures Retrolambda for Java 7 bytecode; `JavaClass` throws for `majorVersion > 52`.

- Observation: Newer Java class-file support is often about parsing and safely ignoring metadata attributes, not implementing new VM opcodes.
  Evidence: records, sealed classes, modules, and nestmates are represented mainly through attributes and constant-pool entries. Ordinary method bodies still use existing bytecodes unless the compiler emits `invokedynamic` for lambdas, method references, string concatenation, or specialized runtime bootstraps.

- Observation: Focused Gradle test execution needed temporary excludes for unrelated local sources before it could reach the new tests.
  Evidence: Before the excludes, `:compileJava` failed on `tc/SSLSocketTest.java`, `totalcross/LauncherRuntime.java`, and `totalcross/preview/*`, and `:compileTestJava` failed on preview/runtime tests. After excluding those sources in `TotalCrossSDK/build.gradle`, `JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-11.jdk/Contents/Home ./gradlew test --tests tc.tools.converter.modernjava.*` completed successfully.

- Observation: Direct parser tests that instantiate `JavaClass` with methods must initialize bytecode classes first.
  Evidence: The first `ClassFileVersionTest` run failed with a `NullPointerException` in `JavaCode` until the tests called `ByteCode.initClasses()`, matching the deployer flow where `J2TC.process` initializes bytecode classes before parsing input classes.

- Observation: For a `Runnable` lambda inside a method named `runnable`, javac emits an invokedynamic call-site name of `run`, the single abstract method name, not the enclosing factory method name.
  Evidence: `InvokeDynamicMetadataTest` initially expected `runnable` and failed with `expected: <runnable> but was: <run>`.

- Observation: A stateless lambda can be lowered without adding a TCVM opcode by generating an ordinary adapter class and replacing the call site with a static factory call.
  Evidence: `Java8LambdaLoweringTest` compiles `Runnable runnable() { return () -> touch(); }`, verifies the generated adapter has `run()` and `$$tc_lambda_factory$0()`, verifies the adapter contains no `invokedynamic`, and `new J2TC(javaClass, true)` no longer throws for the original class.

- Observation: A captured lambda from javac can use the same adapter pattern by storing captured values in final fields and passing them before SAM arguments to the static implementation method.
  Evidence: `Java8LambdaLoweringTest` compiles `Runnable runnable(final String value) { return () -> value.length(); }`, verifies the generated adapter has field `arg$0`, constructor `<init>(Ljava/lang/String;)`, factory `$$tc_lambda_factory$0(Ljava/lang/String;)`, no `invokedynamic`, and both the adapter and original class convert through `J2TC`.

- Observation: Method references emitted by javac can share the generated adapter path as long as the implementation descriptor does not require adaptation.
  Evidence: `Java8LambdaLoweringTest` compiles static `CompiledJava8MethodReference::text`, unbound virtual `CompiledJava8MethodReference::value`, and bound virtual `source::value` references, then verifies three generated adapters and conversion of the adapters and original class through `J2TC`.

- Observation: Constructor references emitted by javac use `REF_newInvokeSpecial` and can be lowered by generating `NEW`, `DUP`, argument loads, `INVOKESPECIAL <init>`, and object return inside the adapter SAM method.
  Evidence: `Java8LambdaLoweringTest` compiles `Box::new`, verifies adapter method `create(Ljava/lang/String;)`, verifies the generated adapter contains no `invokedynamic`, and converts both the adapter and original class through `J2TC`.

- Observation: `altMetafactory` marker-interface arguments can be lowered by adding the marker interfaces to the generated adapter class while keeping the same SAM method body.
  Evidence: `Java8LambdaLoweringTest` compiles `(TextFactory & Marker) CompiledJava8AltMetafactoryMarker::text`, verifies the generated adapter implements both `TextFactory` and `Marker`, verifies it contains no `invokedynamic`, and converts both the adapter and original class through `J2TC`.

- Observation: `altMetafactory` bridge methods can be lowered for direct bridge descriptors by delegating the bridge method to the generated SAM method.
  Evidence: `Java8LambdaLoweringTest` compiles `(StringFactory & ObjectFactory) CompiledJava8AltMetafactoryBridge::text`, verifies generated `get()Ljava/lang/String;` and bridge `get()Ljava/lang/Object;` methods, verifies the adapter contains no `invokedynamic`, and converts both the adapter and original class through `J2TC`.

- Observation: `LambdaMetafactory.metafactory` can emit an instantiated return descriptor that is more specific than the erased SAM return descriptor.
  Evidence: `Java8LambdaLoweringTest` compiles `ObjectFactory factory() { return CompiledJava8ReferenceReturnAdaptation::text; }`, where `ObjectFactory.get()` returns `Object` and `text()` returns `String`, then verifies the adapter has `get()Ljava/lang/Object;`, contains no `invokedynamic`, and converts both the adapter and original class through `J2TC`.

- Observation: Generic functional interfaces often erase SAM arguments to `Object` while the instantiated method type and implementation handle require a narrower reference type.
  Evidence: `Java8LambdaLoweringTest` compiles `ValueMapper<String>` method references backed by both `CompiledJava8ReferenceArgumentAdaptation::trim` and `String::trim`, verifies generated adapters expose `map(Ljava/lang/Object;)Ljava/lang/String;`, contain `CHECKCAST`, contain no `invokedynamic`, and convert both adapters plus the original class through `J2TC`.

- Observation: Generic SAMs can require both unboxing before invoking the implementation handle and boxing before returning through the erased SAM descriptor.
  Evidence: `Java8LambdaLoweringTest` compiles `Function<String, Integer> lengthReference() { return String::length; }` and `Function<Integer, Integer> twiceReference() { return CompiledJava8PrimitiveAdaptation::twice; }`, verifies generated adapters expose `apply(Ljava/lang/Object;)Ljava/lang/Object;`, emit `CHECKCAST`, call `Integer.intValue()` where unboxing is needed, call `Integer.valueOf(int)` where boxing is needed, contain no `invokedynamic`, and convert both adapters plus the original class through `J2TC`.

- Observation: A class compiled directly by javac to Java 8 bytecode can now pass through the deployer lambda lowering path without first running Retrolambda.
  Evidence: `RetrolambdaRemovalTest` compiles `CompiledJava8RetrolambdaRemoval` with javac targeting Java 8, verifies class-file major 52 and remaining `invokedynamic` in the original class, generates five synthetic adapters for captured lambda, static method reference, marker-interface `altMetafactory`, primitive method reference, and constructor reference, verifies the adapters contain no `invokedynamic`, and converts the original class plus every adapter through `J2TC`.

## Decision Log

- Decision: Prioritize accepting modern class-file versions and common javac output over implementing every legal `invokedynamic` behavior.
  Rationale: Users gain more from deploying normal Java 8/11/17/21 code than from rare dynamic-language call-site semantics. The plan should maximize compatibility per unit of effort.
  Date/Author: 2026-07-01 / Codex

- Decision: Implement `invokedynamic` in layers: parse metadata first, lower common bootstraps in the deployer second, remove Retrolambda third, and add runtime call-site execution only for use cases that remain valuable after lowering.
  Rationale: Lowering lets TCVM execute ordinary generated classes and ordinary calls. It also builds the metadata parser required for any later runtime implementation.
  Date/Author: 2026-07-01 / Codex

- Decision: Treat Java 8 `LambdaMetafactory` and Java 9+ `StringConcatFactory` as the two highest-value `invokedynamic` bootstraps.
  Rationale: These are emitted by javac for mainstream language features. Supporting them unlocks far more real applications than implementing mutable call sites first.
  Date/Author: 2026-07-01 / Codex

- Decision: Reject preview class files initially.
  Rationale: Preview features intentionally change across releases and are marked by minor version 65535. Clear rejection is less risky than pretending to support bytecode whose semantics may not match the target JDK.
  Date/Author: 2026-07-01 / Codex

- Decision: Start Java 8 lambda lowering with stateless `LambdaMetafactory.metafactory` sites whose implementation handle is `REF_invokeStatic` and whose SAM, implementation, and instantiated descriptors already match.
  Rationale: This is the smallest deployer-only slice that proves the lowering architecture: generated adapter class, deterministic factory method, normal `CALL_normal`, no TCVM change. Captures and adaptations can build on the same metadata and naming path.
  Date/Author: 2026-07-02 / Codex

- Decision: Extend the same lowering architecture to simple captures by treating the invokedynamic call-site parameters as adapter constructor/factory parameters and storing them in generated final fields.
  Rationale: This unlocks a very common lambda shape without adding runtime `invokedynamic` support. It also keeps unsupported adaptations explicit because the implementation descriptor must exactly equal captured parameters followed by SAM parameters.
  Date/Author: 2026-07-02 / Codex

- Decision: Treat static, unbound virtual, and bound virtual method references as supported when the receiver can be found exactly in the captured values or SAM arguments.
  Rationale: These shapes cover common javac output and reuse the existing adapter class strategy. Exact receiver matching keeps the implementation small and avoids type-adaptation semantics until descriptor adaptation is deliberately implemented.
  Date/Author: 2026-07-02 / Codex

- Decision: Support constructor references only when the constructed class descriptor exactly equals the SAM return descriptor.
  Rationale: Exact matching keeps this deployer-only lowering small and predictable. Wider assignability checks and return adaptation can be added with the broader descriptor-adaptation milestone.
  Date/Author: 2026-07-02 / Codex

- Decision: Support `altMetafactory` marker interfaces now, but keep bridge methods as a separate pending slice.
  Rationale: Marker interfaces only affect the generated adapter class interface list. Bridge methods usually need extra method bodies and often descriptor adaptation, so splitting them preserves the small-step approach and keeps unsupported cases explicit.
  Date/Author: 2026-07-02 / Codex

- Decision: Support only direct `altMetafactory` bridge descriptors whose arguments match the SAM exactly and whose return is exact or reference-covariant.
  Rationale: This covers common bridge output such as `String get()` plus `Object get()` without introducing casting, boxing, unboxing, primitive conversion, or assignability analysis. Broader adaptation remains in the descriptor-adaptation milestone.
  Date/Author: 2026-07-02 / Codex

- Decision: For descriptor adaptation, start with reference-covariant returns while keeping arguments exact.
  Rationale: A more specific implementation return can satisfy an erased reference SAM return with the same bytecode return opcode, so this expands common method-reference support without casts, boxing, unboxing, or assignability analysis.
  Date/Author: 2026-07-02 / Codex

- Decision: Support reference argument adaptation by casting SAM arguments to the instantiated descriptor before invoking the implementation handle.
  Rationale: This covers common generic SAM erasure, such as `map(Object)` dispatching to a `String` method reference, without introducing primitive conversion or boxing semantics.
  Date/Author: 2026-07-02 / Codex

- Decision: Implement primitive descriptor adaptation in generated adapters by composing casts, wrapper unboxing calls, primitive widening opcodes, and wrapper `valueOf` calls.
  Rationale: This keeps the deployer-only lowering model intact and covers common Java 8 generic method-reference shapes without adding runtime `invokedynamic` support.
  Date/Author: 2026-07-02 / Codex

- Decision: Treat Retrolambda as removable for the tested Java 8 lambda/method-reference bytecode shapes, but do not remove the Gradle plugin in this milestone.
  Rationale: The deployer can now lower representative direct Java 8 `invokedynamic` sites, but permanently removing the plugin should also validate SDK build behavior, default methods, and runtime API availability in a broader build-focused change.
  Date/Author: 2026-07-02 / Codex

## Outcomes & Retrospective

No implementation has been completed yet. Update this section after each milestone with the highest class-file version proven by tests, which `invokedynamic` bootstraps are lowered, whether Retrolambda is still required, and which unsupported cases remain intentionally rejected.

2026-07-01 / Codex: The first harness milestone is in place. It does not yet change production parser behavior. Temporary Gradle excludes were added for unrelated local preview, SSL, launcher runtime, and incompatible test sources, and the focused `modernjava` test package passes with JDK 11.

2026-07-01 / Codex: The parser now has a data-driven class-file version policy through Java 26, rejects preview and future major versions clearly, reads modern constant-pool tags for dynamic constants, modules, and packages, and skips unknown class attributes by declared length. The focused `modernjava` tests pass with JDK 11.

2026-07-01 / Codex: The Java 8 lambda milestone now has correct metadata parsing: class-level `BootstrapMethods` are parsed, method handles expose owner/name/descriptor, and opcode 186 has its own bytecode model. Actual lowering to generated adapter classes remains to be implemented.

2026-07-02 / Codex: The first Java 8 lambda lowering slice is implemented for stateless lambdas emitted as `LambdaMetafactory.metafactory` with a static implementation method and no descriptor adaptation. `Java8LambdaLowering` generates deterministic adapter classes, `J2TC` enqueues them when expanding an owner class, and `Bytecode2TCCode` lowers the original `invokedynamic` to a normal static factory call. The focused `modernjava` tests pass with JDK 11. Captured lambdas and method references remain intentionally unsupported with precise diagnostics.

2026-07-02 / Codex: Java 8 lambda lowering now also supports simple captured lambdas where the call-site parameters exactly prefix the static implementation method parameters. The generated adapter stores captures in final fields, the generated factory accepts the captured values, and `Bytecode2TCCode` pops those values from the operand stack before emitting the static factory call. Method references, constructor references, non-static handles, bridge/marker cases, and descriptor adaptation remain unsupported.

2026-07-02 / Codex: The `LambdaMetafactory` lowering now supports common method references in addition to lambda bodies: static method references, unbound virtual method references, and bound virtual method references. The generated SAM method now chooses the correct invoke opcode and receiver source. Constructor references, bridge/marker cases, and descriptor adaptation remain unsupported.

2026-07-02 / Codex: Constructor references are now supported for exact `REF_newInvokeSpecial` cases such as `Box::new`. The generated adapter allocates the implementation owner, invokes its constructor, and returns the new object from the SAM method. `altMetafactory` bridge/marker cases and descriptor adaptation remain unsupported.

2026-07-02 / Codex: `altMetafactory` marker-interface support is implemented for marker-only cases. The parser reads alt-metafactory flags, adds serializable and declared marker interfaces to the generated adapter, and rejects bridge methods with a precise diagnostic. Bridge generation and descriptor adaptation remain unsupported.

2026-07-02 / Codex: Direct `altMetafactory` bridge methods are now generated when they can delegate to the SAM body without adapting arguments and with only exact or reference-covariant return descriptors. Descriptor adaptation remains unsupported for bridge arguments, primitive conversions, boxing, unboxing, or incompatible returns.

2026-07-02 / Codex: Descriptor adaptation now supports the first small non-exact case for Java 8 lambdas and method references: SAM and instantiated argument descriptors must still match exactly, but the implementation may return a more specific reference type than the erased SAM return type. Argument casts, primitive conversions, boxing, and unboxing remain unsupported.

2026-07-02 / Codex: Descriptor adaptation now also supports reference argument casts for erased generic SAM methods. Generated adapters load arguments using the public SAM descriptor and insert `CHECKCAST` to the instantiated argument or receiver type before invoking the implementation handle. Boxing, unboxing, primitive widening, and primitive return adaptation remain unsupported.

2026-07-02 / Codex: The planned Java 8 `LambdaMetafactory` lowering milestone is complete for the common javac shapes targeted by this ExecPlan. Generated adapters now support stateless and captured lambdas, static/bound/unbound method references, constructor references, `altMetafactory` markers and direct bridges, reference casts, reference-covariant returns, and common primitive boxing/unboxing/widening through wrapper methods. Rare adaptations and arbitrary bootstrap behavior remain intentionally outside this initial lowering milestone.

2026-07-02 / Codex: The Retrolambda-removal proof for lambda bytecode is in place. A javac-produced Java 8 class file with `invokedynamic` is converted directly by the deployer lowering path, along with all generated adapters. The Gradle Retrolambda plugin remains configured until a separate build change validates the full SDK/app build surface, including default methods and runtime API compatibility.

## Context and Orientation

A Java class file has a major version. Java 7 uses major 51, Java 8 uses 52, Java 11 uses 55, Java 17 uses 61, Java 21 uses 65, Java 25 uses 69, and Java 26 uses 70. A class file also has a minor version. For modern Java releases, minor version 0 is normal and minor version 65535 means the class uses preview features from that exact JDK release.

The deployer parses Java class files under `TotalCrossSDK/src/main/java/tc/tools/converter/java`. `JavaClass` owns class metadata, `JavaConstantPool` owns raw constants, `JavaMethod` owns method declarations, and `JavaCode` turns method bytecode bytes into `ByteCode` objects. `Bytecode2TCCode` converts those bytecode objects into TotalCross IR instructions. `GlobalConstantPool` serializes method, field, class, and literal references into the TCZ constant pool. TCVM reads that pool in `TotalCrossVM/src/tcvm/tcclass.c` and executes method calls in `TotalCrossVM/src/tcvm/tcvm.c`.

`invokedynamic` is Java bytecode opcode 186. It names a dynamic call site rather than directly naming a target class and method. The JVM links that site through a class-level `BootstrapMethods` attribute and a bootstrap method such as `java.lang.invoke.LambdaMetafactory.metafactory` or `java.lang.invoke.StringConcatFactory.makeConcatWithConstants`. For TotalCross, the most practical first implementation is not a generic dynamic runtime. It is deployer lowering: recognize common bootstrap methods, generate ordinary bytecode/classes or ordinary TotalCross IR calls, and let TCVM execute existing opcodes.

Modern class files add constant-pool tags and attributes that older parsers must understand enough to skip or translate safely. Java 9 introduced module metadata and `CONSTANT_Module`/`CONSTANT_Package` tags. Java 11 introduced `CONSTANT_Dynamic`, which is similar to `invokedynamic` but creates a constant rather than a call site. Java 11 also includes nestmate metadata. Java 16/17-era class files include records and sealed-class metadata. Later releases continue to increment the major version and may add attributes even when ordinary method bytecode remains compatible.

## Plan of Work

Start by building tests that define compatibility as observable behavior. Add a package such as `TotalCrossSDK/src/test/java/tc/tools/converter/modernjava`. It should compile small source fixtures with available local JDKs when possible. When a specific JDK is not installed, tests should still exercise parser support with generated byte arrays or ASM-generated classes for that major version. Each fixture must state its intended Java version, class-file major, feature used, and expected deployer outcome.

Implement a strict class-file version gate in `JavaClass`. It should accept normal minor version 0 up to the highest milestone currently implemented, reject preview minor 65535 with a clear message, and reject higher major versions with a message that says which maximum is supported. The gate must be data-driven so each milestone can raise the maximum without hunting for scattered constants.

Modernize the class-file parser before changing conversion semantics. Update `JavaConstantPool` to parse and name all constant-pool tags needed through Java 26 class-file acceptance: existing tags 1 through 18, plus `CONSTANT_Module` tag 19 and `CONSTANT_Package` tag 20. Keep explicit accessor methods for MethodHandle, MethodType, Dynamic, InvokeDynamic, Module, and Package entries. Update `JavaClass`, `JavaField`, `JavaMethod`, and `JavaCode` to skip unknown attributes using their declared lengths and to parse known attributes only when the deployer needs their contents.

The first `invokedynamic` layer is metadata correctness. Add `JavaBootstrapMethod`, `JavaMethodHandle`, and descriptor utilities. `BC186_invokedynamic` must stop extending `MethodCall`, because an invokedynamic entry is not a normal method reference. It should expose the call-site name, descriptor, bootstrap method index, static bootstrap arguments, parsed return type, and parsed argument descriptors. Unsupported dynamic sites must fail with a diagnostic that names the capturing class, method, bytecode position, bootstrap owner, and bootstrap method.

The second `invokedynamic` layer is Java 8 lambda lowering. Recognize `java/lang/invoke/LambdaMetafactory.metafactory` and the common subset of `altMetafactory`. Generate synthetic adapter classes with ASM, append them to the `J2TC` input queue, and lower the original `invokedynamic` to a normal `CALL_normal` factory call. Support stateless lambdas, captured lambdas, static method references, virtual/interface method references, private/special helper references emitted by javac, and constructor references when the generator can do so safely. Unsupported adaptations such as complex bridge generation, unusual marker interfaces, or hard boxing/unboxing cases should fail clearly at first.

The Retrolambda milestone comes immediately after Java 8 lambda lowering. Remove or disable the Retrolambda plugin in a controlled branch of the build, compile the SDK/application fixtures directly to Java 8 class files, deploy them, and compare behavior. Do not remove Retrolambda permanently until tests prove lambdas, method references, default methods used by the SDK, and `java.util.function` compatibility classes all work. If default methods need separate support, implement that before declaring Retrolambda removable.

The third `invokedynamic` layer is Java 9+ string concatenation. Modern javac emits calls to `java/lang/invoke/StringConcatFactory.makeConcat` or `makeConcatWithConstants` for many string concatenations. Lower these sites in the deployer to existing TotalCross-compatible string building behavior, preferably using `java.lang.StringBuffer`/`StringBuilder` mappings already present in `GlobalConstantPool`. This is required for practical Java 11+ support because ordinary source code with `+` on strings can otherwise introduce unsupported `invokedynamic`.

For Java 11 class-file support, accept major 55 and all intermediate majors 53 and 54. Parse or skip module attributes, `ModulePackages`, `ModuleMainClass`, `NestHost`, and `NestMembers`. Parse `CONSTANT_Dynamic` tag 17. Initially support only dynamic constants that can be lowered cheaply and deterministically; reject the rest with precise diagnostics. Add tests for a Java 11 class that uses string concatenation, a nested private-access pattern that emits nestmate metadata, and a jar that includes `module-info.class`.

For Java 17 class-file support, accept major 61 and intermediate majors 56 through 60. Parse or skip `Record`, `PermittedSubclasses`, and related attributes. Add minimal `jdkcompat.lang.Record4D` if record classes need a superclass available on device. Record classes should deploy as ordinary final classes with fields and generated methods; sealed-class enforcement can initially be treated as metadata only. Add tests that deploy a simple record-like compiled class if API support allows it, and tests that sealed metadata is accepted or clearly rejected if the runtime type hierarchy cannot represent it.

For Java 21 class-file support, accept major 65 and intermediate majors 62 through 64. Keep preview bytecode rejected. Add fixtures for ordinary Java 21 compiler output that does not require unavailable Java 21 runtime APIs. If javac emits `invokedynamic` for switch or pattern-related runtime bootstraps in non-preview code, decide based on frequency: either lower the bootstrap if it is common and simple, or reject it clearly while still accepting other Java 21 class files.

For Java 22 through Java 26, raise parser acceptance one release at a time: major 66, 67, 68, 69, then 70. Each step must include at least one fixture proving the deployer can parse and either convert or intentionally reject a class file of that version. The main work is expected to be keeping the parser resilient, maintaining the known attribute list, and ensuring unsupported runtime APIs produce actionable errors rather than parser crashes.

Only after the high-value lowering paths are working should runtime `invokedynamic` be considered. A limited runtime implementation may add TCZ metadata and a TCVM dynamic-call mechanism for `ConstantCallSite`-like behavior, but it should be justified by real unsupported applications. Full `MutableCallSite`, `VolatileCallSite`, arbitrary bootstrap methods, and complete `MethodHandle.invokeExact` semantics are not the priority while class-file-version support remains behind current Java releases.

## Concrete Steps

Work from the repository root unless a command specifies another directory.

1. Confirm the baseline branch and current invokedynamic commits:

       git status --short
       git log --oneline -n 4

   Expect the last four commits to be the invokedynamic parser/converter commits. Preserve unrelated dirty worktree changes.

2. Add modern class-file parser tests:

       cd TotalCrossSDK
       ./gradlew test --tests tc.tools.converter.modernjava.*

   Initially expect failures for unsupported major versions, missing `BootstrapMethods`, and modern constant-pool entries.

3. Implement parser infrastructure and version gates, then run:

       cd TotalCrossSDK
       ./gradlew test --tests tc.tools.converter.modernjava.ClassFileVersionTest --tests tc.tools.converter.modernjava.ConstantPoolModernTagsTest

   Expect normal class files up to the implemented maximum to parse, preview minor 65535 to be rejected clearly, and unknown-but-skippable attributes to be skipped by length.

4. Implement Java 8 lambda lowering, then run:

       cd TotalCrossSDK
       ./gradlew test --tests tc.tools.converter.modernjava.Java8LambdaLoweringTest

   Expect stateless lambdas, captured lambdas, method references, and constructor references supported by the milestone to convert into generated classes and ordinary `CALL_normal` calls.

5. Prove whether Retrolambda can be removed:

       cd TotalCrossSDK
       ./gradlew test --tests tc.tools.converter.modernjava.RetrolambdaRemovalTest

   This test group should compile fixtures without Retrolambda and verify deployer behavior. If the build file is changed to remove Retrolambda, run the full focused SDK test suite before committing that removal.

6. Implement Java 9+ string-concat lowering and Java 11 parser support:

       cd TotalCrossSDK
       ./gradlew test --tests tc.tools.converter.modernjava.Java11ClassFileTest --tests tc.tools.converter.modernjava.StringConcatFactoryLoweringTest

   Expect ordinary Java 11 classes with string concatenation and nestmate metadata to deploy or fail only for known missing runtime APIs.

7. Implement Java 17 parser support:

       cd TotalCrossSDK
       ./gradlew test --tests tc.tools.converter.modernjava.Java17ClassFileTest

   Expect record and sealed metadata to be accepted or precisely rejected if a specific runtime API is missing.

8. Implement Java 21, Java 25, and Java 26 parser support:

       cd TotalCrossSDK
       ./gradlew test --tests tc.tools.converter.modernjava.Java21ClassFileTest --tests tc.tools.converter.modernjava.Java25ClassFileTest --tests tc.tools.converter.modernjava.Java26ClassFileTest

   Expect class-file major 65, 69, and 70 to pass parser acceptance and common conversion fixtures.

9. Run focused regression tests after each milestone:

       cd TotalCrossSDK
       ./gradlew test --tests tc.tools.converter.modernjava.* --tests totalcross.LauncherArgumentParserTest --tests totalcross.LauncherRuntimeTest

10. Run broad SDK validation when focused tests pass:

       cd TotalCrossSDK
       ./gradlew clean dist -x test

   Do not commit Gradle outputs or generated deployment artifacts.

## Validation and Acceptance

Class-file acceptance is proven by tests that compile or generate class files for the targeted major version and then parse them with `JavaClass`. A successful milestone must either convert the class or reject it with a message that identifies a specific unsupported feature. Generic parser crashes, fake numeric class names, array-index failures, and misleading `Invalid bytecode index` errors are not acceptable.

Java 8 acceptance is that lambdas and method references deploy without Retrolambda. The generated TCZ must contain ordinary synthetic adapter classes, and the converted methods must use ordinary TotalCross calls. A smoke application must demonstrate at least one stateless lambda and one captured lambda producing the expected output.

Retrolambda removal is accepted only when disabling the plugin still lets the SDK and app fixtures compile, deploy, and run the Java 8 lambda tests. If default methods or functional interfaces fail, the milestone is incomplete even if lambda lowering itself works.

Java 11 acceptance includes successful parsing of class-file major 55, module metadata, nestmate metadata, and lowering of javac string concatenation via `StringConcatFactory`. This matters because string `+` is common enough that Java 11 support without string-concat lowering would be frustratingly narrow.

Java 17 acceptance includes successful parsing of major 61 and metadata for records and sealed classes. Full semantic enforcement of sealed classes is not required initially; deployer acceptance and ordinary method behavior are more important.

Java 21, 25, and 26 acceptance is parser and common-output acceptance. Each stage must prove that the deployer can recognize the major version, skip or understand metadata safely, and either convert ordinary code or reject unsupported runtime APIs with clear messages.

`invokedynamic` acceptance is intentionally layered. The first accepted bootstraps are `LambdaMetafactory` and `StringConcatFactory`. Other bootstraps are accepted only when implemented or rejected clearly. Complete dynamic-language support, mutable call sites, volatile call sites, arbitrary bootstrap execution, and full `MethodHandle` combinators are not required for the modern-class-file milestones unless a real high-value fixture depends on them.

## Idempotence and Recovery

All generated fixtures must write to JUnit temporary directories or in-memory byte arrays. Do not commit generated `.class`, `.jar`, `.tcz`, Gradle `build/`, native build, or dependency-cache artifacts. Generated lambda adapter class names must be deterministic for a given input class and call-site position so repeated deploys do not drift.

Parser changes must be safe to rerun. Unknown attributes should be skipped using their declared lengths, and unsupported known features should throw converter errors before mutating global conversion state. If conversion of a generated class fails, the error should name the generated class and original call site.

If a local machine lacks a target JDK such as JDK 25 or JDK 26, tests for those versions should use ASM-generated class files or checked-in source compiled by the available compiler only when the compiler can target that release. Do not download JDKs or dependencies without user approval.

If class-file parsing becomes blocked by a rare feature, keep the milestone moving by rejecting that feature clearly and documenting it in `Outcomes & Retrospective`. The priority is broad safe acceptance, not pretending that every modern Java runtime API exists on device.

## Artifacts and Notes

Current branch evidence:

       730453e70 feat(converter): parse Java 8 invokedynamic bytecode
       1deab8bed feat(converter): parse invokedynamic bytecode
       7c4fdeaf8 fix(converter): resolve invokedynamic class names
       6ecdbe0ee feat(converter): lower invokedynamic to tcvm instructions

Important current code locations:

       TotalCrossSDK/src/main/java/tc/tools/converter/java/JavaConstantPool.java
       TotalCrossSDK/src/main/java/tc/tools/converter/java/JavaClass.java
       TotalCrossSDK/src/main/java/tc/tools/converter/java/JavaMethod.java
       TotalCrossSDK/src/main/java/tc/tools/converter/java/JavaCode.java
       TotalCrossSDK/src/main/java/tc/tools/converter/bytecode/BC186_invokedynamic.java
       TotalCrossSDK/src/main/java/tc/tools/converter/Bytecode2TCCode.java
       TotalCrossSDK/src/main/java/tc/tools/converter/GlobalConstantPool.java
       TotalCrossSDK/src/main/java/tc/tools/converter/J2TC.java
       TotalCrossSDK/build.gradle
       TotalCrossVM/src/tcvm/tcclass.c
       TotalCrossVM/src/tcvm/tcvm.c

The current `BC186_invokedynamic` is only:

       public class BC186_invokedynamic extends MethodCall {
          public BC186_invokedynamic() {
            super(readUInt16(pc + 1));
            pcInc = 5;
          }

          @Override
          public void exec() {
          }
        }

That shape must not remain, because an invokedynamic constant-pool entry is not a normal method reference.

## Interfaces and Dependencies

Create a small version policy in the parser, for example:

       public final class JavaClassFileVersion {
         public static final int JAVA_7 = 51;
         public static final int JAVA_8 = 52;
         public static final int JAVA_11 = 55;
         public static final int JAVA_17 = 61;
         public static final int JAVA_21 = 65;
         public static final int JAVA_25 = 69;
         public static final int JAVA_26 = 70;
         public static int maxSupportedMajor();
         public static void validate(String className, int major, int minor);
       }

In `tc.tools.converter.java.JavaClass`, add:

       public JavaBootstrapMethod[] bootstrapMethods;

In `tc.tools.converter.java.JavaBootstrapMethod`, define:

       public final int bootstrapMethodRef;
       public final int[] bootstrapArguments;

In `tc.tools.converter.java.JavaMethodHandle`, define:

       public final int referenceKind;
       public final int referenceIndex;
       public String getOwner(JavaConstantPool cp);
       public String getName(JavaConstantPool cp);
       public String getDescriptor(JavaConstantPool cp);

In `tc.tools.converter.bytecode.BC186_invokedynamic`, expose:

       public final int constantPoolIndex;
       public final int bootstrapMethodAttrIndex;
       public final int nameAndTypeIndex;
       public final String name;
       public final String descriptor;
       public String ret;
       public String signature;
       public String[] jargs;

In a new converter package such as `tc.tools.converter.modernjava`, provide lowerers with a shared shape:

       public interface InvokeDynamicLowerer {
         boolean supports(InvokeDynamicSite site);
         LoweredInvokeDynamic lower(InvokeDynamicSite site);
       }

Add at least:

       LambdaMetafactoryLowerer
       StringConcatFactoryLowerer

The returned `LoweredInvokeDynamic` must describe either a generated class and factory method, a sequence of ordinary IR instructions, or a precise unsupported-feature error.

In `J2TC` or a nearby generated-class registry, provide a method equivalent to:

       public static void enqueueGeneratedClass(String internalName, byte[] bytes);

The method must append a `TCZ.Entry` for a parsed `JavaClass` only once per internal name.

Compatibility classes should follow the existing `jdkcompat` convention and use `4D` suffixes. Expected early additions include `jdkcompat.lang.invoke.*4D` for method-handle API shells, `jdkcompat.lang.Record4D` for records if needed, and any common Java runtime classes required by fixtures. Add compatibility APIs only when tests show a real class-file milestone needs them.

## Revision Notes

2026-07-01 / Codex: Initial ExecPlan written after inspecting the branch, issue 324, Java class-file requirements, and TotalCross deployer/VM call paths. The first version focused on Java 8 lambda lowering.

2026-07-01 / Codex: Revised the plan to make modern Java class-file support the primary objective. The revised roadmap starts from today's Java 7 class-file baseline, stages Java 8, 11, 17, 21, 22, 23, 24, 25, and 26 support, and treats `invokedynamic` as layered support for high-value bootstraps rather than an all-or-nothing full dynamic runtime.

2026-07-01 / Codex: Added the initial `modernjava` test harness and recorded the current validation blocker. The next implementation step remains parser infrastructure and version gates.

2026-07-01 / Codex: Added Gradle excludes for unrelated local preview/SSL/runtime test sources and reran the focused package successfully. The next implementation step remains parser infrastructure and version gates.

2026-07-01 / Codex: Implemented parser infrastructure and version gates, with focused tests covering roadmap major versions, preview rejection, future-version rejection, unknown class attributes, and modern constant-pool tags. The next implementation step is Java 8 lambda and method-reference lowering from `LambdaMetafactory`.

2026-07-01 / Codex: Added the metadata foundation for Java 8 lambda lowering and a focused test proving a compiled Java 8 lambda exposes `LambdaMetafactory.metafactory` through `BootstrapMethods`. The next implementation step remains the actual lambda adapter generation/lowering.
