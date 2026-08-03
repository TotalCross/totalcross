// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.simulator.awt;

import java.awt.Color;
import java.awt.event.ComponentListener;
import java.awt.event.WindowListener;

/**
 * Immutable AWT preview window configuration.
 */
public class WindowConfiguration {
  public final int width;
  public final int height;
  public final String title;
  /** Raw command-line scale value; {@code -1} means fit the host display. */
  public final double scaleValue;
  /** Device density used to convert the raw scale value to a presentation scale. */
  public final double densityValue;
  public final int x;
  public final int y;
  public final boolean fullscreen;
  public final boolean resizable;
  public final Color background;
  public final WindowListener windowListener;
  public final ComponentListener componentListener;

  public WindowConfiguration(int width, int height, int scale, String title) {
    this(width, height, scale, 1, title, 0, 0, false, true, null, null, null);
  }

  public WindowConfiguration(int width, int height, double scaleValue, String title, int x, int y, boolean fullscreen,
      boolean resizable, Color background, WindowListener windowListener, ComponentListener componentListener) {
    this(width, height, scaleValue, 1, title, x, y, fullscreen, resizable, background, windowListener,
        componentListener);
  }

  public WindowConfiguration(int width, int height, double scaleValue, double densityValue, String title, int x, int y,
      boolean fullscreen, boolean resizable, Color background, WindowListener windowListener,
      ComponentListener componentListener) {
    this.width = width;
    this.height = height;
    this.scaleValue = scaleValue;
    this.densityValue = densityValue;
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
