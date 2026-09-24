// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Vm;
import totalcross.ui.Container;
import totalcross.ui.ImageControl;
import totalcross.ui.MainWindow;
import totalcross.ui.ScrollContainer;
import totalcross.ui.event.TimerEvent;
import totalcross.ui.event.TimerListener;
import totalcross.ui.gfx.Graphics;

/** Focused macOS smoke for detached decode and UI-thread candidate adoption. */
public class ImagePreparationSmokeApp extends MainWindow implements TimerListener {
  private Image image;
  private EncodedImageSource source;
  private Graphics screen;
  private ScrollContainer scroll;
  private TimerEvent timer;
  private boolean callbackSeen;
  private boolean retryRequested;
  private boolean adoptionFailureRetried;
  private int callbackCount;
  private String callbackThread;
  private int timerTicks;
  private long generationBeforeDraw = -1;
  private int targetedBeforeDraw = -1;
  private int denominatorBeforeDraw = -1;
  private int detachedOptimizationMask = -1;
  private int sourceWidth = -1;
  private int sourceHeight = -1;
  private String pngKind = "ordinary";
  private long candidateBackingCreated = -1;
  private long candidateBackingReleased = -1;
  private long candidateBackingLive = -1;
  private long started;

  @Override
  public void initUI() {
    boolean detachedAdoption = false;
    boolean uiCompletion = false;
    boolean responsive = false;
    boolean deferredPlan = false;
    String error = "";
    try {
      ImageRasterBenchmarkSupport.configureApplicationRasterFeatures("post-enabled");
      screen = getGraphics();
      String pngPath = ImageRasterBenchmarkSupport.argument(
          getCommandLine(), "png", "image-abi/tiny.png");
      pngKind = pngPath.endsWith("indexed.png") ? "indexed" : "ordinary";
      Image encoded = new Image(pngPath);
      sourceWidth = encoded.getWidth();
      sourceHeight = encoded.getHeight();
      source = (EncodedImageSource) encoded.pipelineForSmoke().root();
      image = encoded.getSmoothScaledInstance(128, 128);
      scroll = new ScrollContainer(false, false);
      Container row = new Container();
      ImageControl first = new ImageControl(image);
      first.allowBeyondLimits = true;
      ImageControl second = new ImageControl(image);
      second.allowBeyondLimits = true;
      row.add(first, LEFT, TOP, 128, 128);
      row.add(second, AFTER, TOP, 128, 128);
      scroll.add(row, LEFT, TOP, 256, 128);
      scroll.setRect(0, 0, 256, 128);
      scroll.resize();
      Image.resetImageOperationAccountingForTest();
      NativeImageBacking.resetBackingAccountingForTest();
      ImagePreparation.setBeforeAdoptionHookForTest(new Runnable() {
        @Override
        public void run() {
          throw new RuntimeException("discard first prepared candidate");
        }
      });
      started = Vm.getTimeStamp();
      scroll.prepareForDisplay(new Runnable() {
            @Override
            public void run() {
              preparationCompleted();
            }
          });
      addTimerListener(this);
      timer = addTimer(10);
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }
    if (error.length() != 0) {
      System.out.println("fixture=ImagePreparationSmokeApp,error=" + error + ",overallPass=false");
      System.out.flush();
      exit(1);
    }
  }

