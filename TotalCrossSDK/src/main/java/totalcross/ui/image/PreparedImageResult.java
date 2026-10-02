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
  private boolean candidatesReleased;

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

  /** Releases detached native candidates that were not retained by live state. */
  void releaseUnretainedCandidates(ImagePreparationRequest request) {
    if (candidatesReleased) {
      return;
    }
    candidatesReleased = true;

    ImageBacking retainedSourceBacking = null;
    ImageBacking retainedVariantBacking = null;
    if (request != null) {
      retainedSourceBacking = request.source.decodedBackingForReuse(request.decodeDenominator);
      Image retainedVariant = request.pipeline.cachedMaterializedVariant(
          request.materializedScaleBits(), request.source.decodedGeneration());
      retainedVariantBacking = retainedVariant == null ? null : retainedVariant.backing;
    }
    ImageBacking detachedSourceBacking = detachedSource == null
        ? null : detachedSource.decodedBackingForPreparationCleanup();
    releaseIfUnretained(backing, retainedSourceBacking, retainedVariantBacking);
    if (detachedSourceBacking != backing) {
      releaseIfUnretained(detachedSourceBacking, retainedSourceBacking, retainedVariantBacking);
    }
    ImageBacking variantBacking = variant == null ? null : variant.backing;
    if (variantBacking != backing && variantBacking != detachedSourceBacking) {
      releaseIfUnretained(variantBacking, retainedSourceBacking, retainedVariantBacking);
    }
  }

  private static void releaseIfUnretained(ImageBacking candidate,
      ImageBacking retainedSourceBacking, ImageBacking retainedVariantBacking) {
    if (candidate instanceof NativeImageBacking && candidate != retainedSourceBacking
        && candidate != retainedVariantBacking) {
      ((NativeImageBacking) candidate).release();
    }
  }

  void releaseDetachedEncodedSource() {
    if (detachedSource != null) {
      detachedSource.releaseForPreparation();
    }
    detachedSource = null;
  }
}
