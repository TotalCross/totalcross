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
      setInsets(fmH/2,fmH/2,2,2);
      add(new Content(),LEFT,AFTER);
   }
}