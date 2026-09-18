<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# State — image-scroll diagnostics macOS instrumentation

## Active slice

- Milestone: Plan 1 closed after the final short-circuit gate.
- Last functional checkpoint commit: `e1c3bab55`.
- Closeout documentation commit: pending in this slice.
- Next action: Plan 2 at
  `.agent/plans/image-scroll-diagnostics-macos-02-benchmark.md`.
- Required branch: `perf/image-decode-distributed-benchmark`.
- Starting HEAD: `5917a4aa3e20123a1ff02c1a5b0cedd9640c0c6b`.

## Working set

- Plan: `.agent/plans/image-scroll-diagnostics-macos-01-instrumentation.md`
- State: this file; rewrite on resume.
- Evidence: `.agent/evidence/image-scroll-diagnostics-macos-01-instrumentation.md`
- Editorial handoff:
  `.agent/reports/image-scroll-diagnostics-macos-01-instrumentation-editorial.md`

## Deliberate local files

- The Plan 1 file was user-provided at bootstrap and is now tracked with the
  closeout edits; Plan 2 is the next tracked plan.
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
- Milestone 2 static gate passed: `bash -n
  scripts/package-image-scroll-benchmark.sh` and Python compilation.
- Bridge follow-up `f803d4a4d` shortened private native diagnostic method names
  to the VM resolver limit after the first packaged smoke found
  `NoSuchMethodError`; focused header validation and `git diff --check` passed.
- Post-fix focused SDK image tests passed; log:
  `/tmp/image-scroll-diagnostics-m2-bridge-sdk-image-tests.log`.
- Post-fix SDK package passed; log:
  `/tmp/image-scroll-diagnostics-m2-bridge-package-sdk.log`.
- Post-fix macOS ARM64 CMake configure/build passed; logs:
  `/tmp/image-scroll-diagnostics-m2-bridge-cmake-configure.log` and
  `/tmp/image-scroll-diagnostics-m2-bridge-cmake-build.log`.
- macOS-only benchmark package passed; log:
  `/tmp/image-scroll-diagnostics-m2-package-fixed.log`. Bundle self-test passed;
  log `/tmp/image-scroll-diagnostics-m2-fixed-self-test.log`.
- Prescribed scroll smokes passed for `0/off`, `0/on`, `32799/off`, and
  `32799/on`; log `/tmp/image-scroll-diagnostics-m2-fixed-smokes.log`.
- Additional mask-4 `off`/`on` processes passed through the runner's artifact
  validator; log `/tmp/image-scroll-diagnostics-m2-mask4-smokes.log`.
- The final correction gate passed focused SDK image tests; log:
  `/tmp/image-scroll-diagnostics-plan1-fix-sdk-tests.log`.
- Final SDK package passed; log:
  `/tmp/image-scroll-diagnostics-plan1-fix-sdk-package.log`.
- Final macOS ARM64 configure/build passed; logs:
  `/tmp/image-scroll-diagnostics-plan1-fix-cmake-configure.log` and
  `/tmp/image-scroll-diagnostics-plan1-fix-cmake-build.log`.
- Final macOS-only bundle package, self-test, prescribed smokes, and mask-4
  runner validation passed; logs:
  `/tmp/image-scroll-diagnostics-plan1-fix-package-bundle.log`,
  `/tmp/image-scroll-diagnostics-plan1-fix-self-test.log`,
  `/tmp/image-scroll-diagnostics-plan1-fix-smokes.log`, and
  `/tmp/image-scroll-diagnostics-plan1-fix-mask4.log`.
- Final mask 4 observed attempts/hits/fallbacks `220/0/220` off and
  `3414/0/3414` on; candidates were `198` and `3414`, with
  `ATTEMPTED_NO_HIT`. JPEG/frame invariants passed.
- Final SDK ZIP SHA-256:
  `5fb07ada2df36d2267c43eace387b44e8f4de8a6570bbac4ec2beeeabf91338a`.
- Final package output: `/tmp/image-scroll-diagnostics-plan1-fix/bundle/`.
- Final correction commit: `e1c3bab55`; Plan 2 document commit:
  `58699d768`.
  JPEG phase/frame denominator count/ns invariants passed, including prefetch
  `660` versus scroll `0` for mask 4/on.
- No other platform or full decode matrix was built; no optimization policy
  changed. Plan 1 is closed and Plan 2 is the only next action.

## Decisions still active

- Keep writePixels and JPEG instrumentation policy-neutral and gated by the
  existing accounting enablement.
- Preserve existing benchmark reset and prefetch-versus-scroll separation.
- Build and smoke only SDK plus macOS ARM64 native targets at milestone gates.
- Global writePixels counters may include direct image/physical copies under
  composite masks; detailed counters cover regular `tryWritePixels*`, and mask
  4 is authoritative.

## Resume command

Read this file, then the active milestone in the plan and the named source
paths. Start with the next action above.
