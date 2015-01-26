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



package totalcross.ui.gfx;

import totalcross.util.*;

/** This class is the one really used in the device.
  * Don't use anything from this class; use from the Graphics class instead.
  */

public final class Graphics4D
{
   // ORDER MUST NOT BE CHANGED!
   // instance ints
   public int foreColor;
   public int backColor;
   public boolean useAA;
   protected int width,height;
   protected int transX, transY;
   protected int clipX1,clipY1,clipX2,clipY2; // clip1 <= k < clip2
   protected int minX, minY, maxX, maxY;
   protected int lastRX,lastRY,lastXC,lastYC,lastSize; // used by arcPiePointDrawAndFill
   protected int pitch;
   private int alpha;
   public boolean isVerticalText;
   private boolean isControlSurface;
   // instance doubles
   protected double lastPPD; // used by arcPiePointDrawAndFill
   // instance objects
   protected GfxSurface surface;
   protected Object font;
   protected int xPoints[], yPoints[]; // used by arcPiePointDrawAndFill
   protected int[]ints; // used by fillPolygon
   // static objects
   public static boolean needsUpdate; // IMPORTANT: NOT USED IN DEVICE
   private static int[] pal685;
   static int[] mainWindowPixels; // create the pixels
   private static int[]acos,asin;

   static public final byte R3D_EDIT=1;
   static public final byte R3D_LOWERED=2;
   static public final byte R3D_RAISED=3;
   static public final byte R3D_CHECK=4;
   static public final byte R3D_SHADED=5;
   static public final byte ARROW_UP = 1;
   static public final byte ARROW_DOWN = 2;
   static public final byte ARROW_LEFT = 3;
   static public final byte ARROW_RIGHT = 4;

   public Graphics4D(GfxSurface surface)
   {
      this.surface = surface;
      if (surface instanceof totalcross.ui.image.Image)
         alpha = 0xFF000000;
      else
         isControlSurface = true;
      create(surface);
   }

   public boolean isControlSurface()
   {
      return isControlSurface;
   }

   public static int[] getPalette()
   {
      if (pal685 == null)
      {
         pal685 = new int[256];
         int ii=0,rr,gg,bb;
         int R = 6, G = 8, B = 5;
         for (int k =0; k <= 15; k++)
            pal685[ii++] = k*0x111111;
         for (int r = 1; r <= R; r++)
         {
            rr = Math.min(r * 256/R,255);
            for (int g = 1; g <= G; g++)
            {
               gg = Math.min(g * 256/G,255);
               for (int b = 1; b <= B; b++)
               {
                  bb = Math.min(b * 256/B,255);
                  pal685[ii++] = (rr << 16) | (gg << 8) | bb;
               }
            }
         }
      }
      return pal685;
   }
   public totalcross.ui.gfx.Coord getTranslation()
   {
      return new Coord(transX, transY);
   }
   public void setFont(totalcross.ui.font.Font font)
   {
      this.font = font;
   }
   public void translate(int dx, int dy)
   {
      transX += dx;
      transY += dy;
   }
   public void clearClip()
   {
      clipX1 = minX;
      clipY1 = minY;
      clipX2 = maxX;
      clipY2 = maxY;
   }
   public int getClipWidth()
   {
      return clipX2 - clipX1;
   }
   public int getClipHeight()
   {
      return clipY2 - clipY1;
   }
   public Rect getClip(Rect r)
   {
      r.x      = clipX1 - transX;
      r.y      = clipY1 - transY;
      r.width  = clipX2 - clipX1;
      r.height = clipY2 - clipY1;
      return r;
   }
   public void expandClipLimits(int dx1, int dy1, int dx2, int dy2)
   {
      minX += dx1;
      minY += dy1;
      maxX += dx2;
      maxY += dy2;
   }

   public void drawVerticalText(String text, int x, int y)
   {
      isVerticalText = true;
      drawText(text, x,y,0);
      isVerticalText = false;
   }
   ////////////////////////////////////////////////////////////////////////////////
   public void setClip(Rect r)
   {
      setClip(r.x, r.y, r.width, r.height);
   }
   ////////////////////////////////////////////////////////////////////////////////
   private static Hashtable ht3dColors = new Hashtable(83);
   private static StringBuffer sbc = new StringBuffer(30);
   public static void compute3dColors(boolean enabled, int backColor, int foreColor, int fourColors[])
   {
      if (backColor < 0 || foreColor < 0)
         return;
      sbc.setLength(0);
      String key = sbc.append(enabled).append(backColor).append(',').append(foreColor).toString();
      int four[] = (int[])ht3dColors.get(key);
      if (four == null)
      {
         four = new int[4];
         four[0] = backColor; // outside topLeft - BRIGHT
         four[1] = Color.brighter(backColor, Color.LESS_STEP); // inside topLeft - WHITE
	      if (four[0] == four[1]) // both WHITE?
            four[1] = Color.darker(four[0], Color.LESS_STEP);
         four[0] = Color.darker(four[0], Color.HALF_STEP); // in 16 colors, make the buttons more 3d
         four[2] = Color.brighter(foreColor, enabled ? Color.LESS_STEP : Color.FULL_STEP); // inside bottomRight - DARK - guich@tc122_48: use full_step if not enabled
         four[3] = foreColor; // outside bottomRight - BLACK
	      if (!enabled)
         {
            four[3] = four[2];
            four[2] = Color.brighter(four[2], Color.LESS_STEP);
         }
         ht3dColors.put(key, four);
      }
      fourColors[0] = four[0];
      fourColors[1] = four[1];
      fourColors[2] = four[2];
      fourColors[3] = four[3];
   }

