<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Editorial Summary

Lambda value-to-`void` method-reference adaptation is now covered for every
JVM return kind. The focused converter test proves category-one values use
`POP`, category-two `long` and `double` values use `POP2`, and `void` emits
only the void return. A matching macOS VM executes all eleven deployed cases.

## Original Plan versus Actual Outcome

The plan was completed without changing production converter code. The planned
fixture, dedicated smoke, Gradle tasks, matching SDK/VM builds, preserved
regression smokes, complete SDK suite, adapter inventory, state, evidence, and
editorial report all exist. The full suite passed, so no regression
classification or comparative baseline work was needed.

## What Changed

`Java8LambdaLoweringTest` now compiles a fixture with bound method references
returning `void`, `boolean`, `byte`, `char`, `short`, `int`, `long`, `float`,
`double`, `String`, and `int[]`. It matches each generated adapter by the
implementation call, checks the discard opcode and final `RETURN`, rejects
remaining `invokedynamic`, verifies unique adapter names, and converts every
adapter with `J2TC`.

`LambdaReturnDiscardRepro` invokes one action for each return kind, records one
side effect per implementation, catches failures per case, prints eleven case
results and a final summary, and exits nonzero if any case fails. Gradle now
compiles, deploys, and runs this smoke with `-PtcvmDylib=<path>`.

The aggregate modern-Java smoke now gives the Java 8 Predicate case its own
`[CASE]` and `[PASS]` result, counts local failures in the suite, and validates
captured runtime output in addition to the process exit code. It rejects a
missing Predicate pass, a Predicate failure, a Predicate-related
`NoSuchMethodError`, or `Aborting program`.

## Decisions and Trade-offs

The focused fixture is table-driven in its assertions but uses explicit source
methods so each JVM descriptor remains visible. The deployed smoke is separate
from `GraphicsReferenceRepro`, preserving the original inherited-receiver
regression signal. The smoke reuses the existing `lambda-repro` source tree but
uses a separate output directory and task family to isolate its TCZ artifacts.

## Unexpected Problems and Discoveries

TCZ archives are not ordinary ZIP files. Standard `unzip` could not inspect the
new artifact, so inventory validation used the repository's existing
`totalcross.util.zip.TCZ` reader through JShell. This did not affect the
implementation or smoke result.

## Validation and Measurable Results

Focused lambda test: exit 0, 23 tests. Modern-Java converter group: exit 0.
SDK distribution: exit 0. CMake configure and native `tcvm` build: exit 0.
The new smoke: exit 0, eleven pass lines, zero failures. The inherited smoke:
exit 0. The aggregate smoke: exit 0, Java 8 reports 28 tests, Scanner reports
version 1.0, and the asynchronous callback is observed. Complete SDK suite:
exit 0, 43 tests, 43 passed, 0 failed, 0 skipped. Final `git diff --check`:
exit 0.

## Predicate Validation Evidence

The aggregate smoke was run with the matching checkout-built VM using:

    ./gradlew-agent runModernJavaFeatureSmoke \
      -PtcvmDylib=/Users/flsobral/repos/totalcross-github/build/libtcvm.dylib \
      --no-daemon \
      --console=plain

The task exited `0`. Its complete runtime log is
`TotalCrossSDK/agent-logs/20260731-182844-runModernJavaFeatureSmoke-full.log`.
That log contains:

    [CASE] Java 8 - Predicate default methods
    [PASS] Java 8 - predicate default and
    [PASS] Java 8 - predicate default or
    [PASS] Java 8 - predicate default negate
    [PASS] Java 8 - Predicate default methods

The Predicate case exercised `and`, `or`, and `negate`, and the dedicated pass
line was emitted only after all three and the remaining Predicate-family
assertions completed. No Predicate-related `NoSuchMethodError`, Predicate
`[FAIL]` line, or `Aborting program` occurred. The result was not inferred from
the existing `Java 8 smoke OK (28 tests)` summary.

## Return-Type Smoke Matrix

