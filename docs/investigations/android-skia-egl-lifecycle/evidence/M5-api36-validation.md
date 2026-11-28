<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Milestone 5 — API 36 functional validation

This authorized API 36 slice reused the default-off ARM64 Release runtime and
compatible customer, marker, and graphics-probe APKs. No SDK or native runtime
rebuild was needed. No production source changed, and no exceptional-path
injection tests or non-Android builds were repeated.

## Emulator and artifacts

The run used AVD `Codex_ImageOwnership_API36_ARM64`, device `emulator-5556`,
Android API 36, `arm64-v8a`, and fingerprint
`google/sdk_gphone64_arm64/emu64a:16/BE2A.250530.026.F3/13894323:userdebug/dev-keys`.
SurfaceFlinger reported the hardware-accelerated renderer
`Google (Apple), Android Emulator OpenGL ES Translator (Apple M1 Pro)`, GLES
3.0.

| Artifact | SHA-256 |
| --- | --- |
| Default-off ARM64 Release `libtcvm.so` | `c86831b58e4beb0415468d354473cd0f7fc98c09ceb3549abd6d9b89b4316610` |
| Exact customer APK | `842af2c7d984074d09806139252629b626c888e4ccefe570d01f7b704d958af5` |
| Marker-only customer APK | `9252d1738d7e036884f6216138f029f266f9fc4b0495b221e5c568fd01609779` |
| Default-off graphics probe APK | `abb88ebc7790789d0d38cf63ac07834ba947ca660fd9d8b912c9e2b1979c1232` |
| Diagnostic graphics control APK | `b52b2034f0779c085b78aabffbd1812014a4df95215b2ed7e28fcb05a983fcb0` |
| Diagnostic control embedded runtime | `0e9e5e37b79c29db96acce7083f9b29e788832750614fe72c129f5a53c1586b5` |
| Disposable `TCUI.tcz` overlay | `e08519d84906a80e93e1b9e549c992c47fb20ae7ca8e74f78d4f66a18c0dc7fb` |

Each default-off APK embeds the recorded default-off runtime hash. All six
`debug.tcvm.*` properties were zero for the default-off scenarios and the
lifecycle log filter was `SUPPRESS`. The separate diagnostic control used
the same natural Settings-return transition with injection properties zero and
the lifecycle log filter at `INFO` to record EGL events. The complete log
hashes and normalized assertions are in
`M5-api36-scenario-results.json`.

## Customer permission and filesystem cases

| Case | Permission | Filesystem | Return and process | Result |
| --- | --- | --- | --- | --- |
| Exact customer, grant in place | `ignore` before; `allow` after | Exact APK emits no filesystem markers | Settings returned to the same PID; app regained focus | PASS |
| Exact customer, denied return | `ignore` before and after | No `/sdcard/asd` directory remained | Settings returned to the same PID; app regained focus | PASS |
| Marker, grant in place | `ignore` before; `allow` after | Created and deleted `/sdcard/asd` | Same PID and focused app | PASS |
| Marker, denied return | `ignore` before and after | Error 13 `Permission denied`; no create/delete | Same PID and focused app | PASS |
| Marker, pregranted | `allow` before startup | Created and deleted `/sdcard/asd` without Settings | Same PID and focused app | PASS |

All runs had no fatal native signal. Marker directories were absent after the
one-shot operation. The exact customer APK does not expose filesystem markers;
the companion marker app verifies the authorized and denied filesystem
outcomes. `M5-api36-scenario-results.json` derives the denied assertion from
the raw `Permission denied` log entry. The retained raw marker summary's
`denied` boolean was false only because its classifier matched the different
phrase `Operation not permitted`.

## Rendering and natural transitions

The application-level probe reported `READY shared=2 retained=1`. With all
test hooks off, the same PID (`18202`) moved portrait → landscape → portrait,
went Home and returned to the foreground, opened and returned from system
Settings, and created a new image. It regained focus and remained responsive
at every observed return. The probe's Settings launch left its app-op at the
default state; the customer and marker apps separately exercised the actual
permission screen.

PNG files were decoded to RGB24 and checked with exact color counts, without
tolerance. Portrait captures are all 1080×2400; landscape is 2400×1080.

| Capture | Red | Yellow | Blue | Green | Magenta | Cyan |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| Portrait before transitions | 95256 | 31752 | 47628 | 47628 | 15876 | 0 |
| Portrait after rotation | 95256 | 31752 | 47628 | 47628 | 15876 | 0 |
| Portrait after Home/foreground | 95256 | 31752 | 47628 | 47628 | 15876 | 0 |
| Portrait after Settings return | 95256 | 31752 | 47628 | 47628 | 15876 | 0 |
| Portrait after new image | 95256 | 31752 | 47628 | 47628 | 15876 | 47628 |
| Landscape | 95256 | 31752 | 47628 | 47628 | 15876 | 0 |

All original colors remain at their exact counts through the transitions. The
orientation round-trip moves each original image bound down exactly 65 pixels;
the same translated bounds persist after Home/foreground and Settings return.
This is a consistent application layout shift, not missing or unrestored image
content. After the button interaction, the new cyan image appears at
`x=399..650, y=674..925`, and its black interior sample is exact. Before and
after sample coordinates, bounds, full screenshot hashes, and exact counts
are in `M5-api36-pixel-assertions.json` and the six committed PNG captures.

![API 36 probe before recreation](M5-api36-portrait-before.png)

![API 36 probe after rotation](M5-api36-portrait-after-rotation.png)

![API 36 probe after creating a new image](M5-api36-portrait-new-image.png)

## EGL event boundary and crash scan

The default-off run suppressed lifecycle logging and therefore does not
independently prove EGL destroy/create. Its rendering assertions remain valid.
A separate diagnostic build on the same API 36 emulator returned naturally
from Settings in the same PID. Its ordered events were:

1. `seq=58 skia.cleanup.valid-current.end`
2. `seq=59 egl.destroy.begin`
3. `seq=70 egl.create.madeCurrent`
4. `seq=82 skia.init.end`

The excerpt is `M5-api36-diagnostic-lifecycle-excerpt.log`. It shows actual
replacement EGL initialization for that equivalent diagnostic transition;
it is not represented as instrumentation-free replacement evidence.

The exact, marker, default-off graphics, and diagnostic control logcats were
searched for `Fatal signal`, `SIGSEGV`, `SIGABRT`, `FATAL EXCEPTION`, and
`Abort message`; no matches were found. No API 35 or API 29 tests were started.
Pause here pending explicit authorization for the next API validation.
