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
}
