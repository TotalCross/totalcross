// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import totalcross.io.ByteArrayStream;
import totalcross.io.DataStream;

class ImageDeferredFrameStateTest {
  private static final int FRAME_ZERO = 0xFF204060;
  private static final int FRAME_ONE = 0xFFB06020;

  @Test
  void currentFrameNavigationIsMetadataOnlyAndReusesCachedVariants() throws Exception {
    Image image = new Image(twoFramePng()).getSmoothScaledInstance(4, 2);
    assertNull(image.backing);
    assertEquals(0, image.getCurrentFrame());

    Image oneX = image.resolveForDrawing(1);
    Image twoX = image.resolveForDrawing(2);
    int[] oneFrameZero = oneX.getPixels().clone();
    int[] twoFrameZero = twoX.getPixels().clone();

    image.setCurrentFrame(-1);
    assertEquals(1, image.getCurrentFrame());
    assertNull(image.backing);
    assertSame(oneX, image.resolveForDrawing(1));
    assertSame(twoX, image.resolveForDrawing(2));
    assertFalse(Arrays.equals(oneFrameZero, oneX.getPixels()));
    assertFalse(Arrays.equals(twoFrameZero, twoX.getPixels()));

    image.nextFrame();
    assertEquals(0, image.getCurrentFrame());
    image.prevFrame();
    assertEquals(1, image.getCurrentFrame());
    image.setCurrentFrame(99);
    assertEquals(0, image.getCurrentFrame());
    image.setCurrentFrame(-99);
    assertEquals(1, image.getCurrentFrame());
  }

  @Test
  void canonicalBarrierAdoptsTheSelectedFrame() throws Exception {
    Image actual = new Image(twoFramePng());
    actual.setCurrentFrame(1);
    assertNull(actual.backing);
    int[] expected = new Image(twoFramePng()).getFrameInstance(1).getPixels();

    assertArrayEquals(expected, actual.getPixels());
    assertNull(pipeline(actual));
    assertEquals(1, actual.getCurrentFrame());
  }

  @Test
  void explicitFrameExtractionAndCropCaptureAreIndependentOfLaterState() throws Exception {
    Image source = new Image(twoFramePng());
    source.setCurrentFrame(1);
    Image explicitFirst = source.getFrameInstance(0);
    Image cropOfSecond = source.getClippedInstance(0, 0, 2, 2);
    source.setCurrentFrame(0);

    assertArrayEquals(new Image(twoFramePng()).getFrameInstance(0).getPixels(), explicitFirst.getPixels());
    Image expectedCrop = new Image(twoFramePng()).getFrameInstance(1).getClippedInstance(0, 0, 2, 2);
    assertArrayEquals(expectedCrop.getPixels(), cropOfSecond.getPixels());
    assertEquals(0, source.getCurrentFrame());
  }

  @Test
  void deferredColorMutationFrameSideEffectsRemainCompatible() throws Exception {
    Image color = new Image(twoFramePng());
    color.setCurrentFrame(1);
    color.applyColor(0xFF804020);
    assertEquals(0, color.getCurrentFrame());
    assertNotNull(pipeline(color));

    Image fade = new Image(twoFramePng());
    fade.setCurrentFrame(1);
    fade.applyFade(137);
    assertEquals(1, fade.getCurrentFrame());
    Image expectedFade = new Image(twoFramePng());
    expectedFade.getPixels();
    expectedFade.setCurrentFrame(1);
    expectedFade.applyFade(137);
    assertArrayEquals(expectedFade.getPixels(), fade.getPixels());

    Image transparent = new Image(twoFramePng());
    transparent.setCurrentFrame(1);
    transparent.setTransparentColor(0x204060);
    assertEquals(1, transparent.getCurrentFrame());
    assertNotNull(pipeline(transparent));
  }

