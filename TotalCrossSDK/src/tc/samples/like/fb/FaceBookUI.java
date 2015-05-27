package tc.samples.like.fb;

import tc.samples.like.fb.ui.*;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.image.*;

public class FaceBookUI extends MainWindow
{
   public static Button noborder(Image img)
   {
      Button b = new Button(img);
      b.setBorder(Button.BORDER_NONE);
      return b;
   }
   
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
         add(new TopBar(), LEFT,TOP,FILL,fmH*5/2);
         add(new tc.samples.like.fb.ui.TopMenu(), LEFT,AFTER,FILL,SAME);
         add(h = new PostsContainer(),LEFT,AFTER,FILL,FILL); 
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
         exit(0);
      }
   }
   
   PostsContainer h;
}
