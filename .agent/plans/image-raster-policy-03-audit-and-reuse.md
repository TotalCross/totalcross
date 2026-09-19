<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Plan 3 — audit raster identity, eligibility, and reuse

This ExecPlan follows `AGENTS.md`, `.agent/PLANS.md`, and the
logical-commits skill. Execute only on
`perf/image-decode-distributed-benchmark`. Plans 1 and 2 remain ancestors of
this work.

## Purpose and non-negotiable scope

Make the device-space `RASTER_OPAQUE_WRITE_PIXELS` benchmark trustworthy, then
measure whether three existing optimizations are blocked by cache identity or
canvas eligibility:

- bit 13, `RASTER_TARGET_COLORTYPE_CONVERSION`;
- bit 14, `RASTER_PHYSICAL_VARIANT_CACHE`;
- bit 15, `RASTER_PHYSICAL_IDENTITY_FOLDING`.

The milestones are sequential and end at mandatory `STOP / REVIEW` gates. Do
not begin the next milestone until the reviewer records approval in the state
file. Do not change default masks, delayed target-color materialization, or the
single shared raster-variant slot in this plan.

## Working set and resume protocol

Tracked plan artifacts:

- `.agent/plans/image-raster-policy-03-audit-and-reuse.md`
- `.agent/state/image-raster-policy-03-audit-and-reuse.md`
- `.agent/evidence/image-raster-policy-03-audit-and-reuse.md`
- `.agent/reports/image-raster-policy-03-audit-and-reuse-editorial.md`

Milestone summaries:

- `.agent/benchmarks/image-raster-policy-03/m1-write-pixels-accounting.md`
- `.agent/benchmarks/image-raster-policy-03/m2-variant-audit.md`
- `.agent/benchmarks/image-raster-policy-03/m3-structural-validation.md`
- `.agent/benchmarks/image-raster-policy-03/m4-rgb565-target-color.md`

On resume, read the state file first, then only the active source paths and
the evidence needed for the next action. The completed Plan 2 state, evidence,
and benchmark summary are bootstrap references, not repeated reading material.
Primary implementation paths are the Skia image-backing/geometry/surface
sources and tests, JPEG loader, `Image.java`, `NativeImageBacking.java`, the
real-workload smoke app, and
`scripts/run-image-scroll-distributed-benchmark.py`.

## Execution and validation rules

Preserve unrelated local changes and untracked files. Stage only explicit task
paths; do not push, amend, rebase, squash, or rewrite history. Before every
commit, validate changed headers, inspect the staged diff, run
`git diff --check --cached`, and use an English Conventional Commit with every
commit-message line at most 80 characters. New tracked files must be at most
20 KiB and approximately at most 600 lines. Keep verbose logs outside the
repository.

Do not run the historical 126-process profile or the standalone decode
benchmark. Use `work_time_ns` for scroll performance; `frame_time_ns` is
context only. Comparable runs use the same 663-file corpus and, when the
historical corpus is present, self-test hash `588a7e0f4019424a`. Resolve it
only from `$TC_IMAGE_CORPUS`, then `~/Downloads/win32/win32`; otherwise
stop.

Current execution status: M1 and M2 are complete, and the approved M3
structural corrections and gate are complete. `STOP / REVIEW 1` is closed;
`STOP / REVIEW 2` is closed by the recorded approval; `STOP / REVIEW 3` is
ready. M4 remains unauthorized.

## Architecture and fixed decisions

### Intrinsic opacity

A successful JPEG decode publishes `SKIA_IMAGE_OPACITY_OPAQUE` regardless of
`RASTER_OPACITY_METADATA` (bit 1). This is based on decoded format, not the
file extension. Preserve intrinsic opacity for RGB565 and Gray8, add no new
PNG assumption, and retain metadata accounting for optional opacity. Report
intrinsic, metadata, and fallback-scan knowledge separately. In particular,
mask 4 is the no-bit-1 proof; mask 32799 includes
`RASTER_OPACITY_METADATA` and must not be described as bit 1 disabled.

### Clipping proof

The native partial-clip writePixels test must verify every expected visible
pixel and representative pixels outside every clipped edge. An inside-only
assertion is insufficient. Fix the device-space planner if this test exposes
an implementation defect.

### Accounting mode

The benchmark supports `--accounting=on|off`, defaulting to `on`. This is
not encoded in the optimization mask. Both modes retain external Java
`System.nanoTime()` work/paint timing. With accounting off, do not update
Java image counters, native backing/raster/writePixels counters, or diagnostic
key tracking structures on the compared render path. Unavailable diagnostic
JSON values are explicitly null/unavailable, and runner validation does not
require them. Preserve historical counter APIs; gate every new diagnostic
counter and the Plan 2 writePixels counters. Do not refactor unrelated
counters.

