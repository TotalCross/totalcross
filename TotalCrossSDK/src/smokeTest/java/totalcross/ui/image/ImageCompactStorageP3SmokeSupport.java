// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.runtime.ImageRasterSmokeTestSupport;

/** Renderer-driven P3 checks whose canonical source is compact encoded storage. */
final class ImageCompactStorageP3SmokeSupport {
  private ImageCompactStorageP3SmokeSupport() {
  }

  static boolean physicalVariantReuse() throws Exception {
    try {
      ImageRasterSmokeTestSupport.setRasterFeatures(true, false, false);
      Image referenceSource = ImageCompactStorageSmokeSupport.load("opaque-color.png");
      referenceSource.getPixels();
      Image referenceDeferred = referenceSource.getSmoothScaledInstance(4, 3);
      NativeImageBacking referenceBacking = sourceBacking(referenceDeferred);
      Image referenceTarget = new Image(8, 8);
      for (int i = 0; i < 3; i++) {
        referenceTarget.getGraphics().drawImage(referenceDeferred, 1, 1, true);
      }
      int[] expected = referenceTarget.getPixels();
      if (!referenceBacking.isCompact()
          || referenceBacking.formatForTest() != NativeImageBacking.FORMAT_RGB565) {
        return false;
      }

      Image.resetImageOperationAccountingForTest();
      ImageRasterSmokeTestSupport.setRasterFeatures(true, false, true);
      Image source = ImageCompactStorageSmokeSupport.load("opaque-color.png");
      source.getPixels();
      Image deferred = source.getSmoothScaledInstance(4, 3);
      NativeImageBacking canonical = sourceBacking(deferred);
      if (!canonical.isCompact() || canonical.formatForTest() != NativeImageBacking.FORMAT_RGB565) {
        return false;
      }
      Image target = new Image(8, 8);
      target.getGraphics().drawImage(deferred, 1, 1, true);
      boolean pending = canonical.variantStateForTest() == 2
          && Image.physicalVariantFallbackCountForTest() == 1;
      target.getGraphics().drawImage(deferred, 1, 1, true);
      boolean materialized = canonical.variantStateForTest() == 1
          && Image.physicalVariantMaterializationCountForTest() == 1;
      target.getGraphics().drawImage(deferred, 1, 1, true);
      boolean reused = Image.physicalVariantHitCountForTest() == 1;
      return pending && materialized && reused && same(expected, target.getPixels())
          && canonical.isCompact() && canonical.formatForTest() == NativeImageBacking.FORMAT_RGB565;
    } finally {
      ImageRasterSmokeTestSupport.setRasterFeatures(true, false, false);
    }
  }

  static boolean targetColorVariantPreservesCompactSource() throws Exception {
    try {
      ImageRasterSmokeTestSupport.setRasterFeatures(false, false, false);
      Image referenceSource = ImageCompactStorageSmokeSupport.load("opaque-color.png");
      referenceSource.getPixels();
      Image referenceDeferred = referenceSource.getAlphaInstance(0);
      Image referenceTarget = new Image(referenceSource.getPixelWidth(), referenceSource.getPixelHeight());
      referenceTarget.getGraphics().drawImage(referenceDeferred, 0, 0, true);
      int[] expected = referenceTarget.getPixels();

      Image.resetImageOperationAccountingForTest();
      ImageRasterSmokeTestSupport.setRasterFeatures(false, true, false);
      Image source = ImageCompactStorageSmokeSupport.load("opaque-color.png");
      source.getPixels();
      Image deferred = source.getAlphaInstance(0);
      NativeImageBacking canonical = sourceBacking(deferred);
      Image.resetImageOperationAccountingForTest();
      ImageRasterSmokeTestSupport.setRasterFeatures(false, true, false);
      Image target = new Image(source.getPixelWidth(), source.getPixelHeight());
      NativeImageBacking targetBacking = replaceTargetBacking(target,
          NativeImageBacking.TEST_COLOR_BGRA_8888);
      target.getGraphics().drawImage(deferred, 0, 0, true);
      boolean pending = (canonical.variantStateForTest() & 2) != 0;
      target.getGraphics().drawImage(deferred, 0, 0, true);
      boolean materialized = canonical.variantStateForTest() == 1
          && Image.targetColorVariantMaterializationCountForTest() == 1;
      target.getGraphics().drawImage(deferred, 0, 0, true);
      boolean hit = Image.targetColorVariantHitCountForTest() == 1;
      boolean parity = same(expected, target.getPixels());
      boolean targetType = targetBacking.colorTypeForTest() != canonical.colorTypeForTest();
      boolean sourceCompact = canonical.isCompact()
          && canonical.formatForTest() == NativeImageBacking.FORMAT_RGB565;
      if (!(pending && materialized && hit && parity && targetType && sourceCompact)) {
        throw new IllegalStateException("target-color details pending=" + pending
            + ",materialized=" + materialized + ",hit=" + hit + ",parity=" + parity
            + ",targetType=" + targetBacking.colorTypeForTest() + ",sourceCompact="
            + canonical.isCompact() + ",sourceFormat=" + canonical.formatForTest()
            + ",state=" + canonical.variantStateForTest() + ",materializations="
            + Image.targetColorVariantMaterializationCountForTest() + ",hits="
            + Image.targetColorVariantHitCountForTest() + ",dimensions=" + source.getPixelWidth()
            + "x" + source.getPixelHeight() + ",lengths=" + expected.length + "/"
            + target.getPixels().length + ",firstPixels=" + Integer.toHexString(expected[0])
            + "/" + Integer.toHexString(target.getPixels()[0]));
      }
      return true;
    } finally {
      ImageRasterSmokeTestSupport.setRasterFeatures(true, false, false);
    }
  }

