// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** Focused deployed smoke for lazy explicit JPEG factories and real drawing. */
public class ImageJpegFactorySmokeApp extends MainWindow {
  @Override
  public void initUI() {
    boolean bestFitLazy = false;
    boolean explicitRatioLazy = false;
    boolean referenceParity = false;
    boolean screenDraw = false;
    boolean repeatedDrawReuse = false;
    boolean gpuBacking = false;
    boolean nativeSourceCapture = false;
    String error = "";
    try {
      int checks = runFactoryChecks();
      bestFitLazy = (checks & 1) != 0;
      explicitRatioLazy = (checks & 2) != 0;
      referenceParity = (checks & 4) != 0;
      screenDraw = (checks & 8) != 0;
      repeatedDrawReuse = (checks & 16) != 0;
      gpuBacking = (checks & 32) != 0;
      nativeSourceCapture = (checks & 64) != 0;
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }
    boolean overallPass = bestFitLazy && explicitRatioLazy && referenceParity && screenDraw
        && repeatedDrawReuse && gpuBacking && nativeSourceCapture;
    System.out.println("fixture=ImageJpegFactorySmokeApp,bestFitLazy=" + bestFitLazy
        + ",explicitRatioLazy=" + explicitRatioLazy + ",referenceParity=" + referenceParity
        + ",screenDraw=" + screenDraw + ",repeatedDrawReuse=" + repeatedDrawReuse
        + ",gpuBacking=" + gpuBacking + ",nativeSourceCapture=" + nativeSourceCapture
        + ",overallPass=" + overallPass
        + (error.length() == 0 ? "" : ",error=" + error));
    System.out.flush();
    exit(overallPass ? 0 : 1);
  }

  private int runFactoryChecks() throws Exception {
    byte[] sourceBytes = Vm.getFile("image-abi/lena512.jpg");
    require(sourceBytes != null && sourceBytes.length > 0, "JPEG factory resource");
    Graphics screen = getGraphics();
    require(screen != null, "main window graphics");
    Image.resetImageOperationAccountingForTest();
    Image bestFit = Image.getJpegBestFit("image-abi/lena512.jpg", 128, 128);
    Image explicitRatio = Image.getJpegScaled("image-abi/lena512.jpg", 3, 4);
    boolean bestFitLazy = bestFit.pipelineForSmoke() != null && bestFit.getPixelWidth() == 128
        && bestFit.getPixelHeight() == 128 && Image.fullDecodeInvocationCountForTest() == 0
        && Image.targetedDecodeInvocationCountForTest() == 0 && Image.materializationCountForTest() == 0;
    boolean explicitRatioLazy = explicitRatio.pipelineForSmoke() != null
        && explicitRatio.getPixelWidth() == 384 && explicitRatio.getPixelHeight() == 384
        && Image.fullDecodeInvocationCountForTest() == 0
        && Image.targetedDecodeInvocationCountForTest() == 0;
    EncodedImageSource bestFitSource = (EncodedImageSource) bestFit.pipelineForSmoke().root();
    EncodedImageSource explicitSource = (EncodedImageSource) explicitRatio.pipelineForSmoke().root();
    boolean nativeSourceCapture = bestFitSource.hasNativeBackingForSmoke()
        && !bestFitSource.hasJavaBackingForSmoke()
        && explicitSource.hasNativeBackingForSmoke()
        && !explicitSource.hasJavaBackingForSmoke();
    require(bestFitLazy && explicitRatioLazy, "factory decode happened before barrier");

    screen.drawImage(bestFit, 0, 0, true);
    screen.drawImage(explicitRatio, 130, 0, true);
    Image bestFitResolved = bestFit.resolveForDrawing(1);
    Image explicitRatioResolved = explicitRatio.resolveForDrawing(1);
    int decodeCount = Image.fullDecodeInvocationCountForTest()
        + Image.targetedDecodeInvocationCountForTest();
    int materializationCount = Image.materializationCountForTest();
    boolean screenDraw = bestFitResolved.getPixelWidth() == 128 && bestFitResolved.getPixelHeight() == 128
        && explicitRatioResolved.getPixelWidth() == 384 && explicitRatioResolved.getPixelHeight() == 384
        && decodeCount == 2 && materializationCount == 2;

    Image bestReference = new Image(sourceBytes).getSmoothScaledInstance(128, 128);
    Image explicitReference = new Image(sourceBytes).getSmoothScaledInstance(384, 384);
    ImageCompactFormatsBenchmarkSupport.Quality bestQuality =
        ImageCompactFormatsBenchmarkSupport.quality(bestFitResolved.getPixels(), bestReference.getPixels());
    ImageCompactFormatsBenchmarkSupport.Quality explicitQuality =
        ImageCompactFormatsBenchmarkSupport.quality(explicitRatioResolved.getPixels(), explicitReference.getPixels());
    System.out.println("reference_best_max=" + bestQuality.maxError + ",reference_best_rmse=" + bestQuality.rmse
        + ",reference_explicit_max=" + explicitQuality.maxError
        + ",reference_explicit_rmse=" + explicitQuality.rmse);
    boolean referenceParity = bestQuality.maxError <= 128 && bestQuality.rmse <= 24.0
        && explicitQuality.maxError <= 128 && explicitQuality.rmse <= 24.0;

    int decodeCountAfterReferences = Image.fullDecodeInvocationCountForTest()
        + Image.targetedDecodeInvocationCountForTest();
    int materializationCountAfterReferences = Image.materializationCountForTest();
    screen.drawImage(bestFit, 0, 0, true);
    screen.drawImage(explicitRatio, 130, 0, true);
    boolean repeatedDrawReuse = decodeCountAfterReferences == Image.fullDecodeInvocationCountForTest()
        + Image.targetedDecodeInvocationCountForTest()
        && materializationCountAfterReferences == Image.materializationCountForTest();
    boolean gpuBacking = !Settings.isOpenGL
        || (bestFitResolved.hasNativeBackingForSmoke() && explicitRatioResolved.hasNativeBackingForSmoke());
    int result = 0;
    if (bestFitLazy) result |= 1;
    if (explicitRatioLazy) result |= 2;
    if (referenceParity) result |= 4;
    if (screenDraw) result |= 8;
    if (repeatedDrawReuse) result |= 16;
    if (gpuBacking) result |= 32;
    if (nativeSourceCapture) result |= 64;
    return result;
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
