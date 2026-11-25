<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Active state: Image(String) filesystem fallback

## Current execution timebox

- User resumed the plan at **2026-11-25 19:22:44 UTC**; hard deadline is
  **2026-11-25 20:22:44 UTC**. Checkpoint closure starts by 20:12:44 UTC.
- Resume began from clean `d51ba37590ea3dcb3519af65c57306cbb5045486` on
  `fix/image-string-filesystem-fallback`; local and origin refs matched, with
  no active workflow. The attachment requires stopping implementation and
  testing by minute 55 and completing a truthful docs checkpoint by minute 60.

## Resume progress (2026-11-25)

- Removed only the deterministic `Image.java` retry from `231dd329b` in commit
  `43298175a`; the targeted lazy-materialization, image-source, and converter
  tests passed. Details, including the incomplete direct deploy attempt, are
  in the append-only evidence index.
- Replaced the unsupported device `Long.toHexString` diagnostic and moved
  runtime-created PNG/JPEG parity before TCZ decoding in `d3d6af5ef`. The
  Android 15 KVM and manifest-derived launch correction is committed in
  `6ccab1de5`; YAML, shell, and mocked pass/failure preflights passed.
- The previous TCG Android artifact was inspected: dex2oat reported a string
  size mismatch and system ANRs occurred, but no app activity/result proves
  the cause. The new run stopped before `am start -W` because the hosted runner
  could not read/write `/dev/kvm`; the prior cause remains inconclusive.
- Runner availability check found zero repository-level self-hosted runners.
  Listing organization runner groups requires `admin:org`, unavailable to this
  session. The performance-lab reference also uses `ubuntu-24.04`; its latest
  run passed the udev step but failed emulator boot, so it does not establish a
  KVM-capable alternative.
- No different runner label can be selected from the current repository state.
  The next external prerequisite is an owner/admin-provided runner with
  read/write KVM access; no second CI run was started.
- The user confirmed that no KVM runner is available now. Cross-platform
  acceptance cannot advance until runner availability changes; Linux PNG
  decode remains a separate unresolved CI failure.
- Pushed the three code commits and dispatched the existing Merge Flow once
  with `image-string-smoke=true` at source commit
  `6ccab1de50214fac44f2642960762edb1c867444`; run `45688281871` completed.
  Windows passed; Linux observed the expected `cdf05a32` TCZ checksum but
  `Image(byte[])` and `Image(File)` both failed PNG decode with ADLER32. This is
  a Linux decode/fixture issue, not evidence of a filesystem fallback
  regression. Android stopped at KVM preflight. The checkpoint deadline
  remains 20:22:44 UTC, with closure due by 20:12:44.

## Active slice

**Checkpointed after the 2026-11-25 one-hour resume.** Code changes were pushed
through `6ccab1de50214fac44f2642960762edb1c867444`; the final docs checkpoint
commit is recorded in the handoff and this file's Git history. The single
authorized run completed: Windows passed, Linux failed on PNG decode with a
matching source checksum, and Android stopped before deploy because the runner
could not read/write `/dev/kvm`. Acceptance remains incomplete. Read this state
first, then the 2026-11-25 section of the handoff for exact artifacts, commands,
platform results, and the next action. No workflow is active.

## Last checkpoint

- Resume code checkpoint: `6ccab1de50214fac44f2642960762edb1c867444` on
  `fix/image-string-filesystem-fallback`, pushed to origin. The one new Merge
  Flow run is `45688281871`; its acceptance is incomplete. A separate docs
  checkpoint follows in Git history.
- Branch: `fix/image-string-filesystem-fallback`
- Starting commit: `61034a7562e7fa43df076dcc38bcd48128605395`
- Host: Apple Silicon macOS (`Darwin arm64`)
- Added runtime-created PNG/JPEG checks to the existing
  `EncodedImageSourceSmokeApp`; its `compileEncodedImageSourceSmoke` task stages
  only the app class and existing TCZ test resources, so generated image files
  are outside the package. The deployed task is
  `runEncodedImageSourceSmokeMacOS`.
