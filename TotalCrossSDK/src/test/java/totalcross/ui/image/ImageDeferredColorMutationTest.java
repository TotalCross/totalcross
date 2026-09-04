// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.CRC32;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

class ImageDeferredColorMutationTest {
  private static final int APPLY_COLOR = 0xFF804020;
  private static final int APPLY_COLOR2 = 0xFF4080C0;
  private static final int FADE = 137;
  private static final int TRANSPARENT = 0x000000;
  private static final int CHANGE_FROM = 0xFF000000;
  private static final int CHANGE_TO = 0xFF20A040;

  @Test
  void everyInPlaceMutationStaysDeferredAndMatchesAnEagerEncodedBaseline() throws Exception {
    assertDeferredAndEquivalent(Operation.APPLY_COLOR);
    assertDeferredAndEquivalent(Operation.APPLY_COLOR2);
    assertDeferredAndEquivalent(Operation.APPLY_FADE);
    assertDeferredAndEquivalent(Operation.CHANGE_COLORS);
    assertDeferredAndEquivalent(Operation.SET_TRANSPARENT_COLOR);
  }

  @Test
  void transparentColorReturnsTheSameReceiverAndPublicSignaturesRemainStable() throws Exception {
    Image image = new Image(png(6, 4));
    assertSame(image, image.setTransparentColor(TRANSPARENT));
    assertNotNullPipeline(image);

    assertEquals(void.class, Image.class.getMethod("applyColor", int.class).getReturnType());
    assertEquals(void.class, Image.class.getMethod("applyColor2", int.class).getReturnType());
    assertEquals(void.class, Image.class.getMethod("applyFade", int.class).getReturnType());
    assertEquals(void.class, Image.class.getMethod("changeColors", int.class, int.class).getReturnType());
    assertEquals(Image.class, Image.class.getMethod("setTransparentColor", int.class).getReturnType());
  }

  @Test
  void chainedMutationsPreserveCallOrderExactly() throws Exception {
    byte[] encoded = png(7, 5);
    Image expected = new Image(encoded);
    expected.getPixels();
    expected.applyFade(FADE);
    expected.applyColor(APPLY_COLOR);
    expected.changeColors(CHANGE_FROM, CHANGE_TO);
    expected.setTransparentColor(TRANSPARENT);
    expected.applyColor2(APPLY_COLOR2);

    Image actual = new Image(encoded);
    actual.applyFade(FADE);
    actual.applyColor(APPLY_COLOR);
    actual.changeColors(CHANGE_FROM, CHANGE_TO);
    actual.setTransparentColor(TRANSPARENT);
    actual.applyColor2(APPLY_COLOR2);

    assertEquals(Arrays.asList(ImagePipeline.APPLY_FADE, ImagePipeline.APPLY_COLOR,
        ImagePipeline.CHANGE_COLORS, ImagePipeline.SET_TRANSPARENT_COLOR, ImagePipeline.APPLY_COLOR2),
        operationTypes(pipeline(actual)));
    assertArrayEquals(expected.getPixels(), actual.getPixels());
  }

  @Test
  void deferredMutationInvalidatesCachedVariantsWithoutMutatingTheOldVariant() throws Exception {
    Image image = new Image(png(24, 20)).getSmoothScaledInstance(12, 10);
    Image oldVariant = image.resolveForDrawing(1);
    int[] oldPixels = oldVariant.getPixels().clone();
    assertEquals(1, pipeline(image).cachedVariantCountForSmoke());

    image.applyColor(0xFFFFFFFF);

    assertEquals(0, pipeline(image).cachedVariantCountForSmoke());
    assertArrayEquals(oldPixels, oldVariant.getPixels());
    Image newVariant = image.resolveForDrawing(1);
    assertNotSame(oldVariant, newVariant);
    assertFalse(Arrays.equals(oldPixels, newVariant.getPixels()));
  }

  @Test
  void materializedImagesKeepTheirPixelArrayAndEagerBehavior() throws Exception {
    for (Operation operation : Operation.values()) {
      Image image = raster(6, 4);
      int[] pixels = image.getPixels();
      apply(operation, image);
      assertNull(pipeline(image));
      assertSame(pixels, image.getPixels());
    }
  }

