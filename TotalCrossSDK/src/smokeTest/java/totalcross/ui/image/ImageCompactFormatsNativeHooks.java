// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

/**
 * Package-private benchmark seam. Phase 2 supplies conservative fallbacks;
 * Phase 3 replaces these bodies with native format/counter/failure hooks.
 */
final class ImageCompactFormatsNativeHooks {
  private ImageCompactFormatsNativeHooks() {
  }

  static Image materialize(Image image) {
    image.getPixels();
    return image;
  }

  static String format(Image image) {
    return image.hasNativeBackingForSmoke() ? ImageCompactFormatsBenchmarkSupport.RGBA8888
        : "JAVA_RASTER";
  }

  static boolean formatProbeAvailable() {
    return false;
  }

  static boolean metricProbeAvailable(String method) {
    return false;
  }

  static long metric(String method) {
    if ("backingBytesLiveForTest".equals(method)) {
      return NativeImageBacking.backingBytesLiveForTest();
    }
    if ("writePixelsAttemptsForTest".equals(method)) {
      return NativeImageBacking.writePixelsAttemptsForTest();
    }
    if ("writePixelsHitsForTest".equals(method)) {
      return NativeImageBacking.writePixelsHitsForTest();
    }
    if ("writePixelsFallbacksForTest".equals(method)) {
      return NativeImageBacking.writePixelsFallbacksForTest();
    }
    if ("writePixelsCopiedBytesForTest".equals(method)) {
      return NativeImageBacking.writePixelsCopiedBytesForTest();
    }
    return 0;
  }

  static void invokeStaticRequired(String className, String method) {
    // Phase-3-only failure hooks are unavailable on the true-base adapter.
  }
}
