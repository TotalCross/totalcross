<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Fix `Settings.iosCertDate` propagation in the iOS deployment flow

This ExecPlan follows `AGENTS.md` and the ExecPlan standard defined in `.agent/PLANS.md`, including the requested adaptations for resumable documents, risk-proportional validation, and supporting files. It is a living document. The `Progress`, `Surprises & Discoveries`, `Decision Log`, `Outcomes & Retrospective`, and `Revision Note` sections must remain current throughout execution.

All work must take place exclusively on the `fix/396-settingsioscertdate-is-empty` branch, created from `master`. Analysis and a remote comparison performed on 2026-07-21 confirmed that the branch and `master` were identical at commit `641a443b68361e78caabbe22ae68436b5809c72d`. Before each milestone, the executor must confirm whether that relationship has changed and record any differences that affect this plan.

The associated issue is `TotalCross/totalcross#396`, titled `Settings.iosCertDate is empty`, labeled `bug`, and assigned to milestone 7.3.0.

## Purpose / Big Picture

After this change, a TotalCross application deployed for iOS must receive a valid runtime value in `totalcross.sys.Settings.iosCertDate` whenever the deployment process has reliable metadata from which to determine that date. A developer must be able to observe the result in two ways: by inspecting the parameter serialized into the generated TCZ and by running a test application that shows a non-null `totalcross.sys.Time` instance with the expected date.

The current flow may discover the date only after the TCZ has already been generated. The result is an artifact without the parameter even when the deployer finds the provisioning profile later. The fix must guarantee the complete data path: discovery before conversion, serialization into the artifact, runtime loading, and assignment to `Settings.iosCertDate`.

The fix must also explicitly resolve the existing semantic mismatch. The `iosCertDate` name and Javadoc describe the expiration of the iOS certificate, while the observed producer uses `Provision.expirationDate`, which appears to represent the provisioning profile expiration. This plan does not allow the field to be populated with the current date, build date, dummy-material validity, or any other unreliable fallback. When the real validity cannot be determined, `null` is preferable to an incorrect value.

## Working Set and Resume Protocol

When incorporated into the repository, the active file must be kept at `.agent/plans/396-settingsioscertdate-is-empty.md`. The first step of a normal resume is to read `.agent/state/396-settingsioscertdate-is-empty.md`, when it exists. That state file must be rewritten rather than appended to, and must record only the active milestone and slice, the last logical commit, active paths, the next concrete action, focused validation already completed, deferred validations and their reasons, active decisions, blockers, deliberately out-of-scope files, and a resume command.

Use the following supporting files only when they simplify resumption:

- `.agent/state/396-settingsioscertdate-is-empty.md` for the current state. It is the first file read during a resume.
- `.agent/evidence/396-settingsioscertdate-is-empty.jsonl` for compact, append-only evidence. Each record must contain the date, revision, milestone, slice, command or wrapper, status, summary, log or artifact path, and known limitation.
- `.agent/archive/396-settingsioscertdate-is-empty-history.md` for completed milestone details, rejected alternatives whose rationale remains useful, and retired revision notes. It is not part of the default read set.
- `.agent/reports/396-settingsioscertdate-is-empty-editorial.md` for the factual handoff updated when important milestones close and when the plan is completed.

Do not duplicate the same matrix, log, or conclusion across these files. The active plan must retain only the context required for the next work. Full logs must remain outside the plan and be referenced by path.

When resuming without a state file, read in this order: `Progress`, the active milestone under `Plan of Work`, `Decision Log`, `Risks and Open Questions`, and `Validation and Acceptance`. Reread `Current Architecture and Scope` only when the next step touches a contract that has not yet been confirmed.

Run these initial resume commands from the repository root:

    git fetch origin
    git switch fix/396-settingsioscertdate-is-empty
    git pull --ff-only
    git rev-parse HEAD
    git merge-base --is-ancestor origin/master HEAD
    git diff --stat
    git status --short -- \
      TotalCrossSDK/src/main/java/tc/Deploy.java \
      TotalCrossSDK/src/main/java/tc/tools/deployer/Deployer4IPhoneIPA.java \
      TotalCrossSDK/src/main/java/tc/tools/converter/J2TC.java \
      TotalCrossSDK/src/main/java/totalcross/sys/Settings.java \
      .agent

