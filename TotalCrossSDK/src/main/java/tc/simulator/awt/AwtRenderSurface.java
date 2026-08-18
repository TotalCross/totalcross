// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.simulator.awt;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;
import net.coobird.thumbnailator.resizers.configurations.Antialiasing;
import net.coobird.thumbnailator.resizers.configurations.Dithering;
import net.coobird.thumbnailator.resizers.configurations.Rendering;
import net.coobird.thumbnailator.resizers.configurations.ScalingMode;
import totalcross.ui.UIColors;

/**
 * AWT render surface for desktop simulator and preview windows.
 * <p>
 * It owns the rendered image used by {@link RenderSurface#present(int[], int, int)}
 * so the simulator can keep creating and aliasing its BufferedImage exactly as
 * before.
 */
public class AwtRenderSurface extends Canvas implements RenderSurface {
  private final Component paintTarget;
  private final double presentationScale;
  private final double contentScale;
  private final boolean fastScale;
  private BufferedImage image;
  private BufferedImage scaledImageSource;
  private Builder<BufferedImage> thumbnailBuilder;

  public AwtRenderSurface() {
    this(null, 1, false);
  }

  public AwtRenderSurface(Component paintTarget, double scale, boolean fastScale) {
    this(paintTarget, scale, 1, fastScale);
  }

  public AwtRenderSurface(Component paintTarget, double presentationScale, double contentScale, boolean fastScale) {
    if (!Double.isFinite(presentationScale) || presentationScale <= 0 || !Double.isFinite(contentScale)
        || contentScale <= 0) {
      throw new IllegalArgumentException("surface scales must be finite and positive");
    }
    this.paintTarget = paintTarget == null ? this : paintTarget;
    this.presentationScale = presentationScale;
    this.contentScale = contentScale;
    this.fastScale = fastScale;
  }

  @Override
  public void resize(int width, int height, int scale) {
    image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
  }

  @Override
  public void present(int[] pixels, int width, int height) {
    if (image == null || image.getWidth() != width || image.getHeight() != height) {
      resize(width, height, 1);
    }
    int[] target = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    System.arraycopy(pixels, 0, target, 0, Math.min(pixels.length, target.length));
    present(image);
  }

  public void present(BufferedImage image) {
    this.image = image;
    Graphics graphics = paintTarget.getGraphics();
    if (graphics != null) {
      paintImage(graphics, image, paintTarget);
    } else {
      paintTarget.repaint();
    }
  }

  @Override
  public void paint(Graphics graphics) {
    if (image != null) {
      paintImage(graphics, image, this);
    }
  }

  @Override
  public void update(Graphics graphics) {
    paint(graphics);
  }

  private void paintImage(Graphics graphics, BufferedImage image, Component observer) {
    int width = image.getWidth();
    int height = image.getHeight();
    double backingToHostScale = presentationScale / contentScale;
    int scaledWidth = (int) Math.round(width * backingToHostScale);
    int scaledHeight = (int) Math.round(height * backingToHostScale);
    int shiftY = totalcross.ui.Window.shiftY;
    int shiftH = totalcross.ui.Window.shiftH;
    int logicalHeight = (int) Math.round(height / contentScale);
    if ((shiftY + shiftH) > logicalHeight) {
      totalcross.ui.Window.shiftY = shiftY = logicalHeight - shiftH;
    }
    if (shiftY != 0) {
      graphics.setColor(new Color(UIColors.unsafeAreaColor));
      int yy = (int) Math.round(shiftH * presentationScale);
      graphics.fillRect(0, yy, scaledWidth, scaledHeight - yy);
      graphics.setClip(0, 0, scaledWidth, yy);
      graphics.translate(0, -(int) Math.round(shiftY * presentationScale));
    }
    if (backingToHostScale != 1) {
      if (fastScale) {
        graphics.drawImage(image, 0, 0, scaledWidth, scaledHeight, 0, 0, width, height, observer);
      } else {
        try {
          graphics.drawImage(getThumbnailBuilder(image, scaledWidth, scaledHeight).asBufferedImage(), 0, 0, observer);
        } catch (java.io.IOException e) {
          e.printStackTrace();
        }
      }
    } else {
      graphics.drawImage(image, 0, 0, scaledWidth, scaledHeight, 0, 0, width, height, observer);
    }
    if (shiftY != 0) {
      graphics.translate(0, (int) Math.round(shiftY * presentationScale));
      graphics.setClip(0, 0, scaledWidth, scaledHeight);
    }
  }

  private Builder<BufferedImage> getThumbnailBuilder(BufferedImage image, int width, int height) {
    if (thumbnailBuilder == null || scaledImageSource != image) {
      scaledImageSource = image;
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
