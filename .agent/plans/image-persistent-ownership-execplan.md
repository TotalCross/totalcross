<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Fix persistent image ownership lifecycle and scaling

This ExecPlan follows `.agent/PLANS.md` and `AGENTS.md`.

## Purpose / Big Picture

Make persistent raster ownership scale with changed owner/image relations, release
owners when detached container subtrees become inactive, and keep garbage
collection maintenance out of image resolution and drawing. Preserve the
existing raster admission policy, pixels, public bridge boundary, and detached
construction behavior.

## Working Set and Resume Protocol

- `.agent/state/image-persistent-ownership.md` is the first read after a pause;
  it names the active slice, last commit, exact next action, and deferred checks.
- `.agent/evidence/image-persistent-ownership.jsonl` stores concise validation,
  operation-count, benchmark, and platform records. Search only for the active
  milestone or final reconciliation.
- `.agent/reports/image-persistent-ownership-editorial.md` is written at final
  handoff and summarizes delivered behavior and limitations.
- Root `AGENTS.md` and `.agent/PLANS.md` govern commits, headers, API checks,
  and validation scope. The attachment supplied the acceptance contract.

## Progress

- [x] (2026-11-25) Fetched `origin/master`; verified base
  `6b915883dd202f6db891990f6c3af26fb645ecc7`; created the dedicated branch and
  worktree at that exact commit. Initial contracts match the supplied snapshot.
- [x] (2026-11-26) Added deterministic red reproducer tests and captured the
  baseline operation counts; commit them before production changes.
- [x] (2026-11-26) Implemented inherited suspension for explicitly removed
  Container subtrees and verified removal, mutation, reattachment, reparenting,
  popup visibility, and detached ScrollContainer construction.
- [x] (2026-11-26) Replaced owner scans with two weak identity-hash indexes,
  image-group counts, bounded relation unlinking, and aggregate pipeline
  transfers; focused operation, collision, reclamation, and API checks pass.
- [x] (2026-11-26) Removed draw-path maintenance; added bounded mutation and
  outermost repaint sweeping, artifact exclusion, and a 200-sample comparison.
  The measured paired median overhead was negligible against the observed
  variation, so the repaint hook remains.
- [x] (2026-11-26) Completed focused JavaSE/JUnit acceptance, deployed API and
  artifact checks; prepared Linux, Windows, and Android CI functional smokes
  with exact same-run artifact provenance. Nonlocal runs remain pending until
  publication; the optional native macOS smoke was not run.
- [x] (2026-11-26) Reconciled requirements and wrote the handoff report.

## Current Architecture and Scope

`ImageDrawingBridge` delegates ownership to weak identity-hash indexes, with
per-image relation groups and a bounded sweep cursor. Retain, release, and
pipeline transfer inspect hash-collision chains and perform at most 32 stale
relation checks per mutation. Image resolution and draw-cache lookup do not
maintain the registry. `Container` propagates explicit subtree suspension to
descendants; `ImageControl` retains ownership while constructed detached and
releases it when an attached subtree is suspended. The public static surface of
`ImageDrawingFeatureBridge` remains fixed by `ArtifactBoundariesTest`.

Keep changes in the SDK UI/image lifecycle and its focused tests. Do not add
fields to `Image`, modify native-sensitive layout, alter public methods on
`ImageDrawingFeatureBridge`, or perform local builds for non-macOS targets.

## Plan of Work

1. **Baseline reproducer.** Add deterministic probes for quadratic work at
   500/1,510/3,020 owner-image pairs, ancestor removal leaks, stale associations
   across reconstructed screens, and global cleanup from resolution/drawing.
   Confirm the never-attached `ScrollContainer` case remains valid. Commit the
   intentionally failing baseline tests and evidence.
2. **Subtree lifecycle.** Add internal inherited suspension for explicitly
   detached subtrees. Reconcile descendant `ImageControl` slots on structural
   transitions only, preserving active-to-active reparent ownership and Window
   visibility/popup behavior. Cover nested containers, direct removal,
   `removeAll`, mutation while suspended, and reattachment.
3. **Indexed registry.** Implement two identity-hash indexes with weak referents,
   collision-safe `==` checks, atomic dual-index unlink, per-image counts, and a
   weak pipeline association. Transfer aggregate counts in `Image.setPipeline`
   through checked bulk pipeline methods. Verify deployed API availability.
4. **Bounded maintenance.** Remove cleanup from image draw/resolve methods. Add a
  resumable maximum-32-relation sweep, mutation-driven maintenance, a
  no-allocation outermost repaint hook every 64 entries, and a package-private
  deterministic sweep hook. Keep the repaint hook only if the specified
  comparison shows no material frame-pacing cost.