The `git merge-base --is-ancestor` command must exit with status zero while the branch still contains `origin/master` in its history. A divergence must not be corrected with `reset --hard`, `checkout --`, or by discarding local changes. Record the situation and adapt the resume process while preserving existing work.

## Progress

- [x] (2026-07-21T20:16:01Z) Issue #396 was confirmed open, labeled `bug`, assigned to milestone 7.3.0, and describing an empty `Settings.iosCertDate` value on iOS.
- [x] (2026-07-21T23:54:34-03:00) The `fix/396-settingsioscertdate-is-empty` branch was confirmed in the repository and compared with `master`; both were identical at commit `641a443b68361e78caabbe22ae68436b5809c72d`.
- [x] (2026-07-21T23:54:34-03:00) Static analysis identified a probable ordering defect: `J2TC.process()` may produce `tcparms.bin` before the iOS metadata populates `Settings.iosCertDate`.
- [x] (2026-07-21T23:54:34-03:00) Static analysis identified the mismatch between the documented certificate expiration and the provisioning profile expiration currently assigned to the field.
- [x] (2026-07-21T23:54:34-03:00) This ExecPlan was restructured according to the resumable-plan standard supplied by the user, with a resume protocol, supporting files, milestones, and graduated validation.
- [x] (2026-07-22T00:16:06-03:00) Milestone 1: `IOSCertDateDeploymentTest.writesCertificateDateBeforeTczGeneration` reproduces the missing TCZ value using the versioned dummy materials; the runtime search confirmed that no consumer converts `iosCertDate` into `Settings.iosCertDate`.
- [x] (2026-07-24T16:52:31-03:00) Milestone 2: preserved the historical provisioning-profile expiration semantics, documented the null behavior for unavailable metadata, and added focused policy tests without changing deployment ordering.
- [x] (2026-07-24T17:00:06-03:00) Milestone 3: separated iOS metadata discovery from packaging side effects, resolved default paths before conversion, and preserved the selected date during later IPA initialization.
- [x] (2026-07-24T17:09:27-03:00) Milestone 4: added runtime loading of `tcparms.bin`, preserved the existing parameter format, and cleared iOS state across deployments in the same JVM.
- [ ] Milestone 5: validate the artifact and runtime through an iOS smoke deployment, then consolidate documentation, evidence, and the retrospective.

## Current Architecture and Scope

The repository combines the Java SDK, class converter, and native runtime. This fix must remain focused on the Java deployment path and, only when the investigation proves it necessary, the runtime parameter loader. It must not broadly refactor the signing system, replace the TCZ format, or change unrelated public APIs.

`TotalCrossSDK/src/main/java/tc/Deploy.java` coordinates option parsing, conversion through `J2TC`, and platform artifact generation. In the analyzed baseline, it calls `Deployer4IPhoneIPA.iosKeystoreInit()` before `J2TC.process()`, but the default iOS files may be configured only later inside the IPA generation block. A second initialization occurs after conversion, too late to change the TCZ that has already been produced.

`TotalCrossSDK/src/main/java/tc/tools/deployer/Deployer4IPhoneIPA.java` loads the PKCS#12 file and provisioning profile. PKCS#12 is a container that normally stores a certificate and private key. A provisioning profile is the Apple file that combines the signing identity, App ID, devices or distribution method, and expiration date. In the baseline, the routine assigns `Provision.expirationDate` to `Settings.iosCertDate`.

`TotalCrossSDK/src/main/java/tc/tools/converter/J2TC.java` generates `tcparms.bin`. This file is a parameter set embedded in the TCZ, the executable and resource archive consumed by the TotalCross runtime. In the baseline, `J2TC` writes `iosCertDate` only when the static field is already non-null.

`TotalCrossSDK/src/main/java/totalcross/sys/Settings.java` declares `public static Time iosCertDate`. `Time` is the TotalCross type used to represent date and time. This fix must preserve the public API and its source and binary compatibility.

The `tcparms.bin` consumer is `totalcross.sys.Settings.loadDeploymentParameters()`, which reads the resource through `Vm.getFile`, parses the existing `key=value` format, and assigns `Settings.iosCertDate`. `MainWindow` invokes it during runtime startup before application-specific constructor logic. Older runtimes and applications without the parameter remain compatible because absence leaves the field `null`.

