// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.gfx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import totalcross.Launcher;
import totalcross.sys.Settings;
import totalcross.ui.Control;
import totalcross.ui.Label;
import totalcross.ui.Window;
import totalcross.ui.font.Font;
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
  void controlSurfaceRasterizesLogicalRectIntoPhysicalBacking() {
    new Launcher();
    TestControl control = new TestControl();
    int originalWidth = Settings.screenWidth;
    int originalHeight = Settings.screenHeight;
    try {
      Settings.screenWidth = 2;
      Settings.screenHeight = 2;
      Graphics.configureMainWindowSurface(2, 2, 2);
      Graphics graphics = new Graphics(control);
      graphics.refresh(0, 0, 2, 2, 0, 0, null);
      graphics.backColor = 0x123456;

      graphics.fillRect(1, 0, 1, 1);

      assertEquals(16, Graphics.mainWindowPixels.length);
      assertEquals(0, Graphics.mainWindowPixels[0]);
      assertEquals(0, Graphics.mainWindowPixels[1]);
      assertEquals(0xFF123456, Graphics.mainWindowPixels[2]);
      assertEquals(0xFF123456, Graphics.mainWindowPixels[3]);
      assertEquals(0xFF123456, Graphics.mainWindowPixels[6]);
      assertEquals(0xFF123456, Graphics.mainWindowPixels[7]);
    } finally {
      Settings.screenWidth = originalWidth;
      Settings.screenHeight = originalHeight;
      Graphics.configureMainWindowSurface(Math.max(0, originalWidth), Math.max(0, originalHeight), 1);
      Graphics.mainWindowPixels = null;
    }
  }

  @Test
  void javaLauncherDensityMatrixRasterizesTheCompleteWindowBoundary() throws Exception {
    new Launcher();
    TestControl control = new TestControl();
    int originalWidth = Settings.screenWidth;
    int originalHeight = Settings.screenHeight;
    double originalDensity = Settings.screenDensity;
    TextRaster baseline = null;
    int preferredWidth = -1;
    int preferredHeight = -1;
    int logicalFontSize = -1;
    try {
      for (int density = 1; density <= 3; density++) {
        Settings.screenWidth = 64;
        Settings.screenHeight = 48;
        Settings.screenDensity = density;
        Graphics.configureMainWindowSurface(64, 48, density);
        control.setRect(0, 0, 64, 48);
        Graphics graphics = new Graphics(control);
        graphics.refresh(0, 0, 64, 48, 0, 0, null);

        assertEquals(64, Settings.screenWidth);
        assertEquals(48, Settings.screenHeight);
        assertEquals(density, graphics.getContentScale());
        assertEquals(64 * density, graphics.getSurfacePixelWidth());
        assertEquals(48 * density, graphics.getSurfacePixelHeight());
        assertEquals(64 * density, graphics.getSurfacePixelPitch());
        assertEquals(64 * 48 * density * density, Graphics.mainWindowPixels.length);

        Label label = new Label("logical geometry");
        if (density == 1) {
          preferredWidth = label.getPreferredWidth();
          preferredHeight = label.getPreferredHeight();
          logicalFontSize = label.getFont().size;
        } else {
          assertEquals(preferredWidth, label.getPreferredWidth());
          assertEquals(preferredHeight, label.getPreferredHeight());
          assertEquals(logicalFontSize, label.getFont().size);
        }

        clearWindow();
        graphics.backColor = Color.RED;
        graphics.fillRect(10, 8, 7, 5);
        assertPhysicalBounds(0xFFFF0000, 10 * density, 8 * density, 7 * density, 5 * density);

        clearWindow();
        graphics.foreColor = Color.GREEN;
        graphics.drawLine(4, 6, 12, 6);
        assertPhysicalBounds(0xFF00FF00, 4 * density, 6 * density, 9 * density, density);

        clearWindow();
        graphics.translate(3, 2);
        graphics.setClip(1, 1, 4, 3);
        graphics.backColor = Color.BLUE;
        graphics.fillRect(0, 0, 10, 10);
        assertPhysicalBounds(0xFF0000FF, 4 * density, 3 * density, 4 * density, 3 * density);
        graphics.refresh(0, 0, 64, 48, 0, 0, null);

        clearWindow();
        Image source = new Image(4, 1);
        source.getPixels()[0] = 0xFFFF0000;
        source.getPixels()[1] = 0xFF00FF00;
        source.getPixels()[2] = 0xFF0000FF;
        source.getPixels()[3] = 0xFFFFFFFF;
        graphics.copyImageRect(source, 1, 0, 2, 1, true);
        assertPhysicalBounds(0xFF00FF00, 0, 0, density, density);
        assertPhysicalBounds(0xFF0000FF, density, 0, density, density);

        clearWindow();
        Image highResolutionSource = Image.createLogical(2, 1, density);
        for (int pixelY = 0; pixelY < density; pixelY++) {
          for (int pixelX = 0; pixelX < 2 * density; pixelX++) {
            highResolutionSource.getPixels()[pixelY * 2 * density + pixelX] =
                0xFF000000 | ((pixelY + 1) << 12) | pixelX + 1;
          }
        }
        graphics.drawImage(highResolutionSource, 8, 10);
        for (int pixelY = 0; pixelY < density; pixelY++) {
          for (int pixelX = 0; pixelX < 2 * density; pixelX++) {
            assertEquals(highResolutionSource.getPixels()[pixelY * 2 * density + pixelX],
                Graphics.mainWindowPixels[(10 * density + pixelY) * Graphics.getMainWindowPixelWidth()
                    + 8 * density + pixelX]);
          }
        }

        clearWindow();
        int[] logicalPixels = {0xFF112233, 0xFF445566};
        assertEquals(2, graphics.setRGB(logicalPixels, 0, 5, 4, 2, 1));
        assertPhysicalBounds(0xFF112233, 5 * density, 4 * density, density, density);
        assertPhysicalBounds(0xFF445566, 6 * density, 4 * density, density, density);
        int[] readBack = new int[2];
        assertEquals(2, graphics.getRGB(readBack, 0, 5, 4, 2, 1));
        assertEquals(logicalPixels[0], readBack[0]);
        assertEquals(logicalPixels[1], readBack[1]);

        clearWindow();
        graphics.backColor = 0x123456;
        graphics.fillRect(2, 2, 3, 2);
        graphics.dither(2, 2, 3, 2);
        assertDitheredRegion(2 * density, 2 * density, 3 * density, 2 * density);

        clearWindow();
        graphics.backColor = 0x204060;
        graphics.fillRect(1, 1, 2, 2);
        Graphics.fadeScreen(128);
        assertPhysicalBounds(0xFF102030, density, density, 2 * density, 2 * density);

        clearWindow();
        graphics.backColor = Color.BLUE;
        graphics.fillRect(0, 0, 2, 2);
        Image alphaSource = new Image(1, 1);
        alphaSource.getPixels()[0] = 0x80FF0000;
        graphics.drawImage(alphaSource, 0, 0);
        assertPhysicalBounds(Graphics.mainWindowPixels[0], 0, 0, density, density);
        assertTrue((Graphics.mainWindowPixels[0] & 0xFFFFFF) != Color.BLUE);
        assertTrue((Graphics.mainWindowPixels[0] & 0xFFFFFF) != Color.RED);

        clearWindow();
        graphics.backColor = Color.RED;
        graphics.fillRect(0, 0, 1, 1);
        graphics.backColor = Color.GREEN;
        graphics.fillRect(1, 0, 1, 1);
        graphics.copyRect(control, 0, 0, 2, 1, 1, 0);
        assertEquals(Color.RED, graphics.getPixel(1, 0));
        assertEquals(Color.GREEN, graphics.getPixel(2, 0));
        Image physicalScreenCopy = Image.createLogical(3, 1, density);
        physicalScreenCopy.getGraphics().copyRect(control, 0, 0, 3, 1, 0, 0);
        for (int pixelX = 0; pixelX < 3 * density; pixelX++) {
          assertEquals(Graphics.mainWindowPixels[pixelX], physicalScreenCopy.getPixels()[pixelX]);
        }
        Image logicalScreenCopy = new Image(3, 1);
        logicalScreenCopy.getGraphics().copyRect(control, 0, 0, 3, 1, 0, 0);
        assertEquals(0xFFFF0000, logicalScreenCopy.getPixels()[0]);
        assertEquals(0xFFFF0000, logicalScreenCopy.getPixels()[1]);
        assertEquals(0xFF00FF00, logicalScreenCopy.getPixels()[2]);

        Window screenshotWindow = new Window();
        PaintedControl painted = new PaintedControl();
        screenshotWindow.add(painted, 5, 4, 6, 3);
        screenshotWindow.takeScreenShot();
        Image screenshot = screenshotWindow.offscreen;
        assertEquals(density, screenshot.getContentScale());
        assertEquals(64, screenshot.getWidth());
        assertEquals(48, screenshot.getHeight());
        assertEquals(64 * density, screenshot.getPixelWidth());
        assertEquals(48 * density, screenshot.getPixelHeight());
        assertImageBounds(screenshot, 0xFF345678, 5 * density, 4 * density, 6 * density, 3 * density);
        screenshotWindow.releaseScreenShot();

        clearWindow();
        graphics.setFont(Font.getFont(false, 14));
        graphics.foreColor = Color.WHITE;
        graphics.drawText("Density", 3, 3);
        TextRaster raster = textRaster(density);
        if (baseline == null) {
          baseline = raster;
        } else {
          assertEquals(baseline.logicalWidth, raster.logicalWidth, 1.0);
          assertEquals(baseline.logicalHeight, raster.logicalHeight, 1.0);
          assertTrue(raster.physicalWidth > baseline.physicalWidth);
          assertTrue(raster.physicalHeight > baseline.physicalHeight);
          assertTrue(raster.paintCount > baseline.paintCount);
          assertTrue(raster.hasSubLogicalPixelDetail);
        }
      }
    } finally {
      Settings.screenWidth = originalWidth;
      Settings.screenHeight = originalHeight;
      Settings.screenDensity = originalDensity;
      Graphics.configureMainWindowSurface(Math.max(0, originalWidth), Math.max(0, originalHeight),
          originalDensity > 0 ? originalDensity : 1);
      Graphics.mainWindowPixels = null;
    }
  }

  private static void clearWindow() {
    java.util.Arrays.fill(Graphics.mainWindowPixels, 0);
  }

  private static void assertPhysicalBounds(int color, int x, int y, int width, int height) {
    assertBounds(Graphics.mainWindowPixels, Graphics.getMainWindowPixelWidth(), color, x, y, width, height);
  }

  private static void assertImageBounds(Image image, int color, int x, int y, int width, int height) {
    assertBounds(image.getPixels(), image.getPixelWidth(), color, x, y, width, height);
  }

  private static void assertBounds(int[] pixels, int pitch, int color, int x, int y, int width, int height) {
    int minX = Integer.MAX_VALUE;
    int minY = Integer.MAX_VALUE;
    int maxX = -1;
    int maxY = -1;
    for (int index = 0; index < pixels.length; index++) {
      if (pixels[index] == color) {
        int pixelX = index % pitch;
        int pixelY = index / pitch;
        minX = Math.min(minX, pixelX);
        minY = Math.min(minY, pixelY);
        maxX = Math.max(maxX, pixelX);
        maxY = Math.max(maxY, pixelY);
      }
    }
    assertEquals(x, minX);
    assertEquals(y, minY);
    assertEquals(width, maxX - minX + 1);
    assertEquals(height, maxY - minY + 1);
  }

  private static void assertDitheredRegion(int x, int y, int width, int height) {
    int pitch = Graphics.getMainWindowPixelWidth();
    for (int yy = y; yy < y + height; yy++) {
      for (int xx = x; xx < x + width; xx++) {
        int pixel = Graphics.mainWindowPixels[yy * pitch + xx];
        assertEquals(0, ((pixel >> 16) & 0xFF) & 7);
        assertEquals(0, ((pixel >> 8) & 0xFF) & 3);
        assertEquals(0, (pixel & 0xFF) & 7);
      }
    }
  }

  private static TextRaster textRaster(int density) {
    int pitch = Graphics.getMainWindowPixelWidth();
    int minX = Integer.MAX_VALUE;
    int minY = Integer.MAX_VALUE;
    int maxX = -1;
    int maxY = -1;
    int count = 0;
    boolean detail = density == 1;
    for (int index = 0; index < Graphics.mainWindowPixels.length; index++) {
      if (Graphics.mainWindowPixels[index] != 0) {
        int x = index % pitch;
        int y = index / pitch;
        minX = Math.min(minX, x);
        minY = Math.min(minY, y);
        maxX = Math.max(maxX, x);
        maxY = Math.max(maxY, y);
        count++;
      }
    }
    if (density > 1) {
      for (int logicalY = minY / density; logicalY <= maxY / density && !detail; logicalY++) {
        for (int logicalX = minX / density; logicalX <= maxX / density && !detail; logicalX++) {
          int first = Graphics.mainWindowPixels[logicalY * density * pitch + logicalX * density];
          for (int dy = 0; dy < density && !detail; dy++) {
            for (int dx = 0; dx < density; dx++) {
              if (Graphics.mainWindowPixels[(logicalY * density + dy) * pitch + logicalX * density + dx] != first) {
                detail = true;
                break;
              }
            }
          }
        }
      }
    }
    return new TextRaster(maxX - minX + 1, maxY - minY + 1, density, count, detail);
  }

  private static final class TextRaster {
    private final int physicalWidth;
    private final int physicalHeight;
    private final double logicalWidth;
    private final double logicalHeight;
    private final int paintCount;
    private final boolean hasSubLogicalPixelDetail;

    private TextRaster(int physicalWidth, int physicalHeight, int density, int paintCount,
        boolean hasSubLogicalPixelDetail) {
      this.physicalWidth = physicalWidth;
      this.physicalHeight = physicalHeight;
      this.logicalWidth = (double) physicalWidth / density;
      this.logicalHeight = (double) physicalHeight / density;
      this.paintCount = paintCount;
      this.hasSubLogicalPixelDetail = hasSubLogicalPixelDetail;
    }
  }

  private static final class TestControl extends Control {
    private TestControl() {
      super();
    }
  }

  private static final class PaintedControl extends Control {
    @Override
    public void onPaint(Graphics graphics) {
      graphics.backColor = 0x345678;
      graphics.fillRect(0, 0, width, height);
    }
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
