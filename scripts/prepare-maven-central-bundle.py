#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda.
# SPDX-License-Identifier: LGPL-2.1-only

from __future__ import annotations

import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import tempfile
import zipfile
import xml.etree.ElementTree as ET


GROUP_ID = "com.totalcross"
ARTIFACT_ID = "totalcross-sdk"
MAVEN_PATH = Path("com/totalcross/totalcross-sdk")
CHECKSUMS = ("md5", "sha1", "sha256", "sha512")
PRIMARY_KINDS = ("pom", "jar", "sources", "javadoc")
EXPECTED_FILE_COUNT = len(PRIMARY_KINDS) * (2 + len(CHECKSUMS))
FIXED_ZIP_TIME = (1980, 1, 1, 0, 0, 0)


def digest(path: Path, algorithm: str) -> str:
    hasher = hashlib.new(algorithm)
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(1024 * 1024), b""):
            hasher.update(chunk)
    return hasher.hexdigest()


def run(command: list[str], *, input_text: str | None = None) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        command,
        input=input_text,
        text=True,
        check=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )


def assert_jar(path: Path) -> None:
    with zipfile.ZipFile(path) as archive:
        bad = archive.testzip()
        if bad is not None:
            raise RuntimeError(f"{path} has a bad ZIP entry: {bad}")


def validate_classifier_contents(kind: str, path: Path) -> None:
    with zipfile.ZipFile(path) as archive:
        names = [name for name in archive.namelist() if not name.endswith("/")]
    if kind == "sources" and not any(name.endswith(".java") for name in names):
        raise RuntimeError(f"{path} does not contain Java sources")
    if kind == "javadoc" and "index.html" not in names:
        raise RuntimeError(f"{path} does not contain index.html")


def xml_namespace(root: ET.Element) -> dict[str, str]:
    match = re.match(r"\{([^}]+)\}", root.tag)
    return {"m": match.group(1)} if match else {}


def child_text(parent: ET.Element, path: str, ns: dict[str, str]) -> str:
    node = parent.find(path, ns)
    return (node.text or "").strip() if node is not None else ""


def validate_pom(path: Path, version: str) -> None:
    root = ET.parse(path).getroot()
    ns = xml_namespace(root)
    prefix = "m:" if ns else ""

    expected = {
        "groupId": GROUP_ID,
        "artifactId": ARTIFACT_ID,
        "version": version,
    }
    for field, value in expected.items():
        actual = child_text(root, f"{prefix}{field}", ns)
        if actual != value:
            raise RuntimeError(f"POM {field} is {actual!r}, expected {value!r}")

    # Maven's default packaging is "jar"; Gradle omits the element for that
    # default even when the publication is explicitly configured as a JAR.
    packaging = child_text(root, f"{prefix}packaging", ns) or "jar"
    if packaging != "jar":
        raise RuntimeError(f"POM packaging is {packaging!r}, expected 'jar'")

    for field in ("name", "description", "url"):
        if not child_text(root, f"{prefix}{field}", ns):
            raise RuntimeError(f"POM is missing required {field}")

    licenses = root.findall(f"{prefix}licenses/{prefix}license", ns)
    if not licenses:
        raise RuntimeError("POM is missing licenses")
    for license_node in licenses:
        if not child_text(license_node, f"{prefix}name", ns):
            raise RuntimeError("POM license is missing name")
        if not child_text(license_node, f"{prefix}url", ns):
            raise RuntimeError("POM license is missing url")

    developers = root.findall(f"{prefix}developers/{prefix}developer", ns)
    if not developers:
        raise RuntimeError("POM is missing developers")
    if not any(child_text(developer, f"{prefix}name", ns) for developer in developers):
        raise RuntimeError("POM developer is missing name")

    scm = root.find(f"{prefix}scm", ns)
    if scm is None:
        raise RuntimeError("POM is missing scm")
    for field in ("connection", "developerConnection", "url"):
        if not child_text(scm, f"{prefix}{field}", ns):
            raise RuntimeError(f"POM scm is missing {field}")

    runtime_artifact = f"{prefix}dependencies/{prefix}dependency/{prefix}artifactId"
    for dependency in root.findall(runtime_artifact, ns):
        if (dependency.text or "").strip() == "totalcross-runtime-java":
            raise RuntimeError(
                "POM must not depend on unpublished com.totalcross:totalcross-runtime-java"
            )


