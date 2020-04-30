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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import totalcross.sys.Convert;

/**
 * Adds functionality to another output stream, namely the ability to print representations of
 * various data values conveniently through ESC/POS commands.
 *
 * <p>All characters printed by a <code>EscPosPrintStream</code> are converted into bytes using the
 * platform's default character encoding.
 *
 * @author Fábio Sobral
 * @since TotalCross 4.2.0
 */
public class EscPosPrintStream extends FilterOutputStream {

  private Charset charset = Convert.charsetForName("ISO8859-1");

  /**
   * Creates an output stream filter for ESC/POS commands built on top of the specified underlying
   * output stream.
   *
   * @param out the underlying output stream to be assigned to the field <tt>this.out</tt> for later
   *     use, or <code>null</code> if this instance is to be created without an underlying stream.
   */
  public EscPosPrintStream(OutputStream out) {
    super(out);
  }

  /**
   * Initialize printer.
   *
   * <p>Clears the data in the print buffer and resets the printer modes to the modes that were in
   * effect when the power was turned on.
   *
   * @return This EscPosPrinter.
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream initialize() throws IOException {
    out.write(EscPosCommands.ESC_INIT);
    return this;
  }

  /**
   * Print and return to standard mode (in page mode).
   *
   * <p>In page mode, prints all the data in the print buffer collectively and switches from page
   * mode to standard mode.
   *
   * @return This EscPosPrinter.
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream feed() throws IOException {
    out.write(EscPosCommands.FF);
    return this;
  }

  /**
   * Print and return to standard mode (in page mode).
   *
   * <p>In page mode, prints all the data in the print buffer collectively and switches from page
   * mode to standard mode.
   *
   * @return This EscPosPrinter.
   * @throws IOException if an I/O error occurs.
   * @see EscPosPrintStream#feed()
   */
  public EscPosPrintStream ff() throws IOException {
    out.write(EscPosCommands.FF);
    return this;
  }

  /**
   * Print and line feed.
   *
   * <p>Prints the data in the print buffer and feeds one line, based on the current line spacing.
   *
   * @return This EscPosPrinter.
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream lineFeed() throws IOException {
    out.write(EscPosCommands.LF);
    return this;
  }

  /**
   * Print and line feed.
   *
   * <p>Prints the data in the print buffer and feeds one line, based on the current line spacing.
   *
   * @return This EscPosPrinter.
   * @throws IOException if an I/O error occurs.
   * @see EscPosPrintStream#lineFeed()
   */
  public EscPosPrintStream println() throws IOException {
    out.write(EscPosCommands.LF);
    return this;
  }

  /**
   * Print and line feed.
   *
   * <p>Prints the data in the print buffer and feeds one line, based on the current line spacing.
   *
   * @return This EscPosPrinter.
   * @throws IOException if an I/O error occurs.
   * @see EscPosPrintStream#lineFeed()
   */
  public EscPosPrintStream lf() throws IOException {
    out.write(EscPosCommands.LF);
    return this;
  }

  /**
   * Print and carriage return.
   *
   * @return This EscPosPrinter.
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream carriageReturn() throws IOException {
    out.write(EscPosCommands.CR);
    return this;
  }

  /**
   * Print and carriage return.
   *
   * @return This EscPosPrinter.
   * @throws IOException if an I/O error occurs.
   * @see EscPosPrintStream#carriageReturn()
   */
  public EscPosPrintStream cr() throws IOException {
    out.write(EscPosCommands.CR);
    return this;
  }

  /**
   * Horizontal tab.
   *
   * <p>Moves the print position to the next horizontal tab position.
   *
   * @return This EscPosPrinter.
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream horizontalTab() throws IOException {
    out.write(EscPosCommands.HT);
    return this;
  }

  /**
   * Horizontal tab.
   *
   * <p>Moves the print position to the next horizontal tab position.
   *
   * @return This EscPosPrinter.
   * @throws IOException if an I/O error occurs.
   * @see EscPosPrintStream#horizontalTab()
   */
  public EscPosPrintStream ht() throws IOException {
    out.write(EscPosCommands.HT);
    return this;
  }

