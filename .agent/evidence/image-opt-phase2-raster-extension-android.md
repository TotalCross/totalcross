<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Phase 2 raster extension Android validation

Status: `DEFERRED — matching runtime requires a prohibited Android build`

The required availability check passed:

    adb devices
    192.168.1.154:43289 device

Device: Xiaomi 2312DRA50G, Android 13. Installed TotalCross package names
were `totalcross.appdouk`, `totalcross.appmbat`, `totalcross.apprtst`,
`totalcross.appvkss`, `totalcross.appantj`, `totalcross.appsvfc`, and
`totalcross.appsvfv`. None had identifiable provenance for the final Phase 2
runtime `c6515a8f0` or the final branch/harness revision
`7700966325b84b876d42822465b0c9a9d5f3b1ae`.

No Android build, install, logcat capture, or GPU workload was performed.
Therefore the draw-only GPU invariant checks remain unexecuted: raster-only
writePixels, target-color, physical-variant, physical-identity, and readback
counters were not asserted on Android. The macOS software-Skia checks are the
authoritative closeout evidence for this task.
