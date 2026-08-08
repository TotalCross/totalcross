<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Expand TCIR semantic coverage and production readiness

Status: proposed continuation; execution has not started. Activate only after
the priority and Java-level optimization decision gate in Milestone 0.

This ExecPlan follows `.agent/PLANS.md` and `AGENTS.md`. It is a new plan built
on the completed architectural foundation in
`.agent/exec-plan-totalcross-ir-jit-aot.md`; it does not reopen or rename that
plan.

## Purpose / Big Picture

The completed TCIR/JIT/AOT architectural plan proved that verified TotalCross
bytecode can feed a reference interpreter, an optional SLJIT JIT, deterministic
portable-C AOT, and default-off mixed-mode runtime dispatch while unsupported
methods remain in the legacy `executeMethod` interpreter.

This continuation turns that foundation into broader semantic and production
coverage when product/compiler priorities authorize it. The observable outcome
is not merely a larger opcode count: selected real application methods should
execute through TCIR and eligible native backends with legacy-equivalent
results, exceptions, GC behavior, calls, synchronization, lifecycle, and
diagnostics on the platforms the project decides to support.

Execution order is intentionally undecided. Separate work is expected to
evaluate Java-level whole-program optimization and a possible high-level IR
(HIR). That investigation may make Java-level optimization, a particular TCIR
family, AOT productionization, or platform work more valuable than exhaustive
TCIR opcode coverage. Milestone 0 must record the decision before any field,
array, handler, call, monitor, optimizer, or rollout implementation begins.

Throughout this plan, the legacy interpreter remains the semantic authority and
whole-method fallback. `TC_ENABLE_COMPILED_DISPATCH`, `TC_ENABLE_SLJIT_JIT`, and
`TC_ENABLE_C_AOT` remain default-off until explicit release evidence and product
policy justify a change.

## Working Set and Resume Protocol

Until activation, this file is the only live record for this continuation. Its
first resume action is Milestone 0, not implementation. When execution is
authorized, create supporting records under `.agent/state/`, `.agent/evidence/`,
`.agent/archive/`, and `.agent/reports/` using the plan stem
`expand-tcir-semantic-coverage-and-production-readiness`. The state is the
compact first read; evidence is append-only; history receives retired detail;
and the editorial report is updated only at major product/architecture
boundaries and completion.

Stable foundation records are:

- `.agent/state/totalcross-ir-jit-aot.md`: compact completed-state facts and
  limitations; read this first when verifying the inherited baseline.
- `.agent/evidence/totalcross-ir-jit-aot.md`: selectively searched evidence for
  the architecture proof; do not copy its measurements as new-plan evidence.
- `.agent/archive/exec-plan-totalcross-ir-jit-aot-history.md`: historical
  milestone decisions and links to the unabridged snapshot; read only when a
  prior contract or result must be reconstructed.
- `.agent/reports/totalcross-ir-jit-aot-editorial.md`: definitive explanation of
  what the foundation proved and did not prove.
- `docs/architecture/bytecode/totalcross-ir-design.md`: TCIR value/CFG/verifier
  contract.
- `docs/architecture/bytecode/jit-aot-architecture.md`: backend, runtime ABI,
  security, lifecycle, and platform boundary.
- `docs/architecture/bytecode/compatibility-matrix.md`: exact opcode and target
  dispositions; update it with every supported/fallback change.
- `TotalCrossVM/src/tcvm/ir/tcir_opcode_registry.def`: machine-readable opcode
  shape/lowering/status authority.

After activation, normal continuation starts with the new state file and only
then reads the active milestone and relevant source paths. Do not reread the
completed plan, archive, or raw evidence by default.

## Progress

- [x] Architectural prerequisite: TCIR version 1, verifier, bounded frontend,
  reference interpreter, SLJIT, deterministic C AOT, runtime ABI version 5,
  default-off mixed dispatch, representative semantics, and safe fallback were
  delivered and accepted by the completed predecessor plan.
- [x] Continuation boundary: exhaustive semantic coverage and production
  readiness were separated from the completed architecture proof.
