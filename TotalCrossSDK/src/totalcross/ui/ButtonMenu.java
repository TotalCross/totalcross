/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2001-2011 SuperWaba Ltda.                                      *
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
import totalcross.ui.image.*;

/** This class adds an image bar that can be scrolled horizontally (single-row) or vertically (multiple-rows),
 * using arrows or flicking. 
 * @since TotalCross 1.3
 */

public class ButtonMenu extends ScrollContainer implements PressListener
{
   private Button[] btns;
   private Image[] images;
   private String[] names;
   private int disposition;
   private int prefBtnW,prefBtnH;
   private int selected;
   
   /** The gap between the image and the text, in percentage of the font's height.
    * Defaults to 25 (%).
    */
   public int textGap=25;
   
   /** The gap between two vertical buttons, in percentage of the font's height.
    * Also used as gap between the button and the ButtonMenu's border.
    * Defaults to 100 (%).
    */
   public int buttonVertGap=100;
   
   /** The gap between two horizontal buttons, in percentage of the font's height.
    * Also used as gap between the button and the ButtonMenu's border.
    * Defaults to 100 (%).
    */
   public int buttonHorizGap=100;
   
   /** The size of the image, in percentage of the font's height.
    * Defaults to 200 (%). Set to -1 to keep the original size.
    */
   public int imageSize=200;
   
   /** The gap between the text or image and the button borders, in percentage of the font's height. Defaults to 10 (%). */
   public int borderGap = 10;

   // fields that will be passed to all buttons created.
   
   /** Where to place the text (supports only LEFT, TOP, RIGHT, BOTTOM, CENTER - no adjustments!). 
    * Also supports RIGHT_OF (relativeToText is computed automatically). Defaults to BOTTOM. */
   public int textPosition = BOTTOM;
   /** @see Button#setBorder(byte) */
   public byte borderType = Button.BORDER_NONE;
   /** @see Button#cornerRadius3DG */
   public int cornerRadius3DG;
   /** @see Button#borderWidth3DG */
   public int borderWidth3DG;
   /** @see Button#borderColor3DG */
   public int borderColor3DG;
   /** @see Button#topColor3DG */
   public int topColor3DG;
   /** @see Button#bottomColor3DG */
   public int bottomColor3DG;
   
   /** Used in the disposition member of the constructor. The menu will have a single column and multiple rows and will scroll vertically. */
   public static final int SINGLE_COLUMN = 1;
   /** Used in the disposition member of the constructor. The menu will have a single row and multiple columns and will scroll horizontally. */
   public static final int SINGLE_ROW = 2;
   /** Used in the disposition member of the constructor. The menu will have multiple columns and rows and will scroll horizontally. */
   public static final int MULTIPLE_HORIZONTAL = 3;
   /** Used in the disposition member of the constructor. The menu will have multiple columns and rows and will scroll vertically. */
   public static final int MULTIPLE_VERTICAL = 4;

   /** Constructs an ButtonMenu with the giving images and no names.
    * @see #SINGLE_COLUMN
    * @see #SINGLE_ROW
    * @see #MULTIPLE_HORIZONTAL
    * @see #MULTIPLE_VERTICAL
    */
   public ButtonMenu(Image[] images, int disposition)
   {
      this(images,null,disposition);
   }
   
   /** Constructs an ButtonMenu with the giving names and no images.
    * @see #SINGLE_COLUMN
    * @see #SINGLE_ROW
    * @see #MULTIPLE_HORIZONTAL
    * @see #MULTIPLE_VERTICAL
    */
   public ButtonMenu(String[] names, int disposition)
   {
      this(null,names,disposition);
   }
   
   /** Constructs an ButtonMenu with the giving images ant names.
    * @see #SINGLE_COLUMN
    * @see #SINGLE_ROW
    * @see #MULTIPLE_HORIZONTAL
    * @see #MULTIPLE_VERTICAL
    */
   public ButtonMenu(Image[] images, String[] names, int disposition)
   {
      super(disposition == SINGLE_ROW || disposition == MULTIPLE_HORIZONTAL, disposition == SINGLE_COLUMN || disposition == MULTIPLE_VERTICAL);
      this.images = images;
      this.names = names;
      this.disposition = disposition;
   }
   
