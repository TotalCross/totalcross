<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Add typed Image runtime configuration

This ExecPlan follows `.agent/PLANS.md` and `AGENTS.md`.

## Purpose / Big Picture

Add declarative Image configuration to the SDK. Applications can declare
CLASS-retained storage rules on their entry class, the converter prunes and
persists those rules for deployment, and startup resolves them once into an
immutable typed policy. P1 keeps all image decode, storage, rendering, scroll,
prefetch, and pacing behavior unchanged. `COMPACT` remains a request and
resolves effectively to `STANDARD` until a later implementation supports it.

The public surface is limited to `ImageStorageProfile`, `ImageRuntimeRule`,
`ImageRuntimeRules`, and `RuntimeConfigurationReport.describe()`. Internal
feature plumbing remains outside application API artifacts.

## Working Set and Resume Protocol

No `.agent/state` file will be created, per the task instructions. This plan is
the resume record: read `Progress`, `Plan of Work`, and the active phase below
before continuing. At completion, read `.agent/reports/image-runtime-config.md`
for the factual handoff. Temporary build logs stay outside committed source;
the final report records commands and outcomes without embedding logs.

Primary source paths:

- `TotalCrossSDK/src/main/java/totalcross/sys/runtime/`: public selector
  contracts, package-private resolver, deployment metadata codec, and startup.
- `TotalCrossSDK/src/main/java/tc/tools/converter/runtimeconfig/`: ASM parser
  for CLASS-retained rules.
- `TotalCrossSDK/src/main/java/tc/tools/converter/J2TC.java` and
  `tc.tools.deployer.DeploySettings`: entry-class parsing, target tuple
  construction, reserved resource checks, and TCZ metadata insertion.
- `TotalCrossVM/src/init/startup.c`: native startup ordering before main-class
  loading.
- `TotalCrossSDK/src/main/java/tc/simulator/RuntimeConfigurationSimulator.java`,
  `RuntimeState.java`, and `Launcher.java`: simulator parsing and backend
  finalization.
- `TotalCrossSDK/build.gradle` and
  `TotalCrossSDK/src/test/java/tc/tools/ArtifactBoundariesTest.java`: internal
  runtime artifact versus application SDK boundaries.
- Existing `totalcross/sys/runtime` and converter tests define B's selector,
  resolution, deployment, simulator, and artifact behavior. Extend these with
  focused Image cases rather than making a second selector implementation.

## Progress

- [x] (2026-09-30) Fetched `origin/master`, verified RuntimeConfiguration,
  RuntimeSelector, RuntimeEnvironment, and RuntimeDiagnostics, and created the
  clean `feat/image-runtime-config` worktree from `623055afc`.
- [x] (2026-09-30) Committed the plan first, then completed Milestone 1:
  typed declarations, shared selector parsing, the feature bridge, deployment
  pruning, the `tc.imageruntimeconfig` v1 codec, and focused tests (42 passed).
- [ ] Complete Milestone 2: immutable policy, native/simulator startup,
  description contributors, API boundaries, and deployed smoke fixtures.
- [ ] Run final validation, write the final report, commit it last, push, and
  open a PR against `master` without merging.

## Current Architecture and Scope

B already owns runtime facts and selector behavior. Facts are the existing
`Platform`, `RuntimeFamily`, `GraphicsBackend`, and `Architecture` enums;
unavailable facts stay `null`. `RuntimeConfigurationParser` currently parses
`RuntimeWhen` and `RuntimeCondition`, while `RuntimeConfigurationMetadata`
prunes selectors against correlated deployment tuples and encodes them.
`RuntimeRuleResolver` owns specificity and deterministic equal-specificity
conflict semantics. `RuntimeConfigurationStartup` resolves `tc.runtimeconfig`
after native settings/backend setup and before the application main class loads.
The simulator parses class bytes before initialization and finalizes the raster
backend before loading application classes.

P1 will add Image-specific typed values around those shared mechanisms. Its
private resource is `tc.imageruntimeconfig`; its v1 envelope has magic `TCIC`,
version 1, a uint16 count, and records containing uint32 selector-payload
length, opaque selector bytes encoded by B's metadata codec through the feature
bridge, and a stable uint8 action tag (`0x01 STANDARD`, `0x02 COMPACT`). Zero
retained rules means no resource. The Image decoder rejects malformed,
truncated, unknown-tag, unsupported-version, and trailing data.

