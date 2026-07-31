<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Editorial Summary

Lambda lowering now accepts inherited instance receivers, explicitly discards
implementation values when the functional method returns `void`, and packages
generated adapters for every converted class entry. The isolated
`this::getGraphics` case and the aggregate Scanner/runtime cases pass on a
macOS VM built from this checkout.

## Original Plan versus Actual Outcome

The planned receiver, return-adaptation, packaging, focused-test, SDK-build,
and macOS-smoke milestones were completed. The exact fixture source remained
unchanged in meaning, including `map.put("1", this::getGraphics)`. The final
runtime returned zero and emitted the expected pass lines without the previous
converter diagnostic or `Scanner$$TC$$Lambda$N` class-loading failure.

## What Changed

`Java8LambdaLowering` now walks superclass and interface metadata from class
files found through the deployer lookup, caches headers for one conversion run,
and rejects unresolved types conservatively. Its return path emits `POP` for
category-one values and `POP2` for category-two values before a `void` SAM
return. `J2TC` discovers adapters in `processFiles` before converting each
class, independently of recursive dependency expansion, with duplicate guards
preserved. Build tasks compile/deploy/run the isolated fixture and aggregate
smoke, with `-PtcvmDylib` selecting the freshly built runtime.

Focused tests cover the inherited receiver, discarded `String` result, adapter
bytecode, owner conversion, and exactly-once adapter queueing.

## Decisions and Trade-offs

Hierarchy resolution uses class-file metadata and the deployer's real classpath
instead of host reflection, keeping deployment deterministic. Adapter discovery
is centralized at the conversion queue boundary, which covers direct classes,
expanded classes, prohibited expansion paths, and JAR/ZIP entries; the trade-off
is that adapter generation occurs immediately before each class conversion
rather than during dependency discovery.

## Unexpected Problems and Discoveries

The javac fixture uses `()V` as the instantiated descriptor while its inherited
implementation handle returns `String`; the validator therefore needed a
SAM-aware discard exception, not merely a value compatibility relaxation. The
first version of the new Gradle `Exec` tasks passed a lazy Provider directly to
`commandLine`, which Gradle treated as a literal command; assigning the resolved
file in `doFirst` fixed the task. The native VM build emitted existing compiler
warnings but linked successfully.

## Validation and Measurable Results

Focused lowering test: exit 0. Modern-Java converter group: exit 0. SDK
distribution: exit 0. CMake configure and `ninja -C build tcvm`: exit 0. The
isolated and aggregate Gradle run tasks each exited 0 using
`build/libtcvm.dylib`. The aggregate output contains the Java 8 smoke summary
`28 tests`, Scanner version `1.0`, and the asynchronous callback line.

## Smoke Test Success Matrix

| Command | Platform / runtime | Exit | Expected adapters | Observed result |
| --- | --- | ---: | --- | --- |
| `./gradlew-agent deployLambdaLoweringFixesRepro --no-daemon --console=plain` | macOS arm64; SDK dist | 0 | `lambda.repro.GraphicsReferenceRepro$$TC$$Lambda$0` | TCZ created; adapter present exactly once |
| `./gradlew-agent runLambdaLoweringFixesRepro -PtcvmDylib=/Users/flsobral/repos/totalcross-github/build/libtcvm.dylib --no-daemon --console=plain` | macOS arm64; checkout-built `libtcvm.dylib` | 0 | same isolated adapter | `[PASS] Lambda repro - inherited getGraphics method reference` |
| `./gradlew-agent deployModernJavaFeatureSmoke --no-daemon --console=plain` | macOS arm64; SDK dist | 0 | `smoke.Java8FeatureSmokeTest$$TC$$Lambda$0..32,$34,$35`; Scanner `$0..2` in `tc.base.misc.tcz` | Aggregate TCZs created; all names present once |
| `./gradlew-agent runModernJavaFeatureSmoke -PtcvmDylib=/Users/flsobral/repos/totalcross-github/build/libtcvm.dylib --no-daemon --console=plain` | macOS arm64; checkout-built `libtcvm.dylib` | 0 | all above | Map, `HashMap.put`, scheduler, Scanner, and remaining Java 8 checks emitted `[PASS]`; no `ClassNotFoundException` |

## Useful Evidence and Examples

The complete command summaries and compact results are in
`.agent/evidence/lambda-lowering-fixes.md`. Full phase logs are in
`TotalCrossSDK/build/lambda-lowering-fixes/`. TCZ name inspection is in
`tcz-contents.log` and `all-tcz-contents.log`; the isolated and aggregate
runtime transcripts are in the Gradle-agent full logs referenced by the task
summary files.

## Limitations, Remaining Work, and Open Questions

The full SDK test suite was not repeated after the focused modern-Java group;
the plan's relevant converter group and `dist -x test` validations passed. The
category-two branch is implemented by type size but the exact new smoke uses a
category-one `Graphics`/`String` result. No release, publication, or push was
performed.

## Possible Article Angles

One useful angle is how a legal Java method-reference adaptation crosses three
layers: JVM descriptors, generated adapter bytecode, and the deployed TCZ
class graph. Another is why dependency expansion and conversion queueing must
be separate when deployment transforms classes.

## Suggested Narrative

Start with the failing inherited `this::getGraphics` deployment, show that it
combines an assignable receiver with a value-to-void adaptation, then follow
the fix through class-file hierarchy lookup and explicit stack cleanup. Close
with the Scanner missing-class failure and the queue-boundary packaging change,
verified by the fresh macOS runtime and TCZ contents.

## Claims Requiring Human Review

Review the compatibility claim that all legal value-to-void method-reference
forms are covered beyond the tested category-one case, and review whether the
new public adapter-enqueue helper should remain public or be narrowed in a
follow-up API cleanup. All claims about the executed smokes are backed by the
recorded exit codes and logs above.
