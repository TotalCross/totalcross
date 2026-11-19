<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Fix Image(String) Filesystem Loading

This ExecPlan follows `AGENTS.md` and `.agent/PLANS.md`.

## Purpose / Big Picture

Make deployed `new Image(String)` load images from the app filesystem while
preserving the existing TCZ-first lookup, packaged-image fast path, deferred
decode, and `Image(File)` path behavior. A found TCZ entry that is invalid or
unreadable must fail without trying a same-name filesystem file.

## Working Set and Resume Protocol

- Read `.agent/state/fix-image-string-filesystem-fallback.md` first on resume.
- `.agent/evidence/fix-image-string-filesystem-fallback.jsonl` is append-only
  validation evidence; search it for a specific milestone when needed.
- This file records stable scope and acceptance requirements.
- Read `.agent/guides/macos-native-runtime-validation.md` before native macOS
  smoke work. Read `docs/architecture/build-provenance.md` for artifact identity.
- Read the `logical-commits` and `validate-headers` skills before commits and
  focused header checks; never stage unrelated local files.
- The source request is `/Users/flsobral/Downloads/luna-image-string-correction-plan-v4.md`.

## Progress

- [x] (2026-11-19) Confirmed the starting commit is the plan's static-analysis
  reference, created branch `fix/image-string-filesystem-fallback` from
  `master` in an isolated worktree, and confirmed a local Apple Silicon macOS
  host. See the evidence index for the pre-change environment record.
- [x] (2026-11-19) Created PNG/JPEG files at runtime and demonstrated with the
  pre-fix deployed macOS arm64 VM that `Image(File)` loads both while
  `Image(String)` raises `ImageException` for both. SDK, app TCZ, VM, and
  Launcher share build ID `0104442b…`; detailed commands and log paths are in
  the evidence index.
- [x] Implement the Java/native contract and converter regressions; targeted
  converter/image tests, bridge generation, JavaSE, and deployed macOS arm64
  validations passed.
- [x] Add compact correctness coverage and an opt-in input on the existing
  manually dispatchable Merge Flow, reusing `build.yml` for only the Android,
  Windows x64, and Linux x64 functional lanes.
- [x] Commit the fix, regression app, and workflow in three focused local
  commits; validate commit messages, headers, workflow YAML, shell blocks, and
  the staged diffs.
- [x] Push the feature branch and manually dispatch Merge Flow with the smoke
  input after user authorization. The first run exposed three harness issues:
  Linux and Windows launchers need the deployed folder as their working
  directory, and the Android setup action must not request the removed SDK
  Manager `tools` package. The second run exposed three more: Linux needs its
  EGL/GLES runtime libraries installed; Android app IDs must come from the
  smoke class's static initializer; and Windows' GUI launcher exposes the
  result through its process exit code rather than stdout. Corrections are in
  commit `2aa3c08b8`; the focused smoke-app packaging task and workflow syntax
  checks passed. In run `44602636798`, Windows passed with exit code 0; Linux
  aborted with a glibc buffer-overflow report after loading the VM; Android's
  emulator booted, but the action ran a Bash-only `pipefail` setting through
  `/usr/bin/sh`. Added flushed Linux phase markers and changed the Android
  script to POSIX `set -eu` in commit `8705970bd`. The smoke app package and
  workflow syntax checks passed. Pushed and dispatched run `44606045633`.
  Windows passed; Linux still aborts with glibc's buffer-overflow report
  (exit 134), and its `Vm.debug` phase markers were not visible. Android booted,
  but `adb install` received an empty path because the emulator action runs each
  script line in a separate shell. Commit `85e812d57` now writes phases
  directly to stderr and captures a GDB backtrace after a Linux failure. The
  Android workflow now writes a shell script during deploy and invokes it with
  one action-script line, and verifies that deploy produced an APK before
  booting the emulator. YAML, Android shell syntax, headers, and diff checks
  passed. Commit `5afd73e06` was pushed and dispatched as run `44609764897`;
  Windows passed. Linux aborts before the app in `startVM` at glibc
  `__realpath_chk`; the backtrace does not yet show the path or buffer capacity.
  Android remains active. Commit `78ee90d79` adds a GDB breakpoint for those
  values plus a full backtrace; it was pushed and dispatched as run
  `44611514188`. Both runs are pending Android results. See the evidence index.
- [ ] Reconcile the next Android, Windows x64, and Linux x64 functional results
  and artifacts with the acceptance criteria.

