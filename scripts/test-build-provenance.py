#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only
"""Tests for build provenance validation and downloaded-artifact selection."""

import json
import pathlib
import subprocess
import sys
import tarfile
import tempfile
import unittest


ROOT = pathlib.Path(__file__).resolve().parents[1]
VALIDATOR = ROOT / "scripts" / "validate-build-provenance.py"
PACKAGE_WORKFLOW = ROOT / ".github" / "workflows" / "package.yml"
REPOSITORY = "TotalCross/totalcross"
SOURCE_COMMIT = "a" * 40
SOURCE_TREE = "b" * 40


def manifest(repository=REPOSITORY, commit=SOURCE_COMMIT, tree=SOURCE_TREE, dirty=False):
    return {
        "schemaVersion": 1,
        "repository": repository,
        "source": {"commit": commit, "tree": tree, "dirty": dirty},
        "producer": {"runId": "123", "runAttempt": "1"},
    }


class BuildProvenanceTests(unittest.TestCase):
    def setUp(self):
        self.temp_dir = tempfile.TemporaryDirectory()
        self.root = pathlib.Path(self.temp_dir.name)

    def tearDown(self):
        self.temp_dir.cleanup()

    def write_artifact(self, name, value=None):
        artifact = self.root / name
        artifact.mkdir(parents=True, exist_ok=True)
        if value is not None:
            (artifact / "build-provenance.json").write_text(json.dumps(value), encoding="utf-8")
        return artifact

    def run_validator(self, artifacts):
        command = [
            sys.executable,
            str(VALIDATOR),
            "--repository",
            REPOSITORY,
            "--source-commit",
            SOURCE_COMMIT,
            "--source-tree",
            SOURCE_TREE,
            "--require-clean-source",
        ]
        for name, outcome, artifact in artifacts:
            command.extend(("--artifact-download", f"{name}={outcome}={artifact}"))
        return subprocess.run(command, text=True, capture_output=True, check=False)

    def test_valid_manifest(self):
        sdk = self.write_artifact("sdk", manifest())
        result = self.run_validator([("sdk", "success", sdk)])
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertIn("validated build provenance", result.stdout)

    def test_manifest_extracted_from_sdk_tar_is_selected(self):
        source = self.write_artifact("sdk-tar-source", manifest())
        archive_path = self.root / "sdk-build.tar.gz"
        with tarfile.open(archive_path, "w:gz") as archive:
            archive.add(source / "build-provenance.json", arcname="build-provenance.json")
        extracted = self.root / "sdk-extracted"
        extracted.mkdir()
        with tarfile.open(archive_path, "r:gz") as archive:
            archive.extractall(extracted)

        result = self.run_validator([("sdk", "success", extracted)])
        self.assertEqual(0, result.returncode, result.stderr)

    def test_repository_mismatch_is_rejected(self):
        sdk = self.write_artifact("sdk", manifest(repository="other/repo"))
        result = self.run_validator([("sdk", "success", sdk)])
        self.assertNotEqual(0, result.returncode)
        self.assertIn("repository", result.stderr)

    def test_commit_mismatch_is_rejected(self):
        sdk = self.write_artifact("sdk", manifest(commit="c" * 40))
        result = self.run_validator([("sdk", "success", sdk)])
        self.assertNotEqual(0, result.returncode)
        self.assertIn("source.commit", result.stderr)

    def test_tree_mismatch_is_rejected(self):
        sdk = self.write_artifact("sdk", manifest(tree="d" * 40))
        result = self.run_validator([("sdk", "success", sdk)])
        self.assertNotEqual(0, result.returncode)
        self.assertIn("source.tree", result.stderr)

    def test_dirty_source_is_rejected_for_official_build(self):
        sdk = self.write_artifact("sdk", manifest(dirty=True))
        result = self.run_validator([("sdk", "success", sdk)])
        self.assertNotEqual(0, result.returncode)
        self.assertIn("source.dirty", result.stderr)

    def test_downloaded_artifact_without_manifest_is_rejected(self):
        sdk = self.write_artifact("sdk", manifest())
        linux = self.write_artifact("linux-amd64")
        result = self.run_validator(
            [("sdk", "success", sdk), ("linux-amd64", "success", linux)]
        )
        self.assertNotEqual(0, result.returncode)
        self.assertIn("downloaded artifact 'linux-amd64' is missing required", result.stderr)

    def test_absent_optional_artifact_is_ignored(self):
        sdk = self.write_artifact("sdk", manifest())
        missing_android = self.root / "android-not-downloaded"
        result = self.run_validator(
            [("sdk", "success", sdk), ("android", "failure", missing_android)]
        )
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual(1, result.stdout.count("validated build provenance"))

    def test_artifacts_from_different_revisions_are_rejected(self):
        sdk = self.write_artifact("sdk", manifest())
        linux = self.write_artifact("linux-amd64", manifest(commit="e" * 40))
        result = self.run_validator(
            [("sdk", "success", sdk), ("linux-amd64", "success", linux)]
        )
        self.assertNotEqual(0, result.returncode)
        self.assertIn("source.commit", result.stderr)

    def test_required_sdk_artifact_must_be_selected(self):
        optional = self.write_artifact("linux-amd64", manifest())
        result = self.run_validator([("linux-amd64", "success", optional)])
        self.assertNotEqual(0, result.returncode)
        self.assertIn("required SDK build artifact was not downloaded", result.stderr)

    def test_package_workflow_uses_download_outcomes_for_optional_artifacts(self):
        workflow = PACKAGE_WORKFLOW.read_text(encoding="utf-8")
        artifacts = (
            ("linux-amd64", "download_linux_amd64", "LINUX_AMD64", "linux-amd64"),
            ("linux-arm32v7", "download_linux_arm32", "LINUX_ARM32", "linux-arm32v7"),
            ("linux-arm64", "download_linux_arm64", "LINUX_ARM64", "linux-arm64"),
            ("macos-arm64", "download_macos_arm64", "MACOS_ARM64", "macos-arm64"),
            ("windows", "download_windows", "WINDOWS", "windows"),
            ("android", "download_android", "ANDROID", "android"),
            ("ios", "download_ios", "IOS", "ios"),
        )
        for name, step_id, env_name, root in artifacts:
            with self.subTest(artifact=name):
                self.assertIn(f"id: {step_id}", workflow)
                self.assertIn(
                    f"steps.{step_id}.outcome == 'success'", workflow
                )
                self.assertIn(f"{env_name}_DOWNLOAD_OUTCOME:", workflow)
                self.assertIn(
                    f'--artifact-download "{name}=${{{env_name}_DOWNLOAD_OUTCOME}}={root}"',
                    workflow,
                )
        self.assertIn('--artifact-download "sdk=success=."', workflow)
        self.assertIn("if: ${{ always() }}", workflow)

    def test_package_workflow_finds_payloads_inside_downloaded_artifacts(self):
        workflow = PACKAGE_WORKFLOW.read_text(encoding="utf-8")
        for directory, payload in (
            ("linux-amd64", "libtcvm.so"),
            ("linux-arm32v7", "libtcvm.so"),
            ("linux-arm64", "libtcvm.so"),
            ("macos-arm64", "libtcvm.dylib"),
            ("windows", "tcvm.dll"),
            ("android", "TotalCross.aab"),
        ):
            with self.subTest(artifact=directory):
                self.assertIn(f"find {directory}", workflow)
                self.assertIn(payload, workflow)


if __name__ == "__main__":
    unittest.main()
