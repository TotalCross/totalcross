#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only
"""Package the exact marker-selected native dependency tree for downstream CI."""

import argparse
import json
import pathlib
import shutil
import subprocess
import tarfile
import tempfile


ARCHIVE_PREFIX = pathlib.Path("TotalCrossVM/deps/totalcross-depot-tools")


def copy_tree(source, destination, ignore=None):
    shutil.copytree(str(source), str(destination), dirs_exist_ok=True, ignore=ignore)


def package(depot, index_path, output):
    depot = depot.resolve()
    entries = [json.loads(line) for line in index_path.read_text().splitlines() if line.strip()]
    if not entries:
        raise ValueError("prepared dependency index is empty")
    revision = subprocess.check_output(["git", "-C", str(depot), "rev-parse", "HEAD"], text=True).strip()
    with tempfile.TemporaryDirectory() as directory:
        staging = pathlib.Path(directory) / ARCHIVE_PREFIX
        staging.mkdir(parents=True)
        process = subprocess.Popen(["git", "-C", str(depot), "archive", "HEAD"], stdout=subprocess.PIPE)
        assert process.stdout is not None
        with tarfile.open(fileobj=process.stdout, mode="r|") as archive:
            archive.extractall(str(staging))
        process.stdout.close()
        if process.wait() != 0:
            raise subprocess.CalledProcessError(process.returncode, process.args)

        copied = set()
        for entry in entries:
            marker = depot / entry["marker_path"]
            if not marker.is_file():
                raise ValueError("indexed marker is missing: %s" % marker)
            marker_payload = json.loads(marker.read_text())
            for key in ("schema", "dependency", "repository", "release_tag", "asset", "sha256"):
                if marker_payload.get(key) != entry.get(key):
                    raise ValueError("indexed marker changed: %s" % marker)
            source_root = marker.parent
            relative_root = source_root.relative_to(depot)
            destination_root = staging / relative_root
            if entry["dependency"] == "skia-shared":
                copy_tree(source_root, destination_root, ignore=shutil.ignore_patterns("out"))
            elif relative_root not in copied:
                copy_tree(source_root, destination_root)
            copied.add(relative_root)

        manifest = {
            "schema": 1,
            "depot_tools_revision": revision,
            "dependencies": entries,
        }
        (staging / "prepared-native-dependencies.json").write_text(
            json.dumps(manifest, indent=2, sort_keys=True) + "\n"
        )
        output.parent.mkdir(parents=True, exist_ok=True)
        with tarfile.open(str(output), "w:gz") as archive:
            archive.add(str(pathlib.Path(directory) / "TotalCrossVM"), arcname="TotalCrossVM")
    return {"revision": revision, "entries": len(entries), "bytes": output.stat().st_size, "output": str(output)}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--depot-dir", type=pathlib.Path, required=True)
    parser.add_argument("--index", type=pathlib.Path, required=True)
    parser.add_argument("--output", type=pathlib.Path, required=True)
    args = parser.parse_args()
    try:
        print(json.dumps(package(args.depot_dir, args.index, args.output), sort_keys=True))
    except (OSError, ValueError, subprocess.CalledProcessError, tarfile.TarError) as error:
        parser.exit(1, "package-native-dependencies: %s\n" % error)


if __name__ == "__main__":
    main()
