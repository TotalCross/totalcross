#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only
"""Approve or reject a provenance audit and record the decision atomically."""
from __future__ import annotations

import argparse
import dataclasses
import datetime as dt
import os
import subprocess
import sys
from pathlib import Path

sys.dont_write_bytecode = True

SCRIPT_DIR = Path(__file__).resolve().parent
if str(SCRIPT_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPT_DIR))

from provenance import (  # noqa: E402
    ACTIVE_AUDITS,
    ProvenanceError,
    active_refs,
    git,
    manifest_path,
    read_json,
    relative_manifest,
    repo_root,
    set_active_refs,
    sha256_file,
    write_json,
)

VALIDATOR = Path("scripts/validate-copyright-headers.sh")
AUDIT_TOOL = Path("legal/copyright-provenance/audit-code-provenance.py")
REVIEW_TOOL = Path("legal/copyright-provenance/review-audit.py")
COMMON_TOOL = Path("legal/copyright-provenance/provenance.py")


class ReviewError(RuntimeError):
    pass


@dataclasses.dataclass(frozen=True)
class Snapshot:
    path: Path
    existed: bool
    content: bytes

    @classmethod
    def capture(cls, path: Path) -> "Snapshot":
        return cls(path, path.exists(), path.read_bytes() if path.exists() else b"")

    def restore(self) -> None:
        if self.existed:
            self.path.parent.mkdir(parents=True, exist_ok=True)
            self.path.write_bytes(self.content)
        elif self.path.exists():
            self.path.unlink()


def run(command: list[str], check: bool = True, capture: bool = False) -> subprocess.CompletedProcess[str]:
    result = subprocess.run(
        command, check=False, text=True,
        stdout=subprocess.PIPE if capture else None,
        stderr=subprocess.PIPE if capture else None,
        encoding="utf-8", errors="replace",
    )
    if check and result.returncode:
        detail = (result.stderr or result.stdout or "").strip()
        raise ReviewError(detail or f"command failed: {' '.join(command)}")
    return result


def ask_decision(audit_id: str) -> str:
    prompt = f"Review audit {audit_id}: [a]pprove, [r]eject, [c]ancel? "
    while True:
        try:
            value = input(prompt).strip().lower()
        except EOFError as exc:
            raise ReviewError("review cancelled: no interactive input") from exc
        if value in {"a", "approve", "aprovado", "aprovar"}:
            return "approved"
        if value in {"r", "reject", "rejected", "reprovar", "reprovado"}:
            return "rejected"
        if value in {"c", "cancel", "cancelar", ""}:
            raise ReviewError("review cancelled")
        print("Enter a, r, or c.")


def ask_activate() -> bool:
    prompt = "Add this audit to active-audits.json without removing existing entries? [y/N] "
    try:
        return input(prompt).strip().lower() in {"y", "yes", "s", "sim"}
    except EOFError as exc:
        raise ReviewError("review cancelled: no activation answer") from exc


def git_identity() -> dict[str, str]:
    return {
        "name": git(["config", "user.name"], False).strip(),
        "email": git(["config", "user.email"], False).strip(),
    }


def ensure_no_staged_changes() -> None:
    staged = git(["diff", "--cached", "--name-only"]).splitlines()
    if staged:
        raise ReviewError(
            "the index must be empty before review; staged paths: " + ", ".join(staged)
        )


def ensure_framework_committed(root: Path) -> None:
    for path in (AUDIT_TOOL, REVIEW_TOOL, COMMON_TOOL, VALIDATOR):
        if not (root / path).is_file():
            raise ReviewError(f"required tool is missing: {path}")
        if run(["git", "ls-files", "--error-unmatch", str(path)], False, True).returncode:
            raise ReviewError(f"commit the review framework before using it: {path}")
        if run(["git", "diff", "--quiet", "--", str(path)], False).returncode:
            raise ReviewError(f"tool has uncommitted changes: {path}")
        if run(["git", "diff", "--cached", "--quiet", "--", str(path)], False).returncode:
            raise ReviewError(f"tool has staged changes: {path}")


def material_targets(manifest: dict[str, object]) -> list[str]:
    targets: list[str] = []
    for result in manifest.get("results", []):
        if not isinstance(result, dict):
            raise ReviewError("manifest results must contain objects")
        for target in result.get("finalTargets", []):
            if not isinstance(target, dict):
                raise ReviewError("manifest finalTargets must contain objects")
            if target.get("classification") not in {"inherited", "partial-inherited"}:
                continue
            path = target.get("path")
            if not isinstance(path, str):
                raise ReviewError("manifest target is missing path")
            targets.append(path)
    return sorted(set(targets))


def verify_tool_hash(root: Path, manifest: dict[str, object]) -> None:
    tool = manifest.get("tool")
    if not isinstance(tool, dict):
        raise ReviewError("manifest is missing tool metadata")
    path_value, expected = tool.get("path"), tool.get("sha256")
    if not isinstance(path_value, str) or not isinstance(expected, str):
        raise ReviewError("manifest tool metadata is incomplete")
    path = root / path_value
    if not path.is_file():
        raise ReviewError(f"auditing tool does not exist: {path_value}")
    actual = sha256_file(path)
    if actual != expected:
        raise ReviewError(
            f"auditing tool hash mismatch for {path_value}\n"
            f"expected: {expected}\nactual:   {actual}"
        )
    print(f"Verified audit tool SHA-256: {actual}", flush=True)


