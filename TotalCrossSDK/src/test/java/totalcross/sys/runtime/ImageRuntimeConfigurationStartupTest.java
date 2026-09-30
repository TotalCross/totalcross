// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import totalcross.sys.GraphicsBackend;
import totalcross.sys.Platform;
import totalcross.sys.RuntimeFamily;
import totalcross.ui.image.ImageStorageProfile;

class ImageRuntimeConfigurationStartupTest {
  @AfterEach
  void cleanup() {
    RuntimeConfigurationStartup.initializeForSimulator(null, "MacOS", "aarch64");
    ImageRuntimeConfigurationStartup.initializeForSimulator(null);
    RuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend(GraphicsBackend.RASTER);
    ImageRuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend();
  }

  @Test
  void missingMetadataUsesStandardDefaultsWithoutMatchedRules() {
    RuntimeConfigurationStartup.initializeForSimulator(null, "MacOS", "aarch64");
    ImageRuntimeConfigurationStartup.initializeForSimulator(null);

    ImageRuntimePolicy policy = ImageRuntimeConfigurationStartup.currentPolicy();
    assertEquals(ImageStorageProfile.STANDARD, policy.requestedStorageProfile());
    assertEquals(ImageStorageProfile.STANDARD, policy.effectiveStorageProfile());
    assertNull(policy.storageReason());
    assertTrue(policy.matchedRuleNames().isEmpty());
    assertTrue(policy.rasterCore().zeroCopyDecode());
    assertTrue(policy.rasterCore().opacityMetadata());
    assertTrue(policy.rasterCore().opaqueWritePixels());
    assertTrue(policy.rasterCore().rowReadback());
    assertTrue(policy.rasterCore().directColorMaterialization());
    assertTrue(policy.rasterCore().physicalIdentity());
    assertFalse(policy.rasterVariants().targetColorConversion());
    assertFalse(policy.rasterVariants().physicalVariantCache());
    assertFalse(policy.scrollRasterReuse().enabled());
    assertFalse(policy.imagePreparation().automaticPreparation());
    assertEquals(ImageRuntimePolicy.PrefetchWorkerPolicy.LEGACY_PER_ENTRY_THREAD, policy.prefetchWorker());
  }

  @Test
  void waitsForTheFinalSimulatorBackendThenPublishesRequestedAndEffectivePolicy() {
    RuntimeConfigurationStartup.initializeForSimulator(null, "MacOS", "aarch64");
    List<RuntimeConfigurationFeatureBridge.FeatureRule<ImageStorageProfile>> rules = Collections.singletonList(
        rule("image-rule-0", RuntimeSelector.backend(GraphicsBackend.RASTER), ImageStorageProfile.COMPACT));
    ImageRuntimeConfigurationStartup.initializeForSimulator(rules);

    ImageRuntimePolicy pending = ImageRuntimeConfigurationStartup.currentPolicy();
    assertEquals(ImageStorageProfile.STANDARD, pending.requestedStorageProfile());
    assertTrue(pending.matchedRuleNames().isEmpty());

    RuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend(GraphicsBackend.RASTER);
    ImageRuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend();
    ImageRuntimePolicy resolved = ImageRuntimeConfigurationStartup.currentPolicy();
    assertEquals(ImageStorageProfile.COMPACT, resolved.requestedStorageProfile());
    assertEquals(ImageStorageProfile.STANDARD, resolved.effectiveStorageProfile());
    assertEquals("compact storage is not available in P1", resolved.storageReason());
    assertEquals(Collections.singletonList("image-rule-0"), resolved.matchedRuleNames());

    String report = RuntimeConfigurationReport.describe();
    assertTrue(report.contains("graphicsBackend: RASTER"));
    assertTrue(report.contains("requested: COMPACT"));
    assertTrue(report.contains("effective: STANDARD"));
    assertTrue(report.contains("reason: compact storage is not available in P1"));
    assertTrue(report.contains("zeroCopyDecode: enabled"));
    assertTrue(report.contains("targetColorConversion: disabled"));
    assertTrue(report.contains("prefetchWorker: LEGACY_PER_ENTRY_THREAD"));

    ImageRuntimeConfigurationStartup.initializeForSimulator(rules);
    String repeatedInitialization = RuntimeConfigurationReport.describe();
    assertEquals(1, occurrences(repeatedInitialization, "\nImage\n"));
  }

  @Test
  void equalSpecificityConflictFailsAfterBackendResolution() {
    RuntimeConfigurationStartup.initializeForSimulator(null, "MacOS", "aarch64");
    RuntimeSelector selector = RuntimeSelector.platform(Platform.MACOS)
        .and(RuntimeSelector.family(RuntimeFamily.DESKTOP));
    List<RuntimeConfigurationFeatureBridge.FeatureRule<ImageStorageProfile>> rules = Arrays.asList(
        rule("compact", selector, ImageStorageProfile.COMPACT),
        rule("standard", selector, ImageStorageProfile.STANDARD));
    ImageRuntimeConfigurationStartup.initializeForSimulator(rules);

    RuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend(GraphicsBackend.RASTER);
    assertThrows(RuntimeRuleResolver.ConfigurationConflictException.class,
        () -> ImageRuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend());
  }

  private static RuntimeConfigurationFeatureBridge.FeatureRule<ImageStorageProfile> rule(String name,
      RuntimeSelector selector, ImageStorageProfile value) {
    return new RuntimeConfigurationFeatureBridge.FeatureRule<ImageStorageProfile>(name, selector, value);
  }

  private static int occurrences(String text, String value) {
    int count = 0;
    int offset = 0;
    while ((offset = text.indexOf(value, offset)) >= 0) {
      count++;
      offset += value.length();
    }
    return count;
  }
}
