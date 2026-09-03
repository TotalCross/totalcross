// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.CRC32;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import totalcross.Launcher;
import totalcross.io.ByteArrayStream;

class ImageLazyMaterializationTest {
  @Test
  void encodedConstructionExposesMetadataWithoutAllocatingPixels() throws Exception {
    byte[] encoded = png(3, 2);
    Image image = new Image(encoded);

    assertEquals(3, image.getWidth());
    assertEquals(2, image.getHeight());
    assertEquals(3, image.getPixelWidth());
    assertEquals(2, image.getPixelHeight());
    assertEquals(1, image.getFrameCount());
    assertEquals(-1, image.getCurrentFrame());
    assertEquals(1, image.getContentScale());
    assertNull(pixelStorage(image));
    assertNotNull(pipeline(image));
  }

  @Test
  void materializationAdoptsPixelsIntoTheSameImageAndOwnsCallerBytes() throws Exception {
    byte[] encoded = png(2, 1);
    Image image = new Image(encoded);
    encoded[encoded.length - 1] ^= 1;

    int[] pixels = image.getPixels();

    assertNotNull(pixels);
    assertEquals(2, pixels.length);
    assertNull(pipeline(image));
    assertSame(pixels, image.getPixels());
  }

  @Test
  void structurallyValidPayloadFailureIsCachedWithoutMutatingDeferredState() throws Exception {
    byte[] corrupt = corruptIdat(png(2, 1));
    Image image = new Image(corrupt);
    Object deferredPipeline = pipeline(image);

    IllegalStateException first = assertThrows(IllegalStateException.class, image::getPixels);
    IllegalStateException second = assertThrows(IllegalStateException.class, image::getPixels);

    assertTrue(first.getCause() instanceof ImageException);
    assertSame(first.getCause(), second.getCause());
    assertSame(deferredPipeline, pipeline(image));
    assertNull(pixelStorage(image));
  }

  @Test
  void transientMaterializationFailureIsRetriedWithoutCaching() throws Exception {
    Image image = new Image(png(2, 1));
    Object deferredPipeline = pipeline(image);

    Image.failNextDecodedRasterAllocationForTest();
    IllegalStateException first = assertThrows(IllegalStateException.class, image::getPixels);

    assertTrue(first.getCause() instanceof ImageException);
    assertSame(deferredPipeline, pipeline(image));
    assertNotNull(image.getPixels());
    assertNull(pipeline(image));
  }

  @Test
  void multiFrameBufferFailureIsRetriedWithoutCaching() throws Exception {
    Image image = new Image(pngWithComment(2, 1, "FC=2"));
    Object deferredPipeline = pipeline(image);

    Image.failNextMaterializedFrameBufferAllocationForTest();
    IllegalStateException first = assertThrows(IllegalStateException.class, image::getPixels);

    assertTrue(first.getCause() instanceof TransientImageMaterializationException);
    assertSame(deferredPipeline, pipeline(image));
    assertEquals(2, image.getFrameCount());
    assertEquals(1, image.getWidth());
    assertEquals(1, image.getPixelWidth());

    int[] pixels = image.getPixels();
    assertEquals(1, pixels.length);
    assertEquals(0xFF000000, pixels[0]);
    assertEquals(2, image.getFrameCount());
    assertEquals(1, image.getWidth());
    assertEquals(1, image.getPixelWidth());
    assertEquals(0, image.getCurrentFrame());
    assertNull(pipeline(image));
  }

  @Test
  void jpegExportIsAMaterializationBarrier() throws Exception {
    Image image = new Image(png(2, 1));
    ByteArrayStream output = new ByteArrayStream(256);

    image.createJpg(output, 80);

    assertTrue(output.getPos() > 0);
    assertNull(pipeline(image));
    assertNotNull(image.getPixels());
  }

