<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Image/rendering final reconstruction state

- Active milestone: 7, prepare the PowerShell-only Windows x64 validation package after M6 production-candidate measurement.
- Base / branch: `5a44f503bf6fa1bec350f1218f4d501a70fc4812` / `fix/image-rendering-final-reconstruction`.
- Last commit: `bdd27273e` — record integration validation; M1–M5 implementation commits are `3be3a1eaf`, `f45b0a1f9`, `9896ec9ad`, `c7bf28d09`, and `2f81accfb`.
- Active paths: candidate package and benchmark results in sibling checkout `/Users/flsobral/repos/totalcross-performance-lab`, branch `perf/image-rendering-benchmarks`; preserve commits `5b4902108c73842db54287f2b8d0dcec76a6573c` and `264b9fba8581f08f3c0e4f31529831b7270d2c95`. Windows runtime artifacts must match candidate `bdd27273ead29bab320cba43b9ebad27be9b87ad`.
- Next concrete action: prepare the Windows package only if tested Windows runtime files matching the candidate SHA are available; otherwise document the package blocker and keep Windows execution pending. Continue with the feature/history audit using existing reports and merged GitHub metadata.
- Validation completed: M1–M5 complete. M5 passed the 317-test SDK regression suite (20 skipped), SDK distribution, Skia surface assertions, deployed Skia image smokes, and the legacy macOS native scroll-reuse smoke with `nativePrimitive=true`. M6 static-warm and repeated-scroll candidate runs passed; the generic first-use admission regression confirms no final-raster cache entry is retained on first observation. Focused copyright validation and `git diff --check` passed. Exact commands, counters, timings, package identity, and logs are indexed in `.agent/evidence/image-rendering-final-reconstruction.md`.
- Deferred validation: Windows package creation is blocked by the absence of matching Windows runtime files; existing local Windows artifacts attest commit `8945bbff4825a1aa695a9dbdf189e0b5fa74ce8a`, not the candidate. No local Windows build or execution is authorized. Repeated-scroll diagnostics are unsupported in this diagnostics-off package, so feature lifecycle counters, materialization time, writePixels counts, and process/live/peak bytes are unavailable. Do not infer these values. Do not build Android, Linux, iOS, or WinCE. Completed P12 probes remain accepted evidence and must not be repeated.
- Active decisions: generic final-raster admission remains second-observation; only an attached persistent UI owner may request immediate admission; native `TARGET_COLOR`/`PHYSICAL` admission remains unchanged; the existing single pipeline slot clears after the last owner leaves.
- Blockers: matching Windows runtime artifacts for the candidate SHA are not available in the existing local outputs.
- Deliberate untouched local state: dirty source checkout `/Users/flsobral/repos/totalcross-image-scroll-raster-fast-path` and other dirty sibling checkouts.
- Resume command: `cd /Users/flsobral/repos/totalcross-image-rendering-final-reconstruction && sed -n '1,160p' .agent/state/image-rendering-final-reconstruction.md && git -C /Users/flsobral/repos/totalcross-performance-lab status --short --branch`.
