// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Settings;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** Focused compact-storage smoke for lazy JPEG factory policies. */
public class ImageCompactFormatsLazyJpegFactorySmokeApp extends MainWindow {
  private static final int TARGET_SIZE = 256;

  @Override
  public void initUI() {
    boolean rgb565 = false;
    boolean gray8 = false;
    boolean screenDraw = false;
    boolean rasterCountersZero = false;
    String error = "";
    try {
      ImageRasterBenchmarkSupport.require(
          ImageCompactFormatsBenchmarkSupport.formatProbeAvailable(), "native compact format probe");
      CaseResult rgb = runCase("image-opt-phase3/rgb565-source.jpg",
          ImageCompactFormatsBenchmarkSupport.RGB565, false);
      CaseResult gray = runCase("image-opt-phase3/gray8-source.jpg",
          ImageCompactFormatsBenchmarkSupport.GRAY8, true);
      rgb565 = rgb.pass;
      gray8 = gray.pass;
      screenDraw = rgb.screenDraw && gray.screenDraw;
      rasterCountersZero = rgb.rasterCountersZero && gray.rasterCountersZero;
      System.out.println("rgb565Result=" + rgb.describe());
      System.out.println("gray8Result=" + gray.describe());
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }
    boolean overallPass = error.length() == 0 && rgb565 && gray8 && screenDraw
        && rasterCountersZero;
    System.out.println("fixture=ImageCompactFormatsLazyJpegFactorySmokeApp,rgb565=" + rgb565
        + ",gray8=" + gray8 + ",screenDraw=" + screenDraw
        + ",rasterCountersZero=" + rasterCountersZero + ",overallPass=" + overallPass
        + (error.length() == 0 ? "" : ",error=" + error));
    System.out.flush();
    exit(overallPass ? 0 : 1);
  }

  private CaseResult runCase(String path, String expectedFormat, boolean explicitRatio)
      throws Exception {
    ImageRasterBenchmarkSupport.resource(path);
    int[] reference = reference(path, explicitRatio);

    ImageCompactFormatsBenchmarkSupport.configure("post-enabled", "milestone9-full-stack", true);
    Image.resetImageOperationAccountingForTest();
    Image image = explicitRatio
        ? Image.getJpegScaled(path, 1, 2)
        : Image.getJpegBestFit(path, TARGET_SIZE, TARGET_SIZE);
    ImagePipeline pipeline = image.pipelineForSmoke();
    ImageRasterBenchmarkSupport.require(pipeline != null, "lazy factory pipeline");
    Object root = pipeline.root();
    ImageRasterBenchmarkSupport.require(root instanceof EncodedImageSource, "encoded JPEG root");
    EncodedImageSource source = (EncodedImageSource) root;
    boolean lazy = source.hasNativeBackingForSmoke() && !source.hasJavaBackingForSmoke()
        && Image.fullDecodeInvocationCountForTest() == 0
        && Image.targetedDecodeInvocationCountForTest() == 0
        && Image.materializationCountForTest() == 0;

    Graphics screen = getGraphics();
    ImageRasterBenchmarkSupport.require(screen != null, "MainWindow screen graphics");
    screen.drawImage(image, 0, 0, false);
    Image resolved = image.resolveForDrawing(1);
    ImageCompactFormatsBenchmarkSupport.Quality quality =
        ImageCompactFormatsBenchmarkSupport.quality(resolved.getPixels(), reference);
    boolean dimensions = resolved.getPixelWidth() == TARGET_SIZE
        && resolved.getPixelHeight() == TARGET_SIZE;
    boolean backing = expectedFormat.equals(ImageCompactFormatsBenchmarkSupport.format(resolved));
    boolean decodeOnce = Image.fullDecodeInvocationCountForTest()
        + Image.targetedDecodeInvocationCountForTest() == 1
        && Image.materializationCountForTest() == 1;
    boolean compactAccounting = NativeImageBacking.compactDirectDecodeCountForTest() == 1
        && NativeImageBacking.temporaryRgbaDecodeBytesForTest() == 0
        && NativeImageBacking.promotionAttemptsForTest() == 0;
    long decodeCount = Image.fullDecodeInvocationCountForTest()
        + Image.targetedDecodeInvocationCountForTest();
    long materializationCount = Image.materializationCountForTest();
    screen.drawImage(image, 0, 0, false);
    boolean reuse = decodeCount == Image.fullDecodeInvocationCountForTest()
        + Image.targetedDecodeInvocationCountForTest()
        && materializationCount == Image.materializationCountForTest();
    boolean countersZero = !Settings.ANDROID.equals(Settings.platform)
        || !Settings.isOpenGL || rasterCountersZero();
    return new CaseResult(lazy && dimensions && backing && decodeOnce && compactAccounting
        && reuse && quality.maxError <= 32 && quality.rmse <= 9.0,
        true, countersZero, expectedFormat, dimensions, quality.maxError, quality.rmse,
        NativeImageBacking.compactDirectDecodeCountForTest(),
        NativeImageBacking.temporaryRgbaDecodeBytesForTest(),
        NativeImageBacking.promotionAttemptsForTest(), reuse, lazy);
  }

