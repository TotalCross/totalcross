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
`RECORD_AUDIO`, `READ_EXTERNAL_STORAGE`, and `WRITE_EXTERNAL_STORAGE`. It no
longer declares `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`,
`MANAGE_EXTERNAL_STORAGE`, or `REQUEST_INSTALL_PACKAGES` by default. Add any of
these with `/android_permission`, or use the matching legacy option for the two
special permissions.

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
partial, and denied media access separately; a compatibility-mode grant for an
older target SDK is not treated as permanent full access. Android 12L and older
use only declared legacy storage permissions that apply to that OS version.

`MANAGE_EXTERNAL_STORAGE` and `REQUEST_INSTALL_PACKAGES` use separate
app-specific Settings screens. They are never sent through the ordinary runtime
permission dialog. Each flow checks the resulting authorization again when the
user returns. Media declarations do not make arbitrary files readable: a file
operation can still require all-files access, and the system may reject the
operation after media authorization.

## Filesystem recovery

Automatic recovery starts only after native file open/create, directory
creation, deletion, or size operations fail with `EACCES` or `EPERM`. It uses
the permissions declared by the installed app; paths and file extensions do not
select permissions. Declared, applicable media or legacy storage permissions
are requested together, followed by the separate all-files Settings flow when
that permission is declared and supported. A failed system call is retried at
most once after authorization changes. The flow does not retry recursively or
run at startup. After a denied prompt, automatic recovery waits for a relevant
authorization change; a later explicit operation can request again.
