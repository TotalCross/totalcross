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
- [x] (2026-07-17T16:40:39Z) Implemented the owned TCIR version 1 C contract, canonical printer, structural/type verifier, and backend-facing read-only accessors under `TotalCrossVM/src/tcvm/ir`.
- [x] (2026-07-17T16:40:39Z) Added the 160-row macro registry and repository-wide source cross-check, preserving the `bcTClassNames` 158/159 discrepancy as an explicit notice.
- [x] (2026-07-17T16:40:39Z) Added valid `add`, `abs`, and `sumTo` golden fixtures plus ten stable negative verifier cases and the optional `TC_BUILD_IR_TESTS`/`check-tcir` CMake path.
- [x] (2026-07-17T16:40:39Z) Validated Milestone 2 on macOS arm64 with strict warnings, Clang static analysis, normal CTest, and a separate ASan/UBSan build; committed the main implementation as `96c17be4b` and the completed metadata/non-null contract as `a3a5e33fa`.
- [x] (2026-07-17T16:40:39Z) Milestone 2: turned the TCIR proposal into versioned C interfaces, an opcode mapping registry, a structural verifier, and canonical text golden fixtures.
- [x] (2026-07-17T17:28:38Z) Implemented the bounded logical-slot decoder and register-to-TCIR frontend for the exact POC opcode subset, with structured pre-construction diagnostics and method-atomic fallback.
- [x] (2026-07-17T17:28:38Z) Added production-converter fixtures for `add`, `abs`, and `sumTo`, canonical frontend goldens, repeated-build determinism checks, and malformed metadata/payload cases.
- [x] (2026-07-17T17:28:38Z) Validated Milestone 3 on macOS arm64 with the focused SDK converter test, normal and ASan/UBSan CTest, strict C99 warnings, and Clang static analysis; committed the implementation as `0fa51be08` and converter-backed evidence as `f0e241b11`.
- [x] (2026-07-17T17:28:38Z) Milestone 3: implemented the partial TotalCross-bytecode-to-TCIR frontend for the proof-of-concept subset.
- [x] (2026-07-17T18:20:41Z) Implemented the bounded verified TCIR reference interpreter with typed homes, defined i32 wrap semantics, explicit outcomes, step limits, and pre-execution rejection in `d5ebceb43`.
- [x] (2026-07-17T18:20:41Z) Added a fresh-state differential harness linked to the real `executeMethod`, covering 1,179 curated/fixed-seed comparisons and method-atomic fallback in `801ae507b`.
- [x] (2026-07-17T18:20:41Z) Validated Milestone 4 on macOS arm64 with normal and Release 3/3 CTest, TCIR ASan/UBSan, differential ASan, strict C99, Clang static analysis, and the focused SDK converter test.
- [x] (2026-07-17T18:20:41Z) Milestone 4: implemented the TCIR reference interpreter and proved differential equivalence with `executeMethod` for the three proof-of-concept fixtures.
- [x] (2026-07-17T21:34:53Z) Pinned depot-tools tag `sljit-20260717`, added the default-off `TC_ENABLE_SLJIT_JIT` root-CMake integration, and made Android arm64-v8a fetch and compile the prebuilt SLJIT package.
- [x] (2026-07-17T21:34:53Z) Implemented the verified whole-method SLJIT backend, compiled-frame ABI, centralized W^X finalization/disposal, synchronized artifact side table, and forced backend tests in commits `d7d4ad64a` and `8a5ae42d6`.
- [x] (2026-07-17T21:34:53Z) Validated Milestone 5 on macOS arm64 with normal and Release 4/4 CTest, forced 1,179-case three-way differential execution, ASan/UBSan, Clang static analysis, default-off checks, and an Android arm64-v8a NDK r28c/API 23 native build.
- [x] (2026-07-17T21:34:53Z) Milestone 5: integrated pinned SLJIT and implemented the opt-in baseline JIT subset without adding production runtime selection.
- [x] (2026-07-17T22:20:54Z) Added the off-by-default `TC_BUILD_IR_BENCHMARKS` harness and revision-keyed JSON/CSV validator in `e49acf6a5`, with alternating execution order, oracle checksums, raw samples, descriptive statistics, and host/build metadata.
- [x] (2026-07-17T22:32:21Z) Expanded the aggregate benchmark in `77d179edc` to require sequential 60-, 200-, and 1,000-sample profiles with 5, 10, and 20 warmups, respectively, and profile-specific artifact validation.
- [x] (2026-07-17T22:32:21Z) Captured and validated all three post-Milestone-5 macOS arm64 performance profiles under `build/m5-sljit-benchmark/results`, preserving six raw artifacts and recording their hashes and interpretation below for future comparisons.
- [x] (2026-07-17T23:09:05Z) Extracted the versioned compiled-frame contract into backend-neutral `tcir_compiled.h`, then implemented verified deterministic C generation, exact-identity registry lookup, structured manifest output, and the native `tcaot` host tool in commits `8d738ff66` and `1aa428b74`.
- [x] (2026-07-17T23:09:05Z) Added default-off `TC_ENABLE_C_AOT`, build-directory-only generated sources, `tcvm_aot_fixture`, byte-for-byte clean regeneration, independent manifest validation, unsupported-input rejection, identity invalidation, and 1,179-case four-way differential execution.
- [x] (2026-07-17T23:09:05Z) Validated Milestone 6 on macOS arm64 in Debug and Release, AOT-only and default-off configurations, ASan and focused Clang analysis, plus Android arm64-v8a/API 23 and iOS arm64 generated-C compilation; Linux, Windows/MSVC, full iOS linkage, and device execution remain recorded gaps.
- [x] (2026-07-17T23:09:05Z) Repeated the mandatory 60-, 200-, and 1,000-sample macOS benchmark matrix at revision `1aa428b740124b33830c157eadf08deb5660ea69`, preserving the historical `executeMethod`/TCIR/SLJIT scope and all six validated artifacts for later comparison.
- [x] (2026-07-17T23:09:05Z) Milestone 6: implemented the deterministic portable-C AOT proof of concept without production runtime selection or source-tree generated artifacts.
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

The root CMake configuration resolves the full VM dependency graph even when only the focused `check-tcir` target is requested. The first Milestone 2 configure populated ignored `TotalCrossVM/deps/totalcross-depot-tools/*/local` artifacts before building the independent TCIR target. This does not add TCIR dependencies, but it explains why subsequent focused configurations are much faster and why those generated artifacts must remain uncommitted.

A one-way canonical printer is sufficient for stable Milestone 2 fixtures. The committed golden files carry repository copyright comments that the test loader removes before byte-for-byte comparison with printer output; no parser or text-as-cache promise was needed.

`TMethod` does not retain the serialized `opcodeCount`: `tcclass.c` uses that count to allocate and read `m->code`, then discards it. A decoder that accepts only `Method` therefore cannot prove code bounds. Milestone 3 exposes a non-owning `TCIRMethodView` carrying an explicit slot count, typed homes, parameter-home mapping, constants, source lines, handlers, and an optional call-shape resolver. A later runtime adapter must preserve or recover these inputs without changing the shipping class format by accident.

Call continuation width is not present in the call header. It depends on the referenced method's parameter count and whether it returns a value; the production runtime similarly derives `paramSkip` from the resolved method. The frontend view therefore resolves call shape explicitly even though calls remain interpreter fallback in this POC. `SWITCH` and `NEWARRAY_multi` widths are self-describing and are still decoded so targets cannot enter their payloads.

The converter-generated `abs` and `sumTo` methods contain opcode `BREAK` as branch-layout padding. Production `executeMethod` treats it as a no-op, so the frontend retains it as a logical instruction boundary but emits no semantic TCIR operation. This was not visible in the hand-built Milestone 2 functions.

The published depot-tools SLJIT package is upstream revision `3907e69005ba6e30b225000f24aaef3632f88347`, distributed by tag `sljit-20260717`. Each artifact contains the Simplified BSD license, and its per-target manifest records the W^X allocator and argument checks; the Android package was built for arm64-v8a with NDK `28.2.13676358` and API 23. The same archive layout works through the existing root-CMake imported-target flow without editing the generated dependency checkout.

On the macOS arm64 development host, SLJIT's W^X allocator finalized executable mappings that were executable and non-writable when inspected through `mach_vm_region`. This is host test evidence only: it does not establish Hardened Runtime signing/entitlement readiness for distributed applications. Android cross-compilation likewise proves build compatibility, not device execution or distribution-policy approval.

