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

  @Test
  void javaRendererDrawsScaledImagesAtNaturalLogicalSize() throws Exception {
    Image source = Image.createLogical(2, 2, 2);
    java.util.Arrays.fill(source.getPixels(), 0xFFFF0000);
    Image destination = new Image(2, 2);

    destination.getGraphics().drawImage(source, 0, 0);

    for (int pixel : destination.getPixels()) {
      assertEquals(0xFFFF0000, pixel);
    }
  }

  @Test
  void javaPrimitivesCoverPhysicalImageBackingFromLogicalEdges() throws Exception {
    Image image = Image.createLogical(2, 2, 1.5);
    Graphics graphics = image.getGraphics();

    graphics.backColor = Color.RED;
    graphics.fillRect(1, 0, 1, 1);
    graphics.foreColor = Color.BLUE;
    graphics.setPixel(0, 1);
    graphics.foreColor = Color.GREEN;
    graphics.drawLine(0, 0, 0, 0);

    int[] pixels = image.getPixels();
    assertEquals(0xFF00FF00, pixels[0]);
    assertEquals(0xFF00FF00, pixels[1]);
    assertEquals(0xFFFF0000, pixels[2]);
    assertEquals(0xFF0000FF, pixels[6]);
    assertEquals(0xFF0000FF, pixels[7]);
  }

  @Test
  void framesExposeTheirVisibleLogicalWidth() throws Exception {
    Image image = new Image(6, 1);

    image.setFrameCount(2);

    assertEquals(3, image.getWidth());
    assertEquals(3, image.getPixelWidth());
  }

  @Test
  void javaRendererSamplesTheWholePhysicalBackingAtNaturalLogicalSize() throws Exception {
    Image source = Image.createLogical(2, 2, 2);
    int[] pixels = source.getPixels();
    pixels[0] = pixels[1] = pixels[4] = pixels[5] = 0xFFFF0000;
    pixels[2] = pixels[3] = pixels[6] = pixels[7] = 0xFF00FF00;
    pixels[8] = pixels[9] = pixels[12] = pixels[13] = 0xFF0000FF;
    pixels[10] = pixels[11] = pixels[14] = pixels[15] = 0xFFFFFFFF;
    Image destination = new Image(2, 2);

    destination.getGraphics().drawImage(source, 0, 0);

    assertEquals(0xFFFF0000, destination.getPixels()[0]);
    assertEquals(0xFF00FF00, destination.getPixels()[1]);
    assertEquals(0xFF0000FF, destination.getPixels()[2]);
    assertEquals(0xFFFFFFFF, destination.getPixels()[3]);
  }

  @Test
  void javaRendererDrawsIntoScaledDestinationAtLogicalCoordinates() throws Exception {
    Image source = new Image(2, 1);
    source.getPixels()[0] = 0xFFFF0000;
    source.getPixels()[1] = 0xFF00FF00;
    Image destination = Image.createLogical(2, 1, 2);

    destination.getGraphics().drawImage(source, 0, 0);

    assertEquals(0xFFFF0000, destination.getPixels()[0]);
    assertEquals(0xFFFF0000, destination.getPixels()[1]);
    assertEquals(0xFF00FF00, destination.getPixels()[2]);
    assertEquals(0xFF00FF00, destination.getPixels()[3]);
    assertEquals(0xFFFF0000, destination.getPixels()[4]);
    assertEquals(0xFF00FF00, destination.getPixels()[6]);

    Image clippedDestination = Image.createLogical(1, 1, 2);
    clippedDestination.getGraphics().copyImageRect(source, 1, 0, 1, 1, true);
    for (int pixel : clippedDestination.getPixels()) {
      assertEquals(0xFF00FF00, pixel);
    }
  }

  @Test
  void imageTransformationsKeepTheirExistingFixedPixelResultContract() throws Exception {
    Image source = Image.createLogical(3, 2, 2);

    Image transformed = source.getScaledInstance(6, 4);

    assertEquals(6, transformed.getWidth());
    assertEquals(4, transformed.getHeight());
    assertEquals(6, transformed.getPixelWidth());
    assertEquals(4, transformed.getPixelHeight());
    assertEquals(1, transformed.getContentScale());
  }
}
