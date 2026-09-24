<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# PNG prefetch — Part 1: implementation and benchmark contract

This ExecPlan follows `AGENTS.md` and
`totalcross-depot-tools/.agent/PLANS.md`. It is Part 1 of 2 and fixes the
implementation architecture, focused validation, benchmark contract, and
Windows runner requirements. Part 2 performs macOS measurement, creates the
Windows package, and closes the work.

## Purpose / Big Picture

Add static PNG support to the existing `ImagePreparation` prefetch path without
changing the production thread strategy or requiring a new Windows runtime.

The customer workload has 663 files selected by `.jpg`/`.jpeg` filename. Content
inspection identifies 660 JPEG images and 3 PNG images. Before this work the
prefetch-thread benchmark reports:

    requests=663
    ready=660
    failed=0
    not-prefetchable=3

After Part 1, static PNGs must use the same serialized prefetch lifecycle and
the benchmark contract must expect all 663 files to be ready.

The production default remains `legacy`.

## Working Set and Resume Protocol

Repository:

    TotalCross/totalcross

Immutable base:

    feat/semaphore-v1
    86d470c64b7dfc0ab6ac24314f32f0d630ff1990

Create:

    feat/png-prefetch

Save this file as:

    .agent/plans/png-prefetch-part-1.md

Create:

    .agent/state/png-prefetch.md
    .agent/evidence/png-prefetch.md

Part 2 will be saved as:

    .agent/plans/png-prefetch-part-2.md

On resume, read `.agent/state/png-prefetch.md` first, then only the active
milestone in this plan. Run:

    git log --oneline 86d470c64b7dfc0ab6ac24314f32f0d630ff1990..HEAD
    git status --short

Do not reconstruct the prior semaphore investigation.

Existing pre-PNG macOS benchmark evidence is in:

    .agent/evidence/image-scroll-prefetch.md

Read only its `2026-09-24 | semaphore comparison` entries when a baseline is
needed.

## Artifact, Size, and Commit Policy

All intentional source, test, script, plan, state, evidence, and report changes
must be committed through signed logical commits.

New repository-authored files must remain below 20 KB / approximately 600
lines. Do not refactor existing large files merely to reduce size.

Normal build outputs, caches, compiler products, verbose logs, extracted SDKs,
raw benchmark directories, and generated ZIP archives remain untracked. Record
their hashes/paths/results in committed evidence.

Preserve unrelated local changes.

Follow `.agents/skills/logical-commits/SKILL.md` exactly. Before each commit:

    git status --short -- <task paths>
    git diff -- <task paths>
    python3 scripts/validate-copyright-headers.sh --files <task paths>
    git add -- <task paths>
    git diff --check --cached
    git diff --cached --stat
    git diff --cached -- <task paths>

Use English scoped Conventional Commit messages with a body for non-trivial
changes. Do not amend/rewrite history. Do not push unless explicitly requested.

## Progress

- [x] Activate `feat/png-prefetch` and commit both plans plus initial state
      (`4549dadd`, 2026-09-24).
- [x] Milestone 1: static PNG prefetch and focused SDK tests
      (`efc679d8`; 26 tests passed).
- [ ] Milestone 2: update benchmark expectations and add the PowerShell runner.
- [ ] Milestone 3: close the SDK/macOS build and native-smoke gate.
- [ ] Hand off to Part 2.

## Current Architecture and Fixed Decisions

Relevant files:

    TotalCrossSDK/src/main/java/totalcross/ui/image/Image.java
    TotalCrossSDK/src/main/java/totalcross/ui/image/ImagePreparation.java
    TotalCrossSDK/src/test/java/totalcross/ui/image/ImagePreparationTest.java
    TotalCrossSDK/src/test/java/totalcross/ui/image/
    TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/
      ImageScrollRealWorkloadBenchmarkApp.java
    scripts/run-image-scroll-distributed-benchmark.py
    scripts/test-image-scroll-distributed-benchmark.py
    scripts/package-image-scroll-benchmark.sh
    scripts/README-image-benchmarks.md

Add:

    scripts/run-prefetch-thread-benchmark-windows.ps1

Do not modify `TotalCrossVM` in this plan.

