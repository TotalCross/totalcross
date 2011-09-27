package tc.samples.ui.gadgets;

import totalcross.ui.*;

public class ListContainerTest extends Container
{
   // attention: this is the old ListContainer style. See the ListContainerTest for the new style
   class LCItem extends ScrollContainer
   {
      Label lDate,lPrice,lDesc;
      Check chPaid;
      
      public LCItem()
      {
         super(false);
      }
      
      public void initUI()
      {
         add(chPaid = new Check("Paid"),LEFT,TOP);
         add(lDate = new Label("99/99/9999"),RIGHT,TOP); 
         add(new Label("US$"),LEFT,AFTER);
         add(lPrice = new Label("999.999.99"),AFTER,SAME);
         add(lDesc = new Label("",RIGHT),AFTER+10,SAME);
         lDesc.setText("description");
      }
   }
   
   public void initUI()
   {
      ListContainer lc;
      add(lc = new ListContainer(),LEFT,TOP,FILL,FILL);
      for (int i =0; i < 10; i++)
         lc.addContainer(new LCItem());
   }

}
