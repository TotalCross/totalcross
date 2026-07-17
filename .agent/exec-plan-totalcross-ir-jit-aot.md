<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Build a verified TotalCross IR with baseline JIT and portable AOT backends

This ExecPlan is a living document. The sections `Progress`, `Surprises & Discoveries`, `Decision Log`, `Outcomes & Retrospective`, and `Editorial Report` must be kept up to date as work proceeds.

This plan follows `.agent/PLANS.md` and the repository-wide `AGENTS.md`. It is deliberately self-contained: a future contributor should be able to start from this file, reproduce the current analysis, implement one milestone at a time, and stop safely after any accepted milestone. All repository documentation produced by this plan is written in English.

## Purpose / Big Picture

TotalCross currently translates Java class files into a compact, register-based TotalCross class and bytecode format, then executes that bytecode in the C `executeMethod` interpreter. The goal is to insert a typed, backend-neutral TotalCross intermediate representation, or TCIR, after the existing bytecode format. TCIR will first be executable through a small reference interpreter, then through a fast-compiling SLJIT baseline backend and a deterministic portable-C AOT backend. The existing bytecode interpreter remains the compatibility oracle and permanent fallback.

When the plan is complete, a developer can take the same valid TotalCross method through four paths—current bytecode interpreter, TCIR interpreter, SLJIT machine code, and generated C—and obtain the same result, exception, GC behavior, and observable runtime effects for every operation advertised by a backend. An experimental build option selects a backend per compatible method; unsupported or failed methods start and remain in the interpreter without changing TCZ files. The design leaves a versioned backend interface for a future LLVM or Cranelift optimizing tier without making either a required dependency.

Correctness, portability, diagnostics, and safe fallback are the primary outcomes. Performance is measured only after differential correctness exists. The initial proof of concept is intentionally limited to static integer methods with constants, parameters, locals, addition/subtraction/multiplication, comparisons, branches, simple loops, and integer/void returns. A simple static call is added only after the no-call slice passes through all execution paths.

## Progress

- [x] (2026-07-17T05:14:37Z) Read `.agent/PLANS.md`, `AGENTS.md`, and the attached requirements; established the documentation-only boundary for this task.
- [x] (2026-07-17T05:14:37Z) Traced class serialization, Java conversion, all 160 TotalCross opcodes, `executeMethod`, runtime class/method/field structures, frames, calls, exceptions, monitors, and GC roots.
- [x] (2026-07-17T05:14:37Z) Created the eight factual/proposed architecture documents and the initial compatibility inventory without changing analyzed source files or VM behavior.
- [x] (2026-07-17T05:41:55Z) Validated nine new artifacts: all headers and required plan sections are present, all 160 C opcode names occur in both the reference and matrix, Java numeric values match C, runtime dispatch matches C, no Portuguese-language hits were found, whitespace checks passed, and scoped source status is unchanged.
- [ ] Milestone 2: turn the TCIR proposal into versioned C interfaces, an opcode mapping registry, a structural verifier, and canonical text golden fixtures.
- [ ] Milestone 3: implement the partial TotalCross-bytecode-to-TCIR frontend for the proof-of-concept subset.
- [ ] Milestone 4: implement the TCIR reference interpreter and prove differential equivalence with `executeMethod`.
- [ ] Milestone 5: integrate pinned SLJIT and implement the baseline JIT subset behind an experimental build/runtime flag.
- [ ] Milestone 6: implement deterministic portable-C AOT generation, host compilation, registration, and comparison tests.
- [ ] Milestone 7: integrate method-atomic mixed-mode dispatch with GC, exception, call, lifecycle, and observability safeguards.
- [ ] Milestone 8: expand coverage operation family by operation family until every valid opcode has a tested mapping or an explicit retained-fallback decision.
- [ ] Milestone 9: complete the platform, security, performance, and release-readiness gates; reconcile outcomes and finalize the Editorial Report.

## Surprises & Discoveries

The current TotalCross bytecode is already register-based. Java operand-stack manipulation is mostly consumed during conversion by `Bytecode2TCCode`; `executeMethod` operates on the three typed `Context` arenas and has no JVM operand stack. The new frontend therefore reads typed register slots and constructs virtual values; it does not need to reconstruct the original JVM stack. Evidence is in `Bytecode2TCCode.java`, `OperandReg.java`, `tcvm.c`, and `context.h`.

The existing Java package named `tc.tools.converter.ir` is not a backend-neutral IR. Its `Instruction` subclasses mirror TotalCross opcode shapes and serialize directly to `TCode`; CFG and liveness analyses are useful prior art, but adopting its instruction model would preserve the coupling this work is meant to remove.

The internal class record has no magic or independent version. Version 200 belongs to the outer TCZ container. The record uses compact bitfields and indexes, with important 12-bit symbol limits in multiple instruction layouts. Any future format change needs an explicit versioning strategy; this plan does not change the format.

Most instructions occupy one 4-byte slot, but calls, switch, and multidimensional arrays consume continuation slots. All PCs used by branches, exception handlers, and line maps are slot indexes. A decoder must mark continuation slots before accepting targets.

The converter normalizes Java `float` into the runtime double bank. TCIR built from serialized TotalCross bytecode cannot reliably recover an `f32` type. The initial type system therefore models the observable bytecode as `f64` and records any future preservation of `f32` as a converter/format extension.

`TCConstants.bcTClassNames` does not include textual names for opcode values 158 and 159, while Java numeric constants, `opcodes.h`, and runtime dispatch include `MONITOR_Enter2` and `MONITOR_Exit2`. A generated cross-check, not the text-name array, must be the coverage authority.

The current collector is non-moving and does not conservatively scan C stacks or CPU registers. It finds interpreted-frame references in `Context.regO`. This makes homes in the existing `regO` arena the safest POC root protocol and makes “keep a reference in a native register across a helper” incorrect.

There is no explicit periodic safepoint opcode in `executeMethod`. Allocation and runtime helper calls are the immediate collection boundary. Cooperative backedge polls, if required for thread suspension or GC, are a later runtime protocol rather than a backend guess.

Method-level `synchronized` state is carried but automatic locking code is commented out and the converter reports it unsupported. The four explicit monitor opcodes are distinct. JIT/AOT work must preserve current semantics, not opportunistically implement synchronized methods.

