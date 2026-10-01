// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

/** Internal bridge for graphics operations that may reuse a final materialized raster. */
public final class ImageDrawingFeatureBridge {
  private ImageDrawingFeatureBridge() {
  }

  /** @hidden */
  public static Image cachedFinalRasterForDrawing(Image image, double destinationScale) throws ImageException {
    if (image == null) {
      throw new NullPointerException("image");
    }
    return image.cachedFinalRasterForDrawing(destinationScale);
  }
}
