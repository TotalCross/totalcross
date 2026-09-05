// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.io.ByteArrayStream;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** Deployed macOS smoke for preserving deferred rotation through PNG output. */
public class ImageNativeRotateSaveSmokeApp extends MainWindow {
  @Override
  public void initUI() {
    boolean pass = false;
    String failure = "";
    try {
      Image source = patterned(3, 2);
      ByteArrayStream input = new ByteArrayStream(512);
      source.createPng(input);

      Image opened = new Image(input.getBuffer(), input.getPos());
      Image rotated = opened.getRotatedScaledInstance(150, 37, 0xFF102030);
      require(rotated.pipelineForSmoke() != null, "rotation remains deferred");

      Image directTarget = new Image(rotated.getWidth(), rotated.getHeight());
      directTarget.getGraphics().drawImage(rotated, 0, 0, true);
      int[] directPixels = directTarget.getPixels();
      require(contains(directPixels, 0xFF102030), "rotation fill color");

      ByteArrayStream output = new ByteArrayStream(512);
      rotated.createPng(output);
      require(rotated.pipelineForSmoke() == null && rotated.hasNativeBackingForSmoke(),
          "rotation materialization barrier");

      Image saved = new Image(output.getBuffer(), output.getPos());
      require(saved.getPixelWidth() == rotated.getPixelWidth()
          && saved.getPixelHeight() == rotated.getPixelHeight(), "saved dimensions");
      require(same(saved.getPixels(), directPixels), "saved rotated pixels");
      pass = true;
    } catch (Throwable failureCause) {
      failure = failureCause.toString();
    }
    System.out.println("fixture=ImageNativeRotateSaveSmokeApp,pass=" + pass
        + (failure.length() == 0 ? "" : ",failure=" + failure.replace(' ', '_')));
    exit(pass ? 0 : 1);
  }

  private static Image patterned(int width, int height) throws Exception {
    Image image = new Image(width, height);
    Graphics graphics = image.getGraphics();
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        graphics.foreColor = pixel(x, y);
        graphics.setPixel(x, y);
      }
    }
    return image;
  }

  private static int pixel(int x, int y) {
    return 0xFF000000 | (x * 53 + y * 17) << 16 | (x * 19 + y * 47) << 8 | x * 7 + y;
  }

  private static boolean contains(int[] pixels, int expected) {
    for (int pixel : pixels) {
      if (pixel == expected) {
        return true;
      }
    }
    return false;
  }

  private static boolean same(int[] actual, int[] expected) {
    if (actual.length != expected.length) {
      return false;
    }
    for (int i = 0; i < actual.length; i++) {
      if (actual[i] != expected[i]) {
        return false;
      }
    }
    return true;
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
