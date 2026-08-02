// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.simulator.awt;

/**
 * Window lifecycle boundary used by the preview architecture.
 */
public interface SimulatorWindow {
  void start(WindowConfiguration config);

  void stop();

  void setTitle(String title);

  void requestRepaint();

  RenderSurface getRenderSurface();
}
