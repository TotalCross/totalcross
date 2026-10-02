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
    assertEquals(Arrays.asList(
        ImageRuntimePolicy.PrefetchWorkerPolicy.LEGACY_PER_ENTRY_THREAD,
        ImageRuntimePolicy.PrefetchWorkerPolicy.SEMAPHORE_PROCESS_WORKER),
        Arrays.asList(ImageRuntimePolicy.PrefetchWorkerPolicy.values()));
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
    assertEquals("native compact backing is unavailable", resolved.storageReason());
    assertEquals(Collections.singletonList("image-rule-0"), resolved.matchedRuleNames());

    String report = RuntimeConfigurationReport.describe();
    assertTrue(report.contains("graphicsBackend: RASTER"));
    assertTrue(report.contains("requested: COMPACT"));
    assertTrue(report.contains("effective: STANDARD"));
    assertTrue(report.contains("reason: native compact backing is unavailable"));
    assertTrue(report.contains("zeroCopyDecode: enabled"));
    assertTrue(report.contains("targetColorConversion: disabled"));
    assertTrue(report.contains("prefetchWorker: LEGACY_PER_ENTRY_THREAD"));

    ImageRuntimeConfigurationStartup.initializeForSimulator(rules);
    String repeatedInitialization = RuntimeConfigurationReport.describe();
    assertEquals(1, occurrences(repeatedInitialization, "\nImage\n"));
  }

  @Test
  void compactCapabilityControlsEffectiveStorageWithoutChangingTheRequest() {
    ImageRuntimePolicy compact = ImageRuntimePolicy.forRequestedStorage(ImageStorageProfile.COMPACT,
        Collections.singletonList("image-rule-0"), true);
    assertEquals(ImageStorageProfile.COMPACT, compact.requestedStorageProfile());
    assertEquals(ImageStorageProfile.COMPACT, compact.effectiveStorageProfile());
    assertNull(compact.storageReason());

    ImageRuntimePolicy fallback = ImageRuntimePolicy.forRequestedStorage(ImageStorageProfile.COMPACT,
        Collections.singletonList("image-rule-0"), false);
    assertEquals(ImageStorageProfile.COMPACT, fallback.requestedStorageProfile());
    assertEquals(ImageStorageProfile.STANDARD, fallback.effectiveStorageProfile());
    assertEquals("native compact backing is unavailable", fallback.storageReason());

    ImageRuntimePolicy standard = ImageRuntimePolicy.forRequestedStorage(ImageStorageProfile.STANDARD,
        Collections.<String>emptyList(), true);
    assertEquals(ImageStorageProfile.STANDARD, standard.effectiveStorageProfile());
    assertNull(standard.storageReason());
  }

  @Test
  void internalRasterFixtureCanSelectEachP3PolicyWithoutChangingDefaults() {
    RuntimeConfigurationStartup.initializeForSimulator(null, "MacOS", "aarch64");
    ImageRuntimeConfigurationStartup.initializeForSimulator(null);
    ImageRuntimeConfigurationStartup.setRasterFeaturesForTest(false, true, true);

    ImageRuntimePolicy policy = ImageRuntimeConfigurationStartup.currentPolicy();
    assertFalse(policy.rasterCore().physicalIdentity());
    assertTrue(policy.rasterVariants().targetColorConversion());
    assertTrue(policy.rasterVariants().physicalVariantCache());

    ImageRuntimeConfigurationStartup.initializeForSimulator(null);
    policy = ImageRuntimeConfigurationStartup.currentPolicy();
    assertTrue(policy.rasterCore().physicalIdentity());
    assertFalse(policy.rasterVariants().targetColorConversion());
    assertFalse(policy.rasterVariants().physicalVariantCache());
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