  @Test
  void deferredFadeUsesTheCallTimeFrameBeforeFinalPresentationSync() throws Exception {
    assertFadeOrdering(0, 1);
    assertFadeOrdering(1, 0);

    Image expected = new Image(twoFramePng());
    expected.getPixels();
    expected.setCurrentFrame(1);
    expected.applyFade(137);
    expected.setCurrentFrame(0);
    expected.applyFade(137);
    expected.setCurrentFrame(1);

    Image actual = new Image(twoFramePng());
    actual.setCurrentFrame(1);
    actual.applyFade(137);
    actual.setCurrentFrame(0);
    actual.applyFade(137);
    actual.setCurrentFrame(1);

    assertArrayEquals(allFramePixels(expected), allFramePixels(actual));
    assertArrayEquals(expected.getPixels(), actual.getPixels());
    assertEquals(1, actual.getCurrentFrame());
  }

  private static void assertFadeOrdering(int applyFrame, int finalFrame) throws Exception {
    Image expected = new Image(twoFramePng());
    expected.getPixels();
    expected.setCurrentFrame(applyFrame);
    expected.applyFade(137);
    expected.setCurrentFrame(finalFrame);

    Image actual = new Image(twoFramePng());
    actual.setCurrentFrame(applyFrame);
    actual.applyFade(137);
    actual.setCurrentFrame(finalFrame);

    assertArrayEquals(allFramePixels(expected), allFramePixels(actual));
    assertArrayEquals(expected.getPixels(), actual.getPixels());
  }

  @Test
  void deferredFrameLayoutUpdatesMetadataAndPreservesNonDivisibleStrip() throws Exception {
    Image image = new Image(stripPng(5, 2));
    image.setFrameCount(2);

    assertNull(image.backing);
    assertEquals(2, image.getFrameCount());
    assertEquals(2, image.getWidth());
    assertEquals(2, intField(image, "logicalWidth"));
    assertEquals("FC=2", image.comment);
    assertEquals(0, image.getCurrentFrame());
    assertEquals(ImagePipeline.FRAME_LAYOUT, pipeline(image).operationType());

    Image resolved = image.resolveForDrawing(1);
    assertEquals(2, resolved.getPixelWidth());
    assertEquals(5, intField(resolved, "widthOfAllFrames"));
    assertArrayEquals(new int[] { stripPixel(0), stripPixel(1), stripPixel(0), stripPixel(1), }, resolved.getPixels());
    image.setCurrentFrame(1);
    assertSame(resolved, image.resolveForDrawing(1));
    assertArrayEquals(new int[] { stripPixel(2), stripPixel(3), stripPixel(2), stripPixel(3), }, resolved.getPixels());
  }

  @Test
  void pngAndJpegSingleFrameSourcesStayDeferredUntilFrameLayoutResolution() throws Exception {
    for (byte[] encoded : new byte[][] { stripPng(6, 2), jpeg(6, 2) }) {
      Image actual = new Image(encoded);
      Image expected = new Image(encoded);
      expected.getPixels();
      expected.setFrameCount(2);

      actual.setFrameCount(2);
      assertNull(actual.backing);
      assertEquals(2, actual.getFrameCount());
      assertArrayEquals(expected.getPixels(), actual.getPixels());
    }
  }

  @Test
  void deferredFrameLayoutKeepsLogicalWidthAtDestinationScaleAndRoundTripsFullStrip() throws Exception {
    Image image = new Image(stripPng(5, 2)).getSmoothScaledInstance(3, 2);
    image.setFrameCount(2);
    assertEquals(1, image.getWidth());
    assertEquals(1, intField(image, "logicalWidth"));
    assertEquals(2, image.resolveForDrawing(2).getPixelWidth());
    assertEquals(1, image.resolveForDrawing(2).getWidth());

    Image roundTripSource = new Image(stripPng(5, 2));
    roundTripSource.setFrameCount(2);
    ByteArrayStream output = new ByteArrayStream(256);
    roundTripSource.createPng(new DataStream(output));
    ImageEncodedStructure.Inspection inspection = ImageEncodedStructure.inspect(output.getBuffer(), output.getPos());
    assertEquals(5, inspection.width);
    assertEquals(2, inspection.frameCount);
    assertEquals("FC=2", inspection.comment);
  }

  @Test
  void deferredFrameLayoutPreservesHighDensityPhysicalAndLogicalDimensions() throws Exception {
    assertHighDensityFrameLayout(2.0);
    assertHighDensityFrameLayout(1.5);
  }

