<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Implement UTF-16 core semantics for text indexing

This ExecPlan follows `AGENTS.md` and `.agent/PLANS.md`. It is a living document. Keep `Progress`, `Surprises & Discoveries`, `Decision Log`, `Outcomes & Retrospective`, and the final `Revision Note` current as implementation proceeds.

This plan implements GitHub issue #430.

## Purpose / Big Picture

After this work, TotalCross strings continue to expose Java-compatible 16-bit `char` indexing, but the runtime treats those values as real UTF-16 code units rather than as an effectively UCS-2-only representation. Supplementary Unicode code points, including emoji, round-trip correctly between UTF-8 byte sequences and TotalCross strings by using surrogate pairs. Code that uses `String.length()`, `charAt()`, `substring()`, selections, and text ranges continues to count UTF-16 code units, matching Java, Android `CharSequence`, and iOS `NSString`/`NSRange` conventions.

A developer can observe the result by running focused converter tests and confirming that `"A😀B"` has length four, contains the expected high and low surrogates at indexes one and two, and converts to the UTF-8 byte sequence `41 F0 9F 98 80 42`. The same tests must pass through the JavaSE fallback implementation and the deployed native implementation.

This work establishes only the UTF-16 core needed by future text-input and IME APIs. Grapheme-cluster navigation, Unicode normalization, bidi, locale-sensitive case conversion, collation, shaping, and ICU integration are explicitly outside this plan.

## Working Set and Resume Protocol

When resuming, first read this file and inspect the latest entries in `Progress`, `Decision Log`, and `Surprises & Discoveries`. If implementation has begun, create and maintain `.agent/state/utf16-core-text-indexing.md` with the active milestone, last logical commit, files currently being edited, focused validation already run, and the next exact action. Read that state file before rereading the full plan.

Use `.agent/evidence/utf16-core-text-indexing.md` as an append-only record for concise validation evidence. Record the commit, command, result, relevant counts, and the path of any full log. Do not paste complete build logs into this plan.

The primary source paths are:

- `TotalCrossSDK/src/main/java/totalcross/lang/String4D.java`, which stores `String` as `char[]` and defines code-unit-based methods such as `length()`, `charAt()`, and `substring()`.
- `TotalCrossSDK/src/main/java/totalcross/sys/UTF8CharacterConverter.java`, which contains the JavaSE fallback UTF-8 decoder and encoder and currently handles only one-, two-, and three-byte UTF-8 sequences.
- `TotalCrossVM/src/nm/sys/CharacterConverter.c`, which contains the deployed native UTF-8 decoder and encoder and currently treats each `JChar` independently.
- `TotalCrossVM/src/util/xtypes.h`, which defines `JChar` as `uint16`.
- `TotalCrossSDK/src/main/java/totalcross/sys/Convert.java`, which registers and selects character converters.
- `TotalCrossSDK/src/test/java/`, where focused JUnit 5 tests for the Java implementation belong.
- The native VM test infrastructure found during Milestone 1. If no suitable native unit-test target exists, add a narrowly scoped test executable under an existing test directory rather than creating a parallel test framework.

Read `AGENTS.md` before editing or validating. Use the smallest validation that proves the current milestone and preserve unrelated local changes.

## Progress

- [x] (2026-07-26T06:22:02Z) Created GitHub issue #430 describing the purpose, scope, and acceptance requirements.
- [x] (2026-07-26T06:25:00Z) Created this initial ExecPlan on branch `agent/utf16-core-text-indexing`.
- [ ] Milestone 1: inventory all UTF-8 and UTF-16 conversion entry points, confirm current tests and native test infrastructure, and record the exact compatibility contract.
- [ ] Milestone 2: implement shared UTF-16 code-unit and code-point helpers in the native VM without changing `String` object layout.
- [ ] Milestone 3: correct the JavaSE `UTF8CharacterConverter` decoder and encoder, including malformed-input behavior.
- [ ] Milestone 4: correct the native UTF-8 decoder and encoder and keep behavior equivalent to the JavaSE path.
- [ ] Milestone 5: add Java-facing code-point helpers needed for Java compatibility and future text indexing, only where the current class library lacks them.
- [ ] Milestone 6: add focused Java and native tests, differential validation, sanitizers where available, and compatibility regression coverage.
- [ ] Milestone 7: run milestone-closing builds, update documentation and living-plan records, and prepare the implementation for review.

## Current Architecture and Scope

TotalCross `String` is already represented by an array of 16-bit values. `String4D` declares `char chars[]`, while the native runtime accesses the same storage through `JChar`, defined as `uint16`. Therefore this plan does not migrate object layout, alter garbage-collector behavior, or replace `String` storage.

