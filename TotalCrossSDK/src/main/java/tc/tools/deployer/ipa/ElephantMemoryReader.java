// Copyright (C) 2012 SuperWaba Ltda.
// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.deployer.ipa;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Stack;

public class ElephantMemoryReader extends ByteArrayInputStream implements ElephantMemoryStream {
  Stack<Integer> positions = new Stack<Integer>();

  public ElephantMemoryReader(byte[] data) {
    super(data);
  }

  public byte[] copy(int from, int to) {
    return Arrays.copyOfRange(super.buf, from, to);
  }

  public long readUnsignedInt() throws IOException {
    byte[] b = new byte[4];
    read(b);
    return ((((long) (b[0] & 0xFF)) << 24) | (((long) (b[1] & 0xFF)) << 16) | ((b[2] & 0xFF) << 8) | (b[3] & 0xFF));
  }

  public long readUnsignedIntLE() throws IOException {
    byte[] b = new byte[4];
    read(b);
    return ((((long) (b[3] & 0xFF)) << 24) | (((long) (b[2] & 0xFF)) << 16) | ((b[1] & 0xFF) << 8) | (b[0] & 0xFF));
  }

  public long readUnsignedLongLE() throws IOException {
    long l1 = (long) readUnsignedIntLE() & 0xFFFFFFFFL;
    long l2 = (long) readUnsignedIntLE() & 0xFFFFFFFFL;
    return (l2 << 32) | l1;
  }

  public String readString(int maxLength) throws IOException {
    byte[] b = new byte[maxLength];
    read(b);
    int actualLength = maxLength;
    for (int i = 0; i < b.length; i++) {
      if (b[i] == 0) {
        actualLength = i;
      }
    }
    return new String(b, 0, actualLength, "US-ASCII");
  }

  public String readString() throws IOException {
    int startPos = pos;
    byte oneByte;
    do {
      oneByte = (byte) this.read();
    } while (oneByte != 0);

    byte[] b = new byte[(int) (pos - startPos)];
    skip(startPos - pos);
    pos = startPos;
    read(b);
    return new String(b, 0, b.length, "US-ASCII");
  }

  @Override
  public void moveTo(int newPosition) {
    reset();
    skip(newPosition);
  }

  public void moveTo(long newPosition) {
    reset();
    skip(newPosition);
  }

  @Override
  public void memorize() {
    positions.push(this.pos);
  }

  @Override
  public void moveBack() {
    this.pos = positions.pop();
  }

  @Override
  public int getPos() {
    return this.pos;
  }
}
