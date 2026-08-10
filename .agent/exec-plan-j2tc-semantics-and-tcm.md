<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Harden J2TC semantics and emit optional TCM compilation metadata

This ExecPlan follows `AGENTS.md` and `.agent/PLANS.md`. It also follows the requested `totalcross-depot-tools` ExecPlan guidance.

Detailed work is split into three subplans so every new planning file remains
below 20 KiB and approximately 600 lines:

- `.agent/subplan-j2tc-float-and-compatibility.md`
- `.agent/subplan-tcm-semantic-preservation.md`
- `.agent/subplan-tcm-format-and-emission.md`

## Purpose / Big Picture

Make the Java-to-TotalCross conversion boundary safer and preserve information that
future optimization and AOT currently loses.

At completion, observe:

1. A Java method may contain `float` parameters in any position without corrupting
   later arguments on device. The old `JavaMethod` warning infrastructure is gone,
   and a data-driven test matrix covers static and instance methods with `float`
   mixed with `int`, reference, `long`, and `double` parameters.
2. J2TC accepts the JVM-valid class-file shapes identified by the completed
   ProGuard-before-J2TC experiment. Cases that are not valid JVM bytecode or are
   only unsupported ProGuard artifacts are classified with evidence instead of
   being "fixed" by weakening converter invariants.
3. `tc.Deploy` can optionally emit a sibling `.tcm` sidecar containing
   versioned compilation metadata while the TCZ format and normal deploy output
   remain unchanged.
4. With TCM disabled, generated TCZ bytes are unchanged by the metadata work.
   With TCM enabled, the TCZ hashes are identical to the same deploy without TCM,
   and the sidecar is bound to the produced TCZ artifact set by SHA-256.
5. The sidecar preserves facts that are otherwise lost before AOT can consume
   them: original/effective identities, source Java types versus lowered TC types,
   source invoke kind, Java-PC-to-TC-PC origins, hierarchy facts, dynamic-access
   facts, selected verification/type-frame information, and synthetic lowering
   origins.

This plan does not implement a new HIR, a production ProGuard integration, a
general optimizer, a new TCZ version, or an AOT consumer of TCM.

## Working Set and Resume Protocol

Create and maintain:

```text
.agent/state/j2tc-semantics-and-tcm.md
.agent/evidence/j2tc-semantics-and-tcm-01.jsonl
.agent/archive/j2tc-semantics-and-tcm-history.md
.agent/reports/j2tc-semantics-and-tcm-editorial.md
```

The state file is the first read on resume. It records the active milestone and
subplan, last logical commit, plan-start revision, active paths, next concrete
action, focused validation already completed, expensive validation deliberately
deferred, blockers, and unrelated dirty files that must remain untouched.

Read the evidence file only for a specific prior command/result. If it approaches
20 KiB, close it and continue with `-02.jsonl`, then `-03.jsonl`; do not let a new
evidence file exceed the repository-specific size rule.

Read the archive only when a resolved historical decision is needed. Keep the
editorial report concise and update it only at milestone completion and final
closure.

On resume, do not reread both subplans. Read the state file, then only the active
subplan section and named source paths. Use `rg`, headings, and narrow ranges.

Stable predecessor evidence:

```text
.agent/reports/evaluate-proguard-before-j2tc-editorial.md
docs/architecture/bytecode/java-to-totalcross-bytecode.md
.agent/exec-plan-totalcross-ir-jit-aot.md
```

Read predecessor files only for the specific compatibility or lowering fact being
reproduced; do not reopen completed plans by default.

## Progress

- [x] Architectural prerequisite: TCIR was established as a post-TC-bytecode
  runtime/backend IR, and the ProGuard experiment identified converter
  compatibility failures plus Java-level facts that do not survive lowering.
- [x] (2026-08-10) Milestone 0: fixed the start revision and dirty exclusions,
  indexed the retained ProGuard failure corpus and ordinary TCZ hashes, and
  created resumable state/evidence/history/report files without runtime changes.
- [x] (2026-08-10) Milestone 1: separated JVM slot width from TC register-bank
  choice, removed the warning infrastructure, passed the complete static/instance
  matrix, and closed with deploy plus native macOS smoke at `7c960237f`.
- [x] (2026-08-10) Milestone 2: fixed sparse line lookup, inherited declaration
  resolution, and handler-entry stack initialization; verified and classified
  transformed replacement descriptors/generated names as unsupported contracts;
  passed focused corpus rechecks, distribution, deploy, and native smoke.
- [ ] Milestone 3: introduce the in-memory semantic-preservation model required by
  TCM without changing TCZ serialization.
