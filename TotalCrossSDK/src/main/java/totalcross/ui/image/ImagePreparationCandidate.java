// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

/** Detached decoded image ownership transferred to the UI thread on adoption. */
public final class ImagePreparationCandidate {
  private long nativeHandle;
  private int width;
  private int height;
  private int denominator;
  private ImageBacking backing;

  private ImagePreparationCandidate() {
  }

  public static ImagePreparationCandidate fromBacking(ImageBacking backing, int width, int height,
      int denominator) {
    if (backing == null || !backing.isValid() || width <= 0 || height <= 0
        || (denominator != 1 && denominator != 2 && denominator != 4 && denominator != 8)) {
      throw new IllegalArgumentException("Invalid image preparation candidate");
    }
    ImagePreparationCandidate candidate = new ImagePreparationCandidate();
    candidate.backing = backing;
    candidate.width = width;
    candidate.height = height;
    candidate.denominator = denominator;
    return candidate;
  }

  public static ImagePreparationCandidate fromNativeHandle(long nativeHandle, int width, int height,
      int denominator) {
    if (nativeHandle == 0 || width <= 0 || height <= 0
        || (denominator != 1 && denominator != 2 && denominator != 4 && denominator != 8)) {
      throw new IllegalArgumentException("Invalid native image preparation candidate");
    }
    ImagePreparationCandidate candidate = new ImagePreparationCandidate();
    candidate.nativeHandle = nativeHandle;
    candidate.width = width;
    candidate.height = height;
    candidate.denominator = denominator;
    return candidate;
  }

  public ImageBacking takeBackingForAdoption() throws ImageException {
    if (backing != null) {
      ImageBacking result = backing;
      backing = null;
      return result;
    }
    long handle = nativeHandle;
    nativeHandle = 0;
    if (handle == 0) {
      throw new ImageException("Image preparation candidate has no backing");
    }
    long adopted = NativeImageBacking.adoptDetachedNative(handle);
    if (adopted == 0) {
      throw new ImageException("Could not adopt image preparation candidate");
    }
    return NativeImageBacking.fromHandle(adopted, width, height);
  }

  public int width() {
    return width;
  }

  public int height() {
    return height;
  }

  public int denominator() {
    return denominator;
  }

  public void release() {
    ImageBacking current = backing;
    backing = null;
    if (current instanceof NativeImageBacking) {
      ((NativeImageBacking) current).release();
    }
    long handle = nativeHandle;
    nativeHandle = 0;
    if (handle != 0) {
      NativeImageBacking.releaseDetachedNative(handle);
    }
  }

  @Override
  protected void finalize() {
    release();
  }
}
