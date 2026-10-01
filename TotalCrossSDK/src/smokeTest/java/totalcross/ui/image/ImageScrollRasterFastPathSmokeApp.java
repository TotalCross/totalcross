// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Vm;
import totalcross.sys.RuntimeDiagnosticSnapshot;
import totalcross.sys.RuntimeDiagnostics;
import totalcross.sys.runtime.ImageRasterSmokeTestSupport;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** Native smoke for deferred Image copyRect plan handling. */
public class ImageScrollRasterFastPathSmokeApp extends MainWindow {
  private static final int SENTINEL = 0xFF203040;
  private static long imageDiagnosticDeltaForSmoke = -1;

  @Override
  public void initUI() {
    boolean sourceSubrectAndDestination = false;
    boolean translatedPartialClip = false;
    boolean emptyIntersectionNoMutation = false;
    boolean planCopyHandled = false;
    boolean physicalIdentityCopy = false;
    boolean smoothScalePhysicalIdentityCopy = false;
    boolean cachedFinalRasterCopy = false;
    boolean cacheProbeMissContinuesToPlanCopy = false;
    boolean deferredSourcesRemainDeferred = false;
    String error = "";

    try {
      ImageRasterFeatureBridge.resetDrawAccountingForTest();
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

      physicalIdentityCopy = physicalIdentityCopy();
      require(physicalIdentityCopy, "direct physical copy or its mutation record failed");

      smoothScalePhysicalIdentityCopy = smoothScalePhysicalIdentityCopy();
      require(smoothScalePhysicalIdentityCopy, "exact smooth-scale physical copy failed");

      cachedFinalRasterCopy = cachedFinalRasterCopy(png);
      require(cachedFinalRasterCopy, "cached final raster copy was not reused");

      planCopyHandled = ImageRasterFeatureBridge.copyRectPlanAttemptsForTest > 0
          && ImageRasterFeatureBridge.copyRectPlanHandledForTest > 0;
      require(planCopyHandled, "no plan-aware copy was handled");
      cacheProbeMissContinuesToPlanCopy = ImageRasterFeatureBridge.cachedFinalRasterProbesForTest > 0
          && ImageRasterFeatureBridge.cachedFinalRasterMissesForTest > 0
          && ImageRasterFeatureBridge.cachedFinalRasterHitsForTest == 1
          && ImageRasterFeatureBridge.copyRectPlanAttemptsForTest > 0
          && ImageRasterFeatureBridge.copyRectPlanAttemptsForTest
              <= ImageRasterFeatureBridge.cachedFinalRasterMissesForTest
          && actualSource.backing == null && actualSource.pipelineForSmoke() != null;
      require(cacheProbeMissContinuesToPlanCopy, "cache miss did not continue to plan-aware copy");
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean overallPass = sourceSubrectAndDestination && translatedPartialClip
        && emptyIntersectionNoMutation && physicalIdentityCopy && smoothScalePhysicalIdentityCopy
        && cachedFinalRasterCopy && planCopyHandled
        && cacheProbeMissContinuesToPlanCopy
        && deferredSourcesRemainDeferred;
    System.out.println("fixture=ImageScrollRasterFastPathSmokeApp,sourceSubrectAndDestination="
        + sourceSubrectAndDestination + ",translatedPartialClip=" + translatedPartialClip
        + ",emptyIntersectionNoMutation=" + emptyIntersectionNoMutation + ",planCopyHandled="
        + planCopyHandled + ",physicalIdentityCopy=" + physicalIdentityCopy
        + ",smoothScalePhysicalIdentityCopy=" + smoothScalePhysicalIdentityCopy
        + ",cachedFinalRasterCopy=" + cachedFinalRasterCopy
        + ",copyPlanAttempts=" + ImageRasterFeatureBridge.copyRectPlanAttemptsForTest
        + ",cachedFinalRasterProbes=" + ImageRasterFeatureBridge.cachedFinalRasterProbesForTest
        + ",cachedFinalRasterHits=" + ImageRasterFeatureBridge.cachedFinalRasterHitsForTest
        + ",cachedFinalRasterMisses=" + ImageRasterFeatureBridge.cachedFinalRasterMissesForTest
        + ",copyPlanHandledCount=" + ImageRasterFeatureBridge.copyRectPlanHandledForTest
        + ",copyPlanFallbacks=" + ImageRasterFeatureBridge.copyRectPlanFallbacksForTest
        + ",copyPlanLastStatus=" + ImageRasterFeatureBridge.copyRectPlanLastStatusForTest
        + ",imageDiagnosticDelta=" + imageDiagnosticDeltaForSmoke
        + ",cacheProbeMissContinuesToPlanCopy=" + cacheProbeMissContinuesToPlanCopy
        + ",deferredSourcesRemainDeferred=" + deferredSourcesRemainDeferred
        + ",overallPass=" + overallPass + ",error=" + error);
    exit(overallPass ? 0 : 1);
  }

  private static Image deferredCrop(byte[] png) throws ImageException {
    return new Image(png).getClippedInstance(4, 4, 20, 20);
  }

  private static boolean physicalIdentityCopy() throws Exception {
    ImageRasterSmokeTestSupport.setRasterFeatures(true, false, false);
    int[] sourcePixels = {
        0xFF102030, 0xFF405060,
        0xFF708090, 0xFFA0B0C0
    };
    Image source = new Image(2, 2);
    require(source.getGraphics().setRGB(sourcePixels, 0, 0, 0, 2, 2) == sourcePixels.length,
        "could not initialize opaque source");
    Image deferred = source.getClippedInstance(1, 0, 1, 2);

    Image expectedDestination = filled(4, 4);
    Graphics expectedGraphics = expectedDestination.getGraphics();
    expectedGraphics.setClip(1, 2, 1, 1);
    expectedGraphics.copyRect(deferred.resolveForDrawing(1), 0, 0, 1, 2, 1, 1);
    Image actualDestination = filled(4, 4);
    long generationBefore = actualDestination.backing.mutationGeneration();
    Graphics actualGraphics = actualDestination.getGraphics();
    actualGraphics.setClip(1, 2, 1, 1);
    boolean diagnosticsSupported = RuntimeDiagnostics.isSupported();
    if (diagnosticsSupported) {
      RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.IMAGE, true);
    }
    RuntimeDiagnosticSnapshot diagnosticsBefore = RuntimeDiagnostics.snapshot();
    actualGraphics.copyRect(deferred, 0, 0, 1, 2, 1, 1);
    RuntimeDiagnosticSnapshot diagnosticsAfter = RuntimeDiagnostics.snapshot();
    if (diagnosticsSupported) {
      imageDiagnosticDeltaForSmoke = diagnosticsAfter.deltaSince(diagnosticsBefore)
          .getValue(RuntimeDiagnosticSnapshot.Domain.IMAGE, RuntimeDiagnosticSnapshot.Kind.COUNTER);
    }
    int status = ImageRasterFeatureBridge.copyRectPlanLastStatusForTest;

    return samePixels(expectedDestination, actualDestination)
        && (status & ImageRasterFeatureBridge.PHYSICAL_COPY_HIT) != 0
        && (!diagnosticsSupported || imageDiagnosticDeltaForSmoke == 4)
        && deferred.backing == null && deferred.pipelineForSmoke() != null
        && actualDestination.backing.mutationGeneration() > generationBefore;
  }

