// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.zip.CRC32;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

class ImageDeferredTransformsTest {
  @Test
  void allTransformFamiliesRemainDeferredUntilTheCanonicalBarrier() throws Exception {
    Image[] results = {
        new Image(png(5, 4)).getScaledInstance(3, 2),
        new Image(png(5, 4)).getSmoothScaledInstance(3, 2),
        new Image(png(5, 4)).getRotatedScaledInstance(100, 90, 0xFF123456),
        new Image(png(5, 4)).getTouchedUpInstance((byte) 20, (byte) -10),
        new Image(png(5, 4)).getFadedInstance(0xFF204060),
        new Image(png(5, 4)).getAlphaInstance(-40)
    };

    for (Image result : results) {
      assertNotNull(pipeline(result));
      assertNull(result.pixels);
      assertTrue(result.getWidth() > 0);
      assertTrue(result.getHeight() > 0);
      assertNotNull(result.getPixels());
      assertNull(pipeline(result));
    }
  }

  @Test
  void encodedDerivedImagesShareTheSameImmutableRoot() throws Exception {
    Image source = new Image(png(4, 3));
    Image first = source.getScaledInstance(2, 2);
    Image second = source.getAlphaInstance(-20);
    Image chained = first.getTouchedUpInstance((byte) 5, (byte) 0);

    Object root = root(pipeline(first));
    assertSame(root, root(pipeline(second)));
    assertSame(root, root(pipeline(chained)));
    assertTrue(root instanceof EncodedImageSource);
    assertNull(first.pixels);
    assertNull(second.pixels);
    assertNull(chained.pixels);
  }

  @Test
  void rasterRootIsDeepSnapshotAndSubsequentTransformsAppendToIt() throws Exception {
    Image source = new Image(4, 3);
    for (int i = 0; i < source.pixels.length; i++) {
      source.pixels[i] = 0xFF000000 | (i * 0x00010101);
    }
    Image expected = source.getSmoothScaledInstance(2, 2);
    Image derived = source.getSmoothScaledInstance(2, 2).getAlphaInstance(-30);
    Image expectedChained = expected.getAlphaInstance(-30);
    assertTrue(root(pipeline(derived)) instanceof RasterImageSource);

    source.pixels[0] = 0xFFFFFFFF;
    assertArrayEquals(expectedChained.getPixels(), derived.getPixels());
    assertEquals(2, derived.getWidth());
    assertEquals(2, derived.getHeight());
    assertNull(pipeline(derived));
  }

  @Test
  void deferredResolutionPreservesOperationOrderAndRotationFillColor() throws Exception {
    byte[] encoded = png(4, 2);
    Image expected = new Image(encoded).getScaledInstance(3, 3).getAlphaInstance(-60);
    Image actual = new Image(encoded).getScaledInstance(3, 3).getAlphaInstance(-60);

    assertEquals(3, actual.getWidth());
    assertEquals(3, actual.getHeight());
    assertArrayEquals(expected.getPixels(), actual.getPixels());

    Image rotated = new Image(png(5, 4)).getRotatedScaledInstance(100, 30, 0xFF123456);
    int[] pixels = rotated.getPixels();
    assertEquals(4, rotated.getWidth());
    assertEquals(5, rotated.getHeight());
    boolean sawFill = false;
    for (int pixel : pixels) {
      if (pixel == 0xFF123456) {
        sawFill = true;
        break;
      }
    }
    assertTrue(sawFill);
  }

  @Test
  void multiFrameMetadataIsKnownBeforeMaterialization() throws Exception {
    Image image = new Image(pngWithComment(4, 2, "FC=2"));
    Image transformed = image.getScaledInstance(3, 5);

    assertEquals(3, transformed.getWidth());
    assertEquals(5, transformed.getHeight());
    assertEquals(2, transformed.getFrameCount());
    assertEquals(3, transformed.getPixelWidth());
    assertNull(transformed.pixels);
    assertNotNull(transformed.getPixels());
    assertEquals(2, transformed.getFrameCount());
    assertEquals(6, intField(transformed, "widthOfAllFrames"));
  }

  @Test
  void convenienceNoOpsKeepIdentityAndPresentationScale() throws Exception {
    Image source = new Image(png(4, 3));
    assertSame(source, source.scaledBy(1, 1));
    assertSame(source, source.smoothScaledBy(1, 1));
    assertSame(source, source.getSmoothScaledInstance(4, 3));

    source.hwScaleW = 0.5;
    source.hwScaleH = 0.75;
    Image transformed = source.getScaledInstance(2, 2);
    assertEquals(0.5, transformed.hwScaleW);
    assertEquals(0.75, transformed.hwScaleH);
    transformed.getPixels();
    assertEquals(0.5, transformed.hwScaleW);
    assertEquals(0.75, transformed.hwScaleH);
  }

  private static ImagePipeline pipeline(Image image) throws Exception {
    Field field = Image.class.getDeclaredField("pipeline");
    field.setAccessible(true);
    return (ImagePipeline) field.get(image);
  }

  private static Object root(ImagePipeline pipeline) {
    return pipeline.root();
  }

  private static int intField(Image image, String name) throws Exception {
    Field field = Image.class.getDeclaredField(name);
    field.setAccessible(true);
    return field.getInt(image);
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
}
