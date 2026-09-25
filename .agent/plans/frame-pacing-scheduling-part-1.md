<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Frame pacing and scheduling investigation — Part 1: pacer, Flick drivers, and clocks

This ExecPlan follows `AGENTS.md` and the ExecPlan rules in
`totalcross-depot-tools/.agent/PLANS.md`. It is Part 1 of a two-plan sequence.
Part 2 is `frame-pacing-scheduling-part-2.md` and must be executed only after
this plan closes.

## Purpose / Big Picture

Create a reproducible, causal investigation of frame pacing in the real
663-image scroll workload. Do not optimize rendering in this plan. Measure and
separate three effects:

1. pacing produced by the benchmark's current `Vm.sleep()` loop;
2. the real `Flick` driver: current `TimerEvent` at 40 fps, `TimerEvent` at
   60 fps, and `UpdateListener`;
3. millisecond animation time (`Vm.getTimeStamp()`) versus monotonic
   `System.nanoTime()`.

Every stage ends with a native macOS benchmark. Commit compact machine-readable
benchmark results before starting the next stage. Preserve production defaults
through Part 1: `Flick` remains TimerEvent-driven at 40 fps and uses the legacy
millisecond clock unless the benchmark explicitly selects another mode.

The immutable branch base at plan creation is:

    feat/png-prefetch
    d7a9ec93faf9f2da0724611de115649176d0b033

Create and use this branch in the current worktree:

    feat/frame-pacing-scheduling-diagnostics

Do not rebase, amend, squash, reset, or rewrite history.

## Working Set and Resume Protocol

Create these repository files when activating the plan:

- `.agent/plans/frame-pacing-scheduling-part-1.md`
- `.agent/plans/frame-pacing-scheduling-part-2.md`
- `.agent/state/frame-pacing-scheduling.md`
- `.agent/evidence/frame-pacing-scheduling.md`
- `.agent/archive/frame-pacing-scheduling-history.md`
- `.agent/reports/frame-pacing-scheduling-editorial.md`

The state file is always the first read when resuming. Read the active plan
section only after state. Search the evidence index only for the active stage.
Do not reread the completed PNG plans or their raw history unless a provenance
check requires one exact fact.

Expected implementation paths in Part 1:

