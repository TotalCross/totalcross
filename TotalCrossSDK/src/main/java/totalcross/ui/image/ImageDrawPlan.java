// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

/**
 * Internal immutable drawing description for a deferred image pipeline.
 *
 * <p>The root keeps the immutable native source alive. Operations are stored
 * in source-to-result order and parameters contains four entries per
 * operation. The semantic pipeline remains authoritative; this description
 * transports the information needed by the native drawing path, including
 * ordered color candidates that can be applied by the draw paint.</p>
 */
final class ImageDrawPlan {
  static final int CAPABILITY_DRAW_GEOMETRY = 1;
  static final int CAPABILITY_DRAW_COLOR_CANDIDATE = 2;
  static final int CAPABILITY_MATERIALIZATION_BARRIER = 3;

  final Image root;
  final int[] operations;
  final int[] parameters;
  final int[] dimensions;
  final int[] operationCapabilities;
  final int rootWidth;
  final int rootHeight;
  final int rootLogicalWidth;
  final int rootLogicalHeight;
  final int rootFrameCount;
  final int rootWidthOfAllFrames;
  final int outputWidth;
  final int outputHeight;
  final int outputFrameCount;
  final int outputWidthOfAllFrames;
  final int currentFrame;
  final int alphaMask;
  final int transparentColor;
  final int materializeAlphaMask;
  final int outputAlphaMask;
  final double rootContentScale;
  final double outputContentScale;
  final double destinationScale;
  final double hwScaleW;
  final double hwScaleH;
  final double rootHwScaleW;
  final double rootHwScaleH;
  final long sourceDecodeGeneration;

  ImageDrawPlan(Image root, int[] operations, int[] parameters, int[] dimensions, int rootWidth, int rootHeight,
      int rootLogicalWidth, int rootLogicalHeight, int rootFrameCount, int rootWidthOfAllFrames,
      double rootContentScale, int outputWidth, int outputHeight,
      int outputFrameCount, int outputWidthOfAllFrames, int currentFrame, int alphaMask,
      int transparentColor, int materializeAlphaMask, int outputAlphaMask, double destinationScale,
      double outputContentScale, double hwScaleW, double hwScaleH, double rootHwScaleW,
      double rootHwScaleH) {
    this(root, operations, parameters, dimensions, rootWidth, rootHeight, rootLogicalWidth, rootLogicalHeight,
        rootFrameCount, rootWidthOfAllFrames, rootContentScale, outputWidth, outputHeight, outputFrameCount,
        outputWidthOfAllFrames, currentFrame, alphaMask, transparentColor, materializeAlphaMask, outputAlphaMask,
        destinationScale, outputContentScale, hwScaleW, hwScaleH, rootHwScaleW, rootHwScaleH, 0);
  }

  ImageDrawPlan(Image root, int[] operations, int[] parameters, int[] dimensions, int rootWidth, int rootHeight,
      int rootLogicalWidth, int rootLogicalHeight, int rootFrameCount, int rootWidthOfAllFrames,
      double rootContentScale, int outputWidth, int outputHeight,
      int outputFrameCount, int outputWidthOfAllFrames, int currentFrame, int alphaMask,
      int transparentColor, int materializeAlphaMask, int outputAlphaMask, double destinationScale,
      double outputContentScale, double hwScaleW, double hwScaleH, double rootHwScaleW,
      double rootHwScaleH, long sourceDecodeGeneration) {
    if (root == null || operations == null || parameters == null || dimensions == null
        || operations.length * 4 != parameters.length || operations.length * 2 != dimensions.length) {
      throw new IllegalArgumentException("Invalid image draw plan");
    }
    this.root = root;
    this.operations = operations;
    this.parameters = parameters;
    this.dimensions = dimensions;
    operationCapabilities = classifyOperations(operations);
    this.sourceDecodeGeneration = sourceDecodeGeneration;
    this.rootWidth = rootWidth;
    this.rootHeight = rootHeight;
    this.rootLogicalWidth = rootLogicalWidth;
    this.rootLogicalHeight = rootLogicalHeight;
    this.rootFrameCount = rootFrameCount;
    this.rootWidthOfAllFrames = rootWidthOfAllFrames;
    this.outputWidth = outputWidth;
    this.outputHeight = outputHeight;
    this.outputFrameCount = outputFrameCount;
    this.outputWidthOfAllFrames = outputWidthOfAllFrames;
    this.currentFrame = currentFrame;
    this.alphaMask = alphaMask;
    this.transparentColor = transparentColor;
    this.materializeAlphaMask = materializeAlphaMask;
    this.outputAlphaMask = outputAlphaMask;
    this.rootContentScale = rootContentScale;
    this.destinationScale = destinationScale;
    this.outputContentScale = outputContentScale;
    this.hwScaleW = hwScaleW;
    this.hwScaleH = hwScaleH;
    this.rootHwScaleW = rootHwScaleW;
    this.rootHwScaleH = rootHwScaleH;
  }

  /** Returns this structural plan with the caller's current mutable presentation state. */
  ImageDrawPlan withPresentationState(int currentFrame, int alphaMask, int transparentColor,
      int materializeAlphaMask, int outputAlphaMask, double hwScaleW, double hwScaleH) {
    return new ImageDrawPlan(root, operations, parameters, dimensions, rootWidth, rootHeight,
        rootLogicalWidth, rootLogicalHeight, rootFrameCount, rootWidthOfAllFrames, rootContentScale,
        outputWidth, outputHeight, outputFrameCount, outputWidthOfAllFrames, currentFrame, alphaMask,
        transparentColor, materializeAlphaMask, outputAlphaMask, destinationScale, outputContentScale,
        hwScaleW, hwScaleH, rootHwScaleW, rootHwScaleH, sourceDecodeGeneration);
  }

  private static int[] classifyOperations(int[] operations) {
    int[] capabilities = new int[operations.length];
    for (int i = 0; i < operations.length; i++) {
      switch (operations[i]) {
      case ImagePipeline.SCALE:
      case ImagePipeline.SMOOTH_SCALE:
      case ImagePipeline.ROTATE_SCALE:
      case ImagePipeline.FRAME_SELECT:
      case ImagePipeline.CROP:
      case ImagePipeline.FRAME_LAYOUT:
        capabilities[i] = CAPABILITY_DRAW_GEOMETRY;
        break;
      case ImagePipeline.TOUCH_UP:
      case ImagePipeline.FADE:
      case ImagePipeline.ALPHA:
      case ImagePipeline.APPLY_FADE:
      case ImagePipeline.APPLY_COLOR:
      case ImagePipeline.APPLY_COLOR2:
        capabilities[i] = CAPABILITY_DRAW_COLOR_CANDIDATE;
        break;
      case ImagePipeline.CHANGE_COLORS:
      case ImagePipeline.SET_TRANSPARENT_COLOR:
        capabilities[i] = CAPABILITY_MATERIALIZATION_BARRIER;
        break;
      default:
        throw new IllegalArgumentException("Unknown image draw operation: " + operations[i]);
      }
    }
    return capabilities;
  }
}
