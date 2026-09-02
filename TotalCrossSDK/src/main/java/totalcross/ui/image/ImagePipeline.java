// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

/** Immutable linked representation of an Image's deferred source and transforms. */
final class ImagePipeline {
  static final int SCALE = 0;
  static final int SMOOTH_SCALE = 1;
  static final int ROTATE_SCALE = 2;
  static final int TOUCH_UP = 3;
  static final int FADE = 4;
  static final int ALPHA = 5;

  private final ImageSource root;
  private final ImagePipeline previous;
  private final int operationType;
  private final int parameter1;
  private final int parameter2;
  private final int parameter3;
  private final int parameter4;
  private final int width;
  private final int height;
  private final int logicalWidth;
  private final int logicalHeight;
  private final int frameCount;
  private final int widthOfAllFrames;

  // The cache belongs to this pipeline leaf. It is deliberately not shared by
  // roots or images so encoded sources remain authoritative after eviction.
  private long cachedScale1Bits;
  private long cachedScale2Bits;
  private Image cachedVariant1;
  private Image cachedVariant2;
  private long cacheUseCounter;
  private long cachedUse1;
  private long cachedUse2;

  ImagePipeline(ImageSource root) {
    if (root == null) {
      throw new NullPointerException("root");
    }
    this.root = root;
    previous = null;
    operationType = -1;
    parameter1 = parameter2 = parameter3 = parameter4 = 0;
    width = root.width();
    height = root.height();
    logicalWidth = root.logicalWidth();
    logicalHeight = root.logicalHeight();
    frameCount = root.frameCount();
    widthOfAllFrames = root.widthOfAllFrames();
  }

  private ImagePipeline(ImagePipeline previous, int operationType, int parameter1, int parameter2,
      int parameter3, int parameter4, int width, int height, int logicalWidth, int logicalHeight,
      int frameCount, int widthOfAllFrames) {
    this.root = previous.root;
    this.previous = previous;
    this.operationType = operationType;
    this.parameter1 = parameter1;
    this.parameter2 = parameter2;
    this.parameter3 = parameter3;
    this.parameter4 = parameter4;
    this.width = width;
    this.height = height;
    this.logicalWidth = logicalWidth;
    this.logicalHeight = logicalHeight;
    this.frameCount = frameCount;
    this.widthOfAllFrames = widthOfAllFrames;
  }

  ImageSource root() {
    return root;
  }

  ImagePipeline previous() {
    return previous;
  }

  int operationType() {
    return operationType;
  }

  int parameter1() {
    return parameter1;
  }

  int parameter2() {
    return parameter2;
  }

  int parameter3() {
    return parameter3;
  }

  int parameter4() {
    return parameter4;
  }

  int width() {
    return width;
  }

  int height() {
    return height;
  }

  int logicalWidth() {
    return logicalWidth;
  }

  int logicalHeight() {
    return logicalHeight;
  }

  int frameCount() {
    return frameCount;
  }

  int widthOfAllFrames() {
    return widthOfAllFrames;
  }

  ImagePipeline append(int operationType, int parameter1, int parameter2, int parameter3, int parameter4,
      int width, int height, int logicalWidth, int logicalHeight, int frameCount, int widthOfAllFrames) {
    return new ImagePipeline(this, operationType, parameter1, parameter2, parameter3, parameter4,
        width, height, logicalWidth, logicalHeight, frameCount, widthOfAllFrames);
  }

  boolean hasGeometricNode() {
    for (ImagePipeline node = this; node.previous() != null; node = node.previous()) {
      if (node.operationType == SCALE || node.operationType == SMOOTH_SCALE || node.operationType == ROTATE_SCALE) {
        return true;
      }
    }
    return false;
  }

  Image cachedVariant(long scaleBits) {
    if (cachedVariant1 != null && cachedScale1Bits == scaleBits) {
      cachedUse1 = ++cacheUseCounter;
      return cachedVariant1;
    }
    if (cachedVariant2 != null && cachedScale2Bits == scaleBits) {
      cachedUse2 = ++cacheUseCounter;
      return cachedVariant2;
    }
    return null;
  }

  void cacheVariant(long scaleBits, Image variant) {
    long use = ++cacheUseCounter;
    if (cachedVariant1 == null || cachedUse1 <= cachedUse2) {
      if (cachedVariant1 != null) {
        cachedVariant1.releaseTextureOnly();
      }
      cachedScale1Bits = scaleBits;
      cachedVariant1 = variant;
      cachedUse1 = use;
    } else {
      if (cachedVariant2 != null) {
        cachedVariant2.releaseTextureOnly();
      }
      cachedScale2Bits = scaleBits;
      cachedVariant2 = variant;
      cachedUse2 = use;
    }
  }

  void releaseCachedVariantTextures() {
    if (cachedVariant1 != null) {
      cachedVariant1.releaseTextureOnly();
    }
    if (cachedVariant2 != null) {
      cachedVariant2.releaseTextureOnly();
    }
  }

  void clearCachedVariants() {
    releaseCachedVariantTextures();
    cachedVariant1 = null;
    cachedVariant2 = null;
    cachedUse1 = cachedUse2 = 0;
    cachedScale1Bits = cachedScale2Bits = 0;
  }

  int cachedVariantCountForSmoke() {
    return (cachedVariant1 == null ? 0 : 1) + (cachedVariant2 == null ? 0 : 1);
  }
}
