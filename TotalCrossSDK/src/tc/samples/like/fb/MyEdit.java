package tc.samples.like.fb;

import totalcross.ui.*;
import totalcross.ui.gfx.*;

class MyEdit extends Edit
{
   String tip;
   public MyEdit(String tip)
   {
      this.tip = tip;
      transparentBackground = true;
   }
   public void onPaint(Graphics g)
   {
      super.onPaint(g);
      if (!hasFocus && getLength() == 0)
      {
         g.foreColor = 0xAAAAAA;
         g.drawText(tip,0,fmH/4);
      }
   }
}