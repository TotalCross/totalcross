# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

function Get-Percentile {
    param([long[]]$Values, [int]$Percentile)
    if ($Values.Count -eq 0) { return [long]0 }
    [long[]]$ordered = $Values
    [Array]::Sort($ordered)
    $index = [Math]::Max(0, [Math]::Ceiling($ordered.Count * $Percentile / 100.0) - 1)
    return $ordered[[int]$index]
}

function Get-DistributionFields {
    param([long[]]$Values, [string]$Prefix)
    $distribution = [PSCustomObject]@{}
    foreach ($percentile in @(50, 95, 99, 100)) {
        $suffix = if ($percentile -eq 100) { 'MaxNs' } else { "P${percentile}Ns" }
        $distribution | Add-Member -NotePropertyName "$Prefix$suffix" -NotePropertyValue (Get-Percentile $Values $percentile)
    }
    return $distribution
}

function Get-ThresholdCounts {
    param([long[]]$Values)
    return [PSCustomObject]@{
        framesOver16_67Count=@($Values | Where-Object { $_ -gt 16670000 }).Count
        framesOver20Count=@($Values | Where-Object { $_ -gt 20000000 }).Count
        framesOver22_22Count=@($Values | Where-Object { $_ -gt 22220000 }).Count
        framesOver25Count=@($Values | Where-Object { $_ -gt 25000000 }).Count
        framesOver33_3Count=@($Values | Where-Object { $_ -gt 33300000 }).Count
        framesOver50Count=@($Values | Where-Object { $_ -gt 50000000 }).Count
    }
}

function Get-FrameMetrics {
    param([object[]]$Rows)
    $intervals = New-Object 'System.Collections.Generic.List[long]'
    $work = New-Object 'System.Collections.Generic.List[long]'
    $paint = New-Object 'System.Collections.Generic.List[long]'
    $screen = New-Object 'System.Collections.Generic.List[long]'
    $previousByPass = @{}
    foreach ($row in $Rows) {
        $elapsed = [long]::Parse([string]$row.elapsed_ns, [System.Globalization.CultureInfo]::InvariantCulture)
        if ($previousByPass.ContainsKey($row.pass)) {
            $interval = $elapsed - [long]$previousByPass[$row.pass]
            if ($interval -lt 0) { throw "Negative $($row.pass) frame interval" }
            $intervals.Add($interval)
        }
        $previousByPass[$row.pass] = $elapsed
        if ([long]::Parse([string]$row.measured, [System.Globalization.CultureInfo]::InvariantCulture) -ne 0) {
            $work.Add([long]::Parse([string]$row.work_time_ns, [System.Globalization.CultureInfo]::InvariantCulture))
            $paint.Add([long]::Parse([string]$row.paint_work_ns, [System.Globalization.CultureInfo]::InvariantCulture))
            $screen.Add([long]::Parse([string]$row.screen_update_ns, [System.Globalization.CultureInfo]::InvariantCulture))
        }
    }
    $frameDistribution = Get-DistributionFields ([long[]]$intervals.ToArray()) 'frameInterval'
    $workDistribution = Get-DistributionFields ([long[]]$work.ToArray()) 'activeWork'
    $paintDistribution = Get-DistributionFields ([long[]]$paint.ToArray()) 'paint'
    $screenDistribution = Get-DistributionFields ([long[]]$screen.ToArray()) 'screenUpdate'
    $thresholds = Get-ThresholdCounts ([long[]]$intervals.ToArray())
    return [PSCustomObject]@{
        frameCount=$Rows.Count; measuredFrameCount=$work.Count
        frameIntervalP50Ns=$frameDistribution.frameIntervalP50Ns
        frameIntervalP95Ns=$frameDistribution.frameIntervalP95Ns
        frameIntervalP99Ns=$frameDistribution.frameIntervalP99Ns
        frameIntervalMaxNs=$frameDistribution.frameIntervalMaxNs
        activeWorkP50Ns=$workDistribution.activeWorkP50Ns
        activeWorkP95Ns=$workDistribution.activeWorkP95Ns
        activeWorkP99Ns=$workDistribution.activeWorkP99Ns
        activeWorkMaxNs=$workDistribution.activeWorkMaxNs
        paintP50Ns=$paintDistribution.paintP50Ns; paintP95Ns=$paintDistribution.paintP95Ns
        paintP99Ns=$paintDistribution.paintP99Ns; paintMaxNs=$paintDistribution.paintMaxNs
        screenUpdateP50Ns=$screenDistribution.screenUpdateP50Ns
        screenUpdateP95Ns=$screenDistribution.screenUpdateP95Ns
        screenUpdateP99Ns=$screenDistribution.screenUpdateP99Ns
        screenUpdateMaxNs=$screenDistribution.screenUpdateMaxNs
        framesOver16_67Count=$thresholds.framesOver16_67Count
        framesOver20Count=$thresholds.framesOver20Count
        framesOver22_22Count=$thresholds.framesOver22_22Count
        framesOver25Count=$thresholds.framesOver25Count
        framesOver33_3Count=$thresholds.framesOver33_3Count
        framesOver50Count=$thresholds.framesOver50Count
    }
}

