// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.io.ByteArrayStream;
import totalcross.sys.Vm;
import totalcross.ui.gfx.Graphics;

/** Fixtures and reference operations shared by the native color smoke app. */
final class ImageNativeColorFilterSmokeSupport {
  static final int WIDTH = 4;
  static final int HEIGHT = 1;
  static final int[] SOURCE = { 0xFF102030, 0xFF405060, 0xFF8090A0, 0xFFE0F000 };
  static final int FRAME_STRIP_WIDTH = 4;
  static final int FRAME_STRIP_HEIGHT = 2;
  static final int[] FRAME_STRIP_SOURCE = {
      0xFF102030, 0xFF203040, 0xFF405060, 0xFF506070,
      0xFF8090A0, 0xFFA0B0C0, 0xFFD0E0F0, 0xFFE0F000
  };

  static Image sourceImage() throws Exception {
    Image image = new Image(WIDTH, HEIGHT);
    Graphics graphics = image.getGraphics();
    for (int x = 0; x < SOURCE.length; x++) {
      graphics.foreColor = SOURCE[x];
      graphics.setPixel(x, 0);
    }
    return image;
  }

  static Image alphaImage() throws Exception {
    Image image = new Image(WIDTH, HEIGHT);
    Graphics graphics = image.getGraphics();
    int[] alphas = { 0xFF000000, 0x80000000, 0x00000000, 0xFF000000 };
    for (int x = 0; x < SOURCE.length; x++) {
      graphics.foreColor = SOURCE[x];
      graphics.alpha = alphas[x];
      graphics.setPixel(x, 0);
    }
    graphics.alpha = 0xFF000000;
    return image;
  }

  static Image frameStripImage() throws Exception {
    Image image = new Image(FRAME_STRIP_WIDTH, FRAME_STRIP_HEIGHT);
    Graphics graphics = image.getGraphics();
    for (int i = 0; i < FRAME_STRIP_SOURCE.length; i++) {
      graphics.foreColor = FRAME_STRIP_SOURCE[i];
      graphics.setPixel(i % FRAME_STRIP_WIDTH, i / FRAME_STRIP_WIDTH);
    }
    return image;
  }

  static int[] fadedFramePixels(int frame, int value) {
    int[] result = new int[2 * FRAME_STRIP_HEIGHT];
    for (int y = 0; y < FRAME_STRIP_HEIGHT; y++) {
      for (int x = 0; x < 2; x++) {
        result[y * 2 + x] = fade(FRAME_STRIP_SOURCE[y * FRAME_STRIP_WIDTH + frame * 2 + x], value);
      }
    }
    return result;
  }

  static int[] drawToPixels(Image image) throws Exception {
    Image target = new Image(image.getWidth(), image.getHeight());
    target.getGraphics().drawImage(image, 0, 0, true);
    return target.getPixels();
  }

  static Image applyColorImage(byte[] bytes, int length) throws Exception {
    Image image = new Image(bytes, length);
    image.applyColor(0xFF804020);
    return image;
  }

  static Image applyFadeImage(byte[] bytes, int length) throws Exception {
    Image image = new Image(bytes, length);
    image.applyFade(137);
    return image;
  }

  static Image deferredFrameStripImage() throws Exception {
    Image source = frameStripImage();
    source.setFrameCount(2);
    return source.getScaledInstance(3, 2);
  }

  static byte[] createJpeg(int width, int height) throws Exception {
    Image image = new Image(width, height);
    Graphics graphics = image.getGraphics();
    graphics.foreColor = 0xFF204060;
    graphics.fillRect(0, 0, width, height);
    ByteArrayStream stream = new ByteArrayStream(width * height);
    image.createJpg(stream, 80);
    byte[] jpeg = new byte[stream.getPos()];
    Vm.arrayCopy(stream.getBuffer(), 0, jpeg, 0, jpeg.length);
    return jpeg;
  }

  static int fade(int pixel, int value) {
    int r = ((pixel >> 16) & 0xFF) * value / 255;
    int g = ((pixel >> 8) & 0xFF) * value / 255;
    int b = (pixel & 0xFF) * value / 255;
    return (pixel & 0xFF000000) | r << 16 | g << 8 | b;
  }

  static int applyColor(int pixel, int color) {
    int redMultiplier = (int) (Math.sqrt((((color >> 16) & 0xFF) + 128.0) / 128.0) * 0x10000);
    int greenMultiplier = (int) (Math.sqrt((((color >> 8) & 0xFF) + 128.0) / 128.0) * 0x10000);
    int blueMultiplier = (int) (Math.sqrt(((color & 0xFF) + 128.0) / 128.0) * 0x10000);
    int red = Math.min(255, redMultiplier * ((pixel >> 16) & 0xFF) >> 16);
    int green = Math.min(255, greenMultiplier * ((pixel >> 8) & 0xFF) >> 16);
    int blue = Math.min(255, blueMultiplier * (pixel & 0xFF) >> 16);
    return (pixel & 0xFF000000) | red << 16 | green << 8 | blue;
  }

  static int applyColor2(int pixel, int color) {
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

  static boolean sameArray(int[] first, int[] second) {
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

  static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
