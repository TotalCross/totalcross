<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Settings.iosCertDate — Editorial Report

## Editorial Summary

The iOS deployment path now carries the provisioning profile expiration date from metadata discovery, through `tcparms.bin`, into runtime `Settings.iosCertDate`.

## Original Plan versus Actual Outcome

The ordering defect, semantic contract, serialization, runtime loading, and JVM state isolation were implemented. A final TCZ/runtime harness passed. A complete IPA smoke build was not possible because the local iOS template is absent.

## What Changed

- iOS paths and provisioning metadata are resolved before `J2TC.process()`.
- `iosCertDate` preserves the historical provisioning profile expiration semantics.
- Runtime loading uses `Vm.getFile("tcparms.bin")` and existing key/value data.
- Missing or invalid values remain `null`; deployment state is reset between runs.

## Decisions and Trade-offs

The public field was preserved for compatibility. The provisioning profile date remains the source of truth, even though its name mentions a certificate. The runtime consumer was implemented in Java because the native VM had no matching parameter parser or setting field.

## Unexpected Problems and Discoveries

The native VM contains only generic TCZ resource lookup for `tcparms.bin`. Also, `Time.toIso8601()` emits `YYYYMMDDTHH:MM:SS`, which the runtime parser now accepts without changing the artifact format.

## Validation and Measurable Results

Focused policy, deployment, runtime-parameter, and SDK distribution validations passed. The final artifact/runtime harness generated `IOSDateRuntimeFixture.tcz`, found `tcparms.bin`, matched its date through the loader, and confirmed that a parameter set without `iosCertDate` leaves the field `null`. The focused log is `TotalCrossSDK/agent-logs/20260724-171548-test-agent.log`.

## Useful Evidence and Examples

Evidence is recorded in `.agent/evidence/396-settingsioscertdate-is-empty.jsonl`. The final harness is `IOSCertDateArtifactRuntimeTest`.

## Limitations, Remaining Work, and Open Questions

The local `TotalCrossSDK/dist/vm/ios/TotalCross.ipa` template is absent, so IPA signing and on-device iOS execution were not observed. No production credentials were used.

## Possible Article Angles

- A deployment-ordering bug that silently dropped metadata from generated artifacts.
- Preserving a legacy public API while correcting its documented meaning.
- Using an existing resource bridge to complete runtime propagation without native format changes.

## Suggested Narrative

Show the original post-conversion discovery, the missing `tcparms.bin` key, the pre-conversion metadata step, and the final artifact-to-runtime harness result.

## Claims Requiring Human Review

A maintainer should confirm that provisioning profile expiration remains the intended public contract and decide whether a future API should expose certificate expiration separately.
