// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

/** Immutable decode intent carried by an ImagePipeline root. */
final class ImageDecodePolicy {
  static final int TARGET_DECODE = 0;
  static final int BEST_FIT = 1;
  static final int EXPLICIT_RATIO = 2;

  private static final ImageDecodePolicy TARGET = new ImageDecodePolicy(TARGET_DECODE, 0, 0);

  private final int kind;
  private final int parameter1;
  private final int parameter2;

  private ImageDecodePolicy(int kind, int parameter1, int parameter2) {
    this.kind = kind;
    this.parameter1 = parameter1;
    this.parameter2 = parameter2;
  }

  static ImageDecodePolicy targetDecode() {
    return TARGET;
  }

  static ImageDecodePolicy bestFit(int targetWidth, int targetHeight) {
    return new ImageDecodePolicy(BEST_FIT, targetWidth, targetHeight);
  }

  static ImageDecodePolicy explicitRatio(int numerator, int denominator) {
    return new ImageDecodePolicy(EXPLICIT_RATIO, numerator, denominator);
  }

  int kind() {
    return kind;
  }

  int parameter1() {
    return parameter1;
  }

  int parameter2() {
    return parameter2;
  }
}