   public void getAnglePoint(int xc, int yc, int rx, int ry, int angle, Coord out) // guich@300_41: fixed method signature
   {
      if (angle < 0 || angle >= 360)
      {
         while (angle <    0) angle += 360;
         while (angle >= 360) angle -= 360;
      }
      if (acos == null) // create a lookup table for sin and cos - these tend to be slooow when in loop.
      {
         acos = new int[360];
         asin = new int[360];
         double tick = 2.0 * Math.PI / acos.length, a = 0;
         for (int i = 0; i <= 45; a += tick, i++)
         {
            acos[90-i] = asin[i] = (int)(Math.sin(a)*(1<<18));
            asin[90-i] = acos[i] = (int)(Math.cos(a)*(1<<18));
         }
         for (int s,c,i = 0; i < 90; i++)
         {
            s = asin[i];
            c = acos[i];

            asin[i+90]  = c;
            asin[i+180] = acos[i+90 ] = -s;
            asin[i+270] = acos[i+180] = -c;
            acos[i+270] = s;
         }
      }
      out.x = xc + (acos[angle]*rx>>18);
      out.y = yc - (asin[angle]*ry>>18);
   }

   /////////////////////////////////////////////////////////////////////////////////////////////
   native public void fillVistaRect(int x, int y, int width, int height, int back, boolean invert, boolean rotate);
   native public void fillShadedRect(int x, int y, int width, int height, boolean invert, boolean rotate, int c1, int c2, int factor);
   native protected void create(totalcross.ui.gfx.GfxSurface surface);
   native public void drawEllipse(int xc, int yc, int rx, int ry);
   native public void fillEllipse(int xc, int yc, int rx, int ry);
   native public void drawArc(int xc, int yc, int r, double startAngle, double endAngle);
   native public void drawPie(int xc, int yc, int r, double startAngle, double endAngle);
   native public void fillPie(int xc, int yc, int r, double startAngle, double endAngle);
   native public void drawEllipticalArc(int xc, int yc, int rx, int ry, double startAngle, double endAngle);
   native public void drawEllipticalPie(int xc, int yc, int rx, int ry, double startAngle, double endAngle);
   native public void fillEllipticalPie(int xc, int yc, int rx, int ry, double startAngle, double endAngle);
   native public void drawCircle(int xc, int yc, int r);
   native public void fillCircle(int xc, int yc, int r);
   native public int getPixel(int x, int y);
   native public void setPixel(int x, int y);
   native public void drawLine(int ax, int ay, int bx, int by);
   native public void drawDots(int ax, int ay, int bx, int by);
   native public void drawRect(int x, int y, int w, int h);
   native public void fillRect(int x, int y, int w, int h);
   native public void drawDottedRect(int x, int y, int w, int h);
   native public void fillPolygon(int []xPoints, int []yPoints, int nPoints);
   native public void drawPolygon(int []xPoints, int []yPoints, int nPoints);
   native public void drawPolyline(int []xPoints, int []yPoints, int nPoints);
   native public void drawText(String text, int x, int y);
   native public void drawText(char []chars, int start, int count, int x, int y);
   native public void drawText(String text, int x, int y, int justifyWidth);
   native public void drawHatchedRect(int x, int y, int width, int height, boolean top, boolean bottom);
   native public void fillHatchedRect(int x, int y, int width, int height, boolean top, boolean bottom);
   native public void drawRoundRect(int x, int y, int width, int height, int r);
   native public void fillRoundRect(int x, int y, int width, int height, int r);
   native public void setClip(int x, int y, int w, int h);
   native public boolean clip(totalcross.ui.gfx.Rect r);
   native public void copyRect(totalcross.ui.gfx.GfxSurface surface, int x, int y, int width, int height, int dstX, int dstY);
   native public void drawRoundGradient(int startX, int startY, int endX, int endY, int topLeftRadius, int topRightRadius, int bottomLeftRadius, int bottomRightRadius,int startColor, int endColor, boolean vertical);
   native public void drawImage(totalcross.ui.image.Image image, int x, int y, boolean doClip);
   native public void copyImageRect(totalcross.ui.image.Image image, int x, int y, int width, int height, boolean doClip);
   native public void setPixels(int []xPoints, int []yPoints, int nPoints);
   native public void refresh(int sx, int sy, int sw, int sh, int tx, int ty, totalcross.ui.font.Font f);
   native public void drawVistaRect(int x, int y, int width, int height, int topColor, int rightColor, int bottomColor, int leftColor);
   native public void draw3dRect(int x, int y, int width, int height, byte type, boolean yMirror, boolean simple, int []fourColors);
   native private void fillVistaRect(int x, int y, int width, int height, boolean invert, boolean rotate, int[] colors);
   native public void drawArrow(int x, int y, int h, byte type, boolean pressed, int color);
   native public void drawImage(totalcross.ui.image.Image4D image, int x, int y);
   native public void fillEllipseGradient(int xc, int yc, int rx, int ry);
   native public void fillPieGradient(int xc, int yc, int r, double startAngle, double endAngle);
   native public void fillEllipticalPieGradient(int xc, int yc, int rx, int ry, double startAngle, double endAngle);
   native public void fillCircleGradient(int xc, int yc, int r);
   native public void fillPolygonGradient(int []xPoints, int []yPoints, int nPoints);
   native public int getRGB(int[] data, int offset, int x, int y, int w, int h);
   native public int setRGB(int[] data, int offset, int x, int y, int w, int h);
   native public static void fadeScreen(int fadeValue);
   native public void drawText(char chars[], int chrStart, int chrCount, int x, int y, boolean shadow, int shadowColor);
   native public void drawText(StringBuffer sb, int chrStart, int chrCount, int x, int y, boolean shadow, int shadowColor);
   native public void drawText(StringBuffer sb, int chrStart, int chrCount, int x, int y, int justifyWidth, boolean shadow, int shadowColor);
   native public void drawText(String text, int x, int y, boolean shadow, int shadowColor);
   native public void drawText(String text, int x, int y, int justifyWidth, boolean shadow, int shadowColor);
   native public void drawWindowBorder(int xx, int yy, int ww, int hh, int titleH, int footerH, int borderColor, int titleColor, int bodyColor, int footerColor, int thickness, boolean drawSeparators);
   native public void dither(int x, int y, int w, int h);
   native public void drawCylindricShade(int startColor, int endColor, int startX, int startY, int endX, int endY);
   native public void drawThickLine(int x1, int y1, int x2, int y2, int t);
   
