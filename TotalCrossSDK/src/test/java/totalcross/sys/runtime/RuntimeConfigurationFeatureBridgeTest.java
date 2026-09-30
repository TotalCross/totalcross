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

  private static RuntimeConfigurationFeatureBridge.FeatureRule<ImageStorageProfile> rule(String name,
      RuntimeSelector selector, ImageStorageProfile value) {
    return new RuntimeConfigurationFeatureBridge.FeatureRule<ImageStorageProfile>(name, selector, value);
  }
}
