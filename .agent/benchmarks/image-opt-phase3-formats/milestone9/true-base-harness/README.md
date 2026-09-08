<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Milestone-9 exact Phase-2 adapter

This adapter runs the final-stack Milestone-9 harness against the frozen
Phase-2 commit `6d1c95f77fcb9c74d19b4e9393dba7c82cd37aee`. It copies only the
benchmark support/apps, launcher, and fixed fixtures into a disposable detached
worktree. The adapter shims Phase-3-only storage introspection, counters, and
failure hooks; Phase-2 feature IDs `0,1,2,3,4,13,14,15` remain real settings
and native behavior from the exact base.

Run from the Phase-3 repository root:

```sh
git worktree add --detach /private/tmp/image-opt-phase3-milestone9-true-base \
  6d1c95f77fcb9c74d19b4e9393dba7c82cd37aee
.agent/benchmarks/image-opt-phase3-formats/milestone9/true-base-harness/\
prepare-image-opt-phase3-milestone9-true-base.sh \
  /private/tmp/image-opt-phase3-milestone9-true-base "$PWD" HEAD \
  "$PWD/TotalCrossSDK/src/smokeTest/resources/image-opt-phase3" \
  "$PWD/TotalCrossSDK/etc/launchers/macos/Launcher" \
  "$PWD/build/image-opt-phase1-corrective-macos/libtcvm.dylib"
```

The command prints the source revision, exact base revision, and a deterministic
SHA-256 digest over every adapted harness source and fixture. The target path
must be disposable; the script never edits the active Phase-3 worktree.
