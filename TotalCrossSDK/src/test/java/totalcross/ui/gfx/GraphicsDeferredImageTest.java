// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.gfx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Field;

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
}
