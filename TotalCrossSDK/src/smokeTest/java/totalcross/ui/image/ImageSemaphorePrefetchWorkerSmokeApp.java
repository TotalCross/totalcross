// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import java.util.ArrayList;

import totalcross.io.ByteArrayStream;
import totalcross.io.File;
import totalcross.sys.Settings;
import totalcross.sys.runtime.ImagePrefetchWorkerSmokeTestSupport;
import totalcross.ui.ImageControl;
import totalcross.ui.MainWindow;
import totalcross.ui.ScrollContainer;
import totalcross.ui.gfx.Graphics;

/** Deployed macOS smoke for serialized Semaphore-driven image preparation. */
public class ImageSemaphorePrefetchWorkerSmokeApp extends MainWindow {
  private static final String CAPTURED_PATH = "p9-captured-source.jpg";
  private static final String STALE_PATH = "p9-stale-source.jpg";
  private static final String RETRY_PATH = "p9-retry-source.jpg";

  private final ArrayList<Integer> callbackOrder = new ArrayList<Integer>();
  private Image first;
  private Image second;
  private Image third;
  private ScrollContainer thirdScroll;
  private ImagePreparationRequest firstRequest;
  private ImagePreparationRequest secondRequest;
  private ImagePreparationRequest thirdRequest;
  private Thread processWorker;
  private double scale;
  private int maxActive;
  private boolean semaphorePolicy;
  private boolean multipleJpegRequests;
  private boolean oneSerializedWorker;
  private boolean uiCallbacks = true;
  private boolean activeAtMostOne = true;
  private boolean fifoAdoption = true;
  private boolean deduplicated;
  private boolean drawReuse;
  private boolean capturedSource;
  private boolean staleResult;
  private boolean transientRetry;
  private boolean idleWorker;
  private boolean cleanShutdown;
  private boolean finished;
  private String error = "";

  @Override
  public void initUI() {
    try {
      Settings.fingerTouch = false;
      ImagePrefetchWorkerSmokeTestSupport.useSemaphoreWorker();
      scale = getGraphics().getContentScale();
      byte[] encoded = jpeg(0xFF2878C0);
      first = capturedImage(CAPTURED_PATH, encoded);
      second = capturedImage("p9-second-source.jpg", encoded);
      third = capturedImage("p9-third-source.jpg", encoded);
      ScrollContainer firstScroll = visibleImage(first);
      ScrollContainer duplicateScroll = visibleImage(first);
      ScrollContainer secondScroll = visibleImage(second);
      thirdScroll = visibleImage(third);
      capturedSource = !new File(CAPTURED_PATH, File.DONT_OPEN).exists();
      firstRequest = first.captureDisplayPreparationRequest(scale, 1L);
      secondRequest = second.captureDisplayPreparationRequest(scale, 2L);
      thirdRequest = third.captureDisplayPreparationRequest(scale, 3L);
      semaphorePolicy = firstRequest.effectivePolicy.prefetchWorker()
          == ImagePrefetchWorkerMode.SEMAPHORE_PROCESS_WORKER;
      Image.resetImageOperationAccountingForTest();

      firstScroll.prepareForDisplay(new Runnable() {
        @Override
        public void run() {
          firstCallback();
        }
      });
      duplicateScroll.prepareForDisplay(new Runnable() {
        @Override
        public void run() {
          duplicateCallback();
        }
      });
      secondScroll.prepareForDisplay(new Runnable() {
        @Override
        public void run() {
          secondCallback();
        }
      });
      processWorker = ImagePreparationScheduler.processWorkerForTest();
      oneSerializedWorker = processWorker != null
          && ImagePreparationScheduler.processWorkerStartCountForTest() == 1
          && ImagePreparationScheduler.semaphoreWakeCountForTest() == 1;
    } catch (Throwable failure) {
      finish(false, failure);
    }
  }

