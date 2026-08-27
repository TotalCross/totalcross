<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image4D elimination evidence

- 2026-08-27 — revision `df4903620873ee40ba5227dbe045160bcaa71386`; initial reconnaissance recorded in `.agent/state/eliminate-image4d.md`; no validation run yet.
- 2026-08-27 — revision `df4903620873ee40ba5227dbe045160bcaa71386`; `./gradlew-agent test --tests 'totalcross.ui.image.*' --no-daemon --console=plain`; passed; log `artifacts/eliminate-image4d/logs/baseline-image-tests.log`.
- 2026-08-27 — checkpoint 1 working tree; `./gradlew-agent test --tests 'tc.tools.converter.ImageFieldAbiTest' --no-daemon --console=plain`; passed; log `artifacts/eliminate-image4d/logs/image-abi-test-m1.log`; guard covers native I32/object/value64 field prefixes and exposed the pre-existing `Image4D.hashCode` index shift.
- 2026-08-27 — checkpoint 2 working tree; direct `Image.class` conversion and annotated-native checks passed; focused image/converter tests passed; SDK `dist -x test` passed; logs `artifacts/eliminate-image4d/logs/direct-image-conversion-m2.log`, `artifacts/eliminate-image4d/logs/m2-focused-tests.log`, `artifacts/eliminate-image4d/logs/m2-sdk-dist.log`.
- 2026-08-27 — checkpoint 3 working tree; deployed cleanup/cache state alignment passed the ABI and focused image tests; SDK `dist -x test` passed; logs `artifacts/eliminate-image4d/logs/m3-state-tests.log`, `artifacts/eliminate-image4d/logs/m3-sdk-dist.log`.
- 2026-08-27 — checkpoint 4 working tree; removed `Image4D.java`, updated the native test comment, isolated `Image$ImageLoader` with `ImageLoader4D`, direct/field/converter/image tests passed, and SDK `dist -x test` passed; logs `artifacts/eliminate-image4d/logs/m4-focused-tests.log`, `artifacts/eliminate-image4d/logs/m4-sdk-dist.log`.
- 2026-08-27 — revision `cad955305c605bad7cd4dac403333961e7b7f253`; native macOS lane: `cmake -S TotalCrossVM -B build-image-abi -DCMAKE_BUILD_TYPE=Release -G Ninja` and `ninja -C build-image-abi tcvm` passed; fixture compile/deploy passed; direct `runImageAbiSmokeMacOS -PtcvmDylib=/Users/flsobral/repos/totalcross-github/build-image-abi/libtcvm.dylib` passed with source/deployed dylib SHA-256 `f96c671727f3841aaa95034ee2c340da8c0fe70938d28e4f09dacad264a14aa0`, direct executable launch, `otool -L`/loader-symbol identity evidence, exit 0, and `overallPass=true`. Assertion line and identity are in `TotalCrossSDK/agent-logs/20260827-033913-runImageAbiSmokeMacOS-full.log`; compact command logs are under `artifacts/eliminate-image4d/logs/`.
