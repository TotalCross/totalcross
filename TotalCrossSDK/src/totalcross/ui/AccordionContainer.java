package totalcross.ui;

import totalcross.sys.*;
import totalcross.ui.anim.*;
import totalcross.ui.anim.ControlAnimation.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;

import java.util.*;

/** A Container that can be expanded or collapsed
 * 
 * Sample:
 * <pre>
 * AccordionContainer ac = new AccordionContainer();
 * ac.maxH = fmH*10;
 * add(ac, LEFT+50,TOP+100,FILL-50,ac.minH);
 * ac.add(ac.new Caption("Type text"), LEFT,TOP,FILL,PREFERRED);
 * ac.add(new MultiEdit(),LEFT+50,AFTER+50,FILL-50,fmH*7);
 * </pre> 
 * 
 * Note that when the container is changing its height, it calls <code>parent.reposition</code> to open space for its growth.
 */

public class AccordionContainer extends Container implements PathAnimation.SetPosition, AnimationFinished
{
   public static int ANIMATION_TIME = 300;
   
   public static class Group
   {
      ArrayList<AccordionContainer> l = new ArrayList<AccordionContainer>(5);
      
      public void collapseAll()
      {
         for (AccordionContainer a: l)
            if (a.isExpanded())
               a.collapse();
      }
   }
   
   public int minH = fmH + Edit.prefH;
   private Group group;
   
   public AccordionContainer()
   {
   }
   
   public AccordionContainer(Group g)
   {
      g.l.add(this);
      group = g;
   }
   
   public class Caption extends Control
   {
      String caption;
      
      public Caption(String caption)
      {
         this.caption = caption;
      }
      
      public int getPreferredHeight()
      {
         return fmH + Edit.prefH;
      }
      
      public void onPaint(Graphics g)
      {
         if (!isExpanded())
            g.drawArrow(fmH/2, 2, height/3, Graphics.ARROW_RIGHT, false, foreColor);
         else
            g.drawArrow(fmH/2, height/3, height/3, Graphics.ARROW_DOWN, false, foreColor);
         g.drawText(caption, fmH*2, 0, textShadowColor != -1, textShadowColor);
      }
      public void onEvent(Event e)
      {
         PenEvent pe;
         switch (e.type)
         {
            case PenEvent.PEN_DOWN:
               Window.needsPaint = true;
               break;
            case PenEvent.PEN_UP:
               Window.needsPaint = true;
               pe = (PenEvent)e;
               if ((!Settings.fingerTouch || !hadParentScrolled()) && isInsideOrNear(pe.x,pe.y))
               {
                  postPressedEvent();
                  if (isExpanded())
                     collapse();
                  else
                     expand();
               }
               break;
         }
      }
   }
   
   public void expand()
   {
      try
      {
         group.collapseAll();
         int maxH = getMaxHeight();
         PathAnimation p = PathAnimation.create(this, 0, this.height, 0, maxH, this, ANIMATION_TIME);
         p.useOffscreen = false;
         p.setpos = this;
         p.start();
      }
      catch (Exception ee)
      {
         ee.printStackTrace();
      }
   }
   
   public void collapse()
   {
      try
      {
         PathAnimation p = PathAnimation.create(this, 0, this.height, 0, minH, this, ANIMATION_TIME);
         p.useOffscreen = false;
         p.setpos = this;
         p.start();
      }
      catch (Exception ee)
      {
         ee.printStackTrace();
      }
   }
   
   public boolean isExpanded()
   {
      return this.height != minH;
   }
   

   public void onAnimationFinished(ControlAnimation anim)
   {
   }
   
   public void setPos(int x, int y)
   {
      this.height = setH = y;
      Window.needsPaint = true;
      parent.reposition();
   }

   private int getMaxHeight()
   {
      int maxH = 0, minH = Convert.MAX_INT_VALUE;
      for (Control child = children; child != null; child = child.next)
      {
         if (child.y < minH)
            minH = child.y;
         int yy = child.getY2();
         if (yy > maxH)
            maxH = yy;
      }
      return maxH + minH; // use the distance of the first container as a gap at bottom  
   }
   
   public int getPreferredHeight()
   {
      return minH;
   }
}
