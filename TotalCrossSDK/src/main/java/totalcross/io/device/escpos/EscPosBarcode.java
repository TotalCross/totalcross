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
 * Printable ESC/POS bar code representation.
 *
 * @author Fábio Sobral
 * @since TotalCross 4.2.0
 */
public class EscPosBarcode implements EscPosPrintObject {

  public static final byte TEXTPRINTINGPOSITION_NONE = 0;
  public static final byte TEXTPRINTINGPOSITION_ABOVE = 1;
  public static final byte TEXTPRINTINGPOSITION_BELOW = 2;
  public static final byte TEXTPRINTINGPOSITION_BOTH = 3;

  public enum EscPosBarcodeType {
    UPCA(65, 11),
    UPCE(66, 11),
    EAN13(67, 12),
    EAN8(68, 7),
    CODE39(69, 1, 255),
    ITF(70, 1, 255),
    CODABAR(71, 1, 255),
    CODE93(72, 1, 255),
    CODE128(73, 1, 255),
    CODE128AUTO(75, 1, 255),
    EAN128(76, 1, 255),
    PDF417(74, 1, 1000);

    public final int type;
    public final int lengthMin;
    public final int lengthMax;

    private EscPosBarcodeType(int type, int length) {
      this.type = type;
      this.lengthMin = this.lengthMax = length;
    }

    private EscPosBarcodeType(int type, int lengthMin, int lengthMax) {
      this.type = type;
      this.lengthMin = lengthMin;
      this.lengthMax = lengthMax;
    }
  }

  private final EscPosBarcodeType type;
  private final byte[] data;
  private int height = -1;
  private int width = -1;
  private byte textPrintingPosition = -1;

  public EscPosBarcode(final EscPosBarcodeType type, final CharSequence data) {
    this.type = type;
    this.data = data.toString().getBytes();

    if (type.lengthMin == type.lengthMax && this.data.length != type.lengthMin) {
      throw new IllegalArgumentException(
          "The length of "
              + type.toString()
              + " barcode data must be "
              + type.lengthMin
              + " symbols");
    } else if (this.data.length < type.lengthMin || this.data.length > type.lengthMax) {
      throw new IllegalArgumentException(
          "The length of barcode data must be between "
              + type.lengthMin
              + " and "
              + type.lengthMax
              + " symbols");
    }
  }

  /**
   * Selects the print position of Human Readable Interpretation (HRI) characters when printing a
   * bar code.
   *
   * @param textPrintingPosition
   * @return
   */
  public EscPosBarcode textPrintingPosition(byte textPrintingPosition) {
    if (textPrintingPosition < TEXTPRINTINGPOSITION_NONE
        || textPrintingPosition > TEXTPRINTINGPOSITION_BOTH) {
      throw new IllegalArgumentException();
    }
    this.textPrintingPosition = textPrintingPosition;
    return this;
  }

  /**
   * Sets the bar code height to n dots.
   *
   * <p>The original ESC/POS Manual defines the bar code height in dots, but the actual size of a
   * dot and the default value for n and may differ for each device.
   *
   * <p>For instance, the default bar code height for devices with dot size of 0.125mm (1/203
   * inches) is 162.
   *
   * <p>Known default values:
   * <li>Datecs DPP-350: 162
   * <li>Leopardo A7: 36
   *
   * @param n
   * @return
   */
  public EscPosBarcode height(int n) {
    if (n < 1 || n > 255) {
      throw new IllegalArgumentException();
    }
    this.height = n;
    return this;
  }

  /**
   * Sets the bar code width.
   *
   * <p>The units for n depends on the printer model - usually 2 ≤ n ≤ 6 with default value 3.
   *
   * <p>Known models range and default values:
   * <li>Datecs DPP-350: 2 ≤ n ≤ 4 with default value of 3
   * <li>Leopardo A7: 1 ≤ n ≤ 4 with default value of 2
   *
   * @param n
   * @return
   */
  public EscPosBarcode width(int n) {
    if (n < 0) {
      throw new IllegalArgumentException();
    }
    this.width = n;
    return this;
  }

  @Override
  public void write(OutputStream out) throws IOException {
    if (height > 0) {
      out.write(EscPosCommands.GS_BARCODE_HEIGHT);
      out.write((byte) height);
    }
    if (width >= 0) {
      out.write(EscPosCommands.GS_BARCODE_WIDTH);
      out.write((byte) width);
    }
    if (textPrintingPosition >= 0) {
      out.write(EscPosCommands.GS_HRI);
      out.write(textPrintingPosition);
    }

    byte[] buf = new byte[(data.length + 6)];
    int i = 0;
    int i2 = i + 1;
    //GS k
    buf[i] = (byte) 29;
    i = i2 + 1;
    buf[i2] = (byte) 107;
    i2 = i + 1;
    buf[i] = (byte) type.type;
    if (type.type == 73 && data[0] != (byte) 123) {
      i = i2 + 1;
      buf[i2] = (byte) (data.length + 2);
      i2 = i + 1;
      buf[i] = (byte) 123;
      i = i2 + 1;
      buf[i2] = (byte) 66;
      i2 = i;
    } else if (type.type == 74) {
      i = i2 + 1;
      buf[i2] = (byte) 0;
      i2 = i + 1;
      buf[i] = (byte) (data.length & 255);
      i = i2 + 1;
      buf[i2] = (byte) ((data.length >> 8) & 255);
      i2 = i;
    } else {
      i = i2 + 1;
      buf[i2] = (byte) data.length;
      i2 = i;
    }
    int i3 = 0;
    while (i3 < data.length) {
      i = i2 + 1;
      buf[i2] = data[i3];
      i3++;
      i2 = i;
    }
    out.write(buf, 0, i2);
  }
}