Short standalone methods expose transition costs more strongly than generated-code throughput. Both `tcirInterpretFunction` and `tcirJitInvoke` currently allocate invocation scratch, and the TCIR interpreter also verifies and preflights each call; `add` and `abs` therefore remained slower than `executeMethod` in the structured checkpoints. `sumTo(65537)` amortized those costs and made the generated loop observable. The first 30-sample attempt exposed scheduler variance in millisecond-scale batches, so the accepted protocol grew to mandatory 60-, 200-, and 1,000-sample profiles with larger batches rather than filtering outliers. Since 200 and 1,000 are not divisible by six, exact equality among the six execution-order counts is impossible; deterministic round-robin scheduling keeps the difference to at most one.

The first unsupported-AOT negative fixture failed in the canonical verifier before reaching backend eligibility: its synthetic `SWITCH` used source targets and cross-block values without the required slots and block arguments. Repairing the fixture instead of weakening verification preserved the rule that every backend receives only structurally valid TCIR, including for eligibility-rejection tests.

The portable-C generator needs a stable content identity without making a security claim. Milestone 6 uses a deterministic 64-bit FNV-1a digest over semantic TCIR and stable identities for registry matching and regeneration; it is deliberately not an integrity signature or trust boundary. The initial `tcaot` input adapter is the canonical converter-backed POC fixture set, not a general TCZ reader. Production class input, publication, dead-strip-safe registration, and runtime dispatch therefore remain Milestone 7 work.

A broad `scan-build` of the full fixture dependency graph reported 119 findings in pre-existing VM/runtime sources such as `dlmalloc`. A focused scan of the new generator, tool-facing library, generated target, and AOT tests reported no bugs. The focused result is the Milestone 6 evidence; the broad legacy findings were not reclassified as AOT defects or silently discarded.

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
- Decision: Implement TCIR version 1 as an owned C99 library with opaque public structures, optional allocator callbacks, copied caller arrays, explicit destruction, and read-only iteration APIs. Rationale: this makes ownership and backend boundaries testable without exposing allocation layout or depending on the VM runtime. Date: 2026-07-17.
- Decision: Keep builders mechanically permissive for cross-function values and malformed terminators, then make `tcirVerifyFunction` the single mandatory structural/type rejection boundary. Rationale: negative fixtures must prove stable verifier diagnostics rather than being short-circuited by inconsistent builder checks. Date: 2026-07-17.
- Decision: Give the opcode registry separate decoder-shape, lowering-class, and current-POC-status fields. Rationale: encoding shape, eventual implementation strategy, and present support are different facts and must not be collapsed into one ambiguous status. Date: 2026-07-17.
- Decision: Require a deterministic printer but omit a TCIR text parser in Milestone 2. Rationale: one-way golden comparison proves stable diagnostics/output without accidentally creating a new shipping or cache format. Date: 2026-07-17.
- Decision: Expose focused native validation as the optional root-CMake flag `TC_BUILD_IR_TESTS` and target `check-tcir`. Rationale: TCIR tests need a real runnable target while normal VM behavior and backend selection remain unchanged. Date: 2026-07-17.
- Decision: Decode through `TCIRMethodView` rather than directly through `TMethod` in Milestone 3. Rationale: the runtime structure lacks a retained code-slot count, so a direct adapter could not perform the required bounds checks; the view makes every required bound and metadata source explicit without changing runtime ABI yet. Date: 2026-07-17.
- Decision: Decode 32-bit slots with masks and shifts and structurally size all continuation classes before checking POC eligibility. Rationale: this avoids unchecked bitfield aliasing, diagnoses truncated payloads, and prevents a supported branch from targeting continuation data even when the containing opcode will ultimately fall back. Date: 2026-07-17.
- Decision: Thread every POC i32 home through block arguments at non-entry blocks, seeding entry homes from explicit parameter mappings and converter definitions. Rationale: it is deterministic simplified SSA over register bytecode, handles loops without a JVM stack simulation, and leaves liveness-based argument pruning as an optimization rather than a correctness prerequisite. Date: 2026-07-17.
- Decision: Make the SDK test the reproducible authority for native fixture words and source lines. Rationale: compiling Java and running the production `J2TC` converter prevents hand-authored integration encodings from drifting, while malformed slot arrays remain appropriate for decoder rejection tests. Date: 2026-07-17.
- Decision: Make the reference-interpreter frame mirror the three typed `Context` banks without directly depending on the private VM `Context` structure. Rationale: TCIR remains a standalone C99 library, while a later runtime adapter can point the same frame contract at real context arenas. Date: 2026-07-17.
- Decision: Verify and preflight the entire TCIR function on every reference-interpreter entry before mutating the frame. Rationale: malformed and unsupported functions must never partially execute, and the checked release path must not depend on debug assertions. Date: 2026-07-17.
- Decision: Link the differential harness to the already-built `tcvm` artifact instead of the CMake target's legacy PUBLIC source interface. Rationale: consuming that interface recompiles the complete VM source list into the test executable, while the artifact gives the harness the real `executeMethod` implementation without duplicate compilation. Date: 2026-07-17.
- Decision: Keep `TC_ENABLE_SLJIT_JIT` off by default in root CMake while enabling compilation in the current Android arm64-v8a Gradle build without adding runtime selection. Rationale: this satisfies cross-compilation coverage while Milestone 7 still owns dispatcher policy and production activation. Date: 2026-07-17.
- Decision: Use SLJIT's configured W^X allocator behind `tcir_jit_memory.c` as the sole executable-code finalization/disposal boundary. Rationale: SLJIT owns platform transitions and instruction-cache synchronization, while the TCIR backend enforces allocator configuration and exposes one auditable ownership path. Date: 2026-07-17.
- Decision: Publish artifacts through a mutex-protected side-table state machine and keep compilation claims alive across shutdown until their owner rejects or attempts publication. Rationale: competing callers can continue interpreting on `COMPILING`, no partial entry is visible, and shutdown cannot free storage still referenced by a compilation claim. Date: 2026-07-17.
- Decision: Keep performance harnesses behind `TC_BUILD_IR_BENCHMARKS`, independent from `check-tcir`, and require an optimized Release build with SLJIT. Rationale: performance runs must not affect normal builds or correctness tests, and sanitizer/debug numbers must not be mistaken for an optimized baseline. Date: 2026-07-17.
- Decision: Store raw benchmark JSON/CSV under the ignored build directory with filenames keyed by repository revision, then record paths, hashes, protocol, and summary in this living plan. Rationale: future revisions can coexist for direct comparison without committing machine-specific measurements as source or overwriting the current checkpoint. Date: 2026-07-17.
- Decision: Treat the current benchmark as standalone-API evidence, including current per-invocation verifier and scratch-allocation costs. Rationale: production dispatcher/frame management arrives in Milestone 7, so removing those costs from this baseline would create a synthetic number that future runtime comparisons could not interpret honestly. Date: 2026-07-17.
- Decision: Make the aggregate performance target run the 60-, 200-, and 1,000-sample profiles sequentially, with 5, 10, and 20 warmups, and require all six artifacts to validate. Rationale: every future checkpoint must retain both the inexpensive signal and the larger distributions, while sequential execution prevents profiles from competing for host resources. Date: 2026-07-17.
- Decision: Move `TCCompiledFrame`, result/status types, entry signature, and `TC_RUNTIME_ABI_VERSION` into `tcir_compiled.h`. Rationale: SLJIT and generated C must share one backend-neutral ABI rather than making AOT depend on the JIT interface. Date: 2026-07-17.
- Decision: Keep `TC_ENABLE_C_AOT` off by default and generate all C/header/manifest files below the selected build directory. Rationale: the proof of concept must not affect ordinary builds or turn derived platform code into maintained source. Date: 2026-07-17.
- Decision: Emit restricted C11 as a deterministic state machine with lexically sorted registry entries and content-derived escaped symbols. Rationale: this preserves CFG semantics without compiler extensions and makes clean regeneration byte-identical across input order. Date: 2026-07-17.
- Decision: Use the exact class, method, signature, and semantic-content hash tuple for AOT lookup; treat FNV-1a only as deterministic identity, not cryptographic integrity. Rationale: changed input must fail closed while artifact trust/signing remains a separate production concern. Date: 2026-07-17.
- Decision: Limit the Milestone 6 host tool to the canonical POC fixtures and keep static registration standalone. Rationale: a general TCZ/runtime adapter and dead-strip-safe publication would cross the Milestone 7 production-dispatch boundary. Date: 2026-07-17.
- Decision: Repeat the existing three-way performance matrix unchanged after Milestone 6 and test AOT performance only in a future explicitly versioned regime. Rationale: adding AOT would change ordering and workload semantics, making the historical M5/M6 checkpoint incomparable; four-way AOT correctness is established separately. Date: 2026-07-17.

