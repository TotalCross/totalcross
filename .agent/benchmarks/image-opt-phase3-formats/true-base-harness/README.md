<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Reproducible Phase-2 true-base adapter

This adapter replays the Phase-3 compact-format harness on the exact accepted
Phase-2 runtime (`86bfeafe388ce866236c3ae58eecb144664895e2`). It archives the
benchmark sources from an explicit Phase-3 harness revision, copies the fixed
encoded fixtures and macOS launcher into a disposable detached worktree, and
uses the explicit `ImageCompactFormatsNativeHooks` shim in the benchmark
support package for
Phase-3-only format/counter/failure hooks that do not exist in Phase 2.

The adapter does not copy or modify runtime source. Its digest covers the
benchmark Gradle registration, RSS runner, support classes, benchmark/smoke
apps, and fixture bytes. Run it only in a disposable true-base worktree:

```sh
git worktree add --detach /private/tmp/image-opt-phase3-true-base \
  86bfeafe388ce866236c3ae58eecb144664895e2
.agent/benchmarks/image-opt-phase3-formats/true-base-harness/\
prepare-image-opt-phase3-true-base.sh \
  /private/tmp/image-opt-phase3-true-base "$PWD" <harness-revision> \
  "$PWD/TotalCrossSDK/src/smokeTest/resources/image-opt-phase3" \
  "$PWD/TotalCrossSDK/etc/launchers/macos/Launcher" \
  "$PWD/build/libtcvm.dylib"
```

The command prints `source_revision`, `base_revision`, and `adapter_digest`.
