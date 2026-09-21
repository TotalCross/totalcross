// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

/** Process-global rendering optimization switches. */
public final class RenderingOptimizations {
  /** Reuse the software framebuffer during eligible vertical scrolls. */
  public static final long SCROLL_RASTER_REUSE = 1L;

  private static final long KNOWN_MASK = SCROLL_RASTER_REUSE;
  private static final String[] RASTER_REUSE_FALLBACK_REASONS = {
      "unsupportedBackend", "notTopmost", "pendingRepaint", "legacyOffscreen",
      "transparentContent", "nonIntegralPhysicalGeometry", "deltaOutOfRange",
      "overlappingControl", "overlayScrollbar", "nativeMoveFailure", "postMoveRecovery",
      "unsupportedHorizontal"
  };
  static final int FALLBACK_UNSUPPORTED_BACKEND = 0;
  static final int FALLBACK_NOT_TOPMOST = 1;
  static final int FALLBACK_PENDING_REPAINT = 2;
  static final int FALLBACK_LEGACY_OFFSCREEN = 3;
  static final int FALLBACK_TRANSPARENT_CONTENT = 4;
  static final int FALLBACK_NON_INTEGRAL_GEOMETRY = 5;
  static final int FALLBACK_DELTA_OUT_OF_RANGE = 6;
  static final int FALLBACK_OVERLAPPING_CONTROL = 7;
  static final int FALLBACK_OVERLAY_SCROLLBAR = 8;
  static final int FALLBACK_NATIVE_MOVE_FAILURE = 9;
  static final int FALLBACK_POST_MOVE_RECOVERY = 10;
  static final int FALLBACK_UNSUPPORTED_HORIZONTAL = 11;
  private static long mask;
  private static boolean diagnosticsEnabled;
  private static long rasterReuseAttempts;
  private static long rasterReuseHits;
  private static long rasterReuseFallbacks;
  private static long rasterReuseHorizontalAttempts;
  private static long rasterReuseViewportPixels;
  private static long rasterReuseReusedPixels;
  private static long rasterReuseDirtyPixels;
  private static long rasterReuseMovedBytes;
  private static long rasterReuseDecisionNs;
  private static long rasterReuseMoveNs;
  private static long rasterReuseDirtyPaintNs;
  private static long screenUpdateNs;
  private static long lastScreenUpdateNs;
  private static long[] rasterReuseFallbackCounts = new long[RASTER_REUSE_FALLBACK_REASONS.length];
  private static boolean lastRasterReuseHit;
  private static int lastRasterReuseFallbackReason = -1;
  private static long lastRasterReuseRequestedDelta;
  private static long lastRasterReuseActualDelta;
  private static long lastRasterReuseViewportPixels;
  private static long lastRasterReuseReusedPixels;
  private static long lastRasterReuseDirtyPixels;
  private static long lastRasterReuseMovedBytes;

  private RenderingOptimizations() {
  }

  /** Replaces the process-level rendering optimization mask. */
  public static void setMask(long newMask) {
    if (newMask < 0 || (newMask & ~KNOWN_MASK) != 0) {
      throw new IllegalArgumentException("Unknown rendering optimization mask bits: " + newMask);
    }
    mask = newMask;
  }

  /** Returns the current rendering optimization mask. */
  public static long getMask() {
    return mask;
  }

  static boolean isEnabled(long feature) {
    return (mask & feature) != 0;
  }

  static void resetForTest() {
    mask = 0;
  }

  /** Enables diagnostic accounting for the scroll proof-of-concept benchmark. */
  public static void setDiagnosticsEnabledForTest(boolean enabled) {
    diagnosticsEnabled = enabled;
    if (!enabled) {
      resetDiagnosticsForTest();
    }
  }

  /** Clears scroll-reuse counters without changing the feature mask. */
  public static void resetDiagnosticsForTest() {
    rasterReuseAttempts = 0;
    rasterReuseHits = 0;
    rasterReuseFallbacks = 0;
    rasterReuseHorizontalAttempts = 0;
    rasterReuseViewportPixels = 0;
    rasterReuseReusedPixels = 0;
    rasterReuseDirtyPixels = 0;
    rasterReuseMovedBytes = 0;
    rasterReuseDecisionNs = 0;
    rasterReuseMoveNs = 0;
    rasterReuseDirtyPaintNs = 0;
    screenUpdateNs = 0;
    lastScreenUpdateNs = 0;
    rasterReuseFallbackCounts = new long[RASTER_REUSE_FALLBACK_REASONS.length];
    lastRasterReuseHit = false;
    lastRasterReuseFallbackReason = -1;
    lastRasterReuseRequestedDelta = 0;
    lastRasterReuseActualDelta = 0;
    lastRasterReuseViewportPixels = 0;
    lastRasterReuseReusedPixels = 0;
    lastRasterReuseDirtyPixels = 0;
    lastRasterReuseMovedBytes = 0;
  }

  static boolean diagnosticsEnabled() {
    return diagnosticsEnabled;
  }

