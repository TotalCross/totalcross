<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# JIT and AOT architecture

## Architecture boundary

The proposed execution pipeline is:

```text
TCZ / TotalCross class record
            |
            v
verified TotalCross bytecode
            |
            v
backend-neutral TCIR  -----> TCIR interpreter
            |
            +--------------> SLJIT baseline JIT
            |
            +--------------> deterministic C AOT generator
            |
            `--------------> future LLVM/Cranelift adapter
```

The bytecode interpreter remains available for every valid existing method. Compilation is an optional execution choice and must not change serialization, class loading, native method lookup, GC, or exception semantics.

## Why SLJIT for the baseline

[SLJIT](https://github.com/zherczeg/sljit) is a small, low-level, platform-independent JIT generator with direct register and stack operations, code serialization facilities, and upstream support for x86 32/64, ARM 32/64, RISC-V 32/64, s390x, PowerPC, LoongArch and MIPS families. Its low abstraction level fits a baseline backend but does not replace TCIR, validation, register-home, exception, or GC design. SLJIT uses the [Simplified BSD license](https://github.com/zherczeg/sljit/blob/master/LICENSE), whose notice and redistribution requirements must be carried in source and binary distributions.

Milestone 5 pins depot-tools tag `sljit-20260717` in `TotalCrossVM/deps/totalcross-depot-tools.ref`. Its artifacts contain upstream revision `3907e69005ba6e30b225000f24aaef3632f88347`, source SHA-256 `f3e299647a610c537296a41d8866f1e7b664401c229e6cdb67a621250086efd9`, and the upstream license at `share/licenses/sljit/LICENSE`. The package enables argument checks and the W^X executable allocator. Ordinary CMake configuration consumes the prebuilt package instead of fetching unpinned upstream source.

All native integration in this architecture belongs in the root `TotalCrossVM/CMakeLists.txt`. The legacy `TotalCrossVM/src/jni/Android.mk` and `TCVM.xcodeproj` are not build inputs for this plan and must not be edited or used for validation. Android NDK and Apple toolchains are selected through the root CMake project.

`TC_ENABLE_SLJIT_JIT` controls the optional `tcir_jit` library and is off by default. The current Android arm64-v8a Gradle build deliberately passes it as `ON` so NDK builds compile the backend while runtime selection remains absent. The verified Android configuration is NDK `28.2.13676358`, target `aarch64-none-linux-android23`, compile/target SDK 35, and minSdk 23.

## Backend interface

Backends receive only verified TCIR and a versioned runtime ABI table. The conceptual interface is:

```c
typedef struct TCIRFunction TCIRFunction;
typedef struct TCBackendOptions TCBackendOptions;
typedef struct TCCompiledArtifact TCCompiledArtifact;

TCCompileResult tcBackendCompile(
    const TCIRFunction *function,
    const TCBackendOptions *options,
    TCCompiledArtifact **artifact);

TCCompiledStatus tcCompiledInvoke(
    const TCCompiledArtifact *artifact,
    Context context,
    Method method,
    TCCompiledFrame *frame,
    TValue *result);
