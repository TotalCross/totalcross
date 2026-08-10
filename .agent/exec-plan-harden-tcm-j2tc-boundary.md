<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Harden the J2TC metadata boundary before optimizer and AOT work

This ExecPlan follows `AGENTS.md` and `.agent/PLANS.md`. It is based on
`feature/422-create-ir-for-jniaot` at reviewed revision
`441c5785dd88a6aaf8c028c2a390c27d113ad0d6`.

Detailed implementation is split by responsibility so every newly created
planning file remains below 20 KiB and approximately 600 lines:

- `.agent/subplan-tcm-disabled-path-and-origins.md`
- `.agent/subplan-tcm-wire-and-publication.md`
- `.agent/subplan-converter-semantic-resolution.md`

## Purpose / Big Picture

Make TCM v1 cheap when unused, durable when emitted, and semantically anchored to
canonical converter rules before field optimization, HIR, AOT, or production
optimization experiments consume it.

At completion:

1. TCM `NONE` performs no TCM-only class/method/site/origin/StackMap collection.
2. Java-PC-to-TC-slot origin finalization is linear in the lowered instruction
   stream rather than rescanning it for each source bytecode.
3. TCM v1 uses explicit stable wire codes rather than Java enum ordinals.
4. Publishing a new sidecar never deletes the previous valid TCM before the
   replacement is ready.
5. TCZ SHA-256 is streamed and shared by writer/reader validation.
6. Java-to-TC type lowering and declaration-owner resolution have canonical
   implementations shared by J2TC validation and metadata capture.
7. Host-JDK `java.*` reflection is no longer semantic authority for method
   declaration resolution.
8. Existing TCM v1 remains readable and enabling TCM leaves TCZ bytes unchanged.

This plan does not implement field optimization, HIR, a TCM-consuming AOT
backend, telemetry, ProGuard integration, raw-class embedding, TCZ changes, or a
VM dependency on TCM.

## Working Set and Resume Protocol

Create and maintain:

```text
.agent/state/harden-tcm-j2tc-boundary.md
.agent/evidence/harden-tcm-j2tc-boundary-01.jsonl
.agent/archive/harden-tcm-j2tc-boundary-history.md
.agent/reports/harden-tcm-j2tc-boundary-editorial.md
```

The state file is the first read on resume. It records the active milestone and
subplan, current logical commit, active paths, next action, tests written but not
yet executed, validation completed, deferred validation, evidence/log paths,
blockers, and unrelated dirty files.

Read append-only evidence only for a specific prior result. Start `-02.jsonl`
before the first evidence file approaches 20 KiB. Read archive history only for a
resolved decision. Update the editorial report at milestone checkpoints and final
completion, not after micro-steps.

Stable predecessor references:

```text
.agent/reports/j2tc-semantics-and-tcm-editorial.md
docs/architecture/bytecode/tcm-compilation-metadata.md
.agent/reports/evaluate-proguard-before-j2tc-editorial.md
```

Read the first for delivered behavior, the TCM document for the current wire
contract, and the ProGuard report only when a compatibility fixture needs
reconstruction. Do not reread the completed predecessor ExecPlan by default.

## Progress

- [x] Prerequisite: prior work proved TCM default-off emission, TCZ byte identity,
  reader/hash validation, and native macOS smoke.
- [x] (2026-08-10T18:45:53Z) Milestone 0: froze v1 wire values and a fixture,
  captured identical `NONE`/`AOT` metadata work, representative timing, and
  declaration-resolution categories. Evidence is in the plan evidence index.
- [x] (2026-08-10T19:00:53Z) Milestone 1: `NONE` now uses a singleton no-op
  capture and skips StackMap materialization; origin finalization is one
  instruction scan plus one source-site emission pass. AOT facts, v1 bytes, and
  TCZ bytes remained stable; bounded repeat timing stayed within 5% of baseline.
- [x] (2026-08-10T19:09:20Z) Milestone 2: permanent v1 wire codes, neutral
  streaming artifact support, and replacement-safe publication passed frozen
  fixture, injected failure/fallback, deterministic-byte, and TCZ checks.
