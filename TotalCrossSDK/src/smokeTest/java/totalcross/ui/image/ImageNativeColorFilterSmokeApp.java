// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.io.ByteArrayStream;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** Deployed macOS smoke for native fade, alpha, and touch-up operations. */
public class ImageNativeColorFilterSmokeApp extends MainWindow {
  private static final int WIDTH = 4;
  private static final int HEIGHT = 1;
  private static final int[] SOURCE = { 0xFF102030, 0xFF405060, 0xFF8090A0, 0xFFE0F000 };

  @Override
  public void initUI() {
    boolean nativeBacking = false;
    boolean frameScopedFade = false;
    boolean alpha = false;
    boolean faded = false;
    boolean touchUp = false;
    boolean applyColor = false;
    boolean drawAndSave = false;
    String error = "";
    try {
      Image source = sourceImage();
      nativeBacking = source.hasNativeBackingForSmoke();
      require(nativeBacking, "source native backing");

      Image fadedFrame = sourceImage();
      fadedFrame.setFrameCount(2);
      fadedFrame.setCurrentFrame(1);
      fadedFrame.applyFade(128);
      int[] frameOne = fadedFrame.getPixels();
      fadedFrame.setCurrentFrame(0);
      int[] frameZero = fadedFrame.getPixels();
      frameScopedFade = frameOne[0] == fade(SOURCE[2], 128)
          && frameZero[0] == SOURCE[0] && fadedFrame.hasNativeBackingForSmoke();
      require(frameScopedFade, "frame-scoped fade");

      Image alphaResult = sourceImage().getAlphaInstance(-40);
      require(alphaResult.pixels == null && alphaResult.hasNativeBackingForSmoke(),
          "alpha native result backing");
      int[] alphaPixels = alphaResult.getPixels();
      alpha = alphaPixels[0] == 0xD7102030 && alphaPixels[3] == 0xD7E0F000;
      require(alpha, "alpha mapping");

      Image fadedResult = sourceImage().getFadedInstance(0xFF204060);
      require(fadedResult.pixels == null && fadedResult.hasNativeBackingForSmoke(),
          "fade native result backing");
      int[] fadedPixels = fadedResult.getPixels();
      faded = fadedPixels[0] == 0xFF183048 && fadedPixels[3] == 0xFF809830;
      require(faded, "fade mapping");

      Image colorImage = sourceImage();
      colorImage.applyColor(0xFF804020);
      int[] colorPixels = colorImage.getPixels();
      applyColor = colorPixels[0] == applyColor(SOURCE[0], 0xFF804020)
          && (colorPixels[0] & 0xFF000000) == (SOURCE[0] & 0xFF000000);
      require(applyColor, "applyColor mapping");

      Image touched = sourceImage().getTouchedUpInstance((byte) 20, (byte) -10);
      require(touched.pixels == null && touched.hasNativeBackingForSmoke(),
          "touch-up native result backing");
      int[] touchedPixels = touched.getPixels();
      require(touchedPixels.length == SOURCE.length, "touch-up output dimensions");
      touchUp = true;

      ByteArrayStream encoded = new ByteArrayStream(512);
      touched.createPng(encoded);
      Image saved = new Image(encoded.getBuffer(), encoded.getPos());
      Image destination = new Image(WIDTH, HEIGHT);
      Graphics destinationGraphics = destination.getGraphics();
      destinationGraphics.drawImage(touched, 0, 0);
      drawAndSave = sameArray(touchedPixels, saved.getPixels())
          && sameArray(touchedPixels, destination.getPixels());
      require(drawAndSave, "direct draw and saved output");
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean overallPass = nativeBacking && frameScopedFade && alpha && faded && applyColor && touchUp && drawAndSave;
    System.out.println("fixture=ImageNativeColorFilterSmokeApp,nativeBacking=" + nativeBacking
        + ",frameScopedFade=" + frameScopedFade + ",alpha=" + alpha + ",faded=" + faded
        + ",applyColor=" + applyColor + ",touchUp=" + touchUp + ",drawAndSave=" + drawAndSave
        + ",overallPass=" + overallPass
        + (error.length() == 0 ? "" : ",error=" + error));
    exit(overallPass ? 0 : 1);
  }

  private static Image sourceImage() throws Exception {
    Image image = new Image(WIDTH, HEIGHT);
    Graphics graphics = image.getGraphics();
    for (int x = 0; x < SOURCE.length; x++) {
      graphics.foreColor = SOURCE[x];
      graphics.setPixel(x, 0);
    }
    return image;
  }

  private static int fade(int pixel, int value) {
    int r = ((pixel >> 16) & 0xFF) * value / 255;
    int g = ((pixel >> 8) & 0xFF) * value / 255;
    int b = (pixel & 0xFF) * value / 255;
    return (pixel & 0xFF000000) | r << 16 | g << 8 | b;
  }

  private static int applyColor(int pixel, int color) {
    int redMultiplier = (int) (Math.sqrt((((color >> 16) & 0xFF) + 128.0) / 128.0) * 0x10000);
    int greenMultiplier = (int) (Math.sqrt((((color >> 8) & 0xFF) + 128.0) / 128.0) * 0x10000);
    int blueMultiplier = (int) (Math.sqrt(((color & 0xFF) + 128.0) / 128.0) * 0x10000);
    int red = Math.min(255, redMultiplier * ((pixel >> 16) & 0xFF) >> 16);
    int green = Math.min(255, greenMultiplier * ((pixel >> 8) & 0xFF) >> 16);
    int blue = Math.min(255, blueMultiplier * (pixel & 0xFF) >> 16);
    return (pixel & 0xFF000000) | red << 16 | green << 8 | blue;
  }

  private static boolean sameArray(int[] first, int[] second) {
    if (first == null || second == null || first.length != second.length) {
      return false;
    }
    for (int i = 0; i < first.length; i++) {
      if (first[i] != second[i]) {
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
