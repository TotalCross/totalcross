<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda
SPDX-License-Identifier: LGPL-2.1-only
-->

# Foundation plan handoff

Plan 1 establishes explicit image backing contracts while preserving the
legacy Java raster authority.

## Delivered

- Added package-private raster/native backing abstractions and an immutable
  backing-aware pipeline source.
- Preserved existing `Image` object field indexes and added the transitional
  backing field at slot 9.
- Added opaque Skia backing ownership for mutable surfaces and immutable
  snapshots, including checked dimensions, stale-handle rejection, idempotent
  release, and explicit TotalCross ARGB readback.
- Registered the Java/native bridge through the repository native-method tables.
- Added Java contract/converter coverage and a native lifecycle/readback probe.

## Validation

The focused SDK image/backing tests, SDK distribution build, macOS arm64
`tcvm`/`Launcher` build, and native smoke passed. Full platform validation was
not run because this plan permits only SDK and macOS arm64 gates.

## Handoff to plan 2

`Image.pixels` remains authoritative. The transitional
`adoptRasterBackingCompatibility` helper, legacy raster fields, and the
native-backed materialization boundary are intentionally retained for the
materialization phase. No decoder, generator, or pipeline algorithm migration
was performed.

Final plan-1 commit: `1f81dc624`.
