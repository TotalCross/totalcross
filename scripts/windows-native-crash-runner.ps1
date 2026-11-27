# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$Executable,
    [Parameter(Mandatory = $true)][string]$OutputDirectory,
    [Parameter(Mandatory = $true)][string]$SourceCommit,
    [ValidateSet('I386', 'AMD64')][string]$Architecture = 'AMD64',
    [string]$ProcdumpPath = '',
    [string]$DebuggerPath = '',
    [Parameter(Mandatory = $true)][string]$PdbDirectory,
    [int]$TimeoutSeconds = 120
)

$ErrorActionPreference = 'Stop'

function Write-JsonFile([string]$Path, $Value) {
    ConvertTo-Json -InputObject $Value -Depth 10 |
        Set-Content -LiteralPath $Path -Encoding utf8NoBOM
}

function Get-UserObjectName([IntPtr]$Handle) {
    if ($Handle -eq [IntPtr]::Zero) { return $null }
    $buffer = New-Object System.Text.StringBuilder 1024
    $needed = 0
    $ok = [WindowsDesktopProbe]::GetUserObjectInformation(
        $Handle, 2, $buffer, 1024, [ref]$needed)
    if ($ok) { return $buffer.ToString() }
    return $null
}

function New-CleanProcessStartInfo([string]$FileName, [string]$WorkingDirectory,
                                   [string[]]$Arguments, [string]$PathValue,
                                   [switch]$UnicodeOutput) {
    $info = [System.Diagnostics.ProcessStartInfo]::new()
    $info.FileName = $FileName
    $info.WorkingDirectory = $WorkingDirectory
    $info.UseShellExecute = $false
    $info.CreateNoWindow = $true
    $info.RedirectStandardOutput = $true
    $info.RedirectStandardError = $true
    if ($UnicodeOutput) {
        $info.StandardOutputEncoding = [Text.Encoding]::Unicode
        $info.StandardErrorEncoding = [Text.Encoding]::Unicode
    }
    $info.EnvironmentVariables.Clear()
    foreach ($name in @('SystemRoot', 'WINDIR', 'SystemDrive', 'COMSPEC',
                         'HOMEDRIVE', 'HOMEPATH', 'USERPROFILE')) {
        $value = [Environment]::GetEnvironmentVariable($name)
        if (-not [string]::IsNullOrWhiteSpace($value)) {
            $info.EnvironmentVariables[$name] = $value
        }
    }
    $info.EnvironmentVariables['TEMP'] = $WorkingDirectory
    $info.EnvironmentVariables['TMP'] = $WorkingDirectory
    $info.EnvironmentVariables['PATH'] = $PathValue
    foreach ($argument in $Arguments) { $info.ArgumentList.Add($argument) }
    return $info
}

