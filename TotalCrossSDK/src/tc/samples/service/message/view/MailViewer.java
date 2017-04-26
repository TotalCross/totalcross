package tc.samples.service.message.view;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;

public class MailViewer extends MainWindow
{
   static
   {
      Settings.useNewFont = true;
      Settings.vibrateMessageBox = true;
      Settings.uiAdjustmentsBasedOnFontHeight = true;
   }
   
   private ListBox lb;
   
   public MailViewer()
   {
      setUIStyle(Settings.Android);
   }
   
   public void initUI()
   {
      final Button btn = new Button("Exit");
      add(btn,LEFT,TOP,FILL,PREFERRED);
      add(lb = new ListBox(), LEFT,AFTER+50,FILL,FILL);
      showMessage();
      btn.addPressListener(new PressListener()
      {
         public void controlPressed(ControlEvent e)
         {
            exit(0);
         }
      });
   }
   
   public void log(String s)
   {
      lb.addWrapping(s);
      lb.selectLast();
   }

   public void showMessage()
   {
      String inFolder = !Settings.platform.equals(Settings.ANDROID) ? "/msg" : "/sdcard/msg";
      byte []buf = new byte[1024];
      try
      {
         String[] files = new File(inFolder).listFiles();
         if (files == null)
            exit(0);
         else
            for (int i = 0; i < files.length; i++)
               if (files[i].endsWith(".txt"))
               {
                  String fileName = files[i];
                  log("reading file "+fileName);
                  // load file
                  File f = new File(Convert.appendPath(inFolder,fileName),File.READ_WRITE);
                  int len = f.getSize();
                  if (buf.length < len) buf = new byte[len];
                  f.readBytes(buf,0,len);
                  f.delete();
                  // show file
                  log(new String(buf,0,len));
               }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
}
