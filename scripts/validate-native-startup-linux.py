#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

"""Deploy and run the native Linux startup path matrix on a hosted runner."""

import argparse
import hashlib
import importlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import tempfile


APP_NAME = "LinuxStartupProbe"
PNG_APP_NAME = "LinuxPngRegressionProbe"
REQUIRED_STARTUP_MARKERS = ("TC_STARTUP_MARKER", "TC_STARTUP_COMPLETE")
REQUIRED_CONTROL_MARKERS = (
    "TC_FILE_CONTROL_PASS",
    "TC_SQLITE_CONTROL_PASS integrity_check=ok",
    "TC_JPEG_CONTROL_PASS width=8 height=8",
)


def fail(message):
    raise RuntimeError(message)


def sha256(path):
    digest = hashlib.sha256()
    with Path(path).open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def native_directory(root):
    root = Path(root).resolve()
    matches = [path.parent for path in root.rglob("libtcvm.so")
               if (path.parent / "Launcher").is_file()]
    if len(matches) != 1:
        fail("expected one Linux native artifact directory with libtcvm.so and Launcher")
    return matches[0]


def prepare_install(args):
    sys.path.insert(0, str(Path(args.investigation_root).resolve() / "tools"))
    harness = importlib.import_module("functional_validation_730")
    harness.STARTUP_SOURCE = Path(args.java_source).resolve()
    harness.APP_NAMES["startup"] = APP_NAME
    prepared = Path(args.output).resolve() / "prepared"
    namespace = argparse.Namespace(
        kind="startup",
        sdk_root=Path(args.sdk_root).resolve(),
        package=None,
        native_dir=native_directory(args.native_root),
        out=prepared,
    )
    harness.build_probe(namespace)

    install = prepared / "deployed/install/linux"
    executable = install / APP_NAME
    library = install / "libtcvm.so"
    if not executable.is_file() or not library.is_file():
        fail("deployment must include the executable and its native libtcvm.so")
    native_lib = namespace.native_dir / "libtcvm.so"
    if sha256(library) != sha256(native_lib):
        fail("deployed Linux VM does not match the canonical build artifact")
    shutil.copy2(args.jpeg_fixture, install / "comparison-control.jpg")
    return install, executable, native_lib, namespace.native_dir / "Launcher"


def copy_install(source, destination):
    destination.parent.mkdir(parents=True, exist_ok=True)
    shutil.copytree(source, destination)
    return destination


def scenario_installations(base, executable, output):
    temporary_root = Path(tempfile.mkdtemp(prefix="tc", dir="/tmp"))
    short_install = copy_install(base, temporary_root / "s" / "i")

    long_install = temporary_root / "long"
    long_install.mkdir()
    index = 0
    while len(str(long_install / "install" / executable.name)) <= 200:
        component = "segment_%02d_" % index + ("x" * 20)
        long_install = long_install / component
        long_install.mkdir()
        index += 1
    long_install = copy_install(base, long_install / "install")

    alias_parent = Path(output).resolve() / "paths/symlink"
    alias_parent.mkdir(parents=True, exist_ok=True)
    symlink_install = alias_parent / "install"
    symlink_install.symlink_to(base, target_is_directory=True)

    cases = [
        ("normal-path", base, False),
        ("short-path", short_install, False),
        ("bounded-long-path", long_install, False),
        ("symlink", symlink_install, False),
        ("relative-path", base, True),
    ]
    for name, install, relative in cases:
        launch_path = "./" + executable.name if relative else install / executable.name
        path_length = len(str(launch_path if relative else install / executable.name))
        if name == "short-path" and path_length >= 80:
            fail("short executable path exceeded 79 bytes")
        if name == "bounded-long-path" and not 200 < path_length < 238:
            fail("bounded long executable path must remain between 201 and 237 bytes")
        if path_length >= 512:
            fail("executable path exceeds the independent launcher command-line limit")
        yield name, install, launch_path, relative, path_length


