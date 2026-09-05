<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization phase 1 editorial handoff

Status: complete.

Phase 1 delivered internal, package-private controls for the image optimization
series without adding public SDK API. The 13 feature states are independent
tri-state switches; `DEFAULT` remains disabled for all future optimizations.
Only `DIAGNOSTIC_ACCOUNTING` is wired to the existing Image and native-backing
accounting gate. The memory-pressure hook is intentionally a no-op for phase 4.

The control benchmark used 60 samples after three warmups on macOS 26.5.2
arm64 with software Skia. S1 median/p95 was 668/671 ms with peak RSS 107456 KB.
S2, all features disabled, was 669/671 ms and 107280 KB: +0.15% elapsed and
-0.16% RSS versus S1. S3, diagnostic accounting enabled only, was 669/671 ms
and 109008 KB: +0.15% elapsed and +1.44% RSS versus S1. S3 ended with 5752
Image creations, 3832 pipeline creations, 1920 draw-plan creations, and 15360
cache hits, proving the diagnostic switch was active.

The exact source handoff for phase 2 is branch commit
`613762c45ed887733b1dab9a12b20b32280e0fca`; the final documentation commit
`47b81182dd509a8cafd11250510939fd686586d0` follows it without changing
implementation behavior. Phase 2 should branch from the final phase-1 branch
HEAD after the state/evidence bookkeeping commit.

Known limits: one local machine, one software-Skia workload, external 50 ms
RSS sampling, and millisecond VM timing around large batches. These results do
not claim behavior or performance on Android, iOS, GPU paths, or later format,
cache, lifecycle, or allocation optimizations.

The authoritative evidence index is
`.agent/evidence/image-opt-phase1-controls.jsonl`; verbose logs and generated
binaries remain outside the committed artifact set.
