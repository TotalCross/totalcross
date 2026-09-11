// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Vm;
import totalcross.ui.MainWindow;

/** macOS benchmark workload for fresh PNG and JPEG materialization. */
public class ImageRasterDecodeBenchmarkApp extends MainWindow {
  private static final int DEFAULT_SAMPLES = 60;
  private static final int PNG_BATCH = 10;
  private static final int JPEG_BATCH = 2;

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
      Image.resetImageOperationAccountingForTest();
      byte[] encoded = "png".equals(format)
          ? ImageRasterBenchmarkSupport.resource("images/lenna.png")
          : ImageRasterBenchmarkSupport.resource("image-abi/lena1960.jpg");
      int batch = "png".equals(format) ? PNG_BATCH : JPEG_BATCH;
      for (int warmup = 0; warmup < 3; warmup++) {
        decodeBatch(encoded, batch);
      }
      Image.resetImageOperationAccountingForTest();
      String expectedHash = null;
      for (int sample = 1; sample <= samples; sample++) {
        long start = Vm.getTimeStamp();
        Image image = decodeBatch(encoded, batch);
        long elapsed = Vm.getTimeStamp() - start;
        String pixelHash = ImageRasterBenchmarkSupport.hashString(
            ImageRasterBenchmarkSupport.fullPixelHash(image));
        if (expectedHash == null) {
          expectedHash = pixelHash;
        } else {
          ImageRasterBenchmarkSupport.require(expectedHash.equals(pixelHash), "decode hash drift");
        }
        totalBytes += (long) image.getPixelWidth() * image.getPixelHeight() * 4;
        System.out.println("sample=" + sample + ",elapsed_ms=" + elapsed + ",format=" + format
            + ",batch_repetitions=" + batch
            + ",width=" + image.getPixelWidth() + ",height=" + image.getPixelHeight()
            + ",decoded_bytes=" + ((long) image.getPixelWidth() * image.getPixelHeight() * 4)
            + ",pixel_hash=" + pixelHash
            + ",zero_copy=" + Image.zeroCopyDecodeCountForTest()
            + ",copied=" + Image.copiedDecodeCountForTest()
            + ",decode_copied_bytes=" + Image.decodeCopiedBytesForTest()
            + ",decode_final_buffer_bytes=" + Image.decodeFinalBufferBytesForTest());
        System.out.flush();
        completedSamples = sample;
      }
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean pass = ImageRasterBenchmarkSupport.finish("ImageRasterDecodeBenchmarkApp", scenario,
        samples, completedSamples, "format=" + format + ",total_decoded_bytes=" + totalBytes
            + ",zero_copy=" + Image.zeroCopyDecodeCountForTest()
            + ",copied=" + Image.copiedDecodeCountForTest()
            + ",decode_copied_bytes=" + Image.decodeCopiedBytesForTest()
            + ",decode_final_buffer_bytes=" + Image.decodeFinalBufferBytesForTest(), error);
    exit(pass ? 0 : 1);
  }

  private static Image decodeBatch(byte[] encoded, int batch) throws Exception {
    Image last = null;
    for (int i = 0; i < batch; i++) {
      last = ImageRasterBenchmarkSupport.materialize(encoded);
    }
    return last;
  }
}
