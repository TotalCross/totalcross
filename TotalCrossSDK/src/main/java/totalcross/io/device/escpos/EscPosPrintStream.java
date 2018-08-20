package totalcross.io.device.escpos;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;

import totalcross.io.device.escpos.command.Barcode;
import totalcross.io.device.escpos.command.Command;
import totalcross.io.device.escpos.command.EscPosCommands;
import totalcross.io.device.escpos.command.PrintImage;
import totalcross.io.device.escpos.command.QRCode;
import totalcross.ui.image.ImageException;

public class EscPosPrintStream extends FilterOutputStream {

  public static interface PaperSize {
    public static final int A7 = 384;
    public static final int A8 = 576;
  }

  public EscPosPrintStream(OutputStream out) {
    super(out);
  }

  /**
   * Initialize printer. </br>
   * Clears the data in the print buffer and resets the printer modes to the
   * modes that were in effect when the power was turned on.
   * 
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrintStream initialize() throws IOException {
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
  public EscPosPrintStream feed() throws IOException {
    out.write(EscPosCommands.FF);
    return this;
  }

  public EscPosPrintStream ff() throws IOException {
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
  public EscPosPrintStream lineFeed() throws IOException {
    out.write(EscPosCommands.LF);
    return this;
  }
  

  public EscPosPrintStream println() throws IOException {
    out.write(EscPosCommands.LF);
    return this;
  }

  public EscPosPrintStream lf() throws IOException {
    out.write(EscPosCommands.LF);
    return this;
  }

  /**
   * Print and carriage return.
   * 
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrintStream carriageReturn() throws IOException {
    out.write(EscPosCommands.CR);
    return this;
  }

  public EscPosPrintStream cr() throws IOException {
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
  public EscPosPrintStream horizontalTab() throws IOException {
    out.write(EscPosCommands.HT);
    return this;
  }

  public EscPosPrintStream ht() throws IOException {
    out.write(EscPosCommands.HT);
    return this;
  }

  /**
   * Sounds the buzzer.
   * 
   * @return This EscPosPrinter.
   * @throws IOException
   */
  public EscPosPrintStream buzzer() throws IOException {
    out.write(EscPosCommands.BEL);
    return this;
  }

  public EscPosPrintStream bel() throws IOException {
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
  public EscPosPrintStream feedPaper(int lines) throws IOException {
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
  public EscPosPrintStream feedLines(int n) throws IOException {
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
  public EscPosPrintStream textPrintMode(int textPrintMode) throws IOException {
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
  public EscPosPrintStream textSize(byte characterSize) throws IOException {
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
  public EscPosPrintStream underline(boolean enable) throws IOException {
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
  public EscPosPrintStream emphasize(boolean enable) throws IOException {
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
  public EscPosPrintStream doubleStrike(boolean enable) throws IOException {
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
  public EscPosPrintStream reverseWhiteBlack(boolean enable) throws IOException {
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
  public EscPosPrintStream rotate(boolean enable) throws IOException {
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
  public EscPosPrintStream rotate(byte value) throws IOException {
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
  public EscPosPrintStream absolutePrintPosition(int n) throws IOException {
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
  public EscPosPrintStream horizontalTabPosition(byte... tabPositions) throws IOException {
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
  public EscPosPrintStream resetLineSpacing() throws IOException {
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
  public EscPosPrintStream lineSpacing(int spacing) throws IOException {
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
  public EscPosPrintStream characterSpacing(int spacing) throws IOException {
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
  public EscPosPrintStream align(byte alignment) throws IOException {
    if (alignment < EscPosConstants.JUSTIFICATION_LEFT || alignment > EscPosConstants.JUSTIFICATION_RIGHT) {
      throw new IllegalArgumentException();
    }
    out.write(EscPosCommands.ESC_JUSTIFICATION);
    out.write(alignment);
    return this;
  }

  public EscPosPrintStream justification(byte alignment) throws IOException {
    if (alignment < EscPosConstants.JUSTIFICATION_LEFT || alignment > EscPosConstants.JUSTIFICATION_RIGHT) {
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
  public EscPosPrintStream leftMargin(int margin) throws IOException {
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
  public EscPosPrintStream cut(byte cut) throws IOException {
    if (cut < EscPosConstants.CUT_FULL || cut > EscPosConstants.CUT_PART) {
      throw new IllegalArgumentException();
    }
    out.write(EscPosCommands.GS_CUT);
    out.write(cut);
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
  public EscPosPrintStream turnOff() throws IOException {
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
  public EscPosPrintStream selfTest() throws IOException {
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
  public EscPosPrintStream shortSelfTest() throws IOException {
    out.write(EscPosCommands.ESC_SELF_TEST);
    return this;
  }

  public EscPosPrintStream printLogo(byte size) throws IOException {
    if (size < EscPosConstants.LOGO_NORMAL || size > EscPosConstants.LOGO_QUADRUPLE) {
      throw new IllegalArgumentException();
    }
    out.write(EscPosCommands.GS_PRINT_DOWNLOADED_BIT_IMAGE);
    out.write(size);
    return this;
  }
  
  public EscPosPrintStream execute(Command... commands) throws IOException {
    for (Command command : commands) {
      command.write(out);
    }
    return this;
  }

  public EscPosPrintStream print(int val) throws IOException {
    out.write(val);
    return this;
  }

  public EscPosPrintStream print(byte val) throws IOException {
    out.write(val);
    return this;
  }

  public EscPosPrintStream print(byte... vals) throws IOException {
    out.write(vals);
    return this;
  }

  public EscPosPrintStream print(CharSequence text) throws IOException {
    out.write(text.toString().getBytes());
    return this;
  }
  
  public EscPosPrintStream print(PrintImage image) throws ImageException, IOException {
    image.print(out);
    this.println();
    return this;
  }

  public EscPosPrintStream print(Barcode barcode) throws IOException {
    barcode.write(out);
    this.println();
    return this;
  }

  public EscPosPrintStream print(QRCode qrcode) throws IOException {
    qrcode.write(out);
    this.println();
    return this;
  }
}
