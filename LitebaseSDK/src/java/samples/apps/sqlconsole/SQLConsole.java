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

package samples.apps.sqlconsole;

import litebase.*;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;
import totalcross.util.*;

/**
 * A SQL console application for Litebase.
 */
public class SQLConsole extends MainWindow
{   
   /** 
    * The possible operands. 
    */
   private static final String[] OPERS = Settings.screenWidth == 160? 
                      new String[] {"!=", "=", "<", ">", "(", ")", ",", "*", "_", ".", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"} 
                    : new String[] {"!=", "=", "<", ">", "<=", ">=", "(", ")", ",", "*", "_", ".", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
   
   /**
    * One of the color used.
    */
   static final int COLOR_1 = 0xA0C0FF;
   
   /**
    * Another color.
    */
   static final int COLOR_2 = 0xBEDEFF;
   
   /**
    * The menu bar.
    */
   private MenuBar menuBar;
   
   /**
    * The multi edit.
    */
   private MultiEdit multiEdit;
   
   /**
    * Execute button.
    */
   private Button btnExe;
   
   /**
    * Clear button.
    */
   private Button btnClr;
   
   /**
    * The result grid.
    */
   private Grid grid;
   
   /**
    * The status label.
    */
   private Label lStatus;
   
   /**
    * The tabbed container to alternate from the query container to the results container.
    */
   private TabbedContainer tabCont;
   
   /**
    * The combo box of old sql queries.
    */
   private ComboBox cbsql;
   
   /**
    * The sql command key words.
    */
   private PushButtonGroup pbgcmd;
   
   /**
    * SQL operators.
    */
   private PushButtonGroup pbgoper;
   
   /**
    * The possible letters present in a query.
    */
   private PushButtonGroup pbgletters;
   
   /**
    * The key event.
    */
   private KeyEvent keyEvent = new KeyEvent();
   
   /**
    * A button to add a query to the combo box.
    */
   private Button btAdd;
   
   /**
    * A button to remove a query to the combo box.
    */
   private Button btRem;
   
   private Button btCopyResults;
   
   private MenuItem miAscii; // guich@251_2: SQLConsole can now be used with ascii tables.
   private MenuItem miCrypto;
   
   /**
    * The id of the database used in the current session.
    */
   private String databaseId;
   
   /**
    * The connection with Litebase.
    */
   private LitebaseConnection conn;
   
   /**
    * The time a query takes.
    */
   private int time;

   static
   {
      Settings.useNewFont = true;
   }
   
   /**
    * The constructor.
    */
   public SQLConsole()
   {
      if (Settings.screenWidth > 400)
         setDefaultFont(Font.getFont(false, 14));
      setUIStyle(Settings.Vista);
      Grid.useHorizontalScrollBar = true;
      
   }

   /**
    * Initializes the user interface.
    */
   public void initUI()
   {
      try
      {
         Vm.setAutoOff(false); // The device won't turn off the screen.
         
         // The menu.
         MenuItem[] items = 
         {
            new MenuItem("File"),
            new MenuItem("Change app id"),
            new MenuItem("Use default app id"),
            new MenuItem(),
            miAscii = new MenuItem("Is ascii", false), // guich@251_2: SQLConsole can now be used with ascii tables.
            miCrypto = new MenuItem("Is Crypto", false),
            new MenuItem(),
            new MenuItem("Exit")
         };
         setMenuBar(menuBar = new MenuBar(new MenuItem[][]{items}));

         // guich@251_2: SQLConsole can now be used with ascii tables.
         // retrieve data from applicationid
         String s = Settings.appSecretKey;
         if (s != null)
         {
            if (s.indexOf('|') == -1) // legacy
               databaseId = s;
            else
            {
               databaseId = s.substring(0,4);
               miAscii.isChecked = s.charAt(5) == '1';
               miCrypto.isChecked = s.length() == 7 && s.charAt(6) == '1';
            }
         }
         else databaseId = Settings.applicationId;
         connChanged();
         
         Container bottomBar = new Container();
         add(bottomBar, LEFT, BOTTOM, FILL, (int) (fmH * 1.25));
         bottomBar.add(btCopyResults = new Button("Copy to transfer area"), RIGHT, BOTTOM);
         
         Button.commonGap = 1; // The button gaps will be the same.
         int blue = Color.getRGB(173, 214, 255); // Gets the color.
         bottomBar.add(lStatus = new Label(), LEFT, BOTTOM, FIT, PREFERRED); // Status label.
         
         // The tabs with SQL queries and queries result.
         add(tabCont = new TabbedContainer(new String[]{"SQL", "Results"}));
         tabCont.setRect(LEFT, TOP, FILL, FIT, bottomBar);
         tabCont.setBackColor(blue);
         
         // SQL combo box.
         Container container = tabCont.getContainer(0);
         container.setBackColor(blue);
         cbsql = new ComboBox();
         container.add(btAdd = new Button("+"), RIGHT, TOP, PREFERRED, cbsql.getPreferredHeight());
         container.add(btRem = new Button("-"), BEFORE - 2, TOP, SAME, SAME);
         int color = Color.getRGB(50, 203, 255);
         btAdd.setBackColor(color);
         btRem.setBackColor(color);
         container.add(cbsql);
         cbsql.setBackColor(Color.darker(color));
         cbsql.setRect(LEFT, TOP, FIT - 2, PREFERRED);
         cbsql.enableHorizontalScroll();
         cbsql.fullWidth = true;

         // The buttons that execute and clears the queries.
         container.add(btnExe = new Button(new Image("go.gif").smoothScaledFromResolution(320)));
         btnExe.setBorder(Button.BORDER_NONE);
         btnExe.setRect(RIGHT, AFTER + 1, PREFERRED, PREFERRED, btRem);
         container.add(btnClr = new Button(new Image("clear.gif").smoothScaledFromResolution(320)));
         btnClr.setBorder(Button.BORDER_NONE);
         btnClr.setRect(RIGHT, AFTER, PREFERRED, PREFERRED);

         // The multi edit.
         boolean tall = Settings.screenHeight > Settings.screenWidth;
         container.add(multiEdit = new MultiEdit(tall? 7 : 3,3));
         multiEdit.setBackColor(COLOR_1);
         multiEdit.setRect(LEFT, AFTER + 1, FIT - 1, PREFERRED, cbsql);

         // The buttons with commands, operators and letters.
         container.add(pbgcmd = new PushButtonGroup(new String[] 
         {"select", "from", "where", "create", "like", "table", "and", "or", "insert into", "values", "update", "delete", "drop", "index", "count"}, 
                                                                             false, -1, 0, 8, 3, false, PushButtonGroup.BUTTON),LEFT + 1,AFTER + 1);
         pbgcmd.setFocusLess(true);
         container.add(pbgoper = new PushButtonGroup(OPERS, 0, 2),LEFT + 1,AFTER + 1);
         pbgoper.setFocusLess(true);
         if (tall)
         {
            container.add(pbgletters = new PushButtonGroup(new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", null, "�", 
                                           "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", null, " "}, 0, 2),LEFT + 1,AFTER + 1);
            pbgletters.setFocusLess(true);
         }
         pbgoper.setBackColor(color);

         if (Settings.appSettings != null) // Gets the last query and displays it.
            multiEdit.setText(Settings.appSettings);
         if (Settings.appSettingsBin != null) // Populates the combobox query.
         {
            DataStream ds = new DataStream(new ByteArrayStream(Settings.appSettingsBin));
            cbsql.add(ds.readStringArray());
         }
         cbsql.setSelectedIndex(0);
         addGrid(new String[]{"No queries executed."}); // No queries were executed yet.
         lStatus.setText("Current database id: " + databaseId); // The current connection id.
      }
      catch (IOException exception)
      {
         MessageBox.showException(exception, false);
      }
      catch (ImageException exception)
      {
         MessageBox.showException(exception, false);
      }
   }

   /**
    * Called just before an application exits. When this is called, all threads are already killed.
    */
   public void onExit()
   {
      Settings.appSettings = multiEdit.getText(); // Saves the last query.
      int n = cbsql.size();
      
      if (n > 0) // Saves the queries in the combo box.
      {
         String[] strings = new String[n];
         int i = -1;
         while (++i < n)
            strings[i] = (String)cbsql.getItemAt(i);
         ByteArrayStream bas = new ByteArrayStream(2000);
         DataStream ds = new DataStream(bas);
         try
         {
            ds.writeStringArray(strings);
         } catch (IOException exception)
         {
            MessageBox.showException(exception, false);
         }
         Settings.appSettingsBin = bas.toByteArray();
      }
      else
         Settings.appSettingsBin = null;
   }

   /**
    * Adds the grid to the results tab.
    * 
    * @param caps The grid title.
    */
   private void addGrid(String[] caps)
   {
      Container c = tabCont.getContainer(1);
      if (grid != null)
      {
         c.remove(grid);
         grid = null;
      }
      c.add(grid = new Grid(caps,false));
      c.setBackColor(Color.WHITE);
      grid.setBackColor(COLOR_1);
      grid.captionsBackColor = COLOR_1;
      grid.secondStripeColor = COLOR_2;
      grid.setRect(LEFT, TOP, FILL, FILL);
   }

   /**
    * Updates the status.
    * 
    * @param string The new status string.
    */
   private void status(String string)
   {
      lStatus.setForeColor(Color.BLACK);
      lStatus.setText(string);
      lStatus.repaintNow();
   }

   /**
    * process a sql command.
    * 
    * @param sql The sql command.
    */
   private void processSQL(String sql) 
   {
      String command = sql.substring(0, sql.indexOf(' ')).toLowerCase();
      
      if (command.equals("select")) // Select.
      {
         tabCont.setActiveTab(1);
         
         ResultSet rs = null;
         try
         {
            rs = conn.executeQuery(sql);
         }
         catch (TableNotClosedException e)
         {
            // attempt to get the name of the tables used on the query to use the recover table
            String sqlLower = sql.toLowerCase();
            int idxStartFrom = sqlLower.indexOf(" from ") + 6;
            int idxEndFrom = sqlLower.indexOf(" where ");
            if (idxEndFrom == -1)
               idxEndFrom = sqlLower.indexOf(" group by ");
            if (idxEndFrom == -1)
               idxEndFrom = sqlLower.indexOf(" order by ");
            String fromClause = idxEndFrom != -1 ? sql.substring(idxStartFrom, idxEndFrom) : sql.substring(idxStartFrom);
            String[] tableNames = Convert.tokenizeString(fromClause, ',');
            for (int i = tableNames.length - 1 ; i >= 0 ; i--)
               conn.recoverTable(tableNames[i].trim());
            
            rs = conn.executeQuery(sql);
         }
         time = Vm.getTimeStamp() - time;
         int rows = rs.getRowCount();

         if (rows <= 0) // Empty answer.
         {
            status("No records found.");
            if (grid != null)
               grid.removeAllElements();
         }
         else
         {
            status(rows + " records found.");

            // Uses the result set meta data to get the field labels and display settings.
            ResultSetMetaData rsmd = rs.getResultSetMetaData();
            int n = rsmd.getColumnCount(),
                cw = fm.charWidth('0'),
                i = 0;
            String[] titles = new String[n];
            int[] w = new int[n],
                  a = new int[n];
            
            while (++i <= n)
            {
               if (rsmd.getColumnType(i) == ResultSetMetaData.DOUBLE_TYPE)
               {
                  a[i - 1] = RIGHT;
                  rs.setDecimalPlaces(i, 2);
               }
               else a[i - 1] = LEFT;
               w[i - 1] = Math.min(80,rsmd.getColumnDisplaySize(i)) * cw / 2;
               titles[i - 1] = rsmd.getColumnLabel(i);
            }
            Container container = tabCont.getContainer(1);
            if (grid != null)
            {
               container.remove(grid);
               grid = null;
            }
            container.add(grid = new Grid(titles, w, a, false));
            grid.setBackColor(COLOR_1);
            grid.captionsBackColor = COLOR_1;
            grid.secondStripeColor = COLOR_2;
            grid.setRect(LEFT, TOP, FILL, FILL);
            
            // Shows the query results.
            grid.setItems(getStrings(rs));
         }
      }
      
      // Commands that update the database.
      else if (command.equals("insert") || command.equals("update") || command.equals("delete") || command.equals("drop") || command.equals("alter"))
      {
         int res = conn.executeUpdate(sql);
         time = Vm.getTimeStamp() - time;

         if (res >= 0)
            status(res + " records affected.");
      }
      else if (command.equals("purge"))
      {
         int res = conn.purge(sql.substring(6).trim());
         if (res >= 0)
            status(res + " records affected.");
      }
      else // Creates a table or an index.
      {
         conn.execute(sql);
         time = Vm.getTimeStamp() - time;
      }
   }

   /**
    * Executes the queries in the multi edit.
    */
   private void execute()
   {
      String[] sqls = Convert.tokenizeString(multiEdit.getText().trim(), '|');

      grid.removeAllElements();

      try
      {
         time = Vm.getTimeStamp();
         int i = -1,
             n = sqls.length;
         while (++i < n)
         {
            status("Executing SQL #" + (i + 1));
            processSQL(sqls[i]);
         }
         status(lStatus.getText()+ " (" + time + "ms)");
      }
      catch (RuntimeException exception)
      {
         MessageBox.showException(exception, true);
      }
   }

   /**
    * Called to process posted events.
    * 
    * @param event The posted event.
    */
   public void onEvent(Event event)
   {
      if (event.type == ControlEvent.PRESSED)
      {
         if (event.target == btCopyResults)
         {
            StringBuffer sb = new StringBuffer(2048);
            Vector vItems = grid.getItemsVector();
            int rows = vItems.size();
            int columns = grid.captions.length;
            for (int i = 0 ; i < columns ; i++)
               sb.append(grid.captions[i]).append('\t');
            for (int i = 0 ; i < rows ; i++)
            {
               sb.append('\n');
               String[] row = (String[]) vItems.items[i];
               for (int j = 0 ; j < columns ; j++)
                  sb.append(row[j]).append('\t');
            }
            Vm.clipboardCopy(sb.toString());
         }
         else if (event.target == menuBar) // Selects an item in the menu bar.
         {
            switch (menuBar.getSelectedIndex()) 
            {
               case 1: // Changes the application id.
               {
                  InputBox id = new InputBox("Database ID", "Please enter the new id", databaseId, new String[]{"Ok"});
                  id.popup();
                  String answer = id.getValue();
                  if (!answer.equals(databaseId))
                  {
                     if (answer.length() != 4) // juliana@227_5: The new application id of SQLConsole must be 4 characters long.
                        new MessageBox("Error", "The application id must be 4 characters long.").popup();
                     else
                     {
                        // guich@251_2: SQLConsole can now be used with ascii tables.
                        databaseId = answer;
                        connChanged();
                        lStatus.setText("App id changed to " + answer);
                     }
                  }
                  break;
               }
               case 2: // Changes the application id to the default one.
               {
                  databaseId = Settings.applicationId;
                  connChanged();
                  lStatus.setText("App id changed to " + Settings.applicationId);
                  break;
               }
               case 4: // set/unset isascii
               case 5: // set/unset isascii
                  connChanged(); // guich@251_2: SQLConsole can now be used with ascii tables.
                  break;
               case 7: // Exits the application.
                  exit(0);
                  break;
            }
         }
         if (event.target == cbsql && cbsql.getSelectedIndex() >= 0) // Gets the selected sql command from the combo box.
         {
            multiEdit.setText(cbsql.getSelectedItem().toString());
            repaint();
            multiEdit.requestFocus();
         }
         else if (event.target == btnExe && multiEdit.getText().trim().length() > 0) // Executes the queries.
            execute();
         else if (event.target == btAdd) // Adds the queries from the multi edit to the combo box.
         {
            cbsql.add(multiEdit.getText());
            cbsql.selectLast();
         }
         else if (event.target == btRem && cbsql.getSelectedIndex() >= 0) // Removes a block of queries from the combo box.
            cbsql.remove(cbsql.getSelectedIndex());
         else if (event.target == btnClr) // Clears the multi edit.
         {
            multiEdit.setText("");
            Control.repaint();
            grid.removeAllElements();
            multiEdit.requestFocus();
         }
         else if (event.target == tabCont) // Changes the tab or sets the focus to it.
         {
            if (tabCont.getActiveTab() == 0)
               multiEdit.requestFocus();
            else
               grid.requestFocus();
         }
         
         // Gets the event from the command, operators o letter buttons.
         else if (event.target instanceof PushButtonGroup && getFocus() instanceof MultiEdit)
         {
            PushButtonGroup pbgroup = (PushButtonGroup)event.target;
            String string = pbgroup.getSelectedItem();
            if (string != null)
            {
               Control c = getFocus();
               keyEvent.target = c;
               if (string.equals("�"))
               {
                  keyEvent.key = SpecialKeys.BACKSPACE;
                  c.onEvent(keyEvent);
               }
               else
               {
                  if (pbgroup != pbgletters && !(pbgroup == pbgoper && pbgroup.getSelectedIndex() >= OPERS.length - 12) 
                 && !pbgroup.getSelectedItem().endsWith("_") && !pbgroup.getSelectedItem().endsWith(">") && !pbgroup.getSelectedItem().endsWith("<"))
                     string += " ";
                  
                  int n = string.length(),
                      i = -1;
                  while (++i < n)
                  {
                     keyEvent.key = string.charAt(i);
                     c.onEvent(keyEvent);
                  }
               }
            }
         }
      }
   }
   
   // guich@251_2: SQLConsole can now be used with ascii tables.
   private void connChanged()
   {
      Settings.appSecretKey = databaseId + "|" + (miAscii.isChecked?"1":"0") + (miCrypto.isChecked?"1":"0");
      if (conn != null) conn.closeAll();
      String pars = null;
      if (miAscii.isChecked)
         pars = "chars_type=chars_ascii";
      if (miCrypto.isChecked)
         pars = pars == null ? "crypto" : pars+";crypto";
      conn = LitebaseConnection.getInstance(databaseId, pars);
   }

   /**
    * Replacement for ResultSet.getStrings used to avoid using null values on the grid.
    * 
    * @param rs
    * @return
    */
   private String[][] getStrings(ResultSet rs)
   {
      int rowCount = rs.getRowCount();
      int colCount = rs.getResultSetMetaData().getColumnCount();
      
      String[][] result = new String[rowCount][colCount];
      for (int i = 0 ; rs.next() ; i++)
         for (int j = 0 ; j < colCount ; j++)
            result[i][j] = rs.isNull(j+1) ? "�" : rs.getString(j + 1);
      return result;
   }   
}
