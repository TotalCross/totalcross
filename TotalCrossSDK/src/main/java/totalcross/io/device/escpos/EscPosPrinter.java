package totalcross.io.device.escpos;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;

import totalcross.io.device.escpos.command.Barcode;
import totalcross.io.device.escpos.command.CharacterSize;
import totalcross.io.device.escpos.command.Command;
import totalcross.io.device.escpos.command.Cut;
import totalcross.io.device.escpos.command.DrawerKick;
import totalcross.io.device.escpos.command.EscPosCommands;
import totalcross.io.device.escpos.command.Justification;
import totalcross.io.device.escpos.command.PrintImage;
import totalcross.io.device.escpos.command.QRCode;
import totalcross.io.device.escpos.command.Raw;
import totalcross.io.device.escpos.command.TextPrintMode;
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

  /**
   * Initialize printer. </br>
   * Clears the data in the print buffer and resets the printer modes to the
   * modes that were in effect when the power was turned on.
   * 
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrinter initialize() throws IOException {
    out.write(EscPosCommands.ESC_INIT);
    return this;
  }

  /**
   * Print and return to standard mode (in page mode). </br>
   * In page mode, prints all the data in the print buffer collectively and
   * switches from page mode to standard mode.
   * 
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrinter feed() throws IOException {
    out.write(EscPosCommands.FF);
    return this;
  }

  public EscPosPrinter ff() throws IOException {
    out.write(EscPosCommands.FF);
    return this;
  }

  /**
   * Print and line feed.</br>
   * Prints the data in the print buffer and feeds one line, based on the
   * current line spacing.
   * 
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrinter lineFeed() throws IOException {
    out.write(EscPosCommands.LF);
    return this;
  }

  public EscPosPrinter lf() throws IOException {
    out.write(EscPosCommands.LF);
    return this;
  }

  /**
   * Print and carriage return.
   * 
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrinter carriageReturn() throws IOException {
    out.write(EscPosCommands.CR);
    return this;
  }

  public EscPosPrinter cr() throws IOException {
    out.write(EscPosCommands.CR);
    return this;
  }

  /**
   * Horizontal tab.</br>
   * Moves the print position to the next horizontal tab position.
   * 
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrinter horizontalTab() throws IOException {
    out.write(EscPosCommands.HT);
    return this;
  }

  public EscPosPrinter ht() throws IOException {
    out.write(EscPosCommands.HT);
    return this;
  }

  /**
   * Sounds the buzzer.
   * 
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrinter buzzer() throws IOException {
    out.write(EscPosCommands.BEL);
    return this;
  }

  public EscPosPrinter bel() throws IOException {
    out.write(EscPosCommands.BEL);
    return this;
  }

  /**
   * Print and feed paper.</br>
   * Prints the data in the print buffer and feeds the paper n × (vertical or
   * horizontal motion unit).</br>
   * 0.00492610837 inches</br>
   * 0,125 mm</br>
   * 
   * @param lines
   *          0 ≤ n ≤ 255
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrinter feedPaper(int lines) throws IOException {
    if (lines < 0 || lines > 255) {
      throw new IllegalArgumentException("The lines is out of range");
    }
    out.write(EscPosCommands.ESC_FEED_PAPER);
    out.write((byte) lines);
    return this;
  }

  /**
   * Prints the data in the print buffer and feeds n lines.
   * 
   * @param n
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrinter feedLines(int n) throws IOException {
    if (n < 0 || n > 255) {
      throw new IllegalArgumentException("The lines is out of range");
    }
    out.write(EscPosCommands.ESC_FEED_LINES);
    out.write((byte) n);
    return this;
  }

  /**
   * Select print mode(s)
   * 
   * @see TextPrintMode
   * @param textPrintMode
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrinter textPrintMode(int textPrintMode) throws IOException {
    out.write(EscPosCommands.ESC_PRINT_MODE);
    out.write((byte) textPrintMode);
    return this;
  }

  /**
   * Select character size
   * 
   * @see CharacterSize
   * @param characterSize
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrinter textSize(byte characterSize) throws IOException {
    out.write(EscPosCommands.GS_CHARACTER_SIZE);
    out.write(characterSize);
    return this;
  }

  /**
   * Turn underline mode on/off.
   * 
   * @param enable
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrinter underline(boolean enable) throws IOException {
    out.write(EscPosCommands.ESC_UNDERLINE);
    out.write((byte) (enable ? 1 : 0));
    return this;
  }

  /**
   * Turn emphasized mode on/off.
   * 
   * @param enable
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrinter emphasize(boolean enable) throws IOException {
    out.write(EscPosCommands.ESC_EMPHASIZE);
    out.write((byte) (enable ? 1 : 0));
    return this;
  }

  /**
   * Turn double-strike mode on/off.
   * 
   * @param enable
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrinter doubleStrike(boolean enable) throws IOException {
    out.write(EscPosCommands.ESC_DOUBLESTRIKE);
    out.write((byte) (enable ? 1 : 0));
    return this;
  }

  /**
   * Turn white/black reverse print mode on/off.
   * 
   * @param enable
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrinter reverseWhiteBlack(boolean enable) throws IOException {
    out.write(EscPosCommands.GS_REVERSE_BW);
    out.write((byte) (enable ? 1 : 0));
    return this;
  }

  /**
   * Turn 90° clockwise rotation mode on/off.
   * 
   * @param enable
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrinter rotate(boolean enable) throws IOException {
    out.write(EscPosCommands.ESC_ROTATE);
    out.write((byte) (enable ? 1 : 0));
    return this;
  }

  /**
   * Sets 90° clockwise rotation with given value.</br>
   * Provided for printers that define other values for rotation besides on/off,
   * refer to your printer manual for more information.
   * 
   * @param value
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrinter rotate(byte value) throws IOException {
    out.write(EscPosCommands.ESC_ROTATE);
    out.write(value);
    return this;
  }

  /**
   * Set absolute print position.</br>
   * Moves the print position to n × (horizontal or vertical motion unit) from
   * the left edge of the print area.
   * 
   * @param n
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrinter absolutePrintPosition(int n) throws IOException {
    out.write(EscPosCommands.ESC_PRINT_POSITION);
    out.write((byte) (n & 0xFF));
    out.write((byte) ((n >> 8) & 0xFF));
    return this;
  }

  /**
   * Set horizontal tab positions. </br>
   * !!!
   * 
   * @param tabPositions
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrinter horizontalTabPosition(byte... tabPositions) throws IOException {
    if (tabPositions.length > 32) {
      throw new IllegalArgumentException();
    }
    out.write(EscPosCommands.ESC_HORIZONTAL_TAB_POSITION);
    out.write(tabPositions);
    out.write(EscPosCommands.NULL);
    return this;
  }

  /**
   * Select default line spacing.
   * 
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrinter resetLineSpacing() throws IOException {
    out.write(EscPosCommands.ESC_DEFAULT_LINE_SPACING);
    return this;
  }

  /**
   * Set line spacing.</br>
   * Sets the line spacing to n × (vertical or horizontal motion unit).
   * 
   * @param spacing
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrinter lineSpacing(int spacing) throws IOException {
    if (spacing < 0 || spacing > 255) {
      throw new IllegalArgumentException("The spacing is out of range");
    }
    out.write(EscPosCommands.ESC_LINE_SPACING);
    out.write((byte) spacing);
    return this;
  }

  /**
   * Set right-side character spacing.</br>
   * Sets the right-side character spacing to n × (horizontal or vertical motion
   * unit).
   * 
   * @param spacing
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrinter characterSpacing(int spacing) throws IOException {
    if (spacing < 0 || spacing > 255) {
      throw new IllegalArgumentException("The spacing is out of range");
    }
    out.write(EscPosCommands.ESC_CHARACTER_SPACING);
    out.write((byte) spacing);
    return this;
  }

  /**
   * Select justification.
   * 
   * @see Justification
   * @param alignment
   * @return This EscPosPrinter.
   * @throws IOException
   */
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

  /**
   * Set left margin.</br>
   * In standard mode, sets the left margin to (nL + nH × 256) × (horizontal
   * motion unit) from the left edge of the printable area.
   * 
   * @param margin
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrinter leftMargin(int margin) throws IOException {
    if (margin < 0 || margin > 65535) {
      throw new IllegalArgumentException();
    }
    out.write(EscPosCommands.GS_LEFT_MARGIN);
    out.write((byte) (margin & 0xFF));
    out.write((byte) ((margin >> 8) & 0xFF));
    return this;
  }

  /**
   * Select cut mode and cut paper.
   * 
   * @param cut
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrinter cut(byte cut) throws IOException {
    if (cut < Cut.FULL || cut > Cut.PART) {
      throw new IllegalArgumentException();
    }
    out.write(EscPosCommands.GS_CUT);
    out.write(cut);
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

  /**
   * Switch OFF the printer.</br>
   * </br>
   * This command is not listed in the original ESC/POS Manual, refer to the
   * printer's manual to verify support for this command.</br>
   * </br>
   * Known supported devices:
   * <li>Datecs DPP-350</li> </br>
   * 
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrinter turnOff() throws IOException {
    out.write(EscPosCommands.ESC_TURN_OFF);
    return this;
  }

  /**
   * Prints test page and self-diagnostic information. The self-diagnostic
   * information includes print density, print head temperature, battery
   * voltage, baud rate in case of work via RS232 and others.</br>
   * </br>
   * This command is not listed in the original ESC/POS Manual, refer to the
   * printer's manual to verify support for this command.</br>
   * </br>
   * Known supported devices:
   * <li>Datecs DPP-350</li> </br>
   * 
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrinter selfTest() throws IOException {
    out.write(EscPosCommands.ESC_SELF_TEST);
    return this;
  }

  /**
   * Prints current printer parameters, including intensity, temperature of the
   * print head, battery voltage, speed in case of serial connection, etc.</br>
   * </br>
   * This command is not listed in the original ESC/POS Manual, refer to the
   * printer's manual to verify support for this command.</br>
   * </br>
   * Known supported devices:
   * <li>Datecs DPP-350</li> </br>
   * 
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrinter shortSelfTest() throws IOException {
    out.write(EscPosCommands.ESC_SELF_TEST);
    return this;
  }

  public EscPosPrinter image(PrintImage image) throws ImageException, IOException {
    image.print(out);
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
