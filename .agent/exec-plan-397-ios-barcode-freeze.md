<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda
SPDX-License-Identifier: LGPL-2.1-only
-->

# Fix the iOS freeze in `Scanner.readBarcode()`

This ExecPlan follows `AGENTS.md` and `.agent/PLANS.md`. It is a living document. Keep `Progress`, `Surprises & Discoveries`, `Decision Log`, `Outcomes & Retrospective`, and `Revision Note` current as implementation proceeds.
All implementation work belongs on `fix/397-app-freezes-on-readbarcode`. The branch was created from `master`. A remote comparison performed on 2026-07-22 confirmed that both refs were identical at commit `641a443b68361e78caabbe22ae68436b5809c72d`. Recheck that relationship before beginning each milestone because `master` may advance while this plan is active.
The associated issue is `TotalCross/totalcross#397`, titled `App freezes on readBarcode`. It is open, labeled `bug`, assigned to milestone 7.3.0, and reports the failure on an eighth-generation iPad running iOS 17.5. The camera privacy indicator becomes active, but no usable scanner interface is shown and the application stops responding.

## Purpose / Big Picture

After this change, calling `totalcross.io.device.scanner.Scanner.readBarcode(String mode)` on iOS must present a visible barcode-scanning interface, keep the iOS main thread responsive, allow the user to cancel, and return success or a controlled failure to the TotalCross application. The scanner must release the camera, preview layer, callbacks, and presentation objects after every completion path.
A developer must be able to demonstrate the fix by launching a minimal TotalCross application on an iOS device, pressing a button that invokes `readBarcode`, scanning a supported code, and observing the decoded value in the application. The same application must remain responsive when camera permission is requested, denied, or previously denied; when the user cancels; when no camera input is available; when the application is backgrounded; and when scanning is invoked more than once.
The public Java API remains synchronous for milestone 7.3.0. Internally, UIKit and AVFoundation work must remain asynchronous on the iOS main thread. Only the TotalCross VM caller thread may wait for completion, and that wait must be released by every success, cancellation, initialization failure, permission failure, lifecycle interruption, or safety-timeout path.

## Working Set and Resume Protocol

When checked into the repository, keep the active plan at:

    .agent/plans/397-app-freezes-on-readbarcode.md

Use these supporting files only when they make resumption safer:
- `.agent/state/397-app-freezes-on-readbarcode.md` is the first normal read on resume. Rewrite it rather than appending. Record the active milestone and slice, last logical commit, active paths, next concrete action, focused validation completed, deferred validation and reason, active decisions, blockers, deliberately out-of-scope files, and a resume command.
- `.agent/evidence/397-app-freezes-on-readbarcode.jsonl` is append-only. Each entry records timestamp, revision, milestone and slice, command or manual procedure, status, compact result, log or artifact path, device and OS when relevant, and known limitation.
- `.agent/archive/397-app-freezes-on-readbarcode-history.md` stores completed milestone detail, rejected alternatives whose rationale remains useful, and retired revision notes. Do not read it during an ordinary resume.
- `.agent/reports/397-app-freezes-on-readbarcode-editorial.md` is the factual milestone and final handoff. Update it at important milestone boundaries, not after every small edit.
Do not duplicate complete logs, videos, thread dumps, or test matrices in all supporting files. Store raw evidence once and point to it.
On resume, read the state file first. If it does not exist, read `Progress`, the active milestone in `Plan of Work`, `Decision Log`, `Risks and Open Questions`, and `Validation and Acceptance`. Read `Current Architecture and Scope` again only when the next change touches a contract that has not yet been confirmed.
Run the following commands from the repository root before editing:

    git fetch origin
    git switch fix/397-app-freezes-on-readbarcode
    git pull --ff-only
    git rev-parse HEAD
    git merge-base --is-ancestor origin/master HEAD
    git diff --stat
    git status --short -- \
      TotalCrossSDK/src/main/java/totalcross/io/device/scanner/Scanner.java \
      TotalCrossVM/src/nm/io/device/scanner/zxing.c \
      TotalCrossVM/src/nm/ui/darwin/mainview.m \
      TotalCrossVM/src/nm/ui/darwin/mainview.h \
      TotalCrossVM/src/event/darwin \
      TotalCrossVM/xcode \
      .agent

A nonzero result from `git merge-base --is-ancestor` means the branch no longer contains the current remote baseline. Do not use `git reset --hard`, `git checkout --`, force push, or any command that discards local work. Inspect the divergence, record it in state, and preserve unrelated changes.

## Progress

