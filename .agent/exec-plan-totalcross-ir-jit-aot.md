<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Build a verified TotalCross IR with baseline JIT and portable AOT backends

This ExecPlan follows `.agent/PLANS.md` and `AGENTS.md`. It is the active,
compact plan for remaining work. It names the state, evidence, archive, and
editorial records needed to resume safely without rereading completed detail.

## Purpose / Big Picture

TotalCross converts Java classes to compact register bytecode and executes that
bytecode in the C `executeMethod` interpreter. This work inserts a typed,
backend-neutral TotalCross intermediate representation (TCIR) after that
bytecode. TCIR has a reference interpreter, an optional SLJIT baseline backend,
and a deterministic portable-C AOT backend; the legacy interpreter remains the
compatibility oracle and permanent fallback.

At completion, a supported method can run through the legacy interpreter, TCIR,
SLJIT, or generated C and preserve results, exceptions, managed-reference
behavior, and advertised runtime effects. An experimental default-off runtime
policy selects only eligible methods. Unsupported or failed methods remain in
the legacy interpreter without changing TCZ files or default VM behavior.

Correctness, portability, diagnostics, and safe fallback take priority over
performance. Performance evidence is collected only when a workload exercises
the changed behavior or a measured hot path changes.

## Working Set and Resume Protocol

Normal continuation starts with
`.agent/state/totalcross-ir-jit-aot.md`. It identifies the active slice, last
functional checkpoint, next action, focused validation, deferrals, active paths,
and deliberate local exclusions. After reading it, locate headings in this file
and read only the active milestone, validation, and risk sections needed for the
next action.

`.agent/evidence/totalcross-ir-jit-aot.md` is an append-only, selectively
searched index of commands, results, logs, artifact locations, and limitations.
`.agent/archive/exec-plan-totalcross-ir-jit-aot-history.md` contains completed
milestone summaries and points to the unabridged pre-consolidation snapshot at
Git revision `ba6d2f0c3`; do not read it by default. The concise factual
handoff is `.agent/reports/totalcross-ir-jit-aot-editorial.md`; update it at a
material milestone checkpoint, not after each slice.

`AGENTS.md` and `.agent/PLANS.md` are read in full only when creating a plan or
when either changed. Their precedence is: safety; explicit user request; the
`AGENTS.md` token/output budget; `.agent/PLANS.md`; this plan. A context
compaction resumes through the state file, not a broad repository scan.

## Progress

- [x] Milestones 1–4: documented the bytecode/runtime architecture, implemented
  the owned TCIR contract and frontend, and established a reference interpreter
  plus real-`executeMethod` differential oracle. See archive and evidence.
- [x] Milestone 5: integrated pinned optional SLJIT, W^X code management, and
  Android arm64-v8a/API 23 compilation without changing runtime dispatch.
- [x] Milestone 6: implemented deterministic portable-C AOT, host generation,
  identity registry/manifest, and four-way differential execution.
- [x] Milestone 7: added default-off mixed-mode dispatch, a runtime side table,
  policy/lifecycle diagnostics, and a measured disabled-policy fast path.
- [x] Milestone 8: added selected i32/i64/f64 operations, reference transport,
  null checking, switch control flow, pre-bound static calls, and allocation.
- [x] (2026-07-18) Allocation checkpoint: commits `e7ea5cb14` and `051800dcd`
  added `NEWOBJ`, runtime ABI v5 allocation thunks, publication-before-unlock,
  and class preflight. See state and evidence for scope and validation.
- [x] (2026-07-18) Consolidated the ExecPlan process: state-first resumption,
  proportional validation, evidence index, history, and separate editorial
  handoff replace repeated slice narratives and mandatory disconnected
  benchmarks.
- [ ] Milestone 8 next slice: trace fields and class initialization, select a
  bounded lowering/fallback decision, and implement only after effects and GC
  roots are understood.
- [ ] Milestone 8 remaining families: arrays; exceptions/handlers;
  virtual/interface calls; monitors; legacy/special/reflection cases; explicit
  fallback decisions for every valid opcode.
- [ ] Milestone 9: platform, security, performance, publication, packaging, and
  final editorial/release evidence.

## Current Architecture and Scope

The native TCIR implementation is under `TotalCrossVM/src/tcvm/ir/`. Its owned
C API models functions, blocks, typed values, source TC PCs, declared effects,
and GC-visible reference homes. The verifier is the canonical structural and
type boundary; the frontend accepts only a bounded bytecode method view and
rejects unsupported methods before execution.

The reference interpreter evaluates verified TCIR through typed homes. The SLJIT
backend at `TotalCrossVM/src/tcvm/jit/tcir_jit.c` and generated-C backend at
`TotalCrossVM/src/tcvm/aot/tcir_aot.c` compile only whole functions they can
preflight. They never start an effectful function and then fall back. Generated
C remains build-directory output produced by `TotalCrossVM/src/tools/tcaot.c`.

