package totalcross.ui;

import totalcross.res.*;
import totalcross.sys.*;
import totalcross.ui.anim.*;
import totalcross.ui.anim.ControlAnimation.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

/** This class implements the on/off switch present in many platforms
 * 
 */
public class Switch extends Control implements PathAnimation.SetPosition, AnimationFinished
{
   private Image back,backLR;
   private Image btn;
   private boolean isIos,dragged,wasChecked;
   private int startDragPos, dragBarPos, dragBarSize, dragBarMin, dragBarMax;
   /** The animation time. */
   public static int ANIMATION_TIME = 250;
   
   /** Text to draw when on background or foreground, and when this switch is on or off */
   public String textBackOn, textForeOn, textBackOff, textForeOff;
   
   /** Text color to draw when on background or foreground, and when this switch is on or off */
   public int colorBackOn, colorForeOn, colorBackOff, colorForeOff;
   
   public Switch(boolean androidType)
   {
      foreColor = 0x05B6EE;
      backColor = 0xDDDDDD;
      this.isIos = !androidType;
   }
   
   public void onBoundsChanged(boolean b)
   {
      back = btn = null;
      dragBarSize = height;
      dragBarMin = 0;
      dragBarMax = width - dragBarSize-1;
   }
   
   public void onColorsChanged(boolean b)
   {
      back = btn = null;
   }
   
   /** Change the on/off state. */
   public void setOn(boolean b)
   {
      if (b != isOn())
         moveSwitch(!b);
   }
   
   /** Returns true if this switch is ON */
   public boolean isOn()
   {
      return dragBarPos+dragBarSize/2 >= width/2;
   }
   
   public void moveSwitch(boolean toLeft)
   {
      int destPos = toLeft ? dragBarMin : dragBarMax;
      if (dragBarPos != destPos)
         try
         {
            PathAnimation p = PathAnimation.create(this, dragBarPos,0, destPos, 0, this, ANIMATION_TIME);
            p.useOffscreen = false;
            p.setpos = this;
            p.start();
         }
         catch (Exception ee)
         {
            if (Settings.onJavaSE) ee.printStackTrace();
            dragBarPos = destPos;
         }
      else
      if (isOn() != wasChecked)
         postPressedEvent();
   }
   
   /** Used by animation */
   public void setPos(int x, int y) // PathAnimation.SetPosition
   {
      dragBarPos = x;
      Window.needsPaint = true;
   }
   
   public void onPaint(Graphics g)
   {
      try
      {
         if (btn == null)
         {
            btn = (isIos ? Resources.switchBtnIos : Resources.switchBtnAnd).getSmoothScaledInstance(height,height);
            btn.applyColor2(getForeColor());
         }
         if (!isIos) // in iOS
         {
            if (back == null)
            {
               back = Resources.switchBackAnd.getSmoothScaledInstance(width+1,height);
               back.applyColor2(getBackColor());
            }
            back.alphaMask = alphaValue;
            g.drawImage(back,0,0);
            back.alphaMask = 255;            
         }
         else
         {
            if (back == null)
            {
               back = Resources.switchBackIos.getSmoothScaledInstance(width-height,height); // mid
               backLR = Resources.switchBrdIos.getSmoothScaledInstance(height,height); // left/right
               back.applyColor2(getBackColor());
               backLR.applyColor2(getBackColor());
            }
            back.alphaMask = backLR.alphaMask = alphaValue;
            g.drawImage(back,height/2,0);
            // draw left
            g.setClip(0,0,height/2,height);
            g.drawImage(backLR,0,0);
            // draw right
            g.setClip(width-height/2-1,0,height+1,height);
            g.drawImage(backLR,width-height,0);
            g.clearClip();
            back.alphaMask = backLR.alphaMask = 255;
         }               
         if (isOn())
         {
            if (textBackOn != null)
            {
               g.foreColor = colorBackOn;
               g.drawText(textBackOn,(width-fm.stringWidth(textBackOn))/2,(height-fmH)/2);
            }
            g.drawImage(btn,dragBarPos,0);
            if (textForeOn != null)
            {
               g.foreColor = colorForeOn;
               g.drawText(textForeOn,dragBarPos+(dragBarSize-fm.stringWidth(textForeOn))/2,(height-fmH)/2);
            }
         }
         else
         {
            if (textBackOff != null)
            {
               g.foreColor = colorBackOff;
               g.drawText(textBackOff,(width-fm.stringWidth(textBackOff))/2,(height-fmH)/2);
            }
            g.drawImage(btn,dragBarPos,0);
            if (textForeOff != null)
            {
               g.foreColor = colorForeOff;
               g.drawText(textForeOff,dragBarPos+(dragBarSize-fm.stringWidth(textForeOff))/2,(height-fmH)/2);
            }
         }
      }
      catch (Throwable t)
      {
         t.printStackTrace();
      }
      // draw button
   }
   
   public void onEvent(Event event)
   {
      switch (event.type)
      {
         case PenEvent.PEN_DRAG_START:
         case PenEvent.PEN_DRAG_END:
            break;
         case PenEvent.PEN_DRAG:
            if (startDragPos != -1)
            {
               int newDragBarPos = getPos(event);
               if (newDragBarPos != dragBarPos)
               {
                  dragged = true;
                  dragBarPos = newDragBarPos;
                  Window.needsPaint = true;
               }
            }
            break;
         case PenEvent.PEN_DOWN:
            wasChecked = isOn();
            dragged = false;
            startDragPos = ((PenEvent)event).x - dragBarPos;
            break;
         case PenEvent.PEN_UP:
            Window.needsPaint = true;
            if (!hadParentScrolled())
            {
               if (!dragged)
               {
                  boolean nowAtMidLeft = ((PenEvent)event).x < width/2;
                  moveSwitch(nowAtMidLeft);
               }
               else
               {
                  dragBarPos = getPos(event);
                  boolean nowAtMidLeft = dragBarPos+dragBarSize/2 < width/2;
                  moveSwitch(nowAtMidLeft);
               }
            }
            startDragPos = -1;
            break;
      }
   }
   
   private int getPos(Event event)
   {
      int newDragBarPos = ((PenEvent)event).x - startDragPos;
      if (newDragBarPos < dragBarMin) newDragBarPos = dragBarMin; else
      if (newDragBarPos > dragBarMax) newDragBarPos = dragBarMax;
      return newDragBarPos;
   }

   public int getPreferredHeight()
   {
      return fmH + Edit.prefH;
   }
   
   public int getPreferredWidth()
   {
      return (fmH+Edit.prefH)*2;
   }

   public void onAnimationFinished(ControlAnimation anim)
   {
      if (isOn() != wasChecked)
         postPressedEvent();
   }
}
