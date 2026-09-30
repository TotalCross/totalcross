<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Standard streams V1 report

## Summary

Added independent deployed `System.out` and `System.err` streams while
preserving `Vm.debug` and `DebugConsole.txt` compatibility.

## Final implementation

`System4D` exposes separate `PrintStream4D` instances over an internal native
bridge. Standard writes use UTF-8; generic wrapped streams retain default
encoding. Native routing selects explicit POSIX, Android, Darwin, and Windows
sinks. A synchronized lazy writer shares legacy debug-file output without
letting stream close shut down the file. Explicit flush retains durability
where required, auto-flush avoids durable sync, and desktop macOS legacy debug
continues to write to stdout only. Stream boolean-like native ABI parameters
and returns use `int32`; the bridge remains absent from the public API jar.

## Decisions and tradeoffs

The shared file writer separates standard-stream closure from legacy debug-file
lifecycle. Logical flushes and explicit durable flushes follow different paths
to avoid syncing every auto-flushed line. The smoke fixture checks routing and
file behavior with the deployed macOS runtime.

## Validation

- Focused SDK tests: 17 tests passed, 0 failures using
  `./gradlew-agent test --tests jdkcompat.io.PrintStream4DTest --tests
  jdkcompat.lang.System4DTest --no-daemon --console=plain`.
- `./gradlew-agent artifactContentTest --no-daemon --console=plain` — passed,
  7 tests; the internal bridge is in runtime Java and excluded from the API jar.
- `./gradlew-agent dist -x test --no-daemon --console=plain` — passed.
- Release native macOS configure and build passed with 131 build steps.
- `runStandardStreamsSmokeMacOS` passed with 201 stdout bytes, 77 stderr bytes,
  and 233 `DebugConsole.txt` bytes. The clean worktree lacked SDK-staged macOS
  launcher/runtime inputs; both were staged from this branch's native build.
- `git diff --check origin/master...HEAD` and focused copyright validation
  passed.

## Limitations and deferred work

Android, Windows, Linux, WinCE, and iOS builds were omitted under the task's
platform restrictions. Those platform sinks and the iOS integer ABI correction
are preserved from the validated historical implementation but were not
validated here. `System.nanoTime()`, image/raster/prefetch work, benchmarks, and
generalized runtime diagnostics remain outside this branch.
