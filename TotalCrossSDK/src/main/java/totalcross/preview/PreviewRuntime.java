// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.preview;

import java.awt.image.BufferedImage;

import totalcross.LauncherRuntime;
import totalcross.ui.Container;
import totalcross.ui.Control;
import totalcross.ui.MainWindow;

/** Stable runtime contract consumed by external Live Preview tooling. */
public interface PreviewRuntime extends AutoCloseable {
  /** Receives launcher-owned frames; retained frames must be copied by the consumer. */
  @FunctionalInterface
  interface FrameConsumer {
    void present(BufferedImage image);
  }

  static PreviewRuntime startPreview(String mainWindowClass, FrameConsumer surface, ClassLoader appClassLoader,
      String... args) {
    return LauncherRuntime.startPreview(mainWindowClass, surface, appClassLoader, args);
  }

  void pumpEvents();

  MainWindow createMainWindow(String className, ClassLoader classLoader, boolean terminateIfMainClass)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException;

  void preparePreviewMainWindowReload();

  void replaceMainWindow(MainWindow mainWindow, String commandLine);

  void showContainer(Container container);

  void showControl(Control control);

  @Override
  void close();
}
