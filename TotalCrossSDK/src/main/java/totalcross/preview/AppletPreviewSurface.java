// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.preview;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;
import net.coobird.thumbnailator.resizers.configurations.Antialiasing;
import net.coobird.thumbnailator.resizers.configurations.Dithering;
import net.coobird.thumbnailator.resizers.configurations.Rendering;
import net.coobird.thumbnailator.resizers.configurations.ScalingMode;
import totalcross.ui.UIColors;

/**
 * Applet/AWT backed preview surface used by the existing desktop launcher.
 */
public class AppletPreviewSurface implements PreviewSurface {
  private final Component component;
  private final double scale;
  private final boolean fastScale;
  private BufferedImage scaledImageSource;
  private Builder<BufferedImage> thumbnailBuilder;

  public AppletPreviewSurface(Component component, double scale, boolean fastScale) {
    this.component = component;
    this.scale = scale;
    this.fastScale = fastScale;
  }

  @Override
  public void present(BufferedImage image) {
    Graphics g = component.getGraphics();
    int w = image.getWidth();
    int h = image.getHeight();
    int ww = (int) (w * scale);
    int hh = (int) (h * scale);
    int shiftY = totalcross.ui.Window.shiftY;
    int shiftH = totalcross.ui.Window.shiftH;
    if ((shiftY + shiftH) > h) {
      totalcross.ui.Window.shiftY = shiftY = h - shiftH;
    }
    if (shiftY != 0) {
      g.setColor(new Color(UIColors.unsafeAreaColor));
      int yy = (int) (shiftH * scale);
      g.fillRect(0, yy, ww, hh - yy); // erase empty area
      g.setClip(0, 0, ww, yy); // limit drawing area
      g.translate(0, -(int) (shiftY * scale));
    }
    if (scale != 1) // guich@tc126_74 - guich@tc130
    {
      if (fastScale) {
        g.drawImage(image, 0, 0, ww, hh, 0, 0, w, h, component);
      } else {
        try {
          g.drawImage(getThumbnailBuilder(image, ww, hh).asBufferedImage(), 0, 0, component);
        } catch (java.io.IOException e) {
          e.printStackTrace();
        }
      }
    } else if (g != null) {
      g.drawImage(image, 0, 0, ww, hh, 0, 0, w, h, component); // this is faster than use img.getScaledInstance
    }
    if (shiftY != 0) {
      g.translate(0, (int) (shiftY * scale));
      g.setClip(0, 0, ww, hh);
    }
  }

  private Builder<BufferedImage> getThumbnailBuilder(BufferedImage image, int width, int height) {
    if (thumbnailBuilder == null || scaledImageSource != image) {
      scaledImageSource = image;
      // The builder keeps a reference to the image, so it can be reused while
      // the launcher keeps rendering into the same BufferedImage instance.
      thumbnailBuilder = Thumbnails
          .of(image)
          .size(width, height)
          .rendering(Rendering.SPEED)
          .scalingMode(ScalingMode.PROGRESSIVE_BILINEAR)
          .antialiasing(Antialiasing.OFF)
          .dithering(Dithering.DISABLE);
    }
    return thumbnailBuilder;
  }
}