`Image.createPreparationRequest(...)` currently requires an
`EncodedImageSource` with `ImageDecodePolicy.TARGET_DECODE` and accepts only
JPEG.

`ImagePreparation.decode(...)` has two candidate routes:

1. native detached handle when `request.nativeAvailable` is true;
2. `JavaResult` backing when it is false.

The deployed full `decodeEncodedSource(...)` already supports JPEG and PNG.
The detached native candidate is JPEG-only.

The old Windows indexed-PNG problem is assumed fixed. Do not add a workaround
for it unless current validation proves a new regression.

### Supported formats

Eligibility is based only on `EncodedImageSource.getFormat()`, never filename.

Keep the existing structural requirements:

- root is `EncodedImageSource`;
- decode policy is `TARGET_DECODE`.

Support:

- JPEG, unchanged;
- static PNG only.

A PNG with more than one frame remains `NOT_PREFETCHABLE`. Other formats remain
`NOT_PREFETCHABLE`.

### JPEG

Do not change JPEG semantics:

- target dimensions unchanged;
- denominator from `ImageDecodeRequirement.choose(...)`;
- denominators 1/2/4/8;
- native detached candidate retained when native is available;
- existing retry/adoption/COPY_READY/cache behavior retained.

Do not modify `ImageDecodeRequirement` for PNG.

### PNG

For a static PNG:

- calculate the same positive target dimensions used by preparation;
- force `denominator = 1`;
- reuse only `source.decodedBackingForReuse(1)`;
- force the request's effective `nativeAvailable` to `false`;
- decode through `createJavaPreparationResult(...)`;
- call full `decodeEncodedSource(source)`;
- on deployed macOS/Windows this reaches the existing native PNG decoder;
- do not add PNG downsampling;
- do not add a second-decoder fallback.

This route intentionally avoids changing the JPEG-only detached native candidate
ABI and permits the later Windows package to reuse a trusted runtime.

### Candidate ownership

Make `JavaResult` ownership explicit.

`createJavaPreparationResult(...)` must transfer the validated decoded backing to
the result and clear the temporary decoded `Image` backing reference before
returning.

An unadopted `NativeImageBacking` is released once by
`DetachedCandidate.release()`. Successful Java-result adoption transfers the
backing to `EncodedImageSource` and clears the candidate reference once.

Add tests for stale/discarded PNG candidates to prevent leaks/double release.

### Thread strategy

Do not change the production default.

Diagnostic matrix remains exactly:

    legacy           sleep 0 ms x masks 6,38
    worker-poll      sleep 1 ms x masks 6,38
    worker-semaphore sleep 0 ms x masks 6,38

## Plan of Work

### Activation

Run:

    git fetch origin feat/semaphore-v1
    git cat-file -e 86d470c64b7dfc0ab6ac24314f32f0d630ff1990^{commit}
    git switch --detach 86d470c64b7dfc0ab6ac24314f32f0d630ff1990
    git switch -c feat/png-prefetch

If the branch already exists, do not reset it. Require:

    git merge-base --is-ancestor \
      86d470c64b7dfc0ab6ac24314f32f0d630ff1990 feat/png-prefetch

Create state/evidence skeletons. Save and commit both plan files.

Suggested commit:

    docs(plan): define png prefetch execution

State must record the immutable base, current milestone, active paths, last
commit, next command, deferred validations, blockers, and out-of-scope paths.

### Milestone 1 — Static PNG prefetch

Edit SDK code and focused tests only.

In `Image.createPreparationRequest(...)`:

1. preserve the existing immediate READY path;
2. preserve `EncodedImageSource + TARGET_DECODE`;
3. branch on actual source format;
4. keep JPEG request construction unchanged;
5. for static PNG use denominator 1 and effective native availability false;
6. reject multi-frame PNG and all other unsupported formats.

Broaden `decodeEncodedSourceJava(...)`:

- JPEG: current validation and current tiered/full behavior;
- PNG: only denominator 1, initialize decode target, call full
  `decodeEncodedSource(source)`.

Apply the ownership-transfer rule above.

Focused tests must prove:

