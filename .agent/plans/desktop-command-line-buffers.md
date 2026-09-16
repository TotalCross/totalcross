<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Remove fixed desktop command-line buffers

This ExecPlan follows `.agent/PLANS.md` and `AGENTS.md`.

## Purpose / Big Picture

Desktop startup must transport the complete launcher command line through
`startVM()` and `prepareDesktopCommandLines()` without the old 512-byte VM or
256-byte application limits. VM startup options and the `/cmd` format remain
unchanged, while long application arguments reach the existing command-line
consumer intact.

## Working Set and Resume Protocol

This is a short plan with no auxiliary state or evidence files. On resumption,
inspect this plan, the active diff, and the focused startup test before taking
the next action. Do not touch existing benchmark artifacts or unrelated local
changes.

## Progress

- [x] Read `AGENTS.md`, `.agent/PLANS.md`, and the referenced task text.
- [x] Confirmed the fixed buffers and traced `initAll()` pointer movement and
  desktop parser ownership.
- [x] Replaced fixed local buffers, added structured cleanup, and adapted
  focused tests for dynamically sized command lines.
- [x] Finished targeted validation, inspected the final diff/status, and
  created one logical commit.

## Current Architecture and Scope

`filterDesktopCommandLine()` compacts a mutable command buffer in place and
records `DesktopCommandLineOptions`. `prepareDesktopCommandLines()` then
copies only the substring after `/cmd` into its caller-provided application
buffer. `startVM()` copies the launcher input into VM-owned mutable storage,
passes the TCZ-name buffer to `initAll()`, and later copies the application
payload into the legacy `commandLine` consumer before the temporary buffers can
be released.

The change is limited to `TotalCrossVM/src/init/startup.c`, the startup-focused
test coverage, and the existing native-test CMake wiring if a standalone parser
test is needed. `Launcher.c`, public command-line syntax, TCZ loading,
windowing, renderer behavior, and benchmark code are out of scope.

## Plan of Work

1. Allocate `vmCommandLine` and `applicationCommandLine` from
   `strlen(argsOriginal) + 1`, retain one mutable source copy, and replace
   `args`/`argsLower` with owned dynamic buffers sized for their actual source
   content and platform suffix needs.
2. Preserve `prepareDesktopCommandLines()`'s explicit capacity parameter and
   in-place filtering. Convert only the `startVM()` returns affected by these
   allocations to a cleanup path, preserving each existing return code and
   freeing storage after the last pointer-based use.
3. Adapt startup tests and add a native focused case with a command line over
   512 bytes and an application section over 256 bytes. Assert VM option
   consumption, complete `/cmd` delivery, long-input integrity, and the
   existing short-command behavior.
4. Run `git diff --check`, static searches for the removed fixed declarations,
   the focused native test/build available on this host, and the copyright
   validator for changed first-party files. Do not run benchmark matrices.

## Decision Log

- Decision: Keep the legacy `commandLine[256]` global ABI unchanged in this
  focused fix; make the required `startVM()` and parser transport buffers
  dynamic and test the complete application payload at that boundary.
  Rationale: changing that global's type/storage would expand the task into a
  public ABI and lifetime redesign unrelated to the requested local-buffer
  fix.
  Date: 2026-09-16.

- Decision: Use `malloc/free` as requested, which follows the repository's
  allocator compatibility macros, and use one cleanup label in `startVM()`.
  Rationale: ownership stays local and every error path can release all
  partially initialized buffers without a broad function refactor.
  Date: 2026-09-16.

## Validation and Acceptance

This is a Level 2 focused functional change. Acceptance requires the focused
startup test to pass, no prohibited fixed declarations in the desktop startup
flow, `git diff --check` to pass, and the changed-file copyright check to pass.
A Windows build is attempted only if a Windows-capable CMake toolchain is
available; benchmark matrices are intentionally deferred because they are
explicitly out of scope.

## Risks and Open Questions

The native standalone test includes `startup.c` while linking the existing VM
target, so platform linker/export behavior may prevent it from building on a
host even when the parser code is valid. If that occurs, retain the existing
startup-suite coverage and report the platform limitation rather than changing
unrelated build/link architecture.

## Idempotence and Recovery

All edits are additive or localized and can be reapplied after inspecting the
current diff. Do not clean or delete existing build/dependency directories.
If validation generates ignored build output, leave source and user-local
artifacts untouched. Before committing, stage only the plan, startup source,
focused test source, and directly related CMake/test files.

## Outcomes & Retrospective

The implementation uses input-sized `vmCommandLine` and
`applicationCommandLine` storage, dynamic `args`/`argsLower` storage, and a
single cleanup path that preserves startup return codes. The native parser test
proves short-command compatibility plus complete 700-byte application delivery
from a command line larger than 512 bytes. macOS focused builds and tests pass;
Windows validation is deferred because no Windows toolchain is installed.
The final commit hash is recorded by the task handoff after commit creation;
the plan, implementation, and focused tests are part of that one commit.
