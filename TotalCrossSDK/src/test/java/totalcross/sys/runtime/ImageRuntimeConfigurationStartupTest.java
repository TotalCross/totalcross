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
import totalcross.sys.RuntimeDiagnostics;
import totalcross.sys.RuntimeDiagnosticSnapshot;
import totalcross.ui.image.ImagePrefetchWorkerMode;
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
    assertEquals(ImagePrefetchWorkerMode.LEGACY_PER_ENTRY_THREAD, policy.prefetchWorker());
    assertTrue(RuntimeConfigurationReport.describe().contains("reason: none"));
    assertEquals(Arrays.asList(
        ImagePrefetchWorkerMode.LEGACY_PER_ENTRY_THREAD,
        ImagePrefetchWorkerMode.SEMAPHORE_PROCESS_WORKER),
        Arrays.asList(ImagePrefetchWorkerMode.LEGACY_PER_ENTRY_THREAD,
            ImagePrefetchWorkerMode.SEMAPHORE_PROCESS_WORKER));
  }

  @Test
  void waitsForTheFinalSimulatorBackendThenPublishesRequestedAndEffectivePolicy() {
    RuntimeConfigurationStartup.initializeForSimulator(null, "MacOS", "aarch64");
    List<RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>> rules = Collections.singletonList(
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
    assertTrue(report.contains("physicalVariantCache: disabled"));
    assertTrue(report.contains("scrollRasterReuse: disabled"));
    assertTrue(report.contains("automaticPreparation: disabled"));
    assertTrue(report.contains("prefetchWorker: LEGACY_PER_ENTRY_THREAD"));
    assertTrue(report.contains("matchedRules: image-rule-0"));

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
    List<RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>> rules = Arrays.asList(
        rule("compact", selector, ImageStorageProfile.COMPACT),
        rule("standard", selector, ImageStorageProfile.STANDARD));
    ImageRuntimeConfigurationStartup.initializeForSimulator(rules);

    RuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend(GraphicsBackend.RASTER);
    assertThrows(RuntimeRuleResolver.ConfigurationConflictException.class,
        () -> ImageRuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend());
  }

  @Test
  void composesIndependentDesktopWindowsAndRasterAssignments() {
    RuntimeConfigurationStartup.initializeForSimulator(null, "Windows", "amd64");
    RuntimeSelector desktop = RuntimeSelector.family(RuntimeFamily.DESKTOP);
    RuntimeSelector windows = RuntimeSelector.platform(Platform.WINDOWS);
    RuntimeSelector windowsRaster = RuntimeSelector.platform(Platform.WINDOWS)
        .and(RuntimeSelector.backend(GraphicsBackend.RASTER));
    List<RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>> rules = Arrays.asList(
        optionRule("desktop-storage", desktop, options(ImageStorageProfile.COMPACT)),
        optionRule("windows-scroll", windows, options(RuntimeFeatureState.DEFAULT, RuntimeFeatureState.DEFAULT,
            RuntimeFeatureState.ENABLED, ImagePrefetchWorkerMode.DEFAULT)),
        optionRule("windows-raster-worker", windowsRaster,
            options(RuntimeFeatureState.DEFAULT, RuntimeFeatureState.DEFAULT, RuntimeFeatureState.DEFAULT,
                ImagePrefetchWorkerMode.SEMAPHORE_PROCESS_WORKER)));
    ImageRuntimeConfigurationStartup.initializeForSimulator(rules);

    RuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend(GraphicsBackend.RASTER);
    ImageRuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend();
    ImageRuntimePolicy policy = ImageRuntimeConfigurationStartup.currentPolicy();
    assertEquals(ImageStorageProfile.COMPACT, policy.requestedStorageProfile());
    assertEquals(ImageStorageProfile.STANDARD, policy.effectiveStorageProfile());
    assertFalse(policy.rasterVariants().targetColorConversion());
    assertFalse(policy.rasterVariants().physicalVariantCache());
    assertTrue(policy.scrollRasterReuse().enabled());
    assertEquals(ImagePrefetchWorkerMode.SEMAPHORE_PROCESS_WORKER, policy.prefetchWorker());
    assertEquals(3, policy.matchedRuleNames().size());
    assertTrue(policy.matchedRuleNames().contains("desktop-storage"));
    assertTrue(policy.matchedRuleNames().contains("windows-scroll"));
    assertTrue(policy.matchedRuleNames().contains("windows-raster-worker"));
  }

  @Test
  void resolvesSpecificOverridesEqualSpecificityCompositionAndDefaultAsUnassigned() {
    RuntimeConfigurationStartup.initializeForSimulator(null, "Windows", "amd64");
    RuntimeSelector desktop = RuntimeSelector.family(RuntimeFamily.DESKTOP);
    RuntimeSelector windowsRaster = RuntimeSelector.platform(Platform.WINDOWS)
        .and(RuntimeSelector.backend(GraphicsBackend.RASTER));
    List<RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>> rules = Arrays.asList(
        optionRule("broad-enabled", desktop,
            options(RuntimeFeatureState.ENABLED, RuntimeFeatureState.DEFAULT, RuntimeFeatureState.DEFAULT,
                ImagePrefetchWorkerMode.DEFAULT)),
        optionRule("specific-disabled", windowsRaster,
            options(RuntimeFeatureState.DISABLED, RuntimeFeatureState.DEFAULT, RuntimeFeatureState.DEFAULT,
                ImagePrefetchWorkerMode.DEFAULT)),
        optionRule("specific-duplicate", windowsRaster,
            options(RuntimeFeatureState.DISABLED, RuntimeFeatureState.DEFAULT, RuntimeFeatureState.DEFAULT,
                ImagePrefetchWorkerMode.DEFAULT)),
        optionRule("specific-other-property", windowsRaster,
            options(RuntimeFeatureState.DEFAULT, RuntimeFeatureState.ENABLED, RuntimeFeatureState.DEFAULT,
                ImagePrefetchWorkerMode.DEFAULT)),
        optionRule("default-does-not-override", windowsRaster,
            options(RuntimeFeatureState.DEFAULT, RuntimeFeatureState.DEFAULT, RuntimeFeatureState.ENABLED,
                ImagePrefetchWorkerMode.DEFAULT)));
    ImageRuntimeConfigurationStartup.initializeForSimulator(rules);

    RuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend(GraphicsBackend.RASTER);
    ImageRuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend();
    ImageRuntimePolicy policy = ImageRuntimeConfigurationStartup.currentPolicy();
    assertFalse(policy.rasterVariants().targetColorConversion());
    assertTrue(policy.rasterVariants().physicalVariantCache());
    assertTrue(policy.scrollRasterReuse().enabled());
  }

  @Test
  void combinesAllFivePropertiesAndKeepsStableRasterCoreAndPreparationDefaults() {
    RuntimeConfigurationStartup.initializeForSimulator(null, "Windows", "amd64");
    RuntimeSelector eligible = RuntimeSelector.platform(Platform.WINDOWS)
        .and(RuntimeSelector.family(RuntimeFamily.DESKTOP))
        .and(RuntimeSelector.backend(GraphicsBackend.RASTER));
    List<RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>> rules = Arrays.asList(
        optionRule("storage", eligible, options(ImageStorageProfile.COMPACT)),
        optionRule("target", eligible, options(RuntimeFeatureState.ENABLED, RuntimeFeatureState.DEFAULT,
            RuntimeFeatureState.DEFAULT, ImagePrefetchWorkerMode.DEFAULT)),
        optionRule("cache", eligible, options(RuntimeFeatureState.DEFAULT, RuntimeFeatureState.ENABLED,
            RuntimeFeatureState.DEFAULT, ImagePrefetchWorkerMode.DEFAULT)),
        optionRule("scroll", eligible, options(RuntimeFeatureState.DEFAULT, RuntimeFeatureState.DEFAULT,
            RuntimeFeatureState.ENABLED, ImagePrefetchWorkerMode.DEFAULT)),
        optionRule("worker", eligible, options(RuntimeFeatureState.DEFAULT, RuntimeFeatureState.DEFAULT,
            RuntimeFeatureState.DEFAULT, ImagePrefetchWorkerMode.SEMAPHORE_PROCESS_WORKER)));
    ImageRuntimeConfigurationStartup.initializeForSimulator(rules);

    RuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend(GraphicsBackend.RASTER);
    ImageRuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend();
    ImageRuntimePolicy policy = ImageRuntimeConfigurationStartup.currentPolicy();
    assertEquals(ImageStorageProfile.COMPACT, policy.requestedStorageProfile());
    assertTrue(policy.rasterVariants().targetColorConversion());
    assertTrue(policy.rasterVariants().physicalVariantCache());
    assertTrue(policy.scrollRasterReuse().enabled());
    assertEquals(ImagePrefetchWorkerMode.SEMAPHORE_PROCESS_WORKER, policy.prefetchWorker());
    assertTrue(policy.rasterCore().zeroCopyDecode());
    assertTrue(policy.rasterCore().opacityMetadata());
    assertTrue(policy.rasterCore().opaqueWritePixels());
    assertTrue(policy.rasterCore().rowReadback());
    assertTrue(policy.rasterCore().directColorMaterialization());
    assertTrue(policy.rasterCore().physicalIdentity());
    assertFalse(policy.imagePreparation().automaticPreparation());
  }

  @Test
  void reportsPropertySpecificDeterministicEqualSpecificityConflict() {
    RuntimeConfigurationStartup.initializeForSimulator(null, "Windows", "amd64");
    RuntimeSelector same = RuntimeSelector.platform(Platform.WINDOWS);
    List<RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>> rules = Arrays.asList(
        optionRule("a-enabled", same,
            options(RuntimeFeatureState.ENABLED, RuntimeFeatureState.DEFAULT, RuntimeFeatureState.DEFAULT,
                ImagePrefetchWorkerMode.DEFAULT)),
        optionRule("z-disabled", same,
            options(RuntimeFeatureState.DISABLED, RuntimeFeatureState.DEFAULT, RuntimeFeatureState.DEFAULT,
                ImagePrefetchWorkerMode.DEFAULT)));
    ImageRuntimeConfigurationStartup.initializeForSimulator(rules);
    RuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend(GraphicsBackend.RASTER);
    RuntimeRuleResolver.ConfigurationConflictException failure = assertThrows(
        RuntimeRuleResolver.ConfigurationConflictException.class,
        () -> ImageRuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend());
    assertTrue(failure.getMessage().contains("targetColorConversion"));
    assertTrue(failure.getMessage().contains("a-enabled"));
    assertTrue(failure.getMessage().contains("z-disabled"));

    RuntimeConfigurationStartup.initializeForSimulator(null, "Windows", "amd64");
    ImageRuntimeConfigurationStartup.initializeForSimulator(Arrays.asList(rules.get(1), rules.get(0)));
    RuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend(GraphicsBackend.RASTER);
    RuntimeRuleResolver.ConfigurationConflictException reversedFailure = assertThrows(
        RuntimeRuleResolver.ConfigurationConflictException.class,
        () -> ImageRuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend());
    assertEquals(failure.getMessage(), reversedFailure.getMessage());
  }

  @Test
  void explicitLegacyWorkerAndUnassignedWorkerBothResolveToLegacy() {
    RuntimeConfigurationStartup.initializeForSimulator(null, "Windows", "amd64");
    assertEquals(ImagePrefetchWorkerMode.LEGACY_PER_ENTRY_THREAD,
        ImageRuntimeConfigurationStartup.currentPolicy().prefetchWorker());
    RuntimeSelector windows = RuntimeSelector.platform(Platform.WINDOWS);
    ImageRuntimeConfigurationStartup.initializeForSimulator(Collections.singletonList(optionRule("legacy", windows,
        options(RuntimeFeatureState.DEFAULT, RuntimeFeatureState.DEFAULT, RuntimeFeatureState.DEFAULT,
            ImagePrefetchWorkerMode.LEGACY_PER_ENTRY_THREAD))));
    RuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend(GraphicsBackend.RASTER);
    ImageRuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend();
    assertEquals(ImagePrefetchWorkerMode.LEGACY_PER_ENTRY_THREAD,
        ImageRuntimeConfigurationStartup.currentPolicy().prefetchWorker());
  }

  @Test
  void diagnosticsImageDomainDoesNotChangeResolvedConfiguration() {
    RuntimeSelector windows = RuntimeSelector.platform(Platform.WINDOWS);
    List<RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>> rules = Collections.singletonList(
        optionRule("settings", windows, options(RuntimeFeatureState.ENABLED, RuntimeFeatureState.DISABLED,
            RuntimeFeatureState.ENABLED, ImagePrefetchWorkerMode.SEMAPHORE_PROCESS_WORKER)));
    try {
      RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.IMAGE, false);
      RuntimeConfigurationStartup.initializeForSimulator(null, "Windows", "amd64");
      ImageRuntimeConfigurationStartup.initializeForSimulator(rules);
      RuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend(GraphicsBackend.RASTER);
      ImageRuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend();
      ImageRuntimePolicy disabledDiagnostics = ImageRuntimeConfigurationStartup.currentPolicy();

      RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.IMAGE, true);
      RuntimeConfigurationStartup.initializeForSimulator(null, "Windows", "amd64");
      ImageRuntimeConfigurationStartup.initializeForSimulator(rules);
      RuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend(GraphicsBackend.RASTER);
      ImageRuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend();
      ImageRuntimePolicy enabledDiagnostics = ImageRuntimeConfigurationStartup.currentPolicy();

      assertEquals(disabledDiagnostics.requestedStorageProfile(), enabledDiagnostics.requestedStorageProfile());
      assertEquals(disabledDiagnostics.rasterVariants().targetColorConversion(),
          enabledDiagnostics.rasterVariants().targetColorConversion());
      assertEquals(disabledDiagnostics.rasterVariants().physicalVariantCache(),
          enabledDiagnostics.rasterVariants().physicalVariantCache());
      assertEquals(disabledDiagnostics.scrollRasterReuse().enabled(),
          enabledDiagnostics.scrollRasterReuse().enabled());
      assertEquals(disabledDiagnostics.prefetchWorker(), enabledDiagnostics.prefetchWorker());
    } finally {
      RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.IMAGE, false);
    }
  }

  private static RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions> rule(String name,
      RuntimeSelector selector, ImageStorageProfile value) {
    return new RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>(name, selector,
        ImageRuntimeOptions.storageOnly(value));
  }

  private static RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions> optionRule(String name,
      RuntimeSelector selector, ImageRuntimeOptions value) {
    return new RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>(name, selector, value);
  }

  private static ImageRuntimeOptions options(ImageStorageProfile storage) {
    return new ImageRuntimeOptions(storage, RuntimeFeatureState.DEFAULT, RuntimeFeatureState.DEFAULT,
        RuntimeFeatureState.DEFAULT, ImagePrefetchWorkerMode.DEFAULT);
  }

  private static ImageRuntimeOptions options(RuntimeFeatureState targetColorConversion,
      RuntimeFeatureState physicalVariantCache, RuntimeFeatureState scrollRasterReuse,
      ImagePrefetchWorkerMode prefetchWorker) {
    return new ImageRuntimeOptions(ImageStorageProfile.DEFAULT, targetColorConversion, physicalVariantCache,
        scrollRasterReuse, prefetchWorker);
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