  @Test
  void multiFrameColorOperationsMatchHistoricalAllFrameAndVisibleFrameRules() throws Exception {
    byte[] encoded = twoFramePng();

    for (Operation operation : new Operation[] { Operation.APPLY_COLOR, Operation.APPLY_COLOR2,
        Operation.CHANGE_COLORS }) {
      Image expected = new Image(encoded);
      expected.getPixels();
      apply(operation, expected);

      Image actual = new Image(encoded);
      assertEquals(0, actual.getCurrentFrame());
      apply(operation, actual);
      assertEquals(0, actual.getCurrentFrame());
      assertNotNullPipeline(actual);
      assertFrameArraysEqual(expected, actual);
    }

    Image expectedFade = new Image(encoded);
    expectedFade.getPixels();
    expectedFade.applyFade(FADE);
    int[] expectedFadeFrame0 = expectedFade.getPixels().clone();
    expectedFade.setCurrentFrame(1);
    int[] expectedFadeFrame1 = expectedFade.getPixels().clone();

    Image actualFade = new Image(encoded);
    actualFade.applyFade(FADE);
    assertEquals(0, actualFade.getCurrentFrame());
    assertNotNullPipeline(actualFade);
    assertArrayEquals(expectedFadeFrame0, actualFade.getPixels());
    actualFade.setCurrentFrame(1);
    assertArrayEquals(expectedFadeFrame1, actualFade.getPixels());

    Image expectedTransparent = new Image(encoded);
    int[] originalVisible = expectedTransparent.getPixels().clone();
    expectedTransparent.setTransparentColor(TRANSPARENT);
    assertArrayEquals(originalVisible, expectedTransparent.getPixels());
    expectedTransparent.setCurrentFrame(1);
    int[] expectedTransparentFrame1 = expectedTransparent.getPixels().clone();

    Image actualTransparent = new Image(encoded);
    assertSame(actualTransparent, actualTransparent.setTransparentColor(TRANSPARENT));
    assertEquals(0, actualTransparent.getCurrentFrame());
    assertArrayEquals(originalVisible, actualTransparent.getPixels());
    actualTransparent.setCurrentFrame(1);
    assertArrayEquals(expectedTransparentFrame1, actualTransparent.getPixels());
  }

  @Test
  void colorAfterSmoothKeepsJpegTargetedDecodeEligibleButColorBeforeSmoothDoesNot() throws Exception {
    Image.resetTargetedDecodeInvocationCountForTest();
    Image afterSmooth = new Image(jpeg(1024, 768)).getSmoothScaledInstance(64, 48);
    afterSmooth.applyColor(APPLY_COLOR);
    assertEquals(0, Image.targetedDecodeInvocationCountForTest());
    assertEquals(64, afterSmooth.resolveForDrawing(1).getPixelWidth());
    assertEquals(1, Image.targetedDecodeInvocationCountForTest());

    Image.resetTargetedDecodeInvocationCountForTest();
    Image beforeSmooth = new Image(jpeg(1024, 768));
    beforeSmooth.applyColor(APPLY_COLOR);
    Image scaledAfterColor = beforeSmooth.getSmoothScaledInstance(64, 48);
    assertEquals(0, Image.targetedDecodeInvocationCountForTest());
    assertEquals(64, scaledAfterColor.resolveForDrawing(1).getPixelWidth());
    assertEquals(0, Image.targetedDecodeInvocationCountForTest());
  }

  @Test
  void colorNodesAreScaleNeutralAndKeepResolvedContentScale() throws Exception {
    Image image = new Image(png(24, 20)).getSmoothScaledInstance(12, 10);
    image.applyColor(APPLY_COLOR);
    Image resolved = image.resolveForDrawing(2);
    assertEquals(24, resolved.getPixelWidth());
    assertEquals(20, resolved.getPixelHeight());
    assertEquals(2, resolved.getContentScale());
    assertEquals(1, image.getContentScale());
    assertNull(image.pixels);
  }

  private void assertDeferredAndEquivalent(Operation operation) throws Exception {
    byte[] encoded = png(6, 4);
    Image expected = new Image(encoded);
    expected.getPixels();
    apply(operation, expected);

    Image actual = new Image(encoded);
    assertNull(actual.pixels);
    apply(operation, actual);
    assertNotNullPipeline(actual);
    assertNull(actual.pixels);
    assertArrayEquals(expected.getPixels(), actual.getPixels());
  }