   /** Dumb method to keep compilation compatibility with TC 1 */
   public void eraseRect(int x, int y, int w, int h)
   {
   }
   /** Dumb method to keep compilation compatibility with TC 1 */
   public void eraseRect(int x, int y, int w, int h, int fromColor, int toColor, int textColor)
   {
   }
   private static int[] dumbVC = new int[11];
   /** Dumb method to keep compilation compatibility with TC 1 */
   public static int[] getVistaColors(int c)
   {
      return dumbVC;
   }
   /** Dumb method to keep compilation compatibility with TC 1 */
   public void drawHighLightFrame(int x, int y, int w, int h, int topLeftColor, int bottomRightColor, boolean yMirror)
   {
   }
   /** Dumb method to keep compilation compatibility with TC 1 */
   public void fillCursor(int x, int y, int w, int h)
   {
   }
   /** Dumb method to keep compilation compatibility with TC 1 */
   public void drawCursor(int x, int y, int w, int h)
   {
   }
   /** Dumb method to keep compilation compatibility with TC 1 */
   public void drawDottedCursor(int x, int y, int w, int h)
   {
   }
   /** Dumb method to keep compilation compatibility with TC 1; parameters drawOp and backColor are ignored. */
   public void copyImageRect(totalcross.ui.image.Image image, int x, int y, int width, int height, int drawOp, int backColor, boolean doClip)
   {
      copyImageRect(image, x, y, width, height, doClip);
   }
   /** Dumb method to keep compilation compatibility with TC 1 */
   public void drawText(StringBuffer sb, int chrStart, int chrCount, int x, int y)
   {
      drawText(sb, chrStart, chrCount, x, y, false, -1); 
   }
   /** Dumb method to keep compilation compatibility with TC 1 */
   public void drawText(StringBuffer sb, int chrStart, int chrCount, int x, int y, int justifyWidth)
   {
      drawText(sb, chrStart, chrCount, x, y, justifyWidth, false, -1); 
   }
   /** Dumb method to keep compilation compatibility with TC 1; parameters drawOp and backColor are ignored. */
   public void drawImage(totalcross.ui.image.Image image, int x, int y, int drawOp, int backColor, boolean doClip)
   {
      drawImage(image, x,y, doClip);
   }


   /** Dumb field to keep compilation compatibility with TC 1 */
   public int drawOp;
}
