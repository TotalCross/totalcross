// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Vm;
import totalcross.ui.MainWindow;

/** macOS benchmark workload for fresh PNG and JPEG materialization. */
public class ImageRasterDecodeBenchmarkApp extends MainWindow {
  private static final int DEFAULT_SAMPLES = 60;

  @Override
  public void initUI() {
    String scenario = ImageRasterBenchmarkSupport.argument(getCommandLine(), "scenario", "pre");
    String format = ImageRasterBenchmarkSupport.argument(getCommandLine(), "format", "png");
    int samples = ImageRasterBenchmarkSupport.integerArgument(getCommandLine(), "samples", DEFAULT_SAMPLES);
    int completedSamples = 0;
    String error = "";
    long totalBytes = 0;

    try {
      ImageRasterBenchmarkSupport.require("png".equals(format) || "jpeg".equals(format),
          "format must be png or jpeg");
      ImageRasterBenchmarkSupport.require(samples > 0 && samples <= 200,
          "samples must be between 1 and 200");
      ImageRasterBenchmarkSupport.configure(scenario, ImageOptimizationSettings.DECODE_ZERO_COPY);
      byte[] encoded = "png".equals(format)
          ? ImageRasterBenchmarkSupport.resource("images/lenna.png")
          : ImageRasterBenchmarkSupport.resource("image-abi/lena1960.jpg");
      for (int warmup = 0; warmup < 3; warmup++) {
        ImageRasterBenchmarkSupport.materialize(encoded);
      }
      for (int sample = 1; sample <= samples; sample++) {
        long start = Vm.getTimeStamp();
        Image image = ImageRasterBenchmarkSupport.materialize(encoded);
        long elapsed = Vm.getTimeStamp() - start;
        totalBytes += (long) image.getPixelWidth() * image.getPixelHeight() * 4;
        System.out.println("sample=" + sample + ",elapsed_ms=" + elapsed + ",format=" + format
            + ",width=" + image.getPixelWidth() + ",height=" + image.getPixelHeight()
            + ",decoded_bytes=" + ((long) image.getPixelWidth() * image.getPixelHeight() * 4));
        System.out.flush();
        completedSamples = sample;
      }
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean pass = ImageRasterBenchmarkSupport.finish("ImageRasterDecodeBenchmarkApp", scenario,
        samples, completedSamples, "format=" + format + ",total_decoded_bytes=" + totalBytes, error);
    exit(pass ? 0 : 1);
  }
}
