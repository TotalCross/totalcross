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
      Image.resetImageOperationAccountingForTest();
      NativeImageBacking.failNextAdoptionForTest();
      screen = getGraphics();
      image = new Image("image-abi/lena512.jpg").getSmoothScaledInstance(128, 128);
      source = (EncodedImageSource) image.pipelineForSmoke().root();
      scroll = new ScrollContainer(false, false);
      Container row = new Container();
      row.add(new ImageControl(image), LEFT, TOP, 128, 128);
      row.add(new ImageControl(image), AFTER, TOP, 128, 128);
      scroll.add(row, LEFT, TOP, 256, 128);
      scroll.setRect(0, 0, 256, 128);
      scroll.resize();
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
    boolean capturedOptimizationMask = detachedOptimizationMask == (int) ImageOptimizationSettings.effectiveMask();
    boolean stillDeferred = image != null && image.pipelineForSmoke() != null;
    boolean drew = false;
    try {
      if (callbackSeen && screen != null) {
        int before = Image.fullDecodeInvocationCountForTest() + Image.targetedDecodeInvocationCountForTest();
        screen.drawImage(image, 0, 0, true);
        drew = before == Image.fullDecodeInvocationCountForTest() + Image.targetedDecodeInvocationCountForTest();
      }
    } catch (Throwable ignored) {
      drew = false;
    }
    if (!callbackSeen && Vm.getTimeStamp() - started >= 5000) {
      finish(false, false, timerTicks > 0, false, "preparation timeout");
      return;
    }
    if (callbackSeen) {
      finish(decoded && drew && adoptionFailureRetried && capturedOptimizationMask,
          callbackOnUi, timerTicks > 0, stillDeferred, "");
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
      boolean deferredPlan, String error) {
    if (timer != null) {
      removeTimer(timer);
      timer = null;
    }
    boolean pass = detachedAdoption && uiCompletion && responsive && deferredPlan && error.length() == 0;
    System.out.println("fixture=ImagePreparationSmokeApp,detachedAdoption=" + detachedAdoption
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
