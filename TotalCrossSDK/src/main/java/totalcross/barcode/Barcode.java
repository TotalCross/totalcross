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

/** Base class containing properties and methods commom to all barcode types.
 *
 * This class is based on iText "A Free Java-PDF library by Bruno Lowagie and Paulo Soares" using the Mozilla Public License(MPL)
 *
  @author Andres Ederra, Paulo Soares 
 */
public abstract class Barcode {
	/** A type of barcode */
	public static final int EAN13 = 1;
	/** A type of barcode */
	public static final int EAN8 = 2;
	/** A type of barcode */
	public static final int UPCA = 3;
	/** A type of barcode */
	public static final int UPCE = 4;
	/** A type of barcode */
	public static final int SUPP2 = 5;
	/** A type of barcode */
	public static final int SUPP5 = 6;
	/** A type of barcode */
	public static final int POSTNET = 7;
	/** A type of barcode */
	public static final int PLANET = 8;
	/** A type of barcode */
	public static final int CODE128 = 9;
	/** A type of barcode */
	public static final int CODE128_UCC = 10;
	/** A type of barcode */
	public static final int CODABAR = 11;
	/** A type of barcode */
	public static final int CODE93 = 12;
	/** A type of barcode */
	public static final int CODE128_RAW = 13;
	
	/** The minimum bar width.
	 */
	protected int x;

	/** The bar multiplier for wide bars or the distance between
	 * bars for Postnet and Planet.
	 */
	protected int n;

	/** The size of the text or the height of the shorter bar
	 * in Postnet.
	 */
	protected int size;

	/** The height of the bars.
	 */
	protected int barHeight;

	/** The text alignment. Can be <CODE>CoderBar.ALIGN_LEFT</CODE>,
	 * <CODE>Codebar.ALIGN_CENTER</CODE> or <CODE>Codebar.ALIGN_RIGHT</CODE>.
	 */
	protected int textAlignment;

	/** The optional checksum generation.
	 */
	protected boolean generateChecksum;

	/** Shows the generated checksum in the the text.
	 */
	protected boolean checksumText;

	/** Show the start and stop character '*' in the text for
	 * the barcode 39 or 'ABCD' for codabar.
	 */
	protected boolean startStopText;

	/** Generates extended barcode 39.
	 */
	protected boolean extended;

	/** The code to generate.
	 */
	protected String code = "";

	/** Show the guard bars for barcode EAN.
	 */
	protected boolean guardBars;

	/** The code type.
	 */
	protected int codeType;