The current reference-array store handlers perform null/bounds checks in `arc` form but write references without a dynamic array-store compatibility check. `TEST_regO` is also a throwing null check, not a branch despite its placement near branch handlers. These details must be encoded from implementation evidence rather than inferred from opcode names; any semantic correction belongs in a separately scoped compatibility change.

The existing workflow target and the requested architecture matrix are not identical. The inspected workflow includes Windows x86, while the requested first-class matrix names Windows x86-64. Both must remain visible until product/toolchain support is confirmed.

The native build has one source of truth: `TotalCrossVM/CMakeLists.txt`. `TotalCrossVM/src/jni/Android.mk` and the generated/legacy `TCVM.xcodeproj` are explicitly out of scope and must not be inspected, edited, patched, or used as validation evidence by this plan. Android and Apple native builds enter through the root CMake project with their platform toolchain/generator.

SLJIT's upstream architecture coverage fits most TotalCross CPU targets, but executable-memory policy is separate from CPU support. macOS requires a compliant Hardened Runtime/`MAP_JIT`/W^X configuration, Android requires device and distribution-policy validation, and this plan keeps iOS JIT off in favor of statically linked AOT.

## Decision Log

- Decision: Keep the current TCZ and 4-byte TotalCross bytecode as the canonical shipping input for the initial architecture. Rationale: it preserves compatibility and makes the current interpreter a stable oracle. Date: 2026-07-17.
- Decision: Define TCIR independently of current opcode encodings. Rationale: backend-neutral semantic operations, explicit effects, and stable types are required for verification, multiple backends, and future optimization. Date: 2026-07-17.
- Decision: Use typed basic blocks, immutable virtual values, block arguments at merges, and explicit typed TotalCross register-home slots at the bytecode boundary. This is simplified SSA rather than a stack IR or fully mutable register IR. Rationale: the input is already register-based; the hybrid gives deterministic merges and a simple construction path. Date: 2026-07-17.
- Decision: Build and test a TCIR reference interpreter before native backends. Rationale: it separates frontend/IR semantic defects from native code-generation defects. Date: 2026-07-17.
- Decision: Use a uniform frame ABI that aliases the existing `Context` typed arenas. Do not mirror the variadic public `executeMethod` ABI and do not use a boxed value ABI for internal compiled calls. Rationale: arena frames match current calls and GC roots across platforms while accepting modest POC overhead. Date: 2026-07-17.
- Decision: Give every live managed reference a home in `Context.regO` before a `may_gc` helper. Do not use native stack maps in the baseline. Rationale: the current GC already scans these arenas and does not scan native stacks. Date: 2026-07-17.
- Decision: Reuse the current explicit pending-exception protocol. Compiled code publishes TC PC, calls/checks helpers, and returns an exception status to the shared dispatcher. Do not use C++ or native unwinding initially. Rationale: this preserves handler and stack-trace behavior. Date: 2026-07-17.
- Decision: Make compilation method-atomic. Unsupported TCIR causes a structured pre-execution fallback; once compiled code begins, it must return or propagate an exception and may not restart the method in the interpreter. Rationale: restarting duplicates observable effects. Date: 2026-07-17.
- Decision: Store POC compilation state and artifact ownership in a side table keyed by runtime `Method`, rather than immediately changing the serialized/runtime method struct. Rationale: lifecycle, concurrency, unloading, and public-layout impacts need proof first. Date: 2026-07-17.
- Decision: Use SLJIT for the first baseline JIT, pinned and license-compliant through the native dependency flow. Rationale: its low-level API and architecture support fit a fast baseline while TCIR retains semantic ownership. Date: 2026-07-17.
- Decision: Generate deterministic portable C for the first AOT backend and compile it with each platform's native toolchain. Do not require LLVM. Rationale: this maximizes platform reach, supports iOS static linking, and keeps the backend inspectable. Date: 2026-07-17.
- Decision: Keep JIT experimental and off by default. Keep it off by project policy on iOS; use AOT as the primary iOS path. Gate macOS and Android separately on executable-memory, entitlement, signing, device, and distribution review. Date: 2026-07-17.
- Decision: Treat LLVM or Cranelift only as a future optimizing tier behind the same verified backend interface. Rationale: neither dependency is justified before semantics, helper ABI, and baseline conformance are stable. Date: 2026-07-17.
- Decision: Prefer runtime helpers for resolution, calls, allocation, complex object/array operations, checks, monitors, and exceptions until layouts/effects are documented and tested. Rationale: direct lowering must not bypass GC, lazy linking, or error ordering. Date: 2026-07-17.
- Decision: Make `TotalCrossVM/CMakeLists.txt` the exclusive native-build integration point. Ignore legacy `TCVM.xcodeproj` and `TotalCrossVM/src/jni/Android.mk`; do not edit or validate against them. Rationale: the user confirmed that every supported native build now originates at the root TotalCrossVM CMake project. Date: 2026-07-17.
- Decision: Documentation in this repository is English-only. Rationale: explicit user instruction received while drafting the analysis artifacts. Date: 2026-07-17.

## Outcomes & Retrospective

The completed analysis stage produced a traceable description of the existing format and runtime plus a proposed TCIR/JIT/AOT architecture. It did not implement a compiler, alter VM behavior, edit analyzed source, or produce performance claims. The most consequential result is the GC/ABI choice: compiled frames can initially reuse the same typed `Context` arenas, avoiding an unproven native-stack scanner.

The source inventory also corrected two potentially dangerous assumptions before implementation: the runtime input is register bytecode rather than a JVM stack machine, and the package already named `converter.ir` is target-shaped rather than the independent IR. The opcode inventory found the missing final monitor names in one Java text table, which justifies an automated single-source coverage check in Milestone 2.

This section must be updated after every completed milestone with delivered behavior, validation evidence, deferred scope, and lessons. At full completion it must state exactly which opcode and platform sets are production-ready, which remain experimental, and why.

## Editorial Report

This is an interim factual report for the completed analysis/documentation stage. It is not the final report required by `.agent/PLANS.md`; Milestone 9 must reconcile it with actual implementation, tests, measurements, and `Outcomes & Retrospective`.