function Write-AggregateComparison {
    param([PSCustomObject[]]$Summaries, [string]$Path)
    $fields = @(
        'frameIntervalP50Ns','frameIntervalP95Ns','frameIntervalP99Ns','frameIntervalMaxNs',
        'activeWorkP50Ns','activeWorkP95Ns','activeWorkP99Ns','activeWorkMaxNs',
        'paintP50Ns','paintP95Ns','paintP99Ns','paintMaxNs',
        'screenUpdateP50Ns','screenUpdateP95Ns','screenUpdateP99Ns','screenUpdateMaxNs',
        'framesOver22_22Count','framesOver33_3Count','framesOver50Count'
    )
    $rows = New-Object 'System.Collections.Generic.List[object]'
    $passRows = New-Object 'System.Collections.Generic.List[object]'
    foreach ($summary in $Summaries) {
        foreach ($passSummary in $summary.passSummaries) { $passRows.Add($passSummary) }
    }
    foreach ($passName in @('cold','warm')) {
        foreach ($field in $fields) {
            $offRows = @($passRows | Where-Object { $_.pass -eq $passName -and $_.mode -eq 'off' } | Sort-Object sample)
            $onRows = @($passRows | Where-Object { $_.pass -eq $passName -and $_.mode -eq 'on' } | Sort-Object sample)
            if ($offRows.Count -ne 3 -or $onRows.Count -ne 3) { throw "Aggregate $passName comparison requires three runs per mode" }
            [long[]]$offValues = @($offRows | ForEach-Object { [long]($_.$field) })
            [long[]]$onValues = @($onRows | ForEach-Object { [long]($_.$field) })
            $offMedian = Get-Percentile $offValues 50
            $onMedian = Get-Percentile $onValues 50
            $delta = $onMedian - $offMedian
            $deltaPercent = if ($offMedian -eq 0) { $null } else { [Math]::Round(($delta * 100.0) / $offMedian, 4) }
            $rows.Add([PSCustomObject]@{
                pass=$passName; metric=$field
                offRun1=$offValues[0]; offRun2=$offValues[1]; offRun3=$offValues[2]
                offMedian=$offMedian; offMin=($offValues | Measure-Object -Minimum).Minimum
                offMax=($offValues | Measure-Object -Maximum).Maximum
                onRun1=$onValues[0]; onRun2=$onValues[1]; onRun3=$onValues[2]
                onMedian=$onMedian; onMin=($onValues | Measure-Object -Minimum).Minimum
                onMax=($onValues | Measure-Object -Maximum).Maximum
                onMinusOffMedian=$delta; onMinusOffMedianPercent=$deltaPercent
            })
        }
    }
    $rows | Export-Csv -LiteralPath $Path -NoTypeInformation -Encoding UTF8
}