  @Test
  void deferredFrameSelectionPreservesExactFractionalContentScale() throws Exception {
    assertExactFractionalFrameLayout(1.1, 6);
    assertExactFractionalFrameLayout(1.25, 6);
    assertExactFractionalFrameLayout(1.75, 5);
  }

  private static void assertExactFractionalFrameLayout(double contentScale, int logicalWidth) throws Exception {
    int fullPhysicalWidth = (int) Math.ceil(logicalWidth * contentScale);
    Image expectedSource = Image.createLogical(logicalWidth, 2, contentScale);
    fillPhysicalStrip(expectedSource);
    assertEquals(fullPhysicalWidth, expectedSource.getPixelWidth());
    Image expected = expectedSource.getFrameInstance(0);
    expected.getPixels();
    expected.setFrameCount(2);

    Image actualSource = Image.createLogical(logicalWidth, 2, contentScale);
    fillPhysicalStrip(actualSource);
    assertEquals(fullPhysicalWidth, actualSource.getPixelWidth());
    Image actual = actualSource.getFrameInstance(0);
    actual.setFrameCount(2);
    assertFrameLayoutMetadataBeforeBarrier(expected, actual, fullPhysicalWidth, contentScale);
    assertArrayEquals(expected.getPixels(), actual.getPixels());
    actual.setCurrentFrame(1);
    expected.setCurrentFrame(1);
    assertArrayEquals(expected.getPixels(), actual.getPixels());
    assertFrameLayoutMetadataAfterBarrier(expected, actual, fullPhysicalWidth, contentScale);
    assertNull(pipeline(actual));
  }

  @Test
  void deferredCroppedFrameLayoutPreservesExactFractionalContentScale() throws Exception {
    assertExactFractionalCroppedFrameLayout(1.1, 6);
    assertExactFractionalCroppedFrameLayout(1.25, 6);
    assertExactFractionalCroppedFrameLayout(1.75, 5);
  }

  private static void assertExactFractionalCroppedFrameLayout(double contentScale, int logicalWidth) throws Exception {
    int fullPhysicalWidth = (int) Math.ceil(logicalWidth * contentScale);
    Image expectedSource = Image.createLogical(logicalWidth, 2, contentScale);
    fillPhysicalStrip(expectedSource);
    assertEquals(fullPhysicalWidth, expectedSource.getPixelWidth());
    Image expected = expectedSource.getClippedInstance(0, 0, logicalWidth, 2);
    expected.getPixels();
    expected.setFrameCount(2);

    Image actualSource = Image.createLogical(logicalWidth, 2, contentScale);
    fillPhysicalStrip(actualSource);
    assertEquals(fullPhysicalWidth, actualSource.getPixelWidth());
    Image actual = actualSource.getClippedInstance(0, 0, logicalWidth, 2);
    actual.setFrameCount(2);
    assertFrameLayoutMetadataBeforeBarrier(expected, actual, fullPhysicalWidth, contentScale);
    assertArrayEquals(expected.getPixels(), actual.getPixels());
    actual.setCurrentFrame(1);
    expected.setCurrentFrame(1);
    assertArrayEquals(expected.getPixels(), actual.getPixels());
    assertFrameLayoutMetadataAfterBarrier(expected, actual, fullPhysicalWidth, contentScale);
    assertNull(pipeline(actual));
  }

  private static void assertFrameLayoutMetadataBeforeBarrier(Image expected, Image actual, int fullPhysicalWidth,
      double contentScale) throws Exception {
    assertEquals(2, actual.getFrameCount());
    assertEquals(contentScale, actual.getContentScale());
    assertEquals(expected.getPixelWidth(), actual.getPixelWidth());
    assertEquals(expected.getWidth(), actual.getWidth());
    assertEquals(fullPhysicalWidth / 2, actual.getPixelWidth());
    assertEquals((int) Math.ceil(actual.getPixelWidth() / contentScale), actual.getWidth());
    assertEquals(intField(expected, "widthOfAllFrames"), intField(actual, "widthOfAllFrames"));
    assertEquals(fullPhysicalWidth, intField(actual, "widthOfAllFrames"));
  }