- [x] (2026-07-01T20:22:40Z) Issue #397 was created with a report that `Scanner.readBarcode` freezes on iOS while the camera privacy indicator is visible.
- [x] (2026-07-21T23:00:00-03:00) Static analysis traced the Java native call through the C bridge to the Darwin implementation.
- [x] (2026-07-21T23:00:00-03:00) Static analysis found that `barwindow` and `barCodeButton` are used by the scanner presentation path without a visible initialization in the analyzed flow.
- [x] (2026-07-21T23:00:00-03:00) Static analysis found a blocking `while (callingBarcode) Sleep(100)` loop with no guaranteed completion path.
- [x] (2026-07-21T23:00:00-03:00) Static analysis found `dispatch_sync(dispatch_get_main_queue(), ...)`, which can deadlock when the caller already runs on the main thread.
- [x] (2026-07-22T12:00:00-03:00) The branch `fix/397-app-freezes-on-readbarcode` was confirmed identical to `master` at commit `641a443b68361e78caabbe22ae68436b5809c72d`.
- [x] (2026-07-22T12:00:00-03:00) This plan was rewritten in English using the resumable ExecPlan structure required by the supplied planning standard.
- [x] (2026-07-22T12:00:00-03:00) Added bounded diagnostic logging to the iOS barcode bridge, setup, presentation, metadata, cancellation, and caller-wait paths; barcode values are not logged.
- [x] (2026-07-22T12:00:00-03:00) Physical-device reproduction and capture were explicitly waived by the user; no physical-device behavior is claimed as validated.
- [x] Milestone 1: static analysis and bounded diagnostic instrumentation completed without changing scanner behavior; physical reproduction was waived by the user.
- [x] (2026-07-22T12:00:00-03:00) Added and ran the pure-C barcode session-state test for start, transition, repeated-finalization rejection, stale-generation rejection, concurrent-start rejection, and pre-session finish rejection.
- [x] Milestone 2: introduced per-session state and one idempotent completion path while preserving the current public API. Physical-device validation was waived by the user.
- [x] (2026-07-22T12:00:00-03:00) Replaced the `barwindow` presentation path with a full-screen `MainViewController` overlay containing the preview layer, scan highlight, accessible Cancel button, generation ownership, and rotation/safe-area layout.
- [x] Milestone 3: replaced the uninitialized window path with a scanner overlay presented from the active TotalCross view controller. Physical-device validation was waived by the user.
- [x] (2026-07-22T12:00:00-03:00) Routed authorization, capture configuration, metadata filtering, and `startRunning` through the session generation and centralized finalizer.
- [x] Milestone 4: made permission and AVFoundation initialization asynchronous and routed each setup failure through the centralized completion path. Physical-device validation was waived by the user.
- [x] (2026-07-22T12:00:00-03:00) Replaced scanner polling with its session semaphore, queued main-thread start asynchronously, rejected main-thread callers with a controlled error, and added a 15-second setup timeout.
- [x] Milestone 5: replaced polling and unsafe main-queue dispatch with a safe bridge between the VM thread and the main thread. Physical-device validation was waived by the user.
- [ ] Milestone 6: correct UTF-8 result handling, lifecycle cleanup, stale callbacks, rotation behavior, and repeated scans.
- [ ] Milestone 7: run focused automated checks, build the iOS target, complete the device matrix, and produce final evidence and retrospective.

## Current Architecture and Scope

`TotalCrossSDK/src/main/java/totalcross/io/device/scanner/Scanner.java` exposes the public TotalCross scanner API. `readBarcode(String mode)` is native on the target platform. Its synchronous return is part of the existing API contract and is not replaced in this bug fix.
`TotalCrossVM/src/nm/io/device/scanner/zxing.c` is the native bridge. On iOS it calls `iphone_readBarcode`, waits for a C result, and creates the corresponding TotalCross string object. This boundary must preserve memory validity and encoding until the TotalCross object has copied the result.
`TotalCrossVM/src/nm/ui/darwin/mainview.m` owns the observed iOS implementation. The analyzed baseline contains camera-session setup, barcode metadata callbacks, preview-layer presentation, cancellation, and the blocking wait. Relevant symbols include:

    iphone_readBarcode
    readBarcode:
    mountBarCodeWindow
    captureOutput:didOutputMetadataObjects:fromConnection:
    closeBarcode:
    callingBarcode
    barwindow
    barCodeButton

`TotalCrossVM/src/nm/ui/darwin/mainview.h` declares the Darwin main view controller and camera-related fields. Update it only for state or interfaces that genuinely belong to the controller. Avoid adding more file-level globals.
`TotalCrossVM/src/event/darwin` contains the Darwin event integration. Inspect it only far enough to establish which thread invokes the native method and how the VM thread interacts with the iOS main thread. Do not refactor unrelated event delivery.
`TotalCrossVM/xcode` contains the iOS project and build path. The checked-in `TotalCross.xcodeproj` is application scaffolding. `TCVM.xcodeproj` is generated by CMake. Do not patch generated project files or change unrelated native dependency linkage for this issue.
The probable failure sequence in the analyzed baseline is:

    TotalCross VM thread calls Scanner.readBarcode
      -> zxing.c calls iphone_readBarcode
      -> mainview readBarcode starts camera work
      -> code dispatches synchronously to the main queue
      -> AVFoundation activates the camera
      -> mountBarCodeWindow sends messages to a nil or unusable window
      -> no preview or cancellation control becomes usable
      -> VM-side code loops while callingBarcode remains true
      -> only success or closeBarcode can clear the flag
      -> the application appears frozen

