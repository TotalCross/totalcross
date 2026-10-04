// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import java.util.ArrayList;

/** Immutable linked representation of an Image's deferred source and transforms. */
final class ImagePipeline {
  static final int SCALE = 0;
  static final int SMOOTH_SCALE = 1;
  static final int ROTATE_SCALE = 2;
  static final int TOUCH_UP = 3;
  static final int FADE = 4;
  static final int ALPHA = 5;
  static final int APPLY_COLOR = 6;
  static final int APPLY_COLOR2 = 7;
  static final int APPLY_FADE = 8;
  static final int CHANGE_COLORS = 9;
  static final int SET_TRANSPARENT_COLOR = 10;
  static final int FRAME_SELECT = 11;
  static final int CROP = 12;
  static final int FRAME_LAYOUT = 13;

  private final ImageSource root;
  private final ImageDecodePolicy decodePolicy;
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
  private final double contentScale;

  // JavaSE fallback retains at most one exact representation, admitted on its
  // second observation. Native variants are owned by the source backing.
  private long cachedScaleBits;
  private Image cachedVariant;
  private long cachedVariantGeneration;
  private long pendingScaleBits;
  private long pendingGeneration;
  private boolean pendingVariant;
  private long cachedDrawScale1Bits;
  private long cachedDrawScale2Bits;
  private ImageDrawPlan cachedDrawPlan1;
  private ImageDrawPlan cachedDrawPlan2;
  private long cachedDrawUseCounter;
  private long cachedDrawUse1;
  private long cachedDrawUse2;
  private long cachedDrawGeneration1;
  private long cachedDrawGeneration2;
  private int persistentDrawingOwnerCount;

  ImagePipeline(ImageSource root) {
    this(root, ImageDecodePolicy.targetDecode());
  }

  ImagePipeline(ImageSource root, ImageDecodePolicy decodePolicy) {
    if (root == null) {
      throw new NullPointerException("root");
    }
    if (decodePolicy == null) {
      throw new NullPointerException("decodePolicy");
    }
    this.root = root;
    this.decodePolicy = decodePolicy;
    previous = null;
    operationType = -1;
    parameter1 = parameter2 = parameter3 = parameter4 = 0;
    width = root.width();
    height = root.height();
    logicalWidth = root.logicalWidth();
    logicalHeight = root.logicalHeight();
    frameCount = root.frameCount();
    widthOfAllFrames = root.widthOfAllFrames();
    contentScale = root.contentScale();
    Image.recordImagePipelineCreatedForTest();
  }

  private ImagePipeline(ImagePipeline previous, int operationType, int parameter1, int parameter2,
      int parameter3, int parameter4, int width, int height, int logicalWidth, int logicalHeight,
      int frameCount, int widthOfAllFrames) {
    this.root = previous.root;
    this.decodePolicy = previous.decodePolicy;
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
    this.contentScale = previous.contentScale;
    Image.recordImagePipelineCreatedForTest();
  }

  ImageSource root() {
    return root;
  }