  static boolean incompatiblePhysicalFallbackPreservesCompactSource() throws Exception {
    try {
      ImageRasterSmokeTestSupport.setRasterFeatures(false, false, false);
      Image referenceSource = ImageCompactStorageSmokeSupport.load("opaque-gray.png");
      referenceSource.getPixels();
      Image referenceDeferred = referenceSource.getAlphaInstance(0);
      Image referenceTarget = new Image(referenceSource.getPixelWidth(), referenceSource.getPixelHeight());
      replaceTargetBacking(referenceTarget, NativeImageBacking.TEST_COLOR_RGB_565);
      referenceTarget.getGraphics().drawImage(referenceDeferred, 0, 0, true);
      int[] expected = referenceTarget.getPixels();

      Image.resetImageOperationAccountingForTest();
      ImageRasterSmokeTestSupport.setRasterFeatures(true, false, true);
      Image source = ImageCompactStorageSmokeSupport.load("opaque-gray.png");
      source.getPixels();
      Image deferred = source.getAlphaInstance(0);
      NativeImageBacking canonical = sourceBacking(deferred);
      Image target = new Image(source.getPixelWidth(), source.getPixelHeight());
      NativeImageBacking rgb565Target = replaceTargetBacking(target,
          NativeImageBacking.TEST_COLOR_RGB_565);
      target.getGraphics().drawImage(deferred, 0, 0, true);
      return Image.physicalIdentityFallbackCountForTest() > 0
          && Image.physicalVariantFallbackCountForTest() > 0
          && Image.physicalVariantMaterializationCountForTest() == 0
          && same(expected, target.getPixels())
          && rgb565Target.colorTypeForTest() == NativeImageBacking.TEST_COLOR_RGB_565
          && canonical.isCompact() && canonical.formatForTest() == NativeImageBacking.FORMAT_GRAY8;
    } finally {
      ImageRasterSmokeTestSupport.setRasterFeatures(true, false, false);
    }
  }

  private static NativeImageBacking sourceBacking(Image deferred) throws Exception {
    BackingImageSource root = (BackingImageSource) deferred.pipelineForSmoke().root();
    Image canonicalImage = Image.materializeBackingSource(root);
    return ImageCompactStorageSmokeSupport.nativeBacking(canonicalImage);
  }

  private static NativeImageBacking replaceTargetBacking(Image target, int colorType) throws Exception {
    NativeImageBacking previous = (NativeImageBacking) target.backing;
    NativeImageBacking replacement = NativeImageBacking.createEmptyWithColorTypeForTest(
        target.getPixelWidth(), target.getPixelHeight(), colorType);
    target.backing = replacement;
    previous.release();
    return replacement;
  }

  private static boolean same(int[] expected, int[] actual) {
    return java.util.Arrays.equals(expected, actual);
  }
}
