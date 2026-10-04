#!/usr/bin/env python3
#
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

from io import BytesIO
from pathlib import Path, PurePosixPath
import re
import shutil
import struct
import tarfile
import tempfile
import zipfile
import zlib

from build_identity import compute_build_id, is_git_object_id, normalize_repository


BUILD_ID_RE = re.compile(
    rb"TOTALCROSS_BUILD_IDENTITY_V1:([0-9a-f]{64})\x00"
)
SOURCE_TREE_RE = re.compile(
    rb"TOTALCROSS_SOURCE_TREE:([0-9a-f]{64}|[0-9a-f]{40})\x00"
)
RUNTIME_ABI_MARKER = b"TOTALCROSS_RUNTIME_ABI:"
TCZ_IDENTITY_ENTRY = "META-INF/totalcross-build.properties"
RESOURCE_TCZ_NAMES = {"tcfont.tcz", "material icons.tcz"}
TCZ_IDENTITY_PROPERTIES = (
    "identityVersion",
    "buildId",
    "sourceTree",
    "tczFormat",
    "converterAbi",
    "runtimeAbi",
)

ARTIFACT_DOWNLOAD_NAMES = {
    "sdk",
    "linux-amd64",
    "linux-arm32v7",
    "linux-arm64",
    "macos-arm64",
    "windows",
    "android",
    "ios",
}
NATIVE_REQUIRED_NAMES = {
    "linux-amd64": ("libtcvm.so", "Launcher"),
    "linux-arm32v7": ("libtcvm.so", "Launcher"),
    "linux-arm64": ("libtcvm.so", "Launcher"),
    "macos-arm64": ("libtcvm.dylib", "Launcher"),
    "windows": ("tcvm.dll", "Launcher.exe"),
}
VALID_DOWNLOAD_OUTCOMES = {"success", "failure", "cancelled", "skipped"}


def parse_properties(data):
    result = {}
    for raw in data.decode("utf-8").splitlines():
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        if "=" not in line:
            raise ValueError(f"invalid TCZ build identity property: {line!r}")
        key, value = line.split("=", 1)
        key = key.strip()
        value = value.strip()
        if not key or not value:
            raise ValueError(f"invalid TCZ build identity property: {line!r}")
        if key in result:
            raise ValueError(f"duplicate TCZ build identity property: {key}")
        result[key] = value
    return result


def read_manifest_attributes(data):
    attrs = {}
    current = None
    for raw in data.decode("utf-8", errors="replace").splitlines():
        if raw.startswith(" ") and current:
            attrs[current] += raw[1:]
        elif ": " in raw:
            current, value = raw.split(": ", 1)
            key = current.casefold()
            if key in attrs:
                raise ValueError(f"duplicate JAR manifest attribute: {current}")
            attrs[key] = value
            current = key
        else:
            current = None
    return attrs


def jar_identity_bytes(data, path):
    with zipfile.ZipFile(BytesIO(data)) as jar:
        manifest_name = next(
            (name for name in jar.namelist() if name.casefold() == "meta-inf/manifest.mf"),
            None,
        )
        if manifest_name is None:
            raise ValueError(f"SDK JAR is missing META-INF/MANIFEST.MF: {path}")
        attrs = read_manifest_attributes(jar.read(manifest_name))
    return {
        "kind": "jar",
        "buildId": attrs.get("totalcross-build-id"),
        "sourceTree": attrs.get("totalcross-source-tree"),
        "identityVersion": attrs.get("totalcross-build-identity-version"),
        "tczFormat": attrs.get("totalcross-tcz-format"),
        "converterAbi": attrs.get("totalcross-converter-abi"),
        "runtimeAbi": attrs.get("totalcross-runtime-abi"),
        "path": str(path),
    }