Objective-C permits messages to `nil`; they return zero-like values rather than raising an exception. This explains how the camera can start and the privacy indicator can appear while no scanner UI is visible.
UIKit is the iOS user-interface framework. AVFoundation is the media framework that supplies `AVCaptureSession`, camera inputs, metadata outputs, and preview layers. UIKit presentation and most AVFoundation configuration callbacks must coordinate with the main thread. The main thread must never wait for work whose completion also requires the main thread.
The patch may change the Java documentation, C bridge, Darwin Objective-C implementation, focused tests or test harnesses, and plan support files. It must not redesign Android scanner behavior, introduce a new public asynchronous Java API, refactor unrelated camera features, or replace the general TotalCross event model.

## Plan of Work

### Milestone 1 — Reproduce and establish observable facts

The goal is to replace assumptions with a repeatable failure and enough instrumentation to identify the blocked thread, active session, and failed presentation. At the end of this milestone, the existing bug still occurs, but the evidence must show where it occurs and why the caller cannot complete.
Create a minimal TotalCross application with a button that invokes `Scanner.readBarcode("")`. Do not call the scanner from a constructor, startup callback, or while another controller is being presented. Display the returned value or error visibly.
Reproduce first on the reported class of device when available: an eighth-generation iPad on iOS 17.5 or the nearest available iPad and OS. Also exercise one iPhone if available. The simulator may validate presentation and permission branches, but it is not evidence that physical camera capture works.
Add temporary, low-volume logs around session start, caller thread, permission status, input/output creation, presentation, `startRunning`, metadata callback, cancel, finish, and return to the bridge. Give each invocation a monotonically increasing session identifier. Log pointer presence as booleans rather than dumping addresses when the address is not useful.
Capture:
- the Xcode main-thread stack during the freeze;
- the VM caller-thread stack;
- whether the native call originates on the main thread;
- `AVAuthorizationStatus`;
- whether `barwindow`, `barCodeButton`, `_session`, `_input`, `_output`, `_prevLayer`, and the active view controller exist;
- whether the session is running;
- whether the preview layer is attached to a visible layer hierarchy;
- whether `callingBarcode` can be cleared without a recognized barcode.
Store complete logs and thread dumps outside the active plan. Record compact paths and conclusions in the evidence file.
Do not fix presentation or synchronization yet. The normal validation level is level 1 for instrumentation-only diffs and a manual focused reproduction. Create an automated regression test only if an existing Darwin-native test target can reproduce the state without inventing a large test framework.
Acceptance for this milestone is a reproducible freeze with evidence that distinguishes at least these cases: main-queue deadlock, missing UI presentation, and VM-thread wait that never receives a completion signal.
Suggested logical commit:

    test(scanner,ios): capture barcode freeze state

### Milestone 2 — Add explicit session state and one completion path

The goal is to make scanner lifetime understandable and safe before changing the UI. At the end, every existing success, cancellation, and setup-error path must call one idempotent finalizer, even though presentation and waiting may still use the old mechanism temporarily.
Replace `callingBarcode` as the sole state mechanism with a single active session record. The implementation may use a small Objective-C object or a Darwin-local C structure, but it must contain:

    generation identifier
    state
    completion reason
    decoded NSString or UTF-8 result
    start timestamp
    completion signal
    cleanup-completed flag

Use explicit states:

    idle
    requestingPermission
    presenting
    configuring
    running
    finishing
    finished

Only one scan may be active. A concurrent second call must return a controlled busy error or cancel the previous session according to existing scanner semantics; do not allow two `AVCaptureSession` instances to share controller fields.
Introduce one method conceptually equivalent to:

    - (void)finishBarcodeSessionForGeneration:(uint64_t)generation
                                       reason:(TCBarcodeFinishReason)reason
                                        value:(NSString *)value;

The finalizer must ignore stale generations and repeated calls. On the first valid call it must mark the session as finishing, stop accepting metadata, store the result or error, stop capture, remove preview and highlight layers, dismiss or remove the scanner UI, clear delegates and observers, signal the waiting VM thread, and transition to finished or idle.
Do not perform slow `startRunning` or `stopRunning` work while holding the session-state lock. Do not signal completion until the result is stable and memory remains valid for the bridge.
Add focused tests for valid state transitions, repeated finalization, stale generation rejection, concurrent-start rejection, and failure before the capture session exists. If no Objective-C test target exists, extract the transition rules into the smallest pure C helper that can be compiled by the existing native test infrastructure. Do not create a broad new framework just for this issue.
The normal validation level is level 2, focused unit tests, plus the original manual reproduction to confirm instrumentation still identifies the old UI failure.
Suggested logical commit:

    refactor(scanner,ios): centralize barcode session lifecycle

### Milestone 3 — Present scanner UI from the active scene

