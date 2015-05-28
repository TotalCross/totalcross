package tc.samples.like.fb.ui;

import tc.samples.like.fb.*;

import totalcross.ui.*;
import totalcross.ui.gfx.*;

public class PostInput extends Container implements FBConstants
{
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
         add(new ImageControl(FaceBookUI.defaultPhoto.smoothScaledFixedAspectRatio(fmH*2,true)),LEFT+50,TOP+50);         
      }
      catch (Throwable e)
      {
         Container c = new Container();
         c.setBackColor(CNT_BACK);
         add(c,LEFT+50,TOP+50,fmH*2,fmH*2);
      }
      med = new FBEdit("What's on your mind?");
      add(med, AFTER+50,SAME,FILL-100,PREFERRED);
      
      add(FBUtils.createButton("STATUS", FBImages.status, fmH),LEFT,BOTTOM,PARENTSIZE-3,fmH*3/2);
      add(FBUtils.createButton("PHOTO", FBImages.photo, fmH),AFTER,BOTTOM,PARENTSIZE-3,fmH*3/2);
      add(FBUtils.createButton("CHECK-IN", FBImages.checkin, fmH),AFTER,BOTTOM,FILL,fmH*3/2);
      
      add(FBUtils.createRuler(Ruler.HORIZONTAL),LEFT+100,BEFORE,FILL-100,1);
   }
   
   public int getPreferredHeight()
   {
      return fmH*9/2;
   }
}