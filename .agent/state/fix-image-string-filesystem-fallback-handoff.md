<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image(String) filesystem fallback: resumable handoff

> **Status: paused at the user's request on 2026-11-19.** The code and local
> validations are committed and pushed. Cross-platform acceptance is still
> incomplete. This is a handoff checkpoint, not a claim that the task passed.

## Objective and source of truth

Implement the attached plan to make deployed `new Image(String)` load a
filesystem image while preserving TCZ-first lookup, native-bag behavior,
non-fallback errors for a found but invalid TCZ entry, path safety, and
`Image(File)` semantics. The source document is
`/Users/flsobral/Downloads/luna-image-string-correction-plan-v4.md`; the
repository's full scoped requirements and acceptance criteria are copied to
`.agent/plans/fix-image-string-filesystem-fallback-execplan.md`.

The attached plan is the user's requested work. This file records execution
facts and the current resume point; it does not override the design or broaden
the acceptance criteria.

## Checkout and remote state

- Repository: `TotalCross/totalcross`
- Persistent worktree: `/Users/flsobral/repos/totalcross-image-string-filesystem-fallback`
- Branch: `fix/image-string-filesystem-fallback`
- Last implementation commit before this handoff: `6691edc387a36313474e847dec10a88470df2404`
- At handoff start, local branch and `origin/fix/image-string-filesystem-fallback`
  both pointed to that commit. This handoff is the next documentation commit;
  the branch history records its final commit SHA.
- The user explicitly authorized push and manual GitHub Actions workflow runs.
  No PR, merge, rebase, or force-push was created or performed.
- All workflows are now finished. Run `44620259847` was cancelled at the user's
  request to stop the long activity; its Windows lane had passed, Linux had
  already failed, and its still-running Android lane was cancelled. The newest
  run `44621855184` and TCG run `44618392682` completed with failures.

## Resume protocol

For a new agent continuing this work:

1. Read this handoff first.
2. Read `.agent/state/fix-image-string-filesystem-fallback.md` for the longer
   incremental history and `.agent/plans/fix-image-string-filesystem-fallback-execplan.md`
   for the fixed design and acceptance criteria.
3. Treat `.agent/evidence/fix-image-string-filesystem-fallback.jsonl` as the
   append-only record of individual validations and CI dispatches. Search it
   for a milestone rather than rewriting old evidence.
4. Check the worktree and remote branch before editing. Preserve the existing
   commits and any local artifacts.
5. Continue at **Next actions** below. The checksum comparison has not happened.

## Work completed

The implementation commits are present on the branch:

- `550f2648c` — add filesystem fallback in `EncodedImageSource.fromPath` while
  keeping native TCZ capture first and returning a boolean from the bridge.
- `fb7f49589` — add the runtime-created image regression smoke app and focused
  regression coverage.
- `4df085322` and `069edff77` — add the scoped platform lanes and route the
  opt-in through the existing manually dispatchable Merge Flow.
- `153f9fae5` — fix Linux `startVM` path resolution using `realpath(path, NULL)`;
  this was a separate native startup defect exposed before the Linux smoke
  could reach the image test.
- `211d5d20c`, `f25870798`, and `d94573a46` — add phase/cause, byte-first, and
  FNV-1a diagnostics to locate Linux PNG data corruption.
- `b89d2db8f` — set `ANDROID_I_WANT_MY_TCG=yes` for the hosted Android
  emulator.
- `231dd329b` — retry canonical decode after a deterministic direct-decode
  failure. Local focused tests passed, but this did not resolve the Linux CI
  failure.
- Additional commits fix workflow working directories, SDK setup, Android app
  identity and shell handling, Linux diagnostics, emulator timeout, and
  evidence records. The full ordered commit list is available with
  `git log --oneline --reverse 61034a7562e7fa43df076dcc38bcd48128605395..HEAD`.

The implementation and regression touch `EncodedImageSource.java`,
`Image.java`, `image_Image.c`, `startup_c.h`, converter/image tests, the smoke
app and its runtime-only fixtures, SDK smoke tasks, and the existing
`.github/workflows/build.yml` and `merge.yml`. The exact changed-file summary
is in the commit history and active plan.

### Scope items requiring review

- `Image.java` now retries a canonical decoder after deterministic failure.
  This was added after Linux direct decode failed, and local tests passed, but
  it did not solve the Linux CI failure. The attached plan says not to change
  decoders. Re-evaluate this change against that explicit constraint before
  claiming plan compliance; do not assume the retry is the root-cause fix.
- `startup_c.h` changes Linux `realpath` allocation behavior. It fixes a real
  glibc buffer-size abort that blocked the smoke, but it is outside the narrow
  image-path behavior. Decide whether to keep it as a separately justified fix
  or move it out of the eventual image-path change. Do not silently discard it.
- iOS was intentionally not built or functionally tested, per the attached
  plan.

## Verified results

### Pre-fix reproduction

