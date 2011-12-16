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

import litebase.DriverException;
import litebase.LitebaseConnection;
import totalcross.io.*;
import totalcross.sys.Settings;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.dialog.*;
import totalcross.util.Vector;

/**
 * The main user interface class.
 */
class MainUI extends Container
{
   /**
    * The combo box for the applications found.
    */
   private ComboBox cbApplications;
   
   /**
    * The button to convert all the tables.
    */
   private Button btnConvertAll;
   
   /**
    * The button to list all the tables.
    */
   private Button btnListDBs;
   
   /**
    * The button to refresh the data path.
    */
   private Button btnDataPath;
   
   /**
    * The check box for selecting the use of the application id.
    */
   private Check chkCreatorId;
   
   /**
    * The edit for entering the creator id.
    */
   private Edit edtCreatorId;
   
   /**
    * The edit for entering the data path.
    */
   private Edit edtDataPath;
   
   /**
    * The label for the application migration.
    */
   private Label lMsn;

   /**
    * Initializes the user interface.
    */
   public void initUI()
   {
      Container container = new Container(), 
                containerAux = new Container();
      
      edtCreatorId = new Edit();
      edtCreatorId.setMaxLength(4);
      
      int x = (btnListDBs = new Button(" List tables ")).getPreferredWidth() + (btnConvertAll = new Button(" Convert all tables ")).getPreferredWidth(),
          y = btnListDBs.getPreferredHeight() + edtCreatorId.getPreferredHeight() + 4;
      
      add(new Label("Choose the application to migrate"), LEFT + 1, AFTER + 1);
      add(new Label("its tables and click on the button below"), LEFT + 1, AFTER + 1);
      
      add(containerAux);
      containerAux.setRect(CENTER, AFTER + 2, x + 5, y + 1);
      containerAux.add(edtDataPath = new Edit());
      edtDataPath.setRect(CENTER, AFTER + 1, x, PREFERRED);
      edtDataPath.setText(Settings.appSettings);
      containerAux.add(new Label("Data path"), LEFT + 4, AFTER + 1);
      containerAux.add(btnDataPath = new Button("Refresh"), CENTER, SAME + 2);
      
      add(lMsn = new Label("  TotalCross Applications  "), CENTER + 2, AFTER + 4);
      
      try
      {
         add(cbApplications = new ComboBox(getApplications()));
      }
      catch (IOException exception)
      {
         new MessageBox("ERROR", exception.getMessage()).popup();
         add(cbApplications = new ComboBox());
      }
      cbApplications.setRect(CENTER , AFTER + 2, x + 6, PREFERRED);
      
      add(new Label("OR"), CENTER, AFTER + 2);
      chkCreatorId = new Check("Use Creator Id:");
      add(container);
      container.add(btnListDBs);
      container.add(btnConvertAll);
      container.add(chkCreatorId);
      container.add(edtCreatorId);

      container.setRect(CENTER, AFTER + 2, x + 5, y + 5);
      chkCreatorId.setRect(LEFT + 5, TOP, PREFERRED, PREFERRED);
      edtCreatorId.setRect(AFTER + 2, SAME, PREFERRED, PREFERRED);
      btnListDBs.setRect(LEFT, AFTER + 2, PREFERRED, PREFERRED + 5);
      btnConvertAll.setRect(AFTER + 2, SAME, PREFERRED, PREFERRED + 5);
      
      try
      {
         fillComboApplications(false);
      }
      catch (IOException exception)
      {
         new MessageBox("ERROR", exception.getMessage()).popup();
      }
      
      edtCreatorId.setEnabled(false);
      
      repaintNow();
   }
   
