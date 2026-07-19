<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Editorial Handoff: TotalCross IR, JIT, and AOT

Status: interim. Reconcile this report when Milestone 8 completes or when the
user explicitly requests editorial work; finalize it with Milestone 9. It is
not an execution diary.

## Editorial Summary

TotalCross now has a typed intermediate representation between its register
bytecode and optional execution backends. The legacy `executeMethod` interpreter
remains the compatibility oracle and fallback; TCIR, SLJIT, and generated C are
experimental and opt-in.

## Original Plan versus Actual Outcome

Milestones 1–7 established documentation, TCIR, frontend, reference execution,
SLJIT, generated C, and default-off mixed-mode dispatch. Milestone 8 has added
selected numeric/reference/control-flow/static-call/allocation semantics but is
not complete. Fields, arrays, handlers, virtual calls, monitors, special cases,
platform release gates, and production publication remain deferred.

## What Changed

The core resides in `TotalCrossVM/src/tcvm/ir/`; SLJIT and AOT are under
`TotalCrossVM/src/tcvm/jit/` and `TotalCrossVM/src/tcvm/aot/`. Converter fixtures
and differential tests provide the evidence boundary. Runtime ABI version 5
adds a checked allocation helper contract that publishes an object in a visible
reference home before unlock.

## Decisions and Trade-offs

Backend and dispatch flags remain default-off. Effects that can throw, collect,
lock, resolve symbols, or mutate the heap are modeled conservatively and retain
whole-method fallback until tested. Static calls and allocation preflight their
bindings before execution to avoid partial effects followed by fallback.

## Unexpected Problems and Discoveries

The historical arithmetic benchmark exposed a dispatch mutex regression but does
not measure later reference, call, switch, or allocation semantics. Allocation
also showed that a returned object is not a sufficient GC root: publication in a
destination `regO` home must precede unlock.

## Validation and Measurable Results

The latest allocation checkpoint retained 6,398 legacy four-way comparisons and
added 16 TCIR/SLJIT/AOT allocation-contract comparisons. Focused host Release,
ASan, UBSan, default-off, converter, and Android arm64-v8a/API 23 compilation
evidence passed. This does not prove a fully initialized TCZ/class loader/object
memory manager or moving/forced GC. See the evidence index for commands and the
archive baseline for historical raw artifacts.

## Useful Evidence and Examples

Use converter goldens, `tcir_tests.c`, `tcir_differential_tests.c`,
`tcir_runtime_tests.c`, and commits `e7ea5cb14`/`051800dcd` for allocation
examples. The evidence index and archive provide revision and artifact context.

## Limitations, Remaining Work, and Open Questions

The project still needs field/class-initialization semantics, arrays, exceptions
and handlers, virtual/interface calls, monitors, special cases, real
object-memory-manager GC stress, platform/device validation, and release policy.

## Possible Article Angles

Defer detailed article angles until a milestone boundary or finalization so the
report describes settled implementation and measured behavior.

## Suggested Narrative

Defer the final narrative until the remaining semantic and platform boundaries
are resolved.

## Claims Requiring Human Review

Any product-performance, platform-support, security-policy, or release-readiness
claim needs normal technical and editorial review. No broader claim is supported
by the interim proof of concept.
