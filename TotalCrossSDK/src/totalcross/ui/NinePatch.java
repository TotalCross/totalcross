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

package totalcross.ui;

import totalcross.res.*;
import totalcross.sys.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;
import totalcross.util.*;
import totalcross.util.concurrent.*;

/** NinePatch is a class that creates a button of any size by dividing a 
 * sample button into 9 parts: the 4 corners, the 4 sides, and the middle.
 * 
 * Corner are drawn unscaled, sides are resized in a single direction, and the middle
 * is resized, colorized and then dithered.
 * 
 * This class is thread-safe.
 * 
 * @since TotalCross 1.3
 */
public class NinePatch
{   
   public static final int BUTTON       = 0;
   public static final int EDIT         = 1;
   public static final int COMBOBOX     = 2;
   public static final int LISTBOX      = 3;
   public static final int MULTIEDIT    = 4;
   public static final int PROGRESSBARV = 5;
   public static final int PROGRESSBARH = 4;
   public static final int SCROLLPOSH   = 6;
   public static final int SCROLLPOSV   = 7;
   public static final int TAB          = 8;
   public static final int GRID         = 9;
   public static final int GRID_CAPTION = 1;
   
   static NinePatch instance;
   
   /** Defines if we will use the Image.applyColor (1) or Image.applyColor2 (2) algorithms
    * when getting the pressed instance. */ 
   public static int pressColorAlgorithm = 1;
   
   public static NinePatch getInstance()
   {
      return instance != null ? instance : (instance = new NinePatch());
   }
   
   private NinePatch()
   {
   }
   
   public class Parts
   {
      Image imgLT,imgT,imgRT,imgL,imgC,imgR,imgLB,imgB,imgRB; // left top right bottom
      int corner, side;
   }
   
   private Lock imageLock = new Lock();
   
   private Parts []parts = 
   {
      load(Resources.button,7,1), 
      load(Resources.edit,5,3), 
      load(Resources.combobox,5,2),
      load(Resources.listbox,5,3),
      load(Resources.multiedit,9,4), 
      load(Resources.progressbarv,9,4),
      load(Resources.scrollposh,3,2),
      load(Resources.scrollposv,3,2),
      load(Resources.tab,10,4),
      load(Resources.grid,5,3),
   };
   
   private Hashtable htBtn = new Hashtable(100); 
   private Hashtable htPressBtn = new Hashtable(100); 
   private StringBuffer sbBtn = new StringBuffer(25);

   private void copyPixels(int[] buf, Image dst, Image src, int dstX, int dstY, int srcX, int srcY, int srcW, int srcH)
   {
      int dstW = dst.getWidth();
      int dstH = dst.getHeight();
      if (srcW > dstW)
         srcW = dstW;
      if (srcH > dstH)
         srcH = dstH;
      
      int y2 = srcY + srcH;
      Graphics gd = dst.getGraphics();
      Graphics gs = src.getGraphics();
      
      for (; srcY < y2; srcY++, dstY++)
      {
         gs.getRGB(buf, 0, srcX, srcY, srcW, 1);
         gd.setRGB(buf, 0, dstX, dstY, srcW, 1);
      }
   }
   
   private Image getImageArea(int[] buf, Image orig, int x, int y, int w, int h) throws ImageException
   {
      Image img = new Image(w,h);
      copyPixels(buf,img, orig, 0,0, x,y,w,h); 
      return img;
   }
   
