<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Pre-image optimization master stabilization

This ExecPlan follows `AGENTS.md` and `.agent/PLANS.md`. It defines the
technical investigation and stabilization work for
`fix/pre-image-optimization-master`.

## Purpose / Big Picture

Stabilize the image path before image-optimization branches build on it. The
work has two related but independently reviewable parts:

* investigate correctness risks in adaptive JPEG scaling and establish a
  precise contract between decoder physical dimensions, encoded logical
  dimensions, and presentation scale;
* investigate the legacy Skia byte-swap policy and macros, measure the portable
  and native implementations on supported platforms, inspect generated code,
  and decide from evidence whether endian handling should be centralized.

The expected result is a stabilized baseline with correct adaptive JPEG
behavior, permanent regressions for the discovered boundary cases, and an
evidence-based byte-swap design that does not preserve redundant or misleading
platform policy. The investigation must remain separate from later
image-optimization features.

## Working Set and Resume Protocol

Primary implementation and validation paths are:

* `TotalCrossSDK/src/main/java/totalcross/ui/image/Image.java`
* `TotalCrossSDK/src/test/java/totalcross/ui/image/`
* `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/`
* `TotalCrossSDK/build.gradle`
* `TotalCrossVM/src/util/xtypes.h`
* `TotalCrossVM/src/nm/ui/skia/skia.cpp`
* `TotalCrossVM/src/nm/ui/skia/skia_internal.h`
* `TotalCrossVM/src/nm/ui/skia/skia_surface.cpp`
* `TotalCrossVM/src/nm/ui/skia/`

The final factual handoff will be
`.agent/reports/pre-image-optimization-master.md`. Do not add state/evidence
directories, raw benchmark records, temporary workflows, or execution-only
artifacts to this branch. If work is interrupted, resume from the current
logical commit and the scoped status/diff for these paths while preserving
unrelated local files.

## Current Architecture and Scope

Adaptive JPEG decode can choose a reduced-resolution decode for eligible smooth
scaling pipelines. The investigation must determine and preserve the intended
relationship among:

* the decoder's physical raster width and height, including odd-dimension
  rounding;
* the encoded source's logical width and height;
* the scale metadata consumed by drawing, caching, and materialization.

The contract must hold when a result is freshly materialized and when a cached
decoded backing is reconstructed. It must also distinguish operations that are
safe for reduced decode from operations that require the complete raster.
Explicit JPEG factory behavior and compatibility aliases must be inspected for
regressions rather than assumed from their names.

The native investigation covers the existing Skia byte-swap expressions,
legacy native-swap policy macros, duplicate default definitions, compiler
capability detection, and the interaction between shared type utilities and
Skia call sites. It must account for C and C++ consumers and for the supported
native compiler families without assuming that a source-level builtin or a
portable expression wins everywhere.

The standalone measurement harness, if needed, must not link the TotalCross
runtime or Skia. Production source, permanent tests, benchmark tooling, and
benchmark evidence must remain distinguishable throughout the work.

## Plan of Work

### Milestone 1 — JPEG contract and correctness

Inspect the adaptive decode requirement selection, encoded-source bookkeeping,
fresh materialization, cached backing reuse, draw-plan metadata, and the native
JPEG decoder boundary. Define the physical/logical/content-scale invariant from
those contracts before editing code.

Add focused permanent tests for the selected decode tiers, fresh and cached
materialization, odd dimensions, destination-scale boundaries, direct drawing,
readback, and representative smooth-scaling operation families. Use an
independent complete-decode reference for visual comparisons. Prove that
nearest scaling, rotation-related scaling, and explicit eager JPEG factories
retain their intended decode behavior.

Extend the macOS JPEG smoke workload only with durable regression assertions.
Keep compatibility comments aligned with the actual alias behavior without
changing unrelated public API semantics.

### Milestone 2 — Skia byte-swap investigation

Inventory the legacy Skia policy and every byte-swap implementation used by
the surface conversion path. Determine which defaults are authoritative and
which definitions are duplicated. Design a standalone benchmark that compares
portable and native 16-bit/32-bit buffer loops with identical work, warmups,
sample counts, checksums, and deterministic measurement order.

