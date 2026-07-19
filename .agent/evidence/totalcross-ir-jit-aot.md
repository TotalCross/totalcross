<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Evidence Index: TotalCross IR, JIT, and AOT

This append-only index is not a normal continuation read. Search it by revision,
milestone, command, or artifact when a claim needs verification. The unabridged
pre-consolidation checkpoint, including benchmark tables and artifact hashes, is
preserved at `ba6d2f0c3:.agent/exec-plan-totalcross-ir-jit-aot.md`.

## Record format

Append each future record as a new row at the end of the table, which is kept as
the final section of this file. Include date/revision, milestone/slice, command
or wrapper identifier, material result, evidence path when needed, and one short
scope or limitation statement. Add hashes only when integrity requires them.

## Evidence records

| Date | Milestone / slice | Revision(s) | Compact result | Evidence / limitation |
| --- | --- | --- | --- | --- |
| 2026-07-17 | M2 TCIR contract | `96c17be4b`, `a3a5e33fa` | Core/verifier/registry tests and sanitizer evidence passed. | Detailed commands and findings: archive baseline. |
| 2026-07-17 | M3 frontend | `0fa51be08`, `f0e241b11` | Converter fixtures and bounded frontend passed focused host checks. | Converter fixture records in native/SDK tests. |
| 2026-07-17 | M4 interpreter | `d5ebceb43`, `801ae507b` | 1,179 real-`executeMethod` differential inputs matched. | Legacy interpreter remains oracle/fallback. |
| 2026-07-17 | M5 SLJIT | `d7d4ad64a`, `8a5ae42d6` | Optional W^X backend and Android arm64 API 23 cross-build passed. | No Android device execution. |
| 2026-07-17 | M5 benchmark protocol | `e49acf6a5`, `77d179edc` | Revision-keyed JSON/CSV benchmark and validator created. | Historical 1,000-sample requirement retired by process policy. |
| 2026-07-17 | M6 AOT | `8d738ff66`, `1aa428b74` | Deterministic C generator, manifest, and four-way execution passed. | Production publication remains later work. |
| 2026-07-18 | M7 dispatch | `35b14388b`, `3cdfd6974` | Default-off mixed-mode dispatch and mutex fast path passed. | Static-i32 integration scope only. |
| 2026-07-18 | M8 numeric/reference/switch/call | `da38b7278` through `155c74ebd` | Fourteen-fixture progression reached 6,398 retained legacy four-way comparisons. | Effectful division/null checks remain TCIR-only. |
| 2026-07-18 | M8 allocation | `e7ea5cb14`, `051800dcd` | Runtime ABI v5; 16 allocation-contract comparisons; Release/ASan/UBSan/default-off and Android compilation passed. | No real TCZ/class-loader/OMM forced-GC proof. |
| 2026-07-18 | M8 allocation benchmark | `051800dcd6e6` | 60/200/1,000 arithmetic profiles validated. | Workloads do not measure allocation; raw artifact hashes remain in baseline snapshot. |
