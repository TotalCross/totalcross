<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Pre-image master work 02: benchmark native swap and clean Skia flags

This is plan 2 of 2. Execute it only after
`.agent/plans/pre-image-optimization-master-01-correctness.md` is complete on
`fix/pre-image-optimization-master`.

This ExecPlan follows `AGENTS.md`, `.agent/PLANS.md`, and
`.agents/skills/logical-commits/SKILL.md`. It is self-contained and must be
executed without relying on conversation history.

## Purpose / Big Picture

Continue the branch produced by plan 1. Benchmark `USE_NATIVE_SWAP` with the
actual optimized host compilers, locally on macOS and on native GitHub-hosted
Windows/Linux x86-64 and ARM64 runners. Apply the fixed keep/remove rule below,
remove redundant Skia macro-default duplication, and leave the branch as the
master merge candidate that image-optimization branches should later rebase on.

Do not implement image-optimization features and do not rebase those branches.

## Branch and Prerequisite Contract

The active branch must be:

    fix/pre-image-optimization-master

Read plan-1 state/editorial and obtain its recorded
`PLAN1_IMPLEMENTATION_HEAD`. Verify that the implementation commit is an
ancestor of the current branch without requiring the current branch tip to
equal it:

    PLAN1_IMPLEMENTATION_HEAD=$(...)
    git merge-base --is-ancestor "$PLAN1_IMPLEMENTATION_HEAD" HEAD

If that check fails, stop and inspect only the branch history needed to explain
the mismatch. Then inspect every commit after
`PLAN1_IMPLEMENTATION_HEAD` and before plan 2 starts. Their changed paths must
be limited to this plan's and plan 1's handoff artifacts:

    .agent/plans/pre-image-optimization-master-01-correctness.md
    .agent/plans/pre-image-optimization-master-02-native-swap.md
    .agent/state/pre-image-optimization-master-01-correctness.md
    .agent/state/pre-image-optimization-master-02-native-swap.md
    .agent/evidence/pre-image-optimization-master-01-correctness.jsonl
    .agent/evidence/pre-image-optimization-master-02-native-swap.jsonl
    .agent/reports/pre-image-optimization-master-01-correctness-editorial.md
    .agent/reports/pre-image-optimization-master-02-native-swap-editorial.md

If any other path changed, stop. After these checks, adopt the actual current
branch tip as `PLAN2_BASE` and record it in plan-2 state. Do not require a
stable exact tip for plan 1; handoff documentation may advance the branch tip.

Feature-branch commits are authorized. A normal fast-forward push of this branch
is authorized solely to execute the required GitHub Actions benchmark. Never
push/update `master`, force-push, amend, merge, tag, or publish a release.

Invoke `.agents/skills/logical-commits/SKILL.md` before every commit.

## Working Set and Resume Protocol

Commit this plan at:

    .agent/plans/pre-image-optimization-master-02-native-swap.md

Use:

    .agent/state/pre-image-optimization-master-02-native-swap.md
    .agent/evidence/pre-image-optimization-master-02-native-swap.jsonl
    .agent/reports/pre-image-optimization-master-02-native-swap-editorial.md
    .agent/benchmarks/pre-image-optimization-master/native-swap/

After interruption read state first. It must contain `PLAN1_IMPLEMENTATION_HEAD`,
`PLAN2_BASE`, active milestone/slice, last commit, next exact action, benchmark jobs/results already
collected, validation completed/deferred, and blockers. Do not reread all raw
benchmark samples or plan 1 unless a recorded dependency requires it.

Primary implementation paths:

    TotalCrossVM/src/nm/ui/skia/skia_internal.h
    TotalCrossVM/src/nm/ui/skia/skia.cpp
    TotalCrossVM/src/nm/ui/skia/skia_surface.cpp
    .github/workflows/
    .agent/benchmarks/pre-image-optimization-master/native-swap/

Inspect only the current `USE_NATIVE_SWAP`, `SWAP32`, and compile-flag default
regions before editing.

## Execution, Artifact, Build, and Size Rules

Operate token-efficiently. Keep raw samples in files and summaries in evidence;
do not print sample-by-sample output into chat/state/report.

Every new file must stay below 20 KiB and approximately 600 lines. Split raw
benchmark results by platform and image size as needed. Do not refactor existing
files merely to reduce their size.

Commit all direct artifacts: benchmark source, workflow, raw structured samples,
summary, state/evidence/editorial, and production cleanup. Do not commit normal
build products, executable benchmark binaries, caches, or verbose logs.

Local build operations are limited to SDK and macOS native builds, only at the
end of related milestones. This plan normally needs no SDK build. Native smokes
may run only at milestone end and final completion.

