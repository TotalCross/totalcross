<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Performance tooling audit

## Principles

Reviewed the requested refs: `perf/image-opt-phase1-controls`,
`perf/image-opt-phase2-raster`, `perf/image-opt-phase3-formats`,
`perf/image-jpeg-factories-lazy`, `perf/image-scroll-raster-fast-path`,
`perf/image-scroll-distributed-benchmark`,
`perf/image-decode-distributed-benchmark`, `perf/scroll-raster-reuse-poc`,
`perf/prefetch-thread-diagnostics`, `feat/semaphore-v1`, `feat/png-prefetch`,
`feat/frame-pacing-scheduling-diagnostics`, and
`codex/scroll-raster-reuse-windows-package`.

Durable tooling needs an explicit owner, repeatable inputs, a checked output
contract, and useful measurements on supported hosts. It must identify the
runtime and environment it actually exercised, avoid hidden local paths, and
select behavior through named options rather than historical bit masks. Keep
small deterministic fixtures with the runtime behavior they protect. Keep
customer corpora external unless both distribution rights and a maintained
source are established; record a stable manifest and content hash when using
one. Preserve conclusions in feature reports, not raw historical runs.

## Inventory

Counts below are grouped inventory rows, not individual files.

| Tool/fixture/artifact | Classification | Destination | Rationale |
| --- | --- | --- | --- |
| Deterministic image behavior tests and smoke apps for decode, geometry, deferred operations, JPEG factories, modifiers, and fallback/parity | KEEP_RUNTIME_TEST | Owning image feature PR: lazy JPEG, decode, raster, or modifier behavior | Assertions of public output, failure recovery, and lifecycle are durable. Retain only the minimal inputs needed by those assertions. |
| Synthetic compact-format and raster-variant inputs/goldens | KEEP_RUNTIME_TEST | Compact formats/raster feature PR | Useful for RGB565, gray, alpha, color conversion, and physical-variant parity. Keep this as a small synthetic fixture, separate from the 663-image scroll workload. |
| Semaphore smoke and stress correctness cases | KEEP_RUNTIME_TEST | `feat/semaphore-v1` reconstruction PR | Repeated wait/wake and lifecycle checks protect supported concurrency behavior. Latency sampling is not part of this classification. |
| `FlickDriverTest` deterministic driver and clock semantics | KEEP_RUNTIME_TEST | Frame-pacing/Flick feature PR | State, callback, and millisecond-versus-nanosecond semantics are regression contracts; timing a clock selector is not. |
| Phase 1/2/3, JPEG-factory, raster, decode, and scroll benchmark applications plus `ImageRasterBenchmarkSupport` | MOVE_TO_FEATURE_PR | The specific image feature PR that owns each measured behavior | They exercise named workloads but many read `*ForTest` counters, temporary profiles, or internal optimization settings. Keep only the focused fixture/app needed by its owning feature and update its assertions there. |
| `ImageScrollRealWorkloadBenchmarkApp` and its 663-image, three-column, 221-row scroll layout | MOVE_TO_FEATURE_PR | Scroll raster/prefetch feature PRs | This reproduces a meaningful long list and cold/warm scrolling path. Keep the harness, not the customer files or its temporary profile switches. |
| Flick timer 40/60 fps, UpdateListener/Flick, and synthetic 16 ms/16.667 ms workloads | MOVE_TO_FEATURE_PR | Frame-pacing scheduling PR | These are useful workload shapes independent of the old diagnostic matrix. Retain a focused workload for each supported driver and synthetic deadline case. |
| `.github/workflows/image-scroll-raster-validation.yml` | MOVE_TO_FEATURE_PR | Scroll/raster feature PR | The Linux/Windows/macOS validation matrix has cross-platform value, but its push trigger names the historical branch and it builds feature-specific fixtures. Recreate the needed lanes with current PR triggers. |
| `scripts/package-image-scroll-benchmark.sh` foreign-target packaging | MOVE_TO_FEATURE_PR | Feature PR that needs packaged cross-platform smoke validation | `tc.Deploy` can create Windows, macOS, and Linux bundles from a Linux/macOS host. The current path compiles against an SDK ZIP, so a result is relevant only when the packaged runtime and SDK are attested to the exact source under test. Do not check in its bundles. |
| `scripts/run-image-optimization-benchmark.py` macOS process/RSS sampler | REBUILD_GENERIC_TOOL | Shared benchmark tooling only if a feature needs sampled host RSS | It accepts scenario/sample counts, but relies on macOS `ps`/`sysctl`, has no process timeout, and only checks exit status/sample count. Rebuild with portable host metadata and a validated output schema. |
| Scroll matrix runner, embedded summarizer, and `test-image-scroll-distributed-benchmark.py` | REBUILD_GENERIC_TOOL | Scroll/prefetch owning PR after named runtime options exist | Earlier matrices ran 21 masks × 2 prefetch states × 3 rounds (126 fresh processes). Later code grew into a multi-profile runner with fixed masks, profile-specific pass counts, and feature-specific aggregation. Keep fresh-process orchestration, timeout/failure handling, and checked summaries; remove mask tables and split unrelated profiles. |
| Decode matrix runner and `aggregate-image-decode-benchmark.py` | REBUILD_GENERIC_TOOL | Decode/format owning PR | The 90-process matrix (6 corpus variants × 5 source/order/size scenarios × 3 rounds) has useful filesystem-versus-TCZ coverage and per-image nanosecond records. The parser is rigorous but fixes the 663-row schema, variants, and matrix in code; make inputs/schema explicit before retaining it. |
| `count-image-corpus-formats.py` and `package-image-decode-corpus.py` | REBUILD_GENERIC_TOOL | External-corpus tooling owned by decode/format PR | Content sniffing and matching relative paths across variants are useful. Current scripts encode one six-folder corpus, 663 files, and historical spelling; use a manifest-driven validator/stager if the corpus is licensed and available. |
| `frame_pacing_contract.py`, `frame_pacing_results.py`, Python runner, and contract tests | REBUILD_GENERIC_TOOL | Frame-pacing PR after named scheduling configuration/diagnostics exist | Stage and summary validation is valuable, but the 48-process matrix fixes temporary environment variables, exact flags, one bundle shape, and one image corpus. Keep schema validation and process isolation; rebuild the matrix around supported named profiles. |
| PowerShell process, timeout, `ExitCode`, stdout/stderr, `DebugConsole.txt`, and manifest/hash techniques | REBUILD_GENERIC_TOOL | Shared Windows validation tooling only if Windows-host coverage requires it | The techniques are durable. The current scripts parse app-specific records, hard-code corpus/profile assumptions, and duplicate Python logic; carry the behavior into one maintained Windows path rather than copying the scripts. |
| `BuildImageDecodeLibraryTcz.java` | DROP_ONE_OFF | None; reproduce only inside a decode feature PR if library-only TCZ input remains necessary | It creates and round-trip checks six corpus-specific 663-entry artifacts. Normal feature fixtures or a generic resource packager should own any lasting TCZ case. |
| Six-process `prefetch-thread-diagnostics` profile and its Windows wrapper | DROP_ONE_OFF | None; any later worker regression belongs to `feat/png-prefetch` or `feat/semaphore-v1` | It fixes masks 6/38, three thread modes, prefetch/accounting on, one process per combination, one measured pass, and the shared 663-image corpus. It is a one-question comparison, not a reusable concurrency/load benchmark. |
| Windows scroll-raster-reuse PowerShell runner, helper/analysis scripts, test, and README | DROP_ONE_OFF | None; correctness smoke belongs in the scroll-raster-reuse feature PR | The 2 preflights plus OFF×3/ON×3 run order, reuse counters, fixed profile, 540×960 logical window, and resume-from-failure archive are investigation-specific. The captured display/renderer/refresh data cannot make RDP timings comparable. |
| Semaphore Windows validation package scripts and fixed artifact/run provenance | DROP_ONE_OFF | None; keep only a feature-owned Windows correctness test if required | The default path pins workflow IDs, commits, and hashes; diagnostic mode depends on transient workflow artifacts. It is not a general package generator. |
| Clock-selector microbenchmark and temporary deadline/yield diagnostic probes | DROP_ONE_OFF | None; preserve semantic checks in the Flick feature PR | A selector speed result does not represent end-to-end frame pacing. Test clock precision and driver behavior directly; do not retain temporary internal-mode probes as permanent APIs. |
| Early `run-image-scroll-real-workload-benchmark.py` and mask-oriented `ImageOptimizationMaskSmokeApp` | DROP_OBSOLETE | None | The early macOS-only runner and raw-mask smoke app are superseded by later workload/contract coverage and conflict with the named-configuration requirement. |
| `.agent/plans`, `.agent/state`, `.agent/archive`, and investigation-only prose for these branches | DROP_ONE_OFF | Extract only decision/evidence summaries into the owning feature report | Execution plans, handoff notes, and accumulated state describe completed investigations and are not permanent benchmark tooling. |
| `.agent/benchmarks/**`, measured `.agent/evidence/**`, `artifacts/**`, `results/**`, logs, CSV/JSON run manifests, ZIP/TAR bundles, deployed apps, TCZs, and package hashes tied to one run | DROP_GENERATED | Remove from reconstructed PRs; summarize relevant evidence in feature reports | These are execution outputs or provenance for a single build/run, not stable fixtures. Do not retain raw historical performance results. |

