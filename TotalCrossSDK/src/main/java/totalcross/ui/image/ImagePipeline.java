// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

/** Immutable root of an Image's deferred source representation. */
final class ImagePipeline {
  private final EncodedImageSource root;

  ImagePipeline(EncodedImageSource root) {
    if (root == null) {
      throw new NullPointerException("root");
    }
    this.root = root;
  }

  EncodedImageSource root() {
    return root;
  }
}
