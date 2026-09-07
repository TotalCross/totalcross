// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Vm;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** macOS workload for exact physical identity and guarded raster draws. */
public class ImageRasterPhysicalIdentityBenchmarkApp extends MainWindow {
  private static final int DEFAULT_SAMPLES = 60;
  private static final int DRAWS_PER_SAMPLE = 1024;

  @Override
  public void initUI() {
    String scenario = ImageRasterBenchmarkSupport.argument(getCommandLine(), "scenario", "pre");
    String testCase = ImageRasterBenchmarkSupport.argument(getCommandLine(), "case", "identity");
    String filter = ImageRasterBenchmarkSupport.argument(getCommandLine(), "filter", "smooth");
    int samples = ImageRasterBenchmarkSupport.integerArgument(getCommandLine(), "samples", DEFAULT_SAMPLES);
    int completedSamples = 0;
    String error = "";
    String expectedHash = null;
    boolean guardPass = true;
    try {
      ImageRasterBenchmarkSupport.require("identity".equals(testCase) || "nonidentity".equals(testCase)
          || "guards".equals(testCase), "case must be identity, nonidentity, or guards");
      ImageRasterBenchmarkSupport.require("nearest".equals(filter) || "smooth".equals(filter),
          "filter must be nearest or smooth");
      ImageRasterBenchmarkSupport.require(samples > 0 && samples <= 200, "invalid samples");
      ImageRasterBenchmarkSupport.configure(scenario,
          ImageOptimizationSettings.RASTER_PHYSICAL_IDENTITY_FOLDING);
      Image source = sourceFor(testCase);
      Image transformed = transform(source, filter);
      Image target = Image.createLogical(100, 100, 2);
      Graphics canvas = target.getGraphics();
      ImageRasterBenchmarkSupport.require(canvas != null, "target graphics");
      if ("guards".equals(testCase)) {
        Image.resetImageOperationAccountingForTest();
        runGuardCases(canvas, transformed);
        boolean exactGeometryPass = runExactGeometryCases(canvas);
        guardPass = exactGeometryPass;
        ImageRasterBenchmarkSupport.require(guardPass, "identity guard counters");
      }
      for (int warmup = 0; warmup < 3; warmup++) {
        drawBatch(canvas, transformed);
      }
      Image.resetImageOperationAccountingForTest();
      for (int sample = 1; sample <= samples; sample++) {
        long start = Vm.getTimeStamp();
        drawBatch(canvas, transformed);
        long elapsed = Vm.getTimeStamp() - start;
        String hash = ImageRasterBenchmarkSupport.hashString(
            ImageRasterBenchmarkSupport.fullPixelHash(target));
        if (expectedHash == null) {
          expectedHash = hash;
        } else {
          ImageRasterBenchmarkSupport.require(expectedHash.equals(hash), "identity hash drift");
        }
        long expectedDraws = (long) sample * DRAWS_PER_SAMPLE;
        if ("post-enabled".equals(scenario)) {
          if ("identity".equals(testCase)) {
            ImageRasterBenchmarkSupport.require(
                NativeImageBacking.physicalIdentityAttemptsForTest() == expectedDraws
                    && NativeImageBacking.physicalIdentityHitsForTest() == expectedDraws
                    && NativeImageBacking.physicalIdentityFallbacksForTest() == 0
                    && NativeImageBacking.physicalIdentityResamplesAvoidedForTest() == expectedDraws,
                "identity counters");
          } else if ("nonidentity".equals(testCase)) {
            ImageRasterBenchmarkSupport.require(
                NativeImageBacking.physicalIdentityAttemptsForTest() == expectedDraws
                    && NativeImageBacking.physicalIdentityHitsForTest() == 0
                    && NativeImageBacking.physicalIdentityFallbacksForTest() == expectedDraws
                    && NativeImageBacking.physicalIdentityResamplesAvoidedForTest() == 0,
                "nonidentity counters");
          }
        } else {
          ImageRasterBenchmarkSupport.require(
              NativeImageBacking.physicalIdentityAttemptsForTest() == 0
                  && NativeImageBacking.physicalIdentityHitsForTest() == 0
                  && NativeImageBacking.physicalIdentityFallbacksForTest() == 0
                  && NativeImageBacking.physicalIdentityResamplesAvoidedForTest() == 0,
              "disabled identity counters");
        }
        System.out.println("sample=" + sample + ",elapsed_ms=" + elapsed + ",case=" + testCase
            + ",filter=" + filter + ",draws=" + DRAWS_PER_SAMPLE + ",pixel_hash=" + hash
            + ",source_physical=" + source.getPixelWidth() + "x" + source.getPixelHeight()
            + ",target_physical=" + target.getPixelWidth() + "x" + target.getPixelHeight()
            + ",physical_identity_attempts=" + NativeImageBacking.physicalIdentityAttemptsForTest()
            + ",physical_identity_hits=" + NativeImageBacking.physicalIdentityHitsForTest()
            + ",physical_identity_fallbacks=" + NativeImageBacking.physicalIdentityFallbacksForTest()
            + ",physical_identity_resamples_avoided="
            + NativeImageBacking.physicalIdentityResamplesAvoidedForTest());
        System.out.flush();
        completedSamples = sample;
      }
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }
    boolean pass = ImageRasterBenchmarkSupport.finish("ImageRasterPhysicalIdentityBenchmarkApp", scenario,
        samples, completedSamples, "case=" + testCase + ",filter=" + filter + ",draws=" + DRAWS_PER_SAMPLE
            + ",pixel_hash=" + String.valueOf(expectedHash)
            + ",guard_pass=" + guardPass
            + ",physical_identity_attempts=" + NativeImageBacking.physicalIdentityAttemptsForTest()
            + ",physical_identity_hits=" + NativeImageBacking.physicalIdentityHitsForTest()
            + ",physical_identity_fallbacks=" + NativeImageBacking.physicalIdentityFallbacksForTest()
            + ",physical_identity_resamples_avoided="
            + NativeImageBacking.physicalIdentityResamplesAvoidedForTest(), error);
    exit(pass ? 0 : 1);
  }

