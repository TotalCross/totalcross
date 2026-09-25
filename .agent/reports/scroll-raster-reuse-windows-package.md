<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Windows scroll raster reuse package

## Package

The Windows x64 benchmark bundle is [image-scroll-raster-reuse-windows-x64.zip](/tmp/scroll-raster-reuse-windows/package-final/image-scroll-raster-reuse-windows-x64.zip) (44,555,487 bytes; SHA-256 `97709db4cd69a326f5a3232a99981c73ad49ff53dda45b09a84421d643774235`). Its manifest fixes two correctness preflights and six measured processes in OFF × 3, then ON × 3 order. The bundle contains the compiled app, the action-built `tcvm.dll`, the 663-image workload, its manifest, and the PowerShell 5.1 runner and helpers.

The packaged runner records process and per-pass cold/warm timing distributions, all six frame thresholds, host drawable and SDL metrics, reuse counters, logs, and failure evidence. It prints startup and periodic process progress. The expected operator command is in the bundle's `README.md`.

## Provenance

- Source branch: `feat/frame-pacing-scheduling-diagnostics`
- Source and SDK attestation commit: `7af05d79f7a74a7e3ace2aad9e5bafea2cc2ccba`
- Successful [`package.yml` run 36183652625](https://github.com/TotalCross/totalcross/actions/runs/36183652625)
- Full SDK artifact: `TotalCross-7.2.2`, ID `10885666470`; downloaded ZIP SHA-256 `bb0f66a0a15fb6bf9eb5b8988140e712b39a17f00541102a2943074bd4bfe1b8`
- `tcvm.dll` SHA-256: `88f7bcb4b4cd252c6a36de09b42317926a0af1524b2a2167af741ff76f5547d9`
- Corpus digest: `af39fea695191a27` (663 files: 660 JPEG and 3 PNG payloads)

The machine-readable hashes and validation index are in [scroll-raster-reuse-windows-package.json](/tmp/totalcross-scroll-raster-reuse-windows/.agent/evidence/scroll-raster-reuse-windows-package.json).

## Validation

The focused runner/package contract checks (4 groups), frame-pacing contract tests (13 checks), image-scroll distributed-benchmark tests, packaging shell syntax, copyright-header validation, and `git diff --check` passed. The final SDK package workflow passed, including its Windows, Linux, and macOS deploy checks. The produced ZIP passed archive integrity and manifest/hash checks.

No benchmark process was executed. The package manifest records `windowsExecuted: false`; only the build workflow produced the SDK and Windows runtime. PowerShell parsing was not available on this macOS host.
