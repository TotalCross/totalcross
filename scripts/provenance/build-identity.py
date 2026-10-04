#!/usr/bin/env python3
#
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

import argparse
import hashlib
import json
import os
from pathlib import Path
import subprocess
import tempfile

IDENTITY_VERSION = 1


def run_git(root, *args, env=None):
    result = subprocess.run(
        ["git", *args],
        cwd=root,
        env=env,
        check=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
    )
    return result.stdout.strip()


def normalize_repository(value):
    if not value:
        return None
    value = value.strip()
    if value.endswith(".git"):
        value = value[:-4]
    if value.startswith("git@github.com:"):
        return value.split(":", 1)[1]
    marker = "github.com/"
    if marker in value:
        return value.split(marker, 1)[1].strip("/")
    return value


def git_source_identity(root):
    try:
        commit = run_git(root, "rev-parse", "HEAD")
    except (OSError, subprocess.CalledProcessError):
        return None

    status = run_git(root, "status", "--porcelain=v1", "--untracked-files=all")
    dirty = bool(status)
    if not dirty:
        tree = run_git(root, "rev-parse", "HEAD^{tree}")
    else:
        fd, index_path = tempfile.mkstemp(prefix="totalcross-provenance-index-")
        os.close(fd)
        os.unlink(index_path)
        try:
            env = os.environ.copy()
            env["GIT_INDEX_FILE"] = index_path
            run_git(root, "read-tree", "HEAD", env=env)
            run_git(root, "add", "-A", "--", ".", env=env)
            tree = run_git(root, "write-tree", env=env)
        finally:
            try:
                os.unlink(index_path)
            except FileNotFoundError:
                pass

    repository = normalize_repository(os.environ.get("GITHUB_REPOSITORY"))
    if repository is None:
        try:
            repository = normalize_repository(
                run_git(root, "config", "--get", "remote.origin.url")
            )
        except subprocess.CalledProcessError:
            repository = root.name

    return {
        "repository": repository,
        "source": {
            "commit": commit,
            "tree": tree,
            "dirty": dirty,
        },
    }


def manifest_source_identity(root):
    path = root / "build-provenance.json"
    if not path.is_file():
        return None
    value = json.loads(path.read_text(encoding="utf-8"))
    source = value.get("source")
    if not isinstance(source, dict):
        raise ValueError(f"{path} does not contain a source object")
    for key in ("commit", "tree", "dirty"):
        if key not in source:
            raise ValueError(f"{path} is missing source.{key}")
    return {
        "repository": value.get("repository"),
        "source": {
            "commit": source["commit"],
            "tree": source["tree"],
            "dirty": source["dirty"],
        },
    }


def identity(root):
    value = git_source_identity(root)
    if value is None:
        value = manifest_source_identity(root)
    if value is None:
        raise RuntimeError(
            "unable to determine source identity: no Git worktree or build-provenance.json"
        )

    canonical = json.dumps(
        {
            "identityVersion": IDENTITY_VERSION,
            "repository": value["repository"],
            "sourceTree": value["source"]["tree"],
        },
        sort_keys=True,
        separators=(",", ":"),
    ).encode("utf-8")
    value["build"] = {
        "identityVersion": IDENTITY_VERSION,
        "id": hashlib.sha256(canonical).hexdigest(),
    }
    return value


def update_manifest(path, value):
    manifest = json.loads(path.read_text(encoding="utf-8"))
    if manifest.get("repository") != value["repository"]:
        raise ValueError("manifest repository does not match current source identity")
    if manifest.get("source") != value["source"]:
        raise ValueError("manifest source does not match current source identity")
    manifest["build"] = value["build"]
    path.write_text(
        json.dumps(manifest, sort_keys=True) + "\n",
        encoding="utf-8",
    )


def main():
    parser = argparse.ArgumentParser(
        description="Compute the TotalCross content-derived build identity."
    )
    parser.add_argument(
        "--repository-root",
        type=Path,
        default=Path(__file__).resolve().parents[2],
    )
    parser.add_argument(
        "--format",
        choices=("json", "id", "source-tree", "properties"),
        default="json",
    )
    parser.add_argument("--update-manifest", type=Path)
    args = parser.parse_args()

    root = args.repository_root.resolve()
    value = identity(root)

    if args.update_manifest is not None:
        update_manifest(args.update_manifest, value)

    if args.format == "id":
        print(value["build"]["id"])
    elif args.format == "source-tree":
        print(value["source"]["tree"])
    elif args.format == "properties":
        print(f"identityVersion={value['build']['identityVersion']}")
        print(f"repository={value['repository']}")
        print(f"sourceCommit={value['source']['commit']}")
        print(f"sourceTree={value['source']['tree']}")
        print(f"sourceDirty={str(value['source']['dirty']).lower()}")
        print(f"buildId={value['build']['id']}")
    else:
        print(json.dumps(value, sort_keys=True))


if __name__ == "__main__":
    main()
