<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Harden the J2TC/TCM Boundary

## Baseline

At revision `441c5785`, TCM was default-off only at publication. On the fixed
aggregate modern-Java conversion, both `NONE` and `AOT` built metadata for 87
classes, 306 methods, 2,767 bytecode/origin sites, 681 calls, 73 synthetic
lowerings, and 108 StackMap frames. This structural equality is the primary
baseline evidence that ordinary deploys still paid the collection cost.

Three warmups and ten samples gave median in-process deploy times of 3.604 s for
`NONE` and 3.587 s for `AOT`. These timings characterize only this workload and
environment; they do not establish a universal performance difference. The
baseline TCM v1 fixture SHA-256 is
`ee07c01ddcf503044c58ac702ddf1e750c55212f919e59f0d73f51478938b965`.

Hardening implementation has not started at this checkpoint, so no delivered
boundary improvement is claimed yet.

## Disabled path and origin finalization

TCM `NONE` is now a structural no-op. One conversion-scoped decision selects a
singleton disabled capture, while normal converter state—including
`Class.forName` discovery—continues independently. The class parser receives a
semantic capability and skips StackMap bytes without materializing frames when
metadata is unnecessary. Direct parser tests can still request and diagnose
StackMap metadata explicitly.

AOT origin finalization now performs one ordered scan of final lowered
instructions and one ordered emission pass over source sites, replacing the
former scan of every instruction for every Java bytecode. Empty source origins,
allocations, calls, dynamic lowerings, branch-promoted origins, and TC slot
ranges remain represented as before.

On the fixed workload, `NONE` metadata counts fell from the baseline values to
zero; AOT counts, the v1 fixture hash, and TCZ SHA-256 stayed unchanged. The
bounded repeat timing medians were 3.552 s for `NONE` and 3.636 s for AOT,
within 5% of their respective baselines. Timing remains supporting evidence for
this workload only.

## Stable v1 bytes, artifacts, and publication

TCM v1 numeric meanings no longer depend on Java enum order. Every native,
invoke, and synthetic value has a permanent documented wire code, and the reader
maps by code with explicit unknown-code diagnostics. The format stays at v1.0;
the preserved pre-change aggregate fixture remains readable and newly emitted
bytes retain SHA-256
`ee07c01ddcf503044c58ac702ddf1e750c55212f919e59f0d73f51478938b965`.

A neutral artifact component now derives sidecar names, preserves supplied TCZ
order, hashes each TCZ through a bounded 16 KiB stream, and validates manifest
count, names, order, and hashes. The reader no longer depends on writer internals.

Publication no longer deletes the previous sidecar. It writes the sibling
temporary completely, attempts atomic replacement, and falls back to a regular
replacement move only when atomic moves are unsupported. Injected failures
before replacement preserve the prior sidecar and remove the owned temporary.
This is an ordering guarantee, not an fsync or crash-durability guarantee.

## Canonical converter semantics

Java-to-TC descriptor lowering now has one production API shared by J2TC and
TCM capture. Source `F` remains visible as `F` in metadata while its lowered
form remains the converter's TC double type; primitive arrays retain their
existing distinct component encoding. Object and 4D normalization likewise
follow the constant-pool rules instead of a collector-local approximation.

Declaration-owner capture and deploy validation now use one resolver. Parsed
program classes and their known superclasses/interfaces are authoritative for
application methods. Supported Java APIs are checked only through
TotalCross-owned `totalcross`/`jdkcompat` classes plus explicit canonical
mappings, including `java.util.Properties.put` declaring through
`java.util.Hashtable`. The resolver never loads host `java.*` classes, preserves
the bytecode symbolic owner, refuses superclass constructor guesses, and leaves
unknown declarations unresolved.

## Final integration

The completed boundary passed the converter metadata/modern-Java suite, SDK
distribution, and aggregate deploy. Distribution caught a subtle compatibility
case: TotalCross-owned 4D classes can express their inheritance with host-named
Java or Javax types. Resolution now follows those names only into repository-owned
mapped classes; it still never treats host methods as authoritative.

On the final isolated conversion, `NONE` recorded zero metadata objects and
`AOT` retained 87 classes, 306 methods, 681 calls, 2,767 origins, and 108 frames.
Both modes emitted TCZ SHA-256
`21f48888a0817eefe94cbc0e51ec4a775edcf8f6a3e20c6b9aec0b3df2be081c`.
The production inspector accepted the semantic TCM, whose expected content
changes now hash to
`55b189a03adf9e28c919a49743f23b956bba34129a5330d719456329fbd1f10c`.
The aggregate macOS VM smoke finished with 97 passes and no failures.

This work establishes a cheaper, safer metadata boundary. It does not implement
or benchmark field optimization, HIR, an AOT backend, or ProGuard integration;
those remain separate decisions for future plans.
