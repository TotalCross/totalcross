<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Subplan: disable unused TCM collection and linearize origin finalization

Parent: `.agent/exec-plan-harden-tcm-j2tc-boundary.md`.

Read this file only while Milestone 1 is active.

## Goal

Make TCM `NONE` structurally avoid TCM-only work and make AOT origin finalization
linear in the lowered instruction stream, without changing TCZ output or AOT
metadata semantics.

## Current problem

`J2TC.process()` creates a real `CompilationMetadataCollector` for every run and
conversion calls capture methods regardless of `TcmMode`.

`JavaCode` parses `StackMapTable` eagerly although current normal TCZ conversion
does not consume the frames.

`CompilationMetadataCollector.finishMethod()` performs a full instruction scan for
every source site. For `B` Java bytecodes and `I` lowered instructions this is
approximately `O(B * I)`.

## Implementation

Introduce one deploy/conversion-scoped capture abstraction. Prefer:

```text
CompilationMetadataCapture
Real collector
NoOp capture
```

or an equivalent design with one decision at conversion-run start.

The disabled implementation should allocate no per-class/per-method/per-site
metadata. Its methods should return null/no-op without requiring repeated
`tcmMode` branches throughout J2TC.

Do not make conversion correctness depend on capture being enabled.

Use a semantic capability such as `needsSemanticMetadata`, not a direct CLI check
inside the low-level class parser, to decide whether `StackMapTable` is parsed and
retained. With capture disabled, skip the attribute by declared length and retain
stream correctness.

Keep direct parser tests able to request StackMap parsing explicitly.

### Preserve non-TCM deploy state

Audit `Class.forName` tracking, replacement processing, generated lambda state,
constant-pool behavior, and any static J2TC state currently touched near metadata
hooks.

Do not route normal semantic deploy state through a no-op metadata implementation
if that state is needed when TCM is off.

### Linear origin finalization

Keep `javaPc/javaOpcode` tagging on lowered `Instruction`.

Replace the site-by-site instruction rescans with a single ordered scan over the
final `target.insts` vector.

For each instruction:

- maintain cumulative TC slot offset;
- identify its source Java PC/opcode;
- create/update that site's first and end TC slot;
- if it is a lowered `Call`, capture call opcode and call slot range;
- preserve allocation type already attached to the source site.

Dynamic lowerings that do not end as a `Call` use the complete source range, as
before.

Preserve explicit empty/no-output source sites if the current TCM contract emits
them.

If branch promotion or instruction replacement loses origin tags, propagate the
tags at the transformation that creates the replacement. Do not reintroduce a
global rescan workaround.

Do not write metadata fields into serialized `TCCode`.

## Tests to write during implementation

Write but do not run:

- TCM `NONE` conversion proves no class/method/site/origin metadata is accumulated;
- TCM `NONE` proves StackMap frames are not materialized;
- TCM `AOT` still captures StackMap and existing semantic facts;
- linear finalizer reproduces representative origins and call ranges;
- branch-promoted/replaced instructions preserve origin tags;
- dynamic lambda/string-concat/record ranges remain valid;
- TCZ bytes from the same input remain identical between `NONE` and `AOT`.

Prefer test-only counters/hooks that are absent or zero-cost in production. Do not
add verbose production logging.

## Final milestone validation

Testing is the final implementation action.

Run focused collector, metadata, StackMap, and modern-Java tests.

Compare AOT semantic counts and a compact origin/call summary to the Milestone 0
fixture.

Repeat the same fixed deploy timing workload captured in Milestone 0 only after
functional checks pass. Use the same warmup/sample policy and preserve raw samples
outside the plan.

Acceptance:

```text
NONE classes/methods/sites/origins captured = 0
NONE StackMap materialized = 0
AOT semantic summary preserved
TCZ NONE == TCZ AOT byte-for-byte
origin finalization uses one instruction scan
```

Treat timing as supporting evidence. If a repeatable median regression above about
5% appears on the fixed workload, investigate before closing. If noise dominates,
record that limitation rather than increasing samples excessively.

After validation, create logical commits. Suggested boundaries:

```text
perf(compiler): disable unused metadata capture
perf(compiler): linearize metadata origin finalization
```

Exact titles may change with the implementation. Do not push.

## Risks

A no-op capture must not suppress normal `Class.forName` semantics.

Tests that instantiate `J2TC` directly may rely on metadata being implicitly
available today. Update them to explicitly enable metadata rather than keeping
production collection always on.

Gating StackMap parsing must not alter class-file stream position or malformed
attribute diagnostics when metadata is requested.

Some instructions may be created after the original tagging point. Audit only the
known replacement/promotion paths that affect origin evidence.

## Recovery

If final tests fail, record the first stable diagnostic in state, fix the active
milestone, and rerun only its final validation.

Generated timing/output artifacts are replaceable and stay ignored.

Do not commit or start Milestone 2 until this milestone passes.
