<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image(String) filesystem fallback: resumable handoff

> **Status: checkpointed after three bounded 2026-11-25 resumes.** The code
> corrections below are committed and pushed. Cross-platform acceptance is still
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

Runner follow-up found no repository-level self-hosted runners (`total_count=0`).
The organization runner-groups API returned HTTP 403 because this session lacks
`admin:org`. The performance-lab reference workflow also runs on
`ubuntu-24.04`; its most recent run passed the udev step but failed emulator
boot, so it does not establish an available KVM-capable runner. No alternate
label is known and no second matrix was dispatched.

The user confirmed that no KVM runner is currently available. Android
acceptance must wait for an owner/organization-level runner change. Linux PNG
decode remains independently unresolved; no decoder change or additional
platform run is authorized in this checkpoint.

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
The next actionable step is to resume after a repository/organization owner
provides a runner with read/write access to `/dev/kvm`. Confirm the KVM
preflight before spending time on another emulator startup. Preserve the
one-run-per-resume policy and do not dispatch another matrix until that
infrastructure gate is fixed.

## Second bounded resume result (2026-11-25)

The user supplied a new 60-minute resume plan at
`/Users/flsobral/.codex/attachments/c8a65559-c48e-4d65-bc32-9c59750c801a/pasted-text-1.txt`.
It explicitly authorized local accelerated Android emulator execution and
prohibited Linux execution on the Mac, Android VM rebuilds, software emulation,
new AVDs/system images, and repeated cross-platform smoke runs. The resume
started at `2026-11-25 20:24:48 UTC` from clean branch/origin revision
`f86ed0473df7dd880677e8a4a4fbe543b8158e27`; no functional source changed.
The user also confirmed that no KVM-capable runner is available; this resume
used only the locally authorized accelerated macOS AVD.

### Android local smoke

