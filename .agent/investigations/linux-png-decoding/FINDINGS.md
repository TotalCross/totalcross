<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Findings

## Previously Established Evidence

- Issue #564 records the historical 136-byte RGBA PNG failure at source
  `61034a7562e7fa43df076dcc38bcd48128605395`, with
  `IDAT: ADLER32 checksum mismatch` through `Image(File)` and
  `Image(byte[])`. JPEG controls passed.
- I02 run `45949849457` tested frozen source
  `6b915883dd202f6db891990f6c3af26fb645ecc7`. Its unmodified process aborted at
  startup (exit 134) before PNG cases. With the preserved test-only startup
  shim, the original RGBA file and byte-array cases decoded, while indexed PNG
  was rejected during construction. This does not establish a fix or root cause.
- I06 run `45989644203` reports that both baseline and corrected SDK probes
  reached native decoding of the original 2×2 PNG and failed. The ownership
  assertions then used a JPEG control. The exact original source is at
  performance-lab commit `d084949d106f9606a7639d851ad66b97734df1ec`.
- I02 and I06 reports are in the referenced performance-lab branch. Their
  retained evidence and fixture lineage are summarized in `evidence/index.md`.

## Current-Master Baseline

Pending hosted run against the selected master revision. Do not infer current
behavior from the frozen-master shimmed results above.

| Manifestation | Current status | Evidence |
| --- | --- | --- |
| 36×36 RGBA decode through `Image(File)` and `Image(byte[])` | PENDING | Hosted Linux probe |
| Original 2×2 PNG during native image drawing | PENDING | Original I06 sequence |
| Indexed PNG palette-transparency inspection | PENDING | I02 constructor case |
| Normal Linux startup without shim | PENDING | PR #568 hosted startup validation |

## Causal Findings

None. Do not treat symbol overlap, allocation alignment, or the previously
observed shimmed pass as a cause or correction.