def tcz_entries_bytes(data):
    if len(data) < 8:
        raise ValueError("TCZ is truncated")
    version, attributes, base_offset = struct.unpack_from("<HHI", data, 0)
    if base_offset < 8 or base_offset > len(data):
        raise ValueError(f"invalid TCZ base offset {base_offset}")
    header = zlib.decompress(data[8:base_offset])
    if len(header) < 4:
        raise ValueError("TCZ header is truncated")
    count = struct.unpack_from("<I", header, 0)[0]
    table_end = 4 + 4 * (count + 1) + 4 * count
    if table_end > len(header):
        raise ValueError("TCZ entry table is truncated")
    offsets = list(struct.unpack_from(f"<{count + 1}I", header, 4))
    sizes = list(struct.unpack_from(f"<{count}I", header, 4 + 4 * (count + 1)))
    pos = table_end
    names = []
    for _ in range(count):
        if pos >= len(header):
            raise ValueError("TCZ entry name table is truncated")
        length = header[pos]
        pos += 1
        if pos + length > len(header):
            raise ValueError("TCZ entry name is truncated")
        names.append(header[pos:pos + length].decode("utf-8", errors="replace"))
        pos += length

    compressed_data = data[base_offset:]
    if offsets[0] != 0 or offsets[-1] > len(compressed_data):
        raise ValueError("TCZ entry offsets are outside the data section")
    if any(left > right for left, right in zip(offsets, offsets[1:])):
        raise ValueError("TCZ entry offsets are not ordered")
    for index, name in enumerate(names):
        start, end = offsets[index], offsets[index + 1]
        payload = zlib.decompress(compressed_data[start:end])
        yield version, attributes, name, sizes[index], payload


def tcz_identity_bytes(data, path):
    identity = None
    format_version = None
    for current_version, _, name, _, payload in tcz_entries_bytes(data):
        format_version = current_version
        if name == TCZ_IDENTITY_ENTRY:
            if identity is not None:
                raise ValueError(f"duplicate TCZ build identity entry: {path}")
            props = parse_properties(payload)
            missing = [key for key in TCZ_IDENTITY_PROPERTIES if not props.get(key)]
            if missing:
                raise ValueError(
                    f"TCZ build identity is missing required properties in {path}: "
                    + ", ".join(missing)
                )
            if props["identityVersion"] != "1":
                raise ValueError(
                    f"unsupported TCZ build identity version in {path}: "
                    f"{props['identityVersion']!r}"
                )
            if not is_git_object_id(props["buildId"]) or len(props["buildId"]) != 64:
                raise ValueError(f"TCZ build identity has an invalid buildId: {path}")
            if not is_git_object_id(props["sourceTree"]):
                raise ValueError(f"TCZ build identity has an invalid sourceTree: {path}")
            metadata_format = _positive_epoch(props["tczFormat"])
            if metadata_format is None:
                raise ValueError(f"TCZ build identity has an invalid tczFormat: {path}")
            if metadata_format != str(current_version):
                raise ValueError(
                    f"TCZ format mismatch in {path}: physical header is "
                    f"{current_version}, embedded tczFormat is {metadata_format}"
                )
            for key in ("converterAbi", "runtimeAbi"):
                if _positive_epoch(props[key]) is None:
                    raise ValueError(
                        f"TCZ build identity has an invalid {key}: {path}"
                    )
            identity = {
                "kind": "tcz",
                "tczFormat": current_version,
                "buildId": props.get("buildId"),
                "sourceTree": props.get("sourceTree"),
                "identityVersion": props.get("identityVersion"),
                "converterAbi": props.get("converterAbi"),
                "runtimeAbi": props.get("runtimeAbi"),
                "path": str(path),
            }
    if identity is None:
        return {
            "kind": "tcz",
            "tczFormat": format_version,
            "buildId": None,
            "sourceTree": None,
            "identityVersion": None,
            "converterAbi": None,
            "runtimeAbi": None,
            "path": str(path),
        }
    return identity


