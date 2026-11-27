# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

import importlib.util
import json
from pathlib import Path
import struct
import tempfile
from types import SimpleNamespace
import unittest


SCRIPT = Path(__file__).resolve().parents[1] / "scripts" / "windows-native-crash-diagnostics.py"
SPEC = importlib.util.spec_from_file_location("windows_native_crash_diagnostics", SCRIPT)
DIAGNOSTICS = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(DIAGNOSTICS)


def passing_runtime():
    return {"processStartObserved": True, "exitCode": 0, "exitCodeUnsigned": 0,
            "exitCodeHex": "0x00000000",
            "sourceCommit": DIAGNOSTICS.REVISIONS["baseline"]["commit"]}


class WindowsNativeCrashDiagnosticsTest(unittest.TestCase):
    def test_accepts_all_startup_markers_and_clean_exit(self):
        result = DIAGNOSTICS.assess_probe(
            passing_runtime(), set(DIAGNOSTICS.REQUIRED_APP_MARKERS))
        self.assertEqual(result["status"], "PASS")
        self.assertEqual(result["missingAppMarkers"], [])

    def test_rejects_access_violation_before_java_entry(self):
        runtime = passing_runtime()
        runtime.update({"exitCode": -1073741819,
                        "exitCodeUnsigned": 3221225477,
                        "exitCodeHex": "0xC0000005"})
        result = DIAGNOSTICS.assess_probe(runtime, set())
        self.assertEqual(result["status"], "FAIL")
        self.assertIn("0xC0000005", result["error"])
        self.assertEqual(result["observedAppMarkers"], [])

    def test_accepts_only_amd64_pe_images(self):
        with tempfile.TemporaryDirectory() as temporary:
            executable = Path(temporary) / "Launcher.exe"
            image = bytearray(0x88)
            image[:2] = b"MZ"
            struct.pack_into("<I", image, 0x3C, 0x80)
            image[0x80:0x84] = b"PE\0\0"
            struct.pack_into("<H", image, 0x84, 0x8664)
            executable.write_bytes(image)
            DIAGNOSTICS.require_amd64(executable)
            struct.pack_into("<H", image, 0x84, 0x014C)
            executable.write_bytes(image)
            with self.assertRaisesRegex(DIAGNOSTICS.DiagnosticError, "not AMD64"):
                DIAGNOSTICS.require_amd64(executable)

    def test_accepts_only_the_requested_windows_pe_architecture(self):
        with tempfile.TemporaryDirectory() as temporary:
            executable = Path(temporary) / "Launcher.exe"
            image = bytearray(0x88)
            image[:2] = b"MZ"
            struct.pack_into("<I", image, 0x3C, 0x80)
            image[0x80:0x84] = b"PE\0\0"
            struct.pack_into("<H", image, 0x84, 0x014C)
            executable.write_bytes(image)
            DIAGNOSTICS.require_machine(executable, "I386")
            with self.assertRaisesRegex(DIAGNOSTICS.DiagnosticError, "not AMD64"):
                DIAGNOSTICS.require_machine(executable, "AMD64")

    def test_finds_app_path_markers_without_a_trailing_separator(self):
        with tempfile.TemporaryDirectory() as temporary:
            install = Path(temporary) / "install"
            app_directory = install / "win32"
            app_directory.mkdir(parents=True)
            executable = app_directory / "WindowsNativeStartupProbe.exe"
            marker = install / "win32tcdiag-application-complete.marker"
            marker.write_text("application-complete", encoding="utf-8")
            self.assertEqual(DIAGNOSTICS.find_diagnostic_file(
                executable, DIAGNOSTICS.REQUIRED_APP_MARKERS[-1]), marker)

    def test_summarizes_markers_from_app_path_and_launcher_output(self):
        with tempfile.TemporaryDirectory() as temporary:
            output = Path(temporary) / "minimal"
            output.mkdir()
            install = output / "install"
            app_directory = install / "win32"
            app_directory.mkdir(parents=True)
            executable = app_directory / "WindowsNativeStartupProbe.exe"
            marker = DIAGNOSTICS.REQUIRED_APP_MARKERS[-1]
            (install / ("win32" + marker)).write_text("complete", encoding="utf-8")
            (output / "runtime-stdout.log").write_text(
                "\n".join("TC_WINDOWS_STARTUP_STAGE "
                          + name.removeprefix("tcdiag-").removesuffix(".marker")
                          for name in DIAGNOSTICS.REQUIRED_APP_MARKERS[:-1]),
                encoding="utf-8")
            (output / "runtime.json").write_text(json.dumps({
                "processStartObserved": True,
                "exitCode": 0,
                "exitCodeUnsigned": 0,
                "exitCodeHex": "0x00000000",
                "stdoutLog": "runtime-stdout.log",
            }), encoding="utf-8")
            (output / "diagnostic-manifest.json").write_text(json.dumps({
                "deployedProbe": {"path": str(executable)},
                "revision": "fixed",
                "sourceCommit": DIAGNOSTICS.REVISIONS["fixed"]["commit"],
                "sourceTree": DIAGNOSTICS.REVISIONS["fixed"]["tree"],
                "configuration": "Visual Studio 17 2022 Win32 Release",
                "architecture": "I386",
                "peMachine": "0x014c",
            }), encoding="utf-8")
            DIAGNOSTICS.summarize(SimpleNamespace(output=output))
            result = json.loads((output / "diagnostic-result.json").read_text())
            self.assertEqual(result["status"], "PASS")
            self.assertEqual(result["architecture"], "I386")
            self.assertEqual(result["exitCodeHex"], "0x00000000")

    def test_historical_artifacts_are_pinned_to_the_customer_runs(self):
        self.assertEqual(DIAGNOSTICS.REVISIONS["historical-444"]["runId"],
                         "44414042826")
        self.assertEqual(DIAGNOSTICS.REVISIONS["historical-445"]["runId"],
                         "44565183218")

    def test_rejects_mixed_revision_provenance(self):
        with tempfile.TemporaryDirectory() as temporary:
            path = Path(temporary) / "provenance.json"
            fixed = DIAGNOSTICS.REVISIONS["fixed"]
            path.write_text(json.dumps({
                "repository": "TotalCross/totalcross",
                "source": {"commit": fixed["commit"], "tree": fixed["tree"],
                           "dirty": False},
            }), encoding="utf-8")
            with self.assertRaisesRegex(DIAGNOSTICS.DiagnosticError, "does not match baseline"):
                DIAGNOSTICS.verify_build_manifest(path, "baseline", "Windows")

    def test_selects_only_the_matching_native_symbol_file(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            launcher = root / "Launcher.exe"
            launcher.touch()
            launcher_pdb = root / "Launcher.pdb"
            launcher_pdb.write_bytes(b"launcher symbols")
            vm_pdb = root / "tcvm.pdb"
            vm_pdb.write_bytes(b"vm symbols")
            self.assertEqual(DIAGNOSTICS.matching_pdb(
                launcher, [launcher_pdb, vm_pdb]), launcher_pdb)

    def test_rejects_conflicting_matching_symbol_files(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            launcher = root / "Launcher.exe"
            launcher.touch()
            first = root / "Launcher.pdb"
            second = root / "other" / "Launcher.pdb"
            second.parent.mkdir()
            first.write_bytes(b"symbols A")
            second.write_bytes(b"symbols B")
            with self.assertRaisesRegex(DIAGNOSTICS.DiagnosticError, "ambiguous matching PDB"):
                DIAGNOSTICS.matching_pdb(launcher, [first, second])

    def test_minimal_probe_is_independent_of_image_ownership(self):
        source = (SCRIPT.parents[1] / "tests/windows-crash-diagnostics/src/totalcross/ui/"
                  "WindowsNativeStartupProbe.java").read_text(encoding="utf-8")
        for marker in DIAGNOSTICS.REQUIRED_APP_MARKERS:
            self.assertIn(marker.removeprefix("tcdiag-").removesuffix(".marker"), source)
        self.assertIn("repaintNow()", source)
        self.assertIn("MainWindow.exit(0)", source)
        self.assertIn("MainWindow.exit(1)", source)
        for forbidden in ("ImageControl", "ImagePipeline", "new Image(", "ImageDrawingBridge"):
            self.assertNotIn(forbidden, source)

    def test_runner_captures_second_chance_access_violations_with_cdb(self):
        runner = (SCRIPT.parents[1] / "scripts/windows-native-crash-runner.ps1").read_text(
            encoding="utf-8")
        self.assertIn("sxd -c2", runner)
        self.assertIn(".dump /ma ", runner)
        self.assertIn("live-cdb.dmp", runner)
        self.assertIn("GetFullPath($PdbDirectory).Replace('\\', '/')", runner)
        self.assertIn(".Replace('\\', '/')", runner)
        self.assertEqual(runner.count("'-y', $symbolPath"), 2)
        self.assertNotIn("srv*", runner)
        self.assertIn("ln tcvm!__entry_from_strcat_in_strcpy+0x1f", runner)
        self.assertIn("ln tcvm!privateHeapSetJump+0x3c", runner)
        self.assertIn("procdumpCleanupStatus", runner)

    def test_runner_records_native_exit_code_and_hex_value(self):
        runner = (SCRIPT.parents[1] / "scripts/windows-native-crash-runner.ps1").read_text(
            encoding="utf-8")
        self.assertIn("$exitCode = $process.ExitCode", runner)
        self.assertIn("exitCodeUnsigned = $unsignedExit", runner)
        self.assertIn("exitCodeHex = $exitHex", runner)

    def test_i386_crash_capture_uses_x86_context_and_debugger(self):
        root = SCRIPT.parents[1]
        runner = (root / "scripts/windows-native-crash-runner.ps1").read_text(
            encoding="utf-8")
        installer = (root / "scripts/install-windows-crash-tools.ps1").read_text(
            encoding="utf-8")
        self.assertIn("[ValidateSet('I386', 'AMD64')][string]$Architecture", runner)
        self.assertIn("u @eip-0x20 L40", runner)
        self.assertIn("[ValidateSet('I386', 'AMD64')][string]$Architecture", installer)
        self.assertIn("\\$debuggerArchitecture\\cdb.exe", installer)

    def test_win32_validation_builds_only_i386_and_gates_ownership(self):
        root = SCRIPT.parents[1]
        build = (root / ".github/workflows/build.yml").read_text(encoding="utf-8")
        workflow = (root / ".github/workflows/windows-win32-image-ownership-validation.yml").read_text(
            encoding="utf-8")
        self.assertIn("windows_win32_image_ownership_smoke", build)
        self.assertIn("-A %WINDOWS_CMAKE_ARCH%", build)
        self.assertIn("windows_win32_image_ownership_smoke: true", workflow)
        self.assertIn("run-id: 44414042826", workflow)
        self.assertIn("run-id: 44565183218", workflow)
        self.assertIn("needs.minimal-probe.result == 'success'", workflow)
        self.assertIn("--windows-architecture I386", workflow)


if __name__ == "__main__":
    unittest.main()
