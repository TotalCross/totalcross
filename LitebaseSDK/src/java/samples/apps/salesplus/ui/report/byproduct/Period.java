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

package samples.apps.salesplus.ui.report.byproduct;

import samples.apps.salesplus.db.*;
import samples.apps.salesplus.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.dialog.*;
import totalcross.ui.gfx.*;
import totalcross.sys.*;
import totalcross.util.*;

/**
 * The report by product and period.
 */
public class Period extends Container
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
    * The first date edit.
    */
   private Edit edDateFrom;
   
   /**
    * The last date edit.
    */
   private Edit edDateTo;
   
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
      // Set today date.
      Date date = SalesPlus.dateAux;
      date.setToday();
      
      // Initial date label and edit.
      add(new Label("From "), LEFT + 5, TOP + 5);
      add(edDateFrom = new Edit("XX/XX/XXXX"), AFTER, SAME);
      edDateFrom.setMode(Edit.DATE, true);
      edDateFrom.setText(date.toString());      
      
      // Final date label and edit.
      add(new Label("To "), LEFT + 5, AFTER + 2);
      add(edDateTo = new Edit("XX/XX/XXXX"), edDateFrom.getRect().x, SAME);
      edDateTo.setMode(Edit.DATE, true);
      edDateTo.setText(date.toString());      
      
      // Buttons.
      add(btnOk = new Button("Ok"), AFTER + 5, SAME);
      add(btnBack = new Button("Back"), RIGHT - 2, BOTTOM - 1);
      
      // The grid.
      add(grid = new Grid(new String[]{"Product", "Qty", "Amount"}, 
                          new int[]{fm.stringWidth("xxxxxxxxx"), fm.stringWidth("xxxx"), fm.stringWidth("xxxxxxxx")}, 
                          new int[]{LEFT, CENTER, RIGHT}, false), LEFT + 5, AFTER + 5, FILL - 5, FIT, btnOk);
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
            if (event.target == btnOk) // Ok button: does the search.
            {
               int date1,
                   date2;
               Date date = SalesPlus.dateAux;
               
               try
               {
                  date.set(edDateTo.getText(), Settings.dateFormat);
                  date2 = date.getDateInt();
                  date.set(edDateFrom.getText(), Settings.dateFormat);
                  date1 = date.getDateInt();
               }
               catch (InvalidDateException exception)
               {
                  new MessageBox("Error", exception.getMessage()).popup();
                  return;
               }
               
               if (date1 > date2)
               {
                  new MessageBox("Message", "ATTENTION | The date 'to' can | be not lower than | the date 'from'").popup();
                  return;
               }
               
               Vector vector = new Vector(1);
               Object[] orders;

               while (date1 <= date2)
               {
                  orders = OrderDB.getFromDate(date1);
                  if (orders.length != 0)
                     vector.addElements(orders);
                  date.advance(1);
                  date1 = date.getDateInt();
               }
               
               orders = vector.items;
               int count1 = vector.size();
               
               grid.removeAllElements();
               if (count1 > 0)
               {
                  String[][][] strings = new String[count1][][];
                  ItemOrder[] items;
                  ItemOrder item;
                  int i1 = -1,
                      total = 0,
                      i2,
                      count2;
                 
                  while (++i1 < count1)
                  {
                     strings[i1] = new String[count2 = (items = ItemOrderDB.readAll(((Order)orders[i1]).orderId)).length][3];
                     i2 = -1;
                     
                     while (++i2 < count2)
                     {
                        strings[i1][i2][0] = ProductDB.getFromCode((item = items[i2]).productId).name;
                        strings[i1][i2][1] = Convert.toString(item.quant);
                        strings[i1][i2][2] = Convert.toString((double)item.totalAmount * 0.01, 2);
                        total = total + item.totalAmount;
                     }
                     grid.add(strings[i1]);
                  }
                  
                  edTotal.setText(Convert.toString((double)total * 0.01, 2));
               }
            }
            else if (event.target == btnBack)
               MainWindow.getMainWindow().swap(SalesPlus.screens[SalesPlus.BY_PRODUCT_MENU]);
            break;
         
         case ControlEvent.FOCUS_OUT: // Corrects the data inserted to the date format.
            try
            {
               if (event.target == edDateFrom && edDateFrom.getLength() > 0)
               {
                  SalesPlus.dateAux.set(edDateFrom.getText(), Settings.dateFormat);
                  edDateFrom.setText(SalesPlus.dateAux.toString());
               }
               else if (event.target == edDateTo && edDateTo.getLength() > 0)
               {
                  SalesPlus.dateAux.set(edDateTo.getText(), Settings.dateFormat);
                  edDateTo.setText(SalesPlus.dateAux.toString());
               }
            }
            catch (InvalidDateException exception)
            {
               new MessageBox("Error", exception.getMessage()).popup();
            }
         }
   }
}
