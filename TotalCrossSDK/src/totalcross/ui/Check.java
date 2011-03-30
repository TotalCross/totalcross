/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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

// $Id: Check.java,v 1.24 2011-01-13 15:27:23 guich Exp $

package totalcross.ui;

import totalcross.ui.event.*;
import totalcross.ui.gfx.*;

/**
 * Check is a control with a box and a check inside of it when the state is checked.
 * <p>
 * Here is an example showing a check being used:
 *
 * <pre>
 * public class MyProgram extends MainWindow
 * {
 *    Check check;
 *
 *    public void initUI()
 *    {
 *       add(check = new Check("Check me"), LEFT, AFTER);
 *    }
 *
 *    public void onEvent(Event event)
 *    {
 *       if (event.type == ControlEvent.PRESSED && event.target == check)
 *       {
 *          bool checked = check.isChecked();
 *          ... handle check being pressed
 * </pre>
 */

public class Check extends Control
{
   private String text;
   //Font font; removed by guich
   private boolean checked;
   private int cbColor, cfColor;
   private int fourColors[] = new int[4];
   private int textW;
   /** Set to true to left-justify the text in the control. The default is right-justified,
    * if the control's width is greater than the preferred one.
    * @since TotalCross 1.0
    */
   public boolean leftJustify;

   /** Creates a check control displaying the given text. */
   public Check(String text)
   {
      this.text = text;
      textW = fm.stringWidth(text);
   }

   /** Called by the system to pass events to the check control. */
   public void onEvent(Event event)
   {
      if (event.target != this) return;
      switch (event.type)
      {
         case KeyEvent.ACTION_KEY_PRESS: // guich@550_15
            checked = !checked;
            repaintNow();
            postPressedEvent();
            break;
         case PenEvent.PEN_DOWN:
            checked = !checked;
            Window.needsPaint = true;
            PenEvent pe = (PenEvent)event;
            if (isInsideOrNear(pe.x,pe.y))
               postPressedEvent();
            break;
      }
   }

   /** Sets the text that is displayed in the check. */
   public void setText(String text)
   {
      this.text = text;
      textW = fm.stringWidth(text);
      Window.needsPaint = true;
   }
   /** Gets the text displayed in the check. */
   public String getText()
   {
      return text;
   }

   /** Returns the checked state of the control. */
   public boolean isChecked()
   {
      return checked;
   }

   /** Sets the checked state of the control. */
   public void setChecked(boolean checked)
   {
      if (this.checked != checked)
      {
	      this.checked = checked;
	      Window.needsPaint = true;
      }
   }

   /** returns the preffered width of this control. */
   public int getPreferredWidth()
   {
      return /*fm.stringWidth(text)*/textW+getPreferredHeight() + 2;
   }

   /** returns the preffered height of this control. */
   public int getPreferredHeight()
   {
      if (fmH == 11)
         return uiCE ? (fmH+2) : fmH;
      return fm.ascent;
   }

   protected void onColorsChanged(boolean colorsChanged)
   {
      cbColor = UIColors.sameColors ? backColor : Color.brighter(getBackColor()); // guich@572_15
      cfColor = getForeColor();
      Graphics.compute3dColors(enabled,backColor,foreColor,fourColors);
   }

   protected void onFontChanged()
   {
      textW = fm.stringWidth(text);
   }

   /** Called by the system to draw the check control. */
   public void onPaint(Graphics g)
   {
      int wh = height;
      int xx,yy;

      // guich@200b4_126: repaint the background of the whole control
      g.backColor = backColor;
      if (!transparentBackground)
         g.fillRect(0,0,width,height);
      // square paint
      if (uiVista && enabled) // guich@573_6
         g.fillVistaRect(0,0,wh,wh,cbColor,true,false);
      else
      {
         g.backColor = cbColor;
         g.fillRect(0,0,wh,wh); // guich@220_28
      }
      g.draw3dRect(0,0,wh,wh,Graphics.R3D_CHECK,false,false,fourColors); // guich@220_28
      g.foreColor = cfColor;

      if (checked)
         paintCheck(g, fmH, height);
      // draw label
      yy = (this.height - fmH) >> 1;
      xx = leftJustify ? (wh+2) : (this.width - textW); // guich@300_69
      g.backColor = backColor; // guich@400_40: in wince the back color is used as the text background, so we restore it.
      g.drawText(text, xx, yy, textShadowColor != -1, textShadowColor);
   }

   /** Paints a check in the given coordinates. The g must have been translated to destination x,y coordinates.
    * @since SuperWaba 5.5
    * @param g The desired Graphics object where to paint. The forecolor must already be set.
    * @param fmH The fmH member
    * @param height The height of the control. The check will be vertical aligned based on this height.
    */
   public static void paintCheck(Graphics g, int fmH, int height) // guich@550_29
   {
      int wh = height;
      int m = (wh >> 1) - 1;
      if (fmH >= 22) m--;
      int yy = m - (fmH == 15 ? 1 : 0);
      int xx = uiPalm ? 2 : 3;
      wh -= xx;
      if (fmH <= 10) // guich@tc110_18
      {
         g.backColor = g.foreColor;
         g.fillRect(2,2,wh+xx-4,wh+xx-4);
      }
      else
      for (int i = xx; i < wh; i++)
      {
         g.drawLine(xx, yy, xx, yy + 2);
         xx++;
         if (i < m)
            yy++;
         else
            yy--;
      }
   }
   /** Clears this control, checking it if clearValueInt is 1. */
   public void clear() // guich@572_19
   {
      setChecked(clearValueInt == 1);
   }

}
