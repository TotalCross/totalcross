// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

/** Worker-owned detached result; backing ownership moves to the live source on UI adoption. */
final class PreparedImageResult {
  enum FailureKind {
    NONE,
    DETERMINISTIC,
    TRANSIENT
  }

  EncodedImageSource detachedSource;
  final ImageBacking backing;
  final int decodedWidth;
  final int decodedHeight;
  final int decodeDenominator;
  final Image variant;
  final ImageException failure;
  final FailureKind failureKind;

  private PreparedImageResult(EncodedImageSource detachedSource, ImageBacking backing,
      int decodedWidth, int decodedHeight, int decodeDenominator, Image variant,
      ImageException failure, FailureKind failureKind) {
    this.detachedSource = detachedSource;
    this.backing = backing;
    this.decodedWidth = decodedWidth;
    this.decodedHeight = decodedHeight;
    this.decodeDenominator = decodeDenominator;
    this.variant = variant;
    this.failure = failure;
    this.failureKind = failureKind;
  }

  static PreparedImageResult ready(EncodedImageSource source, ImageBacking backing,
      int width, int height, int denominator, Image variant) {
    return new PreparedImageResult(source, backing, width, height, denominator, variant,
        null, FailureKind.NONE);
  }

  static PreparedImageResult deterministicFailure(EncodedImageSource source, ImageException failure) {
    return new PreparedImageResult(source, null, 0, 0, 0, null, failure, FailureKind.DETERMINISTIC);
  }

  static PreparedImageResult transientFailure(EncodedImageSource source) {
    return new PreparedImageResult(source, null, 0, 0, 0, null, null, FailureKind.TRANSIENT);
  }

  void releaseDetachedEncodedSource() {
    detachedSource = null;
  }
}
