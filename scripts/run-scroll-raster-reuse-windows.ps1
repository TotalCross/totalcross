# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$script:bundleRoot = $PSScriptRoot
$script:resultsRoot = Join-Path $script:bundleRoot 'results'
$script:timestamp = Get-Date -Format 'yyyyMMdd-HHmmss-fff'
$script:runId = "$($script:timestamp)-$PID"
$script:evidenceRoot = Join-Path $script:resultsRoot $script:runId
$script:archivePath = Join-Path $script:resultsRoot "scroll-raster-reuse-windows-results-$($script:timestamp).zip"
$script:manifest = $null
$script:datasetHash = $null
$script:runtimeSha256 = $null
$script:failureMessage = $null
$script:completedPreflights = 0
$script:completedMeasured = 0
$script:processSummaries = New-Object 'System.Collections.Generic.List[object]'
$script:preflightSummaries = New-Object 'System.Collections.Generic.List[object]'

function Write-ExecutionMetadata {
    param([string]$Status, [string]$Failure)
    $sourceCommit = $null
    $sdkSourceAttestation = $null
    $sdkZipSha256 = $null
    $contentFormatCounts = $null
    if ($null -ne $script:manifest) {
        $sourceCommit = $script:manifest.sourceCommit
        $sdkSourceAttestation = $script:manifest.sdkSourceAttestation
        $sdkZipSha256 = $script:manifest.sdkZipSha256
        $contentFormatCounts = $script:manifest.contentFormatCounts
    }
    $metadata = [PSCustomObject]@{
        schemaVersion = 1
        benchmark = 'scroll-raster-reuse-windows'
        target = 'windows-x64'
        status = $Status
        createdAt = $script:timestamp
        sourceCommit = $sourceCommit
        sdkSourceAttestation = $sdkSourceAttestation
        sdkZipSha256 = $sdkZipSha256
        runtimeSha256 = $script:runtimeSha256
        datasetHash = $script:datasetHash
        contentFormatCounts = $contentFormatCounts
        expectedPreflightProcesses = 2
        completedPreflightProcesses = $script:completedPreflights
        expectedMeasuredProcesses = 6
        completedMeasuredProcesses = $script:completedMeasured
        measuredModeOrder = @('off','off','off','on','on','on')
        windowsExecuted = $true
        failure = $Failure
        archivePath = $script:archivePath
    }
    ConvertTo-Json -InputObject $metadata -Depth 6 |
        Set-Content -LiteralPath (Join-Path $script:evidenceRoot 'execution-metadata.json') -Encoding UTF8
}

function Save-ProcessSummary {
    param([PSCustomObject]$Summary, [string]$Label)
    $summaryDirectory = Join-Path $script:evidenceRoot 'process-summaries'
    New-Item -ItemType Directory -Path $summaryDirectory -Force | Out-Null
    ConvertTo-Json -InputObject $Summary -Depth 8 |
        Set-Content -LiteralPath (Join-Path $summaryDirectory "$Label.json") -Encoding UTF8
}

function Move-RunLogs {
    param([string]$Label, [string]$StdoutPath, [string]$StderrPath)
    $logDirectory = Join-Path $script:evidenceRoot 'logs'
    New-Item -ItemType Directory -Path $logDirectory -Force | Out-Null
    $debugPath = Join-Path $script:bundleRoot 'DebugConsole.txt'
    $reportPath = Join-Path $script:bundleRoot 'ImageScrollRealWorkloadBenchmarkApp.log'
    $paths = @(
        [PSCustomObject]@{Path=$StdoutPath;Name="$Label.stdout.log"}
        [PSCustomObject]@{Path=$StderrPath;Name="$Label.stderr.log"}
        [PSCustomObject]@{Path=$debugPath;Name="$Label.DebugConsole.log"}
        [PSCustomObject]@{Path=$reportPath;Name="$Label.application.log"}
    )
    foreach ($item in $paths) {
        $source = [string]$item.Path
        if (Test-Path -LiteralPath $source -PathType Leaf) {
            Move-Item -LiteralPath $source -Destination (Join-Path $logDirectory ([string]$item.Name)) -Force
        }
    }
}