Collect measurements on supported native macOS, Linux, Windows, and Android
compiler/architecture combinations when available. Do not use emulation as
performance evidence. Record compiler, operating system, architecture, sample
count, workload dimensions, and checksum agreement. Keep raw samples and
temporary runners local to the investigation unless a durable artifact is
required by the final decision.

Inspect optimized code generation for the same variants on the relevant
compiler families. Separate loop-body code generation from timing noise,
dispatch overhead, startup, allocation, and reporting effects. Treat measured
results as workload-specific evidence rather than a universal performance
claim.

### Milestone 3 — Endian design decision and integration

Use the benchmark and code-generation evidence to decide whether the endian
contract belongs in a shared type utility or should remain local to Skia. If
centralization is justified, specify fixed-width 16-bit and 32-bit behavior,
single evaluation of macro arguments, compiler capability checks, supported
MSVC handling, and a portable fallback before implementation.

Keep duplicate Skia default cleanup independently reviewable when it has
standalone value. Migrate Skia only after the shared contract is validated,
preserving unrelated rendering and windowing policy. If the evidence does not
justify centralization, retain the smallest correct local design and document
why.

### Milestone 4 — Handoff

Run focused image tests, the relevant macOS SDL/Skia surface validation, and
representative C99/C++17 consumers for any shared endian helper. Validate
headers, staged whitespace, tracked-path scope, and the absence of temporary
benchmark machinery from the production history. Write one final report that
distinguishes measured observations from estimates, states limitations, and
records which platform checks were actually completed.

## Decision Log

* Treat JPEG physical dimensions, source logical dimensions, and presentation
  scale as separate pieces of state until the existing consumers and cache
  boundaries have been inspected.
* Use independent complete-decode references for image correctness so the
  regression does not merely reproduce the adaptive path's assumptions.
* Benchmark native and portable byte-swap forms on native hosts with direct
  buffer loops; do not generalize across platforms from one compiler or
  from an indirect-dispatch harness.
* Keep measurement infrastructure out of production unless a durable test
  contract is demonstrated. Preserve only aggregated conclusions in the final
  handoff.
* Let compiler capability, compatibility, and code-generation evidence—not a
  preferred implementation style—determine whether endian handling is
  centralized.

## Validation and Acceptance

The stabilization is accepted when:

* focused SDK image tests and the durable macOS JPEG smoke cover the corrected
  contract and pass;
* the relevant macOS SDL/Skia surface path builds and runs;
* representative C99 and C++17 endian consumers pass, including a
  side-effect test if macros remain part of the interface;
* native benchmark results, when collected, identify compiler, platform,
  architecture, workload, and checksum status, and code-generation inspection
  is recorded without committing raw dumps;
* focused copyright validation and `git diff --check` pass;
* no temporary benchmark workflow, raw sample collection, execution log,
  state/evidence file, or superseded implementation is treated as production
  source; and
* the final report clearly separates delivered behavior, measured evidence,
  platform support, and deferred validation.

Use the smallest validation level that proves each milestone. Save verbose
build and benchmark output outside the tracked source tree. Defer full release
and cross-platform matrices when the focused gates establish the contract, and
name those deferrals explicitly in the final handoff.

## Risks and Open Questions

The JPEG bug may occur at a cache/materialization boundary rather than in
decode selection itself. Rounded physical dimensions may not provide an
independent presentation scale, so the source and decoder metadata must be
traced separately. A visual regression can be hidden by a correctly sized
output buffer unless pixel content is compared.

Compiler-specific byte-swap forms, portable expressions, and legacy macros may
compile to different code on different host families. Benchmark results may
be dominated by measurement design or loop shape, and code generation may
differ between the loop body and its tail. Native runner availability and
SDL/Skia dependency availability may limit the platform matrix.

## Idempotence and Recovery

Stage only the active plan, production, permanent-test, and final-report paths.
Never clean unrelated untracked files, generated dependencies, build
directories, or local logs.

If a milestone must be retried, restore only its scoped paths from the saved
revision and rerun its focused validation. Do not overwrite user files that
share nearby directories. Temporary benchmark executables and raw samples
belong outside the repository and may be discarded without changing the
production contract.

## Expected Outcome

The branch should provide a precise and tested adaptive-JPEG contract, a
durable regression suite, and a documented byte-swap decision grounded in
native measurements and compiler output. The resulting production code should
have one understandable policy boundary per concern, with unrelated image
optimization work left for later branches.
