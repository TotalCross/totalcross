// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Settings;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** Final-stack compact backing, geometry, observer, and adaptive JPEG smoke. */
public class ImageCompactFormatsFinalStackSmokeApp extends MainWindow {
  private static final int SOURCE_SIZE = 512;
  private static final int VARIANT_SIZE = 100;
  private static final String RGB565_JPEG = "image-opt-phase3/rgb565-source.jpg";
  private static final String GRAY8_JPEG = "image-opt-phase3/gray8-source.jpg";

  @Override
  public void initUI() {
    boolean physicalIdentity = false;
    boolean physicalVariant = false;
    boolean targetColorPhysicalVariant = false;
    boolean invalidation = false;
    boolean observers = false;
    boolean writePixels = false;
    boolean adaptiveJpeg = false;
    boolean screenDraw = false;
    String error = "";
    try {
      ImageCompactFormatsBenchmarkSupport.configure("post-enabled", "milestone9-full-stack", true);
      ImageCompactFormatsBenchmarkSupport.Fixture[] fixtures =
          ImageCompactFormatsBenchmarkSupport.fixtures();
      ImageRasterBenchmarkSupport.require(
          ImageCompactFormatsBenchmarkSupport.formatProbeAvailable(), "native compact format probe");
      physicalIdentity = checkPhysicalIdentity(fixtures[1]);
      physicalVariant = checkPhysicalVariant(fixtures[1]);
      targetColorPhysicalVariant = checkTargetColorPhysicalVariant(fixtures[1]);
      invalidation = checkInvalidation(fixtures[1]);
      observers = checkObservers(fixtures[1]);
      writePixels = checkWritePixels(fixtures[1], fixtures[3], fixtures[4]);
      adaptiveJpeg = checkAdaptiveJpeg(fixtures[0], fixtures[2]);
      screenDraw = checkScreenDraw(fixtures[0], fixtures[2], fixtures[4]);
      System.out.println("screenDrawCounters=writePixels="
          + NativeImageBacking.writePixelsAttemptsForTest() + "/"
          + NativeImageBacking.writePixelsHitsForTest() + "/"
          + NativeImageBacking.writePixelsFallbacksForTest() + ",targetColor="
          + NativeImageBacking.targetColorAttemptsForTest() + "/"
          + NativeImageBacking.targetColorMaterializationsForTest() + "/"
          + NativeImageBacking.targetColorHitsForTest() + ",physicalVariant="
          + NativeImageBacking.physicalVariantLookupsForTest() + "/"
          + NativeImageBacking.physicalVariantMaterializationsForTest() + "/"
          + NativeImageBacking.physicalVariantHitsForTest() + ",physicalIdentity="
          + NativeImageBacking.physicalIdentityAttemptsForTest() + "/"
          + NativeImageBacking.physicalIdentityHitsForTest() + ",promotions="
          + NativeImageBacking.promotionAttemptsForTest() + ",temporaryRgba="
          + NativeImageBacking.temporaryRgbaDecodeBytesForTest());
      ImageRasterBenchmarkSupport.require(physicalIdentity && physicalVariant
          && targetColorPhysicalVariant && invalidation && observers && writePixels && adaptiveJpeg
          && screenDraw,
          "final compact stack smoke");
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }
    boolean pass = error.length() == 0 && physicalIdentity && physicalVariant
        && targetColorPhysicalVariant && invalidation && observers && writePixels && adaptiveJpeg
        && screenDraw;
    System.out.println("fixture=ImageCompactFormatsFinalStackSmokeApp,physicalIdentity="
        + physicalIdentity + ",physicalVariant=" + physicalVariant
        + ",targetColorPhysicalVariant=" + targetColorPhysicalVariant
        + ",invalidation=" + invalidation + ",observers=" + observers
        + ",writePixels=" + writePixels + ",adaptiveJpeg=" + adaptiveJpeg
        + ",screenDraw=" + screenDraw
        + ",overallPass=" + pass + (error.length() == 0 ? "" : ",error=" + error));
    System.out.flush();
    exit(pass ? 0 : 1);
  }

