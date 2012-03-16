package tc.samples.service.im.client;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;

public class MailHandler extends MainWindow
{
   private ListBox lblog;
   
   public MailHandler()
   {
      setUIStyle(Settings.Android);
   }
   
   public void initUI()
   {
      try
      {
         add(lblog = new ListBox(),LEFT,TOP,FILL,FILL);
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
