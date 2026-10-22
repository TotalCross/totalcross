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
