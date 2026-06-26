// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross;

import java.awt.image.BufferedImage;

import totalcross.preview.HeadlessPreviewSurface;
import totalcross.ui.MainWindow;

/**
 * Starts a TotalCross application with a headless preview surface.
 * <p>
 * IDE integrations should use this entry point to obtain rendered frames
 * without depending on Applet painting or AWT windows. This runner intentionally
 * does not implement input, navigation, hot reload, or IDE-specific behavior.
 */
public class PreviewRunner {
  private final Launcher launcher;
  private final HeadlessPreviewSurface surface;

  private PreviewRunner(Launcher launcher, HeadlessPreviewSurface surface) {
    this.launcher = launcher;
    this.surface = surface;
  }

  public static PreviewRunner run(Class<? extends MainWindow> clazz, String... args) {
    if (clazz == null) {
      throw new IllegalArgumentException("clazz cannot be null");
    }
    return run(clazz.getCanonicalName(), args);
  }

  public static PreviewRunner run(String className, String... args) {
    if (className == null || className.length() == 0) {
      throw new IllegalArgumentException("className cannot be empty");
    }
    Launcher.isApplication = true;
    HeadlessPreviewSurface surface = new HeadlessPreviewSurface();
    Launcher launcher = new Launcher(surface, true);
    launcher.parseArguments(className, args);
    launcher.init();
    launcher.startApp();
    launcher.pumpEvents();
    return new PreviewRunner(launcher, surface);
  }

  public HeadlessPreviewSurface getSurface() {
    return surface;
  }

  public BufferedImage getLatestFrame() {
    return surface.getLatestFrame();
  }

  public long getFrameNumber() {
    return surface.getFrameNumber();
  }

  public void pumpEvents() {
    launcher.pumpEvents();
  }

  public void stop() {
    launcher.destroy();
  }
}
