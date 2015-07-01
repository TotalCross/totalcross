package tc.samples.app.store;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;
import totalcross.util.*;

public class TCProductStore extends MainWindow
{
   public static String imagePath;

   public TCProductStore()
   {
      setUIStyle(Settings.Holo);
      setBackColor(0x004000);
      Settings.uiAdjustmentsBasedOnFontHeight = true;
   }
   
   public void initUI()
   {
      checkImages();
      new PSlogin().swapToTopmostWindow();
   }

   private void checkImages()
   {
      // create randomly colorized images on disk so they can be read by the image list
      Label l = null;
      try
      {
         imagePath = Settings.appPath+"/images/";
         Vm.debug("Image path: "+imagePath);
         
         File f = new File(imagePath);
         if (!f.exists())
            f.createDir();
         
         f = new File(imagePath+"almof00.png");
         if (!f.exists())
         {
            l = new Label("Preparing sample images...");
            l.setForeColor(Color.WHITE);
            add(l,CENTER,CENTER);
            repaintNow();
            Random r = new Random();
            Image orig = new Image("img/almof.png").getSmoothScaledInstance(fmH*3,fmH*3);
            ByteArrayStream bas = new ByteArrayStream(40*1024);
            for (int i = 0; i < 100; i++)
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
