/**Copyright 2003 by Andrés Ederra.
 * The contents of this file are subject to the Mozilla Public License
 *	Version 1.1 (the "License"); you may not use this file except in
 *	compliance with the License. You may obtain a copy of the License at
 *	http://www.mozilla.org/MPL/
 *
 *	Software distributed under the License is distributed on an "AS IS"
 *	basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 *	License for the specific language governing rights and limitations
 *	under the License.
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 * 
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
 *
 * Derived Code is 'jbars, a free JAVA barcode generation library'
 * The initial developer of jbars is Andrés Ederra. Portions created by the initial
 * Developer of jbars are Copyright (C) 2003 by Andrés Ederra
 **/
package totalcross.barcode;

import totalcross.ui.font.Font;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;

/** Implements the code 128 and UCC/EAN-128. Other symbologies are allowed in raw mode.<p>
 * The code types allowed are:<br>
 * <ul>
 * <li><b>CODE128</b> - plain barcode 128.
 * <li><b>CODE128_UCC</b> - support for UCC/EAN-128.
 * <li><b>CODE128_RAW</b> - raw mode. The code attribute has the actual codes from 0
 *     to 105 followed by '&#92;uffff' and the human readable text.
 * </ul>
 * The default parameters are:
 * <pre>
 * x = 1f;
 * font = new Font("Helvetica",Font.PLAIN,20));
 * textAlignment = ALIGN_CENTER;
 * codeType = CODE128;
 * transparent = true;
 * shotText = true
 * quietZone = true;
 * quietZoneX = 10;
 * </pre>
 *
 * This class is based on iText "A Free Java-PDF library by Bruno Lowagie and Paulo Soares" using the Mozilla Public License(MPL)
 *
 * @author Andres Ederra, Paulo Soares 
 */
public class Barcode128 extends Barcode {

