// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.lang.reflect.Field;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import tc.preview.PreviewFrame;
import tc.preview.PreviewFrameSink;
import totalcross.sys.Settings;

class LauncherPreviewSurfaceTest {
  @AfterEach
  void tearDown() {
    totalcross.ui.gfx.Graphics.mainWindowPixels = null;
    Launcher.instance = null;
  }

  @Test
  @SuppressWarnings("deprecation")
  void launcherDoesNotExtendApplet() {
    assertFalse(java.applet.Applet.class.isAssignableFrom(Launcher.class));
  }

  @Test
  void updateScreenPresentsBufferedImageAndKeepsPixelBufferAlias() throws Exception {
    RecordingPreviewFrameSink copiedSurface = new RecordingPreviewFrameSink();
    Launcher launcher = new Launcher();
    launcher.setPreviewFrameSink(copiedSurface);
    setField(launcher, "toScale", 1D);
    setField(launcher, "toBpp", 24);
    Settings.screenWidth = 2;
    Settings.screenHeight = 2;
    int[] originalPixels = new int[] { 0xFF000001, 0xFF000002, 0xFF000003, 0xFF000004 };
    totalcross.ui.gfx.Graphics.mainWindowPixels = originalPixels;

    launcher.updateScreen();

    assertNotNull(copiedSurface.presentedFrame);
    assertArrayEquals(originalPixels, totalcross.ui.gfx.Graphics.mainWindowPixels);
    totalcross.ui.gfx.Graphics.mainWindowPixels[0] = 0;
    assertArrayEquals(originalPixels, copiedSurface.presentedFrame.copyPixels());
  }

  private void setField(Object target, String name, Object value) throws Exception {
    Class<?> type = target.getClass();
    Field field = null;
    do {
      try {
        field = type.getDeclaredField(name);
        break;
      } catch (NoSuchFieldException e) {
        type = type.getSuperclass();
      }
    } while (type != null);
    if (type == null) {
      throw new NoSuchFieldException(name);
    }
    field.setAccessible(true);
    field.set(target, value);
  }

  private static class RecordingPreviewFrameSink implements PreviewFrameSink {
    private PreviewFrame presentedFrame;

    @Override
    public void present(PreviewFrame frame) {
      presentedFrame = frame;
    }
  }
}
