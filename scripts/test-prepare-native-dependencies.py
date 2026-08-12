#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only
"""Focused contract tests for native dependency preparation."""

import pathlib
import shlex
import subprocess
import unittest


ROOT = pathlib.Path(__file__).resolve().parents[1]
SCRIPT = ROOT / "scripts" / "prepare-native-dependencies.sh"
DEPOT = ROOT / "TotalCrossVM" / "deps" / "totalcross-depot-tools"


class PrepareNativeDependenciesTests(unittest.TestCase):
    def test_dry_run_keeps_targets_sequential_and_installs_skia_shared_once(self):
        result = subprocess.run(
            ["bash", str(SCRIPT), "--dry-run"],
            cwd=str(ROOT),
            env={"PATH": "/usr/bin:/bin", "RUNNER_TEMP": "/tmp", "TOTALCROSS_DEPOT_TOOLS_DIR": str(DEPOT)},
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
        )
        self.assertEqual(0, result.returncode, result.stderr)
        commands = [shlex.split(line)[1:] for line in result.stdout.splitlines() if line.startswith("FETCH ")]
        self.assertEqual(71, len(commands))
        skia_targets = [command for command in commands if command[1].endswith("/skia/fetch.sh") and "--install-shared" not in command]
        skia_shared = [command for command in commands if "--install-shared" in command]
        self.assertEqual(7, len(skia_targets))
        self.assertEqual(1, len(skia_shared))
        self.assertFalse(any("--install-dev" in command for command in skia_targets))
        sqlite = [command for command in commands if command[1].endswith("/sqlite3/fetch.sh")]
        self.assertEqual(7, len(sqlite))
        self.assertEqual(1, len({command[command.index("--release-tag") + 1] for command in sqlite}))
        self.assertEqual(1, len({command[command.index("--github-repo") + 1] for command in sqlite}))


if __name__ == "__main__":
    unittest.main()