- [ ] Milestone 4: implement deterministic optional TCM v1 emission and reading,
  prove TCZ byte identity with TCM off/on, and close with deploy and native macOS
  smoke.
- [ ] Milestone 5: run the final proportional validation gate, reconcile evidence,
  complete the editorial report and Outcomes & Retrospective, and leave a clean
  architectural handoff for optimizer/AOT work.

## Current Architecture and Scope

The current conversion pipeline is:

```text
.class
  -> JavaClass / JavaMethod / JavaCode
  -> ByteCode[]
  -> Bytecode2TCCode + virtual operand stack
  -> target-shaped Instruction[]
  -> CFG
  -> register allocation
  -> TCCode
  -> TCClass
  -> TCZ
```

The float bug is caused by mixing two independent concepts. Java `float` occupies
one JVM local-variable slot, while TotalCross lowers it to the same 64-bit register
bank used for `double`. `OperandReg.init()` currently advances the JVM local index
by two for `F`, `J`, and `D`; only `J` and `D` may advance by two.

The completed ProGuard experiment exposed these J2TC categories:

- a valid `LineNumberTable` may begin after bytecode PC zero; line lookup must not
  index a nonexistent previous entry;
- an invocation may legally name a subclass/static receiver type as owner while
  the selected method is inherited; owner resolution must follow JVM hierarchy
  rules instead of rejecting such a call solely because the named owner does not
  declare the method;
- some optimized operand-stack shapes, replacement constructors, and generated
  method names failed J2TC, but not every such ProGuard output was proven JVM
  valid. Those cases require verification before converter changes.

The TCZ intentionally retains runtime-oriented types and instructions. Important
source facts disappear or become ambiguous:

```text
Java F                    -> TC double/reg64
invokestatic/special/
invokeinterface           -> CALL_normal
invokedynamic lambda      -> generated adapter class/calls
4D/replacement identity   -> effective TC identity
virtual converter values  -> physical TC registers
Java PC                   -> mostly final TC PC/debug line only
```

The TCM sidecar exists to preserve those facts without changing the TCZ runtime
contract.

## Repository and File-Size Policy

Every new source, test, documentation, agent, state, evidence, archive, and report
file must stay at or below 20 KiB and about 600 lines. Split new files by
responsibility before either threshold is exceeded. Existing oversized files may
receive surgical edits; do not refactor or split them merely to reduce size.

Metadata implementation should use small cohesive files under a dedicated
`tc.tools.converter.metadata` package rather than expanding `J2TC.java` or
`Bytecode2TCCode.java` with unrelated responsibilities.

## Plan of Work

### Milestone 0 — freeze the evidence boundary

Record the current branch revision and dirty-worktree exclusions in state. Locate
the exact generated fixtures/logs or reproducible configurations behind the
previous ProGuard J2TC failure categories. Do not regenerate the entire ProGuard
matrix unless the existing build artifacts are unavailable and a specific
reproduction cannot be reconstructed more cheaply.

Capture a small baseline for:

- the float parameter mis-mapping;
- the line-table PC-zero failure;
- inherited-owner invocation rejection;
- each remaining ProGuard failure family that may represent a real J2TC bug;
- normal TCZ output for one deterministic smoke/deploy input that will later be
  used for byte-identity comparison.

Do not run native or full SDK smoke in this milestone. This milestone is accepted
when later changes have reproducible fixtures and a recorded start revision.

### Milestone 1 — float parameter correctness

Execute `.agent/subplan-j2tc-float-and-compatibility.md`, float section.

The implementation must separate JVM local-slot width from TotalCross register-bank
selection, keep the current TotalCross `F -> double/reg64` lowering unchanged, and
remove `FLOAT_WARNING_MESSAGE`, `floatWarningMethods`, registration, flushing, and
all warning-only call sites after tests prove the fix.

Use a data-driven test matrix.

This milestone closes only after its focused converter tests, aggregate deploy
smoke, and native macOS execution pass. Do not run those expensive end-to-end
steps after every edit.

Create one logical commit for the complete float fix and its tests, for example:

```text
fix(compiler): correct float parameter slot mapping
```

Do not push.

### Milestone 2 — J2TC compatibility hardening

Execute the compatibility section of
`.agent/subplan-j2tc-float-and-compatibility.md`.

Fix the line-number and inherited-owner cases when their focused fixtures prove
the JVM-valid behavior. For the stack/replacement/generated-name categories,
first validate the candidate class with a JVM verifier or equivalent ASM verifier.
Only change J2TC when the input is valid and the required semantics fit TotalCross
runtime/replacement contracts.

Do not add broad `try/catch`, disable stack checks, remove validation, or add
ProGuard-specific name exceptions merely to turn a failure green.

