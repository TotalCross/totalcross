// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Vm;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** macOS benchmark workload for eligible and guarded opaque raster draws. */
public class ImageRasterOpaqueDrawBenchmarkApp extends MainWindow {
  private static final int DEFAULT_SAMPLES = 60;
  private static final int DRAWS_PER_SAMPLE = 8;

  @Override
  public void initUI() {
    String scenario = ImageRasterBenchmarkSupport.argument(getCommandLine(), "scenario", "pre");
    String format = ImageRasterBenchmarkSupport.argument(getCommandLine(), "format", "jpeg");
    int samples = ImageRasterBenchmarkSupport.integerArgument(getCommandLine(), "samples", DEFAULT_SAMPLES);
    int completedSamples = 0;
    String error = "";
    long totalDraws = 0;

    try {
      ImageRasterBenchmarkSupport.require("jpeg".equals(format) || "png".equals(format),
          "format must be jpeg or png");
      ImageRasterBenchmarkSupport.require(samples > 0 && samples <= 200,
          "samples must be between 1 and 200");
      ImageRasterBenchmarkSupport.configure(scenario, ImageOptimizationSettings.RASTER_OPAQUE_WRITE_PIXELS);
      Image.resetImageOperationAccountingForTest();
      byte[] encoded = "jpeg".equals(format)
          ? ImageRasterBenchmarkSupport.resource("image-abi/lena512.jpg")
          : ImageRasterBenchmarkSupport.opaquePng(600, 600);
      Image source = ImageRasterBenchmarkSupport.materialize(encoded);
      Image target = new Image(source.getPixelWidth(), source.getPixelHeight());
      Graphics canvas = target.getGraphics();
      ImageRasterBenchmarkSupport.require(canvas != null, "draw target graphics");
      runGuardCases(canvas, source);
      for (int warmup = 0; warmup < 3; warmup++) {
        drawBatch(canvas, source);
      }
      for (int sample = 1; sample <= samples; sample++) {
        long start = Vm.getTimeStamp();
        drawBatch(canvas, source);
        long elapsed = Vm.getTimeStamp() - start;
        totalDraws += DRAWS_PER_SAMPLE;
        System.out.println("sample=" + sample + ",elapsed_ms=" + elapsed + ",format=" + format
            + ",draws=" + DRAWS_PER_SAMPLE + ",width=" + source.getPixelWidth()
            + ",height=" + source.getPixelHeight()
            + ",write_pixels_attempts=" + NativeImageBacking.writePixelsAttemptsForTest()
            + ",write_pixels_hits=" + NativeImageBacking.writePixelsHitsForTest()
            + ",write_pixels_fallbacks=" + NativeImageBacking.writePixelsFallbacksForTest()
            + ",write_pixels_copied_bytes=" + NativeImageBacking.writePixelsCopiedBytesForTest());
        System.out.flush();
        completedSamples = sample;
      }
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean pass = ImageRasterBenchmarkSupport.finish("ImageRasterOpaqueDrawBenchmarkApp", scenario,
        samples, completedSamples, "format=" + format + ",total_draws=" + totalDraws
            + ",write_pixels_attempts=" + NativeImageBacking.writePixelsAttemptsForTest()
            + ",write_pixels_hits=" + NativeImageBacking.writePixelsHitsForTest()
            + ",write_pixels_fallbacks=" + NativeImageBacking.writePixelsFallbacksForTest()
            + ",write_pixels_copied_bytes=" + NativeImageBacking.writePixelsCopiedBytesForTest(), error);
    exit(pass ? 0 : 1);
  }

  private static void drawBatch(Graphics canvas, Image source) {
    for (int draw = 0; draw < DRAWS_PER_SAMPLE; draw++) {
      canvas.drawImage(source, 0, 0, false);
    }
  }

  private static void runGuardCases(Graphics canvas, Image source) throws Exception {
    int originalAlpha = source.alphaMask;
    source.alphaMask = 128;
    canvas.drawImage(source, 0, 0, false);
    source.alphaMask = originalAlpha;
    Image scaled = source.getSmoothScaledInstance(Math.max(1, source.getPixelWidth() / 2),
        Math.max(1, source.getPixelHeight() / 2));
    canvas.drawImage(scaled, 0, 0, false);
    Image rotated = source.getRotatedScaledInstance(100, 7, 0xFF000000);
    canvas.drawImage(rotated, 0, 0, false);
    canvas.copyImageRect(source, 0, 0, source.getPixelWidth() - 1,
        source.getPixelHeight() - 1, false);
  }
}