- JPEG denominator/native behavior is unchanged;
- static PNG is prefetchable;
- PNG denominator is 1;
- PNG forces the Java-result route even when native availability was requested;
- already-decoded PNG backing is reusable at denominator 1;
- multi-frame PNG remains not-prefetchable;
- unsupported formats remain not-prefetchable;
- PNG success adopts/finishes exactly once;
- PNG decode failure finishes FAILED without deadlock;
- stale/discarded PNG candidate backing is released exactly once;
- legacy/poll/semaphore serialization is unchanged.

Reuse an existing small PNG test resource. If no suitable resource exists, add
one deterministic static PNG fixture under existing image test resources, below
20 KB.

Only at the milestone end run focused SDK tests. At minimum:

    cd TotalCrossSDK
    ./gradlew-agent test \
      --tests totalcross.ui.image.ImagePreparationTest

Add the smallest existing image test class needed to validate
`createPreparationRequest(...)`.

Then:

    cd ..
    python3 scripts/validate-copyright-headers.sh --files <changed paths>
    git diff --check

Commit implementation and focused tests together unless validation exposes a
separate logical fix.

Suggested commit:

    feat(image): add static png prefetch

Update state.

### Milestone 2 — Benchmark and Windows runner contract

Update the real-corpus prefetch expectation from:

    663 / 660 / 0 / 3

to:

    663 / 663 / 0 / 0

where fields are request/ready/failed/not-prefetchable.

Change only hardcoded outcome assumptions affected by PNG support. Do not perform
a broad rename of legacy constants such as `EXPECTED_JPEGS` unless correctness
requires it.

Extend `scripts/package-image-scroll-benchmark.sh` to content-inspect the staged
663 files and add manifest format counts:

    total=663
    jpeg=660
    png=3

Use magic/content inspection, not extension. Packaging may use Python on
macOS/Linux. The Windows execution requirement alone forbids Python.

Add:

    scripts/run-prefetch-thread-benchmark-windows.ps1

Requirements:

- Windows PowerShell 5.1 compatible;
- below 20 KB;
- runs from extracted bundle root;
- resolves relative paths from `$PSScriptRoot`;
- parses `manifest.json` and requires `windows-x64`;
- validates executable/runtime/corpus/manifest counts;
- calls no `python`, `py`, `java`, `git`, network tool, or package manager;
- runs exactly six established `pft` configurations;
- uses `/scr -1,-1,540,960`, prefetch on, accounting on;
- uses manifest dataset hash;
- creates a fresh timestamped result directory;
- captures stdout/stderr and per-process `DebugConsole.txt`;
- uses `Start-Process -PassThru`;
- forces `$process.Handle`;
- uses timed `WaitForExit(...)`, then final parameterless `WaitForExit()`;
- reads `ExitCode` only after that;
- validates `summary.json` and `counters.json`, not console text;
- requires 663/663/0/0 for each run;
- preserves legacy/poll/semaphore invariants;
- requires semaphore release=acquire=wake and outstanding=0;
- writes compact CSV, JSON, metadata, logs, and final ZIP;
- stores result rows in normal PowerShell arrays, not
  `System.Collections.Generic.List[object]`;
- on failure writes metadata and attempts evidence ZIP before returning nonzero;
- does not use terminating `Write-Error` in its catch path while
  `$ErrorActionPreference = "Stop"`.

Update packaging so the Windows bundle root contains this script and manifest
records its filename. Non-Windows bundles do not need it.

Update README wording from a pure JPEG corpus to a 663-image mixed-content
customer corpus.

Extend focused runner/package tests for:

- 663/663/0/0 validation;
- 660 JPEG / 3 PNG manifest counts;
- exact six-process matrix;
- PowerShell runner inclusion only for Windows;
- no forbidden Python/Java/Git/network call in the script;
- unique result paths and balanced semaphore validation.

Before commits run:

    PYTHONDONTWRITEBYTECODE=1 python3 -m py_compile \
      scripts/run-image-scroll-distributed-benchmark.py
    python3 scripts/test-image-scroll-distributed-benchmark.py
    bash -n scripts/package-image-scroll-benchmark.sh
    git diff --check

No SDK/native build in the middle of this milestone.