  private static boolean checkPhysicalIdentity(
      ImageCompactFormatsBenchmarkSupport.Fixture fixture) throws Exception {
    Image source = ImageCompactFormatsBenchmarkSupport.materialize(fixture.bytes);
    int[] sourcePixels = source.getPixels();
    Image identity = source.getClippedInstance(0, 0, SOURCE_SIZE, SOURCE_SIZE);
    Image target = Image.createLogical(SOURCE_SIZE, SOURCE_SIZE, 1);
    Graphics canvas = requireGraphics(target);
    Image.resetImageOperationAccountingForTest();
    canvas.drawImage(identity, 0, 0, false);
    boolean counters = NativeImageBacking.physicalIdentityAttemptsForTest() == 1
        && NativeImageBacking.physicalIdentityHitsForTest() == 1
        && NativeImageBacking.physicalIdentityResamplesAvoidedForTest() == 1
        && NativeImageBacking.physicalVariantMaterializationsForTest() == 0
        && NativeImageBacking.physicalVariantLookupsForTest() == 0;
    return counters && ImageCompactFormatsBenchmarkSupport.RGB565.equals(
        ImageCompactFormatsBenchmarkSupport.format(source))
        && samePixels(sourcePixels, target.getPixels())
        && NativeImageBacking.promotionAttemptsForTest() == 0;
  }

  private static boolean checkPhysicalVariant(
      ImageCompactFormatsBenchmarkSupport.Fixture fixture) throws Exception {
    Image source = ImageCompactFormatsBenchmarkSupport.materialize(fixture.bytes);
    Image transformed = source.getSmoothScaledInstance(VARIANT_SIZE, VARIANT_SIZE);
    Image target = Image.createLogical(VARIANT_SIZE, VARIANT_SIZE, 2);
    Graphics canvas = requireGraphics(target);
    Image.resetImageOperationAccountingForTest();
    drawBatch(canvas, transformed, 3);
    String format = ImageCompactFormatsBenchmarkSupport.format(source);
    return ImageCompactFormatsBenchmarkSupport.RGB565.equals(format)
        && NativeImageBacking.physicalVariantLookupsForTest() == 3
        && NativeImageBacking.physicalVariantHitsForTest() == 1
        && NativeImageBacking.physicalVariantMissesForTest() == 2
        && NativeImageBacking.physicalVariantMaterializationsForTest() == 1
        && NativeImageBacking.physicalVariantEvictionsForTest() == 0
        && NativeImageBacking.physicalVariantBytesForTest() == 160000
        && NativeImageBacking.promotionAttemptsForTest() == 0
        && ImageCompactFormatsBenchmarkSupport.format(source).equals(format)
        && ImageRasterBenchmarkSupport.fullPixelHash(target) != 0;
  }

  private static boolean checkTargetColorPhysicalVariant(
      ImageCompactFormatsBenchmarkSupport.Fixture fixture) throws Exception {
    Image source = ImageCompactFormatsBenchmarkSupport.materialize(fixture.bytes);
    Image transformed = source.getSmoothScaledInstance(VARIANT_SIZE, VARIANT_SIZE);
    Image target = Image.createTestRaster(VARIANT_SIZE, VARIANT_SIZE, 2,
        NativeImageBacking.TEST_COLOR_RGB565);
    Graphics canvas = requireGraphics(target);
    Image.resetImageOperationAccountingForTest();
    drawBatch(canvas, transformed, 3);
    return ImageCompactFormatsBenchmarkSupport.RGB565.equals(
            ImageCompactFormatsBenchmarkSupport.format(source))
        && NativeImageBacking.physicalVariantLookupsForTest() == 3
        && NativeImageBacking.physicalVariantHitsForTest() == 1
        && NativeImageBacking.physicalVariantMissesForTest() == 2
        && NativeImageBacking.physicalVariantMaterializationsForTest() == 1
        && NativeImageBacking.physicalVariantBytesForTest() == 80000
        && NativeImageBacking.targetColorAttemptsForTest() == 0
        && NativeImageBacking.targetColorMaterializationsForTest() == 0
        && NativeImageBacking.targetColorHitsForTest() == 0
        && NativeImageBacking.targetColorConvertedBytesForTest() == 0
        && NativeImageBacking.promotionAttemptsForTest() == 0
        && ImageRasterBenchmarkSupport.fullPixelHash(target) != 0;
  }

