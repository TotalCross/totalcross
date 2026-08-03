---
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

name: validate-headers
description: Validate or fix LGPL-2.1-only headers, apply approved copyright provenance, and prepare provenance audits for renamed, split, copied, or extracted first-party code.
---

# Validate headers and copyright provenance

Use the repository tools from the repository root. The validator is the source
of truth; do not derive a header from the current pathname when an approved
active provenance audit applies.

## 1. Identify the intended scope

Avoid dumping a noisy worktree. Prefer:

```sh
git diff --name-only --diff-filter=ACMR --cached -- <task paths>
```

When nothing is staged:

```sh
git diff --name-only --diff-filter=ACMR -- <task paths>
```

Do not add repository headers to upstream, vendored, generated, or exempt files
merely to make validation pass. Respect the exclusions implemented by
`scripts/validate-copyright-headers.sh` and documented in `AGENTS.md`.
Generated evidence under `legal/copyright-provenance/audits/` is intentionally
excluded from ordinary header validation.

## 2. Validate the smallest useful file set

For explicit files:

```sh
python3 scripts/validate-copyright-headers.sh --files <changed files>
```

For staged changes, omit `--files`; when nothing is staged, the validator uses
the working-tree diff:

```sh
python3 scripts/validate-copyright-headers.sh
```

The validator also accepts:

```sh
python3 scripts/validate-copyright-headers.sh --commit <commit>
python3 scripts/validate-copyright-headers.sh <base> <head>
```

Approved manifests listed in
`legal/copyright-provenance/active-audits.json` take precedence over Git
pathname creation dates. For covered files, the validator checks the code
fingerprint and historical source before validating the inherited header. Treat
a stale-audit error as a request for a new audit; never bypass it with a
current-year-only header.

## 3. Fix ordinary or provenance-backed headers

Use the validator instead of manually reconstructing ranges:

```sh
python3 scripts/validate-copyright-headers.sh \
  --fix --files <changed files>
```

New first-party files normally use the current-year Amalgam header. Existing
files preserve the applicable SuperWaba, TotalCross, and Amalgam ranges.
Provenance-backed files inherit the chain from the approved audit.

After fixing, rerun the focused validation and:

```sh
git diff --check -- <task paths>
```

## 4. Audit substantial code movement

Create an audit when a refactor renames, splits, merges, copies, or extracts
substantial code and the destination pathname no longer represents the source
history:

```sh
python3 legal/copyright-provenance/audit-code-provenance.py \
  <initial-commit> <final-commit> [source-path]
```

Omit `source-path` for automatic source discovery. Review `summary.md` and the
per-source reports. Confirm that final targets are real descendants, unrelated
generic code is absent, and intermediate deleted paths appear only as lineage
evidence.

Do not edit generated evidence or reports to alter the conclusion. Correct the
audit tool and run a new audit when the result is wrong.

## 5. Approve or reject an audit

Audit review is a maintainer operation:

```sh
python3 legal/copyright-provenance/review-audit.py <audit-id>
```

The command asks whether to approve, reject, or cancel. Approval can optionally
append the manifest to `active-audits.json` without removing existing entries.
When activated, it verifies the audit tool hash and covered code fingerprints,
fixes the covered headers, runs validation, stages only the audit-related
changes, and creates one signed atomic commit.

Because the review command can stage and commit, do not run it unless the user
explicitly requested the review and authorized the resulting commit. Do not
manually mark a manifest approved or edit `active-audits.json` as a substitute
for this workflow.

## 6. Report results efficiently

Report:

- files checked or fixed;
- whether active provenance was applied;
- validation pass or failure;
- stale audits or manual-review findings;
- the commit created by `review-audit.py`, when explicitly authorized.

Do not paste full generated reports or broad validator output. Show only the
relevant errors and paths.
