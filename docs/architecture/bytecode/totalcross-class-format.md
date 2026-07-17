<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# TotalCross class and container format

## Scope and sources of truth

This document describes the format written and read by the current code. It is not a proposal for a new format. The sources of truth are `TCClass.write`, `TCMethod.write`, `TCField.write`, `GlobalConstantPool.write`, `TCZ`, `tcclass.c`, and `tcclass.h`. All multibyte integers in the class record are little-endian.

There are two distinct layers:

1. the TCZ container, which has a version and compressed entries; and
2. the internal TotalCross class record, which has no magic number or format version of its own.

The currently observable version is `TCZ_VERSION = 200` in the outer container header.

## TCZ container

The `totalcross.util.zip.TCZ` writer emits, in order:

| Field | Size | Representation | Origin | Consumer | Notes |
|---|---:|---|---|---|---|
| `version` | 2 bytes | unsigned little-endian value | `TCZ.TCZ_VERSION` / TCZ writer | `tcz.c`/`tcz.h` | Current value is 200. |
| `attributes` | 2 bytes | unsigned little-endian flags | TCZ writer | TCZ loader | Global file attributes. |
| `baseOffset` | 4 bytes | unsigned little-endian offset | TCZ writer | TCZ loader | Start of entry data. |
| compressed header | variable | compressed count/offset/size/name arrays | TCZ writer | TCZ loader | Names use the short-string representation. |
| compressed entries | variable | independently compressed byte ranges | `J2TC`/global-pool writer | class/pool loaders | Classes and the global pool are separate entries. |

After decompression, the header contains the number of entries, `offsets[n + 1]`, uncompressed sizes, and short-string names. The container can read one entry without decompressing every other entry.

## Class record

The fixed header is 22 bytes and corresponds to `TClassInfoHeader`:

| Field | Size | Representation | Origin | Consumer | Notes |
|---|---:|---|---|---|---|
| `flags` | 2 bytes | packed `TCClassFlags` / `ClassFlags` | `TCClass.write` | `readClass` | Class modifiers and runtime classification. |
| `className` | 2 bytes | global class-pool index | `TCClass.className` | `readClass` | Resolves to the loaded class name. |
| `superClass` | 2 bytes | global class-pool index, zero when absent | `TCClass.superClass` | `loadClass` from `readClass` | `java.lang.Object` has no superclass. |
| `intfCount` | 2 bytes | unsigned count | interface array | `readClass` | Followed by this many 2-byte class indexes. |
| `i32InstanceCount` | 2 bytes | unsigned count | grouped converted fields | object-layout loader | 32-bit instance storage. |
| `objInstanceCount` | 2 bytes | unsigned count | grouped converted fields | object-layout and GC scanner | Managed-reference instance storage. |
| `v64InstanceCount` | 2 bytes | unsigned count | grouped converted fields | object-layout loader | 64-bit instance storage. |
| `i32StaticCount` | 2 bytes | unsigned count | grouped converted fields | static-field loader | 32-bit static storage. |
| `objStaticCount` | 2 bytes | unsigned count | grouped converted fields | static-field loader and GC roots | Reference static storage. |
| `v64StaticCount` | 2 bytes | unsigned count | grouped converted fields | static-field loader | 64-bit static storage. |
| `methodCount` | 2 bytes | unsigned count | converted method array | `readClass`/`readMethod` | Number of following method records. |

The header is followed by:

1. `intfCount` interface indexes as `uint16` values;
2. static fields grouped as `i32`, reference, then `v64`;
3. instance fields in the same group order; and
4. `methodCount` method records.

The loader computes object layout from those groups: 32-bit values first, references next, and 64-bit values last. `objOfs` and `v64Ofs` record the resulting offsets. This is effectively a memory ABI even though it has no explicit version number.

### Field record

Each field occupies 6 bytes:

| Field | Size | Representation | Origin | Consumer | Notes |
|---|---:|---|---|---|---|
| `flags` | 2 bytes | packed `TCFieldFlags` / `FieldFlags` | `TCField.write` | class field loader | Includes static/type/access properties used by the runtime. |
| `cpName` | 2 bytes | method/field-name pool index | converted Java field name | field resolver | Name is not stored inline. |
| `cpType` | 2 bytes | class/type pool index | converted Java descriptor | field loader/resolver | Descriptor is normalized to a TotalCross type identity. |

Initial values are not serialized in the field record. The converter materializes required static initialization in the static initializer method.

### Method record

The fixed method header is 16 bytes and corresponds to `TMethodInfoHeader`:

