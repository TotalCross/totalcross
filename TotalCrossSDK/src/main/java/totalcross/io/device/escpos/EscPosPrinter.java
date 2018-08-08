package totalcross.io.device.escpos;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;

import totalcross.io.device.escpos.command.Barcode;
import totalcross.io.device.escpos.command.Command;
import totalcross.io.device.escpos.command.Cut;
import totalcross.io.device.escpos.command.DrawerKick;
import totalcross.io.device.escpos.command.EscPosCommands;
import totalcross.io.device.escpos.command.FeedRate;
import totalcross.io.device.escpos.command.Justification;
import totalcross.io.device.escpos.command.PrintImage;
import totalcross.io.device.escpos.command.QRCode;
import totalcross.io.device.escpos.command.Raw;
import totalcross.ui.image.ImageException;

public class EscPosPrinter {
  
  public static interface PaperSize {
    public static final int A7 = 384;
    public static final int A8 = 576;
  }

  private final OutputStream out;

  public EscPosPrinter(OutputStream out) {
    this.out = out;
  }

  public EscPosPrinter initialize() throws IOException {
    out.write(EscPosCommands.ESC_INIT);
    return this;
  }

  public EscPosPrinter feed() throws IOException {
    out.write(EscPosCommands.FF);
    return this;
  }

  public EscPosPrinter ff() throws IOException {
    out.write(EscPosCommands.FF);
    return this;
  }

  public EscPosPrinter lineFeed() throws IOException {
    out.write(EscPosCommands.LF);
    return this;
  }

  public EscPosPrinter lf() throws IOException {
    out.write(EscPosCommands.LF);
    return this;
  }

  public EscPosPrinter carriageReturn() throws IOException {
    out.write(EscPosCommands.CR);
    return this;
  }

  public EscPosPrinter cr() throws IOException {
    out.write(EscPosCommands.CR);
    return this;
  }

  public EscPosPrinter horizontalTab() throws IOException {
    out.write(EscPosCommands.HT);
    return this;
  }

  public EscPosPrinter ht() throws IOException {
    out.write(EscPosCommands.HT);
    return this;
  }

  public EscPosPrinter buzzer() throws IOException {
    out.write(EscPosCommands.BEL);
    return this;
  }

  public EscPosPrinter bel() throws IOException {
    out.write(EscPosCommands.BEL);
    return this;
  }

  public EscPosPrinter feedPaper(int lines) throws IOException {
    if (lines < 0 || lines > 255) {
      throw new IllegalArgumentException("The lines is out of range");
    }
    out.write(EscPosCommands.ESC_FEED_PAPER);
    out.write((byte) lines);
    return this;
  }

  public EscPosPrinter feedLines(int lines) throws IOException {
    if (lines < 0 || lines > 255) {
      throw new IllegalArgumentException("The lines is out of range");
    }
    out.write(EscPosCommands.ESC_FEED_LINES);
    out.write((byte) lines);
    return this;
  }

  public EscPosPrinter textPrintMode(int textPrintMode) throws IOException {
    out.write(EscPosCommands.ESC_PRINT_MODE);
    out.write((byte) textPrintMode);
    return this;
  }
  
  public EscPosPrinter textSize(byte characterSize) throws IOException {
    out.write(EscPosCommands.GS_CHARACTER_SIZE);
    out.write(characterSize);
    return this;
  }
  
  public EscPosPrinter underline(boolean enable) throws IOException {
    out.write(EscPosCommands.ESC_UNDERLINE);
    out.write((byte) (enable ? 1 : 0));
    return this;
  }
  
  public EscPosPrinter emphasize(boolean enable) throws IOException {
    out.write(EscPosCommands.ESC_EMPHASIZE);
    out.write((byte) (enable ? 1 : 0));
    return this;
  }
  
  public EscPosPrinter doubleStrike(boolean enable) throws IOException {
    out.write(EscPosCommands.ESC_DOUBLESTRIKE);
    out.write((byte) (enable ? 1 : 0));
    return this;
  }
  
  public EscPosPrinter reverseWhiteBlack(boolean enable) throws IOException {
    out.write(EscPosCommands.GS_REVERSE_BW);
    out.write((byte) (enable ? 1 : 0));
    return this;
  }
  
  /**
   * Turn 90° clockwise rotation mode on/off.
   * 
   * @param enable
   * @return
   * @throws IOException
   */
  public EscPosPrinter rotate(boolean enable) throws IOException {
    out.write(EscPosCommands.ESC_ROTATE);
    out.write((byte) (enable ? 1 : 0));
    return this;
  }
  
  /**
   * Sets 90° clockwise rotation with given value. Provided for printers that
   * define other values for rotation besides on/off, refer to your printer
   * manual for more information.
   * 
   * @param value
   * @return
   * @throws IOException
   */
  public EscPosPrinter rotate(byte value) throws IOException {
    out.write(EscPosCommands.ESC_ROTATE);
    out.write(value);
    return this;
  }
  
  public EscPosPrinter absolutePrintPosition(int value) throws IOException {
    out.write(EscPosCommands.ESC_PRINT_POSITION);
    out.write((byte) (value & 0xFF));
    out.write((byte) ((value >> 8) & 0xFF));
    return this;
  }
  