The probable defective flow is:

    deployment starts
      -> iosKeystoreInit() runs without configured files
      -> Settings.iosCertDate remains null
      -> J2TC.process()
      -> tcparms.bin is generated without iosCertDate
      -> defaults or iOS files are configured
      -> iosKeystoreInit() discovers the date too late
      -> the IPA contains a TCZ that does not carry the date

The fix must not change Android, Windows, Linux, or macOS deployments, and must not force iOS material to be loaded when iOS is not among the selected targets.

The initially in-scope paths are:

- `TotalCrossSDK/src/main/java/tc/Deploy.java`;
- `TotalCrossSDK/src/main/java/tc/tools/deployer/Deployer4IPhoneIPA.java`;
- `TotalCrossSDK/src/main/java/tc/tools/converter/J2TC.java`;
- `TotalCrossSDK/src/main/java/totalcross/sys/Settings.java`;
- existing or new tests under `TotalCrossSDK/src/test`;
- the `tcparms.bin` loader, only after it is located;
- `.agent/plans`, `.agent/state`, `.agent/evidence`, `.agent/archive`, and `.agent/reports` for plan tracking.

Generated files, real keys, production certificates, private provisioning profiles, build directories, and logs must not be committed.

## Plan of Work

### Milestone 1 — Reproduce the absence and confirm the data path

The goal of this milestone is to turn the ordering hypothesis into a repeatable failure. At completion, there will be a focused test that produces or simulates a TCZ using iOS metadata with a known date and demonstrates that `iosCertDate` does not reach the artifact in the baseline. There will also be an objective description of the `tcparms.bin` consumer.

Begin by reading only the four main files listed under `Current Architecture and Scope`. Use `rg` to locate the producer and consumer:

    rg -n "iosCertDate|tcparms\.bin|activationServerURI|appVersion" \
      TotalCrossSDK TotalCrossVM

Create the smallest safe fixture possible. Do not use a production certificate, key, or profile. Prefer a sanitized provisioning profile or a testable parser structure with a fixed expiration. If the parser requires a real Apple signature and that prevents a deterministic test, extract only metadata reading into a unit that accepts fixture bytes. Do not perform the full deployer refactor yet.

Add a focused SDK-module test that invokes the step responsible for generating `tcparms.bin` and inspects the resulting TCZ. Before the fix, the test must prove one of these conditions: the date is discovered only after `J2TC.process()`, or the parameter is absent from the artifact. The final test name must describe the behavior, for example `IOSCertDateDeploymentTest.writesExpirationBeforeTczGeneration`, adapted to existing conventions.

Locate the runtime consumer and record the actual path in the plan:

    tcparms.bin
      -> function that opens the entry
      -> key/value parser
      -> string conversion
      -> assignment to Settings.iosCertDate

If no assignment exists for `iosCertDate`, record the discovery without implementing it during this milestone. Milestone acceptance consists of a reproducible red test, identification of the consumer, and compact evidence containing the command, result, and log path.

The normal validation is level 2 from the `AGENTS.md` scale: a focused unit test. Use level 1, `git diff --check`, for changes limited to the plan or fixtures. Do not run `clean`, `dist`, or a smoke deployment during this milestone.

After the reproduction is clear and contains no secrets, the first logical commit should use a message similar to:

    test(deploy,ios): reproduce missing certificate date

Push is not automatic; see `Idempotence and Recovery`.

### Milestone 2 — Define semantics and cases without reliable information

The purpose of this milestone is to prevent the implementation from correcting the ordering while continuing to transport a semantically incorrect date. At completion, there will be an explicit decision about the meaning of `Settings.iosCertDate`, tests for known cases, and Javadoc consistent with that decision.

The preferred option for 7.3.0 is to preserve the public field without renaming it and populate it with the expiration of the final X.509 certificate when that certificate is known. An X.509 certificate is the signed document that identifies the entity and contains a validity interval. The provisioning profile has its own expiration and must not be presented as the certificate validity without an evidence-based compatibility decision.

Before implementation, inspect the history of `iosCertDate`, repository usages, and locally published documentation. Do not perform broad research without need. Record one of the following decisions:

1. `iosCertDate` represents the real certificate expiration when known and remains `null` when deployment uses only dummy material or external signing.
2. Compatibility evidence shows that clients historically expect the provisioning profile expiration; in that case, preserve that behavior, correct the Javadoc, and record the need for a distinct future API.
3. The contract represents the effective distribution date, defined as the earlier expiration of the certificate and profile, but only if history and usages justify that interpretation. Do not select this option merely for convenience.

