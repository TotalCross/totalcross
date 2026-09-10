// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import totalcross.ui.MainWindow;

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
  void drawReadyLeavesFinalRasterDeferred() throws Exception {
    Image image = new Image(jpeg(64, 48)).getSmoothScaledInstance(16, 12);
    ImagePreparation.Request request = image.createPreparationRequest(1, false,
        ImageDrawingBridge.DRAW_READY);
    ImagePreparationCandidate candidate = image.createPreparationCandidate(request);

    image.adoptPreparationCandidate(request, candidate);
    image.finishPreparation(request, ImageDrawingBridge.DRAW_READY);

    assertNull(image.cachedMaterializedForDrawing(1));
    assertSame(request.pipeline, image.pipelineForSmoke());
  }

  @Test
  void copyReadyCachesFinalRasterAndRepeatedRequestDoesNoWork() throws Exception {
    Image image = new Image(jpeg(64, 48)).getSmoothScaledInstance(16, 12);
    ImagePreparation.Request request = image.createPreparationRequest(1, false,
        ImageDrawingBridge.COPY_READY);
    ImagePreparationCandidate candidate = image.createPreparationCandidate(request);

    image.adoptPreparationCandidate(request, candidate);
    image.finishPreparation(request, ImageDrawingBridge.COPY_READY);
    int materializations = Image.materializationCountForTest();

    assertNotNull(image.cachedMaterializedForDrawing(1));
    ImagePreparation.Request repeated = image.createPreparationRequest(1, false,
        ImageDrawingBridge.COPY_READY);
    assertEquals(ImagePreparation.READY, repeated.status);
    assertEquals(materializations, Image.materializationCountForTest());
  }

  @Test
  void terminalFailureIsRemovedAndSameKeyCanRetry() throws Exception {
    MainWindow.resetPreviewState();
    ImagePreparation.resetAccountingForTest();
    Image image = new Image(jpeg(512, 384)).getSmoothScaledInstance(128, 96);
    Image.failNextTargetedDecodeInfrastructureForTest();
    CountDownLatch failed = new CountDownLatch(1);

    ImagePreparation.request(image, 1, ImageDrawingBridge.COPY_READY, failed::countDown);
    assertTrue(failed.await(5, TimeUnit.SECONDS));
    assertEquals(0, ImagePreparation.activeEntryCountForTest());
    assertEquals(1, ImagePreparation.failedCountForTest());

    CountDownLatch retried = new CountDownLatch(1);
    ImagePreparation.request(image, 1, ImageDrawingBridge.COPY_READY, retried::countDown);
    assertTrue(retried.await(5, TimeUnit.SECONDS));
    assertEquals(0, ImagePreparation.activeEntryCountForTest());
    assertEquals(1, ImagePreparation.readyCountForTest());
  }

  @Test
  void inFlightDrawRequestPromotesToCopyReady() throws Exception {
    MainWindow.resetPreviewState();
    ImagePreparation.resetAccountingForTest();
    Image image = new Image(jpeg(1024, 768)).getSmoothScaledInstance(256, 192);
    CountDownLatch completed = new CountDownLatch(2);

    ImagePreparation.request(image, 1, ImageDrawingBridge.DRAW_READY, completed::countDown);
    ImagePreparation.request(image, 1, ImageDrawingBridge.COPY_READY, completed::countDown);

    assertTrue(completed.await(5, TimeUnit.SECONDS));
    assertEquals(0, ImagePreparation.activeEntryCountForTest());
    assertNotNull(image.cachedMaterializedForDrawing(1));
    assertEquals(2, ImagePreparation.readyCountForTest());
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
