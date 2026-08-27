# Lazy Image materialization state

- Active milestone/slice: Plan 2 complete; handoff to Plan 3.
- Current HEAD: `c69451ec0612f81d622e27745254548c1be997ca`.
- Branch note: the plan names `refactor/lazy-image-pipeline`; the checked-out branch is `feat/image-lazy-decode`, so execution continued after confirming Plan 1 state and matching HEAD.
- Delivered: encoded Image constructors now eagerly capture immutable sources and expose structural metadata while deferring pixels; canonical checked/unchecked barriers atomically adopt decoded pixels; deterministic payload failures are cached; writable/raster constructors remain eager; transformations remain eager after source materialization.
- Native protection: Image and Graphics pixel consumers use Java barriers plus private native bridges; PNG memory decode reads directly from `ImageEncodedBag`; warning-marked PNG payload failures are rejected before adoption; Image object field index 8 is the appended pipeline field and existing ABI prefixes remain unchanged.
- Smoke coverage: added `ImageLazyMaterializationSmokeApp` and Gradle tasks; it verifies byte-source ownership, metadata-before-decode, first draw materialization, repeated barrier reuse, TCZ path capture, JPEG materialization, structural constructor rejection, and deferred payload failure. Existing Image ABI and encoded-source smokes also pass.
- Validation evidence: see `.agent/evidence/image-lazy-materialization.jsonl`. Final macOS dylib is `build-image-lazy-materialization/libtcvm.dylib` with SHA-256 `2a741ede864cd76fdff86b73b425e561a61a7322773a59ccfe206a1ab487b9b2`.
- Deferred validation: Android, iOS, Linux, Windows, packaging, and full platform matrix builds remain intentionally skipped by the plan’s validation boundary.
- Blocker: none.
- Next command: `execute .agent/plans/03-image-deferred-transforms-execplan.md`
