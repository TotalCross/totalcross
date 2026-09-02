// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** Deployed macOS smoke for direct native execution of deferred image geometry. */
public class ImageNativeGeometrySmokeApp extends MainWindow {
  @Override
  public void initUI() {
    boolean nearest = false;
    boolean crop = false;
    boolean smooth = false;
    boolean chained = false;
    boolean rotate = false;
    boolean frames = false;
    String error = "";
    try {
      Image source = patterned(4, 3);

      Image nearestImage = source.getScaledInstance(2, 2);
      Image nearestTarget = new Image(2, 2);
      nearestTarget.getGraphics().drawImage(nearestImage, 0, 0, true);
      int[] nearestActual = nearestTarget.getPixels();
      int[] nearestExpected = new int[] {
          pixel(0, 0), pixel(2, 0), pixel(0, 1), pixel(2, 1)
      };
      nearest = same(nearestActual, nearestExpected);
      require(nearest, "nearest scale " + mismatch(nearestActual, nearestExpected)
          + ",actual=" + pixels(nearestActual) + ",expected=" + pixels(nearestExpected));

      Image cropImage = source.getClippedInstance(1, 1, 2, 2);
      Image cropTarget = new Image(2, 2);
      cropTarget.getGraphics().drawImage(cropImage, 0, 0, true);
      int[] cropActual = cropTarget.getPixels();
      int[] cropExpected = new int[] {
          pixel(1, 1), pixel(2, 1), pixel(1, 2), pixel(2, 2)
      };
      crop = same(cropActual, cropExpected);
      require(crop, "crop " + mismatch(cropActual, cropExpected));

      Image smoothImage = source.getSmoothScaledInstance(3, 2);
      Image smoothTarget = new Image(3, 2);
      smoothTarget.getGraphics().drawImage(smoothImage, 0, 0, true);
      int[] smoothActual = smoothTarget.getPixels();
      int[] smoothExpected = smoothImage.getPixels();
      smooth = same(smoothActual, smoothExpected);
      require(smooth, "smooth scale " + mismatch(smoothActual, smoothExpected));

      Image scaledThenCrop = source.getScaledInstance(2, 2).getClippedInstance(1, 0, 1, 2);
      Image chainedTarget = new Image(1, 2);
      chainedTarget.getGraphics().drawImage(scaledThenCrop, 0, 0, true);
      int[] chainedActual = chainedTarget.getPixels();
      int[] chainedExpected = new int[] { pixel(2, 0), pixel(2, 1) };
      chained = same(chainedActual, chainedExpected);
      require(chained, "scale then crop " + mismatch(chainedActual, chainedExpected));

      Image rotatedImage = source.getRotatedScaledInstance(100, 90, 0xFF102030);
      Image rotatedTarget = new Image(rotatedImage.getWidth(), rotatedImage.getHeight());
      rotatedTarget.getGraphics().drawImage(rotatedImage, 0, 0, true);
      int[] rotatedActual = rotatedTarget.getPixels();
      int[] rotatedExpected = rotatedImage.getPixels();
      rotate = same(rotatedActual, rotatedExpected)
          && rotatedImage.getPixelWidth() > 0 && rotatedImage.getPixelHeight() > 0;
      require(rotate, "rotate scale " + mismatch(rotatedActual, rotatedExpected));

      Image strip = patterned(4, 2);
      strip.setFrameCount(2);
      Image selected = strip.getFrameInstance(1);
      Image frameTarget = new Image(2, 2);
      frameTarget.getGraphics().drawImage(selected, 0, 0, true);
      int[] frameActual = frameTarget.getPixels();
      int[] frameExpected = new int[] {
          pixel(2, 0), pixel(3, 0), pixel(2, 1), pixel(3, 1)
      };
      frames = same(frameActual, frameExpected);
      require(frames, "frame selection " + mismatch(frameActual, frameExpected));
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean overallPass = nearest && crop && smooth && chained && rotate && frames;
    System.out.println("fixture=ImageNativeGeometrySmokeApp,nearest=" + nearest + ",crop=" + crop
        + ",smooth=" + smooth + ",chained=" + chained + ",rotate=" + rotate + ",frames=" + frames + ",overallPass="
        + overallPass + (error.length() == 0 ? "" : ",error=" + error));
    exit(overallPass ? 0 : 1);
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
    return 0xFF000000 | (x * 37 + y * 11) << 16 | (x * 13 + y * 43) << 8 | x * 5 + y;
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

  private static String mismatch(int[] actual, int[] expected) {
    for (int i = 0; i < actual.length && i < expected.length; i++) {
      if (actual[i] != expected[i]) {
        return "index=" + i + ",actual=0x" + Integer.toHexString(actual[i])
            + ",expected=0x" + Integer.toHexString(expected[i]);
      }
    }
    return "none";
  }

  private static String pixels(int[] values) {
    String result = "[";
    for (int i = 0; i < values.length; i++) {
      if (i != 0) {
        result += ",";
      }
      result += "0x" + Integer.toHexString(values[i]);
    }
    return result + "]";
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
