#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

"""Count JPEG and PNG payloads among .jpg/.jpeg-named corpus files."""

from pathlib import Path
import sys


def count_image_formats(directory):
    corpus = Path(directory)
    if not corpus.is_dir():
        raise ValueError("image corpus directory does not exist")
    paths = sorted(
        path for path in corpus.rglob("*")
        if path.is_file() and path.suffix.lower() in (".jpg", ".jpeg")
    )
    counts = {"total": len(paths), "jpeg": 0, "png": 0}
    for path in paths:
        with path.open("rb") as source:
            signature = source.read(8)
        if signature.startswith(b"\xff\xd8"):
            counts["jpeg"] += 1
        elif signature == b"\x89PNG\r\n\x1a\n":
            counts["png"] += 1
        else:
            raise ValueError(f"unsupported image content in {path.name}")
    return counts


def main(argv):
    if len(argv) != 2:
        print("Usage: count-image-corpus-formats.py <imag-directory>", file=sys.stderr)
        return 2
    try:
        counts = count_image_formats(argv[1])
    except (OSError, ValueError) as error:
        print(f"image content inspection failed: {error}", file=sys.stderr)
        return 1
    print(f"{counts['total']}\t{counts['jpeg']}\t{counts['png']}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
