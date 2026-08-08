<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Active State: Evaluate ProGuard before J2TC

## Status

- Plan status: completed.
- Current milestone: all milestones complete; the investment decision has been
  recorded in the plan, editorial report, and TCIR-continuation gate.
- Repository revision: `aa6b2ff3ded73a845848c014ee54fae9bcfc7a77`.
- Artifact root: `TotalCrossSDK/build/proguard-tcz-experiment/aa6b2ff3ded73a845848c014ee54fae9bcfc7a77/`.
- ProGuard version: pinned to `7.9.1` in the isolated experiment configuration.
- Final command: `cd TotalCrossSDK && ./gradlew-agent proguardTczExperiment --warning-mode=none --console=plain` passed in 264 seconds.
- Final log: `TotalCrossSDK/agent-logs/20260808-061159-proguardTczExperiment-full.log`.
- Summary SHA-256: `65db1b01fe75d1337a053f156c01336c3fbfa2c38a537ebbafd0e08bc3a6c80f` (`summary.json`) and `67f8a0187aced351a8a8e6ea6a5eff938daee6f02fcc7ca3a513f6befe328654` (`summary.md`).

## Variant status

- Baseline: passed; four JARs total 2,932,789 bytes and four TCZs total
  1,577,089 bytes, with 1,125 classes, 10,703 methods, and 229,556 code slots.
- Comparison baseline: passed; stripping only `LineNumberTable` changes no
  method or code-slot count and prevents debug bytes from being credited to
  optimization.
- Module-local full optimize: ProGuard passed; all four modules rejected by
  J2TC.
- Whole-runtime full optimize, one and three passes: ProGuard passed; all four
  modules rejected by J2TC in both variants.
- Field family: passed for all four modules in both whole-runtime and
  module-local modes. Whole-runtime reduces code slots by 3.495% versus 3.397%
  module-locally.
- Field subgroups: propagation, marking, and removal passed and materially
  contributed; generalization and specialization did not affect TC code slots.
- Local family: partial; only `lang` converted. Method family: partial; only
  `ui` converted and its code-slot count increased. Enum unboxing: passed but
  negligible.
- Safe shrink: API snapshot passed, but conversion remained partial after two
  distinct replacement-owner failures; three accepted modules were worse than
  field-only optimization on matching coverage.
- Closed-world smoke: not selected because the primary evidence distinguishes
  the bounded runtime-corpus decision; representative applications remain a
  prerequisite for broader investment.

## Focused validation

- `cd TotalCrossSDK && ./gradlew-agent deployTcbaselang deployTcbaseutil deployTcbasemisc deployTcui --warning-mode=none --console=plain`: passed in 39 seconds; log `TotalCrossSDK/agent-logs/20260808-045428-deployTcbaselang-full.log`.
- `cd TotalCrossSDK && ./gradlew-agent proguardTczExperiment --warning-mode=none --console=plain`: passed in 264 seconds; 20 tasks, 18 actionable, 11 executed, 7 up-to-date; log `TotalCrossSDK/agent-logs/20260808-061159-proguardTczExperiment-full.log`.
- The final run verified all four ordinary JAR and four ordinary TCZ hashes
  against the immutable baseline, reconstructed successful module artifacts,
  parsed final TCZ structure, and validated kept API for safe shrink.
- `python3 -m py_compile TotalCrossSDK/gradle/proguard_tcz_experiment.py`:
  passed; the generated Python cache was removed afterward.
- Focused `jq` summary assertions, tracked and untracked `git diff --check`,
  and the focused copyright-header validator all passed. The header validator
  checked seven applicable files and changed none.

## Compatibility issues

- Strict optimized line tables can begin after bytecode PC zero, but J2TC's
  line lookup assumes a preceding entry. Both the strict rejected attempt and
  a line-normalized diagnostic path are preserved.
- Full, local, method, and shrink variants expose replacement-constructor,
  generated-method-name, inherited-owner/device-API-owner, and operand-stack
  incompatibilities. They are retained as evidence rather than hidden by broad
  keep rules.
- Safe shrink stops after an exact `DriverManager` pin exposes another owner
  failure in `Settings`; rule expansion was deliberately not continued merely
  to obtain a passing result.

## Decision and future gate

- The next bounded compiler investment should be field propagation, marking,
  and removal immediately before or within J2TC, using the retained ProGuard
  variants as an external oracle.
- Do not infer a broad Java-aware HIR, production ProGuard integration,
  reachability/tree shaking, or TCIR/JIT/AOT priority from this corpus.
- Obtain representative application-level evidence before broadening into a
  general optimizer. TCIR/JIT/AOT needs separate execution-time evidence.
- No experiment implementation action remains. Generated summaries and raw
  evidence are complete under the artifact root.

## Deliberate local scope

- `.agent/sljit-depot-tools-execplan.md` is unrelated untracked local work and must not be modified.
- Generated JARs, TCZs, configurations, measurements, and logs remain below `TotalCrossSDK/build/proguard-tcz-experiment/`.
