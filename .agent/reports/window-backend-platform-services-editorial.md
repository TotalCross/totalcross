<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Window backend/platform-services handoff

The Window native dispatcher now selects the backend with `TC_WINDOWING_*` and
platform services with `TC_OS_*`. SDL owns the device title/window operation;
Windows, Android, iOS, macOS, and Linux services own SIP, orientation, and safe
area behavior through one standardized adapter contract.

Windows SDL and Windows Native share the Windows SIP/TabTip adapter, so SIP
visibility no longer controls SDL text-input state. macOS SDL has an explicit
no-op services adapter, and native macOS cannot fall through to Linux. Android
JNI calls and iOS SIP/safe-area implementations remain available behind their
service adapters; WinCE behavior remains in the Windows-family adapter.

Proof completed locally: eight Window and eight SDL/event source-contract tests,
copyright/header
validation, scoped whitespace and size validation, and the permitted macOS
Release build for `tcvm` and `Launcher`, plus the SDK `dist` build. Windows,
Linux, Android, iOS, and WinCE builds remain deferred under the plan
restrictions. No interactive Windows artifact or runnable macOS keyboard
sample was available, so keyboard smoke is deferred.

The final local revision is `b63a6b64e`, following SIP centralization
`b2a872553`, safe-area fix `183fc28bb`, header fix `ffdab187f`, and rewritten
functional/docs commits `ab85fa051` and `ed78a0d10`. The SIP centralization
tests, corrected header validation, permitted macOS/SDK builds, and pushed
commit all pass.
GitHub run `33533439023` passed for final HEAD, including copyright validation,
iOS, Windows SDL, Windows Native+Legacy, Android, Linux, macOS, and SDK. The
`linux-arm32v7-cross` job was intentionally skipped. The plan is complete;
interactive Windows keyboard smoke remains deferred because no runnable
artifact was available locally.
