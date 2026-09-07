// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ImageOptimizationSettingsTest {
  @AfterEach
  void reset() {
    ImageOptimizationSettings.resetForTest();
  }

  @Test
  void defaultsAreTriStateAndDisabledForOptimizationCallers() {
    assertEquals(ImageOptimizationSettings.DEFAULT,
        ImageOptimizationSettings.state(ImageOptimizationSettings.DECODE_ZERO_COPY));
    assertFalse(ImageOptimizationSettings.isEnabled(ImageOptimizationSettings.DECODE_ZERO_COPY, false));
    assertTrue(ImageOptimizationSettings.isEnabled(ImageOptimizationSettings.DECODE_ZERO_COPY, true));
    assertEquals(0L, ImageOptimizationSettings.effectiveMask());
  }

  @Test
  void preservesExistingFeatureIdsAndAppendsRasterReservations() {
    assertEquals(0, ImageOptimizationSettings.DECODE_ZERO_COPY);
    assertEquals(1, ImageOptimizationSettings.RASTER_OPACITY_METADATA);
    assertEquals(2, ImageOptimizationSettings.RASTER_OPAQUE_WRITE_PIXELS);
    assertEquals(3, ImageOptimizationSettings.RASTER_ROW_READBACK);
    assertEquals(4, ImageOptimizationSettings.RASTER_DIRECT_COLOR_MATERIALIZATION);
    assertEquals(5, ImageOptimizationSettings.STORAGE_RGB565);
    assertEquals(6, ImageOptimizationSettings.STORAGE_GRAY8);
    assertEquals(7, ImageOptimizationSettings.STORAGE_ARGB4444);
    assertEquals(8, ImageOptimizationSettings.CACHE_BYTE_BUDGET);
    assertEquals(9, ImageOptimizationSettings.CACHE_MEMORY_PRESSURE_EVICTION);
    assertEquals(10, ImageOptimizationSettings.GPU_DISCARD_CPU_BACKING);
    assertEquals(11, ImageOptimizationSettings.STORAGE_MMAP_LARGE_BACKINGS);
    assertEquals(12, ImageOptimizationSettings.DIAGNOSTIC_ACCOUNTING);
    assertEquals(13, ImageOptimizationSettings.RASTER_TARGET_COLORTYPE_CONVERSION);
    assertEquals(14, ImageOptimizationSettings.RASTER_PHYSICAL_VARIANT_CACHE);
    assertEquals(15, ImageOptimizationSettings.RASTER_PHYSICAL_IDENTITY_FOLDING);
    assertEquals(16, ImageOptimizationSettings.FEATURE_COUNT);
  }

  @Test
  void stateAndFeatureValidationIsImmediate() {
    ImageOptimizationSettings.setState(ImageOptimizationSettings.STORAGE_RGB565,
        ImageOptimizationSettings.ENABLED);
    assertEquals(ImageOptimizationSettings.ENABLED,
        ImageOptimizationSettings.state(ImageOptimizationSettings.STORAGE_RGB565));
    ImageOptimizationSettings.setState(ImageOptimizationSettings.STORAGE_RGB565,
        ImageOptimizationSettings.DISABLED);
    assertEquals(ImageOptimizationSettings.DISABLED,
        ImageOptimizationSettings.state(ImageOptimizationSettings.STORAGE_RGB565));
    assertThrows(IllegalArgumentException.class,
        () -> ImageOptimizationSettings.state(-1));
    assertThrows(IllegalArgumentException.class,
        () -> ImageOptimizationSettings.state(ImageOptimizationSettings.FEATURE_COUNT));
    assertThrows(IllegalArgumentException.class,
        () -> ImageOptimizationSettings.setState(ImageOptimizationSettings.STORAGE_RGB565, -1));
    assertThrows(IllegalArgumentException.class,
        () -> ImageOptimizationSettings.setState(ImageOptimizationSettings.STORAGE_RGB565, 3));
  }

  @Test
  void effectiveMaskContainsOnlyExplicitlyEnabledFeatures() {
    ImageOptimizationSettings.setState(ImageOptimizationSettings.DECODE_ZERO_COPY,
        ImageOptimizationSettings.ENABLED);
    ImageOptimizationSettings.setState(ImageOptimizationSettings.DIAGNOSTIC_ACCOUNTING,
        ImageOptimizationSettings.ENABLED);
    ImageOptimizationSettings.setState(ImageOptimizationSettings.STORAGE_GRAY8,
        ImageOptimizationSettings.DISABLED);
    ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_TARGET_COLORTYPE_CONVERSION,
        ImageOptimizationSettings.ENABLED);
    ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_PHYSICAL_VARIANT_CACHE,
        ImageOptimizationSettings.ENABLED);
    ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_PHYSICAL_IDENTITY_FOLDING,
        ImageOptimizationSettings.DISABLED);
    long expected = (1L << ImageOptimizationSettings.DECODE_ZERO_COPY)
        | (1L << ImageOptimizationSettings.DIAGNOSTIC_ACCOUNTING)
        | (1L << ImageOptimizationSettings.RASTER_TARGET_COLORTYPE_CONVERSION)
        | (1L << ImageOptimizationSettings.RASTER_PHYSICAL_VARIANT_CACHE);
    assertEquals(expected, ImageOptimizationSettings.effectiveMask());
  }

  @Test
  void newRasterReservationsAreDefaultDisabledAndCanBeExplicitlyEnabled() {
    int[] newFeatures = {
        ImageOptimizationSettings.RASTER_TARGET_COLORTYPE_CONVERSION,
        ImageOptimizationSettings.RASTER_PHYSICAL_VARIANT_CACHE,
        ImageOptimizationSettings.RASTER_PHYSICAL_IDENTITY_FOLDING
    };
    for (int feature : newFeatures) {
      assertEquals(ImageOptimizationSettings.DEFAULT, ImageOptimizationSettings.state(feature));
      assertFalse(ImageOptimizationSettings.isEnabled(feature, false));
      ImageOptimizationSettings.setState(feature, ImageOptimizationSettings.ENABLED);
      assertTrue(ImageOptimizationSettings.isEnabled(feature, false));
    }
  }

  @Test
  void numericSettingsHaveValidatedDefaultsAndMutators() {
    assertEquals(64L * 1024 * 1024, ImageOptimizationSettings.cacheMaxBytes());
    assertEquals(4L * 1024 * 1024, ImageOptimizationSettings.mmapThresholdBytes());
    ImageOptimizationSettings.setCacheMaxBytes(123456789L);
    ImageOptimizationSettings.setMmapThresholdBytes(987654L);
    assertEquals(123456789L, ImageOptimizationSettings.cacheMaxBytes());
    assertEquals(987654L, ImageOptimizationSettings.mmapThresholdBytes());
    assertThrows(IllegalArgumentException.class,
        () -> ImageOptimizationSettings.setCacheMaxBytes(-1));
    assertThrows(IllegalArgumentException.class,
        () -> ImageOptimizationSettings.setMmapThresholdBytes(-1));
  }

  @Test
  void resetRestoresStatesNumbersAndDiagnosticGate() {
    ImageOptimizationSettings.setState(ImageOptimizationSettings.DIAGNOSTIC_ACCOUNTING,
        ImageOptimizationSettings.ENABLED);
    ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_TARGET_COLORTYPE_CONVERSION,
        ImageOptimizationSettings.ENABLED);
    ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_PHYSICAL_VARIANT_CACHE,
        ImageOptimizationSettings.ENABLED);
    ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_PHYSICAL_IDENTITY_FOLDING,
        ImageOptimizationSettings.ENABLED);
    ImageOptimizationSettings.setCacheMaxBytes(1);
    ImageOptimizationSettings.setMmapThresholdBytes(2);
    ImageOptimizationSettings.resetForTest();
    assertEquals(0L, ImageOptimizationSettings.effectiveMask());
    assertEquals(ImageOptimizationSettings.DEFAULT,
        ImageOptimizationSettings.state(ImageOptimizationSettings.RASTER_TARGET_COLORTYPE_CONVERSION));
    assertEquals(ImageOptimizationSettings.DEFAULT,
        ImageOptimizationSettings.state(ImageOptimizationSettings.RASTER_PHYSICAL_VARIANT_CACHE));
    assertEquals(ImageOptimizationSettings.DEFAULT,
        ImageOptimizationSettings.state(ImageOptimizationSettings.RASTER_PHYSICAL_IDENTITY_FOLDING));
    assertEquals(64L * 1024 * 1024, ImageOptimizationSettings.cacheMaxBytes());
    assertEquals(4L * 1024 * 1024, ImageOptimizationSettings.mmapThresholdBytes());
    assertFalse(Image.imageOperationAccountingForTest);
    assertFalse(Image.backingReadbackAccountingEnabledForTest());
    assertFalse(NativeImageBacking.backingAccountingEnabledForTest());
  }

  @Test
  void diagnosticSwitchUsesExistingAccountingAndExplicitResetStillWorks() {
    ImageOptimizationSettings.setState(ImageOptimizationSettings.DIAGNOSTIC_ACCOUNTING,
        ImageOptimizationSettings.ENABLED);
    assertTrue(Image.imageOperationAccountingForTest);
    assertTrue(Image.backingReadbackAccountingEnabledForTest());
    assertTrue(NativeImageBacking.backingAccountingEnabledForTest());
    ImageOptimizationSettings.setState(ImageOptimizationSettings.DIAGNOSTIC_ACCOUNTING,
        ImageOptimizationSettings.DISABLED);
    assertFalse(Image.imageOperationAccountingForTest);
    assertFalse(Image.backingReadbackAccountingEnabledForTest());
    assertFalse(NativeImageBacking.backingAccountingEnabledForTest());
    Image.resetImageOperationAccountingForTest();
    assertTrue(Image.imageOperationAccountingForTest);
    assertTrue(Image.backingReadbackAccountingEnabledForTest());
    assertTrue(NativeImageBacking.backingAccountingEnabledForTest());
  }

  @Test
  void diagnosticGateCoversJavaPipelineDrawPlanAndBackingReadbackAccounting() {
    ImageOptimizationSettings.setState(ImageOptimizationSettings.DIAGNOSTIC_ACCOUNTING,
        ImageOptimizationSettings.DISABLED);
    Image.clearImageOperationAccountingCountersForTest();
    Image.recordImagePipelineCreatedForTest();
    Image.recordImageDrawPlanCreatedForTest();
    Image.recordImageDrawPlanCacheHitForTest();
    Image.recordBackingReadbackForTest();
    assertEquals(0, Image.imagePipelineCreatedCountForTest());
    assertEquals(0, Image.imageDrawPlanCreatedCountForTest());
    assertEquals(0, Image.imageDrawPlanCacheHitCountForTest());
    assertEquals(0, Image.backingReadbackCountForTest());
    assertEquals(0L, NativeImageBacking.backingRecordsCreatedForTest());
    assertEquals(0L, NativeImageBacking.backingRecordsReleasedForTest());
    assertEquals(0L, NativeImageBacking.backingRecordsLiveForTest());

    ImageOptimizationSettings.setState(ImageOptimizationSettings.DIAGNOSTIC_ACCOUNTING,
        ImageOptimizationSettings.ENABLED);
    Image.clearImageOperationAccountingCountersForTest();
    Image.recordImagePipelineCreatedForTest();
    Image.recordImageDrawPlanCreatedForTest();
    Image.recordImageDrawPlanCacheHitForTest();
    Image.recordBackingReadbackForTest();
    assertEquals(1, Image.imagePipelineCreatedCountForTest());
    assertEquals(1, Image.imageDrawPlanCreatedCountForTest());
    assertEquals(1, Image.imageDrawPlanCacheHitCountForTest());
    assertEquals(1, Image.backingReadbackCountForTest());
  }

  @Test
  void counterClearPreservesTheConfiguredDiagnosticGate() {
    ImageOptimizationSettings.setState(ImageOptimizationSettings.DIAGNOSTIC_ACCOUNTING,
        ImageOptimizationSettings.ENABLED);
    Image.recordImagePipelineCreatedForTest();
    Image.recordBackingReadbackForTest();
    Image.clearImageOperationAccountingCountersForTest();
    assertTrue(Image.imageOperationAccountingForTest);
    assertTrue(Image.backingReadbackAccountingEnabledForTest());
    assertTrue(NativeImageBacking.backingAccountingEnabledForTest());
    assertEquals(0, Image.imagePipelineCreatedCountForTest());
    assertEquals(0, Image.backingReadbackCountForTest());

    ImageOptimizationSettings.setState(ImageOptimizationSettings.DIAGNOSTIC_ACCOUNTING,
        ImageOptimizationSettings.DISABLED);
    Image.recordImagePipelineCreatedForTest();
    Image.recordBackingReadbackForTest();
    Image.clearImageOperationAccountingCountersForTest();
    assertFalse(Image.imageOperationAccountingForTest);
    assertFalse(Image.backingReadbackAccountingEnabledForTest());
    assertFalse(NativeImageBacking.backingAccountingEnabledForTest());
    assertEquals(0, Image.imagePipelineCreatedCountForTest());
    assertEquals(0, Image.backingReadbackCountForTest());
  }

  @Test
  void newRasterReservationsDoNotEnableRuntimeAccountingBehavior() {
    ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_TARGET_COLORTYPE_CONVERSION,
        ImageOptimizationSettings.ENABLED);
    ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_PHYSICAL_VARIANT_CACHE,
        ImageOptimizationSettings.ENABLED);
    ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_PHYSICAL_IDENTITY_FOLDING,
        ImageOptimizationSettings.ENABLED);
    assertFalse(Image.imageOperationAccountingForTest);
    assertFalse(Image.backingReadbackAccountingEnabledForTest());
    assertFalse(NativeImageBacking.backingAccountingEnabledForTest());

    Image.clearImageOperationAccountingCountersForTest();
    Image.recordImagePipelineCreatedForTest();
    Image.recordImageDrawPlanCreatedForTest();
    Image.recordImageDrawPlanCacheHitForTest();
    Image.recordBackingReadbackForTest();
    ImageOptimizationSettings.triggerMemoryPressureForTest();
    assertEquals(0, Image.imagePipelineCreatedCountForTest());
    assertEquals(0, Image.imageDrawPlanCreatedCountForTest());
    assertEquals(0, Image.imageDrawPlanCacheHitCountForTest());
    assertEquals(0, Image.backingReadbackCountForTest());
  }

  @Test
  void descriptionIncludesEveryFeatureAndNumericSetting() {
    ImageOptimizationSettings.setState(ImageOptimizationSettings.CACHE_BYTE_BUDGET,
        ImageOptimizationSettings.ENABLED);
    ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_TARGET_COLORTYPE_CONVERSION,
        ImageOptimizationSettings.ENABLED);
    ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_PHYSICAL_VARIANT_CACHE,
        ImageOptimizationSettings.DISABLED);
    String description = ImageOptimizationSettings.describeForTest();
    assertTrue(description.contains("CACHE_BYTE_BUDGET=ENABLED"));
    assertTrue(description.contains("DIAGNOSTIC_ACCOUNTING=DEFAULT"));
    assertTrue(description.contains("RASTER_TARGET_COLORTYPE_CONVERSION=ENABLED"));
    assertTrue(description.contains("RASTER_PHYSICAL_VARIANT_CACHE=DISABLED"));
    assertTrue(description.contains("RASTER_PHYSICAL_IDENTITY_FOLDING=DEFAULT"));
    assertTrue(description.contains("cacheMaxBytes=67108864"));
    assertTrue(description.contains("mmapThresholdBytes=4194304"));
  }

  @Test
  void memoryPressureHookIsSafeBeforeTheManagerExists() {
    ImageOptimizationSettings.triggerMemoryPressureForTest();
  }
}
