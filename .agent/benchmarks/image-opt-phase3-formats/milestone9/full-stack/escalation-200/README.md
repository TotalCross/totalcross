<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Matrix B 200-sample escalation

This directory preserves the required 200-sample S1/S2 rerun after the first
60-sample S2 peak-RSS signal exceeded 5%. Both processes used the same
`milestone9-full-stack` workload, three warmups, 50 ms external RSS sampling,
and exact scenario settings as the authoritative Matrix B capture.

- S1: exact Phase-2 `6d1c95f77fcb9c74d19b4e9393dba7c82cd37aee`.
- S2: Phase-3 pre-candidate tip
  `ecad01ce1ffac609bc7d61c029c4b9e981518145`.
- Input hashes and S1/S2 output hash match the authoritative Matrix B files.
- `s1-pre.csv.gz` and `s2-disabled.csv.gz` contain 200 rows each; runner
  summaries record peak RSS and process success.
- Matched `vmmap -summary`/`ps` captures at samples 100 and 150 are retained
  in the sibling `../matched-diagnostics/` directory.
