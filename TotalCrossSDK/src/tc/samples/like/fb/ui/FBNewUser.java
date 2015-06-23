package tc.samples.like.fb.ui;

import tc.samples.like.fb.*;
import tc.samples.like.fb.db.*;

import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class FBNewUser extends FBContainer 
{
   private Edit edMed;
   private Button btSave,btPhoto;
   private Image photo;

   private FBPosts posts;
   
   public FBNewUser(FBPosts posts)
   {
      this.posts = posts;
   }

   public void initUI()
   {
      try
      {
         setBackColor(Color.WHITE);
         setBorderStyle(BORDER_SIMPLE);
         borderColor = BORDER;
         
         add(btPhoto = createButton(null,FBImages.regPhoto,fmH),LEFT+50,TOP+50);         
         add(edMed = createEdit("Please type your username"), AFTER+50,SAME,FILL-100,PREFERRED);         
         add(btSave = createButton("SAVE", FBImages.status, fmH),LEFT,BOTTOM,PARENTSIZE-3,fmH*3/2);
         add(createRuler(Ruler.HORIZONTAL),LEFT+100,BEFORE,FILL-100,1);
      }
      catch (Throwable t)
      {
         FBUtils.logException(t);
      }
   }
   
   public int getPreferredHeight()
   {
      return fmH*9/2;
   }
   
   public void onEvent(Event e)
   {
      try
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
                     catch (Throwable t)
                     {
                        FBUtils.logException(t);
                     }
               }
               else
               if (e.target == btSave)
               {
                  if (edMed.getTrimmedLength() == 0)
                     Toast.show("Please enter a name in the field",2000);
                  else
                  if (FBDB.db.addUser(edMed.getText(), photo, true))
                  {
                     posts.reload();
                     Toast.show("Saved.",1000);
                  }
               }
         }
      }
      catch (Throwable t)
      {
         FBUtils.logException(t);
      }
   }
}