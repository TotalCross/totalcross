<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Codex Execution Plans (ExecPlans)

An ExecPlan is a living design and execution document for a complex feature or
significant refactor. It must let a new contributor make the next safe change,
verify it proportionally, and recover after interruption without reconstructing
the entire project history.

## Precedence and scope

Apply instructions in this order:

1. Safety and data-preservation instructions.
2. Explicit instructions from the user for the current task.
3. The token, output, and validation budget in `AGENTS.md`.
4. This document.
5. Instructions specific to the active ExecPlan.

An ExecPlan cannot require an expensive validation on every slice merely because
it recorded that validation at an earlier checkpoint. When a plan and
`AGENTS.md` differ on cost, use the smallest validation that can prove the
current change, unless the user requests the larger matrix, the milestone is
closing, the change directly affects an ABI/platform/measured hot path, or a
recorded prior failure justifies repetition.

## Resumable plans, not duplicated context

An ExecPlan is self-contained when it contains or names stable repository paths
for the knowledge needed to continue. It does not need to duplicate content
already held in stable architecture documentation, an active state file, an
evidence index, milestone history, an editorial report, or a maintained
interface specification. A reference must state what the file contains and when
to read it; “see documentation” is not sufficient.

For a new plan, read `AGENTS.md` and this file in full, then research only the
sources necessary to write a concrete plan. For a continuation, read the active
state file first, locate headings in the plan, and open only the sections needed
for the next action. Do not routinely reread the complete plan, architecture
documents, evidence index, or historical archive. Context compaction is not a
reason to search broadly: restart from state and inspect only active paths.

The active plan should normally be about 300–450 lines and its state file about
100–150 lines. These are soft limits, not a reason to remove necessary safety or
implementation detail. When the active plan outgrows them, consolidate completed
material into history and evidence rather than continuing to append it.

## Required plan structure

Every active ExecPlan contains these sections, kept concise and current:

- `Purpose / Big Picture`: the observable developer or user outcome.
- `Working Set and Resume Protocol`: the state, evidence, history, and report
  paths; their purpose; and the first read for a continuation.
- `Progress`: significant checkpoints and remaining work.
- `Current Architecture and Scope`: only context needed for remaining work.
- `Plan of Work`: completed milestone summaries, active milestone, and next
  milestones.
- `Decision Log`: decisions that change future architecture, semantics,
  compatibility, validation, or operational policy.
- `Validation and Acceptance`: behavior to prove and the applicable validation
  level from `AGENTS.md`.
- `Risks and Open Questions`: unresolved issues that can change implementation.
- `Idempotence and Recovery`: safe retry and local-change handling.
- `Outcomes & Retrospective`: a short milestone-level summary, not a diary.

An active plan may reference a separate editorial report. The report is required
for a completed plan, but need only be synthesized at important milestone
checkpoints and at completion. “Possible Article Angles” and “Suggested
Narrative” are finalization work unless the user explicitly requests editorial
work earlier.

## State, history, evidence, and editorial files

Use these optional supporting files when a plan is long-running or has repeated
validation. Create them only when they simplify resumption:

- `.agent/state/<plan-name>.md` is rewritten, not appended. It records the
  active milestone/slice, last commit, active paths, next concrete action,
  focused validation completed, deferred validation and reason, decisions still
  active, blockers, deliberate out-of-scope local files, and a resume command.
  It is the first normal read.
- `.agent/evidence/<plan-name>.md` or `.jsonl` is append-only. It records
  compact evidence records: timestamp, revision, milestone/slice, command or
  wrapper, status, counts, log/artifact paths, necessary hashes, and scope or
  limitation. It is searched selectively, not read during ordinary resumption.
- `.agent/archive/<plan-name>-history.md` stores completed milestone detail,
  retired revision notes, and references to immutable source snapshots. It is
  not read by default.
- `.agent/reports/<plan-name>-editorial.md` is a concise factual handoff. It is
  updated at major milestone completion and final plan completion, not after
  every slice.

References to these files are valid self-containment. Preserve information by
moving it or recording a stable revision/path; do not copy raw logs, hashes,
benchmark tables, and the same result into all four documents.

## Progress, decisions, and checkpoints

`Progress` records meaningful checkpoints, not commands or microedits. Use at
most one concise entry per logical commit, functional slice, material validation
result, direction change, or completed milestone. It should point to evidence
instead of repeating counts and hashes.

