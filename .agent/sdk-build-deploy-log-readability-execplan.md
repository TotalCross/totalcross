<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Make SDK Build and tc.Deploy Logs Agent-Readable

This ExecPlan is a living document. The sections `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective` must be kept up to date as work proceeds.

This document follows `.agent/PLANS.md`. Keep it self-contained when revising it: a future implementer should be able to start from this file and the current working tree alone.

## Purpose / Big Picture

The SDK build currently succeeds only after temporarily excluding `AnonymousUserDataTest`, but the successful log is still expensive for people and agents to read. After this work, an agent can run one SDK build command, see only a short status line on standard output, and inspect a compact agent log that explains success, failure, warnings, and `tc.Deploy` results without scanning thousands of repetitive lines. Humans can still open the full raw log when they need every detail.

The visible behavior is: from `TotalCrossSDK`, running `./gradlew-agent clean dist` prints only whether the build succeeded or failed, total duration, and relative paths to the full and agent logs. The agent log summarizes Gradle, Java compilation, Javadoc, and `tc.Deploy` output. The default `tc.Deploy` output no longer prints the full classpath or a line for every `Adding ...` entry unless debug logging is requested.

## Progress

- [x] (2026-07-08 14:09 America/Sao_Paulo) Captured a successful baseline log at `TotalCrossSDK/agent-logs/sdk-clean-dist-after-anonymous-test-disable-20260708-full.log` after excluding `AnonymousUserDataTest` from the default Gradle test task.
- [x] (2026-07-08 14:20 America/Sao_Paulo) Re-evaluated priorities from the successful log: `tc.Deploy` output is now the largest readability problem.
- [x] (2026-07-08 15:15 America/Sao_Paulo) Created the `TotalCrossSDK/gradlew-agent` wrapper, verified its no-argument usage contract, and ignored local `agent-logs/`.
- [x] (2026-07-08 15:40 America/Sao_Paulo) Moved the `signJar` warning out of Gradle configuration so ordinary SDK builds no longer emit it.
- [x] (2026-07-08 15:39 America/Sao_Paulo) Added the first structured `tc.Deploy` logging pass: `DeployLogger`, `/log-level`, `/agent-log`, command-line and deploy summaries, and aggregated float-parameter warnings.
- [x] (2026-07-08 15:40 America/Sao_Paulo) Verified that `deployTcbaselang` stays concise at normal log level and restores `Classpath:` plus `Adding ...` output when `/log-level debug` is injected temporarily.
- [x] (2026-07-08 16:02 America/Sao_Paulo) Replaced direct `System.out` and `System.err` usage across `tc.tools.deployer` classes with `DeployLogger`, keeping only the logger's own sink implementation on raw streams.
- [x] (2026-07-08 16:18 America/Sao_Paulo) Implemented the first agent-log summarizer pass in `gradlew-agent`, including failed-task detection and a focused failure excerpt, and validated it on both a successful `compileJava` run and a controlled failing Gradle invocation.
- [x] (2026-07-08 16:21 America/Sao_Paulo) Removed Gradle 10 deprecation noise caused by the SDK build script by replacing deprecated dependency notation, Groovy space-assignment syntax, and `archives` artifact registration, then revalidated `compileJava` with `--warning-mode=all`.
- [x] (2026-07-08 16:26 America/Sao_Paulo) Finished the remaining structured logging control pass by adding explicit log-level checks to `DeployLogger`, routing the remaining `quiet`-gated deploy helper behavior through logger levels, and revalidating with `deployTcbaselang`.
- [x] (2026-07-08 16:32 America/Sao_Paulo) Reduced default `tc.Deploy` output to deployment summaries, key identifiers, and warnings by demoting internal context and secondary metadata to `verbose` or `debug`, then revalidated both the quieter default path and an on-demand debug path.
- [x] (2026-07-08 16:38 America/Sao_Paulo) Cleaned the remaining safe compiler warnings by replacing boxed `Byte` construction with `Byte.valueOf`, annotating the legacy `VirtualKeyboard.updateMessages` API as deprecated, and suppressing the unavoidable Applet and AudioClip removal warnings at the class level, leaving `compileJava` warning-free.
- [ ] Clean Javadoc errors and warnings without disabling doclint.
- [ ] Update `AGENTS.md` to make `gradlew-agent` the preferred SDK build command.
- [ ] Run final validation and update this plan's outcomes.

