<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Windows native startup crash investigation

## Purpose / Big Picture

Determine whether Windows x64 exit 0xC0000005 happens before image ownership is
exercised, and identify the earliest failing native startup phase with
baseline-versus-fix evidence. Deliver a reproducible diagnostic workflow, a
minimal MainWindow probe, native crash artifacts and analysis, and a factual
handoff report. Do not change production runtime code.

The tested TotalCross revisions are baseline
6b915883dd202f6db891990f6c3af26fb645ecc7 and fixed
94af2b8b653cad509994b5ccf3dbb60ac28ef920. The instrumented performance-lab
functional fixture is commit 9d7d548916d2b74a68ee46886104b1d0dfa96c19.

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
  contract suite passes 14 tests; Windows execution remains pending Actions.
- Pinned the progressive fixture at
  9d7d548916d2b74a68ee46886104b1d0dfa96c19 and added the caller workflow with
  separate artifact suffixes for exact baseline/fixed canonical builds.

## Current Architecture and Scope

The canonical Windows build is build-windows in .github/workflows/build.yml,
using windows-2022, Visual Studio 2022, and Release configuration. The
diagnostic caller invokes that reusable workflow twice with the exact baseline
and fixed source SHAs. An opt-in artifact suffix keeps source, prepared
dependencies, native bridges, SDK archives, and Windows runtime artifacts
separate within one Actions run. The standard smoke job is suppressed for
these calls; the caller runs a minimal probe matrix and gates the progressive
image fixture on both minimal probes passing.
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

## Surprises & Discoveries

- The four original runs all built fixed source
  94af2b8b653cad509994b5ccf3dbb60ac28ef920. Run 45853745093 failed during
  deploy because the selected SDK JAR lacked tc.Deploy; runs 45856087270 and
  45857806319 deployed but emitted no app result marker; run 45858785421
  exited 0xC0000005. Their runner image versions and artifact hashes are
  recorded in the evidence index.
- The existing final Windows artifact proved the binary is AMD64, but the
  smoke job did not capture an unhandled-exception dump or native stack.
- Current Actions evidence does not establish whether the runner has a usable
  interactive graphics desktop; collect this empirically in the new job.

## Decision Log

- Use one dedicated diagnostic branch based on the existing Windows validation
  workflow commit; do not modify or rewrite the existing branch.
- Run both source revisions through the reusable canonical build in one
  diagnostic workflow. Add an optional input-controlled suffix to every
  artifact used by these calls so source/runtime artifacts cannot collide.
- Keep the integration fixture commit pinned. Never infer regression from
  temporal correlation; require the baseline run, fixed run, and crash
  location/stage evidence.
- Preserve crash dumps only as short-retention CI artifacts and record hashes;
  do not commit binaries, dumps, SDK packages, or raw logs.

## Validation and Acceptance

Required acceptance follows the user's attached matrix and report questions.
A source revision is connected to every SDK and native Windows artifact by
build provenance; Launcher.exe and tcvm.dll both inspect as PE AMD64; matching
PDBs are retained when supported. Each run records process status, distinct
host/VM/Java/initUI/success markers, module inventory, runner desktop/graphics
facts, and dump/debugger availability.

Run baseline and fixed minimum probes first. Only when both pass, execute the
baseline and fixed functional scenarios; baseline ownership-specific
assertions may fail, but the run must report the first reached stage. Never
label Windows functional ownership smoke PASS unless fixed revision assertions
and native rendering pass. Validate the fixture/workflow contracts, changed
headers, commit message, and git diff. Windows build/runtime/debug validation
is exclusively via Actions; no local Windows cross-compilation or execution.

## Risks and Open Questions

- Does the reusable workflow's artifact set remain downloadable by a caller
  job in the same Actions run? Confirm before designing artifact transfer.
- Does the Windows-2022 runner provide an interactive desktop, working SDL
  window/surface creation, and required DLLs? Treat workflow measurements as
  authoritative for this runner image.
- Are matching MSVC PDBs emitted by the current Release configuration? If not,
  enable diagnostic-only symbol output without changing runtime sources.
- Can ProcDump, CDB, or WinDbg be installed/launched in the hosted runner's
  service session? If not, report the exact limitation and preserve WER/event
  evidence as the fallback.
- Baseline ownership behavior is known incorrect; the full fixture may stop on
  an early assertion. Adapt only diagnostic fixture checks so it can report
  reached stages without suppressing fixed-revision assertions.

## Idempotence and Recovery

All downloaded previous-run evidence is stored in unique paths and preserved.
New Actions runs are immutable evidence. Re-run only a failed stage after
recording the prior run ID and why it is not authoritative. A workflow accepts
only the two hard-coded SHA values. It verifies source provenance before
launching and never mixes artifacts across runs. Keep all temporary fixture
and crash output in the workflow workspace and upload it with short retention.
Do not force-push, rebase, amend, reset, or clean unrelated worktrees.

## Outcomes & Retrospective

Pending Actions evidence and native crash analysis.

## Revision Note

Initial diagnostic plan. The prior investigation stopped before production
runtime changes; this plan preserves that boundary while adding native evidence.
