<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Add Structured Text Editing and Android/iOS IME Support

This ExecPlan is a living document. The sections `Progress`, `Surprises & Discoveries`, `Decision Log`, `Outcomes & Retrospective`, and `Revision Note` must be kept up to date as work proceeds.

This document follows `AGENTS.md` and `.agent/PLANS.md`. Keep it self-contained when revising it: a future contributor must be able to continue from this file and the current working tree without relying on conversation history.

## Purpose / Big Picture

TotalCross text controls currently receive software-keyboard input primarily as individual key events. That model cannot faithfully represent composition, prediction, autocorrection, replacement of a previous word, selection-only changes, emoji sequences, or other edits that contain more than one UTF-16 code unit.

After this plan is complete, `Edit` and `MultiEdit` will accept atomic text-edit operations containing replacement text, replacement range, selection, and composing range. Android will provide those operations through a complete `InputConnection`. iOS will retain the existing `UITextView` as the system editor proxy and synchronize its complete text, selection, and marked-text state with the focused TotalCross control.

Existing applications must remain source and binary compatible. `TextControl`, `MainClass`, `KeyEvent`, existing constructors and methods on `Edit` and `MultiEdit`, physical-key handling, and the TotalCross virtual keyboard remain available. A developer can verify the result with a mobile smoke application that demonstrates prediction, accepted autocorrection, composition, replacement of selected text, emoji, paste, multiline entry, editor actions, focus changes, and keyboard dismissal on Android and iOS.

## Working Set and Resume Protocol

The active plan is this file:

    .agent/structured-text-editing-ime-execplan.md

Create and maintain these supporting files only after implementation begins:

    .agent/state/structured-text-editing-ime.md
    .agent/evidence/structured-text-editing-ime.md
    .agent/archive/structured-text-editing-ime-history.md
    .agent/reports/structured-text-editing-ime-editorial.md

On normal resume, read the state file first. It must name the active milestone, current slice, last logical commit, focused validation completed, deferred validation, active decisions, blockers, files intentionally out of scope, and the next concrete command. Read this ExecPlan only for the active milestone and unresolved decisions. Search the evidence file only when validating a previous claim. Do not routinely reread the archive or editorial report.

Before editing, read the repository-root `AGENTS.md`, `.agent/PLANS.md`, and the relevant files named in `Current Architecture and Scope`. Preserve unrelated local changes and generated dependency directories.

## Progress

- [x] (2026-07-26T06:29:09Z) Created issue #431 describing structured text editing and Android/iOS IME support.
- [x] (2026-07-26) Inspected the current Java text controls, event bridge, Android `InputConnection`, and iOS `UITextView` proxy to establish the baseline described below.
- [ ] Milestone 1: add the Java editing model and tests without changing runtime behavior.
- [ ] Milestone 2: introduce a shared text-editing core before migrating either control.
- [ ] Milestone 3: migrate `Edit` to the shared core while preserving its public behavior.
- [ ] Milestone 4: migrate `MultiEdit` to the shared core while preserving multiline layout and scrolling.
- [ ] Milestone 5: add the string-capable VM/native editing bridge with session and revision checks.
- [ ] Milestone 6: implement and validate the Android IME path.
- [ ] Milestone 7: implement and validate the iOS IME path while retaining `UITextView`.
- [ ] Milestone 8: activate the structured path, complete compatibility validation, document behavior, and remove temporary duplication.

## Current Architecture and Scope

`TotalCrossSDK/src/main/java/totalcross/ui/TextControl.java` contains only `setText(String)` and `getText()`. It is implemented by editable and non-editable controls, so it must not gain new abstract editing methods.

`TotalCrossSDK/src/main/java/totalcross/ui/Edit.java` stores text in a `StringBuffer` and implements insertion, deletion, selection, masking, valid-character filtering, capitalization, clipboard behavior, cursor movement, repaint, and value-change notification inside its `onEvent` handling for `KeyEvent`.

`TotalCrossSDK/src/main/java/totalcross/ui/MultiEdit.java` implements a second, independent editing engine. It has its own text buffer, selection, cursor, valid-character filtering, maximum length, clipboard behavior, line calculation, scrolling, and key handling. This duplication is the main architectural obstacle to adding structured editing safely.

`TotalCrossSDK/src/main/java/totalcross/ui/event/KeyEvent.java` represents a single key through an integer. It remains the correct public API for physical keys, navigation, shortcuts, action keys, and the existing TotalCross virtual keyboard, but it is not a sufficient protocol for software-keyboard text edits.

