<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image4D elimination evidence

- 2026-08-27 — revision `df4903620873ee40ba5227dbe045160bcaa71386`; initial reconnaissance recorded in `.agent/state/eliminate-image4d.md`; no validation run yet.
- 2026-08-27 — revision `df4903620873ee40ba5227dbe045160bcaa71386`; `./gradlew-agent test --tests 'totalcross.ui.image.*' --no-daemon --console=plain`; passed; log `artifacts/eliminate-image4d/logs/baseline-image-tests.log`.
- 2026-08-27 — checkpoint 1 working tree; `./gradlew-agent test --tests 'tc.tools.converter.ImageFieldAbiTest' --no-daemon --console=plain`; passed; log `artifacts/eliminate-image4d/logs/image-abi-test-m1.log`; guard covers native I32/object/value64 field prefixes and exposed the pre-existing `Image4D.hashCode` index shift.
