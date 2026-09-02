<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image native backing plan 03 milestone-2 checkpoint

Milestones 1 and 2 are complete at implementation commit `04a7bfa0a`.
Execution is intentionally stopped before milestone 3 so the next turn can
resume from `.agent/state/image-native-backing-03.md`.

The delivered slice adds `ImageGeometryPlan`, geometry-plan caching, native
Skia execution/materialization, source-to-result operation ordering, frame and
crop metadata preservation, nearest and Catmull-Rom sampling, alpha/hardware
scale compatibility, and the generated native method registrations. Color
pipelines remain on the existing resolver for plan 4.

Passed checkpoint evidence is appended to
`.agent/evidence/image-native-backing-03.jsonl`: final macOS arm64
configure/build, focused SDK tests, SDK distribution, smoke compilation,
header validation, direct geometry smoke, crop/frame differential smoke, and
the native materialization/ImageControl regression smoke.

The broad legacy lazy and ABI smokes were run but remain deferred. They assert
old mutable or identity-preserving `getPixels()` behavior and exercise color
native mutation paths outside this geometry milestone; their failures and
logs are recorded in the evidence index. Android, iOS, Linux, Windows, and the
full platform matrix remain deferred by roadmap scope. Plan 4 has not started.
