// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

/** Package-private benchmark seam for native compact backing introspection. */
final class ImageCompactFormatsNativeHooks {
  private ImageCompactFormatsNativeHooks() {
  }

  static Image materialize(Image image) {
    image.materializeNativeBackingForTest();
    return image;
  }

  static String format(Image image) {
    if (!image.hasNativeBackingForSmoke()) {
      return "JAVA_RASTER";
    }
    switch (image.backing instanceof NativeImageBacking
        ? ((NativeImageBacking) image.backing).currentFormatForTest() : -1) {
    case 0:
      return ImageCompactFormatsBenchmarkSupport.RGBA8888;
    case 1:
      return ImageCompactFormatsBenchmarkSupport.RGB565;
    case 2:
      return ImageCompactFormatsBenchmarkSupport.GRAY8;
    case 3:
      return ImageCompactFormatsBenchmarkSupport.ARGB4444;
    default:
      return "UNKNOWN";
    }
  }

  static boolean formatProbeAvailable() {
    return NativeImageBacking.formatProbeAvailableNative();
  }

  static boolean metricProbeAvailable(String method) {
    return "backingBytesLiveForTest".equals(method)
        || "writePixelsAttemptsForTest".equals(method)
        || "writePixelsHitsForTest".equals(method)
        || "writePixelsFallbacksForTest".equals(method)
        || "writePixelsCopiedBytesForTest".equals(method)
        || "rgba8888BackingBytesForTest".equals(method)
        || "rgb565BackingBytesForTest".equals(method)
        || "gray8BackingBytesForTest".equals(method)
        || "argb4444BackingBytesForTest".equals(method)
        || "compactDirectDecodeCountForTest".equals(method)
        || "compactDirectDecodeBytesForTest".equals(method)
        || "temporaryRgbaDecodeBytesForTest".equals(method)
        || "compactReadbackCountForTest".equals(method)
        || "compactRowScratchPeakBytesForTest".equals(method)
        || "promotionAttemptsForTest".equals(method)
        || "promotionSuccessesForTest".equals(method)
        || "promotionFailuresForTest".equals(method)
        || "promotionBytesForTest".equals(method)
        || "failNextCompactDecodeAfterAllocationForTest".equals(method)
        || "failNextPromotionForTest".equals(method);
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
    if ("rgba8888BackingBytesForTest".equals(method)) {
      return NativeImageBacking.rgba8888BackingBytesForTest();
    }
    if ("rgb565BackingBytesForTest".equals(method)) {
      return NativeImageBacking.rgb565BackingBytesForTest();
    }
    if ("gray8BackingBytesForTest".equals(method)) {
      return NativeImageBacking.gray8BackingBytesForTest();
    }
    if ("argb4444BackingBytesForTest".equals(method)) {
      return NativeImageBacking.argb4444BackingBytesForTest();
    }
    if ("compactDirectDecodeCountForTest".equals(method)) {
      return NativeImageBacking.compactDirectDecodeCountForTest();
    }
    if ("compactDirectDecodeBytesForTest".equals(method)) {
      return NativeImageBacking.compactDirectDecodeBytesForTest();
    }
    if ("temporaryRgbaDecodeBytesForTest".equals(method)) {
      return NativeImageBacking.temporaryRgbaDecodeBytesForTest();
    }
    if ("compactReadbackCountForTest".equals(method)) {
      return NativeImageBacking.compactReadbackCountForTest();
    }
    if ("compactRowScratchPeakBytesForTest".equals(method)) {
      return NativeImageBacking.compactRowScratchPeakBytesForTest();
    }
    if ("promotionAttemptsForTest".equals(method)) {
      return NativeImageBacking.promotionAttemptsForTest();
    }
    if ("promotionSuccessesForTest".equals(method)) {
      return NativeImageBacking.promotionSuccessesForTest();
    }
    if ("promotionFailuresForTest".equals(method)) {
      return NativeImageBacking.promotionFailuresForTest();
    }
    if ("promotionBytesForTest".equals(method)) {
      return NativeImageBacking.promotionBytesForTest();
    }
    return 0;
  }

  static void invokeStaticRequired(String className, String method) {
    if ("totalcross.ui.image.Image".equals(className)
        && "failNextCompactDecodeAfterAllocationForTest".equals(method)) {
      Image.failNextZeroCopyDecodeAfterAllocationForTest();
    } else if ("totalcross.ui.image.NativeImageBacking".equals(className)
        && "failNextPromotionForTest".equals(method)) {
      NativeImageBacking.failNextPromotionForTest();
    } else {
      throw new IllegalArgumentException("Unsupported test hook " + className + "." + method);
    }
  }
}
