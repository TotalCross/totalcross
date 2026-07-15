// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.preview;

import java.awt.image.BufferedImage;

/**
 * Presentation target for frames rendered by the Java desktop launcher.
 * <p>
 * Future IDE integrations should consume rendered frames through this
 * abstraction, or through {@code totalcross.PreviewRunner}, instead of
 * depending directly on Applet painting APIs.
 */
public interface PreviewSurface {
  /**
   * Presents a fully rendered TotalCross frame.
   *
   * @param image
   *          the launcher-owned frame image. Implementations that retain the
   *          frame beyond this call must make their own copy.
   */
  void present(BufferedImage image);
}