def extract_embedded_pom(main_jar: Path, pom_path: Path) -> None:
    embedded = f"META-INF/maven/{GROUP_ID}/{ARTIFACT_ID}/pom.xml"
    with zipfile.ZipFile(main_jar) as archive:
        try:
            pom_bytes = archive.read(embedded)
        except KeyError as exc:
            raise RuntimeError(f"{main_jar} does not contain {embedded}") from exc
    pom_path.write_bytes(pom_bytes)


def manifest_source_commit(main_jar: Path) -> str:
    with zipfile.ZipFile(main_jar) as archive:
        try:
            raw = archive.read("META-INF/MANIFEST.MF").decode("utf-8", errors="replace")
        except KeyError:
            return ""

    logical_lines: list[str] = []
    for line in raw.replace("\r\n", "\n").split("\n"):
        if line.startswith(" ") and logical_lines:
            logical_lines[-1] += line[1:]
        else:
            logical_lines.append(line)

    for line in logical_lines:
        if line.startswith("TotalCross-Source-Commit:"):
            return line.split(":", 1)[1].strip()
    return ""


def sign(path: Path, signature: Path, fingerprint: str, passphrase: str) -> None:
    command = [
        "gpg",
        "--batch",
        "--yes",
        "--pinentry-mode",
        "loopback",
        "--passphrase-fd",
        "0",
        "--local-user",
        fingerprint,
        "--armor",
        "--detach-sign",
        "--output",
        str(signature),
        str(path),
    ]
    run(command, input_text=passphrase + "\n")


def verify_signature(path: Path, signature: Path, fingerprint: str) -> None:
    result = run(
        [
            "gpg",
            "--batch",
            "--status-fd",
            "1",
            "--verify",
            str(signature),
            str(path),
        ]
    )
    valid_fingerprints = []
    for line in result.stdout.splitlines():
        if line.startswith("[GNUPG:] VALIDSIG "):
            fields = line.split()
            if len(fields) >= 3:
                valid_fingerprints.append(fields[2].upper())
    if fingerprint.upper() not in valid_fingerprints:
        raise RuntimeError(
            f"Signature for {path.name} did not validate with {fingerprint}; "
            f"got {valid_fingerprints}"
        )


def primary_paths(version_dir: Path, version: str) -> dict[str, Path]:
    prefix = f"{ARTIFACT_ID}-{version}"
    return {
        "pom": version_dir / f"{prefix}.pom",
        "jar": version_dir / f"{prefix}.jar",
        "sources": version_dir / f"{prefix}-sources.jar",
        "javadoc": version_dir / f"{prefix}-javadoc.jar",
    }


def validate_staging(version_dir: Path, version: str, fingerprint: str) -> None:
    primaries = primary_paths(version_dir, version)
    expected_names: set[str] = set()

    for kind, primary in primaries.items():
        if not primary.is_file():
            raise RuntimeError(f"Missing primary artifact: {primary}")
        expected_names.add(primary.name)

        signature = Path(str(primary) + ".asc")
        if not signature.is_file():
            raise RuntimeError(f"Missing signature: {signature}")
        expected_names.add(signature.name)
        verify_signature(primary, signature, fingerprint)

        for algorithm in CHECKSUMS:
            checksum = Path(str(primary) + f".{algorithm}")
            if not checksum.is_file():
                raise RuntimeError(f"Missing checksum: {checksum}")
            expected_names.add(checksum.name)
            actual = checksum.read_text(encoding="ascii").strip().lower()
            expected = digest(primary, algorithm)
            if actual != expected:
                raise RuntimeError(
                    f"{checksum.name} is {actual}, expected {expected}"
                )

        if kind in ("jar", "sources", "javadoc"):
            assert_jar(primary)
        if kind in ("sources", "javadoc"):
            validate_classifier_contents(kind, primary)

    validate_pom(primaries["pom"], version)

    actual_names = {path.name for path in version_dir.iterdir() if path.is_file()}
    if actual_names != expected_names:
        missing = sorted(expected_names - actual_names)
        extra = sorted(actual_names - expected_names)
        raise RuntimeError(f"Unexpected staging contents: missing={missing}, extra={extra}")
    if len(actual_names) != EXPECTED_FILE_COUNT:
        raise RuntimeError(
            f"Expected {EXPECTED_FILE_COUNT} files, found {len(actual_names)}"
        )