The goal is to make the camera preview and cancellation control visible without depending on an uninitialized global `UIWindow`. At the end, the scanner UI must appear and cancel correctly, even if barcode recognition is not yet fully rewired.
The preferred implementation is a dedicated scanner view controller presented from the active `MainViewController`, or a full-screen child overlay owned by that controller. Use the current scene and controller hierarchy instead of a second global window. This avoids the `UIWindowScene` and key-window problems introduced by modern iOS scene management.
The scanner view must own:
- a container layer for `AVCaptureVideoPreviewLayer`;
- a visible cancellation button with an accessibility label;
- an optional scan-region highlight;
- layout code that updates the preview frame after rotation and safe-area changes;
- a reference to the active session generation;
- no ownership of the bridge result buffer.
Present and dismiss the UI on the main thread. Before presenting, identify the topmost controller already presented by `MainViewController`; do not present from a controller that is not in the window hierarchy. If a scanner overlay is used instead of modal presentation, add and remove it through the controller view hierarchy and restore interaction reliably.
Do not retain `barwindow` as the primary solution. If repository constraints prove that a dedicated `UIWindow` is necessary, record the evidence and create it explicitly from the active `UIWindowScene`, assign a root controller, set an appropriate level, and destroy it during finalization. That is a fallback, not the default design.
Wire the cancel control directly to the centralized finalizer. The UI must be dismissible before capture setup completes, after it starts, and after a permission callback returns.
Acceptance is visible preview scaffolding and a working cancel action on a physical iOS device, with no deadlock and no residual overlay after dismissal. Camera decoding may remain incomplete at this milestone.
The normal validation level is level 3 because controller presentation and native session state interact. Run the focused lifecycle tests and a manual device presentation/cancel test.
Suggested logical commit:

    fix(scanner,ios): present scanner in active scene

### Milestone 4 — Make permission and capture setup asynchronous

The goal is to ensure that no permission or AVFoundation operation blocks the main thread and that every setup failure completes the Java call. At the end, authorized, undecided, denied, restricted, and hardware-error paths all have a visible or controlled result.
Before creating the camera session, inspect `authorizationStatusForMediaType:AVMediaTypeVideo`.
For `AVAuthorizationStatusAuthorized`, continue setup. For `AVAuthorizationStatusNotDetermined`, call `requestAccessForMediaType:completionHandler:` and continue from its callback. Dispatch only the UIKit continuation to the main queue. For denied or restricted status, call the centralized finalizer with the error behavior defined by the existing Java contract. Confirm that the generated iOS `Info.plist` includes `NSCameraUsageDescription`.
Create `AVCaptureDeviceInput` and inspect its `NSError`. Call `canAddInput:` and `canAddOutput:` before adding them. Intersect requested metadata types with `availableMetadataObjectTypes`; unsupported types must not make the session hang. An empty intersection is an initialization error.
Map `mode` deterministically:
- empty mode selects the supported intersection of the framework's normal 1D and 2D set;
- `1D` selects supported linear formats;
- `2D` selects supported QR, Aztec, PDF417, Data Matrix, and equivalent formats already promised by the API;
- unknown values follow the documented existing error behavior rather than silently changing modes.
`AVCaptureSession.startRunning` and `stopRunning` can block. Run them on a dedicated serial capture queue, not the main queue. UI updates and controller presentation remain on the main queue. Associate every queued block with the session generation and ignore blocks from an obsolete session.
Every error branch must call the same finalizer. Do not merely log and return while the VM thread is waiting.
Acceptance is successful progression to `running` for authorized hardware, controlled completion for permission denial and setup errors, and no main-thread stall during permission or `startRunning`.
The normal validation level is level 3, focused integration tests with mocked authorization/session adapters where practical, plus manual first-run and denied-permission cases.
Suggested logical commit:

    fix(scanner,ios): handle camera setup asynchronously

### Milestone 5 — Replace polling and unsafe main-queue dispatch

The goal is to remove the two synchronization hazards directly associated with the freeze. At the end, the VM caller waits efficiently, the main thread never waits on itself, and all completion paths release the caller.
Remove the blind `dispatch_sync(dispatch_get_main_queue(), ...)`. Use a small helper whose behavior is:

    if already on the main thread:
        execute the UI block directly
    otherwise:
        dispatch asynchronously unless the caller truly requires completion

Do not synchronously dispatch a block that starts asynchronous permission or capture work and then assume the scanner has completed.
Replace `while (callingBarcode) Sleep(100)` with a session completion primitive. The preferred design is a condition variable or semaphore owned by the long-lived active session record. The VM thread may wait on it; the main thread must never wait on it. The finalizer signals exactly once, and repeated signals are harmless.
Use separate bounds for setup and active scanning:
- permission and capture initialization must complete or fail within a short, testable setup timeout;
- active scanning normally ends by success, user cancellation, or lifecycle cancellation;
- add an emergency active-session timeout only if the existing API contract does not require an indefinitely open scanner. Record the selected duration and compatibility rationale in `Decision Log`.
A timeout must invoke the centralized finalizer and release the camera. It must not return an empty value that is indistinguishable from user cancellation. Use the established `***` error convention only after confirming it in `Scanner.java` and existing native implementations.
Prove with thread dumps that the main thread is processing events while the VM caller waits. Prove that denied permission, failed input creation, cancel, and success all release the same wait.
The normal validation level is level 3. Escalate to level 4, iOS module build, when synchronization types or bridge signatures change across translation units.
Suggested logical commit:

    fix(scanner,ios): unblock barcode completion wait

### Milestone 6 — Preserve results and survive lifecycle changes

