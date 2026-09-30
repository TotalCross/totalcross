// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import totalcross.sys.Architecture;
import totalcross.sys.GraphicsBackend;
import totalcross.sys.Platform;
import totalcross.sys.RuntimeFamily;
import totalcross.ui.MainWindow;

@RuntimeConfiguration
@RuntimeRule(when = @RuntimeWhen(allOf = {
    @RuntimeCondition(platform = Platform.MACOS),
    @RuntimeCondition(family = RuntimeFamily.DESKTOP),
    @RuntimeCondition(architecture = Architecture.ARM64),
    @RuntimeCondition(backend = GraphicsBackend.RASTER)
}))
@RuntimeRule(when = @RuntimeWhen(allOf = {
    @RuntimeCondition(platform = Platform.MACOS),
    @RuntimeCondition(family = RuntimeFamily.DESKTOP),
    @RuntimeCondition(architecture = Architecture.ARM64),
    @RuntimeCondition(backend = GraphicsBackend.GPU)
}))
public final class RuntimeConfigurationAnnotatedMacSmokeApp extends MainWindow {
  @Override
  public void initUI() {
    try {
      RuntimeEnvironment environment = RuntimeEnvironment.current();
      ResolvedRuntimeConfiguration resolved = RuntimeConfigurationStartup.current();
      require(environment.platform() == Platform.MACOS, "platform=" + environment.platform());
      require(environment.runtimeFamily() == RuntimeFamily.DESKTOP, "family=" + environment.runtimeFamily());
      require(environment.architecture() == Architecture.ARM64, "architecture=" + environment.architecture());
      require(environment.isGraphicsBackendFinalized(), "graphics backend was not finalized");
      require(environment.graphicsBackend() == GraphicsBackend.RASTER,
          "backend=" + environment.graphicsBackend());
      require(resolved != null, "runtime configuration was not resolved");
      require(resolved.matchedRuleCount() == 1, "matchedRules=" + resolved.matchedRuleCount());
      System.out.println("runtime-config-annotated-pass platform=" + environment.platform()
          + " family=" + environment.runtimeFamily() + " architecture=" + environment.architecture()
          + " backend=" + environment.graphicsBackend() + " matchedRules=" + resolved.matchedRuleCount());
      System.out.flush();
      exit(0);
    } catch (Throwable failure) {
      System.out.println("runtime-config-annotated-fail error=" + failure.getMessage());
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
