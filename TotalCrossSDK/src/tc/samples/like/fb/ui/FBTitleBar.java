package tc.samples.like.fb.ui;

import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

import tc.samples.like.fb.*;

public class FBTitleBar extends FBContainer
{
   Button btSearch, btOnline;
   
   public void initUI()
   {
      setBackColor(TOPBAR);
      add(btSearch = noborder(FBImages.search), LEFT+100,CENTER);
      add(btOnline = noborder(FBImages.online), RIGHT-100,CENTER);
      Edit ed = createEdit("Search");
      ed.setForeColor(Color.WHITE);
      add(ed, AFTER,SAME,FIT,PREFERRED,btSearch);
      Ruler r = new Ruler();
      r.dots = true;
      add(r, SAME,AFTER,SAME-100,1);
   }
   
   public void onEvent(Event e)
   {
      try
      {
         switch (e.type)
         {
            case ControlEvent.PRESSED:
               if (e.target == btOnline && FaceBookUI.defaultUser != null)
               {
                  TopMenu.Item it0 = new TopMenu.Item(new Label("Users"), (Image)null);
                  TopMenu.Item it = new TopMenu.Item(new Label(FaceBookUI.defaultUser), FaceBookUI.defaultPhoto);
                  TopMenu top = new TopMenu(new Control[]{it0,it},RIGHT);
                  top.totalTime = 500;
                  top.popup();
               }
               break;
         }
      }
      catch (Throwable t)
      {
         FBUtils.logException(t);
      }
   }
}