// Copyright (C) 2018-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.io.device.escpos;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Printable ESC/POS QR code representation.
 *
 * @author Fábio Sobral
 * @since TotalCross 4.2.0
 */
public class EscPosQRCode implements EscPosPrintObject {
  public interface Size {
    public static final byte SMALLEST = 1;
    public static final byte SMALLER = 4;
    public static final byte SMALL = 6;
    public static final byte REGULAR = 8;
    public static final byte BIG = 10;
    public static final byte BIGGER = 12;
    public static final byte BIGGEST = 14;
  }

  public interface ErrorCorrectionLevel {
    public static final byte LOW = 1;
    public static final byte MEDIUM = 2;
    public static final byte QUARTILE = 3;
    public static final byte HIGH = 4;
  }

  private byte size = Size.REGULAR;
  private byte eccl = ErrorCorrectionLevel.MEDIUM;
  private byte[] data;

  public EscPosQRCode(byte[] data) {
    this.data = data;
    if (data.length == 0) {
      throw new IllegalArgumentException();
    }
  }

  public EscPosQRCode size(byte size) {
    this.size = size;
    return this;
  }

  public EscPosQRCode errorCorrectionLevel(byte eccl) {
    this.eccl = eccl;
    return this;
  }

  @Override
  public void write(OutputStream out) throws IOException {
    byte[] buf = new byte[(data.length + 10 - 3)];
    int i = 0;
    //    buf[0] = (byte) 27;
    int i2 = i + 1;
    //    buf[i] = (byte) 97;
    //    i = i2 + 1;
    //    buf[i2] = (byte) this.mSettings.barcodeAlign;
    i2 = i + 1;
    buf[i] = (byte) 29;
    i = i2 + 1;
    buf[i2] = (byte) 81;
    i2 = i + 1;
    buf[i] = (byte) 6;
    i = i2 + 1;
    buf[i2] = size;
    i2 = i + 1;
    buf[i] = eccl;
    i = i2 + 1;
    buf[i2] = (byte) data.length;
    i2 = i + 1;
    buf[i] = (byte) (data.length >> 8);
    int i3 = 0;
    while (i3 < data.length) {
      i = i2 + 1;
      buf[i2] = data[i3];
      i3++;
      i2 = i;
    }
    out.write(buf);
  }
}
