<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Active state: distributed image decode benchmark

- Active milestone: complete; only the requested full benchmark matrices remain deferred.
- Branch: `perf/image-decode-distributed-benchmark`, created from the checked
  out `perf/image-scroll-distributed-benchmark` HEAD.
- Last implementation commit: `56427147b feat(benchmark): integrate decode into distributed suite`.
- Closeout paths: `scripts/README-image-benchmarks.md`,
  `.agent/plans/image-decode-distributed-benchmark.md`,
  `.agent/state/image-decode-distributed-benchmark.md`, and
  `.agent/evidence/image-decode-distributed-benchmark.md`.
- SDK artifact: `/tmp/image-decode-sdk-34912920763/TotalCross/`.
- Next action: none; implementation, documentation, and focused acceptance are complete.
- Validation completed: synthetic aggregation fixture and 90-job planner
  fixture passed; the rebuilt packaged artifact passed scroll/decode self-tests,
  four scroll smokes, all five requested decode native smokes, and a stubbed
  orchestration-order assertion. Final diff, syntax, Python, and header checks
  passed. The full matrices remain deferred.
- Deferred validation: full 126-scroll and 90-decode matrices.
- Active decisions: see the plan's Decision Log, especially 663 `.jpg` selection
  and `aggressive-*` source to `aggresive-*` bundle mapping.
- Deliberate local paths: downloaded SDK artifact under `/tmp`, original corpus
  under `~/Downloads/win32/win32`, and all unrelated `.agent/benchmarks`.
- Commit-message checks reported over-80-character body lines for `37cfd87d7`
  and `56427147b`; neither commit was rewritten. Commits `d2a2626f1`,
  `b6525af5d`, and `91ef824aa` passed. The final documentation commit body is
  wrapped to at most 80 characters per line.
- Resume command: read this state and the active milestone in
  `.agent/plans/image-decode-distributed-benchmark.md`.
