#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only
"""Tests for deterministic source and embedded build identity generation."""

import importlib.util
import json
import os
import pathlib
import subprocess
import sys
import tempfile
import unittest

sys.dont_write_bytecode = True


ROOT = pathlib.Path(__file__).resolve().parents[1]
SCRIPT = ROOT / "scripts" / "provenance" / "build-identity.py"
HELPER_PATH = ROOT / "scripts" / "provenance" / "build_identity.py"
SPEC = importlib.util.spec_from_file_location("build_identity", HELPER_PATH)
HELPER = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(HELPER)


def git(root, *args, env=None):
    return subprocess.check_output(
        ["git", *args], cwd=root, env=env, text=True, stderr=subprocess.PIPE
    ).strip()


def init_repository(root):
    git(root, "-c", "init.defaultBranch=main", "init", "-q")
    git(root, "config", "user.name", "Build Identity Test")
    git(root, "config", "user.email", "build-identity@example.invalid")
    git(root, "remote", "add", "origin", "git@github.com:TotalCross/totalcross.git")
    (root / "source.txt").write_text("committed source\n", encoding="utf-8")
    (root / ".gitignore").write_text("build/\n", encoding="utf-8")
    git(root, "add", "source.txt", ".gitignore")
    git(root, "commit", "-qm", "fixture")


def identity(root, *arguments, repository_override=None):
    env = os.environ.copy()
    env.pop("GITHUB_REPOSITORY", None)
    if repository_override is not None:
        env["GITHUB_REPOSITORY"] = repository_override
    return subprocess.run(
        [sys.executable, str(SCRIPT), "--repository-root", str(root), *arguments],
        text=True,
        capture_output=True,
        check=False,
        env=env,
    )