class _RuntimeAbiScanner:
    """Read complete NUL-terminated runtime ABI tokens across stream chunks."""

    def __init__(self, path):
        self.path = path
        self.tail = b""
        self.pending_digits = None
        self.values = []

    def _record(self, digits):
        value = bytes(digits)
        if not value:
            raise ValueError(
                f"native artifact has a malformed runtime ABI marker: {self.path}"
            )
        normalized = value.lstrip(b"0")
        if not normalized:
            raise ValueError(
                f"native artifact has a nonpositive runtime ABI marker: {self.path}"
            )
        self.values.append(normalized.decode("ascii"))

    def feed(self, chunk):
        data = self.tail + chunk
        cursor = 0

        if self.pending_digits is not None:
            start = cursor
            while cursor < len(data) and 48 <= data[cursor] <= 57:
                cursor += 1
            self.pending_digits.extend(data[start:cursor])
            if cursor == len(data):
                return
            if data[cursor] != 0:
                raise ValueError(
                    f"native artifact has a malformed runtime ABI marker: {self.path}"
                )
            self._record(self.pending_digits)
            self.pending_digits = None
            cursor += 1

        while cursor < len(data):
            marker_start = data.find(RUNTIME_ABI_MARKER, cursor)
            if marker_start < 0:
                self.tail = data[-(len(RUNTIME_ABI_MARKER) - 1):]
                return

            digits_start = marker_start + len(RUNTIME_ABI_MARKER)
            cursor = digits_start
            while cursor < len(data) and 48 <= data[cursor] <= 57:
                cursor += 1
            digits = data[digits_start:cursor]

            if cursor == len(data):
                self.pending_digits = bytearray(digits)
                self.tail = b""
                return
            if data[cursor] != 0:
                raise ValueError(
                    f"native artifact has a malformed runtime ABI marker: {self.path}"
                )
            self._record(digits)
            cursor += 1

        self.tail = data[-(len(RUNTIME_ABI_MARKER) - 1):]

    def finish(self):
        if self.pending_digits is not None:
            raise ValueError(
                f"native artifact has a truncated runtime ABI marker: {self.path}"
            )
        return self.values


class _NativeIdentityMarkerScanner:
    """Read exact NUL-terminated build and source-tree markers in chunks."""

    def __init__(self, path):
        self.path = path
        self.build_ids = set()
        self.source_trees = set()
        self.markers = (
            (b"TOTALCROSS_BUILD_IDENTITY_V1:", BUILD_ID_RE, self.build_ids),
            (b"TOTALCROSS_SOURCE_TREE:", SOURCE_TREE_RE, self.source_trees),
        )
        self.max_prefix_length = max(len(prefix) for prefix, _, _ in self.markers)
        self.tail = b""

    def feed(self, chunk):
        data = self.tail + chunk
        cursor = 0
        incomplete_marker = None
        while cursor < len(data):
            candidates = [
                (data.find(prefix, cursor), prefix, pattern, values)
                for prefix, pattern, values in self.markers
            ]
            candidates = [candidate for candidate in candidates if candidate[0] >= 0]
            if not candidates:
                break

            position, prefix, pattern, values = min(
                candidates, key=lambda item: item[0]
            )
            match = pattern.match(data, position)
            if match is not None:
                values.add(match.group(1).decode("ascii"))
                cursor = match.end()
                continue

            value_start = position + len(prefix)
            terminator = data.find(b"\x00", value_start)
            if terminator >= 0 or len(data) - value_start > 64:
                raise ValueError(
                    f"native artifact has incomplete or conflicting identity markers: {self.path}"
                )
            incomplete_marker = position
            break

        if incomplete_marker is not None:
            self.tail = data[incomplete_marker:]
        else:
            self.tail = data[-(self.max_prefix_length - 1):]

    def finish(self):
        for prefix, _, _ in self.markers:
            position = self.tail.find(prefix)
            if position >= 0 and b"\x00" not in self.tail[position + len(prefix):]:
                raise ValueError(
                    f"native artifact has incomplete or conflicting identity markers: {self.path}"
                )
        return self.build_ids, self.source_trees


def native_identity_stream(stream, path):
    identity_scanner = _NativeIdentityMarkerScanner(path)
    runtime_scanner = _RuntimeAbiScanner(path)
    while True:
        chunk = stream.read(65536)
        if not chunk:
            break
        identity_scanner.feed(chunk)
        runtime_scanner.feed(chunk)

    build_ids, source_trees = identity_scanner.finish()
    runtime_abis = set(runtime_scanner.finish())
    if not build_ids and not source_trees and not runtime_abis:
        return None
    if len(build_ids) != 1 or len(source_trees) != 1:
        raise ValueError(f"native artifact has incomplete or conflicting identity markers: {path}")
    if not runtime_abis:
        raise ValueError(f"native artifact is missing its runtime ABI marker: {path}")
    if len(runtime_abis) > 1:
        raise ValueError(f"native artifact has conflicting runtime ABI markers: {path}")
    return {
        "kind": "native",
        "buildId": next(iter(build_ids)),
        "sourceTree": next(iter(source_trees)),
        "identityVersion": "1",
        "runtimeAbi": next(iter(runtime_abis)) if runtime_abis else None,
        "path": str(path),
    }


