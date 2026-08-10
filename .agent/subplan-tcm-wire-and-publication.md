<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Subplan: stabilize TCM v1 wire encoding and publication

Parent: `.agent/exec-plan-harden-tcm-j2tc-boundary.md`.

Read this file only while Milestone 2 is active.

## Goal

Make the existing TCM v1 binary contract robust against Java enum evolution,
preserve the previous valid sidecar until replacement succeeds, and stream TCZ
artifact hashes.

Do not change established v1 numeric values or TCZ.

## Explicit wire codes

Replace `enum.ordinal()` serialization for all on-wire enum-like values.

Current documented values must remain stable, including:

```text
NativeKind
  NONE=0
  JAVA_NATIVE=1
  REPLACED_ON_DEPLOY=2

InvokeKind
  STATIC=0
  SPECIAL=1
  INTERFACE=2
  VIRTUAL=3
  DYNAMIC_LAMBDA=4
  DYNAMIC_STRING_CONCAT=5
  DYNAMIC_RECORD=6

SyntheticKind
  LAMBDA=0
  STRING_CONCAT=1
  RECORD_OBJECT_METHOD=2
```

Give each value an explicit `wireCode` or equivalent stable mapping. Reader
decoding must search/map by code, not use `values()[code]`.

Do not change enum order merely to prove the mapping. Tests should prove the wire
code independently.

Keep TCM format major/minor unchanged because compatible bytes remain compatible.

## Shared streaming artifact support

Extract relative artifact validation and SHA-256 calculation from writer-specific
code into a neutral utility used by:

```text
TcmWriter publication
TcmReader artifact validation
tests / inspector where appropriate
```

Use `InputStream`/bounded buffer updates to `MessageDigest`; do not call
`Files.readAllBytes` for TCZ hashing.

Preserve:

- ordered artifact list;
- relative file-name-only contract;
- 32-byte SHA-256;
- exact mismatch diagnostics;
- split-TCZ manifest behavior.

Avoid making the reader depend on writer internals.

## Publication ordering

Current behavior deletes the destination sidecar before replacement. Change to:

```text
encode complete sidecar bytes
remove stale owned temporary file if needed
write sibling <name>.tcm.tmp completely
attempt move(tmp, sidecar, ATOMIC_MOVE, REPLACE_EXISTING)
if AtomicMoveNotSupportedException:
    move(tmp, sidecar, REPLACE_EXISTING)
on failure:
    delete tmp only
    preserve previous sidecar whenever replacement did not succeed
```

Do not delete the existing sidecar before the move.

Document exactly what is guaranteed:

- atomic replacement when supported by the filesystem;
- replacement fallback otherwise;
- failures before replacement preserve the previous valid TCM;
- handled failures do not leave the owned temporary file.

Do not claim fsync/crash-durability semantics that are not implemented.

If deterministic failure testing requires a small package-private publisher or
filesystem operation seam, add the smallest cohesive helper. Avoid permission-
based tests that behave differently on macOS/Linux/CI.

## Tests to write during implementation

Write but do not run:

- stable wire codes for every enum-like value;
- reader rejects unknown enum wire value precisely;
- Milestone 0 v1 fixture remains readable;
- deterministic writer output remains deterministic;
- optional/required section behavior remains unchanged;
- streaming hash equals a known SHA-256;
- artifact name/order/hash mismatch behavior;
- split TCZ manifest validation;
- replacing an existing TCM succeeds;
- injected pre-replacement failure preserves prior sidecar bytes;
- handled failure removes temporary file;
- `NONE`/`AOT` still produce identical TCZ bytes.

Where possible, prove newly written TCM for the Milestone 0 fixture retains the
same wire representation. If build identity or intentionally unrelated metadata
prevents whole-file identity, compare the affected sections/decoded values rather
than weakening the compatibility claim.

## Final milestone validation

Testing is the final implementation action.

Run focused TCM reader/writer/artifact/publication tests and the smallest deploy
fixture required for TCZ byte identity.

Acceptance:

```text
no ordinal() controls TCM wire values
old v1 fixture readable
documented wire values unchanged
artifact hashing uses bounded streaming
old sidecar not deleted before replacement
failure path preserves prior valid sidecar
no handled stale .tmp
TCZ format and bytes unaffected
```

After validation, create logical commits, for example:

```text
fix(compiler): stabilize tcm wire codes
fix(deploy): preserve valid tcm during replacement
perf(deploy): stream tcm artifact hashes
```

Combine when code boundaries are inseparable. Do not push.

## Documentation

Update `docs/architecture/bytecode/tcm-compilation-metadata.md` only after the
implementation is known.

Document explicit wire-code stability and the exact atomic/fallback publication
guarantee. Keep privacy and TCZ-authority language unchanged.

## Risks

`ATOMIC_MOVE` support is filesystem-specific. Do not convert the fallback into an
unsupported guarantee.

A new neutral artifact utility must preserve manifest ordering; do not sort files
unless the existing contract sorts them.

Changing enum serialization must not accidentally change existing v1 bytes.

## Recovery

A failed run may delete its own `.tmp` only. Never remove a previous valid sidecar
as cleanup.

If final tests fail, record the first stable diagnostic and rerun only this
milestone after correction.

Do not start Milestone 3 until compatibility and failure-path tests pass.
