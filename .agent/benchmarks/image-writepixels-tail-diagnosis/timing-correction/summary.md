<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Direct writePixels timing correction

This correction uses only fresh macOS ARM64 rows from the focused primary pair
`32795 -> 32799`. The prior historical compact CSVs remain unchanged because
their raw diagnostic roots are not present in this checkout.

## Coverage and identity

- 663 JPEG-named corpus files; corpus hash `588a7e0f4019424a`.
- Primary pair: `32795` disabled and `32799` enabled.
- Accounting-on cold: 8 processes, two rounds, prefetch OFF/ON.
- Accounting-on reuse: 4 processes, three passes, prefetch OFF/ON.
- Accounting-off control: 4 cold processes, prefetch OFF/ON.
- Result root:
  `build/writepixels-tail-package-m2-v3/image-scroll-benchmark-macos-arm64/results`
- Direct cold archive SHA-256:
  `079db467e348d00c2c9b8d90bbd7a805c2880e5af494e512c1cb2bf46014ee50`
- Direct reuse archive SHA-256:
  `bc84e1b47c2c40cb5cb8f798916060b9a80d0be3810f8fc2a852c58ed2811b40`
- Accounting-off archive SHA-256:
  `24066839abacbedbe55af66d1239f58f56ed63b544d40bc6613a77ab0abad051`
- SDK ZIP SHA-256:
  `a809110df548b40d558cb89b87c0ccb670ed75facfb08abc9f0d132ffc0db721`
- SDK JAR SHA-256:
  `17f364b80ead5afe3e362f2b4f3ea212a9c2e298acf260932c00b557b4f83f6e`
- Runtime SHA-256:
  `1f161b5741a45b8f498a5b403d78d6cf35c58bf1a48531d128b879e37f4da743`

The analyzer selected 30 fresh P95/P99/MAX rows: 12 cold and 18 reuse. All
selected enabled rows had writePixels hits. Full-copy dimensions were bounded
at 358x144 (7,732,800 bytes); the observed hit format was enum `0`, RGBA8888.
RGB565 conversion timing was exposed and validated but was zero in this
32-bit primary pair.

## Work attribution

Values below are representative MAX rows from the fresh enabled cells. The
preparation and copy fractions are direct fractions of `work_time_ns`.

| behavior | work ns | writePixels total ns | preparation ns | actual copy ns | decode ns | remaining paint ns | prep/work | copy/work |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| cold, prefetch off | 373,441,250 | 1,064,040 | 25,832 | 1,037,210 | 39,234,169 | 372,328,877 | 0.007% | 0.278% |
| cold, prefetch on | 39,112,500 | 513,917 | 5,627 | 507,500 | 0 | 38,528,000 | 0.014% | 1.298% |
| reuse, prefetch off | 380,697,625 | 1,155,623 | 26,789 | 1,127,835 | 38,899,915 | 379,493,419 | 0.007% | 0.296% |
| reuse, prefetch on | 30,140,875 | 299,541 | 4,248 | 294,335 | 0 | 29,780,084 | 0.014% | 0.977% |

The highest copy fraction among selected rows was 11.8%, but that row was a
4.6 ms prefetch-on MAX; the 39.1 ms and 30.1 ms tails above were under 1.3%
actual copy. The preparation fraction stayed at or below 0.164% in selected
rows. Decode time is a timed subcomponent of the scroll/paint work, while
materialization is currently a counted activity rather than a separate timer;
the displayed decode and remaining-paint fractions are therefore attribution
context, not additive independent buckets.

The paired CSV records enabled-minus-disabled P50/P95/P99/MAX deltas and raw
frame paths. Accounting-off rows validated all new timing fields as `-1`, so
the control confirms availability gating rather than supplying attribution.

## Correction outcome

Outcome 2 is now supported for the fresh primary pair: direct timing shows
that writePixels itself is not responsible for the expensive tail. The copy
and preparation portions are small in the largest cold and reuse tails; the
remaining paint work dominates, with JPEG decode/materialization activity
present in the prefetch-off tails. This does not authorize policy, default,
GPU, image-format, dirty-region, or scroll-reuse changes.
