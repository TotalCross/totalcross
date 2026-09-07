<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization phase 1 editorial handoff

Status: complete after the accounting-clear corrective follow-up and addendum
rebaseline.

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

## Phase 1 addendum — later raster reservations

The addendum appends `RASTER_TARGET_COLORTYPE_CONVERSION` (13),
`RASTER_PHYSICAL_VARIANT_CACHE` (14), and `RASTER_PHYSICAL_IDENTITY_FOLDING`
(15), and sets `FEATURE_COUNT` to 16. Existing IDs 0-12 and the package-private
process-global tri-state, opt-in, long-mask contracts remain unchanged. The
three reservations are default-disabled and runtime-inert; color-type
conversion, physical variant caching, and physical identity folding were not
implemented. No controls were added for the explicitly excluded swap, JPEG,
hardware-scaling, or GPU `writePixels` cases.

The benchmark protocol now requires equivalent memory/residency diagnostics at
matched execution points when a peak-RSS difference above 5% persists after the
required 200-sample rerun. On macOS this means `vmmap -summary` plus RSS and
available physical-footprint measurements. The local macOS requirement and
historical benchmark artifacts remain unchanged.

Focused `totalcross.ui.image.*` tests passed all 137 tests after a shared-JVM
test precondition was made explicit in `edcefbe06`; SDK `dist -x test` also
passed. The original addendum checkpoint did not rerun the macOS software-Skia
native build because the addendum changed no native code, build configuration,
or counted hot path; the subsequent rebaseline below did run that required
build and smoke path.

## Phase 1 addendum rebaseline handoff

The post-stabilization report is
`.agent/benchmarks/image-opt-phase1-controls/post-stabilization-rebaseline/report.md`.
It records the authored Phase 1 SHA `1898014784b2fba5716cc033e49520740b05f0dd`
as historical metadata and current master `7add0f29e9366a19d894237119a415416e6bb557`
as S1. S2 and S3 use final Phase 1 code at `1d8deacb14cd3847cffb4d238c3f0fa97830951d`.

The initial 60-sample run required the protocol’s 200-sample rerun because of
the peak-RSS review threshold. Final medians were 697 ms (S1), 695 ms (S2),
and 698 ms (S3), with peak RSS of 113680, 115888, and 113920 KB. S2 versus S1
was -0.287% in median and +1.942% in RSS; S3 versus S1 was +0.143% in median
and +0.211% in RSS. The persistent-RSS condition was not met, so matched
`vmmap -summary`/RSS/physical-footprint diagnostics were not required.

All three scenarios passed 200/200 sample and process-exit checks. The final
focused Image suite passed all 137 tests, SDK `dist -x test` passed, and the
Release software-Skia macOS CMake/Ninja build plus exact-dylib smoke passed.
The active plan is 8,507 bytes and 195 lines. Phase 2 should rebase from
current master before implementing any later raster optimization; historical
benchmark artifacts remain immutable.
