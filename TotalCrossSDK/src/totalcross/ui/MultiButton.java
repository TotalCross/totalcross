package totalcross.ui;

import totalcross.sys.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

/** MultiButton is a control that displays a single line button with a set of titles.
 * Can be used to replace a Check (with on/off) or a Radio (with their options).
 * Sample:
 * <pre>
 * MultiButton b = new MultiButton(new String[]{"+","-"});
   b.setBackColor(Color.CYAN);
   add(b,LEFT+50,TOP+50,200,fmH*3/2);
 * </pre>
 * @since TotalCross 2.0
 */

public class MultiButton extends Control
{
   private Image npback,npsel;
   private String[] tits;
   private int sel=-1;
   private boolean []disabled;
   
   /** The color used to divide the texts. */
   public int divColor=-1;
   /** The selection color. */
   public int selColor=-1;
   
   /** Defines if the text will have a 3d style. */
   public boolean is3dText;
   
   /** Set to true to behave like a Radio, false like a Button */
   public boolean isSticky;
   
   /** Constructs a MultiButton with the given captions. */
   public MultiButton(String[] captions)
   {
      this.tits = captions;
   }
   
   public void onColorsChanged(boolean colorsChanged)
   {
      npback = null;
   }
   
   /** Sets the selected index, or -1 to unset it. Only an enabled index can be selected. */
   public void setSelectedIndex(int sel)
   {
      if (sel == -1 || (0 <= sel && sel < tits.length && (disabled == null || !disabled[sel])))
      {
         this.sel = sel;
         repaintNow();
         postPressedEvent();
         if (!isSticky)
         {
            if (Settings.onJavaSE) Vm.sleep(20);
            this.sel = -1;
            repaint();
         }
      }
   }
   
   /** Enables or disables a caption */
   public void setEnabled(int idx, boolean enabled)
   {
      if (disabled == null) disabled = new boolean[tits.length];
      disabled[idx] = !isEnabled();
      repaint();
   }
   
   /** Returns if a caption is enabled or not */
   public boolean isEnabled(int idx)
   {
      return disabled == null || !disabled[idx];
   }
   
   /** Returns the selected index */
   public int getSelectedIndex()
   {
      return sel;
   }

   public void onPaint(Graphics g)
   {
      try
      {
         int bc = getBackColor();
         int tcolor = Color.darker(bc,32);
         if (npback == null)
         {
            int c = isEnabled() ? bc : Color.getCursorColor(tcolor);
            if (divColor == -1) divColor = Color.darker(c,92);
            if (selColor == -1) selColor = Color.darker(backColor,64);
            npback = NinePatch.getInstance().getNormalInstance(NinePatch.COMBOBOX, width, height, c, false);
            npsel  = NinePatch.getInstance().getPressedInstance(npback, backColor, selColor);
         }
         // without this, clicking will make the button fade out
         g.backColor = parent.getBackColor();
         g.fillRect(0,0,width,height);
         
         NinePatch.tryDrawImage(g,npback,0,0);
         int w = width / tits.length;
         if (sel != -1)
            g.copyRect(npsel,sel*w+2,0,w-2,height,sel*w+2,1);
         for (int i = 0, x0 = 0, n = tits.length-1; i <= n; i++, x0 += w)
         {
            String s = tits[i];
            int tw = fm.stringWidth(s);
            int tx = (w - tw) / 2 + x0;
            int ty = (height - fmH) / 2 -1 ;
            boolean textEnabled = isEnabled() && (disabled == null || !disabled[i]);

            if (is3dText && textEnabled)
            {
               g.foreColor = tcolor;
               g.drawText(s, tx+1,ty-1);
               g.foreColor = i == sel ? tcolor : bc;
               g.drawText(s, tx-1,ty+1);
            }
            g.foreColor = textEnabled ? getForeColor() : Color.brighter(foreColor);
            g.drawText(s, tx,ty);
            
            g.drawText(tits[i], tx, ty);
            if (i < n) 
            {
               g.foreColor = isEnabled() ? divColor : Color.brighter(divColor);
               int y1 = (height - fmH)/2, y2 = y1 + fmH, xx = x0+w;
               g.drawLine(xx,y1,xx,y2); xx++;
               g.foreColor = tcolor;
               g.drawLine(xx,y1,xx,y2);
            }
         }
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
   
   public void onEvent(Event e)
   {
      switch (e.type)
      {
         case PenEvent.PEN_UP:
            if (isEnabled() && !hadParentScrolled())
            {
               PenEvent pe = (PenEvent)e;
               int sel = isInsideOrNear(pe.x,pe.y) ? pe.x / (width / tits.length) : -1;
               if (sel != this.sel && (sel == -1 || disabled == null || (sel < disabled.length && !disabled[sel])))
                  setSelectedIndex(sel);
            }
            break;
         case KeyEvent.SPECIAL_KEY_PRESS:
            if (isEnabled())
            {
               KeyEvent ke = (KeyEvent)e;
               if (ke.isPrevKey())
               {
                  for (int i = tits.length; --i >= 0;)
                  {
                     if (--sel < 0) sel = tits.length-1;
                     if (disabled == null || !disabled[sel])
                        break;
                  }
                  if (!Settings.keyboardFocusTraversable)
                     postPressedEvent();
                  repaint();
               }
               else
               if (ke.isNextKey())
               {
                  for (int i = tits.length; --i >= 0;)
                  {
                     if (++sel >= tits.length) sel = 0;
                     if (disabled == null || !disabled[sel])
                        break;
                  }
                  if (!Settings.keyboardFocusTraversable)
                     postPressedEvent();
                  repaint();
               }
               else
               if (ke.isActionKey())
               {
                  int sel = this.sel;
                  sel = -1;
                  setSelectedIndex(sel);
               }
            }
            break;
      }
   }
   
   public int getPreferredHeight()
   {
      return fmH + Edit.prefH;
   }   
   
   public int getPreferredWidth()
   {
      int w = 0;
      for (int i = tits.length; --i >= 0;)
         w += fm.stringWidth(tits[i]);
      return w + fmH*2;      
   }
}