def validate_identity(row, repository, source_tree):
    normalized_repository = normalize_repository(repository)
    if normalized_repository is None:
        raise ValueError("expected repository identity is missing")
    if not is_git_object_id(source_tree):
        raise ValueError("expected source tree must be a 40- or 64-character Git object ID")
    if row.get("identityVersion") != "1":
        raise ValueError(
            f"{row['path']}: expected identityVersion=1, got {row.get('identityVersion')!r}"
        )
    if not is_git_object_id(row.get("buildId")) or len(row["buildId"]) != 64:
        raise ValueError(f"{row['path']}: missing or invalid embedded build ID")
    if not is_git_object_id(row.get("sourceTree")):
        raise ValueError(f"{row['path']}: missing or invalid embedded source tree")
    if row["sourceTree"] != source_tree:
        raise ValueError(
            f"{row['path']}: source tree mismatch, expected {source_tree}, "
            f"got {row['sourceTree']}"
        )
    expected_id = compute_build_id(normalized_repository, source_tree)
    if row["buildId"] != expected_id:
        raise ValueError(
            f"{row['path']}: build ID mismatch, expected {expected_id}, got {row['buildId']}"
        )


def _positive_epoch(value):
    if isinstance(value, bool):
        return None
    if isinstance(value, int):
        epoch = value
    elif isinstance(value, str) and re.fullmatch(r"[0-9]+", value):
        normalized = value.lstrip("0")
        return normalized if normalized else None
    else:
        return None
    return str(epoch) if epoch >= 1 else None


def validate_compatibility(rows):
    runtime_abis = set()
    converter_abis = set()
    tcz_formats = set()
    missing_runtime = []
    missing_converter = []
    missing_tcz_format = []

    for row in rows:
        runtime_abi = _positive_epoch(row.get("runtimeAbi"))
        if runtime_abi is None:
            missing_runtime.append(row["path"])
        else:
            runtime_abis.add(runtime_abi)

        if row.get("kind") in ("jar", "tcz"):
            converter_abi = _positive_epoch(row.get("converterAbi"))
            if converter_abi is None:
                missing_converter.append(row["path"])
            else:
                converter_abis.add(converter_abi)

            tcz_format = _positive_epoch(row.get("tczFormat"))
            if tcz_format is None:
                missing_tcz_format.append(row["path"])
            else:
                tcz_formats.add(tcz_format)

    if missing_runtime:
        raise ValueError("missing runtime ABI epoch: " + ", ".join(missing_runtime))
    if missing_converter:
        raise ValueError("missing converter ABI epoch: " + ", ".join(missing_converter))
    if missing_tcz_format:
        raise ValueError("missing TCZ format version: " + ", ".join(missing_tcz_format))
    if len(runtime_abis) != 1:
        raise ValueError("incompatible TotalCross runtime ABI epochs detected")
    if len(converter_abis) > 1:
        raise ValueError("incompatible TotalCross converter ABI epochs detected")
    if len(tcz_formats) > 1:
        raise ValueError("incompatible TotalCross TCZ format versions detected")

    return {
        "runtimeAbi": next(iter(runtime_abis)),
        "converterAbi": next(iter(converter_abis)) if converter_abis else None,
        "tczFormat": next(iter(tcz_formats)) if tcz_formats else None,
    }


def validate_artifact_set(rows, allow_mixed_builds=False):
    missing = [
        row["path"]
        for row in rows
        if not is_git_object_id(row.get("buildId"))
        or len(row["buildId"]) != 64
        or not is_git_object_id(row.get("sourceTree"))
        or row.get("identityVersion") != "1"
    ]
    if missing:
        raise ValueError(
            "missing or incomplete embedded build identity: " + ", ".join(missing)
        )

    compatibility = validate_compatibility(rows)
    if not allow_mixed_builds:
        build_ids = {row["buildId"] for row in rows}
        trees = {row["sourceTree"] for row in rows}
        if len(build_ids) != 1:
            raise ValueError("mixed TotalCross Build IDs detected")
        if len(trees) != 1:
            raise ValueError("mixed TotalCross source trees detected")
    return compatibility


