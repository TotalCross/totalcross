package tc.samples.like.fb.ui;

import tc.samples.like.fb.*;

import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class FBNewUser extends Container implements FBConstants
{
   public FBEdit edMed;
   private Button btSave,btPhoto;
   
   public Image photo;
   
   public void initUI()
   {
      try
      {
         setBackColor(Color.WHITE);
         setBorderStyle(BORDER_SIMPLE);
         borderColor = BORDER;
         
         add(btPhoto = FBUtils.createButton(null,FBImages.regPhoto,fmH),LEFT+50,TOP+50);         
         edMed = new FBEdit("Please type your username");
         add(edMed, AFTER+50,SAME,FILL-100,PREFERRED);
         
         add(btSave = FBUtils.createButton("SAVE", FBImages.status, fmH),LEFT,BOTTOM,PARENTSIZE-3,fmH*3/2);
         add(FBUtils.createRuler(Ruler.HORIZONTAL),LEFT+100,BEFORE,FILL-100,1);
      }
      catch (Throwable e)
      {
         FBUtils.logException(e);
      }
   }
   
   public int getPreferredHeight()
   {
      return fmH*9/2;
   }
   
   public void onEvent(Event e)
   {
      switch (e.type)
      {
         case ControlEvent.PRESSED:
            if (e.target == btPhoto)
            {
               Image img = FBUtils.takePhoto();
               if (img != null)
                  try
                  {
                     btPhoto.setImage((photo = img).smoothScaledFixedAspectRatio(btSave.getHeight(),true));
                  }
                  catch (Throwable ee)
                  {
                     FBUtils.logException(ee);
                  }
            }
      }
   }
}