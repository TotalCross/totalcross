// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

/**
 * Internal, unsupported bridge for the graphics package.
 *
 * @hidden
 * @deprecated This class is for TotalCross internals only and is not a supported API.
 */
@Deprecated
public final class ImageDrawingBridge {
  private ImageDrawingBridge() {
  }

  /** @hidden */
  @Deprecated
  public static Image resolveForDrawing(Image image, double destinationScale) throws ImageException {
    return image.resolveForDrawing(destinationScale);
  }

  /** @hidden */
  @Deprecated
  public static Object geometryPlanForDrawing(Image image, double destinationScale) throws ImageException {
    if (image == null) {
      throw new NullPointerException("image");
    }
    return image.geometryPlanForDrawing(destinationScale);
  }
}