def deterministic_zip(source_root: Path, bundle: Path) -> None:
    bundle.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(
        bundle, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9
    ) as archive:
        for path in sorted(source_root.rglob("*")):
            if not path.is_file():
                continue
            relative = path.relative_to(source_root).as_posix()
            info = zipfile.ZipInfo(relative, FIXED_ZIP_TIME)
            info.create_system = 3
            info.external_attr = 0o100644 << 16
            info.compress_type = zipfile.ZIP_DEFLATED
            archive.writestr(info, path.read_bytes(), compresslevel=9)


def validate_bundle(bundle: Path, version: str, fingerprint: str) -> None:
    with zipfile.ZipFile(bundle) as archive:
        bad = archive.testzip()
        if bad is not None:
            raise RuntimeError(f"Bundle has a bad ZIP entry: {bad}")
        names = [name for name in archive.namelist() if not name.endswith("/")]
        if len(names) != EXPECTED_FILE_COUNT:
            raise RuntimeError(
                f"Bundle expected {EXPECTED_FILE_COUNT} files, found {len(names)}"
            )

    with tempfile.TemporaryDirectory(prefix="totalcross-central-validate-") as temp:
        root = Path(temp)
        with zipfile.ZipFile(bundle) as archive:
            archive.extractall(root)
        validate_staging(root / MAVEN_PATH / version, version, fingerprint)


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Prepare and validate a Maven Central bundle for totalcross-sdk."
    )
    parser.add_argument("--version", required=True)
    parser.add_argument("--jars-dir", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path, required=True)
    parser.add_argument("--gpg-fingerprint", required=True)
    parser.add_argument("--expected-source-commit")
    parser.add_argument("--expected-source-ancestor")
    parser.add_argument("--max-bundle-bytes", type=int, default=200_000_000)
    args = parser.parse_args()

    if not re.fullmatch(r"[0-9]+\.[0-9]+\.[0-9]+", args.version):
        raise RuntimeError(f"Invalid release version: {args.version}")

    passphrase = os.environ.get("MAVEN_GPG_PASSPHRASE", "")
    output_dir = args.output_dir.resolve()
    if output_dir.exists():
        shutil.rmtree(output_dir)
    repository_root = output_dir / "repository"
    version_dir = repository_root / MAVEN_PATH / args.version
    version_dir.mkdir(parents=True)

    prefix = f"{ARTIFACT_ID}-{args.version}"
    source_main = args.jars_dir / f"{prefix}.jar"
    source_sources = args.jars_dir / f"{prefix}-sources.jar"
    source_javadoc = args.jars_dir / f"{prefix}-javadoc.jar"
    for source in (source_main, source_sources, source_javadoc):
        if not source.is_file():
            raise RuntimeError(f"Missing release artifact: {source}")
        assert_jar(source)
    validate_classifier_contents("sources", source_sources)
    validate_classifier_contents("javadoc", source_javadoc)

    actual_commit = manifest_source_commit(source_main)
    if not actual_commit:
        raise RuntimeError(
            f"{source_main} has no TotalCross-Source-Commit manifest attribute"
        )

    if args.expected_source_commit and actual_commit != args.expected_source_commit:
        raise RuntimeError(
            f"Packaged SDK source commit {actual_commit} does not match "
            f"release commit {args.expected_source_commit}"
        )

    if args.expected_source_ancestor:
        commit_exists = subprocess.run(
            ["git", "cat-file", "-e", actual_commit + "^{commit}"],
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
        )
        if commit_exists.returncode != 0:
            raise RuntimeError(
                f"Packaged SDK source commit {actual_commit} is not present "
                "in the checked-out repository"
            )

        ancestor = subprocess.run(
            [
                "git",
                "merge-base",
                "--is-ancestor",
                actual_commit,
                args.expected_source_ancestor,
            ],
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
        )
        if ancestor.returncode != 0:
            raise RuntimeError(
                f"Packaged SDK source commit {actual_commit} is not an ancestor "
                f"of dry-run ref {args.expected_source_ancestor}"
            )

    staged = primary_paths(version_dir, args.version)
    shutil.copy2(source_main, staged["jar"])
    shutil.copy2(source_sources, staged["sources"])
    shutil.copy2(source_javadoc, staged["javadoc"])
    extract_embedded_pom(source_main, staged["pom"])
    validate_pom(staged["pom"], args.version)

    fingerprint = args.gpg_fingerprint.replace(" ", "").upper()
    key_listing = run(["gpg", "--batch", "--with-colons", "--list-secret-keys", fingerprint])
    if fingerprint not in key_listing.stdout.replace(":", "").upper():
        # The machine-readable output separates fields with colons, so perform
        # a second explicit fingerprint check to avoid accepting a short key id.
        fingerprints = [
            line.split(":")[9].upper()
            for line in key_listing.stdout.splitlines()
            if line.startswith("fpr:") and len(line.split(":")) > 9
        ]
        if fingerprint not in fingerprints:
            raise RuntimeError(f"Required GPG secret key is unavailable: {fingerprint}")

    for primary in staged.values():
        signature = Path(str(primary) + ".asc")
        sign(primary, signature, fingerprint, passphrase)
        verify_signature(primary, signature, fingerprint)
        for algorithm in CHECKSUMS:
            checksum = Path(str(primary) + f".{algorithm}")
            checksum.write_text(digest(primary, algorithm) + "\n", encoding="ascii")

    validate_staging(version_dir, args.version, fingerprint)

    bundle = output_dir / f"{ARTIFACT_ID}-{args.version}-maven-central.zip"
    deterministic_zip(repository_root, bundle)
    first_sha = digest(bundle, "sha256")

    second_bundle = output_dir / f".{bundle.name}.repro"
    deterministic_zip(repository_root, second_bundle)
    second_sha = digest(second_bundle, "sha256")
    second_bundle.unlink()
    if first_sha != second_sha:
        raise RuntimeError(
            f"Bundle is not reproducible: first={first_sha}, second={second_sha}"
        )

    validate_bundle(bundle, args.version, fingerprint)
    if bundle.stat().st_size > args.max_bundle_bytes:
        raise RuntimeError(
            f"Bundle size {bundle.stat().st_size} exceeds {args.max_bundle_bytes}"
        )

    manifest = {
        "coordinates": f"{GROUP_ID}:{ARTIFACT_ID}:{args.version}",
        "gpgFingerprint": fingerprint,
        "sourceCommit": actual_commit,
        "sourceValidation": (
            {"mode": "exact", "expectedCommit": args.expected_source_commit}
            if args.expected_source_commit
            else {
                "mode": "ancestor",
                "expectedAncestor": args.expected_source_ancestor,
            }
        ),
        "fileCount": EXPECTED_FILE_COUNT,
        "bundle": bundle.name,
        "bundleSizeBytes": bundle.stat().st_size,
        "bundleSha256": first_sha,
        "primaries": {
            kind: {
                "name": path.name,
                "sizeBytes": path.stat().st_size,
                "sha256": digest(path, "sha256"),
            }
            for kind, path in staged.items()
        },
        "validation": {
            "pomMetadata": "PASS",
            "jarIntegrity": "PASS",
            "signatures": "PASS",
            "checksums": "PASS",
            "extractedBundle": "PASS",
            "reproducibleBundle": "PASS",
        },
    }
    (output_dir / "central-bundle-manifest.json").write_text(
        json.dumps(manifest, indent=2, sort_keys=True) + "\n",
        encoding="utf-8",
    )
    print(json.dumps(manifest, indent=2, sort_keys=True))


if __name__ == "__main__":
    main()
