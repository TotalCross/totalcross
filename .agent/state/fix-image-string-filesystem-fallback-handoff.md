<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image(String) filesystem fallback: resumable handoff

> **Status: checkpointed after a 2026-11-25 resume.** The code corrections
> below are committed and pushed. Cross-platform acceptance is still
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

## 2026-11-25 resume result

The user resumed this plan with a hard 60-minute budget beginning at
2026-11-25 19:22:44 UTC (deadline 20:22:44 UTC). The single authorized CI run
completed on source commit `6ccab1de50214fac44f2642960762edb1c867444`:

- **Windows x64 passed** with exit code 0. The GUI launcher does not expose
  fixture stdout, so process exit carries `overallPass`. SDK and VM source tree
  `07b128cde6159fa7485b90962835d4c7958c1b2f` and build ID
  `7005145217837a416f905a5e578e9bc71af0d41b8600dda34916a0bd70316e5c` match.
- **Linux x64 failed** during PNG decoding. The runtime `image-abi/tiny.png`
  bytes were 136 bytes with expected FNV-1a `cdf05a32`, but `Image(byte[])`
  pixel decoding failed with libpng ADLER32 mismatch. A runtime-created PNG
  also failed through `Image(File)` before `Image(String)` parity; JPEG parity,
  TCZ JPEG, path lengths, and native bag lifetime passed. This is a Linux
  decode/runtime or fixture-generation issue, not evidence against the path
  fallback; do not change decoders without a new discriminator.
- **Android API 35 did not launch.** After applying the copied KVM udev rule,
  `/dev/kvm` existed but the runner could not read and write it. The job stopped
  before APK deploy and the emulator action. No Android smoke artifact exists;
  do not fall back to TCG or software emulation.

Run: [45688281871](https://github.com/TotalCross/totalcross/actions/runs/45688281871).
Linux job/artifact: `139468036549` / `15057173135` at
`/tmp/luna-linux-run-45688281871/`. Windows job/artifact:
`139468401567` / `15057591393` at
`/tmp/luna-windows-run-45688281871/`. Android job `139468241145` failed at the
KVM preflight, before creating its lane artifact or running the deploy-time
provenance check. Linux and Windows verified matching SDK/VM artifacts from
the same source commit. No workflow is active.

Three code commits were pushed: `43298175a` reverts only the decoder retry
from `231dd329b`; `d3d6af5ef` replaces `Long.toHexString`, retains the expected
checksum, and reports filesystem PNG/JPEG parity before isolated TCZ checks;
`6ccab1de5` adds KVM validation and manifest-derived Android activity launch.
The existing Linux `realpath` startup fix remains as a separately justified
integration change. iOS remains out of scope.

Focused SDK tests, bridge generation, deployed API validation, smoke JAR
packaging, YAML/shell checks, mock Android pass/failure preflight, headers, and
diff checks passed. The local `tc.Deploy -macos` invocation wrote the TCZ but
could not package it because `macos/Launcher` was absent; no new local deployed
runtime result is claimed. Prior macOS ARM64/ASan evidence remains applicable
to native code, which was not changed in this timebox. Exact commands and logs
are in the append-only evidence file.

Acceptance is incomplete: Android is unverified and Linux PNG decode fails.
The next actionable step is to select a hosted runner that grants the job
read/write access to `/dev/kvm`, then confirm the KVM preflight before spending
time on another emulator startup. Preserve the one-run-per-resume policy and
do not dispatch another matrix until that infrastructure gate is fixed.

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

1. Select a hosted runner that gives the Android job read/write access to
   `/dev/kvm`, then confirm the existing KVM preflight on that runner before
   scheduling another emulator. Keep TCG/software acceleration disabled.
2. After the Android infrastructure gate is resolved, continue the Linux PNG
   decode investigation from the matching `cdf05a32` runtime checksum and the
   failure shared by `Image(byte[])` and `Image(File)`. Do not change decoders
   without a discriminator showing a decoder defect.
3. Acceptance requires same-commit Android API 35, Windows x64, and Linux x64
   deployed functional smokes. Windows passed on `6ccab1de5`; Android is
   unverified and Linux PNG decode failed in run `45688281871`. iOS remains
   untested and out of scope.

At the handoff point, no workflow is running and no further functional tests
were started after the user asked to stop. Only the final report/state/evidence
documentation checks and its commit/push remain in this pause operation.