function Write-DependencyReport([string]$ExePath, [string]$VmPath, [string]$ReportPath) {
    $vswhere = 'C:\Program Files (x86)\Microsoft Visual Studio\Installer\vswhere.exe'
    $dumpbin = $null
    if (Test-Path -LiteralPath $vswhere) {
        $installPath = & $vswhere -latest -products '*' -property installationPath 2>$null
    }
    if (-not [string]::IsNullOrWhiteSpace($installPath)) {
        $dumpbin = Get-ChildItem -LiteralPath (Join-Path $installPath 'VC\Tools\MSVC') `
            -Filter dumpbin.exe -Recurse -ErrorAction SilentlyContinue |
            Where-Object { $_.FullName -match 'Hostx64\\x64\\dumpbin\.exe$' } |
            Select-Object -First 1
    }
    $content = [System.Collections.Generic.List[string]]::new()
    if ($null -eq $dumpbin) {
        $content.Add('dumpbin unavailable; import table could not be recorded')
    } else {
        foreach ($path in @($ExePath, $VmPath)) {
            $content.Add("=== $([IO.Path]::GetFileName($path)) ===")
            $content.Add((& $dumpbin.FullName /DEPENDENTS $path 2>&1 | Out-String))
        }
    }
    $content | Set-Content -LiteralPath $ReportPath -Encoding utf8NoBOM
}

$out = [IO.Path]::GetFullPath($OutputDirectory)
$exe = [IO.Path]::GetFullPath($Executable)
$workingDirectory = [IO.Path]::GetDirectoryName($exe)
$vm = Join-Path $workingDirectory 'tcvm.dll'
$dumpDirectory = Join-Path $out 'dumps'
New-Item -ItemType Directory -Force -Path $out, $dumpDirectory | Out-Null
if (-not (Test-Path -LiteralPath $exe)) { throw "Launcher executable not found: $exe" }

$desktopType = @'
using System;
using System.Runtime.InteropServices;
using System.Text;
public static class WindowsDesktopProbe {
  [DllImport("user32.dll", SetLastError = true)]
  public static extern IntPtr GetProcessWindowStation();
  [DllImport("user32.dll", SetLastError = true)]
  public static extern IntPtr GetThreadDesktop(uint threadId);
  [DllImport("kernel32.dll")]
  public static extern uint GetCurrentThreadId();
  [DllImport("user32.dll", CharSet = CharSet.Auto, SetLastError = true)]
  public static extern bool GetUserObjectInformation(IntPtr handle, int index,
      StringBuilder info, int length, ref int needed);
}
'@
Add-Type -TypeDefinition $desktopType -ErrorAction SilentlyContinue

$windowStationName = Get-UserObjectName ([WindowsDesktopProbe]::GetProcessWindowStation())
$threadDesktopName = Get-UserObjectName (
    [WindowsDesktopProbe]::GetThreadDesktop([WindowsDesktopProbe]::GetCurrentThreadId()))
$videoControllers = @()
try {
    $videoControllers = @(Get-CimInstance Win32_VideoController -ErrorAction Stop |
        Select-Object Name, DriverVersion, CurrentHorizontalResolution,
            CurrentVerticalResolution)
} catch { }
$desktopFacts = [ordered]@{
    osVersion = [Environment]::OSVersion.VersionString
    runnerImageOS = $env:ImageOS
    runnerImageVersion = $env:ImageVersion
    userInteractive = [Environment]::UserInteractive
    runnerOS = $env:RUNNER_OS
    runnerArch = $env:RUNNER_ARCH
    sessionId = (Get-Process -Id $PID).SessionId
    windowStation = $windowStationName
    threadDesktop = $threadDesktopName
    explorerProcessCount = @(Get-Process explorer -ErrorAction SilentlyContinue).Count
    dwmProcessCount = @(Get-Process dwm -ErrorAction SilentlyContinue).Count
    videoControllers = $videoControllers
    application = [IO.Path]::GetFileName($exe)
}
Write-JsonFile (Join-Path $out 'runner-environment.json') $desktopFacts
Write-DependencyReport $exe $vm (Join-Path $out 'native-dependencies.txt')

$werKey = 'HKCU:\Software\Microsoft\Windows\Windows Error Reporting\LocalDumps\' +
    [IO.Path]::GetFileName($exe)
New-Item -Path $werKey -Force | Out-Null
New-ItemProperty -Path $werKey -Name DumpFolder -Value $dumpDirectory `
    -PropertyType ExpandString -Force | Out-Null
New-ItemProperty -Path $werKey -Name DumpCount -Value 2 -PropertyType DWord -Force | Out-Null
New-ItemProperty -Path $werKey -Name DumpType -Value 2 -PropertyType DWord -Force | Out-Null

$cleanPath = @($workingDirectory, "$env:SystemRoot\System32", $env:SystemRoot,
    "$env:SystemRoot\System32\Wbem") -join ';'
$procdump = $null
$procdumpOutputTask = $null
$procdumpErrorTask = $null
$procdumpMode = 'unavailable'
$procdumpInstallExitCode = $null
$procdumpCleanupStatus = 'not-installed'
$procdumpPathAvailable = -not [string]::IsNullOrWhiteSpace($ProcdumpPath) -and
    (Test-Path -LiteralPath $ProcdumpPath)
$procdumpLog = Join-Path $out 'procdump.log'
$procdumpErrorLog = Join-Path $out 'procdump-stderr.log'
if ($procdumpPathAvailable) {
    try {
        $procdumpArguments = @('-accepteula', '-ma', '-i', $dumpDirectory)
        $procdumpInfo = New-CleanProcessStartInfo -FileName $ProcdumpPath `
            -WorkingDirectory $dumpDirectory -Arguments $procdumpArguments `
            -PathValue $cleanPath -UnicodeOutput
        $procdump = [System.Diagnostics.Process]::Start($procdumpInfo)
        $procdumpOutputTask = $procdump.StandardOutput.ReadToEndAsync()
        $procdumpErrorTask = $procdump.StandardError.ReadToEndAsync()
        if ($procdump.WaitForExit(30000)) {
            $procdumpInstallExitCode = $procdump.ExitCode
            $procdumpOutputTask.GetAwaiter().GetResult() |
                Set-Content -LiteralPath $procdumpLog -Encoding utf8NoBOM
            $procdumpErrorTask.GetAwaiter().GetResult() |
                Set-Content -LiteralPath $procdumpErrorLog -Encoding utf8NoBOM
            if ($procdumpInstallExitCode -eq 0) {
                $procdumpMode = 'postmortem'
            }
        } else {
            $procdump.Kill($true)
            [void]$procdump.WaitForExit(10000)
            $procdumpMode = 'install-timeout'
        }
    } catch {
        "ProcDump start failed: $($_.Exception.Message)" |
            Set-Content -LiteralPath $procdumpLog -Encoding utf8NoBOM
        $procdumpMode = 'install-failed'
    }
    if ($procdumpMode -ne 'postmortem') {
        try {
            $procdumpArguments = @('-accepteula', '-ma', '-e', '-n', '1', '-w',
                [IO.Path]::GetFileNameWithoutExtension($exe))
            $procdumpInfo = New-CleanProcessStartInfo -FileName $ProcdumpPath `
                -WorkingDirectory $dumpDirectory -Arguments $procdumpArguments `
                -PathValue $cleanPath -UnicodeOutput
            $procdump = [System.Diagnostics.Process]::Start($procdumpInfo)
            $procdumpOutputTask = $procdump.StandardOutput.ReadToEndAsync()
            $procdumpErrorTask = $procdump.StandardError.ReadToEndAsync()
            $procdumpMode = 'monitor-fallback'
        } catch {
            "ProcDump monitor start failed: $($_.Exception.Message)" |
                Add-Content -LiteralPath $procdumpLog -Encoding utf8NoBOM
            $procdump = $null
            $procdumpMode = 'unavailable'
        }
    }
} else {
    'ProcDump executable unavailable; WER LocalDumps remains configured' |
        Set-Content -LiteralPath $procdumpLog -Encoding utf8NoBOM
}

$start = [DateTime]::UtcNow
$process = $null
$processStartObserved = $false
$timedOut = $false
$moduleMap = @{}
$windowSnapshot = $null
$stdout = ''
$stderr = ''
$exitCode = $null
$processSessionId = $null
$processId = $null
$launchError = $null
try {
    $processInfo = New-CleanProcessStartInfo -FileName $exe `
        -WorkingDirectory $workingDirectory -Arguments @() -PathValue $cleanPath
    $process = [System.Diagnostics.Process]::Start($processInfo)
    $processStartObserved = $null -ne $process
    $processId = $process.Id
    $processSessionId = $process.SessionId
    $stdoutTask = $process.StandardOutput.ReadToEndAsync()
    $stderrTask = $process.StandardError.ReadToEndAsync()
    $processMarker = [ordered]@{
        sourceCommit = $SourceCommit
        pid = $processId
        processStartObserved = $true
        startedAtUtc = $start.ToString('o')
        workingDirectory = $workingDirectory
        executable = $exe
        environmentNames = @('SystemRoot', 'WINDIR', 'SystemDrive', 'COMSPEC',
            'HOMEDRIVE', 'HOMEPATH', 'USERPROFILE', 'TEMP', 'TMP', 'PATH')
        inheritedRunnerSecrets = $false
    }
    Write-JsonFile (Join-Path $out 'process-start.json') $processMarker

    $deadline = [DateTime]::UtcNow.AddSeconds($TimeoutSeconds)
    while (-not $process.HasExited -and [DateTime]::UtcNow -lt $deadline) {
        try {
            $process.Refresh()
            $windowSnapshot = [ordered]@{
                handle = ('0x{0:X}' -f $process.MainWindowHandle.ToInt64())
                title = $process.MainWindowTitle
                sessionId = $process.SessionId
            }
            foreach ($module in $process.Modules) {
                $moduleMap[$module.ModuleName] = [ordered]@{
                    name = $module.ModuleName
                    path = $module.FileName
                    baseAddress = ('0x{0:X}' -f $module.BaseAddress.ToInt64())
                    size = $module.ModuleMemorySize
                }
            }
        } catch { }
        Start-Sleep -Milliseconds 100
    }
    if (-not $process.HasExited) {
        $timedOut = $true
        $process.Kill($true)
        $process.WaitForExit()
    }
    $process.WaitForExit()
    $stdout = $stdoutTask.GetAwaiter().GetResult()
    $stderr = $stderrTask.GetAwaiter().GetResult()
    $exitCode = $process.ExitCode
} catch {
    $launchError = $_.Exception.Message
}
$processFinishedAt = [DateTime]::UtcNow

if ($procdumpMode -eq 'postmortem') {
    $uninstallLog = Join-Path $out 'procdump-uninstall.log'
    try {
        $uninstallInfo = New-CleanProcessStartInfo -FileName $ProcdumpPath `
            -WorkingDirectory $dumpDirectory -Arguments @('-accepteula', '-u') `
            -PathValue $cleanPath -UnicodeOutput
        $uninstaller = [System.Diagnostics.Process]::Start($uninstallInfo)
        $uninstallOutTask = $uninstaller.StandardOutput.ReadToEndAsync()
        $uninstallErrTask = $uninstaller.StandardError.ReadToEndAsync()
        if ($uninstaller.WaitForExit(30000)) {
            $uninstallOutTask.GetAwaiter().GetResult() |
                Set-Content -LiteralPath $uninstallLog -Encoding utf8NoBOM
            $uninstallErrTask.GetAwaiter().GetResult() |
                Add-Content -LiteralPath $uninstallLog -Encoding utf8NoBOM
            $procdumpCleanupStatus = if ($uninstaller.ExitCode -eq 0) { 'restored' } else { 'failed' }
        } else {
            $uninstaller.Kill($true)
            $procdumpCleanupStatus = 'timeout'
        }
    } catch {
        "ProcDump uninstall failed: $($_.Exception.Message)" |
            Set-Content -LiteralPath $uninstallLog -Encoding utf8NoBOM
        $procdumpCleanupStatus = 'failed'
    }
} elseif ($null -ne $procdump -and -not $procdump.HasExited) {
    if (-not $timedOut -and $processStartObserved) { [void]$procdump.WaitForExit(5000) }
    if (-not $procdump.HasExited) { $procdump.Kill($true) }
    [void]$procdump.WaitForExit(10000)
}
if ($null -ne $procdump -and $procdumpMode -eq 'monitor-fallback') {
    if ($null -ne $procdumpOutputTask) {
        $procdumpOutputTask.GetAwaiter().GetResult() |
            Set-Content -LiteralPath $procdumpLog -Encoding utf8NoBOM
    }
    if ($null -ne $procdumpErrorTask) {
        $procdumpErrorTask.GetAwaiter().GetResult() |
            Set-Content -LiteralPath $procdumpErrorLog -Encoding utf8NoBOM
    }
}

$debuggerPathAvailable = -not [string]::IsNullOrWhiteSpace($DebuggerPath) -and
    (Test-Path -LiteralPath $DebuggerPath)
$liveDebuggerStatus = 'not-run'
$liveDebuggerProcessId = $null
$liveDebuggerExitCode = $null
$liveDebuggerTargetProcessId = $null
$liveDebuggerTargetObserved = $false
$liveDebuggerTimedOut = $false
$liveDebuggerLog = Join-Path $out 'cdb-live-analysis.log'
$liveDebuggerStdoutLog = Join-Path $out 'cdb-live-stdout.log'
$liveDebuggerStderrLog = Join-Path $out 'cdb-live-stderr.log'
$symbolDirectory = [IO.Path]::GetFullPath($PdbDirectory).Replace('\', '/')
$symbolPath = $symbolDirectory
$preexistingDump = Get-ChildItem -LiteralPath $dumpDirectory -Filter '*.dmp' -File `
    -ErrorAction SilentlyContinue | Select-Object -First 1
if ($debuggerPathAvailable -and $processStartObserved -and $null -ne $exitCode -and
    $exitCode -ne 0 -and -not $timedOut -and $null -eq $preexistingDump) {
    try {
        $liveWorkDirectory = Join-Path $env:RUNNER_TEMP (
            'windows-crash-live-cdb-' + [Guid]::NewGuid().ToString('N'))
        New-Item -ItemType Directory -Force -Path $liveWorkDirectory | Out-Null
        Get-ChildItem -LiteralPath $workingDirectory -Force |
            Copy-Item -Destination $liveWorkDirectory -Recurse -Force
        Get-ChildItem -LiteralPath $liveWorkDirectory -Filter 'tcdiag-*' -File -Recurse |
            Remove-Item -Force -ErrorAction SilentlyContinue
        $liveExecutable = Join-Path $liveWorkDirectory ([IO.Path]::GetFileName($exe))
        if (-not (Test-Path -LiteralPath $liveExecutable)) {
            throw 'Copied runtime does not contain the diagnostic executable.'
        }
        $liveExecutableSha256 = (Get-FileHash -LiteralPath $liveExecutable `
            -Algorithm SHA256).Hash.ToLowerInvariant()
        $sourceExecutableSha256 = (Get-FileHash -LiteralPath $exe `
            -Algorithm SHA256).Hash.ToLowerInvariant()
        if ($liveExecutableSha256 -ne $sourceExecutableSha256) {
            throw 'Copied diagnostic executable does not match the original probe.'
        }

        $liveDumpPath = (Join-Path $dumpDirectory 'live-cdb.dmp').Replace('\', '/')
        if ($Architecture -eq 'I386') {
            $secondChanceCommands = '.lastevent; .exr -1; .ecxr; .lines; kv; r; ' +
                'u @eip-0x20 L40; lmvm tcvm; .dump /ma ' + $liveDumpPath +
                '; !analyze -v; q'
        } else {
            $secondChanceCommands = '.lastevent; .exr -1; .ecxr; kv; .lines; ' +
                'ln tcvm!__entry_from_strcat_in_strcpy+0x1f; ' +
                'u tcvm!__entry_from_strcat_in_strcpy+0x1f L8; lmvm tcvm; ' +
                'ln tcvm!privateHeapSetJump+0x3c; ' +
                'ln tcvm!tczNewInstance+0x68; ln tcvm!tczFindName+0xa9; ' +
                'ln tcvm!tczLoad+0x8b; ln tcvm!startVM+0x1af; ' +
                '.dump /ma ' + $liveDumpPath + '; !analyze -v; q'
        }
        $debuggerCommands = 'sxd -c2 "' + $secondChanceCommands + '" av; g'
        $debuggerArguments = @('-logou', $liveDebuggerLog, '-y', $symbolPath,
            '-i', "$liveWorkDirectory;$workingDirectory;$env:SystemRoot\System32",
            '-o', '-G', '-c', $debuggerCommands, $liveExecutable)
        $liveCleanPath = @($liveWorkDirectory, "$env:SystemRoot\System32", $env:SystemRoot,
            "$env:SystemRoot\System32\Wbem") -join ';'
        $debuggerInfo = New-CleanProcessStartInfo -FileName $DebuggerPath `
            -WorkingDirectory $liveWorkDirectory -Arguments $debuggerArguments `
            -PathValue $liveCleanPath -UnicodeOutput
        $liveDebugger = [System.Diagnostics.Process]::Start($debuggerInfo)
        $liveDebuggerProcessId = $liveDebugger.Id
        $liveDebuggerStdoutTask = $liveDebugger.StandardOutput.ReadToEndAsync()
        $liveDebuggerStderrTask = $liveDebugger.StandardError.ReadToEndAsync()
        $liveDebuggerStatus = 'running'
        $debuggerDeadline = [DateTime]::UtcNow.AddSeconds(180)
        while (-not $liveDebugger.HasExited -and [DateTime]::UtcNow -lt $debuggerDeadline) {
            try {
                $targetProcesses = @(Get-CimInstance Win32_Process -Filter (
                    "Name='" + [IO.Path]::GetFileName($liveExecutable) + "'") `
                    -ErrorAction SilentlyContinue)
                foreach ($target in $targetProcesses) {
                    if ($target.ExecutablePath -ieq $liveExecutable) {
                        $liveDebuggerTargetProcessId = [int]$target.ProcessId
                        $liveDebuggerTargetObserved = $true
                        break
                    }
                }
            } catch { }
            Start-Sleep -Milliseconds 20
        }
        if (-not $liveDebugger.HasExited) {
            $liveDebuggerTimedOut = $true
            $liveDebugger.Kill($true)
            [void]$liveDebugger.WaitForExit(10000)
            $liveDebuggerStatus = 'timed-out'
        } else {
            $liveDebuggerExitCode = $liveDebugger.ExitCode
            $liveDebuggerStatus = 'completed'
        }
        $liveDebuggerStdoutTask.GetAwaiter().GetResult() |
            Set-Content -LiteralPath $liveDebuggerStdoutLog -Encoding utf8NoBOM
        $liveDebuggerError = $liveDebuggerStderrTask.GetAwaiter().GetResult()
        $liveDebuggerError | Add-Content -LiteralPath $liveDebuggerStderrLog `
            -Encoding utf8NoBOM
        if (-not $liveDebuggerTargetObserved -and (Test-Path -LiteralPath $liveDebuggerLog)) {
            $liveDebuggerTargetObserved = [IO.File]::ReadAllText($liveDebuggerLog) `
                -match [regex]::Escape($liveExecutable)
        }
        if ($liveDebuggerStatus -eq 'completed') {
            $liveDebuggerStatus = if (Test-Path -LiteralPath $liveDumpPath) {
                'dump-captured'
            } else {
                'completed-no-dump'
            }
        }
    } catch {
        "CDB live capture failed: $($_.Exception.Message)" |
            Set-Content -LiteralPath $liveDebuggerStderrLog -Encoding utf8NoBOM
        if ($null -ne $liveDebugger -and -not $liveDebugger.HasExited) {
            $liveDebugger.Kill($true)
            [void]$liveDebugger.WaitForExit(10000)
        }
        if ($liveDebuggerStatus -in @('not-run', 'running')) {
            $liveDebuggerStatus = 'failed'
        }
    }
}

$stdout | Set-Content -LiteralPath (Join-Path $out 'runtime-stdout.log') -Encoding utf8NoBOM
$stderr | Set-Content -LiteralPath (Join-Path $out 'runtime-stderr.log') -Encoding utf8NoBOM
ConvertTo-Json -InputObject @($moduleMap.Values | Sort-Object name) -Depth 6 |
    Set-Content -LiteralPath (Join-Path $out 'loaded-modules.json') -Encoding utf8NoBOM
if ($null -ne $windowSnapshot) {
    Write-JsonFile (Join-Path $out 'window-snapshot.json') $windowSnapshot
}

$unsignedExit = $null
$exitHex = $null
if ($null -ne $exitCode) {
    $unsignedExit = [uint64]([int64]$exitCode -band 4294967295L)
    $exitHex = '0x{0:X8}' -f $unsignedExit
}
$dumps = @(Get-ChildItem -LiteralPath $dumpDirectory -Filter '*.dmp' -File `
    -ErrorAction SilentlyContinue | ForEach-Object {
        [ordered]@{ name = $_.Name; size = $_.Length; sha256 = (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant() }
    })

$eventRecords = @()
try {
    $eventRecords = @(Get-WinEvent -FilterHashtable @{
        LogName = 'Application'
        StartTime = $start.AddMinutes(-2)
    } -ErrorAction SilentlyContinue |
        Where-Object { $_.Id -in 1000, 1001 -or $_.ProviderName -match 'Application Error|Windows Error Reporting' } |
        Select-Object -First 30 TimeCreated, Id, ProviderName, LevelDisplayName, Message)
} catch { }
Write-JsonFile (Join-Path $out 'application-events.json') $eventRecords

$debuggerStatus = $liveDebuggerStatus
$debuggerLog = Join-Path $out 'cdb-analysis.log'
$dumpFile = Get-ChildItem -LiteralPath $dumpDirectory -Filter '*.dmp' -File `
    -ErrorAction SilentlyContinue | Select-Object -First 1
if ($null -ne $dumpFile -and $debuggerPathAvailable) {
    try {
        if ($Architecture -eq 'I386') {
            $offlineCommands = '.reload /f; !analyze -v; .exr -1; .ecxr; .lines; ' +
                'r; u @eip-0x20 L40; kv; lmvm tcvm; q'
        } else {
            $offlineCommands = '.reload /f; !analyze -v; .exr -1; .ecxr; .lines; ' +
                'ln tcvm!__entry_from_strcat_in_strcpy+0x1f; ' +
                'u tcvm!__entry_from_strcat_in_strcpy+0x1f L8; ' +
                'ln tcvm!privateHeapSetJump+0x3c; ' +
                'ln tcvm!tczNewInstance+0x68; ln tcvm!tczFindName+0xa9; ' +
                'ln tcvm!tczLoad+0x8b; ln tcvm!startVM+0x1af; ' +
                'kv; lmvm tcvm; q'
        }
        $debuggerArgs = @('-logou', $debuggerLog, '-y', $symbolPath,
            '-i', "$workingDirectory;$env:SystemRoot\System32", '-z', $dumpFile.FullName,
            '-c', $offlineCommands)
        $debuggerInfo = New-CleanProcessStartInfo -FileName $DebuggerPath `
            -WorkingDirectory $out -Arguments $debuggerArgs -PathValue $cleanPath -UnicodeOutput
        $debugger = [System.Diagnostics.Process]::Start($debuggerInfo)
        $debuggerOutTask = $debugger.StandardOutput.ReadToEndAsync()
        $debuggerErrTask = $debugger.StandardError.ReadToEndAsync()
        if ($debugger.WaitForExit(180000)) {
            $debuggerOutTask.GetAwaiter().GetResult() | Set-Content `
                -LiteralPath (Join-Path $out 'windbg-stdout.log') -Encoding utf8NoBOM
            $debuggerErrTask.GetAwaiter().GetResult() | Set-Content `
                -LiteralPath (Join-Path $out 'windbg-stderr.log') -Encoding utf8NoBOM
            $debuggerStatus = 'completed'
        } else {
            $debugger.Kill($true)
            $debuggerStatus = 'timed-out'
        }
    } catch {
        "CDB analysis failed: $($_.Exception.Message)" |
            Set-Content -LiteralPath $debuggerLog -Encoding utf8NoBOM
        $debuggerStatus = 'failed'
    }
} elseif ($null -ne $dumpFile) {
    'CDB unavailable; dump retained for external analysis' |
        Set-Content -LiteralPath $debuggerLog -Encoding utf8NoBOM
}

$runtime = [ordered]@{
    sourceCommit = $SourceCommit
    architecture = $Architecture
    startedAtUtc = $start.ToString('o')
    processFinishedAtUtc = $processFinishedAt.ToString('o')
    durationMilliseconds = [int64]($processFinishedAt - $start).TotalMilliseconds
    processStartObserved = $processStartObserved
    processId = $processId
    processSessionId = $processSessionId
    exitCode = $exitCode
    exitCodeUnsigned = $unsignedExit
    exitCodeHex = $exitHex
    timedOut = $timedOut
    launchError = $launchError
    stdoutLog = 'runtime-stdout.log'
    stderrLog = 'runtime-stderr.log'
    loadedModulesLog = 'loaded-modules.json'
    windowSnapshotLog = if ($null -ne $windowSnapshot) { 'window-snapshot.json' } else { $null }
    procdumpAvailable = $procdumpPathAvailable
    procdumpCaptureMode = $procdumpMode
    procdumpInstallExitCode = $procdumpInstallExitCode
    procdumpCleanupStatus = $procdumpCleanupStatus
    localDumpsConfigured = $true
    dumpFiles = $dumps
    applicationEventsLog = 'application-events.json'
    debuggerStatus = $debuggerStatus
    debuggerPath = if ($debuggerPathAvailable) { $DebuggerPath } else { $null }
    debuggerLog = if (Test-Path -LiteralPath $debuggerLog) { 'cdb-analysis.log' } `
        elseif (Test-Path -LiteralPath $liveDebuggerLog) { 'cdb-live-analysis.log' } else { $null }
    liveDebugger = [ordered]@{
        status = $liveDebuggerStatus
        processId = $liveDebuggerProcessId
        exitCode = $liveDebuggerExitCode
        targetProcessId = $liveDebuggerTargetProcessId
        targetProcessObserved = $liveDebuggerTargetObserved
        timedOut = $liveDebuggerTimedOut
        log = if (Test-Path -LiteralPath $liveDebuggerLog) { 'cdb-live-analysis.log' } else { $null }
        stdoutLog = if (Test-Path -LiteralPath $liveDebuggerStdoutLog) { 'cdb-live-stdout.log' } else { $null }
        stderrLog = if (Test-Path -LiteralPath $liveDebuggerStderrLog) { 'cdb-live-stderr.log' } else { $null }
        dumpFile = if (Test-Path -LiteralPath (Join-Path $dumpDirectory 'live-cdb.dmp')) {
            'dumps/live-cdb.dmp'
        } else { $null }
        executableSha256 = if ($liveExecutableSha256) { $liveExecutableSha256 } else { $null }
    }
}
Write-JsonFile (Join-Path $out 'runtime.json') $runtime
Remove-Item -LiteralPath $werKey -Recurse -Force -ErrorAction SilentlyContinue
