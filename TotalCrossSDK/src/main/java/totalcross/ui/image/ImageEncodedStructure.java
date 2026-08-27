// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

/** Decode-free structural inspection for encoded Image sources. */
final class ImageEncodedStructure {
  enum Format { PNG, JPEG, GIF, BMP }

  static final class Inspection {
    final Format format;
    final int width;
    final int height;
    final int logicalWidth;
    final int logicalHeight;
    final int frameCount;
    final String comment;

    Inspection(Format format, int width, int height, int frameCount, String comment) {
      this.format = format;
      this.width = width;
      this.height = height;
      this.logicalWidth = frameCount > 1 ? width / frameCount : width;
      this.logicalHeight = height;
      this.frameCount = frameCount;
      this.comment = comment;
    }
  }

  private ImageEncodedStructure() {
  }

  static Inspection inspect(byte[] bytes, int length) throws ImageException {
    if (bytes == null || length < 2 || length > bytes.length) {
      throw invalid("truncated image source");
    }
    if (length >= 8 && u(bytes, 0) == 0x89504E47 && u(bytes, 4) == 0x0D0A1A0A) {
      return png(bytes, length);
    }
    if (length >= 6 && ascii(bytes, 0, "GIF87a") || length >= 6 && ascii(bytes, 0, "GIF89a")) {
      return gif(bytes, length);
    }
    if (length >= 2 && bytes[0] == 'B' && bytes[1] == 'M') {
      return bmp(bytes, length);
    }
    if (length >= 2 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8) {
      return jpeg(bytes, length);
    }
    throw invalid("unsupported image format");
  }

  private static Inspection png(byte[] b, int n) throws ImageException {
    int p = 8;
    int width = 0, height = 0;
    int colorType = -1, bitDepth = -1;
    boolean ihdr = false, plte = false, trns = false, idat = false, idatEnded = false, iend = false;
    int paletteEntries = 0;
    String comment = null;
    while (p + 12 <= n) {
      long chunkLength = uint(b, p);
      if (chunkLength > Integer.MAX_VALUE || chunkLength > n - p - 12) {
        throw invalid("truncated PNG chunk");
      }
      int dataLength = (int) chunkLength;
      int type = p + 4;
      int data = p + 8;
      int crc = data + dataLength;
      if (!chunkType(b, type)) {
        throw invalid("invalid PNG chunk type");
      }
      if (!ihdr && !ascii(b, type, "IHDR")) {
        throw invalid("PNG IHDR must be first");
      }
      if (crc32(b, type, dataLength + 4) != uint(b, crc)) {
        throw invalid("invalid PNG chunk CRC");
      }
      if (ascii(b, type, "IHDR")) {
        if (ihdr || dataLength != 13) {
          throw invalid("invalid PNG IHDR");
        }
        width = positiveInt(b, data);
        height = positiveInt(b, data + 4);
        bitDepth = b[data + 8] & 0xFF;
        colorType = b[data + 9] & 0xFF;
        if (!validPngColor(colorType, bitDepth) || (b[data + 10] & 0xFF) != 0
            || (b[data + 11] & 0xFF) != 0 || (b[data + 12] & 0xFF) > 1) {
          throw invalid("unsupported PNG header");
        }
        ihdr = true;
      } else if (ascii(b, type, "PLTE")) {
        if (plte || idat || (colorType != 2 && colorType != 3 && colorType != 6)
            || dataLength == 0 || dataLength % 3 != 0 || dataLength > 768) {
          throw invalid("invalid PNG palette");
        }
        paletteEntries = dataLength / 3;
        if (colorType == 3 && paletteEntries > (1 << bitDepth)) {
          throw invalid("PNG palette exceeds indexed bit depth");
        }
        plte = true;
      } else if (ascii(b, type, "tRNS")) {
        if (trns || idat) {
          throw invalid("invalid PNG transparency ordering");
        }
        if ((colorType == 0 && dataLength != 2) || (colorType == 2 && dataLength != 6)
            || (colorType == 3 && (!plte || dataLength == 0 || dataLength > paletteEntries))
            || (colorType != 0 && colorType != 2 && colorType != 3)) {
          throw invalid("invalid PNG transparency data");
        }
        trns = true;
      } else if (ascii(b, type, "IDAT")) {
        if (!ihdr || idatEnded || (colorType == 3 && !plte)) {
          throw invalid("invalid PNG IDAT ordering");
        }
        idat = true;
      } else if (ascii(b, type, "IEND")) {
        if (dataLength != 0 || !ihdr || !idat || iend) {
          throw invalid("invalid PNG IEND");
        }
        iend = true;
        break;
      } else if ((b[type] & 0x20) == 0) {
        throw invalid("critical PNG chunk after IDAT");
      }
      if (ascii(b, type, "tEXt") && dataLength > 7) {
        int zero = data;
        while (zero < data + dataLength && b[zero] != 0) {
          zero++;
        }
        if (zero < data + dataLength && "Comment".equals(new String(b, data, zero - data, StandardCharsets.ISO_8859_1))) {
          comment = new String(b, zero + 1, data + dataLength - zero - 1, StandardCharsets.ISO_8859_1);
        }
      }
      if (idat && !ascii(b, type, "IDAT")) {
        idatEnded = true;
      }
      p = crc + 4;
    }
    if (!ihdr || !idat || !iend) {
      throw invalid("incomplete PNG structure");
    }
    if (colorType == 3 && !plte) {
      throw invalid("indexed PNG is missing a palette");
    }
    int frames = frameCount(comment);
    return new Inspection(Format.PNG, width, height, frames, comment);
  }

