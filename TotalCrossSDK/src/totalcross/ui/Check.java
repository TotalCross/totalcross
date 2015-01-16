/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

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
   private boolean checked;
   private int cbColor, cfColor;
   private int fourColors[] = new int[4];
   private String []lines = Label.emptyStringArray;
   private int []linesW;
   private int lastASW;
   private String originalText;
   /** Set to true to left-justify the text in the control. The default is right-justified,
    * if the control's width is greater than the preferred one.
    * @since TotalCross 1.0
    * @deprecated Now the align is always at left
    */
   public boolean leftJustify;
   
   /** Sets the text color of the check. Defaults to the foreground color. 
    * @since TotalCross 2.0.
    */
   public int textColor = -1;
   
   /** Set to the color of the check, if you want to make it different of the foreground color.
    * @since TotalCross 1.3
    */
   public int checkColor = -1;
   
   /** Set to true to let the Check split its text based on the width every time its width
    * changes. If the height is PREFERRED, the Label will change its size accordingly.
    * You may change the height again calling setRect.
    * @since TotalCross 1.14
    */
   public boolean autoSplit; // guich@tc114_74

   /** Creates a check control displaying the given text. */
   public Check(String text)
   {
      setText(text);
   }

   /** Called by the system to pass events to the check control. */
   public void onEvent(Event event)
   {
      if (event.target != this || !isEnabled()) return;
      switch (event.type)
      {
         case KeyEvent.ACTION_KEY_PRESS:
            checked = !checked;
            repaintNow();
            postPressedEvent();
            break;
         default: 
            if (!isActionEvent(event))
               break;
            PenEvent pe = (PenEvent)event;
            if (isInsideOrNear(pe.x,pe.y))
            {
               Window.needsPaint = true;
               checked = !checked;
               postPressedEvent();
            }
            break;
      }
   }

   /** Sets the text that is displayed in the check. */
   public void setText(String text)
   {
      originalText = text;
      this.text = text;
      lines = text.equals("") ? new String[]{""} : Convert.tokenizeString(text,'\n'); // guich@tc100: now we use \n
      onFontChanged();
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
      setChecked(checked,Settings.sendPressEventOnChange); 
   }
   /** Sets the checked state of the control, and send the press event if desired. */
   public void setChecked(boolean checked, boolean sendPress)
   {
      if (this.checked != checked)
      {
         this.checked = checked;
         Window.needsPaint = true;
         if (sendPress)
            postPressedEvent();
      }
   }

   /** Returns the maximum text width for the lines of this Label. */
   public int getMaxTextWidth()
   {
      int w = 0;
      for (int i =lines.length-1; i >= 0; i--)
         if (linesW[i] > w) // guich@450_36: why call stringWidth if linesW has everything?
            w = linesW[i];
      return w;
   }

   /** returns the preffered width of this control. */
   public int getPreferredWidth()
   {
      return getMaxTextWidth() + fmH+Edit.prefH+2;
   }

   /** returns the preffered height of this control. */
   public int getPreferredHeight()
   {
      return fmH*lines.length+Edit.prefH;
   }

   protected void onColorsChanged(boolean colorsChanged)
   {
      cbColor = UIColors.sameColors ? backColor : Color.brighter(getBackColor()); // guich@572_15
      cfColor = getForeColor();
      if (!uiAndroid) Graphics.compute3dColors(isEnabled(),backColor,foreColor,fourColors);
   }

   /** Called by the system to draw the check control. */
   public void onPaint(Graphics g)
   {
      boolean enabled = isEnabled();
      int wh = lines.length == 1 ? height : fmH+Edit.prefH;
      int xx,yy;

      // guich@200b4_126: repaint the background of the whole control
      g.backColor = backColor;
      if (!transparentBackground)
         g.fillRect(0,0,width,height);
      // square paint
      if (!uiAndroid && uiVista && enabled) // guich@573_6
         g.fillVistaRect(0,0,wh,wh,cbColor,true,false);
      else
      if (!uiAndroid || !transparentBackground)
      {
         g.backColor = uiAndroid ? backColor : cbColor;
         g.fillRect(0,0,wh,wh); // guich@220_28
      }
      if (uiAndroid)
         try 
         {
            NinePatch.tryDrawImage(g, enabled ? Resources.checkBkg.getNormalInstance(wh,wh,foreColor) : Resources.checkBkg.getDisabledInstance(wh,wh,foreColor),0,0);
            if (checked)
               NinePatch.tryDrawImage(g,Resources.checkSel.getPressedInstance(wh,wh,backColor,checkColor != -1 ? checkColor : foreColor,enabled),0,0);
         } catch (ImageException ie) {}
      else
         g.draw3dRect(0,0,wh,wh,Graphics.R3D_CHECK,false,false,fourColors); // guich@220_28
      g.foreColor = checkColor != -1 ? checkColor : uiAndroid ? foreColor : cfColor;

      if (!uiAndroid && checked)
         paintCheck(g, fmH, wh);
      // draw label
      yy = (this.height - fmH*lines.length) >> 1;
      xx = wh+2; // guich@300_69
      g.foreColor = textColor != -1 ? (enabled ? textColor : Color.interpolate(textColor,backColor)) : cfColor;
      for (int i =0; i < lines.length; i++,yy+=fmH)
         g.drawText(lines[i], xx, yy, textShadowColor != -1, textShadowColor);
   }

   /** Paints a check in the given coordinates. The g must have been translated to destination x,y coordinates.
    * @since SuperWaba 5.5
    * @param g The desired Graphics object where to paint. The forecolor must already be set.
    * @param fmH The fmH member
    * @param height The height of the control. The check will be vertical aligned based on this height.
    */
   public static void paintCheck(Graphics g, int fmH, int height) // guich@550_29
   {
      if (uiAndroid)
         try 
         {
            g.drawImage(Resources.checkSel.getPressedInstance(height,height,0,g.foreColor,true),0,0);
         } 
         catch (ImageException ie) // just paint something 
         {
            g.backColor = g.foreColor;
            g.fillRect(0,0,height,height);
         }
      else
      {
         int wh = height;
         int m = 2*wh/5;
         int yy = m;
         int xx = 3;
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
   }
   /** Clears this control, checking it if clearValueInt is 1. */
   public void clear() // guich@572_19
   {
      setChecked(clearValueInt == 1);
   }

   /** Splits the text to the given width. Remember to set the font (or add the Label to its parent) 
    * before calling this method.
    * @since TotalCross 1.14
    * @see #autoSplit
    */
   public void split(int maxWidth) // guich@tc114_73
   {
      String text = originalText; // originalText will be changed by setText
      setText(Convert.insertLineBreak(maxWidth, fm, text)); // guich@tc126_18: text cannot be assigned here or originalText will be overwritten
      originalText = text;
   }

   protected void onFontChanged()
   {
      int i;
      if (linesW == null || linesW.length != lines.length) // guich@450_36: avoid keep recreating the int array
         linesW = new int[lines.length];
      int []linesW = this.linesW; // guich@450_36: use local var
      for (i = lines.length-1; i >= 0; i--)
         linesW[i] = fm.stringWidth(lines[i]);
   }

   protected void onBoundsChanged(boolean screenChanged)
   {
      if (autoSplit && this.width > 0 && this.width != lastASW) // guich@tc114_74 - guich@tc120_5: only if PREFERRED was choosen in first setRect - guich@tc126_35
      {
         lastASW = this.width;
         int wh = lines.length == 1 ? height : fmH+Edit.prefH;
         split(this.width-wh-2);
         if (PREFERRED-RANGE <= setH && setH <= PREFERRED+RANGE) 
            setRect(KEEP,KEEP,KEEP,getPreferredHeight() + setH-PREFERRED);
      }
   }

}
