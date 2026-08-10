<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Subplan: fix float parameters and harden J2TC compatibility

Parent plan: `.agent/exec-plan-j2tc-semantics-and-tcm.md`.

Read this file only while Milestone 1 or Milestone 2 of the parent plan is active.

## Purpose / Big Picture

Correct the known `float` argument corruption and close the J2TC compatibility
bugs exposed by the completed ProGuard-before-J2TC experiment, without weakening
the converter to accept invalid JVM bytecode.

At the end:

- `float` may appear at any parameter position;
- the warning workaround in `JavaMethod` no longer exists;
- JVM-valid sparse line tables are accepted safely;
- JVM-valid inherited-owner invocations resolve correctly;
- every remaining ProGuard rejection family is either fixed with a regression
  fixture or explicitly classified as invalid/unsupported input with verifier
  evidence.

## Current Architecture

`OperandReg.init(String[] params, boolean methodIsStatic)` maps Java local-variable
indexes to three TotalCross register banks:

```text
hashI   -> regI
hash64  -> reg64
hashO   -> regO
```

For instance methods Java local slot 0 is `this`. JVM parameters then occupy local
slots according to class-file width:

```text
boolean/byte/char/short/int  1 slot
float                        1 slot
reference/array              1 slot
long                         2 slots
double                       2 slots
```

TotalCross register-bank choice is different:

```text
boolean/byte/char/short/int  regI
float/long/double            reg64
reference/array              regO
```

The current bug groups `F` with `J` and `D` for both decisions and increments the
Java local index by two.

`GlobalConstantPool.javaPrimitiveType2TCType()` currently maps `F` to `&D`; that
runtime normalization is not changed by this subplan.

The ProGuard editorial report is the stable source for the compatibility failure
categories. Do not reread generated experiment logs unless reproducing a specific
category.

## Slice A — correct JVM slot width for float

Implement the narrow semantic fix before touching warning code.

Prefer an explicit helper or equivalent structure that makes these independent:

```text
javaLocalSlotWidth(descriptor)
tcRegisterBank(descriptor)
```

Required invariant:

```text
F -> java width 1, TC reg64
J -> java width 2, TC reg64
D -> java width 2, TC reg64
```

Do not change float arithmetic/storage semantics or introduce a separate TC float
register bank.

Add a direct unit test of parameter-local mapping so the root cause remains
obvious even if higher-level converter tests later change.

For static methods, cover at least these descriptors:

```text
(F)V
(FI)I
(IF)F
(IFI)I
(FLjava/lang/Object;)Ljava/lang/Object;
(FJ)J
(FD)D
(DFI)I
(JFLjava/lang/Object;)Ljava/lang/Object;
(FFI)I
```

Repeat representative cases for instance methods to prove the `this` offset.
Prefer parameterized/data-driven tests rather than one method per case.

For every case assert the relevant Java local index maps to the expected TotalCross
parameter register. Include at least one case where a `double` precedes a `float`
and an `int` follows it:

```text
(D F I)
local 0/1 -> D
local 2   -> F
local 3   -> I       (static)
```

and the corresponding instance offset.

Add conversion-level fixtures that compile ordinary Java source and prove methods
read/return the argument after the float, rather than only inspecting internal
maps.

## Slice B — remove obsolete float warning infrastructure

Only after Slice A focused tests pass, remove from `JavaMethod`:

```text
FLOAT_WARNING_MESSAGE
floatWarningMethods
registerFloatWarning(...)
flushFloatWarnings()
```

Remove constructor/parser loops whose only purpose is detecting a non-final float
parameter, and remove `J2TC` call sites that flush these warnings.

Search for the warning text and helper names after the edit. There must be no
remaining workaround message telling users to replace `float` with `double`.

Do not remove unrelated float warnings or documented TCVM binary32/binary64
limitations unless they are specifically the obsolete parameter-order warning.

Update architecture/user-facing documentation only where it still states the
parameter-order restriction.

## Milestone 1 closure validation

Do not run this sequence during every edit. Run it once after Slices A and B are
implementation-complete.

Focused tests should include:

- direct parameter-map matrix;
- Java-source-to-J2TC conversion fixture;
- any existing converter regression suite touched by `OperandReg`.

Then build/deploy the aggregate smoke application. Add a small smoke fixture that
executes methods where a float is first/middle and returns/uses a later int,
reference, long, and double. Keep the new smoke file under 20 KiB/600 lines and
wire it into the existing aggregate `FeatureSmokeApp`.

Typical closure flow:

```bash
cd TotalCrossSDK
./gradlew-agent test --tests <float-matrix-test>
./gradlew-agent dist -x test
./gradlew-agent deployModernJavaFeatureSmoke --warning-mode=none --console=plain
```

Run the generated macOS app as the final proof:

```text
TotalCrossSDK/build/feature-smoke/classes/install/macos/FeatureSmokeApp
```

If the native runtime needed by the smoke app is unavailable, build `tcvm` once at
this closure point, not during implementation loops.

Acceptance:

- all parameter-matrix cases pass;
- deploy succeeds;
- native macOS app exits 0;
- no `[FAIL]` lines;
- no obsolete float-order warning appears.

Commit the code and tests together after validation.

## Slice C — sparse LineNumberTable correctness

Reproduce the previous failure with a minimal class whose valid line table has its
first `start_pc > 0`. Prefer a deterministic generated class fixture over requiring
ProGuard in the unit test.

Inspect `Bytecode2TCCode.getLineOfPC` and line-number generation.

Required behavior:

