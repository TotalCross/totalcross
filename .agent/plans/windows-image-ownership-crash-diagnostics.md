<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Windows persistent image ownership validation

## Purpose / Big Picture

Validate persistent image ownership on the production Windows Win32/I386
target. The earlier x64 investigation is retained as architecture-specific
evidence, but cannot establish customer-target behavior. Deliver a reproducible
historical-I386-versus-corrected-I386 startup comparison, run the ownership
smoke only after corrected startup passes, and record a factual handoff. Do not
change production runtime code.

The tested TotalCross revisions are baseline
6b915883dd202f6db891990f6c3af26fb645ecc7 and fixed
94af2b8b653cad509994b5ccf3dbb60ac28ef920. The instrumented performance-lab
Win32 fixture is commit bf691c14d7c5fe807c62de98c7285f65d38de87e.

## Working Set and Resume Protocol

Read .agent/state/windows-image-ownership-crash-diagnostics.md first. It names
the active milestone, current branch, next command, and validation. Append
compact run/artifact records to
.agent/evidence/windows-image-ownership-crash-diagnostics.jsonl. Keep the
final user-facing technical narrative in
.agent/reports/windows-image-ownership-crash-diagnostics.md. The prior four
Actions logs and downloaded artifacts are preserved under
/Users/flsobral/.codex/windows-crash-diagnostics-prior-runs/<run-id>/.

## Progress

- Audited the original worktree and existing specialized worktrees; created
  codex/image-ownership-windows-crash-diagnostics at
  f417e2513ffdf024d83885498cc68e9ed9fb60d5 without changing existing branches.
- Downloaded all four prior Actions logs and artifact sets into new, per-run
  evidence directories. The final run reports 3221225477 (0xC0000005) with no
  app result marker, DebugConsole.txt, or rendered PNG. Reconciled the source
  commits, fixture commits, runner-image versions, build/deploy outcomes, and
  runtime artifact hashes in the evidence index.
- Added and contract-checked the minimal startup probe, Windows crash runner,
  provenance collector, and progressive ownership fixture. The fixture
  contract suite passes 14 tests; the first Actions run now shows the crash
  occurs in the no-image probe before TCVM loads, but the native stack is still
  missing.
- Pinned the progressive fixture at
  804803a243db61a2ed54756e1f63cfc42ff4412b and added the caller workflow with
  separate artifact suffixes for exact baseline/fixed canonical builds.
- The first isolated baseline/fixed run, 45871774434, built and deployed both
  exact revisions. Both minimal probes exited 0xC0000005 before Java markers;
  neither loaded tcvm.dll. The runner exposed an interactive WinSta0\Default
  desktop with Explorer/DWM and a Hyper-V 1024x768 display. ProcDump's wait
  monitor captured no dump, and the artifacts had no matching Launcher/tcvm
  PDBs. The current slice adds AeDebug postmortem capture, CDB installation and
  analysis, Release linker symbols, and matching-PDB selection.
- The next isolated run, 45876788788, also built/deployed both exact revisions
  and both exited 0xC0000005 before Java markers, now with tcvm.dll visible in
  the module snapshots. ProcDump successfully registered and restored AeDebug;
  CDB was already installed, and both matching PDBs were retained, but no dump
  or Windows Error Event appeared. Baseline and fixed used different hosted
  image versions, so their runner environments were not identical. Add a CDB
  live-debug rerun to capture the second-chance exception and stack.
- Run 45879820691 built/deployed both exact revisions and ran them sequentially
  on the same win22 image 20261123.376.1. Both exited 0xC0000005 before Java
  markers. Live CDB caught the same invalid-pointer write at tcvm RVA 0xB93A52
  and the same relative native stack for baseline and fixed. The full dump was
  not created because CDB parsed backslashes in its command path as control
  escapes; PDB lookup also used a relative path from CDB's temporary directory.
  The local correction resolves the PDB/cache paths absolutely and uses
  forward slashes for CDB paths. A rerun is required for the source-level stack.
