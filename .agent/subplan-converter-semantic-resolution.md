<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Subplan: centralize Java-to-TC semantic resolution

Parent: `.agent/exec-plan-harden-tcm-j2tc-boundary.md`.

Read this file only while Milestone 3 is active.

## Goal

Make J2TC validation and TCM capture use one Java-to-TC type mapping and one
method declaration-owner resolver, without using arbitrary host-JDK `java.*`
reflection as semantic authority.

Preserve existing supported TotalCross/4D device contracts.

## Canonical type mapping

Before editing, locate the production Java-to-TC conversion logic in
`GlobalConstantPool`, J2TC, and related helpers.

Extract or expose one stateless mapping API used by both production conversion and
`CompilationMetadataCollector`.

The canonical mapper must preserve current behavior for:

```text
V
Z/B/C/S/I
J
F
D
objects
arrays
```

In particular, Java `F` remains distinguishable in source metadata but lowers to
the same TC double representation used today.

Keep class-name/4D normalization separate unless it is already inseparable from
the canonical type contract. Do not move unrelated constant-pool state into a new
utility.

Delete the collector's duplicate type-lowering function after migration.

## Canonical declaration-owner resolver

Create one resolver contract that can return:

```text
symbolic owner
resolved declaration owner
effective/mapped device owner where useful
unresolved
```

The symbolic owner always comes from bytecode and must not be rewritten merely
because a declaration is inherited.

Constructors retain their symbolic owner and are never resolved through a
superclass constructor search.

Use repository/conversion-owned information in this order:

1. active program/input class hierarchy;
2. TotalCross-owned mapped/device API hierarchy;
3. explicit canonical mapping rules;
4. unresolved.

Do not use `Class.forName("java....")` or other host-JDK reflection to decide
whether a source Java method exists, which class declares it, or whether a deploy
call is accepted.

Reflection on TotalCross-owned `totalcross`/`jdkcompat` implementation classes may
remain as a bounded transitional mechanism if the SDK controls the classpath and
the result is mapped back through canonical rules. Prefer a parsed/model resolver
when achievable without creating a broad new classpath subsystem.

Do not build a general JDK/JMOD resolver in this plan.

### Program classes

For classes included in the active conversion, resolve methods from parsed
`JavaClass`/`JavaMethod` facts and their known hierarchy.

If a superclass/interface is not available in the conversion-owned model, return
unresolved rather than consulting host Java.

### Mapped Java APIs

For `java.*` APIs supported through `totalcross`/`jdkcompat` mappings, validate
against the TotalCross-owned device API model.

Preserve bytecode symbolic owner. When the mapped declaration can be converted
deterministically to a Java-facing declaration owner, expose that in TCM.

Example target behavior:

```text
symbolic owner: java/util/Properties
method: put(Object,Object)
resolved Java-facing declaration: java/util/Hashtable
lowered virtual semantics: unchanged
```

Do not weaken the canonical 4D replacement contract for ProGuard-created
constructors or generated names that remain intentionally unsupported.

### Shared consumers

Use the same resolver from:

```text
TCMethod device/API validation
CompilationMetadataCollector call-site capture
```

Validation still throws precise unsupported-device diagnostics when required.
Metadata capture may record unresolved declaration owner without failing a
conversion that validation otherwise accepts.

Remove duplicate host-JDK resolver helpers after migration.

## Tests to write during implementation

Write but do not run:

Type mapping:

- primitive matrix;
- `F` source type -> TC double lowered type;
- objects and arrays;
- return `V`;
- differential checks against production constant-pool/J2TC output.

Owner resolution:

- user/program inherited virtual method;
- mapped `java.util.Properties.put`;
- unresolved member negative case;
- constructor owner;
- inherited interface/default method relevant to current support;
- symbolic owner remains unchanged after declaration resolution;
- TCM resolved owner equals the shared resolver result;
- unsupported 4D replacement descriptor/name contracts still reject precisely.

Host independence:

- structural test/inspection that shared resolver contains no host `java.*`
  reflection fallback;
- if two JDK runtimes are already installed, execute the same focused resolver
  fixture under both only at milestone validation and compare results;
- if a second JDK is unavailable, record that limitation instead of installing or
  downloading one.

Do not add a broad external-classpath test matrix.

## Final milestone validation

Testing is the final implementation action.

Run the focused type/owner/metadata/modern-Java tests after all semantic extraction
is complete.

Acceptance:

```text
collector has no duplicate lowerType
TCMethod and TCM share declaration resolver
program inheritance resolves from converter-owned model
mapped Java API resolves from TotalCross-owned model
constructors retain symbolic owner
unresolved facts do not cause host-JDK guesses
unsupported 4D contracts remain unsupported
existing inherited-owner regression passes
```

Run the smallest distribution/deploy check needed only if focused tests expose a
behavior that cannot be proven without it; otherwise defer aggregate deploy/native
smoke to parent Milestone 4.

After validation, create logical commits, for example:

```text
refactor(compiler): centralize java type lowering
refactor(compiler): centralize method owner resolution
```

Use `fix` when observable behavior changes. Do not push.

## File boundaries

Potential small new responsibilities:

```text
tc/tools/converter/JavaTypeMapping.java
tc/tools/converter/JavaMethodResolver.java
```

Names may differ.

Every new source/test file stays <=20 KiB and about 600 lines. Do not split
existing oversized legacy files merely for size.

If code is substantially extracted from an older file, follow `AGENTS.md`
copyright-provenance rules.

## Risks

The deployer may not own complete source hierarchy information for every JDK API.
Unresolved is preferable to a host-dependent guess when TotalCross device
validation can still make the correct supported/unsupported decision.

Mapped device declaring classes may not have a one-to-one Java source owner.
Preserve symbolic owner and document unresolved declaration owner rather than
inventing a mapping.

Do not turn this milestone into a full classpath, verifier, or HIR project.

## Recovery

If final tests fail, stop the parent plan at Milestone 3, record the first stable
diagnostic, correct the resolver/mapping, and rerun only this milestone's final
validation.

Do not start final integration until the shared semantics are proven.
