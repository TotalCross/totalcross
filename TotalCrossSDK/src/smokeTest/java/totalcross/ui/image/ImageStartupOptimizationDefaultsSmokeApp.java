// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.ui.MainWindow;

/** Fresh-process smoke for the optimization masks installed during Image startup. */
public class ImageStartupOptimizationDefaultsSmokeApp extends MainWindow {
  private static final long EXPECTED_MASK = (1L << ImageOptimizationSettings.DECODE_ZERO_COPY)
      | (1L << ImageOptimizationSettings.RASTER_OPACITY_METADATA)
      | (1L << ImageOptimizationSettings.RASTER_OPAQUE_WRITE_PIXELS)
      | (1L << ImageOptimizationSettings.RASTER_ROW_READBACK)
      | (1L << ImageOptimizationSettings.RASTER_DIRECT_COLOR_MATERIALIZATION)
      | (1L << ImageOptimizationSettings.RASTER_PHYSICAL_IDENTITY_FOLDING);

  @Override
  public void initUI() {
    long drawMask = Image.nativeOptimizationMaskForDrawForTest();
    long decodeMask = Image.nativeOptimizationMaskForDecodeForTest();
    boolean drawPass = drawMask == EXPECTED_MASK;
    boolean decodePass = decodeMask == EXPECTED_MASK;
    boolean overallPass = drawPass && decodePass;
    System.out.println("fixture=ImageStartupOptimizationDefaultsSmokeApp"
        + ",startupBeforeSettingsApi=true"
        + ",drawMask=" + drawMask
        + ",decodeMask=" + decodeMask
        + ",expectedMask=" + EXPECTED_MASK
        + ",drawPass=" + drawPass
        + ",decodePass=" + decodePass
        + ",overallPass=" + overallPass);
    System.out.flush();
    exit(overallPass ? 0 : 1);
  }
}
