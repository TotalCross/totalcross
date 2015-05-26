package tc.samples.like.fb;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;
import totalcross.util.*;

class PostData extends Container implements FBConstants
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
         catch (Exception e)
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
   
         add(create("Like", FBImages.like),LEFT,BOTTOM,PARENTSIZE-3,fmH*3/2);
         add(createRuler(Ruler.VERTICAL),AFTER,SAME,1,FILL);
         add(create("Comment", FBImages.comment),AFTER,BOTTOM,PARENTSIZE-3,fmH*3/2);
         add(createRuler(Ruler.VERTICAL),AFTER,SAME,1,FILL);
         add(create("Share", FBImages.share),AFTER,BOTTOM,FILL,fmH*3/2);
         
         add(createRuler(Ruler.HORIZONTAL),0,BEFORE,PARENTSIZE+100,1);
      }
      catch (Exception e)
      {
         FBUtils.logException(e);
      }
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
      return fmH*7;
   }
}