package tc.samples.api.sys;

import tc.samples.api.*;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;

public class ExternalViewersSample extends BaseContainer
{
   private ButtonMenu menu;
   String sdcardPath = "/sdcard/xviewers/";
   String jpg = "sys/filhos_gui.jpg";
   String pdf = "sys/TotalCrossCompanion.pdf";
   private static boolean isAndroid = Settings.platform.equals(Settings.ANDROID);
   
   public void initUI()
   {
      super.initUI();
      try
      {
         if (!Settings.onJavaSE && !isAndroid && !Settings.isIOS())
         {
            add(new Label("This program runs on the\nAndroid and iOS platforms only",CENTER),CENTER,CENTER);
            return;
         }
         if (Settings.isIOS())
            sdcardPath = Settings.appPath+"/";
         
         if (isAndroid || Settings.isIOS())
            copyFiles2Sdcard();
         
         String[] items = {"Zoom image","Read PDF *","Open HTML page"};
         menu = new ButtonMenu(items, ButtonMenu.SINGLE_COLUMN);
         menu.pressedColor = BKGCOLOR;
         menu.textGap = 400;
         menu.borderGap = 100;
         add(new Label("* Requires a third-party PDF reader"),CENTER,BOTTOM);
         add(menu,LEFT,AFTER,FILL,FIT,headerBar);
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
   
   private void copyFiles2Sdcard() throws IllegalArgumentIOException, FileNotFoundException, IOException
   {
      if (isAndroid) try {new File(sdcardPath).createDir();} catch (Exception e) {}
      // extract the files from the tcz and copy them to the sdcard
      copyFile(jpg);
      copyFile(pdf);
      
   }
   
   private String getTargetName(String name)
   {
      return sdcardPath+name.substring(name.lastIndexOf('/')+1);
   }
   
   private void copyFile(String name) throws IllegalArgumentIOException, FileNotFoundException, IOException
   {
      String fullPath = getTargetName(name);
      if (!new File(fullPath).exists())
      {
         try {new File(Convert.getFilePath(fullPath)).createDir();} catch (Exception e) {}
         new File(fullPath,File.CREATE_EMPTY).writeAndClose(Vm.getFile(name));
      }
   }

   public void onEvent(Event e)
   {
      if (e.type == ControlEvent.PRESSED && e.target == menu)
         try
         {
            int idx = menu.getSelectedIndex();
            int ret = 0;
            switch (idx)
            {
               case 0: ret = Vm.exec("viewer",getTargetName(jpg),0,true); break;
               case 1: ret = Vm.exec("viewer",getTargetName(pdf),0,true); break;
               case 2: Vm.exec("url","http://www.google.com/search?hl=en&source=hp&q=abraham+lincoln",0,true); break; // always returns 0
            }
            if (ret == -2)
               new MessageBox("Attention","Viewer returned: file not found").popup();
         }
         catch (Exception ee)
         {
            MessageBox.showException(ee,true);
         }
   }
}