`TotalCrossSDK/src/main/java/totalcross/MainClass.java` exposes `_postEvent(int, int, int, int, int, int)`. Do not add an abstract method to this interface because applications may implement it for headless execution.

`TotalCrossSDK/src/main/java/totalcross/ui/Window.java` receives the six-integer native event contract and routes keys to the focused control. Add structured text dispatch through a separate internal entry point rather than overloading the integer event fields.

`TotalCrossVM/src/event/Event.c` resolves and calls `MainClass._postEvent`. It must keep that contract for existing events. The new structured path may resolve an additional internal method on `Window` or a dedicated Java dispatcher.

`TotalCrossVM/android/app/src/main/java/totalcross/Launcher4A.java` owns the Android `SurfaceView`, event queue, keyboard configuration, and a minimal `BaseInputConnection`. The current connection synthesizes delete key events, returns placeholder surrounding text, and does not support composing text, selection, or atomic replacement.

`TotalCrossVM/src/event/android/event_c.h` converts Android key events into the existing TotalCross key-event path. Structured edits require a separate JNI entry point carrying a Java string and range metadata.

`TotalCrossVM/src/nm/ui/darwin/mainview.h` and `mainview.m` own an existing hidden `UITextView`. Keep this `UITextView`; it will remain the iOS system editor proxy.

`TotalCrossVM/src/event/darwin/event.m` currently translates dictionaries from the iOS view into individual key events. Extend or replace only the text portion of that queue with structured edit payloads.

`TotalCrossVM/src/nm/ui/darwin/Window_c.m` already reads the focused control text when showing the keyboard. Extend this bridge to transfer selection, composing state, editor configuration, and revision information.

All text indices in the new cross-platform contract use UTF-16 code-unit offsets. Java `String`, Android `CharSequence`, and iOS `NSRange` naturally use this unit, avoiding lossy conversions at platform boundaries. Tests must still cover surrogate pairs and grapheme sequences because cursor movement visible to users may need additional handling later.

The implementation covers `Edit` and `MultiEdit`, Android, and iOS. It does not redesign desktop input, replace the TotalCross virtual keyboard, add rich text, add accessibility-specific editing APIs, or change the rendering architecture.

## Plan of Work

### Milestone 1: Add the Java structured-editing model

Add an additive API under `TotalCrossSDK/src/main/java/totalcross/ui/text/` or another narrowly named package chosen consistently before implementation. Define immutable or effectively immutable value types for an atomic edit, editing state, selection/composing ranges, editor configuration, and editor actions.

Create an `EditableTextControl` interface that extends `TextControl` without modifying `TextControl`. It must expose the current editing state, apply one atomic edit, report input configuration, and report the cursor rectangle in screen coordinates. Only editable controls implement it.

Add event classes under `totalcross.ui.event` for committed text edits, composition changes, selection changes, and editor actions. Keep `KeyEvent` unchanged. Define whether intermediate composition changes fire `ValueChangeEvent`; the default policy in this plan is that an accepted mutation changes the control value once per atomic operation, while composition-specific observers receive composition events.

Add focused unit tests for ranges, normalization, revision increments, invalid ranges, selection-only changes, composition-only changes, surrogate pairs, emoji sequences, multiline replacement, and immutable snapshots. At this milestone no platform or control behavior changes.

Acceptance is that the new model compiles, focused tests pass, and existing `Edit`, `MultiEdit`, `TextControl`, `MainClass`, and key APIs remain byte-for-byte unchanged unless imports or documentation are required.

Suggested commit:

    feat(sdk): add structured text editing model

### Milestone 2: Introduce the shared text-editing core

Reduce duplication before migrating either control. This activity belongs before the `Edit` migration, not after both control migrations, because implementing two new atomic-edit engines and consolidating them later would duplicate the riskiest validation, filtering, range, and event semantics.

Create a package-private shared engine, tentatively `totalcross.ui.text.TextEditingEngine`, plus small host callbacks implemented by `Edit` and `MultiEdit`. The engine owns operations common to both controls:

- range normalization and bounds validation;
- replacement of a selected or explicit range;
- maximum-length enforcement;
- valid-character filtering;
- capitalization;
- selection updates;
- composing-range updates;
- revision tracking;
- atomic change results;
- conversion of legacy printable, backspace, delete, and paste operations into structured edits;
- notification intent, without directly posting control-specific events;
- UTF-16-safe handling of replacement strings.

