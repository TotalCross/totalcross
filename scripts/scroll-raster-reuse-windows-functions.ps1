# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

function Read-JsonFile {
    param([string]$Path)
    return (Get-Content -LiteralPath $Path -Raw -Encoding UTF8 | ConvertFrom-Json)
}

function Get-Sha256 {
    param([string]$Path)
    $stream = [System.IO.File]::OpenRead($Path)
    $sha = [System.Security.Cryptography.SHA256]::Create()
    try {
        return ([BitConverter]::ToString($sha.ComputeHash($stream))).Replace('-', '').ToLowerInvariant()
    } finally {
        $stream.Dispose()
        $sha.Dispose()
    }
}

function Add-FnvBytes {
    param([byte[]]$Bytes, [int]$Length)
    for ($index = 0; $index -lt $Length; $index++) {
        [UInt64]$mixed = ([Int64]$script:fnvLow -bxor [Int64]$Bytes[$index])
        [UInt64]$lowProduct = $mixed * [UInt64]435
        [UInt64]$carry = $lowProduct -shr 32
        [UInt64]$highProduct = ([UInt64]$script:fnvHigh * [UInt64]435) +
            ($mixed * [UInt64]256) + $carry
        $script:fnvLow = [UInt32]($lowProduct -band [UInt64]4294967295)
        $script:fnvHigh = [UInt32]($highProduct -band [UInt64]4294967295)
    }
}

