package tc.samples.like.fb;

import tc.samples.like.fb.ui.*;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.image.*;

public class FaceBookUI extends MainWindow implements FBConstants
{
   public static FBPosts posts;
   public static FBNewUser user;
   public static FBTopBar bar;
   public static TabbedContainer tc;
   
   public static Image defaultPhoto;
   public static String defaultUser;

   public FaceBookUI()
   {
      Settings.uiAdjustmentsBasedOnFontHeight = true;
      setUIStyle(Settings.Android);
   }

   public void initUI()
   {
      try
      {
         FBImages.load(fmH);
         add(new FBTitleBar(), LEFT,TOP,FILL,fmH*5/2);
         add(bar = new FBTopBar(), LEFT,AFTER,FILL,SAME);
         String[] tits = {"1","2","3","4","5"};
         tc = new TabbedContainer(tits);
         tc.setBackColor(CONTENTH);
         tc.setType(TabbedContainer.TABS_NONE);
         add(tc,LEFT,AFTER,FILL,FILL);
         tc.setContainer(0, posts = new FBPosts());
         Container c = tc.getContainer(1);
         c.add(user = new FBNewUser(),CENTER,AFTER+25,PARENTSIZE+96,PREFERRED);
      }
      catch (Throwable ee)
      {
         MessageBox.showException(ee,true);
         exit(0);
      }
   }
   
   public void onEvent(Event e)
   {
      switch (e.type)
      {
         case ControlEvent.PRESSED:
            if (e.target == tc)
               bar.setPressed(tc.getActiveTab());
            else
            if (e.target == bar)
               tc.setActiveTab(bar.last);
      }
   }
}
