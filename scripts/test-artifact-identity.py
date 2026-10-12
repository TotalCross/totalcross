#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only
"""Tests for embedded build identity readers and package validation."""

from io import BytesIO
from pathlib import Path
import sys
import tarfile
import tempfile
import unittest
import zipfile
import zlib
import struct


sys.dont_write_bytecode = True

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "scripts" / "provenance"))

from artifact_identity import (  # noqa: E402
    RESOURCE_TCZ_NAMES,
    jar_identity_bytes,
    native_identity_stream,
    scan_downloaded_artifact,
    tcz_identity_bytes,
    validate_downloads,
    validate_identity,
)
from build_identity import compute_build_id  # noqa: E402


REPOSITORY = "totalcross/totalcross"
SOURCE_TREE = "a" * 40
BUILD_ID = compute_build_id(REPOSITORY, SOURCE_TREE)
OTHER_TREE = "b" * 40
OTHER_BUILD_ID = compute_build_id(REPOSITORY, OTHER_TREE)


def native_bytes(build_id=BUILD_ID, source_tree=SOURCE_TREE):
    return (
        b"native-code\0TOTALCROSS_BUILD_IDENTITY_V1:"
        + build_id.encode("ascii")
        + b"\0TOTALCROSS_SOURCE_TREE:"
        + source_tree.encode("ascii")
        + b"\0end\0"
    )


def write_jar(path, build_id=BUILD_ID, source_tree=SOURCE_TREE, identity_version="1"):
    manifest = (
        "Manifest-Version: 1.0\r\n"
        f"TotalCross-Build-Identity-Version: {identity_version}\r\n"
        f"TotalCross-Build-ID: {build_id[:48]}\r\n {build_id[48:]}\r\n"
        f"TotalCross-Source-Tree: {source_tree}\r\n\r\n"
    )
    path.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(path, "w") as jar:
        jar.writestr("META-INF/MANIFEST.MF", manifest)
        jar.writestr("sdk/fixture.class", b"class fixture")


def make_tcz(entries):
    compressed_entries = []
    names = []
    sizes = []
    offsets = [0]
    for name, payload in entries:
        compressed = zlib.compress(payload)
        compressed_entries.append(compressed)
        names.append(name.encode("utf-8"))
        sizes.append(len(payload))
        offsets.append(offsets[-1] + len(compressed))

    header = bytearray(struct.pack("<I", len(names)))
    header.extend(struct.pack(f"<{len(offsets)}I", *offsets))
    if sizes:
        header.extend(struct.pack(f"<{len(sizes)}I", *sizes))
    for name in names:
        header.append(len(name))
        header.extend(name)
    compressed_header = zlib.compress(bytes(header))
    base_offset = 8 + len(compressed_header)
    return (
        struct.pack("<HHI", 200, 0, base_offset)
        + compressed_header
        + b"".join(compressed_entries)
    )


def write_tcz(path, build_id=BUILD_ID, source_tree=SOURCE_TREE, identity_version="1"):
    metadata = (
        f"identityVersion={identity_version}\n"
        f"buildId={build_id}\n"
        f"sourceTree={source_tree}\n"
    ).encode("utf-8")
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(make_tcz([
        ("META-INF/totalcross-build.properties", metadata),
        ("fixture.class", b"class fixture"),
    ]))


def write_sdk(root, build_id=BUILD_ID, source_tree=SOURCE_TREE):
    dist = root / "dist"
    write_jar(dist / "totalcross-sdk-7.2.2.jar", build_id, source_tree)
    write_tcz(dist / "vm" / "TCUI.tcz", build_id, source_tree)
    for resource_name in RESOURCE_TCZ_NAMES:
        path = dist / "vm" / resource_name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(make_tcz([("font.dat", b"resource font")]))


def ios_ipa_bytes(executable):
    buffer = BytesIO()
    with zipfile.ZipFile(buffer, "w") as ipa:
        ipa.writestr("Payload/TotalCross.app/TotalCross", executable)
    return buffer.getvalue()


