package tc.samples.sys.viewers;

import totalcross.io.*;
import totalcross.res.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;

public class ExternalViewers extends MainWindow
{
   public static final int BKGCOLOR = 0x0A246A;
   public static final int SELCOLOR = 0x829CE2; // Color.brighter(BKGCOLOR,120);

   private ButtonMenu menu;
   
   public ExternalViewers()
   {
      super("External Viewers",NO_BORDER);
      setTitle("");
      setUIStyle(Settings.Android);
      setBackColor(UIColors.controlsBack = Color.WHITE);
      Settings.enableWindowTransitionEffects = false;
      UIColors.messageboxBack = Color.brighter(BKGCOLOR,64);
      UIColors.messageboxFore = Color.WHITE;
      Settings.uiAdjustmentsBasedOnFontHeight = true;
   }
   public void initUI()
   {
      try
      {
         addHeaderBar();
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

   Bar headerBar;
   private void addHeaderBar() throws Exception
   {
      int c1 = 0x0A246A;
      Font f = font.adjustedBy(2,true);
      headerBar = new Bar("External Viewers");
      headerBar.setFont(f);
      headerBar.setBackForeColors(c1,Color.WHITE);
      headerBar.addButton(Resources.exit);
      add(headerBar, LEFT,0,FILL,PREFERRED);
      headerBar.addPressListener(new PressListener()
      {
         public void controlPressed(ControlEvent e)  
         {
            e.consumed = true;
            switch (((Bar)e.target).getSelectedIndex())
            {
               case 1:
               {
                  exit(0);
                  break;
               }  
            }
         }
      });
      
      addKeyListener(new KeyListener() // listen for BACK keypress (mapped to ESCAPE)
      {
         public void keyPressed(KeyEvent e) {}
         public void actionkeyPressed(KeyEvent e) {}
         public void specialkeyPressed(KeyEvent e)
         {
            if (e.key == SpecialKeys.ESCAPE)
            {
               e.consumed = true;
               exit(0);
            }
         }
      });
   }
}