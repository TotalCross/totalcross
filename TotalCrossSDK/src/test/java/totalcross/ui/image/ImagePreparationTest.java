// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

class ImagePreparationTest {
  @Test
  void candidateUsesExactDestinationScaleAndLeavesPipelineDeferred() throws Exception {
    Image image = new Image(jpeg(64, 48)).getSmoothScaledInstance(16, 12);
    ImagePreparation.Request first = image.createPreparationRequest(1);
    ImagePreparation.Request second = image.createPreparationRequest(2);

    assertEquals(-1, first.status);
    assertEquals(-1, second.status);
    org.junit.jupiter.api.Assertions.assertNotEquals(first.scaleBits, second.scaleBits);
    assertSame(image.pipelineForSmoke(), first.pipeline);
    EncodedImageSource source = first.source;
    assertEquals(0, source.decodedGeneration());

    ImagePreparationCandidate candidate = image.createPreparationCandidate(first);
    assertNotNull(candidate);
    image.adoptPreparationCandidate(first, candidate);
    image.finishPreparation(first);

    assertEquals(1, source.decodedGeneration());
    assertNotNull(source.decodedBackingForReuse(first.denominator));
    assertSame(first.pipeline, image.pipelineForSmoke());
  }

  @Test
  void sourceGenerationChangesPreparationKeyAfterEviction() throws Exception {
    Image image = new Image(jpeg(64, 48)).getSmoothScaledInstance(16, 12);
    ImagePreparation.Request first = image.createPreparationRequest(1);
    ImagePreparation.Request same = image.createPreparationRequest(1);
    assertEquals(first.sourceGeneration, same.sourceGeneration);
    assertEquals(first.scaleBits, same.scaleBits);

    EncodedImageSource source = first.source;
    source.installDecodedBacking(new RasterImageBacking(8, 6, 1, 8, new int[48], null), 8, 6, 8);
    ImagePreparation.Request afterEviction = image.createPreparationRequest(1);
    source.evictDecodedBacking();
    ImagePreparation.Request afterGenerationChange = image.createPreparationRequest(1);

    assertEquals(1, afterEviction.sourceGeneration);
    assertEquals(2, afterGenerationChange.sourceGeneration);
    org.junit.jupiter.api.Assertions.assertNotEquals(first.sourceGeneration,
        afterGenerationChange.sourceGeneration);
  }

  private static byte[] jpeg(int width, int height) throws Exception {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    ImageIO.write(image, "jpeg", bytes);
    return bytes.toByteArray();
  }
}