def _identity_from_file(path):
    suffix = path.suffix.casefold()
    if suffix == ".jar":
        return jar_identity_bytes(path.read_bytes(), path)
    if suffix == ".tcz":
        return tcz_identity_bytes(path.read_bytes(), path)
    with path.open("rb") as source:
        identity = native_identity_stream(source, path)
    if identity is None:
        return {
            "kind": "native",
            "buildId": None,
            "sourceTree": None,
            "identityVersion": None,
            "runtimeAbi": None,
            "path": str(path),
        }
    return identity


def artifact_identity(path):
    """Read identity metadata from a single JAR, TCZ, or native binary."""
    return _identity_from_file(Path(path))


def _scan_sdk(root):
    distribution = root / "dist"
    jar_files = sorted(
        path
        for path in distribution.glob("totalcross-sdk*.jar")
        if "-sources" not in path.name.casefold()
        and "-javadoc" not in path.name.casefold()
    )
    if not jar_files:
        raise ValueError(f"SDK artifact is missing totalcross-sdk JAR under {distribution}")
    tcz_directory = distribution / "vm"
    tcz_files = sorted(
        path
        for path in tcz_directory.glob("*.tcz")
        if path.name.casefold() not in RESOURCE_TCZ_NAMES
    )
    if not tcz_files:
        raise ValueError(f"SDK artifact is missing generated TCZ files under {tcz_directory}")
    return [_identity_from_file(path) for path in [*jar_files, *tcz_files]]


def _scan_native_artifact(name, root):
    expected_names = NATIVE_REQUIRED_NAMES[name]
    expected_name_set = {item.casefold() for item in expected_names}
    candidates = [
        path
        for path in root.rglob("*")
        if path.is_file() and path.name.casefold() in expected_name_set
    ]
    rows = []
    for expected in expected_names:
        matches = [path for path in candidates if path.name.casefold() == expected.casefold()]
        if not matches:
            raise ValueError(f"{name} artifact is missing required native file {expected}")
        rows.extend(_identity_from_file(path) for path in matches)
    return rows


def _scan_android(root):
    bundles = sorted(
        path
        for path in root.rglob("*")
        if path.is_file() and path.name.casefold() == "totalcross.aab"
    )
    if not bundles:
        raise ValueError(f"Android artifact is missing TotalCross.aab under {root}")
    rows = []
    for bundle in bundles:
        with zipfile.ZipFile(bundle) as archive:
            for info in archive.infolist():
                if info.is_dir() or PurePosixPath(info.filename).name.casefold() != "libtcvm.so":
                    continue
                with archive.open(info) as stream:
                    identity = native_identity_stream(stream, f"{bundle}!{info.filename}")
                if identity is None:
                    raise ValueError(
                        f"Android VM is missing embedded build identity: {bundle}!{info.filename}"
                    )
                rows.append(identity)
    if not rows:
        raise ValueError(f"Android AAB has no libtcvm.so entries with build identity: {root}")
    return rows


def _is_app_executable(path):
    parts = [part for part in PurePosixPath(path).parts if part not in (".", "..")]
    return (
        bool(parts)
        and parts[-1].casefold() == "totalcross"
        and any(part.casefold().endswith(".app") for part in parts[:-1])
    )


def _scan_ipa_stream(stream, display_path):
    rows = []
    with zipfile.ZipFile(stream) as archive:
        for info in archive.infolist():
            if info.is_dir() or not _is_app_executable(info.filename):
                continue
            with archive.open(info) as executable:
                identity = native_identity_stream(
                    executable,
                    f"{display_path}!{info.filename}",
                )
            if identity is None:
                raise ValueError(
                    f"iOS app executable is missing build identity: {display_path}!{info.filename}"
                )
            rows.append(identity)
    return rows