- [x] (2026-08-10T19:34:00Z) Milestone 3: canonical descriptor lowering and
  declaration resolution are shared by converter validation and metadata;
  focused program/device/constructor/host-independence regressions passed.
- [x] (2026-08-10T19:59:18Z) Milestone 4: focused tests, distribution, deploy,
  isolated byte-identity/inspection, 97-pass native macOS smoke, audits, and
  documentation completed.

## Current Architecture and Scope

Current conversion boundary:

```text
.class
  -> JavaClass / JavaMethod / JavaCode
  -> ByteCode[]
  -> Bytecode2TCCode
  -> Instruction[]
  -> CFG / register allocation
  -> TCCode / TCClass
  -> TCZ

               \-> CompilationMetadataCollector
                    -> CompilationMetadata
                    -> TcmWriter
                    -> TCM
```

TCZ remains executable authority. TCM remains optional compilation metadata.

Known hardening targets:

- `J2TC.process()` creates and invokes the real metadata collector even in
  `TcmMode.NONE`;
- `JavaCode` eagerly materializes `StackMapTable`;
- `CompilationMetadataCollector.finishMethod()` rescans the complete instruction
  vector for every Java site;
- TCM enum-like values are serialized with `ordinal()`;
- `TcmWriter.publish()` deletes the old sidecar before replacement;
- TCZ hashing uses whole-file reads;
- type lowering and declaration-owner resolution are duplicated;
- source `java.*` owner resolution consults the host JDK.

Primary production paths:

```text
TotalCrossSDK/src/main/java/tc/tools/converter/J2TC.java
TotalCrossSDK/src/main/java/tc/tools/converter/GlobalConstantPool.java
TotalCrossSDK/src/main/java/tc/tools/converter/tclass/TCMethod.java
TotalCrossSDK/src/main/java/tc/tools/converter/java/JavaCode.java
TotalCrossSDK/src/main/java/tc/tools/converter/metadata/
TotalCrossSDK/src/main/java/tc/tools/deployer/DeploySettings.java
docs/architecture/bytecode/tcm-compilation-metadata.md
```

## Repository and File-Size Policy

Every new source, test, documentation, agent, state, evidence, archive, or report
file must remain at or below 20 KiB and approximately 600 lines. Split a new
responsibility before either limit is exceeded.

Existing oversized files may receive surgical edits. Do not split, reformat, or
refactor them merely to reduce their size.

If substantial existing code is extracted to a new file, follow the copyright
provenance rules in `AGENTS.md`.

## Plan of Work

### Milestone 0 — freeze baselines

Record branch HEAD and scoped dirty-worktree exclusions.

Capture current documented TCM v1 values:

```text
format major/minor
required-section bit
section IDs
NativeKind values
InvokeKind values
SyntheticKind values
```

Produce one representative TCM v1 fixture with the current implementation under
ignored build/evidence output. Record its SHA-256 and compact decoded summary.

Capture current disabled-path work for the same fixed conversion with TCM `NONE`
and `AOT`, including classes/methods/sites/StackMap/origins collected. Temporary
instrumentation used only for the baseline must not become production behavior.

Select one fixed conversion workload for before/after deploy timing. Prefer a
direct converter/deploy invocation so Gradle configuration time does not dominate.
Use approximately three warmups and ten measured samples when practical. Preserve
raw samples outside the plan and record only summary statistics and paths.

Capture representative owner-resolution outcomes for program inheritance, mapped
`java.*` inheritance, constructors, interfaces, and unresolved owners.

The baseline measurement/validation is the final action of Milestone 0. Do not run
other tests earlier in the milestone.

### Milestone 1 — disabled metadata path and linear origins

Execute `.agent/subplan-tcm-disabled-path-and-origins.md`.

At completion, `NONE` must avoid metadata-only collection and StackMap retention,
while `AOT` preserves the same semantic facts. Origin finalization must use one
pass over final lowered instructions.

Run the subplan's tests and timing only after all milestone implementation is
complete. Then create one or more logical validated commits.

### Milestone 2 — stable wire format and safe publication

Execute `.agent/subplan-tcm-wire-and-publication.md`.

Keep TCM v1 numeric values unchanged, replace ordinal serialization with explicit
codes, stream TCZ hashes, share artifact validation, and preserve the previous
sidecar until replacement succeeds.

