// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import totalcross.sys.runtime.ImagePrefetchWorkerTestSupport;
import totalcross.sys.runtime.ImageRuntimeConfigurationStartup;
import totalcross.sys.runtime.ImageRuntimePolicy;
import totalcross.ui.MainWindow;

class ImageAsyncPreparationTest {
  @BeforeAll
  static void initializeRuntime() {
    new tc.simulator.Launcher();
    if (MainWindow.getMainWindow() == null) {
      new MainWindow();
    }
  }

  @BeforeEach
  void forceSemaphoreWorker() {
    ImagePrefetchWorkerTestSupport.useSemaphoreWorker();
  }

  @AfterEach
  void cleanupWorker() throws Exception {
    if (!ImagePreparationScheduler.idleForTest()) {
      awaitSchedulerIdle();
    }
    ImagePreparationSchedulerTestSupport.shutdownAndReset();
    ImagePrefetchWorkerTestSupport.restore();
  }

  @Test
  void staticPngCaptureUsesDenominatorOneAndRejectsMultiFrameAndUnsupportedFormats() throws Exception {
    double scale = MainWindow.getMainWindow().getGraphics().getContentScale();
    Image staticPng = new Image(png(128, 96));
    ImagePreparationRequest request = staticPng.captureDisplayPreparationRequest(scale, 20L);

    assertNotNull(request);
    assertEquals(ImageEncodedStructure.Format.PNG, request.source.getFormat());
    assertEquals(1, request.source.getFrameCount());
    assertEquals(1, request.decodeDenominator);

    Image multiFramePng = new Image(pngWithFrameCount(png(128, 96), 2));
    assertEquals(2, ((EncodedImageSource) multiFramePng.pipelineForSmoke().root()).getFrameCount());
    assertNull(multiFramePng.captureDisplayPreparationRequest(scale, 21L));
    assertNotNull(multiFramePng.resolveForDrawing(scale),
        "multi-frame PNG keeps its ordinary synchronous drawing path");

    Image gif = new Image(gif(48, 32));
    assertEquals(ImageEncodedStructure.Format.GIF, ((EncodedImageSource) gif.pipelineForSmoke().root()).getFormat());
    assertNull(gif.captureDisplayPreparationRequest(scale, 22L));
    assertNull(staticPng.captureDisplayPreparationRequest(Double.MAX_VALUE, 23L),
        "a destination scale that overflows requested dimensions is rejected");
  }

