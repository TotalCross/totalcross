// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.preview;

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
 * and also implements the {@link PreviewRuntime.FrameConsumer} image presentation path
 * so Launcher.updateScreen() can keep creating and aliasing its BufferedImage
 * exactly as before.
 */
public class AwtCanvasSurface extends Canvas implements RenderSurface, PreviewRuntime.FrameConsumer {
  private final Component paintTarget;
  private final double scale;
  private final boolean fastScale;
  private BufferedImage image;
  private BufferedImage scaledImageSource;
  private Builder<BufferedImage> thumbnailBuilder;

  public AwtCanvasSurface() {
    this(null, 1, false);
  }

  public AwtCanvasSurface(Component paintTarget, double scale, boolean fastScale) {
    this.paintTarget = paintTarget == null ? this : paintTarget;
    this.scale = scale;
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

  @Override
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
    int scaledWidth = (int) (width * scale);
    int scaledHeight = (int) (height * scale);
    int shiftY = totalcross.ui.Window.shiftY;
    int shiftH = totalcross.ui.Window.shiftH;
    if ((shiftY + shiftH) > height) {
      totalcross.ui.Window.shiftY = shiftY = height - shiftH;
    }
    if (shiftY != 0) {
      graphics.setColor(new Color(UIColors.unsafeAreaColor));
      int yy = (int) (shiftH * scale);
      graphics.fillRect(0, yy, scaledWidth, scaledHeight - yy);
      graphics.setClip(0, 0, scaledWidth, yy);
      graphics.translate(0, -(int) (shiftY * scale));
    }
    if (scale != 1) {
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
      graphics.translate(0, (int) (shiftY * scale));
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
