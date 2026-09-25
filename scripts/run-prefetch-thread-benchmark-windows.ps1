# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$timeoutMilliseconds = 180000
$bundleRoot = $PSScriptRoot
$timestamp = Get-Date -Format 'yyyyMMdd-HHmmss-fff'
$resultName = "prefetch-thread-$timestamp-$PID"
$resultsRoot = Join-Path $bundleRoot 'results'
$evidenceRoot = Join-Path $resultsRoot $resultName
$archivePath = Join-Path $resultsRoot "$resultName.zip"
$appOutputRelative = $null
$appOutputRoot = $null
$rows = @()
$runLabels = @()
$completedProcesses = 0
$failureMessage = $null

function Write-RunMetadata {
    param([string]$Status, [string]$Failure)

    $metadata = [PSCustomObject]@{
        benchmark = 'prefetch-thread-diagnostics'
        target = 'windows-x64'
        status = $Status
        createdAt = $timestamp
        completedProcesses = $completedProcesses
        expectedProcesses = 6
        datasetHash = $manifestHash
        failure = $Failure
        archivePath = $archivePath
    }
    $metadataPath = Join-Path $evidenceRoot 'metadata.json'
    ConvertTo-Json -InputObject $metadata -Depth 4 -Compress |
        Set-Content -LiteralPath $metadataPath -Encoding UTF8
}

function Assert-Equal {
    param($Actual, $Expected, [string]$Description)

    if ($null -eq $Actual -or [long]$Actual -ne [long]$Expected) {
        throw "$Description expected $Expected, found $Actual"
    }
}

function Read-Count {
    param($Object, [string]$Name, [string]$Description)

    $property = $Object.PSObject.Properties[$Name]
    if ($null -eq $property -or $null -eq $property.Value) {
        throw "$Description is missing $Name"
    }
    try {
        $value = [long]$property.Value
    } catch {
        throw "$Description $Name is not an integer"
    }
    if ($value -lt 0) {
        throw "$Description $Name is negative"
    }
    return $value
}

function Read-JsonFile {
    param([string]$Path)

    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        throw "Required JSON artifact is missing: $Path"
    }
    return (Get-Content -LiteralPath $Path -Raw | ConvertFrom-Json -ErrorAction Stop)
}

function ConvertTo-Base36 {
    param([long]$Value)

    $digits = '0123456789abcdefghijklmnopqrstuvwxyz'
    $text = ''
    do {
        $remainder = [int]($Value % 36)
        $text = [string]$digits[$remainder] + $text
        $Value = [long][Math]::Floor($Value / 36)
    } while ($Value -gt 0)
    return $text
}

