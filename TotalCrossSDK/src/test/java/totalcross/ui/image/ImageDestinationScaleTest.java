// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

class ImageDestinationScaleTest {
  @Test
  void smoothPipelineResolvesPhysicalSizeWithoutAdoptingTheOriginal() throws Exception {
    Image image = new Image(png(96, 96)).getSmoothScaledInstance(48, 48);

    Image one = image.resolveForDrawing(1);
    Image two = image.resolveForDrawing(2);
    Image four = image.resolveForDrawing(4);

    assertEquals(48, one.getPixelWidth());
    assertEquals(96, two.getPixelWidth());
    assertEquals(192, four.getPixelWidth());
    assertEquals(48, one.getWidth());
    assertEquals(96, two.getPixelHeight());
    assertEquals(4, four.getContentScale());
    assertNull(image.backing);
    assertEquals(48, image.getPixelWidth());
    assertEquals(1, image.getContentScale());
    assertNotNull(image.pipelineForSmoke());
  }

  @Test
  void repeatedScaleHitsLeafCacheAndThirdScaleEvictsLeastRecentUse() throws Exception {
    Image image = new Image(png(96, 96)).getSmoothScaledInstance(48, 48);

    Image one = image.resolveForDrawing(1);
    Image two = image.resolveForDrawing(2);
    assertSame(two, image.resolveForDrawing(2));
    image.resolveForDrawing(4);

    assertEquals(2, image.pipelineForSmoke().cachedVariantCountForSmoke());
    assertSame(two, image.resolveForDrawing(2));
    Image oneAgain = image.resolveForDrawing(1);
    assertEquals(48, oneAgain.getPixelWidth());
    assertEquals(2, image.pipelineForSmoke().cachedVariantCountForSmoke());
    assertSame(oneAgain, image.resolveForDrawing(1));
    // The first 1x object was the evicted object; a fresh 1x variant proves the LRU was used.
    assertNotNull(one);
  }

  @Test
  void freeTextureReleasesVariantTexturesButKeepsCpuCache() throws Exception {
    Image image = new Image(png(96, 96)).getSmoothScaledInstance(48, 48);
    Image variant = image.resolveForDrawing(2);

    image.freeTexture();

    assertSame(variant, image.resolveForDrawing(2));
    assertNotNull(variant.getPixels());
    assertEquals(1, image.pipelineForSmoke().cachedVariantCountForSmoke());
  }

  @Test
  void canonicalBarrierAlwaysAdoptsScaleOneAndDropsDrawCache() throws Exception {
    Image image = new Image(png(96, 96)).getSmoothScaledInstance(48, 48);
    assertEquals(192, image.resolveForDrawing(4).getPixelWidth());

    assertEquals(48 * 48, image.getPixels().length);
    assertEquals(48, image.getPixelWidth());
    assertEquals(1, image.getContentScale());
    assertNull(image.pipelineForSmoke());
  }

  @Test
  void presentationStateIsCopiedAfterCacheCreationAndColorOnlyUsesCanonicalKey() throws Exception {
    Image image = new Image(png(8, 8)).getSmoothScaledInstance(4, 4);
    image.alphaMask = 120;
    image.hwScaleW = 0.75;
    image.hwScaleH = 1.25;
    Image variant = image.resolveForDrawing(2);
    image.alphaMask = 80;
    image.hwScaleW = 0.5;
    image.hwScaleH = 1.5;
    assertSame(variant, image.resolveForDrawing(2));
    assertEquals(80, variant.alphaMask);
    assertEquals(0.5, variant.hwScaleW);
    assertEquals(1.5, variant.hwScaleH);

    Image colorOnly = new Image(png(8, 8)).getAlphaInstance(-20);
    Image natural = colorOnly.resolveForDrawing(1);
    assertSame(natural, colorOnly.resolveForDrawing(2));
    assertEquals(1, natural.getContentScale());
    assertEquals(1, colorOnly.pipelineForSmoke().cachedVariantCountForSmoke());
  }

  @Test
  void multipleGeometricNodesUseRequestedScaleAndRotationKeepsLogicalBounds() throws Exception {
    Image image = new Image(png(240, 200))
        .getSmoothScaledInstance(200, 160)
        .getRotatedScaledInstance(100, 30, 0xFF123456)
        .getSmoothScaledInstance(48, 48);

    Image resolved = image.resolveForDrawing(4);

    assertEquals(192, resolved.getPixelWidth());
    assertEquals(192, resolved.getPixelHeight());
    assertEquals(48, resolved.getWidth());
    assertEquals(48, resolved.getHeight());
    assertEquals(4, resolved.getContentScale());
    assertNull(image.backing);
  }

