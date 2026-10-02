// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.runtime.ImageRasterSmokeTestSupport;
import totalcross.sys.runtime.ImageRuntimeConfigurationStartup;
import totalcross.sys.runtime.ImageRuntimePolicy;

import static totalcross.ui.image.ImageCompactStorageSmokeSupport.*;

/** Renderer-driven P3 checks whose canonical source is compact encoded storage. */
final class ImageCompactStorageP3SmokeSupport {
  private static String productionVariantFailureDetails = "";

  private ImageCompactStorageP3SmokeSupport() {
  }

  /** Exercises P3 target-color conversion under the policy resolved from app annotations. */
  static boolean productionConfiguredTargetColorPath() throws Exception {
    ImageRuntimePolicy policy = ImageRuntimeConfigurationStartup.currentPolicy();
    if (!policy.rasterVariants().targetColorConversion()) {
      return false;
    }
    productionVariantFailureDetails = "";
    return productionTargetColorVariant();
  }

  /** Exercises P3 physical-variant caching under a selector that leaves target-color conversion off. */
  static boolean productionConfiguredPhysicalVariantPath() throws Exception {
    ImageRuntimePolicy policy = ImageRuntimeConfigurationStartup.currentPolicy();
    if (policy.rasterVariants().targetColorConversion()
        || !policy.rasterVariants().physicalVariantCache()) {
      return false;
    }
    productionVariantFailureDetails = "";
    return productionPhysicalVariantCache();
  }

  static String productionVariantFailureDetails() {
    return productionVariantFailureDetails;
  }

  /** Confirms explicit DISABLED values leave both P3 variant paths inactive. */
  static boolean productionDisabledVariantsRemainInactive() throws Exception {
    ImageRuntimePolicy policy = ImageRuntimeConfigurationStartup.currentPolicy();
    if (policy.rasterVariants().targetColorConversion()
        || policy.rasterVariants().physicalVariantCache()) {
      return false;
    }
    Image colorSource = ImageCompactStorageSmokeSupport.load("opaque-color.png");
    colorSource.getPixels();
    Image colorDeferred = colorSource.getAlphaInstance(0);
    Image colorTarget = new Image(colorSource.getPixelWidth(), colorSource.getPixelHeight());
    replaceTargetBacking(colorTarget, NativeImageBacking.TEST_COLOR_BGRA_8888);
    ImageRasterFeatureBridge.resetDrawAccountingForTest();
    Image.resetImageOperationAccountingForTest();
    colorTarget.getGraphics().drawImage(colorDeferred, 0, 0, true);
    colorTarget.getGraphics().drawImage(colorDeferred, 0, 0, true);
    colorTarget.getGraphics().drawImage(colorDeferred, 0, 0, true);
    boolean noColorVariant = Image.targetColorVariantMaterializationCountForTest() == 0
        && Image.targetColorVariantHitCountForTest() == 0;

    Image physicalSource = ImageCompactStorageSmokeSupport.load("opaque-color.png");
    physicalSource.getPixels();
    Image physicalDeferred = physicalSource.getSmoothScaledInstance(4, 3);
    NativeImageBacking canonical = sourceBacking(physicalDeferred);
    Image physicalTarget = new Image(8, 8);
    replaceTargetBacking(physicalTarget, canonical.formatForTest() == NativeImageBacking.FORMAT_RGB565
        ? NativeImageBacking.TEST_COLOR_RGB_565 : NativeImageBacking.TEST_COLOR_RGBA_8888);
    ImageRasterFeatureBridge.resetDrawAccountingForTest();
    Image.resetImageOperationAccountingForTest();
    physicalTarget.getGraphics().drawImage(physicalDeferred, 1, 1, true);
    physicalTarget.getGraphics().drawImage(physicalDeferred, 1, 1, true);
    physicalTarget.getGraphics().drawImage(physicalDeferred, 1, 1, true);
    return noColorVariant && Image.physicalVariantMaterializationCountForTest() == 0
        && Image.physicalVariantHitCountForTest() == 0;
  }

