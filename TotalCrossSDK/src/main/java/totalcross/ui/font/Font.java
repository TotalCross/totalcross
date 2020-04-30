// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>   
// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.font;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;
import totalcross.Launcher;
import totalcross.sys.Settings;
import totalcross.util.Hashtable;

/**
 * Font is the character font used when drawing text on a surface.
 * Fonts can be antialiased, and usually range from size 7 to 38.
 * In OpenGL platforms, they are 8bpp antialiased fonts ranging up indefinedly.
 * <ol>
 * <li> To see if the font you created is installed in the target device, query its name after
 * the creation. If the font is not found, its name is changed to match the default font.
 * <li> You may create new fonts based on the TrueType fonts using the tool tc.tools.FontGenerator.
 * <li> There are two font sets: the one before TotalCross 1.3, and the one after TotalCross 1.3. The font set is
 * choosen using Settings.useNewFont.
 * <li> The default font size is based in the device's DPI. This allows the font to have the same physical size
 * in inches on different devices.
 * <li> In JavaSE you can choose the default font size passing <code>/fontsize &lt;size&gt;</code> as argument, 
 * before the application's name.
 * </ol>
 */

public final class Font {
  /** Read only field that contains the font's name. Note that changing this directly will have no effect. */
  public String name;
  /** Read only field that contains the font's style. Note that changing this directly will have no effect. For bold fonts, style == 1. */
  public int style;
  /** Read only field that contains the font's size. Note that changing this directly will have no effect. */
  public int size;
  // HOOK VARIABLES - can't be private - guich@350_20
  public Object hv_UserFont;
  public FontMetrics fm;
  
  public int skiaIndex = -1;

  /** The default font name. If a specified font is not found, this one is used instead. 
   */
  public static final String DEFAULT = "TCFont";
  /** The minimum font size: 7. */
  public static int MIN_FONT_SIZE = 7;
  /** The maximum font size: 48 for Windows32, unlimited for OpenGL platforms (the number here will be 80,
   * since this is the size that the base font was created; you can specify something higher, but it will use
   * upscaling, which usually results in a smooth font). */
  public static int MAX_FONT_SIZE = Settings.WIN32.equals(Settings.platform) ? 48 : 80;

  /** For internal use only. */
  public static int baseChar = ' ';

  /** Returns the default font size, based on the screen's size.
   */
  public static int getDefaultFontSize() {
    int fontSize = Settings.onJavaSE ? returnUserSize() : -1;
    if (fontSize != -1) {
      return fontSize;
    }

    fontSize = 14;
    if (fontSize < MIN_FONT_SIZE) {
      fontSize = MIN_FONT_SIZE;
    } else if (!Settings.isOpenGL && fontSize > MAX_FONT_SIZE) {
      fontSize = MAX_FONT_SIZE;
    }

    return fontSize;
  }

  private static int returnUserSize() // guich@tc130: allow user to set the font size throught the Launcher
  {
    return Launcher.userFontSize;
  }

  /** A normal-sized font */
  public static final int NORMAL_SIZE = getDefaultFontSize();
  /** A big-sized font (2 above the normal size) */
  public static final int BIG_SIZE = NORMAL_SIZE + 2;

  /** When the vm draws a character and founds the tab char, it will draw a set of spaces. 
   * You can define the number of spaces that will be drawn setting this field. 
   * It defaults to 3, but you can change at any time. 
   */
  public static int TAB_SIZE = 3;

  private static Hashtable htFonts = new Hashtable(13);

  private Font(String name, boolean boldStyle, int size) // guich@580_10
  {
    if (size == 0) {
      throw new RuntimeException("Font size cannot be 0!");
    }
    this.name = name;
    this.style = boldStyle ? 1 : 0;
    this.size = size;
    fontCreate();
    fm = new FontMetrics(this); // guich@450_36: get out fontmetrics at once.
  }

  /**
   * Gets the instance of the default font, with the given style and size.
   * @param boldStyle If true, a bold font is used. Otherwise, a plain font is used.
   * @param size If you want a text bigger than the standard size, use Font.NORMAL_SIZE+x; or if you want
   * a text smaller than the standard size, use Font.NORMAL_SIZE-x. Size is adjusted to be in the range
   * <code>Font.MIN_FONT_SIZE ... Font.MAX_FONT_SIZE</code>. That is, passing a value out of the bounds 
   * won't throw an exception, will only use the minimum default size if the size passed is less than it or 
   * use the maximum default size if the size passed is greater than it.
   */
  public static Font getFont(boolean boldStyle, int size) // guich@580_10
  {
    return getFont(DEFAULT, boldStyle, size);
  }

