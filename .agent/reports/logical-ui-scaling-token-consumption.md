<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Logical UI scaling token-consumption report

Estimates below are retrospective shares of the approximately 3.51 million
tokens consumed while executing this plan. They are directional, based on the
recorded investigation and validation loops rather than exact per-command
telemetry.

1. Repeated plan/state/review rereads and continuation recovery — 24%.
   Automatic continuations repeatedly injected the full goal and prompted broad
   reorientation, including stale review material.
2. Native macOS image/Skia investigation and redeploy cycles — 18%.
   Dylib hashes, TCZ deployment, direct runtime execution, and readback
   diagnosis were necessary but repeated after small fixture changes.
3. Android packaging, bundletool download/retry, installation, and log analysis
   — 16%. The slow partial bundletool download and repeated device deploys were
   the largest late-stage operational cost.
4. Java renderer primitives, blits, and scaled-text implementation/review — 10%.
5. Text-layout and SkFont measurement reconciliation — 8%.
6. Image ownership, transforms, frames, and ABI field-order audit — 7%.
7. Screenshot/PID capture and visual inspection — 4%.
8. Git/diff/log/status inspection and evidence maintenance — 4%.
9. Non-Skia macOS audit — 3%.
10. Android resource/TCZ loader diagnosis — 3%.
11. Build-output handling and dependency bootstrap diagnostics — 2%.
12. Miscellaneous formatting, header validation, and report work — 1%.

The estimates total 100%. The principal avoidable cost was repeated recovery
from a stale review and automatic continuation text. A future run should treat
the compact state file as authoritative after the first review, retain one
machine-readable validation ledger, and avoid rereading completed milestones.

For Android, cache and verify bundletool before a final milestone, keep one
known-good AAB template, and add a focused resource-lookup smoke before the
full DANFE fixture. That would have exposed the `TCUI.tcz` lookup failure before
the costly high-density deployment loop. Batch fixture edits before rebuilding
the SDK/AAB, and preserve a single hash record for each deployed runtime.
