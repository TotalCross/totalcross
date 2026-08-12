<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Native dependency fetch reliability evidence

- 2026-08-12, baseline: TotalCross `cc5ab3aa8`; depot-tools `01e346b`; consumer
  pin `repo-2026.08.12`; generated `local/` trees deliberately preserved.
- Downloader fixture: `python3 scripts/tests/test-github-release.py`; passed 5
  tests. Curl exit 52 used 2 requests (failure then success); direct 404 used 1
  attempt before metadata/API fallback; two fallback assets used request kinds
  `direct,metadata,api_asset,api_asset`; checksum mismatch left output intact.
- Install fixture: `python3 scripts/tests/test-artifact-install.py`; passed 3
  tests. First/second identical fetch request count was 1/0; incomplete content
  and changed tag refetched; failed download and failed final move preserved the
  preceding valid tree.
- Skia shared fixture: `python3 skia/scripts/test-install-shared-release.py`;
  passed 1 test. One dev bundle plus two fixture manifests made 3 requests on
  first install and 0 on second.
- Checksum contract: 15 pinned releases, 177 assets, 37 assets in the Skia
  release, and all 34 TotalCross-consumed Skia library/config/dev/manifest
  assets have configured SHA-256 values.
- TotalCross preparation dry run: 71 sequential commands; seven target Skia
  fetches; one shared Skia fetch; seven SQLite targets with one effective
  repository/tag identity.
- Local milestone preparation: completed using the nested unpublished
  depot-tools checkout. A clean marker-backed rerun recorded 71 entries, 71
  reuses, 0 artifact requests, and 0 metadata requests. Full logs:
  `/tmp/native-dependency-preparation.log` and
  `/tmp/native-dependency-preparation-rerun.log`.
- Prepared archive: `/tmp/depot-tools-native-dependencies.tar.gz`; 62,738,241
  bytes (59.83 MiB), 2,459 members, 71 identity records, 0 `.git` paths, and 0
  session-cache paths. Packaging summary:
  `/tmp/native-dependency-package-summary.json`.
- TotalCross fixtures: `python3 scripts/test-package-native-dependencies.py`
  and `python3 scripts/test-native-dependencies-workflow.py`; each passed 1
  test. Workflow YAML parsed successfully with Ruby Psych.
- Deferred: native platform rebuilds, remote Actions workflow, artifact upload /
  download service behavior, and removal of `sdk-native-runtime`. Archive/ABI
  content did not change, and remote CI cannot consume unpublished depot-tools
  `c5b80eb` through the immutable TotalCross pin.
