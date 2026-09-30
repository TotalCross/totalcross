<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Runtime configuration API plan

## Context

Add an additive runtime configuration foundation to the TotalCross SDK. The SDK
already exposes target platform settings, initializes graphics during startup,
and provides converter, TCZ resource, native VM, and simulator paths suitable
for carrying and resolving declarative selectors. This plan depends only on
`origin/master` and existing repository facilities.

## Objectives

- Model platform, family, graphics backend, and architecture as independent
  typed runtime facts.
- Declare selectors with CLASS-retained annotations and parse them in the
  converter without runtime reflection.
- Prune statically impossible predicates and persist remaining selectors in
  compact, versioned application metadata.
- Resolve selectors once during startup and retain an immutable result.
- Keep domain-specific option changes internal until a real typed option
  category is introduced.

## Public API

Place `Platform`, `GraphicsBackend`, `RuntimeFamily`, and `Architecture` in
`totalcross.sys`; place `RuntimeEnvironment`, `RuntimeSelector`, and the
`RuntimeConfiguration`, `RuntimeRule`, `RuntimeRules`, `RuntimeWhen`, and
`RuntimeCondition` annotations in `totalcross.sys.runtime`.

The fact enums contain real values only. Return `null` when a fact cannot be
proven. `RuntimeSelector.any()` represents an unconstrained selector; a
constrained selector does not match when its actual fact is `null`. Keep wire
IDs and metadata details private.

## Configuration lifecycle

Parse application-class annotations during conversion, prune predicates only
when deployment targets prove their values, and persist the remaining rules as
an optional TCZ resource. At runtime, finalize available environment facts,
resolve metadata once before application main-class initialization, and retain
an immutable startup result. The simulator uses the same selector and resolver
semantics.

## Deploy-time processing

Use the converter's ASM support to parse repeatable annotations and fail closed
on malformed or unsupported declarations. Model correlated deployment targets
so only conditions impossible for every target are removed. Preserve selector
specificity when fixed predicates are folded. Keep backend predicates until
runtime, and use explicit versioned non-ordinal metadata IDs. Read valid
version-1 metadata and write version 2; reject zero and unsupported selector
IDs.

## Startup resolution

Derive native platform and architecture from `TC_OS_*` and `TC_ARCH_*` facts,
and the backend from the selected renderer after graphics initialization. Use
host properties in the simulator and finalize its raster backend after the AWT
surface starts. Keep rules pending while a required backend fact is unavailable.
Applications without metadata bypass selector resolution.

## Compatibility constraints

Preserve `Settings`, existing optimization masks, renderer behavior, image
handling, and default application startup. Do not add selector work, reflection,
configuration lookups, or related allocations to frame, draw, image, or other
hot paths. Keep startup, codec, and resolver implementation out of
application-facing SDK artifacts. Do not build Android, Windows, Linux, WinCE,
or iOS.

## Validation

Run focused environment, selector, parser, metadata, startup, and simulator
checks; artifact boundary validation; SDK distribution; and native macOS ARM64
Release build plus deployed annotated and no-metadata smokes. Validate source
headers, new-file sizes, changed-file whitespace, commit ownership, and the
rebuilt history. Record actual results in the final report.

## Out of scope

Do not add concrete image/rendering options, GPU selection or implementation,
capability selectors, mutable preferences, generalized runtime diagnostics,
Settings migration, or runtime JIT specialization.
