# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

if ($env:OS -ne 'Windows_NT') {
    Write-Error 'This validation runner must be started on Windows.'
    exit 2
}

$packageRoot = [System.IO.Path]::GetFullPath($PSScriptRoot)
$provenancePath = Join-Path $packageRoot 'provenance.json'
$resultsDir = Join-Path $packageRoot 'results'
$summaryPath = Join-Path $packageRoot 'results-summary.txt'

if (-not (Test-Path -LiteralPath $provenancePath -PathType Leaf)) {
    Write-Error 'provenance.json is missing from the package.'
    exit 2
}

$provenance = Get-Content -LiteralPath $provenancePath -Raw | ConvertFrom-Json
if ($provenance.workflow.runId -ne 36040721060 -or
        $provenance.workflow.sourceSha -ne '0badac435cc2c6af31de4bb0adad9ed58e6cd0c5') {
    Write-Error 'The package provenance does not match the approved workflow run and source SHA.'
    exit 2
}

New-Item -ItemType Directory -Path $resultsDir -Force | Out-Null

$fileFailures = @()
foreach ($entry in $provenance.files) {
    $filePath = Join-Path $packageRoot $entry.path
    if (-not (Test-Path -LiteralPath $filePath -PathType Leaf)) {
        $fileFailures += "missing: $($entry.path)"
        continue
    }

    $actualHash = (Get-FileHash -LiteralPath $filePath -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($actualHash -ne $entry.sha256.ToLowerInvariant()) {
        $fileFailures += "hash mismatch: $($entry.path)"
    }
}

$testResults = @()
$tests = @(
    [pscustomobject]@{
        name = 'correctness'
        executable = 'SemaphoreSmokeApp.exe'
        timeoutSeconds = 60
        requiredMarkers = @(
            'fixture=SemaphoreSmokeApp,overallPass=true,checks=7'
        )
    },
    [pscustomobject]@{
        name = 'stress'
        executable = 'SemaphoreStressSmokeApp.exe'
        timeoutSeconds = 120
        requiredMarkers = @(
            'fixture=SemaphoreStressSmokeApp,overallPass=true,producers=4,consumers=4,expected=20000,produced=20000,acquired=20000'
        )
    }
)

foreach ($test in $tests) {
    $stdoutName = "$($test.name).stdout.log"
    $stderrName = "$($test.name).stderr.log"
    $debugConsoleName = "$($test.name).debugconsole.log"
    $stdoutPath = Join-Path $resultsDir $stdoutName
    $stderrPath = Join-Path $resultsDir $stderrName
    $debugConsolePath = Join-Path $packageRoot 'DebugConsole.txt'
    $debugConsoleResultPath = Join-Path $resultsDir $debugConsoleName
    $executablePath = Join-Path $packageRoot $test.executable
    $process = $null
    $processHandle = [IntPtr]::Zero
    $exitCode = $null
    $timedOut = $false
    $status = 'failed'
    $errorMessage = $null
    $missingMarkers = @()
    $debugConsoleOutput = ''

    Remove-Item -LiteralPath @(
        $stdoutPath,
        $stderrPath,
        $debugConsolePath,
        $debugConsoleResultPath
    ) -Force -ErrorAction SilentlyContinue
    $debugConsoleCleared = -not (Test-Path -LiteralPath $debugConsolePath)

    try {
        if (-not $debugConsoleCleared) {
            throw 'Could not remove stale DebugConsole.txt before the test.'
        }
        if ($fileFailures.Count -gt 0) {
            throw 'Package file integrity validation failed.'
        }
        if (-not (Test-Path -LiteralPath $executablePath -PathType Leaf)) {
            throw "Executable is missing: $($test.executable)"
        }

        Remove-Item -LiteralPath $stdoutPath, $stderrPath -Force -ErrorAction SilentlyContinue
        $process = Start-Process -FilePath $executablePath `
            -WorkingDirectory $packageRoot `
            -RedirectStandardOutput $stdoutPath `
            -RedirectStandardError $stderrPath `
            -PassThru
        $processHandle = $process.Handle

        if (-not $process.WaitForExit($test.timeoutSeconds * 1000)) {
            $timedOut = $true
            try { $process.Kill() } catch { }
        }

        $process.WaitForExit()
        $exitCode = $process.ExitCode
        if ($timedOut) {
            $errorMessage = "Timed out after $($test.timeoutSeconds) seconds."
        }
    }
    catch {
        $errorMessage = $_.Exception.Message
    }
    finally {
        if ($debugConsoleCleared -and
                (Test-Path -LiteralPath $debugConsolePath -PathType Leaf)) {
            try {
                Copy-Item -LiteralPath $debugConsolePath `
                    -Destination $debugConsoleResultPath -Force
                $debugConsoleOutput = Get-Content `
                    -LiteralPath $debugConsoleResultPath -Raw
            }
            catch {
                if (-not $errorMessage) {
                    $errorMessage = "Could not capture DebugConsole.txt: $($_.Exception.Message)"
                }
            }
        }
        else {
            try {
                Set-Content -LiteralPath $debugConsoleResultPath `
                    -Value '' -Encoding UTF8
            }
            catch {
                if (-not $errorMessage) {
                    $errorMessage = "Could not create the DebugConsole result: $($_.Exception.Message)"
                }
            }
        }

        if ($null -ne $process) {
            try { $process.Dispose() } catch { }
        }
    }

    if (-not (Test-Path -LiteralPath $stdoutPath -PathType Leaf)) {
        Set-Content -LiteralPath $stdoutPath -Value '' -Encoding UTF8
    }
    if (-not (Test-Path -LiteralPath $stderrPath -PathType Leaf)) {
        $stderrValue = ''
        if ($errorMessage) { $stderrValue = $errorMessage }
        Set-Content -LiteralPath $stderrPath -Value $stderrValue -Encoding UTF8
    }

    $output = (Get-Content -LiteralPath $stdoutPath -Raw) + [Environment]::NewLine +
        (Get-Content -LiteralPath $stderrPath -Raw) + [Environment]::NewLine +
        $debugConsoleOutput
    foreach ($marker in $test.requiredMarkers) {
        if (-not $output.Contains($marker)) {
            $missingMarkers += $marker
        }
    }

    if (-not $errorMessage) {
        if ($exitCode -ne 0) {
            $errorMessage = "Process exited with code $exitCode."
        }
        elseif ($missingMarkers.Count -gt 0) {
            $errorMessage = "Required PASS marker missing: $($missingMarkers -join '; ')"
        }
        else {
            $status = 'passed'
        }
    }

    $testResults += [pscustomobject]@{
        name = $test.name
        executable = $test.executable
        status = $status
        exitCode = $exitCode
        timedOut = $timedOut
        timeoutSeconds = $test.timeoutSeconds
        missingMarkers = @($missingMarkers)
        error = $errorMessage
        stdout = "results/$stdoutName"
        stderr = "results/$stderrName"
        debugConsole = "results/$debugConsoleName"
    }
}

$overallStatus = 'passed'
if ($fileFailures.Count -gt 0 -or @($testResults | Where-Object { $_.status -ne 'passed' }).Count -gt 0) {
    $overallStatus = 'failed'
}
$completedUtc = [DateTime]::UtcNow.ToString('o')

$packageFilesStatus = 'verified'
if ($fileFailures.Count -gt 0) { $packageFilesStatus = 'failed' }
$summaryLines = @(
    'Semaphore Windows validation'
    "status=$overallStatus"
    "workflowRun=$($provenance.workflow.runId)"
    "sourceSha=$($provenance.workflow.sourceSha)"
    "completedUtc=$completedUtc"
    "powershell=$($PSVersionTable.PSVersion)"
    "windows=$([Environment]::OSVersion.VersionString)"
    "packageFiles=$packageFilesStatus"
)
foreach ($testResult in $testResults) {
    $summaryLines += "test=$($testResult.name),status=$($testResult.status),exitCode=$($testResult.exitCode),timedOut=$($testResult.timedOut)"
}
if ($fileFailures.Count -gt 0) {
    foreach ($failure in $fileFailures) { $summaryLines += "packageError=$failure" }
}
foreach ($testResult in $testResults) {
    if ($testResult.error) { $summaryLines += "testError=$($testResult.name): $($testResult.error)" }
}
Set-Content -LiteralPath $summaryPath -Value $summaryLines -Encoding UTF8

$provenance.testExecution = [pscustomobject]@{
    status = $overallStatus
    completedUtc = $completedUtc
    powershellVersion = $PSVersionTable.PSVersion.ToString()
    windowsVersion = [Environment]::OSVersion.VersionString
    packageFilesVerified = ($fileFailures.Count -eq 0)
    packageFileFailures = @($fileFailures)
    results = @($testResults)
}
$provenance | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath $provenancePath -Encoding UTF8

$summaryLines | ForEach-Object { Write-Output $_ }
if ($overallStatus -ne 'passed') { exit 1 }
exit 0