  /**
   * Sounds the buzzer.
   *
   * @return This EscPosPrinter.
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream buzzer() throws IOException {
    out.write(EscPosCommands.BEL);
    return this;
  }

  /**
   * Sounds the buzzer.
   *
   * @return This EscPosPrinter.
   * @throws IOException if an I/O error occurs.
   * @see EscPosPrintStream#buzzer()
   */
  public EscPosPrintStream bel() throws IOException {
    out.write(EscPosCommands.BEL);
    return this;
  }

  /**
   * Print and feed paper.
   *
   * <p>Prints the data in the print buffer and feeds the paper n × (vertical or horizontal motion
   * unit).
   *
   * <p>A motion unit usually measures 1/203 inches or 0.125 mm.
   *
   * @param n the motion units value to feed, must be inside the range [0,255]
   * @return This EscPosPrinter.
   * @throws IllegalArgumentException if the value of argument n is outside the range [0,255]
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream feedPaper(int n) throws IOException {
    if (n < 0 || n > 255) {
      throw new IllegalArgumentException("The value of argument 'n' is out of range");
    }
    out.write(EscPosCommands.ESC_FEED_PAPER);
    out.write((byte) n);
    return this;
  }

  /**
   * Prints the data in the print buffer and feeds n lines.
   *
   * @param n the number of lines to feed, must be inside the range [0,255]
   * @return This EscPosPrinter.
   * @throws IllegalArgumentException if the value of argument n is outside the range [0,255]
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream feedLines(int n) throws IOException {
    if (n < 0 || n > 255) {
      throw new IllegalArgumentException("The value of argument 'n' is out of range");
    }
    out.write(EscPosCommands.ESC_FEED_LINES);
    out.write((byte) n);
    return this;
  }

  /**
   * Select print mode(s).
   *
   * <p>The argument textPrintMode must be one of the EscPosConstants.TEXTPRINTMODE constants
   * defined, or any valid combination of them ored together.
   *
   * <p>Other values in the range [0,255] may be supported by selected printers, refer to your
   * printer documentation for more details.
   *
   * @param textPrintMode one of the EscPosConstants.TEXTPRINTMODE constants, or any valid
   *     combination of them ored together.
   * @return This EscPosPrinter.
   * @throws IllegalArgumentException if the value of argument textPrintMode is outside the range
   *     [0,255]
   * @throws IOException if an I/O error occurs.
   * @see EscPosConstants#TEXTPRINTMODE_FONT_1
   * @see EscPosConstants#TEXTPRINTMODE_FONT_2
   * @see EscPosConstants#TEXTPRINTMODE_EMPHASIZED
   * @see EscPosConstants#TEXTPRINTMODE_DOUBLE_HEIGHT
   * @see EscPosConstants#TEXTPRINTMODE_DOUBLE_WIDTH
   * @see EscPosConstants#TEXTPRINTMODE_UNDERLINE
   */
  public EscPosPrintStream textPrintMode(int textPrintMode) throws IOException {
    if (textPrintMode < 0 || textPrintMode > 255) {
      throw new IllegalArgumentException("The value of argument 'textPrintMode' is out of range");
    }
    out.write(EscPosCommands.ESC_PRINT_MODE);
    out.write((byte) textPrintMode);
    return this;
  }

  /**
   * Select character size
   *
   * <p>The argument characterSize must be one of the EscPosConstants.CHARACTERSIZE constants, or a
   * combination of a width constant with a height constant ored together.
   *
   * <p>Most printers support only single or double width/height (constants 1 or 2), but other
   * values in the range [0,255] may be supported by selected printers, refer to your printer
   * documentation for more details.
   *
   * @see CharacterSize
   * @param characterSize one of the EscPosConstants.CHARACTERSIZE constants, or a combination of a
   *     width constant with a height constant ored together.
   * @return This EscPosPrinter.
   * @throws IOException if an I/O error occurs.
   * @see EscPosConstants#CHARACTERSIZE_WIDTH_1
   * @see EscPosConstants#CHARACTERSIZE_WIDTH_2
   * @see EscPosConstants#CHARACTERSIZE_HEIGHT_1
   * @see EscPosConstants#CHARACTERSIZE_HEIGHT_2
   */
  public EscPosPrintStream textSize(byte characterSize) throws IOException {
    out.write(EscPosCommands.GS_CHARACTER_SIZE);
    out.write(characterSize);
    return this;
  }

