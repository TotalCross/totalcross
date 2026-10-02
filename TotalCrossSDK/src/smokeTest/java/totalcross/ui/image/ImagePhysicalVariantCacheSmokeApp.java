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

/** Deployed macOS smoke for the production P3 physical-variant cache option. */
@RuntimeConfiguration
@ImageRuntimeRule(when = @RuntimeWhen(allOf = {
    @RuntimeCondition(platform = Platform.MACOS),
    @RuntimeCondition(family = RuntimeFamily.DESKTOP),
    @RuntimeCondition(architecture = Architecture.ARM64),
    @RuntimeCondition(backend = GraphicsBackend.RASTER)
}), storage = ImageStorageProfile.COMPACT,
    targetColorConversion = RuntimeFeatureState.DISABLED,
    physicalVariantCache = RuntimeFeatureState.ENABLED)
public final class ImagePhysicalVariantCacheSmokeApp extends MainWindow {
  @Override
  public void initUI() {
    try {
      ImageRuntimePolicy policy = ImageRuntimeConfigurationStartup.currentPolicy();
      Image source = ImageCompactStorageSmokeSupport.load("opaque-color.png");
      source.getPixels();
      boolean compactBacking = ImageCompactStorageSmokeSupport.nativeBacking(source).isCompact();
      boolean typedOptionsPolicy = policy.requestedStorageProfile() == ImageStorageProfile.COMPACT
          && policy.effectiveStorageProfile() == ImageStorageProfile.COMPACT
          && !policy.rasterVariants().targetColorConversion()
          && policy.rasterVariants().physicalVariantCache();
      boolean physicalCache = ImageCompactStorageP3SmokeSupport.productionConfiguredPhysicalVariantPath();
      boolean pass = compactBacking && typedOptionsPolicy && physicalCache;
      if (!pass) {
        throw new IllegalStateException("production physical-variant option was not exercised: "
            + ImageCompactStorageP3SmokeSupport.productionVariantFailureDetails());
      }
      System.out.println("fixture=ImagePhysicalVariantCacheSmokeApp,image-physical-variant-cache-pass"
          + " typedOptionsPolicy=" + typedOptionsPolicy + " compactBacking=" + compactBacking
          + " productionPhysicalVariantCache=" + physicalCache);
      System.out.flush();
      exit(0);
    } catch (Throwable failure) {
      System.out.println("fixture=ImagePhysicalVariantCacheSmokeApp,image-physical-variant-cache-fail error="
          + failure.getMessage());
      System.out.flush();
      exit(1);
    }
  }
}