### Target metrics

The benchmark-only native bridge reports the active Skia software target even
when the global GPU surface is null: physical width, height, effective
rowBytes/pitch, alpha type, numeric color type, numeric `kN32_SkColorType`,
stable classification (`BGRA8888`, `RGB565`, or `OTHER`), and backend.
These are diagnostic only. Structural validation requires BGRA rowBytes >=
width*4, RGB565 >= width*2, or OTHER >= width. Do not truncate physical
dimensions in the transport, and preserve the VM native-name length
constraint.

### Milestone 2 evidence boundary

M2 may add accounting-gated diagnostics, but must not change target-color or
physical cache identity, remove `saveCount == 1`, alter clipping, alter
materialization timing, split the shared slot, or enable an optimization by
default. It answers which restrictions matter; it does not fix them.

### Identity diagnostics

With accounting on, target-color diagnostics record attempts, unique source
backing/generation identities, unique full keys, unique keys without
destination coordinates, unique intrinsic keys, pending replacements,
materializations, hits, and fallbacks. The intrinsic key is exactly:
`sourceGeneration`, `sourceDecodeGeneration`, and `targetColorType`.

Physical diagnostics record lookups, full keys, keys without target surface
width/height, misses, materializations, hits, and evictions. Normalized keys
are diagnostic only in M2. Eligibility diagnostics separately classify
canvas/save state, target surface/pixels, device clip, unsupported
mapping/matrix, partial or unsupported geometry, backing/opacity
incompatibility, and execution/materialization failure. Record bounded
`saveCount` aggregates; do not print from the rendering hot path.

The shared one-slot backing records both replacement directions
(`TARGET_COLOR -> PHYSICAL`, `PHYSICAL -> TARGET_COLOR`) and both
pending-key replacement directions. Do not split the slot; record material
thrashing as a later decision.

### M3 boundary

M3 starts only after human review adds an exact `Approved M3 changes` section
to the state file. Only those listed changes may be implemented. Candidate
changes are: canonical intrinsic target-color identity; removal of physical
surface dimensions when materialization and tests prove independence; and a
provably safe rectangular canvas/clip condition replacing blanket
`saveCount == 1`. Never change delayed materialization or split the slot.

If approved, add native tests for position changes, compatible surface-size
changes, saved rectangular clips, outside-clip protection, translated
positive-scale matrices, and fallback for non-rectangular clips and
rotation/skew. Preserve conservative fallback when Skia cannot prove safety.

### Reuse workload

M4 reuses the same controls and backing objects in one process:

1. forward, minimum to maximum;
2. backward, maximum to minimum;
3. forward, minimum to maximum.

Do not rebuild UI, reconstruct image objects, or clear decoded backing, native
variants, draw-plan caches, or raster-variant caches between passes. With
accounting on, report counter deltas per pass.

## Milestones, gates, and acceptance

### Bootstrap

Verify branch and Plan 2 ancestry, record starting HEAD, and run only static
plan/header/whitespace validation. Suggested commit:
`docs(benchmark): plan raster policy audit`.

### M1 — correctness and benchmark hygiene

Implement intrinsic JPEG opacity, strong clipping proof, accounting on/off,
and complete native target metrics. The build gate is: SDK smoke source and
distribution; macOS ARM64 `tcvm`, `Launcher`, and `skia_surface_test`;
native test; one macOS benchmark bundle; and bundle self-test.

Run the exact `write-pixels-accounting-ab` profile: masks 32795 and 32799,
prefetch on, accounting on/off, five rounds, 20 processes. Interleave modes
and masks deterministically. Also run accounting-on smokes for masks 0, 4,
and 32799 with prefetch off/on. Require the intrinsic JPEG opacity proof with
mask 4/off; report 32799 separately as metadata-enabled.

The M1 artifact reports raw samples and work P50/P95/P99/MAX, paint P50/P95,
the 32795-to-32799 delta, target metrics, and intrinsic/metadata/scan counts
by accounting mode. Do not infer a P95 regression from one process.

M1 accepts only when clipping, intrinsic opacity, accounting-off isolation,
physical target metrics, package self-test, six smokes, and all 20 A/B
processes pass. The corrected rerun satisfied these criteria; `STOP / REVIEW 1`
is approved and closed, and M2 is the next authorized stage. M2 is not
executed by this documentation closure.

### M2 — evidence-only identity and eligibility audit