Run all format/failure tests only after implementation is complete. Then create
logical validated commits.

### Milestone 3 — canonical converter semantics

Execute `.agent/subplan-converter-semantic-resolution.md`.

Centralize Java-to-TC type mapping and declaration-owner resolution. Remove
duplicate collector rules and host-JDK `java.*` semantic dependence without
weakening TotalCross device API/4D validation.

Run all semantic-resolution tests only after implementation is complete. Then
create logical validated commits.

### Milestone 4 — final integration and handoff

Do not add new feature scope unless final integration exposes a defect.

Reconcile `docs/architecture/bytecode/tcm-compilation-metadata.md` with delivered
behavior. Do not document planned behavior as current.

The final test gate is the last action of implementation:

```bash
cd TotalCrossSDK
./gradlew-agent test --tests 'tc.tools.converter.metadata.*' \
  --tests 'tc.tools.converter.modernjava.*' \
  --tests tc.tools.converter.java.StackMapTableReaderTest \
  --tests tc.tools.converter.oper.OperandRegParameterMappingTest
./gradlew-agent dist -x test
./gradlew-agent deployModernJavaFeatureSmoke --warning-mode=none --console=plain
```

Perform isolated `NONE` and `AOT` deploys and prove every corresponding TCZ is
byte-identical. Validate the emitted TCM using the production reader/inspector.

Because shared declaration resolution changes deploy validation semantics, run the
aggregate native macOS `FeatureSmokeApp` once as the final runtime check. Build
`tcvm` only if the required runtime is absent.

Do not run Android/iOS/Windows matrices unless a platform-specific path changed or
evidence shows platform dependence.

Finally run:

```bash
git diff --check
python3 scripts/validate-copyright-headers.sh --files <changed-first-party-files>
```

Audit every newly created file against the 20 KiB/~600-line limit. Finish state,
evidence, history, Outcomes, and the editorial report.

## Surprises & Discoveries

- Final distribution showed that repository-owned 4D classes sometimes inherit
  through host-named Java/Javax types. The resolver now follows those names only
  into mapped TotalCross-owned implementations and never inspects host members.
- The local SDK runtime had `libtcvm.dylib` and a generated launcher executable,
  but not the expected launcher input path. The final deploy/runtime gate reused
  that ignored generated launcher without adding repository content.

## Decision Log

- Decision: harden TCM before field optimization.
  Rationale: future optimizer/AOT consumers should depend on a stable, cheap,
  canonical metadata boundary.
  Date: 2026-08-10.

- Decision: keep TCM v1 values and TCZ format unchanged.
  Rationale: these are implementation hardenings, not format redesign.
  Date: 2026-08-10.

- Decision: default-off disables metadata collection, not only serialization.
  Rationale: ordinary deploy users must not pay for an unrequested feature.
  Date: 2026-08-10.

- Decision: one canonical type mapper and one canonical declaration resolver.
  Rationale: TCM facts must not drift from J2TC semantics.
  Date: 2026-08-10.

- Decision: host-JDK `java.*` reflection is not semantic authority.
  Rationale: deploy behavior should depend on input and TotalCross-owned API
  models rather than the JDK release running deploy.
  Date: 2026-08-10.

- Decision: logical local commits are authorized; push/PR/tag/release are not.
  Date: 2026-08-10.

## Validation and Acceptance

User-specific rule: tests are executed only after implementation of the relevant
milestone is complete, and testing is the final implementation action before the
milestone is committed and closed.

Tests may be written alongside code but must not be executed iteratively. Do not
run `clean` by default. Save complete output in ignored logs and evidence paths.

Hard acceptance:

```text
TCZ format unchanged
TCZ NONE/AOT bytes identical
TCM v1 existing wire values unchanged
baseline v1 fixture readable
NONE performs no TCM-only collection
AOT retains required semantic metadata
origin finalization is linear in lowered instructions
failed pre-replacement write preserves previous valid TCM
artifact hashing is streaming
type lowering has one canonical implementation
declaration resolution has one canonical implementation
host-JDK java.* reflection does not define method semantics
```