The decision must define behavior for a missing profile, missing certificate, dummy material, external signing, expired values, and invalid metadata. No case may silently reuse the value from a previous deployment.

Create unit tests for the selected policy. Normal validation remains level 2. If the decision requires exercising the PKCS#12 and profile parsers together, escalate to level 3, a focused integration test. Acceptance is an implementable and tested policy recorded in `Decision Log`.

Do not change ordering in `tc.Deploy.java` yet. This keeps the milestone reversible and separates the architectural decision from the functional change.

### Milestone 3 — Discover metadata before conversion without bringing signing forward

The objective of this milestone is to correct the ordering cause with the least possible coupling. At completion, the iOS deployment flow will have an idempotent operation that resolves and reads metadata before `J2TC.process()`, without signing the IPA or modifying user files.

In `Deployer4IPhoneIPA.java`, separate metadata discovery from packaging side effects. The exact implementation must follow the file's existing style, but the conceptual result must be equivalent to an internal immutable or controlled mutable structure containing:

    certificateExpiration
    provisioningProfileExpiration
    usesDummySigningMaterial
    sourceDescription

The discovery routine must accept already resolved paths, read only the required metadata, produce concise diagnostics, and be safe to call more than once without changing the IPA. It must not overwrite files, unnecessarily import credentials into global stores, or print secrets.

In `tc/Deploy.java`, resolve iOS options and paths before `J2TC.process()` when, and only when, iOS is among the targets. Convert the policy selected in Milestone 2 into a value for `Settings.iosCertDate` before generating `tcparms.bin`. IPA generation and signing must occur later and may reuse the previously calculated result.

Do not transport the validity of a dummy certificate or profile as the application's real validity. When final signing occurs outside the deployer, keep the field `null` unless Milestone 2 documented another behavior supported by compatibility evidence.

Temporarily retain the old initialization path only if needed to reduce risk. In that case, the old path must not alter the already selected value or repeat expensive side effects. Add a short comment explaining the transition and a test proving that discovery occurs before conversion.

Acceptance for this milestone is the red test from Milestone 1 passing, with a diff concentrated in `Deploy.java`, `Deployer4IPhoneIPA.java`, and tests. Normal validation is level 3, a focused deployment integration test. Escalate to level 4, the SDK module build, only if signature or visibility changes cross multiple compilation units.

The suggested logical commit is:

    fix(deploy,ios): resolve certificate date before conversion

### Milestone 4 — Complete serialization, loading, and isolation between deployments

The objective of this milestone is to guarantee that the correct date is not only calculated, but survives the artifact and reaches the runtime without leaking between executions. At completion, coverage will exist for serialization, parsing, and two sequential deployments in the same JVM.

In `J2TC.java`, preserve the existing parameter format. Do not introduce a second date format when the producer and runtime already use ISO-8601 or an equivalent representation supported by `Time`. Absence of the parameter must remain compatible with older applications and runtimes.

In the consumer located during Milestone 1, confirm that a valid value creates `Settings.iosCertDate`. An absent value must leave it `null`. An invalid value must not prevent application startup; it must produce a concise diagnostic and leave the field `null`, following the error policy of neighboring parameters.

Review deployment initialization so the static state is cleared before each execution. An Android deployment executed after an iOS deployment in the same JVM must not inherit the earlier date. A second iOS deployment using another fixture must carry the new date. A loading failure must also remove any remaining value.

Add tests that execute the following in the same process:

1. iOS deployment with date A;
2. non-iOS deployment or iOS deployment without metadata;
3. iOS deployment with date B.

The expected result is A, then `null`, then B. Store only the summary and complete-log path in the evidence file.

Normal validation is level 3. Run level 4 with `TotalCrossSDK/gradlew-agent` when closing the milestone because the deployment path and parameter loading cross multiple classes. Do not use `clean` without evidence of stale artifacts.

The suggested logical commit is:

    fix(deploy,ios): isolate certificate date state

### Milestone 5 — Prove behavior in the artifact and runtime

The final objective is to demonstrate the observable result of the issue. At completion, a test TCZ or IPA will contain the selected date, and a runtime application will show the same value in `Settings.iosCertDate`.

Generate an artifact using a fixture with a known date. Save the full log to a file and display only the summary and errors in the terminal. Extract `tcparms.bin` from the TCZ and confirm the expected key and representation. Then run a minimal iOS application or runtime harness that traverses the same loader and records a stable representation of the date, such as `getSQLString()` when available.