`RuntimeConfigurationFeatureBridge` is Java-public only for converter/runtime
package access and is excluded from `totalcross-api`, `dist/totalcross-sdk.jar`,
and aggregate application SDK output. It wraps B's resolver through one private
setting descriptor per typed feature setting; its generic rule/resolution
adapters carry typed values and matched diagnostic names. It also wraps B's
single-selector deployment pruning/codec and owns internal description
contributors. No public masks, numeric IDs, generic string keys, or historical
compatibility adapter are introduced.

## Plan of Work

### Plan checkpoint

Create and commit this plan first as
`docs(image): plan runtime image configuration`. Use a separate worktree so the
existing dirty worktree and its generated artifacts remain untouched.

### Milestone 1 — typed parsing and metadata

1. Add `ImageStorageProfile`, `ImageRuntimeRule`, and repeatable container
   `ImageRuntimeRules` under `totalcross.ui.image`. Rules require explicit
   `when` and `storage`, use CLASS retention, and require the entry-class
   `@RuntimeConfiguration` marker.
2. Extract a package-internal selector-annotation parser from
   `RuntimeConfigurationParser`. Keep B's accepted annotation structures and
   validation behavior unchanged; use it from both the existing parser and a
   converter-internal Image parser.
3. Add `RuntimeConfigurationFeatureBridge` in `totalcross.sys.runtime`. Keep
   generic `FeatureRule<T>` and `FeatureResolution<T>` adapters typed at feature
   call sites. Reuse B's resolver, deployment tuple pruning, and metadata codec
   instead of repeating their algorithms or wire format.
4. Parse direct and container Image annotations from class bytes without
   initializing the application. Reject missing marker/fields, malformed
   values, unknown enum constants, and invalid mixtures of direct and
   container encodings. Assign deterministic `image-rule-N` diagnostic names;
   names do not affect precedence.
5. Add a private v1 `tc.imageruntimeconfig` codec with explicit stable tags.
   Reuse B's exact correlated deployment tuples; omit empty metadata and reject
   resource-name collisions. Do not modify `tc.runtimeconfig`.
6. Add focused converter/runtime tests for repeatable parsing, required marker,
   malformed annotations/metadata, deployment pruning and specificity
   preservation, stable tags, collisions, resolver conflicts, and round trips.
   At milestone end run those tests and the existing RuntimeConfiguration tests.

### Milestone 2 — startup, policy, and description

1. Add one immutable internal Image policy snapshot. Preserve requested and
   effective storage separately; no rule and `STANDARD` resolve to
   `STANDARD`/`STANDARD`, while `COMPACT` resolves to `COMPACT`/`STANDARD` with
   reason `compact storage is not available in P1`.
2. Store typed defaults for future feature owners: six enabled raster-core
   booleans (zero-copy decode, opacity metadata, opaque write pixels, row
   readback, direct-color materialization, physical identity); two disabled
   raster variants (target-color conversion, physical-variant cache); disabled
   scroll raster reuse; disabled automatic preparation; and
   `LEGACY_PER_ENTRY_THREAD` prefetch. The first five raster-core values belong
   to P2 and `physicalIdentity` to P3; raster variants belong to P3; COMPACT
   storage capability to P4; scroll reuse to P7; preparation to P8; and
   prefetch to P9. These fields describe resolved policy; a field is operational
   only once its owning feature implementation consumes it.
3. Add internal `ImageRuntimeConfigurationStartup`. Native startup invokes it
   after environment/backend facts settle and before application-class loading.
   The simulator parses the same typed rule model before class initialization,
   then resolves after B finalizes the actual graphics backend. Missing Image
   metadata returns the default policy without selector resolution.
4. Add public `RuntimeConfigurationReport.describe()` and an internal,
   deterministic named-section contributor registry on the feature bridge.
   The report always creates Environment from `RuntimeEnvironment.current()`;
   Image registers its section during startup. Reinitialization replaces the
   same name. The report is explicitly invoked, human-readable diagnostic text,
   and not a parsing protocol or hot-path facility. The Image section describes
   the resolved typed policy, including defaults reserved for future
   consumers; a field is operational only once its owner consumes it.
5. Extend artifact tests to prove Image internals and the feature bridge are
   absent from application API, aggregate SDK, and distributed SDK artifacts,
   while the four intended public types are present and compile for
   applications. Add simulator tests and durable macOS smoke fixtures for
   defaults, COMPACT downgrade/report, declaration-order-independent
   specificity, and deterministic conflicts.
6. At milestone end run the focused SDK tests, `artifactContentTest`, and
   `dist -x test` with diagnostics off. Check compatibility with
   `-PruntimeDiagnostics=true` through focused compilation/artifact checks.
   Then build macOS ARM64 Release `tcvm` and `Launcher`, and run Image, B
   RuntimeConfiguration, and practical F RuntimeDiagnostics deployed smokes.

