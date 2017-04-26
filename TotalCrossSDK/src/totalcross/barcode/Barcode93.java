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

import totalcross.ui.font.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

/** Implements the code 93. The default parameters are:
 * <pre>
 * x = 1f;
 * n = 2;
 * font = new Font("Helvetica",Font.PLAIN,20));
 * textAlignment = ALIGN_CENTER;
 * generateChecksum = false;
 * checksumText = false;
 * startStopText = true;
 * extended = false;
 * codeType = CODE93;
 * transparent = true;
 * shotText = true
 * generateChecksum = true;
 * checksumText = false;
 * </pre>
 *
 * This class is based on iText "A Free Java-PDF library by Bruno Lowagie and Paulo Soares" using the Mozilla Public License(MPL)
 *
 * @author Andres Ederra
 */
public class Barcode93 extends Barcode {
	/** The Start bar
	*/
	static byte START_BAR[] = { 1, 1, 1, 1, 4, 1 };
	/** The Stop bar
	*/
	static byte STOP_BAR[] = { 1, 1, 1, 1, 4, 1, 1 };

	/**
	* Bars to represent escape chars
	* index 0 = First escape char used in ASCII_CHARS array
	* index 1 = Second escape char used in ASCII_CHARS array
	* index 2 = Thirth escape char used in ASCII_CHARS array
	* index 3 = Fourth escape char used in ASCII_CHARS array
	*/
	static byte ESCAPE_BARS[][] = { { 1, 2, 1, 2, 1, 1 }, {
			3, 1, 2, 1, 1, 1 }, {
			3, 1, 1, 1, 2, 1 }, {
			1, 2, 2, 2, 1, 1 }
	};

	/** The bars to generate the code.
	 */
	static byte BARS[][] = { { 1, 3, 1, 1, 1, 2 }, {
			1, 1, 1, 2, 1, 3 }, {
			1, 1, 1, 3, 1, 2 }, {
			1, 1, 1, 4, 1, 1 }, {
			1, 2, 1, 1, 1, 3 }, {
			1, 2, 1, 2, 1, 2 }, {
			1, 2, 1, 3, 1, 1 }, {
			1, 1, 1, 1, 1, 4 }, {
			1, 3, 1, 2, 1, 1 }, {
			1, 4, 1, 1, 1, 1 }, {
			2, 1, 1, 1, 1, 3 }, {
			2, 1, 1, 2, 1, 2 }, {
			2, 1, 1, 3, 1, 1 }, {
			2, 2, 1, 1, 1, 2 }, {
			2, 2, 1, 2, 1, 1 }, {
			2, 3, 1, 1, 1, 1 }, {
			1, 1, 2, 1, 1, 3 }, {
			1, 1, 2, 2, 1, 2 }, {
			1, 1, 2, 3, 1, 1 }, {
			1, 2, 2, 1, 1, 2 }, {
			1, 3, 2, 1, 1, 1 }, {
			1, 1, 1, 1, 2, 3 }, {
			1, 1, 1, 2, 2, 2 }, {
			1, 1, 1, 3, 2, 1 }, {
			1, 2, 1, 1, 2, 2 }, {
			1, 3, 1, 1, 2, 1 }, {
			2, 1, 2, 1, 1, 2 }, {
			2, 1, 2, 2, 1, 1 }, {
			2, 1, 1, 1, 2, 2 }, {
			2, 1, 1, 2, 2, 1 }, {
			2, 2, 1, 1, 2, 1 }, {
			2, 2, 2, 1, 1, 1 }, {
			1, 1, 2, 1, 2, 2 }, {
			1, 1, 2, 2, 2, 1 }, {
			1, 2, 2, 1, 2, 1 }, {
			1, 2, 3, 1, 1, 1 }, {
			1, 2, 1, 1, 3, 1 }, {
			3, 1, 1, 1, 1, 2 }, {
			3, 1, 1, 2, 1, 1 }, {
			3, 2, 1, 1, 1, 1 }, {
			1, 1, 2, 1, 3, 1 }, {
			1, 1, 3, 1, 2, 1 }, {
			2, 1, 1, 1, 3, 1 }, {
			1, 2, 1, 2, 1, 1 }, {
			3, 1, 2, 1, 1, 1 }, {
			3, 1, 1, 1, 2, 1 }, {
			1, 2, 2, 2, 1, 1 }
	};

	/**
	* Code 93 chars ordered by code93 value
	*/
	public static String CHARS =
		"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. $/+%*&#@";

	/** ASCII chars representation in Code 93 using escape chars.
	 * First digit is escape char using this convention:
	 * escape ' ' = No escape char
	 * escape '*' = First Escape char; Barcode93 value 43
	 * escape '&' = Second Escape char; Barcode93 value 44
	 * escape '#' = Thirth Escape char; Barcode93 value 45
	 * escape '@' = Fourth Escape char; Barcode93 value 46
	 *
	 * The second digit is the code 93 char to use
	 */
	public static String ASCII_CHARS = "&U" +