  private static void apply(Operation operation, Image image) {
    switch (operation) {
    case APPLY_COLOR:
      image.applyColor(APPLY_COLOR);
      break;
    case APPLY_COLOR2:
      image.applyColor2(APPLY_COLOR2);
      break;
    case APPLY_FADE:
      image.applyFade(FADE);
      break;
    case CHANGE_COLORS:
      image.changeColors(CHANGE_FROM, CHANGE_TO);
      break;
    case SET_TRANSPARENT_COLOR:
      image.setTransparentColor(TRANSPARENT);
      break;
    default:
      throw new AssertionError(operation);
    }
  }

  private static void assertFrameArraysEqual(Image expected, Image actual) {
    assertEquals(expected.getFrameCount(), actual.getFrameCount());
    for (int frame = 0; frame < expected.getFrameCount(); frame++) {
      expected.setCurrentFrame(frame);
      actual.setCurrentFrame(frame);
      assertArrayEquals(expected.getPixels(), actual.getPixels());
    }
  }

  private static List<Integer> operationTypes(ImagePipeline leaf) {
    List<Integer> result = new ArrayList<Integer>();
    for (ImagePipeline node = leaf; node != null && node.previous() != null; node = node.previous()) {
      result.add(0, node.operationType());
    }
    return result;
  }

  private static ImagePipeline pipeline(Image image) throws Exception {
    Field field = Image.class.getDeclaredField("pipeline");
    field.setAccessible(true);
    return (ImagePipeline) field.get(image);
  }

  private static void assertNotNullPipeline(Image image) throws Exception {
    assertTrue(pipeline(image) != null);
  }

  private static Image raster(int width, int height) throws Exception {
    Image image = new Image(width, height);
    for (int i = 0; i < image.pixels.length; i++) {
      image.pixels[i] = 0xFF000000 | ((i * 37) & 0xFF) << 16 | ((i * 19) & 0xFF) << 8 | (i * 11) & 0xFF;
    }
    return image;
  }

  private static byte[] png(int width, int height) throws Exception {
    BufferedImage source = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        source.setRGB(x, y, 0xFF000000 | ((x * 29) & 0xFF) << 16 | ((y * 41) & 0xFF) << 8 | (x + y));
      }
    }
    return writePng(source, null);
  }

  private static byte[] twoFramePng() throws Exception {
    BufferedImage source = new BufferedImage(8, 3, BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < source.getHeight(); y++) {
      for (int x = 0; x < source.getWidth(); x++) {
        int color = x < 4 ? 0xFF000000 | (x * 20) << 16 | (y * 30) << 8 : 0xFF000000 | 0x00004000
            | ((x - 4) * 25) | (y * 15);
        source.setRGB(x, y, color);
      }
    }
    return writePng(source, "FC=2");
  }

  private static byte[] writePng(BufferedImage source, String comment) throws Exception {
    ByteArrayOutputStream encoded = new ByteArrayOutputStream();
    assertTrue(ImageIO.write(source, "png", encoded));
    if (comment == null) {
      return encoded.toByteArray();
    }
    byte[] original = encoded.toByteArray();
    int iend = original.length - 12;
    ByteArrayOutputStream withComment = new ByteArrayOutputStream(original.length + comment.length() + 32);
    withComment.write(original, 0, iend);
    byte[] text = ("Comment\0" + comment).getBytes();
    writeChunk(withComment, "tEXt", text);
    withComment.write(original, iend, 12);
    return withComment.toByteArray();
  }

  private static void writeChunk(ByteArrayOutputStream output, String type, byte[] data) {
    writeInt(output, data.length);
    byte[] typeBytes = type.getBytes();
    output.write(typeBytes, 0, typeBytes.length);
    output.write(data, 0, data.length);
    CRC32 crc = new CRC32();
    crc.update(typeBytes);
    crc.update(data);
    writeInt(output, (int) crc.getValue());
  }

  private static void writeInt(ByteArrayOutputStream output, int value) {
    output.write(value >> 24);
    output.write(value >> 16);
    output.write(value >> 8);
    output.write(value);
  }

  private static byte[] jpeg(int width, int height) throws Exception {
    BufferedImage source = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        source.setRGB(x, y, ((x * 3) & 0xFF) << 16 | ((y * 3) & 0xFF) << 8 | ((x + y) * 2) & 0xFF);
      }
    }
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    assertTrue(ImageIO.write(source, "jpg", output));
    return output.toByteArray();
  }

  private enum Operation {
    APPLY_COLOR, APPLY_COLOR2, APPLY_FADE, CHANGE_COLORS, SET_TRANSPARENT_COLOR
  }
}