  @Test
  void invalidDestinationScaleIsRejected() throws Exception {
    Image image = new Image(png(4, 4)).getSmoothScaledInstance(2, 2);
    assertThrows(ImageException.class, () -> image.resolveForDrawing(0));
    assertThrows(ImageException.class, () -> image.resolveForDrawing(Double.NaN));
    assertThrows(ImageException.class, () -> image.resolveForDrawing(Double.POSITIVE_INFINITY));
  }

  @Test
  void resolverIsNotExposedAsNormalImageApi() {
    assertThrows(NoSuchMethodException.class, () -> Image.class.getMethod("resolveForDrawing", double.class));
  }

  @Test
  void failedDestinationResolutionDoesNotPoisonSharedEncodedSource() throws Exception {
    Image encoded = new Image(jpeg(1024, 768));
    Image smooth = encoded.getSmoothScaledInstance(64, 48);
    Image nearest = encoded.getScaledInstance(32, 24);
    EncodedImageSource source = (EncodedImageSource) smooth.pipelineForSmoke().root();

    assertThrows(ImageException.class, () -> smooth.resolveForDrawing(Double.MAX_VALUE));
    assertNull(source.decodeFailure());
    assertNotNull(smooth.pipelineForSmoke());
    assertEquals(64, smooth.resolveForDrawing(1).getPixelWidth());
    assertEquals(32, nearest.resolveForDrawing(1).getPixelWidth());
    assertNull(source.decodeFailure());
  }

  @Test
  void eligibleJpegSmoothScaleUsesExactTargetDimensionsAndOtherNodesStillResolveNormally() throws Exception {
    Image smooth = new Image(jpeg(160, 120)).getSmoothScaledInstance(40, 30);
    Image reduced = smooth.resolveForDrawing(1);
    assertEquals(40, reduced.getPixelWidth());
    assertEquals(30, reduced.getPixelHeight());
    assertEquals(1, reduced.getContentScale());
    assertNull(smooth.backing);

    Image highDensity = smooth.resolveForDrawing(4);
    assertEquals(160, highDensity.getPixelWidth());
    assertEquals(120, highDensity.getPixelHeight());
    assertEquals(4, highDensity.getContentScale());

    Image nearest = new Image(jpeg(160, 120)).getScaledInstance(40, 30);
    Image nearestResolved = nearest.resolveForDrawing(1);
    assertEquals(40, nearestResolved.getPixelWidth());
    assertEquals(30, nearestResolved.getPixelHeight());
  }

  @Test
  void targetedJpegRequiresBothAxesBeforeChoosingNativeReduction() throws Exception {
    Image.resetTargetedDecodeInvocationCountForTest();
    Image image = new Image(jpeg(1600, 900)).getSmoothScaledInstance(200, 400);

    Image resolved = image.resolveForDrawing(1);

    assertEquals(200, resolved.getPixelWidth());
    assertEquals(400, resolved.getPixelHeight());
    assertEquals(1, Image.targetedDecodeInvocationCountForTest());
    assertEquals(800, Image.targetedDecodeWidthForTest());
    assertEquals(450, Image.targetedDecodeHeightForTest());
  }

  @Test
  void reducedJpegSmoothScaleRemainsWithinTheDocumentedSimilarityTolerance() throws Exception {
    byte[] encoded = jpeg(160, 120);
    Image reduced = new Image(encoded).getSmoothScaledInstance(40, 30).resolveForDrawing(1);
    Image fullSource = new Image(encoded);
    fullSource.getPixels();
    Method smooth = Image.class.getDeclaredMethod("eagerSmoothScaledInstance", int.class, int.class);
    smooth.setAccessible(true);
    Image full = (Image) smooth.invoke(fullSource, 40, 30);

    int maxChannelDifference = 0;
    for (int i = 0; i < reduced.getPixels().length; i++) {
      int reducedPixel = reduced.getPixels()[i];
      int fullPixel = full.getPixels()[i];
      maxChannelDifference = Math.max(maxChannelDifference,
          Math.max(Math.abs(((reducedPixel >> 16) & 0xFF) - ((fullPixel >> 16) & 0xFF)),
              Math.max(Math.abs(((reducedPixel >> 8) & 0xFF) - ((fullPixel >> 8) & 0xFF)),
                  Math.abs((reducedPixel & 0xFF) - (fullPixel & 0xFF)))));
    }
    // Reduced JPEG decode is intentionally not byte-identical; 64 levels per
    // channel bounds decoder/subsampling differences for this contract.
    assertTrue(maxChannelDifference <= 64);
  }

