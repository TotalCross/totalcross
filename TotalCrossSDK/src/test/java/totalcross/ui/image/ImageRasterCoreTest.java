// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import totalcross.sys.Settings;

class ImageRasterCoreTest {
  @Test
  void opaqueSetRgbWritesTheVisibleRasterAndAdvancesGenerationOnce() throws Exception {
    boolean previousJavaSe = Settings.onJavaSE;
    try {
      Settings.onJavaSE = true;
      Image image = new Image(2, 2);
      image.backing.setOpacityState(ImageBacking.OPACITY_HAS_ALPHA);
      long generation = image.backingMutationGenerationForP2();
      int[] input = {0x00112233, 0xFF010203, 0xFF112233, 0xFF405060, 0xFF708090};

      assertEquals(4, image.getGraphics().setRGB(input, 1, 0, 0, 2, 2));

      int[] pixels = new int[4];
      assertTrue(image.backing.readPixels(pixels, 0, 0, 0, 2, 2));
      assertArrayEquals(new int[] {0xFF010203, 0xFF112233, 0xFF405060, 0xFF708090}, pixels);
      assertEquals(generation + 1, image.backingMutationGenerationForP2());
      assertEquals(ImageBacking.OPACITY_OPAQUE, image.opacityStateForP2());
    } finally {
      Settings.onJavaSE = previousJavaSe;
    }
  }

  @Test
  void alphaSetRgbFallsBackWithArgbAndClippingSemantics() throws Exception {
    boolean previousJavaSe = Settings.onJavaSE;
    try {
      Settings.onJavaSE = true;
      Image image = new Image(3, 1);
      int[] pixels = image.getPixels();
      pixels[0] = 0xFF000001;
      pixels[1] = 0xFF000002;
      pixels[2] = 0xFF000003;
      image.getGraphics().setClip(1, 0, 2, 1);
      long generation = image.backingMutationGenerationForP2();

      assertEquals(2, image.getGraphics().setRGB(new int[] {0x80112233, 0x7F445566}, 0, 1, 0, 2, 1));

      assertArrayEquals(new int[] {0xFF000001, 0x80112233, 0x7F445566}, image.getPixels());
      assertTrue(image.backingMutationGenerationForP2() > generation);
      assertEquals(ImageBacking.OPACITY_UNKNOWN, image.opacityStateForP2());
    } finally {
      Settings.onJavaSE = previousJavaSe;
    }
  }

  @Test
  void boundedReadAndRgbaRowPreserveBytesWithoutMutatingBacking() throws Exception {
    Image image = new Image(2, 2);
    int[] pixels = image.getPixels();
    pixels[0] = 0xFF123456;
    pixels[1] = 0x80123456;
    pixels[2] = 0x00010203;
    pixels[3] = 0x7FABCDEF;
    long generation = image.backingMutationGenerationForP2();
    int[] rectangle = new int[4];
    byte[] rgba = new byte[8];

    assertTrue(image.backing.readPixels(rectangle, 0, 0, 0, 2, 2));
    image.getPixelRow(rgba, 0);

    assertArrayEquals(pixels, rectangle);
    assertArrayEquals(new byte[] {0x12, 0x34, 0x56, (byte) 0xFF,
        0x12, 0x34, 0x56, (byte) 0x80}, rgba);
    assertEquals(generation, image.backingMutationGenerationForP2());
    assertThrows(IllegalStateException.class, () -> image.getPixelRow(new byte[7], 0));
  }

  @Test
  void checkedRasterWritesRejectOverflowAndOutOfBoundsWithoutPartialMutation() {
    int[] pixels = {1, 2, 3, 4};
    RasterImageBacking backing = new RasterImageBacking(2, 2, 1, 2, pixels, null);

    assertFalse(backing.writePixels(new int[4], 0, 0, 0, Integer.MAX_VALUE, 2, 2, 2));
    assertFalse(backing.writePixels(new int[] {9}, 0, 1, 1, 2, 1, 2, 2));
    assertArrayEquals(new int[] {1, 2, 3, 4}, pixels);
  }