  private static boolean validPngColor(int color, int depth) {
    if (color == 0) return depth == 1 || depth == 2 || depth == 4 || depth == 8 || depth == 16;
    if (color == 2) return depth == 8 || depth == 16;
    if (color == 3) return depth == 1 || depth == 2 || depth == 4 || depth == 8;
    if (color == 4) return false;
    return color == 6 && (depth == 8 || depth == 16);
  }

  private static Inspection jpeg(byte[] b, int n) throws ImageException {
    int p = 2, width = 0, height = 0, scans = 0;
    boolean sof = false, eoi = false;
    while (p < n) {
      if ((b[p++] & 0xFF) != 0xFF) throw invalid("JPEG marker expected");
      while (p < n && (b[p] & 0xFF) == 0xFF) p++;
      if (p >= n) throw invalid("truncated JPEG marker");
      int marker = b[p++] & 0xFF;
      if (marker == 0x00) throw invalid("invalid JPEG marker escape");
      if (marker == 0xD9) { eoi = true; break; }
      if (marker == 0xD8 || (marker >= 0xD0 && marker <= 0xD7)) {
        throw invalid("unexpected JPEG standalone marker");
      }
      if (p + 2 > n) throw invalid("truncated JPEG segment");
      int segmentLength = u16(b, p);
      if (segmentLength < 2 || segmentLength > n - p) throw invalid("invalid JPEG segment length");
      int data = p + 2;
      if (isSof(marker)) {
        if (segmentLength < 8 || sof) throw invalid("invalid JPEG frame header");
        height = u16(b, data + 1);
        width = u16(b, data + 3);
        if (width <= 0 || height <= 0 || (b[data] & 0xFF) == 0) throw invalid("invalid JPEG dimensions");
        sof = true;
      }
      p += segmentLength;
      if (marker == 0xDA) {
        if (!sof || segmentLength < 2) throw invalid("JPEG scan before frame header");
        scans++;
        p = skipJpegScan(b, p, n);
        if (p >= n) throw invalid("missing JPEG EOI");
        if ((b[p] & 0xFF) != 0xFF) throw invalid("invalid JPEG scan termination");
      }
    }
    if (!sof || scans == 0 || !eoi) throw invalid("incomplete JPEG structure");
    return new Inspection(Format.JPEG, width, height, 1, null);
  }

