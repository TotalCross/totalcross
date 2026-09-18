<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Editorial handoff — image-scroll diagnostics macOS instrumentation

This report is completed at the end of Plan 1. It will summarize delivered
diagnostic contracts, exact commits, validation, limitations, and any
discoveries without claiming optimization results.

## Initial scope

Instrument writePixels fallback classification and native JPEG decode timing
for the existing macOS image-scroll benchmark. No optimization policy changes
are in scope.
