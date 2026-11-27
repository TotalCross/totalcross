<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Fix POSIX startup realpath buffer abort

This ExecPlan follows `AGENTS.md` and `.agent/PLANS.md`.

## Purpose / Big Picture

Fix TotalCross Linux x86_64 startup aborts caused by passing a 1,024-byte
stack buffer to fortified glibc `realpath()`. Preserve the shared Linux/macOS
path helper's fallback, copy bounds, termination, and truncation behavior.
Prove the old call on a hosted Linux runner, exercise the actual helper, then
validate deployed Linux and macOS ARM64 startup plus Linux file/SQLite/JPEG
controls. Native desktop runtime checks run only on GitHub-hosted runners.

## Working Set and Resume Protocol

- `.agent/state/fix-linux-realpath-startup.md`: active slice, latest commit,
  validation, blockers, and next action. Read first when resuming.
- `.agent/evidence/fix-linux-realpath-startup.md`: compact append-only records
  for CI runs, test results, source revisions, and artifact identities. Search
  relevant entries instead of rereading full logs.
- `.agent/reports/fix-linux-realpath-startup-editorial.md`: final technical
  handoff, written at completion.
- `TotalCrossVM/src/init/posix/startup_c.h`: shared path helper.
- `TotalCrossVM/src/util/xtypes.h`: `MAX_PATHNAME` definition.
- `TotalCrossVM/CMakeLists.txt`, `.github/workflows/build.yml`, and
  `scripts/package-sdk.sh`: native test and canonical build/package paths.
- The prior investigation report and immutable runner evidence are linked in
  the final handoff; the investigation branch is read-only for this task.

## Progress

- [x] (2026-11-27) Read repository guidance, logical-commit and header skills,
  and the referenced investigation. Created `fix/linux-realpath-startup` from
  latest fetched `origin/master` (`52a947b5295321d7350350bd618c55fb26304c00`)
  in an isolated worktree.
- [x] Establish a hosted fortified Linux baseline reproduction before changing
  production behavior: Ubuntu 24.04/glibc 2.39, `_FORTIFY_SOURCE=2`,
  `MAX_PATHNAME=1024`, `__realpath_chk`, and child `SIGABRT` (Actions
  `46044420755`).
- [x] Add focused helper coverage and the POSIX allocation-form correction.
- [x] Run Linux x86_64 and macOS ARM64 hosted validation. Helper regression,
  canonical builds, deployed path cases, downstream controls, and macOS smoke
  pass in Actions run `46051419602`.
- [x] Commit and push the final evidence, state, plan, and editorial handoff.

## Current Architecture and Scope

`startup.c` includes `init/posix/startup_c.h` for Linux and macOS. Its inline
`copyResolvedPath()` now calls `realpath(path, NULL)`, copies the allocated
result or original-path fallback into the caller's bounded output, then frees
the returned allocation. Native helper tests cover Linux and macOS behavior.

The glibc 2.39 `__realpath_chk` source establishes that a non-null destination
must be at least `PATH_MAX` bytes (4096 on the recorded runner); otherwise it
calls `__chk_fail` before path length matters. POSIX allocation-form
`realpath(path, NULL)` returns a libc-allocated path that must be released by
`free()`. Keep this change limited to the shared helper and focused tests.

## Plan of Work

1. Inspected canonical Linux/macOS build and dependency setup, captured the
   Linux runner environment, and proved the old helper aborts in a child under
   `_FORTIFY_SOURCE=2` with `__realpath_chk`.
2. Added native tests for path forms, fallback, output capacities, truncation,
   termination, and repeated success/failure. Corrected the helper to use
   `realpath(path, NULL)` and free the returned allocation.
3. Reused canonical Release Linux x86_64 and macOS ARM64 builds. The final
   Linux deployed matrix covers normal, short, bounded-long (201–237-byte
   executable paths), symlink, and relative paths with file, SQLite, and JPEG
   controls. macOS ARM64 covers normal and symlink startup without loader
   shims.
4. Recorded the run and logical commits below. Only this feature branch was
   pushed; no PR, tag, release, or deployment was created.

## Surprises & Discoveries

- Historical 7.3.0 and frozen-master failures are captured, but the original
  artifact omitted `_FORTIFY_SOURCE` and compiler flags.
- glibc 2.39 source checks `resolvedlen < PATH_MAX` directly and aborts before
  using the short result, explaining the observed path-length independence.
- The checkout originally had extensive unrelated untracked files; the task
  uses a separate worktree and leaves them untouched.

## Decision Log

- Decision: Keep the correction in the shared POSIX helper and use libc
  allocation/free pairing. Rationale: it removes the undersized caller buffer
  while preserving Linux and macOS path behavior. Date: 2026-11-27.
- Decision: Run native runtime and sanitizer checks only on the specified
  hosted platforms. Rationale: explicit task restriction. Date: 2026-11-27.
- Decision: Reuse canonical build and dependency paths, adding only one focused
  regression workflow if existing workflow dispatch cannot select the required
  Linux/macOS jobs without Android or iOS. Date: 2026-11-27.
- Decision: Keep the deployed long-path smoke below 238 bytes. Rationale: the
  native VM's application TCZ path handling rejects longer install paths before
  Java starts; the C helper contract independently covers deeper paths. Date:
  2026-11-27.

## Validation and Acceptance

The required final gate is hosted operation-family validation: focused helper
tests and allocation diagnostics; Linux Release native build and deployed
startup matrix plus file/SQLite/JPEG controls on Ubuntu 24.04 x86_64; and
macOS ARM64 native startup smoke. All corrected runtime cases must run without
`LD_PRELOAD`, reach `MainWindow.initUI()`, and exit normally. Preserve compiler
hardening and dependency pins. Use `git diff --check`, focused header and
commit-message validators at each commit. Do not run desktop native tests
locally, Android/iOS builds, Windows AMD64 tests, or benchmarks.

The fortified helper reproducer explicitly compiles with `_FORTIFY_SOURCE=2`.
The canonical Release compile command uses `-O3` without an explicit
`_FORTIFY_SOURCE` definition, and the corrected VM does not import
`__realpath_chk`; the corrected deployed runtime still passes without loader
shims. Report this distinction when summarizing the baseline evidence.

## Risks and Limits

- The reproduced abort uses a helper binary explicitly compiled with
  `_FORTIFY_SOURCE=2`; the current canonical Release VM uses `-O3` without an
  explicit fortify definition. The final evidence reports both facts.
- The deployed long-path test is limited to 201–237-byte executable paths by
  the VM's existing application TCZ path limit. The C helper test covers deeper
  paths without loading an application TCZ.
- The independent historical TotalCross 7.2.2 launcher overflow is out of
  scope and remains distinguished in the final report.

## Idempotence and Recovery

All work is on the new branch in `/Users/flsobral/repos/totalcross-fix-linux-realpath-startup`.
The original `/Users/flsobral/repos/totalcross-github` checkout and its
untracked files remain untouched. Fetching and pushing use only `origin`; the
investigation branch is read-only. Before every push or commit, recheck the
branch head and scoped diff. Retry hosted validation from a recorded commit
and never overwrite prior logs or evidence.

## Outcomes & Retrospective

Hosted baseline, Linux/macOS native builds, both platform CTest suites, Linux
deployed path/control matrix, and macOS deployed smoke passed in
`46051419602`. A pre-existing workflow test has a stale assertion: the base
workflow has eight `prepared-native-dependencies` references while the test
expects seven. The final evidence index and editorial handoff record this
limitation and the completed validation.

## Revision Note

Initial plan created before implementation. Update only at a functional
checkpoint or material validation/workflow change.
