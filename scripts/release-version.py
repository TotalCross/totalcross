# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

from __future__ import annotations

import argparse
import re
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path


ROOT_DIR = Path(__file__).resolve().parents[1]
SETTINGS_JAVA = ROOT_DIR / "TotalCrossSDK" / "src" / "main" / "java" / "totalcross" / "sys" / "Settings.java"
SDK_BUILD_GRADLE = ROOT_DIR / "TotalCrossSDK" / "build.gradle"
TCVM_CMAKE = ROOT_DIR / "TotalCrossVM" / "CMakeLists.txt"
CHANGELOG_SCRIPT = ROOT_DIR / "scripts" / "generate-changelog.py"

SEMVER_RE = re.compile(r"^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)$")
SETTINGS_VERSION_RE = re.compile(r"(?m)^(\s*public\s+static\s+int\s+version\s*=\s*)\d+(;\s*)$")
SETTINGS_VERSION_STR_RE = re.compile(
    r'(?m)^(\s*public\s+static\s+(?:final\s+)?String\s+versionStr\s*=\s*")'
    r'(?P<version>\d+\.\d+\.\d+)'
    r'(";\s*)$'
)
SDK_GRADLE_VERSION_RE = re.compile(
    r"(?m)^(\s*version\s*=\s*['\"])(?P<version>\d+\.\d+\.\d+)(['\"]\s*)$"
)
TCVM_CMAKE_VERSION_RE = re.compile(
    r"(?m)^(\s*project\s*\(\s*tcvm\s+VERSION\s+)(?P<version>\d+\.\d+\.\d+)(\s*\)\s*)$"
)


@dataclass(frozen=True)
class Version:
    major: int
    minor: int
    patch: int

    @classmethod
    def parse(cls, value: str) -> "Version":
        match = SEMVER_RE.match(value)
        if not match:
            raise ValueError(f"invalid version '{value}'; expected MAJOR.MINOR.PATCH")
        major, minor, patch = (int(part) for part in match.groups())
        return cls(major, minor, patch)

    def __str__(self) -> str:
        return f"{self.major}.{self.minor}.{self.patch}"


def read_text(path: Path) -> str:
    try:
        return path.read_text()
    except FileNotFoundError:
        raise RuntimeError(f"required file not found: {path.relative_to(ROOT_DIR)}") from None


def write_text(path: Path, content: str) -> None:
    path.write_text(content)


def extract_version(path: Path, pattern: re.Pattern[str], label: str) -> Version:
    content = read_text(path)
    match = pattern.search(content)
    if not match:
        raise RuntimeError(f"could not find {label} in {path.relative_to(ROOT_DIR)}")
    return Version.parse(match.group("version"))


def current_version() -> Version:
    versions = {
        SETTINGS_JAVA.relative_to(ROOT_DIR): extract_version(
            SETTINGS_JAVA, SETTINGS_VERSION_STR_RE, "Settings.versionStr"
        ),
        SDK_BUILD_GRADLE.relative_to(ROOT_DIR): extract_version(
            SDK_BUILD_GRADLE, SDK_GRADLE_VERSION_RE, "Gradle project version"
        ),
        TCVM_CMAKE.relative_to(ROOT_DIR): extract_version(TCVM_CMAKE, TCVM_CMAKE_VERSION_RE, "CMake project version"),
    }
    unique_versions = {str(version) for version in versions.values()}
    if len(unique_versions) != 1:
        details = "\n".join(f"  {path}: {version}" for path, version in versions.items())
        raise RuntimeError(f"version files are out of sync:\n{details}")
    return next(iter(versions.values()))


def bump_version(version: Version, bump: str) -> Version:
    if bump == "major":
        return Version(version.major + 1, 0, 0)
    if bump == "minor":
        return Version(version.major, version.minor + 1, 0)
    if bump == "patch":
        return Version(version.major, version.minor, version.patch + 1)
    raise RuntimeError(f"unsupported bump type: {bump}")


