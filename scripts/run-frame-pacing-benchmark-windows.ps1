# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$bundleRoot = $PSScriptRoot
$timestamp = Get-Date -Format 'yyyyMMdd-HHmmss-fff'
$runId = "$timestamp-$PID"
$resultsRoot = Join-Path $bundleRoot 'results'
$evidenceRoot = Join-Path $resultsRoot $runId
$archivePath = Join-Path $resultsRoot "frame-pacing-windows-results-$timestamp.zip"
$timeoutMilliseconds = 180000
$expectedMeasuredProcesses = 48
$datasetHash = $null
$runtimeSha256 = $null
$sourceCommit = $null
$executablePath = $null
$manifest = $null
$failureMessage = $null
$completedProcesses = 0
$completedPreflightProcesses = 0
$rowsByStage = @{}
for ($stage = 1; $stage -le 5; $stage++) { $rowsByStage[$stage] = @() }

$csvFields = @(
    'stage','configuration','sample','sourceCommit','runtimeIdentity','frameCount',
    'callbackCount','frameIntervalP50Ns','frameIntervalP95Ns','frameIntervalP99Ns',
    'frameIntervalMaxNs','activeWorkP50Ns','activeWorkP95Ns','activeWorkP99Ns',
    'activeWorkMaxNs','paintP50Ns','paintP95Ns','paintP99Ns','paintMaxNs',
    'framesOver16_67Count','framesOver20Count','framesOver25Count',
    'framesOver33_3Count','framesOver50Count','framesOver100Count',
    'callbackDeltaP50Ns','callbackDeltaP95Ns','callbackDeltaP99Ns',
    'callbackDeltaMaxNs','callbackAbsoluteLatenessP50Ns',
    'callbackAbsoluteLatenessP95Ns','callbackAbsoluteLatenessP99Ns',
    'callbackAbsoluteLatenessMaxNs','callbackDeltaErrorP50Ns',
    'callbackDeltaErrorP95Ns','callbackDeltaErrorP99Ns','callbackDeltaErrorMaxNs',
    'measuredWallDurationNs','processWallNs','driver','timerFps','clock',
    'timerDeadlinePolicy','eventLoopPolicy','yieldPolicy','timerDeadlineMode',
    'eventLoopMode','threadYieldMode','prefetchThreadMode','prefetchWorkerSleepMs',
    'expectedCallbackIntervalNs','syntheticPacingProfile','syntheticPacingIntervalNs',
    'sleepRequestCount','totalRequestedSleepNs','totalActualSleepNs',
    'sleepOvershootP50Ns','sleepOvershootP95Ns','sleepOvershootP99Ns',
    'sleepOvershootMaxNs','deadlineErrorP50Ns','deadlineErrorP95Ns',
    'deadlineErrorP99Ns','deadlineErrorMaxNs'
)