## Outcomes & Retrospective

The completed analysis stage produced a traceable description of the existing format and runtime plus a proposed TCIR/JIT/AOT architecture. It did not implement a compiler, alter VM behavior, edit analyzed source, or produce performance claims. The most consequential result is the GC/ABI choice: compiled frames can initially reuse the same typed `Context` arenas, avoiding an unproven native-stack scanner.

The source inventory also corrected two potentially dangerous assumptions before implementation: the runtime input is register bytecode rather than a JVM stack machine, and the package already named `converter.ir` is target-shaped rather than the independent IR. The opcode inventory found the missing final monitor names in one Java text table, which justifies an automated single-source coverage check in Milestone 2.

Milestone 2 delivered a standalone versioned contract without changing `executeMethod`, TCZ data, or runtime dispatch. The owned graph and verifier now reject structural/type errors before execution can exist, and canonical dumps for `add`, `abs`, and `sumTo` are byte-identical across repeated runs. The three-field registry gives every opcode 0–159 one decoder shape, lowering disposition, and POC decision, while the source validator proves agreement with `opcodes.h`, Java numeric constants, runtime dispatch, and both architecture inventories.

The focused normal and ASan/UBSan builds each passed both CTest entries on macOS arm64. These results establish memory-safe ownership for the exercised construction/destruction paths and deterministic verification; they do not establish frontend correctness, interpreter equivalence, backend performance, or non-host platform support.

Milestone 3 added a bounded frontend without changing `executeMethod`, TCZ serialization, `TMethod`, or default dispatch. The decoder classifies all logical slot widths before block construction, validates the implemented POC operands and control flow, and returns stable fallback for unsupported methods. The frontend translates the converter's register homes into immutable values and block arguments, preserves TC PC/source line metadata, and verifies the result before returning it. Production-converter `add`, `abs`, and `sumTo` inputs now match canonical CFG/TCIR goldens across repeated frontend runs.

The normal and ASan/UBSan focused builds passed 2/2 CTest entries on macOS arm64, with leak detection explicitly disabled because Apple ASan reports it unsupported. The focused SDK fixture test passed, strict C99 compilation covered all six TCIR sources, and Clang static analysis emitted no diagnostics. This is translation and rejection evidence only: no TCIR interpreter, differential execution, backend, runtime adapter, non-host validation, or performance result exists yet.

Milestone 4 added a deliberately unoptimized reference interpreter with mandatory verification, whole-function eligibility, typed homes, SSA block-edge transfer, defined modular i32 arithmetic, explicit return/throw/rejection outcomes, and bounded execution. Unsupported operations and invalid graphs are rejected before the frame or TC PC changes; debug builds assert a second successful verifier pass, while Release retains the checked rejection path.

The differential harness uses the production-converter slots to invoke the existing `executeMethod` and TCIR interpreter with fresh independent state. Across 1,179 comparisons, `add`, `abs`, and `sumTo` agreed for curated zero/positive/negative/extreme/overflow cases and fixed-seed generated values; the bounded high loop includes an overflowing 65,537 iteration case. Normal and Release builds passed 3/3 CTest entries. ASan/UBSan passed the standalone TCIR core, ASan passed the differential runtime, strict C99 and Clang analysis covered all seven TCIR sources, and the SDK fixture test passed. Apple leak detection remains unavailable. UBSan signed-overflow instrumentation was intentionally not applied to `executeMethod` because its existing arithmetic implements wrap with signed C operations; new TCIR arithmetic is defined and passed UBSan. No native backend, production runtime adapter/dispatch, exception-handler/GC/helper evidence, non-host result, or performance claim exists yet.

Milestone 5 added an opt-in SLJIT baseline under `TotalCrossVM/src/tcvm/jit` without changing serialized methods or production dispatch. The backend reruns the canonical verifier, completes a whole-method eligibility scan, and only then emits the POC i32 constants/copies/arithmetic/comparisons, typed i32 home access, block-edge copies, branches, loops, and integer/void returns. The versioned `TCCompiledFrame` carries typed homes, arguments, TC PC, and invocation scratch. Unsupported or invalid functions never receive an entry, and deterministic emission failure returns an owned structured diagnostic.

Executable finalization and disposal are centralized through the depot-tools SLJIT build configured with `SLJIT_WX_EXECUTABLE_ALLOCATOR=1` and `SLJIT_PROT_EXECUTABLE_ALLOCATOR=0`. A synchronized in-memory side table exposes claimed/compiling/ready/rejected states, lets a competing caller retain interpreter execution, transfers artifact ownership only on successful publication, and defers cache destruction until outstanding claims resolve. Forced tests cover W^X mapping permissions, verifier rejection without frame mutation, unsupported switch rejection, emission failure cleanup, 64 create/dispose cycles, concurrent lookup, interpreter progress during a claim, publication, cache-owned disposal, and shutdown during compilation.

On Darwin 25.5.0 arm64 with Apple Clang 21.0.0, CMake 4.3.0, and Ninja 1.11.1, Debug and Release passed all 4/4 TCIR tests. The same 1,179 inputs agreed across `executeMethod`, the TCIR interpreter, and forced SLJIT. One Debug observation reported generated code sizes of 84 bytes (`add`), 256 bytes (`abs`), and 424 bytes (`sumTo`), with aggregate compile CPU time 0.000167 seconds; this is a single functional observation, not a benchmark or performance claim. ASan/UBSan and Clang static analysis reported no backend defect. With the JIT option omitted, no JIT targets existed and the original 3/3 tests passed. Android native cross-compilation passed for arm64-v8a using NDK `28.2.13676358`, target `aarch64-none-linux-android23`, compile/target SDK 35, and the same W^X definitions. No Android device execution, Windows/Linux build, production runtime dispatch, helper/GC/exception lowering, or performance conclusion is claimed.

The post-Milestone-5 measurement checkpoint added a reproducible standalone comparison without changing that acceptance boundary. The three executables reuse prepared contexts, frames, functions, and compiled artifacts; every warmup and measured batch must reproduce the `executeMethod` checksum. The aggregate target always runs the profiles sequentially: 5 warmups plus 60 samples, 10 plus 200, and 20 plus 1,000. The six possible backend orders rotate deterministically with occurrence counts differing by at most one, no samples are removed, and `validate-tcir-jit-benchmark.py` recomputes statistics and cross-checks 720, 2,400, and 12,000 CSV rows against their JSON files. JIT compilation is separately sampled 60, 200, and 1,000 times after the corresponding warmups, with the interpreter compilation baseline explicitly marked not applicable.

The accepted matrix was measured at revision `77d179edc99809f727d69cd5c475d52064bde331` on an Apple M1 Pro with 10 logical CPUs and 16 GiB RAM, Darwin 25.5.0 arm64, AC power at 100% battery with Low Power Mode on, no affinity, and no intentional background workload. It used AppleClang 21.0.0.21000101, Ninja, Release `-O3 -DNDEBUG -Wall -Wextra -Werror`, and the ARM-64 SLJIT backend. The following values are means; execution columns are nanoseconds per invocation and compile is microseconds per method:

| Samples | Workload | `executeMethod` | TCIR | SLJIT | SLJIT vs `executeMethod` | JIT compile |
| ---: | --- | ---: | ---: | ---: | ---: | ---: |
| 60 | `add` | 53.749 | 293.412 | 73.408 | 36.577% slower | 6.542 |
| 60 | `abs` | 59.490 | 467.767 | 84.355 | 41.798% slower | 6.990 |
| 60 | `sumTo(65537)` | 911,394.574 | 4,667,454.601 | 644,757.705 | 29.256% faster (1.414x) | 5.199 |
| 200 | `add` | 50.582 | 232.966 | 74.923 | 48.120% slower | 6.289 |
| 200 | `abs` | 57.163 | 447.766 | 83.412 | 45.920% slower | 4.517 |
| 200 | `sumTo(65537)` | 986,469.166 | 4,807,283.750 | 685,870.020 | 30.472% faster (1.438x) | 5.076 |
| 1,000 | `add` | 53.102 | 239.541 | 74.565 | 40.418% slower | 4.094 |
| 1,000 | `abs` | 57.535 | 448.913 | 82.128 | 42.745% slower | 4.639 |
| 1,000 | `sumTo(65537)` | 917,169.220 | 4,817,632.723 | 660,036.217 | 28.036% faster (1.390x) | 5.389 |

