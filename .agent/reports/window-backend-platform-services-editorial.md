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

Proof completed locally: seven source-contract tests, copyright/header
validation, scoped whitespace and size validation, and the permitted macOS
Release build for `tcvm` and `Launcher`, plus the SDK `dist` build. Windows,
Linux, Android, iOS, and WinCE builds remain deferred under the plan
restrictions. No interactive Windows artifact or runnable macOS keyboard
sample was available, so keyboard smoke is deferred.

The final local revision is `183fc28bb`, following header fix `ffdab187f` and
rewritten functional/docs commits `ab85fa051` and `ed78a0d10`. All four commit
messages pass the repository format check. GitHub has no check-run for
`183fc28bb` and says the SHA is unknown; therefore this handoff is locally
validated but the plan is not complete until the full CI matrix is green.