- [ ] Milestone 0: decide priority and boundaries relative to Java-level
  whole-program optimization/HIR work; choose one authorized first workstream.
- [ ] Milestone 1: refresh the application corpus, opcode/fallback telemetry,
  helper-effect map, and platform/product requirements needed by that workstream.
- [ ] Milestones 2–6: implement only the semantic families selected by the
  priority decision, using one bounded effect contract at a time.
- [ ] Milestone 7: improve TCIR optimization readiness only where a selected
  consumer or measured workload justifies it.
- [ ] Milestone 8: validate selected platforms, security/distribution policy,
  lifecycle, observability, and production AOT/JIT integration.
- [ ] Milestone 9: decide packaging, publication, defaults, and release scope
  from measured product evidence; complete the report and retrospective.

No semantic-family implementation is active. Fields and class initialization
are candidates, not an assumed next slice.

## Current Architecture and Scope

TCIR begins after Java has been converted to TotalCross register bytecode. It
models TotalCross runtime semantics through typed values, explicit CFG edges,
simplified-SSA block arguments, typed register homes, stable symbols, source TC
PCs, exceptional destinations, and declared effects. The verifier is mandatory
before interpretation or backend entry.

The reference interpreter is the semantic middle oracle. SLJIT and generated C
consume only verified whole functions that pass backend preflight. The runtime
adapter owns method registration, policy, diagnostics, artifacts, and lifecycle
outside serialized `TMethod`; `executeMethod` remains the facade and legacy
fallback.

Runtime ABI version 5 provides typed frame cells plus dispatch, pre-bound call,
and allocation thunks. At a `may_gc` boundary, all live managed references must
be visible in `Context.regO` homes. Helper-bearing operations publish exact TC
PCs, return explicit normal/thrown/pre-execution-fallback status, and reload
arena bases after helpers that may grow them. Allocation publishes its result in
the destination home before unlock.

The implemented representative subset covers pure i32/i64/normalized-f64
operations and stable conversions, managed-reference identity/transport, switch,
pre-bound static calls, null checks, and object allocation. Some checked
arithmetic/null operations remain TCIR-only; compiled backends reject their
functions before entry. Fields, arrays, complete handlers, virtual/interface
dispatch, monitors, legacy indirect control, and other special cases remain
method-atomic interpreter fallback.

TCIR is a backend/runtime IR, not a Java-aware whole-program IR. A future HIR
investigation may decide that optimization occurs before TotalCross bytecode,
may define a separate lowering boundary, or may reject a new layer entirely.
This plan must consume that decision; it must not invent the HIR or retrofit
lost Java facts into TCIR.

## Plan of Work

### Milestone 0: priority and Java-level optimization decision gate

Record the current product/compiler objective, representative applications,
target platforms, acceptable fallback rate, deployment constraints, and why the
selected first workstream has priority. Obtain or reference the separate
Java-level whole-program optimization/HIR conclusion when available.

The gate must answer only what this continuation needs:

- whether Java-level optimization/HIR work precedes, runs independently from,
  or supplies requirements to TCIR work;
- which facts remain available at the TCIR boundary and which belong only above
  it;
- whether the immediate need is semantic breadth, a specific application
  blocker, AOT publication, JIT policy, a platform gate, observability, or an
  optimization-ready TCIR contract; and
- which workstreams are explicitly deferred so “complete all opcodes” is not an
  accidental priority.

Do not design or implement the HIR here. Acceptance is a written decision, one
selected first workstream with measurable behavior, updated progress/state, and
no source implementation before that decision.

### Milestone 1: evidence-driven baseline refresh

Build the smallest real application/method corpus that represents the chosen
objective. Record per-method fallback reasons and opcode combinations rather
than relying only on family counts. Refresh the compatibility matrix and helper
effect map only where repository/runtime changes since the predecessor plan
require it.

Trace each candidate's legacy `executeMethod` handlers and transitive helpers,
including resolution, class initialization, allocation/GC, exception creation,
handler selection, locks, volatile/atomic behavior, native transitions, arena
growth, and lifecycle ownership. Select a bounded first slice with a trustworthy
oracle and explicit unsupported boundary.

