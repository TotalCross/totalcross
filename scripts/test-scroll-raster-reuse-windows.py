# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

"""Focused static contract checks for the Windows raster-reuse package."""

from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
RUNNER = (ROOT / "scripts/run-scroll-raster-reuse-windows.ps1").read_text()
HELPER = (ROOT / "scripts/scroll-raster-reuse-windows-functions.ps1").read_text()
ANALYSIS = (ROOT / "scripts/scroll-raster-reuse-windows-analysis.ps1").read_text()
APP = (ROOT / "TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/"
       "ImageScrollRealWorkloadBenchmarkApp.java").read_text()
SDL = (ROOT / "TotalCrossVM/src/init/tcsdl.cpp").read_text()
PACKAGER = (ROOT / "scripts/package-image-scroll-benchmark.sh").read_text()
README = (ROOT / "scripts/README-scroll-raster-reuse-windows.md").read_text()


def require(condition, message):
    if not condition:
        raise AssertionError(message)


def test_profile_and_scroll_counters():
    for value in (
        '"scroll-raster-reuse-windows".equals(benchmarkProfile)',
        'windowsRasterReuseDefaults ? "6" : null',
        'windowsRasterReuseDefaults ? 2 : 1',
        'windowsRasterReuseDefaults ? "on" : "off"',
        'PREFETCH_THREAD_MODE_WORKER_SEMAPHORE',
        'windowsScrollRasterReuseProfile && accountingEnabled()',
        'result.jpegDecodes == 0',
        'result.imageMaterializations == 0',
        'result.nativeGeometryMaterializations == 0',
        'scroll_jpeg_decode_count=',
    ):
        require(value in APP, f"missing app profile contract: {value}")
    require('scrollRasterReuseProfile ? POC_IMAGE_COUNT : IMAGE_COUNT' in APP,
            "Windows profile must retain the full 663-image workload")
    require('!windowsScrollRasterReuseProfile' in APP,
            "Windows profile must accept zero reuse hits on unsupported drawable formats")
    require('expected_final_displacement=' in APP and 'actual_final_displacement=' in APP,
            "app records must expose expected and actual final scroll displacement")


def test_process_matrix_and_configuration():
    require("foreach ($mode in @('off','on'))" in RUNNER and
            "for ($sample = 1; $sample -le 3; $sample++)" in RUNNER,
            "runner must execute OFF x3 followed by ON x3")
    require("measuredModeOrder = @('off','off','off','on','on','on')" in RUNNER,
            "execution metadata must record the exact six-process order")
    require("$script:manifest.measuredProcessCount -ne 6" in RUNNER and
            "'measuredProcessCount': 6" not in RUNNER,
            "runner must validate manifest process count")
    require('"--rendering-reuse=$Mode"' in RUNNER,
            "OFF/ON must vary through only the raster-reuse argument")
    for value in (
        "--profile=scroll-raster-reuse-windows", "--accounting=$Accounting",
        "--flick-config=timer,60,nano,absolute,poll,legacy",
        "TC_TIMER_DEADLINE_MODE='absolute'", "TC_EVENT_LOOP_MODE='poll'",
        "TC_THREAD_YIELD_MODE='legacy'", "TC_SDL_PIXEL_FORMAT='auto'",
    ):
        require(value in RUNNER, f"missing fixed runner configuration: {value}")


def test_windows_contracts():
    require("Set-StrictMode -Version Latest" in RUNNER and
            "$ErrorActionPreference = 'Stop'" in RUNNER,
            "runner must enable strict PowerShell error handling")
    require("Start-Process" in RUNNER and "-PassThru" in RUNNER and
            "$process.Handle" in RUNNER and ".WaitForExit(15000)" in RUNNER and
            "$process.WaitForExit()" in RUNNER and "elapsed_seconds=" in RUNNER and
            "-ge 900000" in RUNNER,
            "runner must use the required process lifecycle")
    require("GetEnvironmentVariable($name, 'Process')" in RUNNER and
            "SetEnvironmentVariable($name, $previousEnvironment[$name], 'Process')" in RUNNER,
            "runner must restore process environment variables")
    forbidden = re.compile(r"\b(python|py|java|git|gh|curl|wget)\b|Invoke-WebRequest|Invoke-RestMethod", re.I)
    require(forbidden.search(RUNNER + HELPER + ANALYSIS) is None,
            "PowerShell package must not invoke external tools or network commands")
    require("1080x1920" not in RUNNER and "1080,1920" not in RUNNER and
            "logicalWidth -ne 540" in RUNNER and "drawableWidth -le 0" in RUNNER,
            "only logical size may be fixed; physical dimensions must be detected")
    require("Write-ExecutionMetadata 'FAILED'" in RUNNER and
            "Compress-Archive" in RUNNER and "Assert-PreflightMatch" in RUNNER,
            "runner must archive failures and compare correctness waypoints")
    require("final displacement differs from its matching waypoints" in RUNNER and
            "expectedFinalDisplacement" in HELPER and "actualFinalDisplacement" in HELPER,
            "runner must match final scroll displacement against preflight waypoints")
    require("function Write-PassSummaryCsv" in RUNNER and "passSummaries=$passSummaries.ToArray()" in HELPER and
            "'screenUpdateP95Ns'" in ANALYSIS and "foreach ($passName in @('cold','warm'))" in HELPER,
            "runner must preserve separate cold/warm distributions and screen-update comparisons")
    require("TC_SCROLL_RASTER_REUSE_SDL_PIXEL_FORMAT=%s" in SDL,
            "native runtime must record its selected SDL pixel format")


def test_package_and_limits():
    require("--windows-scroll-raster-reuse" in PACKAGER and
            "exactly --target windows-x64" in PACKAGER,
            "packager must restrict the dedicated mode to Windows x64")
    require('"measuredProcessCount": 6' in PACKAGER and
            '"preflightProcessCount": 2' in PACKAGER,
            "package manifest must require six measured and two preflight processes")
    require("scroll-raster-reuse-windows-functions.ps1" in PACKAGER and
            "run-scroll-raster-reuse-windows.ps1" in PACKAGER and
            "scroll-raster-reuse-windows-analysis.ps1" in PACKAGER and
            '"ImageScrollRealWorkloadBenchmarkApp.tcz"' in PACKAGER,
            "dedicated package must include its app and PowerShell runner files")
    require("powershell -ExecutionPolicy Bypass -File .\\run-scroll-raster-reuse-windows.ps1" in README,
            "package README must provide the one-command operator entry point")
    for path in (ROOT / "scripts/run-scroll-raster-reuse-windows.ps1",
                 ROOT / "scripts/scroll-raster-reuse-windows-functions.ps1",
                 ROOT / "scripts/scroll-raster-reuse-windows-analysis.ps1"):
        require(path.stat().st_size < 20 * 1024 and len(path.read_text().splitlines()) < 600,
                f"new PowerShell file exceeds the size limit: {path.name}")


def main():
    for test in (test_profile_and_scroll_counters, test_process_matrix_and_configuration,
                 test_windows_contracts, test_package_and_limits):
        test()
    print("Windows scroll raster reuse contract checks passed (4 groups)")


if __name__ == "__main__":
    main()