Repeat the smoke test for the case without reliable metadata. The application must start and the field must remain `null`. Production credentials are not required. The test may use material generated exclusively for tests or a harness that reproduces TCZ loading.

Because the change affects deployment, the generated artifact, and the runtime, normal closing validation reaches level 5, a smoke deployment. Run level 6, a full distribution build, only when closing milestone 7.3.0, when required by the maintainer, or when focused tests reveal cross-cutting impact. Level 7, a clean full distribution build, remains deferred unless stale artifacts are suspected.

Update the Javadoc for `Settings.iosCertDate` and any directly related documentation. Do not create a changelog or release unless repository policy requires it. Update the editorial report with what was actually delivered, not what was merely planned.

The suggested logical commit is:

    docs(sdk,ios): document certificate date behavior

This commit may be combined with the functional commit when the documentation is inseparable from the fix and the diff remains small.

## Surprises & Discoveries

- Observation: the first observed call to `iosKeystoreInit()` may occur before `appleCertStore` and `mobileProvision` are defined. Evidence: the analyzed ordering in `tc/Deploy.java` places initialization before `J2TC.process()`, while the iOS defaults may be resolved only in the later IPA generation block.

- Observation: a second iOS initialization occurs after the TCZ has already been generated. Evidence: the date may be populated in memory, but too late to be included in the `tcparms.bin` produced by `J2TC`.

- Observation: `J2TC` serializes `iosCertDate` only when the static field already contains a value. Evidence: the absence before conversion explains why the artifact does not carry the date.

- Observation: the observed producer uses `Provision.expirationDate`, although the public name and Javadoc refer to the certificate. Evidence: profiles and certificates have different expirations; transporting one as though it were the other can remain incorrect even after the ordering fix.

- Observation: modern flows may package with dummy material and perform final signing externally. Evidence: in that scenario, the deployer does not necessarily know the final certificate and must not publish the validity of temporary material.

- Observation: the initial analysis confirmed the producer of `tcparms.bin`, but had not yet confirmed the consumer that assigns `Settings.iosCertDate`. Evidence: this gap initially kept Milestone 1 open and prevented the issue from being considered resolved merely because the key existed in the TCZ.

- Observation: the native VM has no specific `tcparms.bin` consumer or `TTCSettings` field for `iosCertDate`; the smallest compatible consumer belongs in the Java SDK layer. Evidence: the search under `TotalCrossVM/src` found only the generic TCZ resource loader, so `Settings.loadDeploymentParameters()` uses the existing `Vm.getFile` bridge and is invoked by `MainWindow`.

- Observation: deployment generates the TCZ before attempting to open the IPA template. Evidence: the focused test wrote `TotalCrossSDK/IOSDateFixture.tcz`; the later packaging attempt stopped because `TotalCrossSDK/dist/vm/ios/TotalCross.ipa` does not exist. The regression assertion reached `tcparms.bin` and failed only because the `iosCertDate` key was absent.

- Observation: repository history contains the field and its producer from the first FOSS snapshot, with no conflicting SDK usage. Evidence: `git log -S'iosCertDate'` shows the field and the assignment using `Provision.expirationDate`; the only application-facing sample displays the field without deriving another date.

- Observation: the policy can be tested without signing material by constructing a minimal `MobileProvision` plist. Evidence: `IOSCertDatePolicyTest` verifies the profile expiration, a profile without `ExpirationDate`, and an absent profile.

- Observation: the ordering fix does not need to load the PKCS#12 store before conversion. Evidence: `Deploy` resolves the default paths and `iosMetadataInit()` reads only the provisioning profile; `iosKeystoreInit()` remains after `J2TC.process()` for IPA packaging.

- Observation: repeated metadata discovery is deterministic for the same resolved profile. Evidence: `IOSCertDateDeploymentTest` invokes `iosMetadataInit()` a second time and compares the ISO-8601 value after the TCZ has been generated.

Move resolved discoveries that no longer affect future work to `.agent/archive/396-settingsioscertdate-is-empty-history.md` when closing a milestone.

## Decision Log

- Decision: perform the entire fix on `fix/396-settingsioscertdate-is-empty`, preserving `master` as the base. Rationale: the branch was created specifically for the issue and was identical to `master` at the baseline, allowing an isolated diff. Date/Author: 2026-07-21 / initial plan.

