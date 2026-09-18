<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# State — image-scroll diagnostics macOS instrumentation

## Active slice

- Milestone: Milestone 1 gate — SDK and macOS ARM64 native builds.
- Last checkpoint commit: `0563e08b8`.
- Next action: run focused SDK compilation, SDK distribution build, and only
  the macOS ARM64 `tcvm` and `Launcher` native targets.
- Required branch: `perf/image-decode-distributed-benchmark`.
- Starting HEAD: `5917a4aa3e20123a1ff02c1a5b0cedd9640c0c6b`.

## Working set

- Plan: `.agent/plans/image-scroll-diagnostics-macos-01-instrumentation.md`
- State: this file; rewrite on resume.
- Evidence: `.agent/evidence/image-scroll-diagnostics-macos-01-instrumentation.md`
- Editorial handoff:
  `.agent/reports/image-scroll-diagnostics-macos-01-instrumentation-editorial.md`

## Deliberate local files

- The plan file was already untracked at bootstrap and is the user-provided
  task specification. It is included in the bootstrap artifact set.
- No source, build, package, or benchmark-output changes are in scope at
  bootstrap.

## Validation and deferrals

- Bootstrap branch and HEAD checks passed.
- New-file size checks passed: all four artifacts are <= 20 KiB and <= 600
  lines.
- Copyright validation passed for all four artifacts.
- Staged whitespace validation passed.
- Commit-message validation found an overlong bootstrap body line caused by
  literal `\\n` text in the commit argument. The commit is preserved because
  the plan forbids amend/rewrite; future commit bodies will be wrapped and
  separated correctly.
- No SDK/native build or smoke is allowed at bootstrap.
- Slice 1A static/header/whitespace validation passed.
- Milestone 1 SDK/native gate is deferred until Slice 1B is complete.
- Slice 1B static/header/whitespace validation passed.
- No native or SDK build has failed or been run yet for this milestone.

## Decisions still active

- Keep writePixels and JPEG instrumentation policy-neutral and gated by the
  existing accounting enablement.
- Preserve existing benchmark reset and prefetch-versus-scroll separation.
- Build and smoke only SDK plus macOS ARM64 native targets at milestone gates.

## Resume command

Read this file, then the active milestone in the plan and the named source
paths. Start with the next action above.
