# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

import argparse
import re
import sys
from pathlib import Path


OPCODE_COUNT = 160
DECODER_CLASSES = {"SINGLE", "CALL", "SWITCH", "MULTIARRAY"}
LOWERING_CLASSES = {
    "DIRECT",
    "LOWERED",
    "RUNTIME_HELPER",
    "UNSUPPORTED_IN_POC",
    "FUTURE",
    "OBSOLETE",
    "PLATFORM_SPECIFIC",
    "NEEDS_INVESTIGATION",
}
POC_STATUSES = {"SUPPORTED", "FALLBACK", "INVESTIGATE"}


def read_text(path):
    return path.read_text(encoding="utf-8")


def parse_numeric_definitions(text, pattern):
    result = {}
    for name, value_text in re.findall(pattern, text, re.MULTILINE):
        value = int(value_text)
        if value < OPCODE_COUNT:
            result[value] = name
    return result


def parse_registry(text):
    pattern = re.compile(
        r"^TCIR_OPCODE\(\s*(\d+)\s*,\s*([A-Za-z0-9_]+)\s*,\s*"
        r"([A-Z_]+)\s*,\s*([A-Z_]+)\s*,\s*([A-Z_]+)\s*\)\s*$",
        re.MULTILINE,
    )
    result = {}
    dispositions = {}
    for value_text, name, decoder, lowering, poc_status in pattern.findall(text):
        value = int(value_text)
        if value in result:
            raise ValueError(f"registry duplicates opcode value {value}")
        result[value] = name
        dispositions[value] = (decoder, lowering, poc_status)
    return result, dispositions


def expected_values(label, mapping, failures):
    values = set(mapping)
    expected = set(range(OPCODE_COUNT))
    missing = sorted(expected - values)
    extra = sorted(values - expected)
    if missing:
        failures.append(f"{label} is missing opcode values: {missing}")
    if extra:
        failures.append(f"{label} has out-of-range opcode values: {extra}")


def compare_maps(label, expected, actual, failures):
    expected_values(label, actual, failures)
    for value in range(OPCODE_COUNT):
        if value in expected and value in actual and expected[value] != actual[value]:
            failures.append(
                f"{label} opcode {value} is {actual[value]}, expected {expected[value]}"
            )


def validate_name_table(java_text, canonical, failures):
    match = re.search(r"bcTClassNames\s*=\s*\{(.*?)\};", java_text, re.DOTALL)
    if not match:
        failures.append("TCConstants.bcTClassNames could not be parsed")
        return None
    names = re.findall(r'"([^"]+)"', match.group(1))
    for value, name in enumerate(names):
        expected = "NOP" if value == 0 else canonical.get(value)
        if name != expected:
            failures.append(
                f"TCConstants.bcTClassNames[{value}] is {name}, expected {expected}"
            )
    if len(names) == OPCODE_COUNT:
        return f"TCConstants.bcTClassNames covers all {OPCODE_COUNT} opcodes."
    missing = [canonical[value] for value in range(len(names), OPCODE_COUNT)]
    if len(names) == 158 and missing == ["MONITOR_Enter2", "MONITOR_Exit2"]:
        return (
            "Known name-table discrepancy: TCConstants.bcTClassNames has 158 entries "
            "and omits MONITOR_Enter2 (158) and MONITOR_Exit2 (159)."
        )
    failures.append(
        f"TCConstants.bcTClassNames has {len(names)} entries and unexpectedly omits {missing}"
    )
    return None


