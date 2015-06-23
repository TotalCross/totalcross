package tc.samples.like.fb.ui;

import tc.samples.like.fb.*;
import tc.samples.like.fb.db.*;

import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class PostInput extends FBContainer 
{
   private Edit med;
   private Button btStat, btPhot, btChIn;
   private FBPosts posts;
   
   public PostInput(FBPosts posts)
   {
      this.posts = posts;
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
      catch (Throwable t)
      {
         Container c = new Container();
         c.setBackColor(CNT_BACK);
         add(c,LEFT+50,TOP+50,fmH*2,fmH*2);
      }
      med = createEdit("What's on your mind?");
      add(med, AFTER+50,SAME,FILL-100,PREFERRED);
      
      add(btStat = createButton("STATUS", FBImages.status, fmH),LEFT,BOTTOM,PARENTSIZE-3,fmH*3/2);
      add(btPhot = createButton("PHOTO", FBImages.photo, fmH),AFTER,BOTTOM,PARENTSIZE-3,fmH*3/2);
      add(btChIn = createButton("CHECK-IN", FBImages.checkin, fmH),AFTER,BOTTOM,FILL,fmH*3/2);
      
      add(createRuler(Ruler.HORIZONTAL),LEFT+100,BEFORE,FILL-100,1);
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
               if (e.target == btStat)
               {
                  if (med.getTrimmedLength() == 0)
                     Toast.show("Fill what's on your mind and press this button to post",2000);
                  else
                  if (FBDB.db.addPost(med.getText()))
                     posts.reload();
               }
               else
               if (e.target == btPhot)
               {
                  Image photo = FBUtils.takePhoto();
                  if (photo != null && FBDB.db.addPost(photo))
                     posts.reload();
               }
               else
               if (e.target == btChIn)
               {
                  String coords = FBUtils.getCoords();
                  if (coords != null && FBDB.db.addPost(coords))
                     posts.reload();
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