The Milestone 1 timing workload is supporting evidence. Do not generalize it into
a universal deploy-speed claim.

## Commit Policy

Implement and write tests for a milestone without running them. Run that
milestone's validation once at the end. After it passes, partition the
already-validated changes into logical commits.

Keep code with the tests that prove it when practical. Use English Conventional
Commits compliant with `AGENTS.md`/`CONTRIBUTING.md`, with explanatory bodies for
non-obvious changes.

Inspect/stage scoped paths only. Preserve unrelated changes and never use
destructive Git commands. Do not push or create a PR.

## Risks and Open Questions

A complete source-JDK hierarchy may not exist in deploy inputs. Do not create a
broad new JDK classpath system. Prefer program classes plus TotalCross-owned mapped
device hierarchy, and represent unavailable source facts as unresolved.

A no-op metadata path must not accidentally disable normal deploy semantics such
as `Class.forName` processing. Separate metadata capture from semantic deploy state.

Avoid making low-level class parsing depend directly on CLI parsing when gating
StackMap. Prefer a conversion/session capability.

Atomic move is filesystem-dependent; document atomic replacement where supported
and a precise fallback elsewhere.

## Idempotence and Recovery

Keep generated fixtures, samples, temporary deploys, and decoded TCM output under
ignored build/temp paths.

State records exact active paths. On failure, record the first stable diagnostic
and rerun only the active milestone's final validation after fixing it.

A failed TCM publication may delete its own temporary file but must not delete the
previous valid sidecar before successful replacement.

Preserve unrelated local files and caches. Never reset the worktree to recover.

## Outcomes & Retrospective

Milestone 0 established the comparison boundary at revision `441c5785`: both
`NONE` and `AOT` collected the same 87 classes, 306 methods, 2,767 bytecode
sites/origin ranges, 681 call sites, 73 synthetic origins, and 108 StackMap
frames for the fixed aggregate smoke conversion. The captured TCM v1 fixture
and timing samples are recorded in the evidence index; timing is supporting
evidence only.

Milestone 1 made the disabled path structurally inactive: the same workload now
reports zero classes, fields, methods, sites, calls, origins, StackMap frames,
and synthetic origins in `NONE`, while AOT retains all frozen counts. Origin
finalization changed from `B` full instruction rescans to one `I` scan and one
`B` emission pass. TCZ and TCM fixture hashes remained unchanged. The first
timing pass was system-noisy; one bounded repeat placed both mode medians within
5% of baseline, so no universal speed claim is made.

Milestone 2 replaced every wire-driving enum ordinal with its documented
permanent code and reader lookup. Artifact naming, ordered bounded streaming
SHA-256, and validation now live in a neutral helper shared by writer and
reader. Publication writes the owned temporary file fully before atomic
replacement, falls back to regular replacement only when atomic move is not
supported, and preserves an existing sidecar on deterministic pre-replacement
failure. The frozen aggregate v1 TCM and both TCZ hashes remained byte-identical.

Milestone 3 made constant-pool descriptor lowering canonical for production and
metadata. One declaration resolver now serves deploy validation and metadata,
using active program classes and TotalCross-owned mapped hierarchies. It keeps
symbolic owners, does not search superclass constructors, supports both `java`
and `javax` compatibility namespaces, and leaves unavailable facts unresolved.

Milestone 4 passed the complete focused suite, incremental distribution, and
aggregate deploy. Isolated `NONE` and `AOT` output had the same TCZ SHA-256
`21f48888a0817eefe94cbc0e51ec4a775edcf8f6a3e20c6b9aec0b3df2be081c`;
the production inspector validated the new semantic TCM at
`55b189a03adf9e28c919a49743f23b956bba34129a5330d719456329fbd1f10c`.
The macOS runtime smoke reported 97 passes and no failures. Android, iOS, and
Windows matrices were intentionally not run because no platform-specific path
changed. Field optimization, HIR, and an AOT consumer remain future work.

Do not claim optimizer/AOT performance benefits from this hardening alone.

## Revision Note

2026-08-10: initial coordinator plan created and split into three responsibility
subplans to satisfy the requested 20 KiB/~600-line limit while retaining a short,
state-first resumable execution document.
