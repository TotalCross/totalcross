package totalcross.io.device.escpos.command;

import java.io.IOException;
import java.io.OutputStream;

public class Barcode implements Command {
  
  public enum BarcodeType {
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

    BarcodeType(int type, int length) {
      this.type = type;
      this.lengthMin = this.lengthMax = length;
    }

    BarcodeType(int type, int lengthMin, int lengthMax) {
      this.type = type;
      this.lengthMin = lengthMin;
      this.lengthMax = lengthMax;
    }
  }
  
  public interface TextPrintingPosition {
    public static final byte NONE = 0;
    public static final byte ABOVE = 1;
    public static final byte BELOW = 2;
    public static final byte BOTH = 3;
  }
  
  private final BarcodeType type;
  private final String data;
  private int scaleFactor = -1;
  private byte textPrintingPosition = TextPrintingPosition.BELOW;
  
  public Barcode(BarcodeType type, CharSequence data) {
    this.type = type;
    this.data = data.toString();
  }
  
  public Barcode textPrintingPosition(byte textPrintingPosition) {
    this.textPrintingPosition = textPrintingPosition;
    return this;
  }
  
//  public Barcode scaleFactor(int scaleFactor) {
//   this.scaleFactor = scaleFactor;
//   return this;
//  }
  
  @Override
  public void write(OutputStream out) throws IOException {
    byte[] b = data.getBytes();
    
    if (b.length < this.type.lengthMin || b.length > this.type.lengthMax) {
      // handle better later
      throw new IllegalArgumentException();
    }
//    out.write(29);
//    out.write(107);
//    out.write(this.type);
    
    out.write(EscPosCommands.GS_HRI);
    out.write(textPrintingPosition);
    
    byte[] buf = new byte[(b.length + 21 - 15)];
    int i = 0;
//    buf[0] = (byte) 27;
    int i2 = i + 1;
//    buf[i] = (byte) 97;
//    i = i2 + 1;
//    buf[i2] = (byte) this.mSettings.barcodeAlign;
//    i2 = i + 1;
//    buf[i] = (byte) 29;
//    i = i2 + 1;
//    buf[i2] = (byte) 119;
//    i2 = i + 1;
//    buf[i] = (byte) this.mSettings.barcodeScale;
//    i = i2 + 1;
//    buf[i2] = (byte) 29;
//    i2 = i + 1;
//    buf[i] = (byte) 104;
//    i = i2 + 1;
//    buf[i2] = (byte) this.mSettings.barcodeHeight;
//    i2 = i + 1;
//    buf[i] = (byte) 29;
//    i = i2 + 1;
//    buf[i2] = (byte) 72;
//    i2 = i + 1;
//    buf[i] = (byte) this.mSettings.barcodeHriCode;
//    i = i2 + 1;
//    buf[i2] = (byte) 29;
//    i2 = i + 1;
//    buf[i] = (byte) 102;
//    i = i2 + 1;
//    buf[i2] = (byte) this.mSettings.barcodeHriFont;
    
//    switch (type) {
//        case UPCA /*65*/:
//            if (data.length != 11) {
//                throw new IllegalArgumentException("The length of UPCA barcode data must be 11 symbols");
//            }
//            break;
//        case UPCE /*66*/:
//            if (data.length != 11) {
//                throw new IllegalArgumentException("The length of UPCE barcode data must be 11 symbols");
//            }
//            break;
//        case EAN13 /*67*/:
//            if (data.length != 12) {
//                throw new IllegalArgumentException("The length of EAN13 barcode data must be 12 symbols");
//            }
//            break;
//        case EAN8 /*68*/:
//            if (data.length != 7) {
//                throw new IllegalArgumentException("The length of EAN8 barcode data must be 7 symbols");
//            }
//            break;
//        case CODE39 /*69*/:
//        case ITF /*70*/:
//        case CODABAR /*71*/:
//        case CODE93 /*72*/:
//        case CODE128 /*73*/:
//        case CODE128AUTO /*75*/:
//        case EAN128 /*76*/:
//            if (data.length < 1 || data.length > 255) {
//                throw new IllegalArgumentException("The length of barcode data must be between 1 and 255 symbols");
//            }
//        case PDF417 /*74*/:
//            if (data.length < 1 || data.length > 1000) {
//                throw new IllegalArgumentException("The length of PDF417 barcode data must be between 1 and 1000 symbols");
//            }
//        default:
//            throw new IllegalArgumentException("Invalid barcode type");
//    }
    i2 = i + 1;
    //GS k
    buf[i] = (byte) 29;
    i = i2 + 1;
    buf[i2] = (byte) 107;
    i2 = i + 1;
    buf[i] = (byte) type.type;
    if (type.type == 73 && b[0] != (byte) 123) {
        i = i2 + 1;
        buf[i2] = (byte) (b.length + 2);
        i2 = i + 1;
        buf[i] = (byte) 123;
        i = i2 + 1;
        buf[i2] = (byte) 66;
        i2 = i;
    } else if (type.type == 74) {
        i = i2 + 1;
        buf[i2] = (byte) 0;
        i2 = i + 1;
        buf[i] = (byte) (b.length & 255);
        i = i2 + 1;
        buf[i2] = (byte) ((b.length >> 8) & 255);
        i2 = i;
    } else {
        i = i2 + 1;
        buf[i2] = (byte) b.length;
        i2 = i;
    }
    int i3 = 0;
    while (i3 < b.length) {
        i = i2 + 1;
        buf[i2] = b[i3];
        i3++;
        i2 = i;
    }
      out.write(buf, 0, i2);
}
}