## Surprises & Discoveries

- Observation: The initial `./gradlew clean dist` failure was not caused by the SDK build or deploy packaging; it was caused by `AnonymousUserDataTest` making live HTTP calls.
  Evidence: The older log `TotalCrossSDK/agent-logs/sdk-clean-dist-20260708-full.log` failed in `:test` with three `FileNotFoundException` entries for `https://aqueous-plateau-93003.herokuapp.com/api/v1/users/check-uuid`.

- Observation: After the test was excluded, `./gradlew clean dist` succeeded and exposed the real full-build log shape.
  Evidence: `TotalCrossSDK/agent-logs/sdk-clean-dist-after-anonymous-test-disable-20260708-full.log` ends with `BUILD SUCCESSFUL in 58s`.

- Observation: In the successful build, the biggest source of log volume is `tc.Deploy`, not Gradle itself.
  Evidence: The successful log has 2314 lines total. It contains 1418 lines starting with `Adding `, 4 very long `Classpath:` lines, 12 `SLF4J:` lines, and 4 `tc.Deploy` JavaExec tasks.

- Observation: Javadoc reports many diagnostics but does not currently fail the build.
  Evidence: The successful log contains `100 errors` and `100 warnings` under `> Task :javadoc`, but the Gradle task continues because `TotalCrossSDK/build.gradle` sets `javadoc { failOnError = false }`.

- Observation: The compile warning count is small enough to fix incrementally after logging behavior is under control.
  Evidence: `compileJava` reports `57 warnings`: 56 removal warnings and 1 dep-ann warning.

- Observation: The first wrapper run keeps the terminal quiet and moves Gradle notices into the agent log.
  Evidence: `TotalCrossSDK/agent-logs/20260708-151556-compileJava-agent.log` shows `status: success`, a short stdout summary, and separate `signing_warnings` and `deprecation_notices` sections while the full log still contains the raw Gradle output.

- Observation: The new deploy logger already restores debug detail on demand while keeping the default deploy output concise.
  Evidence: `TotalCrossSDK/agent-logs/20260708-153940-deployTcbaselang-full.log` shows `Classpath:` and many `Adding ...` lines only after a temporary `'/log-level', 'debug'` edit was added to the `deployTcbaselang` task.

- Observation: The default deploy path is now much shorter, but the SLF4J binder warning is still a separate cleanup item.
  Evidence: `TotalCrossSDK/agent-logs/20260708-154050-deployTcbaselang-full.log` still contains the three `SLF4J:` lines, while `classpath_lines: 0` and `adding_lines: 0` in the agent summary confirm the new logger removed the largest deploy spam at the default level.

- Observation: After replacing the remaining deployer prints with `DeployLogger`, the package still compiles and the `deployTcbaselang` smoke run succeeds.
  Evidence: `./gradlew-agent compileJava` succeeded with log `TotalCrossSDK/agent-logs/20260708-160218-compileJava-full.log`, and `./gradlew-agent deployTcbaselang` succeeded with log `TotalCrossSDK/agent-logs/20260708-160228-deployTcbaselang-full.log`.

- Observation: The wrapper summary is now useful on both success and failure without dumping the whole Gradle tail.
  Evidence: `TotalCrossSDK/agent-logs/20260708-161624-compileJava-agent.log` records task, result, and warning counts for a successful run, while `TotalCrossSDK/agent-logs/20260708-161712-doesNotExist-agent.log` captures the `FAILURE: Build failed with an exception.` block for a controlled Gradle failure.

- Observation: The remaining Gradle warning summary came from deprecated SDK build-script syntax, not from compiler or deploy work.
  Evidence: `TotalCrossSDK/agent-logs/20260708-162015-compileJava-full.log` listed deprecated multi-string dependency notation, deprecated Groovy space-assignment syntax, and deprecated `archives` artifact registration in `TotalCrossSDK/build.gradle`.

- Observation: The build-script deprecation cleanup removed the Gradle warning summary from the wrapper's default agent log.
  Evidence: `TotalCrossSDK/agent-logs/20260708-162124-compileJava-full.log` contains no Gradle deprecation diagnostics even under `--warning-mode=all`, and `TotalCrossSDK/agent-logs/20260708-162134-compileJava-agent.log` has an empty `## Gradle Notices` section.

