// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.sys.runtime.ImagePrefetchWorkerSmokeTestSupport;
import totalcross.ui.ImageControl;
import totalcross.ui.MainWindow;
import totalcross.ui.ScrollContainer;

/** Deployed macOS smoke for static PNG preparation and detached-candidate cleanup. */
public class ImagePngAsyncPreparationSmokeApp extends MainWindow {
  private Image color;
  private Image indexed;
  private Image stale;
  private ImagePreparationRequest colorRequest;
  private ImagePreparationRequest indexedRequest;
  private ImagePreparationRequest staleRequest;
  private Thread processWorker;
  private double scale;
  private long staleLiveBefore;
  private long staleLiveAfter;
  private long staleCreatedBefore;
  private long staleReleasedBefore;
  private long staleCandidatesCreated;
  private long staleCandidateReleaseCount;
  private boolean semaphoreWorker;
  private boolean legacyWorker;
  private boolean staticPngDenominator;
  private boolean colorBackingReady;
  private boolean indexedBackingReady;
  private boolean asyncSubmission = true;
  private boolean uiCallbacks = true;
  private boolean singleWorker;
  private boolean activeAtMostOne = true;
  private int maxActive;
  private boolean fullDecodeOnly;
  private boolean drawReuse;
  private boolean staleCandidatesReleased;
  private boolean idleWorker;
  private boolean cleanShutdown;
  private boolean finished;
  private String error = "";

  @Override
  public void initUI() {
    try {
      Settings.fingerTouch = false;
      if (forceSemaphoreWorker()) {
        ImagePrefetchWorkerSmokeTestSupport.useSemaphoreWorker();
      }
      scale = getGraphics().getContentScale();

      color = load("image-compact/opaque-color.png").getSmoothScaledInstance(5, 4);
      colorRequest = color.captureDisplayPreparationRequest(scale, 1L);
      staticPngDenominator = isStaticPng(colorRequest) && colorRequest.decodeDenominator == 1;
      Image.resetImageOperationAccountingForTest();
      submitForDisplay(color, new Runnable() {
        @Override
        public void run() {
          colorCallback();
        }
      });
      processWorker = ImagePreparationScheduler.processWorkerForTest();
      semaphoreWorker = colorRequest != null && colorRequest.effectivePolicy.prefetchWorker()
          == ImagePrefetchWorkerMode.SEMAPHORE_PROCESS_WORKER;
      legacyWorker = colorRequest != null && colorRequest.effectivePolicy.prefetchWorker()
          == ImagePrefetchWorkerMode.LEGACY_PER_ENTRY_THREAD;
      if (forceSemaphoreWorker()) {
        singleWorker = processWorker != null
            && ImagePreparationScheduler.processWorkerStartCountForTest() == 1
            && ImagePreparationScheduler.semaphoreWakeCountForTest() == 1;
      } else {
        legacyWorker &= processWorker == null
            && ImagePreparationScheduler.processWorkerStartCountForTest() == 0
            && ImagePreparationScheduler.semaphoreWakeCountForTest() == 0;
      }
    } catch (Throwable failure) {
      finish(false, failure);
    }
  }

  private void colorCallback() {
    observeCallback();
    try {
      ImageBacking sourceBacking = colorRequest.source.decodedBackingForReuse(1);
      Image variant = color.resolveForDrawing(scale);
      NativeImageBacking variantBacking = nativeBacking(variant);
      colorBackingReady = color.isDisplayPreparationReady(colorRequest)
          && sourceBacking instanceof NativeImageBacking && sourceBacking.isValid()
          && sourceBacking.width() == 8 && sourceBacking.height() == 6
          && variant.getWidth() == 5 && variant.getHeight() == 4
          && variantBacking.isValid();
      int fullBeforeDrawCheck = Image.fullDecodeInvocationCountForTest();
      int targetedBeforeDrawCheck = Image.targetedDecodeInvocationCountForTest();
      drawReuse = color.resolveForDrawing(scale) == variant
          && Image.fullDecodeInvocationCountForTest() == fullBeforeDrawCheck
          && Image.targetedDecodeInvocationCountForTest() == targetedBeforeDrawCheck
          && fullBeforeDrawCheck == 1 && targetedBeforeDrawCheck == 0;
      if (!colorBackingReady || !drawReuse) {
        throw new IllegalStateException("true-color PNG backing or draw reuse mismatch");
      }
      prepareIndexedPng();
    } catch (Throwable failure) {
      finish(false, failure);
    }
  }

