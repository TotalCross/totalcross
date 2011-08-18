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
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

/** This class adds a multi-button menu that can be scrolled horizontally (single-row) or vertically (multiple-rows),
 * using a scrollbar or flicking. 
 * 
 * The buttons can have almost all properties present in the Button class, like:
 * <ul>
 *  <li> textPosition
 *  <li> borderType
 *  <li> cornerRadius3DG
 *  <li> borderWidth3DG
 *  <li> borderColor3DG 
 *  <li> topColor3DG
 *  <li> bottomColor3DG
 * </ul>
 * There are also other properties that can be set, like:
 * <ul>
 *  <li> textGap
 *  <li> buttonVertGap
 *  <li> buttonHorizGap
 *  <li> imageSize
 *  <li> borderGap
 * </ul>
 * The sizes above are not in pixels, but in percentage of the font's height. So, a value of 25 means
 * 25% of the font's height, or 1/4; 150 means 150% of the font's height, or 1.5x; and so on.
 * This enabled the gaps be constant in physical inches no matter the screen DPI or resolution.
 * 
 * See the UIGadgets program for a sample of how to use this class.
 * 
 * @see #pagePositionDisposition
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
   private Spacer spacer;
   private PagePosition pagepos;
   
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

   /** Used in the pagePositionDisposition. Place it at bottom. */
   public final static int PAGEPOSITION_AT_BOTTOM = 0;
   /** Used in the pagePositionDisposition. Place it at top. */
   public final static int PAGEPOSITION_AT_TOP = 1;
   /** Used in the pagePositionDisposition. Don't use a PagePosition, use the ScrollPosition instead. */
   public final static int NO_PAGEPOSITION = 2;
   
   /** If disposition is a MULTIPLE_HORIZONTAL, set how the PagePosition will replace the ScrollPosition control.
    * @see #PAGEPOSITION_AT_BOTTOM
    * @see #PAGEPOSITION_AT_TOP
    * @see #NO_PAGEPOSITION
    */
   public int pagePositionDisposition; // default at bottom

   // fields that will be passed to all buttons created.
   
   /** Where to place the text (supports only LEFT, TOP, RIGHT, BOTTOM, CENTER - no adjustments!). 
    * Also supports RIGHT_OF (relativeToText is computed automatically). Defaults to BOTTOM. */
   public int textPosition = CENTER;
   /** @see Button#setBorder(byte) */
   public byte borderType = Button.BORDER_3D;
   /** @see Button#cornerRadius3DG */
   public int cornerRadius3DG = 10;
   /** @see Button#borderWidth3DG */
   public int borderWidth3DG = 2;
   /** @see Button#borderColor3DG */
   public int borderColor3DG = 0x00108A;
   /** @see Button#topColor3DG */
   public int topColor3DG = 0xDCDCFF;
   /** @see Button#bottomColor3DG */
   public int bottomColor3DG = Color.BLUE;
   
   /** @see Button#setPressedColor(int) */
   public int pressedColor = -1;
   
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
         Button btn = btns[i] = new Button(name, img, textPosition, tg);
         btn.relativeToText = relativeToText;
         btn.appId = i;
         btn.addPressListener(this);
         btn.borderColor3DG = borderColor3DG;
         btn.setBorder(borderType); // setBorder uses borderColor3DG and resets the other values to default.
         btn.borderColor3DG = borderColor3DG;
         btn.cornerRadius3DG = cornerRadius3DG;
         btn.borderWidth3DG = borderWidth3DG;
         btn.topColor3DG = topColor3DG;
         btn.bottomColor3DG = bottomColor3DG;
         if (pressedColor != -1) 
            btn.setPressedColor(pressedColor);
         btn.setFont(this.font);
         
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
      if (super.sbH != null && super.sbH instanceof ScrollPosition) ((ScrollPosition)super.sbH).barColor = pressedColor != -1 ? pressedColor : foreColor;
      if (super.sbV != null && super.sbV instanceof ScrollPosition) ((ScrollPosition)super.sbV).barColor = pressedColor != -1 ? pressedColor : foreColor;
      if (prefBtnW == 0) onFontChanged();
      if (btns != null && btns[0].parent == this) // if button was already added to this container, remove it (may occur during rotation
         for (int i = btns.length; --i >= 0;) remove(btns[i]);
      if (spacer != null)
      {
         remove(spacer);
         spacer = null;
      }
      if (pagepos != null)
      {
         pagepos.parent.remove(pagepos);
         pagepos = null;
      }
      
      int n = images != null ? images.length : names.length;
      int bh = fmH*buttonHorizGap/100;
      int bv = fmH*buttonVertGap/100;
      int imageW0 = prefBtnW;
      int imageH0 = prefBtnH;
      if (height-bv < imageH0+bv)
         bv += imageH0-height;
      if (width-bh < imageW0+bh)
         bh += imageW0-width;
      int imageW  = imageW0 + bh;
      int imageH  = imageH0 + bv;
      int pageW = width-bh;
      int pageH = height-bv;
      int cols = disposition == SINGLE_COLUMN ? 1 : pageW / imageW;
      imageW = pageW / cols;
      cols = disposition == SINGLE_ROW ? n : pageW / imageW;
      imageW0 = imageW - bh;
      int pages = 0;
      int colsPerPage = (width-bh)/imageW;
      int rowsPerPage = (height-bv)/imageH;
      if (rowsPerPage == 0)
         rowsPerPage = 1;
      if (disposition == MULTIPLE_HORIZONTAL)
      {
         int rows = pageH / imageH;
         if (rows == 0)
            rows = 1;
         cols = n / rows;
         if ((n % rows) != 0)
            cols++;
         pages = cols / colsPerPage;
         if ((cols % colsPerPage) != 0)
            pages++;
      }
      
      int x = LEFT+bh,x0=x;
      int y = TOP+bv;
      int maxX2=0;
      int difX = width - (bh + imageW * colsPerPage);
      int difY = height -(bv + imageH * rowsPerPage);
      int itemsPerPage = rowsPerPage * colsPerPage;
      
      int divX = difX / colsPerPage;
      int remX = difX % colsPerPage;
      int divY = difY / rowsPerPage;
      int remY = difY % rowsPerPage;
      for (int i = 0,c=0; i < n; i++)
      {
         // make sure that the page has the exact width and height
         if (disposition == MULTIPLE_HORIZONTAL)
            x += (i % colsPerPage) == 0 ? divX + remX : divX;
         else
         if (disposition == MULTIPLE_VERTICAL)
            y += (i % colsPerPage) == 0 ? (i % itemsPerPage) == 0 ? divY + remY : divY : 0;
         add(btns[i], x, y, imageW0, imageH0);
         int x2 = btns[i].x + btns[i].width;
         if (x2 > maxX2) maxX2 = x2;
         if (++c == cols)
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
      boolean isVertical = disposition == SINGLE_COLUMN || disposition == MULTIPLE_VERTICAL;
      // checks if there's enough space to fit all buttons in our height, and if there is, prevent it from scrolling
      int top = btns[0].y-1;
      int last = !isVertical ? n-1 : Math.min(n, colsPerPage * rowsPerPage) - 1; // in horizontal scroll, the last button is at the bottom of the screen (as all buttons at its same row). in vertical scroll, we use the last page's button
      int bot = btns[last].y + btns[last].height;
      if ((bot-top) < height)
      {
         // check how many space we have at top and bottom, and change the buttons y so they are centered vertically
         bot = height - bot; // how much it leaves at bottom?
         top = (bot-top) / 2;
         if (top != 0)
            for (int i = 0; i < n; i++)
               btns[i].y += top;
         if (disposition == SINGLE_COLUMN)
            return; // don't put a new spacer
      }
      else top = 0;
      boolean hasPagePosition = pagePositionDisposition != NO_PAGEPOSITION && disposition == MULTIPLE_HORIZONTAL && sbH != null && sbH instanceof ScrollPosition;
      if (isVertical)
      {
         int v = bv+top;
         if (v > 0)
            add(spacer = new Spacer(1,v),LEFT,AFTER);
      }
      else
      {
         int v = hasPagePosition ? bh + (maxX2%(width-bh)) : bh;
         if (v > 0)
            add(spacer = new Spacer(v,1),maxX2,TOP); // in a multipage, make sure that the last page has a full width
      }
      
      if (Settings.fingerTouch)
      {
         if (disposition == SINGLE_ROW || disposition == MULTIPLE_HORIZONTAL)
         {
            flick.setScrollDistance(width - bh);
            flick.forcedFlickDirection = Flick.HORIZONTAL_DIRECTION_ONLY;
            if (hasPagePosition)
            {
               pagepos = new PagePosition(Math.min(pages,7));
               pagepos.setCount(pages);
               addToSC(pagepos);
               pagepos.setRect(CENTER,pagePositionDisposition == PAGEPOSITION_AT_BOTTOM ? BOTTOM : TOP,PREFERRED,PREFERRED);
               flick.setPagePosition(pagepos);
               ((ScrollPosition)sbH).barColor = sbH.getBackColor(); // "hide" the bar
            }
         }
         else
         {
            flick.setScrollDistance(pageH);
            flick.setDistanceToAbortScroll(0); // we deliberably disable the scroll abort on vertical scrolls
            flick.forcedFlickDirection = Flick.VERTICAL_DIRECTION_ONLY;
         }
      }
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
