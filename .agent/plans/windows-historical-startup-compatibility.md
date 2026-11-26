<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Validate historical and corrected Windows Win32 artifacts

This ExecPlan follows `AGENTS.md` and `.agent/PLANS.md`.

## Purpose / Big Picture

Run the exact November 18 and November 19 customer-used I386 artifact pairs,
then build source `94af2b8b653cad509994b5ccf3dbb60ac28ef920` as production
Win32/I386 and run it with its matching SDK. Execute all three minimal startup
probes in order on one `windows-2022` job and runner session. If corrected I386
startup passes, run the existing persistent image ownership smoke immediately
against that same Win32 runtime. Do not extend the AMD64 investigation; preserve
its existing finding as a separate architecture-specific reference.

## Working Set and Resume Protocol

- `.agent/state/windows-historical-startup-compatibility.md` is the first read
  on resumption; it records active work and the next action.
- `.agent/evidence/windows-historical-startup-compatibility.jsonl` is the
  append-only source for artifact identities, hashes, and Actions results.
- `.agent/reports/windows-historical-startup-compatibility.md` is the final
  evidence handoff. The existing `.agent/reports/windows-image-ownership-crash-diagnostics.md`
  retains the earlier AMD64 crash finding and will receive a short I386 update.
- `.github/workflows/windows-historical-i386-validation.yml` builds corrected
  Win32 and executes the ordered same-runner comparison and gated smoke.
- `scripts/build-windows-win32-diagnostic.ps1` builds only the corrected
  production I386 runtime from its exact source commit and pinned generated
  inputs.
- `scripts/run-windows-i386-validation.ps1` prepares and runs A, B, and fixed
  I386, then invokes the ownership smoke only after a fixed startup PASS.
- `scripts/windows-native-crash-diagnostics.py` records PE/provenance, deploys
  the shared minimal probe, and classifies durable markers and process status.
- `scripts/windows-native-crash-runner.ps1` captures process facts and uses
  architecture-correct CDB commands if a process fails.
- The ownership smoke source/tool is pinned at
  `TotalCross/totalcross-performance-lab` commit
  `c4ba2f568df374f19a8bbc2a74dcfb598f2ac48e`. It extends the existing I386
  architecture checks without changing the Java functional fixture.
- The earlier crash report and `.agents/skills/logical-commits/SKILL.md` hold
  background and commit guidance; reread only if needed for interpretation or
  the next commit.

## Progress

- [x] Read the original request, priority correction, repository rules, plan
  rules, diagnostic report, and existing crash harness. Confirmed the customer
  artifacts are I386 and the previous current artifact/run was AMD64.
- [x] Retrieved the exact historical A/B pairs and fixed SDK/runtime artifacts;
  confirmed source trees, SDK version, PE architecture, artifact IDs, hashes,
  and run provenance. Details remain in the evidence index.
- [x] Located the existing persistent ownership smoke and added I386 support to
  its pinned diagnostic helper. The Java lifecycle fixture remains unchanged;
  15 contract checks pass. Diagnostic helper commits are on the dedicated
  performance-lab branch recorded in the evidence index.
- [x] Corrected the minimal probe to join marker paths with `Convert.appendPath`,
  report write failures through `Vm.debug`, and require a durable PASS marker.
- [x] Complete and statically validate the one-job Win32 workflow, build helper,
  result harness, report/state updates, and focused tests. The first Actions
  run exposed harness errors before a corrected runtime comparison completed.
- [ ] Commit and push the focused harness corrections, then run the next
  isolated Windows Actions job to build fixed Win32, execute A/B/fixed startup,
  conditionally run ownership, and upload machine-readable evidence.
- [ ] Review the uploaded artifact, record run URL/markers/exits/hashes/dumps,
  update both reports and evidence, and state the Win32 functional verdict.

## Current Architecture and Scope

The historical A/B `Launcher.exe` and `tcvm.dll` are I386. The archived fixed
runtime from run `45884551862` is AMD64, so it cannot substitute for a corrected
production target. Build fixed source `94af2b8b653cad509994b5ccf3dbb60ac28ef920`
with the canonical `Visual Studio 17 2022` generator, `-A Win32`, Release,
Skia ON, legacy renderer OFF, SDL ON, Native windowing OFF, software surface ON,
and GLES OFF. Use the exact fixed SDK plus fixed-source native bridge and
prepared dependency artifacts from run `45884551862`; do not fetch newer
dependencies or alter rendering configuration.

The startup probe uses one source file and each matching SDK. It does not create
images or exercise ownership. Only after fixed I386 records every durable Java
stage marker, a durable PASS marker, and exit code 0 does the job deploy and run
the existing ownership fixture against the same I386 Launcher/tcvm hashes.
That fixture covers two 1,510-control screens, detach/release, subtree mutation
and reattachment, active reparenting, shared owners, pipeline replacement,
raster-cache reuse/eviction, native drawing, red-pixel verification, underflow
diagnostics, and native process completion.

No Windows build, native execution, or debugging runs locally. Do not change
production ownership/runtime implementation. Do not run more AMD64 diagnostics.

