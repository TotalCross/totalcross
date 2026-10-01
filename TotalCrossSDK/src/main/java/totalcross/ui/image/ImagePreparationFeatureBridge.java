// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

/**
 * Internal bridge for the UI package; this is not supported application API.
 *
 * @hidden
 * @deprecated TotalCross internal use only.
 */
@Deprecated
public final class ImagePreparationFeatureBridge {
  private ImagePreparationFeatureBridge() {
  }

  /** @hidden */
  @Deprecated
  public static void prepareForDisplay(Image image, double destinationScale,
      long batchGeneration, Runnable completion) {
    ImagePreparationMetrics.discovered();
    ImagePreparationRequest request = image == null ? null
        : image.captureDisplayPreparationRequest(destinationScale, batchGeneration);
    ImagePreparationScheduler.submit(request, completion);
  }
}