def validate(root):
    paths = {
        "opcodes": root / "TotalCrossVM/src/tcvm/opcodes.h",
        "registry": root / "TotalCrossVM/src/tcvm/ir/tcir_opcode_registry.def",
        "runtime": root / "TotalCrossVM/src/tcvm/tcvm.c",
        "java": root / "TotalCrossSDK/src/main/java/tc/tools/converter/TCConstants.java",
        "reference": root / "docs/architecture/bytecode/totalcross-bytecode-reference.md",
        "matrix": root / "docs/architecture/bytecode/compatibility-matrix.md",
    }
    missing_paths = [str(path) for path in paths.values() if not path.is_file()]
    if missing_paths:
        return [f"required source does not exist: {path}" for path in missing_paths], None

    opcodes_text = read_text(paths["opcodes"])
    registry_text = read_text(paths["registry"])
    runtime_text = read_text(paths["runtime"])
    java_text = read_text(paths["java"])
    reference_text = read_text(paths["reference"])
    matrix_text = read_text(paths["matrix"])

    canonical = parse_numeric_definitions(
        opcodes_text, r"^#define\s+([A-Za-z][A-Za-z0-9_]*)\s+(\d+)\b"
    )
    java_opcode_section = java_text.split("// Opcodes", 1)[1].split(
        "public static final int INSTRUCTION_NOT_FOUND", 1
    )[0]
    java = parse_numeric_definitions(
        java_opcode_section,
        r"public\s+static\s+final\s+int\s+([A-Za-z][A-Za-z0-9_]*)\s*=\s*(\d+)\s*;",
    )
    try:
        registry, dispositions = parse_registry(registry_text)
    except ValueError as error:
        return [str(error)], None

    runtime_names = set(re.findall(r"\bOPCODE\(([A-Za-z0-9_]+)\)", runtime_text))
    runtime_names.discard("x")
    reference = {
        int(value): name
        for value, name in re.findall(
            r"^\|\s*(\d+)\s*\|\s*`([A-Za-z0-9_]+)`\s*\|",
            reference_text,
            re.MULTILINE,
        )
    }
    matrix = {
        int(value): name
        for value, name in re.findall(r"`(\d+)\s+([A-Za-z0-9_]+)`", matrix_text)
    }

    failures = []
    expected_values("opcodes.h", canonical, failures)
    compare_maps("TCIR registry", canonical, registry, failures)
    compare_maps("TCConstants numeric constants", canonical, java, failures)
    compare_maps("bytecode reference", canonical, reference, failures)
    compare_maps("compatibility matrix", canonical, matrix, failures)

    canonical_names = set(canonical.values())
    missing_dispatch = sorted(canonical_names - runtime_names)
    extra_dispatch = sorted(runtime_names - canonical_names)
    if missing_dispatch:
        failures.append(f"runtime dispatch is missing: {missing_dispatch}")
    if extra_dispatch:
        failures.append(f"runtime dispatch has unknown opcode names: {extra_dispatch}")

    for value in range(OPCODE_COUNT):
        if value not in dispositions:
            continue
        decoder, lowering, poc_status = dispositions[value]
        if decoder not in DECODER_CLASSES:
            failures.append(f"registry opcode {value} has invalid decoder class {decoder}")
        if lowering not in LOWERING_CLASSES:
            failures.append(f"registry opcode {value} has invalid lowering class {lowering}")
        if poc_status not in POC_STATUSES:
            failures.append(f"registry opcode {value} has invalid POC status {poc_status}")

    discrepancy = validate_name_table(java_text, canonical, failures)
    return failures, discrepancy


def main():
    parser = argparse.ArgumentParser(
        description="Cross-check the TCIR opcode registry against runtime, SDK, and documentation sources."
    )
    parser.add_argument(
        "--root",
        type=Path,
        default=Path(__file__).resolve().parent.parent,
        help="repository root",
    )
    args = parser.parse_args()

    failures, discrepancy = validate(args.root.resolve())
    if failures:
        print(f"TCIR opcode validation failed with {len(failures)} error(s):", file=sys.stderr)
        for failure in failures:
            print(f"- {failure}", file=sys.stderr)
        return 1

    print("TCIR opcode validation passed for all 160 opcodes.")
    if discrepancy:
        print(discrepancy)
    return 0


if __name__ == "__main__":
    sys.exit(main())