- Decision: preserve the public `Settings.iosCertDate` API in this fix. Rationale: renaming or removing the field would introduce unnecessary source and potential binary compatibility breaks for a 7.3.0 bug fix. Date/Author: 2026-07-21 / initial plan.

- Decision: do not use the current date, build date, dummy expiration, or another arbitrary fallback. Rationale: `null` communicates missing information; a plausible but incorrect value can cause applications to make incorrect decisions. Date/Author: 2026-07-21 / initial plan.

- Decision: conceptually separate metadata discovery from IPA signing. Rationale: `J2TC` needs the date before packaging, but does not need to bring signing or artifact-manipulation side effects forward. Date/Author: 2026-07-21 / initial plan.

- Decision: use graduated validation and stop at the first sufficient level. Rationale: `AGENTS.md` requires focused tests before expensive builds and smoke deployments; repeating the full distribution after each slice would increase cost without proportionally increasing confidence. Date/Author: 2026-07-21 / plan revision.

- Decision: keep Milestone 1 limited to the red test and confirmation that the runtime does not yet consume the parameter. Rationale: adding the runtime reader at that point would exceed the objective of reproducing the ordering defect; Milestone 4 must determine the smallest necessary change to complete loading and state isolation. Date/Author: 2026-07-22 / Milestone 1 execution.

- Decision: `Settings.iosCertDate` continues to represent the expiration date from the iOS provisioning profile, preserving the historical producer behavior even when the X.509 certificate has a different validity interval. Rationale: the existing assignment uses `Provision.expirationDate`, repository history provides no contrary consumer contract, and the user-facing requirement is to maintain this behavior. Date/Author: 2026-07-24 / Milestone 2.

- Decision: return `null` when the provisioning profile or its expiration metadata is absent or cannot be converted to `Time`; dummy signing material and external signing must not be represented as a final validity date. Rationale: no reliable profile expiration exists in those cases, and an arbitrary or stale date would be misleading. Date/Author: 2026-07-24 / Milestone 2.

- Decision: resolve default iOS paths and read the provisioning profile before `J2TC.process()`, while retaining PKCS#12 loading and IPA creation after conversion. Rationale: serialization needs the profile date, but conversion must not trigger signing or modify credentials; the later initializer reuses the already-read profile and does not replace the selected date. Date/Author: 2026-07-24 / Milestone 3.

- Decision: load `tcparms.bin` in Java through `Vm.getFile` and `Settings.loadDeploymentParameters()`, invoked from `MainWindow`; accept the existing `Time.toIso8601()` representation, leave the field `null` when absent, and ignore invalid values with a concise debug message. Rationale: the native VM has no neighboring key/value parameter loader or `TTCSettings` field, while the Java resource bridge already supports TCZ entries and preserves compatibility with older artifacts. Date/Author: 2026-07-24 / Milestone 4.

- Decision: clear iOS deployment state before every `Deploy`, including non-iOS targets. Rationale: static deployer fields and `Settings.iosCertDate` otherwise survive sequential deployments in the same JVM and can leak an earlier iOS value. Date/Author: 2026-07-24 / Milestone 4.

## Validation and Acceptance

Validation must follow the escalation order in `AGENTS.md` and stop at the first level that provides sufficient confidence for the current slice:

1. static verification or `git diff --check`;
2. focused unit test;
3. focused integration test;
4. module build;
5. smoke deployment;
6. full distribution build;
7. clean full distribution build.

Use `TotalCrossSDK/gradlew-agent` for SDK builds. This wrapper keeps a complete log and produces an agent-oriented summary. Do not use `clean` by default. When a command is verbose, redirect the complete log and show only the summary and a short tail.

Focused commands, run from the repository root and adapted to the actual test names:

    git diff --check

    cd TotalCrossSDK
    ./gradlew-agent test --tests '*IOSCertDate*'

    cd TotalCrossSDK
    ./gradlew-agent test --tests '*Deploy*IOS*'

    cd TotalCrossSDK
    ./gradlew-agent dist -x test

Confirm the exact test command against the available tasks before recording it as evidence. Do not turn historical examples into permanent requirements.

The issue may be closed when all of the following behaviors have been observed:

1. A test fails on the baseline because the parameter is not available before TCZ generation and passes after the fix.
2. With reliable final metadata, `tcparms.bin` contains `iosCertDate` in the documented representation.
3. The runtime reads the value and exposes a valid `Settings.iosCertDate` instance.
4. The value matches the semantics recorded in Milestone 2 and the source metadata.
5. Without reliable metadata, the field remains `null` and the application starts.
6. Dummy material or external signing is not presented as the final certificate without a documented decision to the contrary.
7. Two deployments in the same JVM do not incorrectly share the value.
8. A non-iOS deployment does not load iOS material or inherit the previous date.
9. Focused tests and the required module build pass.
10. An artifact/runtime smoke test demonstrates the observable behavior.

Deferred expensive validations must be recorded in the state file with their reason. The full distribution build may be deferred until milestone 7.3.0 closes when the smoke test and module build are sufficient. A clean distribution build must be avoided without evidence of stale artifacts.

## Risks and Open Questions

The main functional risk is correcting the ordering while still transporting the wrong expiration. The decision between certificate expiration, profile expiration, or an effective date must be resolved before Milestone 3.

The main compatibility risk is changing how existing applications interpret `iosCertDate`. Inspect internal usages and local history; when evidence is insufficient, preserve the historically observable behavior and document the limitation.

The main implementation risk is bringing the full keystore initialization forward and introducing credential reads, prompts, or signing side effects before conversion. The early operation must read metadata only.

It has not yet been confirmed whether the runtime has an `iosCertDate` consumer. If it is absent, the fix crosses the SDK and runtime, requires a focused integration test, and increases format-compatibility risk.

iOS fixtures may be difficult to maintain when they depend on Apple-signed content. Prefer sanitized bytes, locally generated test certificates, or small parser abstractions. Never store real passwords or keys in the repository.

Time-zone representation must follow the existing `Time` contract and neighboring parameter formats. Do not introduce a conversion that changes the expiration date across time zones.

The baseline may change as `master` advances. Before a rebase or merge, compare the diff and revalidate the hypotheses. Do not rewrite the branch or force-push without explicit instruction.

## Idempotence and Recovery

Metadata discovery must be idempotent: repeated calls with the same files produce the same result and do not modify the IPA, user keystore, or fixtures. Static state must be cleared at the start of each deployment and, when necessary, after a partial failure.

Tests must be repeatable without depending on files left by a previous execution. Use temporary directories created by the test framework and remove only artifacts produced by the test itself.

If the ordering refactor causes a regression, preserve the reproduction tests and revert only the functional slice. Do not delete evidence or use destructive commands to clean the working tree.

Before committing, run:

    git diff --stat
    git diff -- \
      TotalCrossSDK/src/main/java/tc/Deploy.java \
      TotalCrossSDK/src/main/java/tc/tools/deployer/Deployer4IPhoneIPA.java \
      TotalCrossSDK/src/main/java/tc/tools/converter/J2TC.java \
      TotalCrossSDK/src/main/java/totalcross/sys/Settings.java \
      TotalCrossSDK/src/test \
      .agent
    git diff --check
    git status --short -- \
      TotalCrossSDK/src/main/java/tc/Deploy.java \
      TotalCrossSDK/src/main/java/tc/tools/deployer/Deployer4IPhoneIPA.java \
      TotalCrossSDK/src/main/java/tc/tools/converter/J2TC.java \
      TotalCrossSDK/src/main/java/totalcross/sys/Settings.java \
      TotalCrossSDK/src/test \
      .agent

Logical local commits are allowed during execution when each slice has sufficient validation. Pushes, pull-request creation, issue updates, tags, releases, and artifact publication are not automatic; they require explicit user instruction. This plan does not create tags or releases.

Before any authorized push, fetch the remote again, confirm that the remote branch has not advanced unexpectedly, and push without `--force`. If a pull request already exists, update the same branch rather than creating another. Unrelated local changes must remain untouched.

If an execution fails after generating a TCZ or IPA, record the artifact path and error, remove only temporary files produced by that execution, and resume from the last logical commit. Do not delete global caches or dependency directories merely to obtain a clean tree.

## Outcomes & Retrospective

Milestone 1 completed: `TotalCrossSDK/src/test/java/tc/IOSCertDateDeploymentTest.java` compiles a temporary `MainWindow` application, invokes the public `tc.Deploy` flow for iOS, and reads `tcparms.bin` from the generated TCZ. In the baseline, the date becomes available in `Settings.iosCertDate` only after `J2TC.process()`, and the assertion for the artifact key fails. The focused test was run on 2026-07-22 and failed as expected; the compact log is at `TotalCrossSDK/agent-logs/20260722-001606-test-agent.log`. Static search found no runtime consumer for the key. No module build, smoke deployment, or validation for later milestones was performed.

