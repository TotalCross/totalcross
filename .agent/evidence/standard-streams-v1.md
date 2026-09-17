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