### Editorial Summary

The project now has a source-grounded map from Java class parsing through TotalCross bytecode execution and GC, plus a staged design for a backend-neutral TCIR, an SLJIT baseline JIT, and generated-C AOT. No runtime implementation was changed at this stage.

### Original Plan versus Actual Outcome

The requested outcome for this task was only the ExecPlan and explicit architecture documents. That boundary was met. The future compiler, VM integration, and benchmarks remain planned work and are not represented as completed.

### What Changed

Eight new documents under `docs/architecture/bytecode` and this ExecPlan were added. No existing analyzed source file was edited. During drafting, all repository documentation was standardized to English following the user's instruction.

### Decisions and Trade-offs

The proposed POC selects a simplified-SSA, block/value IR; a frame ABI over current typed arenas; reference homes in `regO`; explicit exception status; method-atomic fallback; SLJIT as the baseline; and deterministic generated C as AOT. These decisions prioritize proof and portability over peak performance.

### Unexpected Problems and Discoveries

The existing converter IR is opcode-shaped, float identity is normalized, the class record is unversioned inside TCZ, and one Java opcode-name table omits values 158/159. These discoveries materially shaped the verifier, type, and inventory requirements.

### Validation and Measurable Results

At this stage, validation is static: all required source/artifact paths existed; the 160 C opcode definitions matched runtime dispatch and the Java numeric constants; every name appeared in both the reference and compatibility matrix; all nine new files had the required header and no detected Portuguese-language terms; every required ExecPlan heading was present; no-index whitespace checks and `git diff --check` passed; and scoped analyzed-source status was empty. No performance benchmark is valid yet because no execution implementation changed.

### Useful Evidence and Examples

The bytecode reference enumerates all 160 opcodes. The TCIR design includes a deterministic textual loop example. The JIT/AOT document records the mixed-mode ABI and platform gates. The compatibility matrix distinguishes current interpreter behavior from POC and planned backend coverage.

### Limitations, Remaining Work, and Open Questions

No TCIR parser, verifier, interpreter, native backend, runtime integration, or platform execution test exists yet. The helper `may_gc` closure, thread-suspension protocol, class/artifact lifecycle, volatile/atomic semantics, and real-world legacy `JUMP_regI` corpus require deeper inspection during implementation.

### Possible Article Angles

A future technical article could explain how a register-bytecode interpreter can gain multiple execution tiers without changing its shipping format, or how reusing managed register arenas simplifies the first correct GC contract for generated code.

### Suggested Narrative

Start with the existing Java-to-register-bytecode path, show why the target-shaped converter IR cannot serve multiple backends, introduce verified TCIR as the semantic boundary, then explain how the same frame/helper contract supports an interpreter, low-latency SLJIT, and portable generated C. End with evidence from differential tests and measured trade-offs once they exist.

### Claims Requiring Human Review

Before external publication, maintainers must review platform policy statements, the interpretation of synchronized-method behavior, class/format compatibility claims, SLJIT licensing notices, and every benchmark comparison. Product/security owners must approve any JIT enablement on Apple or Android platforms.

## Context and Orientation

The Java frontend is under `TotalCrossSDK/src/main/java/tc/tools/converter`. `J2TC.java` converts `JavaClass` objects into `TCClass` records and writes them through `TCClass.write`; `Bytecode2TCCode.java` simulates Java operands and chooses TotalCross instructions. `oper/OperandReg.java`, `ir/CFG.java`, `regalloc/RegAllocation.java`, `GenerateInstruction.java`, `ChooseOpcode.java`, and instruction classes support this process. Serialized structures live in `tclass/TCClass.java`, `TCMethod.java`, and `TCField.java`; `GlobalConstantPool.java` writes the shared pool; `totalcross/util/zip/TCZ.java` supplies the outer container.

The native runtime is under `TotalCrossVM/src/tcvm`. `tcclass.h` defines `TCode`, `TValue`, `TConstantPool`, `TCClass`, and `TMethod`; `tcclass.c` loads records and establishes layouts. `opcodes.h` is the numeric opcode source, while `tcvm.c` implements `executeMethod`. `tcmethod.c`, `tcfield.c`, and related headers resolve methods and fields. `context.h` and `context.c` own typed register arenas and the call stack. `objectmemorymanager.h` and `.c` implement allocation and the collector. Native test declarations are primarily in `tcvm_test.h`, OMM tests, and `TotalCrossVM/src/tests/tc_tests.c`.

A TotalCross code “slot” is a 4-byte `TCode` unit. A logical instruction is one header slot plus any continuation slots. A “TC PC” is a slot index/pointer into a method. A “home” is a typed `Context` arena slot where a value is stored across effects. A “safepoint” is any point at which runtime collection/suspension may observe generated code; initially this is defined by `may_gc` helpers. A “compiled artifact” is backend code plus versions, identity, ownership, entry address, and diagnostics. “Fallback” means selecting the current interpreter before executing any part of that method invocation.

The current class record is little-endian. Its 22-byte class header is followed by interfaces, grouped fields, and method records. A 16-byte method header describes counts, types, parameters, code-slot count, handlers, and lines. The current runtime has typed register banks rather than an operand stack. Calls can be normal/virtual/native, use compact continuation bytes, and update lazy caches. Exceptions use `Context.thrownException` and handler lookup by TC PC. The GC is non-moving, scans managed roots including the `regO` arenas, and does not scan native stacks.

The analysis documents beside this plan are normative for the current baseline and proposed design. Factual statements about current behavior must be updated when the source changes; proposed statements must remain labeled as such.

## Plan of Work

### Milestone 1: inventory and architecture documentation

This milestone is complete for the current revision. It establishes the evidence required to change the runtime safely. The work traces a simple method from Java class parsing through `J2TC`, stack simulation and register selection, class/constant-pool serialization, loading, and `executeMethod`; enumerates every opcode; documents frames, calls, exceptions, monitors, objects, arrays, layout, roots, and collection; and separates current facts from future architecture.