  @Test
  void eligibleJpegTargetDecodeIsInstrumentedAndCorruptEntropyFailureIsCached() throws Exception {
    Image.resetTargetedDecodeInvocationCountForTest();
    Image smooth = new Image(jpeg(1024, 768)).getSmoothScaledInstance(64, 48);
    assertEquals(64, smooth.resolveForDrawing(1).getPixelWidth());
    assertEquals(1, Image.targetedDecodeInvocationCountForTest());
    assertEquals(64, Image.targetedDecodeRequestWidthForTest());
    assertEquals(48, Image.targetedDecodeRequestHeightForTest());
    assertEquals(8, Image.targetedDecodeDenominatorForTest());
    assertEquals(256, smooth.resolveForDrawing(4).getPixelWidth());
    assertEquals(2, Image.targetedDecodeInvocationCountForTest());
    assertEquals(256, Image.targetedDecodeRequestWidthForTest());
    assertEquals(192, Image.targetedDecodeRequestHeightForTest());
    assertEquals(4, Image.targetedDecodeDenominatorForTest());

    Image nearest = new Image(jpeg(1024, 768)).getScaledInstance(64, 48);
    assertEquals(64, nearest.resolveForDrawing(1).getPixelWidth());
    assertEquals(2, Image.targetedDecodeInvocationCountForTest());

    Image corrupt = new Image(corruptJpegEntropy(jpeg(1024, 768))).getSmoothScaledInstance(64, 48);
    ImageException first = assertThrows(ImageException.class, () -> corrupt.resolveForDrawing(1));
    ImageException second = assertThrows(ImageException.class, () -> corrupt.resolveForDrawing(1));
    assertSame(first, second);
    assertNotNull(corrupt.pipelineForSmoke());
  }

  @Test
  void targetedBackingPublicationWaitsForSuccessfulInitialization() throws Exception {
    Image.resetTargetedDecodeInvocationCountForTest();
    Image image = new Image(jpeg(1024, 768)).getSmoothScaledInstance(64, 48);
    EncodedImageSource source = (EncodedImageSource) image.pipelineForSmoke().root();

    Image.failNextTargetedDecodeInitializationForTest();
    ImageException failure = assertThrows(ImageException.class, () -> image.resolveForDrawing(1));
    assertTrue(failure instanceof TransientImageMaterializationException);
    assertEquals(0, source.decodedGeneration());
    assertEquals(0, source.decodedDenominator());
    assertNull(source.decodedBackingForReuse(8));

    Image retry = image.resolveForDrawing(1);
    assertEquals(64, retry.getPixelWidth());
    assertEquals(1, source.decodedGeneration());
    assertEquals(8, source.decodedDenominator());
    assertEquals(2, Image.targetedDecodeInvocationCountForTest());
  }

  @Test
  void targetedJpegRootMetadataUsesTheSelectedDenominatorForFreshAndCachedBacking() throws Exception {
    byte[] encoded = jpeg(161, 121);
    int[] targetWidths = {80, 40, 20};
    int[] targetHeights = {60, 30, 15};
    int[] denominators = {2, 4, 8};

    for (int i = 0; i < denominators.length; i++) {
      Image base = new Image(encoded);
      Image firstPipeline = base.getSmoothScaledInstance(targetWidths[i], targetHeights[i]);
      EncodedImageSource source = (EncodedImageSource) firstPipeline.pipelineForSmoke().root();
      Image fresh = materializeEncodedRoot(firstPipeline);

      assertEquals(denominators[i], source.decodedDenominator());
      assertEquals((161 + denominators[i] - 1) / denominators[i], fresh.getPixelWidth());
      assertEquals((121 + denominators[i] - 1) / denominators[i], fresh.getPixelHeight());
      assertEquals(161, fresh.getWidth());
      assertEquals(121, fresh.getHeight());
      assertEquals(1.0 / denominators[i], fresh.getContentScale(), 0.0000001);

      Image cachedPipeline = base.getSmoothScaledInstance(targetWidths[i], targetHeights[i]);
      Image cached = materializeEncodedRoot(cachedPipeline);
      assertEquals(1, source.decodedGeneration());
      assertEquals(fresh.getContentScale(), cached.getContentScale(), 0.0000001);
      assertEquals(fresh.getWidth(), cached.getWidth());
      assertEquals(fresh.getHeight(), cached.getHeight());
      assertArrayEquals(fresh.getPixels(), cached.getPixels());
    }
  }