Code sizes remained 84, 256, and 424 bytes for `add`, `abs`, and `sumTo`. The raw distributions preserve scheduler outliers, so future analysis can use the recorded means, medians, sample standard deviations, and ranges without silently changing the exclusion policy. These are checkpoint measurements of the current standalone APIs under Low Power Mode, not production-VM or general performance claims.

The accepted raw artifacts are:

- 60 samples: `build/m5-sljit-benchmark/results/tcir-jit-benchmark-77d179edc998-s60.json` (12,005 bytes, SHA-256 `90dfea07ad84b2d7a59d6f69ea0fcb7c3402530bebaf44761980ed4a5b21724f`) and `.csv` (77,185 bytes, SHA-256 `8a9a20e447b3981e9a09898b1651af31718965af2fcedf8c936d8c860cf2a7b5`).
- 200 samples: `build/m5-sljit-benchmark/results/tcir-jit-benchmark-77d179edc998-s200.json` (25,049 bytes, SHA-256 `6c1ac5255985c7589e034bbbe43087b9be7c42abe01a7f45dfea384bad712364`) and `.csv` (258,441 bytes, SHA-256 `2d04f62090360c2c2aab88b9926404642892f1cc3dea6346ebd14511e65e1014`).
- 1,000 samples: `build/m5-sljit-benchmark/results/tcir-jit-benchmark-77d179edc998-s1000.json` (99,512 bytes, SHA-256 `126c37f14e51b6bdb6789527b0a6b1c70a9d63445f8fc7a1dbedfaa0a0313cf7`) and `.csv` (1,296,899 bytes, SHA-256 `9df40cc6ea82684219cc0e3a3e81eb3bef4e77c135dc0923be6d46bdafdf1f9e`).

Each JSON preserves host/build/protocol metadata, raw compile/execution samples, descriptive statistics, deltas, percentages, and ratios; each CSV preserves the paired raw observations and execution position. Future comparisons must rerun all three profiles and retain this scope, or label any changed dispatcher, scratch, power, workload, or profile regime explicitly.

Milestone 6 added `tcir_aot` and a native `tcaot` tool behind default-off `TC_ENABLE_C_AOT`. The generator verifies and preflights the complete function set before emitting a restricted C11 state machine, generated declarations, a lexically sorted exact-identity registry, and schema-versioned JSON manifest. Entry names combine escaped class/method/signature components with semantic content hashes. Successful output records generator, TCIR, and runtime ABI versions, aggregate input hash, target options, supported methods, and an empty rejected-method list; unsupported valid TCIR instead returns a structured diagnostic before any output file is written. Registration accepts only an exact class, method, signature, and content-hash match.

The host proof uses the canonical converter-produced `add`, `abs`, and `sumTo` set through `--input poc-fixtures`; it is not yet a general TCZ reader. In Debug and Release builds, seven focused tests passed: core, opcode-source validation, AOT generator behavior, clean determinism, independent manifest validation, differential execution, and SLJIT lifecycle/security. The differential harness performed 1,179 fresh-state `executeMethod`/TCIR/SLJIT/generated-C comparisons with fixed seed `0x4d595df4`. A separate AOT-only configuration passed 6/6 without SLJIT, and the default configuration exposed no AOT target and retained the original 3/3 tests. ASan covered the full AOT/JIT differential graph; ASan/UBSan covered the generator test with Apple leak detection disabled; focused Clang analysis reported no bugs. The broader VM scan produced 119 pre-existing reports outside the new AOT scope.

The accepted Release output under `build/m6-aot-release/aot/poc` has aggregate input hash `290ae3b843d50748` and method hashes `188dacfc7c8a2565` (`abs`), `2e2ae7a59518baec` (`add`), and `bbb376f3c57bafcb` (`sumTo`). `tcir_aot_generated.c` is 8,423 bytes with SHA-256 `14a7842d2a1d9714b3f49823530102062094e2f238680e9f927b4a0f5ad4351b`; its 340-byte header has SHA-256 `d0b47ae9eadfacdf5eefd27cb3af2d436586cad219f0f0c3cb510af6df2609b1`; and its 1,062-byte manifest has SHA-256 `077d3d005ecfb06f36fe05c6dc54b1237d0a16e0fba6eb4a8e97b1591a91e9ca`. The Release object is 2,608 bytes, static library 2,872 bytes, and native host tool 90,152 bytes. These ignored build artifacts are reproducibility evidence, not committed source or cross-platform size claims.

Android NDK `28.2.13676358` compiled `tcir_aot` and the host-generated C as an ELF64 AArch64 object for arm64-v8a/API 23. Apple Clang compiled the same generated C as a Mach-O arm64 object for iPhoneOS with minimum version 12.0. These are cross-compilation checks only: the Android toolchain cannot execute the host generator, and no Android/iOS device, application, static-registration, signing, or packaging test was performed. GCC, Linux, Windows/MSVC, and a full iOS root-CMake application link were unavailable and remain unvalidated.

The mandatory post-Milestone-6 macOS performance checkpoint reused the historical three-way standalone regime so it can be compared with the post-Milestone-5 baseline; generated C was deliberately excluded from performance ordering. It ran at revision `1aa428b740124b33830c157eadf08deb5660ea69` on the same Apple M1 Pro/Darwin 25.5.0 arm64 host class, AC power at 100% battery with Low Power Mode on, no affinity, and no intentional load. The 5/10/20 warmup and 60/200/1,000-sample profiles all validated their 720/2,400/12,000 CSV rows. Mean nanoseconds per invocation and JIT compile microseconds were:

| Samples | Workload | `executeMethod` | TCIR | SLJIT | SLJIT vs `executeMethod` | JIT compile |
| ---: | --- | ---: | ---: | ---: | ---: | ---: |
| 60 | `add` | 50.407 | 237.111 | 72.796 | 44.415% slower | 3.370 |
| 60 | `abs` | 56.433 | 439.124 | 85.732 | 51.918% slower | 4.750 |
| 60 | `sumTo(65537)` | 894,197.657 | 4,514,294.334 | 638,306.751 | 28.617% faster (1.401x) | 5.117 |
| 200 | `add` | 50.869 | 249.645 | 73.323 | 44.143% slower | 3.689 |
| 200 | `abs` | 68.432 | 568.594 | 99.570 | 45.502% slower | 4.480 |
| 200 | `sumTo(65537)` | 981,886.484 | 4,849,665.449 | 681,906.699 | 30.551% faster (1.440x) | 5.741 |
| 1,000 | `add` | 50.177 | 239.150 | 72.966 | 45.419% slower | 3.580 |
| 1,000 | `abs` | 56.769 | 443.216 | 81.737 | 43.982% slower | 4.008 |
| 1,000 | `sumTo(65537)` | 947,253.648 | 4,687,367.999 | 668,555.915 | 29.422% faster (1.417x) | 24.819 |

The 1,000-sample `sumTo` compilation mean retains its raw outlier influence, as required; code sizes remained 84, 256, and 424 bytes. The six accepted artifacts are:

- 60 samples: `build/m6-sljit-benchmark/results/tcir-jit-benchmark-1aa428b74012-s60.json` (11,988 bytes, SHA-256 `8ec87930a5482eff03b400a60e12aa9e1f1e17882c8a6b36961f3b8de7791ac9`) and `.csv` (77,177 bytes, SHA-256 `7236e4bb69932fac71bbc470705bea0ea6b0a56ff06bb339dcf2ff554bae0ecc`).
- 200 samples: `build/m6-sljit-benchmark/results/tcir-jit-benchmark-1aa428b74012-s200.json` (25,072 bytes, SHA-256 `723ca4078c1f4772fc24bdbcbfa5c0240abc1a02b81ca9e06ee771d892fc7b05`) and `.csv` (258,459 bytes, SHA-256 `3c9893e25a2b99210a00bdea0c58409172c5981e4b07d80140cf3ffdedfafac6`).
- 1,000 samples: `build/m6-sljit-benchmark/results/tcir-jit-benchmark-1aa428b74012-s1000.json` (99,766 bytes, SHA-256 `40f6c9b975ea47870fcc63a84199fdc47375d776184a6753ca43ff0003aa37e3`) and `.csv` (1,297,155 bytes, SHA-256 `c3b8f06c940557eac0bc4788bef4cafd883ce0e9479ae15b02a7dae8e407b440`).

This checkpoint makes no generated-C performance claim. A future four-way benchmark must be introduced as a separately named regime while continuing to repeat the historical three-way matrix.

