// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.ui.MainWindow;

/** macOS smoke for write-pixels execution of a trivial native draw plan. */
public class ImageRasterDrawPlanWritePixelsSmokeApp extends MainWindow {
  @Override
  public void initUI() {
    boolean parity = false;
    boolean fastHit = false;
    String error = "";
    try {
      ImageOptimizationSettings.resetForTest();
      ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_OPACITY_METADATA,
          ImageOptimizationSettings.ENABLED);
      byte[] encoded = ImageRasterBenchmarkSupport.opaquePng(32, 24);
      Image source = new Image(encoded, encoded.length);
      source.getPixels();
      Image planImage = source.getClippedInstance(0, 0, source.getPixelWidth(), source.getPixelHeight());

      ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_OPAQUE_WRITE_PIXELS,
          ImageOptimizationSettings.DISABLED);
      Image slowTarget = new Image(source.getPixelWidth(), source.getPixelHeight());
      slowTarget.getGraphics().drawImage(planImage, 0, 0, false);
      int[] slowPixels = slowTarget.getPixels();

      Image.resetImageOperationAccountingForTest();
      ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_OPAQUE_WRITE_PIXELS,
          ImageOptimizationSettings.ENABLED);
      Image fastTarget = new Image(source.getPixelWidth(), source.getPixelHeight());
      fastTarget.getGraphics().drawImage(planImage, 0, 0, false);
      parity = samePixels(slowPixels, fastTarget.getPixels());
      fastHit = NativeImageBacking.writePixelsHitsForTest() > 0;
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean pass = parity && fastHit && error.length() == 0;
    System.out.println("fixture=ImageRasterDrawPlanWritePixelsSmokeApp,parity=" + parity
        + ",fastHit=" + fastHit + ",overallPass=" + pass
        + (error.length() == 0 ? "" : ",error=" + error));
    System.out.flush();
    exit(pass ? 0 : 1);
  }

  private static boolean samePixels(int[] first, int[] second) {
    if (first == null || second == null || first.length != second.length) {
      return false;
    }
    for (int i = 0; i < first.length; i++) {
      if (first[i] != second[i]) {
        return false;
      }
    }
    return true;
  }
}