## Permanent workload fixtures

The 663-image scroll workload should survive as an external, feature-owned
fixture if its owner can continue to provide it. Its useful dimensions are the
large three-column list, long top-to-bottom and bottom-to-top traversal, mixed
image sizes/color content, progressive-JPEG coverage, and JPEG-named PNG
payloads. A read-only header audit of the available `imag` variant found 660
JPEG payloads, 3 PNG payloads, 17 JPEG dimensions, and 556 progressive JPEGs.
The `lossless` variant has 553 progressive JPEGs; `decode-baseline` and
`decode-fast` have none, and the two aggressive variants collapse to one JPEG
dimension each. Keep those transformed inputs with decode comparisons rather
than treating them as the real-scroll corpus.
The app enforces 663 JPEG-named files, 221 rows, three controls per row, and
full scroll extent; corpus helpers check file identity/hash and PNG/JPEG magic.
They do not enforce the resolution, unique-color-count, or progressive-JPEG
distribution. Put those properties in a versioned external corpus manifest if
the exact corpus is approved for continued use.

The corpus is supplied through an external path/environment variable and is
not a tracked fixture in these branches. No distribution license or durable
source/provenance is recorded in the repository, so it cannot safely or
practically be moved into the tree on this evidence. Its relative paths and
content hash make a supplied copy identifiable, but do not establish rights or
guarantee availability. Unique-color-count coverage and rights/source approval
remain unresolved. Keep decode comparisons as a separate workload: its
six encoded variants and filesystem/TCZ, full/half, sequential/random cases
answer a decode question, not a scroll-performance question.

