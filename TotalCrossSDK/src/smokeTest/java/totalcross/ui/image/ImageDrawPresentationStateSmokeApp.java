// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.io.ByteArrayStream;
import totalcross.sys.Vm;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** Deployed macOS smoke for fresh mutable presentation state on cached plans. */
public class ImageDrawPresentationStateSmokeApp extends MainWindow {
  private static final int FRAME_ZERO = 0xFFFF0000;
  private static final int FRAME_ONE = 0xFF0000FF;
  private static final int TRANSPARENT_COLOR = 0x00112233;

  @Override
  public void initUI() {
    boolean firstDraw = false;
    boolean currentFrameRefresh = false;
    boolean alphaRefresh = false;
    boolean transparentColorRefresh = false;
    boolean hwScaleRefresh = false;
    boolean secondDrawUsesCurrentState = false;
    boolean zeroAllocationCacheHit = false;
    int structuralPlanBuilds = -1;
    int capabilityArrayAllocations = -1;
    int cacheHits = -1;
    int presentationPlanRecreations = -1;
    int imageCreations = -1;
    int pipelineCreations = -1;
    String error = "";

    try {
      byte[] encoded = encodedStrip();
      Image.resetImageOperationAccountingForTest();
      Image deferred = new Image(encoded);
      deferred.setFrameCount(2);
      deferred = deferred.getSmoothScaledInstance(4, 2);
      Image firstTarget = new Image(deferred.getPixelWidth(), deferred.getPixelHeight());
      Image secondTarget = new Image(deferred.getPixelWidth(), deferred.getPixelHeight());
      ImageDrawPlan firstPlan = (ImageDrawPlan) deferred.drawPlanForDrawing(1);
      ImageDrawPlan stablePlan = (ImageDrawPlan) deferred.drawPlanForDrawing(1);
      require(stablePlan == firstPlan, "stable encoded draw plan warmup");
      drawIntoTarget(deferred, firstTarget);
      int[] first = firstTarget.getPixels();
      firstDraw = firstPlan.currentFrame == 0 && firstPlan.alphaMask == 255
          && firstPlan.hwScaleW == 1 && firstPlan.hwScaleH == 1
          && first[0] == FRAME_ZERO;
      require(firstDraw, "first presentation state");

      deferred.setCurrentFrame(1);
      deferred.alphaMask = 128;
      deferred.transparentColor = TRANSPARENT_COLOR;
      ImageDrawPlan secondPlan = (ImageDrawPlan) deferred.drawPlanForDrawing(1);
      drawIntoTarget(deferred, secondTarget);
      int[] second = secondTarget.getPixels();
      currentFrameRefresh = secondPlan == firstPlan && second[0] == 0x800000FF;
      alphaRefresh = (second[0] >>> 24) == 128;
      transparentColorRefresh = secondPlan.presentation == deferred;
      secondDrawUsesCurrentState = second.length == 8 && (second[0] >>> 24) == 128;

      Image.resetImageOperationAccountingForTest();
      for (int i = 0; i < 1000; i++) {
        deferred.setCurrentFrame(i & 1);
        deferred.alphaMask = (i & 1) == 0 ? 255 : 128;
        ImageDrawPlan cachedPlan = (ImageDrawPlan) deferred.drawPlanForDrawing(1);
        require(cachedPlan == firstPlan, "cached presentation plan");
      }
      imageCreations = Image.imageCreatedCountForTest();
      pipelineCreations = Image.imagePipelineCreatedCountForTest();
      structuralPlanBuilds = Image.imageDrawPlanCreatedCountForTest();
      capabilityArrayAllocations = Image.imageDrawPlanCapabilitiesAllocatedCountForTest();
      cacheHits = Image.imageDrawPlanCacheHitCountForTest();
      presentationPlanRecreations = Image.presentationOnlyPlanRecreationCountForTest();
      zeroAllocationCacheHit = imageCreations == 0 && pipelineCreations == 0
          && structuralPlanBuilds == 0 && capabilityArrayAllocations == 0
          && cacheHits >= 1000 && presentationPlanRecreations == 0;

      deferred.hwScaleW = 0.5;
      deferred.hwScaleH = 0.5;
      ImageDrawPlan scaledStatePlan = (ImageDrawPlan) deferred.drawPlanForDrawing(1);
      hwScaleRefresh = scaledStatePlan == firstPlan && deferred.getWidth() == 2 && deferred.getHeight() == 1;
      require(currentFrameRefresh && alphaRefresh && transparentColorRefresh && hwScaleRefresh
          && secondDrawUsesCurrentState && zeroAllocationCacheHit, "refreshed presentation state");
    } catch (Throwable failure) {
      failure.printStackTrace();
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean overallPass = firstDraw && currentFrameRefresh && alphaRefresh && transparentColorRefresh
        && hwScaleRefresh && secondDrawUsesCurrentState && zeroAllocationCacheHit;
    System.out.println("fixture=ImageDrawPresentationStateSmokeApp,firstDraw=" + firstDraw
        + ",currentFrameRefresh=" + currentFrameRefresh + ",alphaRefresh=" + alphaRefresh
        + ",transparentColorRefresh=" + transparentColorRefresh + ",hwScaleRefresh=" + hwScaleRefresh
        + ",secondDrawUsesCurrentState=" + secondDrawUsesCurrentState + ",zeroAllocationCacheHit="
        + zeroAllocationCacheHit + ",structuralPlanBuilds=" + structuralPlanBuilds
        + ",capabilityArrayAllocations=" + capabilityArrayAllocations + ",cacheHits=" + cacheHits
        + ",presentationPlanRecreations=" + presentationPlanRecreations + ",imageCreations="
        + imageCreations + ",pipelineCreations=" + pipelineCreations + ",overallPass=" + overallPass
        + (error.length() == 0 ? "" : ",error=" + error));
    exit(overallPass ? 0 : 1);
  }

  private static byte[] encodedStrip() throws Exception {
    Image image = new Image(8, 2);
    Graphics graphics = image.getGraphics();
      graphics.backColor = FRAME_ZERO & 0x00FFFFFF;
      graphics.fillRect(0, 0, 4, 2);
      graphics.backColor = FRAME_ONE & 0x00FFFFFF;
      graphics.fillRect(4, 0, 4, 2);
    ByteArrayStream stream = new ByteArrayStream(512);
    image.createPng(stream);
    byte[] result = new byte[stream.getPos()];
    Vm.arrayCopy(stream.getBuffer(), 0, result, 0, result.length);
    return result;
  }

  private static void drawIntoTarget(Image image, Image target) throws Exception {
    target.getGraphics().drawImage(image, 0, 0, true);
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