Run `raster-policy-audit`: masks 8192, 16384, 32768, and 57344; prefetch on;
accounting on; one round; four processes. Collect the diagnostics defined
above and calculate:

- target full keys / unique sources;
- target full keys / intrinsic keys;
- physical full keys / no-surface-size keys;
- save-state rejections / feature attempts.

The report answers whether destination position fragments target identity,
surface dimensions fragment physical identity, canvas/save state dominates
rejection, variants evict one another, and zero hits are due to eligibility,
key fragmentation, delayed observation, or another measured reason. No fix is
allowed. Stop at `STOP / REVIEW 2`; the reviewer must record exact M3
changes.

### M3 — approved structural corrections only

Read only the approved state section and active source paths. Implement no
unapproved candidate. Build the changed SDK bridge if needed, macOS ARM64
`tcvm`/`Launcher` as required, `skia_surface_test`, one bundle, and
self-test.

The approved implementation must retain the corrections from `0129316af`:
canonical target-color identity, physical keys independent of target surface
dimensions, delayed materialization, default masks, and one shared
raster-variant slot. `buildRasterPhysicalPlan()` must prove finite positive
logical-to-physical X/Y scales, axis alignment, content-scale compatibility,
integral source/destination/visible mappings, and safe clips; inconsistent
mappings remain fallback cases. Saved states are eligible only for the known
rectangular clip wrapper. Unknown rectangular states, `saveLayer` or equivalent
compositing, non-rectangular clips, rotation, skew, and perspective remain
fallback cases.

Run `raster-structural-smoke`: masks 0, 8192, 16384, 32768, 57344; prefetch
on; accounting on; two rounds; ten processes. Require 10/10 pass, approved
paths to produce expected candidates/hits or documented measured reasons,
valid target metrics, no clip/pixel corruption, and no writePixels regression.
For masks enabling bits 8192 and 32768, require real attempts and require
their `MappingRootToDevice` counts to be below attempts, with later measured
behavior documented even when delayed materialization leaves hits at zero.
This is observational, not a performance-promotion gate. Stop at
`STOP / REVIEW 3`.

### M4 — reuse and RGB565 versus target color

Add the three-pass reuse workload while preserving historical single-pass
profiles. Diagnostic matrix: masks 0, 32, 8192, 8224; prefetch off/on;
accounting on; one round; eight processes. Require all three passes. Primary
performance matrix: same masks and prefetch modes; accounting off; three
rounds; 24 processes. Report per pass work P50/P95/P99/MAX, paint P50/P95,
process/round identity, lifecycle counters, bytes, target classification, key
multiplicity, and shared-slot replacements.

Interpret only these measured comparisons: 0->32 (RGB565 storage), 0->8192
(target conversion), 32->8224 (conversion on RGB565), and 8192->8224 (RGB565
with conversion). If target is BGRA8888, state that conversion targets
BGRA8888 and RGB565 is separate source storage; if target is RGB565, analyze
the converged color family separately. Do not infer causality from time alone,
change defaults, change delayed materialization, or split the slot. Stop at
`STOP / REVIEW 4`.

## State, evidence, and recovery

At each gate record implementation HEAD, SDK/native/bundle hashes, corpus
count/hash, target metrics, profile and process counts, raw result paths,
measured findings, deferred decisions, and the exact next review boundary.
Never combine samples from different HEADs. If source changes after packaging,
invalidate the package. A failed infrastructure run is fixed narrowly and the
complete small profile is rerun. Do not remove caches or unrelated generated
files merely to clean the worktree.

The editorial report must use exactly these headings: `Editorial Summary`,
`Original Plan versus Actual Outcome`, `What Changed`, `Decisions and
Trade-offs`, `Unexpected Problems and Discoveries`, `Validation and
Measurable Results`, `Useful Evidence and Examples`, `Limitations,
Remaining Work, and Open Questions`, `Possible Article Angles`,
`Suggested Narrative`, and `Claims Requiring Human Review`.

## Decision log and open questions

Fixed decisions are: JPEG opacity is intrinsic; accounting-off keeps only
external active-work timing; M2 is diagnostic only; cache/eligibility changes
require review; delayed materialization and the one raster-variant slot remain;
default masks remain; and no historical full matrix is required.

Do not resolve without evidence/review whether target-color materialization
should become immediate, whether variant kinds need separate slots, whether a
new optimization belongs in defaults, whether RGB565 quality trade-offs merit
default use, whether non-rectangular clips are safe, or whether macOS results
generalize to other platforms.

At final close, distinguish correctness fixes, diagnostic findings, approved
structural corrections, measured performance, and hypotheses needing follow-up.
