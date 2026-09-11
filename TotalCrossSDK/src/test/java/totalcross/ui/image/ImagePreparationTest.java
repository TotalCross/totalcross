// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import totalcross.Launcher;
import totalcross.sys.Settings;
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
  void copyReadyCopyRectMatchesNormalDeferredPixels() throws Exception {
    Image normal = new Image(jpeg(64, 48)).getSmoothScaledInstance(16, 12);
    Image expected = Image.createLogical(16, 12, 1);
    expected.getGraphics().copyRect(normal, 0, 0, 16, 12, 0, 0);

    Image prepared = new Image(jpeg(64, 48)).getSmoothScaledInstance(16, 12);
    ImagePreparation.Request request = prepared.createPreparationRequest(1, false,
        ImageDrawingBridge.COPY_READY);
    ImagePreparationCandidate candidate = prepared.createPreparationCandidate(request);
    prepared.adoptPreparationCandidate(request, candidate);
    prepared.finishPreparation(request, ImageDrawingBridge.COPY_READY);

    Image actual = Image.createLogical(16, 12, 1);
    actual.getGraphics().copyRect(prepared, 0, 0, 16, 12, 0, 0);
    assertArrayEquals(expected.getPixels(), actual.getPixels());
  }

  @Test
  void nonPrefetchableImageRetainsNormalFallback() throws Exception {
    Image image = new Image(png(2, 2)).getSmoothScaledInstance(2, 2);
    ImagePreparation.Request request = image.createPreparationRequest(1, false,
        ImageDrawingBridge.COPY_READY);

    assertEquals(ImagePreparation.NOT_PREFETCHABLE, request.status);
    assertEquals(0, ImagePreparation.activeEntryCountForTest());
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
  void alreadyDecodedCopyReadyAdoptionIsDeferredOnUiThread() throws Exception {
    new Launcher();
    MainWindow.resetPreviewState();
    QueuedMainWindow mainWindow = new QueuedMainWindow();
    ImagePreparation.setRunUiInlineForTest(true);
    Image.resetImageOperationAccountingForTest();
    ImagePreparation.resetAccountingForTest();
    try {
      Image image = alreadyDecodedImage();
      Image.resetImageOperationAccountingForTest();
      int materializations = Image.materializationCountForTest();
      CountDownLatch completed = new CountDownLatch(1);

      ImagePreparation.request(image, 1, ImageDrawingBridge.COPY_READY,
          completed::countDown);

      assertEquals(1, ImagePreparation.activeEntryCountForTest());
      assertEquals(1, mainWindow.queuedCount());
      assertEquals(materializations, Image.materializationCountForTest());
      assertEquals(1, completed.getCount());

      mainWindow.runNext();

      assertTrue(completed.await(0, TimeUnit.MILLISECONDS));
      assertEquals(0, ImagePreparation.activeEntryCountForTest());
      assertEquals(materializations + 1, Image.materializationCountForTest());
      assertEquals(0, Image.targetedDecodeInvocationCountForTest()
          + Image.fullDecodeInvocationCountForTest());
    } finally {
      while (mainWindow.queuedCount() > 0) {
        mainWindow.runNext();
      }
      ImagePreparation.setRunUiInlineForTest(false);
      MainWindow.resetPreviewState();
    }
  }

  @Test
  void alreadyDecodedEntriesUseQueuedContinuations() throws Exception {
    new Launcher();
    MainWindow.resetPreviewState();
    QueuedMainWindow mainWindow = new QueuedMainWindow();
    ImagePreparation.setRunUiInlineForTest(true);
    Image.resetImageOperationAccountingForTest();
    ImagePreparation.resetAccountingForTest();
    try {
      final int count = 3;
      ArrayList<Image> images = new ArrayList<Image>(count);
      CountDownLatch completed = new CountDownLatch(count);
      for (int i = 0; i < count; i++) {
        Image image = alreadyDecodedImage();
        images.add(image);
        ImagePreparation.request(image, 1, ImageDrawingBridge.COPY_READY,
            completed::countDown);
      }
      Image.resetImageOperationAccountingForTest();
      int materializations = Image.materializationCountForTest();

      assertEquals(count, ImagePreparation.activeEntryCountForTest());
      assertEquals(1, mainWindow.queuedCount());
      assertEquals(materializations, Image.materializationCountForTest());
      assertEquals(count, completed.getCount());

      for (int i = 0; i < count; i++) {
        mainWindow.runNext();
        assertEquals(count - i - 1, ImagePreparation.activeEntryCountForTest());
        assertEquals(i + 1 < count ? 1 : 0, mainWindow.queuedCount());
      }

      assertTrue(completed.await(0, TimeUnit.MILLISECONDS));
      assertEquals(0, ImagePreparation.activeEntryCountForTest());
      assertEquals(materializations + count, Image.materializationCountForTest());
      assertEquals(0, Image.targetedDecodeInvocationCountForTest()
          + Image.fullDecodeInvocationCountForTest());
      for (Image image : images) {
        assertNotNull(image.cachedMaterializedForDrawing(1));
      }
    } finally {
      while (mainWindow.queuedCount() > 0) {
        mainWindow.runNext();
      }
      ImagePreparation.setRunUiInlineForTest(false);
      MainWindow.resetPreviewState();
    }
  }

  @Test
  void inFlightDrawRequestPromotesToCopyReady() throws Exception {
    MainWindow.resetPreviewState();
    ImagePreparation.resetAccountingForTest();
    Image.resetImageOperationAccountingForTest();
    Image image = new Image(jpeg(1024, 768)).getSmoothScaledInstance(256, 192);
    CountDownLatch completed = new CountDownLatch(2);

    ImagePreparation.request(image, 1, ImageDrawingBridge.DRAW_READY, completed::countDown);
    ImagePreparation.request(image, 1, ImageDrawingBridge.COPY_READY, completed::countDown);

    assertTrue(completed.await(5, TimeUnit.SECONDS));
    assertEquals(0, ImagePreparation.activeEntryCountForTest());
    assertNotNull(image.cachedMaterializedForDrawing(1));
    assertEquals(2, ImagePreparation.readyCountForTest());
    assertEquals(1, Image.targetedDecodeInvocationCountForTest()
        + Image.fullDecodeInvocationCountForTest());
  }

  @Test
  void sharedSourceCopyReadyVariantsSurviveSiblingCleanup() throws Exception {
    Image base = new Image(jpeg(1024, 768));
    Image small = base.getSmoothScaledInstance(128, 96);
    Image large = base.getSmoothScaledInstance(512, 384);
    ImagePreparation.Request smallRequest = small.createPreparationRequest(1, false,
        ImageDrawingBridge.COPY_READY);
    ImagePreparation.Request largeRequest = large.createPreparationRequest(1, false,
        ImageDrawingBridge.COPY_READY);

    assertSame(smallRequest.source, largeRequest.source);
    assertTrue(smallRequest.denominator > largeRequest.denominator);
    ImagePreparationCandidate smallCandidate = small.createPreparationCandidate(smallRequest);
    ImagePreparationCandidate largeCandidate = large.createPreparationCandidate(largeRequest);

    large.adoptPreparationCandidate(largeRequest, largeCandidate);
    large.finishPreparation(largeRequest, ImageDrawingBridge.COPY_READY);
    Image largeRaster = large.cachedMaterializedForDrawing(1);
    assertNotNull(largeRaster);

    small.adoptPreparationCandidate(smallRequest, smallCandidate);
    small.finishPreparation(smallRequest, ImageDrawingBridge.COPY_READY);
    Image smallRaster = small.cachedMaterializedForDrawing(1);
    assertNotNull(smallRaster);
    int materializations = Image.materializationCountForTest();

    ImagePreparation.Request repeatedLarge = large.createPreparationRequest(1, false,
        ImageDrawingBridge.COPY_READY);
    ImagePreparation.Request repeatedSmall = small.createPreparationRequest(1, false,
        ImageDrawingBridge.COPY_READY);
    assertEquals(ImagePreparation.READY, repeatedLarge.status);
    assertEquals(ImagePreparation.READY, repeatedSmall.status);
    assertSame(largeRaster, large.cachedMaterializedForDrawing(1));
    assertSame(smallRaster, small.cachedMaterializedForDrawing(1));

    Image target = Image.createLogical(512, 384, 1);
    target.getGraphics().copyRect(large, 0, 0, 512, 384, 0, 0);
    target.getGraphics().copyRect(small, 0, 0, 128, 96, 0, 0);
    assertEquals(materializations, Image.materializationCountForTest());
    assertEquals(0, ImagePreparation.activeEntryCountForTest());
  }

  @Test
  void sharedSourceDrawPlanSurvivesCopySiblingCleanup() throws Exception {
    Image base = new Image(jpeg(1024, 768));
    Image draw = base.getSmoothScaledInstance(512, 384);
    Image copy = base.getSmoothScaledInstance(128, 96);
    ImagePreparation.Request drawRequest = draw.createPreparationRequest(1, false,
        ImageDrawingBridge.DRAW_READY);
    ImagePreparation.Request copyRequest = copy.createPreparationRequest(1, false,
        ImageDrawingBridge.COPY_READY);
    ImagePreparationCandidate drawCandidate = draw.createPreparationCandidate(drawRequest);
    ImagePreparationCandidate copyCandidate = copy.createPreparationCandidate(copyRequest);

    draw.adoptPreparationCandidate(drawRequest, drawCandidate);
    draw.finishPreparation(drawRequest, ImageDrawingBridge.DRAW_READY);
    EncodedImageSource source = drawRequest.source;
    ImageBacking higherDetail = source.decodedBackingForReuse(copyRequest.denominator);
    assertNotNull(higherDetail);
    long generation = source.decodedGeneration();

    copy.adoptPreparationCandidate(copyRequest, copyCandidate);
    assertSame(higherDetail, source.decodedBackingForReuse(copyRequest.denominator));
    assertEquals(generation, source.decodedGeneration());
    copy.finishPreparation(copyRequest, ImageDrawingBridge.COPY_READY);
    assertNotNull(copy.cachedMaterializedForDrawing(1));
    assertEquals(generation, source.decodedGeneration());
    if (!Settings.onJavaSE) {
      assertNotNull(draw.drawPlanForDrawing(1));
    }
    assertEquals(0, ImagePreparation.activeEntryCountForTest());
  }

  @Test
  void sourceContentIdentitySurvivesDecodedBackingEviction() throws Exception {
    Image image = new Image(jpeg(64, 48)).getSmoothScaledInstance(16, 12);
    ImagePreparation.Request first = image.createPreparationRequest(1);
    ImagePreparation.Request same = image.createPreparationRequest(1);
    assertEquals(first.sourceContentIdentity, same.sourceContentIdentity);
    assertEquals(first.scaleBits, same.scaleBits);

    EncodedImageSource source = first.source;
    source.installDecodedBacking(new RasterImageBacking(8, 6, 1, 8, new int[48], null), 8, 6, 8);
    ImagePreparation.Request afterEviction = image.createPreparationRequest(1);
    source.evictDecodedBacking();
    ImagePreparation.Request afterGenerationChange = image.createPreparationRequest(1);

    assertEquals(first.sourceContentIdentity, afterEviction.sourceContentIdentity);
    assertEquals(first.sourceContentIdentity, afterGenerationChange.sourceContentIdentity);
    assertEquals(-1, afterGenerationChange.status);
  }

  private static byte[] jpeg(int width, int height) throws Exception {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    ImageIO.write(image, "jpeg", bytes);
    return bytes.toByteArray();
  }

  private static byte[] png(int width, int height) throws Exception {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    ImageIO.write(image, "png", bytes);
    return bytes.toByteArray();
  }

  private static Image alreadyDecodedImage() throws Exception {
    Image image = new Image(jpeg(64, 48)).getSmoothScaledInstance(16, 12);
    ImagePreparation.Request request = image.createPreparationRequest(1, false,
        ImageDrawingBridge.COPY_READY);
    ImagePreparationCandidate candidate = image.createPreparationCandidate(request);
    image.adoptPreparationCandidate(request, candidate);
    return image;
  }

  private static final class QueuedMainWindow extends MainWindow {
    private final ArrayList<Runnable> queued = new ArrayList<Runnable>();

    @Override
    public void runOnMainThread(Runnable runnable, boolean singleInstance) {
      queued.add(runnable);
    }

    int queuedCount() {
      return queued.size();
    }

    void runNext() {
      assertFalse(queued.isEmpty());
      queued.remove(0).run();
    }
  }
}
