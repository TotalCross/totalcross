<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Subplan: define and emit TCM v1

Parent plan: `.agent/exec-plan-j2tc-semantics-and-tcm.md`.

Read this file only while Milestone 4 of the parent plan is active. It consumes the
in-memory model produced by `.agent/subplan-tcm-semantic-preservation.md`.

## Purpose / Big Picture

Serialize the preserved semantic facts into a deterministic optional `.tcm`
sidecar, add an opt-in deploy mode, and prove that enabling metadata cannot alter
TCZ bytes or native execution.

TCZ remains the executable semantic authority. TCM is local build metadata and no
TCVM/TCIR/AOT consumer becomes mandatory in this milestone.

## TCM v1 binary format

Use a deterministic, little-endian, sectioned format.

Header:

```text
4 bytes   magic "TCM1"
u16       major format version = 1
u16       minor format version = 0
u32       flags
u32       section count
```

Every section:

```text
u16       section type
u16       section version
u32       payload length
bytes     payload
```

Unknown section types/versions that are declared skippable must be skipped by
length. Required-section incompatibility must produce a precise reader error.

Recommended v1 sections:

```text
1  STRING_TABLE
2  ARTIFACT_MANIFEST
3  CLASSES
4  FIELDS
5  METHODS
6  CALL_SITES
7  ORIGIN_MAP
8  ALLOCATION_AND_SYNTHETIC_ORIGINS
9  DYNAMIC_ACCESS
10 TYPE_FRAMES
```

The exact numeric assignments become part of the documented v1 contract once the
first writer/reader test is committed. Do not casually renumber them afterward.

Use deduplicated UTF-8 strings through `STRING_TABLE`.

Do not use Java serialization.

## Artifact manifest and naming

Emit one TCM per deploy invocation, not one metadata file per class.

If the deploy produces one or multiple TCZ parts, the TCM manifest lists all of
them in deterministic order.

Place the sidecar beside the primary/root TCZ using the same base name:

```text
MyApp.tcz
MyApp.tcm

or, for split output:

MyApp.tcz
MyApp_1.tcz
MyApp_2.tcz
MyApp.tcm
```

The manifest includes every TCZ relative file name and SHA-256.

A reader must reject or mark unusable a TCM whose artifact set/hash does not match
the TCZs supplied to it.

Do not include absolute filesystem paths.

## Deploy option and publication behavior

Use existing `tc.Deploy`/`DeploySettings` command-line conventions.

Implement a clear opt-in mode whose final spelling is documented in the plan and
user-facing deploy help, conceptually:

```text
NONE   -> current behavior, no TCM
AOT    -> emit semantic TCM v1
```

Do not implement raw-class FULL mode in this milestone.

Normal SDK builds/deploys must remain default `NONE` unless a specific test/task
requests TCM.

Metadata is accumulated during conversion but the `.tcm` file is finalized only
after TCZ output is complete and hashes can be calculated.

Write:

```text
<name>.tcm.tmp
```

then atomically rename to `<name>.tcm`.

On explicit TCM request, writer failure is a deploy failure with a precise
diagnostic. Never leave a stale sidecar from a previous successful deploy; remove
only the matching temporary/stale TCM owned by the current explicit output path,
without touching unrelated files.

## Determinism and compatibility tests

Add writer/reader round-trip tests.

Required properties:

```text
same inputs -> byte-identical TCM
different TCZ hash -> reader detects mismatch
unknown skippable section -> reader continues
truncated section -> precise error
unsupported required major version -> precise error
```

Add a small semantic fixture proving facts that TCZ alone loses:

1. Float method:
   - source parameter type `F`;
   - lowered TC type `D/reg64`.

2. Interface invocation:
   - source invoke kind `INTERFACE`;
   - lowered opcode `CALL_normal`.

3. Inherited-owner invocation:
   - symbolic owner differs from resolved declaration owner.

4. 4D/replacement:
   - original and effective identities both present;
   - replacement kind is explicit.

5. Lambda:
   - generated adapter points back to the original lambda site/implementation.

6. StackMap:
   - source frame retains `FLOAT` distinct from `DOUBLE`.

