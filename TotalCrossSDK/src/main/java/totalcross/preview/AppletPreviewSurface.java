// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.preview;

import java.awt.Component;

/**
 * Compatibility wrapper for the existing Applet/AWT launcher presentation path.
 * New simulator code should use {@link AwtCanvasSurface} directly.
 */
@Deprecated
public class AppletPreviewSurface extends AwtCanvasSurface {
  public AppletPreviewSurface(Component component, double scale, boolean fastScale) {
    super(component, scale, fastScale);
  }
}