The `Decision Log` contains only choices that alter future work: architecture,
semantics, compatibility, operation class, validation strategy, or release
policy. Do not record mechanical test choices, every target name, or a repeated
standing rule.

`Surprises & Discoveries` may be retained in a plan when an observation affects
future work. Move resolved historical observations to the archive. Update
`Outcomes & Retrospective` when a milestone completes or a discovery materially
changes interpretation, not after every slice.

During a slice, keep state current only when it is needed for safe resumption.
After a logical commit, update state with what changed, commit, focused
validation, remaining work, and deferrals. Consolidate the active plan after a
family or ABI checkpoint. At milestone completion, update the plan summary,
history, evidence, editorial report, and state for the next milestone.

## Validation and benchmarks

Validation is mandatory in proportion to risk. Follow the four validation levels
in `AGENTS.md`: implementation, functional commit, operation family or ABI, and
milestone/release gate. Stop at the first sufficient level and record a deferred
more-expensive level with its reason.

Each milestone describes its observable acceptance behavior and the validation
level normally required. A plan may name an exact focused command, but it must
not convert a historical full matrix into a perpetual slice requirement.
Preserve full tool output in logs; record compact result summaries and paths in
the evidence index.

Benchmarking is evidence only for the workload and hot path measured. Do not
benchmark a newly added semantic operation when the benchmark does not execute
it and its measured hot path did not change. Use a small smoke benchmark only
for an affected measured hot path. Run full checkpoints at milestone closure,
optimization, measurement-regime changes, or explicit user request. Start with
60 and 200 samples; run more than 200 only for observed variance or an explicit
request. Raw samples stay in artifacts and their index, not repeated in plans.

## Writing guidance

Use plain language and define non-obvious terms at first use. State the purpose
and an observable result before implementation detail. Name repository-relative
paths, modules, functions, working directories, commands, expected concise
outcomes, and safe retries. Prefer prose to tables and inventories; use a table
only when it materially clarifies a comparison.

Do not replace key repository knowledge with an external link. Stable local
documentation may be referenced with a short explanation of its role. Do not
make a novice infer compatibility, ownership, GC, exception, security, or
rollback behavior that the plan can state directly.

Do not dump generated code, large logs, raw manifests, full matrices, or every
differential case into an ExecPlan. Keep evidence reproducible through commands,
small excerpts, paths, and evidence-index records.

## Milestones and completion

Milestones describe goal, work, result, and proof. Each must make an incremental
observable contribution and state what is accepted, what remains fallback, and
which validation level is expected. Prototypes and parallel paths are acceptable
when explicitly bounded, additive, and independently testable.

At plan completion, reconcile the final state with `Outcomes & Retrospective`
and the editorial report. The report must distinguish delivered work from plans,
measurements from estimates, and supported platforms from aspirations. It must
state limitations and claims requiring human review. It is a factual handoff,
not an incremental execution diary.

## Minimal skeleton

    # <Short, action-oriented description>

    This ExecPlan follows `.agent/PLANS.md` and `AGENTS.md`.

    ## Purpose / Big Picture

    Explain the observable outcome and how a developer can see it.

    ## Working Set and Resume Protocol

    Name the state, evidence, history, and editorial paths; state what each
    contains and when to read it.

    ## Progress

    - [x] (YYYY-MM-DDThh:mm:ssZ) Significant completed checkpoint.
    - [ ] Current slice and its next concrete action.
    - [ ] Remaining milestone or finalization work.

    ## Current Architecture and Scope

    Explain only the modules and contracts needed for remaining work.

    ## Plan of Work

    Describe completed milestones concisely, the active milestone, and next
    milestones with observable acceptance.

    ## Decision Log

    - Decision: …
      Rationale: …
      Date: …

    ## Validation and Acceptance

    Name the validation level, command, expected concise result, and deferred
    validation with reason.

    ## Risks and Open Questions

    State unresolved implementation, platform, or compatibility questions.

    ## Idempotence and Recovery

    State safe retries and local paths that must remain untouched.

    ## Outcomes & Retrospective

    Summarize completed milestone outcomes and point to evidence/history.

    ## Revision Note

    Record only a material plan-policy or milestone consolidation and its reason.
