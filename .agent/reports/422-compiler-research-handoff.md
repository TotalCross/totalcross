<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Issue #422 research handoff

## Purpose

Work under Issue #422 evolved into two distinct but related research questions:

- **Runtime JIT/AOT:** can TC bytecode become a verified IR for interpreter,
  JIT, or AOT paths while TCZ and legacy fallback remain compatible?
- **Java/converter optimization:** where should R8-like work live, which
  classfile facts survive J2TC, and does evidence justify ProGuard, a broad
  Java-aware HIR, or a narrower converter optimization?

## Delivered work and current status

| Component | Delivery and status |
|---|---|
| TCIR | Typed, verified backend/runtime IR and whole-method fallback; architectural research, incomplete and default-off. |
| Reference boundary | Verifier and independent TCIR interpreter; research oracle/backend boundary, default-off. |
| SLJIT JIT | Baseline native generation, W^X lifecycle, cache, ABI, and mixed dispatch; research/default-off, not production-enabled. |
| Portable-C AOT | Deterministic C, registry, and execution comparison; research/default-off, not productionized. |
| ProGuard-before-J2TC | Isolated structural experiment and retained variants; experiment/oracle only, not a production pre-pass. |
| J2TC compatibility | Sparse line tables, inherited invocation owners, and valid handler-entry stacks; production converter path. |
| Float slot fix | JVM `float` local/parameter width separated from TC floating-bank width; production converter path. |
| TCM | Deterministic v1 semantic sidecar bound to TCZ artifacts; opt-in `/tcm aot`, default-off, with no required runtime consumer. |
| TCM/J2TC hardening | Disabled-path, format, publication, and semantic hardening; production boundary, emission opt-in. |
| Canonical semantics | Shared Java-to-TC type lowering and repository-owned declaration resolution; production converter path. |
| Source-size ratchet | Unified validation, 20 KiB ratchet, CI, and agent guidance on companion `task/source-size-validation` (`695b3c587`–`ea00dd178`); repository tooling, not compiler behavior. |

The future optimizer was not implemented.

## Established conclusions

- TCIR is viable after TC-bytecode lowering; its verifier, reference interpreter,
  and method-atomic fallback support backend/runtime experiments.
- TCIR is too late to recover every Java-level fact required for R8-like work;
  for example, TC lowering loses exact source `float` identity.
- TCZ remains executable/runtime truth. TCIR and TCM do not replace or change
  its contract.
- TCM persists facts without changing TCZ; it is not an optimizer decision format.
- A future optimizer should consume canonical in-memory semantic facts and may
  serialize the same facts through TCM; it should not write and reread `.tcm`
  within one deploy.
- Evidence does not justify a broad Java-aware HIR. Targeted field propagation,
  marking, and removal are the strongest measured next compiler investment.

## ProGuard result

The compatible field family reduced TC code slots by about **3.5%** and field
accesses by about **4.8%** against the normalized structural baseline.
Module-local optimization retained about **3.4%**, so this effect does not need
a whole-program framework on this corpus. Enum unboxing was negligible. Broad
shrinking/interprocedural results were weak, incompatible, or unsupported.

These are **structural TCZ results**, not runtime performance claims; the SDK
runtime corpus is not representative applications.

## J2TC/TCM hardening facts

- Sparse line tables, inherited symbolic invocation owners, and valid
  exception-handler entry stacks are handled.
- JVM `float` consumes one local slot while using the TC floating bank;
  `long`/`double` still consume two JVM slots.
- TCM `NONE` collects zero metadata, and unrequested StackMap metadata is
  skipped without materialization.
- Origin finalization is linear rather than nested-rescan based.
- TCM v1 has stable explicit wire codes; TCZ hashing is streamed.
- Pre-replacement publication failures preserve the previous valid sidecar.
- Host `java.*` reflection is not semantic authority. Shared type lowering and
  declaration resolution use parsed and repository-owned mapped classes.
- Metadata-off and metadata-on modes produced byte-identical TCZ output in the
  validated aggregate workload.

## Deliberately deferred

- field propagation/marking/removal implementation;
- representative application evidence before broad optimizer architecture;
- broad Java HIR and reachability/tree-shaking framework;
- production ProGuard integration;
- full TCIR opcode/semantic coverage, including remaining handler, field,
  class-initialization, array, monitor, virtual/interface-call, and other
  semantics;
- TCIR rollout and production JIT/AOT selection policy;
- AOT publication, packaging, invalidation, and class-loader lifecycle;
- broad Linux/Windows/Android/iOS production qualification;
- performance claims beyond measured workloads; and
- optional TCM precision work such as unresolved-call analysis or further
  declaration-resolution refinement.

