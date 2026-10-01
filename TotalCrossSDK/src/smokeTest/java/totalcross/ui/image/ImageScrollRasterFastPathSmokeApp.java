// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Vm;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** Native smoke for deferred Image copyRect plan handling. */
public class ImageScrollRasterFastPathSmokeApp extends MainWindow {
  private static final int SENTINEL = 0xFF203040;

  @Override
  public void initUI() {
    boolean sourceSubrectAndDestination = false;
    boolean translatedPartialClip = false;
    boolean emptyIntersectionNoMutation = false;
    boolean planCopyHandled = false;
    boolean deferredSourcesRemainDeferred = false;
    String error = "";

    try {
      byte[] png = Vm.getFile("image-abi/tiny.png");
      require(png != null && png.length > 0, "PNG fixture");

      Image expectedSource = deferredCrop(png);
      Image materializedSource = expectedSource.resolveForDrawing(1);
      Image expectedDestination = filled(28, 28);
      expectedDestination.getGraphics().copyRect(materializedSource, 3, 4, 12, 10, 7, 8);

      Image actualSource = deferredCrop(png);
      Image actualDestination = filled(28, 28);
      actualDestination.getGraphics().copyRect(actualSource, 3, 4, 12, 10, 7, 8);
      sourceSubrectAndDestination = samePixels(expectedDestination, actualDestination);
      deferredSourcesRemainDeferred = actualSource.backing == null && actualSource.pipelineForSmoke() != null;
      require(sourceSubrectAndDestination, "source subrect and non-zero destination");

      Image clipExpectedSource = deferredCrop(png);
      Image clipMaterializedSource = clipExpectedSource.resolveForDrawing(1);
      Image clipExpectedDestination = filled(28, 28);
      Graphics expectedGraphics = clipExpectedDestination.getGraphics();
      expectedGraphics.translate(1, 1);
      expectedGraphics.setClip(2, 2, 10, 10);
      expectedGraphics.copyRect(clipMaterializedSource, 0, 0, 20, 20, 0, 0);

      Image clipActualSource = deferredCrop(png);
      Image clipActualDestination = filled(28, 28);
      Graphics actualGraphics = clipActualDestination.getGraphics();
      actualGraphics.translate(1, 1);
      actualGraphics.setClip(2, 2, 10, 10);
      actualGraphics.copyRect(clipActualSource, 0, 0, 20, 20, 0, 0);
      translatedPartialClip = samePixels(clipExpectedDestination, clipActualDestination);
      require(translatedPartialClip, "translated partial clip");

      Image noOpSource = deferredCrop(png);
      Image noOpDestination = filled(28, 28);
      Graphics noOpGraphics = noOpDestination.getGraphics();
      noOpGraphics.setClip(0, 0, 2, 2);
      long generation = noOpDestination.backing.mutationGeneration();
      int beforeHash = pixelHash(noOpDestination);
      noOpGraphics.copyRect(noOpSource, 0, 0, 20, 20, 8, 8);
      emptyIntersectionNoMutation = generation == noOpDestination.backing.mutationGeneration()
          && beforeHash == pixelHash(noOpDestination);
      require(emptyIntersectionNoMutation, "empty intersection changed destination");

      planCopyHandled = ImageRasterFeatureBridge.copyRectPlanAttemptsForTest > 0
          && ImageRasterFeatureBridge.copyRectPlanHandledForTest > 0;
      require(planCopyHandled, "no plan-aware copy was handled");
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean overallPass = sourceSubrectAndDestination && translatedPartialClip
        && emptyIntersectionNoMutation && planCopyHandled && deferredSourcesRemainDeferred;
    System.out.println("fixture=ImageScrollRasterFastPathSmokeApp,sourceSubrectAndDestination="
        + sourceSubrectAndDestination + ",translatedPartialClip=" + translatedPartialClip
        + ",emptyIntersectionNoMutation=" + emptyIntersectionNoMutation + ",planCopyHandled="
        + planCopyHandled + ",copyPlanAttempts=" + ImageRasterFeatureBridge.copyRectPlanAttemptsForTest
        + ",copyPlanHandledCount=" + ImageRasterFeatureBridge.copyRectPlanHandledForTest
        + ",copyPlanFallbacks=" + ImageRasterFeatureBridge.copyRectPlanFallbacksForTest
        + ",copyPlanLastStatus=" + ImageRasterFeatureBridge.copyRectPlanLastStatusForTest
        + ",deferredSourcesRemainDeferred=" + deferredSourcesRemainDeferred
        + ",overallPass=" + overallPass + ",error=" + error);
    exit(overallPass ? 0 : 1);
  }

  private static Image deferredCrop(byte[] png) throws ImageException {
    return new Image(png).getClippedInstance(4, 4, 20, 20);
  }

  private static Image filled(int width, int height) throws ImageException {
    Image image = new Image(width, height);
    Graphics graphics = image.getGraphics();
    graphics.backColor = SENTINEL;
    graphics.fillRect(0, 0, width, height);
    return image;
  }

  private static boolean samePixels(Image first, Image second) {
    int[] firstPixels = first.getPixels();
    int[] secondPixels = second.getPixels();
    if (firstPixels == null || secondPixels == null || firstPixels.length != secondPixels.length) {
      return false;
    }
    for (int i = 0; i < firstPixels.length; i++) {
      if (firstPixels[i] != secondPixels[i]) {
        return false;
      }
    }
    return true;
  }

  private static int pixelHash(Image image) {
    int[] pixels = image.getPixels();
    int hash = 1;
    for (int i = 0; i < pixels.length; i++) {
      hash = 31 * hash + pixels[i];
    }
    return hash;
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
