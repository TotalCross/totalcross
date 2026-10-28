// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import totalcross.sys.Platform;
import totalcross.sys.RuntimeFamily;
import totalcross.sys.Vm;
import totalcross.ui.image.ImageCompactStorageCapabilityBridge;
import totalcross.ui.image.ImagePrefetchWorkerMode;
import totalcross.ui.image.ImageStorageProfile;
import totalcross.util.concurrent.Lock;

/** Internal startup binding for resolving the optional Image TCZ configuration resource. */
public final class ImageRuntimeConfigurationStartup {
  private static final String RESOURCE_NAME = "tc.imageruntimeconfig";

  private static final Lock STARTUP_LOCK = new Lock();

  private static boolean startupAttempted;
  private static volatile List<RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>> pendingRules;
  private static volatile ImageRuntimePolicy currentPolicy = ImageRuntimePolicy.defaults();

  private ImageRuntimeConfigurationStartup() {
  }

  /** Called by the native VM after runtime environment and graphics setup. */
  public static void initializeAtStartup() {
    synchronized (STARTUP_LOCK) {
      if (startupAttempted) {
        resolveIfReady();
        return;
      }
      startupAttempted = true;
      registerDescriptionContributor();
      byte[] bytes = Vm.getFile(RESOURCE_NAME);
      initializeRules(bytes == null ? null : ImageRuntimeConfigurationMetadata.decode(bytes));
    }
  }

  /** Called by the simulator after parsing Image annotations and after B sets host facts. */
  public static void initializeForSimulator(
      List<RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>> rules) {
    synchronized (STARTUP_LOCK) {
      startupAttempted = true;
      registerDescriptionContributor();
      initializeRules(rules);
    }
  }

  /** Called after B finalizes the simulator's actual graphics backend. */
  public static void finalizeSimulatorGraphicsBackend() {
    synchronized (STARTUP_LOCK) {
      resolveIfReady();
    }
  }

  /** Returns the immutable Image policy snapshot, resolving pending simulator rules when ready. */
  public static ImageRuntimePolicy currentPolicy() {
    // The resolved snapshot is immutable. Only startup/unfinished simulator rules need locking.
    if (pendingRules != null) {
      synchronized (STARTUP_LOCK) {
        resolveIfReady();
      }
    }
    return currentPolicy;
  }

  /** Internal native-decoder query for the already resolved storage capability. */
  public static boolean compactStorageEnabledForNative() {
    return currentPolicy().effectiveStorageProfile() == ImageStorageProfile.COMPACT;
  }

  /** Internal fixture for exercising opt-in raster features without public configuration. */
  static void setRasterFeaturesForTest(boolean physicalIdentity,
      boolean targetColorConversion, boolean physicalVariantCache) {
    synchronized (STARTUP_LOCK) {
      resolveIfReady();
      currentPolicy = currentPolicy.withRasterFeaturesForTest(physicalIdentity,
          targetColorConversion, physicalVariantCache);
    }
  }

  /** Publishes a typed policy supplied by an internal runtime integration. */
  static void installInternalPolicy(ImageRuntimePolicy policy) {
    synchronized (STARTUP_LOCK) {
      if (policy == null) {
        throw new IllegalArgumentException("an Image policy is required");
      }
      startupAttempted = true;
      pendingRules = null;
      currentPolicy = policy;
    }
  }