Suggested commits:

    test(benchmark): validate png prefetch outcomes
    build(benchmark): add python-free windows runner

The second commit contains the PowerShell runner, packaging/manifest integration,
and README update.

Update state after each logical commit.

### Milestone 3 — SDK/macOS build and deployed smoke gate

This is the build boundary. No Windows/Linux/Android/iOS build is allowed.

Run once:

    cd TotalCrossSDK
    ./gradlew-agent dist -x test

Do not use `clean` unless stale output is demonstrated.

On macOS, build only the existing Release native runtime/launcher targets using
the repository's current macOS CMake procedure. Save verbose output as logs.

Run the existing deployed ImagePreparation/image smoke, extending it minimally
if needed to prove PNG prefetch through the SDK Java-result route while the
actual decode uses the deployed native PNG decoder.

Smoke coverage:

- ordinary static PNG;
- indexed PNG when an existing repository/customer fixture is available;
- READY completion;
- correct dimensions;
- valid native backing after adoption;
- no duplicate decode on immediate reuse;
- no leak/double release on discarded candidate.

The historical Windows indexed-PNG bug is assumed fixed; do not add a fallback.

Run header and whitespace checks.

If a dedicated smoke change was necessary, suggested commit:

    test(image): cover deployed png prefetch

Record SDK artifact path and macOS runtime hash once in evidence.

### Handoff to Part 2

Part 1 is complete only when:

- SDK PNG contract is committed;
- focused tests pass;
- benchmark expectations are updated;
- PowerShell runner is committed and packaged by contract;
- SDK dist passes;
- macOS native build/smoke passes;
- `TotalCrossVM` has no task delta.

Require:

    git diff --quiet \
      86d470c64b7dfc0ab6ac24314f32f0d630ff1990..HEAD -- TotalCrossVM

Then update `.agent/state/png-prefetch.md`:

    activePlan=.agent/plans/png-prefetch-part-2.md
    nextAction=run fresh macOS six-process benchmark

Do not start Part 2 until this checkpoint is committed.

## Surprises & Discoveries

Record only observations that change remaining work.

Expected:

- detached native candidate remains JPEG-only;
- generic deployed full decode supports PNG;
- PNG therefore uses Java-result candidate ownership.

If current code contradicts this, record exact evidence before changing
direction.

## Decision Log

- Static PNG only; determine format from encoded content.
- PNG denominator is always 1.
- JPEG behavior remains unchanged.
- PNG uses Java-result candidate path on deployed platforms.
- No `TotalCrossVM` changes.
- No PNG downsampling or decoder fallback.
- Production prefetch-thread default remains `legacy`.

All decisions dated 2026-09-24.

## Validation and Acceptance

Part 1 acceptance:

- exact base ancestry verified;
- static PNG prefetchable at denominator 1;
- JPEG behavior unchanged;
- multi-frame PNG/other formats remain unsupported;
- ownership tests pass;
- focused SDK tests pass;
- benchmark validation expects 663/663/0/0;
- manifest identifies 660 JPEG and 3 PNG payloads;
- PowerShell runner has no Python/Java/Git/network dependency;
- SDK dist passes at milestone boundary;
- macOS native smoke passes;
- no task delta under `TotalCrossVM`;
- all durable task files committed and signed;
- all new repository-authored files stay under size limit.

No performance threshold is part of Part 1.

## Risks and Recovery

Primary risks:

- backing ownership through `JavaResult`;
- off-UI-thread use of existing native PNG full decode;
- stale benchmark assumptions;
- PowerShell 5.1 exit-code/JSON/ZIP edge cases.

Never use destructive Git cleanup. Rerun focused failed validation after a
focused fix. Do not rerun expensive completed gates after documentation-only
changes.

After every logical commit rewrite state with current milestone, last commit,
active paths, completed validation, deferred validation, next command, blockers,
and deliberate out-of-scope files.

## Outcomes & Retrospective

During execution keep only milestone-level facts here or in state. At Part 1
completion, record delivered behavior, commit hashes, focused validation, and
the Part 2 handoff.

## Revision Note

Initial Part 1 revision, 2026-09-24.
