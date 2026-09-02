// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.gfx;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    assertNull(pixels(source));

    for (int scale = 1; scale <= 4; scale *= 2) {
      Image destination = Image.createLogical(1, 1, scale);
      destination.getGraphics().drawImage(source, 0, 0);
      assertAllPixels(destination, 0xFFFF0000);
      assertNull(pixels(source));
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
    assertNull(pixels(source));
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

  private static int[] pixels(Image image) throws Exception {
    Field field = Image.class.getDeclaredField("pixels");
    field.setAccessible(true);
    return (int[]) field.get(image);
  }

  private static void assertAllPixels(Image image, int expected) {
    for (int pixel : image.getPixels()) {
      assertEquals(expected, pixel);
    }
  }
}
