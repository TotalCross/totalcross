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
@ImageRuntimeRule(when = @RuntimeWhen(allOf = { @RuntimeCondition(family = RuntimeFamily.DESKTOP) }),
    storage = ImageStorageProfile.COMPACT)
@ImageRuntimeRule(when = @RuntimeWhen(allOf = {
    @RuntimeCondition(platform = Platform.MACOS),
    @RuntimeCondition(family = RuntimeFamily.DESKTOP),
    @RuntimeCondition(architecture = Architecture.ARM64),
    @RuntimeCondition(backend = GraphicsBackend.RASTER)
}), storage = ImageStorageProfile.STANDARD)
public final class ImageRuntimeConfigurationSpecificityMacSmokeApp extends MainWindow {
  @Override
  public void initUI() {
    try {
      ImageRuntimePolicy policy = ImageRuntimeConfigurationStartup.currentPolicy();
      require(policy.requestedStorageProfile() == ImageStorageProfile.STANDARD,
          "requestedStorage=" + policy.requestedStorageProfile());
      require(policy.effectiveStorageProfile() == ImageStorageProfile.STANDARD,
          "effectiveStorage=" + policy.effectiveStorageProfile());
      require(policy.matchedRuleNames().size() == 2, "matchedRules=" + policy.matchedRuleNames());
      System.out.println("image-runtime-specificity-pass order=broad-first requested=STANDARD matchedRules=2");
      System.out.flush();
      exit(0);
    } catch (Throwable failure) {
      System.out.println("image-runtime-specificity-fail error=" + failure.getMessage());
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
