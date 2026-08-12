<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Native dependency fetch reliability state

Status: locally complete at remote publication handoff.

Depot-tools last commit:
`c5b80eb1bb694d34aade98a3e30dcbfd74221353` on local `main`.

TotalCross last commit:
`9642399b9ce20f1b406a32f0f7b77e015ed3262f` on
`feat/logical-ui-scaling2`.

Active paths are the plan, evidence, state, and editorial report. Functional
source/workflow paths are committed and have no pending task changes. Generated
dependency `local/` trees and unrelated TotalCross files remain deliberately
untracked/untouched.

Focused validation completed:

- depot-tools downloader, idempotency, Skia shared, and native release tests;
- real public libpng fetch;
- 71-entry local TotalCross preparation and zero-request valid rerun;
- 59.83 MiB exact-content packaging/extraction fixture;
- TotalCross workflow producer/consumer static contract and YAML parse;
- focused copyright validation and `git diff --check` in both repositories.

Deferred validation: platform builds and remote GitHub Actions. No ABI or
archive layout changed, and the committed TotalCross pin cannot reach local
depot-tools commits.

Next action requires external state: publish depot-tools through `c5b80eb`,
select its immutable tag or commit ref, update
`TotalCrossVM/deps/totalcross-depot-tools.ref` in a new authorized TotalCross
commit, and run remote CI. Do not point the pin at `main`.

Resume commands:

    git -C TotalCrossVM/deps/totalcross-depot-tools log --oneline 01e346b..c5b80eb
    git log --oneline cc5ab3aa8..9642399b9
    sed -n '1,220p' .agent/state/native-dependency-fetch-reliability.md
