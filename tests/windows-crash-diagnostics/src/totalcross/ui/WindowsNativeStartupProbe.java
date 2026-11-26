// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import totalcross.io.File;
import totalcross.sys.Settings;
import totalcross.sys.Vm;

/** Minimal native startup and text-surface probe. */
public final class WindowsNativeStartupProbe extends MainWindow {
  static {
    record("tcvm-java-class-initialized");
  }

  public WindowsNativeStartupProbe() {
    super();
    record("mainwindow-constructed");
  }

  @Override
  public void initUI() {
    record("init-ui-entered");
    try {
      add(new Label("native startup probe"), CENTER, CENTER);
      record("window-surface-requested");
      repaintNow();
      record("window-render-returned");
      write("tcdiag-result.txt", "PASS\n");
      record("application-complete");
      MainWindow.exit(0);
    } catch (Throwable failure) {
      write("tcdiag-failure.txt", failure.getClass().getName() + ": "
          + failure.getMessage());
      record("application-failed");
      MainWindow.exit(1);
    }
  }

  private static void record(String stage) {
    Vm.debug("TC_WINDOWS_STARTUP_STAGE " + stage);
    write("tcdiag-" + stage + ".marker", stage);
  }

  private static void write(String filename, String contents) {
    File file = null;
    try {
      file = new File(Settings.appPath + filename, File.CREATE_EMPTY);
      byte[] bytes = contents.getBytes();
      file.writeBytes(bytes, 0, bytes.length);
    } catch (Throwable ignored) {
      // The host process report remains available if the app marker path fails.
    } finally {
      if (file != null) {
        try {
          file.close();
        } catch (Throwable ignored) {
          // Keep the startup probe independent of file cleanup behavior.
        }
      }
    }
  }
}
