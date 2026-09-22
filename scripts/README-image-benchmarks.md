<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Distributed image benchmark bundles

The image benchmark package contains the distributed scroll benchmark. Build a
default bundle from the SDK ZIP and a corpus root containing `imag`:

```sh
bash scripts/package-image-scroll-benchmark.sh \
  --sdk-zip /path/to/TotalCross-7.2.2.zip \
  --corpus /path/to/win32 \
  --output /path/to/output \
  --target macos-arm64
```

For Windows, provide the benchmark source and an explicit SDK source
attestation:

```sh
bash scripts/package-image-scroll-benchmark.sh \
  --sdk-zip /path/to/TotalCross-version.zip \
  --corpus /path/to/corpus \
  --output /path/to/output \
  --target windows-x64 \
  --source-commit "$SOURCE_COMMIT" \
  --sdk-source-commit "$SOURCE_COMMIT"
```

Only `corpus/imag` is copied by default. Pass `--include-decode` to preserve
the existing decode suite; the input root must then also contain `lossless`,
`decode-baseline`, `decode-fast`, `aggressive-480`, and `aggressive-540`.
Each variant contributes the same 663 `.jpg`-named paths. The package records
the source revision, runtime SHA-256, and Windows `tcvm.dll` SHA-256 in
`manifest.json`; a stale Windows runtime is rejected during packaging.
Windows packaging also requires `--sdk-source-commit` (equal to
`--source-commit`) as an explicit `sdkSourceAttestation`. This is an
operator-provided provenance assertion, not cryptographic proof that the SDK
ZIP was built from that revision.

From the extracted bundle directory, run the full suite with:

```sh
python3 run-benchmark.py --phase full
```

The scroll CSV keeps `frame_time_ns` as the paced interval between frame
starts. The active-work columns measure the same frame's work region:
`scroll_work_ns` covers `scrollContent`, `paint_work_ns` covers
`repaintNow()`, and `work_time_ns` covers both operations. JPEG deltas in that
row are captured around the same region. Run summaries and `summary.csv`
include P50/P95/P99/MAX work and paint aggregates.

The default Windows ZIP runs the self-test followed by 44 benchmark processes:
30 reduced ImageOptimizations cases, two scroll-reuse correctness cases, six
scroll-reuse performance cases, and six release/default-scroll cases. The
correctness and performance cases invoke
`--profile=scroll-raster-reuse-poc --rendering-reuse=off|on`; every one uses
mask 0, prefetch on, the 120-image POC workload, and cold plus warm passes.
The release/default-scroll cases use the full 663-image client corpus, omit
the mask argument, require effective mask 32799 through Java and native
draw/decode and observed-mask probes, and run reuse off/on with accounting
off.

With `--include-decode`, the full phase additionally runs the existing
90-process decode matrix and aggregation. Without decode assets, any decode
phase fails clearly and leaves the scroll phases unaffected.

The scroll `results/summary.csv` retains its baseline and frame timing columns
and also carries compact JPEG decode count/ns fields plus writePixels attempts,
hits, fallbacks, candidate counts, and the matrix/save-count/size-mismatch
reject counts. Detailed reject reasons, phase-separated `jpegDecode` data, and
feature statuses remain in each run's `counters.json`. Attempt-based features
use `NOT_REACHED`, `ATTEMPTED_NO_HIT`, or `EXERCISED` in addition to
`DISABLED`.

The reduced ImageOptimizations profile uses exactly these masks with prefetch
on, accounting off, and three rounds:

`0, 6, 8, 16, 32, 8192, 16384, 32768, 32795, 32799`

This retains dependency combinations, including writePixels plus opacity;
opacity alone is not tested.

For a focused scroll-reuse profile, use:

```sh
python3 run-benchmark.py --profile scroll-raster-correctness --phase matrix
python3 run-benchmark.py --profile scroll-raster-performance --phase matrix
python3 run-benchmark.py --profile release-default-scroll --phase matrix
```

For package checks without a matrix, `--phase self-test` validates the bundle.
Decode checks remain separate when decode assets are included:
`--phase decode-self-test` followed by `--phase decode-smokes`.
