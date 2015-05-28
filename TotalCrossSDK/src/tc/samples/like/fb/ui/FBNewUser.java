package tc.samples.like.fb.ui;

import tc.samples.like.fb.*;

import totalcross.ui.*;
import totalcross.ui.gfx.*;

public class FBNewUser extends Container implements FBConstants
{
   public FBEdit med;
   private Button save;
   
   public void initUI()
   {
      setBackColor(Color.WHITE);
      setBorderStyle(BORDER_SIMPLE);
      borderColor = BORDER;
      
      try
      {
         add(FBUtils.createButton(null,FBImages.regPhoto,fmH),LEFT+50,TOP+50);         
      }
      catch (Exception e)
      {
         Container c = new Container();
         c.setBackColor(CNT_BACK);
         add(c,LEFT+50,TOP+50,fmH*2,fmH*2);
      }
      med = new FBEdit("Please type your username");
      add(med, AFTER+50,SAME,FILL-100,PREFERRED);
      
      add(FBUtils.createButton("SAVE", FBImages.status, fmH),LEFT,BOTTOM,PARENTSIZE-3,fmH*3/2);
      add(FBUtils.createRuler(Ruler.HORIZONTAL),LEFT+100,BEFORE,FILL-100,1);
   }
   
   public int getPreferredHeight()
   {
      return fmH*9/2;
   }
}