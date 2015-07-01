package tc.samples.app.store;

import totalcross.io.*;
import totalcross.res.*;
import totalcross.ui.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class PSProductList extends Container
{
   Bar bar;
   Container c;
   BulkImageLoader bil;
   
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
         
         c = new ScrollContainer(false,true);
         c.setBackColor(0xDDDDDD);
         add(c, LEFT,AFTER,FILL,FILL);
       
         bil = new BulkImageLoader(3, 10, TCProductStore.imagePath, new File(TCProductStore.imagePath).listFiles());
         bil.start();
         
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
         for (int i = 0, n = bil.arqs.length; i < n; )
         {
            c.add(new PSProduct(bil.new DynImage(i),"Pillow "+i, 100+i),PARENTSIZE+((i&1)==0 ? 25 : 75), AFTER+25, PARENTSIZE+46,PREFERRED);
            i++;
            if (i < n)
               c.add(new PSProduct(bil.new DynImage(i),"Pillow "+i, 100+i),PARENTSIZE+((i&1)==0 ? 25 : 75), SAME, PARENTSIZE+46,PREFERRED);
            i++;
         }
      }
      catch (Throwable t)
      {
         t.printStackTrace();
      }
   }
}
