<#
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
#>

[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$workspace = $env:GITHUB_WORKSPACE
$resultRoot = Join-Path $workspace 'windows-crash-results'
$pythonHarness = Join-Path $workspace 'scripts/windows-native-crash-diagnostics.py'
$nativeRunner = Join-Path $workspace 'scripts/windows-native-crash-runner.ps1'
$ownershipTool = Join-Path $workspace 'performance-lab/tools/image_ownership_smoke.py'
$ownershipLab = Join-Path $workspace 'performance-lab'
$fixedBuildRoot = Join-Path $workspace 'artifacts/fixed-win32-runtime'
$ownershipResult = $null
$startupResults = [System.Collections.Generic.List[object]]::new()

function Write-JsonFile([string]$Path, $Value) {
    ConvertTo-Json -InputObject $Value -Depth 20 |
        Set-Content -LiteralPath $Path -Encoding utf8NoBOM
}

function Write-Incomplete([string]$Path, [string]$Revision,
                          [string]$Phase, [string]$Message) {
    $value = [ordered]@{
        status = 'INCOMPLETE'; revision = $Revision; phase = $Phase; error = $Message
    }
    Write-JsonFile $Path $value
    return $value
}

function Invoke-PythonLogged([string[]]$Arguments, [string]$LogPath) {
    & python @Arguments *> $LogPath
    return $LASTEXITCODE
}

function Get-Sha256([string]$Path) {
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
}

function Invoke-OwnershipSmoke($FixedStartupManifest, [string]$SdkRoot,
                               [string]$SdkArchive, [string]$SdkProvenance) {
    $output = Join-Path $resultRoot 'fixed-win32/ownership'
    New-Item -ItemType Directory -Force -Path $output | Out-Null
    $resultPath = Join-Path $output 'image-ownership-result.json'
    try {
        if (-not (Test-Path -LiteralPath $ownershipTool)) {
            return (Write-Incomplete $resultPath 'fixed-win32' 'ownership-prepare' `
                'Pinned performance-lab ownership smoke checkout is missing.')
        }
        $labCommit = (& git -C $ownershipLab rev-parse HEAD).Trim()
        if ($labCommit -ne $env:PERFORMANCE_LAB_COMMIT) {
            return (Write-Incomplete $resultPath 'fixed-win32' 'ownership-prepare' `
                "Performance-lab checkout resolved to unexpected commit $labCommit")
        }
        $arguments = @(
            $ownershipTool, 'prepare', '--target', 'windows',
            '--runtime-home', $SdkRoot,
            '--platform-artifact', $fixedBuildRoot,
            '--sdk-provenance', $SdkProvenance,
            '--platform-provenance', (Join-Path $fixedBuildRoot 'build-provenance.json'),
            '--sdk-archive', $SdkArchive,
            '--source-commit', '94af2b8b653cad509994b5ccf3dbb60ac28ef920',
            '--source-tree', '539f5b424b6f4593c7d96074d69ff077a38ed361',
            '--lab-root', $ownershipLab, '--lab-commit', $env:PERFORMANCE_LAB_COMMIT,
            '--output', $output, '--diagnostic-mode', 'fixed',
            '--windows-architecture', 'I386'
        )
        $prepareCode = Invoke-PythonLogged $arguments (Join-Path $output 'prepare.log')
        if ($prepareCode -ne 0) {
            return (Write-Incomplete $resultPath 'fixed-win32' 'ownership-prepare' `
                "Ownership smoke preparation failed with exit code $prepareCode.")
        }

        $smokeProvenance = Get-Content -LiteralPath `
            (Join-Path $output 'image-ownership-provenance.json') -Raw | ConvertFrom-Json
        $smokeRuntime = $smokeProvenance.windowsRuntimeArtifact
        $startupTcvm = $FixedStartupManifest.tcvm.sha256
        $startupLauncher = $FixedStartupManifest.launcher.sha256
        $sameRuntime = ($smokeRuntime.vmSha256 -eq $startupTcvm) -and
            ($smokeRuntime.launcherSha256 -eq $startupLauncher)
        if (-not $sameRuntime) {
            return (Write-Incomplete $resultPath 'fixed-win32' 'ownership-runtime-match' `
                'Ownership smoke runtime hashes do not match the fixed startup runtime.')
        }

        $runArguments = @(
            $ownershipTool, 'run-host', '--target', 'windows', '--output', $output,
            '--diagnostic-mode', 'fixed', '--windows-architecture', 'I386',
            '--windows-crash-runner', $nativeRunner,
            '--pdb-directory', $fixedBuildRoot
        )
        if (-not [string]::IsNullOrWhiteSpace($env:PROCDUMP_PATH)) {
            $runArguments += @('--procdump', $env:PROCDUMP_PATH)
        }
        if (-not [string]::IsNullOrWhiteSpace($env:DEBUGGER_PATH)) {
            $runArguments += @('--debugger', $env:DEBUGGER_PATH)
        }
        $runCode = Invoke-PythonLogged $runArguments (Join-Path $output 'run-host.log')
        if (-not (Test-Path -LiteralPath $resultPath)) {
            return (Write-Incomplete $resultPath 'fixed-win32' 'ownership-run' `
                "Ownership smoke emitted no machine-readable result (exit code $runCode).")
        }
        $result = Get-Content -LiteralPath $resultPath -Raw | ConvertFrom-Json
        $result | Add-Member -NotePropertyName sameAsStartupRuntime `
            -NotePropertyValue ([ordered]@{
                launcherSha256 = $sameRuntime ? $startupLauncher : $smokeRuntime.launcherSha256
                tcvmSha256 = $sameRuntime ? $startupTcvm : $smokeRuntime.vmSha256
                matches = $sameRuntime
            }) -Force
        if ($runCode -ne 0 -and $result.status -eq 'PASS') {
            $result.status = 'FAIL'
            $result.error = "Ownership smoke command exited with code $runCode."
        }
        Write-JsonFile $resultPath $result
        return $result
    } catch {
        return (Write-Incomplete $resultPath 'fixed-win32' 'ownership-run' `
            $_.Exception.Message)
    }
}

function Invoke-StartupProbe($Revision) {
    $name = $Revision.name
    $output = Join-Path $resultRoot "$name/minimal"
    $resultPath = Join-Path $output 'diagnostic-result.json'
    New-Item -ItemType Directory -Force -Path $output | Out-Null
    if ($name -eq 'fixed-win32') {
        $buildStatusPath = Join-Path $fixedBuildRoot 'build-status.json'
        if (-not (Test-Path -LiteralPath $buildStatusPath)) {
            return (Write-Incomplete $resultPath $name 'build' 'Fixed Win32 build emitted no status.')
        }
        $buildStatus = Get-Content -LiteralPath $buildStatusPath -Raw | ConvertFrom-Json
        if ($buildStatus.status -ne 'PASS') {
            return (Write-Incomplete $resultPath $name 'build' `
                ($buildStatus.error ?? 'Fixed Win32 build did not pass.'))
        }
    }

    $sdkArchive = Join-Path $workspace "$($Revision.directory)/sdk-build/sdk-build.tar.gz"
    $sdkExtracted = Join-Path $workspace "$($Revision.directory)/sdk-extracted"
    $sdkProvenance = Join-Path $sdkExtracted 'build-provenance.json'
    $runtimeDirectory = Join-Path $workspace $Revision.runtimeDirectory
    $platformProvenance = Join-Path $runtimeDirectory 'build-provenance.json'
    if (-not (Test-Path -LiteralPath $sdkArchive) -or
        -not (Test-Path -LiteralPath $runtimeDirectory) -or
        -not (Test-Path -LiteralPath $sdkProvenance)) {
        return (Write-Incomplete $resultPath $name 'artifact-download' `
            'Exact SDK or Windows runtime artifact is missing.')
    }

    $platformArtifactId = [string]$Revision.platformArtifactId
    $platformArtifactDigest = [string]$Revision.platformArtifactDigest
    if ([string]::IsNullOrWhiteSpace($platformArtifactId)) { $platformArtifactId = 'unavailable' }
    if ([string]::IsNullOrWhiteSpace($platformArtifactDigest)) {
        $platformArtifactDigest = 'unavailable'
    }
    $prepareArguments = @(
        $pythonHarness, 'prepare', '--revision', $name,
        '--runtime-home', (Join-Path $sdkExtracted 'build/TotalCross'),
        '--platform-artifact', $runtimeDirectory,
        '--sdk-provenance', $sdkProvenance,
        '--platform-provenance', $platformProvenance,
        '--source', (Join-Path $workspace 'tests/windows-crash-diagnostics/src/totalcross/ui/WindowsNativeStartupProbe.java'),
        '--output', $output, '--sdk-archive', $sdkArchive,
        '--artifact-run-id', $Revision.runId,
        '--sdk-artifact-id', $Revision.sdkArtifactId,
        '--sdk-artifact-name', $Revision.sdkArtifactName,
        '--sdk-artifact-digest', $Revision.sdkArtifactDigest,
        '--platform-artifact-id', $platformArtifactId,
        '--platform-artifact-name', $Revision.platformArtifactName,
        '--platform-artifact-digest', $platformArtifactDigest
    )
    $prepareCode = Invoke-PythonLogged $prepareArguments (Join-Path $output 'prepare.log')
    if ($prepareCode -ne 0) {
        return (Write-Incomplete $resultPath $name 'prepare' `
            "SDK compilation/deployment failed with exit code $prepareCode.")
    }

    $manifest = Get-Content -LiteralPath (Join-Path $output 'diagnostic-manifest.json') `
        -Raw | ConvertFrom-Json
    $runnerLog = Join-Path $output 'runner-launch.log'
    try {
        & $nativeRunner `
            -Executable $manifest.deployedProbe.path `
            -OutputDirectory $output `
            -SourceCommit $Revision.commit `
            -ProcdumpPath $env:PROCDUMP_PATH `
            -DebuggerPath $env:DEBUGGER_PATH `
            -PdbDirectory (Join-Path $output 'symbols') `
            -Architecture I386 `
            -TimeoutSeconds 60 *> $runnerLog
    } catch {
        Set-Content -LiteralPath $runnerLog -Value $_.Exception.Message -Encoding utf8NoBOM
    }

    $summaryCode = Invoke-PythonLogged @($pythonHarness, 'summarize', '--output', $output) `
        (Join-Path $output 'summarize.log')
    if ($summaryCode -ne 0 -or -not (Test-Path -LiteralPath $resultPath)) {
        return (Write-Incomplete $resultPath $name 'summarize' `
            "Startup evidence could not be summarized (exit code $summaryCode).")
    }
    $result = Get-Content -LiteralPath $resultPath -Raw | ConvertFrom-Json
    $debugConsole = Join-Path (Split-Path -Parent $manifest.deployedProbe.path) 'DebugConsole.txt'
    if (Test-Path -LiteralPath $debugConsole) {
        Copy-Item -LiteralPath $debugConsole -Destination $output -Force
        $result | Add-Member -NotePropertyName debugConsole -NotePropertyValue `
            ([ordered]@{ present = $true; sha256 = Get-Sha256 $debugConsole }) -Force
    } else {
        $result | Add-Member -NotePropertyName debugConsole `
            -NotePropertyValue ([ordered]@{ present = $false; sha256 = $null }) -Force
    }
    Write-JsonFile $resultPath $result

    if ($name -eq 'fixed-win32') {
        if ($result.status -eq 'PASS') {
            $script:ownershipResult = Invoke-OwnershipSmoke $manifest `
                (Join-Path $sdkExtracted 'build/TotalCross') $sdkArchive $sdkProvenance
        } else {
            $script:ownershipResult = Write-Incomplete `
                (Join-Path $resultRoot 'fixed-win32/ownership/image-ownership-result.json') `
                'fixed-win32' 'ownership-gate' 'Not run: corrected Win32 startup did not pass.'
        }
    }
    return $result
}

New-Item -ItemType Directory -Force -Path $resultRoot | Out-Null
$revisions = @(
    [ordered]@{
        name = 'historical-a'; commit = '61034a7562e7fa43df076dcc38bcd48128605395'
        directory = 'artifacts/historical-a'; runtimeDirectory = 'artifacts/historical-a/windows'
        runId = '44414042826'; sdkArtifactId = '14479211695'; sdkArtifactName = 'sdk-build'
        sdkArtifactDigest = 'sha256:fe5384859366b00f5099f564d9069e959c4150a070cf6932396d25f118428fc8'
        platformArtifactId = '14478821824'; platformArtifactName = 'windows'
        platformArtifactDigest = 'sha256:1989c1db41f21d31c82305418f59cbde51b0d891575cd3ba8e3afa553572704a'
    },
    [ordered]@{
        name = 'historical-b'; commit = 'c7cf79575602f3cc19e107643f0fa39d3e151004'
        directory = 'artifacts/historical-b'; runtimeDirectory = 'artifacts/historical-b/windows'
        runId = '44565183218'; sdkArtifactId = '14551617269'; sdkArtifactName = 'sdk-build'
        sdkArtifactDigest = 'sha256:fd53c7871d9427e858044906041bde525f4e785f0f0ad05cbff9ec9e85686eba'
        platformArtifactId = '14551309226'; platformArtifactName = 'windows'
        platformArtifactDigest = 'sha256:8609a483bc1be6796bf97c6cfb7f6524b06ef64146220b70cbded688ed513f72'
    },
    [ordered]@{
        name = 'fixed-win32'; commit = '94af2b8b653cad509994b5ccf3dbb60ac28ef920'
        directory = 'artifacts/fixed-win32'; runtimeDirectory = 'artifacts/fixed-win32-runtime'
        runId = $env:GITHUB_RUN_ID; sdkArtifactId = '15148184230'; sdkArtifactName = 'sdk-build-fixed'
        sdkArtifactDigest = 'sha256:82bfd5bf558dba1bba6a703477fe1feb146d5dae05df84d1d1551baa5db18df8'
        platformArtifactId = $env:FIXED_WIN32_ARTIFACT_ID
        platformArtifactName = 'windows-fixed-win32-runtime'
        platformArtifactDigest = $env:FIXED_WIN32_ARTIFACT_DIGEST
    }
)

foreach ($revision in $revisions) {
    $startupResults.Add((Invoke-StartupProbe $revision))
}

if ($null -eq $ownershipResult) {
    $ownershipResult = Write-Incomplete `
        (Join-Path $resultRoot 'fixed-win32/ownership/image-ownership-result.json') `
        'fixed-win32' 'ownership-gate' 'Not run: corrected Win32 startup did not pass.'
}
$ownershipStatus = $ownershipResult.status
$validation = [ordered]@{
    schemaVersion = 1
    runId = $env:GITHUB_RUN_ID
    runAttempt = $env:GITHUB_RUN_ATTEMPT
    runUrl = "$env:GITHUB_SERVER_URL/$env:GITHUB_REPOSITORY/actions/runs/$env:GITHUB_RUN_ID"
    repository = $env:GITHUB_REPOSITORY
    workflowCommit = $env:GITHUB_SHA
    runner = [ordered]@{ name = 'windows-2022'; imageOS = $env:ImageOS; imageVersion = $env:ImageVersion }
    artifactReferences = [ordered]@{
        dependencies = [ordered]@{
            runId = '45884551862'; id = '15148893237'
            name = 'prepared-native-dependencies-fixed'
            digest = 'sha256:d2b007713e7bc374e4b190585efb7156893bf2ab15ae08b121ea865f2f216d1c'
        }
        nativeBridges = [ordered]@{
            runId = '45884551862'; id = '15147749510'; name = 'native-bridges-fixed'
            digest = 'sha256:a977aac5cbf59c355e5c9f178453952113eaf8588579c31a31d9a95e7d7643c8'
        }
        fixedWin32Runtime = [ordered]@{
            id = $env:FIXED_WIN32_ARTIFACT_ID; name = 'windows-fixed-win32-runtime'
            digest = $env:FIXED_WIN32_ARTIFACT_DIGEST
        }
        performanceLab = [ordered]@{
            commit = $env:PERFORMANCE_LAB_COMMIT
            branch = 'codex/windows-i386-ownership-smoke'
        }
    }
    startup = $startupResults.ToArray()
    ownership = [ordered]@{ status = $ownershipStatus; result = $ownershipResult }
}
Write-JsonFile (Join-Path $resultRoot 'windows-i386-validation-summary.json') $validation
$validation | ConvertTo-Json -Depth 12