  /**
   * Turn underline mode on/off.
   *
   * @param enable sets the command to enable or disable underline mode
   * @return This EscPosPrinter.
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream underline(boolean enable) throws IOException {
    out.write(EscPosCommands.ESC_UNDERLINE);
    out.write((byte) (enable ? 1 : 0));
    return this;
  }

  /**
   * Turn emphasized mode on/off.
   *
   * @param enable sets the command to enable or disable emphasize mode
   * @return This EscPosPrinter.
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream emphasize(boolean enable) throws IOException {
    out.write(EscPosCommands.ESC_EMPHASIZE);
    out.write((byte) (enable ? 1 : 0));
    return this;
  }

  /**
   * Turn double-strike mode on/off.
   *
   * @param enable sets the command to enable or disable double-strike mode
   * @return This EscPosPrinter.
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream doubleStrike(boolean enable) throws IOException {
    out.write(EscPosCommands.ESC_DOUBLESTRIKE);
    out.write((byte) (enable ? 1 : 0));
    return this;
  }

  /**
   * Turn white/black reverse print mode on/off.
   *
   * @param enable sets the command to enable or disable white/black reverse print mode
   * @return This EscPosPrinter.
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream reverseWhiteBlack(boolean enable) throws IOException {
    out.write(EscPosCommands.GS_REVERSE_BW);
    out.write((byte) (enable ? 1 : 0));
    return this;
  }

  /**
   * Turn 90° clockwise rotation mode on/off.
   *
   * @param enable sets the command to enable or disable 90° clockwise rotation mode
   * @return This EscPosPrinter.
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream rotate(boolean enable) throws IOException {
    out.write(EscPosCommands.ESC_ROTATE);
    out.write((byte) (enable ? 1 : 0));
    return this;
  }

  /**
   * Sets 90° clockwise rotation with given value.
   *
   * <p>Provided for printers that define other values for rotation mode besides on/off, refer to
   * your printer documentation for more information.
   *
   * @param value a valid value for the 'ESC V' command as defined by your printer documentation
   * @return This EscPosPrinter.
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream rotate(byte value) throws IOException {
    out.write(EscPosCommands.ESC_ROTATE);
    out.write(value);
    return this;
  }

  /**
   * Set absolute print position.
   *
   * <p>Moves the print position to n × (horizontal or vertical motion unit) from the left edge of
   * the print area.
   *
   * <p>A motion unit usually measures 1/203 inches or 0.125 mm.
   *
   * @param n the motion units value to set for the absolute print position from the left edge of
   *     the print area, which mus be in the range [0,65535]
   * @return This EscPosPrinter.
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream absolutePrintPosition(int n) throws IOException {
    if (n < 0 || n > 65535) {
      throw new IllegalArgumentException("The value of argument 'n' is out of range");
    }
    out.write(EscPosCommands.ESC_PRINT_POSITION);
    out.write((byte) (n & 0xFF));
    out.write((byte) ((n >> 8) & 0xFF));
    return this;
  }

  /**
   * Set horizontal tab positions.
   *
   * <p>A maximum of 32 horizontal tab positions in ascending order can be set, with each value in
   * the range [0,255].
   *
   * <p>Executing this method with no arguments clears all the set tab positions.
   *
   * <p>ATTENTION: The maximum amount of tab positions may be lower for some printers - usually any
   * data past the accepted maximum of tab positions will be processed as general data. Refer to
   * your printer documentation before using this command with more than 8 tab positions.
   *
   * <p>Devices with known maximum number of tab positions:
   * <li>Datecs DPP-350: 32
   * <li>Leopardo A7: 8
   *
   * @param tabPositions A maximum of 32 tab positions in ascending orders, with each value in the
   *     range [0,255], or empty to clear all the set tab positions.
   * @return This EscPosPrinter.
   * @throws IllegalArgumentException if the maximum number of tab positions is greater than 32, OR
   *     if any given tab position is outside the range [0,255], OR if any given tab position is
   *     less than or equal the preceding value.
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream horizontalTabPosition(int... tabPositions) throws IOException {
    if (tabPositions.length > 32) {
      throw new IllegalArgumentException("A maximum of 32 tab positions may be specified");
    }
    int lastPosition = 0;
    final int length = tabPositions.length;
    for (int i = 0; i < length; i++) {
      final int tabPosition = tabPositions[i];
      if (tabPosition < 0 || tabPosition > 255) {
        throw new IllegalArgumentException(
            "The value of the " + (i + 1) + "º tabPosition is is outside the range [0,255]");
      }
      if (tabPosition <= lastPosition) {
        throw new IllegalArgumentException(
            "The value of the "
                + (i + 1)
                + "º tabPosition is less than or equal to the preceding value");
      }
      lastPosition = tabPosition;
    }
    out.write(EscPosCommands.ESC_HORIZONTAL_TAB_POSITION);
    for (int tabPosition : tabPositions) {
      out.write((byte) tabPosition);
    }
    out.write(EscPosCommands.NULL);
    return this;
  }

  /**
   * Select default line spacing.
   *
   * @return This EscPosPrinter.
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream resetLineSpacing() throws IOException {
    out.write(EscPosCommands.ESC_DEFAULT_LINE_SPACING);
    return this;
  }

  /**
   * Set line spacing.
   *
   * <p>Sets the line spacing to n × (vertical or horizontal motion unit).
   *
   * <p>A motion unit usually measures 1/203 inches or 0.125 mm.
   *
   * @param n the size of the line spacing in motion units, which must be inside the range [0,255]
   * @return This EscPosPrinter.
   * @throws IllegalArgumentException if the value of argument n is outside the range [0,255]
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream lineSpacing(int n) throws IOException {
    if (n < 0 || n > 255) {
      throw new IllegalArgumentException("The value of argument 'n' is out of range");
    }
    out.write(EscPosCommands.ESC_LINE_SPACING);
    out.write((byte) n);
    return this;
  }

  /**
   * Set right-side character spacing.
   *
   * <p>Sets the right-side character spacing to n × (horizontal or vertical motion unit).
   *
   * <p>A motion unit usually measures 1/203 inches or 0.125 mm.
   *
   * @param n the size of the line spacing in motion units, which must be inside the range [0,255]
   * @return This EscPosPrinter.
   * @throws IllegalArgumentException if the value of argument n is outside the range [0,255]
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream characterSpacing(int n) throws IOException {
    if (n < 0 || n > 255) {
      throw new IllegalArgumentException("The value of argument 'n' is out of range");
    }
    out.write(EscPosCommands.ESC_CHARACTER_SPACING);
    out.write((byte) n);
    return this;
  }

  /**
   * Select alignment.
   *
   * <p>Sets the printing alignment to either ALIGN_LEFT, ALIGN_CENTER or ALIGN_RIGHT.
   *
   * @param alignment the printing alignment, which must be either ALIGN_LEFT, ALIGN_CENTER or
   *     ALIGN_RIGHT.
   * @return This EscPosPrinter.
   * @throws IllegalArgumentException if the value of argument alignment is not ALIGN_LEFT,
   *     ALIGN_CENTER or ALIGN_RIGHT.
   * @throws IOException if an I/O error occurs.
   * @see EscPosConstants#ALIGN_LEFT
   * @see EscPosConstants#ALIGN_CENTER
   * @see EscPosConstants#ALIGN_RIGHT
   */
  public EscPosPrintStream align(byte alignment) throws IOException {
    if (alignment < EscPosConstants.ALIGN_LEFT || alignment > EscPosConstants.ALIGN_RIGHT) {
      throw new IllegalArgumentException("The value of argument 'alignment' is invalid");
    }
    out.write(EscPosCommands.ESC_JUSTIFICATION);
    out.write(alignment);
    return this;
  }

