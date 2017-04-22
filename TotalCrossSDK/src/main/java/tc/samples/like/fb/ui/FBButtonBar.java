package tc.samples.like.fb.ui;

import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class FBButtonBar extends FBContainer implements PressListener
{
   Button[] btns = new Button[5];
   public int last;
   
   private Button create(int i, Image img)
   {
      Button b = noborder(img);
      try 
      {
         b.pressedImage = img.getCopy();
         b.pressedImage.applyColor2(TOPBAR);
      } catch (Throwable t) {}
      b.shiftOnPress = false;
      b.isSticky = true;
      b.addPressListener(this);
      b.appId = i;
      btns[i] = b;
      return b;
   }
   
   public void initUI()
   {
      setBackColor(Color.WHITE);
      add(create(0,FBImages.content), LEFT,  CENTER, PARENTSIZE+20, PREFERRED);
      add(create(1,FBImages.friends), AFTER, CENTER, PARENTSIZE+20, PREFERRED);
      add(create(2,FBImages.chat),    AFTER, CENTER, PARENTSIZE+20, PREFERRED);
      add(create(3,FBImages.news),    AFTER, CENTER, PARENTSIZE+20, PREFERRED);
      add(create(4,FBImages.menu),    AFTER, CENTER, PARENTSIZE+20, PREFERRED);
      btns[0].press(true);
   }

   public void controlPressed(ControlEvent e)
   {
      setPressed(((Button)e.target).appId,true);
   }

   public void setPressed(int cur)
   {
      setPressed(cur, false);
   }

   private void setPressed(int cur, boolean post)
   {
      if (cur != last)
      {
         btns[last].press(false);
         btns[last=cur].press(true);
         postPressedEvent();
      }
   }
}