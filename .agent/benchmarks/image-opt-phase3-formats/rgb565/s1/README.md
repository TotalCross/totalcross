<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# RGB565 S1 baseline

This is the exact pre-RGB565 baseline captured on Phase-2 runtime
`86bfeafe388ce866236c3ae58eecb144664895e2` using the frozen true-base adapter
at harness digest
`9a0bc2a348b197f597a11f10b2ca9d5787ab7b0f2937c70a643e4fc6f09018ae`.

Command shape:

```sh
python3 scripts/run-image-optimization-benchmark.py \
  <deployed ImageCompactFormatsBenchmarkApp> \
  --scenario pre --samples 60 \
  --arg=--workload=rgb565 --arg=--phase2=false --rss-interval 0.05
```

The fixture input hashes and full output hashes are emitted in `s1.csv`.
All 60 samples completed, elapsed time ranged from 62 to 64 ms, and peak
external RSS was 139520 KiB. Phase-3 format and compact-decode counters are
unavailable on the true base and are represented by the adapter shims.

