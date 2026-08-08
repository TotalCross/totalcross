<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Final Editorial Report: TotalCross IR, JIT, and AOT

Status: definitive end-of-plan factual handoff, finalized 2026-08-08.

## Editorial Summary

The branch answered its architectural question positively. TotalCross bytecode
can be translated into a typed, verified, backend-neutral intermediate
representation and executed through a reference interpreter, an SLJIT baseline
JIT, or deterministic generated C while the existing `executeMethod`
interpreter remains the semantic authority and whole-method fallback.

This is a completed architectural proof, not a claim of exhaustive opcode,
platform, performance, or release coverage. The compiler/runtime controls remain
default-off, unsupported methods remain wholly interpreted, and TCZ and
serialized `TMethod` layouts are unchanged.

The architectural conclusion is:

> TCIR is a valid backend/runtime IR for TotalCross bytecode and native execution, but it is intentionally positioned after Java-to-TotalCross lowering and therefore is not intended to replace a future Java-aware whole-program optimization layer.

No such Java-aware layer was designed or implemented by this plan.

## Original Plan versus Actual Outcome

The original plan proposed an architecture inventory, independent TCIR,
bytecode frontend, verifier, reference interpreter, baseline SLJIT JIT,
portable-C AOT, mixed-mode runtime integration, incremental semantic coverage,
and eventual production readiness.

The branch delivered the architecture inventory and the complete end-to-end
execution foundation. It then extended representative semantics far enough to
exercise pure computation, typed values, control flow, managed-reference
transport, checked failures, calls, allocation, GC-root publication, exception
status, and method-atomic fallback.

It did not deliver exhaustive TotalCross opcode coverage or production rollout.
Fields/class initialization, arrays, full handlers, virtual/interface calls,
monitors, legacy/special cases, broad platform qualification, publication,
packaging, security/distribution review, and default-selection policy are
explicitly deferred. Those were reclassified from mandatory milestones of this
architectural proof into a new continuation ExecPlan; they are not represented
as completed work.

## Architecture Delivered

TCIR sits after TotalCross bytecode decoding. A module owns stable symbols and
typed functions. Functions use explicit control-flow graphs, immutable values,
block arguments, source TC PCs, declared effects, and typed TotalCross register
homes. The canonical form is simplified SSA: it makes merges and value identity
explicit while retaining the homes required by the legacy frame and GC model.

The verifier is the mandatory boundary before any interpreter or backend. It
checks structure, ownership, types, terminators, block arguments, bytecode target
boundaries, effect declarations, exceptional destinations, return types,
non-null and unchecked-access proofs, and GC-visible reference homes. Stable
diagnostics and deterministic dumps make rejection observable and reproducible.

The reference interpreter executes verified TCIR independently of native code
generation and acts as the middle oracle between legacy `executeMethod` and
compiled backends. SLJIT supplies a small, optional baseline code generator with
centralized W^X finalization and artifact lifecycle. The AOT backend emits
deterministic restricted C, a registry, and a manifest into build directories;
it is not a runtime compiler or replacement class format.

Runtime ABI version 5 uses typed frame storage and opaque runtime thunks for
dispatch, pre-bound calls, and allocation. The runtime adapter owns an external
method side table and explicit off/IR/JIT/AOT/auto policy. `executeMethod`
remains the facade for interpreted, compiled, and native calls.

## What Changed

The main implementation lives in:

- `TotalCrossVM/src/tcvm/ir/` for TCIR, decoding, frontend, verifier,
  interpreter, compiled ABI, opcode registry, and runtime adapter;
- `TotalCrossVM/src/tcvm/jit/` for SLJIT compilation, cache, and executable
  memory handling;
- `TotalCrossVM/src/tcvm/aot/` and `TotalCrossVM/src/tools/tcaot.c` for
  deterministic portable-C generation and registry/manifest output; and
