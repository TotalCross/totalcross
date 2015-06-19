package tc.samples.app.store;

import totalcross.res.*;
import totalcross.ui.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class PSProductList extends Container
{
   Bar bar;
   Container c;
   
   public void initUI()
   {
      try
      {
         setBackColor(Color.WHITE);
         setInsets(0,0,2,2);
   
         bar = new Bar("Products List");
         bar.titleAlign = CENTER;
         Image ic = Resources.menu.getCopy();
         ic.applyColor2(0);
         bar.addButton(ic, false); 
         bar.setForeColor(0x009900);
         bar.ignoreInsets = true;
         add(bar, LEFT,TOP,FILL,PREFERRED);
         
         c = new Container();
         c.setBackColor(0xDDDDDD);
         add(c, LEFT,AFTER,FILL,FILL);
         
         reload();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
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