On Apple Silicon macOS ARM64, runtime-created PNG and JPEG fixtures outside the
TCZ were run against the actual pre-fix deployed VM. `Image(File)` loaded both;
`Image(String)` failed for both with `ImageException`. The SDK JAR, app TCZ,
native dylib, and Launcher matched build ID `0104442b…` and source tree
`7d6c8f5e1878316c07003ffb4d7638f61aa9389d`. This is real before-state
evidence, not a simulator inference.

### Local validation

- `generateNativeBridgeSources` passed; converter and image-source tests passed
  (18 tests, no failures/errors/skips), including boolean bridge mapping,
  missing path behavior, and unavailable JavaSE launcher behavior.
- `validateDeployedJavaApi` passed.
- Targeted `ImageLazyMaterializationTest` and
  `jarEncodedImageSourceSmoke` passed after the fallback and again after adding
  the checksum diagnostic. The latest Gradle agent summary is
  `TotalCrossSDK/agent-logs/20261119-184100-test-agent.log`; wrapper output is
  `/tmp/image-string-checksum-diagnostic-build.log`.
- JavaSE smoke and deployed macOS ARM64 smoke passed. The deployed smoke covers
  runtime-only PNG/JPEG, TCZ-only PNG/JPEG, TCZ precedence, empty/invalid TCZ
  non-fallback, missing/invalid files, supported path forms and lengths,
  repeated native-bag release, pixel parity, and stream closure. macOS reports
  the `device/` alias unsupported in its read-only app sandbox.
- A focused macOS ARM64 ASan VM build and deployed smoke passed with
  `overallPass=true` and no AddressSanitizer report. Its artifact identity was
  checked (build ID `50e78dce…`, source tree `ae0595a4…`).
- The Linux native smoke build (`tcvm`, `Launcher`) and deployed macOS smoke
  passed after the `realpath` fix. Copyright/header checks, focused YAML
  parsing, Bash syntax checks, staged-diff checks, and commit-message checks
  passed at the recorded checkpoints.

### Cross-platform functional status

Acceptance remains **unmet**. Several earlier revisions passed the Windows
smoke, but no single final workflow passed Android, Windows, and Linux on the
same corrected implementation. The latest checksum-instrumented source did
not deploy on any of the three lanes, so its FNV-1a comparison never ran.

## CI investigation chronology

Every workflow below is the existing Merge Flow with the opt-in image smoke.
The branch was pushed and each run was manually dispatched under the user's
authorization. “Windows passed” applies only to that run's deployed smoke; it
does not imply the whole workflow passed.

