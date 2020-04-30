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

import samples.apps.salesplus.db.*;
import totalcross.ui.*;
import totalcross.ui.gfx.*;

/**
 * The general information tab.
 */
public class NewGeneral extends Container
{
   /**
    * The customer name edit.
    */
   private Edit edName;

   /**
    * The customer Fed. ID edit.
    */
   private Edit edFedId;
   
   /**
    * The customer address edit.
    */
   private Edit edAddress;
   
   /**
    * The customer city edit.
    */
   private Edit edCity;
   
   /**
    * The customer state edit.
    */
   private Edit edState;
   
   /**
    * The customer country edit.
    */
   private Edit edCountry;
   
   /**
    * The customer zip code edit.
    */
   private Edit edZip;
   
   /**
    * The customer first phone number edit.
    */
   private Edit edTel1;
   
   /**
    * The customer second phone number edit.
    */
   private Edit edTel2;
   
   /**
    * The customer fax edit.
    */
   private Edit edFax;
   
   /**
    * Initializes the user interface.
    */
   public void initUI()
   {
      setBackColor(Color.WHITE);
      
      // Name label and edit.
      add(new Label("Name: "), LEFT + 5, TOP + 5);
      add(edName = new Edit(""), AFTER + 20, SAME, PREFERRED - 5, PREFERRED);

      int initX = edName.getRect().x;
      
      // Fed. ID label and edit.
      add(new Label("Fed. ID: "), LEFT + 5, AFTER + 2);
      add(edFedId = new Edit(""), initX, SAME, PREFERRED - 5, PREFERRED);

      // Address label and edit.
      add(new Label("Address: "), LEFT + 5, AFTER + 2);
      add(edAddress = new Edit(""), initX, SAME, PREFERRED - 5, PREFERRED);
      
      // City label and edit.
      add(new Label("City: "), LEFT + 5, AFTER + 2);
      add(edCity = new Edit(""), initX, SAME, PREFERRED - 5, PREFERRED);
      
      // State label and edit.
      add(new Label("State: "), LEFT + 5, AFTER + 2);
      add(edState = new Edit(""), initX, SAME, PREFERRED - 5, PREFERRED);

      // Country label and edit.
      add(new Label("Country: "), LEFT + 5, AFTER + 2);
      add(edCountry = new Edit(""), initX, SAME, PREFERRED - 5, PREFERRED);

      // Zip code label and edit.
      add(new Label("ZIP: "), LEFT + 5, AFTER + 2);
      add(edZip = new Edit(""), initX, SAME, PREFERRED - 5, PREFERRED);

      // Phone number 1 label and edit.
      add(new Label("Tel. 1: "), LEFT + 5, AFTER + 2);
      add(edTel1 = new Edit(""), initX, SAME, PREFERRED - 5, PREFERRED);

      // Phone number 2 label and edit.
      add(new Label("Tel. 2: "), LEFT + 5, AFTER + 2);
      add(edTel2 = new Edit(""), initX, SAME, PREFERRED - 5, PREFERRED);

      // Fax label and edit.
      add(new Label("Fax: "), LEFT + 5, AFTER + 2);
      add(edFax = new Edit(""), initX, SAME, PREFERRED - 5, PREFERRED);
    }

   /**
    * Sets the customer general data in the edits.
    * 
    * @param customer The customer data.
    */
   public void setEdit(Customer customer)
   {
      edName.setText(customer.name);
      edFedId.setText(customer.fedId);
      edAddress.setText(customer.address);
      edCity.setText(customer.city);
      edState.setText(customer.state);
      edCountry.setText(customer.country);
      edZip.setText(customer.zip);
      edTel1.setText(customer.tel1);
      edTel2.setText(customer.tel2);
      edFax.setText(customer.fax);
      edFedId.setEditable(false);
   }

   /**
    * Gets the customer general data to set the customer data.
    *
    * @param customer The customer data.
    */
   public void getFromEdit(Customer customer)
   {
      customer.name = edName.getText();
      customer.fedId = edFedId.getText();
      customer.address = edAddress.getText();
      customer.city = edCity.getText();
      customer.state = edState.getText();
      customer.country = edCountry.getText();
      customer.zip = edZip.getText();
      customer.tel1 = edTel1.getText();
      customer.tel2 = edTel2.getText();
      customer.fax = edFax.getText();
   }

   /**
    * Clears all customer general data edits.
    */
   public void cleanEdit()
   {
      edName.setText("");
      edFedId.setText("");
      edAddress.setText("");
      edCity.setText("");
      edState.setText("");
      edCountry.setText("");
      edZip.setText("");
      edTel1.setText("");
      edTel2.setText("");
      edFax.setText("");
      edFedId.setEditable(true);
   }
}