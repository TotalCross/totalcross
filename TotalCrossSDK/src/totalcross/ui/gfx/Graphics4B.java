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

import totalcross.*;
import totalcross.Launcher4B.UserFont;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.font.Font;
import totalcross.ui.image.*;
import totalcross.util.*;
import net.rim.device.api.system.*;
import net.rim.device.api.ui.*;

public final class Graphics4B
{
   protected GfxSurface surface;
   public int foreColor; // black
   public int backColor = Color.BRIGHT;
   public int drawOp;
   public boolean useAA;

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

   private int TRANSX, TRANSY;
   private int minX, minY, maxX, maxY;
   private static Hashtable fontCharCaches = new Hashtable(13);
   private Font font;
   private UserFont userFont;
   private net.rim.device.api.ui.Font nativeFont;
   private FontCharCache fontChars;
   private static int ands8Mask[] = {0x80,0x40,0x20,0x10,0x08,0x04,0x02,0x01};
   public Bitmap bitmap;
   private net.rim.device.api.ui.Graphics g;
   private static int[] pal685;
   public static boolean needsUpdate;
   private static int[] tempRowBuf1, tempRowBuf2;
   public static Object mainWindowPixels;
   private int lastXC, lastYC, lastRX, lastRY, lastSize, gxPoints[], gyPoints[], axPoints[][], ayPoints[][], anPoints[], aBase[];
   private double lastPPD;
   private boolean isControlSurface;
   private static int[]acos,asin;
   IntVector ints = new IntVector(4);
   private static XYRect grect = new XYRect();
   public boolean isVerticalText;
   
   static
   {
      int max = Math.max(Display.getWidth(), Display.getHeight());
      tempRowBuf1 = new int[max];
      tempRowBuf2 = new int[max];
   }

   public Graphics4B(GfxSurface surface)
   {
      if (surface instanceof Image)
         bitmap = (Bitmap)((Image)surface).getPixels();
      else
      if (surface instanceof Control)
      {
         if (Launcher4B.instance.screenResizePending)
         {
            mainWindowPixels = bitmap = new Bitmap(Settings.screenWidth, Settings.screenHeight);
            Launcher4B.instance.screenResizePending = false;
         }
         
         bitmap = (Bitmap)mainWindowPixels;
         isControlSurface = true;
      }
      else
         throw new RuntimeException("Only Image and Control can have a Graphics.");

      this.surface = surface;

      minX = 0;
      minY = 0;
      maxX = bitmap.getWidth();
      maxY = bitmap.getHeight();

      g = new net.rim.device.api.ui.Graphics(bitmap);
      g.pushContext(minX, minY, maxX, maxY, 0, 0);
   }
   
   public void refresh(int sx, int sy, int sw, int sh, int tx, int ty, Font f)
   {
      synchronized (this)
      {
         if (isControlSurface)
         {
            if (Launcher4B.instance.screenResizePending)
            {
               mainWindowPixels = new Bitmap(Settings.screenWidth, Settings.screenHeight);
               Launcher4B.instance.screenResizePending = false;
            }
            if (bitmap != mainWindowPixels) // mainWindowPixels was changed by this or by other graphics
            {
               bitmap = (Bitmap)mainWindowPixels; // bitmap may have changed
               g = new net.rim.device.api.ui.Graphics(bitmap);
            }
            else
               g.popContext();
         }
         else
            g.popContext();
         
         int bitmapWidth = bitmap.getWidth();
         int bitmapHeight = bitmap.getHeight();
         
         TRANSX = tx;
         TRANSY = ty;
   
         minX = sx;
         minY = sy;
         maxX = sx + sw;
         maxY = sy + sh;
   
         if (minX < 0)
            minX = 0;
         if (minY < 0)
            minY = 0;
         if (maxX > bitmapWidth)
            maxX = bitmapWidth;
         if (maxY > bitmapHeight)
            maxY = bitmapHeight;
   
         if (f != null && f != this.font)
            setFont(f);
         
         g.pushContext(minX, minY, maxX - minX, maxY - minY, 0, 0);
      }
   }