	/**Left alignment*/
	public final static int ALIGN_LEFT = 0;
	;
	/**Rigth alignment*/
	public final static int ALIGN_RIGHT = 1;
	/**Center alignment*/
	public final static int ALIGN_CENTER = 2;
	/**
	* The option to generate a starting and ending quiet zone
	**/
	protected boolean quietZone;
	/**
	*	The font to use in the text of the codebar
	**/
	protected Font font;
	/**
	* The width of the quiet zone expressed in multiples of x(the minimun width of the bar)
	**/
	protected int quietZoneX = 10;
	/**The property to show the text for barcode.*/
	protected boolean showText = true;
	protected boolean transparent;
	protected int backgroundColor = Color.WHITE;
	public final static String TYPE_CODE128 = "CODE128";
	public final static String TYPE_CODE93 = "CODE93";
	public final static String TYPE_INTERLEAVED2OF5 = "INTERLEAVED2OF5";
	;
	/**
	 * Creates a image representing a codebar
	 * using a codebar type and the codebar code
	 * @param codeType String Codebar type(Barcode.TYPE_CODE128 or Barcode.TYPE_CODE93 or Barcode.INTERLEAVED2OF5)
	 * @param codeValue String The code to represent
	 * @param height the Image's height
	 * @throws ImageException 
	 */
	public static synchronized Image createImage(
		String codeType,
		String codeValue, int height) throws ImageException {

		Barcode barcode = null;
		if (codeType.equalsIgnoreCase(Barcode.TYPE_CODE128)) {
			barcode = new Barcode128();
		} else if (codeType.equalsIgnoreCase(Barcode.TYPE_CODE93)) {
			barcode = new Barcode93();
		} else if (codeType.equalsIgnoreCase(Barcode.TYPE_INTERLEAVED2OF5)) {
			barcode = new BarcodeInter25();
		} else {
			return null;
		}

		barcode.setCode(codeValue);
		barcode.setTransparent(false);

		Image image = new Image((int) barcode.getBarcodeWidth(), height);
		Graphics g = image.getGraphics();
		g.backColor = Color.WHITE;
		g.fillRect(0, 0, (int) barcode.getBarcodeWidth(), height);
		barcode.setShowText(true);
		barcode.placeBarcode(image, Color.BLACK, Color.BLACK);
		return image;
	}
	/**
	 * Creates a image representing a codebar
	 * using a codebar type and the codebar code
	 * @param codeType String Codebar type(Barcode.TYPE_CODE128 or Barcode.TYPE_CODE93 or Barcode.INTERLEAVED2OF5)
	 * @param codeValue String The code to represent
	 * @param barHeight int The bar height in pixels
	 * @param fontName String The name of the font to use
	 * @param fontSize int The size of the font to use
	 * @param transparent boolean Use transparent background
	 * @param alignment int Text alignment :use Barcode.ALIGN_CENTER, Barcode.ALIGN_LEFT, Barcode.ALIGN_RIGHT
	 * @param fontBold The style of the font to use
	 * @param newBarColor Color Color to use in the bars
	 * @param newTextColor Color Color to use in the text
	 * @throws Exception 
	 */
	public static synchronized Image createImage(
		String codeType,
		String codeValue,
		int barHeight,
		String fontName,
		int fontSize,
		boolean transparent,
		int alignment,
		boolean fontBold,
		int newBarColor,
		int newTextColor, int x, int n) throws Exception {

		Barcode barcode = null;
		if (codeType.equalsIgnoreCase(Barcode.TYPE_CODE128)) {
			barcode = new Barcode128();
		} else if (codeType.equalsIgnoreCase(Barcode.TYPE_CODE93)) {
			barcode = new Barcode93();
		} else if (codeType.equalsIgnoreCase(Barcode.TYPE_INTERLEAVED2OF5)) {
			barcode = new BarcodeInter25();
		} else {
			return null;
		}

		if (x > 0) barcode.setX(x);
		if (n > 0) barcode.setN(n);
		barcode.setBarHeight(barHeight);
		barcode.setFontName(fontName);
		barcode.setFontSize(fontSize);
		barcode.setCode(codeValue);
		barcode.setTransparent(transparent);
		barcode.setTextAlignment(alignment);
		barcode.setFontStyle(fontBold);

		Image image = new Image((int) barcode.getBarcodeWidth(),	(int) barcode.getBarHeight());
		Graphics g = image.getGraphics();
		g.backColor = Color.WHITE;
		g.fillRect(0, 0, (int) barcode.getBarcodeWidth(), 100);

		barcode.placeBarcode(image, newBarColor, newTextColor);
		return image;
	}

