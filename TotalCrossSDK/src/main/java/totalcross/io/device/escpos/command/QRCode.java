package totalcross.io.device.escpos.command;

import java.io.IOException;
import java.io.OutputStream;

public class QRCode implements Command {
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

  public final byte size;
  public final byte eccl;
  public final byte[] data;

  QRCode(byte size, byte eccl, byte[] data) {
    this.size = size;
    this.eccl = eccl;
    this.data = data;
    if (data.length == 0) {
      throw new IllegalArgumentException();
    }
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

  public static class Builder {
    private byte size = Size.REGULAR;
    private byte eccl = ErrorCorrectionLevel.MEDIUM;
    private byte[] data;

    public Builder() {
    }

    public Builder size(byte size) {
      this.size = size;
      return this;
    }

    public Builder errorCorrectionLevel(byte eccl) {
      this.eccl = eccl;
      return this;
    }

    public Builder data(byte[] data) {
      this.data = data;
      return this;
    }

    public QRCode build() {
      return new QRCode(size, eccl, data);
    }
  }
}
