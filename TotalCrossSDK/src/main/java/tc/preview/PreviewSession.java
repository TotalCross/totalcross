// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.preview;

/** Stable, runtime-neutral worker lifecycle contract for desktop preview tooling. */
public interface PreviewSession {
  void pumpEvents();

  void resize(int width, int height, double density);

  void pointer(int x, int y, int button, boolean pressed);

  void key(int keyCode, boolean pressed, int modifiers);

  void close();
}