function Invoke-BenchmarkProcess {
    param([string]$Mode, [int]$Sample, [string]$Accounting, [switch]$Preflight)
    $label = if ($Preflight) { "preflight-$Mode" } else { "measured-$Mode-$Sample" }
    $outputRelative = "p-$PID-$($script:timestamp.Replace('-', ''))-$label"
    $outputPath = Join-Path $script:bundleRoot $outputRelative
    if (Test-Path -LiteralPath $outputPath) { throw "Duplicate app output directory: $outputPath" }
    $logDirectory = Join-Path $script:evidenceRoot 'logs'
    New-Item -ItemType Directory -Path $logDirectory -Force | Out-Null
    $stdoutPath = Join-Path $logDirectory "$label.stdout.log"
    $stderrPath = Join-Path $logDirectory "$label.stderr.log"
    $arguments = @(
        '/scr','-1,-1,540,960','--app-root=.','--corpus=corpus/imag',"--output=$outputRelative",
        '--profile=scroll-raster-reuse-windows',"--accounting=$Accounting",
        "--rendering-reuse=$Mode",'--flick-config=timer,60,nano,absolute,poll,legacy',"--run=$Sample"
    )
    if ([string]::Join(' ', $arguments[2..($arguments.Count - 1)]).Length -gt 255) {
        throw "Native command line exceeds 255 characters for $label"
    }
    Write-Host "scroll raster reuse starting label=$label mode=$Mode accounting=$Accounting"
    $environmentValues = @{
        TC_TIMER_DEADLINE_MODE='absolute'
        TC_EVENT_LOOP_MODE='poll'
        TC_THREAD_YIELD_MODE='legacy'
        TC_SDL_PIXEL_FORMAT='auto'
        TC_SCROLL_RASTER_REUSE_BENCHMARK='1'
    }
    $previousEnvironment = @{}
    $process = $null
    $timedOut = $false
    $exitCode = -1
    $watch = [System.Diagnostics.Stopwatch]::StartNew()
    try {
        foreach ($name in $environmentValues.Keys) {
            $previousEnvironment[$name] = [Environment]::GetEnvironmentVariable($name, 'Process')
            [Environment]::SetEnvironmentVariable($name, [string]$environmentValues[$name], 'Process')
        }
        $executable = Join-Path $script:bundleRoot ([string]$script:manifest.executable)
        $process = Start-Process -FilePath $executable -ArgumentList $arguments `
            -WorkingDirectory $script:bundleRoot -RedirectStandardOutput $stdoutPath `
            -RedirectStandardError $stderrPath -PassThru
        $null = $process.Handle
        while (-not $process.WaitForExit(15000)) {
            if ($watch.ElapsedMilliseconds -ge 900000) {
                $timedOut = $true
                try { $process.Kill() } catch { }
                break
            }
            Write-Host "scroll raster reuse still running label=$label elapsed_seconds=$([int]$watch.Elapsed.TotalSeconds)"
        }
        $process.WaitForExit()
        $exitCode = $process.ExitCode
    } finally {
        $watch.Stop()
        if ($null -ne $process) { $process.Dispose() }
        foreach ($name in $environmentValues.Keys) {
            [Environment]::SetEnvironmentVariable($name, $previousEnvironment[$name], 'Process')
        }
        Move-RunLogs $label $stdoutPath $stderrPath
        if (Test-Path -LiteralPath $outputPath -PathType Container) {
            $destination = Join-Path (Join-Path $script:evidenceRoot 'app-results') $label
            New-Item -ItemType Directory -Path (Split-Path -Parent $destination) -Force | Out-Null
            Move-Item -LiteralPath $outputPath -Destination $destination -Force
        }
    }
    if ($timedOut) { throw "$label timed out after 900000 ms (exit $exitCode)" }
    if ($exitCode -ne 0) { throw "$label exited with code $exitCode" }
    $appOutput = Join-Path (Join-Path $script:evidenceRoot 'app-results') $label
    $logRoot = Join-Path $script:evidenceRoot 'logs'
    $logPaths = @(
        (Join-Path $logRoot "$label.stdout.log"),(Join-Path $logRoot "$label.stderr.log"),
        (Join-Path $logRoot "$label.DebugConsole.log"),(Join-Path $logRoot "$label.application.log")
    )
    $completion = Get-CompletionRecord $logPaths
    if ($completion['profile'] -ne 'scroll-raster-reuse-windows' -or
        $completion['passes'] -ne '2' -or $completion['image_count'] -ne '663' -or
        $completion['prefetch_profile'] -ne 'on' -or $completion['accounting'] -ne $Accounting -or
        $completion['prefetch_thread_mode'] -ne 'worker-semaphore' -or
        $completion['prefetch_worker_sleep_ms'] -ne '0' -or
        $completion['requested_mask'] -ne '6' -or $completion['effective_mask'] -ne '6' -or
        $completion['flick_driver'] -ne 'timer' -or $completion['flick_fps'] -ne '60' -or
        $completion['flick_clock'] -ne 'nano' -or $completion['timer_deadline_mode'] -ne 'absolute' -or
        $completion['event_loop_mode'] -ne 'poll' -or $completion['thread_yield_mode'] -ne 'legacy') {
        throw "$label completion record differs from the fixed benchmark configuration"
    }
    $passRecords = @(Get-ScrollReuseRecords $logPaths)
    $pixelRecords = @{}
    foreach ($path in $logPaths) {
        if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { continue }
        foreach ($line in (Get-Content -LiteralPath $path)) {
            if ($line -match '^TC_SCROLL_RASTER_REUSE_SDL_PIXEL_FORMAT=(.+)$') { $pixelRecords[$Matches[1]] = $true }
        }
    }
    if ($pixelRecords.Count -ne 1) { throw "$label did not report exactly one SDL pixel format" }
    $pixelFormat = [string]@($pixelRecords.Keys)[0]
    $summary = Get-ProcessSummary $Mode $Sample $appOutput $completion $passRecords $pixelFormat $exitCode
    if ($summary.logicalWidth -ne 540 -or $summary.logicalHeight -ne 960 -or
        $summary.drawableWidth -le 0 -or $summary.drawableHeight -le 0) {
        throw "$label logical or SDL drawable dimensions are invalid"
    }
    $summary | Add-Member -NotePropertyName processWallNs -NotePropertyValue ([long]($watch.Elapsed.TotalMilliseconds * 1000000.0))
    if ($Preflight) {
        Assert-PreflightRun $summary $completion $passRecords $Mode
        $script:completedPreflights++
        $script:preflightSummaries.Add([PSCustomObject]@{ summary=$summary; waypointPath=$summary.waypointsPath })
        Write-Host "scroll raster reuse preflight passed mode=$Mode"
    } else {
        if ($Accounting -ne 'off') { throw 'Measured processes must keep accounting disabled' }
        $script:completedMeasured++
        $script:processSummaries.Add($summary)
        Save-ProcessSummary $summary "$Mode-$Sample"
        Write-Host "scroll raster reuse progress=$($script:completedMeasured)/6 mode=$Mode sample=$Sample"
    }
    Write-ExecutionMetadata 'RUNNING' $null
    return $summary
}