Acceptance is an evidence-backed slice choice, red/fallback fixture, stable
oracle, documented effects, target platform set, validation level, and rollback
path. If the contract remains ambiguous, retain fallback and choose a better
bounded slice.

### Milestone 2: fields and class initialization, if selected

Trace instance/static reads and writes across all three register banks. Define
stable field/class symbols, resolution and class-initializer ordering, null and
missing-field exceptions, reference rooting, heap read/write effects,
volatile/atomic/platform behavior, and invalidation/lifetime rules.

Prefer runtime helpers until direct layout access is an explicit supported ABI.
Preflight every binding needed to avoid fallback after a read/write or class
initializer effect. Add converter-backed fixtures, verifier/frontend rules,
reference execution, backend eligibility, precise TC-PC/exception cases,
forced-GC tests where the real runtime can provide them, and method-atomic
fallback negatives.

Acceptance is limited to the exact field shapes and class-initialization cases
proven. Unsupported variants remain classified fallback.

### Milestone 3: arrays and type operations, if selected

Cover array length, checked and unchecked loads/stores, primitive element
width/sign behavior, object references, single and multidimensional allocation,
`INSTANCEOF`, and `CHECKCAST` in bounded slices.

Preserve the legacy checked/unchecked distinction. An unchecked access requires
a verifier-visible dominating proof or validated bytecode precondition. Model
null, bounds, negative size, partial multiarray construction, OOM/GC, current
reference-store compatibility behavior, class resolution, and precise TC PCs.
Do not carry an interior pointer across a safepoint.

Acceptance requires differential boundary fixtures and real helper/GC evidence
for each advertised shape. Family names alone do not promote every opcode.

### Milestone 4: exceptions and handlers, if selected

Implement `THROW` together with exception object handling, stack-trace creation,
live-root publication, pending-exception transfer, precise TC PC/source mapping,
handler selection, handler-entry arguments, nested calls, and unwind/frame
restoration. Do not lower `THROW` as a terminal assignment that omits legacy
`fillStackTrace` or `handleException` behavior.

Test handled and propagated exceptions, allocation during trace creation,
matching/non-matching ranges, nested interpreted/compiled transitions, forced
GC, and stable observable exception details. Native unwinding or stack maps are
new ABI designs, not implicit extensions.

Acceptance requires legacy-versus-TCIR and TCIR-versus-backend equivalence for
the advertised handler shapes. Otherwise the entire method falls back.

### Milestone 5: virtual/interface/lazy calls, if selected

Extend the pre-bound static-call foundation only after defining receiver null
checks, class/method resolution, interface behavior, dispatch caches,
interpreted/compiled/native targets, typed references and returns, GC/suspension,
exception propagation, binding lifetime, unloading/redefinition invalidation,
and concurrent publication.

Prefer the existing versioned thunk until direct-call patching has explicit
invalidation and concurrency rules. A backend must resolve all required call
metadata before entry or prove that the runtime thunk can complete without a
method restart.

Acceptance includes all mixed-mode directions, native bridges, forced failures,
exact fallback reasons, and no duplicate call effects.

### Milestone 6: monitors and legacy/special cases, if selected

Trace all four monitor opcodes, ownership, null/error paths, unwinding,
collector/thread interaction, platform locks, and the current distinction from
unsupported method-level synchronization. Investigate `JUMP_regI` through real
shipped `jsr`/`ret` artifacts; compile only when targets and state can be proven.

Classify obsolete, platform-specific, reflection-sensitive, generated, and
otherwise special operations individually. “Fallback” is an acceptable final
product decision when documented and observed; silent missing coverage is not.

Acceptance is a complete disposition for the selected corpus and exact tests
for every operation newly advertised by TCIR or a backend.

### Milestone 7: optimization readiness where justified

