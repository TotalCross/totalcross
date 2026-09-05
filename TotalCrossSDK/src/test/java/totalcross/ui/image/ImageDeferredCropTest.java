// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

class ImageDeferredCropTest {
  @Test
  void encodedCropIsLazyAndMatchesMaterializedCopyImageRect() throws Exception {
    byte[] encoded = png(7, 5);
    Image expectedSource = new Image(encoded);
    Image expected = eagerCrop(expectedSource, 1, 2, 4, 2);
    Image actual = new Image(encoded).getClippedInstance(1, 2, 4, 2);

    assertNull(actual.backing);
    assertEquals(Arrays.asList(ImagePipeline.CROP), operationTypes(pipeline(actual)));
    assertEquals(1, actual.getFrameCount());
    assertEquals(4, actual.getWidth());
    assertEquals(2, actual.getHeight());
    assertArrayEquals(expected.getPixels(), actual.getPixels());
  }

  @Test
  void multiFrameCropCapturesCurrentFrameAndReturnsOneFrame() throws Exception {
    byte[] encoded = twoFramePng();
    Image source = new Image(encoded);
    source.setCurrentFrame(1);
    Image expected = eagerCrop(source, 0, 0, 3, 2);
    Image actual = source.getClippedInstance(0, 0, 3, 2);

    source.setCurrentFrame(0);
    assertEquals(1, actual.getFrameCount());
    assertEquals(Arrays.asList(ImagePipeline.FRAME_SELECT, ImagePipeline.CROP),
        operationTypes(pipeline(actual)));
    assertArrayEquals(expected.getPixels(), actual.getPixels());
  }

  @Test
  void cropUsesNewImagePresentationDefaultsAndSnapshotsMaterializedPixels() throws Exception {
    Image source = materialized(6, 4);
    setField(source, "path", "source.png");
    source.hwScaleW = 0.5;
    source.hwScaleH = 0.75;
    source.alphaMask = 83;
    source.transparentColor = 0x123456;
    source.useAlpha = true;

    Image actual = source.getClippedInstance(1, 1, 3, 2);
    int[] expected = eagerCrop(source, 1, 1, 3, 2).getPixels().clone();
    source.getPixels()[0] = 0xFFFFFFFF;

    assertArrayEquals(expected, actual.getPixels());
    assertNull(actual.getPath());
    assertEquals(1, actual.hwScaleW);
    assertEquals(1, actual.hwScaleH);
    assertEquals(255, actual.alphaMask);
    assertEquals(0xFFFFFF, actual.transparentColor);
    assertEquals(false, actual.useAlpha);
    assertEquals(3, actual.getWidth());
    assertEquals(2, actual.getHeight());
  }

  @Test
  void invalidDimensionsFailImmediatelyAndCoordinatesKeepExistingClipping() throws Exception {
    Image source = new Image(png(5, 4));
    assertThrows(ImageException.class, () -> source.getClippedInstance(0, 0, 0, 2));
    assertThrows(ImageException.class, () -> source.getClippedInstance(0, 0, 2, -1));

    Image expected = eagerCrop(source, -1, 1, 4, 3);
    Image actual = source.getClippedInstance(-1, 1, 4, 3);
    assertArrayEquals(expected.getPixels(), actual.getPixels());
  }

  @Test
  void cropAloneKeepsNaturalScaleWhileScaleNodesRemainDestinationAware() throws Exception {
    Image logical = Image.createLogical(3, 2, 2);
    fillLogicalPixels(logical);

    Image naturalCrop = logical.getClippedInstance(1, 0, 2, 1);
    Image naturalResolved = naturalCrop.resolveForDrawing(3);
    assertEquals(2, naturalResolved.getContentScale());
    assertEquals(4, naturalResolved.getPixelWidth());
    assertEquals(2, naturalResolved.getPixelHeight());

    Image scaledCrop = new Image(png(8, 6)).getSmoothScaledInstance(4, 3).getClippedInstance(1, 1, 2, 1);
    Image scaledResolved = scaledCrop.resolveForDrawing(2);
    assertEquals(2, scaledResolved.getContentScale());
    assertEquals(4, scaledResolved.getPixelWidth());
    assertEquals(2, scaledResolved.getPixelHeight());
    assertEquals(2, scaledResolved.getWidth());
    assertEquals(1, scaledResolved.getHeight());
    assertTrue(scaledResolved.getPixels().length > 0);
  }

  @Test
  void fractionalContentScaleCropCopiesTheCompletePhysicalRectangle() throws Exception {
    assertFractionalCrop(1.5);
    assertFractionalCrop(2.5);
  }

  @Test
  void highDensityCropKeepsLegacyPresentationBehavior() throws Exception {
    Image source = Image.createLogical(3, 2, 2);
    fillLogicalPixels(source);
    source.alphaMask = 127;
    source.hwScaleW = 0.5;
    source.hwScaleH = 0.75;

    Image expected = eagerCrop(source, 1, 0, 2, 1, 2);
    Image actual = source.getClippedInstance(1, 0, 2, 1).resolveForDrawing(1);

    assertArrayEquals(expected.getPixels(), actual.getPixels());
  }

