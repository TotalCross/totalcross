// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;

/** Opaque native backing used by deployed Skia images. */
final class NativeImageBacking extends ImageBacking {
  private long nativeHandle;
  private final int width;
  private final int height;

  private NativeImageBacking(long nativeHandle, int width, int height) {
    if (nativeHandle == 0 || width <= 0 || height <= 0) {
      throw new IllegalArgumentException("Invalid native image backing");
    }
    this.nativeHandle = nativeHandle;
    this.width = width;
    this.height = height;
  }

  static NativeImageBacking fromHandle(long nativeHandle, int width, int height) {
    return new NativeImageBacking(nativeHandle, width, height);
  }

  static NativeImageBacking materializeGeometry(ImageGeometryPlan plan) throws ImageException {
    if (plan == null) {
      throw new ImageException("Invalid native image geometry plan.");
    }
    long handle = materializeGeometryNative(plan);
    if (handle == 0) {
      throw new ImageException("Could not materialize native image geometry.");
    }
    double physicalWidth = Math.ceil(plan.outputWidth * plan.outputContentScale);
    double physicalHeight = Math.ceil(plan.outputHeight * plan.outputContentScale);
    long fullWidth = (long) physicalWidth * Math.max(1, plan.outputFrameCount);
    if (!Double.isFinite(physicalWidth) || !Double.isFinite(physicalHeight) || physicalWidth <= 0
        || physicalHeight <= 0 || fullWidth > Integer.MAX_VALUE) {
      releaseNativeHandle(handle);
      throw new ImageException("Native image geometry dimensions are too large.");
    }
    return new NativeImageBacking(handle, (int) fullWidth, (int) physicalHeight);
  }

  static NativeImageBacking createEmpty(int width, int height) throws ImageException {
    if (width <= 0 || height <= 0) {
      throw new ImageException("Image dimensions must be positive.");
    }
    long handle = createEmptyNative(width, height);
    if (handle == 0) {
      throw new ImageException("Could not create native image backing.");
    }
    return new NativeImageBacking(handle, width, height);
  }

  static NativeImageBacking createFromArgbPixels(int[] pixels, int width, int height) throws ImageException {
    if (pixels == null || width <= 0 || height <= 0 || (long) width * height > pixels.length) {
      throw new ImageException("Invalid native image pixels.");
    }
    long handle = createFromArgbPixelsNative(pixels, width, height);
    if (handle == 0) {
      throw new ImageException("Could not create native image backing.");
    }
    return new NativeImageBacking(handle, width, height);
  }

  static boolean isAvailable() {
    return isAvailableNative();
  }

  @Override
  boolean isNative() {
    return true;
  }

  @Override
  int width() {
    return width;
  }

  @Override
  int height() {
    return height;
  }

  @Override
  boolean isValid() {
    return nativeHandle != 0 && width > 0 && height > 0;
  }

  @Override
  int[] readVisiblePixels(int visibleWidth, int outputHeight, int frame) {
    if (!isValid() || visibleWidth <= 0 || outputHeight != height
        || (long) visibleWidth * outputHeight > Integer.MAX_VALUE
        || (long) visibleWidth * Math.max(0, frame) + visibleWidth > width) {
      throw new IllegalStateException("Invalid native image backing read");
    }
    Image.recordBackingReadbackForTest();
    int[] output = new int[visibleWidth * outputHeight];
    if (!readPixels(output, 0, visibleWidth * Math.max(0, frame), 0, visibleWidth, outputHeight)) {
      throw new IllegalStateException("Could not read native image backing");
    }
    return output;
  }

  @Override
  int[] readStoragePixels() {
    if (!isValid() || (long) width * height > Integer.MAX_VALUE) {
      throw new IllegalStateException("Invalid native image backing read");
    }
    int[] output = new int[width * height];
    if (!readPixels(output, 0, 0, 0, width, height)) {
      throw new IllegalStateException("Could not read native image backing");
    }
    return output;
  }

  @Override
  boolean readRgbaRow(byte[] output, int y) {
    return isValid() && output != null && y >= 0 && y < height && output.length >= width * 4
        && readRgbaRowNative(output, y, width);
  }

  long nativeHandleForBridge() {
    return nativeHandle;
  }

  boolean makeMutable() {
    if (nativeHandle == 0) {
      throw new IllegalStateException("Native image backing has been released");
    }
    return makeMutableNative();
  }

  boolean readPixels(int[] output, int offset, int x, int y, int width, int height) {
    if (nativeHandle == 0 || output == null || offset < 0 || width < 0 || height < 0
        || (long) offset + (long) width * height > output.length) {
      return false;
    }
    return readPixelsNative(output, offset, x, y, width, height);
  }

  NativeImageBacking scale(int outputWidth, int outputHeight, boolean smooth) throws ImageException {
    if (nativeHandle == 0 || outputWidth <= 0 || outputHeight <= 0) {
      throw new ImageException("Invalid native image scale.");
    }
    long scaled = scaleNative(outputWidth, outputHeight, smooth);
    if (scaled == 0) {
      throw new ImageException("Could not scale native image backing.");
    }
    return new NativeImageBacking(scaled, outputWidth, outputHeight);
  }

  @Override
  ImageBacking snapshot() throws ImageException {
    long handle = nativeHandle;
    if (handle == 0) {
      throw new IllegalStateException("Native image backing has been released");
    }
    long snapshot = snapshotNative();
    if (snapshot == 0) {
      throw new IllegalStateException("Could not snapshot native image backing");
    }
    if (snapshot < 0) {
      throw new TransientImageMaterializationException("Could not allocate native image snapshot");
    }
    return new NativeImageBacking(snapshot, width, height);
  }

  /** Test-only hook for exercising retryable native backing snapshot allocation failures. */
  static void failNextSnapshotForTest() {
    failNextSnapshotNative();
  }

  void release() {
    long handle = nativeHandle;
    nativeHandle = 0;
    if (handle != 0) {
      releaseNativeHandle(handle);
    }
  }

  @Override
  protected void finalize() {
    release();
  }

  @ReplacedByNativeOnDeploy
  private static long createEmptyNative(int width, int height) {
    return 0;
  }

  @ReplacedByNativeOnDeploy
  private static boolean isAvailableNative() {
    return false;
  }

  @ReplacedByNativeOnDeploy
  private static long createFromArgbPixelsNative(int[] pixels, int width, int height) {
    return 0;
  }

  @ReplacedByNativeOnDeploy
  private long snapshotNative() {
    return 0;
  }

  @ReplacedByNativeOnDeploy
  private static void failNextSnapshotNative() {
  }

  @ReplacedByNativeOnDeploy
  private boolean makeMutableNative() {
    return false;
  }

  @ReplacedByNativeOnDeploy
  private boolean readPixelsNative(int[] output, int offset, int x, int y, int width, int height) {
    return false;
  }

  @ReplacedByNativeOnDeploy
  private boolean readRgbaRowNative(byte[] output, int y, int width) {
    return false;
  }

  @ReplacedByNativeOnDeploy
  private long scaleNative(int width, int height, boolean smooth) {
    return 0;
  }

  @ReplacedByNativeOnDeploy
  private static long materializeGeometryNative(ImageGeometryPlan plan) {
    return 0;
  }

  @ReplacedByNativeOnDeploy
  private static void releaseNativeHandle(long nativeHandle) {
  }
}