function Assert-ThreadRun {
    param(
        $Summary,
        $Counters,
        [int]$Mask,
        [string]$Mode,
        [int]$SleepMs,
        [string]$RunDirectory
    )

    if ($Summary.status -ne 'PASS' -or $Summary.prefetch -ne 'on' -or
        $Summary.accounting -ne 'on' -or $Summary.prefetchThreadMode -ne $Mode -or
        $Summary.prefetchWorkerSleepMs -ne $SleepMs -or
        $Summary.requestedMask -ne $Mask -or $Summary.effectiveMask -ne $Mask) {
        throw "summary configuration or status mismatch in $RunDirectory"
    }
    Assert-Equal $Summary.imageCount 663 "$RunDirectory imageCount"
    $requests = Read-Count $Summary 'prefetchRequestCount' $RunDirectory
    $ready = Read-Count $Summary 'prefetchReadyCount' $RunDirectory
    $failed = Read-Count $Summary 'prefetchFailedCount' $RunDirectory
    $notPrefetchable = Read-Count $Summary 'prefetchNotPrefetchableCount' $RunDirectory
    Assert-Equal $requests 663 "$RunDirectory prefetch requests"
    Assert-Equal $ready 663 "$RunDirectory prefetch ready"
    Assert-Equal $failed 0 "$RunDirectory prefetch failed"
    Assert-Equal $notPrefetchable 0 "$RunDirectory prefetch not-prefetchable"
    Assert-Equal ($ready + $failed + $notPrefetchable) $requests `
        "$RunDirectory preparation outcomes"
    if ($Counters.accountingEnabled -ne $true -or $null -eq $Counters.prefetchPhases) {
        throw "$RunDirectory counters.json lacks enabled prefetch phases"
    }

    $phases = $Counters.prefetchPhases
    if ($phases.prefetchThreadMode -ne $Mode -or
        $phases.prefetchWorkerSleepMs -ne $SleepMs) {
        throw "$RunDirectory counter thread configuration mismatch"
    }
    $prepared = Read-Count $phases 'preparationEntryCount' $RunDirectory
    $decoded = Read-Count $phases 'decodeEntryCount' $RunDirectory
    $dispatches = Read-Count $phases 'uiDispatchCount' $RunDirectory
    $created = Read-Count $phases 'threadCreateCount' $RunDirectory
    $started = Read-Count $phases 'threadStartCount' $RunDirectory
    $polls = Read-Count $phases 'workerPollCount' $RunDirectory
    $sleepRequested = Read-Count $phases 'workerSleepRequestedNs' $RunDirectory
    $idleElapsed = Read-Count $phases 'workerIdleElapsedNs' $RunDirectory
    $releases = Read-Count $phases 'workerSemaphoreReleaseCount' $RunDirectory
    $acquires = Read-Count $phases 'workerSemaphoreAcquireCount' $RunDirectory
    $wakes = Read-Count $phases 'workerSemaphoreWakeCount' $RunDirectory
    $outstanding = Read-Count $phases 'workerSemaphoreOutstandingWakeCount' $RunDirectory
    Assert-Equal $prepared $decoded "$RunDirectory preparation/decode entries"
    Assert-Equal $dispatches $decoded "$RunDirectory UI dispatch entries"
    if ($decoded -gt $ready -or $started -ne $created) {
        throw "$RunDirectory decode or thread start counts are inconsistent"
    }

    if ($decoded -gt 0) {
        if ($Mode -eq 'legacy') {
            Assert-Equal $created $decoded "$RunDirectory legacy thread count"
            if ($polls -ne 0 -or $sleepRequested -ne 0 -or $idleElapsed -ne 0 -or
                $releases -ne 0 -or $acquires -ne 0 -or
                $wakes -ne 0 -or $outstanding -ne 0) {
                throw "$RunDirectory legacy mode recorded polling or semaphore activity"
            }
        } elseif ($Mode -eq 'worker-poll') {
            Assert-Equal $created 1 "$RunDirectory polling worker count"
            if ($polls -le 0 -or $releases -ne 0 -or $acquires -ne 0 -or
                $wakes -ne 0 -or $outstanding -ne 0) {
                throw "$RunDirectory polling mode counters are inconsistent"
            }
            Assert-Equal $sleepRequested ($polls * $SleepMs * 1000000) `
                "$RunDirectory requested worker sleep"
        } else {
            Assert-Equal $created 1 "$RunDirectory semaphore worker count"
            if ($polls -ne 0 -or $sleepRequested -ne 0 -or $idleElapsed -ne 0 -or
                $releases -ne $acquires -or $releases -ne $wakes -or
                $outstanding -ne 0 -or $releases -gt $decoded) {
                throw "$RunDirectory semaphore notifications are unbalanced"
            }
        }
    } elseif ($created -ne 0 -or $polls -ne 0 -or $releases -ne 0 -or
        $acquires -ne 0 -or $wakes -ne 0 -or $outstanding -ne 0) {
        throw "$RunDirectory recorded worker activity without decode entries"
    }
    foreach ($name in @(
        'workerSemaphoreReleaseCount', 'workerSemaphoreAcquireCount',
        'workerSemaphoreWakeCount', 'workerSemaphoreOutstandingWakeCount'
    )) {
        Assert-Equal (Read-Count $Summary $name "$RunDirectory summary") `
            (Read-Count $phases $name "$RunDirectory counters") `
            "$RunDirectory summary/counter $name"
    }

    return [PSCustomObject]@{
        mode = $Mode
        worker_sleep_ms = $SleepMs
        mask = $Mask
        image_count = 663
        prefetch_request_count = $requests
        prefetch_ready_count = $ready
        prefetch_failed_count = $failed
        prefetch_not_prefetchable_count = $notPrefetchable
        preparation_entry_count = $prepared
        decode_entry_count = $decoded
        thread_create_count = $created
        thread_start_count = $started
        worker_poll_count = $polls
        worker_semaphore_release_count = $releases
        worker_semaphore_acquire_count = $acquires
        worker_semaphore_wake_count = $wakes
        worker_semaphore_outstanding_wake_count = $outstanding
    }
}