  private static boolean productionTargetColorVariant() throws Exception {
    Image source = ImageCompactStorageSmokeSupport.load("opaque-color.png");
    source.getPixels();
    CompactSourceState imageSourceState = new CompactSourceState(source);
    Image deferred = source.getAlphaInstance(0);
    PipelineRootState pipelineRootState = new PipelineRootState(deferred);
    NativeImageBacking canonical = pipelineRootState.backing;
    Image target = new Image(source.getPixelWidth(), source.getPixelHeight());
    NativeImageBacking targetBacking = replaceTargetBacking(target,
        NativeImageBacking.TEST_COLOR_BGRA_8888);
    ImageRasterFeatureBridge.resetDrawAccountingForTest();
    Image.resetImageOperationAccountingForTest();
    target.getGraphics().drawImage(deferred, 0, 0, true);
    target.getGraphics().drawImage(deferred, 0, 0, true);
    target.getGraphics().drawImage(deferred, 0, 0, true);
    boolean passed = canonical.variantStateForTest() == 1
        && Image.targetColorVariantMaterializationCountForTest() == 1
        && Image.targetColorVariantHitCountForTest() == 1
        && targetBacking.colorTypeForTest() != canonical.colorTypeForTest()
        && imageSourceState.preserved() && pipelineRootState.preserved(deferred)
        && imageSourceState.isCompact(NativeImageBacking.FORMAT_RGB565)
        && pipelineRootState.isCompact(NativeImageBacking.FORMAT_RGB565);
    if (!passed) {
      productionVariantFailureDetails += "targetColor(state=" + canonical.variantStateForTest()
          + ",materializations=" + Image.targetColorVariantMaterializationCountForTest()
          + ",hits=" + Image.targetColorVariantHitCountForTest()
          + ",targetType=" + targetBacking.colorTypeForTest()
          + ",sourceType=" + canonical.colorTypeForTest()
          + ",sourceFormat=" + canonical.formatForTest()
          + ",sourceCompact=" + imageSourceState.isCompact(NativeImageBacking.FORMAT_RGB565)
          + ",rootCompact=" + pipelineRootState.isCompact(NativeImageBacking.FORMAT_RGB565) + ");";
    }
    return passed;
  }