## Plan of Work

1. Artifact audit (complete): retain exact source SHA/tree, SDK/runtime artifact
   IDs and digests, JAR/Launcher/tcvm hashes, build identity, imports, PE
   architecture, and runtime packaging.
2. Fixture/harness: keep the minimal Java fixture independent of images; use
   `Convert.appendPath`, expose marker failures, require the durable PASS file,
   and record SDK/TCZ/deployed EXE identity and architecture. Use generic I386
   crash analysis (`.effmach x86`, exception, stack, loaded modules, and dump),
   never AMD64 RVA assumptions.
3. Corrected Win32 build: checkout the exact fixed source in the Windows job,
   download its pinned SDK, dependency and native-bridge artifacts by run/name,
   verify source commit/tree/clean state, configure with the production Win32
   defaults, and build Release. Keep build logs, CMake cache, PDBs when
   available, provenance, PE identities, and artifact digest.
4. Same-run comparison: on one `windows-2022` job, prepare and run historical A,
   historical B, and corrected I386 in that order. Capture creation/PID/session,
   duration, decimal/hex exit code, timeout, durable and debug markers,
   DebugConsole, events, loaded modules, and dumps when failures occur. Keep
   preparation failures distinct as INCOMPLETE and continue to later revisions.
5. Gated ownership: after corrected I386 startup PASS, immediately prepare and
   run the pinned existing functional smoke in the same job. Verify its
   Launcher/tcvm hashes match the corrected startup runtime and persist all
   assertion results and native logs. Do not run this workload after startup
   failure and do not repeat performance measurements.
6. Handoff: upload one result artifact with each probe's manifests/results/logs,
   smoke output, run metadata, and crash evidence. Update the existing crash
   report with the I386 correction, and provide a separate concise final report.

## Decision Log

- Decision: the user priority correction supersedes the original AMD64
  comparison for active validation. Existing AMD64 results stay as a reference;
  no additional AMD64 run is scheduled.
- Decision: use a new isolated branch/worktree based on the completed
  diagnostics branch. The dirty root checkout and existing worktrees remain
  untouched.
- Decision: build only corrected Win32. Reuse historical A/B binaries, corrected
  SDK, and current fixed-source dependency/native-bridge artifacts.
- Decision: adapt the existing smoke helper's architecture validation on its
  dedicated performance-lab branch; keep its Java lifecycle fixture and
  functional assertions unchanged.
- Decision: one workflow/job/session runs all I386 probes sequentially and
  executes ownership only on corrected startup PASS.
- Decision: classify download, provenance, compilation, and deploy failures as
  INCOMPLETE. A launched process is PASS only with all durable app markers, the
  PASS result file, and native exit 0.

## Validation and Acceptance

- Before push: run focused Python contracts, validate changed copyright
  headers, parse the workflow YAML, run `git diff --check`, inspect the full
  scoped diff, and validate commit title/body. Do not run unrelated builds.
- Acceptance requires one completed Actions run with historical A, historical
  B, corrected I386 in that order, all on one `windows-2022` runner session.
  Each version must record exact source/artifact/SDK/TCZ/runtime identities,
  PE machine, process facts, Java markers, durable PASS state, and any crash
  diagnostics. Historic FAIL outcomes remain valid observations; INCOMPLETE
  outcomes do not satisfy the comparison.
- If corrected I386 startup passes, acceptance also requires the existing
  ownership smoke on matching fixed Win32 runtime hashes and all required
  ownership assertions plus native render/pixel/process success.
- If corrected I386 fails, retain a focused dump and resolve its actual I386
  function/stack independently. Never map the AMD64 RVA onto I386.
- Final handoff compares historical I386 with corrected I386 and records the
  already-known corrected AMD64 result separately. It explicitly answers
  whether persistent ownership is validated on production Windows Win32.

## Risks and Limits

- The fixed prepared dependency and native-bridge artifacts have short
  retention. Confirm their exact IDs remain available immediately before the
  push/run.
- GitHub-hosted Windows execution proves behavior on that runner image/session;
  it does not replace the customer desktop environment.
- Historical binaries lack matching PDBs. If a historical process fails,
  module/RVA/stack evidence may be unsymbolized. The corrected Win32 build
  includes matching PDBs when the Windows generator emits them.
- The prior corrected AMD64 startup failure remains a separate issue. The
  production Win32 decision does not depend on rerunning or resolving AMD64.

## Idempotence and Recovery

Artifact inputs use exact originating run IDs and artifact names. Every SDK,
runtime, result folder, and executable remains revision-specific. The startup
loop continues after a version's prep/run failure. The fixed ownership workload
is conditional on fixed startup PASS. Preserve all local caches and existing
worktrees; no force-push, history rewrite, merge, release, or main/master push.

## Outcomes & Retrospective

Pending the corrected Win32 build and same-run I386 comparison.

## Revision Note

Priority corrected from an AMD64 artifact comparison to production Win32/I386
validation after matching the historical customer artifacts to PE I386.
