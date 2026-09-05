// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

/** Internal representation contract for an Image's pixel content. */
abstract class ImageBacking {
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

  /** Returns a detached snapshot suitable for a deferred pipeline root. */
  abstract ImageBacking snapshot() throws ImageException;
}
