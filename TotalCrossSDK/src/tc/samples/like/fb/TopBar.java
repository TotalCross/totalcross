package tc.samples.like.fb;

import totalcross.ui.*;
import totalcross.ui.gfx.*;

class TopBar extends Container implements FBConstants
{
   Button btSearch, btOnline;
   
   public void initUI()
   {
      setBackColor(TOPBAR);
      add(btSearch = FaceBookUI.noborder(FBImages.search), LEFT+100,CENTER);
      add(btOnline = FaceBookUI.noborder(FBImages.online), RIGHT-100,CENTER);
      Edit ed = new MyEdit();
      ed.setForeColor(Color.WHITE);
      ed.transparentBackground = true;
      add(ed, AFTER,SAME,FIT,PREFERRED,btSearch);
      Ruler r = new Ruler();
      r.dots = true;
      add(r, SAME,AFTER,SAME-100,1);
   }

   class MyEdit extends Edit
   {
      public void onPaint(Graphics g)
      {
         super.onPaint(g);
         if (!hasFocus && getLength() == 0)
         {
            g.foreColor = 0xAAAAAA;
            g.drawText("Busca",0,fmH/4);
         }
      }
   }
   
}