- Observation: The current wrapper naming scheme can collide when the same task slug is launched more than once in the same second.
  Evidence: Two concurrent `./gradlew-agent compileJava` validations at 16:21 wrote the same `agent-logs/20260708-162107-compileJava-*.log` paths and one run overwrote the other's output.

- Observation: A small part of deploy behavior was still controlled by the old `DeploySettings.quiet` flag even after the logger-level flags existed.
  Evidence: Before this milestone, `Deployer4Android` still gated manifest dumping and bundletool output on `!DeploySettings.quiet`, and `LinuxBuildNatives` still toggled verbosity through `DeploySettings.quiet = false`.

- Observation: The logger-level cleanup did not change the successful default `deployTcbaselang` path.
  Evidence: `TotalCrossSDK/agent-logs/20260708-162541-deployTcbaselang-full.log` still ends with `BUILD SUCCESSFUL in 4s`, while the updated code now checks `DeployLogger.isVerbose()` or `DeployLogger.isEnabled(...)` instead of consulting `quiet` directly in deploy helpers.

- Observation: The default deploy output is now much shorter while still keeping the key packaging result visible.
  Evidence: `TotalCrossSDK/agent-logs/20260708-163104-deployTcbaselang-full.log` shows only the SDK version line, the float warning, the generated TCZ line, and the title/id summary for the default `deployTcbaselang` path; it no longer prints the command line, current folder, etc path, or other secondary metadata at normal level.

- Observation: The detail removed from normal output still comes back when debug logging is requested explicitly.
  Evidence: After a temporary local `'/log-level', 'debug'` injection in the `deployTcbaselang` Gradle task, `TotalCrossSDK/agent-logs/20260708-163154-deployTcbaselang-full.log` again showed `Command line:`, `Current folder:`, `Etc directory:`, `Classpath:`, and many `Adding ...` lines before the temporary edit was reverted.

- Observation: The compiler warning cleanup reached zero warnings without changing the legacy compatibility surface.
  Evidence: `TotalCrossSDK/agent-logs/20260708-163851-deployTcbaselang-agent.log` reports `compileJava_warnings: 0`, and the raw compile section now contains only compiler notes after the `Byte`, `Applet`, `AudioClip`, and deprecated API annotations were adjusted.

## Decision Log

- Decision: Treat `AnonymousUserDataTest` correction and reactivation as a separate future task, not part of this log-readability implementation.
  Rationale: The user explicitly chose to disable it directly in `build.gradle` and fix it later. Keeping it out of this plan prevents the log work from being blocked by network-test redesign.
  Date/Author: 2026-07-08 / Codex.

- Decision: Prioritize `tc.Deploy` output reduction before Javadoc cleanup.
  Rationale: The successful baseline shows 1418 repetitive `Adding ...` lines, while Javadoc diagnostics are noisy but non-fatal. Reducing `tc.Deploy` output gives the largest immediate readability gain for full `clean dist`.
  Date/Author: 2026-07-08 / Codex.

- Decision: Keep the raw full log intact and generate a second, smaller agent log rather than filtering away information permanently.
  Rationale: Full logs remain necessary for debugging. The agent log is a readable index and summary, not the only source of truth.
  Date/Author: 2026-07-08 / Codex.

- Decision: Do not hide Javadoc diagnostics with `-Xdoclint:none`.
  Rationale: The user wants errors and warnings corrected where possible. Suppressing doclint would make the build quieter but would not improve code or documentation quality.
  Date/Author: 2026-07-08 / Codex.

- Decision: Keep the new deploy logger layered on top of the existing quiet flag instead of replacing the old behavior in one sweep.
  Rationale: The repository has many legacy deploy helpers that still check `DeploySettings.quiet`. A layered approach lets the common high-volume messages move to explicit levels now while the older callers stay stable until a later pass.
  Date/Author: 2026-07-08 / Codex.

- Decision: Treat the SLF4J binder warning as a separate follow-up after the deploy logger pass.
  Rationale: The new logger work already made the biggest deploy noise disappear. Keeping the remaining SLF4J cleanup separate makes it easier to verify that the logging changes themselves are correct before changing runtime dependencies.
  Date/Author: 2026-07-08 / Codex.

- Decision: Leave `System.out` and `System.err` only inside `DeployLogger` itself.
  Rationale: The logger is the sink that ultimately writes to stdout, stderr, and the optional agent log file. Replacing those internal writes with logger methods would recurse and break output delivery.
  Date/Author: 2026-07-08 / Codex.

