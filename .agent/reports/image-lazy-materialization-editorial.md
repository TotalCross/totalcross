# Lazy Image materialization handoff

Plan 2 is complete at functional HEAD `6d9308f4fa0aced98746df4f64ae86774d190490` on
`feat/image-lazy-decode`; Plan 3 has not started.

Encoded sources are captured eagerly while raster decoding remains deferred
until a canonical pixel/export/mutation/frame/equality/hash/transform/native
drawing barrier. Failed attempts preserve the source and canonical adoption
remains atomic. Explicit JPEG scaling APIs remain eager, and transforms remain
eager after source materialization.

Deterministic corrupt/truncated payloads are classified and cached as
`ImageException`. JavaSE decoded-raster and encoded multi-frame frame-buffer
allocation failures use the internal transient category and are retried without
discarding the deferred source. Native PNG/JPEG loaders return an explicit
resource-vs-corruption status; native resource failures use a package-private
transient marker and are not cached. The macOS smoke injects a JPEG raster
allocation failure, verifies the first attempt fails, and verifies the same
instance succeeds on retry. JavaSE regression coverage injects the frame-buffer
failure during `init`/`setFrameCount`, then verifies frame metadata and pixels
after retry.

The exact focused SDK tests, production `dist -x test`, fresh macOS Release
TCVM build, lazy/Image ABI/EncodedImageSource smokes, header validation, and
whitespace validation all passed at the final HEAD. The public `setFrameCount`
signature and Image field ABI are unchanged. Full platform and packaging
validation remains intentionally deferred by the plan.
