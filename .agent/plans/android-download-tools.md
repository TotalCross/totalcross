<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Android offline deployment tools

This plan follows `AGENTS.md` and `.agent/PLANS.md`.

## Context and objective

Android deployment currently resolves Bundletool and Protoc on demand. Add a
standalone `tc.Deploy -download-tools` command that prepares an SDK `etc` tree
for later Android deployments without network access.

## Scope and final behavior

Keep Bundletool 1.10.0 in `etc/tools/android` and store Protoc 21.0 under
versioned platform directories for Windows x64, Linux x86_64, Linux aarch64,
and universal macOS. Normal deployment prefers the current platform's
versioned executable, accepts a valid legacy executable without relocating it,
and downloads only the current platform when neither is usable.

Offline preparation reuses valid files, downloads Bundletool and each Protoc
platform, and never executes foreign binaries. Only the current host receives
executable permissions or macOS quarantine removal. Downloads and extraction
use temporary paths, reject traversal, and install only the expected Protoc
binary. Preparation failures identify tool, version, and target platform. On
success the command prints the normalized SDK `etc` path and prepared tree,
then exits before ordinary deployment.

Keep network, probe, permission, and quarantine test hooks so tests use local
fixtures. Cover command parsing, layout, fallback, extraction safety, offline
multi-platform preparation, reuse, and failures.

## Compatibility constraints

Keep existing Android deployment behavior outside tool lookup intact. Preserve
the legacy Protoc layout and current platform selection rules. Do not probe or
change permissions on foreign platform executables.

## Validation

Run the focused deploy and Android tool locator tests, followed by the SDK
distribution validation at milestone close. Static review must confirm the
command bypasses normal deployment and that extraction cannot write outside
the tool directory. Android platform builds are prohibited for this work.

## Out of scope

Do not redesign Android deployment, change tool versions, add other platforms,
or preserve temporary plans, logs, generated artifacts, or exploratory history.
