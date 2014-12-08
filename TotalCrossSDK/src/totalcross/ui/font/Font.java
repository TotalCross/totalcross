/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/



package totalcross.ui.font;

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

public final class Font
{
   /** Read only field that contains the font's name. Note that changing this directly will have no effect. */
   public String name;
   /** Read only field that contains the font's style. Note that changing this directly will have no effect. For bold fonts, style == 1. */
   public int style;
   /** Read only field that contains the font's size. Note that changing this directly will have no effect. */
   public int size;
   // HOOK VARIABLES - can't be private - guich@350_20
   public Object hv_UserFont;
   public FontMetrics fm;

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
   public static int getDefaultFontSize()
   {
      int fontSize = Settings.onJavaSE ? returnUserSize() : -1;
      if (fontSize != -1)
         return fontSize;

      // determine fonts as if we were in portrait mode
      int w,h;
      w = Math.min(Settings.screenWidth,Settings.screenHeight);
      h = Math.max(Settings.screenWidth,Settings.screenHeight);

      if (Settings.isWindowsDevice()) // flsobral@tc126_49: with the exception of WindowsCE and WinMo, the font size is now based on the screen resolution for all platforms to better support small phones and tablets.
         fontSize = Settings.screenWidth >= 480 ? 28 : Settings.screenWidth >= 320 ? 18 : 14; // added this exception to get the right font when running in the WM phone in landscape mode
      else
      if (Settings.WINDOWSPHONE.equals(Settings.platform) || (Settings.WIN32.equals(Settings.platform) && Settings.windowFont == Settings.WINDOWFONT_DEFAULT))
         fontSize = Settings.deviceFontHeight;
      else
      if (Settings.ANDROID.equals(Settings.platform)) // guich@tc126_69
         fontSize = 20 * Settings.deviceFontHeight / 14;
      else
      if (Settings.isIOS() && Settings.deviceFontHeight != 0)
         fontSize = Settings.deviceFontHeight;
      else
         switch (w)
         {
            // some predefined device screen sizes
            case 480:
            case 360:
            case 320:
               if (h < 240)
                  fontSize = 13;
               else if(h == 240)
                  fontSize = 14;
               else
                  fontSize = 18;
               break;
            case 640:
            case 240:
            case 220:
            case 200:
               fontSize = 12;
               break;
            default :
               if (w >= 600 || h >= 800) // bigger font for tablets, final value will be 26 if the device is fingerTouch
                  fontSize = 23;
               else
                  fontSize = 9; // guich@tc123_13: pk doesn't like to have a size=20 for above 640
         }
      if (fontSize < MIN_FONT_SIZE)
         fontSize = MIN_FONT_SIZE;
      else
      if (!Settings.isOpenGL && fontSize > MAX_FONT_SIZE)
         fontSize = MAX_FONT_SIZE;
      
      return fontSize;
   }
   
   private static int returnUserSize() // guich@tc130: allow user to set the font size throught the Launcher
   {
      return Launcher.userFontSize;
   }

   /** A normal-sized font */
   public static final int NORMAL_SIZE = getDefaultFontSize();
   /** A big-sized font (2 above the normal size) */
   public static final int BIG_SIZE = NORMAL_SIZE+2;

   /** When the vm draws a character and founds the tab char, it will draw a set of spaces. 
    * You can define the number of spaces that will be drawn setting this field. 
    * It defaults to 3, but you can change at any time. 
    */
   public static int TAB_SIZE = 3;

   private static Hashtable htFonts = new Hashtable(13);

   private Font(String name, boolean boldStyle, int size) // guich@580_10
   {
      if (size == 0)
         throw new RuntimeException("Font size cannot be 0!");
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
      char st = boldStyle ? 'B' : 'P';
      String key = name+'$'+st+size;
      Font f = baseChar == ' ' ? (Font)htFonts.get(key) : null;
      if (f == null)
         htFonts.put(key, f = new Font(name, boldStyle, size));
      return f;
   }

   /** Returns this font as Bold */
   public Font asBold()
   {
      return getFont(name,true,size); // guich@450_36: cache the bolded font - guich@580_10: cached now in the Hashtable.
   }
   
   /** Returns a font with the size changed with that delta. 
    * The new size is thisFont.size+delta.
    * delta can be positive or negative. The new size won't pass the minimum nor the maximum sizes.
    * @since TotalCross 1.3
    */
   public Font adjustedBy(int delta)
   {
      return getFont(name,style == 1, size + delta);
   }

   /** Returns a font with the size changed with that delta and the given bold style. 
    * The new size is thisFont.size+delta.
    * delta can be positive or negative. The new size won't pass the minimum nor the maximum sizes.
    * @since TotalCross 1.3
    */
   public Font adjustedBy(int delta, boolean bold)
   {
      return getFont(name,bold, size + delta);
   }
   
   /** Returns if this font is bold.
    * @since TotalCross 1.53
    */
   public boolean isBold()
   {
      return style == 1;
   }

   ///// Native methods
   native void fontCreate4D();
   void fontCreate()
   {
      hv_UserFont = Launcher.instance.getFont(this, (char)baseChar);
   }
   
   /** Used internally. */
   public void removeFromCache()
   {
      char st = style==1 ? 'B' : 'P';
      String key = name+'$'+st+size;
      htFonts.remove(key);
   }
   public void removeFromCache4D()
   {
   }
}
