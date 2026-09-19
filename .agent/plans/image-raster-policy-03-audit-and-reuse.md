<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Plan 3 — audit raster optimization identity, eligibility, and reuse

This ExecPlan follows `AGENTS.md`, the ExecPlan rules in
`TotalCross/totalcross-depot-tools:.agent/PLANS.md`, and
`.agents/skills/logical-commits/SKILL.md`.

Execute only on branch `perf/image-decode-distributed-benchmark`.

At authoring time the branch contains completed Plans 1 and 2 at
`57d0532409c2163ac390d2f0affd8a0787049b07`.

Do not require that exact revision if the branch advances legitimately. Require
instead that this revision remains an ancestor of the execution HEAD.

## Purpose / Big Picture

Close the correctness and measurement issues discovered after the
device-space `RASTER_OPAQUE_WRITE_PIXELS` benchmark, then determine whether
three existing raster optimizations are being prevented from working by
overly specific cache identity or overly conservative canvas eligibility:

- `RASTER_TARGET_COLORTYPE_CONVERSION`, bit 13;
- `RASTER_PHYSICAL_VARIANT_CACHE`, bit 14;
- `RASTER_PHYSICAL_IDENTITY_FOLDING`, bit 15.

The plan must first make the writePixels result trustworthy, then collect
evidence without changing the three policies, then apply only structurally
proven corrections, and finally benchmark reuse and the relationship between
`STORAGE_RGB565` and target-color conversion.

The four milestones are intentionally sequential. Each milestone ends with a
mandatory `STOP / REVIEW`. Do not begin the next milestone until the reviewer
explicitly approves continuation and the approval is recorded in the state
file.

Do not change default optimization masks in this plan.

Do not decide whether target-color variants should materialize immediately
instead of using delayed observation. Preserve the current delayed policy
through these four milestones. The final benchmark is intended to inform that
later decision.

## Working Set and Resume Protocol

Create and commit:

- `.agent/plans/image-raster-policy-03-audit-and-reuse.md`
- `.agent/state/image-raster-policy-03-audit-and-reuse.md`
- `.agent/evidence/image-raster-policy-03-audit-and-reuse.md`
- `.agent/reports/image-raster-policy-03-audit-and-reuse-editorial.md`

Create benchmark summaries as milestones complete:

- `.agent/benchmarks/image-raster-policy-03/m1-write-pixels-accounting.md`
- `.agent/benchmarks/image-raster-policy-03/m2-variant-audit.md`
- `.agent/benchmarks/image-raster-policy-03/m3-structural-validation.md`
- `.agent/benchmarks/image-raster-policy-03/m4-rgb565-target-color.md`

On resume, read the state file first.

Read these completed Plan 2 artifacts once during bootstrap:

- `.agent/state/image-write-pixels-policy-02-device-copy.md`
- `.agent/evidence/image-write-pixels-policy-02-device-copy.md`
- `.agent/benchmarks/image-write-pixels-device-policy/summary.md`

They define the previous writePixels implementation and benchmark baseline.
Do not repeatedly reread them after bootstrap.

Primary implementation paths are:

