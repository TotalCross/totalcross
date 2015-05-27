package tc.samples.like.fb.ui;

import tc.samples.like.fb.*;

import totalcross.ui.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class PostInput extends Container implements FBConstants
{
   public static Image defaultPhoto;
   public static String defaultUser;
   
   public FBEdit med;
   
   public PostInput()
   {
   }
   
   public void initUI()
   {
      setBackColor(Color.WHITE);
      setBorderStyle(BORDER_SIMPLE);
      borderColor = BORDER;
      
      try
      {
         add(new ImageControl(defaultPhoto.smoothScaledFixedAspectRatio(fmH*2,true)),LEFT+50,TOP+50);         
      }
      catch (Exception e)
      {
         Container c = new Container();
         c.setBackColor(CNT_BACK);
         add(c,LEFT+50,TOP+50,fmH*2,fmH*2);
      }
      med = new FBEdit("What's on your mind?");
      add(med, AFTER+50,SAME,FILL-100,PREFERRED);
      
      add(create("STATUS", FBImages.status),LEFT,BOTTOM,PARENTSIZE-3,fmH*3/2);
      add(create("PHOTO", FBImages.photo),AFTER,BOTTOM,PARENTSIZE-3,fmH*3/2);
      add(create("CHECK-IN", FBImages.checkin),AFTER,BOTTOM,FILL,fmH*3/2);
      
      add(createRuler(Ruler.HORIZONTAL),LEFT+100,BEFORE,FILL-100,1);
   }
   
   private Ruler createRuler(int type)
   {
      Ruler r = new Ruler(type,false);
      r.setForeColor(BORDER);
      r.ignoreInsets = true;
      return r;
   }
   
   private Button create(String s, Image i)
   {
      Button b = new Button(s, i, RIGHT, fmH);
      b.setFont(Font.getFont(true,fmH*8/10));
      b.setForeColor(0x9B9EA3);
      b.setBorder(Button.BORDER_NONE);
      return b;
   }
   
   public int getPreferredHeight()
   {
      return fmH*9/2;
   }
}