`TotalCrossVM/src/tcvm/ir/tcir_runtime.c` owns optional mixed-mode registration,
policy, diagnostics, and dispatch. `TC_ENABLE_COMPILED_DISPATCH`,
`TC_ENABLE_SLJIT_JIT`, and `TC_ENABLE_C_AOT` are independent default-off build
options. Runtime state remains outside `TMethod`; `executeMethod` stays the
facade for interpreted, compiled, and native calls.

Runtime ABI v5 supplies direct dispatch, pre-bound call, and allocation thunks.
The allocation thunk receives class identity, live reference homes, destination
home, and TC PC. It may resolve/load/initialize a class, allocate, collect,
lock, throw, or grow arenas, so it has the full conservative effect boundary.
A successful allocation must be written to the destination `regO` home before
unlock. The standalone allocation harness proves that contract but does not
initialize a real TCZ/class-loader/object-memory-manager environment.

Converter fixtures live under `TotalCrossSDK/src/test/java/tc/tools/converter/
modernjava/` and `TotalCrossVM/src/tests/ir/`. Existing fixtures cover selected
numeric, reference, switch, static-call, and allocation behavior. The legacy
oracle remains applicable only where its real runtime setup exists; opaque-token
contract tests are explicitly labeled as such.

## Plan of Work

### Completed milestone summary

Milestone 1 produced architecture and compatibility documentation. Milestone 2
produced TCIR and validation infrastructure. Milestone 3 lowered a bounded
bytecode subset. Milestone 4 provided reference execution and differential
comparison. Milestone 5 added optional SLJIT. Milestone 6 added deterministic
portable C. Milestone 7 added controlled default-off dispatch. Completed
implementation, validation, benchmark, and decision detail is retained in the
archive and evidence index rather than repeated here.

### Milestone 8: complete semantic coverage incrementally

The next family is fields and class initialization. Before writing a lowering,
trace `TotalCrossVM/src/tcvm/tcfield.c`, `TotalCrossVM/src/tcvm/tcclass.c`, the
related headers, and the exact `executeMethod` cases. Identify resolution,
class-initialization, null/bounds/type checks, read/write, volatile/atomic,
exception, lock, and GC effects. Record whether each field operation is direct,
runtime-helper, unsupported-in-POC, future, platform-specific, obsolete, or
needs investigation in the compatibility matrix; no valid opcode may disappear.

Start with the smallest slice that has a stable oracle and one clear effect
contract. Add converter-backed fixtures, canonical TCIR, verifier checks,
reference execution, whole-method backend eligibility, negative/fallback tests,
and only the runtime boundary the slice requires. Do not access object layouts
directly until the GC/layout contract is demonstrated. If class initialization
or helper effects cannot be proven, retain method-atomic fallback and record the
reason rather than approximating legacy behavior.

After fields, use the same approach for arrays, exceptions/handlers,
virtual/interface calls, monitors, and legacy/special cases. Calls move before
direct heap access because helper-based calls exercise the ABI without assuming
heap layout. `THROW` remains fallback until stack-trace creation, live roots,
pending-exception transfer, and handler selection can be modeled together.

Coverage completes only when every valid opcode has a documented lowering or
fallback class, effects and exception edges are explicit, representative boundary
tests exist, and fallback is a conscious product/platform decision rather than
missing implementation.

### Next-slice procedure

1. Record the selected field/class-initialization opcode family and the exact
   uncertainty in the state file before editing source.
2. Trace only the legacy interpreter/helper paths needed to classify effects and
   choose one bounded fixture. Stop and retain fallback if the contract remains
   ambiguous.
3. Implement the frontend, verifier, reference interpreter, and focused tests
   together. Add a compiled backend only after whole-method eligibility and
   helper ABI requirements are explicit.
4. Run Level 1 while iterating and Level 2 before the functional commit. Update
   state with results and deferrals; do not update this plan yet.
5. At a family or ABI boundary, run the justified Level 3 checks, append one
   evidence record, consolidate this plan once, and archive retired detail.

### Milestone 9: platform, security, performance, and release readiness

Validate available macOS arm64, Linux x86-64/aarch64, Windows x86/x86-64,
Android arm64-v8a, Linux armv7 where supported, and iOS arm64 AOT paths. Record
unavailable targets with reasons. Do not enable JIT on iOS. For macOS and Android
record platform-security and distribution constraints; native integration remains
in `TotalCrossVM/CMakeLists.txt`, not legacy `Android.mk` or checked-in
`TCVM.xcodeproj`.

Only after semantic/platform correctness passes, evaluate backend defaults using
startup, compile latency, steady-state execution, memory/code size, product
workloads, and policy. Run a full benchmark only when it measures changed work
or a measurement regime changes. Reconcile the evidence index, archive, active
plan, and editorial report before declaring completion.

## Decision Log

- Decision: Keep every backend and mixed-mode dispatch default-off.
  Rationale: experimental compilation must not alter ordinary VM behavior.

- Decision: Preserve `executeMethod` as facade and the legacy interpreter as
  permanent compatibility fallback.
  Rationale: compiled paths reuse established call/native behavior and unsupported
  methods retain known semantics.

- Decision: Make backend selection and fallback whole-method and preflighted.
  Rationale: no observable effect may execute twice after a late rejection.

