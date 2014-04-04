package tc.samples.ui.controls;

import totalcross.ui.*;

public class DynScrollContainerSample extends BaseContainer
{
   public void initUI()
   {
      super.initUI();
      setTitle("AlignedLabelsContainer");
      ScrollContainer sc = new ScrollContainer(false, true);
      sc.setInsets(gap,gap,gap,gap);
      add(sc,LEFT,TOP,FILL,FILL);
   }
}