This section must be updated after every completed milestone with delivered behavior, validation evidence, deferred scope, and lessons. At full completion it must state exactly which opcode and platform sets are production-ready, which remain experimental, and why.

## Editorial Report

This is an interim factual report through the completed deterministic-C AOT proof of concept. It is not the final report required by `.agent/PLANS.md`; Milestone 9 must reconcile it with production runtime integration, broader semantic/platform coverage, and final performance evidence.

### Editorial Summary

The project now has a source-grounded map from Java class parsing through TotalCross bytecode execution and GC, an implemented backend-neutral TCIR version 1 contract, a bounded frontend for the static-integer POC subset, a verified reference interpreter, an opt-in SLJIT baseline, and an opt-in deterministic portable-C generator. Developers can take the three canonical converter fixtures through `executeMethod`, TCIR, forced native SLJIT, and compiled generated C using one versioned frame ABI. No default production runtime execution path changed.

### Original Plan versus Actual Outcome

Milestone 1 produced only the ExecPlan and architecture documents. Milestone 2 then implemented the planned C contract, verifier, printer, registry, fixtures, and focused tests. The optional text parser was omitted because stable one-way golden output met acceptance without creating another input format. Milestone 3 implemented the partial decoder/frontend through an explicit bounded method view and generated its integration fixtures through the production converter. Milestone 4 implemented reference execution and an isolated real-`executeMethod` oracle. Milestone 5 implemented the pinned, optional SLJIT backend and standalone artifact cache, including Android cross-compilation but not Android execution. Milestone 6 implemented deterministic generated C, an exact-identity registry and manifest, a native POC host tool, and four-way functional comparison. The planned general TCZ input, production runtime adapter, dispatcher publication, and AOT performance regime remain later work.

### What Changed

The initial eight documents under `docs/architecture/bytecode` and this ExecPlan remain the design record. Milestone 2 added the owned TCIR implementation, verifier, printer, registry, focused tests, CMake option, and opcode source validator. Milestone 3 added the bounded frontend, converter-backed fixtures/goldens, and SDK regeneration check. Milestone 4 added `tcir_interp.h`, `tcir_interp.c`, reference execution/rejection tests in `tcir_tests.c`, and the real-runtime differential harness. Milestone 5 added the `tcir_jit` library, backend/cache/memory interfaces, forced JIT test, depot-tools pin, and Android integration. Milestone 6 extracted `tcir_compiled.h`, added `tcir_aot`, `tcaot`, generated C/header/manifest output, deterministic and manifest validators, exact registry tests, four-way differential execution, and CMake fixture targets.

### Decisions and Trade-offs

TCIR version 1 uses opaque owned C structures, simplified-SSA values and block arguments, typed homes, explicit source metadata/effects, stable diagnostics, and a deterministic one-way printer. Builders copy temporary arrays but the verifier—not the builder—is the canonical malformed-graph rejection boundary. Reference, SLJIT, and AOT entries share `tcir_compiled.h` without importing private `Context` layout and use explicit result/rejection status. Generated C uses a portable state machine and stable lexical order instead of compiler extensions; semantic FNV-1a hashes provide deterministic identity, not cryptographic trust. Future runtime work still requires a real class/TCZ adapter, reference homes in `regO`, helper ABI, method-atomic dispatch, and production AOT publication.

### Unexpected Problems and Discoveries

The existing converter IR is opcode-shaped, float identity is normalized, the class record is unversioned inside TCZ, and one Java opcode-name table omits values 158/159. Root CMake resolves the full native dependency graph before focused targets run, and its legacy PUBLIC source list propagates all VM sources to normal target consumers; the differential harness therefore links the built artifact directly. Milestone 3 found that `TMethod` discards its serialized code count, call width depends on resolved signature metadata, and converter control flow uses `BREAK` padding. Milestone 4 exposed the legacy oracle's signed-C overflow under UBSan. Milestone 5 confirmed depot-tools SLJIT/W^X and Android API 23 compilation. Milestone 6 showed that negative backend fixtures must first be valid TCIR, that cross builds need C generated by a native host tool, and that broad VM static analysis surfaces legacy findings which must be separated from focused new-code analysis.

### Validation and Measurable Results

Milestone 6 validation observed Debug and Release 7/7 CTest passing with 1,179 forced `executeMethod`/TCIR/SLJIT/AOT comparisons; AOT-only 6/6; default-off 3/3 with no AOT targets; byte-identical clean regeneration; independent manifest validation; ASan full differential execution; ASan/UBSan generator coverage; and focused Clang analysis with no reports. Android NDK r28c/API 23 and iPhoneOS arm64 compiled the same host-generated C object, without device execution or application linkage. The mandatory macOS 60/200/1,000 matrix was repeated with its historical three-way scope, all 720/2,400/12,000 rows validated, and no samples excluded. It again found short-call SLJIT overhead and a 1.401x–1.440x mean advantage for `sumTo(65537)`; generated-C performance was intentionally not measured. Linux/GCC, Windows/MSVC, full iOS linkage, production dispatch, and product workloads remain unvalidated.

### Useful Evidence and Examples

The bytecode reference enumerates all 160 opcodes. `tcir_converter_fixtures.h` records exact converter words/lines, `golden/frontend-sumTo.tcir` shows the deterministic loop CFG, `tcir_aot_tests.c` covers order-independent output, input invalidation, lookup mismatch, and unsupported rejection, and `tcir_differential_tests.c` contains the four-way real-runtime corpus. `ValidateTCIRAotDeterminism.cmake` and `validate-tcir-aot-manifest.py` independently test derived output. Commits `8d738ff66` and `1aa428b74` are the shared ABI and Milestone 6 implementation. The generated artifact and repeated benchmark paths and SHA-256 hashes above identify the accepted checkpoints; the compatibility matrix distinguishes exact fixture evidence from family-level roadmap labels.

### Limitations, Remaining Work, and Open Questions

No TCIR text parser, general TCZ-to-AOT input adapter, production runtime `Method` adapter, dispatcher integration, static publication model, or non-host platform execution test exists yet. The frontend/interpreter/SLJIT/AOT backends cover only the registry's static-integer POC subset and valid helper- or exception-bearing methods remain fallback. The benchmark measures standalone interpreter/JIT APIs, includes current per-invocation verification/allocation overhead, ran with Low Power Mode enabled, covers only three synthetic integer fixtures, and does not measure AOT. Handler behavior, heap mutation, GC, helper effects, thread suspension, unloading/redefinition, volatile/atomic semantics, dead stripping/signing, and real-world legacy `JUMP_regI` require later evidence.

### Possible Article Angles

A future technical article could explain how to establish a verifiable IR boundary before writing a bytecode frontend, how a three-axis opcode registry prevents roadmap status from masquerading as implementation coverage, or how reusing managed register arenas can simplify the first correct GC contract for generated code.

### Suggested Narrative

Start with the existing Java-to-register-bytecode path, show why the target-shaped converter IR cannot serve multiple backends, then introduce the owned TCIR contract, bounded method view, structural decoder, canonical verifier, converter-backed fixtures, and deterministic CFG as the boundary established before execution. Continue with the reference interpreter, then explain how the same frame contract supports low-latency SLJIT and deterministic portable C. End with four-way differential evidence, current standalone JIT measurements, and the explicit runtime-integration boundary.

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

This milestone is complete. The C99 library under `TotalCrossVM/src/tcvm/ir` consists of `tcir.h`, `tcir.c`, `tcir_internal.h`, `tcir_verify.c`, `tcir_dump.c`, `tcir_opcode_map.h`, `tcir_opcode_map.c`, and `tcir_opcode_registry.def`. The public header declares opaque module/function/block/value/symbol structures, type/effect/op/terminator enums, source metadata, stable diagnostics, allocator ownership, read-only iteration, and `TC_IR_VERSION`. APIs return errors rather than exiting.

The macro registry maps all values 0–159 to decoder shape, lowering class, and current POC status. Compile-time checks bind every row to `opcodes.h`; the C validator checks count, ordering, duplicates, and dispositions; and `scripts/validate-tcir-opcodes.py` compares C, Java numeric constants, runtime dispatch, docs, and tests. The canonical printer uses stable numeric values, numeric block ordering, quoted identities, ordered effects, and explicit metadata. No parser was needed.

The verifier checks continuation-slot source PCs, targets, definition ownership/order, block arguments, types, return signatures, handler edges, symbol kinds, unchecked-array proofs, helper effects, GC homes, non-null proofs, and internal-address lifetime. Every diagnostic includes stable code, function identity, and TC PC. Tests manually construct `add`, `abs`, and `sumTo` plus ten malformed cases: the five required cases and additional continuation, helper-effect, unchecked-proof, internal-address, and non-null-proof failures.