Commit independent compatibility fixes separately with their regression fixtures.

At milestone closure, rerun only the relevant previously rejected ProGuard
families plus focused converter tests. Run aggregate deploy/native macOS smoke once
if invocation or generated-call semantics changed.

### Milestone 3 — preserve semantics in memory

Execute `.agent/subplan-tcm-semantic-preservation.md`.

Create an immutable or effectively immutable compilation-metadata model populated
while J2TC still has both Java and TotalCross representations available.

Preserve facts; do not encode optimizer conclusions such as `dead`, `pure`,
`inline`, or a precomputed call graph.

The model must distinguish:

```text
original identity        vs effective TC identity
source Java type         vs lowered TC type
source invoke kind       vs lowered TC call opcode
Java bytecode PC         vs emitted TC slot range
original/native method   vs ReplacedByNativeOnDeploy
known reflection roots   vs unresolved dynamic access
source synthetic site    vs generated TotalCross artifact
```

Parse/preserve the selected type-frame and classfile facts required by the TCM
subplan. Keep all TCZ writing behavior unchanged.

No native smoke is required here because no external artifact contract exists yet.
Close with focused parser/converter tests and one logical metadata-model commit.

### Milestone 4 — emit TCM v1

Execute `.agent/subplan-tcm-format-and-emission.md`.

Add explicit opt-in TCM emission to `tc.Deploy`; default behavior remains `NONE`.
Use the existing command-line/deploy-settings conventions and document the exact
option selected by implementation. Do not silently emit TCM in ordinary deploys.

TCM v1 is a deterministic, sectioned, little-endian sidecar with a magic/version
header, length-delimited sections, a string table, artifact manifest with SHA-256,
and semantic sections described in the subplan. Unknown future sections must be
skippable.

Write to a temporary file and atomically publish only after the complete deploy
artifact set exists and its hashes are known. If the user explicitly requested
TCM and sidecar emission fails, fail that requested operation rather than leaving
a partial/mismatched TCM. Normal deploy without TCM remains unaffected.

At closure prove:

```text
deploy without TCM -> TCZ A
deploy with TCM    -> byte-identical TCZ A + valid TCM
```

The reader must reject hash mismatch and accept unknown skippable sections.

Close with deploy plus native macOS smoke once, after implementation is otherwise
complete.

### Milestone 5 — final integration and handoff

Run the smallest final gate that proves the combined converter/deployer change.
Do not repeat prior milestone native runs unless the final integration changed
their behavior.

Reconcile:

- all confirmed ProGuard-discovered J2TC bugs and non-bug classifications;
- float warning removal and matrix evidence;
- TCM format documentation and reader/writer behavior;
- new-file size/line limits;
- copyright headers;
- TCZ byte identity with metadata disabled/enabled;
- smoke deploy and native macOS execution;
- logical commit history.

Complete `Outcomes & Retrospective` and
`.agent/reports/j2tc-semantics-and-tcm-editorial.md`.

Do not start field optimization, telemetry collection, TCM-consuming AOT, HIR, or
production ProGuard integration from this plan.

## Surprises & Discoveries

- `float` uses TC reg64 but one JVM local slot; the current code conflates these.
- Sparse line tables and inherited symbolic owners are JVM-valid shapes, not
  inherently ProGuard-specific.
- J2TC has the best metadata capture window before register allocation, while both
  Java bytecode identity and target-shaped instructions are still available.

## Decision Log

- Decision: Keep TCZ as the executable semantic authority and add TCM as optional
  compilation metadata.
  Rationale: preserve backward compatibility and avoid changing the VM format
  before AOT requirements justify it.
  Date: 2026-08-10.

- Decision: TCM is opt-in and default-off in this plan.
  Rationale: it contains application-level structural metadata and no current VM
  consumer requires it.
  Date: 2026-08-10.

- Decision: TCM stores source facts, not optimizer decisions.
  Rationale: facts remain reusable across future optimizer/AOT versions.
  Date: 2026-08-10.

- Decision: Bind TCM to every TCZ produced by the deploy invocation using SHA-256.
  Rationale: metadata must never be consumed with a mismatched executable artifact.
  Date: 2026-08-10.

- Decision: Do not change J2TC for unverified ProGuard output.
  Rationale: the previous experiment explicitly noted that optimize-without-shrink
  can produce unverifiable classes; compatibility work must target valid input.
  Date: 2026-08-10.

- Decision: Local commits are authorized; push, PR creation, tags, and releases are
  not.
  Rationale: the user requested frequent logical commits but did not authorize
  publication.
  Date: 2026-08-10.

## Validation and Acceptance

