package totalcross.ui.anim;

import totalcross.sys.*;
import totalcross.ui.*;

public class PathAnimation extends ControlAnimation
{
   int x0,y0,xf,yf,x,y;
   int speed;
   
   public PathAnimation(Container c)
   {
      super(c);
   }
   
   public void setPath(int x0, int y0, int xf, int yf) throws Exception
   {
      this.x0 = x = x0;
      this.y0 = y = y0;
      this.xf = xf;
      this.yf = yf;
      int dist = (int)Math.sqrt((xf-x0)*(xf-x0) + (yf-y0)*(yf-y0));
      int fps = totalTime / frameRate;
      speed = dist / fps;
      start();
   }
   
   public void animate()
   {
      update();
      if (x == xf && y == yf)
         stop();
      c.setRect(x,y,Control.KEEP,Control.KEEP);
      Window.needsPaint = true;
   }
   
   private void update()
   {
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

   public static void create(Container c, int direction)
   {
      PathAnimation anim = new PathAnimation(c);
      int x0,y0,xf,yf;
      int pw = c instanceof Window ? Settings.screenWidth  : c.getParent().getWidth();
      int ph = c instanceof Window ? Settings.screenHeight : c.getParent().getHeight();
      int cw = c.getWidth();
      int ch = c.getHeight();
      switch (direction)
      {
         case Control.BOTTOM: 
            xf = x0 = (pw - cw) / 2;
            y0 = ph;
            yf = ph - ch;
            break;
         case Control.TOP:
            xf = x0 = (pw - cw) / 2;
            y0 = -ch;
            yf = 0;
            break;
         case Control.LEFT:
            y0 = yf = (ph - ch) / 2;
            x0 = -cw;
            xf = 0;
            break;
         case Control.RIGHT:
            y0 = yf = (ph - ch) / 2;
            x0 = pw;
            xf = pw - cw;
            break;
         default:
            return;
      }
            
      try
      {
         anim.setPath(x0,y0,xf,yf);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }   
   }
}
