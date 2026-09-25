<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# PNG Prefetch Rollout: Editorial Handoff

## Editorial Summary

Static PNG images now participate in the SDK's asynchronous image prefetch path.
On the exact 663-image customer workload, the three requests that were
previously not prefetchable became READY. The prior cold-scroll counters for
full JPEG decode, image materialization, and native geometry materialization
changed from 3 each to zero. Frame-stall counts moved in both directions, so
this work establishes PNG readiness and counter behavior, not a frame-pacing
improvement.

The Windows x64 benchmark package was assembled and inspected with the trusted
Windows runtime. It was not built or executed on Windows.

## Original Plan versus Actual Outcome

The plan's SDK implementation, diagnostic runner, and deployed macOS smoke
gates completed in Part 1. Part 2 ran one fresh process for each of the six
legacy, polling, and semaphore configurations at masks 6 and 38, then created a
Windows x64 package from the current SDK JAR and an unchanged, trusted Windows
runtime. All six macOS rows passed their readiness, lifecycle, and scheduling
checks. The Windows archive passed its manifest, contents, and runner checks.

The work stayed in the existing `feat/png-prefetch` worktree. There are no
`TotalCrossVM` source changes. The Windows deliverable is a generated ZIP kept
outside Git.

## What Changed

- SDK image preparation recognizes PNG payloads from encoded content and
  schedules single-frame PNG images through the Java-result candidate path.
  JPEG behavior and the production `legacy` scheduling default remain intact.
- Benchmark diagnostics record readiness results, preparation/decode/UI
  lifecycle counts, worker scheduling, semaphore notifications, and frame
  metrics for each strategy.
- The Windows package includes the current benchmark executable and TCZ, current
  runners, the 663-record corpus, and the trusted `tcvm.dll`.

## Decisions and Trade-offs

- Keep PNG support additive and leave native VM sources unchanged.
- Compare `legacy`, 1 ms worker polling, and worker semaphore scheduling without
  changing the production default.
- Use the exact customer corpus and keep the performance comparison
  descriptive. Each configuration has one run, so no pass threshold or causal
  performance claim is appropriate.
- Reuse the trusted Windows runtime because this change is SDK-side. Include the
  corresponding trusted Windows launcher in the temporary SDK package input so
  the deployer can create the Windows benchmark executable.

## Unexpected Problems and Discoveries

- The SDK package made on macOS did not contain `win32/Launcher.exe`. The first
  Windows package attempt stopped at deployment. Adding the launcher from the
  previously staged trusted Windows SDK overlay allowed packaging to complete.
- The corpus directory also carries four explicit PNG interface assets. They
  remain bundled but are outside the manifest's 663 benchmark-record count,
  which covers `.jpg`/`.jpeg`-named corpus entries. The content counts for those
  records are 660 JPEG and 3 PNG.
- Worker-semaphore notification counts rose from 641 to 644 per mask, matching
  the three newly supported entries; release, acquire, and work-wake counts
  remained equal with no outstanding wake.

## Validation and Measurable Results

- Part 1: 26 focused `ImagePreparationTest` tests passed; SDK distribution and
  Release native `tcvm`/`Launcher` builds passed; ordinary and indexed deployed
  macOS PNG smokes passed.
- Part 2 macOS: package manifest and ZIP passed; the six-process
  `prefetch-thread-diagnostics` phase passed. Each row reported 663 requests,
  663 READY, 0 failed, and 0 not-prefetchable. Each row reconciled 645
  preparation, decode, and UI-dispatch entries. Legacy created/started 645
  threads; poll and semaphore modes each used one persistent worker. Polling
  rows recorded 10,816 and 8,836 sleeps. Semaphore rows recorded 644 releases,
  acquires, and wakes with zero outstanding wakes.
- Cold-scroll counters: the previous run reported 3 full JPEG decodes, 3 image
  materializations, and 3 native geometry materializations. All are zero in
  each current row. Targeted JPEG decodes remained zero.
- Frame stalls: across the six paired rows, counts above 33.3 ms totalled 7
  before and 6 after; counts above 50 ms totalled 4 before and 5 after. Per-row
  values vary in both directions.
- Part 2 Windows: the benchmark packaging contract tests passed; the ZIP's
  manifest, SDK/runtime attestations, dataset hash/counts, root bundle files,
  PowerShell runner hash, and archive integrity passed static checks. The
  runner contains no invocation of Python, Java, Git, curl, or web request
  commands.

## Useful Evidence and Examples

- Compact six-row macOS data, including timings, lifecycle, frame percentiles,
  and cold-scroll counters: `.agent/evidence/png-prefetch-macos.csv`.
- Evidence index and exact paired baseline table: `.agent/evidence/png-prefetch.md`.
- Executable plan and resume state: `.agent/plans/png-prefetch-part-2.md` and
  `.agent/state/png-prefetch.md`.
- Windows package:
  `/tmp/png-prefetch-part2.Kd03lw/windows-package/image-scroll-benchmark-windows-x64.zip`.
  SHA-256: `fb04343d5c6ea17bfa520ed80771cdd4e52e1871e515cdf26003d0745713c6dd`.
- Runtime provenance: workflow run `36053759681`, source commit
  `ca7d77d88880ac5c6c666bd2b67721c2bead7063`, `tcvm.dll` SHA-256
  `5ebfa185fbf3031258c5463d6da488cce149be7adc799ae9b41bb052834e51bc`.

## Limitations, Remaining Work, and Open Questions

- No Windows build, PowerShell execution, or Windows benchmark measurements
  were performed. Run the packaged
  `run-prefetch-thread-benchmark-windows.ps1` on Windows when a Windows host is
  available.
- The PowerShell runner was statically inspected on macOS; it was not parsed or
  executed by PowerShell because that runtime is unavailable here.
- Each macOS strategy/mask combination ran once. The measurements do not
  resolve run-to-run frame variance or establish whether PNG readiness caused
  any frame change.
- The Windows ZIP is an external generated artifact under `/tmp`; only its
  hash, contents summary, and provenance are committed.

## Possible Article Angles

- How format support can extend an asynchronous preparation path without
  changing the mature JPEG pipeline.
- Why readiness counters and lifecycle reconciliation answer a different
  question from frame-pacing benchmarks.
- How to package cross-platform benchmark software using a trusted native
  runtime when the current SDK build host cannot produce that runtime.

## Suggested Narrative

Start with three real image requests that could not enter prefetch. Explain the
small SDK-side format addition, then show that all 663 workload requests became
READY and the previous cold-scroll counter signature disappeared. Present the
frame data alongside that result: outlier totals remain mixed across single
runs. Close with the Windows package and its trusted runtime provenance, while
making clear that Windows execution and measurements are still pending.

## Claims Requiring Human Review

- “The previous three-image cold-scroll counter signature disappeared” is
  supported by the recorded counters; “PNG support caused frame stalls to
  disappear” is not supported and must not be claimed.
- Avoid describing the frame comparison as a performance win. The `>50 ms`
  aggregate count increased from 4 to 5 and individual rows move both ways.
- Describe Windows as packaged and statically validated, not built, run, or
  benchmarked.
