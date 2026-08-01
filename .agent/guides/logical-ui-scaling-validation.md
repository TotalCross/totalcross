<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Logical UI scaling validation guide

Read the relevant section before closing each milestone. This guide restates the
required proof so execution does not depend on earlier plans or conversation
memory.

## Validation principles

Use the smallest validation that proves the current slice:

1. static or diff check;
2. focused unit test;
3. focused integration test;
4. module build;
5. renderer or platform smoke test;
6. full distribution build;
7. clean full build only when stale artifacts are demonstrated.

Do not run `clean` by default. Redirect verbose output to
`artifacts/logical-ui-scaling/logs/` and report only the command, status, relevant
errors, and a short tail.

Every evidence record includes:

    timestamp
    base and tested commit
    milestone and slice
    command or manual action
    renderer and platform
    status
    concise result or counts
    full log or artifact path
    hash when the artifact exists and is valid
    limitation or deferred validation reason

## Source identity and isolation

Before implementation:

    git fetch origin master
    git rev-parse origin/master
    git worktree list
    git status --short -- <planned paths>

Record the base commit. The feature branch must have that commit as its direct
starting point. Do not use prior-session branches, patches, or generated source.

Before final acceptance, record:

    git merge-base origin/master HEAD
    git log --oneline --decorate --max-count=20
    git diff --stat origin/master...HEAD

Any later master movement is not automatically merged during execution. Record
the original base and rebase only under normal project policy and with a fresh
validation pass.

## Static audits

Use focused searches, adapting exact paths after inspection:

    rg -n "screenDensity" TotalCrossSDK TotalCrossVM
    rg -n "UnitsConverter\.toPixels|Control\.DP|\bDP\s*[+-]" TotalCrossSDK
    rg -n "\bfmH\b|\.fm\.|stringWidth\(" TotalCrossSDK/src/main/java
    rg -n "getWidth\(\)|getHeight\(\)|\bwidth\b|\bheight\b" \
      TotalCrossSDK/src/main/java/totalcross/ui/image
    rg -n "applyChanges|readPixels|writePixels|copy.*Pixels" \
      TotalCrossSDK TotalCrossVM

Classify results rather than replacing blindly.

At milestone closure:

    git diff --check
    git diff --stat
    git status --short -- <active paths>

Audit all new or extracted files:

    wc -c <files>
    wc -l <files>

No new file may exceed 20 KiB or approximately 600 lines.

## Focused test groups

Create focused selectors with stable names. Exact Gradle tasks may differ after
inspection, but prefer commands such as:

    cd TotalCrossSDK
    ./gradlew-agent test --tests '*LogicalLayout*'
    ./gradlew-agent test --tests '*GraphicsScale*'
    ./gradlew-agent test --tests '*LogicalFontMetrics*'
    ./gradlew-agent test --tests '*ImageScale*'
    ./gradlew-agent test --tests '*ImagePixelSync*'
    ./gradlew-agent test --tests '*ImageTextRenderingDanfe*'

Required logical-layout cases:

- DP root default;
- inherited and overridden container units;
- parent owns child placement units;
- root pixel opt-out;
- semantic constants and offsets;
- `PREFERRED`;
- fractional shared-edge rounding;
- event and hit-test conversion exactly once.

Required scale cases:

- `contentScale` `1`, `1.5`, `2`, and `3`;
- `fontScale` default and changed;
- invalid scale rejection;
- simultaneous surfaces with different scales;
- transform reset retains base scale;
- clips, strokes, images, and text share one transform.

Required image cases are listed in the image design. Required text cases are
listed in the text design.

## Renderer equivalence

Run the same semantic fixtures against Skia and Java before touching the non-Skia
native path. After the native path is implemented, run it against the same
fixtures.

Equivalent means:

- equal integer compatibility metrics;
- equal logical component preferred bounds;
- equal line count and approved wrapping points;
- equal baseline ordering;
- equal logical image dimensions;
- equal physical image dimensions for a given scale;
- equal containment and barcode assertions;
- double metrics within an explicitly recorded tolerance.

