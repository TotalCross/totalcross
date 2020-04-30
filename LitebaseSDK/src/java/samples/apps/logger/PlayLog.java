// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package samples.apps.logger;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.Color;
import litebase.*;

// juliana@250_9: added a new sample: LoggerSample. This is to execute a Litebase log file and try to reproduce a problem.  
/**
 * A sample to help in reproducing errors by executing a logger file.
 */
public class PlayLog extends MainWindow
{
   /**
    * The connection with Litebase.
    */
   private LitebaseConnection driver;
   
   /**
    * The button to choose the file.
    */
   private Button button;
   
   /**
    * The default path.
    */
   private String dataPath = (Settings.dataPath != null && Settings.dataPath.length() != 0? Settings.dataPath : Settings.appPath);
   
   static
   {
      Settings.useNewFont = true;
   }
   
   /**
    * The constructor.
    */
   public PlayLog()
   {
      super("Play Log", TAB_BORDER);
      setUIStyle(Settings.Android);
   }
   
   /**
    * Initializes the user interface.
    */
   public void initUI()
   {
      (button = new Button("  Choose file  ")).setBackForeColors(Color.WHITE, Color.BLACK);
      add(new Label("Load logger file:"), LEFT + 5, TOP + 5);
      add(button, AFTER + 5, SAME);
   }
   
   /**
    * Called to process the event when the button is pressed.
    *
    * @param event The event to be processed.
    */
   public void onEvent(Event event)
   {      
      if (event.type == ControlEvent.PRESSED && event.target == button)
      {
         try
         {
            // Opens a file chooser box for choosing a Litebase log file.
            FileChooserBox fcb = new FileChooserBox(new FileChooserBox.Filter()
            {
               // Shows only Litebase log files.
               public boolean accept(File file) throws IOException
               {                  
                  String path = file.getPath();
                  return file.isDir() || (path.indexOf("LITEBASE_") >= 0 && path.endsWith(".LOGS"));
               }
            });
            
            fcb.mountTree("device/");
            fcb.popup();
                       
            // Only process a logger file if there is a chosen file to be processed.
            String loggerName = fcb.getAnswer();
            if (loggerName != null && loggerName.length() > 0)
            {               
               LineReader lineReader = new LineReader(new File(loggerName, File.READ_ONLY)); // Opens the log file.
               String string;
               int i;
               
               try // Erases the test database if it exists.
               {
                  LitebaseConnection.dropDatabase("Test", dataPath, 1); // Erases the previous tables.
               }
               catch (DriverException exception) {}
               
               // Tells the user that the logger is being executed.
               MessageBox pleaseWait = new MessageBox("Please Wait", "Processing log file...", null);
               pleaseWait.popupNonBlocking();
               
               while ((string = lineReader.readLine()) != null) // Executes the possible log sql commands. Do not simulate prepared statements.
               {   
                  // Transforms the string into lower case, trims it and removes possible indications that it is a prepared statement.
                  if ((string = string.toLowerCase().trim()).startsWith("prep: "))
                     string = string.substring(6);
                  
                  // Changes a blob indication with null. This may difficult finding problems with blobs.
                  string = Convert.replace(string, "[blob]", "null");
                  
                  try
                  {
                     if (string.startsWith("select"))
                        driver.executeQuery(string);
                     else if (string.startsWith("update") || string.startsWith("insert") 
                           || string.startsWith("delete") ||string.startsWith("drop") || string.startsWith("alter"))
                        driver.executeUpdate(string);
                     else if (string.startsWith("create"))
                        driver.execute(string);
                     else if (string.startsWith("preparestatement"))
                        driver.prepareStatement(string.substring(17));
                     else if (string.startsWith("new"))
                        driver = LitebaseConnection.getInstance("Test");
                     else if (string.startsWith("close"))
                        driver.closeAll();
                     else if (string.startsWith("purge"))
                        driver.purge(string.substring(6));
                     else if (string.startsWith("getrowcountdeleted"))
                        driver.getRowCountDeleted(string.substring(19));
                     else if (string.startsWith("getrowcount"))
                        driver.getRowCount(string.substring(12));
                     else if (string.startsWith("getcurrentrowid"))
                        driver.getCurrentRowId(string.substring(16));
                     else if (string.startsWith("getrowiterator"))
                        driver.getRowIterator(string.substring(15));
                     else if (string.startsWith("convert"))
                        driver.convert(string.substring(8));
                     else if (string.startsWith("recover table"))
                        driver.convert(string.substring(14));
                     else if (string.startsWith("setrowinc"))
                     {
                        string = string.substring(10);
                        i = string.indexOf(' '); 
                        driver.setRowInc(string.substring(0, i), Convert.toInt(string.substring(i + 1, string.length())));
                     }
                     else if (string.length() > 0)
                        Vm.debug("not executed - " + string);
                  }
                  catch (RuntimeException exception)
                  {
                     Vm.debug(string);
                     Vm.debug(exception.toString());
                  }
               }
               
               // Tells the user that the processing has finished.
               pleaseWait.unpop();
               new MessageBox("Finished", "Finished processing").popup();
            }
         }
         catch (IOException exception)
         {
            MessageBox.showException(exception, true);
         }
         catch (InvalidNumberException exception)
         {
            MessageBox.showException(exception, true);
         }
      }
   }
}