The produced artifacts are the eight documents under `docs/architecture/bytecode` plus this plan. Acceptance means a reader can follow `static int add(int a, int b)` from Java inputs to TotalCross registers and `ADD_regI_regI_regI`/return execution; every opcode 0–159 appears in the reference and compatibility matrix; referenced paths exist; all new documentation is English; only plans/docs changed; and `git diff --check` passes. No SDK/VM build is required because no executable source changed.

### Milestone 2: make TCIR a versioned, verifiable contract

Create a small C library under `TotalCrossVM/src/tcvm/ir`, tentatively `tcir.h`, `tcir.c`, `tcir_verify.c`, `tcir_dump.c`, and `tcir_opcode_map.c`. The public header declares opaque module/function/block/value structures, type/effect/op/terminator enums, source metadata, diagnostics, and `TC_IR_VERSION`. Keep allocation ownership explicit and return errors rather than exiting. Add a single generated or macro-driven opcode registry that maps all values 0–159 to a decoder classification and planned TCIR/helper status; use it to cross-check `opcodes.h`, runtime dispatch, docs, and tests.

Define the canonical text grammar and deterministic ordering in code and golden fixtures. Implement construction APIs and structural/type verification without executing functions. The verifier checks continuation slots, targets, definitions, block arguments, types, return signature, handler edges, symbol kinds, unchecked-array proofs, helper effects, GC homes, and internal-address lifetime. It reports method identity and TC PC in every diagnostic.

Manually construct `add`, `abs`, and `sumTo` TCIR fixtures plus malformed fixtures for undefined values, mismatched block arguments, invalid terminators, wrong return type, and missing GC homes. Keep a serializer/parser out of the critical path: a stable printer is required; a text parser is optional unless it materially improves fixtures.

Acceptance is observed when the three valid fixtures verify and produce byte-identical text on repeated runs, all invalid fixtures fail with stable diagnostic codes, and the registry test proves that every opcode has one disposition. No interpreter or native code exists yet. Update `totalcross-ir-design.md`, `compatibility-matrix.md`, Progress, Decision Log, and the interim Editorial Report with actual names and results.

### Milestone 3: partial TotalCross bytecode to TCIR frontend

Implement `tcir_decode.c` and `tcir_frontend.c`. Decode the POC opcode subset from runtime `Method` code, marking logical instruction starts and validating every operand before building blocks. Because TotalCross input is already register-based, model the typed bank slots directly, calculate leaders and edges, then promote safe values and create block arguments. Do not simulate a fictitious JVM stack. Preserve TC PC and source line.

The first accepted subset is `BREAK`, `MOV_regI_regI`, `MOV_regI_sym`, `MOV_regI_s18`, `INC_regI`, integer add/subtract/multiply variants that do not access arrays, integer comparison variants, `JUMP_s24`, `DECJGTZ_regI`, `DECJGEZ_regI`, and integer/void return variants. Add only forms emitted by the chosen converter fixtures, then add missing POC forms deliberately. A method containing anything else receives `TCIR_UNSUPPORTED_OPCODE` with numeric opcode, name, and TC PC; malformed continuations, targets, registers, symbols, handler ranges, or type merges receive distinct validation diagnostics.

Generate Java fixtures for `add`, `abs`, and `sumTo` through the existing converter rather than hand-authoring binary slots for integration tests. Keep hand-authored malformed slot arrays for negative tests. Golden tests compare canonical TCIR and CFG edges. Repeated frontend runs must not depend on pointer values or hash iteration.

Acceptance means each POC method generated by the real converter yields the expected deterministic IR and passes the verifier; each negative case fails before code execution; and any method outside the subset remains eligible for the current interpreter. Update the mapping matrix with focused-test evidence rather than marking entire families complete.

### Milestone 4: reference TCIR interpreter and differential oracle

Implement `tcir_interp.c` over verified functions. It executes block/value semantics while using the same `Context` typed frame homes and explicit result/exception status planned for native code. The interpreter is deliberately simple: no optimization, direct access only to operations whose layouts are already stable, and runtime helpers for complex behavior. It asserts verification in test/debug builds and rejects unverified input in release APIs.

Build a differential harness that invokes the existing TotalCross bytecode interpreter and TCIR interpreter with independent fresh state for the same method and inputs. Compare return type/value, pending exception class/message where stable, selected handler behavior, and relevant mutated state. For `add`, `abs`, and `sumTo`, include zero, positive, negative, `INT32_MIN`, `INT32_MAX`, overflow-producing combinations, and loop counts including zero and a bounded high value. Add fixed-seed generated integer cases.

Acceptance means every supported fixture agrees across both interpreters for the deterministic corpus and fixed seed; malformed/unverified IR never executes; unsupported methods remain wholly interpreted; and sanitizer-enabled host tests report no IR ownership or bounds defect where the repository toolchain supports them. Only after this milestone can optimization passes begin, and even then each pass must remain independently toggleable.

### Milestone 5: SLJIT baseline backend

Integrate a pinned SLJIT revision through the native dependency process. Do not edit or commit the generated `TotalCrossVM/deps/totalcross-depot-tools` checkout. If depot-tools lacks an SLJIT package, make the source/dependency change in its owning repository, preserve the Simplified BSD notice, then update `totalcross-depot-tools.ref` in this repository through the established bootstrap flow. Record the exact upstream revision, license path, enabled architectures, and any local configuration.

Add an optional CMake feature such as `TC_ENABLE_SLJIT_JIT`, default off, and a backend under `TotalCrossVM/src/tcvm/jit`. Implement the frame ABI and POC integer/branch/return lowering only. Before emitting code, run the canonical verifier and backend eligibility pass over the entire method. Centralize executable-memory allocation, write/finalize/execute transitions, instruction-cache flush, and disposal; enforce platform W^X. Publish artifacts atomically in a side table and let competing threads interpret while one compiles.

Use runtime/dispatcher thunks for anything outside direct arithmetic/control flow. Initially enable execution tests on Linux x86-64 or the available development host only, while preserving compile-time portability. Add forced-JIT mode so a test expected to compile fails if it falls back. Test concurrent compilation claims, compilation failure cleanup, repeated creation/disposal, and explicit no-execution after verification failure.