function Get-CorpusDigest {
    param([string]$CorpusPath)
    $images = @(Get-ChildItem -LiteralPath $CorpusPath -Recurse -File |
        Where-Object { @('.jpg', '.jpeg') -contains $_.Extension.ToLowerInvariant() })
    if ($images.Count -ne 663) { throw "Corpus must contain 663 JPEG-named files; found $($images.Count)" }
    $paths = [string[]]@($images | ForEach-Object { $_.FullName })
    [Array]::Sort($paths, [System.StringComparer]::Ordinal)
    [Int64]$totalBytes = 0
    foreach ($image in $images) { $totalBytes += [Int64]$image.Length }
    [Int64]$processedBytes = 0
    $processedFiles = 0
    Write-Host ("scroll raster reuse corpus hash started files={0} bytes={1:N0}" -f $paths.Count, $totalBytes)
    $root = (Resolve-Path -LiteralPath $CorpusPath).Path.TrimEnd([char[]]@('\', '/'))
    $hexStyle = [System.Globalization.NumberStyles]::HexNumber
    $script:fnvLow = [UInt32]::Parse('84222325', $hexStyle)
    $script:fnvHigh = [UInt32]::Parse('cbf29ce4', $hexStyle)
    $formats = @{ jpeg = 0; png = 0 }
    $buffer = New-Object byte[] 65536
    $separator = [byte[]]@(0)
    foreach ($path in $paths) {
        $relative = $path.Substring($root.Length).TrimStart([char[]]@('\', '/')).Replace('\', '/')
        $relativeBytes = [System.Text.Encoding]::UTF8.GetBytes($relative)
        Add-FnvBytes $relativeBytes $relativeBytes.Length
        Add-FnvBytes $separator 1
        $stream = [System.IO.File]::OpenRead($path)
        try {
            $signature = New-Object byte[] 8
            $read = $stream.Read($signature, 0, 8)
            if ($read -ge 3 -and $signature[0] -eq 255 -and
                $signature[1] -eq 216 -and $signature[2] -eq 255) {
                $formats.jpeg++
            } elseif ($read -eq 8 -and $signature[0] -eq 137 -and
                $signature[1] -eq 80 -and $signature[2] -eq 78 -and
                $signature[3] -eq 71 -and $signature[4] -eq 13 -and
                $signature[5] -eq 10 -and $signature[6] -eq 26 -and
                $signature[7] -eq 10) {
                $formats.png++
            } else {
                throw "Unsupported image content: $path"
            }
            Add-FnvBytes $signature $read
            $processedBytes += $read
            while (($read = $stream.Read($buffer, 0, $buffer.Length)) -gt 0) {
                Add-FnvBytes $buffer $read
                $processedBytes += $read
            }
        } finally {
            $stream.Dispose()
        }
        $processedFiles++
        if (($processedFiles % 16) -eq 0 -or $processedFiles -eq $paths.Count) {
            Write-Host ("scroll raster reuse corpus hash progress files={0}/{1} bytes={2:N0}/{3:N0}" -f $processedFiles, $paths.Count, $processedBytes, $totalBytes)
        }
    }
    if ($formats.jpeg -ne 660 -or $formats.png -ne 3) {
        throw "Corpus magic counts must be 660 JPEG and 3 PNG; found $($formats.jpeg)/$($formats.png)"
    }
    Write-Host ("scroll raster reuse corpus hash passed files={0}/{1} jpeg={2} png={3}" -f $processedFiles, $paths.Count, $formats.jpeg, $formats.png)
    return [PSCustomObject]@{
        hash = $script:fnvHigh.ToString('x8') + $script:fnvLow.ToString('x8')
        total = $paths.Count
        jpeg = $formats.jpeg
        png = $formats.png
    }
}

function Convert-RecordLine {
    param([string]$Line)
    $values = @{}
    foreach ($part in $Line.Split(',')) {
        $separator = $part.IndexOf('=')
        if ($separator -ge 0) {
            $values[$part.Substring(0, $separator)] = $part.Substring($separator + 1)
        }
    }
    return $values
}

function Get-UniqueRecords {
    param([string[]]$Paths, [string]$Prefix)
    $records = @{}
    foreach ($path in $Paths) {
        if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { continue }
        foreach ($line in (Get-Content -LiteralPath $path)) {
            if ($line.StartsWith($Prefix, [System.StringComparison]::Ordinal)) {
                $records[$line] = $true
            }
        }
    }
    return @($records.Keys)
}

function Get-CompletionRecord {
    param([string[]]$Paths)
    $records = @(Get-UniqueRecords $Paths 'fixture=ImageScrollRealWorkloadBenchmarkApp,record=summary,')
    if ($records.Count -ne 1) { throw "Expected one benchmark completion record; found $($records.Count)" }
    $values = Convert-RecordLine $records[0]
    if ($values['overallPass'] -ne 'true') { throw 'Benchmark completion record did not pass' }
    return $values
}

function Get-ScrollReuseRecords {
    param([string[]]$Paths)
    $lines = @(Get-UniqueRecords $Paths 'fixture=ImageScrollRealWorkloadBenchmarkApp,record=scroll-raster-reuse,')
    if ($lines.Count -ne 2) { throw "Expected cold and warm scroll records; found $($lines.Count)" }
    $records = @($lines | ForEach-Object { Convert-RecordLine $_ } | Sort-Object pass)
    if (($records.pass -join ',') -ne 'cold,warm') { throw 'Scroll pass records are not cold,warm' }
    return $records
}

function Get-Long {
    param($Object, [string]$Name)
    $property = $Object.PSObject.Properties[$Name]
    if ($null -eq $property -or [string]::IsNullOrWhiteSpace([string]$property.Value)) {
        throw "Missing numeric field $Name"
    }
    return [long]::Parse([string]$property.Value, [System.Globalization.CultureInfo]::InvariantCulture)
}

function Get-RecordLong {
    param([hashtable]$Record, [string]$Name)
    return [long]::Parse([string]$Record[$Name], [System.Globalization.CultureInfo]::InvariantCulture)
}

function Get-ThresholdCounts {
    param([long[]]$Values)
    return [PSCustomObject]@{
        framesOver22_22Count=@($Values | Where-Object { $_ -gt 22220000 }).Count
        framesOver33_3Count=@($Values | Where-Object { $_ -gt 33300000 }).Count
        framesOver50Count=@($Values | Where-Object { $_ -gt 50000000 }).Count
    }
}

function Get-ProcessSummary {
    param([string]$Mode, [int]$Sample, [string]$OutputDirectory,
        [hashtable]$Completion, [hashtable[]]$PassRecords,
        [string]$PixelFormat, [int]$ExitCode)
    $frameFiles = @(Get-ChildItem -LiteralPath $OutputDirectory -Filter 'scroll_raster_reuse_frames.csv' -Recurse -File)
    $waypointFiles = @(Get-ChildItem -LiteralPath $OutputDirectory -Filter 'scroll_raster_reuse_waypoints.csv' -Recurse -File)
    $environmentPath = Join-Path $OutputDirectory 'environment.json'
    if ($frameFiles.Count -ne 1 -or $waypointFiles.Count -ne 1 -or
        -not (Test-Path -LiteralPath $environmentPath -PathType Leaf)) {
        throw "Run outputs are incomplete under $OutputDirectory"
    }
    $environment = Read-JsonFile $environmentPath
    $frameRows = @(Import-Csv -LiteralPath $frameFiles[0].FullName)
    if ($frameRows.Count -lt 2) { throw "Not enough frame rows under $OutputDirectory" }
    $reuse = @{}
    foreach ($record in $PassRecords) {
        foreach ($name in @('attempts','hits','fallbacks','viewport_pixels','reused_pixels',
            'dirty_pixels','moved_bytes','screen_update_ns','post_move_recoveries',
            'scroll_jpeg_decode_count','scroll_targeted_jpeg_decodes','scroll_full_jpeg_decodes',
            'scroll_image_materializations','scroll_native_geometry_materializations','frame_count')) {
            $key = $name
            if (-not $reuse.ContainsKey($key)) { $reuse[$key] = [long]0 }
            $reuse[$key] += [long]::Parse([string]$record[$name], [System.Globalization.CultureInfo]::InvariantCulture)
        }
        if ($record['rendering_reuse'] -ne $Mode) { throw "Process emitted the wrong reuse mode: $($record['rendering_reuse'])" }
        $expectedDisplacement = Get-RecordLong $record 'expected_final_displacement'
        $actualDisplacement = Get-RecordLong $record 'actual_final_displacement'
        if ($expectedDisplacement -le 0 -or $actualDisplacement -ne $expectedDisplacement) {
            throw "Process emitted invalid final scroll displacement: expected=$expectedDisplacement, actual=$actualDisplacement"
        }
    }
    $attempts = [long]$reuse['attempts']
    $hitRate = 0.0
    if ($attempts -ne 0) { $hitRate = [double]$reuse['hits'] / $attempts }
    $expectedFinalDisplacement = Get-RecordLong $PassRecords[0] 'expected_final_displacement'
    $actualFinalDisplacement = Get-RecordLong $PassRecords[0] 'actual_final_displacement'
    $passSummaries = New-Object 'System.Collections.Generic.List[object]'
    foreach ($passName in @('cold','warm')) {
        $passRows = @($frameRows | Where-Object { $_.pass -eq $passName })
        if ($passRows.Count -lt 2) { throw "Not enough $passName frame rows under $OutputDirectory" }
        $passIntervals = New-Object 'System.Collections.Generic.List[long]'
        $passWork = New-Object 'System.Collections.Generic.List[long]'
        $passPaint = New-Object 'System.Collections.Generic.List[long]'
        $passScreen = New-Object 'System.Collections.Generic.List[long]'
        $previousElapsed = $null
        foreach ($row in $passRows) {
            $elapsed = Get-Long $row 'elapsed_ns'
            if ($null -ne $previousElapsed) {
                $interval = $elapsed - [long]$previousElapsed
                if ($interval -lt 0) { throw "Negative $passName frame interval under $OutputDirectory" }
                $passIntervals.Add($interval)
            }
            $previousElapsed = $elapsed
            if ((Get-Long $row 'measured') -ne 0) {
                $passWork.Add((Get-Long $row 'work_time_ns'))
                $passPaint.Add((Get-Long $row 'paint_work_ns'))
                $passScreen.Add((Get-Long $row 'screen_update_ns'))
            }
        }
        $passFrameDistribution = Get-DistributionFields ([long[]]$passIntervals.ToArray()) 'frameInterval'
        $passWorkDistribution = Get-DistributionFields ([long[]]$passWork.ToArray()) 'activeWork'
        $passPaintDistribution = Get-DistributionFields ([long[]]$passPaint.ToArray()) 'paint'
        $passScreenDistribution = Get-DistributionFields ([long[]]$passScreen.ToArray()) 'screenUpdate'
        $passThresholds = Get-ThresholdCounts ([long[]]$passIntervals.ToArray())
        $passSummaries.Add([PSCustomObject]@{
            mode=$Mode; sample=$Sample; pass=$passName
            frameCount=$passRows.Count; measuredFrameCount=$passWork.Count
            frameIntervalP50Ns=$passFrameDistribution.frameIntervalP50Ns
            frameIntervalP95Ns=$passFrameDistribution.frameIntervalP95Ns
            frameIntervalP99Ns=$passFrameDistribution.frameIntervalP99Ns
            frameIntervalMaxNs=$passFrameDistribution.frameIntervalMaxNs
            activeWorkP50Ns=$passWorkDistribution.activeWorkP50Ns
            activeWorkP95Ns=$passWorkDistribution.activeWorkP95Ns
            activeWorkP99Ns=$passWorkDistribution.activeWorkP99Ns
            activeWorkMaxNs=$passWorkDistribution.activeWorkMaxNs
            paintP50Ns=$passPaintDistribution.paintP50Ns
            paintP95Ns=$passPaintDistribution.paintP95Ns
            paintP99Ns=$passPaintDistribution.paintP99Ns
            paintMaxNs=$passPaintDistribution.paintMaxNs
            screenUpdateP50Ns=$passScreenDistribution.screenUpdateP50Ns
            screenUpdateP95Ns=$passScreenDistribution.screenUpdateP95Ns
            screenUpdateP99Ns=$passScreenDistribution.screenUpdateP99Ns
            screenUpdateMaxNs=$passScreenDistribution.screenUpdateMaxNs
            framesOver22_22Count=$passThresholds.framesOver22_22Count
            framesOver33_3Count=$passThresholds.framesOver33_3Count
            framesOver50Count=$passThresholds.framesOver50Count
        })
    }
    return [PSCustomObject]@{
        mode=$Mode; sample=$Sample; exitCode=$ExitCode; status='PASS'
        imageCount=[int]$Completion['image_count']; prefetchRequestCount=[int]$Completion['prefetch_request_count']
        prefetchReadyCount=[int]$Completion['prefetch_ready_count']; prefetchFailedCount=[int]$Completion['prefetch_failed_count']
        prefetchNotPrefetchableCount=[int]$Completion['prefetch_not_prefetchable_count']
        frameCount=$frameRows.Count; measuredFrameCount=[long]$reuse['frame_count']
        attempts=$attempts; hits=[long]$reuse['hits']; fallbacks=[long]$reuse['fallbacks']
        hitRate=$hitRate
        reusedPixels=[long]$reuse['reused_pixels']; dirtyPixels=[long]$reuse['dirty_pixels']
        movedBytes=[long]$reuse['moved_bytes']; postMoveRecoveries=[long]$reuse['post_move_recoveries']
        scrollJpegDecodes=[long]$reuse['scroll_jpeg_decode_count']
        scrollTargetedJpegDecodes=[long]$reuse['scroll_targeted_jpeg_decodes']
        scrollFullJpegDecodes=[long]$reuse['scroll_full_jpeg_decodes']
        scrollImageMaterializations=[long]$reuse['scroll_image_materializations']
        scrollNativeGeometryMaterializations=[long]$reuse['scroll_native_geometry_materializations']
        expectedFinalDisplacement=$expectedFinalDisplacement
        actualFinalDisplacement=$actualFinalDisplacement
        drawableWidth=$environment.sdlDrawableWidth; drawableHeight=$environment.sdlDrawableHeight
        logicalWidth=$environment.effectiveLogicalWidth; logicalHeight=$environment.effectiveLogicalHeight
        surfaceScaleX=$environment.surfaceScaleX; surfaceScaleY=$environment.surfaceScaleY
        pixelFormat=$PixelFormat; rendererBackend=$environment.rendererBackend
        refreshRate=$environment.refreshRate
        waypointsPath=$waypointFiles[0].FullName
        passSummaries=$passSummaries.ToArray()
        environment=$environment; completion=$Completion
    }
}

function Assert-PreflightRun {
    param([PSCustomObject]$Summary, [hashtable]$Completion, [hashtable[]]$PassRecords,
        [string]$Mode)
    if ($Summary.mode -ne $Mode -or $Summary.imageCount -ne 663 -or
        $Summary.prefetchRequestCount -ne 663 -or $Summary.prefetchReadyCount -ne 663 -or
        $Summary.prefetchFailedCount -ne 0 -or $Summary.prefetchNotPrefetchableCount -ne 0) {
        throw "Preflight $Mode failed corpus or prefetch counts"
    }
    if ($Completion['profile'] -ne 'scroll-raster-reuse-windows' -or
        $Completion['passes'] -ne '2' -or $Completion['accounting'] -ne 'on') {
        throw "Preflight $Mode used the wrong profile or accounting mode"
    }
    if ($Summary.expectedFinalDisplacement -le 0 -or
        $Summary.actualFinalDisplacement -ne $Summary.expectedFinalDisplacement) {
        throw "Preflight $Mode final displacement differs from its expected displacement"
    }
    foreach ($record in $PassRecords) {
        foreach ($field in @('scroll_targeted_jpeg_decodes','scroll_full_jpeg_decodes',
            'scroll_jpeg_decode_count','scroll_image_materializations',
            'scroll_native_geometry_materializations','post_move_recoveries')) {
            if ([long]::Parse([string]$record[$field], [System.Globalization.CultureInfo]::InvariantCulture) -ne 0) {
                throw "Preflight $Mode reported $field=$($record[$field])"
            }
        }
    }
}

function Assert-PreflightMatch {
    param([string]$OffWaypointsPath, [string]$OnWaypointsPath)
    $off = @(Import-Csv -LiteralPath $OffWaypointsPath)
    $on = @(Import-Csv -LiteralPath $OnWaypointsPath)
    if ($off.Count -ne 10 -or $on.Count -ne 10) { throw 'Preflight must record ten OFF and ten ON waypoints' }
    $offByKey = @{}
    foreach ($row in $off) { $offByKey["$($row.pass)|$($row.waypoint_index)"] = $row }
    $matches = New-Object 'System.Collections.Generic.List[object]'
    foreach ($row in $on) {
        $key = "$($row.pass)|$($row.waypoint_index)"
        if (-not $offByKey.ContainsKey($key)) { throw "ON preflight has unmatched waypoint $key" }
        $prior = $offByKey[$key]
        foreach ($field in @('target_scroll','actual_scroll','hash','top_hash','bottom_hash')) {
            if ([string]$prior.$field -cne [string]$row.$field) { throw "OFF/ON waypoint mismatch at $key ($field)" }
        }
        if ([string]$row.target_scroll -cne [string]$row.actual_scroll) { throw "Waypoint $key did not reach its target" }
        $matches.Add([PSCustomObject]@{pass=$row.pass;waypointIndex=[int]$row.waypoint_index;hash=$row.hash})
    }
    $displacements = @{}
    foreach ($pass in @('cold','warm')) {
        $passRows = @($off | Where-Object { $_.pass -eq $pass } | Sort-Object { [int]$_.waypoint_index })
        if ($passRows.Count -ne 5) { throw "Preflight $pass pass does not contain five waypoints" }
        $displacement = [int]$passRows[-1].actual_scroll - [int]$passRows[0].actual_scroll
        if ($displacement -le 0) { throw "Preflight $pass pass did not scroll forward" }
        $displacements[$pass] = $displacement
    }
    if ($displacements.cold -ne $displacements.warm) { throw 'Cold/warm final scroll displacement differs' }
    return [PSCustomObject]@{matchedWaypointCount=$matches.Count; finalDisplacement=$displacements.cold; matches=$matches.ToArray()}
}
