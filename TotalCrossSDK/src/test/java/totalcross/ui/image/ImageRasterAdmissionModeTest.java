// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

class ImageRasterAdmissionModeTest {
  @Test
  void genericResolutionStillWaitsForTheSecondObservation() throws Exception {
    Image image = deferredScale();
    ImagePipeline pipeline = image.pipelineForSmoke();

    image.resolveForDrawing(2);
    assertNull(image.cachedFinalRasterForDrawing(2));
    assertNull(image.cachedFinalRasterForDrawing(1));

    Image second = image.resolveForDrawing(2);

    assertSame(second, image.cachedFinalRasterForDrawing(2));
    assertNull(image.cachedFinalRasterForDrawing(1));
    assertEquals(1, pipeline.cachedVariantCountForSmoke());
  }

  @Test
  void immediateAdmissionUsesTheExistingSingleExactScaleSlot() throws Exception {
    Image image = deferredScale();
    ImagePipeline pipeline = image.pipelineForSmoke();

    Image atTwo = image.resolveForDrawing(2, ImageRasterAdmission.IMMEDIATE);

    assertSame(atTwo, image.cachedFinalRasterForDrawing(2));
    assertNull(image.cachedFinalRasterForDrawing(1));
    assertEquals(1, pipeline.cachedVariantCountForSmoke());

    Image atOne = image.resolveForDrawing(1, ImageRasterAdmission.IMMEDIATE);

    assertSame(atOne, image.cachedFinalRasterForDrawing(1));
    assertNull(image.cachedFinalRasterForDrawing(2));
    assertEquals(1, pipeline.cachedVariantCountForSmoke());
  }

  private static Image deferredScale() throws Exception {
    BufferedImage raster = new BufferedImage(16, 12, BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < raster.getHeight(); y++) {
      for (int x = 0; x < raster.getWidth(); x++) {
        raster.setRGB(x, y, 0xFF204060 | (x << 8) | y);
      }
    }
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ImageIO.write(raster, "png", output);
    return new Image(output.toByteArray()).getSmoothScaledInstance(8, 6);
  }
}
