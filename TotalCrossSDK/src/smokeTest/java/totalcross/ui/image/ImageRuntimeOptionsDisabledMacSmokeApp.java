// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.ui.image;

import totalcross.sys.Architecture;
import totalcross.sys.GraphicsBackend;
import totalcross.sys.Platform;
import totalcross.sys.RuntimeFamily;
import totalcross.sys.runtime.ImageRuntimeConfigurationStartup;
import totalcross.sys.runtime.ImageRuntimePolicy;
import totalcross.sys.runtime.RuntimeCondition;
import totalcross.sys.runtime.RuntimeConfiguration;
import totalcross.sys.runtime.RuntimeFeatureState;
import totalcross.sys.runtime.RuntimeWhen;
import totalcross.ui.MainWindow;

/** Confirms explicit P3 DISABLED values keep both production variant paths inactive. */
@RuntimeConfiguration
@ImageRuntimeRule(when = @RuntimeWhen(allOf = {
    @RuntimeCondition(platform = Platform.MACOS),
    @RuntimeCondition(family = RuntimeFamily.DESKTOP),
    @RuntimeCondition(architecture = Architecture.ARM64),
    @RuntimeCondition(backend = GraphicsBackend.RASTER)
}), targetColorConversion = RuntimeFeatureState.DISABLED,
    physicalVariantCache = RuntimeFeatureState.DISABLED)
public final class ImageRuntimeOptionsDisabledMacSmokeApp extends MainWindow {
  @Override
  public void initUI() {
    try {
      ImageRuntimePolicy policy = ImageRuntimeConfigurationStartup.currentPolicy();
      boolean disabled = policy.requestedStorageProfile() == ImageStorageProfile.STANDARD
          && policy.effectiveStorageProfile() == ImageStorageProfile.STANDARD
          && !policy.rasterVariants().targetColorConversion()
          && !policy.rasterVariants().physicalVariantCache()
          && !policy.scrollRasterReuse().enabled()
          && policy.prefetchWorker() == ImagePrefetchWorkerMode.LEGACY_PER_ENTRY_THREAD;
      boolean inactive = ImageCompactStorageP3SmokeSupport.productionDisabledVariantsRemainInactive();
      require(disabled && inactive, "explicit DISABLED configuration was not honored");
      System.out.println("image-runtime-options-disabled-pass targetColorConversion=disabled"
          + " physicalVariantCache=disabled p3PathsInactive=true");
      System.out.flush();
      exit(0);
    } catch (Throwable failure) {
      System.out.println("image-runtime-options-disabled-fail error=" + failure.getMessage());
      System.out.flush();
      exit(1);
    }
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
