// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.io.ByteArrayStream;
import totalcross.sys.Architecture;
import totalcross.sys.GraphicsBackend;
import totalcross.sys.Platform;
import totalcross.sys.RuntimeFamily;
import totalcross.sys.Vm;
import totalcross.sys.runtime.ImageRuntimeConfigurationStartup;
import totalcross.sys.runtime.RuntimeCondition;
import totalcross.sys.runtime.RuntimeConfiguration;
import totalcross.sys.runtime.RuntimeWhen;
import totalcross.ui.MainWindow;

/** macOS ARM64 integration smoke for compact decode, observation, and promotion. */
@ImageRuntimeRule(when = @RuntimeWhen(allOf = {
    @RuntimeCondition(platform = Platform.MACOS),
    @RuntimeCondition(family = RuntimeFamily.DESKTOP),
    @RuntimeCondition(architecture = Architecture.ARM64),
    @RuntimeCondition(backend = GraphicsBackend.RASTER)
}), storage = ImageStorageProfile.COMPACT)
@RuntimeConfiguration
public final class ImageCompactStorageSmokeApp extends MainWindow {
  private static final int WIDTH = 8;
  private static final int HEIGHT = 6;
  private static final int COLOR_ROW_BYTES = WIDTH * 2;
  private static final int GRAY_ROW_BYTES = WIDTH;
  private static String compactDecodeFailureEvidence = "";

