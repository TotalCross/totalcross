package tc.samples.ui.controls;

import totalcross.ui.*;

public class ImageBookSample extends BaseContainer
{
   public void initUI()
   {
      super.initUI();
      setTitle("Image Book");
      ScrollContainer sc = new ScrollContainer(false, true);
      sc.setInsets(gap,gap,gap,gap);
      add(sc,LEFT,TOP,FILL,FILL);
   }
}
