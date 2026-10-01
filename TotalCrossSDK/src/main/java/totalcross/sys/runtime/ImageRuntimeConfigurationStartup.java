// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import totalcross.sys.Vm;
import totalcross.ui.image.ImageCompactStorageCapabilityBridge;
import totalcross.ui.image.ImageStorageProfile;

/** Internal startup binding for resolving the optional Image TCZ configuration resource. */
public final class ImageRuntimeConfigurationStartup {
  private static final String RESOURCE_NAME = "tc.imageruntimeconfig";

  private static boolean startupAttempted;
  private static List<RuntimeConfigurationFeatureBridge.FeatureRule<ImageStorageProfile>> pendingRules;
  private static volatile ImageRuntimePolicy currentPolicy = ImageRuntimePolicy.defaults();

  private ImageRuntimeConfigurationStartup() {
  }

  /** Called by the native VM after runtime environment and graphics setup. */
  public static synchronized void initializeAtStartup() {
    if (startupAttempted) {
      resolveIfReady();
      return;
    }
    startupAttempted = true;
    registerDescriptionContributor();
    byte[] bytes = Vm.getFile(RESOURCE_NAME);
    initializeRules(bytes == null ? null : ImageRuntimeConfigurationMetadata.decode(bytes));
  }

  /** Called by the simulator after parsing Image annotations and after B sets host facts. */
  public static synchronized void initializeForSimulator(
      List<RuntimeConfigurationFeatureBridge.FeatureRule<ImageStorageProfile>> rules) {
    startupAttempted = true;
    registerDescriptionContributor();
    initializeRules(rules);
  }

  /** Called after B finalizes the simulator's actual graphics backend. */
  public static synchronized void finalizeSimulatorGraphicsBackend() {
    resolveIfReady();
  }

  /** Returns the immutable Image policy snapshot, resolving pending simulator rules when ready. */
  public static synchronized ImageRuntimePolicy currentPolicy() {
    resolveIfReady();
    return currentPolicy;
  }

  /** Internal native-decoder query for the already resolved storage capability. */
  public static boolean compactStorageEnabledForNative() {
    return currentPolicy().effectiveStorageProfile() == ImageStorageProfile.COMPACT;
  }

  /** Internal fixture for exercising opt-in raster features without public configuration. */
  static synchronized void setRasterFeaturesForTest(boolean physicalIdentity,
      boolean targetColorConversion, boolean physicalVariantCache) {
    resolveIfReady();
    currentPolicy = currentPolicy.withRasterFeaturesForTest(physicalIdentity,
        targetColorConversion, physicalVariantCache);
  }

  /** Publishes a typed policy supplied by an internal runtime integration. */
  static synchronized void installInternalPolicy(ImageRuntimePolicy policy) {
    if (policy == null) {
      throw new IllegalArgumentException("an Image policy is required");
    }
    startupAttempted = true;
    pendingRules = null;
    currentPolicy = policy;
  }

  private static void initializeRules(
      List<RuntimeConfigurationFeatureBridge.FeatureRule<ImageStorageProfile>> rules) {
    currentPolicy = ImageRuntimePolicy.defaults();
    if (rules == null || rules.isEmpty()) {
      pendingRules = null;
      return;
    }
    pendingRules = Collections.unmodifiableList(
        new ArrayList<RuntimeConfigurationFeatureBridge.FeatureRule<ImageStorageProfile>>(rules));
    resolveIfReady();
  }

  private static void resolveIfReady() {
    if (pendingRules == null) {
      return;
    }
    RuntimeEnvironment environment = RuntimeEnvironment.current();
    if (!environment.isGraphicsBackendFinalized()) {
      return;
    }
    RuntimeConfigurationFeatureBridge.FeatureResolution<ImageStorageProfile> resolution =
        RuntimeConfigurationFeatureBridge.resolveSingleSetting(environment, pendingRules,
            ImageStorageProfile.STANDARD);
    boolean compactBackingAvailable = resolution.requestedValue() == ImageStorageProfile.COMPACT
        && ImageCompactStorageCapabilityBridge.isAvailable();
    currentPolicy = ImageRuntimePolicy.forRequestedStorage(resolution.requestedValue(),
        resolution.matchedRuleNames(), compactBackingAvailable);
    pendingRules = null;
  }

  private static void registerDescriptionContributor() {
    RuntimeConfigurationFeatureBridge.registerDescriptionContributor("Image",
        new RuntimeConfigurationFeatureBridge.DescriptionContributor() {
          @Override
          public String describe() {
            return currentPolicy().describeSection();
          }
        });
  }
}
