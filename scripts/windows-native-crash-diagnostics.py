#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

"""Prepare and evaluate the isolated native Windows startup probe."""

from __future__ import annotations

import argparse
import hashlib
import json
import os
from pathlib import Path
import shutil
import struct
import subprocess
import sys
import zipfile


REVISIONS = {
    "baseline": {
        "commit": "6b915883dd202f6db891990f6c3af26fb645ecc7",
        "tree": "44047160610b7efcb3d49768736af95bde19f85f",
    },
    "fixed": {
        "commit": "94af2b8b653cad509994b5ccf3dbb60ac28ef920",
        "tree": "539f5b424b6f4593c7d96074d69ff077a38ed361",
    },
}
ENTRY_CLASS = "totalcross.ui.WindowsNativeStartupProbe"
APP_NAME = "WindowsNativeStartupProbe"
REQUIRED_APP_MARKERS = (
    "tcdiag-tcvm-java-class-initialized.marker",
    "tcdiag-mainwindow-constructed.marker",
    "tcdiag-init-ui-entered.marker",
    "tcdiag-window-surface-requested.marker",
    "tcdiag-window-render-returned.marker",
    "tcdiag-application-complete.marker",
)


class DiagnosticError(Exception):
    """Raised when artifact provenance or startup evidence is invalid."""


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def pe_machine(path: Path) -> int:
    try:
        with path.open("rb") as stream:
            if stream.read(2) != b"MZ":
                raise DiagnosticError("Windows executable has no DOS header: " + str(path))
            stream.seek(0x3C)
            pe_offset = struct.unpack("<I", stream.read(4))[0]
            stream.seek(pe_offset)
            if stream.read(4) != b"PE\0\0":
                raise DiagnosticError("Windows executable has no PE signature: " + str(path))
            return struct.unpack("<H", stream.read(2))[0]
    except (OSError, struct.error) as error:
        raise DiagnosticError("cannot inspect Windows executable: " + str(path)) from error


def require_amd64(path: Path) -> None:
    machine = pe_machine(path)
    if machine != 0x8664:
        raise DiagnosticError("Windows artifact is not AMD64: " + str(path)
                              + " (PE machine 0x%04x)" % machine)


def read_json(path: Path) -> dict:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as error:
        raise DiagnosticError("cannot read JSON evidence: " + str(path)) from error


def verify_build_manifest(path: Path, revision: str, label: str) -> dict:
    expected = REVISIONS[revision]
    manifest = read_json(path)
    source = manifest.get("source", {})
    if str(manifest.get("repository", "")).lower() != "totalcross/totalcross":
        raise DiagnosticError(label + " artifact came from an unexpected repository")
    if source.get("commit") != expected["commit"] or source.get("tree") != expected["tree"]:
        raise DiagnosticError(label + " artifact source commit/tree does not match " + revision)
    if source.get("dirty") is not False:
        raise DiagnosticError(label + " artifact was not built from a clean source tree")
    return manifest


def locate_one(root: Path, name: str) -> Path:
    matches = sorted(path for path in root.rglob("*")
                     if path.is_file() and path.name.lower() == name.lower())
    if len(matches) != 1:
        raise DiagnosticError("expected exactly one " + name + " under " + str(root)
                              + "; found " + str(len(matches)))
    return matches[0]


def matching_pdb(binary: Path, pdbs: list[Path]) -> Path | None:
    candidates = [path for path in pdbs if path.stem.lower() == binary.stem.lower()]
    hashes: dict[str, list[Path]] = {}
    for candidate in candidates:
        hashes.setdefault(sha256(candidate), []).append(candidate)
    if len(hashes) > 1:
        raise DiagnosticError("ambiguous matching PDB for " + str(binary))
    adjacent = binary.with_suffix(".pdb")
    if adjacent in candidates:
        return adjacent
    return next(iter(hashes.values()))[0] if hashes else None


def deploy_sdk_jar(runtime_home: Path) -> Path:
    jar = runtime_home / "dist/totalcross-sdk.jar"
    if not jar.is_file():
        raise DiagnosticError("canonical dist/totalcross-sdk.jar is missing")
    try:
        with zipfile.ZipFile(jar) as archive:
            if "tc/Deploy.class" not in archive.namelist():
                raise DiagnosticError("canonical SDK JAR does not contain tc.Deploy")
    except (OSError, zipfile.BadZipFile) as error:
        raise DiagnosticError("cannot inspect SDK JAR: " + str(jar)) from error
    return jar


def run_logged(command: list[str], cwd: Path, log: Path, env: dict[str, str]) -> None:
    result = subprocess.run(command, cwd=cwd, env=env, text=True,
                            stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                            timeout=600, check=False)
    log.write_text(result.stdout, encoding="utf-8", errors="replace")
    if result.returncode:
        raise DiagnosticError("command failed (%d): %s; see %s" %
                              (result.returncode, command[0], log))


