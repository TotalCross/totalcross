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
TCZ_IDENTITY_ENTRY = "META-INF/totalcross-build.properties"
RESOURCE_TCZ_NAMES = {"tcfont.tcz", "material icons.tcz"}

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
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        key = key.strip()
        if key in result:
            raise ValueError(f"duplicate TCZ build identity property: {key}")
        result[key] = value.strip()
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
            identity = {
                "kind": "tcz",
                "tczFormat": current_version,
                "buildId": props.get("buildId"),
                "sourceTree": props.get("sourceTree"),
                "identityVersion": props.get("identityVersion"),
                "path": str(path),
            }
    if identity is None:
        return {
            "kind": "tcz",
            "tczFormat": format_version,
            "buildId": None,
            "sourceTree": None,
            "identityVersion": None,
            "path": str(path),
        }
    return identity


def native_identity_stream(stream, path):
    build_ids = set()
    source_trees = set()
    markers = (
        (b"TOTALCROSS_BUILD_IDENTITY_V1:", BUILD_ID_RE, build_ids),
        (b"TOTALCROSS_SOURCE_TREE:", SOURCE_TREE_RE, source_trees),
    )
    max_prefix_length = max(len(prefix) for prefix, _, _ in markers)
    overlap = b""
    while True:
        chunk = stream.read(65536)
        data = overlap + chunk
        cursor = 0
        incomplete_marker = None
        while cursor < len(data):
            candidates = [
                (data.find(prefix, cursor), prefix, pattern, values)
                for prefix, pattern, values in markers
            ]
            candidates = [candidate for candidate in candidates if candidate[0] >= 0]
            if not candidates:
                break

            position, prefix, pattern, values = min(candidates, key=lambda item: item[0])
            match = pattern.match(data, position)
            if match is not None:
                values.add(match.group(1).decode("ascii"))
                cursor = match.end()
                continue

            value_start = position + len(prefix)
            terminator = data.find(b"\x00", value_start)
            if terminator >= 0 or len(data) - value_start > 64 or not chunk:
                raise ValueError(
                    f"native artifact has incomplete or conflicting identity markers: {path}"
                )
            incomplete_marker = position
            break

        if incomplete_marker is not None:
            overlap = data[incomplete_marker:]
        elif not chunk:
            break
        else:
            overlap = data[-(max_prefix_length - 1):]

    if not build_ids and not source_trees:
        return None
    if len(build_ids) != 1 or len(source_trees) != 1:
        raise ValueError(f"native artifact has incomplete or conflicting identity markers: {path}")
    return {
        "kind": "native",
        "buildId": next(iter(build_ids)),
        "sourceTree": next(iter(source_trees)),
        "identityVersion": "1",
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
    return rows