- never index `-1` or read a nonexistent previous line entry;
- bytecode before the first line-table entry has "unknown source line" semantics;
- do not silently map early bytecode to the first later line unless a documented
  existing TC debug contract requires that behavior;
- normal line tables beginning at zero preserve current output;
- line information after the first entry is unchanged.

If TC instructions require an integer sentinel, define and document one local
contract rather than scattering magic values.

Tests must cover:

```text
no LineNumberTable
first entry at pc 0
first entry after pc 0
multiple sparse entries
pc exactly at an entry
pc between entries
pc after final entry
```

Also prove generated TC line metadata remains structurally valid.

This is a general class-file compatibility fix, not a ProGuard-specific workaround.

After focused validation, create a separate logical commit.

## Slice D — inherited invocation-owner resolution

Create a JVM-valid fixture where the symbolic owner named by an invoke instruction
does not itself declare the target method but inherits it from a superclass.

The previous `java.util.Properties.put` shape is a useful regression target, but
the unit test should preferably use small generated classes so it is stable and
does not depend on JDK implementation details.

Model:

```text
class Parent { Object put(Object a, Object b) ... }
class Child extends Parent { }
void use(Child c) { c.put(...); }
```

Generate or compile bytecode whose method reference owner is `Child`.

Required behavior:

- J2TC resolves the symbolic method reference through the supported class hierarchy;
- the converter keeps the correct invoke semantics (`virtual`, `special`, static,
  or interface as applicable);
- TotalCross 4D/device-API replacement rules are still applied after/beside JVM
  owner resolution and are not bypassed;
- native/replaced method identity remains deterministic;
- access checks or unsupported semantics still produce precise errors.

Do not globally rewrite every owner to the declaring superclass if doing so changes
virtual dispatch semantics. Resolve the declaration for compatibility/validation,
while keeping the runtime call representation appropriate for the original invoke.

Add regression coverage for at least:

```text
inherited virtual method
inherited method through a TotalCross-compatible hierarchy
negative unresolved method
```

If interface/default-method resolution shares this code path, include the smallest
existing interface fixture needed to prove no regression rather than inventing a
large new matrix.

Commit separately after focused validation.

## Slice E — classify optimized stack/replacement/name failures

The previous experiment also saw:

```text
operand-stack underflow in some optimized classes
transformed replacement constructors
generated names such as Reader.read$... / BiPredicate.test$...
```

Do not assume these are J2TC bugs.

For each distinct failure category:

1. Recreate the smallest candidate class from the exact ProGuard configuration or
   saved artifact.
2. Verify the class before J2TC:
   - use `java -Xverify:all` with a minimal loader when practical; and/or
   - use ASM `CheckClassAdapter` if the repository's ASM dependencies support it.
3. Record verifier status and the first stable J2TC diagnostic.
4. If the class is invalid/unverifiable, classify the case as not a J2TC
   compatibility requirement and stop that category.
5. If valid, determine whether the behavior can also arise from javac or another
   standards-compliant transformer.
6. Only then add a focused J2TC fix and regression fixture.

For valid stack-shape bugs, correct the stack simulation/merge logic at the
narrowest semantic point. Do not suppress underflow or prefill missing operands.

For valid transformed replacement-name cases, normalize semantic identity using
the existing 4D/native replacement model. Do not add literal ProGuard-generated
method names to an allowlist.

For any corrected category, add one logical commit. If a category is classified as
non-bug, record the evidence but do not create a no-op code change.

## Milestone 2 closure validation

After all categories are resolved, run one focused compatibility sweep.

Rerun only ProGuard variants needed to demonstrate that the previously confirmed
J2TC bugs are no longer the first failure. Do not rerun the entire historical
experiment unless the set of fixes materially changes its conclusion.

Expected result format:

```text
line-table failure              fixed
inherited-owner failure         fixed
stack category A                fixed | invalid input | intentionally unsupported
replacement category B          fixed | invalid input | intentionally unsupported
generated-name category C       fixed | invalid input | intentionally unsupported
```

Run relevant converter tests and aggregate deploy smoke. Because invocation
resolution may affect runtime semantics, run native macOS FeatureSmokeApp once as
the final milestone step.

Do not claim ProGuard production compatibility merely because more variants pass.

## File-Size and Commit Rules

Every new test/helper/fixture source must stay at or below 20 KiB and around 600
lines. Split data matrices from fixture builders if needed.

Do not refactor `J2TC.java`, `Bytecode2TCCode.java`, or another existing oversized
legacy file merely to make it smaller. Make surgical edits or extract only a new,
cohesive responsibility that this work genuinely needs.

At the end of each validated logical slice:

```bash
git status --short
```

Stage only the slice. Commit locally with a repository-compliant explanatory
message. Do not push.

## Token-Efficient Execution

Use the parent state file as the resume anchor.

While implementing one slice:

- read only the named method/class plus its closest tests;
- do not reread the ProGuard editorial report after the failure category has been
  copied into state;
- avoid full Gradle test suites until milestone closure;
- when a command fails, inspect only the first stable error and nearby context;
- keep full verifier/ProGuard output in ignored logs and record a compact evidence
  pointer.

A successful focused test does not justify immediately running native smoke; defer
that proof until the milestone is ready to close.

## Acceptance Summary

This subplan is complete when:

- float parameter local-slot width is correct and warning infrastructure is gone;
- the requested float matrix passes conversion and native smoke;
- sparse line tables are safe;
- inherited owner references are resolved correctly;
- every remaining previous ProGuard J2TC rejection family has a verified
  classification;
- confirmed converter bugs have regression tests and logical commits;
- no validation invariant was relaxed merely for ProGuard.