   public static void fadeScreen(int fadeValue)
   {
      Bitmap bitmap = (Bitmap)Graphics4B.mainWindowPixels;
      int[] buf = tempRowBuf1;
      
      int w = bitmap.getWidth();
      int h = bitmap.getHeight();
      
      boolean dec = fadeValue > 0;
      int lastColor = -1, lastFaded = 0;
      for (int i = h; --i >= 0;)
      {
         getRGB(bitmap, buf, 0, w, 0, i, w, 1, false);
         
         for (int j = w; --j >= 0;)
         {
            int rgb = buf[j];
            if (rgb == lastColor)
               buf[j] = lastFaded;
            else
            {
               lastColor = rgb;
               int r = ((rgb >> 16) & 0xFF) - fadeValue;
               int g = ((rgb >> 8) & 0xFF) - fadeValue;
               int b = (rgb & 0xFF) - fadeValue;
               if (dec) // if the value is being decreased, it will never be greater than the max value
               {
                  if (r < 0) r = 0; 
                  if (g < 0) g = 0; 
                  if (b < 0) b = 0; 
               }
               else
               {
                  if (r > 255) r = 255;
                  if (g > 255) g = 255;
                  if (b > 255) b = 255;
               }
               lastFaded = buf[j] = (r << 16) | (g << 8) | b;
            }
         }
         
         setRGB(bitmap, buf, 0, w, 0, i, w, 1);
      }
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

   public void drawEllipse(int xc, int yc, int rx, int ry)
   {
      xc += TRANSX;
      yc += TRANSY;

      g.setColor(foreColor);
      g.drawArc(xc - rx, yc - ry, rx * 2, ry * 2, 0, 360);
      
      if (isControlSurface)
         needsUpdate = true;
   }

   public void fillEllipse(int xc, int yc, int rx, int ry)
   {
      xc += TRANSX;
      yc += TRANSY;

      g.setColor(backColor);
      g.fillArc(xc - rx, yc - ry, rx * 2, ry * 2, 0, 360);
      
      if (isControlSurface)
         needsUpdate = true;
   }

   public void fillEllipseGradient(int xc, int yc, int rx, int ry)
   {
      ellipseDrawAndFill(xc,yc,rx,ry,foreColor,backColor,true,true);
   }

   public void drawArc(int xc, int yc, int r, double startAngle, double endAngle)
   {
      arcPiePointDrawAndFill(xc,yc,r,r,startAngle,endAngle,foreColor,foreColor,false,false,false);
   }

   public void drawPie(int xc, int yc, int r, double startAngle, double endAngle)
   {
      arcPiePointDrawAndFill(xc,yc,r,r,startAngle,endAngle,foreColor,foreColor,false,true,false);
   }

   public void fillPie(int xc, int yc, int r, double startAngle, double endAngle)
   {
      arcPiePointDrawAndFill(xc,yc,r,r,startAngle,endAngle,foreColor,backColor,true,true,false);
   }

   public void fillPieGradient(int xc, int yc, int r, double startAngle, double endAngle)
   {
      arcPiePointDrawAndFill(xc,yc,r,r,startAngle,endAngle,foreColor,backColor,true,true,true);
   }

   public void fillEllipticalPieGradient(int xc, int yc, int rx, int ry, double startAngle, double endAngle)
   {
      arcPiePointDrawAndFill(xc,yc,rx,ry,startAngle,endAngle,foreColor,backColor,true,true,true); // guich@402_57: was true,false
   }

   public void fillCircleGradient(int xc, int yc, int r)
   {
      ellipseDrawAndFill(xc,yc,r,r,foreColor, backColor,true, true);
   }

   public void drawEllipticalArc(int xc, int yc, int rx, int ry, double startAngle, double endAngle)
   {
      arcPiePointDrawAndFill(xc,yc,rx,ry,startAngle,endAngle,foreColor,foreColor,false,false,false);
   }

   public void drawEllipticalPie(int xc, int yc, int rx, int ry, double startAngle, double endAngle)
   {
      arcPiePointDrawAndFill(xc,yc,rx,ry,startAngle,endAngle,foreColor,foreColor,false,true,false); // guich@402_57: was true,false
   }

   public void fillEllipticalPie(int xc, int yc, int rx, int ry, double startAngle, double endAngle)
   {
      arcPiePointDrawAndFill(xc,yc,rx,ry,startAngle,endAngle,foreColor,backColor,true,true,false); // guich@402_57: was true,false
   }

   public void drawCircle(int xc, int yc, int r)
   {
      ellipseDrawAndFill(xc,yc,r,r,foreColor, foreColor,false,false);
   }

   public void fillCircle(int xc, int yc, int r)
   {
      ellipseDrawAndFill(xc,yc,r,r,backColor, backColor,true,false);
   }

   public int getPixel(int x, int y)
   {
      x += TRANSX;
      y += TRANSY;

      XYRect r = g.getClippingRect();
      if (x < r.x || x >= r.X2() || y < r.y || y >= r.Y2())
         return -1;

      int[] buf1 = tempRowBuf1;
      getRGB(bitmap, buf1, 0, 1, x, y, 1, 1, false);
      return buf1[0];
   }

   public void setPixel(int x, int y)
   {
      x += TRANSX;
      y += TRANSY;

      g.setColor(foreColor);
      g.drawPoint(x, y);
      
      if (isControlSurface)
         needsUpdate = true;
   }

   public void eraseRect(int x, int y, int w, int h)
   {
      x += TRANSX;
      y += TRANSY;

      XYRect rect = grect;
      rect.set(x, y, w, h);
      rect.intersect(g.getClippingRect());
      
      x = rect.x;
      y = rect.y;
      w = rect.width;
      h = rect.height;

      if (w <= 0 || h <= 0)
         return;

      int fC = getDeviceColor(foreColor);
      int bC = getDeviceColor(backColor);

      int[] buf1 = tempRowBuf1;
      for (int i = 0; i < h; i ++)
      {
         int ty = y + i;
         getRGB(bitmap, buf1, 0, w, x, ty, w, 1, false);

         for (int j = 0; j < w; j ++)
            if (buf1[j] == fC)
               buf1[j] = bC;

         g.drawRGB(buf1, 0, w, x, ty, w, 1);
      }

      if (isControlSurface)
         needsUpdate = true;
   }

   private static Hashtable htAAColors = new Hashtable(31);
   private static IntHashtable htFT = new IntHashtable(31);
   private static int lastFrom=-1,lastTo=-1, lastText=-1;
   private static Bitmap aaPixel = new Bitmap(1, 1);
   private static int []aaInt = new int[1];
   private static net.rim.device.api.ui.Graphics aaG = new net.rim.device.api.ui.Graphics(aaPixel);
   private int[] computeAAColors(int base, int text) // computes the AA colors for a color
   {
      Long key = new Long(((long)base << 24) | text); // guich@tc100b5_55: cast base to long
      int[] a = (int[]) htAAColors.get(key);
      if (a == null)
      {
         a = new int[16];
         for (int transparency = 255,i=15; transparency >= 0; transparency-=17,i--)
         {
            aaG.setGlobalAlpha(255);
            aaG.setColor(base);
            aaG.drawPoint(0, 0);
            aaG.setGlobalAlpha(transparency);
            aaG.setColor(text);
            aaG.drawPoint(0, 0);
            aaPixel.getARGB(a, i, 1, 0, 0, 1, 1);
            a[i] &= 0x00FFFFFF;
         }
         htAAColors.put(key, a);
      }
      return a;
   }

   public void eraseRect(int x, int y, int w, int h, int fromColor, int toColor, int textColor)
   {
      x += TRANSX;
      y += TRANSY;

      XYRect rect = grect;
      rect.set(x, y, w, h);
      rect.intersect(g.getClippingRect());
      
      x = rect.x;
      y = rect.y;
      w = rect.width;
      h = rect.height;
      
      if (w <= 0 || h <= 0)
         return;

      int[] buf1 = tempRowBuf1;
      if (!userFont.antialiased)
      {
         for (int i = 0; i < h; i ++)
         {
            int ty = y + i;
            getRGB(bitmap, buf1, 0, w, x, ty, w, 1, false);

            for (int j = 0; j < w; j ++)
               if (buf1[j] == fromColor)
                  buf1[j] = toColor;

            g.drawRGB(buf1, 0, w, x, ty, w, 1);
         }
      }
      else
      {
         IntHashtable ft = htFT;
         if (lastFrom != fromColor || lastTo != toColor || lastText != textColor)
         {
            lastFrom = fromColor;
            lastTo = toColor;
            lastText = textColor;
            int []froms = computeAAColors(fromColor, textColor);
            int []tos = computeAAColors(toColor, textColor);
            ft.clear();
            for (int i = 0; i < 16; i++)
               ft.put(froms[i],tos[i]);
         }
         for (int i = 0; i < h; i ++)
         {
            int ty = y + i;
            getRGB(bitmap, buf1, 0, w, x, ty, w, 1, false);

            for (int j = 0; j < w; j ++)
            {
               int p = buf1[j];
               if (p == fromColor)
                  buf1[j] = toColor;
               else
               if ((p = ft.get(p, -1)) != -1)
                  buf1[j] = p;
            }

            g.drawRGB(buf1, 0, w, x, ty, w, 1);
         }
      }
      
      if (isControlSurface)
         needsUpdate = true;
   }

   public void drawLine(int Ax, int Ay, int Bx, int By)
   {
      Ax += TRANSX;
      Bx += TRANSX;
      Ay += TRANSY;
      By += TRANSY;

      g.setColor(foreColor);
      if (useAA)
      {
         g.setDrawingStyle(net.rim.device.api.ui.Graphics.DRAWSTYLE_AALINES, true);
         g.drawLine(Ax, Ay, Bx, By);
         g.setDrawingStyle(net.rim.device.api.ui.Graphics.DRAWSTYLE_AALINES, false);
      }
      else
         g.drawLine(Ax, Ay, Bx, By);
      
      if (isControlSurface)
         needsUpdate = true;
   }

   public void drawDots(int Ax, int Ay, int Bx, int By)
   {
      Ax += TRANSX;
      Bx += TRANSX;
      Ay += TRANSY;
      By += TRANSY;

      // Clear all pixels to the background color
      g.setColor(backColor);
      g.drawLine(Ax, Ay, Bx, By);

      // Draw dotted line using 01010101 32 bits mask
      g.setStipple(0x55555555);
      g.setColor(foreColor);
      g.drawLine(Ax, Ay, Bx, By);

      // Clear mask
      g.setStipple(0xFFFFFFFF);
      
      if (isControlSurface)
         needsUpdate = true;
   }

   public void fillCursor(int x, int y, int w, int h)
   {
      x += TRANSX;
      y += TRANSY;

      g.invert(x, y, w, h);
      
      if (isControlSurface)
         needsUpdate = true;
   }

   public void drawCursor(int x, int y, int width, int height)
   {
      x += TRANSX;
      y += TRANSY;

      // bruno@583...: drawCursorOutline is an exception! it resizes the rectangle instead of just cropping it
      XYRect rect = grect;
      rect.set(x, y, width, height);
      rect.intersect(g.getClippingRect());
      
      if (rect.width <= 0 || rect.height <= 0)
         return;
      
      x = rect.x;
      y = rect.y;
      width = rect.width;
      height = rect.height;
      
      int x2 = x + width - 1;
      int y2 = y + height - 1;

      // Draw top and bottom lines
      g.invert(x, y, width, 1);
      if (y != y2)
         g.invert(x, y2, width, 1);

      // Draw left and right lines
      g.invert(x, y + 1, 1, height - 2);
      if (x != x2)
         g.invert(x2, y + 1, 1, height - 2);
      
      if (isControlSurface)
         needsUpdate = true;
   }

   public void drawDottedCursor(int x, int y, int width, int height) // guich@550_32
   {
      x += TRANSX;
      y += TRANSY;
      int x2 = x + width - 1;
      int y2 = y + height - 1;

      // Draw top and bottom lines
      for (int i = x; i < x2; i += 2)
      {
         g.invert(i, y, 1, 1);
         g.invert(i, y2, 1, 1);
      }

      // Draw left and right lines
      for (int i = y + 1; i < y2; i += 2)
      {
         g.invert(x, i, 1, 1);
         g.invert(x2, i, 1, 1);
      }
      
      if (isControlSurface)
         needsUpdate = true;
   }

   public void drawRect(int x, int y, int w, int h)
   {
      x += TRANSX;
      y += TRANSY;

      g.setColor(foreColor);
      g.drawRect(x, y, w, h);
      
      if (isControlSurface)
         needsUpdate = true;
   }

   public void fillRect(int x, int y, int w, int h)
   {
      x += TRANSX;
      y += TRANSY;

      g.setColor(backColor);
      g.fillRect(x, y, w, h);
      
      if (isControlSurface) 
         needsUpdate = true;
   }

   public void drawDottedRect(int x, int y, int w, int h)
   {
      x += TRANSX;
      y += TRANSY;

      // Clear all pixels to the background color
      g.setColor(backColor);
      g.drawRect(x, y, w, h);
      g.setColor(foreColor);

      // Draw the rectangle using 01010101 32 bits mask
      g.setStipple(0x55555555);
      g.drawRect(x, y, w, h);

      // Clear mask
      g.setStipple(0xFFFFFFFF);
      
      if (isControlSurface)
         needsUpdate = true;
   }

   private void fillPolygon(int []xPoints1, int []yPoints1, int base1, int nPoints1, int []xPoints2, int []yPoints2, int nPoints2, int c1, int c2, boolean gradient)
   {
      int x1, y1, x2, y2,n=0,temp, numSteps=0, startRed=0, startGreen=0, startBlue=0, endRed=0, endGreen=0, endBlue=0, redInc=0, greenInc=0, blueInc=0, red=0, green=0, blue=0, c=0, j;
      if (xPoints1 == null || yPoints1 == null || nPoints1 <= 0)
         return;
      if (nPoints1 < 2)
         return;
      if (ints == null)
         ints = new totalcross.util.IntVector(2);
      else
         ints.removeAllElements();
      if (axPoints == null)
      {
         axPoints = new int[2][];
         ayPoints = new int[2][];
         anPoints = new int[2];
         aBase = new int[2];
      }
      axPoints[0] = xPoints1; ayPoints[0] = yPoints1; anPoints[0] = nPoints1; aBase[0] = base1;
      axPoints[1] = xPoints2; ayPoints[1] = yPoints2; anPoints[1] = nPoints2;

      int miny = yPoints1[0];
      int maxy = yPoints1[0];
      for (int i = 1; i < nPoints1; i++)
      {
         if (yPoints1[base1+i] < miny) miny = yPoints1[base1+i];
         if (yPoints1[base1+i] > maxy) maxy = yPoints1[base1+i];
      }
      for (int i = 0; i < nPoints2; i++)
      {
         if (yPoints2[i] < miny) miny = yPoints2[i];
         if (yPoints2[i] > maxy) maxy = yPoints2[i];
      }
      if (gradient)
      {
         numSteps = maxy - miny; // guich@tc110_11: support horizontal gradient
         if (numSteps == 0) numSteps = 1; // guich@tc115_86: prevent divide by 0
         startRed = (c1 >> 16) & 0xFF;
         startGreen = (c1 >> 8) & 0xFF;
         startBlue = c1 & 0xFF;
         endRed = (c2 >> 16) & 0xFF;
         endGreen = (c2 >> 8) & 0xFF;
         endBlue = c2 & 0xFF;
         redInc = ((endRed - startRed) << 16) / numSteps;
         greenInc = ((endGreen - startGreen) << 16) / numSteps;
         blueInc = ((endBlue - startBlue) << 16) / numSteps;
         red = startRed << 16;
         green = startGreen << 16;
         blue = startBlue << 16;
      }
      else c = c1;
      for (int y = miny; y <= maxy; y++)
      {
         for (int a = 0; a < 2; a++)
         {
            int[] xPoints = axPoints[a];
            int[] yPoints = ayPoints[a];
            int nPoints = anPoints[a];
            int base = aBase[a];
            j = nPoints-1;
            for (int i = 0; i < nPoints; j=i,i++)
            {
               y1 = yPoints[base+j];
               y2 = yPoints[base+i];
               if (y1 == y2)
                  continue;
               if (y1 > y2) // invert
               {
                  temp = y1;
                  y1 = y2;
                  y2 = temp;
               }
               // compute next x point
               if ( (y1 <= y && y < y2) || (y == maxy && y1 < y && y <= y2) )
               {
                  if (yPoints[base+j] < yPoints[base+i])
                  {
                     x1 = xPoints[base+j];
                     x2 = xPoints[base+i];
                  }
                  else
                  {
                     x2 = xPoints[base+j];
                     x1 = xPoints[base+i];
                  }
                  ints.addElement((y - y1) * (x2 - x1) / (y2 - y1) + x1);
                  n++;
               }
            }
         }
         if (n >= 2)
         {
            if (gradient)
            {
               c = (red & 0xFF0000) | ((green>>8) & 0x00FF00) | ((blue >> 16) & 0xFF);
               red += redInc;
               green += greenInc;
               blue += blueInc;
            }
            int []items = ints.items;
            foreColor = c;
            if (n == 2) // most of the times
               drawLine(items[0],y,items[1],y);
            else
            {
               // sort the ints
               ints.qsort();
               // draw the lines
               for (int i = 0; i < n; i += 2)
                  drawLine(items[i],y,items[i+1],y);
            }
         }
         if (n > 0)
         {
            ints.removeAllElements();
            n = 0;
         }
      }
   }

   public void fillPolygon(int []xPoints, int []yPoints, int nPoints)
   {
      // took from gd 2.0.1 beta (http://www.boutell.com/gd/manual2.0.html) from Thomas Boutell
      int x1, y1, x2, y2;
      int ind1, ind2;
      if (nPoints < 2)
         return;

      int txPoints[] = new int[nPoints];
      int tyPoints[] = new int[nPoints];
      for (int i = 0; i < nPoints; i ++)
      {
         txPoints[i] = xPoints[i] + TRANSX;
         tyPoints[i] = yPoints[i] + TRANSY;
      }

      int miny = tyPoints[0];
      int maxy = tyPoints[0];
      for (int i = 1; i < nPoints; i++)
      {
         if (tyPoints[i] < miny)
            miny = tyPoints[i];
         if (tyPoints[i] > maxy)
            maxy = tyPoints[i];
      }
      for (int y = miny; y <= maxy; y++)
      {
         ints.removeAllElements();
         for (int i = 0; i < nPoints; i++)
         {
            if (i == 0)
            {
               ind1 = nPoints - 1;
               ind2 = 0;
            }
            else
            {
               ind1 = i - 1;
               ind2 = i;
            }
            y1 = tyPoints[ind1];
            y2 = tyPoints[ind2];
            if (y1 < y2)
            {
               x1 = txPoints[ind1];
               x2 = txPoints[ind2];
            }
            else if (y1 > y2)
            {
               y2 = tyPoints[ind1];
               y1 = tyPoints[ind2];
               x2 = txPoints[ind1];
               x1 = txPoints[ind2];
            }
            else
               continue;
            // compute next x point
            if ((y1 <= y && y < y2) || (y == maxy && y1 < y && y <= y2))
               ints.addElement(((y - y1) * (x2 - x1) / (y2 - y1) + x1));
         }
         int n = ints.size();
         if (n > 0)
         {
            int []items = ints.items;
            g.setColor(backColor);

            if (n == 2) // most of the times
               g.drawLine(items[0], y, items[1], y);
            else
            {
               // sort the ints
               ints.qsort();
               // draw the lines
               for (int i = 0; i < n; i += 2)
                  g.drawLine(items[i], y, items[i + 1], y);
            }
         }
      }
      
      if (isControlSurface)
         needsUpdate = true;
   }

   private void quadPixel(int xc, int yc, int x, int y)
   {
      // draw 4 points using symmetry
      setPixel(xc + x, yc + y);
      setPixel(xc + x, yc - y);
      setPixel(xc - x, yc + y);
      setPixel(xc - x, yc - y);
   }
   private void quadLine(int xc, int yc, int x, int y)
   {
      // draw 2 lines using symmetry
      drawLine(xc - x, yc - y, xc + x, yc - y);
      drawLine(xc - x, yc + y, xc + x, yc + y);
   }
   private void ellipseDrawAndFill(int xc, int yc, int rx, int ry, int c1, int c2, boolean fill, boolean gradient)
   {
      int numSteps=0, startRed=0, startGreen=0, startBlue=0, endRed=0, endGreen=0, endBlue=0, redInc=0, greenInc=0, blueInc=0, red=0, green=0, blue=0, c=0;
      // intermediate terms to speed up loop
      int t1 = rx*rx, t2 = t1<<1, t3 = t2<<1;
      int t4 = ry*ry, t5 = t4<<1, t6 = t5<<1;
      int t7 = rx*t5, t8 = t7<<1, t9 = 0;
      int d1 = t2 - t7 + (t4>>1);    // error terms
      int d2 = (t1>>1) - t8 + t5;
      int x = rx, y = 0; // ellipse points
      if (rx < 0 || ry < 0) // guich@501_13
         return;

      if (gradient)
      {
         numSteps = ry + ry; // guich@tc110_11: support horizontal gradient
         startRed = (c1 >> 16) & 0xFF;
         startGreen = (c1 >> 8) & 0xFF;
         startBlue = c1 & 0xFF;
         endRed = (c2 >> 16) & 0xFF;
         endGreen = (c2 >> 8) & 0xFF;
         endBlue = c2 & 0xFF;
         redInc = ((endRed - startRed) << 16) / numSteps;
         greenInc = ((endGreen - startGreen) << 16) / numSteps;
         blueInc = ((endBlue - startBlue) << 16) / numSteps;
         red = startRed << 16;
         green = startGreen << 16;
         blue = startBlue << 16;
      }
      else c = c1;

      while (d2 < 0)          // til slope = -1
      {
         if (gradient)
         {
            c = (red & 0xFF0000) | ((green>>8) & 0x00FF00) | ((blue >> 16) & 0xFF);
            red += redInc;
            green += greenInc;
            blue += blueInc;
         }
         foreColor = c;
         if (fill)
            quadLine(xc,yc,x,y);
         else
            quadPixel(xc,yc,x,y);
         y++;        // always move up here
         t9 += t3;
         if (d1 < 0) // move straight up
         {
            d1 += t9 + t2;
            d2 += t9;
         }
         else        // move up and left
         {
            x--;
            t8 -= t6;
            d1 += t9 + t2 - t8;
            d2 += t9 + t5 - t8;
         }
      }

      do              // rest of top right quadrant
      {
         if (gradient)
         {
            c = (red & 0xFF0000) | ((green>>8) & 0x00FF00) | ((blue >> 16) & 0xFF);
            red += redInc;
            green += greenInc;
            blue += blueInc;
         }
         foreColor = c;
         // draw 4 points using symmetry
         if (fill)
            quadLine(xc,yc,x,y);
         else
            quadPixel(xc,yc,x,y);
         x--;        // always move left here
         t8 -= t6;
         if (d2 < 0) // move up and left
         {
            y++;
            t9 += t3;
            d2 += t9 + t5 - t8;
         }
         else        // move straight left
            d2 += t5 - t8;
      } while (x >= 0);
   }
   ////////////////////////////////////////////////////////////////////////////
   // draw an elliptical arc from startAngle to endAngle. c is the fill color and c2 is the outline color (if in fill mode - otherwise, c = outline color)
   private void arcPiePointDrawAndFill(int xc, int yc, int rx, int ry, double startAngle, double endAngle, int c, int c2, boolean fill, boolean pie, boolean gradient)
   {
      // make sure the values are -359 <= x <= 359
      while (startAngle <= -360) startAngle += 360;
      while (endAngle   <= -360) endAngle   += 360;
      while (startAngle >   360) startAngle -= 360;
      while (endAngle   >   360) endAngle   -= 360;

      if (startAngle == endAngle) // guich@501_13
         return;
      if (startAngle > endAngle) // eg 235 to 45
         startAngle -= 360; // set to -45 to 45 so we can handle it correctly
      if (startAngle >= 0 && endAngle <= 0) // eg 135 to -135
         endAngle += 360; // set to 135 to 225

      int nPoints=0;
      // this algorithm was created by Guilherme Campos Hazan
      double ppd;
      int startIndex,endIndex,index,i,oldX1=0,oldY1=0,oldX2=0,oldY2=0;
      int nq,size=0;
      if (rx < 0 || ry < 0) // guich@501_13
         return;
      // step 0: correct angle values
      if (startAngle < 0.1 && endAngle > 359.9) // full circle? use the fastest routine instead
      {
         if (fill)
            ellipseDrawAndFill(xc,yc,rx,ry,c, c2,true, gradient); // guich@201_2: corrected colors. - guich@401_3: changed c->c2 and vice versa (line below)
         ellipseDrawAndFill(xc,yc,rx,ry,c, c,false, gradient);
         return;
      }
      int[]xPoints = gxPoints;
      int[]yPoints = gyPoints;
      // step 0: if possible, use cached results
      boolean sameC = xc == lastXC && yc == lastYC;
      boolean sameR = rx == lastRX && ry == lastRY;
      if (!sameC || !sameR)
      {
         int t1 = rx*rx, t2 = t1<<1, t3 = t2<<1;
         int t4 = ry*ry, t5 = t4<<1, t6 = t5<<1;
         int t7 = rx*t5, t8 = t7<<1, t9 = 0;
         int d1 = t2 - t7 + (t4>>1);    // error terms
         int d2 = (t1>>1) - t8 + t5;
         int x = rx, y = 0; // ellipse points

         if (sameR)
            size = (lastSize-2)/4;
         else
         {
            // step 1: computes how many points the circle has (computes only 45 degrees and mirrors the rest)
            // intermediate terms to speed up loop
            while (d2 < 0)          // til slope = -1
            {
               t9 += t3;
               if (d1 < 0) // move straight up
               {
                  d1 += t9 + t2;
                  d2 += t9;
               }
               else        // move up and left
               {
                  x--;
                  t8 -= t6;
                  d1 += t9 + t2 - t8;
                  d2 += t9 + t5 - t8;
               }
               size++;
            }

            do              // rest of top right quadrant
            {
               x--;        // always move left here
               t8 -= t6;
               if (d2 < 0) // move up and left
               {
                  t9 += t3;
                  d2 += t9 + t5 - t8;
               }
               else        // move straight left
                  d2 += t5 - t8;
               size++;

            } while (x >= 0);
         }
         nq = size;
         size *= 4;
         // step 2: computes how many points per degree
         ppd = (double)size / 360.0;
         // step 3: create space in the buffer so it can save all the circle
         size+=2;
         if (nPoints < size)
         {
            gxPoints = gyPoints = null;
            xPoints = gxPoints = new int[size];
            yPoints = gyPoints = new int[size];
         }
         // step 4: stores all the circle in the array. the odd arcs are drawn in reverse order
         // intermediate terms to speed up loop
         if (!sameR)
         {
            t2 = t1<<1; t3 = t2<<1;
            t8 = t7<<1; t9 = 0;
            d1 = t2 - t7 + (t4>>1); // error terms
            d2 = (t1>>1) - t8 + t5;
            x = rx;
         }
         i=0;
         while (d2 < 0)          // til slope = -1
         {
            // save 4 points using symmetry
            // guich@340_3: added clipping
            index = nq*0+i;      // 0/3
            xPoints[index]=xc+x;
            yPoints[index]=yc-y;

            index = nq*2-i-1;    // 1/3
            xPoints[index]=xc-x;
            yPoints[index]=yc-y;

            index = nq*2+i;      // 2/3
            xPoints[index]=xc-x;
            yPoints[index]=yc+y;

            index = nq*4-i-1;    // 3/3
            xPoints[index]=xc+x;
            yPoints[index]=yc+y;
            i++;

            y++;        // always move up here
            t9 += t3;
            if (d1 < 0) // move straight up
            {
               d1 += t9 + t2;
               d2 += t9;
            }
            else        // move up and left
            {
               x--;
               t8 -= t6;
               d1 += t9 + t2 - t8;
               d2 += t9 + t5 - t8;
            }
         }

         do              // rest of top right quadrant
         {
            // save 4 points using symmetry
            // guich@340_3: added clipping
            index = nq*0+i;    // 0/3
            xPoints[index]=xc+x;
            yPoints[index]=yc-y;

            index = nq*2-i-1;  // 1/3
            xPoints[index]=xc-x;
            yPoints[index]=yc-y;

            index = nq*2+i;    // 2/3
            xPoints[index]=xc-x;
            yPoints[index]=yc+y;

            index = nq*4-i-1;  // 3/3
            xPoints[index]=xc+x;
            yPoints[index]=yc+y;
            i++;

            x--;        // always move left here
            t8 -= t6;
            if (d2 < 0) // move up and left
            {
               y++;
               t9 += t3;
               d2 += t9 + t5 - t8;
            }
            else        // move straight left
               d2 += t5 - t8;
         } while (x >= 0);
         // save last arguments
         lastPPD = ppd;
         lastSize = size;
         lastXC = xc; lastYC = yc; lastRX = rx; lastRY = ry;
      }
      else
      {
         ppd = lastPPD;
         size = lastSize;
      }
      // step 5: computes the start and end indexes that will become part of the arc
      if (startAngle < 0)
         startAngle += 360;
      if (endAngle < 0)
         endAngle += 360;
      startIndex = (int)(ppd * startAngle);
      endIndex = (int)(ppd * endAngle);
      int last = size-2;
      if (endIndex >= last) // 360?
         endIndex--;
      // step 6: fill or draw the polygons
      endIndex++;
      if (pie)
      {
         // connect two lines from the center to the two edges of the arc
         oldX1 = xPoints[endIndex];
         oldY1 = yPoints[endIndex];
         oldX2 = xPoints[endIndex+1];
         oldY2 = yPoints[endIndex+1];
         xPoints[endIndex] = xc;
         yPoints[endIndex] = yc;
         xPoints[endIndex+1] = xPoints[startIndex];
         yPoints[endIndex+1] = yPoints[startIndex];
         endIndex+=2;
      }

      if (startIndex > endIndex) // drawing from angle -30 to +30 ? (startIndex = 781, endIndex = 73, size=854)
      {
         int p1 = last-startIndex;
         if (fill)
            fillPolygon(xPoints, yPoints, startIndex, p1, xPoints, yPoints, endIndex, gradient ? c : c2, c2, gradient); // lower half, upper half
         if (!gradient) drawPolygon(xPoints, yPoints, startIndex, p1, xPoints, yPoints, endIndex, c);
      }
      else
      {
         if (fill)
            fillPolygon(xPoints, yPoints, startIndex, endIndex-startIndex, null,null,0, gradient ? c : c2, c2, gradient);
         if (!gradient) drawPolygon(xPoints, yPoints, startIndex, endIndex-startIndex, null,null,0, c);
      }
      if (pie)  // restore saved points
      {
         endIndex-=2;
         xPoints[endIndex]   = oldX1;
         yPoints[endIndex]   = oldY1;
         xPoints[endIndex+1] = oldX2;
         yPoints[endIndex+1] = oldY2;
      }
   }

   public void fillPolygonGradient(int []xPoints, int []yPoints, int nPoints)
   {
      if (nPoints > xPoints.length || nPoints > yPoints.length)
         throw new ArrayIndexOutOfBoundsException("array index out of range at fillPolygon: "+nPoints);
      else
         fillPolygon(xPoints,yPoints,0,nPoints,null,null,0,foreColor, backColor, true);
   }

   public void drawPolygon(int []xPoints, int []yPoints, int nPoints)
   {
      drawPolygon(xPoints,yPoints,nPoints,foreColor,true);
   }

   public void drawPolyline(int []xPoints, int []yPoints, int nPoints) // guich@330_19
   {
      drawPolygon(xPoints,yPoints,nPoints,foreColor,false);
   }

   public void setPixels(int []xPoints, int []yPoints, int nPoints) // guich@330_19
   {
      g.setColor(foreColor);
      for (int i = 0; i < nPoints; i ++)
         g.drawPoint(xPoints[i], yPoints[i]);
      
      if (isControlSurface)
         needsUpdate = true;
   }

   //
   public void drawText(char chars[], int chrStart, int chrCount, int x, int y, boolean shadow, int shadowColor)
   {
      drawText(new String(chars, chrStart, chrCount), x,y, 0, shadow, shadowColor);
   }

   public void drawText(StringBuffer sb, int chrStart, int chrCount, int x, int y, boolean shadow, int shadowColor)
   {
      drawText(sb.toString().substring(chrStart, chrStart+chrCount), x,y, 0, shadow, shadowColor);
   }
   
   public void drawText(StringBuffer sb, int chrStart, int chrCount, int x, int y, int justifyWidth, boolean shadow, int shadowColor)
   {
      drawText(sb.toString().substring(chrStart, chrStart+chrCount), x,y, justifyWidth, shadow, shadowColor);
   }
   
   public void drawText(String text, int x, int y, boolean shadow, int shadowColor)
   {
      drawText(text, x,y,0, shadow, shadowColor);
   }

   public void drawText(String text, int x, int y, int justifyWidth, boolean shadow, int shadowColor)
   {
      if (shadow)
      {
         int old = foreColor;
         foreColor = shadowColor;
         drawText(text, x-1,y-1,justifyWidth);
         drawText(text, x+1,y-1,justifyWidth);
         drawText(text, x+1,y+1,justifyWidth);
         drawText(text, x-1,y+1,justifyWidth);
         foreColor = old;
      }
      drawText(text, x,y,justifyWidth);
   }
   //
   
   public void drawText(char chars[], int chrStart, int chrCount, int x, int y)
   {
      drawText(new String(chars, chrStart, chrCount), x,y,0);
   }

   public void drawText(StringBuffer sb, int chrStart, int chrCount, int x, int y)
   {
      drawText(sb.toString().substring(chrStart, chrStart+chrCount), x,y,0);
   }

   public void drawText(StringBuffer sb, int chrStart, int chrCount, int x, int y, int justifyWidth)
   {
      drawText(sb.toString().substring(chrStart, chrStart+chrCount), x,y, justifyWidth);
   }

   public void drawText(String text, int x, int y)
   {
      drawText(text, x,y,0);
   }

   public void drawVerticalText(String text, int x, int y)
   {
      isVerticalText = true;
      drawText(text, x,y,0);
      isVerticalText = false;
   }

   public void drawText(String text, int x, int y, int justifyWidth)
   {
      char[] chars;
      int count;

      if (text == null || (count = (chars = text.toCharArray()).length) == 0) return;

      x += TRANSX;
      y += TRANSY;

      int extraPixelsPerChar = 0, extraPixelsRemaining = 0;

      if (justifyWidth > 0)
      {
         while (chars[count - 1] <= ' ')
            count--;
         if (count == 0) return;

         int rem = justifyWidth, i = count;
         while (--i >= 0)
            rem -= Launcher4B.instance.getCharWidth(font, chars[i]);
         
         if (rem > 0)
         {
            extraPixelsPerChar = rem / count;
            extraPixelsRemaining = rem % count;
         }
      }

      if (nativeFont != null) // if using system font, we must set the font color
         g.setColor(foreColor);

      int maxX = g.getClippingRect().X2();
      int incY = userFont.maxHeight + justifyWidth;
      boolean isVert = isVerticalText;
      
      for (int i = 0; i < count && x < maxX; i++) // draw chars while text is visible
      {
         char ch = chars[i];
         
         if (ch == ' ')
         {
            if (isVert)
               y += incY;
            else
               x += userFont.spaceWidth;
         }
         else
         if (ch == 160)
         {
            if (isVert)
               y += incY;
            else
               x += userFont.numberWidth;
         }
         else
         if (ch == '\t')
         {
            if (isVert)
               y += incY * Font.TAB_SIZE;
            else
               x += userFont.spaceWidth * Font.TAB_SIZE;
         }
         else if (ch > ' ')
         {
            int w;
            
            if (ch < userFont.firstChar || ch > userFont.lastChar) // guich@tc122_16: if the char is outside the font's range, load the new font
            {
               this.font.hv_UserFont = Launcher4B.instance.getFont(this.font, ch);
               setFont(this.font, true);
            }
            if (nativeFont == null) // using TC font
            {
               Bitmap b = fontChars.getChar(ch, foreColor);
               if (b == null)
                  w = userFont.spaceWidth;
               else
               {
                  w = b.getWidth();
                  g.drawBitmap(x, y, w, userFont.maxHeight, b, 0, 0);
               }
            }
            else // using system font
            {
               g.drawText(ch, x, y, 0, userFont.maxWidth);
               w = nativeFont.getAdvance(ch);
            }
            
            if (isVert)
               y += incY;
            else
               x += w;
         }

         x += extraPixelsPerChar + (i < extraPixelsRemaining ? 1 : 0);
      }

      if (isControlSurface)
         needsUpdate = true;
   }

   public void drawRoundRect(int x, int y, int width, int height, int r)
   {
      x += TRANSX;
      y += TRANSY;

      int maxW = width >> 1, maxH = height >> 1;
      if (r > maxW || r > maxH)
         r = Math.min(maxW, maxH); // guich@200b4_6: correct bug that crashed the device.

      r = r << 1; // r = 2r
      g.setColor(foreColor);
      g.drawRoundRect(x, y, width, height, r, r);
      
      if (isControlSurface)
         needsUpdate = true;
   }

   public void fillRoundRect(int x, int y, int width, int height, int r)
   {
      x += TRANSX;
      y += TRANSY;

      int maxW = width >> 1, maxH = height >> 1;
      if (r > maxW || r > maxH)
         r = Math.min(maxW, maxH); // guich@200b4_6: correct bug that crashed the device.

      r = r << 1; // r = 2r
      g.setColor(backColor);
      g.fillRoundRect(x, y, width, height, r, r);
      if (isControlSurface) needsUpdate = true;
   }

   public void setClip(int x, int y, int w, int h)
   {
      int clipX1 = x + TRANSX;
      int clipY1 = y + TRANSY;
      int clipX2 = clipX1 + w;
      int clipY2 = clipY1 + h;

      if (clipX1 < minX) clipX1 = minX;
      if (clipY1 < minY) clipY1 = minY;
      if (clipX1 > maxX) clipX1 = maxX;
      if (clipY1 > maxY) clipY1 = maxY;

      if (clipX2 < minX) clipX2 = minX;
      if (clipY2 < minY) clipY2 = minY;
      if (clipX2 > maxX) clipX2 = maxX;
      if (clipY2 > maxY) clipY2 = maxY;

      if (clipX2 > Settings.screenWidth)  clipX2 = Settings.screenWidth;
      if (clipY2 > Settings.screenHeight) clipY2 = Settings.screenHeight;

      w = clipX2 - clipX1;
      h = clipY2 - clipY1;

      synchronized (this)
      {
         g.popContext();
         g.pushContext(clipX1, clipY1, w, h, 0, 0);
      }
   }

   public void setClip(Rect r)
   {
      setClip(r.x, r.y, r.width, r.height);
   }

   public Rect getClip(Rect r)
   {
      XYRect rect = g.getClippingRect();
      r.x = rect.x - TRANSX;
      r.y = rect.y - TRANSY;
      r.width = rect.width;
      r.height = rect.height;

      return r;
   }

   public boolean clip(Rect r)
   {
      XYRect rect = grect;
      rect.set(r.x + TRANSX, r.y + TRANSY, r.width, r.height);
      rect.intersect(g.getClippingRect());
      
      if (rect.width <= 0 || rect.height <= 0)
         return false;
      else
      {
         r.x = rect.x - TRANSX;
         r.y = rect.y - TRANSY;
         r.width = rect.width;
         r.height = rect.height;
         
         return true;
      }
   }

   public void clearClip()
   {
      int w = maxX - minX;
      int h = maxY - minY;

      synchronized (this)
      {
         g.popContext();
         g.pushContext(minX, minY, w, h, 0, 0);
      }
   }

   public void translate(int dx, int dy)
   {
      TRANSX += dx;
      TRANSY += dy;
   }

   public void copyRect(GfxSurface surface, int x, int y, int width, int height, int dstX, int dstY)
   {
      Bitmap b = (Bitmap)((surface instanceof Image) ? ((Image)surface).getPixels() : mainWindowPixels);
      if (b != null)
         drawSurface(b, x,y,width,height, dstX,dstY, drawOp, backColor, foreColor, true, surface.getWidth(), surface.getHeight(), surface instanceof Image ? ((Image)surface).useAlpha : false);
   }

   public void free()
   {
      g = null;
      bitmap = null;
   }

   public void setFont(Font font)
   {
      setFont(font, false);
   }
   
   private void setFont(Font font, boolean forceRefresh)
   {
      if (font != this.font || forceRefresh)
      {
         userFont = (Launcher4B.UserFont)font.hv_UserFont;
         nativeFont = (net.rim.device.api.ui.Font)userFont.nativeFont;
         
         if (nativeFont == null) // font is a TC font
         {
            fontChars = (FontCharCache)fontCharCaches.get(font);
            if (fontChars == null)
            {
               fontChars = new FontCharCache(font);
               fontCharCaches.put(font, this.fontChars);
            }
         }
         else // font is a native font
         {
            fontChars = null;
            g.setFont((net.rim.device.api.ui.Font)nativeFont);
         }

         this.font = font;
      }
   }

   public Coord getTranslation()
   {
      return new Coord(TRANSX,TRANSY);
   }

   public void drawHatchedRect(int x, int y, int width, int height, boolean top, boolean bottom)
   {
      x += TRANSX;
      y += TRANSY;

      int x2 = x + width - 1;
      int y2 = y + height - 1;

      g.setColor(foreColor);
      if (top && bottom)
      {
         g.drawLine(x, y + 2, x, y2 - 2); // left
         g.drawLine(x + 2, y2, x2 - 2, y2); // bottom
         g.drawLine(x2, y + 2, x2, y2 - 2); // right
         g.drawLine(x + 2, y, x2 - 2, y); // top
         g.drawPoint(x + 1, y + 1); // top left
         g.drawPoint(x2 - 1, y + 1); // top right
         g.drawPoint(x + 1, y2 - 1); // bottom left
         g.drawPoint(x2 - 1, y2 - 1); // bottom right
      }
      else if (top && !bottom)
      {
         g.drawLine(x, y + 2, x, y2); // left
         g.drawLine(x, y2, x2, y2); // bottom
         g.drawLine(x2, y + 2, x2, y2); // right
         g.drawLine(x + 2, y, x2 - 2, y); // top
         g.drawPoint(x + 1, y + 1); // top left
         g.drawPoint(x2 - 1, y + 1); // top right
      }
      else if (!top && bottom)
      {
         g.drawLine(x, y, x, y2 - 2); // left
         g.drawLine(x + 2, y2, x2 - 2, y2); // bottom
         g.drawLine(x2, y, x2, y2 - 2); // right
         g.drawLine(x, y, x2, y); // top
         g.drawPoint(x + 1, y2 - 1); // bottom left
         g.drawPoint(x2 - 1, y2 - 1); // bottom right
      }
      
      if (isControlSurface)
         needsUpdate = true;
   }

   public void fillHatchedRect(int x, int y, int width, int height, boolean top, boolean bottom)
   {
      x += TRANSX;
      y += TRANSY;

      int x2 = x + width - 1;
      int y2 = y + height - 1;

      g.setColor(backColor);
      if (top && bottom)
      {
         g.fillRect(x, y + 2, width, height - 4); // middle
         g.drawLine(x + 2, y, x2 - 2, y); // 1st line
         g.drawLine(x + 1, y + 1, x2 - 1, y + 1); // 2nd line
         g.drawLine(x + 1, y2 - 1, x2 - 1, y2 - 1); // last-1 line
         g.drawLine(x + 2, y2, x2 - 2, y2); // last line
      }
      else if (top && !bottom)
      {
         g.fillRect(x, y + 2, width, height - 2); // middle
         g.drawLine(x + 2, y, x2 - 2, y); // 1st line
         g.drawLine(x + 1, y + 1, x2 - 1, y + 1); // 2nd line
      }
      else if (!top && bottom)
      {
         g.fillRect(x, y, width, height - 2); // middle
         g.drawLine(x + 1, y2 - 1, x2 - 1, y2 - 1); // last-1 line
         g.drawLine(x + 2, y2, x2 - 2, y2); // last line
      }
      
      if (isControlSurface)
         needsUpdate = true;
   }

   public void drawHighLightFrame(int x, int y, int w, int h, int topLeftColor, int bottomRightColor, boolean yMirror)
   {
      x += TRANSX;
      y += TRANSY;

      int x2 = x + w - 1;
      int y2 = y + h - 1;

      if (topLeftColor >= 0)
      {
         g.setColor(topLeftColor);
         g.drawLine(x, y, x, y2);
         if (!yMirror)
            g.drawLine(x, y, x2, y);
         else
            g.drawLine(x, y2, x2, y2);
      }

      if (bottomRightColor >= 0)
      {
         g.setColor(bottomRightColor);
         g.drawLine(x2, y2, x2, y);
         if (!yMirror)
            g.drawLine(x2, y2, x, y2);
         else
            g.drawLine(x2, y, x, y);
      }
      
      if (isControlSurface)
         needsUpdate = true;
   }

   public void drawRoundGradient(int startX, int startY, int endX, int endY, int topLeftRadius, int topRightRadius, int bottomLeftRadius, int bottomRightRadius,int startColor, int endColor, boolean vertical)
   {
      if (startX > endX)
      {
         int temp = startX;
         startX = endX;
         endX = temp;
      }
      if (startY > endY)
      {
         int temp = startY;
         startY = endY;
         endY = temp;
      }

      startX += TRANSX;
      startY += TRANSY;

      endX += TRANSX;
      endY += TRANSY;

      int numSteps = Math.max(1,vertical ? (endY - startY) : (endX - startX)); // guich@tc110_11: support horizontal gradient - guich@gc114_41: prevent div by 0 if numsteps is 0
      int startRed = (startColor >> 16) & 0xFF;
      int startGreen = (startColor >> 8) & 0xFF;
      int startBlue = startColor & 0xFF;
      int endRed = (endColor >> 16) & 0xFF;
      int endGreen = (endColor >> 8) & 0xFF;
      int endBlue = endColor & 0xFF;
      int redInc = ((endRed - startRed) << 16) / numSteps;
      int greenInc = ((endGreen - startGreen) << 16) / numSteps;
      int blueInc = ((endBlue - startBlue) << 16) / numSteps;
      int red = startRed << 16;
      int green = startGreen << 16;
      int blue = startBlue << 16;
      int leftOffset=0;
      int rightOffset=0;
      boolean hasRadius = (topLeftRadius+topRightRadius+bottomLeftRadius+bottomRightRadius) > 0;
      for (int i = 0; i < numSteps; i++)
      {
         if (hasRadius)
         {
            leftOffset = rightOffset = 0;

            if (topLeftRadius > 0 && i < topLeftRadius)
               leftOffset = getOffset(topLeftRadius,topLeftRadius - i - 1) - 1;
            else
            if (bottomLeftRadius > 0 && i > numSteps - bottomLeftRadius)
               leftOffset = getOffset(bottomLeftRadius,bottomLeftRadius - (numSteps - i + 1)) - 1;

            if (topRightRadius > 0 && i < topRightRadius)
               rightOffset = getOffset(topRightRadius,topRightRadius - i - 1) - 1;
            else
            if (bottomRightRadius > 0 && i > numSteps - bottomRightRadius)
               rightOffset = getOffset(bottomRightRadius,bottomRightRadius - (numSteps - i + 1)) - 1;
            if (leftOffset < 0) leftOffset = 0;
            if (rightOffset < 0) rightOffset = 0;
         }

         g.setColor((red & 0xFF0000) | ((green>>8) & 0x00FF00) | ((blue >> 16) & 0xFF));
         if (vertical)
            g.drawLine(startX + leftOffset, startY+i, endX - rightOffset, startY+i);
         else
            g.drawLine(startX+i, startY + leftOffset, startX+i, endY - rightOffset);

         red += redInc;
         green += greenInc;
         blue += blueInc;
      }
   }

   private static int getOffset(int radius, int y)
   {
      return radius - (int)Math.sqrt(radius * radius - y * y);
   }

   public void drawImage(totalcross.ui.image.Image image, int x, int y, int drawOp, int backColor, boolean doClip)
   {
      int transpPixel = image.transparentColor;
      if (backColor >= 0 && drawOp != DRAW_PAINT)
         transpPixel = backColor;

      Bitmap b = (Bitmap) image.getPixels();
      if (b != null)
         drawSurface(b, 0,0, image.getWidth(), image.getHeight(), x,y, drawOp, transpPixel, this.foreColor, doClip, image.getWidth(), image.getHeight(), image.useAlpha);
   }

   public void copyImageRect(totalcross.ui.image.Image src, int x, int y, int width, int height, int drawOp, int backColor, boolean doClip)
   {
      int transpPixel = src.transparentColor;
      if (backColor >= 0 && drawOp != DRAW_PAINT) // this same cache is used in the pda
         transpPixel = backColor;

      Bitmap b = (Bitmap) src.getPixels();
      if (b != null)
         drawSurface(b, x,y, width, height, 0,0, drawOp, transpPixel, this.foreColor, doClip, src.getWidth(), src.getHeight(), src.useAlpha);
   }

   public void drawImage(totalcross.ui.image.Image4B image, int x, int y)
   {
      copyRect(image, 0, 0, image.getWidth(),image.getHeight(), x, y);
   }

   public void setDrawOp(int drawOp)
   {
      this.drawOp = drawOp;
   }

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

   public void drawVistaRect(int x, int y, int width, int height, int topColor, int rightColor, int bottomColor, int leftColor) // guich@573_6
   {
      x += TRANSX;
      y += TRANSY;

      int x1 = x + 1;
      int y1 = y + 1;
      int x2 = x + width - 1;
      int y2 = y + height - 1;

      g.setColor(topColor);
      g.drawLine(x1, y, x2 - 1, y);
      g.setColor(rightColor);
      g.drawLine(x2, y1, x2, y2 - 1);
      g.setColor(bottomColor);
      g.drawLine(x1, y2, x2 - 1, y2);
      g.setColor(leftColor);
      g.drawLine(x, y1, x, y2 - 1);
      
      if (isControlSurface)
         needsUpdate = true;
   }

   private static Hashtable htVistaColors = new Hashtable(83);

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

   public void fillVistaRect(int x, int y, int width, int height, int backColor, boolean invert, boolean rotate) // guich@573_6
   {
      x += TRANSX;
      y += TRANSY;

      int[] vistaColors = getVistaColors(backColor);
      int dim = rotate ? width : height;
      int y0 = rotate ? x : y;
      int hh = rotate ? x+dim : y+dim;
      dim <<= 16;
      int incY = dim/10;
      int lineH = (incY>>16)+1;
      int lineY=0;

      for (int c = 0; lineY < dim; c++, lineY += incY)
      {
         g.setColor(vistaColors[invert ? 10 - c : c]);
         int yy = y0+(lineY>>16);
         int k = hh - yy;
         if (!rotate)
            g.fillRect(x, yy, width, k < lineH ? k : lineH);
         else
            g.fillRect(yy, y, k < lineH ? k : lineH, height);
      }
      
      if (isControlSurface)
         needsUpdate = true;
   }

   public void fillShadedRect(int x, int y, int width, int height, boolean invert, boolean rotate, int c1, int c2, int factor) // guich@573_6
   {
      int dim = rotate ? width : height, dim0 = dim;
      int y0 = rotate ? x : y;
      int hh = rotate ? x+dim : y+dim;
      dim <<= 16;
      if (height == 0) return;
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
   
   public void draw3dRect(int x, int y, int width, int height, byte type, boolean yMirror, boolean simple, int []fourColors)
   {
      if (type == R3D_SHADED)
      {
         boolean menu = simple; // is menu?
         drawLine(menu ? 0 : 1, 0, menu ? width - 1 : width - 3, 0);
         drawLine(0, 1, 0, height - 3);
         drawLine(width - 2, 1, width - 2, height - 3);
         drawLine(width - 1, menu ? 1 : 2, width - 1, height - 3);
         drawLine(1, height - 2, width - 2, height - 2);
         drawLine(2, height - 1, width - 3, height - 1);
      }
      else
      {
         int fC = foreColor, bC = backColor;
         switch (Settings.uiStyle)
         {
            case Settings.WinCE:
               switch (type)
               {
                  case R3D_EDIT:
                     drawHighLightFrame(x, y, width, height, fourColors[2], fourColors[1], yMirror);
                     if (!simple)
                        drawHighLightFrame(x + 1, y + 1, width - 2, height - 2, fourColors[3], fourColors[0], yMirror);
                     break;
                  case R3D_LOWERED:
                     drawHighLightFrame(x, y, width, height, fourColors[3], fourColors[0], yMirror);
                     if (!simple)
                        drawHighLightFrame(x + 1, y + 1, width - 2, height - 2, fourColors[2], fourColors[1], yMirror);
                     break;
                  case R3D_RAISED:
                     drawHighLightFrame(x, y, width, height, fourColors[0], fourColors[3], yMirror);
                     if (!simple)
                        drawHighLightFrame(x + 1, y + 1, width - 2, height - 2, fourColors[1], fourColors[2], yMirror);
                     break;
                  case R3D_CHECK:
                     drawHighLightFrame(x, y, width, height, fourColors[2], fourColors[1], yMirror);
                     if (!simple)
                        drawHighLightFrame(x + 1, y + 1, width - 2, height - 2, fourColors[3], fourColors[0], yMirror);
                     break;
               }
               break;
            case Settings.PalmOS:
               foreColor = fourColors[2];
               switch (type)
               {
                  case R3D_CHECK:
                     drawRect(x, y, width, height);
                     break;
                  case R3D_EDIT:
                     int h = height - 1;
                     drawDots(x, y + h, x + width, y + h);
                     break;
                  case R3D_LOWERED:
                     backColor = fourColors[1]; // dont move it from here!
                     if (simple)
                        fillRect(x, y, width, height);
                     else
                        fillHatchedRect(x, y, width, height, true, true); // no break; here!
                  case R3D_RAISED:
                     if (simple)
                        drawRect(x, y, width, height);
                     else
                        drawHatchedRect(x, y, width, height, true, true);
                     break;
               }
               break;
            case Settings.Flat:
               foreColor = fourColors[2];
               switch (type)
               {
                  case R3D_CHECK:
                     drawRect(x, y, width, height);
                     break;
                  case R3D_EDIT:
                     drawRect(x, y, x + width, y + height);
                     break;
                  case R3D_LOWERED:
                     backColor = fourColors[1]; // dont move it from here!
                     fillRect(x, y, width, height);
                  case R3D_RAISED:
                     drawRect(x, y, width, height);
                     break;
               }
               break;
            case Settings.Android:
            case Settings.Vista:
               foreColor = fourColors[2];
               switch (type)
               {
                  case R3D_CHECK:
                     backColor = fourColors[0];
                     drawRect(x, y, width, height);
                     break;
                  case R3D_EDIT:
                     drawRect(x, y, x + width, y + height);
                     break;
                  case R3D_RAISED:
                     drawVistaRect(x, y, width, height, fourColors[1], fourColors[1], fourColors[2], fourColors[3]);
                     break;
                  case R3D_LOWERED:
                     drawVistaRect(x, y, width, height, fourColors[2], fourColors[1], fourColors[1], fourColors[3]);
                     break;
               }
               break;
         }

         foreColor = fC;
         backColor = bC;
      }
      
      if (isControlSurface)
         needsUpdate = true;
   }

   public String toString()
   {
      Rect r = new Rect();
      getClip(r);
      return super.toString()+", Clip:"+r+", translation from origin: "+getTranslation();
   }

   public void drawArrow(int x, int y, int h, byte type, boolean pressed, int foreColor)
   {
      x += TRANSX;
      y += TRANSY;

      g.setColor(foreColor); // guich@300_11: now use getCursorColor so it can behave correctly in bright foregrounds
      if (pressed)
      {
         x++;
         y++;
      }
      int step = 1;
      if (type == ARROW_RIGHT || type == ARROW_LEFT)
      {
         if (type == ARROW_LEFT)
         {
            x += h - 1;
            step = -1;
         }
         h--;
         while (h >= 0)
         {
            g.drawLine(x, y, x, y + (h << 1));
            x += step;
            y++;
            h--;
         }
      }
      else
      {
         if (type == ARROW_UP)
         {
            y += h - 1;
            step = -1;
         }
         h--;
         while (h >= 0)
         {
            g.drawLine(x, y, x + (h << 1), y);
            y += step;
            x++;
            h--;
         }
      }

      if (isControlSurface)
         needsUpdate = true;
   }
   
   public int getRGB(int[] data, int offset, int x, int y, int w, int h)
   {
      x += TRANSX;
      y += TRANSY;
      
      XYRect rect = grect;
      rect.set(x, y, w, h);
      rect.intersect(g.getClippingRect());
      
      x = rect.x;
      y = rect.y;
      w = rect.width;
      h = rect.height;
      
      if (w <= 0 || h <= 0)
         return 0;
      else
      {
         bitmap.getARGB(data, offset, w, x, y, w, h);
         return w * h;
      }
   }
   
   public int setRGB(int[] data, int offset, int x, int y, int w, int h)
   {
      x += TRANSX;
      y += TRANSY;
      
      XYRect rect = grect;
      rect.set(x, y, w, h);
      rect.intersect(g.getClippingRect());
      
      x = rect.x;
      y = rect.y;
      w = rect.width;
      h = rect.height;
      
      if (w <= 0 || h <= 0)
         return 0;
      else
      {
         g.drawRGB(data, offset, w, x, y, w, h);
         return w * h;
      }
   }

   public void drawCylindricShade(int startColor, int endColor, int x, int y, int w, int h)
   {
      int startX = x;
      int startY = y;
      int endX = startX+w;
      int endY = startY+h;
      int numSteps = Math.max(1,Math.min((endY - startY)/2, (endX - startX)/2)); // guich@tc110_11: support horizontal gradient - guich@gc114_41: prevent div by 0 if numsteps is 0
      int startRed = (startColor >> 16) & 0xFF;
      int startGreen = (startColor >> 8) & 0xFF;
      int startBlue = startColor & 0xFF;
      int endRed = (endColor >> 16) & 0xFF;
      int endGreen = (endColor >> 8) & 0xFF;
      int endBlue = endColor & 0xFF;
      int redInc = (((endRed - startRed)*2) << 16) / numSteps;
      int greenInc = (((endGreen - startGreen)*2) << 16) / numSteps;
      int blueInc = (((endBlue - startBlue)*2) << 16) / numSteps;
      int red = startRed << 16;
      int green = startGreen << 16;
      int blue = startBlue << 16;
      for (int i = 0; i < numSteps; i++)
      {
         int rr = (red+i*redInc >> 16) & 0xFFFFFF;     if (rr > endRed) rr = endRed;
         int gg = (green+i*greenInc >> 16) & 0xFFFFFF; if (gg > endGreen) gg = endGreen;
         int bb = (blue+i*blueInc >> 16) & 0xFFFFFF;   if (bb > endBlue) bb = endBlue;
         foreColor = backColor = (rr << 16) | (gg << 8) | bb;
         int sx = startX+i, sy = startY+i;
         drawRect(sx,sy,endX-i-sx,endY-i-sy);
         int ii = i-8;
         rr = (red+ii*redInc >> 16) & 0xFFFFFF;     if (rr > endRed) rr = endRed;
         gg = (green+ii*greenInc >> 16) & 0xFFFFFF; if (gg > endGreen) gg = endGreen;
         bb = (blue+ii*blueInc >> 16) & 0xFFFFFF;   if (bb > endBlue) bb = endBlue;
         foreColor = backColor = (rr << 16) | (gg << 8) | bb;
         int i2 = i/8;
         drawLine(sx-i2,sy+i2,sx+i2,sy-i2);
         sx = endX-i; drawLine(sx-i2,sy-i2,sx+i2,sy+i2);
         sy = endY-i; drawLine(sx-i2,sy+i2,sx+i2,sy-i2);
         sx = startX+i; drawLine(sx-i2,sy-i2,sx+i2,sy+i2);
      }
      if (Settings.screenBPP < 24) dither(startX, startY, endX-startX, endY-startY, -1);
   }

   /** Apply a 16-bit Floyd-Steinberg dithering on the give region of the surface.
    * Don't use dithering if Settings.screenBPP is not equal to 16, like on desktop computers.
    * @param ignoreColor Pass a color that should not be changed, like the transparent color of an Image, or -1 to dither all colors
    * @since TotalCross 1.53
    */
   public void dither(int x0, int y0, int w, int h, int ignoreColor)
   {
      x0 += TRANSX;
      y0 += TRANSY;
      
      XYRect rect = grect;
      rect.set(x0, y0, w, h);
      rect.intersect(g.getClippingRect());
      
      x0 = rect.x;
      y0 = rect.y;
      w = rect.width;
      h = rect.height;
      if (w <= 0 || h <= 0)
         return;
      int xf = x0+w;
      int yf = y0+h;
      // based on http://en.wikipedia.org/wiki/Floyd-Steinberg_dithering
      int[] buff1 = tempRowBuf1;
      int[] buff2 = tempRowBuf2;
      int oldR,oldG,oldB,newR,newG,newB,errR,errG,errB;
      for (int y = y0; y < yf; y++)
      {
         bitmap.getARGB(buff1, 0, w, 0, y, w, 1);
         if (y+1 < h)
            bitmap.getARGB(buff2, 0, w, 0, y+1, w, 1);
         for (int x = x0; x < xf; x++)
         {
            int p = buff1[x];
            if (p == ignoreColor) continue;
            // get current pixel values
            oldR = (p>>16) & 0xFF;
            oldG = (p>>8) & 0xFF;
            oldB = p & 0xFF;
            // convert to 565 component values
            newR = oldR >> 3 << 3; 
            newG = oldG >> 2 << 2;
            newB = oldB >> 3 << 3;
            // compute error
            errR = oldR-newR;
            errG = oldG-newG;
            errB = oldB-newB;
            // set new pixel
            buff1[x] = (p & 0xFF000000) | (newR<<16) | (newG<<8) | newB;

            addError(buff1, x+1, y ,w,h, errR,errG,errB,7,16);
            addError(buff2, x-1,y+1,w,h, errR,errG,errB,3,16);
            addError(buff2, x,  y+1,w,h, errR,errG,errB,5,16);
            addError(buff2, x+1,y+1,w,h, errR,errG,errB,1,16);
         }
         bitmap.setARGB(buff1, 0, w, 0, y, w, 1);
         if (y+1 < h)
            bitmap.setARGB(buff2, 0, w, 0, y+1, w, 1);
      }
   }

   private static void addError(int[] pixel, int x, int y, int w, int h, int errR, int errG, int errB, int j, int k)
   {
      if (x >= w || y >= h || x < 0) return;
      int i = x;
      int p = pixel[i];
      int r = (p>>16) & 0xFF;
      int g = (p>>8) & 0xFF;
      int b = p & 0xFF;
      r += j*errR/k;
      g += j*errG/k;
      b += j*errB/k;
      if (r > 255) r = 255; else if (r < 0) r = 0;
      if (g > 255) g = 255; else if (g < 0) g = 0;
      if (b > 255) b = 255; else if (b < 0) b = 0;
      pixel[i] = (p & 0xFF000000) | (r << 16) | (g << 8) | b;
   }

   /////////////////////////////////////////////////////////////////////////////////
   //                      I N T E R N A L      R O U T I N E S                  //
   /////////////////////////////////////////////////////////////////////////////////
   public static int getDeviceColor(int rgb)
   {
      int[] buf1 = aaInt;
      // Get real RGB color (using pixel 0,0)
      buf1[0] = rgb;
      aaG.setGlobalAlpha(0xFF);
      aaG.setColor(rgb);
      aaG.drawPoint(0, 0);
      getRGB(aaPixel, buf1, 0, 1, 0, 0, 1, 1,false);
      int realRgb = buf1[0];
      return realRgb;
   }

   private static void setRGB(Bitmap b, int[] buff, int off, int scanLength, int x, int y, int w, int h)
   {
      // Append transparency information
      for (int i = off, n = off + w * h; i < n; i ++)
         buff[i] |= 0xFF000000;

      b.setARGB(buff, off, scanLength, x, y, w, h);
   }

   private static void getRGB(Bitmap b, int[] buff, int off, int scanLength, int x, int y, int w, int h, boolean useAlpha)
   {
      b.getARGB(buff, off, scanLength, x, y, w, h);

      // Discard transparency information
      if (!useAlpha)
         for (int i = off, n = off + w * h; i < n; i ++)
            buff[i] &= 0x00FFFFFF;
   }

   ////////////////////   METHODS TAKED FROM THE TOTALCROSS VIRTUAL MACHINE //////////
   // copy the area x,y,width,height of the bitmap bmp with dimensions bmpW,bmpH to the (current active) screen location dstX,dstY
   private void drawSurface(Bitmap b, int x, int y, int width, int height, int dstX, int dstY, int drawOp, int backColor, int foreColor, boolean doClip, int bmpW, int bmpH, boolean useAlpha)
   {
      // petrus@450_7: revamp of the drawBitmap clipping algorithm
      int i, j;
      dstX += TRANSX;
      dstY += TRANSY;

      if (!doClip)
      {
         //| Although doClip is not set, we make sure that the area of the bitmap
         //| that we want to copy IS inside its area
         //| If doClip is set, then the clipping computation below ensures
         //| the sanity of the operation (and is quite as fast).
         //| DoClip is important for game sprites where programmers ensure that the
         //| sprite will never pass screen boundaries.
         if ((x <= -width) || (x >= bmpW) || (y <= -height) || (y >= bmpH))
            return;
      }
      else
      {
         // clip the source rectangle to the source surface
         if (x < 0)
         {
            width += x;
            dstX -= x;
            x = 0;
         }
         i = bmpW - x;
         if (width > i)
            width = i;
         if (y < 0)
         {
            height += y;
            dstY -= y;
            y = 0;
         }
         i = bmpH - y;
         if (height > i)
            height = i;

         /* clip the destination rectangle against the clip rectangle */
         XYRect rect = grect;
         rect.set(dstX, dstY, width, height);
         rect.intersect(g.getClippingRect());
         
         x += rect.x - dstX;
         dstX = rect.x;
         y += rect.y - dstY;
         dstY = rect.y;
         width = rect.width;
         height = rect.height;

         /* check the validity */
         if ((width <= 0) || (height <= 0))
            return;
      }

      int op = -1;
      switch (drawOp)
      {
         case DRAW_PAINT:           op = net.rim.device.api.ui.Graphics.ROP2_S; break;
         case DRAW_ERASE:           op = net.rim.device.api.ui.Graphics.ROP2_DSa; break;
         case DRAW_MASK:            op = net.rim.device.api.ui.Graphics.ROP2_DSna; break;
         case DRAW_INVERT:          op = net.rim.device.api.ui.Graphics.ROP2_DSo; break;
         case DRAW_OVERLAY:         op = net.rim.device.api.ui.Graphics.ROP2_DSx; break;
         case DRAW_PAINT_INVERSE:   op = net.rim.device.api.ui.Graphics.ROP2_Sn; break;
      }

      if (!useAlpha && op != -1)
         g.rop(op, dstX, dstY, width, height, b, x, y);
      else
      {
         int[] buf1 = tempRowBuf1;
         int[] buf2 = tempRowBuf2;
         // color manipulation
         for (j = 0; j < height; j++)
         {
            getRGB(b, buf2, 0, width, x, y + j, width, 1, useAlpha);
            getRGB(bitmap, buf1, 0, width, dstX, dstY + j, width, 1, false);

            if (useAlpha)
               for (i = width; --i >= 0;)
               {
                  int bmpPt = buf2[i];
                  int a = (bmpPt >>> 24) & 0xFF;
                  if (a == 0xFF)
                     buf1[i] = bmpPt;
                  else
                  if (a != 0)
                  {
                     int screenPt = buf1[i];
                     int br = (bmpPt >> 16) & 0xFF;
                     int bg = (bmpPt >> 8) & 0xFF;
                     int bb = (bmpPt     ) & 0xFF;
                     int sr = (screenPt >> 16) & 0xFF;
                     int sg = (screenPt >> 8 ) & 0xFF;
                     int sb = (screenPt      ) & 0xFF;
                     
                     int ma = 0xFF-a;
                     int cr = (a * br + ma * sr); cr = (cr+1 + (cr >> 8)) >> 8; // fast way to divide by 255
                     int cg = (a * bg + ma * sg); cg = (cg+1 + (cg >> 8)) >> 8;
                     int cb = (a * bb + ma * sb); cb = (cb+1 + (cb >> 8)) >> 8;
                     buf1[i] = (screenPt & 0xFF000000) | (cr << 16) | (cg << 8) | cb;
                  }
               }
            else
               switch (drawOp)
               {
                  case DRAW_SPRITE:
                     for (i = width; --i >= 0;)
                        if (buf2[i] != backColor)
                           buf1[i] = buf2[i];
                     break;
                  case DRAW_REPLACE_COLOR:
                     for (i = width; --i >= 0;)
                        if (buf2[i] != backColor)
                           buf1[i] = foreColor;
                     break;
                  case DRAW_SWAP_COLORS:
                     for (i = width; --i >= 0;)
                        if (buf2[i] == backColor)
                           buf1[i] = foreColor;
                        else if (buf2[i] == foreColor)
                           buf1[i] = backColor;
                     break;
               }

            g.drawRGB(buf1, 0, width, dstX, dstY + j, width, 1);
         }
      }
      
      if (isControlSurface)
         needsUpdate = true;
   }

   private void drawPolygon(int []xPoints1, int []yPoints1, int base1, int nPoints1, int []xPoints2, int []yPoints2, int nPoints2, int c)
   {
      int i;
      if (xPoints1 == null || yPoints1 == null || nPoints1 < 2)
         return;
      foreColor = c;
      for (i=1; i < nPoints1; i++)
         drawLine(xPoints1[base1+i-1],yPoints1[base1+i-1],xPoints1[base1+i],yPoints1[base1+i]);
      for (i=1; i < nPoints2; i++)
         drawLine(xPoints2[i-1],yPoints2[i-1],xPoints2[i],yPoints2[i]);
   }

   // draws a polygon. if the polygon is not closed, close it
   private void drawPolygon(int []xPoints, int []yPoints, int nPoints, int c, boolean close)
   {
      int i;
      if (nPoints < 2) return;

      int[] tx = new int[nPoints];
      int[] ty = new int[nPoints];

      for (i = 0; i < nPoints; i ++)
      {
         tx[i] += xPoints[i] + TRANSX;
         ty[i] += yPoints[i] + TRANSY;
      }

      xPoints = tx;
      yPoints = ty;

      g.setColor(c);
      for (i = 1; i < nPoints; i++)
         g.drawLine(xPoints[i-1], yPoints[i-1], xPoints[i], yPoints[i]);
      if (close)
      {
         nPoints--;
         if (xPoints[0] != xPoints[nPoints] || yPoints[0] != yPoints[nPoints])
            g.drawLine(xPoints[0], yPoints[0], xPoints[nPoints], yPoints[nPoints]);
      }
      
      if (isControlSurface)
         needsUpdate = true;
   }

   public void getAnglePoint(int xc, int yc, int rx, int ry, int angle, Coord out) // guich@300_41: fixed method signature
   {
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

   ////////////////////////////////////////////////////////////////////////////

   static class FontCharCache
   {
      private Vector[] table; // each char has a vector of bitmaps, one of each color
      private IntHashtable colorIndex; // each color has a specific index at char table
      private int charCount;
      private int colorCount;

      private Launcher4B.UserFont userFont;
      private int[] buffer;
      private Launcher4B.CharBits charBits = new Launcher4B.CharBits();

      public FontCharCache(Font font)
      {
         this.userFont = (UserFont)font.hv_UserFont;

         buffer = new int[userFont.maxWidth * userFont.maxHeight];
         charCount = userFont.lastChar - userFont.firstChar + 1;
         colorCount = 0;

         table = new Vector[charCount];
         colorIndex = new IntHashtable(32);
      }

      public synchronized Bitmap getChar(char ch, int color)
      {
         Launcher4B.UserFont uf = userFont;
         Bitmap b = null;
         Vector v = null;
         boolean validChar;

         int idxChar = ch - uf.firstChar; // get char vector index at table
         int idxColor = -2; // get color index at char vector

         if ((validChar = idxChar >= 0 && idxChar < charCount) && (v = table[idxChar]) != null && (idxColor = colorIndex.get(color, -1)) >= 0 && idxColor < v.size())
            b = (Bitmap)v.items[idxColor];
         
         if (b == null) // construct char bitmap
         {
            Launcher4B.CharBits bits = charBits;
            int[] buf = buffer;

            uf.setCharBits(ch, bits);

            int width = bits.width;
            int height = (byte)uf.maxHeight;
            
            b = new Bitmap(width, height);

            if (bits.offset != -1)
            {
               int ands8[] = ands8Mask;
               int start, startBit;
               int current, currentBit;
               byte[] bitmapTable = bits.charBitmapTable;
               int rowWIB = bits.rowWIB;
               int pos = 0;

               if (!uf.antialiased) // anti aliased?
               {
                  int opaque = color | 0xFF000000;
                  start = bits.offset >> 3;
                  startBit = bits.offset & 7;

                  // draws the char
                  for (int h = height; --h >= 0; )    // draw each row
                  {
                     current = start;
                     currentBit = startBit;
                     for (int w = width; --w >= 0; )  // draw each pixel
                     {
                        buf[pos++] = (bitmapTable[current] & ands8[currentBit]) != 0 ? opaque : 0;
                        if (++currentBit == 8) // finished this byte?
                        {
                           currentBit = 0; // reset counter
                           current++;      // increment current byte
                        }
                     }
                     
                     start += rowWIB;
                  }
               }
               else
               {
                  start = bits.offset >> 1;
                  boolean isNibbleStartingLow = (bits.offset & 1) == 1;
                  int transparency;

                  // draws the char
                  for (int h = height; --h >= 0; )    // draw each row
                  {
                     current = start;
                     boolean isLowNibble = isNibbleStartingLow;

                     for (int w = width; --w >= 0; )  // draw each pixel
                     {
                        transparency = isLowNibble ? (bitmapTable[current++] & 0xF) : ((bitmapTable[current] >> 4) & 0xF);
                        isLowNibble = !isLowNibble;
                        buf[pos++] = (transparency << 28) | (transparency << 24) | color;
                     }
                     
                     start += rowWIB;
                  }
               }

               b.setARGB(buf, 0, width, 0, 0, width, height); // store pixels

               if (idxColor == -2) // didn't try to get color index yet
                  idxColor = colorIndex.get(color, -1);
               if (idxColor == -1) // color does not exist in cache yet
                  colorIndex.put(color, (idxColor = colorCount++));

               // Add bitmap to cache
               if (validChar)
               {
                  if (v == null)
                     table[idxChar] = v = new Vector();
                  if (v.size() <= idxColor)
                     v.setSize(idxColor + 1);
                  
                  v.items[idxColor] = b; // set bitmap
               }
            }
         }
         return b;
      }
   }

   /////////////////////////////////////////////////////////////////////////////////////////////
   // guich@tc130: now stuff for Android ui style
   // inside points are negative values, outside points are positive values
   private static final int IN  = 0;
   private static final int OUT = 0x100;

   static final int[][][] windowBorderAlpha =
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


   private static int interpolate(int color1r, int color1g, int color1b, int color2, int factor)
   {
      int m = 255-factor;
      int color2r = (color2 >> 16) & 0xFF;
      int color2g = (color2 >>  8) & 0xFF;
      int color2b = (color2      ) & 0xFF;

      int r = (color1r*factor+color2r*m)/255;
      int g = (color1g*factor+color2g*m)/255;
      int b = (color1b*factor+color2b*m)/255;
      return (r << 16) | (g << 8) | b;
   }
   
   private static int interpolate(int color1r, int color1g, int color1b, int color2r, int color2g, int color2b, int factor)
   {
      int m = 255-factor;
      int r = (color1r*factor+color2r*m)/255;
      int g = (color1g*factor+color2g*m)/255;
      int b = (color1b*factor+color2b*m)/255;
      return (r << 16) | (g << 8) | b;
   }

   private void setPixel(int x, int y, int c)
   {
      foreColor = c;
      setPixel(x,y);
   }
   private void drawLine(int x1, int y1, int x2, int y2, int c)
   {
      foreColor = c;
      drawLine(x1,y1,x2,y2);
   }
   
   public void drawWindowBorder(int xx, int yy, int ww, int hh, int titleH, int footerH, int borderColor, int titleColor, int bodyColor, int footerColor, int thickness, boolean drawSeparators)
   {
      int kx, ky, a, c;
      
      if (thickness < 1) thickness = 1;
      else if (thickness > 3) thickness = 3;
      int [][]aa = windowBorderAlpha[thickness-1];
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
      
      // horizontal and vertical lines
      for (int i = 0; i < 3; i++)
      {
         a = aa[i][0];
         if (a == OUT || a == IN)
            continue;
         kx = x1l;
         ky = yy+i;
         c = getPixel(kx, ky);
         drawLine(kx,ky,x2r,yy+i,interpolate(borderColorR,borderColorG,borderColorB, c, a)); // top
         
         ky = y2-i;
         c = getPixel(kx, ky);
         drawLine(kx,ky,x2r,y2-i,interpolate(borderColorR,borderColorG,borderColorB, c, a)); // bottom
         
         kx = xx+i;
         ky = y1l;
         c = getPixel(kx, ky);
         drawLine(kx,ky,xx+i,y2r,interpolate(borderColorR,borderColorG,borderColorB, c, a)); // left

         kx = x2-i;
         c = getPixel(kx, ky);
         drawLine(kx,ky,x2-i,y2r,interpolate(borderColorR,borderColorG,borderColorB, c, a)); // right
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
                  setPixel(left,top,interpolate(borderColorR,borderColorG,borderColorB, titleColorR,titleColorG,titleColorB, -a));
               else
                  setPixel(left,top,interpolate(borderColorR,borderColorG,borderColorB, getPixel(left,top), a));
            }

            // top right
            a = aa[j][i];
            if (a != OUT)
            {
               if (a <= 0)
                  setPixel(right,top,interpolate(borderColorR,borderColorG,borderColorB, titleColorR,titleColorG,titleColorB, -a));
               else
                  setPixel(right,top,interpolate(borderColorR,borderColorG,borderColorB, getPixel(right,top), a));
            }            
            // bottom left
            a = aa[i][j];
            if (a != OUT)
            {
               if (a <= 0)
                  setPixel(left,bot,interpolate(borderColorR,borderColorG,borderColorB, footerColorR,footerColorG,footerColorB, -a));
               else
                  setPixel(left,bot,interpolate(borderColorR,borderColorG,borderColorB, getPixel(left,bot), a));
            }            
            // bottom right
            a = aa[6-i][j];
            if (a != OUT)
            {
               if (a <= 0)
                  setPixel(right,bot,interpolate(borderColorR,borderColorG,borderColorB, footerColorR,footerColorG,footerColorB, -a));
               else
                  setPixel(right,bot,interpolate(borderColorR,borderColorG,borderColorB, getPixel(right,bot), a));
            }
         }
      }
      // now fill text, body and footer
      int t0 = thickness <= 2 ? 2 : 3;
      int ty = t0 + yy;
      int rectX1 = xx+t0;
      int rectX2 = x2-t0;
      int rectW = ww-t0*2;
      int bodyH = hh - (titleH == 0 ? 7 : titleH) - (footerH == 0 ? 7 : footerH);
      // remove corners from title and footer heights
      titleH -= 7;  if (titleH < 0) titleH = 0;
      footerH -= 7; if (footerH < 0) footerH = 0;
      
      // text
      backColor = titleColor;
      fillRect(x1l,ty,x2r-x1l,7-t0);    ty += 7-t0;   // corners
      fillRect(rectX1,ty,rectW,titleH); ty += titleH; // non-corners
      // separator
      if (drawSeparators && titleH > 0 && titleColor == bodyColor)
         drawLine(rectX1,ty-1,rectX2,ty-1,interpolate(borderColorR,borderColorG,borderColorB,titleColorR,titleColorG,titleColorB,64));
      // body
      backColor = bodyColor;
      fillRect(rectX1,ty,rectW,bodyH); ty += bodyH;
      // separator
      if (drawSeparators && footerH > 0 && bodyColor == footerColor)
         {drawLine(rectX1,ty,rectX2,ty,interpolate(borderColorR,borderColorG,borderColorB,titleColorR,titleColorG,titleColorB,64)); ty++; footerH--;}
      // footer
      backColor = footerColor;
      fillRect(rectX1,ty,rectW,footerH); ty += footerH; // non-corners
      fillRect(x1l,ty,x2r-x1l,7-t0);                    // corners
   }
}
