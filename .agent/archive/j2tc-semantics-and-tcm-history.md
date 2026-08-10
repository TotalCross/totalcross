<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# J2TC semantics and TCM milestone history

## Milestone 0 — evidence boundary

The plan began at `42dec24e3353a1e3e97de5ea0288956318a55142`. The retained
ProGuard experiment corpus was available, so no expensive regeneration was
needed. Its exact converter rejection families and the four ordinary baseline
TCZ hashes were indexed in
`.agent/evidence/j2tc-semantics-and-tcm-01.jsonl`. Unrelated local SLJIT and TCIR
plist files were explicitly excluded from this plan.

## Milestone 1 — float parameter correctness

`OperandReg.init` now selects the TC register bank independently from JVM local
width: `F`, `J`, and `D` remain in `reg64`, while only `J` and `D` consume two
locals. The warning-only parser scans and J2TC flushes were deleted. A ten-case
descriptor matrix runs for both static and instance dispatch, javac conversion
fixtures assert post-float return registers, and the aggregate native smoke runs
nine value cases across int, reference, long, and double. Commit `7c960237f`;
full validation is indexed in the evidence file.
