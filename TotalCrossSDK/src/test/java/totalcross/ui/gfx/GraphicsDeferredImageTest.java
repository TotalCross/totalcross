// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.gfx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.zip.CRC32;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import totalcross.ui.image.Image;

class GraphicsDeferredImageTest {
  @Test
  void drawsDeferredSourceAtEachDestinationContentScale() throws Exception {
    Image base = new Image(2, 2);
    java.util.Arrays.fill(base.getPixels(), 0xFFFF0000);
    Image source = base.getSmoothScaledInstance(1, 1);
    assertNull(backing(source));

    for (int scale = 1; scale <= 4; scale *= 2) {
      Image destination = Image.createLogical(1, 1, scale);
      destination.getGraphics().drawImage(source, 0, 0);
      assertAllPixels(destination, 0xFFFF0000);
      assertNull(backing(source));
    }

  }

  @Test
  void copyOperationsResolveDeferredImageSources() throws Exception {
    Image base = new Image(2, 2);
    java.util.Arrays.fill(base.getPixels(), 0xFF00FF00);
    Image source = base.getSmoothScaledInstance(1, 1);

    Image copiedRect = Image.createLogical(1, 1, 4);
    copiedRect.getGraphics().copyImageRect(source, 0, 0, 1, 1, true);
    assertAllPixels(copiedRect, 0xFF00FF00);

    Image copiedSurface = Image.createLogical(1, 1, 2);
    copiedSurface.getGraphics().copyRect(source, 0, 0, 1, 1, 0, 0);
    assertAllPixels(copiedSurface, 0xFF00FF00);
    assertNull(backing(source));
  }

  @Test
  void copyRectPreservesDeferredSourceRectangleDestinationTranslationAndClip() throws Exception {
    Image base = new Image(3, 2);
    int[] basePixels = base.getPixels();
    for (int i = 0; i < basePixels.length; i++) {
      basePixels[i] = 0xFF000000 | (i * 0x00112233);
    }

    Image expectedSource = base.getSmoothScaledInstance(6, 4);
    int[] expectedSourcePixels = expectedSource.getPixels().clone();
    Image deferredSource = base.getSmoothScaledInstance(6, 4);
    Image expected = filledImage(6, 5, 0xFF202020);
    Image actual = filledImage(6, 5, 0xFF202020);

    Graphics expectedGraphics = expected.getGraphics();
    expectedGraphics.translate(1, 1);
    expectedGraphics.setClip(1, 1, 3, 2);
    expectedGraphics.copyRect(expectedSource, 1, 0, 4, 4, 1, 0);

    Graphics actualGraphics = actual.getGraphics();
    actualGraphics.translate(1, 1);
    actualGraphics.setClip(1, 1, 3, 2);
    actualGraphics.copyRect(deferredSource, 1, 0, 4, 4, 1, 0);

    assertArrayEquals(expected.getPixels(), actual.getPixels());
    assertArrayEquals(expectedSourcePixels, expectedSource.getPixels());
    assertNull(backing(deferredSource));
  }

  @Test
  void copyRectWithEmptyVisibleIntersectionLeavesDestinationUnchanged() throws Exception {
    Image base = new Image(2, 2);
    java.util.Arrays.fill(base.getPixels(), 0xFFABCDEF);
    Image source = base.getSmoothScaledInstance(4, 4);
    Image destination = filledImage(3, 3, 0xFF123456);
    int[] before = destination.getPixels().clone();

    destination.getGraphics().copyRect(source, 0, 0, 2, 2, 8, 8);

    assertArrayEquals(before, destination.getPixels());
  }

  @Test
  void copyRectPreservesCurrentFrameForDeferredSources() throws Exception {
    byte[] encoded = twoFramePng();
    Image expectedSource = new Image(encoded);
    expectedSource.setCurrentFrame(1);
    Image deferredSource = new Image(encoded);
    deferredSource.setCurrentFrame(1);
    expectedSource.getPixels();

    Image expected = new Image(4, 2);
    expected.getGraphics().copyRect(expectedSource, 0, 0, 4, 2, 0, 0);
    Image actual = new Image(4, 2);
    actual.getGraphics().copyRect(deferredSource, 0, 0, 4, 2, 0, 0);

    assertArrayEquals(expected.getPixels(), actual.getPixels());
    assertNull(backing(deferredSource));
  }

  @Test
  void copyRectResolvesDeferredSourceAtDestinationContentScale() throws Exception {
    Image base = new Image(2, 2);
    java.util.Arrays.fill(base.getPixels(), 0xFF55AA33);
    Image source = base.getSmoothScaledInstance(1, 1);
    Image destination = Image.createLogical(1, 1, 2);

    destination.getGraphics().copyRect(source, 0, 0, 1, 1, 0, 0);

    assertAllPixels(destination, 0xFF55AA33);
    assertNull(backing(source));
  }

  @Test
  void materializedSourceStillUsesItsExistingNaturalBacking() throws Exception {
    Image source = Image.createLogical(1, 1, 2);
    java.util.Arrays.fill(source.getPixels(), 0xFF0000FF);
    Image destination = Image.createLogical(1, 1, 4);

    destination.getGraphics().drawImage(source, 0, 0);

    assertAllPixels(destination, 0xFF0000FF);
    assertEquals(2, source.getContentScale());
  }

  private static Object backing(Image image) throws Exception {
    Field field = Image.class.getDeclaredField("backing");
    field.setAccessible(true);
    return field.get(image);
  }

  private static void assertAllPixels(Image image, int expected) {
    for (int pixel : image.getPixels()) {
      assertEquals(expected, pixel);
    }
  }

  private static Image filledImage(int width, int height, int pixel) throws Exception {
    Image image = new Image(width, height);
    java.util.Arrays.fill(image.getPixels(), pixel);
    return image;
  }

  private static byte[] twoFramePng() throws Exception {
    BufferedImage source = new BufferedImage(8, 2, BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < source.getHeight(); y++) {
      for (int x = 0; x < source.getWidth(); x++) {
        source.setRGB(x, y, x < 4 ? 0xFF204060 : 0xFFB06020);
      }
    }
    ByteArrayOutputStream encoded = new ByteArrayOutputStream();
    if (!ImageIO.write(source, "png", encoded)) {
      throw new AssertionError("PNG writer unavailable");
    }
    byte[] png = encoded.toByteArray();
    int iend = png.length - 12;
    ByteArrayOutputStream withFrameCount = new ByteArrayOutputStream(png.length + 32);
    withFrameCount.write(png, 0, iend);
    byte[] text = "Comment\0FC=2".getBytes("ISO-8859-1");
    byte[] type = "tEXt".getBytes("ISO-8859-1");
    writeInt(withFrameCount, text.length);
    withFrameCount.write(type);
    withFrameCount.write(text);
    CRC32 crc = new CRC32();
    crc.update(type);
    crc.update(text);
    writeInt(withFrameCount, (int) crc.getValue());
    withFrameCount.write(png, iend, 12);
    return withFrameCount.toByteArray();
  }

  private static void writeInt(ByteArrayOutputStream output, int value) {
    output.write(value >> 24);
    output.write(value >> 16);
    output.write(value >> 8);
    output.write(value);
  }
}
