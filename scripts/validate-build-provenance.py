#!/usr/bin/env python3
#
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

import argparse
import json
from pathlib import Path
import sys

sys.dont_write_bytecode = True

from provenance.build_identity import compute_build_id


def parse_args():
    parser = argparse.ArgumentParser(description="Validate TotalCross build provenance.")
    parser.add_argument(
        "--manifest",
        type=Path,
        action="append",
        help="provenance manifest to validate; may be repeated",
    )
    parser.add_argument(
        "--artifact-download",
        action="append",
        default=[],
        metavar="NAME=OUTCOME=ROOT",
        help=(
            "download outcome and extraction directory for an artifact; successful "
            "downloads must contain build-provenance.json"
        ),
    )
    parser.add_argument("--repository", required=True)
    parser.add_argument("--source-commit", required=True)
    parser.add_argument("--source-tree", required=True)
    parser.add_argument("--require-clean-source", action="store_true")
    parser.add_argument("--require-build-identity", action="store_true")
    return parser.parse_args()


def select_downloaded_manifests(artifact_downloads):
    """Return manifests for successful downloads and reject incomplete artifacts."""
    manifests = []
    downloaded_names = set()
    required_sdk_downloaded = False
    valid_outcomes = {"success", "failure", "cancelled", "skipped"}

    for specification in artifact_downloads:
        parts = specification.split("=", 2)
        if len(parts) != 3 or not all(parts):
            raise ValueError(
                "invalid artifact download; expected NAME=OUTCOME=ROOT, "
                f"got {specification!r}"
            )
        name, outcome, root = parts
        if outcome not in valid_outcomes:
            raise ValueError(
                f"invalid download outcome for artifact {name!r}: {outcome!r}"
            )
        if name in downloaded_names:
            raise ValueError(f"duplicate artifact download entry: {name!r}")
        downloaded_names.add(name)

        if outcome != "success":
            continue
        if name == "sdk":
            required_sdk_downloaded = True

        manifest = Path(root) / "build-provenance.json"
        if not manifest.is_file():
            raise ValueError(
                f"downloaded artifact {name!r} is missing required provenance "
                f"manifest: {manifest}"
            )
        manifests.append(manifest)

    if "sdk" not in downloaded_names or not required_sdk_downloaded:
        raise ValueError("required SDK build artifact was not downloaded")

    return manifests


def validate_manifest(
    manifest,
    repository,
    source_commit,
    source_tree,
    require_clean_source,
    require_build_identity=False,
):
    try:
        value = json.loads(manifest.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as error:
        return [f"invalid provenance manifest {manifest}: {error}"]

    if not isinstance(value, dict):
        return [f"invalid provenance manifest {manifest}: expected a JSON object"]

    mismatches = []

    expected_top_level = {
        "schemaVersion": 1,
        "repository": repository,
    }
    mismatches.extend(
        f"{key}: expected {expected_value!r}, got {value.get(key)!r}"
        for key, expected_value in expected_top_level.items()
        if value.get(key) != expected_value
    )

    source = value.get("source")
    if not isinstance(source, dict):
        mismatches.append(f"source: expected object, got {source!r}")
    else:
        expected_source = {
            "commit": source_commit,
            "tree": source_tree,
        }
        mismatches.extend(
            f"source.{key}: expected {expected_value!r}, got {source.get(key)!r}"
            for key, expected_value in expected_source.items()
            if source.get(key) != expected_value
        )
        if require_clean_source and source.get("dirty") is not False:
            mismatches.append(
                f"source.dirty: expected False, got {source.get('dirty')!r}"
            )
        if require_build_identity:
            build = value.get("build")
            if not isinstance(build, dict):
                mismatches.append(f"build: expected object, got {build!r}")
            else:
                if (
                    type(build.get("identityVersion")) is not int
                    or build.get("identityVersion") != 1
                ):
                    mismatches.append(
                        "build.identityVersion: expected 1, "
                        f"got {build.get('identityVersion')!r}"
                    )
                expected_build_id = compute_build_id(repository, source_tree)
                if build.get("id") != expected_build_id:
                    mismatches.append(
                        f"build.id: expected {expected_build_id!r}, "
                        f"got {build.get('id')!r}"
                    )

    return mismatches


def main():
    args = parse_args()
    if args.manifest and args.artifact_download:
        print("use either --manifest or --artifact-download, not both", file=sys.stderr)
        return 2

    try:
        manifests = (
            select_downloaded_manifests(args.artifact_download)
            if args.artifact_download
            else args.manifest or []
        )
    except ValueError as error:
        print(f"build artifact provenance selection failed: {error}", file=sys.stderr)
        return 1

    if not manifests:
        print("no build provenance manifests were selected", file=sys.stderr)
        return 1

    failed = False
    for manifest in manifests:
        mismatches = validate_manifest(
            manifest,
            args.repository,
            args.source_commit,
            args.source_tree,
            args.require_clean_source,
            args.require_build_identity,
        )
        if mismatches:
            failed = True
            print(f"build provenance mismatch in {manifest}", file=sys.stderr)
            for mismatch in mismatches:
                print("  " + mismatch, file=sys.stderr)
        else:
            print(f"validated build provenance: {manifest}")

    return 1 if failed else 0


if __name__ == "__main__":
    raise SystemExit(main())
