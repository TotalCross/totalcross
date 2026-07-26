// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.preview;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PreviewFrameTest {
  @Test
  void ownsPixelsAndReportsRuntimeNeutralMetadata() {
    int[] source = { 1, 2, 3, 4, 5, 6 };
    PreviewFrame frame = new PreviewFrame(2, 2, 3, 2, PreviewFrame.PixelFormat.ARGB_8888, source);
    source[0] = 99;
    int[] copy = frame.copyPixels();
    copy[1] = 88;

    assertEquals(2, frame.getWidth());
    assertEquals(2, frame.getHeight());
    assertEquals(3, frame.getStride());
    assertEquals(2, frame.getDensity());
    assertArrayEquals(new int[] { 1, 2, 3, 4, 5, 6 }, frame.copyPixels());
  }
}