  @Override
  public void timerTriggered(TimerEvent event) {
    timerTicks++;
    if (!callbackSeen && Vm.getTimeStamp() - started < 5000) {
      return;
    }
    boolean callbackOnUi = callbackSeen && callbackThread != null
        && callbackThread.equals(Thread.currentThread().getName());
    boolean decoded = source != null && source.decodedGeneration() == 1
        && Image.fullDecodeInvocationCountForTest() + Image.targetedDecodeInvocationCountForTest() == 2;
    generationBeforeDraw = source == null ? -1 : source.decodedGeneration();
    denominatorBeforeDraw = source == null ? -1 : source.decodedDenominator();
    targetedBeforeDraw = Image.targetedDecodeInvocationCountForTest();
    detachedOptimizationMask = Image.detachedDecodeOptimizationMaskForTest();
    boolean pngSource = source != null
        && source.getFormat() == ImageEncodedStructure.Format.PNG;
    boolean capturedOptimizationMask = pngSource ? detachedOptimizationMask == 0
        : detachedOptimizationMask == (int) ImageOptimizationSettings.effectiveMask();
    boolean stillDeferred = image != null && image.pipelineForSmoke() != null;
    boolean nativeBackingValid = false;
    boolean pngDimensions = false;
    boolean fullPngDecode = false;
    boolean discardedCandidateReleased = false;
    if (callbackSeen) {
      ImageBacking decodedBacking = source == null ? null : source.decodedBackingForReuse(1);
      nativeBackingValid = decodedBacking instanceof NativeImageBacking
          && decodedBacking.isValid();
      int expectedSourceWidth = pngKind.equals("indexed") ? 182 : 36;
      int expectedSourceHeight = pngKind.equals("indexed") ? 26 : 36;
      pngDimensions = sourceWidth == expectedSourceWidth && sourceHeight == expectedSourceHeight
          && source.decodedWidth() == sourceWidth && source.decodedHeight() == sourceHeight
          && image.getWidth() == 128 && image.getHeight() == 128;
      fullPngDecode = source != null && source.decodedDenominator() == 1
          && Image.fullDecodeInvocationCountForTest() == 2
          && Image.targetedDecodeInvocationCountForTest() == 0;
      candidateBackingCreated = NativeImageBacking.backingRecordsCreatedForTest();
      candidateBackingReleased = NativeImageBacking.backingRecordsReleasedForTest();
      candidateBackingLive = NativeImageBacking.backingRecordsLiveForTest();
      discardedCandidateReleased = candidateBackingCreated == 2
          && candidateBackingReleased == 1 && candidateBackingLive == 1;
    }
    boolean drew = false;
    try {
      if (callbackSeen && screen != null) {
        int before = Image.fullDecodeInvocationCountForTest() + Image.targetedDecodeInvocationCountForTest();
        screen.copyRect(image, 0, 0, image.getWidth(), image.getHeight(), 0, 0);
        drew = before == Image.fullDecodeInvocationCountForTest() + Image.targetedDecodeInvocationCountForTest();
      }
    } catch (Throwable ignored) {
      drew = false;
    }
    if (!callbackSeen && Vm.getTimeStamp() - started >= 5000) {
      finish(false, false, timerTicks > 0, false, false, false, false, false, false,
          "preparation timeout");
      return;
    }
    if (callbackSeen) {
      boolean detachedAdoption = decoded && drew && adoptionFailureRetried
          && capturedOptimizationMask && nativeBackingValid && pngDimensions
          && fullPngDecode && discardedCandidateReleased;
      finish(detachedAdoption, callbackOnUi, timerTicks > 0, stillDeferred,
          fullPngDecode, pngDimensions, nativeBackingValid, drew,
          discardedCandidateReleased, "");
    }
  }

  private void preparationCompleted() {
    callbackCount++;
    if (!retryRequested) {
      retryRequested = true;
      scroll.prepareForDisplay(new Runnable() {
        @Override
        public void run() {
          adoptionFailureRetried = true;
          preparationCompleted();
        }
      });
      return;
    }
    callbackSeen = true;
    callbackThread = Thread.currentThread().getName();
  }

  private void finish(boolean detachedAdoption, boolean uiCompletion, boolean responsive,
      boolean deferredPlan, boolean fullPngDecode, boolean pngDimensions,
      boolean nativeBackingValid, boolean immediateReuse, boolean discardedCandidateReleased,
      String error) {
    if (timer != null) {
      removeTimer(timer);
      timer = null;
    }
    boolean pass = detachedAdoption && uiCompletion && responsive && deferredPlan && error.length() == 0;
    System.out.println("fixture=ImagePreparationSmokeApp,detachedAdoption=" + detachedAdoption
        + ",pngKind=" + pngKind + ",pngPrefetch=" + fullPngDecode
        + ",pngDimensions=" + pngDimensions + ",nativeBackingValid=" + nativeBackingValid
        + ",sourceWidth=" + sourceWidth + ",sourceHeight=" + sourceHeight
        + ",immediateReuse=" + immediateReuse
        + ",discardedCandidateReleased=" + discardedCandidateReleased
        + ",candidateBackingCreated=" + candidateBackingCreated
        + ",candidateBackingReleased=" + candidateBackingReleased
        + ",candidateBackingLive=" + candidateBackingLive
        + ",uiCompletion=" + uiCompletion + ",responsive=" + responsive
        + ",deferredPlan=" + deferredPlan + ",timerTicks=" + timerTicks
        + ",callbackCount=" + callbackCount + ",adoptionFailureRetried=" + adoptionFailureRetried
        + ",decodedGeneration=" + (source == null ? -1 : source.decodedGeneration())
        + ",generationBeforeDraw=" + generationBeforeDraw
        + ",denominatorBeforeDraw=" + denominatorBeforeDraw
        + ",fullDecodes=" + Image.fullDecodeInvocationCountForTest()
        + ",targetedDecodes=" + Image.targetedDecodeInvocationCountForTest()
        + ",targetedBeforeDraw=" + targetedBeforeDraw
        + ",targetedDenominator=" + Image.targetedDecodeDenominatorForTest()
        + ",detachedOptimizationMask=" + detachedOptimizationMask
        + ",overallPass=" + pass + (error.length() == 0 ? "" : ",error=" + error));
    System.out.flush();
    exit(pass ? 0 : 1);
  }
}
