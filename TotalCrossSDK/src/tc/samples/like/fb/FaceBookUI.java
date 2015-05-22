package tc.samples.like.fb;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.image.*;

public class FaceBookUI extends MainWindow
{
   static Button noborder(Image img)
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
         add(new TopMenu(), LEFT,AFTER,FILL,SAME);
         add(h = new ContentHolder(),LEFT,AFTER,FILL,FILL); 
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
         exit(0);
      }
   }
   
   ContentHolder h;
}
