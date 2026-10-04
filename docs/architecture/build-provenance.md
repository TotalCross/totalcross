<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Build provenance and artifact identity

TotalCross distinguishes source provenance, build identity, artifact integrity,
and compatibility. These concepts must not be collapsed into the product
version or the TCZ format version.

## Source identity

A source identity records:

- repository;
- Git commit for traceability;
- Git tree representing the exact source contents;
- whether the worktree was dirty when the identity was computed.

For a dirty worktree, `scripts/provenance/build-identity.py` creates a temporary
Git index and computes a tree from the current tracked and untracked,
non-ignored contents without modifying the developer's real index.

The commit is descriptive provenance. The source tree is the content identity.

## TotalCross Build ID

Build Identity version 1 is the SHA-256 of the canonical JSON object containing:

- `identityVersion = 1`;
- normalized repository identity;
- source tree.

The commit is intentionally not part of the Build ID. Two commits with exactly
the same source tree therefore have the same Build ID.

The Build ID is embedded into the SDK JAR, converter-produced TCZ files, the
native VM, and the desktop Launcher.

`TCZ_VERSION` remains a container-format version and is not a build identity.

## Embedded representations

The SDK JAR stores its identity in `META-INF/MANIFEST.MF`.

Converter-produced TCZ files contain the reserved resource:

`META-INF/totalcross-build.properties`

Native VM and Launcher binaries contain stable textual markers for the Build ID
and source tree. These markers are intended for build diagnostics and artifact
coherence checks; they are not a replacement for cryptographic artifact hashes.

## Validation

Use:

```bash
python3 scripts/provenance/artifact-identity.py \
  <sdk.jar> <application.tcz> <libtcvm> <Launcher>
```

The command fails if any artifact lacks an embedded identity or if the supplied
artifacts report different Build IDs or source trees.

Official CI artifacts also carry `build-provenance.json`. The external
provenance record remains the integrity/attestation layer and may record
artifact hashes separately from embedded identity.

## Compatibility

Equal Build IDs mean that artifacts were produced from the same source identity.
They do not define the long-term compatibility policy between independently
built artifacts.

The canonical compatibility registry is
`config/artifact-compatibility.properties` and currently defines:

- `tczFormat`: the existing TCZ container format version; it must remain equal
  to `TCZ_VERSION` in both Java and native readers;
- `converterAbi`: the epoch for the converter/TCZ semantic contract;
- `runtimeAbi`: the epoch required between converted artifacts/SDK and the
  native VM/Launcher.

Increment an ABI epoch only when a change intentionally makes artifacts across
that boundary incompatible. Ordinary source changes and bug fixes do not bump
an epoch.

Strict smoke validation uses the default artifact validator mode and requires
one Build ID. To diagnose or intentionally combine independently produced but
compatible artifacts, use:

```bash
python3 scripts/provenance/artifact-identity.py --compatibility-only \\
  <sdk.jar> <application.tcz> <libtcvm> <Launcher>
```

Compatibility-only mode permits different Build IDs but still requires aligned
runtime ABI epochs, converter ABI epochs where applicable, and TCZ format
versions. It must not be used to claim same-build provenance.