Acceptance means `add`, `abs`, and `sumTo` agree among current interpreter, TCIR interpreter, and forced SLJIT for all Milestone 4 inputs; executable memory is not left writable and executable; unsupported functions never produce an entry; artifacts are freed safely at shutdown; and JIT remains silent/off by default. Record platform/toolchain and code-size/compile-time observations as measurements, not performance claims.

### Milestone 6: deterministic portable-C AOT backend

Add an AOT generator, tentatively under `TotalCrossVM/src/tcvm/aot`, plus a host tool entry point. It consumes only verified TCIR and emits portable C, a stable method registry, a manifest, and optional line maps into the build directory. Generated names derive from escaped stable method identities plus content hashes; units, declarations, symbols, includes, and registry order are deterministic. Never write generated C into source directories or commit it as source.

Generated functions implement the same frame ABI and runtime-helper calls as JIT. The manifest records generator, IR and runtime ABI versions, input hash, target options, supported and rejected methods, and diagnostic codes. Registration verifies class/method/signature and content identity before publishing an entry; mismatch falls back. For the POC, compile with the host C toolchain through CMake, link a focused harness or VM target, and run the same corpus. Verify the C also compiles with both GCC/Clang syntax expectations and avoids extensions unsupported by MSVC where applicable.

Acceptance means two clean generations from identical inputs are byte-for-byte equal; the POC C compiles, links, and produces results identical to the two interpreters and SLJIT; a changed input invalidates registration; unsupported IR is reported before C output; and a build with AOT disabled is unchanged. Document root-`TotalCrossVM/CMakeLists.txt` configure/build commands for Linux, Windows, Apple Clang, Android NDK, and iOS toolchains even if only the host is executed in this milestone. Do not add an `Android.mk` or Xcode-project integration path.

### Milestone 7: controlled TCVM mixed-mode integration

Introduce an experimental runtime policy after the standalone engines are correct. Keep `executeMethod` as the facade and compatibility path. Define the exact point where frame reservation, usage-lock ownership, stack-trace state, and compiled dispatch interact. Use the compilation side table initially; changing `TMethod` requires a separate ABI/layout decision supported by measurements.

Support interpreter-to-compiled entry, compiled-to-interpreter dispatcher calls, compiled-to-compiled calls through the dispatcher thunk, and compiled-to-native calls through the existing native bridge. Add static calls only after frame/result/exception/GC tests pass. Direct call patching, inline caches, and devirtualization remain disabled. Before every `may_gc` helper, spill live references to `regO` homes and reload arena bases afterward. Before every potentially throwing effect, publish TC PC and return pending-exception status immediately when set.

Add runtime options for backend off/IR/JIT/AOT/auto, force-method selection in tests, structured fallback reasons, bytecode/CFG/IR/C dumps, compilation timing, code-cache size, and unsupported-opcode counts. All are silent by default and write bounded structured output or dedicated files.

Acceptance means an experimental option compiles only compatible methods while all others use the interpreter; all four call directions preserve parameters, primitive/reference results, exceptions, stack traces, GC roots, arena growth, and usage locks; forced GC at helper boundaries passes; concurrent compilation/dispatch has no partial publication; and disabling the option reproduces the pre-integration path.

### Milestone 8: expand to complete semantic coverage

Expand in risk-ordered slices, updating the opcode registry, compatibility matrix, verifier, text form, TCIR interpreter, both backends, differential tests, and platform results in the same change. The recommended order is: remaining 32-bit operations and conversions; long; double and normalized-float semantics; static calls and native bridge; object allocation and reference returns; fields and class initialization; arrays and checked/unchecked access; exceptions and handlers; virtual/interface calls; monitors; and legacy/special/reflection cases. Calls move earlier than raw object access because helper-based calls validate the ABI, while direct layout manipulation waits for GC/layout proof.

For each opcode choose and record exactly one lowering class: `direct`, `lowered`, `runtime-helper`, `unsupported-in-poc`, `future`, `obsolete`, `platform-specific`, or `needs-investigation`. No opcode may disappear from the matrix. `aru` operations require a checked precondition; `JUMP_regI` requires validated target enumeration or retained fallback; `BREAK` keeps its production/test distinction; monitor forms require ownership/error tests; float/double comparisons require NaN-direction tests.

Coverage is complete only when every valid opcode has a mapping, every runtime interaction and exception edge is documented, GC remains correct, differential tests cover representative and boundary cases, and fallback remains only as an explicit product/platform decision rather than missing implementation. Keep optimizing transformations local and disabled by default until their before/after IR and differential property tests pass.

### Milestone 9: platform, security, performance, and release readiness

Run the backend conformance suite on macOS arm64, Linux x86-64, Linux aarch64, Windows x86 and confirmed x86-64, Android arm64-v8a, Linux armv7 where supported, and iOS arm64 AOT. Record unavailable targets explicitly. Do not enable JIT on iOS. macOS JIT requires Hardened Runtime/`MAP_JIT`/W^X evidence; Android requires device, security, signing, and distribution review. Validate every native configuration, AOT cross-compilation path, and static registration through `TotalCrossVM/CMakeLists.txt` with the relevant CMake generator/toolchain. Packaging or platform wrappers may invoke that build, but no native source/build logic may be added to legacy `Android.mk` or `TCVM.xcodeproj`.

Only after correctness gates pass, run the benchmark protocol in `Validation and Acceptance`. Decide backend defaults using measured startup, compile latency, steady-state execution, memory/code size, product workloads, and platform policy. A backend stays opt-in if evidence is incomplete. Evaluate LLVM and Cranelift only against the objective criteria in `Interfaces and Dependencies`; do not add one merely because the baseline is complete.

Finally, reconcile every Progress item, Decision Log entry, discovery, validation result, benchmark sample, limitation, and deferred platform into `Outcomes & Retrospective` and the final `Editorial Report`. Acceptance requires all claims to point to repository paths, test commands, measured result files, or external primary policy sources, with human review items named.

## Concrete Steps

All commands below run from `/Users/flsobral/repos/totalcross-github` unless a command starts with `cd`. Future agents should use the equivalent checkout path rather than hard-coding this user's path in source or scripts.

