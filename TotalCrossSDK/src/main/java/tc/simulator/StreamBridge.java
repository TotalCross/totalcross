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

@SuppressWarnings({"deprecation", "removal"})
class StreamBridge extends SettingsBridge {
  public void alert(String msg) {
    if (alert == null) {
      System.err.println("Alert: " + msg);
    } else if (!started) {
      System.out.println("Alert: " + msg);
    } else {
      alert.setText(msg);
      alert.setVisible(true);
      while (alert.isVisible()) {
        try {
          Thread.sleep(10);
        } catch (Exception e) {
        }
      }
    }
  }

  /** Converts a java.io.InputStream into a totalcross.io.Stream */
  public void setTitle(String title) {
    if (isApplication) {
      frameTitle = title;
      if (hasWindowBackend()) {
        setWindowTitle(title);
      }
    }
  }

  public void vibrate(final int millis) {
    if (isApplication && hasWindowBackend()) {
      new Thread() {
        @Override
        public void run() {
          Point p = getWindowLocation();
          int x = p.x, y = p.y;

          int[] xPoints = { x - 3, x, x + 3, x, x + 3, x, x - 3, x };
          int[] yPoints = { y - 3, y, y + 3, y, y - 3, y, y + 3, y };
          int i = 0;
          int j = 0;

          int t = Vm.getTimeStamp();
          do {
            setWindowLocation(xPoints[i], yPoints[j]);

            i = ++i % xPoints.length;
            if (i == 0) {
              j = ++j % yPoints.length;
            }

            Thread.yield();// give some time for the other threads to execute
          } while (Vm.getTimeStamp() - t < millis);

          setWindowLocation(x, y); // restore original location
        }
      }.start();
    }
  }

  public void setSIP(int option, Control edit, boolean secret) {
  }

  @Override
  public void componentHidden(ComponentEvent arg0) {
  }

  @Override
  public void componentMoved(ComponentEvent arg0) {
  }

  @Override
  public void componentShown(ComponentEvent arg0) {
  }

  boolean ignoreNextResize; // guich@tc168: ignore when using F9

  @Override
  public void componentResized(ComponentEvent ev) {
    if (ignoreNextResize) {
      ignoreNextResize = false;
      return;
    }
    if (!hasWindowBackend()) {
      return;
    }
    int w = getWindowContentWidth();
    int h = getWindowContentHeight();
    w /= toScale; // guich@tc168: consider scale
    h /= toScale;
    if (w < toWidth || h < toHeight) {
      screenResized(w >= toWidth ? w : toWidth, h >= toHeight ? h : toHeight, true);
    } else {
      screenResized(w, h, false);
    }
  }
}
