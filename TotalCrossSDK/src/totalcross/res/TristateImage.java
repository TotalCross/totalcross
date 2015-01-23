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

package totalcross.res;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;
import totalcross.util.*;

/** An image that has three states: normal, pressed and disabled.
 * 
 * Used in the Android user interface style.
 * 
 * @since TotalCross 1.3
 */

public class TristateImage
{
   private Hashtable htNormal = new Hashtable(5);
   private Hashtable htPressed = new Hashtable(5);
   private Hashtable htDisabled = new Hashtable(5);
   private Image base;
   
   public TristateImage(String name) throws ImageException, IOException
   {
      base = new Image(name);
   }

   public void flush()
   {
      htNormal.clear();
      htPressed.clear();
      htDisabled.clear();
   }
   
   public Image getNormalInstance(int width, int height, int backColor) throws ImageException
   {
      return getNormalInstance(width, height, backColor, true);
   }
   
   private Image getNormalInstance(int width, int height, int backColor, boolean apply) throws ImageException
   {
      int hash;
      StringBuffer sb = new StringBuffer(20);
      hash = Convert.hashCode(sb.append((width << 16) | height).append('|').append(backColor));
      Image ret = (Image)htNormal.get(hash);
      if (ret == null)
      {
         ret = scaleTo(width,height);
         ret.applyColor(backColor);
         htNormal.put(hash, ret);
      }
      return ret;
   }
   
   public Image getDisabledInstance(int width, int height, int backColor) throws ImageException
   {
      int hash;
      StringBuffer sb = new StringBuffer(20);
      hash = Convert.hashCode(sb.append((width << 16) | height).append('|').append(backColor));
      Image ret = (Image)htDisabled.get(hash);
      if (ret == null)
         htDisabled.put(hash, ret = getNormalInstance(width,height,backColor,false).getFadedInstance());
      return ret;
   }
   
   public Image getPressedInstance(int width, int height, int backColor, int pressColor, boolean enabled) throws ImageException
   {
      int hash;
      StringBuffer sb = new StringBuffer(20);
      hash = Convert.hashCode(sb.append((width << 16) | height).append('|').append(backColor).append(enabled).append(pressColor));
      Image ret = (Image)htPressed.get(hash);
      if (ret == null)
      {         
         if (pressColor != -1)
         {
            ret = scaleTo(width,height);
            ret.applyColor2(pressColor);
         }
         else 
            ret = getNormalInstance(width,height,backColor,false).getTouchedUpInstance(Color.getAlpha(backColor) > (256-32) ? (byte)-64 : (byte)32,(byte)0);
         if (!enabled)
            ret = ret.getFadedInstance();
         htPressed.put(hash, ret);
      }
      return ret;
   }
   
   private Image scaleTo(int w, int h) throws ImageException
   {
      Image img = base.getSmoothScaledInstance(w,h);
      if (img == base) // if image's width/height are the same of w/h
         img = base.getFrameInstance(0);
      return img;
   }
}