Reproduce the initial source and opcode inventory:

    test -f .agent/PLANS.md
    test -f AGENTS.md
    test -f TotalCrossSDK/src/main/java/tc/tools/converter/J2TC.java
    test -f TotalCrossSDK/src/main/java/tc/tools/converter/Bytecode2TCCode.java
    test -f TotalCrossVM/src/tcvm/tcvm.c
    test -f TotalCrossVM/src/tcvm/objectmemorymanager.c
    rg --files TotalCrossSDK/src/main/java/tc/tools/converter TotalCrossVM/src/tcvm | sort
    awk '/#define [A-Z][A-Za-z0-9_]*[[:space:]]+[0-9]+/ {print $3 "\t" $2}' TotalCrossVM/src/tcvm/opcodes.h
    rg -n 'OPCODE\(' TotalCrossVM/src/tcvm/tcvm.c
    rg -n 'public static final int [A-Z]' TotalCrossSDK/src/main/java/tc/tools/converter/TCConstants.java

Before each implementation milestone, inspect only scoped local changes and preserve unrelated work:

    git status --short -- plans docs/architecture/bytecode TotalCrossVM/src/tcvm TotalCrossVM/src/tests TotalCrossVM/CMakeLists.txt TotalCrossVM/deps/totalcross-depot-tools.ref
    git diff --stat -- plans docs/architecture/bytecode TotalCrossVM/src/tcvm TotalCrossVM/src/tests TotalCrossVM/CMakeLists.txt

Milestone 2 should first add the IR library and focused native tests, then expose it to CMake without enabling a backend. The exact test command must follow the repository's native test harness discovered at implementation time. At minimum configure a focused host build without cleaning unrelated artifacts:

    cmake -S TotalCrossVM -B build-ir -DCMAKE_BUILD_TYPE=Debug -G Ninja -DTC_BUILD_IR_TESTS=ON
    ninja -C build-ir

`TC_BUILD_IR_TESTS` is a proposed focused-test option to add in Milestone 2, not a current repository option. If implementation chooses a different name, update this command first. The existing `ENABLE_TEST_SUITE` preprocessor macro is not currently exposed as an equivalent CMake option, so passing `-DENABLE_TEST_SUITE=ON` to CMake would not prove that tests were built. If the current CMake configuration has no runnable focused native-test target, add one as part of Milestone 2 rather than claiming tests ran. Record the target and command in this plan. Use ASan/UBSan in a separate host build where supported; do not impose unsupported sanitizer flags on all platforms.

Generate POC converter inputs with the smallest SDK test/task that is available. Use `TotalCrossSDK/gradlew-agent`, keep full Gradle output in its log, and avoid `clean` unless stale artifacts are proven. Example shape, to be replaced by the actual focused test name created in Milestone 3:

    cd TotalCrossSDK
    ./gradlew-agent test --tests 'tc.tools.converter.ir.*'

For SLJIT, fetch dependencies through the documented bootstrap and pinned ref only after the owning dependency source is ready:

    TotalCrossVM/deps/fetch-depot-tools.sh
    cmake -S TotalCrossVM -B build-jit -G Ninja -DCMAKE_BUILD_TYPE=Debug -DTC_ENABLE_SLJIT_JIT=ON -DENABLE_TEST_SUITE=ON
    ninja -C build-jit

Do not log authenticated URLs or tokens. Save verbose configure/build/test output to a temporary log and show only the status, relevant errors, and a short tail.

For AOT, the milestone must provide an explicit tool command. Its expected shape is:

    build-aot/tools/tcaot --input <fixture.tcz> --output <build-directory> --manifest <manifest.json>
    cmake --build build-aot --target tcvm_aot_fixture
    ctest --test-dir build-aot -R 'tcir|jit|aot' --output-on-failure

These names are placeholders until implemented; update the plan immediately when real target names exist. Never report placeholder commands as executed.

At every milestone boundary run:

    git diff --check
    git status --short -- <only-the-milestone-paths>

Run broader SDK/VM builds only when the milestone changes their runtime, build, native integration, or packaging behavior. Follow the repository's validation escalation order and quiet-log policy. Do not run `clean` by default.

## Validation and Acceptance

Documentation/inventory validation extracts opcode numbers from `opcodes.h`, dispatch occurrences from `tcvm.c`, Java constants from `TCConstants.java`, and names/matrix entries from the docs. A purpose-built script added in Milestone 2 must fail on a missing number, duplicate number/name, dispatch mismatch, undocumented opcode, or unclassified matrix entry. It must explicitly expose the current name-table discrepancy for 158/159 until that source is corrected in a separately scoped change.

Frontend validation uses canonical golden TCIR, expected CFG edges, type and merge checks, continuation-slot and target checks, and stable diagnostic codes. Negative cases include unsupported opcode, incompatible type/bank, undefined slot/value, invalid branch offset, target into continuation data, incompatible merge, malformed call/switch payload, invalid handler, wrong return type, and missing GC root at a `may_gc` effect.

Differential validation runs the current interpreter, TCIR interpreter, SLJIT, and AOT on independent but equivalent state and compares return values, exceptions, mutated fields/arrays, monitor outcome, and stack/source metadata appropriate to the supported operation. Fixed-seed property tests generate inputs only after curated boundary cases pass. Forced-backend mode prevents silent fallback from producing a false pass.

GC validation forces collection at every helper declared `may_gc`, exercises all live-reference home positions, grows each `Context` arena, crosses every interpreted/compiled/native call direction, returns/throws otherwise-dead objects, covers arrays/static/instance roots and weak references, and stresses concurrent compilation publication. Native pointers or interior object addresses across those points are test failures.

Platform validation begins on the development host but the design acceptance matrix is macOS arm64, Linux x86-64, Windows x86-64 plus the repository's current Windows x86 target, Android arm64, Linux arm32, and iOS arm64 AOT. Each result records OS/toolchain/architecture, build flags, backend status, command, and pass/fail/skipped reason. “SLJIT supports the CPU” is not evidence that executable JIT is permitted or correctly configured on that platform.

Performance is never inferred from a single run. Before benchmarking, record repository revision, dirty paths, benchmark source/input, backend and flags, OS/kernel, CPU model/count, RAM, power mode, compiler and version, build type, warm-up policy, affinity policy where applicable, and background-load notes. Use a dedicated result file under a build/results directory, not source. Run the unmodified interpreter baseline and changed backend in alternating order when possible. Use at least 10 measured samples after at least 3 warm-ups; increase to 30 or more when variance is high or the difference is small. Record every raw sample, never only a best result.