Keep compact-format and raster-variant tests synthetic and small. They should
cover storage format, color conversion, parity, and fallback without forcing
those cases into the customer-derived scroll corpus.

## Reusable runners/parsers

No historical runner qualifies as `KEEP_REUSABLE_TOOL` unchanged. The best
surviving concepts are: fresh-process execution with a timeout; a declared
matrix and deterministic order; strict record/summary validation; a single
parser shared by hosts; corpus/runtime identity; and environment metadata.
Current optimization and scroll runners pin integer masks (including 6, 38,
32799 and larger values), profile names, output fields, process counts, and
pass layouts. The decode runner is more cleanly separated, but still embeds a
single corpus schema and matrix. Rebuild only the axes needed by an owning
feature, and keep the scroll and decode workloads separate.

## Windows tooling

The PowerShell runners do collect a real child `ExitCode`, enforce timeouts,
capture stdout/stderr and `DebugConsole.txt`, and validate bundle/runtime and
dataset hashes. Those are worthwhile requirements for a future Windows host
runner. Python-free execution alone does not justify maintaining a duplicate
hard-coded parser when the Python runner can be made portable. Retain a
PowerShell implementation only if supported Windows environments cannot
provide the shared runner runtime.

The scroll-reuse package verifies 663 inputs and a runtime hash, but its app
arguments, profiles, and parser bind it to one experiment. Its manifest/hash
pattern is useful; per-run manifests and archives are generated evidence and
must not be checked in. The foreign-target packager creates a Windows bundle
from macOS/Linux, but its SDK-ZIP input can differ from the reconstructed
runtime. Prefer the fresh-runtime cross-platform workflow; otherwise require
source commit and binary hashes that prove which VM was tested. RDP/remote
desktop, refresh rate, scaling, and renderer can materially affect frame
timings. Record them, but do not compare those measurements across unlike
sessions or treat an RDP run as a stable regression gate.

## Distributed benchmark tooling

The 6-process prefetch-thread profile is not generic concurrency/load tooling:
it is the Cartesian product of two historical masks and three fixed thread
profiles, uses the same external corpus, enables prefetch and accounting, and
has one process per cell with no repeated statistical sample. Other scroll
profiles use fresh processes and a fixed mask/prefetch/accounting matrix; the
application measures explicit cold/warm passes, while file and OS caches are
not reset between processes. Corpus reuse and prefetch state must therefore be
reported; a fresh process is not proof of a cold storage cache.

The decode suite uses 90 fresh processes and 663 per-image rows each. It
reuses one corpus across variants, distinguishes filesystem from TCZ loading,
and mixes sequential and seeded random order; it does not reset the host file
cache. Preserve these axes only if they still reproduce supported workloads.
Neither distributed runner should require users to know an optimization mask;
future execution must select named runtime configuration and report its
effective settings.

