<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Defer explicit JPEG factory materialization

This ExecPlan follows `AGENTS.md` and `.agent/PLANS.md`.

## Purpose / Big Picture

Create the focused pre-Phase-4 milestone requested from the frozen Phase-3
tip `224682b15a244201718701ad24be8f9d54b3fa71`. The public
`Image.getJpegBestFit` and `Image.getJpegScaled` factories will eagerly capture
and structurally validate an owned JPEG source, expose correct dimensions, and
return a deferred Image whose first pixel barrier or draw performs exactly one
decode. Existing automatic destination-scale `TARGET_DECODE` remains separate.

The work is confined to branch `perf/image-jpeg-factories-lazy`; the frozen
Phase-3 branch is not modified and Phase 4 is not started.

## Working Set and Resume Protocol

Read `.agent/state/image-jpeg-factories-lazy.md` first on resume. It records
the active slice, last commit, exact paths, validation, deferrals, and next
command. Append compact results to
`.agent/evidence/image-jpeg-factories-lazy.jsonl`; do not copy raw logs there.
Update `.agent/reports/image-jpeg-factories-lazy-editorial.md` at milestone
closeout. Existing Phase-3 handoff files
`.agent/state/image-opt-phase3-formats.md`,
`.agent/evidence/image-opt-phase3-formats.jsonl`, and
`.agent/reports/image-opt-phase3-formats-editorial.md` are read at final
reconciliation so their runtime and docs provenance remains separate.

## Progress

- [x] Branch from the exact Phase-3 final docs tip.
- [x] Add immutable explicit JPEG policies to the deferred pipeline.
- [x] Route both public factories through common Java lazy construction.
- [x] Retire public native eager factory replacements and add native decode
  primitives for deferred policy resolution.
- [x] Replace the eager regression and add deployed lazy/draw/parity coverage.
- [x] Run focused, macOS, and Android build/package validation; reconcile the
  handoff and stop before Phase 4. Android device execution and the GitHub
  matrix were blocked/unavailable for this unpublished candidate and remain
  explicitly deferred.

## Current Architecture and Scope

`EncodedImageSource` already owns captured bytes or an `ImageEncodedBag`,
performs structural JPEG validation, exposes intrinsic metadata, and supports
deterministic-failure caching plus automatic reduced JPEG decode reuse. The
deferred `ImagePipeline` is an immutable linked root-plus-operation structure;
`Image.resolveForDrawing` and `materializeCanonicalChecked` are the materialize
barriers. Native code already decodes from the owned bag through private
`decodeEncodedSource*` bridges.

The current public factories capture a source, append a smooth-scale operation,
and call `materializeCanonicalChecked`, while deployed replacement methods
decode eagerly in `image_Image.c`. This milestone changes that boundary only.

The root pipeline will carry an immutable `ImageDecodePolicy` separate from
`EncodedImageSource`:

- `TARGET_DECODE` is the existing automatic policy selected from a drawing
  request and remains used by ordinary encoded constructors/transforms.
- `BEST_FIT(targetWidth,targetHeight)` selects the historical inclusive
  libjpeg best-fit denominator at materialization.
- `EXPLICIT_RATIO(numerator,denominator)` preserves the caller's positive
  ratio, including arbitrary values such as `3/4`, at materialization.

The factory root metadata is the policy's expected output dimensions, while
the source remains the authority for ownership and structural validity. Chained
smooth, alpha, export, mutation, `getPixels`, `getGraphics`, and drawing paths
continue through the existing pipeline/barrier machinery.

## Plan of Work

1. Add `ImageDecodePolicy` and carry it immutably through root and appended
   `ImagePipeline` nodes. Add a factory-specific deferred initializer that sets
   output metadata without allocating pixel storage.
2. Implement policy-aware root resolution. BEST_FIT uses the exact existing
   inclusive denominator selection; EXPLICIT_RATIO uses a native bridge that
   passes the original numerator/denominator to libjpeg. JavaSE uses the
   existing full decode plus smooth resampling for arbitrary explicit ratios,
   and the existing denominator reader for BEST_FIT. Publish only complete
   results; deterministic corrupt-payload failures may be cached, while
   transient/resource failures leave the deferred pipeline retryable.
3. Remove `@ReplacedByNativeOnDeploy` from the two public factories and retire
   only their public native registrations/implementations. Add private native
   BEST_FIT and EXPLICIT_RATIO decode bridges and keep the existing automatic
   TARGET_DECODE and full-decode registrations intact. Native JPEG decode must
   consume the owned bag directly.
