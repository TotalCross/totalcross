# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

"""Validate issue-417-result.json and its generated PNG without dependencies."""

import json
import struct
import sys
from pathlib import Path


EXPECTED_SIGNATURE = b"\x89PNG\r\n\x1a\n"


def fail(message):
    print(f"FAIL: {message}", file=sys.stderr)
    return 1


def main():
    if len(sys.argv) not in (2, 3):
        print(f"usage: {sys.argv[0]} RESULT_JSON [PNG_PATH]", file=sys.stderr)
        return 2

    result_path = Path(sys.argv[1])
    result = json.loads(result_path.read_text(encoding="utf-8"))
    if result.get("pass") is not True:
        return fail(f"application reported pass={result.get('pass')}: {result.get('failure', '')}")
    if result.get("dimensions") != {"width": 576, "height": 576}:
        return fail(f"unexpected dimensions: {result.get('dimensions')}")
    if result.get("expectedPixels") != {"background": 0xFFFFFF, "border": 0, "interior": 0xFFFFFF}:
        return fail(f"unexpected expectedPixels: {result.get('expectedPixels')}")
    if result.get("observedPixels") != [0xFFFFFF, 0, 0xFFFFFF, 0xFFFFFF]:
        return fail(f"unexpected observedPixels: {result.get('observedPixels')}")
    if result.get("rowInteriorRgba") != [255, 255, 255, 255]:
        return fail(f"unexpected rowInteriorRgba: {result.get('rowInteriorRgba')}")
    if result.get("encodedSize", 0) <= 0 or not result.get("encodedCrc32"):
        return fail("encoded output metadata is missing")

    png_path = Path(sys.argv[2]) if len(sys.argv) == 3 else Path(result["outputPath"])
    if not png_path.is_absolute():
        png_path = result_path.parent / png_path
    png = png_path.read_bytes()
    if len(png) != result["encodedSize"]:
        return fail(f"PNG size {len(png)} differs from result {result['encodedSize']}")
    if png[:8] != EXPECTED_SIGNATURE:
        return fail("PNG signature is invalid")
    if len(png) < 33 or png[12:16] != b"IHDR":
        return fail("PNG does not contain an IHDR chunk")
    width, height = struct.unpack(">II", png[16:24])
    if (width, height) != (576, 576):
        return fail(f"PNG dimensions are {(width, height)}")
    if b"IDAT" not in png:
        return fail("PNG does not contain image data")
    print(f"PASS: {result['implementationPath']} {width}x{height}, {len(png)} bytes")
    return 0


if __name__ == "__main__":
    sys.exit(main())