def settings_numeric_version(version: Version) -> int:
    if version.major > 9:
        raise RuntimeError("Settings.version supports only one digit for MAJOR")
    if version.minor > 9:
        raise RuntimeError("Settings.version supports only one digit for MINOR")

    patch_digit = version.patch if version.patch < 10 else 9
    return version.major * 100 + version.minor * 10 + patch_digit


def replace_once(content: str, pattern: re.Pattern[str], replacement: str, label: str, path: Path) -> str:
    updated, count = pattern.subn(replacement, content, count=1)
    if count != 1:
        raise RuntimeError(f"could not update {label} in {path.relative_to(ROOT_DIR)}")
    return updated


def update_settings(version: Version) -> None:
    content = read_text(SETTINGS_JAVA)
    content = replace_once(
        content,
        SETTINGS_VERSION_RE,
        rf"\g<1>{settings_numeric_version(version)}\g<2>",
        "Settings.version",
        SETTINGS_JAVA,
    )
    content = replace_once(
        content,
        SETTINGS_VERSION_STR_RE,
        rf"\g<1>{version}\g<3>",
        "Settings.versionStr",
        SETTINGS_JAVA,
    )
    write_text(SETTINGS_JAVA, content)


def update_gradle(version: Version) -> None:
    content = read_text(SDK_BUILD_GRADLE)
    content = replace_once(
        content,
        SDK_GRADLE_VERSION_RE,
        rf"\g<1>{version}\g<3>",
        "Gradle project version",
        SDK_BUILD_GRADLE,
    )
    write_text(SDK_BUILD_GRADLE, content)


def update_cmake(version: Version) -> None:
    content = read_text(TCVM_CMAKE)
    content = replace_once(
        content,
        TCVM_CMAKE_VERSION_RE,
        rf"\g<1>{version}\g<3>",
        "CMake project version",
        TCVM_CMAKE,
    )
    write_text(TCVM_CMAKE, content)


def verify_release_tag(version: Version) -> str:
    tag = f"v{version}"
    result = subprocess.run(
        ["git", "rev-parse", "--verify", "--quiet", f"refs/tags/{tag}^{{commit}}"],
        cwd=ROOT_DIR,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        check=False,
    )
    if result.returncode != 0:
        raise RuntimeError(f"release tag not found: {tag}")
    return tag


def run_changelog(start_ref: str, update_changelog: bool) -> str:
    args = [sys.executable, str(CHANGELOG_SCRIPT), start_ref, "HEAD"]
    if update_changelog:
        args.append("--update-changelog")

    result = subprocess.run(
        args,
        cwd=ROOT_DIR,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        check=False,
    )
    if result.returncode != 0:
        output = (result.stderr or result.stdout).strip()
        raise RuntimeError(f"generate-changelog failed:\n{output}")
    return result.stdout


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Bump the TotalCross release version in Settings.java, build.gradle, and CMakeLists.txt."
    )
    parser.add_argument(
        "bump",
        choices=("major", "minor", "patch"),
        help=(
            "Version bump. 'major' increments MAJOR and resets MINOR/PATCH; "
            "'minor' increments MINOR and resets PATCH; 'patch' increments PATCH."
        ),
    )
    parser.add_argument("--dry-run", action="store_true", help="Print the planned update without changing files.")
    return parser.parse_args()


def main() -> int:
    args = parse_args()

    try:
        old_version = current_version()
        new_version = bump_version(old_version, args.bump)
        new_numeric_version = settings_numeric_version(new_version)
        start_ref = verify_release_tag(old_version)

        print(f"{old_version} -> {new_version}")
        print(f"Settings.version -> {new_numeric_version}")
        print(f"Changelog range -> {start_ref}..HEAD")

        if args.dry_run:
            print()
            print(run_changelog(start_ref, update_changelog=False), end="")
            print("dry-run: no files changed")
            return 0

        update_settings(new_version)
        update_gradle(new_version)
        update_cmake(new_version)
        print(run_changelog(start_ref, update_changelog=True), end="")
        return 0
    except RuntimeError as exc:
        print(f"error: {exc}", file=sys.stderr)
        return 1
    except ValueError as exc:
        print(f"error: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
