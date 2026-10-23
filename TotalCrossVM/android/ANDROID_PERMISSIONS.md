<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Android permissions

## Deploy-time manifest declarations

Add an Android permission to an application manifest with the repeatable Deploy
option `/android_permission <fully.qualified.permission.name>`. For example:

```text
Deploy MyApp.java -android /android_permission android.permission.CAMERA /android_permission com.vendor.permission.ACCESS_DEVICE
```

Permission names keep their original case. Duplicate declarations are removed,
including duplicates already present in the Android template. The older
`/android_manage_external_storage` and `/android_request_install_packages`
options remain supported and add the corresponding permission through the same
manifest editor.

The Android template keeps its existing baseline permissions, including
`RECORD_AUDIO` and `WRITE_EXTERNAL_STORAGE`. It does not add
`READ_EXTERNAL_STORAGE` by default. It no
longer declares `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`,
`MANAGE_EXTERNAL_STORAGE`, or `REQUEST_INSTALL_PACKAGES` by default. Add any of
these with `/android_permission`, or use the matching legacy option for the two
special permissions. Permission identifiers use Android's fully qualified
name format, such as `android.permission.CAMERA` or
`com.vendor.permission.ACCESS_DEVICE`; repeat the option for each declaration.

A manifest declaration is not runtime authorization. Android may still ask the
user, deny access, or provide only partial access. A declaration also does not
guarantee Google Play policy approval. Google Play restricts broad media access,
all-files access, and permission to install unknown packages; apps must meet the
current policy requirements for their core use case.

## Runtime authorization

The Android runtime coordinator reads the installed app's effective permission
declarations from `PackageManager.GET_PERMISSIONS` and checks current grants
when an operation needs access. It serializes different pending requests,
coalesces matching requests, and completes callers after a bounded wait. Camera
and microphone requests are independent. Phone, location, and Bluetooth
requests continue through the same coordinator.

On Android 13 and newer, file-access recovery may request only the declared
`READ_MEDIA_IMAGES` and `READ_MEDIA_VIDEO` permissions together. On Android 14
and newer it also includes `READ_MEDIA_VISUAL_USER_SELECTED` when that
permission is present in the installed manifest. The coordinator reports full,
partial, and denied media access separately. On Android 14 and newer, when an
app targets API 34 or newer without declaring
`READ_MEDIA_VISUAL_USER_SELECTED`, compatibility-mode grants for
`READ_MEDIA_IMAGES` or `READ_MEDIA_VIDEO` are reported as partial rather than
permanent full access. Android 12L and older use only declared legacy storage
permissions that apply to that OS version.

`MANAGE_EXTERNAL_STORAGE` and `REQUEST_INSTALL_PACKAGES` use separate
app-specific Settings screens. They are never sent through the ordinary runtime
permission dialog. Each flow checks the resulting authorization again when the
user returns. Media declarations do not make arbitrary files readable: a file
operation can still require all-files access, and the system may reject the
operation after media authorization. Settings screens are launched directly
with controlled failure handling rather than checked through package visibility.
Repeated automatic filesystem failures are suppressed only while the permission
snapshot and foreground user-interaction generation are unchanged. A new user
interaction can permit another recovery attempt, although Android may still
suppress a runtime permission dialog after repeated denial.

## Camera, gallery, and video

Camera hardware operations and barcode scanning request `CAMERA` only. Gallery
selection and video playback do not request camera access. On Android 11 and
newer, image selection first attempts the system photo picker, including on
devices with a supporting modular system update, and falls back to
`ACTION_GET_CONTENT` if that activity is unavailable. Older Android versions
use `ACTION_GET_CONTENT` directly. The selected `content://` stream is copied
to a temporary file beside the existing TotalCross output filename and replaces
that destination only after the full copy and stream close succeed. Reading a
source URI explicitly returned by a system picker does not require broad
media-library permissions.