  public EscPosPrinter horizontalTabPosition(byte... tabPositions) throws IOException {
    if (tabPositions.length > 32) {
      throw new IllegalArgumentException();
    }
    out.write(EscPosCommands.ESC_HORIZONTAL_TAB_POSITION);
    out.write(tabPositions);
    out.write(EscPosCommands.NULL);
    return this;
  }
  
  public EscPosPrinter resetLineSpacing() throws IOException {
    out.write(EscPosCommands.ESC_DEFAULT_LINE_SPACING);
    return this;
  }
  
  public EscPosPrinter lineSpacing(int spacing) throws IOException {
    if (spacing < 0 || spacing > 255) {
      throw new IllegalArgumentException("The spacing is out of range");
    }
    out.write(EscPosCommands.ESC_LINE_SPACING);
    out.write((byte) spacing);
    return this;
  }
  
  public EscPosPrinter characterSpacing(int spacing) throws IOException {
    if (spacing < 0 || spacing > 255) {
      throw new IllegalArgumentException("The spacing is out of range");
    }
    out.write(EscPosCommands.ESC_CHARACTER_SPACING);
    out.write((byte) spacing);
    return this;
  }
  
  public EscPosPrinter align(byte alignment) throws IOException {
    if (alignment < Justification.LEFT || alignment > Justification.RIGHT) {
      throw new IllegalArgumentException();
    }
    out.write(EscPosCommands.ESC_JUSTIFICATION);
    out.write(alignment);
    return this;
  }
  
  public EscPosPrinter justification(byte alignment) throws IOException {
    if (alignment < Justification.LEFT || alignment > Justification.RIGHT) {
      throw new IllegalArgumentException();
    }
    out.write(EscPosCommands.ESC_JUSTIFICATION);
    out.write(alignment);
    return this;
  }
  
  public EscPosPrinter leftMargin(byte margin) throws IOException {
    if (margin < Justification.LEFT || margin > Justification.RIGHT) {
      throw new IllegalArgumentException();
    }
    out.write(EscPosCommands.GS_LEFT_MARGIN);
    out.write((byte) (margin & 0xFF));
    out.write((byte) ((margin >> 8) & 0xFF));
    return this;
  }

  public EscPosPrinter cut(Cut cut) {
    if (cut != null)
      cut.uncheckedWrite(out);
    return this;
  }

  public EscPosPrinter kick(DrawerKick kick) {
    if (kick != null)
      try {
        kick.write(out);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    return this;
  }

  public EscPosPrinter kick(DrawerKick kick, int t1Pulse, int t2Pulse) {
    if (kick != null)
      try {
        kick.write(out, t1Pulse <= 0 ? 0 : t1Pulse, t2Pulse <= 0 ? 0 : t2Pulse);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    return this;
  }

  public EscPosPrinter feedRate(int rate) throws IOException {
    FeedRate.Instance.write(out, rate);
    return this;
  }
  
  public EscPosPrinter image(PrintImage image) throws ImageException, IOException {
    image.print(out);
    return this;
  }

  public EscPosPrinter turnOff() throws IOException {
    byte[] buf = new byte[] { (byte) 27, (byte) 43 };
    Raw.Instance.write(out, buf);
    return this;
  }

  public EscPosPrinter printSelfTest() throws IOException {
    byte[] buf = new byte[] { (byte) 27, (byte) 46 };
    Raw.Instance.write(out, buf);
    return this;
  }

  public EscPosPrinter setLineSpace(int lines) throws IOException {
    if (lines < 0 || lines > 255) {
      throw new IllegalArgumentException("The lines is out of range");
    }
    byte[] buf = new byte[] { (byte) 27, (byte) 51, (byte) (lines & 255) };
    Raw.Instance.write(out, buf);
    return this;
  }

  public EscPosPrinter printLogo() throws IOException {
    byte[] buf = new byte[3];
    buf[0] = (byte) 29;
    buf[1] = (byte) 47;
    Raw.Instance.write(out, buf);
    return this;
  }

  public EscPosPrinter barcode(Barcode barcode) throws IOException {
    barcode.write(out);
    //    byte[] buf = new byte[3];
    //    buf[0] = (byte) 29;
    //    buf[1] = (byte) 47;
    //    Raw.Instance.write(out, buf);
    return this;
  }

  public EscPosPrinter qrcode(QRCode qrcode) throws IOException {
    qrcode.write(out);
    return this;
  }

  public EscPosPrinter execute(Command... commands) throws IOException {
    for (Command command : commands) {
      command.write(out);
    }
    return this;
  }

  @Override
  public String toString() {
    return out.toString();
  }
  
  public EscPosPrinter raw(int val) {
    try {
      Raw.Instance.write(out, val);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return this;
  }

  public EscPosPrinter raw(byte val) {
    try {
      Raw.Instance.write(out, val);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return this;
  }

  public EscPosPrinter raw(byte... vals) {
    if (vals != null)
      try {
        Raw.Instance.write(out, vals);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    return this;
  }

  public EscPosPrinter text(String text) {
    if (text != null)
      try {
        Raw.Instance.write(out, text);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    return this;
  }
}