  @Test
  void applyColor2MatchesEagerJavaForAlphaAndAllFrameStorage() throws Exception {
    boolean previousJavaSe = Settings.onJavaSE;
    try {
      Settings.onJavaSE = true;
      int color = 0xAA6080A0;
      Image single = new Image(3, 1);
      int[] singlePixels = single.getPixels();
      singlePixels[0] = 0xFF804020;
      singlePixels[1] = 0x80102030;
      singlePixels[2] = 0x00010203;
      int[] singleExpected = singlePixels.clone();
      eagerApplyColor2(singleExpected, color);
      long singleGeneration = single.backingMutationGenerationForP2();

      single.applyColor2(color);

      assertArrayEquals(singleExpected, single.getPixels());
      assertTrue(single.backingMutationGenerationForP2() > singleGeneration);
      assertEquals(ImageBacking.OPACITY_UNKNOWN, single.opacityStateForP2());

      Image multi = new Image(4, 1);
      multi.setFrameCount(2);
      RasterImageBacking multiBacking = (RasterImageBacking) multi.backing;
      int[] allFrames = multiBacking.pixelsOfAllFrames();
      allFrames[0] = 0xFF804020;
      allFrames[1] = 0x80102030;
      allFrames[2] = 0xFF204060;
      allFrames[3] = 0x00010203;
      System.arraycopy(allFrames, 0, multiBacking.pixels(), 0, 2);
      int[] multiExpected = allFrames.clone();
      eagerApplyColor2(multiExpected, color);

      multi.applyColor2(color);

      assertArrayEquals(multiExpected, multiBacking.pixelsOfAllFrames());
      assertArrayEquals(new int[] {multiExpected[0], multiExpected[1]}, multi.getPixels());
      assertEquals(0, multi.getCurrentFrame());
      assertEquals(ImageBacking.OPACITY_UNKNOWN, multi.opacityStateForP2());
    } finally {
      Settings.onJavaSE = previousJavaSe;
    }
  }

  @Test
  void drawPlanClassificationKeepsOnlyColorFiltersInTheTrivialSubset() throws Exception {
    boolean previousJavaSe = Settings.onJavaSE;
    try {
      Settings.onJavaSE = true;
      Image root = new Image(2, 1);
      Image colorOnly = root.getAlphaInstance(128);
      colorOnly.applyColor(0x406080);
      Image geometry = root.getScaledInstance(1, 1);
      Image unsupported = root.getAlphaInstance(128);
      unsupported.applyColor2(0xFF406080);

      assertTrue(colorOnly.pipelineForSmoke().isTrivialDrawPlan());
      assertTrue(colorOnly.pipelineForSmoke().isDrawFusable());
      assertFalse(geometry.pipelineForSmoke().isTrivialDrawPlan());
      assertTrue(geometry.pipelineForSmoke().isDrawFusable());
      assertFalse(unsupported.pipelineForSmoke().isTrivialDrawPlan());
      assertFalse(unsupported.pipelineForSmoke().isDrawFusable());

      Settings.onJavaSE = false;
      assertNull(unsupported.drawPlanForDrawing(1));
    } finally {
      Settings.onJavaSE = previousJavaSe;
    }
  }

  private static void eagerApplyColor2(int[] pixels, int color) {
    int targetRed = (color >> 16) & 0xFF;
    int targetGreen = (color >> 8) & 0xFF;
    int targetBlue = color & 0xFF;
    boolean changeAlpha = (color & 0xFF000000) == 0xAA000000;
    int highest = 0;
    int highestPixel = 0;
    for (int pixel : pixels) {
      if ((pixel & 0xFF000000) == 0xFF000000) {
        int rgb = pixel & 0x00FFFFFF;
        int brightness = totalcross.ui.gfx.Color.getBrightness(rgb);
        if (brightness > highest) {
          highest = brightness;
          highestPixel = rgb;
        }
      }
    }
    int highRed = (highestPixel >> 16) & 0xFF;
    int highGreen = (highestPixel >> 8) & 0xFF;
    int highBlue = highestPixel & 0xFF;
    if (highRed == 0) highRed = 255;
    if (highGreen == 0) highGreen = 255;
    if (highBlue == 0) highBlue = 255;
    int highChannel = Math.max(highRed, Math.max(highGreen, highBlue));
    for (int index = 0; index < pixels.length; index++) {
      int pixel = pixels[index];
      if ((pixel & 0xFF000000) == 0) continue;
      int red = Math.min(255, ((pixel >> 16) & 0xFF) * targetRed / highRed);
      int green = Math.min(255, ((pixel >> 8) & 0xFF) * targetGreen / highGreen);
      int blue = Math.min(255, (pixel & 0xFF) * targetBlue / highBlue);
      if (changeAlpha) {
        int alpha = Math.max((pixel >> 16) & 0xFF, Math.max((pixel >> 8) & 0xFF, pixel & 0xFF));
        alpha = Math.min(255, alpha * 255 / highChannel);
        pixels[index] = (alpha << 24) | (red << 16) | (green << 8) | blue;
      } else {
        pixels[index] = (pixel & 0xFF000000) | (red << 16) | (green << 8) | blue;
      }
    }
  }
}
