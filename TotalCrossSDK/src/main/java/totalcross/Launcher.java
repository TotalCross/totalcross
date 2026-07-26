// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>
// Copyright (C) 2000 Dave Slaughter
// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross;

import java.util.Arrays;

import totalcross.preview.PreviewRuntime;
import totalcross.preview.PreviewFrame;
import totalcross.preview.PreviewFrameConsumer;

/**
 * Public compatibility facade for the desktop launcher.
 *
 * The implementation is split into package-private responsibility layers so
 * existing callers still construct and use {@code totalcross.Launcher} while
 * lifecycle, arguments, events, rendering, I/O, settings, fonts, and streams
 * remain independently testable.
 */
@SuppressWarnings({"deprecation", "removal"})
public final class Launcher extends LauncherStreams {
  // Kept on the facade for source-compatible reflective test and tooling access.
  private double toScale = -1;
  private int toBpp = 24;

  public class UserFont extends LauncherFontTypes.UserFont {
    protected UserFont(String fontName, String suffix, int size, totalcross.ui.font.Font base) throws Exception {
      super(fontName, suffix, size, base);
    }

    protected UserFont(String fontName, String suffix) throws Exception {
      super(fontName, suffix);
    }
  }

  private PreviewFrameConsumer previewFrameConsumer;

  public Launcher() {
    super();
    initializeLauncher();
  }

  Launcher(PreviewRuntime.FrameConsumer previewSurface, boolean previewMode) {
    super();
    this.previewSurface = previewSurface;
    this.previewMode = previewMode;
    initializeLauncher();
  }

  Launcher(PreviewRuntime.FrameConsumer previewSurface, boolean previewMode, ClassLoader appClassLoader) {
    super();
    this.previewSurface = previewSurface;
    this.previewMode = previewMode;
    this.appClassLoader = appClassLoader;
    initializeLauncher();
  }

  @Override
  public void updateScreen() {
    if (toScale != -1 || super.toScale == -1) {
      super.toScale = toScale;
    }
    if (toBpp != 24 || super.toBpp == 24) {
      super.toBpp = toBpp;
    }
    super.updateScreen();
  }

  public void setPreviewFrameConsumer(PreviewFrameConsumer consumer) {
    previewFrameConsumer = consumer;
  }

  @Override
  protected PreviewFrameConsumer getPreviewFrameConsumer() {
    return previewFrameConsumer;
  }

  public static void main(String[] args) {
    if (args.length == 0 || args[0].equals("/help")) {
      if (args.length == 0) {
        showInstructions();
      }
      args = new String[] { "/scr", "480x620x32", "/fontsize", "16", "tc.Help" };
    }
    isApplication = true;
    LauncherRuntime.startApplication(args[args.length - 1], Arrays.copyOf(args, args.length - 1));
  }
}