Follow the four validation levels in `AGENTS.md` and stop at the first sufficient
level during implementation.

Token/output policy for this plan:

- do not run the full SDK build, ProGuard experiment, deploy smoke, or native
  macOS smoke after each slice;
- use compile-only or one focused JUnit class while implementing a slice only when
  needed to catch a local regression;
- perform the milestone's focused test sweep once at closure;
- perform deploy/native macOS smoke as the last validation step of the milestone,
  not as an exploratory loop;
- save full command output under ignored build/log paths and record only compact
  result summaries in evidence;
- inspect failures with `rg`, `tail`, and narrow log ranges instead of rereading
  complete output.

Typical milestone-closure commands may include:

```bash
cd TotalCrossSDK
./gradlew-agent test --tests <focused-test-class-or-package>
./gradlew-agent dist -x test
./gradlew-agent deployModernJavaFeatureSmoke --warning-mode=none --console=plain
```

For native macOS runtime smoke, reuse the established aggregate smoke flow:

```text
TotalCrossSDK/build/feature-smoke/classes/install/macos/FeatureSmokeApp
```

If the required `libtcvm.dylib` is absent or native VM source changed, build only
at the end of the relevant milestone:

```bash
cmake -S TotalCrossVM -B build -DCMAKE_BUILD_TYPE=Release -G Ninja
ninja -C build tcvm
```

Then place the resulting dylib in the generated smoke install folder using the
existing repository smoke procedure and run `FeatureSmokeApp`. Acceptance is exit
status 0 and no `[FAIL]` lines.

At final closure run header and whitespace checks on changed files, plus one SDK
distribution gate if not already covered by the final milestone:

```bash
python3 scripts/validate-copyright-headers.sh --files <changed-files>
git diff --check
```

Check every newly created file from the plan-start revision. Fail the milestone if
a new file exceeds 20 KiB or approximately 600 lines; split it instead. Do not
apply this as a refactoring requirement to pre-existing oversized files.

## Commit Policy

Commit after each validated logical slice, keeping code and its regression tests
together unless they are independently useful. Check `git status --short`, stage
only intended files, and preserve unrelated changes. Follow `CONTRIBUTING.md`;
examples include `fix(compiler): correct float parameter slot mapping`,
`fix(compiler): handle sparse line number tables`, and
`feat(deploy): emit optional tcm metadata sidecar`. Local commits are authorized;
push, PR, tag, and release operations are not.

## Risks and Open Questions

The exact previous ProGuard stack failures may be invalid JVM bytecode under the
experiment's optimize-without-shrink configuration. Verification decides whether
they are J2TC work.

Line-number behavior before the first `LineNumberTable` entry must preserve
"unknown source line" rather than inventing a mapping unless existing TC debug
encoding requires a documented fallback.

J2TC mutates 4D/replacement identities during conversion. Metadata capture must
snapshot original identity before mutation without changing replacement behavior.

Instruction identity may or may not remain stable through register allocation and
TCCode expansion. The TCM subplan contains a decision gate for using a side map
versus a minimal origin identifier on instructions.

StackMapTable parsing must remain bounded and verifier-oriented; do not turn this
plan into a second JVM verifier.

TCM contains class/member identities and is not anonymous telemetry. This plan
does not upload it or enable network collection.

## Idempotence and Recovery

Generated fixtures, temporary TCM files, smoke output, ProGuard output, verifier
logs, and hash reports belong under ignored build directories or JUnit temporary
directories. Never commit them.

Write TCM via temporary file plus atomic rename so interruption cannot leave a
sidecar that appears complete.

A repeated TCM build from the same inputs and converter version must be
deterministic.

If a validation fails, preserve the first stable diagnostic and fix the relevant
slice. Do not clean unrelated caches or reset the worktree to recover.

Do not use destructive Git commands. Do not overwrite or stage unrelated local
changes.

After every logical commit, update state with the commit, focused proof, remaining
work, and any expensive validation intentionally deferred to milestone closure.

## Outcomes & Retrospective

Populate at milestone completion.

The final retrospective must state:

- the exact float root cause and supported parameter matrix;
- which previous ProGuard failures became confirmed J2TC fixes;
- which were classified as invalid/unsupported input and why;
- the TCM v1 format and metadata facts actually delivered;
- proof that TCM does not alter TCZ bytes;
- deploy/native macOS smoke results;
- limitations relevant to future field optimization, HIR, or AOT.

## Revision Note

2026-08-10: initial plan split into a coordinating ExecPlan plus two subplans to
keep every new planning artifact below the requested 20 KiB/~600-line limit and
to isolate compatibility work from the TCM format/metadata responsibility.