class BuildIdentityTests(unittest.TestCase):
    def setUp(self):
        self.temp_dir = tempfile.TemporaryDirectory()
        self.root = pathlib.Path(self.temp_dir.name) / "repo"
        self.root.mkdir()
        init_repository(self.root)

    def tearDown(self):
        self.temp_dir.cleanup()

    def test_clean_tree_is_deterministic(self):
        first = json.loads(identity(self.root).stdout)
        second = json.loads(identity(self.root).stdout)
        self.assertEqual(first, second)
        self.assertFalse(first["source"]["dirty"])

    def test_commit_is_traceability_only_when_tree_is_identical(self):
        before = json.loads(identity(self.root).stdout)
        git(self.root, "commit", "--allow-empty", "-qm", "same source tree")
        after = json.loads(identity(self.root).stdout)
        self.assertNotEqual(before["source"]["commit"], after["source"]["commit"])
        self.assertEqual(before["source"]["tree"], after["source"]["tree"])
        self.assertEqual(before["build"]["id"], after["build"]["id"])

    def test_dirty_tracked_and_untracked_content_changes_identity(self):
        clean = json.loads(identity(self.root).stdout)
        (self.root / "source.txt").write_text("changed source\n", encoding="utf-8")
        tracked = json.loads(identity(self.root).stdout)
        self.assertTrue(tracked["source"]["dirty"])
        self.assertNotEqual(clean["build"]["id"], tracked["build"]["id"])

        (self.root / "extra.txt").write_text("untracked source\n", encoding="utf-8")
        untracked = json.loads(identity(self.root).stdout)
        self.assertNotEqual(tracked["build"]["id"], untracked["build"]["id"])

    def test_dirty_identity_uses_a_temporary_index(self):
        (self.root / "source.txt").write_text("staged version\n", encoding="utf-8")
        git(self.root, "add", "source.txt")
        (self.root / "source.txt").write_text("worktree version\n", encoding="utf-8")
        index_path = self.root / ".git" / "index"
        index_before = index_path.read_bytes()
        cached_before = git(self.root, "diff", "--cached", "--binary")
        status_before = git(self.root, "status", "--porcelain=v1", "--untracked-files=all")

        result = identity(self.root)
        self.assertEqual(0, result.returncode, result.stderr)
        with tempfile.TemporaryDirectory() as temporary:
            expected_index = pathlib.Path(temporary) / "index"
            expected_env = {**os.environ, "GIT_INDEX_FILE": str(expected_index)}
            git(self.root, "read-tree", "HEAD", env=expected_env)
            git(self.root, "add", "-A", "--", ".", ":(exclude)build-provenance.json", env=expected_env)
            expected_tree = git(self.root, "write-tree", env=expected_env)
        self.assertEqual(expected_tree, json.loads(result.stdout)["source"]["tree"])
        self.assertEqual(index_before, index_path.read_bytes())
        self.assertEqual(cached_before, git(self.root, "diff", "--cached", "--binary"))
        self.assertEqual(
            status_before,
            git(self.root, "status", "--porcelain=v1", "--untracked-files=all"),
        )

    def test_generated_provenance_manifest_is_excluded_from_source_tree(self):
        clean = json.loads(identity(self.root).stdout)
        manifest_path = self.root / "build-provenance.json"
        manifest_path.write_text("generated build metadata\n", encoding="utf-8")

        generated = json.loads(identity(self.root).stdout)
        self.assertFalse(generated["source"]["dirty"])
        self.assertEqual(clean["source"]["tree"], generated["source"]["tree"])
        self.assertEqual(clean["build"]["id"], generated["build"]["id"])

    def test_repository_urls_normalize_without_exposing_credentials(self):
        first = json.loads(identity(self.root).stdout)
        self.assertEqual("totalcross/totalcross", first["repository"])
        git(self.root, "remote", "set-url", "origin", "https://github.com/totalcross/totalcross")
        second = json.loads(identity(self.root).stdout)
        self.assertEqual(first["build"]["id"], second["build"]["id"])

        git(
            self.root,
            "remote",
            "set-url",
            "origin",
            "https://user:secret@example.invalid/Group/Repo.git",
        )
        private_remote = identity(self.root)
        self.assertEqual(0, private_remote.returncode, private_remote.stderr)
        self.assertNotIn("secret", private_remote.stdout)
        self.assertEqual("example.invalid/group/repo", json.loads(private_remote.stdout)["repository"])

    def test_github_repository_environment_is_normalized(self):
        result = identity(self.root, repository_override="TotalCross/TotalCross")
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual("totalcross/totalcross", json.loads(result.stdout)["repository"])

    def test_manifest_fallback_requires_complete_source_metadata(self):
        plain = pathlib.Path(self.temp_dir.name) / "source-archive"
        plain.mkdir()
        missing = identity(plain)
        self.assertNotEqual(0, missing.returncode)
        self.assertIn("build-provenance.json", missing.stderr)

        tree = "a" * 40
        manifest = {
            "schemaVersion": 1,
            "repository": "TotalCross/totalcross",
            "source": {"commit": "b" * 40, "tree": tree, "dirty": False},
        }
        (plain / "build-provenance.json").write_text(json.dumps(manifest), encoding="utf-8")
        result = identity(plain)
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual(HELPER.compute_build_id("totalcross/totalcross", tree),
                         json.loads(result.stdout)["build"]["id"])

        del manifest["source"]["tree"]
        (plain / "build-provenance.json").write_text(json.dumps(manifest), encoding="utf-8")
        invalid = identity(plain)
        self.assertNotEqual(0, invalid.returncode)
        self.assertIn("source.tree", invalid.stderr)

    def test_manifest_update_does_not_hash_its_own_build_metadata(self):
        manifest_path = self.root / "build-provenance.json"
        manifest_path.write_text(json.dumps({
            "schemaVersion": 1,
            "repository": "TotalCross/totalcross",
            "source": {
                "commit": git(self.root, "rev-parse", "HEAD"),
                "tree": git(self.root, "rev-parse", "HEAD^{tree}"),
                "dirty": False,
            },
            "producer": {"runId": "1", "runAttempt": "1"},
        }), encoding="utf-8")
        first = identity(self.root, "--update-manifest", str(manifest_path), "--format", "json")
        self.assertEqual(0, first.returncode, first.stderr)
        first_id = json.loads(first.stdout)["build"]["id"]
        second = identity(self.root, "--update-manifest", str(manifest_path), "--format", "json")
        self.assertEqual(0, second.returncode, second.stderr)
        self.assertEqual(first_id, json.loads(second.stdout)["build"]["id"])

    def test_c_header_is_rewritten_only_when_identity_changes(self):
        header = self.root / "build" / "generated" / "tc_build_identity.h"
        arguments = ("--format", "c-header", "--output", str(header))
        first = identity(self.root, *arguments)
        self.assertEqual(0, first.returncode, first.stderr)
        header_before = header.read_text(encoding="utf-8")
        mtime_before = header.stat().st_mtime_ns
        again = identity(self.root, *arguments)
        self.assertEqual(0, again.returncode, again.stderr)
        self.assertEqual(mtime_before, header.stat().st_mtime_ns)

        (self.root / "source.txt").write_text("updated source\n", encoding="utf-8")
        changed = identity(self.root, *arguments)
        self.assertEqual(0, changed.returncode, changed.stderr)
        header_after = header.read_text(encoding="utf-8")
        self.assertNotEqual(header_before, header_after)
        self.assertIn("#define TC_BUILD_ID \"", header_after)
        self.assertIn("#define TC_SOURCE_TREE \"", header_after)


if __name__ == "__main__":
    unittest.main()