- `TotalCrossVM/src/tests/ir/` plus
  `TotalCrossSDK/src/test/java/tc/tools/converter/modernjava/` for converter
  fixtures, goldens, differential execution, backend/runtime contracts, and
  benchmark tooling.

Representative implemented families include i32/i64 arithmetic and control
flow, normalized-f64 arithmetic and stable integer-to-f64 conversions,
reference null/identity/transport, `SWITCH`, a pre-bound static `CALL_normal`
slice, and `NEWOBJ`. Checked integer/long/f64 division paths and `TEST_regO`
carry explicit throw/GC effects in TCIR; compiled backends reject ineligible
functions before execution. Exact opcode status is recorded in the compatibility
matrix and registry rather than inferred from family names.

## Important Decisions and Trade-offs

- TCIR is backend-neutral and consumes TotalCross bytecode rather than replacing
  the Java converter or TCZ format.
- Simplified SSA plus explicit typed homes was selected over a stack IR, raw
  mutable register IR, or immediate full optimizing SSA.
- The legacy interpreter remains the permanent compatibility fallback.
- Backend selection is whole-method and preflighted; fallback is illegal after
  an observable effect.
- Runtime helpers carry conservative effects until narrower behavior is proven.
  Live managed references are materialized in `regO` homes at `may_gc` points.
- A newly allocated object must be published to its destination root home before
  unlock. A returned pointer alone is not accepted as collector visibility.
- JIT, AOT, and compiled dispatch remain separately default-off. SLJIT CPU
  support is not treated as proof of platform executable-memory or distribution
  policy.
- Deterministic AOT identity is useful for regeneration and exact lookup, but
  the current FNV-based semantic hash is not a security signature.

## Surprises and Discoveries

TotalCross PCs are 4-byte slot indexes, and calls, switches, and multidimensional
arrays own continuation slots. Correct decoding therefore requires marking
continuations before validating branch, handler, or line targets.

The converter has already normalized Java `float` into the 64-bit floating
bank, so post-bytecode TCIR cannot recover exact Java `float` identity. Several
legacy floating-to-integer/remainder C paths also have target-dependent behavior
outside well-defined ranges; those operations correctly remain fallback instead
of being assigned invented portable semantics.

The disabled compiled-dispatch policy initially acquired a mutex for every
method and materially slowed small interpreter calls. Benchmarking exposed the
regression, and a lock-free off path restored the expected baseline. The same
benchmark does not measure reference, call, allocation, or other later families.

Reference transport required no heap-layout knowledge, but helper-bearing
semantics exposed stronger contracts. Null checking required visible live homes
at the exception/GC boundary. Static calls required typed call-shape metadata and
pre-bound targets. Allocation required destination publication before unlock.
`THROW` could not be lowered as a simple terminal transfer because the legacy
path may fill a stack trace and select a handler after allocation-capable work.

## Validation and Measurable Results

The final representative corpus contains 15 production-converter-backed
fixtures. Eligible paths retained 6,398 fresh-state comparisons across legacy
`executeMethod`, TCIR, SLJIT, and generated C. Allocation added 16 separate
TCIR/SLJIT/AOT contract comparisons because the isolated harness cannot provide
a trustworthy real-VM `executeMethod` object-memory oracle.

The allocation checkpoint passed eight focused Release tests and eight ASan
tests on macOS arm64, focused UBSan runtime execution, the SDK converter fixture,
and a dispatch-disabled seven-test build with no runtime target or symbols.
Android NDK r28c/API 23 compiled the TCIR, SLJIT, AOT, runtime adapter, and
conditional VM hook for arm64-v8a. Earlier checkpoints also covered deterministic
AOT regeneration/manifest validation, W^X mapping checks, mixed-mode lifecycle
and concurrency, exact pending-exception/TC-PC status, and default-off isolation.

