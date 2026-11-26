<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Historical Windows Win32 startup and ownership validation

## Scope

The historical customer-used Windows artifacts from runs `44414042826` and
`44565183218` are I386/Win32. The prior fixed artifact and crash investigation
from run `45884551862` are AMD64 and do not establish behavior on the production
Win32 target.

This validation uses the matching SDK/runtime pairs for historical A and B,
then builds fixed source `94af2b8b653cad509994b5ccf3dbb60ac28ef920` with the
canonical `Visual Studio 17 2022`, `-A Win32`, Release configuration. Production
rendering defaults remain enabled: Skia and SDL, with legacy rendering and
native windowing disabled. The fixed SDK and its Win32 runtime are tested
together.

## Artifact identities

| Revision | Source SHA | Run / SDK artifact | Windows runtime artifact | PE machine |
|---|---|---|---|---|
| Historical A | `61034a7562e7fa43df076dcc38bcd48128605395` | `44414042826` / `14479211695` | `44414042826` / `14478821824` | I386 |
| Historical B | `c7cf79575602f3cc19e107643f0fa39d3e151004` | `44565183218` / `14551617269` | `44565183218` / `14551309226` | I386 |
| Corrected Win32 | `94af2b8b653cad509994b5ccf3dbb60ac28ef920` | `45884551862` / `15148184230` | `45907759163` / `15158467793` | I386 |

All SDKs report TotalCross version 7.2.2. Exact artifact digests, local
archive/JAR hashes, historical Launcher/tcvm hashes, dependency bundle
identity, and native bridge identity are in
[`windows-historical-startup-compatibility.jsonl`](../evidence/windows-historical-startup-compatibility.jsonl).

## Startup comparison

**Startup status: PASS for all three I386 artifacts.** Final run
[45907759163](https://github.com/TotalCross/totalcross/actions/runs/45907759163)
executed historical A, historical B, and corrected production Win32 startup in
that order on one `windows-2022` runner session. All three processes started,
recorded all six durable Java markers and a durable `PASS` file, and exited
`0x00000000` without timing out or producing a dump.

| Runtime | Source commit | PE | PID / session | Duration | Result |
|---|---|---:|---:|---:|---|
| Historical A | `61034a7562e7fa43df076dcc38bcd48128605395` | I386 | 7092 / 2 | 1,455 ms | PASS |
| Historical B | `c7cf79575602f3cc19e107643f0fa39d3e151004` | I386 | 1972 / 2 | 236 ms | PASS |
| Corrected Win32 | `94af2b8b653cad509994b5ccf3dbb60ac28ef920` | I386 | 5944 / 2 | 224 ms | PASS |

The corrected runtime was built with `cmake .. -G "Visual Studio 17 2022" -A
Win32` and Release. CMake recorded Skia ON, SDL ON, legacy renderer OFF, native
windowing OFF, software surface ON, and GLES OFF. Launcher SHA-256 is
`9ffe796f6cfa5ceaf33beaccde5f158cb9e47304256642913894849485fc8e31`; tcvm
SHA-256 is `000d9d2ae734659383db9266b7e5c2db0e056de78ebbee68383142f7d797af52`.
The fixed SDK archive is artifact `15148184230` from run `45884551862`; its
JAR SHA-256 is `a18c80e2784543493370d1120afc3e2d10d7594b51e5032d1f779f1f51c2f12d`.
The build consumed prepared dependencies `15148893237` and native bridges
`15147749510` from the same run. The corrected runtime is artifact
[`15158467793`](https://github.com/TotalCross/totalcross/actions/runs/45907759163/artifacts/15158467793)
(`sha256:81c313cf8c6ab83c0a9b722a153f2b13e344358a91c99df806dcd613e64a73ec).

The accepted run uploaded
[`windows-historical-i386-validation-results`](https://github.com/TotalCross/totalcross/actions/runs/45907759163/artifacts/15158866549)
(`sha256:4d1f7ac1622ae4055b5816bdaa0ed74f99b46146bd4448ddef90710b43a7814c).
The run used performance-lab commit
`bf691c14d7c5fe807c62de98c7285f65d38de87e` to recognize marker files written
beside the app directory.

## Persistent ownership gate

The existing performance-lab lifecycle fixture is pinned to commit
`bf691c14d7c5fe807c62de98c7285f65d38de87e`. Its helper accepts I386, checks
the deployed launcher/`tcvm.dll` architecture and hashes, and discovers
diagnostic files beside the app directory when needed. Its Java fixture remains
unchanged.

The ownership workload runs only after corrected Win32 minimal startup passes.
In the accepted run, process 6196 completed in session 2 in 489 ms with exit
code `0x00000000`. It verified two consecutive screens of 1,510 controls,
immediate detach and complete owner release, subtree mutation and reattachment,
active reparenting, shared ownership, pipeline replacement, raster-cache reuse
and eviction, native draw output and red-pixel value, no owner-underflow
diagnostic, and clean process completion. Its Launcher/VM hashes must match
the corrected minimal-startup runtime.

**Production Win32 ownership verdict: PASS.** The helper reports two full screen
cycles, 3,020 controls, 0 mismatches, highest stage 7, `drawPixelRgb=16711680`,
zero owners after detach, and all cache/reparent/shared-owner assertions true.
The rendered PNG SHA-256 is
`961193a64a23a06ebe62da397919e4e04d752c4f1a6fa31c4e7170fd70d365b5`. The
ownership Launcher and `tcvm.dll` hashes match the corrected startup runtime.

## AMD64 reference and limitations

The separate AMD64 comparison in run `45884551862` failed before Java on both
the original baseline and fixed ownership revision, with an access violation
at AMD64 RVA `0xB93A52` in the TCZ/heap error path. This is documented in
[`windows-image-ownership-crash-diagnostics.md`](windows-image-ownership-crash-diagnostics.md).
No AMD64 rerun is part of this validation, and that RVA will not be mapped onto
I386. GitHub-hosted I386 results describe the tested Windows runner, not every
customer desktop environment.

## Validation and remote operations

The first two Actions attempts exposed and then isolated harness issues; both
are retained in the evidence index. Final run `45907759163` passed contract
tests, the production Win32 build, all three I386 startup probes, the complete
ownership smoke, and the final workflow gate. Focused local tests passed:
12 TotalCross diagnostic contracts and 17 ownership helper contracts. Header
validation, workflow YAML parsing, `git diff --check`, and commit-message
validation passed. The hosted run used Windows Server 2022 image
`20261116.370.1`; the build emitted no PDBs, and no dump was needed because no
native process failed. Diagnostic commits were pushed only to the dedicated
TotalCross and performance-lab branches. No PR, merge, release, production
branch push, or production ownership change was made.
