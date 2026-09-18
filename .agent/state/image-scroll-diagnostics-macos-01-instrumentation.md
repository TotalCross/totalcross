<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# State — image-scroll diagnostics macOS instrumentation

## Active slice

- Milestone: Milestone 2 gate after Slice 2B — benchmark phase/frame
  integration and distributed result-schema validation.
- Last checkpoint commit: `72fe224f2`.
- Next action: run the Milestone 2 static checks, macOS ARM64 rebuild/package,
  benchmark self-test, four existing smokes, and mask-4 diagnostics smoke.
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
- Focused SDK image tests passed; wrapper log:
  `/tmp/image-scroll-diagnostics-sdk-image-tests.log`.
- SDK distribution build passed; wrapper log:
  `/tmp/image-scroll-diagnostics-sdk-dist.log`.
- macOS ARM64 CMake/Ninja gate passed for `tcvm` and `Launcher`; logs:
  `/tmp/image-scroll-diagnostics-cmake-configure.log` and
  `/tmp/image-scroll-diagnostics-cmake-build.log`.
- No other platform was built. Gate warnings were existing compiler/linker
  warnings and did not prevent the two requested targets from linking.
- Milestone 1 accepted; no benchmark smoke has run yet.
- Slice 2A commit: `4d781d5a2` (`perf(benchmark): record scroll decode
  diagnostics`). Added prefetch/scroll JPEG snapshots, per-frame JPEG deltas,
  detailed writePixels counters, four attempt-feature statuses, and runtime
  consistency checks.
- Slice 2B commit: `72fe224f2` (`test(benchmark): validate diagnostic result
  schema`). Added expanded frame-schema validation, compact diagnostic fields
  in distributed `summary.csv`, and README field/status documentation.
- Slice 2A/2B static checks passed: focused header validation, `git diff
  --check`, `bash -n scripts/package-image-scroll-benchmark.sh`, Python
  compilation, and a deterministic diagnostic parser fixture. No SDK/native
  build ran between the two slices, per plan.

## Decisions still active

- Keep writePixels and JPEG instrumentation policy-neutral and gated by the
  existing accounting enablement.
- Preserve existing benchmark reset and prefetch-versus-scroll separation.
- Build and smoke only SDK plus macOS ARM64 native targets at milestone gates.

## Resume command

Read this file, then the active milestone in the plan and the named source
paths. Start with the next action above.