  /** The bars to generate the code.
   */
  static byte BARS[][] = { { 2, 1, 2, 2, 2, 2 }, { 2, 2, 2, 1, 2, 2 }, { 2, 2, 2, 2, 2, 1 }, { 1, 2, 1, 2, 2, 3 },
      { 1, 2, 1, 3, 2, 2 }, { 1, 3, 1, 2, 2, 2 }, { 1, 2, 2, 2, 1, 3 }, { 1, 2, 2, 3, 1, 2 }, { 1, 3, 2, 2, 1, 2 },
      { 2, 2, 1, 2, 1, 3 }, { 2, 2, 1, 3, 1, 2 }, { 2, 3, 1, 2, 1, 2 }, { 1, 1, 2, 2, 3, 2 }, { 1, 2, 2, 1, 3, 2 },
      { 1, 2, 2, 2, 3, 1 }, { 1, 1, 3, 2, 2, 2 }, { 1, 2, 3, 1, 2, 2 }, { 1, 2, 3, 2, 2, 1 }, { 2, 2, 3, 2, 1, 1 },
      { 2, 2, 1, 1, 3, 2 }, { 2, 2, 1, 2, 3, 1 }, { 2, 1, 3, 2, 1, 2 }, { 2, 2, 3, 1, 1, 2 }, { 3, 1, 2, 1, 3, 1 },
      { 3, 1, 1, 2, 2, 2 }, { 3, 2, 1, 1, 2, 2 }, { 3, 2, 1, 2, 2, 1 }, { 3, 1, 2, 2, 1, 2 }, { 3, 2, 2, 1, 1, 2 },
      { 3, 2, 2, 2, 1, 1 }, { 2, 1, 2, 1, 2, 3 }, { 2, 1, 2, 3, 2, 1 }, { 2, 3, 2, 1, 2, 1 }, { 1, 1, 1, 3, 2, 3 },
      { 1, 3, 1, 1, 2, 3 }, { 1, 3, 1, 3, 2, 1 }, { 1, 1, 2, 3, 1, 3 }, { 1, 3, 2, 1, 1, 3 }, { 1, 3, 2, 3, 1, 1 },
      { 2, 1, 1, 3, 1, 3 }, { 2, 3, 1, 1, 1, 3 }, { 2, 3, 1, 3, 1, 1 }, { 1, 1, 2, 1, 3, 3 }, { 1, 1, 2, 3, 3, 1 },
      { 1, 3, 2, 1, 3, 1 }, { 1, 1, 3, 1, 2, 3 }, { 1, 1, 3, 3, 2, 1 }, { 1, 3, 3, 1, 2, 1 }, { 3, 1, 3, 1, 2, 1 },
      { 2, 1, 1, 3, 3, 1 }, { 2, 3, 1, 1, 3, 1 }, { 2, 1, 3, 1, 1, 3 }, { 2, 1, 3, 3, 1, 1 }, { 2, 1, 3, 1, 3, 1 },
      { 3, 1, 1, 1, 2, 3 }, { 3, 1, 1, 3, 2, 1 }, { 3, 3, 1, 1, 2, 1 }, { 3, 1, 2, 1, 1, 3 }, { 3, 1, 2, 3, 1, 1 },
      { 3, 3, 2, 1, 1, 1 }, { 3, 1, 4, 1, 1, 1 }, { 2, 2, 1, 4, 1, 1 }, { 4, 3, 1, 1, 1, 1 }, { 1, 1, 1, 2, 2, 4 },
      { 1, 1, 1, 4, 2, 2 }, { 1, 2, 1, 1, 2, 4 }, { 1, 2, 1, 4, 2, 1 }, { 1, 4, 1, 1, 2, 2 }, { 1, 4, 1, 2, 2, 1 },
      { 1, 1, 2, 2, 1, 4 }, { 1, 1, 2, 4, 1, 2 }, { 1, 2, 2, 1, 1, 4 }, { 1, 2, 2, 4, 1, 1 }, { 1, 4, 2, 1, 1, 2 },
      { 1, 4, 2, 2, 1, 1 }, { 2, 4, 1, 2, 1, 1 }, { 2, 2, 1, 1, 1, 4 }, { 4, 1, 3, 1, 1, 1 }, { 2, 4, 1, 1, 1, 2 },
      { 1, 3, 4, 1, 1, 1 }, { 1, 1, 1, 2, 4, 2 }, { 1, 2, 1, 1, 4, 2 }, { 1, 2, 1, 2, 4, 1 }, { 1, 1, 4, 2, 1, 2 },
      { 1, 2, 4, 1, 1, 2 }, { 1, 2, 4, 2, 1, 1 }, { 4, 1, 1, 2, 1, 2 }, { 4, 2, 1, 1, 1, 2 }, { 4, 2, 1, 2, 1, 1 },
      { 2, 1, 2, 1, 4, 1 }, { 2, 1, 4, 1, 2, 1 }, { 4, 1, 2, 1, 2, 1 }, { 1, 1, 1, 1, 4, 3 }, { 1, 1, 1, 3, 4, 1 },
      { 1, 3, 1, 1, 4, 1 }, { 1, 1, 4, 1, 1, 3 }, { 1, 1, 4, 3, 1, 1 }, { 4, 1, 1, 1, 1, 3 }, { 4, 1, 1, 3, 1, 1 },
      { 1, 1, 3, 1, 4, 1 }, { 1, 1, 4, 1, 3, 1 }, { 3, 1, 1, 1, 4, 1 }, { 4, 1, 1, 1, 3, 1 }, { 2, 1, 1, 4, 1, 2 },
      { 2, 1, 1, 2, 1, 4 }, { 2, 1, 1, 2, 3, 2 } };

  /** The stop bars.
   */
  static byte BARS_STOP[] = { 2, 3, 3, 1, 1, 1, 2 };
  /** The charset code change.
   */
  public static final char CODE_AB_TO_C = 99;
  /** The charset code change.
   */
  public static final char CODE_AC_TO_B = 100;
  /** The charset code change.
   */
  public static final char CODE_BC_TO_A = 101;
  /** The code for UCC/EAN-128.
   */
  public static final char FNC1 = 102;
  /** The start code.
   */
  public static final char START_A = 103;
  /** The start code.
   */
  public static final char START_B = 104;
  /** The start code.
   */
  public static final char START_C = 105;

  /** Creates new Barcode128 */
  public Barcode128() {
    x = 1;
    setFont(Font.getFont(false, 20));
    textAlignment = Barcode.ALIGN_CENTER;
    codeType = CODE128;
    quietZone = true;
    quietZoneX = 10;

  }

  /** Gets the maximum width that the barcode will occupy.
   *  The lower left corner is always (0, 0).
   * @return the size the barcode occupies.
   */
  @Override
  public int getBarcodeWidth() {

    String fullCode;
    if (codeType == CODE128_RAW) {
      int idx = code.indexOf('\uffff');
      if (idx >= 0) {
        fullCode = code.substring(0, idx);
      } else {
        fullCode = code;
      }
    } else {
      fullCode = getRawText(code, codeType == CODE128_UCC);
    }
    int len = fullCode.length();
    int fullWidth = (len + 2) * 11 * x + 2 * x;

    //int quietZone = 0;
    if (isQuietZone()) {
      //quietZone = Math.round(quietZoneX * x);
      fullWidth = fullWidth + (quietZoneX * 2 * x);
    }
    return fullWidth;
  }

