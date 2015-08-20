package totalcross.ui;

import totalcross.res.*;
import totalcross.sys.*;
import totalcross.ui.anim.*;
import totalcross.ui.anim.ControlAnimation.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class Switch extends Control implements AnimationFinished
{
   Image back,backLR;
   Image btn;
   boolean isIos;
   int startDragPos, dragBarPos, dragBarSize, dragBarMin, dragBarMax;
   
   public Switch(boolean androidType)
   {
      foreColor = Color.CYAN;
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
   
   public void moveSwitch(boolean toLeft)
   {
      int destPos = toLeft ? dragBarMin : dragBarMax;
      if (dragBarPos != destPos)
         try
         {
            PathAnimation p = PathAnimation.create(this, dragBarPos,0, destPos, 0, this, 300);
            p.useOffscreen = false;
            p.setpos = new PathAnimation.SetPosition() 
            {
               public void setPos(int x, int y)
               {
                  dragBarPos = x;
                  Window.needsPaint = true;
               }
            };
            p.start();
         }
         catch (Exception ee)
         {
            if (Settings.onJavaSE) ee.printStackTrace();
            dragBarPos = destPos;
         }
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
            g.drawImage(back,0,0);
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
            g.drawImage(back,height/2,0);
            // draw left
            g.setClip(0,0,height/2,height);
            g.drawImage(backLR,0,0);
            // draw right
            g.setClip(width-height/2-1,0,height+1,height);
            g.drawImage(backLR,width-height,0);
            g.clearClip();
         }               
      }
      catch (Throwable t)
      {
         t.printStackTrace();
      }
      // draw button
      g.drawImage(btn,dragBarPos,0);
   }
   
   private boolean dragged;
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
            dragged = false;
            startDragPos = getPos(event);
            break;
         case PenEvent.PEN_UP:
            Window.needsPaint = true;
            if (dragged && !hadParentScrolled())
            {
               dragBarPos = getPos(event);
               boolean nowAtMidLeft = dragBarPos+dragBarSize/2 < width/2;
               moveSwitch(nowAtMidLeft);
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

   public void onAnimationFinished(ControlAnimation anim)
   {
      // TODO Auto-generated method stub
      
   }
}