  ImageDecodePolicy decodePolicy() {
    return decodePolicy;
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

  double contentScale() {
    return contentScale;
  }

  ImagePipeline append(int operationType, int parameter1, int parameter2, int parameter3, int parameter4,
      int width, int height, int logicalWidth, int logicalHeight, int frameCount, int widthOfAllFrames) {
    return new ImagePipeline(this, operationType, parameter1, parameter2, parameter3, parameter4,
        width, height, logicalWidth, logicalHeight, frameCount, widthOfAllFrames);
  }

  /** Copies only immutable node data; materialized caches are always fresh. */
  ImagePipeline detachedCopy(EncodedImageSource detachedRoot) {
    ArrayList<ImagePipeline> nodes = new ArrayList<ImagePipeline>();
    for (ImagePipeline node = this; node.previous != null; node = node.previous) {
      nodes.add(node);
    }
    ImagePipeline copy = new ImagePipeline(detachedRoot, decodePolicy);
    for (int i = nodes.size() - 1; i >= 0; i--) {
      ImagePipeline node = nodes.get(i);
      copy = copy.append(node.operationType, node.parameter1, node.parameter2, node.parameter3,
          node.parameter4, node.width, node.height, node.logicalWidth, node.logicalHeight,
          node.frameCount, node.widthOfAllFrames);
    }
    return copy;
  }

  boolean hasGeometricNode() {
    for (ImagePipeline node = this; node.previous() != null; node = node.previous()) {
      if (node.operationType == SCALE || node.operationType == SMOOTH_SCALE || node.operationType == ROTATE_SCALE) {
        return true;
      }
    }
    return false;
  }

  boolean hasDeferredOperations() {
    return previous != null;
  }

  boolean hasGeometryOperation() {
    for (ImagePipeline node = this; node.previous() != null; node = node.previous()) {
      if (node.operationType == SCALE || node.operationType == SMOOTH_SCALE || node.operationType == ROTATE_SCALE
          || node.operationType == FRAME_SELECT || node.operationType == CROP || node.operationType == FRAME_LAYOUT) {
        return true;
      }
    }
    return false;
  }

  boolean isGeometryOnly() {
    for (ImagePipeline node = this; node.previous() != null; node = node.previous()) {
      switch (node.operationType) {
      case SCALE:
      case SMOOTH_SCALE:
      case ROTATE_SCALE:
      case FRAME_SELECT:
      case CROP:
      case FRAME_LAYOUT:
        break;
      default:
        return false;
      }
    }
    return hasGeometryOperation();
  }

  static boolean isFrameDomainBarrier(int operationType) {
    return operationType == FRAME_SELECT || operationType == FRAME_LAYOUT;
  }

  /** Returns whether this pipeline can be represented by the current draw plan. */
  boolean isDrawFusable() {
    boolean hasDrawableOperation = false;
    int frameDomainBarrierCount = 0;
    for (ImagePipeline node = this; node.previous() != null; node = node.previous()) {
      switch (node.operationType) {
      case SCALE:
      case CROP:
        hasDrawableOperation = true;
        break;
      case FRAME_SELECT:
      case FRAME_LAYOUT:
        if (isFrameDomainBarrier(node.operationType) && ++frameDomainBarrierCount > 1) {
          // A native geometry segment may contain only one frame-domain
          // operation. Resolve later frame changes canonically.
          return false;
        }
        hasDrawableOperation = true;
        break;
      case SMOOTH_SCALE:
        hasDrawableOperation = true;
        break;
      case ROTATE_SCALE:
        hasDrawableOperation = true;
        break;
      case TOUCH_UP:
      case FADE:
      case ALPHA:
      case APPLY_FADE:
      case APPLY_COLOR:
        hasDrawableOperation = true;
        break;
      default:
        return false;
      }
    }
    return hasDrawableOperation;
  }

  /** Returns whether drawing can apply this color-only pipeline without a transformed variant. */
  boolean isTrivialDrawPlan() {
    boolean hasColorOperation = false;
    for (ImagePipeline node = this; node.previous() != null; node = node.previous()) {
      switch (node.operationType) {
      case TOUCH_UP:
      case FADE:
      case ALPHA:
      case APPLY_FADE:
      case APPLY_COLOR:
        hasColorOperation = true;
        break;
      default:
        return false;
      }
    }
    return hasColorOperation;
  }

  boolean hasZeroWidthFrameLayout() {
    for (ImagePipeline node = this; node.previous() != null; node = node.previous()) {
      if (node.operationType == FRAME_LAYOUT && node.logicalWidth == 0) {
        return true;
      }
    }
    return false;
  }

  void releaseCachedVariantTextures() {
    if (cachedVariant != null) {
      cachedVariant.releaseTextureOnly();
    }
  }

  void clearCachedVariants() {
    clearCachedMaterializedVariantAndPending();
    cachedDrawPlan1 = null;
    cachedDrawPlan2 = null;
    cachedDrawUse1 = cachedDrawUse2 = 0;
    cachedDrawScale1Bits = cachedDrawScale2Bits = 0;
    cachedDrawGeneration1 = cachedDrawGeneration2 = 0;
    cachedDrawUseCounter = 0;
  }

  ImageDrawPlan cachedDrawPlan(long scaleBits, long sourceDecodeGeneration) {
    if (cachedDrawPlan1 != null && cachedDrawScale1Bits == scaleBits) {
      if (cachedDrawGeneration1 != sourceDecodeGeneration) {
        return null;
      }
      cachedDrawUse1 = ++cachedDrawUseCounter;
      Image.recordImageDrawPlanCacheHitForTest();
      return cachedDrawPlan1;
    }
    if (cachedDrawPlan2 != null && cachedDrawScale2Bits == scaleBits) {
      if (cachedDrawGeneration2 != sourceDecodeGeneration) {
        return null;
      }
      cachedDrawUse2 = ++cachedDrawUseCounter;
      Image.recordImageDrawPlanCacheHitForTest();
      return cachedDrawPlan2;
    }
    return null;
  }

  void cacheDrawPlan(long scaleBits, ImageDrawPlan plan) {
    long use = ++cachedDrawUseCounter;
    if (cachedDrawPlan1 != null && cachedDrawScale1Bits == scaleBits) {
      cachedDrawPlan1 = plan;
      cachedDrawGeneration1 = plan.sourceDecodeGeneration;
      cachedDrawUse1 = use;
    } else if (cachedDrawPlan2 != null && cachedDrawScale2Bits == scaleBits) {
      cachedDrawPlan2 = plan;
      cachedDrawGeneration2 = plan.sourceDecodeGeneration;
      cachedDrawUse2 = use;
    } else if (cachedDrawPlan1 == null || cachedDrawUse1 <= cachedDrawUse2) {
      cachedDrawScale1Bits = scaleBits;
      cachedDrawPlan1 = plan;
      cachedDrawGeneration1 = plan.sourceDecodeGeneration;
      cachedDrawUse1 = use;
    } else {
      cachedDrawScale2Bits = scaleBits;
      cachedDrawPlan2 = plan;
      cachedDrawGeneration2 = plan.sourceDecodeGeneration;
      cachedDrawUse2 = use;
    }
  }

  /** Returns a materialized variant cached by this node, including a prefix node. */
  Image cachedMaterializedVariant(long scaleBits, long sourceDecodeGeneration) {
    if (cachedVariant != null && cachedScaleBits == scaleBits
        && cachedVariantGeneration == sourceDecodeGeneration) {
      if (cachedVariant.backing != null && cachedVariant.backing.isValid()) {
        return cachedVariant;
      }
      clearCachedMaterializedVariantOnly();
    }
    return null;
  }

  private void clearCachedMaterializedVariantOnly() {
    if (cachedVariant != null) {
      cachedVariant.releaseTextureOnly();
    }
    cachedVariant = null;
    cachedScaleBits = 0;
    cachedVariantGeneration = 0;
  }

  private void clearCachedMaterializedVariantAndPending() {
    clearCachedMaterializedVariantOnly();
    pendingVariant = false;
    pendingScaleBits = pendingGeneration = 0;
  }

  /** Admits this exact representation after two consecutive observations. */
  boolean observeMaterializedVariant(long scaleBits, long sourceDecodeGeneration) {
    if (pendingVariant && pendingScaleBits == scaleBits && pendingGeneration == sourceDecodeGeneration) {
      pendingVariant = false;
      return true;
    }
    pendingVariant = true;
    pendingScaleBits = scaleBits;
    pendingGeneration = sourceDecodeGeneration;
    return false;
  }

  void retainPersistentDrawingOwner() {
    if (persistentDrawingOwnerCount == Integer.MAX_VALUE) {
      throw new IllegalStateException("Too many persistent image drawing owners");
    }
    persistentDrawingOwnerCount++;
  }

  void releasePersistentDrawingOwner() {
    if (persistentDrawingOwnerCount <= 0) {
      throw new IllegalStateException("Persistent image drawing owner underflow");
    }
    if (--persistentDrawingOwnerCount == 0) {
      clearCachedMaterializedVariantAndPending();
    }
  }

  int persistentDrawingOwnerCountForSmoke() {
    return persistentDrawingOwnerCount;
  }

  boolean hasPersistentDrawingOwners() {
    return persistentDrawingOwnerCount > 0;
  }

  /** Caches the admitted representation on this node for later prefix reuse. */
  void cacheMaterializedVariant(long scaleBits, Image variant, long sourceDecodeGeneration) {
    if (cachedVariant != null && cachedVariant != variant) {
      cachedVariant.releaseTextureOnly();
    }
    cachedScaleBits = scaleBits;
    cachedVariant = variant;
    cachedVariantGeneration = sourceDecodeGeneration;
    pendingVariant = false;
  }

  int cachedVariantCountForSmoke() {
    return cachedVariant == null ? 0 : 1;
  }
}
