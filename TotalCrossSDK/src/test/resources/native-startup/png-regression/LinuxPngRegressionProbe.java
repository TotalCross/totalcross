// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.io.File;
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.ui.Container;
import totalcross.ui.ImageControl;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;

/** Focused deployed regressions for the historical Linux PNG failures. */
public final class LinuxPngRegressionProbe extends MainWindow {
  @Override
  public void initUI() {
    try {
      checkHistoricalRgbaFile();
      checkHistoricalRgbaBytes();
      checkOriginalOwnershipDrawing();
      System.out.println("TC_LINUX_PNG_RESULT complete=true");
      System.out.flush();
      MainWindow.exit(0);
    } catch (Throwable failure) {
      System.out.println("TC_LINUX_PNG_RESULT complete=false");
      System.out.println("TC_LINUX_PNG_FAIL " + failure.getClass().getName() + ": "
          + String.valueOf(failure.getMessage()));
      failure.printStackTrace();
      System.out.flush();
      MainWindow.exit(1);
    }
  }

  private static void checkHistoricalRgbaFile() throws Throwable {
    File file = new File(path("historical-rgba-36x36.png"), File.READ_ONLY);
    Image image;
    try {
      image = new Image(file);
    } finally {
      file.close();
    }
    checkHistoricalPixels(image, "historical-rgba-file");
  }

  private static void checkHistoricalRgbaBytes() throws Throwable {
    Image image = new Image(readFixture("historical-rgba-36x36.png"));
    checkHistoricalPixels(image, "historical-rgba-bytes");
  }

  private static void checkHistoricalPixels(Image image, String name) {
    int[] pixels = image.getPixels();
    if (image.getWidth() != 36 || image.getHeight() != 36 || pixels.length != 1296
        || pixels[0] != 0) {
      throw new IllegalStateException(name + " expected 36x36 transparent-black pixel data");
    }
    System.out.println("TC_LINUX_PNG case=" + name
        + ",status=PASS,width=36,height=36,pixelCount=1296,pixel0=0");
  }

  private void checkOriginalOwnershipDrawing() throws Throwable {
    byte[] png = readFixture("ownership-red-2x2.png");
    if (png.length != 74) {
      throw new IllegalStateException("original ownership PNG must remain 74 bytes");
    }
    Image image = new Image(png);
    if (image.getWidth() != 2 || image.getHeight() != 2) {
      throw new IllegalStateException("original ownership PNG dimensions changed");
    }

    Container screen = new Container();
    screen.add(new ImageControl(image));
    add(screen);
    Image firstRaster = ImageDrawingBridge.resolveForDrawing(image, 1,
        ImageDrawingBridge.AdmissionMode.IMMEDIATE);
    Image secondRaster = ImageDrawingBridge.resolveForDrawing(image, 1,
        ImageDrawingBridge.AdmissionMode.IMMEDIATE);
    if (firstRaster != secondRaster || image.cachedFinalRasterForDrawing(1) != firstRaster) {
      throw new IllegalStateException("repeated drawing resolution did not reuse the PNG raster");
    }

    Image destination = new Image(4, 4);
    Graphics graphics = destination.getGraphics();
    ImageDrawingBridge.drawPersistentImage(graphics, image, 0, 0, true);
    ImageDrawingBridge.drawPersistentImage(graphics, image, 0, 0, true);
    int pixel = graphics.getPixel(0, 0);
    if ((pixel & 0xFFFFFF) != Color.RED) {
      throw new IllegalStateException("original PNG draw expected red pixel; got 0x"
          + Integer.toHexString(pixel));
    }
    File rendered = new File("image-ownership-render.png", File.CREATE_EMPTY);
    try {
      destination.createPng(rendered);
    } finally {
      rendered.close();
    }

    remove(screen);
    if (ImageDrawingBridge.persistentOwnerCount(image) != 0) {
      throw new IllegalStateException("detaching the PNG control left a persistent image owner");
    }
    screen.removeAll();
    System.out.println("TC_LINUX_PNG case=original-ownership-drawing,status=PASS"
        + ",width=2,height=2,drawPixelRgb=" + (pixel & 0xFFFFFF)
        + ",ownerCountAfterDetach=0");
  }

  private static byte[] readFixture(String name) throws Throwable {
    return new File(path(name), File.READ_ONLY).readAndClose();
  }

  private static String path(String name) {
    return Convert.appendPath(Settings.appPath, name);
  }
}