The compatibility problem exists at interpretation and conversion boundaries. The Java and native UTF-8 decoders stop after three-byte sequences, so they cannot decode code points from U+10000 through U+10FFFF into UTF-16 surrogate pairs. The encoders process each 16-bit value independently, so a valid surrogate pair is emitted as two invalid three-byte sequences rather than one four-byte UTF-8 sequence. Malformed UTF-8 validation is also incomplete and must be made explicit and equivalent across implementations.

A UTF-16 code unit is one 16-bit `char`. A Unicode code point is a Unicode scalar value. Code points from U+0000 through U+FFFF normally use one UTF-16 code unit, except that U+D800 through U+DFFF are reserved as surrogates. Code points from U+10000 through U+10FFFF use a high-surrogate and low-surrogate pair. A grapheme cluster is a user-perceived character and may contain several code points; grapheme segmentation is not implemented here.

The public compatibility contract remains Java-like:

- `String.length()` returns the number of UTF-16 code units.
- `String.charAt(index)` returns one code unit and may therefore return one half of a surrogate pair.
- `substring`, array indexes, and future text-edit ranges continue to use UTF-16 code-unit indexes.
- Code-point-aware helpers treat a valid surrogate pair as one code point.
- Existing BMP strings retain their current indexes and serialized representation.

The implementation must not add ICU or another large Unicode dependency. Small UTF-16 helpers belong in the runtime because they are required for correct encoding, decoding, and indexing regardless of advanced Unicode services.

## Plan of Work

### Milestone 1: Freeze the compatibility contract and inventory entry points

Inspect every use and declaration of `utf8bytes2chars`, `utf8chars2bytes`, `utf8chars2bytesBuf`, `utf8len`, `UTF8CharacterConverter`, `chars2bytes`, and direct casts between `JChar*`, `char*`, `NSString`, JNI strings, or platform wide strings. Search for assumptions that UTF-8 requires at most three bytes per `char`, and for buffers allocated as `length * 3`.

Inspect `String4D`, `StringBuffer4D`, `Character4D` or equivalent classes, deploy substitutions, and native-method registration to establish which Java code runs on JavaSE and which methods are replaced by native code on deployment. Locate existing tests for converters and determine whether native helper functions can be tested directly through a C test target or indirectly through deployed TotalCross tests.

Record the final malformed-input policy in `Decision Log`. The recommended policy is compatible with modern Java decoders in replacement mode: invalid UTF-8 input produces U+FFFD, and an unpaired surrogate encoded to UTF-8 produces the replacement byte sequence for U+FFFD rather than an illegal UTF-8 representation. Confirm existing TotalCross compatibility expectations before finalizing this choice.

At the end of this milestone, the plan names every production and test file to change, no hidden three-byte assumption remains unaccounted for, and a focused test command is known for both Java and native paths.

### Milestone 2: Add native UTF-16 helpers

Create a small internal header and implementation in the native VM, following existing utility placement and naming conventions discovered in Milestone 1. Prefer names such as `utf16IsHighSurrogate`, `utf16IsLowSurrogate`, `utf16ToCodePoint`, `utf16CodePointAt`, `utf16CodePointBefore`, and `utf16CodePointLength`. Keep these helpers internal; do not expose ICU-like public APIs or alter object structures.

The helpers must use bounds-aware signatures. A code-point read must never inspect the next code unit unless the caller supplied a length proving it exists. Arithmetic for constructing or splitting supplementary code points must use at least 32-bit unsigned intermediate values.

Define constants for:

- high-surrogate range U+D800 through U+DBFF;
- low-surrogate range U+DC00 through U+DFFF;
- minimum supplementary code point U+10000;
- maximum Unicode code point U+10FFFF;
- replacement character U+FFFD.

Add focused native tests for helper boundaries before using the helpers in converters. Test the first and last high surrogate, first and last low surrogate, U+10000, U+10FFFF, a BMP code point, an isolated high surrogate at end of input, and an isolated low surrogate.

At the end of this milestone, the native runtime has a single reviewed implementation of surrogate arithmetic and tests prove that no helper reads beyond its supplied range.

### Milestone 3: Correct the JavaSE UTF-8 converter

Update `TotalCrossSDK/src/main/java/totalcross/sys/UTF8CharacterConverter.java` so `bytes2chars` validates and decodes legal one-, two-, three-, and four-byte UTF-8 sequences. Four-byte sequences must produce exactly two UTF-16 code units. Reject overlong encodings, UTF-8 sequences that encode surrogate code points, values above U+10FFFF, invalid continuation bytes, and truncated sequences according to the replacement policy selected in Milestone 1.

