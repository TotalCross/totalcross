<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Native dependency fetch reliability editorial report

## Editorial Summary

Native dependency preparation now has a shared resilient transport, verified
and reusable installations, deduplicated release metadata, complete checksum
coverage, and an explicit current-run CI artifact boundary.

## Original Plan versus Actual Outcome

All six priorities were implemented without parallel downloads. Existing
artifact names, install paths, fetcher options, and the immutable TotalCross pin
contract remain compatible. Remote publication and pin advancement remain a
separate authorized handoff.

## What Changed

Depot-tools gained a shared GitHub Release helper, provenance installation
markers, staged tree replacement, a one-time Skia shared operation, 177 pinned
asset checksums, and release-time checksum generation. TotalCross gained a
testable sequential preparation script and a clean exact-content archive passed
from `checkout-source` to six downstream jobs.

## Decisions and Trade-offs

GitHub asset digests seed the checked-in contract because every currently pinned
release already exposes them. The producer cache remains an accelerator. The
specialized SDK runtime artifact remains until remote CI proves the broader
transport and an independent cleanup is justified.

## Unexpected Problems and Discoveries

Several fetcher default tags lagged the compatible `deps.yml` pins, so the new
orchestrator resolves defaults from `deps.yml`. A locally yielded validation
process overlapped a manual rerun and exercised rollback; the final single run
completed. This did not introduce parallel production downloads.

## Validation and Measurable Results

Transient curl exit 52 retries and succeeds on the second request. Permanent
404 moves to fallback after one direct request. Valid second installs use zero
requests. Seven-target Skia preparation changes from 98 to 26 first-run requests
and zero after reuse. SQLite API fallback changes from 21 to 9 requests and one
metadata read. The prepared artifact is 62,738,241 bytes with 2,459 members and
71 identities.

## Useful Evidence and Examples

The compact command/result index is
`.agent/evidence/native-dependency-fetch-reliability.md`. Deterministic request
tests live beside the downloader, installation, and Skia scripts; TotalCross
packaging and workflow contracts live in `scripts/test-*.py`.

## Limitations, Remaining Work, and Open Questions

Depot-tools commits must be published and the TotalCross pin advanced to an
immutable ref before remote CI can validate the final service-level upload /
download flow. Platform builds were not repeated because no native archive or
ABI changed.

## Possible Article Angles

- Why immutable CI artifacts are a correctness boundary and caches are not.
- Designing idempotent binary dependency installation around provenance.
- Cutting release traffic without adding parallelism.

## Suggested Narrative

Begin with the observed empty-reply and repeated-download failures, centralize
transport semantics, make installation identity explicit, remove shared-data
duplication, establish cryptographic trust, and finally carry that exact state
across CI jobs.

## Claims Requiring Human Review

Remote GitHub Actions behavior and eventual `sdk-native-runtime` removal require
review after depot-tools publication. No performance claim beyond recorded
request counts and archive size should be inferred.