  private static int skipJpegScan(byte[] b, int p, int n) throws ImageException {
    while (p < n) {
      if ((b[p++] & 0xFF) != 0xFF) continue;
      while (p < n && (b[p] & 0xFF) == 0xFF) p++;
      if (p >= n) return n;
      int marker = b[p] & 0xFF;
      if (marker == 0) { p++; continue; }
      if (marker >= 0xD0 && marker <= 0xD7) { p++; continue; }
      return p - 1;
    }
    return n;
  }

  private static boolean isSof(int marker) {
    return marker == 0xC0 || marker == 0xC2;
  }

  private static Inspection gif(byte[] b, int n) throws ImageException {
    if (n < 13) throw invalid("truncated GIF header");
    int width = u16le(b, 6), height = u16le(b, 8);
    if (width <= 0 || height <= 0) throw invalid("invalid GIF dimensions");
    int p = 13;
    int packed = b[10] & 0xFF;
    if ((packed & 0x80) != 0) p = skipTable(p, packed & 7, n);
    int frames = 0;
    while (p < n) {
      int block = b[p++] & 0xFF;
      if (block == 0x3B) {
        if (frames == 0) throw invalid("GIF has no image frames");
        return new Inspection(Format.GIF, width, height, frames, null);
      }
      if (block == 0x21) {
        if (p >= n) throw invalid("truncated GIF extension");
        p++;
        p = skipSubBlocks(p, b, n);
      } else if (block == 0x2C) {
        if (p + 9 > n) throw invalid("truncated GIF image descriptor");
        int frameWidth = u16le(b, p + 4), frameHeight = u16le(b, p + 6);
        if (frameWidth <= 0 || frameHeight <= 0) throw invalid("invalid GIF frame dimensions");
        int flags = b[p + 8] & 0xFF;
        p += 9;
        if ((flags & 0x80) != 0) p = skipTable(p, flags & 7, n);
        if (p >= n) throw invalid("missing GIF LZW code size");
        int codeSize = b[p++] & 0xFF;
        if (codeSize < 2 || codeSize > 8) throw invalid("invalid GIF LZW code size");
        p = skipSubBlocks(p, b, n);
        frames++;
      } else {
        throw invalid("invalid GIF block");
      }
    }
    throw invalid("missing GIF trailer");
  }

  private static int skipTable(int p, int sizeBits, int n) throws ImageException {
    int size = 3 * (1 << (sizeBits + 1));
    if (size > n - p) throw invalid("truncated GIF color table");
    return p + size;
  }

  private static int skipSubBlocks(int p, byte[] b, int n) throws ImageException {
    while (true) {
      if (p >= n) throw invalid("truncated GIF sub-block");
      int size = b[p++] & 0xFF;
      if (size == 0) return p;
      if (size > n - p) throw invalid("truncated GIF sub-block data");
      p += size;
    }
  }

  private static Inspection bmp(byte[] b, int n) throws ImageException {
    if (n < 54) throw invalid("truncated BMP header");
    int offset = uintLe(b, 10), dib = uintLe(b, 14), width = uintLe(b, 18), height = uintLe(b, 22);
    int planes = u16le(b, 26), bpp = u16le(b, 28), compression = uintLe(b, 30);
    int colors = uintLe(b, 46);
    if (dib != 40 || width <= 0 || height <= 0 || width > 65535 || height > 65535 || planes != 1) {
      throw invalid("unsupported BMP header");
    }
    if (!(bpp == 1 || bpp == 2 || bpp == 4 || bpp == 8 || bpp == 16 || bpp == 24 || bpp == 32)) {
      throw invalid("unsupported BMP depth");
    }
    if (compression != 0 && !((compression == 1 && bpp == 8) || (compression == 2 && bpp == 4))) {
      throw invalid("unsupported BMP compression");
    }
    long tableEntries = bpp <= 8 ? (colors == 0 ? 1L << bpp : colors) : 0;
    if (tableEntries > (1L << Math.min(bpp, 8)) || 54L + tableEntries * 4 > n || offset < 54 + tableEntries * 4 || offset > n) {
      throw invalid("invalid BMP palette or pixel offset");
    }
    if (compression == 0) {
      long row = ((long) width * bpp + 31) / 32 * 4;
      if (row * height > n - offset) throw invalid("truncated BMP pixels");
    } else {
      validateRle(b, offset, n, width, height, compression == 1);
    }
    return new Inspection(Format.BMP, width, height, 1, null);
  }

