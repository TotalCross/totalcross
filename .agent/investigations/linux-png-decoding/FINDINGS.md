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

Hosted Linux run `46061400453` completed successfully against exact master
revision `6beef491f28c63aa364d3661dd5c37ccff01fb0d`. All probes ran without
`LD_PRELOAD`.

| Manifestation | Current status | Evidence |
| --- | --- | --- |
| 36×36 RGBA decode through `Image(File)` and `Image(byte[])` | NOT REPRODUCED | Both I02 cases constructed and decoded 1,296 pixels; no exception |
| Original 2×2 PNG during native image drawing | NOT REPRODUCED | I06 completed all seven stages and drew exact red `0xff0000` |
| Indexed PNG palette-transparency inspection | CONSTRUCTOR REJECTED | I02 still reports `Invalid or unsupported encoded image` |
| Normal Linux startup without shim | PASS | Five startup/path cases exited 0 and emitted completion/control markers |

The RGBA results report `pixel0=0`, which matches the retained fixture's
transparent-black top-left sample. Indexed rejection remains a separate
behavior; it is not evidence for either non-reproduced failure.

## Causal Findings

None. Neither the historical ADLER32 symptom nor the I06 drawing failure
reproduced on current master, so there is no current failure to localize. The
indexed constructor rejection is observed but has not been causally investigated.
Do not treat symbol overlap, allocation alignment, or the historical shimmed
pass as a cause or correction.