## Outcomes & Retrospective

The wrapper milestone is complete, the Gradle configuration warning from `signJar` now waits until the task actually runs, and the first structured deploy logging pass is in place. The wrapper now also emits an agent summary with task lists, compile and Javadoc counts, deploy high-volume counters, and a focused failure excerpt instead of a blind tail. The SDK build script no longer emits the Gradle 10 deprecation summary on ordinary runs, because the deprecated dependency declarations, property assignments, and `archives` usage were replaced with current Gradle DSL. The remaining deploy helpers that still depended on the legacy `quiet` flag now derive their behavior from `DeployLogger` levels instead, which keeps `/log-level` as the single control surface for deploy verbosity. Default `tc.Deploy` output is now closer to a result summary: key identifiers, warnings, and generated-artifact lines stay visible, while command-line details, path discovery, classpath dumps, and secondary metadata move to `verbose` or `debug`. Compiler warnings are now down to zero, with only non-fatal compiler notes remaining. The remaining immediate work is the SLF4J binder noise and the Javadoc cleanup. Update this section after each milestone with what changed, what was validated, and which risks remain.

## Context and Orientation

The repository root is `/Users/flsobral/repos/totalcross-github`. The SDK project lives in `TotalCrossSDK`. Its Gradle build file is `TotalCrossSDK/build.gradle`; the Gradle wrapper is `TotalCrossSDK/gradlew`; the SDK distribution output is `TotalCrossSDK/dist`; and generated build outputs live under `TotalCrossSDK/build`.

The SDK build task `dist` depends on `build`, `copySdkDependencies`, `sourcesJar`, and `javadocJar`. The `build` task is finalized by four deploy tasks: `deployTcbaselang`, `deployTcbaseutil`, `deployTcbasemisc`, and `deployTcui`. Those deploy tasks invoke the Java main class `tc.Deploy`, defined in `TotalCrossSDK/src/main/java/tc/Deploy.java`.

`tc.Deploy` converts classes and resources into TotalCross `.tcz` files. Its converter code is mainly under `TotalCrossSDK/src/main/java/tc/tools/converter`, and deployment helpers live under `TotalCrossSDK/src/main/java/tc/tools/deployer`. The current output is split between `System.out.println`, `System.out.print`, `System.err.println`, and `Utils.println`, so output control is inconsistent. `DeploySettings.quiet` currently exists, but several high-volume messages bypass it.

`TotalCrossSDK/src/main/java/tc/tools/converter/J2TC.java` prints `Adding ...` for every class or resource processed. `TotalCrossSDK/src/main/java/tc/tools/deployer/DeploySettings.java` prints the full classpath on every deploy. `TotalCrossSDK/src/main/java/tc/tools/converter/java/JavaMethod.java` prints repeated float-parameter warnings directly. `TotalCrossSDK/src/main/java/tc/tools/deployer/Utils.java` has `println` and `warn` helpers, but they are not enough to express levels or write an agent log.

The current successful baseline log is `TotalCrossSDK/agent-logs/sdk-clean-dist-after-anonymous-test-disable-20260708-full.log`. This directory is local working output and should not be committed. The plan must add `agent-logs/` to `TotalCrossSDK/.gitignore`.

## Plan of Work

The first milestone creates `TotalCrossSDK/gradlew-agent`, a Bash wrapper beside `gradlew`. The wrapper requires explicit Gradle arguments; if called without arguments it prints a short usage message and exits with code 2. With arguments, it runs `./gradlew "$@"` after adding `--console=plain`, `--warning-mode=summary`, and `--no-problems-report` unless the caller already supplied an equivalent option. It writes the full combined stdout and stderr to `TotalCrossSDK/agent-logs/<timestamp>-<task-slug>-full.log` and writes a compact summary to `TotalCrossSDK/agent-logs/<timestamp>-<task-slug>-agent.log`. Standard output from the wrapper contains only status, duration, and relative log paths.

The agent log summarizer should be implemented inside the Bash script using portable shell, `awk`, `sed`, and `grep`/`rg` only where available. It must not require Python or external dependencies. It should summarize the result, elapsed time, Gradle tasks seen, failed tasks, compile warning counts, Javadoc error and warning counts by message, `tc.Deploy` high-volume counts, and the last relevant failure block when the build fails. The full log remains unmodified.

