<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Make native dependency fetching reliable, idempotent, and reusable across CI jobs

This ExecPlan follows the `AGENTS.md` rules of both repositories and the
`TotalCross/totalcross-depot-tools` `.agent/PLANS.md` format.

## Purpose / Big Picture

Make native dependency preparation deterministic and cheaper without adding
parallel downloads.

The observable outcome is:

1. transient GitHub/CDN failures such as `curl: (52) Empty reply from server`
   are retried safely and produce actionable diagnostics;
2. depot-tools fetchers share one GitHub Release download implementation instead
   of duplicating curl/token/API fallback logic;
3. already-installed artifacts with matching provenance are reused without
   network access, while incomplete or mismatched installs are fetched again;
4. Skia target libraries are fetched per target, but its shared dev bundle and
   release metadata are installed only once per preparation session;
5. SQLite release metadata is resolved once per repository/tag session instead
   of repeating a known-failing direct request plus API metadata lookup for
   every target;
6. consumed release assets have SHA-256 verification, including Skia libraries,
   dev bundle, machine/build metadata, and manifests;
7. TotalCross CI treats the prepared depot-tools tree as an explicit workflow
   artifact passed to downstream jobs. `actions/cache` may accelerate the
   producer, but it is no longer the correctness/transport mechanism;
8. no concurrent/parallel download implementation is added in this plan.

Execution started on local `feat/logical-ui-scaling2`; depot-tools changes were made in the nested `TotalCrossVM/deps/totalcross-depot-tools` checkout on its fast-forwarded local `main`. Logical local commits exist in each repository; no push, tag, release, rebase, amend, or history rewrite was performed.

## Working Set and Resume Protocol

Primary TotalCross paths are `.github/workflows/build.yml`, `TotalCrossVM/deps/fetch-depot-tools.sh`, `TotalCrossVM/deps/totalcross-depot-tools.ref`, `scripts/`, and `TotalCrossSDK/build.gradle` only if SDK vcruntime handoff changes. Keep the committed depot-tools ref immutable; never replace it with `main`.

Primary depot-tools paths are `scripts/`, representative `<dependency>/fetch.sh` files, `sqlite3/fetch.sh`, `skia/fetch.sh`, `skia/artifacts.json`, `deps.yml`, dependency manifests, and existing script tests.

On resume, inspect status separately in both repositories:

    git status --short
    git -C TotalCrossVM/deps/totalcross-depot-tools status --short

Then read only this plan's active milestone and changed paths. If execution spans
multiple sessions, keep compact evidence in
`.agent/evidence/native-dependency-fetch-reliability.md`; do not duplicate raw
logs in the plan.

## Progress

- [x] (2026-08-12T19:10:00Z) Recorded TotalCross `cc5ab3aa8`, depot-tools
      `01e346b`, immutable consumer pin `repo-2026.08.12`, generated local
      trees, and request-count baselines without cleaning either repository.
- [x] (2026-08-12T19:35:00Z) Milestones 1-2 completed in depot-tools commits
      `d4e10aa` and `4dec589`: shared resilient transport and marker-backed
      staged installation.
- [x] (2026-08-12T20:00:00Z) Milestones 3-4 completed in depot-tools commits
      `6acf53a`, `158125b`, and `c5b80eb`: one Skia shared install, cached
      repo/tag metadata, and checksums for 177 published assets.
- [x] (2026-08-12T20:08:00Z) Milestone 5 completed in TotalCross commits
      `f326da795` and `9642399b9`: extracted preparation and explicit
      producer-to-consumer artifact transport.
- [x] (2026-08-12T20:20:00Z) Focused and local milestone validation completed.
      The final 71-entry rerun made zero artifact or metadata requests; its
      clean prepared archive is 62,738,241 bytes. Remote CI is deferred until
      depot-tools `c5b80eb` is published and the immutable consumer pin can be
      updated.

## Current Architecture and Scope

The TotalCross `checkout-source` job restores an `actions/cache` entry for the
whole `TotalCrossVM/deps/totalcross-depot-tools` directory, refreshes depot-tools,
and invokes dependency `fetch.sh` scripts for a target matrix. Downstream jobs
restore the same cache.

