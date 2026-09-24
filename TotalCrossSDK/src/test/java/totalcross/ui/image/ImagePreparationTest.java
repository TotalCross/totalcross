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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.CRC32;

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
  void jpegRequestKeepsItsChosenDenominatorAndNativeAvailability() throws Exception {
    Image image = new Image(jpeg(512, 384)).getSmoothScaledInstance(128, 96);
    ImagePreparation.Request request = image.createPreparationRequest(1, true,
        ImageDrawingBridge.DRAW_READY);

    assertEquals(-1, request.status);
    assertEquals(ImageDecodeRequirement.choose(request.source, request.pipeline,
        request.targetWidth, request.targetHeight), request.denominator);
    assertTrue(request.nativeAvailable);
  }

  @Test
  void staticPngUsesDenominatorOneAndReusesDecodedBacking() throws Exception {
    Image image = new Image(png(8, 6)).getSmoothScaledInstance(4, 3);
    ImagePreparation.Request request = image.createPreparationRequest(2, true,
        ImageDrawingBridge.COPY_READY);

    assertEquals(-1, request.status);
    assertEquals(ImageEncodedStructure.Format.PNG, request.source.getFormat());
    assertEquals(8, request.targetWidth);
    assertEquals(6, request.targetHeight);
    assertEquals(1, request.denominator);
    assertFalse(request.nativeAvailable);

    request.source.installDecodedBacking(new RasterImageBacking(request.targetWidth,
        request.targetHeight, 1, request.targetWidth,
        new int[request.targetWidth * request.targetHeight], null),
        request.targetWidth, request.targetHeight, 1);
    ImagePreparation.Request reused = image.createPreparationRequest(2, true,
        ImageDrawingBridge.COPY_READY);

    assertTrue(reused.alreadyDecoded);
    assertEquals(-1, reused.status);
    assertEquals(1, reused.denominator);
  }

  @Test
  void multiFramePngAndUnsupportedFormatsRemainNotPrefetchable() throws Exception {
    Image animatedPng = new Image(pngWithFrameCount(8, 6, 2)).getSmoothScaledInstance(4, 3);
    ImagePreparation.Request animatedRequest = animatedPng.createPreparationRequest(1, false,
        ImageDrawingBridge.COPY_READY);
    Image bitmap = new Image(bmp(8, 6)).getSmoothScaledInstance(4, 3);
    ImagePreparation.Request bitmapRequest = bitmap.createPreparationRequest(1, false,
        ImageDrawingBridge.COPY_READY);

    assertEquals(2, animatedRequest.source.getFrameCount());
    assertEquals(ImagePreparation.NOT_PREFETCHABLE, animatedRequest.status);
    assertEquals(ImageEncodedStructure.Format.BMP, bitmapRequest.source.getFormat());
    assertEquals(ImagePreparation.NOT_PREFETCHABLE, bitmapRequest.status);
  }

  @Test
  void staticPngPrefetchFinishesReadyOnceAndReusesTheDecodedBacking() throws Exception {
    ImagePreparation.resetThreadModeForTest();
    ImagePreparation.setRunUiInlineForTest(true);
    MainWindow.resetPreviewState();
    Image.resetImageOperationAccountingForTest();
    ImagePreparation.resetAccountingForTest();
    try {
      Image image = new Image(png(8, 6)).getSmoothScaledInstance(4, 3);
      ImagePreparation.Request request = image.createPreparationRequest(1, true,
          ImageDrawingBridge.DRAW_READY);
      CountDownLatch completed = new CountDownLatch(1);
      AtomicInteger callbacks = new AtomicInteger();

      ImagePreparation.request(image, 1, ImageDrawingBridge.DRAW_READY, () -> {
        callbacks.incrementAndGet();
        completed.countDown();
      });

      assertTrue(completed.await(5, TimeUnit.SECONDS));
      assertEquals(1, callbacks.get());
      assertEquals(1, Image.fullDecodeInvocationCountForTest());
      assertNotNull(request.source.decodedBackingForReuse(1));
      assertEquals(1, request.source.decodedGeneration());

      ImagePreparation.Request reused = image.createPreparationRequest(1, true,
          ImageDrawingBridge.DRAW_READY);
      assertEquals(-1, reused.status);
      assertTrue(reused.alreadyDecoded);
      assertFalse(reused.nativeAvailable);
      assertEquals(1, Image.fullDecodeInvocationCountForTest());
    } finally {
      ImagePreparation.setRunUiInlineForTest(false);
      ImagePreparation.resetThreadModeForTest();
      MainWindow.resetPreviewState();
    }
  }

  @Test
  void staticPngDecodeFailureFinishesFailedWithoutBlocking() throws Exception {
    ImagePreparation.resetThreadModeForTest();
    ImagePreparation.setRunUiInlineForTest(true);
    MainWindow.resetPreviewState();
    Image.resetImageOperationAccountingForTest();
    ImagePreparation.resetAccountingForTest();
    try {
      Image image = new Image(corruptPngIdat(png(8, 6))).getSmoothScaledInstance(4, 3);
      CountDownLatch completed = new CountDownLatch(1);
      AtomicInteger callbacks = new AtomicInteger();

      ImagePreparation.request(image, 1, ImageDrawingBridge.COPY_READY, () -> {
        callbacks.incrementAndGet();
        completed.countDown();
      });

      assertTrue(completed.await(5, TimeUnit.SECONDS));
      assertEquals(1, callbacks.get());
      assertEquals(1, ImagePreparation.failedCountForTest());
      assertEquals(0, ImagePreparation.readyCountForTest());
      assertEquals(0, ImagePreparation.activeEntryCountForTest());
    } finally {
      ImagePreparation.setRunUiInlineForTest(false);
      ImagePreparation.resetThreadModeForTest();
      MainWindow.resetPreviewState();
    }
  }

  @Test
  void javaResultCandidateTransfersBackingAndStalePngCandidateReleasesOnce() throws Exception {
    Image image = new Image(png(8, 6)).getSmoothScaledInstance(4, 3);
    ImagePreparation.Request request = image.createPreparationRequest(1, false,
        ImageDrawingBridge.DRAW_READY);
    ImagePreparation.JavaResult result = image.createJavaPreparationResult(request);
    ImageBacking prepared = result.backing;
    ImagePreparation.DetachedCandidate adopted = ImagePreparation.DetachedCandidate.fromJavaResult(result);

    adopted.adoptJavaResult(request);
    assertNull(adopted.javaResult);
    assertSame(prepared, request.source.decodedBackingForReuse(1));
    adopted.release();
    assertSame(prepared, request.source.decodedBackingForReuse(1));

    Image staleImage = new Image(png(8, 6)).getSmoothScaledInstance(4, 3);
    ImagePreparation.Request staleRequest = staleImage.createPreparationRequest(1, false,
        ImageDrawingBridge.DRAW_READY);
    NativeImageBacking nativeBacking = NativeImageBacking.fromHandle(1,
        staleRequest.targetWidth, staleRequest.targetHeight);
    ImagePreparation.DetachedCandidate stale = ImagePreparation.DetachedCandidate.fromJavaResult(
        new ImagePreparation.JavaResult(nativeBacking, staleRequest.targetWidth,
            staleRequest.targetHeight, 1));
    staleImage.getPixels();

    assertThrows(ImageException.class, () -> stale.adoptJavaResult(staleRequest));
    stale.release();
    assertNull(stale.javaResult);
    assertFalse(nativeBacking.isValid());
    stale.release();
    assertFalse(nativeBacking.isValid());
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
    Image image = new Image(bmp(2, 2)).getSmoothScaledInstance(2, 2);
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
  void legacyLifecycleAccountingMeasuresSerializedEntries() throws Exception {
    boolean previousAccounting = Image.diagnosticAccountingEnabledForTest();
    CountDownLatch firstDecodeReachedAdoption = new CountDownLatch(1);
    CountDownLatch continueFirstDecode = new CountDownLatch(1);
    CountDownLatch firstCallbackEntered = new CountDownLatch(1);
    CountDownLatch continueFirstCallback = new CountDownLatch(1);
    CountDownLatch secondCallbackEntered = new CountDownLatch(1);
    CountDownLatch continueSecondCallback = new CountDownLatch(1);
    CountDownLatch completed = new CountDownLatch(2);
    ImagePreparation.setRunUiInlineForTest(true);
    Image.setDiagnosticAccountingForTest(true);
    ImagePreparation.resetAccountingForTest();
    ImagePreparation.setBeforeAdoptionHookForTest(new Runnable() {
      @Override
      public void run() {
        firstDecodeReachedAdoption.countDown();
        try {
          continueFirstDecode.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException interrupted) {
          Thread.currentThread().interrupt();
        }
      }
    });
    try {
      Image first = new Image(jpeg(512, 384)).getSmoothScaledInstance(128, 96);
      Image second = new Image(jpeg(640, 480)).getSmoothScaledInstance(160, 120);

      ImagePreparation.request(first, 1, ImageDrawingBridge.COPY_READY,
          blockingCompletion(firstCallbackEntered, continueFirstCallback, completed));
      assertTrue(firstDecodeReachedAdoption.await(5, TimeUnit.SECONDS));
      ImagePreparation.request(second, 1, ImageDrawingBridge.COPY_READY,
          blockingCompletion(secondCallbackEntered, continueSecondCallback, completed));

      assertEquals(1, ImagePreparation.threadCreateCountForTest());
      assertEquals(1, ImagePreparation.threadStartCountForTest());
      continueFirstDecode.countDown();
      assertTrue(firstCallbackEntered.await(5, TimeUnit.SECONDS));
      assertEquals(0, ImagePreparation.preparationEntryTotalNsForTest());
      continueFirstCallback.countDown();
      assertTrue(secondCallbackEntered.await(5, TimeUnit.SECONDS));
      long firstPreparationTotalNs = ImagePreparation.preparationEntryTotalNsForTest();
      assertTrue(firstPreparationTotalNs > 0);
      continueSecondCallback.countDown();
      assertTrue(completed.await(5, TimeUnit.SECONDS));

      assertEquals(2, ImagePreparation.preparationEntryCountForTest());
      assertEquals(2, ImagePreparation.threadCreateCountForTest());
      assertEquals(2, ImagePreparation.threadStartCountForTest());
      assertEquals(2, ImagePreparation.decodeEntryCountForTest());
      assertEquals(2, ImagePreparation.uiDispatchCountForTest());
      assertDiagnosticMetricsNonNegative();
    } finally {
      continueFirstDecode.countDown();
      continueFirstCallback.countDown();
      continueSecondCallback.countDown();
      ImagePreparation.setBeforeAdoptionHookForTest(null);
      ImagePreparation.setRunUiInlineForTest(false);
      Image.setDiagnosticAccountingForTest(previousAccounting);
      MainWindow.resetPreviewState();
    }
  }

  @Test
  void lifecycleAccountingStaysZeroWhenDisabled() throws Exception {
    boolean previousAccounting = Image.diagnosticAccountingEnabledForTest();
    CountDownLatch completed = new CountDownLatch(1);
    ImagePreparation.setRunUiInlineForTest(true);
    Image.setDiagnosticAccountingForTest(false);
    ImagePreparation.resetAccountingForTest();
    try {
      Image image = new Image(jpeg(512, 384)).getSmoothScaledInstance(128, 96);
      ImagePreparation.request(image, 1, ImageDrawingBridge.COPY_READY, completed::countDown);

      assertTrue(completed.await(5, TimeUnit.SECONDS));
      assertDiagnosticMetricsZero();
      assertEquals(0, ImagePreparation.requestCountForTest());
    } finally {
      ImagePreparation.setRunUiInlineForTest(false);
      Image.setDiagnosticAccountingForTest(previousAccounting);
      MainWindow.resetPreviewState();
    }
  }

  @Test
  void prefetchThreadModeDefaultsToLegacyAndValidatesSleep() {
    ImagePreparation.resetThreadModeForTest();
    assertEquals(ImagePreparation.PREFETCH_THREAD_MODE_LEGACY,
        ImagePreparation.prefetchThreadModeForTest());
    assertEquals(0, ImagePreparation.prefetchWorkerSleepMsForTest());

    ImagePreparation.configurePrefetchThreadModeForDiagnostic(
        ImagePreparation.PREFETCH_THREAD_MODE_LEGACY, 20);
    assertEquals(0, ImagePreparation.prefetchWorkerSleepMsForTest());

    assertWorkerSleepRejected(0);
    assertWorkerSleepRejected(-1);
    ImagePreparation.configurePrefetchThreadModeForDiagnostic(
        ImagePreparation.PREFETCH_THREAD_MODE_WORKER, 1);
    assertEquals(1, ImagePreparation.prefetchWorkerSleepMsForTest());
    ImagePreparation.configurePrefetchThreadModeForDiagnostic(
        ImagePreparation.PREFETCH_THREAD_MODE_WORKER, 2);
    assertEquals(2, ImagePreparation.prefetchWorkerSleepMsForTest());
    ImagePreparation.configurePrefetchThreadModeForDiagnostic(
        ImagePreparation.PREFETCH_THREAD_MODE_WORKER_SEMAPHORE, 0);
    assertEquals(0, ImagePreparation.prefetchWorkerSleepMsForTest());
    assertSemaphoreSleepRejected(1);
    ImagePreparation.resetThreadModeForTest();
  }

  @Test
  void workerModeSerializesMultipleDecodesOnOnePersistentThread() throws Exception {
    boolean previousAccounting = Image.diagnosticAccountingEnabledForTest();
    CountDownLatch firstDecodeReachedAdoption = new CountDownLatch(1);
    CountDownLatch continueFirstDecode = new CountDownLatch(1);
    CountDownLatch completed = new CountDownLatch(2);
    ImagePreparation.resetThreadModeForTest();
    ImagePreparation.configurePrefetchThreadModeForDiagnostic(
        ImagePreparation.PREFETCH_THREAD_MODE_WORKER, 1);
    ImagePreparation.setRunUiInlineForTest(true);
    Image.setDiagnosticAccountingForTest(true);
    ImagePreparation.resetAccountingForTest();
    ImagePreparation.setBeforeAdoptionHookForTest(new Runnable() {
      @Override
      public void run() {
        firstDecodeReachedAdoption.countDown();
        try {
          continueFirstDecode.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException interrupted) {
          Thread.currentThread().interrupt();
        }
      }
    });
    try {
      Image first = new Image(jpeg(512, 384)).getSmoothScaledInstance(128, 96);
      Image second = new Image(jpeg(640, 480)).getSmoothScaledInstance(160, 120);

      ImagePreparation.request(first, 1, ImageDrawingBridge.COPY_READY, completed::countDown);
      assertTrue(firstDecodeReachedAdoption.await(5, TimeUnit.SECONDS));
      assertTrue(ImagePreparation.workerRunningForTest());
      assertTrue(ImagePreparation.activeEntryInFlightForTest());
      ImagePreparation.request(second, 1, ImageDrawingBridge.COPY_READY, completed::countDown);

      assertEquals(1, ImagePreparation.threadCreateCountForTest());
      assertEquals(1, ImagePreparation.decodeEntryCountForTest());
      continueFirstDecode.countDown();
      assertTrue(completed.await(5, TimeUnit.SECONDS));
      awaitWorkerPoll();

      assertEquals(2, ImagePreparation.preparationEntryCountForTest());
      assertEquals(1, ImagePreparation.threadCreateCountForTest());
      assertEquals(1, ImagePreparation.threadStartCountForTest());
      assertEquals(2, ImagePreparation.decodeEntryCountForTest());
      assertTrue(ImagePreparation.workerPollCountForTest() > 0);
      assertTrue(ImagePreparation.workerSleepRequestedNsForTest() > 0);
      assertTrue(ImagePreparation.workerIdleElapsedNsForTest() > 0);
      assertDiagnosticMetricsNonNegative();

      ImagePreparation.shutdownWorkerForTest();
      assertFalse(ImagePreparation.workerRunningForTest());
      assertEquals(ImagePreparation.PREFETCH_THREAD_MODE_WORKER,
          ImagePreparation.prefetchThreadModeForTest());
    } finally {
      continueFirstDecode.countDown();
      ImagePreparation.setBeforeAdoptionHookForTest(null);
      ImagePreparation.setRunUiInlineForTest(false);
      ImagePreparation.resetThreadModeForTest();
      Image.setDiagnosticAccountingForTest(previousAccounting);
      MainWindow.resetPreviewState();
    }
    assertEquals(ImagePreparation.PREFETCH_THREAD_MODE_LEGACY,
        ImagePreparation.prefetchThreadModeForTest());
    assertEquals(0, ImagePreparation.prefetchWorkerSleepMsForTest());
  }

  @Test
  void workerModeKeepsAlreadyDecodedAdoptionOnTheUiQueue() throws Exception {
    boolean previousAccounting = Image.diagnosticAccountingEnabledForTest();
    new Launcher();
    MainWindow.resetPreviewState();
    QueuedMainWindow mainWindow = new QueuedMainWindow();
    ImagePreparation.resetThreadModeForTest();
    ImagePreparation.configurePrefetchThreadModeForDiagnostic(
        ImagePreparation.PREFETCH_THREAD_MODE_WORKER, 2);
    ImagePreparation.setRunUiInlineForTest(true);
    Image.setDiagnosticAccountingForTest(true);
    ImagePreparation.resetAccountingForTest();
    try {
      Image image = alreadyDecodedImage();
      CountDownLatch completed = new CountDownLatch(1);
      ImagePreparation.request(image, 1, ImageDrawingBridge.COPY_READY, completed::countDown);

      assertEquals(1, mainWindow.queuedCount());
      assertFalse(ImagePreparation.workerRunningForTest());
      assertEquals(0, ImagePreparation.threadCreateCountForTest());
      mainWindow.runNext();

      assertTrue(completed.await(0, TimeUnit.MILLISECONDS));
      assertEquals(0, ImagePreparation.threadCreateCountForTest());
      assertEquals(0, ImagePreparation.decodeEntryCountForTest());
      assertEquals(1, ImagePreparation.preparationEntryCountForTest());
      assertTrue(ImagePreparation.finishPreparationNsForTest() >= 0);
    } finally {
      while (mainWindow.queuedCount() > 0) {
        mainWindow.runNext();
      }
      ImagePreparation.setRunUiInlineForTest(false);
      ImagePreparation.resetThreadModeForTest();
      Image.setDiagnosticAccountingForTest(previousAccounting);
      MainWindow.resetPreviewState();
    }
  }

  @Test
  void semaphoreWorkerCoalescesWakeupsAndWaitsForEachUiFinish() throws Exception {
    boolean previousAccounting = Image.diagnosticAccountingEnabledForTest();
    new Launcher();
    MainWindow.resetPreviewState();
    QueuedMainWindow mainWindow = new QueuedMainWindow();
    ImagePreparation.resetThreadModeForTest();
    ImagePreparation.configurePrefetchThreadModeForDiagnostic(
        ImagePreparation.PREFETCH_THREAD_MODE_WORKER_SEMAPHORE, 0);
    ImagePreparation.setRunUiInlineForTest(false);
    Image.setDiagnosticAccountingForTest(true);
    ImagePreparation.resetAccountingForTest();
    try {
      Image first = new Image(jpeg(512, 384)).getSmoothScaledInstance(128, 96);
      Image second = new Image(jpeg(640, 480)).getSmoothScaledInstance(160, 120);
      Image third = new Image(jpeg(720, 540)).getSmoothScaledInstance(180, 135);

      ImagePreparation.request(first, 1, ImageDrawingBridge.COPY_READY, null);
      awaitQueuedAdoption(mainWindow);
      awaitSemaphoreWorkerWaiting();
      ImagePreparation.request(second, 1, ImageDrawingBridge.COPY_READY, null);
      ImagePreparation.request(third, 1, ImageDrawingBridge.COPY_READY, null);

      assertEquals(1, ImagePreparation.decodeEntryCountForTest());
      assertTrue(ImagePreparation.activeEntryInFlightForTest());
      assertEquals(0, ImagePreparation.workerSemaphoreReleaseCountForTest());
      mainWindow.runNext();

      awaitQueuedAdoption(mainWindow);
      awaitSemaphoreWorkerWaiting();
      assertEquals(2, ImagePreparation.decodeEntryCountForTest());
      assertEquals(1, ImagePreparation.workerSemaphoreReleaseCountForTest());
      assertEquals(1, ImagePreparation.workerSemaphoreAcquireCountForTest());
      assertEquals(1, ImagePreparation.workerSemaphoreWakeCountForTest());
      assertEquals(0, ImagePreparation.workerSemaphoreOutstandingWakeCountForTest());
      mainWindow.runNext();

      awaitQueuedAdoption(mainWindow);
      awaitSemaphoreWorkerWaiting();
      assertEquals(3, ImagePreparation.decodeEntryCountForTest());
      assertEquals(2, ImagePreparation.workerSemaphoreReleaseCountForTest());
      assertEquals(2, ImagePreparation.workerSemaphoreAcquireCountForTest());
      assertEquals(2, ImagePreparation.workerSemaphoreWakeCountForTest());
      assertEquals(0, ImagePreparation.workerSemaphoreOutstandingWakeCountForTest());
      mainWindow.runNext();

      assertEquals(0, mainWindow.queuedCount());
      awaitSemaphoreWorkerWaiting();
      assertEquals(3, ImagePreparation.readyCountForTest());
      assertEquals(0, ImagePreparation.failedCountForTest());
      assertEquals(3, ImagePreparation.preparationEntryCountForTest());
      assertEquals(1, ImagePreparation.threadCreateCountForTest());
      assertEquals(1, ImagePreparation.threadStartCountForTest());
      assertEquals(0, ImagePreparation.workerPollCountForTest());
      assertEquals(0, ImagePreparation.workerSleepRequestedNsForTest());

      ImagePreparation.shutdownWorkerForTest();
      assertFalse(ImagePreparation.workerRunningForTest());
      assertEquals(3, ImagePreparation.workerSemaphoreReleaseCountForTest());
      assertEquals(3, ImagePreparation.workerSemaphoreAcquireCountForTest());
      assertEquals(2, ImagePreparation.workerSemaphoreWakeCountForTest());
      assertEquals(0, ImagePreparation.workerSemaphoreOutstandingWakeCountForTest());
      assertEquals(ImagePreparation.PREFETCH_THREAD_MODE_WORKER_SEMAPHORE,
          ImagePreparation.prefetchThreadModeForTest());
      assertDiagnosticMetricsNonNegative();
    } finally {
      while (mainWindow.queuedCount() > 0) {
        mainWindow.runNext();
      }
      ImagePreparation.setRunUiInlineForTest(false);
      ImagePreparation.resetThreadModeForTest();
      Image.setDiagnosticAccountingForTest(previousAccounting);
      MainWindow.resetPreviewState();
    }
  }

  @Test
  void workerModeFailureCanRetryWithoutCreatingAnotherThread() throws Exception {
    boolean previousAccounting = Image.diagnosticAccountingEnabledForTest();
    ImagePreparation.resetThreadModeForTest();
    ImagePreparation.configurePrefetchThreadModeForDiagnostic(
        ImagePreparation.PREFETCH_THREAD_MODE_WORKER, 1);
    ImagePreparation.setRunUiInlineForTest(true);
    Image.setDiagnosticAccountingForTest(true);
    ImagePreparation.resetAccountingForTest();
    try {
      Image image = new Image(jpeg(512, 384)).getSmoothScaledInstance(128, 96);
      Image.failNextTargetedDecodeInfrastructureForTest();
      CountDownLatch failed = new CountDownLatch(1);
      ImagePreparation.request(image, 1, ImageDrawingBridge.COPY_READY, failed::countDown);
      assertTrue(failed.await(5, TimeUnit.SECONDS));

      CountDownLatch retried = new CountDownLatch(1);
      ImagePreparation.request(image, 1, ImageDrawingBridge.COPY_READY, retried::countDown);
      assertTrue(retried.await(5, TimeUnit.SECONDS));
      awaitWorkerPoll();

      assertEquals(1, ImagePreparation.failedCountForTest());
      assertEquals(1, ImagePreparation.readyCountForTest());
      assertEquals(2, ImagePreparation.decodeEntryCountForTest());
      assertEquals(1, ImagePreparation.threadCreateCountForTest());
      assertEquals(1, ImagePreparation.threadStartCountForTest());
    } finally {
      ImagePreparation.setRunUiInlineForTest(false);
      ImagePreparation.resetThreadModeForTest();
      Image.setDiagnosticAccountingForTest(previousAccounting);
      MainWindow.resetPreviewState();
    }
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
      assertEquals(count, ImagePreparation.preparationEntryCountForTest());
      assertEquals(0, ImagePreparation.threadCreateCountForTest());
      assertEquals(0, ImagePreparation.threadStartCountForTest());
      assertEquals(0, ImagePreparation.decodeEntryCountForTest());
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
  void deferredCompletionSnapshotSeesFinalAccountingAfterCallbackBookkeeping() throws Exception {
    boolean previousAccounting = Image.diagnosticAccountingEnabledForTest();
    new Launcher();
    MainWindow.resetPreviewState();
    QueuedMainWindow mainWindow = new QueuedMainWindow();
    ImagePreparation.setRunUiInlineForTest(true);
    Image.setDiagnosticAccountingForTest(true);
    ImagePreparation.resetAccountingForTest();
    long[] callbackMetrics = {-1, -1, -1};
    long[] deferredMetrics = {-1, -1};
    long[] callbackWork = {0};
    try {
      Image image = alreadyDecodedImage();
      CountDownLatch completed = new CountDownLatch(1);
      ImagePreparation.request(image, 1, ImageDrawingBridge.COPY_READY, new Runnable() {
        @Override
        public void run() {
          callbackMetrics[0] = ImagePreparation.finishBookkeepingNsForTest();
          callbackMetrics[1] = ImagePreparation.preparationEntryTotalNsForTest();
          long callbackStartNs = System.nanoTime();
          long checksum = 0;
          for (int i = 0; i < 10000; i++) {
            checksum += i;
          }
          callbackWork[0] = checksum;
          callbackMetrics[2] = System.nanoTime() - callbackStartNs;
          mainWindow.runOnMainThread(new Runnable() {
            @Override
            public void run() {
              deferredMetrics[0] = ImagePreparation.finishBookkeepingNsForTest();
              deferredMetrics[1] = ImagePreparation.preparationEntryTotalNsForTest();
              completed.countDown();
            }
          }, false);
        }
      });

      assertEquals(1, mainWindow.queuedCount());
      mainWindow.runNext();
      assertEquals(1, mainWindow.queuedCount());
      assertEquals(0, callbackMetrics[0]);
      assertEquals(0, callbackMetrics[1]);
      mainWindow.runNext();

      assertTrue(completed.await(0, TimeUnit.MILLISECONDS));
      assertEquals(1, ImagePreparation.preparationEntryCountForTest());
      assertEquals(49995000, callbackWork[0]);
      assertTrue(callbackMetrics[2] > 0);
      assertTrue(deferredMetrics[0] >= callbackMetrics[2]);
      assertTrue(deferredMetrics[1] >= callbackMetrics[2]);
      assertTrue(deferredMetrics[0] > 0);
      assertTrue(deferredMetrics[1] > 0);
      assertEquals(ImagePreparation.finishBookkeepingNsForTest(), deferredMetrics[0]);
      assertEquals(ImagePreparation.preparationEntryTotalNsForTest(), deferredMetrics[1]);
      ImagePreparation.resetAccountingForTest();
      assertDiagnosticMetricsZero();
    } finally {
      while (mainWindow.queuedCount() > 0) {
        mainWindow.runNext();
      }
      ImagePreparation.setRunUiInlineForTest(false);
      Image.setDiagnosticAccountingForTest(previousAccounting);
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

  private static byte[] bmp(int width, int height) throws Exception {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    ImageIO.write(image, "bmp", bytes);
    return bytes.toByteArray();
  }

  private static byte[] pngWithFrameCount(int width, int height, int frameCount) throws Exception {
    byte[] encoded = png(width, height);
    int position = 8;
    while (position + 12 <= encoded.length) {
      int length = readInt(encoded, position);
      if (encoded[position + 4] == 'I' && encoded[position + 5] == 'D'
          && encoded[position + 6] == 'A' && encoded[position + 7] == 'T') {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        result.write(encoded, 0, position);
        writePngChunk(result, "tEXt", ("Comment\0FC=" + frameCount).getBytes(StandardCharsets.ISO_8859_1));
        result.write(encoded, position, encoded.length - position);
        return result.toByteArray();
      }
      position += length + 12;
    }
    throw new IllegalArgumentException("PNG fixture has no IDAT chunk");
  }

  private static byte[] corruptPngIdat(byte[] validPng) {
    byte[] corrupted = validPng.clone();
    int position = 8;
    while (position + 12 <= corrupted.length) {
      int length = readInt(corrupted, position);
      int data = position + 8;
      if (corrupted[position + 4] == 'I' && corrupted[position + 5] == 'D'
          && corrupted[position + 6] == 'A' && corrupted[position + 7] == 'T') {
        if (length < 2) {
          throw new IllegalArgumentException("PNG IDAT fixture is too short");
        }
        corrupted[data] = 0;
        corrupted[data + 1] = 0;
        CRC32 crc = new CRC32();
        crc.update(corrupted, position + 4, length + 4);
        writeInt(corrupted, data + length, (int) crc.getValue());
        return corrupted;
      }
      position += length + 12;
    }
    throw new IllegalArgumentException("PNG fixture has no IDAT chunk");
  }

  private static void writePngChunk(ByteArrayOutputStream output, String name, byte[] data) {
    byte[] type = name.getBytes(StandardCharsets.ISO_8859_1);
    writeInt(output, data.length);
    output.write(type, 0, type.length);
    output.write(data, 0, data.length);
    CRC32 crc = new CRC32();
    crc.update(type);
    crc.update(data);
    writeInt(output, (int) crc.getValue());
  }

  private static int readInt(byte[] bytes, int offset) {
    return ((bytes[offset] & 0xff) << 24) | ((bytes[offset + 1] & 0xff) << 16)
        | ((bytes[offset + 2] & 0xff) << 8) | (bytes[offset + 3] & 0xff);
  }

  private static void writeInt(ByteArrayOutputStream bytes, int value) {
    bytes.write((value >>> 24) & 0xff);
    bytes.write((value >>> 16) & 0xff);
    bytes.write((value >>> 8) & 0xff);
    bytes.write(value & 0xff);
  }

  private static void writeInt(byte[] bytes, int offset, int value) {
    bytes[offset] = (byte) (value >>> 24);
    bytes[offset + 1] = (byte) (value >>> 16);
    bytes[offset + 2] = (byte) (value >>> 8);
    bytes[offset + 3] = (byte) value;
  }

  private static Image alreadyDecodedImage() throws Exception {
    Image image = new Image(jpeg(64, 48)).getSmoothScaledInstance(16, 12);
    ImagePreparation.Request request = image.createPreparationRequest(1, false,
        ImageDrawingBridge.COPY_READY);
    ImagePreparationCandidate candidate = image.createPreparationCandidate(request);
    image.adoptPreparationCandidate(request, candidate);
    return image;
  }

  private static void assertDiagnosticMetricsNonNegative() {
    assertTrue(ImagePreparation.preparationEntryTotalNsForTest() >= 0);
    assertTrue(ImagePreparation.threadObjectCreateNsForTest() >= 0);
    assertTrue(ImagePreparation.threadStartCallNsForTest() >= 0);
    assertTrue(ImagePreparation.threadStartLatencyNsForTest() >= 0);
    assertTrue(ImagePreparation.decodeWorkerNsForTest() >= 0);
    assertTrue(ImagePreparation.uiDispatchWaitNsForTest() >= 0);
    assertTrue(ImagePreparation.adoptNsForTest() >= 0);
    assertTrue(ImagePreparation.finishPreparationNsForTest() >= 0);
    assertTrue(ImagePreparation.finishBookkeepingNsForTest() >= 0);
    assertTrue(ImagePreparation.workerPollCountForTest() >= 0);
    assertTrue(ImagePreparation.workerSleepRequestedNsForTest() >= 0);
    assertTrue(ImagePreparation.workerIdleElapsedNsForTest() >= 0);
    assertTrue(ImagePreparation.workerSemaphoreReleaseCountForTest() >= 0);
    assertTrue(ImagePreparation.workerSemaphoreAcquireCountForTest() >= 0);
    assertTrue(ImagePreparation.workerSemaphoreWakeCountForTest() >= 0);
  }

  private static Runnable blockingCompletion(final CountDownLatch entered,
      final CountDownLatch release, final CountDownLatch completed) {
    return new Runnable() {
      @Override
      public void run() {
        entered.countDown();
        try {
          release.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException interrupted) {
          Thread.currentThread().interrupt();
        } finally {
          completed.countDown();
        }
      }
    };
  }

  private static void assertWorkerSleepRejected(int sleepMs) {
    boolean rejected = false;
    try {
      ImagePreparation.configurePrefetchThreadModeForDiagnostic(
          ImagePreparation.PREFETCH_THREAD_MODE_WORKER, sleepMs);
    } catch (IllegalArgumentException expected) {
      rejected = true;
    }
    assertTrue(rejected);
  }

  private static void assertSemaphoreSleepRejected(int sleepMs) {
    boolean rejected = false;
    try {
      ImagePreparation.configurePrefetchThreadModeForDiagnostic(
          ImagePreparation.PREFETCH_THREAD_MODE_WORKER_SEMAPHORE, sleepMs);
    } catch (IllegalArgumentException expected) {
      rejected = true;
    }
    assertTrue(rejected);
  }

  private static void awaitQueuedAdoption(QueuedMainWindow mainWindow) {
    long deadlineNs = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
    while (mainWindow.queuedCount() == 0 && System.nanoTime() < deadlineNs) {
      Thread.yield();
    }
    assertEquals(1, mainWindow.queuedCount());
  }

  private static void awaitSemaphoreWorkerWaiting() {
    long deadlineNs = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
    while (!ImagePreparation.workerWaitingOnSemaphoreForTest()
        && System.nanoTime() < deadlineNs) {
      Thread.yield();
    }
    assertTrue(ImagePreparation.workerWaitingOnSemaphoreForTest());
  }

  private static void awaitWorkerPoll() {
    long deadlineNs = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
    while ((ImagePreparation.workerPollCountForTest() == 0
        || ImagePreparation.workerIdleElapsedNsForTest() == 0)
        && System.nanoTime() < deadlineNs) {
      Thread.yield();
    }
    assertTrue(ImagePreparation.workerPollCountForTest() > 0);
    assertTrue(ImagePreparation.workerIdleElapsedNsForTest() > 0);
  }

  private static void assertDiagnosticMetricsZero() {
    assertEquals(0, ImagePreparation.preparationEntryCountForTest());
    assertEquals(0, ImagePreparation.preparationEntryTotalNsForTest());
    assertEquals(0, ImagePreparation.threadCreateCountForTest());
    assertEquals(0, ImagePreparation.threadObjectCreateNsForTest());
    assertEquals(0, ImagePreparation.threadStartCountForTest());
    assertEquals(0, ImagePreparation.threadStartCallNsForTest());
    assertEquals(0, ImagePreparation.threadStartLatencyNsForTest());
    assertEquals(0, ImagePreparation.decodeEntryCountForTest());
    assertEquals(0, ImagePreparation.decodeWorkerNsForTest());
    assertEquals(0, ImagePreparation.uiDispatchCountForTest());
    assertEquals(0, ImagePreparation.uiDispatchWaitNsForTest());
    assertEquals(0, ImagePreparation.adoptNsForTest());
    assertEquals(0, ImagePreparation.finishPreparationNsForTest());
    assertEquals(0, ImagePreparation.finishBookkeepingNsForTest());
    assertEquals(0, ImagePreparation.workerPollCountForTest());
    assertEquals(0, ImagePreparation.workerSleepRequestedNsForTest());
    assertEquals(0, ImagePreparation.workerIdleElapsedNsForTest());
    assertEquals(0, ImagePreparation.workerSemaphoreReleaseCountForTest());
    assertEquals(0, ImagePreparation.workerSemaphoreAcquireCountForTest());
    assertEquals(0, ImagePreparation.workerSemaphoreWakeCountForTest());
    assertEquals(0, ImagePreparation.workerSemaphoreOutstandingWakeCountForTest());
  }

  private static final class QueuedMainWindow extends MainWindow {
    private final ArrayList<Runnable> queued = new ArrayList<Runnable>();

    @Override
    public synchronized void runOnMainThread(Runnable runnable, boolean singleInstance) {
      queued.add(runnable);
    }

    synchronized int queuedCount() {
      return queued.size();
    }

    void runNext() {
      Runnable next;
      synchronized (this) {
        assertFalse(queued.isEmpty());
        next = queued.remove(0);
      }
      next.run();
    }
  }
}
