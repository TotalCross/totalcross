<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Reproducible true-base benchmark harness

`prepare-image-opt-phase2-true-base.sh` adapts the exact benchmark harness to
the Phase-1 runtime without copying Phase-2 runtime code. It checks out the
benchmark sources from an explicit source revision, copies the two local PNG
fixtures and the macOS launcher used by deployment, adds no-op counter shims,
and rewrites only benchmark references to APIs unavailable in the true base.

The required base is:

`9545c18207fab74d81340b24825c5a82ddbda7fd`

Run it against a disposable detached worktree. The final two arguments are
the exact fixture directory containing `lenna.png` and `lenna_full.png`, and
the launcher used by the deployment. The script prints a content digest over
the eight adapted harness files (six Java files, `build.gradle`, and the RSS
runner). The digest is the reproducibility identity for the adapter.

Example:

```sh
git worktree add --detach /private/tmp/image-opt-phase2-true-base \
  9545c18207fab74d81340b24825c5a82ddbda7fd
.agent/benchmarks/image-opt-phase2-raster/true-base-harness/\
prepare-image-opt-phase2-true-base.sh \
  /private/tmp/image-opt-phase2-true-base \
  "$PWD" <source-harness-commit> \
  "$PWD/TotalCrossSDK/src/main/resources/images" \
  "$PWD/TotalCrossSDK/etc/launchers/macos/Launcher"
```

The base build still uses the pinned software-Skia CMake configuration. The
adapter does not enable any Phase-2 feature and does not change native runtime
sources; its compatibility methods return zero because the base has no Phase-2
counters.