- The smoke compares both constructors' decoded dimensions and pixels and closes
  the `File` streams.
- A first deploy stopped because this legacy task did not stage the native
  Launcher; fixed it with the shared macOS image-smoke staging helper. That
  attempt did not execute the app and is not baseline evidence.
- Earlier pre-fix SDK/VM builds were identity-coherent, but the fixture and
  staging task changed afterward; rebuild both before any baseline claim.
- The first deployed process stopped with
  `ClassNotFoundException:EncodedImageSourceSmokeApp$FilesystemResult` because
  the staging task included only the outer class. The include now stages nested
  classes; this run is not baseline evidence.
- The next native run passed artifact identity and confirmed the runtime names
  were absent from the app JAR, but an NPE interrupted fixture generation
  before either constructor result was recorded. The fixture now records its
  last phase and continues both format cases so the failure can be localized.
- Milestone A is now complete. On the actual pre-fix VM, both runtime-created
  files loaded through `Image(File)` and failed through `Image(String)` with
  `ImageException`; `runtimeFixturesNotPackaged=true`. All four artifacts
  shared build ID `0104442b…` and source tree
  `7d6c8f5e1878316c07003ffb4d7638f61aa9389d`.
- `generateNativeBridgeSources` passed. Focused converter and image-source tests
  passed (18 tests, 0 failures/errors/skips), including boolean return mapping,
  missing-file fallback, and unavailable simulator initialization.
- `validateDeployedJavaApi` passed for the deployable SDK changes.
- Expanded `EncodedImageSourceSmokeApp` coverage passes on JavaSE and the
  deployed macOS arm64 VM: TCZ-only PNG/JPEG bag capture, TCZ precedence,
  invalid/empty TCZ no-fallback, filesystem-only PNG/JPEG pixel parity,
  missing/invalid files, absolute paths, safe 255/256/300-character paths,
  repeated native bag release, and runtime-only fixture checks. macOS reports
  the `device/` alias unsupported because its app sandbox is read-only there.
- A macOS ARM64 ASan build (`-fsanitize=address`) and the deployed smoke both
  passed. The smoke emitted `overallPass=true` without an AddressSanitizer
  report; SDK JAR, TCZ, dylib, and Launcher identity passed with build ID
  `50e78dce74ec094634ecca35ef65c62e5d87b6a3c58b8159977deb115e4f14e9` and
  tree `ae0595a4d09b64b4c93569451df33b795cecdb80`.
- The local SDK JAR, deployed TCZ, native dylib, and Launcher passed
  `scripts/provenance/artifact-identity.py` with build ID
  `213b5a927ee5baeef0dc0f6ff05960b9aabeb9baae9abe19c79c1e41e2504449` and
  source tree `cbc6792d5e1dfedb88ab1c6415e3e166d4b8e961`.
- Ruby YAML parsing and focused copyright-header validation passed.
- Added the opt-in smoke input to `build.yml` and the existing manually
  dispatchable `merge.yml`. The smoke run skips macOS/iOS and non-x64 Linux,
  runs Windows x64, Android API 35 emulator, and Linux x64 deployed functional
  checks, verifies SDK/VM provenance, and uploads result logs. The user
  authorized pushing the feature branch and dispatching this flow.
- GitHub requires a manual-dispatch workflow to exist on the default branch;
  use the existing Merge Flow, whose default-branch trigger is already active,
  to dispatch the feature branch with `image-string-smoke=true`.
- The first authorized Merge Flow run (`44599193944`, commit `d0bf6ab31`) built
  the SDK, Linux VM, Android VM, and Windows VM, but all three smoke lanes
  exposed harness setup problems. Linux and Windows launchers resolve their VM
  library from the current working directory, so launch from each deployed
  folder. `setup-android` also tried to install the removed SDK Manager
  `tools` package; the canonical Android build's `packages: ''` setting avoids
  that request.