  private void firstCallback() {
    observeCallback();
    deduplicated = ImagePreparationScheduler.semaphoreWakeCountForTest() == 1;
    fifoAdoption &= ImagePreparationScheduler.preparationStartCountForTest() == 1
        && first.isDisplayPreparationReady(firstRequest);
    callbackOrder.add(1);
    thirdScroll.prepareForDisplay(new Runnable() {
      @Override
      public void run() {
        thirdCallback();
      }
    });
  }

  private void duplicateCallback() {
    observeCallback();
    fifoAdoption &= ImagePreparationScheduler.preparationStartCountForTest() == 1;
    callbackOrder.add(11);
  }

  private void secondCallback() {
    observeCallback();
    fifoAdoption &= ImagePreparationScheduler.preparationStartCountForTest() == 2
        && second.isDisplayPreparationReady(secondRequest);
    callbackOrder.add(2);
  }

  private void thirdCallback() {
    observeCallback();
    fifoAdoption &= ImagePreparationScheduler.preparationStartCountForTest() == 3
        && third.isDisplayPreparationReady(thirdRequest);
    callbackOrder.add(3);
    fifoAdoption &= callbackOrder.size() == 4
        && callbackOrder.get(0) == 1 && callbackOrder.get(1) == 11
        && callbackOrder.get(2) == 2 && callbackOrder.get(3) == 3;
    reusePreparedImagesThenCheckStale();
  }

  private void reusePreparedImagesThenCheckStale() {
    int decodesBeforeDraw = Image.targetedDecodeInvocationCountForTest();
    try {
      first.resolveForDrawing(scale);
      second.resolveForDrawing(scale);
      third.resolveForDrawing(scale);
      drawReuse = Image.targetedDecodeInvocationCountForTest() == decodesBeforeDraw;
    } catch (Throwable failure) {
      finish(false, failure);
      return;
    }
    staleRequestCase();
  }

  private void staleRequestCase() {
    try {
      final Image stale = capturedImage(STALE_PATH, jpeg(0xFF804030));
      final ImagePreparationRequest staleRequest = stale.captureDisplayPreparationRequest(scale, 4L);
      final EncodedImageSource source = staleRequest.source;
      ScrollContainer staleScroll = visibleImage(stale);
      staleScroll.prepareForDisplay(new Runnable() {
        @Override
        public void run() {
          observeCallback();
          staleResult = stale.pipelineForSmoke() != staleRequest.pipeline
              && source.decodeFailure() == null
              && source.decodedBackingForReuse(staleRequest.decodeDenominator) == null;
          transientRetryCase();
        }
      });
      stale.applyColor2(0xFF4070A0);
    } catch (Throwable failure) {
      finish(false, failure);
    }
  }

  private void transientRetryCase() {
    try {
      final Image retry = capturedImage(RETRY_PATH, jpeg(0xFFC06030));
      final EncodedImageSource source = (EncodedImageSource) retry.pipelineForSmoke().root();
      final ScrollContainer retryScroll = visibleImage(retry);
      final ImagePreparationRequest firstAttempt = retry.captureDisplayPreparationRequest(scale, 5L);
      Image.failNextTargetedDecodeInfrastructureForTest();
      retryScroll.prepareForDisplay(new Runnable() {
        @Override
        public void run() {
          observeCallback();
          final boolean transientFailure = source.decodeFailure() == null
              && !retry.isDisplayPreparationReady(firstAttempt);
          final ImagePreparationRequest retryRequest = retry.captureDisplayPreparationRequest(scale, 6L);
          retryScroll.prepareForDisplay(new Runnable() {
            @Override
            public void run() {
              observeCallback();
              transientRetry = transientFailure && source.decodeFailure() == null
                  && retry.isDisplayPreparationReady(retryRequest);
              finish(true, null);
            }
          });
        }
      });
    } catch (Throwable failure) {
      finish(false, failure);
    }
  }

  private void observeCallback() {
    uiCallbacks &= MainWindow.isMainThread();
    int active = ImagePreparationScheduler.activeCountForTest();
    maxActive = Math.max(maxActive, active);
    activeAtMostOne &= active <= 1;
    oneSerializedWorker &= ImagePreparationScheduler.processWorkerForTest() == processWorker;
  }

