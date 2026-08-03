// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>
// Copyright (C) 2000 Dave Slaughter
// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.simulator;

import tc.preview.PreviewFrameSink;

/**
 * Public compatibility facade for the desktop launcher.
 *
 * The implementation is split into package-private responsibility layers so
 * existing callers still construct and use {@code totalcross.Launcher} while
 * lifecycle, arguments, events, rendering, I/O, settings, fonts, and streams
 * remain independently testable.
 */
@SuppressWarnings({"deprecation", "removal"})
public class SimulatorCore extends StreamBridge {
  // Kept on the facade for source-compatible reflective test and tooling access.
  protected double toScale = -1;
  protected int toBpp = 24;

  private PreviewFrameSink previewFrameConsumer;

  public SimulatorCore() {
    super();
    initializeLauncher();
  }

  SimulatorCore(PreviewFrameSink previewSurface, boolean previewMode) {
    super();
    this.previewFrameConsumer = previewSurface;
    this.previewMode = previewMode;
    initializeLauncher();
  }

  SimulatorCore(PreviewFrameSink previewSurface, boolean previewMode, ClassLoader appClassLoader) {
    super();
    this.previewFrameConsumer = previewSurface;
    this.previewMode = previewMode;
    this.appClassLoader = appClassLoader;
    initializeLauncher();
  }

  @Override
  public void updateScreen() {
    if (toScale == -1) {
      // Preview/headless runs have no AWT window to resolve the presentation
      // scale. Use the density baseline until a graphical backend is present.
      toScale = 1 / toDensityValue;
    }
    if (toScale != -1 || super.toScale == -1) {
      super.toScale = toScale;
    }
    if (toBpp != 24 || super.toBpp == 24) {
      super.toBpp = toBpp;
    }
    super.updateScreen();
  }

  public void setPreviewFrameSink(PreviewFrameSink consumer) {
    previewFrameConsumer = consumer;
  }

  @Override
  protected PreviewFrameSink getPreviewFrameSink() {
    return previewFrameConsumer;
  }

}
