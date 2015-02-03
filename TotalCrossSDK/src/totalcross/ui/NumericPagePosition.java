package totalcross.ui;

import totalcross.ui.gfx.*;

public class NumericPagePosition extends PagePosition
{
   private String txt;
   public int extra,fmw;
   
   public NumericPagePosition()
   {
      super(0);
   }
   
   public void setPosition(int p)
   {
      super.setPosition(p+extra);
      txt = position+" / "+count;
      fmw = fm.stringWidth(txt);
   }
   
   public void setCount(int c)
   {
      super.setCount(c);
      txt = position+" / "+count;
      fmw = fm.stringWidth(txt);
   }

   public void onPaint(Graphics g)
   {
      g.backColor = backColor;
      g.fillRect(0,0,width,height);
      g.foreColor = foreColor;
      String txt = position+" / "+count;
      g.drawText(txt,(width-fmw)/2,(height-fmH)/2);
   }
}