def validate_audit_directory(audit_dir: Path) -> None:
    if not audit_dir.is_dir():
        raise ReviewError(f"audit directory does not exist: {audit_dir}")
    forbidden = [path for path in audit_dir.rglob("*") if path.name == "__MACOSX" or path.name.startswith("._")]
    if forbidden:
        raise ReviewError("remove macOS metadata before review: " + ", ".join(map(str, forbidden)))


def update_review(manifest: dict[str, object], decision: str) -> None:
    manifest["reviewStatus"] = decision
    manifest["reviewedAt"] = dt.datetime.now().astimezone().isoformat()
    manifest["reviewedBy"] = git_identity()


def validator_command(audit_id: str, targets: list[str], fix: bool) -> list[str]:
    command = [sys.executable, str(VALIDATOR), "--audit-id", audit_id]
    if fix:
        command.append("--fix")
    command.extend(["--files", *targets])
    return command


def stage_and_commit(root: Path, allowed: list[str], subject: str,
                     audit_id: str, sign: bool) -> None:
    run(["git", "add", "--", *allowed])
    staged = set(git(["diff", "--cached", "--name-only"]).splitlines())
    def permitted(path: str) -> bool:
        return any(path == item or path.startswith(item.rstrip("/") + "/") for item in allowed)
    unexpected = {path for path in staged if not permitted(path)}
    if unexpected:
        raise ReviewError("unexpected staged paths: " + ", ".join(sorted(unexpected)))
    if not staged:
        raise ReviewError("review produced no changes to commit")
    run(["git", "diff", "--cached", "--check"])
    command = ["git", "commit"]
    if sign:
        command.append("-S")
    command.extend(["-m", subject, "-m", f"Audit-ID: {audit_id}"])
    run(command)


def reset_paths(paths: list[str]) -> None:
    if paths:
        run(["git", "restore", "--staged", "--", *paths], False)


def main() -> int:
    parser = argparse.ArgumentParser(description="Approve or reject a provenance audit.")
    parser.add_argument("audit_id", help="directory name under legal/copyright-provenance/audits")
    parser.add_argument("--no-sign", action="store_true", help="create an unsigned commit")
    args = parser.parse_args()

    snapshots: list[Snapshot] = []
    allowed: list[str] = []
    committed = False
    try:
        root = repo_root()
        os.chdir(root)
        ensure_no_staged_changes()
        ensure_framework_committed(root)

        manifest_file = manifest_path(root, args.audit_id)
        audit_dir = manifest_file.parent
        validate_audit_directory(audit_dir)
        manifest = read_json(manifest_file)
        if manifest.get("auditId") != args.audit_id:
            raise ReviewError("auditId in manifest does not match the requested directory")
        targets = material_targets(manifest)
        if not targets:
            raise ReviewError("audit has no inherited or partial-inherited targets")
        for path in targets:
            if not (root / path).is_file():
                raise ReviewError(f"audited target does not exist: {path}")

        decision = ask_decision(args.audit_id)
        activate = False
        if decision == "approved":
            verify_tool_hash(root, manifest)
            activate = ask_activate()

        active_file = root / ACTIVE_AUDITS
        snapshot_paths = [manifest_file, active_file]
        if decision == "approved" and activate:
            snapshot_paths.extend(root / path for path in targets)
        snapshots = [Snapshot.capture(path) for path in snapshot_paths]

        update_review(manifest, decision)
        write_json(manifest_file, manifest)
        ref = relative_manifest(args.audit_id)
        refs = active_refs(root)

        if decision == "rejected":
            removed_from_active = ref in refs
            if removed_from_active:
                set_active_refs(root, [item for item in refs if item != ref])
            allowed = [str(audit_dir.relative_to(root))]
            if ref in refs:
                allowed.append(str(ACTIVE_AUDITS))
            stage_and_commit(
                root, allowed, "docs(legal): reject copyright provenance audit",
                args.audit_id, not args.no_sign,
            )
            if removed_from_active:
                print("Removed rejected audit from active-audits.json.", flush=True)
        elif activate:
            added_to_active = ref not in refs
            if added_to_active:
                set_active_refs(root, [*refs, ref])
            run(validator_command(args.audit_id, targets, True))
            run(validator_command(args.audit_id, targets, False))
            verify_tool_hash(root, manifest)
            allowed = [
                str(audit_dir.relative_to(root)), str(ACTIVE_AUDITS), *targets,
            ]
            stage_and_commit(
                root, allowed, "fix(legal): activate copyright provenance audit",
                args.audit_id, not args.no_sign,
            )
            if added_to_active:
                print("Activated audit without removing existing active entries.", flush=True)
            else:
                print("Audit was already active.", flush=True)
        else:
            print("Audit approved but not activated; headers were not changed.", flush=True)
            allowed = [str(audit_dir.relative_to(root))]
            stage_and_commit(
                root, allowed, "docs(legal): approve copyright provenance audit",
                args.audit_id, not args.no_sign,
            )

        committed = True
        print(f"Recorded {decision} audit in one atomic commit: {args.audit_id}", flush=True)
        return 0
    except (ProvenanceError, ReviewError, OSError) as exc:
        if not committed:
            reset_paths(allowed)
            for snapshot in reversed(snapshots):
                snapshot.restore()
        print(f"Audit review failed: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())