Cache currently serves acceleration and cross-runner transport; an exact hit can leave downstream jobs with older content after the producer fetched an override. Make the prepared tree an explicit current-run artifact.

Fetchers duplicate token selection, curl flags, direct Release URLs, and API
fallback. Several use `--retry 3`; this did not retry the observed curl exit 52.
When an explicit token-env option is passed but empty, some fetchers do not fall
through to `GITHUB_TOKEN`.

SQLite can use a private/overridden release where the direct browser URL returns
404 but the authenticated Release Asset API succeeds. Repeating that direct
failure and metadata request for every target is unnecessary.

Skia's `--install-dev` path installs shared development/release metadata. Calling
it once per target repeats target-independent downloads.

Scope is the first six reliability/efficiency priorities and the TotalCross CI
transport needed to consume them. Do not add download parallelism, change ABI or
artifact names, redesign the native build matrix, or publish releases. If a
checksum gap requires a new release, stop and report it as a release-state
blocker instead of publishing.

## Plan of Work

### Milestone 1 — Shared resilient GitHub Release downloader

Work in depot-tools first.

Create one sourceable shared script under `scripts/`, for example
`scripts/github-release.sh`; choose the final name after checking conventions.
It must:

- resolve a token from the explicitly requested env first, then the
  dependency-specific default, then `GITHUB_TOKEN`;
- never print token values or authenticated/private URLs;
- download to a temporary file and expose the file only after success;
- retry transient transport failures including curl exit 52;
- retry HTTP 408, 429, and 5xx with bounded backoff;
- avoid wasting retries on permanent direct-asset 404s;
- use `--retry-all-errors` only when supported, or provide a portable bounded
  retry loop for older curl versions;
- after direct Release URL failure, allow Release Asset API fallback for both
  authenticated and public releases;
- cache release metadata for the current preparation session by repo/tag;
- emit concise attempt/fallback diagnostics and a concrete terminal error.

Use a caller-provided session cache directory such as
`TOTALCROSS_DEPOT_FETCH_CACHE_DIR`. Without it, use a safe temporary/per-process
fallback. Never commit or archive session metadata.

Add focused tests with a local/mock HTTP endpoint or injectable curl wrapper.
Cover transient error then success, direct 404 then API success, empty explicit
token falling back to `GITHUB_TOKEN`, public API fallback, bounded terminal
failure, and no partial output after failure.

Migrate representative fetchers first, then all fetchers that duplicate the
same GitHub Release transport logic. Preserve existing `<dependency>/fetch.sh`
CLI and install layout.

Acceptance: an injected curl-52 is retried and the existing public/private
release paths still resolve through one shared implementation.

Logical depot-tools commit:

    fix(fetch): harden GitHub release downloads

### Milestone 2 — Provenance-aware idempotent installs

Define a small installation marker owned by the fetcher, for example
`.totalcross-artifact.json` in each final artifact root. It should identify:

- marker schema version;
- dependency;
- source repository;
- effective release tag;
- asset name;
- expected/downloaded SHA-256.

An install is reusable only when the destination exists, the marker matches the
requested repo/tag/asset/checksum identity, and required headers/libraries/target
files are present. Missing/mismatched marker or required files means cache miss.
A partial extraction must never create a valid marker.

Install through staging and replace the final destination only after download,
checksum, extraction, and content validation succeed. Preserve SQLite's
repo/tag namespace. Add one shared force-refetch mechanism only if useful for
recovery.

Tests must prove: first fetch downloads; identical second fetch does no network
request; changed tag/repo/checksum refetches; incomplete destination refetches;
and failed refresh leaves the previous valid install intact.

Acceptance: restoring stale Actions cache can accelerate preparation but cannot
silently satisfy a different requested artifact.

Logical depot-tools commit:

    perf(fetch): reuse verified native artifacts

### Milestone 3 — Deduplicate Skia shared data and SQLite release metadata

For Skia, separate target-artifact installation from release-wide development
metadata. `skia/fetch.sh --platform ... --arch ...` should fetch target-specific
content unless shared metadata is explicitly requested.

Provide a release-wide operation or idempotent option that installs at most once
per effective release/session:

- Skia dev headers bundle;
- machine build configs needed by consumers;
- build manifests needed by consumers.

