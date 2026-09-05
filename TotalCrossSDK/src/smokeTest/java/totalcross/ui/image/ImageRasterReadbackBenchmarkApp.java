// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.io.ByteArrayStream;
import totalcross.sys.Vm;
import totalcross.ui.MainWindow;

/** macOS benchmark workload for readback, encoding, and color materialization. */
public class ImageRasterReadbackBenchmarkApp extends MainWindow {
  private static final int DEFAULT_SAMPLES = 60;

  @Override
  public void initUI() {
    String scenario = ImageRasterBenchmarkSupport.argument(getCommandLine(), "scenario", "pre");
    String operation = ImageRasterBenchmarkSupport.argument(getCommandLine(), "operation", "pixels");
    int samples = ImageRasterBenchmarkSupport.integerArgument(getCommandLine(), "samples", DEFAULT_SAMPLES);
    int completedSamples = 0;
    String error = "";
    long totalOutputBytes = 0;
    int checksum = 0;

    try {
      ImageRasterBenchmarkSupport.require("pixels".equals(operation) || "encode".equals(operation)
          || "color".equals(operation), "operation must be pixels, encode, or color");
      ImageRasterBenchmarkSupport.require(samples > 0 && samples <= 200,
          "samples must be between 1 and 200");
      int targetFeature = "color".equals(operation)
          ? ImageOptimizationSettings.RASTER_DIRECT_COLOR_MATERIALIZATION
          : ImageOptimizationSettings.RASTER_ROW_READBACK;
      ImageRasterBenchmarkSupport.configure(scenario, targetFeature);
      Image.resetImageOperationAccountingForTest();
      byte[] encoded = ImageRasterBenchmarkSupport.resource("image-abi/lena1960.jpg");
      for (int warmup = 0; warmup < 3; warmup++) {
        runOperation(encoded, operation);
      }
      for (int sample = 1; sample <= samples; sample++) {
        long start = Vm.getTimeStamp();
        OperationResult result = runOperation(encoded, operation);
        long elapsed = Vm.getTimeStamp() - start;
        totalOutputBytes += result.outputBytes;
        checksum ^= result.checksum;
        System.out.println("sample=" + sample + ",elapsed_ms=" + elapsed + ",operation=" + operation
            + ",output_bytes=" + result.outputBytes + ",checksum=" + result.checksum
            + ",row_readbacks=" + Image.rowReadbackCountForTest()
            + ",full_readbacks=" + Image.fullReadbackCountForTest()
            + ",row_scratch_peak_bytes=" + Image.rowScratchPeakBytesForTest()
            + ",full_scratch_bytes=" + Image.fullScratchBytesForTest()
            + ",direct_color_materializations=" + Image.directColorMaterializationCountForTest());
        System.out.flush();
        completedSamples = sample;
      }
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean pass = ImageRasterBenchmarkSupport.finish("ImageRasterReadbackBenchmarkApp", scenario,
        samples, completedSamples, "operation=" + operation + ",total_output_bytes=" + totalOutputBytes
            + ",checksum=" + checksum
            + ",row_readbacks=" + Image.rowReadbackCountForTest()
            + ",full_readbacks=" + Image.fullReadbackCountForTest()
            + ",row_scratch_peak_bytes=" + Image.rowScratchPeakBytesForTest()
            + ",full_scratch_bytes=" + Image.fullScratchBytesForTest()
            + ",direct_color_materializations=" + Image.directColorMaterializationCountForTest(), error);
    exit(pass ? 0 : 1);
  }

  private static OperationResult runOperation(byte[] encoded, String operation) throws Exception {
    Image image = ImageRasterBenchmarkSupport.materialize(encoded);
    if ("pixels".equals(operation)) {
      int[] pixels = image.getPixels();
      return new OperationResult((long) pixels.length * 4, checksum(pixels));
    }
    if ("encode".equals(operation)) {
      ByteArrayStream stream = new ByteArrayStream(8192);
      image.createPng(stream);
      return new OperationResult(stream.getPos(), stream.getPos());
    }
    image.applyColor2(0x0090A0B0);
    int[] pixels = image.getPixels();
    return new OperationResult((long) pixels.length * 4, checksum(pixels));
  }

  private static int checksum(int[] pixels) {
    int result = 1;
    for (int i = 0; i < pixels.length; i += Math.max(1, pixels.length / 97)) {
      result = 31 * result + pixels[i];
    }
    return result;
  }

  private static final class OperationResult {
    final long outputBytes;
    final int checksum;

    OperationResult(long outputBytes, int checksum) {
      this.outputBytes = outputBytes;
      this.checksum = checksum;
    }
  }
}
