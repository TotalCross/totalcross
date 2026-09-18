<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# macOS image diagnostics benchmark summary

## Run identity and artifacts

- Benchmark revision: `a43535e48`.
- Closeout artifact commit: `fe54b5e21`.
- Target: macOS ARM64; no other platform was built.
- Corpus: `/Users/flsobral/Downloads/win32/win32`, 3,978 JPEG files.
- Packager dataset hash: `af39fea695191a27`.
- Corpus manifest SHA-256 (sorted relative path + NUL + file bytes):
  `77dd1e0560f33677f116bcd9aff75dbbcaccb143c4e304b207dd27d45c566f07`.
- Raw results directory:
  `/private/tmp/image-scroll-diagnostics-plan2-a43535e/bundle/image-scroll-benchmark-macos-arm64/results`
- Combined results ZIP:
  `/private/tmp/image-scroll-diagnostics-plan2-a43535e/bundle/image-scroll-benchmark-macos-arm64/results/totalcross-image-benchmark-results-1789775641349415000.zip`
- Combined ZIP SHA-256:
  `2eb68a53125b25cec27cd46fdcb1b961ec59c80a99139228a2f4df57dbd8ae45`.
- SDK ZIP SHA-256:
  `d9055df2dfad516d1fce114dcd370405c68b2d6706f2ac97da73d4d4e4f01fc7`.
- `totalcross-sdk.jar` SHA-256:
  `0eb93da0a557010e992b00e611533f1f1ac19a580c128eb7fc1af0736a98c63a`.
- `libtcvm.dylib` SHA-256:
  `caec5b46d1b58ce65cbe7e43eeb8e25e13fe6a493df7f81b49f1fb2d81b649b2`.
- `Launcher` SHA-256:
  `ef6f924f3beda71e6615badf94dd93d5dd167a332250aa22e6dc3855cbcf384a`.

## Suite result

- Scroll matrix: 126/126 PASS rows; 126 unique `(run,mask,prefetch)` keys.
- Decode matrix: 90/90 processes; 59,670 detailed image rows; 0 failed
  image rows.
- Every run passed attempts/hits/fallback, candidate, JPEG bucket, and frame
  delta invariants.
- Final full-run duration: 2,242 seconds.

## Mask 4 writePixels diagnostics

The status was `ATTEMPTED_NO_HIT` in every run. Values are listed per run as
`attempts/hits/fallbacks; candidates; known-opaque candidates`.

- Prefetch off: r1 `223/0/223; 201; 0`, r2 `240/0/240; 216; 0`,
  r3 `240/0/240; 216; 0`; medians `240/0/240; 216; 0`.
- Prefetch on: r1 `3411/0/3411; 3411; 0`, r2 `3405/0/3405; 3405; 0`,
  r3 `3408/0/3408; 3408; 0`; medians `3408/0/3408; 3408; 0`.

Overlapping rejection counters were:

- off r1: matrix 223, saveCount 223, sourceRect 57, sizeMismatch 201;
  all other reasons 0.
- off r2: matrix 240, saveCount 240, sourceRect 66, sizeMismatch 216;
  all other reasons 0.
- off r3: matrix 240, saveCount 240, sourceRect 63, sizeMismatch 216;
  all other reasons 0.
- on r1: matrix 3411, saveCount 3411, sourceRect 1107, sizeMismatch 3411;
  all other reasons 0.
- on r2: matrix 3405, saveCount 3405, sourceRect 1107, sizeMismatch 3405;
  all other reasons 0.
- on r3: matrix 3408, saveCount 3408, sourceRect 1113, sizeMismatch 3408;
  all other reasons 0.

The global writePixels counters may include direct image/physical copies under
composite masks. Detailed rejection/candidate counters cover regular
`tryWritePixels*`; mask 4 is authoritative for `OPAQUE_WRITE_PIXELS`.

## JPEG and frame metrics for selected cells

Each line gives `run: count/ns, full/half/quarter/eighth/other buckets,
JPEG-ns/frame-ns percentage, frame P50/P95 ns`. For prefetch-on, count/ns are
the prefetch section; scroll count/ns are zero after the accounting reset.

- Mask 0 off: r1 `157/363673584,0/157/0/0/0,10.7885%,372720917/382686291`;
  r2 `160/363211290,0/160/0/0/0,10.8009%,374099541/381086417`;
  r3 `161/368914448,0/161/0/0/0,12.1046%,373306250/385268375`;
  medians `160/363673584,0/160/0/0/0,10.8009%,373306250/382686291`.
- Mask 0 on: r1 `660/1490963257,5/655/0/0/0,0%,16061916/16898334`;
  r2 `660/1483010241,5/655/0/0/0,0%,16038334/17002541`;
  r3 `660/1585408470,5/655/0/0/0,0%,16111958/16742625`;
  medians `660/1490963257,5/655/0/0/0,0%,16061916/16898334`.
- Mask 4 off: r1 `161/374661626,1/160/0/0/0,12.2476%,375141292/387711334`;
  r2 `157/364492542,0/157/0/0/0,10.7678%,377651750/384250291`;
  r3 `161/371229458,0/161/0/0/0,11.068%,371647458/378644000`;
  medians `161/371229458,0/160/0/0/0,11.068%,375141292/384250291`.
- Mask 4 on: r1 `660/1473690413,5/655/0/0/0,0%,16095917/16677459`;
  r2 `660/1495922956,5/655/0/0/0,0%,16078917/17037084`;
  r3 `660/1744538294,5/655/0/0/0,0%,16092000/16871959`;
  medians `660/1495922956,5/655/0/0/0,0%,16092000/16871959`.

Half resolution was dominant in observed JPEG work: scroll-off selected cells
were almost entirely half, and prefetch-on selected cells were 655 half plus
5 full. No quarter, eighth, or other bucket was observed. The measured
writePixels rejection distribution is dominated by matrix and saveCount, with
sizeMismatch also present on every attempt in the prefetch-on mask-4 cells;
these counters overlap and are not an optimization recommendation.

No optimization policy was changed. This summary is evidence for a later
policy decision, not a policy decision itself.