`CAMERA_NATIVE` keeps a public original and returns a copy at the requested
TotalCross path. Android 10 and newer store that original as an app-owned
MediaStore item, finalize it after a successful capture, and remove incomplete
items. `CAMERA_NATIVE_NOCOPY` uses an app-specific temporary file shared with
the camera through FileProvider, copies it to the requested TotalCross path,
and removes the temporary file. Neither flow resolves a gallery filesystem
path or deletes an arbitrary gallery item. On older Android versions, a public
camera destination may still depend on a declared legacy storage permission.

CameraX still captures write directly to the requested file. `CAMERA_PICTURE`
requests `CAMERA` and never requests microphone permission. The
`CAMERA_VIDEO_RECORDER` mode requests `RECORD_AUDIO` immediately before an
audio-enabled recording starts. `CAMERA_FULL` keeps preview and photographs
independent of microphone access and requests `RECORD_AUDIO` only when a
sustained press starts an audio-enabled video recording. Denial leaves preview
and still photographs available, but prevents that recording. Recording with
audio disabled never requests or accesses `RECORD_AUDIO`. In `CAMERA_FULL`, a
press shorter than 200 ms selects a photograph and a sustained press selects
video.
`CAMERACAPTURE_MODE_VIDEOONLY` records without audio and does not request
microphone access.

Speech input checks and requests `RECORD_AUDIO` before opening the external
speech recognizer. Denial, cancellation, a missing recognizer, or an empty
result returns the existing null result. Text-to-speech output and app startup
do not request microphone access. Speech input has a bounded native wait so a
missing result callback cannot block the VM indefinitely.

## APK installation

`Vm.exec()` calls whose command ends in `.apk` use the existing FileProvider to
share a read-only APK URI with the Android package installer. On Android 8 and
newer, the installed app must declare
`android.permission.REQUEST_INSTALL_PACKAGES`; if it is declared but not yet
allowed, TotalCross opens the app-specific unknown-sources Settings page and
checks authorization again when the user returns. If the declaration is
missing, the diagnostic names both supported Deploy options. The install
permission is never requested at startup or for other `Vm.exec()` commands.

## External activities

The external camera, system photo picker, speech recognizer, APK installer, and
app-specific Settings screens are launched directly and handle launch failures
through their existing controlled result paths. They do not rely on
`resolveActivity()` package-visibility prechecks. If the system photo picker
cannot be launched on Android 11 or newer, image selection falls back to
`ACTION_GET_CONTENT`; a picker cancellation after a successful launch does not
trigger that fallback.

## Compatibility and policy

The Android minimum SDK remains 23. Android 13 and newer use separate image and
video media permissions for recovery; Android 14 and newer can additionally
grant only the selected visual items. A partial grant is reported as partial,
not permanent full-library access. Compatibility grants for apps targeting an
older SDK are also not treated as full access. Android 12L and older consider
only declared legacy storage permissions that apply to that version.

Broad media permissions, all-files access, and permission to install unknown
packages are optional declarations. Google Play policy can limit their use or
require a qualifying core use case and review. Developers must confirm the
current Play requirements for the app before enabling them; a manifest entry
alone does not establish policy eligibility.

## Filesystem recovery

Automatic recovery starts only after native file open/create, directory
creation, deletion, rename, existence/type/empty and metadata operations, or
size/free-space queries fail with `EACCES` or `EPERM`. It uses the permissions
declared by the installed app; paths and file extensions do not select
permissions. Declared, applicable media or legacy storage permissions
are requested together, followed by the separate all-files Settings flow when
that permission is declared and supported. A failed system call is retried at
most once after authorization changes. The flow does not retry recursively or
run at startup. After a denied prompt, automatic recovery is suppressed while
both the permission-state snapshot and user-interaction generation remain
unchanged. A new foreground user interaction can permit another recovery
attempt; repeated background failures and timer operations do not advance that
generation. A relevant authorization change is detected even without a new
interaction. Android may suppress subsequent runtime permission dialogs after
repeated denials. A new user interaction permits another recovery attempt, but
does not guarantee that Android will show a dialog or grant authorization.
Explicit `requestFileAccess()` operations continue to bypass automatic
suppression.
