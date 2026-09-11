<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Warm image copyRect editorial report

## Delivered

Startup resolves the native optimization masks from a compile-time policy
constant before any settings API or native reset call. The fresh-process smoke
confirms the exact default mask for features 0, 1, 2, 3, 4, and 15.

The native diagnostic bridge exposes compact physical-identity rejection lanes.
In the final 663-JPEG workload, mapping geometry remains the dominant cold-path
rejection (663 at both resolutions); backing rejection is zero.

Physical variant hits now use a proven software raster copy when geometry is
integer, 1:1, in bounds, alpha-safe, and the cached variant has an exact opaque
pixel proof. Otherwise the existing generic path remains available. The
physical-variant smoke confirms one hit and one write-pixels hit with no generic
or smooth draw, while alpha, hardware-scale, rotation, and combined cases keep
their fallback behavior.

The cached-final copyRect path checks destination scale and source decode
generation before using the final raster cache. The variant-first regression
creates a physical variant when no final raster exists, verifies its hit,
materializes the equivalent final raster, observes one eviction, then verifies
cached-final copyRect reuse. A scale-mismatched physical variant remains
resident. Hashes are unchanged, and drawImage ordering is unchanged.

The corrected warm micro uses independent materialized, deferred-draw, and
deferred-copy inputs. Full and partial hashes match. Its identity-direct lane
records 2560 physical-identity hits and 2560 write-pixels hits, with zero
generic/smooth draws.

## Workload result

The final matrix covers 663 JPEGs, both resolutions, four feature-13/14
profiles, and three fresh-process passes per profile. All 24 records passed.
Warm p95 is 9 ms for all profiles at both resolutions, with zero warm JPEG
decodes, physical-variant stores, and physical-variant bytes. Feature 14 is
still separately validated when no final raster exists: its mutation smoke
records one materialization and 160,000 physical-variant bytes. The target-color
byte field is emitted and is zero on this macOS target because no conversion was
selected.

## Scope and limitations

Validation was intentionally local to the macOS native runtime and SDK. The
Android, Linux, Windows, and iOS platform matrices were not run. Generated
launchers, SDK artifacts, and unrelated local files remain outside the change.

Evidence: `.agent/evidence/image-warm-copyrect-fast-path/copyrect-revalidated/`.
