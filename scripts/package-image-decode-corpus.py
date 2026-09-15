#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

"""Validate and stage the six image-decode benchmark variants."""

import argparse
from collections import Counter
from pathlib import Path
import shutil
import sys


EXPECTED_IMAGES = 663
VARIANTS = (
    ("imag", "imag"),
    ("lossless", "lossless"),
    ("decode-baseline", "decode-baseline"),
    ("decode-fast", "decode-fast"),
    ("aggresive-480", "aggressive-480"),
    ("aggresive-540", "aggressive-540"),
)


class CorpusFailure(RuntimeError):
    pass


def image_format(path):
    with path.open("rb") as source:
        header = source.read(16)
    if header.startswith(b"\xff\xd8\xff"):
        return "jpeg"
    if header.startswith(b"\x89PNG\r\n\x1a\n"):
        return "png"
    if header.startswith((b"GIF87a", b"GIF89a")):
        return "gif"
    if header.startswith(b"BM"):
        return "bmp"
    if header[:4] in (b"II*\x00", b"MM\x00*"):
        return "tiff"
    if header.startswith(b"RIFF") and header[8:12] == b"WEBP":
        return "webp"
    return None


def selected_paths(folder):
    return sorted(
        path for path in folder.rglob("*")
        if path.is_file() and path.suffix.lower() == ".jpg"
    )


def validate(source_root):
    expected_sources = {source for _, source in VARIANTS}
    actual_dirs = {path.name for path in source_root.iterdir() if path.is_dir()}
    if actual_dirs != expected_sources:
        raise CorpusFailure(
            "source variant folders differ: "
            f"missing={sorted(expected_sources - actual_dirs)}, "
            f"extra={sorted(actual_dirs - expected_sources)}"
        )

    selected = {}
    base_names = None
    formats = Counter()
    for variant, source_name in VARIANTS:
        folder = source_root / source_name
        paths = selected_paths(folder)
        names = [path.relative_to(folder).as_posix() for path in paths]
        if len(names) != EXPECTED_IMAGES:
            raise CorpusFailure(
                f"{source_name} must contain exactly {EXPECTED_IMAGES} .jpg-named "
                f"benchmark images; found {len(names)}"
            )
        if len(set(names)) != EXPECTED_IMAGES:
            raise CorpusFailure(f"{source_name} contains duplicate relative names")
        if base_names is None:
            base_names = names
        elif names != base_names:
            missing = sorted(set(base_names) - set(names))
            extra = sorted(set(names) - set(base_names))
            raise CorpusFailure(
                f"{source_name} relative names differ from imag: "
                f"missing={missing[:3]}, extra={extra[:3]}"
            )
        for path in paths:
            detected = image_format(path)
            if detected is None:
                raise CorpusFailure(
                    f"unrecognized image content in {source_name}/"
                    f"{path.relative_to(folder).as_posix()}"
                )
            formats[detected] += 1
        selected[variant] = (folder, paths)
    return selected, formats


def stage(source_root, output_root):
    selected, formats = validate(source_root)
    output_root.mkdir(parents=True, exist_ok=True)
    for variant, _ in VARIANTS:
        destination = output_root / variant
        destination.mkdir(parents=True, exist_ok=True)
        folder, paths = selected[variant]
        for source in paths:
            target = destination / source.relative_to(folder)
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(source, target)
        copied = selected_paths(destination)
        if len(copied) != EXPECTED_IMAGES:
            raise CorpusFailure(
                f"staged {variant} has {len(copied)} entries, expected {EXPECTED_IMAGES}"
            )
    if set(path.name for path in output_root.iterdir()) != {
        variant for variant, _ in VARIANTS
    }:
        raise CorpusFailure("staged corpus has unexpected top-level entries")
    print(
        "corpus staged,variants=6,images_per_variant=663,"
        f"selected_formats={dict(sorted(formats.items()))}"
    )


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source_root", type=Path)
    parser.add_argument("output_root", type=Path)
    args = parser.parse_args(argv[1:])
    source_root = args.source_root.expanduser().resolve()
    output_root = args.output_root.expanduser().resolve()
    if not source_root.is_dir():
        raise CorpusFailure(f"source corpus root not found: {source_root}")
    stage(source_root, output_root)
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main(sys.argv))
    except (CorpusFailure, OSError) as error:
        print(str(error), file=sys.stderr)
        sys.exit(1)
