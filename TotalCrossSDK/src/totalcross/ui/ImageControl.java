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

import totalcross.sys.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

/** A control that can show an image bigger than its area and that can be dragged using a pen to show the hidden parts.
  Note that, by default, events (and dragging) are disabled. You must call setEventsEnabled to allow dragging.
*/

public class ImageControl extends Control
{
   /** The amount to scroll when in penless mode. Defaults to 10. */
   public static int scrollValue = 10;
   private Image img,img0,imgBack;
   private int startX,startY;
   private Coord c = new Coord();
   private boolean isEventEnabled, canDrag;
   /** Set to true to center the image in the control when it is loaded */
   public boolean centerImage;

   /** The last position used for X and Y. */
   public int lastX,lastY;

   /** Change this member to set the border color.
    * You may also set it to -1 if you don't want a border color.
    * Note: starting on TotalCross 3.1, the border is drawn around the color and no longer around the image,
    * because it was not working on OpenGL devices.
    */
   public int borderColor = -1;
   /** Set to true to let the image be dragged beyond container limits. 
    * Should be false for open gl. */
   public boolean allowBeyondLimits;

   /** Dumb field to keep compilation compatibility with TC 1 */
   public int drawOp;
   
   /** Set to true to enable zooming in open gl devices. */
   public boolean hwScale = Settings.isOpenGL || Settings.onJavaSE;
   
   private static final double NOTEMP = Convert.MIN_DOUBLE_VALUE;
   /** Temporary values to set the hwScaleW/hwScaleH to during draw. */
   public double tempHwScale=NOTEMP;
   
   /** Set to true to scale the image to fit the bounds. */
   public boolean scaleToFit;

   /** Constructs an ImageControl using the given image. */
   public ImageControl(Image img)
   {
      this();
      setImage(img);
   }

   /** Constructs with no initial image. You must set the image with the setImage method. */
   public ImageControl()
   {
      isEventEnabled = false;
      focusTraversable = true;
   }

   /** Change this to true to enable dragging and events on the image. */
   public void setEventsEnabled(boolean enabled)
   {
      focusTraversable = isEventEnabled = enabled;
   }

   /** Sets the image to the given one. If the image size is different, you must explicitly call
    * setRect again if you want to resize the control.
    */
   public void setImage(Image img)
   {
      this.img = this.img0 = img;
      c.x = c.y = lastX = lastY = 0;
      tempHwScale=NOTEMP;
      // test if it is really loaded.
      if (img != null && getImageWidth() > 0)
      {
         if (scaleToFit)
            try
            {
               if (width < height)
                  this.img = Settings.enableWindowTransitionEffects ? img0.smoothScaledFixedAspectRatio(this.width,false) : img0.hwScaledFixedAspectRatio(this.width,false);
               else
                  this.img = Settings.enableWindowTransitionEffects ? img0.smoothScaledFixedAspectRatio(this.height,true) : img0.hwScaledFixedAspectRatio(this.height,true);
            }
            catch (ImageException e)
            {
               // keep original image
            }
         if (centerImage)
         {
            lastX = (width-getImageWidth())/2;
            lastY = (height-getImageHeight())/2;
         }
      }
      Window.needsPaint = true;
   }

   public void onEvent(Event event)
   {
      if (img == null || !isEventEnabled) // no images found, nothing to do!
         return;
      PenEvent pe;
      switch (event.type)
      {
         case MultiTouchEvent.SCALE:
            if (hwScale)
            {
               if (tempHwScale == NOTEMP)
                  tempHwScale = 1;
               double step = ((MultiTouchEvent)event).scale;
               double newScale = tempHwScale * step;
               if (newScale > 0)
               {
                  tempHwScale = newScale;
                  // always centers on screen
                  lastX = (width-getImageWidth())/2;
                  lastY = (height-getImageHeight())/2;
                  repaintNow();
               }
            }
            break;
         case KeyEvent.ACTION_KEY_PRESS:
            canDrag = !canDrag;
            break;
         case KeyEvent.SPECIAL_KEY_PRESS:
            KeyEvent ke = (KeyEvent)event;
            if (ke.isDownKey())
               moveTo(lastX, lastY - scrollValue);
            else
            if (ke.isUpKey())
               moveTo(lastX, lastY + scrollValue);
            else
            if (ke.key == SpecialKeys.RIGHT)
               moveTo(lastX - scrollValue, lastY);
            else
            if (ke.key == SpecialKeys.LEFT)
               moveTo(lastX + scrollValue, lastY);
            else
            if (ke.isActionKey())
               parent.setHighlighting();
            break;
         case PenEvent.PEN_DOWN:
            pe = (PenEvent)event;
            startX = pe.x-lastX; // save the start relative to the last point clicked
            startY = pe.y-lastY;
            break;
         case PenEvent.PEN_DRAG:
            if (getImageWidth() > this.width || getImageHeight() > this.height || allowBeyondLimits)
            {
               pe = (PenEvent)event;
               if (moveTo(pe.x-startX,pe.y-startY))
                  pe.consumed = true;               
            }
            break;
         case ControlEvent.FOCUS_OUT:
            canDrag = false;
            break;
      }
   }

