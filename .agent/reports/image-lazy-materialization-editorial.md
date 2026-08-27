# Lazy Image materialization handoff

Plan 2 is complete at `c69451ec0612f81d622e27745254548c1be997ca` on
`feat/image-lazy-decode`.

The SDK now captures encoded sources eagerly and defers raster decoding until a
pixel, export, mutation, frame, equality/hash, transform, or native drawing
barrier. Canonical adoption preserves the original `Image` identity and keeps
deferred decode failures deterministic. JavaSE decodes from captured bytes;
deployed PNG decoding reads the native bag directly, with private native bridge
methods protecting the existing public ABI.

Focused SDK tests, production `dist -x test`, a fresh macOS Release TCVM build,
the new lazy-materialization smoke, the existing Image ABI smoke, and the
encoded-source regression smoke all passed. Header and whitespace validation
also passed. Full platform and packaging validation is deferred per the plan.

Next: `execute .agent/plans/03-image-deferred-transforms-execplan.md`.
