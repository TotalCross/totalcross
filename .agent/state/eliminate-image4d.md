<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image4D elimination execution state

- Active milestone: 2 — make `Image.java` independently deployable.
- Starting revision: `df4903620873ee40ba5227dbe045160bcaa71386`.
- Last commit: `73d4ae716` — checkpoint 1, ABI guard and `Image4D.hashCode` order fix.
- Active paths: `TotalCrossSDK/src/main/java/totalcross/ui/image/Image.java`, `TotalCrossSDK/src/main/java/totalcross/ui/image/Image4D.java`, `TotalCrossSDK/src/main/java/tc/tools/converter/J2TC.java`, `TotalCrossSDK/src/main/java/tc/tools/converter/tclass/TCClass.java`, `TotalCrossSDK/src/main/java/tc/tools/converter/tclass/TCField.java`, `TotalCrossSDK/src/test/java/tc/tools/converter/`, `TotalCrossVM/src/nm/instancefields.h`, `TotalCrossVM/src/nm/ui/image_Image.c`, `TotalCrossVM/src/nm/ui/ImagePrimitives_c.h`.
- Reconnaissance: `Image.java` is the JavaSE implementation and currently declares 12 I32, 6 object, and 3 value64 instance fields after conversion grouping only when deployed through the `Image4D` replacement; `Image4D.java` carries the ABI-sensitive `lastAccess`, `textureId`, `changed`, `instanceCount`, and `master` state. Native entry points already name `totalcross/ui/image/Image`. `Image.java` additionally reaches AWT/ImageIO and `totalcross.Launcher` in desktop-only code.
- Baseline focused validation: `TotalCrossSDK/./gradlew-agent test --tests 'totalcross.ui.image.*' --no-daemon --console=plain` passed; log: `artifacts/eliminate-image4d/logs/baseline-image-tests.log`.
- Checkpoint 1 focused validation: `TotalCrossSDK/./gradlew-agent test --tests 'tc.tools.converter.ImageFieldAbiTest' --no-daemon --console=plain` passed after moving `Image4D.hashCode` below the ABI-sensitive I32 fields; log: `artifacts/eliminate-image4d/logs/image-abi-test-m1.log`. `git diff --check` and scoped copyright validation passed.
- Checkpoint 2 focused validation: direct `Image.class` conversion, annotated native replacement checks, `tc.tools.converter.*Image*`, `totalcross.ui.image.*`, and `./gradlew-agent dist -x test --no-daemon --console=plain` passed; logs: `artifacts/eliminate-image4d/logs/direct-image-conversion-m2.log`, `artifacts/eliminate-image4d/logs/m2-focused-tests.log`, and `artifacts/eliminate-image4d/logs/m2-sdk-dist.log`.
- Deferred validation: direct conversion, SDK distribution, native macOS smoke, provenance audit, and final cleanup remain pending.
- Blockers: none.
- Exact next action: commit checkpoint 2, then port `Image4D` copy/share/finalization state into unified `Image` while preserving the ABI prefix and direct conversion test.
- Resume command: `cd TotalCrossSDK && ./gradlew-agent test --tests 'tc.tools.converter.ImageFieldAbiTest' --no-daemon --console=plain`
