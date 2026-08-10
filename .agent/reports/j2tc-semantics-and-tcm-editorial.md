<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# J2TC semantics and TCM editorial handoff

This report will be synthesized at milestone checkpoints and completed when the
converter-boundary plan closes. Milestone 0 retained the previous ProGuard
corpus, fixed the plan-start revision, and recorded ordinary TCZ hashes without
rerunning the full experiment.

Delivered behavior, validation results, compatibility classifications, and the
future optimizer/AOT handoff will be added only after they are established.

## Float parameter boundary

The corruption came from treating TC register-bank width as JVM local-slot width.
Java `float` still lowers to the TC 64-bit bank, but now advances one JVM local;
`long` and `double` advance two. Ten descriptor shapes run under both static and
instance mapping, ordinary javac fixtures verify returned parameters, and native
macOS execution passed nine value cases. The obsolete warning and its parser/J2TC
bookkeeping are gone; distribution and deploy logs contain no warning instances.

## Optimized-class compatibility boundary

Sparse line tables, inherited method owners, and exception handlers beginning
with stack operations were confirmed JVM-valid and fixed in J2TC. The retained
optimized TCUI corpus now converts in both strict and optimized forms. ASM also
verified representative language and utility inputs.

The remaining optimizer-created replacement descriptors and generated member
names are not generalized by J2TC. Although structurally valid JVM bytecode,
they no longer describe supported TotalCross 4D replacement contracts. Keeping
their rejection prevents a ProGuard-specific exception from weakening device ABI
validation. The milestone closed with focused tests, SDK distribution, aggregate
deploy, and a native run containing 97 passes and no failures.

## Preserved compilation facts

The converter now snapshots original class-file identity and raw access flags
before TotalCross normalization. It retains source/signature, nest, record,
permitted-subclass, exact JVM descriptors, explicit native/replacement kind, and
symbolic StackMap types including distinct float and double. During lowering it
records Java PCs, opcodes, allocations, source invoke kind and symbolic/resolved
owners, final TC slot ranges, reflection roots, unresolved lookup state, and
lambda/string-concat/record origins. The resulting object graph exposes immutable
lists and contains source facts rather than optimization conclusions. No metadata
field is serialized into TCCode or TCZ.

## TCM v1 delivery

`/tcm aot` emits one deterministic `TCM1` sidecar; ordinary deploy remains
default-off. Ten length-delimited little-endian sections use a sorted string
table, and the required manifest binds ordered relative TCZ names to SHA-256.
Unknown optional sections are skipped, while unknown required sections, malformed
lengths, incompatible major versions, and artifact mismatches are rejected. The
writer publishes through a sibling temporary file. Platform builds retain their
root TCZ set only when TCM is requested, keeping the sidecar directly verifiable
without changing installed copies.

The closure deploys produced TCZ SHA-256
`21f48888a0817eefe94cbc0e51ec4a775edcf8f6a3e20c6b9aec0b3df2be081c`
with metadata both off and on. `TcmInspector` validated the emitted sidecar, and
the TCM-enabled macOS app completed 97 checks with zero failures. TCM remains a
local build artifact containing application identities; it is not telemetry and
has no required runtime or AOT consumer.

## Final handoff

All requested converter and sidecar boundaries are now explicit and tested.
Future field optimization, HIR, or AOT work should consume TCM facts and rederive
its own analyses; it must not treat source metadata as permission to change TCZ
runtime semantics. Raw class embedding, optimizer decisions, production ProGuard
integration, telemetry, and non-macOS platform qualification remain outside this
delivery.

Final validation covered the focused modern-Java/metadata suite, SDK distribution,
isolated default and TCM-enabled deploys, reader/hash inspection, native macOS
execution, all changed copyright headers, committed and worktree whitespace, and
every new file's size/line limits. No in-scope working-tree changes remain.
