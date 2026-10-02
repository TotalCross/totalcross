// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import totalcross.sys.Architecture;
import totalcross.sys.Platform;
import totalcross.sys.RuntimeFamily;
import totalcross.ui.image.ImagePrefetchWorkerMode;
import totalcross.ui.image.ImageStorageProfile;

class RuntimeConfigurationFeatureBridgeTest {
  @Test
  void usesSharedSpecificityResolutionRegardlessOfDeclarationOrder() {
    RuntimeEnvironment environment = RuntimeEnvironmentTestSupport.environment("MacOS", 4, false);
    RuntimeSelector broad = RuntimeSelector.family(RuntimeFamily.DESKTOP);
    RuntimeSelector specific = RuntimeSelector.platform(Platform.MACOS)
        .and(RuntimeSelector.family(RuntimeFamily.DESKTOP))
        .and(RuntimeSelector.architecture(Architecture.ARM64));
    RuntimeConfigurationFeatureBridge.FeatureRule<ImageStorageProfile> broadRule = rule("broad", broad,
        ImageStorageProfile.COMPACT);
    RuntimeConfigurationFeatureBridge.FeatureRule<ImageStorageProfile> specificRule = rule("specific", specific,
        ImageStorageProfile.STANDARD);

    assertEquals(ImageStorageProfile.STANDARD, RuntimeConfigurationFeatureBridge.resolveSingleSetting(environment,
        Arrays.asList(broadRule, specificRule), ImageStorageProfile.STANDARD).requestedValue());
    assertEquals(ImageStorageProfile.STANDARD, RuntimeConfigurationFeatureBridge.resolveSingleSetting(environment,
        Arrays.asList(specificRule, broadRule), ImageStorageProfile.STANDARD).requestedValue());
  }

  @Test
  void appliesDefaultAndRejectsEqualSpecificityConflictsDeterministically() {
    RuntimeEnvironment environment = RuntimeEnvironmentTestSupport.environment("MacOS", 4, false);
    assertEquals(ImageStorageProfile.STANDARD, RuntimeConfigurationFeatureBridge.resolveSingleSetting(environment,
        Collections.<RuntimeConfigurationFeatureBridge.FeatureRule<ImageStorageProfile>>emptyList(),
        ImageStorageProfile.STANDARD).requestedValue());

    RuntimeSelector same = RuntimeSelector.platform(Platform.MACOS)
        .and(RuntimeSelector.family(RuntimeFamily.DESKTOP));
    RuntimeConfigurationFeatureBridge.FeatureRule<ImageStorageProfile> compact = rule("a-compact", same,
        ImageStorageProfile.COMPACT);
    RuntimeConfigurationFeatureBridge.FeatureRule<ImageStorageProfile> standard = rule("z-standard", same,
        ImageStorageProfile.STANDARD);
    List<RuntimeConfigurationFeatureBridge.FeatureRule<ImageStorageProfile>> rules = Arrays.asList(compact, standard);

    assertThrows(RuntimeRuleResolver.ConfigurationConflictException.class,
        () -> RuntimeConfigurationFeatureBridge.resolveSingleSetting(environment, rules,
            ImageStorageProfile.STANDARD));
    assertThrows(RuntimeRuleResolver.ConfigurationConflictException.class,
        () -> RuntimeConfigurationFeatureBridge.resolveSingleSetting(environment,
            Arrays.asList(standard, compact), ImageStorageProfile.STANDARD));
  }

  @Test
  void projectsOneOptionalPropertyThroughTheSharedSpecificityResolver() {
    RuntimeEnvironment environment = RuntimeEnvironmentTestSupport.environment("MacOS", 4, false);
    RuntimeSelector broad = RuntimeSelector.family(RuntimeFamily.DESKTOP);
    RuntimeSelector specific = RuntimeSelector.platform(Platform.MACOS)
        .and(RuntimeSelector.family(RuntimeFamily.DESKTOP));
    List<RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>> rules = Arrays.asList(
        new RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>("broad", broad,
            ImageRuntimeOptions.storageOnly(ImageStorageProfile.COMPACT)),
        new RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>("specific", specific,
            new ImageRuntimeOptions(ImageStorageProfile.DEFAULT, RuntimeFeatureState.DEFAULT,
                RuntimeFeatureState.DEFAULT, RuntimeFeatureState.DEFAULT,
                ImagePrefetchWorkerMode.LEGACY_PER_ENTRY_THREAD)));

    RuntimeConfigurationFeatureBridge.FeatureResolution<ImageStorageProfile> resolution =
        RuntimeConfigurationFeatureBridge.resolveProjectedSetting(environment, rules,
            new RuntimeConfigurationFeatureBridge.ValueProjector<ImageRuntimeOptions, ImageStorageProfile>() {
              @Override
              public ImageStorageProfile project(ImageRuntimeOptions value) {
                return value.storage() == ImageStorageProfile.DEFAULT ? null : value.storage();
              }
            }, ImageStorageProfile.STANDARD, "storage");

    assertEquals(ImageStorageProfile.COMPACT, resolution.requestedValue());
    assertEquals(Collections.singletonList("broad"), resolution.matchedRuleNames());
  }

  private static RuntimeConfigurationFeatureBridge.FeatureRule<ImageStorageProfile> rule(String name,
      RuntimeSelector selector, ImageStorageProfile value) {
    return new RuntimeConfigurationFeatureBridge.FeatureRule<ImageStorageProfile>(name, selector, value);
  }
}