### Finalization

Write `.agent/reports/image-runtime-config.md` with the required sections:
Summary, Public API, Final architecture, Selector/deployment integration,
Requested vs effective policy, Runtime configuration description, Startup
integration, Validation, Compatibility, Known limitations, and Deferred work.
State explicitly that COMPACT is request-only and falls back to STANDARD, no
image decode/render behavior changed, no mask adapter exists, future P2/P3/P4/
P6/P7/P8/P9/P10 consume typed internal policy, and `describe()` is diagnostic
text rather than a parsing contract. Commit this report last.

Push `feat/image-runtime-config` and open a PR against `master`; do not merge.
Return the base/head revisions, PR URL/number, ordered commits, public and
internal API summaries, metadata format, policy examples and report sample,
mask confirmation, and concise focused/artifact/native/smoke/compatibility/
diff-check results.

## Decision Log

- Decision: Build the feature on `origin/master` and keep the existing dirty
  worktree untouched by using a sibling worktree.
  Rationale: the task requires latest master and the current checkout contains
  unrelated tracked edits, benchmark outputs, and generated artifacts.
  Date: 2026-09-30.
- Decision: Image owns only typed action parsing, Image metadata, policy
  capability resolution, and its description section; B remains the sole owner
  of selector parsing semantics, pruning, resolution, specificity, conflicts,
  and selector persistence.
  Rationale: preserve compatibility and avoid a second configuration system.
  Date: 2026-09-30.
- Decision: Do not create `.agent/state`.
  Rationale: explicit task instruction. Progress and resume instructions stay
  in this plan.
  Date: 2026-09-30.

## Validation and Acceptance

Validation follows `AGENTS.md` and stops at the smallest sufficient level per
slice. No native build occurs before Milestone 1 is complete.

- Milestone 1: focused Image parser/codec/deployment tests and existing
  RuntimeConfiguration parser, metadata, deployment, selector, resolver, and
  simulator tests pass; malformed data and conflict fixtures fail as expected.
- Milestone 2: focused Image and B tests, artifact-boundary tests,
  `artifactContentTest`, diagnostics-on focused checks, and
  `./gradlew-agent dist -x test` pass. macOS ARM64 Release `tcvm` and `Launcher`
  build, then deployed default, COMPACT downgrade, specificity, conflict, B
  regression, and practical F compatibility smokes pass.
- Final: `git diff --check origin/master...HEAD` passes; ordered history has the
  plan first and report last; no `.agent/state`, mask API, numeric public option
  IDs, Image decode/render edits, or hot-path configuration lookup exists.
  Review new files for the requested approximate 20 KiB / 600-line limits.
- Android, Windows, Linux, WinCE, and iOS builds are prohibited for this task.
  Full unrelated platform matrices and benchmarks are deferred because P1
  changes configuration paths only and does not alter a measured image hot
  path.

## Risks and Open Questions

- Verify the exact runtime Java artifact packaging paths while extending
  `build.gradle`; bridge and Image startup classes must be available to the
  converter/VM but absent from normal application artifacts.
- Startup and simulator integration must avoid resolving backend-dependent
  rules before the real backend is finalized. Follow B's existing deferred
  resolution lifecycle.
- `tc.imageruntimeconfig` is separate from `tc.runtimeconfig`; both collision
  checks and split-TCZ resource availability must remain consistent.
- The available host must support macOS ARM64 native output and deployed smoke
  execution. Report concrete limitations if toolchain/runtime prerequisites
  prevent that validation.

## Idempotence and Recovery

The source checkout is `/Users/flsobral/repos/totalcross-image-runtime-config`,
a clean worktree tracking the requested branch. The original
`/Users/flsobral/repos/totalcross-image-scroll-raster-fast-path` contains
pre-existing local modifications, benchmark artifacts, logs, and generated
files; do not clean, stage, or otherwise change those paths. Re-fetching
`origin/master` is safe, but do not rebase the feature branch onto a changed base
without checking that the plan's base requirement still holds. Re-run a focused
test freely. Keep SDK logs under `/tmp` or the ignored build directory.

## Outcomes & Retrospective

Milestone 1 uses B's selector parser, target pruning, metadata selector codec,
and resolver. Image storage tags are explicit stable values; impossible target
rules are omitted. Focused parsing, codec, converter deployment, collision,
specificity, and B regression tests passed. Milestone 2 remains active.

## Revision Note

Initial plan created before implementation; it captures the typed contract,
artifact boundaries, milestone order, and required final validation.
