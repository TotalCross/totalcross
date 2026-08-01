// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.gfx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import totalcross.ui.image.Image;

class GraphicsScaleTest {
  @Test
  void defaultsToOneAndAcceptsFractionalScales() throws Exception {
    Graphics graphics = new Graphics(new Image(1, 1));

    assertEquals(1, graphics.getContentScale());
    assertEquals(1, graphics.getFontScale());

    graphics.setScales(1.5, 2);
    assertEquals(1.5, graphics.getContentScale());
    assertEquals(2, graphics.getFontScale());
  }

  @Test
  void rejectsInvalidScales() throws Exception {
    Graphics graphics = new Graphics(new Image(1, 1));

    assertThrows(IllegalArgumentException.class, () -> graphics.setScales(0, 1));
    assertThrows(IllegalArgumentException.class, () -> graphics.setScales(1, Double.NaN));
  }

  @Test
  void logicalImagesKeepLogicalAndPhysicalDimensionsSeparate() throws Exception {
    Image image = Image.createLogical(3, 7, 1.5);

    assertEquals(3, image.getWidth());
    assertEquals(7, image.getHeight());
    assertEquals(5, image.getPixelWidth());
    assertEquals(11, image.getPixelHeight());
    assertEquals(55, image.getPixels().length);
    assertEquals(1.5, image.getContentScale());
  }
}