Acceptance was observed on macOS arm64: both normal Debug and ASan/UBSan `check-tcir` runs passed 2/2 CTest entries; repeated valid dumps matched the committed goldens byte-for-byte; all ten invalid functions produced repeatable codes/messages/identity/TC PC; Clang static analysis emitted no diagnostics; and the registry/source validators covered all 160 opcodes. No interpreter or native generated code exists yet. The executable implementation is commits `96c17be4b` and `a3a5e33fa`.

### Milestone 3: partial TotalCross bytecode to TCIR frontend

Implement `tcir_decode.c` and `tcir_frontend.c`. Decode the POC opcode subset from runtime `Method` code, marking logical instruction starts and validating every operand before building blocks. Because TotalCross input is already register-based, model the typed bank slots directly, calculate leaders and edges, then promote safe values and create block arguments. Do not simulate a fictitious JVM stack. Preserve TC PC and source line.

The first accepted subset is `BREAK`, `MOV_regI_regI`, `MOV_regI_sym`, `MOV_regI_s18`, `INC_regI`, integer add/subtract/multiply variants that do not access arrays, integer comparison variants, `JUMP_s24`, `DECJGTZ_regI`, `DECJGEZ_regI`, and integer/void return variants. Add only forms emitted by the chosen converter fixtures, then add missing POC forms deliberately. A method containing anything else receives `TCIR_UNSUPPORTED_OPCODE` with numeric opcode, name, and TC PC; malformed continuations, targets, registers, symbols, handler ranges, or type merges receive distinct validation diagnostics.

Generate Java fixtures for `add`, `abs`, and `sumTo` through the existing converter rather than hand-authoring binary slots for integration tests. Keep hand-authored malformed slot arrays for negative tests. Golden tests compare canonical TCIR and CFG edges. Repeated frontend runs must not depend on pointer values or hash iteration.

Acceptance means each POC method generated by the real converter yields the expected deterministic IR and passes the verifier; each negative case fails before code execution; and any method outside the subset remains eligible for the current interpreter. Update the mapping matrix with focused-test evidence rather than marking entire families complete.

Acceptance was observed on macOS arm64. The production converter emitted the committed slot/line fixtures for `add`, `abs`, and `sumTo`; both repeated frontend runs matched the canonical goldens and verifier; ten frontend-negative/fallback cases produced stable codes/messages/function/TC PC without adding a function to the module; normal and ASan/UBSan CTest passed 2/2; strict C99 and Clang analysis passed; and the focused SDK fixture test passed. Apple ASan leak detection was unavailable and no execution equivalence was attempted. The executable implementation is commits `0fa51be08` and `f0e241b11`.

### Milestone 4: reference TCIR interpreter and differential oracle

Implement `tcir_interp.c` over verified functions. It executes block/value semantics while using the same `Context` typed frame homes and explicit result/exception status planned for native code. The interpreter is deliberately simple: no optimization, direct access only to operations whose layouts are already stable, and runtime helpers for complex behavior. It asserts verification in test/debug builds and rejects unverified input in release APIs.

Build a differential harness that invokes the existing TotalCross bytecode interpreter and TCIR interpreter with independent fresh state for the same method and inputs. Compare return type/value, pending exception class/message where stable, selected handler behavior, and relevant mutated state. For `add`, `abs`, and `sumTo`, include zero, positive, negative, `INT32_MIN`, `INT32_MAX`, overflow-producing combinations, and loop counts including zero and a bounded high value. Add fixed-seed generated integer cases.

Acceptance means every supported fixture agrees across both interpreters for the deterministic corpus and fixed seed; malformed/unverified IR never executes; unsupported methods remain wholly interpreted; and sanitizer-enabled host tests report no IR ownership or bounds defect where the repository toolchain supports them. Only after this milestone can optimization passes begin, and even then each pass must remain independently toggleable.

Acceptance was observed on macOS arm64. The reference interpreter rejected invalid or ineligible functions before frame mutation in both assertion-enabled and Release builds. The real `executeMethod` and TCIR paths agreed for all 1,179 fresh-state comparisons across the converter-produced `add`, `abs`, and `sumTo` fixtures, including signed extrema, overflow-producing arithmetic, negative/zero loops, a bounded 65,537-iteration overflow loop, and seed `0x4d595df4`. Unsupported frontend input returned method-atomic fallback without publishing a function. Normal and Release CTest passed 3/3; ASan/UBSan passed the standalone TCIR tests; ASan passed the differential runtime; strict C99, Clang analysis, and the focused SDK fixture test passed. Apple leak detection was unavailable, and UBSan signed-overflow checking was excluded only from the legacy oracle comparison for the reason recorded in Outcomes. The executable implementation is commits `d5ebceb43` and `801ae507b`.

### Milestone 5: SLJIT baseline backend

Integrate a pinned SLJIT revision through the native dependency process. Do not edit or commit the generated `TotalCrossVM/deps/totalcross-depot-tools` checkout. If depot-tools lacks an SLJIT package, make the source/dependency change in its owning repository, preserve the Simplified BSD notice, then update `totalcross-depot-tools.ref` in this repository through the established bootstrap flow. Record the exact upstream revision, license path, enabled architectures, and any local configuration.

Add an optional CMake feature such as `TC_ENABLE_SLJIT_JIT`, default off, and a backend under `TotalCrossVM/src/tcvm/jit`. Implement the frame ABI and POC integer/branch/return lowering only. Before emitting code, run the canonical verifier and backend eligibility pass over the entire method. Centralize executable-memory allocation, write/finalize/execute transitions, instruction-cache flush, and disposal; enforce platform W^X. Publish artifacts atomically in a side table and let competing threads interpret while one compiles.

Use runtime/dispatcher thunks for anything outside direct arithmetic/control flow. Initially enable execution tests on Linux x86-64 or the available development host only, while preserving compile-time portability. Add forced-JIT mode so a test expected to compile fails if it falls back. Test concurrent compilation claims, compilation failure cleanup, repeated creation/disposal, and explicit no-execution after verification failure.

Acceptance means `add`, `abs`, and `sumTo` agree among current interpreter, TCIR interpreter, and forced SLJIT for all Milestone 4 inputs; executable memory is not left writable and executable; unsupported functions never produce an entry; artifacts are freed safely at shutdown; and JIT remains silent/off by default. Record platform/toolchain and code-size/compile-time observations as measurements, not performance claims.

Acceptance was observed on macOS arm64 and for Android cross-compilation. The depot-tools tag `sljit-20260717` supplies upstream revision `3907e69005ba6e30b225000f24aaef3632f88347`, Simplified BSD license at `share/licenses/sljit/LICENSE`, W^X allocation, and argument checks. Root CMake keeps `TC_ENABLE_SLJIT_JIT` off by default; the Android arm64-v8a Gradle configuration compiles it without selecting runtime execution. The backend verified and preflighted every function before emission, and the forced differential suite matched all 1,179 Milestone 4 inputs. Normal and Release CTest passed 4/4, standalone ASan/UBSan and Clang analysis passed, mapping inspection found executable/non-writable finalized code, default-off CTest passed 3/3 with no JIT targets, and Android compiled with NDK `28.2.13676358` for API 23. One Debug host observation produced 84/256/424 code bytes and 0.000167 seconds aggregate compile CPU time for `add`/`abs`/`sumTo`; it is not a benchmark. Device execution, distributed macOS entitlements, production dispatch, helper/GC/exception lowering, and non-host execution remain later work. The implementation is commits `d7d4ad64a` and `8a5ae42d6`.

An additional accepted measurement checkpoint now follows Milestone 5 without expanding backend semantics. Commit `e49acf6a5` adds the optional `TC_BUILD_IR_BENCHMARKS` path and independent JSON/CSV validation; `77d179edc` makes `run-tcir-jit-benchmark` execute all three profiles sequentially. The accepted macOS matrix uses 5/10/20 warmups and 60/200/1,000 samples, rotates through every execution-order permutation with counts differing by at most one, validates every batch against the interpreter oracle, and excludes nothing. Its detailed conditions, six raw artifact hashes, and results are recorded in Outcomes. Future milestones must rerun the same aggregate target into their revision- and profile-keyed filenames before changing the benchmark scope; when scope changes, retain both result families and document the new regime instead of overwriting or directly combining unlike measurements.

### Milestone 6: deterministic portable-C AOT backend

