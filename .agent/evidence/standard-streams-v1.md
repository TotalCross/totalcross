<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Standard streams V1 evidence

- 2026-09-17 | milestone 0 | `git fetch origin perf/image-decode-distributed-benchmark` | pass | refreshed remote base; output had no errors.
- 2026-09-17 | milestone 0 | `git rev-parse origin/perf/image-decode-distributed-benchmark` and `git rev-parse HEAD` | pass | both resolved to `5917a4aa3e20123a1ff02c1a5b0cedd9640c0c6b`.
- 2026-09-17 | milestone 0 | `git switch -c feat/standard-streams-v1 origin/perf/image-decode-distributed-benchmark` | pass | branch created at the recorded base.
- 2026-09-17 | milestone 0 | `git commit -m "docs(agent): add standard streams v1 plan"` | pass with message-format deviation | commit `ed56aff81`; checker reported one body line over 80 characters, and the commit was not amended.
- 2026-09-17 | milestone 1 | `./gradlew-agent test --tests 'jdkcompat.io.PrintStream4DTest' --no-daemon --console=plain` | pass | focused SDK tests passed; full log `/tmp/tc-standard-streams-v1-m1-test.log`.
- 2026-09-17 | milestone 1 | copyright validator and `git diff --check` | pass | `PrintStream4D.java`, its focused test, and plan state/evidence validated.
- 2026-09-17 | milestone 1 | `git commit -m "feat(sdk): complete print stream core api"` | pass with message-format deviation | commit `b8eefe31c`; checker reported one unwrapped 401-character body paragraph, and the commit was not amended.
- 2026-09-17 | milestone 2 | native-method generator commands for declarations and prototypes | pass with scoped recovery | generator completed, but exposed pre-existing unrelated generated-file drift; restored those files and retained only the three focused symbols.
- 2026-09-17 | milestone 2 | `cmake -S TotalCrossVM -B /tmp/tc-standard-streams-v1-m2-macos-20260917 -DCMAKE_BUILD_TYPE=Release -G Ninja` | pass | configure log `/tmp/tc-standard-streams-v1-m2-cmake.log`.
- 2026-09-17 | milestone 2 | `cmake --build /tmp/tc-standard-streams-v1-m2-macos-20260917` | pass | 94/94 build steps; log `/tmp/tc-standard-streams-v1-m2-native-build.log`; only existing warnings were emitted.
- 2026-09-17 | milestone 2 | `./gradlew-agent test --tests 'jdkcompat.io.PrintStream4DTest' --tests 'jdkcompat.lang.System4DTest' --no-daemon --console=plain` | pass | focused SDK tests passed; log `/tmp/tc-standard-streams-v1-m2-test.log` and Gradle agent logs under `TotalCrossSDK/agent-logs/`.
- 2026-09-17 | milestone 2 | copyright validator, `git diff --check`, platform/symbol static checks, and new-file size check | pass | all focused files validated; new files remained below 20 KiB and 600 lines.
- 2026-09-17 | milestone 2 | `git commit -m "feat(runtime): add standard stream router"` | pass | commit `8e644678a`; wrapped body passed the repository hook.