def run_case(name, install, launch_path, relative, path_length, output):
    environment = os.environ.copy()
    environment["LD_LIBRARY_PATH"] = str(install)
    environment.pop("LD_PRELOAD", None)
    command = ["xvfb-run", "-a", "env", str(launch_path)]
    log_path = Path(output) / (name + ".log")
    log_path.parent.mkdir(parents=True, exist_ok=True)
    with log_path.open("w", encoding="utf-8") as log:
        process = subprocess.run(
            command,
            cwd=str(install),
            env=environment,
            stdout=log,
            stderr=subprocess.STDOUT,
            text=True,
            timeout=120,
            check=False,
        )
    text = log_path.read_text(encoding="utf-8", errors="replace")
    if process.returncode != 0:
        fail("%s exited with code %s; see %s" % (name, process.returncode, log_path))
    if re.search(r"buffer overflow detected|SIGABRT|SIGSEGV|Segmentation fault", text, re.I):
        fail("%s emitted an abort or buffer-overflow diagnostic; see %s" % (name, log_path))
    for marker in REQUIRED_STARTUP_MARKERS:
        if marker not in text:
            fail("%s is missing %s; see %s" % (name, marker, log_path))

    path_line = next((line for line in text.splitlines()
                      if line.startswith("TC_PATH_MARKER appPath=")), None)
    if path_line is None:
        fail("%s is missing native path discovery output" % name)
    match = re.fullmatch(r"TC_PATH_MARKER appPath=(.*?) vmPath=(.*)", path_line)
    if not match:
        fail("%s emitted an invalid native path discovery marker" % name)
    app_path, vm_path = match.groups()
    expected_directory = os.path.realpath(install)
    if os.path.realpath(app_path) != expected_directory:
        fail("%s discovered appPath=%r, expected %r" % (name, app_path, expected_directory))
    if os.path.realpath(vm_path) != expected_directory:
        fail("%s discovered vmPath=%r, expected %r" % (name, vm_path, expected_directory))

    controls_expected = True
    if controls_expected:
        for marker in REQUIRED_CONTROL_MARKERS:
            if marker not in text:
                fail("%s is missing downstream control %s" % (name, marker))

    result = {
        "case": name,
        "exitCode": process.returncode,
        "executablePathLength": path_length,
        "launchMode": "relative" if relative else "absolute",
        "appPath": app_path,
        "vmPath": vm_path,
        "downstreamControls": controls_expected,
        "log": str(log_path),
    }
    print(json.dumps(result, sort_keys=True))
    return result


def run_logged(command, cwd, environment, log_path, timeout=1800):
    log_path.parent.mkdir(parents=True, exist_ok=True)
    with log_path.open("w", encoding="utf-8") as log:
        log.write("$ " + " ".join(str(arg) for arg in command) + "\n")
        log.flush()
        result = subprocess.run(command, cwd=str(cwd), env=environment,
                                stdout=log, stderr=subprocess.STDOUT,
                                text=True, timeout=timeout, check=False)
        log.write("\nEXIT_CODE=" + str(result.returncode) + "\n")
    if result.returncode:
        fail("command failed with exit %d; see %s" % (result.returncode, log_path))


def verify_png_fixtures(fixture_root):
    sums = fixture_root / "SHA256SUMS"
    if not sums.is_file():
        fail("PNG regression fixture checksums are missing")
    verified = {}
    for line in sums.read_text(encoding="utf-8").splitlines():
        if not line.strip():
            continue
        digest, name = line.split(None, 1)
        path = fixture_root / name.strip().lstrip("*")
        if not path.is_file() or sha256(path) != digest:
            fail("PNG regression fixture checksum mismatch: " + path.name)
        verified[path.name] = digest
    required = {"historical-rgba-36x36.png", "ownership-red-2x2.png"}
    if not required.issubset(verified):
        fail("PNG regression fixtures are incomplete: " + repr(sorted(required - set(verified))))
    return verified