$matrix = @(
    @{Stage=1; Name='synthetic-current-16ms'; Driver='synthetic'; Fps=0; Clock='monotonic'; Interval=0; Profile='synthetic-current-16ms'; Deadline='relative'; EventLoop='poll'; Yield='legacy'; DeadlinePolicy='absolute-from-start'; EventPolicy='benchmark-loop'; YieldPolicy='none'},
    @{Stage=1; Name='synthetic-60hz'; Driver='synthetic'; Fps=0; Clock='monotonic'; Interval=0; Profile='synthetic-60hz'; Deadline='relative'; EventLoop='poll'; Yield='legacy'; DeadlinePolicy='absolute-from-start'; EventPolicy='benchmark-loop'; YieldPolicy='none'},
    @{Stage=2; Name='timer-40-millis'; Driver='timer'; Fps=40; Clock='millis'; Interval=25000000; Profile=''; Deadline='relative'; EventLoop='poll'; Yield='legacy'; DeadlinePolicy='native-relative'; EventPolicy='timer-events'; YieldPolicy='none'},
    @{Stage=2; Name='timer-60-millis'; Driver='timer'; Fps=60; Clock='millis'; Interval=16000000; Profile=''; Deadline='relative'; EventLoop='poll'; Yield='legacy'; DeadlinePolicy='native-relative'; EventPolicy='timer-events'; YieldPolicy='none'},
    @{Stage=2; Name='update-millis'; Driver='update'; Fps=0; Clock='millis'; Interval=16000000; Profile=''; Deadline='relative'; EventLoop='poll'; Yield='legacy'; DeadlinePolicy='not-applicable'; EventPolicy='update-listener'; YieldPolicy='none'},
    @{Stage=3; Name='timer-60-millis'; Driver='timer'; Fps=60; Clock='millis'; Interval=16000000; Profile=''; Deadline='relative'; EventLoop='poll'; Yield='legacy'; DeadlinePolicy='native-relative'; EventPolicy='timer-events'; YieldPolicy='none'},
    @{Stage=3; Name='timer-60-nano'; Driver='timer'; Fps=60; Clock='nano'; Interval=16000000; Profile=''; Deadline='relative'; EventLoop='poll'; Yield='legacy'; DeadlinePolicy='native-relative'; EventPolicy='timer-events'; YieldPolicy='none'},
    @{Stage=3; Name='update-millis'; Driver='update'; Fps=0; Clock='millis'; Interval=16000000; Profile=''; Deadline='relative'; EventLoop='poll'; Yield='legacy'; DeadlinePolicy='not-applicable'; EventPolicy='update-listener'; YieldPolicy='none'},
    @{Stage=3; Name='update-nano'; Driver='update'; Fps=0; Clock='nano'; Interval=16000000; Profile=''; Deadline='relative'; EventLoop='poll'; Yield='legacy'; DeadlinePolicy='not-applicable'; EventPolicy='update-listener'; YieldPolicy='none'},
    @{Stage=4; Name='timer-60-nano-relative'; Driver='timer'; Fps=60; Clock='nano'; Interval=16000000; Profile=''; Deadline='relative'; EventLoop='poll'; Yield='legacy'; DeadlinePolicy='native-relative'; EventPolicy='timer-events'; YieldPolicy='none'},
    @{Stage=4; Name='timer-60-nano-absolute'; Driver='timer'; Fps=60; Clock='nano'; Interval=16000000; Profile=''; Deadline='absolute'; EventLoop='poll'; Yield='legacy'; DeadlinePolicy='native-absolute'; EventPolicy='timer-events'; YieldPolicy='none'},
    @{Stage=4; Name='update-nano-relative'; Driver='update'; Fps=0; Clock='nano'; Interval=16000000; Profile=''; Deadline='relative'; EventLoop='poll'; Yield='legacy'; DeadlinePolicy='not-applicable'; EventPolicy='update-listener'; YieldPolicy='none'},
    @{Stage=4; Name='update-nano-absolute'; Driver='update'; Fps=0; Clock='nano'; Interval=16000000; Profile=''; Deadline='absolute'; EventLoop='poll'; Yield='legacy'; DeadlinePolicy='not-applicable'; EventPolicy='update-listener'; YieldPolicy='none'},
    @{Stage=5; Name='poll-legacy-yield'; Driver='update'; Fps=0; Clock='nano'; Interval=16000000; Profile=''; Deadline='absolute'; EventLoop='poll'; Yield='legacy'; DeadlinePolicy='not-applicable'; EventPolicy='update-listener'; YieldPolicy='none'},
    @{Stage=5; Name='wait-legacy-yield'; Driver='update'; Fps=0; Clock='nano'; Interval=16000000; Profile=''; Deadline='absolute'; EventLoop='wait'; Yield='legacy'; DeadlinePolicy='not-applicable'; EventPolicy='update-listener'; YieldPolicy='none'},
    @{Stage=5; Name='wait-native-yield'; Driver='update'; Fps=0; Clock='nano'; Interval=16000000; Profile=''; Deadline='absolute'; EventLoop='wait'; Yield='native'; DeadlinePolicy='not-applicable'; EventPolicy='update-listener'; YieldPolicy='native'}
)

function Write-RunMetadata {
    param([string]$Status, [string]$Failure)

    $counts = @{}
    for ($stage = 1; $stage -le 5; $stage++) {
        $counts[[string]$stage] = $script:rowsByStage[$stage].Count
    }
    $metadata = [PSCustomObject]@{
        schemaVersion = 1
        benchmark = 'frame-pacing'
        target = 'windows-x64'
        status = $Status
        createdAt = $timestamp
        sourceCommit = $script:sourceCommit
        runtimeIdentity = $script:runtimeSha256
        datasetHash = $script:datasetHash
        completedMeasuredProcesses = $script:completedProcesses
        expectedMeasuredProcesses = $expectedMeasuredProcesses
        completedPreflightProcesses = $script:completedPreflightProcesses
        stageRowCounts = $counts
        windowsExecuted = $true
        failure = $Failure
        archivePath = $archivePath
    }
    ConvertTo-Json -InputObject $metadata -Depth 6 -Compress |
        Set-Content -LiteralPath (Join-Path $evidenceRoot 'execution-metadata.json') -Encoding UTF8
}

