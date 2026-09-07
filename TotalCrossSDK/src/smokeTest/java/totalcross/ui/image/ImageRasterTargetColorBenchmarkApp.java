// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Vm;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** macOS workload for target-color conversion controls. */
public class ImageRasterTargetColorBenchmarkApp extends MainWindow {
  private static final int DEFAULT_SAMPLES = 60;
  private static final int DRAWS_PER_SAMPLE = 1024;

  @Override
  public void initUI() {
    String scenario = ImageRasterBenchmarkSupport.argument(getCommandLine(), "scenario", "pre");
    String target = ImageRasterBenchmarkSupport.argument(getCommandLine(), "target", "rgba");
    int samples = ImageRasterBenchmarkSupport.integerArgument(getCommandLine(), "samples", DEFAULT_SAMPLES);
    int completedSamples = 0;
    String error = "";
    String expectedHash = null;
    try {
      ImageRasterBenchmarkSupport.require("rgba".equals(target) || "bgra".equals(target)
          || "rgb565".equals(target) || "translucent".equals(target), "invalid target");
      ImageRasterBenchmarkSupport.require(samples > 0 && samples <= 200, "invalid samples");
      ImageRasterBenchmarkSupport.configure(scenario,
          ImageOptimizationSettings.RASTER_TARGET_COLORTYPE_CONVERSION);
      Image source = Image.createLogical(100, 100, 2);
      fill(source);
      Image transformed = source.getSmoothScaledInstance(100, 100);
      if ("translucent".equals(target)) {
        transformed.alphaMask = 127;
      }
      Image destination = Image.createTestRaster(100, 100, 2, colorType(target));
      Graphics canvas = destination.getGraphics();
      ImageRasterBenchmarkSupport.require(canvas != null, "target graphics");
      for (int warmup = 0; warmup < 3; warmup++) {
        drawBatch(canvas, transformed);
      }
      Image.resetImageOperationAccountingForTest();
      for (int sample = 1; sample <= samples; sample++) {
        long start = Vm.getTimeStamp();
        drawBatch(canvas, transformed);
        long elapsed = Vm.getTimeStamp() - start;
        String hash = ImageRasterBenchmarkSupport.hashString(
            ImageRasterBenchmarkSupport.fullPixelHash(destination));
        if (expectedHash == null) {
          expectedHash = hash;
        } else {
          ImageRasterBenchmarkSupport.require(expectedHash.equals(hash), "target color hash drift");
        }
        System.out.println("sample=" + sample + ",elapsed_ms=" + elapsed + ",target=" + target
            + ",draws=" + DRAWS_PER_SAMPLE + ",pixel_hash=" + hash);
        System.out.flush();
        completedSamples = sample;
      }
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }
    boolean pass = ImageRasterBenchmarkSupport.finish("ImageRasterTargetColorBenchmarkApp", scenario,
        samples, completedSamples, "target=" + target + ",draws=" + DRAWS_PER_SAMPLE
            + ",pixel_hash=" + String.valueOf(expectedHash), error);
    exit(pass ? 0 : 1);
  }

  private static void fill(Image image) throws Exception {
    Graphics graphics = image.getGraphics();
    for (int y = 0; y < 100; y += 10) {
      for (int x = 0; x < 100; x += 10) {
        graphics.foreColor = 0xFF000000 | ((x * 23) & 0xFF) << 16
            | ((y * 29) & 0xFF) << 8 | ((x * 7 + y) & 0xFF);
        graphics.fillRect(x, y, Math.min(10, 100 - x), Math.min(10, 100 - y));
      }
    }
  }

  private static void drawBatch(Graphics canvas, Image image) {
    for (int i = 0; i < DRAWS_PER_SAMPLE; i++) {
      canvas.drawImage(image, 0, 0, false);
    }
  }

  private static int colorType(String target) {
    if ("bgra".equals(target)) {
      return NativeImageBacking.TEST_COLOR_BGRA8888;
    }
    if ("rgb565".equals(target)) {
      return NativeImageBacking.TEST_COLOR_RGB565;
    }
    return NativeImageBacking.TEST_COLOR_RGBA8888;
  }
}