Update the TotalCross preparation loop so per-target Skia fetches do not pass
`--install-dev` repeatedly. Invoke the shared Skia installation once. Preserve
all existing paths expected by CMake.

For SQLite, use the shared downloader's repo/tag metadata cache. Once release
metadata is available, later targets must resolve asset API URLs from that cache
instead of repeating the known-failing direct request and downloading the same
JSON. Keep this cache session-scoped and outside CI artifacts.

Add/request-count tests. For one SQLite repo/tag across multiple targets, expect
one release metadata request. For Skia, expect one shared dev/metadata install
regardless of target count.

Preferred logical commits:

    perf(skia): install shared release metadata once
    perf(sqlite): reuse release asset metadata

If SQLite behavior is entirely provided by Milestone 1 and requires no
SQLite-specific source change, do not create an empty SQLite commit; capture the
proof in the shared helper tests.

### Milestone 4 — Complete SHA-256 verification

Use immutable, reviewable checksum metadata rather than trusting only successful
HTTP transfer.

For Skia, `skia/artifacts.json` lists release assets but lacks the SHA-256 values
consumed by `skia/fetch.sh`. Populate hashes for all TotalCross-consumed Skia
assets, including target libraries, dev bundle, machine build configs, and build
manifests. Derive them from the published `SHA256SUMS` or another existing
repository-generated source of truth; never invent hashes.

For ordinary dependency archives, inspect existing release/manifests and choose
one maintainable representation. Prefer checksum data generated by the release
pipeline. GitHub release `digest: sha256:...` may be useful for transport
verification, but do not rely on mutable remote metadata as the only reproducible
checksum contract.

Teach the shared fetch path to verify before extraction. On mismatch, delete the
temporary file, leave an existing valid install untouched, and report
expected/actual digest without secrets.

Update release/package scripts only as needed to keep checksum generation automatic. Do not publish a release.

Acceptance: focused TotalCross-consumed fetches emit no `no sha256 configured`
warning, and an injected mismatch fails before installation.

Preferred logical depot-tools commit:

    build(fetch): enforce native artifact checksums

If generation and enforcement are independently meaningful, split into:

    build(release): publish native artifact checksums
    build(fetch): enforce native artifact checksums

### Milestone 5 — Make prepared dependencies an explicit TotalCross CI artifact

Work in TotalCross after depot-tools behavior is locally validated.

Extract the large dependency-preparation shell block from
`.github/workflows/build.yml` into a repository script if this materially
improves testability, e.g. `scripts/fetch-native-dependencies.sh`. Preserve the
current sequential target matrix and environment overrides. Set one
`TOTALCROSS_DEPOT_FETCH_CACHE_DIR` under `$RUNNER_TEMP` for the producer session.

Keep `actions/cache` in `checkout-source` only as an optional accelerator if it
still helps. Correctness must not depend on cache save/restore.

After depot-tools/bootstrap and dependency preparation succeed, package a
prepared depot-tools consumer artifact, for example:

    depot-tools-native-dependencies.tar.zst

It must include the depot-tools files needed by CMake/fetch consumers plus all
required `local/` installs for this TotalCross build. Exclude `.git`, session
metadata cache, temporary downloads, logs, and unrelated generated files.
Include a compact manifest with the effective depot-tools revision and dependency
repo/tag/checksum identities.

Upload once from `checkout-source`. Downstream SDK/platform jobs that currently
restore `TotalCrossVM/deps/totalcross-depot-tools` from `actions/cache` must
instead download/extract the prepared artifact at that exact path before build
configuration.

Keep `source-code.tar.gz` separate. Do not inflate it with native dependencies.

Reconcile the existing explicit `sdk-native-runtime` artifact carefully. If the
prepared dependency artifact already contains the exact vcruntime and the SDK
path can reuse it without network, remove the specialized handoff only after a
focused proof. Otherwise retain it temporarily.

Acceptance:

- producer works from cache miss and produces the complete prepared tree;
- downstream jobs make no release-asset network request for prepared artifacts;
- downstream correctness does not depend on cache save;
- a producer override is exactly what downstream receives;
- cache miss/deletion does not alter prepared artifact contents.