## Current Architecture and Scope

`EncodedImageSource.fromPath` calls the private native-replaced
`captureNativePath(String)`. Its native bridge currently looks only in loaded
TCZ resources, writes the unchecked path into `char path[256]`, and throws if
the entry is absent. `Image(File)` instead reads through `totalcross.io.File`
and `fromStream`, which supplies path normalization, `device/` handling,
permissions, error behavior, and encoded-source ownership.

Change only `EncodedImageSource.java`, its converter and runtime regressions,
`TotalCrossVM/src/nm/ui/image_Image.c`, and narrowly scoped build/CI harness
files when needed. Do not alter the public `Image` API, decoders, Skia,
caching, external-storage permission policy, or unrelated file APIs.

## Plan of Work

### A. Establish a real pre-fix regression

1. Inspect the current SDK tests and macOS smoke conventions.
2. Add a fixture that creates distinct valid PNG and JPEG files at runtime,
   outside TCZ packaging, then checks `Image(File)` and `Image(String)` on the
   same path. Close every `File` and image resource it owns.
3. On this local Apple Silicon macOS host, build the pre-fix SDK and VM from
   this worktree, deploy the fixture with that SDK, and run it with that freshly
   built VM. Record exact fixture paths, commit, commands, and results.
4. If a prerequisite prevents an authentic pre-fix run, keep the regression
   and record the unavailable evidence without inferring a reproduction.

### B. Implement the fallback contract

1. Make private native-replaced `captureNativePath(String)` return `boolean`:
   true means capture succeeded; false means no TCZ entry exists. Preserve
   JavaSE `Launcher.instance.readBytes`, including the unavailable-launcher
   error and missing-path result.
2. In `fromPath`, reject null, return the captured source on true, and only on
   false open `new File(path, File.READ_ONLY)` in try-with-resources and capture
   through `fromStream`. Do not catch `ImageException` to trigger fallback.
3. In native code initialize `p->retI` to false, validate the path object, and
   check `String_charsLen` before conversion. Paths of 256 or more Java
   characters return false without copying. Missing TCZ entries return false;
   set true only after successful bag capture and metadata publication.
   Preserve error categories, cleanup, symbol name, and ABI mapping.
4. Extend converter tests for the native bridge and boolean return contract.
   Generate bridges and verify the legacy symbol ABI.

### C. Keep focused regression coverage

The main repository owns product correctness regressions. Cover TCZ-only and
filesystem-only PNG/JPEG; TCZ precedence over a different same-name physical
file; empty/corrupt TCZ non-fallback; missing and invalid physical files;
supported relative, absolute, and `device/` paths; lengths 255, 256, and above
256; native-bag packaged path; repeated create/release; and runtime-only
filesystem fixtures. Add larger harness orchestration in
`TotalCross/totalcross-performance-lab` only if it exists and is needed to
avoid broad infrastructure in this SDK. Reuse its runner patterns. Never use
its unrelated fixed-version SQLite/customer workflow or historical artifacts.

### D. Validate only the assigned platform lanes

- Local: targeted SDK tests and JavaSE functional checks; build the corrected
  native VM and run the deployed smoke on Apple Silicon macOS. A simulator pass
  does not count as native evidence.
- GitHub Actions only: Android emulator functional smoke, Windows x64 native
  functional smoke, and Linux x64 native functional smoke. All must use the
  SDK and VM from the same implementation commit and report provenance,
  assertions, exit code, metadata, and logs as artifacts.
- iOS: no build or functional test; report this coverage limitation.
- Do not locally build/run Android, Windows, Linux, or iOS applications, and do
  not use local emulators, VMs, containers, paid device farms, or external
  physical devices for these smokes.

Inspect `.github/workflows/build.yml`, `.github/workflows/merge.yml`, and
available lab workflows before adding CI. Reuse canonical entry points. Add a
narrow manually dispatchable or branch-scoped workflow only when no suitable
functional smoke exists. Never claim CI success without run artifacts.

### E. Commits and publication

Create small, meaningful English commits at coherent validated checkpoints:
functional Java fix and converter tests; native bounds/error handling; and
dedicated regression/CI harness when warranted. Use the repository commit
validator and header validator; inspect and diff-check each staged set. Do not
amend, rebase, force-push, merge, or create a PR. Local commits are part of the
requested plan; pushing or triggering remote workflows requires explicit
authorization unless an already-authorized trigger exists.

## Surprises & Discoveries

