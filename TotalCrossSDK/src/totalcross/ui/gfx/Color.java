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



package totalcross.ui.gfx;

import totalcross.sys.*;
import totalcross.util.*;

/**
 * The Color class is just an utility class used to do operations in a color, which
 * is actually represented by an <code>int</code> in the format 0x00RRGGBB. 
 * <p>
 * A color is defined as a mixture of red, green and blue color values.
 * Each value is in the range of 0 to 255 where 0 is darkest and 255 is brightest.
 * For example, Color(255, 0, 0) is the color red.
 * <p>
 * Here are some more examples:
 * <ul>
 * <li>Color(0, 0, 0) is black
 * <li>Color(255, 255, 255) is white
 * <li>Color(255, 0, 0 ) is red
 * <li>Color(0, 255, 0) is green
 * <li>Color(0, 0, 255) is blue
 * </ul>
 */

public final class Color
{
   public static final int WHITE   = 0xFFFFFF;
   public static final int BRIGHT  = 0xBFBFBF;
   public static final int DARK    = 0x7F7F7F;
   public static final int BLACK   = 0x000000;
   public static final int RED     = 0xFF0000;
   public static final int PINK    = 0xFFAFAF;
   public static final int ORANGE  = 0xFFC800;
   public static final int YELLOW  = 0xFFFF00;
   public static final int GREEN   = 0x00FF00;
   public static final int MAGENTA = 0xFF00FF;
   public static final int CYAN    = 0x00FFFF;
   public static final int BLUE    = 0x0000FF;

   /** Increase or decrease all RGB values by 96. To be used in the darker(step) and brighter(step) methods. */
   public static final int FULL_STEP = 96;
   /** Increase or decrease all RGB values by 32. To be used in the darker(step) and brighter(step) methods. */
   public static final int LESS_STEP = 32;
   /** Increase or decrease all RGB values by 48. To be used in the darker(step) and brighter(step) methods. */
   public static final int HALF_STEP = 48;

   private static int lastDarkerSrc=-1,lastDarkerDst,lastBrighterSrc=-1,lastBrighterDst;

   /**
    * Returns a color integer value with the given red, green and blue components.
    * @param red the red value in the range of 0 to 255
    * @param green the green value in the range of 0 to 255
    * @param blue the blue value in the range of 0 to 255
    * @since SuperWaba 5.8
    */
   public static int getRGB(int red, int green, int blue) // guich@580_28
   {
      return (red << 16) | (green << 8) | blue;
   }

   /**
    * Returns a color integer value with the given red, green and blue components,
    * ensuring that the values are within the 0-255 range.
    * @param red the red value in any range
    * @param green the green value in any range
    * @param blue the blue value in any range
    * @since SuperWaba 5.8
    */
   public static int getRGBEnsureRange(int red, int green, int blue) // guich@580_28
   {
      if (red < 0) red = 0;
      else
      if (red > 255) red = 255;

      if (green < 0) green = 0;
      else
      if (green > 255) green = 255;

      if (blue < 0) blue = 0;
      else
      if (blue > 255) blue = 255;

      return (red << 16) | (green << 8) | blue;
   }

   /**
    * Returns a color integer value parsing the given rgb. Note that this method is rather slow.
    * @param rrggbb a string in the format RRGGBB (colors in hex).
    * @since SuperWaba 5.8
    */
   public static int getRGB(String rrggbb) // guich@580_28
   {
      try {return rrggbb == null ? -1 : (int)Convert.toLong(rrggbb,16);} catch (InvalidNumberException ine) {return -1;}
   }

   /** This class can't be instantiated. */
   private Color()
   {
   }

   /** Returns the alpha channel (brightness) of the color value in a value from 0 to 255 */
   public static int getAlpha(int rgb)
   {
      return ((((rgb >> 16) & 0xFF) << 5) + (((rgb >> 8) & 0xFF) << 6) + ((rgb & 0xFF) << 2)) / 100;
   }

   /** Returns the blue value of the color. */
   public static int getBlue(int rgb)
   {
      return rgb & 0xFF;
   }

   /** Returns the green value of the color. */
   public static int getGreen(int rgb)
   {
      return (rgb >> 8) & 0xFF;
   }

   /** Returns the red value of the color. */
   public static int getRed(int rgb)
   {
      return (rgb >> 16) & 0xFF;
   }

   /** Returns a color that is brighter than the current one. */
   public static int brighter(int rgb)
   {
      if (rgb == lastBrighterSrc)
         return lastBrighterDst;
      lastBrighterSrc = rgb;
      return lastBrighterDst = getRGBEnsureRange(((rgb >> 16) & 0xFF)+FULL_STEP,((rgb >> 8) & 0xFF)+FULL_STEP,(rgb & 0xFF)+FULL_STEP);
   }
   
