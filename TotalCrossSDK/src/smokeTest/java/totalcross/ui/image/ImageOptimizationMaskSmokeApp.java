// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Vm;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** Native macOS diagnostic for Java-to-native image optimization mask transport. */
public class ImageOptimizationMaskSmokeApp extends MainWindow {
  private static final long EXPECTED_MASK = ImageOptimizationSettings.DEFAULT_EFFECTIVE_MASK;
  private static final long DECODE_ZERO_COPY_BIT = 1L << ImageOptimizationSettings.DECODE_ZERO_COPY;
  private static final long PHYSICAL_IDENTITY_BIT =
      1L << ImageOptimizationSettings.RASTER_PHYSICAL_IDENTITY_FOLDING;
  private static final long TARGET_COLORTYPE_BIT =
      1L << ImageOptimizationSettings.RASTER_TARGET_COLORTYPE_CONVERSION;

  @Override
  public void initUI() {
    long javaDrawMask = -1;
    long javaDecodeMask = -1;
    int exactDrawMask = -2;
    int exactDecodeMask = -2;
    int subclassDrawMask = -2;
    int subclassDecodeMask = -2;
    String exactRuntimeClass = "";
    String subclassRuntimeClass = "";
    DecodeResult zeroCopyDefault = DecodeResult.empty();
    DecodeResult zeroCopyDisabled = DecodeResult.empty();
    DecodeResult zeroCopyRestored = DecodeResult.empty();
    PhysicalIdentityResult physicalDefault = PhysicalIdentityResult.empty();
    PhysicalIdentityResult physicalDisabled = PhysicalIdentityResult.empty();
    PhysicalIdentityResult physicalRestored = PhysicalIdentityResult.empty();
    long targetDefaultMask = -1;
    long targetEnabledMask = -1;
    long targetRestoredMask = -1;
    String error = "";
    boolean javaMaskPass = false;
    boolean exactRuntimePass = false;
    boolean probePass = false;
    boolean zeroCopyPass = false;
    boolean physicalIdentityPass = false;
    boolean defaultDisabledPass = false;
    boolean diagnosticComplete = false;

    try {
      ImageOptimizationSettings.resetForTest();
      javaDrawMask = Image.nativeOptimizationMaskForDrawForTest();
      javaDecodeMask = Image.nativeOptimizationMaskForDecodeForTest();
      javaMaskPass = javaDrawMask == EXPECTED_MASK && javaDecodeMask == EXPECTED_MASK;

      Image exact = new Image(1, 1);
      Image subclass = new SmokeImage();
      exactRuntimeClass = exact.getClass().getName();
      subclassRuntimeClass = subclass.getClass().getName();
      exactRuntimePass = Image.class.getName().equals(exactRuntimeClass)
          && subclassRuntimeClass.length() > 0;
      exactDrawMask = Image.nativeOptimizationMaskObservedForTest(exact, true);
      exactDecodeMask = Image.nativeOptimizationMaskObservedForTest(exact, false);
      subclassDrawMask = Image.nativeOptimizationMaskObservedForTest(subclass, true);
      subclassDecodeMask = Image.nativeOptimizationMaskObservedForTest(subclass, false);
      probePass = exactDrawMask == javaDrawMask && exactDecodeMask == javaDecodeMask
          && subclassDrawMask == javaDrawMask && subclassDecodeMask == javaDecodeMask
          && exactDrawMask >= 0 && exactDecodeMask >= 0
          && subclassDrawMask >= 0 && subclassDecodeMask >= 0;

      byte[] png = Vm.getFile("image-abi/tiny.png");
      require(png != null && png.length > 0, "zero-copy fixture");
      zeroCopyDefault = decodeAfterState(png, ImageOptimizationSettings.DEFAULT);
      long defaultDecodeMask = Image.nativeOptimizationMaskForDecodeForTest();
      zeroCopyDisabled = decodeAfterState(png, ImageOptimizationSettings.DISABLED);
      long disabledDecodeMask = Image.nativeOptimizationMaskForDecodeForTest();
      zeroCopyRestored = decodeAfterState(png, ImageOptimizationSettings.DEFAULT);
      long restoredDecodeMask = Image.nativeOptimizationMaskForDecodeForTest();
      zeroCopyPass = zeroCopyDefault.zeroCopy > 0 && zeroCopyDefault.copied == 0
          && zeroCopyDisabled.zeroCopy == 0 && zeroCopyDisabled.copied > 0
          && zeroCopyRestored.zeroCopy > 0 && zeroCopyRestored.copied == 0
          && samePixels(zeroCopyDefault.pixels, zeroCopyDisabled.pixels)
          && samePixels(zeroCopyDefault.pixels, zeroCopyRestored.pixels)
          && ImageOptimizationSettings.state(ImageOptimizationSettings.DECODE_ZERO_COPY)
              == ImageOptimizationSettings.DEFAULT
          && ImageOptimizationSettings.isEnabled(ImageOptimizationSettings.DECODE_ZERO_COPY)
          && (defaultDecodeMask & DECODE_ZERO_COPY_BIT) != 0
          && (disabledDecodeMask & DECODE_ZERO_COPY_BIT) == 0
          && (restoredDecodeMask & DECODE_ZERO_COPY_BIT) != 0;

      ImageOptimizationSettings.resetForTest();
      Image source = Image.createLogical(100, 100, 2);
      Image transformed = source.getSmoothScaledInstance(100, 100);
      Graphics warmupGraphics = Image.createLogical(100, 100, 2).getGraphics();
      require(warmupGraphics != null, "physical identity warmup graphics");
      for (int warmup = 0; warmup < 3; warmup++) {
        warmupGraphics.drawImage(transformed, 0, 0, false);
      }
      physicalDefault = drawPhysical(transformed, ImageOptimizationSettings.DEFAULT);
      long defaultDrawMask = Image.nativeOptimizationMaskForDrawForTest();
      physicalDisabled = drawPhysical(transformed, ImageOptimizationSettings.DISABLED);
      long disabledDrawMask = Image.nativeOptimizationMaskForDrawForTest();
      physicalRestored = drawPhysical(transformed, ImageOptimizationSettings.DEFAULT);
      long restoredDrawMask = Image.nativeOptimizationMaskForDrawForTest();
      physicalIdentityPass = physicalDefault.attempts == 1 && physicalDefault.hits == 1
          && physicalDefault.fallbacks == 0 && physicalDefault.resamplesAvoided == 1
          && physicalDisabled.attempts == 0 && physicalDisabled.hits == 0
          && physicalDisabled.resamplesAvoided == 0
          && physicalRestored.attempts == 1 && physicalRestored.hits == 1
          && physicalRestored.fallbacks == 0 && physicalRestored.resamplesAvoided == 1
          && samePixels(physicalDefault.pixels, physicalDisabled.pixels)
          && samePixels(physicalDefault.pixels, physicalRestored.pixels)
          && ImageOptimizationSettings.state(ImageOptimizationSettings.RASTER_PHYSICAL_IDENTITY_FOLDING)
              == ImageOptimizationSettings.DEFAULT
          && ImageOptimizationSettings.isEnabled(
              ImageOptimizationSettings.RASTER_PHYSICAL_IDENTITY_FOLDING)
          && (defaultDrawMask & PHYSICAL_IDENTITY_BIT) != 0
          && (disabledDrawMask & PHYSICAL_IDENTITY_BIT) == 0
          && (restoredDrawMask & PHYSICAL_IDENTITY_BIT) != 0;

      ImageOptimizationSettings.resetForTest();
      targetDefaultMask = Image.nativeOptimizationMaskForDrawForTest();
      boolean targetDefaultOff = ImageOptimizationSettings.state(
          ImageOptimizationSettings.RASTER_TARGET_COLORTYPE_CONVERSION)
          == ImageOptimizationSettings.DEFAULT
          && !ImageOptimizationSettings.isEnabled(
              ImageOptimizationSettings.RASTER_TARGET_COLORTYPE_CONVERSION)
          && (targetDefaultMask & TARGET_COLORTYPE_BIT) == 0;
      ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_TARGET_COLORTYPE_CONVERSION,
          ImageOptimizationSettings.ENABLED);
      targetEnabledMask = Image.nativeOptimizationMaskForDrawForTest();
      boolean targetEnabled = ImageOptimizationSettings.isEnabled(
          ImageOptimizationSettings.RASTER_TARGET_COLORTYPE_CONVERSION)
          && (targetEnabledMask & TARGET_COLORTYPE_BIT) != 0
          && (Image.nativeOptimizationMaskForDecodeForTest() & TARGET_COLORTYPE_BIT) != 0;
      ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_TARGET_COLORTYPE_CONVERSION,
          ImageOptimizationSettings.DEFAULT);
      targetRestoredMask = Image.nativeOptimizationMaskForDrawForTest();
      boolean targetRestoredOff = ImageOptimizationSettings.state(
          ImageOptimizationSettings.RASTER_TARGET_COLORTYPE_CONVERSION)
          == ImageOptimizationSettings.DEFAULT
          && !ImageOptimizationSettings.isEnabled(
              ImageOptimizationSettings.RASTER_TARGET_COLORTYPE_CONVERSION)
          && (targetRestoredMask & TARGET_COLORTYPE_BIT) == 0
          && (Image.nativeOptimizationMaskForDecodeForTest() & TARGET_COLORTYPE_BIT) == 0;
      defaultDisabledPass = targetDefaultOff && targetEnabled && targetRestoredOff;
      diagnosticComplete = true;
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean overallPass = diagnosticComplete && javaMaskPass && exactRuntimePass && probePass
        && zeroCopyPass && physicalIdentityPass && defaultDisabledPass && error.length() == 0;
    System.out.println("fixture=ImageOptimizationMaskSmokeApp"
        + ",expectedMask=" + EXPECTED_MASK
        + ",javaDrawMask=" + javaDrawMask
        + ",javaDecodeMask=" + javaDecodeMask
        + ",javaMaskPass=" + javaMaskPass
        + ",exactDrawMask=" + exactDrawMask
        + ",exactDecodeMask=" + exactDecodeMask
        + ",subclassDrawMask=" + subclassDrawMask
        + ",subclassDecodeMask=" + subclassDecodeMask
        + ",exactRuntimeClass=" + exactRuntimeClass
        + ",subclassRuntimeClass=" + subclassRuntimeClass
        + ",exactRuntimePass=" + exactRuntimePass
        + ",probePass=" + probePass
        + ",zeroCopyDefault=" + zeroCopyDefault.zeroCopy
        + ",zeroCopyDefaultCopied=" + zeroCopyDefault.copied
        + ",zeroCopyDisabled=" + zeroCopyDisabled.zeroCopy
        + ",zeroCopyDisabledCopied=" + zeroCopyDisabled.copied
        + ",zeroCopyRestored=" + zeroCopyRestored.zeroCopy
        + ",zeroCopyRestoredCopied=" + zeroCopyRestored.copied
        + ",zeroCopyPass=" + zeroCopyPass
        + ",physicalIdentityDefaultAttempts=" + physicalDefault.attempts
        + ",physicalIdentityDefaultHits=" + physicalDefault.hits
        + ",physicalIdentityDefaultFallbacks=" + physicalDefault.fallbacks
        + ",physicalIdentityDefaultResamplesAvoided=" + physicalDefault.resamplesAvoided
        + ",physicalIdentityDisabledAttempts=" + physicalDisabled.attempts
        + ",physicalIdentityDisabledHits=" + physicalDisabled.hits
        + ",physicalIdentityDisabledFallbacks=" + physicalDisabled.fallbacks
        + ",physicalIdentityDisabledResamplesAvoided=" + physicalDisabled.resamplesAvoided
        + ",physicalIdentityRestoredAttempts=" + physicalRestored.attempts
        + ",physicalIdentityRestoredHits=" + physicalRestored.hits
        + ",physicalIdentityRestoredFallbacks=" + physicalRestored.fallbacks
        + ",physicalIdentityRestoredResamplesAvoided=" + physicalRestored.resamplesAvoided
        + ",physicalIdentityPass=" + physicalIdentityPass
        + ",targetDefaultMask=" + targetDefaultMask
        + ",targetEnabledMask=" + targetEnabledMask
        + ",targetRestoredMask=" + targetRestoredMask
        + ",defaultDisabledPass=" + defaultDisabledPass
        + ",diagnosticComplete=" + diagnosticComplete
        + ",overallPass=" + overallPass
        + (error.length() == 0 ? "" : ",error=" + error));
    System.out.flush();
    exit(overallPass ? 0 : 1);
  }

  private static DecodeResult decodeAfterState(byte[] png, int state) throws Exception {
    ImageOptimizationSettings.setState(ImageOptimizationSettings.DECODE_ZERO_COPY, state);
    Image.resetImageOperationAccountingForTest();
    Image image = new Image(png, png.length);
    int[] pixels = image.getPixels();
    return new DecodeResult(pixels, Image.zeroCopyDecodeCountForTest(),
        Image.copiedDecodeCountForTest());
  }

  private static PhysicalIdentityResult drawPhysical(Image source, int state) throws Exception {
    ImageOptimizationSettings.setState(
        ImageOptimizationSettings.RASTER_PHYSICAL_IDENTITY_FOLDING, state);
    Image target = Image.createLogical(100, 100, 2);
    Graphics graphics = target.getGraphics();
    require(graphics != null, "physical identity graphics");
    Image.resetImageOperationAccountingForTest();
    graphics.drawImage(source, 0, 0, false);
    return new PhysicalIdentityResult(target.getPixels(),
        NativeImageBacking.physicalIdentityAttemptsForTest(),
        NativeImageBacking.physicalIdentityHitsForTest(),
        NativeImageBacking.physicalIdentityFallbacksForTest(),
        NativeImageBacking.physicalIdentityResamplesAvoidedForTest());
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

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }

  private static final class DecodeResult {
    final int[] pixels;
    final long zeroCopy;
    final long copied;

    DecodeResult(int[] pixels, long zeroCopy, long copied) {
      this.pixels = pixels;
      this.zeroCopy = zeroCopy;
      this.copied = copied;
    }

    static DecodeResult empty() {
      return new DecodeResult(null, -1, -1);
    }
  }

  private static final class PhysicalIdentityResult {
    final int[] pixels;
    final long attempts;
    final long hits;
    final long fallbacks;
    final long resamplesAvoided;

    PhysicalIdentityResult(int[] pixels, long attempts, long hits, long fallbacks,
        long resamplesAvoided) {
      this.pixels = pixels;
      this.attempts = attempts;
      this.hits = hits;
      this.fallbacks = fallbacks;
      this.resamplesAvoided = resamplesAvoided;
    }

    static PhysicalIdentityResult empty() {
      return new PhysicalIdentityResult(null, -1, -1, -1, -1);
    }
  }

  private static final class SmokeImage extends Image {
    SmokeImage() throws ImageException {
      super(1, 1);
    }
  }
}
