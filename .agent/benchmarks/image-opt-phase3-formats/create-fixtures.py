#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

"""Create deterministic Phase-3 compact-format input fixtures."""

from pathlib import Path
import argparse
import struct
import subprocess
import tempfile
import zlib


WIDTH = 512
HEIGHT = 512


def rgb_pixel(x, y):
    return ((x * 37 + y * 11 + 17) & 0xFF,
            (x * 7 + y * 43 + 83) & 0xFF,
            (x * 19 + y * 5 + 149) & 0xFF)


def gray_pixel(x, y):
    value = (x * 29 + y * 31 + (x * y) // 17 + 23) & 0xFF
    return value


def alpha_pixel(x, y):
    period = (x * 13 + y * 17) % 64
    if period == 0:
        alpha = 0
    elif period < 8:
        alpha = 64
    elif period < 24:
        alpha = 128
    elif period < 40:
        alpha = 192
    else:
        alpha = 255
    return ((x * 23 + y * 3 + 7) & 0xFF,
            (x * 5 + y * 29 + 71) & 0xFF,
            (x * 31 + y * 13 + 139) & 0xFF,
            alpha)


def png_chunk(kind, payload):
    return (struct.pack(">I", len(payload)) + kind + payload
            + struct.pack(">I", zlib.crc32(kind + payload) & 0xFFFFFFFF))


def write_png(path, channels, pixel):
    rows = bytearray()
    for y in range(HEIGHT):
        rows.append(0)
        for x in range(WIDTH):
            value = pixel(x, y)
            rows.extend(value if channels != 1 else (value,))
    color_type = {1: 0, 3: 2, 4: 6}[channels]
    header = struct.pack(">IIBBBBB", WIDTH, HEIGHT, 8, color_type, 0, 0, 0)
    png = (b"\x89PNG\r\n\x1a\n" + png_chunk(b"IHDR", header)
           + png_chunk(b"IDAT", zlib.compress(bytes(rows), 9))
           + png_chunk(b"IEND", b""))
    path.write_bytes(png)


def write_ppm(path, channels, pixel):
    magic = b"P6" if channels == 3 else b"P5"
    with path.open("wb") as output:
        output.write(magic + b"\n# phase3 deterministic fixture\n")
        output.write(f"{WIDTH} {HEIGHT}\n255\n".encode("ascii"))
        for y in range(HEIGHT):
            for x in range(WIDTH):
                value = pixel(x, y)
                output.write(bytes(value if channels == 3 else (value,)))


def jpeg_from_ppm(ppm, jpeg):
    subprocess.run(["cjpeg", "-baseline", "-quality", "95", "-optimize",
                    "-outfile", str(jpeg), str(ppm)], check=True)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    args.output.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="tc-phase3-fixtures-") as temporary:
        temporary = Path(temporary)
        color_ppm = temporary / "rgb.ppm"
        gray_ppm = temporary / "gray.pgm"
        write_ppm(color_ppm, 3, rgb_pixel)
        write_ppm(gray_ppm, 1, gray_pixel)
        jpeg_from_ppm(color_ppm, args.output / "rgb565-source.jpg")
        jpeg_from_ppm(gray_ppm, args.output / "gray8-source.jpg")
    write_png(args.output / "rgb565-source.png", 3, rgb_pixel)
    write_png(args.output / "gray8-source.png", 1, gray_pixel)
    write_png(args.output / "argb4444-source.png", 4, alpha_pixel)


if __name__ == "__main__":
    main()
