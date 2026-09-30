// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

/** Immutable JPEG decode request associated with one deferred pipeline root. */
final class ImageDecodePolicy {
  enum Mode {
    TARGET_DECODE,
    BEST_FIT,
    EXPLICIT_RATIO
  }

  private static final ImageDecodePolicy TARGET = new ImageDecodePolicy(Mode.TARGET_DECODE,
      0, 0, 0, 0, 0, 0, 1);

  private final Mode mode;
  private final int requestedWidth;
  private final int requestedHeight;
  private final int scaleNumerator;
  private final int scaleDenominator;
  private final int logicalWidth;
  private final int logicalHeight;
  private final int decodeDenominator;

  private ImageDecodePolicy(Mode mode, int requestedWidth, int requestedHeight, int scaleNumerator,
      int scaleDenominator, int logicalWidth, int logicalHeight, int decodeDenominator) {
    this.mode = mode;
    this.requestedWidth = requestedWidth;
    this.requestedHeight = requestedHeight;
    this.scaleNumerator = scaleNumerator;
    this.scaleDenominator = scaleDenominator;
    this.logicalWidth = logicalWidth;
    this.logicalHeight = logicalHeight;
    this.decodeDenominator = decodeDenominator;
  }

  static ImageDecodePolicy targetDecode() {
    return TARGET;
  }

  static ImageDecodePolicy bestFit(int requestedWidth, int requestedHeight, int decodeDenominator,
      int logicalWidth, int logicalHeight) {
    requirePositive(requestedWidth, "requestedWidth");
    requirePositive(requestedHeight, "requestedHeight");
    requireDecodeDenominator(decodeDenominator);
    requirePositive(logicalWidth, "logicalWidth");
    requirePositive(logicalHeight, "logicalHeight");
    return new ImageDecodePolicy(Mode.BEST_FIT, requestedWidth, requestedHeight, 0, 0,
        logicalWidth, logicalHeight, decodeDenominator);
  }

  static ImageDecodePolicy explicitRatio(int scaleNumerator, int scaleDenominator, int decodeDenominator,
      int logicalWidth, int logicalHeight) {
    requirePositive(scaleNumerator, "scaleNumerator");
    requirePositive(scaleDenominator, "scaleDenominator");
    requireDecodeDenominator(decodeDenominator);
    requirePositive(logicalWidth, "logicalWidth");
    requirePositive(logicalHeight, "logicalHeight");
    return new ImageDecodePolicy(Mode.EXPLICIT_RATIO, 0, 0, scaleNumerator, scaleDenominator,
        logicalWidth, logicalHeight, decodeDenominator);
  }

  Mode mode() {
    return mode;
  }

  int requestedWidth() {
    return requestedWidth;
  }

  int requestedHeight() {
    return requestedHeight;
  }

  int scaleNumerator() {
    return scaleNumerator;
  }

  int scaleDenominator() {
    return scaleDenominator;
  }

  int logicalWidth() {
    return logicalWidth;
  }

  int logicalHeight() {
    return logicalHeight;
  }

  int decodeDenominator() {
    return decodeDenominator;
  }

  private static void requirePositive(int value, String name) {
    if (value <= 0) {
      throw new IllegalArgumentException(name + " must be positive");
    }
  }

  private static void requireDecodeDenominator(int denominator) {
    if (denominator != 1 && denominator != 2 && denominator != 4 && denominator != 8) {
      throw new IllegalArgumentException("Unsupported JPEG decode denominator: " + denominator);
    }
  }
}
