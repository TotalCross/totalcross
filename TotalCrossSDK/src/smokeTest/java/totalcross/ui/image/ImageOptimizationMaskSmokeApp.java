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

  @Override
  public void initUI() {
    long javaDrawMask = -1;
    long javaDecodeMask = -1;
    int exactDrawMask = -2;
    int exactDecodeMask = -2;
    int subclassDrawMask = -2;
    int subclassDecodeMask = -2;
    long zeroCopyCount = -1;
    long physicalIdentityAttempts = -1;
    long physicalIdentityHits = -1;
    long physicalIdentityFallbacks = -1;
    long physicalIdentityResamplesAvoided = -1;
    String exactRuntimeClass = "";
    String subclassRuntimeClass = "";
    String error = "";
    boolean javaMaskPass = false;
    boolean exactRuntimePass = false;
    boolean exactProbePass = false;
    boolean zeroCopyPass = false;
    boolean physicalIdentityPass = false;
    boolean diagnosticComplete = false;

    try {
      ImageOptimizationSettings.resetForTest();
      javaDrawMask = Image.nativeOptimizationMaskForDrawForTest();
      javaDecodeMask = Image.nativeOptimizationMaskForDecodeForTest();
      javaMaskPass = javaDrawMask == EXPECTED_MASK && javaDecodeMask == EXPECTED_MASK;
      require(javaMaskPass, "java masks");

      Image exact = new Image(1, 1);
      Image subclass = new SmokeImage();
      exactRuntimeClass = exact.getClass().getName();
      subclassRuntimeClass = subclass.getClass().getName();
      exactRuntimePass = Image.class.getName().equals(exactRuntimeClass);
      require(exactRuntimePass, "exact runtime class");
      require(subclassRuntimeClass.length() > 0, "subclass runtime class");

      exactDrawMask = Image.nativeOptimizationMaskObservedForTest(exact, true);
      exactDecodeMask = Image.nativeOptimizationMaskObservedForTest(exact, false);
      subclassDrawMask = Image.nativeOptimizationMaskObservedForTest(subclass, true);
      subclassDecodeMask = Image.nativeOptimizationMaskObservedForTest(subclass, false);
      exactProbePass = exactDrawMask == EXPECTED_MASK && exactDecodeMask == EXPECTED_MASK;
      require(exactProbePass, "exact native probe");

      byte[] png = Vm.getFile("image-abi/tiny.png");
      require(png != null && png.length > 0, "zero-copy fixture");
      Image.resetImageOperationAccountingForTest();
      Image decoded = new Image(png, png.length);
      require(decoded.getPixels() != null, "zero-copy decode");
      zeroCopyCount = Image.zeroCopyDecodeCountForTest();
      zeroCopyPass = zeroCopyCount > 0 && Image.copiedDecodeCountForTest() == 0;
      require(zeroCopyPass, "default zero-copy counter");

      Image.resetImageOperationAccountingForTest();
      Image source = Image.createLogical(100, 100, 2);
      Image transformed = source.getSmoothScaledInstance(100, 100);
      Image target = Image.createLogical(100, 100, 2);
      Graphics graphics = target.getGraphics();
      require(graphics != null, "physical identity target graphics");
      for (int warmup = 0; warmup < 3; warmup++) {
        graphics.drawImage(transformed, 0, 0, false);
      }
      Image.resetImageOperationAccountingForTest();
      graphics.drawImage(transformed, 0, 0, false);
      physicalIdentityAttempts = NativeImageBacking.physicalIdentityAttemptsForTest();
      physicalIdentityHits = NativeImageBacking.physicalIdentityHitsForTest();
      physicalIdentityFallbacks = NativeImageBacking.physicalIdentityFallbacksForTest();
      physicalIdentityResamplesAvoided = NativeImageBacking.physicalIdentityResamplesAvoidedForTest();
      physicalIdentityPass = physicalIdentityAttempts > 0 && physicalIdentityHits > 0
          && physicalIdentityFallbacks == 0 && physicalIdentityResamplesAvoided > 0;
      require(physicalIdentityPass, "default physical identity counter");
      diagnosticComplete = true;
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    String classification = classify(javaMaskPass, exactDrawMask, exactDecodeMask,
        subclassDrawMask, subclassDecodeMask, zeroCopyPass && physicalIdentityPass);
    boolean overallPass = diagnosticComplete && javaMaskPass && exactRuntimePass && exactProbePass
        && zeroCopyPass && physicalIdentityPass && error.length() == 0;
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
        + ",exactProbePass=" + exactProbePass
        + ",zeroCopyCount=" + zeroCopyCount
        + ",zeroCopyPass=" + zeroCopyPass
        + ",physicalIdentityAttempts=" + physicalIdentityAttempts
        + ",physicalIdentityHits=" + physicalIdentityHits
        + ",physicalIdentityFallbacks=" + physicalIdentityFallbacks
        + ",physicalIdentityResamplesAvoided=" + physicalIdentityResamplesAvoided
        + ",physicalIdentityPass=" + physicalIdentityPass
        + ",classification=" + classification
        + ",diagnosticComplete=" + diagnosticComplete
        + ",overallPass=" + overallPass
        + (error.length() == 0 ? "" : ",error=" + error));
    System.out.flush();
    exit(overallPass ? 0 : 1);
  }

  private static String classify(boolean javaMaskPass, int exactDrawMask, int exactDecodeMask,
      int subclassDrawMask, int subclassDecodeMask, boolean countersPass) {
    if (!javaMaskPass) {
      return "E";
    }
    if (exactDrawMask != EXPECTED_MASK || exactDecodeMask != EXPECTED_MASK) {
      return "C";
    }
    if (subclassDrawMask != EXPECTED_MASK || subclassDecodeMask != EXPECTED_MASK) {
      return "B";
    }
    return countersPass ? "A" : "D";
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }

  private static final class SmokeImage extends Image {
    SmokeImage() throws ImageException {
      super(1, 1);
    }
  }
}
