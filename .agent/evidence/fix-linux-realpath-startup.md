<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Evidence index: POSIX realpath startup correction

Append concise records for each hosted run and logical commit. Preserve full
logs as CI artifacts and keep only stable source/result identities here.

## Investigation baseline

- Prior report: [I01-B Linux realpath startup abort](https://github.com/TotalCross/totalcross-performance-lab/blob/investigation/totalcross-7.3.0-functional-validation/investigations/7.3.0-functional/reports/I01-B-linux-realpath.md)
- Prior run: [Actions 45949849457](https://github.com/TotalCross/totalcross-performance-lab/actions/runs/45949849457), jobs 140342925090 and 140343721563; Ubuntu 24.04 x86_64, glibc 2.39.
- Source revisions: official 7.3.0 `5464c21e3107e3f1ce9c287b724829e2bb870c1d`; frozen master `6b915883dd202f6db891990f6c3af26fb645ecc7`.
- Frozen-master observations: normal/short/long/symlink all exit 134 at `__realpath_chk -> startVM -> executeProgram -> main`; short executable path 68 bytes. The report did not preserve `_FORTIFY_SOURCE` or resolved size.
- Build identity: `totalcross/linux-amd64:v1.0.7`, Release, depot-tools `0fd3c98727c87a441bc9c8d2c8dd2b731ef0861a`, libpng `1.6.48-r3`, zlib-ng `2.1.6-r3`.
- Current source contract: `startup_c.h` passes `MAX_PATHNAME` (Linux 1024) to `realpath`; glibc 2.39 `__realpath_chk` calls `__chk_fail()` when `resolvedlen < PATH_MAX` (runner PATH_MAX 4096). Sources: [glibc 2.39 realpath_chk.c](https://github.com/bminor/glibc/blob/release/2.39/master/debug/realpath_chk.c), [glibc fortification manual](https://sourceware.org/glibc/manual/2.42/html_node/Source-Fortification.html), [POSIX realpath](https://man7.org/linux/man-pages/man3/realpath.3p.html).

## New correction branch runs

- Baseline test commit: `4b591083ac1f919217ef8912037c2d08ff161217`.
- Hosted run: [46044420755](https://github.com/TotalCross/totalcross/actions/runs/46044420755),
  job 140659968957, success. Runner Ubuntu 24.04 x86_64, glibc
  `2.39-0ubuntu8.12`, GCC `13.3.0-6ubuntu2~24.04.1`, `PATH_MAX=4096`,
  `MAX_PATHNAME=1024`, `_FORTIFY_SOURCE=2`; test binary imports
  `__realpath_chk`. The production helper aborted in its child with
  `*** buffer overflow detected ***` and `SIGABRT`; the parent test passed.
- Earlier test-only attempts: runs [46043804169](https://github.com/TotalCross/totalcross/actions/runs/46043804169)
  and [46044147440](https://github.com/TotalCross/totalcross/actions/runs/46044147440)
  failed at compile time due to isolated harness/header and `-Wformat-truncation`
  issues. Neither executed the helper. The corrected harness passed in
  `46044420755`.
- Baseline evidence artifact was downloaded and inspected from run `46044420755`;
  the compact runner and result values above are retained in this index.

## Corrected implementation and final hosted validation

- Fix commit: `1539182de` (`fix(posix): allocate realpath result safely`). Test
  and workflow series ends at `c752de6084135f66164401f83f72354e003a2099`.
- Final hosted run: [Actions 46051419602](https://github.com/TotalCross/totalcross/actions/runs/46051419602),
  success. Jobs: helper regression `140683399430`; canonical Linux build
  `140683770360`; canonical macOS ARM64 build `140683770286`; SDK `140683562550`;
  deployed Linux runtime `140684411981`; deployed macOS runtime `140684412088`.
- Helper job: Ubuntu 24.04 x86_64, glibc `2.39-0ubuntu8.12`, GCC
  `13.3.0-6ubuntu2~24.04.1`, `PATH_MAX=4096`, `MAX_PATHNAME=1024`. The old
  helper compiled with `_FORTIFY_SOURCE=2` imports `__realpath_chk` and aborts
  with `SIGABRT`; the corrected helper passes startup-safe and contract modes.
  ASan/UBSan with leak detection passes the full contract suite.
- Canonical Release build inspection: `startup.c` compiles with `-O3`, no
  explicit `_FORTIFY_SOURCE` definition, and the corrected `libtcvm.so` does not
  import `__realpath_chk`. This is recorded separately from the explicit
  fortified baseline reproducer.
- Canonical CTest: Linux and macOS each pass both
  `copy-resolved-path-contract` and `copy-resolved-path-startup-safe` (2/2).
- Deployed Linux: all five cases exit 0 and report expected real `appPath` and
  `vmPath`: normal (94-byte executable path), short (37), bounded-long (206),
  symlink (84), and relative (19). Each reaches startup completion and passes
  file write/read/delete, SQLite reopen plus `integrity_check=ok`, and 8x8 JPEG
  decode/draw controls. The deployed VM matches the canonical Linux artifact;
  SHA-256 `827a962dd094ea63a20b8efa02b3236c63cae29e5b66eca351ffede793d79486`.
  Linux launcher SHA-256:
  `2e0f74c20554d1f86e4c3a3dcc7221a69d8547a2d62cc90ee2449f840a5f2477`.
- Deployed macOS: ARM64 runner, normal and symlink cases exit 0, reach startup
  completion, and report expected real paths with no `DYLD_LIBRARY_PATH` or
  `DYLD_INSERT_LIBRARIES`. Deployed dylib matches the canonical artifact;
  SHA-256 `120d5e805f590c5ea4088ff98dc6631706e015d591c01cafa43a0d03638c88c7`.
  The VM carries `@rpath/libtcvm.dylib` as its install name.
- Final evidence artifacts in run `46051419602`: `native-startup-linux-helper-c752de6084135f66164401f83f72354e003a2099`,
  `native-startup-linux-runtime-c752de6084135f66164401f83f72354e003a2099`,
  and `native-startup-macos-runtime-c752de6084135f66164401f83f72354e003a2099`.

## Validation notes and limits

- Superseded hosted attempts exposed harness setup issues and were corrected
  before the final passing run: missing Linux EGL/GLES runtime libraries;
  Java-source resolution from a temporary working directory; macOS launcher
  dynamic loading; the native app's TCZ path cap; and Python's normalization of
  a literal `./` launch path. Final run `46051419602` passed all cases.
- `scripts/test-build-provenance.py` passed (19 tests),
  `scripts/test-build-identity.py` passed (10 tests), and
  `scripts/test-artifact-identity.py` passed (33 tests). Python syntax, Ruby
  workflow YAML parsing, focused copyright validation, and `git diff --check`
  passed.
- `scripts/test-native-dependencies-workflow.py` fails its existing assertion
  that `build.yml` contains seven `prepared-native-dependencies` references;
  the base revision already contains eight. This unrelated assertion was not
  changed.
- No native desktop build or runtime test was run locally. Android, iOS,
  Windows, ARM Linux cross-builds, and benchmarks were not run; their jobs are
  skipped in the focused hosted workflow.