  @Override
  public void initUI() {
    boolean policy = false;
    boolean standardBaselineRgba = false;
    boolean formatsAndAccounting = false;
    boolean observersAndEncoding = false;
    boolean equalityHash = false;
    boolean drawParity = false;
    boolean alphaQuality = false;
    boolean promotionRetry = false;
    boolean p3Invalidation = false;
    boolean directMutationPromotion = false;
    boolean graphicsMutationPromotion = false;
    boolean compactDecodeFailureRetry = false;
    boolean compactMetrics = false;
    int rgb565ModelError = -1;
    int jpegSourceError = -1;
    int grayJpegSourceError = -1;
    int alphaDrawError = -1;
    int pngSourceError = -1;
    double pngSourceRmse = -1;
    int rgbTargetError = -1;
    int grayPngSourceError = -1;
    int promotionReadbackError = -1;
    int promotionFormat = -1;
    int alphaCompositeError = -1;
    int alphaModelCompositeError = -1;
    long promotionBytesBefore = -1;
    long promotionBytesAfter = -1;
    int promotionElapsedMillis = -1;
    long liveGrayBytes = -1;
    long peakGrayBytes = -1;
    long liveRgb565Bytes = -1;
    long peakRgb565Bytes = -1;
    long liveArgb4444Bytes = -1;
    long peakArgb4444Bytes = -1;
    long liveRgbaBytes = -1;
    long peakRgbaBytes = -1;
    long grayDecodeCount = -1;
    long grayDecodeBytes = -1;
    long rgb565DecodeCount = -1;
    long rgb565DecodeBytes = -1;
    long argb4444DecodeCount = -1;
    long argb4444DecodeBytes = -1;
    long compactReadbackCount = -1;
    long rowScratchPeakBytes = -1;
    long fullRgbaDecodeTempBytes = -1;
    long promotionAttempts = -1;
    long promotionSuccesses = -1;
    long promotionFailures = -1;
    long promotionBytes = -1;
    boolean promotionOpacityPreserved = false;
    boolean promotionGenerationAdvanced = false;
    boolean promotionVariantsCleared = false;
    boolean cachedSiblingPreserved = false;
    int compactDecodeFixtureCount = 0;
    String stage = "startup";
    String error = "";
    try {
      NativeImageBacking.resetBackingAccountingForTest();
      stage = "policy";
      policy = ImageRuntimeConfigurationStartup.currentPolicy().effectiveStorageProfile()
          == ImageStorageProfile.COMPACT;
      require(policy, "runtime policy did not enable COMPACT");

      Image standardBaseline = new Image(WIDTH, HEIGHT);
      NativeImageBacking standardBacking = nativeBacking(standardBaseline);
      standardBaselineRgba = !standardBacking.isCompact()
          && standardBacking.formatForTest() == NativeImageBacking.FORMAT_RGBA8888;
      require(standardBaselineRgba, "mutable STANDARD baseline did not remain RGBA8888");

      stage = "decode-and-format";
      Image rgbPng = load("opaque-color.png");
      Image rgbJpeg = load("opaque-color.jpg");
      Image grayPng = load("opaque-gray.png");
      Image grayJpeg = load("opaque-gray.jpg");
      Image alphaPng = load("alpha-gradient.png");
      Image alphaDuplicate = load("alpha-gradient.png");
      Image trnsPng = load("trns-color.png");
      ImageSource alphaRoot = alphaPng.pipelineForSmoke().root();
      NativeImageBacking rgbPngBacking = nativeBacking(rgbPng);
      NativeImageBacking rgbJpegBacking = nativeBacking(rgbJpeg);
      NativeImageBacking grayPngBacking = nativeBacking(grayPng);
      NativeImageBacking grayJpegBacking = nativeBacking(grayJpeg);
      NativeImageBacking alphaBacking = nativeBacking(alphaPng);
      NativeImageBacking alphaDuplicateBacking = nativeBacking(alphaDuplicate);
      NativeImageBacking trnsBacking = nativeBacking(trnsPng);
      compactDecodeFixtureCount = countCompact(rgbPngBacking, rgbJpegBacking, grayPngBacking,
          grayJpegBacking, alphaBacking, alphaDuplicateBacking, trnsBacking);

      formatsAndAccounting = checkFormat(rgbPngBacking, NativeImageBacking.FORMAT_RGB565,
              COLOR_ROW_BYTES, COLOR_ROW_BYTES * HEIGHT)
          && checkFormat(rgbJpegBacking, NativeImageBacking.FORMAT_RGB565,
              COLOR_ROW_BYTES, COLOR_ROW_BYTES * HEIGHT)
          && checkFormat(grayPngBacking, NativeImageBacking.FORMAT_GRAY8,
              GRAY_ROW_BYTES, GRAY_ROW_BYTES * HEIGHT)
          && checkFormat(grayJpegBacking, NativeImageBacking.FORMAT_GRAY8,
              GRAY_ROW_BYTES, GRAY_ROW_BYTES * HEIGHT)
          && checkFormat(alphaBacking, NativeImageBacking.FORMAT_ARGB4444,
              COLOR_ROW_BYTES, COLOR_ROW_BYTES * HEIGHT)
          && checkFormat(alphaDuplicateBacking, NativeImageBacking.FORMAT_ARGB4444,
              COLOR_ROW_BYTES, COLOR_ROW_BYTES * HEIGHT)
          && checkFormat(trnsBacking, NativeImageBacking.FORMAT_ARGB4444,
              COLOR_ROW_BYTES, COLOR_ROW_BYTES * HEIGHT);
      require(formatsAndAccounting, "format or actual resident byte accounting mismatch");

      observersAndEncoding = checkObservers(rgbPng, NativeImageBacking.FORMAT_RGB565)
          && checkObservers(rgbJpeg, NativeImageBacking.FORMAT_RGB565)
          && checkObservers(grayPng, NativeImageBacking.FORMAT_GRAY8)
          && checkObservers(grayJpeg, NativeImageBacking.FORMAT_GRAY8)
          && checkObservers(alphaPng, NativeImageBacking.FORMAT_ARGB4444)
          && checkObservers(trnsPng, NativeImageBacking.FORMAT_ARGB4444);
      equalityHash = alphaPng.equals(alphaDuplicate)
          && alphaPng.hashCode() == alphaDuplicate.hashCode()
          && alphaBacking.formatForTest() == NativeImageBacking.FORMAT_ARGB4444
          && alphaDuplicateBacking.formatForTest() == NativeImageBacking.FORMAT_ARGB4444;
      observersAndEncoding &= equalityHash;
      require(observersAndEncoding, "readback or PNG encoding changed canonical compact storage");

      stage = "observer-quality-and-draw";
      int[] rgbExpected = expectedOpaqueRgb565();
      int[] pngPixels = rgbPng.getPixels();
      int[] jpegPixels = rgbJpeg.getPixels();
      int[] grayPngPixels = grayPng.getPixels();
      int[] grayJpegPixels = grayJpeg.getPixels();
      int[] alphaPixels = alphaPng.getPixels();
      Image rgbTarget = new Image(WIDTH, HEIGHT);
      rgbTarget.getGraphics().drawImage(rgbPng, 0, 0, true);
      Image alphaTarget = new Image(WIDTH, HEIGHT);
      int[] whitePixels = new int[WIDTH * HEIGHT];
      java.util.Arrays.fill(whitePixels, 0xFFFFFFFF);
      require(alphaTarget.getGraphics().setRGB(whitePixels, 0, 0, 0, WIDTH, HEIGHT) == whitePixels.length,
          "could not initialize alpha draw target");
      alphaTarget.getGraphics().drawImage(alphaPng, 0, 0, true);
      rgb565ModelError = maxChannelError(pngPixels, rgbExpected);
      pngSourceRmse = rootMeanSquareChannelError(pngPixels, expectedOpaqueSource());
      jpegSourceError = maxChannelError(jpegPixels, expectedOpaqueSource());
      grayJpegSourceError = maxChannelError(grayJpegPixels, expectedGraySource());
      pngSourceError = maxChannelError(pngPixels, expectedOpaqueSource());
      rgbTargetError = maxChannelError(pngPixels, rgbTarget.getPixels());
      grayPngSourceError = maxChannelError(grayPngPixels, expectedGraySource());
      alphaDrawError = compositingError(compositeOnWhite(alphaPixels), alphaTarget.getPixels());
      drawParity = rgbTargetError <= 1 && rgb565ModelError == 0
          && pngSourceError <= 9
          && alphaDrawError <= 1
          && jpegSourceError <= 24
          && exactGray(grayPngPixels, expectedGraySource())
          && grayscaleChannels(grayJpegPixels)
          && grayJpegSourceError <= 10
          && nativeBacking(rgbPng).formatForTest() == NativeImageBacking.FORMAT_RGB565;

      int[] alphaExpected = expectedAlphaSource();
      alphaCompositeError = compositingError(alphaExpected, alphaPixels);
      alphaModelCompositeError = compositingError(expectedArgb4444Readback(), alphaPixels);
      alphaQuality = alphaPixels[0] == 0 && trnsPng.getPixels()[0] == 0
          && alphaCompositeError <= 24 && alphaModelCompositeError <= 1;

      stage = "shared-source-inspection";
      require(alphaRoot instanceof EncodedImageSource, "compact source lost its encoded root");
      ImageBacking cachedSiblingBacking = ((EncodedImageSource) alphaRoot).decodedBackingForReuse(1);
      require(cachedSiblingBacking instanceof NativeImageBacking
              && ((NativeImageBacking) cachedSiblingBacking).formatForTest() == NativeImageBacking.FORMAT_ARGB4444,
          "compact sibling did not retain ARGB4444");
      long generationBefore = alphaPng.backingMutationGenerationForP2();
      int opacityBefore = alphaPng.backing.opacityState();
      int[] pixelsBeforePromotion = alphaPng.getPixels();
      NativeImageBacking promotionBacking = nativeBacking(alphaPng);
      int[] variantKey = {0x10203040, 0x50607080};
      promotionBacking.observeVariantForTest(NativeImageBacking.RASTER_VARIANT_PHYSICAL, variantKey);
      promotionBacking.observeVariantForTest(NativeImageBacking.RASTER_VARIANT_PHYSICAL, variantKey);
      int variantBeforePromotion = promotionBacking.variantStateForTest();
      promotionBytesBefore = promotionBacking.backingBytesForTest();
      NativeImageBacking.failNextPromotionForTest();
      boolean failedAsExpected = false;
      stage = "injected-promotion-failure";
      try {
        alphaPng.prepareForMutation();
      } catch (IllegalStateException expected) {
        failedAsExpected = true;
      }
      boolean failurePreserved = failedAsExpected && promotionBacking.isCompact()
          && promotionBacking.variantStateForTest() == variantBeforePromotion
          && alphaPng.backingMutationGenerationForP2() == generationBefore
          && alphaPng.backing.opacityState() == opacityBefore
          && same(pixelsBeforePromotion, alphaPng.getPixels());
      require(failurePreserved, "failed promotion changed backing state or pixels");

      stage = "promotion-retry";
      int promotionStart = Vm.getTimeStamp();
      alphaPng.prepareForMutation();
      promotionElapsedMillis = Vm.getTimeStamp() - promotionStart;
      NativeImageBacking promotedBacking = nativeBacking(alphaPng);
      promotionBytesAfter = promotedBacking.backingBytesForTest();
      promotionFormat = promotedBacking.formatForTest();
      promotionGenerationAdvanced = alphaPng.backingMutationGenerationForP2() > generationBefore;
      promotionOpacityPreserved = alphaPng.backing.opacityState() == opacityBefore;
      promotionVariantsCleared = promotedBacking.variantStateForTest() == 0;
      promotionReadbackError = maxChannelError(pixelsBeforePromotion, alphaPng.getPixels());
      cachedSiblingPreserved = cachedSiblingBacking instanceof NativeImageBacking
          && cachedSiblingBacking != promotedBacking
          && ((NativeImageBacking) cachedSiblingBacking).isCompact();
      boolean retrySucceeded = !promotedBacking.isCompact()
          && promotionFormat == NativeImageBacking.FORMAT_RGBA8888
          && promotionGenerationAdvanced && promotionOpacityPreserved && promotionVariantsCleared
          && promotionReadbackError == 0 && cachedSiblingPreserved;
      require(retrySucceeded, "promotion retry did not preserve state or clear variants");
      long attemptsAfterPromotion = metric(NativeImageBacking.TEST_METRIC_PROMOTION_ATTEMPTS, 0);
      alphaPng.getGraphics().fillRect(0, 0, 1, 1);
      promotionRetry = !nativeBacking(alphaPng).isCompact()
          && alphaPng.backingMutationGenerationForP2() > generationBefore
          && metric(NativeImageBacking.TEST_METRIC_PROMOTION_ATTEMPTS, 0) == attemptsAfterPromotion;
      p3Invalidation = retrySucceeded;
      stage = "graphics-mutation-after-promotion";
      require(promotionRetry, "normal Graphics mutation did not stay in RGBA8888 after promotion");

      stage = "direct-mutator-promotion";
      Image directReadOnly = load("opaque-color.png");
      int[] beforeDirectMutation = directReadOnly.getPixels();
      Image directMutation = load("opaque-color.png");
      ImagePipeline directPipeline = directMutation.pipelineForSmoke();
      EncodedImageSource directRoot = (EncodedImageSource) directPipeline.root();
      long directGeneration = directMutation.backingMutationGenerationForP2();
      directMutation.applyFade(128);
      NativeImageBacking directPromoted = nativeBacking(directMutation);
      ImageBacking directSourceBacking = directRoot.decodedBackingForReuse(1);
      directMutationPromotion = !directPromoted.isCompact()
          && directPromoted.formatForTest() == NativeImageBacking.FORMAT_RGBA8888
          && directSourceBacking instanceof NativeImageBacking
          && ((NativeImageBacking) directSourceBacking).isCompact()
          && directMutation.backingMutationGenerationForP2() > directGeneration
          && maxChannelError(beforeDirectMutation, directMutation.getPixels()) > 0;
      require(directMutationPromotion,
          "direct Image mutator did not promote compact pixels before writing");

      stage = "graphics-promotion-on-access";
      Image graphicsMutation = load("opaque-gray.png");
      NativeImageBacking graphicsCompact = nativeBacking(graphicsMutation);
      long graphicsGeneration = graphicsMutation.backingMutationGenerationForP2();
      graphicsMutation.getGraphics().fillRect(0, 0, 1, 1);
      graphicsMutationPromotion = !graphicsCompact.isCompact()
          && nativeBacking(graphicsMutation).formatForTest() == NativeImageBacking.FORMAT_RGBA8888
          && graphicsMutation.backingMutationGenerationForP2() > graphicsGeneration;
      require(graphicsMutationPromotion,
          "getGraphics did not promote a compact image before native drawing");

      stage = "format-and-lifecycle-metrics";
      liveGrayBytes = metric(NativeImageBacking.TEST_METRIC_LIVE_BYTES_BY_FORMAT,
          NativeImageBacking.FORMAT_GRAY8);
      peakGrayBytes = metric(NativeImageBacking.TEST_METRIC_PEAK_BYTES_BY_FORMAT,
          NativeImageBacking.FORMAT_GRAY8);
      liveRgb565Bytes = metric(NativeImageBacking.TEST_METRIC_LIVE_BYTES_BY_FORMAT,
          NativeImageBacking.FORMAT_RGB565);
      peakRgb565Bytes = metric(NativeImageBacking.TEST_METRIC_PEAK_BYTES_BY_FORMAT,
          NativeImageBacking.FORMAT_RGB565);
      liveArgb4444Bytes = metric(NativeImageBacking.TEST_METRIC_LIVE_BYTES_BY_FORMAT,
          NativeImageBacking.FORMAT_ARGB4444);
      peakArgb4444Bytes = metric(NativeImageBacking.TEST_METRIC_PEAK_BYTES_BY_FORMAT,
          NativeImageBacking.FORMAT_ARGB4444);
      liveRgbaBytes = metric(NativeImageBacking.TEST_METRIC_LIVE_BYTES_BY_FORMAT,
          NativeImageBacking.FORMAT_RGBA8888);
      peakRgbaBytes = metric(NativeImageBacking.TEST_METRIC_PEAK_BYTES_BY_FORMAT,
          NativeImageBacking.FORMAT_RGBA8888);
      grayDecodeCount = metric(NativeImageBacking.TEST_METRIC_COMPACT_DECODE_COUNT_BY_FORMAT,
          NativeImageBacking.FORMAT_GRAY8);
      grayDecodeBytes = metric(NativeImageBacking.TEST_METRIC_COMPACT_DECODE_BYTES_BY_FORMAT,
          NativeImageBacking.FORMAT_GRAY8);
      rgb565DecodeCount = metric(NativeImageBacking.TEST_METRIC_COMPACT_DECODE_COUNT_BY_FORMAT,
          NativeImageBacking.FORMAT_RGB565);
      rgb565DecodeBytes = metric(NativeImageBacking.TEST_METRIC_COMPACT_DECODE_BYTES_BY_FORMAT,
          NativeImageBacking.FORMAT_RGB565);
      argb4444DecodeCount = metric(NativeImageBacking.TEST_METRIC_COMPACT_DECODE_COUNT_BY_FORMAT,
          NativeImageBacking.FORMAT_ARGB4444);
      argb4444DecodeBytes = metric(NativeImageBacking.TEST_METRIC_COMPACT_DECODE_BYTES_BY_FORMAT,
          NativeImageBacking.FORMAT_ARGB4444);
      compactReadbackCount = metric(NativeImageBacking.TEST_METRIC_COMPACT_READBACK_COUNT, 0);
      rowScratchPeakBytes = metric(NativeImageBacking.TEST_METRIC_ROW_SCRATCH_PEAK_BYTES, 0);
      fullRgbaDecodeTempBytes = metric(NativeImageBacking.TEST_METRIC_FULL_RGBA_DECODE_TEMP_BYTES, 0);
      promotionAttempts = metric(NativeImageBacking.TEST_METRIC_PROMOTION_ATTEMPTS, 0);
      promotionSuccesses = metric(NativeImageBacking.TEST_METRIC_PROMOTION_SUCCESSES, 0);
      promotionFailures = metric(NativeImageBacking.TEST_METRIC_PROMOTION_FAILURES, 0);
      promotionBytes = metric(NativeImageBacking.TEST_METRIC_PROMOTION_BYTES, 0);
      long directDecodeCount = grayDecodeCount + rgb565DecodeCount + argb4444DecodeCount;
      long directDecodeBytes = grayDecodeBytes + rgb565DecodeBytes + argb4444DecodeBytes;
      compactMetrics = directDecodeCount == compactDecodeFixtureCount + 3
          && directDecodeBytes == 3L * GRAY_ROW_BYTES * HEIGHT
              + 7L * COLOR_ROW_BYTES * HEIGHT
          && grayDecodeCount == 3 && grayDecodeBytes == 3L * GRAY_ROW_BYTES * HEIGHT
          && rgb565DecodeCount == 4 && rgb565DecodeBytes == 4L * COLOR_ROW_BYTES * HEIGHT
          && argb4444DecodeCount == 3 && argb4444DecodeBytes == 3L * COLOR_ROW_BYTES * HEIGHT
          && liveGrayBytes >= grayDecodeBytes && liveRgb565Bytes >= rgb565DecodeBytes
          && liveArgb4444Bytes >= argb4444DecodeBytes && liveRgbaBytes >= WIDTH * HEIGHT * 4
          && peakGrayBytes >= grayDecodeBytes && peakRgb565Bytes >= rgb565DecodeBytes
          && peakArgb4444Bytes >= argb4444DecodeBytes && peakRgbaBytes >= liveRgbaBytes
          && peakRgb565Bytes >= liveRgb565Bytes && peakGrayBytes >= liveGrayBytes
          && peakArgb4444Bytes >= liveArgb4444Bytes
          && compactReadbackCount > 0 && rowScratchPeakBytes == WIDTH * 4
          && fullRgbaDecodeTempBytes == 0
          && promotionAttempts == promotionSuccesses + promotionFailures
          && promotionFailures == 1 && promotionSuccesses >= 3
          && promotionBytes == promotionSuccesses * WIDTH * HEIGHT * 4;
      require(compactMetrics, "compact storage lifecycle metrics were incomplete or inconsistent");

      stage = "compact-decode-allocation-failure-retry";
      compactDecodeFailureRetry = compactDecodeFailureRetry("opaque-color.png")
          && compactDecodeFailureRetry("opaque-color.jpg");
      require(compactDecodeFailureRetry,
          "compact decoder candidate was not released exactly once or could not be retried");
    } catch (Throwable failure) {
      error = "stage=" + stage + "," + failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean overallPass = policy && standardBaselineRgba && formatsAndAccounting
        && observersAndEncoding && drawParity
        && alphaQuality && promotionRetry && p3Invalidation && directMutationPromotion
        && graphicsMutationPromotion && compactMetrics && compactDecodeFailureRetry;
    System.out.println("fixture=ImageCompactStorageSmokeApp,policy=" + policy
        + ",standardBaselineRgba=" + standardBaselineRgba
        + ",formatsAndAccounting=" + formatsAndAccounting
        + ",observersAndEncoding=" + observersAndEncoding + ",equalityHash=" + equalityHash
        + ",drawParity=" + drawParity
        + ",alphaQuality=" + alphaQuality + ",promotionRetry=" + promotionRetry
        + ",p3Invalidation=" + p3Invalidation + ",directMutationPromotion="
        + directMutationPromotion + ",graphicsMutationPromotion=" + graphicsMutationPromotion
        + ",compactMetrics=" + compactMetrics + ",compactDecodeFailureRetry=" + compactDecodeFailureRetry
        + ",opaqueRgbBytes=" + (COLOR_ROW_BYTES * HEIGHT)
        + ",rgb565ModelError=" + rgb565ModelError + ",pngSourceRmse=" + pngSourceRmse
        + ",jpegSourceError=" + jpegSourceError
        + ",pngSourceError=" + pngSourceError + ",grayJpegSourceError=" + grayJpegSourceError
        + ",grayPngSourceError=" + grayPngSourceError + ",rgbTargetError=" + rgbTargetError
        + ",alphaDrawError=" + alphaDrawError
        + ",alphaCompositeError=" + alphaCompositeError
        + ",alphaModelCompositeError=" + alphaModelCompositeError
        + ",compactDecodeFixtureCount=" + compactDecodeFixtureCount
        + ",compactDecodeFailureEvidence=" + compactDecodeFailureEvidence
        + ",liveBytesByFormat=" + liveRgbaBytes + "/" + liveRgb565Bytes + "/"
        + liveGrayBytes + "/" + liveArgb4444Bytes
        + ",peakBytesByFormat=" + peakRgbaBytes + "/" + peakRgb565Bytes + "/"
        + peakGrayBytes + "/" + peakArgb4444Bytes
        + ",compactDecodeCountByFormat=" + rgb565DecodeCount + "/" + grayDecodeCount + "/"
        + argb4444DecodeCount
        + ",compactDecodeBytesByFormat=" + rgb565DecodeBytes + "/" + grayDecodeBytes + "/"
        + argb4444DecodeBytes + ",compactReadbackCount=" + compactReadbackCount
        + ",rowScratchPeakBytes=" + rowScratchPeakBytes
        + ",fullRgbaDecodeTempBytes=" + fullRgbaDecodeTempBytes
        + ",promotionAttempts=" + promotionAttempts + ",promotionSuccesses=" + promotionSuccesses
        + ",promotionFailures=" + promotionFailures + ",promotionBytes=" + promotionBytes
        + ",promotionBytesBefore=" + promotionBytesBefore + ",promotionBytesAfter=" + promotionBytesAfter
        + ",promotionElapsedMillis=" + promotionElapsedMillis
        + ",promotionFormat=" + promotionFormat + ",promotionGenerationAdvanced="
        + promotionGenerationAdvanced + ",promotionOpacityPreserved=" + promotionOpacityPreserved
        + ",promotionVariantsCleared=" + promotionVariantsCleared + ",promotionReadbackError="
        + promotionReadbackError + ",cachedSiblingPreserved=" + cachedSiblingPreserved
        + ",grayBytes=" + (GRAY_ROW_BYTES * HEIGHT) + ",alphaBytes=" + (COLOR_ROW_BYTES * HEIGHT)
        + ",rgbaBytes=" + (WIDTH * 4 * HEIGHT) + ",overallPass=" + overallPass
        + (error.length() == 0 ? "" : ",error=" + error));
    System.out.flush();
    exit(overallPass ? 0 : 1);
  }

  private static Image load(String name) throws Exception {
    byte[] bytes = Vm.getFile("image-compact/" + name);
    require(bytes != null && bytes.length > 0, "missing fixture " + name);
    return new Image(bytes, bytes.length);
  }

  private static NativeImageBacking nativeBacking(Image image) {
    require(image.hasNativeBackingForSmoke(), "image did not materialize a native backing");
    require(image.backing instanceof NativeImageBacking, "image backing is not native");
    return (NativeImageBacking) image.backing;
  }

  private static boolean checkFormat(NativeImageBacking backing, int format, int rowBytes, long bytes) {
    return backing.formatForTest() == format && backing.rowBytesForTest() == rowBytes
        && backing.backingBytesForTest() == bytes;
  }

  private static int countCompact(NativeImageBacking... backings) {
    int count = 0;
    for (NativeImageBacking backing : backings) {
      if (backing.isCompact()) count++;
    }
    return count;
  }

  private static boolean checkObservers(Image image, int format) throws Exception {
    NativeImageBacking backing = nativeBacking(image);
    int[] first = image.getPixels();
    int[] second = image.getPixels();
    byte[] row = new byte[WIDTH * 4];
    image.getPixelRow(row, 2);
    boolean rowParity = true;
    for (int x = 0; x < WIDTH; x++) {
      int pixel = first[2 * WIDTH + x];
      rowParity &= (row[x * 4] & 0xFF) == ((pixel >>> 16) & 0xFF)
          && (row[x * 4 + 1] & 0xFF) == ((pixel >>> 8) & 0xFF)
          && (row[x * 4 + 2] & 0xFF) == (pixel & 0xFF)
          && (row[x * 4 + 3] & 0xFF) == (pixel >>> 24);
    }
    ByteArrayStream encoded = new ByteArrayStream(1024);
    image.createPng(encoded);
    return same(first, second) && rowParity && encoded.getPos() > 0
        && backing.formatForTest() == format;
  }

  private static int[] expectedOpaqueSource() {
    int[] expected = new int[WIDTH * HEIGHT];
    for (int y = 0; y < HEIGHT; y++) {
      for (int x = 0; x < WIDTH; x++) {
        int red = (x * 31 + y * 7) & 255;
        int green = (y * 43 + x * 5) & 255;
        int blue = ((x + y) * 23 + 11) & 255;
        expected[y * WIDTH + x] = 0xFF000000 | red << 16 | green << 8 | blue;
      }
    }
    return expected;
  }

  private static int[] expectedOpaqueRgb565() {
    int[] source = expectedOpaqueSource();
    for (int i = 0; i < source.length; i++) {
      int p = source[i];
      int r5 = (((p >>> 16) & 255) * 31 + 127) / 255;
      int g6 = (((p >>> 8) & 255) * 63 + 127) / 255;
      int b5 = ((p & 255) * 31 + 127) / 255;
      int red = (r5 << 3) | (r5 >>> 2);
      int green = (g6 << 2) | (g6 >>> 4);
      int blue = (b5 << 3) | (b5 >>> 2);
      source[i] = 0xFF000000 | red << 16 | green << 8 | blue;
    }
    return source;
  }

  private static int[] expectedArgb4444Readback() {
    int[] source = expectedAlphaSource();
    for (int i = 0; i < source.length; i++) {
      int pixel = source[i];
      int alpha = pixel >>> 24;
      int alpha4 = (alpha * 15 + 127) / 255;
      int expandedAlpha = alpha4 * 17;
      int red = unpremultiplyArgb4444((pixel >>> 16) & 255, alpha, alpha4);
      int green = unpremultiplyArgb4444((pixel >>> 8) & 255, alpha, alpha4);
      int blue = unpremultiplyArgb4444(pixel & 255, alpha, alpha4);
      source[i] = expandedAlpha << 24 | red << 16 | green << 8 | blue;
    }
    return source;
  }

  private static int unpremultiplyArgb4444(int channel, int alpha, int alpha4) {
    if (alpha4 == 0) {
      return 0;
    }
    int premultiplied4 = (channel * alpha * 15 + 255 * 255 / 2) / (255 * 255);
    return Math.min(255, (premultiplied4 * 255 + alpha4 / 2) / alpha4);
  }

  private static int[] expectedAlphaSource() {
    int[] expected = new int[WIDTH * HEIGHT];
    int[] alphas = {0, 32, 64, 96, 128, 160, 224, 255};
    for (int y = 0; y < HEIGHT; y++) {
      for (int x = 0; x < WIDTH; x++) {
        int alpha = alphas[x];
        int red = x == 0 ? 255 : (x * 29) & 255;
        int green = x == 0 ? 0 : (y * 37 + x * 3) & 255;
        int blue = x == 0 ? 255 : (255 - x * 21 - y * 5) & 255;
        expected[y * WIDTH + x] = alpha << 24 | red << 16 | green << 8 | blue;
      }
    }
    return expected;
  }

  private static int[] expectedGraySource() {
    int[] expected = new int[WIDTH * HEIGHT];
    for (int y = 0; y < HEIGHT; y++) {
      for (int x = 0; x < WIDTH; x++) {
        int gray = 18 + x * 24 + y * 7;
        expected[y * WIDTH + x] = 0xFF000000 | gray << 16 | gray << 8 | gray;
      }
    }
    return expected;
  }

  private static int[] compositeOnWhite(int[] pixels) {
    int[] result = pixels.clone();
    for (int i = 0; i < pixels.length; i++) {
      int alpha = pixels[i] >>> 24;
      int red = (pixels[i] >>> 16) & 255;
      int green = (pixels[i] >>> 8) & 255;
      int blue = pixels[i] & 255;
      red = (red * alpha + 255 * (255 - alpha) + 127) / 255;
      green = (green * alpha + 255 * (255 - alpha) + 127) / 255;
      blue = (blue * alpha + 255 * (255 - alpha) + 127) / 255;
      result[i] = 0xFF000000 | red << 16 | green << 8 | blue;
    }
    return result;
  }

  private static boolean exactGray(int[] actual, int[] expected) {
    return java.util.Arrays.equals(actual, expected);
  }

  private static boolean grayscaleChannels(int[] pixels) {
    for (int pixel : pixels) {
      if (((pixel >>> 16) & 255) != ((pixel >>> 8) & 255)
          || ((pixel >>> 8) & 255) != (pixel & 255) || (pixel >>> 24) != 255) {
        return false;
      }
    }
    return true;
  }

  private static int compositingError(int[] expected, int[] actual) {
    int maxError = 0;
    for (int i = 0; i < expected.length; i++) {
      int expectedAlpha = expected[i] >>> 24;
      int actualAlpha = actual[i] >>> 24;
      for (int shift = 0; shift <= 16; shift += 8) {
        int expectedChannel = (expected[i] >>> shift) & 255;
        int actualChannel = (actual[i] >>> shift) & 255;
        for (int background : new int[] {0, 255}) {
          int expectedComposite = (expectedChannel * expectedAlpha
              + background * (255 - expectedAlpha) + 127) / 255;
          int actualComposite = (actualChannel * actualAlpha
              + background * (255 - actualAlpha) + 127) / 255;
          maxError = Math.max(maxError, Math.abs(expectedComposite - actualComposite));
        }
      }
    }
    return maxError;
  }

  private static int maxChannelError(int[] first, int[] second) {
    int maxError = 0;
    for (int i = 0; i < first.length; i++) {
      for (int shift = 0; shift <= 24; shift += 8) {
        maxError = Math.max(maxError,
            Math.abs(((first[i] >>> shift) & 255) - ((second[i] >>> shift) & 255)));
      }
    }
    return maxError;
  }

  private static double rootMeanSquareChannelError(int[] first, int[] second) {
    long squaredError = 0;
    int channelCount = first.length * 3;
    for (int i = 0; i < first.length; i++) {
      for (int shift = 0; shift <= 16; shift += 8) {
        int error = ((first[i] >>> shift) & 255) - ((second[i] >>> shift) & 255);
        squaredError += (long) error * error;
      }
    }
    return Math.sqrt((double) squaredError / channelCount);
  }

  private static long metric(int metric, int format) {
    return NativeImageBacking.metricForTest(metric, format);
  }

  private static boolean compactDecodeFailureRetry(String fixture) throws Exception {
    NativeImageBacking.resetBackingAccountingForTest();
    long liveBefore = NativeImageBacking.backingRecordsLiveForTest();
    Image image = load(fixture);
    EncodedImageSource source = (EncodedImageSource) image.pipelineForSmoke().root();
    Image.failCompactCandidateForTest();
    boolean transientFailure = false;
    boolean materializedOnFirstAttempt = false;
    try {
      materializedOnFirstAttempt = image.hasNativeBackingForSmoke();
    } catch (IllegalStateException failure) {
      transientFailure = failure.getCause() instanceof TransientImageMaterializationException;
    }
    long createdAfterFirstAttempt = NativeImageBacking.backingRecordsCreatedForTest();
    long releasedAfterFirstAttempt = NativeImageBacking.backingRecordsReleasedForTest();
    long liveAfterFirstAttempt = NativeImageBacking.backingRecordsLiveForTest();
    boolean transientFailureClean = transientFailure && createdAfterFirstAttempt == 1
        && releasedAfterFirstAttempt == 1 && liveAfterFirstAttempt == liveBefore
        && image.pipelineForSmoke() != null;
    boolean fallbackRetryClean = materializedOnFirstAttempt && createdAfterFirstAttempt >= 2
        && releasedAfterFirstAttempt == 1
        && liveAfterFirstAttempt == liveBefore + createdAfterFirstAttempt - releasedAfterFirstAttempt;
    boolean failedCandidateReleased = transientFailureClean || fallbackRetryClean;
    if (!failedCandidateReleased || source.decodeFailure() != null
        || !image.hasNativeBackingForSmoke()) {
      compactDecodeFailureEvidence += fixture + "=failed:thrown=" + transientFailure
          + ",materialized=" + materializedOnFirstAttempt + ",created=" + createdAfterFirstAttempt
          + ",released=" + releasedAfterFirstAttempt + ";";
      return false;
    }
    NativeImageBacking backing = nativeBacking(image);
    long createdAfterRetry = NativeImageBacking.backingRecordsCreatedForTest();
    long releasedAfterRetry = NativeImageBacking.backingRecordsReleasedForTest();
    boolean retrySucceeded = backing.isCompact() && releasedAfterRetry == 1
        && NativeImageBacking.backingRecordsLiveForTest()
            == liveBefore + createdAfterRetry - releasedAfterRetry;
    compactDecodeFailureEvidence += fixture + "="
        + (transientFailure ? "explicit-retry" : "fallback-retry") + ":"
        + createdAfterRetry + "/" + releasedAfterRetry + ";";
    return retrySucceeded;
  }

  private static boolean same(int[] first, int[] second) {
    return java.util.Arrays.equals(first, second);
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
