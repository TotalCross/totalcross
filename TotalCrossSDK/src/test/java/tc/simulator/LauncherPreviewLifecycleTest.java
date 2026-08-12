// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.simulator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import tc.preview.PreviewFrame;
import tc.preview.PreviewFrameSink;
import totalcross.ui.MainWindow;
import totalcross.ui.Window;

class LauncherPreviewLifecycleTest {
  private Launcher activeRuntime;

  @AfterEach
  void resetPreviewGlobals() {
    if (activeRuntime != null) {
      activeRuntime.stop();
      activeRuntime = null;
    }
    MainWindow.resetPreviewState();
    Launcher.instance = null;
    FirstPreviewApp.instance = null;
    SecondPreviewApp.instance = null;
  }

  @Test
  void sequentialPreviewSessionsCreateFreshApplicationAndWindowState() {
    RecordingSink firstSink = new RecordingSink();
    activeRuntime = start(FirstPreviewApp.class, firstSink, "123x234x32");

    assertNotNull(FirstPreviewApp.instance);
    assertFrame(firstSink.frame, 123, 234);
    assertSame(FirstPreviewApp.instance, MainWindow.getMainWindow());
    assertSame(FirstPreviewApp.instance, Window.getTopMost());
    MainWindow firstMainWindow = MainWindow.getMainWindow();
    assertDoesNotThrow(activeRuntime::stop);
    activeRuntime = null;

    RecordingSink secondSink = new RecordingSink();
    activeRuntime = start(SecondPreviewApp.class, secondSink, "321x432x32");

    assertNotNull(SecondPreviewApp.instance);
    assertFrame(secondSink.frame, 321, 432);
    assertNotSame(firstMainWindow, MainWindow.getMainWindow());
    assertSame(SecondPreviewApp.instance, MainWindow.getMainWindow());
    assertSame(SecondPreviewApp.instance, Window.getTopMost());
    assertDoesNotThrow(activeRuntime::stop);
    activeRuntime = null;
  }

  private Launcher start(Class<? extends MainWindow> appClass, PreviewFrameSink sink, String screen) {
    return Launcher.startPreviewFrames(appClass.getName(), sink, getClass().getClassLoader(),
        "/scr", screen, "/density", "1");
  }

  private static void assertFrame(PreviewFrame frame, int width, int height) {
    assertNotNull(frame);
    assertEquals(width, frame.getWidth());
    assertEquals(height, frame.getHeight());
  }

  public static final class FirstPreviewApp extends MainWindow {
    static FirstPreviewApp instance;

    public FirstPreviewApp() {
      instance = this;
    }
  }

  public static final class SecondPreviewApp extends MainWindow {
    static SecondPreviewApp instance;

    public SecondPreviewApp() {
      instance = this;
    }
  }

  private static final class RecordingSink implements PreviewFrameSink {
    private PreviewFrame frame;

    @Override
    public void present(PreviewFrame frame) {
      this.frame = frame;
    }
  }
}
