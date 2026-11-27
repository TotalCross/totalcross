<#
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
#>

[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$OutputDirectory,
    [ValidateSet('I386', 'AMD64')][string]$Architecture = 'AMD64',
    [string]$ToolDirectory = (Join-Path $env:RUNNER_TEMP 'windows-crash-tools')
)

$ErrorActionPreference = 'Stop'
New-Item -ItemType Directory -Force -Path $OutputDirectory, $ToolDirectory | Out-Null

function Write-JsonFile([string]$Path, $Value) {
    ConvertTo-Json -InputObject $Value -Depth 6 |
        Set-Content -LiteralPath $Path -Encoding utf8NoBOM
}

function Find-Cdb([string]$TargetArchitecture) {
    $debuggerArchitecture = if ($TargetArchitecture -eq 'I386') { 'x86' } else { 'x64' }
    $candidates = @(
        "C:\Program Files (x86)\Windows Kits\10\Debuggers\$debuggerArchitecture\cdb.exe",
        "C:\Program Files\Windows Kits\10\Debuggers\$debuggerArchitecture\cdb.exe"
    )
    foreach ($candidate in $candidates) {
        if (Test-Path -LiteralPath $candidate) { return $candidate }
    }
    return $null
}

$procDumpZip = Join-Path $ToolDirectory 'procdump.zip'
$procDumpPath = Join-Path $ToolDirectory 'procdump64.exe'
$procDumpStatus = 'unavailable'
$procDumpError = $null
try {
    Invoke-WebRequest -Uri 'https://download.sysinternals.com/files/Procdump.zip' `
        -OutFile $procDumpZip
    Expand-Archive -LiteralPath $procDumpZip -DestinationPath $ToolDirectory -Force
    if (-not (Test-Path -LiteralPath $procDumpPath)) {
        $procDumpPath = Join-Path $ToolDirectory 'procdump.exe'
    }
    if (-not (Test-Path -LiteralPath $procDumpPath)) {
        throw 'Downloaded archive does not contain ProcDump.'
    }
    $procDumpStatus = 'downloaded'
} catch {
    $procDumpError = $_.Exception.Message
    $procDumpPath = ''
}

$debuggerPath = Find-Cdb $Architecture
$sdkStatus = if ($debuggerPath) { 'already-installed' } else { 'unavailable' }
$sdkBootstrapperPath = Join-Path $ToolDirectory 'winsdksetup.exe'
$sdkBootstrapperSha256 = $null
$sdkExitCode = $null
$sdkError = $null
if (-not $debuggerPath) {
    try {
        Invoke-WebRequest `
            -Uri 'https://download.microsoft.com/download/e6cfffdc-0d3e-4c6c-96d7-d491da459a07/KIT_BUNDLE_WINDOWSSDK_MEDIACREATION/winsdksetup.exe' `
            -OutFile $sdkBootstrapperPath
        $sdkBootstrapperSha256 = (Get-FileHash -LiteralPath $sdkBootstrapperPath `
            -Algorithm SHA256).Hash.ToLowerInvariant()
        $sdkLog = Join-Path $OutputDirectory 'windows-sdk-install.log'
        $sdkInstaller = Start-Process -FilePath $sdkBootstrapperPath `
            -ArgumentList @('/features', 'OptionId.WindowsDesktopDebuggers',
                '/quiet', '/norestart') -PassThru -WindowStyle Hidden
        if ($sdkInstaller.WaitForExit(600000)) {
            $sdkExitCode = $sdkInstaller.ExitCode
            "Windows SDK setup exit code: $sdkExitCode" |
                Set-Content -LiteralPath $sdkLog -Encoding utf8NoBOM
            $debuggerPath = Find-Cdb $Architecture
            $sdkStatus = if ($debuggerPath) { 'installed' } else { 'installer-finished-cdb-missing' }
        } else {
            $sdkInstaller.Kill($true)
            $sdkStatus = 'installer-timeout'
            'Windows SDK setup exceeded the ten-minute limit.' |
                Set-Content -LiteralPath $sdkLog -Encoding utf8NoBOM
        }
    } catch {
        $sdkError = $_.Exception.Message
        $sdkStatus = 'installer-failed'
        "Windows SDK debugger-tools install failed: $sdkError" |
            Set-Content -LiteralPath (Join-Path $OutputDirectory 'windows-sdk-install.log') `
                -Encoding utf8NoBOM
    }
}

$procDumpHash = $null
if ($procDumpPath -and (Test-Path -LiteralPath $procDumpPath)) {
    $procDumpHash = (Get-FileHash -LiteralPath $procDumpPath -Algorithm SHA256).Hash.ToLowerInvariant()
}
$debuggerHash = $null
if ($debuggerPath -and (Test-Path -LiteralPath $debuggerPath)) {
    $debuggerHash = (Get-FileHash -LiteralPath $debuggerPath -Algorithm SHA256).Hash.ToLowerInvariant()
}
$manifest = [ordered]@{
    targetArchitecture = $Architecture
    procDumpStatus = $procDumpStatus
    procDumpPath = if ($procDumpHash) { $procDumpPath } else { $null }
    procDumpExeSha256 = $procDumpHash
    procDumpZipSha256 = if (Test-Path -LiteralPath $procDumpZip) {
        (Get-FileHash -LiteralPath $procDumpZip -Algorithm SHA256).Hash.ToLowerInvariant()
    } else { $null }
    procDumpError = $procDumpError
    debuggerStatus = $sdkStatus
    debuggerPath = if ($debuggerHash) { $debuggerPath } else { $null }
    debuggerSha256 = $debuggerHash
    windowsSdkBootstrapperSha256 = $sdkBootstrapperSha256
    windowsSdkInstallerExitCode = $sdkExitCode
    windowsSdkInstallerError = $sdkError
}
Write-JsonFile (Join-Path $OutputDirectory 'crash-tools.json') $manifest

if (-not [string]::IsNullOrWhiteSpace($env:GITHUB_ENV)) {
    "PROCDUMP_PATH=$($manifest.procDumpPath)" >> $env:GITHUB_ENV
    "DEBUGGER_PATH=$($manifest.debuggerPath)" >> $env:GITHUB_ENV
}
$manifest | ConvertTo-Json -Depth 4
