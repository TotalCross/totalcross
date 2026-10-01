// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

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

import static totalcross.ui.image.ImageCompactStorageSmokeSupport.*;

/** macOS ARM64 integration smoke for compact decode, observation, and promotion. */
@ImageRuntimeRule(when = @RuntimeWhen(allOf = {
    @RuntimeCondition(platform = Platform.MACOS),
    @RuntimeCondition(family = RuntimeFamily.DESKTOP),
    @RuntimeCondition(architecture = Architecture.ARM64),
    @RuntimeCondition(backend = GraphicsBackend.RASTER)
}), storage = ImageStorageProfile.COMPACT)
@RuntimeConfiguration
public final class ImageCompactStorageSmokeApp extends MainWindow {
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
    boolean p3PhysicalVariantReuse = false;
    boolean p3TargetColorVariant = false;
    boolean p3IncompatibleFallback = false;
    boolean p6Rgb565DirectCopy = false;
    boolean p6Gray8Fallback = false;
    boolean p6Argb4444Fallback = false;
    boolean p6TargetColorCopyRect = false;
    boolean p6CachedFinalReuse = false;
    boolean compactMetrics = false;
    int rgb565ModelError = -1;
    int jpegSourceError = -1;
    int grayJpegSourceError = -1;
    int alphaDrawError = -1;
    int pngSourceError = -1;
    double pngSourceRmse = -1;
    int rgbTargetError = -1;
    int grayPngSourceError = -1;
    int alphaCompositeError = -1;
    int alphaModelCompositeError = -1;
    int compactDecodeFixtureCount = 0;
    Metrics metrics = new Metrics();
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
      int[] pendingVariantKey = {0x10203040, 0x50607081};
      int firstVariantMiss = promotionBacking.observeVariantForTest(
          NativeImageBacking.RASTER_VARIANT_PHYSICAL, variantKey);
      int variantAdmission = promotionBacking.observeVariantForTest(
          NativeImageBacking.RASTER_VARIANT_PHYSICAL, variantKey);
      int cachedVariantHit = promotionBacking.observeVariantForTest(
          NativeImageBacking.RASTER_VARIANT_PHYSICAL, variantKey);
      int pendingVariantMiss = promotionBacking.observeVariantForTest(
          NativeImageBacking.RASTER_VARIANT_PHYSICAL, pendingVariantKey);
      int variantBeforePromotion = promotionBacking.variantStateForTest();
      boolean cachedAndPendingPrepared = firstVariantMiss == NativeImageBacking.RASTER_VARIANT_MISS
          && variantAdmission == NativeImageBacking.RASTER_VARIANT_MATERIALIZE
          && cachedVariantHit == NativeImageBacking.RASTER_VARIANT_HIT
          && pendingVariantMiss == NativeImageBacking.RASTER_VARIANT_MISS
          && variantBeforePromotion == 3;
      require(cachedAndPendingPrepared, "could not prepare both cached and pending P3 state");
      metrics.promotionBytesBefore = promotionBacking.backingBytesForTest();
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
      metrics.promotionElapsedMillis = Vm.getTimeStamp() - promotionStart;
      NativeImageBacking promotedBacking = nativeBacking(alphaPng);
      metrics.promotionBytesAfter = promotedBacking.backingBytesForTest();
      metrics.promotionFormat = promotedBacking.formatForTest();
      metrics.promotionGenerationAdvanced = alphaPng.backingMutationGenerationForP2() > generationBefore;
      metrics.promotionOpacityPreserved = alphaPng.backing.opacityState() == opacityBefore;
      metrics.promotionVariantsCleared = promotedBacking.variantStateForTest() == 0;
      metrics.promotionReadbackError = maxChannelError(pixelsBeforePromotion, alphaPng.getPixels());
      metrics.cachedSiblingPreserved = cachedSiblingBacking instanceof NativeImageBacking
          && cachedSiblingBacking != promotedBacking
          && ((NativeImageBacking) cachedSiblingBacking).isCompact();
      boolean retrySucceeded = !promotedBacking.isCompact()
          && metrics.promotionFormat == NativeImageBacking.FORMAT_RGBA8888
          && metrics.promotionGenerationAdvanced && metrics.promotionOpacityPreserved && metrics.promotionVariantsCleared
          && metrics.promotionReadbackError == 0 && metrics.cachedSiblingPreserved;
      require(retrySucceeded, "promotion retry did not preserve state or clear variants");
      long attemptsAfterPromotion = metric(NativeImageBacking.TEST_METRIC_PROMOTION_ATTEMPTS, 0);
      alphaPng.getGraphics().fillRect(0, 0, 1, 1);
      promotionRetry = !nativeBacking(alphaPng).isCompact()
          && alphaPng.backingMutationGenerationForP2() > generationBefore
          && metric(NativeImageBacking.TEST_METRIC_PROMOTION_ATTEMPTS, 0) == attemptsAfterPromotion;
      p3Invalidation = retrySucceeded && cachedAndPendingPrepared && failurePreserved
          && variantBeforePromotion == 3;
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
      ImageCompactStorageSmokeSupport.captureAccounting(metrics, compactDecodeFixtureCount);
      compactMetrics = metrics.compactMetrics;
      require(compactMetrics, "compact storage lifecycle metrics were incomplete or inconsistent");
      stage = "compact-decode-allocation-failure-retry";
      compactDecodeFailureRetry = compactDecodeFailureRetry("opaque-color.png")
          && compactDecodeFailureRetry("opaque-color.jpg");
      require(compactDecodeFailureRetry,
          "compact decoder candidate was not released exactly once or could not be retried");

