<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Editorial handoff — raster policy audit and reuse

## Editorial Summary

Milestone 1 is complete and passes its correctness, packaging, smoke, and
20-process accounting A/B gates. The writePixels bit-2 comparison improves
median active work by about 19% in both accounting modes. Tail behavior is
mixed, so the result is evidence for review rather than a final performance
claim. Execution is stopped at the required review boundary.

## Original Plan versus Actual Outcome

The four-milestone plan is preserved. Milestone 1 completed without changing
default masks, delayed target-color materialization, or the single shared
raster-variant slot. Milestones 2–4 remain gated.

## What Changed

Implemented and validated the strengthened clip proof, intrinsic JPEG opacity,
accounting on/off isolation, and native target-format reporting. Added a safe
packed target-metric bridge through `NativeImageBacking` after the original
benchmark `Image` bridge proved unsafe in the deployed VM. Added the benchmark
summary at `.agent/benchmarks/image-raster-policy-03/m1-write-pixels-accounting.md`.

## Decisions and Trade-offs

The plan’s fixed decisions remain active: no default-mask changes, delayed
target-color materialization, and one shared raster-variant slot. Accounting
off disables diagnostic output while retaining external timing.

## Unexpected Problems and Discoveries

The native metric method name must follow the VM’s 32-character truncation
rule. The original `Image` native probe also corrupted the deployed VM after
rendering; capturing a scalar snapshot during Skia initialization and reading
it once through the established backing bridge resolved the issue.

## Validation and Measurable Results

Passed: SDK distribution; macOS ARM64 `tcvm`, `Launcher`, and
`skia_surface_test`; native surface test; package self-test; six accounting-on
smokes; and the 20-process `write-pixels-accounting-ab` matrix. The required
corpus hash was `588a7e0f4019424a`. Environment reporting emitted software
target `56x896`, row bytes `4320`, BGRA8888 color classification, and alpha
type `2`.

## Useful Evidence and Examples

See `.agent/evidence/image-raster-policy-03-audit-and-reuse.md` and
`.agent/benchmarks/image-raster-policy-03/m1-write-pixels-accounting.md`.

## Limitations, Remaining Work, and Open Questions

Milestone 2 and later work remain. No structural raster-policy change has been
made or authorized. The five-round tail distributions need reviewer guidance
before any interpretation or structural audit proceeds.

## Possible Article Angles

The benchmark is a useful narrative about separating diagnostic accounting
from active-work timing: median writePixels benefit is stable, while P95 and
tail behavior changes with accounting mode.

## Suggested Narrative

Lead with the 20-process interleaved design, then distinguish median benefit
from unresolved tail behavior and explain why accounting-off artifacts are
explicitly unavailable.

## Claims Requiring Human Review

All structural corrections, reuse conclusions, and performance interpretation
require the mandatory review boundaries in the active plan. Review approval is
now required before Milestone 2.
