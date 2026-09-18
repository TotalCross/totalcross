<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Plan 2 of 2 — run and record the full macOS image diagnostics benchmark

This ExecPlan follows `AGENTS.md`, the ExecPlan rules in
`TotalCross/totalcross-depot-tools:.agent/PLANS.md`, and
`.agents/skills/logical-commits/SKILL.md`.

Execute only on branch `perf/image-decode-distributed-benchmark`, after Plan 1
(`image-scroll-diagnostics-macos-01-instrumentation.md`) is complete.

## Purpose / Big Picture

Build/package the exact final diagnostic revision for macOS ARM64, execute the
existing full distributed image benchmark, validate the new diagnostic
invariants, and finish by reporting absolute paths for the raw results directory
and combined results ZIP.

This plan does not implement any optimization policy. It measures the evidence
needed for a later `OPAQUE_WRITE_PIXELS` / physical-variant policy plan.

## Working Set and Resume Protocol

Save this plan at:

- `.agent/plans/image-scroll-diagnostics-macos-02-benchmark.md`

Create and commit:

- `.agent/state/image-scroll-diagnostics-macos-02-benchmark.md`
- `.agent/evidence/image-scroll-diagnostics-macos-02-benchmark.md`
- `.agent/reports/image-scroll-diagnostics-macos-02-benchmark-editorial.md`
- `.agent/benchmarks/image-scroll-diagnostics-macos/summary.md`

On resume, read this plan's state first. Read Plan 1 state/report once only to
verify its final commit and diagnostic contract. Do not reread Plan 1 source
investigation unless final validation reveals a contradiction.

Relevant existing tooling:

- `scripts/package-sdk.sh`
- `scripts/package-image-scroll-benchmark.sh`
- `scripts/run-image-scroll-distributed-benchmark.py`
- `scripts/run-image-decode-benchmark.py`
- `scripts/aggregate-image-decode-benchmark.py`

## Preconditions

Require:

    test "$(git branch --show-current)" = "perf/image-decode-distributed-benchmark"

Read:

- `.agent/state/image-scroll-diagnostics-macos-01-instrumentation.md`
- `.agent/reports/image-scroll-diagnostics-macos-01-instrumentation-editorial.md`

Do not start the full run unless Plan 1 records:

- SDK build PASS;
- macOS ARM64 `tcvm` and `Launcher` build PASS;
- scroll self-test/smokes PASS;
- mask-4 rejection/candidate diagnostics present;
- JPEG phase/frame tier/timing diagnostics present;
- no optimization policy change.

Record current HEAD in Plan 2 state. If implementation paths changed after Plan
1's final validation, this plan must rebuild from the new HEAD before running.

## Execution Constraints

- Operate economically with tokens/output; use narrow reads and concise logs.
- Preserve unrelated local changes.
- No Android, iOS, Windows, Linux, or platform matrix builds.
- Build operations are allowed only for SDK and macOS, and only at the final
  build/package milestone below.
- Native smoke is allowed at the final milestone.
- Build/log/raw benchmark outputs remain outside Git.
- All compact repository artifacts created by this plan must be committed.
- Every new tracked file must remain <= 20 KiB and approximately <= 600 lines.
- Raw benchmark directories/ZIPs remain operational measurement output outside
  Git because they exceed this file-size policy. Record absolute paths and
  SHA-256 hashes in committed evidence/summary.
- Do not push.

## Diagnostic Contract to Verify

The final result must preserve these Plan 1 meanings.

writePixels:

- `attempts == hits + fallbacks`;
- global attempts/hits/fallbacks may include direct image/physical copies under
  composite masks; mask 4 remains authoritative for `OPAQUE_WRITE_PIXELS`,
  while detailed rejection/candidate counters cover regular `tryWritePixels*`;
- detailed structural/downstream rejection counters;
- `writePixelsDeviceOneToOneCandidates`;
- `writePixelsDeviceOneToOneKnownOpaqueCandidates`;
- candidate counters <= attempts;
- feature state `ATTEMPTED_NO_HIT` when attempts > 0 and hits = 0.

JPEG:

- successful native decode total count/ns;
- count/ns buckets for denominator 1, 2, 4, 8, other;
- mode counts for full, target, explicit-ratio, best-fit;
- failure count;
- prefetch and measured-scroll sections separated;
- per-frame JPEG count/ns deltas;
- total count = denominator counts;
- total ns = denominator ns;
- completed frame deltas sum to measured-scroll aggregate.

Do not reinterpret `jpegNativeDecodeNs` as pure libjpeg/IDCT time. It includes
successful loader allocation/storage/backing work from the Plan 1 start point.

## Progress