  @Test
  void jpegScalingFactoriesAlwaysReturnMaterializedImages() throws Exception {
    Path file = Files.createTempFile("totalcross-jpeg-scaling", ".jpg");
    tc.simulator.Launcher previous = (tc.simulator.Launcher) Launcher.instance;
    try {
      Files.write(file, jpeg(4, 2));
      new tc.simulator.Launcher();

      Image bestFit = Image.getJpegBestFit(file.toString(), 4, 2);
      assertMaterialized(bestFit);

      Image scaled = Image.getJpegScaled(file.toString(), 1, 1);
      assertMaterialized(scaled);
    } finally {
      Files.deleteIfExists(file);
      Launcher.instance = previous;
    }
  }

  @Test
  void blankAndLogicalImagesRemainEager() throws Exception {
    Image blank = new Image(2, 3);
    Image logical = Image.createLogical(2, 3, 1.5);

    assertNotNull(blank.getPixels());
    assertNotNull(logical.getPixels());
    assertFalse(pipeline(blank) != null);
    assertFalse(pipeline(logical) != null);
  }

  private static Object pipeline(Image image) throws Exception {
    Field field = Image.class.getDeclaredField("pipeline");
    field.setAccessible(true);
    return field.get(image);
  }

  private static int[] pixelStorage(Image image) throws Exception {
    Field field = Image.class.getDeclaredField("backing");
    field.setAccessible(true);
    return (int[]) field.get(image);
  }

  private static byte[] png(int width, int height) throws Exception {
    BufferedImage source = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        source.setRGB(x, y, 0xFF000000 | (x * 0x00330000) | (y * 0x00003300));
      }
    }
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    assertTrue(ImageIO.write(source, "png", output));
    return output.toByteArray();
  }

  private static byte[] pngWithComment(int width, int height, String comment) throws Exception {
    byte[] source = png(width, height);
    int iend = source.length - 12;
    ByteArrayOutputStream output = new ByteArrayOutputStream(source.length + comment.length() + 32);
    output.write(source, 0, iend);
    byte[] text = ("Comment\0" + comment).getBytes();
    writeChunk(output, "tEXt", text);
    output.write(source, iend, 12);
    return output.toByteArray();
  }

  private static void writeChunk(ByteArrayOutputStream output, String type, byte[] data) throws Exception {
    writeInt(output, data.length);
    byte[] typeBytes = type.getBytes();
    output.write(typeBytes);
    output.write(data);
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
        source.setRGB(x, y, (x * 0x330000) | (y * 0x003300));
      }
    }
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    assertTrue(ImageIO.write(source, "jpg", output));
    return output.toByteArray();
  }

  private static void assertMaterialized(Image image) throws Exception {
    assertNull(pipeline(image));
    assertNotNull(image.getPixels());
  }

  private static byte[] corruptIdat(byte[] source) {
    byte[] result = source.clone();
    int position = 8;
    while (position + 12 <= result.length) {
      int length = readInt(result, position);
      String type = new String(result, position + 4, 4);
      if ("IDAT".equals(type) && length > 0) {
        result[position + 8] ^= 1;
        CRC32 crc = new CRC32();
        crc.update(result, position + 4, length + 4);
        writeInt(result, position + 8 + length, (int) crc.getValue());
        return result;
      }
      position += length + 12;
    }
    throw new AssertionError("PNG has no IDAT chunk");
  }

  private static int readInt(byte[] bytes, int position) {
    return ((bytes[position] & 0xFF) << 24) | ((bytes[position + 1] & 0xFF) << 16)
        | ((bytes[position + 2] & 0xFF) << 8) | (bytes[position + 3] & 0xFF);
  }

  private static void writeInt(byte[] bytes, int position, int value) {
    bytes[position] = (byte) (value >> 24);
    bytes[position + 1] = (byte) (value >> 16);
    bytes[position + 2] = (byte) (value >> 8);
    bytes[position + 3] = (byte) value;
  }
}