def _scan_ios(root):
    archives = sorted(
        path
        for path in root.rglob("*")
        if path.is_file() and path.name == "ios.tar.gz"
    )
    if not archives:
        raise ValueError(f"iOS artifact is missing ios.tar.gz under {root}")
    rows = []
    for archive_path in archives:
        archive_rows = []
        ipa_rows = []
        with tarfile.open(archive_path, "r:*") as archive:
            for member in archive:
                if not member.isfile():
                    continue
                parts = [
                    part
                    for part in PurePosixPath(member.name).parts
                    if part not in (".", "..")
                ]
                if not parts:
                    continue
                if parts[0] not in {"TotalCross.xcarchive", "TotalCross.ipa"}:
                    continue
                source = archive.extractfile(member)
                if source is None:
                    continue
                display_path = f"{archive_path}!{member.name}"
                if (
                    parts[0] == "TotalCross.ipa"
                    and member.name.casefold().endswith(".ipa")
                ):
                    with source:
                        with tempfile.TemporaryFile() as ipa:
                            shutil.copyfileobj(source, ipa)
                            ipa.seek(0)
                            ipa_rows.extend(_scan_ipa_stream(ipa, display_path))
                elif _is_app_executable(member.name):
                    with source:
                        identity = native_identity_stream(source, display_path)
                    if identity is None:
                        raise ValueError(
                            f"iOS app executable is missing build identity: {display_path}"
                        )
                    if parts[0] == "TotalCross.xcarchive":
                        archive_rows.append(identity)
                    else:
                        ipa_rows.append(identity)
        if not archive_rows:
            raise ValueError(
                f"iOS xcarchive is missing the TotalCross app executable identity: {archive_path}"
            )
        if not ipa_rows:
            raise ValueError(
                f"iOS IPA is missing the TotalCross app executable identity: {archive_path}"
            )
        rows.extend(archive_rows)
        rows.extend(ipa_rows)
    return rows


def scan_downloaded_artifact(name, root):
    root = Path(root)
    if not root.exists():
        raise ValueError(f"downloaded {name} artifact root does not exist: {root}")
    if name == "sdk":
        if not root.is_dir():
            raise ValueError(f"SDK artifact root is not a directory: {root}")
        return _scan_sdk(root)
    if name in NATIVE_REQUIRED_NAMES:
        if not root.is_dir():
            raise ValueError(f"{name} artifact root is not a directory: {root}")
        return _scan_native_artifact(name, root)
    if name == "android":
        if not root.is_dir():
            raise ValueError(f"Android artifact root is not a directory: {root}")
        return _scan_android(root)
    if name == "ios":
        if not root.is_dir():
            raise ValueError(f"iOS artifact root is not a directory: {root}")
        return _scan_ios(root)
    raise ValueError(f"unsupported build artifact identity group: {name!r}")


def parse_artifact_downloads(specifications):
    downloads = {}
    for specification in specifications:
        parts = specification.split("=", 2)
        if len(parts) != 3 or not all(parts):
            raise ValueError(
                "invalid artifact download; expected NAME=OUTCOME=ROOT, "
                f"got {specification!r}"
            )
        name, outcome, root = parts
        if name not in ARTIFACT_DOWNLOAD_NAMES:
            raise ValueError(f"unsupported build artifact identity group: {name!r}")
        if outcome not in VALID_DOWNLOAD_OUTCOMES:
            raise ValueError(f"invalid download outcome for {name!r}: {outcome!r}")
        if name in downloads:
            raise ValueError(f"duplicate artifact download entry: {name!r}")
        downloads[name] = (outcome, Path(root))
    missing = ARTIFACT_DOWNLOAD_NAMES - set(downloads)
    if missing:
        raise ValueError(
            "artifact identity validation is missing download outcomes for: "
            + ", ".join(sorted(missing))
        )
    if downloads["sdk"][0] != "success":
        raise ValueError("required SDK build artifact was not downloaded")
    return downloads


def validate_downloads(specifications, repository, source_tree):
    downloads = parse_artifact_downloads(specifications)
    rows = []
    for name in sorted(downloads):
        outcome, root = downloads[name]
        if outcome != "success":
            continue
        artifact_rows = scan_downloaded_artifact(name, root)
        if not artifact_rows:
            raise ValueError(f"downloaded {name} artifact contains no identity-bearing payloads")
        for row in artifact_rows:
            validate_identity(row, repository, source_tree)
        rows.extend(artifact_rows)
    if not rows:
        raise ValueError("no embedded build identities were found in downloaded artifacts")
    validate_artifact_set(rows)
    return rows
