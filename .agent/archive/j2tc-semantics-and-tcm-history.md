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

## Milestone 2 — compatibility hardening

Three JVM-valid cases became focused converter fixes: sparse line tables retain
unknown line zero before their first entry; inherited calls validate against the
resolved declaration hierarchy while preserving the symbolic owner; and catch
handlers receive their implicit throwable even when they begin with `dup` rather
than `astore`. Commits `8542a4948`, `4a673de38`, and `398d7095f` contain the
independent fixes and fixtures.

ASM verification established that representative optimized classes were valid.
The remaining replacement constructors and generated member names nevertheless
change canonical TotalCross 4D contracts, so they remain deterministic
unsupported-input diagnostics. Current-converter corpus rechecks, the focused
test sweep, distribution, deploy, and native macOS smoke all passed as applicable.

## Milestone 3 — semantic preservation in memory

Commit `b9e6abf65` retains exact class/member access flags, original hierarchy
names, source/signature attributes, and bounded symbolic StackMap frames. Its
JVM-verified fixtures cover every required frame encoding and precise malformed
input diagnostics. Commit `0507ad84d` adds an effectively immutable deploy-scoped
model and captures source/effective identities, source/lowered types, call kinds
and owners, Java-PC origins, allocations, reflection roots, native/replacement
kinds, and lambda/string-concat/record origins. Converter-only origin tags do not
enter TCCode serialization. The full focused modern-Java sweep passed.