5. **Acceptance and handoff.** Expand focused tests for ownership semantics,
   collisions, weak reclamation, pipeline transitions, cache and pixel behavior,
   and public artifact boundaries. Run the permitted JavaSE/JUnit checks and
   macOS arm64 smoke only as justified. Prepare nonlocal target functional CI
   smokes but report them pending until they can run on the authorized branch.

## Surprises & Discoveries

- The checked-out repository has 144 unrelated untracked local artifacts. A
  separate worktree preserves them; the implementation branch starts clean.
- `origin/master` exactly matches the snapshot SHA and expected ownership
  structures.
- The local platform is Darwin arm64, so the requested local boundary is
  available.

## Decision Log

- Decision: use a new clean worktree on `fix/image-persistent-ownership-scaling`
  at the fetched `origin/master` tip.
  Rationale: preserve local untracked content and satisfy the branch/base gate.
  Date: 2026-11-25.
- Decision: preserve direct-parent-independent ownership for construction by
  distinguishing explicit subtree suspension from never-attached controls.
  Rationale: the existing admission test makes this observable behavior.
  Date: 2026-11-25.

## Validation and Acceptance

Use the smallest JavaSE/JUnit slice during development, with deterministic
operation counters instead of timing thresholds. At the milestone gate, run the
specified focused UI/image and artifact-boundary tests, deployed Java API
validation, focused header validation, `git diff --check`, and the required
repaint comparison. The comparison used 200 samples per configuration because
the paired timing spread exceeded the observed median delta. A native smoke is
optional and must follow `.agent/guides/macos-native-runtime-validation.md`.
Android, Windows, Linux, iOS, and other nonlocal targets are CI-only. Focused
Linux, Windows, and Android functional smoke jobs are prepared in
`.github/workflows/build.yml`; the manual trigger is in
`.github/workflows/merge.yml`. They remain pending without a published trigger.

## Risks and Open Questions

- Container and ScrollContainer internal child structure may require one
  lifecycle hook at the structural ownership boundary to avoid duplicate walks.
- Repaint maintenance may fail the measured overhead gate; mutation-driven
  bounded sweeping remains required either way.
- Nonlocal CI smoke jobs may require reusable workflow changes and may not be
  triggerable until publication; do not push or open a PR.
- The performance-lab repository holds the compact native lifecycle probe and
  cross-target runner. Its exact pinned commit and status are in the final
  report.

## Idempotence and Recovery

All changes and commits belong to the dedicated worktree and branch. Do not
modify or clean the original worktree, caches, generated outputs, or unrelated
branches. Recheck `HEAD`, scoped status, and the next uncommitted diff before
retrying a failed slice. Do not push, open a PR, merge, rewrite history, or
publish artifacts.

## Outcomes & Retrospective

The original implementation failed all three reproducer tests. Registration
inspections grew from 249,500 at 500 pairs to 9,117,380 at 3,020 pairs;
resolution/cache lookup scanned all 1,510 relationships twice. Removing a
screen root retained its 1,510 descendant owners, and reconstructing the next
screen raised the count to 3,020 without GC. See the evidence index for the
exact run and log paths. The subtree lifecycle slice now releases descendant
owners immediately and reconciles suspended mutations on reattachment. The
indexed registry passes collision, arbitrary-slot, weak-reclamation,
pipeline-transfer, and near-linear registration checks. Resolution and drawing
no longer trigger global cleanup; bounded maintenance is exercised on mutation
and on the measured repaint cadence. Focused JavaSE tests and artifact-content
validation pass. Android, Windows, and Linux functional smokes are prepared
with source and artifact provenance checks and remain pending until
publication. The optional native macOS runtime smoke was not run.

## Final Reconciliation

The final root branch is `fix/image-persistent-ownership-scaling`, based at
`6b915883dd202f6db891990f6c3af26fb645ecc7`; the final implementation and CI
commit is `ed026ea40c6e256335f288b7e5e39c75da857638`. The companion branch is
`fix/image-persistent-ownership-smoke`, based at
`fe23e009fa2fcdbf8c36746cea58b9946a138858`, with smoke app HEAD
`bae23107b57390525a90669086e9fb47efaaa440`. Exact commit history, changed
files, validation results, and pending target status are in
`.agent/reports/image-persistent-ownership-editorial.md`.

No branches were published, no PR was opened, and no merge or remote write was
performed. Read-only remote fetch/inspection was used to establish the bases.

## Revision Note

Initial plan created after the fresh-base and contract gate passed. Final
acceptance was reconciled on 2026-11-26.
