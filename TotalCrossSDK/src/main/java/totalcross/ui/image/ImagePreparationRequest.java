// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.runtime.ImageRuntimePolicy;

/** Immutable identity and captured state for one explicit display preparation. */
final class ImagePreparationRequest {
  enum Readiness {
    DRAW_READY,
    COPY_READY
  }

  final Image target;
  final EncodedImageSource source;
  final ImagePipeline pipeline;
  final ImageDecodePolicy decodePolicy;
  final long destinationScaleBits;
  private final double capturedDestinationScale;
  final int requestedWidth;
  final int requestedHeight;
  final int decodeDenominator;
  final long sourceGeneration;
  final long targetBackingMutationGeneration;
  final ImageRuntimePolicy effectivePolicy;
  final int currentFrame;
  final int imageWidth;
  final int imageHeight;
  final int logicalWidth;
  final int logicalHeight;
  final Readiness readiness;
  final long batchGeneration;
  final Image prototype;

  ImagePreparationRequest(Image target, EncodedImageSource source, ImagePipeline pipeline,
      ImageDecodePolicy decodePolicy, long destinationScaleBits, double destinationScale,
      int requestedWidth, int requestedHeight,
      int decodeDenominator, long sourceGeneration, long targetBackingMutationGeneration,
      ImageRuntimePolicy effectivePolicy, int currentFrame,
      int imageWidth, int imageHeight, int logicalWidth, int logicalHeight,
      Readiness readiness, long batchGeneration, Image prototype) {
    this.target = target;
    this.source = source;
    this.pipeline = pipeline;
    this.decodePolicy = decodePolicy;
    this.destinationScaleBits = destinationScaleBits;
    this.capturedDestinationScale = destinationScale;
    this.requestedWidth = requestedWidth;
    this.requestedHeight = requestedHeight;
    this.decodeDenominator = decodeDenominator;
    this.sourceGeneration = sourceGeneration;
    this.targetBackingMutationGeneration = targetBackingMutationGeneration;
    this.effectivePolicy = effectivePolicy;
    this.currentFrame = currentFrame;
    this.imageWidth = imageWidth;
    this.imageHeight = imageHeight;
    this.logicalWidth = logicalWidth;
    this.logicalHeight = logicalHeight;
    this.readiness = readiness;
    this.batchGeneration = batchGeneration;
    this.prototype = prototype;
  }

  double destinationScale() {
    return capturedDestinationScale;
  }

  long materializedScaleBits() {
    return Double.doubleToLongBits(pipeline.hasGeometricNode() ? destinationScale() : 1.0);
  }

  ImagePreparationRequest metadataOnly() {
    return new ImagePreparationRequest(target, source, pipeline, decodePolicy, destinationScaleBits,
        capturedDestinationScale, requestedWidth, requestedHeight, decodeDenominator, sourceGeneration,
        targetBackingMutationGeneration, effectivePolicy, currentFrame, imageWidth, imageHeight,
        logicalWidth, logicalHeight, readiness, 0L, null);
  }

  boolean equivalentTo(ImagePreparationRequest other) {
    return sameExceptSourceGeneration(other) && sourceGeneration == other.sourceGeneration
        && readinessSatisfies(readiness, other.readiness);
  }

  boolean equivalentExceptSourceGeneration(ImagePreparationRequest other) {
    return sameExceptSourceGeneration(other) && readinessSatisfies(readiness, other.readiness);
  }

  private boolean sameExceptSourceGeneration(ImagePreparationRequest other) {
    return other != null && target == other.target && source == other.source && pipeline == other.pipeline
        && decodePolicy == other.decodePolicy && destinationScaleBits == other.destinationScaleBits
        && requestedWidth == other.requestedWidth && requestedHeight == other.requestedHeight
        && decodeDenominator == other.decodeDenominator && effectivePolicy == other.effectivePolicy
        && targetBackingMutationGeneration == other.targetBackingMutationGeneration
        && pipeline.decodePolicy() == decodePolicy && other.pipeline.decodePolicy() == other.decodePolicy
        && currentFrame == other.currentFrame && imageWidth == other.imageWidth
        && imageHeight == other.imageHeight && logicalWidth == other.logicalWidth
        && logicalHeight == other.logicalHeight;
  }

  private static boolean readinessSatisfies(Readiness actual, Readiness required) {
    return actual == required || actual == Readiness.COPY_READY && required == Readiness.DRAW_READY;
  }
}