Update `chars2bytes` so a valid high-surrogate and low-surrogate pair produces one four-byte UTF-8 sequence. Handle unpaired high and low surrogates according to the same replacement policy used by the native path. Replace the current fixed `length * 3` assumption with overflow-checked sizing that accounts for four output bytes per valid pair while still using no more than three bytes per individual BMP code unit.

Do not change the default converter in `Convert.java`; ISO-8859-1 compatibility is out of scope. Update comments that describe the converter as UCS-2 so they describe UTF-8 to UTF-16 conversion accurately.

At the end of this milestone, focused Java tests pass for supplementary code points, malformed input, BMP compatibility, and round trips.

### Milestone 4: Correct the native UTF-8 converter

Update `TotalCrossVM/src/nm/sys/CharacterConverter.c`. The decoder must perform the same validation and replacement decisions as the Java implementation and emit surrogate pairs for valid four-byte UTF-8. The output allocation remains safe because the number of UTF-16 code units can never exceed the number of input bytes, but every length adjustment must remain correct for pairs and replacement characters.

Update `utf8len` so it counts a valid surrogate pair as four output bytes. Update `utf8chars2bytesBuf` so it combines a valid pair and advances over both code units. It must not read the low surrogate unless one remains in the supplied range. Unpaired surrogates must follow the selected replacement policy. Check all callers of `utf8chars2bytesBuf` to confirm they allocate with the corrected `utf8len` and do not rely on the old three-bytes-per-code-unit maximum.

Where practical, factor validation rules into small static helpers rather than maintaining opaque bit arithmetic in two loops. Keep the implementation allocation-free apart from the returned VM arrays.

At the end of this milestone, native conversion behavior matches the Java path for the complete focused test corpus.

### Milestone 5: Complete code-point-aware Java compatibility helpers

Inspect the TotalCross equivalents of `java.lang.Character` and `java.lang.String`. Add only the methods required to expose correct UTF-16 code-point behavior and to support future text indexing. Candidate methods are `Character.isHighSurrogate`, `isLowSurrogate`, `isSurrogatePair`, `toCodePoint`, `charCount`, `toChars`, `codePointAt`, and `codePointBefore`, plus `String.codePointAt`, `codePointBefore`, `codePointCount`, and `offsetByCodePoints` if their Java signatures are expected by the supported class-library profile and are currently missing.

Implement JavaSE fallback bodies and native replacements only when repository conventions require native implementations. Favor small Java implementations for non-hot helpers unless profiling or existing class-library architecture justifies native code. Preserve standard Java exception behavior for invalid indexes.

This milestone does not change `char` semantics and does not make `substring` grapheme-aware. Document that indexes and lengths remain UTF-16 code units.

At the end of this milestone, application code can safely inspect and move by Unicode code point without breaking Java-compatible code-unit APIs.

### Milestone 6: Build the conformance and regression test corpus

Add a focused JUnit 5 test class under `TotalCrossSDK/src/test/java/totalcross/sys/` for `UTF8CharacterConverter`. Structure test data so each case states UTF-8 bytes, expected UTF-16 code units, and expected round-trip result. Avoid relying only on Java's built-in converter for expected values; include explicit byte and code-unit constants for boundary cases.

The valid corpus must include:

- empty input and ASCII;
- U+007F and U+0080;
- U+07FF and U+0800;
- U+D7FF and U+E000, around the surrogate range;
- U+FFFF;
- U+10000, encoded as `F0 90 80 80` and UTF-16 `D800 DC00`;
- U+1F600, encoded as `F0 9F 98 80` and UTF-16 `D83D DE00`;
- U+10FFFF, encoded as `F4 8F BF BF` and UTF-16 `DBFF DFFF`;
- mixed text such as `A😀B`, whose UTF-16 length is four;
- multiple adjacent supplementary code points;
- BMP text already used by existing applications, including accented Latin and CJK characters.

The malformed UTF-8 corpus must include:

- lone continuation bytes;
- truncated two-, three-, and four-byte sequences;
- invalid continuation bytes in every position;
- overlong encodings of ASCII and other values;
- UTF-8 encodings of surrogate values;
- values above U+10FFFF, including sequences beginning above F4;
- repeated malformed sequences to verify progress and prevent infinite loops;
- malformed data immediately before and after valid supplementary characters.

The malformed UTF-16 encoding corpus must include:

- isolated high surrogate at end of input;
- high surrogate followed by a BMP character;
- isolated low surrogate;
- two high surrogates;
- two low surrogates;
- valid pair surrounded by malformed surrogates.

Add native coverage using the repository's test mechanism. If the native converter is difficult to invoke directly, create a deployed TotalCross test application whose assertions run through native replacements and can be executed by the VM. Do not accept Java-only tests as proof of native correctness.

Add differential tests that run the same corpus through JavaSE and native paths and compare resulting UTF-16 code units or UTF-8 bytes. If one command cannot exercise both, store the same corpus in a simple shared source format or duplicate only the data with a comment requiring synchronized updates.

Where supported, run the native focused tests under AddressSanitizer and UndefinedBehaviorSanitizer. The malformed corpus is specifically intended to expose out-of-bounds reads, signed shifts, integer overflow, and incorrect pointer advancement.

At the end of this milestone, tests fail against the original implementation for four-byte UTF-8 and pass with the new implementation. The tests also prove existing BMP and ISO-8859-1 behavior is unchanged where applicable.

### Milestone 7: Closing validation and documentation

Run focused tests first, then the SDK test suite and the smallest native build that compiles the changed runtime. Run broader platform builds only if the modified code is compiled differently on those platforms or focused evidence reveals platform-specific behavior.

Update comments and API documentation that still call the internal representation UCS-2. State explicitly that `String` indexes are UTF-16 code-unit indexes and that code-point helpers are required to avoid splitting supplementary characters. Do not claim grapheme-cluster correctness.

Update `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective`. Add concise validation evidence to `.agent/evidence/utf16-core-text-indexing.md`, and create `.agent/reports/utf16-core-text-indexing-editorial.md` when the implementation is ready for final handoff, following `.agent/PLANS.md`.

At completion, issue #430 can be closed only after JavaSE and native paths pass the same supplementary and malformed-input corpus and the normal focused builds remain green.

## Validation and Acceptance

Use the validation levels defined in `AGENTS.md`. Feature-specific tests are Level 1. SDK module tests and the native test target are Level 2. Full SDK distribution or platform builds are Level 3 or Level 4 and are required only at milestone closure or when affected contracts justify them.

From the repository root, the expected focused Java command should be equivalent to:

    cd TotalCrossSDK
    ./gradlew-agent test --tests totalcross.sys.UTF8CharacterConverterTest

Expect all converter tests to pass. Before implementation, tests for U+10000, U+1F600, U+10FFFF, and four-byte malformed sequences should fail or expose the existing limitation.

Run the complete SDK tests after focused tests pass:

    cd TotalCrossSDK
    ./gradlew-agent test

Run the native focused target identified or created in Milestone 1. Record the exact command in this section when known. If a CMake target is added, prefer a command shaped like:

    cmake -S TotalCrossVM -B build -DCMAKE_BUILD_TYPE=Debug -G Ninja
    ninja -C build <utf16-test-target>
    ctest --test-dir build -R utf16 --output-on-failure

For a native smoke build at milestone closure:

    cmake -S TotalCrossVM -B build -DCMAKE_BUILD_TYPE=Release -G Ninja
    ninja -C build

Run sanitizer validation when supported by the active platform and toolchain. Save full logs outside the plan and record their paths in the evidence file.

Acceptance requires all of the following observable behavior:

1. Decoding `F0 90 80 80` returns UTF-16 code units `D800 DC00`.
2. Decoding `F4 8F BF BF` returns `DBFF DFFF`.
3. Encoding `D83D DE00` returns `F0 9F 98 80`.
4. `"A😀B".length()` is four and its code-point count is three.
5. Moving one code point from UTF-16 index one in `"A😀B"` reaches index three.
6. JavaSE and deployed native converter paths produce identical results for every corpus entry.
7. Malformed inputs produce the documented replacement behavior and never hang or access memory outside supplied buffers.
8. Existing one-, two-, and three-byte UTF-8 cases continue to round-trip.
9. ISO-8859-1 conversion behavior remains unchanged.
10. Focused tests, the SDK test suite, and the required native build pass.

## Surprises & Discoveries

- Observation: The current storage already uses 16-bit code units, so object layout and garbage collection do not need migration.
  Evidence: `String4D` stores `char[]`, native access uses `JChar*`, and `JChar` is `uint16`.

- Observation: Both Java and native UTF-8 converters share the same historical three-byte limitation, so fixing only one path would make JavaSE simulation disagree with deployed applications.
  Evidence: `UTF8CharacterConverter.java` and `CharacterConverter.c` both stop after a third UTF-8 byte and encode each `char` independently.