function Write-ProcessSummaryCsv {
    $fields = @(
        'mode','sample','exitCode','processWallNs','imageCount','prefetchRequestCount','prefetchReadyCount',
        'prefetchFailedCount','prefetchNotPrefetchableCount','frameCount','measuredFrameCount',
        'frameIntervalP50Ns','frameIntervalP95Ns','frameIntervalP99Ns','frameIntervalMaxNs',
        'activeWorkP50Ns','activeWorkP95Ns','activeWorkP99Ns','activeWorkMaxNs',
        'paintP50Ns','paintP95Ns','paintP99Ns','paintMaxNs',
        'screenUpdateP50Ns','screenUpdateP95Ns','screenUpdateP99Ns','screenUpdateMaxNs',
        'framesOver16_67Count','framesOver20Count','framesOver22_22Count',
        'framesOver25Count','framesOver33_3Count','framesOver50Count',
        'attempts','hits','fallbacks','hitRate','reusedPixels','dirtyPixels','movedBytes','postMoveRecoveries',
        'scrollJpegDecodes','scrollTargetedJpegDecodes','scrollFullJpegDecodes','scrollImageMaterializations',
        'scrollNativeGeometryMaterializations','expectedFinalDisplacement','actualFinalDisplacement',
        'logicalWidth','logicalHeight','drawableWidth','drawableHeight',
        'surfaceScaleX','surfaceScaleY','pixelFormat','rendererBackend','refreshRate'
    )
    $script:processSummaries | Select-Object $fields |
        Export-Csv -LiteralPath (Join-Path $script:evidenceRoot 'process-summaries.csv') -NoTypeInformation -Encoding UTF8
}