  private static void initializeRules(
      List<RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>> rules) {
    currentPolicy = ImageRuntimePolicy.defaults();
    pendingRules = rules == null ? Collections.<RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>>emptyList()
        : Collections.unmodifiableList(
            new ArrayList<RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>>(rules));
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
    boolean windowsDesktop = environment.platform() == Platform.WINDOWS
        && environment.runtimeFamily() == RuntimeFamily.DESKTOP;
    RuntimeConfigurationFeatureBridge.FeatureResolution<ImageStorageProfile> storage =
        RuntimeConfigurationFeatureBridge.resolveProjectedSetting(environment, pendingRules,
            new RuntimeConfigurationFeatureBridge.ValueProjector<ImageRuntimeOptions, ImageStorageProfile>() {
              @Override
              public ImageStorageProfile project(ImageRuntimeOptions value) {
                return value.storage() == ImageStorageProfile.DEFAULT ? null : value.storage();
              }
            }, windowsDesktop ? ImageStorageProfile.COMPACT : ImageStorageProfile.STANDARD, "storage");
    RuntimeConfigurationFeatureBridge.FeatureResolution<Boolean> targetColorConversion =
        resolveFeatureState(environment, pendingRules, "targetColorConversion", windowsDesktop, new FeatureStateSelector() {
          @Override
          public RuntimeFeatureState state(ImageRuntimeOptions options) {
            return options.targetColorConversion();
          }
        });
    RuntimeConfigurationFeatureBridge.FeatureResolution<Boolean> physicalVariantCache =
        resolveFeatureState(environment, pendingRules, "physicalVariantCache", windowsDesktop, new FeatureStateSelector() {
          @Override
          public RuntimeFeatureState state(ImageRuntimeOptions options) {
            return options.physicalVariantCache();
          }
        });
    RuntimeConfigurationFeatureBridge.FeatureResolution<Boolean> scrollRasterReuse =
        resolveFeatureState(environment, pendingRules, "scrollRasterReuse", false, new FeatureStateSelector() {
          @Override
          public RuntimeFeatureState state(ImageRuntimeOptions options) {
            return options.scrollRasterReuse();
          }
        });
    RuntimeConfigurationFeatureBridge.FeatureResolution<ImagePrefetchWorkerMode> prefetchWorker =
        RuntimeConfigurationFeatureBridge.resolveProjectedSetting(environment, pendingRules,
            new RuntimeConfigurationFeatureBridge.ValueProjector<ImageRuntimeOptions, ImagePrefetchWorkerMode>() {
              @Override
              public ImagePrefetchWorkerMode project(ImageRuntimeOptions value) {
                return value.prefetchWorker() == ImagePrefetchWorkerMode.DEFAULT ? null : value.prefetchWorker();
              }
            }, windowsDesktop ? ImagePrefetchWorkerMode.SEMAPHORE_PROCESS_WORKER
                : ImagePrefetchWorkerMode.LEGACY_PER_ENTRY_THREAD, "prefetchWorker");

    Set<String> matchedNames = new TreeSet<String>();
    matchedNames.addAll(storage.matchedRuleNames());
    matchedNames.addAll(targetColorConversion.matchedRuleNames());
    matchedNames.addAll(physicalVariantCache.matchedRuleNames());
    matchedNames.addAll(scrollRasterReuse.matchedRuleNames());
    matchedNames.addAll(prefetchWorker.matchedRuleNames());
    ImageStorageProfile requestedStorage = storage.requestedValue();
    boolean compactBackingAvailable = requestedStorage == ImageStorageProfile.COMPACT
        && ImageCompactStorageCapabilityBridge.isAvailable();
    currentPolicy = ImageRuntimePolicy.forResolvedConfiguration(requestedStorage,
        targetColorConversion.requestedValue().booleanValue(),
        physicalVariantCache.requestedValue().booleanValue(),
        scrollRasterReuse.requestedValue().booleanValue(), prefetchWorker.requestedValue(),
        new ArrayList<String>(matchedNames), compactBackingAvailable);
    pendingRules = null;
  }

  private static RuntimeConfigurationFeatureBridge.FeatureResolution<Boolean> resolveFeatureState(
      RuntimeEnvironment environment,
      List<RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>> rules,
      String settingName, boolean defaultEnabled, final FeatureStateSelector selector) {
    return RuntimeConfigurationFeatureBridge.resolveProjectedSetting(environment, rules,
        new RuntimeConfigurationFeatureBridge.ValueProjector<ImageRuntimeOptions, Boolean>() {
          @Override
          public Boolean project(ImageRuntimeOptions value) {
            RuntimeFeatureState state = selector.state(value);
            return state == RuntimeFeatureState.DEFAULT ? null : Boolean.valueOf(state == RuntimeFeatureState.ENABLED);
          }
        }, Boolean.valueOf(defaultEnabled), settingName);
  }

  private interface FeatureStateSelector {
    RuntimeFeatureState state(ImageRuntimeOptions options);
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
