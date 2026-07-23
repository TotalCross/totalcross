<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Issue 417 generated-image smoke test

This test is a minimized copy of the flow in the attached `Tcsort.zip` from
issue 417. It keeps `MonoImage(576, 576)`, `getGraphics()`, a white full-image
fill, a black inset rectangle, and `createPng()` to `nome.png`. The unrelated
controls, viewer launch, and customer text were removed.

The application writes `nome.png` and `issue-417-result.json` below
`Settings.appPath`. The result records the platform, implementation path,
expected and observed pixels, a row read from the image, encoded size, and a
CRC32 of the encoded PNG. The checker validates the JSON, PNG signature,
dimensions, and presence of image data without Pillow or another dependency.

## Build and run

Compile the source with the SDK jars from the same checkout:

    mkdir -p /tmp/issue-417-classes
    javac -cp TotalCrossSDK/build/libs/totalcross-sdk-7.2.2.jar:TotalCrossSDK/build/libs/tcui-7.2.2.jar \
      -d /tmp/issue-417-classes tests/smoke/issue-417-generated-image/Tcsort.java

Deploy the class with the platform-specific TotalCross deployer. For example,
from the repository root, Android packaging uses:

    java -cp TotalCrossSDK/build/libs/totalcross-sdk-7.2.2.jar:TotalCrossSDK/build/libs/tcui-7.2.2.jar \
      tc.Deploy /tmp/issue-417-classes -android /o /tmp/issue-417-android

Run the resulting application on the target platform, then pull the two files
from its application path and validate them:

    python3 tests/smoke/issue-417-generated-image/check_result.py \
      /path/to/issue-417-result.json /path/to/nome.png

The checker exits zero only when all fixed geometry, row, and encoded-output
assertions pass. It can also validate a Java SE result; its
`implementationPath` must then be `java-byte-array`.

On Android, stop the application after the two files are pulled. The Android
loader's asynchronous VM teardown can race `MainWindow.exit`; the result JSON
and checker are the test boundary on that target.

The original archive is not needed after this source directory is committed.