Preferred logical TotalCross commits:

    refactor(ci): extract native dependency preparation
    ci(deps): distribute prepared native dependencies

Use one commit if extraction is too small to stand alone. Never mix nested
repo commits into TotalCross commits.

### Milestone 6 — Cross-repository handoff and final verification

`TotalCrossVM/deps/totalcross-depot-tools.ref` must remain an immutable published
ref. The nested depot-tools checkout being on `main` is only the implementation
workspace; do not commit `main` as the consumer pin.

This plan does not authorize push/tag/release. Do not update the TotalCross pin
to an unpublished local depot-tools commit. For local integration, use the
already-present nested checkout directly and, where necessary, an explicit local
commit SHA without changing the committed pin.

If the final TotalCross workflow requires the new depot-tools implementation to
be remote before CI can pass, stop with both repositories committed locally and
report the exact depot-tools commit(s) that must be published/tagged. After the
user publishes an immutable ref, a later authorized handoff may add:

    build(deps): update depot tools revision

Do not create/push that tag automatically.

## Surprises & Discoveries

- An inspected CI run restored an exact depot-tools cache hit and then fetched
  dependencies again, showing that current installs are not reused.
- The same run showed Skia shared dev/build metadata installed repeatedly after
  target-specific downloads.
- SQLite repeatedly attempted a direct URL returning 404, then fetched/parsing
  the same release metadata before using the asset API.
- The TotalCross branch already has an explicit SDK vcruntime handoff; preserve
  it until the broader artifact proves it redundant.
- The committed TotalCross depot-tools ref is immutable while the requested
  implementation workspace is nested `main`; keep those roles distinct.
- Every asset in all 15 releases pinned by current `deps.yml` already exposes a
  GitHub `sha256:` digest. This allowed a checked-in 177-asset checksum contract
  without republishing any release.
- Several fetch-script default tags lagged `deps.yml`. The extracted TotalCross
  preparation script now reads `deps.yml` when no override is supplied, while
  direct fetcher CLI defaults remain backward compatible.
- A validation tool yield left an earlier preparation process running and a
  second manual run briefly overlapped it. The staged replacement restored the
  old valid tree; a subsequent single run passed. CI still invokes exactly one
  sequential producer, and no parallel downloader was added.

## Decision Log

- Decision: centralize GitHub Release transport in depot-tools rather than patch
  curl flags independently in TotalCross.
  Rationale: all consumers should share retry, token, API fallback, checksum,
  and diagnostic behavior.
  Date: 2026-08-12.

- Decision: explicit workflow artifact is the correctness boundary; cache is
  optional acceleration only.
  Rationale: cache entries are immutable by key and are not a dependable
  cross-runner handoff contract.
  Date: 2026-08-12.

- Decision: keep downloads sequential.
  Rationale: parallel fetching is explicitly outside this plan.
  Date: 2026-08-12.

- Decision: never point TotalCross at depot-tools `main`.
  Rationale: consumer builds need a reproducible immutable ref.
  Date: 2026-08-12.

- Decision: check in GitHub-published release digests and refresh them from
  locally verified artifacts during future release preparation.
  Rationale: existing releases provide immutable reviewable SHA-256 values,
  while override releases can still use GitHub's digest field without weakening
  verification.
  Date: 2026-08-12.

- Decision: retain the specialized `sdk-native-runtime` artifact for this
  handoff.
  Rationale: the broader artifact is proven locally, but removing the existing
  SDK boundary should wait for remote CI after depot-tools publication.
  Date: 2026-08-12.

## Validation and Acceptance

Follow the smallest sufficient validation level from each repository's
`AGENTS.md`. Do not run the full platform matrix after each commit.

For depot-tools functional commits, normally use Level 1/2:

- `bash -n` on changed shell scripts;
- focused downloader tests with simulated failures/request counts;
- changed-file header validation through repository tooling/skill;
- one public fetch such as libpng or zlib-ng into a temporary destination;
- one SQLite/API fallback test when credentials/override are available, without
  exposing secrets;
- two consecutive identical fetches proving the second performs zero artifact
  network requests;
- checksum mismatch fixture;
- `git diff --check`.

At depot-tools milestone closure, run a sequential focused subset spanning the consumed platform families, SQLite, and Skia; do not rebuild the native libraries.