function Write-PassSummaryCsv {
    $fields = @(
        'mode','sample','pass','frameCount','measuredFrameCount',
        'frameIntervalP50Ns','frameIntervalP95Ns','frameIntervalP99Ns','frameIntervalMaxNs',
        'activeWorkP50Ns','activeWorkP95Ns','activeWorkP99Ns','activeWorkMaxNs',
        'paintP50Ns','paintP95Ns','paintP99Ns','paintMaxNs',
        'screenUpdateP50Ns','screenUpdateP95Ns','screenUpdateP99Ns','screenUpdateMaxNs',
        'framesOver16_67Count','framesOver20Count','framesOver22_22Count',
        'framesOver25Count','framesOver33_3Count','framesOver50Count'
    )
    $rows = New-Object 'System.Collections.Generic.List[object]'
    foreach ($summary in $script:processSummaries) {
        foreach ($passSummary in $summary.passSummaries) { $rows.Add($passSummary) }
    }
    $rows | Select-Object $fields |
        Export-Csv -LiteralPath (Join-Path $script:evidenceRoot 'pass-summaries.csv') -NoTypeInformation -Encoding UTF8
}

try {
    New-Item -ItemType Directory -Path $script:evidenceRoot -Force | Out-Null
    . (Join-Path $script:bundleRoot 'scroll-raster-reuse-windows-functions.ps1')
    . (Join-Path $script:bundleRoot 'scroll-raster-reuse-windows-analysis.ps1')
    foreach ($name in @('manifest.json','run-scroll-raster-reuse-windows.ps1',
        'scroll-raster-reuse-windows-functions.ps1','scroll-raster-reuse-windows-analysis.ps1')) {
        $source = Join-Path $script:bundleRoot $name
        if (Test-Path -LiteralPath $source -PathType Leaf) {
            Copy-Item -LiteralPath $source -Destination $script:evidenceRoot -Force
        }
    }
    $script:manifest = Read-JsonFile (Join-Path $script:bundleRoot 'manifest.json')
    if ($script:manifest.benchmark -ne 'scroll-raster-reuse-windows' -or
        $script:manifest.target -ne 'windows-x64' -or $script:manifest.datasetFileCount -ne 663 -or
        $script:manifest.measuredProcessCount -ne 6 -or $script:manifest.preflightProcessCount -ne 2 -or
        ($script:manifest.measuredModeOrder -join ',') -ne 'off,off,off,on,on,on' -or
        $script:manifest.executable -ne 'ImageScrollRealWorkloadBenchmarkApp.exe' -or
        $script:manifest.runtime -ne 'tcvm.dll' -or $script:manifest.columns -ne 3 -or
        $script:manifest.logicalWidth -ne 540 -or $script:manifest.logicalHeight -ne 960 -or
        $script:manifest.imageOptimizationMask -ne 6 -or $script:manifest.prefetch -ne 'on' -or
        $script:manifest.prefetchThreadMode -ne 'worker-semaphore' -or
        $script:manifest.flickDriver -ne 'TimerEvent' -or $script:manifest.flickFrameRate -ne 60 -or
        $script:manifest.flickClock -ne 'nano' -or $script:manifest.timerDeadlineMode -ne 'absolute' -or
        $script:manifest.eventLoopMode -ne 'poll' -or $script:manifest.threadYieldMode -ne 'legacy' -or
        $script:manifest.sdlPixelFormatRequest -ne 'auto' -or $script:manifest.passCount -ne 2 -or
        $script:manifest.renderingReuseBit -ne 1) {
        throw 'Package manifest does not describe the required six-process matrix'
    }
    if ((Get-Sha256 (Join-Path $script:bundleRoot 'run-scroll-raster-reuse-windows.ps1')) -cne
        [string]$script:manifest.runnerSha256 -or
        (Get-Sha256 (Join-Path $script:bundleRoot 'scroll-raster-reuse-windows-functions.ps1')) -cne
        [string]$script:manifest.runnerCompanionSha256 -or
        (Get-Sha256 (Join-Path $script:bundleRoot 'scroll-raster-reuse-windows-analysis.ps1')) -cne
        [string]$script:manifest.runnerAnalysisSha256) {
        throw 'Packaged PowerShell runner differs from the manifest'
    }
    $runtimePath = Join-Path $script:bundleRoot ([string]$script:manifest.runtime)
    $script:runtimeSha256 = Get-Sha256 $runtimePath
    if ($script:runtimeSha256 -cne [string]$script:manifest.runtimeSha256 -or
        $script:runtimeSha256 -cne [string]$script:manifest.tcvmSha256) {
        throw 'Packaged tcvm.dll differs from the manifest'
    }
    $corpusPath = Join-Path $script:bundleRoot 'corpus/imag'
    $digest = Get-CorpusDigest $corpusPath
    $script:datasetHash = $digest.hash
    if ($digest.hash -cne [string]$script:manifest.datasetHash -or
        $digest.total -ne 663 -or $digest.jpeg -ne 660 -or $digest.png -ne 3) {
        throw 'Corpus digest or content magic counts differ from the manifest'
    }
    Write-Host 'scroll raster reuse package validation passed; starting two preflights'
    Write-ExecutionMetadata 'RUNNING' $null
    $offPreflight = Invoke-BenchmarkProcess 'off' 0 'on' -Preflight
    $onPreflight = Invoke-BenchmarkProcess 'on' 0 'on' -Preflight
    $offWaypoints = $script:preflightSummaries[0].waypointPath
    $onWaypoints = $script:preflightSummaries[1].waypointPath
    $waypointResult = Assert-PreflightMatch $offWaypoints $onWaypoints
    foreach ($summary in @($offPreflight, $onPreflight)) {
        if ($summary.expectedFinalDisplacement -ne $waypointResult.finalDisplacement -or
            $summary.actualFinalDisplacement -ne $waypointResult.finalDisplacement) {
            throw "Preflight $($summary.mode) final displacement differs from its matching waypoints"
        }
    }
    $preflightResult = [PSCustomObject]@{
        status='PASS'; preflightProcessCount=2; imageCount=663; jpegCount=660; pngCount=3
        prefetchRequestCount=663; prefetchReadyCount=663; prefetchFailedCount=0
        prefetchNotPrefetchableCount=0; scrollTimeDecodeAndMaterializationCounts=0
        matchedWaypointCount=$waypointResult.matchedWaypointCount
        finalScrollDisplacement=$waypointResult.finalDisplacement
        offExpectedFinalDisplacement=$offPreflight.expectedFinalDisplacement
        offActualFinalDisplacement=$offPreflight.actualFinalDisplacement
        onExpectedFinalDisplacement=$onPreflight.expectedFinalDisplacement
        onActualFinalDisplacement=$onPreflight.actualFinalDisplacement
        off=$offPreflight; on=$onPreflight; matchingWaypoints=$waypointResult.matches
    }
    ConvertTo-Json -InputObject $preflightResult -Depth 9 |
        Set-Content -LiteralPath (Join-Path $script:evidenceRoot 'preflight-result.json') -Encoding UTF8
    Write-Host "scroll raster reuse preflight passed matching_waypoints=$($waypointResult.matchedWaypointCount) displacement=$($waypointResult.finalDisplacement)"
    foreach ($mode in @('off','on')) {
        for ($sample = 1; $sample -le 3; $sample++) {
            [void](Invoke-BenchmarkProcess $mode $sample 'off')
        }
    }
    if ($script:completedPreflights -ne 2 -or $script:completedMeasured -ne 6) {
        throw "Expected two preflights and six measured processes; completed $($script:completedPreflights)/$($script:completedMeasured)"
    }
    Write-ProcessSummaryCsv
    Write-PassSummaryCsv
    Write-AggregateComparison $script:processSummaries.ToArray() `
        (Join-Path $script:evidenceRoot 'aggregate-comparison.csv')
    Write-ExecutionMetadata 'PASS' $null
    Write-Host "scroll raster reuse benchmark passed measured=$($script:completedMeasured) preflights=$($script:completedPreflights) archive=$($script:archivePath)"
    Compress-Archive -Path $script:evidenceRoot -DestinationPath $script:archivePath -Force
} catch {
    $script:failureMessage = $_.Exception.Message
    Write-Host "scroll raster reuse benchmark failed: $($script:failureMessage)"
    try {
        if (-not (Test-Path -LiteralPath $script:evidenceRoot -PathType Container)) {
            New-Item -ItemType Directory -Path $script:evidenceRoot -Force | Out-Null
        }
        if ($null -ne $script:manifest -and $script:processSummaries.Count -gt 0) { Write-ProcessSummaryCsv }
        Write-ExecutionMetadata 'FAILED' $script:failureMessage
        Compress-Archive -Path $script:evidenceRoot -DestinationPath $script:archivePath -Force
        Write-Host "failure evidence archive=$($script:archivePath)"
    } catch {
        Write-Host "failure metadata or ZIP could not be written: $($_.Exception.Message)"
    }
    exit 1
}

exit 0
