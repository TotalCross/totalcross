package tc.samples.like.fb.ui;

import tc.samples.like.fb.*;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;
import totalcross.util.*;

public class PostData extends Container implements FBConstants
{
   public String name, text;
   public int likes;
   private Image icon;
   private Time t;
   
   public PostData(String name, String text, Image icon, int likes, Time t)
   {
      this.name = name;
      this.text = text;
      this.icon = icon;
      this.likes = likes;
      this.t = t;
   }
   
   public void initUI()
   {
      try
      {
         setBackColor(Color.WHITE);
         setBorderStyle(BORDER_SIMPLE);
         borderColor = BORDER;
         
         try
         {
            add(new ImageControl(icon.hwScaledFixedAspectRatio(fmH*2,true)),LEFT+50,TOP+50);         
         }
         catch (Throwable e)
         {
            Container c = new Container();
            c.setBackColor(CNT_BACK);
            add(c,LEFT+50,TOP+50,fmH*2,fmH*2);
         }
         Label lname = new Label(name);
         lname.setFont(Font.getFont(true, fmH*8/10));
         add(lname, AFTER+50,SAME);
         
         Label linfo = new Label(new Date(t)+" at "+t);
         linfo.setFont(Font.getFont(false, fmH*8/10));
         add(linfo, SAME,AFTER);
         
         add(new Label(text),LEFT+50, AFTER+25);
         
         Label llikes = new Label(likes+" likes");
         llikes.setFont(Font.getFont(false, fmH*8/10));
         add(llikes,LEFT+50,AFTER+25);
   
         add(FBUtils.createButton("Like", FBImages.like, fmH),LEFT,BOTTOM,PARENTSIZE-3,fmH*3/2);
         add(FBUtils.createRuler(Ruler.VERTICAL),AFTER,SAME,1,FILL);
         add(FBUtils.createButton("Comment", FBImages.comment, fmH),AFTER,BOTTOM,PARENTSIZE-3,fmH*3/2);
         add(FBUtils.createRuler(Ruler.VERTICAL),AFTER,SAME,1,FILL);
         add(FBUtils.createButton("Share", FBImages.share, fmH),AFTER,BOTTOM,FILL,fmH*3/2);
         
         add(FBUtils.createRuler(Ruler.HORIZONTAL),0,BEFORE,PARENTSIZE+100,1);
      }
      catch (Throwable e)
      {
         FBUtils.logException(e);
      }
   }
   
   public int getPreferredHeight()
   {
      return fmH*7;
   }
}