package tc.samples.like.fb;

import totalcross.ui.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

class Content extends Container
{
   String name="Sponge Bob", info = "March 25, 2015 at 09:21 am", text = "I'm a very funny cartoon!";
   
   public void initUI()
   {
      setBackColor(Color.WHITE);
      try
      {
         add(new ImageControl(new Image("img/user.png").smoothScaledFixedAspectRatio(fmH*2,true)),LEFT+50,TOP+50);         
      }
      catch (Exception e)
      {
         Container c = new Container();
         c.setBackColor(0xDDDDDD);
         add(c,LEFT+50,TOP+50,fmH*2,fmH*2);
      }
      Label lname = new Label(name);
      lname.setFont(Font.getFont(true, fmH*2/3));
      add(lname, AFTER+50,SAME);
      
      Label linfo = new Label(info);
      linfo.setFont(Font.getFont(false, fmH*2/3));
      add(linfo, SAME,AFTER);
      
      add(new Label(text),LEFT+50, AFTER+50);
   }
   
   public int getPreferredWidth()
   {
      return FILL;
   }
   
   public int getPreferredHeight()
   {
      return fmH*5;
   }
}