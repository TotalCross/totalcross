# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$Executable,
    [Parameter(Mandatory = $true)][string]$OutputDirectory,
    [Parameter(Mandatory = $true)][string]$SourceCommit,
    [Parameter(Mandatory = $true)][string]$ProcdumpPath,
    [Parameter(Mandatory = $true)][string]$PdbDirectory,
    [int]$TimeoutSeconds = 120
)

$ErrorActionPreference = 'Stop'

function Write-JsonFile([string]$Path, $Value) {
    $Value | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath $Path -Encoding utf8NoBOM
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
                                   [string[]]$Arguments, [string]$PathValue) {
    $info = [System.Diagnostics.ProcessStartInfo]::new()
    $info.FileName = $FileName
    $info.WorkingDirectory = $WorkingDirectory
    $info.UseShellExecute = $false
    $info.CreateNoWindow = $true
    $info.RedirectStandardOutput = $true
    $info.RedirectStandardError = $true
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
$procdumpLog = Join-Path $out 'procdump.log'
$procdumpErrorLog = Join-Path $out 'procdump-stderr.log'
if (Test-Path -LiteralPath $ProcdumpPath) {
    try {
        $procdumpArguments = @('-accepteula', '-ma', '-e', '-n', '1', '-w',
            [IO.Path]::GetFileNameWithoutExtension($exe))
        $procdumpInfo = New-CleanProcessStartInfo -FileName $ProcdumpPath `
            -WorkingDirectory $dumpDirectory -Arguments $procdumpArguments -PathValue $cleanPath
        $procdump = [System.Diagnostics.Process]::Start($procdumpInfo)
        $procdumpOutputTask = $procdump.StandardOutput.ReadToEndAsync()
        $procdumpErrorTask = $procdump.StandardError.ReadToEndAsync()
        Start-Sleep -Milliseconds 1200
    } catch {
        "ProcDump start failed: $($_.Exception.Message)" |
            Set-Content -LiteralPath $procdumpLog -Encoding utf8NoBOM
        $procdump = $null
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

if ($null -ne $procdump) {
    if (-not $procdump.HasExited -and -not $timedOut -and $processStartObserved) {
        [void]$procdump.WaitForExit(30000)
    }
    if (-not $procdump.HasExited) { $procdump.Kill($true) }
    [void]$procdump.WaitForExit(10000)
    if ($null -ne $procdumpOutputTask) {
        $procdumpOutputTask.GetAwaiter().GetResult() |
            Set-Content -LiteralPath $procdumpLog -Encoding utf8NoBOM
    }
    if ($null -ne $procdumpErrorTask) {
        $procdumpErrorTask.GetAwaiter().GetResult() |
            Set-Content -LiteralPath $procdumpErrorLog -Encoding utf8NoBOM
    }
}

$stdout | Set-Content -LiteralPath (Join-Path $out 'runtime-stdout.log') -Encoding utf8NoBOM
$stderr | Set-Content -LiteralPath (Join-Path $out 'runtime-stderr.log') -Encoding utf8NoBOM
@($moduleMap.Values | Sort-Object name) |
    ConvertTo-Json -Depth 6 | Set-Content -LiteralPath (Join-Path $out 'loaded-modules.json') `
        -Encoding utf8NoBOM
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

$debuggerStatus = 'unavailable'
$debuggerLog = Join-Path $out 'windbg-analysis.log'
$dumpFile = Get-ChildItem -LiteralPath $dumpDirectory -Filter '*.dmp' -File `
    -ErrorAction SilentlyContinue | Select-Object -First 1
$windbg = Get-Command WinDbgX.exe -ErrorAction SilentlyContinue
if ($null -ne $dumpFile -and $null -ne $windbg) {
    try {
        $symbolCache = Join-Path $env:RUNNER_TEMP 'windbg-symbol-cache'
        New-Item -ItemType Directory -Force -Path $symbolCache | Out-Null
        $symbolPath = "$PdbDirectory;srv*$symbolCache*https://msdl.microsoft.com/download/symbols"
        $debuggerArgs = @('-Q', '-logo', $debuggerLog, '-y', $symbolPath,
            '-i', "$workingDirectory;$env:SystemRoot\System32", '-z', $dumpFile.FullName,
            '-c', '.reload /f; !analyze -v; .ecxr; kv; lm; q')
        $debuggerInfo = New-CleanProcessStartInfo -FileName $windbg.Source `
            -WorkingDirectory $out -Arguments $debuggerArgs -PathValue $cleanPath
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
        "WinDbg analysis failed: $($_.Exception.Message)" |
            Set-Content -LiteralPath $debuggerLog -Encoding utf8NoBOM
        $debuggerStatus = 'failed'
    }
} elseif ($null -ne $dumpFile) {
    'WinDbgX.exe unavailable; dump retained for external analysis' |
        Set-Content -LiteralPath $debuggerLog -Encoding utf8NoBOM
}

$runtime = [ordered]@{
    sourceCommit = $SourceCommit
    startedAtUtc = $start.ToString('o')
    durationMilliseconds = [int64]([DateTime]::UtcNow - $start).TotalMilliseconds
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
    procdumpAvailable = Test-Path -LiteralPath $ProcdumpPath
    localDumpsConfigured = $true
    dumpFiles = $dumps
    applicationEventsLog = 'application-events.json'
    debuggerStatus = $debuggerStatus
    debuggerLog = if (Test-Path -LiteralPath $debuggerLog) { 'windbg-analysis.log' } else { $null }
}
Write-JsonFile (Join-Path $out 'runtime.json') $runtime
Remove-Item -LiteralPath $werKey -Recurse -Force -ErrorAction SilentlyContinue