  /**
   * Gets the instance of the default font, normal style and with the given size.
   * @param boldStyle If true, a bold font is used. Otherwise, a plain font is used.
   * @param size If you want a text bigger than the standard size, use Font.NORMAL_SIZE+x; or if you want
   * a text smaller than the standard size, use Font.NORMAL_SIZE-x. Size is adjusted to be in the range
   * <code>Font.MIN_FONT_SIZE ... Font.MAX_FONT_SIZE</code>. That is, passing a value out of the bounds 
   * won't throw an exception, will only use the minimum default size if the size passed is less than it or 
   * use the maximum default size if the size passed is greater than it.
   */
  public static Font getFont(int size) // guich@580_10
  {
    return getFont(DEFAULT, false, size);
  }

  /**
   * Gets the instance of a font of the given name, style and size. Font styles are defined
   * in this class. Font.DEFAULT will be used if the font is not installed on the device. 
   * This method can be used to check if the created font is in fact installed on the device.
   * @param name Font.DEFAULT is the default font. You must install other fonts if you want to use them.
   * @param boldStyle If true, a bold font is used. Otherwise, a plain font is used.
   * @param size If you want a text bigger than the standard size, use Font.NORMAL_SIZE+x; or if you want
   * a text smaller than the standard size, use Font.NORMAL_SIZE-x. Size is adjusted to be in the range
   * <code>Font.MIN_FONT_SIZE ... Font.MAX_FONT_SIZE</code>. That is, passing a value out of the bounds won't throw an exception, 
   * will only use the minimum default size if the size passed is less than it or use the maximum default size if the size passed is 
   * greater than it.
   */
  public static Font getFont(String name, boolean boldStyle, int size) // guich@580_10
  {
    return getFont(name, boldStyle, size, -1);
  }
  
  private static Font getFont(String name, boolean boldStyle, int size, int skiaIndex) // guich@580_10
  {
    char st = boldStyle ? 'B' : 'P';
    String key = name + '$' + st + size;
    Font f = baseChar == ' ' ? (Font) htFonts.get(key) : null;
    if (f == null) {
      htFonts.put(key, f = new Font(name, boldStyle, size));
    }
    
    if (f.skiaIndex == -1) {
    	f.skiaIndex = skiaIndex;
    }
    
    return f;
  }

  /** Returns this font as Bold */
  public Font asBold() {
    return getFont(name, true, size, skiaIndex); // guich@450_36: cache the bolded font - guich@580_10: cached now in the Hashtable.
  }

  /** Returns a font with the size changed with that delta. 
   * The new size is thisFont.size+delta.
   * delta can be positive or negative. The new size won't pass the minimum nor the maximum sizes.
   * @since TotalCross 1.3
   */
  public Font adjustedBy(int delta) {
    return getFont(name, style == 1, size + delta, skiaIndex);
  }

  /** Returns a font with the size changed with that delta and the given bold style. 
   * The new size is thisFont.size+delta.
   * delta can be positive or negative. The new size won't pass the minimum nor the maximum sizes.
   * @since TotalCross 1.3
   */
  public Font adjustedBy(int delta, boolean bold) {
    return getFont(name, bold, size + delta, skiaIndex);
  }

  /** Returns a font with the size changed with that percentage. 
   * The new size is thisFont.size * percent / 100.
   * delta can be positive or negative. The new size won't pass the minimum nor the maximum sizes.
   */
  public Font percentBy(int percent) {
    return getFont(name, style == 1, size * percent / 100, skiaIndex);
  }

  /** Returns a font with the size changed with that percentage and the given bold style. 
   * The new size is thisFont.size * percent / 100.
   * delta can be positive or negative. The new size won't pass the minimum nor the maximum sizes.
   */
  public Font percentBy(int percent, boolean bold) {
    return getFont(name, bold, size * percent / 100, skiaIndex);
  }

  /** Returns if this font is bold.
   * @since TotalCross 1.53
   */
  public boolean isBold() {
    return style == 1;
  }

  @ReplacedByNativeOnDeploy
  void fontCreate() {
    hv_UserFont = Launcher.instance.getFont(this, (char) baseChar);
  }

  /** Used internally. */
  public void removeFromCache() {
    char st = style == 1 ? 'B' : 'P';
    String key = name + '$' + st + size;
    htFonts.remove(key);
  }

  public void removeFromCache4D() {
  }

  @Override
  public String toString() {
    return name + "$" + (style == 1 ? "B" : "N") + size;
  }
}
