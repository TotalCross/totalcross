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

  long nativeHandleForBridge() {
    return nativeHandle;
  }

  @Override
  ImageBacking snapshot() {
    long handle = nativeHandle;
    if (handle == 0) {
      throw new IllegalStateException("Native image backing has been released");
    }
    long snapshot = snapshotNative(handle);
    if (snapshot == 0) {
      throw new IllegalStateException("Could not snapshot native image backing");
    }
    return new NativeImageBacking(snapshot, width, height);
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
  private static long snapshotNative(long nativeHandle) {
    return 0;
  }

  @ReplacedByNativeOnDeploy
  private static void releaseNativeHandle(long nativeHandle) {
  }
}
