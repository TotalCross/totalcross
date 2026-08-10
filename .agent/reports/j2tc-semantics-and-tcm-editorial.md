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
