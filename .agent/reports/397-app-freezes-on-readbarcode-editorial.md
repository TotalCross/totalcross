<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# ExecPlan 397 Editorial Handoff

## Editorial Summary

Issue #397 addressed an iOS `Scanner.readBarcode()` path that could activate the camera without presenting usable scanner UI and could keep the caller waiting indefinitely. The completed change replaces that fragmented path with an explicit session lifecycle, a visible overlay owned by the active TotalCross controller, asynchronous UIKit and AVFoundation initiation, and one completion signal for the synchronous VM caller.

## Original Plan versus Actual Outcome

The plan called for a real-device reproduction and final device matrix. The user explicitly waived physical-device tests and reproduction. Instead, the completed evidence consists of static inspection, a focused C state test, and a successful unsigned iPhoneOS archive using the repository-supported CMake, CocoaPods, and Xcode flow. No physical-camera result is inferred from that archive.

## What Changed

- Introduced `TCBarcodeSession` state, generation gating, and idempotent completion.
- Replaced the `barwindow` presentation path with a full-screen scanner overlay on `MainViewController.view`.
- Moved authorization and capture setup to a serial capture queue, retaining UIKit work on the main queue.
- Replaced `Sleep(100)` polling with a completion semaphore that only the VM caller waits on.
- Added controlled main-thread rejection and a 15-second setup timeout.
- Returned UTF-8 payloads safely across the C bridge and cancelled an active scan when the app resigns active.

## Decisions and Trade-offs

The public Java API remains synchronous. This preserves compatibility while allowing the iOS main thread to remain available for permission, presentation, capture, and cancellation. The implementation adds no active-scan timeout; once setup succeeds, scanning remains user-controlled. The scanner is presented as an in-view overlay rather than a second `UIWindow`, avoiding the original invisible-window failure mode.

## Unexpected Problems and Discoveries

The initial validation stopped before scanner compilation because the local depot-tools checkout lacked `axtls/axtls_pbkdf2.h`. Updating depot tools supplied the iOS arm64 header. The first CocoaPods integration then did not apply the generated tcvm target settings because the locally installed `xcodeproj` reported unsupported CMake-generated project attributes. Running `pod install` again, then the prescribed dependency patch, applied the required configuration and allowed the archive to complete.

## Validation and Measurable Results

Passed:

- Pure-C barcode-session transition test with `-Wall -Wextra -Werror`.
- `git diff --check` for the change set.
- iPhoneOS Release archive with `CODE_SIGNING_ALLOWED=NO` after CMake generation, CocoaPods integration, and the dependency patch.

Artifacts:

- `TotalCrossVM/xcode/build/TotalCross.xcarchive`
- `TotalCrossVM/xcode/build/logs/TotalCross-retry3.xcresult`
- `/tmp/exec397-xcodebuild-retry3.log`

## Useful Evidence and Examples

The evidence ledger is `.agent/evidence/397-app-freezes-on-readbarcode.jsonl`. It includes the prior axtls failure, the final passing archive command, and its limitations. The state transition test covers start eligibility, valid transitions, repeated completion, stale callbacks, concurrent starts, and pre-session completion.

## Limitations, Remaining Work, and Open Questions

Physical-device reproduction and the manual scanner matrix were waived by the user. Consequently, there is no evidence for camera permission interaction, actual barcode decoding, cancellation interaction, rotation, lifecycle behavior on hardware, repeated scans on hardware, or the privacy-indicator shutdown. The iPhoneOS archive confirms compilation and linkage, not those runtime properties. Generated Xcode/CocoaPods files and local archive artifacts were intentionally not committed.

## Possible Article Angles

- Converting a legacy synchronous mobile API into a safe asynchronous platform bridge.
- Why camera activation does not prove scanner UI presentation succeeded on iOS.
- Using generation ownership and a single finalizer to tame native callback lifecycles.

## Suggested Narrative

Start with the observed symptom: the camera indicator appeared while the application looked frozen. Trace it to a nil or unusable secondary window and a polling caller with no universal completion path. Then show how an explicit session object, controller-owned overlay, capture queue, and one semaphore-based completion boundary restore the separation between responsive UI work and synchronous VM compatibility.

## Claims Requiring Human Review

- The original iPad freeze no longer occurs on an eighth-generation iPad running iOS 17.5.
- The scanner decodes supported 1D, 2D, and Unicode payloads on physical hardware.
- Permission, cancellation, rotation, backgrounding, interruptions, and camera privacy-indicator behavior meet the full acceptance matrix.
- The selected 15-second setup timeout is appropriate for all supported devices and deployments.
