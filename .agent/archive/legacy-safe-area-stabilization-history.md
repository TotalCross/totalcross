<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Legacy safe-area stabilization history

## 2026-08-10T17:39:06Z — execution started

Established the baseline at `09dc39143c7edf0363ad3c1670bdd16e141b4572`.
The local branch `feat/logical-ui-scaling2` points exactly to the requested
remote `origin/feat/logical-ui-scaling`. No target tracked file had local
changes. Unrelated untracked files were recorded and left untouched.

## 2026-08-10T17:44:27Z — milestone 1 complete

Commit `d74000089` removed the post-checkpoint generic fade/screenshot repair
path and its dedicated tests/smoke. `SafeAreaLayoutTest` and focused header
validation passed. Work advanced to SlidingWindow reconstruction.

## 2026-08-10T17:49:52Z — milestone 2 complete

Commit `efb9b7ff6` restored the Window-based SlidingWindow/MaterialWindow path.
Safe-area geometry tests cover directional preparation, asymmetric resize and
reopen, and CENTER fade selection. Work advanced to TopMenu and SideMenu.

## 2026-08-10T17:56:14Z — milestone 3 complete

Commits `82b2ffff5` and `9e430ce30` restored TopMenu/SideMenu and added usable
safe-width sizing. Legacy geometry, fixed bars, input wiring, and shared
safe-area coverage passed. Work advanced to presentation isolation.

## 2026-08-10T18:02:41Z — milestone 4 complete

Commit `a44665341` removed Window coupling and relocated the compile-clean
deferred package without API widening. The focused test passed; provenance
evidence is pending human review. Work advanced to final release validation.

## 2026-08-10T18:23:54Z — milestone 5 validation complete

Commit `b3fcb3111` added the JavaSE visual and replacement smoke coverage. Ten
screenshots were inspected; the 18-test focused set, non-clean SDK distribution,
smoke compilation, and native macOS run passed. Final repository checks remain.

## 2026-08-10T18:30:02Z — execution complete

Final diff and header checks passed for 27 changed files. Nine new tracked files
and all 16 pending-audit files meet the size limits; all 10 visual PNGs are under
10 KiB. Android/iOS were intentionally skipped and nothing was pushed.
