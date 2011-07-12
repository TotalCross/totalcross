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

package totalcross.ui;

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
class NinePatch
{
   static Lock imageLock = new Lock();
   static Image imgLT,imgT,imgRT,imgL,imgC,imgR,imgLB,imgB,imgRB; // left top right bottom
   final static int CORNER = 7;
   final static int SIDE = 1;
   
   private static Hashtable htBtn = new Hashtable(100); 
   private static Hashtable htPressBtn = new Hashtable(100); 
   private static StringBuffer sbBtn = new StringBuffer(25);

   private static void copyPixels(int[] buf, Image dst, Image src, int dstX, int dstY, int srcX, int srcY, int srcW, int srcH)
   {
      int y2 = srcY + srcH;
      Graphics gd = dst.getGraphics();
      Graphics gs = src.getGraphics();
      
      for (; srcY < y2; srcY++, dstY++)
      {
         gs.getRGB(buf, 0, srcX, srcY, srcW, 1);
         gd.setRGB(buf, 0, dstX, dstY, srcW, 1);
      }
   }
   
   private static Image getImageArea(int[] buf, Image orig, int x, int y, int w, int h) throws ImageException
   {
      Image img = new Image(w,h);
      img.useAlpha = orig.useAlpha;
      img.transparentColor = orig.transparentColor;
      copyPixels(buf,img, orig, 0,0, x,y,w,h); 
      return img;
   }
   
   static
   {
      try
      {
         Image original = new Image("totalcross/res/button.png");
         int w = original.getWidth();
         int h = original.getHeight();
         int[] buf = new int[w > h ? w : h];
         imgLT = getImageArea(buf, original, 0,0,CORNER,CORNER);
         imgRT = getImageArea(buf, original, w-CORNER,0,CORNER,CORNER);
         imgLB = getImageArea(buf, original, 0,h-CORNER,CORNER,CORNER);
         imgRB = getImageArea(buf, original, w-CORNER,h-CORNER,CORNER,CORNER);
         imgT  = getImageArea(buf, original, CORNER,0,w-CORNER*2,CORNER);
         imgB  = getImageArea(buf, original, CORNER,h-CORNER,w-CORNER*2,CORNER);
         imgL  = getImageArea(buf, original, 0,CORNER,SIDE,h-CORNER*2);
         imgR  = getImageArea(buf, original, w-SIDE,CORNER,SIDE,h-CORNER*2);
         imgC  = getImageArea(buf, original, SIDE,CORNER,w-SIDE*2,h-CORNER*2);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e+" "+e.getMessage());
      }
   }
   
   public static Image getButtonImage(int width, int height, int color, boolean fromCache) throws ImageException
   {
      Image ret = null;
      synchronized (imageLock)
      {
         int hash = 0;
         if (fromCache)
         {
            sbBtn.setLength(0);
            hash = Convert.hashCode(sbBtn.append(width).append('|').append(height).append('|').append(color));
            ret = (Image)htBtn.get(hash);
         }
         if (ret == null)
         {
            int []buf = new int[width > height ? width : height];
            ret = new Image(width,height);
            ret.useAlpha = imgC.useAlpha;
            ret.transparentColor = imgC.transparentColor;
            Image c;
            // sides
            c = imgT.getScaledInstance(width-CORNER*2,CORNER); copyPixels(buf, ret, c, CORNER,0, 0,0,width-CORNER*2,CORNER);
            c = imgB.getScaledInstance(width-CORNER*2,CORNER); copyPixels(buf, ret, c, CORNER,height-CORNER, 0,0,width-CORNER*2,CORNER);
            c = imgL.getScaledInstance(SIDE,height-CORNER*2);  copyPixels(buf, ret, c, 0,CORNER, 0,0,SIDE,height-CORNER*2);
            c = imgR.getScaledInstance(SIDE,height-CORNER*2);  copyPixels(buf, ret, c, width-SIDE,CORNER, 0,0,SIDE,height-CORNER*2);
            // corners
            copyPixels(buf, ret, imgLT, 0,0, 0,0,CORNER,CORNER);
            copyPixels(buf, ret, imgRT, width-CORNER, 0,0,0,CORNER,CORNER);
            copyPixels(buf, ret, imgLB, 0,height-CORNER,0,0,CORNER,CORNER);
            copyPixels(buf, ret, imgRB, width-CORNER,height-CORNER,0,0,CORNER,CORNER);
            // center
            c = imgC.getScaledInstance(width-SIDE*2,height-CORNER*2); // smoothscale generates a worst result because it enhances the edges
            copyPixels(buf, ret, c, SIDE,CORNER, 0,0,width-SIDE*2,height-CORNER*2);
            if (Settings.screenBPP == 16) 
               ret.dither();
            ret.applyColor2(color);
            if (fromCache)
               htBtn.put(hash, ret);
         }
      }
      return ret;
   }
   
   public static Image getPressedInstance(Image img, int backColor, int pressColor, boolean fromCache) throws ImageException
   {
      Image pressed = null;
      sbBtn.setLength(0);
      int hash = 0;
      if (fromCache)
      {
         hash = Convert.hashCode(sbBtn.append(img).append('|').append(backColor).append('|').append(pressColor));
         pressed = (Image)htPressBtn.get(hash);
      }
      if (pressed == null)
      {
         if (pressColor != -1)
         {
            pressed = img.getFrameInstance(0); // get a copy of the image
            pressed.applyColor(pressColor); // colorize as red
         }
         else pressed = img.getTouchedUpInstance(Color.getAlpha(backColor) > (256-32) ? (byte)-64 : (byte)32,(byte)0);
         if (fromCache)
            htPressBtn.put(hash, pressed);
      }
      return pressed;
   }
}

