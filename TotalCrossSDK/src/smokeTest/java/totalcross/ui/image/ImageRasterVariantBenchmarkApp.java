// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Vm;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** macOS workload for repeated transformed-raster use and invalidation. */
public class ImageRasterVariantBenchmarkApp extends MainWindow {
  private static final int DEFAULT_SAMPLES = 60;
  private static final int DRAWS_PER_SAMPLE = 256;

  @Override
  public void initUI() {
    String scenario = ImageRasterBenchmarkSupport.argument(getCommandLine(), "scenario", "pre");
    String testCase = ImageRasterBenchmarkSupport.argument(getCommandLine(), "case", "repeat");
    int samples = ImageRasterBenchmarkSupport.integerArgument(getCommandLine(), "samples", DEFAULT_SAMPLES);
    int completedSamples = 0;
    String error = "";
    String expectedHash = null;
    try {
      ImageRasterBenchmarkSupport.require("first".equals(testCase) || "repeat".equals(testCase)
          || "mutation".equals(testCase) || "size".equals(testCase), "invalid case");
      ImageRasterBenchmarkSupport.require(samples > 0 && samples <= 200, "invalid samples");
      ImageRasterBenchmarkSupport.configure(scenario,
          ImageOptimizationSettings.RASTER_PHYSICAL_VARIANT_CACHE);
      Image source = source();
      Image repeated = source.getSmoothScaledInstance(100, 100);
      Image alternate = source.getSmoothScaledInstance(96, 96);
      Image destination = Image.createLogical(100, 100, 2);
      Image alternateDestination = Image.createLogical(96, 96, 2);
      Graphics canvas = destination.getGraphics();
      Graphics alternateCanvas = alternateDestination.getGraphics();
      for (int warmup = 0; warmup < 3; warmup++) {
        runCase(testCase, source, repeated, alternate, canvas, alternateCanvas);
      }
      Image.resetImageOperationAccountingForTest();
      for (int sample = 1; sample <= samples; sample++) {
        long start = Vm.getTimeStamp();
        runCase(testCase, source, repeated, alternate, canvas, alternateCanvas);
        long elapsed = Vm.getTimeStamp() - start;
        String hash = ImageRasterBenchmarkSupport.hashString(
            ImageRasterBenchmarkSupport.fullPixelHash(destination));
        if (expectedHash == null) {
          expectedHash = hash;
        } else {
          ImageRasterBenchmarkSupport.require(expectedHash.equals(hash), "variant hash drift");
        }
        System.out.println("sample=" + sample + ",elapsed_ms=" + elapsed + ",case=" + testCase
            + ",draws=" + DRAWS_PER_SAMPLE + ",pixel_hash=" + hash);
        System.out.flush();
        completedSamples = sample;
      }
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }
    boolean pass = ImageRasterBenchmarkSupport.finish("ImageRasterVariantBenchmarkApp", scenario,
        samples, completedSamples, "case=" + testCase + ",draws=" + DRAWS_PER_SAMPLE
            + ",pixel_hash=" + String.valueOf(expectedHash), error);
    exit(pass ? 0 : 1);
  }

  private static Image source() throws Exception {
    Image image = Image.createLogical(200, 200, 2);
    Graphics graphics = image.getGraphics();
    for (int y = 0; y < 200; y += 16) {
      for (int x = 0; x < 200; x += 16) {
        graphics.foreColor = 0xFF000000 | ((x * 11) & 0xFF) << 16
            | ((y * 13) & 0xFF) << 8 | ((x + y * 3) & 0xFF);
        graphics.fillRect(x, y, Math.min(16, 200 - x), Math.min(16, 200 - y));
      }
    }
    return image;
  }

  private static void runCase(String testCase, Image source, Image repeated, Image alternate,
      Graphics canvas, Graphics alternateCanvas) {
    if ("first".equals(testCase)) {
      for (int i = 0; i < DRAWS_PER_SAMPLE; i++) {
        Image fresh = source.getSmoothScaledInstance(100, 100);
        canvas.drawImage(fresh, 0, 0, false);
      }
      return;
    }
    if ("size".equals(testCase)) {
      for (int i = 0; i < DRAWS_PER_SAMPLE; i++) {
        Graphics target = (i & 1) == 0 ? canvas : alternateCanvas;
        target.drawImage((i & 1) == 0 ? repeated : alternate, 0, 0, false);
      }
      return;
    }
    for (int i = 0; i < DRAWS_PER_SAMPLE; i++) {
      canvas.drawImage(repeated, 0, 0, false);
      if ("mutation".equals(testCase) && i == DRAWS_PER_SAMPLE / 2) {
        source.applyColor2(0x0080A0C0);
      }
    }
  }
}
