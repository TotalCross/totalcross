// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.preview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

class HeadlessPreviewSurfaceTest {
  @Test
  void presentStoresLatestFrameAndIncrementsFrameNumber() {
    HeadlessPreviewSurface surface = new HeadlessPreviewSurface();
    BufferedImage frame = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
    frame.setRGB(1, 1, 0xFF112233);

    surface.present(frame);

    BufferedImage latest = surface.getLatestFrame();
    assertNotNull(latest);
    assertEquals(1, surface.getFrameNumber());
    assertEquals(0xFF112233, latest.getRGB(1, 1));
  }

  @Test
  void presentAndReadUseDefensiveCopies() {
    HeadlessPreviewSurface surface = new HeadlessPreviewSurface();
    BufferedImage frame = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    frame.setRGB(0, 0, 0xFF000001);

    surface.present(frame);
    frame.setRGB(0, 0, 0xFF000002);
    BufferedImage firstRead = surface.getLatestFrame();
    firstRead.setRGB(0, 0, 0xFF000003);
    BufferedImage secondRead = surface.getLatestFrame();

    assertNotSame(frame, firstRead);
    assertNotSame(firstRead, secondRead);
    assertEquals(0xFF000001, secondRead.getRGB(0, 0));
  }
}