  /**
   * Set left margin.
   *
   * <p>In standard mode, sets the left margin to n × (horizontal motion unit) from the left edge of
   * the printable area.
   *
   * <p>A motion unit usually measures 1/203 inches or 0.125 mm.
   *
   * @param n the motion units value to set the left margin, must be inside the range [0,65535]
   * @return This EscPosPrinter.
   * @throws IllegalArgumentException if the value of argument n is outside the range [0,65535]
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream leftMargin(int n) throws IOException {
    if (n < 0 || n > 65535) {
      throw new IllegalArgumentException("The value of argument 'n' is out of range");
    }
    out.write(EscPosCommands.GS_LEFT_MARGIN);
    out.write((byte) (n & 0xFF));
    out.write((byte) ((n >> 8) & 0xFF));
    return this;
  }

  /**
   * Select cut mode and cut paper.
   *
   * @param cut the cut mode, which must be either CUT_FULL or CUT_PART.
   * @return This EscPosPrinter.
   * @throws IllegalArgumentException if the value of argument cut is not either CUT_FULL or
   *     CUT_PART
   * @return This EscPosPrinter.
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream cut(byte cut) throws IOException {
    if (cut < EscPosConstants.CUT_FULL || cut > EscPosConstants.CUT_PART) {
      throw new IllegalArgumentException("The value of argument 'cut' is invalid");
    }
    out.write(EscPosCommands.GS_CUT);
    out.write(cut);
    return this;
  }

  /**
   * Switch OFF the printer.
   *
   * <p>This command is not listed in the original ESC/POS Manual, refer to the printer's manual to
   * verify support for this command.
   *
   * <p>Known supported devices:
   * <li>Datecs DPP-350
   *
   * @return This EscPosPrinter.
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream turnOff() throws IOException {
    out.write(EscPosCommands.ESC_TURN_OFF);
    return this;
  }

  /**
   * Prints test page and self-diagnostic information. The self-diagnostic information includes
   * print density, print head temperature, battery voltage, baud rate in case of work via RS232 and
   * others.
   *
   * <p>This command is not listed in the original ESC/POS Manual, refer to the printer's manual to
   * verify support for this command.
   *
   * <p>Known supported devices:
   * <li>Datecs DPP-350
   *
   * @return This EscPosPrinter.
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream selfTest() throws IOException {
    out.write(EscPosCommands.ESC_SELF_TEST);
    return this;
  }

  /**
   * Prints current printer parameters, including intensity, temperature of the print head, battery
   * voltage, speed in case of serial connection, etc.
   *
   * <p>This command is not listed in the original ESC/POS Manual, refer to the printer's manual to
   * verify support for this command.
   *
   * <p>Known supported devices:
   * <li>Datecs DPP-350
   *
   * @return This EscPosPrinter.
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream shortSelfTest() throws IOException {
    out.write(EscPosCommands.ESC_SHORT_SELF_TEST);
    return this;
  }

  /**
   * Sets the default charset to be used when printing CharSequence to the underlying stream.
   *
   * @param charset The {@link Charset} to be used to encode the data
   * @return This EscPosPrinter.
   * @throws IllegalArgumentException If the given charset is null
   */
  public EscPosPrintStream charset(Charset charset) throws IllegalArgumentException {
    if (charset == null) {
      throw new IllegalArgumentException();
    }
    this.charset = charset;
    return this;
  }

