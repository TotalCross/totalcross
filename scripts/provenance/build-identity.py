#!/usr/bin/env python3
#
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

import argparse
import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile

sys.dont_write_bytecode = True

from build_identity import (
    IDENTITY_VERSION,
    compute_build_id,
    is_git_object_id,
    normalize_repository,
)


def run_git(root, *args, env=None):
    result = subprocess.run(
        ["git", *args],
        cwd=root,
        env=env,
        check=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        universal_newlines=True,
    )
    return result.stdout.strip()


def git_source_identity(root):
    try:
        commit = run_git(root, "rev-parse", "HEAD")
    except (OSError, subprocess.CalledProcessError):
        return None

    source_pathspecs = [".", ":(exclude)build-provenance.json"]
    status = run_git(
        root,
        "status",
        "--porcelain=v1",
        "--untracked-files=all",
        "--",
        *source_pathspecs,
    )
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
            run_git(root, "add", "-A", "--", *source_pathspecs, env=env)
            tree = run_git(root, "write-tree", env=env)
        finally:
            try:
                os.unlink(index_path)
            except FileNotFoundError:
                pass

    repository_value = os.environ.get("GITHUB_REPOSITORY")
    if not repository_value:
        try:
            repository_value = run_git(root, "config", "--get", "remote.origin.url")
        except subprocess.CalledProcessError:
            repository_value = None
    repository = normalize_repository(repository_value)
    if repository is None:
        raise RuntimeError(
            "unable to determine repository identity: set GITHUB_REPOSITORY or "
            "configure remote.origin.url"
        )

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
    if not isinstance(value, dict):
        raise ValueError(f"{path} does not contain a JSON object")
    source = value.get("source")
    if not isinstance(source, dict):
        raise ValueError(f"{path} does not contain a source object")
    repository = normalize_repository(value.get("repository"))
    if repository is None:
        raise ValueError(f"{path} does not contain a repository identity")
    if not is_git_object_id(source.get("commit")):
        raise ValueError(f"{path} is missing source.commit")
    if not is_git_object_id(source.get("tree")):
        raise ValueError(f"{path} is missing source.tree")
    if not isinstance(source.get("dirty"), bool):
        raise ValueError(f"{path} is missing boolean source.dirty")
    return {
        "repository": repository,
        "source": {
            "commit": source["commit"],
            "tree": source["tree"],
            "dirty": source["dirty"],
        },
    }


def build_identity(value):
    value["build"] = {
        "identityVersion": IDENTITY_VERSION,
        "id": compute_build_id(value["repository"], value["source"]["tree"]),
    }
    return value


def identity(root):
    value = git_source_identity(root)
    if value is None:
        value = manifest_source_identity(root)
    if value is None:
        raise RuntimeError(
            "unable to determine source identity: no Git worktree or build-provenance.json"
        )
    return build_identity(value)


def identity_from_manifest(path):
    manifest = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(manifest, dict):
        raise ValueError(f"{path} does not contain a JSON object")
    source = manifest.get("source")
    if not isinstance(source, dict):
        raise ValueError(f"{path} does not contain a source object")
    repository = normalize_repository(manifest.get("repository"))
    if repository is None:
        raise ValueError(f"{path} does not contain a repository identity")
    if not is_git_object_id(source.get("commit")):
        raise ValueError(f"{path} is missing source.commit")
    if not is_git_object_id(source.get("tree")):
        raise ValueError(f"{path} is missing source.tree")
    if not isinstance(source.get("dirty"), bool):
        raise ValueError(f"{path} is missing boolean source.dirty")
    value = {
        "repository": repository,
        "source": {
            "commit": source.get("commit"),
            "tree": source.get("tree"),
            "dirty": source.get("dirty"),
        },
    }
    return build_identity(value)


def update_manifest(path, value):
    manifest = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(manifest, dict):
        raise ValueError(f"{path} does not contain a JSON object")
    if normalize_repository(manifest.get("repository")) != value["repository"]:
        raise ValueError("manifest repository does not match current source identity")
    if manifest.get("source") != value["source"]:
        raise ValueError("manifest source does not match current source identity")
    manifest["build"] = value["build"]
    path.write_text(
        json.dumps(manifest, sort_keys=True) + "\n",
        encoding="utf-8",
    )


def write_identity_header(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    content = (
        "#ifndef TOTALCROSS_BUILD_IDENTITY_GENERATED_H\n"
        "#define TOTALCROSS_BUILD_IDENTITY_GENERATED_H\n"
        f'#define TC_BUILD_ID "{value["build"]["id"]}"\n'
        f'#define TC_SOURCE_TREE "{value["source"]["tree"]}"\n'
        "#endif\n"
    )
    if path.is_file() and path.read_text(encoding="utf-8") == content:
        return

    fd, temporary_path = tempfile.mkstemp(prefix=f".{path.name}.", dir=path.parent)
    try:
        with os.fdopen(fd, "w", encoding="utf-8", newline="\n") as output:
            output.write(content)
        os.replace(temporary_path, path)
    finally:
        try:
            os.unlink(temporary_path)
        except FileNotFoundError:
            pass


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
        choices=("json", "id", "source-tree", "properties", "c-header"),
        default="json",
    )
    parser.add_argument("--update-manifest", type=Path)
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()

    root = args.repository_root.resolve()
    if args.update_manifest is not None:
        value = identity_from_manifest(args.update_manifest)
        update_manifest(args.update_manifest, value)
    else:
        value = identity(root)

    if args.format == "c-header":
        if args.output is None:
            parser.error("--format c-header requires --output")
        write_identity_header(args.output, value)
    elif args.format == "id":
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
