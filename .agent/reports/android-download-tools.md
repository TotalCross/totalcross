<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Android offline deployment tools report

## Summary

Added standalone `tc.Deploy -download-tools` preparation for an SDK that will
be copied to Android deployment hosts without network access.

## Final implementation

Bundletool 1.10.0 stays under `etc/tools/android`. Protoc 21.0 uses
versioned per-platform directories. Normal lookup prefers the current
platform's versioned executable, then accepts a valid legacy executable, and
downloads only the current platform when needed. Offline preparation reuses
valid files and prepares all four supported platforms without probing foreign
binaries. Archive extraction rejects traversal and installs only `protoc`.
The command prints the normalized `etc` path and prepared tree, then returns
before normal deployment.

## Decisions and tradeoffs

Host-only probing, permission changes, and macOS quarantine removal keep foreign
executables inert during cross-host preparation. Network and host-preparation
hooks let focused tests verify behavior without contacting release servers.

## Validation

- `./gradlew-agent test --tests tc.DeployDownloadToolsTest --tests
  tc.tools.deployer.AndroidToolLocatorTest --console=plain` — passed, 16 tests,
  0 failures (log: `/tmp/tc-pr-android-download-tools-test.log`).
- `./gradlew-agent dist -x test --console=plain` — passed (log:
  `/tmp/tc-pr-android-download-tools-dist.log`).
- `git diff --check origin/master...HEAD` and focused copyright-header
  validation passed.

No live downloads were run; downloader behavior used test hooks. Android and
other non-macOS platform builds were omitted under the task's build restrictions.

## Limitations and deferred work

The prepared Protoc set is limited to Windows x64, Linux x86_64, Linux aarch64,
and universal macOS. No tool-version changes or unrelated Android deployment
redesign are included.
