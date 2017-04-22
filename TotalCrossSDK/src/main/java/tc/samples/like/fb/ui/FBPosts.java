package tc.samples.like.fb.ui;

import tc.samples.like.fb.*;
import tc.samples.like.fb.db.*;

import totalcross.ui.*;

public class FBPosts extends ScrollContainer implements FBConstants
{
   public FBPosts()
   {
      super(false,true);
      setBackColor(CONTENTH);
   }
   
   public void initUI()
   {
      setInsets(0,0,2,2);
      reload();
   }

   public void reload()
   {
      removeAll();
      try
      {
         add(new PostInput(this),CENTER,TOP+25,PARENTSIZE+96,PREFERRED);
         for (PostData p: FBDB.db.getPosts())
            add(p,CENTER,AFTER+25,PARENTSIZE+96,PREFERRED);
      }
      catch (Throwable t)
      {
         FBUtils.logException(t);
      }
   }
}