For each metric—TCIR translation time, JIT compile time, generated code bytes, execution time, interpreted/compiled transition cost, code-cache memory, generated C bytes, object/library bytes—report units, each before sample, each after sample, arithmetic mean before, arithmetic mean after, absolute difference, percentage difference, standard deviation, median, and sample count. State whether lower or higher is better. For compile-only metrics with no interpreter equivalent, label the baseline “not applicable” rather than inventing zero. Preserve failures/outliers and explain exclusion before calculating an additional filtered statistic. A benchmark is valid only after identical-result correctness checks pass in the same build configuration.

The initial POC is accepted only when `add`, `abs`, and `sumTo` produce identical results for curated and fixed-seed inputs through current interpreter, TCIR interpreter, forced SLJIT, and compiled generated C; negative methods reject deterministically; unsupported methods remain wholly interpreted; and no source format or default VM behavior changes. Full completion additionally requires every valid opcode to have an explicit tested lowering/decision, all runtime effects and exception edges documented, GC stress passing, mixed mode passing, platform gates recorded, fallback retained only by decision, final performance evidence recorded, and the Editorial Report finalized.

## Risks and Mitigations

Semantic drift is the largest risk because current opcodes combine compact encodings, implicit checks, lazy resolution, and runtime effects. Mitigate it with an IR interpreter, effect declarations, precise TC PC metadata, curated edge tests, and method-atomic differential comparisons before optimization.

GC unsafety can produce rare corruption if native registers hide references. Mitigate it by using `Context.regO` homes for every live reference at `may_gc` points, prohibiting interior pointers across safepoints, forcing GC in tests, and postponing native stack maps.

Exception drift can select the wrong handler or duplicate effects. Mitigate it by publishing TC PC before throwing effects, reusing `Context.thrownException`, returning explicit status, and forbidding post-effect fallback/re-execution.

Executable-memory and distribution rules vary by platform. Mitigate them with a centralized W^X allocator, feature flags off by default, platform/security review, JIT-off iOS policy, and AOT parity.

Dependency and license changes can make builds non-reproducible. Mitigate them by pinning SLJIT through the established depot-tools flow, carrying its Simplified BSD notice, avoiding network fetches during normal configure, and recording the exact revision.

The compact bytecode format and C bitfields can conceal portability bugs. Mitigate them with byte-level fixtures, masks/shifts in the new decoder rather than unchecked struct aliasing where feasible, cross-endian reasoning, and target CI. Do not redesign the shipping format in the same change.

Compilation races and stale entries can call freed code. Mitigate them with a side-table state machine, atomic publication, reference/lifecycle ownership, shutdown quiescence tests, and no direct call patching in the baseline.

Generated C can become toolchain-specific or nondeterministic. Mitigate it with a restricted C dialect, stable sorting/names/hashes, byte-for-byte regeneration tests, GCC/Clang/MSVC compilation checks, manifests, and build-directory-only output.

Scope can expand into an optimizer before semantics are stable. Mitigate it with milestone acceptance gates, a small POC, per-family coverage updates, disabled local passes, and objective criteria for any LLVM/Cranelift evaluation.

## Idempotence and Recovery

Documentation and generation steps must be repeatable. Canonical TCIR and AOT output use stable identities and sorted traversal, so rerunning them should either leave files unchanged or reproduce identical build artifacts. CMake build directories are disposable but must be named narrowly (`build-ir`, `build-jit`, `build-aot`) and never confused with repository source.

Every experimental feature is compiled and selected through an off-by-default flag. A failed verifier/backend compilation records a bounded reason and returns to the interpreter before execution. A failed AOT identity match does the same. This is the primary runtime recovery path.

At implementation boundaries, preserve unrelated dirty work and inspect scoped diffs. Do not use `git reset --hard`, `git checkout --`, or broad removal. Python caches produced by tests may be removed under repository rules; downloaded native artifacts and depot-tools `local` directories must not be deleted merely to clean status.

If SLJIT dependency provisioning fails, leave the runtime buildable with the feature disabled, record the exact external prerequisite, and continue frontend/IR-interpreter/AOT work that does not require it. Never patch the generated depot-tools checkout as the authoritative fix.

If generated code crashes, first reproduce with TCIR interpreter and forced backend on the smallest fixture, disable execution by default, preserve IR/C/native dump and diagnostic artifacts, and inspect precise TC PC. Do not work around the crash by allowing mid-method fallback.

If a milestone changes a public/runtime layout unexpectedly, stop before merging that change, document the dependency and rollback, and prefer the side table/helper ABI. Each milestone should be independently revertible without changing TCZ compatibility.

## Artifacts and Notes

The analysis stage produced:

    .agent/exec-plan-totalcross-ir-jit-aot.md
    docs/architecture/bytecode/totalcross-class-format.md
    docs/architecture/bytecode/java-to-totalcross-bytecode.md
    docs/architecture/bytecode/totalcross-bytecode-reference.md
    docs/architecture/bytecode/execute-method-interpreter.md
    docs/architecture/bytecode/memory-management-gc.md
    docs/architecture/bytecode/totalcross-ir-design.md
    docs/architecture/bytecode/jit-aot-architecture.md
    docs/architecture/bytecode/compatibility-matrix.md

Milestone 2 is expected to add the TCIR core/verifier/dumper/opcode registry and focused native fixtures/tests. Milestone 3 adds the bytecode decoder/frontend and converter-generated golden fixtures. Milestone 4 adds the TCIR interpreter/differential harness. Milestone 5 adds the optional SLJIT backend, dependency pin/license, executable-memory abstraction, and JIT tests. Milestone 6 adds the C generator, tool, manifest/registry schema, CMake integration, and reproducibility tests. Milestone 7 adds experimental dispatcher policy, runtime thunks, diagnostics, and mixed-mode/GC tests. Milestone 8 grows those artifacts without introducing a second competing IR. Milestone 9 adds recorded platform/benchmark result artifacts and final editorial evidence.

