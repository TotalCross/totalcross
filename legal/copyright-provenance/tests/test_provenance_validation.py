# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only
"""Regression tests for immutable provenance evidence validation."""
from __future__ import annotations

import datetime as dt
import json
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

sys.dont_write_bytecode = True

REPOSITORY_ROOT = Path(__file__).resolve().parents[3]
PROVENANCE_DIR = REPOSITORY_ROOT / "legal" / "copyright-provenance"
VALIDATOR = REPOSITORY_ROOT / "scripts" / "validate-copyright-headers.sh"
if str(PROVENANCE_DIR) not in sys.path:
    sys.path.insert(0, str(PROVENANCE_DIR))

from provenance import java_fingerprint  # noqa: E402

AUDIT_ID = "test-missing-history"
MISSING_REVISION = "4672da9b98ae3196cf49d8b410b14170ef6f1877"
SOURCE_PATH = "src/Historical.java"
TARGET_PATH = "src/Target.java"
SOURCE_NOTICE = {"years": "2026", "owner": "Amalgam Solucoes em TI Ltda"}


class ProvenanceValidationTest(unittest.TestCase):
    def setUp(self) -> None:
        self.temporary_directory = tempfile.TemporaryDirectory()
        self.root = Path(self.temporary_directory.name)
        self.git("init", "-q")
        self.git("config", "user.name", "Provenance Test")
        self.git("config", "user.email", "provenance@example.com")

    def tearDown(self) -> None:
        self.temporary_directory.cleanup()

    def git(self, *args: str) -> str:
        return subprocess.run(
            ["git", *args], cwd=self.root, check=True,
            stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True,
        ).stdout.strip()

    def write_json(self, path: Path, value: dict[str, object]) -> None:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(value, indent=2) + "\n", encoding="utf-8")

    def target_text(self) -> str:
        current_year = dt.date.today().year
        years = "2026" if current_year == 2026 else f"2026-{current_year}"
        return (
            f"// Copyright (C) {years} Amalgam Solucoes em TI Ltda\n"
            "//\n"
            "// SPDX-License-Identifier: LGPL-2.1-only\n"
            "\n"
            "final class Target {}\n"
        )

    def create_audit(self, initial_revision: str = MISSING_REVISION,
                     source_blob: str = "a" * 40) -> Path:
        target = self.root / TARGET_PATH
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(self.target_text(), encoding="utf-8")
        audit_dir = self.root / "legal" / "copyright-provenance" / "audits" / AUDIT_ID
        evidence_ref = "evidence/source.json"
        self.write_json(audit_dir / "manifest.json", {
            "schemaVersion": 4,
            "auditId": AUDIT_ID,
            "reviewStatus": "approved",
            "initialRevision": initial_revision,
            "results": [{
                "sourcePath": SOURCE_PATH,
                "sourceBlob": source_blob,
                "sourceCodeFingerprint": "b" * 64,
                "evidence": evidence_ref,
                "finalTargets": [{
                    "path": TARGET_PATH,
                    "codeFingerprint": java_fingerprint(self.target_text()),
                    "classification": "inherited",
                }],
            }],
        })
        evidence_path = audit_dir / evidence_ref
        self.write_json(evidence_path, {
            "schemaVersion": 4,
            "source": {
                "commit": initial_revision,
                "path": SOURCE_PATH,
                "blob": source_blob,
            },
            "sourceCodeFingerprint": "b" * 64,
            "sourceHeader": {"sourceNotices": [SOURCE_NOTICE]},
        })
        self.write_json(
            self.root / "legal" / "copyright-provenance" / "active-audits.json",
            {"schemaVersion": 1, "active": [f"audits/{AUDIT_ID}/manifest.json"]},
        )
        return evidence_path

    def validate(self, strict: bool = False) -> subprocess.CompletedProcess[str]:
        command = [sys.executable, str(VALIDATOR), "--audit-id", AUDIT_ID]
        if strict:
            command.append("--require-provenance-snapshots")
        command.extend(["--files", TARGET_PATH])
        return subprocess.run(
            command, cwd=self.root, check=False,
            stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True,
        )

    def test_ordinary_validation_uses_evidence_when_history_is_missing(self) -> None:
        self.create_audit()

        result = self.validate()

        self.assertEqual(0, result.returncode, result.stderr)
        self.assertIn("Copyright headers validated: 1 file(s)", result.stdout)
        self.assertNotIn(MISSING_REVISION, result.stderr)

    def test_strict_validation_requires_missing_historical_commit(self) -> None:
        self.create_audit()

        result = self.validate(strict=True)

        self.assertEqual(2, result.returncode)
        self.assertIn(f"cannot resolve commit: {MISSING_REVISION}", result.stderr)

    def test_strict_validation_preserves_resolvable_snapshot_checks(self) -> None:
        source = self.root / SOURCE_PATH
        source.parent.mkdir(parents=True, exist_ok=True)
        source.write_text(
            "// Copyright (C) 2026 Amalgam Solucoes em TI Ltda\n"
            "//\n"
            "// SPDX-License-Identifier: LGPL-2.1-only\n\n"
            "final class Historical {}\n",
            encoding="utf-8",
        )
        self.git("add", SOURCE_PATH)
        self.git("commit", "-q", "-m", "test: add historical source")
        initial_revision = self.git("rev-parse", "HEAD")
        source_blob = self.git("rev-parse", f"HEAD:{SOURCE_PATH}")
        self.create_audit(initial_revision, source_blob)

        result = self.validate(strict=True)

        self.assertEqual(0, result.returncode, result.stderr)

    def test_evidence_source_mismatch_is_not_ignored(self) -> None:
        evidence_path = self.create_audit()
        evidence = json.loads(evidence_path.read_text(encoding="utf-8"))
        evidence["source"]["path"] = "src/Unrelated.java"
        self.write_json(evidence_path, evidence)

        result = self.validate()

        self.assertEqual(2, result.returncode)
        self.assertIn("evidence source path mismatch", result.stderr)

    def test_malformed_recorded_notices_are_rejected(self) -> None:
        evidence_path = self.create_audit()
        evidence = json.loads(evidence_path.read_text(encoding="utf-8"))
        evidence["sourceHeader"]["sourceNotices"] = [{"years": "not-a-year", "owner": "Owner"}]
        self.write_json(evidence_path, evidence)

        result = self.validate()

        self.assertEqual(2, result.returncode)
        self.assertIn("invalid source notice years", result.stderr)


if __name__ == "__main__":
    unittest.main()
