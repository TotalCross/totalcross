<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Warm image copyRect editorial report

## Delivered

Startup now resolves the native optimization masks from a compile-time policy
constant before any settings API or native reset call. The fresh-process smoke
confirms the exact default mask for features 0, 1, 2, 3, 4, and 15.

The native diagnostic bridge now exposes compact physical-identity rejection
lanes. The revalidated 663-JPEG workload shows mapping geometry as the dominant
rejection (4509 at 480x720 and 5898 at 540x960 per pass); backing rejection is
zero.

Physical variant hits now use a proven software raster copy when geometry is
integer, 1:1, in bounds, alpha-safe, and the cached variant has an exact opaque
pixel proof. Otherwise the existing generic path remains available. The
physical-variant smoke confirms one hit and one write-pixels hit with no generic
or smooth draw, while alpha, hardware-scale, rotation, and combined cases keep
their fallback behavior.

The corrected warm micro uses independent materialized, deferred-draw, and
deferred-copy inputs. Full and partial hashes match. Its identity-direct lane
records 2560 physical-identity hits and 2560 write-pixels hits, with zero
generic/smooth draws.

## Workload result

The final matrix covers 663 JPEGs, both resolutions, four feature-13/14
profiles, and three fresh-process passes per profile. Warm p95 is 9 ms for all
profiles at both resolutions, with zero warm JPEG decodes. Variant-enabled warm
passes write 4509 or 5898 cached raster hits; cold variant materialization is
268,180,848 bytes at 480x720 and 339,890,928 bytes at 540x960. The target-color
byte field is emitted and is zero on this macOS target because no conversion was
selected.

## Scope and limitations

Validation was intentionally local to the macOS native runtime and SDK. The
Android, Linux, Windows, and iOS platform matrices were not run. Generated
launchers, SDK artifacts, and unrelated local files remain outside the change.

Evidence: `.agent/evidence/image-warm-copyrect-fast-path/final-revalidated/`.