  /** Generates the bars. The input has the actual barcodes, not
   * the human readable text.
   * @param text the barcode
   * @return the bars
   */
  public static byte[] getBarsCode128Raw(String text) {
    int idx = text.indexOf('\uffff');
    if (idx >= 0) {
      text = text.substring(0, idx);
    }
    int chk = text.charAt(0);
    for (int k = 1; k < text.length(); ++k) {
      chk += k * text.charAt(k);
    }
    chk = chk % 103;
    text += (char) chk;
    byte bars[] = new byte[(text.length() + 1) * 6 + 7];
    int k;
    for (k = 0; k < text.length(); ++k) {
      System.arraycopy(BARS[text.charAt(k)], 0, bars, k * 6, 6);
    }
    System.arraycopy(BARS_STOP, 0, bars, k * 6, 7);
    return bars;
  }

  /** Packs the digits for charset C. It assumes that all the parameters
   * are valid.
   * @param text the text to pack
   * @param textIndex where to pack from
   * @param numDigits the number of digits to pack. It is always an even number
   * @return the packed digits, two digits per character
   */
  static String getPackedRawDigits(String text, int textIndex, int numDigits) {
    String out = "";
    while (numDigits > 0) {
      numDigits -= 2;
      int c1 = text.charAt(textIndex++) - '0';
      int c2 = text.charAt(textIndex++) - '0';
      out += (char) (c1 * 10 + c2);
    }
    return out;
  }

  /** Converts the human readable text to the characters needed to
   * create a barcode. Some optimization is done to get the shortest code.
   * @param text the text to convert
   * @param ucc <CODE>true</CODE> if it is an UCC/EAN-128. In this case
   * the character FNC1 is added
   * @return the code ready to be fed to getBarsCode128Raw()
   */
  public static String getRawText(String text, boolean ucc) {
    String out = "";
    int tLen = text.length();
    if (tLen == 0) {
      out += START_B;
      if (ucc) {
        out += FNC1;
      }
      return out;
    }
    int c = 0;
    for (int k = 0; k < tLen; ++k) {
      c = text.charAt(k);
      if (c > 127) {
        throw new RuntimeException("There are illegal characters for barcode 128 in '" + text + "'.");
      }
    }
    c = text.charAt(0);
    char currentCode = START_B;
    int index = 0;
    if (isNextDigits(text, index, 2)) {
      currentCode = START_C;
      out += currentCode;
      if (ucc) {
        out += FNC1;
      }
      out += getPackedRawDigits(text, index, 2);
      index += 2;
    } else if (c < ' ') {
      currentCode = START_A;
      out += currentCode;
      if (ucc) {
        out += FNC1;
      }
      out += (char) (c + 64);
      ++index;
    } else {
      out += currentCode;
      if (ucc) {
        out += FNC1;
      }
      out += (char) (c - ' ');
      ++index;
    }
    while (index < tLen) {
      switch (currentCode) {
      case START_A: {
        if (isNextDigits(text, index, 4)) {
          currentCode = START_C;
          out += CODE_AB_TO_C;
          out += getPackedRawDigits(text, index, 4);
          index += 4;
        } else {
          c = text.charAt(index++);
          if (c > '_') {
            currentCode = START_B;
            out += CODE_AC_TO_B;
            out += (char) (c - ' ');
          } else if (c < ' ') {
            out += (char) (c + 64);
          } else {
            out += (char) (c - ' ');
          }
        }
      }
        break;
      case START_B: {
        if (isNextDigits(text, index, 4)) {
          currentCode = START_C;
          out += CODE_AB_TO_C;
          out += getPackedRawDigits(text, index, 4);
          index += 4;
        } else {
          c = text.charAt(index++);
          if (c < ' ') {
            currentCode = START_A;
            out += CODE_BC_TO_A;
            out += (char) (c + 64);
          } else {
            out += (char) (c - ' ');
          }
        }
      }
        break;
      case START_C: {
        if (isNextDigits(text, index, 2)) {
          out += getPackedRawDigits(text, index, 2);
          index += 2;
        } else {
          c = text.charAt(index++);
          if (c < ' ') {
            currentCode = START_A;
            out += CODE_BC_TO_A;
            out += (char) (c + 64);
          } else {
            currentCode = START_B;
            out += CODE_AC_TO_B;
            out += (char) (c - ' ');
          }
        }
      }
        break;
      }
    }
    return out;
  }