- Corrected the deployed working directories and Android SDK setup in
  `11eced133` (`fix(ci): run smoke apps from deployed folders`), pushed it, and
  manually dispatched run `44600571238` on that commit. All three smoke lanes
  reached the app stage but exposed further harness issues: the Linux runner
  lacks runtime `libEGL.so.1`/`libGLESv2.so.2`; Android rejects a command-line
  application ID for applications; and the Windows GUI-subsystem launcher
  does not expose stdout for the fixture result (confirmed from the PE
  subsystem metadata). The smoke returns its assertion result as its process
  exit code. The current correction installs EGL/GLES, assigns the Android ID
  in the smoke class initializer, and records/checks the Windows exit code.
- `jarEncodedImageSourceSmoke` passed with the Android app ID initializer.
  Current workflow YAML and 50 Bash blocks parsed and passed `bash -n`; the
  changed smoke source/workflow headers and `git diff --check` passed. A local
  PowerShell parser is unavailable; the updated Windows block was reviewed and
  its execution status will be verified by the next remote run.
- Committed these corrections in `2aa3c08b8` (`fix(ci): prepare platform
  smoke environments`), pushed it, and dispatched run `44602636798` on that
  commit. Windows passed with process exit code 0. Linux still aborts with
  `*** buffer overflow detected ***` (exit 134) after loading the VM. The
  Android emulator booted in 566 seconds, but the smoke script failed because
  the action executed it through `/usr/bin/sh`, which rejects `pipefail`.
- Added flushed smoke phase markers for the Linux abort diagnosis and changed
  the Android runner script from Bash-only `set -euo pipefail` to POSIX
  `set -eu`. The Linux markers and Android shell fix are pending the next
  workflow run.
- Committed the diagnostics and Android shell fix in `8705970bd` (`fix(ci):
  trace linux smoke and use posix android shell`). The app packaging,
  workflow YAML, all 50 Bash blocks, POSIX Android script syntax, copyright
  headers, and diff checks passed. Pushed it and dispatched run `44606045633`.
- In run `44606045633`, Windows passed again, while Linux aborted with exit
  134 and `*** buffer overflow detected ***`; the `Vm.debug` phase markers
  were absent from stderr. Android booted in 587 seconds, then failed before
  launching the app because `adb install` received an empty APK path. The
  action's script parser splits the multiline input into independent shell
  processes, so variables set on one line do not survive to the next.
- The diagnostic switches phase markers to flushed
  `System.err` output and adds a failure-only GDB backtrace step plus artifact.
  These changes were committed and pushed in `85e812d57` (`fix(ci): trace
  linux image smoke failure`). Workflow YAML, the new Bash diagnostic block,
  Android POSIX script syntax, copyright headers, diff checks, and commit
  message validation passed. The new Android runner script is created during
  deploy and invoked as one action-script line, keeping variables in one shell.
  Committed the single-shell Android harness and APK preflight in `5afd73e06`
  (`fix(ci): keep android smoke in one shell`), pushed it, and dispatched run
  `44609764897` with the Linux diagnostics. Windows passed. Linux aborted
  before app startup: GDB shows `__realpath_chk` called from `startVM` before
  `executeProgram`, and the Android emulator job remains active. Artifact
  `14571039635` has the first backtrace. Commit `78ee90d79` now breaks on
  `__realpath_chk`, prints its input path and output capacity, and captures a
  full backtrace. The commit was pushed and dispatched as run `44611514188`
  while Android in run `44609764897` remained active. In that run, Windows
  passed; Linux GDB captured `realpath` input `./libtcvm.so` and output capacity
  `1024` before SIGABRT in `startVM`. Android runs `44609764897` and
  `44611514188` both timed out waiting for the emulator at the action's 600-
  second default, before launching the app. Run `44611514188` passed Windows and
  reproduced the pre-fix Linux failure. Run `44613588440` is now building with
  the realpath fix and a 900-second Android boot timeout.
