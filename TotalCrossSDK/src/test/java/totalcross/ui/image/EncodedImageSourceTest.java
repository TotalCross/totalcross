// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.zip.CRC32;

import org.junit.jupiter.api.Test;

import totalcross.Launcher;
import totalcross.io.Stream;

class EncodedImageSourceTest {
  @Test
  void copiesCallerBytesAndReportsMetadataWithoutDecoding() throws Exception {
    byte[] encoded = png("FC=3", new byte[] { 0x78, (byte) 0x9c });
    EncodedImageSource source = EncodedImageSource.fromBytes(encoded);
    encoded[0] = 0;

    assertEquals(ImageEncodedStructure.Format.PNG, source.getFormat());
    assertEquals(7, source.getIntrinsicWidth());
    assertEquals(5, source.getIntrinsicHeight());
    assertEquals(3, source.getFrameCount());
    assertEquals(2, source.getLogicalWidth());
    assertEquals(5, source.getLogicalHeight());
    assertEquals("FC=3", source.getComment());
    assertArrayEquals(png("FC=3", new byte[] { 0x78, (byte) 0x9c }), source.copyBytes());
  }

  @Test
  void streamIsConsumedAndNoStreamReferenceIsRetained() throws Exception {
    byte[] encoded = png(null, new byte[] { 1, 2, 3 });
    Stream stream = Stream.asStream(new ByteArrayInputStream(encoded));
    EncodedImageSource source = EncodedImageSource.fromStream(stream);

    assertEquals(encoded.length, source.getEncodedLength());
    assertArrayEquals(encoded, source.copyBytes());
    for (Field field : EncodedImageSource.class.getDeclaredFields()) {
      assertFalse(Stream.class.isAssignableFrom(field.getType()), field.getName());
    }
  }

  @Test
  void pathIsSnapshottedDuringCapture() throws Exception {
    Path file = Files.createTempFile("totalcross-image-source", ".png");
    tc.simulator.Launcher previous = Launcher.instance;
    try {
      byte[] original = png("original", new byte[] { 4, 5 });
      Files.write(file, original);
      new Launcher();
      EncodedImageSource source = EncodedImageSource.fromPath(file.toString());
      Files.write(file, png("changed", new byte[] { 8, 9 }));
      Files.delete(file);
      assertArrayEquals(original, source.copyBytes());
      assertEquals("original", source.getComment());
    } finally {
      Files.deleteIfExists(file);
      Launcher.instance = previous;
    }
  }

  @Test
  void rejectsBadPngCrcAndTruncatedChunks() throws Exception {
    assertThrows(ImageException.class, () -> EncodedImageSource.fromBytes(new byte[0]));
    byte[] valid = png(null, new byte[] { 1 });
    byte[] badCrc = valid.clone();
    badCrc[badCrc.length - 5] ^= 1;
    assertThrows(ImageException.class, () -> EncodedImageSource.fromBytes(badCrc));
    assertThrows(ImageException.class, () -> EncodedImageSource.fromBytes(Arrays.copyOf(valid, valid.length - 3)));
    assertThrows(ImageException.class, () -> EncodedImageSource.fromBytes(Arrays.copyOf(valid, 8)));
  }

  @Test
  void acceptsValidPngWithoutInflatingItsPayload() throws Exception {
    EncodedImageSource source = EncodedImageSource.fromBytes(png(null, new byte[] { 0, 0, 0, 0 }));
    assertEquals(7, source.getIntrinsicWidth());
    assertEquals(5, source.getIntrinsicHeight());
  }

  @Test
  void preservesImageLoaderFrameMetadataRules() throws Exception {
    assertEquals(100, EncodedImageSource.fromBytes(pngWithDimensions("FC=3", 300, 40)).getLogicalWidth());
    assertEquals(100, EncodedImageSource.fromBytes(pngWithDimensions("FC=3", 301, 40)).getLogicalWidth());
    assertThrows(ImageException.class, () -> EncodedImageSource.fromBytes(png("FC=0", new byte[] {1})));
    assertThrows(ImageException.class, () -> EncodedImageSource.fromBytes(png("FC=-2", new byte[] {1})));
    assertEquals(7, EncodedImageSource.fromBytes(png("FC=not-a-number", new byte[] {1})).getLogicalWidth());
  }

  @Test
  void rejectsUnsupportedPngChunkCombinations() throws Exception {
    final byte[] unknownCritical = insertChunkBeforeIend(png(null, new byte[] {1}), "ABCD", new byte[] {1});
    assertThrows(ImageException.class, () -> EncodedImageSource.fromBytes(unknownCritical));

    byte[] indexed = pngIndexedWithoutPalette();
    assertThrows(ImageException.class, () -> EncodedImageSource.fromBytes(indexed));
  }

  @Test
  void rejectsMalformedJpegMarkersButDoesNotDecodeEntropy() throws Exception {
    byte[] jpeg = jpeg(new byte[] { 0x12, (byte) 0xff, 0, 0x34 });
    EncodedImageSource source = EncodedImageSource.fromBytes(jpeg);
    assertEquals(ImageEncodedStructure.Format.JPEG, source.getFormat());
    assertEquals(11, source.getIntrinsicWidth());
    assertEquals(9, source.getIntrinsicHeight());
    assertThrows(ImageException.class, () -> EncodedImageSource.fromBytes(Arrays.copyOf(jpeg, jpeg.length - 1)));
    byte[] malformedLength = jpeg.clone();
    malformedLength[4] = 0x7f;
    assertThrows(ImageException.class, () -> EncodedImageSource.fromBytes(malformedLength));
  }