Add an AOT generator, tentatively under `TotalCrossVM/src/tcvm/aot`, plus a host tool entry point. It consumes only verified TCIR and emits portable C, a stable method registry, a manifest, and optional line maps into the build directory. Generated names derive from escaped stable method identities plus content hashes; units, declarations, symbols, includes, and registry order are deterministic. Never write generated C into source directories or commit it as source.

Generated functions implement the same frame ABI and runtime-helper calls as JIT. The manifest records generator, IR and runtime ABI versions, input hash, target options, supported and rejected methods, and diagnostic codes. Registration verifies class/method/signature and content identity before publishing an entry; mismatch falls back. For the POC, compile with the host C toolchain through CMake, link a focused harness or VM target, and run the same corpus. Verify the C also compiles with both GCC/Clang syntax expectations and avoids extensions unsupported by MSVC where applicable.

Acceptance means two clean generations from identical inputs are byte-for-byte equal; the POC C compiles, links, and produces results identical to the two interpreters and SLJIT; a changed input invalidates registration; unsupported IR is reported before C output; and a build with AOT disabled is unchanged. Document root-`TotalCrossVM/CMakeLists.txt` configure/build commands for Linux, Windows, Apple Clang, Android NDK, and iOS toolchains even if only the host is executed in this milestone. Do not add an `Android.mk` or Xcode-project integration path.

Acceptance was observed for the standalone POC in commits `8d738ff66` and `1aa428b74`. Two clean generations and reversed input order produced byte-identical C, header, and manifest; a semantic input change changed the content/aggregate hash and made old identity lookup fail; a valid unsupported switch returned a structured diagnostic before output; and the default-off build retained only the original targets. Debug and Release passed 7/7 focused tests, including 1,179 four-way comparisons. AOT-only passed 6/6 without SLJIT. Host-generated C compiled as macOS, Android arm64-v8a/API 23, and iPhoneOS arm64 objects, while the Android/iOS cross toolchains did not execute `tcaot`. Linux/GCC, Windows/MSVC, full iOS application linkage, production TCZ input, dead-strip-safe registration, runtime publication, and AOT performance remain later validation/integration work.

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

All commands below run from the repository root unless a command starts with `cd`. Build scripts and source files must not hard-code a checkout path.

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

Milestone 2 exposes the IR library and focused native tests without enabling a backend. Its actual host commands are:

    cmake -S TotalCrossVM -B build-ir -DCMAKE_BUILD_TYPE=Debug -G Ninja -DTC_BUILD_IR_TESTS=ON
    ninja -C build-ir check-tcir

`TC_BUILD_IR_TESTS` is optional and off by default. `check-tcir` builds `libtcir.a`, `tcir_tests`, the normal `tcvm` artifact, and `tcir_differential_tests`, then runs `tcir-core`, `tcir-opcode-sources`, and `tcir-differential`. The existing `ENABLE_TEST_SUITE` preprocessor macro is unrelated. The standalone TCIR ASan/UBSan configuration is:

    cmake -S TotalCrossVM -B build-ir-sanitize -DCMAKE_BUILD_TYPE=Debug -G Ninja -DTC_BUILD_IR_TESTS=ON -DCMAKE_C_FLAGS='-fsanitize=address,undefined -fno-omit-frame-pointer' -DCMAKE_EXE_LINKER_FLAGS='-fsanitize=address,undefined'
    ninja -C build-ir-sanitize tcir_tests
    ASAN_OPTIONS=detect_leaks=0 ctest --test-dir build-ir-sanitize --output-on-failure -R '^tcir-(core|opcode-sources)$'

The differential oracle uses ASan without UBSan signed-overflow instrumentation because `executeMethod` currently performs signed C arithmetic for TotalCross/Java wrap semantics:

    cmake -S TotalCrossVM -B build-ir-asan-diff -DCMAKE_BUILD_TYPE=Debug -G Ninja -DTC_BUILD_IR_TESTS=ON -DCMAKE_C_FLAGS='-fsanitize=address -fno-omit-frame-pointer' -DCMAKE_CXX_FLAGS='-fsanitize=address -fno-omit-frame-pointer' -DCMAKE_EXE_LINKER_FLAGS='-fsanitize=address' -DCMAKE_SHARED_LINKER_FLAGS='-fsanitize=address'
    ninja -C build-ir-asan-diff tcir_differential_tests
    ASAN_OPTIONS=detect_leaks=0 ctest --test-dir build-ir-asan-diff --output-on-failure -R '^tcir-differential$'

Generate and verify the POC converter inputs with the focused SDK test. Use `TotalCrossSDK/gradlew-agent`, keep full Gradle output in its log, and avoid `clean` unless stale artifacts are proven:

    cd TotalCrossSDK
    ./gradlew-agent test --tests tc.tools.converter.modernjava.TCIRConverterFixtureTest --console=plain --warning-mode=none

Set `TCIR_UPDATE_FIXTURES=true` only when intentionally regenerating the reviewed native fixture header. Set `TCIR_UPDATE_GOLDENS=1` on `build-ir/tcir_tests` only when intentionally regenerating canonical frontend goldens; the normal tests compare without modifying source.

For SLJIT, fetch dependencies through the documented bootstrap and pinned ref only after the owning dependency source is ready:

    TotalCrossVM/deps/fetch-depot-tools.sh
    cmake -S TotalCrossVM -B build-jit -G Ninja -DCMAKE_BUILD_TYPE=Debug -DTC_ENABLE_SLJIT_JIT=ON -DTC_BUILD_IR_TESTS=ON
    cmake --build build-jit --target check-tcir --parallel

Configure and run the optional standalone performance checkpoint only in an optimized build. Supply honest power/load/dirty-path notes; CMake replaces semicolons in those values with commas before invoking the harness. The aggregate target runs the 60-, 200-, and 1,000-sample profiles sequentially and validates each one. Result filenames include the first twelve characters of the configured repository revision plus the sample count, so later revisions and all required profiles coexist in the same results directory:

    cmake -S TotalCrossVM -B build/m5-sljit-benchmark -G Ninja \
      -DCMAKE_BUILD_TYPE=Release \
      -DTC_ENABLE_SLJIT_JIT=ON \
      -DTC_BUILD_IR_TESTS=ON \
      -DTC_BUILD_IR_BENCHMARKS=ON \
      -DUSE_SKIA=OFF \
      -DTC_BENCHMARK_POWER_MODE='<observed power state>' \
      -DTC_BENCHMARK_BACKGROUND_LOAD='<observed load/isolation>' \
      -DTC_BENCHMARK_DIRTY_PATHS='<observed dirty paths>'
    cmake --build build/m5-sljit-benchmark --target check-tcir --parallel
    cmake --build build/m5-sljit-benchmark --target run-tcir-jit-benchmark
    for samples in 60 200 1000; do
      python3 scripts/validate-tcir-jit-benchmark.py \
        --json build/m5-sljit-benchmark/results/tcir-jit-benchmark-<revision>-s${samples}.json \
        --csv build/m5-sljit-benchmark/results/tcir-jit-benchmark-<revision>-s${samples}.csv
    done

Do not log authenticated URLs or tokens. Save verbose configure/build/test output to a temporary log and show only the status, relevant errors, and a short tail.

Milestone 6 exposes `TC_ENABLE_C_AOT`, native host tool `tcaot`, and aggregate fixture target `tcvm_aot_fixture`. The current tool input name `poc-fixtures` selects the three canonical converter-backed methods; it is not a path or a general TCZ reader. Generate only into a build directory:

    cmake -S TotalCrossVM -B build/m6-aot -G Ninja \
      -DCMAKE_BUILD_TYPE=Debug \
      -DTC_ENABLE_C_AOT=ON \
      -DTC_ENABLE_SLJIT_JIT=ON \
      -DTC_BUILD_IR_TESTS=ON \
      -DUSE_SKIA=OFF
    cmake --build build/m6-aot --target tcvm_aot_fixture --parallel
    cmake -E make_directory build/m6-aot/aot/manual
    build/m6-aot/tools/tcaot \
      --input poc-fixtures \
      --output build/m6-aot/aot/manual \
      --manifest build/m6-aot/aot/manual/manifest.json \
      --target-options host-c11-poc
    cmake --build build/m6-aot --target check-tcir --parallel

The same root project configures Linux/GCC or Clang. Use a native build first so `tcaot` can generate C, then pass or compile the generated translation unit in the target build:

    CC=gcc cmake -S TotalCrossVM -B build/m6-aot-linux -G Ninja \
      -DCMAKE_BUILD_TYPE=Release -DTC_ENABLE_C_AOT=ON \
      -DTC_BUILD_IR_TESTS=ON -DUSE_SKIA=OFF
    cmake --build build/m6-aot-linux --target tcvm_aot_fixture --parallel

