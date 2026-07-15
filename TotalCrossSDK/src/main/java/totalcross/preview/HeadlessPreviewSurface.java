// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.preview;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

/**
 * Preview surface that retains the latest rendered frame without showing UI.
 */
public class HeadlessPreviewSurface implements PreviewSurface {
  private BufferedImage latestFrame;
  private long frameNumber;

  @Override
  public synchronized void present(BufferedImage image) {
    latestFrame = copy(image);
    frameNumber++;
  }

  /**
   * Returns a copy of the latest frame, or {@code null} if no frame was rendered yet.
   */
  public synchronized BufferedImage getLatestFrame() {
    return latestFrame == null ? null : copy(latestFrame);
  }

  public synchronized long getFrameNumber() {
    return frameNumber;
  }

  private BufferedImage copy(BufferedImage image) {
    if (image.getType() == BufferedImage.TYPE_CUSTOM) {
      ColorModel colorModel = image.getColorModel();
      WritableRaster raster = image.copyData(null);
      return new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);
    }
    BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
    Graphics2D graphics = copy.createGraphics();
    graphics.drawImage(image, 0, 0, null);
    graphics.dispose();
    return copy;
  }
}
