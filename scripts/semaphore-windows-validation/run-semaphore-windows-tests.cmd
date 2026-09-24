@rem Copyright (C) 2026 Amalgam Solucoes em TI Ltda
@rem
@rem SPDX-License-Identifier: LGPL-2.1-only

@echo off
setlocal
powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-semaphore-windows-tests.ps1" %*
exit /b %ERRORLEVEL%
