// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;

import org.junit.jupiter.api.Test;

class NativeImageDecodeRendererGuardTest {
  @Test
  void directNativeDecodeStateAndCallsAreInsideSkiaGuards() throws IOException {
    assertSkiaGuarded("../TotalCrossVM/third_party/jpeg/JpegLoader.h",
        "JPEG_DECODE_DIRECT_FULL");
    assertSkiaGuarded("../TotalCrossVM/third_party/jpeg/JpegLoader.c",
        "JPEG_DECODE_DIRECT_FULL", "nativeHandle", "rgbaRow", "skia_image_backing_");
    assertSkiaGuarded("../TotalCrossVM/third_party/png/PngLoader.c",
        "bool directDecode;", "userData.directDecode", "userData->directDecode",
        "nativeHandle", "rgbaRow", "skia_image_backing_");
  }

  private static void assertSkiaGuarded(String sourcePath, String... patterns) throws IOException {
    Path path = Path.of(sourcePath);
    Deque<Boolean> rendererBranches = new ArrayDeque<>();
    Deque<Boolean> rendererConditions = new ArrayDeque<>();
    var lines = Files.readAllLines(path);
    for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
      String line = lines.get(lineNumber);
      String directive = line.trim();
      if (directive.startsWith("#if ") || directive.startsWith("#ifdef ")
          || directive.startsWith("#ifndef ")) {
        boolean rendererGuard = directive.equals("#if TC_RENDERER_SKIA");
        rendererConditions.push(rendererGuard);
        rendererBranches.push(rendererGuard);
      } else if (directive.startsWith("#else") || directive.startsWith("#elif")) {
        if (!rendererConditions.isEmpty() && rendererConditions.peek()) {
          rendererBranches.pop();
          rendererBranches.push(false);
        }
      } else if (directive.startsWith("#endif")) {
        if (!rendererConditions.isEmpty()) {
          rendererConditions.pop();
          rendererBranches.pop();
        }
      }

      boolean directReference = false;
      for (String pattern : patterns) {
        if (line.contains(pattern)) {
          directReference = true;
          break;
        }
      }
      if (directReference) {
        assertTrue(rendererBranches.contains(Boolean.TRUE),
            path + ":" + (lineNumber + 1) + " uses direct native decode outside #if TC_RENDERER_SKIA: "
                + line.trim());
      }
    }
  }
}
