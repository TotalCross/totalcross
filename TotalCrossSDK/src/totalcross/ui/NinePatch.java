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
import totalcross.util.concurrent.*;

public class NinePatch extends Control
{
   static Lock imageLock = new Lock();
   static Image imgLT,imgT,imgRT,imgL,imgC,imgR,imgLB,imgB,imgRB; // left top right bottom
   static int corner = 7;
   static int side = 1;
   Image background;
   boolean colorSet, boundSet;
   
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
         Image original = new Image("button.png");
         int w = original.getWidth();
         int h = original.getHeight();
         int[] buf = new int[w > h ? w : h];
         imgLT = getImageArea(buf, original, 0,0,corner,corner);
         imgRT = getImageArea(buf, original, w-corner,0,corner,corner);
         imgLB = getImageArea(buf, original, 0,h-corner,corner,corner);
         imgRB = getImageArea(buf, original, w-corner,h-corner,corner,corner);
         imgT  = getImageArea(buf, original, corner,0,w-corner*2,corner);
         imgB  = getImageArea(buf, original, corner,h-corner,w-corner*2,corner);
         imgL  = getImageArea(buf, original, 0,corner,side,h-corner*2);
         imgR  = getImageArea(buf, original, w-side,corner,side,h-corner*2);
         imgC  = getImageArea(buf, original, side,corner,w-side*2,h-corner*2);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e+" "+e.getMessage());
      }
   }
   
   String text;
   
   public NinePatch(String text)
   {
      this.text = text;
   }
   
   public void onColorsChanged(boolean state)
   {
      colorSet = true;
      if (!transparentBackground && colorSet && boundSet)
         setImage();
   }
   
   public void onBoundsChanged(boolean screenChanged)
   {
      boundSet = true;
      if (!transparentBackground && !screenChanged && colorSet && boundSet)
         setImage();
   }
   
   private void setImage()
   {
      synchronized (imageLock)
      {
         try
         {
            int []buf = new int[width > height ? width : height];
            background = new Image(width,height);
            background.useAlpha = imgC.useAlpha;
            background.transparentColor = imgC.transparentColor;
            Image c;
            // sides
            c = imgT.getScaledInstance(width-corner*2,corner); copyPixels(buf, background, c, corner,0, 0,0,width-corner*2,corner);
            c = imgB.getScaledInstance(width-corner*2,corner); copyPixels(buf, background, c, corner,height-corner, 0,0,width-corner*2,corner);
            c = imgL.getScaledInstance(side,height-corner*2);  copyPixels(buf, background, c, 0,corner, 0,0,side,height-corner*2);
            c = imgR.getScaledInstance(side,height-corner*2);  copyPixels(buf, background, c, width-side,corner, 0,0,side,height-corner*2);
            // corners
            copyPixels(buf, background, imgLT, 0,0, 0,0,corner,corner);
            copyPixels(buf, background, imgRT, width-corner, 0,0,0,corner,corner);
            copyPixels(buf, background, imgLB, 0,height-corner,0,0,corner,corner);
            copyPixels(buf, background, imgRB, width-corner,height-corner,0,0,corner,corner);
            // center
            c = imgC.getScaledInstance(width-side*2,height-corner*2); // smoothscale generates a worst result because it enhances the edges
            copyPixels(buf, background, c, side,corner, 0,0,width-side*2,height-corner*2);
            if (Settings.screenBPP == 16) 
               background.dither();
            background.applyColor2(backColor);
         }
         catch (Exception e)
         {
            e.printStackTrace();
            background = null;
         }
      }
   }
   
   public void onPaint(Graphics g)
   {
      if (background != null)
         g.drawImage(background,0,0);
      g.foreColor = foreColor;
      g.drawText(text, (width-fm.stringWidth(text))/2,(height-fmH)/2, true, backColor);
   }
}
