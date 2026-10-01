// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.io.ByteArrayStream;
import totalcross.sys.RuntimeDiagnosticSnapshot;
import totalcross.sys.RuntimeDiagnostics;
import totalcross.sys.runtime.RuntimeConfigurationReport;
import totalcross.ui.MainWindow;

/** macOS smoke for P2 raster backing, writes, readback, color mutation, and diagnostics. */
public final class ImageRasterCoreSmokeApp extends MainWindow {
  @Override
  public void initUI() {
    boolean pngParity = false;
    boolean jpegOpaque = false;
    boolean opaqueWrite = false;
    boolean alphaWriteFallback = false;
    boolean colorMutation = false;
    boolean mutationReadDraw = false;
    boolean physicalIdentity = false;
    boolean variantAdmission = false;
    boolean variantMutationInvalidation = false;
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
        && runtimeConfiguration && diagnostics;
    System.out.println("fixture=ImageRasterCoreSmokeApp,pngParity=" + pngParity + ",jpegOpaque=" + jpegOpaque
        + ",opaqueWrite=" + opaqueWrite + ",alphaWriteFallback=" + alphaWriteFallback
        + ",colorMutation=" + colorMutation + ",mutationReadDraw=" + mutationReadDraw
        + ",physicalIdentity=" + physicalIdentity + ",identityHitCount="
        + Image.physicalIdentityHitCountForTest() + ",identityMaterializations="
        + Image.nativeGeometryMaterializationCountForTest()
        + ",variantAdmission=" + variantAdmission + ",variantMutationInvalidation="
        + variantMutationInvalidation
        + ",runtimeConfiguration=" + runtimeConfiguration + ",diagnostics=" + diagnostics
        + ",overallPass=" + overallPass + (error.length() == 0 ? "" : ",error=" + error));
    System.out.flush();
    exit(overallPass ? 0 : 1);
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
