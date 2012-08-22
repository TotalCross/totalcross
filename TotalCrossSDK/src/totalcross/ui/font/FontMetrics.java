/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
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
import totalcross.Launcher.UserFont;

/**
 * FontMetrics used to obtain information about the widths and
 * heights of characters and strings when drawing text on a surface.
 * <p>
 * Here is an example that uses FontMetrics to get the width of a string:
 *
 * <pre>
 * ...
 * Font font = Font.getFont("Tiny", true, Font.BIG_SIZE);
 * FontMetrics fm = font.fm;
 * String s = "This is a line of text.";
 * int stringWidth = fm.stringWidth(s);
 * ...
 * </pre>
 */

public final class FontMetrics
{
   // Note! These fields are accessed directly from the VM!
   protected Font font;

   /** READ-ONLY member: indicates the average height of this font from the baseline to up. */
   public int ascent;

   /** READ-ONLY member: indicates the average height of this font from the baseline to down. */
   public int descent;

   /** READ-ONLY member: total height of this font (ascent+descent). */
   public int height;

   /**
   * Constructs a font metrics object referencing the given font.
   */
   FontMetrics(Font font)
   {
      this.font = font;
      fontMetricsCreate();
      this.height = ascent+descent;
   }
   void fontMetricsCreate()
   {
      if (Launcher.instance == null)
         throw new RuntimeException("\n\nThe class you specified to Java as the main class is wrong. The correct way of launching a TotalCross application is: \n\n   java -classpath <your classpath here> totalcross.Launcher <the class you specified here>\n");
      
      UserFont uf = (UserFont)font.hv_UserFont;
      if (uf != null)
      {
         this.ascent  = uf.ascent;
         this.descent = uf.descent;
      }
   }
   native void fontMetricsCreate4D();

   /**
   * Returns the width in pixels of the given character.
   */
   public int charWidth(char c)
   {
      return Launcher.instance.getCharWidth(this.font, c);
   }
   native public int charWidth4D(char c);

   /** Returns the width in pixels of the given text string. */
   public int stringWidth(String s)
   {
      int sum = 0;
      for (int i =0, n = s.length(); i < n; i++)
         sum += Launcher.instance.getCharWidth(this.font, s.charAt(i));
      return sum;
   }
   native public int stringWidth4D(String s);

   /**
   * Returns the width in pixels of the given char array range.
   * @param chars the text character array
   * @param start the start position in array
   * @param count the number of characters
   */
   public int stringWidth(char chars[], int start, int count)
   {
      int sum = 0;
      while (count-- > 0)
         sum += Launcher.instance.getCharWidth(this.font, chars[start++]);
      return sum;
   }
   native public int stringWidth4D(char chars[], int start, int count);

   /** Returns the maximum text width from the given list of names.
    * It is useful to compute the best x position to place the controls,
    * in order to align them in the container.
    * For example:
    * <pre>
    * String []labels = {"Name","Age","Address"};
    * int xx = this.fm.getMaxWidth(labels, 0, labels.length);
    * add(new Label(labels[0]), LEFT, AFTER);
    * add(edName = new Edit(""),xx, SAME);
    * ...
    * </pre>
    *
    * @param start The starting index
    * @param count The number of elements to check.
    * @since SuperWaba 5.72
    */
   public int getMaxWidth(String []names, int start, int count) // guich@572_17
   {
      int w = 0;
      while (count-- > 0)
      {
         int v = stringWidth(names[start++]);
         if (v > w)
            w = v;
      }
      return w;
   }

   /** Returns the width in pixels of the given StringBuffer. 
    * This method is used to preserve memory, since it avoids the creation 
    * of a String just to get the width.
    * @since TotalCross 1.0
    */
   public int sbWidth(StringBuffer s)
   {
      return sbWidth(s, 0, s.length());
   }
   native public int sbWidth4D(StringBuffer s);

   /** Returns the width in pixels of the given StringBuffer range. 
    * This method is used to preserve memory, since it avoids the creation 
    * of a String just to get the width.
    * @since TotalCross 1.0
    */
   public int sbWidth(StringBuffer s, int start, int count)
   {
      int w = 0;
      while (count-- > 0)
         w += charWidth(s.charAt(start++));
      return w;
   }
   native public int sbWidth4D(StringBuffer s, int start, int count);

   /** Returns the width in pixels of the char located at the given index in the StringBuffer. 
    * @since TotalCross 1.0
    */
   public int charWidth(StringBuffer s, int i)
   {
      return charWidth(s.charAt(i));
   }
   native public int charWidth4D(StringBuffer s, int i);
}