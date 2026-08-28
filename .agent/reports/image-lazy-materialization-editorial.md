# Lazy Image materialization handoff

Plan 2 is complete at functional HEAD `af62bb123fd3e40a10d762c07fc6cc0036ae8ca4` on
`feat/image-lazy-decode`; Plan 3 has not started.

Encoded sources are captured eagerly while raster decoding remains deferred
until a canonical pixel/export/mutation/frame/equality/hash/transform/native
drawing barrier. Failed attempts preserve the source and canonical adoption
remains atomic. Explicit JPEG scaling APIs remain eager, and transforms remain
eager after source materialization.

Deterministic corrupt/truncated payloads are classified and cached as
`ImageException`. JavaSE decoded-raster allocation failures use the internal
transient category and are retried. Native PNG/JPEG loaders return an explicit
resource-vs-corruption status; native resource failures use a package-private
transient marker and are not cached. The macOS smoke injects a JPEG raster
allocation failure, verifies the first attempt fails, and verifies the same
instance succeeds on retry.

The exact focused SDK tests, production `dist -x test`, fresh macOS Release
TCVM build, lazy/Image ABI/EncodedImageSource smokes, header validation, and
whitespace validation all passed at the final HEAD. Full platform and
packaging validation remains intentionally deferred by the plan.