Do not force control-specific behavior into the engine. `Edit` retains masks, currency/date rules, overwrite behavior, and horizontal cursor visibility through host callbacks or a narrow policy object. `MultiEdit` retains line calculation, wrapping, vertical cursor movement, and scrolling outside the engine.

First add characterization tests that exercise the existing behavior of both controls. Then add direct engine tests. The milestone ends with the shared engine present but neither production control required to use it yet.

Acceptance is that the engine can apply the same ordinary insert, replace-selection, delete, backspace, paste, maximum-length, valid-character, capitalization, selection, and composition operations for both host types, while characterization tests still describe the legacy behavior.

Suggested commit:

    refactor(sdk): share text editing operations

### Milestone 3: Migrate `Edit` to the shared core

Refactor `Edit.onEvent` so legacy `KeyEvent` input is translated into engine operations. Add `EditableTextControl` implementation and a direct structured-edit path. Preserve all public constructors, fields, methods, masks, `setCursorPos`, `getCursorPos`, `setText`, clipboard commands, `PreprocessKey`, focus traversal, and repaint behavior.

Keep masked and currency/date edits as explicit `Edit` policies. An edit that the mask cannot represent must return a deterministic applied result to the platform, including the final text and selection, rather than leaving the native editor with a different state.

Do not generate a series of key events for a structured replacement. Apply the replacement atomically. Continue calling `PreprocessKey` for the legacy key path. Add a new optional structured preprocessing hook only if applications need to transform complete edits; do not reinterpret `PreprocessKey` as a complete-edit hook.

Add tests comparing the legacy key path and structured path for equivalent simple edits. Add dedicated tests for masks, overwrite mode, valid characters, maximum length, capitalization, selection replacement, clipboard, password modes, value-change count, and composing state.

Acceptance is that all prior focused `Edit` tests pass, new structured tests pass, and a structured replacement such as replacing `nao` with `não` produces one logical edit and the expected selection.

Suggested commit:

    refactor(sdk): route Edit through shared editing

### Milestone 4: Migrate `MultiEdit` to the shared core

Implement `EditableTextControl` on `MultiEdit` and route legacy key insertion, deletion, selection replacement, and paste through the same engine. Keep multiline layout, line indexes, cursor-to-coordinate conversion, wrapping, scrollbars, first visible line, vertical navigation, and flick behavior in `MultiEdit`.

After every accepted mutation, recompute only the layout state needed for correctness. Prefer an incremental path only after tests establish a correct full recalculation baseline.

Add tests for newline insertion, replacement across line boundaries, deletion joining two lines, multiline paste, selection replacement, maximum length, valid characters, capitalization, scrolling to the caret, cursor restoration, and value-change count.

At the end of this milestone, remove duplicated low-level replacement and filtering code from `Edit` and `MultiEdit`. Do not remove separate rendering and navigation behavior.

Acceptance is that both controls use one shared mutation engine, ordinary legacy behavior remains stable, and equivalent structured edits produce equivalent text/selection semantics in single-line and multiline controls.

Suggested commit:

    refactor(sdk): route MultiEdit through shared editing

### Milestone 5: Add the VM/native structured-edit bridge

Do not change `MainClass._postEvent`. Add a separate internal Java dispatcher, preferably a final method on `Window` or a dedicated internal class, that accepts a session identifier, revision, edit kind, replacement string, replacement range, resulting selection, composing range, flags, and timestamp.

Add native support for safely resolving and invoking that dispatcher. The VM must create a TotalCross `String` from platform UTF-16 data and keep it rooted for the duration of the call. Never store an unrooted `TCObject` in a platform queue.

Introduce text-input sessions. A session begins when an editable control gains focus and ends when it loses focus, is removed, its window closes, or the keyboard is dismissed. Every platform edit includes the session identifier and the revision it was based on. Ignore stale sessions. When revisions disagree, send the authoritative Java state back to the platform rather than applying an edit to the wrong text.

Extend keyboard show/update operations to carry text, selection, composing range, editor configuration, session, revision, and cursor rectangle. Keep the existing SIP methods as compatibility wrappers.

Add native and Java tests where possible, including stale-session rejection, stale-revision resynchronization, selection-only updates, empty replacement, long replacement strings, and string lifetime across the bridge.