function Write-StageSummaries {
    for ($stage = 1; $stage -le 5; $stage++) {
        $path = Join-Path $evidenceRoot "stage-$stage-summary.csv"
        if ($script:rowsByStage[$stage].Count -gt 0) {
            $script:rowsByStage[$stage] | Select-Object $script:csvFields |
                Export-Csv -LiteralPath $path -NoTypeInformation -Encoding UTF8
        } else {
            ($script:csvFields -join ',') |
                Set-Content -LiteralPath $path -Encoding UTF8
        }
    }
}

function Invoke-BenchmarkProcess {
    param($Config, [int]$Stage, [int]$ConfigIndex, [int]$Sample,
        [string]$Accounting, [switch]$Preflight)
    $label = if ($Preflight) { "preflight-stage-$Stage-$($Config.Name)" } else { "stage-$Stage-$($Config.Name)-sample-$Sample" }
    $outputRelative = "p-$PID-$Stage-$ConfigIndex-$Sample"
    $outputPath = Join-Path $bundleRoot $outputRelative
    if (Test-Path -LiteralPath $outputPath) { throw "Duplicate app output path: $outputPath" }
    $logRoot = Join-Path $evidenceRoot 'logs'
    New-Item -ItemType Directory -Path $logRoot -Force | Out-Null
    $stdoutPath = Join-Path $logRoot "$label.stdout.log"
    $stderrPath = Join-Path $logRoot "$label.stderr.log"
    $debugPath = Join-Path $bundleRoot 'DebugConsole.txt'
    $appReportPath = Join-Path $bundleRoot 'ImageScrollRealWorkloadBenchmarkApp.log'
    if (Test-Path -LiteralPath $debugPath -PathType Leaf) {
        Move-Item -LiteralPath $debugPath -Destination (Join-Path $logRoot "preexisting-$label.DebugConsole.log")
    }
    if (Test-Path -LiteralPath $appReportPath -PathType Leaf) {
        Move-Item -LiteralPath $appReportPath -Destination (Join-Path $logRoot "preexisting-$label.application.log")
    }
    $arguments = @(
        '/scr','-1,-1,540,960','-p','.','--app-root=.','--corpus=corpus/imag',
        "--output=$outputRelative",'--image-optimization=6','--prefetch=on',
        "--accounting=$Accounting","--run=$Sample",'--duration=3000',
        '--prefetch-thread-mode=worker-semaphore'
    )
    foreach ($argument in (Get-ConfigurationArguments $Config)) { $arguments += [string]$argument }
    if ([string]::Join(' ', $arguments[4..($arguments.Count - 1)]).Length -gt 255) {
        throw "Native command line exceeds 255 characters for $label"
    }
    $environmentValues = @{
        TC_TIMER_DEADLINE_MODE=$Config.Deadline
        TC_EVENT_LOOP_MODE=$Config.EventLoop
        TC_THREAD_YIELD_MODE=$Config.Yield
    }
    $previousEnvironment = @{}
    $watch = [System.Diagnostics.Stopwatch]::StartNew()
    $process = $null
    $timedOut = $false
    $exitCode = -1
    try {
        foreach ($name in $environmentValues.Keys) {
            $previousEnvironment[$name] = [Environment]::GetEnvironmentVariable($name, 'Process')
            [Environment]::SetEnvironmentVariable($name, [string]$environmentValues[$name], 'Process')
        }
        $process = Start-Process -FilePath $executablePath -ArgumentList $arguments `
            -WorkingDirectory $bundleRoot -RedirectStandardOutput $stdoutPath `
            -RedirectStandardError $stderrPath -PassThru
        $null = $process.Handle
        if (-not $process.WaitForExit($timeoutMilliseconds)) {
            $timedOut = $true
            try { $process.Kill() } catch { }
        }
        $process.WaitForExit()
        $exitCode = $process.ExitCode
    } finally {
        $watch.Stop()
        if ($null -ne $process) { $process.Dispose() }
        foreach ($name in $environmentValues.Keys) {
            [Environment]::SetEnvironmentVariable($name, $previousEnvironment[$name], 'Process')
        }
        if (Test-Path -LiteralPath $debugPath -PathType Leaf) {
            Move-Item -LiteralPath $debugPath -Destination (Join-Path $logRoot "$label.DebugConsole.log")
        }
        if (Test-Path -LiteralPath $appReportPath -PathType Leaf) {
            Move-Item -LiteralPath $appReportPath -Destination (Join-Path $logRoot "$label.application.log")
        }
    }
    if ($timedOut) { throw "$label timed out after $timeoutMilliseconds ms (exit $exitCode)" }
    if ($exitCode -ne 0) { throw "$label exited with code $exitCode" }
    $summaries = @(Get-ChildItem -LiteralPath $outputPath -Filter 'summary.json' -Recurse -File)
    if ($summaries.Count -ne 1) { throw "$label produced $($summaries.Count) summary files" }
    $summary = Read-JsonFile $summaries[0].FullName
    Assert-Summary $summary $Config $Accounting
    $completion = Get-CompletionRecord -Paths @($stdoutPath, $stderrPath)
    if ($Preflight) { Assert-Preflight $summary $completion $summaries[0].DirectoryName }
    $elapsedNs = [long]($watch.Elapsed.TotalMilliseconds * 1000000.0)
    if (-not $Preflight) { $row = New-ResultRow $summary $Config $Stage $Sample $elapsedNs }
    $appEvidence = Join-Path (Join-Path $evidenceRoot 'app-results') $label
    New-Item -ItemType Directory -Path (Split-Path -Parent $appEvidence) -Force | Out-Null
    Move-Item -LiteralPath $outputPath -Destination $appEvidence
    if ($Preflight) {
        $script:completedPreflightProcesses++
        Write-Host "frame pacing preflight passed stage=$Stage"
    } else {
        $script:rowsByStage[$Stage] += $row
        $script:completedProcesses++
        Write-StageSummaries
        Write-Host "frame pacing progress=$script:completedProcesses/$expectedMeasuredProcesses stage=$Stage configuration=$($Config.Name) sample=$Sample"
    }
    Write-RunMetadata 'RUNNING' $null
}