		//NUL
		"*A" + //SOH
		"*B" + //STX
		"*C" + //ETX
		"*D" + //EOT
		"*E" + //ENQ
		"*F" + //ACK
		"*G" + //BEL
		"*H" + //BS
		"*I" + //TAB
		"*J" + //LF
		"*K" + //VT
		"*L" + //FF
		"*M" + //CR
		"*N" + //SO
		"*O" + //SI
		"*P" + //DLE
		"*Q" + //DC1
		"*R" + //DC2
		"*S" + //DC3
		"*T" + //DC4
		"*U" + //NAK
		"*V" + //SYN
		"*W" + //ETB
		"*X" + //CAN
		"*Y" + //EM
		"*Z" + //SUB
		"&A" + //ESC
		"&B" + //FS
		"&C" + //GS
		"&D" + //RS
		"&E" + //US
		"  " + //Space
		"#A" + //!
		"#B" + //"
		"#C" + //#
		" $" +
	//$ 	    It's also valid to use "#D" but it uses an extra char
		" %" + //%		It's also valid to use "#E" but it uses an extra char
		"#F" + //&
		"#G" + //'
		"#H" + //(
		"#I" + //)
		"#J" + //*
		" +" + //+		It's also valid to use "#K" but it uses an extra char
		"#L" + //
		" -" + //-		It's also valid to use "#M" but it uses an extra char
		" ." + //.		It's also valid to use "#N" but it uses an extra char
		" /" + ///		It's also valid to use "#O" but it uses an extra char
		" 0" + //0		It's also valid to use "#P" but it uses an extra char
		" 1" + //1		It's also valid to use "#Q" but it uses an extra char
		" 2" + //2		It's also valid to use "#R" but it uses an extra char
		" 3" + //3		It's also valid to use "#S" but it uses an extra char
		" 4" + //4		It's also valid to use "#T" but it uses an extra char
		" 5" + //5		It's also valid to use "#U" but it uses an extra char
		" 6" + //6		It's also valid to use "#V" but it uses an extra char
		" 7" + //7		It's also valid to use "#W" but it uses an extra char
		" 8" + //8		It's also valid to use "#X" but it uses an extra char
		" 9" + //9		It's also valid to use "#Y" but it uses an extra char
		"#Z" + //:
		"&F" + //;
		"&G" + //<
		"&H" + //;
		"&I" + //;
		"&J" + //;
		"&V" + //@
		" A" + //A
		" B" + //B
		" C" + //C
		" D" + //D
		" E" + //E
		" F" + //F
		" G" + //G
		" H" + //H
		" I" + //I
		" J" + //J
		" K" + //K
		" L" + //L
		" M" + //M
		" N" + //N
		" O" + //O
		" P" + //P
		" Q" + //Q
		" R" + //R
		" S" + //S
		" T" + //T
		" U" + //U
		" V" + //V
		" W" + //W
		" X" + //X
		" Y" + //Y
		" Z" + //Z
		"&K" + //[
		"&L" + //\
		"&M" + //]
		"&N" + //^
		"&O" + //_
		"&W" + //`
		"@A" + //a
		"@B" + //b
		"@C" + //c
		"@D" + //d
		"@E" + //e
		"@F" + //f
		"@G" + //g
		"@H" + //h
		"@I" + //i
		"@J" + //j
		"@K" + //k
		"@L" + //l
		"@M" + //m
		"@N" + //n
		"@O" + //o
		"@P" + //p
		"@Q" + //q
		"@R" + //r
		"@S" + //s
		"@T" + //t
		"@U" + //u
		"@V" + //v
		"@W" + //w
		"@X" + //x
		"@Y" + //y
		"@Z" + //z
		"&P" + //{
		"&Q" + // 
		"&R" + //}
		"&S" + //~
		"&T" //DEL
	;

