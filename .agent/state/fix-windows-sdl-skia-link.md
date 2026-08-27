<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# State: Windows SDL + Skia link fix

Active milestone: Complete — default and fallback Windows CI gates passed
Current TotalCross HEAD: `859bf1ef89da7add80ecdda41df72d81935bc1c5`
Last logical commit: `e2fe6c2a4` — CI asserts generated static CRT settings;
final documentation snapshot: `859bf1ef8`
Current depot-tools pin: `0ebff1d7202fab6e61758344219f60fa757fe6ce`
Candidate SDL2 release tag: `sdl2-2.32.8-r2`
Verified SDL2 release commit: annotated tag peels to
`e9ce760e3a4ba23f3af926a0c0260f4b175009a8`; source, manifest, assets, and
allocator symbol checks passed. The pinned depot revision also contains the
static-runtime releases for AxTLS, Minizip-ng, and qrcodegen.
Private SQLite SEE release: `sqlite3-see-3.32.3-r1`, built from commit
`e173237` and verified by its Windows CI matrix.
Active build directories: CI run `33040181465`, Windows jobs
`98411983584` (default) and `98411983588` (Native + Legacy)
Focused validation completed: consumer commits `4fdb6b044`, `044515ca0`,
`4efb13e0d`, `cae516c90`, `ee697c09d`, `a9c153c53`, and `e2fe6c2a4` were
published; the dependency releases were rebuilt or republished with static
CRT verification; CI passed both Windows configurations and explicitly found
`MultiThreaded` and `MultiThreadedDebug` in generated Visual Studio projects.
The default job selected SDL + Skia + software and produced `tcvm.dll`; the
fallback selected Native + Legacy + software and produced `tcvm.dll`.
Deferred validation: local Visual Studio validation was intentionally skipped;
the authorized Windows CI performed the equivalent generator/build checks.
Blockers: none.
Next concrete action: review and merge the consumer branch.
Resume command: read this state and evidence; no further execution is required
unless CI or review introduces a new failure.

## Baseline notes

- Branch: `feat/migrate-windows-to-sdl2-skia-raster`.
- The generated depot checkout is dirty only with untracked `local/` artifacts
  and a Python cache; preserve it and do not switch it during source
  verification.
- The repository has no existing `CMAKE_MSVC_RUNTIME_LIBRARY` or MSVC `/MT` /
  `/MD` policy in the inspected CMake files.
