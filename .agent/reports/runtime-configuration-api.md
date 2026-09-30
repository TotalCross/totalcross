<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Runtime configuration API final report

## Summary

This change adds a declarative foundation for selecting future runtime
configuration rules from typed environment facts. It adds selector models,
CLASS-retained annotations, conversion-time parsing, private TCZ metadata, and
startup resolution. It does not add concrete image, rendering, or other option
values.

## Public API

- `Platform`: `WINDOWS`, `MACOS`, `LINUX`, `ANDROID`, `IOS`.
- `RuntimeFamily`: `DESKTOP`, `MOBILE`, `EMBEDDED`.
- `GraphicsBackend`: `RASTER`, `GPU`.
- `Architecture`: `X86`, `X86_64`, `ARM32`, `ARM64`.
- `RuntimeEnvironment`, `RuntimeSelector`, `RuntimeConfiguration`,
  `RuntimeRule`, `RuntimeRules`, `RuntimeWhen`, and `RuntimeCondition`.
- The startup bridge and codec, resolver, and resolved result are internal
  implementation components. Artifact boundary checks keep them out of both
  `totalcross-api` and the aggregate `dist/totalcross-sdk.jar`;
  `ResolvedRuntimeConfiguration` is package-private.

The public API declares selectors only. It has no numeric masks, wire IDs,
option keys, or concrete values. Fact enums have no `UNKNOWN` or `ANY`
sentinels. `RuntimeEnvironment` returns `null` for each unavailable fact;
`graphicsBackend()` remains `null` until the backend is finalized, as reported
by `isGraphicsBackendFinalized()`.

The internal classes remain in `totalcross-runtime-java.jar` under `dist/libs`
and as a runtime-scoped Maven dependency. SDK tool tasks include that jar on
their runtime classpaths. The aggregate SDK manifest omits it because Java
compilers follow manifest `Class-Path` entries.

## Final architecture

Selectors use immutable disjunctive normal form over platform, runtime family,
graphics backend, and architecture. A selector that constrains a `null` fact
does not match; `RuntimeSelector.any()` can match without constraining any
dimension. The internal resolver matches rules by specificity and detects
equal-specificity conflicts. Resolved environment and match-count data stay
internal until typed setting descriptors are introduced.

Native platform and architecture facts come from `TC_OS_*` and `TC_ARCH_*`;
architecture classification is available in both configured CMake and
fallback builds. Private native transport code zero maps to `null`. The
graphics fact comes from the selected renderer after graphics initialization.
The simulator uses host properties and finalizes its raster backend after its
AWT render surface starts. Unsupported host values and unprovable family
classifications map to `null`.

## Deploy-time processing

The converter parses annotations on the application entry class with ASM,
including javac repeatable-annotation containers. The annotations use CLASS
retention, so runtime reflection is not required. Invalid declarations fail
conversion with class and rule context.

Deployment builds correlated platform, family, and architecture target tuples.
It removes impossible selector alternatives and folds dimensions fixed across
all selected targets while preserving selector specificity. `linux_arm` fixes
ARM32; other or mixed targets keep architecture predicates. Applet targets
leave platform `null`, and deployment does not guess a graphics backend.

Declared selectors are stored in the reserved `tc.runtimeconfig` TCZ resource
using metadata version 2. Runtime decoding also supports version 1. Applications
with declarations do not serialize unavailable facts: stable selector value
IDs start at one, and zero or unsupported IDs are rejected. Valid version 1
payloads remain readable; metadata remains at version 2. Applications without
declarations do not receive this resource.

## Startup resolution

Native startup reads optional metadata after runtime settings and graphics
initialization and before loading the application main class. The simulator
reads the application class resource before initializing that class. Both use
the shared resolver. Simulator rules that depend on graphics remain pending
until the AWT surface finalizes the backend; the final environment is resolved
at startup.

When metadata is absent, startup produces no result and does not resolve
selectors. The simulator parses class metadata without initializing the
application class.

## Validation

- `./gradlew-agent test --rerun-tasks --tests 'totalcross.sys.runtime.*Test' --tests 'tc.simulator.RuntimeConfigurationSimulatorTest' --console=plain --warning-mode=summary` passed: 8 suites, 42 tests, 0 failures, 0 errors, 0 skipped.
- `./gradlew-agent artifactContentTest dist --rerun-tasks -x test --console=plain --warning-mode=summary`
  passed: 11 artifact boundary checks, including public API compilation and rejected
  startup/codec imports against the delivered `dist/totalcross-sdk.jar` with
  `dist/libs` beside it. SDK distribution completed successfully.
- `./gradlew-agent runRuntimeConfigurationMacOSSmokes -x test` passed with the
  rebuilt macOS ARM64 VM and launcher. The annotated fixture
  observed `MACOS`, `DESKTOP`, `ARM64`, `RASTER`, and one matching rule; the
  no-metadata fixture confirmed the resource and resolved configuration were
  absent while normal startup completed.
- macOS ARM64 Release CMake build of `tcvm` and `Launcher` passed with the
  supported QR backend and pinned SQLite release; both outputs are ARM64 Mach-O.
- Focused copyright validation passed for changed files, and final
  `git diff --check origin/master...HEAD` passed. All added Java files remain
  below 20 KiB and 600 lines.
- Android, Windows, Linux, WinCE, and iOS builds were omitted as requested.

## Execution-cost properties

Annotation parsing happens during conversion or simulator startup. Selector
matching happens during application startup; the simulator revisits pending
backend-dependent rules when its backend becomes final. Native startup with no
metadata performs no selector resolution. No selector traversal, annotation
reflection, or configuration lookup was added to drawing, frame, image, or
other hot paths.

## Compatibility

Existing `Settings` fields, optimization masks, renderer behavior, and image
handling are unchanged. Applications without the annotations retain their
existing behavior. Runtime metadata version 1 remains readable, while new
metadata uses version 2 to preserve specificity after deploy-time folding.

## Known limitations

This foundation selects rules but does not apply concrete settings. Platform,
family, architecture, or graphics facts remain `null` when the target or runtime
cannot prove them. Backend-dependent simulator rules resolve after the AWT
surface starts.

## Deferred integration

Later feature work can add typed option categories and bind matched rules to
settings. Concrete image and rendering options, GPU selection, capability
selectors, and generalized runtime diagnostics remain deferred.
