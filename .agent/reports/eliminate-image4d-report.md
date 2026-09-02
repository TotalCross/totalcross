<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Report: Image4D elimination

## Original intention

The change unified the JavaSE and deployed image implementations. Before the
refactor, `Image` was the JavaSE implementation while `Image4D` was a separate
deployment replacement. That arrangement duplicated behavior, obscured the
native ABI, and allowed deployment-only fixes to diverge from the SDK class.

## Final implementation

`Image` is now directly deployable and carries the native runtime state in the
required storage-category order. The class retains native creation and parsing
bridges, cleanup and locked-texture behavior, shared state for derived images,
and the deployed native method boundary.

`Image4D` was removed. The JavaSE-only reader remains isolated behind a small
deployment replacement boundary, so desktop image loading does not become a
deployed dependency. Native replicate scaling, smooth scaling, rotation,
fade, alpha, and touch-up transformations are preserved in `Image`, including
the historical native argument mapping and the final rotation `fillColor`
behavior.

Copies and derived images preserve hardware scale, logical dimensions, content
scale, frame state, and shared texture bookkeeping. `Image.getGraphics` no
longer requires a `Launcher` instance to select its font. The native test
comment and converter checks now refer to `Image` consistently.

## Problems found and decisions

The original replacement class had an ABI-sensitive field ordering issue around
the cached hash value. A converter test was established first, and the field
was moved out of the native prefix before the implementation was unified. The
unified class was then arranged to match the native I32, object, and value64
field prefixes exactly.

The native transformation entry point has a historical parameter ordering that
does not read like its Java parameter names. The final bridge preserves that
contract and keeps JavaSE algorithms available when native replacement is not
active. Native-created and copy-created instances use the same state model so
that texture cleanup and hardware scale do not depend on which construction
path was used.

## Validation performed

The following validations passed on the rebuilt history:

- Focused converter and image tests covering `ImageFieldAbiTest` and the
  `totalcross.ui.image` test package.
- SDK `dist -x test` build.
- `artifactContentTest`, including public artifact boundary assertions.
- Release CMake configuration and Ninja build of the native `tcvm` target.
- Image ABI smoke compilation and macOS deployment.
- Direct macOS execution of the deployed image smoke application. Constructor,
  decoding, logical and physical dimensions, frame handling, color mutation,
  resize, texture upload/recreation, PNG round-trip, replicate/smooth scaling,
  rotation, touch-up, fade, alpha, and hardware-scale-copy assertions all
  passed.
- Source search for stale `Image4D` references and inspection of the generated
  SDK artifact; neither contains an `Image4D` class.
- Full functional-tree comparison against the preserved pre-rebuild final
  state, ignoring only the deliberate removal/consolidation of agent
  documents and the whitespace cleanup.
- Diff, copyright-header, and commit-message checks for the reconstructed
  commits.

## Limitations

The deployed native smoke was run on macOS with the available OpenGL runtime.
Other platform-specific native builds were not repeated because no production
image implementation changed after this validated behavior was reconstructed.
The native VM test header retains its existing skipped test body; the active
coverage comes from the converter ABI test and the deployed smoke assertions.

## Result

The branch now has a short five-commit history: an initial plan, an ABI
baseline, the unified implementation, consolidated tests, and this report.
The functional SDK and native state matches the prior final branch state while
the intermediate fixes, reversions, checkpoints, and agent-only evidence/state
are excluded from the reconstructed history.
