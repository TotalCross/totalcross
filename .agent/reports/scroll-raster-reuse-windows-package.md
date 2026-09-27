<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Windows scroll raster reuse package

## Package

The initial Windows x64 bundle was [image-scroll-raster-reuse-windows-x64.zip](/tmp/scroll-raster-reuse-windows/package-final/image-scroll-raster-reuse-windows-x64.zip) (44,555,487 bytes; SHA-256 `97709db4cd69a326f5a3232a99981c73ad49ff53dda45b09a84421d643774235`). Its manifest fixes two correctness preflights and six measured processes in OFF × 3, then ON × 3 order. It contains the compiled app, `tcvm.dll`, the 663-image workload, and the PowerShell 5.1 runner.

After two operator failures exposed a cold/warm record parser issue and an ON-mode assertion bug, the app and runner were corrected. The rebuilt operator bundle is [image-scroll-raster-reuse-windows-x64.zip](/tmp/scroll-raster-reuse-windows/hotfix-package/image-scroll-raster-reuse-windows-x64.zip) (44,557,233 bytes; SHA-256 `cf62c66b498b6376bf1943d6a4812b7aff6913c2b960a8bc0f7158a3abbb44d6`). A smaller replacement archive is [scroll-raster-reuse-windows-preflight-fix.zip](/tmp/scroll-raster-reuse-windows/hotfix-package/scroll-raster-reuse-windows-preflight-fix.zip) (56,711 bytes; SHA-256 `50de92eae3ef5e0c7c36a828f12167192a4e8edc04fc51d14e48f31b204d2666`).

The packaged runner records process and per-pass cold/warm timing distributions, all six frame thresholds, host drawable and SDL metrics, reuse counters, logs, and failure evidence. It prints startup and periodic process progress. The expected operator command is in the bundle's `README.md`.

## Provenance

- Source branch: `feat/frame-pacing-scheduling-diagnostics`
- Source and SDK attestation commit: `7af05d79f7a74a7e3ace2aad9e5bafea2cc2ccba`
- Windows assertion and runner recovery fix: `1e8d7c03f120356316603f6a32ca929d7423748c`
- Successful [`package.yml` run 36183652625](https://github.com/TotalCross/totalcross/actions/runs/36183652625)
- Full SDK artifact: `TotalCross-7.2.2`, ID `10885666470`; downloaded ZIP SHA-256 `bb0f66a0a15fb6bf9eb5b8988140e712b39a17f00541102a2943074bd4bfe1b8`
- `tcvm.dll` SHA-256: `88f7bcb4b4cd252c6a36de09b42317926a0af1524b2a2167af741ff76f5547d9`
- Corpus digest: `af39fea695191a27` (663 files: 660 JPEG and 3 PNG payloads)

The machine-readable hashes and validation index are in [scroll-raster-reuse-windows-package.json](/tmp/totalcross-scroll-raster-reuse-windows/.agent/evidence/scroll-raster-reuse-windows-package.json).

## Validation

The focused runner/package contract checks (4 groups), frame-pacing contract tests (13 checks), image-scroll distributed-benchmark tests, packaging shell syntax, copyright-header validation, and `git diff --check` passed for the original package. The final SDK package workflow passed, including its Windows, Linux, and macOS deploy checks. The hotfix package was rebuilt from the same full SDK ZIP and corpus; the Windows launcher and `tcvm.dll` hashes match the original. The produced ZIP and replacement patch passed manifest/hash checks.

The operator run [scroll-raster-reuse-windows-results-20260926-222513-115.zip](/Users/flsobral/Downloads/scroll-raster-reuse-windows-results-20260926-222513-115.zip) passed: two correctness preflights, ten matching waypoints, final displacement 4095, and all six measured processes. Every ON sample recorded 142/142 reuse hits with zero fallbacks.

Across the three samples per mode, median frame-interval P95 changed by +0.24% in cold and -2.04% in warm. Active-work P95 changed by -40.21% in cold and -36.77% in warm. Screen-update P95 changed by +6.07% in cold and -9.05% in warm. The active-work reduction is consistent; frame-interval and screen-update changes are mixed, so the run does not support a blanket end-to-end speedup claim. PowerShell parsing and the benchmark ran on Windows; this macOS host did not execute them.
