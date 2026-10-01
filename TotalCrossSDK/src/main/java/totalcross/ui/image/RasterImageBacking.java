// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.ui.gfx.Color;

/** Java raster backing retained by Java SE and compatibility paths. */
final class RasterImageBacking extends ImageBacking {
  private final int width;
  private final int height;
  private final int frameCount;
  private final int widthOfAllFrames;
  private final int[] pixels;
  private final int[] pixelsOfAllFrames;

  RasterImageBacking(int width, int height, int frameCount, int widthOfAllFrames,
      int[] pixels, int[] pixelsOfAllFrames) {
    this.width = width;
    this.height = height;
    this.frameCount = frameCount;
    this.widthOfAllFrames = widthOfAllFrames;
    this.pixels = pixels;
    this.pixelsOfAllFrames = pixelsOfAllFrames;
  }

  @Override
  boolean isNative() {
    return false;
  }

  @Override
  int width() {
    return frameCount > 1 ? widthOfAllFrames : width;
  }

  @Override
  int height() {
    return height;
  }

  @Override
  boolean isValid() {
    int storageWidth = width();
    long visiblePixelCount = (long) width * height;
    long storagePixelCount = (long) storageWidth * height;
    return frameCount > 0 && width >= 0 && height > 0 && storageWidth > 0 && pixels != null
        && visiblePixelCount <= pixels.length
        && (frameCount == 1 ? width > 0
            : (long) width * frameCount <= storageWidth && pixelsOfAllFrames != null
                && storagePixelCount <= pixelsOfAllFrames.length);
  }

  @Override
  int[] readVisiblePixels(int visibleWidth, int outputHeight, int frame) {
    if (!isValid() || visibleWidth != width || outputHeight != height) {
      throw new IllegalStateException("Invalid raster image backing read");
    }
    return pixels;
  }

  @Override
  int[] readStoragePixels() {
    if (!isValid()) {
      throw new IllegalStateException("Invalid raster image backing read");
    }
    return frameCount > 1 ? pixelsOfAllFrames : pixels;
  }

  @Override
  boolean readRgbaRow(byte[] output, int y) {
    int storageWidth = width();
    if (!isValid() || output == null || y < 0 || y >= height
        || (long) output.length < (long) storageWidth * 4) {
      return false;
    }
    int[] source = readStoragePixels();
    int sourceIndex = y * storageWidth;
    for (int x = 0, pixel = sourceIndex; x < storageWidth; x++, pixel++) {
      int value = source[pixel];
      output[x * 4] = (byte) (value >> 16);
      output[x * 4 + 1] = (byte) (value >> 8);
      output[x * 4 + 2] = (byte) value;
      output[x * 4 + 3] = (byte) (value >>> 24);
    }
    return true;
  }

  @Override
  boolean readPixels(int[] output, int offset, int x, int y, int width, int height) {
    int storageWidth = width();
    long pixelCount = (long) width * height;
    if (!isValid() || output == null || offset < 0 || x < 0 || y < 0 || width < 0 || height < 0
        || x > storageWidth - width || y > this.height - height
        || pixelCount > Integer.MAX_VALUE || (long) offset + pixelCount > output.length) {
      return false;
    }
    int[] source = readStoragePixels();
    for (int row = 0; row < height; row++) {
      System.arraycopy(source, (y + row) * storageWidth + x, output, offset + row * width, width);
    }
    return true;
  }

  boolean writePixels(int[] input, int offset, int x, int y, int width, int height,
      int visibleWidth, int visibleHeight) {
    long pixelCount = (long) width * height;
    if (!isValid() || input == null || offset < 0 || x < 0 || y < 0 || width < 0 || height < 0
        || visibleWidth != this.width || visibleHeight != this.height
        || x > visibleWidth - width || y > visibleHeight - height
        || pixelCount > Integer.MAX_VALUE || (long) offset + pixelCount > input.length) {
      return false;
    }
    for (int row = 0; row < height; row++) {
      System.arraycopy(input, offset + row * width, pixels, (y + row) * visibleWidth + x, width);
    }
    return true;
  }

  int applyColor2(int color) {
    int redTarget = Color.getRed(color);
    int greenTarget = Color.getGreen(color);
    int blueTarget = Color.getBlue(color);
    boolean changeAlpha = (color & 0xFF000000) == 0xAA000000;
    int[] storage = readStoragePixels();
    int highestBrightness = 0;
    int highestPixel = 0;
    for (int pixel : storage) {
      if ((pixel & 0xFF000000) == 0xFF000000) {
        int rgb = pixel & 0x00FFFFFF;
        int brightness = Color.getBrightness(rgb);
        if (brightness > highestBrightness) {
          highestBrightness = brightness;
          highestPixel = rgb;
        }
      }
    }

    int highestRed = (highestPixel >> 16) & 0xFF;
    int highestGreen = (highestPixel >> 8) & 0xFF;
    int highestBlue = highestPixel & 0xFF;
    if (highestRed == 0) highestRed = 255;
    if (highestGreen == 0) highestGreen = 255;
    if (highestBlue == 0) highestBlue = 255;
    int highestChannel = Math.max(highestRed, Math.max(highestGreen, highestBlue));

    for (int index = 0; index < storage.length; index++) {
      int pixel = storage[index];
      if ((pixel & 0xFF000000) == 0) {
        continue;
      }
      int red = ((pixel >> 16) & 0xFF) * redTarget / highestRed;
      int green = ((pixel >> 8) & 0xFF) * greenTarget / highestGreen;
      int blue = (pixel & 0xFF) * blueTarget / highestBlue;
      if (red > 255) red = 255;
      if (green > 255) green = 255;
      if (blue > 255) blue = 255;
      if (changeAlpha) {
        int alpha = Math.max((pixel >> 16) & 0xFF, (pixel >> 8) & 0xFF);
        alpha = Math.max(alpha, pixel & 0xFF) * 255 / highestChannel;
        if (alpha > 255) alpha = 255;
        storage[index] = (alpha << 24) | (red << 16) | (green << 8) | blue;
      } else {
        storage[index] = (pixel & 0xFF000000) | (red << 16) | (green << 8) | blue;
      }
    }
    return changeAlpha ? OPACITY_UNKNOWN : opacityState();
  }

  int frameCount() {
    return frameCount;
  }

  int widthOfAllFrames() {
    return widthOfAllFrames;
  }

  int[] pixels() {
    return pixels;
  }

  int[] pixelsOfAllFrames() {
    return pixelsOfAllFrames;
  }

  @Override
  ImageBacking snapshot() throws ImageException {
    try {
      RasterImageBacking snapshot = new RasterImageBacking(width, height, frameCount, widthOfAllFrames,
          pixels == null ? null : pixels.clone(),
          pixelsOfAllFrames == null ? null : pixelsOfAllFrames.clone());
      copyStateTo(snapshot);
      return snapshot;
    } catch (OutOfMemoryError oome) {
      throw new TransientImageMaterializationException(oome);
    }
  }
}
