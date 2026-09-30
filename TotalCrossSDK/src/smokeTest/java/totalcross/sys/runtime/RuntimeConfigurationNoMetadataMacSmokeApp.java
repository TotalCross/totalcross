// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import totalcross.sys.Architecture;
import totalcross.sys.GraphicsBackend;
import totalcross.sys.Platform;
import totalcross.sys.RuntimeFamily;
import totalcross.sys.Vm;
import totalcross.ui.MainWindow;

public final class RuntimeConfigurationNoMetadataMacSmokeApp extends MainWindow {
  @Override
  public void initUI() {
    try {
      RuntimeEnvironment environment = RuntimeEnvironment.current();
      require(environment.platform() == Platform.MACOS, "platform=" + environment.platform());
      require(environment.runtimeFamily() == RuntimeFamily.DESKTOP, "family=" + environment.runtimeFamily());
      require(environment.architecture() == Architecture.ARM64, "architecture=" + environment.architecture());
      require(environment.isGraphicsBackendFinalized(), "graphics backend was not finalized");
      require(environment.graphicsBackend() == GraphicsBackend.RASTER,
          "backend=" + environment.graphicsBackend());
      require(Vm.getFile("tc.runtimeconfig") == null, "runtime metadata resource exists");
      require(RuntimeConfigurationStartup.current() == null, "configuration exists without metadata");
      System.out.println("runtime-config-no-metadata-pass platform=" + environment.platform()
          + " family=" + environment.runtimeFamily() + " architecture=" + environment.architecture()
          + " backend=" + environment.graphicsBackend() + " metadata=absent");
      System.out.flush();
      exit(0);
    } catch (Throwable failure) {
      System.out.println("runtime-config-no-metadata-fail error=" + failure.getMessage());
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
