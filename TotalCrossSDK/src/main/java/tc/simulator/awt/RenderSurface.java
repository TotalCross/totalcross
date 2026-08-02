// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.simulator.awt;

/**
 * Pixel presentation boundary for future IDE and simulator backends.
 */
public interface RenderSurface {
  void resize(int width, int height, int scale);

  void present(int[] pixels, int width, int height);
}
