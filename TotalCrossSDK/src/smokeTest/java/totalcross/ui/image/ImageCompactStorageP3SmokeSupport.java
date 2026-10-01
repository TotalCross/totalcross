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
      CompactSourceState imageSourceState = new CompactSourceState(source);
      Image deferred = source.getAlphaInstance(0);
      PipelineRootState pipelineRootState = new PipelineRootState(deferred);
      NativeImageBacking canonical = pipelineRootState.backing;
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
      boolean sourceStatePreserved = imageSourceState.preserved() && pipelineRootState.preserved(deferred);
      boolean sourceCompact = imageSourceState.isCompact(NativeImageBacking.FORMAT_RGB565)
          && pipelineRootState.isCompact(NativeImageBacking.FORMAT_RGB565);
      if (!(pending && materialized && hit && parity && targetType && sourceCompact
          && sourceStatePreserved)) {
        throw new IllegalStateException("target-color details pending=" + pending
            + ",materialized=" + materialized + ",hit=" + hit + ",parity=" + parity
            + ",targetType=" + targetBacking.colorTypeForTest() + ",sourceCompact="
            + sourceCompact + ",sourceStatePreserved=" + sourceStatePreserved
            + ",imageSourceState=" + imageSourceState.summary()
            + ",pipelineRootState=" + pipelineRootState.summary(deferred)
            + ",sourceFormat=" + canonical.formatForTest()
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
      CompactSourceState imageSourceState = new CompactSourceState(source);
      Image deferred = source.getAlphaInstance(0);
      PipelineRootState pipelineRootState = new PipelineRootState(deferred);
      NativeImageBacking canonical = pipelineRootState.backing;
      Image target = new Image(source.getPixelWidth(), source.getPixelHeight());
      NativeImageBacking rgb565Target = replaceTargetBacking(target,
          NativeImageBacking.TEST_COLOR_RGB_565);
      target.getGraphics().drawImage(deferred, 0, 0, true);
      int[] actual = target.getPixels();
      boolean sourceStatePreserved = imageSourceState.preserved() && pipelineRootState.preserved(deferred);
      return Image.physicalIdentityFallbackCountForTest() > 0
          && Image.physicalVariantFallbackCountForTest() > 0
          && Image.physicalVariantMaterializationCountForTest() == 0
          && same(expected, actual)
          && rgb565Target.colorTypeForTest() == NativeImageBacking.TEST_COLOR_RGB_565
          && imageSourceState.isCompact(NativeImageBacking.FORMAT_GRAY8)
          && pipelineRootState.isCompact(NativeImageBacking.FORMAT_GRAY8)
          && sourceStatePreserved;
    } finally {
      ImageRasterSmokeTestSupport.setRasterFeatures(true, false, false);
    }
  }

  private static NativeImageBacking sourceBacking(Image deferred) throws Exception {
    BackingImageSource root = (BackingImageSource) deferred.pipelineForSmoke().root();
    Image canonicalImage = Image.materializeBackingSource(root);
    return ImageCompactStorageSmokeSupport.nativeBacking(canonicalImage);
  }

  private static final class CompactSourceState {
    final Image image;
    final NativeImageBacking backing;
    final long generation;
    final long nativeGeneration;
    final int opacity;
    final int nativeOpacity;
    final int format;

    CompactSourceState(Image image) {
      this.image = image;
      backing = ImageCompactStorageSmokeSupport.nativeBacking(image);
      generation = image.backingMutationGenerationForP2();
      nativeGeneration = backing.mutationGeneration();
      opacity = image.opacityStateForP2();
      nativeOpacity = backing.opacityState();
      format = backing.formatForTest();
    }

    boolean isCompact(int expectedFormat) {
      return backing.isCompact() && format == expectedFormat;
    }

    boolean preserved() {
      return image.backing == backing
          && image.backingMutationGenerationForP2() == generation
          && backing.mutationGeneration() == nativeGeneration
          && image.opacityStateForP2() == opacity && backing.opacityState() == nativeOpacity
          && backing.formatForTest() == format && backing.isCompact();
    }

    String summary() {
      return "identity=" + (image.backing == backing)
          + ",generation=" + generation + "/" + image.backingMutationGenerationForP2()
          + ",nativeGeneration=" + nativeGeneration + "/" + backing.mutationGeneration()
          + ",opacity=" + opacity + "/" + image.opacityStateForP2()
          + ",nativeOpacity=" + nativeOpacity + "/" + backing.opacityState()
          + ",format=" + format + "/" + backing.formatForTest()
          + ",compact=" + backing.isCompact();
    }
  }

  private static final class PipelineRootState {
    final BackingImageSource root;
    final NativeImageBacking backing;
    final int format;

    PipelineRootState(Image deferred) {
      root = (BackingImageSource) deferred.pipelineForSmoke().root();
      if (!(root.backing instanceof NativeImageBacking)) {
        throw new IllegalStateException("P3 root does not retain a native compact backing");
      }
      backing = (NativeImageBacking) root.backing;
      format = backing.formatForTest();
    }

    boolean isCompact(int expectedFormat) {
      return backing.isCompact() && format == expectedFormat;
    }

    boolean preserved(Image deferred) {
      return deferred.pipelineForSmoke().root() == root && root.backing == backing
          && backing.formatForTest() == format && backing.isCompact();
    }

    String summary(Image deferred) {
      return "root=" + (deferred.pipelineForSmoke().root() == root)
          + ",identity=" + (root.backing == backing) + ",format=" + format + "/"
          + backing.formatForTest() + ",compact=" + backing.isCompact();
    }
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