	/** Creates a new Barcode39.
	 */
	public Barcode93() {
		//Wide to Narrow bar ratio is defined as follows in code93 standard:
		//Between 2:1 to 3:1 for sizes smaller than 0.020 inches
		//Between 2.2:1 to 3:1 for sizes larger than 0.020 inches
		//As we are working in a discrete environment, we can only use 2:1 or 3:1 ratio
		//We choose 3:1 ratio because it is valid for all sizes, and it is less error prone when doing image scalation operations        setFont(new Font("Helvetica", Font.PLAIN, 20));
	   x = 1;
		n = 3;
		setFont(Font.getFont(false, 20));
		textAlignment = Barcode.ALIGN_CENTER;
		generateChecksum = true;
		checksumText = false;

	}
	/** Gets the maximum width that the barcode will occupy.
	 *  The lower left corner is always (0, 0).
	 * @return the size the barcode occupies.
	 */
	public int getBarcodeWidth() {
		String bCode = getCode93ASCII(code);
		if (generateChecksum)
			bCode = getChecksum(bCode);

		int len = bCode.length() + 2;

		int fullWidth = (len * 9 * x) + x;

		//int quietZone = 0;
		if (isQuietZone()) {
			//quietZone = Math.round(quietZoneX * x);
			fullWidth = fullWidth + (quietZoneX * 2 * x);
		}
		return fullWidth;
	}
	/** Creates the bars.
	 * @param text the text to create the bars. This text does not include the start and
	 * stop characters
	 * @return the bars
	 */
	static byte[] getBarsCode93(String text) {
		byte bars[] = new byte[((text.length() + 2) * 6) + 1];
		System.arraycopy(START_BAR, 0, bars, 0, 6);
		int k;
		for (k = 0; k < text.length(); ++k) {
			int idx = CHARS.indexOf(text.charAt(k));
			if (idx < 0)
				throw new IllegalArgumentException(
					"The character '"
						+ text.charAt(k)
						+ "' is illegal in code 93.");
			System.arraycopy(BARS[idx], 0, bars, (k * 6) + 6, 6);
		}
		System.arraycopy(STOP_BAR, 0, bars, (text.length() * 6) + 6, 7);

		return bars;
	}
	/** Converts the ASCII text into a normal, escaped text,
	 * ready to generate bars.
	 * @param text the extended text
	 * @return the escaped text
	 */
	static String getCode93ASCII(String text) {
		String out = "";
		for (int k = 0; k < text.length(); ++k) {
			char c = text.charAt(k);
			if (c > 127 || c < 0) {
				throw new IllegalArgumentException(
					"The character " + c + " is not supported in Code 93");
			}
			char c1 = ASCII_CHARS.charAt(c * 2);
			char c2 = ASCII_CHARS.charAt(c * 2 + 1);
			if (c1 != ' ')
				out += c1;
			out += c2;
		}
		return out;
	}
	/** Calculates the checksum.
	 * @param text the text
	 * @return the checksum
	 */
	public static String getChecksum(String text) {
		String checksum = text;
		char chc = 0;
		char chk = 0;

		//Calculate C check char
		int weight = 0;
		int sum = 0;
		for (int k = checksum.length() - 1; k >= 0; k--) {
			int idx = CHARS.indexOf(checksum.charAt(k));
			if (idx < 0) {

				throw new IllegalArgumentException(
					"The character '"
						+ checksum.charAt(k)
						+ "' is illegal in code 93.");

			}
			sum += (idx) * (weight + 1);
			weight = (weight + 1) % 20;
		}
		chc = CHARS.charAt(sum % 47);

		checksum = checksum + chc;

		//Calculate K check char
		weight = 0;
		sum = 0;
		for (int k = checksum.length() - 1; k >= 0; k--) {
			int idx = CHARS.indexOf(checksum.charAt(k));
			if (idx < 0) {

				throw new IllegalArgumentException(
					"The character '"
						+ checksum.charAt(k)
						+ "' is illegal in code 93.");

			}
			sum += (idx) * (weight + 1);
			weight = (weight + 1) % 15;
		}
		chk = CHARS.charAt(sum % 47);

		checksum = checksum + chk;

		return checksum;
	}
	/** Places the barcode in a Image. The
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
	public void placeBarcode(
		Image i,
		int barColor,
		int textColor) {
		String fullCode = code;
		int barStartX = 0;
		int barStartY = 0;
		Graphics g = i.getGraphics();
		int imageX = i.getWidth();
		int imageY = i.getHeight();

		String bCode = getCode93ASCII(code);

		int fontX = 0;
		//int fontY = 0;
		int fontHeight = 0;
		if (isShowText() && getFont() != null) {
         Font f = getFont();
         g.setFont(f);
         fontHeight = f.size;
         fontX = f.fm.stringWidth(fullCode);
		}
		barHeight = imageY - fontHeight;

		if (generateChecksum)
			bCode = getChecksum(bCode);
		int len = bCode.length() + 2;

		int fullWidth = (len * 9 * x) + x;

		int textStartX = 0;
		switch (textAlignment) {
			case Barcode.ALIGN_LEFT :
				break;
			case Barcode.ALIGN_RIGHT :
				textStartX = imageX - fontX;
				break;
			default :
				textStartX = (imageX - fontX) / 2;
				break;
		}
		int textStartY = 0;
		textStartY = imageY - fontHeight;

		int quietZone = 0;
		if (isQuietZone()) {
			fullWidth = fullWidth + (quietZoneX * 2 * x);
			quietZone = scale(imageX, fullWidth, quietZoneX * x);
		}

		byte bars[] = getBarsCode93(bCode);
		boolean print = true;
		if (barColor != -1)
			g.backColor = barColor;

		for (int k = 0; k < bars.length; ++k) {
			int w = (bars[k] * x);
			if (print) {

				g.fillRect(
						quietZone + scale(imageX, fullWidth, barStartX),
						scale(imageY, barHeight, barStartY),
						scale(imageX, fullWidth, w),
						Math.round(barHeight));
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
