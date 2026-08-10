// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.simulator.awt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import totalcross.sys.Settings;
import totalcross.ui.gfx.Graphics;

class AwtWindowTest {
  @Test
  void preservesExplicitPresentationScale() {
    WindowConfiguration config = new WindowConfiguration(640, 960, 0.75, "test", 0, 0, false, true, null, null,
        null);

    assertEquals(0.75, AwtWindow.resolveScale(config), 0.000001);
  }

  @Test
  void fitsAutomaticScaleToDisplayWithUsableMargin() {
    WindowConfiguration config = new WindowConfiguration(1920, 1080, -1, "test", 0, 0, false, true, null, null,
        null);

    assertEquals(0.88 / 1.2, AwtWindow.resolveScale(config, new Rectangle(0, 0, 1600, 1200)), 0.000001);
  }

  @Test
  void defaultsToOneWhenAutomaticScaleCannotInspectDisplay() {
    WindowConfiguration config = new WindowConfiguration(1440, 2560, -1, "test", 0, 0, false, true, null, null,
        null);

    assertEquals(1, AwtWindow.resolveScale(config, null), 0.000001);
  }

  @Test
  void densityChangesBackingResolutionWithoutChangingHostViewport() {
    AwtRenderSurface scaleOne = new AwtRenderSurface(null, 1, 1, true);
    AwtRenderSurface scaleTwo = new AwtRenderSurface(null, 1, 2, true);
    AwtRenderSurface scaleThree = new AwtRenderSurface(null, 1, 3, true);
    int[] lowResolution = new int[4];
    int[] highResolution = new int[16];
    int[] highestResolution = new int[36];
    Arrays.fill(lowResolution, 0xFF123456);
    Arrays.fill(highResolution, 0xFF123456);
    Arrays.fill(highestResolution, 0xFF123456);

    scaleOne.present(lowResolution, 2, 2);
    scaleTwo.present(highResolution, 4, 4);
    scaleThree.present(highestResolution, 6, 6);

    assertEquals(4, paintedPixelCount(scaleOne));
    assertEquals(4, paintedPixelCount(scaleTwo));
    assertEquals(4, paintedPixelCount(scaleThree));
  }

  @Test
  void hostDefaultTransformCannotRedefineSimulatedDensity() {
    double originalDensity = Settings.screenDensity;
    try {
      Settings.screenDensity = 2;
      Graphics.configureMainWindowSurface(10, 10, 2);

      assertEquals(3, AwtWindow.resolveHostPresentationScale(AffineTransform.getScaleInstance(3, 3)));
      assertEquals(2, Settings.screenDensity);
      assertEquals(2, Graphics.getMainWindowContentScale());
      assertEquals(20, Graphics.getMainWindowPixelWidth());
    } finally {
      Settings.screenDensity = originalDensity;
      Graphics.configureMainWindowSurface(0, 0, originalDensity > 0 ? originalDensity : 1);
      Graphics.mainWindowPixels = null;
    }
  }

  private static int paintedPixelCount(AwtRenderSurface surface) {
    BufferedImage target = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
    java.awt.Graphics2D graphics = target.createGraphics();
    try {
      surface.paint(graphics);
    } finally {
      graphics.dispose();
    }
    int count = 0;
    for (int pixel : target.getRGB(0, 0, 4, 4, null, 0, 4)) {
      if (pixel == 0xFF123456) {
        count++;
      }
    }
    return count;
  }
}
