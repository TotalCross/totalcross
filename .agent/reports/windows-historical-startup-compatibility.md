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

**Status: pending corrected Actions rerun.** Initial run
[45901886592](https://github.com/TotalCross/totalcross/actions/runs/45901886592)
downloaded all pinned inputs but stopped before the corrected Win32 build:
PowerShell treated the empty output from a clean Git status as null. Historical
A and B each started and exited 0 with a durable `PASS` result, but their
summaries were marked INCOMPLETE because marker parsing lacked `import re` and
the class-initialization marker was called from the static initializer. The
run did not produce a corrected I386 startup result, so ownership was NOT
EXECUTED. Attempt and artifact details are in the evidence index.

Those harness issues are corrected on the dedicated branch. The rerun will
again execute historical A, historical B, and corrected I386 sequentially in
one `windows-2022` job/session. It records provenance, SDK JAR and deployed TCZ
hashes, PE machine, process creation/PID/session, elapsed time, decimal and hex
exit codes, timeout, durable Java markers, PASS marker, DebugConsole, loaded
modules, events, and crash evidence. Artifact preparation failures remain
INCOMPLETE and do not stop later historical probes.

## Persistent ownership gate

The existing performance-lab lifecycle fixture is pinned to commit
`c4ba2f568df374f19a8bbc2a74dcfb598f2ac48e`. Its helper now accepts I386 and
checks the deployed launcher/`tcvm.dll` architecture and hashes. Its Java
fixture remains unchanged.

The ownership workload is not run unless corrected Win32 minimal startup
passes. When run, it verifies two consecutive screens of 1,510 controls,
immediate detach and complete owner release, subtree mutation and reattachment,
active reparenting, shared ownership, pipeline replacement, raster-cache reuse
and eviction, native draw output and red-pixel value, no owner-underflow
diagnostic, and clean process completion. Its Launcher/VM hashes must match
the corrected minimal-startup runtime.

**Production Win32 ownership verdict: pending.** Any skipped ownership run will
be reported as NOT EXECUTED, never PASS.

## AMD64 reference and limitations

The separate AMD64 comparison in run `45884551862` failed before Java on both
the original baseline and fixed ownership revision, with an access violation
at AMD64 RVA `0xB93A52` in the TCZ/heap error path. This is documented in
[`windows-image-ownership-crash-diagnostics.md`](windows-image-ownership-crash-diagnostics.md).
No AMD64 rerun is part of this validation, and that RVA will not be mapped onto
I386. GitHub-hosted I386 results describe the tested Windows runner, not every
customer desktop environment.

## Validation and remote operations

The initial Actions attempt passed both contract suites and retrieved the
historical artifacts; focused local harness tests (12), changed-file copyright
validation, and `git diff --check` pass for the corrections. Corrected Win32
build/deployment/native execution and the gated ownership smoke remain pending
the next dedicated Actions run. The I386-specific ownership helper is on the
dedicated performance-lab branch named in the evidence index. No production
ownership implementation changes are included.