Acceptance is that tests can inject a structured native-style payload into the focused control and receive the authoritative resulting state without using `KeyEvent`.

Suggested commit:

    feat(vm): add structured text edit bridge

### Milestone 6: Implement Android IME support

Replace the minimal `Launcher4A.MyInputConnection` behavior with a complete connection backed by a Java-side mirror of the focused control state. The mirror is necessary because Android may synchronously call surrounding-text methods on the UI thread while the TotalCross event thread owns the control.

The Android mirror stores text, selection, composing range, session, and revision. Update it whenever Java accepts an edit or changes selection programmatically. Do not synchronously call into the VM from `getTextBeforeCursor`, `getTextAfterCursor`, `getSelectedText`, or `getExtractedText`.

Implement at least:

    commitText
    setComposingText
    finishComposingText
    deleteSurroundingText
    deleteSurroundingTextInCodePoints
    setSelection
    getTextBeforeCursor
    getTextAfterCursor
    getSelectedText
    getExtractedText
    beginBatchEdit
    endBatchEdit
    performEditorAction
    requestCursorUpdates
    sendKeyEvent

Configure `EditorInfo.inputType`, `imeOptions`, capitalization, suggestions, password behavior, multiline mode, and action keys from `TextInputConfiguration`. Keep physical keyboard and scanner key events on the existing `KeyEvent` path.

Batch edits must be coalesced when doing so preserves Android semantics. The platform mirror must be updated optimistically only with a revision that can be reconciled; the authoritative Java response wins.

Create an Android instrumentation smoke application with `Edit`, `MultiEdit`, password, numeric, email, and maximum-length fields. Exercise Gboard and Samsung Keyboard on real devices. Record device, OS version, keyboard package/version, and result in the evidence file.

Acceptance requires accepted autocorrection, prediction, composition, swipe typing, emoji, selection replacement, paste, multiline newline, editor action, focus transfer, rotation, background/foreground, and keyboard dismissal without duplicate text.

Suggested commit:

    feat(android): support structured IME editing

### Milestone 7: Implement iOS IME support with `UITextView`

Retain `MainViewController.kbd` as a `UITextView`. It remains the first responder and system editor proxy; do not replace it with a custom `UITextInput` implementation.

When a TotalCross editable control gains focus, synchronize the complete text, selected range, editor configuration, session, revision, and composing state into the `UITextView`. Use a guard to distinguish programmatic synchronization from user-originated delegate callbacks.

Use `UITextViewDelegate` callbacks and marked-text state to send structured edits and selection changes. The implementation must account for `shouldChangeTextInRange`, `textViewDidChange`, `textViewDidChangeSelection`, `markedTextRange`, accepted autocorrection, dictation, and replacements that affect previously typed text. Do not read only the first UTF-16 unit of the replacement string.

Configure autocorrection, spell checking, capitalization, keyboard type, secure behavior, return key, and multiline behavior from `TextInputConfiguration`. Preserve existing keyboard show/hide notifications and screen-shift behavior unless a focused fix is required.

Keep the proxy's authoritative content synchronized after Java filtering, masks, maximum length, or stale-revision recovery. Preserve selection and marked text where the accepted Java result allows it; otherwise end composition and resynchronize explicitly.

Create an iOS smoke application equivalent to Android. Test the standard keyboard, autocorrection, prediction, marked text, dictation, emoji, selection replacement, paste, multiline input, hardware keyboard, focus changes, rotation, and background/foreground.

Acceptance is that software keyboard text no longer arrives as one `KEY_PRESS` per character, while physical keyboard navigation and shortcuts continue to use `KeyEvent`.

Suggested commit:

    feat(ios): synchronize UITextView structured edits

### Milestone 8: Activate, validate compatibility, document, and clean up

Enable structured software-keyboard editing by default on Android and iOS after their smoke matrices pass. Keep a temporary diagnostic fallback only if it is required to isolate regressions during rollout. Give it an explicit removal criterion and do not expose it as a permanent application API.

Preserve source and binary compatibility for:

    TextControl
    MainClass
    KeyEvent and KeyListener
    Edit and MultiEdit constructors and public methods
    setText and getText
    setCursorPos and getCursorPos
    ValueChangeEvent
    PreprocessKey on the legacy key path
    the TotalCross virtual keyboard
    physical keyboard input