The required GitHub benchmark is the only cross-platform compilation exception:
Windows/Linux jobs may compile the standalone microbenchmark only. They must not
configure/build the TotalCross SDK, TCVM, Skia, SDL, launcher, or package output.

## Current Architecture and Scope

`USE_NATIVE_SWAP` only selects the 32-bit byte-swap expression used by legacy
`skia_makeBitmap()` conversion:

- native/compiler path: `__builtin_bswap32` on GCC/Clang or `_byteswap_ulong`
  on MSVC;
- portable path: the existing shift/mask expression.

Current policy disables native swap on Apple/Android and enables it elsewhere.
Historical motivation was weak embedded CPUs, but modern compilers can generate
identical or better code from the portable expression. Decide from measurement,
not assumption.

Default-definition blocks for `USE_COMPUTE_OPAQUE`,
`USE_COLORTYPE_CONVERSION`, and `USE_NATIVE_SWAP` are duplicated between
`skia_internal.h` and `skia.cpp`; `USE_WRITE_PIXELS` also belongs to the same
legacy configuration family. `skia_internal.h` must be the single authoritative
default-definition site after this plan.

Do not remove or change the behavior/defaults of `USE_COMPUTE_OPAQUE`,
`USE_WRITE_PIXELS`, or `USE_COLORTYPE_CONVERSION`. Their redesign belongs to the
image-optimization series. Do not port target color-type conversion to
`NativeImageBacking` here.

## Explicitly Out of Scope

Do not implement:

- runtime opacity/writePixels flags from Phase 2;
- physical-transform folding/materialization cache;
- GPU backing policy;
- compact image formats or color-type specialization;
- any image-optimization branch rebase.

## Progress

- [x] Milestone 0: bootstrap plan-2 state and add benchmark harness/workflow.
- [x] Milestone 1: collect macOS and native Windows/Linux benchmark evidence.
- [x] Milestone 2: apply fixed decision rule and clean duplicate macro defaults.
- [x] Milestone 3: final macOS validation and master/rebase handoff.
- [x] Corrective rerun: replace indirect-dispatch benchmark evidence with
  production-like buffer-level loops and recompute the policy.

## Benchmark Design Contract

Create exactly this standalone benchmark source:

    TotalCrossVM/src/nm/ui/skia/benchmarks/native_swap_benchmark.cpp

It must exercise the exact two swap expressions without linking Skia or
TotalCross. The executable interface is fixed:

    native_swap_benchmark <width> <height> <warmups> <samples> <raw.csv> <summary.json>

It must write one raw CSV row per paired sample and one compact JSON summary for
that size. Do not add a second summarizer unless this fixed interface proves
impossible on a supported host.

Requirements:

- allocate deterministic source/destination buffers before timing;
- keep allocation, startup, file I/O, formatting, and checksum reporting outside
  timed regions;
- prevent dead-code elimination with a checksum consumed after each sample;
- keep native and portable swap functions non-inlined;
- alternate measurement order A/B then B/A across paired samples;
- use identical source data/work per pair;
- record time in a monotonic high-resolution clock;
- emit compact machine-readable raw samples and a concise summary.

Benchmark independently:

    512 x 512
    1920 x 1080
    3840 x 2160

Use three warmups. Run 60 paired samples as the preliminary checkpoint, then 200
paired samples as the final recorded checkpoint on every required platform.
Store raw samples in separate files per platform/architecture/size/sample-count
so each file remains below 20 KiB.

Summary fields must include median, p95, native/portable ratio, compiler/version,
OS, architecture, warmups, sample count, pixel count, and checksum agreement.

Optimized compile mode is fixed. On macOS/Linux compile to a temporary
executable with:

    c++ -std=c++17 -O3 -DNDEBUG \
      TotalCrossVM/src/nm/ui/skia/benchmarks/native_swap_benchmark.cpp \
      -o /tmp/native_swap_benchmark

On Windows use `cl /nologo /std:c++17 /O2 /DNDEBUG /EHsc` and emit the
executable into the runner temporary directory. Benchmark binaries are never
committed.

The macOS local run uses and records `uname -m`; do not assume architecture.

## Required GitHub Actions Matrix

Create `.github/workflows/native-swap-benchmark.yml` with native hosted jobs:

- `ubuntu-22.04` — Linux x86-64;
- `ubuntu-22.04-arm` — Linux ARM64;
- `windows-2022` — Windows x86-64;
- `windows-11-arm` — Windows ARM64.

Do not use QEMU, Docker cross-emulation, or the repository's ARM32 emulated job
as performance evidence. If a required native runner is unavailable to this
repository, record the failure and stop the decision. Do not substitute an
emulated architecture.