  @Test
  void requestIdentityKeepsSourcePipelineScaleAndBatchDistinct() throws Exception {
    Path path = writeJpeg(jpeg(96, 64));
    try {
      Image image = Image.getJpegScaled(path.toString(), 1, 2);
      double scale = MainWindow.getMainWindow().getGraphics().getContentScale();
      ImagePreparationRequest first = image.captureDisplayPreparationRequest(scale, 3L);
      ImagePreparationRequest laterBatch = image.captureDisplayPreparationRequest(scale, 9L);
      ImagePreparationRequest nextScale = image.captureDisplayPreparationRequest(Math.nextUp(scale), 3L);
      Image otherPipeline = image.getSmoothScaledInstance(24, 16);
      ImagePreparationRequest differentPipeline = otherPipeline.captureDisplayPreparationRequest(scale, 3L);
      Image samePath = Image.getJpegScaled(path.toString(), 1, 2);
      ImagePreparationRequest differentSource = samePath.captureDisplayPreparationRequest(scale, 3L);

      assertTrue(first.equivalentTo(laterBatch));
      assertFalse(first.equivalentTo(nextScale));
      assertFalse(first.equivalentTo(differentPipeline));
      assertFalse(first.equivalentTo(differentSource));
      assertFalse(first.equivalentTo(copyRequestWith(first, otherPipeline, first.pipeline,
          first.decodePolicy, first.sourceGeneration, first.targetBackingMutationGeneration,
          first.requestedWidth, first.requestedHeight, first.currentFrame, first.effectivePolicy,
          first.readiness, first.prototype, scale)));
      assertFalse(first.equivalentTo(copyRequestWith(first, first.target, first.pipeline,
          ImageDecodePolicy.bestFit(24, 16, 2, first.logicalWidth, first.logicalHeight),
          first.sourceGeneration, first.targetBackingMutationGeneration, first.requestedWidth,
          first.requestedHeight, first.currentFrame, first.effectivePolicy, first.readiness,
          first.prototype, scale)));
      assertFalse(first.equivalentTo(copyRequestWith(first, first.target, first.pipeline,
          first.decodePolicy, first.sourceGeneration, first.targetBackingMutationGeneration,
          first.requestedWidth + 1, first.requestedHeight, first.currentFrame, first.effectivePolicy,
          first.readiness, first.prototype, scale)));
      assertFalse(first.equivalentTo(copyRequestWith(first, first.target, first.pipeline,
          first.decodePolicy, first.sourceGeneration, first.targetBackingMutationGeneration,
          first.requestedWidth, first.requestedHeight, first.currentFrame + 1, first.effectivePolicy,
          first.readiness, first.prototype, scale)));
      assertFalse(first.equivalentTo(copyRequestWith(first, first.target, first.pipeline,
          first.decodePolicy, first.sourceGeneration + 1, first.targetBackingMutationGeneration,
          first.requestedWidth, first.requestedHeight, first.currentFrame, first.effectivePolicy,
          first.readiness, first.prototype, scale)));
      assertFalse(first.equivalentTo(copyRequestWith(first, first.target, first.pipeline,
          first.decodePolicy, first.sourceGeneration, first.targetBackingMutationGeneration + 1,
          first.requestedWidth, first.requestedHeight, first.currentFrame, first.effectivePolicy,
          first.readiness, first.prototype, scale)));
      assertFalse(first.equivalentTo(copyRequestWith(first, first.target, first.pipeline,
          first.decodePolicy, first.sourceGeneration, first.targetBackingMutationGeneration,
          first.requestedWidth, first.requestedHeight, first.currentFrame, differentPolicyForTest(),
          first.readiness, first.prototype, scale)));
      assertNotSame(first.source, differentSource.source);
      assertEquals(ImagePreparationRequest.Readiness.DRAW_READY, first.readiness);

      ImagePreparationRequest copyReady = copyRequest(first,
          ImagePreparationRequest.Readiness.COPY_READY, first.prototype);
      assertTrue(copyReady.equivalentTo(first));
      assertFalse(first.equivalentTo(copyReady));
    } finally {
      Files.deleteIfExists(path);
    }
  }

  @Test
  void detachedWorkerLeavesLiveImageUntouchedAndAdoptionFeedsTheNextDraw() throws Exception {
    Image image = lazyImage(jpeg(128, 96));
    double scale = MainWindow.getMainWindow().getGraphics().getContentScale();
    ImagePreparationRequest request = image.captureDisplayPreparationRequest(scale, 4L);
    ImagePipeline capturedPipeline = image.pipelineForSmoke();
    EncodedImageSource source = request.source;
    long generation = source.decodedGeneration();
    Image.resetImageOperationAccountingForTest();

    PreparedImageResult result = request.prototype.prepareDetachedForDisplay(request);

    assertEquals(PreparedImageResult.FailureKind.NONE, result.failureKind);
    assertSame(capturedPipeline, image.pipelineForSmoke());
    assertEquals(generation, source.decodedGeneration());
    assertNull(source.decodeFailure());
    assertEquals(ImagePreparationScheduler.TerminalState.READY,
        image.adoptPreparedForDisplay(request, result));
    assertEquals(1, capturedPipeline.cachedVariantCountForSmoke());
    Image ready = image.resolveForDrawing(scale);
    assertSame(result.variant, ready);
    assertEquals(1, Image.targetedDecodeInvocationCountForTest());
    result.releaseDetachedEncodedSource();
  }