| Java type | JVM descriptor | Stack category | Expected discard | Generated adapter | Deploy | Runtime | Side effect | Evidence |
| --- | --- | ---: | --- | --- | ---: | --- | --- | --- |
| void | `()V` | none | none | `LambdaReturnDiscardRepro$$TC$$Lambda$0` | 0 | PASS | `returnVoid` count 1 | focused test; `run-return-types-smoke.log` |
| boolean | `()Z` | 1 | `POP` | `$$TC$$Lambda$1` | 0 | PASS | `returnBoolean` count 1 | same |
| byte | `()B` | 1 | `POP` | `$$TC$$Lambda$2` | 0 | PASS | `returnByte` count 1 | same |
| char | `()C` | 1 | `POP` | `$$TC$$Lambda$3` | 0 | PASS | `returnChar` count 1 | same |
| short | `()S` | 1 | `POP` | `$$TC$$Lambda$4` | 0 | PASS | `returnShort` count 1 | same |
| int | `()I` | 1 | `POP` | `$$TC$$Lambda$5` | 0 | PASS | `returnInt` count 1 | same |
| long | `()J` | 2 | `POP2` | `$$TC$$Lambda$6` | 0 | PASS | `returnLong` count 1 | same |
| float | `()F` | 1 | `POP` | `$$TC$$Lambda$7` | 0 | PASS | `returnFloat` count 1 | same |
| double | `()D` | 2 | `POP2` | `$$TC$$Lambda$8` | 0 | PASS | `returnDouble` count 1 | same |
| object (`String`) | `()Ljava/lang/String;` | 1 | `POP` | `$$TC$$Lambda$9` | 0 | PASS | `returnObject` count 1 | same |
| array (`int[]`) | `()[I` | 1 | `POP` | `$$TC$$Lambda$10` | 0 | PASS | `returnArray` count 1 | same |

The complete adapter names and TCZ paths are in
`TotalCrossSDK/build/lambda-return-types-regression/all-tcz-inventory.log`.

## Full-Suite Regression Matrix

| Suite or failing test | Command | Exit | Counts | First failure | Isolated reproduction | Attribution | Action | Final status |
| --- | --- | ---: | --- | --- | --- | --- | --- | --- |
| Complete `TotalCrossSDK` Gradle suite | `./gradlew-agent test --no-daemon --console=plain` | 0 | 43 passed, 0 failed, 0 skipped | none | not applicable | no regression observed | none | PASS |

## Useful Evidence and Examples

Environment and task names: `TotalCrossSDK/build/lambda-return-types-regression/environment.log`.
Focused and full logs: `focused-lambda-test.log`, `modernjava-tests.log`, and
`full-sdk-tests.log`. Build logs: `dist.log`, `cmake.log`, and `tcvm.log`.
Runtime logs: `run-return-types-smoke.log`, `run-inherited-smoke.log`, and
`run-aggregate-smoke.log`. XML test reports are under
`TotalCrossSDK/build/test-results/test/`.

## Limitations, Remaining Work, and Open Questions

This plan validates the macOS runtime and the complete Gradle-managed SDK suite
only. It does not add cross-platform native builds, publish artifacts, or
broaden support to non-lambda `invokedynamic` bootstraps.

## Possible Article Angles

One angle is how a legal Java value-to-`void` method reference crosses source
descriptors, generated adapter bytecode, and a deployed TCZ class graph. A
second is why category-two JVM values require a distinct `POP2` runtime proof.

## Suggested Narrative

Begin with the old inherited reference smoke and its category-one-only signal.
Then show the descriptor matrix and the focused bytecode assertions, followed
by the eleven deployed runtime cases. Close with the preserved aggregate smoke,
adapter inventories, and the 43-test clean SDK suite.

## Claims Requiring Human Review

Review whether the source-order mapping from return kind to adapter ordinal is
worth treating as a documented convention; the focused test itself matches
semantic implementation calls and does not depend on those ordinals. Review
whether future non-macOS runtime coverage should be added separately.