| Field | Size | Representation | Origin | Consumer | Notes |
|---|---:|---|---|---|---|
| `flags` | 2 bytes | packed `TCMethodFlags` / `MethodFlags` | `TCMethod.write` | `readMethod`/dispatch | Includes access, static, native, constructor, and related state. |
| `opcodeCount` | 2 bytes | unsigned slot count | generated `TCCode[]` | `readMethod` | Counts continuation slots; zero for native/abstract code paths as applicable. |
| `exceptionCount` | 2 bytes | unsigned count | converted handler array | `readMethod`/exception lookup | Runtime handlers, not a declared-throws list. |
| `lineNumberCount` | 2 bytes | unsigned count | converted line table | debug/stack-trace lookup | May be set to zero when delta encoding fails. |
| `iCount` | 1 byte | unsigned register count | register allocator | frame reservation | Compact instruction operands impose stricter practical limits. |
| `oCount` | 1 byte | unsigned register count | register allocator | frame reservation and GC roots | Includes managed reference homes. |
| `v64Count` | 1 byte | unsigned register count | register allocator | frame reservation | Shared by long/double. |
| `paramCount` | 1 byte | unsigned count | method signature | parameter mapping | Followed by this many 2-byte type indexes. |
| `cpName` | 2 bytes | method/field-name pool index | converted method name | lookup/dispatch | Signature uses name plus parameter types. |
| `cpReturn` | 2 bytes | class/type pool index | converted return descriptor | return dispatch | Zero represents `void`/constructor form. |

The header is followed by:

- `paramCount` type indexes as `uint16` values;
- `opcodeCount` 4-byte slots;
- `exceptionCount` 10-byte records; and
- the compressed line table.

Each handler contains five `uint16` values: inclusive start PC, end PC as used by the current search, handler PC, reference register receiving the exception, and exception-class index.

The Java `Exceptions` attribute (declared `throws`) is not a list in this runtime method record. Runtime exception metadata here is the Code attribute's handler table. Other Java attributes are either consumed by conversion/lowering, represented through flags/signature/line information, or omitted; the format is not an arbitrary Java attribute container.

For one source line, the table stores one `uint16`. For multiple lines it stores byte-sized PC deltas, the first line as `uint16`, and byte-sized line deltas. Marker 255 represents an all-ones delta sequence. The converter normalizes decreasing line numbers; if a delta cannot be represented, it discards line information for the method.

## Instruction slots

Every slot is exactly 4 bytes and is interpreted through the `TCode` union. The opcode always occupies the first 8 bits. Remaining bits use one of the layouts declared in `tcclass.h`:

| Layout | Fields after opcode | Typical use |
|---|---|---|
| `op` | 24 unused bits | no operand |
| `s24` | signed 24-bit immediate/displacement | jumps and immediate returns |
| `inc` | register 8, immediate 16 | increment |
| `params` | three bytes | call continuation |
| `dims` | three bytes | multi-array continuation |
| `reg_reg` | register 8, register 8 | moves and conversions |
| `field_reg` | symbol 12, receiver 6, register 6 | instance field |
| `static_reg` | register 8, symbol 16 | static field |
| `mtd` | method 12, receiver/first argument 6, return 6 | call |
| `reg_ar` | base 8, index 8, value 8 | array access |
| `reg_sym` | register 8, symbol 16 | constant/symbol |
| `s18_reg` | register 6, signed immediate 18 | short integer constant |
| `reg_reg_reg` | three 8-bit registers | ternary operation |
| `reg_reg_s12` | register 6, register 6, immediate 12 | compact operation/branch |
| `reg_s6_ar` | register 6, base 6, index 6, immediate 6 | fused array operation |
| `reg_reg_sym` | symbol 12, two 6-bit registers | operation with symbol |
| `reg_s6_desloc` | register 6, immediate 6, displacement 12 | branch against short constant |
| `reg_desloc` | register 8, displacement 16 | decrement and branch |
| `reg_sym_sdesloc` | symbol 12, register 6, displacement 6 | branch against symbol |
| `reg_arl_s12` | register 6, base 6, displacement 12 | compare with array length |
| `reg` | register 8 | test, return, monitor, indirect jump |
| `sym` | symbol 16 | constant return |
| `newarray` | symbol 12, destination 6, length/register/dimensions 6 | array allocation |
| `switch` | key 8, count 16 | multiple selection |
| `instanceof` | symbol 12, integer destination 6, object 6 | type test |

Most instructions occupy one slot. `CALL_normal`, `CALL_virtual`, `SWITCH`, and `NEWARRAY_multi` use continuation slots. Branch targets, handlers, line PCs, and `opcodeCount` are therefore measured in slots rather than bytes or logical instruction count.