  private static boolean cachedFinalRasterCopy(byte[] png) throws Exception {
    Image source = deferredCrop(png);
    source.resolveForDrawing(1);
    Image cached = source.resolveForDrawing(1);
    int cachedHitsBefore = ImageRasterFeatureBridge.cachedFinalRasterHitsForTest;
    int planAttemptsBefore = ImageRasterFeatureBridge.copyRectPlanAttemptsForTest;

    Image expectedDestination = filled(28, 28);
    expectedDestination.getGraphics().copyRect(cached, 0, 0, 20, 20, 3, 4);
    Image actualDestination = filled(28, 28);
    actualDestination.getGraphics().copyRect(source, 0, 0, 20, 20, 3, 4);

    return samePixels(expectedDestination, actualDestination)
        && ImageRasterFeatureBridge.cachedFinalRasterHitsForTest == cachedHitsBefore + 1
        && ImageRasterFeatureBridge.copyRectPlanAttemptsForTest == planAttemptsBefore
        && source.backing == null && source.pipelineForSmoke() != null;
  }

  private static boolean smoothScalePhysicalIdentityCopy() throws Exception {
    int[] pixels = {0xFF102030, 0xFF405060, 0xFF708090, 0xFFA0B0C0};
    Image expectedBase = new Image(2, 2);
    if (expectedBase.getGraphics().setRGB(pixels, 0, 0, 0, 2, 2) != pixels.length) {
      return false;
    }
    Image expectedSource = expectedBase.getSmoothScaledInstance(1, 1);
    Image materialized = expectedSource.resolveForDrawing(2);
    Image expectedDestination = Image.createLogical(1, 1, 2);
    expectedDestination.getGraphics().copyRect(materialized, 0, 0, 1, 1, 0, 0);

    Image actualBase = new Image(2, 2);
    if (actualBase.getGraphics().setRGB(pixels, 0, 0, 0, 2, 2) != pixels.length) {
      return false;
    }
    Image deferredSource = actualBase.getSmoothScaledInstance(1, 1);
    Image actualDestination = Image.createLogical(1, 1, 2);
    int attemptsBefore = ImageRasterFeatureBridge.copyRectPlanAttemptsForTest;
    int genericBefore = ImageRasterFeatureBridge.genericGeometryDrawsForTest;
    int smoothBefore = ImageRasterFeatureBridge.smoothResampleDrawsForTest;
    actualDestination.getGraphics().copyRect(deferredSource, 0, 0, 1, 1, 0, 0);
    int status = ImageRasterFeatureBridge.copyRectPlanLastStatusForTest;

    return samePixels(expectedDestination, actualDestination)
        && (status & ImageRasterFeatureBridge.PHYSICAL_COPY_HIT) != 0
        && ImageRasterFeatureBridge.copyRectPlanAttemptsForTest == attemptsBefore + 1
        && ImageRasterFeatureBridge.genericGeometryDrawsForTest == genericBefore
        && ImageRasterFeatureBridge.smoothResampleDrawsForTest == smoothBefore
        && deferredSource.backing == null && deferredSource.pipelineForSmoke() != null;
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
