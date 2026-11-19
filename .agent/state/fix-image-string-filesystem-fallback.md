<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Active state: Image(String) filesystem fallback

## Active slice

Local implementation, regression coverage, workflow preparation, validation,
and commits are complete. The remaining gate is Android, Windows x64, and Linux
x64 GitHub Actions after the user authorizes pushing this branch and dispatching
the manual workflow.

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
- The local SDK JAR, deployed TCZ, native dylib, and Launcher passed
  `scripts/provenance/artifact-identity.py` with build ID
  `213b5a927ee5baeef0dc0f6ff05960b9aabeb9baae9abe19c79c1e41e2504449` and
  source tree `cbc6792d5e1dfedb88ab1c6415e3e166d4b8e961`.
- Ruby YAML parsing and focused copyright-header validation passed.
- Added `.github/workflows/image-string-path-smoke.yml` and an opt-in input to
  `build.yml`. The manual workflow skips macOS/iOS and non-x64 Linux, runs
  Windows x64, Android API 35 emulator, and Linux x64 deployed functional
  smokes, checks SDK/VM build provenance, and uploads result logs. It has not
  been dispatched because remote publication is not authorized.
- Committed the work in three focused local commits:
  `550f2648c` (production fix), `fb7f49589` (deployed regression harness), and
  `4df085322` (manual cross-platform workflow). Each commit passed the
  repository message validator; its staged diff was reviewed and checked.
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

Obtain explicit user authorization to push `fix/image-string-filesystem-fallback`
and dispatch `.github/workflows/image-string-path-smoke.yml`; then inspect all
three platform run results and update the acceptance record. Until that
authorization arrives, keep the branch local and report remote CI as pending.

## Preservation boundary

The original checkout at `/Users/flsobral/repos/totalcross-github` contains
unrelated untracked files. Work only in this worktree; never stage or clean
those files. No remote push, PR, merge, rebase, or force-push is authorized by
the plan.

## Validation and deferrals

- Pre-fix macOS arm64 deployed regression passed as an expected failure; see
  `.agent/evidence/fix-image-string-filesystem-fallback.jsonl`.
- Corrected JavaSE and deployed macOS arm64 checks passed.
- Android, Windows, and Linux functional validation belongs in GitHub Actions;
  the manual workflow is prepared but has not run.
- iOS build and functional validation are out of scope.