  private static void validateRle(byte[] b, int p, int n, int width, int height, boolean rle8) throws ImageException {
    int x = 0, y = height - 1;
    while (p < n) {
      int count = b[p++] & 0xFF;
      if (p >= n) throw invalid("truncated BMP RLE command");
      int value = b[p++] & 0xFF;
      if (count != 0) {
        if (x + count > width) throw invalid("BMP RLE run exceeds row");
        x += count;
      } else if (value == 0) {
        x = 0;
        if (--y < -1) throw invalid("BMP RLE row exceeds image");
      } else if (value == 1) {
        return;
      } else if (value == 2) {
        if (p + 2 > n) throw invalid("truncated BMP RLE delta");
        x += b[p++] & 0xFF;
        y -= b[p++] & 0xFF;
        if (x > width || y < -1) throw invalid("BMP RLE delta exceeds image");
      } else {
        int bytes = rle8 ? value : (value + 1) / 2;
        if (x + value > width || p + bytes + (bytes & 1) > n) throw invalid("invalid BMP RLE absolute data");
        x += value;
        p += bytes + (bytes & 1);
      }
    }
    throw invalid("missing BMP RLE end marker");
  }

  private static int frameCount(String comment) throws ImageException {
    if (comment != null && comment.startsWith("FC=")) {
      try {
        int value = Integer.parseInt(comment.substring(3));
        if (value < 1) throw invalid("frame count must be positive");
        return value;
      } catch (NumberFormatException ignored) {
      }
    }
    return 1;
  }

  private static int positiveInt(byte[] b, int p) throws ImageException {
    int value = (int) uint(b, p);
    if (value <= 0) throw invalid("invalid image dimensions");
    return value;
  }

  private static long crc32(byte[] b, int p, int length) {
    CRC32 crc = new CRC32();
    crc.update(b, p, length);
    return crc.getValue();
  }

  private static boolean ascii(byte[] b, int p, String value) {
    if (p < 0 || p + value.length() > b.length) return false;
    for (int i = 0; i < value.length(); i++) if (b[p + i] != (byte) value.charAt(i)) return false;
    return true;
  }

  private static boolean chunkType(byte[] b, int p) {
    for (int i = 0; i < 4; i++) {
      int c = b[p + i] & 0xFF;
      if ((c < 'A' || c > 'Z') && (c < 'a' || c > 'z')) return false;
    }
    return true;
  }

  private static int u(byte[] b, int p) {
    return (int) uint(b, p);
  }

  private static long uint(byte[] b, int p) {
    return ((long) (b[p] & 0xFF) << 24) | ((long) (b[p + 1] & 0xFF) << 16)
        | ((long) (b[p + 2] & 0xFF) << 8) | (b[p + 3] & 0xFFL);
  }

  private static int uintLe(byte[] b, int p) {
    return (b[p] & 0xFF) | ((b[p + 1] & 0xFF) << 8) | ((b[p + 2] & 0xFF) << 16)
        | ((b[p + 3] & 0xFF) << 24);
  }

  private static int u16(byte[] b, int p) {
    return ((b[p] & 0xFF) << 8) | (b[p + 1] & 0xFF);
  }

  private static int u16le(byte[] b, int p) {
    return (b[p] & 0xFF) | ((b[p + 1] & 0xFF) << 8);
  }

  private static ImageException invalid(String message) {
    return new ImageException(message);
  }
}
