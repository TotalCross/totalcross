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
| Corrected Win32 | `94af2b8b653cad509994b5ccf3dbb60ac28ef920` | `45884551862` / `15148184230` | Built in the validation run from pinned dependencies and bridges | I386 expected; verify in run |

All SDKs report TotalCross version 7.2.2. Exact artifact digests, local
archive/JAR hashes, historical Launcher/tcvm hashes, dependency bundle
identity, and native bridge identity are in
[`windows-historical-startup-compatibility.jsonl`](../evidence/windows-historical-startup-compatibility.jsonl).

## Startup comparison

**Startup status: PASS for all three I386 artifacts.** Run
[45902867357](https://github.com/TotalCross/totalcross/actions/runs/45902867357)
executed historical A, historical B, and corrected production Win32 startup in
that order on one `windows-2022` runner session. All three processes started,
recorded all six durable Java markers and a durable `PASS` file, and exited
`0x00000000` without timing out or producing a dump.

| Runtime | Source commit | PE | PID / session | Duration | Result |
|---|---|---:|---:|---:|---|
| Historical A | `61034a7562e7fa43df076dcc38bcd48128605395` | I386 | 2876 / 2 | 1,558 ms | PASS |
| Historical B | `c7cf79575602f3cc19e107643f0fa39d3e151004` | I386 | 4716 / 2 | 236 ms | PASS |
| Corrected Win32 | `94af2b8b653cad509994b5ccf3dbb60ac28ef920` | I386 | 2600 / 2 | 232 ms | PASS |

The corrected runtime was built with `cmake .. -G "Visual Studio 17 2022" -A
Win32` and Release. CMake recorded Skia ON, SDL ON, legacy renderer OFF, native
windowing OFF, software surface ON, and GLES OFF. Launcher SHA-256 is
`970f09146e877141f0f4ea2a52cfb545a0e348abb8733b386b91ef05b10deba2`; tcvm SHA-256
is `bf843e6c78ef110f9f44a41d014b327392887e42230743b8bd3b5455261973c7`.

**Ownership wrapper status: rerun pending.** The native fixture reached stage 7,
exited 0, and wrote a durable JSON `PASS` containing every required ownership,
cache, detach/reparent, and drawing assertion. The wrapper failed to recognize
its stage files because they were written beside `install/win32/` as
`install/win32tcdiag-*`. Launcher/tcvm hashes matched the corrected startup
runtime. The helper now checks both locations in performance-lab commit
`bf691c14d7c5fe807c62de98c7285f65d38de87e`; a final Actions rerun is needed to
produce a passing wrapper summary. Full attempt details and artifact digests
are in the evidence index.

## Persistent ownership gate

The existing performance-lab lifecycle fixture is pinned to commit
`c4ba2f568df374f19a8bbc2a74dcfb598f2ac48e`. Its helper now accepts I386 and
checks the deployed launcher/`tcvm.dll` architecture and hashes. Its Java
fixture remains unchanged.

The ownership workload runs only after corrected Win32 minimal startup passes.
In run `45902867357`, it verified two consecutive screens of 1,510 controls,
immediate detach and complete owner release, subtree mutation and reattachment,
active reparenting, shared ownership, pipeline replacement, raster-cache reuse
and eviction, native draw output and red-pixel value, no owner-underflow
diagnostic, and clean process completion. Its Launcher/VM hashes must match
the corrected minimal-startup runtime.

**Production Win32 ownership verdict: functionally PASS in the fixture; final
wrapper acceptance pending.** The durable fixture JSON reports two full screen
cycles, 3,020 controls, 0 mismatches, highest stage 7, `drawPixelRgb=16711680`,
zero owners after detach, and all cache/reparent/shared-owner assertions true.
The helper's false FAIL is due only to marker-path discovery and will be
rechecked after its dedicated fix.

## AMD64 reference and limitations

The separate AMD64 comparison in run `45884551862` failed before Java on both
the original baseline and fixed ownership revision, with an access violation
at AMD64 RVA `0xB93A52` in the TCZ/heap error path. This is documented in
[`windows-image-ownership-crash-diagnostics.md`](windows-image-ownership-crash-diagnostics.md).
No AMD64 rerun is part of this validation, and that RVA will not be mapped onto
I386. GitHub-hosted I386 results describe the tested Windows runner, not every
customer desktop environment.

## Validation and remote operations

The initial Actions attempt exposed three harness defects and is recorded in the
evidence index. The corrected Actions run passed both contract suites and the
Win32 build/startup; the smoke fixture assertions passed while marker discovery
failed. Focused local harness tests (12), ownership helper contracts (17),
changed-file copyright validation, and `git diff --check` pass. One final
Actions run with the corrected helper is pending. No production ownership
implementation changes are included.
