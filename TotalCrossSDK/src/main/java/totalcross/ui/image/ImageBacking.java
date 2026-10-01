// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

/** Internal representation contract for an Image's pixel content. */
abstract class ImageBacking {
  static final int OPACITY_UNKNOWN = 0;
  static final int OPACITY_OPAQUE = 1;
  static final int OPACITY_HAS_ALPHA = 2;

  // These fields are read by internal VM helpers as well as Java. Keep their
  // order synchronized with TotalCrossVM/src/nm/instancefields.h.
  private long mutationGeneration;
  private int opacityState;
  private boolean mutableStorageEscaped;

  abstract boolean isNative();

  final boolean isRaster() {
    return !isNative();
  }

  abstract int width();

  abstract int height();

  abstract boolean isValid();

  /** Returns the visible frame, preserving the live raster identity when applicable. */
  abstract int[] readVisiblePixels(int visibleWidth, int height, int frame);

  /** Returns the physical storage sequence used by legacy equality and hashing. */
  abstract int[] readStoragePixels();

  /** Reads one physical storage row as RGBA bytes without allocating a full raster. */
  abstract boolean readRgbaRow(byte[] output, int y);

  /** Reads a checked rectangle into row-major ARGB output without exposing padding. */
  abstract boolean readPixels(int[] output, int offset, int x, int y, int width, int height);

  final long mutationGeneration() {
    return mutationGeneration;
  }

  final int opacityState() {
    return opacityState;
  }

  final boolean isMutableStorageEscaped() {
    return mutableStorageEscaped;
  }

  final boolean backingIdentityStableForCaching() {
    return !mutableStorageEscaped;
  }

  final boolean markMutableStorageEscaped() {
    if (mutableStorageEscaped) {
      return false;
    }
    mutableStorageEscaped = true;
    return true;
  }

  final boolean isProvenOpaque() {
    return opacityState == OPACITY_OPAQUE;
  }

  final void setOpacityState(int state) {
    if (state < OPACITY_UNKNOWN || state > OPACITY_HAS_ALPHA) {
      throw new IllegalArgumentException("Invalid image opacity state");
    }
    opacityState = state;
  }

  final void markMutation() {
    markMutation(OPACITY_UNKNOWN);
  }

  final void markMutation(int resultingOpacityState) {
    if (resultingOpacityState < OPACITY_UNKNOWN || resultingOpacityState > OPACITY_HAS_ALPHA) {
      throw new IllegalArgumentException("Invalid image opacity state");
    }
    mutationGeneration++;
    opacityState = resultingOpacityState;
  }

  final void markMutationAfter(long minimumGeneration, int resultingOpacityState) {
    if (resultingOpacityState < OPACITY_UNKNOWN || resultingOpacityState > OPACITY_HAS_ALPHA) {
      throw new IllegalArgumentException("Invalid image opacity state");
    }
    mutationGeneration = Math.max(mutationGeneration, minimumGeneration) + 1;
    opacityState = resultingOpacityState;
  }

  final void copyStateTo(ImageBacking destination) {
    if (destination == null) {
      throw new NullPointerException("destination");
    }
    destination.mutationGeneration = mutationGeneration;
    destination.opacityState = opacityState;
    // A snapshot owns separate storage, so never inherit the escaped-array state.
  }

  final void replaceStateFrom(ImageBacking previous, long minimumGeneration) {
    long prior = previous == null ? 0 : previous.mutationGeneration;
    mutationGeneration = Math.max(Math.max(mutationGeneration, prior), minimumGeneration) + 1;
  }

  /** Returns a detached snapshot suitable for a deferred pipeline root. */
  abstract ImageBacking snapshot() throws ImageException;
}