- Decision: Model helper-bearing calls and allocation with conservative effects,
  live `regO` homes, TC-PC publication, and arena-base reload requirements.
  Rationale: correctness precedes direct layout access or optimization.

- Decision: Require allocation publication in the destination home before
  unlock, and treat opaque allocation tests as contract evidence only.
  Rationale: a helper result alone is not a collector-visible root; no full OMM
  setup exists in the standalone harness.

- Decision: Retain checked arithmetic/null checks as TCIR-only until compiled
  helper/GC evidence exists.
  Rationale: whole-method rejection avoids partial exception effects.

- Decision: Use state-first continuation, an evidence index, milestone archive,
  and separate editorial report.
  Rationale: resumability needs stable references, not repeated historical text.

- Decision: Use proportional validation and benchmark only measured changes.
  Rationale: confidence must be proportional to risk and a disconnected workload
  cannot prove performance of a new semantic operation.

## Validation and Acceptance

Use the four validation levels in `AGENTS.md`. During implementation, run the
smallest affected build/test/golden/negative case. Before a functional commit,
run focused module tests, affected differential fixtures, `git diff --check`,
and only the relevant sanitizer. At a field-family boundary or ABI change, run
the complete differential suite, Release, relevant sanitizer, default-off
isolation when dispatch changes, and only directly affected cross-builds. Save
full output to logs and record compact results in the evidence index.

For fields and class initialization, acceptance must include the legacy oracle
where a real setup is available; precise TC PC for exceptions; correct reference
home materialization at every `may_gc` helper; class-resolution and initializer
ordering; and method-atomic fallback for unsupported bindings. Do not claim
moving/forced GC, class initialization, or layout correctness from an opaque
contract harness.

Run a default-off build only when a conditional build/runtime boundary changes.
Run Android or another cross-build only when the edited code is compiled for that
platform or the user requests it. Device execution, full platform matrices,
packaging, and broad sanitizers are Milestone 9/release work unless the user
requests them or a specific change requires them.

The historical `add`, `abs`, and `sumTo` benchmark does not measure fields,
references, calls, switches, or allocation. Do not run it for such slices unless
their dispatcher, invocation ABI, frame, scratch allocation, or backend-emission
hot path changes. For an affected hot path, use a smoke benchmark with about
three warmups and ten samples. Full checkpoints begin at 60 and 200 samples;
more than 200 requires observed variance or an explicit user request.

## Risks and Open Questions

Fields may implicitly resolve symbols, initialize classes, allocate exceptions,
trigger GC, or use platform-specific volatile/atomic behavior. The next slice
must trace those paths instead of assuming a raw offset load/store is safe.

The current runtime still lacks forced/moving GC, arena-growth, and thread
suspension evidence inside arbitrary native helpers. Every helper-bearing slice
must define what it proves and leave unproven dynamic behavior open.

Class/method unloading, replacement, and redefinition lifetime guarantees remain
unknown. Do not add persistent direct bindings or embedded compiled-entry fields
without resolving them. Likewise, inspect monitor semantics, `JUMP_regI`/`jsr`/
`ret` artifacts, and iOS publication/dead-strip/signing before promotion.

Supported Windows/embedded target policy, iOS registration/linking, Android
device/security behavior, and non-host CI runners are Milestone 9 questions. Do
not infer support from a CPU backend or cross-compiled object alone.

## Idempotence and Recovery

Feature flags are default-off; rerun focused commands in an existing build
directory unless stale output is suspected. Do not delete generated dependency
checkouts, build directories, logs, or user-local artifacts merely to clean
status. Preserve unrelated work, especially
`.agent/sljit-depot-tools-execplan.md`.

Before a commit, inspect scoped status/diff, run `git diff --check`, stage only
the intended files, and run `python3 scripts/staged-copyright-headers.py`. Keep
functional changes, checkpoint documentation, and process-policy changes in
logical commits with descriptive English titles and bodies.

If context compacts or work stops, rewrite the state file with the active slice,
last commit, exact next action, validations completed, deferrals, and local
exclusions. Do not append a chat transcript or regenerate historical evidence.

## Outcomes & Retrospective

The project has a bounded, verifiable TCIR boundary with reference, SLJIT, and
portable-C execution plus optional mixed-mode dispatch. Milestone 8 has advanced
through allocation but remains incomplete. The allocation checkpoint establishes
ABI-v5 publication/status/root transport, not full object-memory-manager or
class-loader behavior. The evidence index and archive preserve the detailed
commands, counts, benchmark artifacts, hashes, and limitations.

The execution process now uses state-first resumption and proportional
validation. This removes the prior requirement to reread a 900-plus-line plan,
duplicate every result across living sections, rerun 1,000-sample arithmetic
benchmarks for unrelated slices, or poll silent long-running commands for
progress.

## Revision Note

2026-07-18: consolidated the active ExecPlan around remaining Milestone 8/9
work. Completed milestone detail, repeated revision notes, benchmark tables, and
editorial material were moved to stable referenced records so continuation stays
safe, traceable, and token-efficient.