   /** Creates and resizes all Button and images. For better performance, call setFont for this control BEFORE
    * calling add or setRect (this is a general rule for all other controls as well).
    */
   public void onFontChanged()
   {
      // compute the maximum width and height for the buttons.
      int n = images != null ? images.length : names.length;
      int imageS = fmH*imageSize/100;
      if (btns == null)
         btns = new Button[n];
      else
         for (int i = btns.length; --i >= 0;)
         {
            btns[i].removePressListener(this);
            btns[i] = null; // release memory
         }
      
      // if border type is RIGHT_OF, compute the biggest string
      String relativeToText = null;
      if (textPosition == RIGHT_OF && names != null)
      {
         int maxW = fm.stringWidth(relativeToText = names[0]);
         for (int i = names.length; --i >= 1;)
         {
            int m = fm.stringWidth(names[i]);
            if (m > maxW)
            {
               maxW = m;
               relativeToText = names[i];
            }
         }
      }
      
      prefBtnH = prefBtnW = 0;
      int tg = fmH*textGap/100;
      for (int i = n; --i >= 0;)
      {
         Image  img  = images == null ? null : images[i];
         String name = names  == null ? null : names[i];
         if (img != null && imageSize != -1 && img.getHeight() != imageS) // should we resize the image?
            try {img = img.getSmoothScaledInstance(img.getWidth()*imageS/img.getHeight(),imageS,img.transparentColor);} catch (ImageException ie) {} // just keep old image if there's no memory
         btns[i] = new Button(name, img, textPosition, tg);
         btns[i].cornerRadius3DG = cornerRadius3DG;
         btns[i].borderWidth3DG = borderWidth3DG;
         btns[i].borderColor3DG = borderColor3DG;
         btns[i].topColor3DG = topColor3DG;
         btns[i].bottomColor3DG = bottomColor3DG;
         btns[i].relativeToText = relativeToText;
         btns[i].appId = i;
         btns[i].addPressListener(this);
         btns[i].setBorder(borderType);
         btns[i].setFont(this.font);
         
         int pw = btns[i].getPreferredWidth();
         int ph = btns[i].getPreferredHeight();
         if (pw > prefBtnW) prefBtnW = pw;
         if (ph > prefBtnH) prefBtnH = ph;
      }
      prefBtnW += borderGap*fmH/100*2;
      prefBtnH += borderGap*fmH/100*2;
   }

   public void initUI()
   {
      if (super.sbH != null && super.sbH instanceof ScrollPosition) ((ScrollPosition)super.sbH).barColor = foreColor;
      if (super.sbV != null && super.sbV instanceof ScrollPosition) ((ScrollPosition)super.sbV).barColor = foreColor;
      if (prefBtnW == 0) onFontChanged();
      if (btns != null && btns[0].parent == this) // if button was already added to this container, remove it (may occur during rotation
         for (int i = btns.length; --i >= 0;) remove(btns[i]);
      
      int n = images != null ? images.length : names.length;
      int bh = fmH*buttonHorizGap/100;
      int bv = fmH*buttonVertGap/100;
      int imageW0 = prefBtnW;
      int imageW  = imageW0 + bh;
      int imageH0 = prefBtnH;
      int imageH  = imageH0 + bv;
      int ss = this.width-bh;
      int nn = disposition == SINGLE_COLUMN ? 1 : ss / imageW;
      imageW = ss / nn;
      nn = disposition == SINGLE_ROW ? n : ss / imageW;
      imageW0 = imageW - bh;
      if (disposition == MULTIPLE_HORIZONTAL)
      {
         ss = this.height-bv;
         int rows = ss / imageH;
         nn = n / rows;
         if ((n % rows) != 0)
            nn++;
         imageH = ss / rows;
         imageH0 = imageH - bv;
      }
      
      int x = LEFT+bh,x0=x;
      int y = TOP+bv;
      int maxX2=0;
      for (int i = 0,c=0; i < n; i++)
      {
         add(btns[i], x, y, imageW0,imageH0);
         int x2 = btns[i].x+btns[i].width;
         if (x2 > maxX2) maxX2 = x2;
         if (++c == nn)
         {
            x = x0;
            y = AFTER+bh;
            c = 0;
         }
         else
         {
            x = AFTER+bv;
            y = SAME;
         }
      }
      if (disposition == SINGLE_COLUMN || disposition == MULTIPLE_VERTICAL)
         add(new Spacer(1,bv),LEFT,AFTER);
      else
         add(new Spacer(bh,1),maxX2,TOP);
   }
   
   public void reposition()
   {
      super.reposition(false);
      initUI();
   }      

   /** Returns the preferred width as if all images were in a single row. */
   public int getPreferredWidth()
   {
      return getPreferredWidth(images.length);
   }
   
   /** Returns the preferred width for the given number of columns. */
   public int getPreferredWidth(int cols)
   {
      if (prefBtnW == 0) onFontChanged();
      int bh = fmH*buttonHorizGap/100;
      int sb = Settings.fingerTouch || !sbV.isVisible() ? 0 : sbV.getPreferredWidth();
      return prefBtnW * cols + bh * (cols-1) + bh*2 + sb;
   }
   
   /** Returns the preferred height as if all images were in a single row. */ 
   public int getPreferredHeight()
   {
      return getPreferredHeight(1);
   }
   
   /** Returns the preferred height for the given number of rows.
    * For example:
    * <pre>
    * add(ib2,LEFT+10,CENTER,FILL-10,ib2.getPreferredHeight(4));
    * </pre> 
    */
   public int getPreferredHeight(int rows)
   {
      if (prefBtnH == 0) onFontChanged();
      int bv = fmH*buttonVertGap/100;
      int sb = Settings.fingerTouch || !sbH.isVisible() ? 0 : sbH.getPreferredHeight(); 
      return prefBtnH * rows + bv*(rows-1) + bv*2 + sb;
   }
   
   public int getSelectedIndex()
   {
      return selected;
   }

   public void controlPressed(ControlEvent e)
   {
      e.consumed = true;
      if (!scrolled)
      {
         selected = ((Control)e.target).appId;
         postPressedEvent();
      }
   }
}
