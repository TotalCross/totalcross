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
   /** The animation time. Set to 0 to disable animations in all Switches. */
   public static int ANIMATION_TIME = 250;
   
   /** Text to draw when on background or foreground, and when this switch is on or off */
   public String textBackOn, textForeOn, textBackOff, textForeOff;
   
   /** Text color to draw when on background or foreground, and when this switch is on or off */
   public int colorBackOn, colorForeOn, colorBackOff, colorForeOff;
   
   /** Set to true to center text instead of moving it to left or right */
   public boolean centerText;
   
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
   
   /** Returns true if this switch is ON. Note that, if this Switch has animations, it will return on only after the animation finishes */
   public boolean isOn()
   {
      return dragBarPos+dragBarSize/2 >= width/2;
   }
   
   public void moveSwitch(boolean toLeft)
   {
      int destPos = toLeft ? dragBarMin : dragBarMax;
      if (ANIMATION_TIME == 0)
      {
         dragBarPos = destPos;
         if (isOn() != wasChecked)
            postPressedEvent();
      }
      else
      {
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
         boolean enabled = isEnabled();
         if (btn == null)
         {
            btn = (isIos ? Resources.switchBtnIos : Resources.switchBtnAnd).getSmoothScaledInstance(height,height);
            btn.applyColor2(!enabled ? backColor : foreColor);
         }
         if (back == null)
         {
            backLR = (isIos ? Resources.switchBrdIos : Resources.switchBrdAnd).smoothScaledFixedAspectRatio(height,true); // left/right
            back = Resources.switchBack.getSmoothScaledInstance(width-backLR.getWidth(),height); // mid
            int fillB = !enabled ? Color.interpolate(backColor, parent.backColor) : backColor;
            back.applyColor2(fillB);
            backLR.applyColor2(fillB);
         }
         int lrww = backLR.getWidth();
         int lrw = lrww / 2;
         back.alphaMask = backLR.alphaMask = alphaValue;
         g.drawImage(back,lrw,0);
         // draw left
         g.setClip(0,0,lrw,height);
         g.drawImage(backLR,0,0);
         // draw right
         g.setClip(width-lrw-1,0,height+1,height);
         g.drawImage(backLR,width-lrww-1,0);
         g.clearClip();
         back.alphaMask = backLR.alphaMask = 255;
         if (isOn()) // text at left
         {
            if (textBackOn != null)
            {
               g.foreColor = colorBackOn;
               int ww = fm.stringWidth(textBackOn);
               g.drawText(textBackOn,centerText ? (width-ww)/2 : (width-dragBarSize-ww)/2,(height-fmH)/2);
            }
            g.drawImage(btn,dragBarPos,0);
            if (textForeOn != null)
            {
               g.foreColor = colorForeOn;
               g.drawText(textForeOn,dragBarPos+(dragBarSize-fm.stringWidth(textForeOn))/2,(height-fmH)/2);
            }
         }
         else // text at right
         {
            if (textBackOff != null)
            {
               g.foreColor = colorBackOff;
               int ww = fm.stringWidth(textBackOn);
               g.drawText(textBackOff,centerText ? (width-ww)/2 : (width-dragBarSize-ww)/2+dragBarSize,(height-fmH)/2);
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
      if (isEnabled())
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
                  moveSwitch(isOn());
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
      int textW = Math.max(textBackOn == null ? 0 : fm.stringWidth(textBackOn), textBackOff == null ? 0 : fm.stringWidth(textBackOff));
      int btW = fmH+Edit.prefH;
      return textW == 0 ? btW * 2 : btW*3/2+textW;
   }

   public void onAnimationFinished(ControlAnimation anim)
   {
      if (isOn() != wasChecked)
         postPressedEvent();
   }
}