	/**
	 * Gets the background color to use
	 * @return Color
	 */
	public int getBackgroundColor() {
		return backgroundColor;
	}
	/** Gets the maximum width that the barcode will occupy.
	 *  The lower left corner is always (0, 0).
	 * @return the size the barcode occupies.
	 */
	protected abstract int getBarcodeWidth();
	/** Gets the height of the bars.
	 * @return the height of the bars
	 */
	public int getBarHeight() {
		return barHeight;
	}
	/** Gets the code to generate.
	 * @return the code to generate
	 */
	public String getCode() {
		return code;
	}
	/** Gets the code type.
	 * @return the code type
	 */
	public int getCodeType() {
		return codeType;
	}
	/**
	 * Gets the font to use in the text of the barcode
	 * @return Font
	 */
	public Font getFont() {
		return font;
	}
	/**
	 * Gets name of the font to use in the text of the barcode
	 	 * Return null if no text is displayed
	 * @return int
	 */
	public String getFontName() {
		if (font == null)
			return null;
		else
			return font.name;
	}
	/**
	 * Gets size of the font to use in the text of the barcode
		 * Return -1 if no text is displayed
	 * @return int
	 */
	public int getFontSize() {
		if (font == null)
			return -1;
		else
			return font.size;
	}
	/** Gets the bar multiplier for wide bars.
	 * @return the bar multiplier for wide bars
	 */
	public int getN() {
		return n;
	}
	/**
	 * Get the width of the quiet zone
	 * @return int
	 */
	public int getQuietZoneX() {
		return quietZoneX;
	}
	/** Gets the size of the text.
	 * @return the size of the text
	 */
	public int getSize() {
		return size;
	}
	/** Gets the text alignment. Can be <CODE>Barcode.ALIGN_LEFT</CODE>,
	 * <CODE>Barcode.ALIGN_CENTER</CODE> or <CODE>Barcode.ALIGN_RIGHT</CODE>.
	 * @return the text alignment
	 */
	public int getTextAlignment() {
		return textAlignment;
	}
	/** Gets the minimum bar width.
	 * @return the minimum bar width
	 */
	public int getX() {
		return x;
	}
	/** Gets the property to show the generated checksum in the the text.
	 * @return value of property checksumText
	 */
	public boolean isChecksumText() {
		return checksumText;
	}
	/** Gets the property to generate extended barcode 39.
	 * @return value of property extended.
	 */
	public boolean isExtended() {
		return extended;
	}
	/** Gets the optional checksum generation.
	 * @return the optional checksum generation
	 */
	public boolean isGenerateChecksum() {
		return generateChecksum;
	}
	/** Gets the property to show the guard bars for barcode EAN.
	 * @return value of property guardBars
	 */
	public boolean isGuardBars() {
		return guardBars;
	}
	/**
	 * Gets the property to show the quiet zone at start and end of the bars.
	 * @return boolean
	 */
	public boolean isQuietZone() {
		return quietZone;
	}
	/**
	 * Gets the property to show the text for barcode.
	 * @return boolean
	 */
	public boolean isShowText() {
		return showText;
	}
	/** Sets the property to show the start and stop character '*' in the text for
	 * the barcode 39.
	 * @return value of property startStopText
	 */
	public boolean isStartStopText() {
		return startStopText;
	}
	/**
	 * Get the tranaparent background property
	 * @return boolean
	 */
	public boolean isTransparent() {
		return transparent;
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
	 * @param i the <CODE>BufferedImage</CODE> where the barcode will be placed
	 * @param barColor the color of the bars. It can be <CODE>null</CODE>
	 * @param textColor the color of the text. It can be <CODE>null</CODE>
	 */
	public abstract void placeBarcode(
		Image i,
		int barColor,
		int textColor);

	/**
	 * Scales a logical coordinate to fit a physical image coordinate
	 * Example: Having the logical dimension 200, and the logical coordinate 20.5 we want
	 * to scale the coordinate to a 300 pixel physical dimension.
	 * The call should be: int physicalCoordinate=scale(300,200,20.5);
	 * Fecha de creación: (26/02/2003 12:20:14)
	 * @return int
	 * @param imageSize The size of the image
	 * @param totalSize The logical size
	 * @param coordinate The logical coordinate
	 */
	public int scale(int imageSize, int totalSize, int coordinate) {
		return Math.round(imageSize * coordinate / totalSize);
	}
	/**
	 * Set the backgroud color od the barcode
	 * Default color is white
	 * @param newBackgroundColor Color
	 */
	public void setBackgroundColor(int newBackgroundColor) {
		backgroundColor = newBackgroundColor;
	}
	/** Sets the height of the bars.
	 * @param barHeight the height of the bars
	 */
	public void setBarHeight(int barHeight) {
		this.barHeight = barHeight;
	}
	/** Sets the code to generate.
	 * @param code the code to generate
	 */
	public void setCode(String code) {
		this.code = code;
	}
	/** Sets the code type.
	 * @param codeType the code type
	 */
	public void setCodeType(int codeType) {
		this.codeType = codeType;
	}
	/** Sets the property to show the generated checksum in the the text.
	 * @param checksumText new value of property checksumText
	 */
	public void setChecksumText(boolean checksumText) {
		this.checksumText = checksumText;
	}
	/** Sets the property to generate extended barcode 39.
	 * @param extended new value of property extended
	 */
	public void setExtended(boolean extended) {
		this.extended = extended;
	}
	/**
	 * Sets the font to use
	 * null font prints no text.
	 * default value is Helvetica,Plain, size 20
	 * @param newFont Font
	 */
	public void setFont(Font newFont) {
		font = newFont;
	}
	/**
	 * Modifies the font name
	 * @param newFontName int
	 */
	public void setFontName(String newFontName) {
		if (font != null)
			font = Font.getFont(newFontName, font.style==1, font.size);
	}
	/**
	* Modifies the font size
	  * @param newFontSize int
	 */
	public void setFontSize(int newFontSize) {
		if (font != null)
			font = Font.getFont(font.name, font.style == 1, newFontSize);
	}
	/**
	 * Modifies the font style
	 */
	public void setFontStyle(boolean bold) {
		if (font != null)
			font = Font.getFont(font.name, bold, font.size);
	}
	/** Setter for property generateChecksum.
	 * @param generateChecksum New value of property generateChecksum.
	 */
	public void setGenerateChecksum(boolean generateChecksum) {
		this.generateChecksum = generateChecksum;
	}
	/** Sets the property to show the guard bars for barcode EAN.
	 * @param guardBars new value of property guardBars
	 */
	public void setGuardBars(boolean guardBars) {
		this.guardBars = guardBars;
	}
	/** Sets the bar multiplier for wide bars.
	 * @param n the bar multiplier for wide bars
	 */
	public void setN(int n) {
		this.n = n;
	}
	/**
	 * Enables/Disables quiet zone usage
	 * @param newQuietZone boolean
	 */
	public void setQuietZone(boolean newQuietZone) {
		quietZone = newQuietZone;
	}
	/**
	 * Set the width of the quiet zone
	 * @param newQuietZoneX int
	 */
	public void setQuietZoneX(int newQuietZoneX) {
		quietZoneX = newQuietZoneX;
	}
	/**
	 * Sets the property to show the text for barcode.
	 * @param newShowText boolean
	 */
	public void setShowText(boolean newShowText) {
		showText = newShowText;
	}
	/** Sets the size of the text.
	 * @param size the size of the text
	 */
	public void setSize(int size) {
		this.size = size;
	}
	/** Gets the property to show the start and stop character '*' in the text for
	 * the barcode 39.
	 * @param startStopText new value of property startStopText
	 */
	public void setStartStopText(boolean startStopText) {
		this.startStopText = startStopText;
	}
	/** Sets the text alignment. Can be <CODE>Element.ALIGN_LEFT</CODE>,
	 * <CODE>Element.ALIGN_CENTER</CODE> or <CODE>Element.ALIGN_RIGHT</CODE>.
	 * @param textAlignment the text alignment
	 */
	public void setTextAlignment(int textAlignment) {
		this.textAlignment = textAlignment;
	}
	/**
	 * Sets the tranaparent background property
	 * Default tranparency is false
	 * @param newTransparent boolean
	 */
	public void setTransparent(boolean newTransparent) {
		transparent = newTransparent;
	}
	/** Sets the minimum bar width.
	 * @param x the minimum bar width
	 */
	public void setX(int x) {
		this.x = x;
	}
}
