package tc.samples.like.fb.ui;

import tc.samples.like.fb.*;

import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;

public class FBTitleBar extends Container implements FBConstants
{
   Button btSearch, btOnline;
   
   public void initUI()
   {
      setBackColor(TOPBAR);
      add(btSearch = FBUtils.noborder(FBImages.search), LEFT+100,CENTER);
      add(btOnline = FBUtils.noborder(FBImages.online), RIGHT-100,CENTER);
      Edit ed = new FBEdit("Search");
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
                  TopMenu.Item it0 = new TopMenu.Item(new Label("Users"), null);
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