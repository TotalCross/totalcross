// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** macOS smoke for invalidating cached opacity after native surface writes. */
public class ImageRasterOpacityMutationSmokeApp extends MainWindow {
  @Override
  public void initUI() {
    boolean decodedOpaque = false;
    boolean invalidated = false;
    boolean drawParity = false;
    String error = "";
    try {
      ImageOptimizationSettings.resetForTest();
      ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_OPACITY_METADATA,
          ImageOptimizationSettings.ENABLED);
      ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_OPAQUE_WRITE_PIXELS,
          ImageOptimizationSettings.ENABLED);
      byte[] jpeg = ImageRasterBenchmarkSupport.resource("image-abi/lena512.jpg");
      Image source = new Image(jpeg, jpeg.length);
      source.getPixels();
      decodedOpaque = source.backing instanceof NativeImageBacking
          && ((NativeImageBacking) source.backing).opacityForTest() == NativeImageBacking.OPACITY_OPAQUE;
      Graphics sourceGraphics = source.getGraphics();
      sourceGraphics.foreColor = 0x00112233;
      sourceGraphics.alpha = 0;
      sourceGraphics.setPixel(0, 0);
      invalidated = ((NativeImageBacking) source.backing).opacityForTest()
          == NativeImageBacking.OPACITY_UNKNOWN;

      ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_OPAQUE_WRITE_PIXELS,
          ImageOptimizationSettings.DISABLED);
      Image disabledTarget = new Image(source.getPixelWidth(), source.getPixelHeight());
      disabledTarget.getGraphics().drawImage(source, 0, 0, false);
      int[] disabledPixels = disabledTarget.getPixels();

      ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_OPAQUE_WRITE_PIXELS,
          ImageOptimizationSettings.ENABLED);
      Image enabledTarget = new Image(source.getPixelWidth(), source.getPixelHeight());
      enabledTarget.getGraphics().drawImage(source, 0, 0, false);
      drawParity = samePixels(disabledPixels, enabledTarget.getPixels());
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean pass = decodedOpaque && invalidated && drawParity && error.length() == 0;
    System.out.println("fixture=ImageRasterOpacityMutationSmokeApp,decodedOpaque=" + decodedOpaque
        + ",invalidated=" + invalidated + ",drawParity=" + drawParity
        + ",overallPass=" + pass + (error.length() == 0 ? "" : ",error=" + error));
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
