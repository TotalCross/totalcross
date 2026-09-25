# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

function Read-JsonFile {
    param([string]$Path)
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        throw "Required JSON artifact is missing: $Path"
    }
    return (Get-Content -LiteralPath $Path -Raw | ConvertFrom-Json -ErrorAction Stop)
}

function Read-Count {
    param($Object, [string]$Name, [string]$Description)
    $raw = $null
    if ($Object -is [System.Collections.IDictionary]) {
        if (-not $Object.Contains($Name)) { throw "$Description is missing $Name" }
        $raw = $Object[$Name]
    } else {
        $property = $Object.PSObject.Properties[$Name]
        if ($null -eq $property) { throw "$Description is missing $Name" }
        $raw = $property.Value
    }
    $number = 0L
    if (-not [long]::TryParse([string]$raw, [ref]$number) -or $number -lt 0) {
        throw "$Description $Name is not a nonnegative integer"
    }
    return $number
}

function Get-OptionalValue {
    param($Object, [string]$Name)
    $property = $Object.PSObject.Properties[$Name]
    if ($null -eq $property) { return $null }
    return $property.Value
}

function Get-Sha256 {
    param([string]$Path)
    $stream = [System.IO.File]::OpenRead($Path)
    $sha = [System.Security.Cryptography.SHA256]::Create()
    try {
        $bytes = $sha.ComputeHash($stream)
        return ([BitConverter]::ToString($bytes)).Replace('-', '').ToLowerInvariant()
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

function Get-DatasetDigest {
    param([string]$CorpusPath)
    $images = @(Get-ChildItem -LiteralPath $CorpusPath -Recurse -File |
        Where-Object { @('.jpg', '.jpeg') -contains $_.Extension.ToLowerInvariant() })
    if ($images.Count -ne 663) { throw "Corpus must contain 663 named images; found $($images.Count)" }
    $paths = [string[]]@($images | ForEach-Object { $_.FullName })
    [Array]::Sort($paths, [System.StringComparer]::Ordinal)
    $hexStyle = [System.Globalization.NumberStyles]::HexNumber
    $script:fnvLow = [UInt32]::Parse('84222325', $hexStyle)
    $script:fnvHigh = [UInt32]::Parse('cbf29ce4', $hexStyle)
    $formats = @{ jpeg = 0; png = 0 }
    $buffer = New-Object byte[] 65536
    $separator = [byte[]]@(0)
    foreach ($path in $paths) {
        $relative = $path.Substring($CorpusPath.Length).TrimStart([char[]]@('\', '/')).Replace('\', '/')
        Add-FnvBytes ([System.Text.Encoding]::UTF8.GetBytes($relative)) ([System.Text.Encoding]::UTF8.GetByteCount($relative))
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
            while (($read = $stream.Read($buffer, 0, $buffer.Length)) -gt 0) {
                Add-FnvBytes $buffer $read
            }
        } finally {
            $stream.Dispose()
        }
    }
    if ($formats.jpeg -ne 660 -or $formats.png -ne 3) {
        throw "Corpus magic counts must be 660 JPEG and 3 PNG; found $($formats.jpeg)/$($formats.png)"
    }
    $digest = $script:fnvHigh.ToString('x8') + $script:fnvLow.ToString('x8')
    return [PSCustomObject]@{ hash = $digest; jpeg = $formats.jpeg; png = $formats.png; total = $paths.Count }
}

function Get-CompletionRecord {
    param([string[]]$Paths)
    $prefix = 'fixture=ImageScrollRealWorkloadBenchmarkApp,record=summary,'
    $lines = @()
    foreach ($path in $Paths) {
        if (Test-Path -LiteralPath $path -PathType Leaf) {
            $lines += @(Get-Content -LiteralPath $path)
        }
    }
    $records = @($lines | Where-Object { $_.StartsWith($prefix) })
    if ($records.Count -ne 1) { throw "Expected one benchmark completion record; found $($records.Count)" }
    $values = @{}
    foreach ($part in $records[0].Split(',')) {
        $separator = $part.IndexOf('=')
        if ($separator -ge 0) {
            $values[$part.Substring(0, $separator)] = $part.Substring($separator + 1)
        }
    }
    if ($values['overallPass'] -ne 'true') { throw 'Benchmark completion record did not pass' }
    return $values
}

function Assert-Summary {
    param($Summary, $Config, [string]$Accounting)
    if ($Summary.fixture -ne 'ImageScrollRealWorkloadBenchmarkApp' -or
        $Summary.status -ne 'PASS' -or $Summary.prefetch -ne 'on' -or
        $Summary.accounting -ne $Accounting -or $Summary.prefetchThreadMode -ne 'worker-semaphore' -or
        $Summary.prefetchWorkerSleepMs -ne 0 -or $Summary.requestedMask -ne 6 -or
        $Summary.effectiveMask -ne 6) { throw "Summary configuration mismatch for $($Config.Name)" }
    if ((Read-Count $Summary 'imageCount' $Config.Name) -ne 663 -or
        (Read-Count $Summary 'frameCount' $Config.Name) -le 1 -or
        (Read-Count $Summary 'durationNs' $Config.Name) -le 0) {
        throw "Summary image or timing counts are invalid for $($Config.Name)"
    }
    foreach ($field in @(
        'frameTimeP50Ns','frameTimeP95Ns','frameTimeP99Ns','frameTimeMaxNs',
        'workTimeP50Ns','workTimeP95Ns','workTimeP99Ns','workTimeMaxNs',
        'paintTimeP50Ns','paintTimeP95Ns','paintTimeP99Ns','paintTimeMaxNs',
        'framesOver16_67Count','framesOver20Count','framesOver25Count',
        'framesOver33_3Count','framesOver50Count','framesOver100Count'
    )) { [void](Read-Count $Summary $field $Config.Name) }
    if ($Config.Stage -eq 1) {
        $interval = if ($Config.Profile -eq 'synthetic-current-16ms') { 16000000 } else { 16666667 }
        if ($Summary.syntheticPacingProfile -ne $Config.Profile -or
            (Read-Count $Summary 'syntheticPacingIntervalNs' $Config.Name) -ne $interval -or
            (Read-Count $Summary 'sleepRequestCount' $Config.Name) -le 0) {
            throw "Synthetic pacing summary mismatch for $($Config.Name)"
        }
        return
    }
    $fps = if ($Config.Fps -eq 0) { 0 } else { $Config.Fps }
    if ($Summary.driver -ne $Config.Driver -or $Summary.timerFps -ne $fps -or
        $Summary.clock -ne $Config.Clock -or
        $Summary.timerDeadlinePolicy -ne $Config.DeadlinePolicy -or
        $Summary.eventLoopPolicy -ne $Config.EventPolicy -or
        $Summary.yieldPolicy -ne $Config.YieldPolicy -or
        $Summary.timerDeadlineMode -ne $Config.Deadline -or
        $Summary.eventLoopMode -ne $Config.EventLoop -or
        $Summary.threadYieldMode -ne $Config.Yield -or
        (Read-Count $Summary 'expectedCallbackIntervalNs' $Config.Name) -ne $Config.Interval -or
        (Read-Count $Summary 'callbackCount' $Config.Name) -le 1) {
        throw "Flick or scheduling summary mismatch for $($Config.Name)"
    }
    foreach ($field in @(
        'callbackDeltaP50Ns','callbackDeltaP95Ns','callbackDeltaP99Ns','callbackDeltaMaxNs',
        'callbackAbsoluteLatenessP50Ns','callbackAbsoluteLatenessP95Ns',
        'callbackAbsoluteLatenessP99Ns','callbackAbsoluteLatenessMaxNs',
        'callbackDeltaErrorP50Ns','callbackDeltaErrorP95Ns',
        'callbackDeltaErrorP99Ns','callbackDeltaErrorMaxNs'
    )) { [void](Read-Count $Summary $field $Config.Name) }
}

function Assert-Preflight {
    param($Summary, $Completion, [string]$RunDirectory)
    foreach ($pair in @(
        @{Name='prefetchRequestCount'; Expected=663},
        @{Name='prefetchReadyCount'; Expected=663},
        @{Name='prefetchFailedCount'; Expected=0},
        @{Name='prefetchNotPrefetchableCount'; Expected=0}
    )) {
        if ((Read-Count $Summary $pair.Name $RunDirectory) -ne $pair.Expected -or
            (Read-Count $Completion ($pair.Name -creplace '([A-Z])','_$1').ToLowerInvariant() $RunDirectory) -ne $pair.Expected) {
            throw "Preflight $($pair.Name) differs from $($pair.Expected) in $RunDirectory"
        }
    }
    $framesPath = Join-Path $RunDirectory 'frames.csv'
    if (-not (Test-Path -LiteralPath $framesPath -PathType Leaf)) { throw "Preflight frames are missing: $framesPath" }
    $frames = @(Import-Csv -LiteralPath $framesPath)
    if ($frames.Count -eq 0) { throw "Preflight frame CSV is empty: $framesPath" }
    foreach ($field in @('scroll_jpeg_decode_count','scroll_image_materializations','scroll_native_geometry_materializations')) {
        $total = 0L
        foreach ($frame in $frames) { $total += Read-Count $frame $field $framesPath }
        if ($total -ne 0) { throw "Preflight scroll-time $field was $total" }
    }
}


function New-ResultRow {
    param($Summary, $Config, [int]$Stage, [int]$Sample, [long]$ProcessWallNs)
    $row = [ordered]@{
        stage=$Stage; configuration=$Config.Name; sample=$Sample
        sourceCommit=$script:sourceCommit; runtimeIdentity=$script:runtimeSha256
        frameCount=$Summary.frameCount; callbackCount=(Get-OptionalValue $Summary 'callbackCount')
        frameIntervalP50Ns=$Summary.frameTimeP50Ns; frameIntervalP95Ns=$Summary.frameTimeP95Ns
        frameIntervalP99Ns=$Summary.frameTimeP99Ns; frameIntervalMaxNs=$Summary.frameTimeMaxNs
        activeWorkP50Ns=$Summary.workTimeP50Ns; activeWorkP95Ns=$Summary.workTimeP95Ns
        activeWorkP99Ns=$Summary.workTimeP99Ns; activeWorkMaxNs=$Summary.workTimeMaxNs
        paintP50Ns=$Summary.paintTimeP50Ns; paintP95Ns=$Summary.paintTimeP95Ns
        paintP99Ns=$Summary.paintTimeP99Ns; paintMaxNs=$Summary.paintTimeMaxNs
        framesOver16_67Count=$Summary.framesOver16_67Count; framesOver20Count=$Summary.framesOver20Count
        framesOver25Count=$Summary.framesOver25Count; framesOver33_3Count=$Summary.framesOver33_3Count
        framesOver50Count=$Summary.framesOver50Count; framesOver100Count=$Summary.framesOver100Count
        callbackDeltaP50Ns=(Get-OptionalValue $Summary 'callbackDeltaP50Ns')
        callbackDeltaP95Ns=(Get-OptionalValue $Summary 'callbackDeltaP95Ns')
        callbackDeltaP99Ns=(Get-OptionalValue $Summary 'callbackDeltaP99Ns')
        callbackDeltaMaxNs=(Get-OptionalValue $Summary 'callbackDeltaMaxNs')
        callbackAbsoluteLatenessP50Ns=(Get-OptionalValue $Summary 'callbackAbsoluteLatenessP50Ns')
        callbackAbsoluteLatenessP95Ns=(Get-OptionalValue $Summary 'callbackAbsoluteLatenessP95Ns')
        callbackAbsoluteLatenessP99Ns=(Get-OptionalValue $Summary 'callbackAbsoluteLatenessP99Ns')
        callbackAbsoluteLatenessMaxNs=(Get-OptionalValue $Summary 'callbackAbsoluteLatenessMaxNs')
        callbackDeltaErrorP50Ns=(Get-OptionalValue $Summary 'callbackDeltaErrorP50Ns')
        callbackDeltaErrorP95Ns=(Get-OptionalValue $Summary 'callbackDeltaErrorP95Ns')
        callbackDeltaErrorP99Ns=(Get-OptionalValue $Summary 'callbackDeltaErrorP99Ns')
        callbackDeltaErrorMaxNs=(Get-OptionalValue $Summary 'callbackDeltaErrorMaxNs')
        measuredWallDurationNs=$Summary.durationNs; processWallNs=$ProcessWallNs
        driver=$Config.Driver; timerFps=$(if ($Config.Fps -eq 0) { $null } else { $Config.Fps })
        clock=$Config.Clock; timerDeadlinePolicy=$Config.DeadlinePolicy
        eventLoopPolicy=$Config.EventPolicy; yieldPolicy=$Config.YieldPolicy
        timerDeadlineMode=$Config.Deadline; eventLoopMode=$Config.EventLoop
        threadYieldMode=$Config.Yield
        prefetchThreadMode=(Get-OptionalValue $Summary 'prefetchThreadMode')
        prefetchWorkerSleepMs=(Get-OptionalValue $Summary 'prefetchWorkerSleepMs')
        expectedCallbackIntervalNs=$(if ($Config.Interval -eq 0) { $null } else { $Config.Interval })
        syntheticPacingProfile=(Get-OptionalValue $Summary 'syntheticPacingProfile')
        syntheticPacingIntervalNs=(Get-OptionalValue $Summary 'syntheticPacingIntervalNs')
        sleepRequestCount=(Get-OptionalValue $Summary 'sleepRequestCount')
        totalRequestedSleepNs=(Get-OptionalValue $Summary 'totalRequestedSleepNs')
        totalActualSleepNs=(Get-OptionalValue $Summary 'totalActualSleepNs')
        sleepOvershootP50Ns=(Get-OptionalValue $Summary 'sleepOvershootP50Ns')
        sleepOvershootP95Ns=(Get-OptionalValue $Summary 'sleepOvershootP95Ns')
        sleepOvershootP99Ns=(Get-OptionalValue $Summary 'sleepOvershootP99Ns')
        sleepOvershootMaxNs=(Get-OptionalValue $Summary 'sleepOvershootMaxNs')
        deadlineErrorP50Ns=(Get-OptionalValue $Summary 'deadlineErrorP50Ns')
        deadlineErrorP95Ns=(Get-OptionalValue $Summary 'deadlineErrorP95Ns')
        deadlineErrorP99Ns=(Get-OptionalValue $Summary 'deadlineErrorP99Ns')
        deadlineErrorMaxNs=(Get-OptionalValue $Summary 'deadlineErrorMaxNs')
    }
    return [PSCustomObject]$row
}

function Get-ConfigurationArguments {
    param($Config)
    if ($Config.Driver -eq 'synthetic') { return "--synthetic-pacing=$($Config.Profile)" }
    $arguments = @("--flick-driver=$($Config.Driver)", "--flick-clock=$($Config.Clock)")
    if ($Config.Fps -gt 0) { $arguments += "--flick-fps=$($Config.Fps)" }
    if ($Config.EventLoop -ne 'poll' -or $Config.Yield -ne 'legacy') {
        $arguments += "--pacing=$($Config.Deadline),$($Config.EventLoop),$($Config.Yield)"
    } elseif ($Config.Deadline -ne 'relative') {
        $arguments += "--deadline=$($Config.Deadline)"
    }
    return $arguments
}
