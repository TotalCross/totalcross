package tc.samples.app.store;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;
import totalcross.ui.image.BulkImageLoader.DynImage;

public class PSProduct extends Container
{
   private String productName;
   private BulkImageLoader.DynImage dyn;
   private double price;
   
   public PSProduct(DynImage dyn, String productName, double price)
   {
      this.productName = productName;
      this.price = price;
      this.dyn = dyn;
   }
   
   public void initUI()
   {
      setBackColor(Color.WHITE);
      add(dyn, LEFT+50,TOP+50,FILL-50,fmH*3);
      add(new Label(productName),LEFT+50,AFTER+25);
      add(new Label(Convert.toString(price, 2),LEFT,Color.RED,true),RIGHT-50,AFTER);
   }
   
   public int getPreferredHeight()
   {
      return fmH*6;
   }
}
