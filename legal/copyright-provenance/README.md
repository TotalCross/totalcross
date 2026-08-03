<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Copyright provenance audits

This directory keeps the tooling, immutable evidence, and reviewed decisions used to preserve copyright notices when source files are renamed, split, copied, or substantially adapted.

The policy is based on code provenance, not only on the Git creation date of a pathname. A file introduced recently can legitimately retain older notices when its implementation descends from an older source file.

## Layout

```text
legal/copyright-provenance/
├── README.md
├── audit-code-provenance.py
├── review-audit.py
├── provenance.py
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
- `review-audit.py` records approval or rejection and creates the review commit.
- `provenance.py` provides stable manifest, fingerprint, and header helpers.
- `active-audits.json` selects approved manifests consumed by header validation.
- `audits/<audit-id>/` stores the immutable technical evidence.

Generated audits start as `pending-review`. Never add one to `active-audits.json` manually before reviewing its reports.

## Generate an audit

Audit one historical source:

```bash
python3 legal/copyright-provenance/audit-code-provenance.py \
  <initial-commit> \
  <final-commit> \
  TotalCrossSDK/src/main/java/totalcross/Launcher.java
```

Discover refactorings automatically:

```bash
python3 legal/copyright-provenance/audit-code-provenance.py \
  <initial-commit> \
  <final-commit>
```

Automatic mode seeds only files materially modified, removed, or renamed in the interval. Unchanged files are not treated as sources merely because aggressive Git copy detection considers them similar.

Optional modes:

```bash
--include-unchanged-copy-sources
--include-responsibility-transfer
```

`responsibility-transfer` is disabled by default. When enabled, it emits only `manual-review` candidates and never establishes inherited copyright automatically.

The default output is:

```text
legal/copyright-provenance/audits/<date>-<initial-sha>-<final-sha>/
```

Use `--audit-id` to choose a stable identifier.

## Review an audit

The review framework itself must already be committed. The Git index must be empty before starting.

Run:

```bash
python3 legal/copyright-provenance/review-audit.py <audit-id>
```

The script asks whether to approve, reject, or cancel.

### Approval without activation

When an audit is approved, the script asks:

```text
Add this audit to active-audits.json without removing existing entries? [y/N]
```

Answering no:

- marks the manifest as `approved`;
- records reviewer identity and timestamp;
- does not alter headers;
- does not activate the audit;
- creates one signed documentation commit.

### Approval with activation

Answering yes performs one transactional workflow:

1. Verify the SHA-256 of the exact audit tool recorded by the manifest.
2. Mark the manifest as `approved`.
3. Append the manifest to `active-audits.json` without removing existing entries.
4. Load the approved audit through the header validator.
5. Verify each audited target's code fingerprint.
6. Repair inherited headers for all material targets.
7. Run focused header validation again.
8. Verify the audit tool hash again.
9. Stage only the audit directory, active index, and affected headers.
10. Create one signed atomic commit.

Default commit subject:

```text
fix(legal): activate copyright provenance audit
```

Use `--no-sign` only where signed commits are unavailable:

```bash
python3 legal/copyright-provenance/review-audit.py <audit-id> --no-sign
```

### Rejection

A rejected audit is marked `rejected` and committed as historical evidence. If it was already active, only that manifest is removed from the active index; all other active audits are preserved.

Default commit subject:

```text
docs(legal): reject copyright provenance audit
```

### Transaction safety

Before modifying files, the review script snapshots:

- the manifest;
- `active-audits.json`;
- every target header that may be repaired.

If hash verification, fingerprint validation, header repair, staging, or commit fails, those files are restored and the intended paths are unstaged. The generated audit evidence itself is left available for inspection.

The script refuses to run when:

- unrelated files are already staged;
- the audit framework has uncommitted changes;
- the manifest tool hash does not match;
- an audited target is missing;
- a target code fingerprint changed;
- the audit contains macOS `__MACOSX` or `._*` metadata.

## Header validation

The repository validator is:

```bash
python3 scripts/validate-copyright-headers.sh
```

Repair explicit files:

```bash
python3 scripts/validate-copyright-headers.sh --fix --files \
  path/to/First.java \
  path/to/Second.java
```

Validate only one active audit:

```bash
python3 scripts/validate-copyright-headers.sh \
  --audit-id <audit-id> \
  --files <audited-targets>
```

For a covered file, validation uses this precedence:

1. Locate the target in approved manifests listed by `active-audits.json`.
2. Verify its code fingerprint.
3. Read each historical source from the immutable initial revision.
4. Build the inherited copyright chain.
5. Apply the current Amalgam end year.
6. Validate or repair the target header.

Only files without an active provenance rule fall back to the ordinary Git creation-year policy.

A fingerprint mismatch fails as a stale audit. The validator never silently falls back to a new-file header for a changed audited target.

Generated evidence under:

```text
legal/copyright-provenance/audits/
```

is excluded from ordinary header validation. The audit tool, review tool, shared helper, and this README remain normal first-party files and must carry repository headers.

## Active audit index

`active-audits.json` is the only mutable index:

```json
{
  "schemaVersion": 1,
  "active": [
    "audits/2026-08-02-aaaaaaaaaaaa-bbbbbbbbbbbb/manifest.json"
  ]
}
```

Each audit keeps its own manifest because it represents a specific immutable interval, tool version, and review decision. Older evidence is not overwritten when a later audit supersedes it.

## Inherited header normalization

For historical sources, the validator preserves named early holders and normalizes repository-era ranges:

- Wabasoft notices retain their source years.
- Dave Slaughter notices retain their source years.
- SuperWaba ranges end in 2013.
- TotalCross ranges end in 2021.
- Amalgam covers 2022 through the current year for historical files.

Distinct additional holders already present in a target are retained during automatic repair.

## Evidence and limitations

The audit combines Git rename/copy hints, commit-by-commit lineage, direct initial-to-final comparison, and Java member token similarity. Intermediate files removed before the final commit remain in evidence but are not listed as final targets.

Classifications:

- `inherited`: strong material lineage.
- `partial-inherited`: material lineage affecting part of one or both files.
- `manual-review`: optional semantic signal requiring a human decision.
- `no-material-lineage`: omitted from final results.

The result is technical evidence, not an independent legal opinion. Review every final target, lineage route, match, coverage value, and header status before approving an audit.

## Recommended repository sequence

1. Commit the refactoring and audit framework.
2. Record the resulting immutable final SHA.
3. Generate the audit against that SHA.
4. Review reports and evidence.
5. Run `review-audit.py`.
6. Let the script activate, repair, validate, and commit the reviewed result atomically.
