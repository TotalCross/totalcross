<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# POSIX realpath startup correction

## Summary

The shared Linux/macOS startup helper passed a fixed 1,024-byte stack buffer to
`realpath()`. With fortified glibc, `__realpath_chk` rejects a non-null output
buffer smaller than `PATH_MAX` (4,096 bytes on the recorded Ubuntu runner),
even when the resolved path itself is short. The child process aborts with
`SIGABRT` before Java startup.

`copyResolvedPath()` now calls `realpath(path, NULL)`, copies the allocated
result when resolution succeeds, falls back to the input path otherwise, and
frees the allocated result. It preserves the existing bounded copy,
termination, truncation, empty/null-input, and fallback behavior.

## Changes

- Updated `TotalCrossVM/src/init/posix/startup_c.h` with the allocation-form
  call and matching `free()`.
- Added focused native helper tests for path forms, fallback, capacities,
  termination, truncation, repeated success/failure, and startup-safe behavior.
- Parameterized the canonical reusable build workflow so hosted validation runs
  Linux x86_64, macOS ARM64, and SDK jobs while skipping Android, iOS, Windows,
  and ARM cross-builds.
- Added a deployed Linux path matrix with file, SQLite, and known-good JPEG
  controls, plus a macOS ARM64 normal/symlink startup smoke.
- Recorded the reproduction, final run, and implementation caveats in the
  [evidence index](../evidence/fix-linux-realpath-startup.md).

## Validation

Hosted run [46051419602](https://github.com/TotalCross/totalcross/actions/runs/46051419602)
passed:

- The old helper aborts under explicit `_FORTIFY_SOURCE=2`; the fixed helper
  passes startup-safe and path-contract checks under ASan/UBSan with leak
  detection.
- Canonical Linux and macOS native builds each pass both resolved-path CTests.
- The deployed Linux application reaches `MainWindow.initUI()` and completes
  normal, short, bounded-long, symlink, and relative launch cases. All five
  pass file I/O, SQLite `integrity_check`, and JPEG decode/draw controls.
- The deployed macOS ARM64 application reaches startup completion from normal
  and symlink launch paths without `DYLD_LIBRARY_PATH` or
  `DYLD_INSERT_LIBRARIES`.
- The SDK build, provenance tests, native bridge generation, and SDK graphical
  smoke pass.

The current canonical Linux Release compile uses `-O3`, has no explicit
`_FORTIFY_SOURCE` definition, and the corrected VM does not import
`__realpath_chk`. The fortified baseline is a separate hosted helper build that
explicitly sets `_FORTIFY_SOURCE=2`; the deployed runtime smoke validates the
corrected canonical artifacts.

The deployed long-path case is limited to a 206-byte executable path because
the existing VM cannot load the application TCZ from longer install paths.
Deeper paths are covered by the C helper contract test. The original
TotalCross 7.2.2 launcher overflow remains a separate historical issue and is
not attributed to this fix.

One existing static check, `scripts/test-native-dependencies-workflow.py`,
still fails because it expects seven prepared-dependency references while the
base workflow already has eight. No native desktop tests ran locally; Android,
iOS, Windows, ARM Linux cross-builds, and benchmarks were not run.

## Branch

The branch `fix/linux-realpath-startup` is pushed from base
`52a947b5295321d7350350bd618c55fb26304c00`. The fix and test commits are
separate logical changes. No pull request, tag, release, or deployment was
created.
