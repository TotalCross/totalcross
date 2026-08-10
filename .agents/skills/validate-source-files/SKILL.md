---
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

name: validate-source-files
description: Validate repository source files for LGPL-2.1-only headers, approved copyright provenance, and forward-only byte-size limits; fix eligible headers and prepare provenance audits when required.
---

# Validate source files and copyright provenance

Use the repository tools from the repository root. The unified validator runs
two independent checks:

- copyright headers and approved provenance;
- the forward-only size ratchet for manually maintained source/build files.

The validator is the source of truth. Do not derive a header from the current
pathname when an approved active provenance audit applies, and do not create a
size baseline or exception file.

## 1. Identify the intended scope and snapshot

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
`scripts/source_validation/headers.py` and `scripts/source_validation/size.py`
and documented in `AGENTS.md`. Generated provenance evidence under
`legal/copyright-provenance/audits/` is excluded from ordinary validation.

## 2. Validate the smallest useful source slice

For explicit working-tree files:

```sh
python3 scripts/validate-source-files.py --files <changed files>
```

For the exact blobs staged for a logical commit:

```sh
python3 scripts/validate-source-files.py --staged --files <intended staged paths>
```

`--staged` compares `HEAD` with the Git index. Unstaged changes to the same path
do not affect this result. `--working-tree` compares `HEAD` with working-tree
contents. With neither flag nor `--files`, the validator selects staged changes
when present and otherwise tracked working-tree changes.

The validator also accepts:

```sh
python3 scripts/validate-source-files.py --commit <commit>
python3 scripts/validate-source-files.py <base> <head>
```

Use `--check headers` or `--check size` only when independently testing one
policy. Normal and pre-commit validation must run both checks.

## 3. Apply the source-size ratchet

The size check uses byte counts:

- new manually maintained source must not exceed 20,480 bytes;
- a file at or below 20,480 bytes in the base must not cross that boundary;
- an already oversized file may shrink or remain unchanged without warning;
- an already oversized file may grow by 1 through 500 bytes with a warning;
- legacy growth above 500 bytes fails.

The 500-byte tolerance never applies to a base file at or below 20 KiB.
Git-detected renames inherit the old path's base size. The selected Git snapshot
is always the baseline; there is no grandfather list, allowlist, exception file,
or stored per-file baseline.

Warnings exit successfully. Report a legacy-growth warning compactly and allow
the logical commit to proceed; do not require an unrelated refactor merely to
remove the warning.

On a size failure, do not commit, raise a baseline, or add an exception. Split,
extract, refactor, or reduce the staged source, stage the corrected slice, and
rerun validation. The size validator never modifies files automatically.

## 4. Fix ordinary or provenance-backed headers

Use the validator instead of manually reconstructing ranges:

```sh
python3 scripts/validate-source-files.py --fix --files <changed files>
```

`--fix` repairs only eligible working-tree headers; it never fixes size
violations. After a staged header failure:

1. fix the working-tree file;
2. restage only the intended path;
3. rerun `--staged --files` so the index is authoritative.

New first-party files normally use the current-year Amalgam header. Existing
files preserve the applicable SuperWaba, TotalCross, and Amalgam ranges.
Provenance-backed files inherit the chain from the approved audit.

Approved manifests listed in
`legal/copyright-provenance/active-audits.json` take precedence over Git
pathname creation dates. For covered files, the validator checks the recorded
code fingerprint and historical source before validating the inherited header.
Treat a stale-audit error as a request for a new audit; never bypass it with a
current-year-only header.

After fixing, rerun focused validation and:

```sh
git diff --check -- <task paths>
```

## 5. Audit substantial code movement

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

## 6. Approve or reject an audit

Audit review is a maintainer operation:

```sh
python3 legal/copyright-provenance/review-audit.py <audit-id>
```

The command asks whether to approve, reject, or cancel. Approval can optionally
append the manifest to `active-audits.json` without removing existing entries.
When activated, it verifies the audit tool hash and covered code fingerprints,
fixes covered headers, runs header-only source validation, stages only the
audit-related changes, and creates one signed atomic commit.

Because the review command can stage and commit, do not run it unless the user
explicitly requested review and authorized the resulting commit. Do not
manually mark a manifest approved or edit `active-audits.json` as a substitute.

## 7. Report results efficiently

Report:

- files checked or fixed;
- whether active provenance was applied;
- header and size validation results;
- any successful legacy-growth warnings;
- stale audits, size failures, or manual-review findings;
- the commit created by `review-audit.py`, when explicitly authorized.

Do not paste full generated reports or broad validator output. Show only the
relevant diagnostics and paths.
