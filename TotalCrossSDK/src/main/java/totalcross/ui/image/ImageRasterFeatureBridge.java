// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

/** Internal cross-package bridge for raster-core behavior. */
public final class ImageRasterFeatureBridge {
  public static final int DRAW_HANDLED = ImageRasterDiagnostics.DRAW_HANDLED;
  static int copyRectPlanAttemptsForTest;
  static int copyRectPlanHandledForTest;
  static int copyRectPlanFallbacksForTest;
  static int copyRectPlanLastStatusForTest;
  static int cachedFinalRasterProbesForTest;
  static int cachedFinalRasterHitsForTest;
  static int cachedFinalRasterMissesForTest;

  private ImageRasterFeatureBridge() {
  }

  /** @hidden */
  public static void prepareForMutation(Image image) {
    if (image != null) {
      image.prepareForMutation();
    }
  }

  /** @hidden */
  public static void recordMutation(Image image) {
    if (image != null) {
      image.recordGraphicsMutation();
    }
  }

  /** @hidden */
  public static boolean tryWriteOpaquePixels(Image image, int[] data, int offset,
      int x, int y, int width, int height, boolean unscaled) {
    if (image == null || !image.opaqueWritePixelsEnabledForP2()) {
      return false;
    }
    boolean written = unscaled && image.tryWriteOpaquePixels(data, offset, x, y, width, height);
    ImageRasterDiagnostics.record(written
        ? ImageRasterDiagnostics.OPAQUE_WRITE_SUCCESS : ImageRasterDiagnostics.RASTER_FALLBACK);
    return written;
  }

  /** @hidden */
  public static void recordOpaqueWriteResult(boolean directWrite) {
    ImageRasterDiagnostics.record(directWrite
        ? ImageRasterDiagnostics.OPAQUE_WRITE_SUCCESS : ImageRasterDiagnostics.RASTER_FALLBACK);
  }

  /** @hidden */
  public static void recordRasterFallback() {
    ImageRasterDiagnostics.record(ImageRasterDiagnostics.RASTER_FALLBACK);
  }

  /** @hidden */
  public static void recordDrawEvents(int status) {
    ImageRasterDiagnostics.recordDrawEvents(status);
  }

  /** @hidden */
  public static void recordCopyRectPlanResult(int status) {
    copyRectPlanAttemptsForTest++;
    copyRectPlanLastStatusForTest = status;
    if ((status & DRAW_HANDLED) != 0) {
      copyRectPlanHandledForTest++;
    } else {
      copyRectPlanFallbacksForTest++;
    }
  }

  /** @hidden */
  public static void recordCachedFinalRasterProbe(boolean hit) {
    cachedFinalRasterProbesForTest++;
    if (hit) {
      cachedFinalRasterHitsForTest++;
    } else {
      cachedFinalRasterMissesForTest++;
    }
  }
}