  private static void assertFrameLayoutMetadataAfterBarrier(Image expected, Image actual, int fullPhysicalWidth,
      double contentScale) throws Exception {
    assertEquals(2, actual.getFrameCount());
    assertEquals(contentScale, actual.getContentScale());
    assertEquals(expected.getPixelWidth(), actual.getPixelWidth());
    assertEquals(expected.getWidth(), actual.getWidth());
    assertEquals(fullPhysicalWidth / 2, actual.getPixelWidth());
    assertEquals((int) Math.ceil(actual.getPixelWidth() / contentScale), actual.getWidth());
    assertEquals(intField(expected, "widthOfAllFrames"), intField(actual, "widthOfAllFrames"));
    assertEquals(fullPhysicalWidth, intField(actual, "widthOfAllFrames"));
  }

  @Test
  void deferredCroppedFrameLayoutPreservesHighDensityRasterSemantics() throws Exception {
    assertCroppedHighDensityFrameLayout(2.0);
    assertCroppedHighDensityFrameLayout(1.5);
  }

  private static void assertCroppedHighDensityFrameLayout(double contentScale) throws Exception {
    Image eagerSource = highDensityStrip(contentScale);
    Image expected = eagerSource.getClippedInstance(0, 0, 4, 2);
    expected.getPixels();
    expected.setFrameCount(2);

    Image actual = highDensityStrip(contentScale).getClippedInstance(0, 0, 4, 2);
    actual.setFrameCount(2);
    assertNull(actual.backing);
    assertEquals(expected.getPixelWidth(), actual.getPixelWidth());
    assertEquals(expected.getWidth(), actual.getWidth());
    assertEquals(expected.getContentScale(), actual.getContentScale());
    assertEquals(intField(expected, "widthOfAllFrames"), intField(actual, "widthOfAllFrames"));

    assertArrayEquals(expected.getPixels(), actual.getPixels());
    actual.setCurrentFrame(1);
    expected.setCurrentFrame(1);
    assertArrayEquals(expected.getPixels(), actual.getPixels());
    assertNull(pipeline(actual));
  }

  private static void assertHighDensityFrameLayout(double contentScale) throws Exception {
    Image expected = highDensityStrip(contentScale);
    int fullPhysicalWidth = expected.getPixelWidth();
    int physicalFrameWidth = fullPhysicalWidth / 2;
    int logicalFrameWidth = (int) Math.ceil(physicalFrameWidth / contentScale);
    expected.setFrameCount(2);

    Image actual = highDensityStrip(contentScale).getFrameInstance(0);
    actual.setFrameCount(2);
    assertNull(actual.backing);
    assertEquals(physicalFrameWidth, actual.getPixelWidth());
    assertEquals(logicalFrameWidth, actual.getWidth());
    assertEquals(logicalFrameWidth, intField(actual, "logicalWidth"));

    assertArrayEquals(expected.getPixels(), actual.getPixels());
    expected.setCurrentFrame(1);
    actual.setCurrentFrame(1);
    assertArrayEquals(expected.getPixels(), actual.getPixels());
  }

  @Test
  void deferredFrameLayoutPreservesLegacyZeroWidthFrameBehavior() throws Exception {
    Image expected = new Image(1, 2);
    fillPhysicalStrip(expected);
    expected.setFrameCount(2);

    Image actual = new Image(stripPng(1, 2));
    actual.setFrameCount(2);
    assertEquals(0, actual.getPixelWidth());
    assertEquals(0, actual.getWidth());
    assertEquals(0, intField(actual, "logicalWidth"));
    assertEquals(2, actual.getFrameCount());
    assertEquals(0, actual.resolveForDrawing(1).getPixels().length);

    actual.setCurrentFrame(1);
    assertEquals(0, actual.getPixels().length);
    assertArrayEquals(allFramePixels(expected), allFramePixels(actual));
  }