- glibc `__realpath_chk` requires a destination at least `PATH_MAX` bytes;
  `MAX_PATHNAME` is 1024 on Linux while the runner's `PATH_MAX` is 4096. Replaced
  the fixed stack output with `realpath(path, NULL)` and freed the allocated
  result after copying. The focused CMake build of `tcvm` and `Launcher` passed,
  as did `runEncodedImageSourceSmokeMacOS` with those fresh native artifacts.
  Copyright-header validation passed. Committed and pushed the native fix in
  `153f9fae5` (`fix(vm): allocate resolved paths safely`). The workflow now
  gives Android 900 seconds after run `44609764897` timed out at the action's
  600-second default. Commit `121bec382` (`fix(ci): allow slow android emulator
  startup`) is pushed, and run `44613588440` is active on that revision. The
  sixth run's Android emulator step had not yet concluded at this checkpoint;
  it later timed out at 600 seconds before app launch.
- Run `44613588440` passed Windows. Linux now starts the VM without the
  `__realpath_chk` abort, but the app exits during the TCZ image parity checks
  with `IllegalStateException:Could_not_materialize_encoded_image`; libpng emits
  an ADLER32 warning immediately before exit. Android job `135877939298` timed
  out before app launch after 900 seconds; its artifact has only `deploy.log`.
  The run's Linux artifacts are in `/tmp/image-string-linux-run-44613588440/`.
  Added smoke-only phase markers around TCZ path/byte decoding and inclusion of
  nested exception causes. `jarEncodedImageSourceSmoke` passed with these
  diagnostics; diagnostic packaging log:
  `/tmp/image-string-smoke-diagnostics-build.log`.
- Committed and pushed the diagnostic harness/state/evidence as
  `211d5d20c` (`fix(ci): trace TCZ image decode failures`). Manual Merge Flow
  run `44615780340` passed Windows but Linux failed during
  `tcz-parity-pixels-path-start=image-abi/tiny.png`; `Image(path)` creation
  succeeded, then `getPixels()` failed with
  `Image$DeterministicImageDecodeException:Could_not_decode_encoded_image` and
  an ADLER32 warning. Artifacts are in
  `/tmp/image-string-linux-run-44615780340/`. Android job `135885942718` is
  active. The smoke now decodes the matching `Image(byte[])` first to separate
  input-data/decoder behavior from TCZ path-capture behavior. The updated
  `jarEncodedImageSourceSmoke` and copyright/diff checks passed; build log:
  `/tmp/image-string-smoke-byte-first-build.log`.
- The byte-first diagnostics were committed and pushed as `f25870798`
  (`fix(ci): trace byte-first image decode`). Manual Merge Flow run
  `44617483756` is active on that commit. Run `44615780340` still has Android
  job `135885942718` in progress.
- Run `44617483756` passed Windows. Linux then failed at
  `tcz-parity-pixels-bytes-start=image-abi/tiny.png`, before `Image(path)` was
  decoded, with the same deterministic decode error and ADLER32 warning. This
  places the problem in the common native-bag/direct decode path, not solely
  TCZ path capture. Artifact: `/tmp/image-string-linux-run-44617483756/`.
  Added fallback to canonical non-direct decode after a deterministic direct
  decode failure. Focused `ImageLazyMaterializationTest` and
  `jarEncodedImageSourceSmoke` passed (4 tasks executed, 6 up-to-date); deployed
  macOS smoke passed. Summaries are in
  `TotalCrossSDK/agent-logs/20261119-183224-test-agent.log` and
  `TotalCrossSDK/agent-logs/20261119-183349-runEncodedImageSourceSmokeMacOS-agent.log`.
- Committed and pushed the canonical decode retry as `231dd329b`
  (`fix(sdk): retry canonical decode after direct failure`). Manual Merge Flow
  run `44620259847` is active on this revision. Android run `44618392682` is
  the first run with the TCG environment opt-in and its emulator job remains
  active.
- Run `44620259847` passed Windows; Linux still failed on the byte-backed PNG.
  Both direct and canonical decode emitted `ADLER32 checksum mismatch`, so the
  fallback did not solve the issue. Linux artifact:
  `/tmp/image-string-linux-run-44620259847/`. Added FNV-1a logging for the
  `Vm.getFile` byte array before native capture; focused test and smoke
  packaging passed (`TotalCrossSDK/agent-logs/20261119-184100-test-agent.log`).
  The source fixture is 136 bytes with FNV-1a `cdf05a32`. The TCG-enabled
  Android job `135894837011` remains active after the 900-second boot limit.
