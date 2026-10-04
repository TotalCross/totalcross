<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Image/rendering final reconstruction state

- Status: complete. Active milestone: none.
- Implementation and historical feature audit are complete. No required production behavior in the audited image/rendering/scroll chain is classified `STILL MISSING`.
- Branch: `fix/image-rendering-final-reconstruction`, based on `5a44f503bf6fa1bec350f1218f4d501a70fc4812`. PR #488 is open and pending review/merge.
- Production benchmark candidate: `bdd27273ead29bab320cba43b9ebad27be9b87ad`. The candidate SHA and its artifact hashes/results are preserved in `.agent/evidence/image-rendering-final-reconstruction.md`; later cleanup commits do not change runtime behavior.
- macOS validation: complete. The focused SDK integration selection, SDK distribution, Skia surface assertions, deployed image smokes and legacy scroll-reuse smoke passed.
- Merge Flow run `37172383967`: success on the pre-cleanup branch head. The Windows and `windows-native-legacy` builds passed, along with the listed SDK, macOS, iOS, Android and Linux jobs. A non-expired Windows artifact was produced; `linux-arm32v7-cross` was skipped.
- Dedicated Windows performance benchmark: not executed. Windows build validation is available, while Windows performance remains unmeasured.
- Architecture retained: one exact final materialized representation per pipeline; generic/transient resolution waits for a second observation; a persistently owned UI consumer may admit its first successful materialization; native `TARGET_COLOR` and `PHYSICAL` variants retain second-observation admission.
- No P12 investigation, production benchmark, or platform benchmark matrix was rerun during cleanup. Candidate diagnostics do not expose repeated-scroll cache lifecycle counters, materialization time, writePixels counts or derived-raster memory.
- Durable records: `.agent/plans/image-rendering-final-reconstruction.md`, `.agent/evidence/image-rendering-final-reconstruction.md`, and `.agent/reports/image-rendering-reconstruction-final.md`.
