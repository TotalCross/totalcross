<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Windows native crash investigation

## Finding

**Classification: A — preexisting AMD64 TCVM startup failure on the tested
hosted image.** The original `origin/master` revision and the ownership-fix
revision both reproduce the same native write access violation before Java
application initialization. Matching PDBs resolve both stacks to the same
Windows CRT write instruction and the same TotalCross TCZ/heap-error path. The
probe creates no images or `ImageControl`s and does not use the image ownership
registry. This finding is architecture-specific; it does not establish a
regression in the Windows I386 runtime distributed to customers.

The failure occurs while `startVM` loads a TCZ module and looks up its
`ConstantPool`. `tczNewInstance` calls `privateHeapSetJump`, which copies the
source-file path into `m->ex.setjmpFile`. The CRT copy instruction attempts to
write to an invalid address. The evidence identifies this immediate failure
site, but does not establish why the `Heap`/destination became invalid upstream.

No production runtime code was changed. Both AMD64 probes fail before the
functional image workload is eligible to run, so ownership and rendering
results for AMD64 remain **NOT EXECUTED**.

## Priority correction: customer target is I386

The historical customer-used Windows artifacts from runs `44414042826` and
`44565183218` have PE machine `0x014c` (I386). The probes and crash dumps
documented below used AMD64 artifacts. Their matching x64 RVA and stack cannot
be mapped onto Win32, and the x64 finding does not answer whether persistent
image ownership works on the production Windows target.

The corrected I386 validation is tracked in
[`windows-historical-startup-compatibility.md`](windows-historical-startup-compatibility.md).
Its one-run workflow will compare both historical I386 pairs with a new
production-config Win32 build from `94af2b8b653cad509994b5ccf3dbb60ac28ef920`.
The existing ownership smoke runs only if that corrected I386 startup passes.
**I386 functional validation is pending the Windows Actions run.**

## Tested revisions and provenance

| Case | TotalCross source | Source tree | Fixture |
|---|---|---|---|
| Baseline / original master | `6b915883dd202f6db891990f6c3af26fb645ecc7` | `44047160610b7efcb3d49768736af95bde19f85f` | `804803a243db61a2ed54756e1f63cfc42ff4412b` |
| Fixed ownership revision | `94af2b8b653cad509994b5ccf3dbb60ac28ef920` | `539f5b424b6f4593c7d96074d69ff077a38ed361` | `804803a243db61a2ed54756e1f63cfc42ff4412b` |

At investigation time, a read-only `git ls-remote` confirmed
`origin/master` was exactly `6b915883dd202f6db891990f6c3af26fb645ecc7`.
The native sources containing the fault and callers—`mem.c`, `tcz.c`, and
`startup.c`—are unchanged between the baseline and fixed commits.

## Original four validation failures

All four original smoke runs used fixed revision
`94af2b8b653cad509994b5ccf3dbb60ac28ef920`:

| Run | Build/deploy and process result |
|---|---|
| [45853745093](https://github.com/TotalCross/totalcross/actions/runs/45853745093) | Build completed, deploy failed with `ClassNotFoundException: tc.Deploy`; no application process. The selected launcher and VM hashes were `c1736bdc…` and `d585a858…`. |
| [45856087270](https://github.com/TotalCross/totalcross/actions/runs/45856087270) | Deploy completed; no application startup marker or stdout. Exit status was not recorded. |
| [45857806319](https://github.com/TotalCross/totalcross/actions/runs/45857806319) | Deploy completed; no application startup marker or stdout. Exit status was not recorded. |
| [45858785421](https://github.com/TotalCross/totalcross/actions/runs/45858785421) | Build and deploy completed; Launcher exited `0xC0000005`. No Java assertion, `DebugConsole.txt`, rendered PNG, dump, or stack was collected. |

The first run was a deploy-selection error. The later runs narrowed the issue
to native startup, but could not initially show whether TCVM had loaded or
which native instruction failed.

## Diagnostic workflow results

The isolated workflow builds each exact revision through the canonical Windows
build, downloads those same-run artifacts, deploys the no-image startup probe,
and gates the larger image workload on both minimal probes passing.

| Run | Result |
|---|---|
| [45871774434](https://github.com/TotalCross/totalcross/actions/runs/45871774434) | Both builds/deploys passed and both probes exited `0xC0000005`. The runner had an interactive desktop, but the captured module snapshots had no `tcvm.dll`; no dump or matching application PDBs were available. |
| [45876788788](https://github.com/TotalCross/totalcross/actions/runs/45876788788) | Both probes again exited `0xC0000005` with `tcvm.dll` loaded. ProcDump postmortem registration was restored, but no dump was produced. The two probes received different hosted image versions. |
| [45879820691](https://github.com/TotalCross/totalcross/actions/runs/45879820691) | Baseline and fixed ran sequentially on one image. CDB caught the same second-chance write fault at TCVM RVA `0xB93A52`; a command-path parsing error prevented dump creation and the PDB path was relative to CDB's temporary directory. |
| [45881889694](https://github.com/TotalCross/totalcross/actions/runs/45881889694) | Corrected live CDB paths captured a 57 MB dump per revision and loaded private PDBs. The common stack resolved through TCZ loading; offline analysis still had a relative symbol path and produced a wrong OS-symbol bucket. |
| [45883413961](https://github.com/TotalCross/totalcross/actions/runs/45883413961) | Offline PDB loading and disassembly worked. Both revisions faulted at the same instruction and RVA. CDB's `!analyze` still emitted an invalid OS-symbol bucket, which was excluded from the conclusion. |
| [45884551862](https://github.com/TotalCross/totalcross/actions/runs/45884551862) | Final source-line comparison. Both builds/deploys passed on `win22` image `20261116.370.1`; both minimal probes failed identically and produced matching full-memory user dumps. PDBs mapped the TCVM callers to source lines. The image ownership workload was skipped by its gate. |

### Final AMD64 minimal comparison: run 45884551862

Both builds used **Visual Studio 17 2022 x64 Release**. The `Launcher.exe` and
`tcvm.dll` PE architecture was AMD64. The deployed `tcvm.dll` SHA matched the
same-run build artifact for each revision, and the selected PDBs were the
matching private PDBs for those binaries.

**Baseline**

- SDK source/tree: `6b915883dd202f6db891990f6c3af26fb645ecc7` /
  `44047160610b7efcb3d49768736af95bde19f85f`.
- SDK JAR SHA-256: `8a48ef094971f4cd8bdc51d188e59e7489082225401c047b0c4b6940d780372d`.
- Probe JAR SHA-256: `8f8b9b9eaeab5f9809daeb4dfc7382a7f04099e42c40a95a969e869920c130c8`.
- SDK/Windows provenance SHA-256:
  `0d44c378daa526678e4a78b553d4b0ab104d13c772522e89ea4294f20c07fd4e`.
- Build `Launcher.exe` SHA-256:
  `cd8f810622931ac03c35995e3d2acf2bf673e909c20010e63cc461dc3f20a529`.
- Deployed `WindowsNativeStartupProbe.exe` SHA-256:
  `f68150a06ab068d16b8ce5fab7df9f97344e65b78448cc8b9b6fa11bf735bca5`.
- Deployed `tcvm.dll` SHA-256:
  `a15f020cdfa32f36f2cbcfa05ac419ee995099c1624be130df908b4ac9a6c565`.
- Matching `Launcher.pdb` / `tcvm.pdb` SHA-256:
  `f09d957a72f4b55a00921f8c166bb07adc1d86f0a6d1586beaf4e22efc4bd4c2` /
  `44e5dde70f9b11da0f499c530e2f5816862bcd6b5af88aa177769a4b7b7629e5`.
- Result: `0xC0000005`, no startup marker; process duration 141 ms.
- `live-cdb.dmp`: 57,543,633 bytes, SHA-256
  `c4642255fc83f4b57fcaf2884e74708ebd7bc5c42e35e41b000f938c246cefa9`.

**Fixed**

- SDK source/tree: `94af2b8b653cad509994b5ccf3dbb60ac28ef920` /
  `539f5b424b6f4593c7d96074d69ff077a38ed361`.
- SDK JAR SHA-256: `a18c80e2784543493370d1120afc3e2d10d7594b51e5032d1f779f1f51c2f12d`.
- Probe JAR SHA-256: `b32a7a299d9fbca57f50606a08d2630398cd5bd4a56ea496739caf69d0d9f66d`.
- SDK/Windows provenance SHA-256:
  `4a56803ad63d619cb1a14d2100c9fa6369652b1bb8afce7d0e0361b3ae178b3a`.
- Build `Launcher.exe` SHA-256:
  `e031c26bb1384d79f07150bb274d9cfec6fd17fd646a1500a4c9546d8f3c7206`.
- Deployed `WindowsNativeStartupProbe.exe` SHA-256:
  `7c0441da14eba6375c89ced2a8024060393601e14ad64116602cc0af158ef1ea`.
- Deployed `tcvm.dll` SHA-256:
  `2b88417f1e4476cb0b47dbcd82769b7acc23f5c6c028af4cf75637a15d42d92f`.
- Matching `Launcher.pdb` / `tcvm.pdb` SHA-256:
  `54ce9957f9cdfb2135fb5137d6f188e8a5483fac961e5d83ed8e19f17e20c034` /
  `592c9ce281590f8e03e2f959954ec0cde69085e36ab388bbaafe999e4be7c7f5`.
- Result: `0xC0000005`, no startup marker; process duration 108 ms.
- `live-cdb.dmp`: 57,543,633 bytes, SHA-256
  `a29081f89dd45aef4cf44c89c8cac1150a5927a04bc76139b44371ae9f74d9e4`.

Crash artifacts, PDBs, manifests, logs, runner facts, and dump hashes are
preserved under:
`/Users/flsobral/.codex/windows-crash-diagnostics-new-runs/45884551862/`.
The CI artifact retention is seven days. ProcDump's postmortem registration
installed successfully and was restored; WER LocalDumps were configured. The
Windows Application Error/WER event query returned no records.

## Native exception and first failing phase

For both revisions, CDB resolved the fault to
`tcvm!__entry_from_strcat_in_strcpy+0x1f`. The same-host comparison in run
45883413961 measured the same module-relative RVA, `0xB93A52`; run 45884551862
resolved the same symbol and offset. The fault is an access violation on a
write. The instruction bytes are `48 89 04 0A`:

```text
mov qword ptr [rdx+rcx], rax
```

The attempted write targets an invalid `0x4f...` address in both dumps. The
first eight bytes in `rax` decode as the beginning of the Windows source path
`D:\a\to`, consistent with the source-file string being copied by
`privateHeapSetJump`.

The matching PDBs resolve the native call path as:

```text
tcvm!__entry_from_strcat_in_strcpy+0x1f
  [Microsoft UCRT strcat.asm:180]
tcvm!privateHeapSetJump+0x3c
  [PDB nearest source mapping: TotalCrossVM/src/util/mem.c:353]
tcvm!tczNewInstance+0x68
  [TotalCrossVM/src/util/tcz.c:177]
tcvm!tczFindName+0xa9
  [TotalCrossVM/src/util/tcz.c:254]
tcvm!tczLoad+0x8b
  [TotalCrossVM/src/util/tcz.c:385, ConstantPool lookup]
tcvm!startVM+0x1af
  [TotalCrossVM/src/init/startup.c:653, module loading loop]
tcvm!executeProgram+0xe
Launcher!executeProgram+0xf8 [inline]
Launcher!WinMain+0x278
```

The source expression in `privateHeapSetJump` is at `mem.c:350`:
`xstrcpy(m->ex.setjmpFile, ...)`. Its PDB's nearest line mapping is line 353.
That function is reached by `IF_HEAP_ERROR(ntcz->header->hheap)` in
`tczNewInstance`. `tczLoad` is loading a TCZ's `ConstantPool` while
`startVM` iterates runtime modules. The exception happens before the probe's
first Java marker, `MainWindow.initUI()`, any image creation, or native drawing.

The offline `!analyze -v` output still labels a wrong OS-symbol bucket
(`ntdll.wrong.symbols.dll`). That bucket is not used: it conflicts with the
exception record, the direct fault address, the instruction, and the matching
private TCVM PDB stack. The source-level stack and disassembly come from CDB's
live/offline PDB resolution, not that bucket.

## Runner and graphics findings

Run 45884551862 used the same hosted image for both probes:

- Windows Server 2022, image `win22` / `20261116.370.1`.
- Interactive process session 2, window station `WinSta0`, desktop `Default`.
- One Explorer and one Desktop Window Manager process.
- `Microsoft Hyper-V Video`, 1024×768.
- The diagnostic executable's imports include `VCRUNTIME140.dll`,
  `KERNEL32.dll`, and `USER32.dll`; `tcvm.dll` imports the Windows multimedia,
  socket, graphics, and system libraries recorded in `native-dependencies.txt`.

This disproves a completely noninteractive/no-desktop description of this
runner. It does **not** prove SDL, Skia, a window surface, or drawing works:
the process crashes in TCZ/heap bookkeeping before Java reaches `MainWindow`
or requests a surface. The failure is not evidence of a graphics limitation.

## Classification and next action

**A — preexisting AMD64 startup failure in the tested hosted environment.** The
original master reproduces the fault. Baseline and fixed fail at the same TCVM
instruction, PDB-resolved stack, and source call path. The crashing native
files are unchanged between the two source commits. The probe has no image
ownership operations, and execution stops before Java or graphics startup.
This is stronger evidence than matching exit codes alone and points away from
an ownership-triggered cause of the AMD64 startup failure. It does not establish
the behavior of production Win32/I386.

The immediate mechanism is an invalid write while `privateHeapSetJump` copies
the source path into a heap's exception-file buffer. The upstream reason the
`Heap`/destination is invalid remains unresolved. Do not patch image ownership
or add a CI graphics workaround based on this evidence.

The next useful AMD64-only diagnostic would stop in CDB at entry to
`privateHeapSetJump` and at `tczNewInstance` line 177, recording the x64
arguments (`Heap *m`, `file`, and `line`) and the addresses of
`ntcz->header->hheap` and `m->ex.setjmpFile`. That follow-up is deferred until
the production I386 comparison and gated ownership smoke are resolved. The
current priority is the I386 workflow referenced above; no production fix is
proposed from the AMD64 finding alone.

## Scenario comparison

| Scenario | Baseline | Fixed |
|---|---|---|
| Minimal native AMD64 Launcher startup | **FAIL**, `0xC0000005` | **FAIL**, `0xC0000005` |
| MainWindow initialization | **FAIL before entry** | **FAIL before entry** |
| Basic image drawing | **NOT EXECUTED**, gated | **NOT EXECUTED**, gated |
| Ownership lifecycle smoke | **NOT EXECUTED**, gated | **NOT EXECUTED**, gated |
| Native crash diagnostics | **Available**, dump + matching PDB | **Available**, dump + matching PDB |

## Diagnostic branch, files, and commits

TotalCross diagnostic branch:
`codex/image-ownership-windows-crash-diagnostics`, based on the existing
Windows validation branch at `f417e2513ffdf024d83885498cc68e9ed9fb60d5`.
Final diagnostic HEAD:
`9ca20fa3c3de9bc97c5ec7f0b130db4ff1b7e6d0`.

Logical commits added on that branch:

- `210f457e8` — `test(windows): add native crash diagnostic harness`
- `e2fb0f137` — `ci(windows): trigger diagnostics on dedicated branch`
- `04041664b` — `test(windows): capture native startup crash stacks`
- `1728e6fae` — `test(windows): debug both startup probes on one runner`
- `d76135bd9` — `fix(windows): resolve CDB diagnostic paths`
- `31d6c1503` — `fix(windows): analyze dumps with local PDBs`
- `9ca20fa3c` — `test(windows): map callers in crash stack`

The companion performance-lab diagnostic branch is
`codex/windows-image-ownership-crash-diagnostics`, ending at
`804803a243db61a2ed54756e1f63cfc42ff4412b`. It contains the crash-stage
fixture and optional dump-tool handling. The implementation branch and
production image ownership files were not changed by this investigation.

Performance-lab files changed on that branch:

- `tests/image-ownership/README.md`
- `tests/image-ownership/src/totalcross/ui/image/ImageOwnershipLifecycleProbe.java`
- `tests/test_image_ownership_smoke_contract.py`
- `tools/image_ownership_smoke.py`

TotalCross files changed relative to the diagnostic branch base:

- `.agent/evidence/windows-image-ownership-crash-diagnostics.jsonl`
- `.agent/plans/windows-image-ownership-crash-diagnostics.md`
- `.agent/reports/windows-image-ownership-crash-diagnostics.md`
- `.agent/state/windows-image-ownership-crash-diagnostics.md`
- `.github/workflows/build.yml`
- `.github/workflows/windows-native-crash-diagnostics.yml`
- `scripts/install-windows-crash-tools.ps1`
- `scripts/windows-native-crash-diagnostics.py`
- `scripts/windows-native-crash-runner.ps1`
- `tests/test_windows_native_crash_diagnostics.py`
- `tests/windows-crash-diagnostics/src/totalcross/ui/WindowsNativeStartupProbe.java`

Performance-lab diagnostic commits include `9d7d548`, `4954ad9`, and
`804803a`. No dumps, PDBs, SDK distributions, generated dependency files, or
temporary logs were committed.

## Validation and remote operations

Passed local checks:

- `python3 -B -m unittest discover -s tests -p test_windows_native_crash_diagnostics.py -v` — 8 passed.
- Performance-lab smoke contract suite — 14 passed.
- Ruby parsed `build.yml` and `windows-native-crash-diagnostics.yml`.
- Focused copyright-header validation — 3 applicable files, 0 changed.
- `git diff --check` and staged diff checks passed.
- The HEAD commit message validator passed. Commit `31d6c1503` was created
  after its validator emitted one 81-character body-line warning; history was
  not rewritten, and later commits have warning-free messages.

Remote operations performed:

- Read-only `git ls-remote` confirmed `origin/master` at
  `6b915883dd202f6db891990f6c3af26fb645ecc7`; a read-only check found no remote
  `fix/image-persistent-ownership-scaling` ref. A read-only check confirmed
  the companion diagnostic branch at
  `804803a243db61a2ed54756e1f63cfc42ff4412b`.
- Pushed the diagnostic commits listed above to
  `codex/image-ownership-windows-crash-diagnostics` to trigger the Windows
  workflow runs.
- Pushed the companion fixture commits to
  `codex/windows-image-ownership-crash-diagnostics`.
- Ran the approved `TotalCrossVM/deps/fetch-depot-tools.sh` dependency fetch;
  it completed successfully and produced no tracked dependency changes.
- Ran Windows Actions diagnostics `45871774434`, `45876788788`,
  `45879820691`, `45881889694`, `45883413961`, and `45884551862`, in addition
  to reviewing the four original runs listed above.

No push to `master` or `main`, PR, merge, tag, release, or force-push occurred.
No Windows executable was built, deployed, run, or debugged locally. The final
workflow and artifacts are available at
[run 45884551862](https://github.com/TotalCross/totalcross/actions/runs/45884551862).