The goal is to make the corrected scanner reusable and eliminate residual resource and encoding defects. At the end, repeated scans, Unicode payloads, rotation, backgrounding, and interruption all complete safely.
Keep the decoded value as `NSString` within the Objective-C session. Convert to UTF-8 only at the C bridge boundary. Allocate by `lengthOfBytesUsingEncoding:NSUTF8StringEncoding` plus the null terminator. Do not size a UTF-8 buffer from `[NSString length]`, which counts UTF-16 code units. Ensure the buffer remains valid until the TotalCross string object has copied it, then release it exactly once.
Define distinct completion reasons for at least:

    success
    user cancellation
    permission denied
    unsupported or unavailable camera
    configuration failure
    setup timeout
    active-session timeout, if enabled
    application background or interruption
    scanner already active

Preserve the existing externally visible return/error contract. If the contract is unclear, record actual behavior of Android and the prior iOS implementation before selecting strings.
Handle lifecycle notifications for application backgrounding and `AVCaptureSession` interruption. Finalize or pause only according to a recorded decision. The safer 7.3.0 default is to cancel the scan and release the camera when the application leaves the active state; automatic resume can be a future enhancement because it introduces more stale-callback and controller-state risk.
Update preview layout after rotation. Remove notification observers, metadata delegates, preview layers, highlight views, controllers, and capture references during finalization. A callback from generation N must not affect generation N+1.
Test this sequence in one process:
1. successful scan;
2. second successful scan;
3. cancellation;
4. scan after cancellation;
5. permission or setup failure;
6. scan after failure;
7. Unicode QR payload;
8. rotation while preview is visible;
9. background and foreground;
10. metadata callback arriving after cancellation.
Acceptance is no retained camera indicator after completion, no stale UI, no crash or hang, correct UTF-8, and correct results across repeated invocations.
The normal validation level is level 3, then level 4 at milestone closure.
Suggested logical commit:

    fix(scanner,ios): clean up repeated barcode scans

### Milestone 7 — Close with device and artifact evidence

The goal is to demonstrate the issue is fixed on real hardware and that the patch has not changed unrelated platforms. At the end, the branch contains a small, reviewable implementation, focused tests, build evidence, device evidence, and an accurate retrospective.
Build the iOS path using the repository-supported flow. From the repository root:

    cd TotalCrossVM/xcode
    cmake ../ -GXcode
    pod install
    ruby ../../scripts/fix-ios-xcode-dependencies.rb \
      TCVM.xcodeproj/project.pbxproj
    xcodebuild -workspace TotalCross.xcworkspace \
      -scheme TotalCross \
      -configuration Debug \
      -sdk iphonesimulator \
      build

Use the closest available simulator destination if the generic simulator SDK does not work. Save complete output to a log and show only the relevant tail. For physical-device validation, use Xcode with a development signing setup owned by the executor; do not commit credentials, profiles, team identifiers, or derived-data paths.
Run the required manual matrix:
- first launch with undecided camera permission;
- permission granted;
- permission denied from the prompt;
- permission already denied in Settings;
- cancel before capture starts;
- cancel while running;
- one supported 1D code;
- one QR or other supported 2D code;
- Unicode payload;
- two successful scans in succession;
- scan after cancellation;
- scan after setup failure when it can be simulated;
- rotation while scanning;
- background and foreground;
- interruption or camera unavailability when reproducible;
- no code visible for the selected timeout behavior;
- device return to the application with camera indicator inactive.
The reported iPad class is the primary acceptance device. Record every actual device model and OS. Do not report untested iOS versions or devices as supported evidence.
Run Android scanner smoke validation only if the patch changes shared Java code or shared native declarations. Otherwise record Android as deliberately out of scope because the implementation is Darwin-specific.
Update `Scanner.java` documentation only where the actual behavior changed or was previously ambiguous. Consolidate the active plan, state, evidence, history, and editorial report. Do not create a release, tag, or issue comment without explicit user instruction.
The normal closing validation reaches level 5: an iOS device smoke test. A full distribution build is level 6 and may be deferred to the 7.3.0 milestone close unless the changed files affect shared packaging. A clean full build is level 7 and requires evidence of stale artifacts or an explicit request.

## Surprises & Discoveries