- Run 45881889694 captured 57 MB dumps for both revisions and loaded both
  matching private PDBs in live CDB. Each stack resolves through
  `__entry_from_strcat_in_strcpy`, `privateHeapSetJump`, `tczNewInstance`,
  `tczFindName`, `tczLoad`, and `startVM` before Java entry. Both faults share
  RVA 0xB93A52. The separate offline CDB pass still had the old relative PDB
  path and a bad remote symbol store, so the next slice fixes offline symbol
  setup and requests source-line/disassembly output from the local PDBs.
- Run 45883413961 captured 55 MB dumps on the same hosted image version for
  both revisions. The faulting instruction resolves to Microsoft CRT
  `strcat.asm:180`, `mov qword ptr [rdx+rcx],rax`, at the same tcvm RVA
  0xB93A52; its write target is invalid in both processes. The PDB stack again
  runs through `privateHeapSetJump`, `tczNewInstance`, `tczFindName`, `tczLoad`,
  and `startVM`. Offline CDB now loads the matching local PDBs and shows
  registers/disassembly, but `!analyze` still emits a wrong OS-symbol bucket.
  The current slice explicitly asks `ln` for the TotalCross caller and TCZ
  frames to capture their source lines before classification and handoff.
- Run 45884551862 resolved the complete source path in matching private PDBs
  for both revisions. `privateHeapSetJump+0x3c` maps to `mem.c:353` (the source
  `xstrcpy(m->ex.setjmpFile, ...)` is at line 350), called by
  `tczNewInstance` at `tcz.c:177` while `tczLoad` searches the ConstantPool at
  line 385 during `startVM`'s module loop at `startup.c:653`. The fault remains
  at the same CRT write instruction/RVA in baseline/master and fixed. Classify
  as A, a preexisting Windows TCVM startup defect; the invalid heap destination
  is the confirmed immediate mechanism, while the upstream source of that
  invalid pointer remains unisolated. The functional fixture remains gated.
- Drafted `.agent/reports/windows-image-ownership-crash-diagnostics.md` with
  the four historical failures, exact source/artifact provenance, all
  diagnostic runs, dump hashes, symbolized call path, runner findings, category
  A classification, comparison matrix, remaining uncertainty, and next
  diagnostic action. Final checks and a report-only handoff commit remain.
- Committed the AMD64-only handoff report locally as `231eb8deb`. That
  architecture-specific diagnostic milestone remains complete; the report-only
  commit was not pushed to avoid a redundant build. The priority Win32/I386
  validation is recorded in the next progress checkpoint.
- Priority correction: verified customer Windows artifacts from runs
  `44414042826` and `44565183218` against their clean source commit/tree,
  producer run ID, SDK/Windows build ID, archive and executable hashes. Both
  `Launcher.exe` and `tcvm.dll` are PE I386 (`0x014c`); neither run retained
  matching PDB/MAP files. Added a canonical `-A Win32` Release build and a
  same-runner historical/corrected I386 startup comparison. The functional
  ownership smoke consumes the exact corrected artifacts and is gated on all
  corrected startup markers plus normal exit. This workflow has not run yet;
  production Win32 acceptance remains INCOMPLETE.

## Current Architecture and Scope

The canonical Windows build is build-windows in .github/workflows/build.yml,
using windows-2022, Visual Studio 2022, and Release configuration. The
diagnostic caller invokes that reusable workflow twice with the exact baseline
and fixed source SHAs. An opt-in artifact suffix keeps source, prepared
dependencies, native bridges, SDK archives, and Windows runtime artifacts
separate within one Actions run. The standard smoke job is suppressed for
these calls; the caller runs baseline and fixed minimal probes sequentially in
one Windows job and gates the progressive image fixture on both passing.
The artifact-suffix input and diagnostic PDB preservation are branch-scoped
capabilities. Remove them when retiring this workflow unless maintainers adopt
multi-revision diagnostic builds as a supported canonical workflow use case.