  private static boolean productionPhysicalVariantCache() throws Exception {
    Image source = ImageCompactStorageSmokeSupport.load("opaque-color.png");
    source.getPixels();
    Image deferred = source.getSmoothScaledInstance(4, 3);
    NativeImageBacking canonical = sourceBacking(deferred);
    if (!canonical.isCompact() || canonical.formatForTest() != NativeImageBacking.FORMAT_RGB565) {
      return false;
    }
    Image target = new Image(8, 8);
    ImageRasterFeatureBridge.resetDrawAccountingForTest();
    Image.resetImageOperationAccountingForTest();
    target.getGraphics().drawImage(deferred, 1, 1, true);
    target.getGraphics().drawImage(deferred, 1, 1, true);
    target.getGraphics().drawImage(deferred, 1, 1, true);
    boolean passed = canonical.variantStateForTest() == 1
        && Image.physicalVariantMaterializationCountForTest() == 1
        && Image.physicalVariantHitCountForTest() == 1
        && canonical.isCompact() && canonical.formatForTest() == NativeImageBacking.FORMAT_RGB565;
    if (!passed) {
      productionVariantFailureDetails += "physicalVariant(state=" + canonical.variantStateForTest()
          + ",materializations=" + Image.physicalVariantMaterializationCountForTest()
          + ",hits=" + Image.physicalVariantHitCountForTest()
          + ",sourceType=" + canonical.colorTypeForTest()
          + ",sourceFormat=" + canonical.formatForTest() + ");";
    }
    return passed;
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

  static boolean p6Rgb565DirectCopyPreservesCompactSource() throws Exception {
    try {
      ImageRasterSmokeTestSupport.setRasterFeatures(true, false, false);
      Image source = ImageCompactStorageSmokeSupport.load("opaque-color.png");
      int[] sourcePixels = source.getPixels();
      CompactSourceState imageSourceState = new CompactSourceState(source);
      Image deferred = source.getClippedInstance(0, 0, WIDTH, HEIGHT);
      PipelineRootState pipelineRootState = new PipelineRootState(deferred);
      NativeImageBacking canonical = pipelineRootState.backing;

      Image expectedTarget = new Image(WIDTH, HEIGHT);
      replaceTargetBacking(expectedTarget, NativeImageBacking.TEST_COLOR_RGB_565);
      expectedTarget.getGraphics().copyRect(source, 0, 0, WIDTH, HEIGHT, 0, 0);
      int[] expected = expectedTarget.getPixels();
      Image target = new Image(WIDTH, HEIGHT);
      NativeImageBacking targetBacking = replaceTargetBacking(target,
          NativeImageBacking.TEST_COLOR_RGB_565);
      ImageRasterFeatureBridge.resetDrawAccountingForTest();
      Image.resetImageOperationAccountingForTest();
      target.getGraphics().copyRect(deferred, 0, 0, WIDTH, HEIGHT, 0, 0);

      int status = ImageRasterFeatureBridge.copyRectPlanLastStatusForTest;
      int[] actualPixels = target.getPixels();
      boolean passed = same(expected, actualPixels) && same(sourcePixels, expected)
          && ImageRasterFeatureBridge.copyRectPlanAttemptsForTest == 1
          && ImageRasterFeatureBridge.copyRectPlanHandledForTest == 1
          && ImageRasterFeatureBridge.physicalCopyHitsForTest == 1
          && ImageRasterFeatureBridge.genericGeometryDrawsForTest == 0
          && Image.nativeGeometryMaterializationCountForTest() == 0
          && (status & ImageRasterFeatureBridge.PHYSICAL_COPY_HIT) != 0
          && targetBacking.colorTypeForTest() == NativeImageBacking.TEST_COLOR_RGB_565
          && imageSourceState.isCompact(NativeImageBacking.FORMAT_RGB565)
          && pipelineRootState.isCompact(NativeImageBacking.FORMAT_RGB565)
          && imageSourceState.preserved() && pipelineRootState.preserved(deferred)
          && canonical == pipelineRootState.backing;
      if (!passed) {
        throw new IllegalStateException("status=" + status + ",copyHits="
            + ImageRasterFeatureBridge.physicalCopyHitsForTest + ",generic="
            + ImageRasterFeatureBridge.genericGeometryDrawsForTest + ",materializations="
            + Image.nativeGeometryMaterializationCountForTest() + ",targetColor="
            + targetBacking.colorTypeForTest() + ",source=" + imageSourceState.summary()
            + ",root=" + pipelineRootState.summary(deferred) + ",planHandled="
            + ImageRasterFeatureBridge.copyRectPlanHandledForTest + ",sourceExpected="
            + firstDifference(sourcePixels, expected) + ",expectedActual="
            + firstDifference(expected, actualPixels));
      }
      return true;
    } finally {
      ImageRasterSmokeTestSupport.setRasterFeatures(true, false, false);
    }
  }

  static boolean p6Gray8FallbackPreservesCompactSource() throws Exception {
    return p6CompactFallbackPreservesSource("opaque-gray.png",
        NativeImageBacking.FORMAT_GRAY8, NativeImageBacking.TEST_COLOR_RGB_565);
  }

  static boolean p6Argb4444FallbackPreservesCompactSource() throws Exception {
    return p6CompactFallbackPreservesSource("alpha-gradient.png",
        NativeImageBacking.FORMAT_ARGB4444, NativeImageBacking.TEST_COLOR_BGRA_8888);
  }

  private static boolean p6CompactFallbackPreservesSource(String fixture, int expectedFormat,
      int targetColorType) throws Exception {
    try {
      ImageRasterSmokeTestSupport.setRasterFeatures(true, false, false);
      Image referenceSource = ImageCompactStorageSmokeSupport.load(fixture);
      referenceSource.getPixels();
      Image expectedTarget = new Image(WIDTH, HEIGHT);
      replaceTargetBacking(expectedTarget, targetColorType);
      expectedTarget.getGraphics().copyRect(referenceSource, 0, 0, WIDTH, HEIGHT, 0, 0);
      int[] expected = expectedTarget.getPixels();

      Image source = ImageCompactStorageSmokeSupport.load(fixture);
      source.getPixels();
      CompactSourceState imageSourceState = new CompactSourceState(source);
      Image deferred = source.getClippedInstance(0, 0, WIDTH, HEIGHT);
      PipelineRootState pipelineRootState = new PipelineRootState(deferred);
      Image target = new Image(WIDTH, HEIGHT);
      NativeImageBacking targetBacking = replaceTargetBacking(target, targetColorType);
      ImageRasterFeatureBridge.resetDrawAccountingForTest();
      Image.resetImageOperationAccountingForTest();
      target.getGraphics().copyRect(deferred, 0, 0, WIDTH, HEIGHT, 0, 0);

      int status = ImageRasterFeatureBridge.copyRectPlanLastStatusForTest;
      int[] actual = target.getPixels();
      boolean passed = same(expected, actual)
          && ImageRasterFeatureBridge.copyRectPlanAttemptsForTest == 1
          && ImageRasterFeatureBridge.copyRectPlanHandledForTest == 1
          && ImageRasterFeatureBridge.physicalCopyHitsForTest == 0
          && ImageRasterFeatureBridge.genericGeometryDrawsForTest > 0
          && ImageRasterFeatureBridge.identityAttemptsForTest > 0
          && (status & ImageRasterFeatureBridge.PHYSICAL_COPY_HIT) == 0
          && imageSourceState.isCompact(expectedFormat)
          && pipelineRootState.isCompact(expectedFormat)
          && imageSourceState.preserved() && pipelineRootState.preserved(deferred);
      if (!passed) {
        throw new IllegalStateException("fixture=" + fixture + ",status=" + status
            + ",copyHits=" + ImageRasterFeatureBridge.physicalCopyHitsForTest
            + ",generic=" + ImageRasterFeatureBridge.genericGeometryDrawsForTest
            + ",identityAttempts=" + ImageRasterFeatureBridge.identityAttemptsForTest
            + ",materializations=" + Image.nativeGeometryMaterializationCountForTest()
            + ",targetColor=" + targetBacking.colorTypeForTest()
            + ",source=" + imageSourceState.summary() + ",root="
            + pipelineRootState.summary(deferred) + ",parity=" + firstDifference(expected, actual));
      }
      return true;
    } finally {
      ImageRasterSmokeTestSupport.setRasterFeatures(true, false, false);
    }
  }

  static boolean p6TargetColorCopyRectPreservesCompactSource() throws Exception {
    try {
      ImageRasterSmokeTestSupport.setRasterFeatures(true, true, false);
      Image referenceSource = ImageCompactStorageSmokeSupport.load("opaque-color.png");
      referenceSource.getPixels();
      Image referenceDeferred = referenceSource.getClippedInstance(0, 0, WIDTH, HEIGHT);
      Image referenceTarget = new Image(WIDTH, HEIGHT);
      replaceTargetBacking(referenceTarget, NativeImageBacking.TEST_COLOR_BGRA_8888);
      referenceTarget.getGraphics().copyRect(referenceDeferred, 0, 0, WIDTH, HEIGHT, 0, 0);
      int[] expected = referenceTarget.getPixels();

      Image source = ImageCompactStorageSmokeSupport.load("opaque-color.png");
      source.getPixels();
      CompactSourceState imageSourceState = new CompactSourceState(source);
      Image deferred = source.getClippedInstance(0, 0, WIDTH, HEIGHT);
      PipelineRootState pipelineRootState = new PipelineRootState(deferred);
      NativeImageBacking canonical = pipelineRootState.backing;
      Image target = new Image(WIDTH, HEIGHT);
      NativeImageBacking targetBacking = replaceTargetBacking(target,
          NativeImageBacking.TEST_COLOR_BGRA_8888);
      ImageRasterFeatureBridge.resetDrawAccountingForTest();
      Image.resetImageOperationAccountingForTest();
      for (int i = 0; i < 3; i++) {
        target.getGraphics().copyRect(deferred, 0, 0, WIDTH, HEIGHT, 0, 0);
      }

      int status = ImageRasterFeatureBridge.copyRectPlanLastStatusForTest;
      int[] actual = target.getPixels();
      boolean passed = same(expected, actual)
          && ImageRasterFeatureBridge.copyRectPlanAttemptsForTest == 3
          && ImageRasterFeatureBridge.physicalCopyHitsForTest == 0
          && ImageRasterFeatureBridge.targetColorVariantFallbacksForTest == 1
          && ImageRasterFeatureBridge.targetColorVariantMaterializationsForTest == 1
          && ImageRasterFeatureBridge.targetColorVariantHitsForTest == 1
          && (status & ImageRasterFeatureBridge.PHYSICAL_COPY_HIT) == 0
          && targetBacking.colorTypeForTest() != canonical.colorTypeForTest()
          && canonical.formatForTest() == NativeImageBacking.FORMAT_RGB565
          && imageSourceState.isCompact(NativeImageBacking.FORMAT_RGB565)
          && pipelineRootState.isCompact(NativeImageBacking.FORMAT_RGB565)
          && imageSourceState.preserved() && pipelineRootState.preserved(deferred);
      if (!passed) {
        throw new IllegalStateException("status=" + status + ",copyAttempts="
            + ImageRasterFeatureBridge.copyRectPlanAttemptsForTest + ",copyHits="
            + ImageRasterFeatureBridge.physicalCopyHitsForTest + ",targetColorFallbacks="
            + ImageRasterFeatureBridge.targetColorVariantFallbacksForTest + ",targetColorMaterializations="
            + ImageRasterFeatureBridge.targetColorVariantMaterializationsForTest + ",targetColorHits="
            + ImageRasterFeatureBridge.targetColorVariantHitsForTest + ",generic="
            + ImageRasterFeatureBridge.genericGeometryDrawsForTest + ",variantState="
            + canonical.variantStateForTest() + ",targetColor=" + targetBacking.colorTypeForTest()
            + ",sourceColor=" + canonical.colorTypeForTest() + ",source="
            + imageSourceState.summary() + ",root=" + pipelineRootState.summary(deferred)
            + ",parity=" + firstDifference(expected, actual));
      }
      return true;
    } finally {
      ImageRasterSmokeTestSupport.setRasterFeatures(true, false, false);
    }
  }

  static boolean p6CachedFinalReusePreservesCompactSource() throws Exception {
    try {
      ImageRasterSmokeTestSupport.setRasterFeatures(true, false, false);
      Image source = ImageCompactStorageSmokeSupport.load("opaque-color.png");
      source.getPixels();
      CompactSourceState imageSourceState = new CompactSourceState(source);
      Image deferred = source.getClippedInstance(0, 0, WIDTH, HEIGHT);
      PipelineRootState pipelineRootState = new PipelineRootState(deferred);
      deferred.resolveForDrawing(1);
      Image cached = deferred.resolveForDrawing(1);

      Image expectedTarget = new Image(WIDTH, HEIGHT);
      expectedTarget.getGraphics().copyRect(cached, 0, 0, WIDTH, HEIGHT, 0, 0);
      int[] expected = expectedTarget.getPixels();
      Image target = new Image(WIDTH, HEIGHT);
      ImageRasterFeatureBridge.resetDrawAccountingForTest();
      target.getGraphics().copyRect(deferred, 0, 0, WIDTH, HEIGHT, 0, 0);

      return same(expected, target.getPixels())
          && ImageRasterFeatureBridge.cachedFinalRasterHitsForTest == 1
          && ImageRasterFeatureBridge.copyRectPlanAttemptsForTest == 0
          && imageSourceState.isCompact(NativeImageBacking.FORMAT_RGB565)
          && pipelineRootState.isCompact(NativeImageBacking.FORMAT_RGB565)
          && imageSourceState.preserved() && pipelineRootState.preserved(deferred);
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

  private static String firstDifference(int[] first, int[] second) {
    if (first == null || second == null || first.length != second.length) {
      return "length=" + (first == null ? -1 : first.length) + "/" + (second == null ? -1 : second.length);
    }
    for (int i = 0; i < first.length; i++) {
      if (first[i] != second[i]) {
        return "index=" + i + ",pixels=" + Integer.toHexString(first[i]) + "/"
            + Integer.toHexString(second[i]);
      }
    }
    return "none";
  }

  private static boolean same(int[] expected, int[] actual) {
    return java.util.Arrays.equals(expected, actual);
  }
}
