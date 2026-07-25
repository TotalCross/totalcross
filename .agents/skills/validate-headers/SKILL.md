---
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

name: validate-headers
description: Validate the repository's LGPL-2.1-only copyright headers on changed or staged first-party files before a commit; use for new files, header fixes, or copyright-check failures, not for generated or vendored artifacts.
---

# Validate changed-file headers

Use the repository validator from the repository root and the narrowest file set
that matches the intended commit. This repository uses LGPL-2.1-only headers;
do not introduce a different license identifier in first-party files.

1. Identify the scope without dumping the whole worktree:

       git diff --name-only --diff-filter=ACMR --cached -- <task paths>

   If nothing is staged, inspect the intended working-tree paths with:

       git diff --name-only --diff-filter=ACMR -- <task paths>

2. Respect the exclusions implemented by
   `scripts/validate-copyright-headers.sh` and documented in `AGENTS.md`.
   In particular, skip `TotalCrossVM/deps/`, `build/`,
   `TotalCrossSDK/src/main/java/totalcross/util/regex/`, and
   `TotalCrossSDK/src/main/java/totalcross/db/sqlite/`, except for
   `SQLiteUtil.java` and `ui/DBListBox.java`. The validator also ignores
   unsupported file types and `.agent/PLANS.md`. Do not add repository headers
   to upstream, vendored, generated, or exempt files merely to make validation
   pass.

3. Prefer changed-file validation when the validator supports it:

       python3 scripts/validate-copyright-headers.sh --files <changed files>

   For staged changes, omit `--files` so the validator discovers the staged
   paths (or the working-tree diff when nothing is staged):

       python3 scripts/validate-copyright-headers.sh

   It also accepts `--commit <commit>`, or a base and head commit as positional
   arguments. Save verbose output to a temporary log when the file set is
   broad, and report only failures relevant to the intended change.

4. Fix headers with the comment style appropriate to the file. New
   first-party files use the current year and:

       // Copyright (C) 2026 Amalgam Solucoes em TI Ltda
       //
       // SPDX-License-Identifier: LGPL-2.1-only

   Use `#` for shell, Python, Ruby, and YAML files, and an HTML comment for
   Markdown and HTML files. A Markdown skill with YAML frontmatter keeps the
   repository header as YAML comments inside the frontmatter so the metadata
   remains valid.

   For existing files, preserve historical ownership: SuperWaba ranges end in
   2013, TotalCross ranges end in 2021, and Amalgam covers 2022 through the
   current year. For files created from 2014 through 2021, the TotalCross range
   starts in the Git creation year. For files created from 2000 through 2013,
   retain the SuperWaba, TotalCross, and Amalgam sequence required by
   `AGENTS.md`.

5. Rerun the focused check and `git diff --check -- <task paths>`.

6. Report only the files checked, pass/fail status, and log path. Do not paste the full repository validation output.

Do not stage, commit, amend, or push unless the user explicitly requests those actions or the active ExecPlan requires a commit checkpoint.