The second milestone removes Gradle-side noise that is not meaningful to a normal SDK build. In `TotalCrossSDK/build.gradle`, the current `signJar` task emits `TC_PATH_TO_KEY_STORE or TC_STORE_PASSWORD or TC_STORE_KEY not defined! Jar won't be signed` during project configuration. Move that warning into the actual task action or only emit it when `signJar` is requested. The normal `clean dist` build must not print a signing warning for a task that is not being executed. The wrapper's default `--no-problems-report` should suppress the `[Incubating] Problems report is available` line for ordinary agent builds.

The third milestone adds a central deploy logger. Create a small class under `TotalCrossSDK/src/main/java/tc/tools/deployer`, for example `DeployLogger`, with levels `QUIET`, `NORMAL`, `VERBOSE`, and `DEBUG`. It should provide methods for normal info, warnings, debug messages, and agent-log-only details. Add fields to `DeploySettings` for the current log level and optional agent log path. In `tc.Deploy.parseOptions`, add `/log-level quiet|normal|verbose|debug` and `/agent-log <path>`. Keep `/v` as a compatibility alias for verbose output. Invalid levels must throw `DeployerException` with a clear message.

The fourth milestone routes the noisy deploy output through the logger. Replace direct printing of `Command line:`, `Classpath:`, `Adding ...`, `Total References`, generated TCZ files, application metadata, float-parameter warnings, and platform output with logger calls. In normal output, show start context, generated file summaries, application id/title, warnings, and fatal errors. Move full classpath and per-entry `Adding ...` lines to debug. For float warnings, print the long explanation once and aggregate affected methods; debug output may include every occurrence. Full details must still be present in the raw Gradle full log when debug is selected and in the optional deploy agent log when `/agent-log` is used.

The fifth milestone removes SLF4J binder noise. Add a compatible `slf4j-nop` runtime dependency or otherwise provide a no-op binding that matches the existing SLF4J API version pulled into the SDK runtime classpath. Validate that the three-line `SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder"` block no longer appears during `deployTcbaselang` or `clean dist`.

The sixth milestone cleans Java compiler warnings. Replace deprecated boxed constructors such as `new Integer(...)`, `new Long(...)`, `new Double(...)`, `new Float(...)`, `new Byte(...)`, and `new Character(...)` with `valueOf` or equivalent parsing when behavior is unchanged. Add missing `@Deprecated` annotations where the API is already documented as deprecated. For Applet and AudioClip compatibility, use targeted `@SuppressWarnings("removal")` only if the API must remain. Run `./gradlew-agent compileJava` after each batch.

The seventh milestone cleans Javadoc diagnostics without disabling doclint. Start with fatal-looking errors by type: malformed HTML, bad HTML entities, semicolon missing inside documentation comments, invalid tags such as `</br>`, unknown tags such as `<rec>`, unsupported HTML5 attributes in tables, broken references, and unclosed `<pre>`. Then fix warning groups such as empty `<p>`, nested `<code>`, missing `@param`, missing `@return`, no comment, and no description for `@param`. Run `./gradlew-agent javadoc` until it reports zero errors and zero warnings.

The eighth milestone updates agent documentation. In `AGENTS.md`, change SDK build instructions to prefer `cd TotalCrossSDK` followed by `./gradlew-agent clean dist`. Document that agents should read the `*-agent.log` first and open the `*-full.log` only for deeper diagnosis. Update deploy/smoke guidance so direct `tc.Deploy` invocations use `/agent-log` when log analysis is expected.

At the end of each milestone, validate the copyright headers of every modified file against `AGENTS.md`, correct any stale year range or missing Amalgam line, and only then stage the files changed by that milestone. Finish with a descriptive commit message. Do not stage local logs or other generated artifacts unless a milestone explicitly says to promote them.

## Concrete Steps

Begin from the repository root:

    cd /Users/flsobral/repos/totalcross-github

Before editing, inspect the current relevant files:

    sed -n '1,220p' TotalCrossSDK/build.gradle
    sed -n '1,560p' TotalCrossSDK/src/main/java/tc/Deploy.java
    sed -n '1,320p' TotalCrossSDK/src/main/java/tc/tools/deployer/DeploySettings.java
    sed -n '900,970p' TotalCrossSDK/src/main/java/tc/tools/converter/J2TC.java
    sed -n '100,125p' TotalCrossSDK/src/main/java/tc/tools/converter/java/JavaMethod.java
    sed -n '300,345p' AGENTS.md