  @Test
  void stalePipelineAndScaleAreRejectedButAnOldBatchCanStillAdopt() throws Exception {
    Image changedPipeline = lazyImage(jpeg(96, 64));
    double scale = MainWindow.getMainWindow().getGraphics().getContentScale();
    ImagePreparationRequest stalePipeline = changedPipeline.captureDisplayPreparationRequest(scale, 1L);
    changedPipeline.applyColor2(0xFF4080C0);
    assertEquals(ImagePreparationScheduler.TerminalState.STALE,
        changedPipeline.adoptPreparedForDisplay(stalePipeline, PreparedImageResult.transientFailure(null)));

    Image changedScale = lazyImage(jpeg(96, 64));
    ImagePreparationRequest captured = changedScale.captureDisplayPreparationRequest(scale, 2L);
    ImagePreparationRequest mismatchedScale = copyRequest(captured, captured.readiness,
        captured.prototype, Math.nextUp(scale));
    assertEquals(ImagePreparationScheduler.TerminalState.STALE,
        changedScale.adoptPreparedForDisplay(mismatchedScale, PreparedImageResult.transientFailure(null)));

    Image oldBatchTarget = lazyImage(jpeg(96, 64));
    ImagePreparationRequest oldBatch = oldBatchTarget.captureDisplayPreparationRequest(scale, 3L);
    // The batch generation is callback identity only, so an older batch can still prepare a valid Image.
    PreparedImageResult usefulResult = oldBatch.prototype.prepareDetachedForDisplay(oldBatch);
    assertEquals(ImagePreparationScheduler.TerminalState.READY,
        oldBatchTarget.adoptPreparedForDisplay(oldBatch, usefulResult));
    usefulResult.releaseDetachedEncodedSource();
  }

  @Test
  void targetBackingMutationAndSourceDecodeGenerationRemainIndependent() throws Exception {
    Image image = lazyImage(jpeg(96, 64));
    double scale = MainWindow.getMainWindow().getGraphics().getContentScale();
    ImagePreparationRequest request = image.captureDisplayPreparationRequest(scale, 13L);
    PreparedImageResult detached = request.prototype.prepareDetachedForDisplay(request);
    long sourceGeneration = request.source.decodedGeneration();
    assertEquals(0L, request.targetBackingMutationGeneration);

    image.recordGraphicsMutation();
    assertEquals(1L, image.backingMutationGenerationForP2());
    assertEquals(sourceGeneration, request.source.decodedGeneration());
    assertEquals(ImagePreparationScheduler.TerminalState.STALE,
        image.adoptPreparedForDisplay(request, detached));
    assertEquals(sourceGeneration, request.source.decodedGeneration());
    assertNull(request.source.decodedBackingForReuse(request.decodeDenominator));
    detached.releaseDetachedEncodedSource();
  }

  @Test
  void staleDeterministicFailureDoesNotPoisonTheLiveSource() throws Exception {
    Image image = lazyImage(corruptJpegEntropy(jpeg(64, 48)));
    double scale = MainWindow.getMainWindow().getGraphics().getContentScale();
    ImagePreparationRequest request = image.captureDisplayPreparationRequest(scale, 14L);
    PreparedImageResult failure = request.prototype.prepareDetachedForDisplay(request);
    assertEquals(PreparedImageResult.FailureKind.DETERMINISTIC, failure.failureKind);

    image.recordGraphicsMutation();
    assertEquals(ImagePreparationScheduler.TerminalState.STALE,
        image.adoptPreparedForDisplay(request, failure));
    assertNull(request.source.decodeFailure());
    failure.releaseDetachedEncodedSource();
  }