  /**
   * Sets the default charset to be used when printing CharSequence to the underlying stream.
   *
   * @param charsetName The name of a supported {@link charset}
   * @return This EscPosPrinter.
   * @throws IllegalArgumentException If the given charsetName is null
   * @throws UnsupportedEncodingException If no support for the named charset is available
   */
  public EscPosPrintStream charset(String charsetName)
      throws IllegalArgumentException, UnsupportedEncodingException {
    if (charsetName == null) {
      throw new IllegalArgumentException();
    }
    this.charset = Charset.forName(charsetName);
    return this;
  }

  /**
   * Print downloaded bit image using the specified mode (size).
   *
   * <p>The value of argument mode must be either LOGO_NORMAL, LOGO_DOUBLE_WIDTH, LOGO_DOUBLE_HEIGHT
   * or LOGO_QUADRUPLE.
   *
   * @param mode the printing mode, which must be either LOGO_NORMAL, LOGO_DOUBLE_WIDTH,
   *     LOGO_DOUBLE_HEIGHT or LOGO_QUADRUPLE.
   * @return This EscPosPrinter.
   * @throws IllegalArgumentException if the value of argument mode is not LOGO_NORMAL,
   *     LOGO_DOUBLE_WIDTH, LOGO_DOUBLE_HEIGHT or LOGO_QUADRUPLE.
   * @throws IOException if an I/O error occurs.
   * @see EscPosConstants#LOGO_NORMAL
   * @see EscPosConstants#LOGO_DOUBLE_WIDTH
   * @see EscPosConstants#LOGO_DOUBLE_HEIGHT
   * @see EscPosConstants#LOGO_QUADRUPLE
   */
  public EscPosPrintStream printLogo(byte mode) throws IOException {
    if (mode < EscPosConstants.LOGO_NORMAL || mode > EscPosConstants.LOGO_QUADRUPLE) {
      throw new IllegalArgumentException();
    }
    out.write(EscPosCommands.GS_PRINT_DOWNLOADED_BIT_IMAGE);
    out.write(mode);
    return this;
  }