## Known residuals before stronger optimizer/AOT use

These are known future-use constraints, not unfinished acceptance criteria for
the completed Issue #422 research. None blocks reintegration of this branch.

- **Permanent TCM v1 compatibility fixture:** historical compatibility passed
  against a frozen fixture, but it remains external/build-local and its test is
  skipped unless `TCM_V1_FIXTURE_DIR` is set. A small immutable committed
  fixture in normal CI remains desirable before long-term format evolution.
- **Program-to-device hierarchy transition:** program-class traversal does not
  explicitly bridge an application superclass edge into the mapped hierarchy,
  as in `MyProperties -> java.util.Properties -> java.util.Hashtable.put`.
  This does not block the field optimizer, but must be resolved or treated
  conservatively before CHA, devirtualization, or aggressive AOT relies on
  declaration ownership.
- **Full JVM descriptor precision:** device declaration matching currently uses
  parameter compatibility rather than the full descriptor including return
  type. Harden this before bridge, covariant, or synthetic method cases become
  optimizer authority.
- **Remaining unresolved calls:** classify intentional native, replacement,
  dynamic, external, and generated cases separately from resolver gaps before
  using call/declaration metadata for devirtualization, inlining, or aggressive
  AOT. Until then, unresolved cases must remain conservative.

For exact validation and measurements, see the [hardening editorial](harden-tcm-j2tc-boundary-editorial.md)
and [hardening evidence](../evidence/harden-tcm-j2tc-boundary-01.jsonl).

## Recommended order

1. Gather representative application evidence when optimization resumes.
2. Implement a bounded field propagation, marking, and removal optimizer, using
   retained ProGuard variants as a differential oracle.
3. Put it before or inside J2TC and consume canonical in-memory converter
   semantics, not a `.tcm` round-trip.
4. Add a broader Java IR/HIR only if later valuable transformations cannot be
   expressed cleanly with the current model.
5. Keep TCIR expansion and JIT/AOT productionization independent and
   runtime-workload driven.
6. Revisit devirtualization/inlining only after hierarchy/declaration facts and
   representative application evidence justify them.

## Suggested architecture

```text
Java/classfile semantics
  -> optional Java-aware analysis/optimization
  -> J2TC -> TC bytecode / TCZ -> TCIR -> interpreter / JIT / AOT
```

Semantic facts also flow independently to TCM. Within one deploy, use:

```text
semantic facts -> FieldAnalysis/Optimizer -> J2TC
```

TCM is persistence/diagnostics/future-consumer metadata, not an unnecessary
serialization round-trip.

## Evidence index

Authoritative summaries:

- [TotalCross IR, JIT, and AOT](totalcross-ir-jit-aot-editorial.md)
- [ProGuard before J2TC](evaluate-proguard-before-j2tc-editorial.md)
- [J2TC semantics and TCM](j2tc-semantics-and-tcm-editorial.md)
- [Harden J2TC/TCM](harden-tcm-j2tc-boundary-editorial.md)

Supporting indexes: [TCIR state](../state/totalcross-ir-jit-aot.md) and
[evidence](../evidence/totalcross-ir-jit-aot.md); [ProGuard state](../state/evaluate-proguard-before-j2tc.md)
and [evidence](../evidence/evaluate-proguard-before-j2tc.jsonl); [J2TC/TCM state](../state/j2tc-semantics-and-tcm.md)
and [evidence](../evidence/j2tc-semantics-and-tcm-01.jsonl); [hardening state](../state/harden-tcm-j2tc-boundary.md)
and [evidence](../evidence/harden-tcm-j2tc-boundary-01.jsonl). Their corresponding
completed ExecPlans use the same workstream names under `.agent/`.
Deferred runtime work is scoped by the [TCIR continuation plan](../exec-plan-expand-tcir-semantic-coverage-and-production-readiness.md).

## Integration context

- Issue: **#422**.
- Source branch: `feature/422-create-ir-for-jniaot`.
- Final research/provenance revision:
  `de87b35d35dd67a2119a51760ca32c7d85896cf4`.
- Required copyright-provenance work before reintegration is complete there.
- The branch is closing intentionally so unrelated priorities can proceed;
  deliberate deferrals are not reasons to keep it open.
- Master integration: **to be recorded after landing.**

The source-size series branched from `797c0913f` and is not an ancestor of the
feature revision above. This handoff neither merges nor reproduces that policy.

## Resume protocol

Read this handoff first, then only the editorial report for the workstream being
resumed. Consult raw evidence/history only to reconstruct a precise measurement
or decision. Do not rerun the original experiments by default.
