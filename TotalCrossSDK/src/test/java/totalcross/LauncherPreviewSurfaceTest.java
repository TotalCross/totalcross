// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.lang.reflect.Field;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import totalcross.preview.PreviewSurface;
import totalcross.sys.Settings;

class LauncherPreviewSurfaceTest {
  @AfterEach
  void tearDown() {
    totalcross.ui.gfx.Graphics.mainWindowPixels = null;
    Launcher.instance = null;
  }

  @Test
  void updateScreenPresentsBufferedImageAndKeepsPixelBufferAlias() throws Exception {
    RecordingPreviewSurface surface = new RecordingPreviewSurface();
    Launcher launcher = new Launcher(surface, true);
    setField(launcher, "toScale", 1D);
    setField(launcher, "toBpp", 24);
    Settings.screenWidth = 2;
    Settings.screenHeight = 2;
    int[] originalPixels = new int[] { 0xFF000001, 0xFF000002, 0xFF000003, 0xFF000004 };
    totalcross.ui.gfx.Graphics.mainWindowPixels = originalPixels;

    launcher.updateScreen();

    assertNotNull(surface.presentedImage);
    int[] backingPixels = ((DataBufferInt) surface.presentedImage.getRaster().getDataBuffer()).getData();
    assertSame(backingPixels, totalcross.ui.gfx.Graphics.mainWindowPixels);
    assertArrayEquals(originalPixels, backingPixels);
  }

  private void setField(Object target, String name, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(name);
    field.setAccessible(true);
    field.set(target, value);
  }

  private static class RecordingPreviewSurface implements PreviewSurface {
    private BufferedImage presentedImage;

    @Override
    public void present(BufferedImage image) {
      presentedImage = image;
    }
  }
}