function Move-PendingAppOutput {
    $destinationRoot = Join-Path $evidenceRoot 'app-results'
    New-Item -ItemType Directory -Path $destinationRoot -Force | Out-Null
    $pending = @(Get-ChildItem -LiteralPath $bundleRoot -Directory |
        Where-Object { $_.Name -like "p-$PID-*" })
    foreach ($directory in $pending) {
        $destination = Join-Path $destinationRoot $directory.Name
        if (-not (Test-Path -LiteralPath $destination)) {
            Move-Item -LiteralPath $directory.FullName -Destination $destination
        }
    }
}

function Validate-Bundle {
    $manifestPath = Join-Path $bundleRoot 'manifest.json'
    if (Test-Path -LiteralPath $manifestPath -PathType Leaf) {
        Copy-Item -LiteralPath $manifestPath -Destination (Join-Path $evidenceRoot 'manifest.json') -Force
    }
    $manifest = Read-JsonFile $manifestPath
    if ($manifest.benchmark -ne 'image-scroll' -or $manifest.target -ne 'windows-x64' -or
        $manifest.framePacingRunner -ne 'run-frame-pacing-benchmark-windows.ps1' -or
        $manifest.framePacingRunnerCompanion -ne 'frame-pacing-benchmark-windows-functions.ps1' -or
        $manifest.framePacingSchemaVersion -ne 1 -or
        $manifest.framePacingMeasuredProcessCount -ne 48 -or
        (@($manifest.framePacingStages) -join ',') -ne '1,2,3,4,5') {
        throw 'Manifest frame-pacing package contract is invalid'
    }
    if ($manifest.datasetFileCount -ne 663 -or $manifest.contentFormatCounts.total -ne 663 -or
        $manifest.contentFormatCounts.jpeg -ne 660 -or $manifest.contentFormatCounts.png -ne 3) {
        throw 'Manifest dataset content counts are not 663/660/3'
    }
    if ([string]$manifest.datasetHash -notmatch '^[0-9a-fA-F]{16}$' -or
        [string]$manifest.runtimeSha256 -notmatch '^[0-9a-fA-F]{64}$' -or
        [string]$manifest.sourceCommit -notmatch '^[0-9a-f]{40}$') {
        throw 'Manifest dataset, runtime, or source identity is invalid'
    }
    $script:sourceCommit = [string]$manifest.sourceCommit
    $executablePath = Join-Path $bundleRoot ([string]$manifest.executable)
    $runtimePath = Join-Path $bundleRoot ([string]$manifest.runtime)
    $corpusPath = Join-Path $bundleRoot 'corpus/imag'
    foreach ($path in @($executablePath,$runtimePath)) {
        if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "Bundle file is missing: $path" }
    }
    if (-not (Test-Path -LiteralPath $corpusPath -PathType Container)) { throw "Corpus is missing: $corpusPath" }
    $script:runtimeSha256 = Get-Sha256 $runtimePath
    if ($script:runtimeSha256 -cne [string]$manifest.runtimeSha256 -or
        ($null -ne $manifest.tcvmSha256 -and $script:runtimeSha256 -cne [string]$manifest.tcvmSha256)) {
        throw 'Bundle runtime SHA-256 differs from manifest'
    }
    $digest = Get-DatasetDigest $corpusPath
    $script:datasetHash = $digest.hash
    if ($script:datasetHash -cne [string]$manifest.datasetHash -or
        $digest.total -ne 663 -or $digest.jpeg -ne 660 -or $digest.png -ne 3) {
        throw 'Bundle dataset hash or content counts differ from manifest'
    }
    $script:manifest = $manifest
    $script:executablePath = $executablePath
}

