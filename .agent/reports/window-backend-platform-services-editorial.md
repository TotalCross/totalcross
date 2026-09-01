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

Proof completed: focused source-contract tests, copyright/header validation,
scoped whitespace validation, and the permitted macOS Release build for
`tcvm` and `Launcher`. Windows, Linux, Android, iOS, and WinCE builds were not
run under the plan restrictions. No interactive Windows artifact or runnable
macOS keyboard sample was available, so keyboard smoke is deferred.

The functional commit is `8afc9cf8d`. Its title is valid, but its body was
created with a literal escaped newline and exceeds the repository's wrapped
body-line check. Closure records are committed separately; history was not
rewritten because the plan forbids amend or rewrite operations.
