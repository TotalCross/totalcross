package tc.samples.like.fb;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class FaceBookUI extends MainWindow
{
   private static final int TOPBAR = 0x3C5998;
   private static final int BOTBAR = 0x47639E;
   
   class TopBar extends Container
   {
      public void initUI()
      {
         setBackColor(TOPBAR);
         add(noborder(FBImages.search), LEFT+100,CENTER);
         add(noborder(FBImages.online), RIGHT-100,CENTER);
      }
   }
   
   private static Button noborder(Image img)
   {
      Button b = new Button(img);
      b.setBorder(Button.BORDER_NONE);
      return b;
   }
   
   public FaceBookUI()
   {
      Settings.uiAdjustmentsBasedOnFontHeight = true;
      setBackColor(Color.WHITE);
   }

   public void initUI()
   {
      try
      {
         FBImages.load(fmH);
         add(new TopBar(), LEFT,TOP,FILL,fmH*2);
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
         exit(0);
      }
   }
}