- [x] Bootstrap Plan 2 state/evidence and verify Plan 1 preconditions.
- [x] Milestone 1: build/package exact final macOS revision and run final smoke.
- [x] Milestone 2: execute full benchmark, validate, summarize, commit closeout.

## Milestone 1 — exact final macOS build and package

### Resolve corpus deterministically

Use `$TC_IMAGE_CORPUS` if set and valid.

Otherwise try `~/Downloads/win32/win32`, which the completed distributed-decode
state previously documented as the local corpus root. Accept it only if it
contains the six expected variants used by the packager.

If neither is valid, stop with an explicit corpus-path error. Do not search the
home directory broadly.

### Build only SDK and macOS

Use a task-specific build/output directory and save verbose output to logs
outside tracked source.

Build the SDK package:

    (cd scripts && ./package-sdk.sh)

Build macOS ARM64 only:

    cmake -S TotalCrossVM -B build/image-scroll-diagnostics-macos \
      -DCMAKE_BUILD_TYPE=Release \
      -DCMAKE_OSX_ARCHITECTURES=arm64 \
      -G Ninja

    cmake --build build/image-scroll-diagnostics-macos \
      --target tcvm Launcher --parallel

Do not build any other target/platform.

Assemble the package tree in the same locations used by `package.yml`:

    mkdir -p build/TotalCross/dist/vm/macos
    mkdir -p build/TotalCross/etc/launchers/macos

    cp build/image-scroll-diagnostics-macos/libtcvm.dylib \
       build/TotalCross/dist/vm/macos/

    cp build/image-scroll-diagnostics-macos/Launcher \
       build/TotalCross/etc/launchers/macos/

Determine the version from the versioned SDK JAR under
`build/TotalCross/dist/`. Create a fresh task-specific SDK ZIP under a path such
as:

    /tmp/image-scroll-diagnostics-macos-<short-head>/TotalCross-<version>.zip

The ZIP root must be `TotalCross/`.

Record SHA-256 for:

- packaged SDK ZIP;
- `totalcross-sdk.jar`;
- `libtcvm.dylib`;
- `Launcher`.

### Package the benchmark bundle

Use only the macOS target:

    scripts/package-image-scroll-benchmark.sh \
      --sdk-zip <fresh-sdk-zip> \
      --corpus <resolved-corpus-root> \
      --output <task-output-dir> \
      --target macos-arm64

Require the generated directory:

    <task-output-dir>/image-scroll-benchmark-macos-arm64

Run one final self-test/smoke gate before the full matrix. The existing full
runner already performs smokes, but this separate gate proves the package before
the long run.

Run:

- benchmark self-test;
- existing four scroll smokes (`0/off`, `0/on`, `32799/off`, `32799/on`);
- `mask=4/off` and `mask=4/on` if not covered by the smoke phase;
- decode self-test only; do not run the 90-process decode matrix yet.

Validate from smoke output:

- requested mask = effective mask;
- diagnostic schema fields exist;
- writePixels/JPEG invariants pass;
- mask 4 has writePixels attempts;
- prefetch and scroll JPEG sections are separated;
- frame JPEG sums match scroll totals.

If this gate fails because of a diagnostic correctness bug, return to a focused
implementation fix, create a logical commit, rebuild SDK/macOS, and rerun this
gate. Do not begin the full matrix with a known schema/runtime failure.

Record the exact final HEAD and package hashes in evidence/state.

## Milestone 2 — full benchmark and closeout

### Run the existing complete suite

From the packaged macOS bundle run:

    python3 <bundle>/run-benchmark.py \
      --bundle <bundle> \
      --phase full

This must keep the existing suite composition:

1. scroll self-test and four normal smokes;
2. 126-process scroll matrix;
3. scroll aggregation;
4. existing 90-process standalone decode matrix;
5. decode aggregation;
6. one combined results ZIP.

Do not add quarter/eighth scenarios to the standalone decode matrix. The
real-scroll JPEG tier distribution is now measured by the Plan 1 instrumentation.

Do not continuously poll the long-running command. Use one blocking invocation
with a suitable timeout. On success print only a concise result summary and
paths.

### Validate final result structure

Require:

- scroll `summary.csv`: exactly 126 matrix rows and all PASS;
- no missing/duplicate `(mask,prefetch,run)` keys;
- decode plan/process count remains 90;
- decode detailed rows remain 90 x 663 and no failed image rows;
- final combined ZIP exists;
- every run carrying new diagnostics satisfies the Plan 1 invariants.

For diagnostic analysis, inspect at least these scroll cells across all three
runs:

- mask 0, prefetch off/on;
- mask 4, prefetch off/on;
- mask 32799, prefetch off/on.

Do not use mean-only conclusions when runs are noisy. Report each run and the
median when summarizing P50/P95 and diagnostic totals.

### Create compact committed summary

Write:

