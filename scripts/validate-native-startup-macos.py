#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

"""Deploy and run the native macOS startup path smoke on an ARM64 runner."""

import argparse
import hashlib
import json
import os
from pathlib import Path
import platform
import re
import shutil
import subprocess
import sys


APP_NAME = "LinuxStartupProbe"


def fail(message):
    raise RuntimeError(message)


def sha256(path):
    digest = hashlib.sha256()
    with Path(path).open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def run_logged(command, cwd, environment, log_path, timeout=900):
    log_path = Path(log_path)
    log_path.parent.mkdir(parents=True, exist_ok=True)
    with log_path.open("w", encoding="utf-8") as log:
        log.write("$ " + " ".join(map(str, command)) + "\n")
        log.flush()
        process = subprocess.run(
            list(map(str, command)),
            cwd=str(cwd),
            env=environment,
            stdout=log,
            stderr=subprocess.STDOUT,
            timeout=timeout,
            check=False,
        )
    if process.returncode != 0:
        fail("command exited with code %s; see %s" % (process.returncode, log_path))
    return process


def sdk_home(root):
    root = Path(root).resolve()
    for jar in root.rglob("totalcross-sdk.jar"):
        candidate = jar.parent.parent
        if (candidate / "etc").is_dir():
            return candidate
    fail("SDK artifact has no dist/totalcross-sdk.jar and etc/ layout")


def native_directory(root):
    root = Path(root).resolve()
    matches = [path.parent for path in root.rglob("libtcvm.dylib")
               if (path.parent / "Launcher").is_file()]
    if len(matches) != 1:
        fail("expected one macOS native artifact directory with libtcvm.dylib and Launcher")
    return matches[0]


