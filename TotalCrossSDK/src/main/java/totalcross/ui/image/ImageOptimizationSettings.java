// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

/** Internal process-global switches reserved for the image optimization series. */
final class ImageOptimizationSettings {
  static final int DEFAULT = 0;
  static final int ENABLED = 1;
  static final int DISABLED = 2;

  static final int DECODE_ZERO_COPY = 0;
  static final int RASTER_OPACITY_METADATA = 1;
  static final int RASTER_OPAQUE_WRITE_PIXELS = 2;
  static final int RASTER_ROW_READBACK = 3;
  static final int RASTER_DIRECT_COLOR_MATERIALIZATION = 4;
  static final int STORAGE_RGB565 = 5;
  static final int STORAGE_GRAY8 = 6;
  static final int STORAGE_ARGB4444 = 7;
  static final int CACHE_BYTE_BUDGET = 8;
  static final int CACHE_MEMORY_PRESSURE_EVICTION = 9;
  static final int GPU_DISCARD_CPU_BACKING = 10;
  static final int STORAGE_MMAP_LARGE_BACKINGS = 11;
  static final int DIAGNOSTIC_ACCOUNTING = 12;
  static final int FEATURE_COUNT = 13;

  private static final long DEFAULT_CACHE_MAX_BYTES = 64L * 1024 * 1024;
  private static final long DEFAULT_MMAP_THRESHOLD_BYTES = 4L * 1024 * 1024;
  private static final int[] states = new int[FEATURE_COUNT];
  private static long cacheMaxBytes = DEFAULT_CACHE_MAX_BYTES;
  private static long mmapThresholdBytes = DEFAULT_MMAP_THRESHOLD_BYTES;

  private ImageOptimizationSettings() {
  }

  static void setState(int feature, int state) {
    checkFeature(feature);
    checkState(state);
    states[feature] = state;
    if (feature == DIAGNOSTIC_ACCOUNTING) {
      Image.setDiagnosticAccountingForTest(state == ENABLED);
    }
    long mask = effectiveMask();
    Image.setNativeOptimizationMaskForDrawForTest(mask);
    Image.setNativeOptimizationMaskForDecodeForTest(mask);
  }

  static int state(int feature) {
    checkFeature(feature);
    return states[feature];
  }

  static boolean isEnabled(int feature, boolean defaultEnabled) {
    int featureState = state(feature);
    return featureState == ENABLED || featureState == DEFAULT && defaultEnabled;
  }

  static long effectiveMask() {
    long mask = 0;
    for (int feature = 0; feature < FEATURE_COUNT; feature++) {
      if (states[feature] == ENABLED) {
        mask |= 1L << feature;
      }
    }
    return mask;
  }

  static void setCacheMaxBytes(long value) {
    if (value < 0) {
      throw new IllegalArgumentException("cacheMaxBytes must not be negative");
    }
    cacheMaxBytes = value;
  }

  static long cacheMaxBytes() {
    return cacheMaxBytes;
  }

  static void setMmapThresholdBytes(long value) {
    if (value < 0) {
      throw new IllegalArgumentException("mmapThresholdBytes must not be negative");
    }
    mmapThresholdBytes = value;
  }

  static long mmapThresholdBytes() {
    return mmapThresholdBytes;
  }

  static void resetForTest() {
    for (int feature = 0; feature < FEATURE_COUNT; feature++) {
      states[feature] = DEFAULT;
    }
    cacheMaxBytes = DEFAULT_CACHE_MAX_BYTES;
    mmapThresholdBytes = DEFAULT_MMAP_THRESHOLD_BYTES;
    Image.setDiagnosticAccountingForTest(false);
    Image.setNativeOptimizationMaskForDrawForTest(0);
    Image.setNativeOptimizationMaskForDecodeForTest(0);
  }

  static void triggerMemoryPressureForTest() {
    // Connected to the real backing manager by phase 4.
  }

  static String describeForTest() {
    StringBuilder description = new StringBuilder(256);
    appendFeature(description, "DECODE_ZERO_COPY", DECODE_ZERO_COPY);
    appendFeature(description, "RASTER_OPACITY_METADATA", RASTER_OPACITY_METADATA);
    appendFeature(description, "RASTER_OPAQUE_WRITE_PIXELS", RASTER_OPAQUE_WRITE_PIXELS);
    appendFeature(description, "RASTER_ROW_READBACK", RASTER_ROW_READBACK);
    appendFeature(description, "RASTER_DIRECT_COLOR_MATERIALIZATION", RASTER_DIRECT_COLOR_MATERIALIZATION);
    appendFeature(description, "STORAGE_RGB565", STORAGE_RGB565);
    appendFeature(description, "STORAGE_GRAY8", STORAGE_GRAY8);
    appendFeature(description, "STORAGE_ARGB4444", STORAGE_ARGB4444);
    appendFeature(description, "CACHE_BYTE_BUDGET", CACHE_BYTE_BUDGET);
    appendFeature(description, "CACHE_MEMORY_PRESSURE_EVICTION", CACHE_MEMORY_PRESSURE_EVICTION);
    appendFeature(description, "GPU_DISCARD_CPU_BACKING", GPU_DISCARD_CPU_BACKING);
    appendFeature(description, "STORAGE_MMAP_LARGE_BACKINGS", STORAGE_MMAP_LARGE_BACKINGS);
    appendFeature(description, "DIAGNOSTIC_ACCOUNTING", DIAGNOSTIC_ACCOUNTING);
    description.append(",cacheMaxBytes=").append(cacheMaxBytes)
        .append(",mmapThresholdBytes=").append(mmapThresholdBytes);
    return description.toString();
  }

  private static void appendFeature(StringBuilder description, String name, int feature) {
    if (description.length() > 0) {
      description.append(',');
    }
    description.append(name).append('=').append(stateName(states[feature]));
  }

  private static String stateName(int state) {
    switch (state) {
    case DEFAULT:
      return "DEFAULT";
    case ENABLED:
      return "ENABLED";
    case DISABLED:
      return "DISABLED";
    default:
      throw new IllegalArgumentException("Invalid optimization state: " + state);
    }
  }

  private static void checkFeature(int feature) {
    if (feature < 0 || feature >= FEATURE_COUNT) {
      throw new IllegalArgumentException("Invalid optimization feature: " + feature);
    }
  }

  private static void checkState(int state) {
    if (state < DEFAULT || state > DISABLED) {
      throw new IllegalArgumentException("Invalid optimization state: " + state);
    }
  }
}
