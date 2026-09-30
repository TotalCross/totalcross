<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# P1 Image Runtime Configuration

## Summary

P1 adds typed Image storage requests to the existing RuntimeConfiguration
system. Selectors, target pruning, specificity, and conflicts continue to use
B's shared implementation. The runtime publishes one immutable Image policy
after environment facts are ready.

## Public API

The application API adds `ImageStorageProfile`, the repeatable CLASS-retained
`ImageRuntimeRule`/`ImageRuntimeRules` annotations, and
`RuntimeConfigurationReport.describe()`. Image rules require the entry class to
carry `@RuntimeConfiguration` and explicitly specify a storage profile.
Internal bridge, codec, startup, and policy classes are excluded from
application-facing artifacts.

## Final architecture

`RuntimeConfigurationFeatureBridge` adapts typed feature rules to B's shared
resolver, selector codec, and deployment pruning. Its sorted named-section
registry lets features contribute report sections without making the public
report depend on each feature package. Re-registering a section replaces its
provider.

The converter reads CLASS-retained annotations without initializing the app
class. It creates `tc.imageruntimeconfig` independently from
`tc.runtimeconfig`; deployment omits the Image resource when all rules are
pruned for the selected targets.

## Selector/deployment integration

The Image resource is a big-endian `TCIC` envelope: 32-bit signature, one-byte
version (`1`), 16-bit retained-rule count, then each rule's 32-bit selector
payload length, opaque B selector payload, and one-byte storage tag. Tags are
`0x01` for `STANDARD` and `0x02` for `COMPACT`. The decoder rejects unsupported
versions, malformed lengths, unknown tags, truncation, and trailing bytes.
The former temporary `TCIR` signature is rejected; no backward compatibility
is provided. Selector persistence and target pruning reuse B's existing codec
and rules.

## Requested vs effective policy

With no matching rule, requested and effective storage are both `STANDARD`. A
matching `COMPACT` rule records `COMPACT` as requested and resolves to effective
`STANDARD`, with reason `compact storage is not available in P1`. No specific
native pixel representation is promised.

The typed policy also carries future defaults: six enabled raster-core values
(zero-copy decode, opacity metadata, opaque write pixels, row readback,
direct-color materialization, and physical identity); disabled target-color
conversion, physical-variant cache, scroll raster reuse, and automatic image
preparation; and `LEGACY_PER_ENTRY_THREAD` prefetch. P1 records these defaults
without activating those behaviors. This is resolved policy only: a field is
operational only once its owning feature implementation consumes that policy.
The first five raster-core values belong to P2; `physicalIdentity` and both
raster-variant values belong to P3; COMPACT storage capability belongs to P4;
scroll reuse to P7; image preparation to P8; and prefetch to P9.

## Runtime configuration description

`RuntimeConfigurationReport.describe()` creates its Environment section from
`RuntimeEnvironment.current()` and appends registered sections in name order.
The report is diagnostic text and is not a parsing contract. The Image section
describes the resolved typed policy, including defaults reserved for future
consumers. Policy fields describe resolved runtime policy; a field is
operational only once its owning feature implementation consumes that policy.
A matching COMPACT request produces output in this form:

```text
Runtime configuration

Environment
  platform: MACOS
  family: DESKTOP
  architecture: ARM64
  graphicsBackend: RASTER

Image
  storage:
    requested: COMPACT
    effective: STANDARD
    reason: compact storage is not available in P1

  rasterCore:
    zeroCopyDecode: enabled
    opacityMetadata: enabled
    opaqueWritePixels: enabled
    rowReadback: enabled
    directColorMaterialization: enabled
    physicalIdentity: enabled

  rasterVariants:
    targetColorConversion: disabled
    physicalVariantCache: disabled

  scrollRasterReuse: disabled

  imagePreparation:
    automaticPreparation: disabled

  prefetchWorker: LEGACY_PER_ENTRY_THREAD
```

## Startup integration

Native startup initializes Image after B has established runtime environment
and graphics facts, and before loading the application entry class. The
simulator parses both annotation sets from the same class bytes before class
initialization, then resolves Image rules after the actual graphics backend is
finalized. Without Image metadata, startup registers the report section with
the default `STANDARD` policy and does not run selector resolution.

## Validation

- Focused post-correction SDK runtime, parser, deployment, simulator, and B
  regression tests: 46 passed, 0 failed, 0 errors, 0 skipped.
- `./gradlew-agent artifactContentTest`: 11 artifact-boundary tests passed;
  public API classes are present and internal Image/bridge classes are absent
  from application artifacts.
- Diagnostics-off `./gradlew-agent dist -x test`: passed.
- Diagnostics-on focused compilation/artifact checks and
  `./gradlew-agent -PruntimeDiagnostics=true dist -x test`: passed.
- macOS ARM64 Release builds of `tcvm` and `Launcher` passed with diagnostics
  off and on. The configure used the available QR code and SQLite release tags
  `qrcodegen-20250123-r2` and `sqlite3-3.32.3-r2`.
- Six deployed Image/B smokes passed: no-metadata defaults, COMPACT downgrade,
  specificity in both annotation orders, equal-specificity conflict, B's
  annotated configuration, and B's no-metadata startup.
- The diagnostics-enabled RuntimeDiagnostics deployed smoke passed with the
  matching diagnostics-enabled SDK and VM.
- Copyright validation passed for 34 changed files with no edits. Final
  `git diff --check origin/master...HEAD` passed.

The conflict smoke observed the shared resolver exception before application
entry loading. This VM startup-error path returns process status zero, so the
fixture checks for the expected exception text.

## Compatibility

P1 reuses B's matching and conflict semantics. The B deployment/simulator
coverage and macOS smokes passed. RuntimeDiagnostics remains independent of
configuration policy; the matching diagnostics-enabled build and F smoke
passed. No F-related runtime or artifact integration changed in this
correction, so that validation remains applicable. No mask compatibility layer
or numeric option IDs were introduced, and no image decode/render
implementation or hot path changed.

## Known limitations

`COMPACT` is request-only in P1 and always falls back to `STANDARD`. The
effective profile does not currently select a reduced backing representation.
The repository base already tracks unrelated files under `.agent/state`; they
were preserved, and this work added no task-specific state file.

## Deferred work

Future P2/P3/P4/P6/P7/P8/P9/P10 work consumes the typed internal policy when
those features are implemented. Android, Windows, Linux, WinCE, and iOS builds
were not run because this task's build restrictions prohibit them. Benchmarks
were deferred because P1 changes configuration paths only and does not change
the measured image hot path.