  /**
   * Writes the specified byte to the underlying stream.
   *
   * @param b the <code>byte</code>.
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream print(byte b) throws IOException {
    out.write(b);
    return this;
  }

  /**
   * Writes the specified byte to the underlying stream. The general contract for <code>write</code>
   * is that one byte is written to the output stream. The byte to be written is the eight low-order
   * bits of the argument <code>b</code>. The 24 high-order bits of <code>b</code> are ignored.
   *
   * @param b the <code>byte</code>.
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream print(int b) throws IOException {
    out.write(b);
    return this;
  }

  /**
   * Writes <code>len</code> bytes from the specified byte array starting at offset <code>off</code>
   * to the underlying output stream.
   *
   * <p>If <code>b</code> is <code>null</code>, a <code>NullPointerException</code> is thrown.
   *
   * <p>If <code>off</code> is negative, or <code>len</code> is negative, or <code>off+len</code> is
   * greater than the length of the array <code>b</code>, then an <tt>IndexOutOfBoundsException</tt>
   * is thrown.
   *
   * @param b the data.
   * @param off the start offset in the data.
   * @param len the number of bytes to write.
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream print(byte b[], int off, int len) throws IOException {
    out.write(b, off, len);
    return this;
  }

  /**
   * Writes the given bytes to the underlying output stream.
   *
   * @param b the data.
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream print(byte... b) throws IOException {
    out.write(b);
    return this;
  }

  /**
   * Writes the given text to the underlying stream.
   *
   * <p>All characters are converted into bytes using the platform's default character encoding.
   *
   * @param text the data
   * @return This EscPosPrinter.
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream print(CharSequence text) throws IOException {
    out.write(text.toString().getBytes(charset));
    return this;
  }

  /**
   * Writes an EscPosPrintObject to the underlying stream.
   *
   * @param printStruct an object that writes its own set of ESC/POS commands
   * @return This EscPosPrinter.
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream print(EscPosPrintObject printStruct) throws IOException {
    printStruct.write(out);
    this.println();
    return this;
  }

  /**
   * Writes a set of EscPosPrintObject to the underlying stream.
   *
   * @param commands a set of EscPosPrintStruct
   * @return This EscPosPrinter.
   * @throws IOException if an I/O error occurs.
   */
  public EscPosPrintStream print(EscPosPrintObject... commands) throws IOException {
    for (EscPosPrintObject command : commands) {
      command.write(out);
      this.println();
    }
    return this;
  }
}
