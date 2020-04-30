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
import totalcross.ui.gfx.Color;

/**
 * The contact information tab.
 */
public class NewContact extends Container
{
   /**
    * The contact name edit.
    */
   private Edit edName;

   /**
    * The contact role edit.
    */
   private Edit edRole;
   
   /**
    * The contact tel edit.
    */
   private Edit edCTel;
   
   /**
    * The contact cel edit.
    */
   private Edit edCel;
   
   /**
    * The contact e-mail edit.
    */
   private Edit edEmail;
   
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
      
      // Role label and edit.
      add(new Label("Role: "), LEFT + 5, AFTER + 2);
      add(edRole = new Edit(""), initX, SAME, PREFERRED - 5, PREFERRED);

      // E-mail label and edit.
      add(new Label("Email: "), LEFT + 5, AFTER + 2);
      add(edEmail = new Edit(""), initX, SAME, PREFERRED - 5, PREFERRED);

      // Contact tel label and edit.
      add(new Label("Tel.: "), LEFT + 5, AFTER + 2);
      add(edCTel = new Edit(""), initX, SAME, PREFERRED - 5, PREFERRED);

      // Cel label and edit.
      add(new Label("Cel.: "), LEFT + 5, AFTER + 2);
      add(edCel = new Edit(""), initX, SAME, PREFERRED - 5, PREFERRED);
   }

   /**
    * Sets the customer contact data in the edits.
    * 
    * @param customer The customer data.
    */
   public void setEdit(Customer customer)
   {
      edName.setText(customer.cName);
      edRole.setText(customer.role);
      edEmail.setText( customer.email);
      edCTel.setText(customer.cTel);
      edCel.setText(customer.cel);
   }

   /**
    * Gets the customer contact data to set the customer data.
    *
    * @param customer The customer data.
    */
   public void getFromEdit(Customer customer)
   {
      customer.cName = edName.getText();
      customer.role = edRole.getText();
      customer.email = edEmail.getText();
      customer.cTel = edCTel.getText();
      customer.cel = edCel.getText();
   }

   /**
    * Clears all customer contact data edits.
    */
   public void cleanEdit()
   {
      edName.setText("");
      edRole.setText("");
      edCTel.setText("");
      edEmail.setText("");
      edCel.setText("");
   }
}