  private static Image sourceFor(String testCase) throws Exception {
    int logical = "nonidentity".equals(testCase) ? 200 : 100;
    Image source = Image.createLogical(logical, logical, 2);
    Graphics graphics = source.getGraphics();
    for (int y = 0; y < logical; y += 8) {
      for (int x = 0; x < logical; x += 8) {
        graphics.foreColor = 0xFF000000 | ((x * 17) & 0xFF) << 16
            | ((y * 19) & 0xFF) << 8 | ((x + y) & 0xFF);
        graphics.fillRect(x, y, Math.min(8, logical - x), Math.min(8, logical - y));
      }
    }
    return source;
  }

  private static Image transform(Image source, String filter) throws Exception {
    return "nearest".equals(filter) ? source.getScaledInstance(100, 100)
        : source.getSmoothScaledInstance(100, 100);
  }

  private static void drawBatch(Graphics canvas, Image image) {
    for (int i = 0; i < DRAWS_PER_SAMPLE; i++) {
      canvas.drawImage(image, 0, 0, false);
    }
  }

  private static void runGuardCases(Graphics canvas, Image transformed) throws Exception {
    int alpha = transformed.alphaMask;
    transformed.alphaMask = 127;
    boolean alphaPass = fallbackObserved(canvas, transformed, true);
    transformed.alphaMask = alpha;
    double hwScale = transformed.hwScaleW;
    transformed.hwScaleW = 0.75;
    boolean hwScalePass = noIdentityHit(canvas, transformed);
    transformed.hwScaleW = hwScale;
    ImageRasterBenchmarkSupport.require(alphaPass && hwScalePass,
        "alpha=" + alphaPass + ",hwScale=" + hwScalePass);
  }

  private static boolean fallbackObserved(Graphics canvas, Image image, boolean doClip) {
    Image.resetImageOperationAccountingForTest();
    canvas.drawImage(image, 0, 0, doClip);
    long attempts = NativeImageBacking.physicalIdentityAttemptsForTest();
    long hits = NativeImageBacking.physicalIdentityHitsForTest();
    long fallbacks = NativeImageBacking.physicalIdentityFallbacksForTest();
    long avoided = NativeImageBacking.physicalIdentityResamplesAvoidedForTest();
    return attempts == 1 && hits == 0 && fallbacks == 1 && avoided == 0;
  }

  private static boolean noIdentityHit(Graphics canvas, Image image) {
    Image.resetImageOperationAccountingForTest();
    canvas.drawImage(image, 0, 0, false);
    return NativeImageBacking.physicalIdentityHitsForTest() == 0
        && NativeImageBacking.physicalIdentityResamplesAvoidedForTest() == 0;
  }

  private static boolean runExactGeometryCases(Graphics canvas) throws Exception {
    Image crop = Image.createLogical(100, 100, 2).getClippedInstance(0, 0, 100, 100);
    Image.resetImageOperationAccountingForTest();
    canvas.drawImage(crop, 0, 0, false);
    boolean cropPass = NativeImageBacking.physicalIdentityAttemptsForTest() == 1
        && NativeImageBacking.physicalIdentityHitsForTest() == 1
        && NativeImageBacking.physicalIdentityFallbacksForTest() == 0
        && NativeImageBacking.physicalIdentityResamplesAvoidedForTest() == 1;

    Image frames = Image.createLogical(200, 100, 2);
    frames.setFrameCount(2);
    Image frame = frames.getFrameInstance(1);
    Image.resetImageOperationAccountingForTest();
    canvas.drawImage(frame, 0, 0, false);
    boolean framePass = NativeImageBacking.physicalIdentityAttemptsForTest() == 1
        && NativeImageBacking.physicalIdentityHitsForTest() == 1
        && NativeImageBacking.physicalIdentityFallbacksForTest() == 0
        && NativeImageBacking.physicalIdentityResamplesAvoidedForTest() == 1;
    return cropPass && framePass;
  }
}
