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

package samples.apps.salesplus.ui.customer;

import litebase.PrimaryKeyViolationException;
import samples.apps.salesplus.*;
import samples.apps.salesplus.db.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.dialog.*;
import totalcross.ui.gfx.*;

/**
 * The new customer menu.
 */
public class NewCustomer extends Container
{
   /**
    * An auxiliary customer.
    */
   static Customer sentCustomer;

   /**
    * A tab for general customer data.
    */
   private NewGeneral newGeneral;
   
   /**
    * A tab for contact customer data.
    */
   private NewContact newContact;
   
   /**
    * A tabbed container.
    */
   private TabbedContainer tabCont;
   
   /**
    * The back button.
    */
   private Button btnBack; 
   
   /**
    * The clear button.
    */
   private Button btnClear;
   
   /**
    * The insert or update button.
    */
   private Button btnInsert;
   
   /**
    * Initializes the user interface.
    */
   public void initUI()
   {      
      Button.commonGap = 1;

      // The tabbed container.
      add(tabCont = new TabbedContainer(new String[]{"General", "Contact"}), LEFT, TOP, FILL, FILL);
      tabCont.setBorderStyle(Window.NO_BORDER); 
      tabCont.setBackColor(Color.BRIGHT);
      tabCont.setContainer(0, newGeneral = new NewGeneral());
      tabCont.setContainer(1, newContact = new NewContact());
      
      // The buttons.
      add(btnInsert = new Button("Insert "), RIGHT - 2, BOTTOM - 2);
      add(btnBack = new Button("Back"), BEFORE - 2, SAME);
      add(btnClear = new Button("Clear"), BEFORE - 2, SAME);
      
      onAddAgain();
   }

   /**
    * Empties all edit boxes and set the active panel to the first one.
    */
   public void cleanEdit()
   {
      newGeneral.cleanEdit();
      newContact.cleanEdit();
      tabCont.setActiveTab(0);
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
         if (event.target == btnClear) // Pressed clear button: clears all data.
            cleanEdit();
         else if (event.target == btnBack) // Pressed back button: goes to the previous menu.
         {
            if (sentCustomer != null) // There is a selected customer, goes to the customer search
            {
               sentCustomer = null;
               MainWindow.getMainWindow().swap(SalesPlus.screens[SalesPlus.CUSTOMER_SEARCH]);
            }
            else // No customer were selected, goes to the parent menu.
               MainWindow.getMainWindow().swap(SalesPlus.screens[SalesPlus.CUSTOMER_MENU]);
         }
         else if (event.target == btnInsert) // Pressed insert button: checks data and inserts or updates customer.
         {
            Customer customer = new Customer();
            StringBuffer sBuffer = SalesPlus.sBuffer;
            
            newGeneral.getFromEdit(customer);
            newContact.getFromEdit(customer);
            sBuffer.setLength(0);
            
            if (customer.name.length() == 0)    
               sBuffer.append("Name |");
            if (customer.fedId.length() == 0)   
               sBuffer.append("Fed. ID |");
            if (customer.address.length() == 0) 
               sBuffer.append("Address |");
            if (customer.city.length() == 0)    
               sBuffer.append("City |");
            if (customer.state.length() == 0)   
               sBuffer.append("State |");
            if (customer.country.length() == 0) 
               sBuffer.append("Country |");
            if (customer.zip.length() == 0)     
               sBuffer.append("ZIP |");
            if (customer.tel1.length() == 0)    
               sBuffer.append("tel1 ");

            if (sBuffer.length() > 0)
               new MessageBox("Error", "Please fill in the following fields:|"+sBuffer.toString()).popupNonBlocking();
            else
            {
               try
               {
                  if (CustomerDB.write(customer, sentCustomer != null) == 0) // Inserts or updates customer. 
                  {
                     new MessageBox("Error", "Customer name can't be repeated.").popup();
                     return;
                  }
               } 
               catch (PrimaryKeyViolationException exception)
               {
                  new MessageBox("Error", "Fed. ID already exists").popup();
                  return;
               }
                  
               cleanEdit();
               if (sentCustomer != null) // The user may want to update more customers.
               {
                  sentCustomer = null;
                  new MessageBox("Message", "Customer updated").popup();
                  MainWindow.getMainWindow().swap(SalesPlus.screens[SalesPlus.CUSTOMER_SEARCH]);
               }
               else 
                  new MessageBox("Message", "Customer inserted").popupNonBlocking();
            }
         }
      }
   }

   /**
    * Sets the new customer menu to insert or update a customer. 
    */
   public void onAddAgain()
   {
      if (sentCustomer == null) // Insert a customer: clears the edits.
      {
         cleanEdit();
         btnInsert.setText("Insert");
      }
      else // Update a customer: sets the edits with the selected customer data.
      {
         btnInsert.setText("Update");
         newGeneral.setEdit(sentCustomer);
         newContact.setEdit(sentCustomer);
      }
   }
}