Do not add a general optimizer merely because TCIR uses simplified SSA. Start
from the HIR decision, backend needs, and measured product workloads. Candidate
local passes include exact constant folding, copy propagation, dead pure
operations, branch simplification, and bounds-check reuse with dominance proof.

Every pass is independently toggleable, deterministic, verified before and
after, and differential-tested with exact Java/TotalCross integer and IEEE
semantics. No pass may reorder `may_throw`, `may_gc`, resolution, locks,
volatile-like operations, calls, or heap effects without a documented memory
and effect model.

If multiple backends require a common lowering not expressible cleanly in TCIR,
record whether TCIR needs a versioned extension or a lower backend IR. Do not
reshape TCIR to mimic an unimplemented HIR.

### Milestone 8: platform and production integration

Choose platforms from current product policy rather than the predecessor's
aspirational matrix. Validate relevant Linux, Windows, macOS, Android, and iOS
build/run paths, including 32-bit constraints where still supported. Record
unavailable runners honestly. Keep JIT off on iOS unless project policy changes
after legal/security/product review; generated and statically linked AOT is the
default direction there.

Define executable-memory policy, entitlements, signing, static linkage, dead
stripping, artifact integrity, class-loader publication, invalidation,
unloading/shutdown, code/data ownership, observability, fallback telemetry, and
failure recovery. Persistent machine-code caching requires a separate threat
model and secure identity; SLJIT serialization support is not authorization.

Acceptance is platform-specific build and execution evidence plus explicit
distribution/security decisions. Cross-compilation alone does not promote a
platform to supported.

### Milestone 9: packaging, defaults, and release decision

Measure startup, compilation latency, steady-state performance, memory, code
size, package size, fallback rate, and representative application workloads for
the exact selected backend and platform. Historical arithmetic benchmarks are
reused only when their measured hot path changes; they are not a mandatory
ritual for unrelated semantics.

Decide how generated AOT sources/objects are produced, registered, packaged,
signed, invalidated, and published. Decide whether IR/JIT/AOT remains opt-in,
becomes application-selectable, or receives any platform default. A default
change requires explicit compatibility, GC/exception, application-corpus,
security, rollback, and support evidence.

Reconcile plan, state, evidence, archive, architecture docs, compatibility
matrix, and final editorial report. Record exact supported families/platforms
and preserve every intentional fallback.

## Decision Log

- Decision: Do not assume exhaustive TCIR opcode coverage is the immediate next
  priority.
  Rationale: Java-level whole-program/HIR work and product deployment needs may
  change the value and order of semantic or production work.
  Date: 2026-08-08.

- Decision: Require Milestone 0 before source implementation.
  Rationale: fields were the old plan's next slice, but that ordering predates
  the planned higher-level optimization investigation.
  Date: 2026-08-08.

- Decision: Keep TCIR after TotalCross bytecode and keep any Java-aware HIR
  investigation separate.
  Rationale: TCIR owns runtime/backend semantics after Java information has
  already been normalized; this plan must not fabricate lost information.

- Decision: Preserve default-off controls and whole-method fallback.
  Rationale: incremental semantic expansion must not change ordinary VM behavior
  or repeat effects after late rejection.

- Decision: Accept intentional per-family fallback as a valid product outcome.
  Rationale: compatibility and maintainability matter more than an unsupported
  claim of complete opcode compilation.

- Decision: Require real effect/GC/exception contracts before direct heap or
  helper lowering.
  Rationale: opaque tokens and signatures cannot establish collector, class
  initialization, handler, lock, or lifecycle behavior.

## Validation and Acceptance

Follow the four levels in `AGENTS.md` and stop at the first sufficient level.
During a semantic slice, use focused golden, negative, fallback, and oracle
tests. Before a functional commit, run affected differential fixtures,
`git diff --check`, and only the relevant sanitizer. At a family or runtime ABI
boundary, run the complete differential suite, Release, relevant sanitizer,
default-off isolation when dispatch changes, and only directly affected
cross-builds.

Milestone or release gates use the available selected platform matrix,
complete relevant sanitizers, packaging validation, and product workloads.
Benchmarks run only when the workload exercises the changed semantic or measured
hot path. Start full checkpoints at 60 and 200 samples; add more only when
variance or an explicit request justifies it.

