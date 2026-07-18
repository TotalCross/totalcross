# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

import argparse
import json
import re
from pathlib import Path


EXPECTED_IDENTITIES = (
    "fixtures.TCIRPoc.abs:(I)I",
    "fixtures.TCIRPoc.add:(II)I",
    "fixtures.TCIRPoc.i32ToF64:(I)D",
    "fixtures.TCIRPoc.i64ToF64:(J)D",
    "fixtures.TCIRPoc.normalizedF32:(F)F",
    "fixtures.TCIRPoc.pureF64:(DD)D",
    "fixtures.TCIRPoc.pureI32:(II)I",
    "fixtures.TCIRPoc.pureI64:(JI)J",
    "fixtures.TCIRPoc.sumTo:(I)I",
)
HEX64 = re.compile(r"^[0-9a-f]{16}$")
C_IDENTIFIER = re.compile(r"^[A-Za-z_][A-Za-z0-9_]*$")


def require(condition, message):
    if not condition:
        raise ValueError(message)


def validate(manifest_path, source_path, header_path):
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    source = source_path.read_text(encoding="utf-8")
    header = header_path.read_text(encoding="utf-8")

    require(manifest["schema_version"] == 1, "unsupported manifest schema")
    require(manifest["generator"] == "tcir-portable-c", "unexpected generator")
    require(manifest["generator_version"] == 1, "unexpected generator version")
    require(manifest["ir_version"] == 1, "unexpected IR version")
    require(manifest["runtime_abi_version"] == 3, "unexpected runtime ABI version")
    require(manifest["input_hash_algorithm"] == "fnv1a64", "unexpected input hash algorithm")
    require(HEX64.fullmatch(manifest["input_hash"]) is not None, "invalid input hash")
    require(manifest["target_options"], "missing target options")
    require(manifest["source"] == source_path.name, "source filename mismatch")
    require(manifest["header"] == header_path.name, "header filename mismatch")
    require(manifest["rejected_methods"] == [], "POC manifest contains rejected methods")

    methods = manifest["supported_methods"]
    identities = tuple(method["identity"] for method in methods)
    require(identities == EXPECTED_IDENTITIES, "supported methods are missing or not sorted")
    symbols = set()
    for method in methods:
        require(
            method["identity"]
            == f'{method["class"]}.{method["method"]}:{method["signature"]}',
            f'identity components do not match for {method["identity"]}',
        )
        require(HEX64.fullmatch(method["content_hash"]) is not None, "invalid method content hash")
        require(C_IDENTIFIER.fullmatch(method["symbol"]) is not None, "invalid generated C symbol")
        require(method["symbol"] not in symbols, "duplicate generated C symbol")
        require(method["symbol"] in source, "generated C symbol missing from source")
        require(method["diagnostic"] == "none", "supported method has a diagnostic")
        symbols.add(method["symbol"])

    copyright_line = "// Copyright (C) 2026 Amalgam Solucoes em TI Ltda"
    require(source.startswith(copyright_line), "generated source copyright header missing")
    require(header.startswith(copyright_line), "generated header copyright header missing")
    require('#include "tcir_aot_generated.h"' in source, "generated source include missing")
    require('#include "tcir_aot.h"' in header, "generated header ABI include missing")
    require(
        f"const size_t tcir_aot_generated_registry_count = {len(methods)}U;" in source,
        "generated registry count mismatch",
    )
    return len(methods), manifest["input_hash"]


def main():
    parser = argparse.ArgumentParser(description="Validate a TCIR portable-C AOT manifest and unit.")
    parser.add_argument("--manifest", required=True, type=Path)
    parser.add_argument("--source", required=True, type=Path)
    parser.add_argument("--header", required=True, type=Path)
    args = parser.parse_args()
    method_count, input_hash = validate(args.manifest, args.source, args.header)
    print(f"TCIR AOT manifest validated: {method_count} methods, input hash {input_hash}.")


if __name__ == "__main__":
    main()
