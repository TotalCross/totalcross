---
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

name: logical-commits
description: Create focused Git commits that follow this repository's validated commit-message format, with required scopes, focused validation, and preservation of unrelated local changes; invoke only when the user asks to commit or an active ExecPlan explicitly requires commits.
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

4. Before each commit, run the `validate-headers` skill and the smallest
   validation sufficient for that slice. Always run:

       git diff --check --cached

   Save verbose build output to a log and record only a compact summary.

5. Stage only intended paths:

       git add -- <paths>

   Review:

       git diff --cached --stat
       git diff --cached -- <paths>

6. Write an English commit message in the repository's
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

7. Commit without amending or rewriting history unless explicitly requested.

8. Validate the created commit message against the same rules embedded in
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

9. Update the active ExecPlan state after the logical commit. Update the active
   plan only when this commit reaches a functional-family, architecture, ABI,
   release-policy, or milestone checkpoint.

10. Report the commit hash, subject, paths, focused validations, and any deferred expensive validation. Do not push unless explicitly requested.
