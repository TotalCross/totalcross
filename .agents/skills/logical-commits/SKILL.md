---
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

name: logical-commits
description: Create focused Git commits with exact staged source validation, the repository's validated commit-message format, required scopes, and preservation of unrelated local changes; invoke only when the user asks to commit or an active ExecPlan explicitly requires commits.
---

# Commit logical repository changes

This skill changes Git state. Confirm that committing is explicitly requested by the user or required by an active ExecPlan whose execution was requested.

1. Read the active ExecPlan state first when executing an ExecPlan. Determine
   the current functional slice and intended paths. Follow `.agent/PLANS.md`
   when a plan is present.

2. Inspect scoped changes:

       git status --short -- <task paths>
       git diff --stat -- <task paths>
       git diff -- <task paths>

   Inspect staged changes separately. Do not run destructive cleanup and do not include unrelated modifications.

3. Split changes by behavior or contract, not by file count. Suitable commit boundaries include:

   - one shared interface plus its focused tests;
   - one platform-family migration;
   - one dependency scaffold and its documentation;
   - one release-policy change and its idempotence tests;
   - one follow-up fix discovered by validation.

   Do not create a commit for formatting-only fragments that belong to the same functional change. Do not combine architecture, unrelated cleanup, generated artifacts, and documentation for another feature.

4. Perform any required header fixes in the working tree before staging. Use
   the `validate-source-files` skill and a focused header check when needed:

       python3 scripts/validate-source-files.py \
           --check headers --files <intended paths>

   If it reports a fixable header mismatch, run:

       python3 scripts/validate-source-files.py \
           --check headers --fix --files <intended paths>

5. Stage only intended paths:

       git add -- <paths>

6. Before every commit, invoke the `validate-source-files` skill against the
   exact staged source slice. This is the authoritative pre-commit gate and
   includes both copyright/provenance and source-size checks:

       python3 scripts/validate-source-files.py \
           --staged --files <intended staged paths>

   If header validation fails, fix the working-tree file, restage only that
   path, and rerun staged validation. If size validation fails, do not commit,
   create an exception, or raise a baseline. Split, extract, refactor, or reduce
   the staged source until it passes, then rerun the gate.

   Growth of 1 through 500 bytes in a source file already above 20 KiB passes
   with a warning. Include that warning in the compact validation report and
   continue; do not require an unrelated refactor solely to remove it.

   Run the smallest additional validation sufficient for the logical slice.
   Save verbose build output to a log and record only a compact summary.

7. Check and review the exact staged result:

       git diff --check --cached

       git diff --cached --stat
       git diff --cached -- <paths>

8. Write an English commit message in the repository's
   Conventional-Commits-inspired format:

       <type>(<scope>): imperative description
       <type>(<scope>,<platform>): imperative description
       <type>!(<scope>[,<platform>]): imperative description
       <type>(<scope>[,<platform>])!: imperative description

   Use only these types: `fix`, `feat`, `refactor`, `perf`, `style`, `test`,
   `docs`, `build`, `ci`, `chore`, or `revert`. The scope is required and may
   contain letters, numbers, underscores, and hyphens. The optional platform
   must be lowercase and is the second qualifier. Architecture is not a title
   qualifier; mention it in the body instead.

   The title must be English, start with a lowercase letter, use the
   imperative mood, contain at least three words, be at most 80 characters,
   and not end with a period. If there is a body, separate it from the title
   with one blank line. Keep every body line at 80 characters or less; the CI
   check ignores Markdown link URLs when measuring body lines.

   For every non-trivial change, add a body that explains:

   - why the change is needed;
   - what behavior or contract changes;
   - platform, compatibility, artifact, or release impact;
   - focused validation completed and important deferrals.

   Example:

       refactor(build): centralize native target policy

       Resolve Android, Linux, and Windows platform settings from the shared
       native-build configuration so workflows and explicit target wrappers no
       longer repeat toolchain values.

       Keep minizip on Android API 24 while the default remains API 23. Validate
       configuration resolution and the zlib build-only workflow.

   For breaking changes, put `!` in the title and add a `BREAKING CHANGE:`
   footer describing the compatibility impact. For `revert` commits, name the
   reverted commit hash or hashes in the body.

9. Commit without amending or rewriting history unless explicitly requested.

10. Validate the created commit message against the same rules embedded in
   `.github/workflows/commit.yml`. There is no standalone repository script;
   run this focused local check:

       python3 - <<'PY'
       import re
       import subprocess
       import sys

       message = subprocess.check_output(["git", "show", "-s", "--format=%B", "HEAD"], text=True).rstrip("\n")
       title = message.split("\n", 1)[0]
       title_format = re.compile(
           r"^(fix|feat|refactor|perf|style|test|docs|build|ci|chore|revert)"
           r"(!\([A-Za-z0-9_-]+(,[a-z0-9_-]+)?\)|"
           r"\([A-Za-z0-9_-]+(,[a-z0-9_-]+)?\)!?): [a-z0-9 ].*$"
       )
       link_urls_removed = lambda line: re.sub(r"\[([^\]]*)\]\([^)]+\)", r"\1", line)
       failures = []
       if not title_format.match(title): failures.append("invalid title format")
       if re.search(r"[A-Z]", title[:1]): failures.append("title starts uppercase")
       if not re.match(r"^[^ ]+([ \t]+[^ ]+){2,}$", title): failures.append("title has fewer than 3 words")
       if len(title) > 80: failures.append("title exceeds 80 characters")
       if title.endswith("."): failures.append("title ends with a period")
       if len(message) > len(title) and not message.startswith(title + "\n\n"):
           failures.append("body is not separated by a blank line")
       if any(len(link_urls_removed(line)) > 80 for line in message.split("\n")[1:]):
           failures.append("body line exceeds 80 characters")
       if failures:
           print("; ".join(failures), file=sys.stderr)
           sys.exit(1)
       print("Commit message validation passed.")
       PY

   This mirrors the workflow's structural checks for the current `HEAD`.
   Keep the title and body in English and use the repository's imperative
   wording convention; CI remains the authoritative automated check.

11. Update the active ExecPlan state after the logical commit. Update the active
   plan only when this commit reaches a functional-family, architecture, ABI,
   release-policy, or milestone checkpoint.

12. Report the commit hash, subject, paths, focused validations, and any
    deferred expensive validation. Do not push unless explicitly requested.
