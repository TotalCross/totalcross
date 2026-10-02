<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Expose typed production Image runtime options

## Purpose

Make Image storage, raster variant behavior, scroll raster reuse, and prefetch
worker selection available through typed `@ImageRuntimeRule` configuration.
The converter, deployed runtime, simulator, policy report, and production
consumers use one immutable resolved policy. Existing storage-only source rules
and version-1 deployed metadata remain compatible.

## Scope / non-goals

The public API consists of `RuntimeFeatureState`,
`ImagePrefetchWorkerMode`, `ImageStorageProfile.DEFAULT`, and the expanded
`ImageRuntimeRule`. The configurable properties are storage, target color
conversion, physical variant cache, scroll raster reuse, and prefetch worker.
Stable raster-core defaults remain enabled. Automatic preparation remains
disabled and non-configurable.

Do not expose numeric masks, native ABI changes, diagnostics IDs, cache
budgets, frame pacing controls, automatic preparation, or stable raster-core
toggles. P11 scheduling experiments remain diagnostic or test-only.

## Public API

`RuntimeFeatureState.DEFAULT` leaves a property unassigned in one rule;
`ENABLED` and `DISABLED` express explicit requests. `ImageStorageProfile.DEFAULT`
is a rule sentinel only. Resolved storage is always `STANDARD` or `COMPACT`.
`ImagePrefetchWorkerMode.DEFAULT` leaves the worker unassigned; its concrete
values select the legacy per-entry worker or semaphore process worker.

`ImageRuntimeRule.when` is required. Every option defaults to its sentinel, so
storage-only annotation source remains valid. A rule with all five properties
defaulted is invalid. Single and repeated rules can be used to express
independent property assignments.

## Rule composition

The converter produces `FeatureRule<ImageRuntimeOptions>`. The immutable rule
value carries only the five optional assignments. Startup resolves each
property independently by projecting explicit values into the existing
`RuntimeConfigurationFeatureBridge.resolveSingleSetting` machinery. Selector
specificity and ambiguity therefore follow the shared runtime resolver.

## Conflict semantics

A more-specific explicit assignment overrides a less-specific assignment for
its property. Equally specific assignments to different properties compose.
Equal-specificity duplicates with the same value are accepted; different
values for the same property produce a deterministic configuration error.
`DEFAULT` does not enter a property projection and cannot conflict. Unassigned
properties use production defaults.

## Resolved policy and consumers

`ImageRuntimePolicy` is the immutable startup snapshot. It records concrete
requested and effective storage with a downgrade reason, stable raster-core
defaults, raster variant options, scroll reuse, preparation policy, and the
selected prefetch worker. Compact capability handling retains the existing
best-effort behavior. Feature eligibility and safe fallback remain owned by
the relevant P3, P7, and P9 consumers.

Production startup supplies the resolved values to target-color conversion,
physical variant caching, `ScrollRasterReuse`, and
`ImagePreparationScheduler`. The semaphore process worker remains the existing
semaphore implementation. No consumer derives behavior from a test-only policy
injection path.

## Metadata v2 / v1 compatibility

`tc.imageruntimeconfig` version 2 stores each retained selector, a five-bit
presence mask, and explicit stable value tags. It serializes only explicit
assignments, never enum ordinals or `DEFAULT`. Decode rejects unsupported
versions, unknown presence bits or tags, empty rules, truncated input, and
trailing bytes.

Version 1 remains readable as a storage-only rule; newer properties remain
unassigned and resolve to their defaults.

## Deployment pruning

Pruning removes a rule only when its selector has no target in the deployment.
Every explicit property on a retained rule is preserved. If all rules are
pruned, runtime startup uses the no-rule defaults.

## Simulator parity

The simulator uses the same typed rule value and property resolver as deployed
startup. It waits for graphics-backend finalization before resolving rules.
For identical annotation and environment facts, both paths produce the same
policy.

## Defaults

With no assignment, requested and effective storage are `STANDARD`; target
color conversion, physical variant cache, scroll raster reuse, and automatic
preparation are disabled; the worker is `LEGACY_PER_ENTRY_THREAD`; and all six
stable raster-core defaults are enabled. A version-1 storage-only resource
preserves these values for newer properties.

## Validation strategy

Use focused SDK tests for the public API, parser, resolver, metadata codec,
startup, deployment pruning, simulator parity, artifact boundaries, and
production consumers. Cover sparse version-2 rules, version-1 decoding,
malformed metadata, independent composition, same-property conflicts, defaults,
and configured feature paths. Run SDK validation with diagnostics both off and
on.

Build the macOS ARM64 Release VM and launcher and exercise deployed configuration
smokes for storage, raster variants, scroll reuse, preparation workers, and
affected regressions. Use the legacy software renderer at unit scale when
validating the native row-move path. Local platform builds are limited to the
SDK and macOS native targets; enabled platform lanes are validated in Merge
Flow. Use `git diff --check` for source and documentation changes. Performance
benchmarks belong to P12, not this configuration change.

## Decision log

- Represent partial assignments in one immutable rule value and project each
  property through the generic resolver. This preserves shared specificity and
  conflict semantics while allowing equally specific rules to compose across
  properties.
- Keep `DEFAULT` at the annotation and rule layer, then resolve concrete values
  before constructing the runtime policy. This prevents sentinels from leaking
  into operational consumers.
- Encode version 2 with a presence mask and explicit stable tags while retaining
  a dedicated version-1 decoding path. Sparse rules remain compatible and wire
  values do not depend on Java enum ordinals.
- Keep the runtime configuration report human-readable and non-stable for
  machine parsing. Report effective values and matched-rule provenance for
  diagnostics.

## Risks / limitations

The option path crosses conversion, deployment metadata, runtime startup,
simulator resolution, and feature consumers; each path must use the same policy
snapshot. Compact storage can downgrade to standard storage when native compact
backing is unavailable. Scroll raster reuse is eligible only when its renderer,
surface, scale, viewport, and damage checks permit a safe row move; other
cases use the existing repaint fallback.

## P12 boundary

P12 benchmarks use named typed production configuration and record the resolved
runtime report. Raw masks and test-only policy injection are not benchmark
configuration.

## Planned logical commits

1. `docs(image): plan production runtime options` — durable scope and
   architecture.
2. `feat(runtime): add typed image option values` — public API, immutable rule
   values, and parser validation.
3. `feat(image): resolve runtime options per property` — shared resolver,
   concrete policy, consumers, report, and simulator parity.
4. `feat(runtime): encode image runtime options metadata` — sparse version-2
   codec, version-1 compatibility, pruning, and converter consumption.
5. `test(image): validate production runtime options` — resolver, policy,
   production consumer, defaults, and diagnostics coverage.
6. `test(image): validate deployed option integration` — deployed metadata,
   artifact boundary, production-configured smokes, and historical mapping.
7. `docs(image): report production runtime options` — factual production report.
