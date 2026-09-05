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
    long expected = (1L << ImageOptimizationSettings.DECODE_ZERO_COPY)
        | (1L << ImageOptimizationSettings.DIAGNOSTIC_ACCOUNTING);
    assertEquals(expected, ImageOptimizationSettings.effectiveMask());
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
    ImageOptimizationSettings.setCacheMaxBytes(1);
    ImageOptimizationSettings.setMmapThresholdBytes(2);
    ImageOptimizationSettings.resetForTest();
    assertEquals(0L, ImageOptimizationSettings.effectiveMask());
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
  }

  @Test
  void descriptionIncludesEveryFeatureAndNumericSetting() {
    ImageOptimizationSettings.setState(ImageOptimizationSettings.CACHE_BYTE_BUDGET,
        ImageOptimizationSettings.ENABLED);
    String description = ImageOptimizationSettings.describeForTest();
    assertTrue(description.contains("CACHE_BYTE_BUDGET=ENABLED"));
    assertTrue(description.contains("DIAGNOSTIC_ACCOUNTING=DEFAULT"));
    assertTrue(description.contains("cacheMaxBytes=67108864"));
    assertTrue(description.contains("mmapThresholdBytes=4194304"));
  }

  @Test
  void memoryPressureHookIsSafeBeforeTheManagerExists() {
    ImageOptimizationSettings.triggerMemoryPressureForTest();
  }
}
