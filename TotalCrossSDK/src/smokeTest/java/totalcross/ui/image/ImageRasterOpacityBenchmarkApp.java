// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Vm;
import totalcross.ui.MainWindow;

/** macOS benchmark workload for source opacity classification. */
public class ImageRasterOpacityBenchmarkApp extends MainWindow {
  private static final int DEFAULT_SAMPLES = 60;

  @Override
  public void initUI() {
    String scenario = ImageRasterBenchmarkSupport.argument(getCommandLine(), "scenario", "pre");
    String kind = ImageRasterBenchmarkSupport.argument(getCommandLine(), "kind", "jpeg");
    int samples = ImageRasterBenchmarkSupport.integerArgument(getCommandLine(), "samples", DEFAULT_SAMPLES);
    int completedSamples = 0;
    String error = "";
    long totalPixels = 0;

    try {
      ImageRasterBenchmarkSupport.require("jpeg".equals(kind) || "png-rgb".equals(kind)
          || "png-alpha-opaque".equals(kind) || "png-alpha-transparent".equals(kind),
          "invalid opacity fixture kind");
      ImageRasterBenchmarkSupport.require(samples > 0 && samples <= 200,
          "samples must be between 1 and 200");
      ImageRasterBenchmarkSupport.configure(scenario, ImageOptimizationSettings.RASTER_OPACITY_METADATA);
      byte[] encoded = fixture(kind);
      for (int warmup = 0; warmup < 3; warmup++) {
        ImageRasterBenchmarkSupport.materialize(encoded);
      }
      for (int sample = 1; sample <= samples; sample++) {
        long start = Vm.getTimeStamp();
        Image image = ImageRasterBenchmarkSupport.materialize(encoded);
        long elapsed = Vm.getTimeStamp() - start;
        long pixels = (long) image.getPixelWidth() * image.getPixelHeight();
        totalPixels += pixels;
        System.out.println("sample=" + sample + ",elapsed_ms=" + elapsed + ",kind=" + kind
            + ",width=" + image.getPixelWidth() + ",height=" + image.getPixelHeight()
            + ",pixels=" + pixels);
        System.out.flush();
        completedSamples = sample;
      }
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean pass = ImageRasterBenchmarkSupport.finish("ImageRasterOpacityBenchmarkApp", scenario,
        samples, completedSamples, "kind=" + kind + ",total_pixels=" + totalPixels, error);
    exit(pass ? 0 : 1);
  }

  private static byte[] fixture(String kind) throws Exception {
    if ("jpeg".equals(kind)) {
      return ImageRasterBenchmarkSupport.resource("image-abi/lena512.jpg");
    }
    if ("png-rgb".equals(kind)) {
      return ImageRasterBenchmarkSupport.resource("images/lenna_full.png");
    }
    if ("png-alpha-transparent".equals(kind)) {
      return ImageRasterBenchmarkSupport.resource("image-abi/tiny.png");
    }
    return ImageRasterBenchmarkSupport.opaquePng(600, 600);
  }
}