Pixel-for-pixel identity is required only when comparing the same renderer, font
files, and image scale while changing unrelated display scale. It is not required
between Skia and Java antialiasing.

## SDK and native builds

Use the SDK wrapper:

    cd TotalCrossSDK
    ./gradlew-agent dist -x test

For verbose output:

    ./gradlew-agent dist -x test --warning-mode=none --console=plain \
      > ../artifacts/logical-ui-scaling/logs/sdk-dist.log 2>&1
    status=$?
    tail -80 ../artifacts/logical-ui-scaling/logs/sdk-dist.log
    exit $status

Use the smallest native target first. At final closure:

    cmake -S TotalCrossVM -B build -DCMAKE_BUILD_TYPE=Release -G Ninja
    ninja -C build

Run a clean build only when a stale object or generated binding is demonstrated.

## macOS validation

Run on a real Retina Mac where possible. Record:

    OS version
    Java runtime
    renderer
    display identity in sanitized form
    logical window size
    physical framebuffer size
    contentScale
    fontScale
    tested commit

Prove scale detection after the window peer exists. Where two monitors are
available, move the window between scale values and show:

- logical control bounds unchanged;
- physical framebuffer updated;
- glyph/raster caches invalidated;
- no stale or double-scaled frame;
- line wrapping unchanged.

The screenshot procedure is in the privacy guide.

## Android validation

Issue #433 explicitly names Android, so macOS proof is not enough. Use at least one
real device or emulator with density greater than `1`.

Build using the current repository flow:

    cd TotalCrossVM/android
    ./gradlew :tcvm:fetchNativeDependencies
    ./gradlew :app:assembleStandardRelease

Use the repository Android smoke-test guide if present. Avoid verbose deploy
flags. Record device model or emulator profile, Android version, density,
renderer, commit, and output image hash.

Run the DANFE at two effective screen densities when practical. The default image
export must remain the same physical size and, within the same renderer and font
set, byte-identical.

## Image synchronization validation

Synchronization is a release blocker. Test:

- Java edits followed by native drawing;
- native drawing followed by Java pixel reads;
- export after each direction;
- repeated synchronization with no changes;
- alternating ownership;
- alpha `0`, `128`, and `255`;
- odd widths and row pitches;
- failed copy does not clear dirty state;
- multiframe images.

A visual PNG alone is insufficient. Assert exact selected pixel values and buffer
dimensions.

## DANFE validation

The full fixture and assertions are in
`.agent/guides/logical-ui-scaling-danfe.md`. Required proof includes:

- exact default-image dimensions;
- logical versus physical scaled-image dimensions;
- text containment without over-shrinking;
- 31 barcode dark runs;
- display-density independence;
- Skia/Java semantic equivalence;
- macOS and Android results;
- sanitized application screenshot and generated PNG.

## Artifact integrity and privacy

Use `.agent/guides/private-screenshot-capture.md`. Before accepting artifacts:

- verify the file exists and decodes;
- record dimensions;
- inspect the image visually;
- crop unrelated desktop content;
- scan logs and text metadata for usernames, private window titles, local paths,
  tokens, authenticated URLs, and unrelated application names;
- compute a hash only after validation;
- omit failed or empty captures from evidence.

Do not publish raw screen recordings or screenshots containing other applications.

## Final acceptance sequence

At final closure, run in this order:

1. focused logical layout, font, image, synchronization, and DANFE tests;
2. Skia and Java equivalence suite;
3. non-Skia native equivalence suite;
4. SDK distribution build;
5. native build;
6. macOS Retina smoke and screenshot;
7. Android smoke and exported DANFE;
8. privacy and artifact integrity scan;
9. scoped diff, file-size, deprecation, and remaining-global-density audits.

Stop and record a blocker if Android proof, synchronization, source identity, or
privacy validation is incomplete. Do not describe issue #433 as closed-ready
without all four.