Windows Release builds must use the static MSVC runtime `/MT`; configure x64 and the repository's x86 target independently:

    cmake -S TotalCrossVM -B build/m6-aot-windows-x64 -G "Visual Studio 17 2022" -A x64 \
      -DTC_ENABLE_C_AOT=ON -DTC_BUILD_IR_TESTS=ON \
      -DCMAKE_MSVC_RUNTIME_LIBRARY=MultiThreaded -DUSE_SKIA=OFF
    cmake --build build/m6-aot-windows-x64 --config Release --target tcvm_aot_fixture
    cmake -S TotalCrossVM -B build/m6-aot-windows-x86 -G "Visual Studio 17 2022" -A Win32 \
      -DTC_ENABLE_C_AOT=ON -DTC_BUILD_IR_TESTS=ON \
      -DCMAKE_MSVC_RUNTIME_LIBRARY=MultiThreaded -DUSE_SKIA=OFF
    cmake --build build/m6-aot-windows-x86 --config Release --target tcvm_aot_fixture

Apple Clang host validation uses the first native command with `-DCMAKE_BUILD_TYPE=Release`. Cross configurations intentionally build `tcir_aot` but not `tcaot`; generated C must come from a prior native host invocation. Android arm64-v8a/API 23 with the currently selected NDK is:

    cmake -S TotalCrossVM -B build/m6-aot-android -G Ninja \
      -DCMAKE_BUILD_TYPE=Release \
      -DCMAKE_TOOLCHAIN_FILE=<android-ndk-28.2.13676358>/build/cmake/android.toolchain.cmake \
      -DANDROID_ABI=arm64-v8a -DANDROID_PLATFORM=android-23 \
      -DANDROID_STL=c++_static -DTC_ENABLE_C_AOT=ON \
      -DTC_BUILD_IR_TESTS=OFF -DUSE_SKIA=OFF
    cmake --build build/m6-aot-android --target tcir_aot --parallel
    <android-ndk-28.2.13676358>/toolchains/llvm/prebuilt/<host>/bin/aarch64-linux-android23-clang \
      -std=c11 -Wall -Wextra -Werror -pedantic \
      -I TotalCrossVM/src/tcvm/aot -I TotalCrossVM/src/tcvm/ir \
      -c build/m6-aot/aot/poc/tcir_aot_generated.c \
      -o build/m6-aot-android/tcir_aot_generated.o

The root CMake iOS cross-configuration is likewise separate from the checked-in legacy project. A representative arm64 device configuration and direct generated-C syntax/object check are:

    cmake -S TotalCrossVM -B build/m6-aot-ios -G Xcode \
      -DCMAKE_SYSTEM_NAME=iOS -DCMAKE_OSX_ARCHITECTURES=arm64 \
      -DCMAKE_OSX_DEPLOYMENT_TARGET=12.0 -DTC_ENABLE_C_AOT=ON \
      -DTC_BUILD_IR_TESTS=OFF -DUSE_SKIA=OFF
    cmake --build build/m6-aot-ios --config Release --target tcir_aot
    xcrun --sdk iphoneos clang -arch arm64 -miphoneos-version-min=12.0 \
      -std=c11 -Wall -Wextra -Werror -pedantic \
      -I TotalCrossVM/src/tcvm/aot -I TotalCrossVM/src/tcvm/ir \
      -c build/m6-aot/aot/poc/tcir_aot_generated.c \
      -o build/m6-aot-ios/tcir_aot_generated.o

Only the macOS host, Android object, and iPhoneOS object paths were executed for Milestone 6. Linux/GCC, Windows/MSVC, and the full iOS CMake/link commands above are reproducibility instructions for future platform validation, not reported results.

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

Milestone 2 delivered the TCIR core/verifier/dumper/opcode registry and focused native fixtures/tests in commits `96c17be4b` and `a3a5e33fa`. Milestone 3 delivered the bounded bytecode decoder/frontend and production-converter golden fixtures in commits `0fa51be08` and `f0e241b11`. Milestone 4 delivered the TCIR interpreter and real-`executeMethod` differential harness in commits `d5ebceb43` and `801ae507b`. Milestone 5 delivered the optional SLJIT dependency/build integration and baseline backend/tests in commits `d7d4ad64a` and `8a5ae42d6`; commits `e49acf6a5` and `77d179edc` add its reproducible post-milestone three-profile performance checkpoint. Milestone 6 delivered the shared compiled ABI in `8d738ff66` and the deterministic C generator, host tool, manifest/registry schema, CMake integration, reproducibility/registration tests, and four-way differential execution in `1aa428b74`. Milestone 7 adds experimental dispatcher policy, runtime thunks, diagnostics, and mixed-mode/GC tests. Milestone 8 grows those artifacts without introducing a second competing IR. Milestone 9 adds broader recorded platform/benchmark result artifacts and final editorial evidence.

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

What iOS AOT registration/linking model best fits the root `TotalCrossVM/CMakeLists.txt` target graph, static initialization, dead stripping, and code-signing flow when configured with the supported iOS CMake toolchain/generator? Milestone 6 established a deterministic explicit registry and proved that host-generated C compiles for iPhoneOS arm64 without touching legacy `TCVM.xcodeproj`; Milestone 7 must select and test its production archive/link/publication model, including dead stripping and signing.

Revision note (2026-07-17): created the initial self-contained plan after source analysis, changed all repository documentation to English, and made root `TotalCrossVM/CMakeLists.txt` the only native-build integration point while excluding legacy `TCVM.xcodeproj` and `Android.mk`, following the user's explicit instructions.

Revision note (2026-07-17, Milestone 2): reconciled the plan with the implemented version 1 owned TCIR API, verifier, one-way canonical printer, three-axis 160-opcode registry, focused CMake tests, source cross-check, normal/sanitizer evidence, and commits `96c17be4b` and `a3a5e33fa`.

Revision note (2026-07-17, Milestone 3): reconciled the plan with the bounded method view, structural continuation decoder, static-integer CFG/SSA frontend, production-converter fixtures, twenty deterministic diagnostic cases, host validation evidence, and commits `0fa51be08` and `f0e241b11`. At that revision, Milestones 4–9 were unstarted.

Revision note (2026-07-17, Milestone 4): reconciled the plan with the verified reference interpreter, typed-home execution contract, defined i32 wrap semantics, pre-execution rejection, 1,179-case real-`executeMethod` oracle, method-atomic fallback, host sanitizer/Release evidence, and commits `d5ebceb43` and `801ae507b`. Milestones 5–9 remain unstarted.

Revision note (2026-07-17, Milestone 5): reconciled the plan with pinned depot-tools tag `sljit-20260717`, the default-off root-CMake backend, Android arm64-v8a compilation, verified whole-method SLJIT emission, W^X finalization, frame ABI, synchronized side-table lifecycle, 1,179-case forced three-way comparison, host sanitizer/Release/static-analysis evidence, and commits `d7d4ad64a` and `8a5ae42d6`. Milestones 6–9 remain unstarted.

Revision note (2026-07-17, post-Milestone-5 benchmark): added the optional revision-keyed benchmark/validator in `e49acf6a5`, then made its 60/200/1,000-sample matrix mandatory in `77d179edc`; captured 5/10/20 warmups and all raw samples across near-balanced rotations of all six backend permutations on macOS arm64; recorded compile latency, code size, execution statistics, host/build/power/dirty metadata, six artifact hashes, and the standalone-API scope. Every accepted profile found SLJIT slower for short `add`/`abs` calls but 1.390x–1.438x faster on mean for `sumTo(65537)` under the recorded Low Power Mode conditions. Future checkpoints must repeat all three profiles. Milestones 6–9 remain unstarted.

Revision note (2026-07-17, Milestone 6): reconciled the plan with backend-neutral `tcir_compiled.h`, default-off deterministic C11 generation, the native canonical-fixture `tcaot` adapter, exact identity/hash lookup, schema-versioned manifest, build-directory-only output, seven focused tests, 1,179-case four-way execution, host/AOT-only/default-off/sanitizer/static-analysis evidence, and Android/iPhoneOS arm64 object compilation in commits `8d738ff66` and `1aa428b74`. The mandatory three-way performance matrix was repeated unchanged at revision `1aa428b740124b33830c157eadf08deb5660ea69` with all six artifacts recorded above. General TCZ input, production runtime publication, full iOS linkage, Linux/Windows validation, device execution, and AOT performance remain later work. Milestones 7–9 remain unstarted.