  @Test
  void frameLayoutValidationIsImmediateAndSameCountIsNoOp() throws Exception {
    Image image = new Image(stripPng(5, 2));
    assertThrows(IllegalArgumentException.class, () -> image.setFrameCount(0));
    assertNull(image.backing);
    image.setFrameCount(2);
    ImagePipeline layout = pipeline(image);
    image.setFrameCount(2);
    assertSame(layout, pipeline(image));
    assertThrows(IllegalStateException.class, () -> image.setFrameCount(3));
    assertNull(image.backing);

    Image multi = new Image(twoFramePng());
    assertThrows(IllegalStateException.class, () -> multi.setFrameCount(3));
    assertNull(multi.backing);
  }

  @Test
  void frameLayoutAllocationFailureIsRetryableAndDoesNotPoisonEncodedSource() throws Exception {
    Image image = new Image(stripPng(5, 2));
    image.setFrameCount(2);
    EncodedImageSource source = (EncodedImageSource) pipeline(image).root();
    Image.failNextMaterializedFrameBufferAllocationForTest();
    assertThrows(TransientImageMaterializationException.class, () -> image.resolveForDrawing(1));
    assertNull(source.decodeFailure());
    assertNotNull(pipeline(image));
    assertNotNull(image.resolveForDrawing(1));
  }

  private static ImagePipeline pipeline(Image image) throws Exception {
    Field field = Image.class.getDeclaredField("pipeline");
    field.setAccessible(true);
    return (ImagePipeline) field.get(image);
  }

  private static int intField(Image image, String name) throws Exception {
    Field field = Image.class.getDeclaredField(name);
    field.setAccessible(true);
    return field.getInt(image);
  }

  private static int stripPixel(int x) {
    return 0xFF000000 | (x + 1) << 16 | (x + 2) << 8 | x + 3;
  }

  private static Image highDensityStrip(double contentScale) throws Exception {
    Image image = Image.createLogical(4, 2, contentScale);
    fillPhysicalStrip(image);
    return image;
  }

  private static void fillPhysicalStrip(Image image) {
    int width = image.getPixelWidth();
    int[] pixels = image.getPixels();
    for (int y = 0; y < image.getPixelHeight(); y++) {
      for (int x = 0; x < width; x++) {
        pixels[y * width + x] = stripPixel(x);
      }
    }
  }

  private static int[] allFramePixels(Image image) throws Exception {
    image.getPixels();
    return ((RasterImageBacking) image.backing).pixelsOfAllFrames().clone();
  }

  private static byte[] twoFramePng() throws Exception {
    BufferedImage source = new BufferedImage(8, 2, BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < source.getHeight(); y++) {
      for (int x = 0; x < source.getWidth(); x++) {
        source.setRGB(x, y, x < 4 ? FRAME_ZERO : FRAME_ONE);
      }
    }
    return writePng(source, "FC=2");
  }

  private static byte[] stripPng(int width, int height) throws Exception {
    BufferedImage source = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        source.setRGB(x, y, stripPixel(x));
      }
    }
    return writePng(source, null);
  }

  private static byte[] jpeg(int width, int height) throws Exception {
    BufferedImage source = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        source.setRGB(x, y, (x * 0x220000) | (y * 0x003300));
      }
    }
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    assertTrue(ImageIO.write(source, "jpg", output));
    return output.toByteArray();
  }

  private static byte[] writePng(BufferedImage source, String comment) throws Exception {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    assertTrue(ImageIO.write(source, "png", output));
    byte[] bytes = output.toByteArray();
    if (comment == null) {
      return bytes;
    }
    int iend = bytes.length - 12;
    ByteArrayOutputStream withComment = new ByteArrayOutputStream(bytes.length + 32);
    withComment.write(bytes, 0, iend);
    byte[] text = ("Comment\0" + comment).getBytes("ISO-8859-1");
    byte[] type = "tEXt".getBytes("ISO-8859-1");
    writeInt(withComment, text.length);
    withComment.write(type);
    withComment.write(text);
    java.util.zip.CRC32 crc = new java.util.zip.CRC32();
    crc.update(type);
    crc.update(text);
    writeInt(withComment, (int) crc.getValue());
    withComment.write(bytes, iend, 12);
    return withComment.toByteArray();
  }

  private static void writeInt(ByteArrayOutputStream output, int value) {
    output.write(value >> 24);
    output.write(value >> 16);
    output.write(value >> 8);
    output.write(value);
  }
}