try {
    New-Item -ItemType Directory -Path $resultsRoot -Force | Out-Null
    New-Item -ItemType Directory -Path $evidenceRoot | Out-Null
    Write-RunMetadata 'RUNNING' $null

    $manifestPath = Join-Path $bundleRoot 'manifest.json'
    $manifest = Read-JsonFile $manifestPath
    if ($manifest.target -ne 'windows-x64') {
        throw 'This runner requires a windows-x64 bundle'
    }
    if ($manifest.datasetFileCount -ne 663 -or
        $manifest.contentFormatCounts.total -ne 663 -or
        $manifest.contentFormatCounts.jpeg -ne 660 -or
        $manifest.contentFormatCounts.png -ne 3) {
        throw 'manifest dataset or content format counts are not 663/660/3'
    }
    if ($manifest.windowsPrefetchThreadRunner -ne
        'run-prefetch-thread-benchmark-windows.ps1') {
        throw 'manifest does not name this Windows prefetch thread runner'
    }
    $manifestHash = [string]$manifest.datasetHash
    if ($manifestHash -notmatch '^[0-9a-f]{16}$') {
        throw 'manifest dataset hash is missing or invalid'
    }

    $executablePath = Join-Path $bundleRoot ([string]$manifest.executable)
    $runtimePath = Join-Path $bundleRoot ([string]$manifest.runtime)
    $corpusPath = Join-Path $bundleRoot 'corpus/imag'
    if (-not (Test-Path -LiteralPath $executablePath -PathType Leaf)) {
        throw "Bundle executable is missing: $executablePath"
    }
    if (-not (Test-Path -LiteralPath $runtimePath -PathType Leaf)) {
        throw "Bundle runtime is missing: $runtimePath"
    }
    if (-not (Test-Path -LiteralPath $corpusPath -PathType Container)) {
        throw "Bundle image corpus is missing: $corpusPath"
    }
    $imageFiles = @(Get-ChildItem -LiteralPath $corpusPath -Recurse -File |
        Where-Object { @('.jpg', '.jpeg') -contains $_.Extension.ToLowerInvariant() })
    if ($imageFiles.Count -ne 663) {
        throw "Bundle corpus must contain 663 .jpg/.jpeg-named images; found $($imageFiles.Count)"
    }
    $actualFormats = @{ total = $imageFiles.Count; jpeg = 0; png = 0 }
    foreach ($image in $imageFiles) {
        $stream = [System.IO.File]::OpenRead($image.FullName)
        try {
            $signature = New-Object byte[] 8
            $read = $stream.Read($signature, 0, 8)
        } finally {
            $stream.Dispose()
        }
        if ($read -ge 2 -and $signature[0] -eq 255 -and $signature[1] -eq 216) {
            $actualFormats.jpeg++
        } elseif ($read -eq 8 -and $signature[0] -eq 137 -and
            $signature[1] -eq 80 -and $signature[2] -eq 78 -and
            $signature[3] -eq 71 -and $signature[4] -eq 13 -and
            $signature[5] -eq 10 -and $signature[6] -eq 26 -and
            $signature[7] -eq 10) {
            $actualFormats.png++
        } else {
            throw "Unsupported image content in $($image.Name)"
        }
    }
    if ($actualFormats.jpeg -ne 660 -or $actualFormats.png -ne 3) {
        throw "Bundle image content must contain 660 JPEG and 3 PNG files; found $($actualFormats.jpeg)/$($actualFormats.png)"
    }

    do {
        $epochSeconds = [long](([DateTime]::UtcNow - [datetime]'1970-01-01').TotalSeconds)
        $appOutputRelative = 'p' + (ConvertTo-Base36 $epochSeconds)
        $appOutputRoot = Join-Path $bundleRoot $appOutputRelative
        if (Test-Path -LiteralPath $appOutputRoot) {
            Start-Sleep -Milliseconds 250
        }
    } while (Test-Path -LiteralPath $appOutputRoot)

    $configurations = @(
        @{ Mode = 'legacy'; SleepMs = 0; Masks = @(6, 38) },
        @{ Mode = 'worker-poll'; SleepMs = 1; Masks = @(6, 38) },
        @{ Mode = 'worker-semaphore'; SleepMs = 0; Masks = @(6, 38) }
    )
    $order = 0
    foreach ($configuration in $configurations) {
        foreach ($mask in $configuration.Masks) {
            $order++
            $run = 1
            $runLabel = "prefetch-thread-$($configuration.Mode)-sleep-$($configuration.SleepMs)-mask-$mask-run-$run"
            if ($runLabels -contains $runLabel) {
                throw "Duplicate result path for $runLabel"
            }
            $runLabels += $runLabel
            $logLabel = "$($configuration.Mode)-sleep-$($configuration.SleepMs)-mask-$mask"
            $stdoutPath = Join-Path $evidenceRoot "$logLabel.stdout.log"
            $stderrPath = Join-Path $evidenceRoot "$logLabel.stderr.log"
            $debugConsole = Join-Path $bundleRoot 'DebugConsole.txt'
            if (Test-Path -LiteralPath $debugConsole -PathType Leaf) {
                Copy-Item -LiteralPath $debugConsole -Destination `
                    (Join-Path $evidenceRoot "$logLabel.DebugConsole.preexisting.txt")
                Remove-Item -LiteralPath $debugConsole
            }
            $arguments = @(
                '/scr', '-1,-1,540,960', '-p', '.', '--app-root=.',
                '--mode=benchmark', '--corpus=corpus/imag',
                "--output=$appOutputRelative", "--image-optimization=$mask",
                '--prefetch=on', '--accounting=on', "--run=$run",
                "--dataset-hash=$manifestHash",
                "--prefetch-thread-mode=$($configuration.Mode)",
                "--prefetch-worker-sleep-ms=$($configuration.SleepMs)", '--profile=pft'
            )
            $watch = [System.Diagnostics.Stopwatch]::StartNew()
            $process = $null
            $timedOut = $false
            try {
                $process = Start-Process -FilePath $executablePath `
                    -ArgumentList $arguments -WorkingDirectory $bundleRoot `
                    -RedirectStandardOutput $stdoutPath -RedirectStandardError $stderrPath `
                    -PassThru
                $null = $process.Handle
                if (-not $process.WaitForExit($timeoutMilliseconds)) {
                    $timedOut = $true
                    try {
                        $process.Kill()
                    } catch {
                    }
                }
                $process.WaitForExit()
                $exitCode = $process.ExitCode
            } finally {
                $watch.Stop()
                if (Test-Path -LiteralPath $debugConsole -PathType Leaf) {
                    Move-Item -LiteralPath $debugConsole -Destination `
                        (Join-Path $evidenceRoot "$logLabel.DebugConsole.txt")
                }
                if ($null -ne $process) {
                    $process.Dispose()
                }
            }
            if ($timedOut) {
                throw "$runLabel timed out after $timeoutMilliseconds ms (exit $exitCode)"
            }
            if ($exitCode -ne 0) {
                throw "$runLabel exited with code $exitCode"
            }
            $runDirectory = Join-Path (Join-Path $appOutputRoot 'runs') $runLabel
            $summary = Read-JsonFile (Join-Path $runDirectory 'summary.json')
            $counters = Read-JsonFile (Join-Path $runDirectory 'counters.json')
            $validated = Assert-ThreadRun $summary $counters $mask `
                $configuration.Mode $configuration.SleepMs $runDirectory
            $validated | Add-Member -NotePropertyName order -NotePropertyValue $order
            $validated | Add-Member -NotePropertyName status -NotePropertyValue 'PASS'
            $validated | Add-Member -NotePropertyName elapsed_ms `
                -NotePropertyValue ([long]$watch.ElapsedMilliseconds)
            $validated | Add-Member -NotePropertyName stdout_log -NotePropertyValue `
                ([System.IO.Path]::GetFileName($stdoutPath))
            $validated | Add-Member -NotePropertyName stderr_log -NotePropertyValue `
                ([System.IO.Path]::GetFileName($stderrPath))
            $Rows += $validated
            $appEvidenceRoot = Join-Path $evidenceRoot 'app-results'
            New-Item -ItemType Directory -Path $appEvidenceRoot -Force | Out-Null
            Copy-Item -LiteralPath $runDirectory -Destination `
                (Join-Path $appEvidenceRoot $runLabel) -Recurse
            $completedProcesses++
            Write-RunMetadata 'RUNNING' $null
            Write-Host "prefetch thread progress=$completedProcesses/6,mode=$($configuration.Mode),mask=$mask"
        }
    }
    if ($completedProcesses -ne 6 -or $runLabels.Count -ne 6) {
        throw "Expected exactly six unique runs; completed $completedProcesses"
    }
    Copy-Item -LiteralPath (Join-Path $appOutputRoot 'environment.json') `
        -Destination (Join-Path (Join-Path $evidenceRoot 'app-results') 'environment.json')

    $Rows | Select-Object order, mode, worker_sleep_ms, mask, status, image_count, `
        prefetch_request_count, prefetch_ready_count, prefetch_failed_count, `
        prefetch_not_prefetchable_count, preparation_entry_count, decode_entry_count, `
        thread_create_count, thread_start_count, worker_poll_count, `
        worker_semaphore_release_count, worker_semaphore_acquire_count, `
        worker_semaphore_wake_count, worker_semaphore_outstanding_wake_count, `
        elapsed_ms, stdout_log, stderr_log |
        Export-Csv -LiteralPath (Join-Path $evidenceRoot 'prefetch-thread-diagnostics.csv') `
            -NoTypeInformation -Encoding UTF8
    $summaryOutput = [PSCustomObject]@{
        status = 'PASS'
        processCount = $Rows.Count
        datasetHash = $manifestHash
        resultRows = $Rows
    }
    ConvertTo-Json -InputObject $summaryOutput -Depth 6 -Compress |
        Set-Content -LiteralPath (Join-Path $evidenceRoot 'summary.json') -Encoding UTF8
    Write-RunMetadata 'PASS' $null
    Compress-Archive -Path $evidenceRoot -DestinationPath $archivePath -Force
    Write-Host "prefetch thread diagnostics passed,processes=$completedProcesses,archive=$archivePath"
} catch {
    $failureMessage = $_.Exception.Message
    Write-Host "prefetch thread diagnostics failed: $failureMessage"
    try {
        if (Test-Path -LiteralPath $evidenceRoot -PathType Container) {
            if ($null -ne $appOutputRoot) {
                $appRuns = Join-Path $appOutputRoot 'runs'
                if (Test-Path -LiteralPath $appRuns -PathType Container) {
                    $appEvidenceRoot = Join-Path $evidenceRoot 'app-results'
                    New-Item -ItemType Directory -Path $appEvidenceRoot -Force | Out-Null
                    foreach ($appRun in @(Get-ChildItem -LiteralPath $appRuns -Directory)) {
                        $destination = Join-Path $appEvidenceRoot $appRun.Name
                        if (-not (Test-Path -LiteralPath $destination)) {
                            Copy-Item -LiteralPath $appRun.FullName -Destination $destination -Recurse
                        }
                    }
                }
                $environmentPath = Join-Path $appOutputRoot 'environment.json'
                if (Test-Path -LiteralPath $environmentPath -PathType Leaf) {
                    $appEvidenceRoot = Join-Path $evidenceRoot 'app-results'
                    New-Item -ItemType Directory -Path $appEvidenceRoot -Force | Out-Null
                    Copy-Item -LiteralPath $environmentPath -Destination $appEvidenceRoot -Force
                }
            }
            Write-RunMetadata 'FAILED' $failureMessage
            try {
                Compress-Archive -Path $evidenceRoot -DestinationPath $archivePath -Force
                Write-Host "failure evidence archive=$archivePath"
            } catch {
                Write-Host "failure evidence ZIP could not be created: $($_.Exception.Message)"
            }
        }
    } catch {
        Write-Host "failure metadata could not be written: $($_.Exception.Message)"
    }
    exit 1
}
