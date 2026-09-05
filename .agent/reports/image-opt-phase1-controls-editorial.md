<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization phase 1 editorial handoff

Status: complete after the accounting-clear corrective follow-up.

Phase 1 delivered internal package-private controls for 13 feature IDs without
adding public SDK API. All future optimization defaults remain disabled and
the pressure hook remains a no-op. The original control-plumbing report is
preserved at
`.agent/benchmarks/image-opt-phase1-controls/control-plumbing/report.md`.

The corrective report is
`.agent/benchmarks/image-opt-phase1-controls/complete-diagnostic-gating/report.md`.
Its S1/S2/S3 runs used 60 samples after three warmups on macOS 26.5.2 arm64
software Skia: medians 676, 677, and 671 ms; peak RSS 115712, 116128, and
116112 KB. S2 was +0.148% in median and +0.360% in RSS versus S1, with every
diagnostic field zero. S3 proved the enabled path with 5752 Image creations,
3832 pipelines, 1920 draw plans, 60 readbacks, and 60 native backing creates.
All CVs were below 0.25%; no 200-sample rerun was required.

The follow-up test and deployed smoke landed in `89458ecc7`; the native fix is
`62a4c9278`. `Image.clearImageOperationAccountingCountersForTest()` now uses
a native clear-only path that preserves both configured gate states, while
the legacy reset helpers retain reset-and-enable behavior. The macOS smoke
passed real native create/readback/release assertions with zero counters while
disabled and incrementing counters while enabled. No benchmark rerun was
needed because the timed workload and counted hot paths were unchanged.

The corrected implementation/evidence handoff for phase 2 is `62a4c9278`,
with final branch documentation following it. Focused Image tests, SDK
distribution, macOS Release CMake/Ninja, exact-dylib deployment, and relevant
Image smokes passed. Native diagnostic pointers are cached at the boundary;
counted native operations perform no Java class or field lookup.

Known limits: one local machine, one software-Skia workload, external 50 ms
RSS sampling, and millisecond VM timing around large batches. Android, iOS,
GPU, Windows, Linux, and later optimization phases remain out of scope.

The authoritative evidence index is
`.agent/evidence/image-opt-phase1-controls.jsonl`; verbose logs and generated
binaries remain outside the committed artifact set.