  @Test
  void newerCompatibleSynchronousDecodeWinsTheSourceGenerationRace() throws Exception {
    Image image = lazyImage(jpeg(128, 96));
    double scale = MainWindow.getMainWindow().getGraphics().getContentScale();
    ImagePreparationRequest request = image.captureDisplayPreparationRequest(scale, 5L);
    PreparedImageResult detached = request.prototype.prepareDetachedForDisplay(request);
    Image synchronous = image.resolveForDrawing(scale);
    ImageBacking synchronousBacking = request.source.decodedBackingForReuse(request.decodeDenominator);
    long winningGeneration = request.source.decodedGeneration();

    assertEquals(ImagePreparationScheduler.TerminalState.READY,
        image.adoptPreparedForDisplay(request, detached));
    assertEquals(winningGeneration, request.source.decodedGeneration());
    assertSame(synchronousBacking, request.source.decodedBackingForReuse(request.decodeDenominator));
    assertNotNull(synchronous);
    Image adopted = image.resolveForDrawing(scale);
    assertSame(detached.variant, adopted);
    assertSame(adopted, image.resolveForDrawing(scale));
    detached.releaseDetachedEncodedSource();
  }

  @Test
  void transientFailureRetriesAndDeterministicFailureIsCachedAtAdoption() throws Exception {
    Image retry = lazyImage(jpeg(96, 64));
    double scale = MainWindow.getMainWindow().getGraphics().getContentScale();
    ImagePreparationRequest firstAttempt = retry.captureDisplayPreparationRequest(scale, 6L);
    Image.failNextTargetedDecodeInfrastructureForTest();
    PreparedImageResult temporary = firstAttempt.prototype.prepareDetachedForDisplay(firstAttempt);
    assertEquals(PreparedImageResult.FailureKind.TRANSIENT, temporary.failureKind);
    assertNull(firstAttempt.source.decodeFailure());
    assertEquals(ImagePreparationScheduler.TerminalState.TRANSIENT_FAILURE,
        retry.adoptPreparedForDisplay(firstAttempt, temporary));

    ImagePreparationRequest secondAttempt = retry.captureDisplayPreparationRequest(scale, 7L);
    PreparedImageResult successful = secondAttempt.prototype.prepareDetachedForDisplay(secondAttempt);
    assertEquals(PreparedImageResult.FailureKind.NONE, successful.failureKind);
    assertEquals(ImagePreparationScheduler.TerminalState.READY,
        retry.adoptPreparedForDisplay(secondAttempt, successful));
    successful.releaseDetachedEncodedSource();

    Image corrupt = lazyImage(corruptJpegEntropy(jpeg(64, 48)));
    ImagePreparationRequest corruptRequest = corrupt.captureDisplayPreparationRequest(scale, 8L);
    PreparedImageResult invalid = corruptRequest.prototype.prepareDetachedForDisplay(corruptRequest);
    assertEquals(PreparedImageResult.FailureKind.DETERMINISTIC, invalid.failureKind);
    assertEquals(ImagePreparationScheduler.TerminalState.DETERMINISTIC_FAILURE,
        corrupt.adoptPreparedForDisplay(corruptRequest, invalid));
    assertSame(invalid.failure, corruptRequest.source.decodeFailure());
    final int[] settled = {0};
    ImagePreparationScheduler.submit(corruptRequest, new Runnable() {
      @Override
      public void run() {
        settled[0]++;
      }
    });
    assertEquals(1, settled[0]);
    assertTrue(ImagePreparationScheduler.idleForTest());
    assertNull(ImagePreparationScheduler.processWorkerForTest());
    invalid.releaseDetachedEncodedSource();
  }