Document the intentional behavioral difference: software-keyboard text may now arrive as one atomic replacement rather than several key presses. Applications must use the structured edit hooks or value-change events to observe text mutations. Do not synthesize legacy key events for composition, autocorrection, selection replacement, dictation, or multi-code-unit input because that would be lossy or duplicate mutations.

Run focused tests first, then SDK build, Android build, iOS archive, and real-device smoke validation. Remove placeholder surrounding text, character-only iOS conversion, duplicated mutation helpers, temporary logs, and fallback code whose removal criteria have been met.

Update public documentation and release notes. Complete the editorial report with delivered behavior, validation devices, known limitations, and any remaining compatibility concerns.

Acceptance is that the smoke application passes on the supported Android and iOS test matrix, existing SDK text-control tests pass, API compatibility review finds no removed or changed public signatures, and no software-keyboard composition is implemented by replaying individual key events.

Suggested commit:

    feat(sdk): enable structured mobile text input

## Surprises & Discoveries

- Observation: `TextControl` is implemented by non-editable controls.
  Evidence: the interface contains only `setText` and `getText`, while controls such as labels and buttons also implement it. A separate `EditableTextControl` is required.

- Observation: `Edit` and `MultiEdit` contain independent editing engines.
  Evidence: both classes own separate text buffers, selections, key handling, filtering, clipboard behavior, and cursor updates. Shared mutation logic must be introduced before migrating either control.

- Observation: the Android connection does not expose real editor state.
  Evidence: the current `getTextBeforeCursor` returns placeholder text and deletion is synthesized as key events.

- Observation: iOS already has a suitable system editor proxy.
  Evidence: `MainViewController` creates a hidden `UITextView` and makes it first responder. The proxy can be retained and upgraded to synchronize complete editing state.

- Observation: the current native event contract cannot carry replacement strings or ranges.
  Evidence: `_postEvent` and `postEvent` carry only integers. Structured text requires a parallel string-capable bridge.

## Decision Log

- Decision: Introduce `EditableTextControl` rather than expanding `TextControl`.
  Rationale: expanding `TextControl` would affect editable and non-editable implementations and could break user implementations.
  Date/Author: 2026-07-26 / OpenAI

- Decision: Reduce editing duplication before the `Edit` migration.
  Rationale: adding atomic editing independently to both controls and consolidating later would duplicate the highest-risk semantics and tests. A shared core first allows each control migration to be independently verified.
  Date/Author: 2026-07-26 / OpenAI

- Decision: Keep `KeyEvent` for physical keys and legacy key-driven input.
  Rationale: key presses and text mutations are different concepts. Keeping the existing key API preserves compatibility while allowing correct IME behavior.
  Date/Author: 2026-07-26 / OpenAI

- Decision: Use UTF-16 code-unit offsets across Java, Android, and iOS.
  Rationale: all three platform string/range APIs naturally use UTF-16, minimizing conversion errors at the bridge.
  Date/Author: 2026-07-26 / OpenAI

- Decision: Keep `MainClass` unchanged and add a separate internal structured dispatcher.
  Rationale: adding an abstract method to `MainClass` would break headless implementations.
  Date/Author: 2026-07-26 / OpenAI

- Decision: Retain `UITextView` as the iOS system editor proxy.
  Rationale: it already participates in the iOS keyboard system and provides autocorrection, marked text, prediction, dictation, and selection behavior at lower cost and risk than implementing `UITextInput` from scratch.
  Date/Author: 2026-07-26 / OpenAI

- Decision: Use session identifiers and revisions.
  Rationale: focus changes and asynchronous platform callbacks can otherwise apply edits to the wrong control or stale text.
  Date/Author: 2026-07-26 / OpenAI

## Validation and Acceptance

Use the smallest sufficient validation for each slice, following the escalation order in `AGENTS.md`.

For plan-only changes, run from the repository root:

    git diff --check -- .agent/structured-text-editing-ime-execplan.md

For Java model and shared-engine slices, add focused JUnit tests and run:

    cd TotalCrossSDK
    ./gradlew-agent test --tests '<new focused test class>'

Expect the focused test class to pass with no unrelated failures. Save verbose output to the agent logs produced by `gradlew-agent`.

For each completed Java-control milestone, run its focused tests and then:

    cd TotalCrossSDK
    ./gradlew-agent test
    ./gradlew-agent dist -x test

Do not run `clean` unless stale artifacts are suspected.

For Android bridge or IME changes, run:

    cd TotalCrossVM/android
    ./gradlew :tcvm:fetchNativeDependencies
    ./gradlew :app:testStandardDebugUnitTest
    ./gradlew :app:assembleStandardDebug

