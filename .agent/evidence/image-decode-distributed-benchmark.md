<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Evidence: distributed image decode benchmark

- 2026-09-15: branch `perf/image-decode-distributed-benchmark` created from
  `perf/image-scroll-distributed-benchmark`.
- Corpus scan: all six selected `.jpg` sets contain 663 entries; all relative
  names match `imag`. Four `.png` side images occur in each source folder, and
  `lossless` has one `.csv`; these are excluded from the measured set.
- Actions run `34912920763` has artifact `TotalCross-7.2.2` downloaded at
  `/tmp/image-decode-package-34912920763/TotalCross-7.2.2.zip` (128,812,656
  bytes compressed on GitHub).
- Commit `37cfd87d7`: `scripts/package-image-scroll-benchmark.sh` staged the
  six variants and generated six `*Lib.tcz` files with SDK `TCZ`/`Storage` APIs;
  every TCZ's 663 resources passed SDK read-back, name-order, and SHA-256 checks.
- Packaging command passed for `macos-arm64`; package self-test and all four
  scroll smokes passed. Logs are under
  `/tmp/image-decode-package-check-20260915/`.
- The supplied local commit-message check reported a body line over 80 chars for
  `37cfd87d7`; the commit was not rewritten. Future commit bodies will wrap at
  80 characters.
- Commit `d2a2626f1` adds `ImageDecodeBenchmarkApp`, compiles and deploys it
  beside the scroll runtime, and checks its executable/application TCZ during
  bundle self-test. Artifact-SDK Java compilation, shell syntax, focused header
  validation, and commit-message validation passed.
- Commit `b6525af5d` adds the 90-process planner and native process runner.
  A planner fixture passed exact scenario/round counts and shared random-order
  checks. A synthetic 59,670-row aggregation fixture passed count, size, speedup,
  and percentile checks without launching a native benchmark matrix.
- Commit `91ef824aa` adds `decode-detailed.csv`, `process-summary.csv`, and
  `decode-summary.csv`; the synthetic fixture verified 59,670 image rows and 30
  variant/scenario summaries.
- Five native decode smokes passed on the macOS ARM64 bundle built from run
  `34912920763`: imag/filesystem/full/sequential, imag/tcz/full/sequential,
  imag/tcz/full/random, decode-fast/filesystem/half/sequential, and
  decode-fast/tcz/half/sequential. Each processed 663 rows; detailed CSV and
  process logs are under
  `/tmp/image-decode-package-smokes-20260915/output/image-scroll-benchmark-macos-arm64/results/decode/smokes/`.
- Commit `56427147b` packages the decode runner and aggregator, validates them
  during scroll bundle self-test, and invokes decode `full` after scroll
  aggregation and before one combined ZIP. A rebuilt macOS ARM64 package at
  `/tmp/image-decode-integrated-20260915/output/image-scroll-benchmark-macos-arm64`
  passed scroll/decode self-tests, four scroll smokes, and the five decode
  smokes through the packaged top-level runner. Package and per-phase logs are
  in `/tmp/image-decode-integrated-20260915/`.
- A stubbed orchestration check confirmed scroll matrix, scroll aggregation,
  decode full phase, and exactly one ZIP in that order without launching either
  full benchmark matrix. Final `git diff --check`, Bash syntax, Python compile,
  and focused copyright-header validation passed.
- Commit-message validation failed for `56427147b` on body line length; the
  commit was preserved per the logical-commits skill's no-rewrite rule.
