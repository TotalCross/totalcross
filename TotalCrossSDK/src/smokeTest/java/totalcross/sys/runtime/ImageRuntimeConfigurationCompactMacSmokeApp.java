// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import totalcross.sys.Architecture;
import totalcross.sys.GraphicsBackend;
import totalcross.sys.Platform;
import totalcross.sys.RuntimeFamily;
import totalcross.ui.MainWindow;
import totalcross.ui.image.ImageRuntimeRule;
import totalcross.ui.image.ImageStorageProfile;

@RuntimeConfiguration
@ImageRuntimeRule(when = @RuntimeWhen(allOf = {
    @RuntimeCondition(platform = Platform.MACOS),
    @RuntimeCondition(family = RuntimeFamily.DESKTOP),
    @RuntimeCondition(architecture = Architecture.ARM64),
    @RuntimeCondition(backend = GraphicsBackend.RASTER)
}), storage = ImageStorageProfile.COMPACT)
public final class ImageRuntimeConfigurationCompactMacSmokeApp extends MainWindow {
  @Override
  public void initUI() {
    try {
      ImageRuntimePolicy policy = ImageRuntimeConfigurationStartup.currentPolicy();
      require(policy.requestedStorageProfile() == ImageStorageProfile.COMPACT,
          "requestedStorage=" + policy.requestedStorageProfile());
      require(policy.effectiveStorageProfile() == ImageStorageProfile.COMPACT,
          "effectiveStorage=" + policy.effectiveStorageProfile());
      require(policy.storageReason() == null,
          "reason=" + policy.storageReason());
      String report = RuntimeConfigurationReport.describe();
      require(report.contains("requested: COMPACT"), "requested policy missing from report");
      require(report.contains("effective: COMPACT"), "effective policy missing from report");
      System.out.println("image-runtime-compact-pass requested=COMPACT effective=COMPACT");
      System.out.flush();
      exit(0);
    } catch (Throwable failure) {
      System.out.println("image-runtime-compact-fail error=" + failure.getMessage());
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