For TotalCross, validate:

- syntax of any extracted preparation script;
- workflow YAML with an existing validator if available;
- local preparation using the nested depot-tools checkout and temporary session
  cache;
- archive content/manifest proving required targets exist and `.git`/session
  cache are absent;
- extraction into a clean temporary directory followed by representative CMake
  dependency resolution or equivalent find checks;
- focused SDK vcruntime packaging only if that handoff changes;
- `python3 scripts/validate-copyright-headers.sh --files ...` for changed
  first-party files;
- `git diff --check`.

At final closure compare request counts with baseline: identical valid installs perform no network fetch; Skia shared data installs once; SQLite metadata resolves once per repo/tag session; consumed assets have checksums; downstream jobs consume the producer artifact rather than cache as source of truth.

Do not run full platform builds solely for shell/CI refactoring unless focused checks expose platform-specific risk.

## Risks and Open Questions

- Older supported environments may lack `--retry-all-errors`; feature-detect or
  use a portable retry loop.
- Private SQLite metadata must never be archived or printed.
- Existing releases may lack a reproducible checksum source for every asset. Do
  not fabricate hashes; identify the proper published/release-pipeline source.
- The prepared depot-tools artifact may be larger than source. Measure compressed
  size before considering platform splitting; do not preemptively create a
  complex artifact matrix.
- Defensive downstream fetches should reuse valid installs rather than disable local auto-fetch.
- TotalCross cannot pin unpublished local depot-tools commits. Remote handoff
  requires later user-authorized publication/tagging.

## Idempotence and Recovery

Never delete local native caches solely to create a clean baseline.

Downloader writes use temporary/staging paths. A failed download, checksum,
extraction, or validation leaves an existing valid installation usable. Write
the valid marker last.

Reruns may reuse completed valid installs; session metadata is disposable.

Before each commit, stage only intended paths in the active repository. Commit depot-tools inside its repository, then return to TotalCross for TotalCross commits.

Do not run `fetch-depot-tools.sh` in a way that would replace active uncommitted nested depot-tools work; validate against the nested checkout until it is safely committed.

No push, tag, release, amend, rebase, reset-hard, or force checkout is part of
this plan.

## Outcomes & Retrospective

The six priorities are implemented locally without parallel downloads. Shared
transport retries curl exit 52 and transient HTTP statuses, avoids retrying 404,
falls through empty token variables, and caches API metadata/routing. Provenance
markers bind repository, tag, asset, and checksum to required installed files;
the clean rerun reused all 71 identities with zero network requests.

For seven TotalCross targets, Skia changes from 98 requests (seven repetitions
of library, config, dev bundle, and eleven manifests) to 26 on an empty
preparation and zero on a valid rerun. A seven-target SQLite API-fallback session
changes from 21 requests (seven direct 404s, metadata reads, and API assets) to
9 (one direct 404, one metadata read, and seven API assets), with metadata read
once. Checksum coverage is 177 assets in 15 pinned releases; all 34
TotalCross-consumed Skia assets also carry hashes in `skia/artifacts.json`.

The prepared artifact is 62,738,241 bytes (59.83 MiB), has 2,459 members, 71
dependency identities, the exact depot-tools revision, and no Git/session-cache
content. Six downstream workflow jobs consume this artifact rather than cache.

The remaining boundary is remote publication: publish depot-tools
`c5b80eb1bb694d34aade98a3e30dcbfd74221353`, choose an immutable tag or commit
reference, then replace TotalCross pin `repo-2026.08.12` in a later authorized
commit and run the remote workflow. Evidence and final narrative are in
`.agent/evidence/native-dependency-fetch-reliability.md` and
`.agent/reports/native-dependency-fetch-reliability-editorial.md`.

## Revision Note

2026-08-12: Initial plan. It covers resilient shared downloads, Skia
shared-download deduplication, explicit CI artifact transport, idempotent
installs, SQLite metadata reuse, and checksum completion. Parallel downloads are
deliberately excluded.

2026-08-12: Completed all locally executable milestones and consolidated factual
request counts, checksum coverage, archive measurements, commit boundaries, and
the unpublished immutable-pin handoff.