  private static boolean checkInvalidation(
      ImageCompactFormatsBenchmarkSupport.Fixture fixture) throws Exception {
    Image source = ImageCompactFormatsBenchmarkSupport.materialize(fixture.bytes);
    Image transformed = source.getSmoothScaledInstance(VARIANT_SIZE, VARIANT_SIZE);
    Image target = Image.createLogical(VARIANT_SIZE, VARIANT_SIZE, 2);
    Graphics canvas = requireGraphics(target);
    Image.resetImageOperationAccountingForTest();
    drawBatch(canvas, transformed, 3);
    boolean beforeMutation = NativeImageBacking.physicalVariantMaterializationsForTest() == 1
        && NativeImageBacking.physicalVariantHitsForTest() == 1;
    ImageRasterBenchmarkSupport.mutateDeferredRootForTest(transformed,
        target.getContentScale());
    boolean promoted = NativeImageBacking.promotionAttemptsForTest() == 1
        && NativeImageBacking.promotionSuccessesForTest() == 1;
    Image.resetImageOperationAccountingForTest();
    drawBatch(canvas, transformed, 3);
    return beforeMutation && promoted
        && NativeImageBacking.physicalVariantLookupsForTest() == 3
        && NativeImageBacking.physicalVariantHitsForTest() == 1
        && NativeImageBacking.physicalVariantMissesForTest() == 2
        && NativeImageBacking.physicalVariantMaterializationsForTest() == 1
        && NativeImageBacking.physicalVariantEvictionsForTest() == 0
        && NativeImageBacking.physicalVariantBytesForTest() == 160000
        && NativeImageBacking.promotionAttemptsForTest() == 0;
  }

  private static boolean checkObservers(
      ImageCompactFormatsBenchmarkSupport.Fixture fixture) throws Exception {
    Image source = ImageCompactFormatsBenchmarkSupport.materialize(fixture.bytes);
    Image target = Image.createLogical(SOURCE_SIZE, SOURCE_SIZE, 1);
    Graphics canvas = requireGraphics(target);
    Image.resetImageOperationAccountingForTest();
    drawBatch(canvas, source, 3);
    int[] first = source.getPixels();
    int[] second = source.getPixels();
    ImageCompactFormatsBenchmarkSupport.encodePng(source);
    return ImageCompactFormatsBenchmarkSupport.RGB565.equals(
        ImageCompactFormatsBenchmarkSupport.format(source))
        && samePixels(first, second)
        && NativeImageBacking.promotionAttemptsForTest() == 0
        && NativeImageBacking.compactReadbackCountForTest() > 0
        && NativeImageBacking.compactRowScratchPeakBytesForTest() <= SOURCE_SIZE * 4
        && NativeImageBacking.temporaryRgbaDecodeBytesForTest() == 0;
  }