- `TotalCrossVM/src/nm/ui/skia/skia_image_backing.cpp`
- `TotalCrossVM/src/nm/ui/skia/skia_image_backing_internal.h`
- `TotalCrossVM/src/nm/ui/skia/skia_image_geometry.cpp`
- `TotalCrossVM/src/nm/ui/skia/skia_surface_test.cpp`
- `TotalCrossVM/src/nm/ui/skia/skia.cpp`
- `TotalCrossVM/third_party/jpeg/JpegLoader.c`
- `TotalCrossSDK/src/main/java/totalcross/ui/image/Image.java`
- `TotalCrossSDK/src/main/java/totalcross/ui/image/NativeImageBacking.java`
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageScrollRealWorkloadBenchmarkApp.java`
- `scripts/run-image-scroll-distributed-benchmark.py`

Read other files only when a concrete dependency requires them.

## Execution Constraints

Before editing:

    test "$(git branch --show-current)" = "perf/image-decode-distributed-benchmark"
    git merge-base --is-ancestor \
      57d0532409c2163ac390d2f0affd8a0787049b07 HEAD
    git rev-parse HEAD
    git status --short

Read `AGENTS.md`, the ExecPlan rules, and the logical-commits skill before the
first commit.

Preserve unrelated local and untracked files.

Stage only explicit task paths.

Do not push.

Do not amend, rebase, squash, or rewrite history.

Use frequent logical commits, but do not run native builds or benchmark
packages after every commit. Expensive validation belongs at milestone gates.

Before every commit:

1. validate changed-file copyright/SPDX headers;
2. stage only the intended paths;
3. run `git diff --check --cached`;
4. inspect `git diff --cached --stat`;
5. inspect the staged diff;
6. use an English Conventional-Commit message with all lines <=80 characters;
7. run the repository commit-message validation from
   `.agents/skills/logical-commits/SKILL.md`.

Every new tracked file must remain <=20 KiB and approximately <=600 lines.

Store verbose build and benchmark logs outside tracked source. Record only
compact evidence and stable artifact paths.

Do not run the historical 126-process full profile.

Do not run the standalone decode benchmark.

Use `work_time_ns` as the primary scroll performance metric.
`frame_time_ns` remains pacing/context only.

For comparable performance runs, require the same 663-file corpus used by the
previous benchmark. When the historical corpus is available, require self-test
hash:

    588a7e0f4019424a

Resolve the corpus in this order:

1. `$TC_IMAGE_CORPUS` when valid;
2. `~/Downloads/win32/win32` when valid;
3. otherwise stop with an explicit corpus-path error.

Do not scan the home directory looking for substitute images.

## Progress

- [ ] Bootstrap plan/state/evidence.
- [ ] Milestone 1: correctness and benchmark hygiene.
- [ ] STOP / REVIEW 1.
- [ ] Milestone 2: evidence-only raster identity and eligibility audit.
- [ ] STOP / REVIEW 2.
- [ ] Milestone 3: reviewer-approved structural corrections.
- [ ] STOP / REVIEW 3.
- [ ] Milestone 4: reuse and RGB565/target-color benchmark.
- [ ] STOP / REVIEW 4 and close the four-milestone execution.

## Current Architecture and Scope

Plan 2 enabled device-space `OPAQUE_WRITE_PIXELS`.

The focused benchmark showed:

- regular physical 1:1 candidates now convert to hits at high rates;
- warm median work improves materially with writePixels;
- cold results do not show the same benefit;
- warm P95 increased in several comparisons;
- JPEG-backed RGBA images were often `UNKNOWN` unless
  `RASTER_OPACITY_METADATA` was enabled;
- internal benchmark accounting executes inside the rendering path and may
  contaminate tail latency.

The current JPEG decoder already writes alpha 255 for every JPEG pixel.
JPEG has no alpha channel. A successfully decoded JPEG is therefore
intrinsically opaque; this fact must not depend on optimization bit 1.

The current target-color variant materializer converts an existing source
backing to a target `SkColorType`. The converted raster does not depend on the
screen position where it is later drawn.

However, its current cache key includes source and destination rectangles and
target surface dimensions.

The shared native variant cache also delays materialization:

    first matching key  -> observe and fall back
    second matching key -> materialize
    later matching key  -> cache hit

A changing destination position can therefore prevent target-color reuse even
when the same source is drawn repeatedly.

`RASTER_TARGET_COLORTYPE_CONVERSION`,
`RASTER_PHYSICAL_VARIANT_CACHE`, and
`RASTER_PHYSICAL_IDENTITY_FOLDING` also retain conservative canvas-state
checks, including `saveCount == 1` in relevant physical planning paths.

The existing Java materialized-image and draw-plan caches are keyed by source
identity/generation and destination scale, not by screen X/Y. They are not in
scope for redesign unless new evidence proves a defect.

## Fixed Architectural Decisions

### A. Intrinsic opacity

A successful JPEG decode must publish its native backing as
`SKIA_IMAGE_OPACITY_OPAQUE` regardless of
`RASTER_OPACITY_METADATA`.

This rule is based on the decoded format, never the file extension.

Preserve existing intrinsic opacity for compact formats such as RGB565 and
Gray8.

Do not add new PNG opacity assumptions in this plan.

Preserve `RASTER_OPACITY_METADATA` for cases where opacity is optional metadata
rather than an intrinsic JPEG property.

Keep compatibility accounting and add enough diagnostic distinction to report:

- opacity known intrinsically from format;
- opacity known from the existing metadata policy;
- opacity discovered by fallback pixel scan.

Do not merge these categories into one ambiguous benchmark count.

### B. Clipping proof

Strengthen the Plan 2 native partial-clip tests.

A clipped writePixels test passes only if it verifies both:

- all expected pixels inside the visible destination changed correctly;
- representative pixels outside every clipped edge remain unchanged.

A test that checks only an inside pixel is insufficient.

If the stronger test exposes a real implementation defect, fix the device-space
planner before continuing.

### C. Accounting-off benchmark mode

Add a benchmark-only accounting mode:

    --accounting=on
    --accounting=off

Default to `on` so historical diagnostic profiles keep their existing behavior.

Do not encode this mode into the image optimization mask.

When accounting is off:

- preserve external Java `System.nanoTime()` work/paint measurement;
- do not update Java image-operation counters;
- do not update native backing/raster/writePixels diagnostic counters touched
  by the benchmark render path;
- do not allocate diagnostic key-tracking structures;
- diagnostic JSON values that are unavailable must be explicitly null or
  marked unavailable;
- runner validation must not require disabled counters.

Do not remove historical counter APIs.

Gate every new diagnostic counter added by this plan and the writePixels
regular counters introduced by Plan 2.

Also gate existing native counters on the compared `32795/32799` render path
when they currently mutate despite accounting being disabled.

Do not perform a repository-wide unrelated counter refactor.

### D. Native target-format metrics

Make `Image.nativeMetricForBenchmarkTest()` report the actual active Skia
software target even when the global GPU `surface` object is null.

For software SDL, obtain the information from the active bitmap/raster target.

Expose at least:

- target width;
- target height;
- row bytes;
- alpha type;
- numeric Skia color type;
- numeric `kN32_SkColorType`;
- a stable target-color classification:
  `BGRA8888`, `RGB565`, or `OTHER`.

Populate the corresponding `environment.json` fields instead of writing null
when these metrics are available.

This is diagnostic only and must not change renderer initialization.

### E. Milestone 2 is evidence-only

Milestone 2 may add accounting and diagnostic structures.

It must not:

- change target-color cache identity;
- change physical-variant cache identity;
- remove `saveCount == 1`;
- alter clipping policy;
- alter variant materialization timing;
- split the shared raster-variant slot;
- enable any optimization by default.

The point of Milestone 2 is to prove which restrictions matter.

### F. Cache-identity diagnostics

When accounting is enabled, collect exact diagnostic identity information
without affecting accounting-off production behavior.

For target-color requests record:

- attempts;
- unique source backing/generation identities;
- unique current full keys;
- unique keys normalized without destination coordinates;
- unique intrinsic conversion keys containing only values that affect
  `makeTargetColorVariant`;
- pending-key replacements;
- materializations;
- hits;
- fallbacks.

The intrinsic target-color identity is:

    sourceGeneration
    sourceDecodeGeneration
    targetColorType

Do not include source/destination draw rectangles or target surface dimensions
in the normalized intrinsic diagnostic identity.

For physical variants record:

- lookups;
- unique full keys;
- unique keys normalized without target surface width/height;
- misses;
- materializations;
- hits;
- evictions.

These normalized forms are diagnostic in Milestone 2. They do not change cache
behavior yet.

### G. Eligibility diagnostics

Record rejection reasons separately for:

- target-color conversion;
- physical-variant cache;
- physical-identity folding.

At minimum distinguish:

- canvas/save state;
- target surface/pixels;
- device clip;
- unsupported mapping/matrix;
- partial/unsupported geometry;
- backing/opacity incompatibility;
- execution/materialization failure.

Record the actual `saveCount` when rejection is caused by canvas state, using
bounded aggregate counts rather than per-draw logs.

Do not print from the rendering hot path.

### H. Shared raster-variant diagnostics

The current backing has one `rasterVariant` slot and one pending variant key.

When accounting is enabled, record:

- `TARGET_COLOR -> PHYSICAL` replacement/eviction;
- `PHYSICAL -> TARGET_COLOR` replacement/eviction;
- pending target-color key replaced by physical;
- pending physical key replaced by target color.

Do not split the cache slot in these four milestones.

If significant cross-kind thrashing is observed, record it as a later
architectural decision.

### I. Milestone 3 change boundary

Milestone 3 begins only after human review of Milestone 2.

The reviewer must add an `Approved M3 changes` section to the state file.

The agent may implement only changes listed there.

Expected candidate corrections are:

1. canonicalize target-color cache identity to intrinsic conversion identity;
2. remove target surface width/height from the physical-variant key if review
   confirms they do not affect materialization;
3. replace blanket `saveCount == 1` rejection with a provably safe rectangular
   clip/canvas-state condition where the native Skia API and tests support it.

Do not change delayed versus immediate target-color materialization in
Milestone 3.

Do not split the single variant slot.

Do not proceed on an unapproved candidate merely because this plan lists it.

### J. Reuse workload

Milestone 4 adds a benchmark workload that reuses the same controls and
backings in one process:

    pass 1: forward  minimum -> maximum
    pass 2: backward maximum -> minimum
    pass 3: forward  minimum -> maximum

Do not rebuild the UI or reconstruct the image objects between passes.

Do not clear decoded backing, native variants, draw-plan caches, or raster
variant caches between passes.

Capture counters as deltas per pass when accounting is enabled.

External active-work timing remains available with accounting disabled.

## Plan of Work

### Bootstrap checkpoint

Create plan, state, and evidence files.

Verify the Plan 2 completion state and current branch ancestry.

Record the starting HEAD.

Run only static plan/header/whitespace validation.

Suggested commit:

    docs(benchmark): plan raster policy audit

No build and no benchmark at bootstrap.

### Milestone 1 — correctness and benchmark hygiene

Implement Contracts A through D.

First strengthen the clipping fixture. Do not use the stronger test merely as
documentation; make it fail if an outside pixel is modified.

Then make JPEG opacity intrinsic at successful native backing publication.

Preserve existing metadata counters for compatibility and add precise subtype
accounting.

Add `--accounting=on|off` through benchmark app and runner.

Ensure accounting-off avoids native diagnostic mutations in the compared
render path while preserving external active-work timing.

Populate real native target-format metrics in `environment.json`.

Suggested logical commits:

    test(skia): strengthen clipped write pixels proof

    fix(image): preserve intrinsic jpeg opacity

    perf(benchmark): isolate diagnostic accounting

    feat(benchmark): expose native target format

Combine commits only when the changes are inseparable by contract.

Milestone 1 build gate:

1. build the SDK smoke-test source;
2. build the SDK distribution if Java/native contracts changed;
3. configure and build only macOS ARM64 native targets needed for:
   - `tcvm`;
   - `Launcher`;
   - `skia_surface_test`;
4. run `skia_surface_test`;
5. package one macOS benchmark bundle;
6. run bundle self-test.

Require the strengthened clip fixture to prove outside pixels unchanged.

Require a JPEG decoded with bit 1 disabled to be intrinsically opaque without
a fallback opacity scan.

Require the environment artifact to report a non-null target-color
classification.

Add runner profile `write-pixels-accounting-ab`:

- masks: `32795`, `32799`;
- prefetch: `on` only;
- accounting modes: `on`, `off`;
- rounds: 5;
- expected processes: 20.

Interleave accounting modes and masks deterministically within each round.

Do not require diagnostic counters in accounting-off processes.

Also run one diagnostic `4/off` and one `4/on` smoke with accounting enabled.
For JPEG candidates, require intrinsic-known opacity to be present even though
bit 1 is absent.

Write:

- `.agent/benchmarks/image-raster-policy-03/m1-write-pixels-accounting.md`

Report separately for accounting on/off:

- both rounds/raw samples;
- work P50/P95/P99/MAX;
- paint P50/P95;
- `32795 -> 32799` delta;
- target color type;
- intrinsic/metadata/scan opacity counts when available.

Do not declare the writePixels P95 regression real or artificial solely from a
single process. Report the five-round distributions.

Commit milestone state/evidence/report updates.

Then:

    STOP / REVIEW 1

Do not begin Milestone 2 until approval is recorded.

### Milestone 2 — evidence-only raster identity and eligibility audit

Add Contracts F, G, and H.

Diagnostic collection must execute only when accounting is enabled.

Do not change any optimization decision.

Add a runner profile `raster-policy-audit` with one process for each mask:

- `8192` — target color only;
- `16384` — physical variant only;
- `32768` — physical identity only;
- `57344` — bits 13, 14, and 15 together.

Use:

- prefetch `on`;
- accounting `on`;
- one round;
- four processes total.

This is diagnostic execution, not a performance comparison.

Run the existing corpus and collect:

For target color:

- attempts;
- unique sources;
- unique full keys;
- unique no-destination keys;
- unique intrinsic keys;
- materializations;
- hits;
- pending replacements;
- rejection reasons.

For physical variant:

- lookups;
- unique full keys;
- unique no-surface-size keys;
- misses;
- stores/materializations;
- hits;
- evictions;
- rejection reasons.

For physical identity:

- attempts;
- hits;
- fallbacks;
- rejection reasons.

For shared-slot behavior:

- both cross-kind replacement directions;
- both pending-key replacement directions.

Calculate and report, without changing policy:

    target_full_keys / target_unique_sources

    target_full_keys / target_intrinsic_keys

    physical_full_keys / physical_no_surface_size_keys

    save_state_rejections / feature_attempts

Write:

- `.agent/benchmarks/image-raster-policy-03/m2-variant-audit.md`

The report must answer factually:

1. Does target-color key multiplicity exceed intrinsic key multiplicity because
   of destination position?
2. Do target surface dimensions fragment physical-variant identity?
3. How often does canvas/save state reject each physical feature?
4. Do target-color and physical variants evict or replace one another?
5. Are zero-hit results caused mainly by eligibility, key fragmentation,
   delayed observation, or another measured reason?

Do not fix any result yet.

Commit the instrumentation and audit result separately when appropriate.

Suggested commits:

    perf(benchmark): audit raster variant identity

    docs(benchmark): record raster policy audit

Then:

    STOP / REVIEW 2

This stop is mandatory.

The reviewer must write the exact approved Milestone 3 changes into the state
file.

### Milestone 3 — apply only reviewer-approved structural corrections

Read only the `Approved M3 changes` state section plus the active source paths.

Reject any implementation idea not explicitly approved.

When target-color key canonicalization is approved, make its actual cache key
match the intrinsic output of `makeTargetColorVariant`:

    sourceGeneration
    sourceDecodeGeneration
    targetColorType

Do not key it by destination X/Y, source/destination draw rectangles, or target
surface dimensions unless the review identifies a demonstrated dependency.

When physical surface-size normalization is approved, remove target surface
width/height only if the physical materializer output does not consume those
dimensions and native tests prove reuse across compatible surfaces.

When relaxing canvas-state eligibility is approved, do not simply delete the
guard.

Require the fast path to prove the effective clip is rectangular and the
device mapping is safe.

Use the strongest available Skia rectangular-clip query. If the repository's
Skia version cannot prove rectangular clip state, preserve the conservative
fallback and record the limitation.

Add native tests for every approved behavior.

At minimum, when corresponding changes are approved, cover:

- target-color reuse after drawing the same source at different screen
  positions;
- target-color reuse after a compatible destination-surface size change;
- physical-variant reuse across compatible surface sizes;
- saved rectangular partial clip;
- pixels outside the clip unchanged;
- translated positive-scale matrix;
- unsupported/non-rectangular clip fallback;
- unsupported rotation/skew fallback.

Preserve delayed materialization.

Preserve the single raster-variant slot.

Suggested commits depend on approved changes, for example:

    perf(skia): canonicalize target color variants

    perf(skia): reuse physical variants across surfaces

    perf(skia): accept proven rectangular canvas state

Do not create a commit for a candidate that review did not approve.

Milestone 3 build gate:

1. SDK focused build if bridge/schema changed;
2. macOS ARM64 `tcvm`;
3. `Launcher` only if required by changed native contracts;
4. `skia_surface_test`;
5. package one benchmark bundle;
6. bundle self-test.

Add profile `raster-structural-smoke`:

- masks: `0`, `8192`, `16384`, `32768`, `57344`;
- prefetch: `on`;
- accounting: `on`;
- rounds: 2;
- expected processes: 10.

Require:

- all processes PASS;
- approved feature paths produce the expected non-zero candidates/hits or a
  documented measured reason why not;
- no outside-clip corruption;
- no new writePixels correctness failure;
- target-format metrics remain valid.

Performance is observational at this milestone, not a promotion gate.

Write:

- `.agent/benchmarks/image-raster-policy-03/m3-structural-validation.md`

Commit state/evidence/report updates.

Then:

    STOP / REVIEW 3

Do not begin the reuse/RGB565 benchmark until approved.

### Milestone 4 — reuse and RGB565 versus target-color benchmark

Add the forward/backward/forward workload from Contract J.

Preserve the historical single-pass profiles.

Do not rename or silently change their semantics.

Add a new runner profile dedicated to this investigation.

Use these masks:

- `0` — baseline;
- `32` — `STORAGE_RGB565`;
- `8192` — `RASTER_TARGET_COLORTYPE_CONVERSION`;
- `8224` — RGB565 plus target-color conversion.

First run a diagnostic reuse matrix:

- four masks;
- prefetch `off` and `on`;
- accounting `on`;
- one round;
- eight processes.

Require each process to complete all three passes.

Verify per-pass diagnostic deltas and actual target color type.

Then run the primary performance reuse matrix:

- four masks;
- prefetch `off` and `on`;
- accounting `off`;
- three rounds;
- 24 processes.

Do not require unavailable diagnostic counters in the accounting-off matrix.

Use the same package unless source changes after diagnostic validation.

For every mask/prefetch/pass report:

- work P50/P95/P99/MAX;
- paint P50/P95;
- process/round identity.

From the diagnostic matrix report:

- target-color observations/materializations/hits by pass;
- physical-variant observations/materializations/hits when applicable;
- converted bytes;
- RGB565 backing bytes;
- RGBA8888 backing bytes;
- cross-kind variant replacements;
- source/key multiplicity;
- target color classification.

Controlled comparisons are:

    0 -> 32
    0 -> 8192
    32 -> 8224
    8192 -> 8224

Interpret them only as measured deltas:

- `0 -> 32`: effect of compact RGB565 storage;
- `0 -> 8192`: effect of target-color conversion;
- `32 -> 8224`: added target-color behavior on RGB565 storage;
- `8192 -> 8224`: added RGB565 storage when target-color conversion is active.

Do not infer causality from total time alone. Use the color type, bytes, and
variant lifecycle counters to support any explanation.

If the active target is BGRA8888, state explicitly that target-color
conversion is testing conversion toward BGRA8888, while RGB565 storage remains
a separate 2-byte source-storage effect.

If the active target is RGB565, state explicitly that target-color conversion
and RGB565 storage converge on the same target color family and analyze that
interaction separately.

Write:

- `.agent/benchmarks/image-raster-policy-03/m4-rgb565-target-color.md`

Include:

- implementation HEAD;
- SDK/native hashes;
- bundle hash;
- corpus count/hash;
- target color type;
- raw result directories;
- ZIP SHA-256;
- 8/8 diagnostic status;
- 24/24 performance status;
- all controlled comparisons by pass and prefetch;
- accounting-on/off limitation carried from Milestone 1;
- unresolved variant-slot interference;
- evidence relevant to delayed versus immediate materialization.

Do not change defaults based on these numbers.

Do not change delayed materialization after seeing the result.

Do not split the raster-variant cache slot.

Those are follow-up architectural decisions.

Commit final benchmark summary and four-milestone state.

Suggested final commit:

    docs(benchmark): record raster reuse results

Then:

    STOP / REVIEW 4

## Surprises & Discoveries

Record only findings that change later work.

Expected categories include:

- accounting materially changes writePixels P95;
- JPEG intrinsic opacity changes cold writePixels behavior;
- actual target color differs from assumption;
- target-color full keys collapse strongly after position normalization;
- save-state rejection dominates a feature;
- physical target-size identity does or does not fragment cache use;
- shared variant-slot thrashing is material;
- RGB565 gain is primarily decode/storage bandwidth rather than target-color
  matching.

Do not pre-populate conclusions.

## Decision Log

Initial decisions:

- JPEG opacity is intrinsic, not controlled by bit 1.
- Accounting-off performance measurement keeps only external active-work
  timing.
- Milestone 2 changes diagnostics only.
- Cache and eligibility changes require human review.
- Delayed target-color materialization remains unchanged through Milestone 4.
- The single raster-variant slot remains unchanged through Milestone 4.
- Default optimization masks remain unchanged.
- No historical full matrix is required.

Add future entries only when a review changes implementation policy.

## Validation and Acceptance

Static validation is required before every logical commit.

Native and SDK builds occur only at milestone gates or after a validation
failure requires a focused retry.

Milestone 1 accepts when:

- strong clip assertions pass;
- JPEG is intrinsically opaque with bit 1 disabled;
- accounting-off avoids render-path diagnostic mutations;
- native target color is known;
- the 20-process accounting A/B completes.

Milestone 2 accepts when:

- all four diagnostic runs pass;
- required unique-key/source and rejection data are present;
- no optimization behavior changed.

Milestone 3 accepts when:

- only reviewer-approved changes were implemented;
- native correctness tests pass;
- 10/10 structural smoke processes pass.

Milestone 4 accepts when:

- 8/8 diagnostic reuse runs pass;
- 24/24 accounting-off performance runs pass;
- all three passes are present;
- controlled comparisons and artifact hashes are recorded.

Performance regression is not a correctness failure. Preserve and report it.

## Risks and Open Questions

Do not resolve these without evidence or review:

- whether target-color materialization should become immediate;
- whether target-color and physical variants need separate cache slots;
- whether any new optimization should enter the default mask;
- whether RGB565 quality trade-offs justify default use;
- whether non-rectangular Skia clips can safely use any direct physical copy;
- whether the macOS findings generalize to Windows, Linux, or Android.

The runner's bundle-relative macOS launch behavior from Plan 2 remains in
place. Do not redesign cross-platform launch behavior here.

## Idempotence and Recovery

Benchmark output directories must include profile/accounting/run identity so a
retry cannot silently overwrite another sample.

A failed process may be rerun individually only when its previous output is
moved aside or explicitly replaced by the runner.

Never combine samples from different implementation HEADs into one aggregate.

If source changes after packaging, invalidate that package and create a new
one.

If a milestone benchmark fails because of infrastructure, fix only the
infrastructure defect, rebuild if required, and rerun the complete small
milestone profile.

Do not cherry-pick or rewrite completed milestone commits to recover.

Record reviewer approval and the next permitted action in the state file before
resuming after each STOP.

## Outcomes & Retrospective

At each milestone close, record:

- delivered behavior;
- validation actually executed;
- benchmark profile and result count;
- important measured findings;
- deferred decisions;
- exact next review boundary.

At final Milestone 4 close, distinguish clearly between:

- correctness fixes delivered;
- diagnostic findings;
- structural corrections approved and delivered;
- measured performance;
- hypotheses still requiring follow-up.

Update the editorial report with exactly these headings:

- `Editorial Summary`
- `Original Plan versus Actual Outcome`
- `What Changed`
- `Decisions and Trade-offs`
- `Unexpected Problems and Discoveries`
- `Validation and Measurable Results`
- `Useful Evidence and Examples`
- `Limitations, Remaining Work, and Open Questions`
- `Possible Article Angles`
- `Suggested Narrative`
- `Claims Requiring Human Review`

Do not claim an optimization is faster unless the corresponding benchmark
supports that statement.

## Revision Note

This plan deliberately combines four related milestones into one resumable
ExecPlan because each consumes evidence produced by the previous one.

Mandatory human review boundaries prevent diagnostic hypotheses from turning
automatically into cache-policy or renderer-policy changes.

The plan stops after the RGB565/target-color reuse benchmark. Default-mask,
immediate-materialization, shared-cache-slot, and cross-platform promotion
decisions belong to a later reviewed plan.