- Observation: The native encoder has a separate sizing function and output function, so they must be changed together to prevent under-allocation.
  Evidence: `utf8chars2bytes` allocates with `utf8len` and fills with `utf8chars2bytesBuf`.

Add implementation discoveries here only when they affect remaining work.

## Decision Log

- Decision: Preserve `String`, `char[]`, and `JChar[]` layout and define their indexes as UTF-16 code-unit indexes.
  Rationale: This matches existing TotalCross storage and Java, Android, and iOS indexing conventions without a GC or ABI migration.
  Date/Author: 2026-07-26 / OpenAI agent.

- Decision: Implement the UTF-16 core without ICU.
  Rationale: Surrogate arithmetic, UTF-8 conversion, and code-point indexing are small mandatory runtime operations. ICU is better evaluated separately for grapheme segmentation, normalization, bidi, and locale services.
  Date/Author: 2026-07-26 / OpenAI agent.

- Decision: Keep grapheme clusters and visual cursor behavior out of scope.
  Rationale: The immediate requirement is a stable UTF-16 unit for text ranges. Grapheme handling requires Unicode segmentation data and a separate UI/editor design.
  Date/Author: 2026-07-26 / OpenAI agent.

- Decision: JavaSE fallback and native deployed behavior must be validated against the same corpus.
  Rationale: TotalCross develops and simulates on JavaSE but replaces methods on deployment; divergence would create platform-only text corruption.
  Date/Author: 2026-07-26 / OpenAI agent.

- Decision: Final malformed-input replacement behavior remains to be confirmed in Milestone 1 and then recorded here.
  Rationale: The current code already emits U+FFFD in some decoder cases, but exact consumption and encoder behavior must be chosen deliberately for compatibility and parity.
  Date/Author: 2026-07-26 / OpenAI agent.

## Risks and Open Questions

The largest compatibility risk is malformed-input consumption. Replacing each invalid byte, replacing each maximal invalid subsequence, or following the exact JDK decoder policy can produce different counts of U+FFFD. Milestone 1 must compare current behavior, Java expectations, and native implementation cost before freezing the policy.

Some native callers may rely on the old `utf8len` behavior or allocate buffers with a hardcoded `3 * charCount`. The inventory must find and update these assumptions before changing shared helpers.

Existing APIs may use the word `character` when they mean code unit. Documentation must be corrected without changing Java-compatible method behavior.

Literal supplementary characters in Java source require the source and compiler path to preserve UTF-8 correctly. Tests should use explicit byte arrays and `\uD83D\uDE00` escapes for critical cases so source-file encoding cannot mask converter behavior.

A full native unit-test path may not exist for these internal functions. If so, the implementation must choose between a small CMake test executable and an end-to-end VM test. The choice must favor the repository's established test conventions and prove native replacement behavior.

The plan does not decide whether future public text APIs should reject ranges that split surrogate pairs. Java permits code-unit ranges that can split pairs; IME adapters may impose stricter validation later. This plan only makes representation and conversion correct.

## Idempotence and Recovery

All planned source changes are additive or local replacements and can be retried safely. Tests should be introduced before or with the implementation so a partial conversion change is visible immediately.

Do not modify generated output, downloaded files under `TotalCrossVM/deps`, build directories, or unrelated local files. Stage only files belonging to the active milestone. If Java and native behavior temporarily differ during development, keep commits clearly scoped and do not merge until differential tests pass.

If a converter change fails midway, restore consistency by reverting the logical converter commit rather than manually mixing old sizing code with new output code. `utf8len` and `utf8chars2bytesBuf` must always be committed together.

Build directories may be deleted and regenerated if they contain only generated output, but never use destructive Git commands against user changes. Record reproducible commands and failing corpus entries in the state and evidence files before stopping.

## Outcomes & Retrospective

Initial state: issue #430 and this implementation plan define the migration from UCS-2-like conversion behavior to real UTF-16 code-unit semantics. No production code has been changed yet.

At each milestone closure, summarize delivered behavior, remaining gaps, validation performed, and any compatibility decision that differed from this initial plan. At final completion, compare actual Java and native behavior with the ten acceptance requirements and identify follow-up work for grapheme-aware editing and ICU-backed Unicode services.

## Revision Note

2026-07-26: Created the initial plan from issue #430 and the current `String4D`, Java converter, native converter, repository test, and ExecPlan policies. The plan deliberately separates the UTF-16 core from ICU and grapheme-aware text editing so the first implementation remains bounded, testable, and suitable as the indexing foundation for future IME APIs.