  @Test
  void decodedAllocationFailureIsTransientAndCanRetry() throws Exception {
    awaitSchedulerIdle();
    Image image = lazyImage(jpeg(96, 64));
    double scale = MainWindow.getMainWindow().getGraphics().getContentScale();
    ImagePreparationRequest first = image.captureDisplayPreparationRequest(scale, 15L);
    assertTrue(first.decodeDenominator > 1, "the allocation hook exercises targeted JPEG raster creation");
    final int[] completions = {0};
    Image.failNextDecodedRasterAllocationForTest();
    ImagePreparationScheduler.submit(first, new Runnable() {
      @Override
      public void run() { completions[0]++; }
    });
    pumpUntil(new CompletionCheck() {
      @Override
      public boolean isComplete() {
        return completions[0] == 1 && ImagePreparationScheduler.idleForTest();
      }
    });
    assertNull(first.source.decodeFailure());
    assertNull(first.source.decodedBackingForReuse(first.decodeDenominator));

    ImagePreparationRequest retry = image.captureDisplayPreparationRequest(scale, 16L);
    ImagePreparationScheduler.submit(retry, new Runnable() {
      @Override
      public void run() { completions[0]++; }
    });
    pumpUntil(new CompletionCheck() {
      @Override
      public boolean isComplete() {
        return completions[0] == 2 && ImagePreparationScheduler.idleForTest();
      }
    });
    assertTrue(image.isDisplayPreparationReady(retry));
  }

  private static Image lazyImage(byte[] encoded) throws Exception {
    Path path = writeJpeg(encoded);
    try {
      return Image.getJpegScaled(path.toString(), 1, 2);
    } finally {
      Files.deleteIfExists(path);
    }
  }

  private static Path writeJpeg(byte[] encoded) throws Exception {
    Path path = Files.createTempFile("tc-async-image", ".jpg");
    Files.write(path, encoded);
    return path;
  }

