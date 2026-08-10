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