The diagnostic app must write durable markers before initUI, at initUI entry,
and after successful initialization, and request clean exit. A Windows-side
launcher/process marker and process status distinguish native process launch
from VM and Java entry. Crash capture must cover failures before app code.

Allowed edits are confined to diagnostic fixture/script, workflow/config,
artifact handling, and startup instrumentation. Do not change TotalCross
production Launcher, TCVM, ImageDrawingBridge, ImagePipeline, ImageControl,
Container, or the ownership implementation. All Windows compilation,
deployment, execution, and debugging occur through GitHub Actions.

## Plan of Work

1. Reconcile all prior runs: source and lab SHAs, runner image, build/deploy
   result, executable/DLL identity, PE architecture, process result, markers,
   and runtime dependencies. Keep the raw logs and artifacts outside Git.
2. Add a self-contained diagnostic app and contract test. Confirm host-process
   start, TCVM-to-Java entry, initUI entry, and clean application completion
   are separate durable records. Add a runner probe for session, window station,
   desktop, graphics/display, SDL/Skia runtime, and loaded modules.
3. Extend the canonical reusable build workflow only as needed to preserve
   matching PDBs and expose its same-run SDK/Windows artifacts while skipping
   the existing ownership job until diagnostics complete. Document any
   temporary capability and its upstream/removal condition.
4. Add a branch-scoped push and workflow_dispatch diagnostic workflow. It
   builds both exact source SHAs with the canonical build, verifies provenance
   and hashes, deploys and runs the minimal probe under ProcDump or WER
   LocalDumps, and archives short-retention logs, dumps, module lists, runner
   environment, Event Log records, and integrity hashes. Analyze dumps
   using WinDbg/CDB where available.
5. Run baseline and fixed minimal probes in the same Actions run. If both
   succeed, run the pinned functional fixture for both revisions with the same
   launcher/runtime configuration. Baseline ownership assertion failures are
   recorded as expected; do not count them as functional success. Stop before
   the large fixture if either minimal probe crashes.
6. If startup passes, instrument or adapt the diagnostic fixture to report
   progressive stages and run only as far as needed to identify first
   divergence. Investigate the original Actions runner's graphical/session
   capabilities empirically; do not substitute JavaSE/AWT.
7. Classify evidence A-F from the request, update the final report and evidence
   index, validate diagnostic contracts, headers, commit messages and diffs,
   then make focused logical commits. Push only the dedicated branch and run
   Actions there. No PR is needed unless a workflow trigger requires it.

### Priority correction: production Win32/I386 validation

The attached correction takes precedence over the earlier x64-only steps
above. Do not trigger another AMD64 diagnostic run. The x64 results remain
historical evidence for a separate architecture-specific failure.

1. Use the already published historical Windows artifacts from Actions runs
   `44414042826` and `44565183218`; verify source provenance and PE I386
   architecture before execution.
2. Build only corrected source `94af2b8b653cad509994b5ccf3dbb60ac28ef920`
   using the canonical Windows Release configuration with CMake `-A Win32`.
   Preserve the production renderer and retain matching PDBs for diagnosis.
3. On one `windows-2022` runner, deploy the minimal probe with each historical
   SDK/runtime pair and with the corrected SDK/runtime pair. Record source
   SHA/tree, SDK and TCZ hashes, PE machine, stage markers, signed and unsigned
   process exit codes, and dump/debugger evidence. Corrected startup must
   complete every marker and exit zero before ownership work can run.
4. After corrected startup passes, run the existing 1,510-control ownership
   fixture against the same corrected SDK and I386 runtime artifacts. Require
   the lifecycle, cache, rendering, pixel, and process-exit assertions to pass.
5. If corrected startup fails, retain its I386 dump and compare the exception
   and stack with both historical I386 runs. Do not interpret an AMD64 RVA as
   an I386 symbol. Historical artifacts have no matching PDB or MAP files, so
   report the symbolization limit if their faulting functions cannot be named.

