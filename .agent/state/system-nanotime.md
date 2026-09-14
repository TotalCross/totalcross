<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# System nanoTime execution state

Updated: 2026-09-14
Branch: `feat/system-nanotime`
Active milestone: Milestone 1 — API and conversion
Last commit: branch created from `perf/image-scroll-prefetch`; no task commit yet

## Active paths

- `TotalCrossSDK/src/main/java/jdkcompat/lang/System4D.java`
- `TotalCrossSDK/src/main/java/tc/tools/converter/`
- `TotalCrossSDK/src/test/java/tc/tools/converter/`
- `TotalCrossVM/src/nm/NativeMethods.txt`
- `TotalCrossVM/src/nm/NativeMethodsPrototypes.txt`
- `TotalCrossVM/src/nm/NativeMethods.h`
- `TotalCrossVM/src/init/nativeProcAddressesTC.c`

## Next concrete action

Add the `System4D` declaration, native metadata contract, and focused converter
tests. First inspect the existing test fixture patterns, then run only focused
non-build checks before the Milestone 1 commit.

## Focused validation completed

- Read the pasted requirements file.
- Read `AGENTS.md` and `.agent/PLANS.md` in full.
- Confirmed `perf/image-scroll-prefetch` was up to date before branch creation.
- Confirmed the repository initially had only unrelated untracked artifacts;
  they are deliberately out of scope.

## Deferred validation

No SDK or native build has been run. Per user instruction, SDK build is deferred
until Milestone 1 is complete; native macOS build is deferred until Milestone 2
is complete; all other platform builds are prohibited locally.

## Active decisions and blockers

Use `jlS_nanoTime` for `java/lang/System.nanoTime()` and keep `Vm` unchanged.
The `logical-commits` skill named by the request is unavailable; milestone
commits will follow the repository's commit-title rules manually.

## Deliberate out-of-scope local files

Leave all unrelated benchmark logs, generated launcher files, `IOSDateFixture.tcz`,
and existing `.agent/plans/image-optimization-mask-*` files untouched.

## Resume command

`git status --short -- .agent/plans/system-nanotime.md .agent/state/system-nanotime.md TotalCrossSDK/src/main/java/jdkcompat/lang/System4D.java TotalCrossSDK/src/test/java/tc/tools/converter TotalCrossVM/src/nm TotalCrossVM/src/init/nativeProcAddressesTC.c`