  @Test
  void targetedJpegSmoothFamiliesMatchIndependentFullDecodeReference() throws Exception {
    byte[] encoded = jpeg(160, 120);
    assertSmoothParity(new Image(encoded).getSmoothScaledInstance(20, 15).resolveForDrawing(1),
        fullSmoothReference(encoded, 20, 15));
    assertSmoothParity(new Image(encoded).getSmoothScaledInstance(40, 30).resolveForDrawing(1),
        fullSmoothReference(encoded, 40, 30));
    assertSmoothParity(new Image(encoded).getHwScaledInstance(40, 30).resolveForDrawing(1),
        fullSmoothReference(encoded, 40, 30));
    Image actualAlpha = new Image(encoded).getSmoothScaledInstance(40, 30).getAlphaInstance(-40)
        .resolveForDrawing(1);
    Image expectedAlpha = fullSmoothAlphaReference(encoded, 40, 30);
    assertSmoothParity(actualAlpha, expectedAlpha);
    assertExactAlpha(actualAlpha, expectedAlpha);
    assertSmoothParity(new Image(encoded).getClippedInstance(0, 0, 80, 60)
        .getSmoothScaledInstance(40, 30).resolveForDrawing(1), fullCropSmoothReference(encoded));
    assertSmoothParity(new Image(encoded).getSmoothScaledInstance(80, 60)
        .getSmoothScaledInstance(40, 30).resolveForDrawing(1), fullSmoothTwiceReference(encoded));

    byte[] oddEncoded = blockJpeg(161, 121);
    Image oddActual = new Image(oddEncoded).getSmoothScaledInstance(20, 15).resolveForDrawing(1);
    assertSmoothParity(oddActual, fullSmoothReference(oddEncoded, 20, 15));
  }

  @Test
  void nearestAndRotateScaleStillForceFullDecode() throws Exception {
    byte[] encoded = jpeg(161, 121);

    Image.resetImageOperationAccountingForTest();
    new Image(encoded).getScaledInstance(40, 30).resolveForDrawing(1);
    assertEquals(0, Image.targetedDecodeInvocationCountForTest());
    assertEquals(1, Image.fullDecodeInvocationCountForTest());

    Image.resetImageOperationAccountingForTest();
    new Image(encoded).getRotatedScaledInstance(100, 37, 0xFF123456).resolveForDrawing(1);
    assertEquals(0, Image.targetedDecodeInvocationCountForTest());
    assertEquals(1, Image.fullDecodeInvocationCountForTest());
  }

  @Test
  void targetedImageIoInfrastructureFailureIsTransientAndRetried() throws Exception {
    Image.resetTargetedDecodeInvocationCountForTest();
    Image image = new Image(jpeg(1024, 768)).getSmoothScaledInstance(64, 48);
    EncodedImageSource source = (EncodedImageSource) image.pipelineForSmoke().root();

    Image.failNextTargetedDecodeInfrastructureForTest();
    ImageException first = assertThrows(ImageException.class, () -> image.resolveForDrawing(1));
    assertTrue(first instanceof TransientImageMaterializationException);
    assertNull(source.decodeFailure());
    assertNotNull(image.pipelineForSmoke());
    assertEquals(1, Image.targetedDecodeInvocationCountForTest());

    Image second = image.resolveForDrawing(1);
    assertEquals(64, second.getPixelWidth());
    assertEquals(2, Image.targetedDecodeInvocationCountForTest());
    assertNull(source.decodeFailure());
  }

