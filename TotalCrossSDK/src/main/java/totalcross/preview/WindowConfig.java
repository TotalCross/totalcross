// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.preview;

import java.awt.Color;
import java.awt.event.ComponentListener;
import java.awt.event.WindowListener;

/**
 * Immutable AWT preview window configuration.
 */
public class WindowConfig {
  public final int width;
  public final int height;
  public final int scale;
  public final String title;
  public final double scaleFactor;
  public final int x;
  public final int y;
  public final boolean fullscreen;
  public final boolean resizable;
  public final Color background;
  public final WindowListener windowListener;
  public final ComponentListener componentListener;

  public WindowConfig(int width, int height, int scale, String title) {
    this(width, height, scale, scale, title, 0, 0, false, true, null, null, null);
  }

  public WindowConfig(int width, int height, double scaleFactor, String title, int x, int y, boolean fullscreen,
      boolean resizable, Color background, WindowListener windowListener, ComponentListener componentListener) {
    this(width, height, (int) scaleFactor, scaleFactor, title, x, y, fullscreen, resizable, background, windowListener,
        componentListener);
  }

  private WindowConfig(int width, int height, int scale, double scaleFactor, String title, int x, int y,
      boolean fullscreen, boolean resizable, Color background, WindowListener windowListener,
      ComponentListener componentListener) {
    this.width = width;
    this.height = height;
    this.scale = scale;
    this.scaleFactor = scaleFactor;
    this.title = title;
    this.x = x;
    this.y = y;
    this.fullscreen = fullscreen;
    this.resizable = resizable;
    this.background = background;
    this.windowListener = windowListener;
    this.componentListener = componentListener;
  }
}
