// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

/** Immutable detached pixel snapshot used as a deferred pipeline root. */
final class RasterImageSource extends ImageSource {
  final int width;
  final int height;
  final int logicalWidth;
  final int logicalHeight;
  final double contentScale;
  final int frameCount;
  final int currentFrame;
  final int widthOfAllFrames;
  final int[] pixels;
  final int[] pixelsOfAllFrames;
  final String comment;
  final String path;
  final int surfaceType;
  final int transparentColor;
  final boolean useAlpha;
  final int alphaMask;
  final double hwScaleW;
  final double hwScaleH;

  RasterImageSource(int width, int height, int logicalWidth, int logicalHeight, double contentScale,
      int frameCount, int currentFrame, int widthOfAllFrames, int[] pixels, int[] pixelsOfAllFrames,
      String comment, String path, int surfaceType, int transparentColor, boolean useAlpha, int alphaMask,
      double hwScaleW, double hwScaleH) {
    this.width = width;
    this.height = height;
    this.logicalWidth = logicalWidth;
    this.logicalHeight = logicalHeight;
    this.contentScale = contentScale;
    this.frameCount = frameCount;
    this.currentFrame = currentFrame;
    this.widthOfAllFrames = widthOfAllFrames;
    this.pixels = pixels;
    this.pixelsOfAllFrames = pixelsOfAllFrames;
    this.comment = comment;
    this.path = path;
    this.surfaceType = surfaceType;
    this.transparentColor = transparentColor;
    this.useAlpha = useAlpha;
    this.alphaMask = alphaMask;
    this.hwScaleW = hwScaleW;
    this.hwScaleH = hwScaleH;
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
  int logicalWidth() {
    return logicalWidth;
  }

  @Override
  int logicalHeight() {
    return logicalHeight;
  }

  @Override
  int frameCount() {
    return frameCount;
  }

  @Override
  int widthOfAllFrames() {
    return widthOfAllFrames;
  }

  @Override
  double contentScale() {
    return contentScale;
  }

  Image materialize() throws ImageException {
    return Image.materializeRasterSource(this);
  }
}