- Observation: the camera can start even though the scanner interface is not visible. Evidence: Objective-C messages to a nil `barwindow` do not raise an exception, while `AVCaptureSession` can still activate the camera and trigger the green privacy indicator.
- Observation: the analyzed `loadView` path initializes the main child view, keyboard support, and current barcode state, but does not visibly create `barwindow` or `barCodeButton`. Evidence: `mountBarCodeWindow` later assumes those objects already exist.
- Observation: cancellation depends on UI that may never be attached. Evidence: `closeBarcode:` clears the running flag and stops capture, but the uninitialized or invisible button removes the user's route to invoke it.
- Observation: barcode completion currently requires the same value to remain stable for more than approximately 1.5 seconds. Evidence: the metadata callback tracks repeated content before calling the close path. Preserve or intentionally revise this behavior; do not change it accidentally while fixing presentation.
- Observation: `dispatch_sync` to the main queue is conditionally fatal. Evidence: it deadlocks immediately if the native call already runs on the main thread, and adds unnecessary blocking even when called from a VM thread.
- Observation: the polling loop has no independent failure signal. Evidence: permission denial, setup failure, invisible UI, or missing callback can leave `callingBarcode` true indefinitely.
- Observation: the result path uses an ASCII-oriented global buffer and `strncpy`. Evidence: UTF-8 byte length can differ from `NSString.length`, creating truncation or missing termination for non-ASCII payloads.
- Observation: the public API is synchronous while the platform APIs are asynchronous. Evidence: the correction needs a bridge that waits only on the VM thread and lets the main thread continue permission, presentation, and capture work.
- Observation: this workspace currently exposes only iOS simulators to Xcode tooling, and it has no executable TotalCross app under `TotalCrossVM/xcode/Debug-iphonesimulator` for the requested manual reproduction. Evidence: `xcrun xctrace list devices` listed the host and simulators only; the scoped executable search returned no application binary. A simulator cannot establish physical-camera capture behavior.
- Observation: the standalone Objective-C syntax check cannot reach `mainview.m` in this workspace because the checked-in source references the absent CocoaPods header `YTPlayerView.h`. Evidence: `xcrun --sdk iphonesimulator clang -fsyntax-only ... mainview.m` exited 1 with `fatal error: 'YTPlayerView.h' file not found`. The new C state helper remains independently compilable and tested.
- Observation: the scanner presentation path no longer sends UI messages to `barwindow`. Evidence: the scoped source search finds `barwindow` only in the retained milestone-1 diagnostics; `mountBarcodeOverlay:generation:` attaches the preview, controls, and highlight to `_barcodeOverlay` inside `MainViewController.view`.
- Observation: the scanner's AVFoundation setup can be isolated without changing the VM wait yet. Evidence: `TCBarcodeSession.captureQueue` performs input/output validation, metadata-type intersection, and `startRunning`; only overlay attachment and completion remain on the main queue.
- Observation: `Scanner.readBarcode` documents `***` as its error prefix. Evidence: `TotalCrossSDK/src/main/java/totalcross/io/device/scanner/Scanner.java` documents that convention directly above the native method, enabling controlled iOS errors without inventing a new public API.
Move resolved discoveries that no longer affect future work to the archive at milestone boundaries.

## Decision Log

- Decision: perform all implementation on `fix/397-app-freezes-on-readbarcode`. Rationale: the branch is dedicated to issue #397 and began identical to the analyzed `master` baseline. Date/Author: 2026-07-22 / plan revision.
- Decision: preserve `Scanner.readBarcode(String mode)` as a synchronous public API for 7.3.0. Rationale: changing it to asynchronous behavior would be a public API and migration change larger than this bug fix. Date/Author: 2026-07-22 / plan revision.
- Decision: present scanner UI from the active TotalCross view controller rather than relying on a global secondary `UIWindow`. Rationale: controller presentation follows the active `UIWindowScene`, works with modern iOS scene management, and removes the likely nil-window failure. Date/Author: 2026-07-22 / plan revision.
- Decision: use one explicit session generation and one idempotent finalizer. Rationale: success, cancel, errors, timeout, and lifecycle callbacks currently have fragmented completion behavior; generation checks also neutralize stale callbacks from previous scans. Date/Author: 2026-07-22 / plan revision.
- Decision: keep `startRunning` and `stopRunning` off the main queue. Rationale: AVFoundation documents these as potentially blocking operations, while UIKit presentation must remain responsive. Date/Author: 2026-07-22 / plan revision.
- Decision: cancel and clean up when the application leaves the active state. Rationale: transparent pause/resume adds more controller and stale-callback risk than is appropriate for the 7.3.0 bug fix. Date/Author: 2026-07-22 / plan revision.
- Decision: use focused validation first and stop at the first sufficient level. Rationale: `AGENTS.md` requires proportional validation and discourages repeated full iOS or distribution builds after each slice. Date/Author: 2026-07-22 / plan revision.
- Decision: retain the diagnostic instrumentation until the physical-device reproduction is captured. Rationale: it is the declared output of milestone 1, assigns a monotonically increasing invocation identifier, and distinguishes the main-thread, object-presence, capture-running, preview-attachment, metadata, cancellation, and bridge-return states without logging barcode payloads. Date/Author: 2026-07-22 / milestone 1.
- Decision: close milestone 1 without a device reproduction. Rationale: the user explicitly waived tests and reproduction on a physical device; static analysis and diagnostic instrumentation are complete, but no physical-device behavior is claimed. Date/Author: 2026-07-22 / user direction.
- Decision: make the session transition rules a pure C header used by the Darwin controller and a standalone C test. Rationale: no focused Objective-C test target is available; this provides executable coverage for lifecycle admissibility without adding a test framework or changing the Xcode project. Date/Author: 2026-07-22 / milestone 2.
- Decision: retain the synchronous dispatch and polling wait until milestone 5. Rationale: milestone 2 centralizes state and finalization first; changing the waiting primitive or main-queue behavior now would combine two planned milestones and make lifecycle regressions harder to isolate. Date/Author: 2026-07-22 / milestone 2.
- Decision: use a full-screen child overlay instead of modal presentation. Rationale: `MainViewController.view` is the active TotalCross scene owner, so adding/removing a child view avoids a second `UIWindow` and does not need to select or dismiss a potentially unrelated presented controller. Date/Author: 2026-07-22 / milestone 3.
- Decision: preserve the current synchronous capture setup in milestone 3. Rationale: the overlay is attached before `startRunning`, but permission and capture setup must move off the main queue only in milestones 4 and 5. The user waived physical-device UI validation, so this milestone does not claim cancellation responsiveness while those legacy waits remain. Date/Author: 2026-07-22 / milestone 3.
- Decision: treat an unknown mode or an empty supported metadata intersection as configuration failure. Rationale: the previous iOS path ignored `mode`; silently falling back could claim formats that the selected camera cannot decode. The current public bridge continues returning its established empty result for controlled failure. Date/Author: 2026-07-22 / milestone 4.
- Decision: use a 15-second setup timeout and no active-scan timeout. Rationale: authorization and capture setup should not wait indefinitely, while the existing synchronous scanner is deliberately user-controlled once visible. Setup errors use the documented `***` prefix; active scanning ends by success or cancellation until lifecycle behavior is added in milestone 6. Date/Author: 2026-07-22 / milestone 5.
- Decision: return a controlled `***` error when the native call is entered from the iOS main thread. Rationale: waiting synchronously there would prevent the UI work required to complete the scan; returning instead preserves main-thread responsiveness without changing the Java method signature. Date/Author: 2026-07-22 / milestone 5.
- Decision pending: preserve the current approximately 1.5-second stable-read requirement or return the first accepted metadata value. Rationale: this changes user-visible scan latency and must be based on current API intent and device evidence. Date/Author: resolve before milestone 6.
- Decision pending: exact setup and active-session timeout values and their external error representation. Rationale: a bound prevents permanent waits, but an overly short active timeout may break legitimate workflows. Confirm the existing Java error contract before implementation. Date/Author: resolve in milestone 5.