  private static void assertFractionalCrop(double contentScale) throws Exception {
    Image source = Image.createLogical(3, 2, contentScale);
    fillPhysicalPattern(source);
    int sourceX = (int) Math.round(contentScale);
    int expectedWidth = (int) Math.ceil(contentScale);
    int expectedHeight = (int) Math.ceil(contentScale);
    int[] expected = new int[expectedWidth * expectedHeight];
    for (int row = 0; row < expectedHeight; row++) {
      System.arraycopy(source.getPixels(), row * source.getPixelWidth() + sourceX, expected,
          row * expectedWidth, expectedWidth);
    }

    Image actual = source.getClippedInstance(1, 0, 1, 1).resolveForDrawing(1);
    assertEquals(expectedWidth, actual.getPixelWidth());
    assertEquals(expectedHeight, actual.getPixelHeight());
    assertArrayEquals(expected, actual.getPixels());
  }

  private static void fillPhysicalPattern(Image image) {
    int[] pixels = image.getPixels();
    int width = image.getPixelWidth();
    for (int y = 0; y < image.getPixelHeight(); y++) {
      for (int x = 0; x < width; x++) {
        pixels[y * width + x] = 0xFF000000 | ((x + 1) << 16) | ((y + 1) << 8) | (x + y + 1);
      }
    }
  }

  @Test
  void jpegEligibilityFollowsConservativePipelineGeometry() throws Exception {
    byte[] encoded = jpeg(1024, 768);

    Image.resetTargetedDecodeInvocationCountForTest();
    Image smoothThenCrop = new Image(encoded).getSmoothScaledInstance(64, 48).getClippedInstance(0, 0, 32, 24);
    assertEquals(0, Image.targetedDecodeInvocationCountForTest());
    smoothThenCrop.resolveForDrawing(1);
    assertEquals(0, Image.targetedDecodeInvocationCountForTest());

    Image.resetTargetedDecodeInvocationCountForTest();
    Image cropThenSmooth = new Image(encoded).getClippedInstance(0, 0, 128, 96).getSmoothScaledInstance(64, 48);
    cropThenSmooth.resolveForDrawing(1);
    assertEquals(1, Image.targetedDecodeInvocationCountForTest());
  }

  private static Image eagerCrop(Image source, int x, int y, int w, int h) throws Exception {
    return eagerCrop(source, x, y, w, h, 1);
  }

  private static Image eagerCrop(Image source, int x, int y, int w, int h, double contentScale) throws Exception {
    source.getPixels();
    Image result = Image.createLogical(w, h, contentScale);
    result.getGraphics().copyImageRect(source, x, y, w, h, true);
    return result;
  }

  private static Image materialized(int width, int height) throws Exception {
    Image image = new Image(width, height);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        image.getPixels()[y * width + x] = 0xFF000000 | (x * 30) << 16 | (y * 40) << 8 | x + y;
      }
    }
    return image;
  }

  private static void fillLogicalPixels(Image image) {
    for (int i = 0; i < image.getPixels().length; i++) {
      image.getPixels()[i] = 0xFF000000 | (i * 13) << 8 | i;
    }
  }

  private static ImagePipeline pipeline(Image image) throws Exception {
    Field field = Image.class.getDeclaredField("pipeline");
    field.setAccessible(true);
    return (ImagePipeline) field.get(image);
  }

  private static java.util.List<Integer> operationTypes(ImagePipeline leaf) {
    java.util.List<Integer> result = new java.util.ArrayList<Integer>();
    for (ImagePipeline node = leaf; node != null && node.previous() != null; node = node.previous()) {
      result.add(0, node.operationType());
    }
    return result;
  }

  private static void setField(Object object, String name, Object value) throws Exception {
    Field field = object.getClass().getDeclaredField(name);
    field.setAccessible(true);
    field.set(object, value);
  }

  private static byte[] png(int width, int height) throws Exception {
    BufferedImage source = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        source.setRGB(x, y, 0xFF000000 | ((x * 29) & 0xFF) << 16 | ((y * 41) & 0xFF) << 8 | x + y);
      }
    }
    return write(source, "png");
  }

  private static byte[] twoFramePng() throws Exception {
    BufferedImage source = new BufferedImage(8, 3, BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < source.getHeight(); y++) {
      for (int x = 0; x < source.getWidth(); x++) {
        source.setRGB(x, y, x < 4 ? 0xFF102030 : 0xFF406070);
      }
    }
    byte[] bytes = write(source, "png");
    int iend = bytes.length - 12;
    ByteArrayOutputStream output = new ByteArrayOutputStream(bytes.length + 32);
    output.write(bytes, 0, iend);
    byte[] text = "Comment\0FC=2".getBytes("ISO-8859-1");
    byte[] type = "tEXt".getBytes("ISO-8859-1");
    writeInt(output, text.length);
    output.write(type);
    output.write(text);
    java.util.zip.CRC32 crc = new java.util.zip.CRC32();
    crc.update(type);
    crc.update(text);
    writeInt(output, (int) crc.getValue());
    output.write(bytes, iend, 12);
    return output.toByteArray();
  }

  private static byte[] jpeg(int width, int height) throws Exception {
    BufferedImage source = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        source.setRGB(x, y, (x * 0x330000) | (y * 0x003300));
      }
    }
    return write(source, "jpg");
  }

  private static byte[] write(BufferedImage source, String format) throws Exception {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    assertTrue(ImageIO.write(source, format, output));
    return output.toByteArray();
  }

  private static void writeInt(ByteArrayOutputStream output, int value) {
    output.write(value >> 24);
    output.write(value >> 16);
    output.write(value >> 8);
    output.write(value);
  }
}