- `.agent/benchmarks/image-scroll-diagnostics-macos/summary.md`

Keep it under 20 KiB / approximately 600 lines. Include only:

- final commit SHA;
- packaged SDK/native hashes;
- corpus file count/hash;
- absolute raw results directory;
- absolute final ZIP path and ZIP SHA-256;
- scroll/decode PASS counts;
- for mask 4 off/on:
  - attempts/hits/fallbacks;
  - every writePixels rejection reason;
  - device-one-to-one candidate counts;
  - feature status;
- for mask 0 and mask 4 off/on:
  - JPEG total count/ns;
  - full/half/quarter/eighth/other count/ns;
  - JPEG ns divided by total measured frame ns;
  - P50/P95 frame times;
- factual statement whether half is actually dominant on macOS;
- factual statement whether writePixels is primarily rejected by save-count,
  matrix, size mismatch, or another measured reason.

Do not make or implement an optimization recommendation in this plan. The
summary is evidence for the next policy decision.

Do not copy raw CSV/JSON content into the tracked summary.

### Closeout artifacts

Update:

- Plan 2 state;
- Plan 2 evidence;
- Plan 2 editorial report;
- Plan 2 Progress / Outcomes if this plan is stored as a living tracked plan.

The editorial report must contain the factual sections required by `PLANS.md`
and distinguish measurement from inference.

Commit all compact tracked closeout artifacts.

Suggested final commit:

    docs(benchmark): record macos diagnostic benchmark

## Failure and Retry Policy

The runner is fail-fast.

If the full benchmark fails:

1. preserve the partial output directory and logs;
2. record failing phase/process, exit status, and absolute paths;
3. retry once only if the failure is clearly environmental/transient and not a
   schema/runtime/diagnostic invariant failure;
4. use a new task output directory for the retry;
5. otherwise stop, commit compact state/evidence, and report the partial-results
   path.

Do not change optimization policy to make the benchmark pass.

## Validation and Acceptance

Before final closeout commit:

    python3 scripts/validate-copyright-headers.sh --files <changed tracked files>
    git diff --check --cached

For every new tracked file:

    wc -c <file>
    wc -l <file>

Require <= 20480 bytes and approximately <= 600 lines.

Plan 2 completes only when:

- final exact HEAD is known;
- SDK and macOS-only build/package gate passes;
- full scroll matrix completes with 126 PASS rows;
- existing decode matrix completes with 90 successful processes and zero failed
  image rows;
- new writePixels and JPEG diagnostics are internally consistent;
- compact summary/evidence/report/state are committed;
- no other platform was built;
- final response reports absolute paths for:
  - raw results directory;
  - combined results ZIP;
  - committed compact summary;
- final response reports final HEAD and logical commits created.

## Idempotence and Recovery

Use task-specific `/tmp` output directories. Never delete prior benchmark
results, unrelated caches, or local changes.

Do not amend commits. If final validation exposes a diagnostic defect, create a
focused follow-up commit and rebuild only SDK/macOS.

State is the first resume read. Evidence is append-only and compact.

## Logical Commits

Follow `.agents/skills/logical-commits/SKILL.md`.

Expected Plan 2 commits:

- bootstrap/state artifact commit if Plan 2 files are newly introduced;
- focused diagnostic fix commits only if final smoke exposes a real defect;
- `docs(benchmark): record macos diagnostic benchmark` for successful closeout.

Every non-trivial body must state motivation, behavior, platform/compatibility
impact, validation, and important deferrals. Do not push.

## Outcomes & Retrospective

Plan 2 completed successfully on benchmark revision `a43535e48`.

- SDK and macOS ARM64 `tcvm`/`Launcher` build/package passed; hashes and
  absolute output paths are recorded in state, evidence, and summary.
- The scroll suite produced 126/126 PASS rows with unique matrix keys. The
  existing decode suite produced 90 processes, 59,670 detailed image rows,
  and zero failed image rows.
- Every run passed attempts/hits/fallback, candidate, JPEG bucket, and frame
  delta invariants. Mask 4 remained `ATTEMPTED_NO_HIT`; matrix and save-count
  rejections were present in every mask-4 run, with overlapping reasons as
  defined by Plan 1.
- Half-resolution decoding dominated observed JPEG work: scroll-off runs were
  almost entirely half, and prefetch-on runs were 655 half plus 5 full; no
  quarter, eighth, or other bucket was observed in the selected cells.
- The only deviation was an initial self-test invocation missing `--bundle`;
  the prescribed invocation passed. The full benchmark required no retry.
- No optimization policy or other platform was changed.

## Revision Note

Initial Plan 2: consume the completed diagnostic instrumentation from Plan 1,
run the exact full macOS distributed benchmark, and preserve compact evidence.