Linux jobs use the host C++ compiler. Windows jobs locate Visual Studio with
`vswhere`, invoke `Common7\Tools\VsDevCmd.bat` with `-arch=x64`/
`-host_arch=x64` or `-arch=arm64`/`-host_arch=arm64`, then invoke `cl` only for
the standalone benchmark. Do not hard-code a Visual Studio edition path.

The workflow must not fetch TotalCross native dependencies and must not build
TotalCross targets.

To execute a new workflow before it exists on `master`, initially support:

- `workflow_dispatch`; and
- `push` restricted to `fix/pre-image-optimization-master`.

After all required CI results are collected, remove the branch-specific `push`
trigger and leave the final workflow `workflow_dispatch` only.

## Fixed `USE_NATIVE_SWAP` Decision Rule

Apply exactly this rule. The executor must not invent platform policy.

1. If native has no stable >=5% median win on any Windows/Linux target/size and
   the portable loop is not stably >5% slower anywhere, remove `USE_NATIVE_SWAP`
   and use the portable expression unconditionally.
2. If native has a stable >=5% win on at least one target and no stable >5%
   regression on another target where it is enabled, retain the current policy.
3. If there is a stable >=5% native win on one target and a stable >5%
   regression on another, choose the simplest platform/architecture default
   policy supported by the measurements while preserving Apple/Android
   behavior. Do not introduce runtime selection.
4. “Stable” means the >=5% result occurs in both the 60- and 200-pair
   checkpoints for the same platform/architecture/size. macOS remains
   corroborating evidence only.

A runner failure or invalid benchmark is not evidence. Leave the milestone
incomplete until required measurements exist or the user changes the contract.

## Plan of Work

### Milestone 0 — Add reproducible benchmark infrastructure

Verify `PLAN1_IMPLEMENTATION_HEAD` is an ancestor of `HEAD`, validate the
handoff-only commit path allowlist, and set `PLAN2_BASE=$(git rev-parse HEAD)`.
Create concise state/evidence/editorial skeletons recording both values.
Add the fixed standalone C++ benchmark and GitHub workflow per the contracts
above. The C++ program itself must emit both raw CSV and summary JSON; do not add
a separate summarizer.

Before committing, run a short local macOS smoke benchmark only (3 warmups, 10
pairs) to prove checksum equality and output format. This is not final evidence
and does not require a TotalCross build.

Run focused header validation and `git diff --check --cached`. Commit benchmark
infrastructure, for example:

    test(skia): add native swap benchmark

Acceptance: local smoke output is valid and the workflow contains only the four
required native jobs with no TotalCross build steps.

### Milestone 1 — Collect final benchmark evidence

Run local macOS 60-pair preliminary and 200-pair final checkpoints for all three
sizes. Save structured raw files and summary under the committed benchmark
artifact directory. Do not commit the benchmark executable.

Fast-forward push the feature branch to `origin` so the temporary branch-specific
workflow trigger runs. Before pushing:

    git fetch origin fix/pre-image-optimization-master || true

If the remote branch exists and local is not a normal fast-forward update, stop.
Never force-push.

Wait for/inspect only the native-swap workflow jobs. Rerun failed jobs when the
failure is transient; do not rerun successful jobs merely for uniformity.
Download structured artifacts from all four required jobs.

Commit raw samples and concise summary. Suggested subject:

    test(skia): record native swap results

The summary must state the fixed decision rule outcome but must not change
production code yet.

Acceptance:

- macOS local 60/200 evidence exists;
- Linux x86-64 and ARM64 60/200 evidence exists;
- Windows x86-64 and ARM64 60/200 evidence exists;
- all checksums agree;
- each result identifies compiler/OS/architecture;
- no emulation result participates.

### Milestone 2 — Apply decision and clean macro defaults

Apply the fixed decision rule to the committed summary.

If rule selects removal:

- delete `USE_NATIVE_SWAP` default definitions;
- remove the conditional selection around `SWAP32`;
- keep one portable shift/mask implementation;
- remove dead native-swap helper code/includes.

If rule selects retention:

- keep current Apple/Android vs other-platform defaults unchanged;
- keep native and portable implementations unchanged.

If rule selects differentiated defaults:

- encode only the minimal platform/architecture condition justified by the
  stable measurements;
- preserve Apple/Android behavior and do not add runtime selection.

In both cases:

- remove redundant macro default-definition blocks from `skia.cpp`;
- keep every surviving compile-time flag default only in `skia_internal.h`;
- do not change `USE_COMPUTE_OPAQUE`, `USE_WRITE_PIXELS`, or
  `USE_COLORTYPE_CONVERSION` semantics/defaults;
- change no image backing, draw, or decode behavior.

Remove the temporary feature-branch `push` trigger from the benchmark workflow;
leave `workflow_dispatch` only.

Use a focused logical production commit. Suggested subject if the flag is
removed:

    refactor(skia): remove redundant native swap flag