Historical 60/200/1,000-sample arithmetic profiles discovered the disabled-path
mutex regression and verified its correction. They are longitudinal evidence
for the measured `add`, `abs`, and `sumTo` paths only; they are not evidence of
call, allocation, or application-level performance.

The compact record is `.agent/evidence/totalcross-ir-jit-aot.md`. Historical
commands, measurements, hashes, and milestone context remain available through
`.agent/archive/exec-plan-totalcross-ir-jit-aot-history.md` and the preserved
`ba6d2f0c3` plan snapshot.

## Limitations

The allocation harness proves status, root transport, and publication against
opaque tokens. It does not initialize the real TCZ loader, class initializer,
or object memory manager and does not force moving collection, arena growth, or
arbitrary-native thread suspension.

Full exception/handler execution, stack-trace equivalence, fields, class
initialization, arrays, virtual/interface and unresolved calls, monitors,
`JUMP_regI`, reflection-sensitive cases, and remaining opcodes are not generally
implemented in TCIR/backends. Their methods remain wholly interpreted.

Host execution is concentrated on macOS arm64. Android arm64 evidence is
compilation, not device execution. Linux/GCC, Windows/MSVC, full iOS application
linkage, dead stripping, signing, entitlements, device execution, production
class-loader publication, packaging, and representative application benchmarks
remain unvalidated. No JIT/AOT backend is production-ready or enabled by
default.

## Deferred Work

Deferred work is owned by
`.agent/exec-plan-expand-tcir-semantic-coverage-and-production-readiness.md`:

- remaining bytecode families and explicit fallback classifications;
- real class initialization, heap, GC, exception-handler, dispatch, and monitor
  contracts;
- optimizer-readiness improvements justified by actual consumers;
- Linux, Windows, Android-device, macOS distribution, and iOS AOT validation;
- secure artifact identity, AOT publication, class-loader integration,
  packaging, observability, and backend default decisions; and
- release-hardening evidence and product workload measurements.

Execution order is intentionally undecided. Separate work is expected to
evaluate Java-level whole-program optimization and a possible high-level IR.
That investigation may make higher-level optimization, selected TCIR semantics,
or productionization more urgent than exhaustive opcode coverage.

## Lessons for Future Work

Start each semantic family by tracing the existing interpreter and all
transitive helper effects. Add frontend, verifier, interpreter, backend
eligibility, focused negative/fallback tests, and a trustworthy oracle together.
Do not infer heap, exception, or GC behavior from signatures or opaque harnesses.

Keep method-atomic preflight and default-off controls until real application
corpora and platform policies justify broader selection. Record evidence at the
scope it actually proves: cross-compilation is not device support, an allocation
token is not an object-memory-manager stress test, and an arithmetic benchmark
does not measure unrelated operations.

Preserve TCIR's runtime/backend focus. Optimization passes should be added only
when exact TotalCross semantics and effect ordering are proven and a backend or
measured workload needs them. Java-aware whole-program work should retain its
own information-rich boundary rather than attempting to reconstruct Java facts
after TotalCross-bytecode lowering.

## Suggested Follow-up Architecture and Workstreams

1. Decide the relationship and priority among Java-level whole-program/HIR
   investigation, selected TCIR semantic expansion, and runtime productionization.
2. If semantic expansion is selected, implement one bounded family at a time,
   beginning only after its helper, GC, exception, and oracle contract is known.
3. Build a product corpus and fallback telemetry before choosing breadth or
   backend defaults.
4. Qualify platform security and distribution models independently from CPU
   code-generation capability.
5. Define automatic AOT/JIT artifact identity, publication, invalidation, and
   packaging only after class-loader and lifecycle ownership are explicit.

## Claims Requiring Human Review

Any product-performance, supported-platform, security-policy, licensing,
distribution, or release-readiness statement still requires normal technical,
legal, product, and editorial review. The supported final claim is narrower and
complete: the branch successfully validated TCIR as a backend/runtime
architecture with safe incremental fallback.
