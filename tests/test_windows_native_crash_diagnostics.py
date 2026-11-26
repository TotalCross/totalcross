# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

import importlib.util
import json
from pathlib import Path
import struct
import tempfile
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


if __name__ == "__main__":
    unittest.main()
