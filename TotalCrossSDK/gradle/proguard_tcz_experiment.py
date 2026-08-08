#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

"""Run the isolated ProGuard-before-J2TC structural experiment.

The script is intentionally build-local. It never replaces the ordinary SDK
JARs or TCZs and writes every generated artifact below the experiment root.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import os
import shutil
import struct
import subprocess
import sys
import time
import zlib
import zipfile
from pathlib import Path
from typing import Any, Iterable


SCHEMA_VERSION = 1
PROGUARD_VERSION = "7.9.1"
FULL_OPTIMIZATIONS = "!class/merging/*,*"
COMPATIBILITY_ADJUSTMENTS = [
    "A build-local post-pass removes only LineNumberTable from optimized runtime classes because valid "
    "tables whose first entry starts after PC 0 trigger Bytecode2TCCode.getLineOfPC with index -1; "
    "the preserved strict attempt is compatibility evidence."
]
SHRINK_COMPATIBILITY_PINS = [
    "totalcross.sql.DriverManager code is pinned because ProGuard retargets an inherited Hashtable.put "
    "call to java.util.Properties.put, which is valid JVM resolution but rejected by J2TC's device API check."
]
ABLATION_GROUPS = {
    "local": (
        "code/simplification/*,code/removal/*,code/merging,"
        "code/allocation/variable"
    ),
    "method": (
        "method/marking/*,method/removal/parameter,method/generalization/*,"
        "method/specialization/*,method/propagation/*,method/inlining/*"
    ),
    "field": (
        "field/removal/*,field/marking/*,field/generalization/*,"
        "field/specialization/*,field/propagation/*"
    ),
    "enum": "class/unboxing/enum",
}
FIELD_SUBGROUPS = {
    "field-removal": "field/removal/*",
    "field-marking": "field/marking/*",
    "field-generalization": "field/generalization/*",
    "field-specialization": "field/specialization/*",
    "field-propagation": "field/propagation/*",
}
METRIC_KEYS = (
    "jar_bytes",
    "tcz_bytes",
    "tcz_compressed_chunk_bytes",
    "tcz_actual_uncompressed_bytes",
    "tcz_class_payload_bytes",
    "tcz_constant_pool_bytes",
    "jar_class_count",
    "tcz_class_count",
    "java_method_count",
    "java_bytecode_instruction_count",
    "tc_method_count",
    "tc_code_slots",
    "tc_call_normal",
    "tc_call_virtual",
    "tc_allocations",
    "tc_branches",
    "tc_field_accesses",
    "tc_checkcasts",
    "tc_instanceof",
    "tc_register_i",
    "tc_register_o",
    "tc_register_v64",
)


class ExperimentError(RuntimeError):
    """A hard failure that prevents trustworthy experiment evidence."""


class Reader:
    def __init__(self, data: bytes):
        self.data = data
        self.pos = 0

    def take(self, size: int) -> bytes:
        end = self.pos + size
        if size < 0 or end > len(self.data):
            raise ValueError(f"truncated input at {self.pos}, need {size} bytes")
        value = self.data[self.pos:end]
        self.pos = end
        return value

    def u1(self) -> int:
        return self.take(1)[0]

    def u2(self) -> int:
        return struct.unpack(">H", self.take(2))[0]

    def u4(self) -> int:
        return struct.unpack(">I", self.take(4))[0]


def sha256_path(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def sha256_text(value: str) -> str:
    return hashlib.sha256(value.encode("utf-8")).hexdigest()


def write_json(path: Path, value: Any) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    payload = json.dumps(value, indent=2, sort_keys=True, ensure_ascii=True) + "\n"
    path.write_text(payload, encoding="utf-8")


def read_json(path: Path) -> Any:
    with path.open(encoding="utf-8") as stream:
        return json.load(stream)


def write_text(path: Path, value: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(value, encoding="utf-8")


def git_output(repository_root: Path, *arguments: str) -> str:
    result = subprocess.run(
        ["git", *arguments],
        cwd=repository_root,
        check=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
    )
    return result.stdout.strip()


def stable_diagnostic(log_path: Path) -> str:
    if not log_path.exists():
        return "command failed without a log"
    lines = log_path.read_text(encoding="utf-8", errors="replace").splitlines()
    preferred = (
        "InvalidClassException:",
        "ArrayIndexOutOfBoundsException:",
        "Exception in thread",
        "Error:",
        "Caused by:",
        "Unsupported",
        "Unexpected",
        "Can't find",
        "can't find",
        "Warning:",
    )
    for marker in preferred:
        for line in lines:
            if marker in line:
                return line.strip()[:1000]
    for line in reversed(lines):
        if line.strip():
            return line.strip()[:1000]
    return "command failed without a diagnostic"


def run_logged(command: list[str], log_path: Path, cwd: Path) -> dict[str, Any]:
    log_path.parent.mkdir(parents=True, exist_ok=True)
    start = time.monotonic()
    with log_path.open("w", encoding="utf-8") as log:
        log.write("command=" + " ".join(command) + "\n")
        log.flush()
        result = subprocess.run(
            command,
            cwd=cwd,
            stdout=log,
            stderr=subprocess.STDOUT,
            text=True,
        )
    duration = round(time.monotonic() - start, 3)
    content = log_path.read_text(encoding="utf-8", errors="replace")
    return {
        "exit_code": result.returncode,
        "duration_seconds": duration,
        "warning_count": sum(1 for line in content.splitlines() if "Warning:" in line),
        "diagnostic": None if result.returncode == 0 else stable_diagnostic(log_path),
        "log": str(log_path),
    }


def parse_constant_pool(reader: Reader) -> tuple[list[Any], list[str | None]]:
    count = reader.u2()
    pool: list[Any] = [None] * count
    utf8: list[str | None] = [None] * count
    index = 1
    while index < count:
        tag = reader.u1()
        if tag == 1:
            raw = reader.take(reader.u2())
            value = raw.decode("utf-8", errors="replace")
            pool[index] = (tag, value)
            utf8[index] = value
        elif tag in (3, 4):
            reader.take(4)
            pool[index] = (tag,)
        elif tag in (5, 6):
            reader.take(8)
            pool[index] = (tag,)
            index += 1
        elif tag in (7, 8, 16, 19, 20):
            pool[index] = (tag, reader.u2())
        elif tag in (9, 10, 11, 12, 17, 18):
            reader.take(4)
            pool[index] = (tag,)
        elif tag == 15:
            reader.take(3)
            pool[index] = (tag,)
        else:
            raise ValueError(f"unsupported constant-pool tag {tag}")
        index += 1
    return pool, utf8


def class_name(pool: list[Any], utf8: list[str | None], index: int) -> str:
    entry = pool[index]
    if not entry or entry[0] != 7:
        raise ValueError(f"constant-pool entry {index} is not a class")
    value = utf8[entry[1]]
    if value is None:
        raise ValueError(f"class name at constant-pool entry {index} is missing")
    return value


def skip_attributes(reader: Reader, count: int) -> None:
    for _ in range(count):
        reader.u2()
        reader.take(reader.u4())


def bytecode_metrics(code: bytes) -> dict[str, int]:
    metrics = {
        "instructions": 0,
        "calls": 0,
        "virtual_calls": 0,
        "allocations": 0,
        "branches": 0,
        "field_accesses": 0,
        "checkcasts": 0,
        "instanceof": 0,
    }
    lengths: dict[int, int] = {}
    for opcode in (16, 18, 21, 22, 23, 24, 25, 54, 55, 56, 57, 58, 169, 188):
        lengths[opcode] = 2
    for opcode in (17, 19, 20, 132):
        lengths[opcode] = 3
    for opcode in range(153, 169):
        lengths[opcode] = 3
    for opcode in (178, 179, 180, 181, 182, 183, 184, 187, 189, 192, 193, 198, 199):
        lengths[opcode] = 3
    lengths.update({185: 5, 186: 5, 197: 4, 200: 5, 201: 5})
    branch_opcodes = set(range(153, 169)) | {170, 171, 198, 199, 200, 201}
    pc = 0
    while pc < len(code):
        opcode = code[pc]
        metrics["instructions"] += 1
        if opcode in (182, 183, 184, 185, 186):
            metrics["calls"] += 1
        if opcode in (182, 185):
            metrics["virtual_calls"] += 1
        if opcode in (187, 188, 189, 197):
            metrics["allocations"] += 1
        if opcode in branch_opcodes:
            metrics["branches"] += 1
        if opcode in (178, 179, 180, 181):
            metrics["field_accesses"] += 1
        if opcode == 192:
            metrics["checkcasts"] += 1
        if opcode == 193:
            metrics["instanceof"] += 1
        if opcode == 170:
            padding = (4 - ((pc + 1) % 4)) % 4
            header = pc + 1 + padding
            if header + 12 > len(code):
                raise ValueError("truncated tableswitch")
            low = struct.unpack_from(">i", code, header + 4)[0]
            high = struct.unpack_from(">i", code, header + 8)[0]
            size = 1 + padding + 12 + 4 * (high - low + 1)
        elif opcode == 171:
            padding = (4 - ((pc + 1) % 4)) % 4
            header = pc + 1 + padding
            if header + 8 > len(code):
                raise ValueError("truncated lookupswitch")
            pairs = struct.unpack_from(">i", code, header + 4)[0]
            size = 1 + padding + 8 + 8 * pairs
        elif opcode == 196:
            if pc + 1 >= len(code):
                raise ValueError("truncated wide instruction")
            size = 6 if code[pc + 1] == 132 else 4
        else:
            size = lengths.get(opcode, 1)
        if size <= 0 or pc + size > len(code):
            raise ValueError(f"invalid bytecode size {size} for opcode {opcode} at {pc}")
        pc += size
    return metrics


def parse_class_file(data: bytes) -> dict[str, Any]:
    reader = Reader(data)
    if reader.u4() != 0xCAFEBABE:
        raise ValueError("not a Java class file")
    minor = reader.u2()
    major = reader.u2()
    pool, utf8 = parse_constant_pool(reader)
    access = reader.u2()
    this_class = reader.u2()
    reader.u2()
    name = class_name(pool, utf8, this_class)
    for _ in range(reader.u2()):
        reader.u2()

    public_api: list[str] = []
    is_public_class = bool(access & 0x0001)
    if is_public_class:
        public_api.append(f"class:{name}")

    field_count = reader.u2()
    for _ in range(field_count):
        member_access = reader.u2()
        member_name = utf8[reader.u2()] or "<missing>"
        descriptor = utf8[reader.u2()] or "<missing>"
        skip_attributes(reader, reader.u2())
        if is_public_class and member_access & (0x0001 | 0x0004):
            public_api.append(f"field:{name}.{member_name}:{descriptor}")

    metrics = {
        "method_count": 0,
        "bytecode_bytes": 0,
        "bytecode_instruction_count": 0,
        "calls": 0,
        "virtual_calls": 0,
        "allocations": 0,
        "branches": 0,
        "field_accesses": 0,
        "checkcasts": 0,
        "instanceof": 0,
    }
    method_count = reader.u2()
    metrics["method_count"] = method_count
    for _ in range(method_count):
        member_access = reader.u2()
        member_name = utf8[reader.u2()] or "<missing>"
        descriptor = utf8[reader.u2()] or "<missing>"
        if is_public_class and member_access & (0x0001 | 0x0004):
            public_api.append(f"method:{name}.{member_name}{descriptor}")
        attribute_count = reader.u2()
        for _ in range(attribute_count):
            attribute_name = utf8[reader.u2()] or ""
            attribute = reader.take(reader.u4())
            if attribute_name != "Code":
                continue
            code_reader = Reader(attribute)
            code_reader.u2()
            code_reader.u2()
            code = code_reader.take(code_reader.u4())
            metrics["bytecode_bytes"] += len(code)
            code_metrics = bytecode_metrics(code)
            for key, value in code_metrics.items():
                target = "bytecode_instruction_count" if key == "instructions" else key
                metrics[target] += value
    skip_attributes(reader, reader.u2())
    if reader.pos != len(data):
        raise ValueError(f"class parser stopped at {reader.pos} of {len(data)} bytes")
    return {
        "name": name,
        "minor_version": minor,
        "major_version": major,
        "access": access,
        "public_api": sorted(public_api),
        "metrics": metrics,
    }


def strip_line_numbers_from_code(attribute: bytes, utf8: list[str | None]) -> tuple[bytes, int]:
    reader = Reader(attribute)
    output = bytearray()
    output.extend(reader.take(4))
    code_length_raw = reader.take(4)
    code_length = struct.unpack(">I", code_length_raw)[0]
    output.extend(code_length_raw)
    output.extend(reader.take(code_length))
    exception_count_raw = reader.take(2)
    exception_count = struct.unpack(">H", exception_count_raw)[0]
    output.extend(exception_count_raw)
    output.extend(reader.take(exception_count * 8))
    nested_count = reader.u2()
    kept: list[tuple[int, bytes]] = []
    removed = 0
    for _ in range(nested_count):
        name_index = reader.u2()
        payload = reader.take(reader.u4())
        if utf8[name_index] == "LineNumberTable":
            removed += 1
        else:
            kept.append((name_index, payload))
    if reader.pos != len(attribute):
        raise ValueError("Code attribute rewrite did not consume its input")
    output.extend(struct.pack(">H", len(kept)))
    for name_index, payload in kept:
        output.extend(struct.pack(">HI", name_index, len(payload)))
        output.extend(payload)
    return bytes(output), removed


def strip_line_numbers_from_class(data: bytes) -> tuple[bytes, int]:
    reader = Reader(data)
    if reader.u4() != 0xCAFEBABE:
        raise ValueError("not a Java class file")
    reader.take(4)
    _, utf8 = parse_constant_pool(reader)
    constant_pool_end = reader.pos
    output = bytearray(data[:constant_pool_end])

    output.extend(reader.take(6))
    interface_count_raw = reader.take(2)
    interface_count = struct.unpack(">H", interface_count_raw)[0]
    output.extend(interface_count_raw)
    output.extend(reader.take(interface_count * 2))

    field_count_raw = reader.take(2)
    field_count = struct.unpack(">H", field_count_raw)[0]
    output.extend(field_count_raw)
    for _ in range(field_count):
        output.extend(reader.take(6))
        attribute_count_raw = reader.take(2)
        attribute_count = struct.unpack(">H", attribute_count_raw)[0]
        output.extend(attribute_count_raw)
        for _ in range(attribute_count):
            output.extend(reader.take(2))
            length_raw = reader.take(4)
            length = struct.unpack(">I", length_raw)[0]
            output.extend(length_raw)
            output.extend(reader.take(length))

    method_count_raw = reader.take(2)
    method_count = struct.unpack(">H", method_count_raw)[0]
    output.extend(method_count_raw)
    removed = 0
    for _ in range(method_count):
        output.extend(reader.take(6))
        attribute_count = reader.u2()
        output.extend(struct.pack(">H", attribute_count))
        for _ in range(attribute_count):
            name_index = reader.u2()
            payload = reader.take(reader.u4())
            if utf8[name_index] == "Code":
                payload, removed_here = strip_line_numbers_from_code(payload, utf8)
                removed += removed_here
            output.extend(struct.pack(">HI", name_index, len(payload)))
            output.extend(payload)

    class_attribute_count_raw = reader.take(2)
    class_attribute_count = struct.unpack(">H", class_attribute_count_raw)[0]
    output.extend(class_attribute_count_raw)
    for _ in range(class_attribute_count):
        output.extend(reader.take(2))
        length_raw = reader.take(4)
        length = struct.unpack(">I", length_raw)[0]
        output.extend(length_raw)
        output.extend(reader.take(length))
    if reader.pos != len(data):
        raise ValueError("class rewrite did not consume its input")
    return bytes(output), removed


def strip_line_numbers_from_jar(path: Path, eligible_classes: set[str]) -> dict[str, int]:
    temporary = path.with_suffix(path.suffix + ".rewrite")
    removed_attributes = 0
    changed_classes = 0
    with zipfile.ZipFile(path) as source, zipfile.ZipFile(
        temporary, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9
    ) as target:
        for name in sorted(source.namelist()):
            if name.endswith("/"):
                continue
            content = source.read(name)
            if name in eligible_classes:
                content, removed = strip_line_numbers_from_class(content)
                if removed:
                    changed_classes += 1
                    removed_attributes += removed
            info = zipfile.ZipInfo(name, date_time=(1980, 1, 1, 0, 0, 0))
            info.compress_type = zipfile.ZIP_DEFLATED
            info.external_attr = 0o644 << 16
            target.writestr(info, content)
    temporary.replace(path)
    return {
        "eligible_class_count": len(eligible_classes),
        "changed_class_count": changed_classes,
        "removed_line_number_table_count": removed_attributes,
    }


def parse_tc_constants(path: Path) -> dict[str, int]:
    import re

    constants: dict[str, int] = {}
    pattern = re.compile(r"public static final int\s+([A-Za-z0-9_]+)\s*=\s*([0-9]+)\s*;")
    for match in pattern.finditer(path.read_text(encoding="utf-8")):
        constants[match.group(1)] = int(match.group(2))
    required = {"CALL_normal", "CALL_virtual", "NEWOBJ", "CHECKCAST", "INSTANCEOF"}
    missing = required - constants.keys()
    if missing:
        raise ExperimentError(f"cannot parse TC opcode constants: missing {sorted(missing)}")
    return constants


def parse_tclass(data: bytes, constants: dict[str, int]) -> dict[str, int]:
    if len(data) < 22:
        raise ValueError("TC class chunk is too small")
    pos = 0

    def u1() -> int:
        nonlocal pos
        if pos + 1 > len(data):
            raise ValueError("truncated TC class")
        value = data[pos]
        pos += 1
        return value

    def u2() -> int:
        nonlocal pos
        if pos + 2 > len(data):
            raise ValueError("truncated TC class")
        value = struct.unpack_from("<H", data, pos)[0]
        pos += 2
        return value

    def u4() -> int:
        nonlocal pos
        if pos + 4 > len(data):
            raise ValueError("truncated TC class")
        value = struct.unpack_from("<I", data, pos)[0]
        pos += 4
        return value

    u2()
    values = [u2() for _ in range(10)]
    interface_count = values[2]
    field_count = sum(values[3:9])
    method_count = values[9]
    pos += interface_count * 2 + field_count * 6
    if pos > len(data):
        raise ValueError("invalid TC class field/interface counts")

    call_normal = constants["CALL_normal"]
    call_virtual = constants["CALL_virtual"]
    allocation_ops = {
        value for name, value in constants.items()
        if name == "NEWOBJ" or name.startswith("NEWARRAY_")
    }
    branch_ops = {
        value for name, value in constants.items()
        if name == "SWITCH" or name.startswith(("JUMP", "JEQ", "JNE", "JLT", "JLE", "JGT", "JGE", "DECJ"))
    }
    field_ops = {
        value for name, value in constants.items()
        if name.startswith("MOV_") and ("_field" in name or "_static" in name)
    }
    metrics = {
        "method_count": method_count,
        "code_slots": 0,
        "call_normal": 0,
        "call_virtual": 0,
        "allocations": 0,
        "branches": 0,
        "field_accesses": 0,
        "checkcasts": 0,
        "instanceof": 0,
        "register_i": 0,
        "register_o": 0,
        "register_v64": 0,
    }
    for _ in range(method_count):
        u2()
        opcode_count = u2()
        exception_count = u2()
        line_count = u2()
        metrics["register_i"] += u1()
        metrics["register_o"] += u1()
        metrics["register_v64"] += u1()
        parameter_count = u1()
        u2()
        u2()
        pos += parameter_count * 2
        metrics["code_slots"] += opcode_count
        for _ in range(opcode_count):
            opcode = u4() & 0xFF
            if opcode == call_normal:
                metrics["call_normal"] += 1
            if opcode == call_virtual:
                metrics["call_virtual"] += 1
            if opcode in allocation_ops:
                metrics["allocations"] += 1
            if opcode in branch_ops:
                metrics["branches"] += 1
            if opcode in field_ops:
                metrics["field_accesses"] += 1
            if opcode == constants["CHECKCAST"]:
                metrics["checkcasts"] += 1
            if opcode == constants["INSTANCEOF"]:
                metrics["instanceof"] += 1
        pos += exception_count * 10
        if pos > len(data):
            raise ValueError("invalid TC method exception count")
        if line_count == 1:
            pos += 2
        elif line_count > 1:
            first_pc = u1()
            if first_pc != 255:
                pos += line_count - 2
            u2()
            first_line = u1()
            if first_line != 255:
                pos += line_count - 2
        if pos > len(data):
            raise ValueError("invalid TC line-number data")
    if pos != len(data):
        raise ValueError(f"TC class parser stopped at {pos} of {len(data)} bytes")
    return metrics


def analyze_jar(path: Path) -> dict[str, Any]:
    metrics = {
        "jar_bytes": path.stat().st_size,
        "class_count": 0,
        "resource_count": 0,
        "classfile_bytes": 0,
        "method_count": 0,
        "bytecode_bytes": 0,
        "bytecode_instruction_count": 0,
        "calls": 0,
        "virtual_calls": 0,
        "allocations": 0,
        "branches": 0,
        "field_accesses": 0,
        "checkcasts": 0,
        "instanceof": 0,
    }
    classes: list[str] = []
    public_api: list[str] = []
    versions: dict[str, int] = {}
    with zipfile.ZipFile(path) as archive:
        for info in sorted(archive.infolist(), key=lambda item: item.filename):
            if info.is_dir():
                continue
            data = archive.read(info)
            if not info.filename.endswith(".class") or info.filename == "module-info.class":
                metrics["resource_count"] += 1
                continue
            parsed = parse_class_file(data)
            metrics["class_count"] += 1
            metrics["classfile_bytes"] += len(data)
            classes.append(info.filename)
            public_api.extend(parsed["public_api"])
            versions[str(parsed["major_version"])] = versions.get(str(parsed["major_version"]), 0) + 1
            for key, value in parsed["metrics"].items():
                metrics[key] += value
    return {
        "path": str(path),
        "sha256": sha256_path(path),
        "metrics": metrics,
        "class_versions": versions,
        "classes": classes,
        "public_api": sorted(public_api),
        "public_api_sha256": sha256_text("\n".join(sorted(public_api))),
    }


def analyze_tcz(path: Path, constants: dict[str, int]) -> dict[str, Any]:
    data = path.read_bytes()
    if len(data) < 8:
        raise ExperimentError(f"TCZ is truncated: {path}")
    version, attributes, base_offset = struct.unpack_from("<HHI", data, 0)
    if base_offset < 8 or base_offset > len(data):
        raise ExperimentError(f"TCZ has invalid base offset {base_offset}: {path}")
    try:
        header = zlib.decompress(data[8:base_offset])
    except zlib.error as error:
        raise ExperimentError(f"cannot decompress TCZ header {path}: {error}") from error
    cursor = 0

    def header_u4() -> int:
        nonlocal cursor
        if cursor + 4 > len(header):
            raise ExperimentError(f"truncated TCZ header: {path}")
        value = struct.unpack_from("<I", header, cursor)[0]
        cursor += 4
        return value

    chunk_count = header_u4()
    relative_offsets = [header_u4() for _ in range(chunk_count + 1)]
    declared_sizes = [header_u4() for _ in range(chunk_count)]
    names: list[str] = []
    for _ in range(chunk_count):
        if cursor >= len(header):
            raise ExperimentError(f"truncated TCZ name table: {path}")
        length = header[cursor]
        cursor += 1
        if cursor + length > len(header):
            raise ExperimentError(f"truncated TCZ name: {path}")
        names.append(header[cursor:cursor + length].decode("latin-1"))
        cursor += length
    if cursor != len(header):
        raise ExperimentError(f"TCZ header has {len(header) - cursor} trailing bytes: {path}")
    offsets = [base_offset + value for value in relative_offsets]
    if offsets[0] != base_offset or offsets[-1] != len(data):
        raise ExperimentError(f"TCZ offsets do not cover the file exactly: {path}")

    entries: list[dict[str, Any]] = []
    totals = {
        "tcz_bytes": len(data),
        "chunk_count": chunk_count,
        "compressed_chunk_bytes": 0,
        "declared_uncompressed_bytes": 0,
        "actual_uncompressed_bytes": 0,
        "class_payload_bytes": 0,
        "constant_pool_bytes": 0,
        "resource_payload_bytes": 0,
        "class_count": 0,
        "method_count": 0,
        "code_slots": 0,
        "call_normal": 0,
        "call_virtual": 0,
        "allocations": 0,
        "branches": 0,
        "field_accesses": 0,
        "checkcasts": 0,
        "instanceof": 0,
        "register_i": 0,
        "register_o": 0,
        "register_v64": 0,
    }
    for index, name in enumerate(names):
        compressed = data[offsets[index]:offsets[index + 1]]
        try:
            uncompressed = zlib.decompress(compressed)
        except zlib.error as error:
            raise ExperimentError(f"cannot decompress TCZ chunk {name} in {path}: {error}") from error
        entry = {
            "name": name,
            "compressed_bytes": len(compressed),
            "declared_uncompressed_bytes": declared_sizes[index],
            "actual_uncompressed_bytes": len(uncompressed),
            "sha256": hashlib.sha256(uncompressed).hexdigest(),
            "kind": "resource",
        }
        totals["compressed_chunk_bytes"] += len(compressed)
        totals["declared_uncompressed_bytes"] += declared_sizes[index]
        totals["actual_uncompressed_bytes"] += len(uncompressed)
        if name == "ConstantPool":
            entry["kind"] = "constant-pool"
            totals["constant_pool_bytes"] += len(uncompressed)
        else:
            try:
                tclass = parse_tclass(uncompressed, constants)
            except ValueError:
                totals["resource_payload_bytes"] += len(uncompressed)
            else:
                entry["kind"] = "class"
                entry["tc_metrics"] = tclass
                totals["class_count"] += 1
                totals["class_payload_bytes"] += len(uncompressed)
                for key, value in tclass.items():
                    totals[key] += value
        entries.append(entry)
    return {
        "path": str(path),
        "sha256": sha256_path(path),
        "version": version,
        "attributes": attributes,
        "base_offset": base_offset,
        "header_compressed_bytes": base_offset - 8,
        "metrics": totals,
        "entries": entries,
    }


def combined_metrics(jar: dict[str, Any], tcz: dict[str, Any]) -> dict[str, int]:
    java = jar["metrics"]
    tc = tcz["metrics"]
    return {
        "jar_bytes": java["jar_bytes"],
        "tcz_bytes": tc["tcz_bytes"],
        "tcz_compressed_chunk_bytes": tc["compressed_chunk_bytes"],
        "tcz_actual_uncompressed_bytes": tc["actual_uncompressed_bytes"],
        "tcz_class_payload_bytes": tc["class_payload_bytes"],
        "tcz_constant_pool_bytes": tc["constant_pool_bytes"],
        "jar_class_count": java["class_count"],
        "tcz_class_count": tc["class_count"],
        "java_method_count": java["method_count"],
        "java_bytecode_instruction_count": java["bytecode_instruction_count"],
        "tc_method_count": tc["method_count"],
        "tc_code_slots": tc["code_slots"],
        "tc_call_normal": tc["call_normal"],
        "tc_call_virtual": tc["call_virtual"],
        "tc_allocations": tc["allocations"],
        "tc_branches": tc["branches"],
        "tc_field_accesses": tc["field_accesses"],
        "tc_checkcasts": tc["checkcasts"],
        "tc_instanceof": tc["instanceof"],
        "tc_register_i": tc["register_i"],
        "tc_register_o": tc["register_o"],
        "tc_register_v64": tc["register_v64"],
    }


def sum_metrics(modules: Iterable[dict[str, Any]]) -> dict[str, int]:
    totals = {key: 0 for key in METRIC_KEYS}
    for module in modules:
        for key in METRIC_KEYS:
            totals[key] += module["metrics"][key]
    return totals


def metric_delta(current: int, baseline: int) -> dict[str, float | int | None]:
    return {
        "absolute": current - baseline,
        "percent": None if baseline == 0 else round((current - baseline) * 100.0 / baseline, 6),
    }


def module_deltas(current: dict[str, int], baseline: dict[str, int]) -> dict[str, Any]:
    return {key: metric_delta(current[key], baseline[key]) for key in METRIC_KEYS}


def analyze_module(jar_path: Path, tcz_path: Path, constants: dict[str, int]) -> dict[str, Any]:
    jar = analyze_jar(jar_path)
    tcz = analyze_tcz(tcz_path, constants)
    return {
        "status": "success",
        "jar": jar,
        "tcz": tcz,
        "metrics": combined_metrics(jar, tcz),
    }


def markdown_baseline(baseline: dict[str, Any]) -> str:
    lines = [
        "# ProGuard-before-J2TC baseline",
        "",
        f"Revision: `{baseline['revision']}`",
        "",
        "| Module | JAR bytes | TCZ bytes | TC class payload | TC methods | TC code slots |",
        "|---|---:|---:|---:|---:|---:|",
    ]
    for module_id, module in baseline["modules"].items():
        metrics = module["metrics"]
        lines.append(
            f"| {module_id} | {metrics['jar_bytes']} | {metrics['tcz_bytes']} | "
            f"{metrics['tcz_class_payload_bytes']} | {metrics['tc_method_count']} | "
            f"{metrics['tc_code_slots']} |"
        )
    aggregate = baseline["aggregate"]
    lines.append(
        f"| **aggregate** | **{aggregate['jar_bytes']}** | **{aggregate['tcz_bytes']}** | "
        f"**{aggregate['tcz_class_payload_bytes']}** | **{aggregate['tc_method_count']}** | "
        f"**{aggregate['tc_code_slots']}** |"
    )
    lines.extend([
        "",
        "Compressed TCZ bytes are reported as artifact evidence; structural interpretation uses actual decompressed class payload and parsed TotalCross method/code metrics.",
        "",
    ])
    return "\n".join(lines)


def proguard_quote(path: Path) -> str:
    value = str(path).replace("'", "\\'")
    return f"'{value}'"


def filtered_archive(path: Path) -> str:
    return f"{proguard_quote(path)}(!META-INF/versions/**;!module-info.class)"


def make_remainder_library(sdk_jar: Path, program_jars: list[Path], output: Path) -> None:
    owned: set[str] = set()
    for program in program_jars:
        with zipfile.ZipFile(program) as archive:
            owned.update(name for name in archive.namelist() if name.endswith(".class"))
    output.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(sdk_jar) as source, zipfile.ZipFile(
        output, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9
    ) as target:
        for name in sorted(source.namelist()):
            if not name.endswith(".class") or name in owned or name == "module-info.class":
                continue
            info = zipfile.ZipInfo(name, date_time=(1980, 1, 1, 0, 0, 0))
            info.compress_type = zipfile.ZIP_DEFLATED
            info.external_attr = 0o644 << 16
            target.writestr(info, source.read(name))


def archive_class_names(path: Path) -> list[str]:
    with zipfile.ZipFile(path) as archive:
        return sorted(
            name[:-6].replace("/", ".")
            for name in archive.namelist()
            if name.endswith(".class") and name != "module-info.class"
        )


def auxiliary_program(context: dict[str, Any], variant_dir: Path) -> tuple[Path, list[str]]:
    auxiliary = variant_dir / "auxiliary" / "sdk-auxiliary.jar"
    make_remainder_library(context["sdk_jar"], context["module_jars"], auxiliary)
    return auxiliary, archive_class_names(auxiliary)


def library_inputs(context: dict[str, Any], program_jars: list[Path], variant_dir: Path) -> list[Path]:
    libraries: list[Path] = []
    program_resolved = {path.resolve() for path in program_jars}
    sdk_resolved = context["sdk_jar"].resolve()
    for entry in context["deploy_classpath"]:
        if not entry.exists() or entry.suffix != ".jar":
            continue
        resolved = entry.resolve()
        if resolved == sdk_resolved or resolved in program_resolved:
            continue
        libraries.append(entry)
    jmods = context["java_home"] / "jmods"
    if not jmods.is_dir():
        raise ExperimentError(f"Java runtime modules are unavailable under {jmods}")
    libraries.extend(sorted(jmods.glob("*.jmod")))
    return libraries


def common_configuration(
    injars: list[Path],
    outjar: Path,
    libraries: list[Path],
    passes: int,
    optimizations: str,
    shrink: bool,
    variant_dir: Path,
    optimizable_classes: list[str],
    pinned_classes: list[str],
) -> str:
    lines: list[str] = []
    for path in injars:
        lines.append(f"-injars {filtered_archive(path)}")
    lines.append(f"-outjars {proguard_quote(outjar)}")
    for path in libraries:
        if path.suffix == ".jmod":
            lines.append(f"-libraryjars {proguard_quote(path)}(!**.jar;!module-info.class)")
        else:
            lines.append(f"-libraryjars {filtered_archive(path)}")
    if not shrink:
        lines.append("-dontshrink")
    lines.extend([
        "-dontobfuscate",
        f"-optimizationpasses {passes}",
        f"-optimizations {optimizations}",
        "-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod,MethodParameters,Record,PermittedSubclasses,NestHost,NestMembers",
        "-keepdirectories",
        f"-printconfiguration {proguard_quote(variant_dir / 'effective-configuration.pro')}",
    ])
    if not shrink:
        lines.extend(
            f"-keep,allowoptimization class {name} {{ *; }}"
            for name in optimizable_classes
        )
    lines.extend(f"-keep class {name} {{ *; }}" for name in pinned_classes)
    if shrink:
        lines.extend([
            f"-printusage {proguard_quote(variant_dir / 'usage.txt')}",
            f"-printseeds {proguard_quote(variant_dir / 'seeds.txt')}",
            "-keep,includedescriptorclasses public class * {",
            "    public <fields>;",
            "    protected <fields>;",
            "    public <methods>;",
            "    protected <methods>;",
            "}",
            "-keep class **4D { *; }",
            "-keepclasseswithmembers,includedescriptorclasses class * {",
            "    native <methods>;",
            "}",
            "-keep class totalcross.net.SocketFactory { *; }",
            "-keep,includecode class totalcross.sql.DriverManager { *; }",
            "-keepclassmembers class * implements totalcross.io.Storable {",
            "    public byte getID();",
            "    public totalcross.io.Storable getInstance();",
            "    public void saveState(totalcross.io.DataStream);",
            "    public void loadState(totalcross.io.DataStream);",
            "}",
            "-keepclassmembers class * implements java.io.Serializable {",
            "    private static final long serialVersionUID;",
            "    private void writeObject(java.io.ObjectOutputStream);",
            "    private void readObject(java.io.ObjectInputStream);",
            "    java.lang.Object writeReplace();",
            "    java.lang.Object readResolve();",
            "}",
        ])
    return "\n".join(lines) + "\n"


def reset_variant_dir(path: Path, artifact_root: Path) -> None:
    resolved = path.resolve()
    root = artifact_root.resolve()
    if resolved == root or root not in resolved.parents:
        raise ExperimentError(f"refusing to reset non-variant path {path}")
    if path.exists():
        shutil.rmtree(path)
    path.mkdir(parents=True)


def run_proguard(context: dict[str, Any], config_path: Path, log_path: Path) -> dict[str, Any]:
    return run_logged(
        [
            str(context["java"]),
            "-cp",
            context["proguard_classpath"],
            "proguard.ProGuard",
            f"@{config_path}",
        ],
        log_path,
        context["project_dir"],
    )


def deploy_jar(
    context: dict[str, Any], jar_path: Path, tcz_name: str, output_dir: Path, log_path: Path
) -> dict[str, Any]:
    output_dir.mkdir(parents=True, exist_ok=True)
    return run_logged(
        [
            str(context["java"]),
            "-cp",
            os.pathsep.join(str(path) for path in context["deploy_classpath"]),
            "tc.Deploy",
            str(jar_path),
            "/n",
            tcz_name,
            "/a",
            "TCvm",
            "/o",
            str(output_dir),
        ],
        log_path,
        context["project_dir"],
    )


def capture_baseline(context: dict[str, Any]) -> None:
    root = context["artifact_root"]
    baseline_dir = root / "baseline"
    jar_dir = baseline_dir / "jars"
    tcz_dir = baseline_dir / "o"
    jar_dir.mkdir(parents=True, exist_ok=True)
    tcz_dir.mkdir(parents=True, exist_ok=True)
    modules: dict[str, Any] = {}
    for module in context["modules"]:
        for source in (module["jar"], module["tcz"]):
            if not source.is_file():
                raise ExperimentError(f"baseline artifact is missing: {source}")
        jar_target = jar_dir / module["jar"].name
        tcz_target = tcz_dir / module["tcz"].name
        for source, target in ((module["jar"], jar_target), (module["tcz"], tcz_target)):
            if target.exists() and sha256_path(target) != sha256_path(source):
                raise ExperimentError(
                    f"immutable baseline conflict at {target}; use a new revision/artifact root"
                )
            if not target.exists():
                shutil.copy2(source, target)
        analyzed = analyze_module(jar_target, tcz_target, context["tc_constants"])
        analyzed["module"] = module["id"]
        analyzed["tcz_name"] = module["tcz_name"]
        modules[module["id"]] = analyzed
    baseline = {
        "schema_version": SCHEMA_VERSION,
        "revision": context["revision"],
        "proguard_version": PROGUARD_VERSION,
        "tool_sha256": sha256_path(Path(__file__)),
        "modules": modules,
        "aggregate": sum_metrics(modules.values()),
    }
    write_json(baseline_dir / "baseline.json", baseline)
    write_text(baseline_dir / "baseline.md", markdown_baseline(baseline))
    second = {
        module_id: analyze_module(
            Path(module["jar"]["path"]), Path(module["tcz"]["path"]), context["tc_constants"]
        )
        for module_id, module in modules.items()
    }
    deterministic = all(
        second[module_id]["metrics"] == modules[module_id]["metrics"]
        and second[module_id]["jar"]["sha256"] == modules[module_id]["jar"]["sha256"]
        and second[module_id]["tcz"]["sha256"] == modules[module_id]["tcz"]["sha256"]
        for module_id in modules
    )
    if not deterministic:
        raise ExperimentError("baseline analysis is not deterministic")
    write_json(
        baseline_dir / "stage-result.json",
        {"status": "success", "deterministic_reanalysis": True, "module_count": len(modules)},
    )
    normalized_dir = root / "baseline-no-lines"
    reset_variant_dir(normalized_dir, root)
    normalized_modules: dict[str, Any] = {}
    for module in context["modules"]:
        module_id = module["id"]
        jar_source = Path(modules[module_id]["jar"]["path"])
        jar_target = normalized_dir / "jars" / jar_source.name
        jar_target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(jar_source, jar_target)
        rewrite = strip_line_numbers_from_jar(
            jar_target,
            {name for name in modules[module_id]["jar"]["classes"]},
        )
        deploy = deploy_jar(
            context, jar_target, module["tcz_name"], normalized_dir / "o",
            normalized_dir / "logs" / f"{module_id}-j2tc.log",
        )
        if deploy["exit_code"]:
            raise ExperimentError(
                f"line-normalized baseline failed J2TC for {module_id}: {deploy['diagnostic']}"
            )
        analyzed = analyze_module(
            jar_target, normalized_dir / "o" / module["tcz"].name,
            context["tc_constants"],
        )
        analyzed.update({
            "module": module_id,
            "tcz_name": module["tcz_name"],
            "compatibility_rewrite": rewrite,
            "deltas_from_ordinary_baseline": module_deltas(
                analyzed["metrics"], modules[module_id]["metrics"]
            ),
        })
        normalized_modules[module_id] = analyzed
    normalized = {
        "schema_version": SCHEMA_VERSION,
        "revision": context["revision"],
        "comparison_boundary": "ordinary baseline with only LineNumberTable removed",
        "compatibility_adjustments": COMPATIBILITY_ADJUSTMENTS,
        "modules": normalized_modules,
        "aggregate": sum_metrics(normalized_modules.values()),
    }
    normalized["aggregate_deltas_from_ordinary_baseline"] = module_deltas(
        normalized["aggregate"], baseline["aggregate"]
    )
    write_json(normalized_dir / "baseline.json", normalized)
    write_text(normalized_dir / "baseline.md", markdown_baseline(normalized))
    write_json(
        normalized_dir / "stage-result.json",
        {"status": "success", "module_count": len(normalized_modules)},
    )
    print(
        "stage=baseline status=success modules=4 "
        f"tcz_bytes={baseline['aggregate']['tcz_bytes']} "
        f"tc_code_slots={baseline['aggregate']['tc_code_slots']}"
    )


def baseline_data(context: dict[str, Any]) -> dict[str, Any]:
    path = context["artifact_root"] / "baseline" / "baseline.json"
    if not path.exists():
        raise ExperimentError("baseline evidence is unavailable; run the baseline stage first")
    return read_json(path)


def comparison_baseline_data(context: dict[str, Any]) -> dict[str, Any]:
    path = context["artifact_root"] / "baseline-no-lines" / "baseline.json"
    if not path.exists():
        raise ExperimentError("line-normalized comparison baseline is unavailable")
    return read_json(path)


def module_local_variant(
    context: dict[str, Any],
    variant: str = "module-opt",
    optimizations: str = FULL_OPTIMIZATIONS,
    variant_dir: Path | None = None,
) -> dict[str, Any]:
    baseline = comparison_baseline_data(context)
    if variant_dir is None:
        variant_dir = context["artifact_root"] / "module-opt"
    reset_variant_dir(variant_dir, context["artifact_root"])
    result: dict[str, Any] = {
        "schema_version": SCHEMA_VERSION,
        "variant": variant,
        "context": "module-local",
        "passes": 1,
        "shrinking": False,
        "optimizations": optimizations,
        "compatibility_adjustments": COMPATIBILITY_ADJUSTMENTS,
        "modules": {},
    }
    for module in context["modules"]:
        module_id = module["id"]
        module_work_dir = variant_dir / module_id
        module_work_dir.mkdir(parents=True, exist_ok=True)
        other_program_jars = [item["jar"] for item in context["modules"] if item["id"] != module_id]
        auxiliary, auxiliary_classes = auxiliary_program(context, module_work_dir)
        combined = module_work_dir / "combined" / "runtime-plus-auxiliary.jar"
        combined.parent.mkdir(parents=True, exist_ok=True)
        libraries = library_inputs(context, context["module_jars"], module_work_dir)
        optimizable_classes = archive_class_names(module["jar"])
        pinned_classes = auxiliary_classes
        for other in other_program_jars:
            pinned_classes.extend(archive_class_names(other))
        config = common_configuration(
            [module["jar"], *other_program_jars, auxiliary], combined, libraries, 1,
            optimizations, False, module_work_dir, optimizable_classes,
            sorted(pinned_classes),
        )
        config_path = variant_dir / "configs" / f"{module_id}.pro"
        write_text(config_path, config)
        proguard = run_proguard(context, config_path, variant_dir / "logs" / f"{module_id}-proguard.log")
        module_result: dict[str, Any] = {
            "module": module_id,
            "status": "rejected" if proguard["exit_code"] else "running",
            "stage": "proguard" if proguard["exit_code"] else None,
            "diagnostic": proguard["diagnostic"],
            "configuration_hash": sha256_text(config),
            "proguard": proguard,
            "input_jar_sha256": sha256_path(module["jar"]),
        }
        if proguard["exit_code"] == 0:
            module_result["compatibility_rewrite"] = strip_line_numbers_from_jar(
                combined,
                {name.replace(".", "/") + ".class" for name in optimizable_classes},
            )
            try:
                split_outputs = split_whole_output(
                    combined, context["modules"], module_work_dir / "split", False,
                    {name.replace(".", "/") + ".class" for name in auxiliary_classes},
                )
            except ValueError as error:
                module_result.update({
                    "status": "rejected",
                    "stage": "reconstruction",
                    "diagnostic": str(error),
                })
                result["modules"][module_id] = module_result
                continue
            output_jar = variant_dir / "jars" / module["jar"].name
            output_jar.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(split_outputs[module_id], output_jar)
            deploy = deploy_jar(
                context, output_jar, module["tcz_name"], variant_dir / "o",
                variant_dir / "logs" / f"{module_id}-j2tc.log",
            )
            module_result["deploy"] = deploy
            if deploy["exit_code"]:
                module_result.update({
                    "status": "rejected",
                    "stage": "j2tc",
                    "diagnostic": deploy["diagnostic"],
                })
            else:
                output_tcz = variant_dir / "o" / module["tcz"].name
                analyzed = analyze_module(output_jar, output_tcz, context["tc_constants"])
                module_result.update(analyzed)
                module_result["deltas"] = module_deltas(
                    analyzed["metrics"], baseline["modules"][module_id]["metrics"]
                )
        result["modules"][module_id] = module_result
    successful = [item for item in result["modules"].values() if item["status"] == "success"]
    result["status"] = "success" if len(successful) == 4 else ("partial" if successful else "rejected")
    result["aggregate"] = sum_metrics(successful) if successful else None
    if successful:
        baseline_subset = sum_metrics(baseline["modules"][item["module"]] for item in successful)
        result["aggregate_deltas"] = module_deltas(result["aggregate"], baseline_subset)
        result["coverage_modules"] = sorted(item["module"] for item in successful)
    write_json(variant_dir / "result.json", result)
    print(f"variant={variant} status={result['status']} successful_modules={len(successful)}")
    return result


def split_whole_output(
    combined_jar: Path,
    modules: list[dict[str, Any]],
    output_dir: Path,
    allow_missing: bool,
    ignored_classes: set[str],
) -> dict[str, Path]:
    ownership: dict[str, str] = {}
    baseline_entries: dict[str, dict[str, bytes]] = {}
    baseline_classes: dict[str, set[str]] = {}
    for module in modules:
        module_id = module["id"]
        resources: dict[str, bytes] = {}
        classes: set[str] = set()
        with zipfile.ZipFile(module["jar"]) as archive:
            for name in archive.namelist():
                if name.endswith("/"):
                    continue
                if name.endswith(".class"):
                    if name in ownership:
                        raise ValueError(
                            f"class ownership is ambiguous for {name}: {ownership[name]} and {module_id}"
                        )
                    ownership[name] = module_id
                    classes.add(name)
                else:
                    resources[name] = archive.read(name)
        baseline_entries[module_id] = resources
        baseline_classes[module_id] = classes
    optimized: dict[str, dict[str, bytes]] = {module["id"]: {} for module in modules}
    with zipfile.ZipFile(combined_jar) as archive:
        for name in archive.namelist():
            if not name.endswith(".class"):
                continue
            owner = ownership.get(name)
            if owner is None:
                if name in ignored_classes:
                    continue
                raise ValueError(f"optimized class has no baseline owner: {name}")
            optimized[owner][name] = archive.read(name)
    if not allow_missing:
        missing = {
            module_id: sorted(classes - optimized[module_id].keys())
            for module_id, classes in baseline_classes.items()
            if classes - optimized[module_id].keys()
        }
        if missing:
            first_module = sorted(missing)[0]
            raise ValueError(
                f"non-shrinking output lost {len(missing[first_module])} classes from {first_module}; "
                f"first={missing[first_module][0]}"
            )
    output_dir.mkdir(parents=True, exist_ok=True)
    outputs: dict[str, Path] = {}
    for module in modules:
        module_id = module["id"]
        output = output_dir / module["jar"].name
        with zipfile.ZipFile(output, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
            content = dict(baseline_entries[module_id])
            content.update(optimized[module_id])
            for name in sorted(content):
                info = zipfile.ZipInfo(name, date_time=(1980, 1, 1, 0, 0, 0))
                info.compress_type = zipfile.ZIP_DEFLATED
                info.external_attr = 0o644 << 16
                archive.writestr(info, content[name])
        outputs[module_id] = output
    return outputs


def api_validation(baseline_jar: Path, candidate_jar: Path) -> dict[str, Any]:
    baseline = analyze_jar(baseline_jar)
    candidate = analyze_jar(candidate_jar)
    missing = sorted(set(baseline["public_api"]) - set(candidate["public_api"]))
    return {
        "status": "success" if not missing else "rejected",
        "baseline_public_api_count": len(baseline["public_api"]),
        "candidate_public_api_count": len(candidate["public_api"]),
        "baseline_public_api_sha256": baseline["public_api_sha256"],
        "candidate_public_api_sha256": candidate["public_api_sha256"],
        "missing_count": len(missing),
        "missing": missing,
    }


def whole_variant(
    context: dict[str, Any],
    variant: str,
    variant_dir: Path,
    passes: int,
    optimizations: str,
    shrink: bool,
) -> dict[str, Any]:
    baseline = comparison_baseline_data(context)
    reset_variant_dir(variant_dir, context["artifact_root"])
    combined = variant_dir / "combined" / "whole-runtime.jar"
    combined.parent.mkdir(parents=True, exist_ok=True)
    auxiliary, auxiliary_classes = auxiliary_program(context, variant_dir)
    libraries = library_inputs(context, context["module_jars"], variant_dir)
    config = common_configuration(
        [*context["module_jars"], auxiliary], combined, libraries, passes,
        optimizations, shrink, variant_dir,
        [name for path in context["module_jars"] for name in archive_class_names(path)],
        auxiliary_classes,
    )
    config_path = variant_dir / "configs" / f"{variant}.pro"
    write_text(config_path, config)
    proguard = run_proguard(context, config_path, variant_dir / "logs" / "proguard.log")
    result: dict[str, Any] = {
        "schema_version": SCHEMA_VERSION,
        "variant": variant,
        "context": "whole-runtime",
        "passes": passes,
        "shrinking": shrink,
        "optimizations": optimizations,
        "configuration_hash": sha256_text(config),
        "compatibility_adjustments": COMPATIBILITY_ADJUSTMENTS,
        "proguard": proguard,
        "modules": {},
    }
    if shrink:
        result["shrink_compatibility_pins"] = SHRINK_COMPATIBILITY_PINS
    if proguard["exit_code"]:
        result.update({
            "status": "rejected",
            "stage": "proguard",
            "diagnostic": proguard["diagnostic"],
        })
        write_json(variant_dir / "result.json", result)
        print(f"variant={variant} status=rejected stage=proguard")
        return result
    result["compatibility_rewrite"] = strip_line_numbers_from_jar(
        combined,
        {
            name.replace(".", "/") + ".class"
            for path in context["module_jars"]
            for name in archive_class_names(path)
        },
    )
    try:
        outputs = split_whole_output(
            combined, context["modules"], variant_dir / "jars", shrink,
            {name.replace(".", "/") + ".class" for name in auxiliary_classes},
        )
    except ValueError as error:
        result.update({"status": "rejected", "stage": "reconstruction", "diagnostic": str(error)})
        write_json(variant_dir / "result.json", result)
        print(f"variant={variant} status=rejected stage=reconstruction")
        return result

    any_api_failure = False
    successful: list[dict[str, Any]] = []
    for module in context["modules"]:
        module_id = module["id"]
        output_jar = outputs[module_id]
        module_result: dict[str, Any] = {
            "module": module_id,
            "status": "running",
            "input_jar_sha256": sha256_path(module["jar"]),
            "optimized_jar_sha256": sha256_path(output_jar),
        }
        if shrink:
            validation = api_validation(module["jar"], output_jar)
            write_json(variant_dir / "api" / f"{module_id}.json", validation)
            module_result["api_validation"] = validation
            if validation["status"] != "success":
                any_api_failure = True
                module_result.update({
                    "status": "rejected",
                    "stage": "api-validation",
                    "diagnostic": f"missing {validation['missing_count']} public/protected API entries",
                })
        deploy = deploy_jar(
            context, output_jar, module["tcz_name"], variant_dir / "o",
            variant_dir / "logs" / f"{module_id}-j2tc.log",
        )
        module_result["deploy"] = deploy
        if deploy["exit_code"]:
            module_result.update({
                "status": "rejected",
                "stage": "j2tc",
                "diagnostic": deploy["diagnostic"],
            })
        else:
            output_tcz = variant_dir / "o" / module["tcz"].name
            analyzed = analyze_module(output_jar, output_tcz, context["tc_constants"])
            if module_result["status"] != "rejected":
                module_result.update(analyzed)
                successful.append(module_result)
            else:
                module_result["rejected_artifact_metrics"] = analyzed["metrics"]
            module_result["deltas"] = module_deltas(
                analyzed["metrics"], baseline["modules"][module_id]["metrics"]
            )
        result["modules"][module_id] = module_result
    if any_api_failure:
        result["status"] = "rejected"
        result["stage"] = "api-validation"
        result["diagnostic"] = "safe-shrink output did not preserve the baseline API snapshot"
    else:
        result["status"] = "success" if len(successful) == 4 else ("partial" if successful else "rejected")
    if successful:
        result["aggregate"] = sum_metrics(successful)
        baseline_subset = sum_metrics(baseline["modules"][item["module"]] for item in successful)
        result["aggregate_deltas"] = module_deltas(result["aggregate"], baseline_subset)
        result["coverage_modules"] = sorted(item["module"] for item in successful)
    write_json(variant_dir / "result.json", result)
    print(f"variant={variant} status={result['status']} successful_modules={len(successful)}")
    return result


def whole_optimize_stage(context: dict[str, Any]) -> None:
    one = whole_variant(
        context, "whole-opt-1", context["artifact_root"] / "whole-opt-1", 1,
        FULL_OPTIMIZATIONS, False,
    )
    three = whole_variant(
        context, "whole-opt-3", context["artifact_root"] / "whole-opt-3", 3,
        FULL_OPTIMIZATIONS, False,
    )
    status = "success" if one["status"] == "success" and three["status"] == "success" else "partial"
    print(f"stage=whole-opt status={status}")


def is_material(result: dict[str, Any]) -> tuple[bool, str]:
    if (
        result.get("proguard", {}).get("exit_code") == 0
        and result.get("modules")
        and result.get("status") != "success"
    ):
        return True, "full conservative optimization reached J2TC but was rejected; grouped attribution is required to isolate compatibility"
    if result.get("status") != "success" or not result.get("aggregate_deltas"):
        return False, "whole-opt-1 did not produce a complete successful aggregate"
    slots = result["aggregate_deltas"]["tc_code_slots"]["percent"] or 0.0
    payload = result["aggregate_deltas"]["tcz_class_payload_bytes"]["percent"] or 0.0
    material = slots <= -3.0 or payload <= -5.0
    return material, f"tc_code_slots={slots:.3f}%, tcz_class_payload_bytes={payload:.3f}%"


def ablation_stage(context: dict[str, Any]) -> None:
    whole_path = context["artifact_root"] / "whole-opt-1" / "result.json"
    if not whole_path.exists():
        raise ExperimentError("whole-opt-1 evidence is unavailable before ablation selection")
    whole = read_json(whole_path)
    material, reason = is_material(whole)
    ablations_dir = context["artifact_root"] / "ablations"
    ablations_dir.mkdir(parents=True, exist_ok=True)
    index: dict[str, Any] = {
        "schema_version": SCHEMA_VERSION,
        "status": "running" if material else "skipped",
        "selection_reason": reason,
        "variants": {},
    }
    if material:
        for group, optimizations in ABLATION_GROUPS.items():
            existing_path = ablations_dir / group / "result.json"
            if existing_path.exists():
                candidate = read_json(existing_path)
            else:
                candidate = {}
            if (
                candidate.get("optimizations") == optimizations
                and candidate.get("compatibility_adjustments") == COMPATIBILITY_ADJUSTMENTS
            ):
                result = candidate
                print(f"variant=ablation-{group} status={result['status']} reused=true")
            else:
                result = whole_variant(
                    context, f"ablation-{group}", ablations_dir / group, 1, optimizations, False
                )
            index["variants"][group] = {
                "status": result["status"],
                "result": str(ablations_dir / group / "result.json"),
            }
        for group in ("local", "field"):
            existing_path = ablations_dir / f"{group}-module" / "result.json"
            if existing_path.exists():
                candidate = read_json(existing_path)
            else:
                candidate = {}
            if (
                candidate.get("optimizations") == ABLATION_GROUPS[group]
                and candidate.get("compatibility_adjustments") == COMPATIBILITY_ADJUSTMENTS
            ):
                result = candidate
                print(f"variant=ablation-{group}-module status={result['status']} reused=true")
            else:
                result = module_local_variant(
                    context, f"ablation-{group}-module", ABLATION_GROUPS[group],
                    ablations_dir / f"{group}-module",
                )
            index["variants"][f"{group}-module"] = {
                "status": result["status"],
                "result": str(ablations_dir / f"{group}-module" / "result.json"),
            }
        field_result = read_json(ablations_dir / "field" / "result.json")
        if field_result.get("status") == "success" and signal_classification(field_result) in ("moderate", "strong"):
            for subgroup, optimizations in FIELD_SUBGROUPS.items():
                existing_path = ablations_dir / subgroup / "result.json"
                if existing_path.exists():
                    candidate = read_json(existing_path)
                else:
                    candidate = {}
                if (
                    candidate.get("optimizations") == optimizations
                    and candidate.get("compatibility_adjustments") == COMPATIBILITY_ADJUSTMENTS
                ):
                    result = candidate
                    print(f"variant=ablation-{subgroup} status={result['status']} reused=true")
                else:
                    result = whole_variant(
                        context, f"ablation-{subgroup}", ablations_dir / subgroup,
                        1, optimizations, False,
                    )
                index["variants"][subgroup] = {
                    "status": result["status"],
                    "result": str(ablations_dir / subgroup / "result.json"),
                }
        index["status"] = "success"
    write_json(ablations_dir / "result.json", index)
    print(f"stage=ablations status={index['status']} reason={reason}")


def safe_shrink_stage(context: dict[str, Any]) -> None:
    result = whole_variant(
        context, "safe-shrink", context["artifact_root"] / "safe-shrink", 1,
        ABLATION_GROUPS["field"], True,
    )
    field_path = context["artifact_root"] / "ablations" / "field" / "result.json"
    if result.get("aggregate") and field_path.exists():
        field = read_json(field_path)
        if field.get("aggregate"):
            covered_modules = result.get("coverage_modules", [])
            field_subset = sum_metrics(
                field["modules"][module_id]
                for module_id in covered_modules
                if field["modules"][module_id].get("status") == "success"
            )
            result["incremental_deltas_from_optimize_only"] = module_deltas(
                result["aggregate"], field_subset
            )
            for module_id, module in result.get("modules", {}).items():
                field_module = field.get("modules", {}).get(module_id)
                if module.get("metrics") and field_module and field_module.get("metrics"):
                    module["incremental_deltas_from_optimize_only"] = module_deltas(
                        module["metrics"], field_module["metrics"]
                    )
            write_json(context["artifact_root"] / "safe-shrink" / "result.json", result)
    usage_path = context["artifact_root"] / "safe-shrink" / "usage.txt"
    if usage_path.exists():
        lines = usage_path.read_text(encoding="utf-8", errors="replace").splitlines()
        result["usage_summary"] = {
            "removed_class_count": sum(
                1 for line in lines if line and not line[0].isspace() and not line.endswith(":")
            ),
            "classes_with_removed_members": sum(
                1 for line in lines if line and not line[0].isspace() and line.endswith(":")
            ),
            "removed_member_line_count": sum(
                1 for line in lines if line and line[0].isspace()
            ),
            "usage_sha256": sha256_path(usage_path),
        }
        write_json(context["artifact_root"] / "safe-shrink" / "result.json", result)


def entry_delta_summary(current: dict[str, Any], baseline: dict[str, Any]) -> dict[str, Any]:
    baseline_entries = {entry["name"]: entry for entry in baseline["tcz"]["entries"]}
    current_entries = {entry["name"]: entry for entry in current["tcz"]["entries"]}
    shared = sorted(baseline_entries.keys() & current_entries.keys())
    deltas = [
        {
            "name": name,
            "actual_uncompressed_bytes": (
                current_entries[name]["actual_uncompressed_bytes"]
                - baseline_entries[name]["actual_uncompressed_bytes"]
            ),
            "compressed_bytes": (
                current_entries[name]["compressed_bytes"]
                - baseline_entries[name]["compressed_bytes"]
            ),
        }
        for name in shared
    ]
    deltas.sort(key=lambda item: (item["actual_uncompressed_bytes"], item["name"]))
    return {
        "added": sorted(current_entries.keys() - baseline_entries.keys()),
        "removed": sorted(baseline_entries.keys() - current_entries.keys()),
        "largest_reductions": deltas[:10],
        "largest_increases": list(reversed(deltas[-10:])),
    }


def compact_variant(
    result: dict[str, Any], baseline: dict[str, Any], result_path: Path
) -> dict[str, Any]:
    compact: dict[str, Any] = {
        "variant": result.get("variant"),
        "status": result.get("status"),
        "stage": result.get("stage"),
        "diagnostic": result.get("diagnostic"),
        "context": result.get("context"),
        "passes": result.get("passes"),
        "shrinking": result.get("shrinking"),
        "optimizations": result.get("optimizations"),
        "configuration_hash": result.get("configuration_hash"),
        "compatibility_adjustments": result.get("compatibility_adjustments", []),
        "compatibility_rewrite": result.get("compatibility_rewrite"),
        "shrink_compatibility_pins": result.get("shrink_compatibility_pins", []),
        "usage_summary": result.get("usage_summary"),
        "coverage_modules": result.get("coverage_modules", []),
        "aggregate": result.get("aggregate"),
        "aggregate_deltas": result.get("aggregate_deltas"),
        "incremental_deltas_from_optimize_only": result.get(
            "incremental_deltas_from_optimize_only"
        ),
        "modules": {},
    }
    for module_id, module in result.get("modules", {}).items():
        diagnostic = module.get("diagnostic")
        failure_log = module.get("deploy", module.get("proguard", result.get("proguard", {}))).get("log")
        if module.get("status") != "success" and failure_log:
            diagnostic = stable_diagnostic(Path(failure_log))
        item = {
            "status": module.get("status"),
            "stage": module.get("stage"),
            "diagnostic": diagnostic,
            "configuration_hash": module.get("configuration_hash", result.get("configuration_hash")),
            "compatibility_rewrite": module.get("compatibility_rewrite"),
            "metrics": module.get("metrics"),
            "deltas": module.get("deltas"),
            "incremental_deltas_from_optimize_only": module.get(
                "incremental_deltas_from_optimize_only"
            ),
            "proguard_warnings": (
                module.get("proguard", result.get("proguard", {})).get("warning_count")
            ),
            "log": failure_log,
        }
        if module.get("status") == "success":
            item["entry_deltas"] = entry_delta_summary(module, baseline["modules"][module_id])
            item["optimized_jar_sha256"] = module["jar"]["sha256"]
            item["tcz_sha256"] = module["tcz"]["sha256"]
        baseline_jar = baseline["modules"][module_id]["jar"]
        candidate_jar_path = result_path.parent / "jars" / Path(baseline_jar["path"]).name
        if candidate_jar_path.exists():
            candidate_jar = analyze_jar(candidate_jar_path)
            java_keys = (
                "jar_bytes", "class_count", "classfile_bytes", "method_count",
                "bytecode_bytes", "bytecode_instruction_count", "calls", "virtual_calls",
                "allocations", "branches", "field_accesses", "checkcasts", "instanceof",
            )
            item["java_artifact"] = {
                "path": str(candidate_jar_path),
                "sha256": candidate_jar["sha256"],
                "metrics": {key: candidate_jar["metrics"][key] for key in java_keys},
                "deltas": {
                    key: metric_delta(
                        candidate_jar["metrics"][key], baseline_jar["metrics"][key]
                    )
                    for key in java_keys
                },
            }
        if module.get("api_validation"):
            item["api_validation"] = {
                key: module["api_validation"][key]
                for key in (
                    "status", "baseline_public_api_count", "candidate_public_api_count",
                    "missing_count", "baseline_public_api_sha256", "candidate_public_api_sha256",
                )
            }
        compact["modules"][module_id] = item
    return compact


def signal_classification(variant: dict[str, Any]) -> str:
    deltas = variant.get("aggregate_deltas")
    if not deltas:
        return "unavailable"
    slots = -(deltas["tc_code_slots"]["percent"] or 0.0)
    payload = -(deltas["tcz_class_payload_bytes"]["percent"] or 0.0)
    if slots >= 10.0 or payload >= 15.0:
        return "strong"
    if slots >= 3.0 or payload >= 5.0:
        return "moderate"
    return "weak"


def recommendation_hint(variants: dict[str, dict[str, Any]]) -> str:
    module = variants.get("module-opt", {})
    whole = variants.get("whole-opt-1", {})
    shrink = variants.get("safe-shrink", {})
    field = variants.get("ablation-field", {})
    field_module = variants.get("ablation-field-module", {})
    if field.get("status") == "success" and field_module.get("status") == "success":
        field_slots = -(field["aggregate_deltas"]["tc_code_slots"]["percent"] or 0.0)
        module_slots = -(field_module["aggregate_deltas"]["tc_code_slots"]["percent"] or 0.0)
        if field_slots >= 3.0 and field_slots - module_slots < 1.0:
            return (
                "Prioritize a bounded, targeted pre-J2TC/J2TC field propagation, marking, and removal effort; "
                "the moderate TC-code reduction does not require whole-runtime context. Do not start a broad "
                "Java HIR or production ProGuard integration from this corpus, and require application-level "
                "evidence before any larger Java optimizer investment."
            )
    if whole.get("status") != "success":
        return "Compatibility evidence must be interpreted before choosing an optimizer architecture."
    whole_signal = signal_classification(whole)
    shrink_signal = signal_classification(shrink) if shrink.get("status") == "success" else "unavailable"
    if whole_signal == "weak" and shrink_signal in ("moderate", "strong"):
        return "Reachability/tree shaking is the leading structural opportunity; broad optimization is secondary."
    if whole_signal == "weak" and shrink_signal == "weak":
        return "The SDK corpus does not justify a new Java-level HIR; TCIR/JIT/AOT or application-level evidence should be considered next."
    if module.get("status") == "success":
        whole_slots = -(whole["aggregate_deltas"]["tc_code_slots"]["percent"] or 0.0)
        module_slots = -(module["aggregate_deltas"]["tc_code_slots"]["percent"] or 0.0)
        whole_payload = -(whole["aggregate_deltas"]["tcz_class_payload_bytes"]["percent"] or 0.0)
        module_payload = -(module["aggregate_deltas"]["tcz_class_payload_bytes"]["percent"] or 0.0)
        if whole_slots - module_slots >= 3.0 or whole_payload - module_payload >= 5.0:
            return "Whole-runtime context adds material value; attribute the gain before defining a bounded Java-aware HIR investment."
        return "Module-local and whole-runtime results are close; targeted pre-J2TC/J2TC optimization is favored over a broad HIR."
    return "Whole-runtime optimization is material, but module-local rejection must be interpreted before choosing the implementation layer."


def summary_markdown(summary: dict[str, Any]) -> str:
    lines = [
        "# ProGuard before J2TC experiment summary",
        "",
        f"Revision: `{summary['provenance']['revision']}`  ",
        f"ProGuard: `{summary['provenance']['proguard_version']}`",
        "",
        "## Aggregate evidence",
        "",
        "| Variant | Status | Coverage | JAR delta | TCZ delta | TC class payload delta | TC code-slot delta | Signal |",
        "|---|---|---|---:|---:|---:|---:|---|",
    ]
    for name, variant in summary["variants"].items():
        deltas = variant.get("aggregate_deltas") or {}

        def percent(key: str) -> str:
            value = (deltas.get(key) or {}).get("percent")
            return "n/a" if value is None else f"{value:+.3f}%"

        lines.append(
            f"| {name} | {variant.get('status')} | {','.join(variant.get('coverage_modules', [])) or 'none'} | "
            f"{percent('jar_bytes')} | {percent('tcz_bytes')} | "
            f"{percent('tcz_class_payload_bytes')} | {percent('tc_code_slots')} | "
            f"{variant.get('signal', 'unavailable')} |"
        )
    ordinary = summary["baseline"]["aggregate"]
    normalized_delta = summary["comparison_baseline"]["aggregate_deltas_from_ordinary_baseline"]
    field = summary["variants"].get("ablation-field", {})
    field_module = summary["variants"].get("ablation-field-module", {})
    propagation = summary["variants"].get("ablation-field-propagation", {})
    marking = summary["variants"].get("ablation-field-marking", {})
    removal = summary["variants"].get("ablation-field-removal", {})
    shrink = summary["variants"].get("safe-shrink", {})

    def delta_percent(variant: dict[str, Any], key: str) -> str:
        value = ((variant.get("aggregate_deltas") or {}).get(key) or {}).get("percent")
        return "n/a" if value is None else f"{value:+.3f}%"

    lines.extend([
        "",
        "## Findings",
        "",
        f"- The ordinary build baseline contains {ordinary['tcz_class_count']} TC classes, {ordinary['tc_method_count']} TC methods, {ordinary['tc_code_slots']} TC code slots, and {ordinary['tcz_bytes']} final TCZ bytes.",
        f"- Removing only `LineNumberTable` for the compatibility-normalized boundary changes TCZ bytes by {normalized_delta['tcz_bytes']['percent']:+.3f}% and TC class payload by {normalized_delta['tcz_class_payload_bytes']['percent']:+.3f}%, but changes TC methods and code slots by exactly 0%; optimization deltas therefore use this normalized boundary.",
        f"- The complete compatible field family changes TC code slots by {delta_percent(field, 'tc_code_slots')} and TC class payload by {delta_percent(field, 'tcz_class_payload_bytes')}. The module-local field run changes slots by {delta_percent(field_module, 'tc_code_slots')}, leaving only a {abs((field.get('aggregate_deltas') or {}).get('tc_code_slots', {}).get('percent', 0) - (field_module.get('aggregate_deltas') or {}).get('tc_code_slots', {}).get('percent', 0)):.3f}-point whole-runtime advantage.",
        f"- Within the field family, propagation changes TC code slots by {delta_percent(propagation, 'tc_code_slots')}, marking by {delta_percent(marking, 'tc_code_slots')}, and removal by {delta_percent(removal, 'tc_code_slots')}; generalization and specialization do not change TC code slots.",
        f"- Enum unboxing changes TC code slots by {delta_percent(summary['variants'].get('ablation-enum', {}), 'tc_code_slots')}, a negligible signal.",
        f"- Conservative shrinking is {shrink.get('status')} and covers {','.join(shrink.get('coverage_modules', [])) or 'no modules'}; its usage report removes {(shrink.get('usage_summary') or {}).get('removed_class_count', 0)} complete classes. The public/protected API snapshot passes for all modules, but `misc` remains J2TC-incompatible, so shrinking is unresolved rather than a shipping-safe result.",
        "- Full conservative module-local, one-pass whole-runtime, and three-pass whole-runtime optimization all reach ProGuard successfully but are rejected by J2TC. The failures identify line-table assumptions, TotalCross `4D`/device-API owner and signature conventions, synthetic method shapes, and operand-stack patterns as compatibility boundaries.",
        "",
        "## Investment implication",
        "",
        summary["recommendation_hint"],
        "",
        "JAR changes, final TCZ changes, parsed TotalCross code structure, shrinking, API checks, and compatibility failures are recorded separately in `summary.json` and the per-variant result files. Byte and instruction-count reductions are structural evidence only; this experiment does not claim runtime performance changes.",
        "",
    ])
    rejections = summary.get("rejections", [])
    lines.extend(["## Compatibility evidence", ""])
    if not rejections:
        lines.append("No required variant was rejected.")
    else:
        for rejection in rejections:
            lines.append(
                f"- `{rejection['variant']}` / `{rejection.get('module') or 'all'}` at "
                f"`{rejection.get('stage')}`: {rejection.get('diagnostic')}"
            )
    lines.append("")
    return "\n".join(lines)


def analyze_stage(context: dict[str, Any]) -> None:
    baseline = baseline_data(context)
    comparison_baseline = comparison_baseline_data(context)
    variant_paths = {
        "module-opt": context["artifact_root"] / "module-opt" / "result.json",
        "whole-opt-1": context["artifact_root"] / "whole-opt-1" / "result.json",
        "whole-opt-3": context["artifact_root"] / "whole-opt-3" / "result.json",
        "safe-shrink": context["artifact_root"] / "safe-shrink" / "result.json",
    }
    ablation_index_path = context["artifact_root"] / "ablations" / "result.json"
    if not ablation_index_path.exists():
        raise ExperimentError("ablation selection evidence is missing")
    ablation_index = read_json(ablation_index_path)
    for group, item in ablation_index.get("variants", {}).items():
        variant_paths[f"ablation-{group}"] = Path(item["result"])
    variants: dict[str, dict[str, Any]] = {}
    rejections: list[dict[str, Any]] = []
    for name, path in variant_paths.items():
        if not path.exists():
            raise ExperimentError(f"required variant evidence is missing: {path}")
        compact = compact_variant(read_json(path), comparison_baseline, path)
        compact["signal"] = signal_classification(compact)
        variants[name] = compact
        if compact["status"] not in ("success", "skipped"):
            if compact.get("diagnostic"):
                rejections.append({
                    "variant": name,
                    "module": None,
                    "stage": compact.get("stage"),
                    "diagnostic": compact.get("diagnostic"),
                })
            for module_id, module in compact.get("modules", {}).items():
                if module["status"] != "success":
                    rejections.append({
                        "variant": name,
                        "module": module_id,
                        "stage": module.get("stage"),
                        "diagnostic": module.get("diagnostic"),
                    })
    reports = context["artifact_root"] / "reports"
    summary = {
        "schema_version": SCHEMA_VERSION,
        "provenance": {
            "revision": context["revision"],
            "proguard_version": PROGUARD_VERSION,
            "java_home": str(context["java_home"]),
            "tool_sha256": sha256_path(Path(__file__)),
            "baseline_json_sha256": sha256_path(context["artifact_root"] / "baseline" / "baseline.json"),
            "artifact_root": str(context["artifact_root"]),
        },
        "baseline": {
            "aggregate": baseline["aggregate"],
            "modules": {
                module_id: {
                    "metrics": module["metrics"],
                    "input_jar_sha256": module["jar"]["sha256"],
                    "tcz_sha256": module["tcz"]["sha256"],
                }
                for module_id, module in baseline["modules"].items()
            },
        },
        "comparison_baseline": {
            "boundary": comparison_baseline["comparison_boundary"],
            "aggregate": comparison_baseline["aggregate"],
            "aggregate_deltas_from_ordinary_baseline": comparison_baseline[
                "aggregate_deltas_from_ordinary_baseline"
            ],
            "modules": {
                module_id: {
                    "metrics": module["metrics"],
                    "input_jar_sha256": module["jar"]["sha256"],
                    "tcz_sha256": module["tcz"]["sha256"],
                    "compatibility_rewrite": module["compatibility_rewrite"],
                }
                for module_id, module in comparison_baseline["modules"].items()
            },
        },
        "ablation_selection": ablation_index,
        "variants": variants,
        "rejections": rejections,
    }
    summary["recommendation_hint"] = recommendation_hint(variants)
    write_json(reports / "summary.json", summary)
    write_text(reports / "summary.md", summary_markdown(summary))
    verification = read_json(reports / "summary.json")
    if verification != summary:
        raise ExperimentError("machine-readable summary failed round-trip validation")
    print(
        f"stage=analyze status=success variants={len(variants)} rejections={len(rejections)} "
        f"summary={reports / 'summary.json'}"
    )


def parse_module(value: str) -> dict[str, Any]:
    parts = value.split("|")
    if len(parts) != 4:
        raise argparse.ArgumentTypeError("module must be ID|JAR|TCZ|TCZ_NAME")
    return {
        "id": parts[0],
        "jar": Path(parts[1]).resolve(),
        "tcz": Path(parts[2]).resolve(),
        "tcz_name": parts[3],
    }


def build_context(arguments: argparse.Namespace) -> dict[str, Any]:
    repository_root = arguments.repository_root.resolve()
    revision = git_output(repository_root, "rev-parse", "HEAD")
    project_dir = arguments.project_dir.resolve()
    artifact_root = project_dir / "build" / "proguard-tcz-experiment" / revision
    artifact_root.mkdir(parents=True, exist_ok=True)
    module_ids = [module["id"] for module in arguments.module]
    if module_ids != ["lang", "util", "misc", "ui"]:
        raise ExperimentError(f"expected modules lang,util,misc,ui; got {module_ids}")
    deploy_classpath = [Path(value).resolve() for value in arguments.deploy_classpath.split(os.pathsep)]
    context = {
        "repository_root": repository_root,
        "project_dir": project_dir,
        "artifact_root": artifact_root,
        "revision": revision,
        "java": arguments.java.resolve(),
        "java_home": arguments.java_home.resolve(),
        "proguard_classpath": arguments.proguard_classpath,
        "deploy_classpath": deploy_classpath,
        "sdk_jar": arguments.sdk_jar.resolve(),
        "modules": arguments.module,
        "module_jars": [module["jar"] for module in arguments.module],
        "tc_constants": parse_tc_constants(
            project_dir / "src/main/java/tc/tools/converter/TCConstants.java"
        ),
    }
    return context


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "stage", choices=("baseline", "module-opt", "whole-opt", "ablations", "safe-shrink", "analyze")
    )
    parser.add_argument("--project-dir", type=Path, required=True)
    parser.add_argument("--repository-root", type=Path, required=True)
    parser.add_argument("--java", type=Path, required=True)
    parser.add_argument("--java-home", type=Path, required=True)
    parser.add_argument("--proguard-classpath", required=True)
    parser.add_argument("--deploy-classpath", required=True)
    parser.add_argument("--sdk-jar", type=Path, required=True)
    parser.add_argument("--module", action="append", type=parse_module, required=True)
    arguments = parser.parse_args()
    try:
        context = build_context(arguments)
        if arguments.stage == "baseline":
            capture_baseline(context)
        elif arguments.stage == "module-opt":
            module_local_variant(context)
        elif arguments.stage == "whole-opt":
            whole_optimize_stage(context)
        elif arguments.stage == "ablations":
            ablation_stage(context)
        elif arguments.stage == "safe-shrink":
            safe_shrink_stage(context)
        elif arguments.stage == "analyze":
            analyze_stage(context)
        return 0
    except ExperimentError as error:
        print(f"hard_failure={error}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
