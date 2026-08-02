<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Copyright provenance audits

This directory keeps the tooling, immutable evidence, and reviewed decisions used to preserve copyright notices when source files are renamed, split, copied, or substantially adapted.

The audit is based on code provenance, not merely the Git creation date of a pathname. A file introduced in 2026 can legitimately retain older notices when its implementation descends from an older file.

## Layout

```text
legal/copyright-provenance/
├── README.md
├── audit-code-provenance.py
├── active-audits.json
└── audits/
    └── <audit-id>/
        ├── manifest.json
        ├── summary.md
        ├── evidence/
        │   └── <historical-source>.json
        └── reports/
            └── <historical-source>.md
```

- `audit-code-provenance.py` performs the technical analysis.
- `audits/<audit-id>/manifest.json` records the immutable commit interval and final classifications.
- `evidence/*.json` contains detailed member matches and transitive lineage.
- `reports/*.md` presents the same evidence for human review.
- `active-audits.json` lists reviewed manifests that header validation may consume.

Generated audits start as `pending-review`. Do not activate one automatically.

## Usage

Run the script from any location inside the repository.

### Audit one historical file

```bash
python3 legal/copyright-provenance/audit-code-provenance.py \
  <initial-commit> \
  <final-commit> \
  TotalCrossSDK/src/main/java/totalcross/Launcher.java
```

The source path is interpreted at the initial commit. The initial commit must be an ancestor of the final commit.

### Discover refactorings automatically

```bash
python3 legal/copyright-provenance/audit-code-provenance.py \
  <initial-commit> \
  <final-commit>
```

Automatic mode considers supported files that were modified, deleted, renamed, or used as Git copy sources during the interval. It emits reports only when material lineage reaches a file present at the final commit.

By default only `.java` files are analyzed. Use `--extensions` to change the set:

```bash
--extensions .java,.kt
```

### Fail when an inherited header is missing

```bash
python3 legal/copyright-provenance/audit-code-provenance.py \
  <initial-commit> <final-commit> \
  --fail-on-missing-notice
```

Exit code `2` means material inherited code was detected in a final file whose header did not preserve all source copyright holders or the LGPL SPDX identifier.

### Choose the output directory

```bash
--output /tmp/provenance-audit
```

The default output is:

```text
legal/copyright-provenance/audits/<date>-<initial-sha>-<final-sha>/
```

Use `--audit-id` when a stable descriptive identifier is preferred. Existing non-empty output is protected unless `--overwrite` is supplied.

## How lineage is determined

The script combines four signals:

1. Git rename and copy hints from `git diff -M -C --find-copies-harder`.
2. Commit-by-commit propagation across the ancestry path from the initial commit to the final commit.
3. Java member comparison using exact tokens and normalized structural tokens.
4. Direct initial-to-final comparison to recover relationships obscured by heavy intermediate edits or a squashed history.

For example, the technical lineage may be:

```text
totalcross/Launcher.java
  → totalcross/LauncherArguments.java
  → tc/simulator/ApplicationLoader.java
```

If `LauncherArguments.java` no longer exists at the final commit, it is retained only inside the evidence lineage. The report lists `ApplicationLoader.java` as the final target.

The script also handles splits: one historical source can produce several final targets. A small `sourceCoverage` with high `targetCoverage` is expected when a focused helper class was extracted from a very large source file.

## Classifications

- `inherited`: strong evidence that a material portion of the final file descends from the historical source.
- `partial-inherited`: material extracted or adapted code was detected, but it represents only part of one or both files.
- `manual-review`: similarity exists but does not meet the material thresholds.
- `no-material-lineage`: no significant relationship was detected; these pairs are not emitted as final results.

Both `inherited` and `partial-inherited` normally require preserving the applicable source copyright chain.

The classifications are technical evidence, not an independent legal opinion. A person must review the generated report and confirm the final policy.

## Review and commit workflow

Use two commits so that the evidence refers to an immutable code revision without circularity.

### 1. Commit the code and header decisions

Include the refactoring, corrected headers, validator integration, and any tool changes. Record the resulting SHA as the audit final revision.

```bash
FINAL_SHA=$(git rev-parse HEAD)
```

Choose an initial SHA that contains the historical source before the refactoring began.

### 2. Generate and review the audit

```bash
python3 legal/copyright-provenance/audit-code-provenance.py \
  "$INITIAL_SHA" "$FINAL_SHA"
```

Review:

- every final target;
- the intermediate lineage;
- member-level matches and coverage;
- header status;
- false positives caused by boilerplate or trivial members;
- possible false negatives after extensive rewrites.

Edit only human-review fields deliberately added to the manifest schema. Do not rewrite raw evidence to make a result appear stronger.

### 3. Commit the audit separately

Commit the complete audit directory in a documentation/legal commit, for example:

```text
docs(legal): record code provenance audit
```

The first commit remains the exact code revision audited; the second commit adds its evidence.

## Activating an audit

After human review, change the audit manifest from:

```json
"reviewStatus": "pending-review"
```

to an approved project-defined state such as:

```json
"reviewStatus": "approved"
```

Then add its relative manifest path to `active-audits.json`:

```json
{
  "schemaVersion": 1,
  "active": [
    "audits/2026-08-02-aaaaaaaaaaaa-bbbbbbbbbbbb/manifest.json"
  ]
}
```

Header validation should consume only approved manifests listed there. Historical audits remain immutable even after a newer audit supersedes them. A new manifest can identify older audit IDs in its `supersedes` array.

## Multiple manifests

Each audit has its own manifest because it represents a specific immutable interval, tool version, and review decision. This is preferable to one global provenance file that is repeatedly rewritten.

`active-audits.json` is the small mutable index that selects the currently applicable reviewed manifests. The audit directories themselves form the permanent history.

## Staleness

Each historical source and final target records both its Git blob SHA and a code fingerprint calculated from Java tokens after comments and whitespace are removed. A validator can therefore distinguish:

- exact blob equality;
- header or formatting changes with the same code fingerprint;
- material code changes that invalidate the reviewed evidence.

Rerun or explicitly review the audit whenever a covered final file's code fingerprint changes.

## Limitations

- Similarity thresholds are heuristic and must be reviewed.
- Git history that was rewritten before the selected initial commit cannot be reconstructed.
- Reflection, generated code, macros, and cross-language rewrites may require manual evidence.
- Very small files and boilerplate may be ambiguous.
- The current member parser is intentionally lightweight and optimized for ordinary Java source, not every possible syntax edge case.
- Automatic mode analyzes changed source candidates, not every file in repository history.

## Reproducibility

The manifest records:

- full initial and final commit SHAs;
- execution timestamp;
- command line;
- script path and SHA-256;
- source and final-target blob SHAs.

Use full commit SHAs when publishing or reviewing an audit. Branch names are useful for navigation but are not immutable evidence.
