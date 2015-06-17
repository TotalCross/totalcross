package tc.samples.app.store;

import totalcross.res.*;
import totalcross.ui.*;
import totalcross.ui.gfx.*;

public class PSProductList extends Container
{
   Bar bar;
   Container c;
   
   public void initUI()
   {
      setInsets(0,0,2,2);

      bar = new Bar("Products List");
      bar.addButton(Resources.menu); 
      bar.setForeColor(Color.WHITE);
      add(bar, LEFT,TOP,FILL,PREFERRED);
      
      c = new Container();
      c.setBackColor(0xDDDDDD);
      add(c, LEFT,AFTER,FILL,FILL);
      
      reload();
   }

   public void reload()
   {
      c.removeAll();
      try
      {
         c.add(new PSProduct(),LEFT+2,AFTER+25,PARENTSIZE+48,PREFERRED);
      }
      catch (Throwable t)
      {
         t.printStackTrace();
      }
   }
}
