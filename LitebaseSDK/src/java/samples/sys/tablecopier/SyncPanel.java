/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



package samples.sys.tablecopier;

import totalcross.io.sync.*;
import totalcross.ui.*;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;
import totalcross.io.*;
import totalcross.sys.*;

/**
 * The painel to do the sincronization.
 */
public class SyncPanel extends Container
{  
   /**
    * The list box to list the files synchronized.
    */
   private ListBox listBox;
   
   /**
    * The progress bar which indicates the synchronization progress.
    */
   private ProgressBar progressBar;

   /**
    * The appPath of the device.
    */
   protected String targetAppPath;

   /**
    * Initializes the user interface and lists the device files.
    */
   public void initUI()
   {
      MainWindow.setDefaultFont(Font.getFont(false, 12));
      if (Settings.onJavaSE || (Settings.appSettings != null && !Settings.appSettings.equals("no")))
      {
         // First the volumes are listed.
         add(progressBar = new ProgressBar(), LEFT, TOP + 2, FILL, PREFERRED);
         add(listBox = new ListBox(), LEFT, AFTER + 2, FILL, FILL);
         progressBar.textColor = Color.CYAN;
         progressBar.suffix = " of 0";
         progressBar.highlight = true;
         repaintNow();

         String[] files = RemoteFile.listFiles("");
         if (files == null) // NOT PALMOS!
            files = RemoteFile.listFiles(targetAppPath);
         else // PALMOS!
         {
            int vol = -1,
                i = files.length;
            String fileVol;
            
            while (--i >= 0)
            {
               if ((fileVol = files[i]).indexOf("builtin") >= 0)
                  try
                  {
                     vol = Convert.toInt(fileVol.substring(0, fileVol.indexOf(':')));
                     break;
                  }
                  catch (InvalidNumberException exception) {}
            }
            
            log("Internal Volume: #" + vol);
            files = RemoteFile.listFiles(targetAppPath = (vol + ":/Litebase_DBs/"));
         }

         try
         {
            File file = new File("c:\\Litebase_DBs\\from\\");
            if (!file.exists())
               file.createDir();
            if (!(file = new File("c:\\Litebase_DBs\\to\\")).exists())
               file.createDir();
            if (files == null) // guich@tc101_42: shows this message only for the fromRemote() step. 
            {
               log("Litebase's database folder not found");
               log("or no files found on such folder");
            }
            else 
               copyFromRemote(files);
            copyToRemote();
         }
         catch (IOException exception)
         {
            new MessageBox("ERROR", exception.getMessage()).popup();
         }
         log("Sincronization finished.");
         new MessageBox("OK", "Sincronization finished.").popup();
         repaintNow();
      }
   }

   /**
    * Copies the remote files to the desktop.
    * 
    * @param files The files to be copied.
    */
   private void copyFromRemote(String []files)
   {
      log("DEVICE -> DESKTOP");
      progressBar.max = files.length;
      progressBar.suffix = " of " + progressBar.max;
      progressBar.setValue(0); // guich@tc101_44
      
      int i = -1,
          n = files.length;
      String file;
      
      // Copies all the files.
      while (++i < n)
      {
         if ((file = files[i]) != null && file.length() > 0 && !file.endsWith("/"))
         {
            String from = targetAppPath + file;
            String to = "c:\\Litebase_DBs\\from\\" + file;
            log("Copied " + from + ": " + (RemoteFile.copyFromRemote(from, to)? "success" : "failure"));
            progressBar.setValue(i + 1); // guich@tc101_44
         }
      }
   }

   /**
    * Logs a string to the list box.
    * 
    * @param string The string to be logged.
    */
   private void log(String string)
   {
      listBox.add(string);
      listBox.selectLast();
   }

   /**
    * Copies the desktop files to the device.
    * 
    * @throws IOException If an <code>IOException is thrown by an internal method.
    */
   private void copyToRemote() throws IOException
   {
      log("DESKTOP -> DEVICE");
      String[] files = new File("c:\\Litebase_DBs\\to\\").listFiles();
      if (files == null)
         log("No files to be copied");
      else
      {
         progressBar.max = files.length;
         progressBar.suffix = " of " + progressBar.max;
         progressBar.setValue(0); // guich@tc101_44
         
         int i = -1,
             n = files.length;
         String file;
         
         // Copies all the files.
         while (++i < n)
         {
            if ((file = files[i]) != null && file.length() > 0 && !file.endsWith("/"))
            {
               String from = "c:\\Litebase_DBs\\to\\" + file;
               String to = targetAppPath + file;
               log("Copied " + file + ": "+ (RemoteFile.copyToRemote(from, to)? "success" : "failure"));
               progressBar.setValue(i + 1); // guich@tc101_44
            }
         }
      }
   }
}
