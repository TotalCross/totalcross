<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Subplan: preserve Java semantics for TCM

Parent plan: `.agent/exec-plan-j2tc-semantics-and-tcm.md`.

Read this file only while Milestone 3 of the parent plan is active.

## Purpose / Big Picture

Add an optional, versioned `TCM` (TotalCross Compilation Metadata) artifact that
preserves Java/source facts needed by future optimization and AOT without changing
the TCZ format or interpreter semantics.

TCZ remains the executable truth. TCM is additional metadata tied to the exact TCZ
artifact set.

This subplan implements the in-memory semantic model and capture points only.
Serialization and deploy emission are handled by the format/emission subplan.

## Architectural Principle

Do not attempt to reconstruct source facts later from:

```text
CALL_normal
regI/regO/reg64
physical register numbers
generated lambda classes
normalized 4D names
```

Capture them while the converter still owns both the Java representation and the
lowered TotalCross representation.

The in-memory model must preserve facts, not optimizer decisions.

Good TCM facts:

```text
source descriptor F
lowered type &D
source invoke kind INTERFACE
lowered opcode CALL_normal
original method identity
effective TC method identity
Java PC 116
TC slot range 73..75
known Class.forName root
unknown dynamic-access marker
lambda adapter origin
```

Do not serialize:

```text
canInline=true
isDead=true
predictedReceiver=...
optimizedCallGraph=...
```

Those are version-dependent analysis results.

## Scope of TCM v1

TCM v1 must preserve these semantic families when the information exists:

1. Artifact identity:
   - TCM format version;
   - converter/deployer version or build identity available without private paths;
   - ordered list of TCZ artifacts produced by the deploy invocation;
   - file name relative to the deploy output set;
   - SHA-256 for every TCZ.

2. Class facts:
   - original Java class name;
   - effective TotalCross class name after Java-lang/4D normalization;
   - raw Java access flags plus convenient semantic flags;
   - superclass and interfaces;
   - nest host/members when present;
   - permitted subclasses when present;
   - record-component identity/descriptors when present;
   - source file when present.

3. Field facts:
   - original/effective owner;
   - field name;
   - original Java descriptor;
   - lowered TC type/bank;
   - raw Java access flags;
   - constant value when already safe/representable in converter metadata;
   - mapping to the corresponding TC field/symbol identity.

4. Method facts:
   - original/effective owner, name, and full JVM descriptor;
   - source parameter/return descriptors;
   - lowered parameter/return TC types;
   - raw Java access flags;
   - original native method versus `ReplacedByNativeOnDeploy`;
   - mapping to TC method identity.

5. Call-site facts:
   - caller method;
   - Java bytecode PC and opcode;
   - source invoke kind: `STATIC`, `SPECIAL`, `INTERFACE`, `VIRTUAL`, or recognized
     dynamic lowering kind;
   - symbolic owner/name/descriptor;
   - resolved declaration owner when known;
   - lowered TotalCross call opcode;
   - final TC slot range.

6. Origin map:
   - Java PC/source opcode;
   - final TC start slot;
   - final TC end slot exclusive;
   - allow one Java bytecode to map to zero, one, or multiple TC instructions.

7. Allocation/synthetic-lowering facts:
   - exact `new`/array allocation type when known;
   - generated lambda adapter -> original lambda site;
   - recognized record/string-concat lowering origin where practical without
     duplicating large recipes.

8. Dynamic-access facts:
   - resolved `Class.forName` roots already discovered by the deployer;
   - whether unresolved/dynamic class lookup was observed;
   - native/replacement/pinning facts already known to the deployer.

9. Verification/type-frame facts:
   - parsed `StackMapTable` frames at their Java bytecode offsets when present;
   - locals and operand-stack verification types in symbolic form;
   - preserve exact `FLOAT` versus `DOUBLE`.

Do not embed raw `.class` files in TCM v1. Reserve the sectioned format for a
future FULL/debug mode if later AOT evidence requires raw class blobs.

## In-Memory Metadata Model

Create a dedicated package, preferably:

```text
TotalCrossSDK/src/main/java/tc/tools/converter/metadata/
```

Keep responsibilities small. Suggested files are examples:

```text
CompilationMetadata.java
CompilationMetadataCollector.java
TcmFormat.java
TcmWriter.java
TcmReader.java
StackMapTableReader.java
```

Each new file must remain <=20 KiB and around 600 lines.

Avoid a mutable graph shared globally across the entire converter when a
deploy-scoped collector can own the state.

A practical shape is:

```text
CompilationMetadata
  artifacts
  classes
  dynamicAccess

ClassMetadata
  identities
  hierarchy
  fields
  methods
  syntheticOrigins

MethodMetadata
  identities/types/flags
  typeFrames
  callSites
  originRanges
```

Use stable integer/string-table identifiers inside the serialized format, but keep
the Java in-memory API readable.

## Preserve original identity before normalization

J2TC currently normalizes class and method identity for TotalCross/4D replacement
behavior.

Before mutation, snapshot:

```text
originalClassName
originalMethodName
originalJvmDescriptor
```

Keep separate effective fields rather than attempting to reverse 4D normalization
later.

