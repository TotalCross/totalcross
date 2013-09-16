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
 * @see totalcross.sys.Settings#useNewFont
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

   /** The name of the file that has the old font set (prior to TotalCross 1.3). */
   public static final String OLD_FONT_SET = "TCFontOld";
   /** The name of the file that has the current font set (after TotalCross 1.3). */
   public static final String NEW_FONT_SET = "TCFont";
   
   /** The default font name: Font.NEW_FONT_SET if new font set is being used, Font.OLD_FONT_SET otherwise. 
    * If a specified font is not found, this one is used instead. 
    */
   public static final String DEFAULT = Settings.useNewFont ? NEW_FONT_SET : OLD_FONT_SET;
   /** The minimum font size: 7. */
   public static int MIN_FONT_SIZE = 7;
   /** The maximum font size: 44 for Palm OS, 60 for other platforms. */
   public static int MAX_FONT_SIZE = Settings.PALMOS.equals(Settings.platform) ? 44 : 60;


   /** Returns the default font size, based on the screen's size.
    */
   public static int getDefaultFontSize()
   {
      int fontSize = Settings.onJavaSE ? returnUserSize() : -1;
      if (fontSize != -1)
         return fontSize;

      // determine fonts as if we were in portrait mode
      int w,h;
      if (Settings.BLACKBERRY.equals(Settings.platform)) // blackberry devices are often landscape
      {
         w = Settings.screenWidth;
         h = Settings.screenHeight;
      }
      else
      {
         w = Math.min(Settings.screenWidth,Settings.screenHeight);
         h = Math.max(Settings.screenWidth,Settings.screenHeight);
      }

      if (Settings.WIN32.equals(Settings.platform) && Settings.windowFont == Settings.WINDOWFONT_DEFAULT)
         fontSize = Settings.deviceFontHeight;
      else
      if (Settings.isWindowsDevice()) // flsobral@tc126_49: with the exception of WindowsCE and WinMo, the font size is now based on the screen resolution for all platforms to better support small phones and tablets.
         fontSize = Settings.screenWidth >= 480 ? 28 : Settings.screenWidth >= 320 ? 18 : 12; // added this exception to get the right font when running in the WM phone in landscape mode
      else
      if (Settings.ANDROID.equals(Settings.platform)) // guich@tc126_69
         fontSize = 20 * Settings.deviceFontHeight / 14;
      else
      if (Settings.BLACKBERRY.equals(Settings.platform) && w >= 640)
         fontSize = 26; // storm 7.0 with 640x480         
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
      if (Settings.useNewFont && ((Settings.WIN32.equals(Settings.platform) && Settings.windowFont == Settings.WINDOWFONT_12) || Settings.deviceFontHeight == 0)) // keep font height of the new font the same as before on platforms that are not Android
      {
         byte[] new2oldInc = {1,1,1,2,2,2,2,2,3,3,3,3,3,4,4,4,4,4,5,5,5,5,5,6,6,6,6,6,7,7,7,7};
         fontSize += new2oldInc[fontSize-MIN_FONT_SIZE];
      }
      if (fontSize < MIN_FONT_SIZE)
         fontSize = MIN_FONT_SIZE;
      else
      if (fontSize > MAX_FONT_SIZE)
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
   private static StringBuffer sb = new StringBuffer(30);

   private Font(String name, boolean boldStyle, int size) // guich@580_10
   {
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
    * <code>Font.MIN_FONT_SIZE ... Font.MAX_FONT_SIZE</code>.
    */
   public static Font getFont(boolean boldStyle, int size) // guich@580_10
   {
      return getFont(DEFAULT, boldStyle, size);
   }

   /**
    * Gets the instance of a font of the given name, style and size. Font styles are defined
    * in this class. BlackBerry supports the use of native system fonts, which are formed by
    * the font family name preceded by a '$' (e.g.: "$BBCasual"). You can also specify only
    * "$" for the font name, which means the default system font. Font.DEFAULT will be used in
    * place of native fonts for all platforms that do not support them.
    * @param name Font.DEFAULT is the default font. You must install other fonts if you want to use them.
    * @param boldStyle If true, a bold font is used. Otherwise, a plain font is used.
    * @param size If you want a text bigger than the standard size, use Font.NORMAL_SIZE+x; or if you want
    * a text smaller than the standard size, use Font.NORMAL_SIZE-x. Size is adjusted to be in the range
    * <code>Font.MIN_FONT_SIZE ... Font.MAX_FONT_SIZE</code>.
    */
   public static Font getFont(String name, boolean boldStyle, int size) // guich@580_10
   {
      sb.setLength(0);
      String key = sb.append(name).append('$').append(boldStyle?'B':'P').append(size).toString();
      Font f = (Font)htFonts.get(key);
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
    * @since TotalCross 1.3
    */
   public Font adjustedBy(int delta)
   {
      return getFont(name,style == 1, size + delta);
   }

   /** Returns a font with the size changed with that delta and the given bold style. 
    * The new size is thisFont.size+delta.
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
      hv_UserFont = Launcher.instance.getFont(this, ' ');
   }
}