- FNV-1a diagnostic was committed/pushed as `d94573a46` (`fix(ci): report TCZ
  image byte checksum`) after `jarEncodedImageSourceSmoke` and the focused image
  test passed. The source fixture checksum is `cdf05a32`; manual run
  `44621855184` is active on this revision. Android TCG-enabled job
  `135894837011` has not completed yet, more than 900 seconds after its action
  step began.
- The Android job log for run `44613588440` shows the emulator exited
  immediately: software CPU emulation with `-accel off` now requires
  `ANDROID_I_WANT_MY_TCG=yes`. The action's boot loop retried until 900 seconds.
  Added that environment variable to the emulator action in
  `b89d2db8f` (`fix(ci): enable Android TCG software emulation`). Workflow YAML
  parsing, header validation, and diff checks passed. Pushed and manually
  dispatched run `44618392682`; its checkout is active. Official emulator
  command-line docs confirm the required TCG opt-in.
- Committed the production fix (`550f2648c`), deployed regression harness
  (`fb7f49589`), cross-platform jobs (`4df085322`), and manual trigger routing
  (`069edff77`). The execution record is in `39ff4c8aa` and this checkpoint.
  Each commit passed the repository message validator; its staged diff was
  reviewed and checked.
- Final workflow validation parsed both YAML files and checked syntax for all
  55 Bash `run` blocks. The two workflow copyright headers passed validation.
- Evidence: `.agent/evidence/fix-image-string-filesystem-fallback.jsonl`

## Active paths

- `.agent/plans/fix-image-string-filesystem-fallback-execplan.md`
- `TotalCrossSDK/src/main/java/totalcross/ui/image/EncodedImageSource.java`
- `TotalCrossSDK/src/test/java/tc/tools/converter/EncodedImageSourceConverterTest.java`
- `TotalCrossSDK/src/test/java/totalcross/ui/image/`
- `TotalCrossVM/src/nm/ui/image_Image.c`
- `TotalCrossSDK/build.gradle` and any focused smoke fixture/build task
- `TotalCrossVM/src/init/posix/startup_c.h`
- `.github/workflows/` for the existing CI smoke lanes

## Next concrete action

Fix the smoke app's unsupported `Long.toHexString(long)` use identified in
run `44621855184`; its deploy failed on Linux, Android, and Windows before the
checksum could be measured. Then investigate why the TCG-enabled Android 15 run
`44618392682` booted at 898,991 ms but emitted no fixture result. Continue from
the detailed steps in `.agent/state/fix-image-string-filesystem-fallback-handoff.md`.

## Preservation boundary

The original checkout at `/Users/flsobral/repos/totalcross-github` contains
unrelated untracked files. Work only in this worktree; never stage or clean
those files. The user authorized a feature-branch push and manual workflow
dispatch; PR creation, merge, rebase, and force-push remain unauthorized.

## Validation and deferrals

- Pre-fix macOS arm64 deployed regression passed as an expected failure; see
  `.agent/evidence/fix-image-string-filesystem-fallback.jsonl`.
- Corrected JavaSE and deployed macOS arm64 checks passed.
- Corrected deployed macOS arm64 AddressSanitizer smoke passed.
- Windows x64 functional validation passed repeatedly on earlier revisions,
  including runs `44613588440` and `44615780340`. The latest run
  `44621855184` failed deployment on all three platform lanes because its new
  checksum diagnostic calls unsupported `Long.toHexString(long)`. Linux's
  `realpath` startup abort is fixed and verified in CI; its PNG decode still
  fails with ADLER32 mismatch, and the checksum comparison remains unobserved.
- Android runs without TCG opt-in timed out before launch. Run `44618392682`
  enabled TCG and booted API 35 after 898,991 ms; APK deployment succeeded, but
  no smoke assertion line was emitted and the workflow failed. Run
  `44620259847` was cancelled at the user's request while Android was active.
  All workflows are now finished. iOS is out of scope.
- iOS build and functional validation are out of scope.