Do not commit generated dependency checkouts, native archives, build directories, generated C, local logs, or benchmark binaries. Small canonical golden text/manifest fixtures may be source artifacts when they are deterministic and reviewed.

External factual references used by the architecture stage are the official [SLJIT repository](https://github.com/zherczeg/sljit), its [Simplified BSD license](https://github.com/zherczeg/sljit/blob/master/LICENSE), Apple's [JIT guidance for Apple Silicon](https://developer.apple.com/documentation/apple-silicon/porting-just-in-time-compilers-to-apple-silicon), [App Review software requirements](https://developer.apple.com/app-store/review/guidelines/#software-requirements), the [LLVM backend guide](https://llvm.org/docs/WritingAnLLVMBackend.html), [LLVM CMake target configuration](https://llvm.org/docs/CMake.html#llvm-specific-variables), and the current [Cranelift ISA](https://docs.rs/cranelift-codegen/latest/cranelift_codegen/isa/) and [x86-64 backend](https://docs.rs/cranelift-codegen/latest/src/cranelift_codegen/isa/x64/mod.rs.html) documentation. Recheck current primary sources when platform policy or an optimizing tier is implemented because these facts can change.

## Interfaces and Dependencies

The required TCIR API is an owned, opaque C interface. It needs builders and destructors; decoder/frontend entry; structural verifier; stable diagnostic code/message/TC-PC access; canonical dump; block/value/type/effect iteration for backends; and `TC_IR_VERSION`. Concrete struct fields stay private so a backend cannot couple to allocation layout.

The runtime ABI must define `TC_RUNTIME_ABI_VERSION`, `TCCompiledFrame`, `TCCompiledStatus`, stable symbol identities, result transfer, precise TC PC publication, and a helper table. Each helper declares `may_throw`, `may_gc`, `may_lock`, heap read/write, and symbol-resolution effects. The baseline invocation concept is:

    TCCompiledStatus tcCompiledInvoke(
        const TCCompiledArtifact *artifact,
        Context context,
        Method method,
        TCCompiledFrame *frame,
        TValue *result);

The frame describes typed arena bases/counts, return destination, current TC PC, and call metadata. It aliases memory reserved through `Context` and must reload bases after helpers that may grow arenas. The existing variadic `executeMethod` remains an external facade, not the generated-code ABI.

The backend interface accepts only verified `TCIRFunction` plus options and returns an owned artifact or structured failure. It exposes eligibility, compile, invoke/registration metadata, disposal, backend/version/target identity, code size, and diagnostics. SLJIT and C generation implement this interface without adding backend-specific concepts to TCIR.

SLJIT is the only required JIT dependency in the baseline and remains optional at build time. It must be pinned, reproducibly acquired, and license-noticed. Generated C depends on the official platform toolchains: GCC or Clang on Linux, MSVC or Clang on Windows, Apple Clang/Xcode on macOS/iOS, and Android NDK Clang on Android. All of them are configured from `TotalCrossVM/CMakeLists.txt`; the native build never branches into `Android.mk` or a maintained `TCVM.xcodeproj`. AOT C must avoid requiring LLVM APIs.

An LLVM or Cranelift backend may be proposed only after the backend conformance suite is independent of SLJIT; all selected operations have differential tests; GC and exception ABI are stable; and the evaluation records binary/distribution size, build time, compile latency, target coverage including 32-bit requirements, licensing, debug/profiler integration, stack-map support, and product workload benefit. Current official LLVM documentation exposes a broad configurable target list including native 32-bit families but at significant C++ dependency/build scope. Current Cranelift documentation exposes native x86-64, Arm64, s390x, and RISC-V 64 backends and adds a Rust/crate dependency; its x86-64 backend rejects a 32-bit pointer ABI, so it cannot presently replace TotalCross's native 32-bit coverage. Both facts must be reverified at evaluation time. Neither tier is part of initial acceptance.

## Open Questions Requiring Deeper Inspection

These questions must be answered with source traces, targeted instrumentation, or representative application artifacts during the named milestone; they should not be answered by assumption.

Which runtime helpers can directly or transitively allocate or trigger GC on every platform, including native methods, class initialization, resolution, monitors, string operations, and exception creation? Milestones 4 and 7 need a reviewed helper-effect registry.

How does the current thread suspension/GC coordination behave while a context is inside arbitrary native code, and does safe mixed-mode execution require entry/backedge polls beyond helper safepoints? Milestone 7 needs a concurrency trace and stress test.

Are `volatile`, atomic, memory-ordering, or platform-specific field semantics represented outside the opcode names and direct interpreter cases? Milestone 8 must inspect field metadata, native helpers, and application expectations before direct field lowering.

Can classes or methods be unloaded, replaced, or redefined in any supported runtime mode, and what lifetime guarantees apply to constant-pool lazy bindings and `Method` pointers? Milestones 5 and 7 need this before persistent entries or direct call patching.

Which legacy applications still emit `JUMP_regI`/`jsr`/`ret`, and can every valid pattern be statically enumerated into CFG edges? Milestone 8 needs a representative artifact corpus before compiling it.

Does any external/native consumer depend on the binary layout of `Context`, `TMethod`, or the call stack, constraining a future shadow stack or embedded compiled-entry fields? Milestone 7 must inventory exported headers and native SDK usage.

What exact semantics distinguish the two symbol-based monitor opcodes and what converter paths emit them, given the incomplete Java text-name table? Milestone 8 needs focused conversion/runtime tests.

Which Windows x86-64 and embedded targets are product-supported rather than aspirational, and what compiler, calling convention, executable-memory, and CI runners apply? Milestone 9 requires maintainer/product confirmation.

What iOS AOT registration/linking model best fits the root `TotalCrossVM/CMakeLists.txt` target graph, static initialization, dead stripping, and code-signing flow when configured with the supported iOS CMake toolchain/generator? Milestone 6 must answer this entirely in CMake and must not inspect or patch legacy `TCVM.xcodeproj`.

Revision note (2026-07-17): created the initial self-contained plan after source analysis, changed all repository documentation to English, and made root `TotalCrossVM/CMakeLists.txt` the only native-build integration point while excluding legacy `TCVM.xcodeproj` and `Android.mk`, following the user's explicit instructions. The implementation milestones remain unstarted.