Create `TotalCrossSDK/gradlew-agent` with the current-year Amalgam header as shell comments. Make it executable. Add `agent-logs/` to `TotalCrossSDK/.gitignore`. Validate the no-argument behavior:

    cd TotalCrossSDK
    ./gradlew-agent

Expected output is a short usage message and exit code 2. Then validate a simple task:

    ./gradlew-agent compileJava

Expected standard output shape:

    status=success
    duration_seconds=<number>
    full_log=agent-logs/<timestamp>-compileJava-full.log
    agent_log=agent-logs/<timestamp>-compileJava-agent.log

The exact timestamp and duration will vary. The full log should contain ordinary Gradle output; the agent log should contain a compact summary.

After the wrapper exists, use it for all further validation in this plan unless debugging the wrapper itself requires direct `./gradlew`.

Implement Gradle-side cleanup in `TotalCrossSDK/build.gradle`, then run:

    ./gradlew-agent clean dist

The agent log should not include `TC_PATH_TO_KEY_STORE or TC_STORE_PASSWORD or TC_STORE_KEY not defined! Jar won't be signed` unless `signJar` was requested. It should not include the Gradle Problems report line because the wrapper passes `--no-problems-report`.

Implement `DeployLogger` and CLI parsing in `tc.Deploy`, then run:

    ./gradlew-agent deployTcbaselang

The normal deploy output inside the full Gradle log should no longer include the full `Classpath:` line or per-entry `Adding ...` lines. To verify that debug detail still exists, make a temporary local edit to the `deployTcbaselang` task in `TotalCrossSDK/build.gradle`, appending `'/log-level', 'debug'` to that task's `args` list. Run `./gradlew-agent deployTcbaselang` and confirm the full log contains `Classpath:` and `Adding ...` lines. Revert only that temporary debug-argument edit before committing. The durable acceptance is that `/log-level debug` preserves detailed deploy output when used, while the committed default remains concise.

Fix compiler warnings in small batches and validate:

    ./gradlew-agent compileJava

Fix Javadoc diagnostics in batches and validate:

    ./gradlew-agent javadoc

Finally run the full build:

    ./gradlew-agent clean dist

The final wrapper stdout must remain short. The final agent log should say the build succeeded and should summarize zero compile warnings if all safe compiler warnings were fixed, zero Javadoc errors and warnings after documentation cleanup, and concise `tc.Deploy` output without repetitive `Adding ...` lines.

## Validation and Acceptance

The implementation is accepted when the following commands pass from `TotalCrossSDK`:

    ./gradlew-agent compileJava
    ./gradlew-agent javadoc
    ./gradlew-agent deployTcbaselang
    ./gradlew-agent deployModernJavaFeatureSmoke
    ./gradlew-agent clean dist

Run this repository-level check from the root:

    git diff --check

For `./gradlew-agent clean dist`, standard output must contain only success or failure, duration, and the relative paths to the two generated logs. It must not stream Gradle task output, Javadoc diagnostics, or `tc.Deploy` details to the terminal.

The generated full log must contain the complete raw output from Gradle. The generated agent log must be small enough to read quickly and must include at least: exit status, duration, task list, failed task and error excerpt if failed, compile warning counts, Javadoc diagnostics summary, deploy file summaries, paths to generated TCZ outputs, and high-volume counters such as omitted `Adding ...` lines.

The default full build must no longer fail because of `AnonymousUserDataTest`; that test remains intentionally excluded until a separate future task fixes its network dependency. The final logs must not contain the old Heroku `FileNotFoundException` failure.

The default `tc.Deploy` output must not print full classpaths or one `Adding ...` line per class/resource. When `/log-level debug` is used, those details must remain available.

The final build should not contain Javadoc errors or warnings. If any Javadoc diagnostic remains because fixing it would alter public documentation meaning or require a broader documentation rewrite, update this ExecPlan with the reason, the exact remaining message, and a follow-up task before marking the milestone complete.

## Idempotence and Recovery

The wrapper must be safe to run repeatedly. It creates new timestamped log files under `TotalCrossSDK/agent-logs/` and must not overwrite previous logs unless the timestamp and task slug collide, which should be avoided by including seconds in the name. The directory is ignored by Git and can be deleted manually if local disk usage grows.