   /** Return a color that is darker than the current one. */
   public static int darker(int rgb)
   {
      if (rgb == lastDarkerSrc)
         return lastDarkerDst;
      lastDarkerSrc = rgb;
      return lastDarkerDst = getRGBEnsureRange(((rgb >> 16) & 0xFF)-FULL_STEP,((rgb >> 8) & 0xFF)-FULL_STEP,(rgb & 0xFF)-FULL_STEP);
   }
   
   /** Returns the best cursor color depending on this color */
   public static int getCursorColor(int rgb)
   {
      return getAlpha(rgb) > 128
           ? getRGBEnsureRange(((rgb >> 16) & 0xFF)-FULL_STEP,((rgb >> 8) & 0xFF)-FULL_STEP,(rgb & 0xFF)-FULL_STEP)
           : getRGBEnsureRange(((rgb >> 16) & 0xFF)+FULL_STEP,((rgb >> 8) & 0xFF)+FULL_STEP,(rgb & 0xFF)+FULL_STEP); // guich@220_45 - guich@340_51: in color devices, use normal step
   }
   
   /** Returns a color that is brighter than the current one. */
   public static int brighter(int rgb, int step)
   {
      return getRGBEnsureRange(((rgb >> 16) & 0xFF)+step,((rgb >> 8) & 0xFF)+step,(rgb & 0xFF)+step);
   }
   
   /** Returns a color that is darker than the current one. */
   public static int darker(int rgb, int step)
   {
      return getRGBEnsureRange(((rgb >> 16) & 0xFF)-step,((rgb >> 8) & 0xFF)-step,(rgb & 0xFF)-step);
   }
   
   /** Returns the string representation of this color: the rgb in hexadecimal */
   public static String toString(int rgb)
   {
      return Convert.unsigned2hex(rgb,6); // never change this!
   }

   /** Interpolates the given colors.
    *  @since TotalCross 1.0 beta 4  
    */
   public static int interpolate(int color1, int color2)
   {
      int r1 = (color1 >> 16) & 0xFF;
      int g1 = (color1 >>  8) & 0xFF;
      int b1 = (color1      ) & 0xFF;
      int r2 = (color2 >> 16) & 0xFF;
      int g2 = (color2 >>  8) & 0xFF;
      int b2 = (color2      ) & 0xFF;
      int r = (r1+r2)/2;
      int g = (g1+g2)/2;
      int b = (b1+b2)/2;
      
      return (r << 16) | (g << 8) | b;
   }

   /** Interpolates the given colors by the given factor, ranging from 0 to 100.
    *  @since TotalCross 1.23
    */
   public static int interpolate(int color1, int color2, int factor)
   {
      int m = 100-factor;
      int r1 = (color1 >> 16) & 0xFF;
      int g1 = (color1 >>  8) & 0xFF;
      int b1 = (color1      ) & 0xFF;
      int r2 = (color2 >> 16) & 0xFF;
      int g2 = (color2 >>  8) & 0xFF;
      int b2 = (color2      ) & 0xFF;
      int r = (r1*factor+r2*m)/100;
      int g = (g1*factor+g2*m)/100;
      int b = (b1*factor+b2*m)/100;
      
      return (r << 16) | (g << 8) | b;
   }

   /** Interpolates the given colors by the given factor, ranging from 0 to 255.
    *  @since TotalCross 1.23
    */
   public static int interpolateA(int color1, int color2, int factor)
   {
      int m = 255-factor;
      int r1 = (color1 >> 16) & 0xFF;
      int g1 = (color1 >>  8) & 0xFF;
      int b1 = (color1      ) & 0xFF;
      int r2 = (color2 >> 16) & 0xFF;
      int g2 = (color2 >>  8) & 0xFF;
      int b2 = (color2      ) & 0xFF;
      int r = (r1*factor+r2*m)/255;
      int g = (g1*factor+g2*m)/255;
      int b = (b1*factor+b2*m)/255;
      
      return (r << 16) | (g << 8) | b;
   }

   /** Returns a color that better contrasts with the given original color.
    * @since TotalCross 1.23 
    */
   public static int getBetterContrast(int original, int color1, int color2) 
   {
      int a0 = getAlpha(original);
      int a1 = getAlpha(color1);
      int a2 = getAlpha(color2);
      return Math.abs(a1-a0) > Math.abs(a2-a0) ? color1 : color2;
   }

   /** Returns a random color.
    * Example:
    * <pre>
      Random r = new Random();
      for (int i = 0; i < tt.length; i++)
         tabPanel.getContainer(i).setBackColor(Color.getRandomColor(r));
    * </pre>
    * @since TotalCross 3.0
    */
   public static int getRandomColor(Random r)
   {
      return getRGBEnsureRange(r.nextInt(256),r.nextInt(256),r.nextInt(256));
   }
}