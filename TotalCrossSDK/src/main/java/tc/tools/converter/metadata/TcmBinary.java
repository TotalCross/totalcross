// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.metadata;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

final class TcmBinary {
  private TcmBinary() {
  }

  static final class Output {
    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();

    void u8(int value) {
      bytes.write(value & 0xFF);
    }

    void u16(int value) {
      u8(value);
      u8(value >>> 8);
    }

    void i32(int value) {
      u8(value);
      u8(value >>> 8);
      u8(value >>> 16);
      u8(value >>> 24);
    }

    void i64(long value) {
      i32((int) value);
      i32((int) (value >>> 32));
    }

    void bytes(byte[] value) {
      bytes.write(value, 0, value.length);
    }

    void utf8(String value) {
      byte[] encoded = value.getBytes(StandardCharsets.UTF_8);
      i32(encoded.length);
      bytes(encoded);
    }

    byte[] toByteArray() {
      return bytes.toByteArray();
    }
  }

  static final class Input {
    private final byte[] bytes;
    private final int end;
    private int position;

    Input(byte[] bytes) {
      this(bytes, 0, bytes.length);
    }

    Input(byte[] bytes, int offset, int length) {
      if (offset < 0 || length < 0 || offset > bytes.length - length) {
        throw new IllegalArgumentException("Invalid TCM input range");
      }
      this.bytes = bytes;
      this.position = offset;
      this.end = offset + length;
    }

    int u8() {
      require(1);
      return bytes[position++] & 0xFF;
    }

    int u16() {
      return u8() | (u8() << 8);
    }

    int i32() {
      return u8() | (u8() << 8) | (u8() << 16) | (u8() << 24);
    }

    long i64() {
      return (i32() & 0xFFFFFFFFL) | ((long) i32() << 32);
    }

    byte[] bytes(int length) {
      require(length);
      byte[] value = new byte[length];
      System.arraycopy(bytes, position, value, 0, length);
      position += length;
      return value;
    }

    String utf8() {
      int length = count("UTF-8 byte length");
      return new String(bytes(length), StandardCharsets.UTF_8);
    }

    int count(String label) {
      int value = i32();
      if (value < 0) {
        throw error("negative " + label + " " + value);
      }
      return value;
    }

    Input section(int length) {
      require(length);
      Input section = new Input(bytes, position, length);
      position += length;
      return section;
    }

    boolean hasRemaining() {
      return position < end;
    }

    int remaining() {
      return end - position;
    }

    void requireFullyRead(String label) {
      if (hasRemaining()) {
        throw error(label + " has " + remaining() + " trailing bytes");
      }
    }

    private void require(int length) {
      if (length < 0 || length > end - position) {
        throw error("truncated input: need " + length + " bytes, have " + (end - position));
      }
    }

    IllegalArgumentException error(String detail) {
      return new IllegalArgumentException("Invalid TCM at byte " + position + ": " + detail);
    }
  }
}
