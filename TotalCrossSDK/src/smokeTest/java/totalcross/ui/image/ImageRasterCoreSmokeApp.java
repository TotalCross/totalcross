// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.io.ByteArrayStream;
import totalcross.sys.RuntimeDiagnosticSnapshot;
import totalcross.sys.RuntimeDiagnostics;
import totalcross.sys.Vm;
import totalcross.sys.runtime.ImageRasterSmokeTestSupport;
import totalcross.sys.runtime.RuntimeConfigurationReport;
import totalcross.ui.MainWindow;

/** macOS smoke for P2 backing state and P3 raster variant behavior. */
public final class ImageRasterCoreSmokeApp extends MainWindow {
  private static long physicalVariantHitLoopMillis;

  @Override
  public void initUI() {
    boolean pngParity = false;
    boolean jpegOpaque = false;
    boolean opaqueWrite = false;
    boolean alphaWriteFallback = false;
    boolean colorMutation = false;
    boolean mutationReadDraw = false;
    boolean physicalIdentity = false;
    int identityHitCount = 0;
    int identityMaterializations = 0;
    boolean variantAdmission = false;
    boolean variantMutationInvalidation = false;
    boolean targetColorVariants = false;
    boolean physicalVariants = false;
    boolean runtimeConfiguration = false;
    boolean diagnostics = false;
    String error = "";
    try {
      boolean diagnosticsSupported = RuntimeDiagnostics.isSupported();
      RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.IMAGE, true);

      Image pngSource = new Image(2, 2);
      int[] opaquePixels = {
          0xFF123456, 0xFFABCDEF,
          0xFF204060, 0xFF708090
      };
      opaqueWrite = pngSource.getGraphics().setRGB(opaquePixels, 0, 0, 0, 2, 2) == 4
          && pngSource.opacityStateForP2() == ImageBacking.OPACITY_OPAQUE;
      require(opaqueWrite, "opaque setRGB count");
      ByteArrayStream pngBytes = new ByteArrayStream(128);
      pngSource.createPng(pngBytes);
      Image decodedPng = new Image(pngBytes.getBuffer(), pngBytes.getPos());
      int[] decodedPngPixels = decodedPng.getPixels();
      pngParity = same(opaquePixels, decodedPngPixels);
      require(pngParity, "PNG decoded pixels differ expected=" + java.util.Arrays.toString(opaquePixels)
          + " actual=" + java.util.Arrays.toString(decodedPngPixels));

      Image alphaSource = new Image(2, 1);
      int[] alphaPixels = {0x80112233, 0x7F445566};
      alphaWriteFallback = alphaSource.getGraphics().setRGB(alphaPixels, 0, 0, 0, 2, 1) == 2
          && same(alphaPixels, alphaSource.getPixels());
      require(alphaWriteFallback, "alpha write fallback changed ARGB pixels");

      Image jpegSource = new Image(2, 2);
      assertEquals(4, jpegSource.getGraphics().setRGB(opaquePixels, 0, 0, 0, 2, 2), "JPEG source setRGB");
      ByteArrayStream jpegBytes = new ByteArrayStream(256);
      jpegSource.createJpg(jpegBytes, 100);
      Image decodedJpeg = new Image(jpegBytes.getBuffer(), jpegBytes.getPos());
      int[] jpegPixels = decodedJpeg.getPixels();
      jpegOpaque = decodedJpeg.getPixelWidth() == 2 && decodedJpeg.getPixelHeight() == 2
          && jpegPixels.length == 4 && allOpaque(jpegPixels)
          && same(jpegPixels, new Image(jpegBytes.getBuffer(), jpegBytes.getPos()).getPixels());
      require(jpegOpaque, "JPEG dimensions, alpha, or repeat decode mismatch");

      int[] colorExpected = opaquePixels.clone();
      applyColor2(colorExpected, 0xFF6090C0);
      Image colorized = new Image(pngBytes.getBuffer(), pngBytes.getPos());
      colorized.applyColor2(0xFF6090C0);
      colorMutation = same(colorExpected, colorized.getPixels());
      require(colorMutation, "APPLY_COLOR2 output differs");

      byte[] row = new byte[8];
      decodedPng.getPixelRow(row, 0);
      Image target = new Image(2, 2);
      target.getGraphics().drawImage(decodedPng, 0, 0, true);
      mutationReadDraw = same(opaquePixels, target.getPixels())
          && (row[0] & 0xFF) == 0x12 && (row[1] & 0xFF) == 0x34
          && (row[2] & 0xFF) == 0x56 && (row[3] & 0xFF) == 0xFF;
      require(mutationReadDraw, "readback after write/draw mismatch");

      Image.resetImageOperationAccountingForTest();
      Image identitySource = new Image(2, 2);
      identitySource.getGraphics().setRGB(opaquePixels, 0, 0, 0, 2, 2);
      Image identityDeferred = identitySource.getAlphaInstance(0);
      Image identityTarget = new Image(2, 2);
      identityTarget.getGraphics().drawImage(identityDeferred, 0, 0, true);
      physicalIdentity = same(opaquePixels, identityTarget.getPixels())
          && Image.physicalIdentityHitCountForTest() == 1
          && Image.nativeGeometryMaterializationCountForTest() == 0;
      identityHitCount = Image.physicalIdentityHitCountForTest();
      identityMaterializations = Image.nativeGeometryMaterializationCountForTest();
      require(physicalIdentity, "exact physical identity did not draw directly");

      Image variantSource = new Image(2, 2);
      NativeImageBacking variantBacking = (NativeImageBacking) variantSource.backing;
      int[] firstKey = {0x12345678, 0x00000001};
      int[] otherKey = {0x12345678, 0x00000002};
      int first = variantBacking.observeVariantForTest(NativeImageBacking.RASTER_VARIANT_PHYSICAL, firstKey);
      boolean pending = variantBacking.variantStateForTest() == 2;
      int different = variantBacking.observeVariantForTest(NativeImageBacking.RASTER_VARIANT_PHYSICAL, otherKey);
      int returnToFirst = variantBacking.observeVariantForTest(NativeImageBacking.RASTER_VARIANT_PHYSICAL, firstKey);
      int admitted = variantBacking.observeVariantForTest(NativeImageBacking.RASTER_VARIANT_PHYSICAL, firstKey);
      int hit = variantBacking.observeVariantForTest(NativeImageBacking.RASTER_VARIANT_PHYSICAL, firstKey);
      variantAdmission = first == NativeImageBacking.RASTER_VARIANT_MISS && pending
          && different == NativeImageBacking.RASTER_VARIANT_MISS
          && returnToFirst == NativeImageBacking.RASTER_VARIANT_MISS
          && admitted == NativeImageBacking.RASTER_VARIANT_MATERIALIZE
          && hit == NativeImageBacking.RASTER_VARIANT_HIT
          && variantBacking.variantStateForTest() == 1;
      require(variantAdmission, "exact-key second-use admission did not produce one slot and a hit");
      variantSource.recordBackingMutation(ImageBacking.OPACITY_UNKNOWN);
      variantMutationInvalidation = variantBacking.variantStateForTest() == 0;
      require(variantMutationInvalidation, "backing mutation retained raster variant state");
      variantBacking.release();
      variantMutationInvalidation = variantMutationInvalidation && variantBacking.variantStateForTest() == 0;
      Image crossKindSource = new Image(2, 2);
      NativeImageBacking crossKindBacking = (NativeImageBacking) crossKindSource.backing;
      int targetColorFirst = crossKindBacking.observeVariantForTest(
          NativeImageBacking.RASTER_VARIANT_TARGET_COLOR, firstKey);
      int physicalSecond = crossKindBacking.observeVariantForTest(
          NativeImageBacking.RASTER_VARIANT_PHYSICAL, firstKey);
      int targetColorThird = crossKindBacking.observeVariantForTest(
          NativeImageBacking.RASTER_VARIANT_TARGET_COLOR, firstKey);
      int physicalFourth = crossKindBacking.observeVariantForTest(
          NativeImageBacking.RASTER_VARIANT_PHYSICAL, firstKey);
      int physicalAdmitted = crossKindBacking.observeVariantForTest(
          NativeImageBacking.RASTER_VARIANT_PHYSICAL, firstKey);
      variantAdmission = variantAdmission
          && targetColorFirst == NativeImageBacking.RASTER_VARIANT_MISS
          && physicalSecond == NativeImageBacking.RASTER_VARIANT_MISS
          && targetColorThird == NativeImageBacking.RASTER_VARIANT_MISS
          && physicalFourth == NativeImageBacking.RASTER_VARIANT_MISS
          && physicalAdmitted == NativeImageBacking.RASTER_VARIANT_MATERIALIZE;
      crossKindBacking.release();

      targetColorVariants = checkTargetColorVariants();
      require(targetColorVariants, "target-color variant lifecycle or output parity failed");
      physicalVariants = checkPhysicalVariants();
      require(physicalVariants, "physical variant admission, hit, or output parity failed");

      String configuration = RuntimeConfigurationReport.describe();
      runtimeConfiguration = configuration.contains("rasterCore:")
          && configuration.contains("zeroCopyDecode: enabled")
          && configuration.contains("physicalIdentity: enabled");
      require(runtimeConfiguration, "P1 Image runtime configuration report mismatch");

      if (diagnosticsSupported) {
        RuntimeDiagnosticSnapshot snapshot = RuntimeDiagnostics.snapshot();
        diagnostics = snapshot.getValue(RuntimeDiagnosticSnapshot.Domain.IMAGE,
            RuntimeDiagnosticSnapshot.Kind.COUNTER) >= 6;
      } else {
        diagnostics = RuntimeDiagnostics.snapshot().size() == 0;
      }
      require(diagnostics, diagnosticsSupported
          ? "Image aggregate diagnostics were not collected" : "disabled diagnostics returned metrics");
      RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.IMAGE, false);
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean overallPass = pngParity && jpegOpaque && opaqueWrite && alphaWriteFallback && colorMutation
        && mutationReadDraw && physicalIdentity && variantAdmission && variantMutationInvalidation
        && targetColorVariants && physicalVariants && runtimeConfiguration && diagnostics;
    System.out.println("fixture=ImageRasterCoreSmokeApp,pngParity=" + pngParity + ",jpegOpaque=" + jpegOpaque
        + ",opaqueWrite=" + opaqueWrite + ",alphaWriteFallback=" + alphaWriteFallback
        + ",colorMutation=" + colorMutation + ",mutationReadDraw=" + mutationReadDraw
        + ",physicalIdentity=" + physicalIdentity + ",identityHitCount="
        + identityHitCount + ",identityMaterializations=" + identityMaterializations
        + ",variantAdmission=" + variantAdmission + ",variantMutationInvalidation="
        + variantMutationInvalidation + ",targetColorVariants=" + targetColorVariants
        + ",physicalVariants=" + physicalVariants + ",physicalVariantMaterializations="
        + Image.physicalVariantMaterializationCountForTest() + ",physicalVariantHits="
        + Image.physicalVariantHitCountForTest() + ",physicalVariantHitLoopMillis="
        + physicalVariantHitLoopMillis
        + ",runtimeConfiguration=" + runtimeConfiguration + ",diagnostics=" + diagnostics
        + ",overallPass=" + overallPass + (error.length() == 0 ? "" : ",error=" + error));
    System.out.flush();
    exit(overallPass ? 0 : 1);
  }

  private static boolean checkTargetColorVariants() throws Exception {
    final int[] pixels = {0xFF000000, 0xFFFFFFFF, 0xFF00FF00, 0xFF0000FF};
    Image.resetImageOperationAccountingForTest();
    setRasterFeaturesForTest(false, true, false);

    Image source = rasterSource(pixels);
    Image deferred = source.getAlphaInstance(0);
    Image cachedSource = materializePipelineRoot(deferred);
    NativeImageBacking sourceBacking = (NativeImageBacking) cachedSource.backing;
    int sourceColorType = sourceBacking.colorTypeForTest();
    Image bgraTarget = new Image(2, 2);
    NativeImageBacking bgraBacking = replaceTargetBacking(bgraTarget,
        NativeImageBacking.TEST_COLOR_BGRA_8888);
    bgraTarget.getGraphics().drawImage(deferred, 0, 0, true);
    int firstUseState = sourceBacking.variantStateForTest();
    boolean firstUsePending = (firstUseState & 2) != 0;
    bgraTarget.getGraphics().drawImage(deferred, 0, 0, true);
    int secondUseState = sourceBacking.variantStateForTest();
    boolean secondUseStored = secondUseState == 1;
    bgraTarget.getGraphics().drawImage(deferred, 0, 0, true);
    boolean bgraParity = same(pixels, bgraTarget.getPixels())
        && sourceBacking.colorTypeForTest() == sourceColorType
        && bgraBacking.colorTypeForTest() != sourceColorType
        && Image.targetColorVariantMaterializationCountForTest() == 1
        && Image.targetColorVariantHitCountForTest() == 1
        && firstUsePending && secondUseStored;
    require(bgraParity, "BGRA variant parity/state failed: sourceType=" + sourceColorType
        + ",targetType=" + bgraBacking.colorTypeForTest() + ",state="
        + sourceBacking.variantStateForTest() + ",materializations="
        + Image.targetColorVariantMaterializationCountForTest() + ",hits="
        + Image.targetColorVariantHitCountForTest() + ",firstPending=" + firstUsePending
        + ",firstState=" + firstUseState + ",secondStored=" + secondUseStored
        + ",secondState=" + secondUseState + ",pixels="
        + same(pixels, bgraTarget.getPixels()));

    cachedSource.recordBackingMutation(ImageBacking.OPACITY_UNKNOWN);
    boolean mutationCleared = sourceBacking.variantStateForTest() == 0;
    require(mutationCleared, "target-color cache survived pipeline-root mutation: state="
        + sourceBacking.variantStateForTest());

    setRasterFeaturesForTest(false, false, false);
    Image referenceSource = rasterSource(pixels).getScaledInstance(3, 3);
    Image referenceTarget = new Image(3, 3);
    replaceTargetBacking(referenceTarget, NativeImageBacking.TEST_COLOR_RGB_565);
    referenceTarget.getGraphics().drawImage(referenceSource, 0, 0, true);
    int[] expected565 = referenceTarget.getPixels();

    setRasterFeaturesForTest(false, true, false);
    Image opaqueSource = rasterSource(pixels);
    Image opaqueDeferred = opaqueSource.getScaledInstance(3, 3);
    Image opaqueCachedSource = materializePipelineRoot(opaqueDeferred);
    NativeImageBacking opaqueBacking = (NativeImageBacking) opaqueCachedSource.backing;
    Image rgb565Target = new Image(3, 3);
    NativeImageBacking rgb565Backing = replaceTargetBacking(rgb565Target,
        NativeImageBacking.TEST_COLOR_RGB_565);
    rgb565Target.getGraphics().drawImage(opaqueDeferred, 0, 0, true);
    rgb565Target.getGraphics().drawImage(opaqueDeferred, 0, 0, true);
    rgb565Target.getGraphics().drawImage(opaqueDeferred, 0, 0, true);
    boolean rgb565Parity = same(expected565, rgb565Target.getPixels())
        && rgb565Backing.colorTypeForTest() != opaqueBacking.colorTypeForTest()
        && opaqueBacking.variantStateForTest() == 1;
    require(rgb565Parity, "RGB565 variant parity/state failed: targetType="
        + rgb565Backing.colorTypeForTest() + ",sourceType=" + opaqueBacking.colorTypeForTest()
        + ",state=" + opaqueBacking.variantStateForTest() + ",opacity="
        + opaqueCachedSource.opacityStateForP2() + ",materializations="
        + Image.targetColorVariantMaterializationCountForTest() + ",hits="
        + Image.targetColorVariantHitCountForTest() + ",fallbacks="
        + Image.targetColorVariantFallbackCountForTest() + ",pixels="
        + same(expected565, rgb565Target.getPixels()));

    Image alphaSource = new Image(2, 2);
    alphaSource.getGraphics().setRGB(new int[] {0x80112233, 0xFF445566, 0x7F778899, 0xFFABCDEF},
        0, 0, 0, 2, 2);
    Image alphaDeferred = alphaSource.getAlphaInstance(0);
    NativeImageBacking alphaBacking = (NativeImageBacking) materializePipelineRoot(alphaDeferred).backing;
    Image alphaTarget = new Image(2, 2);
    replaceTargetBacking(alphaTarget, NativeImageBacking.TEST_COLOR_RGB_565);
    alphaTarget.getGraphics().drawImage(alphaDeferred, 0, 0, true);
    boolean alphaRejected = alphaBacking.variantStateForTest() == 0;
    require(alphaRejected, "alpha-bearing source was admitted to RGB565 target variant");

    Image unsupportedSource = rasterSource(pixels);
    Image unsupportedDeferred = unsupportedSource.getAlphaInstance(0);
    NativeImageBacking unsupportedBacking = (NativeImageBacking) materializePipelineRoot(unsupportedDeferred).backing;
    Image unsupportedTarget = new Image(2, 2);
    replaceTargetBacking(unsupportedTarget, NativeImageBacking.TEST_COLOR_ALPHA_8);
    unsupportedTarget.getGraphics().drawImage(unsupportedDeferred, 0, 0, true);
    boolean unsupportedRejected = unsupportedBacking.variantStateForTest() == 0;
    require(unsupportedRejected, "unsupported target type was admitted to color variant");

    Image failedSource = rasterSource(pixels);
    Image failedDeferred = failedSource.getAlphaInstance(0);
    NativeImageBacking failedBacking = (NativeImageBacking) materializePipelineRoot(failedDeferred).backing;
    Image failedTarget = new Image(2, 2);
    replaceTargetBacking(failedTarget, NativeImageBacking.TEST_COLOR_BGRA_8888);
    failedTarget.getGraphics().drawImage(failedDeferred, 0, 0, true);
    NativeImageBacking.failNextVariantMaterializationForTest();
    failedTarget.getGraphics().drawImage(failedDeferred, 0, 0, true);
    boolean failureFallback = failedBacking.variantStateForTest() == 0
        && Image.targetColorVariantFallbackCountForTest() >= 2
        && same(pixels, failedTarget.getPixels());
    require(failureFallback, "target-color allocation failure did not clear and fall back: state="
        + failedBacking.variantStateForTest() + ",fallbacks="
        + Image.targetColorVariantFallbackCountForTest() + ",pixels="
        + same(pixels, failedTarget.getPixels()));
    require(alphaRejected && unsupportedRejected && failureFallback,
        "target-color fallback guard failed: alphaState=" + alphaBacking.variantStateForTest()
            + ",unsupportedState=" + unsupportedBacking.variantStateForTest()
            + ",failureState=" + failedBacking.variantStateForTest()
            + ",fallbacks=" + Image.targetColorVariantFallbackCountForTest()
            + ",failurePixels=" + same(pixels, failedTarget.getPixels()));

    setRasterFeaturesForTest(true, false, false);
    return bgraParity && mutationCleared && rgb565Parity && alphaRejected
        && unsupportedRejected && failureFallback;
  }

  private static Image rasterSource(int[] pixels) throws Exception {
    Image source = new Image(2, 2);
    require(source.getGraphics().setRGB(pixels, 0, 0, 0, 2, 2) == pixels.length,
        "test source pixels were not written");
    return source;
  }

  private static Image materializePipelineRoot(Image deferred) throws Exception {
    BackingImageSource root = (BackingImageSource) deferred.pipelineForSmoke().root();
    return Image.materializeBackingSource(root);
  }

  private static boolean checkPhysicalVariants() throws Exception {
    final int[] pixels = {0xFF102030, 0xFF405060, 0xFF8090A0, 0xFFE0F000};
    Image.resetImageOperationAccountingForTest();
    setRasterFeaturesForTest(true, false, true);
    Image identitySource = rasterSource(pixels);
    Image identityDeferred = identitySource.getAlphaInstance(0);
    NativeImageBacking identityBacking = (NativeImageBacking) materializePipelineRoot(identityDeferred).backing;
    Image identityTarget = new Image(2, 2);
    identityTarget.getGraphics().drawImage(identityDeferred, 0, 0, true);
    boolean identityBeforeCache = Image.physicalIdentityHitCountForTest() == 1
        && Image.physicalVariantMaterializationCountForTest() == 0
        && identityBacking.variantStateForTest() == 0;

    setRasterFeaturesForTest(true, false, false);
    Image reference = rasterSource(pixels).getSmoothScaledInstance(4, 4);
    Image referenceTarget = new Image(8, 8);
    for (int i = 0; i < 3; i++) {
      referenceTarget.getGraphics().drawImage(reference, 1, 1, true);
    }
    int[] expected = referenceTarget.getPixels();

    Image.resetImageOperationAccountingForTest();
    setRasterFeaturesForTest(true, false, true);
    Image source = rasterSource(pixels);
    Image deferred = source.getSmoothScaledInstance(4, 4);
    Image cachedSource = materializePipelineRoot(deferred);
    NativeImageBacking sourceBacking = (NativeImageBacking) cachedSource.backing;
    Image target = new Image(8, 8);
    target.getGraphics().drawImage(deferred, 1, 1, true);
    boolean firstUsePending = (sourceBacking.variantStateForTest() & 2) != 0
        && Image.physicalVariantMaterializationCountForTest() == 0
        && Image.physicalVariantFallbackCountForTest() == 1;
    target.getGraphics().drawImage(deferred, 1, 1, true);
    boolean secondUseMaterialized = sourceBacking.variantStateForTest() == 1
        && Image.physicalVariantMaterializationCountForTest() == 1;
    target.getGraphics().drawImage(deferred, 1, 1, true);
    boolean hit = Image.physicalVariantHitCountForTest() == 1;
    boolean parity = same(expected, target.getPixels())
        && Image.nativeGeometryMaterializationCountForTest() == 0;
    int hitLoopStart = Vm.getTimeStamp();
    for (int i = 0; i < 1000; i++) {
      target.getGraphics().drawImage(deferred, 1, 1, true);
    }
    physicalVariantHitLoopMillis = Vm.getTimeStamp() - hitLoopStart;
    boolean repeatedHits = Image.physicalVariantHitCountForTest() == 1001;

    target.getGraphics().drawImage(deferred, 2, 1, true);
    boolean keyReplacedPending = (sourceBacking.variantStateForTest() & 3) == 3;
    target.getGraphics().drawImage(deferred, 2, 1, true);
    boolean keyReplacedSlot = sourceBacking.variantStateForTest() == 1
        && Image.physicalVariantMaterializationCountForTest() == 2;
    cachedSource.recordBackingMutation(ImageBacking.OPACITY_UNKNOWN);
    boolean mutationCleared = sourceBacking.variantStateForTest() == 0;

    setRasterFeaturesForTest(true, false, false);
    return identityBeforeCache && firstUsePending && secondUseMaterialized && hit && parity && repeatedHits
        && keyReplacedPending && keyReplacedSlot && mutationCleared;
  }

  private static NativeImageBacking replaceTargetBacking(Image target, int colorType) throws Exception {
    NativeImageBacking previous = (NativeImageBacking) target.backing;
    NativeImageBacking replacement = NativeImageBacking.createEmptyWithColorTypeForTest(
        target.getPixelWidth(), target.getPixelHeight(), colorType);
    target.backing = replacement;
    previous.release();
    return replacement;
  }

  private static void setRasterFeaturesForTest(boolean physicalIdentity,
      boolean targetColorConversion, boolean physicalVariantCache) throws Exception {
    ImageRasterSmokeTestSupport.setRasterFeatures(physicalIdentity, targetColorConversion,
        physicalVariantCache);
  }

  private static boolean allOpaque(int[] pixels) {
    for (int pixel : pixels) {
      if ((pixel >>> 24) != 0xFF) {
        return false;
      }
    }
    return true;
  }

  private static boolean same(int[] first, int[] second) {
    return java.util.Arrays.equals(first, second);
  }

  private static void applyColor2(int[] pixels, int color) {
    int targetRed = (color >> 16) & 0xFF;
    int targetGreen = (color >> 8) & 0xFF;
    int targetBlue = color & 0xFF;
    int highestBrightness = 0;
    int highestPixel = 0;
    for (int pixel : pixels) {
      if ((pixel >>> 24) == 0xFF) {
        int rgb = pixel & 0x00FFFFFF;
        int brightness = totalcross.ui.gfx.Color.getBrightness(rgb);
        if (brightness > highestBrightness) {
          highestBrightness = brightness;
          highestPixel = rgb;
        }
      }
    }
    int highestRed = (highestPixel >> 16) & 0xFF;
    int highestGreen = (highestPixel >> 8) & 0xFF;
    int highestBlue = highestPixel & 0xFF;
    if (highestRed == 0) highestRed = 255;
    if (highestGreen == 0) highestGreen = 255;
    if (highestBlue == 0) highestBlue = 255;
    for (int i = 0; i < pixels.length; i++) {
      int pixel = pixels[i];
      if ((pixel >>> 24) == 0) continue;
      int red = Math.min(255, ((pixel >> 16) & 0xFF) * targetRed / highestRed);
      int green = Math.min(255, ((pixel >> 8) & 0xFF) * targetGreen / highestGreen);
      int blue = Math.min(255, (pixel & 0xFF) * targetBlue / highestBlue);
      pixels[i] = (pixel & 0xFF000000) | (red << 16) | (green << 8) | blue;
    }
  }

  private static void assertEquals(int expected, int actual, String message) {
    if (expected != actual) {
      throw new IllegalStateException(message + ": expected=" + expected + ",actual=" + actual);
    }
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
