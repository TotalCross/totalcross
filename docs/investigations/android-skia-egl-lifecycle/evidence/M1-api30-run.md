<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Milestone 1 — API 30 current-source attempt

## Provenance

- TotalCross source baseline: 52a947b5295321d7350350bd618c55fb26304c00.
- Investigation documentation commit: e298f864fb3894126ae0f51ea506f0ec635b6c4f (documentation only).
- SDK version from current source: 7.2.2.
- SDK jar SHA-256: 79c0f047497e1da65f5b49466f58eb9a04dc876b1bfd571e07cdc5438659a7d7.
- Android AAB SHA-256: bd0e14d085e952798d659b0f5774ecb0be3c3c254e8fba157e4795439bc9cf14.
- Embedded arm64-v8a/libtcvm.so SHA-256: 5ed15c8da02dda3a59846a7acc9472a25b32fc5e1451cf752863a72d608b6428.
- Customer and marker-only APK SHA-256:
  - exact: a4ac9e2de7d275eb7e1721e2947b56fac457a8e45ff0788d6321377746579e9f (23106866 bytes).
  - trace: 8f9f83fc4e38462f197b4e1b717b42ed35bce5c5d35180c867d2d41eb07f5fa4 (23106866 bytes).

## Emulator and execution

- AVD Codex_TC730_API30_ARM64; API 30; arm64-v8a.
- Fingerprint: google/sdk_gphone_arm64/emulator_arm64:11/RSR1.240422.006/12134477:userdebug/dev-keys.
- Renderer: Android Emulator OpenGL ES Translator (Apple M1 Pro), OpenGL ES 3.0.
- Emulator launched read-only with -gpu host and -accel on.
- Reused performance-lab PR 7 build/test scripts. Temporary metadata adapter labeled local source revision and SDK 7.2.2; customer Java source and operation were unchanged.
- Build result: exact and trace APKs compiled. Test harness overall: FAIL; 6 phases executed. Native crash signature matches in captured logcat: 0.

## Phase assertions

```json
[
  {
    "variant": "exact",
    "phase": "denied",
    "sameProcess": true,
    "permissionPromptSeen": false,
    "grantAppliedWhilePromptOpen": false,
    "fileDirCreated": false,
    "fileDirDeleted": false,
    "fileDirFailed": false,
    "storageAccessBefore": "MANAGE_EXTERNAL_STORAGE: ignore\n",
    "storageAccessAfter": "MANAGE_EXTERNAL_STORAGE: ignore\n",
    "storageDirAfter": "ls: /sdcard/asd: No such file or directory\n"
  },
  {
    "variant": "exact",
    "phase": "grant-in-place",
    "sameProcess": true,
    "permissionPromptSeen": false,
    "grantAppliedWhilePromptOpen": false,
    "fileDirCreated": false,
    "fileDirDeleted": false,
    "fileDirFailed": false,
    "storageAccessBefore": "MANAGE_EXTERNAL_STORAGE: ignore\n",
    "storageAccessAfter": "MANAGE_EXTERNAL_STORAGE: ignore\n",
    "storageDirAfter": "ls: /sdcard/asd: No such file or directory\n"
  },
  {
    "variant": "exact",
    "phase": "granted-before-launch",
    "sameProcess": true,
    "permissionPromptSeen": false,
    "grantAppliedWhilePromptOpen": false,
    "fileDirCreated": false,
    "fileDirDeleted": false,
    "fileDirFailed": false,
    "storageAccessBefore": "MANAGE_EXTERNAL_STORAGE: allow\n",
    "storageAccessAfter": "MANAGE_EXTERNAL_STORAGE: allow\n",
    "storageDirAfter": "ls: /sdcard/asd: No such file or directory\n"
  },
  {
    "variant": "trace",
    "phase": "denied",
    "sameProcess": true,
    "permissionPromptSeen": false,
    "grantAppliedWhilePromptOpen": false,
    "fileDirCreated": false,
    "fileDirDeleted": false,
    "fileDirFailed": false,
    "storageAccessBefore": "MANAGE_EXTERNAL_STORAGE: ignore\n",
    "storageAccessAfter": "MANAGE_EXTERNAL_STORAGE: ignore\n",
    "storageDirAfter": "ls: /sdcard/asd: No such file or directory\n"
  },
  {
    "variant": "trace",
    "phase": "grant-in-place",
    "sameProcess": true,
    "permissionPromptSeen": false,
    "grantAppliedWhilePromptOpen": false,
    "fileDirCreated": false,
    "fileDirDeleted": false,
    "fileDirFailed": false,
    "storageAccessBefore": "MANAGE_EXTERNAL_STORAGE: ignore\n",
    "storageAccessAfter": "MANAGE_EXTERNAL_STORAGE: ignore\n",
    "storageDirAfter": "ls: /sdcard/asd: No such file or directory\n"
  },
  {
    "variant": "trace",
    "phase": "granted-before-launch",
    "sameProcess": true,
    "permissionPromptSeen": false,
    "grantAppliedWhilePromptOpen": false,
    "fileDirCreated": false,
    "fileDirDeleted": false,
    "fileDirFailed": false,
    "storageAccessBefore": "MANAGE_EXTERNAL_STORAGE: allow\n",
    "storageAccessAfter": "MANAGE_EXTERNAL_STORAGE: allow\n",
    "storageDirAfter": "ls: /sdcard/asd: No such file or directory\n"
  }
]
```

## Interpretation

The attempt is blocked before the permission and filesystem scenarios. The first screenshot shows an unhandled FileNotFoundException for /data/user/0/totalcross.appzmtw/totalcross/res/android/floatingFrg.png in Resources.uiStyleChanged / MainWindow.setUIStyle, followed by “Aborting program.” No permission prompt or customer File operation marker appeared in any phase. MANAGE_EXTERNAL_STORAGE remained ignore in denied and grant-in-place phases and was allow in pregranted phases. The application process staying alive is the visible error dialog, not a successful application result. No EGL crash was observed, but this does not establish that current master avoids the historical crash because the test never reached the permission Activity transition.

The current source graphics lifecycle implementation remains the same as the historical 7.3.0 source in the relevant Android paths. Resolve the missing UI resource or obtain an equivalent valid current-source deployment input before attempting another baseline run. Do not change the customer source or production graphics code to bypass this unrelated startup failure.

## Commands

SDK: cd TotalCrossSDK && ./gradlew-agent dist -x test.
Android dependency preparation: cd TotalCrossVM/android && ANDROID_HOME=/Users/flsobral/Library/Android/sdk ANDROID_SDK_ROOT=/Users/flsobral/Library/Android/sdk ./gradlew --no-daemon :tcvm:fetchNativeDependencies.
Android runtime bundle: cd TotalCrossVM/android && ANDROID_HOME=/Users/flsobral/Library/Android/sdk ANDROID_SDK_ROOT=/Users/flsobral/Library/Android/sdk ./gradlew --no-daemon :app:bundleStandardRelease.
Reproducer: android_client_main_build.py --package <current-source-sdk.zip> --out <results>; android_client_main_test.py --out <results> --timeout 32, with ANDROID_SERIAL=emulator-5560 and Android SDK environment set.

Full generated APKs and logcats remain outside Git. Full build logs: /tmp/codex-android-skia-egl-sdk-dist.log, /tmp/codex-android-skia-egl-deps-final.log, /tmp/codex-android-skia-egl-android-bundle.log, /tmp/codex-android-skia-egl-build-apks.log, and /tmp/codex-android-skia-egl-api30-baseline.log.
