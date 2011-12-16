/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/


 
package samples.sys.migration;

import litebase.*;
import totalcross.io.*;
import totalcross.sys.Settings;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.dialog.*;
import totalcross.util.Vector;

/**
 * Lists all .db files from the tables found.
 */
class ListDBs extends Container
{
   /**
    * The application if used.
    */
   String appCrid;
   
   /**
    * The button to migrate the tables.
    */
   private Button btnMigrate;
   
   /**
    * The button to return to the main screen again.
    */
   private Button btnBack;
   
   /**
    * The grid to list the tables.
    */
   private Grid grid;

   /**
    * Initializes this container interface.
    */
   public void initUI()
   {
      Label label = new Label("Choose the tables to migrate");
      
      add(label, LEFT + 1, TOP + 1);
      add(grid = new Grid(new String[]{"Table names"}, true));
      add(btnBack = new Button(" Back "), RIGHT, BOTTOM - 2);
      add(btnMigrate = new Button(" Migrate "), BEFORE - 2, SAME);
      grid.setRect(LEFT + 3, AFTER + 3, FILL, FIT - 2, label);
      try
      {
         fillGrid(); // Fills the table grid.
      }
      catch (IOException exception)
      {
         new MessageBox("ERROR", exception.getMessage()).popup();
      }
   }

   /** 
    * When the container is added for the first time, the method <code>initUI()</code> is called, so the user interface can be initialized.
    * From the second time and up that the container is added, the <code>onAddAgain()</code> method is called instead.
    */
   public void onAddAgain()
   {
      try
      {
         fillGrid(); // Fills the table grid.
      }
      catch (IOException exception)
      {
         new MessageBox("ERROR", exception.getMessage()).popup();
      }
   }

   /**
    * Fills the table grid.
    * 
    * @throws IOException If an internal method throws it.
    */
   private void fillGrid() throws IOException
   {
      grid.clear();
      Vector tables = getTables(appCrid);
      int i = tables.size();
      Object[] items = tables.items;
      
      // Repopulates the grid.
      grid.removeAllElements();
      while (--i >= 0)
         grid.add(new String[]{items[i].toString()});
      btnMigrate.setEnabled(tables.size() > 0);
      grid.repaintNow();
   }

   /**
    * Called to process posted events.
    *
    * @param event The posted event.
    */
   public void onEvent(Event event)
   {
      switch (event.type)
      {
         case ControlEvent.PRESSED:
         {
            if (event.target == btnMigrate)
            {
               LitebaseConnection driver = LitebaseConnection.getInstance(appCrid, Settings.appSettings);
               int i = grid.size();
               boolean repaint = false,
                       error = false;
               MessageBox msb = new MessageBox("ATTENTION", "This operation can | take a while...");
               
               msb.popupNonBlocking();
               
               while (--i >= 0)
                  if (grid.isChecked(i))
                  {
                     String name = grid.getCellText(i, 1);
                     try
                     {
                        driver.convert(name);
                     }
                     catch (DriverException exception)
                     {
                        new MessageBox("ERROR", exception.getMessage()).popup();
                        error = true;
                        break;
                     }
                     repaint = true;
                  }
               msb.unpop();
               if (repaint)
               {
                  if (error == false)
                     new MessageBox("SUCCESSFUL","The selected tables | were migrated! ").popupNonBlocking();
                  try
                  {
                     fillGrid();
                  }
                  catch (IOException exception)
                  {
                     new MessageBox("ERROR", exception.getMessage());
                  }
               }
               else if (error == false)
                  new MessageBox("ATTENTION","There's no selected | tables! ").popupNonBlocking();
            }
            if (event.target == btnBack)
            {
               Migration.mig.swap(Migration.mig.mainUI);
            }
            break;
         }
      }
   }

   /**
    * Fetches all the tables in the data path with the given application id.
    * 
    * @param crid The application id.
    * @return A <code>Vector</code> with all the desired table names.
    * @throws IOException If an internal method throws it.
    */
   public static Vector getTables(String crid) throws IOException
   {
      String[] tables = new File(Settings.appSettings, File.DONT_OPEN, 1).listFiles(); 
      Vector vector = new Vector();
      
      if (tables != null)
      {
         int i = tables.length;
         String tableName;
         
         if (i == 0)
            new MessageBox("ATTENTION", "There's no table|for this creator Id").popup();
         
         while (--i >= 0)
            if ((tableName = tables[i]).endsWith(".db") && tables[i].startsWith(crid + "-"))
               vector.addElement(tableName.substring(5, tableName.length() - 3));
         vector.qsort();
      }
      return vector;
   }
}
