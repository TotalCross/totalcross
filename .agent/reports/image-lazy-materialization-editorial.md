# Lazy Image materialization handoff

Plan 2 is complete at functional HEAD `4628c98223b11b3ab607ee038b7d28ad67aee39d` on
`feat/image-lazy-decode`.

The SDK now captures encoded sources eagerly and defers raster decoding until a
pixel, export, mutation, frame, equality/hash, transform, or native drawing
barrier. Canonical adoption preserves the original `Image` identity and keeps
deferred decode failures deterministic. JavaSE decodes from captured bytes;
deployed PNG decoding reads the native bag directly, with private native bridge
methods protecting the existing public ABI.

The final fixes preserve nonfatal libpng warnings while rejecting incomplete
pixel payloads, and place `createJpg(Stream,int)` behind the same canonical
materialization barrier on deployed targets. Focused SDK tests, production
`dist -x test`, a macOS Release TCVM build, the lazy-materialization smoke, the
existing Image ABI smoke, and the encoded-source regression smoke all passed
against the functional HEAD. Header and whitespace validation also passed.
Full platform and packaging validation is deferred per the plan.

Next: `execute .agent/plans/03-image-deferred-transforms-execplan.md`.
