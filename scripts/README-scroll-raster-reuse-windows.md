<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Windows scroll raster reuse benchmark

Unzip this package, open PowerShell 5.1 in the extracted package directory, and
run:

```powershell
powershell -ExecutionPolicy Bypass -File .\run-scroll-raster-reuse-windows.ps1
```

The runner validates the 663-image corpus and package, runs OFF/ON correctness
preflights, then starts six measured processes in this order: OFF three times,
then ON three times. Measured diagnostic accounting stays disabled. The result
ZIP is written under `results` on both success and failure. It contains both
process summaries and separate cold/warm pass summaries, with OFF-to-ON median
comparisons for each pass.

The workload uses a logical 540 × 960 window. The runner records the drawable
size, scale, SDL pixel format, renderer, and refresh rate reported by this
machine; physical display dimensions may vary.