  private static boolean checkWritePixels(
      ImageCompactFormatsBenchmarkSupport.Fixture rgbFixture,
      ImageCompactFormatsBenchmarkSupport.Fixture grayFixture,
      ImageCompactFormatsBenchmarkSupport.Fixture alphaFixture) throws Exception {
    boolean rgb = checkOpaqueWritePixels(rgbFixture, ImageCompactFormatsBenchmarkSupport.RGB565);
    boolean gray = checkOpaqueWritePixels(grayFixture, ImageCompactFormatsBenchmarkSupport.GRAY8);
    ImageCompactFormatsBenchmarkSupport.configure("post-enabled", "milestone9-full-stack", true);
    ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_OPACITY_METADATA,
        ImageOptimizationSettings.DISABLED);
    Image alpha = ImageCompactFormatsBenchmarkSupport.materialize(alphaFixture.bytes);
    Image target = new Image(SOURCE_SIZE, SOURCE_SIZE);
    Graphics canvas = requireGraphics(target);
    Image.resetImageOperationAccountingForTest();
    int[] expected = alpha.getPixels();
    canvas.drawImage(alpha, 0, 0, false);
    return rgb && gray
        && ImageCompactFormatsBenchmarkSupport.ARGB4444.equals(
            ImageCompactFormatsBenchmarkSupport.format(alpha))
        && samePixels(expected, target.getPixels())
        && NativeImageBacking.writePixelsFallbacksForTest() > 0
        && NativeImageBacking.promotionAttemptsForTest() == 0;
  }

  private static boolean checkOpaqueWritePixels(
      ImageCompactFormatsBenchmarkSupport.Fixture fixture, String expectedFormat) throws Exception {
    ImageCompactFormatsBenchmarkSupport.configure("post-enabled", "milestone9-full-stack", true);
    Image source = ImageCompactFormatsBenchmarkSupport.materialize(fixture.bytes);
    Image target = new Image(SOURCE_SIZE, SOURCE_SIZE);
    Graphics canvas = requireGraphics(target);
    Image.resetImageOperationAccountingForTest();
    int[] expected = source.getPixels();
    canvas.drawImage(source, 0, 0, false);
    return expectedFormat.equals(ImageCompactFormatsBenchmarkSupport.format(source))
        && samePixels(expected, target.getPixels())
        && NativeImageBacking.writePixelsHitsForTest() > 0
        && NativeImageBacking.writePixelsFallbacksForTest() == 0
        && NativeImageBacking.promotionAttemptsForTest() == 0;
  }

  private static boolean checkAdaptiveJpeg(
      ImageCompactFormatsBenchmarkSupport.Fixture rgbFixture,
      ImageCompactFormatsBenchmarkSupport.Fixture grayFixture) throws Exception {
    boolean rgb = checkJpegTiers(rgbFixture, RGB565_JPEG,
        ImageCompactFormatsBenchmarkSupport.RGB565);
    boolean gray = checkJpegTiers(grayFixture, GRAY8_JPEG,
        ImageCompactFormatsBenchmarkSupport.GRAY8);
    return rgb && gray && checkAdaptiveFailure(rgbFixture);
  }

  private static boolean checkJpegTiers(
      ImageCompactFormatsBenchmarkSupport.Fixture fixture, String path, String expectedFormat)
      throws Exception {
    int[] denominators = {1, 2, 4, 8};
    for (int denominator : denominators) {
      ImageCompactFormatsBenchmarkSupport.configure("post-enabled", "milestone9-full-stack", true);
      Image.resetImageOperationAccountingForTest();
      Image result = Image.decodeJpegAtDenominatorForTest(fixture.bytes, denominator);
      int expectedWidth = (SOURCE_SIZE + denominator - 1) / denominator;
      long expectedBytes = (long) expectedWidth * expectedWidth
          * compactBytesPerPixel(expectedFormat);
      boolean decodeCount = denominator == 1
          ? Image.fullDecodeInvocationCountForTest() == 1
          : Image.targetedDecodeInvocationCountForTest() == 1;
      if (result.getPixelWidth() != expectedWidth || result.getPixelHeight() != expectedWidth
          || result.getWidth() != SOURCE_SIZE || result.getHeight() != SOURCE_SIZE
          || Math.abs(result.getContentScale() - 1.0 / denominator) > 0.000001
          || !expectedFormat.equals(ImageCompactFormatsBenchmarkSupport.format(result))
          || Image.decodeFinalBufferBytesForTest() != expectedBytes
          || NativeImageBacking.temporaryRgbaDecodeBytesForTest() != 0
          || NativeImageBacking.promotionAttemptsForTest() != 0 || !decodeCount) {
        System.out.println("adaptiveTierFailure path=" + path + ",denominator=" + denominator
            + ",pixel=" + result.getPixelWidth() + "x" + result.getPixelHeight()
            + ",logical=" + result.getWidth() + "x" + result.getHeight()
            + ",scale=" + result.getContentScale() + ",format="
            + ImageCompactFormatsBenchmarkSupport.format(result) + ",finalBytes="
            + Image.decodeFinalBufferBytesForTest() + ",expectedBytes=" + expectedBytes
            + ",full=" + Image.fullDecodeInvocationCountForTest() + ",targeted="
            + Image.targetedDecodeInvocationCountForTest() + ",temp="
            + NativeImageBacking.temporaryRgbaDecodeBytesForTest() + ",promotions="
            + NativeImageBacking.promotionAttemptsForTest());
        return false;
      }
      Image explicit = Image.getJpegScaled(path, 1, denominator);
      Image bestFit = Image.getJpegBestFit(path, expectedWidth, expectedWidth);
      if (explicit.getPixelWidth() != expectedWidth || explicit.getPixelHeight() != expectedWidth
          || bestFit.getPixelWidth() != expectedWidth || bestFit.getPixelHeight() != expectedWidth
          || !expectedFormat.equals(ImageCompactFormatsBenchmarkSupport.format(explicit))
          || !expectedFormat.equals(ImageCompactFormatsBenchmarkSupport.format(bestFit))) {
        System.out.println("adaptiveApiFailure path=" + path + ",denominator=" + denominator
            + ",explicit=" + explicit.getPixelWidth() + "x" + explicit.getPixelHeight()
            + ",bestFit=" + bestFit.getPixelWidth() + "x" + bestFit.getPixelHeight()
            + ",explicitFormat=" + ImageCompactFormatsBenchmarkSupport.format(explicit)
            + ",bestFitFormat=" + ImageCompactFormatsBenchmarkSupport.format(bestFit));
        return false;
      }
    }
    return true;
  }

  private static boolean checkAdaptiveFailure(
      ImageCompactFormatsBenchmarkSupport.Fixture fixture) throws Exception {
    int denominator = 4;
    ImageCompactFormatsBenchmarkSupport.configure("post-enabled", "milestone9-full-stack", true);
    Image.resetImageOperationAccountingForTest();
    long beforeLive = NativeImageBacking.backingBytesLiveForTest();
    int beforeFinalBytes = Image.decodeFinalBufferBytesForTest();
    Image.failNextZeroCopyDecodeAfterAllocationForTest();
    boolean failed = false;
    try {
      Image.decodeJpegAtDenominatorForTest(fixture.bytes, denominator);
    } catch (Throwable expected) {
      failed = true;
    }
    long afterFailureLive = NativeImageBacking.backingBytesLiveForTest();
    int afterFailureFinalBytes = Image.decodeFinalBufferBytesForTest();
    Image retry = Image.decodeJpegAtDenominatorForTest(fixture.bytes, denominator);
    long expectedBytes = (long) (SOURCE_SIZE / denominator) * (SOURCE_SIZE / denominator) * 2;
    boolean pass = failed && afterFailureLive <= beforeLive
        && beforeFinalBytes == afterFailureFinalBytes
        && Image.decodeFinalBufferBytesForTest() == beforeFinalBytes + expectedBytes
        && ImageCompactFormatsBenchmarkSupport.RGB565.equals(
            ImageCompactFormatsBenchmarkSupport.format(retry))
        && NativeImageBacking.temporaryRgbaDecodeBytesForTest() == 0;
    return pass;
  }

  private boolean checkScreenDraw(
      ImageCompactFormatsBenchmarkSupport.Fixture rgbFixture,
      ImageCompactFormatsBenchmarkSupport.Fixture grayFixture,
      ImageCompactFormatsBenchmarkSupport.Fixture alphaFixture) throws Exception {
    Image rgb = ImageCompactFormatsBenchmarkSupport.materialize(rgbFixture.bytes);
    Image gray = ImageCompactFormatsBenchmarkSupport.materialize(grayFixture.bytes);
    Image alpha = ImageCompactFormatsBenchmarkSupport.materialize(alphaFixture.bytes);
    Graphics screen = getGraphics();
    ImageRasterBenchmarkSupport.require(screen != null, "MainWindow screen graphics");
    Image.resetImageOperationAccountingForTest();
    screen.drawImage(rgb, 0, 0, false);
    screen.drawImage(gray, SOURCE_SIZE, 0, false);
    screen.drawImage(alpha, SOURCE_SIZE * 2, 0, false);
    boolean pass = ImageCompactFormatsBenchmarkSupport.RGB565.equals(
            ImageCompactFormatsBenchmarkSupport.format(rgb))
        && ImageCompactFormatsBenchmarkSupport.GRAY8.equals(
            ImageCompactFormatsBenchmarkSupport.format(gray))
        && ImageCompactFormatsBenchmarkSupport.ARGB4444.equals(
            ImageCompactFormatsBenchmarkSupport.format(alpha))
        && (!Settings.ANDROID.equals(Settings.platform) || (NativeImageBacking.writePixelsAttemptsForTest() == 0
            && NativeImageBacking.writePixelsHitsForTest() == 0
            && NativeImageBacking.writePixelsFallbacksForTest() == 0
            && NativeImageBacking.targetColorAttemptsForTest() == 0
            && NativeImageBacking.targetColorMaterializationsForTest() == 0
            && NativeImageBacking.targetColorHitsForTest() == 0
            && NativeImageBacking.physicalVariantLookupsForTest() == 0
            && NativeImageBacking.physicalVariantMaterializationsForTest() == 0
            && NativeImageBacking.physicalVariantHitsForTest() == 0
            && NativeImageBacking.physicalIdentityAttemptsForTest() == 0
            && NativeImageBacking.physicalIdentityHitsForTest() == 0))
        && NativeImageBacking.promotionAttemptsForTest() == 0
        && NativeImageBacking.temporaryRgbaDecodeBytesForTest() == 0;
    if (!pass) {
      System.out.println("screenDrawFailure=rgb="
          + ImageCompactFormatsBenchmarkSupport.format(rgb) + ",gray="
          + ImageCompactFormatsBenchmarkSupport.format(gray) + ",alpha="
          + ImageCompactFormatsBenchmarkSupport.format(alpha) + ",write="
          + NativeImageBacking.writePixelsAttemptsForTest() + "/"
          + NativeImageBacking.writePixelsHitsForTest() + "/"
          + NativeImageBacking.writePixelsFallbacksForTest() + ",target="
          + NativeImageBacking.targetColorAttemptsForTest() + "/"
          + NativeImageBacking.targetColorMaterializationsForTest() + "/"
          + NativeImageBacking.targetColorHitsForTest() + ",variant="
          + NativeImageBacking.physicalVariantLookupsForTest() + "/"
          + NativeImageBacking.physicalVariantMaterializationsForTest() + "/"
          + NativeImageBacking.physicalVariantHitsForTest() + ",identity="
          + NativeImageBacking.physicalIdentityAttemptsForTest() + "/"
          + NativeImageBacking.physicalIdentityHitsForTest() + ",promotions="
          + NativeImageBacking.promotionAttemptsForTest() + ",tempRgba="
          + NativeImageBacking.temporaryRgbaDecodeBytesForTest() + ",opengl="
          + Settings.isOpenGL);
    }
    return pass;
  }

  private static int compactBytesPerPixel(String format) {
    return ImageCompactFormatsBenchmarkSupport.GRAY8.equals(format) ? 1 : 2;
  }

  private static Graphics requireGraphics(Image image) {
    Graphics graphics = image.getGraphics();
    ImageRasterBenchmarkSupport.require(graphics != null, "smoke target graphics");
    return graphics;
  }

  private static void drawBatch(Graphics canvas, Image image, int count) {
    for (int i = 0; i < count; i++) {
      canvas.drawImage(image, 0, 0, false);
    }
  }

  private static boolean samePixels(int[] first, int[] second) {
    if (first == null || second == null || first.length != second.length) {
      return false;
    }
    for (int i = 0; i < first.length; i++) {
      if (first[i] != second[i]) {
        return false;
      }
    }
    return true;
  }
}
