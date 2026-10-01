// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

/** Internal cross-package bridge for raster-core behavior. */
public final class ImageRasterFeatureBridge {
  public static final int DRAW_HANDLED = ImageRasterDiagnostics.DRAW_HANDLED;
  public static final int PHYSICAL_COPY_HIT = ImageRasterDiagnostics.DRAW_PHYSICAL_COPY_HIT;
  static int copyRectPlanAttemptsForTest;
  static int copyRectPlanHandledForTest;
  static int copyRectPlanFallbacksForTest;
  static int copyRectPlanLastStatusForTest;
  static int cachedFinalRasterProbesForTest;
  static int cachedFinalRasterHitsForTest;
  static int cachedFinalRasterMissesForTest;
  static int physicalCopyHitsForTest;
  static int identityAttemptsForTest;
  static int genericGeometryDrawsForTest;
  static int smoothResampleDrawsForTest;
  static int targetColorVariantMaterializationsForTest;
  static int targetColorVariantHitsForTest;
  static int targetColorVariantFallbacksForTest;
  private static boolean drawAccountingEnabledForTest;

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
    if (!drawAccountingEnabledForTest) {
      return;
    }
    if ((status & ImageRasterDiagnostics.DRAW_IDENTITY_ATTEMPT) != 0) {
      identityAttemptsForTest++;
    }
    if ((status & ImageRasterDiagnostics.DRAW_PHYSICAL_COPY_HIT) != 0) {
      physicalCopyHitsForTest++;
    }
    if ((status & ImageRasterDiagnostics.DRAW_GENERIC_GEOMETRY) != 0) {
      genericGeometryDrawsForTest++;
    }
    if ((status & ImageRasterDiagnostics.DRAW_SMOOTH_RESAMPLE) != 0) {
      smoothResampleDrawsForTest++;
    }
    if ((status & ImageRasterDiagnostics.DRAW_TARGET_COLOR_MATERIALIZED) != 0) {
      targetColorVariantMaterializationsForTest++;
    }
    if ((status & ImageRasterDiagnostics.DRAW_TARGET_COLOR_HIT) != 0) {
      targetColorVariantHitsForTest++;
    }
    if ((status & ImageRasterDiagnostics.DRAW_TARGET_COLOR_FALLBACK) != 0) {
      targetColorVariantFallbacksForTest++;
    }
  }

  /** @hidden */
  public static void recordCopyRectPlanResult(int status) {
    ImageRasterDiagnostics.record(ImageRasterDiagnostics.COPY_RECT_PLAN_ATTEMPT);
    if ((status & DRAW_HANDLED) != 0) {
      ImageRasterDiagnostics.record(ImageRasterDiagnostics.COPY_RECT_PLAN_HANDLED);
    } else {
      ImageRasterDiagnostics.record(ImageRasterDiagnostics.COPY_RECT_PLAN_FALLBACK);
    }
    if (drawAccountingEnabledForTest) {
      copyRectPlanAttemptsForTest++;
      copyRectPlanLastStatusForTest = status;
      if ((status & DRAW_HANDLED) != 0) {
        copyRectPlanHandledForTest++;
      } else {
        copyRectPlanFallbacksForTest++;
      }
    }
  }

  /** @hidden */
  public static void recordCachedFinalRasterProbe(boolean hit) {
    if (!drawAccountingEnabledForTest) {
      return;
    }
    cachedFinalRasterProbesForTest++;
    if (hit) {
      cachedFinalRasterHitsForTest++;
    } else {
      cachedFinalRasterMissesForTest++;
    }
  }

  static void resetDrawAccountingForTest() {
    drawAccountingEnabledForTest = true;
    copyRectPlanAttemptsForTest = 0;
    copyRectPlanHandledForTest = 0;
    copyRectPlanFallbacksForTest = 0;
    copyRectPlanLastStatusForTest = 0;
    cachedFinalRasterProbesForTest = 0;
    cachedFinalRasterHitsForTest = 0;
    cachedFinalRasterMissesForTest = 0;
    physicalCopyHitsForTest = 0;
    identityAttemptsForTest = 0;
    genericGeometryDrawsForTest = 0;
    smoothResampleDrawsForTest = 0;
    targetColorVariantMaterializationsForTest = 0;
    targetColorVariantHitsForTest = 0;
    targetColorVariantFallbacksForTest = 0;
  }
}