      stage = "p3-compact-source-variants";
      p3PhysicalVariantReuse = ImageCompactStorageP3SmokeSupport.physicalVariantReuse();
      require(p3PhysicalVariantReuse, "compact source physical variant was not reused safely");
      p3TargetColorVariant = ImageCompactStorageP3SmokeSupport.targetColorVariantPreservesCompactSource();
      require(p3TargetColorVariant, "target-color variant replaced or promoted the compact source");
      p3IncompatibleFallback = ImageCompactStorageP3SmokeSupport.incompatiblePhysicalFallbackPreservesCompactSource();
      require(p3IncompatibleFallback, "incompatible physical path did not fall back with compact pixels intact");

      stage = "p6-compact-source-integration";
      p6Rgb565DirectCopy = ImageCompactStorageP3SmokeSupport.p6Rgb565DirectCopyPreservesCompactSource();
      require(p6Rgb565DirectCopy, "P6 did not directly copy a compatible compact RGB565 source");
      p6Gray8Fallback = ImageCompactStorageP3SmokeSupport.p6Gray8FallbackPreservesCompactSource();
      require(p6Gray8Fallback, "P6 did not preserve the GRAY8 fallback source");
      p6Argb4444Fallback = ImageCompactStorageP3SmokeSupport.p6Argb4444FallbackPreservesCompactSource();
      require(p6Argb4444Fallback, "P6 did not preserve the ARGB4444 fallback source");
      p6TargetColorCopyRect = ImageCompactStorageP3SmokeSupport.p6TargetColorCopyRectPreservesCompactSource();
      require(p6TargetColorCopyRect, "P6 copyRect bypassed P3 target-color variant reuse");
      p6CachedFinalReuse = ImageCompactStorageP3SmokeSupport.p6CachedFinalReusePreservesCompactSource();
      require(p6CachedFinalReuse, "P6 did not reuse the compact source cached final raster");
    } catch (Throwable failure) {
      error = "stage=" + stage + "," + failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean overallPass = policy && standardBaselineRgba && formatsAndAccounting
        && observersAndEncoding && drawParity
        && alphaQuality && promotionRetry && p3Invalidation && directMutationPromotion
        && graphicsMutationPromotion && compactMetrics && compactDecodeFailureRetry
        && p3PhysicalVariantReuse && p3TargetColorVariant && p3IncompatibleFallback
        && p6Rgb565DirectCopy && p6Gray8Fallback && p6Argb4444Fallback
        && p6TargetColorCopyRect && p6CachedFinalReuse;
    System.out.println("fixture=ImageCompactStorageSmokeApp,policy=" + policy
        + ",standardBaselineRgba=" + standardBaselineRgba
        + ",formatsAndAccounting=" + formatsAndAccounting
        + ",observersAndEncoding=" + observersAndEncoding + ",equalityHash=" + equalityHash
        + ",drawParity=" + drawParity
        + ",alphaQuality=" + alphaQuality + ",promotionRetry=" + promotionRetry
        + ",p3Invalidation=" + p3Invalidation + ",directMutationPromotion="
        + directMutationPromotion + ",graphicsMutationPromotion=" + graphicsMutationPromotion
        + ",p3PhysicalVariantReuse=" + p3PhysicalVariantReuse
        + ",p3TargetColorVariant=" + p3TargetColorVariant
        + ",p3IncompatibleFallback=" + p3IncompatibleFallback
        + ",p6Rgb565DirectCopy=" + p6Rgb565DirectCopy
        + ",p6Gray8Fallback=" + p6Gray8Fallback
        + ",p6Argb4444Fallback=" + p6Argb4444Fallback
        + ",p6TargetColorCopyRect=" + p6TargetColorCopyRect
        + ",p6CachedFinalReuse=" + p6CachedFinalReuse
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
        + ",liveBytesByFormat=" + metrics.liveRgbaBytes + "/" + metrics.liveRgb565Bytes + "/"
        + metrics.liveGrayBytes + "/" + metrics.liveArgb4444Bytes
        + ",peakBytesByFormat=" + metrics.peakRgbaBytes + "/" + metrics.peakRgb565Bytes + "/"
        + metrics.peakGrayBytes + "/" + metrics.peakArgb4444Bytes
        + ",compactDecodeCountByFormat=" + metrics.rgb565DecodeCount + "/" + metrics.grayDecodeCount + "/"
        + metrics.argb4444DecodeCount
        + ",compactDecodeBytesByFormat=" + metrics.rgb565DecodeBytes + "/" + metrics.grayDecodeBytes + "/"
        + metrics.argb4444DecodeBytes + ",compactReadbackCount=" + metrics.compactReadbackCount
        + ",rowScratchPeakBytes=" + metrics.rowScratchPeakBytes
        + ",fullRgbaDecodeTempBytes=" + metrics.fullRgbaDecodeTempBytes
        + ",promotionAttempts=" + metrics.promotionAttempts + ",promotionSuccesses=" + metrics.promotionSuccesses
        + ",promotionFailures=" + metrics.promotionFailures + ",promotionBytes=" + metrics.promotionBytes
        + ",promotionBytesBefore=" + metrics.promotionBytesBefore + ",promotionBytesAfter=" + metrics.promotionBytesAfter
        + ",promotionElapsedMillis=" + metrics.promotionElapsedMillis
        + ",promotionFormat=" + metrics.promotionFormat + ",promotionGenerationAdvanced="
        + metrics.promotionGenerationAdvanced + ",promotionOpacityPreserved=" + metrics.promotionOpacityPreserved
        + ",promotionVariantsCleared=" + metrics.promotionVariantsCleared + ",promotionReadbackError="
        + metrics.promotionReadbackError + ",cachedSiblingPreserved=" + metrics.cachedSiblingPreserved
        + ",grayBytes=" + (GRAY_ROW_BYTES * HEIGHT) + ",alphaBytes=" + (COLOR_ROW_BYTES * HEIGHT)
        + ",rgbaBytes=" + (WIDTH * 4 * HEIGHT) + ",overallPass=" + overallPass
        + (error.length() == 0 ? "" : ",error=" + error));
    System.out.flush();
    exit(overallPass ? 0 : 1);
  }


}