  @Test
  void validatesGifAndBmpStructure() throws Exception {
    EncodedImageSource gif = EncodedImageSource.fromBytes(gif());
    assertEquals(ImageEncodedStructure.Format.GIF, gif.getFormat());
    assertEquals(1, gif.getFrameCount());
    assertEquals(2, gif.getIntrinsicWidth());
    assertThrows(ImageException.class, () -> EncodedImageSource.fromBytes(Arrays.copyOf(gif(), gif().length - 1)));

    EncodedImageSource bmp = EncodedImageSource.fromBytes(bmp());
    assertEquals(ImageEncodedStructure.Format.BMP, bmp.getFormat());
    assertEquals(1, bmp.getIntrinsicWidth());
    assertThrows(ImageException.class, () -> EncodedImageSource.fromBytes(Arrays.copyOf(bmp(), 54)));
  }

  private static byte[] png(String comment, byte[] idat) throws Exception {
    return pngWithDimensions(comment, 7, 5, idat);
  }

  private static byte[] pngWithDimensions(String comment, int width, int height) throws Exception {
    return pngWithDimensions(comment, width, height, new byte[] {1});
  }

  private static byte[] pngWithDimensions(String comment, int width, int height, byte[] idat) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(new byte[] {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10});
    byte[] ihdr = {(byte) (width >> 24), (byte) (width >> 16), (byte) (width >> 8), (byte) width,
        (byte) (height >> 24), (byte) (height >> 16), (byte) (height >> 8), (byte) height, 8, 6, 0, 0, 0};
    chunk(out, "IHDR", ihdr);
    if (comment != null) chunk(out, "tEXt", ("Comment\0" + comment).getBytes());
    chunk(out, "IDAT", idat);
    chunk(out, "IEND", new byte[0]);
    return out.toByteArray();
  }

  private static byte[] insertChunkBeforeIend(byte[] png, String name, byte[] data) throws Exception {
    int iend = png.length - 12;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(png, 0, iend);
    chunk(out, name, data);
    out.write(png, iend, 12);
    return out.toByteArray();
  }

  private static byte[] pngIndexedWithoutPalette() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(new byte[] {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10});
    chunk(out, "IHDR", new byte[] {0, 0, 0, 1, 0, 0, 0, 1, 8, 3, 0, 0, 0});
    chunk(out, "IDAT", new byte[] {1});
    chunk(out, "IEND", new byte[0]);
    return out.toByteArray();
  }

  private static void chunk(ByteArrayOutputStream out, String name, byte[] data) throws Exception {
    writeInt(out, data.length);
    byte[] type = name.getBytes("ISO-8859-1");
    out.write(type);
    out.write(data);
    CRC32 crc = new CRC32();
    crc.update(type);
    crc.update(data);
    writeInt(out, (int) crc.getValue());
  }

  private static byte[] jpeg(byte[] entropy) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(new byte[] {(byte) 0xff, (byte) 0xd8});
    segment(out, 0xe0, new byte[] {1, 2, 3});
    segment(out, 0xc0, new byte[] {8, 0, 9, 0, 11, 1, 1, 0x11, 0});
    segment(out, 0xda, new byte[] {1, 1, 0, 0, 63, 0});
    out.write(entropy);
    out.write(new byte[] {(byte) 0xff, (byte) 0xd9});
    return out.toByteArray();
  }

  private static void segment(ByteArrayOutputStream out, int marker, byte[] data) throws Exception {
    out.write(new byte[] {(byte) 0xff, (byte) marker, (byte) ((data.length + 2) >> 8), (byte) (data.length + 2)});
    out.write(data);
  }

  private static byte[] gif() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.writeBytes("GIF89a".getBytes());
    out.writeBytes(new byte[] {2, 0, 2, 0, (byte) 0x80, 0, 0, 0, 0, 0, (byte) 0xff, (byte) 0xff, (byte) 0xff});
    out.writeBytes(new byte[] {0x2c, 0, 0, 0, 0, 2, 0, 2, 0, 0, 2, 1, 0, 0, 0x3b});
    return out.toByteArray();
  }

  private static byte[] bmp() {
    byte[] result = new byte[58];
    result[0] = 'B'; result[1] = 'M';
    putLe(result, 2, result.length); putLe(result, 10, 54); putLe(result, 14, 40);
    putLe(result, 18, 1); putLe(result, 22, 1); result[26] = 1; result[28] = 24;
    putLe(result, 34, 4);
    result[54] = 0; result[55] = 0; result[56] = (byte) 0xff; result[57] = 0;
    return result;
  }

  private static void putLe(byte[] bytes, int p, int value) {
    bytes[p] = (byte) value; bytes[p + 1] = (byte) (value >> 8);
    bytes[p + 2] = (byte) (value >> 16); bytes[p + 3] = (byte) (value >> 24);
  }

  private static void writeInt(ByteArrayOutputStream out, int value) {
    out.write(value >>> 24); out.write(value >>> 16); out.write(value >>> 8); out.write(value);
  }
}
