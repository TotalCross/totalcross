# Lazy Image materialization state

- Active milestone/slice: Plan 2 complete; handoff to Plan 3.
- Current functional tip: `4628c9822` (`test(sdk,macos): verify lazy image warning barriers`).
- Logical commits: `bb034835b` added Java lazy materialization; `57df779ed`
  added native barriers and PNG bag decoding; `72e539c44` added macOS smoke
  coverage and the initial completion records; `f7d5e72dc` restored
  nonfatal libpng warning behavior; `2f0065d9c` wrapped JPEG export behind
  materialization; `173879cb3` rejected incomplete PNG payloads; and
  `4628c9822` finalized warning, JPEG, and corrupt-payload smoke coverage.
- Branch note: the plan names `refactor/lazy-image-pipeline`; the checked-out branch is `feat/image-lazy-decode`, so execution continued after confirming Plan 1 state and matching HEAD.
- Delivered: encoded Image constructors now eagerly capture immutable sources and expose structural metadata while deferring pixels; canonical checked/unchecked barriers atomically adopt decoded pixels; deterministic payload failures are cached; writable/raster constructors remain eager; transformations remain eager after source materialization.
- Native protection: Image and Graphics pixel consumers use Java barriers plus private native bridges; PNG memory decode reads directly from `ImageEncodedBag`; ordinary libpng warnings remain nonfatal while incomplete pixel payloads are rejected before adoption; Image object field index 8 is the appended pipeline field and existing ABI prefixes remain unchanged.
- Smoke coverage: added `ImageLazyMaterializationSmokeApp` and Gradle tasks; it verifies byte-source ownership, metadata-before-decode, first draw materialization, repeated barrier reuse, TCZ path capture, JPEG export as a materialization barrier, structural constructor rejection, and deferred corrupt-payload failure. Existing Image ABI and encoded-source smokes also pass.
- Validation evidence: see `.agent/evidence/image-lazy-materialization.jsonl`. Final macOS dylib is `build-image-lazy-materialization/libtcvm.dylib` with SHA-256 `adb53b4365f2505e8a27870b2e131c8e69c0a2f3a58ed1633fb2dd320f37ba61`, validated against functional HEAD `4628c9822`.
- Deferred validation: Android, iOS, Linux, Windows, packaging, and full platform matrix builds remain intentionally skipped by the plan’s validation boundary.
- Blocker: none.
- Next command: `execute .agent/plans/03-image-deferred-transforms-execplan.md`
