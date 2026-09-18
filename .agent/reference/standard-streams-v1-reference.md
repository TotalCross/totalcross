<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Standard streams V1 reference

This is the compact technical reference for the completed and corrected V1
implementation. Read it with `.agent/state/standard-streams-v1.md` when
resuming work; read `.agent/archive/standard-streams-v1-history.md` only when
historical rationale is needed.

## Architecture

The fixed data path is:

    System.out/err
      -> PrintStream4D
      -> VmStandardOutputStream
      -> native StandardStreamRouter
      -> platform sink + LegacyDebugConsoleWriter

`Vm.debug()` remains outside the router. Standard streams never call
`debugStr`, `privateDebug`, or `closeDebug`. The standard bridge remains a
runtime implementation class: it is present in `totalcross-runtime-java` but
excluded from `totalcross-api`.

The Java side has distinct `PrintStream4D` objects, locks, error state, and
`VmStandardOutputStream` channels for OUT and ERR. Native routing uses one
mutex for standard-stream emission, then the shared legacy writer's recursive
mutex. A single native write is the atomic unit.

## Java contract

`PrintStream4D` implements the V1 non-formatter subset:

- `write(int)`, `write(byte[])`, ranged byte writes, and `writeBytes`;
- primitive, `char[]`, `String`, and `Object` `print`/`println` overloads;
- `append` overloads, `flush`, `close`, `checkError`, and protected error hooks.

The compatibility rules are:

- String/Object null values print `null`;
- null `char[]` arguments throw `NullPointerException`;
- invalid append ranges throw `IndexOutOfBoundsException`;
- I/O failures set trouble and do not escape ordinary print operations;
- output after close sets trouble; close is idempotent;
- `checkError()` flushes and includes trouble reported by an underlying
  `java.io.PrintStream` while open; after a successful close it reports only
  the wrapper's existing trouble state without flushing or querying the wrapped
  stream;
- every value-bearing `println` creates one `value + '\n'` byte record and
  makes one underlying write; `println()` makes one newline write.

Formatter/`printf`/`format`, `Formatter`, file/filename constructors, and
additional charset constructors are not part of V1.

Generic `PrintStream4D` instances preserve the platform-default
`String.getBytes()` behavior. Only instances backed directly by
`VmStandardOutputStream` use the supported UTF-8 path before crossing the
native bridge. Character conversion remains in Java.

## Native router and lifecycle

`VmStandardOutputStream` validates OUT/ERR and byte ranges, maps ordinary
write failure to `IOException`, separates logical flush from durable flush,
and performs a logical close without closing OS handles or the shared legacy
file.

The router attempts both available sinks and returns aggregate failure. A
missing Windows inherited handle is unavailable rather than an error, so the
legacy file can still receive output. Standard writes use `durable=false`;
explicit Java `flush()` uses `durable=true`.

The VM initializes the router after globals and destroys it before the shared
debug writer. The native method metadata and registration contain only the
focused bridge symbols.

## Platform mapping

Five required targets are represented by four dedicated sink headers, with the
Darwin implementation shared by macOS/iOS:

| Target | Standard OUT | Standard ERR | Key constraint |
| --- | --- | --- | --- |
| Linux/POSIX | fd 1 | fd 2 | `write`, partial-write and EINTR retry |
| Windows desktop | inherited `STD_OUTPUT_HANDLE` | inherited `STD_ERROR_HANDLE` | `WriteFile`; never allocate/close a console handle |
| WinCE compatibility | existing legacy path | existing legacy path | no new durable primitive |
| Android | `ANDROID_LOG_INFO` | `ANDROID_LOG_WARN` | bounded separate buffers and real package tag |
| macOS desktop | fd 1 | fd 2 | directly redirectable process streams |
| iOS/Darwin | Unified Logging INFO | Unified Logging ERROR | bundle ID subsystem and `System.out`/`System.err` categories |

Android resolves and caches `applicationContext.getPackageName()` and uses
`TotalCross` only as a defensive fallback. Android and Darwin buffers are
separate and bounded at 4096 bytes.

## Legacy compatibility

`legacy_debug_console.c` owns one lazy `<appPath>/DebugConsole.txt` `FILE*`
and synchronizes raw writes, flushes, erase, separator, close, and shutdown.
The shared writer is used by standard streams and legacy debug backends, but
caller semantics remain separate:

- standard bytes are literal; `ERASE_DEBUG`, `ALTERNATIVE_DEBUG`, and
  `Vm.disableDebug` have no special meaning;
- standard auto-flush calls `fflush`-equivalent logic only;
- explicit standard flush calls the durable path;
- POSIX/Android `Vm.debug` retains per-message flush plus `fsync`;
- Windows legacy `Vm.debug` retains `fflush` without durable sync;
- desktop macOS `Vm.debug` remains stdout-only and does not create, write, or
  erase `DebugConsole.txt`;
- iOS, Android, POSIX, Windows, and termination separator behavior remains
  localized to the existing platform debug backend.

On Windows desktop, durable file flush uses CRT `_fileno`/
`_get_osfhandle` followed by `FlushFileBuffers`. WinCE ignores the durable
flag and keeps its historical compatibility behavior. Auto-flush never calls
the durable Windows primitive.

## Validation contract

Required focused coverage includes Java print semantics, atomic records,
`char[]` null behavior, underlying error propagation, standard-only UTF-8,
logical versus explicit flush, and the API artifact boundary.

The macOS smoke must prove independent stdout/stderr capture, literal standard
`ERASE_DEBUG`, shared standard output in `DebugConsole.txt`, debug disabling
that does not affect standard streams, and desktop macOS `Vm.debug` stdout-only
behavior with no legacy-file marker or erase.

Static review covers all platform mappings and dedicated-file scope. Local
builds are restricted to the SDK and native macOS; Windows, Linux, Android,
and iOS builds/smokes and the full image benchmark matrix are deferred.

## Implementation paths

Java: `PrintStream4D.java`, `System4D.java`,
`VmStandardOutputStream.java`, focused tests, artifact boundary test, smoke
fixture, and `build.gradle` tasks.

Native: `standard_stream.{h,c}`,
`nm/sys/VmStandardOutputStream.c`, `legacy_debug_console.{h,c}`, platform
standard/debug backends, VM startup/lifecycle, CMake, and native metadata.

Plan support: `.agent/plans/standard-streams-v1.md`, state, append-only
evidence, archive, and editorial report.
