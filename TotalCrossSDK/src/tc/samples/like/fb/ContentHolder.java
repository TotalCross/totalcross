package tc.samples.like.fb;

import totalcross.ui.*;

class ContentHolder extends ScrollContainer implements FBConstants
{
   public ContentHolder()
   {
      super(false,true);
      setBackColor(CONTENTH);         
   }
   
   public void initUI()
   {
      setInsets(2,2,2,2);
      add(new ContentStatus("img/user.png"),CENTER,AFTER+25,PARENTSIZE+96,PREFERRED);
      add(new Content("Sponge Bob", "March 25, 2015 at 09:21 am", "I'm a very funny cartoon!","img/user.png"),CENTER,AFTER+25,PARENTSIZE+96,PREFERRED);
      add(new Content("Patrick Star", "March 25, 2015 at 09:31 am", "I'm his best pal!","img/user2.png"),CENTER,AFTER+25,PARENTSIZE+96,PREFERRED);
   }
}