Artifacts were downloaded from the finished Merge Flow run
[45688281871](https://github.com/TotalCross/totalcross/actions/runs/45688281871):
`sdk-build` (`15058170965`), `android` (`15056564349`), and
`image-string-smoke-app` (`15057901724`). The Android and SDK provenance match
source commit `6ccab1de50214fac44f2642960762edb1c867444`, tree
`07b128cde6159fa7485b90962835d4c7958c1b2f`, build ID
`7005145217837a416f905a5e578e9bc71af0d41b8600dda34916a0bd70316e5c`, and run
ID `45688281871`. The AAB contains `libtcvm.so` for `arm64-v8a`.

The existing `Pixel_3a_API_34_extension_level_7_arm64-v8a` AVD is the only AVD
defined; there is no API 35 AVD. It uses the installed Google APIs API 34
`arm64-v8a` image and passed the macOS Hypervisor.Framework acceleration check.
The APK declares min SDK 24/target SDK 36 and contains `libtcvm.so` for
`arm64-v8a`, so it matches the existing AVD ABI and minimum API. No AVD data was
wiped and no system image was downloaded.

The workflow's deployment procedure was reproduced from the matching artifacts
with `tc.Deploy EncodedImageSourceSmokeApp.jar -android`; it completed in 12 s
and produced `EncodedImageSourceSmokeApp.apk` (SHA-256
`f81b5775ae7e7116d7e61bc47e2c99fd72ba1abdeff8094b6a2336083a4a0a1d`). `aapt`
derived package `totalcross.appimgsmoke` and activity
`totalcross.appimgsmoke.Loader`. `adb install -r` succeeded in 7.27 s, and
`adb shell am start -W -n totalcross.appimgsmoke/totalcross.appimgsmoke.Loader`
returned exit 0 / `Status: ok` in 1.75 s. The app process then died before
emitting any smoke fixture result; the 60-second result wait expired. The
process failure was:

`ClassNotFoundException: androidx.multidex.MultiDexApplication`

ART also rejected APK `classes.dex` with “String longer than indicated size
16”. The APK contains only that DEX; `dexdump` rejects it, and it lacks the
class its manifest names. The matching AAB's `base/dex/classes.dex` passes
`dexdump` and contains `androidx.multidex.MultiDexApplication`. This isolates
the immediate Android blocker to the APK generated by the established deploy
step, before TotalCross VM startup. It is not an Android functional pass.

Saved Android evidence and logs are under
`/tmp/luna-image-string-resume-20261125/android-deploy/smoke/`; emulator
startup/logs are under `/tmp/luna-image-string-resume-20261125/`. The AVD was
stopped after evidence collection. The emulator run was local macOS evidence,
not GitHub Actions CI.

### Android DEX-width follow-up

Focused inspection of `TotalCrossSDK/src/main/java/tc/tools/deployer/Deployer4Android.java`
found that `processClassesDex` performs an in-place byte-array replacement.
Its comment requires equal package lengths: source `totalcross/android` is 18
bytes, while target `totalcross/appimgsmoke` was 22 bytes. The longer copy
overran the original DEX string, matching the `dexdump`/ART length failure.
Changed only the smoke app's ID to `test` in commit `93f4ef016`; target
`totalcross/apptest` is also 18 bytes. The source and target width check passed.

`./gradlew-agent jarEncodedImageSourceSmoke --quiet` passed in 3 seconds. The
established `tc.Deploy ... -android` command used the same SDK and Android AAB
artifacts from run `45688281871` and produced APK SHA-256
`e4051047278849d329a1e4e60a5d23dcdd39357056fd04b6e869896672e59a51`.
`dexdump -f` passed for its sole 3,344,772-byte `classes.dex` (SHA-256
`a0c00a9f31c395f8a12918f85d10ff24672dfe91ec474ef7d82e6692f746684e`). `aapt`
derived package `totalcross.apptest` and activity
`totalcross.apptest.Loader`; install succeeded and `am start -W` returned
`Status: ok` (11.2 seconds). The existing API 34 ARM64 AVD booted and was
stopped after evidence capture.

The app emitted the fixture line within the 60-second bound, but
`overallPass=false` solely because `absoluteFilesystemPath=false`. Filesystem
PNG/JPEG parity, TCZ PNG/JPEG, TCZ precedence/no-fallback, missing/invalid
files, `device/` path, path-length boundary, runtime fixture isolation, bag
lifetime, and expected `cdf05a32` checksum all passed. The process exited from
the smoke failure; no DEX crash remained. Full line and logs are in the JSONL
record and `/tmp/luna-image-string-resume-20261125/android-fixed-id/`. This
remains local evidence, not CI, and does not meet acceptance.

Static review of that log narrowed the next Android diagnostic. The absolute
case logs `filesystem-start=png` and then `absolute-path-checked=false`, without
`filesystem-written`, `filesystem-file-decoded`, or `filesystem-path-loaded`.
The following `device/` case logs the complete write/read/path parity sequence.
`filesystemRoundTrip` logs its start before constructing/writing and only logs
the write after `File.CREATE_EMPTY` and PNG encoding return; the absolute case
does not expose its `FilesystemResult.error`. This places the failure before a
successful write marker, but does not identify which operation threw. Log the
composed path and phase/error before attributing this to image path decoding.

### Linux PNG investigation

Downloaded the matching `linux-amd64` artifact (`15058040689`) and reused the
existing Linux smoke artifact (`15057173135`) from the same run/provenance. The
136-byte `image-abi/tiny.png` is byte-identical in the SDK archive and smoke
JAR. Independent parsing found valid IHDR/IDAT/IEND chunk CRCs; the 79-byte
IDAT inflates to the expected 5,220 bytes with a valid zlib Adler-32. macOS
`sips` also reads the 36x36 RGBA fixture. This rules out corruption in the
checked-in source fixture; the earlier runtime FNV-1a `cdf05a32` confirms the
TCZ bytes matched its expected value before native decode.

The file-backed stream route and encoded-bag mapped-byte route both call
`pngLoad`, then feed data to `png_process_data` in 512-byte chunks. The file
route uses `readBytes`; the TCZ byte route passes `bag->bytes`/`bag->length` to
the mapped reader. Both fail with libpng's IDAT ADLER32 warning; JPEG succeeds.
The matching Linux VM was built with `TC_RENDERER_SKIA=ON` and links static
`libpng16.a` (libpng 1.6.48) plus static `libz.a` from zlib-ng 2.1.6; neither
PNG nor zlib appears as a dynamic dependency.

Evidence localizes the failure after source-fixture generation, in the shared
native PNG decode stack or its static library pair, but does not distinguish
input handoff/custom allocator/progressive integration from the library pair.
No Linux app was run locally, no decoder was changed, and no CI workflow was
dispatched. The next discriminator is one Linux-only diagnostic: capture the
length/hash immediately before `png_process_data` for the mapped `tiny.png`
input, then decode those same bytes using the same static libpng/zlib-ng pair
with the standard allocator. A matching feed hash plus successful standalone
decode would implicate the TotalCross progressive/custom-allocator path; a
failed standalone decode would implicate the linked pair. Do not run the
three-platform smoke matrix for this diagnosis.

Acceptance remains incomplete. Windows passed on the prior same-source run;
Android did not produce `overallPass=true`, and Linux PNG decoding still fails.
No workflow is active. Exact artifact hashes, command statuses, and append-only
results are in `.agent/evidence/fix-image-string-filesystem-fallback.jsonl`.

## Third bounded continuation result (2026-11-25)

The user requested execution of
`/Users/flsobral/Downloads/luna-image-string-resume-60min.md` and confirmed
that no KVM runner is available. This continuation began at `21:25:31 UTC`
from `ecdcbe1fcc618a95a55e180dcaf4022111dace7a`, with deadline `22:25:31 UTC`.
It retained the existing worktree and branch; Android used the locally
authorized accelerated macOS AVD, while Linux execution remained CI-only.

### Android API 34 local smoke

The smoke's absolute fixture path omitted a separator because
`Settings.appPath` has no trailing slash on this VM. The test now uses
`Convert.appendPath` for deployed and JavaSE fixture construction and records
the composed path and filesystem error. Commit `7d4d9d9b1` was pushed.
`./gradlew-agent jarEncodedImageSourceSmoke --quiet` passed; deployment used
the SDK and Android VM from run `45688281871`, and `dexdump`, APK install, and
manifest-derived `am start -W` passed. On the existing
`Pixel_3a_API_34_extension_level_7_arm64-v8a` AVD (Hypervisor.Framework), the
actual fixture line reports `absoluteFilesystemPath=true` and
`overallPass=true`. This is local API 34 evidence, not API 35 CI. APK, DEX,
smoke JAR identities and logs are in the append-only evidence record and
`/tmp/luna-image-string-resume-20261125/android-absdiag-final/`.

### Linux-only diagnostic run

Commit `3084f55dd` adds an opt-in Merge Flow input that compiles a macro-guarded
probe into only the Linux x64 VM. The probe hashes the exact mapped feed buffer
before progressive decoding and asks the same static libpng/zlib-ng pair to
decode those bytes through the simplified standard-allocator API. Android,
Windows, macOS, and iOS build/smoke lanes are excluded when this input is used.

The sole authorized run was
[45707753798](https://github.com/TotalCross/totalcross/actions/runs/45707753798)
on commit `3084f55dd2db6993fc28dd60d0fa6607c17ce923`. SDK build job
`139534165430` and Linux VM build job `139534388835` passed; the VM compile
commands confirm `-DTC_IMAGE_PNG_DIAGNOSTIC`. Android, Windows, macOS, and iOS
lanes were skipped. The Linux deployed smoke job `139535204309` failed with
exit 139. The first filesystem PNG decode still emitted ADLER32; filesystem
JPEG parity passed. In the later TCZ `Image(byte[])` check, the standard
`png_image_finish_read` cleanup segfaulted through `png_image_free` and
`inflateEnd`/`free`. The probe printed no feed hash because it only logged after
the standard decode returned. Therefore this run does not distinguish mapped
feed bytes from the static decoder pair; do not treat it as Linux acceptance.

Relevant artifacts: Linux smoke `15066072630`, Linux VM `15066960602`, SDK
`15067080574`, smoke JAR `15066526652`. Logs and downloaded artifacts are in
`/tmp/luna-image-string-linux-diag-45707753798/`. No second run was started;
the one-run limit was consumed. No decoder fix is supported by this evidence.

Acceptance is still incomplete: the local API 34 smoke passes, but Android API
35 CI has no available KVM runner; Linux PNG decoding fails and the diagnostic
did not emit its feed hash; Windows did not run on this commit. No workflow is
active. The next Linux discriminator is to flush the feed length/FNV before
calling libpng, persist the bytes, and perform the standard-allocator decode
in an isolated helper process with the same static libraries so its cleanup
crash cannot terminate the primary smoke. Compare the feed FNV with `cdf05a32`.

## Checkout and remote state

- Repository: `TotalCross/totalcross`
- Persistent worktree: `/Users/flsobral/repos/totalcross-image-string-filesystem-fallback`
- Branch: `fix/image-string-filesystem-fallback`
- Latest diagnostic code commit: `3084f55dd2db6993fc28dd60d0fa6607c17ce923`;
  it is pushed to `origin/fix/image-string-filesystem-fallback`. This handoff,
  active state, plan, and evidence index record the current checkpoint.
- The user explicitly authorized push and manual GitHub Actions workflow runs.
  No PR, merge, rebase, or force-push was created or performed.
- All workflows are now finished. Run `45707753798` completed with SDK/Linux
  builds passed and the Linux deployed smoke failed as described above. Earlier
  runs `44620259847`, `44621855184`, and `44618392682` are historical evidence.

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
5. Continue at **Next actions** below. The Linux feed FNV comparison was not
   emitted; one standard-allocator probe crashed in libpng/zlib cleanup.

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
- `7d4d9d9b1` — compose the Android absolute fixture path with
  `Convert.appendPath`; the existing API 34 ARM64 AVD smoke then passed all
  fixture assertions.
- `3084f55dd` — add one opt-in Linux-only mapped-feed/libpng diagnostic; the
  build passed but its smoke crashed in standard decoder cleanup before the
  feed hash was printed.
- `211d5d20c`, `f25870798`, and `d94573a46` — add phase/cause, byte-first, and
  FNV-1a diagnostics to locate Linux PNG data corruption.
- `b89d2db8f` — set `ANDROID_I_WANT_MY_TCG=yes` for the hosted Android
  emulator.
- `231dd329b` — retry canonical decode after a deterministic direct-decode
  failure. Local focused tests passed, but this did not resolve the Linux CI
  failure.
- `43298175a` — revert the ineffective decoder retry because it was outside
  the approved filesystem-fallback scope and did not fix Linux decoding.
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

- The ineffective canonical decoder retry from `231dd329b` was reverted in
  `43298175a` after it failed to resolve Linux decode and was found to be
  outside the approved filesystem-fallback scope.
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

Acceptance remains **unmet**. Local Android API 34 ARM64 smoke passed on the
existing accelerated emulator, but Android API 35 CI is unavailable without a
KVM-capable runner. Windows passed on an earlier same-source run, not on the
current diagnostic commit. The current Linux-only run built successfully but
failed in PNG decoding and did not print its mapped-feed FNV.

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
| [45707753798](https://github.com/TotalCross/totalcross/actions/runs/45707753798) | Linux-only diagnostic commit `3084f55dd`. SDK and Linux VM builds passed; Android, Windows, macOS, and iOS lanes were skipped. The deployed Linux smoke failed with ADLER32 then segfaulted in `png_image_finish_read` cleanup (`inflateEnd`/`free`); the diagnostic had not printed the feed hash. |

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
7. Reproduced the Android absolute-path smoke issue: `Settings.appPath` lacked
   a trailing separator. Commit `7d4d9d9b1` uses `Convert.appendPath`, and the
   existing local API 34 ARM64 AVD emitted `overallPass=true`. The user
   confirmed no KVM runner is available for API 35 CI.
8. Added one Linux-only probe in `3084f55dd`. The runner compiled the macro, but
   `png_image_finish_read` hit ADLER32 and segfaulted while cleaning zlib state,
   before the probe logged the feed FNV. The one-run allowance is consumed;
   isolate the next standard-allocator decode and flush the feed checksum before
   calling libpng.
9. Commit `2faf62d24` moves the diagnostic feed length/FNV print and flush
   before any libpng call. The copyright-header validator and `git diff
   --check` passed. This is static-only evidence: no local Linux execution or
   second CI run occurred, so runtime output remains unverified.

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
- Latest Linux diagnostic run `45707753798`: artifact IDs `15066072630`
  (smoke), `15066960602` (VM), and `15067080574` (SDK); local downloads are in
  `/tmp/luna-image-string-linux-diag-45707753798/`.

`/tmp` downloads are host-local and may be removed by environment cleanup;
GitHub run pages/artifacts and the committed evidence record are the durable
references.

## Next actions

1. The one Linux x64 run
   [45720318038](https://github.com/TotalCross/totalcross/actions/runs/45720318038)
   finished unsuccessfully before baseline SDK construction. It passed exact
   baseline selection, dependency archive/tag checks, and baseline provenance,
   and feature SDK/VM builds passed. `scripts/package-sdk.sh` was called from
   the repository root; its canonical working directory is `scripts/`. The
   deployed comparison and conditional standalone probe were skipped.
2. Commit `903996257` fixes the script working directory and was pushed, but
   has not been run. Do not rerun CI in the current timebox because the
   one-run allowance is consumed. Preserve the failure as infrastructure
   evidence; no Linux PNG conclusion was reached.
3. The user confirmed no KVM runner is available. Do not retry Android API 35,
   use TCG/software emulation, or dispatch another workflow in this timebox.
   Windows and Android same-commit CI acceptance remain incomplete; iOS stays
   out of scope.

The comparison JAR, workflow YAML/Bash syntax, headers, and diff checks passed.
A first Gradle attempt failed at task configuration because the resource
provider was used incorrectly; after correction, `jarLinuxPngDecodeComparison`
passed and the JAR contains the app and both fixtures. Run `45720318038` was the
only dispatch and failed before decode; no Linux result is claimed.
