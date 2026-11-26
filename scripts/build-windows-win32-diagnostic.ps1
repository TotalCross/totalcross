<#
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
#>

[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$workspace = $env:GITHUB_WORKSPACE
$source = Join-Path $workspace 'fixed-source'
$output = Join-Path $workspace 'artifacts/fixed-win32-runtime'
$dependencyArchive = Join-Path $workspace 'artifacts/fixed-inputs/deps/depot-tools-native-dependencies.tar.gz'
$bridgeDirectory = Join-Path $source 'TotalCrossSDK/build/generated/native-bridges'
$sdkProvenancePath = Join-Path $workspace 'artifacts/fixed-win32/sdk-extracted/build-provenance.json'

function Write-JsonFile([string]$Path, $Value) {
    ConvertTo-Json -InputObject $Value -Depth 20 |
        Set-Content -LiteralPath $Path -Encoding utf8NoBOM
}

function Invoke-Logged([string]$FilePath, [string[]]$Arguments,
                       [string]$WorkingDirectory, [string]$LogPath) {
    Push-Location $WorkingDirectory
    try {
        & $FilePath @Arguments *> $LogPath
        $code = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    if ($code -ne 0) {
        throw "$FilePath failed with exit code $code; see $LogPath"
    }
}

New-Item -ItemType Directory -Force -Path $output | Out-Null
$statusPath = Join-Path $output 'build-status.json'
try {
    if (-not (Test-Path -LiteralPath $source)) { throw 'Fixed source checkout is missing.' }
    $commit = (& git -C $source rev-parse HEAD).Trim()
    if ($commit -ne '94af2b8b653cad509994b5ccf3dbb60ac28ef920') {
        throw "Fixed source checkout resolved to unexpected commit $commit"
    }
    $tree = (& git -C $source rev-parse 'HEAD^{tree}').Trim()
    if ($tree -ne '539f5b424b6f4593c7d96074d69ff077a38ed361') {
        throw "Fixed source checkout resolved to unexpected tree $tree"
    }
    $dirty = @(& git -C $source status --porcelain=v1 --untracked-files=no)
    if ($LASTEXITCODE -ne 0) { throw 'Cannot verify fixed source checkout status.' }
    if ($dirty.Count -gt 0) { throw 'Fixed source checkout contains tracked source changes.' }
    if (-not (Test-Path -LiteralPath $dependencyArchive)) {
        throw 'Pinned prepared native dependency artifact is missing.'
    }
    if (-not (Test-Path -LiteralPath $bridgeDirectory) -or
        -not (Get-ChildItem -LiteralPath $bridgeDirectory -File -Recurse | Select-Object -First 1)) {
        throw 'Pinned generated native bridge artifact is missing.'
    }

    Invoke-Logged 'tar' @('-xzf', $dependencyArchive, '-C', $source) `
        $workspace (Join-Path $output 'dependency-extraction.log')
    Invoke-Logged 'python' @('scripts/provenance/validate-compatibility.py',
        '--repository-root', '.') $source (Join-Path $output 'compatibility-validation.log')

    $sdkProvenance = Get-Content -LiteralPath $sdkProvenancePath -Raw | ConvertFrom-Json
    $manifest = [ordered]@{
        schemaVersion = 1
        repository = 'TotalCross/totalcross'
        source = [ordered]@{ commit = $commit; tree = $tree; dirty = $false }
        producer = [ordered]@{
            kind = 'github-actions'
            runId = $env:GITHUB_RUN_ID
            runAttempt = $env:GITHUB_RUN_ATTEMPT
        }
        compatibility = $sdkProvenance.compatibility
    }
    Write-JsonFile (Join-Path $source 'build-provenance.json') $manifest
    Invoke-Logged 'python' @('scripts/provenance/build-identity.py',
        '--repository-root', '.', '--update-manifest', 'build-provenance.json',
        '--format', 'id') $source (Join-Path $output 'build-identity.log')

    $buildDirectory = Join-Path $source 'TotalCrossVM/vc2022'
    New-Item -ItemType Directory -Force -Path $buildDirectory | Out-Null
    Invoke-Logged 'cmake' @('..', '-G', 'Visual Studio 17 2022', '-A', 'Win32') `
        $buildDirectory (Join-Path $output 'cmake-configure.log')
    $cachePath = Join-Path $buildDirectory 'CMakeCache.txt'
    $cacheText = Get-Content -LiteralPath $cachePath -Raw
    $requiredCache = @(
        'CMAKE_GENERATOR:INTERNAL=Visual Studio 17 2022',
        'CMAKE_GENERATOR_PLATFORM:INTERNAL=Win32',
        'TC_RENDERER_SKIA:BOOL=ON',
        'TC_RENDERER_LEGACY:BOOL=OFF',
        'TC_WINDOWING_SDL:BOOL=ON',
        'TC_WINDOWING_NATIVE:BOOL=OFF',
        'TC_GRAPHICS_SOFTWARE:BOOL=ON',
        'TC_GRAPHICS_GLES:BOOL=OFF'
    )
    foreach ($entry in $requiredCache) {
        if (-not $cacheText.Contains($entry)) { throw "Production CMake setting is missing: $entry" }
    }
    $projectFile = Join-Path $buildDirectory 'tcvm.vcxproj'
    if (-not (Select-String -LiteralPath $projectFile -SimpleMatch `
        '<RuntimeLibrary>MultiThreaded</RuntimeLibrary>' -Quiet)) {
        throw 'Win32 Release runtime library is not statically linked.'
    }
    if (-not (Select-String -LiteralPath $projectFile -SimpleMatch `
        '<RuntimeLibrary>MultiThreadedDebug</RuntimeLibrary>' -Quiet)) {
        throw 'Win32 Debug runtime library is not statically linked.'
    }
    Invoke-Logged 'cmake' @('--build', '.', '--config', 'Release') `
        $buildDirectory (Join-Path $output 'cmake-build.log')

    $releaseDirectory = Join-Path $buildDirectory 'Release'
    foreach ($name in @('Launcher.exe', 'tcvm.dll')) {
        $binary = Join-Path $releaseDirectory $name
        if (-not (Test-Path -LiteralPath $binary)) { throw "Build output is missing $name" }
        Copy-Item -LiteralPath $binary -Destination $output -Force
    }
    Copy-Item -LiteralPath (Join-Path $source 'build-provenance.json') `
        -Destination $output -Force
    Copy-Item -LiteralPath $cachePath -Destination $output -Force
    Get-ChildItem -LiteralPath $releaseDirectory -Filter '*.pdb' -File |
        Copy-Item -Destination $output -Force

    $provenance = Get-Content -LiteralPath (Join-Path $output 'build-provenance.json') `
        -Raw | ConvertFrom-Json
    $cacheLines = [regex]::Split($cacheText, '\r?\n')
    $cCompiler = ($cacheLines | Where-Object { $_ -match '^CMAKE_C_COMPILER:FILEPATH=' } |
        Select-Object -First 1) -replace '^CMAKE_C_COMPILER:FILEPATH=', ''
    $cxxCompiler = ($cacheLines | Where-Object { $_ -match '^CMAKE_CXX_COMPILER:FILEPATH=' } |
        Select-Object -First 1) -replace '^CMAKE_CXX_COMPILER:FILEPATH=', ''
    $binaryRecords = foreach ($name in @('Launcher.exe', 'tcvm.dll')) {
        $path = Join-Path $output $name
        [ordered]@{
            name = $name
            sha256 = (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant()
            length = (Get-Item -LiteralPath $path).Length
        }
    }
    $buildSummary = [ordered]@{
        status = 'PASS'
        sourceCommit = $commit
        sourceTree = $tree
        buildId = $provenance.build.id
        producerRunId = $env:GITHUB_RUN_ID
        configuration = 'Visual Studio 17 2022; Win32; Release'
        cmakeCommand = 'cmake .. -G "Visual Studio 17 2022" -A Win32'
        buildCommand = 'cmake --build . --config Release'
        graphics = [ordered]@{
            skia = $true; legacyRenderer = $false; sdlWindowing = $true
            nativeWindowing = $false; softwareSurface = $true; gles = $false
        }
        compiler = [ordered]@{
            c = $cCompiler
            cxx = $cxxCompiler
        }
        runnerImage = [ordered]@{ os = $env:ImageOS; version = $env:ImageVersion }
        dependencyArtifact = [ordered]@{
            runId = '45884551862'; id = '15148893237'
            name = 'prepared-native-dependencies-fixed'
            digest = 'sha256:d2b007713e7bc374e4b190585efb7156893bf2ab15ae08b121ea865f2f216d1c'
            archiveSha256 = (Get-FileHash -LiteralPath $dependencyArchive -Algorithm SHA256).Hash.ToLowerInvariant()
        }
        nativeBridgeArtifact = [ordered]@{
            runId = '45884551862'; id = '15147749510'; name = 'native-bridges-fixed'
            digest = 'sha256:a977aac5cbf59c355e5c9f178453952113eaf8588579c31a31d9a95e7d7643c8'
        }
        binaries = @($binaryRecords)
        pdbs = @(Get-ChildItem -LiteralPath $output -Filter '*.pdb' -File |
            ForEach-Object { [ordered]@{ name = $_.Name; sha256 = (Get-FileHash $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant() } })
    }
    Write-JsonFile (Join-Path $output 'win32-build-manifest.json') $buildSummary
    Write-JsonFile $statusPath $buildSummary
    $buildSummary | ConvertTo-Json -Depth 8
} catch {
    $failure = [ordered]@{
        status = 'FAIL'
        sourceCommit = '94af2b8b653cad509994b5ccf3dbb60ac28ef920'
        phase = 'build'
        error = $_.Exception.Message
        logDirectory = $output
    }
    Write-JsonFile $statusPath $failure
    Write-Error $_
    exit 1
}