  private static byte[] jpeg(int width, int height) throws Exception {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int red = (x * 29 + y * 17) & 0xFF;
        image.setRGB(x, y, (red << 16) | (((red * 3) & 0xFF) << 8) | ((red * 7) & 0xFF));
      }
    }
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
    ImageWriter writer = writers.next();
    try (ImageOutputStream output = ImageIO.createImageOutputStream(bytes)) {
      writer.setOutput(output);
      writer.write(null, new IIOImage(image, null, null), writer.getDefaultWriteParam());
    } finally {
      writer.dispose();
    }
    return bytes.toByteArray();
  }

  private static byte[] png(int width, int height) throws Exception {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    image.setRGB(0, 0, 0xFF336699);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    assertTrue(ImageIO.write(image, "png", bytes));
    return bytes.toByteArray();
  }

  private static byte[] pngWithFrameCount(byte[] encoded, int frameCount) {
    byte[] text = ("Comment\u0000FC=" + frameCount).getBytes(StandardCharsets.ISO_8859_1);
    byte[] chunk = new byte[text.length + 12];
    writeInt(chunk, 0, text.length);
    chunk[4] = 't';
    chunk[5] = 'E';
    chunk[6] = 'X';
    chunk[7] = 't';
    System.arraycopy(text, 0, chunk, 8, text.length);
    CRC32 crc = new CRC32();
    crc.update(chunk, 4, text.length + 4);
    writeInt(chunk, 8 + text.length, (int) crc.getValue());

    int afterHeader = 33;
    byte[] result = new byte[encoded.length + chunk.length];
    System.arraycopy(encoded, 0, result, 0, afterHeader);
    System.arraycopy(chunk, 0, result, afterHeader, chunk.length);
    System.arraycopy(encoded, afterHeader, result, afterHeader + chunk.length, encoded.length - afterHeader);
    return result;
  }

  private static void writeInt(byte[] target, int offset, int value) {
    target[offset] = (byte) (value >>> 24);
    target[offset + 1] = (byte) (value >>> 16);
    target[offset + 2] = (byte) (value >>> 8);
    target[offset + 3] = (byte) value;
  }

  private static byte[] gif(int width, int height) throws Exception {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    assertTrue(ImageIO.write(image, "gif", bytes));
    return bytes.toByteArray();
  }

  private static byte[] corruptJpegEntropy(byte[] source) {
    int sos = -1;
    for (int i = 0; i + 1 < source.length; i++) {
      if ((source[i] & 0xFF) == 0xFF && (source[i + 1] & 0xFF) == 0xDA) {
        sos = i;
        break;
      }
    }
    int segmentLength = ((source[sos + 2] & 0xFF) << 8) | (source[sos + 3] & 0xFF);
    int entropy = sos + 2 + segmentLength;
    byte[] invalidTail = new byte[] {
        (byte) 0xFF, (byte) 0xC3, 0, 8, 8, 0, 1, 0, 1, 1, (byte) 0xFF, (byte) 0xD9
    };
    byte[] result = new byte[entropy + invalidTail.length];
    System.arraycopy(source, 0, result, 0, entropy);
    System.arraycopy(invalidTail, 0, result, entropy, invalidTail.length);
    return result;
  }

  private static ImagePreparationRequest copyRequest(ImagePreparationRequest request,
      ImagePreparationRequest.Readiness readiness, Image prototype) {
    return copyRequest(request, readiness, prototype, request.destinationScale());
  }

  private static ImagePreparationRequest copyRequest(ImagePreparationRequest request,
      ImagePreparationRequest.Readiness readiness, Image prototype, double destinationScale) {
    return copyRequestWith(request, request.target, request.pipeline, request.decodePolicy,
        request.sourceGeneration, request.targetBackingMutationGeneration, request.requestedWidth,
        request.requestedHeight, request.currentFrame, request.effectivePolicy, readiness,
        prototype, destinationScale);
  }

  private static ImagePreparationRequest copyRequestWith(ImagePreparationRequest request, Image target,
      ImagePipeline pipeline, ImageDecodePolicy decodePolicy, long sourceGeneration,
      long targetMutationGeneration, int requestedWidth, int requestedHeight, int currentFrame,
      ImageRuntimePolicy effectivePolicy, ImagePreparationRequest.Readiness readiness,
      Image prototype, double destinationScale) {
    return new ImagePreparationRequest(target, request.source, pipeline, decodePolicy,
        Double.doubleToLongBits(destinationScale), destinationScale, requestedWidth, requestedHeight,
        request.decodeDenominator, sourceGeneration, targetMutationGeneration, effectivePolicy,
        currentFrame, request.imageWidth, request.imageHeight, request.logicalWidth, request.logicalHeight,
        readiness, request.batchGeneration, prototype);
  }

  private static ImageRuntimePolicy differentPolicyForTest() throws Exception {
    java.lang.reflect.Field policyField = ImageRuntimeConfigurationStartup.class.getDeclaredField("currentPolicy");
    policyField.setAccessible(true);
    ImageRuntimePolicy original = ImageRuntimeConfigurationStartup.currentPolicy();
    java.lang.reflect.Method setter = ImageRuntimeConfigurationStartup.class.getDeclaredMethod(
        "setRasterFeaturesForTest", boolean.class, boolean.class, boolean.class);
    setter.setAccessible(true);
    try {
      setter.invoke(null, true, false, false);
      return ImageRuntimeConfigurationStartup.currentPolicy();
    } finally {
      policyField.set(null, original);
    }
  }

  private static void awaitSchedulerIdle() throws Exception {
    pumpUntil(new CompletionCheck() {
      @Override
      public boolean isComplete() {
        return ImagePreparationScheduler.idleForTest();
      }
    });
  }

  private static void pumpUntil(CompletionCheck condition) throws Exception {
    pumpUntil(condition, 8);
  }

  private static void pumpUntil(CompletionCheck condition, int timeoutSeconds) throws Exception {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(timeoutSeconds);
    MainWindow mainWindow = MainWindow.getMainWindow();
    while (!condition.isComplete() && System.nanoTime() < deadline) {
      mainWindow._onTimerTick(false);
      Thread.sleep(4);
    }
    assertTrue(condition.isComplete(), "Timed out waiting for image preparation callbacks");
  }

  private interface CompletionCheck {
    boolean isComplete();
  }

}
