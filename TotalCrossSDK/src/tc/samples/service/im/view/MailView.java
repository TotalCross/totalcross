package tc.samples.service.im.view;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;

public class MailView extends MainWindow
{
   private ListBox lblog;
   private Button btInst;
   
   public MailView()
   {
      setUIStyle(Settings.Android);
   }
   
   public void initUI()
   {
      try
      {
         add(btInst = new Button("INSTALL SERVICE"),CENTER,TOP);
         add(lblog = new ListBox(),LEFT,AFTER+10,FILL,FILL);
         readFiles();
      }
      catch (FileNotFoundException fnfe)
      {
         log("File not found; stopping");
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
   
   public void onEvent(Event e)
   {
      if (e.type == ControlEvent.PRESSED && e.target == btInst)
      {
         log("Starting service");
         int ret = Vm.exec("\\MailService\\TaskMgr.exe","/startsvc TotalCrossSrv",0,true);
         log(ret == 0 ? "Started" : "Error");
      }
   }
   
   private void readFiles() throws IOException
   {
      String outFolder = Settings.onJavaSE ? "/msg" : Settings.platform.equals(Settings.ANDROID) ? "/sdcard/msg" : "/msg";
      for (int i = 1; i < 1000; i++)
      {
         String fileName = "toclient"+i+".txt";
         log("Looking for file "+fileName);
         File f = new File(Convert.appendPath(outFolder,fileName),File.READ_ONLY);
         int len = f.getSize();
         byte[] buf = new byte[len];
         f.readBytes(buf,0,len);
         f.close();
         
         log(new String(buf,0,len));
      }
   }

   public void log(String s)
   {
      lblog.addWrapping(s);
      lblog.selectLast();
   }
}
