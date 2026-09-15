<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Distributed image benchmark bundles

The image benchmark package combines the existing scroll benchmark with an
image-decode suite. Build a bundle from the SDK ZIP and six-variant corpus:

```sh
bash scripts/package-image-scroll-benchmark.sh \
  --sdk-zip /path/to/TotalCross-7.2.2.zip \
  --corpus /path/to/win32 \
  --output /path/to/output \
  --target macos-arm64
```

The corpus root must contain `imag`, `lossless`, `decode-baseline`,
`decode-fast`, `aggressive-480`, and `aggressive-540`. Each variant contributes
the same 663 `.jpg`-named paths. The package maps the last two directories to
`aggresive-480` and `aggresive-540` in the bundle. Format is detected from each
file's contents. The bundle contains six `Decode*Lib.tcz` SDK library resources
alongside the scroll and decode applications.

From the extracted bundle directory, run the full suite with:

```sh
python3 run-benchmark.py --phase full
```

The full phase runs the 126-process scroll matrix and aggregation, then the
90-process decode matrix and aggregation, and finally writes one combined ZIP
under `results/`. The decode matrix covers five acquisition/order/size
scenarios, six variants, and three rounds. Per-image timings remain in
nanoseconds in `results/decode/decode-detailed.csv`; process and variant
summaries are in `process-summary.csv` and `decode-summary.csv` in that same
directory.

For package checks without either full matrix, `--phase self-test` validates
the bundle and `--phase smokes` runs the four scroll smoke processes. Decode
checks are separate: run `--phase decode-self-test` before
`--phase decode-smokes`; the latter runs the five prescribed decode smoke
combinations and writes `results/decode/smoke-summary.csv`.