  private static int[] reference(String path, boolean explicitRatio) throws Exception {
    ImageCompactFormatsBenchmarkSupport.configureExplicit("pre", new int[0]);
    Image reference = explicitRatio
        ? Image.getJpegScaled(path, 1, 2)
        : Image.getJpegBestFit(path, TARGET_SIZE, TARGET_SIZE);
    return reference.getPixels();
  }

  private static boolean rasterCountersZero() {
    return NativeImageBacking.writePixelsAttemptsForTest() == 0
        && NativeImageBacking.writePixelsHitsForTest() == 0
        && NativeImageBacking.writePixelsFallbacksForTest() == 0
        && NativeImageBacking.targetColorAttemptsForTest() == 0
        && NativeImageBacking.targetColorMaterializationsForTest() == 0
        && NativeImageBacking.targetColorHitsForTest() == 0
        && NativeImageBacking.physicalVariantLookupsForTest() == 0
        && NativeImageBacking.physicalVariantMaterializationsForTest() == 0
        && NativeImageBacking.physicalVariantHitsForTest() == 0
        && NativeImageBacking.physicalIdentityAttemptsForTest() == 0
        && NativeImageBacking.physicalIdentityHitsForTest() == 0;
  }

  private static final class CaseResult {
    final boolean pass;
    final boolean screenDraw;
    final boolean rasterCountersZero;
    final String format;
    final boolean dimensions;
    final int qualityMax;
    final double qualityRmse;
    final long compactDecodes;
    final long temporaryRgba;
    final long promotions;
    final boolean reuse;
    final boolean lazy;

    CaseResult(boolean pass, boolean screenDraw, boolean rasterCountersZero, String format,
        boolean dimensions, int qualityMax, double qualityRmse, long compactDecodes,
        long temporaryRgba, long promotions, boolean reuse, boolean lazy) {
      this.pass = pass;
      this.screenDraw = screenDraw;
      this.rasterCountersZero = rasterCountersZero;
      this.format = format;
      this.dimensions = dimensions;
      this.qualityMax = qualityMax;
      this.qualityRmse = qualityRmse;
      this.compactDecodes = compactDecodes;
      this.temporaryRgba = temporaryRgba;
      this.promotions = promotions;
      this.reuse = reuse;
      this.lazy = lazy;
    }

    String describe() {
      return "pass=" + pass + ",lazy=" + lazy + ",format=" + format
          + ",dimensions=" + dimensions + ",qualityMax=" + qualityMax
          + ",qualityRmse=" + qualityRmse + ",compactDecodes=" + compactDecodes
          + ",temporaryRgba=" + temporaryRgba + ",promotions=" + promotions
          + ",reuse=" + reuse + ",screenDraw=" + screenDraw
          + ",rasterCountersZero=" + rasterCountersZero;
    }
  }
}
