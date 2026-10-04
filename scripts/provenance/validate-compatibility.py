#!/usr/bin/env python3
#
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

import argparse
from pathlib import Path
import re
import sys


def read_contract(path):
    values = {}
    for raw in path.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        if "=" not in line:
            raise ValueError(f"invalid compatibility line: {raw!r}")
        key, value = line.split("=", 1)
        values[key.strip()] = int(value.strip())
    required = {"schemaVersion", "tczFormat", "converterAbi", "runtimeAbi"}
    missing = required.difference(values)
    if missing:
        raise ValueError("missing compatibility keys: " + ", ".join(sorted(missing)))
    return values


def read_tcz_version(path, pattern):
    text = path.read_text(encoding="utf-8")
    match = re.search(pattern, text)
    if not match:
        raise ValueError(f"unable to find TCZ_VERSION in {path}")
    return int(match.group(1))


def main():
    parser = argparse.ArgumentParser(
        description="Validate the TotalCross artifact compatibility contract."
    )
    parser.add_argument(
        "--repository-root",
        type=Path,
        default=Path(__file__).resolve().parents[2],
    )
    args = parser.parse_args()
    root = args.repository_root.resolve()
    contract = read_contract(root / "config/artifact-compatibility.properties")

    java_version = read_tcz_version(
        root / "TotalCrossSDK/src/main/java/totalcross/util/zip/TCZ.java",
        r"TCZ_VERSION\s*=\s*(\d+)",
    )
    native_version = read_tcz_version(
        root / "TotalCrossVM/src/util/tcz.h",
        r"#define\s+TCZ_VERSION\s+(\d+)",
    )

    failures = []
    for label, value in (("Java TCZ_VERSION", java_version), ("native TCZ_VERSION", native_version)):
        if value != contract["tczFormat"]:
            failures.append(
                f"{label}={value} does not match contract tczFormat={contract['tczFormat']}"
            )

    if contract["schemaVersion"] != 1:
        failures.append(
            f"unsupported compatibility schemaVersion={contract['schemaVersion']}"
        )
    if contract["converterAbi"] < 1 or contract["runtimeAbi"] < 1:
        failures.append("ABI epochs must be positive integers")

    if failures:
        for failure in failures:
            print(failure, file=sys.stderr)
        return 1

    print(
        "validated compatibility contract: "
        f"TCZ/{contract['tczFormat']} "
        f"converterAbi={contract['converterAbi']} "
        f"runtimeAbi={contract['runtimeAbi']}"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
