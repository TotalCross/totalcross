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
   
   public class Caption extends Container
   {
      public Button btExpanded, btCollapsed;
      public Label lCaption;
      
      public Caption(String caption)
      {
         this.lCaption = new Label(caption);
      }
      
      public Caption(Label lCaption, Button btExpanded, Button btCollapsed)
      {
         this.lCaption = lCaption;
         this.btExpanded = btExpanded;
         this.btCollapsed = btCollapsed;
      }
      
      public void initUI()
      {
         if (btExpanded == null)
         {
            btExpanded = new ArrowButton(Graphics.ARROW_DOWN,fmH/2, foreColor);
            btExpanded.setBorder(Button.BORDER_NONE);
         }
         if (btCollapsed == null)
         {
            btCollapsed = new ArrowButton(Graphics.ARROW_RIGHT,fmH/2, foreColor);
            btCollapsed.setBorder(Button.BORDER_NONE);
         }
         add(btExpanded, LEFT,TOP,PREFERRED,FILL);
         add(btCollapsed, SAME,SAME,SAME,SAME);
         add(lCaption, uiAdjustmentsBasedOnFontHeightIsSupported ? AFTER+50 : AFTER+fmH/2, CENTER);
         btExpanded.setVisible(false);
      }
      
      public int getPreferredHeight()
      {
         return fmH + Edit.prefH;
      }

      public void invert()
      {
         postPressedEvent();         
         if (isExpanded())
            collapse();
         else
            expand();
         boolean b = isExpanded();
         btExpanded.setVisible(!b);
         btCollapsed.setVisible(b);
      }
      
      public void onEvent(Event e)
      {
         PenEvent pe;
         switch (e.type)
         {
            case ControlEvent.PRESSED:
               if (e.target == btExpanded || e.target == btCollapsed)
                  invert();
               break;                  
            case PenEvent.PEN_DOWN:
               Window.needsPaint = true;
               break;
            case PenEvent.PEN_UP:
               Window.needsPaint = true;
               pe = (PenEvent)e;
               if ((!Settings.fingerTouch || !hadParentScrolled()) && isInsideOrNear(pe.x,pe.y))
                  invert();
               break;
         }
      }
   }
   
   public void expand()
   {
      try
      {
         if (group != null)
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
      getParentWindow().reposition();
   }
   
   public void setPos(int x, int y)
   {
      this.height = setH = y;
      Window.needsPaint = true;
      getParentWindow().reposition();
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