  /** Returns <CODE>true</CODE> if the next <CODE>numDigits</CODE>
   * starting from index <CODE>textIndex</CODE> are numeric.
   * @param text the text to check
   * @param textIndex where to check from
   * @param numDigits the number of digits to check
   * @return the check result
   */
  static boolean isNextDigits(String text, int textIndex, int numDigits) {
    if (textIndex + numDigits > text.length()) {
      return false;
    }
    while (numDigits-- > 0) {
      char c = text.charAt(textIndex++);
      if (c < '0' || c > '9') {
        return false;
      }
    }
    return true;
  }

  /** Places the barcode in a BufferedImage. The
   * The bars and text are written in the following colors:<p>
   * <P><TABLE BORDER=1>
   * <TR>
   *    <TH><P><CODE>barColor</CODE></TH>
   *    <TH><P><CODE>textColor</CODE></TH>
   *    <TH><P>Result</TH>
   *    </TR>
   * <TR>
   *    <TD><P><CODE>null</CODE></TD>
   *    <TD><P><CODE>null</CODE></TD>
   *    <TD><P>bars and text painted with current fill color</TD>
   *    </TR>
   * <TR>
   *    <TD><P><CODE>barColor</CODE></TD>
   *    <TD><P><CODE>null</CODE></TD>
   *    <TD><P>bars and text painted with <CODE>barColor</CODE></TD>
   *    </TR>
   * <TR>
   *    <TD><P><CODE>null</CODE></TD>
   *    <TD><P><CODE>textColor</CODE></TD>
   *    <TD><P>bars painted with current color<br>text painted with <CODE>textColor</CODE></TD>
   *    </TR>
   * <TR>
   *    <TD><P><CODE>barColor</CODE></TD>
   *    <TD><P><CODE>textColor</CODE></TD>
   *    <TD><P>bars painted with <CODE>barColor</CODE><br>text painted with <CODE>textColor</CODE></TD>
   *    </TR>
   * </TABLE>
   * @param i the <CODE>Image</CODE> where the barcode will be placed
   * @param barColor the color of the bars. It can be <CODE>-1</CODE>
   * @param textColor the color of the text. It can be <CODE>-1</CODE>
   */
  @Override
  public void placeBarcode(Image i, int barColor, int textColor) {
    String fullCode = code;
    Graphics g = i.getGraphics();
    int imageX = i.getWidth();
    int imageY = i.getHeight();
    int fontHeight = 0;

    if (codeType == CODE128_RAW) {
      int idx = code.indexOf('\uffff');
      if (idx < 0) {
        fullCode = "";
      } else {
        fullCode = code.substring(idx + 1);
      }
    }

    int fontX = 0;
    //int fontY = 0;
    if (isShowText() && getFont() != null) {
      Font f = getFont();
      g.setFont(f);
      fontHeight = f.size;
      fontX = f.fm.stringWidth(fullCode);
    }
    barHeight = imageY - fontHeight;

    String bCode;
    if (codeType == CODE128_RAW) {
      int idx = code.indexOf('\uffff');
      if (idx >= 0) {
        bCode = code.substring(0, idx);
      } else {
        bCode = code;
      }
    } else {
      bCode = getRawText(code, codeType == CODE128_UCC);
    }
    int len = bCode.length();
    int fullWidth = (len + 2) * 11 * x + 2 * x;
    int barStartX = 0;
    int textStartX = 0;
    switch (textAlignment) {
    case Barcode.ALIGN_LEFT:
      break;
    case Barcode.ALIGN_RIGHT:
      textStartX = imageX - fontX;
      break;
    default:
      textStartX = (imageX - fontX) / 2;
      break;
    }
    int barStartY = 0;
    int textStartY = 0;
    textStartY = imageY - fontHeight;
    byte bars[] = getBarsCode128Raw(bCode);
    boolean print = true;
    if (barColor != -1) {
      g.backColor = barColor;
    }
    int quietZone = 0;
    if (isQuietZone()) {
      fullWidth = fullWidth + (quietZoneX * 2 * x);
      quietZone = scale(imageX, fullWidth, quietZoneX * x);
    }
    for (int k = 0; k < bars.length; ++k) {
      int w = (bars[k] * x);
      if (print) {
        g.fillRect(quietZone + scale(imageX, fullWidth, barStartX), scale(imageY, barHeight, barStartY),
            scale(imageX, fullWidth, w), Math.round(barHeight));
      }

      print = !print;
      barStartX += w;
    }
    if (isShowText() && getFont() != null && textColor != -1) {
      g.foreColor = textColor;
      g.drawText(fullCode, textStartX, textStartY);
    }
  }
}
