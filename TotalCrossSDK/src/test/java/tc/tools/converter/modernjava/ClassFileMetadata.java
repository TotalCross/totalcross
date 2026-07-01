// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter.modernjava;

final class ClassFileMetadata {
  final int minorVersion;
  final int majorVersion;

  private ClassFileMetadata(int minorVersion, int majorVersion) {
    this.minorVersion = minorVersion;
    this.majorVersion = majorVersion;
  }

  static ClassFileMetadata read(byte[] bytes) {
    if (bytes.length < 8) {
      throw new IllegalArgumentException("Class file is too short");
    }
    int magic = readInt(bytes, 0);
    if (magic != 0xCAFEBABE) {
      throw new IllegalArgumentException("Class file has invalid magic: 0x" + Integer.toHexString(magic));
    }
    return new ClassFileMetadata(readUnsignedShort(bytes, 4), readUnsignedShort(bytes, 6));
  }

  private static int readInt(byte[] bytes, int offset) {
    return ((bytes[offset] & 0xFF) << 24) | ((bytes[offset + 1] & 0xFF) << 16) | ((bytes[offset + 2] & 0xFF) << 8)
        | (bytes[offset + 3] & 0xFF);
  }

  private static int readUnsignedShort(byte[] bytes, int offset) {
    return ((bytes[offset] & 0xFF) << 8) | (bytes[offset + 1] & 0xFF);
  }
}