7. Origin:
   - known Java PC maps to expected final TC slot range.

## Prove TCZ non-interference

This is a hard acceptance contract.

For a deterministic input:

```text
A = deploy normally
B = deploy with TCM enabled
```

Assert:

```text
SHA256(A.tcz) == SHA256(B.tcz)
```

and, for split artifacts, every corresponding TCZ hash matches.

If enabling the collector changes TCZ order, constant-pool ordering, generated
class order, code words, or compression output, treat it as a bug. Metadata
collection must be observational.

Prefer a regression test/task that performs both deploys in isolated temporary
directories.

## TCM documentation

Create a concise architecture document, for example:

```text
docs/architecture/bytecode/tcm-compilation-metadata.md
```

Keep it below 20 KiB/600 lines.

Document:

- TCZ remains canonical;
- purpose of TCM;
- opt-in mode;
- file naming;
- v1 header/sections;
- artifact hash binding;
- source facts preserved;
- deterministic/forward-compatible rules;
- privacy note that TCM contains class/member identities and is not anonymous
  telemetry;
- no runtime/AOT consumer is required in this milestone.

Do not duplicate complete Java class-file or TCZ specifications.

## Milestone 4 closure validation: emitted sidecar

Only after writer, reader, command option, docs, and deterministic tests are
complete:

```bash
cd TotalCrossSDK
./gradlew-agent test --tests <tcm-focused-tests>
./gradlew-agent dist -x test
./gradlew-agent deployModernJavaFeatureSmoke --warning-mode=none --console=plain
```

Run the aggregate smoke once without TCM and once with TCM in isolated output
directories, then compare TCZ hashes.

Inspect the emitted TCM using the new reader/test tool and assert its artifact
manifest matches the generated TCZ.

As the final step, run the generated macOS `FeatureSmokeApp` from the TCM-enabled
deploy artifact set. TCM must not change execution; acceptance is exit 0 and no
`[FAIL]` lines.

No TCIR/JIT/AOT platform matrix is required because no runtime consumer changed.

## File-Size and Commit Rules

Every new metadata class, parser, writer, reader, test, and document must remain
<=20 KiB and around 600 lines. Split model/encoding/decoding responsibilities
before they exceed the limit.

Do not split existing `J2TC.java`, `Bytecode2TCCode.java`, or other large legacy
files just to satisfy the new-file policy.

Suggested logical commits:

```text
feat(compiler): preserve source compilation metadata
feat(compiler): parse stack map metadata for tcm
feat(deploy): emit optional tcm metadata sidecar
test(deploy): prove tcm preserves tcz output
docs(compiler): document tcm metadata contract
```

Combine or rename when a different boundary is more logical. Do not create
artificial commits solely to match this list.

Push/PR/release are not authorized.

## Token-Efficient Execution

The parent state file is the resume source.

During implementation:

- do not reread the complete Java-to-TC architecture document after the required
  interfaces are recorded in state;
- inspect only the parser/lowering/helper being changed;
- use generated minimal fixtures instead of repeatedly building the whole SDK;
- do not run deploy/native smoke until milestone closure;
- keep binary dumps and full TCM hex output in ignored artifacts, not in chat or
  the plan;
- compare deterministic files by hashes and focused decoder summaries rather than
  dumping them.

If a round-trip test fails, inspect the first differing section and offset, not
the entire file.

## Idempotence and Recovery

TCM writer output is replaceable and deterministic.

Interrupted `.tcm.tmp` files may be removed/replaced on retry only when they
belong to the current output target.

The collector must reset all deploy-scoped state between deploy invocations so
tests cannot leak metadata across cases.

A failed TCM request must not corrupt or delete successfully generated TCZ files.
It may report the deploy operation as failed because the explicitly requested
sidecar contract was not satisfied.

## Acceptance Summary

This subplan is complete when TCM v1 has documented stable sections and a
reader/writer, emission is explicit opt-in/default-off, every sidecar is hash-bound
to its TCZ set, TCZ bytes are identical with metadata off/on, semantic fixtures
prove the previously lost facts are readable from TCM, and aggregate deploy plus
native macOS smoke pass with TCM enabled.