## Global constant pool

The pool begins with nine `uint16` counts in this order: `i32`, `i64`, double, classes, static fields, instance fields, methods, method/field names and descriptors, and strings. Index zero is reserved.

Numeric values use their natural width. Field references store name indexes/deltas followed by class indexes. Methods store byte-sized parameter counts and tuples of class, name, and parameter indexes. Classes and names use short strings. String constants have short Latin-1, long Latin-1, and UTF-16 encodings. On load, the VM creates and locks constant-pool `String` objects, making them GC roots.

| Pool section | Size/representation | Origin | Consumer | Notes |
|---|---|---|---|---|
| nine counts | 18 bytes, nine `uint16` values | `GlobalConstantPool.write` | constant-pool loader | Index zero is reserved in each applicable table. |
| `i32` | 4 bytes per entry | Java/converter constants | arithmetic/move/branch handlers | Addressed by `cp->i32`. |
| `i64` | 8 bytes per entry | Java long constants | long handlers | Addressed by `cp->i64`. |
| double | 8 bytes per entry | Java float/double conversion | floating handlers | Java float identity is normalized. |
| static/instance field references | 2-byte name deltas/indexes plus 2-byte class indexes | converted field references | lazy field resolver | Runtime keeps separate bound-field caches. |
| method references | byte parameter counts, then 2-byte class/name/parameter indexes | converted calls | normal/virtual resolver | Compact call opcodes use 12-bit method symbols. |
| method/field names | 4-byte byte-size followed by short strings | converted identifiers | lookup/hash/diagnostics | Names are shared. |
| class/type names | 4-byte byte-size followed by short strings | converted descriptors/classes | loader/type checks | Primitive marker normalization occurs on write. |
| string constants | tagged Latin-1 or UTF-16 records | Java string constants | `cp->str` | Loaded objects are locked GC roots. |

Short strings use a one-byte length followed by bytes. String constants use a tag/length scheme: Latin-1 values below 254 use the short form; tag 254 introduces a `uint16` length and Latin-1 bytes for the writer's long-Latin path; tag 255 introduces the UTF-16 character representation. The exact boundary behavior is defined by `GlobalConstantPool.write` and must be fixture-tested before a new independent reader is accepted.

## Alignment, offsets, flags, and validation

`TCClass.write` and `TCMethod.write` append fields sequentially through `DataStreamLE`; there is no padding between serialized fields. The C loader defines the fixed headers as packed and reads exactly 22 and 16 bytes. Object-field alignment is a separate runtime-layout concern computed after loading and must not be confused with file padding.

Offsets inside TCZ are container-entry offsets. Inside a class record, the current loader advances sequentially rather than reading a section directory. Code and metadata PCs are slot indexes. Cross-references are constant-pool indexes rather than file pointers.

`TCClassFlags`, `TCMethodFlags`, and `TCFieldFlags` are serialized packed flag words and consumed by matching C bitfield structures. The existing source, not Java access-flag numbers alone, is the authority for their bit assignment. A portable independent reader should decode with explicit masks once those assignments are captured by byte fixtures.

Current conversion performs numerous range and compatibility checks, including class-file version checks, unsupported Java construct diagnostics, compact constant-pool/register limits, branch rewriting, and selected line-table constraints. The runtime loader also resolves superclasses/interfaces and can fail loading, but the analyzed path does not constitute a complete hostile-input verifier for every count, index, continuation slot, and target. TCIR decoding must add those checks before native compilation rather than treating successful loading as proof of structural safety.

Effective limits are not uniform. Instructions with 12-bit symbols limit addressable classes, methods, and instance fields to 4095 indexes in those layouts; static fields use a 16-bit field with additional converter limits. Format evolution should make every limit explicit and validate it before serialization.

## Invariants for future readers

- Validate lengths and indexes before producing pointers into code or pools.
- Never treat a continuation slot as an instruction start or branch target.
- Preserve field-group order because it defines object layout.
- Resolve symbols through stable identity; lazy-bound pointers belong only to the loaded process instance.
- Version any incompatible container change or introduce an explicit internal-record version.
- Keep the current 4-byte TotalCross bytecode as a compatible input; the first IR/JIT/AOT stage must not change it.

## Future decisions

The current code relies on the C compiler layout of `TCode` bitfields. Before treating this as a public portable specification, the project should decide whether to formalize encoding through masks and shifts independent of C bitfields. It must also decide how to version class records independently of TCZ and how to raise compact symbol limits without invalidating existing artifacts.
