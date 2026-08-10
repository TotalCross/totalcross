# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only
"""Discover changed paths and read their exact Git snapshot contents."""
from __future__ import annotations

import dataclasses
import os
import subprocess
from pathlib import Path
from typing import Literal

ZERO_SHA = "0000000000000000000000000000000000000000"


class ChangeDiscoveryError(RuntimeError):
    """Raised when a requested Git comparison cannot be constructed."""


@dataclasses.dataclass(frozen=True)
class BlobSource:
    kind: Literal["empty", "git", "index", "working-tree"]
    revision: str | None = None

    def read(self, root: Path, path: str) -> bytes | None:
        if self.kind == "empty":
            return None
        if self.kind == "working-tree":
            file_path = root / path
            return file_path.read_bytes() if file_path.is_file() else None
        object_name = f":{path}" if self.kind == "index" else f"{self.revision}:{path}"
        result = subprocess.run(
            ["git", "cat-file", "blob", object_name], cwd=root, check=False,
            stdout=subprocess.PIPE, stderr=subprocess.DEVNULL,
        )
        return result.stdout if result.returncode == 0 else None


@dataclasses.dataclass(frozen=True)
class ChangedFile:
    status: str
    old_path: str | None
    new_path: str | None

    @property
    def current_path(self) -> str | None:
        return self.new_path


@dataclasses.dataclass(frozen=True)
class ChangeSet:
    mode: str
    base: BlobSource
    target: BlobSource
    files: tuple[ChangedFile, ...]


def _git(root: Path, args: list[str], *, check: bool = True) -> bytes:
    result = subprocess.run(
        ["git", *args], cwd=root, check=False, stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )
    if check and result.returncode:
        detail = result.stderr.decode("utf-8", "replace").strip()
        raise ChangeDiscoveryError(detail or f"git {' '.join(args)} failed")
    return result.stdout


def _text(root: Path, args: list[str], *, check: bool = True) -> str:
    return _git(root, args, check=check).decode("utf-8", "replace").strip()


def _parse_name_status(output: bytes) -> tuple[ChangedFile, ...]:
    fields = output.split(b"\0")
    if fields and not fields[-1]:
        fields.pop()
    changes: list[ChangedFile] = []
    index = 0
    while index < len(fields):
        status = fields[index].decode("ascii", "replace")
        index += 1
        kind = status[:1]
        if kind in {"R", "C"}:
            old_path = fields[index].decode("utf-8", "surrogateescape")
            new_path = fields[index + 1].decode("utf-8", "surrogateescape")
            index += 2
        else:
            path = fields[index].decode("utf-8", "surrogateescape")
            index += 1
            old_path = None if kind == "A" else path
            new_path = None if kind == "D" else path
        changes.append(ChangedFile(status, old_path, new_path))
    return tuple(changes)


def _filter(changes: tuple[ChangedFile, ...],
            files: list[str] | None) -> tuple[ChangedFile, ...]:
    if not files:
        return changes
    selected = set(files)
    return tuple(
        change for change in changes
        if change.old_path in selected or change.new_path in selected
    )


def _diff(root: Path, args: list[str], files: list[str] | None) -> tuple[ChangedFile, ...]:
    changes = _parse_name_status(_git(root, [
        "diff", "-M", "--name-status", "-z", "--diff-filter=ACMRTD",
        *args,
    ]))
    return _filter(changes, files)


def _tree(root: Path, commit: str, files: list[str] | None) -> tuple[ChangedFile, ...]:
    changes = _parse_name_status(_git(root, [
        "diff-tree", "-M", "--root", "--no-commit-id", "--name-status",
        "-z", "--diff-filter=ACMRTD", "-r", commit,
    ]))
    return _filter(changes, files)


def _head(root: Path) -> str | None:
    return _text(root, ["rev-parse", "--verify", "HEAD"], check=False) or None


def _explicit_files(root: Path, files: list[str]) -> ChangeSet:
    head = _head(root)
    base = BlobSource("git", head) if head else BlobSource("empty")
    target = BlobSource("working-tree")
    changes: list[ChangedFile] = []
    for path in files:
        existed = base.read(root, path) is not None
        exists = target.read(root, path) is not None
        if exists:
            # Preserve the legacy header validator's explicit-file status. Size
            # validation derives newness from the base blob, not this label.
            changes.append(ChangedFile("M", path, path))
        elif existed:
            changes.append(ChangedFile("D", path, None))
    return ChangeSet("files", base, target, tuple(changes))


def _staged(root: Path, files: list[str] | None) -> ChangeSet:
    head = _head(root)
    base = BlobSource("git", head) if head else BlobSource("empty")
    args = ["--cached", head] if head else ["--cached"]
    return ChangeSet("staged", base, BlobSource("index"), _diff(root, args, files))


def _working_tree(root: Path, files: list[str] | None) -> ChangeSet:
    head = _head(root)
    base = BlobSource("git", head) if head else BlobSource("empty")
    tracked = _diff(root, [head] if head else [], files)
    return ChangeSet("working-tree", base, BlobSource("working-tree"), tracked)


def _revision_range(root: Path, base: str, head: str,
                    files: list[str] | None, mode: str) -> ChangeSet:
    return ChangeSet(
        mode, BlobSource("git", base), BlobSource("git", head),
        _diff(root, [base, head], files),
    )


def discover_changes(root: Path, *, base: str | None = None,
                     head: str | None = None, commit: str | None = None,
                     files: list[str] | None = None, staged: bool = False,
                     working_tree: bool = False) -> ChangeSet:
    """Build the requested comparison without reading unrelated snapshots."""
    if staged:
        return _staged(root, files)
    if working_tree:
        return _working_tree(root, files)
    if base and head:
        return _revision_range(root, base, head, files, "base-head")
    if commit:
        parents = _text(root, ["rev-list", "--parents", "-n", "1", commit]).split()
        if len(parents) > 1:
            return _revision_range(root, parents[1], commit, files, "commit")
        return ChangeSet("commit", BlobSource("empty"), BlobSource("git", commit),
                         _tree(root, commit, files))
    if files is not None:
        return _explicit_files(root, files)

    event = os.environ.get("EVENT_NAME") or os.environ.get("GITHUB_EVENT_NAME", "")
    pr_base = os.environ.get("PR_BASE_SHA")
    pr_head = os.environ.get("PR_HEAD_SHA")
    if event == "pull_request" and pr_base and pr_head:
        merge_base = _text(root, ["merge-base", pr_base, pr_head]) or pr_base
        return _revision_range(root, merge_base, pr_head, None, "pull-request")
    push_after = os.environ.get("PUSH_AFTER")
    if push_after:
        push_before = os.environ.get("PUSH_BEFORE", "")
        if push_before and push_before != ZERO_SHA:
            return _revision_range(root, push_before, push_after, None, "push")
        return ChangeSet("push", BlobSource("empty"), BlobSource("git", push_after),
                         _tree(root, push_after, None))

    staged_changes = _staged(root, None)
    return staged_changes if staged_changes.files else _working_tree(root, None)
