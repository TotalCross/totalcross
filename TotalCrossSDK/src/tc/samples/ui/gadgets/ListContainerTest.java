package tc.samples.ui.gadgets;

import totalcross.ui.*;
import totalcross.ui.event.*;

public class ListContainerTest extends Container
{
   ListContainer lc;
   // attention: this is the old ListContainer style. See the ListContainerTest for the new style
   static int count;
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
         appId = ++count;
         add(chPaid = new Check(appId+" Paid"),LEFT,TOP);
         add(lDate = new Label("99/99/9999"),RIGHT,TOP); 
         add(new Label("US$"),LEFT,AFTER);
         add(lPrice = new Label("999.999.99"),AFTER,SAME);
         add(lDesc = new Label("",RIGHT),AFTER+10,SAME);
         lDesc.setText("description");
      }
   }
   
   public void initUI()
   {
      add(lc = new ListContainer(),LEFT,TOP,FILL,FILL);
      for (int i =0; i < 40; i++)
         lc.addContainer(new LCItem());
   }
   
   public void onEvent(Event e)
   {
      if (e.target == lc && e.type == ControlEvent.PRESSED)
      {
         Container sel = lc.getSelectedItem();
         lc.scrollToControl(sel);
      }
   }

}
