// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import totalcross.io.File;
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.Vm;

/** Minimal native startup and text-surface probe. */
public final class WindowsNativeStartupProbe extends MainWindow {
  public WindowsNativeStartupProbe() {
    super();
    record("tcvm-java-class-initialized");
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
      if (!write("tcdiag-result.txt", "PASS\n")) {
        throw new IllegalStateException("could not persist the startup PASS marker");
      }
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
    if (!write("tcdiag-" + stage + ".marker", stage)) {
      Vm.debug("TC_WINDOWS_STARTUP_MARKER_WRITE_FAILED " + stage);
    }
  }

  private static boolean write(String filename, String contents) {
    File file = null;
    try {
      file = new File(Convert.appendPath(Settings.appPath, filename), File.CREATE_EMPTY);
      byte[] bytes = contents.getBytes();
      file.writeBytes(bytes, 0, bytes.length);
      file.close();
      file = null;
      return true;
    } catch (Throwable failure) {
      Vm.debug("TC_WINDOWS_STARTUP_MARKER_WRITE_FAILED " + filename + " "
          + failure.getClass().getName() + ": " + failure.getMessage());
      return false;
    } finally {
      if (file != null) {
        try {
          file.close();
        } catch (Throwable failure) {
          Vm.debug("TC_WINDOWS_STARTUP_MARKER_CLOSE_FAILED " + filename + " "
              + failure.getClass().getName() + ": " + failure.getMessage());
        }
      }
    }
  }
}