- The source checkout initially contained many unrelated untracked files, so
  work is isolated in `/Users/flsobral/repos/totalcross-image-string-filesystem-fallback`.
- The sibling performance lab checkout exists on branch
  `perf/image-rendering-benchmarks`; its README assigns product correctness
  regressions to the main repository, and its current runner inventory has no
  image-path smoke workflow. Keep the compact product regression in this repo.
- The existing encoded-source macOS deploy task did not stage its native
  Launcher; it now uses `configureImageMacSmokeSdk`. Its staging task also
  omitted the added nested result class; the include now covers nested classes.
  Both harness issues were fixed before the accepted baseline run.
- `Image(File)` and `Image(String)` results are recorded separately so the
  expected pre-fix failure remains diagnostic and does not stop the JPEG case.
- The expanded deployed smoke passed on macOS arm64. A runtime write probe
  found `device/` unavailable in the desktop app sandbox, so that platform
  records the path as unsupported; the Android lane requires the alias to be
  writable and verifies its PNG round-trip.
- The normal Windows build in `build.yml` targets Win32. When the new smoke
  input is enabled, that same canonical build job switches to x64, and the
  functional job requires a 64-bit Visual Studio project before running it.
- GitHub requires a `workflow_dispatch` file to exist on the default branch.
  The feature-only dispatcher was removed and the smoke input was added to the
  existing Merge Flow so it can run against this branch without a PR.
- A local macOS ARM64 AddressSanitizer build and deployed smoke passed with no
  sanitizer report. SDK JAR, smoke TCZ, ASan dylib, and Launcher share build ID
  `50e78dce…`; logs and exact identity output are in the evidence index.
- The authorized Merge Flow has run four times. Windows passed in runs
  `44602636798` and `44606045633`; Linux's exit-134 abort remains under
  diagnosis. Android reached the smoke script in run `44606045633`, where an
  empty APK variable prevented the app from running; a single-shell harness
  correction is in progress.

## Decision Log

- A TCZ entry wins over a same-name disk file. Fallback occurs only when
  `tczGetFile` finds no entry; capture failures retain their exception.
- Native capture declines Java paths of 256 or more characters without
  conversion, allowing the ordinary `File` constructor to validate them.
- Keep file access on `totalcross.io.File` plus `fromStream` so deployed
  filesystem semantics match `Image(File)`.
- Use an isolated worktree to preserve the original checkout's local files.

## Validation and Acceptance

Required: pre-fix deployed reproduction or explicit unavailable-evidence
record; bridge generation and converter test; targeted JavaSE tests; corrected
native macOS build and real deployed PNG/JPEG parity; path-length and TCZ
non-fallback behavior; local commits; and Android, Windows, and Linux CI
functional results. The user authorized the branch push and manual dispatch.
Windows passed, Linux still aborts, and Android did not reach the app because
the action split the smoke script into separate shells. Re-run with the
single-shell Android harness and Linux diagnostics.
The exact Gradle test selector and existing smoke tasks must be established
from project configuration before invocation. Use `.agent/guides/macos-native-runtime-validation.md`
and the build-provenance guide for local deployed identity. iOS coverage is
explicitly out of scope and must be stated as a limitation.

## Risks and Open Questions

- Can this clean worktree fetch/build all pinned native dependencies and run a
  real deployed fixture on the available Apple Silicon host?
- Is the performance-lab repository available through a connected source even
  though it is not checked out locally?
- Remote Android/Windows/Linux functional runs cannot be claimed unless an
  authorized push/PR or existing trigger produces those runs.

## Idempotence and Recovery

Use the isolated branch/worktree and task-specific logs/artifacts. Never clean
or modify the original checkout's unrelated files. Check the branch and
worktree before retrying setup. Rebuild the SDK, Launcher, and native VM from
the same worktree/commit. Keep generated outputs and logs untracked/ignored.
Stage explicit task paths only; do not use broad `git add`.

## Outcomes & Retrospective

Local implementation and validation are complete. The pre-fix deployed failure
was reproduced on macOS arm64, then the corrected SDK and native VM passed the
expanded JavaSE and deployed macOS suites. Three local commits contain the
production change, deployed regression harness, and opt-in Actions workflow.

The user authorized pushing the feature branch and dispatching the existing
Merge Flow with the smoke input. Windows has passed; Android API 35 and Linux
x64 still need successful functional results. iOS remains out of scope.

## Revision Note

Created from the user's v4 plan; the starting commit matches its specified
static-analysis reference.