  private Image capturedImage(String path, byte[] encoded) throws Exception {
    write(path, encoded);
    Image image = Image.getJpegScaled(path, 1, 4);
    delete(path);
    return image;
  }

  private ScrollContainer visibleImage(Image image) {
    ScrollContainer scroll = new ScrollContainer(false, false);
    scroll.setRect(0, 0, 220, 160);
    ImageControl control = new ImageControl(image);
    scroll.add(control);
    control.setRect(4, 4, Math.max(24, image.getWidth()), Math.max(20, image.getHeight()));
    return scroll;
  }

  private static byte[] jpeg(int color) throws Exception {
    Image image = new Image(64, 32);
    Graphics graphics = image.getGraphics();
    graphics.foreColor = color;
    graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
    ByteArrayStream output = new ByteArrayStream(8192);
    image.createJpg(output, 90);
    byte[] encoded = new byte[output.getPos()];
    System.arraycopy(output.getBuffer(), 0, encoded, 0, encoded.length);
    return encoded;
  }

  private static void write(String path, byte[] bytes) throws Exception {
    File file = new File(path, File.CREATE_EMPTY);
    file.writeBytes(bytes, 0, bytes.length);
    file.close();
  }

  private static void delete(String path) throws Exception {
    File file = new File(path, File.DONT_OPEN);
    file.delete();
  }

  private void finish(boolean passed, Throwable failure) {
    if (finished) {
      return;
    }
    finished = true;
    if (failure != null) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }
    multipleJpegRequests = ImagePreparationScheduler.preparationStartCountForTest() == 6
        && ImagePreparationScheduler.semaphoreWakeCountForTest() == 6;
    Thread activeWorker = ImagePreparationScheduler.processWorkerForTest();
    oneSerializedWorker &= activeWorker != null && activeWorker == processWorker
        && ImagePreparationScheduler.processWorkerStartCountForTest() == 1;
    if (activeWorker != null && ImagePreparationScheduler.idleForTest()) {
      ImagePreparationScheduler.awaitWorkerWaitingForSignalForTest();
    }
    idleWorker = ImagePreparationScheduler.idleForTest()
        && "RUNNING_OR_BLOCKED".equals(ImagePreparationScheduler.workerLifecycleForTest())
        && ImagePreparationScheduler.workerWaitingForSignalForTest();
    boolean overall = passed && semaphorePolicy && multipleJpegRequests && oneSerializedWorker
        && uiCallbacks && activeAtMostOne && maxActive <= 1 && fifoAdoption && deduplicated
        && drawReuse && capturedSource && staleResult && transientRetry && idleWorker;
    try {
      if (ImagePreparationScheduler.idleForTest()) {
        ImagePreparationScheduler.shutdownSemaphoreWorkerForTest();
        cleanShutdown = "SHUTDOWN".equals(ImagePreparationScheduler.workerLifecycleForTest());
      }
      ImagePrefetchWorkerSmokeTestSupport.restore();
    } catch (Throwable shutdownFailure) {
      overall = false;
      error = shutdownFailure.getClass().getName() + ":"
          + String.valueOf(shutdownFailure.getMessage()).replace(' ', '_');
    }
    overall &= cleanShutdown;
    System.out.println("fixture=ImageSemaphorePrefetchWorkerSmokeApp,semaphoreWorker=" + semaphorePolicy
        + ",multipleJpegRequests=" + multipleJpegRequests + ",oneSerializedWorker=" + oneSerializedWorker
        + ",uiCallbacks=" + uiCallbacks + ",activeAtMostOne=" + activeAtMostOne
        + ",fifoAdoption=" + fifoAdoption + ",deduplicated=" + deduplicated
        + ",drawReuse=" + drawReuse + ",capturedSource=" + capturedSource
        + ",staleResult=" + staleResult + ",transientRetry=" + transientRetry
        + ",idleWorker=" + idleWorker + ",cleanShutdown=" + cleanShutdown
        + ",overallPass=" + overall + (error.length() == 0 ? "" : ",error=" + error));
    System.out.flush();
    exit(overall ? 0 : 1);
  }
}
