// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import totalcross.Launcher;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;

class ControlScreenshotTest {
  @BeforeAll
  static void initializeFontBackend() {
    new Launcher();
  }

  @Test
  void nestedTargetUsesTargetLocalCoordinates() {
    Window owner = owner();
    Container ancestor = new Container();
    owner.add(ancestor, 80, 90, 150, 150);
    Container target = new Container();
    target.transparentBackground = true;
    ancestor.add(target, 60, 70, 80, 70);
    target.add(new ColorBlock(Color.RED), 7, 9, 11, 13);

    target.takeScreenShot();

    assertEquals(Color.RED, rgbAt(target, 7, 9));
    assertEquals(Color.RED, rgbAt(target, 17, 21));
    assertEquals(0, target.offscreen.getPixels()[0]);
  }

  @Test
  void transparentRootLeavesUntouchedPixelsTransparent() {
    Window owner = owner();
    Container target = new Container();
    target.transparentBackground = true;
    owner.add(target, 40, 50, 30, 20);
    target.add(new ColorBlock(Color.BLUE), 10, 6, 5, 4);

    target.takeScreenShot();

    assertEquals(0, target.offscreen.getPixels()[0]);
    assertEquals(Color.BLUE, rgbAt(target, 10, 6));
  }

  @Test
  void screenshotPreservesDestinationContentAndFontScales() {
    Window owner = owner();
    ScaleCapture target = new ScaleCapture();
    target.transparentBackground = true;
    owner.add(target, 20, 25, 12, 8);
    target.getGraphics().setScales(3, 1.75);

    target.takeScreenShot();

    assertEquals(12, target.offscreen.getWidth());
    assertEquals(8, target.offscreen.getHeight());
    assertEquals(36, target.offscreen.getPixelWidth());
    assertEquals(24, target.offscreen.getPixelHeight());
    assertEquals(3, target.offscreen.getContentScale());
    assertEquals(3, target.seenContentScale);
    assertEquals(1.75, target.seenFontScale);
  }

  private static Window owner() {
    Window owner = new Window();
    owner.setRect(0, 0, 240, 320);
    return owner;
  }

  private static int rgbAt(Control control, int x, int y) {
    int physicalX = (int) (x * control.offscreen.getContentScale());
    int physicalY = (int) (y * control.offscreen.getContentScale());
    return control.offscreen.getPixels()[physicalY * control.offscreen.getPixelWidth() + physicalX] & 0xFFFFFF;
  }

  private static final class ColorBlock extends Control {
    private final int color;

    ColorBlock(int color) {
      this.color = color;
      transparentBackground = true;
    }

    @Override
    public void onPaint(Graphics g) {
      g.backColor = color;
      g.fillRect(0, 0, width, height);
    }
  }

  private static final class ScaleCapture extends Control {
    double seenContentScale;
    double seenFontScale;

    @Override
    public void onPaint(Graphics g) {
      seenContentScale = g.getContentScale();
      seenFontScale = g.getFontScale();
    }
  }
}
