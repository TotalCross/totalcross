// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.preview.internal;

import tc.preview.PreviewFrameSink;
import tc.preview.PreviewSession;
import tc.simulator.Launcher;

/** Adapts the stable preview worker contract to the desktop simulator. */
public final class SimulatorPreviewSession implements PreviewSession {
  private final Launcher launcher;

  public SimulatorPreviewSession(String mainClass, String[] arguments, ClassLoader applicationLoader,
      PreviewFrameSink frameSink) {
    launcher = Launcher.startPreview(mainClass, frameSink, applicationLoader,
        arguments == null ? new String[0] : arguments.clone());
  }

  @Override
  public void pumpEvents() {
    launcher.pumpEvents();
  }

  @Override
  public void resize(int width, int height, double density) {
    launcher.resizePreview(width, height, density);
  }

  @Override
  public void pointer(int x, int y, int button, boolean pressed) {
    launcher.injectPreviewPointer(x, y, button, pressed);
  }

  @Override
  public void key(int keyCode, boolean pressed, int modifiers) {
    launcher.injectPreviewKey(keyCode, pressed, modifiers);
  }

  @Override
  public void close() {
    launcher.close();
  }
}
