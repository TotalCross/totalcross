// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

/** Internal source contract used by the deferred Image pipeline. */
abstract class ImageSource {
  ImageSource() {
  }

  abstract int width();

  abstract int height();

  abstract int logicalWidth();

  abstract int logicalHeight();

  abstract int frameCount();

  abstract int widthOfAllFrames();

  abstract double contentScale();
}