  private void prepareIndexedPng() throws Exception {
    indexed = load("image-png/static-indexed.png");
    indexedRequest = indexed.captureDisplayPreparationRequest(scale, 2L);
    staticPngDenominator &= isStaticPng(indexedRequest) && indexedRequest.decodeDenominator == 1;
    if (!staticPngDenominator) {
      throw new IllegalStateException("static PNG was not captured at denominator 1");
    }
    submitForDisplay(indexed, new Runnable() {
      @Override
      public void run() {
        indexedCallback();
      }
    });
  }

  private void indexedCallback() {
    observeCallback();
    try {
      ImageBacking sourceBacking = indexedRequest.source.decodedBackingForReuse(1);
      int fullBeforeDraw = Image.fullDecodeInvocationCountForTest();
      int targetedBeforeDraw = Image.targetedDecodeInvocationCountForTest();
      Image resolved = indexed.resolveForDrawing(scale);
      indexedBackingReady = indexed.isDisplayPreparationReady(indexedRequest)
          && sourceBacking != null && sourceBacking.isValid()
          && sourceBacking.width() == 4 && sourceBacking.height() == 3
          && resolved != null
          && Image.fullDecodeInvocationCountForTest() == fullBeforeDraw
          && Image.targetedDecodeInvocationCountForTest() == targetedBeforeDraw;
      fullDecodeOnly = Image.fullDecodeInvocationCountForTest() == 2
          && Image.targetedDecodeInvocationCountForTest() == 0;
      if (!indexedBackingReady || !fullDecodeOnly) {
        throw new IllegalStateException("indexed PNG decode or accounting mismatch");
      }
      prepareStalePng();
    } catch (Throwable failure) {
      finish(false, failure);
    }
  }

  private void prepareStalePng() throws Exception {
    stale = load("image-compact/opaque-color.png").getSmoothScaledInstance(3, 2);
    staleRequest = stale.captureDisplayPreparationRequest(scale, 3L);
    if (!isStaticPng(staleRequest) || staleRequest.decodeDenominator != 1) {
      throw new IllegalStateException("stale PNG request was not captured");
    }
    staleLiveBefore = NativeImageBacking.backingRecordsLiveForTest();
    staleCreatedBefore = NativeImageBacking.backingRecordsCreatedForTest();
    staleReleasedBefore = NativeImageBacking.backingRecordsReleasedForTest();
    submitForDisplay(stale, new Runnable() {
      @Override
      public void run() {
        staleCallback();
      }
    });
    stale.applyColor2(0xFF4070A0);
  }

  private void staleCallback() {
    observeCallback();
    try {
      staleCandidatesCreated = NativeImageBacking.backingRecordsCreatedForTest() - staleCreatedBefore;
      staleCandidateReleaseCount = NativeImageBacking.backingRecordsReleasedForTest() - staleReleasedBefore;
      staleLiveAfter = NativeImageBacking.backingRecordsLiveForTest();
      staleCandidatesReleased = !stale.isDisplayPreparationReady(staleRequest)
          && staleRequest.source.decodedBackingForReuse(1) == null
          && staleCandidatesCreated >= 2 && staleCandidateReleaseCount == staleCandidatesCreated
          && staleLiveAfter == staleLiveBefore;
      fullDecodeOnly &= Image.fullDecodeInvocationCountForTest() == 3
          && Image.targetedDecodeInvocationCountForTest() == 0;
      if (!staleCandidatesReleased || !fullDecodeOnly) {
        throw new IllegalStateException("stale PNG candidates were retained or decode accounting changed");
      }
      finish(true, null);
    } catch (Throwable failure) {
      finish(false, failure);
    }
  }

  private void observeCallback() {
    uiCallbacks &= MainWindow.isMainThread();
    int active = ImagePreparationScheduler.activeCountForTest();
    maxActive = Math.max(maxActive, active);
    activeAtMostOne &= active <= 1;
    if (forceSemaphoreWorker()) {
      singleWorker &= ImagePreparationScheduler.processWorkerForTest() == processWorker
          && ImagePreparationScheduler.processWorkerStartCountForTest() == 1;
    } else {
      legacyWorker &= ImagePreparationScheduler.processWorkerForTest() == null
          && ImagePreparationScheduler.processWorkerStartCountForTest() == 0
          && ImagePreparationScheduler.semaphoreWakeCountForTest() == 0;
    }
  }

  private void submitForDisplay(Image image, final Runnable callback) {
    final boolean[] returned = {false};
    visibleImage(image).prepareForDisplay(new Runnable() {
      @Override
      public void run() {
        asyncSubmission &= returned[0];
        callback.run();
      }
    });
    returned[0] = true;
  }

  protected boolean forceSemaphoreWorker() {
    return true;
  }

  private static boolean isStaticPng(ImagePreparationRequest request) {
    return request != null && request.source.getFormat() == ImageEncodedStructure.Format.PNG
        && request.source.getFrameCount() == 1;
  }