   /** Moves to the given coordinates, respecting the current moving policy regarding <i>allowBeyondLimits</i>.
    * @return True if the image's position was changed. 
    */
   public boolean moveTo(int newX, int newY)
   {
      int lx = lastX;
      int ly = lastY;
      if (allowBeyondLimits)
      {
         lastX = newX;
         lastY = newY;
      }
      else
      {
         if (getImageWidth() > width)
            lastX = Math.max(width-getImageWidth(),Math.min(newX, 0)); // don't let it move the image beyond its bounds
         if (getImageHeight() > height)
            lastY = Math.max(height-getImageHeight(),Math.min(newY,0));
      }
      if (lx != lastX || ly != lastY)
      {
         repaintNow();
         return true;
      }
      return false;
   }

   protected void onBoundsChanged(boolean screenChanged)
   {
      translateFromOrigin(c);
      if (scaleToFit)
         try
         {
            if (width < height)
               this.img = Settings.enableWindowTransitionEffects ? img0.smoothScaledFixedAspectRatio(this.width,false) : img0.hwScaledFixedAspectRatio(this.width,false);
            else
               this.img = Settings.enableWindowTransitionEffects ? img0.smoothScaledFixedAspectRatio(this.height,true) : img0.hwScaledFixedAspectRatio(this.height,true);
         }
         catch (ImageException e)
         {
            // keep original image
         }
      if (centerImage && img != null) // guich@100_1: reset the image's position if bounds changed
      {
         lastX = (width-getImageWidth())/2;
         lastY = (height-getImageHeight())/2;
      }
      else lastX = lastY = 0;
   }

   private void fillBack(Graphics g)
   {
      if (imgBack != null)
         g.drawImage(imgBack,0,0, true);
   }

   public void onPaint(Graphics g)
   {
      paint(g, true);
   }

   private void paint(Graphics g, boolean drawBack)
   {
      g.backColor = isEnabled() ? backColor : Color.interpolate(backColor,parent.backColor);
      if (!transparentBackground) // guich@tc115_41
         g.fillRect(0,0,width,height);
      if (img != null) // images found?
      {
         double dw = img.hwScaleW, dh = img.hwScaleH;
         if (tempHwScale != NOTEMP)
            img.hwScaleW = img.hwScaleH = tempHwScale;
         if (allowBeyondLimits)
            g.drawImage(img, lastX,lastY, true);
         else
            g.copyRect(img,0,0,img.getWidth(),img.getHeight(),lastX,lastY);
         img.hwScaleW = dw; img.hwScaleH = dh;
      }
      if (drawBack)
         fillBack(g);
      if (borderColor != -1)
      {
         g.foreColor = borderColor;
         g.drawRect(0,0,width,height);
      }
   }

   /** Returns the image's width; when scaling, returns the scaled width. */
   public int getImageWidth()
   {
      return img == null ? 0 : tempHwScale != NOTEMP ? (int)(img.getWidth()*tempHwScale) : img.getWidth();
   }
   
   /** Returns the image's height; when scaling, returns the scaled height. */
   public int getImageHeight()
   {
      return img == null ? 0 : tempHwScale != NOTEMP ? (int)(img.getHeight()*tempHwScale) : img.getHeight();
   }
   
   public int getPreferredWidth()
   {
      return img != null ? Math.min(getImageWidth(),Settings.screenWidth) : imgBack != null ? imgBack.getWidth() : Settings.screenWidth; // guich@tc115_35
   }

   public int getPreferredHeight()
   {
      return img != null ? Math.min(getImageHeight(),Settings.screenHeight) : imgBack != null ? imgBack.getHeight() : Settings.screenHeight; // guich@tc115_35
   }

   /** Returns the current image assigned to this ImageControl. */
   public Image getImage()
   {
      return img;
   }

   /** Sets the given image as a freezed background of this image control. */
   public void setBackground(Image img)
   {
      imgBack = img;
   }

   /** Gets an image representing the portion being shown. If all image is being shown,
    * returns the currently assigned image. */
   public Image getVisibleImage(boolean includeBackground) throws ImageException
   {
      Rect rImg = new Rect(lastX, lastY, getImageWidth(), getImageHeight());
      Rect rArea = getRect();
      rArea.x = rArea.y = 0;
      Image ret = img;
      if (!rArea.contains(rImg.x, rImg.y) || !rArea.contains(rImg.x2(), rImg.y2()))
      {
         rImg.intersectWith(rArea);
         ret = new Image(rImg.width, rImg.height);
         Graphics g = ret.getGraphics();
         if (!includeBackground) paint(getGraphics(), false); // remove the background image
         g.copyRect(this, rImg.x, rImg.y, rImg.width, rImg.height, 0,0);
         if (!includeBackground) paint(getGraphics(), true);
      }
      return ret;
   }
   
   public Control handleGeographicalFocusChangeKeys(KeyEvent ke) // guich@tc111_9
   {
      if (!canDrag)
         return null;
      if (ke.isDownKey())
         moveTo(lastX, lastY - scrollValue);
      else
      if (ke.isUpKey())
         moveTo(lastX, lastY + scrollValue);
      else
      if (ke.key == SpecialKeys.RIGHT)
         moveTo(lastX - scrollValue, lastY);
      else
      if (ke.key == SpecialKeys.LEFT)
         moveTo(lastX + scrollValue, lastY);
      return this;
   }
}
