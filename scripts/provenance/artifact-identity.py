#!/usr/bin/env python3
#
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

import argparse
import json
from pathlib import Path
import re
import struct
import sys
import zipfile
import zlib

BUILD_ID_RE = re.compile(rb"TOTALCROSS_BUILD_IDENTITY_V1:([0-9a-f]{64})")
SOURCE_TREE_RE = re.compile(rb"TOTALCROSS_SOURCE_TREE:([0-9a-f]{40,64})")
TCZ_IDENTITY_ENTRY = "META-INF/totalcross-build"


def parse_properties(data):
    result = {}
    for raw in data.decode("utf-8").splitlines():
        line = raw.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        result[key.strip()] = value.strip()
    return result


def read_manifest_attributes(data):
    attrs = {}
    current = None
    for raw in data.decode("utf-8", errors="replace").splitlines():
        if raw.startswith(" ") and current:
            attrs[current] += raw[1:]
        elif ": " in raw:
            current, value = raw.split(": ", 1)
            attrs[current] = value
        else:
            current = None
    return attrs


def jar_identity(path):
    with zipfile.ZipFile(path) as jar:
        attrs = read_manifest_attributes(jar.read("META-INF/MANIFEST.MF"))
    return {
        "kind": "jar",
        "buildId": attrs.get("TotalCross-Build-ID"),
        "sourceTree": attrs.get("TotalCross-Source-Tree"),
        "identityVersion": attrs.get("TotalCross-Build-Identity-Version"),
    }


def tcz_entries(path):
    data = path.read_bytes()
    if len(data) < 8:
        raise ValueError("TCZ is truncated")
    version, attributes, base_offset = struct.unpack_from("<HHI", data, 0)
    if base_offset < 8 or base_offset > len(data):
        raise ValueError(f"invalid TCZ base offset {base_offset}")
    header = zlib.decompress(data[8:base_offset])
    pos = 0
    count = struct.unpack_from("<I", header, pos)[0]
    pos += 4
    offsets = list(struct.unpack_from(f"<{count + 1}I", header, pos))
    pos += 4 * (count + 1)
    sizes = list(struct.unpack_from(f"<{count}I", header, pos))
    pos += 4 * count
    names = []
    for _ in range(count):
        length = header[pos]
        pos += 1
        names.append(header[pos:pos + length].decode("utf-8"))
        pos += length
    for index, name in enumerate(names):
        start = base_offset + offsets[index]
        end = base_offset + offsets[index + 1]
        yield version, attributes, name, sizes[index], zlib.decompress(data[start:end])


def tcz_identity(path):
    version = None
    for current_version, _, name, _, payload in tcz_entries(path):
        version = current_version
        if name == TCZ_IDENTITY_ENTRY:
            props = parse_properties(payload)
            return {
                "kind": "tcz",
                "tczFormat": current_version,
                "buildId": props.get("buildId"),
                "sourceTree": props.get("sourceTree"),
                "identityVersion": props.get("identityVersion"),
            }
    return {"kind": "tcz", "tczFormat": version, "buildId": None, "sourceTree": None}


def native_identity(path):
    data = path.read_bytes()
    build_match = BUILD_ID_RE.search(data)
    tree_match = SOURCE_TREE_RE.search(data)
    return {
        "kind": "native",
        "buildId": build_match.group(1).decode("ascii") if build_match else None,
        "sourceTree": tree_match.group(1).decode("ascii") if tree_match else None,
        "identityVersion": "1" if build_match else None,
    }


def artifact_identity(path):
    suffix = path.suffix.lower()
    if suffix == ".jar":
        return jar_identity(path)
    if suffix == ".tcz":
        return tcz_identity(path)
    return native_identity(path)


def main():
    parser = argparse.ArgumentParser(
        description="Read or validate embedded TotalCross artifact build identities."
    )
    parser.add_argument("artifacts", nargs="+", type=Path)
    parser.add_argument("--json", action="store_true")
    args = parser.parse_args()

    rows = []
    for path in args.artifacts:
        identity = artifact_identity(path)
        identity["path"] = str(path)
        rows.append(identity)

    missing = [row["path"] for row in rows if not row.get("buildId")]
    build_ids = {row.get("buildId") for row in rows if row.get("buildId")}
    trees = {row.get("sourceTree") for row in rows if row.get("sourceTree")}

    if args.json:
        print(json.dumps(rows, sort_keys=True))
    else:
        for row in rows:
            print(
                f"{row['path']}: build={row.get('buildId') or 'missing'} "
                f"tree={row.get('sourceTree') or 'missing'}"
            )

    if missing:
        print("missing embedded build identity: " + ", ".join(missing), file=sys.stderr)
        return 1
    if len(build_ids) != 1:
        print("mixed TotalCross Build IDs detected", file=sys.stderr)
        return 1
    if len(trees) > 1:
        print("mixed TotalCross source trees detected", file=sys.stderr)
        return 1
    print(f"coherent TotalCross build: {next(iter(build_ids))}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