def verify_runtime_output(name, executable, install, environment, log_path):
    log_path = Path(log_path)
    with log_path.open("w", encoding="utf-8") as log:
        process = subprocess.run(
            [str(executable)],
            cwd=str(install),
            env=environment,
            stdout=log,
            stderr=subprocess.STDOUT,
            timeout=120,
            check=False,
        )
    output = log_path.read_text(encoding="utf-8", errors="replace")
    if process.returncode != 0:
        fail("%s exited with code %s; see %s" % (name, process.returncode, log_path))
    if re.search(r"buffer overflow detected|SIGABRT|SIGSEGV|Segmentation fault", output, re.I):
        fail("%s emitted an abort or buffer-overflow diagnostic; see %s" % (name, log_path))
    for marker in ("TC_STARTUP_MARKER", "TC_STARTUP_COMPLETE"):
        if marker not in output:
            fail("%s is missing %s; see %s" % (name, marker, log_path))
    line = next((item for item in output.splitlines()
                 if item.startswith("TC_PATH_MARKER appPath=")), None)
    match = re.fullmatch(r"TC_PATH_MARKER appPath=(.*?) vmPath=(.*)", line or "")
    if not match:
        fail("%s did not report application and VM directories" % name)
    app_path, vm_path = match.groups()
    expected = os.path.realpath(install)
    if os.path.realpath(app_path) != expected or os.path.realpath(vm_path) != expected:
        fail("%s discovered unexpected appPath=%r or vmPath=%r" % (name, app_path, vm_path))
    result = {
        "case": name,
        "exitCode": process.returncode,
        "appPath": app_path,
        "vmPath": vm_path,
        "log": str(log_path),
    }
    print(json.dumps(result, sort_keys=True))
    return result


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--sdk-root", required=True)
    parser.add_argument("--native-root", required=True)
    parser.add_argument("--java-source", required=True)
    parser.add_argument("--output", required=True)
    args = parser.parse_args()

    if platform.machine().lower() not in ("arm64", "aarch64"):
        fail("native macOS startup smoke requires an ARM64 runner")
    output = Path(args.output).resolve()
    output.mkdir(parents=True, exist_ok=True)
    sdk = sdk_home(args.sdk_root)
    native = native_directory(args.native_root)
    java_source = Path(args.java_source).resolve()
    if not java_source.is_file():
        fail("Java startup probe source does not exist: " + str(java_source))
    runtime_source = native / "libtcvm.dylib"
    launcher_source = native / "Launcher"

    runtime_destination = sdk / "dist/vm/macos/libtcvm.dylib"
    launcher_destination = sdk / "etc/launchers/macos/Launcher"
    runtime_destination.parent.mkdir(parents=True, exist_ok=True)
    launcher_destination.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(runtime_source, runtime_destination)
    shutil.copy2(launcher_source, launcher_destination)

    classes = output / "classes"
    classes.mkdir()
    sdk_jar = sdk / "dist/totalcross-sdk.jar"
    classpath = str(sdk_jar) + os.pathsep + str(sdk / "dist/libs/*")
    environment = os.environ.copy()
    environment["TOTALCROSS3_HOME"] = str(sdk)
    compile_log = output / "compile.log"
    run_logged(
        ["javac", "--release", "8", "-cp", classpath, "-d", classes, java_source],
        output,
        environment,
        compile_log,
    )
    app_jar = output / (APP_NAME + ".jar")
    run_logged(["jar", "cf", app_jar, "-C", classes, "."], output, environment,
               output / "jar.log")
    deployed = output / "deployed"
    deploy_log = output / "deploy.log"
    run_logged(
        ["java", "-cp", classpath, "tc.Deploy", app_jar, "-macos", "/p", "/n",
         APP_NAME, "/o", str(deployed) + os.sep],
        output,
        environment,
        deploy_log,
        timeout=1800,
    )

    install = deployed / "install/macos"
    executable = install / APP_NAME
    if not executable.is_file():
        fail("deployer did not create " + str(executable))
    runtime_destination_install = install / "libtcvm.dylib"
    shutil.copy2(runtime_source, runtime_destination_install)
    shutil.copy2(launcher_source, executable)
    executable.chmod(executable.stat().st_mode | 0o111)
    if sha256(runtime_source) != sha256(runtime_destination_install):
        fail("staged macOS VM differs from the canonical build artifact")

    linkage = subprocess.run(
        ["otool", "-L", str(executable)],
        cwd=str(install),
        env=environment,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        check=False,
    )
    if linkage.returncode != 0 or "libtcvm.dylib" not in linkage.stdout:
        fail("macOS executable does not declare its libtcvm.dylib dependency")
    (output / "linkage.txt").write_text(linkage.stdout, encoding="utf-8")

    runtime_environment = environment.copy()
    runtime_environment.pop("DYLD_LIBRARY_PATH", None)
    runtime_environment.pop("DYLD_INSERT_LIBRARIES", None)
    results = [verify_runtime_output(
        "normal-path", executable, install, runtime_environment, output / "normal.log")]

    alias_parent = output / "paths/symlink"
    alias_parent.mkdir(parents=True, exist_ok=True)
    alias_install = alias_parent / "install"
    alias_install.symlink_to(install, target_is_directory=True)
    results.append(verify_runtime_output(
        "symlink", alias_install / APP_NAME, alias_install, runtime_environment,
        output / "symlink.log"))

    summary = {
        "status": "passed",
        "architecture": platform.machine(),
        "nativeLibrarySha256": sha256(runtime_source),
        "deployedLibrarySha256": sha256(runtime_destination_install),
        "nativeLauncherSha256": sha256(launcher_source),
        "linkage": linkage.stdout.strip(),
        "cases": results,
    }
    (output / "summary.json").write_text(json.dumps(summary, indent=2) + "\n")
    print(json.dumps({"status": summary["status"], "caseCount": len(results)}, sort_keys=True))


if __name__ == "__main__":
    try:
        main()
    except Exception as failure:
        print("native macOS startup validation failed: " + str(failure), file=sys.stderr)
        sys.exit(1)