   /**
    * Fills the combo box which lists the TotalCross applications.
    * 
    * @param updateData Idicates tha the combo box data must be updated.
    * @throws IOException If an internal method throws it.
    */
   private void fillComboApplications(boolean updateData) throws IOException
   {
      if (!chkCreatorId.isChecked()) // Only fills the combo box if application id is not used.
      {
         if (updateData)
         {
            Object [] apps = getApplications();
            cbApplications.removeAll();
            if (apps != null)
               cbApplications.add(apps);
         }
         if (cbApplications.size() > 0)
         {
            cbApplications.setSelectedIndex(0);
            cbApplications.setEnabled(true);
            btnConvertAll.setEnabled(true);
            btnListDBs.setEnabled(true);
            lMsn.setText("  TotalCross Applications  ");
         }
         else
         {
            cbApplications.setEnabled(false);
            btnConvertAll.setEnabled(false);
            btnListDBs.setEnabled(false);
            lMsn.setText("No application to migrate");
         }
      }
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
            if (event.target == btnDataPath) // Refreshes the data path, which must exist.
            {
               String newPath = edtDataPath.getText();
               
               try
               {
                  if (new File(newPath, File.DONT_OPEN, 1).isDir())
                  {
                     Settings.appSettings = newPath;
                     fillComboApplications(true);
                  }
                  else
                  {
                     new MessageBox("ERROR", "This directory | does not exist").popup();
                  }
               }
               catch (IOException exception)
               {
                  new MessageBox("ERROR", exception.getMessage()).popup();
               }
            } 
            else if (event.target == chkCreatorId) // The application id box state has been changed.
            {
               boolean checked = chkCreatorId.isChecked();
               
               edtCreatorId.setEnabled(checked);   
               btnConvertAll.setEnabled(true);
               btnListDBs.setEnabled(true);

               // Only enables the application list if the application id box is unchecked.
               if (cbApplications.size() > 0)
                  cbApplications.setEnabled(!checked);
               else if (!checked)
               {
                  btnConvertAll.setEnabled(false);
                  btnListDBs.setEnabled(false);
               }
            }
            else if (event.target == btnListDBs || event.target == btnConvertAll) // Lists all tables or just converts all of them.
            {
               boolean checked = chkCreatorId.isChecked();
               String crid,
                      appName;
               int indexOf = 0;
               
               if (checked) // Uses the application id.
               {
                  if ((crid = edtCreatorId.getText()).length() != 4) // The application id must have 4 characters.
                  {
                     new MessageBox("Error","The creator Id must | have 4 characters").popup();
                     break;
                  }
               }
               else // Uses the application list.
               {
                  indexOf = (appName = cbApplications.getSelectedItem().toString()).indexOf('-');
                  crid = appName.substring(indexOf + 2, appName.length());
               }
               Migration.mig.listDBs.appCrid = crid;

               // Lists or converts all the tables.
               if (event.target == btnListDBs)
                  Migration.mig.swap(Migration.mig.listDBs);
               else if (event.target == btnConvertAll) 
                  try
                  {
                     convertAllTables(crid);
                  }
                  catch (IOException exception)
                  {
                     new MessageBox("ERROR", exception.getMessage()).popup();
                  }
            }
            break;
         }
      }
   }

   /**
    * Lists TotalCross applications in a given data path.
    * 
    * @return An array of applications.
    * @throws IOException If an internal method throws it.
    */
   public Object[] getApplications() throws IOException
   {
      String[] files = null; 
      Vector vProgs = new Vector();
      String name,
             crid;
      
      if (!new File(Settings.appSettings, File.DONT_OPEN, 1).isDir())
         new MessageBox("ERROR", "This directory | does not exist").popup();
      else if (!(Settings.platform.equals(Settings.PALMOS) || Settings.platform.equals(Settings.BLACKBERRY)))
         files = new File(Settings.appSettings, File.DONT_OPEN, 1).listFiles();   
      
      if (files != null)
      {
         int i = files.length;
         while (--i >= 0)
            if ((name = files[i]).endsWith(".tcz") || (name = files[i]).endsWith(".jar"))
            {
               name = files[i].substring(0, name.length() - 4);
               Migration.mig.listDBs.appCrid = crid = getCreator(name);
               if (ListDBs.getTables(crid).size() > 0)
                  vProgs.addElement(name + " - " + crid);
            }
         vProgs.qsort();
      }
      return vProgs.toObjectArray();
   }

   /**
    * Converts all tables using a given application id.
    * 
    * @param crid The application id.
    * @throws IOException If an internal method throws it.
    */
   public void convertAllTables(String crid) throws IOException
   {
      Vector tables = ListDBs.getTables(crid);
      LitebaseConnection driver = LitebaseConnection.getInstance(crid, Settings.appSettings);
      int i = tables.size();
      boolean hasTable = (i > 0),
              error = false;
      
      MessageBox msb = new MessageBox("ATTENTION", "This operation can | take a while...");
      msb.popupNonBlocking();

      while (--i >= 0) // Tries to convert all the tables.
         try
         {
            driver.convert(tables.items[i].toString());
         }
         catch (DriverException exception)
         {
            new MessageBox("ERROR", exception.getMessage()).popup();
            error = true;
            break;
         }
      
      msb.unpop();
      if (error == false)
         if (hasTable)
            new MessageBox("SUCCESSFUL","The tables | were migrated! ").popupNonBlocking();
         else
            new MessageBox("ATTENTION","There's no tables | for this application! ").popupNonBlocking();
   }
   
   /**
    * Gets the application if from an application name.
    * 
    * @param name The application name.
    * @return The application id.
    */
   public static String getCreator(String name)
   {
      int i,
          n = name.length(),
          hash = 0;
      byte[] creator = new byte[4];
       
      for (i = 0; i < n; i++)
         hash += name.charAt(i);
      for (i = 0; i < 4; i++)
      {
         creator[i] = (byte)((hash % 26) + 'a');
         if ((hash & 64) > 0)
            creator[i] += ('A'-'a');
         hash = hash / 2;
      }
      return new String(creator);
   }
}