## Validation and Acceptance

Follow the validation escalation in `AGENTS.md` and stop at the first level that proves the current slice:
1. static inspection or `git diff --check`;
2. focused unit test;
3. focused integration test;
4. iOS module build;
5. physical-device smoke test;
6. full distribution build;
7. clean full distribution build.
Do not run `clean` by default. Save verbose CMake, CocoaPods, and Xcode output to files. Report only the failed command, exit code, most relevant lines, short context, and log path.
Before committing any slice, run from the repository root:

    git diff --stat
    git diff -- \
      TotalCrossSDK/src/main/java/totalcross/io/device/scanner/Scanner.java \
      TotalCrossVM/src/nm/io/device/scanner/zxing.c \
      TotalCrossVM/src/nm/ui/darwin/mainview.m \
      TotalCrossVM/src/nm/ui/darwin/mainview.h \
      TotalCrossVM/src/event/darwin \
      TotalCrossVM/xcode \
      .agent
    git diff --check
    git status --short -- \
      TotalCrossSDK/src/main/java/totalcross/io/device/scanner/Scanner.java \
      TotalCrossVM/src/nm/io/device/scanner/zxing.c \
      TotalCrossVM/src/nm/ui/darwin/mainview.m \
      TotalCrossVM/src/nm/ui/darwin/mainview.h \
      TotalCrossVM/src/event/darwin \
      TotalCrossVM/xcode \
      .agent

Issue #397 may be closed only when all of these are observed:
1. The scanner UI appears on the reported iPad class or the closest available documented device.
2. The iOS main thread remains responsive during permission, presentation, scanning, cancellation, setup failure, and completion.
3. First-run permission grant continues into scanning without a second call.
4. Permission denial and previously denied permission return without a hang.
5. Missing camera, input failure, output failure, and unsupported metadata configuration return without a hang.
6. The user can cancel before and after capture starts.
7. No completion path uses an infinite polling loop.
8. The VM caller is released for success, cancel, errors, lifecycle cancellation, and the selected timeout behavior.
9. The decoded result is correct for ASCII and UTF-8 payloads.
10. Two or more scans work in the same process after success, cancellation, and
    failure.
11. Rotation and backgrounding leave no preview, controller, observer, or
    active camera session behind.
12. Stale metadata callbacks from an earlier generation do not affect a newer
    scan.
13. The camera privacy indicator turns off after every completion path.
14. Focused tests pass and the supported iOS build succeeds.
15. A physical-device smoke test demonstrates the original scenario no longer
    freezes.

## Risks and Open Questions

The most immediate risk is changing presentation without first proving which thread invokes `readBarcode`. Milestone 1 must capture the thread facts before synchronization is rewritten.
The largest compatibility risk is altering synchronous return semantics, stable-read timing, error strings, or cancellation behavior. Preserve the Java signature and document any externally visible change.
The largest native-safety risk is a callback touching a session that has already been finalized or reused. Generation checks, centralized ownership, and idempotent cleanup are mandatory.
The existing Darwin source may use manual reference counting rather than ARC. Confirm the compilation mode before introducing Objective-C session objects, blocks, or dispatch-object ownership. Match the repository's current memory management; do not mix assumptions.
A modal scanner controller can conflict with another controller being presented. Resolve the active presentation controller and return a controlled busy/presentation error rather than forcing a second presentation.
A dedicated capture queue and the VM wait primitive can create lock inversion if cleanup holds a state lock while synchronously dispatching to another queue. Never hold the session lock across UI dispatch, `startRunning`, `stopRunning`, or controller dismissal.
The simulator cannot prove camera capture or the privacy-indicator lifecycle. Physical-device evidence remains mandatory.
The exact error format and timeout policy remain open until `Scanner.java`, Android behavior, and prior iOS behavior are inspected. Record the decision before exposing new strings to applications.

