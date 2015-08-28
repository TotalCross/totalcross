package tc.samples.api.ui.transluc;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;
import totalcross.ui.image.BulkImageLoader.*;
import totalcross.util.*;

public class ListScreen extends TranslucentBaseContainer
{
   private static ListScreen inst;
   public static ListScreen getInstance() {return inst == null ? inst = new ListScreen() : inst;}

   public class Product extends Container
   {
      private String description;
      private BulkImageLoader.DynImage photo;
      private double price;
      
      public Product(DynImage photo, String description, double price)
      {
         this.description = description;
         this.price = price;
         this.photo = photo;
      }
      
      public void initUI()
      {
         Label l;
         setTranslucent(TranslucentShape.LESS_ROUND);
         int gap = fmH/2;
         add(photo, LEFT+gap,TOP+gap,FILL-gap,fmH*3);
         l = new Label(description);
         setTranslucentProps(l);
         add(l,LEFT+gap,AFTER+gap/2); 
         l = new Label(Convert.toString(price, 2),LEFT,Color.RED,true);
         setTranslucentProps(l);
         add(l,RIGHT-gap,AFTER); 
      }
      
      /** The preferred height is 6 times the font height */
      public int getPreferredHeight()
      {
         return fmH*6;
      }
      
      public void onEvent(Event e)
      {
         if (e.target == this && e.type == ControlEvent.PRESSED)
         {
            // do something 
         }
      }
   }

   Container clist;
   BulkImageLoader bil;
   public static String imagePath;

   public void initUI()
   {
      try
      {
         super.initUI();
         setBackgroundImage(3);

         Button t4 = createTransBarButton("ui/images/bt_back.png");
         t4.setBackColor(Color.BLACK);
         add(t4, RIGHT,TOP,fmH*2,fmH*2);
         t4.addPressListener(new PressListener() {public void controlPressed(ControlEvent e) {goback();}});
         
         Label l = new Label("Products List",CENTER,Color.WHITE, true);
         l.setTranslucent(TranslucentShape.RECT);
         add(l, LEFT,TOP,FIT,SAME);

         clist = new ScrollContainer(false,true);
         clist.transparentBackground = true;
         add(clist, LEFT,AFTER+fmH/2,FILL,FILL-fmH/2);
       
         // create the image loader
         setupFiles();
         
         reload();
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }

   private void setupFiles() throws IllegalArgumentIOException, IOException
   {
      imagePath = Settings.appPath+"/images/";
      Vm.debug("Image path: "+imagePath);
      
      File f = new File(imagePath);
      if (!f.exists())
         f.createDir();
      checkImages();

      bil = new BulkImageLoader(3, 10, imagePath, new File(imagePath).listFiles());
      bil.start();
   }

   public void reload()
   {
      // add all products
      clist.removeAll();
      try
      {
         for (int i = 0, n = bil.arqs.length; i < n; )
         {
            clist.add(new Product(bil.new DynImage(i),"Cushion "+i, 100+i),PARENTSIZE+25, AFTER+50, PARENTSIZE+46,PREFERRED);
            i++;
            if (i < n)
               clist.add(new Product(bil.new DynImage(i),"Cushion "+i, 100+i),PARENTSIZE+75, SAME, SAME, SAME);
            i++;
         }
      }
      catch (Throwable t)
      {
         t.printStackTrace();
      }
   }

   private void checkImages()
   {
      // create randomly colorized images on disk so they can be read by the image list
      Label l = null;
      try
      {
         File f = new File(imagePath+"almof00.png");
         if (!f.exists())
         {
            // create and same images
            Random r = new Random();
            Image orig = new Image("ui/images/almof.png").getSmoothScaledInstance(fmH*3,fmH*3);
            ByteArrayStream bas = new ByteArrayStream(40*1024);
            for (int i = 0; i < 40; i++)
            {
               bas.reset();
               Image c = orig.getCopy();
               c.applyColor2(Color.getRandomColor(r));
               c.createPng(bas);
               new File(imagePath+"almof"+(i<10?"0"+i:i)+".png",File.CREATE_EMPTY).writeAndClose(bas.toByteArray());
            }
         }
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
      if (l != null)
         remove(l);
   }
}
