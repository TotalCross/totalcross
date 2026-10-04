#!/usr/bin/env python3
#
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

import argparse
from pathlib import Path
import re
import sys

sys.path.insert(0, str(Path(__file__).resolve().parent))
sys.dont_write_bytecode = True
from compatibility_contract import read_compatibility_contract


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
    try:
        contract = read_compatibility_contract(
            root / "config/artifact-compatibility.properties"
        )
    except (OSError, ValueError) as error:
        print(f"invalid compatibility contract: {error}", file=sys.stderr)
        return 1

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