def run_png_regression(args, output, native_dir):
    fixture_root = Path(args.png_fixture_dir).resolve()
    source = Path(args.png_regression_source).resolve()
    if not source.is_file():
        fail("PNG regression source is missing: " + str(source))
    fixture_hashes = verify_png_fixtures(fixture_root)

    home = Path(args.sdk_root).resolve()
    sdk_jar = home / "dist/totalcross-sdk.jar"
    if not sdk_jar.is_file() or not (home / "dist/libs").is_dir():
        fail("SDK root must contain dist/totalcross-sdk.jar and dist/libs")
    vm_dir = home / "dist/vm/linux"
    launcher_dir = home / "etc/launchers/linux"
    vm_dir.mkdir(parents=True, exist_ok=True)
    launcher_dir.mkdir(parents=True, exist_ok=True)
    shutil.copy2(native_dir / "libtcvm.so", vm_dir / "libtcvm.so")
    shutil.copy2(native_dir / "Launcher", launcher_dir / "Launcher")
    deployed_launcher = launcher_dir / "Launcher"
    deployed_launcher.chmod(deployed_launcher.stat().st_mode | 0o111)

    probe_root = output / "png-regression"
    classes = probe_root / "classes"
    classes.mkdir(parents=True, exist_ok=True)
    classpath = os.pathsep.join((str(sdk_jar), str(home / "dist/libs" / "*")))
    environment = os.environ.copy()
    environment["TOTALCROSS3_HOME"] = str(home)
    environment.pop("LD_PRELOAD", None)
    run_logged(["javac", "--release", "8", "-cp", classpath, "-d", str(classes), str(source)],
               Path.cwd(), environment, probe_root / "compile.log")

    app_jar = probe_root / (PNG_APP_NAME + ".jar")
    run_logged(["jar", "cf", str(app_jar), "-C", str(classes), "."],
               Path.cwd(), environment, probe_root / "jar.log")
    deploy_root = probe_root / "deployed"
    deploy_root.mkdir(parents=True, exist_ok=True)
    run_logged(["java", "-cp", classpath, "tc.Deploy", str(app_jar), "-linux", "/p", "/n",
                PNG_APP_NAME, "/o", str(deploy_root) + os.sep],
               Path.cwd(), environment, probe_root / "deploy.log")

    install = deploy_root / "install/linux"
    executable = install / PNG_APP_NAME
    if not executable.is_file():
        fail("deployer did not create " + str(executable))
    executable.chmod(executable.stat().st_mode | 0o111)
    for fixture_name in sorted({"historical-rgba-36x36.png", "ownership-red-2x2.png"}):
        shutil.copy2(fixture_root / fixture_name, install / fixture_name)

    runtime_environment = os.environ.copy()
    runtime_environment["LD_LIBRARY_PATH"] = str(install) + os.pathsep + runtime_environment.get("LD_LIBRARY_PATH", "")
    runtime_environment.pop("LD_PRELOAD", None)
    runtime_log = probe_root / "runtime.log"
    with runtime_log.open("w", encoding="utf-8") as log:
        process = subprocess.run(["xvfb-run", "-a", "env", str(executable)],
                                 cwd=str(install), env=runtime_environment,
                                 stdout=log, stderr=subprocess.STDOUT, text=True,
                                 timeout=180, check=False)
    runtime_output = runtime_log.read_text(encoding="utf-8", errors="replace")
    if process.returncode != 0:
        fail("Linux PNG regression probe exited with code %d; see %s"
             % (process.returncode, runtime_log))
    if "TC_LINUX_PNG_RESULT complete=true" not in runtime_output:
        fail("Linux PNG regression probe omitted its completion marker; see " + str(runtime_log))
    cases = [line for line in runtime_output.splitlines()
             if line.startswith("TC_LINUX_PNG case=")]
    required_cases = {
        "historical-rgba-file",
        "historical-rgba-bytes",
        "original-ownership-drawing",
    }
    names = {line.split("case=", 1)[1].split(",", 1)[0] for line in cases}
    if names != required_cases or any(",status=PASS" not in line for line in cases):
        fail("Linux PNG regression assertions are incomplete or failed: " + repr(cases))
    render = install / "image-ownership-render.png"
    if not render.is_file() or render.stat().st_size == 0:
        fail("ownership drawing regression did not write its PNG output")

    result = {
        "status": "PASS",
        "processReturnCode": process.returncode,
        "runtimeShim": None,
        "sourceSha256": sha256(source),
        "fixtureSha256": fixture_hashes,
        "cases": cases,
        "renderSha256": sha256(render),
        "runtimeLog": "runtime.log",
    }
    (probe_root / "result.json").write_text(json.dumps(result, indent=2) + "\n",
                                            encoding="utf-8")
    for case in cases:
        print(case)
    print(json.dumps({"pngRegressionStatus": result["status"], "caseCount": len(cases)},
                     sort_keys=True))
    return result


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--sdk-root", required=True)
    parser.add_argument("--native-root", required=True)
    parser.add_argument("--investigation-root", required=True)
    parser.add_argument("--java-source", required=True)
    parser.add_argument("--jpeg-fixture", required=True)
    parser.add_argument("--png-regression-source", required=True)
    parser.add_argument("--png-fixture-dir", required=True)
    parser.add_argument("--output", required=True)
    args = parser.parse_args()

    output = Path(args.output).resolve()
    output.mkdir(parents=True, exist_ok=True)
    install, executable, native_lib, native_launcher = prepare_install(args)
    results = [run_case(*case, output) for case in
               scenario_installations(install, executable, output)]
    png_result = run_png_regression(args, output, native_directory(args.native_root))
    summary = {
        "status": "passed",
        "nativeLibrarySha256": sha256(native_lib),
        "nativeLauncherSha256": sha256(native_launcher),
        "cases": results,
        "pngRegression": png_result,
    }
    (output / "summary.json").write_text(json.dumps(summary, indent=2) + "\n")
    print(json.dumps({"status": summary["status"], "caseCount": len(results)}, sort_keys=True))


if __name__ == "__main__":
    try:
        main()
    except Exception as failure:
        print("native Linux startup validation failed: " + str(failure), file=sys.stderr)
        sys.exit(1)
