<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Milestone 1 — API 30 baseline reproduction

## Result

The current-source baseline reproduces the Android native graphics crash on
API 30 when returning from the in-place storage permission screen. The
unmodified customer APK and marker-only APK both contain a fatal SIGSEGV in
their grant-in-place logcats. The trace variant records successful customer
directory creation and deletion before the crash. This confirms reproduction;
it does not establish the Skia/EGL ownership cause.

The test harness returned exit 1 because it correctly detected that the
grant-in-place trace process did not remain alive. One exact-APK summary
sampled the original PID as alive, while that phase's logcat contains a fatal
signal for the same PID. Treat the native logcat as the process outcome.

## Source and artifact provenance

- Source baseline: `52a947b5295321d7350350bd618c55fb26304c00`, initially equal
  to `origin/master` on 2026-11-27.
- Current-source SDK version: 7.2.2.
- SDK jar SHA-256: `79c0f047497e1da65f5b49466f58eb9a04dc876b1bfd571e07cdc5438659a7d7`.
- Android AAB SHA-256: `bd0e14d085e952798d659b0f5774ecb0be3c3c254e8fba157e4795439bc9cf14`.
- `lib/arm64-v8a/libtcvm.so` SHA-256 in both APKs:
  `5ed15c8da02dda3a59846a7acc9472a25b32fc5e1451cf752863a72d608b6428`.
  Native Build ID from tombstone: `d9015557ee8a9432481be50af2cf101384e0b0d1`.
- Exact customer APK SHA-256:
  `17dc6136e10f06e1b1d794026f783c3bee17ab19ae91e320d96602cd2a18981a`
  (23,106,866 bytes).
- Marker-only APK SHA-256:
  `52dac6fa949cb627d90c2618660659e39f5ec4ab5b9ce869a6b275522bf3432f`
  (23,106,866 bytes).
- Disposable test package SHA-256:
  `dfde4bcf5678498baecf571ccad2c9026f1e92a28815fa71af7cf55cc8db852e`.
- Rebuilt `TCUI.tcz` in that test package SHA-256:
  `e08519d84906a80e93e1b9e549c992c47fb20ae7ca8e74f78d4f66a18c0dc7fb`.

The original SDK source lacks `res/android/floatingFrg.png` and
`res/android/outlined_edit.png`, though same-name files exist in
`res/mat/`. Those two Material assets were copied temporarily to the Android
resource directory while running `./gradlew-agent deployTcui`. The generated
TCUI archive replaced only `TotalCrossSDK/dist/vm/TCUI.tcz` in a temporary SDK
package. The two source aliases were removed immediately after generation.
The customer Java files and graphics code were unchanged. The overlay is a
test-input workaround and is not a production correction.

## Emulator and execution

- Host: Apple Silicon Mac, arm64.
- AVD: `Codex_TC730_API30_ARM64`; Android 11 / API 30; `arm64-v8a`.
- Fingerprint: `google/sdk_gphone_arm64/emulator_arm64:11/RSR1.240422.006/12134477:userdebug/dev-keys`.
- Renderer: Android Emulator OpenGL ES Translator (Apple M1 Pro), GLES 3.0;
  launched with `-gpu host -accel on`.
- Reused the performance-lab PR 7 build and test scripts. The exact customer
  Java source was unchanged; the trace variation added only four `Vm.debug`
  markers around the existing file operations.
- The six phases installed fresh app data between runs and captured the actual
  `MANAGE_EXTERNAL_STORAGE` AppOp before and after each phase.

## Phase assertions

| Variant | Phase | AppOp | Prompt / operation evidence | Outcome |
| --- | --- | --- | --- | --- |
| Exact | Denied | ignore → ignore | Prompt seen; exact source has no markers | No graphics crash observed |
| Exact | Grant in place | ignore → allow | Prompt and grant observed | Logcat records SIGSEGV during surface callback; summary PID field conflicts |
| Exact | Pregranted | allow → allow | No prompt; exact source has no markers | No graphics crash observed |
| Trace | Denied | ignore → ignore | Prompt; `BEGIN`, then `Operation not permitted` | Expected denied operation; no graphics crash observed |
| Trace | Grant in place | ignore → allow | `BEGIN`, `CREATED`, `DELETED` | SIGSEGV after returning to the app |
| Trace | Pregranted | allow → allow | `BEGIN`, `CREATED`, `DELETED` | Same process remained alive; no graphics crash |

The crash is in `trace-grant-in-place-logcat.txt` at 17:14:11 local time,
immediately after `initSkia` is logged from
`Java_totalcross_Launcher4A_nativeSurfaceChanged`. The exact unmodified APK
also logs fatal signal 11 at 17:12:58 during the same grant-in-place phase.
The trace run's crash caused the expected `inPlaceRemainsSameProcess` assertion
to fail. The full logs and APKs remain outside Git; compact sanitized native
frames are in `M1-api30-crash-excerpt.log`.

## Interpretation and limits

This establishes that the defect remains on the recorded current-source
baseline and that returning from the storage permission Activity reaches the
failing surface callback. Its signature matches the historical 7.3.0 report.
The log shows Skia GPU cleanup frames under `initSkia`, but M1 did not record
EGL context or Skia resource identities and cannot prove whether old-context
resources were released with a valid/current context.

The run did not include manual rotation, standalone background/foreground
controls, API 29/35/36, a production correction, or diagnostic lifecycle
instrumentation. Stop after M1; begin M2 only after explicit authorization.

## Reproduction commands

The build used `TotalCrossSDK/gradlew-agent deployTcui` with temporary aliases
for the two files listed above, then replaced only `TCUI.tcz` in the local SDK
package. The local PR 7 scripts were run as:

```sh
python3 tools/android_client_main_build.py --package <test-overlay-sdk.zip> --out <results>
ANDROID_SERIAL=emulator-5560 ANDROID_HOME=/Users/flsobral/Library/Android/sdk ANDROID_SDK_ROOT=/Users/flsobral/Library/Android/sdk \
  python3 tools/android_client_main_test.py --out <results> --timeout 32
```

The complete original startup-blocked attempt remains in `M1-api30-run.md` and
`M1-api30-startup-error.png`.
