// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>
// Copyright (C) 2000 Dave Slaughter
// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.simulator.awt;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

/**
 * AWT window backend for the desktop simulator.
 */
public class AwtWindow implements SimulatorWindow {
  private final Frame frame;
  private final Component component;
  private final RenderSurface renderSurface;
  private Insets insets = new Insets(0, 0, 0, 0);
  private double scale = 1;

  public AwtWindow(Component component) {
    this(new Frame() {
      @Override
      public void update(java.awt.Graphics g) {
      }
    }, component);
  }

  public AwtWindow(Frame frame, Component component) {
    this(frame, component, component instanceof RenderSurface ? (RenderSurface) component : new AwtRenderSurface());
  }

  public AwtWindow(Frame frame, Component component, RenderSurface renderSurface) {
    this.frame = frame;
    this.component = component;
    this.renderSurface = renderSurface;
  }

  @Override
  public void start(WindowConfiguration config) {
    scale = resolveScale(config);
    if (config.fullscreen) {
      frame.setExtendedState(Frame.MAXIMIZED_BOTH);
      frame.setUndecorated(true);
    }
    if (config.background != null) {
      frame.setBackground(config.background);
    }
    frame.setResizable(config.resizable);
    frame.setLayout(null);
    frame.add(component);
    frame.addNotify();
    insets = frame.getInsets();
    if (insets == null) {
      insets = new Insets(0, 0, 0, 0);
    }
    setContentSize(config.width, config.height, true);
    frame.setLocation(config.x, config.y);
    frame.setTitle(config.title);
    frame.setVisible(true);
    if (config.windowListener != null) {
      frame.addWindowListener(config.windowListener);
    }
    if (config.componentListener != null) {
      frame.addComponentListener(config.componentListener);
    }
  }

  @Override
  public void stop() {
    frame.dispose();
  }

  @Override
  public void setTitle(String title) {
    frame.setTitle(title);
  }

  @Override
  public void requestRepaint() {
    component.repaint();
  }

  @Override
  public RenderSurface getRenderSurface() {
    return renderSurface;
  }

  public double getScale() {
    return scale;
  }

  public double getContentScale() {
    if (!frame.isDisplayable() || frame.getGraphicsConfiguration() == null) {
      return Double.NaN;
    }
    AffineTransform transform = frame.getGraphicsConfiguration().getDefaultTransform();
    double contentScale = transform.getScaleX();
    return Double.isFinite(contentScale) && contentScale > 0 ? contentScale : Double.NaN;
  }

  static double resolveScale(WindowConfiguration config) {
    Rectangle bounds = null;
    if (!GraphicsEnvironment.isHeadless()) {
      bounds = GraphicsEnvironment.getLocalGraphicsEnvironment()
          .getDefaultScreenDevice()
          .getDefaultConfiguration()
          .getBounds();
    }
    return resolveScale(config, bounds);
  }

  static double resolveScale(WindowConfiguration config, Rectangle displayBounds) {
    if (config.scaleValue != -1) {
      return Math.abs(config.scaleValue) / config.densityValue;
    }
    if (displayBounds == null) {
      return 1 / config.densityValue;
    }

    int viewportWidth = (int) (config.width / config.densityValue);
    int viewportHeight = (int) (config.height / config.densityValue);
    double maxRatio = Math.max((double) viewportWidth / displayBounds.width,
        (double) viewportHeight / displayBounds.height);
    double usableArea = 0.88;
    return maxRatio > usableArea ? usableArea / maxRatio / config.densityValue : 1 / config.densityValue;
  }

  public void setContentSize(int width, int height, boolean resizeFrame) {
    if (resizeFrame) {
      frame.setSize((int) (width * scale) + insets.left + insets.right,
          (int) (height * scale) + insets.top + insets.bottom);
    }
    component.setBounds(insets.left, insets.top, (int) (width * scale), (int) (height * scale));
  }

  public int getContentWidth() {
    return frame.getWidth() - insets.left - insets.right;
  }

  public int getContentHeight() {
    return frame.getHeight() - insets.top - insets.bottom;
  }

  public Insets getInsets() {
    return insets;
  }

  public Point getLocation() {
    return frame.getLocation();
  }

  public void setLocation(int x, int y) {
    frame.setLocation(x, y);
  }

  public int getExtendedState() {
    return frame.getExtendedState();
  }

  public void setExtendedState(int state) {
    frame.setExtendedState(state);
  }

  public Component getFocusOwner() {
    return frame.getFocusOwner();
  }
}