   public Parts load(Image original, int corner, int side)
   {
      try
      {
         int w = original.getWidth();
         int h = original.getHeight();
         int[] buf = new int[w > h ? w : h];
         Parts p = new Parts();
         p.corner = corner;
         p.side = side;
         p.imgLT = getImageArea(buf, original, 0,0,corner,corner);
         p.imgRT = getImageArea(buf, original, w-corner,0,corner,corner);
         p.imgLB = getImageArea(buf, original, 0,h-corner,corner,corner);
         p.imgRB = getImageArea(buf, original, w-corner,h-corner,corner,corner);
         p.imgT  = getImageArea(buf, original, corner,0,w-corner*2,corner);
         p.imgB  = getImageArea(buf, original, corner,h-corner,w-corner*2,corner);
         p.imgL  = getImageArea(buf, original, 0,corner,side,h-corner*2);
         p.imgR  = getImageArea(buf, original, w-side,corner,side,h-corner*2);
         p.imgC  = getImageArea(buf, original, side,corner,w-side*2,h-corner*2);
         return p;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e+" "+e.getMessage());
      }
   }

   public Image getNormalInstance(Parts p, int width, int height, int color, boolean rotate) throws ImageException
   {
      int []buf = new int[width > height ? width : height];
      Image ret = new Image(width,height);
      Image c;
      int side = p.side, s;
      int corner = p.corner;
      // sides
      s = width-corner*2;
      if (s > 0)
      {
         c = p.imgT.getScaledInstance(s,corner); copyPixels(buf, ret, c, corner,0, 0,0,s,corner);
         c = p.imgB.getScaledInstance(s,corner); copyPixels(buf, ret, c, corner,height-corner, 0,0,s,corner);
      }
      s = height-corner*2;
      if (s > 0)
      {
         c = p.imgL.getScaledInstance(side,s);  copyPixels(buf, ret, c, 0,corner, 0,0,side,s);
         c = p.imgR.getScaledInstance(side,s);  copyPixels(buf, ret, c, width-side,corner, 0,0,side,s);
      }
      // corners
      try
      {
         copyPixels(buf, ret, p.imgLT, 0,0, 0,0,corner,corner);
         copyPixels(buf, ret, p.imgRT, width-corner, 0,0,0,corner,corner);
         copyPixels(buf, ret, p.imgLB, 0,height-corner,0,0,corner,corner);
         copyPixels(buf, ret, p.imgRB, width-corner,height-corner,0,0,corner,corner);
      }
      catch (ArrayIndexOutOfBoundsException aioobe) {} // ignore error that comes sometimes in JavaSE
      // center
      if (width-side*2 > 0 && height-corner*2 > 0)
      {
         c = p.imgC.getScaledInstance(width-side*2,height-corner*2); // smoothscale generates a worst result because it enhances the edges
         copyPixels(buf, ret, c, side,corner, 0,0,width-side*2,height-corner*2);
      }
      if (Settings.screenBPP == 16)
         ret.getGraphics().dither(0,0,ret.getWidth(),ret.getHeight());
      if (color != -1)
         ret.applyColor2(color);
      if (rotate)
         ret = ret.getRotatedScaledInstance(100,180,-1);
      return ret;
   }
   
   // rotate is only used by TabbedContainer
   public Image getNormalInstance(int type, int width, int height, int color, boolean rotate) throws ImageException
   {
      Image ret = null;
      synchronized (imageLock)
      {
         int hash = 0;
         sbBtn.setLength(0);
         hash = Convert.hashCode(sbBtn.append(type).append('|').append(width).append('|').append(height).append('|').append(color).append('|').append(rotate));
         ret = (Image)htBtn.get(hash);
         if (ret == null)
         {
            ret = getNormalInstance(parts[type],width,height,color,rotate);
            htBtn.put(hash, ret);
         }
      }
      return ret;
   }
   
   public Image getPressedInstance(Image img, int backColor, int pressColor) throws ImageException
   {
      Image pressed = null;
      sbBtn.setLength(0);
      int hash = Convert.hashCode(sbBtn.append(img).append('|').append(backColor).append('|').append(pressColor));
      pressed = (Image)htPressBtn.get(hash);
      if (pressed == null)
         synchronized(imageLock)
         {
            if (pressColor != -1)
            {
               pressed = img.getFrameInstance(0); // get a copy of the image
               if (pressColorAlgorithm == 1)
                  pressed.applyColor(pressColor); // colorize it
               else
                  pressed.applyColor2(pressColor); // colorize it
            }
            else pressed = img.getTouchedUpInstance(Color.getAlpha(backColor) > (256-32) ? (byte)-64 : (byte)32,(byte)0);
            htPressBtn.put(hash, pressed);
         }
      return pressed;
   }
   
   public void flush()
   {
      htBtn.clear();
      htPressBtn.clear();
   }

   /** Used internally to prevent Out of Memory errors. */
   public static void tryDrawImage(Graphics g, Image npback, int x, int y)
   {
      try
      {
         if (npback != null)
            g.drawImage(npback, x,y);
      }
      catch (OutOfMemoryError oome)
      {
         getInstance().flush(); // release memory and try again
         g.drawImage(npback, x,y);
      }
   }
}