def prepare(arguments) -> None:
    revision = arguments.revision
    expected = REVISIONS[revision]
    runtime_home = arguments.runtime_home.resolve()
    platform_artifact = arguments.platform_artifact.resolve()
    sdk_manifest_path = arguments.sdk_provenance.resolve()
    platform_manifest_path = arguments.platform_provenance.resolve()
    output = arguments.output.resolve()
    if not runtime_home.is_dir() or not platform_artifact.is_dir():
        raise DiagnosticError("extracted SDK and Windows artifact directories are required")
    if not output.is_dir():
        raise DiagnosticError("diagnostic output directory must exist")

    sdk_manifest = verify_build_manifest(sdk_manifest_path, revision, "SDK")
    platform_manifest = verify_build_manifest(platform_manifest_path, revision, "Windows")
    if sdk_manifest["source"] != platform_manifest["source"]:
        raise DiagnosticError("SDK and Windows artifacts do not have identical provenance")

    launcher = locate_one(platform_artifact, "Launcher.exe")
    vm = locate_one(platform_artifact, "tcvm.dll")
    require_amd64(launcher)
    require_amd64(vm)
    pdbs = sorted(platform_artifact.rglob("*.pdb"))
    symbols_directory = output / "symbols"
    symbols_directory.mkdir(parents=True, exist_ok=True)
    copied_pdbs = []
    launcher_pdb_source = matching_pdb(launcher, pdbs)
    vm_pdb_source = matching_pdb(vm, pdbs)
    for pdb in (launcher_pdb_source, vm_pdb_source):
        if pdb is None:
            continue
        copied = symbols_directory / pdb.name
        shutil.copy2(pdb, copied)
        copied_pdbs.append(copied)
    launcher_pdb = next((p for p in copied_pdbs if p.stem.lower() == launcher.stem.lower()), None)
    vm_pdb = next((p for p in copied_pdbs if p.stem.lower() == vm.stem.lower()), None)

    launcher_destination = runtime_home / "etc/launchers/win32/Launcher.exe"
    vm_destination = runtime_home / "dist/vm/win32/tcvm.dll"
    launcher_destination.parent.mkdir(parents=True, exist_ok=True)
    vm_destination.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(launcher, launcher_destination)
    shutil.copy2(vm, vm_destination)

    sdk_jar = deploy_sdk_jar(runtime_home)
    libraries = runtime_home / "dist/libs"
    if not libraries.is_dir():
        raise DiagnosticError("packaged SDK libraries are missing")
    source = Path(arguments.source).resolve()
    if not source.is_file():
        raise DiagnosticError("startup probe Java source is missing: " + str(source))
    classes = output / "classes"
    classes.mkdir(parents=True, exist_ok=True)
    classpath = os.pathsep.join([str(sdk_jar), str(libraries / "*")])
    env = os.environ.copy()
    env["TOTALCROSS3_HOME"] = str(runtime_home)
    run_logged(["javac", "--release", "8", "-cp", classpath, "-d", str(classes),
                str(source)], output, output / "compile.log", env)
    jar_path = output / (APP_NAME + ".jar")
    run_logged(["jar", "cf", str(jar_path), "-C", str(classes), "."], output,
               output / "jar.log", env)
    deploy_classpath = os.pathsep.join([str(sdk_jar), str(libraries / "*")])
    run_logged(["java", "-cp", deploy_classpath, "tc.Deploy", str(jar_path),
                "-win32", "/p", "/n", APP_NAME], output, output / "deploy.log", env)

    installed = output / "install/win32" / (APP_NAME + ".exe")
    if not installed.is_file():
        raise DiagnosticError("deployer output is missing: " + str(installed))
    deployed_vm = installed.parent / "tcvm.dll"
    shutil.copy2(vm, deployed_vm)
    require_amd64(installed)

    manifest = {
        "schemaVersion": 1,
        "revision": revision,
        "sourceCommit": expected["commit"],
        "sourceTree": expected["tree"],
        "configuration": "Visual Studio 17 2022 x64 Release",
        "sdkProvenance": {"path": str(sdk_manifest_path), "sha256": sha256(sdk_manifest_path)},
        "windowsProvenance": {
            "path": str(platform_manifest_path), "sha256": sha256(platform_manifest_path)},
        "sdkJar": {"path": str(sdk_jar), "sha256": sha256(sdk_jar)},
        "launcher": {"path": str(launcher), "sha256": sha256(launcher),
                     "machine": "AMD64"},
        "tcvm": {"path": str(vm), "sha256": sha256(vm), "machine": "AMD64"},
        "deployedLauncher": {"path": str(installed), "sha256": sha256(installed)},
        "deployedTcvm": {"path": str(deployed_vm), "sha256": sha256(deployed_vm)},
        "probeJar": {"path": str(jar_path), "sha256": sha256(jar_path)},
        "deployedProbe": {"path": str(installed), "sha256": sha256(installed)},
        "symbols": [
            {"path": str(path), "sha256": sha256(path)} for path in copied_pdbs
        ],
        "matchingPdbs": {
            "launcher": None if launcher_pdb is None else {
                "path": str(launcher_pdb), "sha256": sha256(launcher_pdb)},
            "tcvm": None if vm_pdb is None else {
                "path": str(vm_pdb), "sha256": sha256(vm_pdb)},
        },
    }
    (output / "diagnostic-manifest.json").write_text(
        json.dumps(manifest, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    print(json.dumps({"status": "prepared", "revision": revision,
                      "sourceCommit": expected["commit"],
                      "launcherSha256": manifest["launcher"]["sha256"],
                      "tcvmSha256": manifest["tcvm"]["sha256"],
                      "matchingPdbs": manifest["matchingPdbs"]}, sort_keys=True))


def assess_probe(runtime: dict, marker_names: set[str]) -> dict:
    missing = [marker for marker in REQUIRED_APP_MARKERS if marker not in marker_names]
    exit_code = runtime.get("exitCodeUnsigned")
    errors = []
    if runtime.get("processStartObserved") is not True:
        errors.append("Launcher process start was not observed")
    if exit_code != 0:
        exit_hex = runtime.get("exitCodeHex", "unavailable")
        errors.append("Launcher exited with %s" % exit_hex)
    if missing:
        errors.append("missing TotalCross startup markers: " + ", ".join(missing))
    return {
        "status": "PASS" if not errors else "FAIL",
        "sourceCommit": runtime.get("sourceCommit"),
        "processStartObserved": runtime.get("processStartObserved") is True,
        "exitCode": runtime.get("exitCode"),
        "exitCodeUnsigned": exit_code,
        "exitCodeHex": runtime.get("exitCodeHex"),
        "observedAppMarkers": [m for m in REQUIRED_APP_MARKERS if m in marker_names],
        "missingAppMarkers": missing,
        "error": None if not errors else "; ".join(errors),
    }


def summarize(arguments) -> None:
    output = arguments.output.resolve()
    runtime_path = output / "runtime.json"
    runtime = read_json(runtime_path)
    manifest = read_json(output / "diagnostic-manifest.json")
    app_directory = Path(manifest["deployedProbe"]["path"]).parent
    marker_names = {path.name for path in app_directory.glob("tcdiag-*.marker")}
    summary = assess_probe(runtime, marker_names)
    summary.update({
        "revision": manifest["revision"],
        "sourceCommit": manifest["sourceCommit"],
        "sourceTree": manifest["sourceTree"],
        "configuration": manifest["configuration"],
        "diagnosticManifest": "diagnostic-manifest.json",
        "runtimeReport": "runtime.json",
    })
    (output / "diagnostic-result.json").write_text(
        json.dumps(summary, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    print(json.dumps(summary, sort_keys=True))


def require_pass(arguments) -> None:
    summary = read_json(arguments.output.resolve() / "diagnostic-result.json")
    if summary.get("status") != "PASS":
        raise DiagnosticError(summary.get("error") or "startup probe did not pass")


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    subparsers = parser.add_subparsers(dest="command", required=True)
    prepare_parser = subparsers.add_parser("prepare")
    prepare_parser.add_argument("--revision", choices=REVISIONS, required=True)
    prepare_parser.add_argument("--runtime-home", type=Path, required=True)
    prepare_parser.add_argument("--platform-artifact", type=Path, required=True)
    prepare_parser.add_argument("--sdk-provenance", type=Path, required=True)
    prepare_parser.add_argument("--platform-provenance", type=Path, required=True)
    prepare_parser.add_argument("--source", type=Path, required=True)
    prepare_parser.add_argument("--output", type=Path, required=True)
    summarize_parser = subparsers.add_parser("summarize")
    summarize_parser.add_argument("--output", type=Path, required=True)
    pass_parser = subparsers.add_parser("require-pass")
    pass_parser.add_argument("--output", type=Path, required=True)
    arguments = parser.parse_args(argv)
    try:
        if arguments.command == "prepare":
            prepare(arguments)
        elif arguments.command == "summarize":
            summarize(arguments)
        else:
            require_pass(arguments)
    except (DiagnosticError, OSError, subprocess.SubprocessError) as error:
        print("Windows native diagnostic error: " + str(error), file=sys.stderr)
        return 2
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
