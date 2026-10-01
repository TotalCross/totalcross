// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.sys.runtime;

/** Test-only access to internal raster feature toggles for the deployed smoke app. */
public final class ImageRasterSmokeTestSupport {
  private ImageRasterSmokeTestSupport() {
  }

  public static void setRasterFeatures(boolean physicalIdentity,
      boolean targetColorConversion, boolean physicalVariantCache) {
    ImageRuntimeConfigurationStartup.setRasterFeaturesForTest(physicalIdentity,
        targetColorConversion, physicalVariantCache);
  }
}
