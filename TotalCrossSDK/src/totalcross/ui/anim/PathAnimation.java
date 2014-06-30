package totalcross.ui.anim;

import totalcross.sys.*;
import totalcross.ui.*;

public class PathAnimation extends ControlAnimation
{
   int x0,y0,xf,yf,x,y;
   int dir;
   
   public PathAnimation(Control c, AnimationFinished animFinish)
   {
      super(c,animFinish);
   }
   
   public PathAnimation(Control c)
   {
      this(c,null);
   }
   
   public void setPath(int x0, int y0, int xf, int yf) throws Exception
   {
      this.x0 = x = x0;
      this.y0 = y = y0;
      this.xf = xf;
      this.yf = yf;
   }
   
   public void animate()
   {
      update();
      c.setRect(x,y,Control.KEEP,Control.KEEP);
      Window.needsPaint = true;
   }
   
   public void stop()
   {
      super.stop();
      switch (dir)
      {
         case Control.LEFT:   c.setSet(Control.LEFT,Control.TOP); break;
         case Control.RIGHT:  c.setSet(Control.RIGHT,Control.TOP); break;
         case Control.TOP:    c.setSet(Control.CENTER,Control.TOP); break;
         case Control.BOTTOM: c.setSet(Control.CENTER,Control.BOTTOM); break;
         case Control.CENTER: c.setSet(Control.CENTER,Control.CENTER); break;
      }
   }
   
   private void update()
   {
      double distanceRemaining = Math.sqrt((xf-x)*(xf-x) + (yf-y)*(yf-y));
      int speed = (int)computeSpeed(distanceRemaining);
      if ((x == xf && y == yf) || speed == 0)
      {
         x = xf; y = yf;
         stop();
         return;
      }
      int dx = xf - this.x;
      int dy = yf - this.y;
      int steps;

      if (dx == 0) // vertical move
      {
         steps = Math.min(dy >= 0 ? dy : -dy, speed);
         if (dy < 0)
            this.y -= steps;
         else
         if (dy > 0)
            this.y += steps;
      }
      else
      if (dy == 0) // horizontal move
      {
         steps = Math.min(dx >= 0 ? dx : -dx, speed);
         if (dx < 0)
            this.x -= steps;
         else
         if (dx > 0)
            this.x += steps;
      }
      else
      {
         dx = dx >= 0 ? dx : -dx; 
         dy = dy >= 0 ? dy : -dy;
         int CurrentX = this.x; 
         int CurrentY = this.y;
         int Xincr = (this.x > xf) ? -1 : 1; 
         int Yincr = (this.y > yf) ? -1 : 1; 
         steps = speed;
         if (dx >= dy) 
         {
            int dPr = dy << 1; 
            int dPru = dPr - (dx << 1); 
            int P = dPr - dx; 
            for (; dx >= 0 && steps > 0; dx--) 
            {
               this.x = CurrentX; 
               this.y = CurrentY;
               CurrentX += Xincr; 
               steps--;
               if (P > 0) 
               {
                  CurrentY += Yincr; 
                  steps--;
                  P += dPru; 
               }
               else P += dPr; 
            }
         }
         else
         {
            int dPr = dx << 1; 
            int dPru = dPr - (dy << 1); 
            int P = dPr - dy; 
            for (; dy >= 0 && steps > 0; dy--) 
            {
               this.x = CurrentX; 
               this.y = CurrentY;
               CurrentY += Yincr; 
               steps--;
               if (P > 0) 
               {
                  CurrentX += Xincr; 
                  steps--;
                  P += dPru; 
               }
               else P += dPr; 
            }
         }
      }
   }

   public static PathAnimation create(Control c, int toX, int toY, AnimationFinished animFinish) throws Exception
   {
      PathAnimation anim = new PathAnimation(c,animFinish);
      anim.setPath(c.getX(),c.getY(),toX,toY);
      return anim;
   }
   
   public static PathAnimation create(Control c, int fromX, int fromY, int toX, int toY, AnimationFinished animFinish) throws Exception
   {
      PathAnimation anim = new PathAnimation(c,animFinish);
      anim.setPath(fromX,fromY,toX,toY);
      return anim;
   }
   
   public static PathAnimation create(Control c, int direction, AnimationFinished animFinish) throws Exception
   {
      PathAnimation anim = new PathAnimation(c,animFinish);
      anim.dir = direction;
      int x0,y0,xf,yf;
      int pw = c instanceof Window ? Settings.screenWidth  : c.getParent().getWidth();
      int ph = c instanceof Window ? Settings.screenHeight : c.getParent().getHeight();
      int cw = c.getWidth();
      int ch = c.getHeight();
      xf = x0 = (pw - cw) / 2; 
      y0 = yf = (ph - ch) / 2;
      switch (direction)
      {
         case -Control.BOTTOM:
            y0 = c.getY();
            yf = ph;
            break;
         case Control.BOTTOM: 
            y0 = ph;
            yf = ph - ch;
            break;
         case -Control.TOP:
            y0 = c.getY();
            yf = -ch;
            break;
         case Control.TOP:
            y0 = -ch;
            yf = 0;
            break;
         case -Control.LEFT:
            x0 = c.getX();
            xf = -cw;
            break;
         case Control.LEFT:
            x0 = -cw;
            xf = 0;
            break;
         case -Control.RIGHT:
            x0 = c.getX();
            xf = pw;
            break;
         case Control.RIGHT:
            x0 = pw;
            xf = pw - cw;
            break;
         default:
            return null;
      }
            
      anim.setPath(x0,y0,xf,yf);
      return anim;
   }

   public static PathAnimation create(Control c, int direction) throws Exception
   {
      return create(c, direction, null);
   }
}
