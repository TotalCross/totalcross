<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image4D elimination execution state

- Active milestone: complete.
- Starting revision: `df4903620873ee40ba5227dbe045160bcaa71386`.
- Last commit: `c8f5d5f99` — checkpoint 5, native macOS unified Image ABI smoke.
- Checkpoint 4: `Image4D.java` deleted; stale native/test references were removed; `ImageLoader4D` isolates the JavaSE AWT/ImageIO reader during deployment.
- Milestone 3 changes: unified `Image` now carries the deployed cleanup/cache state, private native-creation constructor, shared-copy state path, `lockChanges`, finalization, and null-pixel graphics behavior.
- Active paths: `TotalCrossSDK/src/main/java/totalcross/ui/image/Image.java`, `TotalCrossSDK/src/main/java/tc/tools/converter/J2TC.java`, `TotalCrossSDK/src/main/java/tc/tools/converter/tclass/TCClass.java`, `TotalCrossSDK/src/main/java/tc/tools/converter/tclass/TCField.java`, `TotalCrossSDK/src/test/java/tc/tools/converter/`, `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageAbiSmokeApp.java`, `TotalCrossSDK/build.gradle`, `TotalCrossVM/src/nm/instancefields.h`, `TotalCrossVM/src/nm/ui/image_Image.c`, `TotalCrossVM/src/nm/ui/ImagePrimitives_c.h`.
- Reconnaissance: `Image.java` is the JavaSE implementation and currently declares 12 I32, 6 object, and 3 value64 instance fields after conversion grouping only when deployed through the `Image4D` replacement; `Image4D.java` carries the ABI-sensitive `lastAccess`, `textureId`, `changed`, `instanceCount`, and `master` state. Native entry points already name `totalcross/ui/image/Image`. `Image.java` additionally reaches AWT/ImageIO and `totalcross.Launcher` in desktop-only code.
- Baseline focused validation: `TotalCrossSDK/./gradlew-agent test --tests 'totalcross.ui.image.*' --no-daemon --console=plain` passed; log: `artifacts/eliminate-image4d/logs/baseline-image-tests.log`.
- Checkpoint 1 focused validation: `TotalCrossSDK/./gradlew-agent test --tests 'tc.tools.converter.ImageFieldAbiTest' --no-daemon --console=plain` passed after moving `Image4D.hashCode` below the ABI-sensitive I32 fields; log: `artifacts/eliminate-image4d/logs/image-abi-test-m1.log`. `git diff --check` and scoped copyright validation passed.
- Checkpoint 2 focused validation: direct `Image.class` conversion, annotated native replacement checks, `tc.tools.converter.*Image*`, `totalcross.ui.image.*`, and `./gradlew-agent dist -x test --no-daemon --console=plain` passed; logs: `artifacts/eliminate-image4d/logs/direct-image-conversion-m2.log`, `artifacts/eliminate-image4d/logs/m2-focused-tests.log`, and `artifacts/eliminate-image4d/logs/m2-sdk-dist.log`.
- Checkpoint 3 focused validation: `./gradlew-agent test --tests 'tc.tools.converter.ImageFieldAbiTest' --tests 'totalcross.ui.image.*' --no-daemon --console=plain` and `./gradlew-agent dist -x test --no-daemon --console=plain` passed; logs: `artifacts/eliminate-image4d/logs/m3-state-tests.log` and `artifacts/eliminate-image4d/logs/m3-sdk-dist.log`.
- Checkpoint 4 focused validation: `Image4D.java` is absent; direct/field/converter/image tests passed; SDK `dist -x test` passed after `ImageLoader4D` isolation; `git diff --check` passed. Logs: `artifacts/eliminate-image4d/logs/m4-focused-tests.log` and `artifacts/eliminate-image4d/logs/m4-sdk-dist.log`.
- Milestone 5 validation: `compileImageAbiSmoke` and `deployImageAbiSmokeMacOS` passed; fresh Release CMake/Ninja built `build-image-abi/libtcvm.dylib`; `runImageAbiSmokeMacOS` launched the deployed executable directly with matching source/deployed SHA-256 `f96c671727f3841aaa95034ee2c340da8c0fe70938d28e4f09dacad264a14aa0` and all assertions passed at revision `cad955305c605bad7cd4dac403333961e7b7f253`. Logs: `artifacts/eliminate-image4d/logs/compile-image-abi-smoke.log`, `artifacts/eliminate-image4d/logs/deploy-image-abi-smoke.log`, `artifacts/eliminate-image4d/logs/macos-cmake.log`, `artifacts/eliminate-image4d/logs/macos-tcvm.log`, `artifacts/eliminate-image4d/logs/macos-smoke-run.log`; full Gradle identity/assertion log: `TotalCrossSDK/agent-logs/20260827-033913-runImageAbiSmokeMacOS-full.log`.
- Final validation: focused converter/image tests, SDK `dist -x test`, `git diff --check`, changed-file copyright validation, and provenance audit all passed. Source reference search is clean; generic `4D` conversion remains. The native smoke need not be repeated after checkpoint 5 because no production `Image` code changed after the successful run.
- Blockers: none.
- Exact next action: none; execution complete at checkpoint `c8f5d5f99`.
- Resume command: `cd TotalCrossSDK && ./gradlew-agent test --tests 'tc.tools.converter.ImageFieldAbiTest' --no-daemon --console=plain`
