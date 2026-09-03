// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.io.ByteArrayStream;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** Deployed macOS smoke for native color mutations and mixed pipelines. */
public class ImageNativeColorFilterSmokeApp extends MainWindow {
  private static final int WIDTH = 4;
  private static final int HEIGHT = 1;
  private static final int[] SOURCE = { 0xFF102030, 0xFF405060, 0xFF8090A0, 0xFFE0F000 };
  private static final int FRAME_STRIP_WIDTH = 4;
  private static final int FRAME_STRIP_HEIGHT = 2;
  private static final int[] FRAME_STRIP_SOURCE = {
      0xFF102030, 0xFF203040, 0xFF405060, 0xFF506070,
      0xFF8090A0, 0xFFA0B0C0, 0xFFD0E0F0, 0xFFE0F000
  };

  @Override
  public void initUI() {
    boolean nativeBacking = false;
    boolean frameScopedFade = false;
    boolean alpha = false;
    boolean faded = false;
    boolean applyColor2 = false;
    boolean touchUp = false;
    boolean applyColor = false;
    boolean exactColors = false;
    boolean mixedPipeline = false;
    boolean drawAndSave = false;
    String error = "";
    try {
      Image source = sourceImage();
      nativeBacking = source.hasNativeBackingForSmoke();
      require(nativeBacking, "source native backing");

      Image fadedFrame = frameStripImage();
      fadedFrame.setFrameCount(2);
      fadedFrame.setCurrentFrame(1);
      fadedFrame.applyFade(128);
      int[] frameOne = fadedFrame.getPixels();
      fadedFrame.setCurrentFrame(0);
      int[] frameZero = fadedFrame.getPixels();
      frameScopedFade = sameArray(frameOne, fadedFramePixels(1, 128))
          && sameArray(frameZero, fadedFramePixels(0, 255))
          && fadedFrame.hasNativeBackingForSmoke();
      require(frameScopedFade, "frame-scoped fade");

      Image alphaResult = sourceImage().getAlphaInstance(-40);
      require(alphaResult.hasNativeBackingForSmoke(),
          "alpha native result backing");
      int[] alphaPixels = alphaResult.getPixels();
      alpha = alphaPixels[0] == 0xD7102030 && alphaPixels[3] == 0xD7E0F000;
      require(alpha, "alpha mapping");

      Image fadedResult = sourceImage().getFadedInstance(0xFF204060);
      require(fadedResult.hasNativeBackingForSmoke(),
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

      Image color2Image = sourceImage();
      color2Image.applyColor2(0xAA4080C0);
      int[] color2Pixels = color2Image.getPixels();
      applyColor2 = color2Pixels[0] == applyColor2(SOURCE[0], 0xAA4080C0)
          && color2Pixels[3] == applyColor2(SOURCE[3], 0xAA4080C0);
      require(applyColor2, "applyColor2 mapping");

      Image changed = sourceImage();
      changed.changeColors(SOURCE[0], 0xFFAABBCC);
      int[] changedPixels = changed.getPixels();
      Image transparent = sourceImage();
      transparent.setTransparentColor(SOURCE[0] & 0x00FFFFFF);
      int[] transparentPixels = transparent.getPixels();
      Image opaque = sourceImage();
      opaque.setTransparentColor(-1);
      int[] opaquePixels = opaque.getPixels();
      exactColors = changedPixels[0] == 0xFFAABBCC
          && changedPixels[1] == SOURCE[1]
          && transparentPixels[0] == (SOURCE[0] & 0x00FFFFFF)
          && (transparentPixels[0] >>> 24) == 0
          && (transparentPixels[1] >>> 24) == 0xFF
          && opaquePixels[0] == SOURCE[0];
      require(exactColors, "exact color mutations");

      Image mixed = sourceImage().getClippedInstance(0, 0, WIDTH, HEIGHT)
          .getSmoothScaledInstance(8, 2);
      mixed.applyFade(128);
      mixed.changeColors(fade(SOURCE[0], 128), 0xFF010203);
      Image mixedResult = mixed.getRotatedScaledInstance(100, 90, 0).getAlphaInstance(-20);
      int[] mixedPixels = mixedResult.getPixels();
      ByteArrayStream mixedEncoded = new ByteArrayStream(512);
      mixedResult.createPng(mixedEncoded);
      Image mixedSaved = new Image(mixedEncoded.getBuffer(), mixedEncoded.getPos());
      Image mixedDestination = new Image(mixedResult.getWidth(), mixedResult.getHeight());
      mixedDestination.getGraphics().drawImage(mixedResult, 0, 0);
      mixedPipeline = sameArray(mixedPixels, mixedSaved.getPixels())
          && sameArray(mixedPixels, mixedDestination.getPixels());
      require(mixedPipeline, "mixed native pipeline");

      Image chained = sourceImage();
      chained.applyColor(0xFF804020);
      chained.applyColor2(0xAA4080C0);
      chained.setTransparentColor(0x010203);
      ByteArrayStream chainedEncoded = new ByteArrayStream(512);
      chained.createPng(chainedEncoded);
      Image chainedSaved = new Image(chainedEncoded.getBuffer(), chainedEncoded.getPos());
      Image chainedDestination = new Image(WIDTH, HEIGHT);
      chainedDestination.getGraphics().drawImage(chained, 0, 0);
      drawAndSave = sameArray(chained.getPixels(), chainedSaved.getPixels())
          && sameArray(chained.getPixels(), chainedDestination.getPixels());

      Image touched = sourceImage().getTouchedUpInstance((byte) 20, (byte) -10);
      require(touched.hasNativeBackingForSmoke(),
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
      drawAndSave = drawAndSave && sameArray(touchedPixels, saved.getPixels())
          && sameArray(touchedPixels, destination.getPixels());
      require(drawAndSave, "direct draw and saved output");
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean overallPass = nativeBacking && frameScopedFade && alpha && faded && applyColor && applyColor2
        && touchUp && exactColors && mixedPipeline && drawAndSave;
    System.out.println("fixture=ImageNativeColorFilterSmokeApp,nativeBacking=" + nativeBacking
        + ",frameScopedFade=" + frameScopedFade + ",alpha=" + alpha + ",faded=" + faded
        + ",applyColor=" + applyColor + ",applyColor2=" + applyColor2
        + ",touchUp=" + touchUp + ",exactColors=" + exactColors
        + ",mixedPipeline=" + mixedPipeline + ",drawAndSave=" + drawAndSave
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

  private static Image frameStripImage() throws Exception {
    Image image = new Image(FRAME_STRIP_WIDTH, FRAME_STRIP_HEIGHT);
    Graphics graphics = image.getGraphics();
    for (int i = 0; i < FRAME_STRIP_SOURCE.length; i++) {
      graphics.foreColor = FRAME_STRIP_SOURCE[i];
      graphics.setPixel(i % FRAME_STRIP_WIDTH, i / FRAME_STRIP_WIDTH);
    }
    return image;
  }

  private static int[] fadedFramePixels(int frame, int value) {
    int[] result = new int[2 * FRAME_STRIP_HEIGHT];
    for (int y = 0; y < FRAME_STRIP_HEIGHT; y++) {
      for (int x = 0; x < 2; x++) {
        result[y * 2 + x] = fade(FRAME_STRIP_SOURCE[y * FRAME_STRIP_WIDTH + frame * 2 + x], value);
      }
    }
    return result;
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

  private static int applyColor2(int pixel, int color) {
    int brightest = 0;
    int brightestPixel = 0;
    for (int sourcePixel : SOURCE) {
      int brightness = (3 * ((sourcePixel >> 16) & 0xFF) + 4 * ((sourcePixel >> 8) & 0xFF)
          + (sourcePixel & 0xFF)) >> 3;
      if (brightness > brightest) {
        brightest = brightness;
        brightestPixel = sourcePixel;
      }
    }
    int redScale = ((brightestPixel >> 16) & 0xFF) == 0 ? 255 : (brightestPixel >> 16) & 0xFF;
    int greenScale = ((brightestPixel >> 8) & 0xFF) == 0 ? 255 : (brightestPixel >> 8) & 0xFF;
    int blueScale = (brightestPixel & 0xFF) == 0 ? 255 : brightestPixel & 0xFF;
    int scale = Math.max(redScale, Math.max(greenScale, blueScale));
    int red = Math.min(255, ((pixel >> 16) & 0xFF) * ((color >> 16) & 0xFF) / redScale);
    int green = Math.min(255, ((pixel >> 8) & 0xFF) * ((color >> 8) & 0xFF) / greenScale);
    int blue = Math.min(255, (pixel & 0xFF) * (color & 0xFF) / blueScale);
    int alpha = (color >>> 24) == 0xAA
        ? Math.min(255, Math.max((pixel >> 16) & 0xFF, Math.max((pixel >> 8) & 0xFF, pixel & 0xFF)) * 255 / scale)
        : (pixel >>> 24);
    return alpha << 24 | red << 16 | green << 8 | blue;
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