  private static Image load(String path) throws Exception {
    byte[] bytes = Vm.getFile(path);
    if (bytes == null || bytes.length == 0) {
      throw new IllegalStateException("missing PNG fixture " + path);
    }
    return new Image(bytes, bytes.length);
  }

  private ScrollContainer visibleImage(Image image) {
    ScrollContainer scroll = new ScrollContainer(false, false);
    scroll.setRect(0, 0, 220, 160);
    ImageControl control = new ImageControl(image);
    scroll.add(control);
    control.setRect(4, 4, Math.max(24, image.getWidth()), Math.max(20, image.getHeight()));
    return scroll;
  }

  private static NativeImageBacking nativeBacking(Image image) {
    if (image.backing == null || !(image.backing instanceof NativeImageBacking)) {
      throw new IllegalStateException("image did not materialize a native backing");
    }
    return (NativeImageBacking) image.backing;
  }

  private void finish(boolean passed, Throwable failure) {
    if (finished) {
      return;
    }
    finished = true;
    if (failure != null) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }
    Thread activeWorker = ImagePreparationScheduler.processWorkerForTest();
    if (forceSemaphoreWorker()) {
      singleWorker &= activeWorker != null && activeWorker == processWorker
          && ImagePreparationScheduler.processWorkerStartCountForTest() == 1
          && ImagePreparationScheduler.semaphoreWakeCountForTest() == 3;
    } else {
      legacyWorker &= activeWorker == null && processWorker == null
          && ImagePreparationScheduler.processWorkerStartCountForTest() == 0
          && ImagePreparationScheduler.semaphoreWakeCountForTest() == 0;
    }
    if (forceSemaphoreWorker() && activeWorker != null && ImagePreparationScheduler.idleForTest()) {
      ImagePreparationScheduler.awaitWorkerWaitingForSignalForTest();
    }
    idleWorker = ImagePreparationScheduler.idleForTest() && (forceSemaphoreWorker()
        ? "RUNNING_OR_BLOCKED".equals(ImagePreparationScheduler.workerLifecycleForTest())
            && ImagePreparationScheduler.workerWaitingForSignalForTest()
        : "NOT_STARTED".equals(ImagePreparationScheduler.workerLifecycleForTest()));
    boolean overall = passed && staticPngDenominator && colorBackingReady
        && indexedBackingReady && asyncSubmission && uiCallbacks
        && (forceSemaphoreWorker() ? singleWorker : !semaphoreWorker && legacyWorker)
        && activeAtMostOne && maxActive <= 1
        && fullDecodeOnly && drawReuse && staleCandidatesReleased && idleWorker;
    try {
      if (forceSemaphoreWorker() && ImagePreparationScheduler.idleForTest()) {
        ImagePreparationScheduler.shutdownSemaphoreWorkerForTest();
        cleanShutdown = "SHUTDOWN".equals(ImagePreparationScheduler.workerLifecycleForTest());
      } else if (!forceSemaphoreWorker()) {
        cleanShutdown = ImagePreparationScheduler.idleForTest()
            && "NOT_STARTED".equals(ImagePreparationScheduler.workerLifecycleForTest());
      }
      ImagePrefetchWorkerSmokeTestSupport.restore();
    } catch (Throwable shutdownFailure) {
      overall = false;
      error = shutdownFailure.getClass().getName() + ":"
          + String.valueOf(shutdownFailure.getMessage()).replace(' ', '_');
    }
    overall &= cleanShutdown;
    System.out.println("fixture=ImagePngAsyncPreparationSmokeApp,semaphoreWorker=" + semaphoreWorker
        + ",legacyWorker=" + legacyWorker + ",staticPngDenominator=" + staticPngDenominator
        + ",colorBackingReady=" + colorBackingReady
        + ",indexedBackingReady=" + indexedBackingReady + ",uiCallbacks=" + uiCallbacks
        + ",asyncSubmission=" + asyncSubmission
        + ",singleWorker=" + singleWorker + ",activeAtMostOne=" + activeAtMostOne
        + ",fullDecodeOnly=" + fullDecodeOnly + ",drawReuse=" + drawReuse
        + ",staleCandidatesReleased=" + staleCandidatesReleased + ",idleWorker=" + idleWorker
        + ",staleCandidateCreated=" + staleCandidatesCreated
        + ",staleCandidateReleased=" + staleCandidateReleaseCount
        + ",staleLiveBefore=" + staleLiveBefore + ",staleLiveAfter=" + staleLiveAfter
        + ",cleanShutdown=" + cleanShutdown + ",overallPass=" + overall
        + (error.length() == 0 ? "" : ",error=" + error));
    System.out.flush();
    exit(overall ? 0 : 1);
  }
}