- `TotalCrossSDK/src/main/java/totalcross/ui/Flick.java`
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/FlickBenchmarkSupport.java`
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageScrollRealWorkloadBenchmarkApp.java`
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/SyntheticPacingTest.java`
- `TotalCrossSDK/src/test/java/totalcross/ui/FlickTest.java` or the nearest
  existing focused Flick test file
- `scripts/run-frame-pacing-benchmark.py`
- `scripts/frame_pacing_contract.py` and `scripts/frame_pacing_results.py`
- `scripts/test-frame-pacing-benchmark.py`
- `scripts/package-image-scroll-benchmark.sh`
- `scripts/README-image-benchmarks.md`

Do not refactor existing large files merely to satisfy a size policy. Every new
file must remain below 20 KiB and approximately 600 lines. Split a new helper
before it crosses either limit.

## Activation and preservation

Read, once, in full:

    AGENTS.md
    .agents/skills/logical-commits/SKILL.md
    totalcross-depot-tools/.agent/PLANS.md

Then record:

    git status --short
    git rev-parse feat/png-prefetch
    git rev-parse HEAD

The `feat/png-prefetch` ref must resolve to
`d7a9ec93faf9f2da0724611de115649176d0b033`. If it moved, record a blocker and
do not silently choose a different base.

If `feat/frame-pacing-scheduling-diagnostics` does not exist, create it from
that exact SHA:

    git switch -c feat/frame-pacing-scheduling-diagnostics \
      d7a9ec93faf9f2da0724611de115649176d0b033

If it already exists, switch to it only when its ancestry contains the immutable
base. Preserve all unrelated tracked and untracked worktree changes. Record
their paths in state and never stage them.

Commit the two plan files plus initial state/evidence/archive/report scaffold as
one signed logical activation commit. Validate the commit message using the
`logical-commits` skill. Do not push in Part 1.

## Current Architecture and fixed experimental contract

The real workload is
`ImageScrollRealWorkloadBenchmarkApp`: 663 image paths, three columns, 3000 ms
scroll duration, and current synthetic pacing based on a 16 ms interval.
The corpus must contain exactly 663 `.jpg`/`.jpeg`-named files whose payload
magic is 660 JPEG and 3 PNG. Prefetch must complete before measured scroll.

Use the same workload geometry and image corpus for every measured configuration.
Use ImageOptimizations mask `6`, prefetch `on`, the
`worker-semaphore` preparation strategy, and no diagnostic accounting during
measured runs. Before each stage's measured matrix, run one correctness process
with accounting enabled and require:

- 663 requests;
- 663 READY;
- 0 failed;
- 0 not-prefetchable;
- zero scroll-time full decodes/materializations for the prefetched corpus.

A measured configuration always runs three fresh processes. One process equals
one independent sample. Never run multiple experimental configurations in the
same VM process.

The benchmark must produce a compact row containing at least:

- stage and configuration name;
- source commit and runtime identity;
- frame count and callback count;
- frame interval P50/P95/P99/MAX;
- active work P50/P95/P99/MAX;
- paint P50/P95/P99/MAX;
- counts over 16.67, 20, 25, 33.3, 50, and 100 ms;
- callback delta P50/P95/P99/MAX when a Flick driver is active;
- callback absolute-lateness P50/P95/P99/MAX relative to an ideal timeline
  anchored at the first measured callback;
- callback delta error P50/P95/P99/MAX relative to the configured interval;
- measured wall duration;
- the selected driver, fps, clock, timer deadline policy, event-loop policy,
  and yield policy.

The ideal interval is configuration data, not inferred from observed callbacks:
25,000,000 ns for timer-40; 16,000,000 ns for timer-60 and the current
UpdateListener cadence.

For the synthetic pacer also record sleep request count, total requested sleep,
total actual sleep, oversleep P50/P95/P99/MAX, and deadline error
P50/P95/P99/MAX.

The macOS runner writes only two canonical persistent result files per stage:

    .agent/evidence/frame-pacing-stage-N-macos.csv
    .agent/evidence/frame-pacing-stage-N-macos.json

Keep each below 20 KiB. Per-process stdout/stderr and temporary run directories
are execution logs and remain outside Git. The committed CSV/JSON are the
canonical benchmark results required by this plan.

## Benchmark harness architecture

Add `scripts/run-frame-pacing-benchmark.py` as a small data-driven runner. It
must support:

    --stage 1|2|3|4|5
    --rounds 3
    --bundle <native bundle directory>
    --evidence-csv <path>
    --evidence-json <path>

Part 1 implements stages 1-3; Part 2 extends the same runner for 4-5. Do not
duplicate process-launch, JSON validation, percentile, or CSV code.

Add focused contract tests in `scripts/test-frame-pacing-benchmark.py`. Tests
must validate exact stage matrices, required summary fields, failure handling,
and evidence row counts without launching a native application.

Extend `package-image-scroll-benchmark.sh` only enough in Part 1 to compile and
package `FlickBenchmarkSupport` with the existing benchmark application. Do not
add the Windows pacing runner until Part 2.

For native macOS runs, create a temporary SDK ZIP from `build/TotalCross` after
the permitted milestone-end SDK build. Copy the current macOS `libtcvm.dylib`
and `Launcher` into that temporary SDK layout, zip it, and invoke the existing
image-scroll package script for `macos-arm64`. Do not commit SDK ZIPs, deployed
binaries, CMake output, Gradle output, or normal logs.

Require `TC_IMAGE_CORPUS` to point to the same corpus root used by the PNG
benchmark. If it is unset, use a previously recorded existing absolute corpus
path only if the state/evidence file explicitly names it. Otherwise record a
blocker; do not search arbitrary user directories.

## Stage 1 — measure the synthetic pacer itself

Goal: prove how much of the observed ~16-17 ms distribution is introduced by
the benchmark's own pacing and millisecond sleeps.

Keep the existing synthetic algorithm behavior intact for its legacy mode.
Instrument every wait operation using `System.nanoTime()` around `Vm.sleep()`.
Do not replace the sleep yet.

Add exactly two synthetic configurations:

- `synthetic-current-16ms`: 16,000,000 ns target;
- `synthetic-60hz`: 16,666,667 ns target.

Both use the current absolute `start + frameIndex * interval` deadline model
and the same ceil-to-millisecond sleep strategy. The second configuration
changes only the target interval.

Record for each frame:

    target deadline
    actual frame start
    deadline error
    requested sleep amount
    actual sleep elapsed
    sleep call count
    active work and paint time

Do not persist per-frame files in Git; aggregate them into the canonical stage
CSV/JSON.

Focused tests must prove:

- legacy mode still targets exactly 16,000,000 ns;
- 60 Hz mode targets exactly 16,666,667 ns;
- sleep overshoot is `actualSleep - requestedSleep`, clamped only for impossible
  negative clock noise;
- deadline error uses the configured absolute deadline;
- frame thresholds remain independent from the pacing interval.

Before the benchmark, commit the implementation and focused tests as logical
signed commits. At milestone close, local builds are permitted. Establish a
fresh local SDK package and native macOS runtime if not already valid:

    (cd scripts && ./package-sdk.sh)
    cmake -S TotalCrossVM -B build/frame-pacing-macos \
      -DCMAKE_BUILD_TYPE=Release \
      -DCMAKE_OSX_ARCHITECTURES=arm64 -G Ninja
    cmake --build build/frame-pacing-macos \
      --target tcvm Launcher --parallel

Capture verbose output to `/tmp` logs and print only status and a short failure
tail. Run the Stage 1 macOS matrix with three fresh processes per configuration,
write the canonical Stage 1 CSV/JSON, inspect the values without drawing a
performance conclusion, and commit the two result files plus compact evidence
index/state updates in a signed `docs(plan)` checkpoint.

Acceptance: six measured processes pass; correctness preflight passes; the
sleep/deadline fields are populated and internally consistent.

## Stage 2 — compare real Flick drivers at millisecond time

Goal: exercise the real animation mechanism and separate the historic 40 fps
limit from TimerEvent-versus-UpdateListener behavior.

Refactor `Flick` so all frame callbacks call one private advancement method.
The default TimerEvent/40 behavior must remain semantically unchanged.

`Flick` must implement both `TimerListener` and `UpdateListener`. Add internal,
benchmark-only configuration for:

    driver = timer | update
    timer fps = 40 | 60

Do not expose a new public SDK API. Add
`TotalCrossSDK/src/smokeTest/java/totalcross/ui/FlickBenchmarkSupport.java` in
package `totalcross.ui`; it may call package-private test hooks in `Flick` and
expose only benchmark-side static helpers.

Track the active frame-driver registration explicitly. Starting a Flick may
register exactly one TimerEvent or one UpdateListener. Stopping is idempotent
and unregisters exactly the active mechanism. Switching a driver while a Flick
is active is forbidden by the test hook.

Use one deterministic benchmark motion for all Flick configurations:

- vertical direction: UP;
- duration: 3000 ms;
- target displacement magnitude: 22,440 logical pixels;
- initial velocity and constant acceleration derived so velocity reaches zero
  at 3000 ms and total displacement is -22,440 px.

The test hook configures those exact physics values and starts the normal Flick
lifecycle without fabricating mouse timing. Do not duplicate the advancement
formula in the benchmark application.

Stage 2 matrix, three fresh processes each:

- `timer-40-millis`
- `timer-60-millis`
- `update-millis`

All use the legacy `Vm.getTimeStamp()` animation clock.

Tests must prove default behavior is TimerEvent/40, timer registration interval
is 25 ms at 40 fps and 16 ms at 60 fps, UpdateListener mode creates no Flick
TimerEvent, stop unregisters once, and all three drivers feed the same
advancement method.

Commit the refactor separately from the new driver behavior when both are
non-trivial. Then commit harness/matrix work separately.

At milestone close rerun the SDK package build because `Flick.java` changed.
Reuse the Stage 1 native macOS runtime only if `git diff` proves no native source
changed since its build; otherwise rebuild macOS now. Package and run Stage 2.
Commit the Stage 2 CSV/JSON and state/evidence checkpoint.

Acceptance: nine measured processes pass, every configuration traverses the same
deterministic Flick motion contract, and production defaults remain Timer/40.

## Stage 3 — compare millisecond and nano animation clocks

Goal: isolate clock precision from driver choice. Do not modify the native
timer scheduler in this stage.

Add a benchmark-only Flick clock selector:

    millis -> Vm.getTimeStamp()
    nano   -> System.nanoTime()

Keep gesture sampling and drag velocity calculation on the existing
`Vm.getTimeStamp()` path. Only elapsed animation time after Flick start changes.

For nano mode, capture `animationStartNs = System.nanoTime()` at the same
logical start point as the existing `t0`, and compute:

    elapsedMs = (System.nanoTime() - animationStartNs) / 1_000_000.0

The existing position equation continues to use milliseconds. Do not round
elapsed nano time back to an integer before applying the physics formula.

Stage 3 matrix, three fresh processes each:

- `timer-60-millis`
- `timer-60-nano`
- `update-millis`
- `update-nano`

Timer-40 is intentionally excluded because Stage 2 already establishes its
frequency effect.

Focused tests must prove identical position calculations at exact integer
millisecond boundaries, sub-millisecond progress exists only in nano mode,
default clock remains millis, and nano mode never interprets the value as wall
clock.

Commit implementation/tests before benchmarking. At milestone close rerun the
SDK package build. Reuse the existing macOS native runtime if native sources are
unchanged. Run Stage 3, commit the 12-row canonical results, and update state so
Part 2 is the next read.

Acceptance: twelve measured processes pass; clock selection is explicit in
every result row; no native scheduler or event-loop source changed in Part 1.

## Progress

Use this section as a compact live checklist; rewrite rather than duplicating
history in multiple files.

- [x] (2026-09-25) Activate the plan branch and commit the plans and supporting
  state/evidence scaffolding.
- [x] (2026-09-25) Stage 1 implementation, focused tests, preflight, and six
  measured processes passed; canonical results are in the signed checkpoint.
- [x] (2026-09-25) Stage 2 Flick drivers, tests, preflight, and nine measured
  processes passed; canonical results are in the signed checkpoint.
- [x] (2026-09-25) Stage 3 clock implementation, tests, preflight, and twelve
  measured processes passed; canonical results are in the signed checkpoint.
- [x] (2026-09-25) Part 1 state is complete; Part 2 is the next sequenced plan.

## Decision Log

- Production defaults remain TimerEvent, 40 fps, millisecond clock through
  Part 1.
- `FlickBenchmarkSupport` is smoke-test/benchmark code; no new public SDK API.
- The deterministic Flick motion is fixed at 22,440 px over 3000 ms.
- Three fresh processes per measured configuration are descriptive samples, not
  statistical proof.
- Stage result CSV/JSON are committed; build products, deployed binaries, raw
  logs, and temporary run directories are not.
- Image decode/render optimization masks are not an experimental variable.

## Validation and Acceptance

For every functional commit:

    python3 scripts/test-frame-pacing-benchmark.py
    git diff --check
    python3 scripts/validate-copyright-headers.sh --files <changed first-party files>
    git diff --check --cached

Run focused Java tests for `Flick` whenever `Flick.java` changes. Run
`scripts/test-image-scroll-distributed-benchmark.py` whenever shared packaging
or benchmark contracts change.

Builds are permitted only at milestone closure and only for SDK and macOS.
Do not locally build Windows, Linux, Android, or iOS.

All commits are signed and follow `.agents/skills/logical-commits/SKILL.md`.
Use frequent behavior-oriented commits. Never amend a bad message; add a
corrective/revert commit if necessary.

## Risks and Open Questions

Do not decide conclusions while implementing. A result may show Timer60 and
UpdateListener are equivalent; preserve both paths until Part 2 completes.

`System.nanoTime()` precision does not imply precise wakeups. Stage 3 measures
physics/measurement clock precision only. Scheduler deadlines and waits belong
to Part 2.

## Idempotence and Recovery

A benchmark stage may be rerun safely into a fresh `/tmp` directory. Overwrite
the canonical stage CSV/JSON only after all expected processes validate. On a
partial failure, retain the previous committed evidence and record the failed
attempt in the append-only evidence index.

Do not clean the worktree. Do not delete prior build directories merely to make
status clean. If a required local SDK/native artifact is missing after resume,
rebuild it only at the current milestone boundary.

## Outcomes & Retrospective

At each stage closure record only facts: implementation commit(s), validation
status, process counts, compact metric ranges, and evidence paths. Defer causal
conclusions until the final editorial report in Part 2.

## Revision Note

Initial plan split into two sequential files to remain below the user's 20 KiB
and ~600-line limit while preserving all architectural decisions.
