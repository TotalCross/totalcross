<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Release Notes Guide

Release notes and `CHANGELOG.md` serve different purposes in TotalCross and must be maintained together for each release.

## Source files

Human-oriented release notes live at:

```text
docs/releases/<MAJOR>.<MINOR>.<PATCH>.md
```

For example:

```text
docs/releases/7.3.0.md
```

Do not include the `v` prefix in the filename.

The release workflow uses this file verbatim as the GitHub Release body. A release fails if the matching file does not exist, is empty, or does not contain the exact title:

```text
# TotalCross <MAJOR>.<MINOR>.<PATCH>
```

The workflow stages the selected file as `RELEASE-NOTES-v<version>.md` together with the other prepared release assets.

## Relationship with CHANGELOG.md

`CHANGELOG.md` remains the concise historical ledger and follows the repository's Keep a Changelog structure.

Use `CHANGELOG.md` to answer **what changed** using categories such as `Added`, `Changed`, `Deprecated`, `Removed`, `Fixed`, and `Security`.

Use the versioned release-note file to explain:

- why the important changes matter;
- whether developers need to change application code or deployment settings;
- platform and store compatibility implications;
- measured results and their scope;
- migration guidance;
- important limitations;
- significant internal engineering and R&D when useful for understanding the release.

Do not generate one document mechanically from the other. They describe the same release at different levels of detail.

## Recommended structure

Large releases should normally use the following structure. Omit sections that do not apply rather than adding empty boilerplate.

```markdown
# TotalCross X.Y.Z

**Release date:** Month D, YYYY
**Commits since X.Y.Z:** N

<short release summary>

## Migration Summary

## Highlights

### Developer Action Required
### Major New Capabilities
### Major Runtime Improvements

## Changes for SDK Users

### <platform or subsystem>

## Relevant Bug Fixes

## Internal Project Changes

## Dependency Version Audit

## R&D and Experimental Work
```

Patch releases may be shorter when the same information can be communicated clearly with fewer sections.

## Editorial rules

Write release notes for SDK users and application developers rather than as a commit log.

- Describe the net difference from the previous released version.
- Group related implementation PRs into one user-visible change when they are part of the same initiative.
- Do not document regressions introduced and fixed entirely within the same unreleased development cycle as release bug fixes.
- Put breaking changes and required developer actions near the top.
- Separate application-facing changes from project-internal build, CI, architecture, and maintenance work.
- Keep experimental work clearly separated from production capabilities.
- Prefer practical outcomes over implementation details, but include implementation details when they explain compatibility, migration, or important limitations.
- Link relevant issues, pull requests, or commits when they provide useful context or evidence.

## Performance and benchmark claims

Only make performance claims when there is evidence for the final or materially equivalent implementation.

When reporting a benchmark:

- identify the platform and workload;
- report the measured quantity and units;
- distinguish benchmarks from CI/build validation;
- do not generalize a platform-specific result to all platforms;
- do not turn storage-size measurements into whole-application memory claims;
- state important limitations when they affect interpretation.

If a change is expected to improve performance but was not benchmarked, describe the architectural improvement without claiming a measured speedup.

## Platform and store compatibility

When a release responds to Google Play or Apple App Store requirements, state that motivation explicitly.

Keep store requirements distinct from TotalCross support-policy decisions. For example, a Play Store target API requirement and a TotalCross minimum Android API are separate facts.

Verify time-sensitive store requirements against current official documentation while preparing the release notes.

For permissions and privacy changes, explain whether TotalCross merely makes compliance easier or whether the application still has policy/declaration responsibilities of its own.

## Dependencies

Distinguish among:

- an upstream dependency version upgrade;
- replacement of one technology/library by another;
- migration of dependency ownership or packaging;
- a TotalCross depot artifact rebuild/revision.

Depot suffixes such as `-r2` and `-r3` are package revisions and must not be described as newer upstream versions unless the upstream version actually changed.

## Release preparation checklist

Before creating a release:

1. update `CHANGELOG.md` with the concise Keep a Changelog entry;
2. add or update `docs/releases/<version>.md`;
3. verify the release-note version, date, migration guidance, and links;
4. verify benchmark and store-compliance claims against their evidence;
5. ensure experimental work is not presented as a production capability;
6. run the release workflow in dry-run mode before publication when appropriate.

Historical files under this directory preserve the corresponding GitHub Release text so release documentation is versioned with the source tree.
