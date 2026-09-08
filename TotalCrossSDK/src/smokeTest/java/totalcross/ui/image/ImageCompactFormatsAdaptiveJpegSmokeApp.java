// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.ui.MainWindow;

/** Focused lazy TARGET_DECODE coverage for compact JPEG backings. */
public class ImageCompactFormatsAdaptiveJpegSmokeApp extends MainWindow {
  private static final int SOURCE_SIZE = 512;
  private static final int[] DENOMINATORS = { 2, 4, 8 };

  @Override
  public void initUI() {
    boolean rgb565 = false;
    boolean gray8 = false;
    String error = "";
    try {
      ImageRasterBenchmarkSupport.require(
          ImageCompactFormatsBenchmarkSupport.formatProbeAvailable(),
          "native compact format probe");
      ImageCompactFormatsBenchmarkSupport.Fixture[] fixtures =
          ImageCompactFormatsBenchmarkSupport.fixtures();
      rgb565 = checkFixture(fixtures[0], ImageCompactFormatsBenchmarkSupport.RGB565);
      gray8 = checkFixture(fixtures[2], ImageCompactFormatsBenchmarkSupport.GRAY8);
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }
    boolean pass = error.length() == 0 && rgb565 && gray8;
    System.out.println("fixture=ImageCompactFormatsAdaptiveJpegSmokeApp,rgb565="
        + rgb565 + ",gray8=" + gray8 + ",overallPass=" + pass
        + (error.length() == 0 ? "" : ",error=" + error));
    System.out.flush();
    exit(pass ? 0 : 1);
  }

  private static boolean checkFixture(
      ImageCompactFormatsBenchmarkSupport.Fixture fixture, String expectedFormat)
      throws Exception {
    for (int denominator : DENOMINATORS) {
      int expectedDimension = (SOURCE_SIZE + denominator - 1) / denominator;
      int[] referencePixels = fullDecodeReference(fixture.bytes, expectedDimension);

      ImageCompactFormatsBenchmarkSupport.configure("post-enabled",
          "milestone9-full-stack", true);
      Image.resetImageOperationAccountingForTest();
      Image base = new Image(fixture.bytes, fixture.bytes.length);
      Object root = base.pipelineForSmoke().root();
      ImageRasterBenchmarkSupport.require(root instanceof EncodedImageSource,
          "lazy JPEG root");
      EncodedImageSource source = (EncodedImageSource) root;
      Image scaled = base.getSmoothScaledInstance(expectedDimension, expectedDimension);
      Image actual = scaled.resolveForDrawing(1);
      int targeted = Image.targetedDecodeInvocationCountForTest();
      long expectedBytes = (long) expectedDimension * expectedDimension
          * (ImageCompactFormatsBenchmarkSupport.GRAY8.equals(expectedFormat) ? 1 : 2);
      ImageCompactFormatsBenchmarkSupport.Quality quality =
          ImageCompactFormatsBenchmarkSupport.quality(actual.getPixels(), referencePixels);
      String actualHash = ImageRasterBenchmarkSupport.hashString(
          ImageRasterBenchmarkSupport.fullPixelHash(actual));

      Image cachedPipeline = base.getSmoothScaledInstance(expectedDimension, expectedDimension);
      Image cached = cachedPipeline.resolveForDrawing(1);
      String cachedHash = ImageRasterBenchmarkSupport.hashString(
          ImageRasterBenchmarkSupport.fullPixelHash(cached));
      boolean valid = actual.getPixelWidth() == expectedDimension
          && actual.getPixelHeight() == expectedDimension
          && actual.getWidth() == SOURCE_SIZE
          && actual.getHeight() == SOURCE_SIZE
          && Math.abs(actual.getContentScale() - 1.0 / denominator) < 0.000001
          && expectedFormat.equals(ImageCompactFormatsBenchmarkSupport.format(actual))
          && source.decodedDenominator() == denominator
          && source.decodedGeneration() == 1
          && source.decodedBackingForReuse(denominator) != null
          && targeted == 1
          && expectedBytes == Image.decodeFinalBufferBytesForTest()
          && NativeImageBacking.temporaryRgbaDecodeBytesForTest() == 0
          && NativeImageBacking.promotionAttemptsForTest() == 0
          && actualHash.equals(cachedHash)
          && quality.maxError <= 32
          && quality.rmse <= 9.0;
      if (!valid) {
        System.out.println("adaptiveLazyFailure=format=" + expectedFormat
            + ",denominator=" + denominator + ",pixel=" + actual.getPixelWidth()
            + "x" + actual.getPixelHeight() + ",logical=" + actual.getWidth()
            + "x" + actual.getHeight() + ",scale=" + actual.getContentScale()
            + ",sourceDenominator=" + source.decodedDenominator()
            + ",generation=" + source.decodedGeneration() + ",targeted=" + targeted
            + ",finalBytes=" + Image.decodeFinalBufferBytesForTest()
            + ",expectedBytes=" + expectedBytes + ",temp="
            + NativeImageBacking.temporaryRgbaDecodeBytesForTest() + ",promotions="
            + NativeImageBacking.promotionAttemptsForTest() + ",qualityMax="
            + quality.maxError + ",qualityRmse=" + quality.rmse + ",hashes="
            + actualHash + "/" + cachedHash);
        return false;
      }
    }
    return true;
  }

  private static int[] fullDecodeReference(byte[] encoded, int targetDimension)
      throws Exception {
    ImageCompactFormatsBenchmarkSupport.configureExplicit("pre", new int[0]);
    Image reference = new Image(encoded, encoded.length)
        .getSmoothScaledInstance(targetDimension, targetDimension)
        .resolveForDrawing(1);
    return reference.getPixels();
  }
}
