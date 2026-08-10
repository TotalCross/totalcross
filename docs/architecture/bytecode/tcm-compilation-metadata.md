<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# TotalCross compilation metadata (TCM v1)

## Purpose and boundary

TCM is an optional local-build sidecar that preserves Java facts lost while J2TC
lowers a class file to TotalCross bytecode. TCZ remains the canonical executable
artifact and the VM does not require or read TCM. TCM v1 introduces no optimizer,
HIR, JIT, or AOT consumer.

Enable emission explicitly with:

```text
tc.Deploy <input> ... /tcm aot
```

The default mode is `NONE`, which emits no sidecar. The implemented opt-in mode
is `AOT`; its name describes the intended future consumer, not a compiler run by
the current deployer.

For a primary `MyApp.tcz`, the deployer publishes `MyApp.tcm` beside it. One TCM
covers the whole deploy invocation, including every split TCZ. Publication uses
`MyApp.tcm.tmp` followed by an atomic replacement where the filesystem supports
it. An explicitly requested sidecar failure fails that deploy operation without
modifying the completed TCZ files.

## Binary envelope

All integers are little-endian. The file header is:

| Field | Encoding | v1 value |
|---|---:|---:|
| magic | 4 bytes | `TCM1` |
| major version | `u16` | `1` |
| minor version | `u16` | `0` |
| flags | `u32` | `0` |
| section count | `u32` | number of following sections |

Each section has a `u16` encoded type, `u16` section version, `u32` payload
length, and exactly that many payload bytes. Bit `0x8000` in the encoded type
marks a required section; the remaining 15 bits are the section ID. A reader
skips an unknown optional section by its declared length and rejects an unknown
required section. It likewise skips an optional section version it does not
understand and rejects an unsupported required version.

TCM v1 writes section version `1` and these stable IDs:

| ID | Section | Required in v1 | Contents |
|---:|---|:---:|---|
| 1 | `STRING_TABLE` | yes | sorted, deduplicated UTF-8 strings |
| 2 | `ARTIFACT_MANIFEST` | yes | build identity and ordered TCZ hashes |
| 3 | `CLASSES` | no | identity, hierarchy, flags, source and modern class facts |
| 4 | `FIELDS` | no | descriptors, lowered types, constants and TC symbols |
| 5 | `METHODS` | no | descriptors, lowered types, flags and native kind |
| 6 | `CALL_SITES` | no | source invoke facts and final TC call ranges |
| 7 | `ORIGIN_MAP` | no | Java PC/opcode to final TC slot range |
| 8 | `ALLOCATION_AND_SYNTHETIC_ORIGINS` | no | allocation types and lowering origins |
| 9 | `DYNAMIC_ACCESS` | no | known reflection roots and unresolved-lookup marker |
| 10 | `TYPE_FRAMES` | no | symbolic StackMap locals and operand stacks |

The string-table payload starts with a `u32` count. Each string is a `u32` byte
length followed by UTF-8 bytes. Other sections use `i32` string indexes;
`-1` represents null. Record collections start with a `u32` count. Class and
method references are zero-based indexes into their deterministic section order.

The manifest starts with the SDK/deployer build-identity string index and an
artifact count. Every artifact record contains a relative file-name string index
and exactly 32 SHA-256 bytes. Absolute paths are forbidden. The reader validates
the ordered name/hash set supplied by a consumer and rejects any mismatch.

Native kinds are encoded as `NONE=0`, `JAVA_NATIVE=1`, and
`REPLACED_ON_DEPLOY=2`. Invoke kinds are `STATIC=0`, `SPECIAL=1`, `INTERFACE=2`,
`VIRTUAL=3`, `DYNAMIC_LAMBDA=4`, `DYNAMIC_STRING_CONCAT=5`, and
`DYNAMIC_RECORD=6`. Synthetic kinds are `LAMBDA=0`, `STRING_CONCAT=1`, and
`RECORD_OBJECT_METHOD=2`. Verification kinds are string values so future readers
can diagnose an unfamiliar symbolic type precisely.

## Preserved facts

TCM distinguishes original Java identity from effective TotalCross identity,
source descriptors from lowered TC types, and source invocation kind from the
lowered call opcode. It retains raw access flags, hierarchy, source/signature,
nest, record and permitted-subclass facts; explicit Java-native versus
deploy-replaced methods; symbolic and resolved call owners; Java-PC-to-TC-slot
origins; exact allocations; known and unresolved dynamic class access; lambda,
string-concat and record lowering origins; and symbolic StackMap frames with
`FLOAT` distinct from `DOUBLE`.

TCM stores facts, not version-specific optimizer decisions. It contains no
`canInline`, liveness, predicted receiver, or precomputed call graph fields. It
also does not embed raw class files.

## Determinism and non-interference

The writer sorts its deduplicated string table, uses stable deploy/class/member
order, emits fixed section order, and hashes finalized TCZ files. Repeating the
same deploy with the same build identity produces identical TCM bytes.

Metadata is collected through converter-only structures. Origin tags never enter
TCCode serialization, and enabling `/tcm aot` does not alter TCZ constant-pool
order, entries, compression, or bytes. A sidecar is usable only with the exact
artifact set recorded in its manifest.

`TcmInspector` provides a compact validation utility:

```text
java ... tc.tools.converter.metadata.TcmInspector MyApp.tcm MyApp.tcz [split.tcz ...]
```

It verifies the manifest hashes through the production reader and prints counts,
not application identities.

## Privacy

TCM contains application class, member, descriptor, hierarchy, and source-file
identities. It is not anonymous telemetry and the deployer does not upload it.
Treat it as a local build artifact with the same disclosure considerations as
debug symbols or unobfuscated class metadata.