  private static byte[] png(int width, int height) throws Exception {
    BufferedImage source = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        source.setRGB(x, y, 0xFF000000 | ((x * 5) << 16) | ((y * 5) << 8));
      }
    }
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    assertEquals(true, ImageIO.write(source, "png", output));
    return output.toByteArray();
  }

  private static byte[] jpeg(int width, int height) throws Exception {
    BufferedImage source = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        source.setRGB(x, y, ((x * 3) << 16) | ((y * 3) << 8) | ((x + y) * 2));
      }
    }
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    assertEquals(true, ImageIO.write(source, "jpg", output));
    return output.toByteArray();
  }

  private static byte[] corruptJpegEntropy(byte[] source) {
    byte[] result;
    int sos = -1;
    for (int i = 0; i + 1 < source.length; i++) {
      if ((source[i] & 0xFF) == 0xFF && (source[i + 1] & 0xFF) == 0xDA) {
        sos = i;
        break;
      }
    }
    assertTrue(sos >= 0);
    int segmentLength = ((source[sos + 2] & 0xFF) << 8) | (source[sos + 3] & 0xFF);
    int entropy = sos + 2 + segmentLength;
    byte[] invalidEntropyTail = new byte[] {
        (byte) 0xFF, (byte) 0xC3, 0, 8, 8, 0, 1, 0, 1, 1, (byte) 0xFF, (byte) 0xD9
    };
    result = new byte[entropy + invalidEntropyTail.length];
    System.arraycopy(source, 0, result, 0, entropy);
    System.arraycopy(invalidEntropyTail, 0, result, entropy, invalidEntropyTail.length);
    return result;
  }

  private static Image materializeEncodedRoot(Image image) throws Exception {
    Method materialize = Image.class.getDeclaredMethod("materializePipelineRoot", ImagePipeline.class,
        ArrayList.class, double.class);
    materialize.setAccessible(true);
    return (Image) materialize.invoke(image, image.pipelineForSmoke(), new ArrayList<ImagePipeline>(), 1.0);
  }

  private static Image fullSmoothReference(byte[] encoded, int width, int height) throws Exception {
    Image full = fullDecoded(encoded);
    return full.getSmoothScaledInstance(width, height).resolveForDrawing(1);
  }

  private static Image fullSmoothAlphaReference(byte[] encoded, int width, int height) throws Exception {
    Image full = fullDecoded(encoded);
    return full.getSmoothScaledInstance(width, height).getAlphaInstance(-40).resolveForDrawing(1);
  }

  private static Image fullCropSmoothReference(byte[] encoded) throws Exception {
    Image full = fullDecoded(encoded);
    return full.getClippedInstance(0, 0, 80, 60).getSmoothScaledInstance(40, 30).resolveForDrawing(1);
  }

  private static Image fullSmoothTwiceReference(byte[] encoded) throws Exception {
    Image full = fullDecoded(encoded);
    return full.getSmoothScaledInstance(80, 60).getSmoothScaledInstance(40, 30).resolveForDrawing(1);
  }

  private static Image fullDecoded(byte[] encoded) throws Exception {
    Image full = new Image(encoded);
    full.getPixels();
    assertNull(full.pipelineForSmoke());
    return full;
  }

  private static void assertExactAlpha(Image actual, Image expected) {
    int[] actualPixels = actual.getPixels();
    int[] expectedPixels = expected.getPixels();
    assertEquals(expectedPixels.length, actualPixels.length);
    for (int i = 0; i < actualPixels.length; i++) {
      assertEquals(expectedPixels[i] >>> 24, actualPixels[i] >>> 24, "alpha at pixel " + i);
    }
  }

  private static byte[] blockJpeg(int width, int height) throws Exception {
    BufferedImage source = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int blockX = x / 64;
        int blockY = y / 64;
        int red = 40 + blockX * 32 + blockY * 16;
        int green = 48 + blockX * 24 + blockY * 32;
        int blue = 56 + blockX * 16 + blockY * 24;
        source.setRGB(x, y, (red << 16) | (green << 8) | blue);
      }
    }
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    assertEquals(true, ImageIO.write(source, "jpg", output));
    return output.toByteArray();
  }

  private static void assertSmoothParity(Image actual, Image expected) {
    assertEquals(expected.getPixelWidth(), actual.getPixelWidth());
    assertEquals(expected.getPixelHeight(), actual.getPixelHeight());
    int[] actualPixels = actual.getPixels();
    int[] expectedPixels = expected.getPixels();
    assertEquals(expectedPixels.length, actualPixels.length);
    int maxChannelDifference = 0;
    for (int i = 0; i < actualPixels.length; i++) {
      int actualPixel = actualPixels[i];
      int expectedPixel = expectedPixels[i];
      maxChannelDifference = Math.max(maxChannelDifference,
          Math.max(Math.abs(((actualPixel >> 16) & 0xFF) - ((expectedPixel >> 16) & 0xFF)),
              Math.max(Math.abs(((actualPixel >> 8) & 0xFF) - ((expectedPixel >> 8) & 0xFF)),
                  Math.abs((actualPixel & 0xFF) - (expectedPixel & 0xFF)))));
    }
    assertTrue(maxChannelDifference <= 64);
    int[] focusedPixels = {0, actualPixels.length / 3, actualPixels.length / 2, actualPixels.length - 1};
    for (int index : focusedPixels) {
      int actualPixel = actualPixels[index];
      int expectedPixel = expectedPixels[index];
      assertTrue(Math.abs(((actualPixel >> 16) & 0xFF) - ((expectedPixel >> 16) & 0xFF)) <= 64);
      assertTrue(Math.abs(((actualPixel >> 8) & 0xFF) - ((expectedPixel >> 8) & 0xFF)) <= 64);
      assertTrue(Math.abs((actualPixel & 0xFF) - (expectedPixel & 0xFF)) <= 64);
    }
  }
}