If retained and only duplicate definitions are removed, use a subject describing
that cleanup rather than claiming a performance change.

At milestone end, if native production code changed, build one Release macOS
software-Skia runtime using the same `build-preopt-macos` CMake configuration
from plan 1. Build and run the existing `skia_surface_test` target because it
exercises legacy Skia bitmap/surface creation directly:

    ninja -C build-preopt-macos skia_surface_test
    ./build-preopt-macos/skia_surface_test

If the executable is emitted in a configuration-specific subdirectory, locate
that single freshly built target under `build-preopt-macos` and run it there; do
not rebuild another platform. Do not run Windows/Linux TotalCross builds; CI
evidence is only the standalone benchmark.

Acceptance: selected policy exactly matches benchmark rule, macro defaults are
single-source, and unrelated optimization flags are behaviorally unchanged.

### Milestone 3 — Final validation and handoff

Inspect branch diff against the plan-1 `BASE_SHA` by stat first. Confirm the
branch contains only:

- plan-1 targeted JPEG correctness work;
- native-swap benchmark/evidence and its rule-selected cleanup;
- minimal Skia macro-definition cleanup/documentation.

Run scoped copyright validation and `git diff --check`. Repeat macOS native build
or smoke only if production C/C++ changed after Milestone 2. Do not run SDK
build/tests unless SDK production source changed after plan 1's final checkpoint.

Update the editorial report with:

- plan-1 `BASE_SHA`, `PLAN1_IMPLEMENTATION_HEAD`, and plan-2 `PLAN2_BASE`;
- final branch HEAD;
- complete native-swap platform matrix and measured decision;
- exact production macro policy after the decision;
- flags deliberately deferred to image-optimization branches;
- commands/validations actually run and limitations;
- statement that this final HEAD is the intended master merge candidate.

Commit final factual handoff, for example:

    docs(image): complete pre-optimization master fixes

Do not merge to master and do not rebase optimization branches. Report the exact
final HEAD and later intended rebase order:

    perf/image-opt-phase1-controls
      -> perf/image-opt-phase2-raster
      -> subsequent image-optimization branches

## Validation and Acceptance

Every commit must use the logical-commits skill, focused header validation, and:

    git diff --check --cached

Final plan-2 acceptance requires all four native CI architectures plus local
macOS evidence, deterministic checksum parity, a decision derived only from the
fixed rule, and no Windows/Linux TotalCross build.

## Surprises & Discoveries

The first result set was invalid for policy because its per-pixel function
pointer prevented production-like loop optimization. The corrected direct-loop
matrix makes Linux x86-64 and Windows x86-64 effectively equal, while Windows
ARM64 has a stable greater-than-5-percent native win at every size. No
Windows/Linux target has a stable greater-than-5-percent regression.

## Decision Log

- Benchmark native swap on native x86-64/ARM64 runners; never use QEMU evidence.
- Compile only the standalone benchmark on Windows/Linux.
- Use 60-pair preliminary and 200-pair final checkpoints.
- Apply the corrected fixed rule without inventing runtime selection.
- Retain the current `USE_NATIVE_SWAP` defaults because Windows ARM64 has a
  stable win and no enabled Windows/Linux target has a stable regression.
- Keep `skia_internal.h` as the sole default-definition site for surviving
  legacy Skia compile flags.
- Leave opacity/writePixels/color-type optimization changes to the image series.

## Risks and Open Questions

There are no architecture choices left to the executor. Stop rather than guess
if a required native runner is unavailable, checksum parity fails, timing is
optimized away, or branch push would require force.

## Idempotence and Recovery

Raw benchmark filenames must include platform, architecture, size, and sample
count. A rerun may add a clearly distinguished rerun file; do not silently
replace committed evidence used for a decision.

If CI partially succeeds, retain partial data only as labeled evidence and rerun
missing/failed jobs. Do not finalize the decision until all required native jobs
exist.

Before each commit/push, inspect scoped status/diffs only and preserve unrelated
local changes.

## Outcomes & Retrospective

The first benchmark result set and policy conclusion were superseded because
the harness used per-pixel indirect dispatch. The corrective slice replaced the
source, reran all required local macOS and native Linux/Windows checkpoints,
and replaced the 30 CSV/JSON artifacts. The corrected evidence retains the
current policy under rule 2: Windows ARM64 has stable wins and no enabled
Windows/Linux target has a stable regression. Duplicate defaults remain removed
from `skia.cpp`; the focused macOS software-Skia surface test passes.
Measurements are microbenchmark evidence, not end-to-end image-performance
claims.

## Revision Note

Initial revision. This plan completes master-preparation work after the targeted
JPEG correctness fix by measuring and rationalizing the legacy native byte-swap
path without importing image-optimization features.
