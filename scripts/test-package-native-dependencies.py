#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only
"""Focused exact-content tests for prepared native dependency packaging."""

import importlib.util
import json
import pathlib
import subprocess
import tarfile
import tempfile
import unittest


ROOT = pathlib.Path(__file__).resolve().parents[1]
MODULE_PATH = ROOT / "scripts" / "package-native-dependencies.py"
SPEC = importlib.util.spec_from_file_location("package_native_dependencies", MODULE_PATH)
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


def marker(dependency, asset):
    return {
        "schema": 1,
        "dependency": dependency,
        "repository": "owner/repo",
        "release_tag": "release-tag",
        "asset": asset,
        "sha256": "1" * 64,
    }


class PackageNativeDependenciesTests(unittest.TestCase):
    def test_archive_contains_tracked_checkout_and_only_selected_installs(self):
        with tempfile.TemporaryDirectory() as directory:
            root = pathlib.Path(directory)
            depot = root / "depot"
            depot.mkdir()
            subprocess.run(["git", "init", "-q", str(depot)], check=True)
            subprocess.run(["git", "-C", str(depot), "config", "user.name", "Test"], check=True)
            subprocess.run(["git", "-C", str(depot), "config", "user.email", "test@example.invalid"], check=True)
            (depot / "deps.yml").write_text("bundle: test\n")
            subprocess.run(["git", "-C", str(depot), "add", "deps.yml"], check=True)
            subprocess.run(["git", "-C", str(depot), "commit", "-qm", "fixture"], check=True)

            selected = depot / "libpng/local/linux/x86_64"
            selected.mkdir(parents=True)
            (selected / "libpng.a").write_bytes(b"selected")
            selected_marker = marker("libpng", "libpng.tar.gz")
            (selected / ".totalcross-artifact.json").write_text(json.dumps(selected_marker))
            stale = depot / "libpng/local/windows/x86"
            stale.mkdir(parents=True)
            (stale / "stale.lib").write_bytes(b"stale")

            skia_local = depot / "skia/local"
            (skia_local / "include/core").mkdir(parents=True)
            (skia_local / "include/core/SkCanvas.h").write_text("header")
            shared_marker = marker("skia-shared", "dev.zip")
            (skia_local / ".totalcross-artifact.json").write_text(json.dumps(shared_marker))
            skia_target = skia_local / "out/Release/linux/x86_64"
            skia_target.mkdir(parents=True)
            (skia_target / "libskia.a").write_bytes(b"selected skia")
            target_marker = marker("skia", "libskia.a")
            (skia_target / ".totalcross-artifact.json").write_text(json.dumps(target_marker))
            stale_skia = skia_local / "out/Release/windows/x64"
            stale_skia.mkdir(parents=True)
            (stale_skia / "stale.lib").write_bytes(b"stale")

            entries = []
            for payload, path in (
                (selected_marker, selected / ".totalcross-artifact.json"),
                (shared_marker, skia_local / ".totalcross-artifact.json"),
                (target_marker, skia_target / ".totalcross-artifact.json"),
            ):
                entry = dict(payload)
                entry["marker_path"] = str(path.relative_to(depot))
                entries.append(entry)
            index = root / "index.jsonl"
            index.write_text("".join(json.dumps(entry) + "\n" for entry in entries))
            output = root / "prepared.tar.gz"
            summary = MODULE.package(depot, index, output)
            self.assertEqual(3, summary["entries"])
            with tarfile.open(str(output), "r:gz") as archive:
                names = set(archive.getnames())
                manifest = json.load(archive.extractfile("TotalCrossVM/deps/totalcross-depot-tools/prepared-native-dependencies.json"))
            prefix = "TotalCrossVM/deps/totalcross-depot-tools/"
            self.assertIn(prefix + "deps.yml", names)
            self.assertIn(prefix + "libpng/local/linux/x86_64/libpng.a", names)
            self.assertIn(prefix + "skia/local/include/core/SkCanvas.h", names)
            self.assertIn(prefix + "skia/local/out/Release/linux/x86_64/libskia.a", names)
            self.assertFalse(any("/.git/" in name or name.endswith("/.git") for name in names))
            self.assertFalse(any("windows/x86/stale.lib" in name or "windows/x64/stale.lib" in name for name in names))
            self.assertEqual(3, len(manifest["dependencies"]))
            self.assertEqual(summary["revision"], manifest["depot_tools_revision"])

            extraction = root / "consumer"
            extraction.mkdir()
            with tarfile.open(str(output), "r:gz") as archive:
                archive.extractall(str(extraction))
            restored = extraction / "TotalCrossVM/deps/totalcross-depot-tools"
            self.assertEqual(summary["revision"], subprocess.check_output(
                ["git", "-C", str(depot), "rev-parse", "HEAD"], text=True
            ).strip())
            self.assertTrue((restored / "libpng/local/linux/x86_64/libpng.a").is_file())
            self.assertTrue((restored / "skia/local/out/Release/linux/x86_64/libskia.a").is_file())


if __name__ == "__main__":
    unittest.main()