```

Milestone 5 implements this boundary as `TCIRJitArtifact`, `tcirJitCompile`, `TCCompiledFrame`, `TCCompiledResult`, `tcirJitInvoke`, and explicit artifact disposal under `TotalCrossVM/src/tcvm/jit`. These properties remain required:

- compile failure is structured and non-fatal;
- an artifact records IR version, runtime ABI version, target, feature flags, code ownership and entry address;
- invocation returns normal, exception-pending, or pre-execution fallback status;
- no fallback is legal after the method has produced an observable effect;
- artifact publication is atomic and thread-safe; and
- shutdown releases executable memory only after no context can enter it.

## ABI alternatives and decision

| ABI | Benefits | Risks | Decision |
|---|---|---|---|
| mirror the public variadic `executeMethod` ABI | minimal-looking call site | unsafe to generate, type-dependent varargs, poor mixed-call and GC contract | rejected for compiled internals |
| pass all values in a boxed `TValue[]` | simple and portable | boxing/copy overhead, loses typed banks | useful only at external/testing boundary |
| frame-based ABI over `Context` typed arenas | matches current frames and GC scanner; predictable thunks | constrains register allocation and requires careful arena refresh | selected for baseline |

`TCCompiledFrame` describes base/count for `regI`, `regO`, and `reg64`, caller return destination, TC PC, and call metadata. It aliases regions reserved through the existing `Context`; it does not expose a native stack pointer as a managed root. Public native APIs remain unchanged.

## Runtime helper ABI

All non-trivial VM operations go through a versioned table or link-visible functions with declared effects. Categories include:

- symbol/class/field/method resolution;
- object and array allocation;
- checked array and field access where direct lowering is unsafe;
- direct, virtual, interpreted, compiled and native method calls;
- exception creation and propagation;
- cast/instanceof;
- monitor operations;
- safepoint/poll when introduced; and
- tracing/profile counters.

Helpers must document argument ownership, whether they may allocate/GC/throw/lock, and where returned references remain rooted. A backend must not duplicate private VM layouts merely to avoid a helper until the layout is an explicit ABI.

## Baseline JIT flow

1. A method becomes eligible by explicit option or simple threshold.
2. A per-`Method` side table atomically claims verification/compilation.
3. The frontend builds and verifies TCIR.
4. The backend rejects any unsupported TCIR operation before code emission.
5. SLJIT emits code into writable memory, resolves fixups, changes memory to executable according to the platform protocol, flushes instruction cache if required, and publishes the entry atomically.
6. Other threads continue interpreting while compilation occurs, then observe either `ready` or a cached failure.
7. The dispatcher invokes ready code through the frame ABI.

The POC supports integer constants/copies, integer arithmetic, comparisons, conditional/unconditional branches, loops, and integer/void return. Direct calls may be the next slice. Unsupported methods remain entirely interpreted.

Milestone 5 implements steps 3–6 as a standalone backend and test cache. It reruns the canonical verifier and a whole-method eligibility pass before creating an SLJIT compiler, publishes no artifact on any rejection/emission failure, and returns `COMPILING` to competing cache lookups so their caller can retain interpreter execution. Production thresholding and dispatcher invocation in steps 1, 2, and 7 remain Milestone 7 work.

Tiering, background compilation and hot counters are deliberately later milestones. Correctness and deterministic eligibility come first.

## Executable-memory policy

JIT memory handling is a platform security boundary. `tcir_jit_memory.c` is the only backend path that finalizes or disposes executable code. It rejects SLJIT builds without `SLJIT_WX_EXECUTABLE_ALLOCATOR=1` or with `SLJIT_PROT_EXECUTABLE_ALLOCATOR=1`; SLJIT owns platform write/finalize/execute transitions and instruction-cache synchronization. The host test inspects the finalized entry mapping and requires executable permission without writable permission.

On Apple Silicon macOS, Apple's [JIT porting guidance](https://developer.apple.com/documentation/apple-silicon/porting-just-in-time-compilers-to-apple-silicon) describes `MAP_JIT`, the Hardened Runtime JIT entitlement, write-protect transitions, and architecture-specific requirements. The default project policy should therefore be:

| Platform | Baseline JIT default | AOT direction | Gate |
|---|---|---|---|
| Linux x86-64/aarch64/armv7 | experimental opt-in | supported | executable-memory and CI tests |
| Windows x86/x86-64 | experimental opt-in where built | supported | allocation/protection/unwind tests |
| macOS arm64 | off unless correctly entitled/configured | supported | forced host execution/W^X passed; Hardened Runtime and distribution review remain |
| Android arm64-v8a | compiled but not selected | supported | NDK r28c/API 23 build passed; device and Play/security validation remain |
| iOS arm64 | off by project policy | primary path | static AOT, signing and App Review review |

Apple's [App Review Guideline 2.5.2](https://developer.apple.com/app-store/review/guidelines/#software-requirements) restricts downloading, installing, or executing code that changes app functionality. This plan does not claim that every form of JIT is universally prohibited by the OS; it chooses JIT-off for iOS until entitlement, distribution, and legal/product policy are reviewed. Generated C compiled and statically linked as part of the app build is the intended path.

## Generated-C AOT flow

The AOT backend translates verified TCIR into deterministic, portable C that calls the same runtime helper ABI. It is a build tool, not a runtime C compiler.

Generated artifacts live in the build directory and include:

- one or more `.c` translation units;
- a registry mapping stable method identities to entry functions;
- a manifest containing tool/IR/ABI version, input content hash, target options, supported/unsupported methods and diagnostics; and
- optional line maps from generated C to TC PC/source line.

The generated functions use the frame ABI and explicit labels/branches. Names derive from stable hashes plus readable escaped components, never addresses or iteration order. Sorting inputs and symbols ensures two identical invocations produce byte-identical C and manifests.

At startup/class load, the static registry matches class, method name and signature plus an artifact identity/hash. A mismatch leaves the method interpreted. AOT must never bind solely by a mutable constant-pool pointer or index from another process.

## Mixed-mode calls

All directions are required:

- interpreter → compiled: dispatcher finds a published artifact and invokes its frame entry;
- compiled → interpreted: a call thunk asks the dispatcher to execute the target using the existing `Context` frame protocol;
- compiled → compiled: initially through the same thunk, optionally direct after stable publication/invalidation rules;
- compiled → native: through the runtime native-call helper and `NMParams` contract.

This keeps the first backend simple and preserves stack traces, exceptions and GC roots. Direct-call patching and inline caches are later optimizations with explicit invalidation rules.

## Exception and GC contract

Before every helper that may throw or collect, generated code publishes TC PC and spills every live managed reference to its `regO` home. It then calls the helper and immediately checks `context->thrownException`. On exception it returns `TC_COMPILED_EXCEPTION`; the shared dispatcher searches handlers/unwinds using TotalCross method and PC metadata.

The baseline does not use native unwinding, conservative stack scanning or hidden references in callee-saved CPU registers. A future stack-map mode is a separate ABI version.

## Cache, invalidation and lifecycle

The first JIT cache is in-memory only. The Milestone 5 side table accepts an opaque method key supplied by its future runtime adapter and records `COMPILING`, `READY`, or `REJECTED`; only the claiming thread can publish or reject. A mutex makes state publication atomic, the cache owns only successfully published artifacts, and shutdown defers final disposal until outstanding claims resolve. The full runtime identity must later include method bytecode content, IR/runtime/backend versions, target architecture, and semantic flags. Machine-code serialization is not enabled merely because SLJIT exposes serialization; persistent cache requires threat modeling, signature/integrity, ASLR/relocation, CPU feature and app-version design.

Compiled artifacts are invalidated on VM shutdown and any future class unloading/redefinition event. Because current behavior must be confirmed for those events, the POC side table owns artifacts without altering serialized `TMethod` layout.

## Testing and observability

Every supported operation needs:

- TC bytecode interpreter vs TCIR interpreter comparison;
- TCIR interpreter vs JIT comparison on each enabled target;
- generated-C compile/run comparison;
- exception, TC PC, GC-at-helper and mixed-call variants;
- deterministic IR/C golden tests; and
- a forced-backend mode that fails tests when an expected method falls back.

Counters should expose methods seen/verified/compiled/fallen back, code bytes, compile time, runtime time and fallback reasons. Logging is opt-in and bounded.

The current forced tests compile converter-produced `add`, `abs`, and `sumTo` and fail on any fallback. The Milestone 4 corpus now performs 1,179 fresh-state comparisons across `executeMethod`, TCIR interpretation, and SLJIT. Separate tests cover invalid/unsupported rejection, deterministic emission failure, repeated create/dispose, competing lookup plus interpreter progress, publication, shutdown with an outstanding claim, and W^X permissions.

An additional macOS checkpoint is available through the off-by-default `TC_BUILD_IR_BENCHMARKS` option and `run-tcir-jit-benchmark` target. It requires Release plus SLJIT and sequentially runs 60-, 200-, and 1,000-sample profiles after 5, 10, and 20 warmups. Each profile rotates through all six backend orders with occurrence counts differing by at most one, excludes no outliers, and validates every batch against the `executeMethod` checksum. The paired JSON/CSV validator independently recomputes statistics and checks all 720, 2,400, and 12,000 CSV rows. At revision `77d179edc` on the recorded Apple M1 Pro/Low Power Mode host, every profile found SLJIT mean execution slower than `executeMethod` for short `add` (36.577%–48.120%) and `abs` (41.798%–45.920%), but faster for `sumTo(65537)` (28.036%–30.472%, or 1.390x–1.438x). This is a revision- and profile-keyed standalone-API baseline that includes current per-invocation verification and scratch allocation, not production-dispatch or application performance evidence; the ExecPlan records the six raw artifact paths, hashes, complete conditions, compilation statistics, and unfiltered scheduler variance. Future checkpoints must repeat all three profiles.

## Readiness for an optimizing backend

LLVM or Cranelift should be evaluated only after the full baseline subset has differential tests on at least Linux x86-64, Linux aarch64, Windows and macOS; the helper ABI and IR versioning have survived one release cycle; GC roots and exception PCs have stress coverage; and a backend conformance suite can run without knowledge of SLJIT. The decision must compare binary size, build complexity, licensing, supported targets, compile latency and debugging—not only peak throughput.

### Reserved LLVM/Cranelift comparison

This is a future decision, not an implementation dependency. LLVM offers a broad configurable target set: its current official CMake documentation lists targets including AArch64, ARM, PowerPC, RISC-V, X86, and many others, and supports selecting a cross target through `LLVM_TARGETS_TO_BUILD`/target triples. That breadth makes it the stronger candidate when TotalCross must retain native 32-bit x86 and ARM, but exact ABI/JIT support still needs target tests. LLVM also brings a substantial C++ library/toolchain, build, packaging, and update surface. See the official [LLVM backend guide](https://llvm.org/docs/WritingAnLLVMBackend.html) and [CMake target configuration](https://llvm.org/docs/CMake.html#llvm-specific-variables).

The current Cranelift package exposes native codegen features for x86-64, Arm64, s390x, and RISC-V 64. Its x86 module is explicitly an x86-64 backend and rejects a non-64-bit pointer ABI, so it does not currently satisfy TotalCross native Windows x86 or Linux ARM32 coverage; Pulley32 is a portable interpreter target, not equivalent native code generation. Cranelift would add a Rust/crate integration and supplies its own SSA IR and verifier, but TotalCross would still own managed-reference homes, runtime calls, exception PC mapping, and GC integration. See the current [Cranelift ISA documentation](https://docs.rs/cranelift-codegen/latest/cranelift_codegen/isa/), [x86-64 backend source](https://docs.rs/cranelift-codegen/latest/src/cranelift_codegen/isa/x64/mod.rs.html), and [codegen feature list](https://docs.rs/crate/cranelift-codegen/latest/source/Cargo.toml.orig).

For either candidate, TCIR must preserve types, explicit CFG/exception edges, stable symbols, source/TC-PC metadata, helper effects, safepoints, and live-reference information. The adapter lowers verified TCIR into the optimizer's IR and continues to call the same versioned runtime ABI. An optimizer's native unwinding, GC, or object model must not silently replace the TCVM protocols.

Evaluation is authorized only when representative workloads show a material performance gap that local TCIR passes plus SLJIT/AOT cannot close. The written comparison must include all TotalCross platforms, native 32-bit support, JIT/AOT modes, dependency and distribution size, clean/incremental build time, compile latency, peak performance, debug/profiler support, licensing, security maintenance, and integration effort through the root `TotalCrossVM/CMakeLists.txt`.