For `ReplacedByNativeOnDeploy`, preserve an explicit kind such as:

```text
NONE
JAVA_NATIVE
REPLACED_ON_DEPLOY
```

Do not serialize annotation implementation source or native C code.

## Preserve raw access flags

`JavaClass`, `JavaField`, and `JavaMethod` expose selected booleans but do not
retain every original JVM access bit.

Add `rawAccessFlags` or an equivalent exact value at parse time and in the ASM
constructor path.

Do not remove existing booleans; they remain convenient compatibility APIs.

Where an existing TC flag exists but is not currently populated, such as
synthetic class information, populate it only when that change is independently
correct for TC runtime serialization. Otherwise preserve the flag in TCM without
silently changing TCZ semantics.

## Preserve class-file attributes needed by TCM

Extend parsing only for attributes used by the sidecar.

Class-level minimum:

```text
SourceFile
Signature (when present)
InnerClasses / EnclosingMethod when useful for synthetic origin
existing NestHost / NestMembers
existing Record
existing PermittedSubclasses
```

Method/code minimum:

```text
StackMapTable
```

`LocalVariableTable` and `LocalVariableTypeTable` are debug-friendly but not
required for TCM v1 unless implementation can preserve them cheaply without
inflating scope.

Unknown attributes must continue to be skipped by declared length.

### StackMapTable

Implement a bounded parser for JVM verification frames, not a second bytecode
verifier.

Convert constant-pool-based verification types to symbolic TCM types while the
Java constant pool is still available.

Preserve frame bytecode offset and locals/stack types.

Test all frame encodings needed by current javac output and at least generated
fixtures for:

```text
same_frame
same_locals_1_stack_item_frame
chop_frame
append_frame
full_frame
object verification type
uninitialized verification type
float/double/long distinction
```

Malformed attributes must fail with a precise converter/class-file diagnostic,
not an array exception.

## Capture Java -> TC origin

Origin capture must begin during `Bytecode2TCCode.convert`, before Java opcode
identity disappears.

Each lowered semantic operation needs:

```text
javaPc
javaOpcode
source invoke/type/symbol facts when relevant
```

Then correlate it with the final TC slot range after instruction expansion and
register allocation.

First inspect whether `Instruction` object identity survives
`RegAllocation.makeRegAllocation()` and `instruction2TCCode()`.

Decision gate:

- if identity is stable, keep an external identity-based origin map in the
  metadata collector and avoid changing `Instruction` layout;
- if instructions are replaced/expanded such that identity cannot survive, add
  the smallest explicit origin identifier/source metadata necessary to the base
  instruction representation.

Do not add a large metadata object to every runtime TCCode slot.

For Java bytecodes that lower to no TC operation, an origin entry may explicitly
record an empty TC range if useful for diagnostics. Do not fabricate a TC PC.

## Capture call-site semantics before lowering

`MethodCall` already knows source owner/name/descriptor and argument/return
descriptors. Capture source invoke kind from the Java opcode before:

```text
invokestatic
invokespecial
invokeinterface
```

collapse into `CALL_normal`.

For `invokevirtual`, record both source `VIRTUAL` and lowered `CALL_virtual`.

After the compatibility milestone resolves inherited owners, store both:

```text
symbolicOwner
resolvedDeclarationOwner
```

when they differ.

This is critical for future AOT devirtualization and diagnostics.

## Capture synthetic lowering origins

Hook metadata into the existing lowering helpers instead of reverse-engineering
generated names later.

For lambda lowering preserve at least:

```text
original owner/method/javaPc
generated adapter class
factory method
SAM descriptor
implementation kind
implementation owner/name/descriptor
capture descriptors
```

Keep bridge/marker details when already represented by `LambdaSite`.

For record `ObjectMethods` and string concat, record a compact lowering kind and
origin site. Do not duplicate large constant recipes unless future consumption
requires them.

## Capture dynamic access

Convert existing deploy-time knowledge such as:

```text
J2TC.callForName
J2TC.notResolvedForNameFound
```

into deploy-scoped metadata facts.

Avoid adding network/telemetry behavior. TCM contains application identities and
must remain a local build artifact.

## Milestone 3 closure validation

Run only focused tests for raw access flags, StackMap parsing,
original/effective identity, source/lowered types, call-site facts, dynamic-access
facts, and origin collection. Do not run native smoke because no external artifact
contract exists yet.

Commit cohesive slices logically. StackMap parsing may be a separate commit from
the core semantic model if that boundary simplifies review and recovery.

## File-Size and Token Rules

Every new metadata model/parser/test file must remain <=20 KiB and about 600 lines.
Do not split existing large converter files solely for size. During execution read
only the active parser/lowering/helper and closest tests; use minimal generated
fixtures and defer broad Gradle/deploy/native validation to milestone closure.

## Acceptance Summary

This subplan is complete when source semantic facts survive in an explicit
in-memory model without changing TCZ serialization, including exact source types,
raw flags, original/effective identities, StackMap frames, source invoke kind,
dynamic-access facts, synthetic origins, and Java-PC origin information suitable
for final TC slot correlation.
