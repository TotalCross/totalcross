package tc.samples.like.fb.ui;

import tc.samples.like.fb.*;

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
      setInsets(2,2,2,2);
      add(new PostInput(),CENTER,AFTER+25,PARENTSIZE+96,PREFERRED);
      //add(new News("Sponge Bob", "March 25, 2015 at 09:21 am", "I'm a very funny cartoon!","img/user.png"),CENTER,AFTER+25,PARENTSIZE+96,PREFERRED);
      //add(new News("Patrick Star", "March 25, 2015 at 09:31 am", "I'm his best pal!","img/user2.png"),CENTER,AFTER+25,PARENTSIZE+96,PREFERRED);
   }
}