<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# State: POSIX realpath startup correction

- Branch: `fix/linux-realpath-startup`
- Base: `52a947b5295321d7350350bd618c55fb26304c00`
- Worktree: `/Users/flsobral/repos/totalcross-fix-linux-realpath-startup`
 Status: implementation and hosted validation complete; final records are
 included in the closing documentation commit.
 Last code commit: `c752de6084135f66164401f83f72354e003a2099`
  (`fix(test): preserve relative launcher path`).
- Active paths: `TotalCrossVM/src/init/posix/startup_c.h`,
  `TotalCrossVM/src/util/xtypes.h`, `TotalCrossVM/CMakeLists.txt`, native
  startup tests, relevant workflow, and the named plan/evidence/report files.
- Completed: repository policies and `logical-commits` / `validate-headers`
  skills read; prior investigation report and run evidence inspected; glibc
  2.39 source confirms short paths still fail when buffer < `PATH_MAX`; shared
  POSIX helper now uses allocation-form `realpath(path, NULL)` and frees the
  returned allocation. Native helper contract, startup-safe, sanitizer, and
  focused Linux/macOS runtime workflows are committed.
- Validation: hosted baseline reproduction passed in Actions run
  [46044420755](https://github.com/TotalCross/totalcross/actions/runs/46044420755),
  job 140659968957. Ubuntu 24.04 x86_64, glibc 2.39, `PATH_MAX=4096`,
  `_FORTIFY_SOURCE=2`, `__realpath_chk`, and `MAX_PATHNAME=1024`; child
  terminated with `SIGABRT`. Final hosted run
  [46051419602](https://github.com/TotalCross/totalcross/actions/runs/46051419602)
  passed helper contract/sanitizer checks, Linux and macOS CTest suites,
  canonical Linux/macOS/SDK builds, five Linux deployed path cases with
  file/SQLite/JPEG controls, and two macOS startup cases. Earlier hosted
  iterations caught and corrected runner dependencies, probe path handling,
  deployed path-length bounds, and literal relative launch handling.
 Next action: none after the closing documentation commit is pushed.
- Deferred: all desktop native validation, build, smoke, and sanitizer runs are
  hosted-only; no Android/iOS/Windows AMD64 tests or benchmarks.
- Blockers: none currently.
- Deliberately untouched: original checkout's unrelated untracked files and
  `TotalCrossVM/deps/totalcross-depot-tools` in that checkout; performance-lab
  investigation branch.
- Resume: read this state, then inspect the active workflow/test paths above.