## Idempotence and Recovery

Session start, finish, and cleanup must be safe to retry. A second finish call for the same generation is a no-op. A callback for an old generation is a no-op. A failure before input, output, preview, or controller creation still runs the same finalizer without assuming those objects exist.
Tests must use temporary data and test-only codes. Do not commit signing credentials, provisioning profiles, derived data, CocoaPods caches, generated Xcode projects, device logs containing private application data, or user barcodes.
If a UI strategy fails, preserve the reproduction, state machine, tests, and completion-path work. Revert only the presentation slice. If synchronization changes regress the bridge, return to the last logical commit without deleting evidence.
Local commits are allowed after each validated logical slice. Push, pull-request creation, issue comments, tags, releases, and artifact publication require explicit user instruction. This plan performs none of them automatically.
Before an authorized push, fetch the remote, confirm that the remote branch did not advance unexpectedly, and push without `--force`. If a pull request already exists, update the same branch. Preserve unrelated local changes.
If an Xcode build or device run fails halfway, record the command, exit status, relevant lines, and log path. Remove only artifacts created by the failed attempt when necessary. Do not delete global caches or dependency directories merely to make the tree clean.

## Outcomes & Retrospective

Current state: milestones 1 through 5 are complete. `TCBarcodeSession` owns a serial capture queue and completion semaphore; the VM caller waits on that semaphore while all UI initiation is queued asynchronously on the main thread. Main-thread callers return a documented-style `***` error rather than deadlocking, and setup has a 15-second controlled timeout. The user explicitly waived physical-device reproduction and thread capture, so this plan does not claim device validation. The standalone Objective-C syntax check is blocked before the modified source by the absent `YTPlayerView.h` CocoaPods dependency; no Xcode build was run.
At each milestone boundary, replace this paragraph or append a short factual entry describing the behavior delivered, focused validation executed, evidence path, unresolved limitation, and next boundary. Move large completed detail to the history file when it makes the active plan harder to resume.
At completion, the editorial report must contain:
- `Editorial Summary`;
- `Original Plan versus Actual Outcome`;
- `What Changed`;
- `Decisions and Trade-offs`;
- `Unexpected Problems and Discoveries`;
- `Validation and Measurable Results`;
- `Useful Evidence and Examples`;
- `Limitations, Remaining Work, and Open Questions`;
- `Possible Article Angles`;
- `Suggested Narrative`;
- `Claims Requiring Human Review`.
The final retrospective must distinguish implemented behavior from planned behavior, physical-device results from simulator results, and measured timing from estimates.

## Revision Note

2026-07-22: the previous Portuguese, linear plan was replaced with an English resumable ExecPlan following the supplied planning standard. The revision added a working-set and resume protocol, supporting state/evidence/archive/report paths, risk-proportional validation, seven independently verifiable milestones, explicit recovery behavior, branch policy, and factual baseline status. It preserved the earlier technical findings about `barwindow`, `barCodeButton`, `dispatch_sync`, `callingBarcode`, AVFoundation setup, result encoding, and lifecycle cleanup. All work now explicitly targets `fix/397-app-freezes-on-readbarcode`.

2026-07-22: milestone 1 added diagnostic-only `TCBarcode` logs to the bridge and existing scanner path. The plan now records the physical-device reproduction blocker and its local evidence. No milestone-2-or-later behavior was implemented or validated.

2026-07-22: at user direction, milestone 1 was closed without a physical-device reproduction or test. The plan records the waiver rather than presenting unavailable device evidence as completed validation. Milestone 2 was not started.

2026-07-22: milestone 2 added explicit per-invocation state, a generation gate, and one finalizer while intentionally retaining the existing UI presentation and waiting mechanisms for their later milestones. A standalone C test covers the transition rules. The Objective-C syntax check is limited by an absent pre-existing CocoaPods header, and physical-device validation remains waived.

2026-07-22: milestone 3 replaced the nil `barwindow` path with a full-screen overlay attached to `MainViewController.view`. The overlay owns the preview, highlight, Cancel button, and layout, and finalization removes it only for its matching session generation. Permission/capture scheduling and the wait mechanism remain deferred by milestone boundary.

2026-07-22: milestone 4 made authorization and capture configuration asynchronous, using a session-owned serial queue for AVFoundation work. It filters requested scanner modes against available metadata types and completes denied, unsupported, and configuration-error paths through the existing finalizer. The main-queue dispatch and polling wait remain for milestone 5.

2026-07-22: milestone 5 replaced the scanner's synchronous main-queue work and 100 ms polling loop with asynchronous UI initiation plus one semaphore wait on the VM caller. The setup timeout and main-thread rejection use the documented `***` error convention. No active-scan timeout was added.
