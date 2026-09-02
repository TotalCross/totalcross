// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

/**
 * Native drawing description for a geometry-only deferred image pipeline.
 *
 * <p>The root keeps the immutable native source alive. Operations are stored
 * in source-to-result order and parameters contains four entries per
 * operation. The class intentionally has no rendering behavior; the native
 * Skia bridge owns execution and materialization.</p>
 */
final class ImageGeometryPlan {
  final Image root;
  final int[] operations;
  final int[] parameters;
  final int[] dimensions;
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

  ImageGeometryPlan(Image root, int[] operations, int[] parameters, int[] dimensions, int rootWidth, int rootHeight,
      int rootLogicalWidth, int rootLogicalHeight, int rootFrameCount, int rootWidthOfAllFrames,
      double rootContentScale, int outputWidth, int outputHeight,
      int outputFrameCount, int outputWidthOfAllFrames, int currentFrame, int alphaMask,
      int transparentColor, int materializeAlphaMask, int outputAlphaMask, double destinationScale,
      double outputContentScale, double hwScaleW, double hwScaleH, double rootHwScaleW,
      double rootHwScaleH) {
    if (root == null || operations == null || parameters == null || dimensions == null
        || operations.length * 4 != parameters.length || operations.length * 2 != dimensions.length) {
      throw new IllegalArgumentException("Invalid image geometry plan");
    }
    this.root = root;
    this.operations = operations;
    this.parameters = parameters;
    this.dimensions = dimensions;
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
}