  static void beginRasterReuseAttempt(int requestedDelta, int actualDelta) {
    if (!diagnosticsEnabled) {
      return;
    }
    rasterReuseAttempts++;
    lastRasterReuseHit = false;
    lastRasterReuseFallbackReason = -1;
    lastRasterReuseRequestedDelta = requestedDelta;
    lastRasterReuseActualDelta = actualDelta;
    lastRasterReuseViewportPixels = 0;
    lastRasterReuseReusedPixels = 0;
    lastRasterReuseDirtyPixels = 0;
    lastRasterReuseMovedBytes = 0;
  }

  static void recordRasterReuseGeometry(long viewportPixels) {
    if (diagnosticsEnabled) {
      lastRasterReuseViewportPixels = viewportPixels;
    }
  }

  static void recordRasterReuseDecision(long elapsedNs) {
    if (diagnosticsEnabled) {
      rasterReuseDecisionNs += Math.max(0, elapsedNs);
    }
  }

  static void recordRasterReuseMove(long elapsedNs) {
    if (diagnosticsEnabled) {
      rasterReuseMoveNs += Math.max(0, elapsedNs);
    }
  }

  static void recordRasterReuseDirtyPaint(long elapsedNs) {
    if (diagnosticsEnabled) {
      rasterReuseDirtyPaintNs += Math.max(0, elapsedNs);
    }
  }

  static void recordRasterReuseHit(long viewportPixels, long reusedPixels, long dirtyPixels,
      long movedBytes) {
    if (!diagnosticsEnabled) {
      return;
    }
    rasterReuseHits++;
    rasterReuseViewportPixels += viewportPixels;
    rasterReuseReusedPixels += reusedPixels;
    rasterReuseDirtyPixels += dirtyPixels;
    rasterReuseMovedBytes += movedBytes;
    lastRasterReuseHit = true;
    lastRasterReuseFallbackReason = -1;
    lastRasterReuseViewportPixels = viewportPixels;
    lastRasterReuseReusedPixels = reusedPixels;
    lastRasterReuseDirtyPixels = dirtyPixels;
    lastRasterReuseMovedBytes = movedBytes;
  }

  static void recordRasterReuseFallback(int reason) {
    if (!diagnosticsEnabled) {
      return;
    }
    if (reason < 0 || reason >= rasterReuseFallbackCounts.length) {
      throw new IllegalArgumentException("Invalid raster reuse fallback reason: " + reason);
    }
    rasterReuseFallbacks++;
    rasterReuseFallbackCounts[reason]++;
    lastRasterReuseHit = false;
    lastRasterReuseFallbackReason = reason;
  }

  static void recordUnsupportedHorizontal() {
    if (diagnosticsEnabled) {
      rasterReuseHorizontalAttempts++;
      rasterReuseFallbackCounts[11]++;
      lastRasterReuseHit = false;
      lastRasterReuseFallbackReason = 11;
    }
  }

  static void recordScreenUpdate(long elapsedNs) {
    if (diagnosticsEnabled) {
      lastScreenUpdateNs = Math.max(0, elapsedNs);
      screenUpdateNs += lastScreenUpdateNs;
    }
  }

  /** Returns a compact scroll-reuse diagnostic metric for benchmark output. */
  public static long diagnosticMetricForTest(int kind) {
    switch (kind) {
    case 0: return rasterReuseAttempts;
    case 1: return rasterReuseHits;
    case 2: return rasterReuseFallbacks;
    case 3: return rasterReuseHorizontalAttempts;
    case 4: return rasterReuseViewportPixels;
    case 5: return rasterReuseReusedPixels;
    case 6: return rasterReuseDirtyPixels;
    case 7: return rasterReuseMovedBytes;
    case 8: return rasterReuseDecisionNs;
    case 9: return rasterReuseMoveNs;
    case 10: return rasterReuseDirtyPaintNs;
    case 11: return screenUpdateNs;
    case 12: return lastScreenUpdateNs;
    case 13: return lastRasterReuseRequestedDelta;
    case 14: return lastRasterReuseActualDelta;
    case 15: return lastRasterReuseViewportPixels;
    case 16: return lastRasterReuseReusedPixels;
    case 17: return lastRasterReuseDirtyPixels;
    case 18: return lastRasterReuseMovedBytes;
    case 19: return lastRasterReuseHit ? 1 : 0;
    case 20: return lastRasterReuseFallbackReason;
    default:
      if (kind >= 100 && kind < 100 + rasterReuseFallbackCounts.length) {
        return rasterReuseFallbackCounts[kind - 100];
      }
      return 0;
    }
  }

  /** Returns the stable name of a raster-reuse fallback reason. */
  public static String fallbackReasonNameForTest(int reason) {
    return reason >= 0 && reason < RASTER_REUSE_FALLBACK_REASONS.length
        ? RASTER_REUSE_FALLBACK_REASONS[reason] : "";
  }

  /** Returns the last frame's primary raster-reuse fallback reason. */
  public static String lastFallbackReasonForTest() {
    return fallbackReasonNameForTest(lastRasterReuseFallbackReason);
  }
}