For every newly advertised operation, acceptance requires:

- converter-backed or real-artifact input and bounded decoding;
- verifier/frontend rules and deterministic diagnostics/goldens;
- legacy `executeMethod` versus TCIR comparison where a trustworthy real setup
  exists;
- TCIR versus each eligible backend on selected targets;
- exact effects, TC PCs, exceptions, GC roots, arena reloads, calls, locks, and
  lifecycle behavior relevant to the operation;
- pre-execution rejection and whole-method fallback for unsupported shapes; and
- compatibility-matrix and opcode-registry updates that do not over-promote the
  surrounding family.

Save verbose output to task-specific logs and append one compact evidence
record. Record unavailable or deferred expensive validation with its reason.

The plan completes only after its Milestone 0 scope is executed and the selected
semantic/product objectives have evidence-backed outcomes. It need not compile
all 160 opcodes if the decision log and final report explicitly preserve
intentional fallback and explain the supported product boundary.

## Risks and Open Questions

- Java-level whole-program/HIR investigation may alter sequencing, ownership,
  or the useful optimization boundary. The gate prevents speculative TCIR work.
- Helpers may transitively allocate, collect, throw, lock, resolve, initialize,
  suspend, or grow arenas differently by platform. Conservative effects remain
  until traced and stressed.
- Non-moving-GC assumptions today do not authorize hidden native references or
  interior pointers across safepoints. Future collector changes may require a
  new ABI or stack-map design.
- Class/method unloading, replacement, redefinition, and binding lifetime remain
  unclear. Persistent entries and direct call/field bindings require explicit
  invalidation.
- Legacy floating casts/remainder, `THROW`, reference-array stores,
  method-level synchronization, and `JUMP_regI` contain observable compatibility
  details that must not be silently normalized.
- Platform CPU support does not establish executable-memory, signing,
  entitlement, store-policy, or distribution support.
- Generated artifact hashes and registries are not yet security boundaries.
- A small synthetic corpus can hide application fallback and performance
  behavior; production decisions require representative methods and telemetry.

## Idempotence and Recovery

All compiler/runtime features remain default-off. Re-run focused commands in
existing build directories unless stale output is suspected. Generated C,
manifests, dependency checkouts, native build output, logs, and benchmark samples
remain outside source control unless explicitly required.

Never restart an effectful method in the interpreter after backend entry. A
frontend, verifier, binding, or backend failure before entry records a bounded
reason and retains legacy execution. A generated identity mismatch publishes no
entry. Shutdown must wait for active compiled dispatches before freeing code.

Preserve unrelated local changes and inspect scoped status/diffs. Do not modify
the generated `TotalCrossVM/deps/totalcross-depot-tools` checkout as an
authoritative source. Native build integration belongs in root CMake, with iOS
dependency/linkage behavior following `AGENTS.md`.

If interrupted after activation, rewrite the new state file with the chosen
workstream, active slice, last logical commit, active paths, exact next action,
focused validation, deferrals, open decisions, blockers, and local exclusions.
Do not append a diary or mutate the predecessor's completed state.

## Outcomes & Retrospective

No continuation implementation has started. The plan currently records a safe,
resumable expansion path and, most importantly, prevents the predecessor's old
“fields next” ordering from outrunning the Java-level optimization/HIR and
product-priority decisions.

The inherited foundation is complete and remains usable independently: TCIR is
a proven backend/runtime IR with interpreter, SLJIT, portable-C AOT, mixed-mode
integration, representative semantic coverage, explicit effects, and safe
fallback. This plan will record its own outcomes only after Milestone 0 selects
an authorized objective and evidence demonstrates the resulting semantic or
production behavior.

## Revision Note

2026-08-08: created as a separate continuation when the original architectural
TCIR/JIT/AOT ExecPlan was closed successfully. The plan preserves future
semantic and production work without assuming it outranks upcoming Java-level
whole-program optimization/HIR investigation.