Milestone 2 completed: `Deployer4IPhoneIPA` now isolates the historical provisioning-profile expiration conversion in a null-safe helper, and `Settings.iosCertDate` documents that contract. `IOSCertDatePolicyTest` covers a known profile expiration, missing expiration metadata, and an absent profile. The focused command passed on 2026-07-24; the compact log is at `TotalCrossSDK/agent-logs/20260724-165231-test-agent.log`. Deploy ordering, TCZ serialization, runtime loading, and smoke deployment remain deferred to milestones 3–5.

Milestone 3 completed: `Deploy` now initializes iOS paths and reads the provisioning profile before `J2TC.process()`, while the PKCS#12 load and IPA packaging remain later. `IOSCertDateDeploymentTest` passed and verified both the `tcparms.bin` value and repeated discovery. The focused command passed on 2026-07-24; the compact log is at `TotalCrossSDK/agent-logs/20260724-170006-test-agent.log`. Runtime loading and cross-deployment state isolation remain deferred to milestones 4–5.

Milestone 4 completed: `Settings.loadDeploymentParameters()` reads `tcparms.bin` through `Vm.getFile`, parses the existing ISO-8601-like representation, assigns a `Time`, and clears/ignores absent or invalid values. `Deploy` now resets iOS state before parsing each execution. Focused runtime-parameter, iOS deployment, state-policy, and SDK `dist -x test` validations passed; logs are `TotalCrossSDK/agent-logs/20260724-170800-test-agent.log`, `TotalCrossSDK/agent-logs/20260724-170822-test-agent.log`, `TotalCrossSDK/agent-logs/20260724-170927-test-agent.log`, and `TotalCrossSDK/agent-logs/20260724-170834-dist-agent.log`. A final iOS runtime smoke deployment remains deferred to milestone 5.

When closing each milestone, record a short factual summary here containing the delivered behavior, validation performed, associated evidence, and any limitation that affects the next milestone. Move completed details to the history file when they begin to make the active plan difficult to resume.

At completion, the editorial report must include:

- `Editorial Summary`;
- `Original Plan versus Actual Outcome`;
- `What Changed`;
- `Decisions and Trade-offs`;
- `Unexpected Problems and Discoveries`;
- `Validation and Measurable Results`;
- `Useful Evidence and Examples`;
- `Limitations, Remaining Work, and Open Questions`;
- `Possible Article Angles`;
- `Suggested Narrative`;
- `Claims Requiring Human Review`.

The final retrospective must clearly distinguish delivered behavior from merely planned work, observed measurements from estimates, and tested platforms from assumed platforms.

## Revision Note

2026-07-21: the previous plan was reorganized to follow the resumable standard supplied by the user. The revision replaced the linear structure with `Purpose / Big Picture`, `Working Set and Resume Protocol`, incremental milestones, graduated validation, explicit risks, safe recovery, supporting files, and commit/publication policy. The technical findings and scope of issue #396 were preserved. The `fix/396-settingsioscertdate-is-empty` branch was confirmed identical to `master` at commit `641a443b68361e78caabbe22ae68436b5809c72d`.

2026-07-22: Milestone 1 was completed with an automated red reproduction and identification of the actual runtime path. The reproduction uses only the dummy materials already versioned under `TotalCrossSDK/etc/tools/ipa`; the missing IPA template prevents later packaging, but the failure occurs after the TCZ is generated and does not prevent inspection of the parameter.

2026-07-24: Milestone 2 preserved the historical provisioning-profile expiration contract, added null-safe policy coverage, and aligned the public Javadoc. No deployment ordering or runtime validation was performed.

2026-07-24: Milestone 3 moved profile metadata discovery before TCZ conversion without moving PKCS#12 loading or IPA packaging. The focused iOS deployment test passed; runtime loading and broader builds were not run.

2026-07-24: Milestone 4 completed the Java-side runtime consumer and deployment state cleanup. The existing `tcparms.bin` representation was preserved, invalid values are non-fatal, and focused tests plus the SDK distribution build passed. iOS runtime smoke validation remains for milestone 5.
