// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>
// Copyright (C) 2000 Dave Slaughter
// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.simulator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;

import tc.tools.JarClassPathLoader;
import tc.tools.deployer.DeploySettings;
import totalcross.io.IOException;
import totalcross.io.RandomAccessStream;
import totalcross.io.Stream;
import tc.simulator.awt.AppletPreviewSurface;
import tc.simulator.awt.AwtWindow;
import tc.simulator.awt.AwtRenderSurface;
import tc.preview.PreviewSession;
import totalcross.sys.Settings;
import totalcross.sys.SpecialKeys;
import totalcross.sys.Time;
import totalcross.sys.Vm;
import totalcross.ui.Control;
import totalcross.ui.Container;
import totalcross.ui.MainWindow;
import totalcross.ui.Window;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.MultiTouchEvent;
import totalcross.ui.event.PenEvent;
import totalcross.util.Hashtable;
import totalcross.util.IntHashtable;
import totalcross.util.zip.TCZ;
import tc.preview.PreviewFrame;
import tc.preview.PreviewFrameSink;

@SuppressWarnings({"deprecation", "removal"})
abstract class FrameRenderer extends InputDispatcher {
  /** Calls System.out.println. TotalCross system debugging uses this method. See also debug(String s). */
  public static void print(String s) {
    if (totalcross.sys.Settings.showDesktopMessages) {
      System.err.println(s);
    }
  }
  public static void debug(String s){
    if(totalcross.sys.Settings.showDebugMessages){
      System.out.println(s);
    }
  }

  //// Graphics ////////////////////////////////////////////////////////////////////

  protected void createColorPaletteLookupTables() {
    int i, r, g, b;
    lookupR = new int[256];
    lookupG = new int[256];
    lookupB = new int[256];
    lookupGray = new int[256];

    for (i = 0; i < 256; i++) {
      r = (i + 1) * 6 / 256;
      if (r > 0) {
        r--;
      }
      g = (i + 1) * 8 / 256;
      if (g > 0) {
        g--;
      }
      b = (i + 1) * 5 / 256;
      if (b > 0) {
        b--;
      }
      lookupR[i] = r * 40;
      lookupG[i] = g * 5;
      lookupB[i] = b + 16;
      lookupGray[i] = i / 0x11;
    }
    pal685 = totalcross.ui.gfx.Graphics.getPalette();
  }

  protected int getScreenColor(int p) {
    int r = (p >> 16) & 0xFF;
    int g = (p >> 8) & 0xFF;
    int b = p & 0xFF;
    switch (toBpp) {
    case 8:
      if (lookupR == null) {
        createColorPaletteLookupTables();
      }
      return pal685[(g == r && g == b) ? lookupGray[r] : (lookupR[r] + lookupG[g] + lookupB[b])];
    case 16:
      return (((r) >> 3) << 19) | (((g) >> 2) << 10) | (((b >> 3) << 3));
    default:
      return p;
    }
  }

  public void updateScreen() {
    //int ini = totalcross.sys.Vm.getTimeStamp();
    int w = totalcross.ui.gfx.Graphics.getMainWindowPixelWidth();
    int h = totalcross.ui.gfx.Graphics.getMainWindowPixelHeight();

    if (screenImg == null || screenImg.getWidth() != w || screenImg.getHeight() != h) {
      screenImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
      // We can typecast directly to DataBufferInt because that's the type used for images with 24+ bit color
      DataBufferInt dbi = (DataBufferInt) screenImg.getRaster().getDataBuffer();
      int[] pixels = dbi.getData();
      // Copy whatever was drawn before
      System.arraycopy(totalcross.ui.gfx.Graphics.mainWindowPixels, 0, pixels, 0,
          Math.min(totalcross.ui.gfx.Graphics.mainWindowPixels.length, w * h));
      // And replace with our bytes, now we no longer need to copy from the mainWindowPixels to our image. Saving a ton of memory.
      totalcross.ui.gfx.Graphics.mainWindowPixels = pixels;
    }
    int[] pixels = totalcross.ui.gfx.Graphics.mainWindowPixels;
    int n = w * h;
    if (toBpp >= 24) {
      screenPixels = pixels;
    } else if (screenPixels.length < n) {
      screenPixels = new int[n];
    }
    // convert to the target bpp on-the-fly
    switch (toBpp) {
    case 8: {
      if (lookupR == null) {
        createColorPaletteLookupTables();
      }
      int[] pal = pal685;
      int[] toR = lookupR;
      int[] toG = lookupG;
      int[] toB = lookupB;
      int[] toGray = lookupGray;
      while (--n >= 0) {
        int p = pixels[n];
        int r = (p >> 16) & 0xFF;
        int g = (p >> 8) & 0xFF;
        int b = p & 0xFF;
        screenPixels[n] = pal[(g == r && g == b) ? toGray[r] : (toR[r] + toG[g] + toB[b])];
      }
      break;
    }
    case 16: {
      while (--n >= 0) {
        screenPixels[n] = pixels[n] & 0xF8FCF8; // guich@tc100b4_2: use a direct and instead of a bunch of shifts. note: using a DirectColorModel(32,0xF80000,0x00FC00,0x0000F8,0) is 5x SLOWER than doing the mapping by ourselves.
      }
      break;
    }
    }
    getPreviewSurface().present(screenImg);
    PreviewFrameSink frameConsumer = getPreviewFrameSink();
    if (frameConsumer != null) {
      frameConsumer.present(new PreviewFrame(w, h, w, totalcross.ui.gfx.Graphics.getMainWindowContentScale(),
          PreviewFrame.PixelFormat.ARGB_8888, screenPixels));
    }
    // make the emulator work like OpenGL: erase the screen to instruct the user that everything must be drawn always
    //java.util.Arrays.fill(pixels, getScreenColor(UIColors.unsafeAreaColor));
  }

  protected AwtRenderSurface getPreviewSurface() {
    if (previewSurface == null) {
      previewSurface = new AppletPreviewSurface(this, toScale,
          totalcross.ui.gfx.Graphics.getMainWindowContentScale(), fastScale);
    }
    return previewSurface;
  }

  public static BufferedImage toBufferedImage(java.awt.Image img) {
    if (img instanceof BufferedImage) {
      return (BufferedImage) img;
    }

    BufferedImage bufferedImage = new BufferedImage(img.getWidth(null), img.getHeight(null),
        BufferedImage.TYPE_INT_ARGB);

    Graphics2D graphics = bufferedImage.createGraphics();
    graphics.drawImage(img, 0, 0, null);
    graphics.dispose();

    return bufferedImage;
  }

  //static int count;

}
