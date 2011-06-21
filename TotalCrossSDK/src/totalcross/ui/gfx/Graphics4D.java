/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
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
import totalcross.ui.*;
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
   public int drawOp;
   public boolean useAA;
   protected int width,height;
   protected int transX, transY;
   protected int clipX1,clipY1,clipX2,clipY2; // clip1 <= k < clip2
   protected int minX, minY, maxX, maxY;
   protected int lastRX,lastRY,lastXC,lastYC,lastSize; // used by arcPiePointDrawAndFill
   protected int pitch;
   // instance doubles
   protected double lastPPD; // used by arcPiePointDrawAndFill
   // instance objects
   protected GfxSurface surface;
   protected Object font;
   protected int xPoints[], yPoints[]; // used by arcPiePointDrawAndFill
   protected int[]ints; // used by fillPolygon
   // static objects
   public static boolean needsUpdate; // IMPORTANT: NOT IMPLEMENTED
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

   public static final int DRAW_PAINT         = 0;
   public static final int DRAW_ERASE         = 1;
   public static final int DRAW_MASK          = 2;
   public static final int DRAW_INVERT        = 3;
   public static final int DRAW_OVERLAY       = 4;
   public static final int DRAW_PAINT_INVERSE = 5;
   public static final int DRAW_SPRITE        = 6;
   public static final int DRAW_REPLACE_COLOR = 7;
   public static final int DRAW_SWAP_COLORS   = 8;

   public Graphics4D(GfxSurface surface)
   {
      this.surface = surface;
      create(surface);
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
   public Rect getClip(Rect r)
   {
      r.x      = clipX1 - transX;
      r.y      = clipY1 - transY;
      r.width  = clipX2 - clipX1;
      r.height = clipY2 - clipY1;
      return r;
   }

   native protected void create(totalcross.ui.gfx.GfxSurface surface);
   native public void drawDottedCursor(int x, int y, int w, int h);
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
   native public void eraseRect(int x, int y, int w, int h);
   native public void drawLine(int ax, int ay, int bx, int by);
   native public void drawDots(int ax, int ay, int bx, int by);
   native public void fillCursor(int x, int y, int w, int h); // OLD drawCursor
   native public void drawCursor(int x, int y, int w, int h); // OLD drawCursorOutline
   native public void drawRect(int x, int y, int w, int h);
   native public void fillRect(int x, int y, int w, int h);
   native public void drawDottedRect(int x, int y, int w, int h);
   native public void fillPolygon(int []xPoints, int []yPoints, int nPoints);
   native public void drawPolygon(int []xPoints, int []yPoints, int nPoints);
   native public void drawPolyline(int []xPoints, int []yPoints, int nPoints);
   native public void drawText(String text, int x, int y);
   native public void drawText(char []chars, int start, int count, int x, int y);
   native public void drawText(String text, int x, int y, int justifyWidth);
   native public void drawText(StringBuffer sb, int chrStart, int chrCount, int x, int y, int justifyWidth);
   native public void drawText(StringBuffer sb, int chrStart, int chrCount, int x, int y);
   native public void drawHatchedRect(int x, int y, int width, int height, boolean top, boolean bottom);
   native public void fillHatchedRect(int x, int y, int width, int height, boolean top, boolean bottom);
   native public void drawRoundRect(int x, int y, int width, int height, int r);
   native public void fillRoundRect(int x, int y, int width, int height, int r);
   native public void setClip(int x, int y, int w, int h);
   native public boolean clip(totalcross.ui.gfx.Rect r);
   native public void copyRect(totalcross.ui.gfx.GfxSurface surface, int x, int y, int width, int height, int dstX, int dstY);
   native public void drawHighLightFrame(int x, int y, int w, int h, int topLeftColor, int bottomRightColor, boolean yMirror);
   native public void drawRoundGradient(int startX, int startY, int endX, int endY, int topLeftRadius, int topRightRadius, int bottomLeftRadius, int bottomRightRadius,int startColor, int endColor, boolean vertical);
   native public void drawImage(totalcross.ui.image.Image image, int x, int y, int drawOp, int backColor, boolean doClip);
   native public void copyImageRect(totalcross.ui.image.Image image, int x, int y, int width, int height, int drawOp, int backColor, boolean doClip);
   native public void setPixels(int []xPoints, int []yPoints, int nPoints);
   native public void refresh(int sx, int sy, int sw, int sh, int tx, int ty, totalcross.ui.font.Font f);
   native public void drawVistaRect(int x, int y, int width, int height, int topColor, int rightColor, int bottomColor, int leftColor);
   native public void draw3dRect(int x, int y, int width, int height, byte type, boolean yMirror, boolean simple, int []fourColors);
   native public void eraseRect(int x, int y, int w, int h, int fromColor, int toColor, int textColor);
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
      Vm.arrayCopy(four, 0, fourColors, 0, 4);
   }

   private static Hashtable htVistaColors = new Hashtable(83);
   private static int[] lastVistaColors; // speedup
   private static int lastVistaColor=-1;

   public static int[] getVistaColors(int c) // guich@573_6
   {
      int []vistaColors = (int[])htVistaColors.get(c);
      if (vistaColors == null)
      {
         int origC = c;
         int step = UIColors.vistaFadeStep;
         vistaColors = new int[11];
         for (int p = 0; p <= 10; p++)
         {
            vistaColors[p] = c;
            c = Color.darker(c, p == 4 ? (step+step) : step);
         }
         htVistaColors.put(origC, vistaColors);
      }
      return vistaColors;
   }

   public void fillVistaRect(int x, int y, int width, int height, int back, boolean invert, boolean rotate) // guich@573_6
   {
      fillVistaRect(x, y, width, height, invert, rotate, back == lastVistaColor ? lastVistaColors : (lastVistaColors = getVistaColors(lastVistaColor=back)));
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

   public void fillShadedRect(int x, int y, int width, int height, boolean invert, boolean rotate, int c1, int c2, int factor) // guich@573_6
   {
      int dim = rotate ? width : height, dim0 = dim;
      int y0 = rotate ? x : y;
      int hh = rotate ? x+dim : y+dim;
      dim <<= 16;
      int incY = dim/height;
      int lineH = (incY>>16)+1;
      int lineY=0;
      int lastF=-1;
      // now paint the shaded area
      for (int c=0; lineY < dim; c++, lineY += incY)
      {
         int i = c >= dim0 ? dim0-1 : c;
         int f = (invert ? dim0-1-i : i)*factor/dim0;
         if (f != lastF) // colors repeat often
            backColor = Color.interpolate(c1, c2, lastF = f);
         int yy = y0+(lineY>>16);
         int k = hh - yy;
         if (!rotate)
            fillRect(x,yy,width,k < lineH ? k : lineH);
         else
            fillRect(yy,y,k < lineH ? k : lineH, height);
      }
   }
   /////////////////////////////////////////////////////////////////////////////////////////////
   // guich@tc130: now stuff for Android ui style
   // inside points are negative values, outside points are positive values
   private static final int IN  = 0;
   private static final int OUT = 0x100;

   int[][][] windowBorderAlpha =
   {
      {  // thickness 1
         { 190, 190,  152,   89,  OUT, OUT, OUT },
         { 255, 255,  255,  255,  220, OUT, OUT },
         {  IN,  IN,  -32, -110,  255, 174, OUT },
         {  IN,  IN,   IN,  -11,  255, 245,  62 },
         {  IN,  IN,   IN,   IN, -110, 255,  81 },
         {  IN,  IN,   IN,   IN, -26,  255, 152 },
         {  IN,  IN,   IN,   IN,  IN,  255, 190 },
      },
      {  // thickness 2
         { 255, 229,  163,   95,  OUT, OUT, OUT },
         { 255, 255,  255,  255,  215, OUT, OUT },
         {  IN, -36, -102, -197,  255, 215, OUT },
         {  IN,  IN,   IN,  -36, -191, 255, 122 },
         {  IN,  IN,   IN,   IN,  -77, 255, 179 },
         {  IN,  IN,   IN,   IN,  -32, 255, 229 },
         {  IN,  IN,   IN,   IN,   IN, 255, 255 },
      },
      {  // thickness 3
         { 255, 199,  128,   10,  OUT, OUT, OUT },
         { 255, 255,  255,  223,   59, OUT, OUT },
         { 255, 255,  255,  255,  255,  81, OUT },
         {  IN, -79, -234,  255,  255, 245,  16 },
         {  IN,  IN,  -32, -215,  255, 255, 133 },
         {  IN,  IN,   IN,  -77,  255, 255, 207 },
         {  IN,  IN,   IN,  -15,  255, 255, 245 },
      }
   };


   private int interpolate(int color1r, int color1g, int color1b, int color2, int factor)
   {
      int m = 255-factor;
      return Color.getRGB((color1r*factor+Color.getRed(color2)*m)/255, (color1g*factor+Color.getGreen(color2)*m)/255, (color1b*factor+Color.getBlue(color2)*m)/255);
   }
   
   private int interpolate(int color1r, int color1g, int color1b, int color2r, int color2g, int color2b, int factor)
   {
      int m = 255-factor;
      return Color.getRGB((color1r*factor+color2r*m)/255, (color1g*factor+color2g*m)/255, (color1b*factor+color2b*m)/255);
   }
   
   public void drawWindowBorder(int xx, int yy, int ww, int hh, int titleH, int footerH, int borderColor, int titleColor, int bodyColor, int footerColor, int thickness, boolean drawSeparators)
   {
      int kx, ky, a, c;
      
      int [][]aa = windowBorderAlpha[thickness-1];
      int bodyH = (titleH+footerH == 0) ? hh - 14 : hh - titleH-footerH;
      int y2 = yy+hh-1;
      int x2 = xx+ww-1;
      int x1l = xx+7;
      int y1l = yy+7;
      int x2r = x2-6;
      int y2r = y2-6;
      
      int borderColorR = (borderColor>>16) & 0xFF;
      int borderColorG = (borderColor>> 8) & 0xFF;
      int borderColorB = (borderColor    ) & 0xFF;

      int titleColorR = (titleColor>>16) & 0xFF;
      int titleColorG = (titleColor>> 8) & 0xFF;
      int titleColorB = (titleColor    ) & 0xFF;
      
      int footerColorR = (footerColor>>16) & 0xFF;
      int footerColorG = (footerColor>> 8) & 0xFF;
      int footerColorB = (footerColor    ) & 0xFF;
      
      int bodyColorR = (bodyColor>>16) & 0xFF;
      int bodyColorG = (bodyColor>> 8) & 0xFF;
      int bodyColorB = (bodyColor    ) & 0xFF;
      
      // horizontal and vertical lines
      for (int i = 0; i < 3; i++)
      {
         a = aa[i][0];
         if (a == OUT || a == IN)
            continue;
         kx = x1l;
         ky = yy+i;
         c = getPixel(kx, ky);
         foreColor = interpolate(borderColorR,borderColorG,borderColorB, c, a);
         drawLine(kx,ky,x2r,yy+i); // top
         
         ky = y2-i;
         c = getPixel(kx, ky);
         foreColor = interpolate(borderColorR,borderColorG,borderColorB, c, a);
         drawLine(kx,ky,x2r,y2-i); // bottom
         
         kx = xx+i;
         ky = y1l;
         c = getPixel(kx, ky);
         foreColor = interpolate(borderColorR,borderColorG,borderColorB, c, a);
         drawLine(kx,ky,xx+i,y2r); // left

         kx = x2-i;
         c = getPixel(kx, ky);
         foreColor = interpolate(borderColorR,borderColorG,borderColorB, c, a);
         drawLine(kx,ky,x2-i,y2r); // right
      }      
      // round corners
      for (int j = 0; j < 7; j++)
      {
         int top = yy+j, bot = y2r+j;
         for (int i = 0; i < 7; i++)
         {
            int left = xx+i, right = x2r+i;
            // top left
            a = aa[j][6-i];
            if (a != OUT)
            {
               if (a <= 0)
               {
                  foreColor = interpolate(borderColorR,borderColorG,borderColorB, titleColorR,titleColorG,titleColorB, -a);
                  setPixel(left,top);
               }
               else
               {
                  foreColor = interpolate(borderColorR,borderColorG,borderColorB, getPixel(left,top), a);
                  setPixel(left,top);
               }
            }

            // top right
            a = aa[j][i];
            if (a != OUT)
            {
               if (a <= 0)
               {
                  foreColor = interpolate(borderColorR,borderColorG,borderColorB, titleColorR,titleColorG,titleColorB, -a);
                  setPixel(right,top);
               }
               else
               {
                  foreColor = interpolate(borderColorR,borderColorG,borderColorB, getPixel(right,top), a);
                  setPixel(right,top);
               }
            }            
            // bottom left
            a = aa[i][j];
            if (a != OUT)
            {
               if (a <= 0)
               {
                  foreColor = interpolate(borderColorR,borderColorG,borderColorB, footerColorR,footerColorG,footerColorB, -a);
                  setPixel(left,bot);
               }
               else
               {
                  foreColor = interpolate(borderColorR,borderColorG,borderColorB, getPixel(left,bot), a);
                  setPixel(left,bot);
               }
            }            
            // bottom right
            a = aa[6-i][j];
            if (a != OUT)
            {
               if (a <= 0)
               {
                  foreColor = interpolate(borderColorR,borderColorG,borderColorB, footerColorR,footerColorG,footerColorB, -a);
                  setPixel(right,bot);
               }
               else
               {
                  foreColor = interpolate(borderColorR,borderColorG,borderColorB, getPixel(right,bot), a);
                  setPixel(right,bot);
               }
            }
         }
      }
      // now fill text, body and footer
      titleH -= 7;
      footerH -= 7;
      // text
      foreColor = titleColor;
      int t0 = thickness <= 2 ? 2 : 3;
      int ty = t0 + yy;
      for (int i = t0; i < 7; i++,ty++) // corners
         drawLine(x1l,ty,x2r,ty);
      for (int i = 0; i < titleH; i++,ty++) // non-corners
         drawLine(xx+t0,ty,x2-t0,ty);
      
      if (drawSeparators && titleColor == bodyColor)
      {
         foreColor = interpolate(borderColorR,borderColorG,borderColorB,titleColorR,titleColorG,titleColorB,64);
         drawLine(xx+t0,ty-1,x2-t0,ty-1);
      }
      // body
      foreColor = bodyColor;
      for (int i = 0; i < bodyH; i++, ty++)
         drawLine(xx+t0,ty,x2-t0,ty);
      
      if (drawSeparators && bodyColor == footerColor)
      {
         foreColor = interpolate(bodyColorR,bodyColorG,bodyColorB,titleColorR,titleColorG,titleColorB,64);
         drawLine(xx+t0,ty-1,x2-t0,ty-1);
      }
      // footer
      foreColor = footerColor;
      for (int i = 0; i < footerH; i++,ty++) // non-corners
         drawLine(xx+t0,ty,x2-t0,ty);
      for (int i = t0; i < 7; i++,ty++) // corners
         drawLine(x1l,ty,x2r,ty);
   }
}
