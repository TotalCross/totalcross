# Lazy Image materialization state

- Active milestone/slice: Plan 2 complete; handoff to Plan 3.
- Current functional tip: `6d9308f4f` (`fix(sdk): retry multi-frame materialization buffers`).
- Logical commits: `bb034835b` added Java lazy materialization; `57df779ed`
  added native barriers and PNG bag decoding; `72e539c44` added macOS smoke
  coverage and the initial completion records; `f7d5e72dc` restored
  nonfatal libpng warning behavior; `2f0065d9c` wrapped JPEG export behind
  materialization; `173879cb3` rejected incomplete PNG payloads; and
  `4628c9822` finalized warning, JPEG, and corrupt-payload smoke coverage;
  `303b246d0` kept explicit JPEG scaling eager; `380231224` made transient
  materialization failures retryable; and `d1dee95d2` moved the JavaSE
  allocation failure injection into decoded-raster construction; and
  `87d6f1e36` classified native PNG/JPEG decode resource failures explicitly;
  and `16eb1101a` added the macOS same-instance native allocation retry smoke;
  and `af62bb123` completed explicit retryable native status propagation;
  `6d9308f4f` kept multi-frame frame-buffer allocation failures retryable during
  encoded materialization.
- Branch note: the plan names `refactor/lazy-image-pipeline`; the checked-out branch is `feat/image-lazy-decode`, so execution continued after confirming Plan 1 state and matching HEAD.
- Delivered: encoded Image constructors now eagerly capture immutable sources and expose structural metadata while deferring pixels; canonical checked/unchecked barriers atomically adopt decoded pixels; deterministic payload failures are cached; writable/raster constructors remain eager; transformations remain eager after source materialization.
- Native protection: Image and Graphics pixel consumers use Java barriers plus private native bridges; PNG memory decode reads directly from `ImageEncodedBag`; ordinary libpng warnings remain nonfatal while incomplete pixel payloads are rejected before adoption; Image object field index 8 is the appended pipeline field and existing ABI prefixes remain unchanged.
- Smoke coverage: added `ImageLazyMaterializationSmokeApp` and Gradle tasks; it verifies byte-source ownership, metadata-before-decode, first draw materialization, repeated barrier reuse, TCZ path capture, JPEG export as a materialization barrier, structural constructor rejection, deferred corrupt-payload failure, and same-instance native allocation retry. JavaSE regression coverage now also verifies retryable multi-frame frame-buffer allocation. Existing Image ABI and encoded-source smokes also pass.
- Deferred validation: Android, iOS, Linux, Windows, packaging, and full platform matrix builds remain intentionally skipped by the plan’s validation boundary.
- Validation evidence: final exact-HEAD records for `6d9308f4f` are appended to
  `.agent/evidence/image-lazy-materialization.jsonl`; the fresh macOS dylib is
  `build-image-lazy-materialization/libtcvm.dylib` with SHA-256
  `3aeb44197c9b510a1e5a152505e275ebbdb0fd635deb33d20d44a94f4c1ddff8`.
- Deferred validation: Android, iOS, Linux, Windows, packaging, and full
  platform matrix builds remain intentionally skipped by the plan boundary.
- Blocker: none.
- Next command: `execute .agent/plans/03-image-deferred-transforms-execplan.md`