At the Android milestone boundary, install the smoke app on at least one Google/Gboard device and one Samsung Keyboard device. Record the exact devices and keyboard versions in evidence.

For native VM changes, run from the repository root when the host supports the target:

    cmake -S TotalCrossVM -B build -DCMAKE_BUILD_TYPE=Release -G Ninja
    ninja -C build

For iOS milestone validation, use the repository flow:

    cd TotalCrossVM/xcode
    cmake ../ -GXcode
    pod install
    ruby ../../scripts/fix-ios-xcode-dependencies.rb TCVM.xcodeproj/project.pbxproj
    xcodebuild -workspace TotalCross.xcworkspace -scheme TotalCross archive

At the iOS milestone boundary, run the smoke application on a real iPhone or iPad and record OS version, keyboard configuration, and results.

Final acceptance requires observable success for:

- ordinary typing;
- selection replacement;
- accepted autocorrection;
- prediction;
- active composition and composition commit;
- emoji and surrogate pairs;
- paste;
- multiline insertion;
- editor actions;
- password and numeric configuration;
- maximum length and valid-character filtering;
- focus changes;
- keyboard dismissal;
- rotation;
- background and foreground;
- physical keyboard navigation and shortcuts;
- no duplicate characters;
- no application of stale-session edits;
- no removed or changed existing public signatures.

Expensive device matrices may be deferred during intermediate slices, but not when closing the Android, iOS, or final activation milestones. Record every deferral and its reason in the state file.

## Risks and Open Questions

The main compatibility risk is applications that observe software-keyboard text through `KeyEvent.KEY_PRESS`. The implementation preserves that API but cannot represent autocorrection, composition, dictation, or replacement of previous text as a faithful key sequence. Document the behavioral change and avoid duplicate synthetic events.

Masks and valid-character filtering can cause the Java-accepted result to differ from the platform's optimistic edit. The authoritative state response must be fast and deterministic to avoid visible cursor jumps or IME loops.

Android keyboards differ in callback ordering and surrounding-text assumptions. Gboard and Samsung Keyboard are mandatory validation targets; additional vendor keyboards should be added when regressions are reported.

The iOS `UITextView` proxy may require careful placement or cursor-rectangle reporting so candidate UI appears near the TotalCross caret. Validate this before considering any custom input implementation.

`ValueChangeEvent` frequency is observable. The intended policy is one value change per accepted atomic edit, not one per UTF-16 unit. Existing tests and release notes must make this explicit.

Masked fields may not be compatible with active composition. The initial policy should allow composition only when the control can preserve a stable composing range; otherwise commit or reject deterministically and resynchronize.

## Idempotence and Recovery

All milestones are additive before cleanup. Keep legacy key input working until both mobile paths pass their smoke validation. A failed platform migration can be disabled without reverting the shared Java editing core.

Do not rewrite history or use destructive Git commands. Preserve unrelated worktree files. Stage only paths belonging to the active milestone.

Generated Gradle, CMake, Xcode, device, and dependency outputs must not be committed. Store full validation logs outside tracked source and record compact evidence references.

If a platform edit arrives for an inactive session, ignore it. If it arrives with a stale revision, request or push a full-state resynchronization. Do not guess a replacement range.

If the native bridge fails after allocating a Java string, release or unlock the object according to VM ownership rules before returning. Do not retain raw platform pointers across asynchronous calls.

Each milestone should end in one or more focused commits. Before a write, re-read the current branch head and scoped diff to avoid overwriting concurrent changes.

## Outcomes & Retrospective

No implementation milestone has been completed yet. The initial outcome is an implementation-ready decomposition that preserves the existing public API, introduces shared editing semantics before control migrations, keeps Android and iOS independently testable, and retains `UITextView` on iOS.

At every milestone boundary, update this section with factual delivered behavior, validation performed, remaining gaps, and lessons that affect subsequent work. At completion, compare actual device coverage, API compatibility, and behavioral results with the purpose stated above.

## Revision Note

2026-07-26 / OpenAI: Initial ExecPlan created from the current TotalCross `master` architecture. The plan places shared editing-core extraction before the `Edit` and `MultiEdit` migrations to avoid duplicating atomic replacement, filtering, range, composition, and notification semantics. It retains `UITextView` as the iOS editor proxy and keeps existing public APIs additive and compatible.
