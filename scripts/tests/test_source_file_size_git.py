# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only
"""Git integration tests for source-size snapshot and rename semantics."""
from __future__ import annotations

import os
import subprocess
import tempfile
import unittest
from pathlib import Path

VALIDATOR = Path(__file__).resolve().parents[1] / "validate-source-files.py"
LIMIT = 20_480


class GitSizeValidationTest(unittest.TestCase):
    def setUp(self) -> None:
        self.temp_dir = tempfile.TemporaryDirectory()
        self.root = Path(self.temp_dir.name)
        self.git("init", "-q")
        self.git("config", "user.name", "Source Validator Test")
        self.git("config", "user.email", "source-validator@example.test")

    def tearDown(self) -> None:
        self.temp_dir.cleanup()

    def git(self, *args: str) -> str:
        return subprocess.check_output(
            ["git", *args], cwd=self.root, text=True, stderr=subprocess.STDOUT,
        ).strip()

    def write(self, path: str, size: int, byte: bytes = b"x") -> None:
        target = self.root / path
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(byte * size)

    def commit_file(self, path: str, size: int, message: str = "fixture") -> str:
        self.write(path, size)
        self.git("add", "--", path)
        self.git("commit", "-q", "-m", message)
        return self.git("rev-parse", "HEAD")

    def validate(self, *args: str, env: dict[str, str] | None = None) -> subprocess.CompletedProcess[str]:
        process_env = os.environ.copy()
        process_env.pop("EVENT_NAME", None)
        process_env.pop("GITHUB_EVENT_NAME", None)
        process_env.pop("PUSH_BEFORE", None)
        process_env.pop("PUSH_AFTER", None)
        process_env.pop("PR_BASE_SHA", None)
        process_env.pop("PR_HEAD_SHA", None)
        if env:
            process_env.update(env)
        return subprocess.run(
            ["python3", str(VALIDATOR), "--check", "size", *args],
            cwd=self.root, env=process_env, text=True,
            stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=False,
        )

    def test_new_file_boundaries_and_deletion(self) -> None:
        self.commit_file("seed.txt", 1)
        self.write("src/Exact.java", LIMIT)
        self.git("add", "src/Exact.java")
        self.assertEqual(0, self.validate("--staged").returncode)

        self.write("src/TooLarge.java", LIMIT + 1)
        self.git("add", "src/TooLarge.java")
        result = self.validate("--staged")
        self.assertEqual(1, result.returncode)
        self.assertIn("new source exceeds", result.stderr)

        self.git("reset", "-q", "HEAD", "--", "src/TooLarge.java")
        (self.root / "src/Exact.java").unlink()
        self.git("add", "-u", "src/Exact.java")
        self.assertEqual(0, self.validate("--staged").returncode)

    def test_root_commit_treats_sources_as_new(self) -> None:
        root_commit = self.commit_file("src/Root.java", LIMIT + 1)
        result = self.validate("--commit", root_commit)
        self.assertEqual(1, result.returncode)
        self.assertIn("base:       new file", result.stderr)

    def test_rename_inherits_oversized_baseline(self) -> None:
        base_size = 30 * 1024
        self.commit_file("src/Old.java", base_size)
        self.git("mv", "src/Old.java", "src/New.java")
        self.assertEqual(
            0, self.validate("--staged", "--files", "src/New.java").returncode,
        )

        self.write("src/New.java", base_size + 500)
        self.git("add", "src/New.java")
        warning = self.validate("--staged", "--files", "src/New.java")
        self.assertEqual(0, warning.returncode)
        self.assertIn("WARNING", warning.stderr)
        self.assertIn("+500 bytes", warning.stderr)

        self.write("src/New.java", base_size + 501)
        self.git("add", "src/New.java")
        failure = self.validate("--staged", "--files", "src/New.java")
        self.assertEqual(1, failure.returncode)
        self.assertIn("+501 bytes", failure.stderr)

    def test_staged_validation_reads_index_not_working_tree(self) -> None:
        path = "src/Partial.java"
        self.commit_file(path, 19 * 1024)

        self.write(path, 19_500)
        self.git("add", path)
        self.write(path, 25 * 1024)
        self.assertEqual(0, self.validate("--staged", "--files", path).returncode)

        self.write(path, 25 * 1024)
        self.git("add", path)
        self.write(path, 19_500)
        result = self.validate("--staged", "--files", path)
        self.assertEqual(1, result.returncode)
        self.assertIn("current:    25,600 bytes", result.stderr)

    def test_revision_environment_and_working_tree_modes(self) -> None:
        path = "src/Legacy.java"
        base = self.commit_file(path, 30 * 1024, "base")
        self.write(path, 30 * 1024 + 500)
        self.git("add", path)
        self.git("commit", "-q", "-m", "growth")
        head = self.git("rev-parse", "HEAD")

        for args, env in (
            ((base, head), None),
            (("--commit", head), None),
            ((), {"PUSH_BEFORE": base, "PUSH_AFTER": head}),
            ((), {"EVENT_NAME": "pull_request", "PR_BASE_SHA": base, "PR_HEAD_SHA": head}),
        ):
            with self.subTest(args=args, env=env):
                result = self.validate(*args, env=env)
                self.assertEqual(0, result.returncode)
                self.assertIn("WARNING", result.stderr)

        self.write(path, 30 * 1024 + 500 + 501)
        self.assertEqual(1, self.validate("--working-tree").returncode)


if __name__ == "__main__":
    unittest.main()