try {
    New-Item -ItemType Directory -Path $resultsRoot -Force | Out-Null
    New-Item -ItemType Directory -Path $evidenceRoot -Force | Out-Null
    $manifestPath = Join-Path $bundleRoot 'manifest.json'
    $runnerCopies = Join-Path $evidenceRoot 'runner-copies'
    New-Item -ItemType Directory -Path $runnerCopies -Force | Out-Null
    if (Test-Path -LiteralPath $manifestPath -PathType Leaf) {
        Copy-Item -LiteralPath $manifestPath -Destination (Join-Path $evidenceRoot 'manifest.json') -Force
    }
    foreach ($runnerName in @('run-frame-pacing-benchmark-windows.ps1','frame-pacing-benchmark-windows-functions.ps1')) {
        $runnerFile = Join-Path $bundleRoot $runnerName
        if (Test-Path -LiteralPath $runnerFile -PathType Leaf) {
            Copy-Item -LiteralPath $runnerFile -Destination $runnerCopies -Force
        }
    }
    . (Join-Path $PSScriptRoot 'frame-pacing-benchmark-windows-functions.ps1')
    Validate-Bundle
    Write-RunMetadata 'RUNNING' $null
    for ($stage = 1; $stage -le 5; $stage++) {
        $configs = @($matrix | Where-Object { $_.Stage -eq $stage })
        if ($configs.Count -eq 0) { throw "Stage $stage has no configurations" }
        $preflight = Invoke-BenchmarkProcess $configs[0] $stage 0 0 'on' -Preflight
        for ($configIndex = 0; $configIndex -lt $configs.Count; $configIndex++) {
            $config = $configs[$configIndex]
            for ($sample = 1; $sample -le 3; $sample++) {
                Invoke-BenchmarkProcess $config $stage ($configIndex + 1) $sample 'off'
            }
        }
        if ($script:rowsByStage[$stage].Count -ne ($configs.Count * 3)) {
            throw "Stage $stage did not complete three processes per configuration"
        }
    }
    if ($script:completedProcesses -ne $expectedMeasuredProcesses) {
        throw "Expected $expectedMeasuredProcesses measured processes; completed $script:completedProcesses"
    }
    Write-StageSummaries
    Write-RunMetadata 'PASS' $null
    Compress-Archive -Path $evidenceRoot -DestinationPath $archivePath -Force
    Write-Host "frame pacing benchmark passed,processes=$completedProcesses,preflights=$completedPreflightProcesses,archive=$archivePath"
} catch {
    $failureMessage = $_.Exception.Message
    Write-Host "frame pacing benchmark failed: $failureMessage"
    try {
        Move-PendingAppOutput
        Write-StageSummaries
        Write-RunMetadata 'FAILED' $failureMessage
        Compress-Archive -Path $evidenceRoot -DestinationPath $archivePath -Force
        Write-Host "failure evidence archive=$archivePath"
    } catch {
        Write-Host "failure metadata or ZIP could not be written: $($_.Exception.Message)"
    }
    exit 1
}