## Frame-pacing tooling

Keep the workload shapes that stand independently: Flick timer at 40 and 60
fps, UpdateListener/Flick modes, synthetic 16 ms versus 16.667 ms pacing, and
tests of millisecond/nanosecond clock semantics. These belong with the Flick
and scheduling feature PR. The five-stage historical runner performs three
rounds per configuration (48 measured processes plus preflight), tests
relative/absolute deadlines and poll/wait/yield modes, and validates callback,
work, paint, sleep-overshoot, and deadline-error summaries. That coverage is
useful as a future test plan, but its raw environment flags and diagnostics
schema must be replaced by named runtime options and supported diagnostic
snapshots. Do not preserve a standalone clock-selector timing benchmark.

## Generated artifacts to remove

Never preserve run-specific contents under `.agent/benchmarks/`, generated
`.agent/evidence/` tables/JSONL/logs, `artifacts/`, `results/`, temporary
`corpus/` copies, deployed executables/libraries, benchmark ZIP/TAR files,
`DebugConsole.txt`, per-run manifests, suite plans, CSV/JSON summaries, and
hash/provenance files for one package. Keep small checked-in synthetic inputs
only when a runtime test consumes them. Future feature reports should state
the useful historical result and conditions without committing raw output.

## Standalone tooling PR decision

DO NOT CREATE STANDALONE TOOLING PR

There is no coherent independent set of reusable code today. The shared
process, parser, corpus, packaging, and environment ideas depend on future
feature-owned workloads and runtime configuration/diagnostics. Rebuilding
them before those contracts exist would create another generic layer around
temporary APIs. Implement focused tooling with the owning scroll, decode,
prefetch/semaphore, or frame-pacing feature PR when its workload is ready.

## Integration with runtime configuration/diagnostics

- Replace raw `--image-optimization=<integer>` values and mask lists such as
  6, 38, and 32799 with named runtime options. Record requested and effective
  names, not just their former integer values.
- Replace temporary app profiles and environment switches for timer deadline,
  event-loop, yield, SDL format, prefetch thread mode, and sleep duration with
  supported named settings. Record effective settings in each run's metadata.
- Replace direct dependencies on `*ForTest` counters and fixed CSV/JSON keys
  with supported diagnostic snapshots/metrics for decode counts, materialized
  images, raster attempts/hits/fallbacks, bytes, frame intervals, active work,
  callback lateness, and sleep/deadline error.
- Record source commit, SDK/runtime binary hashes, OS/architecture, Java/tool
  versions, build configuration, logical and drawable dimensions, scale,
  renderer, refresh rate, corpus manifest/hash, prefetch state, and cold/warm
  protocol explicitly. A bundle hash alone does not describe the run.

## Reconstruction guidance

| Owning feature PR | Keep or rebuild | Explicit instruction |
| --- | --- | --- |
| Image optimization phases 1–3 and lazy JPEG factories | Keep focused semantic/synthetic regressions; move only a useful benchmark app; rebuild its runner if needed | Do not carry the phase scripts, raw masks, or full matrix by default. |
| Image scroll raster fast path, scroll reuse, and warm copyRect | Keep the three-column scroll harness; rebuild a feature-owned runner/workflow only for supported cross-platform validation | Keep the 663-image corpus external and manifest-identified; separate synthetic raster-format coverage. |
| Image decode/format | Keep decoder parity fixtures; rebuild the corpus adapter and decode runner only if the six-variant workload remains supported | Do not merge decode matrices into scroll matrices or retain generated TCZ packages. |
| PNG prefetch and semaphore | Keep correctness/stress fixtures; drop the six-cell worker comparison and pinned Windows artifact validator | Any later worker benchmark must use named thread configuration and repeated samples. |
| Frame pacing/Flick scheduling | Keep deterministic driver tests and focused 40/60, UpdateListener, and synthetic workloads; rebuild one runner for supported hosts | Use named scheduling configuration and current diagnostic snapshots; discard the duplicate fixed-matrix scripts. |

## Validation

- Inspected the requested branch refs, benchmark app/script inventories, the
  image-scroll validation workflow, package and corpus helpers, Windows
  runners, frame-pacing contracts, and grouped generated evidence/result
  directories.
- Every surviving fixture/tool has an owning feature destination in the
  inventory and reconstruction guidance. No raw run output is recommended for
  preservation, and no proposed runner retains raw historical masks.
- No builds, benchmarks, or test suites were run, as this is an analysis-only
  audit.
