// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

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
    return width() > 0 && height > 0 && pixels != null
        && (frameCount <= 1 || pixelsOfAllFrames != null);
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
      return new RasterImageBacking(width, height, frameCount, widthOfAllFrames,
          pixels == null ? null : pixels.clone(),
          pixelsOfAllFrames == null ? null : pixelsOfAllFrames.clone());
    } catch (OutOfMemoryError oome) {
      throw new TransientImageMaterializationException(oome);
    }
  }
}
