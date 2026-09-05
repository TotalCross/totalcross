<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization phase 1 history

This append-only file records completed milestone summaries and stable evidence
references for `.agent/plans/exec-plan-image-opt-phase1-controls.md`.

## Bootstrap

The phase branch was created from the authored master SHA. The detailed state
and initial evidence record are in:

- `.agent/state/image-opt-phase1-controls.md`
- `.agent/evidence/image-opt-phase1-controls.jsonl`

## Milestone 1 — benchmark harness and S1

The deterministic control workload and reusable RSS runner were added and
deployed through the existing macOS smoke registration. The exact pre-settings
baseline is commit `d00d7c1dfec4bddc14bdfbb8293b30dfe8b3a3c6`; compact samples and
machine metadata are in
`.agent/benchmarks/image-opt-phase1-controls/control-plumbing/`.

## Milestone 2 — internal controls and diagnostics

`ImageOptimizationSettings` reserves all phase 2–4 feature IDs, validates
tri-state/numeric controls, exposes an allocation-free effective mask, and
keeps the memory-pressure hook as a no-op. `DIAGNOSTIC_ACCOUNTING` synchronizes
the existing Image accounting gate used by Java and native-backing diagnostics;
all other features remain inert. Focused tests and the corrected S2/S3 report
are in the control-plumbing benchmark directory.

## Milestone 3 — final validation and handoff

Final focused Image tests, SDK distribution, macOS software-Skia Release build,
and the native geometry and draw-presentation Image smokes passed. The source
handoff SHA for phase 2 is `613762c45ed887733b1dab9a12b20b32280e0fca`.
Implementation changes stop at the internal settings and diagnostic gate;
later raster, format, cache, lifecycle, GPU, and allocation work remains for
the next plans.

## Corrective milestone — complete diagnostic accounting gate

The original control-plumbing report remains unchanged. Focused tests landed in
`42a183473`; the separate workload, explicit native backing
create/readback/release probe, generic artifact path, and revision-aware runner
landed in `f33760435`. The gate fix and cached native counter boundary landed
in `4721397d6`, with native symbol registration corrected in `8399b8b0a`.

The corrective S1/S2/S3 item is
`.agent/benchmarks/image-opt-phase1-controls/complete-diagnostic-gating/`.
S1 at `f33760435` recorded 60 backing readbacks and 60 native creates/releases
despite Java diagnostics being disabled. Post-fix S2 at `8399b8b0a` recorded
zero in every emitted diagnostic field; S3 recorded nonzero Java pipeline and
draw-plan counters, 60 readbacks, and native backing create/release counts.
All 60-sample CVs were below 0.25%, so no 200-sample rerun was needed.

Focused Image tests, SDK distribution, macOS software-Skia Release CMake/Ninja,
exact-dylib deployment, and relevant Image smokes passed. The corrective
evidence commit is `884ffcb61`; verbose logs and generated binaries remain
outside the committed artifact set.
