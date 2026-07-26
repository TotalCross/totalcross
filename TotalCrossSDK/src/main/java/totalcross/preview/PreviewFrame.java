// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.preview;

import java.util.Arrays;

/** Immutable, runtime-neutral snapshot of one rendered preview frame. */
public final class PreviewFrame {
  public enum PixelFormat {
    ARGB_8888
  }

  private final int width;
  private final int height;
  private final int stride;
  private final double density;
  private final PixelFormat pixelFormat;
  private final int[] pixels;

  public PreviewFrame(int width, int height, int stride, double density, PixelFormat pixelFormat, int[] pixels) {
    if (width < 0 || height < 0 || stride < width) {
      throw new IllegalArgumentException("invalid frame dimensions");
    }
    if (pixelFormat == null || pixels == null || pixels.length < stride * height) {
      throw new IllegalArgumentException("invalid frame payload");
    }
    this.width = width;
    this.height = height;
    this.stride = stride;
    this.density = density;
    this.pixelFormat = pixelFormat;
    this.pixels = Arrays.copyOf(pixels, stride * height);
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public int getStride() {
    return stride;
  }

  public double getDensity() {
    return density;
  }

  public PixelFormat getPixelFormat() {
    return pixelFormat;
  }

  /** Returns a defensive copy so callers cannot mutate a retained frame. */
  public int[] copyPixels() {
    return Arrays.copyOf(pixels, pixels.length);
  }
}
