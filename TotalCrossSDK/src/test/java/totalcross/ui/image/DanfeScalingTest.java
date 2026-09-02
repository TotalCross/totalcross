// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import totalcross.io.ByteArrayStream;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;

class DanfeScalingTest {
  private static final int LOGICAL_WIDTH = 360;
  private static final int LOGICAL_HEIGHT = 540;
  private static final int BARCODE_Y = 460;

  @Test
  void rendersTheSameBarcodeStructureAtDefaultAndDoubleBackingScale() throws Exception {
    Image defaultImage = render(new Image(LOGICAL_WIDTH, LOGICAL_HEIGHT));
    Image scaledImage = render(Image.createLogical(LOGICAL_WIDTH, LOGICAL_HEIGHT, 2));

    assertDimensions(defaultImage, 360, 540);
    assertDimensions(scaledImage, 720, 1080);
    assertEquals(31, countDarkRuns(defaultImage, BARCODE_Y));
    assertEquals(31, countDarkRuns(scaledImage, BARCODE_Y * 2));
  }

  @Test
  void preservesPhysicalPixelsAndLogicalDrawingAcrossSynchronization() throws Exception {
    Image image = Image.createLogical(3, 2, 2);
    int[] pixels = image.getPixels();
    pixels[3 * image.getPixelWidth() + 5] = 0x80A0B0C0;
    image.applyChanges();

    Graphics graphics = image.getGraphics();
    graphics.backColor = Color.BLACK;
    graphics.fillRect(1, 0, 1, 1);
    image.applyChanges();

    assertEquals(0x80A0B0C0, image.getPixels()[3 * image.getPixelWidth() + 5]);
    assertEquals(0xFF000000, image.getPixels()[2]);
    assertEquals(0xFF000000, image.getPixels()[3]);
    assertEquals(0xFF000000, image.getPixels()[8]);
    assertEquals(0xFF000000, image.getPixels()[9]);
  }

  @Test
  void ordinaryPngLoadsAtFixedPixelScale() throws Exception {
    Image source = Image.createLogical(3, 2, 2);
    ByteArrayStream png = new ByteArrayStream(128);
    source.createPng(png);
    png.setPos(0);

    Image loaded = new Image(png);

    assertEquals(6, loaded.getWidth());
    assertEquals(4, loaded.getHeight());
    assertEquals(6, loaded.getPixelWidth());
    assertEquals(4, loaded.getPixelHeight());
    assertEquals(1, loaded.getContentScale());
  }

  @Test
  void derivedImagesRetainHardwareScale() throws Exception {
    Image source = new Image(4, 2);
    source.setHwScaleFixedAspectRatio(8, false);

    Image derived = source.getScaledInstance(2, 1);

    assertEquals(4, derived.getWidth());
    assertEquals(2, derived.getHeight());
  }

  private static Image render(Image image) {
    Graphics graphics = image.getGraphics();
    graphics.backColor = Color.WHITE;
    graphics.fillRect(0, 0, LOGICAL_WIDTH, LOGICAL_HEIGHT);
    graphics.backColor = Color.DARK;
    graphics.fillRect(12, 12, 336, 30);
    graphics.backColor = Color.BLACK;
    for (int run = 0; run < 31; run++) {
      graphics.fillRect(20 + run * 10, BARCODE_Y, 4, 40);
    }
    return image;
  }

  private static void assertDimensions(Image image, int pixelWidth, int pixelHeight) throws Exception {
    assertEquals(LOGICAL_WIDTH, image.getWidth());
    assertEquals(LOGICAL_HEIGHT, image.getHeight());
    assertEquals(pixelWidth, image.getPixelWidth());
    assertEquals(pixelHeight, image.getPixelHeight());

    ByteArrayStream png = new ByteArrayStream(1024);
    image.createPng(png);
    BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(Arrays.copyOf(png.getBuffer(), png.getPos())));
    assertEquals(pixelWidth, decoded.getWidth());
    assertEquals(pixelHeight, decoded.getHeight());
  }

  private static int countDarkRuns(Image image, int physicalY) {
    int runs = 0;
    boolean dark = false;
    for (int x = 0; x < image.getPixelWidth(); x++) {
      boolean current = (image.getPixels()[physicalY * image.getPixelWidth() + x] & 0xFFFFFF) == Color.BLACK;
      if (current && !dark) {
        runs++;
      }
      dark = current;
    }
    return runs;
  }
}
