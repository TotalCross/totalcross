<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# RuntimeDiagnostics foundation: implementation report

## Architecture

The public API consists of `RuntimeDiagnostics` and immutable
`RuntimeDiagnosticSnapshot`. Callers can enable the `RUNTIME` observation
domain and request aggregate values by domain and kind. The API does not expose
metric IDs, keys, descriptors, reset masks, or native protocol values.

Internal descriptors use private stable IDs and identify metric domain, kind,
and owner. Java-owned observations are read directly in Java. Native-owned
observations use one generic bridge with batched reads; a private single read
and reset operation support validation. The only catalog in this change is a
small synthetic `RUNTIME` foundation catalog.

Counters and timers are cumulative and support deltas. Gauges are absolute in
snapshots and deltas. Selective reset clears cumulative observations and
preserves live gauges. Deltas reject snapshots from different metric sets or
reset epochs. Snapshots copy their arrays and do not expose the internal epoch.

SDK diagnostics are opt-in with `-PruntimeDiagnostics=true`. The default SDK
variant includes neither optional metric storage nor native declarations. In
the enabled variant, the runtime domain gate precedes lazy diagnostics storage,
metric-only synchronization, allocation, and native calls. No diagnostic clock
reads were added. CMake defaults `TC_ENABLE_RUNTIME_DIAGNOSTICS` to `OFF`;
that configuration omits the native bridge source and registrations. The
RuntimeEnvironment fact methods remain ordinary native registrations; only the
five RuntimeDiagnostics registrations are conditional.

## Validation

Validation was performed on macOS. Focused tests cover public domain/kind
metadata, Java/native ownership, mixed snapshots, immutable data, batch and
single native reads, cumulative counter/timer deltas, absolute gauges, reset
semantics, incompatible sets/epochs, and the disabled-domain early return.
Converter-boundary checks cover the SDK default-off variant, native declarations
and registrations, optional source exclusion, private protocols, and gate order.

Both SDK variants passed the focused diagnostics and converter-boundary tests:

- `./gradlew-agent test --tests totalcross.sys.RuntimeDiagnosticsTest --tests tc.tools.converter.RuntimeDiagnosticsConverterTest`
- The same focused tests with `-PruntimeDiagnostics=true`.
- `./gradlew-agent artifactContentTest`, with diagnostics both off and on.
- `./gradlew-agent dist -x test`, with diagnostics both off and on.

Artifact-boundary validation also confirmed that RuntimeConfiguration contracts
remain in the public SDK artifact while startup/metadata implementations remain
in `totalcross-runtime-java.jar`.

Native VM CMake configure and builds of `tcvm` and `Launcher` passed with
`TC_ENABLE_RUNTIME_DIAGNOSTICS` both `OFF` and `ON`. Symbol inspection found
`OFF`: zero `tsRDS_*` and three `tsrRE_*`; `ON`: five `tsRDS_*` and the same
three `tsrRE_*` symbols.

The diagnostics-enabled deployed macOS smoke passed mixed Java/native
snapshots, one batched native read, a single native read, counter/timer deltas,
absolute gauge deltas, selective reset, gauge preservation, and disabled-domain
exit, and rejection of a delta across the reset epoch. The smoke output included
`fixture=RuntimeDiagnosticsSmokeApp,overallPass=true`. Copyright-header
validation and `git diff --check origin/master...HEAD` also passed. The final
worktree is clean.

Both RuntimeConfiguration macOS smoke fixtures passed in the diagnostics-enabled
SDK/VM. The annotated fixture reported
`platform=MACOS family=DESKTOP architecture=ARM64 backend=RASTER matchedRules=1`.
The no-metadata fixture reported `metadata=absent`; its startup path returns
before selector resolution when no selectors are present.

No manual local builds were run for Android, Windows, Linux, WinCE, or iOS. The
local native implementation was built and smoke-tested on macOS only.

## Limitations

This change adds no Image, Rendering, Prefetch, or Scheduling catalogs, detailed
tracing, event timelines, or runtime policy changes. Those feature-specific
observations remain deferred. The foundation currently defines only the
`RUNTIME` domain and synthetic observations needed to validate its mechanisms.