## Surprises & Discoveries

- The four original runs all built fixed source
  94af2b8b653cad509994b5ccf3dbb60ac28ef920. Run 45853745093 failed during
  deploy because the selected SDK JAR lacked tc.Deploy; runs 45856087270 and
  45857806319 deployed but emitted no app result marker; run 45858785421
  exited 0xC0000005. Their runner image versions and artifact hashes are
  recorded in the evidence index.
- The existing final Windows artifact proved the binary is AMD64, but the
  smoke job did not capture an unhandled-exception dump or native stack.
- Run 45871774434 establishes that the runner has an interactive desktop and
  display adapter, but does not establish whether native rendering or SDL
  surface creation works. The minimal probes fail before loading TCVM.
- ProcDump's `-w` monitor missed short-lived crashes. Use its AeDebug
  postmortem registration and uninstall it after each run; retain WER
  LocalDumps as a fallback. The next run must verify capture and cleanup.
- The first Release artifact contained toolchain PDBs but no Launcher/tcvm
  PDBs. Diagnostic builds now set Release linker debug flags and artifact
  preparation copies only unambiguous matching PDBs.
- Run 45879820691's CDB `!analyze -v` classified both exceptions as
  `INVALID_POINTER_WRITE_c0000005_tcvm.dll!Unknown`. Baseline and fixed faulted
  at the same module-relative offset (0xB93A52) before any Java marker. This
  strongly locates the failure in pre-Java native startup, but without loaded
  matching PDBs it does not identify a source function or establish the
  ownership fix's relationship to the fault.
- Run 45881889694's live CDB loaded private symbols and resolved the common
  first frame as `tcvm!__entry_from_strcat_in_strcpy+0x1f`; the subsequent
  frames are `privateHeapSetJump`, `tczNewInstance`, `tczFindName`, `tczLoad`,
  `startVM`, and `executeProgram`. The app is still in native TCZ/constant-pool
  loading, before the probe's first Java marker. Both dumps are 57 MB; their
  SHA-256 hashes and matching PDB identities are in the evidence index. The
  offline pass's symbol path remains faulty and produced a misleading wrong
  OS-symbol bucket, so do not use that bucket for root-cause classification.
- Run 45883413961 confirms the faulting instruction and write access for both
  source revisions. CDB resolves the exception address to the same CRT source
  line and TCVM-relative stack; register state shows the attempted write
  target is invalid in both processes. The `!analyze` OS-symbol bucket remains
  wrong, so classify from the exception record, direct instruction, matching
  TotalCross PDBs, and identical baseline/fixed stack instead.
- Run 45884551862 resolves the native caller lines in both exact builds:
  `privateHeapSetJump` at `TotalCrossVM/src/util/mem.c:353` (copy expression at
  line 350), `tczNewInstance` at `TotalCrossVM/src/util/tcz.c:177`,
  `tczFindName` at line 254, `tczLoad` ConstantPool lookup at line 385, and
  `startVM` module loading at `TotalCrossVM/src/init/startup.c:653`. The same
  invalid heap exception-file destination is therefore reached on the original
  master and fixed revisions before any Java or graphics startup stage.
  Classification A is supported; retain the uncertainty about what corrupted
  or invalidated the heap pointer upstream.

## Decision Log

- Use one dedicated diagnostic branch based on the existing Windows validation
  workflow commit; do not modify or rewrite the existing branch.
- Run both source revisions through the reusable canonical build in one
  diagnostic workflow. Add an optional input-controlled suffix to every
  artifact used by these calls so source/runtime artifacts cannot collide.
- Keep the integration fixture commit pinned. Never infer regression from
  temporal correlation; require the baseline run, fixed run, and crash
  location/stage evidence.
- Run baseline and fixed minimal probes sequentially in one Actions job; the
  previous matrix received different hosted Windows image versions within one
  run, which weakened the environment comparison.