4. Replace the regression requiring materialized factories with assertions for
   deferred state, zero decode before use, one reusable decode on first/repeated
   draw, dimensions/policy, ratio coverage (`1/8`, `1/2`, `3/4`, `1/1`), chained
   operations, retry behavior, and independent eager-reference parity. Extend
   the existing deployed image smoke with real screen/draw coverage and a GPU
   software-path guard. Add a focused factory-only/first-draw/repeated-draw
   benchmark that records decode and materialization counts without rerunning
   Phase-3 matrices.
5. Validate headers/native registration/ABI, focused image tests, SDK dist,
   Release macOS software-Skia build and smokes, Android GPU smoke if the
   device is available, and the existing GitHub build matrix at the final
   runtime candidate. Record skipped expensive checks and blockers explicitly.

## Decision Log

- Decision: Store factory decode intent on the pipeline root, not on
  `EncodedImageSource`. Rationale: one immutable owned source can remain a
  reusable ownership/validation object without per-call policy state.
- Decision: Use private Java decode bridges for native materialization and keep
  pipeline traversal in Java. Rationale: native code performs only the actual
  libjpeg decode from the owned bag and does not reconstruct ImagePipeline.
- Decision: Keep the public factory output dimensions fixed at factory time.
  Rationale: dimensions are metadata and must be correct before pixel decode;
  later chained transforms remain ordinary pipeline nodes.
- Decision: Retire only public eager factory native replacements. Rationale:
  private decode primitives and their registrations remain required by the
  common deferred resolver and preserve deployed ABI behavior.

## Validation and Acceptance

Level 3/4 is required because this changes an Image ABI boundary and native
dispatch. Acceptance requires unchanged public signatures, correct eager
exception timing, zero pixel/decode work before use, one reusable decode for
repeated use, exact BEST_FIT boundaries, arbitrary EXPLICIT_RATIO semantics,
compact JPEG backing eligibility without unintended RGBA promotion, JavaSE vs
deployed parity within existing JPEG tolerance, and no software-only path on
GPU.

Focused commands are the `totalcross.ui.image.*` tests, `dist -x test`,
Release software-Skia macOS build/smoke, targeted Android deployment smoke,
the existing GitHub matrix, focused header validation, native registration/ABI
tests, and `git diff --check`. Full Phase-3 performance matrices are not
repeated. Raw command output belongs in task-specific logs.

## Risks and Open Questions

Libjpeg explicit-ratio support must remain available for arbitrary positive
ratios and must not be silently converted to automatic tiers. JavaSE's
arbitrary-ratio fallback uses its established smooth resampler, so parity is
validated with the existing quality tolerance rather than byte equality.
The dirty worktree contains unrelated untracked artifacts; only scoped files
are staged and committed. Hosted/GPU validation may be unavailable and will be
reported rather than inferred.

## Idempotence and Recovery

All source changes are additive or scoped edits and can be retried from the
last commit. Use new ignored build/log directories; do not clean broad roots or
touch `TotalCrossVM/deps/totalcross-depot-tools` generated artifacts. If native
registration generation fails, restore consistency across `NativeMethods.txt`,
`NativeMethods.h`, `NativeMethodsPrototypes.txt`, and
`nativeProcAddressesTC.c` before any build. Preserve all unrelated untracked
files.

## Outcomes & Retrospective

Final runtime SHA: `4ce4c74d2d6310ff0eb603546c81da28ebf1bc9f`. Final docs tip is
the closeout branch tip after this plan/state/evidence/editorial commit. The
exact policy representation is `ImageDecodePolicy` on the immutable
`ImagePipeline` root with separate `TARGET_DECODE`, `BEST_FIT(w,h)`, and
`EXPLICIT_RATIO(n,d)` kinds. Public native eager factory implementations and
registrations were removed; private decode primitives and native-bag bridges
remain. Factory argument/path/structure/metadata failures are eager, while
payload/decoder failures are deferred; deterministic failures cache and
transient failures retry. The 60-sample benchmark passed zero-decode factory
only and one-decode first/repeated-use gates. macOS and Android build/package
validation passed; Android install was blocked by device policy and no GitHub
run exists for the unpublished candidate. Decision: `NO-GO` for Phase 4 until
the final candidate cross-platform matrix and installable Android GPU smoke
are complete.
