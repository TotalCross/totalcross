// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.style.render;

import totalcross.ui.Insets;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.gfx.RRect;

/**
 * Describes the visual rendering contract used by a control.
 */
public interface ControlRenderer {
    /**
     * Paints the control for the given bounds.
     */
    void draw(Graphics g, int x, int y, int w, int h, boolean pressed);

    /**
     * Returns how much the renderer extends beyond the control bounds.
     */
    double getOutset();

    /**
     * Returns the effective insets contributed by the renderer.
     */
    Insets getInsets();

    /**
     * Returns whether child painting should be clipped.
     */
    boolean shouldClipChildren();

    /**
     * Returns the clip used for children at the given control size, or {@code null} when child clipping is disabled.
     */
    RRect getChildrenClip(int width, int height);
}