Do not remove or revert unrelated user changes. This repository often has generated outputs and local artifacts. Use scoped status and diffs, for example:

    git status --short -- TotalCrossSDK/build.gradle TotalCrossSDK/src/main/java/tc TotalCrossSDK/src/main/java/tc/tools .agent AGENTS.md
    git diff -- TotalCrossSDK/build.gradle

If a validation command fails because the Gradle cache under `~/.gradle` is not writable in a sandbox, rerun the same command with the appropriate approval rather than changing build behavior. If `clean dist` fails because of native dependency fetching or local environment requirements, inspect the agent log first and record the exact failure in `Surprises & Discoveries`.

If a Javadoc fix produces a suspicious documentation change, prefer preserving the original visible text with escaped HTML entities or `{@code ...}` rather than deleting content. If a compiler warning fix touches compatibility classes such as Applet or AudioClip support, prefer targeted suppression over removing legacy API surface.

When a milestone is complete, first review the copyright header of each file you touched and update it to the current-year form required by `AGENTS.md`. After that, use `git add` on the related source and plan files only, confirm the staged diff is the intended one, and then end that operation with a commit message that clearly names the change. Local logs under `TotalCrossSDK/agent-logs/` stay untracked.

Revision note (2026-07-08): Added an explicit pre-staging copyright-header validation step because this repository requires current-year header maintenance on every touched file, and staged changes should already satisfy that rule before commit preparation.

## Artifacts and Notes

The successful baseline log used to shape this plan is local and should not be committed:

    TotalCrossSDK/agent-logs/sdk-clean-dist-after-anonymous-test-disable-20260708-full.log

Key baseline evidence:

    BUILD SUCCESSFUL in 58s
    2314 total lines
    1418 lines starting with "Adding "
    4 lines starting with "Classpath:"
    12 lines starting with "SLF4J:"
    57 compile warnings
    100 Javadoc errors
    100 Javadoc warnings

The older failed baseline is also local and should not be committed:

    TotalCrossSDK/agent-logs/sdk-clean-dist-20260708-full.log

It failed before deploy tasks completed because `AnonymousUserDataTest` attempted live HTTP calls to a Heroku endpoint. That failure is not part of this implementation beyond preserving the temporary exclusion already requested by the user.

## Interfaces and Dependencies

Create `TotalCrossSDK/gradlew-agent` as an executable Bash script. It must accept arbitrary Gradle arguments and forward them to `./gradlew`. It must inject these Gradle flags unless the caller already supplied a conflicting or equivalent flag:

    --console=plain
    --warning-mode=summary
    --no-problems-report

Its output contract is stable:

    status=<success|failure>
    duration_seconds=<integer>
    full_log=<relative path>
    agent_log=<relative path>

The script exits with the same code as Gradle, except no-argument usage exits with 2.

Create or update Java logging support under `tc.tools.deployer`. The logger must expose four levels:

    QUIET: only fatal errors and explicit forced messages.
    NORMAL: concise build/deploy progress, generated files, warnings, and errors.
    VERBOSE: normal output plus useful diagnostic context.
    DEBUG: verbose output plus classpath, per-entry conversion lines, and detailed converter internals.

`tc.Deploy` must accept:

    /log-level quiet
    /log-level normal
    /log-level verbose
    /log-level debug
    /agent-log <path>

`/v` remains supported and maps to verbose. Existing platform flags and options must keep their behavior.

The agent log format does not need to be JSON in the first implementation. Plain Markdown-like text is acceptable and preferable for human/agent reading. If a later implementation needs machine parsing, add a versioned format section to this ExecPlan before changing the output contract.

## Revision Notes

2026-07-08 / Codex: Initial ExecPlan created from the successful `clean dist` log after `AnonymousUserDataTest` was excluded. The plan prioritizes `tc.Deploy` log reduction first because the successful log shows it dominates output volume.

2026-07-08 / Codex: Added explicit end-of-milestone staging and commit instructions so each completed operation closes with a focused `git add` and a descriptive commit message, while keeping generated logs out of version control.

2026-07-08 / Codex: Marked the wrapper-and-ignore milestone complete after validating `./gradlew-agent` usage and `compileJava`. The plan now records that the wrapper emits a short stdout summary and moves Gradle notices into the agent log.