| Run | Outcome and finding |
| --- | --- |
| [44599193944](https://github.com/TotalCross/totalcross/actions/runs/44599193944) | Initial smoke exposed harness setup issues: launchers require the deployed folder as working directory, and Android setup must not request the removed SDK Manager `tools` package. |
| [44600571238](https://github.com/TotalCross/totalcross/actions/runs/44600571238) | All lanes reached deployment. Windows failed to expose GUI-launcher stdout; Linux lacked EGL/GLES libraries; Android rejected the command-line app ID. |
| [44602636798](https://github.com/TotalCross/totalcross/actions/runs/44602636798) | Windows passed. Linux aborted with glibc buffer-overflow (134). Android shell rejected Bash `pipefail`. |
| [44606045633](https://github.com/TotalCross/totalcross/actions/runs/44606045633) | Windows passed; Linux still aborted before flushed Java phases. Android `adb install` received an empty APK path because action script lines used separate shells. |
| [44609764897](https://github.com/TotalCross/totalcross/actions/runs/44609764897) | Windows passed. GDB localized Linux abort to `__realpath_chk` in `startVM`; Android remained in emulator boot. |
| [44611514188](https://github.com/TotalCross/totalcross/actions/runs/44611514188) | Windows passed. GDB captured `realpath("./libtcvm.so", ..., 1024)` before SIGABRT. Android hit the action's 600-second boot timeout. |
| [44613588440](https://github.com/TotalCross/totalcross/actions/runs/44613588440) | Windows passed. Linux now started the VM but failed PNG materialization with libpng `IDAT: ADLER32 checksum mismatch`. Android timed out after the configured 900 seconds before app launch; it lacked TCG opt-in. |
| [44615780340](https://github.com/TotalCross/totalcross/actions/runs/44615780340) | Windows passed. Linux created `Image(path)`, then `getPixels()` failed at `tcz-parity-pixels-path-start` with `DeterministicImageDecodeException` and ADLER32 mismatch. |
| [44617483756](https://github.com/TotalCross/totalcross/actions/runs/44617483756) | Windows passed. Linux failed on the matching `Image(byte[])` before `Image(path)` decode, also with ADLER32 mismatch. This rules out only the TCZ path-capture code as the sole trigger; it does not yet distinguish bad bytes from decoder/runtime behavior. Android was an old no-TCG run. |
| [44618392682](https://github.com/TotalCross/totalcross/actions/runs/44618392682) | Windows passed and Linux still failed decode. TCG opt-in let API 35 boot in 898,991 ms, just under the 900-second limit; APK deploy succeeded. The Android action later failed because it found no smoke result. Artifact `14577127318` records `monkey` exit code 0 but no `fixture=... overallPass=...` line. Logcat also records dexopt failure `String longer than indicated size 16` and emulator/service ANRs/timeouts. Investigate whether the app launched and why no assertion line was emitted; the log does not yet establish a single root cause. |
| [44620259847](https://github.com/TotalCross/totalcross/actions/runs/44620259847) | Windows passed. Linux direct and canonical decode both emitted ADLER32 checksum mismatch. Android was still running when the user asked to stop and was cancelled; final workflow status is `cancelled`. Linux artifact was downloaded to `/tmp/image-string-linux-run-44620259847/`. |
| [44621855184](https://github.com/TotalCross/totalcross/actions/runs/44621855184) | SDK build and smoke-app packaging passed. All platform deploy steps failed before running the smoke: the new diagnostic invokes `java/lang/Long.toHexString(long)`, unavailable to TotalCross device code. Linux and Android artifacts contain only `deploy.log`; Windows includes `deploy.log` and no smoke execution result. The expected fixture hash comparison `cdf05a32` was not obtained. |

### Corrections made in response to CI

1. Set deployed working directories, removed obsolete Android SDK package
   request, installed Linux EGL/GLES runtime, assigned Android application ID
   in the smoke app, and checked the Windows GUI process exit code.
2. Added Linux phase markers and GDB backtrace, changed Android shell content
   to POSIX syntax, then moved the Android flow into one script because action
   multiline commands did not share variables.
3. Used GDB to identify the fixed `MAX_PATHNAME=1024` destination passed to
   glibc `realpath` on a Linux runner with `PATH_MAX=4096`; changed the helper to
   allocate the resolved path. This removed the startup abort.
4. Raised Android emulator boot timeout from 600 to 900 seconds. After the
   runner log showed `-accel off` requires `ANDROID_I_WANT_MY_TCG=yes`, added
   that opt-in. The next TCG run finally booted at almost the full timeout but
   did not produce an app smoke result.
5. Added phase and nested-cause diagnostics, then a byte-first comparison. The
   Linux error moved from path decode to byte decode, both with the same ADLER32
   mismatch. Added canonical decode retry; it passed local focused tests but
   failed the same Linux case.
6. Added FNV-1a logging to compare runtime TCZ bytes against the fixture
   (136 bytes, FNV-1a `cdf05a32`, SHA-256
   `958903b3f880fbf626fa8fbba8f69856a989f5930531984be9585aac08a5aba6`). The
   device-incompatible `Long.toHexString` call in this last diagnostic now
   blocks platform deployment and must be fixed before that measurement is
   useful.

## Artifacts and logs

Local CI artifacts downloaded for analysis:

- Linux runs: `/tmp/image-string-linux-run-44613588440/`,
  `/tmp/image-string-linux-run-44615780340/`,
  `/tmp/image-string-linux-run-44617483756/`,
  `/tmp/image-string-linux-run-44620259847/`.
- Latest failed deploy logs: `/tmp/image-string-linux-run-44621855184/deploy.log`,
  `/tmp/image-string-android-run-44621855184/deploy.log`, and
  `/tmp/image-string-windows-run-44621855184/deploy.log`.
- TCG Android artifact: `/tmp/image-string-android-run-44618392682/`;
  hosted artifact ID `14577127318`.
- Gradle summaries include `TotalCrossSDK/agent-logs/20261119-183224-test-agent.log`,
  `TotalCrossSDK/agent-logs/20261119-183349-runEncodedImageSourceSmokeMacOS-agent.log`,
  and `TotalCrossSDK/agent-logs/20261119-184100-test-agent.log`.
- Earlier Linux GDB artifact ID `14571039635`; the detailed CI outcomes and
  artifact links are retained in the JSONL evidence and run pages.

`/tmp` downloads are host-local and may be removed by environment cleanup;
GitHub run pages/artifacts and the committed evidence record are the durable
references.

## Next actions

1. Replace `Long.toHexString` in
   `EncodedImageSourceSmokeApp` with a device-supported fixed-width hex
   formatter (or decimal hash), then run the focused smoke packaging task and
   all required deploy checks. A host-only Java test is insufficient to catch
   the converter compatibility issue.
2. Inspect and explain the TCG Android artifact: verify why `monkey` returned
   zero while no fixture result line was captured, and investigate the
   `dex2oat` string-size failure. Emulator boot nearly consumes the full
   900-second allowance, so account for that when choosing the next hosted run.
3. Dispatch the minimal authorized Android/Windows/Linux workflow on the
   repaired branch. For Linux, compare the runtime `Vm.getFile` FNV-1a value
   with `cdf05a32`; only then choose whether the fault is in TCZ bytes or later
   decoding. Do not infer the checksum from the existing failures.
4. Reassess the `Image.java` decoder retry against the attached plan's explicit
   “do not change decoders” constraint, and decide how to handle the separate
   Linux `realpath` fix. Keep or remove neither by assumption; explain the
   choice in the plan and commit history.
5. Continue until Android API 35, Windows x64, and Linux x64 deployed functional
   smokes pass on the same intended implementation. Report iOS as untested, as
   required by the plan.

At the handoff point, no workflow is running and no further functional tests
were started after the user asked to stop. Only the final report/state/evidence
documentation checks and its commit/push remain in this pause operation.
