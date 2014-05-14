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
   
   public void initUI()
   {
      super.initUI();
      try
      {
         if (!Settings.onJavaSE && !Settings.platform.equals(Settings.ANDROID) && !Settings.isIOS())
         {
            add(new Label("This program runs on\nthe Android or iOS platforms only",CENTER),CENTER,CENTER);
            return;
         }
         copyFiles2Sdcard();
         
         String[] items = {"Zoom image","Read PDF *","Open HTML page"};
         menu = new ButtonMenu(items, ButtonMenu.SINGLE_COLUMN);
         menu.textPosition = RIGHT_OF;
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
   
   String sdcardPath = "/sdcard/xviewers/";
   String jpg = "filhos_gui.jpg";
   String pdf = "TotalCrossCompanion.pdf";
   private void copyFiles2Sdcard() throws IllegalArgumentIOException, FileNotFoundException, IOException
   {
      try {new File("/sdcard/xviewers").createDir();} catch (Exception e) {}
      // extract the files from the tcz and copy them to the sdcard
      copyFile(jpg);
      copyFile(pdf);
   }
   
   private void copyFile(String name) throws IllegalArgumentIOException, FileNotFoundException, IOException
   {
      String fullPath = sdcardPath+name;
      if (!new File(fullPath).exists())
      {
         File f = new File(fullPath,File.CREATE_EMPTY);
         byte[] b = Vm.getFile(name);
         f.writeBytes(b);
         f.close();
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
               case 0: ret = Vm.exec("viewer",sdcardPath+jpg,0,true); break;
               case 1: ret = Vm.exec("viewer",sdcardPath+pdf,0,true); break;
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