- Prefer ProcDump postmortem dumps for pre-TCVM failures; install CDB from the
  Windows SDK when it is absent and analyze with local matching PDBs plus the
  Microsoft symbol server. WER LocalDumps and the module/Event Log reports
  remain fallback evidence. When ProcDump/WER produce no dump, rerun the
  failed minimal probe under CDB in a copied runtime directory and request a
  dump on second-chance access violation.
- Preserve crash dumps only as short-retention CI artifacts and record hashes;
  do not commit binaries, dumps, SDK packages, or raw logs.

## Validation and Acceptance

Required acceptance follows the user's attached matrix and report questions.
A source revision is connected to every SDK and native Windows artifact by
build provenance. For the current production acceptance, Launcher.exe,
tcvm.dll, and the deployed probe must inspect as PE I386 (`0x014c`); SDK and
TCZ identities must match the same source revision. Corrected Release PDBs are
retained for diagnosis. Each probe records process status, distinct
host/VM/Java/initUI/success markers, module inventory, runner desktop facts,
and dump/debugger availability.

Run both historical I386 probes and the corrected I386 probe first on one
runner. Gate ownership work only on corrected startup passing, and use the same
corrected SDK/runtime artifact identities. Never label production Windows
functional ownership PASS unless every fixed fixture assertion, image draw,
pixel check, and normal process exit passes. Validate fixture/workflow
contracts, changed headers, commit message, and staged diff. Windows
build/runtime/debug validation is exclusively via Actions; no local Windows
cross-compilation or execution.

## Risks and Open Questions

- Does the reusable workflow's artifact set remain downloadable by a caller
  job in the same Actions run? Confirm before designing artifact transfer.
- Does the Windows-2022 runner provide an interactive desktop, working SDL
  window/surface creation, and required DLLs? Treat workflow measurements as
  authoritative for this runner image.
- Does Release `/DEBUG:FULL` emit matching Launcher/tcvm PDBs in the next
  canonical build artifact?
- Can ProcDump register as the AeDebug postmortem handler, and can the Windows
  SDK install CDB silently on the hosted runner? If not, report the exact
  limitation and preserve WER/Event Log evidence as the fallback.
- Can the CDB live rerun capture an exception stack/dump when ProcDump's
  postmortem handler produces no dump? The next run will test this directly.
- The hosted baseline/fixed runner image versions differed in run 45876788788;
  the next workflow iteration now runs both minimal probes in one job to
  guarantee a shared Windows image and desktop session.
- Baseline ownership behavior is known incorrect; the full fixture may stop on
  an early assertion. Adapt only diagnostic fixture checks so it can report
  reached stages without suppressing fixed-revision assertions.
- Historical I386 artifacts from runs 44414042826 and 44565183218 have
  provenance and PE I386 images, but no matching PDB or MAP files. The
  corrected I386 build emits PDBs; historical failure stacks may remain
  module-relative if those artifacts fault.

## Idempotence and Recovery

All downloaded previous-run evidence is stored in unique paths and preserved.
New Actions runs are immutable evidence. Re-run only a failed stage after
recording the prior run ID and why it is not authoritative. A workflow accepts
only the two hard-coded SHA values. It verifies source provenance before
launching and never mixes artifacts across runs. Keep all temporary fixture
and crash output in the workflow workspace and upload it with short retention.
Do not force-push, rebase, amend, reset, or clean unrelated worktrees.

## Outcomes & Retrospective

The AMD64 comparison is complete and remains architecture-specific historical
evidence. Production Win32/I386 acceptance remains INCOMPLETE until the new
Actions workflow builds the corrected source, runs all three startup probes,
and, if corrected startup passes, completes every ownership/rendering assertion.

## Revision Note

Priority correction dated 2026-11-27: validate production Win32/I386 before
continuing any AMD64 investigation. The prior x64 investigation stopped before
production runtime changes; this plan preserves that boundary.
