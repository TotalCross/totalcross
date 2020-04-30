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

package samples.apps.salesplus.ui.report.summary;

import samples.apps.salesplus.db.*;
import samples.apps.salesplus.*;
import totalcross.ui.*;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.sys.*;
import totalcross.util.*;

/**
 * The report by summary and day.
 */
public class Day extends Container
{
   /**
    * The Ok button.
    */
   private Button btnOk;
   
   /**
    * The back button.
    */
   private Button btnBack;
   
   /**
    * The date edit.
    */
   private Edit edDate; 
   
   /**
    * The total edit.
    */
   private Edit edTotal;
   
   /**
    * The result grid.
    */
   private Grid grid;

   /**
    * Initializes the user interface.
    */
   public void initUI()
   {            
      // Date label and edit.
      add(new Label("Date: "), LEFT + 5, TOP + 5);
      add(edDate = new Edit("XX/XX/XXXX"), AFTER, SAME);
      edDate.setMode(Edit.DATE, true);
      edDate.setText(SalesPlus.dateAux.toString());
      
      // Buttons.
      add(btnOk = new Button("Ok"), AFTER + 5, SAME);
      add(btnBack = new Button("Back"), RIGHT - 2, BOTTOM - 1);
      
      // The grid.
      add(grid = new Grid(new String[]{"ID", "Customer", "Amount"}, 
                          new int[]{fm.stringWidth("x"), fm.stringWidth("xxxxxxxxxxxxxxxx"), fm.stringWidth("x")}, 
                          new int[]{LEFT, LEFT, RIGHT}, false), LEFT + 5, AFTER + 5, FILL - 5, FIT, btnOk);
      grid.verticalLineStyle = Grid.VERT_DOT;
      grid.setBackColor(Color.WHITE);

      // Total label and edit.
      add(new Label("Total: "), LEFT + 5, AFTER);
      add(edTotal = new Edit("xxxxxxx"), AFTER + 2, SAME);
      edTotal.setEditable(false);
      edTotal.alignment = RIGHT;
   }

   /**
    * Called to process the posted events.
    *
    * @param event The posted event.
    */
   public void onEvent(Event event)
   {
      switch (event.type)
      {
         case ControlEvent.PRESSED:
            if (event.target == btnOk)
            {
               Date date = SalesPlus.dateAux;
               try
               {
                  date.set(edDate.getText(), Settings.dateFormat);
               }
               catch (InvalidDateException exception)
               {
                  new MessageBox("Error", exception.getMessage()).popup();
                  return;
               }
               
               int total = 0;
               Order[] orders = OrderDB.getFromDate(date.getDateInt());
               int count = orders.length;
               
               grid.removeAllElements();
               if (count > 0)
               {
                  String[][] strings = new String[count][3];
                  Order order;
                  int i = -1;
                  
                  while (++i < count)
                  {
                     total = total + (order = orders[i]).totalAmount;
                     strings[i][0] = Convert.toString(order.orderId);
                     strings[i][1] = order.customer;
                     strings[i][2] = Convert.toString((double)order.totalAmount * 0.01, 2);
                  }
                  
                  grid.setItems(strings);
                  edTotal.setText(Convert.toString((double)total * 0.01, 2));
               }
            }
            else if (event.target == btnBack)
               MainWindow.getMainWindow().swap(SalesPlus.screens[SalesPlus.SUMMARY_MENU]);

         }
   }
}