def write_ios_tarball(path):
    path.parent.mkdir(parents=True, exist_ok=True)
    with tarfile.open(path, "w:gz") as archive:
        app_executable = "TotalCross.xcarchive/Products/Applications/TotalCross.app/TotalCross"
        content = native_bytes()
        info = tarfile.TarInfo(app_executable)
        info.size = len(content)
        archive.addfile(info, BytesIO(content))

        ipa = ios_ipa_bytes(native_bytes())
        info = tarfile.TarInfo("TotalCross.ipa/TotalCross.ipa")
        info.size = len(ipa)
        archive.addfile(info, BytesIO(ipa))


def download_specs(root, **overrides):
    values = {
        "sdk": ("success", root / "sdk"),
        "linux-amd64": ("skipped", root / "linux-amd64"),
        "linux-arm32v7": ("skipped", root / "linux-arm32v7"),
        "linux-arm64": ("skipped", root / "linux-arm64"),
        "macos-arm64": ("skipped", root / "macos-arm64"),
        "windows": ("skipped", root / "windows"),
        "android": ("skipped", root / "android"),
        "ios": ("skipped", root / "ios"),
    }
    values.update(overrides)
    return [
        f"{name}={outcome}={path}"
        for name, (outcome, path) in values.items()
    ]


class ArtifactIdentityTests(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.root = Path(self.temporary.name)

    def tearDown(self):
        self.temporary.cleanup()

    def test_jar_manifest_identity_is_read(self):
        path = self.root / "sdk.jar"
        write_jar(path)
        row = jar_identity_bytes(path.read_bytes(), path)
        self.assertEqual(BUILD_ID, row["buildId"])
        self.assertEqual(SOURCE_TREE, row["sourceTree"])
        self.assertEqual("1", row["identityVersion"])

    def test_tcz_reserved_entry_identity_is_read(self):
        path = self.root / "sdk.tcz"
        write_tcz(path)
        row = tcz_identity_bytes(path.read_bytes(), path)
        self.assertEqual(BUILD_ID, row["buildId"])
        self.assertEqual(SOURCE_TREE, row["sourceTree"])
        self.assertEqual("1", row["identityVersion"])
        self.assertEqual(200, row["tczFormat"])

    def test_native_markers_can_cross_stream_chunk_boundaries(self):
        chunk_size = 65536
        source_tree = "c" * 64
        build_id = compute_build_id(REPOSITORY, source_tree)
        build_marker = (
            b"TOTALCROSS_BUILD_IDENTITY_V1:" + build_id.encode("ascii") + b"\0"
        )
        source_marker = (
            b"TOTALCROSS_SOURCE_TREE:" + source_tree.encode("ascii") + b"\0"
        )
        build_offset = chunk_size - 10
        source_offset = 2 * chunk_size - 10
        padding_size = source_offset - build_offset - len(build_marker)
        content = (
            b"x" * build_offset
            + build_marker
            + b"x" * padding_size
            + source_marker
            + b"end"
        )

        row = native_identity_stream(BytesIO(content), "fixture-native")
        self.assertIsNotNone(row)
        self.assertEqual(build_id, row["buildId"])
        self.assertEqual(source_tree, row["sourceTree"])

    def test_native_source_tree_accepts_sha1(self):
        row = native_identity_stream(BytesIO(native_bytes()), "fixture-native")
        self.assertEqual(SOURCE_TREE, row["sourceTree"])
        validate_identity(row, REPOSITORY, SOURCE_TREE)

    def test_native_source_tree_accepts_sha256_without_truncation(self):
        source_tree = "c" * 64
        build_id = compute_build_id(REPOSITORY, source_tree)
        row = native_identity_stream(
            BytesIO(native_bytes(build_id, source_tree)),
            "fixture-native",
        )
        self.assertEqual(source_tree, row["sourceTree"])
        validate_identity(row, REPOSITORY, source_tree)

    def test_native_identity_rejects_malformed_source_tree_lengths(self):
        for source_tree in ("a" * 39, "a" * 41, "a" * 63, "a" * 65):
            with self.subTest(length=len(source_tree)):
                with self.assertRaisesRegex(ValueError, "incomplete or conflicting"):
                    native_identity_stream(
                        BytesIO(native_bytes(BUILD_ID, source_tree)),
                        "fixture-native",
                    )

    def test_native_identity_rejects_invalid_marker_characters(self):
        invalid_source_tree = "a" * 20 + "g" + "a" * 19
        invalid_build_id = BUILD_ID[:20] + "G" + BUILD_ID[21:]
        for build_id, source_tree in (
            (BUILD_ID, invalid_source_tree),
            (invalid_build_id, SOURCE_TREE),
        ):
            with self.subTest(build_id=build_id, source_tree=source_tree):
                with self.assertRaisesRegex(ValueError, "incomplete or conflicting"):
                    native_identity_stream(
                        BytesIO(native_bytes(build_id, source_tree)),
                        "fixture-native",
                    )

    def test_native_identity_rejects_incomplete_markers(self):
        build_marker = b"TOTALCROSS_BUILD_IDENTITY_V1:" + BUILD_ID.encode("ascii") + b"\0"
        source_marker = b"TOTALCROSS_SOURCE_TREE:" + SOURCE_TREE.encode("ascii") + b"\0"
        for content in (build_marker, source_marker):
            with self.subTest(content=content):
                with self.assertRaisesRegex(ValueError, "incomplete or conflicting"):
                    native_identity_stream(BytesIO(content), "fixture-native")

    def test_native_identity_rejects_markers_without_nul_terminators(self):
        content = (
            b"TOTALCROSS_BUILD_IDENTITY_V1:"
            + BUILD_ID.encode("ascii")
            + b"\0TOTALCROSS_SOURCE_TREE:"
            + SOURCE_TREE.encode("ascii")
        )
        with self.assertRaisesRegex(ValueError, "incomplete or conflicting"):
            native_identity_stream(BytesIO(content), "fixture-native")

    def test_native_identity_rejects_invalid_marker_even_with_valid_duplicate(self):
        malformed = (
            b"\0TOTALCROSS_SOURCE_TREE:"
            + ("a" * 39 + "g").encode("ascii")
            + b"\0"
        )
        with self.assertRaisesRegex(ValueError, "incomplete or conflicting"):
            native_identity_stream(
                BytesIO(native_bytes() + malformed),
                "fixture-native",
            )

    def test_native_identity_rejects_marker_with_non_nul_boundary(self):
        malformed = (
            b"TOTALCROSS_SOURCE_TREE:"
            + SOURCE_TREE.encode("ascii")
            + b"!\0"
        )
        with self.assertRaisesRegex(ValueError, "incomplete or conflicting"):
            native_identity_stream(BytesIO(malformed), "fixture-native")

    def test_native_identity_rejects_conflicting_markers(self):
        conflicting_build_id = (
            b"\0TOTALCROSS_BUILD_IDENTITY_V1:"
            + OTHER_BUILD_ID.encode("ascii")
            + b"\0"
        )
        conflicting_source_tree = (
            b"\0TOTALCROSS_SOURCE_TREE:"
            + OTHER_TREE.encode("ascii")
            + b"\0"
        )
        for conflict in (conflicting_build_id, conflicting_source_tree):
            with self.subTest(conflict=conflict):
                with self.assertRaisesRegex(ValueError, "incomplete or conflicting"):
                    native_identity_stream(
                        BytesIO(native_bytes() + conflict),
                        "fixture-native",
                    )

    def test_native_identity_validation_rejects_inconsistent_markers(self):
        source_tree = "c" * 64
        row = native_identity_stream(
            BytesIO(native_bytes(BUILD_ID, source_tree)),
            "fixture-native",
        )
        with self.assertRaisesRegex(ValueError, "build ID mismatch"):
            validate_identity(row, REPOSITORY, source_tree)

    def test_generated_tcz_without_reserved_entry_is_detected(self):
        row = tcz_identity_bytes(make_tcz([("font.dat", b"resource")]), "fixture.tcz")
        self.assertIsNone(row["buildId"])

    def test_sdk_scan_requires_identity_on_generated_tcz_and_skips_resource_tcz(self):
        sdk = self.root / "sdk"
        write_sdk(sdk)
        rows = scan_downloaded_artifact("sdk", sdk)
        self.assertEqual(2, len(rows))
        self.assertEqual({"jar", "tcz"}, {row["kind"] for row in rows})
        self.assertFalse(any(Path(row["path"]).name.casefold() in RESOURCE_TCZ_NAMES for row in rows))

    def test_sdk_scan_rejects_missing_generated_tcz_identity(self):
        sdk = self.root / "sdk"
        write_sdk(sdk)
        write_tcz(sdk / "dist" / "vm" / "TCUI.tcz", "0" * 64)
        with self.assertRaisesRegex(ValueError, "build ID mismatch"):
            validate_downloads(
                download_specs(self.root, sdk=("success", sdk)),
                REPOSITORY,
                SOURCE_TREE,
            )

    def test_native_markers_are_required_for_each_platform_binary(self):
        linux = self.root / "linux"
        (linux / "build").mkdir(parents=True)
        (linux / "build" / "libtcvm.so").write_bytes(native_bytes())
        (linux / "build" / "Launcher").write_bytes(native_bytes())
        self.assertEqual(2, len(scan_downloaded_artifact("linux-amd64", linux)))
        (linux / "build" / "Launcher").write_bytes(b"native without markers")
        with self.assertRaisesRegex(ValueError, "expected identityVersion=1"):
            validate_downloads(
                download_specs(
                    self.root,
                    sdk=("success", self.root / "sdk"),
                    **{"linux-amd64": ("success", linux)},
                ),
                REPOSITORY,
                SOURCE_TREE,
            )

    def test_different_launcher_build_id_is_rejected_after_sdk_and_vm_pass(self):
        sdk = self.root / "sdk"
        write_sdk(sdk)
        linux = self.root / "linux-amd64"
        (linux / "build").mkdir(parents=True)
        (linux / "build" / "libtcvm.so").write_bytes(native_bytes())
        (linux / "build" / "Launcher").write_bytes(
            native_bytes(OTHER_BUILD_ID, SOURCE_TREE)
        )
        with self.assertRaisesRegex(ValueError, "build ID mismatch"):
            validate_downloads(
                download_specs(
                    self.root,
                    sdk=("success", sdk),
                    **{"linux-amd64": ("success", linux)},
                ),
                REPOSITORY,
                SOURCE_TREE,
            )

    def test_android_aab_reads_vm_identity_from_nested_zip(self):
        android = self.root / "android"
        android.mkdir()
        with zipfile.ZipFile(android / "TotalCross.aab", "w") as bundle:
            bundle.writestr("base/lib/arm64-v8a/libtcvm.so", native_bytes())
            bundle.writestr("base/lib/x86_64/libtcvm.so", native_bytes())
        rows = scan_downloaded_artifact("android", android)
        self.assertEqual(2, len(rows))
        self.assertTrue(all(row["buildId"] == BUILD_ID for row in rows))

    def test_ios_archive_and_ipa_executables_are_both_checked(self):
        ios = self.root / "ios"
        write_ios_tarball(ios / "ios.tar.gz")
        rows = scan_downloaded_artifact("ios", ios)
        self.assertEqual(2, len(rows))
        self.assertTrue(all(row["buildId"] == BUILD_ID for row in rows))

    def test_expected_repository_and_tree_are_checked_against_embedded_identity(self):
        sdk = self.root / "sdk"
        write_sdk(sdk)
        with self.assertRaisesRegex(ValueError, "build ID mismatch"):
            validate_downloads(download_specs(self.root, sdk=("success", sdk)),
                              "another/repository", SOURCE_TREE)
        with self.assertRaisesRegex(ValueError, "source tree mismatch"):
            validate_downloads(download_specs(self.root, sdk=("success", sdk)),
                              REPOSITORY, OTHER_TREE)

    def test_optional_downloads_are_skipped_but_successes_are_required(self):
        sdk = self.root / "sdk"
        write_sdk(sdk)
        rows = validate_downloads(download_specs(self.root, sdk=("success", sdk)),
                                  REPOSITORY, SOURCE_TREE)
        self.assertEqual(2, len(rows))

        incomplete = download_specs(self.root, sdk=("success", sdk))
        with self.assertRaisesRegex(ValueError, "missing download outcomes"):
            validate_downloads(incomplete[:-1], REPOSITORY, SOURCE_TREE)

        bad_linux = self.root / "linux-amd64"
        bad_linux.mkdir()
        with self.assertRaisesRegex(ValueError, "missing required native file"):
            scan_downloaded_artifact("linux-amd64", bad_linux)


if __name__ == "__main__":
    unittest.main()
