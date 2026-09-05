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

  /** Returns a detached snapshot suitable for a deferred pipeline root. */
  abstract ImageBacking snapshot();
}
