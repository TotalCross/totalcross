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

package samples.apps.addressbook;

import litebase.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.util.*;

/** 
 * A simple address book application that demonstrates a real case where Litebase can be used. It also shows how to use the grid.
 */
public class AddressBook extends MainWindow implements Grid.DataSource
{
   /**
    * The connection with Litebase.
    */
   LitebaseConnection driver;
   
   /**
    * The tabbed container for changing windows.
    */
   private TabbedContainer container;
   
   /**
    * The grid for the table contents.
    */
   private Grid grid;
   
   /**
    * The object to edit people data.
    */
   private Edition edit;
   
   /**
    * The current result set used.
    */
   ResultSet activeRS;
   
   /**
    * A prepared statement to list all the table elements.
    */
   PreparedStatement psList;

   static
   {
      Settings.useNewFont = true;
   }
   
   /**
    * The constructor.
    */
   public AddressBook()
   {
      super("TC Address Book", TAB_ONLY_BORDER);
      if (Settings.onJavaSE)
         totalcross.sys.Settings.showDesktopMessages = false;
      setUIStyle(Settings.Android);
      driver = LitebaseConnection.getInstance("ABok");
   }

   /**
    * Initializes the user interface.
    */
   public void initUI()
   {
      createTables(); // Data base setup stage.

      // User interface set up stage.
      String[] tpCaptions = {"Listing", "Edition"};
      add(container = new TabbedContainer(tpCaptions));
      container.setBorderStyle(Window.NO_BORDER);
      container.setRect(getClientRect());
      edit = new Edition();
      edit.book = this;
      container.setContainer(1, edit);

      String[] gridCaptions = {"id", "Name", "Address", "Phone", "Birthday", "Salary", "Married", "Gender", "Last Updated"};
      int gridWidths[] =
      {
         0, // id
         fm.stringWidth("aaaaaaaaaa"), // name
         fm.stringWidth("aaaaaaaaaaaaaaa"), // address
         fm.stringWidth(edit.edPhone.getMask()), // phone
         fm.stringWidth(edit.edBirth.getMask()), // birthday
         fm.stringWidth(edit.edSalary.getMask()), // salary
         1, 1, // The title width will be replaced by the value because it is bigger.
         fm.stringWidth("99/99/9999 99:99:99")
      };
          
      int gridAligns[] = {LEFT, LEFT, LEFT, LEFT, LEFT, LEFT, LEFT, LEFT, LEFT};
      container.setContainer(0, grid = new Grid(gridCaptions, gridWidths, gridAligns, false));
      
      invalidateRS();
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
            if (event.target == container)
               switch (container.getActiveTab())
               {
                  case 0: // listing
                     invalidateRS(); // Update the grid.
                     break;
                  case 1: // edition
                     String[] item = grid.getSelectedItem();
                     if (item == null)
                        edit.clear();
                     else
                     {
                        try
                        {
                           edit.rowId = Convert.toInt(item[0]);
                        }
                        catch (InvalidNumberException exception) {}
                        edit.show();
                     }
               }
      }
   }

   /**
    * Gets some results if there is an opened result set.
    * 
    * @param startIndex The first record shown.
    * @param count The number of records to be shown.
    * @return A string matrix with all the desired results.
    */
   public String[][] getItems(int startIndex, int count)
   {
      if (activeRS != null)
      {
         activeRS.absolute(startIndex);
         
         String[][] matrix = activeRS.getStrings(count);
         int size = matrix.length,
             date,
             time;
         long dateTime;
         Date tempDate = edit.tempDate;
         Time tempTime = edit.tempTime;
         StringBuffer sb = edit.buffer;
         
         try
         {
            while (--size >= 0)
            {
               date = Convert.toInt(matrix[size][4]);
               tempDate.set(date % 100, (date /= 100) % 100, date / 100);
               matrix[size][4] = edit.tempDate.toString(); 
               matrix[size][6] = matrix[size][6].equals("1")? "Y" : "N"; 
               matrix[size][7] = matrix[size][7].equals("1")? "M" : "F";  
               dateTime = Convert.toLong(matrix[size][8]);
               time = (int)(dateTime % 1000000);
               date = (int)(dateTime / 1000000);
               tempDate.set(date % 100, (date /= 100) % 100, date / 100);
               tempTime.second = time % 100;
               tempTime.minute = (time /= 100) % 100;
               tempTime.hour = time / 100;
               sb.setLength(0);
               matrix[size][8] = sb.append(tempDate).append(' ').append(tempTime).toString();
            }
         }
         catch (InvalidNumberException exception) {}
         catch (InvalidDateException exception) {}
         return matrix;
      }
      return null;
   }

   /**
    * Invalidates the result set to get a new one.
    */
   void invalidateRS()
   {
      if (activeRS != null)
         activeRS.close();

      if (psList != null)
      {
         (activeRS = psList.executeQuery()).setDecimalPlaces(6, 2);
         if (activeRS.first()) // Maybe the result set is empty (nothing in table).
            grid.setDataSource(this, activeRS.getRowCount());
         else
         {
            grid.removeAllElements();
            activeRS.close();
            activeRS = null;
         }
      }
   }

   /**
    * Creates the table.
    */
   private void createTables()
   {
      try
      {
         // Uses the rowid as the person id.
         if (!driver.exists("bookentry"))
         {
            driver.execute("create table bookentry(name char(30), address char(50), phone char(20), birthday int, salary float, married short, " 
                                                                                 + "gender short, lastUpdated long, photoidx short)");
            driver.execute("CREATE INDEX IDX_0 ON bookentry(rowid)"); // The index names are completely ignored. 
         }
         
         // Photo table for the data base.
         if (!driver.exists("photodb"))
            driver.execute("create table photodb(photo blob(16384))");
         
         PreparedStatement ps = driver.prepareStatement("insert into photodb values (?)");
         ps.setBlob(0, Vm.getFile("fabio.jpg"));
         ps.executeUpdate();
         ps.setBlob(0, Vm.getFile("guilherme.jpg"));
         ps.executeUpdate();
         ps.setBlob(0, Vm.getFile("juliana.jpg"));
         ps.executeUpdate();        
      }
      catch (AlreadyCreatedException exception) {} // ignored

      psList = driver.prepareStatement("select rowid, name, address, phone, birthday, salary, married, gender, lastUpdated, photoidx from " 
                                                                                                                                  + "bookentry");   
   }  
}
