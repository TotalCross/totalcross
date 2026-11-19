<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Active state: Image(String) filesystem fallback

## Active slice

Local implementation, regression coverage, workflow preparation, validation,
and commits are complete. The user authorized pushing this branch and manually
dispatching the existing Merge Flow with the smoke input. The remaining gate is
the Android, Windows x64, and Linux x64 GitHub Actions results.

## Last checkpoint

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
  were absent from stderr. The Android 15 emulator smoke is still running.
- The diagnostic switches phase markers to flushed
  `System.err` output and adds a failure-only GDB backtrace step plus artifact.
  These changes were committed and pushed in `85e812d57` (`fix(ci): trace
  linux image smoke failure`). Workflow YAML, the new Bash diagnostic block,
  Android POSIX script syntax, copyright headers, diff checks, and commit
  message validation passed. After the active Android step finishes, dispatch
  the next run with the diagnostics.
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
- `.github/workflows/` only if no existing CI smoke fits

## Next concrete action

Wait for the Android job in run `44606045633`, then inspect its result and the
Linux smoke evidence. Dispatch another run with commit `85e812d57` to localize
the Linux abort. Keep the plan open until Android, Windows, and Linux all pass.

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
- Windows x64 functional validation passed twice. Linux x64 still aborts during
  the deployed smoke; the latest run has GDB diagnostics pending. Android API
  35 is active in the fourth run. iOS remains out of scope.
- iOS build and functional validation are out of scope.
